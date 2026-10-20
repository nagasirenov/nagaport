#!/usr/bin/env python3
"""Bounded, exact NTM constructor/builder projections against genuine native Block."""
import argparse,json,re,subprocess,sys,tempfile
from pathlib import Path
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2]
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--family-only',action='store_true');p.add_argument('--mutant',choices=['drop-assignment','wrong-argument','null-return']);p.add_argument('--mutant-owner',choices=['WasteEarth','WasteLog','WasteLeaves']);a=p.parse_args()
assert bool(a.mutant)==bool(a.mutant_owner) and __debug__
subprocess.run([sys.executable,str(HERE/'check-source.py')],check=True)
cp=a.classpath_file.read_text().strip()
def method(source,pattern):
 m=re.search(pattern,source);assert m,pattern;end,depth=m.end(),1
 while depth:
  if source[end]=='{':depth+=1
  if source[end]=='}':depth-=1
  end+=1
 return source[m.start():end]
rows=json.loads((HERE/'sites.json').read_text())
with tempfile.TemporaryDirectory(prefix='waste-terrain-native-') as d:
 t=Path(d);files=[]
 def write(name,source):
  f=t/name;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(source);files.append(f)
 for name in ['WasteEarth','WasteLog','WasteLeaves']:
  source=(ROOT/('src/main/java/com/hbm/blocks/generic/'+name+'.java')).read_text()
  constructor=method(source,r'public '+name+r'\([^)]*\)\s*\{');builder=method(source,r'public '+name+r' withSoundType\([^)]*\)\s*\{')
  if a.mutant_owner==name:
   builder=builder.replace('super.setSoundType(sound);',{'drop-assignment':';','wrong-argument':'super.setSoundType(net.minecraft.block.SoundType.STONE);','null-return':'super.setSoundType(sound);'}[a.mutant])
   if a.mutant=='null-return':builder=builder.replace('return this;','return null;')
  construction_overrides=''.join(method(source,r'public boolean '+n+r'\([^)]*\)\s*\{') for n in ['isFullCube','isOpaqueCube']) if name=='WasteLeaves' else ''
  write('com/hbm/blocks/generic/'+name+'.java','package com.hbm.blocks.generic; import net.minecraft.block.*; import net.minecraft.block.material.Material; public class '+name+' extends Block {'+constructor+builder+construction_overrides+'}')
 write('com/hbm/main/MainRegistry.java','package com.hbm.main; public final class MainRegistry { public static final net.minecraft.creativetab.CreativeTabs blockTab=net.minecraft.creativetab.CreativeTabs.BUILDING_BLOCKS; }')
 production=(ROOT/'src/main/java/com/hbm/blocks/ModBlocks.java').read_text();slices=[]
 for row in rows:
  assert production.count(row['after'])==1;text=next(x for x in production.splitlines() if x==row['after']).strip()
  if '.setBlockTextureName(' in text:text=text[:text.index('.setBlockTextureName(')]+';'
  slices.append(text)
 names=[r['name'] for r in rows]
 def values(fn):return ','.join(fn(r) for r in rows)
 generated='import net.minecraft.block.*; import net.minecraft.block.material.Material; import net.minecraft.creativetab.CreativeTabs; import com.hbm.blocks.generic.*; import com.hbm.main.MainRegistry; public class SoundConstruction { static Block '+','.join(names)+'; static Block[] create(){'+'\n'.join(slices)+'return new Block[]{'+','.join(names)+'};}'
 generated+='static SoundType[] expected={'+values(lambda r:r['sound'])+'};'
 generated+='static String[] names={'+values(lambda r:'"'+r['name']+'"')+'};'
 generated+='static Class<?>[] owners={'+values(lambda r:r['owner']+'.class')+'};'
 generated+='static Material[] materials={'+values(lambda r:re.search(r'new \w+\((Material.\w+)',r['before']).group(1))+'};'
 generated+='static float[] hardness={'+values(lambda r:re.search(r'\.setHardness\(([^)]+)\)',r['before']).group(1))+'};'
 generated+='static float[] resistance={'+values(lambda r:re.search(r'\.setResistance\(([^)]+)\)',r['before']).group(1)+'*3F/5F' if '.setResistance(' in r['before'] else re.search(r'\.setHardness\(([^)]+)\)',r['before']).group(1))+'};'
 generated+='static CreativeTabs[] tabs={'+values(lambda r:re.search(r'\.setCreativeTab\(([^)]+)\)',r['before']).group(1))+'};'
 generated+='static boolean[] ticks={'+values(lambda r:'true' if r['owner']=='WasteLeaves' or ', true)' in r['before'] else 'false')+'};'
 generated+='static int[] light={'+values(lambda r:'15' if '.setLightLevel(1F)' in r['before'] else '0')+'};}'
 write('SoundConstruction.java',generated)
 files += [HERE/'WasteTerrainSoundNativeTest.java',HERE.parent/'ForgeEventNativeLauncher.java']
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-cp',cp,'-sourcepath',str(t/'empty'),'-d',d]+list(map(str,files)),check=True)
 print('COMPILE_PASS: exact current NTM constructors/builders; all Minecraft/Forge classes genuine; construction projections only',flush=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-cp',d+':'+cp,'ForgeEventNativeLauncher','WasteTerrainSoundNativeTest'],cwd=d,check=True)
