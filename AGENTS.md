# CampusUDLATracker — Contexto del proyecto

> Cambio de alcance vigente (2026-11-17): por solicitud del usuario se elimina
> la gestión de conductores y su asignación a buses. Las referencias a ese
> componente en el alcance original y las decisiones históricas quedan superadas.
> Las unidades conservan placa/disco, capacidad, ruta, estado y posición simulada.

## Identidad y objetivo

**CampusUDLATracker** es una aplicación web de **monitoreo y rastreo GPS en tiempo real del transporte intercampus de la UDLA**.

- **Cliente:** Dirección de Logística y Servicios Generales.
- **Problema que resuelve:** hoy nadie sabe dónde están los buses ni cuándo llegan; los horarios estáticos rara vez se cumplen, hay esperas largas, aglomeraciones e impuntualidad, y la administración no registra tiempos ni frecuencias reales por ruta.
- **Meta:** hasta **40 % menos de espera**, con visibilidad total de los buses, mejor planificación de viajes y métricas para optimizar frecuencias y rutas.

La **ubicación de los buses es simulada** para desarrollo y demostración. No se conecta a dispositivos GPS reales. Es un proyecto de aprendizaje de Java y POO: se prioriza código claro, bien encapsulado y fácil de explicar sobre soluciones «inteligentes» pero opacas.

## Equipo y responsables

| Rol | Responsable |
|---|---|
| Líder y arquitectura POO | Joselin Guachamin |
| Análisis, diseño y backend | Martín Estevez |
| Interfaz gráfica (GUI) y QA | Sebastián Cazco y Uriel Fierro |
| Pruebas integradas y entrega | Todo el equipo |
| Docente | Pablo Garces |

Planificación original: arquitectura en semana 1, backend en semanas 2–3, GUI y QA en semanas 3–4, pruebas integradas y entrega final en semana 5.

## Stack

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 17 (records, `switch` con patrones y `sealed` disponibles; **no** virtual threads, son preview en 17) |
| Build | Maven (wrapper `./mvnw` incluido) |
| Framework | Spring Boot 3.5.16 |
| Vistas | Thymeleaf (HTML renderizado en el servidor) |
| Persistencia | JPA/Hibernate con **H2 en archivo** (`jdbc:h2:file:./data/intercampus`) |
| Tests | JUnit 5 + Spring Boot Test + Mockito |
| Lombok | Opcional; si se usa, solo `@Getter`, `@RequiredArgsConstructor`, `@Slf4j`. **Prohibido `@Data`** |

## Alcance: tres módulos

Cada módulo combina **gestión (CRUD)** y **una regla de negocio**.

### Módulo 1 — Rutas, paradas y GPS
Gestión de rutas y paradas, y cálculo del **tiempo de llegada (ETA)** con distancia haversine.
- Requerimiento `MOD1-02` — backend: Martín Estevez.

### Módulo 2 — Flota y unidades
Registro de buses y conductores, y alertas por desvío de ruta o detención no programada.
- Requerimientos `MOD2-01` y `MOD2-02`.

### Módulo 3 — Usuarios y reportes
Perfiles con rutas favoritas e indicadores de puntualidad y retraso promedio por ruta.
- Requerimientos `MOD3-01` y `MOD3-02` — frontend y QA: Sebastián Cazco y Uriel Fierro.

## Reglas de negocio

**ETA dinámico (MOD1-02).** Entradas: identificador de unidad, parada seleccionada, coordenadas de la posición simulada y velocidad actual. Paso 1: distancia **haversine** entre la posición del bus y la parada destino. Paso 2: ETA con un factor de velocidad promedio, mostrado en **minutos y metros**. Resultado: la parada pasa a estado **«Próximo»** o **«En Parada»** según la cercanía. La velocidad la genera el simulador, no un GPS real.

**Flota y alertas (MOD2).** Las unidades se registran por **número de disco o placa**, con **capacidad** y **conductor asignado**. Estados operativos: **Activo**, **Mantenimiento** o **Fuera de Servicio**. La **geocerca** valida que el bus se mantenga dentro del **umbral de tolerancia** de su ruta. Se generan alertas por **unidad desviada** y por **detención no programada prolongada**, visibles en el panel de control.

**Usuarios y reportes (MOD3).** Perfil con nombre, correo institucional y tipo: **estudiante**, **docente** o **administrativo**. Acceso rápido a **rutas y paradas favoritas** con preferencias visuales. Reportes de **puntualidad** y **retraso promedio por ruta y franja horaria**, más un **gráfico de eficiencia operativa** con minutos promedio de retraso intercampus.

## Modelo de dominio

Conceptos centrales y sus relaciones:

- **Ruta**: recorrido identificado (código/nombre), con una lista ordenada de paradas.
- **Parada**: punto físico con nombre y coordenadas; según el ETA puede estar «Próxima» o «En Parada».
- **Bus / Unidad**: placa o número de disco, capacidad, ruta asignada, conductor asignado, estado operativo y posición actual.
- **Conductor**: registro del responsable de una unidad.
- **Coordenada**: *value object* inmutable con `latitud` y `longitud`. No exponer setters.
- **Posicion**: ubicación de un bus en un instante (`Coordenada`, `momento` y `velocidad`). Inmutable. La velocidad alimenta el ETA y la generación es simulada.
- **EstadoOperativo**: enumeración `ACTIVO`, `MANTENIMIENTO`, `FUERA_DE_SERVICIO`.
- **TipoUsuario**: enumeración `ESTUDIANTE`, `DOCENTE`, `ADMINISTRATIVO`.
- **Usuario**: nombre, correo institucional, tipo y preferencias; puede marcar rutas y paradas favoritas.
- **Alerta**: desvío de ruta o detención no programada, con fecha, unidad afectada y descripción.
- **Reporte**: puntualidad y retraso promedio por ruta y franja horaria.

Relaciones: una `Ruta` tiene muchas `Paradas` (el orden importa) y muchas `Alertas`; un `Bus` pertenece a una `Ruta`, tiene un `Conductor`, un `EstadoOperativo` y muchas `Posiciones` históricas; un `Usuario` tiene muchas rutas y paradas favoritas.

## Estructura de paquetes

```
ec.intercampus.intercampus
├── IntercampusApplication.java
├── DatosIniciales.java
├── domain/          # entidades JPA, enumeraciones y value objects
├── repository/      # interfaces Spring Data, sin implementación manual
├── service/         # interfaz <Feature>Service + <Feature>ServiceImpl
├── simulation/      # generador de posiciones simuladas y velocidad
├── alert/           # reglas de geocerca y generación de alertas
├── report/          # cálculo de puntualidad y retrasos
├── web/             # controllers Thymeleaf
└── dto/             # records para pasar datos a las vistas
```

## Reglas de POO (obligatorias)

1. **Encapsulación**: campos privados; el estado se modifica solo por métodos con significado de negocio (`bus.avanzarA(coordenada)`, no `bus.setLatitud(x)`).
2. **Interfaces + implementación**: todo servicio se define como interfaz y tiene una sola implementación (`RutaService` / `RutaServiceImpl`). Los controllers y tests dependen de la interfaz.
3. **Inyección por constructor** siempre. Nunca `@Autowired` sobre campos (salvo en tests).
4. **Composición antes que herencia**. No crear jerarquías de clases para reutilizar un par de métodos.
5. **Inmutabilidad por defecto**: `record` para DTOs y value objects; `final` en campos inyectados. Sin estado `static` mutable.
6. **Responsabilidad única**: un cambio de regla de negocio debe tocar un solo lugar. Si un método hace dos cosas, separarlo.
7. **Sin excepciones genéricas**: no `catch (Exception e)` vacío, no excepciones sin mensaje. Usar una excepción de dominio con código de error.
8. **Recursos**: `try-with-resources` para cualquier `Closeable`.
9. **Nombres en inglés para el código**, **comentarios y mensajes de usuario en español**. Métodos con nombres que describan intención: `obtenerRutasActivas()`, no `getDatos()`.

## Persistencia

- Entidades JPA en `domain/`, sin lógica de negocio dentro.
- Repositorios son interfaces que extienden `JpaRepository`; no escribir `*RepositoryImpl`.
- `spring.jpa.hibernate.ddl-auto=update` durante desarrollo.
- La consola H2 está habilitada en desarrollo (`spring.h2.console.enabled=true`).
- `spring.jpa.open-in-view=false`: las vistas reciben DTOs, nunca entidades JPA.
- **Antes de escribir una query**, confirmar el esquema con la entidad real; no asumir nombres de columnas.

## Simulación de ubicación

- **La ubicación es simulada. No hay integración con GPS real.**
- El generador vive en `simulation/` y expone una interfaz clara (por ejemplo `SimuladorPosiciones`).
- Usar `@Scheduled` con intervalo configurable en `application.properties`; **nunca** un `Thread` suelto ni estado `static` compartido.
- Cada avance produce una `Posicion` inmutable (coordenada, momento y velocidad simulada) y se persiste; la posición «actual» se obtiene por consulta, no acumulándola en memoria.
- El simulador mueve cada bus hacia la siguiente parada de su ruta y calcula la distancia haversine restante para el ETA.
- La simulación debe poder desactivarse con una propiedad para que los tests no dependan del reloj.

## Vistas (Thymeleaf)

- Plantillas en `src/main/resources/templates/`, estáticos en `static/`.
- Los controllers pasan **records DTO**, nunca entidades JPA directamente a la vista.
- Deben existir: listado de rutas, detalle de ruta con paradas y estados «Próximo»/«En Parada», vista de unidades con su estado operativo, panel de alertas, perfil con favoritos y pantalla de reportes con el gráfico.
- La representación del mapa (Leaflet + OpenStreetMap u otra) es una **decisión abierta**; preguntar antes de añadir dependencias o scripts externos.

## Tests

- Unitarios de servicios con Mockito; `@InjectMocks` apunta a la **implementación** (Mockito no puede instanciar interfaces). Los demás mocks son de la interfaz.
- Slice tests: `@DataJpaTest` para repositorios, `@WebMvcTest` para controllers. Reservar `@SpringBootTest` para integración real; usarlo en cada test genera una suite lentísima.
- Probar por separado el haversine, el ETA, la geocerca y el cálculo de puntualidad; son reglas de negocio puras.
- Nombres de test en español o inglés, pero **consistentes** en todo el proyecto (elegir uno y mantenerlo).

## Flujo de trabajo con Codex

- Ejecutar `./mvnw test` después de cada cambio relevante y reportar el resultado real; no decir «debería compilar».
- Si una API o versión no se conoce con certeza, **preguntar o consultar documentación** en lugar de inventarla.
- No añadir dependencias, endpoints ni tablas que no estén en el alcance acordado.
- Mantener los comentarios del código en español, acordes con el resto del proyecto.
- Trabajar **una fase o un requerimiento por vez** (por ejemplo `MOD1-02`), no el proyecto completo en una sola instrucción.
- Al terminar una fase, actualizar el apartado «Decisiones registradas».

## Alcance y fases

1. **Fase 1 — Dominio y persistencia** *(completada)*: entidades `Ruta`, `Parada`, `Bus`, `Coordenada`; repositorios; datos de ejemplo en H2.
2. **Fase 2 — Servicios** *(completada)*: interfaces + implementaciones para rutas, paradas, unidades, conductores y usuarios.
3. **Fase 3 — Simulación y reglas GPS**: generador con `@Scheduled`, distancia haversine y ETA (`MOD1-02`).
4. **Fase 4 — Flota y alertas**: estados operativos, geocerca, detección de desvíos y detenciones (`MOD2-01`, `MOD2-02`).
5. **Fase 5 — Web**: vistas Thymeleaf, mapa, panel de alertas, favoritos y reportes (`MOD3-01`, `MOD3-02`).
6. **Fase 6 — Tests integrados y entrega**: cobertura de servicios, vistas y reglas de negocio; integración de los tres módulos.

> Pendiente de confirmar con el usuario: librería del mapa y si se requiere autenticación. El CRUD de los tres módulos **sí está confirmado** por el documento de avance.

## Decisiones registradas

- 2026-11-17: se elige Java 17 + Spring Boot 3.5.16 + Thymeleaf, H2 en archivo y **ubicación simulada** (sin GPS real).
- 2026-11-17: se incorpora el documento *CampusUDLATracker — Avance del proyecto final*: tres módulos, requerimientos `MOD1-02`, `MOD2-01`, `MOD2-02`, `MOD3-01`, `MOD3-02`, equipo y meta de 40 % menos de espera.
- 2026-11-17: la velocidad necesaria para el ETA la produce el simulador, no un dispositivo GPS.

- 2026-11-17: fase 2: servicios CRUD en inglés con DTOs inmutables y transacciones. Se incorporan Driver y UserProfile (tabla user_profiles), tipo de usuario, capacidad y conductor opcional del bus; los buses existentes conservan capacidad 0 hasta completarse su registro. Se impide borrar rutas con buses y conductores asignados. Las paradas se agregan al final y se reordenan al borrar. Favoritos, estados operativos y reglas GPS se implementarán en sus fases.

- 2026-11-17: fase 2 completada: cinco servicios CRUD con interfaz e implementación, DTOs, validaciones y protección de referencias. Verificación: 14 pruebas, 0 fallos y 0 errores, con Maven offline, bloqueos locales y agente Mockito explícito por restricciones del entorno. Detalles en `docs/FASE_2.md`.

- 2026-11-17: se autoriza continuar secuencialmente hasta la interfaz (fases 3–5). Se conserva Java/Thymeleaf local. La simulación tendrá historial persistido, reloj inyectable, velocidad configurable, recorrido circular y pausa en cada parada. El ETA usa haversine y velocidad efectiva (respaldo configurable si está detenido); es una estimación directa, no tráfico real. Las coordenadas de demostración son ficticias y se indicará en pantalla.
- 2026-11-17: fase 3 completada y verificada: 20 pruebas aprobadas. Historial consultado por bus y fecha, simulación desactivada en tests y ETA con respaldo de velocidad explícito.
- 2026-11-17: fase 4: geocerca medida contra los segmentos del recorrido circular, no solo sus paradas. Una alerta activa por bus y tipo; se resuelve cuando desaparece la condición. Las detenciones en paradas y las unidades no activas no generan alertas de detención. Umbrales configurables (150 m de geocerca y 60 s de detención por defecto).
- 2026-11-17: fase 4 completada y verificada: 22 pruebas aprobadas. Estados operativos y alertas persistidas con resolución automática.
- 2026-11-17: fase 5: interfaz Thymeleaf local sin autenticación (selección explícita de perfiles de demostración). Mapa esquemático SVG y recursos locales hasta confirmar proveedor externo. Favoritos persistidos por perfil y preferencia claro/oscuro. Reportes calculados sobre llegadas simuladas persistidas, agrupados por ruta y franja horaria de Ecuador; puntualidad con tolerancia configurable de 2 minutos respecto al horario teórico de cada tramo, sin afirmar datos GPS reales ni reducción de espera medida.
- 2026-11-17: fase 5 validada automáticamente: 28 pruebas aprobadas, 0 fallos y 0 errores. Se añadieron pruebas de integración web con renderizado Thymeleaf real y pruebas puras de puntualidad. JavaScript e iniciador verificados sintácticamente. El entorno bloquea sockets, por lo que queda pendiente la comprobación manual del servidor y del navegador en la computadora del usuario. Se incorpora `iniciar.sh` y el recorrido de verificación en `docs/FASE_5.md`; no se declara completada la revisión visual ni la fase 6.

- 2026-11-17: se retira la gestión de conductores a petición del usuario: navegación, pantalla, endpoints, servicio, repositorio, DTO, entidad y asignación en buses. Se actualizan datos de demostración y pruebas. No se ejecutan borrados de datos ni tablas sobre H2 existente; con ddl-auto=update pueden permanecer estructuras históricas sin uso. Verificación: 27 pruebas aprobadas, 0 fallos y 0 errores (se elimina la prueba de borrado de conductor que dejó de aplicar).
- 2026-11-17: Avance 2: el usuario solicita tres paradas (Jipijapa, UDLA Granados y UDLA Park) y tres buses. Se configura un único recorrido circular en ese orden, con UDLA-01/02/03 inicialmente en campus distintos y límite de tres unidades en el servicio. Se muestran llegada automática al campus durante la pausa, siguiente campus y ETA incluyendo espera restante, sin GPS real. Se actualiza al reiniciar únicamente el escenario anterior reconocido; se conservan perfiles, favoritos válidos y llegadas históricas, respetando recorridos personalizados. El detalle y la vista general consumen JourneyDto; las reglas de ETA permanecen centralizadas.
- 2026-11-17: verificación del ajuste de tres campus y buses: 34 pruebas aprobadas, 0 fallos y 0 errores, incluida migración del escenario de demostración, límite de flota, estados del recorrido y ETA con pausa. JavaScript verificado con node --check. Sigue pendiente la revisión visual manual por la restricción de sockets del entorno.
- 2026-11-17: se corrige el arranque con H2 de fase 1: Hibernate no podía agregar capacity ni next_stop_index NOT NULL a buses existentes. Ambas columnas se crean con DEFAULT 0, conservando los datos. Se incorpora migración del escenario original R-01/R-02 y BUS-001/BUS-002 al circuito de tres campus y buses. La nueva prueba LegacyDatabaseStartupTest reproduce un esquema antiguo con datos, arranca Spring con ddl-auto=update sin servidor HTTP y verifica la actualización. Suite: 35 pruebas aprobadas, 0 fallos y 0 errores.
- 2026-11-17: el usuario solicita un apartado para que los conductores registren llegadas. Se añade /arrivals (Registrar llegadas), sin recuperar el CRUD ni la entidad Driver y manteniendo la demostración sin autenticación. Control manual persistido por bus: detiene su simulación; permite confirmar únicamente la siguiente parada con hora de servidor y consulta de las últimas 30 confirmaciones. Cada confirmación actualiza posición declarada, pausa, siguiente destino y horario teórico; los reportes incluyen estas observaciones. ArrivalRecord distingue SIMULATED y DRIVER_CONFIRMED con valor por defecto compatible con H2 anterior. Simulador y servicio manual usan bloqueos por unidad para evitar transiciones simultáneas. Se puede volver a la simulación automática.
- 2026-11-17: validación del panel de llegadas: 37 pruebas aprobadas, 0 fallos y 0 errores. Se comprueban renderizado del panel e historial, confirmación persistida, rechazo de repetición/parada incorrecta/unidad no activa, pausa del simulador y retorno al modo automático. La prueba de base antigua conserva llegadas históricas y verifica los valores por defecto de origen y control manual. JavaScript validado sintácticamente; revisión visual manual pendiente por la restricción de sockets.
- 2026-11-17: el usuario solicita publicar para su grupo. Se prepara despliegue gratuito en Render con Docker de Java 17, render.yaml y perfil application-render.properties (PORT configurable, consola H2 desactivada y errores internos ocultos). .dockerignore excluye base local, compilados y credenciales. La demo mantiene H2 temporal y acceso sin autenticación; no se contratan recursos de pago. Verificación: 37 pruebas aprobadas, empaquetado JAR correcto y arranque sin HTTP del JAR con perfil render sobre H2 temporal. Docker no pudo construirse en este entorno por falta de acceso al daemon. Pendiente conectar GitHub/Render, subir el repositorio y ejecutar/verificar el despliegue real; aún no hay URL pública. Guía en docs/DESPLIEGUE_RENDER.md.
- 2026-11-17: el usuario solicita permisos separados para gestionar buses y confirmar llegadas. Se preparan localmente Spring Security, sesión de personal y roles independientes: ADMIN (usuario admin) para POST /fleet/** y DRIVER (usuario conductor) para /arrivals/**. Las cuentas se habilitan solo al definir ADMIN_PASSWORD y DRIVER_PASSWORD fuera del repositorio; contraseñas BCrypt, formularios con CSRF y menú condicionado al rol. No se incorpora tabla de conductores ni permisos por bus. La consola H2 local queda reservada a ADMIN. Otros CRUD de demostración mantienen su alcance previo. Detalles en docs/PERMISOS.md.
- 2026-11-17: instrucción explícita del usuario: NO subir ni desplegar nada en Render hasta que lo indique. Mantener cambios locales; no hacer push a GitHub si puede provocar auto-deploy. No se ha publicado ningún cambio de permisos desde esta sesión.
- 2026-11-17: intento de ./mvnw test con permisos: BUILD FAILURE antes de compilar, por dependencias spring-boot-starter-security 3.5.16 y spring-security-test 6.5.11 no disponibles en caché; el entorno no resuelve repo.maven.apache.org. Se añaden seis pruebas de autorización y se adaptan las pruebas web existentes, pero no se declaran aprobadas. POM, YAML e iniciar.sh verificados sintácticamente. La validación funcional/compilación queda pendiente de descargar dependencias desde una terminal con internet. iniciar.sh deja de forzar -o para permitir esa descarga.
- 2026-11-17: el usuario revoca la pausa anterior y autoriza publicar («ahora sí súbelo», «está todo bien»). GitHub informado: https://github.com/MartinAlejandro007/avance2. No repetir aprobación; falta acceso conectado a GitHub/Render y el ejecutor no resuelve sus dominios. La autorización no equivale a despliegue realizado.
- 2026-11-17: las dependencias de seguridad ya están disponibles en la caché al reintentar. Se corrige la condición de sesión del menú para páginas de error con modelo incompleto. Verificación real final: 43 pruebas aprobadas, 0 fallos y 0 errores. Maven configura el agente Mockito con la versión heredada de Spring Boot; Docker ejecuta mvn package con pruebas, evitando generar la imagen final si fallan. Preparar actualización pequeña de archivos para el repositorio, sin datos ni contraseñas, mientras no haya conexión de publicación.
