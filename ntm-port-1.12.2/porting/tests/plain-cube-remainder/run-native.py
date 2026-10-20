#!/usr/bin/env python3
"""Native model proof; explicit NTM-only construction slices, no Forge/Minecraft substitutes."""
import argparse,hashlib,json,os,re,subprocess,tempfile,zipfile
from pathlib import Path
HERE=Path(__file__).resolve().parent; ROOT=HERE.parents[2]
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--resources',type=Path,required=True);p.add_argument('--legacy-jar',type=Path,required=True);args=p.parse_args()
JAVA=args.java_home;CP=args.classpath_file.read_text().strip()
assert hashlib.sha256(args.legacy_jar.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
for r in json.loads((HERE/'resource-audit.json').read_text()):assert hashlib.sha256((args.resources/r['path'].removeprefix('src/main/resources/')).read_bytes()).hexdigest()==r['sha256']
rows=json.loads((HERE/'sites.json').read_text())
def source(path):return (ROOT/'src/main/java'/path).read_text()
def body(s,pattern):
 m=re.search(pattern,s);assert m,pattern
 depth=1;end=m.end()
 while depth:
  if s[end]=='{':depth+=1
  if s[end]=='}':depth-=1
  end+=1
 return s[m.start():end]
with tempfile.TemporaryDirectory(prefix='plain-remainder-native-') as d:
 t=Path(d);files=[]
 def write(name,s):
  p=t/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(s);files.append(p)
 # NTM-only exact construction slices. Gameplay bodies are untouched and not claimed compiled.
 for owner,parent in [('BlockHazard','Block'),('BlockHazardFalling','BlockFalling'),('BlockOutgas','Block'),('BlockNetherCoal','BlockOutgas'),('BlockNuclearWaste','BlockHazard'),('BlockSpeedy','Block'),('BlockRadResistant','Block'),('BlockMeteoriteTreasure','Block'),('FragileBrick','Block')]:
  s=source('com/hbm/blocks/generic/'+owner+'.java')
  # Turn the exact matched declaration into a regex safely when extracting.
  members=[]
  for m in re.finditer(r'public '+owner+r'\([^)]*\)\s*\{',s):members.append(body(s,re.escape(m.group())))
  fields=''
  if owner in ['BlockHazard','BlockHazardFalling']:
   fields='\n'.join(re.findall(r'^\s*(?:(?:protected|private) float rad|private ExtDisplayEffect extEffect|private boolean beaconable)\s*=.*?;',s,re.M))
   for method in ['withSoundType','makeBeaconable']:
    members.append(body(s,r'public '+owner+' '+method+r'\([^)]*\)\s*\{'))
   if owner=='BlockHazard':
    members.append(body(s,r'public BlockHazard setDisplayEffect\([^)]*\)\s*\{'));members.append(body(s,r'public static enum ExtDisplayEffect\s*\{'))
  elif owner=='BlockSpeedy':fields='double speed;'
  elif owner=='BlockOutgas':fields='boolean randomTick,onBreak,onNeighbour;int rate;'
  write('com/hbm/blocks/generic/'+owner+'.java','package com.hbm.blocks.generic;import net.minecraft.block.*;import net.minecraft.block.material.Material;public class '+owner+' extends '+parent+' {'+fields+'\n'+'\n'.join(members)+'}')
 # This selected owner compiles as a whole, and the vanilla BlockFalling owner is never substituted.
 files.append(ROOT/'src/main/java/com/hbm/blocks/generic/BlockNoDrop.java')
 s=source('com/hbm/items/block/ItemBlockBase.java')
 members=[body(s,r'public ItemBlockBase\(Block block\)\s*\{'),body(s,r'public int getMetadata\(int meta\)\s*\{')]
 write('com/hbm/items/block/ItemBlockBase.java','package com.hbm.items.block;import net.minecraft.block.Block;import com.hbm.blocks.IBlockMulti;public class ItemBlockBase extends net.minecraft.item.ItemBlock {'+'\n'.join(members)+'}')
 s=source('com/hbm/items/block/ItemBlockBlastInfo.java')
 write('com/hbm/items/block/ItemBlockBlastInfo.java','package com.hbm.items.block;import net.minecraft.block.Block;public class ItemBlockBlastInfo extends ItemBlockBase {'+body(s,r'public ItemBlockBlastInfo\(Block block\)\s*\{')+'}')
 names=[r['name'] for r in rows];q=lambda a:','.join('"'+n+'"' for n in a)
 write('CubeConstruction.java','import net.minecraft.block.*;import net.minecraft.block.material.*;import com.hbm.blocks.generic.*;import com.hbm.blocks.generic.BlockHazard.ExtDisplayEffect;import com.hbm.lib.RefStrings;public class CubeConstruction {static class MainRegistry {static net.minecraft.creativetab.CreativeTabs blockTab;}static Block '+','.join(names)+';static Block[] create() {'+'\n'.join(r['after'] for r in rows)+'return new Block[]{'+','.join(names)+'};}static String[] names={'+q(names)+'};static String[] textures={'+q([r['texture'] for r in rows])+'};static String[] owners={'+q([r['owner'] for r in rows])+'};static boolean[] baseItems={'+','.join(str(r['item']!='ItemBlock').lower() for r in rows)+'};static String[] items={'+q([r['item'] for r in rows])+'};static float[] hardness={'+','.join(re.search(r'setHardness\(([^)]+)\)',r['after'])[1] for r in rows)+'};static float[] resistance={'+','.join((re.search(r'setResistance\(([^)]+)\)',r['after'])[1]+'*3') if 'setResistance' in r['after'] else (re.search(r'setHardness\(([^)]+)\)',r['after'])[1]+'*5') for r in rows)+'};static String trace="";static Block receiver(Block b){trace+="receiver,";return b;}static String texture(String s){trace+="texture,";return s;}}')
 for j in CP.split(':'):
  if 'lwjgl-platform' in j and j.endswith('natives-linux.jar'):
   with zipfile.ZipFile(j) as z:
    for n in z.namelist():
     if n.endswith('.so') and '/' not in n:(t/n).write_bytes(z.read(n))
 files += [ROOT/'src/main/java'/n for n in ['com/hbm/blocks/IBlockMulti.java','com/hbm/blocks/PlainCubeTextures.java','com/hbm/render/model/PlainCubeModel.java','com/hbm/main/PlainCubeModelHandler.java','com/hbm/lib/RefStrings.java']]
 files += [HERE/'PlainCubeNativeTest.java',HERE/'OriginalCubeFaces.java',HERE.parent/'block-registry-native/NativeAtlasEnvironment.java',HERE.parent/'ForgeEventNativeLauncher.java']
 subprocess.run([str(JAVA/'bin/javac'),'-cp',CP,'-sourcepath',str(t/'empty'),'-d',d]+list(map(str,files)),check=True)
 subprocess.run([str(JAVA/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+d,'-cp',d+':'+CP,'ForgeEventNativeLauncher','PlainCubeNativeTest',str(args.resources),str(args.legacy_jar)],cwd=d,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)
