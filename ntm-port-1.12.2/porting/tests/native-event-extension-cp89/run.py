#!/usr/bin/env python3
"""Offline native payload proof plus strict javac controls; no full build or downloads."""
from pathlib import Path
import argparse,base64,collections,hashlib,json,os,re,subprocess,tempfile,zipfile
from check_source import verify,M
p=argparse.ArgumentParser();p.add_argument('--module',type=Path);p.add_argument('--java-home',type=Path,default=Path('/workspace/shared/nagaport-toolchain/java8'));p.add_argument('--classpath-file',type=Path,default=Path('/workspace/shared/nagaport-toolchain/native-classpath.txt'));p.add_argument('--forge1614',type=Path,default=Path('/tmp/nagaport-forge1614-userdev.jar'));p.add_argument('--verify-attribution',action='store_true');a=p.parse_args()
H=Path(__file__).resolve().parent;R=a.module or H.parents[2];J=a.java_home;CP=a.classpath_file.read_text().strip();verify(R)
reads=[e for f in M['phases'][0]['files'] for e in f['edits']];writes=[e for f in M['phases'][1]['files'] for e in f['edits']]
def run(cmd,**kw):return subprocess.run(list(map(str,cmd)),check=True,text=True,**kw)
with tempfile.TemporaryDirectory(prefix='native-event-cp89-') as td:
 t=Path(td)
 selected='public class SelectedReads {\nstatic final String[] OWNERS={'+','.join('"'+e['owner']+'"' for e in reads)+'};\nstatic final String[] FIELDS={'+','.join('"'+e['old']+'"' for e in reads)+'};\nstatic final Class<?>[] RECEIVERS={'+','.join(e['receiver']+'.class' for e in reads)+'};\n'+'\n'.join('static Object read%d(%s event){return event.%s;}'%(i,e['receiver'],e['new']) for i,e in enumerate(reads))+'\n}\n'
 (t/'SelectedReads.java').write_text(selected)
 # Exact production assignment expressions, with only their external inputs supplied as fixtures.
 astro=(R/'src/main/java/com/hbm/util/AstronomyUtil.java').read_text();constant=re.search(r'public static final float STANDARD_GRAVITY = ([^;]+);',astro).group(1);assert constant=='1.6F'
 s='import net.minecraft.item.ItemStack;import net.minecraft.init.Items;import net.minecraft.util.math.Vec3d;\npublic class SelectedWrites {static final class AstronomyUtil {static final float STANDARD_GRAVITY='+constant+';}\n'
 for i,e in enumerate(writes):s+='static void write%d(%s event,ItemStack result,float gravity,Vec3d color,float interp,float sootColor,float lastFogDensity){%s;}\n'%(i,e['receiver'],e['new'])
 s+='static void test()throws Exception {\n'
 for i,e in enumerate(writes):
  f=e['field'];r=e['receiver'];getter={'result':'getFilledBucket','distance':'getDistance','density':'getDensity','red':'getRed','green':'getGreen','blue':'getBlue'}[f];expected={'result':'result','density':'lastFogDensity','red':'(float)color.x','green':'(float)color.y','blue':'(float)color.z','distance':'0F'}[f]
  if e['kind']=='multiply':expected='(float)(8F*(gravity/AstronomyUtil.STANDARD_GRAVITY))'
  if 'interp' in e['new']:expected='8F*(1-interp)+sootColor*interp'
  s+='{%s event=(%s)NativeEventExtensionTest.u.allocateInstance(%s.class);ItemStack result=new ItemStack(Items.APPLE);float gravity=3.2F;Vec3d color=new Vec3d(.31,.51,.71);float interp=.25F,sootColor=.9F,lastFogDensity=.37F;\n'%(r,r,r)
  if f!='result':s+='event.%s(8F);\n'%e['setter']
  s+='write%d(event,result,gravity,color,interp,sootColor,lastFogDensity);NativeEventExtensionTest.ok(event.%s()==%s,"exact selected production setter %d");}\n'%(i,getter,expected,i)
 s+='}\n}\n';(t/'SelectedWrites.java').write_text(s)
 run([J/'bin/javac','-cp',CP+':'+str(J/'lib/tools.jar'),'-d',t,H/'NativeEventExtensionTest.java',H/'EventExtensionScanner.java',H/'ForgeEventNativeLauncher.java',H/'BucketHandlerNativeTest.java',R/'src/main/java/com/hbm/handler/BucketHandler.java',t/'SelectedReads.java',t/'SelectedWrites.java'])
 run([J/'bin/java','-cp',str(t)+':'+CP,'NativeEventExtensionTest',H/'rules.tsv',a.classpath_file],cwd=t)
 run([J/'bin/java','-cp',str(t)+':'+CP,'ForgeEventNativeLauncher','BucketHandlerNativeTest'],cwd=t)
 # Every originally inaccessible selected read fails native compilation, then exact mapped calls compile.
 legacy='public class LegacyReads {'+'\n'.join('Object read%d(%s event){return event.%s;}'%(i,e['receiver'],e['old']) for i,e in enumerate(reads))+'}'
 (t/'LegacyReads.java').write_text(legacy);bad=subprocess.run([str(J/'bin/javac'),'-Xmaxerrs','10000','-cp',CP,'-d',str(t),str(t/'LegacyReads.java')],capture_output=True,text=True);assert bad.returncode and bad.stderr.count('has private access')==165,bad.stderr
 print('PASS all 165 old read sites fail actual native API; exact migrated calls compile and execute',flush=True)
 fixture=t/'Fixture.java';sources=t/'sources';sources.write_text(str(fixture)+'\n')
 def scan(body,mode=None):
  fixture.write_text('class Fixture {'+body+'}');cmd=[J/'bin/java','-cp',str(t)+':'+str(J/'lib/tools.jar'),'EventExtensionScanner',sources,a.classpath_file,H/'rules.tsv'];return run(cmd+([mode] if mode else []),capture_output=True).stdout.splitlines()
 P='net.minecraftforge.event.entity.player.PlayerEvent';L='net.minecraftforge.event.entity.living.LivingFallEvent'
 cases=[('read','Object f('+P+' e){return e.entityPlayer;}',1),('write','void f('+P+' e){e.entityPlayer=null;}',0),('compound','void f('+P+' e){e.entityPlayer+=null;}',0),('unary','void f('+P+' e){e.entityPlayer++;}',0),('type receiver','Object f(){return '+P+'.entityPlayer;}',0),('unknown','Object f(Unknown e){return e.entityPlayer;}',0),('unrelated','static class S{Object entityPlayer;} Object f(S e){return e.entityPlayer;}',0),('shadow','static class S extends '+P+'{Object entityPlayer;S(){super(null);}}Object f(S e){return e.entityPlayer;}',0),('override','static class S extends '+P+'{S(){super(null);}public net.minecraft.entity.player.EntityPlayer getEntityPlayer(){return null;}}Object f('+P+' e){return e.entityPlayer;}',0),('receiver once',P+' e(){return null;}Object f(){return e().entityPlayer;}',1),('comment','String x="event.entityPlayer";/* event.entityPlayer */',0),('transparent native override','Object f(net.minecraftforge.event.entity.player.ItemTooltipEvent e){return e.entityPlayer;}',1)]
 for n,b,count in cases:assert len(scan(b))==count,n
 wc=[('simple','void f('+L+' e){e.distance=0;}',1),('multiply','void f('+L+' e){e.distance*=.5D;}',1),('getter RHS','void f('+L+' e){e.distance=e.getDistance()*2F;}',1),('nested expression','float f('+L+' e){return e.distance=2F;}',0),('effectful receiver',L+' e(){return null;}void f(){e().distance=0;}',0),('receiver reassignment','void f('+L+' e){e.distance*=(e=null).getDistance();}',0),('other operator','void f('+L+' e){e.distance+=1;}',0),('wrong RHS','void f('+L+' e){e.distance="bad";}',0),('unknown RHS','void f('+L+' e){e.distance=missing;}',0),('setter override','static class S extends '+L+'{S(){super(null,0,0);}public void setDistance(float v){}}void f('+L+' e){e.distance=0;}',0)]
 for n,b,count in wc:assert len(scan(b,'writes'))==count,n
 print('PASS 22 native javac ownership, shadow, override, write-shape, RHS-type and receiver-evaluation controls',flush=True)
 # Authentic Forge1614 fields, not a recreated old API. Record exact original declarations and bytecode.
 proofs=[]
 with zipfile.ZipFile(a.forge1614) as z:
  (t/'old.jar').write_bytes(z.read('binaries.jar'));names=set(z.namelist());dumps={}
  for line in (H/'rules.tsv').read_text().splitlines():
   owner,field,getter,typ,setter,storage=line.split('\t');path='src/main/java/'+owner.replace('.','/')+'.java'
   while path not in names:path=path.rsplit('/',1)[0]+'.java'
   src=z.read(path).decode();declaration=re.search(r'public\s+(?:final\s+)?[\w<>\[\]]+\s+[^;\n]*\b'+re.escape(field)+r'\b[^;\n]*;',src);assert declaration,(owner,field)
   parts=owner.split('.');first=next(i for i,x in enumerate(parts) if x[0].isupper());binary='.'.join(parts[:first])+'.'+'$'.join(parts[first:])
   if binary not in dumps:dumps[binary]=run([J/'bin/javap','-p','-c','-classpath',t/'old.jar',binary],capture_output=True).stdout
   dump=dumps[binary];assert re.search(r'public (?:final )?[^\n]+ '+re.escape(field)+r';',dump),(owner,field)
   proofs.append(dict(owner=owner,field=field,native_getter=getter,native_setter=setter,original_declaration=declaration.group(),original_bytecode_field=next(l.strip() for l in dump.splitlines() if l.strip().endswith(' '+field+';'))))
  (H/'original-fields-proof.json').write_text(json.dumps(proofs,indent=2)+'\n')
  hooks=z.read('src/main/java/net/minecraftforge/common/ForgeHooks.java').decode();assert 'new AnvilRepairEvent(player, left, right, output)' in hooks
 print('PASS 46 genuine Forge1614 public payload declarations/binary fields; Anvil hook-role mismatch explicitly excluded',flush=True)
 if a.verify_attribution:
  sources.write_text('\n'.join(map(str,sorted((R/'src/main/java').rglob('*.java'))))+'\n')
  result=run([J/'bin/java','-Xmx3g','-cp',str(t)+':'+str(J/'lib/tools.jar'),'EventExtensionScanner',sources,a.classpath_file,H/'rules.tsv','post'],capture_output=True)
  actual={}
  for row in result.stdout.splitlines():
   path,start,end,old,new,receiver,owner=row.split('\t');actual[(str(Path(path).relative_to(R)),int(start))]=(new,receiver,owner)
  expected={};writefiles={f['path']:f for f in M['phases'][1]['files']}
  for f in M['phases'][0]['files']:
   shift=0
   for e in f['edits']:
    pos=e['start']+shift;shift+=len(e['new'])-len(e['old']);delta=0
    for w in writefiles.get(f['path'],{}).get('edits',[]):
     if w['start']<=pos<w['end']:
      assert w['new'].count(e['new'])==1;pos=w['start']+delta+w['new'].index(e['new']);break
     if w['end']<=pos:delta+=len(w['new'])-len(w['old'])
    else:pos+=delta
    expected[(f['path'],pos)]=(e['new'],e['receiver'],e['owner'])
  assert len(expected)==165
  for key,value in expected.items():assert actual.get(key)==value,(key,value,actual.get(key))
  print('PASS 165 exact-position native getter re-attributions; other native getters:',len(actual)-len(expected));print(result.stderr.strip())
  (H/'verified-after.tsv').write_text(result.stdout);(H/'verified-after.log').write_text(result.stderr)
 deps={p:hashlib.sha256(Path(p).read_bytes()).hexdigest() for p in CP.split(os.pathsep) if Path(p).is_file()};deps[str(a.forge1614)]=hashlib.sha256(a.forge1614.read_bytes()).hexdigest();(H/'dependency-sha256.json').write_text(json.dumps(deps,indent=2)+'\n')
print('PASS bounded CP89 native event-extension evidence. Not full-module compilation success.',flush=True)
