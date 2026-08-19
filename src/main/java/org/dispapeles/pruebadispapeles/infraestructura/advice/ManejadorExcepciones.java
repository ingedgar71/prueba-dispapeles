package org.dispapeles.pruebadispapeles.infraestructura.advice;


import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ManejadorExcepciones {

    private static final String INDICE_NUM_IDENTIFICACION = "uk_cliente_num_identificacion";

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail integridadDeDatos(DataIntegrityViolationException ex) {
        if (violaIndiceUnico(ex)) {
            return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                    "Ya existe un cliente con ese número de identificación.");
        }
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Los datos enviados violan una restricción de integridad.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail datosInvalidos(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(error.getField(), error.getDefaultMessage());
        }

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Los datos enviados no son válidos.");
        problema.setProperty("errores", errores);
        return problema;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail cuerpoIlegible(HttpMessageNotReadableException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "El cuerpo de la petición no es un JSON válido.");
    }

    /**
     * El nombre del índice solo aparece en el mensaje de la excepción de MySQL, que
     * viaja envuelta en la traducción de Spring; de ahí que haya que recorrer causas.
     */
    private boolean violaIndiceUnico(Throwable ex) {
        Throwable causa = ex;
        while (causa != null) {
            if (causa.getMessage() != null && causa.getMessage().contains(INDICE_NUM_IDENTIFICACION)) {
                return true;
            }
            causa = causa.getCause() == causa ? null : causa.getCause();
        }
        return false;
    }
}
