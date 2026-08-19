package org.dispapeles.pruebadispapeles.dominio.services;


import org.dispapeles.pruebadispapeles.dominio.dto.ClienteDTO;
import org.dispapeles.pruebadispapeles.dominio.entity.ClienteEntity;
import org.dispapeles.pruebadispapeles.dominio.mapper.ClienteMapperImpl;
import org.dispapeles.pruebadispapeles.infraestructura.persistencia.ICliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServicesTest {

    private static final String NUM_IDENTIFICACION = "123456789";

    @Mock
    private ICliente iCliente;

    private ClienteServices clienteServices;

    @BeforeEach
    void setUp() {
        // Se usa la implementación real que genera MapStruct: mockearla solo duplicaría
        // el mapeo en las expectativas, y así los tests cubren también lo generado.
        clienteServices = new ClienteServices(iCliente, new ClienteMapperImpl());
    }

    @Test
    @DisplayName("getAll convierte cada entidad a DTO")
    void getAllDevuelveDtos() {
        when(iCliente.findAll()).thenReturn(List.of(entidadDeEjemplo()));

        List<ClienteDTO> resultado = clienteServices.getAll();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).numIdentificacion()).isEqualTo(NUM_IDENTIFICACION);
        assertThat(resultado.get(0).nombre()).isEqualTo("MARCELA");
    }

    @Test
    @DisplayName("getByNumIdentificacion devuelve el cliente mapeado cuando existe")
    void getByNumIdentificacionExistente() {
        when(iCliente.findByNumIdentificacion(NUM_IDENTIFICACION)).thenReturn(Optional.of(entidadDeEjemplo()));

        Optional<ClienteDTO> resultado = clienteServices.getByNumIdentificacion(NUM_IDENTIFICACION);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().nombre()).isEqualTo("MARCELA");
        assertThat(resultado.get().numIdentificacion()).isEqualTo(NUM_IDENTIFICACION);
    }

    @Test
    @DisplayName("getByNumIdentificacion devuelve vacío cuando no existe")
    void getByNumIdentificacionInexistente() {
        when(iCliente.findByNumIdentificacion("000")).thenReturn(Optional.empty());

        assertThat(clienteServices.getByNumIdentificacion("000")).isEmpty();
    }

    @Test
    @DisplayName("save persiste la entidad mapeada desde el DTO")
    void saveMapeaYPersiste() {
        when(iCliente.save(any(ClienteEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ClienteDTO resultado = clienteServices.save(dtoDeEjemplo());

        ArgumentCaptor<ClienteEntity> capturada = ArgumentCaptor.forClass(ClienteEntity.class);
        verify(iCliente).save(capturada.capture());
        assertThat(capturada.getValue().getNumIdentificacion()).isEqualTo(NUM_IDENTIFICACION);
        assertThat(capturada.getValue().getNombre()).isEqualTo("MARCELA");
        assertThat(resultado.nombre()).isEqualTo("MARCELA");
    }

    @Test
    @DisplayName("save nunca propaga el id del cuerpo hacia la entidad")
    void saveIgnoraElIdDeLaEntidadNueva() {
        when(iCliente.save(any(ClienteEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        clienteServices.save(dtoDeEjemplo());

        ArgumentCaptor<ClienteEntity> capturada = ArgumentCaptor.forClass(ClienteEntity.class);
        verify(iCliente).save(capturada.capture());
        assertThat(capturada.getValue().getId()).isNull();
    }

    @Test
    @DisplayName("update solo modifica los campos no nulos del DTO")
    void updateAplicaCambiosParciales() {
        ClienteEntity existente = entidadDeEjemplo();
        when(iCliente.findByNumIdentificacion(NUM_IDENTIFICACION)).thenReturn(Optional.of(existente));
        when(iCliente.save(any(ClienteEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ClienteDTO parcial = new ClienteDTO(null, null, null, null, null, "3009998877", null);
        Optional<ClienteDTO> resultado = clienteServices.update(NUM_IDENTIFICACION, parcial);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().telefono()).isEqualTo("3009998877");
        assertThat(resultado.get().nombre()).isEqualTo("MARCELA");
        assertThat(resultado.get().direccion()).isEqualTo("CALLE 132A # 89-91");
    }

    @Test
    @DisplayName("update devuelve vacío y no guarda cuando el cliente no existe")
    void updateSinCoincidencia() {
        when(iCliente.findByNumIdentificacion("000")).thenReturn(Optional.empty());

        Optional<ClienteDTO> resultado = clienteServices.update("000", dtoDeEjemplo());

        assertThat(resultado).isEmpty();
        verify(iCliente, never()).save(any(ClienteEntity.class));
    }

    @Test
    @DisplayName("delete elimina la entidad encontrada y confirma con true")
    void deleteExistente() {
        ClienteEntity existente = entidadDeEjemplo();
        when(iCliente.findByNumIdentificacion(NUM_IDENTIFICACION)).thenReturn(Optional.of(existente));

        boolean resultado = clienteServices.delete(NUM_IDENTIFICACION);

        assertThat(resultado).isTrue();
        verify(iCliente).delete(existente);
    }

    @Test
    @DisplayName("delete devuelve false y no borra nada cuando el cliente no existe")
    void deleteInexistente() {
        when(iCliente.findByNumIdentificacion("000")).thenReturn(Optional.empty());

        boolean resultado = clienteServices.delete("000");

        assertThat(resultado).isFalse();
        verify(iCliente, never()).delete(any(ClienteEntity.class));
    }

    private ClienteEntity entidadDeEjemplo() {
        ClienteEntity entity = new ClienteEntity();
        entity.setId(1L);
        entity.setTipoIdentificacion("CC");
        entity.setNumIdentificacion(NUM_IDENTIFICACION);
        entity.setNombre("MARCELA");
        entity.setApellidos("QUIÑONEZ");
        entity.setEdad(26);
        entity.setTelefono("3218956210");
        entity.setDireccion("CALLE 132A # 89-91");
        return entity;
    }

    private ClienteDTO dtoDeEjemplo() {
        return new ClienteDTO("CC", NUM_IDENTIFICACION, "MARCELA", "QUIÑONEZ", 26,
                "3218956210", "CALLE 132A # 89-91");
    }
}
