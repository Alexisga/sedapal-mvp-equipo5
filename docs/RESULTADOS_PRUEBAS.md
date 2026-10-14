# Resultados de verificación técnica

Comando ejecutado con JDK 21:

```text
mvn clean verify
```

Resultado verificado: **12 pruebas ejecutadas, 0 fallos, 0 errores y 0 omitidas**.

También se ejecutó el JAR empaquetado con el perfil `demo` y se verificó por HTTP el
arranque de Tomcat, el formulario de acceso, la autenticación real con BCrypt y la carga
del portal del usuario: respuesta de login 302 y portal autenticado 200.

## Cobertura funcional

- Registro con código, incidencia, historial y notificación.
- Rechazo de suministros que no pertenecen al usuario.
- Recorrido `Asignado → En atención → Atendido → Cerrado` con trazabilidad.
- Cálculo de indicadores y generación de CSV sin depender de una sesión JPA abierta.
- Login público y redirección de visitantes en áreas privadas.
- Aislamiento de rutas por rol y respuesta 403 ante acceso cruzado.
- Protección CSRF de operaciones que modifican datos.
- Renderizado de las vistas de Usuario, Operador, Evaluador, Supervisor, Técnico, Calidad
  y Administrador.

Los reportes detallados son generados por Maven en `target/surefire-reports`.
