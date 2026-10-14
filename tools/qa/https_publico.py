"""Pruebas reales de Render autorizado; secreto temporal fuera de la entrega."""
from pathlib import Path
import sys,os,json,ssl,socket,hashlib,urllib.request,urllib.error,traceback
from datetime import datetime,timezone
ROOT=Path(__file__).resolve().parents[2]
OUT=ROOT/'ENTREGA_PUBLICACION_CM_HTTPS_SEDAPAL/HTTPS_PUBLICO'
OUT.mkdir(parents=True,exist_ok=True)
BASE='https://sedapal-mvp-equipo5.onrender.com'
HOST='sedapal-mvp-equipo5.onrender.com'
PASSWORD=(ROOT/'target/demo-cloud-privado.txt').read_text(encoding='utf-8')
assert len(PASSWORD)>=8
RESULT=[]
def check(id,expected,fn):
 try:
  obtained=fn();assert obtained is not False;state='PASS'
 except Exception as e:obtained=type(e).__name__+': '+str(e);state='FAIL'
 RESULT.append({'id':id,'esperado':expected,'obtenido':obtained,'estado':state,'fecha':datetime.now(timezone.utc).isoformat(),'responsable':'Codex agente automatizado','url':BASE,'commit_desplegado':'ea97f87900262905b1bb59da6df947205e21769b'})
 (OUT/'TLS_HTTP_RESULTADOS.json').write_text(json.dumps(RESULT,ensure_ascii=False,indent=2),encoding='utf-8')
 print(id,state,flush=True)

def tls():
 ctx=ssl.create_default_context()
 with socket.create_connection((HOST,443),timeout=30) as sock:
  with ctx.wrap_socket(sock,server_hostname=HOST) as s:
   cert=s.getpeercert();der=s.getpeercert(True)
   data={'tls':s.version(),'cifrado':s.cipher(),'certificado':cert,'cert_sha256':hashlib.sha256(der).hexdigest(),'validacion_cadena_y_hostname':True}
   (OUT/'CERTIFICADO_PUBLICO.pem').write_text(ssl.DER_cert_to_PEM_cert(der),encoding='ascii')
   return data
check('PUB-TLS-01','Cadena de confianza y hostname válidos con almacén del sistema; sin omitir validación TLS',tls)
class NoRedirect(urllib.request.HTTPRedirectHandler):
 def redirect_request(self,*args,**kwargs):return None
def redirect():
 opener=urllib.request.build_opener(NoRedirect)
 try:r=opener.open('http://'+HOST+'/login',timeout=45)
 except urllib.error.HTTPError as e:r=e
 assert r.status in [301,302,307,308]
 assert r.headers.get('Location','').startswith(BASE)
 return {'http':r.status,'location':r.headers.get('Location')}
check('PUB-TLS-02','HTTP redirige al mismo host HTTPS',redirect)
def response():
 with urllib.request.urlopen(BASE+'/login',timeout=60) as r:
  html=r.read().decode();h=r.headers
  assert r.status==200 and 'DATOS SIMULADOS' in html and 'JSESSIONID=' in h.get('Set-Cookie','')
  cookie=h.get('Set-Cookie','');assert all(x in cookie.lower() for x in ['secure','httponly','samesite=lax'])
  assert h.get('Strict-Transport-Security')
  assert PASSWORD not in html
  return {'http':r.status,'HSTS':h.get('Strict-Transport-Security'),'cookie_atributos':'Secure; HttpOnly; SameSite=Lax; valor omitido','banner_simulado':True,'secreto_en_html':False}
check('PUB-TLS-03','Login real, HSTS, cookie protegida, banner simulado y secreto ausente del HTML',response)

# Reutilizar funciones probadas, sin iniciar servidor local ni repetir otros motores.
sys.path.insert(0,str(ROOT.parent/'.qa-python'))
os.environ['QA_DEMO_PASSWORD']=PASSWORD
oldargv=sys.argv[:]
sys.argv=['cierre_browser.py','--jar',str(ROOT/'target/sedapal-reclamos-0.1.2-SNAPSHOT.jar'),'--out',str(OUT/'NAVEGADOR_CHROME')]
source=(ROOT/'tools/qa/cierre_browser.py').read_text(encoding='utf-8').split('with sync_playwright() as pw:')[0]
source=source.replace('timeout=5000','timeout=30000')
scope={'__file__':str(ROOT/'tools/qa/cierre_browser.py'),'__name__':'qa_publico'}
exec(compile(source,'cierre_browser_publico','exec'),scope)
sys.argv=oldargv
try:
 with scope['sync_playwright']() as pw:
  browser=pw.chromium.launch(executable_path='C:/Program Files/Google/Chrome/Application/chrome.exe',headless=True)
  scope['ENGINES'].append({'motor':'chrome','version':browser.version,'estado':'EJECUTADO','entorno':'Render público HTTPS; Windows headless; datos simulados','url':BASE})
  scope['execute'](browser,'chrome',BASE)
  # Validación de cookies autenticadas y rutas sensibles después del flujo completo.
  c=browser.new_context();p=c.new_page();scope['login'](p,BASE)
  cookies=[{k:v for k,v in x.items() if k!='value'} for x in c.cookies()]
  check('PUB-TLS-04','Cookie de sesión autenticada Secure, HttpOnly y SameSite=Lax',lambda:all(x['secure'] and x['httpOnly'] and x['sameSite']=='Lax' for x in cookies if x['name']=='JSESSIONID') and any(x['name']=='JSESSIONID' for x in cookies))
  for route in ['/h2-console/','/.env','/actuator/env']:
   res=c.request.get(BASE+route,max_redirects=0)
   check('RUTA-'+route,'Ruta sensible no disponible (403/404)',lambda res=res:{'http':res.status} if res.status in [403,404] else False)
  c.close();browser.close()
except Exception:
 (OUT/'ERROR_NAVEGADOR.txt').write_text(traceback.format_exc(),encoding='utf-8')
 print('NAVEGADOR FALLÓ; ver registro sin secretos',flush=True)
finally:
 scope['save']()
summary={'checks_tls_http':len(RESULT),'pass_tls_http':sum(x['estado']=='PASS' for x in RESULT),'checks_navegador':len(scope['ROWS']),'pass_navegador':sum(x['estado']=='PASS' for x in scope['ROWS']),'scans':len(scope['SCANS']),'fecha':datetime.now(timezone.utc).isoformat(),'publico_real':True,'url':BASE}
(OUT/'RESUMEN.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(summary),flush=True)
assert summary['pass_tls_http']==7 and summary['checks_navegador']==131 and summary['pass_navegador']==131
