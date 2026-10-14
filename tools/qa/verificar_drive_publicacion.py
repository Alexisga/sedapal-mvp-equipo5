from pathlib import Path
import json,hashlib,urllib.request,concurrent.futures
from datetime import datetime,timezone
ROOT=Path(__file__).resolve().parents[2]
OUT=ROOT/'ENTREGA_PUBLICACION_CM_HTTPS_SEDAPAL'
data=json.loads((ROOT/'target/drive-readbacks-privado.json').read_text(encoding='utf-8'))
def verify(item):
 p=item['p'];meta=item['metadata'];local=Path(p.get('local',str(ROOT/'ENTREGA_CIERRE_TECNICO_SEDAPAL/LEEME_ENTREGA.md')))
 expected=hashlib.sha256(local.read_bytes()).hexdigest()
 with urllib.request.urlopen(item['readback']['download_url'],timeout=90) as r:
  obtained=hashlib.file_digest(r,'sha256').hexdigest()
 permissions=meta.get('permissions',[])
 restricted=meta.get('shared') is False and len(permissions)==1 and permissions[0].get('role')=='owner'
 correct_parent=p['parent'] in meta.get('parent_ids',[])
 good=expected==obtained and restricted and correct_parent and int(meta['size'])==local.stat().st_size
 return {'nombre':p['nombre'],'area':p['area'],'id':p['id'],'url':p['url'],'parent':p['parent'],'bytes_local':local.stat().st_size,'bytes_drive':int(meta['size']),'sha256_local':expected,'sha256_descargado':obtained,'permisos':permissions,'restringido_solo_propietario':restricted,'padre_correcto':correct_parent,'resultado':'PASS' if good else 'FAIL','fecha_verificacion':datetime.now(timezone.utc).isoformat(),'responsable':'Codex agente automatizado'}
with concurrent.futures.ThreadPoolExecutor(max_workers=6) as pool:results=list(pool.map(verify,data))
(OUT/'DRIVE_VERIFICACION_CARGAS.json').write_text(json.dumps(results,ensure_ascii=False,indent=2),encoding='utf-8')
assert all(r['resultado']=='PASS' for r in results),[x for x in results if x['resultado']!='PASS']
print(json.dumps({'archivos_descargados_y_SHA256_verificado':len(results),'solo_propietario':True,'resultado':'PASS'}))
