import hashlib,json,re,zipfile
from pathlib import Path
HERE=Path(__file__).resolve().parent
LEGACY_SHA256='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
def method(text,name):
 m=re.search(r'public\s+(?:static\s+)?[\w<>]+\s+'+name+r'\([^)]*\)\s*\{',text);assert m,name
 e=m.end();depth=1
 while depth:depth+=(text[e]=='{')-(text[e]=='}');e+=1
 return text[m.start():e]
def generate(legacy_client):
 assert hashlib.sha256(Path(legacy_client).read_bytes()).hexdigest()==LEGACY_SHA256
 manifest=json.loads((HERE/'original-sha256.json').read_text());source={}
 with zipfile.ZipFile(HERE/'pristine-grate-sources.zip') as z:
  for path in z.namelist():
   data=z.read(path);assert hashlib.sha256(data).hexdigest()==manifest[path];source[Path(path).stem]=data.decode()
 out=(HERE/'OriginalGrateRenderOracle.java.in').read_text()
 for marker,owner,name in [('GET_Y','BlockGrate','getY'),('WORLD','RenderGrate','renderWorldBlock'),('ITEM','RenderGrate','renderInventoryBlock'),('ITEM_3D','RenderGrate','shouldRender3DInInventory')]:out=out.replace('/* '+marker+' */',method(source[owner],name))
 return out
