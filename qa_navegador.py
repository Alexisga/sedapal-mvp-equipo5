"""QA reproducible local; Python + Playwright, Chrome y Edge instalados. Sin datos reales."""
import sys, os, json, time, socket, subprocess, re, traceback, hashlib
from pathlib import Path
from datetime import datetime, timezone, timedelta
sys.path.insert(0,str(Path(__file__).parent.parent/'.qa-python'))
from playwright.sync_api import sync_playwright

ROOT=Path(__file__).resolve().parent
OUT=Path(os.environ.get('SEDAPAL_ENTREGA_DIR',str(ROOT/'ENTREGA_PARA_APROBACION')))/'EVIDENCIAS'
OUT.mkdir(parents=True,exist_ok=True)
ROWS=[]
VERSION=subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT,text=True).strip()
def now():return datetime.now(timezone(timedelta(hours=-5))).isoformat()
def check(id,refs,expected,fn,steps='Ejecutar qa_navegador.py',version=None):
 start=time.perf_counter()
 try:
  actual=fn();ok=actual is not False;state='PASS' if ok else 'FAIL'
 except Exception as e:actual=f'{type(e).__name__}: {e}';state='FAIL'
 ROWS.append(dict(id=id,referencias=refs,fecha=now(),version=version or VERSION,responsable='Codex agente automatizado',esperado=expected,obtenido=actual,estado=state,duracion_s=round(time.perf_counter()-start,4),pasos=steps,evidencia='EVIDENCIAS/navegador-resultados.json'))
 (OUT/'navegador-resultados.json').write_text(json.dumps(ROWS,ensure_ascii=False,indent=2),encoding='utf-8')
 print(id,state,str(actual)[:100],flush=True)
 return actual

def app(jar,label):
 sock=socket.socket();sock.bind(('127.0.0.1',0));port=sock.getsockname()[1];sock.close()
 log=open(OUT/f'servidor-{label}.log','wb');work=ROOT/'target'/f'qa-{label}';work.mkdir(parents=True,exist_ok=True)
 args=['C:/Program Files/Java/jdk-21/bin/java.exe','-jar',str(jar),'--spring.profiles.active=demo',f'--server.port={port}','--server.address=127.0.0.1','--spring.datasource.url=jdbc:h2:mem:qa'+label+';MODE=MySQL;DB_CLOSE_DELAY=-1','--spring.h2.console.enabled=false',f'--app.upload-dir={work}/uploads']
 proc=subprocess.Popen(args,cwd=work,stdout=log,stderr=subprocess.STDOUT,creationflags=subprocess.CREATE_NO_WINDOW)
 (OUT/f'entorno-{label}.json').write_text(json.dumps(dict(fecha=now(),pid=proc.pid,args=args,jar_sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),version=VERSION if label=='final' else '41070eb'),indent=2),encoding='utf-8')
 return proc,log,f'http://127.0.0.1:{port}'
def stop(proc,log):
 proc.terminate()
 try:proc.wait(timeout=10)
 except subprocess.TimeoutExpired:proc.kill();proc.wait()
 log.close()
def login(page,base,doc='12345678',password='1234'):
 page.goto(base+'/login');page.locator('#documento').fill(doc);page.locator('#password').fill(password);page.locator('button[type=submit]').click();page.wait_for_load_state();return page
def ready(page,base):
 for _ in range(90):
  try:
   login(page,base)
   if page.url.endswith('/usuario') and 'REC-2026-0002' in page.inner_text('body') and 'Asignado' in page.inner_text('body'):return
  except Exception:pass
  time.sleep(.5)
 raise RuntimeError('La app no inició con datos demo')
def token(page):return page.locator('input[name=_csrf]').first.get_attribute('value')
def status_is(response,expected):
 assert response.status==expected,f'HTTP obtenido={response.status}; esperado={expected}'
 return {'http':response.status}
def submit(page,url,data):
 data={'_csrf':token(page),**data};return page.context.request.post(url,form=data,max_redirects=0)
def snapshot(page,name):
 path=OUT/(name+'.png');page.screenshot(path=str(path),full_page=True);return str(path.relative_to(OUT.parent))
def own(page,base,code='REC-2026-0002'):page.goto(base+'/usuario/consultar?codigo='+code)
def run():
 with sync_playwright() as pw:
  chrome=pw.chromium.launch(executable_path='C:/Program Files/Google/Chrome/Application/chrome.exe',headless=True)
  # Repeat the supplied 8-check HU-02 battery and reconstruct 9 HTTP checks explicitly.
  proc,log,base=app(Path('C:/Users/User/Downloads/SEDAPAL_MVP_Demo_Java21_v0.1.0_2026-10-14.jar'),'baseline')
  try:
   ctx=chrome.new_context();page=ctx.new_page();ready(page,base);own(page,base)
   for i,(name,fn) in enumerate([
    ('HTTP 200',lambda:page.context.request.get(page.url).status==200),('Código titular',lambda:'REC-2026-0002' in page.inner_text('body')),
    ('Estado actual',lambda:'Estado actual' in page.inner_text('body')),('Última actualización',lambda:'Última actualización registrada' in page.inner_text('body')),
    ('Fecha y hora',lambda:bool(re.search(r'\d{2}/\d{2}/\d{4} \d{2}:\d{2}',page.inner_text('body')))),
    ('Bloques responsivos',lambda:page.locator('.col-12.col-md-6').count()>=2),('Historial',lambda:'Historial verificable' in page.inner_text('body'))],1):
    check(f'BASE-HU02-{i:02}','HU-02',name,fn,version='41070eb')
   login(page,base,'87654321');own(page,base)
   check('BASE-HU02-08','HU-02;RNF-05','Ajeno sin resumen',lambda:'No se encontró un reclamo propio' in page.inner_text('body') and page.locator('#codigo-reclamo').count()==0,version='41070eb')
   login(page,base);page.goto(base+'/usuario/reclamos/nuevo')
   checks=[('Formulario',lambda:page.locator('#descripcion').count()==1),('CSRF',lambda:status_is(submit(page,base+'/usuario/reclamos',{}),200)),('Sin CSRF',lambda:status_is(page.context.request.post(base+'/usuario/reclamos',form={}),403)),('Rol ajeno',lambda:status_is(page.context.request.get(base+'/admin'),403))]
   for i,(name,fn) in enumerate(checks,1):check(f'BASE-HTTP-{i:02}','HU-01;RNF-05',name,fn,version='41070eb')
   data=dict(codigoSuministro='SUM-1001',tipoIncidencia='Fuga de agua',prioridad='Alta',descripcion='Prueba ficticia HTTP sin datos personales.',usuarioId='2')
   r=submit(page,base+'/usuario/reclamos',data);location=r.headers.get('location','')
   check('BASE-HTTP-05','HU-01','Alta con redirección',lambda:r.status==302,version='41070eb')
   code=re.search(r'REC-\d{4}-\d+',location).group()
   check('BASE-HTTP-06','HU-01','Código único generado',lambda:code not in ('REC-2026-0001','REC-2026-0002','REC-2026-0003'),version='41070eb')
   own(page,base,code);check('BASE-HTTP-07','HU-01','Caso vinculado al titular autenticado',lambda:page.locator('#codigo-reclamo').inner_text()==code,version='41070eb')
   login(page,base,'87654321');own(page,base,code);check('BASE-HTTP-08','HU-01','Suplantación ignorada',lambda:page.locator('#codigo-reclamo').count()==0,version='41070eb')
   login(page,base,password='incorrecta');check('BASE-HTTP-09','HU-08','Login incorrecto genérico',lambda:'Documento o contraseña incorrectos' in page.inner_text('body'),version='41070eb')
   snapshot(page,'baseline-login');ctx.close()
  finally:stop(proc,log)
  proc,log,base=app(ROOT/'target'/'sedapal-reclamos-0.1.1-SNAPSHOT.jar','final')
  try:
   ctx=chrome.new_context(viewport={'width':1366,'height':900});page=ctx.new_page();ready(page,base)
   check('SECURITY-CSRF-01','RNF-05','POST sin CSRF devuelve HTTP 403',lambda:status_is(page.context.request.post(base+'/usuario/reclamos',form={},max_redirects=0),403))
   # Public tracking cannot leak addresses or identity.
   pub=chrome.new_context();pp=pub.new_page();pp.goto(base+'/consulta?codigo=REC-2026-0002')
   check('WEB-01','HU-02/CA-01,02,03,04;RF-03;RF-04','Seguimiento público sin datos privados',lambda:'Asignado' in pp.inner_text('body') and 'Jr. Los Jardines' not in pp.inner_text('body') and '12345678' not in pp.inner_text('body'))
   pp.goto(base+'/consulta?codigo=INVALIDO');check('WEB-02','HU-02/CA-05','Mensaje para código inexistente',lambda:'No se encontró' in pp.inner_text('body'))
   pp.goto(base+'/registro');snapshot(pp,'registro-desktop')
   pp.locator('#nombre').fill('Ciudadano Ficticio QA');pp.locator('#documento').fill('qaCiudadano');pp.locator('#password').fill('ClaveQA123');pp.get_by_role('button',name='Crear cuenta').click()
   check('WEB-03','HU-08/CA-01','Alta confirmada sin contraseña',lambda:'Cuenta creada' in pp.inner_text('body') and 'ClaveQA123' not in pp.content())
   pp.goto(base+'/registro');r=submit(pp,base+'/registro',dict(nombre='QA',documento='qaCiudadano',password='ClaveQA123'))
   check('WEB-04','HU-08/CA-02','Duplicado rechazado',lambda:r.status==200 and 'ya está registrado' in r.text())
   login(pp,base,'qaCiudadano','ClaveQA123');check('WEB-05','HU-08/CA-03,04','Login correcto y listado propio vacío',lambda:pp.url.endswith('/usuario') and 'REC-2026-0001' not in pp.inner_text('body'))
   pp.goto(base+'/usuario/reclamos/nuevo');r=submit(pp,base+'/usuario/reclamos',dict(tipoIncidencia='Fuga de agua',prioridad='Alta',descripcion='Incidencia ficticia para prueba de cuenta nueva.',direccion='Ubicación simulada QA 100',distrito='Lima',usuarioId='1'))
   newloc=r.headers.get('location','');check('WEB-06','HU-01/CA-01,02,03,04,05,06;HU-08/CA-04','Alta propia y código visible',lambda:r.status==302 and 'REC-' in newloc)
   newcode=re.search(r'REC-\d{4}-\d+',newloc).group();own(pp,base,newcode);snapshot(pp,'cuenta-nueva-incidencia')
   r=submit(pp,base+'/logout',{});pp.goto(base+'/usuario');check('WEB-07','HU-08/CA-06','Logout protege sesión anterior',lambda:'/login' in pp.url)
   login(pp,base,'qaCiudadano','ClaveQA123');check('WEB-08','HU-08/CA-07','Incidencia persiste tras relogin',lambda:newcode in pp.inner_text('body'))
   own(pp,base,'REC-2026-0003');check('WEB-09','HU-08/CA-05;RNF-05','Consulta ajena denegada',lambda:'No se encontró un reclamo propio' in pp.inner_text('body'))
   # Technical evidence and operator review use real PDF fixture.
   tech=chrome.new_context();tp=tech.new_page();login(tp,base,'33333333');tp.locator('.accordion-button').first.click();snapshot(tp,'tecnico-antes')
   for ident,filename,mime,buffer,expected in [
    ('UPLOAD-01','qa.exe','application/octet-stream',b'contenido ficticio',302),
    ('UPLOAD-02','qa.png','image/png',b'%PDF-1.4\ncontenido falso\n%%EOF',302),
    ('UPLOAD-03','qa.pdf','application/pdf',b'%PDF-1.4\n'+b'0'*(5*1024*1024)+b'%%EOF',413)]:
    r=tp.context.request.post(base+'/tecnico/ordenes/1/resolver',multipart={'_csrf':token(tp),'accionesRealizadas':'Acciones ficticias de QA para validar archivos.','resultado':'Resultado ficticio QA sin guardar archivo inválido.','evidencia':{'name':filename,'mimeType':mime,'buffer':buffer}},max_redirects=0)
    tp.goto(base+'/tecnico')
    message=tp.inner_text('body') if expected==302 else r.text()
    check(ident,'HU-07/CA-02;RNF-06','Servidor rechaza archivo indebido o excedido con mensaje controlado',lambda r=r,expected=expected,message=message:r.status==expected and ('evidencia' in message.lower() or 'extensión' in message.lower()))
   tp.locator('.accordion-button').first.click()
   form=tp.locator('form').filter(has=tp.locator('input[type=file]')).first
   fixture=OUT/'evidencia-ficticia.pdf'
   from reportlab.pdfgen import canvas
   cv=canvas.Canvas(str(fixture));cv.drawString(70,750,'EVIDENCIA FICTICIA QA SEDAPAL - SIN DATOS REALES');cv.save()
   form.locator('[name=accionesRealizadas]').fill('Se verificó una conexión simulada para la auditoría QA.')
   form.locator('[name=resultado]').fill('Resultado ficticio de atención registrado para probar el flujo.')
   form.locator('input[type=file]').set_input_files(str(fixture));form.locator('button[type=submit]').click();tp.wait_for_load_state()
   check('WEB-10','HU-07/CA-01,03','Técnico adjunta PDF real',lambda:'registrad' in tp.inner_text('body').lower())
   own(page,base);check('WEB-11','HU-03/CA-02,04;HU-07/CA-04','Adjunto sin revisar no visible',lambda:'No se encuentran evidencias disponibles' in page.inner_text('body'))
   op=chrome.new_context();opage=op.new_page();login(opage,base,'22222222');opage.goto(base+'/operador/evidencias?reclamoId=2')
   href=opage.locator('a[href^="/evidencias/"]').first.get_attribute('href');eid=href.split('/')[-1]
   before=page.context.request.get(base+href,max_redirects=0);check('WEB-12','HU-03/CA-03;RNF-05','Titular no abre adjunto oculto',lambda:before.status==403)
   opage.get_by_role('button',name='He revisado y apruebo su consulta ciudadana').click();own(page,base)
   check('WEB-13','HU-03/CA-01,02,03;HU-07/CA-04','Titular descarga adjunto revisado',lambda:page.locator('a[href^="/evidencias/"]').count()==1 and page.context.request.get(base+href).body()==fixture.read_bytes())
   check('WEB-14','HU-03/CA-03;HU-08/CA-05','Usuario ajeno recibe 403',lambda:pp.context.request.get(base+href).status==403)
   for width,height in [(1366,900),(768,1024),(360,800)]:
    page.set_viewport_size({'width':width,'height':height})
    for path,name in [('/login','login'),('/usuario','avisos'),('/usuario/reclamos/nuevo','reclamo'),('/usuario/consultar?codigo=REC-2026-0002','seguimiento')]:
     page.goto(base+path);img=snapshot(page,f'{name}-{width}')
     check(f'RESP-{name}-{width}','RNF-01',f'Sin overflow a {width}px; {img}',lambda:page.evaluate('document.documentElement.scrollWidth <= innerWidth'))
   own(page,base)
   check('WEB-15','HU-04/CA-01,02,03,04','Aviso de atención con código y fecha en bandeja propia',lambda:check_notice(page,base))
   # Close only on explicit POST, then request reopening without erasing history.
   own(page,base);initial=page.locator('.history-list li').count()
   r=submit(page,base+'/usuario/reclamos/2/cerrar',{});own(page,base)
   check('WEB-16','HU-05/CA-01,02','Sin confirmación permanece Atendido',lambda:'Atendido' in page.inner_text('body') and page.locator('input[name=confirmar]').count()==1)
   page.locator('input[name=confirmar]').check();page.get_by_role('button',name='Confirmar cierre').click();own(page,base)
   check('WEB-17','HU-05/CA-01,02,04','Titular confirma y ve Cerrado',lambda:page.locator('#motivo').count()==1 and 'Cerrado' in page.inner_text('body'))
   for w,h in [(1366,900),(768,1024),(360,800)]:
    page.set_viewport_size({'width':w,'height':h});snapshot(page,f'cierre-{w}')
   r=submit(page,base+'/usuario/reclamos/2/reapertura',dict(motivo='corto'));own(page,base)
   check('WEB-18','HU-06/CA-02','Motivo corto no cambia Cerrado',lambda:page.locator('#motivo').count()==1)
   page.locator('#motivo').fill('El problema ficticio persiste después de la atención simulada.');page.get_by_role('button',name='Solicitar reapertura').click();own(page,base)
   check('WEB-19','HU-06/CA-01,03,04,05;RNF-08','Solicitud pendiente preserva historial y oculta formulario',lambda:'Reapertura solicitada' in page.inner_text('body') and page.locator('.history-list li').count()==initial+2 and page.locator('#motivo').count()==0)
   for w,h in [(1366,900),(768,1024),(360,800)]:
    page.set_viewport_size({'width':w,'height':h});snapshot(page,f'reapertura-{w}')
   check('WEB-20','HU-06/CA-01;RNF-05','Duplicado y ajeno sin segunda transición',lambda:reject_reopen(page,pp,base,initial+2))
   # Controlled latency includes navigation + DOM result, not just a socket request.
   page.set_viewport_size({'width':1366,'height':900});samples=[]
   for _ in range(20):
    start=time.perf_counter();own(page,base);page.locator('#codigo-reclamo').wait_for();samples.append(time.perf_counter()-start)
   opage.goto(base+'/operador');ncasos=opage.locator('tbody tr').count()
   (OUT/'rendimiento-20.json').write_text(json.dumps(dict(fecha=now(),escenario=f'Chrome headless, navegación DOM seguimiento propio, H2 en memoria {ncasos} incidencias, un usuario secuencial, 20 consultas, equipo local compartido',segundos=samples,cumplen=sum(x<=3 for x in samples),p95=sorted(samples)[18]),indent=2),encoding='utf-8')
   check('PERF-01','RNF-04','Al menos 19/20 navegaciones DOM <=3 segundos',lambda:sum(x<=3 for x in samples)>=19)
   pp.goto(base+'/login');pp.keyboard.press('Tab');focused=pp.evaluate('({tag:document.activeElement.tagName,outline:getComputedStyle(document.activeElement).outlineStyle})')
   check('A11Y-01','RNF-09','Tab desplaza foco visible; verificación parcial',lambda:focused['tag']!='BODY' and focused['outline']!='none')
   (OUT/'a11y-parcial.json').write_text(json.dumps(focused),encoding='utf-8')
   # Check same-origin navigational links visible to the citizen.
   own(page,base);links=page.locator('a[href^="/"]').evaluate_all('(els)=>els.map(e=>e.getAttribute("href"))')
   results={u:page.context.request.get(base+u).status for u in set(links) if not u.startswith('/evidencias/')}
   (OUT/'enlaces.json').write_text(json.dumps(results,indent=2),encoding='utf-8');check('LINK-01','RNF-02','Enlaces ciudadanos sin 404/500',lambda:all(s<400 for s in results.values()))
   check('KEYBOARD-01','RNF-09','Consulta y cierre/reapertura solo con teclado',lambda:keyboard_flow(chrome,base))
   contrast=page.evaluate('''() => [...document.querySelectorAll('main label, main p, main h1, main h2, main h3, main a, main button, main strong, main span')].filter(e=>e.getClientRects().length && e.textContent.trim()).map(e=>{
     let c=getComputedStyle(e),p=e,bg='rgba(0, 0, 0, 0)',gradient='';
     while(p){let s=getComputedStyle(p);if(s.backgroundImage!=='none'){gradient=s.backgroundImage;break;}if(s.backgroundColor!=='rgba(0, 0, 0, 0)'&&s.backgroundColor!=='transparent'){bg=s.backgroundColor;break;}p=p.parentElement;}
     return {text:e.textContent.trim().slice(0,80),color:c.color,bg,gradient};
   })''')
   (OUT/'contraste-muestras.json').write_text(json.dumps(contrast,ensure_ascii=False,indent=2),encoding='utf-8')
   (OUT/'chrome-version.txt').write_text(chrome.version,encoding='utf-8')
   # Edge selected explicitly, independent browser installation.
   edge=pw.chromium.launch(executable_path='C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe',headless=True)
   ep=edge.new_page();login(ep,base);own(ep,base);snapshot(ep,'edge-seguimiento')
   check('EDGE-01','RNF-02','Edge login y seguimiento',lambda:'Última actualización registrada' in ep.inner_text('body'))
   (OUT/'edge-version.txt').write_text(edge.version,encoding='utf-8');edge.close()
   ctx.close();pub.close();tech.close();op.close()
  finally:stop(proc,log);chrome.close()
def check_notice(p,b):
 p.goto(b+'/usuario');text=p.inner_text('body');return 'REC-2026-0002' in text and 'registrada' in text and bool(re.search(r'\d{2}/\d{2}/\d{4}',text))
def keyboard_flow(browser,b):
 ctx=browser.new_context();p=ctx.new_page();p.goto(b+'/login')
 p.keyboard.insert_text('87654321');p.keyboard.press('Tab');p.keyboard.insert_text('1234');p.keyboard.press('Tab');p.keyboard.press('Enter');p.wait_for_url('**/usuario')
 p.goto(b+'/usuario/consultar')
 def reach(selector):
  for _ in range(30):
   if p.locator(selector).evaluate('(e)=>document.activeElement===e'):return
   p.keyboard.press('Tab')
  raise AssertionError('No se alcanzó con Tab '+selector)
 reach('#codigo');p.keyboard.insert_text('REC-2026-0003');p.keyboard.press('Tab');p.keyboard.press('Enter');p.locator('input[name=confirmar]').wait_for()
 reach('input[name=confirmar]');p.keyboard.press('Space');p.keyboard.press('Tab');p.keyboard.press('Enter');p.wait_for_url('**/usuario')
 p.goto(b+'/usuario/consultar?codigo=REC-2026-0003');reach('#motivo');p.keyboard.insert_text('El problema ficticio persiste, recorrido de teclado.');p.keyboard.press('Tab');p.keyboard.press('Enter');p.wait_for_url('**/usuario')
 p.goto(b+'/usuario/consultar?codigo=REC-2026-0003');assert 'Reapertura solicitada' in p.inner_text('body');snapshot(p,'teclado-cierre-reapertura');ctx.close();return True
def reject_reopen(p,other,b,count):
 submit(p,b+'/usuario/reclamos/2/reapertura',dict(motivo='Duplicado ficticio de QA.'))
 own(other,b,'REC-2026-0003');r=submit(other,b+'/usuario/reclamos/2/reapertura',dict(motivo='Solicitud de un titular ajeno.'))
 own(p,b);actual=p.locator('.history-list li').count()
 assert r.status==403 and actual==count, f'HTTP ajeno={r.status}; historial={actual}; esperado={count}'
 return True
if __name__=='__main__':
 try:run()
 except Exception:
  (OUT/'navegador-error.txt').write_text(traceback.format_exc(),encoding='utf-8');raise
