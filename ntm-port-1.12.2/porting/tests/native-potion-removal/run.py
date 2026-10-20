#!/usr/bin/env python3
"""Offline: run.py JAVA_HOME CLASSPATH_FILE ORIGINAL_CLIENT_JAR"""
from pathlib import Path
import sys,json,hashlib,subprocess,tempfile,zipfile
here=Path(__file__).resolve().parent;root=here.parents[3]
java,cpf,legacy=map(Path,sys.argv[1:4]);cp=cpf.read_text().strip();m=json.loads((here/'manifest.json').read_text())
assert hashlib.sha256(legacy.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
def run(args):
 p=subprocess.run(list(map(str,args)),stdout=subprocess.PIPE,stderr=subprocess.PIPE,text=True)
 assert p.returncode==0,p.stdout+p.stderr
 return p.stdout
originals={};n=0
for rel,f in m['files'].items():
 before=subprocess.check_output(['git','-C',root,'show',m['baseline']+':'+rel]);actual=(root/rel).read_bytes();originals[rel]=before
 assert hashlib.sha256(before).hexdigest()==f['before'];assert hashlib.sha256(actual).hexdigest()==f['after']
 raw=before.decode().encode('utf-16-le');shift=0;reverse=[]
 for e in sorted(f['edits'],key=lambda x:x['start']):
  reverse.append((e['start']+shift,e['start']+shift+len(e['new']),e));shift+=len(e['new'])-(e['end']-e['start'])
 for e in sorted(f['edits'],key=lambda x:x['start'],reverse=True):
  a,b=e['start']*2,e['end']*2;assert raw[a:b].decode('utf-16-le')==e['old'];raw=raw[:a]+e['new'].encode('utf-16-le')+raw[b:];n+=1
 assert raw.decode('utf-16-le').encode()==actual
 for a,b,e in reversed(reverse):
  assert raw[a*2:b*2].decode('utf-16-le')==e['new'];raw=raw[:a*2]+e['old'].encode('utf-16-le')+raw[b*2:]
 assert raw.decode('utf-16-le').encode()==before
print('PASS',n,'UTF16 exact edits and complete source inverse; other calls, ordering and arguments byte-identical.')
with tempfile.TemporaryDirectory(prefix='potion-remove-proof-') as tmp:
 t=Path(tmp);classes=t/'classes';classes.mkdir();old=t/'original-classes';old.mkdir()
 # Byte-identical original class entries, extracted to avoid default-package signed-JAR mixing with test subclass.
 with zipfile.ZipFile(legacy) as z:
  for name in z.namelist():
   if name.endswith('.class'):
    p=old/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(z.read(name));assert p.read_bytes()==z.read(name)
 c=cp+':'+str(java/'lib/tools.jar')+':'+str(old)
 run([java/'bin/javac','-cp',c,'-d',classes,*here.glob('*.java')])
 print(run([java/'bin/java','-cp',str(classes)+':'+c,'RemovalParity']).strip())
 cases=[('import net.minecraft.potion.Potion;class T {void f(net.minecraft.entity.EntityLivingBase e){e.removePotionEffect(Potion.hunger.id);}}',1),('import net.minecraft.potion.Potion;class T {void f(net.minecraft.entity.EntityLivingBase e){e.removePotionEffect(Potion.weakness.getId());}}',1),('import net.minecraft.potion.Potion;class T {Object x=Potion.hunger;}',0),('import net.minecraft.potion.Potion;class T {int x=Potion.hunger.id;}',0),('import net.minecraft.potion.Potion;import net.minecraft.potion.PotionEffect;class T {Object x=new PotionEffect(Potion.hunger.id,20,1);}',0),('import net.minecraft.potion.Potion;class T {void f(Other e){e.removePotionEffect(Potion.hunger.id);}}',0),('import net.minecraft.potion.Potion;class T {Object Potion;void f(net.minecraft.entity.EntityLivingBase e){e.removePotionEffect(Potion.hunger.id);}}',0),('import net.minecraft.potion.*;class T {void f(net.minecraft.entity.EntityLivingBase e){e.removePotionEffect(Potion.hunger.id);}}',0),('class T {void f(net.minecraft.entity.EntityLivingBase e){e.removePotionEffect(net.minecraft.potion.Potion.hunger.id);}}',1),('import net.minecraft.potion.Potion;class T extends Parent {void f(net.minecraft.entity.EntityLivingBase e){e.removePotionEffect(Potion.hunger.id);}}',0)]
 paths=[]
 for i,(src,n) in enumerate(cases):
  p=t/('T'+str(i)+'.java');p.write_text(src.replace('class T ','class T'+str(i)+' '));paths.append(p)
 p=t/'Parent.java';p.write_text('class Parent {Object Potion;} class Other {void removePotionEffect(int id){}}');paths.append(p)
 inputs=t/'inputs';inputs.write_text('\n'.join(map(str,paths)))
 def scan():return run([java/'bin/java','-Xmx3g','-cp',str(classes)+':'+c,'PotionRemovalScanner',here/'map.tsv',inputs,cpf])
 rows=[r.split('\t') for r in scan().splitlines()]
 for i,(_,n) in enumerate(cases):assert sum(r[0]==str(paths[i]) for r in rows)==n,(i,rows)
 print('PASS',len(cases),'native owner, shadow, foreign receiver and application exclusion fixtures.')
 sources=t/'sources';paths=[]
 for p in (root/'ntm-port-1.12.2/src/main/java').rglob('*.java'):
  rel=p.relative_to(root).as_posix();dest=sources/rel;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(originals.get(rel,p.read_bytes()));paths.append(dest)
 inputs.write_text('\n'.join(map(str,paths)));normalized=[]
 for row in scan().splitlines():
  r=row.split('\t');r[0]=Path(r[0]).relative_to(sources).as_posix();normalized.append('\t'.join(r))
 assert sorted(normalized)==sorted((here/'attributed-accesses.tsv').read_text().splitlines())
 print('PASS complete-source pre-edit native owner re-attribution; diagnostics are not compile success.')
 inputs.write_text('\n'.join(str(root/r) for r in originals));assert not scan()
 print('PASS changed-source idempotence.')
