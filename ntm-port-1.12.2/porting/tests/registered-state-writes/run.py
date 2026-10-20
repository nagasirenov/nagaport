#!/usr/bin/env python3
import argparse,hashlib,json,os,re,subprocess,tempfile,zipfile,struct
from pathlib import Path
import audit
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,default=Path('/workspace/shared/nagaport-toolchain/java8'));p.add_argument('--classpath-file',type=Path,default=Path('/tmp/nagaport-ae2-native-classpath.txt'));p.add_argument('--legacy-client',type=Path,default=Path('/tmp/nagaport-legacy-client.jar'));p.add_argument('--legacy-mod',type=Path,default=Path('/workspace/shared/nagaport/mods1710/HBM-NTM-.1.0.27_X5645_H261.jar'));p.add_argument('--legacy-srg',type=Path,default=Path('/tmp/nagaport1710-srg.zip'));p.add_argument('--legacy-forge',type=Path,default=Path('/tmp/nagaport-forge1614-userdev.jar'));p.add_argument('--attribute',action='store_true');p.add_argument('--mutants',action='store_true');a=p.parse_args()
H=audit.HERE;R=audit.ROOT;m=audit.verify();cp=a.classpath_file.read_text().strip();java=a.java_home/'bin/java';javac=a.java_home/'bin/javac';tools=a.java_home/'lib/tools.jar';previous=H.parent/'world-block-write-native'
def run(cmd,**kw):return subprocess.run(list(map(str,cmd)),check=True,**kw)
with tempfile.TemporaryDirectory(prefix='registered-state-writes-')as tmp:
 t=Path(tmp);out=t/'classes';out.mkdir();runtime=str(out)+os.pathsep+cp
 production=['com/hbm/'+n+'.java'for n in ['blocks/BlockBase','blocks/BlockMulti','blocks/BlockEnumMulti','blocks/IBlockMulti','blocks/PlainCubeTextures','blocks/generic/BlockGeneric','blocks/generic/BlockNoDrop','blocks/generic/BlockDecoCT','world/WorldBlockState','render/icon/IIcon','render/icon/IIconRegister','lib/RefStrings','util/EnumUtil','interfaces/IOrderedEnum','render/block/ct/CT','render/block/ct/CTContext','render/block/ct/IBlockCT']]
 # Only the two CT neighbor identity fields are collaborators, never a platform stub or a compiled registry replacement.
 identity=t/'com/hbm/blocks/ModBlocks.java';identity.parent.mkdir(parents=True);identity.write_text('package com.hbm.blocks; public final class ModBlocks { public static net.minecraft.block.Block deco_steel, deco_rusty_steel; }')
 # These are exact production statement templates with only argument expressions replaced by observation seams.
 generated=[]
 for arity,cls in [(6,'CapturedRegisteredWrite'),(4,'CapturedRegisteredShort')]:
  e=next(e for es in m['files'].values()for e in es if e['arity']==arity);statement=e['replacement']
  expressions=['receiver()','arg(1)','arg(2)','arg(3)','block()']+(['metadata()','flags()']if arity==6 else ['0','3'])
  for name,expr in zip(['World','X','Y','Z','Block','Meta','Flags'],expressions):statement=re.sub('(nativeRegisteredWrite'+name+' = )[^;]+;',lambda q:q[1]+expr+';',statement)
  path=t/(cls+'.java');path.write_text('public class '+cls+' extends RegisteredEvaluation { static void invoke() '+statement+' }\n');generated.append(path)
 native_fields=[f for f,o in m['owners'].items()if o in ['BlockGeneric','BlockNoDrop','BlockDecoCT','BlockFalling']]
 construction=t/'RegisteredConstructionSlice.java';construction.write_text('import net.minecraft.block.*;import net.minecraft.block.material.Material;import com.hbm.blocks.*;import com.hbm.blocks.generic.*;import com.hbm.lib.RefStrings; public final class RegisteredConstructionSlice {static class MainRegistry {static final net.minecraft.creativetab.CreativeTabs blockTab=net.minecraft.creativetab.CreativeTabs.BUILDING_BLOCKS;} public static Block '+','.join(native_fields)+';public static Block[] initialize(){'+''.join(m['assignments'][f] for f in native_fields)+'return new Block[]{'+','.join(native_fields)+'};}}')
 tests=[H/'LegacyRegisteredOwners.java',H/'RegisteredWriteTest.java',H/'RegisteredConversionAudit.java',H/'RegisteredEvaluation.java',previous/'WorldWriteTest.java',identity,construction]+generated
 run([javac,'-encoding','UTF-8','-sourcepath',t,'-cp',cp,'-d',out]+[R/'src/main/java'/q for q in production]+tests)
 legacy=t/'legacy';legacy.mkdir();forge=t/'legacy-forge.jar';forge.write_bytes(zipfile.ZipFile(a.legacy_forge).read('binaries.jar'))
 run([javac,'-cp',str(a.legacy_client)+os.pathsep+cp,'-d',legacy,H/'OriginalFlagWorld.java'])
 original=legacy/'OriginalFlagWorld.class';raw=original.read_bytes();old=b'OriginalFlagWorld';new=b'proof/OriginalFlagWorld';needle=b'\1'+struct.pack('>H',len(old))+old;assert raw.count(needle)==1;dest=legacy/'proof/OriginalFlagWorld.class';dest.parent.mkdir();dest.write_bytes(raw.replace(needle,b'\1'+struct.pack('>H',len(new))+new));original.unlink()
 run([java,'-cp',runtime,'LegacyRegisteredOwners',a.legacy_mod,a.legacy_srg,legacy])
 provenance={'kind':'supplied original mod classfiles with symbol-only package/SRG remapping; genuine original vanilla and Forge dependencies; no method instructions replaced','files':{str(f):audit.digest(f.read_bytes())for f in [a.legacy_client,a.legacy_mod,a.legacy_srg,a.legacy_forge]},'classes':{}}
 with zipfile.ZipFile(a.legacy_mod)as z:
  for path in sorted((legacy/'prooflegacy').rglob('*.class')):
   n=str(path.relative_to(legacy/'prooflegacy'));provenance['classes']['com/hbm/'+n]={'original_sha256':audit.digest(z.read('com/hbm/'+n)),'remapped_sha256':audit.digest(path.read_bytes())}
 pin=H/'dependency-provenance.json'
 if not pin.exists():pin.write_text(json.dumps(provenance,indent=2)+'\n')
 assert provenance==json.loads(pin.read_text()),'original dependency provenance changed'
 print('PASS: original classfile/dependency SHA-256 provenance',flush=True)
 run([java,'-Xmx1g','-cp',runtime,'RegisteredWriteTest',a.legacy_client,legacy,forge],cwd=t)
 run([java,'-cp',runtime,'RegisteredConversionAudit'],cwd=t)
 run([java,'-cp',runtime,'RegisteredEvaluation'],cwd=t)
 run([javac,'-cp',str(a.legacy_client)+os.pathsep+cp,'-d',out,H/'OriginalRegisteredEvaluation.java'])
 old=b'OriginalRegisteredEvaluation';new=b'proof/OriginalRegisteredEvaluation';f=out/'OriginalRegisteredEvaluation.class';raw=f.read_bytes();needle=b'\1'+struct.pack('>H',len(old))+old;assert raw.count(needle)==1;dst=out/'proof/OriginalRegisteredEvaluation.class';dst.parent.mkdir();dst.write_bytes(raw.replace(needle,b'\1'+struct.pack('>H',len(new))+new));f.unlink();run([java,'-cp',runtime+os.pathsep+str(a.legacy_client),'proof.OriginalRegisteredEvaluation'],cwd=t)
 run([javac,'-cp',str(tools)+os.pathsep+cp,'-d',out,H/'RegisteredWriteScanner.java',previous/'NativeWriteScanner.java'])
 inputs=t/'inputs';cpf=t/'cp';cpf.write_text(runtime)
 def scan(name,files):
  inputs.write_text('\n'.join(map(str,files))+'\n');args=[java,'-Xmx3g','-cp',str(out)+os.pathsep+str(tools),name,inputs,cpf]
  if name=='RegisteredWriteScanner':args.append(H/'selected-fields.txt')
  return run(args,stdout=subprocess.PIPE,stderr=subprocess.PIPE,universal_newlines=True)
 control=t/'Controls.java';control.write_bytes((H/'controls.java.in').read_bytes());rows=scan('RegisteredWriteScanner',[control]);assert len(rows.stdout.splitlines())==2,rows.stdout
 print('PASS: two typed positive and eleven receiver/alias/type/result/comment negative attribution controls',flush=True)
 for text in [control.read_text().replace('static Block rubber_silt;void init()', 'static Block rubber_silt=null;void init()',1),control.read_text().replace('void positive(int x){','void positive(int x){ModBlocks.rubber_silt=alias;'),control.read_text().replace('new BlockGeneric(Material.ROCK).setHardness(1)','new BlockGeneric(Material.ROCK){}')]:
  negative=t/'Negative.java';negative.write_text(text)
  try:scan('RegisteredWriteScanner',[negative])
  except subprocess.CalledProcessError:pass
  else:raise AssertionError('assignment closure mutation accepted')
 print('PASS: field initializer, aliased reassignment and anonymous-subclass constructor controls rejected',flush=True)
 if a.attribute:
  cpf.write_text(cp) # Whole-source attribution must not see the bounded test collaborators.
  files=sorted((R/'src/main/java').rglob('*.java'));original_root=t/'original';original_paths={}
  with zipfile.ZipFile(H/'baseline.zip')as z:
   for rel in m['files']:
    q=original_root/rel;q.parent.mkdir(parents=True,exist_ok=True);q.write_bytes(z.read(rel));original_paths[str(R/rel)]=q
  rows=scan('RegisteredWriteScanner',[original_paths.get(str(q),q)for q in files]);actual={}
  for line in rows.stdout.splitlines():
   r=line.split('\t');q=Path(r[0]);rel=str(q.relative_to(original_root)if str(q).startswith(str(original_root))else q.relative_to(R));actual[(rel,int(r[1]))]=r
  retained=[]
  for rel,es in m['files'].items():
   before=(original_root/rel).read_bytes().decode()
   for e in es:
    row=actual[(rel,e['start'])];assert row[4]==e['field'] and int(row[7])==e['arity'];assert before[int(row[8]):int(row[9])]==e['receiver'];assert [before[int(row[i]):int(row[i+1])]for i in range(12,len(row)-1,2)]==e['arguments'];row[0]=rel;retained.append('\t'.join(row))
  assignments=[l.split('\t')for l in rows.stderr.splitlines()if l.startswith('FIELD\t')];assert len(assignments)==32
  expected={f:('net.minecraft.block.BlockFalling'if o=='BlockFalling'else 'com.hbm.blocks.'+('bomb.'if o=='BlockCrashedBomb'else 'generic.')+o)for f,o in m['owners'].items()}
  assert {r[1]:r[2]for r in assignments}==expected
  assert rows.stderr.count('ALIAS\tBlock4\tcom.hbm.blocks.ModBlocks.crashed_balefire')==1
  (H/'attributed-before.tsv').write_text('\n'.join(sorted(retained))+'\n');(H/'assignments.tsv').write_text('\n'.join('\t'.join(r[1:3])for r in assignments)+'\n')
  print(rows.stderr,end='');print('PASS: all 72 original invocation receiver/target/argument positions and 32 exact constructor assignments attributed; sole Block4 initializer verified',flush=True)
  rows=scan('NativeWriteScanner',files);actual={(str(Path(l.split('\t')[0]).relative_to(R)),int(l.split('\t')[1]))for l in rows.stdout.splitlines()};expected=set()
  for rel,es in m['files'].items():
   for e in es:
    pos=e['start']+sum(len(q['replacement'])-len(q['original'])for q in es if q['start']<e['start']);pos+=e['replacement'].index('com.hbm.world.WorldBlockState.write');expected.add((rel,pos))
  assert expected<=actual and len(expected)==72,expected-actual
  (H/'attributed-after.tsv').write_text('\n'.join(rel+'\t'+str(pos)for rel,pos in sorted(expected))+'\n')
  print(rows.stderr,end='');print('PASS: all 72 exact new native IBlockState boundary positions resolve; NOT whole-mod compilation',flush=True)
 if a.mutants:
  for rel,old,new in [('com/hbm/blocks/BlockMulti.java','meta & 15','0'),('com/hbm/blocks/BlockMulti.java','meta & 15','meta % 4'),('com/hbm/blocks/generic/BlockDecoCT.java','metadata & 15','0')]:
   production=R/'src/main/java'/rel;original=production.read_text();changed=original.replace(old,new);assert changed!=original;mutant=t/production.name;mutant.write_text(changed);run([javac,'-cp',runtime,'-d',out,mutant]);result=subprocess.run(list(map(str,[java,'-Xmx1g','-cp',runtime,'RegisteredWriteTest',a.legacy_client,legacy,forge])),cwd=t,stdout=subprocess.PIPE,stderr=subprocess.STDOUT);assert result.returncode!=0,rel;run([javac,'-cp',runtime,'-d',out,production])
  print('PASS: zeroed Multi, creative-count normalized Multi and zeroed CT production-codec mutants rejected',flush=True)
  generated=generated[0];original=generated.read_text();mutants=[original.replace('nativeRegisteredWriteX >= -30000000 && nativeRegisteredWriteX < 30000000','true'),original.replace('if (nativeRegisteredWriteWorld == null) throw new NullPointerException();',''),original.replace('nativeRegisteredWriteBlock.getStateFromMeta(nativeRegisteredWriteMeta)','nativeRegisteredWriteBlock.getStateFromMeta(0)'),original.replace(', nativeRegisteredWriteFlags);',', 3);'),original.replace('if (nativeRegisteredWriteWorld == null)', 'nativeRegisteredWriteBlock.getStateFromMeta(nativeRegisteredWriteMeta); if (nativeRegisteredWriteWorld == null)')]
  for i,changed in enumerate(mutants):
   assert changed!=original;generated.write_text(changed);run([javac,'-cp',runtime,'-d',out,generated]);result=subprocess.run(list(map(str,[java,'-cp',runtime,'RegisteredEvaluation'])),cwd=t,stdout=subprocess.PIPE,stderr=subprocess.STDOUT);assert result.returncode!=0,('capture mutant survived',i)
  print('PASS: five generated capture bounds/null/metadata/flags/eager-conversion mutants rejected',flush=True)
  count=0
  with zipfile.ZipFile(H/'baseline.zip')as z:
   for rel,es in m['files'].items():
    current=(R/rel).read_bytes().decode();expected=z.read(rel).decode()
    for q in sorted(es,key=lambda q:q['start'],reverse=True):expected=expected[:q['start']]+q['replacement']+expected[q['end']:]
    assert current==expected
    for e in es:
     pos=e['start']+sum(len(q['replacement'])-len(q['original'])for q in es if q['start']<e['start']);changed=current[:pos]+'{}'+current[pos+len(e['replacement']):];assert changed!=expected;count+=1
  assert count==72;print('PASS: every one of 72 production-call deletion mutants violates exact-source regeneration',flush=True)
print('PASS: bounded registered-state write proof complete, not full mod compilation')
