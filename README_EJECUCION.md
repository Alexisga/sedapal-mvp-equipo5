# Sistema de gestión y seguimiento de reclamos técnicos de SEDAPAL

Prototipo académico desarrollado con Java 21, Spring Boot 3.3, Thymeleaf, Spring
Security, JPA/DAO y MySQL. Implementa el recorrido documentado desde el registro del
reclamo hasta su cierre, con trazabilidad, notificaciones, evidencia técnica y reportes.

## Requisitos

- JDK 21.
- Maven 3.9 o Maven incluido en NetBeans 22.
- Para el modo principal: MySQL 8.0 o Docker Desktop.
- Navegador actualizado.

## Inicio rápido para la exposición (H2)

En Windows, haga doble clic en `INICIAR_SEDAPAL.bat`. El script compila, ejecuta las
pruebas e inicia el perfil de demostración. También puede usar:

```powershell
mvn clean verify
java -jar target/sedapal-reclamos-0.1.2-SNAPSHOT.jar --spring.profiles.active=demo
```

Abra `http://localhost:8080/login`. H2 se usa solo para la demostración local y conserva
los datos en `data/sedapal-demo`.

## Ejecución principal con MySQL

1. Copie `.env.example` como `.env` si desea cambiar las credenciales.
2. Inicie MySQL: `docker compose up -d`.
3. Compile y pruebe: `mvn clean verify`.
4. Ejecute: `java -jar target/sedapal-reclamos-0.1.2-SNAPSHOT.jar`.
5. Abra `http://localhost:8080/login`.

Sin Docker, cree la base `bd_reclamos_sedapal` y configure las variables `DB_URL`,
`DB_USER` y `DB_PASSWORD`. Hibernate crea o actualiza las tablas a partir del modelo
físico JPA.

## Usuarios de demostración

La contraseña se define mediante DEMO_PASSWORD; no existe una contraseña común predeterminada.

| Rol | Documento | Función demostrable |
|---|---:|---|
| Usuario | 12345678 | Registrar y consultar sus reclamos |
| Usuario | 87654321 | Segundo usuario para validar privacidad |
| Operador | 22222222 | Registrar por atención y cerrar reclamos |
| Evaluador | 44444444 | Clasificar y determinar atención en campo |
| Supervisor | 55555555 | Generar orden y asignar técnico |
| Técnico | 33333333 | Iniciar y registrar atención/evidencia |
| Calidad | 66666666 | Filtrar indicadores y exportar CSV |
| Administrador | 99999999 | Gestionar usuarios y revisar auditoría |

## Estados oficiales

`Registrado → Clasificado → Asignado → En atención → Atendido → Cerrado`.

Una clasificación que no necesita visita pasa de `Registrado` a `Atendido`, quedando la
decisión explicada en el historial. El operador luego comunica el resultado y cierra el
reclamo.

## Arquitectura

- Presentación: controladores MVC, Thymeleaf, Bootstrap local y validación accesible.
- Aplicación: servicios transaccionales y reglas de cada caso de uso.
- Persistencia/DAO: repositorios Spring Data JPA.
- Dominio: entidades de reclamo, suministro, incidencia, orden, asignación, atención,
  evidencia, historial, notificación y auditoría.
- Seguridad: autenticación BCrypt, autorización por siete roles y protección CSRF.

## Verificación

```powershell
mvn clean verify
```

Se ejecutan 12 pruebas de integración. La matriz completa está en
`docs/MATRIZ_CASOS_USO_IMPLEMENTACION.md` y los resultados en
`docs/RESULTADOS_PRUEBAS.md`.

## Datos que no deben falsificarse

La evidencia de entrega a SEDAPAL y sus recomendaciones debe provenir de una interacción
real. El proyecto deja una guía en `docs/evidencia-empresa/LEEME.md`, pero no inventa
constancias, nombres, firmas ni capturas institucionales.
