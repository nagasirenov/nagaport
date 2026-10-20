#!/usr/bin/env python3
"""Bounded source attribution and genuine old/native runtime; never a full build."""
import argparse,subprocess,tempfile,os,struct,zipfile,re,json,hashlib,io
from pathlib import Path
import audit
p=argparse.ArgumentParser();p.add_argument('--java-home',required=True,type=Path);p.add_argument('--classpath-file',required=True,type=Path);p.add_argument('--legacy-client',required=True,type=Path);p.add_argument('--legacy-forge',required=True,type=Path);p.add_argument('--attribute',action='store_true');p.add_argument('--mutants',action='store_true');a=p.parse_args()
H=audit.HERE;R=audit.ROOT;m=audit.verify();cp=a.classpath_file.read_text().strip();java=a.java_home/'bin/java';javac=a.java_home/'bin/javac';tools=a.java_home/'lib/tools.jar'
def run(cmd,**kw):return subprocess.run(list(map(str,cmd)),check=True,**kw)
def relocate(f):
 raw=f.read_bytes();name=f.stem.encode();n=struct.unpack('>H',raw[8:10])[0];out=bytearray(raw[:10]);i=1;pos=10
 while i<n:
  tag=raw[pos];out.append(tag);pos+=1
  if tag==1:
   k=struct.unpack('>H',raw[pos:pos+2])[0];pos+=2;s=raw[pos:pos+k].replace(name,b'proof/'+name);pos+=k;out+=struct.pack('>H',len(s))+s
  else:
   size={3:4,4:4,5:8,6:8,7:2,8:2,9:4,10:4,11:4,12:4,15:3,16:2,18:4}[tag];out+=raw[pos:pos+size];pos+=size
   if tag in (5,6):i+=1
  i+=1
 out+=raw[pos:];dest=f.parent/'proof'/f.name;dest.parent.mkdir(exist_ok=True);dest.write_bytes(out);f.unlink()
paths=sorted((R/'src/main/java').rglob('*.java'))
worlds=[f for f in paths if re.search(r'extends\s+(?:net\.minecraft\.world\.)?(?:World|WorldServer|WorldClient)\b',f.read_text())]
assert [str(f.relative_to(R))for f in worlds]==['src/main/java/com/hbm/wiaj/FauxWorld.java']
assert not re.search(r'\b(?:func_147480_a|destroyBlock|setBlockState|setBlockToAir|setBlock)\s*\(',worlds[0].read_text())
# Method declarations, not isAir calls. Existing beam material and method are compiled below.
air_decl=[f for f in paths if re.search(r'\b(?:public|protected)\s+boolean\s+isAir\s*\(',f.read_text())]
assert [str(f.relative_to(R))for f in air_decl]==['src/main/java/com/hbm/blocks/machine/BlockBeamBase.java']
with zipfile.ZipFile(a.legacy_forge)as z:
 mapping=z.read('conf/packaged.srg').decode();assert 'MD: ahb/a (IIIZ)Z net/minecraft/world/World/func_147480_a (IIIZ)Z' in mapping
 for n in ['forgepatches.zip','fmlpatches.zip']:
  with zipfile.ZipFile(io.BytesIO(z.read(n)))as q:patch=q.read('net/minecraft/world/World.java.patch')
  assert patch==(H/(n+'.World.patch')).read_bytes();assert b'func_147480_a' not in patch
print('PASS: authentic original SRG mapping and Forge/FML World patch archives; source World override and NTM isAir declaration audit')
with tempfile.TemporaryDirectory(prefix='native-destroy-')as tmp:
 t=Path(tmp);out=t/'classes';out.mkdir();runtime=str(out)+os.pathsep+cp
 helper=R/'src/main/java/com/hbm/world/WorldBlockDestruction.java'
 sources=[helper,R/'src/main/java/com/hbm/world/WorldBlockState.java',R/'src/main/java/com/hbm/interfaces/IMultiblock.java',R/'src/main/java/com/hbm/tileentity/machine/TileEntityDummy.java',R/'src/main/java/com/hbm/blocks/machine/BlockBeamBase.java',H/'DestroyBlockTest.java']
 run([javac,'-encoding','UTF-8','-cp',cp,'-d',out]+sources)
 run([java,'-cp',runtime,'DestroyBlockTest'],cwd=t)
 for name in ['OriginalDestroyBlockTest','OriginalDestroyEvaluation']:
  run([javac,'-cp',str(a.legacy_client)+os.pathsep+cp,'-d',out,H/(name+'.java')]);relocate(out/(name+'.class'));run([java,'-cp',runtime+os.pathsep+str(a.legacy_client),'proof.'+name],cwd=t)
 run([javac,'-cp',str(tools)+os.pathsep+cp,'-d',out,H/'DestroyScanner.java',H/'NativeDestroyScanner.java',H/'DispatchAudit.java',H/'BlockAirAudit.java'])
 for path,root in [(cp,'net/minecraft/world/World'),(str(a.legacy_client),'ahb')]:run([java,'-cp',runtime,'DispatchAudit',path,root])
 run([java,'-cp',runtime,'BlockAirAudit',cp])
 inputs=t/'inputs';cpf=t/'cp';cpf.write_text(cp)
 def scan(name,files):
  inputs.write_text('\n'.join(map(str,files))+'\n');return run([java,'-Xmx3g','-cp',str(out)+os.pathsep+str(tools),name,inputs,cpf],stdout=subprocess.PIPE,universal_newlines=True).stdout
 control=t/'Controls.java';control.write_bytes((H/'negative-controls.java.in').read_bytes());rows=scan('DestroyScanner',[control]);assert len(rows.splitlines())==2,rows
 print('PASS: 2 typed positives and 10 owner/type/arity/boxing/unresolved/effectful-receiver negative attribution controls')
 if a.attribute:
  rows=scan('NativeDestroyScanner',paths);actual={(str(Path(l.split('\t')[0]).relative_to(R)),int(l.split('\t')[1]))for l in rows.splitlines()}
  expected={(str(f.relative_to(R)),len(f.read_bytes().decode()[:hit.start()].encode('utf-16-le'))//2)for f in paths for hit in re.finditer(re.escape('com.hbm.world.WorldBlockDestruction.destroy('),f.read_bytes().decode())}
  assert actual==expected and len(actual)==60,(len(actual),len(expected),actual^expected)
  original=t/'original'
  with zipfile.ZipFile(H/'baseline.zip')as z:
   for f in paths:
    rel=str(f.relative_to(R));target=original/rel;target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(z.read(rel)if rel in m['files']else f.read_bytes())
  rows=scan('DestroyScanner',sorted(original.rglob('*.java')));actual={(str(Path(l.split('\t')[0]).relative_to(original)),int(l.split('\t')[1]))for l in rows.splitlines()}
  expected={(rel,len((original/rel).read_bytes().decode()[:e['start']].encode('utf-16-le'))//2)for rel,es in m['files'].items()for e in es}
  assert actual==expected and len(actual)==60,(len(actual),len(expected),actual^expected)
  print('PASS: exact 60 old full-source World/int,int,int,boolean positions equal 60 resolved native helper invocations')
 if a.mutants:
  original=helper.read_text();mutants=[original.replace('x < -30000000','x <= -30000000'),original.replace('x >= 30000000','x > 30000000'),original.replace('y >= 256','y > 256'),original.replace('Objects.requireNonNull(world);',''),original.replace('new BlockPos(x, y, z)','new BlockPos(z, y, x)'),original.replace('), dropBlock)', '), !dropBlock)'),original.replace('return world.destroyBlock(new BlockPos(x, y, z), dropBlock);','world.destroyBlock(new BlockPos(x, y, z), dropBlock); return true;'),original.replace('world.destroyBlock(new BlockPos(x, y, z), dropBlock)','world.setBlockToAir(new BlockPos(x, y, z))')]
  f=t/'WorldBlockDestruction.java'
  for i,s in enumerate(mutants):
   assert s!=original;f.write_text(s);run([javac,'-cp',cp,'-d',out,f]);r=subprocess.run([str(java),'-cp',runtime,'DestroyBlockTest'],cwd=t,stdout=subprocess.PIPE,stderr=subprocess.STDOUT);assert r.returncode!=0,('mutant survived',i)
  print('PASS: eight bound/null/coordinate/drop-flag/return/drop-suppression mutants rejected')
print('PASS: bounded proof complete; unfinished-port diagnostics are not whole-source build success')
