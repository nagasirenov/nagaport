#!/usr/bin/env python3
import argparse,hashlib,json,re,subprocess,tempfile,sys
from pathlib import Path
H=Path(__file__).resolve().parent;R=H.parents[2]
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--legacy-jar',type=Path,required=True);p.add_argument('--out',type=Path,required=True);p.add_argument('--mutant',choices=['fullcube','null-collision','offset-twice','float-rounding','old-signature']);a=p.parse_args();cp=a.classpath_file.read_text().strip();out=a.out;out.mkdir(parents=True,exist_ok=True);src=out/'src';classes=out/'classes';classes.mkdir(exist_ok=True);oldclasses=out/'oldclasses';oldclasses.mkdir(exist_ok=True)
assert hashlib.sha256(a.legacy_jar.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
subprocess.run([sys.executable,str(H/'verify.py'),str(src)],check=True)
def write(n,s):
 f=src/n;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(s);return f
write('com/hbm/blocks/ModBlocks.java','package com.hbm.blocks; public class ModBlocks { public static net.minecraft.block.Block salted_fallout; }')
write('com/hbm/dim/trait/CBT_Atmosphere.java','package com.hbm.dim.trait; public class CBT_Atmosphere {}')
if a.mutant:
 for f in (src/'com/hbm/blocks').rglob('*.java'):
  t=f.read_text()
  if a.mutant=='fullcube':t=t.replace('return shape;','return FULL_BLOCK_AABB;')
  if a.mutant=='null-collision' and f.stem=='BlockMush':t=t.replace('return null;','return shape;')
  if a.mutant=='offset-twice':t=t.replace('return shape;','return shape.offset(pos);')
  if a.mutant=='float-rounding':t=t.replace('float f = 0.2F;','double f = 0.2;')
  if a.mutant=='old-signature':t=t.replace('@Override public net.minecraft.util.math.AxisAlignedBB getBoundingBox','public net.minecraft.util.math.AxisAlignedBB oldBoundingBox')
  f.write_text(t)
rows=json.loads((H/'sites.json').read_text());oldsrc=[]
for i,r in enumerate(rows):
 t=(R.parent/'ntm-original-pristine'/r['path']).read_text();name=Path(r['path']).stem
 # Strictly audited geometry-only original constructor projection.
 ctor=t[t.index('public '+name+'('):];ctor=ctor[:ctor.index('\n\t}')]
 geometry=re.findall(r'(?:float f = [^;]+;|(?:this\.)?setBlockBounds\([^;]+;)',ctor)
 assert geometry
 body='\n'.join(geometry).replace('setBlockBounds(', 'a(')
 parent='ajr' if name=='BlockCrop' else 'aji'
 init='super();' if parent=='ajr' else 'super(awt.e);'
 null='public azt a(ahb w,int x,int y,int z){return null;}' if name=='BlockMush' else ''
 oldsrc.append(write('OldShape'+str(i)+'.java','public class OldShape'+str(i)+' extends '+parent+' { public OldShape'+str(i)+'(){'+init+body+'}'+null+'}'))
subprocess.run([str(a.java_home/'bin/javac'),'-cp',str(a.legacy_jar),'-d',str(oldclasses)]+[str(f) for f in oldsrc],check=True)
files=[str(f) for f in src.rglob('*.java') if f not in oldsrc]+[str(H/'ShapeNativeTest.java'),str(H/'Relocate.java')]
subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-cp',cp,'-sourcepath',str(out/'empty'),'-d',str(classes)]+files,check=True)
subprocess.run([str(a.java_home/'bin/java'),'-cp',str(classes)+':'+cp,'Relocate',str(oldclasses)],check=True)
subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-cp',str(classes)+':'+cp,'ShapeNativeTest',str(a.legacy_jar),str(oldclasses)],cwd=out,check=True)
