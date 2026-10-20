#!/usr/bin/env python3
"""Exact metadata method projections on genuine native parents, never complete owners/game acceptance."""
from pathlib import Path
import re,json,hashlib,subprocess,tempfile,os,sys
folder=Path(__file__).resolve().parent;root=folder.parents[3];manifest=json.loads((folder/'source-manifest.json').read_text());base=manifest['base'];prefix='ntm-port-1.12.2/src/main/java/com/hbm/';slices={};units={}
def source(rel):
 p=root/(prefix+rel)
 return p.read_bytes().decode() if p.exists() else subprocess.check_output(['git','-C',str(root),'show',base+':'+prefix+rel]).decode()
def extract(s,pat):
 m=re.search(pat,s);assert m,pat;i=s.index('{',m.end()-1);j=i+1;d=1
 while d:d+=(s[j]=='{')-(s[j]=='}');j+=1
 return s[m.start():j]
def keep(owner,label,s):slices[owner+'.'+label]={'source':s,'sha256':hashlib.sha256(s.encode()).hexdigest()};return s
def method(s,n):return extract(s,r'(?:public|protected) (?:static )?[\w<>]+ '+n+r'\([^)]*\)\s*\{')
imports='''import net.minecraft.entity.*;import net.minecraft.entity.item.EntityMinecart;import net.minecraft.world.World;import net.minecraft.network.datasync.*;import net.minecraft.nbt.*;import net.minecraft.util.math.Vec3d;import com.hbm.items.tool.ItemModMinecart.EnumCartBase;import com.hbm.entity.cart.EntityMinecartBogie;import com.hbm.entity.mob.*;import com.hbm.entity.mob.siege.SiegeTier;'''
for e in manifest['entries']:
 rel=e['path'][len(prefix):];s=source(rel);name=Path(rel).stem;pkg='com.hbm.'+str(Path(rel).parent).replace('/','.');parent=next(h['parent'] for h in manifest['hierarchy'] if h['class_name']==name);parts=[]
 parts += [keep(name,'key-'+str(i),k) for i,k in enumerate(re.findall(r'private static final DataParameter<\w+> [^;]+;',s))]
 if name=='EntitySiegeTunneler':parts.append('public '+name+'(World world) { super(world); }') # explicit synthetic constructor; original yOffset/AI are out of scope
 else:
  for i,c in enumerate(re.finditer(r'public '+name+r'\([^)]*\)\s*\{',s)):parts.append(keep(name,'constructor-'+str(i),extract(s[c.start():],r'public '+name+r'\([^)]*\)\s*\{')))
 names=['entityInit']
 if name=='EntityMinecartNTM':names+=['setBase','getBase','writeEntityToNBT','readEntityFromNBT']
 if name=='EntityMagnusCartus':names+=['setBogie','getBogieID','writeEntityToNBT','readEntityFromNBT'];parts.append(keep(name,'bogie-field',re.search(r'public EntityMinecartBogie bogie;',s).group()))
 if name=='EntityUFOBase':names+=['setWaypoint','getX','getY','getZ']
 if name.startswith('EntitySiege'):
  names+=['getTier','writeEntityToNBT'];full=method(s,'setTier');statement=next(l.strip() for l in full.splitlines() if '.set(SIEGE_TIER,' in l)
  parts.append('public void setTierMetadataPrefix(SiegeTier tier) { '+keep(name,'setTier-exact-metadata-prefix',statement)+' }')
  fullread=method(s,'readEntityFromNBT');selection=re.search(r'SiegeTier.tiers\[nbt.getInteger\("siegeTier"\)\]',fullread).group()
  parts.append('public static SiegeTier readTierSelection(NBTTagCompound nbt) { return '+keep(name,'read-NBT-tier-selection-expression',selection)+'; }')
  if name=='EntitySiegeUFO':
   for field in ['private int lifetime = 0;','private boolean isRetreating = false;']:assert field in s;parts.append(keep(name,field,field))
   suffix=fullread[fullread.index('\n\t\tthis.lifetime'):fullread.rindex('}')];parts.append('public void readLifetimeSuffix(NBTTagCompound nbt) {'+keep(name,'read-NBT-exact-lifetime-suffix',suffix)+'}')
 if name=='EntitySiegeCraft':names+=['setBeam','getBeam','setLockon','getLockon']
 for n in names:parts.append(keep(name,n,method(s,n)))
 if parent=='EntityMinecart':parts.append('public EntityMinecart.Type getType() { return EntityMinecart.Type.RIDEABLE; }') # fixture native abstract requirement, not production kind migration
 if name in ['EntityMinecartNTM','EntityMagnusCartus']:parts+=['public void saveFixture(NBTTagCompound n) { writeEntityToNBT(n); }','public void loadFixture(NBTTagCompound n) { readEntityFromNBT(n); }']
 units[pkg.replace('.','/')+'/'+name+'.java']='package '+pkg+';'+imports+'\npublic '+('abstract ' if name in ['EntityMinecartNTM','EntityUFOBase'] else '')+'class '+name+' extends '+parent+' {\n'+'\n'.join(parts)+'\n}'
# Every unchanged named descendant is represented by its actual named ancestry, but not full constructors, inventory, AI or methods.
for e in manifest['hierarchy']:
 name=e['class_name'];rel=e['path'][len(prefix):];pkg='com.hbm.'+str(Path(rel).parent).replace('/','.')
 if any(k['declaring_owner']==name for k in manifest['keys']):continue
 s=source(rel);keep(name,'actual-inheritance',re.search(r'public (?:abstract )?class '+name+r' extends \w+',s).group());assert not re.search(r'dataWatcher|getDataWatcher|DataParameter|void entityInit',s)
 units[pkg.replace('.','/')+'/'+name+'.java']='package '+pkg+';'+imports+'public class '+name+' extends '+e['parent']+' { public '+name+'(World w) { super(w); } }'
# Tunneler ancestry: unchanged classes contain no custom metadata registrations.
for name,parent in [('EntityBurrowingBase','EntityCreature'),('EntityBurrowingSwingingBase','EntityBurrowingBase')]:
 s=source('entity/mob/'+name+'.java');assert not re.search(r'dataWatcher|getDataWatcher|DataParameter|void entityInit',s);keep(name,'actual-inheritance',re.search(r'public abstract class '+name+r' extends \w+',s).group())
 units['com/hbm/entity/mob/'+name+'.java']='package com.hbm.entity.mob;'+imports+'public abstract class '+name+' extends '+parent+' { public '+name+'(World w) { super(w); } }'
s=source('entity/cart/EntityMinecartBogie.java');parts=[keep('EntityMinecartBogie','constructor',extract(s,r'public EntityMinecartBogie\(World world\)\s*\{'))]
units['com/hbm/entity/cart/EntityMinecartBogie.java']='package com.hbm.entity.cart;'+imports+'public class EntityMinecartBogie extends EntityMinecart {'+'\n'.join(parts)+' public Type getType() { return Type.RIDEABLE; } }'
s=source('items/tool/ItemModMinecart.java');enum=keep('ItemModMinecart','exact-base-enum',extract(s,r'public static enum EnumCartBase\s*\{'));units['com/hbm/items/tool/ItemModMinecart.java']='package com.hbm.items.tool;public class ItemModMinecart {'+enum+'}'
# Exact SiegeTier primitive constructor/fields, without game item registration or drop policy fixtures.
s=source('entity/mob/siege/SiegeTier.java');parts=[]
for field in [r'public static SiegeTier\[\] tiers = [^;]+;',r'private static int nextID = [^;]+;',r'public static SiegeTier CLAY;',r'public int id;',r'public float health = [^;]+;',r'public String name = [^;]+;']:parts.append(keep('SiegeTier',field,re.search(field,s).group()))
parts.append(keep('SiegeTier','constructor',extract(s,r'public SiegeTier\([^)]*\)\s*\{')));units['com/hbm/entity/mob/siege/SiegeTier.java']='package com.hbm.entity.mob.siege;public class SiegeTier {'+'\n'.join(parts)+'}'
# Actual consumer expressions, not renderer GL.
s=source('render/entity/item/RenderMagnusCartus.java');a=next(l.strip() for l in s.splitlines() if 'EntityMagnusCartus cart =' in l);b=next(l.strip() for l in s.splitlines() if 'Entity e = ' in l);a=keep('RenderMagnusCartus','typed-cast',a);b=keep('RenderMagnusCartus','world-bogie-lookup',b)
s=source('render/entity/mob/RenderSiegeCraft.java');delta=keep('RenderSiegeCraft','lockon-read-expression',re.search(r'ufo.getLockon\(\)',s).group());tier=keep('RenderSiegeCraft','tier-lookup',next(l.strip() for l in s.splitlines() if 'SiegeTier tier =' in l))
units['Consumers.java']='import net.minecraft.entity.Entity;import net.minecraft.util.math.Vec3d;import com.hbm.entity.item.EntityMagnusCartus;import com.hbm.entity.mob.siege.*;class Consumers { static Entity bogie(Entity entity) {'+a+b+'return e;} static Vec3d lockonRead(EntitySiegeCraft ufo) {return '+delta+';} static SiegeTier tier(EntitySiegeCraft entity) {'+tier+'return tier;} }'
if '--record-slices' in sys.argv:(folder/'slices.json').write_text(json.dumps(slices,indent=2)+'\n')
assert slices==json.loads((folder/'slices.json').read_text())
cp=Path(os.environ.get('NAGAPORT_NATIVE_CP_FILE','/workspace/shared/nagaport-toolchain/native-classpath.txt')).read_text().strip();java=Path(os.environ.get('JAVA_HOME','/workspace/shared/nagaport-toolchain/java8'))/'bin'
with tempfile.TemporaryDirectory(prefix='native-cart-siege-') as d:
 t=Path(d);files=[]
 for n,s in units.items():p=t/n;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(s);files.append(str(p))
 files.append(str(folder/'Probe.java'));subprocess.run([str(java/'javac'),'-encoding','UTF-8','-cp',cp,'-d',d]+files,check=True)
 for order in ['craft-first','ufo-first']:subprocess.run([str(java/'java'),'-Djava.awt.headless=true','-cp',d+os.pathsep+cp,'Probe',order],cwd=d,check=True)
print('PASS',len(slices),'pinned exact source slices; all are bounded projections, not whole owners or game testing')
