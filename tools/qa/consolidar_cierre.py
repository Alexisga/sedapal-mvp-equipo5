from pathlib import Path
import json,shutil
p=Path(__file__).resolve().parents[2]/'EVIDENCIAS_CIERRE';out=p/'03_MATRIZ_CONSOLIDADA';assert not out.exists();out.mkdir()
allchecks=[];allscans=[];engines=[]
for engine in ['chrome','edge','firefox','webkit']:
 source=p/('03_WEBKIT_FINAL' if engine=='webkit' else '03_NAVEGADORES_FINAL')
 checks=[x for x in json.loads((source/'checks.json').read_text(encoding='utf-8')) if x['motor']==engine]
 scans=[x for x in json.loads((source/'scans-indice.json').read_text(encoding='utf-8')) if x['motor']==engine]
 assert len(checks)==131 and len(scans)==39 and all(x['estado']=='PASS' for x in checks),(engine,len(checks),len(scans))
 for f in source.iterdir():
  if f.is_file() and (f.name.startswith(engine+'-') or f.name in [f'servidor-{engine}.log',f'entorno-{engine}.json',f'evidencia-simulada-{engine}.pdf']):shutil.copy2(f,out/f.name)
 info=next(x for x in json.loads((source/'motores.json').read_text(encoding='utf-8')) if x['motor']==engine and x.get('estado')=='EJECUTADO');info['fuente_ejecucion']=source.name;engines.append(info)
 allchecks.extend(checks);allscans.extend(scans)
for name,rows in [('checks.json',allchecks),('scans-indice.json',allscans),('motores.json',engines)]:
 (out/name).write_text(json.dumps(rows,ensure_ascii=False,indent=2),encoding='utf-8')
(out/'CRITERIO_CONSOLIDACION.md').write_text('Misma versión de código 4aad203; última ejecución COMPLETA por motor. Chrome/Edge/Firefox provienen de 03_NAVEGADORES_FINAL; WebKit de 03_WEBKIT_FINAL tras corregir la espera de autenticación del verificador. Se conservan todos los intentos originales. No se cambian resultados de casos fallidos ni se denominan una nueva ejecución única.\n',encoding='utf-8')
q=p.parent/'tools/qa/entrega_cierre.py';s=q.read_text(encoding='utf-8').replace("browser=EV/'03_NAVEGADORES_FINAL'","browser=EV/'03_MATRIZ_CONSOLIDADA'")
s=s.replace('EVIDENCIAS_NUEVAS/03_NAVEGADORES_FINAL/','EVIDENCIAS_NUEVAS/03_MATRIZ_CONSOLIDADA/')
s=s.replace('Los motores/versiones y cada scan raw están en 03_NAVEGADORES_FINAL.','Los motores/versiones y cada scan raw están en 03_MATRIZ_CONSOLIDADA; se consolida la última ejecución completa por motor, conservando intentos anteriores.')
q.write_text(s,encoding='utf-8');print('CONSOLIDADO',len(allchecks),len(allscans))
