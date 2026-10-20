#!/usr/bin/env python3
"""Bounded genuine-native pillar model/atlas proof, never a full-mod build."""
import argparse, hashlib, json, os, re, subprocess, tempfile, zipfile
from pathlib import Path
HERE=Path(__file__).resolve().parent; ROOT=HERE.parents[2]; REPO=ROOT.parent
p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--java-home',type=Path,required=True)
p.add_argument('--classpath-file',type=Path,required=True)
p.add_argument('--resources',type=Path,required=True)
p.add_argument('--legacy-jar',type=Path,required=True)
a=p.parse_args(); cp=a.classpath_file.read_text().strip()
assert hashlib.sha256(a.legacy_jar.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
def read(path):
 f=ROOT/path
 return f.read_text() if f.exists() else subprocess.check_output(['git','-C',str(REPO),'show','HEAD:ntm-port-1.12.2/'+path],text=True)
def body(s,pattern):
 m=re.search(pattern,s); assert m,pattern
 end=m.end(); depth=1
 while depth:
  if s[end]=='{': depth+=1
  elif s[end]=='}': depth-=1
  end+=1
 return s[m.start():end]
s=read('src/main/java/com/hbm/blocks/ModBlocks.java')
rows=[]
for line in s.splitlines():
 if '= ((BlockPillar) new BlockPillar' not in line: continue
 name=line.strip().split(' = ')[0]
 textures=re.findall(r'RefStrings.MODID \+ "(:[^"]+)"',line)
 assert len(textures)==2,line
 registration=re.findall(r'^\s*(?:GameRegistry.registerBlock|register)\('+name+r'(?:,.*)?\);$',s,re.M)
 assert len(registration)==1,(name,registration)
 item='ItemBlockLore' if 'ItemBlockLore' in registration[0] else ('ItemBlockBase' if registration[0].strip()=='register('+name+');' else 'ItemBlock')
 rows.append(dict(name=name,after=line.strip(),top='hbm'+textures[0],side='hbm'+textures[1],registration=registration[0].strip(),item=item,owner='BlockPillarPWR' if 'new BlockPillarPWR' in line else 'BlockPillar'))
assert len(rows)==11
(HERE/'sites.json').write_text(json.dumps(rows,indent=2)+'\n')
production=['com/hbm/blocks/machine/BlockPillar.java','com/hbm/blocks/machine/BlockPillarPWR.java','com/hbm/main/PillarModelHandler.java','com/hbm/render/model/PlainCubeModel.java','com/hbm/registry/GameRegistry.java','com/hbm/lib/RefStrings.java','com/hbm/render/icon/IIcon.java','com/hbm/render/icon/IIconRegister.java','com/hbm/render/icon/LegacyIconRegister.java','com/hbm/render/icon/LegacyTextureAtlasSprite.java','com/hbm/main/LegacyIconRegistrationHandler.java','com/hbm/render/item/NativeItemTextureRegistry.java','com/hbm/blocks/ITooltipProvider.java','com/hbm/util/i18n/I18nUtil.java','com/hbm/util/i18n/ITranslate.java','com/hbm/blocks/IBlockMulti.java','com/hbm/blocks/machine/BlockCMFlux.java','com/hbm/blocks/machine/BlockCMHeat.java']
with tempfile.TemporaryDirectory(prefix='pillar-native-') as d:
 t=Path(d); files=[]; inputs={}
 def write(name,text):
  f=t/name;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(text);files.append(f);inputs[name]=hashlib.sha256(text.encode()).hexdigest()
 for path in production: write(path,read('src/main/java/'+path))
 # Explicit native descendant-access fixture, not replacement platform classes.
 write('accessprobe/PillarAccess.java','package accessprobe; public class PillarAccess extends com.hbm.blocks.machine.BlockPillar {public PillarAccess(){super(net.minecraft.block.material.Material.ROCK, "hbm:gneiss_tile");} public String readTexture(){return textureName;} public com.hbm.render.icon.IIcon readIcon(){return blockIcon;} public void write(String name,com.hbm.render.icon.IIcon icon){textureName=name;blockIcon=icon;}}')
 # Exact item-constructor/metadata slices. Their unrelated tooltip/name/rarity bodies
 # are unchanged and source-guarded; only the selected native visual lifecycle is run.
 item=read('src/main/java/com/hbm/items/block/ItemBlockBase.java')
 write('com/hbm/items/block/ItemBlockBase.java','package com.hbm.items.block; import net.minecraft.block.Block; import com.hbm.blocks.IBlockMulti; public class ItemBlockBase extends net.minecraft.item.ItemBlock {'+body(item,r'public ItemBlockBase\(Block block\)\s*\{')+body(item,r'public int getMetadata\(int meta\)\s*\{')+'}')
 item=read('src/main/java/com/hbm/items/block/ItemBlockLore.java')
 write('com/hbm/items/block/ItemBlockLore.java','package com.hbm.items.block; import net.minecraft.block.Block; public class ItemBlockLore extends ItemBlockBase {'+body(item,r'public ItemBlockLore\(Block p_i45328_1_\)\s*\{')+'}')
 write('com/hbm/main/MainRegistry.java','package com.hbm.main; public final class MainRegistry { public static final org.apache.logging.log4j.Logger logger=org.apache.logging.log4j.LogManager.getLogger("PillarProbe"); public static final net.minecraft.creativetab.CreativeTabs blockTab=net.minecraft.creativetab.CreativeTabs.BUILDING_BLOCKS, machineTab=net.minecraft.creativetab.CreativeTabs.REDSTONE; public static final Proxy proxy=new Proxy(); public static class Proxy {public com.hbm.util.i18n.ITranslate getI18n(){throw new AssertionError("Unrelated tooltip localization outside native visual probe");}}}')
 q=lambda values:','.join(json.dumps(n) for n in values)
 names=[r['name'] for r in rows]
 write('PillarConstruction.java','import net.minecraft.block.*;import net.minecraft.block.material.*;import com.hbm.blocks.machine.*;import com.hbm.items.block.*;import com.hbm.lib.RefStrings;import com.hbm.main.MainRegistry;import com.hbm.registry.GameRegistry;public class PillarConstruction {static Block '+','.join(names)+';static Block[] create(){'+''.join(r['after'] for r in rows)+'return new Block[]{'+','.join(names)+'};} static void register(){'+''.join(r['registration'] for r in rows)+'}'+body(s,r'private static void register\(Block b\)\s*\{')+'static String[] names={'+q(names)+'},tops={'+q(r['top'] for r in rows)+'},sides={'+q(r['side'] for r in rows)+'},owners={'+q(r['owner'] for r in rows)+'},items={'+q(r['item'] for r in rows)+'};}')
 for path in ['porting/tests/block-registry-native/NativeAtlasEnvironment.java','porting/tests/ForgeEventNativeLauncher.java','porting/tests/plain-cube-depth/OriginalCubeFaces.java']:
  write(Path(path).name,read(path))
 files.append(HERE/'PillarNativeTest.java')
 for jar in cp.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for n in z.namelist():
     if n.endswith('.so') and '/' not in n:(t/n).write_bytes(z.read(n))
 (HERE/'runtime-inputs.json').write_text(json.dumps(inputs,indent=2)+'\n')
 print('SCOPE: eighteen complete production owners/helpers, eleven verbatim construction and registry statements; explicit ItemBlockBase constructor/metadata and ItemBlockLore constructor slices; MainRegistry bootstrap/localization boundary fixture.',flush=True)
 print('BOUNDARY: native jars are unchanged. No full ModBlocks, whole-mod compilation, launched client or game visual parity claim.',flush=True)
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(t/'empty'),'-cp',cp,'-d',d]+list(map(str,files)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+d,'-cp',d+os.pathsep+cp,'ForgeEventNativeLauncher','PillarNativeTest',str(a.resources),str(a.legacy_jar)],cwd=d,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)
