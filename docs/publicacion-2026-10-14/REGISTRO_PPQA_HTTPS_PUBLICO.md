# Seguimiento PPQA del despliegue público

Responsable de ejecución: Codex, agente automatizado. Fecha real y commit por caso en HTTPS_PUBLICO/TLS_HTTP_RESULTADOS.json y HTTPS_PUBLICO/NAVEGADOR_CHROME/checks.json. Esperados, obtenidos, capturas e índices adjuntos. No se atribuye revisión independiente al autor del código.

Resultado: TLS/HTTP 7/7 PASS; navegador 131/131 PASS; 39 scans axe sin violaciones detectadas, 40 capturas; regresión Java 51/51 PASS. Se validó cadena de confianza y hostname con configuración predeterminada, redirección HTTP al mismo HTTPS, HSTS, cookie anónima y autenticada Secure/HttpOnly/SameSite=Lax, autenticación, autorización de titular/evidencia y denegación de ajenos, CSRF, operaciones principales y logout. H2 devuelve 403; .env y actuator/env devuelven 404. No se entrega valor de cookie ni secreto.

Correcciones específicas: quitar contraseñas por defecto de Docker; cuenta/dirección inicial genéricas en perfil público; aviso de datos simulados; HTTPS obligatorio y almacenamiento efímero en cloud-demo. Pruebas unitarias/integración nuevas PublicDemoConfigTest (2 PASS). No se cambió alcance funcional aprobado ni se modificaron RF/HU/RNF/DoD.

Estado de no conformidades relacionadas: preparación/publicación CM y comprobación técnica de HTTPS público resueltas mediante evidencia nueva. Usabilidad RNF03, revisión humana RNF09, PPQA independiente, actualización oficial y aceptación formal siguen pendientes. IP05 no se considera cumplido. Safari físico no se sustituye por WebKit/Chrome. La gratuidad impone arranques lentos y no persistencia; registrar estos límites al evaluar RNF04, sin afirmar disponibilidad garantizada.

Los resultados anteriores permanecen inalterados en la entrega de cierre técnico. Para revisión independiente, usar el checklist vacío y las cinco fichas ya publicados en 03_PPQA_Calidad, sin participantes ni firmas inventados.
