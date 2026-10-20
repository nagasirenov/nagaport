#!/usr/bin/env python3
"""Whole cargo owner and renderer, real native inventory/data/GL APIs; explicit train-base fixture."""
import argparse,pathlib,subprocess,tempfile,os,zipfile,importlib.util,hashlib
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args();here=pathlib.Path(__file__).resolve().parent;root=here.parents[2]
base='''package com.hbm.entity.train;
public abstract class EntityRailCarBase extends net.minecraft.entity.Entity {
 public int baseTicks,baseReads,baseWrites;public double lastRenderX,lastRenderY,lastRenderZ,renderX,renderY,renderZ;
 public EntityRailCarBase(net.minecraft.world.World world){super(world);}
 protected void entityInit(){} protected void readEntityFromNBT(net.minecraft.nbt.NBTTagCompound n){baseReads++;} protected void writeEntityToNBT(net.minecraft.nbt.NBTTagCompound n){baseWrites++;}
 public void onUpdate(){baseTicks++;}
}'''
with tempfile.TemporaryDirectory(prefix='train-cargo-native-') as d:
 out=pathlib.Path(d);files=[]
 for n in ['com/hbm/entity/train/EntityRailCarCargo.java']:
  f=root/'src/main/java'/n;files.append(f);print('WHOLE INPUT',n,hashlib.sha256(f.read_bytes()).hexdigest(),flush=True)
 f=out/'EntityRailCarBase.java';f.write_text(base);files.append(f);files.append(here/'TrainCargoNativeTest.java')
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-cp',a.classpath,'-d',d]+list(map(str,files)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-cp',d+os.pathsep+a.classpath,'TrainCargoNativeTest'],cwd=d,check=True)
 # Reuse existing genuine native GL setup and declared NTM machine collaborators,
 # without rerunning unrelated machine suites.
 files += [root/p for p in [
 'src/main/java/com/hbm/render/util/NativeItemBaseModel.java','src/main/java/com/hbm/render/util/RenderDecoItem.java','src/main/java/com/hbm/render/util/RenderItemStack.java','src/main/java/com/hbm/render/util/NativeMachineItemRenderer.java','src/main/java/com/hbm/render/util/NativeIconExtrusion.java','src/main/java/com/hbm/render/util/NativeItemQuadRenderer.java','src/main/java/com/hbm/render/util/NativeGuiItemDraw.java','src/main/java/com/hbm/render/item/nativeitem/MaterialTextureAtlasSprite.java','src/main/java/com/hbm/render/entity/item/RenderTrainCargoTramTrailer.java','porting/tests/NativeMachineItemTest.java','porting/tests/NativeMachineDrawTest.java','porting/tests/NativeMachineConsumerTest.java','porting/tests/NativeBaseModelOwnershipTest.java','porting/tests/MaterialTextureAtlasSpriteNativeTest.java']]
 spec=importlib.util.spec_from_file_location('fixtures',root/'porting/tests/machine-renderer-fixtures.py');fixtures=importlib.util.module_from_spec(spec);spec.loader.exec_module(fixtures);fixtures.add(root,out,files)
 f=out/'fixtures/com/hbm/blocks/ModBlocks.java';f.write_text(f.read_text().replace('public class ModBlocks {','public class ModBlocks { public static net.minecraft.block.Block crate=net.minecraft.init.Blocks.STONE;'))
 f=out/'fixtures/com/hbm/main/ResourceManager.java';f.write_text(f.read_text().replace('public class ResourceManager {','public class ResourceManager { public static Draw train_cargo_tram_trailer=new Draw(); public static final net.minecraft.util.ResourceLocation tram_trailer=net.minecraft.client.renderer.texture.TextureMap.LOCATION_BLOCKS_TEXTURE;'))
 import json
 layouts=json.loads((here/'original-layouts.json').read_text())['layouts']
 values='{'+','.join('{'+','.join('{'+','.join(str(x)+'D' for x in p)+'}' for p in layouts[str(i)])+'}' for i in range(5,46,5))+'}'
 f=out/'TrainCargoRenderTest.java';f.write_text((here/'TrainCargoRenderTest.java.in').read_text().replace('@LAYOUTS@',values));files.append(f)
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for n in z.namelist():
     if n.endswith('.so') and '/' not in n:(out/n).write_bytes(z.read(n))
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-cp',a.classpath,'-d',d]+list(map(str,files)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+d,'-cp',d+os.pathsep+a.classpath,'TrainCargoRenderTest'],cwd=d,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)
 # Compile the two complete verbatim nested container classes in explicit outer
 # train fixtures. The remaining outer train physics/GUI is not substituted as certified.
 container_files=[root/'src/main/java/api/hbm/energymk2/IBatteryItem.java',here/'TrainCargoContainerTest.java']
 for name,size in [('TrainCargoTram',29),('TrainCargoTramTrailer',45)]:
  source=(root/'src/main/java/com/hbm/entity/train'/(name+'.java')).read_text();start=source.index('public static class Container'+name+' extends Container {');level=0;end=None
  for i in range(source.index('{',start),len(source)):
   if source[i]=='{':level+=1
   elif source[i]=='}':
    level-=1
    if level==0:end=i+1;break
  assert end is not None
  body=source[start:end]
  fixture='package com.hbm.entity.train; import net.minecraft.inventory.*;import net.minecraft.item.*;import net.minecraft.entity.player.*;import api.hbm.energymk2.IBatteryItem;public class '+name+' extends EntityRailCarCargo {public '+name+'(){super(null);}public int getSizeInventory(){return '+str(size)+';}public String getInventoryName(){return "container.fixture";}'+body+'}'
  f=out/(name+'.java');f.write_text(fixture);container_files.append(f)
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-cp',d+os.pathsep+a.classpath,'-sourcepath','', '-d',d]+list(map(str,container_files)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-cp',d+os.pathsep+a.classpath,'TrainCargoContainerTest'],cwd=d,check=True)
