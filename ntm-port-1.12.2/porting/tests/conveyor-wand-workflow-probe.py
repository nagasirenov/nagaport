#!/usr/bin/env python3
"""Whole native ItemConveyorWand gameplay proof. Never compiles extracted production bodies.
Only the independent oracle extracts verbatim pristine routing/helper bodies, with an
explicit old-coordinate facade. Minecraft/Forge classes and conveyor owners are real.
"""
import argparse,hashlib,json,os,pathlib,re,subprocess,tempfile,zipfile
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);p.add_argument('--pristine',type=pathlib.Path,default=pathlib.Path('/workspace/shared/nagaport/ntm-original-pristine'));p.add_argument('--keep',type=pathlib.Path);p.add_argument('--section');a=p.parse_args()
r=pathlib.Path(__file__).resolve().parents[2];s=r/'src/main/java';t=r/'porting/tests'
collaborators={
'com/hbm/blocks/ModBlocks.java':'package com.hbm.blocks; public final class ModBlocks {public static net.minecraft.block.Block conveyor, conveyor_express, conveyor_double, conveyor_triple, conveyor_lift, conveyor_chute;}',
'com/hbm/items/ModItems.java':'package com.hbm.items; public final class ModItems {public static net.minecraft.item.Item conveyor_wand;}',
'com/hbm/inventory/RecipesCommon.java':'package com.hbm.inventory; public class RecipesCommon {public static class ComparableStack {public ComparableStack(net.minecraft.item.ItemStack s){}}}',
'com/hbm/util/i18n/I18nUtil.java':'package com.hbm.util.i18n; public final class I18nUtil {public static String[] resolveKeyArray(String s){return new String[]{s};} public static String resolveKey(String s){return s;}}',
'com/hbm/blocks/network/BlockCraneBase.java':'package com.hbm.blocks.network; public class BlockCraneBase extends net.minecraft.block.Block {public BlockCraneBase(){super(net.minecraft.block.material.Material.IRON);}}',
'com/hbm/main/MainRegistry.java':'package com.hbm.main; public final class MainRegistry {public static final Proxy proxy=new Proxy(); public static final class Proxy {public net.minecraft.entity.player.EntityPlayer player;public net.minecraft.entity.player.EntityPlayer me(){return player;}}}',
'com/hbm/render/util/RenderOverhead.java':'''package com.hbm.render.util; public final class RenderOverhead {public static com.hbm.wiaj.WorldInAJar preview;public static int x,y,z,sets,clears;public static boolean success;public static void clearActionPreview(){preview=null;clears++;}public static void setActionPreview(com.hbm.wiaj.WorldInAJar w,int a,int b,int c,boolean s){preview=w;x=a;y=b;z=c;success=s;sets++;}}'''
}
production=['com/hbm/items/tool/ItemConveyorWand.java','com/hbm/wiaj/WorldInAJar.java','com/hbm/blocks/ILookOverlay.java','com/hbm/entity/item/EntityMovingItem.java','com/hbm/entity/item/EntityMovingConveyorObject.java','com/hbm/util/fauxpointtwelve/BlockPos.java','com/hbm/util/fauxpointtwelve/Rotation.java','com/hbm/blocks/ITooltipProvider.java','com/hbm/blocks/network/ConveyorModelState.java','com/hbm/lib/ForgeDirection.java','api/hbm/block/IToolable.java']
production += ['com/hbm/interfaces/Spaghetti.java']
production += ['com/hbm/blocks/network/BlockConveyor'+n+'.java' for n in ['Base','Bendable','','Double','Triple','Express','Lift','Chute','Classic']]
production += ['api/hbm/conveyor/'+n+'.java' for n in ['IConveyorBelt','IConveyorItem','IConveyorPackage','IEnterableBlock']]
pristine=a.pristine/'src/main/java/com/hbm/items/tool/ItemConveyorWand.java';original=pristine.read_text()
def body(start):
 b=original.index('{',start);e=b+1;depth=1
 while depth:depth+=(original[e]=='{')-(original[e]=='}');e+=1
 return original[start:e]
methods=[]
for m in re.finditer(r'(?:public|private) static (?:int|Block|ForgeDirection|boolean) (?:construct|getFacingMeta|getConveyorMetaForDirection|getConveyorForDirection|getTargetDirection|taxiDistance|getConveyorBlock|hasSnakesAndLadders)\(',original):
 text=body(m.start())
 text=text.replace('private static','static').replace('World routeWorld','OracleWorld routeWorld').replace('IBlockAccess buildWorld','Object buildWorld').replace('buildWorld instanceof World)','buildWorld instanceof OracleWorld)').replace('((World) buildWorld)','((OracleWorld) buildWorld)').replace('WorldInAJar','OracleJar').replace('MathHelper.floor_double','MathHelper.floor')
 text=re.sub(r'routeWorld\.getBlock\(([^;]*?)\)\.isReplaceable\(routeWorld, ([^;]*?)\)',r'routeWorld.replaceable(\2)',text)
 methods.append(text)
assert len(methods)==9,len(methods)
oracle=(t/'conveyor-wand-workflow-oracle.java.in').read_text().replace('@@PRISTINE_ROUTING@@','\n'.join(methods))
manifest={'pristine':{str(pristine):hashlib.sha256(pristine.read_bytes()).hexdigest()},'production':{v:hashlib.sha256((s/v).read_bytes()).hexdigest() for v in production},'boundaries':list(collaborators),'oracle':'pristine entire routing/helper bodies; old API facade; production is whole compiled classes'}
print('SOURCE_MANIFEST '+json.dumps(manifest,sort_keys=True),flush=True)
def run(d):
 out=pathlib.Path(d);files=[]
 for path,text in collaborators.items():
  f=out/path;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(text);files.append(f)
 for name,text in [('LegacyWandOracle.java',oracle),('ConveyorWandWorkflowTest.java',(t/'conveyor-wand-workflow-test.java.in').read_text())]:
  f=out/name;f.write_text(text);files.append(f)
 files += [s/v for v in production]+[t/'ForgeEventNativeLauncher.java']
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for name in z.namelist():
     if name.endswith('.so') and '/' not in name:(out/name).write_bytes(z.read(name))
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(out),'-cp',a.classpath,'-d',str(out)]+[str(f) for f in files],check=True)
 print('PASS complete native gameplay item, actual WorldInAJar/ILookOverlay, nine conveyor owners, moving item/superclass, and dependencies compile',flush=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Xmx1536m','-Djava.awt.headless=true','-Djava.library.path='+str(out),'-cp',str(out)+os.pathsep+a.classpath,'ForgeEventNativeLauncher','ConveyorWandWorkflowTest']+([a.section] if a.section else []),cwd=out,check=True)
if a.keep:a.keep.mkdir(parents=True,exist_ok=True);run(a.keep)
else:
 with tempfile.TemporaryDirectory(prefix='conveyor-wand-workflow-') as d:run(d)
print('PASS bounded native gameplay proof only; no graphical RenderOverhead, full registration, full-mod build, or live server claim',flush=True)
