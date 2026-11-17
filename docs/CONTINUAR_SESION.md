# Continuar CampusUDLATracker en otra sesión

## Objetivo pendiente del usuario

Publicar la aplicación para que los compañeros de grupo puedan verla mediante
un enlace. El usuario pidió que Codex se encargue de subir el código, configurar
y verificar el despliegue. Se eligió preparar Render en plan gratuito; no se han
autorizado servicios de pago. Aún no hay un despliegue ni una URL pública.

## Estado de la aplicación

- Java 17, Spring Boot 3.5.16, Maven, Thymeleaf, H2 en archivo.
- Tres paradas: Jipijapa → UDLA Granados → UDLA Park → Jipijapa.
- Tres buses: UDLA-01, UDLA-02 y UDLA-03. Máximo de tres unidades.
- Simulación con historial, ETA al siguiente campus, llegada y pausa de 12 segundos.
- Se eliminó el CRUD y la entidad de conductores por solicitud del usuario.
- Luego se añadió «Registrar llegadas» (/arrivals): permite activar control manual
  por bus, confirmar su siguiente parada, guardar hora e historial y volver al
  modo automático. No reintroduce el registro de conductores ni autenticación.
- Las llegadas manuales y simuladas se distinguen en ArrivalRecord y alimentan
  los reportes. La ubicación es simulada o declarada, sin GPS real.
- El pie de navegación dice «Avance 2».
- Se corrigió el arranque con bases antiguas: capacity y next_stop_index se
  agregan con DEFAULT 0. También se actualizan los escenarios iniciales conocidos
  a los tres campus y buses, conservando perfiles y favoritos válidos.

## Archivos que leer

1. AGENTS.md: reglas y decisiones del proyecto. Sus últimas decisiones reemplazan
   las referencias antiguas a conductores y escenarios anteriores.
2. docs/FASE_5.md: comportamiento y comprobación de la aplicación.
3. docs/DESPLIEGUE_RENDER.md: procedimiento de publicación y limitaciones.
4. Dockerfile, render.yaml, .dockerignore y
   src/main/resources/application-render.properties: despliegue ya preparado.

## Publicación pendiente

El último estado confirmado: GitHub y Render estaban disponibles pero no
instalados/conectados en esta sesión. Se sugirieron ambas conexiones y el
usuario preguntó cómo habilitarlas. Verificar su estado actual antes de repetir
la configuración: podrían haberse habilitado después de escribir este documento.

El usuario necesita iniciar sesión y autorizar sus propias cuentas. Codex no debe
pedir contraseñas ni asumir que una integración sugerida ya está conectada.
No había repositorio Git inicializado en esta carpeta al preparar el despliegue.
La CLI gh no pudo verificar GitHub por restricciones de red del entorno.

Una vez disponibles las conexiones, continuar con la publicación solicitada:
subir solo el código, crear/configurar el servicio Free y comprobar el enlace y
los logs del despliegue. No subir data/, target/, credenciales ni .env. La base
local se mantiene separada de la demo pública.

El plan gratuito tiene disco temporal: los cambios y confirmaciones se pierden
al suspender, reiniciar o desplegar. El usuario fue informado. La demo continúa
sin login: quien tenga el enlace puede modificar datos. No añadir autenticación
ni migrar la base a otro motor sin acordar ese cambio de alcance.

## Verificación realizada

Última suite: 37 pruebas aprobadas, 0 fallos y 0 errores. JAR empaquetado y
arranque sin HTTP con perfil render sobre H2 temporal correctos.

```bash
./mvnw -o -Daether.syncContext.named.factory=rwlock-local \
  -DargLine=-javaagent:/home/martin/.m2/repository/org/mockito/mockito-core/5.17.0/mockito-core-5.17.0.jar test
```

Para abrir localmente: ./iniciar.sh y http://localhost:8080.

No se pudo verificar el servidor HTTP ni construir la imagen Docker desde el
entorno restringido: sockets no permitidos y acceso al daemon Docker denegado.
No declarar el despliegue terminado sin comprobar una URL pública real.
