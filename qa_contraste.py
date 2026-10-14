import json,re,csv,os
from pathlib import Path
p=Path(os.environ.get('SEDAPAL_ENTREGA_DIR',str(Path(__file__).parent/'ENTREGA_PARA_APROBACION')))/'EVIDENCIAS'
rows=json.loads((p/'contraste-muestras.json').read_text(encoding='utf-8'))
def rgb(s):return [float(x)/255 for x in re.findall(r'[\d.]+',s)[:3]]
def luminance(v):return sum(a*b for a,b in zip([.2126,.7152,.0722],[x/12.92 if x<=.04045 else ((x+.055)/1.055)**2.4 for x in v]))
out=[]
for r in rows:
 if r['gradient'] or 'rgba' in r['color'] or r['bg']=='rgba(0, 0, 0, 0)':
  out.append({**r,'ratio':'','estado':'NO MEDIDO gradiente/transparencia'})
 else:
  a,b=luminance(rgb(r['color'])),luminance(rgb(r['bg']));ratio=(max(a,b)+.05)/(min(a,b)+.05)
  out.append({**r,'ratio':round(ratio,3),'estado':'PASS' if ratio>=4.5 else 'FAIL'})
with (p/'contraste-calculo.csv').open('w',encoding='utf-8-sig',newline='') as f:
 w=csv.DictWriter(f,fieldnames=list(out[0]));w.writeheader();w.writerows(out)
print(json.dumps({'muestras':len(out),'PASS':sum(r['estado']=='PASS' for r in out),'FAIL':sum(r['estado']=='FAIL' for r in out),'no_medido':sum(r['ratio']=='' for r in out)},ensure_ascii=False))
