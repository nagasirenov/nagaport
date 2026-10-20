#!/usr/bin/env python3
import argparse,os,subprocess,tempfile,json,hashlib,zipfile
from pathlib import Path
H=Path(__file__).resolve().parent;R=H.parents[2];OLD=H.parent/'native-entity-spawn';XP=H.parent/'native-xp-spawn'
a=argparse.ArgumentParser();a.add_argument('--java-home',type=Path,required=True);a.add_argument('--classpath-file',type=Path,required=True);a.add_argument('--legacy-client',type=Path,required=True);a.add_argument('--attribute',action='store_true');a=a.parse_args();cp=a.classpath_file.read_text().strip();java=a.java_home/'bin/java';javac=a.java_home/'bin/javac';tools=a.java_home/'lib/tools.jar';m=json.loads((H/'manifest.json').read_text())
xp_hashes=json.loads((H/'xp-class-hashes.json').read_text())
with zipfile.ZipFile(a.legacy_client)as z:assert hashlib.sha256(z.read('sq.class')).hexdigest()==xp_hashes['legacy_1710_sq']
found=False
for path in cp.split(os.pathsep):
 if not path.endswith('.jar')or not Path(path).exists():continue
 with zipfile.ZipFile(path)as z:
  if 'net/minecraft/entity/item/EntityXPOrb.class' in z.namelist():assert hashlib.sha256(z.read('net/minecraft/entity/item/EntityXPOrb.class')).hexdigest()==xp_hashes['native_2768_EntityXPOrb'];found=True
assert found
print('PASS pinned original/native XP constructor class hashes')
def inverse(raw,info):
 assert hashlib.sha256(raw).hexdigest()==info['after'];s=raw.decode();delta=0;rev=[]
 for e in sorted(info['edits'],key=lambda x:x['start']):
  start=e['start']+delta;end=start+len(e['new']);assert s[start:end]==e['new'];rev.append((start,end,e['old']));delta+=len(e['new'])-len(e['old'])
 for start,end,text in reversed(rev):s=s[:start]+text+s[end:]
 assert hashlib.sha256(s.encode()).hexdigest()==info['before'];return s.encode()
baseline={rel:inverse((R/rel).read_bytes(),info)for rel,info in m['files'].items()}
for rel,info in json.loads((OLD/'manifest.json').read_text())['files'].items():inverse(baseline.get(rel,(R/rel).read_bytes()),info)
assert len(m['files'])==20 and sum(len(v['edits'])for v in m['files'].values())==24
print('PASS exact inverse: 24 extra call tokens -> immutable 43-site baseline -> exact CP57 bytes, no prior spawn changes lost')
def run(cmd,**kw):return subprocess.run(list(map(str,cmd)),check=True,**kw)
def scope(rel,index=0):
 info=m['files'][rel];e=info['edits'][index];delta=sum(len(v['new'])-len(v['old'])for v in info['edits']if v['start']<e['scope_start']);s=(R/rel).read_bytes().decode();result=s[e['scope_start']+delta:e['call_end']+delta+len(e['new'])-len(e['old'])];assert result==e['scope'].replace(e['old'],e['new']);return result+';'
with tempfile.TemporaryDirectory(prefix='local-spawn-')as td:
 t=Path(td);out=t/'classes';out.mkdir();runtime=str(out)+os.pathsep+cp
 imp='import net.minecraft.world.World;import net.minecraft.entity.*;import net.minecraft.entity.item.*;import net.minecraft.item.*;import java.util.*;'
 producers=t/'Producers.java';producers.write_text(imp+'class LocalProducer extends EntityItem {LocalProducer(World w){super(w);}void spawn(ItemStack stack){'+scope('src/main/java/com/hbm/entity/item/EntityMovingPackage.java')+'}}\nclass TileProducer extends net.minecraft.tileentity.TileEntity {void spawn(ItemStack scrap){'+scope('src/main/java/com/hbm/tileentity/machine/TileEntityMachineStrandCaster.java')+'}}\nclass DebrisProducer {static void spawn(World world,double x,double y,double z,double motionX,double motionY,double motionZ,double deviation,List<ItemStack> debris,int i,Random rand){'+scope('src/main/java/com/hbm/explosion/ExplosionLarge.java')+'}}\nclass XPProducer {static void spawn(World world,EntityLivingBase living){'+scope('src/main/java/com/hbm/handler/ability/IWeaponAbility.java')+'}}')
 call=next(l.strip()for l in (R/'src/main/java/com/hbm/blocks/machine/BlockMassStorage.java').read_text().splitlines()if 'world.spawnEntity(new EntityItem(' in l)
 production=t/'ProductionSpawn.java';production.write_text(imp+'class ProductionSpawn {static void spawn(World world,double x,double y,double z,ItemStack drop){'+call+'}}')
 run([javac,'-cp',cp+os.pathsep+str(tools),'-d',out,producers,production,H/'LocalSpawnScanner.java',XP/'XPScanner.java',H/'LocalSpawnTest.java',H/'OriginalXPSpawnTest.java',OLD/'SpawnNativeTest.java',OLD/'OriginalSpawnTest.java',H.parent/'ForgeEventNativeLauncher.java'])
 for test in ['SpawnNativeTest','LocalSpawnTest']:run([java,'-cp',runtime,'ForgeEventNativeLauncher',test],cwd=t)
 for test in ['OriginalSpawnTest','OriginalXPSpawnTest']:run([java,'-cp',runtime,test,a.legacy_client],cwd=t)
 def scan(name,paths,native=False):
  inputs=t/'inputs';inputs.write_text('\n'.join(map(str,paths))+'\n');cmd=[java,'-Xmx1400m','-cp',str(out)+os.pathsep+str(tools),name,inputs,a.classpath_file]
  if native:cmd.append('native')
  return run(cmd,stdout=subprocess.PIPE,universal_newlines=True).stdout.splitlines()
 controls=t/'Controls.java';controls.write_bytes((H/'Controls.java.in').read_bytes());rows=scan('LocalSpawnScanner',[controls]);assert len(rows)==4,rows;assert any(r.endswith('\th.nested.world')for r in rows)
 controls.write_text(controls.read_text().replace('spawnEntityInWorld','spawnEntity'));native_controls=scan('LocalSpawnScanner',[controls],True);assert len(native_controls)==4
 for row in native_controls:
  _,_,_,rs,re,*rest=row.split('\t');text=controls.read_text().encode('utf-16-le')[int(rs)*2:int(re)*2].decode('utf-16-le');assert text==rest[-1]
 run([javac,'-cp',cp,'-d',out,controls])
 print('PASS four typed fresh-local positives including complete nested receiver; 11 dead/UUID/alias/reassignment/escape/callback/reuse/loop/lambda/dead-field candidate invocations rejected')
 if a.attribute:
  paths=sorted((R/'src/main/java').rglob('*.java'));rows=scan('LocalSpawnScanner',paths,True)
  selected={};expected=set()
  for rel,info in m['files'].items():
   delta=0;s=(R/rel).read_bytes().decode()
   for e in sorted(info['edits'],key=lambda x:x['start']):
    conv=lambda n:len(s[:n+delta].encode('utf-16-le'))//2
    if e['kind']=='fresh_local_item':selected[(rel,conv(e['start']))]=(conv(e['receiver_start']),conv(e['receiver_end']),e['receiver']);expected.add((rel,conv(e['start'])))
    delta+=len(e['new'])-len(e['old'])
  actual={}
  for row in rows:
   f,start,end,rs,re,ds,ce,var,receiver=row.split('\t');rel=str(Path(f).relative_to(R));actual[(rel,int(start))]=(int(rs),int(re),receiver)
  for k in expected:assert actual[k]==selected[k],(k,actual.get(k),selected[k])
  # Compare all other destination-attributed candidates to independently attributed pre-expansion bytes.
  originals=[]
  for p in paths:
   rel=str(p.relative_to(R))
   if rel in baseline:q=t/'baseline'/rel;q.parent.mkdir(parents=True,exist_ok=True);q.write_bytes(baseline[rel]);originals.append(q)
   else:originals.append(p)
  pre=scan('LocalSpawnScanner',originals,True);prior={}
  for row in pre:
   f,start,end,rs,re,ds,ce,var,receiver=row.split('\t');p=Path(f);rel=str(p.relative_to(t/'baseline'))if p.is_relative_to(t/'baseline')else str(p.relative_to(R));old=p.read_bytes().decode();current=(R/rel).read_bytes().decode()
   def shifted(n):
    n=len(old.encode('utf-16-le')[:int(n)*2].decode('utf-16-le'));n+=sum(len(e['new'])-len(e['old'])for e in m['files'].get(rel,{}).get('edits',[])if e['start']<n);return len(current[:n].encode('utf-16-le'))//2
   prior[(rel,shifted(start))]=(shifted(rs),shifted(re),receiver)
  assert actual==prior|selected,(actual.keys()^(prior.keys()|selected.keys()))
  xp_rows=scan('XPScanner',[R/'src/main/java/com/hbm/handler/ability/IWeaponAbility.java'],True);assert len(xp_rows)==1
  xp=xp_rows[0].split('\t');entry=m['files']['src/main/java/com/hbm/handler/ability/IWeaponAbility.java']['edits'][0];assert [int(xp[i])for i in (1,5,6)]==[entry['start'],entry['receiver_start'],entry['receiver_end']] and xp[-1]==entry['receiver']
  print('PASS all 23 destination local calls resolve to native World and identical COMPLETE receiver source ranges; native XP constructor/call attributed; preexisting destination candidates retained')
