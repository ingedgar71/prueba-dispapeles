package org.dispapeles.pruebadispapeles.infraestructura.mapper;


import org.dispapeles.pruebadispapeles.infraestructura.dto.ClienteDTO;
import org.dispapeles.pruebadispapeles.infraestructura.entity.ClienteEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * La implementación la genera MapStruct al compilar (`target/generated-sources`).
 *
 * <p>Con {@code unmappedTargetPolicy = ERROR}, agregar un campo a la entidad o al DTO
 * sin mapearlo rompe la compilación en vez de perderse en silencio.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ClienteMapper {

    ClienteDTO toDto(ClienteEntity entity);

    /** El id es interno: lo asigna la base de datos, nunca el cuerpo de la petición. */
    @Mapping(target = "id", ignore = true)
    ClienteEntity toEntity(ClienteDTO dto);

    /**
     * Semántica de PATCH: los campos nulos del DTO se ignoran y la entidad conserva
     * su valor actual.
     */
    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void aplicarCambios(ClienteDTO dto, @MappingTarget ClienteEntity entity);
}
