#!/usr/bin/env python3
"""Whole generic grenade owners against declared bounded ancestors and native item/data/NBT APIs."""
from pathlib import Path
import os,re,json,hashlib,subprocess,tempfile,sys
folder=Path(__file__).resolve().parent;root=folder.parents[3];src=root/'ntm-port-1.12.2/src/main/java';manifest=json.loads((folder/'source-manifest.json').read_text());slices={}
def extract(s,pat):
 m=re.search(pat,s);assert m,pat
 i=s.index('{',m.end()-1);end=i+1;depth=1
 while depth:depth+=(s[end]=='{')-(s[end]=='}');end+=1
 return s[m.start():end]
def keep(n,label,s):slices[n+'.'+label]={'sha256':hashlib.sha256(s.encode()).hexdigest(),'source':s};return s
imports='package com.hbm.entity.grenade;import net.minecraft.entity.*;import net.minecraft.entity.projectile.EntityThrowable;import net.minecraft.world.World;import net.minecraft.nbt.NBTTagCompound;import net.minecraft.util.math.RayTraceResult;'
base=(src/'com/hbm/entity/grenade/EntityGrenadeBase.java').read_text();bouncy=(src/'com/hbm/entity/grenade/EntityGrenadeBouncyBase.java').read_text();units={}
parts=[keep('EntityGrenadeBase','constructor-'+str(i),extract(base,r'public EntityGrenadeBase\('+args+r'\)\s*\{')) for i,args in enumerate([r'World p_i1773_1_',r'World p_i1774_1_, EntityLivingBase p_i1774_2_',r'World p_i1775_1_, double p_i1775_2_, double p_i1775_4_, double p_i1775_6_'])]
parts+=['protected void onImpact(RayTraceResult hit){throw new AssertionError("Impact mechanics excluded");}','public abstract void explode();']
units['com/hbm/entity/grenade/EntityGrenadeBase.java']=imports+'public abstract class EntityGrenadeBase extends EntityThrowable {'+'\n'.join(parts)+'}'
parts=[keep('EntityGrenadeBouncyBase','world-constructor',extract(bouncy,r'public EntityGrenadeBouncyBase\(World world\)\s*\{'))]
for label,text in [('timer','protected int timer = 0;'),('thrower','protected EntityLivingBase thrower;'),('throwerName','protected String throwerName;')]:assert text in bouncy;parts.append(keep('EntityGrenadeBouncyBase',label,text))
for method,typ in [('entityInit','void'),('readEntityFromNBT','void'),('writeEntityToNBT','void'),('getThrower','EntityLivingBase')]:parts.append(keep('EntityGrenadeBouncyBase',method,extract(bouncy,r'(?:public|protected) '+typ+' '+method+r'\([^)]*\)\s*\{')))
parts+=['public EntityGrenadeBouncyBase(World w,EntityLivingBase e){super(w);throw new AssertionError("Throwing constructor excluded");}','public EntityGrenadeBouncyBase(World w,double x,double y,double z){super(w);throw new AssertionError("Position constructor excluded");}','public abstract void explode(); protected abstract int getMaxTimer(); protected abstract double getBounceMod();']
units['com/hbm/entity/grenade/EntityGrenadeBouncyBase.java']=imports+'public abstract class EntityGrenadeBouncyBase extends Entity {'+'\n'.join(parts)+'}'
# No NTM explosion payload simulation: fail if accidental test invocation expands scope.
units['com/hbm/items/weapon/ItemGenericGrenade.java']='package com.hbm.items.weapon;public class ItemGenericGrenade extends net.minecraft.item.Item {public void explode(net.minecraft.entity.Entity e,net.minecraft.entity.EntityLivingBase p,net.minecraft.world.World w,double x,double y,double z){throw new AssertionError("Payload excluded");}public int getMaxTimer(){throw new AssertionError("Fuse mechanics excluded");}public double getBounceMod(){throw new AssertionError("Bounce mechanics excluded");}}'
units['com/hbm/items/ModItems.java']='package com.hbm.items;public class ModItems {public static net.minecraft.item.Item grenade_kyiv;}'
expected=folder/'slices.json'
if '--record-slices' in sys.argv:expected.write_text(json.dumps(slices,indent=2)+'\n')
assert slices==json.loads(expected.read_text())
for e in manifest['entries']:assert hashlib.sha256((root/e['path']).read_bytes()).hexdigest()==e['after_sha256']
cpfile=Path(os.environ.get('NAGAPORT_NATIVE_CP_FILE','/tmp/nagaport-ae2-native-classpath.txt'));assert cpfile.is_file();cp=cpfile.read_text().strip();java=Path(os.environ.get('JAVA_HOME','/workspace/shared/nagaport-toolchain/java8'))/'bin'
with tempfile.TemporaryDirectory(prefix='native-generic-grenades-') as d:
 t=Path(d);files=[]
 for name,s in units.items():p=t/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(s);files.append(str(p))
 files += [str(root/e['path']) for e in manifest['entries']]+[str(src/'com/hbm/entity/grenade/IGenericGrenade.java'),str(folder/'Probe.java')]
 subprocess.run([str(java/'javac'),'-encoding','UTF-8','-cp',cp,'-d',d]+files,check=True)
 for order in ['impact-first','bouncy-first']:subprocess.run([str(java/'java'),'-Djava.awt.headless=true','-cp',d+os.pathsep+cp,'Probe',order],cwd=d,check=True)
print('PASS complete ImpactGeneric/BouncyGeneric + IGenericGrenade compiled unchanged; '+str(len(slices))+' ancestor slices; explicit NTM item/ancestor fixtures, no flight/payload claim')
