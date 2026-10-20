#!/usr/bin/env python3
"""Native conveyor model/loader closure with verbatim pristine descriptor oracles.
Compiles complete production owners, renderers, model, helper and loader. Only explicit
NTM collaborators are controlled; no Minecraft/Forge facades or production source shims.
The pristine renderer bodies run in a test-only pure-Java descriptor recorder.
"""
import argparse, ast, hashlib, json, os, re, subprocess, tempfile, zipfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--pristine',type=Path,default=Path('/workspace/shared/nagaport/ntm-original-pristine'));p.add_argument('--vanilla-assets',type=Path,default=Path('/workspace/shared/nagaport-toolchain/gradle-user-home/caches/minecraft/net/minecraft/minecraft/1.12.2/minecraft-1.12.2.jar'));a=p.parse_args()
r=Path(__file__).resolve().parents[2];s=r/'src/main/java';t=r/'porting/tests';resources=r/'src/main/resources';exe='.exe' if os.name=='nt' else ''
def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def method(text,name):
 m=re.search(r'(?:public|private|protected) (?:void|boolean|IIcon|int) '+name+r'\([^)]*\)\s*\{',text);assert m,name
 b=m.end();e=b;depth=1
 while depth:depth+=(text[e]=='{')-(text[e]=='}');e+=1
 return text[m.start():e]
def convert(body):
 for old,new in {'Block':'OracleBlock','RenderBlocks':'Recorder','IBlockAccess':'OracleWorld','Tessellator':'SilentTessellator','IIcon':'String','GL11':'RecordingGL','ModBlocks':'OracleBlocks','IConveyorBelt':'OracleBelt','IEnterableBlock':'OracleEnterable'}.items():body=re.sub(r'\b'+old+r'\b',new,body)
 return body.replace('Blocks.iron_block','OracleBlocks.iron_block')
manifest={'purpose':'Pinned pristine conveyor model/layout/icon/asset sources; native geometry/loader proof is separate from live graphics acceptance.','sources':{},'assets':{}}
oracle=(t/'conveyor-model-oracle.java.in').read_text()
for name in ['RenderConveyor','RenderConveyorLift','RenderConveyorChute']:
 f=a.pristine/'src/main/java/com/hbm/render/block'/(name+'.java');manifest['sources'][str(f.relative_to(a.pristine))]=sha(f);source=f.read_text()
 body='\n'.join(convert(method(source,n)) for n in ['renderInventoryBlock','renderWorldBlock']+(['standardBundle'] if name.endswith('Chute') else []))
 oracle=oracle.replace('/* '+name+' */',body)
for name in ['Base','Bendable','Classic']:
 f=a.pristine/'src/main/java/com/hbm/blocks/network'/('BlockConveyor'+name+'.java');manifest['sources'][str(f.relative_to(a.pristine))]=sha(f)
 body=convert(method(f.read_text(),'getIcon'))
 if name=='Bendable':body+='\n'+method(f.read_text(),'getPathDirection')
 oracle=oracle.replace('/* Icon'+name+' */',body)
for rel in ['src/main/java/com/hbm/blocks/ModBlocks.java','src/main/java/com/hbm/blocks/generic/BlockGrate.java']:
 manifest['sources'][rel]=sha(a.pristine/rel)
for stem in ['conveyor','conveyor_express','conveyor_double','conveyor_triple']:
 for suffix in ['', '_curve_left','_curve_right']:
  for ext in ['.png','.png.mcmeta']:
   rel='assets/hbm/textures/blocks/'+stem+suffix+ext;original=a.pristine/'src/main/resources'/rel
   assert original.is_file(),rel
   assert sha(original)==sha(resources/rel),'Changed original '+rel
   manifest['assets'][rel]=sha(original)
for stem in ['conveyor_side','concrete','grate_top']:
 for ext in ['.png','.png.mcmeta']:
  rel='assets/hbm/textures/blocks/'+stem+ext;original=a.pristine/'src/main/resources'/rel
  if original.is_file():
   assert sha(original)==sha(resources/rel);manifest['assets'][rel]=sha(original)
with zipfile.ZipFile(a.vanilla_assets) as z:
 manifest['vanilla_assets']={rel:hashlib.sha256(z.read(rel)).hexdigest() for rel in ['assets/minecraft/models/item/generated.json','assets/minecraft/textures/blocks/iron_block.png']}
manifest_path=t/'conveyor-model-native-source-manifest.json'
if manifest_path.exists():assert json.loads(manifest_path.read_text())==manifest,'Pinned pristine source/asset mismatch'
else:manifest_path.write_text(json.dumps(manifest,indent=2,sort_keys=True)+'\n')
# Reuse the explicit NTM collaborators from the independent complete-owner probe,
# reading only its literal dictionary rather than importing/running the script.
collaborators=None
for node in ast.parse((t/'probe-native-conveyor-owners.py').read_text()).body:
 if isinstance(node,ast.Assign) and any(isinstance(n,ast.Name) and n.id=='collaborators' for n in node.targets):collaborators=ast.literal_eval(node.value)
assert collaborators
names=['Base','Bendable','','Double','Triple','Express','Lift','Chute','Classic']
production=['com/hbm/blocks/network/BlockConveyor'+n+'.java' for n in names]+['com/hbm/blocks/network/ConveyorModelState.java','com/hbm/blocks/ITooltipProvider.java','com/hbm/lib/ForgeDirection.java','com/hbm/lib/RefStrings.java','api/hbm/block/IToolable.java']+['api/hbm/conveyor/'+n+'.java' for n in ['IConveyorBelt','IEnterableBlock','IConveyorItem','IConveyorPackage']]
production+=['com/hbm/render/block/'+n+'.java' for n in ['RenderConveyor','RenderConveyorLift','RenderConveyorChute']]+['com/hbm/render/block/nativeblock/'+n+'.java' for n in ['ConveyorBakedModel','ConveyorCuboidGeometry','ConveyorMaterials','ThermalModelTransforms']]+['com/hbm/main/ConveyorBlockModelHandler.java']
with tempfile.TemporaryDirectory(prefix='conveyor-model-native-') as d:
 out=Path(d);files=[]
 for rel,body in collaborators.items():
  assert rel.startswith('com/hbm/');f=out/rel;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(body);files.append(str(f))
 f=out/'LegacyConveyorModelOracle.java';f.write_text(oracle);files.append(str(f))
 for rel in production:
  f=s/rel;print('PRODUCTION',rel,sha(f),flush=True);files.append(str(f))
 f=out/'ConveyorModelNativeTest.java';f.write_text((t/'conveyor-model-native-test.java.in').read_text());files.append(str(f))
 subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',d,'-cp',a.classpath,'-d',d]+files,check=True)
 print('PASS complete production conveyor owners + model family + loader compiled against native MC/Forge.',flush=True)
 jar=out/'conveyor-resources.jar'
 with zipfile.ZipFile(jar,'w') as z:
  for rel in manifest['assets']:z.write(resources/rel,rel)
  for f in (resources/'assets/hbm/blockstates').glob('tile.conveyor*.json'):z.write(f,f.relative_to(resources).as_posix())
  f=resources/'assets/hbm/models/item/conveyor_lift.json';z.write(f,f.relative_to(resources).as_posix())
 subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-Xmx1500m','-Djava.awt.headless=true','-cp',os.pathsep.join([d,a.classpath]),'ConveyorModelNativeTest',str(resources),str(jar),str(a.vanilla_assets)],cwd=d,check=True)
print('PASS unchanged pristine assets, real native registry/state/item/custom-loader closure in directory and JAR resources. No GL/live game/full-mod claim.',flush=True)
