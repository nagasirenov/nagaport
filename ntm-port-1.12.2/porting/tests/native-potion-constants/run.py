#!/usr/bin/env python3
"""Offline: run.py JAVA_HOME CLASSPATH_FILE ORIGINAL_CLIENT_JAR [--verify-attribution]."""
from pathlib import Path
import sys,subprocess,tempfile,json,hashlib
here=Path(__file__).resolve().parent;root=here.parents[3]
java,cpf,legacy=map(Path,sys.argv[1:4]);cp=cpf.read_text().strip();m=json.loads((here/'manifest.json').read_text())
assert hashlib.sha256(legacy.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
def run(args):
 p=subprocess.run(list(map(str,args)),stdout=subprocess.PIPE,stderr=subprocess.PIPE,text=True)
 assert p.returncode==0,p.stdout+p.stderr
 return p.stdout
originals={};count=0
for rel,f in m['files'].items():
 before=subprocess.check_output(['git','-C',str(root),'show',m['baseline']+':'+rel]);actual=(root/rel).read_bytes();originals[rel]=before
 assert hashlib.sha256(before).hexdigest()==f['before'];assert hashlib.sha256(actual).hexdigest()==f['after']
 raw=before.decode().encode('utf-16-le');shift=0;reverse=[]
 for e in sorted(f['edits'],key=lambda e:e['start']):
  reverse.append((e['start']+shift,e['start']+shift+len(e['new']),e));shift+=len(e['new'])-(e['end']-e['start'])
 for e in sorted(f['edits'],key=lambda e:e['start'],reverse=True):
  a,b=e['start']*2,e['end']*2;assert raw[a:b].decode('utf-16-le')==e['old'];raw=raw[:a]+e['new'].encode('utf-16-le')+raw[b:];count+=1
 assert raw.decode('utf-16-le').encode()==actual
 for a,b,e in reversed(reverse):
  assert raw[a*2:b*2].decode('utf-16-le')==e['new'];raw=raw[:a*2]+e['old'].encode('utf-16-le')+raw[b*2:]
 assert raw.decode('utf-16-le').encode()==before
print('PASS',count,'exact UTF16-position edits and complete inverse preservation in',len(originals),'files; formulas/custom effects/nonselected sources untouched.')
with tempfile.TemporaryDirectory(prefix='potion-proof-') as td:
 t=Path(td);classes=t/'classes';classes.mkdir();c=cp+':'+str(java/'lib/tools.jar')
 print(run([java/'bin/javac','-cp',c,'-d',classes,*here.glob('*.java')]))
 for main,args in [('PotionParity',[here/'map.tsv',legacy]),('EffectExecution',[])]:print(run([java/'bin/java','-cp',str(classes)+':'+c,main,*args]))
 cases=[('import net.minecraft.potion.Potion; class T {Object x=Potion.poison;}',1),('import net.minecraft.potion.*; class T {Object x=Potion.poison;}',0),('import net.minecraft.potion.Potion; class T {Object Potion;Object x=Potion.poison;}',0),('class T {class Potion {static Object poison;}Object x=Potion.poison;}',0),('import net.minecraft.potion.Potion; class T extends Parent {Object x=Potion.poison;}',0),('class T {Object x=net.minecraft.potion.Potion.poison;}',1),('import net.minecraft.potion.Potion; class T {String x="Potion.poison";/* Potion.poison */}',0),('import net.minecraft.potion.Potion; class T {Object x=Potion.damageBoost;Object y=Potion.hunger;}',0),('import net.minecraft.potion.*; class T {Object x=new PotionEffect(Potion.poison.id,20,1);}',0),('import net.minecraft.potion.Potion;import net.minecraft.potion.PotionEffect; class T {Object x=new PotionEffect(Potion.poison.id,20,1);}',1),('import net.minecraft.potion.Potion;import net.minecraft.potion.PotionEffect; class T {Object x=new PotionEffect(Potion.poison.getId(),20,1);}',1),('import net.minecraft.potion.Potion;import net.minecraft.potion.PotionEffect; class T {Object x=new PotionEffect(Potion.poison.id,20,1,true);}',0),('import net.minecraft.potion.Potion; class T {int x=Potion.poison.id;}',0),('import net.minecraft.potion.Potion;class T {void f(net.minecraft.entity.EntityLivingBase e){e.removePotionEffect(Potion.poison.id);}}',1),('import net.minecraft.potion.Potion;class T {void f(Other e){e.removePotionEffect(Potion.poison.id);}}',0),('import net.minecraft.potion.Potion;class T {String s="😀";Object x=Potion.poison;}',1),('import net.minecraft.potion.Potion;class T {Object x=Potion.\\u0070oison;}',0)]
 paths=[]
 for i,(src,n) in enumerate(cases):
  f=t/('T'+str(i)+'.java');f.write_text(src.replace('class T ','class T'+str(i)+' ').replace('class T {','class T'+str(i)+' {'));paths.append(f)
 parent=t/'Parent.java';parent.write_text('class Parent {Object Potion;} class Other {void removePotionEffect(int id){}}')
 inputs=t/'inputs';inputs.write_text('\n'.join(map(str,paths+[parent])))
 out=run([java/'bin/java','-cp',str(classes)+':'+c,'PotionAccessScanner',here/'map.tsv',inputs,cpf]);rows=[r.split('\t') for r in out.splitlines()]
 for i,(_,n) in enumerate(cases):assert sum(r[0]==str(paths[i]) for r in rows)==n,(i,rows)
 print('PASS',len(cases),'attributed owner/shadow/inheritance/custom/constructor/receiver/Unicode controls.')
 # Re-scan changed production for idempotence, requiring zero new accepted edits.
 inputs.write_text('\n'.join(str(root/r) for r in originals));out=run([java/'bin/java','-Xmx2g','-cp',str(classes)+':'+c,'PotionAccessScanner',here/'map.tsv',inputs,cpf]);assert not out,out
 print('PASS changed-source idempotence; unfinished-unit attribution errors are not compile success.')
 if '--verify-attribution' in sys.argv:
  sources=t/'sources';paths=[]
  for f in (root/'ntm-port-1.12.2/src/main/java').rglob('*.java'):
   rel=f.relative_to(root).as_posix();dest=sources/rel;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(originals.get(rel,f.read_bytes()));paths.append(dest)
  inputs.write_text('\n'.join(map(str,paths)));out=run([java/'bin/java','-Xmx3g','-cp',str(classes)+':'+c,'PotionAccessScanner',here/'map.tsv',inputs,cpf]);normalized=[]
  for row in out.splitlines():
   r=row.split('\t');r[0]=Path(r[0]).relative_to(sources).as_posix();normalized.append('\t'.join(r))
  assert sorted(normalized)==sorted((here/'attributed-accesses.tsv').read_text().splitlines())
  print('PASS complete production owner re-attribution against exact pre-edit sources.')
