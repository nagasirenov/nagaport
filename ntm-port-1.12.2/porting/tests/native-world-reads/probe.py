#!/usr/bin/env python3
import argparse,subprocess,tempfile,os,re,json,struct
from pathlib import Path
import audit
HERE=audit.HERE;ROOT=audit.ROOT
p=argparse.ArgumentParser();p.add_argument('--java-home',required=True,type=Path);p.add_argument('--classpath',required=True);p.add_argument('--original-client',required=True,type=Path);p.add_argument('--attribute',action='store_true');p.add_argument('--mutants',action='store_true');p.add_argument('--integrated',action='store_true');a=p.parse_args()
def run(cmd,**kw):return subprocess.run(list(map(str,cmd)),check=True,**kw)
def method(p,sig):
 s=(ROOT/'src/main/java'/p).read_text();start=s.index(sig);end=s.index('{',start)+1;depth=1
 while depth:depth+=(s[end]=='{')-(s[end]=='}');end+=1
 return s[start:end]
if a.integrated:
 assert not a.attribute,'Integrated mode is runtime only; use NativeWorldReadScanner separately for integrated attribution'
 for rel,r in audit.manifest()['files'].items():assert (ROOT/rel).read_bytes().decode().count('com.hbm.world.WorldBlockState.read(')==len(r['edits']),rel
 print('PASS: integrated read counts retained; baseline whole-file hashes intentionally not asserted')
else:audit.verify()
sep=os.pathsep;java=a.java_home/'bin/java';javac=a.java_home/'bin/javac';tools=a.java_home/'lib/tools.jar'
with tempfile.TemporaryDirectory(prefix='world-reads-') as tmp:
 tmp=Path(tmp);classes=tmp/'classes';classes.mkdir();cp=str(classes)+sep+a.classpath
 entries=[('ProductionTorus','extends net.minecraft.tileentity.TileEntity','com/hbm/tileentity/machine/TileEntityFusionTorusStruct.java',['private Block gbr(','private int gmr(']),('ProductionSapling','','com/hbm/blocks/BlockNTSapling.java',['public int getDamageValue(']),('ProductionTallPlant','','com/hbm/blocks/generic/BlockTallPlant.java',['public int getDamageValue(']),('ProductionCrop','','com/hbm/blocks/BlockCrop.java',['public boolean func_149851_a('])]
 extracted=tmp/'ProductionWorldReads.java';extracted.write_text('import net.minecraft.world.World;import net.minecraft.block.Block;\n'+'\n'.join('class '+n+' '+base+' {\n'+'\n'.join(method(p,sig) for sig in sigs)+'\n}'for n,base,p,sigs in entries))
 helper=ROOT/'src/main/java/com/hbm/world/WorldBlockState.java'
 run([javac,'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',classes,helper,HERE/'WorldReadNativeTest.java',extracted,HERE/'ProductionMethodsTest.java'])
 for test in ['WorldReadNativeTest','ProductionMethodsTest']:run([java,'-cp',cp,test,a.original_client],cwd=tmp)
 run([javac,'-cp',str(a.original_client)+sep+a.classpath,'-d',classes,HERE/'OriginalWorldEvaluation.java'])
 # Move only the generated test class into a named package to avoid signed
 # original default-package classes conflicting with our unsigned test class.
 # Every byte of the original client JAR remains untouched.
 raw=(classes/'OriginalWorldEvaluation.class').read_bytes();old=b'OriginalWorldEvaluation';new=b'proof/OriginalWorldEvaluation'
 needle=bytes([1])+struct.pack('>H',len(old))+old;replacement=bytes([1])+struct.pack('>H',len(new))+new
 assert raw.count(needle)==1;dest=classes/'proof/OriginalWorldEvaluation.class';dest.parent.mkdir();dest.write_bytes(raw.replace(needle,replacement));(classes/'OriginalWorldEvaluation.class').unlink()
 run([java,'-cp',cp+sep+str(a.original_client),'proof.OriginalWorldEvaluation'],cwd=tmp)
 run([javac,'-cp',tools,'-d',classes,HERE/'WorldReadScanner.java',HERE/'NativeWorldReadScanner.java'])
 controls=tmp/'Controls.java';controls.write_bytes((HERE/'negative-controls.java.in').read_bytes());inputs=tmp/'inputs.txt';cpf=tmp/'classpath.txt';cpf.write_text(a.classpath)
 def scan(scanner,paths):
  inputs.write_text('\n'.join(map(str,paths))+'\n');return run([java,'-Xmx2g','-cp',str(classes)+sep+str(tools),scanner,inputs,cpf],stdout=subprocess.PIPE,universal_newlines=True).stdout
 rows=scan('WorldReadScanner',[controls]);assert len(rows.splitlines())==2,rows;print('PASS: two exact World/int controls accepted; eight interface/subclass/custom/unresolved/non-int/arity controls excluded')
 if a.attribute:
  paths=sorted((ROOT/'src/main/java').rglob('*.java'));rows=scan('NativeWorldReadScanner',paths)
  actual={(str(Path(line.split('\t')[0]).relative_to(ROOT)),int(line.split('\t')[1])) for line in rows.splitlines()}
  expected={(str(path.relative_to(ROOT)),len(path.read_bytes().decode()[:m.start()].encode('utf-16-le'))//2) for path in paths for m in re.finditer(re.escape('com.hbm.world.WorldBlockState.read('),path.read_bytes().decode())}
  assert actual==expected and len(actual)==1030,(len(actual),len(expected),actual^expected)
  original=tmp/'original'
  for path in paths:
   rel=str(path.relative_to(ROOT));dest=original/rel;dest.parent.mkdir(parents=True,exist_ok=True);text=path.read_bytes().decode();record=audit.manifest()['files'].get(rel);dest.write_bytes((audit.restore(text,record) if record else text).encode())
  rows=scan('WorldReadScanner',sorted(original.rglob('*.java')))
  actual={(str(Path(line.split('\t')[0]).relative_to(original)),int(line.split('\t')[1])) for line in rows.splitlines()}
  expected={(rel,len((original/rel).read_bytes().decode()[:e['start']].encode('utf-16-le'))//2) for rel,r in audit.manifest()['files'].items() for e in r['edits']}
  assert actual==expected and len(actual)==1030,(len(actual),len(expected),actual^expected)
  print('PASS: 1030 original exact-owner calls and 1030 actual native helper overload resolutions replayed')
 if a.mutants:
  text=helper.read_text();variants=[text.replace('x < -30000000','x <= -30000000'),text.replace('x >= 30000000','x > 30000000'),text.replace('y >= 256','y > 256'),text.replace('Objects.requireNonNull(world);',''),text.replace('new BlockPos(x, y, z)','new BlockPos(z, y, x)'),text.replace('return state.getBlock().getMetaFromState(state);','return 0;')]
  path=tmp/'WorldBlockState.java'
  for n,v in enumerate(variants):
   path.write_text(v);run([javac,'-cp',a.classpath,'-d',classes,path]);result=subprocess.run([str(java),'-cp',cp,'WorldReadNativeTest',str(a.original_client)],cwd=tmp,stdout=subprocess.PIPE,stderr=subprocess.STDOUT);assert result.returncode!=0,('mutant survived',n)
  print('PASS: six runtime bounds/null/coordinate/metadata mutants rejected')
