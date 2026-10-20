#!/usr/bin/env python3
"""Post-ticket metadata model. DOES NOT EXECUTE/VERIFY FORGE TICKET OR FULL ENTITY LIFECYCLE."""
from pathlib import Path
import re,json,hashlib,subprocess,tempfile,os
folder=Path(__file__).parent;root=folder.resolve().parents[3];src=root/'ntm-port-1.12.2/src/main/java';manifest=json.loads((folder/'source-manifest.json').read_text())
def extract(s,pattern):
 m=re.search(pattern,s);assert m,pattern
 opening=s.index('{',m.end()-1);end=opening+1;depth=1
 while depth:depth+=(s[end]=='{')-(s[end]=='}');end+=1
 return s[m.start():end]
def method(s,name):return extract(s,r'(?:public|protected) (?:void|int|byte|double|float) '+name+r'\([^)]*\)\s*\{')
def ctor(s,name):return extract(s,r'public '+name+r'\(World world\)\s*\{')
def get(name):return next(src.rglob(name+'.java')).read_text()
for e in manifest['entries']:assert hashlib.sha256((root/e['path']).read_bytes()).hexdigest()==e['after_sha256']
for e in manifest['unchanged_dependencies']:assert hashlib.sha256((root/e['path']).read_bytes()).hexdigest()==e['sha256']
imports='import java.util.*; import net.minecraft.entity.*; import net.minecraft.world.World; import net.minecraft.nbt.*; import net.minecraft.item.ItemStack; import net.minecraft.network.datasync.*; import com.hbm.handler.RocketMetadata;'
with tempfile.TemporaryDirectory(prefix='native-missile-slice-') as temp:
 t=Path(temp);units=[]
 def unit(name,parent,parts,abstract=False):
  p=t/(name+'.java');p.write_text(imports+'\n'+('abstract ' if abstract else '')+'class '+name+' extends '+parent+' {\n'+'\n'.join(parts)+'\n}');units.append(str(p))
 nt=get('EntityThrowableNT');interp=get('EntityThrowableInterp');base=get('EntityMissileBaseNT');custom=get('EntityMissileCustom');tier=get('EntityMissileTier4');ride=get('EntityRideableRocket')
 unsupported='protected void readEntityFromNBT(NBTTagCompound nbt) {throw new AssertionError("Full parent NBT unsupported in post-ticket slice");} protected void writeEntityToNBT(NBTTagCompound nbt) {throw new AssertionError("Full parent NBT unsupported in post-ticket slice");}'
 unit('EntityThrowableNT','Entity',[re.search(r'private static final DataParameter<Byte> STUCK_FACE[^;]+;',nt).group(),ctor(nt,'EntityThrowableNT'),method(nt,'entityInit'),method(nt,'getStuckIn'),method(nt,'setStuckIn'),unsupported],True)
 unit('EntityThrowableInterp','EntityThrowableNT',[ctor(interp,'EntityThrowableInterp')],True)
 fields=re.findall(r'public (?:int|double|boolean) \w+(?: = [^;]+)?;',base);fields+=re.findall(r'protected static final DataParameter<Byte> [^;]+;',base)
 init=method(base,'entityInit');ticket='init(ForgeChunkManager.requestTicket(MainRegistry.instance, this.world, Type.ENTITY));';assert init.count(ticket)==1
 # EXPLICIT ASSUMPTION, NOT A FAKE ForgeChunkManager: retain sequence, record the boundary assumed returned.
 modeled=init.replace(ticket,'AssumedTicketBoundary.recordReturn(this);')
 parts=fields+[ctor(base,'EntityMissileBaseNT'),modeled,method(base,'getDirection'),method(base,'setDirection')]
 for op,direction in [('read','From'),('write','To')]:
  full=method(base,op+'Entity'+direction+'NBT');call='super.'+op+'Entity'+direction+'NBT(nbt);';assert full.count(call)==1;parts.append('public void '+op+'BaseTail(NBTTagCompound nbt) {'+full[full.index(call)+len(call):])
 unit('EntityMissileBaseNT','EntityThrowableInterp',parts,True)
 parts=re.findall(r'private static final DataParameter<Integer> [^;]+;',custom)+re.findall(r'public float \w+;',custom)+[ctor(custom,'EntityMissileCustom'),method(custom,'entityInit')]+[method(custom,n) for n in ['getWarheadId','getFuselageId','getFinsId','getThrusterId']]
 for op,direction in [('read','From'),('write','To')]:
  full=method(custom,op+'Entity'+direction+'NBT');call='super.'+op+'Entity'+direction+'NBT(nbt);';assert full.count(call)==1;parts.append('public void '+op+'CustomTail(NBTTagCompound nbt) {'+full[full.index(call)+len(call):])
 unit('EntityMissileCustom','EntityMissileBaseNT',parts)
 line=next(l.strip() for l in tier.splitlines() if 'byte rot = this.dataManager.get(DIRECTION);' in l)
 unit('EntityMissileTier4','EntityMissileBaseNT',[ctor(tier,'EntityMissileTier4'),'public byte contrailDirectionSlice() {'+line+' return rot;}'],True)
 # Actual existing sibling key declarations, including RocketMetadata allocation. Only static allocation is tested.
 fields=re.findall(r'private static final DataParameter<[^>]+> [^;]+;',ride[:ride.index('private double rocketVelocity')]);fields.append(re.search(r'private static final RocketMetadata ROCKET = new RocketMetadata\([\s\S]*?\);',ride).group())
 unit('EntityRideableRocket','EntityMissileBaseNT',fields+['EntityRideableRocket(World world) {super(world);}'],True)
 p=t/'RocketMetadata.java';p.write_text(get('RocketMetadata'));units.append(str(p))
 # Consumers are isolated exact source expressions, not renderer/launch simulation.
 readers=[]
 for name in ['Generic','Huge','Nuclear','Strong','Taint']:
  render=get('RenderMissile'+name);assert 'if(entity instanceof EntityMissileBaseNT) switch(((EntityMissileBaseNT) entity).getDirection())' in render
  readers.append('static byte '+name+'(Entity entity) {if(entity instanceof EntityMissileBaseNT) return ((EntityMissileBaseNT) entity).getDirection();throw new AssertionError("Not missile");}')
 render=get('RenderMissileCustom');lines=[l.strip() for l in render.splitlines() if re.search(r'int [wfst] = ',l)];assert len(lines)==4
 readers.append('static int[] parts(Entity entity) {'+' '.join(lines)+' return new int[]{w,f,s,t};}')
 launch=next(l.strip() for l in get('TileEntityLaunchPadBase').splitlines() if 'missile.setDirection(' in l)
 # Only remove receiver of tile metadata read for a pure source-expression helper with matching int input.
 assert launch.count('this.getBlockMetadata()')==1
 readers.append('static void launchDirection(EntityMissileBaseNT missile,int blockMetadata) {'+launch.replace('this.getBlockMetadata()','blockMetadata')+'}')
 p=t/'Consumers.java';p.write_text(imports+' import net.minecraft.util.math.MathHelper; class Consumers {'+' '.join(readers)+'}');units.append(str(p))
 p=t/'Probe.java';p.write_text((folder/'Probe.java').read_text());units.append(str(p))
 cp=Path(os.environ.get('NAGAPORT_NATIVE_CP_FILE','/tmp/nagaport-ae2-native-classpath.txt')).read_text().strip();java=Path(os.environ.get('JAVA_HOME','/workspace/shared/nagaport-toolchain/java8'))/'bin'
 subprocess.run([str(java/'javac'),'-cp',cp,'-d',temp]+units,check=True);subprocess.run([str(java/'java'),'-cp',temp+':'+cp,'Probe'],check=True)
 print('PASS: real native post-ticket metadata slice ONLY; ticket return assumed, full constructor/chunk lifecycle UNVERIFIED')
