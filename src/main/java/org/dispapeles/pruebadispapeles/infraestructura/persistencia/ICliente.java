package org.dispapeles.pruebadispapeles.infraestructura.persistencia;

import org.dispapeles.pruebadispapeles.dominio.entity.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ICliente extends JpaRepository<ClienteEntity, Long> {

    Optional<ClienteEntity> findByNumIdentificacion(String numIdentificacion);
}
