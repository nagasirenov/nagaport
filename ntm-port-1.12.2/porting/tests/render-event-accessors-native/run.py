#!/usr/bin/env python3
"""Reconstructed native proof runner. Output is fresh only when this command actually succeeds."""
import os,argparse,collections,hashlib,json,re,subprocess,tempfile,zipfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--java-home',required=True);p.add_argument('--classpath-file',required=True);p.add_argument('--forge1614',required=True);p.add_argument('--original-root',required=True);p.add_argument('--verify-attribution',action='store_true');a=p.parse_args()
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2]
cp=Path(a.classpath_file).read_text().strip();java=Path(a.java_home)/'bin/java';javac=Path(a.java_home)/'bin/javac';tools=Path(a.java_home)/'lib/tools.jar'
assert cp and Path(a.forge1614).is_file() and java.is_file() and tools.is_file()
for path in cp.split(os.pathsep):
 if path.endswith('.jar'):assert Path(path).is_file(),('Missing actual dependency',path)
def run(cmd,**kw):return subprocess.run(list(map(str,cmd)),check=True,text=True,**kw)
run(['python3',HERE/'check_source.py'])
manifest=json.loads((HERE/'source-manifest.json').read_text());rules=[line.split('\t') for line in (HERE/'rules.tsv').read_text().splitlines()]
with tempfile.TemporaryDirectory(prefix='render-event-proof-') as td:
 t=Path(td); rows=[e for f in manifest['files'] for e in f['edits']]
 methods=['static Object read%d(%s event) { return event.%s; }'%(i,e['receiver'],e['new']) for i,e in enumerate(rows)]
 (t/'SelectedReads.java').write_text('public class SelectedReads {static final String[] GETTERS={'+','.join('"'+e['new'][:-2]+'"' for e in rows)+'};\n'+'\n'.join(methods)+'\n}')
 production=(ROOT/'src/main/java/com/hbm/main/ModEventHandlerClient.java').read_text()
 original=(Path(a.original_root)/'src/main/java/com/hbm/main/ModEventHandlerClient.java').read_text()
 pattern=r'public void preRenderEventFirst\(RenderLivingEvent.Pre event\) \{.*?\n\t\}'
 method=re.search(pattern,production,re.S).group();old=re.search(pattern,original,re.S).group()
 assert method.replace('event.getEntity()','event.entity')==old
 (t/'ExtractedHandler.java').write_text('import net.minecraftforge.client.event.RenderLivingEvent;import net.minecraft.entity.Entity;public class ExtractedHandler {'+method+"""
 static class MainRegistry {static final Proxy proxy=new Proxy();}
 static class Proxy {Entity seen;int calls;boolean value,fail;boolean isVanished(Entity e){calls++;seen=e;if(fail)throw new IllegalArgumentException();return value;}}
 }""")
 (t/'HandlerProof.java').write_text((HERE/'HandlerProof.java.in').read_text())
 legacy='public class LegacyReads {'+'\n'.join('static Object read%d(%s event){return event.%s;}'%(i,e['receiver'],e['old']) for i,e in enumerate(rows))+'}'
 (t/'LegacyReads.java').write_text(legacy)
 failed=subprocess.run([str(javac),'-cp',cp,'-Xmaxerrs','1000','-d',str(t),str(t/'LegacyReads.java')],capture_output=True,text=True)
 assert failed.returncode!=0 and failed.stderr.count('has private access')==65,failed.stderr
 print('PASS 65 reversed-field negative compile controls; complete pristine preRenderEventFirst callback inversion',flush=True)
 run([javac,'-cp',cp+os.pathsep+str(tools),'-d',t,HERE/'RenderEventScanner.java',HERE/'NativeRenderEventTest.java',HERE/'NativeRenderContext.java',t/'SelectedReads.java',t/'ExtractedHandler.java',t/'HandlerProof.java'])
 for jar in cp.split(os.pathsep):
  if jar.endswith('.jar'):
   with zipfile.ZipFile(jar) as z:
    for name in z.namelist():
     if name in ('liblwjgl64.so','liblwjgl.so'):(t/name).write_bytes(z.read(name))
 env=dict(os.environ,LIBGL_ALWAYS_SOFTWARE='1',MESA_SHADER_CACHE_DIR=str(t/'mesa-cache'))
 run([java,'-Djava.library.path='+str(t),'-cp',str(t)+os.pathsep+cp,'NativeRenderEventTest',Path(a.classpath_file).resolve(),HERE/'rules.tsv'],env=env,cwd=t)
 selected=(t/'SelectedReads.java').read_text();mutant=selected.replace('return event.getResolution();','return event.getType();',1)
 assert mutant!=selected;(t/'SelectedReads.java').write_text(mutant)
 run([javac,'-cp',cp,'-d',t,t/'SelectedReads.java'])
 result=subprocess.run([str(java),'-Djava.library.path='+str(t),'-cp',str(t)+os.pathsep+cp,'NativeRenderEventTest',str(Path(a.classpath_file).resolve()),str(HERE/'rules.tsv')],env=env,cwd=t,capture_output=True,text=True)
 assert result.returncode!=0 and 'compiled selected production token read0' in result.stderr,(result.stdout,result.stderr)
 (t/'SelectedReads.java').write_text(selected);run([javac,'-cp',cp,'-d',t,t/'SelectedReads.java'])
 print('PASS compiled wrong-getter runtime mutant rejected by native payload assertions',flush=True)
 old_dump=[];native_dump=[]
 with zipfile.ZipFile(a.forge1614) as z:
  (t/'original-forge.jar').write_bytes(z.read('binaries.jar'))
  for owner in dict.fromkeys(r[0] for r in rules):
   source=z.read('src/main/java/'+owner.replace('.','/')+'.java').decode()
   dump=run([Path(a.java_home)/'bin/javap','-p','-c','-classpath',t/'original-forge.jar',owner],capture_output=True).stdout;old_dump.append(dump)
   for _,field,getter,typ in (r for r in rules if r[0]==owner):
    assert re.search(r'public final \w+ '+field+r'\s*;',source),(owner,field)
    assert re.search(r'this\.'+field+r'\s*=\s*'+field+r'\s*;',source),(owner,field)
    assert 'putfield' in dump and ' '+field+';' in dump and getter+'(' not in dump
   native_dump.append(run([Path(a.java_home)/'bin/javap','-p','-c','-classpath',cp,owner],capture_output=True).stdout)
 print('PASS 18 immutable original Forge1614 fields in 5 owners; genuine original/native bytecode',flush=True)
 fixture=t/'Fixture.java';source_list=t/'fixture-sources';source_list.write_text(str(fixture)+'\n')
 def scan(body):
  fixture.write_text('class Fixture { '+body+' }')
  return run([java,'-cp',str(t)+os.pathsep+str(tools),'RenderEventScanner',source_list,a.classpath_file,HERE/'rules.tsv'],capture_output=True).stdout.splitlines()
 all_reads='\n'.join('Object f%d(%s e){return e.%s;}'%(i,r[0],r[1]) for i,r in enumerate(rules));assert len(scan(all_reads))==18
 overlay='net.minecraftforge.client.event.RenderGameOverlayEvent';res='net.minecraft.client.gui.ScaledResolution'
 cases=[('read','Object f('+overlay+' e){return e.resolution;}',1),('once',''+overlay+' e(){return null;}Object f(){return e().resolution;}',1),('write','void f('+overlay+' e,'+res+' r){e.resolution=r;}',0),('parenthesized_write','void f('+overlay+' e,'+res+' r){(e.resolution)=r;}',0),('compound','void f('+overlay+' e){e.partialTicks+=1;}',0),('postfix','void f('+overlay+' e){(e.partialTicks)++;}',0),('negative_read','float f('+overlay+' e){return -e.partialTicks;}',1),('inherit','static class S extends '+overlay+' {S(){super(0,null);}}Object f(S e){return e.resolution;}',1),('shadow','static class S extends '+overlay+' {Object resolution;S(){super(0,null);}}Object f(S e){return e.resolution;}',0),('override','static class S extends '+overlay+' {S(){super(0,null);}public '+res+' getResolution(){return null;}}Object f(S e){return e.resolution;}',0),('reachable_override','static class S extends '+overlay+' {S(){super(0,null);}public '+res+' getResolution(){return null;}}Object f('+overlay+' e){return e.resolution;}',0),('anonymous_override',overlay+' x=new '+overlay+'(0,null){public '+res+' getResolution(){return null;}};Object f('+overlay+' e){return e.resolution;}',0),('unknown','Object f(Unknown e){return e.resolution;}',0),('unrelated','static class S{Object resolution;}Object f(S e){return e.resolution;}',0),('interface_shadow','interface I{'+res+' resolution=null;}static class S extends '+overlay+' implements I{S(){super(0,null);}}Object f(S e){return e.resolution;}',0),('type_name','Object f(){return '+overlay+'.resolution;}',0),('comment_string','String x="e.resolution";/* e.resolution */',0),('utf16','String x="🌕";Object f('+overlay+' e){return e.resolution;}',1),('lambda','java.util.function.Supplier<Object> f('+overlay+' e){return ()->e.resolution;}',1)]
 for name,body,count in cases:
  result=scan(body);assert len(result)==count,(name,result)
  for row in result:
   parts=row.split('\t');raw=fixture.read_text().encode('utf-16-le');assert raw[int(parts[1])*2:int(parts[2])*2].decode('utf-16-le')==parts[3]
 print('PASS 18 genuine owner signatures and',len(cases),'attributed scope fixtures',flush=True)
 attribution=[]
 if a.verify_attribution:
  sources=t/'all-sources';paths=sorted((ROOT/'src/main/java').rglob('*.java'));sources.write_text('\n'.join(map(str,paths))+'\n')
  result=run([java,'-Xmx3g','-cp',str(t)+os.pathsep+str(tools),'RenderEventScanner',sources,a.classpath_file,HERE/'rules.tsv','post'],capture_output=True)
  print(result.stderr.strip(),flush=True);expected=collections.Counter();actual=collections.Counter()
  for f in manifest['files']:
   shift=0
   for e in f['edits']:
    expected[(f['path'],e['start']+shift,e['old'],e['new'],e['receiver'],e['owner'])]+=1;shift+=len(e['new'])-len(e['old'])
  selected={(e[0],e[1]) for e in expected};unrelated=0
  for row in result.stdout.splitlines():
   path,start,end,old,new,receiver,owner=row.split('\t');rel=str(Path(path).relative_to(ROOT));start=int(start)
   if (rel,start) in selected:actual[(rel,start,old,new,receiver,owner)]+=1
   else:unrelated+=1
  assert actual==expected,(actual-expected,expected-actual)
  print('PASS 65 exact selected native getter re-attributions;',unrelated,'pre-existing getters excluded',flush=True)
  attribution.append(dict(phase='after',diagnostics=result.stderr.strip(),selected=65,existing_excluded=unrelated))
  # Fresh pre-attribution uses complete reconstructed input files, never historical TSV as new output.
  from check_source import reverse
  replacements={}
  for f in manifest['files']:
   copy=t/'pre'/f['path'];copy.parent.mkdir(parents=True,exist_ok=True);copy.write_bytes(reverse((ROOT/f['path']).read_bytes(),f));replacements[ROOT/f['path']]=copy
  sources.write_text('\n'.join(str(replacements.get(path,path)) for path in paths)+'\n')
  result=run([java,'-Xmx3g','-cp',str(t)+os.pathsep+str(tools),'RenderEventScanner',sources,a.classpath_file,HERE/'rules.tsv'],capture_output=True)
  inverse={str(v):str(k.relative_to(ROOT)) for k,v in replacements.items()};before=collections.Counter()
  for row in result.stdout.splitlines():
   path,start,end,old,new,receiver,owner=row.split('\t');rel=inverse[path] if path in inverse else str(Path(path).relative_to(ROOT));before[(rel,int(start),old,new,receiver,owner)]+=1
  expected_before=collections.Counter((f['path'],e['start'],e['old'],e['new'],e['receiver'],e['owner']) for f in manifest['files'] for e in f['edits'])
  assert before==expected_before,(before-expected_before,expected_before-before)
  print(result.stderr.strip(),flush=True);print('PASS fresh pre-attribution reproduces all 65 historical token selections',flush=True)
  attribution.append(dict(phase='before',diagnostics=result.stderr.strip(),selected=65))
 hashes={str(Path(path)):hashlib.sha256(Path(path).read_bytes()).hexdigest() for path in cp.split(os.pathsep) if path.endswith('.jar')}
 hashes[str(Path(a.forge1614))]=hashlib.sha256(Path(a.forge1614).read_bytes()).hexdigest()
 # These files are created only by a successful current execution, not by recovery transcription.
 (HERE/'dependency-sha256-current.json').write_text(json.dumps(hashes,indent=2)+'\n')
 (HERE/'original-bytecode-current.txt').write_text('\n'.join(old_dump));(HERE/'native-bytecode-current.txt').write_text('\n'.join(native_dump))
 (HERE/'attribution-current.json').write_text(json.dumps(attribution,indent=2)+'\n')
print('PASS current bounded render-event proof; full handlers, game, event bus and rendering not certified',flush=True)
