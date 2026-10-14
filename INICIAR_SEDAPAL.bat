@echo off
setlocal
cd /d "%~dp0"

where java >nul 2>&1
if errorlevel 1 (
    echo ERROR: No se encontro Java. Instale JDK 21 y agreguelo al PATH.
    pause
    exit /b 1
)

where mvn >nul 2>&1
if not errorlevel 1 goto :maven_path
if exist "C:\Program Files\NetBeans-22\netbeans\java\maven\bin\mvn.cmd" goto :maven_netbeans
echo ERROR: No se encontro Maven ni el Maven incluido en NetBeans 22.
pause
exit /b 1

:maven_path
echo Compilando y ejecutando las pruebas del sistema...
call mvn clean verify
goto :verificar

:maven_netbeans
echo Compilando y ejecutando las pruebas del sistema con Maven de NetBeans...
call "C:\Program Files\NetBeans-22\netbeans\java\maven\bin\mvn.cmd" clean verify

:verificar
if errorlevel 1 (
    echo.
    echo ERROR: La compilacion o alguna prueba no termino correctamente.
    pause
    exit /b 1
)

echo.
echo Sistema listo en http://localhost:8080/login
echo Perfil: demostracion local H2. Presione CTRL+C para detenerlo.
java -jar "target\sedapal-reclamos-0.1.2-SNAPSHOT.jar" --spring.profiles.active=demo
pause
endlocal
