# Publicar Avance 2 en Render

> El usuario volvió a autorizar la publicación. Falta acceso a GitHub/Render
> desde esta sesión. Los nuevos permisos están en docs/PERMISOS.md. La imagen
> exige pasar las pruebas antes de publicarse; aún no hay despliegue confirmado.

La configuración está preparada para una demostración gratuita con Java 17,
Spring Boot, Thymeleaf y H2, con tres campus y tres buses. Docker compila el
proyecto en un contenedor Maven y ejecuta el JAR con Java 17, sin instalar Java
en los dispositivos de quienes visitan la web.

## Publicación

1. Sube el código a un repositorio de GitHub (puede ser privado). Incluye el
   archivo oculto `.mvn/` si usas el wrapper, `src/`, `pom.xml`, `Dockerfile`,
   `.dockerignore` y `render.yaml`. No subas `data/`, `target/`, credenciales
   ni archivos `.env`. La base local de tu computadora no se publica.
2. En https://dashboard.render.com crea una cuenta o inicia sesión con GitHub.
3. Selecciona **New → Blueprint**, conecta el repositorio y revisa el servicio
   definido en `render.yaml`: Docker, plan **Free**, una instancia.
4. Crea el Blueprint. Espera a que el despliegue termine y el servicio indique
   **Live**. El primer build descarga Maven y las dependencias; puede tardar.
5. Abre la URL `https://<nombre-asignado>.onrender.com` que muestre Render.
   Ese es el enlace para tus compañeros. No uses `localhost` para compartirlo.

También puedes crear **New → Web Service**, seleccionar el repositorio,
Language **Docker**, Dockerfile `./Dockerfile` y plan **Free**. La imagen activa
el perfil `render` y utiliza el puerto de la variable `PORT` automáticamente.

## Qué comprobar al abrir el enlace

- Vista general: tres buses y las paradas Jipijapa, UDLA Granados y UDLA Park.
- Rutas: ETA y posiciones de demostración que se actualizan.
- Registrar llegadas: activar control manual, confirmar la siguiente parada y
  comprobar el historial. Al terminar, devolver el bus a simulación automática.
- Reportes: aparecen las llegadas simuladas y declaradas registradas en la sesión.

## Datos y acceso de la demo

El plan gratuito suspende el servicio después de 15 minutos sin visitas; al
volver a abrirlo puede tardar alrededor de un minuto en despertar. Su disco es
temporal: al reiniciar, suspenderse o desplegar de nuevo, se pierden los cambios
y se vuelve a crear el escenario de demostración. Los datos de la aplicación
local permanecen separados.

Los cambios locales nuevos separan la gestión de buses y las llegadas con
cuentas de administrador y conductor; configurar ADMIN_PASSWORD y DRIVER_PASSWORD
antes del futuro despliegue. Ver docs/PERMISOS.md. Usa únicamente datos ficticios.
La consola H2 y los detalles internos de errores están desactivados en este perfil.

Para conservar cambios entre reinicios hay que acordar almacenamiento persistente:
un servicio con disco de pago o una base externa. Esta configuración no contrata
recursos de pago. Con un disco montado y accesible para el usuario 10001, puede
configurarse `JDBC_DATABASE_URL=jdbc:h2:file:/ruta/del/disco/intercampus`.

## Validación y límites

Ejecutar las pruebas del proyecto antes de publicar. El Dockerfile ejecuta
`mvn -B package`, incluyendo las pruebas. Si fallan, no genera la imagen final.
Mockito utiliza un agente explícito configurado en Maven para evitar depender
de la adjunción dinámica de agentes dentro del contenedor.
La construcción de la imagen requiere internet y acceso a Docker; si no están
disponibles localmente, Render realiza el build y sus logs confirman el resultado.
Un archivo preparado no implica que el despliegue ya esté publicado.

Documentación oficial:
- https://render.com/docs/blueprint-spec
- https://render.com/docs/web-services
- https://render.com/docs/free

Verificación local del 2026-11-17: **37 pruebas aprobadas**, JAR ejecutable
empaquetado correctamente y arranque sin HTTP del perfil `render` sobre una base
temporal completado. El entorno no permite acceso al daemon Docker ni abrir un
puerto HTTP: la construcción Docker y la comprobación de la URL pública quedan
pendientes del despliegue real. GitHub y Render todavía no están conectados.

Actualización de permisos: 43 pruebas aprobadas localmente. Configurar en
Environment las variables ADMIN_PASSWORD y DRIVER_PASSWORD con valores distintos
y privados antes de desplegar la actualización. El repositorio informado es
https://github.com/MartinAlejandro007/avance2. No hay publicación confirmada
de esta actualización desde Codex por falta de conexiones y red.
