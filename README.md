# SEDAPAL MVP · Equipo 5

Prototipo académico de Desarrollo de Software I. Java 21, Spring Boot, Thymeleaf y Spring Security; versión 0.1.2-SNAPSHOT. No es un servicio oficial de SEDAPAL. Usar exclusivamente datos simulados.

El historial conserva la importación antecedente y la mejora HU-02. `docs/auditoria-2026-10-14` y `docs/cierre-2026-10-14` distinguen evidencias anteriores, cambios nuevos y pendientes. Ninguna HU está declarada Done; IP05 medido es 3/6=50%, inferior a la meta del 90%.

## Ejecución local

Definir `DEMO_PASSWORD` por entorno (mínimo ocho caracteres, única para la demostración), compilar con `mvn clean verify` y ejecutar `java -jar target/sedapal-reclamos-0.1.2-SNAPSHOT.jar --spring.profiles.active=demo --server.address=127.0.0.1 --spring.h2.console.enabled=false`. No se carga `.env` automáticamente desde Java. El modo MySQL exige DB_URL, DB_USER, DB_PASSWORD y esquema previamente preparado; Docker Compose exige credenciales por entorno.

## Demostración con HTTPS

`Dockerfile` y `render.yaml` preparan Render Free, Java 21, usuario sin privilegios, datos H2 en memoria, adjuntos efímeros y perfil `cloud-demo`. Contraseña generada por el proveedor, ausente del repositorio. Cookies Secure/HttpOnly/SameSite=Lax y HTTPS obligatorio detrás del proxy. Consola H2 deshabilitada. No configurar base externa ni datos reales. Los datos se reinician tras reinicio o suspensión; servicio sin garantía de disponibilidad ni persistencia. Despliegue manual y sujeto a aprobación expresa.

## Configuración y procedencia

No publicar target, bases, adjuntos, .env, claves privadas ni credenciales. Historial antecedente contiene valores de ejemplo y claves sintéticas de pruebas antiguas, sin validez en el despliegue; se conservan para preservar trazabilidad, nunca deben reutilizarse. Los README antiguos son documentos históricos y sus instrucciones de versión anterior no sustituyen este README. Las credenciales de ejecución actual se suministran únicamente por entorno.

La documentación oficial Word/Excel se conserva en Drive; propuestas de actualización están separadas. Flujo de CI manual/PR con permisos de lectura. No se agregan colaboradores ni se comparte públicamente documentación.

## Publicación verificada · 14/10/2026

Repositorio privado: https://github.com/Alexisga/sedapal-mvp-equipo5. Documentación restringida: https://drive.google.com/drive/folders/1T5AY5WP8QlTUfERQerTPwIMU8C7WG0Tg. Demo pública autorizada en Render Free: https://sedapal-mvp-equipo5.onrender.com.

Código desplegado ea97f87900262905b1bb59da6df947205e21769b. Regresión Java local 51 PASS; sobre HTTPS público, 7 checks TLS/HTTP y 131 de navegador PASS. Evidencias y límites en docs/publicacion-2026-10-14. Auto Sync del Blueprint desactivado; próximos despliegues manuales. Credencial demo únicamente en el entorno privado Render. IP05 y validaciones humanas continúan pendientes; ninguna HU Done.
