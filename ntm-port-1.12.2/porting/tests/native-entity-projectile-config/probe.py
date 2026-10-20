#!/usr/bin/env python3
"""Real native data/NBT/packet boundary, source-extracted model, no full entity/gameplay claim."""
from pathlib import Path
import hashlib,json,os,re,subprocess,tempfile,sys
folder=Path(__file__).resolve().parent;root=folder.parents[3];src=root/'ntm-port-1.12.2/src/main/java';manifest=json.loads((folder/'source-manifest.json').read_text());slices={}
def get(n):return next(src.rglob(n+'.java')).read_text()
def extract(s,pattern):
 m=re.search(pattern,s);assert m,pattern
 opening=s.index('{',m.end()-1);end=opening+1;depth=1
 while depth:depth+=(s[end]=='{')-(s[end]=='}');end+=1
 return s[m.start():end]
def method(s,n):return extract(s,r'(?:public|protected) (?:void|int|byte|boolean|double\[\]|BulletConfig|EntityArtilleryRocket|EntityArtilleryShell|HIMARSRocket|ArtilleryShell) '+n+r'\([^)]*\)\s*\{')
def ctor(s,n):return extract(s,r'public '+n+r'\(World world\)\s*\{')
def keep(owner,label,s):slices[owner+'.'+label]={'sha256':hashlib.sha256(s.encode()).hexdigest(),'source':s};return s
imports='import java.util.*; import net.minecraft.entity.*; import net.minecraft.world.World; import net.minecraft.nbt.*; import net.minecraft.network.datasync.*; import net.minecraft.util.math.Vec3d; import net.minecraft.util.ResourceLocation; import io.netty.buffer.ByteBuf; import ItemAmmoHIMARS.HIMARSRocket; import ItemAmmoArty.ArtilleryShell;'
# Default-package nested types cannot be imported, so insert explicit nested fixture aliases instead.
imports=imports.replace(' import ItemAmmoHIMARS.HIMARSRocket; import ItemAmmoArty.ArtilleryShell;','')
units={}
def unit(n,parent,parts,abstract=False):units[n]=imports+'\n'+('abstract ' if abstract else '')+'class '+n+' extends '+parent+' {\n'+'\n'.join(parts)+'\n}'
unsupported='protected void readEntityFromNBT(NBTTagCompound nbt) {throw new AssertionError("Full inherited NBT is outside metadata model");} protected void writeEntityToNBT(NBTTagCompound nbt) {throw new AssertionError("Full inherited NBT is outside metadata model");}'
nt=get('EntityThrowableNT');interp=get('EntityThrowableInterp')
parts=[keep('EntityThrowableNT','key',re.search(r'private static final DataParameter<Byte> STUCK_FACE[^;]+;',nt).group()),keep('EntityThrowableNT','constructor',ctor(nt,'EntityThrowableNT'))]
parts +=[keep('EntityThrowableNT',m,method(nt,m)) for m in ['entityInit','setStuckIn','getStuckIn']]
parts +=['protected EntityLivingBase thrower;',unsupported]
unit('EntityThrowableNT','Entity',parts,True)
unit('EntityThrowableInterp','EntityThrowableNT',[keep('EntityThrowableInterp','constructor',ctor(interp,'EntityThrowableInterp'))],True)
ticket='init(ForgeChunkManager.requestTicket(MainRegistry.instance, this.world, Type.ENTITY));'
for n,parent in [('EntityBulletBaseMK4','EntityThrowableInterp'),('EntityBulletBeamBase','Entity'),('EntityArtilleryRocket','EntityThrowableInterp'),('EntityArtilleryShell','EntityThrowableNT'),('EntityBulletBaseMK4CL','EntityBulletBaseMK4')]:
 s=get(n);parts=[]
 for i,key in enumerate(re.findall(r'private static final DataParameter<Integer> [^;]+;',s)):parts.append(keep(n,'key'+str(i),key))
 init=keep(n,'entityInit',method(s,'entityInit'))
 if ticket in init:
  assert init.count(ticket)==1
  # Explicit assumed-return marker. No fake ForgeChunkManager or attempted real chunk setup.
  init=init.replace(ticket,'AssumedTicketBoundary.recordReturn(this);')
 parts.append(init)
 if n in ['EntityArtilleryShell','EntityBulletBaseMK4CL']:parts.append(keep(n,'constructor',ctor(s,n)))
 else:parts.append('public '+n+'(World world) {super(world);}') # TEST MODEL ONLY; real production ctor is not certified.
 if n.startswith('EntityBullet') and n!='EntityBulletBaseMK4CL':
  parts.append(keep(n,'config-field',re.search(r'public BulletConfig config;',s).group()))
  for m in ['setBulletConfig','getBulletConfig']:parts.append(keep(n,m,method(s,m)))
  lazy=keep(n,'lazy-resolution',next(l.strip() for l in s.splitlines() if 'if(config == null) config = this.getBulletConfig();' in l))
  parts.append('public void resolveConfigSlice() {'+lazy+'}')
  if n=='EntityBulletBeamBase':
   parts+=['public double beamLength; public EntityLivingBase thrower;']
   for m in ['readEntityFromNBT','writeEntityToNBT','writeToNBTOptional','writeSpawnData','readSpawnData']:parts.append(keep(n,m,method(s,m)))
  else:
   parts.append(keep(n,'writeSpawnData',method(s,'writeSpawnData')))
 if n.startswith('EntityArtillery'):
  for m in ['setType','getType']:parts.append(keep(n,m,method(s,m)))
  if n=='EntityArtilleryRocket':
   parts.append(keep(n,'getTypeId',method(s,'getTypeId')));parts.append('public Vec3d lastTargetPos;')
  else:
   for field in ['targetX','targetY','targetZ','shouldWhistle','didWhistle']:
    parts.append(keep(n,'field-'+field,re.search(r'private (?:double|boolean) '+field+r'(?: = [^;]+)?;',s).group()))
   for m in ['getTarget','setTarget','setWhistle','getWhistle','didWhistle']:parts.append(keep(n,m,method(s,m)))
  for op,direction in [('read','From'),('write','To')]:
   full=method(s,op+'Entity'+direction+'NBT');call='super.'+op+'Entity'+direction+'NBT(nbt);';assert full.count(call)==1
   tail=full[full.index(call)+len(call):]
   if n=='EntityArtilleryShell':tail=tail[:tail.index('NBTTagCompound compound' if op=='read' else 'if(this.cargo != null)')]+'}'
   keep(n,op+'-scalar-tail',tail)
   parts.append('public void '+op+'ScalarTail(NBTTagCompound nbt) {'+tail)
 unit(n,parent,parts)
# Minimal NTM configuration-registry fixture: exact list/id/constructor code, not payload behavior.
s=get('BulletConfig');units['BulletConfig']='import java.util.*; class BulletConfig {'+' '.join(keep('BulletConfig',label,text) for label,text in [('configs',re.search(r'public static List<BulletConfig> configs = new ArrayList\(\);',s).group()),('id','public int id;'),('constructor',extract(s,r'public BulletConfig\(\)\s*\{'))])+'}'
# Explicit two-entry NTM ammo fixtures. Array bounds/null/fallback selection is exercised, no content mechanics.
units['ItemAmmoHIMARS']='import net.minecraft.util.ResourceLocation; class HIMARSRocket {final ResourceLocation texture; HIMARSRocket(String n) {texture=new ResourceLocation("fixture",n);}} class ItemAmmoHIMARS {static HIMARSRocket[] itemTypes={new HIMARSRocket("zero"),new HIMARSRocket("one")};}'
units['ItemAmmoArty']='class ArtilleryShell {} class ItemAmmoArty {static ArtilleryShell[] itemTypes={new ArtilleryShell(),new ArtilleryShell()};}'
s=get('RenderArtilleryRocket');body=extract(s,r'protected ResourceLocation getEntityTexture\(Entity entity\)\s*\{');keep('RenderArtilleryRocket','getEntityTexture',body);units['TextureConsumer']=imports+' class TextureConsumer {'+body.replace('protected ResourceLocation','public static ResourceLocation',1)+'}'
# Separate selected-source hashes allow an independently audited cargo-loader migration to coexist.
expected=folder/'slices.json'
if '--record-slices' in sys.argv:expected.write_text(json.dumps(slices,indent=2)+'\n')
assert slices==json.loads(expected.read_text()),'Executed source slices changed; review before certifying them'
cpfile=Path(os.environ.get('NAGAPORT_NATIVE_CP_FILE','/tmp/nagaport-ae2-native-classpath.txt'))
assert cpfile.is_file(),'Missing authorized native classpath'
cp=cpfile.read_text().strip();java=Path(os.environ.get('JAVA_HOME','/workspace/shared/nagaport-toolchain/java8'))/'bin'
with tempfile.TemporaryDirectory(prefix='native-projectile-config-') as d:
 t=Path(d);files=[]
 for n,s in units.items():p=t/(n+'.java');p.write_text(s);files.append(str(p))
 files.append(str(folder/'Probe.java'))
 subprocess.run([str(java/'javac'),'-encoding','UTF-8','-cp',cp,'-d',d]+files,check=True)
 for order in ['artillery-first','sedna-first']:
  subprocess.run([str(java/'java'),'-Djava.awt.headless=true','-cp',d+os.pathsep+cp,'Probe',order],check=True)
 print('PASS: '+str(len(slices))+' exact source slices; genuine native EntityDataManager/packet/NBT only, synthetic owner constructors and assumed ticket returns explicitly excluded')
