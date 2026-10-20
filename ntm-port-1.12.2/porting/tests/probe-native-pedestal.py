#!/usr/bin/env python3
"""Whole selected production classes against native Forge; explicit nonselected NTM collaborators only."""
import argparse,pathlib,subprocess,tempfile,os,hashlib,json,zipfile
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
r=pathlib.Path(__file__).resolve().parents[2];s=r/'src/main/java'
prod=['blocks/generic/BlockPedestal','render/block/RenderPedestal','render/block/nativeblock/PedestalBakedModel','render/block/nativeblock/ConveyorCuboidGeometry','render/block/nativeblock/ThermalModelTransforms','main/PedestalModelHandler','lib/RefStrings','lib/ForgeDirection']
collab={
'com/hbm/dim/WorldProviderCelestial.java':'package com.hbm.dim; public class WorldProviderCelestial extends net.minecraft.world.WorldProviderSurface { public boolean isEclipse(){return false;} }',
'com/hbm/extprop/HbmPlayerProps.java':'package com.hbm.extprop; public class HbmPlayerProps { public int reputation; public static HbmPlayerProps getData(net.minecraft.entity.player.EntityPlayer p){return new HbmPlayerProps();} }',
'com/hbm/main/MainRegistry.java':'package com.hbm.main; public class MainRegistry { public static net.minecraft.stats.StatBase statLegendary; }',
'com/hbm/particle/helper/ExplosionSmallCreator.java':'package com.hbm.particle.helper; public class ExplosionSmallCreator {public static int calls;public static void composeEffect(net.minecraft.world.World w,double x,double y,double z,int n,float s,float f){calls++;}}',
'com/hbm/inventory/recipes/PedestalRecipes.java':'''package com.hbm.inventory.recipes; public class PedestalRecipes {
public static java.util.List<PedestalRecipe> recipes=new java.util.ArrayList<>();
public enum Extra {NONE,FULL_MOON,NEW_MOON,SUN,BAD_KARMA,GOOD_KARMA}
public static class Ingredient { public int stacksize; public net.minecraft.item.Item item; public Ingredient(net.minecraft.item.Item i,int n){item=i;stacksize=n;} public boolean matchesRecipe(net.minecraft.item.ItemStack s,boolean b){return s.getItem()==item;} }
public static class PedestalRecipe {public Extra extra=Extra.NONE;public Ingredient[] input=new Ingredient[9];public net.minecraft.item.ItemStack output;}
}'''}
original=r.parent/'ntm-original-pristine';manifest=r/'porting/tests/pedestal-original-manifest.json'
for f,h in json.loads(manifest.read_text()).items():assert hashlib.sha256((original/f).read_bytes()).hexdigest()==h,f
old=(original/'src/main/java/com/hbm/blocks/generic/BlockPedestal.java').read_text();new=(s/'com/hbm/blocks/generic/BlockPedestal.java').read_text()
for marker,end in [('/// EXTRA CONDITIONS ///','/// CHECK ITEMS ///')]:
 assert old[old.index(marker):old.index(end)].split()==new[new.index(marker):new.index(end)].split(),'Original environmental/karma predicates changed'
for name in ['pedestal_top','pedestal_side']:
 f='src/main/resources/assets/hbm/textures/blocks/'+name+'.png';assert (original/f).read_bytes()==(r/f).read_bytes()
print('PASS original pinned owner, renderer, recipes and unchanged texture assets; exact environmental/karma predicate oracle',flush=True)
with tempfile.TemporaryDirectory(prefix='native-pedestal-') as tmp:
 files=[str(s/('com/hbm/'+x+'.java')) for x in prod]+[str(r/'porting/tests/NativePedestalTest.java')]
 for path,body in collab.items():
  f=pathlib.Path(tmp)/path;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(body);files.append(str(f))
 subprocess.run([str(a.java_home/'bin/javac'),'-source','8','-target','8','-encoding','UTF-8','-sourcepath',tmp,'-cp',a.classpath,'-d',tmp]+files,check=True)
 print('PASS 8 complete production classes; 5 explicit unrelated NTM collaborators; no owner/Minecraft/Forge replacements',flush=True)
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:z.extractall(tmp)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.library.path='+tmp,'-cp',tmp+os.pathsep+a.classpath,'NativePedestalTest',str(original)],check=True)
