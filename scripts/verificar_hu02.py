"""Prueba de humo HU-02 para el entorno de demostración LOCAL.

Requisitos: Python 3, Java 21 y el JAR empaquetado. No prueba DoD completa.
Ejemplo: python scripts/verificar_hu02.py --jar target/sedapal-reclamos-0.0.1-SNAPSHOT.jar
"""
import argparse
from datetime import datetime, timezone
from http.cookiejar import CookieJar
from pathlib import Path
import re
import socket
import subprocess
import sys
import tempfile
import time
from urllib.parse import urlencode
from urllib.request import build_opener, HTTPCookieProcessor, Request
from urllib.error import URLError


def open_session(base, documento):
    client = build_opener(HTTPCookieProcessor(CookieJar()))
    login_html = client.open(base + "/login", timeout=4).read().decode("utf-8")
    token = re.search(r'name="_csrf"[^>]*value="([^"]+)"', login_html)
    if not token:
        raise AssertionError("No se encontró token CSRF en el formulario")
    payload = urlencode({"documento": documento, "password": "1234", "_csrf": token.group(1)}).encode()
    response = client.open(Request(base + "/login", data=payload,
                                   headers={"Content-Type": "application/x-www-form-urlencoded"}), timeout=5)
    if not response.geturl().endswith("/usuario"):
        raise AssertionError("Error de ingreso en cuenta de demostración")
    return client, response.read().decode("utf-8")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--jar", default="target/sedapal-reclamos-0.0.1-SNAPSHOT.jar")
    args = parser.parse_args()
    jar = Path(args.jar).expanduser().resolve()
    if not jar.is_file():
        print("ERROR: el JAR no existe:", jar)
        return 2
    with socket.socket() as sock:
        sock.bind(("127.0.0.1", 0))
        port = sock.getsockname()[1]
    base = f"http://127.0.0.1:{port}"
    with tempfile.TemporaryDirectory(prefix="sedapal-hu02-") as td:
        logfile = Path(td) / "servidor.log"
        with logfile.open("wb") as log:
            process = subprocess.Popen(["java", "-jar", str(jar), "--spring.profiles.active=demo",
                                        f"--server.port={port}", "--server.address=127.0.0.1",
                                        "--spring.h2.console.enabled=false"], cwd=td, stdout=log, stderr=subprocess.STDOUT)
            try:
                for _ in range(60):
                    try:
                        if build_opener().open(base + "/login", timeout=2).status == 200:
                            break
                    except (URLError, TimeoutError):
                        pass
                    if process.poll() is not None:
                        raise RuntimeError("Falló el inicio de Java; revisar servidor.log")
                    time.sleep(.5)
                else:
                    raise RuntimeError("No inició el servidor en 30 segundos")
                # Datos de demostración: la carga inicial finaliza después de abrir el puerto.
                time.sleep(2)
                usuario, tablero = open_session(base, "12345678")
                codigos = re.findall(r"REC-\d{4}-\d+", tablero)
                if not codigos:
                    raise AssertionError("La cuenta de demostración no tiene incidencias")
                codigo = codigos[0]
                respuesta = usuario.open(base + "/usuario/consultar?" + urlencode({"codigo": codigo}), timeout=5)
                html = respuesta.read().decode("utf-8")
                checks = {
                    "Consulta con HTTP 200": respuesta.status == 200,
                    "Incidencia del titular": codigo in html,
                    "Resumen estado actual": "Estado actual" in html,
                    "Resumen última actualización": "Última actualización registrada" in html,
                    "Fecha y hora visibles": bool(re.search(r"\d{2}/\d{2}/\d{4} \d{2}:\d{2}", html)),
                    "Dos bloques responsivos": html.count("col-12 col-md-6") >= 2,
                    "Historial visible": "Historial verificable" in html,
                }
                otro, _ = open_session(base, "87654321")
                html_otro = otro.open(base + "/usuario/consultar?" + urlencode({"codigo": codigo}),timeout=5).read().decode("utf-8")
                checks["Acceso ajeno rechazado"] = (
                    "No se encontró un reclamo propio" in html_otro
                    and "Resumen de seguimiento de la incidencia" not in html_otro
                    and codigo not in re.findall(r"<h2[^>]*>([^<]+)</h2>", html_otro)
                )
                for nombre, passed in checks.items():
                    print(("PASS" if passed else "FAIL") + " | " + nombre)
                print("Fecha UTC:",datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%SZ"))
                print(f"Resultado: {sum(checks.values())}/{len(checks)} verificaciones")
                return 0 if all(checks.values()) else 1
            finally:
                process.terminate()
                try:
                    process.wait(timeout=8)
                except subprocess.TimeoutExpired:
                    process.kill()


if __name__ == "__main__":
    sys.exit(main())
