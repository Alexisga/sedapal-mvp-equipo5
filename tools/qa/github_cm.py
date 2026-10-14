"""GitHub CM mediante credencial existente de GCM, nunca impresa ni guardada."""
import subprocess, json, urllib.request, urllib.error, sys
from pathlib import Path
from datetime import datetime, timezone

ROOT=Path(__file__).resolve().parents[2]
OUT=ROOT/'ENTREGA_PUBLICACION_CM_HTTPS_SEDAPAL'
OUT.mkdir(exist_ok=True)
def request(method,path,payload=None):
    p=subprocess.run(['git','-c','credential.interactive=never','credential','fill'],input='protocol=https\nhost=github.com\nusername=Alexisga\n\n',text=True,capture_output=True,check=True,cwd=ROOT)
    cred=dict(line.split('=',1) for line in p.stdout.splitlines() if '=' in line)
    token=cred['password']
    req=urllib.request.Request('https://api.github.com'+path,data=json.dumps(payload).encode() if payload is not None else None,method=method,headers={'Authorization':'Bearer '+token,'Accept':'application/vnd.github+json','X-GitHub-Api-Version':'2022-11-28','User-Agent':'SEDAPAL-CM-QA','Content-Type':'application/json'})
    try:
        with urllib.request.urlopen(req,timeout=45) as r:return r.status,json.load(r)
    except urllib.error.HTTPError as e:
        return e.code,json.loads(e.read())

def save(name,data):
    (OUT/name).write_text(json.dumps({'fecha':datetime.now(timezone.utc).isoformat(),'resultado':data},ensure_ascii=False,indent=2),encoding='utf-8')

if __name__=='__main__':
    mode=sys.argv[1]
    status,me=request('GET','/user')
    assert status==200,(status,me.get('message'))
    assert me['login']=='Alexisga'
    save('GITHUB_IDENTIDAD.json',{'login':me['login'],'id':me['id'],'html_url':me['html_url'],'autenticacion':'GCM existente; credencial no registrada'})
    path='/repos/Alexisga/sedapal-mvp-equipo5'
    status,repo=request('GET',path)
    if mode=='create' and status==404:
        status,repo=request('POST','/user/repos',{'name':'sedapal-mvp-equipo5','private':True,'auto_init':False,'description':'SEDAPAL MVP Equipo 5 - Desarrollo de Software I. Datos simulados y evidencias CM/PPQA.','has_wiki':False,'has_projects':False})
        assert status==201,(status,repo.get('message'))
    assert status==200 or status==201,(status,repo.get('message'))
    assert repo['private'] and repo['owner']['login']=='Alexisga'
    if mode=='final':
        status,repo=request('PATCH',path,{'default_branch':'main','delete_branch_on_merge':False})
        assert status==200
    save('GITHUB_REPOSITORIO.json',{k:repo.get(k) for k in ['id','full_name','html_url','private','visibility','default_branch','created_at','updated_at','pushed_at','size','permissions']})
    if mode=='final':
        results={}
        for endpoint in ['branches','tags','collaborators','actions/workflows','commits?per_page=100']:
            status,data=request('GET',path+'/'+endpoint)
            results[endpoint]={'status':status,'data':data}
        save('GITHUB_VERIFICACION_API.json',results)
    print(json.dumps({'login':me['login'],'url':repo['html_url'],'private':repo['private'],'default_branch':repo['default_branch']}))
