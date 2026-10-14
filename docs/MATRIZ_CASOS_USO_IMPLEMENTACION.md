# Matriz de trazabilidad de casos de uso

Esta matriz vincula los 12 casos de uso especificados en el informe final con una interfaz,
una regla de aplicación y datos persistentes. Así se evita presentar pantallas sin lógica.

| CUS | Caso de uso | Actor principal | Interfaz/ruta | Implementación y evidencia persistente |
|---:|---|---|---|---|
| 01 | Iniciar sesión | Todos | `/login`, `/panel` | `AuthService`, `SecurityConfig`, BCrypt y redirección por rol |
| 02 | Registrar reclamo | Usuario/Operador | `/usuario/reclamos/nuevo`, `/operador/reclamos/nuevo` | Crea `Reclamo`, `Incidencia`, código único, historial y notificación |
| 03 | Registrar datos del reclamo | Usuario/Operador | Formularios de registro | Valida suministro, usuario, tipo, prioridad y descripción; impide usar un suministro ajeno |
| 04 | Clasificar reclamo | Evaluador | `/evaluador` | Actualiza tipo/prioridad, determina visita, registra decisión e informa al usuario |
| 05 | Generar orden de trabajo | Supervisor | `/supervisor` | Crea `OrdenTrabajo` separada, evita órdenes activas duplicadas |
| 06 | Asignar técnico | Supervisor | `/supervisor` | Crea `AsignacionTecnica`, verifica disponibilidad y cambia a `Asignado` |
| 07 | Registrar atención técnica | Técnico | `/tecnico` | Inicio controlado, acciones, resultado y estado `Atendido` |
| 08 | Adjuntar evidencia | Técnico | Formulario multipart de `/tecnico` | Guarda JPG/PNG/PDF hasta 5 MB como `Evidencia`, con nombre seguro y metadatos |
| 09 | Actualizar estado e historial | Sistema/Roles | Todos los flujos | `HistorialEstado` registra estado, fecha, responsable y observación en cada transición |
| 10 | Consultar estado | Usuario | `/usuario/consultar` | Restringe la consulta al propietario y muestra línea de tiempo, historial y órdenes |
| 11 | Notificar avance | Sistema | Portal del usuario | `Notificacion` se genera al registrar, clasificar, asignar, iniciar, atender y cerrar |
| 12 | Generar reportes | Calidad | `/calidad/reportes` y `.csv` | Filtros por zona/estado/prioridad/fecha, indicadores y exportación CSV UTF-8 |

## Roles implementados

`USUARIO`, `OPERADOR`, `EVALUADOR`, `SUPERVISOR`, `TECNICO`, `CALIDAD` y
`ADMINISTRADOR`. Cada prefijo de ruta está protegido en `SecurityConfig` y el acceso
cruzado devuelve HTTP 403.

## Transiciones controladas

| Origen | Acción autorizada | Destino |
|---|---|---|
| Registrado | Clasificar con visita | Clasificado |
| Registrado | Resolver mediante orientación | Atendido |
| Clasificado | Generar orden | Clasificado; orden pendiente |
| Clasificado | Asignar técnico | Asignado |
| Asignado | Iniciar visita | En atención |
| En atención | Registrar resultado | Atendido |
| Atendido | Comunicar y cerrar | Cerrado |

Los servicios rechazan operaciones fuera del estado esperado y registran auditoría de
responsable, acción, entidad, fecha y detalle.
