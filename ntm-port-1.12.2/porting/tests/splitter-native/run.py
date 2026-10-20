#!/usr/bin/env python3
"""Complete splitter owners against real cached MC/Forge. No platform stand-ins.
Explicit domain fixtures are inherited from the finite diode network probe, less its moving-item
fixture: this probe compiles the actual native moving entity and complete dummyable base.
"""
from pathlib import Path
import argparse, ast, hashlib, json, subprocess, tempfile, os, re
p=argparse.ArgumentParser();p.add_argument('--module',type=Path,default=Path(__file__).resolve().parents[3]);p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--pristine',type=Path,required=True);a=p.parse_args();r=a.module;s=r/'src/main/java';t=Path(__file__).resolve().parent
# A frozen delivery fails closed if a production owner changes without renewed evidence.
freeze=t/'production.sha256'
if freeze.exists():
 for line in freeze.read_text().splitlines():
  digest,rel=line.split('  ',1);assert hashlib.sha256((r/rel).read_bytes()).hexdigest()==digest,'Frozen production changed: '+rel
assign={}
for node in ast.parse((r/'porting/tests/probe-native-diode-network.py').read_text()).body:
 if isinstance(node,ast.Assign) and len(node.targets)==1 and isinstance(node.targets[0],ast.Name) and node.targets[0].id in ['collaborators','basefiles','production']:assign[node.targets[0].id]=ast.literal_eval(node.value)
fixtures=assign['collaborators'];del fixtures['com/hbm/entity/item/EntityMovingItem.java']
fixtures.update({'com/hbm/main/MainRegistry.java':'package com.hbm.main; public class MainRegistry {public static Object instance;}', 'com/hbm/util/CompatExternal.java':'package com.hbm.util; public class CompatExternal {public static net.minecraft.tileentity.TileEntity getCoreFromPos(net.minecraft.world.World w,int x,int y,int z){return w.getTileEntity(new net.minecraft.util.math.BlockPos(x,y,z));}}'})
production=['com/hbm/blocks/network/CraneSplitter.java','com/hbm/tileentity/network/TileEntityCraneSplitter.java','com/hbm/render/block/RenderSplitter.java','com/hbm/render/tileentity/SplitterBreakingRenderer.java','com/hbm/main/SplitterModelHandler.java']
support=assign['basefiles']+assign['production']+['com/hbm/blocks/BlockDummyable.java','com/hbm/handler/MultiblockHandlerXR.java','com/hbm/interfaces/ICopiable.java','com/hbm/blocks/ICustomBlockHighlight.java','com/hbm/handler/ThreeInts.java','com/hbm/util/Either.java','com/hbm/tileentity/IPersistentNBT.java','com/hbm/world/gen/nbt/INBTBlockTransformable.java','com/hbm/entity/item/EntityMovingConveyorObject.java','com/hbm/entity/item/EntityMovingItem.java']
def method(text,name):
 m=re.search(r'(?:public|private) (?:static )?(?:void|boolean|ItemStack\[\]|Vec3|ForgeDirection) '+name+r'\([^)]*\)\s*\{',text);assert m,name
 i=m.end();depth=1
 while depth:depth+=(text[i]=='{')-(text[i]=='}');i+=1
 return text[m.start():i]
source=(a.pristine/'src/main/java/com/hbm/render/block/RenderSplitter.java').read_text();methods=[]
for name in ['renderInventoryBlock','renderWorldBlock','drawSplitter']:
 body=method(source,name)
 for old,new in {'Block':'OracleBlock','CraneSplitter':'OracleBlock','IBlockAccess':'OracleWorld','IIcon':'int'}.items():body=re.sub(r'\b'+old+r'\b',new,body)
 methods.append(body)
oracle=(t/'oracle.java.in').read_text().replace('/* METHODS */','\n'.join(methods))
source=(a.pristine/'src/main/java/com/hbm/tileentity/network/TileEntityCraneSplitter.java').read_text();logic='''import net.minecraft.util.math.MathHelper;import com.hbm.lib.ForgeDirection;
final class LegacySplitterLogic {boolean position;byte remaining;byte leftRatio=1,rightRatio=1;
 static final class ItemStack {int stackSize;ItemStack(int n){stackSize=n;}ItemStack copy(){return new ItemStack(stackSize);}}
 static final class World {void markTileEntityChunkModified(int x,int y,int z,Object t){}}World worldObj=new World();int xCoord,yCoord,zCoord;
'''+method(source,'splitStack')+'\n'
source=(a.pristine/'src/main/java/com/hbm/blocks/network/CraneSplitter.java').read_text();logic+='''static final class MutableVec {double xCoord,yCoord,zCoord;MutableVec(double x,double y,double z){xCoord=x;yCoord=y;zCoord=z;}static MutableVec createVectorHelper(double x,double y,double z){return new MutableVec(x,y,z);}double lengthVector(){return MathHelper.sqrt(xCoord*xCoord+yCoord*yCoord+zCoord*zCoord);}}
 static final class MoveWorld {int metadata;int getBlockMetadata(int x,int y,int z){return metadata;}}static final int offset=10;
'''
for name in ['getTravelLocation','getClosestSnappingPosition','getTravelDirection']:
 body=method(source,name).replace('Vec3','MutableVec').replace('World world','MoveWorld world').replace('MathHelper.clamp_double','MathHelper.clamp');logic+=body+'\n'
logic+='}\n'
manifest={'production':{},'support':{},'domain_fixtures':sorted(fixtures),'pristine':{},'assets':{}}
for rel in ['blocks/network/CraneSplitter.java','tileentity/network/TileEntityCraneSplitter.java','render/block/RenderSplitter.java']:
 path=a.pristine/'src/main/java/com/hbm'/rel;manifest['pristine'][rel]=hashlib.sha256(path.read_bytes()).hexdigest()
for path in sorted(list((r/'src/main/resources/assets/hbm').rglob('*splitter*'))+[r/'src/main/resources/assets/hbm/textures/blocks/crane_side.png']):
 if path.is_file():
  rel=path.relative_to(r);digest=hashlib.sha256(path.read_bytes()).hexdigest();assert digest==hashlib.sha256((a.pristine/rel).read_bytes()).hexdigest();manifest['assets'][str(rel)]=digest
with tempfile.TemporaryDirectory(prefix='splitter-native-') as d:
 out=Path(d);files=[]
 for rel,body in fixtures.items():
  path=out/rel;path.parent.mkdir(parents=True,exist_ok=True);path.write_text(body);files.append(str(path))
 for kind,paths in [('production',production),('support',sorted(set(support)))]:
  for rel in paths:
   path=s/rel;files.append(str(path));manifest[kind][rel]=hashlib.sha256(path.read_bytes()).hexdigest()
 for name,body in [('LegacySplitterOracle.java',oracle),('LegacySplitterLogic.java',logic)]:
  path=out/name;path.write_text(body);files.append(str(path))
 files.append(str(t/'SplitterNativeTest.java'))
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',d,'-cp',a.classpath,'-d',d]+files,check=True)
 print('PASS whole splitter block/tile/model/loader/breaking renderer, actual BlockDummyable and moving entities compile against native APIs.',flush=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Xmx1200m','-Djava.awt.headless=true','-cp',d+os.pathsep+a.classpath,'SplitterNativeTest',str(r),str(a.pristine)],cwd=d,check=True)
(t/'source-manifest.json').write_text(json.dumps(manifest,indent=2,sort_keys=True)+'\n')
