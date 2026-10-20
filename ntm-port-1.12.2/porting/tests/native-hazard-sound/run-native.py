#!/usr/bin/env python3
"""Finite exact NTM construction slices, genuine native dependencies; no platform stubs."""
import argparse, hashlib, json, re, subprocess, tempfile
from pathlib import Path
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2]
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--legacy-jar',type=Path,required=True);p.add_argument('--mutant',choices=['drop-assignment','wrong-argument','null-return']);a=p.parse_args();cp=a.classpath_file.read_text().strip()
assert hashlib.sha256(a.legacy_jar.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
def body(s,pattern):
 m=re.search(pattern,s);assert m,pattern;end=m.end();depth=1
 while depth:
  if s[end]=='{':depth+=1
  if s[end]=='}':depth-=1
  end+=1
 return s[m.start():end]
rows=json.loads((HERE/'sites.json').read_text())
with tempfile.TemporaryDirectory(prefix='hazard-sound-native-') as d:
 t=Path(d);files=[]
 def write(name,s):
  p=t/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(s);files.append(p)
 for name,parent in [('BlockHazard','Block'),('BlockHazardFalling','BlockFalling')]:
  s=(ROOT/f'src/main/java/com/hbm/blocks/generic/{name}.java').read_text()
  if a.mutant=='drop-assignment':s=s.replace('super.setSoundType(sound);',';')
  if a.mutant=='wrong-argument':s=s.replace('super.setSoundType(sound);','super.setSoundType(net.minecraft.block.SoundType.STONE);')
  if a.mutant=='null-return':s=s.replace('super.setSoundType(sound);\n\t\treturn this;','super.setSoundType(sound);\n\t\treturn null;')
  fields=re.findall(r'\b(?:protected|private)\s+(?:float|boolean|ExtDisplayEffect)\s+\w+\s*=[^;]+;',s)
  bodies=[body(s,r'public '+name+r'\(\)\s*\{'),body(s,r'public '+name+r'\(Material mat\)\s*\{'),body(s,r'public '+name+r' makeBeaconable\(\)\s*\{'),body(s,r'public '+name+r' withSoundType\([^)]*\)\s*\{')]
  if name=='BlockHazard':bodies += [body(s,r'public BlockHazard setDisplayEffect\([^)]*\)\s*\{'),body(s,r'public static enum ExtDisplayEffect\s*\{')]
  write('com/hbm/blocks/generic/'+name+'.java','package com.hbm.blocks.generic; import net.minecraft.block.*; import net.minecraft.block.material.Material; public class '+name+' extends '+parent+' {'+'\n'.join(fields+bodies)+'}')
 hot=(ROOT/'src/main/java/com/hbm/blocks/generic/BlockHotHazard.java').read_text();assert 'extends BlockHazard {' in hot and not re.search(r'\bBlockHotHazard\s*\(',hot)
 write('com/hbm/blocks/generic/BlockHotHazard.java','package com.hbm.blocks.generic; public class BlockHotHazard extends BlockHazard {}')
 # MainRegistry only contributes an existing tab and RNG to native constructors/ModSoundTypes.
 write('com/hbm/main/MainRegistry.java','package com.hbm.main; public final class MainRegistry { public static final MainRegistry instance=new MainRegistry(); public final java.util.Random rand=new java.util.Random(1); public static final net.minecraft.creativetab.CreativeTabs blockTab=net.minecraft.creativetab.CreativeTabs.BUILDING_BLOCKS; }')
 names=[r['name'] for r in rows]
 slices=[]
 for r in rows:
  text=r['after'].strip();text=text[:text.index('.setBlockTextureName(')]+';';slices.append(text)
 write('HazardConstruction.java','import net.minecraft.block.*; import com.hbm.blocks.*; import com.hbm.blocks.generic.*; import com.hbm.blocks.generic.BlockHazard.ExtDisplayEffect; import com.hbm.main.MainRegistry; public class HazardConstruction { static Block '+','.join(names)+'; static Block[] create(){'+'\n'.join(slices)+'return new Block[]{'+','.join(names)+'};} static SoundType[] expected={'+','.join(r['sound'] for r in rows)+'}; static String[] names={'+','.join('"'+n+'"' for n in names)+'};}')
 files += [ROOT/'src/main/java'/f for f in ['com/hbm/blocks/ModSoundTypes.java','com/hbm/blocks/ModSoundType.java','com/hbm/main/ModSoundEvents.java','com/hbm/lib/RefStrings.java']]
 files += [HERE/'HazardSoundNativeTest.java',HERE.parent/'ForgeEventNativeLauncher.java',HERE.parent/'LegacyBlockSoundOracle.java']
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-cp',cp,'-sourcepath',str(t/'empty'),'-d',d]+list(map(str,files)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-cp',d+':'+cp,'LegacyBlockSoundOracle',str(a.legacy_jar)],cwd=d,check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-cp',d+':'+cp,'ForgeEventNativeLauncher','HazardSoundNativeTest',str(a.legacy_jar)],cwd=d,check=True)
