# Primer avance técnico verificable del MVP (14/10/2026)

**Contexto y procedencia**
- Fuente: `SEDAPAL_Reclamos_Proyecto_Final_Completo.zip`, aplicación académica de un proyecto anterior.
- Primer commit de este repositorio local: **importación del antecedente**, no desarrollo efectuado durante los Sprints 1 y 2 del curso actual.
- Mejoras nuevas fechadas el 14/10/2026. No reescribir las fechas históricas de GantPro.

## Mejora HU-02 (RF-03/RF-04)
La vista ciudadana `/usuario/consultar` ahora destaca el **estado actual** y la **última actualización registrada** en dos bloques responsivos. La fecha se calcula a partir del último evento del historial ordenado por fecha; si no hay eventos, se usa la fecha de registro.

## Alcance de la verificación
- Un resultado de pantalla correcto **no equivale** a cumplir todos los criterios de aceptación de HU-02 ni la DoD.
- Se deben comprobar identidad/titularidad, pruebas del historial, compatibilidad y responsividad, documentar evidencias y revisión PPQA.
- El histórico de pruebas dentro de `target/surefire-reports/` es del ZIP anterior, no del presente cambio.

## Pendiente
- Ejecutada prueba de humo local el 14/10/2026 (8/8, EV-HU02-001). Quedan pendientes pruebas completas con Maven y otros RNF.
- Revisión de QA y controles DoD-01..DoD-09.
- Vincular versión Git/commit a la Matriz Maestra.
- Definir adaptación para RF-10/HU-08 y evidencia ciudadana RF-05/RF-09.
