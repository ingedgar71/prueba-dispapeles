package org.dispapeles.pruebadispapeles;

import org.dispapeles.pruebadispapeles.dominio.mapper.ClienteMapper;
import org.dispapeles.pruebadispapeles.dominio.services.ClienteServices;
import org.dispapeles.pruebadispapeles.infraestructura.advice.ManejadorExcepciones;
import org.dispapeles.pruebadispapeles.infraestructura.controllers.ClienteController;
import org.dispapeles.pruebadispapeles.infraestructura.persistencia.ICliente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Levanta el contexto completo contra H2 (ver application-test.properties), de modo que
 * el cableado de beans se verifica sin depender de un MySQL levantado.
 *
 * <p>Ojo: al crear H2 el esquema desde las entidades, este test NO detecta desajustes
 * entre `ClienteEntity` y la tabla real de MySQL. Eso lo sigue cubriendo el
 * `ddl-auto=validate` al arrancar la aplicación.
 */
@SpringBootTest
@ActiveProfiles("test")
class PruebaDispapelesApplicationTests {

    @Autowired
    private ApplicationContext contexto;

    @Test
    @DisplayName("El contexto de la aplicación levanta")
    void contextLoads() {
        assertThat(contexto).isNotNull();
    }

    @Test
    @DisplayName("Los beans de cada capa quedan registrados y cableados")
    void beansPrincipalesDisponibles() {
        assertThat(contexto.getBean(ClienteController.class)).isNotNull();
        assertThat(contexto.getBean(ClienteServices.class)).isNotNull();
        assertThat(contexto.getBean(ICliente.class)).isNotNull();
        assertThat(contexto.getBean(ManejadorExcepciones.class)).isNotNull();
    }

    @Test
    @DisplayName("La implementación del mapper la aporta MapStruct")
    void mapperGeneradoPorMapStruct() {
        ClienteMapper mapper = contexto.getBean(ClienteMapper.class);

        assertThat(mapper.getClass().getSimpleName()).isEqualTo("ClienteMapperImpl");
    }
}
