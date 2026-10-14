"""Entrega derivada, sin escribir los documentos oficiales. Requiere pruebas concluidas."""
from pathlib import Path
from datetime import datetime,timezone,timedelta
import json,csv,hashlib,shutil,subprocess,zipfile,xml.etree.ElementTree as ET
import os
_copy_original=shutil.copy2
def copy_long(src,dst,*,follow_symlinks=True):
 if os.name=='nt':
  src='\\\\?\\'+str(Path(src).resolve());dst='\\\\?\\'+str(Path(dst).resolve())
 return _copy_original(src,dst,follow_symlinks=follow_symlinks)
shutil.copy2=copy_long
from PIL import Image
ROOT=Path(__file__).resolve().parents[2];OUT=ROOT/'ENTREGA_CIERRE_TECNICO_SEDAPAL';EV=ROOT/'EVIDENCIAS_CIERRE';DL=Path('C:/Users/User/Downloads')
if (DL/'TODO SOBRE SEDAPAÑ/SEDAPAL_Borrador_Maestro_Formatos_Docente_v1.9_2026-10-14.docx').exists():DL=DL/'TODO SOBRE SEDAPAÑ'
STAMP=datetime.now(timezone(timedelta(hours=-5))).isoformat()
def git(*a):return subprocess.check_output(['git',*a],cwd=ROOT,text=True,encoding='utf-8').strip()
CODE=git('rev-parse','HEAD');BASE='70b4b77b2a84a08b12c2eacdac84e2a1edc37890'
def write(name,text):
 f=OUT/name;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(text,encoding='utf-8')
def csvfile(name,rows):
 rows=list(rows);f=OUT/name;f.parent.mkdir(parents=True,exist_ok=True)
 with f.open('w',encoding='utf-8-sig',newline='') as stream:
  w=csv.DictWriter(stream,fieldnames=list(rows[0]));w.writeheader();w.writerows(rows)
browser=EV/'03_MATRIZ_CONSOLIDADA';checks=json.loads((browser/'checks.json').read_text(encoding='utf-8'));scans=json.loads((browser/'scans-indice.json').read_text(encoding='utf-8'));engines=json.loads((browser/'motores.json').read_text(encoding='utf-8'))
assert len([r for r in engines if r.get('estado')=='EJECUTADO'])==4
assert not any('razon' in r for r in engines), 'No declarar compatible un motor con ejecución truncada'
assert all(r['estado']=='PASS' for r in checks)
assert len(scans)==156
metric=json.loads((EV/'02_JAVA_FINAL/IP05/medicion.json').read_text(encoding='utf-8'))
tls=json.loads((EV/'04_TLS_FINAL/tls-resultados.json').read_text(encoding='utf-8'));assert all(r['estado']=='PASS' for r in tls['checks'])
junit=[]
for f in (EV/'02_JAVA_FINAL/junit').glob('TEST-*.xml'):
 e=ET.parse(f).getroot()
 for t in e.findall('testcase'):junit.append(dict(id=e.get('name')+'.'+t.get('name'),segundos=t.get('time'),estado='FAIL' if t.find('failure') is not None or t.find('error') is not None else 'PASS',archivo=f.name))
assert len(junit)==3 and all(r['estado']=='PASS' for r in junit)
if (OUT/'VERSION_ENTREGA.json').exists():raise RuntimeError('No sobrescribir una entrega finalizada')
OUT.mkdir(exist_ok=True)
shutil.copytree(EV,OUT/'EVIDENCIAS_NUEVAS',dirs_exist_ok=True,copy_function=copy_long)
for f in ROOT.glob('cierre-*.log'):shutil.copy2(f,OUT/'EVIDENCIAS_NUEVAS'/f.name)
csvfile('REPORTES/RESULTADOS_JAVA.csv',junit);csvfile('REPORTES/RESULTADOS_NAVEGADOR.csv',checks)
csvfile('REPORTES/RESPONSIVIDAD_Y_AXE.csv',[dict(id=s['id'],motor=s['motor'],pantalla=s['pantalla'],ancho=s['ancho'],fecha=s['fecha'],commit=s['commit'],violaciones=len(s['violaciones']),incompletos=';'.join(s['incompletos']),ancho_documento=s['scroll']['document'],etiquetados=s['controles_etiquetados'],captura='EVIDENCIAS_NUEVAS/03_MATRIZ_CONSOLIDADA/'+s['captura'],axe='EVIDENCIAS_NUEVAS/03_MATRIZ_CONSOLIDADA/'+s['axe']) for s in scans])
shots=[]
for f in sorted(browser.glob('*.png')):
 im=Image.open(f);shots.append(dict(archivo='EVIDENCIAS_NUEVAS/03_MATRIZ_CONSOLIDADA/'+f.name,ancho=im.width,alto=im.height,fecha_archivo=datetime.fromtimestamp(f.stat().st_mtime,timezone(timedelta(hours=-5))).isoformat(),commit=CODE,responsable='Codex Playwright',tipo='Captura auténtica de aplicación local con datos simulados',sha256=hashlib.sha256(f.read_bytes()).hexdigest()))
csvfile('REPORTES/CAPTURAS_INDICE.csv',shots)
csvfile('MEDICIONES/IP05_CASOS.csv',[dict(codigo=r['codigo'],estado=r['estado'],fecha_registro=r['fecha_registro'],fecha_ultima=r['fecha_ultima'],fecha_completa=r['fecha_completa'],evidencias=len(r['evidencias']),consultable_titular=r['evidencia_consultable_titular'],completo=r['completo'],causa=r['causa'],fuente='EVIDENCIAS_NUEVAS/02_JAVA_FINAL/IP05/medicion.json',datos='SIMULADOS') for r in metric['evaluados']])
csvfile('MEDICIONES/IP05_EXCLUIDOS.csv',metric['excluidos'])
write('MEDICIONES/REGISTRO_MA_CIERRE.md',f'''# Medición del cierre técnico

Corte {STAMP}. Responsable de ejecución Codex. Incremento v0.1.2-SNAPSHOT, código {CODE}.

MTR-IP05 = {metric['numerador']}/{metric['denominador']} ×100 = {metric['IP05_porcentaje']:.2f} %. Meta ≥90 %: NO CUMPLE. Se evaluó toda la cohorte prefijada de seis expedientes Atendido, Cerrado o Reapertura solicitada, en H2 aislada. El caso Registrado se excluyó por etapa y está listado. Dos casos no tenían adjuntos y uno tenía evidencia pendiente de publicación. Se exige que la evidencia vinculada sea consultable por el titular: una restricción correcta por revisión pendiente no se confunde con completitud ciudadana.

La cohorte es sintética y deliberadamente mixta para comprobar detección de vacíos; no es una muestra aleatoria ni estima producción. No se descartaron casos incompletos ni se añadieron éxitos después del cálculo. La meta no se fuerza inventando adjuntos. Las respuestas de titular/ajeno/operador y los archivos exactos con SHA-256 están en medicion.json y adjuntos.

MTR-IP03 por ciclo nuevo: Java {len(junit)}/{len(junit)} PASS; navegador {len(checks)}/{len(checks)} PASS; TLS {len(tls['checks'])}/{len(tls['checks'])} PASS. Son baterías distintas y checks solapados; no equivalen a historias terminadas. Los 46 Java, 41 web, 24 capturas y 20 consultas del incremento anterior se conservan como antecedentes de v0.1.1, sin presentarlos como ejecución nueva. IP04/RNF-03 humano: no medido. GP01: sin HP comprometidas verificables. HU Done: 0/8.
''')
# Verify the incoming CM manifest before reusing its content.
cm=ROOT/'FUENTES_ANALISIS/CM_REFERENCIA';validation=[]
for r in csv.DictReader((cm/'MANIFIESTO_CM_LOCAL.csv').open(encoding='utf-8-sig')):
 f=cm/r['Ruta'];ok=f.is_file() and f.stat().st_size==int(r['Tamano_bytes']) and hashlib.sha256(f.read_bytes()).hexdigest().lower()==r['SHA256'].lower()
 validation.append(dict(ruta=r['Ruta'],estado='PASS' if ok else 'FAIL',esperado_sha256=r['SHA256']))
csvfile('CM/VALIDACION_CM_ORIGINAL.csv',validation)
sources=[]
for name in ['SEDAPAL_Borrador_Maestro_Formatos_Docente_v1.9_2026-10-14.docx','SEDAPAL_Matriz_Maestra_Trazabilidad_v1.12_2026-10-14.xlsx','SEDAPAL_Repositorio_CM_Local_v0.1_2026-10-14.zip','SEDAPAL_Catalogo_RF_Backlog_RNF_DoD_v1.4_2026-10-14.docx']:
 f=DL/name
 if not f.exists() and 'Catalogo_RF' in name:
  f=ROOT.parent/'SEDAPAL_AUDITORIA_CODEX_2026-10-14/REVISION_FINAL/ENTREGA_PARA_APROBACION/FUENTES_REFERENCIA'/name
 if not f.exists() and 'Catalogo_RF' in name:continue
 dest=OUT/'FUENTES_OFICIALES_SOLO_LECTURA'/name;dest.parent.mkdir(exist_ok=True);shutil.copy2(f,dest)
 sources.append(dict(archivo=name,version='Ver nombre y contenido de fuente',sha256=hashlib.sha256(f.read_bytes()).hexdigest(),original=str(f),uso='Solo lectura; copia idéntica',fecha_verificacion=STAMP))
csvfile('CM/FUENTES_SHA256.csv',sources)
prior=ROOT.parent/'SEDAPAL_AUDITORIA_CODEX_2026-10-14/REVISION_FINAL/ENTREGA_PARA_APROBACION'
for name in ['ESTADO_39_CA.csv','ESTADO_8_HU.csv','ESTADO_9_DOD.csv','ESTADO_9_RNF.csv','NO_CONFORMIDADES.csv','VERSIONES_ENTREGA.json','RESULTADOS_PRUEBAS.csv']:
 dest=OUT/'ANTECEDENTES_v0.1.1'/name;dest.parent.mkdir(exist_ok=True);shutil.copy2(prior/name,dest)
priorca=list(csv.DictReader((prior/'ESTADO_39_CA.csv').open(encoding='utf-8-sig')))
RF={1:'RF-01;RF-02',2:'RF-03;RF-04',3:'RF-05',4:'RF-06',5:'RF-07',6:'RF-08',7:'RF-09',8:'RF-10'}
RNF={1:'01;02;05;07;08;09',2:'01;02;03;04;05;07;08;09',3:'01;02;03;05;06;07;09',4:'01;02;05;07;08;09',5:'01;02;05;07;08;09',6:'01;02;05;07;08;09',7:'01;02;05;06;07;08;09',8:'01;02;05;07;09'}
NEW={1:'VINCULO-TITULAR;INCIDENCIA-AJENA;registro-incidencia',2:'consulta-publica;seguimiento-sin-evidencia;atendido-evidencia',3:'ADJUNTO-OCULTO;ADJUNTO-TITULAR;ADJUNTO-AJENO;IP05',4:'notificaciones;HU04CadaTransicion (antecedente Java)',5:'AJENO-cerrar;FLUJO-TECLADO;cerrado-reapertura-form',6:'AJENO-reapertura;FLUJO-TECLADO;reapertura-solicitada',7:'atencion-tecnica;revision-evidencia;resultado-carga;IP05',8:'ERROR-ARIA;CUENTA-PROPIA;VINCULO-TITULAR;LOGOUT;TLS-03;CierreAccesibilidadSeguridadTest'}
dodrows=[];hurows=[]
for n in range(1,9):
 hu=f'HU-{n:02}';criteria=[r for r in priorca if r['HU']==hu]
 for k in range(1,10):
  if k in [1,2,6,7]:state='VERIFICADO';detail={1:'Definición y aprobación documental conservadas en maestro v1.9 y catálogo v1.4.',2:'Funcionalidad integrada ejecutada en los escenarios focalizados; no supone expediente completo en todos los casos.',6:'Versión y commits Git reales, bundle recuperable.',7:'Pruebas nuevas y antecedentes separados, con fecha/commit/esperado/obtenido/responsable.'}[k]
  else:state='PENDIENTE DE PERSONA';detail={3:'Criterios trazados a evidencia previa y regresión focalizada; falta ratificación independiente de casos y aceptación.',4:'Revisión humana de accesibilidad/ayudas técnicas y Safari físico; RNF-03 con personas cuando aplica. HTTPS público depende de entorno autorizado.',5:'Revisor distinto del autor no ha emitido conformidad; IP05 no alcanza meta en cohorte medida.',8:'Matriz oficial v1.12 no se modifica; propuesta de versión v0.1.2 pendiente de incorporación aprobada.',9:'Incremento disponible para inspección; aceptación humana del nuevo cierre no acreditada.'}[k]
  dodrows.append(dict(HU=hu,DoD=f'DoD-{k:02}',estado=state,detalle=detail,RF=RF[n],CA=';'.join(r['CA'] for r in criteria),RNF_aplicables=';'.join('RNF-'+x for x in RNF[n].split(';')),pruebas_nuevas=NEW[n],pruebas_previas='ANTECEDENTES_v0.1.1/ESTADO_39_CA.csv; versión 55c805e',evidencias='REPORTES/RESULTADOS_NAVEGADOR.csv;REPORTES/RESULTADOS_JAVA.csv;EVIDENCIAS_NUEVAS',NC_relacionadas='NC-07..11;CIERRE-IP05',commit=CODE))
 hurows.append(dict(HU=hu,RF=RF[n],CA_total=len(criteria),criterios_con_evidencia='Todos en antecedente identificado; regresión selectiva nueva en funciones afectadas',RNF_aplicables=RNF[n],pruebas_nuevas=NEW[n],DoD_verificadas='01;02;06;07',DoD_pendientes='03;04;05;08;09',resultado='NO DONE',responsable_resolver='Equipo 5: revisor independiente y responsable delegado; entorno Safari/HTTPS autorizado'))
csvfile('MATRICES/HU_DOD_72_CONDICIONES.csv',dodrows);csvfile('MATRICES/ESTADO_8_HU.csv',hurows)
csvfile('MATRICES/ESTADO_39_CA.csv',[{**r,'evidencia_nueva':NEW[int(r['HU'][-2:])],'commit_nuevo':CODE,'alcance_reverificacion':'Conservar prueba anterior identificada; nueva regresión focalizada. No se repitió toda la auditoría.'} for r in priorca])
csvfile('MATRICES/ESTADO_10_RF.csv',[dict(RF=rf,HU=f'HU-{n:02}',estado='VERIFICADO',resultado='Implementado y probado en escenarios identificados; HU NO DONE',evidencia=NEW[n],fuente_aprobada='Catálogo v1.4; Borrador v1.9',commit=CODE) for n,refs in RF.items() for rf in refs.split(';')])
rnfdata=[
 ('01','VERIFICADO','Matriz automatizada 360/768/1366 en cuatro motores; cabecera Firefox corregida.','Inspección física de ergonomía por persona aún pendiente.'),
 ('02','VERIFICADO','Chrome, Edge, Firefox y WebKit headless completaron flujos; versiones en motores.json.','Safari físico no ejecutado; WebKit no equivale a Safari.'),
 ('03','PENDIENTE DE PERSONA','Protocolo y cinco fichas vacías preparados.','Cinco personas reales y moderador; cronometraje de localización ≤30s.'),
 ('04','VERIFICADO','Antecedente v0.1.1: 20/20 ≤3s; p95 0.170s. No medido nuevamente ni presentado como dato de v0.1.2.','Revisor debe aceptar continuidad de evidencia o definir nueva medición; no se altera el resultado previo.'),
 ('05','VERIFICADO','Titularidad, archivos, ajeno, CSRF y logout probados en cuatro motores; permisos por caso IP05.','No sustituye pentest ni revisión independiente.'),
 ('06','VERIFICADO','Pruebas previas de tipos/tamaños/cupo conservadas; cargas PDF y adjuntos PNG/JPG/PDF del IP05 ejecutados.','No se repitió batería previa completa de límites; alcance identificado.'),
 ('07','VERIFICADO','BCrypt y credenciales por entorno; TLS local con confianza explícita, cookies seguras, HSTS y logout.','HTTPS público PENDIENTE DE ENTORNO/AUTORIZACIÓN; no se publicó el MVP.'),
 ('08','VERIFICADO','Antecedente de integridad y transiciones nuevas de cierre/reapertura con historial visible.','No afirma recuperación ante pérdida de discos ni backup productivo.'),
 ('09','VERIFICADO',f'{len(scans)} escaneos axe WCAG A/AA sin violaciones detectadas; teclado/foco/etiquetas y errores.','Axe incomplete y aspectos semánticos/ayudas técnicas requieren revisión humana; no es certificación WCAG.')]
csvfile('MATRICES/ESTADO_9_RNF.csv',[dict(RNF='RNF-'+a,estado=b,resultado=c,limite_o_pendiente=d,evidencia='REPORTES/RESPONSIVIDAD_Y_AXE.csv;EVIDENCIAS_NUEVAS;ANTECEDENTES_v0.1.1',commit=CODE) for a,b,c,d in rnfdata])
csvfile('MATRICES/PROPUESTAS_MATRIZ_V1.12.csv',[dict(EDT={'HU-01':'1.2.1-1.2.3','HU-02':'1.3.1-1.3.3','HU-03':'1.4.2','HU-04':'1.4.3','HU-05':'1.5.1','HU-06':'1.5.2','HU-07':'1.4.1','HU-08':'CF-001;sin EDT nueva aprobada'}[r['HU']],RF=r['RF'],HU=r['HU'],CA=r['CA'],evidencia_anterior=r['evidencia'],evidencia_nueva=NEW[int(r['HU'][-2:])],commit=CODE,PPQA='Automatizada; independiente pendiente',estado_propuesto='Avance técnico verificado con límites; NO DONE',aprobacion='PENDIENTE DE PERSONA; no sobrescribe matriz oficial') for r in priorca])
defects=[
 ('CIERRE-A11Y-01','Seria','Contraste 4.21 y 4.29:1 de enlaces y avisos','Corregido y verificado','01_ANTES;03_NAVEGADORES_FINAL','VERIFICADO','No requiere decisión de alcance'),
 ('CIERRE-A11Y-02','Media','Error de contraseña sin asociación accesible y foco','Corregido y verificado','ERROR-ARIA;CierreAccesibilidadSeguridadTest','VERIFICADO','Validación humana semántica pendiente por separado'),
 ('CIERRE-RESP-01','Media','Firefox: overflow 5px en cabecera técnica móvil','Corregido y verificado','03_NAVEGADORES_INTENTO1;03_NAVEGADORES_FINAL','VERIFICADO','Cabecera flexible y controles dentro de contenedor'),
 ('CIERRE-SEG-01','Media','Credenciales demo y BD predeterminadas en código/configuración','Corregido y verificado','Código DatosInicialesService, propiedades y prueba BCrypt','VERIFICADO','Valores externos; ningún secreto privado entregado'),
 ('CIERRE-QA-01','Baja','Selectores ambiguos y espera de upload; token TLS obsoleto en verificadores','Corregido en herramientas; histórico conservado','03_NAVEGADORES_INTENTO1;03_WEBKIT_DIAGNOSTICO;04_TLS;04_TLS_FINAL','VERIFICADO','No atribuir fallos de herramientas al producto'),
 ('CIERRE-IP05','Alta para aceptación','3/6 expedientes completos: 50%, meta 90%','Resultado medido NO CUMPLE','02_JAVA_FINAL/IP05/medicion.json','PENDIENTE DE PERSONA','Responsable de operación/PO aporta y valida evidencia faltante, revisa publicación y tratamiento de orientación; no se inventan adjuntos'),
 ('NC-07','Media','Usabilidad con personas no realizada','Abierta','INSTRUMENTOS_HUMANOS','PENDIENTE DE PERSONA','Moderador + cinco participantes reales'),
 ('NC-08','Media','Accesibilidad automatizada ampliada; revisión humana pendiente','Cierre técnico automatizado con observaciones','axe raw e incomplete','PENDIENTE DE PERSONA','Revisor accesibilidad con ayudas técnicas y evaluación semántica'),
 ('NC-09','Media','Motores completados; Safari/dispositivos físicos faltan','Parcial','motores.json','PENDIENTE DE ENTORNO/AUTORIZACIÓN','Equipo facilita Safari/iOS/dispositivos y persona ejecutora'),
 ('NC-10','Media','PPQA independiente y actualización oficial de matriz pendientes','Abierta','HU_DOD_72_CONDICIONES.csv','PENDIENTE DE PERSONA','Revisor distinto del autor y responsable delegado'),
 ('NC-11','Media','HTTPS local probado; despliegue público no autorizado ni probado','Parcial','04_TLS_FINAL','PENDIENTE DE ENTORNO/AUTORIZACIÓN','Administrador/PO proporciona entorno HTTPS autorizado'),
]
csvfile('PPQA/DEFECTOS_Y_SEGUIMIENTO.csv',[dict(id=a,prioridad=b,hallazgo=c,resultado=d,evidencia='EVIDENCIAS_NUEVAS/'+e,estado_final=f,que_se_necesita_y_quien=g,fecha=STAMP) for a,b,c,d,e,f,g in defects])
# Blank instruments, no participants, dates, signatures or conformities invented.
usability=[dict(ficha=i,participante_id='',nombre_o_consentimiento='',fecha='',dispositivo='',navegador='',version_MVP='',codigo_referencia='REC-2026-0002; confirmar caso preparado',tarea='Localizar estado, última actualización y evidencia publicada',hora_inicio='',hora_fin='',segundos='',exito='',estado_identificado='',actualizacion_identificada='',evidencia_identificada='',ayuda_recibida='',observaciones='',responsable_cronometraje='') for i in range(1,6)]
csvfile('INSTRUMENTOS_HUMANOS/RNF03_CINCO_FICHAS_SIN_EJECUTAR.csv',usability)
write('INSTRUMENTOS_HUMANOS/PROTOCOLO_RNF03.md','''# Protocolo de usabilidad pendiente

Estado PENDIENTE DE PERSONA. Se necesitan cinco participantes reales y un moderador. El caso de referencia de la prueba automatizada fue REC-2026-0002; antes de una sesión humana debe iniciarse una demo autorizada y confirmar que el titular puede abrir una evidencia publicada. Los servidores de esta auditoría fueron detenidos. No usar un caso sin adjunto ni sustituir tiempo humano por latencia.

Tarea: desde la pantalla inicial y con el código entregado, identificar correctamente estado, última actualización y evidencia. Inicio: se entrega tarea/código con pantalla lista. Fin: la persona identifica los tres elementos. Registrar tiempo individual, errores, abandonos y ayuda. No guiar durante la tarea. Guardar cinco fichas reales y evidencias anonimizadas. Calcular promedio de intentos completados y contrastar con ≤30s; reportar por separado fallos/abandonos y no declarar éxito solo con la media. No hay participantes ni tiempos registrados en esta entrega.
''')
csvfile('INSTRUMENTOS_HUMANOS/PPQA_CHECKLIST_VACIO.csv',[dict(id=i,comprobacion=c,revisor_nombre='',independiente_del_autor='',fecha='',resultado='',hallazgo='',evidencia_revisada='',conformidad='',firma_o_referencia_aprobacion='') for i,c in enumerate(['Confirmar versiones y hashes','Revisar IP05 incluyendo los tres casos incompletos','Inspeccionar permisos de evidencia','Revisar 156 scans y axe incomplete','Evaluar semántica, teclado y ayudas técnicas','Revisar TLS local y límites de despliegue','Comprobar cada HU y sus nueve DoD','Validar propuestas para matriz v1.12','Revisar defectos cerrados/abiertos y seguimiento'],1)])
csvfile('INSTRUMENTOS_HUMANOS/SAFARI_DISPOSITIVOS_FICHA_VACIA.csv',[dict(escenario=s,persona='',fecha='',dispositivo='',SO='',navegador_version='',viewport_o_resolucion='',codigo_caso='',resultado='',captura='',hallazgo='') for s in ['Cuenta y login','Registro','Consulta y timeline','Adjuntos privados','Notificaciones','Cierre titular','Reapertura','Logout y enlace protegido']])
incomplete=[dict(scan=s['id'],regla=r,motor=s['motor'],captura=s['captura'],revision_humana='',revisor='',fecha='',resultado='',observaciones='') for s in scans for r in s['incompletos']]
if incomplete:csvfile('INSTRUMENTOS_HUMANOS/AXE_INCOMPLETE_REVISION.csv',incomplete)
statuses=[
 ('Fuentes e integridad CM','VERIFICADO',f'{sum(r["estado"]=="PASS" for r in validation)}/{len(validation)} registros originales coinciden; oficiales sin escritura','Nada para la verificación local','Codex ejecutado'),
 ('IP05 medición','VERIFICADO',f'{metric["numerador"]}/{metric["denominador"]} = {metric["IP05_porcentaje"]:.2f}%; NO cumple meta ≥90%','Consultar los casos, sin elevar el porcentaje con selección de éxitos','Codex ejecutado'),
 ('IP05 subsanación','PENDIENTE DE PERSONA','Dos sin adjuntos y uno pendiente de revisión','Evidencia auténtica/simulada válida que corresponda a los casos; decisión de operación/PO si exige cambiar reglas de atención','Operador responsable y Product Owner/delegado'),
 ('Accesibilidad automatizada','VERIFICADO',f'{len(scans)} scans WCAG A/AA sin violaciones detectadas','No equivale a revisión humana o certificación','Codex ejecutado'),
 ('Accesibilidad humana','PENDIENTE DE PERSONA',f'{len(incomplete)} entradas axe incomplete y revisión semántica pendiente','Persona revisa contexto, lector de pantalla/ayudas y tareas completas','Revisor accesibilidad del equipo'),
 ('Responsividad y motores','VERIFICADO','Chrome, Edge, Firefox, WebKit; 360/768/1366','Límites headless y emulación documentados','Codex ejecutado'),
 ('Safari/dispositivo físico','PENDIENTE DE ENTORNO/AUTORIZACIÓN','No ejecutado','Safari/iOS y dispositivos disponibles para prueba; después completar ficha','Equipo facilita entorno; persona designada prueba'),
 ('Seguridad y HTTPS local','VERIFICADO',f'{len(tls["checks"])}/{len(tls["checks"])} TLS PASS y permisos probados','No equivale a despliegue público','Codex ejecutado'),
 ('HTTPS de despliegue','PENDIENTE DE ENTORNO/AUTORIZACIÓN','Sin publicación','Entorno autorizado, DNS/certificado y configuración de despliegue','Administrador y responsable delegado'),
 ('Usabilidad RNF03','PENDIENTE DE PERSONA','Sin medición humana','Cinco personas y moderador; fichas vacías listas','Equipo 5 y participantes reales'),
 ('PPQA independiente','PENDIENTE DE PERSONA','Sin conformidad independiente','Revisor distinto del implementador completa checklist','Revisor designado por Equipo 5'),
 ('Matriz y APF2 oficial','PENDIENTE DE PERSONA','Propuestas preparadas; originales no editados','Validar e incorporar CSV y redactar APF2 formal sin inventar eventos','Responsable delegado/encargado documental'),
 ('GitHub y Drive','PENDIENTE DE ENTORNO/AUTORIZACIÓN','Estructuras locales listas; nada publicado','URL de repositorio, carpeta Drive y permisos/destinos autorizados explícitamente','Propietario del repositorio y carpeta compartida'),
]
csvfile('MATRICES/ESTADOS_FINALES.csv',[dict(aspecto=a,estado=b,resultado=c,que_se_necesita=d,quien_resuelve=e) for a,b,c,d,e in statuses])
write('REPORTES/INFORME_EJECUTIVO.md',f'''# Cierre técnico SEDAPAL para integración APF2

Corte {STAMP}. Base v0.1.1 conservada; incremento v0.1.2-SNAPSHOT, commit {CODE}. Fuentes: Maestro v1.9, Matriz v1.12, CM v0.1. No se sobrescribieron originales ni se publicó el proyecto.

Se corrigieron contraste, asociación/foco de errores y cabecera móvil Firefox; se retiraron credenciales predeterminadas y se añadió perfil TLS local. Pruebas nuevas: {len(junit)}/{len(junit)} Java, {len(checks)}/{len(checks)} checks de navegador en cuatro motores, {len(scans)} scans axe y {len(tls['checks'])}/{len(tls['checks'])} TLS. Hay {len(shots)} capturas auténticas nuevas. Los 46/41/24 anteriores se conservan como antecedentes, no se repitieron ni se suman como HU.

IP05: {metric['numerador']}/{metric['denominador']} = {metric['IP05_porcentaje']:.2f} %, debajo de 90 %. Se incluyeron todos los casos elegibles de la cohorte mixta fijada antes de medir, incluidos dos sin adjuntos y uno con publicación pendiente. Es un escenario sintético, no una estimación de producción. Sus accesos y archivos son verificables.

Ninguna HU se declara Done. Quedan personas para usabilidad, revisión independiente, accesibilidad semántica y ratificación/incorporación documental; entornos para Safari/dispositivos, HTTPS público y destinos autorizados de GitHub/Drive. Los instrumentos están vacíos y listos. ESTADOS_FINALES.csv especifica cada necesidad y responsable con los tres estados solicitados.
''')
write('REPORTES/REPORTE_CIERRE_TECNICO.md',f'''# Verificaciones técnicas de cierre y límites

{STAMP}. Código {CODE}. Responsable de ejecución Codex agente automatizado; revisión independiente no afirmada.

La línea base previa estaba controlada en 70b4b77 (contenido probado 55c805e). Se usó una nueva rama cierre-tecnico/2026-10-14. La batería focalizada tiene tres pruebas Java, incluida una medición IP05 que conserva los casos incompletos; los 46 Java previos no se volvieron a ejecutar. Los resultados anteriores permanecen identificados por versión. Git diff distingue los cambios nuevos.

La inspección inicial de v0.1.1 produjo cinco checks fallidos, por contraste de 4.21/4.29:1 y error de contraseña sin relación ARIA. Se corrigieron enlaces/avisos y errores de cliente/servidor con foco. La primera matriz amplió la evidencia y detectó overflow 5px en Firefox. Se corrigió la cabecera; además se ajustaron selectores ambiguos y espera del POST de upload del verificador. WebKit no requería relajar MIME ni validación de contenido; el diagnóstico conservado completó su flujo tras corregir la espera. La primera prueba TLS usó un token antiguo tras relogin; el producto lo denegó con 403. Se corrigió el verificador para obtener un token vigente; los intentos previos se conservan.

La matriz definitiva contiene {len(scans)} estados/pantalla/viewport, tres checks por scan (axe WCAG A/AA, overflow y etiquetas) y controles funcionales y de seguridad: {len(checks)} PASS. Los motores/versiones y cada scan raw están en 03_MATRIZ_CONSOLIDADA; se consolida la última ejecución completa por motor, conservando intentos anteriores. Chrome y Edge son instalaciones locales; Firefox y WebKit provienen de Playwright. No se llaman Safari físico ni dispositivos reales. Las animaciones se esperan antes de capturar.

RNF-09 no se certifica íntegramente con axe: {len(incomplete)} entradas incomplete conservadas para revisión humana, además de semántica y ayudas técnicas. No hay participantes ni firmas. Permisos, titularidad, CSRF, adjunto restringido/publicado, ajeno y logout fueron comprobados por motor. La prueba Java verificó hash BCrypt y que la contraseña demo anterior ya no coincide. Las credenciales de ejecución se suministraron por entorno y no están en la entrega.

TLS: certificado efímero SAN localhost/127.0.0.1, confianza explícita y rechazo de confianza predeterminada, autenticación, cookie Secure/HttpOnly/SameSite=Lax, HSTS y logout. Solo loopback; no se alteró confianza del sistema. Se entrega únicamente el certificado público; claves privadas, bases H2, target y .env quedan excluidos. No se probaron DNS, proxy ni HTTPS de un despliegue público.

IP05 y su límite: MEDICIONES/IP05_CASOS.csv y JSON incluyen estado, fecha, archivos, hashes y HTTP por rol. La cohorte mezcla casos positivos y negativos prefijados; todos los elegibles entran en denominador. El valor {metric['IP05_porcentaje']:.2f}% NO satisface meta. No se cambia una regla aprobada de atención ni se inventa un documento para elevarlo. Operación/PO debe completar/revisar evidencias y decidir si hace falta un cambio autorizado de flujo.

Cada HU tiene nueve condiciones reevaluadas en HU_DOD_72_CONDICIONES.csv, con CA, RF, RNF, pruebas, evidencias, NC y resultado NO DONE. La aprobación con observaciones del incremento anterior no se extrapola al nuevo. La matriz oficial v1.12 permanece sin cambios; las propuestas están separadas. No se inventan Sprints, horas, presupuestos, reuniones, HC ni proveedores.

Referencias primarias de metodología: [Playwright](https://playwright.dev/docs/accessibility-testing), [API axe-core](https://www.deque.com/axe/core-documentation/api-documentation/). Son referencias de herramientas, no evidencia de aprobación humana.
''')
write('CM/REGISTRO_CM_CIERRE.md',f'''# Configuración y preservación

Base {BASE}; nuevo código {CODE}; versión 0.1.2-SNAPSHOT. Maestro v1.9, matriz v1.12 y CM v0.1 en solo lectura. El manifiesto CM original fue comprobado: {sum(r['estado']=='PASS' for r in validation)}/{len(validation)} entradas PASS. Cualquier FAIL queda en VALIDACION_CM_ORIGINAL.csv y no se oculta.

Se conservan commits previos y nuevos, exportación de fuente, bundle recuperable y manifiesto SHA-256. La rama y las fechas son reales. GitHub y Drive no se usaron porque no hay destino ni autorización explícita; se entregan estructuras e instrucciones offline. Las credenciales reales, claves privadas y BD no forman parte de la entrega. Datos de QA sintéticos e historia de pruebas anterior se distinguen de secretos privados.
''')
with (OUT/'REPORTES/INFORME_EJECUTIVO.md').open('a',encoding='utf-8') as f:
 f.write('''
| Estado final | Alcance y resultado | Qué se necesita y quién lo resuelve |
|---|---|---|
| VERIFICADO | Compilación y pruebas focalizadas; axe, cuatro motores, permisos, TLS local e IP05 medido. IP05 no cumple la meta. | Evidencias reales disponibles para inspección. |
| PENDIENTE DE PERSONA | Usabilidad, semántica/accesibilidad humana, PPQA independiente, expedientes incompletos y actualización oficial. | Cinco participantes y moderador; revisor independiente; operador/PO y responsable documental del Equipo 5. |
| PENDIENTE DE ENTORNO/AUTORIZACIÓN | Safari/dispositivos físicos, HTTPS desplegado y publicación GitHub/Drive. | Entornos y destinos con permisos explícitos, suministrados por sus propietarios/administradores. |
''')
write('CM/PUBLICACION_PENDIENTE_GITHUB_DRIVE.md','''# Preparación para plataformas del equipo

Estado PENDIENTE DE ENTORNO/AUTORIZACIÓN. No se crearon remotos, repositorios ni carpetas compartidas ni se enviaron mensajes.

GitHub: el propietario indica URL de repositorio y autoriza expresamente el destino/visibilidad. Recuperar HISTORIAL_GIT_CIERRE.bundle mediante git clone; inspeccionar rama, .gitignore, .env.example y workflow Java 21 preparado. Excluir target, claves, bases y datos reales; revisar el historial, que contiene datos y contraseñas históricas sintéticas de prueba. Configurar remoto únicamente con la URL autorizada y publicar la rama tras autorización. CI es una plantilla, no una ejecución remota verificada.

Drive: el propietario indica carpeta destino y permisos. ESTRUCTURA_DRIVE contiene copias organizadas por 00–08; subir solo tras autorización y comparar manifiesto SHA-256. Registrar URL, responsable, fecha real y permisos aplicados; no completar esos campos antes. Mantener documentos oficiales vigentes separados de propuestas de cierre.
''')
write('REPORTES/PROPUESTA_INTEGRACION_APF2.md',f'''# Insumos derivados para el Maestro v1.9

No reemplaza ni modifica el Word oficial. Propuesta para revisión del responsable y posterior integración por ChatGPT.

§17.3 y §24.5 MA: incorporar IP05 {metric['numerador']}/{metric['denominador']}={metric['IP05_porcentaje']:.2f}%, meta no alcanzada; cohorte sintética mixta, casos incompletos y motivos. Distinguir medición correcta de cumplimiento de meta. IP04 permanece sin medición humana.

§18.8 PPQA: incorporar defectos corregidos nuevos, ampliación automatizada de RNF-09 y revisión independiente pendiente. Conservar intentos fallidos y observaciones, sin atribuir aprobación independiente.

§19/§20 CM y áreas: identificar v0.1.2, commits reales y SHA-256; consignar estructuras GitHub/Drive preparadas pero no publicadas. Mantener aprobación anterior con observaciones vinculada a v0.1.1.

Seguimiento y cierre: adjuntar matriz de 72 condiciones DoD, ocho HU NO DONE, cinco fichas vacías de usabilidad, checklist independiente y guía Safari. No inventar reuniones, fechas de Sprint ni productividad. Incorporar propuestas CSV a matriz v1.12 solo después de aprobación del responsable.
''')
# Drive-ready document structure; no connector or external writes.
drive=OUT/'ESTRUCTURA_DRIVE'
mapping={'00_Administracion_y_Aprobaciones':'CM','02_Requisitos_RF_RNF_HU':'MATRICES','04_Pruebas_y_Evidencias':'REPORTES','05_QA_y_No_Conformidades':'PPQA','06_MA_y_Mediciones':'MEDICIONES','07_Informes_APF2_Final':'REPORTES'}
for target,source in mapping.items():shutil.copytree(OUT/source,drive/target,dirs_exist_ok=True,copy_function=copy_long)
shutil.copytree(OUT/'INSTRUMENTOS_HUMANOS',drive/'05_QA_y_No_Conformidades/INSTRUMENTOS_HUMANOS',dirs_exist_ok=True,copy_function=copy_long)
for name in ['03_MATRIZ_CONSOLIDADA','02_JAVA_FINAL','04_TLS_FINAL']:
 shutil.copytree(EV/name,drive/'04_Pruebas_y_Evidencias/EVIDENCIAS_NUEVAS'/name,dirs_exist_ok=True,copy_function=copy_long)
for name in ['SEDAPAL_Borrador_Maestro_Formatos_Docente_v1.9_2026-10-14.docx','SEDAPAL_Matriz_Maestra_Trazabilidad_v1.12_2026-10-14.xlsx']:
 shutil.copy2(DL/name,drive/('07_Informes_APF2_Final' if name.endswith('.docx') else '02_Requisitos_RF_RNF_HU')/name)
(drive/'01_Planificacion').mkdir(exist_ok=True);(drive/'03_Codigo_Git_y_Baselines').mkdir(exist_ok=True);(drive/'08_SAM_Adquisiciones').mkdir(exist_ok=True)
(drive/'08_SAM_Adquisiciones/ESTADO.md').write_text('Aplicabilidad conservada de CM v0.1: sin proveedor o contrato acreditados; no se inventa contratación.\n',encoding='utf-8')
write('LEEME_ENTREGA.md','''# Entrega de cierre técnico para ChatGPT

Comenzar por REPORTES/INFORME_EJECUTIVO.md y MATRICES/ESTADOS_FINALES.csv. La medición IP05 es 50% y NO cumple meta de 90%; revisar los seis casos y no asumir que PASS técnico significa completitud del expediente. Ninguna HU Done.

EVIDENCIAS_NUEVAS contiene ejecuciones de esta revisión, incluidos intentos anteriores identificados. ANTECEDENTES_v0.1.1 conserva resultados anteriores sin atribuirlos a esta ejecución. FUENTES_OFICIALES_SOLO_LECTURA son copias idénticas; propuestas aparte. INSTRUMENTOS_HUMANOS tiene formularios sin personas, firmas, fechas ni resultados.

El repositorio y bundle conservan historial; .github y configuración local se prepararon sin publicación. ESTRUCTURA_DRIVE está lista para un destino autorizado. No se entrega clave privada, .env ni base H2. Versión/commits, fuente exportada y manifiestos permiten verificar integridad y procedencia.
''')
print(json.dumps(dict(fecha=STAMP,codigo=CODE,junit=len(junit),checks=len(checks),scans=len(scans),capturas=len(shots),tls=len(tls['checks']),ip05=metric['IP05_porcentaje'],cm_pass=sum(r['estado']=='PASS' for r in validation),cm_total=len(validation),axe_incomplete=len(incomplete)),ensure_ascii=False))
