# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Comandos

Windows (PowerShell) — usar el wrapper `mvnw.cmd`; en Git Bash usar `./mvnw`.

```powershell
.\mvnw.cmd spring-boot:run                                   # levantar la app
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--server.port=8099   # en otro puerto
.\mvnw.cmd clean package                                     # compilar + tests + jar en target/
.\mvnw.cmd test                                              # toda la suite (no requiere MySQL, ver abajo)
.\mvnw.cmd test -Dtest=ClienteControllerTest                 # una clase de test
.\mvnw.cmd test -Dtest=ClienteControllerTest#deleteDevuelve204   # un solo test
```

No hay linter ni formateador configurado en el proyecto.

## Stack

Spring Boot **4.1.0**, con `java.version=17` (el JDK instalado puede ser mayor; el compilador apunta a 17). Importante: Boot 4 renombró los starters respecto de Boot 3, y este `pom.xml` usa los nombres nuevos. Al agregar dependencias, mantener esa nomenclatura:

- `spring-boot-starter-webmvc` (no `-web`)
- `spring-boot-starter-webmvc-test` y `spring-boot-starter-data-jpa-test` (no el `spring-boot-starter-test` monolítico)

Boot 4 también movió de paquete las anotaciones de test. Al escribir tests nuevos, copiar los imports de los ya existentes en vez de los de Boot 3 que sugieren el IDE o la memoria:

- `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest` (no `org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest`)
- `@MockitoBean` (`org.springframework.test.context.bean.override.mockito.MockitoBean`), no el `@MockBean` retirado.

## Arquitectura

Paquete raíz `org.dispapeles.pruebadispapeles`, dividido en dos capas por nombre de paquete:

- `dominio/` — `entity/` (entidades JPA), `dto/` (records expuestos por el API), `mapper/` (interfaces MapStruct) y `services/` (lógica de negocio, `@Service`).
- `infraestructura/` — `controllers/` (`@RestController`), `persistencia/` (interfaces `JpaRepository`, prefijadas con `I`) y `advice/` (manejo centralizado de excepciones).

Flujo: Controller → Service → Repository. Todas las dependencias se inyectan por constructor, sin `@Autowired`. Los controllers devuelven `ResponseEntity<T>` construido explícitamente con `HttpStatus`.

**Las entidades JPA no salen de la capa de dominio.** El service recibe y devuelve DTOs; el controller nunca ve un `*Entity`. Al agregar un recurso nuevo, replicar esa frontera: record DTO + mapper + service que traduce.

Convención de resultados en el service: `Optional<T>` vacío cuando el recurso no existe (→ 404) y `boolean` en las bajas. El controller decide el código HTTP a partir de eso, sin lanzar excepciones para el flujo normal.

No hay `@Transactional` explícito en ninguna capa: cada llamada al repositorio corre en su propia transacción y `spring.jpa.open-in-view=false`, así que **el mapeo entidad→DTO tiene que ocurrir dentro del service**, nunca después de devolver una entidad hacia arriba.

Los fallos de infraestructura sí van por excepción, centralizados en `infraestructura/advice/ManejadorExcepciones` (`@RestControllerAdvice`), que responde con `ProblemDetail` (RFC 7807): validación fallida → `400` con un mapa `errores` campo→mensaje, cuerpo JSON ilegible → `400`, violación del índice único → `409`. Distingue el `409` de unicidad buscando el nombre del índice en la cadena de causas, así que **si se renombra `uk_cliente_num_identificacion` en la base de datos hay que actualizar la constante** o el mensaje específico degrada al genérico.

Corolario: **la unicidad de `numIdentificacion` no se comprueba en Java** — el service no consulta antes de guardar; el `409` depende por completo del índice de MySQL. En los tests (H2, esquema generado desde la entidad, sin ese índice) ese camino no se ejercita.

### Validación

`ClienteDTO` lleva las restricciones Bean Validation, con los `@Size` calcados de los anchos de columna de MySQL para que un valor largo dé `400` y no un error de base de datos.

La obligatoriedad (`@NotBlank`) está en el grupo `ClienteDTO.Crear`, no en el grupo por defecto, porque solo aplica al alta. De ahí que los dos endpoints se validen distinto y **esa asimetría deba mantenerse al agregar campos**:

- `POST` → `@Validated({ClienteDTO.Crear.class, Default.class})`: exige obligatorios + tamaños.
- `PATCH` → `@Valid`: solo tamaños, ya que un campo ausente significa "déjalo como está".

Recurso actual: `ClienteEntity` (tabla `cliente`), expuesta como `ClienteDTO` en `/api/v1/clientes` — `GET`, `GET /{numIdentificacion}`, `POST`, `PATCH /{numIdentificacion}`, `DELETE /{numIdentificacion}`.

**El `id` de base de datos no sale al API.** `ClienteDTO` no tiene campo `id` y el mapper no lo copia: es una clave interna. El identificador público es `numIdentificacion`, y por él resuelven las rutas vía `ICliente.findByNumIdentificacion`. Al agregar endpoints, mantener ese criterio.

El `PATCH` es parcial de verdad: `ClienteMapper.aplicarCambios` solo copia los campos no nulos del DTO. Consecuencia a tener presente: como `numIdentificacion` es un campo más del DTO, **un `PATCH` que lo incluya reescribe el identificador público del recurso**.

### Mapeo (MapStruct)

`ClienteMapper` es una **interfaz**; la implementación (`ClienteMapperImpl`, un `@Component`) la genera el annotation processor en `target/generated-sources/annotations` durante la compilación. No se edita a mano ni se versiona.

- `unmappedTargetPolicy = ERROR`: un campo nuevo en el DTO o la entidad sin mapear **rompe la compilación**, en vez de perderse en silencio. Es el motivo principal de usar la librería aquí.
- El `PATCH` parcial sale de `@BeanMapping(nullValuePropertyMappingStrategy = IGNORE)`.
- El `id` se ignora explícitamente en los mapeos hacia la entidad.

Los tests instancian `new ClienteMapperImpl()`, así que **si el processor no corrió, el código de test no compila**. En IntelliJ hace falta *Enable annotation processing* además del reload de Maven; si el IDE marca `ClienteMapperImpl` como inexistente pero `.\mvnw.cmd test` pasa, el problema es del IDE.

## Estado del proyecto

Es un proyecto en etapa inicial (prueba técnica).

### Base de datos

MySQL 8.0 local, esquema `empresa`, tabla `cliente`. Las credenciales se leen de las variables de entorno **`DB_USER_MYSQL`** (con `root` como valor por defecto) y **`DB_PASSWORD_MYSQL`** (sin valor por defecto: si falta, la app no arranca) — `application.properties:5-6`. En IntelliJ se definen en la Run Configuration, no a nivel de sistema, para no chocar con las de otros proyectos.

`spring.jpa.hibernate.ddl-auto=validate`: Hibernate no crea ni modifica el esquema, solo lo verifica contra `ClienteEntity`. Cualquier cambio en la entidad exige un `ALTER TABLE` manual equivalente, o el arranque falla con `SchemaManagementException`. Hibernate aborta en el primer desajuste que encuentra, así que pueden quedar más errores ocultos detrás del que reporta.

El esquema que `ClienteEntity` espera encontrar (los anchos se replican en los `@Size` de `ClienteDTO`):

| columna | tipo | notas |
|---|---|---|
| `id` | `BIGINT` autoincrement | PK interna, nunca sale al API |
| `tipo_identificacion` | `VARCHAR(20)` | |
| `numero_identificacion` | `VARCHAR(20)` | índice único `uk_cliente_num_identificacion`; **el nombre de columna no coincide con el del campo** (`numIdentificacion`), va explícito en `@Column` |
| `nombre` | `VARCHAR(50)` | |
| `apellidos` | `VARCHAR(100)` | |
| `edad` | `INT` | |
| `telefono` | `VARCHAR(20)` | |
| `direccion` | `VARCHAR(200)` | |

### Tests

La suite (**24 tests en 3 clases**) corre **sin MySQL**: `.\mvnw.cmd test` no necesita variables de entorno ni base de datos levantada.

- `ClienteServicesTest` — unitario con Mockito sobre `ICliente`, pero usando el `ClienteMapperImpl` **real** en vez de un mock, para que el mapeo generado también quede cubierto.
- `ClienteControllerTest` — `@WebMvcTest` con `@MockitoBean ClienteServices`: contrato HTTP (códigos, JSON, validación por grupo, que el `id` no se filtre). No toca base de datos.
- `PruebaDispapelesApplicationTests` — `@SpringBootTest` contra **H2 en memoria** vía `@ActiveProfiles("test")` + `src/test/resources/application-test.properties`, que sobrescribe solo el datasource y pone `ddl-auto=create-drop`. Cubre el cableado de beans.

**Lo que la suite no cubre:** como H2 crea el esquema a partir de las entidades, un desajuste entre `ClienteEntity` y la tabla real de MySQL pasa desapercibido, igual que el `409` del índice único. Esa verificación la sigue haciendo `ddl-auto=validate` al arrancar la aplicación contra MySQL, no los tests.

### Si el arranque falla con `Cannot load driver class: com.mysql.cj.jdbc.Driver`

Es el classpath de IntelliJ, no el proyecto: el IDE cachea el modelo del módulo en `%LOCALAPPDATA%\JetBrains\<version>\projects\prueba-dispapeles.<hash>\external_build_system\modules\`. Si ese XML no lista `mysql-connector-j`, el IDE quedó con un modelo previo — hay que hacer *Maven → Reload Project*. Arrancar con `.\mvnw.cmd spring-boot:run` no depende de esa caché y sirve para descartar el IDE.
