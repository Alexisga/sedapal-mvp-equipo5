# Informe QA del incremento académico SEDAPAL

Corte 2026-10-14T13:47:57.813583-05:00. Elaborado por Codex, agente automatizado, para el responsable delegado del Equipo 5. Estado: para aprobación. Código probado `55c805e352fb20afe064187a79341dcf8abb3fb8`; versión 0.1.1-SNAPSHOT. Este informe documenta ejecución técnica y pendientes; no certifica DoD ni CMMI nivel 2.

## Resultado y alcance

Se revisaron diez RF, ocho HU, 39 CA, nueve RNF y nueve condiciones DoD del catálogo v1.4 frente a la matriz v1.8. Se implementaron brechas del alcance aprobado y se ejecutaron pruebas reales con datos ficticios. Los documentos oficiales no fueron sobrescritos. El encargo Markdown se ejecutó dentro de la solicitud del usuario; las declaraciones de aprobación documental no se interpretaron como aceptación del software.

La carpeta abierta carecía de .git. Se recuperó el bundle completo y se comprobó que contenía 41070eb y fc52549. La comparación con la carpeta original y los hashes están en [FUENTES_Y_SHA256.csv](FUENTES_Y_SHA256.csv) y [COMPARACION_CARPETA_ORIGINAL_BUNDLE.csv](COMPARACION_CARPETA_ORIGINAL_BUNDLE.csv). El ZIP de código no estaba disponible; el código extraído y el bundle fueron suficientes. La mejora HU-02 está conservada.

## Compilación y pruebas

Java 21.0.9, Maven de caché local, Spring Boot 3.3.5, Windows, H2 aislada. Se usaron puertos libres y dirección 127.0.0.1; no se alteró la instancia del usuario en 8080. El primer intento de Maven quedó bloqueado por el entorno aislado; se conservó su log y se ejecutó de nuevo fuera del aislamiento con `mvn -DforkCount=0 clean verify`. No se presenta el intento bloqueado como fallo del código.

| Ejecución | Resultado actual | Evidencia |
|---|---|---|
| Fuente baseline 41070eb | 12 JUnit PASS | EVIDENCIAS/maven-baseline.log y junit-baseline |
| Fuente incremento | 46 JUnit PASS, cero fallos/errores/omitidos | EVIDENCIAS/maven-final.log y junit-final |
| JAR original | 8/8 HU-02 y 8/9 HTTP PASS | BASE-* en navegador-resultados.json |
| Chrome y Edge incremento | 41/41 checks PASS | EVIDENCIAS/playwright.log y navegador-resultados.json |
| Rendimiento DOM local | 20/20 <=3 s; p95 0.170 s | EVIDENCIAS/rendimiento-20.json |

El humo HTTP de nueve checks es una reconstrucción explícita del alcance del informe previo; no se dispone de su script original. Los checks se solapan y no se suman como HU terminadas. El script del navegador incluye las aserciones, entradas y procedimiento; los JUnit XML identifican cada método, duración y resultado. El catálogo completo de ejecución está en [RESULTADOS_PRUEBAS.csv](RESULTADOS_PRUEBAS.csv).

El JAR original devuelve HTTP 405 al omitir CSRF en una sesión autenticada (BASE-HTTP-03); el incremento devuelve 403 (SECURITY-CSRF-01). El intento 03 también mostró una espera insuficiente del verificador durante la carga demo: siete resultados dependían de datos aún no preparados; se corrigió la espera y se conservó la ejecución. El primer recorrido de navegador falló en WEB-20: el reenvío a una pantalla GET producía HTTP 405 para un POST ajeno. Se corrigió a 403 y se repitió la batería. Evidencia histórica en EVIDENCIAS/HISTORICO_ANTERIOR/INTENTO_01. Nunca se reemplaza ese resultado por una aprobación retrospectiva.

## Implementación y trazabilidad

La entrega añade autorregistro con rol ciudadano fijo, hash BCrypt y validación de cuenta; permite registrar la primera incidencia mediante ubicación local cuando no hay suministro asociado. No conecta ni vincula automáticamente suministros institucionales. La consulta pública utiliza una lista de campos permitidos; los adjuntos quedan restringidos al titular tras revisión por operador. El cierre exige titular autenticado, estado Atendido y confirmación expresa. La reapertura exige Cerrado, motivo 10 a 500 y ausencia de solicitud pendiente; conserva el historial.

Los archivos se validan por extensión, MIME y contenido; imágenes mediante ImageIO y PDF mediante PDFBox 3.0.7. El límite es 5 MiB y hasta cinco por caso. La revisión registra actor y fecha. La generación de códigos usa identidad asignada por la BD para evitar el cálculo concurrente count+1 del antecedente. La consola H2 queda denegada y los datos demo se desactivan fuera del perfil demo por defecto.

Consultar [ESTADO_10_RF.csv](ESTADO_10_RF.csv), [ESTADO_8_HU.csv](ESTADO_8_HU.csv), [ESTADO_39_CA.csv](ESTADO_39_CA.csv), [ESTADO_9_RNF.csv](ESTADO_9_RNF.csv) y [ESTADO_9_DOD.csv](ESTADO_9_DOD.csv). Todos los textos aprobados se conservan en los derivados. Los 39 CA tienen correspondencia a pruebas satisfactorias dentro de los escenarios ejecutados; no equivale a aceptación formal. Ninguna HU cumple todavía toda la DoD.

## Hallazgos abiertos y límites

RNF-03 no fue ejecutado: hacen falta personas reales. RNF-09 tiene una comprobación parcial de Tab/foco y recorrido de consulta/cierre/reapertura solo con teclado; no se midió contraste de todas las vistas. Chrome y Edge headless no sustituyen Safari iOS ni móviles físicos. No se desplegó HTTPS. PDF cifrado y limpieza ante rollback fueron corregidos y probados; los negativos multipart HTTP fueron comprobados. Cada evento de aviso y campo obligatorio tiene ahora aserciones específicas. La auditoría no incluye escaneo de vulnerabilidades de dependencias ni pentest exhaustivo.

La prueba de rendimiento usó Chrome headless, navegación DOM seguimiento propio, H2 en memoria 4 incidencias, un usuario secuencial, 20 consultas, equipo local compartido. Su muestra no acredita capacidad de producción. Las capturas verifican pantallas realmente ejecutadas; no prueban todas las combinaciones de datos ni dispositivos. El código heredado y sus afirmaciones documentales se distinguen de las ejecuciones actuales. Los pendientes completos están en [NO_CONFORMIDADES.csv](NO_CONFORMIDADES.csv) y [PLAN_CORRECTIVO.md](PLAN_CORRECTIVO.md).

## Registros y aprobación

MA registra mediciones derivadas de archivos actuales; PPQA conserva hallazgos y requiere revisión independiente. CM conserva procedencia, commits y hashes. PMC propone seguimiento sin inventar responsables nominales, plazos o reuniones. SAM queda con aplicabilidad por determinar por falta de información. Sprint Backlogs, APF2 y cronograma no se declaran completados.

[ACTUALIZACIONES_PARA_MATRIZ.csv](ACTUALIZACIONES_PARA_MATRIZ.csv) es una propuesta, no una modificación de la matriz oficial. El responsable debe revisar las NC, validar el incremento y autorizar su incorporación. No se contabilizan HU Done ni se fabrican aprobaciones. Registros: REGISTRO_MA.md, CHECKLIST_PPQA.md, REGISTRO_CM.md y MATRIZ_PRELIMINAR_ESTADO.md.

Referencia técnica primaria usada para la validación PDF: [Guía oficial PDFBox 3](https://pdfbox.apache.org/3.0/migration.html). Esta referencia describe la API; no acredita por sí misma pruebas ni seguridad integral.
