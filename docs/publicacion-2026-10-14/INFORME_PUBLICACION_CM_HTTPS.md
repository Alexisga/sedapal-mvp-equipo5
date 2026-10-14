# Publicación CM y HTTPS SEDAPAL · 14/10/2026

Se ejecutaron los tres resultados autorizados: repositorio GitHub privado, documentación Drive restringida y prototipo público HTTPS en Render Free. No se modificaron Word ni Excel oficiales, no se añadieron colaboradores y no se activaron planes de pago.

| Resultado | URL real | Verificación |
|---|---|---|
| GitHub privado | https://github.com/Alexisga/sedapal-mvp-equipo5 | API private=true, default_branch=main; push real, clon remoto y comparación de SHA. Historial antecedente y HU-02 conservados. |
| Google Drive restringido | https://drive.google.com/drive/folders/1T5AY5WP8QlTUfERQerTPwIMU8C7WG0Tg | Ocho áreas organizadas; archivos descargados y SHA-256 cotejado. Solo propietario, sin enlaces públicos ni invitados. Índice publicado con URLs e integridad. |
| HTTPS público | https://sedapal-mvp-equipo5.onrender.com | Render Live; certificado y hostname válidos, TLS 1.3, HTTP→HTTPS, HSTS y cookies protegidas. 7/7 verificaciones TLS/HTTP y 131/131 checks de navegador PASS. |

Código desplegado y verificado: `ea97f87900262905b1bb59da6df947205e21769b`, v0.1.2-SNAPSHOT. Java remoto 21.0.12.1, Docker sin privilegios, perfil cloud-demo. La rama principal puede avanzar con documentación y verificadores; el commit de despliegue queda identificado por separado. Etiqueta `v0.1.2-cm-20261014` identifica el incremento de publicación. Etiqueta original `mvp-v0.1.0-dev001` conservada.

Compilación y regresión local del código publicado: **51 pruebas Java PASS**, ninguna omitida. Se agregaron dos pruebas para anonimización de la demo y obligación de HTTPS. Los reportes JUnit y logs reales se adjuntan. Docker construyó la imagen en Render; el build remoto omite tests y la regresión se acredita con el registro local, no con una CI inexistente. Workflow GitHub preparado, no ejecutado durante esta publicación.

Sobre la URL pública se ejecutaron 39 scans axe, tres anchos (360/768/1366) y 40 capturas Chrome: autenticación, registro, consulta privada/pública, notificaciones, evidencia PDF y permisos, cierre, reapertura, teclado, CSRF y cierre de sesión. Cero violaciones automáticas detectadas en dichos scans no equivalen a certificación WCAG ni revisión humana. La matriz anterior de cuatro motores se conserva como antecedente separado.

La contraseña de demo fue generada por Render y solo se usó temporalmente para QA; está excluida de Git y de la entrega. Se retiraron los valores predeterminados de contraseñas de Docker. El historial conserva ejemplos sintéticos de pruebas antiguas, sin validez en el despliegue y sin reescritura de commits. La inspección automatizada de firmas de secretos tiene límites documentados; no sustituye una auditoría de seguridad exhaustiva.

Render Free conserva la demo de forma efímera: suspensión por inactividad, arranque lento y pérdida de H2/adjuntos al reiniciar. Se verificó un arranque inicial de aproximadamente 129 segundos; no se garantiza una latencia de reactivación. No se añadió almacenamiento persistente, tarjeta ni servicio de pago. Auto Deploy está desactivado por configuración; Auto Sync del Blueprint se dejó en No (Sync paused), con cambios futuros manuales. Límite de gratuidad y recursos según https://render.com/docs/free; suspender el servicio si el titular deja de necesitarlo.

Los documentos anteriores que dicen «publicación pendiente» se conservan como registros históricos de su corte. Este informe y los registros nuevos actualizan el estado; no se editan fuentes oficiales para simular una aprobación.

| Estado final | Actividad | Necesidad y responsable |
|---|---|---|
| VERIFICADO | GitHub privado, Drive restringido y HTTPS público | Evidencias reales, SHA-256 y referencias remotas adjuntas. |
| PENDIENTE DE PERSONA | Usabilidad con cinco participantes, revisión PPQA independiente, accesibilidad humana y conformidad documental | Equipo 5 designa participantes/moderador y revisor distinto del autor; responsable documental integra propuestas a Word/Excel tras revisión. |
| PENDIENTE DE PERSONA | IP05 inferior al objetivo | Última cohorte formal 3/6=50%; operador y PO resuelven expedientes faltantes o autorizan reglas nuevas. Las pruebas públicas no reemplazan esa medición ni elevan la cifra. |
| PENDIENTE DE ENTORNO/PERSONA | Safari y dispositivos físicos | Equipo facilita dispositivos y persona ejecuta los formularios existentes. |

Ninguna HU se declara Done. Cerrar CM de publicación y la verificación técnica de HTTPS no constituye aceptación independiente PPQA ni cierra toda la DoD.
