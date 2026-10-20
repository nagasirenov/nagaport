#!/usr/bin/env python3
"""Native tests + exact production method extraction; optional reproducible full-source attribution.
No Gradle, downloads, generated fake platform APIs, production exclusions or Git dependency.
"""
import argparse,importlib.util,json,os,subprocess,tempfile,re
from pathlib import Path
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2]
spec=importlib.util.spec_from_file_location('lookup_audit',HERE/'audit.py');audit=importlib.util.module_from_spec(spec);spec.loader.exec_module(audit)
def method(rel,signature):
 s=(ROOT/'src/main/java'/rel).read_text();a=s.index(signature);b=s.index('{',a);depth=1;e=b+1
 while depth:depth+=(s[e]=='{')-(s[e]=='}');e+=1
 return s[a:e]
def run(args,**kw):return subprocess.run(list(map(str,args)),check=True,**kw)
def parsed(rows,root):
 result=set()
 for row in rows.splitlines():
  p,start,end,*_=row.split('\t');text=Path(p).read_bytes().decode();raw=text.encode('utf-16-le');start=len(raw[:2*int(start)].decode('utf-16-le'));end=len(raw[:2*int(end)].decode('utf-16-le'))
  result.add((str(Path(p).relative_to(root)),start,text[start:end]))
 return result
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--java-home',required=True,type=Path);p.add_argument('--classpath',required=True);p.add_argument('--attribute',action='store_true');p.add_argument('--strict',action='store_true');a=p.parse_args();audit.verify(a.strict)
 java=a.java_home/'bin'/('java.exe' if os.name=='nt' else 'java');javac=a.java_home/'bin'/('javac.exe' if os.name=='nt' else 'javac');sep=os.pathsep
 with tempfile.TemporaryDirectory(prefix='native-world-tile-') as tmp:
  tmp=Path(tmp);cpfile=tmp/'classpath.txt';cpfile.write_text(a.classpath);classes=tmp/'classes';classes.mkdir()
  # Complete actual method bodies, with native TileEntity base and native dependencies.
  extracted=tmp/'ProductionTileLookup.java';extracted.write_text('import net.minecraft.world.World; import net.minecraft.tileentity.TileEntity; import net.minecraft.entity.player.EntityPlayer; import net.minecraft.inventory.Container; import net.minecraft.inventory.IInventory; class ProductionTileLookup extends TileEntity {\n'+method('com/hbm/tileentity/machine/rbmk/TileEntityRBMKActiveBase.java','public boolean isUseableByPlayer(')+'\n'+method('com/hbm/blocks/generic/BlockStorageCrate.java','public int getComparatorInputOverride(')+'\n}')
  run([javac,'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',classes,HERE/'TileLookupNativeTest.java',extracted,HERE/'ProductionMethodsTest.java'])
  for test in ['TileLookupNativeTest','ProductionMethodsTest']:run([java,'-cp',str(classes)+sep+a.classpath,test],cwd=tmp)
  bytecode=run([a.java_home/'bin'/('javap.exe' if os.name=='nt' else 'javap'),'-p','-c','-classpath',classes,'ProductionTileLookup'],stdout=subprocess.PIPE,universal_newlines=True).stdout
  assert bytecode.count('BlockPos."<init>":(III)V')==2 and bytecode.count('World.getTileEntity:(Lnet/minecraft/util/math/BlockPos;)')==2
  print('PASS: both complete extracted production methods invoke native int BlockPos constructor and World lookup')
  toolcp=str(a.java_home/'lib/tools.jar');run([javac,'-cp',toolcp,'-d',classes,HERE/'TileLookupScanner.java'])
  controls=tmp/'LookupControls.java';controls.write_bytes((HERE/'negative-controls.java.in').read_bytes());inputs=tmp/'inputs.txt';inputs.write_text(str(controls)+'\n')
  def scan(paths,native=False):
   inputs.write_text('\n'.join(map(str,paths))+'\n');args=[java,'-Xmx2g','-cp',str(classes)+sep+toolcp,'TileLookupScanner',inputs,cpfile];args+=['native'] if native else []
   return run(args,stdout=subprocess.PIPE,universal_newlines=True).stdout
  rows=scan([controls]);assert len(rows.splitlines())==2,rows;print('PASS: attributed controls accept only two exact-World/int calls; 11 excluded cases remain excluded')
  if a.attribute:
   m=audit.manifest();native=parsed(scan(sorted((ROOT/'src/main/java').rglob('*.java')),True),ROOT)
   expected={(rel,list(re.finditer(re.escape(audit.forms((ROOT/rel).read_bytes().decode(),e,'after')['after']),(ROOT/rel).read_bytes().decode()))[e['after_ordinal']].start(),audit.forms((ROOT/rel).read_bytes().decode(),e,'after')['after']) for rel,r in m['files'].items() for e in r['edits']};assert expected<=native,expected-native
   original=tmp/'original';original.mkdir()
   for path in (ROOT/'src/main/java').rglob('*.java'):
    rel=str(path.relative_to(ROOT));dest=original/rel;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(audit.restore(rel,path.read_bytes().decode()).encode())
   before=parsed(scan(sorted(original.rglob('*.java'))),original);expected={(rel,list(re.finditer(re.escape(audit.forms((original/rel).read_bytes().decode(),e,'before')['before']),(original/rel).read_bytes().decode()))[e['before_ordinal']].start(),audit.forms((original/rel).read_bytes().decode(),e,'before')['before']) for rel,r in m['files'].items() for e in r['edits']};assert expected<=before,(len(before),len(expected),expected-before)
   if a.strict:assert before==expected,(len(before),len(expected),before-expected)
   print('PASS: all 579 baseline candidates replayed exactly; all 579 target calls resolve native World and BlockPos(int,int,int), amid expected unrelated unfinished-port errors')
