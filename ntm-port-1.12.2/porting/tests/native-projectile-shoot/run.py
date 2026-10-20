#!/usr/bin/env python3
"""Source-extracted callback boundary against genuine installed 1.12 Entity/IProjectile."""
from pathlib import Path
import argparse,collections,hashlib,importlib.util,io,json,os,re,subprocess,tempfile,zipfile
HOME=Path(__file__).resolve().parent;ROOT=HOME.parents[3]
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,default=Path('/workspace/shared/nagaport-toolchain/java8'));p.add_argument('--classpath-file',type=Path,default=Path('/workspace/shared/nagaport-toolchain/native-classpath.txt'));p.add_argument('--legacy-client',type=Path,default=Path('/tmp/nagaport-legacy-client.jar'));p.add_argument('--forge-userdev',type=Path,default=Path('/tmp/nagaport-forge1614-userdev.jar'));p.add_argument('--legacy-srg',type=Path,default=Path('/tmp/nagaport1710-srg.zip'));p.add_argument('--record-slices',action='store_true');p.add_argument('--attribute',action='store_true');p.add_argument('--composed-source',action='store_true');a=p.parse_args()
spec=importlib.util.spec_from_file_location('guard',HOME/'check-source.py');guard=importlib.util.module_from_spec(spec);spec.loader.exec_module(guard);m=guard.DATA
guard.main(composed=a.composed_source)
cp=a.classpath_file.read_text().strip();java=str(a.java_home/'bin/java');javac=str(a.java_home/'bin/javac');javap=str(a.java_home/'bin/javap');tools=str(a.java_home/'lib/tools.jar')
assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
assert hashlib.sha256(a.forge_userdev.read_bytes()).hexdigest()=='6ed364c0defdb481a41ef2d99c02d382f16fc42a6ea0ea1c2b849848d8466e91'
srg=zipfile.ZipFile(a.legacy_srg).read('joined.srg').decode();assert 'MD: zh/c (DDDFF)V net/minecraft/entity/IProjectile/func_70186_c (DDDFF)V' in srg;assert 'MD: zk/c (DDDFF)V net/minecraft/entity/projectile/EntityThrowable/func_70186_c (DDDFF)V' in srg
z=zipfile.ZipFile(a.forge_userdev);patchproof={}
for pkg in ('forgepatches.zip','fmlpatches.zip'):
 data=z.read(pkg);q=zipfile.ZipFile(io.BytesIO(data));assert not any('IProjectile' in n or n.endswith('EntityThrowable.java.patch') or 'func_70186_c' in q.read(n).decode() for n in q.namelist());patchproof[pkg]={'sha256':hashlib.sha256(data).hexdigest(),'entries':len(q.namelist()),'IProjectile_or_EntityThrowable_patch':False,'func_70186_c_patch_reference':False}
print('PASS original zh.c / func_70186_c DDDFF mapping; actual Forge1614 patch archives do not modify callback/interface/throwable')
def bytecode(cp,cls):return subprocess.check_output([javap,'-c','-p','-classpath',cp,cls],text=True)
def block(s,name):i=s.index(name);return s[i:s.find('\n\n',i) if '\n\n' in s[i:] else len(s)]
old=block(bytecode(str(a.legacy_client),'zk'),'public void c(double');native=block(bytecode(cp,'net.minecraft.entity.projectile.EntityThrowable'),'public void shoot(double');iface=bytecode(cp,'net.minecraft.entity.IProjectile');oldiface=bytecode(str(a.legacy_client),'zh')
assert 'public abstract void shoot(double, double, double, float, float);' in iface and 'public abstract void c(double, double, double, float, float);' in oldiface
assert 'java/lang/Math.atan2:(DD)D' in old and 'double 3.1415927410125732d' in old
assert 'net/minecraft/util/math/MathHelper.atan2:(DD)D' in native and 'double 57.29577951308232d' in native
oldmath=block(bytecode(str(a.legacy_client),'qh'),'public static final float a(double);');nativemath=block(bytecode(cp,'net.minecraft.util.math.MathHelper'),'public static float sqrt(double);')
for s in (oldmath,nativemath):assert 'java/lang/Math.sqrt:(D)D' in s and 'd2f' in s and 'freturn' in s
oldconsumer=bytecode(str(a.legacy_client),'kk');newconsumer=bytecode(cp,'net.minecraft.dispenser.BehaviorProjectileDispense');assert 'InterfaceMethod zh.c:(DDDFF)V' in oldconsumer and 'InterfaceMethod net/minecraft/entity/IProjectile.shoot:(DDDFF)V' in newconsumer
(HOME/'engine-contract.javap.txt').write_text('\n\n'.join([oldiface,iface,old,native,oldmath,nativemath,oldconsumer,newconsumer]))
(HOME/'forge1614-mapping-evidence.json').write_text(json.dumps({'legacy_client_sha256':hashlib.sha256(a.legacy_client.read_bytes()).hexdigest(),'forge1614_userdev_sha256':hashlib.sha256(a.forge_userdev.read_bytes()).hexdigest(),'patches':patchproof,'interface_srg_line':'MD: zh/c (DDDFF)V net/minecraft/entity/IProjectile/func_70186_c (DDDFF)V','deferred_SiegeLaser':'Owned EntitySiegeLaser inherits engine EntityThrowable. Motion/RNG bytecode corresponds, but old Math.atan2 / float pi differs from new MathHelper.atan2 / double degree factor. Its caller remains unchanged.'},indent=2)+'\n')
print('PASS actual native IProjectile and dispenser invokeinterface; sqrt helper contract; SiegeLaser native rotation delta explicitly deferred')
slices={};classes=m['hierarchy'];byname={c['name']:c for c in classes}
def short(n):return n.replace('com.hbm.entity.grenade.','').replace('com.hbm.entity.projectile.','').replace('com.hbm.entity.missile.','').replace('.','_')
def keep(n,k,s):slices[n+'.'+k]={'source':s,'sha256':hashlib.sha256(s.encode()).hexdigest()};return s
def original(n,method):s=guard.baseline(byname[n]['path']).decode();return guard.method(s,method)
def current(n,method):s=(ROOT/byname[n]['path']).read_bytes().decode();return guard.method(s,method)
roots={n for n in byname if n.rsplit('.',1)[-1] in m['roots']};helpers={n for n in byname if re.search(r'protected double headingForceMult\(',guard.baseline(byname[n]['path']).decode())}
# Whole files for nested classes may contain a helper in another class. The finite helper owners are explicitly attributed.
helpers={n for n in helpers if n.rsplit('.',1)[-1] in ['EntityThrowableNT','EntityBulletBaseNT','EntityBulletBaseMK4']};assert len(helpers)==3
parts=['import java.util.*; import net.minecraft.entity.*; import net.minecraft.world.World; import net.minecraft.nbt.NBTTagCompound; import net.minecraft.util.math.MathHelper;','public final class Models {',
'''public static class Boundary extends Entity {
 public int ticksInGround; public Boundary(){super((World)null);} public void random(Random r){this.rand=r;}
 protected void entityInit(){} protected void readEntityFromNBT(NBTTagCompound n){throw new AssertionError("NBT not modeled");} protected void writeEntityToNBT(NBTTagCompound n){throw new AssertionError("NBT not modeled");}
}''']
for prefix in ('','Old_'):
 for c in classes:
  n=c['name'];name=prefix+short(n);parent='Boundary' if n in roots else prefix+short(c['parent']);body=[]
  if n in roots:
   old=keep(n,'baseline-callback',original(n,'setThrowableHeading'));new=keep(n,'native-callback',current(n,'shoot'));assert new==old.replace('setThrowableHeading','shoot',1)
   body.append(old if prefix else new)
  if n in helpers:
   old=keep(n,'headingForceMult',original(n,'headingForceMult'));assert old==current(n,'headingForceMult');body.append(old)
  parts.append('public static class '+name+' extends '+parent+(' implements IProjectile' if n in roots and not prefix else '')+' {\n'+'\n'.join(body)+'\n}')
owner={}
for c in classes:
 n=c['name'];o=n
 while o not in roots:o=byname[o]['parent']
 owner[n]=o
mult={}
for c in classes:
 n=c['name'];o=n
 while o not in helpers and o not in roots:o=byname[o]['parent']
 mult[n]='1D' if short(o) in ['EntityBulletBaseNT','EntityBulletBaseMK4'] else '0.0075D'
parts += ['public static final Class<?>[] CLASSES={'+','.join(short(c['name'])+'.class' for c in classes)+'};','public static final Class<?>[] OLD_CLASSES={'+','.join('Old_'+short(c['name'])+'.class' for c in classes)+'};','public static final Class<?>[] OWNERS={'+','.join(short(owner[c['name']])+'.class' for c in classes)+'};','public static final double[] MULTIPLIERS={'+','.join(mult[c['name']] for c in classes)+'};','}'];source='\n'.join(parts)+'\n'
if a.record_slices:(HOME/'slices.json').write_text(json.dumps(slices,indent=2)+'\n')
assert slices==json.loads((HOME/'slices.json').read_text()),'Exact complete executed methods changed'
with tempfile.TemporaryDirectory(prefix='projectile-shoot-native-') as td:
 d=Path(td);models=d/'Models.java';models.write_text(source)
 def compile_run(text):
  models.write_text(text);r=subprocess.run([javac,'-source','8','-target','8','-cp',cp,'-d',str(d),str(models),str(HOME/'Probe.java')],capture_output=True,text=True);assert r.returncode==0,r.stderr
  return subprocess.run([java,'-cp',str(d)+os.pathsep+cp,'Probe'],capture_output=True,text=True,cwd=d)
 r=compile_run(source);assert r.returncode==0,r.stdout+r.stderr;print(r.stdout.strip())
 # Reject wrong Gaussian/sign/distribution, owner-local override changes, state reset changes, and duplicate virtual dispatch.
 mutants=[('signed_rng_removed',source.replace('(this.rand.nextBoolean() ? -1 : 1)','1',1)),('wrong_throwable_scale',source.replace('return 0.0075D;','return 0.007499999832361937D;',1)),('lost_custom_override',source.replace('return 1D;','return 0.0075D;',1)),('wrong_rotation',source.replace('Math.atan2(p_70186_1_, p_70186_5_)','Math.atan2(p_70186_5_, p_70186_1_)',1)),('lost_ground_reset',source.replace('this.ticksInGround = 0;','this.ticksInGround = 1;',1)),('extra_rng',source.replace('float throwLen = MathHelper.sqrt','this.rand.nextGaussian(); float throwLen = MathHelper.sqrt',1))]
 for name,text in mutants:
  assert text!=source,name;r=compile_run(text);assert r.returncode!=0 and 'AssertionError' in r.stderr,(name,r.stdout,r.stderr);print('PASS rejected runtime mutant: '+name)
 # The seven exact legacy methods do not implement the real native interface. No fake Minecraft classes provided.
 bad=['import net.minecraft.entity.IProjectile; import net.minecraft.util.math.MathHelper;','class BadCallbacks { static class Fields {double motionX,motionY,motionZ;float rotationYaw,prevRotationYaw,rotationPitch,prevRotationPitch;int ticksInGround;java.util.Random rand=new java.util.Random();}']
 for n in sorted(roots):bad.append('static class '+short(n)+' extends Fields implements IProjectile {'+original(n,'setThrowableHeading')+(original(n,'headingForceMult') if short(n)=='EntityThrowableNT' else '')+'}')
 bad.append('}');(d/'BadCallbacks.java').write_text('\n'.join(bad));r=subprocess.run([javac,'-cp',cp,'-d',str(d),str(d/'BadCallbacks.java')],capture_output=True,text=True);assert r.returncode!=0;errors=[l for l in r.stderr.splitlines() if 'error:' in l];assert len(errors)==7 and all('does not override abstract method shoot(double,double,double,float,float) in IProjectile' in l for l in errors),r.stderr
 (HOME/'baseline-native-failures.log').write_text(r.stderr.replace(td,'<probe>'));print('PASS seven exact legacy callback declarations fail native-interface compilation for the specific missing shoot descriptor')
 if a.attribute:
  assert not a.composed_source,'Use isolated tree for offset-based attribution'
  scanner=d/'scanner';scanner.mkdir();subprocess.run([javac,'-cp',tools,'-d',str(scanner),str(HOME/'ProjectileScanner.java')],check=True)
  inputs=d/'inputs';inputs.write_text('\n'.join(str(x) for x in sorted((ROOT/'ntm-port-1.12.2/src/main/java').rglob('*.java')))+'\n')
  r=subprocess.run([java,'-Xmx3g','-cp',str(scanner)+os.pathsep+tools,'ProjectileScanner',str(inputs),str(a.classpath_file)],capture_output=True,text=True,check=True);actual=r.stdout.replace(str(ROOT)+'/','');assert actual==(HOME/'after-attribution.jsonl').read_text(),'Full source after attribution drift';print('PASS repeat full 4,068-unit typed census matches frozen final closure exactly')
print('PASS finite callback boundary. Complete NTM classes, native constructors, game launch, flight/collision/payloads and save/restart remain unverified.')
