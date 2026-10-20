#!/usr/bin/env python3
"""Offline native complete range-method projections and original engine differential oracle."""
from pathlib import Path
import hashlib,json,os,re,subprocess,tempfile,zipfile
import source
H=Path(__file__).resolve().parent;ROOT=H.parents[2];M=source.M
J=Path(os.environ.get('JAVA8_HOME','/workspace/shared/nagaport-toolchain/java8'))
CP=Path(os.environ.get('NATIVE_CP_FILE','/workspace/shared/nagaport-toolchain/native-classpath.txt')).read_text().strip()
OLD=Path(os.environ.get('LEGACY_CLIENT_JAR','/tmp/nagaport-legacy-client.jar'))
def run(cmd,cwd=None):
 p=subprocess.run(list(map(str,cmd)),stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,cwd=cwd)
 assert p.returncode==0,p.stdout
 return p.stdout
def constructors(s,name):
 result=[]
 for x in re.finditer(r'public '+name+r'\([^)]*\)\s*\{',s):
  start=x.end();depth=1;end=start
  while depth:depth+=(s[end]=='{')-(s[end]=='}');end+=1
  result.append((x.group(),s[start:end-1]))
 return result
source.check()
assert hashlib.sha256(OLD.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
classes=[];cases=[];weights=[];closure=[]
with zipfile.ZipFile(H/'baseline.zip') as z:
 for rel,item in M['files'].items():
  old=z.read(rel).decode();current=(ROOT/rel).read_text();name=Path(rel).stem
  oldctors=constructors(old,name);newctors=constructors(current,name)
  assert len(oldctors)==len(newctors)
  for (sig,body),(new_sig,new_body) in zip(oldctors,newctors):
   assert sig==new_sig
   expected=body.replace('renderDistanceWeight','renderRangeWeight') if item['variable_weight'] else re.sub(r'^[ \t]*this\.renderDistanceWeight (?:= (?:10|15|24)\.0D|\*= 10);\r?\n','',body,flags=re.M)
   assert new_body==expected
   closure.append({'owner':name,'signature':sig,'old_weight_writes':re.findall(r'this\.renderDistanceWeight[^;]*;',body),'delegates_this':bool(re.search(r'\bthis\(',body))})
  if not item['range_method']:continue
  method=item['range_method'];assert method in current
  field='private double renderRangeWeight = 1.0D;' if item['variable_weight'] else ''
  ctorswitch=''
  for i,(sig,body) in enumerate(newctors):
   if item['variable_weight']:
    assert not re.search(r'\bthis\(',body),name
    assignments=re.findall(r'this\.renderRangeWeight = [^;]+;',body)
    assert len(assignments)<=1
    ctorswitch+='case %d: %s break;\n'%(i,' '.join(assignments))
    weight='10.0D' if assignments else '1.0D'
   else:
    weight=item['effective_weight']
   cases.append('new '+name+'Projection('+str(i)+')');weights.append(weight)
  classes.append('static class '+name+'Projection extends Probe {'+field+' '+name+'Projection(int ctor){super();switch(ctor){'+ctorswitch+'}}'+method+'}')
 # Preserve complete inherited method of weight-ignoring EntityThrowableNT.
 throwable=z.read('src/main/java/com/hbm/entity/projectile/EntityThrowableNT.java').decode()
 method=re.search(r'public boolean isInRangeToRenderDist\(double dist\) \{[^{}]*\}',throwable).group()
 classes.append('static class ThrowableProjection extends Probe {'+method+'}')
 # Original 1.7 EntityThrowableNT implementation is the identical body modulo established bounding-box accessor migration.
 assert 'renderDistanceWeight' not in method and 'perimeter = this.getEntityBoundingBox().getAverageEdgeLength() * 4.0D;' in method
 cases.append('new ThrowableProjection()');weights.append('4.0D')
assert json.loads((H/'constructor-inventory.json').read_text())==closure,'constructor inventory drift'
with tempfile.TemporaryDirectory(prefix='entity-render-distance-') as td:
 d=Path(td); native=(H/'NativeRangeTest.java.in').read_text().replace('@PROJECTIONS@','\n'.join(classes))
 native=native.replace('@CASES@','Probe[] entities={'+','.join(cases)+'}; double[] weights={'+','.join(weights)+'};')
 native=native.replace('@NULL_CHECKS@','for(Probe e:entities){e.setEntityBoundingBox(null);e.boxReads=0;try{e.isInRangeToRenderDist(0);throw new AssertionError("null box");}catch(NullPointerException expected){require(e.boxReads==1,"null box single read");checks++;}}')
 nf=d/'NativeRangeTest.java';nf.write_text(native)
 old=(H/'LegacyRangeTest.java.in').read_text().replace('@WEIGHTS@','double[] weights={'+','.join(weights)+'};')
 lf=d/'LegacyRangeTest.java';lf.write_text(old)
 logs=[]
 logs.append(run([J/'bin/javac','-source','8','-target','8','-cp',CP,'-d',d,nf,H/'RangeVectors.java']))
 logs.append(run([J/'bin/java','-cp',str(d)+':'+CP,'NativeRangeTest',d/'native.tsv'],cwd=d))
 logs.append(run([J/'bin/javac','-source','8','-target','8','-cp',OLD,'-d',d,lf,H/'RangeVectors.java']))
 # Preserve every original class byte; omit only signature metadata for a default-package subclass test.
 unsigned=d/'original-test.jar'
 with zipfile.ZipFile(OLD) as src,zipfile.ZipFile(unsigned,'w') as out:
  for info in src.infolist():
   if info.filename.upper().startswith('META-INF/') and info.filename.upper().endswith(('.SF','.RSA','.DSA')):continue
   data=src.read(info.filename);out.writestr(info,data)
 logs.append(run([J/'bin/java','-cp',str(d)+':'+str(unsigned)+':'+CP,'LegacyRangeTest',d/'legacy.tsv'],cwd=d))
 a=(d/'native.tsv').read_bytes();b=(d/'legacy.tsv').read_bytes();assert a==b,'native/original differential mismatch'
 # Original field is private static in the native engine; direct field access must remain a compiler error.
 bad=d/'BadPrivateField.java';bad.write_text('import net.minecraft.entity.Entity; class BadPrivateField {void f(Entity e){e.renderDistanceWeight=10.0D;}}')
 p=subprocess.run([str(J/'bin/javac'),'-cp',CP,'-d',str(d),str(bad)],stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True)
 assert p.returncode and 'renderDistanceWeight has private access in Entity' in p.stdout
 logs.append('PASS rejected private native direct-field write; no reflection, access transformer or global setter used in production.\n')
 logs.append('PASS %d constructor paths, %d exact complete range-method projections, actual native and original range decisions match byte-for-byte (%s).\n'%(len(closure),len(classes),hashlib.sha256(a).hexdigest()))
 text=''.join(logs);(H/'proof.log').write_text(text);print(text,end='')
