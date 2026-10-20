#!/usr/bin/env python3
import argparse,subprocess,tempfile,os,struct,zipfile,re,json
from pathlib import Path
import audit
p=argparse.ArgumentParser();p.add_argument('--java-home',required=True,type=Path);p.add_argument('--classpath-file',required=True,type=Path);p.add_argument('--legacy-client',required=True,type=Path);p.add_argument('--attribute',action='store_true');p.add_argument('--mutants',action='store_true');a=p.parse_args()
H=audit.HERE;R=audit.ROOT;m=audit.verify();
source_worlds=[f for f in (R/'src/main/java').rglob('*.java') if re.search(r'extends\s+(?:net\.minecraft\.world\.)?(?:World|WorldServer|WorldClient)\b',f.read_text())]
assert [str(f.relative_to(R))for f in source_worlds]==['src/main/java/com/hbm/wiaj/FauxWorld.java'],source_worlds
for content in (source_worlds[0].read_text(),(H/'original-FauxWorld.java.txt').read_text()):assert not re.search(r'\b(?:setBlockToAir|setBlockState|setBlock)\s*\(',content)
print('PASS: only source World subclass is original/native FauxWorld; no block mutation overrides')
cp=a.classpath_file.read_text().strip();java=a.java_home/'bin/java';javac=a.java_home/'bin/javac';tools=a.java_home/'lib/tools.jar'
def run(cmd,**kw):return subprocess.run(list(map(str,cmd)),check=True,**kw)
with tempfile.TemporaryDirectory(prefix='native-air-') as t:
 t=Path(t);out=t/'classes';out.mkdir();runtime=str(out)+os.pathsep+cp
 helper=R/'src/main/java/com/hbm/world/WorldAirRemoval.java'
 source=(R/'src/main/java/com/hbm/tileentity/machine/TileEntityMachineRocketAssembly.java').read_text();start=source.index('public void deletePlatform(');end=source.index('{',start)+1;depth=1
 while depth:depth+=(source[end]=='{')-(source[end]=='}');end+=1
 actual=t/'ProductionPlatform.java';actual.write_text('class ProductionPlatform extends net.minecraft.tileentity.TileEntity {\n'+source[start:end]+'\n}\n')
 run([javac,'-encoding','UTF-8','-cp',cp,'-d',out,helper,H/'AirRemovalTest.java',actual,H/'ProductionMethodTest.java'])
 for cls in ('AirRemovalTest','ProductionMethodTest'):run([java,'-cp',runtime,cls,a.legacy_client],cwd=t)
 run([javac,'-cp',str(a.legacy_client)+os.pathsep+cp,'-d',out,H/'OriginalAirEvaluation.java'])
 old=b'OriginalAirEvaluation';new=b'proof/OriginalAirEvaluation';f=out/'OriginalAirEvaluation.class';raw=f.read_bytes();needle=b'\1'+struct.pack('>H',len(old))+old;assert raw.count(needle)==1
 dest=out/'proof/OriginalAirEvaluation.class';dest.parent.mkdir();dest.write_bytes(raw.replace(needle,b'\1'+struct.pack('>H',len(new))+new));f.unlink()
 run([java,'-cp',runtime+os.pathsep+str(a.legacy_client),'proof.OriginalAirEvaluation'],cwd=t)
 run([javac,'-cp',str(tools)+os.pathsep+cp,'-d',out,H/'AirScanner.java',H/'NativeAirScanner.java',H/'DispatchAudit.java'])
 for path,root in ((cp,'net/minecraft/world/World'),(str(a.legacy_client),'ahb')):run([java,'-cp',runtime,'DispatchAudit',path,root])
 inputs=t/'inputs';cpf=t/'cp';cpf.write_text(cp)
 def scan(name,paths):
  inputs.write_text('\n'.join(map(str,paths))+'\n');return run([java,'-Xmx2g','-cp',str(out)+os.pathsep+str(tools),name,inputs,cpf],stdout=subprocess.PIPE,universal_newlines=True).stdout
 control=t/'Controls.java';control.write_bytes((H/'negative-controls.java.in').read_bytes());rows=scan('AirScanner',[control]);assert len(rows.splitlines())==2,rows;print('PASS: 2 exact World/int positive and 8 negative owner/type/arity/unresolved attribution controls')
 if a.attribute:
  paths=sorted((R/'src/main/java').rglob('*.java'));rows=scan('NativeAirScanner',paths)
  actual={(str(Path(l.split('\t')[0]).relative_to(R)),int(l.split('\t')[1]))for l in rows.splitlines()};expected={(str(f.relative_to(R)),len(f.read_bytes().decode()[:hit.start()].encode('utf-16-le'))//2)for f in paths for hit in re.finditer(re.escape('com.hbm.world.WorldAirRemoval.remove('),f.read_bytes().decode())}
  assert actual==expected and len(actual)==150,(len(actual),len(expected),actual^expected)
  original=t/'original'
  with zipfile.ZipFile(H/'baseline.zip') as z:
   for f in paths:
    rel=str(f.relative_to(R));target=original/rel;target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(z.read(rel)if rel in m['files']else f.read_bytes())
  rows=scan('AirScanner',sorted(original.rglob('*.java')))
  actual={(str(Path(l.split('\t')[0]).relative_to(original)),int(l.split('\t')[1]))for l in rows.splitlines()}
  expected={(rel,len((original/rel).read_bytes().decode()[:e['start']].encode('utf-16-le'))//2)for rel,es in m['files'].items()for e in es}
  assert actual==expected and len(actual)==150,(len(actual),len(expected),actual^expected)
  print('PASS: exact 150 original full-source attributed positions equal 150 native helper overload resolutions')
 if a.mutants:
  original=helper.read_text();mutants=[original.replace('x < -30000000','x <= -30000000'),original.replace('x >= 30000000','x > 30000000'),original.replace('y >= 256','y > 256'),original.replace('Objects.requireNonNull(world);',''),original.replace('new BlockPos(x, y, z)','new BlockPos(z, y, x)'),original.replace('return world.setBlockToAir(new BlockPos(x, y, z));','world.setBlockToAir(new BlockPos(x, y, z)); return true;'),original.replace('world.setBlockToAir(new BlockPos(x, y, z))','world.destroyBlock(new BlockPos(x, y, z), true)')]
  f=t/'WorldAirRemoval.java'
  for i,s in enumerate(mutants):
   assert s!=original;f.write_text(s);run([javac,'-cp',cp,'-d',out,f]);result=subprocess.run([str(java),'-cp',runtime,'AirRemovalTest',str(a.legacy_client)],cwd=t,stdout=subprocess.PIPE,stderr=subprocess.STDOUT);assert result.returncode!=0,('mutant survived',i)
  print('PASS: seven behavioral bound/null/coordinate/boolean-return/drop-path mutants rejected')
