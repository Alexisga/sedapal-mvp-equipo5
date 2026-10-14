"""Empaqueta evidencia existente; no ejecuta ni inventa pruebas."""
from pathlib import Path
import csv, hashlib, json, shutil, subprocess, zipfile
from datetime import datetime
from zoneinfo import ZoneInfo

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / 'ENTREGA_CIERRE_TECNICO_SEDAPAL'
CODE = '4aad20366da34c617148a0b56ad7c5c575e30c04'

def git(*args):
    return subprocess.check_output(['git', *args], cwd=ROOT, text=True, encoding='utf-8').strip()

def sha(path):
    with path.open('rb') as f:
        return hashlib.file_digest(f, 'sha256').hexdigest()

def rows(path):
    with path.open(encoding='utf-8-sig', newline='') as f:
        return list(csv.DictReader(f))

def main():
    assert not (OUT/'VERSION_ENTREGA.json').exists(), 'Entrega ya finalizada'
    assert not git('diff', CODE, '--', 'src', 'pom.xml'), 'Código difiere del probado'
    ev=OUT/'EVIDENCIAS_NUEVAS'
    checks=json.loads((ev/'03_MATRIZ_CONSOLIDADA/checks.json').read_text(encoding='utf-8'))
    scans=json.loads((ev/'03_MATRIZ_CONSOLIDADA/scans-indice.json').read_text(encoding='utf-8'))
    tls=json.loads((ev/'04_TLS_FINAL/tls-resultados.json').read_text(encoding='utf-8'))
    assert len(checks)==524 and all(x['estado']=='PASS' for x in checks)
    assert len(scans)==156 and len(tls['checks'])==6 and all(x['estado']=='PASS' for x in tls['checks'])
    assert all(x['resultado']=='NO DONE' for x in rows(OUT/'MATRICES/ESTADO_8_HU.csv'))
    for row in rows(OUT/'CM/FUENTES_SHA256.csv'):
        assert sha(Path(row['original']))==row['sha256']
        assert sha(OUT/'FUENTES_OFICIALES_SOLO_LECTURA'/row['archivo'])==row['sha256']
    docs=ROOT/'docs/cierre-2026-10-14'
    for section in ['REPORTES','MATRICES','MEDICIONES','PPQA','CM','INSTRUMENTOS_HUMANOS']:
        for p in (OUT/section).rglob('*'):
            if p.is_file() and p.suffix in ['.md','.csv']:
                dest=docs/section/p.relative_to(OUT/section)
                dest.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(p,dest)
    git('add','tools/qa','docs/cierre-2026-10-14','.gitignore')
    git('-c','user.name=Codex QA','-c','user.email=codex.qa@example.invalid','commit','-m','docs(qa): consolidate technical closure evidence and approval inputs')
    package=git('rev-parse','HEAD')
    assert not git('diff', CODE, '--', 'src', 'pom.xml')
    bundle=OUT/'HISTORIAL_GIT_CIERRE.bundle'
    git('bundle','create',str(bundle),'--all')
    verification=subprocess.run(['git','bundle','verify',str(bundle)],cwd=ROOT,capture_output=True,text=True,check=True)
    (OUT/'CM/VERIFICACION_BUNDLE.txt').write_text(verification.stdout+verification.stderr,encoding='utf-8')
    archive=ROOT/'target/fuente-cierre.zip'
    git('archive','--format=zip','-o',str(archive),package)
    with zipfile.ZipFile(archive) as z:z.extractall(OUT/'CODIGO_FUENTE')
    restore=ROOT/'target/restauracion-bundle-cierre'
    assert not restore.exists()
    subprocess.run(['git','clone','--branch','cierre-tecnico/2026-10-14',str(bundle),str(restore)],check=True,capture_output=True)
    recovered=subprocess.check_output(['git','rev-parse','HEAD'],cwd=restore,text=True).strip()
    assert recovered==package
    (OUT/'CM/GIT_LOG.txt').write_text(git('log','--all','--date=iso-strict','--format=%H | %ad | %an | %s')+'\n',encoding='utf-8')
    (OUT/'CM/CAMBIOS_DESDE_v0.1.1.patch').write_text(git('diff','70b4b77b2a84a08b12c2eacdac84e2a1edc37890',package,'--','src','pom.xml','.github','.env.example','README_CIERRE_TECNICO.md')+'\n',encoding='utf-8')
    (OUT/'CM/PROCEDENCIA_CODIGO.md').write_text(f'# Procedencia\n\nBase conservada: 70b4b77b2a84a08b12c2eacdac84e2a1edc37890. Código probado: {CODE}. Commit documental y herramientas: {package}. No hay diferencias en src ni pom.xml entre ambos últimos commits. La mejora HU-02 y los commits originales permanecen en el bundle. El parche separa modificaciones nuevas de la base reutilizada.\n\nRestauración real del bundle comprobada mediante clon local y comparación de HEAD. CODIGO_FUENTE es una exportación sin .git; el bundle contiene el historial recuperable.\n',encoding='utf-8')
    drive=OUT/'ESTRUCTURA_DRIVE/03_Codigo_Git_y_Baselines'
    shutil.copy2(bundle,drive/bundle.name)
    shutil.copy2(OUT/'CM/PROCEDENCIA_CODIGO.md',drive/'LEEME.md')
    jar=ROOT/'target/sedapal-reclamos-0.1.2-SNAPSHOT.jar'
    assert sha(jar)==tls['jar_sha256']
    (OUT/'EJECUTABLE_LOCAL').mkdir()
    shutil.copy2(jar,OUT/'EJECUTABLE_LOCAL'/jar.name)
    metadata={'fecha_empaquetado':datetime.now(ZoneInfo('America/Lima')).isoformat(),'version':'0.1.2-SNAPSHOT','commit_codigo_probado':CODE,'commit_entrega':package,'bundle_restaurado_HEAD':recovered,'originales_sin_cambios':True,'java_nuevas_PASS':3,'checks_navegador_PASS':524,'scans_axe':156,'capturas_canonicas':160,'tls_local_PASS':6,'IP05':'3/6 = 50%; NO CUMPLE 90%','HU_Done':0,'publicacion_externa':False,'jar_sha256':sha(jar)}
    (OUT/'VERSION_ENTREGA.json').write_text(json.dumps(metadata,ensure_ascii=False,indent=2),encoding='utf-8')
    files=sorted(p for p in OUT.rglob('*') if p.is_file())
    forbidden=[p for p in files if p.suffix.lower() in ['.p12','.key','.mv.db'] or p.name=='.env']
    assert not forbidden,forbidden
    manifest=OUT/'MANIFIESTO_SHA256.csv'
    with manifest.open('w',encoding='utf-8',newline='') as f:
        w=csv.writer(f);w.writerow(['archivo','bytes','sha256'])
        for p in files:w.writerow([p.relative_to(OUT).as_posix(),p.stat().st_size,sha(p)])
    dest=ROOT/'ENTREGA_CIERRE_TECNICO_SEDAPAL_v0.1.2_2026-10-14.zip'
    assert not dest.exists()
    with zipfile.ZipFile(dest,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as z:
        for p in sorted(OUT.rglob('*')):
            if p.is_file():z.write(p,p.relative_to(OUT.parent).as_posix())
    with zipfile.ZipFile(dest) as z:
        assert z.testzip() is None
        for row in rows(manifest):
            with z.open(OUT.name+'/'+row['archivo']) as f:
                assert hashlib.file_digest(f,'sha256').hexdigest()==row['sha256']
    checksum=sha(dest)
    dest.with_suffix('.zip.sha256').write_text(checksum+'  '+dest.name+'\n',encoding='utf-8')
    result={'zip':str(dest),'bytes':dest.stat().st_size,'sha256':checksum,'archivos_manifestados':len(files),'CRC_y_SHA256':'PASS','restauracion_bundle':'PASS','commit_entrega':package,'git_status':git('status','--short')}
    (ROOT/'VALIDACION_ENTREGA_CIERRE.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
    print(json.dumps(result,ensure_ascii=False,indent=2))

if __name__=='__main__':main()
