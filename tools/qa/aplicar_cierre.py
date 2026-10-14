from pathlib import Path
p=Path(__file__).resolve().parents[2]
def edit(f,a,b):
 q=p/f;s=q.read_text(encoding='utf-8');assert a in s,f; q.write_text(s.replace(a,b),encoding='utf-8')
edit('pom.xml','<version>0.1.1-SNAPSHOT</version>','<version>0.1.2-SNAPSHOT</version>')
f=p/'src/main/resources/static/css/style.css';f.write_text(f.read_text(encoding='utf-8')+'''
/* Axe verificó contraste insuficiente del azul Bootstrap sobre fondos claros. */
:root {
    --bs-link-color: #0056a0;
    --bs-link-color-rgb: 0, 86, 160;
    --bs-link-hover-color: #064273;
    --bs-link-hover-color-rgb: 6, 66, 115;
}
''',encoding='utf-8')
f=p/'src/main/resources/templates/registro.html';f.write_text('''<!DOCTYPE html>
<html lang="es" xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{fragments/common :: head('Crear cuenta')}"></head>
<body>
<a class="skip-link" href="#contenido">Saltar al formulario</a>
<main id="contenido" class="container page-shell">
 <h1>Crear cuenta ciudadana</h1>
 <div th:replace="~{fragments/common :: alerts}"></div>
 <form th:action="@{/registro}" th:object="${registroForm}" method="post" class="panel-body panel needs-validation" novalidate>
  <label for="nombre">Nombre</label>
  <input id="nombre" class="form-control" th:field="*{nombre}" maxlength="80" required autocomplete="name"
         th:aria-invalid="${#fields.hasErrors('nombre')}" th:aria-describedby="${#fields.hasErrors('nombre') ? 'nombre-error' : null}">
  <p id="nombre-error" role="alert" th:errors="*{nombre}"></p>
  <label for="documento">Nombre de usuario</label>
  <input id="documento" class="form-control" th:field="*{documento}" autocomplete="username" maxlength="255" required
         th:aria-invalid="${#fields.hasErrors('documento')}" th:aria-describedby="${#fields.hasErrors('documento') ? 'documento-error' : null}">
  <p id="documento-error" role="alert" th:errors="*{documento}"></p>
  <label for="password">Contraseña de 8 a 72 caracteres</label>
  <input id="password" name="password" class="form-control" type="password" autocomplete="new-password" minlength="8" maxlength="72" required
         th:aria-invalid="${#fields.hasErrors('password') or #fields.hasErrors('passwordBytesValid')}"
         th:aria-describedby="${#fields.hasErrors('password') ? 'password-error' : (#fields.hasErrors('passwordBytesValid') ? 'password-bytes-error' : null)}">
  <p id="password-error" role="alert" th:errors="*{password}"></p>
  <p id="password-bytes-error" role="alert" th:errors="*{passwordBytesValid}"></p>
  <button class="btn btn-sedapal mt-3">Crear cuenta</button>
 </form>
 <a th:href="@{/login}">Iniciar sesión</a>
</main>
<th:block th:replace="~{fragments/common :: scripts}"></th:block>
</body></html>
''',encoding='utf-8')
edit('src/main/resources/templates/fragments/common.html','role="alert">','role="alert" tabindex="-1">')
edit('src/main/java/com/utp/sedapal/services/DatosInicialesService.java','    private final boolean demoData;','    private final boolean demoData;\n    private final String demoPassword;')
edit('src/main/java/com/utp/sedapal/services/DatosInicialesService.java','@Value("${app.demo-data:true}") boolean demoData)', '@Value("${app.demo-data:false}") boolean demoData,\n                                 @Value("${app.demo-password:}") String demoPassword)')
edit('src/main/java/com/utp/sedapal/services/DatosInicialesService.java','        this.demoData = demoData;','        this.demoData = demoData;\n        this.demoPassword = demoPassword;')
edit('src/main/java/com/utp/sedapal/services/DatosInicialesService.java','        if (!demoData || usuarioRepository.count() > 0) return;','        if (!demoData || usuarioRepository.count() > 0) return;\n        if (demoPassword.length() < 8) throw new IllegalStateException("Defina DEMO_PASSWORD con al menos ocho caracteres para crear datos de demostración.");')
edit('src/main/java/com/utp/sedapal/services/DatosInicialesService.java','passwordEncoder.encode("1234")','passwordEncoder.encode(demoPassword)')
edit('src/main/resources/application.properties','${DB_URL:jdbc:mysql://localhost:3306/bd_reclamos_sedapal?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Lima}','${DB_URL}')
edit('src/main/resources/application.properties','${DB_USER:sedapal_app}','${DB_USER:}')
edit('src/main/resources/application.properties','${DB_PASSWORD:sedapal123}','${DB_PASSWORD:}')
edit('src/main/resources/application.properties','spring.jpa.hibernate.ddl-auto=update','spring.jpa.hibernate.ddl-auto=${DB_DDL_AUTO:validate}')
f=p/'src/main/resources/application-demo.properties';f.write_text(f.read_text(encoding='utf-8')+'\napp.demo-password=${DEMO_PASSWORD:}\n',encoding='utf-8')
f=p/'src/test/resources/application-test.properties';f.write_text(f.read_text(encoding='utf-8')+'\napp.demo-password=ClaveDemoQA123\n',encoding='utf-8')
f=p/'.gitignore';f.write_text(f.read_text(encoding='utf-8')+'\nFUENTES_ANALISIS/\nEVIDENCIAS_CIERRE/\nENTREGA_CIERRE_TECNICO_SEDAPAL/\n*.p12\n*.key\n',encoding='utf-8')
