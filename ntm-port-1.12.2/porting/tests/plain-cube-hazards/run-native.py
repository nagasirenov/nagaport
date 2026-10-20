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
with tempfile.TemporaryDirectory(prefix='plain-hazard-native-') as d:
 t=Path(d);files=[]
 def write(name,s):
  p=t/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(s);files.append(p)
 # Retain exact production field initializers, constructors, and every used builder.
 s=source('com/hbm/blocks/generic/BlockHazard.java')
 fields='\n'.join(re.findall(r'^\s*(?:protected float rad|private ExtDisplayEffect extEffect|private boolean beaconable)\s*=.*?;',s,re.M))
 members=[body(s,r'public BlockHazard\(\)\s*\{'),body(s,r'public BlockHazard\(Material mat\)\s*\{')]
 for method in ['setDisplayEffect','withSoundType','makeBeaconable']:members.append(body(s,r'public BlockHazard '+method+r'\([^)]*\)\s*\{'))
 members.append(body(s,r'public static enum ExtDisplayEffect\s*\{'))
 write('com/hbm/blocks/generic/BlockHazard.java','package com.hbm.blocks.generic; import net.minecraft.block.Block;import net.minecraft.block.material.Material; public class BlockHazard extends Block {'+fields+'\n'+'\n'.join(members)+'}')
 hot=source('com/hbm/blocks/generic/BlockHotHazard.java');assert 'class BlockHotHazard extends BlockHazard {' in hot and not re.search(r'\bBlockHotHazard\s*\(',hot)
 write('com/hbm/blocks/generic/BlockHotHazard.java','package com.hbm.blocks.generic; public class BlockHotHazard extends BlockHazard {}')
 # Actual complete native sound code, with only unrelated application bootstrap isolated.
 write('com/hbm/main/MainRegistry.java','package com.hbm.main;public class MainRegistry {public static final MainRegistry instance=new MainRegistry();public final java.util.Random rand=new java.util.Random(0);}')
 s=source('com/hbm/items/block/ItemBlockBase.java')
 members=[body(s,r'public ItemBlockBase\(Block block\)\s*\{'),body(s,r'public int getMetadata\(int meta\)\s*\{')]
 write('com/hbm/items/block/ItemBlockBase.java','package com.hbm.items.block;import net.minecraft.block.Block;import com.hbm.blocks.IBlockMulti;public class ItemBlockBase extends net.minecraft.item.ItemBlock {'+'\n'.join(members)+'}')
 names=[r['name'] for r in rows];q=lambda a:','.join('"'+n+'"' for n in a)
 write('CubeConstruction.java','import net.minecraft.block.*;import net.minecraft.block.material.*;import com.hbm.blocks.generic.*;import com.hbm.blocks.generic.BlockHazard.ExtDisplayEffect;import com.hbm.blocks.ModSoundTypes;import com.hbm.lib.RefStrings;public class CubeConstruction {static class MainRegistry {static net.minecraft.creativetab.CreativeTabs blockTab;}static Block '+','.join(names)+';static Block[] create() {'+'\n'.join(r['after'] for r in rows)+'return new Block[]{'+','.join(names)+'};}static String[] names={'+q(names)+'};static String[] textures={'+q([r['texture'] for r in rows])+'};static String[] owners={'+q([r['owner'] for r in rows])+'};static boolean[] baseItems={'+','.join(str(r['item']=='ItemBlockBase').lower() for r in rows)+'};static boolean[] sounded={'+','.join(str('withSoundType' in r['after']).lower() for r in rows)+'};static String trace="";static Block receiver(Block b){trace+="receiver,";return b;}static String texture(String s){trace+="texture,";return s;}}')
 for j in CP.split(':'):
  if 'lwjgl-platform' in j and j.endswith('natives-linux.jar'):
   with zipfile.ZipFile(j) as z:
    for n in z.namelist():
     if n.endswith('.so') and '/' not in n:(t/n).write_bytes(z.read(n))
 files += [ROOT/'src/main/java'/n for n in ['com/hbm/blocks/IBlockMulti.java','com/hbm/blocks/PlainCubeTextures.java','com/hbm/render/model/PlainCubeModel.java','com/hbm/main/PlainCubeModelHandler.java','com/hbm/lib/RefStrings.java','com/hbm/blocks/ModSoundType.java','com/hbm/blocks/ModSoundTypes.java','com/hbm/main/ModSoundEvents.java']]
 files += [HERE/'PlainCubeNativeTest.java',HERE/'OriginalCubeFaces.java',HERE.parent/'block-registry-native/NativeAtlasEnvironment.java',HERE.parent/'ForgeEventNativeLauncher.java']
 subprocess.run([str(JAVA/'bin/javac'),'-cp',CP,'-sourcepath',str(t/'empty'),'-d',d]+list(map(str,files)),check=True)
 subprocess.run([str(JAVA/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+d,'-cp',d+':'+CP,'ForgeEventNativeLauncher','PlainCubeNativeTest',str(args.resources),str(args.legacy_jar)],cwd=d,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)
