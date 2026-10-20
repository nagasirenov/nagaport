#!/usr/bin/env python3
"""Compile the full rail/funnel owner, native base, geometry and actual registry closure. Only unrelated NTM collaborators are controlled; no platform facades."""
from pathlib import Path
import argparse,hashlib,importlib.util,json,subprocess,tempfile,zipfile
HERE=Path(__file__).resolve().parent
parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--root',type=Path,default=HERE.parents[2])
parser.add_argument('--original-root',type=Path,required=True)
parser.add_argument('--java-home',type=Path,required=True)
parser.add_argument('--classpath-file',type=Path,required=True)
parser.add_argument('--output',type=Path,required=True)
a=parser.parse_args();a.root=a.root.resolve();s=a.root/'src/main/java';o=a.output.resolve();o.mkdir(parents=True,exist_ok=True)
classpath=a.classpath_file.read_text().strip()
spec=importlib.util.spec_from_file_location('routing_oracle',HERE/'build-routing-oracle.py');routing=importlib.util.module_from_spec(spec);spec.loader.exec_module(routing)
oracle,original_hashes=routing.generate(a.original_root)
expected=json.loads((HERE/'routing-original-manifest.json').read_text())
assert original_hashes==expected['sha256'],'Pristine routing source drift'
print('PINNED ORIGINAL ROUTING SOURCES',len(original_hashes),flush=True)
# Compile complete native owners, base and geometry. Controlled NTM collaborators are not platform stubs.
prod=list((s/'com/hbm/blocks/rail').glob('*.java'))
prod += [s/p for p in ['com/hbm/blocks/machine/MachineFunnel.java','com/hbm/blocks/BlockDummyable.java','com/hbm/blocks/ICustomBlockHighlight.java','com/hbm/handler/MultiblockHandlerXR.java','com/hbm/handler/ThreeInts.java','com/hbm/interfaces/ICopiable.java','com/hbm/tileentity/IPersistentNBT.java','com/hbm/world/gen/nbt/INBTBlockTransformable.java','com/hbm/lib/ForgeDirection.java','com/hbm/lib/RefStrings.java','com/hbm/util/Either.java','com/hbm/interfaces/Spaghetti.java','com/hbm/util/fauxpointtwelve/Rotation.java','com/hbm/util/Tuple.java','com/hbm/util/fauxpointtwelve/BlockPos.java','com/hbm/render/block/RenderRail.java','com/hbm/render/block/nativeblock/RailBakedModel.java','com/hbm/render/block/nativeblock/AtlasObjGeometry.java','com/hbm/render/block/nativeblock/BlockPositionLightQuad.java','com/hbm/render/block/nativeblock/ThermalModelTransforms.java','com/hbm/render/mesh/ObjMesh.java','com/hbm/render/mesh/ObjMeshParser.java','com/hbm/main/RailModelHandler.java','com/hbm/blocks/ITooltipProvider.java']]
coll={
'com.hbm.main.MainRegistry':'public class MainRegistry {public static Object instance;public static net.minecraft.creativetab.CreativeTabs machineTab=net.minecraft.creativetab.CreativeTabs.REDSTONE;}',
'com.hbm.util.CompatExternal':'public class CompatExternal {public static net.minecraft.tileentity.TileEntity getCoreFromPos(net.minecraft.world.World w,int x,int y,int z){return w.getTileEntity(new net.minecraft.util.math.BlockPos(x,y,z));}}',
'com.hbm.items.ModItems':'public class ModItems {public static net.minecraft.item.Item train=new net.minecraft.item.Item();}',
'com.hbm.lib.Library':'public class Library {public static final ForgeDirection POS_X=ForgeDirection.EAST, NEG_X=ForgeDirection.WEST,POS_Z=ForgeDirection.SOUTH,NEG_Z=ForgeDirection.NORTH;}',
'com.hbm.util.ParticleUtil':'public class ParticleUtil {public static void spawnDroneLine(net.minecraft.world.World w,double x,double y,double z,double dx,double dy,double dz,int color){}}',
'com.hbm.util.i18n.I18nUtil':'public class I18nUtil {public static String[] resolveKeyArray(String key){return new String[]{key};}}',
'com.hbm.tileentity.machine.TileEntityMachineFunnel':'public class TileEntityMachineFunnel extends net.minecraft.tileentity.TileEntity {}',
}
def method(text,name):
 import re
 m=re.search(r'public static [^\n]+\b'+name+r'\([^)]*\)\s*\{',text);assert m,name;e=m.end();d=1
 while d:d+=(text[e]=='{')-(text[e]=='}');e+=1
 return text[m.start():e]
coll['com.hbm.entity.train.EntityRailCarBase']='import net.minecraft.util.math.Vec3d;import net.minecraft.util.math.MathHelper;public class EntityRailCarBase {'+method((s/'com/hbm/entity/train/EntityRailCarBase.java').read_text(),'generateYaw').replace('MathHelper.wrapAngleTo180_double(', 'MathHelper.wrapDegrees(')+'}'
coll['com.hbm.util.BobMathUtil']='public class BobMathUtil {'+method((s/'com/hbm/util/BobMathUtil.java').read_text(),'angularDifference')+'}'
for name,body in coll.items():
 p=o/(name.replace('.','/')+'.java');p.parent.mkdir(parents=True,exist_ok=True);p.write_text('package '+name.rsplit('.',1)[0]+';'+body);prod.append(p)

# Compile and invoke the exact twelve actual shared registration statements, not hand-written constructor substitutes.
import re
manifest=json.loads((HERE/'oracle-original-manifest.json').read_text());registration=(s/'com/hbm/blocks/ModBlocks.java').read_text();lines=[]
for owner in manifest['owners']:
 match=re.search(r'^\s*'+re.escape(owner['name'])+r' = [^\n]+;',registration,re.M);assert match,owner['name'];lines.append(match[0])
registration_source='import java.util.*;import net.minecraft.block.Block;import com.hbm.blocks.rail.*;import com.hbm.blocks.machine.*;import com.hbm.main.MainRegistry;public class RailRegistrationProbe {public static Map<String,Block> create(){'+''.join('Block '+owner['name']+';' for owner in manifest['owners'])+''.join(lines)+'Map<String,Block> out=new LinkedHashMap<>();'+''.join('out.put("'+owner['name']+'",'+owner['name']+');' for owner in manifest['owners'])+'return out;}}'
registration_file=o/'RailRegistrationProbe.java';registration_file.write_text(registration_source);prod.append(registration_file)
for path in prod:
 print('PRODUCTION' if str(path).startswith(str(s)) else 'EXTRACTED SHARED REGISTRATION' if path==registration_file else 'CONTROLLED NTM COLLABORATOR',path,hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
f=o/'OriginalRailRouting.java';f.write_text(oracle);prod.append(f)
prod += [HERE/'RailOwnerNativeTest.java',HERE/'RailModelNativeTest.java']
subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',classpath,'-d',str(o)]+list(map(str,prod)),check=True)
cp=str(o)+':'+classpath
subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-cp',cp,'RailOwnerNativeTest'],cwd=o,check=True)
resources=a.root/'src/main/resources';manifest=json.loads((HERE/'oracle-original-manifest.json').read_text());jar=o/'rail-native-resources.jar'
with zipfile.ZipFile(jar,'w') as z:
 for asset in manifest['assets']+['blockstates/tile.'+owner['name']+'.json' for owner in manifest['owners']]:
  path=resources/'assets/hbm'/asset;z.write(path,'assets/hbm/'+asset)
subprocess.run([str(a.java_home/'bin/java'),'-Xmx768m','-Djava.awt.headless=true','-cp',cp,'RailModelNativeTest',str(resources),str(HERE/'oracle-original-manifest.json'),str(jar)],cwd=o,check=True)
# Each mutant compiles against real native classes and must fail the native routing/placement/lifecycle probe.
mutants=[('com/hbm/blocks/rail/BlockRailWaypointSystem.java','.subtractReverse(','.subtract('),('com/hbm/blocks/rail/RailStandardRamp.java','cY + 1 - dist','cY + 2 - dist')]
for relative,before,after in mutants:
 source=s/relative;text=source.read_text();assert before in text
 with tempfile.TemporaryDirectory(prefix='rail-mutation-',dir=o) as temp:
  mutation=Path(temp)/relative;mutation.parent.mkdir(parents=True,exist_ok=True);mutation.write_text(text.replace(before,after))
  subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',cp,'-d',temp,str(mutation)],check=True)
  result=subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-cp',temp+':'+cp,'RailOwnerNativeTest'],cwd=o,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True)
  assert result.returncode!=0 and 'AssertionError' in result.stdout,'Invalid mutation accepted: '+relative
  print('PASS intentional production behavior mutation rejected:',relative,before,'=>',after,flush=True)
print('PASS complete native rail owner/model closure and two behavior negative controls. Not full build/gameplay/visual acceptance.',flush=True)
