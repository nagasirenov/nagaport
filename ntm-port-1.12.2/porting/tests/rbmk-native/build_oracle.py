#!/usr/bin/env python3
"""Extract original RBMK methods and assets into an independent recording fixture."""
import argparse,hashlib,json,re,zipfile
from pathlib import Path
HERE=Path(__file__).resolve().parent

def methods(source,name):
 result=[]
 for m in re.finditer(r'public\s+(?:static\s+)?[\w<>]+\s+'+re.escape(name)+r'\([^)]*\)\s*\{',source):
  e=m.end();depth=1
  while depth:
   depth+=(source[e]=='{')-(source[e]=='}');e+=1
  result.append(source[m.start():e])
 assert result,name
 return '\n'.join(result)

def generate(legacy_client=None,pristine=None):
 manifest=json.loads((HERE/'source-manifest.json').read_text())
 if legacy_client is not None:
  assert hashlib.sha256(Path(legacy_client).read_bytes()).hexdigest()==manifest['legacy_client_sha256'],'Wrong original client JAR'
 with zipfile.ZipFile(HERE/'pristine-rbmk-sources.zip') as z:
  data={p:(Path(pristine)/p).read_bytes() if pristine else z.read(p) for p in manifest['files']}
 for p,b in data.items():assert hashlib.sha256(b).hexdigest()==manifest['files'][p],p
 src={Path(p).stem:b.decode() for p,b in data.items() if p.endswith('.java')}
 out=(HERE/'OriginalRBMKOracle.java.in').read_text()
 replacements={}
 for n in ('Rod','Control','Reflector'):
  replacements['RENDER_'+n.upper()]='\n'.join(methods(src['RenderRBMK'+n],m) for m in ('renderInventoryBlock','renderWorldBlock','shouldRender3DInInventory'))
 replacements['BASE']='\n'.join(methods(src['RBMKBase'],m) for m in ('getIcon','hasOwnLid','metaToLid','shouldSideBeRendered'))
 replacements['PIPED']=methods(src['RBMKPipedBase'],'getIcon')
 replacements['CONTROL']=methods(src['RBMKControl'],'getIcon')
 replacements['AUTO']=methods(src['RBMKControlAuto'],'getIcon')
 replacements['OBJ']=methods(src['ObjUtil'],'renderPartWithIcon')
 for marker,s in replacements.items():
  if marker=='OBJ':
   # Only recording instrumentation, original face geometry/color expressions remain verbatim.
   sig='public static void renderPartWithIcon(HFRWavefrontObject model, String name, IIcon icon, Tessellator tes, float rot, float pitch, float roll, boolean shadow) {'
   s=s.replace(sig,sig+'\n active.group = model.name + "/" + name; active.texture = icon.name; active.side = -1; active.standard = false;')
  out=out.replace('/* '+marker+' */',s)
 ownerLines=[]
 for line in src['ModBlocks'].splitlines():
  m=re.search(r'\b(rbmk_\w+) = new (RBMK\w+)\([^)]*\).*setBlockTextureName\(RefStrings.MODID \+ ":(rbmk/[^"]+)"',line)
  if not m:continue
  ident,cls,texture=m.groups()
  if cls not in ('RBMKRod','RBMKRodReaSim','RBMKControl','RBMKControlAuto','RBMKBlank','RBMKReflector','RBMKAbsorber','RBMKModerator','RBMKOutgasser','RBMKStorage','RBMKCooler','RBMKBoiler','RBMKHeater'):continue
  fixture= 'RBMKRod' if cls in ('RBMKRod','RBMKRodReaSim') else cls if cls in ('RBMKControl','RBMKControlAuto') else 'RBMKPipedBase' if cls in ('RBMKBoiler','RBMKHeater') else 'RBMKBase'
  ownerLines.append('static final RBMKBase '+ident+' = register('+json.dumps(ident)+', '+json.dumps('hbm:'+texture)+', new '+fixture+'());')
 assert len(ownerLines)==18,len(ownerLines)
 out=out.replace('/* OWNERS */','\n'.join(ownerLines))
 assetLines=[]
 for n in ('rbmk_element','rbmk_element_rods','rbmk_rods'):
  s=data['src/main/resources/assets/hbm/models/rbmk/'+n+'.obj'].decode()
  assetLines.append('static final HFRWavefrontObject '+n+' = parseObj('+json.dumps(n)+', '+json.dumps(s)+');')
 out=out.replace('/* ASSETS */','\n'.join(assetLines))
 assert '/* RENDER_' not in out
 return out

if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--legacy-client');p.add_argument('--pristine');p.add_argument('--output',required=True);a=p.parse_args();Path(a.output).write_text(generate(a.legacy_client,a.pristine))
