from pathlib import Path
import subprocess,os,json,hashlib,tempfile,zipfile,re,collections
H=Path(__file__).resolve().parent;R=H.parents[2];J=Path('/workspace/shared/nagaport-toolchain/java8');CPFILE=Path('/tmp/nagaport-ae2-native-classpath.txt');CP=CPFILE.read_text().strip();M=json.loads((H/'source-manifest.json').read_text());rows=[e for f in M['files'] for e in f['edits']]
assert len(rows)==27 and len(M['files'])==6
assert collections.Counter(e['old'] for e in rows)=={'renderer':8,'partialRenderTick':13,'partialTicks':2,'item':4}
def run(args,**kw):return subprocess.run(list(map(str,args)),check=True,text=True,**kw)
def sha(b):return hashlib.sha256(b).hexdigest()
def reverse(data,f):
 assert sha(data)==f['after'];raw=data.decode().encode('utf-16-le');shift=0;edits=[]
 for e in f['edits']:edits.append((e['start']+shift,e));shift+=len(e['new'])-len(e['old'])
 for start,e in reversed(edits):
  a=start*2;b=a+len(e['new'])*2;assert raw[a:b].decode('utf-16-le')==e['new'];raw=raw[:a]+e['old'].encode('utf-16-le')+raw[b:]
 old=raw.decode('utf-16-le').encode();assert sha(old)==f['before'];return old
for f in M['files']:
 data=(R/f['path']).read_bytes();original=reverse(data,f)
 assert original==subprocess.check_output(['git','-C',str(R),'show',M['base']+':ntm-port-1.12.2/'+f['path']])
 shift=0
 for e in f['edits']:
  raw=data.decode().encode('utf-16-le');a=(e['start']+shift)*2;b=a+len(e['new'])*2
  assert raw[a:b].decode('utf-16-le')==e['new']
  bad=(raw[:a]+e['old'].encode('utf-16-le')+raw[b:]).decode('utf-16-le').encode()
  shift+=len(e['new'])-len(e['old'])
  try:reverse(bad,f);raise RuntimeError('source mutation accepted')
  except AssertionError:pass
print('PASS 6 exact whole-file inverses and 27 source mutations',flush=True)
with tempfile.TemporaryDirectory(prefix='event-extension-') as td:
 t=Path(td);methods=['static Object read%d(%s event){return event.%s;}'%(i,e['receiver'],e['new']) for i,e in enumerate(rows)]
 selected='public class SelectedReads {static final String[] GETTERS={'+','.join('"'+e['new'][:-2]+'"' for e in rows)+'};static final Class<?>[] RECEIVERS={'+','.join(e['receiver']+'.class' for e in rows)+'};\n'+'\n'.join(methods)+'\n}'
 (t/'SelectedReads.java').write_text(selected)
 (t/'LegacyReads.java').write_text('public class LegacyReads {'+'\n'.join('static Object read%d(%s event){return event.%s;}'%(i,e['receiver'],e['old']) for i,e in enumerate(rows))+'}')
 old=subprocess.run([str(J/'bin/javac'),'-cp',CP,'-d',str(t),str(t/'LegacyReads.java')],capture_output=True,text=True);assert old.returncode and old.stderr.count('has private access')==27,old.stderr
 run([J/'bin/javac','-cp',CP+os.pathsep+str(J/'lib/tools.jar'),'-d',t,H/'NativeExtensionTest.java',H/'NativeRenderContext.java',H/'RenderEventScanner.java',t/'SelectedReads.java'])
 for p in CP.split(os.pathsep):
  if p.endswith('.jar'):
   with zipfile.ZipFile(p) as z:
    for n in ['liblwjgl64.so','liblwjgl.so']:
     if n in z.namelist():(t/n).write_bytes(z.read(n))
 env=dict(os.environ,LIBGL_ALWAYS_SOFTWARE='1',MESA_SHADER_CACHE_DIR=str(t/'mesa'))
 cmd=[J/'bin/java','-Djava.library.path='+str(t),'-cp',str(t)+os.pathsep+CP,'NativeExtensionTest']
 run(cmd,env=env,cwd=t)
 mutant=selected.replace('return event.getPartialRenderTick();','return Float.valueOf(event.getPartialRenderTick()*0F);',1);assert mutant!=selected;(t/'SelectedReads.java').write_text(mutant);run([J/'bin/javac','-cp',CP,'-d',t,t/'SelectedReads.java'])
 bad=subprocess.run(list(map(str,cmd)),env=env,cwd=t,capture_output=True,text=True);assert bad.returncode and 'selected ' in bad.stderr,bad.stderr
 print('PASS wrong-value compiled getter mutant rejected',flush=True)
 fixture=t/'Fixture.java';sources=t/'sources.txt';sources.write_text(str(fixture)+'\n');rules=H/'rules.tsv'
 def scan(body):
  fixture.write_text('class Fixture {'+body+'}');return run([J/'bin/java','-cp',str(t)+os.pathsep+str(J/'lib/tools.jar'),'RenderEventScanner',sources,CPFILE,rules],capture_output=True).stdout.splitlines()
 typ='net.minecraftforge.client.event.RenderHandEvent';cases=[('read','Object f('+typ+' e){return e.partialTicks;}',1),('write','void f('+typ+' e){e.partialTicks=1;}',0),('compound','void f('+typ+' e){e.partialTicks+=1;}',0),('increment','void f('+typ+' e){e.partialTicks++;}',0),('once',typ+' e(){return null;} Object f(){return e().partialTicks;}',1),('unknown','Object f(Unknown e){return e.partialTicks;}',0),('shadow','static class S extends '+typ+' {float partialTicks;S(){super(null,0,0);}} Object f(S e){return e.partialTicks;}',0),('override','static class S extends '+typ+' {S(){super(null,0,0);}public float getPartialTicks(){return 2;}} Object f('+typ+' e){return e.partialTicks;}',0),('nested','Object f('+typ+' e){return new Object(){Object g(){return e.partialTicks;}};}',1),('comment','String s="e.partialTicks"; /* e.partialTicks */',0)]
 for name,body,n in cases:assert len(scan(body))==n,name
 print('PASS 10 typed scope controls including reachable override, writes and shadow',flush=True)
 proofs=[]
 with zipfile.ZipFile('/tmp/nagaport-forge1614-userdev.jar') as z:
  (t/'old-forge.jar').write_bytes(z.read('binaries.jar'))
  for rule in rules.read_text().splitlines():
   owner,field,getter,typ=rule.split('\t');src=z.read('src/main/java/'+owner.replace('.','/')+'.java').decode();assert re.search(r'public final \w+ '+field+r'\s*;',src)
   if field=='item':assert 'item = itemFrame.func_82335_i();' in src
   else:assert re.search(r'this\.'+field+r'\s*=\s*'+field+r'\s*;',src)
   old=run([J/'bin/javap','-p','-c','-classpath',t/'old-forge.jar',owner],capture_output=True).stdout
   native=run([J/'bin/javap','-p','-c','-classpath',CP,owner],capture_output=True).stdout
   assert 'public final ' in old and ' '+field+';' in old and getter+'(' not in old
   sig=re.search(r'public [^\n]+ '+getter+r'\(\);\s+Code:\s+0: aload_0\s+1: getfield[^\n]+Field '+field+r':[^\n]+\s+4: [af]return',native);assert sig,(owner,getter)
   proofs.append(dict(owner=owner,field=field,getter=getter,original=old,native_getter=sig.group()))
 (H/'bytecode-proof.json').write_text(json.dumps(proofs,indent=2)+'\n')
print('PASS 4 immutable original fields and exact native direct-field getters; no handler/event-bus/game claim',flush=True)
