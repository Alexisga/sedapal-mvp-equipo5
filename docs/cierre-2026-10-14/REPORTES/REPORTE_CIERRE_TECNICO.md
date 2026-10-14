# Verificaciones técnicas de cierre y límites

2026-10-14T17:21:59.168434-05:00. Código 4aad20366da34c617148a0b56ad7c5c575e30c04. Responsable de ejecución Codex agente automatizado; revisión independiente no afirmada.

La línea base previa estaba controlada en 70b4b77 (contenido probado 55c805e). Se usó una nueva rama cierre-tecnico/2026-10-14. La batería focalizada tiene tres pruebas Java, incluida una medición IP05 que conserva los casos incompletos; los 46 Java previos no se volvieron a ejecutar. Los resultados anteriores permanecen identificados por versión. Git diff distingue los cambios nuevos.

La inspección inicial de v0.1.1 produjo cinco checks fallidos, por contraste de 4.21/4.29:1 y error de contraseña sin relación ARIA. Se corrigieron enlaces/avisos y errores de cliente/servidor con foco. La primera matriz amplió la evidencia y detectó overflow 5px en Firefox. Se corrigió la cabecera; además se ajustaron selectores ambiguos y espera del POST de upload del verificador. WebKit no requería relajar MIME ni validación de contenido; el diagnóstico conservado completó su flujo tras corregir la espera. La primera prueba TLS usó un token antiguo tras relogin; el producto lo denegó con 403. Se corrigió el verificador para obtener un token vigente; los intentos previos se conservan.

La matriz definitiva contiene 156 estados/pantalla/viewport, tres checks por scan (axe WCAG A/AA, overflow y etiquetas) y controles funcionales y de seguridad: 524 PASS. Los motores/versiones y cada scan raw están en 03_MATRIZ_CONSOLIDADA; se consolida la última ejecución completa por motor, conservando intentos anteriores. Chrome y Edge son instalaciones locales; Firefox y WebKit provienen de Playwright. No se llaman Safari físico ni dispositivos reales. Las animaciones se esperan antes de capturar.

RNF-09 no se certifica íntegramente con axe: 108 entradas incomplete conservadas para revisión humana, además de semántica y ayudas técnicas. No hay participantes ni firmas. Permisos, titularidad, CSRF, adjunto restringido/publicado, ajeno y logout fueron comprobados por motor. La prueba Java verificó hash BCrypt y que la contraseña demo anterior ya no coincide. Las credenciales de ejecución se suministraron por entorno y no están en la entrega.

TLS: certificado efímero SAN localhost/127.0.0.1, confianza explícita y rechazo de confianza predeterminada, autenticación, cookie Secure/HttpOnly/SameSite=Lax, HSTS y logout. Solo loopback; no se alteró confianza del sistema. Se entrega únicamente el certificado público; claves privadas, bases H2, target y .env quedan excluidos. No se probaron DNS, proxy ni HTTPS de un despliegue público.

IP05 y su límite: MEDICIONES/IP05_CASOS.csv y JSON incluyen estado, fecha, archivos, hashes y HTTP por rol. La cohorte mezcla casos positivos y negativos prefijados; todos los elegibles entran en denominador. El valor 50.00% NO satisface meta. No se cambia una regla aprobada de atención ni se inventa un documento para elevarlo. Operación/PO debe completar/revisar evidencias y decidir si hace falta un cambio autorizado de flujo.

Cada HU tiene nueve condiciones reevaluadas en HU_DOD_72_CONDICIONES.csv, con CA, RF, RNF, pruebas, evidencias, NC y resultado NO DONE. La aprobación con observaciones del incremento anterior no se extrapola al nuevo. La matriz oficial v1.12 permanece sin cambios; las propuestas están separadas. No se inventan Sprints, horas, presupuestos, reuniones, HC ni proveedores.

Referencias primarias de metodología: [Playwright](https://playwright.dev/docs/accessibility-testing), [API axe-core](https://www.deque.com/axe/core-documentation/api-documentation/). Son referencias de herramientas, no evidencia de aprobación humana.
