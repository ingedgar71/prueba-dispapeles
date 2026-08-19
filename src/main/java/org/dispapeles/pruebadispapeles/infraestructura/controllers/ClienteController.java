package org.dispapeles.pruebadispapeles.infraestructura.controllers;


import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.dispapeles.pruebadispapeles.dominio.dto.ClienteDTO;
import org.dispapeles.pruebadispapeles.dominio.services.ClienteServices;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private ClienteServices clienteService;

    public ClienteController(ClienteServices clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public ResponseEntity<List<ClienteDTO>> getAll(){
        List<ClienteDTO> lstCliente = clienteService.getAll();
        return new ResponseEntity<>(lstCliente, HttpStatus.OK);
    }

    @GetMapping("/{numIdentificacion}")
    public ResponseEntity<ClienteDTO> getByNumIdentificacion(@PathVariable String numIdentificacion){
        return clienteService.getByNumIdentificacion(numIdentificacion)
                .map(cliente -> new ResponseEntity<>(cliente, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<ClienteDTO> save(
            @Validated({ClienteDTO.Crear.class, Default.class}) @RequestBody ClienteDTO cliente){
        ClienteDTO clienteCreado = clienteService.save(cliente);
        return new ResponseEntity<>(clienteCreado, HttpStatus.CREATED);
    }

    @PatchMapping("/{numIdentificacion}")
    public ResponseEntity<ClienteDTO> update(
            @PathVariable String numIdentificacion, @Valid @RequestBody ClienteDTO cliente){
        return clienteService.update(numIdentificacion, cliente)
                .map(clienteActualizado -> new ResponseEntity<>(clienteActualizado, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{numIdentificacion}")
    public ResponseEntity<Void> delete(@PathVariable String numIdentificacion){
        if (!clienteService.delete(numIdentificacion)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
