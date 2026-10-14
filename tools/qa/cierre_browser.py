"""Pruebas nuevas de cierre: axe, viewports, teclado y protección. Datos ficticios."""
from pathlib import Path
import sys,os,argparse,subprocess,time,socket,json,hashlib,re,traceback
from datetime import datetime,timezone,timedelta
ROOT=Path(__file__).resolve().parents[2]
sys.path.insert(0,str(ROOT.parent/'.qa-python'))
os.environ['PLAYWRIGHT_BROWSERS_PATH']=str(ROOT/'target'/'qa-browsers')
from playwright.sync_api import sync_playwright
from reportlab.pdfgen import canvas
P=argparse.ArgumentParser();P.add_argument('--jar',required=True);P.add_argument('--out',required=True);P.add_argument('--baseline',action='store_true');P.add_argument('--engines',default='chrome,edge,firefox,webkit');A=P.parse_args()
OUT=Path(A.out).resolve();OUT.mkdir(parents=True,exist_ok=True)
PASSWORD=os.environ.get('QA_DEMO_PASSWORD','1234');VERSION=subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT,text=True).strip()
ROWS=[];SCANS=[];ENGINES=[]
def now():return datetime.now(timezone(timedelta(hours=-5))).isoformat()
def save():
 (OUT/'checks.json').write_text(json.dumps(ROWS,ensure_ascii=False,indent=2),encoding='utf-8')
 (OUT/'scans-indice.json').write_text(json.dumps(SCANS,ensure_ascii=False,indent=2),encoding='utf-8')
 (OUT/'motores.json').write_text(json.dumps(ENGINES,ensure_ascii=False,indent=2),encoding='utf-8')
def check(id,refs,expected,fn,engine):
 try:
  value=fn();assert value is not False;state='PASS'
 except Exception as e:value=f'{type(e).__name__}: {e}';state='FAIL'
 ROWS.append(dict(id=id,motor=engine,referencias=refs,fecha=now(),commit=VERSION,responsable='Codex agente automatizado',esperado=expected,obtenido=value,estado=state));save();print(id,engine,state,str(value)[:120],flush=True)
def status(r,n):assert r.status==n,f'HTTP {r.status}, esperado {n}';return {'http':r.status}
def start(engine):
 s=socket.socket();s.bind(('127.0.0.1',0));port=s.getsockname()[1];s.close()
 log=open(OUT/f'servidor-{engine}.log','wb');env=os.environ.copy();env['DEMO_PASSWORD']=PASSWORD
 args=['C:/Program Files/Java/jdk-21/bin/java.exe','-jar',str(Path(A.jar).resolve()),'--spring.profiles.active=demo',f'--server.port={port}','--server.address=127.0.0.1',f'--spring.datasource.url=jdbc:h2:mem:cierre{engine};MODE=MySQL;DB_CLOSE_DELAY=-1','--spring.h2.console.enabled=false',f'--app.upload-dir={ROOT}/target/cierre-browser-{engine}/uploads']
 proc=subprocess.Popen(args,cwd=ROOT,env=env,stdout=log,stderr=subprocess.STDOUT,creationflags=subprocess.CREATE_NO_WINDOW)
 (OUT/f'entorno-{engine}.json').write_text(json.dumps(dict(fecha=now(),pid=proc.pid,args=args,jar_sha256=hashlib.sha256(Path(A.jar).read_bytes()).hexdigest(),commit=VERSION,datos='SIMULADOS',credenciales='Suministradas por entorno; no registradas'),indent=2),encoding='utf-8')
 return proc,log,f'http://127.0.0.1:{port}'
def login(p,b,doc='12345678',password=None):
 p.goto(b+'/login');p.locator('#documento').fill(doc);p.locator('#password').fill(password or PASSWORD);p.locator('button[type=submit]').click()
 expected={'22222222':'/operador','33333333':'/tecnico','44444444':'/evaluador','55555555':'/supervisor','66666666':'/calidad/reportes','99999999':'/admin'}.get(doc,'/usuario')
 p.wait_for_url(b+expected,timeout=5000)
def ready(p,b):
 for _ in range(100):
  try:
   login(p,b)
   if p.url.endswith('/usuario') and 'Asignado' in p.inner_text('body'):return
  except Exception:pass
  time.sleep(.5)
 raise RuntimeError('No terminó la carga demo')
def token(p):return p.locator('input[name=_csrf]').first.get_attribute('value')
def post(p,url,**data):return p.context.request.post(url,form={'_csrf':token(p),**data},max_redirects=0)
def scan(p,name,engine,widths=None):
 for width,height in (widths or [(360,800),(768,1024),(1366,900)]):
  p.set_viewport_size({'width':width,'height':height});p.wait_for_function("() => !document.querySelector('.collapsing')");p.wait_for_timeout(80)
  id=f'{engine}-{name}-{width}';p.screenshot(path=str(OUT/(id+'.png')),full_page=True)
  p.add_script_tag(path=str(ROOT/'tools/qa/axe.min.js'))
  result=p.evaluate("async()=>await axe.run(document,{runOnly:{type:'tag',values:['wcag2a','wcag2aa','wcag21a','wcag21aa']}})")
  (OUT/(id+'-axe.json')).write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
  dims=p.evaluate('({viewport:innerWidth,document:document.documentElement.scrollWidth})')
  labelled=p.evaluate("() => [...document.querySelectorAll('input:not([type=hidden]):not([type=submit]),select,textarea')].filter(e=>e.getClientRects().length && e.type!=='checkbox').every(e=>e.labels.length||e.getAttribute('aria-label')||e.getAttribute('aria-labelledby'))")
  SCANS.append(dict(id=id,motor=engine,pantalla=name,ancho=width,fecha=now(),commit=VERSION,violaciones=[dict(id=v['id'],impacto=v['impact'],nodos=len(v['nodes'])) for v in result['violations']],incompletos=[v['id'] for v in result['incomplete']],scroll=dims,controles_etiquetados=labelled,captura=id+'.png',axe=id+'-axe.json'))
  check(id+'-axe','RNF-09','Sin violaciones automáticas WCAG A/AA en el estado visible',lambda result=result:len(result['violations'])==0,engine)
  check(id+'-responsive','RNF-01','Sin desbordamiento horizontal de documento',lambda dims=dims:dims['document']<=dims['viewport'],engine)
  check(id+'-labels','RNF-09','Controles visibles etiquetados',lambda labelled=labelled:labelled,engine)
def reach(p,selector):
 for _ in range(50):
  if p.locator(selector).evaluate('(e)=>document.activeElement===e'):return
  p.keyboard.press('Tab')
 raise AssertionError('No se alcanzó por Tab: '+selector)
def execute(browser,engine,b):
 c=browser.new_context();p=c.new_page();ready(p,b)
 p.goto(b+'/login');scan(p,'autenticacion',engine,[(360,800),(1366,900)] if A.baseline else None)
 p.goto(b+'/registro');scan(p,'autorregistro',engine,[(360,800)] if A.baseline else None)
 p.locator('#nombre').fill('Ciudadano ficticio QA');p.locator('#documento').fill('qaError'+engine);p.locator('#password').fill('corto');p.locator('form').evaluate('(e)=>e.noValidate=true');p.get_by_role('button',name='Crear cuenta').click()
 scan(p,'error-registro',engine,[(360,800)] if A.baseline else None)
 check('ERROR-ARIA','RNF-09','Error vinculado a contraseña con aria-invalid y aria-describedby',lambda:p.locator('#password').get_attribute('aria-invalid')=='true' and bool(p.locator('#password').get_attribute('aria-describedby')),engine)
 login(p,b);p.goto(b+'/usuario');scan(p,'notificaciones',engine,[(360,800),(1366,900)] if A.baseline else None)
 p.goto(b+'/usuario/reclamos/nuevo');scan(p,'registro-incidencia',engine,[(360,800)] if A.baseline else None)
 p.goto(b+'/usuario/consultar?codigo=REC-2026-0001');scan(p,'seguimiento-sin-evidencia',engine,[(360,800)] if A.baseline else None)
 if A.baseline:c.close();return
 tc=browser.new_context();t=tc.new_page();login(t,b,'33333333');t.locator('.accordion-button').first.click();scan(t,'atencion-tecnica',engine)
 form=t.locator('form').filter(has=t.locator('input[type=file]')).first
 fixture=OUT/f'evidencia-simulada-{engine}.pdf';cv=canvas.Canvas(str(fixture));cv.drawString(70,750,'EVIDENCIA SIMULADA PARA PRUEBA QA - NO CASO REAL');cv.save()
 form.locator('[name=accionesRealizadas]').fill('Atención ficticia para verificar evidencia y accesibilidad.');form.locator('[name=resultado]').fill('Resultado simulado de la verificación automatizada.');form.locator('input[type=file]').set_input_files(str(fixture));mime=form.locator('input[type=file]').evaluate('(e)=>e.files[0].type')
 with t.expect_response(lambda r:'/resolver' in r.url and r.request.method=='POST') as uploaded:
  form.locator('button[type=submit]').click()
 t.wait_for_load_state();t.screenshot(path=str(OUT/f'{engine}-resultado-carga.png'),full_page=True)
 (OUT/f'{engine}-resultado-carga.json').write_text(json.dumps({'http':uploaded.value.status,'texto':t.inner_text('body'),'tipo_declarado_por_navegador':mime},ensure_ascii=False,indent=2),encoding='utf-8')
 oc=browser.new_context();op=oc.new_page();login(op,b,'22222222');op.goto(b+'/operador/evidencias?reclamoId=2');scan(op,'revision-evidencia',engine)
 href=op.locator('a[href^="/evidencias/"]').first.get_attribute('href')
 p.goto(b+'/usuario/consultar?codigo=REC-2026-0002');check('ADJUNTO-OCULTO','RNF-05;RF-05','Titular recibe 403 antes de revisión',lambda:status(p.context.request.get(b+href),403),engine)
 op.get_by_role('button',name='He revisado y apruebo su consulta ciudadana').click();p.goto(b+'/usuario/consultar?codigo=REC-2026-0002');scan(p,'atendido-evidencia',engine)
 check('ADJUNTO-TITULAR','RNF-05;RNF-07','Bytes del PDF coinciden al descargar como titular',lambda:p.context.request.get(b+href).body()==fixture.read_bytes(),engine)
 ac=browser.new_context();ap=ac.new_page();login(ap,b,'87654321');ap.goto(b+'/usuario/consultar?codigo=REC-2026-0002');scan(ap,'consulta-ajena-error',engine)
 check('ADJUNTO-AJENO','RNF-05;RNF-07','Ajeno recibe 403',lambda:status(ap.context.request.get(b+href),403),engine)
 for action,data in [('cerrar',{'confirmar':'true'}),('reapertura',{'motivo':'Motivo ficticio ajeno de la prueba.'})]:
  check('AJENO-'+action,'RNF-05','POST ajeno devuelve 403',lambda action=action,data=data:status(post(ap,b+'/usuario/reclamos/2/'+action,**data),403),engine)
 check('CSRF','RNF-05','POST sin CSRF denegado',lambda:status(p.context.request.post(b+'/usuario/reclamos/2/cerrar',form={'confirmar':'true'},max_redirects=0),403),engine)
 public=browser.new_context();pub=public.new_page();pub.goto(b+'/consulta?codigo=REC-2026-0002');scan(pub,'consulta-publica',engine)
 check('PRIVACIDAD-PUBLICA','RNF-07','Consulta pública no contiene dirección, documento ni adjuntos',lambda:all(x not in pub.content() for x in ['Jr. Los Jardines','12345678','/evidencias/']),engine)
 p.goto(b+'/usuario/consultar?codigo=REC-2026-0002');reach(p,'input[name=confirmar]');p.keyboard.press('Space');p.keyboard.press('Tab');p.keyboard.press('Enter');p.wait_for_url('**/usuario');p.goto(b+'/usuario/consultar?codigo=REC-2026-0002');scan(p,'cerrado-reapertura-form',engine)
 reach(p,'#motivo');p.keyboard.insert_text('El problema simulado persiste después de la atención de QA.');p.keyboard.press('Tab')
 focus=p.evaluate('({tag:document.activeElement.tagName,outline:getComputedStyle(document.activeElement).outlineStyle})')
 check('FOCO-TECLADO','RNF-09','Botón de reapertura alcanzable con foco visible',lambda:focus['tag']=='BUTTON' and focus['outline']!='none',engine)
 p.keyboard.press('Enter');p.wait_for_url('**/usuario');p.goto(b+'/usuario/consultar?codigo=REC-2026-0002');scan(p,'reapertura-solicitada',engine)
 check('FLUJO-TECLADO','RNF-02;RNF-09','Consulta/cierre/reapertura con teclado, nuevo estado visible',lambda:'Reapertura solicitada' in p.inner_text('body'),engine)
 # One new account per motor confirms ownership, separate from the IP05 cohort.
 pub.goto(b+'/registro');pub.locator('#nombre').fill('Cuenta simulada '+engine);pub.locator('#documento').fill('qaCierre'+engine);pub.locator('#password').fill(PASSWORD);pub.get_by_role('button',name='Crear cuenta').click();pub.wait_for_url(b+'/login');login(pub,b,'qaCierre'+engine)
 check('CUENTA-PROPIA','RNF-05;RF-10','Cuenta nueva no hereda incidencias',lambda:'REC-2026-0001' not in pub.inner_text('body'),engine)
 pub.goto(b+'/usuario/reclamos/nuevo');pub.locator('#direccion').fill('Ubicación ficticia QA 100');pub.locator('#distrito').fill('Lima');pub.locator('#tipoIncidencia').select_option('Fuga de agua');pub.locator('#prioridad').select_option('Alta');pub.locator('#descripcion').fill('Incidencia simulada para comprobar asociación al titular.');pub.get_by_role('button',name='Registrar y generar código').click();pub.wait_for_load_state()
 code=pub.locator('#codigo-reclamo').inner_text();check('VINCULO-TITULAR','RNF-05;RF-01','Incidencia registrada en cuenta nueva',lambda:code.startswith('REC-'),engine)
 p.goto(b+'/usuario/consultar?codigo='+code);check('INCIDENCIA-AJENA','RNF-05','Cuenta anterior no puede ver incidencia nueva',lambda:p.locator('#codigo-reclamo').count()==0,engine)
 post(pub,b+'/logout');pub.goto(b+'/usuario');check('LOGOUT','RNF-05;RNF-07','Recurso privado exige login tras cierre de sesión',lambda:'/login' in pub.url,engine)
 for context in [c,tc,oc,ac,public]:context.close()
with sync_playwright() as pw:
 for engine in A.engines.split(','):
  proc=log=browser=None
  try:
   if engine in ['chrome','edge']:
    exe='C:/Program Files/Google/Chrome/Application/chrome.exe' if engine=='chrome' else 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe';browser=pw.chromium.launch(executable_path=exe,headless=True)
   else:browser=getattr(pw,engine).launch(headless=True)
   ENGINES.append(dict(motor=engine,version=browser.version,estado='EJECUTADO',plataforma='Windows; headless; viewport emulado',es_safari_fisico=False));save()
   proc,log,base=start(engine);execute(browser,engine,base)
  except Exception:
   (OUT/f'error-{engine}.txt').write_text(traceback.format_exc(),encoding='utf-8');ENGINES.append(dict(motor=engine,estado='BLOQUEADO/FALLO',razon=traceback.format_exc()));save();print('ERROR',engine,traceback.format_exc(),flush=True)
  finally:
   if browser:browser.close()
   if proc:
    proc.terminate()
    try:proc.wait(timeout=8)
    except subprocess.TimeoutExpired:proc.kill();proc.wait()
    log.close()
save();print(json.dumps(dict(checks=len(ROWS),pass_count=sum(r['estado']=='PASS' for r in ROWS),fail_count=sum(r['estado']=='FAIL' for r in ROWS),scans=len(SCANS))),flush=True)
