#!/usr/bin/env python3
import argparse,hashlib,json,os,re,shutil,struct,subprocess,tempfile,zipfile
from pathlib import Path
import audit
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,default=Path('/workspace/shared/nagaport-toolchain/java8'));p.add_argument('--classpath-file',type=Path,default=Path('/tmp/nagaport-ae2-native-classpath.txt'));p.add_argument('--legacy-client',type=Path,default=Path('/tmp/nagaport-legacy-client.jar'));p.add_argument('--legacy-mod',type=Path,default=Path('/workspace/shared/nagaport/mods1710/HBM-NTM-.1.0.27_X5645_H261.jar'));p.add_argument('--legacy-srg',type=Path,default=Path('/tmp/nagaport1710-srg.zip'));p.add_argument('--legacy-forge',type=Path,default=Path('/tmp/nagaport-forge1614-userdev.jar'));p.add_argument('--attribute',action='store_true');p.add_argument('--mutants',action='store_true');a=p.parse_args()
H=audit.HERE;R=audit.ROOT;m=audit.verify();cp=a.classpath_file.read_text().strip();java=a.java_home/'bin/java';javac=a.java_home/'bin/javac';tools=a.java_home/'lib/tools.jar';previous=H.parent/'world-block-write-native'
def run(cmd,**kw):return subprocess.run(list(map(str,cmd)),check=True,**kw)
with tempfile.TemporaryDirectory(prefix='world-owned-write-')as tmp:
 t=Path(tmp);out=t/'classes';out.mkdir();runtime=str(out)+os.pathsep+cp
 owners=['BlockRedBrick','DecoSteelPoles','DecoPoleTop'];production=[R/('src/main/java/com/hbm/blocks/generic/'+n+'.java')for n in owners]+[R/('src/main/java/'+s+'.java')for s in ['com/hbm/world/WorldBlockState','com/hbm/render/icon/IIcon','com/hbm/render/icon/IIconRegister','com/hbm/lib/RefStrings']]
 tests=[H/(n+'.java')for n in ['LegacyOwners','OwnedWriteTest','OwnedConversionAudit','OwnedEvaluation','NullBlockLimits']]+[previous/'WorldWriteTest.java']
 # The evaluation fixture uses the actual generated production capture template, substituting only argument expressions with logging seams.
 e=next(e for es in m['files'].values()for e in es);s=e['replacement']
 for name,expr in zip(['World','X','Y','Z','Block','Meta','Flags'],['receiver()','arg(1)','arg(2)','arg(3)','block()','metadata()','flags()']):s=re.sub('(nativeOwnedWrite'+name+' = )[^;]+;',lambda q:q[1]+expr+';',s)
 generated=t/'CapturedOwnedWrite.java';generated.write_text('public class CapturedOwnedWrite extends OwnedEvaluation { static void invoke() '+s+' }\n');tests.append(generated)
 run([javac,'-encoding','UTF-8','-cp',cp,'-d',out]+production+tests)
 legacy=t/'legacy';legacy.mkdir();forge=t/'legacy-forge.jar';forge.write_bytes(zipfile.ZipFile(a.legacy_forge).read('binaries.jar'))
 run([java,'-cp',runtime,'LegacyOwners',a.legacy_mod,a.legacy_srg,legacy])
 provenance={'kind':'actual supplied original mod classfiles, symbol-only SRG-to-obfuscated/owner-package remapping; not compiler-generated source fixtures','files':{str(f):audit.digest(f.read_bytes())for f in [a.legacy_client,a.legacy_mod,a.legacy_srg,a.legacy_forge]},'classes':{}}
 with zipfile.ZipFile(a.legacy_mod)as z:
  for n in owners:
   entry='com/hbm/blocks/generic/'+n+'.class';provenance['classes'][entry]={'original_sha256':audit.digest(z.read(entry)),'remapped_sha256':audit.digest((legacy/'prooflegacy'/ (n+'.class')).read_bytes())}
 assert provenance==json.loads((H/'dependency-provenance.json').read_text()),'dependency/actual original classfile provenance changed'
 print('PASS: original mod/classfile, vanilla client, Forge1614 and SRG SHA-256 provenance matches retained evidence')
 for cls,args in [('OwnedWriteTest',[a.legacy_client,legacy,forge]),('NullBlockLimits',[a.legacy_client,legacy,forge]),('OwnedConversionAudit',[]),('OwnedEvaluation',[])]:run([java,'-Xmx1g','-cp',runtime,cls]+args,cwd=t)
 run([javac,'-cp',str(a.legacy_client)+os.pathsep+cp,'-d',out,H/'OriginalOwnedEvaluation.java'])
 old=b'OriginalOwnedEvaluation';new=b'proof/OriginalOwnedEvaluation';f=out/'OriginalOwnedEvaluation.class';raw=f.read_bytes();needle=b'\1'+struct.pack('>H',len(old))+old;assert raw.count(needle)==1;dst=out/'proof/OriginalOwnedEvaluation.class';dst.parent.mkdir();dst.write_bytes(raw.replace(needle,b'\1'+struct.pack('>H',len(new))+new));f.unlink();run([java,'-cp',runtime+os.pathsep+str(a.legacy_client),'proof.OriginalOwnedEvaluation'],cwd=t)
 run([javac,'-cp',str(tools)+os.pathsep+cp,'-d',out,H/'OwnedWriteScanner.java',previous/'NativeWriteScanner.java'])
 inputs=t/'inputs';cpf=t/'cp';cpf.write_text(cp)
 def scan(name,files):
  inputs.write_text('\n'.join(map(str,files))+'\n');return run([java,'-Xmx3g','-cp',str(out)+os.pathsep+str(tools),name,inputs,cpf],stdout=subprocess.PIPE,stderr=subprocess.PIPE,universal_newlines=True)
 control=t/'Controls.java';control.write_bytes((H/'controls.java.in').read_bytes());rows=scan('OwnedWriteScanner',[control]);assert len(rows.stdout.splitlines())==2,rows.stdout;print('PASS: two owned positives and ten receiver/alias/range/type/result/comment negative attribution controls')
 # Assignment closure refuses a field initializer and a source reassignment through an alias.
 for text in [control.read_text().replace('static Block brick_red,','static Block brick_red = null,'),control.read_text().replace('void positive(World w,int x){','void positive(World w,int x){ModBlocks.brick_red = ModBlocks.steel_poles;')]:
  negative=t/'Negative.java';negative.write_text(text)
  try:scan('OwnedWriteScanner',[negative])
  except subprocess.CalledProcessError:pass
  else:raise AssertionError('assignment closure mutation accepted')
 print('PASS: field-initializer and aliased-field-reassignment closure mutations rejected')
 if a.attribute:
  files=sorted((R/'src/main/java').rglob('*.java'));rows=scan('OwnedWriteScanner',files);assign=[l.split('\t')for l in rows.stderr.splitlines()if l.startswith('FIELD\t')]
  assert sorted((x[1],x[2])for x in assign)==sorted(zip(m['owners'],['com.hbm.blocks.generic.'+n for n in owners])),assign
  print(rows.stderr,end='');print('PASS: exactly three attributed field assignments, each a reviewed native identity-returning builder chain from its exact concrete constructor; no initializers')
  rows=scan('NativeWriteScanner',files);actual={(str(Path(l.split('\t')[0]).relative_to(R)),int(l.split('\t')[1]))for l in rows.stdout.splitlines()};expected=set()
  for rel,es in m['files'].items():
   for e in es:
    pos=e['start']+sum(len(p['replacement'])-len(p['original'])for p in es if p['start']<e['start']);pos+=e['replacement'].index('com.hbm.world.WorldBlockState.write');expected.add((rel,pos))
  assert expected<=actual and len(expected)==57,expected-actual
  print(rows.stderr,end='');print('PASS: all 57 exact new call positions resolve to the existing IBlockState boundary; NOT full compilation')
 if a.mutants:
  # Production owner mutants must fail the differential original/native behavior suite.
  owner=production[0];original=owner.read_text();mutants=[original.replace('meta & 15','0'),original.replace('side == meta ?','side != meta ?'),original.replace('height - pos.getY() > 2.0D','height - pos.getY() >= 2.0D'),original.replace('(float) player.posX - pos.getX()','(float) player.posX - (pos.getX() + 0.5F)'),original.replace('current.getBlock().getStateFromMeta(l)','this.getStateFromMeta(l)')]
  mutant=t/'BlockRedBrick.java'
  for i,s in enumerate(mutants):
   assert s!=original;mutant.write_text(s);run([javac,'-cp',runtime,'-d',out,mutant]);r=subprocess.run([str(java),'-Xmx1g','-cp',runtime,'OwnedWriteTest',str(a.legacy_client),str(legacy),str(forge)],cwd=t,stdout=subprocess.PIPE,stderr=subprocess.STDOUT);assert r.returncode!=0,('owner mutant survived',i)
  run([javac,'-cp',runtime,'-d',out,owner]);print('PASS: five state/icon/strict-height/corner/actual-owner behavior mutants rejected')
  # Template mutations include the formerly unsafe eager conversion, field lookup reordering and lost flags.
  original=generated.read_text();mutants=[original.replace('nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000','true'),original.replace('if (nativeOwnedWriteWorld == null) throw new NullPointerException();',''),original.replace('nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta)','nativeOwnedWriteBlock.getStateFromMeta(0)'),original.replace(', nativeOwnedWriteFlags);',', 3);'),original.replace('if (nativeOwnedWriteWorld == null)', 'nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta); if (nativeOwnedWriteWorld == null)')]
  for i,s in enumerate(mutants):
   assert s!=original;generated.write_text(s);run([javac,'-cp',runtime,'-d',out,generated]);r=subprocess.run([str(java),'-cp',runtime,'OwnedEvaluation'],cwd=t,stdout=subprocess.PIPE,stderr=subprocess.STDOUT);assert r.returncode!=0,('capture mutant survived',i)
  print('PASS: five capture bounds/null/metadata/flags/eager-conversion behavior mutants rejected')
  # Every caller is exact-regenerated; a removed boundary invocation cannot hide in aggregate counts.
  for rel,es in m['files'].items():
   before=(R/rel).read_text()
   for e in es:
    pos=e['start']+sum(len(p['replacement'])-len(p['original'])for p in es if p['start']<e['start']);changed=before[:pos]+'{}'+before[pos+len(e['replacement']):];assert changed!=before
    expected=zipfile.ZipFile(H/'baseline.zip').read(rel).decode()
    for q in sorted(es,key=lambda q:q['start'],reverse=True):expected=expected[:q['start']]+q['replacement']+expected[q['end']:]
    assert changed!=expected
  print('PASS: all 57 individual production-call deletion mutations rejected by exact-source comparison')
print('PASS: bounded owned-write proof complete; no full-mod/client/server/persistence certification')
