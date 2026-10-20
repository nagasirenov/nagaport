#!/usr/bin/env python3
"""Whole native rotated-cube owners/model/handler plus pristine selectors and original face bytecode."""
import argparse, ast, hashlib, json, os, re, subprocess, tempfile, zipfile
from pathlib import Path
from side_rotation_fixtures import pristine_root
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--pristine',type=Path,default=None);p.add_argument('--legacy-client',type=Path,default=Path('/tmp/nagaport-legacy-client.jar'));p.add_argument('--generate-only',action='store_true');a=p.parse_args();a.pristine=pristine_root(a.pristine)
r=Path(__file__).resolve().parents[2];s=r/'src/main/java';t=r/'porting/tests';resources=r/'src/main/resources'
def sha(f):return hashlib.sha256(f.read_bytes()).hexdigest()
def method(text,start):
 b=text.index('{',start)+1;e=b;depth=1
 while depth:depth+=(text[e]=='{')-(text[e]=='}');e+=1
 return text[start:e]
def collaborators(path):
 scope={}
 for node in ast.parse(path.read_text()).body:
  selected=isinstance(node,ast.Assign) and any(isinstance(n,ast.Name) and n.id in ['names','production','collaborators'] for n in node.targets)
  selected|=isinstance(node,ast.AugAssign) and isinstance(node.target,ast.Name) and node.target.id=='production'
  selected|=isinstance(node,ast.For) and isinstance(node.target,ast.Tuple) and all(isinstance(x,ast.Name) for x in node.target.elts) and [x.id for x in node.target.elts] in [['n','count'],['owner','method','values']]
  if selected:exec(compile(ast.Module(body=[node],type_ignores=[]),str(path),'exec'),scope)
 return scope
crane=collaborators(t/'probe-native-crane-owners.py');editor=collaborators(t/'editor-cube-native-probe.py')
fixtures=dict(crane['collaborators']);fixtures.update(editor['collaborators'])
fixtures['com/hbm/interfaces/ICopiable.java']=fixtures['com/hbm/interfaces/ICopiable.java'].replace(' }',' default String[] infoForDisplay(World w,int x,int y,int z){return null;} }')
production=list(dict.fromkeys(crane['production']+editor['production']+['com/hbm/render/block/RenderBlockSideRotation.java','com/hbm/render/block/nativeblock/SideRotationBakedModel.java','com/hbm/render/block/nativeblock/ConveyorCuboidGeometry.java','com/hbm/render/block/nativeblock/ThermalModelTransforms.java','com/hbm/main/SideRotationBlockModelHandler.java']))
manifest={'purpose':'Pinned independent pristine selector/renderer/texture evidence; complete combined native owners/models are compiled, not replaced.','sources':{},'assets':{},'legacy_client_sha256':'a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'}
def original(rel):
 f=a.pristine/'src/main/java'/rel;manifest['sources']['src/main/java/'+rel]=sha(f);return f.read_text()
interface=original('com/hbm/blocks/IBlockSideRotation.java')
oracle='''import com.hbm.lib.ForgeDirection;import com.hbm.lib.RefStrings;
final class OriginalSideRotationOracle {
 static class World {int meta;boolean missing;ForgeDirection outputOverride;int getBlockMetadata(int x,int y,int z){return meta;}}
 static class Icons {java.util.Set<String> textures=new java.util.LinkedHashSet<>();String registerIcon(String s){s=s.replace(":",":blocks/");textures.add(s);return s;}}
 static abstract class Owner {abstract String getIcon(int side,int meta);String getIcon(World w,int x,int y,int z,int side){return getIcon(side,w.meta);}int getRotationFromSide(World w,int x,int y,int z,int side){return 0;} }
 static class IBlockSideRotation {
'''
for m in re.finditer(r'public static (?:int topToBottom|boolean isOpposite)\(',interface):oracle+=method(interface,m.start())+'\n'
oracle+='}\n'
textures=set()
for name in crane['names']+['BlockWand'+n for n in ['Jigsaw','Tandem','Logic','Loot']]:
 is_crane=name in crane['names'];base=name=='BlockCraneBase';orig=original('com/hbm/blocks/'+('network/' if is_crane else 'generic/')+name+'.java')
 oracle+='static class '+name+' extends '+('BlockCraneBase' if is_crane and not base else 'Owner')+' {\n'
 if base:oracle+='String '+','.join(['blockIcon']+re.findall(r'protected IIcon (\w+);',orig))+';\nForgeDirection getInputSide(World w,int x,int y,int z){return ForgeDirection.getOrientation(w.meta);}\nForgeDirection getOutputSideOverride(World w,int x,int y,int z){return w.missing?ForgeDirection.UNKNOWN:w.outputOverride;}\nForgeDirection getOutputSide(World w,int x,int y,int z){return w.missing?ForgeDirection.UNKNOWN:w.outputOverride==ForgeDirection.UNKNOWN?getInputSide(w,x,y,z).getOpposite():w.outputOverride;}\n'
 elif not is_crane:oracle+='String blockIcon,iconTop,iconSide,iconBack;\n'
 oracle+=name+'(){registerBlockIcons(new Icons());}\n'
 for m in re.finditer(r'public (?:void|IIcon|int) (registerBlockIcons|getIcon|getRotationFromSide)\(',orig):
  body=method(orig,m.start()).replace('IIconRegister','Icons').replace('IIcon','String').replace('IBlockAccess','World');oracle+=body+'\n'
 for tex in re.findall(r'registerIcon\(RefStrings.MODID \+ ":([^"\n]+)"\)',orig):textures.add('assets/hbm/textures/blocks/'+tex+'.png')
 oracle+='}\n'
render=original('com/hbm/render/block/RenderBlockSideRotation.java')
assignments=re.findall(r'renderer\.uvRotate(\w+) = rot\.getRotationFromSide\(world, x, y, z, (\d)\);',render);assert len(assignments)==6
obf={'Bottom':'w','Top':'v','East':'r','West':'s','North':'u','South':'t'}
oracle+='static void configure(Object renderer,Class<?> type,Owner rot,World world,boolean item)throws Exception {\n'
for name,side in assignments:oracle+='SideRotationModelNativeTest.field(type,"'+obf[name]+'").setInt(renderer,item?0:rot.getRotationFromSide(world,0,0,0,'+side+'));\n'
oracle+='}\n'
yaw=re.search(r'GL11.glRotatef\(([0-9.]+)F, 0.0F, 1.0F, 0.0F\)',render).group(1);assert 'GL11.glTranslatef(-0.5F, -0.5F, -0.5F)' in render
oracle+='static final int ITEM_YAW='+str(int(float(yaw)))+';\n}\n'
# Pin registry spellings independently, not from the new handler table.
registry=original('com/hbm/blocks/ModBlocks.java')
for var,owner in [('crane_'+n.lower(),'Crane'+n) for n in ['Extractor','Inserter','Grabber','Boxer','Unboxer']]+[('wand_'+n.lower(),'BlockWand'+n) for n in ['Jigsaw','Tandem','Logic','Loot']]:
 line=next(x for x in registry.splitlines() if re.search(r'\b'+var+r' = new ',x));assert 'new '+owner+'(' in line and 'setBlockName("'+var+'")' in line
for rel in sorted(textures):
 assert sha(resources/rel)==sha(a.pristine/'src/main/resources'/rel),'Original texture changed: '+rel
 manifest['assets'][rel]=sha(resources/rel)
assert sha(a.legacy_client)==manifest['legacy_client_sha256'],'Unexpected cached original client'
mp=t/'side-rotation-model-native-source-manifest.json'
if mp.exists():assert json.loads(mp.read_text())==manifest,'Pinned pristine source/asset mismatch'
else:mp.write_text(json.dumps(manifest,indent=2,sort_keys=True)+'\n')
print('GENERATED ORACLE SHA256',hashlib.sha256(oracle.encode('UTF-8')).hexdigest(),flush=True)
if a.generate_only:raise SystemExit(0)
with tempfile.TemporaryDirectory(prefix='side-rotation-model-native-') as d:
 out=Path(d);files=[]
 for rel,body in fixtures.items():
  assert rel.startswith('com/hbm/');f=out/rel;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(body);files.append(str(f));print('BOUNDED COLLABORATOR',rel,flush=True)
 for rel in production:files.append(str(s/rel));print('COMPLETE PRODUCTION',rel,sha(s/rel),flush=True)
 for name,body in [('OriginalSideRotationOracle',oracle),('SideRotationModelNativeTest',(t/'side-rotation-model-native-test.java.in').read_text())]:f=out/(name+'.java');f.write_text(body);files.append(str(f))
 files.append(str(t/'ForgeEventNativeLauncher.java'))
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',d,'-cp',a.classpath,'-d',d]+files,check=True)
 print('PASS combined complete real nine owners, crane base/tile, four full editor nested tile/GUI classes, snapshot/interface, geometry, native baked model and native handler compile against unchanged Minecraft/Forge.',flush=True)
 jar=out/'side-rotation-resources.jar'
 with zipfile.ZipFile(jar,'w') as z:
  for rel in manifest['assets']:z.write(resources/rel,rel)
  for name in ['crane_'+n for n in ['extractor','inserter','grabber','boxer','unboxer']]+['wand_'+n for n in ['jigsaw','tandem','logic','loot']]:
   rel='assets/hbm/blockstates/tile.'+name+'.json';z.write(resources/rel,rel)
 subprocess.run([str(a.java_home/'bin/java'),'-Xmx1200m','-Djava.awt.headless=true','-cp',d+os.pathsep+a.classpath,'ForgeEventNativeLauncher','SideRotationModelNativeTest',str(resources),str(jar),str(a.legacy_client)],cwd=d,check=True)
