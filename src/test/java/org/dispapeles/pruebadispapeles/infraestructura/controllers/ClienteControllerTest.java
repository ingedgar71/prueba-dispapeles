package org.dispapeles.pruebadispapeles.infraestructura.controllers;


import org.dispapeles.pruebadispapeles.infraestructura.dto.ClienteDTO;
import org.dispapeles.pruebadispapeles.dominio.services.ClienteServices;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    private static final String RUTA = "/api/v1/clientes";
    private static final String NUM_IDENTIFICACION = "123456789";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteServices clienteServices;

    @Test
    @DisplayName("GET devuelve 200 y la lista de clientes")
    void getAllDevuelve200() throws Exception {
        when(clienteServices.getAll()).thenReturn(List.of(dtoDeEjemplo()));

        mockMvc.perform(get(RUTA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numIdentificacion").value(NUM_IDENTIFICACION))
                .andExpect(jsonPath("$[0].nombre").value("MARCELA"));
    }

    @Test
    @DisplayName("GET no expone el id interno del cliente")
    void getAllNoExponeId() throws Exception {
        when(clienteServices.getAll()).thenReturn(List.of(dtoDeEjemplo()));

        mockMvc.perform(get(RUTA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").doesNotExist());
    }

    @Test
    @DisplayName("GET por número de identificación devuelve 200 con el cliente")
    void getPorNumIdentificacionDevuelve200() throws Exception {
        when(clienteServices.getByNumIdentificacion(NUM_IDENTIFICACION)).thenReturn(Optional.of(dtoDeEjemplo()));

        mockMvc.perform(get(RUTA + "/" + NUM_IDENTIFICACION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numIdentificacion").value(NUM_IDENTIFICACION))
                .andExpect(jsonPath("$.nombre").value("MARCELA"))
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    @DisplayName("GET de un número inexistente devuelve 404")
    void getPorNumIdentificacionInexistenteDevuelve404() throws Exception {
        when(clienteServices.getByNumIdentificacion("000")).thenReturn(Optional.empty());

        mockMvc.perform(get(RUTA + "/000"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST válido devuelve 201 con el cliente creado")
    void saveDevuelve201() throws Exception {
        when(clienteServices.save(any(ClienteDTO.class))).thenReturn(dtoDeEjemplo());

        mockMvc.perform(post(RUTA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoCompleto()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numIdentificacion").value(NUM_IDENTIFICACION));
    }

    @Test
    @DisplayName("POST sin campos obligatorios devuelve 400 y no llega al service")
    void saveSinObligatoriosDevuelve400() throws Exception {
        mockMvc.perform(post(RUTA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").exists())
                .andExpect(jsonPath("$.errores.tipoIdentificacion").exists())
                .andExpect(jsonPath("$.errores.numIdentificacion").exists());

        verify(clienteServices, never()).save(any(ClienteDTO.class));
    }

    @Test
    @DisplayName("POST con JSON malformado devuelve 400")
    void saveConJsonInvalidoDevuelve400() throws Exception {
        mockMvc.perform(post(RUTA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": no-es-json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH parcial devuelve 200 sin exigir los campos obligatorios")
    void updateParcialDevuelve200() throws Exception {
        when(clienteServices.update(eq(NUM_IDENTIFICACION), any(ClienteDTO.class)))
                .thenReturn(Optional.of(dtoDeEjemplo()));

        mockMvc.perform(patch(RUTA + "/" + NUM_IDENTIFICACION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"telefono\":\"3009998877\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH sobre un cliente inexistente devuelve 404")
    void updateInexistenteDevuelve404() throws Exception {
        when(clienteServices.update(eq("000"), any(ClienteDTO.class))).thenReturn(Optional.empty());

        mockMvc.perform(patch(RUTA + "/000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"telefono\":\"3009998877\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH con un campo que excede el tamaño devuelve 400")
    void updateConCampoLargoDevuelve400() throws Exception {
        String direccionLarga = "X".repeat(250);

        mockMvc.perform(patch(RUTA + "/" + NUM_IDENTIFICACION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"direccion\":\"" + direccionLarga + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.direccion").exists());

        verify(clienteServices, never()).update(any(), any(ClienteDTO.class));
    }

    @Test
    @DisplayName("DELETE existente devuelve 204")
    void deleteDevuelve204() throws Exception {
        when(clienteServices.delete(NUM_IDENTIFICACION)).thenReturn(true);

        mockMvc.perform(delete(RUTA + "/" + NUM_IDENTIFICACION))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE de un cliente inexistente devuelve 404")
    void deleteInexistenteDevuelve404() throws Exception {
        when(clienteServices.delete("000")).thenReturn(false);

        mockMvc.perform(delete(RUTA + "/000"))
                .andExpect(status().isNotFound());
    }

    private ClienteDTO dtoDeEjemplo() {
        return new ClienteDTO("CC", NUM_IDENTIFICACION, "MARCELA", "QUIÑONEZ", 26,
                "3218956210", "CALLE 132A # 89-91");
    }

    private String cuerpoCompleto() {
        return """
                {
                  "tipoIdentificacion": "CC",
                  "numIdentificacion": "123456789",
                  "nombre": "MARCELA",
                  "apellidos": "QUIÑONEZ",
                  "edad": 26,
                  "telefono": "3218956210",
                  "direccion": "CALLE 132A # 89-91"
                }
                """;
    }
}
