"""Empaqueta exclusivamente la entrega derivada y verifica sus archivos."""
from pathlib import Path
from datetime import datetime,timezone,timedelta
from PIL import Image
import csv,json,hashlib,subprocess,zipfile,shutil,os
ROOT=Path(__file__).parent.resolve();OUT=ROOT/'REVISION_FINAL'/'ENTREGA_PARA_APROBACION';EV=OUT/'EVIDENCIAS'
STAMP=datetime.now(timezone(timedelta(hours=-5))).isoformat()
CODE='55c805e352fb20afe064187a79341dcf8abb3fb8'
def run(*args):return subprocess.check_output(args,cwd=ROOT,text=True,encoding='utf-8').strip()
def write(name,text):(OUT/name).write_text(text,encoding='utf-8')
web=json.loads((EV/'navegador-resultados.json').read_text(encoding='utf-8'))
current=[r for r in web if not r['id'].startswith('BASE-')]
assert len(current)==41 and all(r['estado']=='PASS' for r in current)
assert not run('git','diff',CODE,'--','src','pom.xml'), 'El código exportado debe coincidir con el código probado'
assert all(r['version']==CODE for r in current)
official=list(csv.DictReader((OUT/'FUENTES_Y_SHA256.csv').open(encoding='utf-8-sig')))
sources=OUT/'FUENTES_REFERENCIA';sources.mkdir(exist_ok=True)
for r in official:
 f=Path(r['archivo']);assert hashlib.sha256(f.read_bytes()).hexdigest()==r['sha256']
 if f.suffix.lower() in ['.md','.docx','.xlsx']:shutil.copy2(f,sources/f.name)
write('FUENTES_REFERENCIA/LEEME.md','Copias idénticas de las fuentes aportadas, conservadas solo para consulta. Los originales en Descargas no se modificaron. Las propuestas y resultados están fuera de esta carpeta. SHA-256 en FUENTES_Y_SHA256.csv. No se incluyen aquí el JAR ni el bundle originales: la procedencia está registrada y el bundle actualizado se entrega en la raíz.\n')
write('REGISTRO_PMC.md',f'''# Registro de seguimiento del proyecto

Corte {STAMP}. Responsable de ejecución técnica: Codex. Responsables nominales de seguimiento pendientes de asignación por el Equipo 5.

El incremento probado {CODE} pasa 46 pruebas Java y 41 checks del navegador; conserva un fallo histórico del JAR original sin atribuirle una corrección retrospectiva. Cinco NC siguen abiertas: usabilidad con personas, accesibilidad completa, entornos de navegador/dispositivos, revisión independiente y matriz oficial, y HTTPS desplegado. No hay HU declaradas Done.

Seguimiento propuesto: el equipo asigna responsables y fechas reales, revisa la evidencia, formaliza la planificación de HU-08 y actualiza la matriz después de aprobar las propuestas. No hay datos para medir desvío de presupuesto, esfuerzo real de equipo ni cumplimiento de Sprints. No se inventan actas, reuniones ni compromisos. Véanse NO_CONFORMIDADES.csv, PLAN_CORRECTIVO.md y MATRIZ_PRELIMINAR_ESTADO.md.
''')
write('EVIDENCIAS/QA_DOCUMENTO/VERIFICACION_VISUAL.md',f'''# Verificación visual del informe

Fecha de registro {STAMP}. Responsable: Codex agente automatizado. Documento: REPORTE_QA.docx del incremento {CODE}.

El renderizador empaquetado se intentó y no encontró LibreOffice; sus logs históricos están conservados. Se exportó el Word final con una instancia independiente de Microsoft Word instalada, en modo invisible y solo lectura del documento derivado. Se generaron cuatro PNG mediante PDFium. Se inspeccionaron las cuatro páginas: sin texto recortado, solapamientos, tablas truncadas ni regla decorativa bajo el título; el resumen ejecutivo cabe en la primera página. Esta revisión visual automatizada no es aprobación del Equipo 5.
''')
write('EVIDENCIAS/LEEME_EVIDENCIAS.md','''# Evidencia de la entrega

Los archivos directamente en EVIDENCIAS corresponden a la ejecución definitiva. HISTORICO_ANTERIOR conserva intentos y documentos preliminares; sus resultados no sustituyen la última ejecución. INTENTO_01 contiene el defecto HTTP 405 del incremento intermedio; INTENTO_03 contiene la espera insuficiente del verificador, que fue corregida. La batería definitiva conserva un FAIL del JAR original (BASE-HTTP-03); todos los checks del incremento son PASS. QA_DOCUMENTO son renders reales del informe Word, separados de las capturas PNG de la aplicación.

Las capturas reales están indexadas en CAPTURAS_INDICE.csv, con tamaño, fecha de archivo, commit, responsable y SHA-256. El contraste medido corresponde solo a 59 muestras sólidas de la pantalla de seguimiento; no es una auditoría de todas las vistas.
''')
# Correct historical paths in derived text without rewriting any official document.
for f in OUT.glob('*.md'):
 s=f.read_text(encoding='utf-8').replace('EVIDENCIAS/INTENTO_01','EVIDENCIAS/HISTORICO_ANTERIOR/INTENTO_01').replace('EVIDENCIAS/INTENTO_03','EVIDENCIAS/HISTORICO_ANTERIOR/INTENTO_03')
 f.write_text(s,encoding='utf-8')
for f in OUT.glob('*.csv'):
 s=f.read_text(encoding='utf-8-sig').replace('EVIDENCIAS/INTENTO_01','EVIDENCIAS/HISTORICO_ANTERIOR/INTENTO_01').replace('EVIDENCIAS/INTENTO_03','EVIDENCIAS/HISTORICO_ANTERIOR/INTENTO_03')
 f.write_text(s,encoding='utf-8-sig')
compiled=OUT/'COMPILACION';compiled.mkdir(exist_ok=True);jar=ROOT/'target'/'sedapal-reclamos-0.1.1-SNAPSHOT.jar';shutil.copy2(jar,compiled/jar.name)
env=json.loads((EV/'entorno-final.json').read_text(encoding='utf-8'))
assert hashlib.sha256(jar.read_bytes()).hexdigest()==env['jar_sha256']
# Keep the reviewed records under configuration control as derived documents.
records=ROOT/'docs'/'auditoria-2026-10-14';records.mkdir(exist_ok=True)
for f in OUT.iterdir():
 if f.is_file() and f.suffix in ['.md','.csv','.docx']:shutil.copy2(f,records/f.name)
gitignore=ROOT/'.gitignore';s=gitignore.read_text(encoding='utf-8')
for line in ['REVISION_FINAL/','contacto-capturas.png','contacto-final.png','qa_actualizar_informe.py','qa_cierre_informe.py','qa_cobertura.py','qa_evidencias.py','qa_finalizar_codigo.py','qa_integracion.py','qa_limites.py','qa_modificar.py','qa_pdf.py']:
 if line not in s.splitlines():s+='\n'+line
gitignore.write_text(s+'\n',encoding='utf-8')
subprocess.run(['git','add','.gitignore','qa_navegador.py','qa_entrega.py','qa_contraste.py','qa_render_word.ps1','qa_empaquetar.py','docs/auditoria-2026-10-14'],cwd=ROOT,check=True)
subprocess.run(['git','-c','user.name=Codex QA','-c','user.email=codex-qa@local.invalid','commit','-m','docs(QA): entrega derivada, trazabilidad y herramientas reproducibles para aprobacion'],cwd=ROOT,check=True)
PACKAGE=run('git','rev-parse','HEAD')
assert not run('git','diff',CODE,PACKAGE,'--','src','pom.xml')
write('HISTORIAL_COMMITS.txt',run('git','log','--all','--date=iso-strict','--format=%H | %aI | %an | %s')+'\n')
write('DIFF_CODIGO_DESDE_41070EB.patch',run('git','diff','41070eb',PACKAGE,'--','src','pom.xml')+'\n')
bundle=OUT/'HISTORIAL_GIT.bundle';subprocess.run(['git','bundle','create',str(bundle),'--all'],cwd=ROOT,check=True)
validation=subprocess.run(['git','bundle','verify',str(bundle)],cwd=ROOT,capture_output=True,text=True,check=True)
write('EVIDENCIAS/bundle-verificacion.txt',validation.stdout+'\n'+validation.stderr)
sourcezip=OUT/'CODIGO_FUENTE_SEDAPAL_v0.1.1_2026-10-14.zip';subprocess.run(['git','archive','--format=zip','-o',str(sourcezip),PACKAGE],cwd=ROOT,check=True)
with zipfile.ZipFile(sourcezip) as z:
 assert z.testzip() is None
 for name in z.namelist():assert not name.startswith('/') and '..' not in Path(name).parts
 z.extractall(OUT/'CODIGO_FUENTE')
restore=ROOT/'REVISION_FINAL'/'VERIFICACION_RESTAURACION_GIT'
assert not restore.exists();subprocess.run(['git','clone',str(bundle),str(restore)],cwd=ROOT,check=True,capture_output=True)
assert subprocess.check_output(['git','rev-parse','HEAD'],cwd=restore,text=True).strip()==PACKAGE
write('VERSIONES_ENTREGA.json',json.dumps(dict(fecha=STAMP,version='0.1.1-SNAPSHOT',commit_codigo_probado=CODE,commit_empaquetado=PACKAGE,src_pom_identicos_al_probado=True,junit_pass=46,checks_incremento_pass=41,humo_original_pass=16,humo_original_total=17,capturas=24,HU_done=0,bundle_restaurado_y_verificado=True),ensure_ascii=False,indent=2))
manifest=[]
for f in sorted(OUT.rglob('*')):
 if f.is_file() and f.name!='MANIFIESTO_SHA256.csv':manifest.append(dict(archivo=str(f.relative_to(OUT)).replace('\\','/'),bytes=f.stat().st_size,sha256=hashlib.sha256(f.read_bytes()).hexdigest()))
with (OUT/'MANIFIESTO_SHA256.csv').open('w',encoding='utf-8-sig',newline='') as f:
 w=csv.DictWriter(f,fieldnames=['archivo','bytes','sha256']);w.writeheader();w.writerows(manifest)
finalzip=OUT.parent/'ENTREGA_PARA_APROBACION_SEDAPAL_v0.1.1_2026-10-14.zip'
assert not finalzip.exists()
with zipfile.ZipFile(finalzip,'w',compression=zipfile.ZIP_DEFLATED,compresslevel=6) as z:
 for f in sorted(OUT.rglob('*')):
  if f.is_file():z.write(f,str(Path(OUT.name)/f.relative_to(OUT)))
with zipfile.ZipFile(finalzip) as z:
 assert z.testzip() is None
 for r in manifest:assert hashlib.sha256(z.read(OUT.name+'/'+r['archivo'])).hexdigest()==r['sha256']
digest=hashlib.sha256(finalzip.read_bytes()).hexdigest()
finalzip.with_suffix('.zip.sha256').write_text(digest+'  '+finalzip.name+'\n',encoding='ascii')
result=dict(zip=str(finalzip),bytes=finalzip.stat().st_size,sha256=digest,archivos=len(manifest)+1,commit_codigo=CODE,commit_paquete=PACKAGE,zip_crc_y_sha256_verificados=True,git_status=run('git','status','--short'))
(OUT.parent/'VALIDACION_ZIP.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8');print(json.dumps(result,ensure_ascii=False,indent=2))
