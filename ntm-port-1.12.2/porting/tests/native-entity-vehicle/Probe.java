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
 static void defaults(Entity a,Entity b,Object... expected) throws Exception {
  List<EntityDataManager.DataEntry<?>> all=a.getDataManager().getAll(); Set<Integer> ids=new HashSet<>(); for(EntityDataManager.DataEntry<?> e:all) check(ids.add(e.getKey().getId()),"no vanilla/custom collision");
  check(ids.size()==6+expected.length/2,"exact native key count"); packet(a,b,true);
  for(int i=0;i<expected.length;i+=2) { DataParameter<?> k=key(a.getClass(),(String)expected[i]); check(a.getDataManager().get(k).equals(expected[i+1]) && b.getDataManager().get(k).equals(expected[i+1]),"original default/full snapshot"); }
 }
 public static void main(String[] args) throws Exception {
  EntityBoatRubber boat=new EntityBoatRubber(null), boatClient=new EntityBoatRubber(null);
  EntityMinerRocket rocket=new EntityMinerRocket(null), rocketClient=new EntityMinerRocket(null);
  EntitySoyuz soyuz=new EntitySoyuz(null), soyuzClient=new EntitySoyuz(null);
  EntityCombatDropPod pod=new EntityCombatDropPod(null), podClient=new EntityCombatDropPod(null);
  defaults(boat,boatClient,"TIME_SINCE_HIT",0,"FORWARD_DIRECTION",1,"DAMAGE_TAKEN",0F);
  defaults(rocket,rocketClient,"FLIGHT_MODE",0,"SATELLITE_FREQUENCY",0);defaults(soyuz,soyuzClient,"SKIN",0);defaults(pod,podClient,"COLOR",0);
  check(Probe.<Float>key(EntityBoatRubber.class,"DAMAGE_TAKEN").getSerializer()==DataSerializers.FLOAT,"boat float serializer");
  boat.setTimeSinceHit(Integer.MIN_VALUE);boat.setForwardDirection(-123);boat.setDamageTaken(-0F);packet(boat,boatClient,false);
  check(boatClient.getTimeSinceHit()==Integer.MIN_VALUE && boatClient.getForwardDirection()==-123 && Float.floatToRawIntBits(boatClient.getDamageTaken())==0x80000000,"boat exact setter values no clamp");
  boat.getDataManager().setClean();boat.setForwardDirection(-123);check(!boat.getDataManager().isDirty(),"equal scalar setter stays clean");
  NBTTagCompound boatSave=new NBTTagCompound();boat.writeEntityToNBT(boatSave);check(boatSave.isEmpty(),"boat custom NBT remains empty");
  EntityBoatRubber freshBoat=new EntityBoatRubber(null);freshBoat.readEntityFromNBT(binary(boatSave));check(freshBoat.getForwardDirection()==1 && freshBoat.getDamageTaken()==0F,"boat fresh load original defaults");
  boat.readEntityFromNBT(boatSave);check(boat.getForwardDirection()==-123,"empty NBT does not reset live boat state");
  DataParameter<Integer> mode=key(EntityMinerRocket.class,"FLIGHT_MODE");
  for(int value:new int[]{0,-1,Integer.MIN_VALUE,Integer.MAX_VALUE}) {
   ConsumerReads.setFrequency(rocket,value);rocket.getDataManager().set(mode,value);rocket.timer=51;
   packet(rocket,rocketClient,false); check(ConsumerReads.frequency(rocketClient)==value && rocketClient.getFlightMode()==value,"dock scalar accessors/packet preserve arbitrary int");
   NBTTagCompound rn=new NBTTagCompound();rocket.writeEntityToNBT(rn);rocketClient.readEntityFromNBT(binary(rn));
   check(rocketClient.getFlightMode()==value && rocketClient.getSatelliteFrequency()==value && rocketClient.timer==51,"native binary rocket mode/sat/timer NBT");
  }
  rocket.getDataManager().set(mode,1);rocket.timer=49;check(!ConsumerReads.unload(rocket),"dock no early unloading");rocket.timer=50;check(ConsumerReads.unload(rocket),"exact dock mode1 timer50 predicate");rocket.timer=51;check(!ConsumerReads.unload(rocket),"dock no late unloading");
  rocket.readEntityFromNBT(new NBTTagCompound());check(rocket.getFlightMode()==0 && rocket.getSatelliteFrequency()==0 && rocket.timer==0,"rocket missing NBT zero defaults");
  for(int skin:new int[]{-1,0,1,Integer.MAX_VALUE}) {
   soyuz.setSkin(skin);packet(soyuz,soyuzClient,false);check(ConsumerReads.skin(soyuzClient)==skin,"actual Soyuz renderer typed read");
   soyuz.mode=3;soyuz.targetX=-100;soyuz.targetZ=205;NBTTagCompound sn=new NBTTagCompound();soyuz.writeScalarPrefix(sn);soyuzClient.readScalarPrefix(binary(sn));
   check(soyuzClient.getSkin()==skin && soyuzClient.mode==3 && soyuzClient.targetX==-100 && soyuzClient.targetZ==205,"exact Soyuz pre-payload scalar save prefix");
   check(!sn.hasKey("items"),"bounded prefix does not pretend to serialize payload");
  }
  NBTTagCompound payload=new NBTTagCompound();payload.setString("id","unchanged_payload");pod.setPayload(payload,4,0x12345678);
  NBTTagCompound pn=new NBTTagCompound();pod.writeEntityToNBT(pn);podClient.readEntityFromNBT(binary(pn));
  check(podClient.getColor()==0x12345678,"pod actual NBT color setter updates manager");
  NBTTagCompound roundtrip=new NBTTagCompound();podClient.writeEntityToNBT(roundtrip);check(roundtrip.getInteger("color")==0x12345678 && roundtrip.getInteger("amount")==4 && roundtrip.getCompoundTag("entityType").getString("id").equals("unchanged_payload"),"pod original payload NBT untouched");
  pod.setColor(-1);packet(pod,podClient,false);check(podClient.getColor()==-1,"pod scalar metadata arbitrary color");
  NBTTagCompound clientSave=new NBTTagCompound();podClient.writeEntityToNBT(clientSave);check(clientSave.getInteger("color")==0x12345678,"metadata does not update separate local color save field (original behavior)");
  podClient.readEntityFromNBT(new NBTTagCompound());check(podClient.getColor()==0,"pod missing NBT original zero");
  System.out.println("PASS: "+checks+" native vehicle metadata/default/NBT/accessor lifecycle assertions");
 }
}
