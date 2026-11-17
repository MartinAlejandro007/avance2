# Permisos separados para buses y llegadas

El usuario autorizó publicar después de la pausa inicial. Los archivos siguen
locales porque las conexiones con GitHub y Render no están habilitadas y la red
del entorno no permite subirlos. No hay publicación confirmada de estos cambios.
El Dockerfile ahora exige que las pruebas pasen antes de construir la imagen final.

## Accesos

| Acceso | Usuario | Acciones autorizadas |
|---|---|---|
| Visitante | Sin sesión | Consulta de flota, rutas, ETA y reportes |
| Administrador | `admin` | Registrar, editar y eliminar buses; cambiar su estado |
| Conductor | `conductor` | Panel de llegadas, control manual y confirmación de la siguiente parada |

Los roles son independientes: el administrador no confirma llegadas y el
conductor no modifica los registros de buses. No se añade una tabla de conductores.
La cuenta de conductor es compartida para esta demostración y puede operar los
buses del panel; no se implementa asignación individual de conductor a bus.
Los perfiles de demostración, favoritos y otros CRUD conservan su alcance previo;
esta fase protege la flota y el panel de llegadas, no todo el resto de escrituras.

El menú incluye **Acceso de personal**. Al iniciar sesión se abre el panel que
corresponde al rol. Los botones de gestión de flota solo se muestran al
administrador. Las acciones están protegidas también por Spring Security en el
servidor. Se mantiene protección CSRF en los formularios y el cierre de sesión.
La consola H2 local requiere administrador; sigue desactivada en el perfil Render.

## Definir contraseñas sin guardarlas en GitHub

Configurar dos variables de entorno con contraseñas distintas:

- `ADMIN_PASSWORD`: contraseña del usuario `admin`.
- `DRIVER_PASSWORD`: contraseña del usuario `conductor`.

No hay contraseñas predeterminadas. Si una variable está vacía, su cuenta no se
habilita. Las cuentas se crean al arrancar y las contraseñas se codifican con
BCrypt. Para cambiar una contraseña hay que cambiar su variable y reiniciar.

En una terminal local Bash puedes introducirlas sin que se vean ni queden en el
historial de comandos:

```bash
read -rsp 'Contraseña de administrador: ' ADMIN_PASSWORD
printf '\n'
export ADMIN_PASSWORD
read -rsp 'Contraseña de conductor: ' DRIVER_PASSWORD
printf '\n'
export DRIVER_PASSWORD
./iniciar.sh
```

Después abre http://localhost:8080/login. Las credenciales ficticias en
src/test/resources/application.properties se utilizan solo en las pruebas y no
se incluyen en la configuración de ejecución del JAR.

Cuando el usuario autorice publicar, configurar ambas variables en **Environment**
del servicio Render. render.yaml declara esos valores como `sync: false`, para
que se introduzcan fuera del repositorio. La autorización de publicación ya se recibió; falta el acceso a las cuentas.

## Verificación

Se añadieron pruebas para visitante, administrador y conductor, inicio y cierre
de sesión, contraseñas incorrectas, rechazo de acciones cruzadas y CSRF.
Las pruebas web existentes ahora incluyen usuario y token CSRF.

Resultado final: **43 pruebas aprobadas, 0 fallos, 0 errores, BUILD SUCCESS**.
La primera ejecución no pudo resolver dependencias; al reintentar ya estaban
disponibles en la caché. Se corrigió además el menú de acceso en páginas de error
cuando faltan atributos del modelo. El agente Mockito queda configurado en el POM.

Para descargar las dependencias y validar desde una terminal con internet:

```bash
./mvnw -Daether.syncContext.named.factory=rwlock-local \
  test
```

El iniciador ahora permite
que Maven descargue dependencias en lugar de forzar el modo offline.
