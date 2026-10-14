"""TLS de loopback con confianza explícita en certificado efímero; no despliega Internet."""
from pathlib import Path
from datetime import datetime,timezone,timedelta
import subprocess,os,secrets,socket,ssl,time,json,re,hashlib
from urllib.request import build_opener,HTTPSHandler,HTTPCookieProcessor,Request
from urllib.parse import urlencode
from http.cookiejar import CookieJar
ROOT=Path(__file__).resolve().parents[2];OUT=ROOT/'EVIDENCIAS_CIERRE'/'04_TLS_FINAL';OUT.mkdir(parents=True,exist_ok=True)
PRIVATE=ROOT/'target'/('tls-private-'+secrets.token_hex(4));PRIVATE.mkdir(parents=True,exist_ok=True)
env=os.environ.copy();env['TLS_KEYSTORE_PASSWORD']=secrets.token_urlsafe(30);env['DEMO_PASSWORD']='QaTLS_'+secrets.token_hex(12)
key=PRIVATE/'sedapal-local.p12';cert=OUT/'certificado-publico-local.pem'
keytool='C:/Program Files/Java/jdk-21/bin/keytool.exe'
if key.exists():raise RuntimeError('No sobrescribir clave de un intento anterior')
subprocess.run([keytool,'-genkeypair','-alias','sedapal-local','-keyalg','RSA','-keysize','2048','-storetype','PKCS12','-keystore',str(key),'-storepass:env','TLS_KEYSTORE_PASSWORD','-validity','2','-dname','CN=localhost,OU=QA Academica,O=Proyecto Simulado,C=PE','-ext','SAN=dns:localhost,ip:127.0.0.1','-noprompt'],env=env,check=True,capture_output=True)
subprocess.run([keytool,'-exportcert','-rfc','-alias','sedapal-local','-keystore',str(key),'-storepass:env','TLS_KEYSTORE_PASSWORD','-file',str(cert)],env=env,check=True,capture_output=True)
env['TLS_KEYSTORE']=key.as_uri();s=socket.socket();s.bind(('127.0.0.1',0));port=s.getsockname()[1];s.close()
base=f'https://127.0.0.1:{port}';trusted=ssl.create_default_context(cafile=str(cert));client=build_opener(HTTPSHandler(context=trusted),HTTPCookieProcessor(CookieJar()))
args=['C:/Program Files/Java/jdk-21/bin/java.exe','-jar',str(ROOT/'target'/'sedapal-reclamos-0.1.2-SNAPSHOT.jar'),'--spring.profiles.active=demo,tls-local',f'--server.port={port}','--spring.datasource.url=jdbc:h2:mem:qatls;MODE=MySQL;DB_CLOSE_DELAY=-1','--spring.h2.console.enabled=false']
rows=[]
def record(id,expected,fn):
 try:v=fn();assert v is not False;state='PASS'
 except Exception as e:v=f'{type(e).__name__}: {e}';state='FAIL'
 rows.append(dict(id=id,fecha=datetime.now(timezone(timedelta(hours=-5))).isoformat(),responsable='Codex agente automatizado',esperado=expected,obtenido=v,estado=state,entorno='TLS local 127.0.0.1;certificado efímero confiado explícitamente'));print(id,state,str(v)[:100],flush=True)
def login():
 page=client.open(base+'/login').read().decode();token=re.search(r'name="_csrf"[^>]*value="([^"]+)"',page).group(1)
 response=client.open(Request(base+'/login',data=urlencode(dict(documento='12345678',password=env['DEMO_PASSWORD'],_csrf=token)).encode()))
 body=response.read().decode();assert response.url.endswith('/usuario') and 'REC-2026-0002' in body
 return response,body
with (OUT/'servidor-tls.log').open('wb') as log:
 proc=subprocess.Popen(args,cwd=ROOT,env=env,stdout=log,stderr=subprocess.STDOUT,creationflags=subprocess.CREATE_NO_WINDOW)
 try:
  for _ in range(100):
   try:
    response,body=login()
    if 'Asignado' in body:break
   except Exception:pass
   if proc.poll() is not None:raise RuntimeError('Servidor TLS terminó; revisar log')
   time.sleep(.4)
  else:raise RuntimeError('No inició TLS local')
  record('TLS-01','Handshake TLS validando certificado y nombre de host',lambda:client.open(base+'/login').status==200)
  def reject_unknown():
   try:build_opener(HTTPSHandler(context=ssl.create_default_context())).open(base+'/login');return False
  except Exception as e:
    assert isinstance(getattr(e,'reason',None),ssl.SSLCertVerificationError),repr(e)
    return {'certificado_no_confiado_rechazado':True,'tipo':type(e.reason).__name__}
  record('TLS-02','La confianza predeterminada rechaza el certificado autofirmado',reject_unknown)
  record('TLS-03','Autenticación y datos propios sobre TLS',lambda:login()[0].status==200)
  def cookies():
   anonymous=build_opener(HTTPSHandler(context=trusted));r=anonymous.open(base+'/login');values=r.headers.get_all('Set-Cookie') or []
   assert any('Secure' in v and 'HttpOnly' in v and 'SameSite=Lax' in v for v in values)
   return {'atributos':['Secure','HttpOnly','SameSite=Lax'],'valores_cookie':'OMITIDOS'}
  record('TLS-04','Cookie de sesión Secure, HttpOnly y SameSite=Lax',cookies)
  record('TLS-05','HSTS emitido en respuesta HTTPS',lambda:bool(client.open(base+'/usuario').headers.get('Strict-Transport-Security')))
  body=client.open(base+'/usuario').read().decode()
  token=re.search(r'name="_csrf"[^>]*value="([^"]+)"',body).group(1)
  client.open(Request(base+'/logout',data=urlencode({'_csrf':token}).encode())).read()
  record('TLS-06','Logout invalida acceso protegido sobre TLS',lambda:'/login' in client.open(base+'/usuario').url)
 finally:
  proc.terminate()
  try:proc.wait(timeout=8)
  except subprocess.TimeoutExpired:proc.kill();proc.wait()
(OUT/'tls-resultados.json').write_text(json.dumps(dict(fecha=datetime.now(timezone(timedelta(hours=-5))).isoformat(),commit=subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT,text=True).strip(),jar_sha256=hashlib.sha256((ROOT/'target'/'sedapal-reclamos-0.1.2-SNAPSHOT.jar').read_bytes()).hexdigest(),certificado_publico_sha256=hashlib.sha256(cert.read_bytes()).hexdigest(),publicacion_externa=False,clave_privada_entregada=False,checks=rows),ensure_ascii=False,indent=2),encoding='utf-8')
