#!/usr/bin/env python3
import argparse,hashlib,json,os,re,subprocess,tempfile,zipfile
from pathlib import Path
HERE=Path(__file__).resolve().parent; ROOT=HERE.parents[2]
parser=argparse.ArgumentParser();parser.add_argument('--java-home',type=Path,required=True);parser.add_argument('--classpath-file',type=Path,required=True);parser.add_argument('--resources',type=Path,required=True);parser.add_argument('--legacy-jar',type=Path,required=True);args=parser.parse_args()
JAVA=args.java_home; CP=args.classpath_file.read_text().strip()
assert hashlib.sha256(args.legacy_jar.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
for resource in json.loads((HERE/'resource-audit.json').read_text()):
 assert hashlib.sha256((args.resources/resource['path'].removeprefix('src/main/resources/')).read_bytes()).hexdigest()==resource['sha256']
rows=json.loads((HERE/'sites.json').read_text())
with tempfile.TemporaryDirectory(prefix='plain-cube-native-') as d:
 t=Path(d); files=[]
 def write(name,s):
  p=t/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(s);files.append(p)
 for name in ['BlockOutgas','BlockSmolder','BlockStepEffect']:
  source=(ROOT/f'src/main/java/com/hbm/blocks/generic/{name}.java').read_text();bodies=[]
  for m in re.finditer(r'public '+name+r'\([^)]*\)\s*\{',source):
   end=source.index('}',m.end());bodies.append(source[m.start():end+1])
  fields='boolean randomTick,onBreak,onNeighbour; int rate;' if name=='BlockOutgas' else ''
  if name=='BlockStepEffect':
   fields+='public boolean allowFortune = true;'; m=re.search(r'public BlockStepEffect noFortune\(\)\s*\{[^}]+\}',source);bodies.append(m[0])
  write(f'com/hbm/blocks/generic/{name}.java','package com.hbm.blocks.generic; import net.minecraft.block.Block; import net.minecraft.block.material.Material; public class '+name+' extends Block {'+fields+'\n'+'\n'.join(bodies)+'}')
 # NTM-only item visual/placement slices. Unrelated tooltip/rarity callbacks remain unported.
 for name,package,parent in [('ItemBlockBase','com.hbm.items.block','net.minecraft.item.ItemBlock'),('ItemBlockLore','com.hbm.items.block','ItemBlockBase'),('ItemOreBlock','com.hbm.items.special','net.minecraft.item.ItemBlock')]:
  source=(ROOT/'src/main/java'/Path(package.replace('.','/'))/(name+'.java')).read_text();bodies=[]
  for m in re.finditer(r'public '+name+r'\([^)]*\)\s*\{',source):
   depth=1;end=m.end()
   while depth:
    if source[end]=='{':depth+=1
    if source[end]=='}':depth-=1
    end+=1
   bodies.append(source[m.start():end])
  if name=='ItemBlockBase':
   m=re.search(r'public int getMetadata\(int meta\)\s*\{.*?\n\t}',source,re.S);assert m;bodies.append(m[0])
  write(package.replace('.','/')+'/'+name+'.java','package '+package+'; import net.minecraft.block.Block; import com.hbm.blocks.IBlockMulti; public class '+name+' extends '+parent+' {'+'\n'.join(bodies)+'}')
 files.append(ROOT/'src/main/java/com/hbm/blocks/IBlockMulti.java')
 names=[re.search(r'(\w+) = ',r['after'])[1] for r in rows];textures=[re.search(r'RefStrings.MODID \+ ":([^"]+)"',r['after'])[1] for r in rows]
 write('CubeConstruction.java','import net.minecraft.block.*; import net.minecraft.block.material.*; import com.hbm.blocks.generic.*; import com.hbm.lib.RefStrings; public class CubeConstruction { static class MainRegistry { static net.minecraft.creativetab.CreativeTabs blockTab; } static Block '+','.join(names)+'; static Block[] create() {'+'\n'.join(r['after'] for r in rows)+'return new Block[]{'+','.join(names)+'};} static String[] names={'+','.join('"'+n+'"' for n in names)+'};static String[] textures={'+','.join('"'+n+'"' for n in textures)+'};}')
 for j in CP.split(':'):
  if 'lwjgl-platform' in j and j.endswith('natives-linux.jar'):
   with zipfile.ZipFile(j) as z:
    for n in z.namelist():
     if n.endswith('.so') and '/' not in n:(t/n).write_bytes(z.read(n))
 files += [ROOT/'src/main/java'/n for n in ['com/hbm/blocks/PlainCubeTextures.java','com/hbm/render/model/PlainCubeModel.java','com/hbm/main/PlainCubeModelHandler.java','com/hbm/lib/RefStrings.java']]
 files += [HERE/'PlainCubeNativeTest.java', HERE/'OriginalCubeFaces.java', HERE.parent/'block-registry-native/NativeAtlasEnvironment.java',HERE.parent/'ForgeEventNativeLauncher.java']
 subprocess.run([str(JAVA/'bin/javac'),'-cp',CP,'-sourcepath',str(t/'empty'),'-d',d]+list(map(str,files)),check=True)
 subprocess.run([str(JAVA/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+d,'-cp',d+':'+CP,'ForgeEventNativeLauncher','PlainCubeNativeTest',str(args.resources),str(args.legacy_jar)],cwd=d,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)
