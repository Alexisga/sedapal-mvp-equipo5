# Registro de medición y análisis

Corte 2026-10-14T13:47:57.813583-05:00. Fuente de medición: JUnit XML, JSON Playwright y rendimiento-20.json. Responsable de ejecución: Codex, agente automatizado. Revisión humana pendiente.

- Línea base 41070eb: 12 tests JUnit actuales; no se reutilizan reportes antiguos.
- Incremento 55c805e352fb20afe064187a79341dcf8abb3fb8: 46 tests JUnit, 46 PASS, sin omitidos.
- Humo del JAR original: 17 checks actuales; ocho HU-02 y nueve HTTP reconstruidos a partir del alcance del informe (no se dispone del script HTTP original).
- Navegador del incremento: 41 checks; 41 PASS. No sumar checks solapados como historias.
- Rendimiento: 20/20 <=3 s; p95 0.170 s, rango 0.098–0.175 s. Método nearest-rank, 19.ª muestra ordenada. Chrome headless, navegación DOM seguimiento propio, H2 en memoria 4 incidencias, un usuario secuencial, 20 consultas, equipo local compartido.
- Usabilidad humana: NO EJECUTADA; sin promedio válido. HC según DoD: 0 HU declaradas Done. Productividad, esfuerzo real del equipo y cumplimiento de Sprint: no calculables con las fuentes disponibles.

No se extrapolan las mediciones a producción ni se afirma independencia entre todos los checks.
