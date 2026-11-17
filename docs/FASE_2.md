# Fase 2 — Servicios

> Registro histórico: el 2026-11-17 se retiró la gestión de conductores por
> solicitud del usuario. Las referencias a conductores de esta fase ya no
> describen el alcance actual; las unidades se gestionan sin conductor.

Los consumidores dependen de `RouteService`, `StopService`, `BusService`,
`DriverService` y `UserService`. Spring inyecta sus implementaciones por constructor.
No se han añadido controladores ni endpoints en esta fase.

- Rutas: listar, consultar, crear, actualizar y borrar. El detalle contiene paradas ordenadas.
- Paradas: consultar por identificador o ruta, agregar al final, actualizar y borrar reordenando el recorrido.
- Unidades: listar, consultar, registrar con posición inicial, actualizar los datos de registro y borrar.
  La capacidad debe ser positiva; el conductor puede quedar sin asignar (`null`).
  Actualizar el registro de una unidad no modifica su posición.
- Conductores: listar, consultar, registrar, actualizar y borrar; licencia única.
- Usuarios: listar, consultar, registrar, actualizar y borrar; correo único normalizado
  a minúsculas y tipo `STUDENT`, `TEACHER` o `ADMINISTRATIVE`.
  Se valida el formato del correo. La restricción a dominios institucionales queda
  pendiente de definir los dominios admitidos.

Las escrituras son transaccionales. Las consultas construyen DTOs dentro de la
transacción para permitir `spring.jpa.open-in-view=false`.
`RouteDto` copia la lista de paradas para que sea inmutable.

Los errores de servicio son `DomainException` con códigos `NOT_FOUND`,
`INVALID_INPUT`, `DUPLICATE` e `IN_USE` y mensajes en español.
No se pueden borrar rutas con unidades ni conductores asignados a una unidad.
Borrar una ruta sin unidades también borra sus paradas.

Se conservan las entidades de fase 1 y sus nombres en español para evitar una
renombración ajena al alcance. El código añadido utiliza nombres en inglés.
Los buses de fase 1 tienen capacidad 0 hasta completar sus datos mediante
`BusService.update`; los registros nuevos deben indicar capacidad positiva.
Los favoritos, estados operativos, alertas, historial de posiciones y ETA pertenecen
a las fases siguientes. No hay integración con GPS real.

## Verificación

Ejecutar `./mvnw test`. Hay pruebas unitarias con Mockito para los cinco servicios
y pruebas `@DataJpaTest` que verifican escrituras y borrados reales, referencias,
normalización de correo y orden de paradas. El test de contexto utiliza H2 en memoria.

En un entorno restringido que impida crear bloqueos en la caché Maven y adjuntar
el agente de Mockito automáticamente, se puede ejecutar con la caché ya instalada:

```bash
./mvnw -o -Daether.syncContext.named.factory=rwlock-local \
  -DargLine=-javaagent:/home/martin/.m2/repository/org/mockito/mockito-core/5.17.0/mockito-core-5.17.0.jar test
```

La ruta del agente depende de la instalación local; no es una configuración de la aplicación.

Resultado verificado al completar la fase: **14 pruebas, 0 fallos, 0 errores; BUILD SUCCESS**.
