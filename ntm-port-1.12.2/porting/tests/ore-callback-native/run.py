#!/usr/bin/env python3
import argparse,hashlib,json,re,subprocess,tempfile,zipfile
from pathlib import Path
H=Path(__file__).resolve().parent;M=H.parents[2];SRC=M/'src/main/java'
def extract(s,sig):
 start=s.index(sig);i=s.index('{',start)+1;n=1
 while n:n+=(s[i]=='{')-(s[i]=='}');i+=1
 return s[start:i]
def generate(t):
 files=[]
 def fixture(path,body):
  p=t/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(body);files.append(p);return p
 ore=(SRC/'com/hbm/blocks/generic/BlockOre.java').read_text();cluster=(SRC/'com/hbm/blocks/generic/BlockCluster.java').read_text()
 blocknames=sorted(set(re.findall(r'ModBlocks\.(\w+)',ore+cluster))|{'deco_titanium'})
 itemnames=sorted(set(re.findall(r'ModItems\.(\w+)',ore+cluster)))
 fixture('com/hbm/blocks/ModBlocks.java','package com.hbm.blocks;import net.minecraft.block.Block;public class ModBlocks {public static Block '+','.join(blocknames)+';}')
 fixture('com/hbm/items/ModItems.java','package com.hbm.items;import net.minecraft.item.Item;public class ModItems {public static Item '+','.join(itemnames)+';}')
 fixture('com/hbm/config/SpaceConfig.java','package com.hbm.config;public class SpaceConfig {public static boolean showOreLocations; public static int orbitDimension;}')
 fixture('com/hbm/util/i18n/I18nUtil.java','package com.hbm.util.i18n;public class I18nUtil { public static String resolveKey(String key,Object...args){return key;} public static String[] resolveKeyArray(String key,Object...args){return new String[]{key};}}')
 # Actual full enum bodies, not enum ordinal/name substitutes. Their unexercised astronomical collaborator is explicit.
 body=extract((SRC/'com/hbm/dim/SolarSystem.java').read_text(),'public enum Body {')
 fixture('com/hbm/dim/SolarSystem.java','package com.hbm.dim;import net.minecraft.util.ResourceLocation;import com.hbm.config.SpaceConfig;public class SolarSystem {\n'+body+'\n}')
 fixture('com/hbm/dim/CelestialBody.java','package com.hbm.dim;import net.minecraft.util.ResourceLocation;public class CelestialBody {public ResourceLocation stoneTexture,surfaceTexture;public int dimensionId;public static CelestialBody getBody(String name){throw new AssertionError("Astronomy collaborator not exercised");}public int getProcessingLevel(CelestialBody from){throw new AssertionError("Astronomy collaborator not exercised");}}')
 fixture('com/hbm/items/ItemEnums.java','package com.hbm.items;public class ItemEnums {\n'+extract((SRC/'com/hbm/items/ItemEnums.java').read_text(),'public static enum EnumChunkType {')+'\n}')
 with zipfile.ZipFile(H/'baseline.zip') as z:old=z.read('BlockOre.java').decode()
 signatures=['public Item getItemDropped(int','public int quantityDropped(Random','public int quantityDroppedWithBonus(','public int damageDropped(int','public int getSubCount()']
 methods='\n'.join(extract(old,sig).replace('ModBlocks.','OracleRegistry.') for sig in signatures)
 fixture('OriginalOreOracle.java','import java.util.*;import net.minecraft.block.Block;import net.minecraft.block.material.Material;import net.minecraft.item.Item;import net.minecraft.init.Items;import com.hbm.items.ModItems;import com.hbm.items.ItemEnums.EnumChunkType;import com.hbm.dim.SolarSystem;import com.hbm.blocks.IBlockMulti;public class OriginalOreOracle extends Block implements IBlockMulti {public boolean allowFortune=true;public OriginalOreOracle(){super(Material.ROCK);}\n'+methods+'\n}')
 fixture('OracleRegistry.java','import net.minecraft.block.Block;public class OracleRegistry {public static Block '+','.join(blocknames)+';}')
 # Exact titanium constructor; registry and creative-tab fixture are explicit, no other ModBlocks initialization implied.
 ctor=re.findall(r'^\s*deco_titanium = .*;$',(SRC/'com/hbm/blocks/ModBlocks.java').read_text(),re.M);assert len(ctor)==1
 fixture('com/hbm/main/MainRegistry.java','package com.hbm.main;public class MainRegistry {public static final net.minecraft.creativetab.CreativeTabs blockTab=net.minecraft.creativetab.CreativeTabs.BUILDING_BLOCKS;}')
 fixture('TitaniumConstruction.java','import net.minecraft.block.Block;import net.minecraft.block.material.Material;import com.hbm.blocks.generic.BlockOre;import com.hbm.main.MainRegistry;public class TitaniumConstruction {public static Block deco_titanium;public static void initialize(){'+ctor[0].strip()+'}}')
 initialization='\n'.join('ModItems.'+n+'=new Item().setRegistryName("hbm","'+n+'");ForgeRegistries.ITEMS.register(ModItems.'+n+');' for n in itemnames)
 initialization+='\n'+ '\n'.join('add("'+n+'", '+('true' if n.startswith('cluster_') else 'false')+');' for n in blocknames)
 fixture('OreNativeTest.java',(H/'OreNativeTest.java.in').read_text().replace('INITIALIZERS',initialization))
 production=[SRC/p for p in ['com/hbm/blocks/generic/BlockOre.java','com/hbm/blocks/generic/BlockCluster.java','com/hbm/blocks/IBlockMulti.java','com/hbm/blocks/IBlockMultiPass.java','com/hbm/blocks/ITooltipProvider.java','com/hbm/blocks/MultipassContext.java']]
 return files+production

def main():
 p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--legacy-client',type=Path,required=True);p.add_argument('--mutants',action='store_true');a=p.parse_args();cp=a.classpath_file.read_text().strip();assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=="a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce"
 with tempfile.TemporaryDirectory(prefix='ore-callback-') as td:
  t=Path(td);files=generate(t);subprocess.run(list(map(str,[a.java_home/'bin/javac','-encoding','UTF-8','-sourcepath',t,'-cp',cp,'-d',t]+files)),check=True);subprocess.run(list(map(str,[a.java_home/'bin/java','-Xmx500m','-cp',str(t)+':'+cp,'OreNativeTest',a.legacy_client])),check=True,cwd=t)
  dis=subprocess.check_output(list(map(str,[a.java_home/'bin/javap','-c','-p','-classpath',t,'com.hbm.blocks.generic.BlockOre'])),text=True)
  assert 'net/minecraft/world/World.setBlockState:' in dis and 'net/minecraft/world/chunk/Chunk.markDirty:' in dis
  (H/'native-attribution.log').write_text(dis)
  if a.mutants:
   original=SRC/'com/hbm/blocks/generic/BlockOre.java';source=original.read_text();mutant=t/'com/hbm/blocks/generic/BlockOre.java';mutant.parent.mkdir(parents=True,exist_ok=True)
   changes=[('return getItemDropped(getMetaFromState(state), rand, fortune);','return Item.getItemFromBlock(this);'),('return damageDropped(getMetaFromState(state));','return 0;'),('i < getSubCount()','i < 1'),('chunk.markDirty();',''),('if(chunk.isPopulated()) world.notifyBlockUpdate(pos, current, changed, 2);',''),('world.setBlockState(pos, changed, 2);','world.setBlockState(pos, changed, 3);')]
   for index,(old,new) in enumerate(changes):
    assert source.count(old)==1;mutant.write_text(source.replace(old,new));subprocess.run(list(map(str,[a.java_home/'bin/javac','-encoding','UTF-8','-sourcepath',t,'-cp',str(t)+':'+cp,'-d',t,mutant])),check=True,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL)
    result=subprocess.run(list(map(str,[a.java_home/'bin/java','-Xmx500m','-cp',str(t)+':'+cp,'OreNativeTest',a.legacy_client])),cwd=t,capture_output=True,text=True)
    assert result.returncode!=0 and 'java.lang.AssertionError:' in result.stderr,(index,result.stdout,result.stderr)
    print('PASS mutant rejected:',index,next(line for line in result.stderr.splitlines() if 'java.lang.AssertionError:' in line))
if __name__=='__main__':main()
