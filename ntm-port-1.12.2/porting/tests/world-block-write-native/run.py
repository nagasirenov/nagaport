#!/usr/bin/env python3
import argparse,hashlib,json,os,struct,subprocess,tempfile,zipfile,re,shutil
from pathlib import Path
import audit
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,default=Path('/workspace/shared/nagaport-toolchain/java8'));p.add_argument('--classpath-file',type=Path,default=Path('/tmp/nagaport-ae2-native-classpath.txt'));p.add_argument('--legacy-client',type=Path,default=Path('/tmp/nagaport-legacy-client.jar'));p.add_argument('--attribute',action='store_true');p.add_argument('--mutants',action='store_true');a=p.parse_args()
H=audit.HERE;R=audit.ROOT;m=audit.verify();cp=a.classpath_file.read_text().strip();java=a.java_home/'bin/java';javac=a.java_home/'bin/javac';tools=a.java_home/'lib/tools.jar'
def run(cmd,**kw):return subprocess.run(list(map(str,cmd)),check=True,**kw)
with tempfile.TemporaryDirectory(prefix='world-write-native-')as tmp:
 t=Path(tmp);out=t/'classes';out.mkdir();runtime=str(out)+os.pathsep+cp
 helper=R/'src/main/java/com/hbm/world/WorldBlockState.java'
 run([javac,'-encoding','UTF-8','-cp',cp,'-d',out,helper,H/'WorldWriteTest.java',H/'ConversionAudit.java',H/'DispatchAudit.java'])
 for cls,args in [('WorldWriteTest',[a.legacy_client]),('ConversionAudit',[]),('DispatchAudit',[cp,'net/minecraft/world/World']),('DispatchAudit',[a.legacy_client,'ahb'])]:run([java,'-Xmx1g','-cp',runtime,cls]+args,cwd=t)
 worlds=[f for f in (R/'src/main/java').rglob('*.java')if re.search(r'extends\s+(?:net\.minecraft\.world\.)?(?:World|WorldServer|WorldClient)\b',f.read_text())]
 assert [str(f.relative_to(R))for f in worlds]==['src/main/java/com/hbm/wiaj/FauxWorld.java']
 assert not re.search(r'\b(?:setBlock|setBlockState)\s*\(',worlds[0].read_text());print('PASS: only source World subclass is FauxWorld with no mutation override')
 run([javac,'-cp',str(a.legacy_client)+os.pathsep+cp,'-d',out,H/'OriginalWriteEvaluation.java'])
 old=b'OriginalWriteEvaluation';new=b'proof/OriginalWriteEvaluation';f=out/'OriginalWriteEvaluation.class';raw=f.read_bytes();needle=b'\1'+struct.pack('>H',len(old))+old;assert raw.count(needle)==1
 dst=out/'proof/OriginalWriteEvaluation.class';dst.parent.mkdir();dst.write_bytes(raw.replace(needle,b'\1'+struct.pack('>H',len(new))+new));f.unlink()
 run([java,'-cp',runtime+os.pathsep+str(a.legacy_client),'proof.OriginalWriteEvaluation'],cwd=t)
 run([javac,'-cp',str(tools)+os.pathsep+cp,'-d',out,H/'WorldWriteScanner.java',H/'NativeWriteScanner.java'])
 inputs=t/'inputs';cpf=t/'cp';cpf.write_text(cp)
 def scan(name,files):
  inputs.write_text('\n'.join(map(str,files))+'\n');return run([java,'-Xmx3g','-cp',str(out)+os.pathsep+str(tools),name,inputs,cpf],stdout=subprocess.PIPE,universal_newlines=True).stdout
 control=t/'Controls.java';control.write_bytes((H/'controls.java.in').read_bytes());rows=scan('WorldWriteScanner',[control]);assert len(rows.splitlines())==2,rows;print('PASS: 2 exact native World/vanilla-block positive and 12 lookalike/type/metadata/flag/comment negative attribution controls')
 if a.attribute:
  rows=scan('NativeWriteScanner',sorted((R/'src/main/java').rglob('*.java')))
  actual={(str(Path(l.split('\t')[0]).relative_to(R)),int(l.split('\t')[1]))for l in rows.splitlines()}
  expected=set()
  for rel,edits in m['files'].items():
   for e in edits:expected.add((rel,e['start']+sum(len(x['replacement'])-len(x['original'])for x in edits if x['start']<e['start'])))
  assert actual==expected and len(actual)==36,(actual^expected);print('PASS: all 36 exact rewritten positions resolve to native-state boundary overload; no whole-source compilation claim')
 if a.mutants:
  original=helper.read_text();mutants=[original.replace('x < -30000000','x <= -30000000'),original.replace('x >= 30000000','x > 30000000'),original.replace('y >= 256','y > 256'),original.replace('Objects.requireNonNull(world);',''),original.replace('world.setBlockState(new BlockPos(x, y, z), state, flags)','world.setBlockState(new BlockPos(z, y, x), state, flags)'),original.replace('return world.setBlockState(new BlockPos(x, y, z), state, flags);','world.setBlockState(new BlockPos(x, y, z), state, flags); return true;'),original.replace(', state, flags);',', state, flags | 1);'),original.replace(', state, flags);',', state.getBlock().getDefaultState(), flags);')]
  mutant=t/'WorldBlockState.java'
  for i,s in enumerate(mutants):
   assert s!=original;mutant.write_text(s);run([javac,'-cp',cp,'-d',out,mutant]);result=subprocess.run([str(java),'-Xmx1g','-cp',runtime,'WorldWriteTest',str(a.legacy_client)],cwd=t,stdout=subprocess.PIPE,stderr=subprocess.STDOUT);assert result.returncode!=0,('mutant survived',i)
  print('PASS: eight boundary/null/coordinates/return/flags/metadata behavioral mutants rejected')
  root=t/'source-mutations';files=set(m['files'])|{e['file']for e in m['excluded']}|{'src/main/java/com/hbm/world/WorldBlockState.java'}
  for rel in files:q=root/rel;q.parent.mkdir(parents=True,exist_ok=True);q.write_bytes((R/rel).read_bytes())
  for rel,edits in m['files'].items():
   f=root/rel;before=f.read_bytes()
   for e in edits:
    s=before.decode();pos=e['start']+sum(len(x['replacement'])-len(x['original'])for x in edits if x['start']<e['start']);s=s[:pos]+'false'+s[pos+len(e['replacement']):];f.write_bytes(s.encode())
    try:audit.verify(root)
    except AssertionError:pass
    else:raise AssertionError('source guard accepted removed call: '+rel)
   f.write_bytes(before)
  print('PASS: 36 production-call deletion mutations rejected by exact source guard')
print('PASS: bounded write suite complete; no full-mod build or gameplay-runtime claim')
