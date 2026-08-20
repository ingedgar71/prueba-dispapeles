package org.dispapeles.pruebadispapeles.infraestructura.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Los tamaños replican los de la tabla `cliente`, de modo que un valor demasiado
 * largo se rechaza con 400 en vez de reventar en MySQL con un 500.
 *
 * <p>Las restricciones de obligatoriedad viven en el grupo {@link Crear} porque solo
 * aplican al alta: en un PATCH los campos ausentes son legítimos y significan
 * "déjalo como está". Los límites de tamaño no llevan grupo, así que aplican siempre.
 */
public record ClienteDTO(
        @NotBlank(groups = ClienteDTO.Crear.class, message = "El tipo de identificación es obligatorio")
        @Size(max = 20, message = "El tipo de identificación no puede superar 20 caracteres")
        String tipoIdentificacion,

        @NotBlank(groups = ClienteDTO.Crear.class, message = "El número de identificación es obligatorio")
        @Size(max = 20, message = "El número de identificación no puede superar 20 caracteres")
        String numIdentificacion,

        @NotBlank(groups = ClienteDTO.Crear.class, message = "El nombre es obligatorio")
        @Size(max = 50, message = "El nombre no puede superar 50 caracteres")
        String nombre,

        @Size(max = 100, message = "Los apellidos no pueden superar 100 caracteres")
        String apellidos,

        @PositiveOrZero(message = "La edad no puede ser negativa")
        @Max(value = 150, message = "La edad no puede superar 150")
        Integer edad,

        @Size(max = 20, message = "El teléfono no puede superar 20 caracteres")
        String telefono,

        @Size(max = 200, message = "La dirección no puede superar 200 caracteres")
        String direccion
) {

    /** Grupo de validación para el alta, donde los campos obligatorios sí se exigen. */
    public interface Crear {
    }
}
