from pathlib import Path
import shutil,json,hashlib,mimetypes,zipfile,csv
ROOT=Path(__file__).resolve().parents[2]
BASE=ROOT/'ENTREGA_CIERRE_TECNICO_SEDAPAL'
OUT=ROOT/'ENTREGA_PUBLICACION_CM_HTTPS_SEDAPAL'
STAGE=ROOT/'target/drive-publicacion'
STAGE.mkdir(parents=True,exist_ok=True)
plan=[]
def add(path,section):
 plan.append({'local':str(path.resolve()),'nombre':path.name,'area':section,'bytes':path.stat().st_size,'sha256':hashlib.sha256(path.read_bytes()).hexdigest(),'mime':mimetypes.guess_type(path.name)[0] or 'application/octet-stream'})
for section,area in [('MATRICES','01_REQM_Requisitos'),('MEDICIONES','02_MA_Mediciones'),('PPQA','03_PPQA_Calidad'),('INSTRUMENTOS_HUMANOS','03_PPQA_Calidad'),('CM','04_CM_Configuracion'),('REPORTES','08_INFORMES_Y_ORIGINALES')]:
 for f in sorted((BASE/section).iterdir()):
  if f.is_file():add(f,area)
for f in (BASE/'FUENTES_OFICIALES_SOLO_LECTURA').iterdir():
 add(f,'01_REQM_Requisitos' if f.suffix=='.xlsx' or 'Catalogo' in f.name else '08_INFORMES_Y_ORIGINALES')
for name in ['REGISTRO_PMC.md','PLAN_CORRECTIVO.md']:
 add(ROOT/'docs/auditoria-2026-10-14'/name,'05_PMC_Seguimiento')
sam=STAGE/'SAM_APLICABILIDAD.md'
sam.write_text('# SAM\n\nSe conserva aplicabilidad documentada en CM v0.1: no se acredita proveedor ni contratación. No se inventa contrato ni evaluación de proveedor. Render Free es una propuesta de entorno académico sin compra ni plan de pago; cualquier aceptación contractual corresponde al titular.\n',encoding='utf-8');add(sam,'06_SAM_Adquisiciones')
add(BASE/'HISTORIAL_GIT_CIERRE.bundle','04_CM_Configuracion')
for name in ['02_JAVA_FINAL','03_MATRIZ_CONSOLIDADA','04_TLS_FINAL']:
 src=BASE/'EVIDENCIAS_NUEVAS'/name;dest=STAGE/(name+'.zip')
 with zipfile.ZipFile(dest,'w',zipfile.ZIP_DEFLATED) as z:
  for p in sorted(src.rglob('*')):
   if p.is_file():z.write(p,p.relative_to(src.parent).as_posix())
 add(dest,'07_EVIDENCIAS_TECNICAS')
# Preservar íntegros los intentos y antecedentes sin atribuirlos al último PASS.
dest=STAGE/'ANTECEDENTES_E_INTENTOS_CIERRE.zip'
with zipfile.ZipFile(dest,'w',zipfile.ZIP_DEFLATED) as z:
 for p in sorted((BASE/'EVIDENCIAS_NUEVAS').rglob('*')):
  if p.is_file() and p.relative_to(BASE/'EVIDENCIAS_NUEVAS').parts[0] not in ['02_JAVA_FINAL','03_MATRIZ_CONSOLIDADA','04_TLS_FINAL']:
   z.write(p,p.relative_to(BASE).as_posix())
 for p in sorted((BASE/'ANTECEDENTES_v0.1.1').rglob('*')):
  if p.is_file():z.write(p,p.relative_to(BASE).as_posix())
add(dest,'07_EVIDENCIAS_TECNICAS')
index=STAGE/'INDICE_DOCUMENTAL.csv'
with index.open('w',encoding='utf-8',newline='') as f:
 w=csv.DictWriter(f,fieldnames=['area','nombre','bytes','sha256','ruta_original']);w.writeheader()
 for p in plan:w.writerow({'area':p['area'],'nombre':p['nombre'],'bytes':p['bytes'],'sha256':p['sha256'],'ruta_original':p['local']})
add(index,'RAIZ')
(OUT/'DRIVE_PLAN_CARGA.json').write_text(json.dumps(plan,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({'archivos':len(plan),'bytes':sum(p['bytes'] for p in plan)}))
