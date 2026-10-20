import net.minecraft.entity.Entity;
import net.minecraft.nbt.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.*;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import io.netty.buffer.Unpooled;
import java.lang.reflect.*;
import java.util.*;

public class Probe {
 static int checks;
 static void check(boolean b,String why) { checks++; if(!b) throw new AssertionError(why); }
 @SuppressWarnings("unchecked") static <T> DataParameter<T> key(Class<?> c,String n) throws Exception { Field f=c.getDeclaredField(n); f.setAccessible(true); return (DataParameter<T>)f.get(null); }
 static void packet(Entity a,Entity b,boolean full) throws Exception {
  PacketBuffer buf=new PacketBuffer(Unpooled.buffer()); new SPacketEntityMetadata(a.getEntityId(),a.getDataManager(),full).writePacketData(buf);
  SPacketEntityMetadata p=new SPacketEntityMetadata(); p.readPacketData(buf); check(buf.readableBytes()==0,"real metadata bytes consumed"); if(p.getDataManagerEntries()!=null) b.getDataManager().setEntryValues(p.getDataManagerEntries()); else check(!a.getDataManager().isDirty(),"unchanged default produces empty delta");buf.release();
 }
 static NBTTagCompound binary(NBTTagCompound n) throws Exception {
  java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream(); CompressedStreamTools.write(n,new java.io.DataOutputStream(bytes));
  return CompressedStreamTools.read(new java.io.DataInputStream(new java.io.ByteArrayInputStream(bytes.toByteArray())));
 }
 static class Tier4Probe extends EntityMissileTier4 {Tier4Probe(){super(null);}}
 public static void main(String[] args) throws Exception {
  EntityMissileCustom server=new EntityMissileCustom(null),client=new EntityMissileCustom(null);Tier4Probe tier=new Tier4Probe(),tc=new Tier4Probe();
  check(AssumedTicketBoundary.returns==4,"explicit assumed-return marker, NOT executed Forge ticket operation");
  DataParameter<Byte> direction=key(EntityMissileBaseNT.class,"DIRECTION"),stuck=key(EntityThrowableNT.class,"STUCK_FACE");DataParameter<Integer> health=key(EntityMissileCustom.class,"HEALTH_SNAPSHOT");
  check(stuck.getId()<direction.getId() && direction.getId()<health.getId(),"real native inherited allocation");check(direction.getSerializer()==DataSerializers.BYTE && health.getSerializer()==DataSerializers.VARINT,"correct serializers");
  check(server.health==50 && server.getDataManager().get(health)==0,"Java virtual constructor-time register reads zero before health initializer50");check(server.getDirection()==5 && server.getWarheadId()==0 && server.getFuselageId()==0 && server.getFinsId()==0 && server.getThrusterId()==0,"exact defaults after assumed ticket return");
  for(Entity e:new Entity[]{server,client,tier,tc}) {Set<Integer> ids=new HashSet<>();for(EntityDataManager.DataEntry<?> d:e.getDataManager().getAll())check(ids.add(d.getKey().getId()),"no vanilla/base/child duplicate");check(ids.size()==(e instanceof EntityMissileCustom?13:8),"exact native entry count");}
  packet(server,client,true);packet(tier,tc,true);check(client.getDirection()==5 && client.getDataManager().get(health)==0,"native full default snapshot");
  for(byte v:new byte[]{-128,-1,0,2,3,4,5,127}) {
   server.setDirection(v);tier.setDirection(v);packet(server,client,false);packet(tier,tc,false);check(client.getDirection()==v && tc.contrailDirectionSlice()==v,"inherited direction accessor and Tier4 exact read");
   check(Consumers.Generic(client)==v && Consumers.Huge(client)==v && Consumers.Strong(client)==v && Consumers.Nuclear(client)==v && Consumers.Taint(client)==v,"five guarded renderer expressions");
  }
  server.getDataManager().setClean();server.setDirection((byte)127);check(!server.getDataManager().isDirty(),"equal immutable byte clean");
  for(int metadata:new int[]{Integer.MIN_VALUE,0,11,12,13,14,15,16,Integer.MAX_VALUE}) {Consumers.launchDirection(server,metadata);check(server.getDirection()==(byte)Math.max(2,Math.min(5,metadata-10)),"exact launcher subtraction/clamp including original int overflow");}
  NBTTagCompound n=new NBTTagCompound();n.setInteger("warhead",Integer.MIN_VALUE);n.setInteger("fuselage",-1);n.setInteger("fins",0);n.setInteger("thruster",Integer.MAX_VALUE);n.setFloat("fuel",-3.5F);n.setFloat("consumption",0.125F);server.readCustomTail(binary(n));packet(server,client,false);
  check(Arrays.equals(Consumers.parts(client),new int[]{Integer.MIN_VALUE,-1,0,Integer.MAX_VALUE}),"four typed renderer part IDs through native packet");
  NBTTagCompound save=new NBTTagCompound();server.writeCustomTail(save);client.readCustomTail(binary(save));check(client.fuel==-3.5F && client.consumption==0.125F && client.getThrusterId()==Integer.MAX_VALUE,"exact custom scalar NBT tail");
  server.motionX=-2;server.motionY=3;server.motionZ=4;server.posX=5;server.posY=6;server.posZ=7;server.targetX=8;server.targetZ=9;server.startX=10;server.startZ=11;server.velocity=12;server.decelY=.2;server.accelXZ=.3;
  save=new NBTTagCompound();server.writeBaseTail(save);client.readBaseTail(binary(save));check(client.motionX==-2 && client.posZ==7 && client.targetZ==9 && client.startX==10 && client.velocity==12 && client.decelY==.2 && client.accelXZ==.3,"complete base tail after excluded ThrowableNT NBT");check(!save.hasKey("health") && !save.hasKey("direction"),"health snapshot/direction not newly saved");
  client.setDirection((byte)3);client.readCustomTail(new NBTTagCompound());check(client.getWarheadId()==0 && client.getThrusterId()==0 && client.fuel==0 && client.getDirection()==3,"missing custom tags reset parts, not unsaved direction");
  // Existing sibling's exact static key declarations allocate after new base key. Do not instantiate full rideable or test ItemStack alias semantics.
  Set<Integer> siblingIds=new HashSet<>();for(Field field:EntityRideableRocket.class.getDeclaredFields())if(field.getType()==DataParameter.class){DataParameter<?> k=key(EntityRideableRocket.class,field.getName());check(k.getId()>direction.getId() && siblingIds.add(k.getId()),"existing sibling own keys allocate after base");}
  Field rocket=EntityRideableRocket.class.getDeclaredField("ROCKET");rocket.setAccessible(true);Object metadata=rocket.get(null);for(Field field:metadata.getClass().getDeclaredFields()){field.setAccessible(true);Object value=field.get(metadata);if(value instanceof DataParameter){DataParameter<?> k=(DataParameter<?>)value;check(k.getId()>direction.getId() && siblingIds.add(k.getId()),"existing RocketMetadata key allocation");}else if(value instanceof List)for(Object entry:(List<?>)value){DataParameter<?> k=(DataParameter<?>)entry;check(k.getId()>direction.getId() && siblingIds.add(k.getId()),"existing stage pair allocation");}}
  check(siblingIds.size()==15,"all15 existing sibling keys collision-free, no fixed-ID assumption");
  System.out.println("PASS: "+checks+" native POST-TICKET-SLICE metadata/allocation/consumer/NBT-tail assertions; no chunk lifecycle claim");
 }
}
class AssumedTicketBoundary {
 static int returns;
 static void recordReturn(Entity entity) {returns++;if(entity.getDataManager().getAll().size()!=7)throw new AssertionError("Boundary must follow ThrowableNT key and precede missile registration");}
}
