from pathlib import Path
import subprocess,tempfile,os,zipfile,hashlib,json,io
H=Path(__file__).resolve().parent;R=H.parents[2];J=Path('/workspace/shared/nagaport-toolchain/java8');CP=Path('/workspace/shared/nagaport-toolchain/native-classpath.txt').read_text().strip();OLD=Path('/tmp/nagaport-legacy-client.jar');F=Path('/tmp/nagaport-forge1614-userdev.jar')
assert hashlib.sha256(OLD.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
assert hashlib.sha256(F.read_bytes()).hexdigest()=='6ed364c0defdb481a41ef2d99c02d382f16fc42a6ea0ea1c2b849848d8466e91'
srg=zipfile.ZipFile('/tmp/nagaport1710-srg.zip').read('joined.srg').decode()
for row in ['FD: bex/i net/minecraft/client/gui/inventory/GuiContainer/field_147003_i','FD: bex/r net/minecraft/client/gui/inventory/GuiContainer/field_147009_r','MD: bex/c (IIIIII)Z net/minecraft/client/gui/inventory/GuiContainer/func_146978_c (IIIIII)Z']:assert row in srg
with zipfile.ZipFile(F) as z:
 for a in ['forgepatches.zip','fmlpatches.zip']:
  with zipfile.ZipFile(io.BytesIO(z.read(a))) as q:
   for n in q.namelist():
    if n.endswith('/GuiContainer.java.patch'):
     text=q.read(n).decode();assert not any('func_146978_c' in line for line in text.splitlines() if line[:1] in '+-' and not line.startswith(('+++','---')));(H/('original-'+a+'.txt')).write_text(text)
M=json.loads((H/'manifest.json').read_text())
with zipfile.ZipFile(H/'baseline.zip') as z:
 for p,d in M['files'].items():
  b=z.read(p);n=(R/p).read_bytes();assert hashlib.sha256(b).hexdigest()==d['before'];assert hashlib.sha256(n).hexdigest()==d['after'];assert b.replace(b'this.func_146978_c(',b'this.isPointInRegion(')==n;assert n.replace(b'this.isPointInRegion(',b'this.func_146978_c(')==b
print('PASS four exact whole-file inverses; five method-name-only edits')
with tempfile.TemporaryDirectory(prefix='native-hit-region-') as td:
 d=Path(td);old=d/'original.jar'
 with zipfile.ZipFile(OLD) as z,zipfile.ZipFile(old,'w') as out:
  for i in z.infolist():
   if i.filename.upper().startswith('META-INF/')and i.filename.upper().endswith(('.SF','.RSA','.DSA')):continue
   out.writestr(i,z.read(i.filename))
 cp=str(d)+os.pathsep+CP+os.pathsep+str(old)
 subprocess.run([str(J/'bin/javac'),'-cp',cp,'-d',str(d),str(H/'NativeHitRegionTest.java')],check=True);subprocess.run([str(J/'bin/java'),'-cp',cp,'NativeHitRegionTest'],check=True)
