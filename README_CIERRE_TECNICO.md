# Incremento de cierre técnico SEDAPAL

Base v0.1.1: 70b4b77, código probado previamente 55c805e. Incremento de cierre: v0.1.2-SNAPSHOT. Las 46 pruebas previas permanecen como antecedentes; este cierre ejecuta una batería focalizada nueva y una matriz nueva de navegadores.

Java 21 y Maven: `mvn -Dtest=CompletitudIP05IntegrationTest,CierreAccesibilidadSeguridadTest -DforkCount=0 package`.

El perfil demo necesita definir DEMO_PASSWORD por entorno con al menos ocho caracteres. No hay contraseña demo visible ni contraseña de BD predeterminada. El perfil normal requiere DB_URL, DB_USER y DB_PASSWORD; no se conecta a sistemas oficiales. Para demo local usar `--spring.profiles.active=demo --server.address=127.0.0.1 --server.port=0 --spring.h2.console.enabled=false`. El puerto real se informa en el log.

El perfil optativo tls-local usa un almacén PKCS12 externo, TLS_KEYSTORE y TLS_KEYSTORE_PASSWORD. tools/qa/cierre_tls.py crea clave efímera únicamente en target, prueba TLS en loopback y entrega solo el certificado público. No representa despliegue público.

tools/qa/cierre_browser.py usa axe-core y Playwright con Chrome/Edge instalados y Firefox/WebKit descargados en target/qa-browsers. Consultar requirements.txt. Las pruebas de WebKit no son pruebas de Safari físico. Datos sintéticos, sin participantes humanos ni aprobaciones ficticias.

La medición IP05 fija una cohorte mixta antes de ejecutar y conserva TODOS sus casos elegibles, incluso sin evidencia o con adjuntos pendientes de publicación. Su resultado no se extrapola a producción ni se cambia generando casos satisfactorios adicionales.

CI para GitHub está preparada, sin ejecutar ni publicar. Destino, autorización y revisión de permisos pendientes. No subir target, bases, adjuntos privados, .env, claves TLS ni la entrega completa como repositorio público. El historial contiene datos y contraseñas históricas de prueba, identificados como sintéticos; no contiene autorización para publicar información real.

Referencias técnicas: https://playwright.dev/docs/accessibility-testing, https://www.deque.com/axe/core-documentation/api-documentation/, https://github.com/actions/checkout, https://github.com/actions/setup-java.
