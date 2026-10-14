# MVP académico SEDAPAL · Desarrollo de Software I

**Inicio de las modificaciones actuales: 14/10/2026.**

Este repositorio se preparó a partir del ZIP **antecedente** `SEDAPAL_Reclamos_Proyecto_Final_Completo.zip`. El primer commit importa ese software previo; **no acredita trabajo efectuado durante los Sprints 1 y 2** del proyecto actual.

## Primer cambio real: HU-02 / RF-03 y RF-04

La consulta del ciudadano muestra un resumen de **estado actual** y **última actualización** junto a su historial. Fecha extraída del último evento histórico o, en su ausencia, de la fecha original del reclamo. Los dos bloques se ajustan a pantalla pequeña con Bootstrap.

## Cómo abrirlo en Windows

1. Descomprima el proyecto y ábralo como proyecto Maven en **NetBeans o VS Code**.
2. Use **Java 21** y **Maven 3.9 o superior**.
3. En la raíz del proyecto ejecute `mvn clean verify` (esto debe ejecutarse localmente).
4. Para una demostración **exclusivamente local**: `java -jar target/sedapal-reclamos-0.0.1-SNAPSHOT.jar --spring.profiles.active=demo --server.address=127.0.0.1`.
5. Abra `http://127.0.0.1:8080/login`. Las cuentas de ejemplo se muestran en el propio login y en `README_EJECUCION.md`.
6. Para verificar la mejora automática: `python scripts/verificar_hu02.py --jar target/sedapal-reclamos-0.0.1-SNAPSHOT.jar`.

> **Seguridad:** el perfil `demo` crea usuarios/contraseñas triviales de prueba y activa configuraciones pensadas para una exposición local. No debe publicarse en Internet ni tratar datos personales reales sin endurecer la configuración y verificar los RNF de seguridad.

## Estado verdadero

- Importación del ZIP previo: disponible en commit inicial.
- Mejora de resumen HU-02: **implementada** el 14/10/2026 y comprobada con una prueba de humo local.
- Maven `clean verify` del **código nuevo**: pendiente de ejecutar en un equipo con Maven (no disponible en el entorno de esta revisión).
- HU-02 **NO está marcada como Done**: faltan pruebas integrales, PPQA, RNF y evidencias exigidas por la DoD.
- Otras HU: pendientes de adaptación y verificación; ninguna cambia de estado por esta mejora.
