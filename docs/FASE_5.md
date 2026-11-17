# Fase 5 — Validación y comprobación local

## Abrir la aplicación

Abre una terminal en la carpeta del proyecto y ejecuta:

```bash
./iniciar.sh
```

Cuando aparezca `Started IntercampusApplication`, abre http://localhost:8080.
Mantén la terminal abierta. Para detener la aplicación pulsa Ctrl+C.
El script permite descargar las dependencias Maven que falten; la primera
ejecución después de añadir dependencias necesita conexión a internet.

Los cambios hechos en la interfaz se guardan en `data/intercampus.mv.db`.
No borres ese archivo si quieres conservarlos. Los datos de ejemplo se cargan
solo cuando no existen rutas; una base anterior puede mostrar otros registros.

## Recorrido manual de comprobación

1. Vista general: verifica que aparezcan rutas y buses. Espera 10 segundos;
   la hora de actualización y las posiciones deben cambiar cuando la simulación
   está activa. El recorrido tiene pausas en las paradas. Filtra una ruta y usa
   los botones de acercar, alejar y ajustar.
2. Rutas y paradas: crea una ruta de prueba; agrega dos paradas con coordenadas
   distintas, edita una y consulta su detalle. Para ver ETA necesita una unidad
   activa asignada. El ETA muestra minutos, metros y estado de cercanía.
3. Flota: crea un bus de prueba con capacidad positiva, posición inicial y
   ruta. Edita el registro. Cambia el bus a
   Mantenimiento y verifica que deje de circular. Devuélvelo a Activo.
4. Perfiles: crea un perfil, abre su detalle, marca una ruta y una parada como
   favoritas, guarda el tema oscuro y recarga. Comprueba que las preferencias
   permanezcan guardadas. La selección de perfiles es de demostración, sin login.
5. Reportes: deja circular los buses hasta que alcancen paradas; consulta el
   intervalo de hoy. Deben aparecer llegadas y el gráfico de retraso. Sin llegadas
   se muestra un mensaje. Un retraso cero es válido en la simulación.
6. Alertas: abre el panel. Puede estar vacío mientras los buses cumplen su ruta;
   las reglas de desvío y detención se comprueban también con pruebas automáticas.
7. Borra los registros de prueba: primero bus y luego ruta. Una ruta
   con buses asignados debe rechazar el borrado con un mensaje comprensible.
8. Revisa las mismas pantallas en una ventana estrecha y prueba abrir el menú.

Las coordenadas son ficticias y el mapa es esquemático SVG, sin GPS ni proveedor
cartográfico externo. Las métricas no acreditan una reducción real de espera.

## Validación automática

```bash
./mvnw -o -Daether.syncContext.named.factory=rwlock-local \
  -DargLine=-javaagent:/home/martin/.m2/repository/org/mockito/mockito-core/5.17.0/mockito-core-5.17.0.jar test
```

La ruta del agente Mockito corresponde a esta computadora. Los tests usan H2
en memoria y desactivan la simulación; no modifican los datos de trabajo.

Se añadieron pruebas de integración web con servicios y plantillas reales:
listados, detalles, formularios de edición, recursos locales, consulta de rastreo,
favoritos, tema, errores de entrada y reporte con llegadas persistidas. También
se comprueban el cálculo de puntualidad, la tolerancia, los adelantos, el promedio,
las franjas de Ecuador y la ausencia de observaciones.

Limitaciones: el entorno de ejecución de Codex impide crear sockets (`Operation
not permitted`), por lo que el arranque HTTP no pudo completarse aquí. Las
plantillas sí se renderizaron mediante MockMvc. Falta ejecutar el recorrido
manual en el navegador para validar interacción JavaScript y presentación visual.
La ejecución disponible utiliza Java 21 con destino de compilación Java 17;
no se ha verificado la ejecución con un JDK 17 instalado.

Resultado del 2026-11-17: **28 pruebas, 0 fallos, 0 errores, BUILD SUCCESS**.
Comprobaciones de sintaxis: `node --check src/main/resources/static/js/app.js`
y `bash -n iniciar.sh`, ambas correctas.

Actualización del 2026-11-17: se elimina la gestión de conductores y su asignación
a unidades por solicitud del usuario. Resultado tras el cambio: **27 pruebas,
0 fallos, 0 errores, BUILD SUCCESS**. Se retiró una prueba exclusiva de conductores.
Las estructuras históricas de conductores en H2 no se borran automáticamente.

## Avance 2 — Tres campus y tres buses

El escenario inicial usa una ruta circular en este orden: **Jipijapa → UDLA
Granados → UDLA Park → Jipijapa**. Se cargan UDLA-01, UDLA-02 y UDLA-03, cada uno
inicialmente en un campus. La flota admite un máximo de tres unidades; se puede
eliminar una para registrar un reemplazo.

En la vista general y en el detalle de ruta, cada bus muestra automáticamente
«Llegó a [campus]» durante su pausa de 12 segundos y «En camino a [campus]»
al continuar. Se indican el siguiente campus, los minutos aproximados y los
metros restantes. El ETA incluye la pausa restante y usa la velocidad de
referencia indicada cuando está detenido. La simulación registra las llegadas
para los reportes; las unidades no activas no anuncian un ETA.

Para comprobarlo, reinicia con `./iniciar.sh`, abre la vista general y espera un
recorrido. Las tarjetas se actualizan automáticamente. Las coordenadas siguen
siendo ficticias y el ETA no considera tráfico real.

Al reiniciar, el escenario anterior de dos rutas y cuatro buses se actualiza
si conserva los códigos, nombres de paradas y placas originales. Se conservan
los perfiles, la ruta principal y los favoritos que siguen siendo válidos; se
retiran la segunda ruta, sus paradas y la cuarta unidad, junto con su historial
de posiciones. Las llegadas históricas de reportes se conservan. Los recorridos
personalizados no se reemplazan automáticamente.

Verificación de este ajuste: **34 pruebas aprobadas, 0 fallos, 0 errores**.
Se incluyen actualización del escenario anterior, reinicio sin duplicados,
respeto de rutas personalizadas, límite de tres buses y estados del viaje.

## Corrección de arranque con base de fase 1

Si la base venía de la primera versión, Hibernate no podía agregar columnas
obligatorias sobre buses existentes y fallaba con `Column B1_0.CAPACITY not found`.
Se definen valores por defecto de cero para agregar `capacity` y `next_stop_index`
sin borrar la base. El escenario original reconocido (R-01/R-02, BUS-001/BUS-002)
se transforma al circuito de tres campus y buses al reiniciar; las unidades de
esa demostración reciben capacidad 40.

Se incorpora una prueba de arranque sobre un archivo H2 con el esquema antiguo
y datos existentes. Resultado: **35 pruebas aprobadas, 0 fallos, 0 errores**.

## Panel del conductor — Registrar llegadas

En el menú aparece **Registrar llegadas** (`/arrivals`). Se selecciona el bus
mediante su tarjeta; no se reintroduce el CRUD de conductores ni autenticación.

1. Pulsa **Activar control manual** en la tarjeta del bus activo. La simulación
   deja de mover únicamente esa unidad y el modo se conserva al reiniciar.
2. Al llegar al campus indicado pulsa **Llegué a [campus]** y confirma. Se guarda
   la hora del servidor, se registra la posición del campus declarado y se
   prepara el siguiente tramo del recorrido circular con su ETA.
3. Consulta **Últimas confirmaciones**: aparecen hasta 30 llegadas manuales con
   bus, campus y fecha/hora de Ecuador. Estas observaciones también alimentan
   los reportes, que indican que incluyen llegadas simuladas y declaradas.
4. Para volver a la demostración pulsa **Volver a simulación automática**. Si la
   simulación global está desactivada, seguirá pausada hasta habilitarla.

Solo se admite la siguiente parada de la ruta y una unidad activa. Un formulario
antiguo que intente confirmar la parada anterior se rechaza. El simulador y el
registro manual bloquean la unidad durante cada actualización para impedir que
ambos registren la misma transición simultáneamente.

Las llegadas son declaraciones, sin verificación GPS ni identificación del
conductor. El historial distingue `DRIVER_CONFIRMED` de `SIMULATED`; los registros
anteriores conservan el origen simulado. Las nuevas columnas tienen valores
por defecto para actualizar bases existentes sin borrar los datos.

Verificación del panel: **37 pruebas aprobadas, 0 fallos, 0 errores**. Incluye el
flujo web real, historial no vacío, rechazo de confirmaciones inválidas, cambio
de modo y actualización de H2 con observaciones antiguas. JavaScript verificado
con `node --check`. Sigue pendiente la revisión visual manual en navegador.

Actualización de permisos: ver docs/PERMISOS.md. La validación final de
seguridad fue correcta: 43 pruebas aprobadas. El usuario ya autorizó publicar;
faltan las conexiones con GitHub/Render para completar el despliegue.
