# Auditoría del proyecto frente a la rúbrica final

## Resultado de la revisión

El prototipo cubre los criterios técnicos de la rúbrica: Java, conexión MySQL, MVC, DAO,
capas, seguridad por rol, casos de uso, persistencia, trazabilidad, accesibilidad y pruebas.
El informe y la PPT siguen siendo los archivos que acreditan los modelos y diagramas UML;
el código no los sustituye.

| Criterio de la rúbrica | Evidencia del entregable | Estado |
|---|---|---|
| Empresa: objetivos, misión, visión, rubro, RR. HH., hardware, software y cronograma | Informe final, secciones iniciales y ámbito | Completo en informe |
| Modelo de negocio y modelo de análisis | Informe final: objetivos, actores, CUN, trabajadores, entidades y realizaciones | Completo en informe |
| Requerimientos y todos los casos de uso | Informe final + `MATRIZ_CASOS_USO_IMPLEMENTACION.md` | Completo y trazable |
| Arquitectura, realizaciones, secuencia y comunicación | Diagramas del informe final | Completo en informe |
| Modelo conceptual, lógico y físico | Diagramas del informe; entidades JPA y MySQL en el prototipo | Completo y aplicado |
| MVC, DAO y capas, conectados a BD y lenguaje | Controladores, servicios, repositorios, modelos, Thymeleaf, Java 21 y MySQL | Completo |
| Aplicativo Java | `pom.xml`, código fuente y JAR ejecutable | Completo |
| Pantallas, implementación y pruebas | Interfaces por rol, README y 12 pruebas de integración | Completo |
| Accesibilidad y facilidad de uso | Navegación por teclado, salto al contenido, foco visible, etiquetas, mensajes, diseño adaptable y reducción de movimiento | Completo en prototipo |
| RSU: empresa real y alcance nacional | Proyecto orientado a SEDAPAL y usuarios de Lima/Callao | Sustentado en informe |
| Evidencia de entrega y recomendaciones de la empresa | Deben adjuntarse constancias reales | Pendiente de evidencia externa real |
| Sustentación máxima de 10 minutos y participación total | PPT y guía de demostración para dos integrantes | Preparado; depende de la grabación |

## Hallazgos corregidos en el código recibido

- Se actualizó el proyecto a Java 21 y se incorporó MySQL como base principal.
- Se sustituyeron contraseñas en texto simple y sesiones manuales por Spring Security y
  BCrypt.
- Se implementaron siete roles y autorización independiente por función.
- Se separaron clasificación, generación de orden, asignación, inicio, atención y cierre.
- Se añadieron historial persistente, notificaciones, evidencias, auditoría y reportes CSV.
- Se añadieron validaciones de servidor, CSRF y control de pertenencia del suministro.
- Se agregó un diseño adaptable y accesible sin depender de recursos CDN.
- Se incorporaron pruebas de integración de lógica, seguridad y renderizado de vistas.

## Único requisito que no puede generarse desde el software

La constancia de entrega a la empresa y sus recomendaciones deben ser auténticas. Antes de
subir el informe, reemplace cualquier texto pendiente con correo, acta, captura o constancia
real, protegiendo datos personales que no sean necesarios. No se debe inventar una firma,
un contacto ni una respuesta de SEDAPAL.
