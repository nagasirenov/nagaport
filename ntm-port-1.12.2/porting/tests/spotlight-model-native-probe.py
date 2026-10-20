#!/usr/bin/env python3
"""Native Spotlight whole-owner/model/handler proof; only external NTM collaborators are fixtures."""
import argparse,ast,hashlib,json,os,re,subprocess,tempfile,zipfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--pristine',type=Path,default=Path('/workspace/shared/nagaport/ntm-original-pristine'));p.add_argument('--legacy-client',type=Path,default=Path('/tmp/nagaport-legacy-client.jar'));a=p.parse_args()
r=Path(__file__).resolve().parents[2];s=r/'src/main/java';t=r/'porting/tests';resources=r/'src/main/resources'
def sha(f):return hashlib.sha256(f.read_bytes()).hexdigest()
def method(text,name,select=-1):
 matches=list(re.finditer(r'(?:public|private|protected) (?:static )?[\w<>]+ '+name+r'\([^)]*\)\s*\{',text));assert matches,name
 m=matches[select];e=m.end();d=1
 while d:d+=(text[e]=='{')-(text[e]=='}');e+=1
 return text[m.start():e]
def src(rel):return (a.pristine/'src/main/java'/rel).read_text()
light=src('com/hbm/render/block/RenderLight.java');spot=src('com/hbm/blocks/machine/Spotlight.java');modular=src('com/hbm/blocks/machine/SpotlightModular.java');obj=src('com/hbm/render/util/ObjUtil.java')
oracle=(t/'spotlight-model-oracle.java.in').read_text()
for name in ['getModel','getPartName','getDirection']:oracle=oracle.replace('/* '+name+' */',method(spot,name).replace('HFRWavefrontObject','String'))
for name in ['getYaw','getPitch']:oracle=oracle.replace('/* '+name+' */',method(obj,name))
oracle=oracle.replace('/* modularPart */',method(modular,'getPartName')).replace('/* renderer */','\n'.join(method(light,n) for n in ['renderInventoryBlock','renderWorldBlock','getRotation']))
# Bind the six tested definitions to original ModBlocks and ResourceManager declarations,
# rather than merely trusting the native handler's table or fixture constructors.
blocks_source=src('com/hbm/blocks/ModBlocks.java');resource_source=src('com/hbm/main/ResourceManager.java')
for suffix,owner,length,kind,asset in [('incandescent','Spotlight',2,'INCANDESCENT','cage_lamp'),('fluoro','SpotlightModular',8,'FLUORESCENT','fluorescent_lamp'),('halogen','Spotlight',32,'HALOGEN','flood_lamp')]:
 for off in ['', '_off']:
  name='spotlight_'+suffix+off;line=next(x for x in blocks_source.splitlines() if re.search(r'\b'+name+r' = new ',x))
  assert 'new '+owner+'(Material.iron, '+str(length)+', LightType.'+kind+', '+('false' if off else 'true')+')' in line
  assert 'setBlockName("'+name+'")' in line and 'setBlockTextureName(RefStrings.MODID + ":'+asset+off+'")' in line
 assert '"models/lights/'+asset+'.obj"' in resource_source
manifest={'purpose':'Pinned pristine Spotlight selectors/OBJ/texture evidence for native model tests, separate from actual-owner behavior and graphical acceptance.','sources':{},'assets':{}}
for rel in ['com/hbm/render/block/RenderLight.java','com/hbm/render/util/ObjUtil.java','com/hbm/blocks/machine/Spotlight.java','com/hbm/blocks/machine/SpotlightModular.java','com/hbm/blocks/ModBlocks.java','com/hbm/main/ResourceManager.java']:
 manifest['sources']['src/main/java/'+rel]=sha(a.pristine/'src/main/java'/rel)
for stem in ['cage_lamp','fluorescent_lamp','flood_lamp']:
 for rel in ['assets/hbm/models/lights/'+stem+'.obj']+['assets/hbm/textures/blocks/'+stem+x+'.png' for x in ['','_off']]:
  f=a.pristine/'src/main/resources'/rel;assert sha(f)==sha(resources/rel),'Native asset differs '+rel;manifest['assets'][rel]=sha(f)
mp=t/'spotlight-model-native-source-manifest.json'
if mp.exists():assert json.loads(mp.read_text())==manifest,'Pinned pristine source/asset mismatch'
else:mp.write_text(json.dumps(manifest,indent=2,sort_keys=True)+'\n')
# Reuse only the audited owner probe's literal collaborators and production list.
# Compile complete actual owners here as well: the model integration has no owner shim.
owner_tree=ast.parse((t/'probe-native-spotlight-owners.py').read_text())
owner_scope={}
for node in owner_tree.body:
 if isinstance(node,ast.Assign) and any(isinstance(n,ast.Name) and n.id in ['production','collaborators'] for n in node.targets):
  exec(compile(ast.Module(body=[node],type_ignores=[]),'owner-collaborators','exec'),owner_scope)
collaborators=owner_scope['collaborators']
production=['com/hbm/blocks/BlockEnums.java','com/hbm/lib/ForgeDirection.java','com/hbm/lib/RefStrings.java','com/hbm/blocks/machine/SpotlightModelState.java','com/hbm/render/block/RenderLight.java','com/hbm/render/block/nativeblock/SpotlightBakedModel.java','com/hbm/main/SpotlightBlockModelHandler.java','com/hbm/render/mesh/ObjMesh.java','com/hbm/render/mesh/ObjMeshParser.java']+['com/hbm/render/block/nativeblock/'+n+'.java' for n in ['ThermalModelTransforms','BlockPositionLightQuad','SpotlightGeometry']]
production=list(dict.fromkeys(production+owner_scope['production']))
with tempfile.TemporaryDirectory(prefix='spotlight-model-native-') as d:
 out=Path(d);files=[]
 for rel,body in collaborators.items():
  assert rel.startswith('com/hbm/');f=out/rel;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(body);files.append(str(f))
 for rel in production:print('PRODUCTION',rel,sha(s/rel),flush=True);files.append(str(s/rel))
 for name,body in [('OriginalLightOracle',oracle),('SpotlightModelNativeTest',(t/'spotlight-model-native-test.java.in').read_text())]:f=out/(name+'.java');f.write_text(body);files.append(str(f))
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',d,'-cp',a.classpath,'-d',d]+files,check=True)
 print('PASS complete production Spotlight owners/geometry/model/state/handler compile against native Minecraft/Forge; explicit external NTM registry/celestial collaborators.',flush=True)
 jar=out/'spotlight-resources.jar'
 with zipfile.ZipFile(jar,'w') as z:
  for rel in manifest['assets']:z.write(resources/rel,rel)
  for f in (resources/'assets/hbm/blockstates').glob('tile.spotlight*.json'):z.write(f,f.relative_to(resources).as_posix())
 if a.legacy_client.exists():assert sha(a.legacy_client)=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce','Unexpected legacy client'
 subprocess.run([str(a.java_home/'bin/java'),'-Xmx1400m','-Djava.awt.headless=true','-cp',os.pathsep.join([d,a.classpath]),'SpotlightModelNativeTest',str(resources),str(jar),str(a.pristine/'src/main/resources'),str(a.legacy_client)],cwd=d,check=True)
