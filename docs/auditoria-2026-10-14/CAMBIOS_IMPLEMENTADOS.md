# Cambios implementados y procedencia

Fecha de corte 2026-10-14T13:47:57.813583-05:00. Versión 0.1.1-SNAPSHOT. Commit del código probado `55c805e352fb20afe064187a79341dcf8abb3fb8`.

El bundle contiene la importación antecedente `fc52549` y la mejora HU-02 `41070eb`. Se recuperó el historial completo; no se atribuye este trabajo a Sprints 1 o 2. El POM heredado declaraba 0.0.1-SNAPSHOT pese a la denominación del paquete v0.1.0. El incremento usa 0.1.1-SNAPSHOT.

La carpeta original se contrastó contra el árbol de 41070eb; resultados en COMPARACION_CARPETA_ORIGINAL_BUNDLE.csv. Los documentos oficiales se leyeron sin escritura; sus SHA-256 están en FUENTES_Y_SHA256.csv. El ZIP de código mencionado no está en Descargas; se utilizó la carpeta extraída y el bundle disponible, sin bloquear el trabajo.

Cambios del curso ejecutados en esta auditoría: alta ciudadana y hash BCrypt; referencia local de ubicación para primera incidencia; consulta pública con campos permitidos; cierre titular y solicitud pendiente de reapertura; descargas privadas y aprobación por operador; validación de JPG/PNG/PDF y cupo; corrección HTTP 403; generación de código por identidad de BD; pruebas nuevas y registros QA. No hay integración institucional, nueva EDT aprobada ni cambios en RF/HU/RNF/DoD.

## Historial real

```text
55c805e352fb20afe064187a79341dcf8abb3fb8 | 2026-10-14T13:33:07-05:00 | Codex QA | fix(HU-02,RNF-09): progreso basado en historial real y contraste de colores
c79c2cdcbdc549223ae9a81e4ef1f2accb137236 | 2026-10-14T13:29:16-05:00 | Codex QA | fix(QA): limpiar adjuntos en rollback y cubrir PDF cifrado, avisos y campos obligatorios
63c06ecedda77b85ab10980ed6bd332825c46507 | 2026-10-14T13:21:34-05:00 | Codex QA | test(QA): limites de motivo y archivos, estado de cierre y datos obligatorios
06dbf25cf0f0fc6f0b95f2a0bef2055b321808ef | 2026-10-14T13:19:12-05:00 | Codex QA | fix(RNF-06): validar estructura PDF y registrar revision de evidencia
91511b404aebb930da3be6d61e2631997ae02c09 | 2026-10-14T13:17:28-05:00 | Codex QA | fix(seguridad): devolver 403 real para POST ajeno y controlar exceso multipart
24f521a8ec34aecbac827d298a13bce48ec951e1 | 2026-10-14T13:14:36-05:00 | Codex QA | feat(HU-03,05,06,07): evidencias privadas, cierre titular y solicitud de reapertura con pruebas
c95de8defa62d751c7995426b4817760613cf929 | 2026-10-14T13:14:36-05:00 | Codex QA | feat(HU-08,HU-02): cuentas ciudadanas y consulta publica sin datos privados
41070ebc56ae97de6666268e27308a0ca7ad7307 | 2026-10-14T16:51:26Z | Asistente IA (desarrollo asistido) | feat(HU-02): destacar estado y fecha de última actualización con prueba local
fc5254916b516c38a8410ef8f9bf1f2a67166fc0 | 2026-10-14T16:45:10Z | Asistente IA (importación técnica) | chore: importar ZIP antecedente sin atribuir desarrollo nuevo
```

El bundle entregado permite recuperar el repositorio. Las pruebas referencian el commit del código ejecutado; un commit posterior de empaquetado solo agrega herramientas y documentación.
