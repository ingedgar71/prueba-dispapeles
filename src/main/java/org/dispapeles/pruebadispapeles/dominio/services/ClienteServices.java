package org.dispapeles.pruebadispapeles.dominio.services;


import org.dispapeles.pruebadispapeles.dominio.dto.ClienteDTO;
import org.dispapeles.pruebadispapeles.dominio.entity.ClienteEntity;
import org.dispapeles.pruebadispapeles.dominio.mapper.ClienteMapper;
import org.dispapeles.pruebadispapeles.infraestructura.persistencia.ICliente;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteServices {
    private ICliente iCliente;
    private ClienteMapper clienteMapper;

    public ClienteServices(ICliente iCliente, ClienteMapper clienteMapper) {
        this.iCliente = iCliente;
        this.clienteMapper = clienteMapper;
    }

    public List<ClienteDTO> getAll() {
        return iCliente.findAll()
                .stream()
                .map(clienteMapper::toDto)
                .toList();
    }

    public Optional<ClienteDTO> getByNumIdentificacion(String numIdentificacion) {
        return iCliente.findByNumIdentificacion(numIdentificacion)
                .map(clienteMapper::toDto);
    }

    public ClienteDTO save(ClienteDTO cliente) {
        ClienteEntity entity = clienteMapper.toEntity(cliente);
        return clienteMapper.toDto(iCliente.save(entity));
    }

    public Optional<ClienteDTO> update(String numIdentificacion, ClienteDTO cliente) {
        return iCliente.findByNumIdentificacion(numIdentificacion)
                .map(entity -> {
                    clienteMapper.aplicarCambios(cliente, entity);
                    return clienteMapper.toDto(iCliente.save(entity));
                });
    }

    public boolean delete(String numIdentificacion) {
        return iCliente.findByNumIdentificacion(numIdentificacion)
                .map(entity -> {
                    iCliente.delete(entity);
                    return true;
                })
                .orElse(false);
    }
}
