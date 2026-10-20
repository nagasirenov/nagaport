#!/usr/bin/env python3
import argparse,subprocess,tempfile,json,hashlib,zipfile,collections,re
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--java-home',default='/workspace/shared/nagaport-toolchain/java8');p.add_argument('--classpath-file',default='/tmp/nagaport-ae2-native-classpath.txt');p.add_argument('--forge1614',default='/tmp/nagaport-forge1614-userdev.jar');p.add_argument('--verify-attribution',action='store_true');a=p.parse_args()
here=Path(__file__).resolve().parent;root=here.parents[2];java=a.java_home+'/bin/java';javac=a.java_home+'/bin/javac';cp=Path(a.classpath_file).read_text().strip();specs=[('net.minecraftforge.event.entity.EntityEvent','entity','getEntity','net.minecraft.entity.Entity'),('net.minecraftforge.event.entity.living.LivingEvent','entityLiving','getEntityLiving','net.minecraft.entity.EntityLivingBase'),('net.minecraftforge.event.world.WorldEvent','world','getWorld','net.minecraft.world.World')]
def run(cmd,**kw):return subprocess.run(list(map(str,cmd)),check=True,text=True,**kw)
run(['python3',here/'check_source.py'])
with tempfile.TemporaryDirectory(prefix='event-proof-') as td:
 t=Path(td);run([javac,'-cp',cp+':'+a.java_home+'/lib/tools.jar','-d',t,here/'NativeEventTest.java',here/'EventAccessorScanner.java'])
 # Extract an unchanged complete actual handler method, not a handcrafted facsimile.
 production=(root/'src/main/java/com/hbm/handler/radiation/ChunkRadiationHandlerSimple.java').read_text()
 method=re.search(r'public void receiveWorldUnload\(WorldEvent.Unload event\) \{.*?\n\t\}',production,re.S).group()
 (t/'ExtractedHandler.java').write_text('import java.util.*; import net.minecraft.world.World; import net.minecraftforge.event.world.WorldEvent; public class ExtractedHandler { public Map<World,Object> perWorld=new IdentityHashMap<>(); '+method+' }')
 (t/'ExtractedHandlerTest.java').write_text('''import java.lang.reflect.*; import net.minecraft.world.*; import net.minecraftforge.event.world.WorldEvent; import sun.misc.Unsafe;
public class ExtractedHandlerTest { public static void main(String[] args)throws Exception {net.minecraft.init.Bootstrap.register();Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);Unsafe u=(Unsafe)f.get(null);World s=(World)u.allocateInstance(WorldServer.class),other=(World)u.allocateInstance(WorldServer.class),client=(World)u.allocateInstance(WorldServer.class);Field remote=World.class.getDeclaredField("isRemote");remote.setAccessible(true);remote.setBoolean(client,true);ExtractedHandler h=new ExtractedHandler();Object value=new Object();h.perWorld.put(s,value);h.perWorld.put(other,value);h.perWorld.put(client,value);h.receiveWorldUnload(new WorldEvent.Unload(client));if(h.perWorld.size()!=3)throw new AssertionError("client no removal");h.receiveWorldUnload(new WorldEvent.Unload(s));if(h.perWorld.containsKey(s)||h.perWorld.get(other)!=value||h.perWorld.get(client)!=value)throw new AssertionError("exact server identity removed");h.receiveWorldUnload(new WorldEvent.Unload(s));if(h.perWorld.size()!=2)throw new AssertionError("repeat harmless");try{h.receiveWorldUnload(new WorldEvent.Unload(null));throw new AssertionError();}catch(NullPointerException e){}try{h.receiveWorldUnload(null);throw new AssertionError();}catch(NullPointerException e){}System.out.println("PASS 5 extracted complete production unload handler checks; not whole radiation handler or event bus lifecycle");System.exit(0);}}''')
 run([javac,'-cp',cp,'-d',t,t/'ExtractedHandler.java',t/'ExtractedHandlerTest.java'])
 run([java,'-cp',str(t)+':'+cp,'ExtractedHandlerTest'])
 run([java,'-cp',str(t)+':'+cp,'NativeEventTest',a.classpath_file])
 # Authentic original source and constructor bytecode; no recreated platform API.
 with zipfile.ZipFile(a.forge1614) as z:
  (t/'old.jar').write_bytes(z.read('binaries.jar'))
  for owner,field,getter,typ in specs:
   source=z.read('src/main/java/'+owner.replace('.','/')+'.java').decode()
   assert 'public final '+typ.split('.')[-1]+' '+field+';' in source
   assert re.search(r'(?:this\.)?'+field+r'\s*=\s*\w+;',source)
   dump=run([a.java_home+'/bin/javap','-p','-c','-classpath',t/'old.jar',owner],capture_output=True).stdout
   assert 'public final ' in dump and ' '+field+';' in dump and 'putfield' in dump and getter+'(' not in dump
   (here/('original-'+owner.split('.')[-1]+'.javap.txt')).write_text(dump)
 print('PASS 3 original Forge1614 immutable source fields and authentic constructor bytecode comparisons; userdev SHA256',hashlib.sha256(Path(a.forge1614).read_bytes()).hexdigest())
 # Native compiler controls independently per case to avoid cross-case override reachability.
 tests=0
 for owner,field,getter,typ in specs:
  cases=[('read','Object f('+owner+' e){ return e.'+field+'; }',1),('write','void f('+owner+' e,'+typ+' x){ e.'+field+'=x; }',0),('parenthesized_write','void f('+owner+' e,'+typ+' x){(e.'+field+')=x;}',0),('parenthesized_unary','void f('+owner+' e){(e.'+field+')++;}',0),('compound','void f('+owner+' e){e.'+field+'+=null;}',0),('shadow','static class S extends '+owner+' {public Object '+field+';S(){super(null);}} Object f(S e){return e.'+field+';}',0),('override','static class S extends '+owner+' {S(){super(null);}public '+typ+' '+getter+'(){return null;}} Object f(S e){return e.'+field+';}',0),('reachable_override','static class S extends '+owner+' {S(){super(null);}public '+typ+' '+getter+'(){return null;}} Object f('+owner+' e){return e.'+field+';}',0),('unknown','Object f(Unknown e){return e.'+field+';}',0),('unrelated','static class S {Object '+field+';} Object f(S e){return e.'+field+';}',0),('evaluate',''+owner+' e(){return null;} Object f(){return e().'+field+';}',1)]
  for name,body,count in cases:
   file=t/'Fixture.java';file.write_text('class Fixture {'+body+'}');(t/'sources').write_text(str(file)+'\n')
   result=run([java,'-cp',str(t)+':'+a.java_home+'/lib/tools.jar','EventAccessorScanner',t/'sources',a.classpath_file,*specs[specs.index((owner,field,getter,typ))]],capture_output=True)
   assert len(result.stdout.splitlines())==count,(owner,name,result.stdout,result.stderr);tests+=1
 print('PASS',tests,'real javac read/write/shadow/reachable override/unresolved/evaluation fixtures')
 if a.verify_attribution:
  (t/'sources').write_text('\n'.join(map(str,sorted((root/'src/main/java').rglob('*.java'))))+'\n')
  manifest=json.loads((here/'source-manifest.json').read_text());expected=collections.Counter();actual=collections.Counter();unrelated=0
  for file in manifest['files']:
   shift=0
   for e in file['edits']:
    start=e['start']+shift;expected[(file['path'],start,e['new'],e['receiver'])]+=1;shift+=len(e['new'])-(e['end']-e['start'])
  selected={(key[0],key[1]) for key in expected}
  for spec in specs:
   result=run([java,'-Xmx3g','-cp',str(t)+':'+a.java_home+'/lib/tools.jar','EventAccessorScanner',t/'sources',a.classpath_file,*spec,'post'],capture_output=True)
   print(result.stderr.strip())
   for row in result.stdout.splitlines():
    path,start,end,new,receiver=row.split('\t');relative=str(Path(path).relative_to(root));start=int(start)
    if (relative,start) in selected:actual[(relative,start,new,receiver)]+=1
    else:unrelated+=1
  assert actual==expected,(actual-expected,expected-actual)
  print('PASS complete native declaring-method re-attribution:',sum(actual.values()),'exact token positions;',unrelated,'pre-existing unrelated getters excluded')
