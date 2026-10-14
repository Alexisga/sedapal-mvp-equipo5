# Entrega para aprobación

Comience por RESUMEN_EJECUTIVO.md y REPORTE_QA.md (el Word es una contraparte resumida del informe). Los CSV registran estado por cada RF, HU, CA, RNF y DoD. Ninguna HU está Done. Evidencia actual en EVIDENCIAS; intento fallido histórico en INTENTO_01.

Código probado `55c805e352fb20afe064187a79341dcf8abb3fb8`, 0.1.1-SNAPSHOT. Recuperar con `git clone HISTORIAL_GIT.bundle sedapal-revision`. Compilar con Java 21 y Maven: `mvn -DforkCount=0 clean verify`. No copiar bases ni adjuntos de trabajo. Para demostración local: `java -jar target/sedapal-reclamos-0.1.1-SNAPSHOT.jar --spring.profiles.active=demo --server.address=127.0.0.1 --server.port=0 --spring.h2.console.enabled=false`; el log muestra el puerto libre asignado. Usar solo datos ficticios.

qa_navegador.py requiere Playwright 1.64.0 y reportlab en Python, Chrome/Edge instalados y la ruta local del JAR original configurable en el script. La ejecución inicia y detiene únicamente sus propios procesos locales; escribe evidencias. No modifica el Word ni Excel oficiales. Fuentes oficiales identificadas por SHA-256, sin copias sobrescritas.
