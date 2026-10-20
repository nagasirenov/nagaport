#!/usr/bin/env python3
"""Bounded exact source fragments plus complete FallingNuke; native metadata packets, no world effects."""
from pathlib import Path
import hashlib,json,os,re,subprocess,sys,tempfile
folder=Path(__file__).resolve().parent;root=folder.parents[3];src='ntm-port-1.12.2/src/main/java/';manifest=json.loads((folder/'source-manifest.json').read_text());slices={};units={}
def source(path):
 p=root/(src+path)
 return p.read_bytes().decode() if p.exists() else subprocess.check_output(['git','-C',str(root),'show',manifest['base']+':'+src+path]).decode()
def extract(s,pat):
 m=re.search(pat,s);assert m,pat
 i=s.index('{',m.end()-1);end=i+1;depth=1
 while depth:depth+=(s[end]=='{')-(s[end]=='}');end+=1
 return s[m.start():end]
def keep(n,label,s):slices[n+'.'+label]={'sha256':hashlib.sha256(s.encode()).hexdigest(),'source':s};return s
def method(s,n):return extract(s,r'(?:public|protected|private) (?:static )?(?:<[^>]+> )?[\w<>]+ '+n+r'\([^)]*\)\s*\{')
def keys(s,n):return [keep(n,'key'+str(i),x) for i,x in enumerate(re.findall(r'private static final DataParameter<[^>]+> [^;]+;',s))]
imports='import net.minecraft.entity.*;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.world.World;import net.minecraft.nbt.*;import net.minecraft.network.datasync.*;import net.minecraft.util.math.*;'
def unit(path,parent,parts,abstract=False):
 package,name=path.rsplit('/',1);units[path+'.java']='package '+package.replace('/','.')+';'+imports+'\npublic '+('abstract ' if abstract else '')+'class '+name+' extends '+parent+' {\n'+'\n'.join(parts)+'\n}'
# Unchanged true parent allocation and exact complete world constructor. Other parent behavior is excluded.
n='EntityThrowableNT';s=source('com/hbm/entity/projectile/'+n+'.java');parts=keys(s,n)
parts.append(keep(n,'world-constructor',extract(s,r'public EntityThrowableNT\(World world\)\s*\{')))
parts += [keep(n,m,method(s,m)) for m in ['entityInit','setStuckIn','getStuckIn']]
parts += ['protected void readEntityFromNBT(NBTTagCompound n){throw new AssertionError("ThrowableNT inherited NBT excluded: legacy owner-name API remains");}', 'protected void writeEntityToNBT(NBTTagCompound n){throw new AssertionError("ThrowableNT inherited NBT excluded: legacy owner-name API remains");}']
unit('com/hbm/entity/projectile/'+n,'Entity',parts,True)
n='EntityChemical';s=source('com/hbm/entity/projectile/'+n+'.java');parts=keys(s,n)
parts.append(keep(n,'world-constructor',extract(s,r'public EntityChemical\(World world\)\s*\{')))
parts += [keep(n,m,method(s,m)) for m in ['entityInit','setFluid','getType','getStyle','getStyleFromType','getAirDrag','getWaterDrag','getMaxAge','getGravityVelocity']]
parts.append(keep(n,'style-enum',extract(s,r'public static enum ChemicalStyle\s*\{')))
unit('com/hbm/entity/projectile/'+n,'EntityThrowableNT',parts)
units['com/hbm/entity/projectile/EntityChemical.java']=units['com/hbm/entity/projectile/EntityChemical.java'].replace(imports,imports+'import com.hbm.inventory.fluid.*;import com.hbm.inventory.fluid.trait.*;')
# Complete actual FallingNuke compiles, including all constructors, native NBT overrides and unchanged update.
n='EntityFallingNuke';units['com/hbm/entity/projectile/'+n+'.java']=keep(n,'complete-production-class',source('com/hbm/entity/projectile/'+n+'.java'))
units['com/hbm/world/WorldBlockState.java']=keep('WorldBlockState','complete-production-class',source('com/hbm/world/WorldBlockState.java'))
units['com/hbm/blocks/bomb/NukeCustom.java']='package com.hbm.blocks.bomb; public class NukeCustom {public static void explodeCustom(net.minecraft.world.World w,double x,double y,double z,float a,float b,float c,float d,float e,float f,float g){throw new AssertionError("Explosion payload outside probe scope");}}'
# Complete Depress constructors, field initializers, direction accessors and NBT. No AABB/force/tick simulation.
n='EntityDepress';s=source('com/hbm/entity/effect/'+n+'.java');parts=keys(s,n)
for field in ['public int timeToLive;','public static double range = 8;']:assert field in s;parts.append(keep(n,field,field))
for args in [r'World world',r'World world, ForgeDirection dir, int timeToLive']:parts.append(keep(n,'constructor-'+args,extract(s,r'public EntityDepress\('+args+r'\)\s*\{')))
parts += [keep(n,m,method(s,m)) for m in ['entityInit','setDir','getDir','readEntityFromNBT','writeEntityToNBT']]
prefix=method(s,'onUpdate');prefix=prefix[prefix.index('\n\t\tForgeDirection dir = getDir();'):prefix.index('\n\t\tMainRegistry.proxy.effectNT(data);')]
parts.append('public NBTTagCompound particleParametersSlice() {'+keep(n,'exact-particle-parameters-slice',prefix)+'\nreturn data;}')
unit('com/hbm/entity/effect/'+n,'Entity',parts);units['com/hbm/entity/effect/'+n+'.java']=units['com/hbm/entity/effect/'+n+'.java'].replace(imports,imports+'import com.hbm.lib.ForgeDirection;')
units['com/hbm/lib/ForgeDirection.java']=keep('ForgeDirection','complete-production-class',source('com/hbm/lib/ForgeDirection.java'))
# Complete Torex constructor and original scalar fields plus actual inner Cloudlet constructors/color consumer.
n='EntityNukeTorex';s=source('com/hbm/entity/effect/'+n+'.java');parts=keys(s,n)
fields=s[s.index('\n\tpublic double coreHeight'):s.index('\n\tpublic EntityNukeTorex(World world)')];parts.append(keep(n,'all-owner-fields',fields))
parts.append(keep(n,'complete-constructor',extract(s,r'public EntityNukeTorex\(World world\)\s*\{')))
parts += [keep(n,m,method(s,m)) for m in ['entityInit','setScale','setSynchronizedScale','setType','getScale','getMaxAge','getSimulationSpeed','getSaturation','getGreying','getAlpha','writeEntityToNBT','writeToNBTOptional','readEntityFromNBT']]
cloudfields=s[s.index('\n\t\tpublic double posX;'):s.index('\n\t\tpublic Cloudlet(')];cp=[keep(n,'cloudlet-fields',cloudfields)]
for args in [r'double posX, double posY, double posZ, float angle, int age, int maxAge',r'double posX, double posY, double posZ, float angle, int age, int maxAge, TorexType type']:cp.append(keep(n,'cloudlet-constructor-'+args,extract(s,r'public Cloudlet\('+args+r'\)\s*\{')))
cp.append(keep(n,'cloudlet-updateColor',method(s,'updateColor')));parts.append('public class Cloudlet {'+'\n'.join(cp)+'}')
parts.append(keep(n,'torex-enum',extract(s,r'public static enum TorexType\s*\{')))
unit('com/hbm/entity/effect/'+n,'Entity',parts);units['com/hbm/entity/effect/'+n+'.java']=units['com/hbm/entity/effect/'+n+'.java'].replace(imports,imports+'import java.util.ArrayList;import java.awt.Color;import com.hbm.dim.CelestialBody;import com.hbm.dim.trait.CBT_Atmosphere;')
# Explicit test-only atmosphere input fixture, not mod startup/planet simulation.
units['com/hbm/dim/trait/CBT_Atmosphere.java']='package com.hbm.dim.trait;public class CBT_Atmosphere {public double pressure; public CBT_Atmosphere(double p){pressure=p;} public double getPressure(){return pressure;}}'
units['com/hbm/dim/CelestialBody.java']='package com.hbm.dim;import com.hbm.dim.trait.CBT_Atmosphere;public class CelestialBody {public static CBT_Atmosphere atmosphere=new CBT_Atmosphere(1);public static boolean orbit;public static <T>T getTrait(net.minecraft.world.World w,Class<T> type){return type.cast(atmosphere);}public static boolean inOrbit(net.minecraft.world.World w){return orbit;}}'
# Bounded NTM fluid fixture: original ID/getType/trait methods and registry lookup, explicit fixture contents.
s=source('com/hbm/inventory/fluid/FluidType.java');parts=[]
for field in ['private int id;','private int color;','public HashMap<Class<? extends FluidTrait>, FluidTrait> traits = new HashMap();']:assert field in s;parts.append(keep('FluidType',field,field))
parts += [keep('FluidType',m,method(s,m)) for m in ['getID','getColor','hasTrait','isAntimatter']]
parts.append('public FluidType(int fixtureID,int fixtureColor,FluidTrait... fixtureTraits){this.id=fixtureID;this.color=fixtureColor;for(FluidTrait t:fixtureTraits)traits.put(t.getClass(),t);}')
units['com/hbm/inventory/fluid/FluidType.java']='package com.hbm.inventory.fluid;import java.util.HashMap;import com.hbm.inventory.fluid.trait.*; public class FluidType {'+'\n'.join(parts)+'}'
s=source('com/hbm/inventory/fluid/Fluids.java');parts=[keep('Fluids','idMapping','private static final HashMap<Integer, FluidType> idMapping = new HashMap();'),keep('Fluids','fromID',method(s,'fromID'))]
parts+=['public static final FT_Gaseous GASEOUS=new FT_Gaseous();public static final FT_Gaseous_ART EVAP=new FT_Gaseous_ART();public static final FT_Liquid LIQUID=new FT_Liquid();public static final FluidType NONE=new FluidType(0,0x888888),IONGEL=new FluidType(1,0x99aaff);static{idMapping.put(0,NONE);idMapping.put(1,IONGEL);} public static void fixtureRegister(FluidType f){idMapping.put(f.getID(),f);}']
units['com/hbm/inventory/fluid/Fluids.java']='package com.hbm.inventory.fluid;import java.util.HashMap;import com.hbm.inventory.fluid.trait.*;public class Fluids {'+'\n'.join(parts)+'}'
for typ in ['FluidTrait','FT_Amat','FT_Liquid','FT_Gaseous','FT_Gaseous_ART','FT_Flammable','FT_Combustible']:units['com/hbm/inventory/fluid/trait/'+typ+'.java']='package com.hbm.inventory.fluid.trait;public class '+typ+(' extends FluidTrait' if typ!='FluidTrait' else '')+' {}'
# Exact external metadata-only setters/renderer read, with no enclosing gameplay/GL side effects.
parts=[]
s=source('com/hbm/blocks/bomb/NukeCustom.java');line=next(l.strip() for l in s.splitlines() if 'bomb.setBlockMetadata' in l);parts.append('public static void setFallingFromBlock(EntityFallingNuke bomb,World world,int x,int y,int z) {'+keep('NukeCustom','exact-metadata-application',line)+'}')
s=source('com/hbm/blocks/bomb/NukeBoy.java');line=next(l.strip() for l in s.splitlines() if 'torex.setSynchronizedScale' in l);parts.append('public static void setBoyScale(EntityNukeTorex torex) {'+keep('NukeBoy','exact-metadata-application',line)+'}')
s=source('com/hbm/render/entity/projectile/RenderFallingNuke.java');expr=re.search(r'switch\((.+)\) \{',s)[1];parts.append('public static byte rendererLookup(Entity p_76986_1_) {return '+keep('RenderFallingNuke','exact-lookup-expression',expr)+';}')
units['ConsumerSlices.java']=imports+'import com.hbm.entity.effect.EntityNukeTorex;import com.hbm.entity.projectile.EntityFallingNuke;public class ConsumerSlices {'+'\n'.join(parts)+'}'
expected=folder/'slices.json'
if '--record-slices' in sys.argv:expected.write_text(json.dumps(slices,indent=2)+'\n')
assert slices==json.loads(expected.read_text()),'Reviewed executable slices changed'
for e in manifest['entries']:assert hashlib.sha256((root/e['path']).read_bytes()).hexdigest()==e['after_sha256']
cpfile=Path(os.environ.get('NAGAPORT_NATIVE_CP_FILE','/tmp/nagaport-ae2-native-classpath.txt'));assert cpfile.is_file();cp=cpfile.read_text().strip();java=Path(os.environ.get('JAVA_HOME','/workspace/shared/nagaport-toolchain/java8'))/'bin'
with tempfile.TemporaryDirectory(prefix='native-projectile-utility-data-') as d:
 t=Path(d);files=[]
 for path,s in units.items():p=t/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(s);files.append(str(p))
 files.append(str(folder/'Probe.java'))
 subprocess.run([str(java/'javac'),'-encoding','UTF-8','-cp',cp,'-d',d]+files,check=True)
 for order in ['projectiles-first','effects-first']:subprocess.run([str(java/'java'),'-Djava.awt.headless=true','-cp',d+os.pathsep+cp,'Probe',order],cwd=d,check=True)
print('PASS '+str(len(slices))+' pinned slices/whole-class inputs; whole FallingNuke and bounded Chemical/Depress/Torex; no physics, explosion, mod registry, tickets, transport, or GL claim')
