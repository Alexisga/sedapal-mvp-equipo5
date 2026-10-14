from pathlib import Path
import subprocess, re, json, io, zipfile
from datetime import datetime, timezone

ROOT=Path(__file__).resolve().parents[2]
OUT=ROOT/'ENTREGA_PUBLICACION_CM_HTTPS_SEDAPAL'
def git(*args):return subprocess.check_output(['git',*args],cwd=ROOT)
objects=git('rev-list','--objects','--all').decode().splitlines()
paths={x.split(' ',1)[0]:x.split(' ',1)[1] if ' ' in x else '' for x in objects}
info=subprocess.run(['git','cat-file','--batch-check=%(objectname) %(objecttype)'],cwd=ROOT,input=('\n'.join(paths)+'\n').encode(),capture_output=True,check=True).stdout.decode().splitlines()
blobs=[x.split()[0] for x in info if x.endswith(' blob')]
raw=subprocess.run(['git','cat-file','--batch'],cwd=ROOT,input=('\n'.join(blobs)+'\n').encode(),capture_output=True,check=True).stdout
rules={'token_github':rb'(?:gh[pousr]_[A-Za-z0-9]{30,}|github_pat_[A-Za-z0-9_]{60,})','clave_privada':rb'-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----','aws_access':rb'AKIA[0-9A-Z]{16}','google_oauth':rb'ya29\.[A-Za-z0-9_-]{30,}'}
findings=[];offset=0
for oid in blobs:
 end=raw.index(b'\n',offset);n=int(raw[offset:end].split()[-1]);data=raw[end+1:end+1+n];offset=end+1+n+1
 for rule,pattern in rules.items():
  if re.search(pattern,data):findings.append({'objeto':oid,'archivo':paths[oid],'regla':rule})
 path=paths[oid]
 if Path(path).name=='.env' or path.endswith(('.p12','.key','.mv.db','.sqlite','.sql')):
  findings.append({'objeto':oid,'archivo':path,'regla':'archivo_sensible'})
for p in ROOT.rglob('*'):
 if not p.is_file() or any(x in p.parts for x in ['.git','target','FUENTES_ANALISIS','EVIDENCIAS_CIERRE','ENTREGA_CIERRE_TECNICO_SEDAPAL','ENTREGA_PUBLICACION_CM_HTTPS_SEDAPAL']):continue
 if p.stat().st_size>5_000_000:continue
 data=p.read_bytes()
 for rule,pattern in rules.items():
  if re.search(pattern,data):findings.append({'archivo':str(p.relative_to(ROOT)),'regla':rule,'estado':'working tree'})
report={'fecha':datetime.now(timezone.utc).isoformat(),'commits':len(git('rev-list','--all').splitlines()),'blobs_git_revisados':len(blobs),'reglas':list(rules)+['archivos_sensibles'],'hallazgos':findings,'resultado':'PASS' if not findings else 'BLOQUEADO','limites':'Revisión automatizada de firmas de secretos y archivos sensibles, acompañada de inspección de configuraciones. No certifica ausencia universal de secretos.','valores_historicos':'Contraseñas de ejemplo/test sintéticas conservadas en historia antecedente para trazabilidad; no son credenciales vigentes ni se reutilizan. GCM, credencial de GitHub y secreto de despliegue quedan fuera del repositorio y de evidencias.'}
(OUT/'AUDITORIA_SECRETOS.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
assert not findings,findings
print(json.dumps({k:report[k] for k in ['commits','blobs_git_revisados','resultado']}))
