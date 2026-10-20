package com.hbm.entity.train;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.*;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import net.minecraft.world.*;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.*;
import net.minecraft.profiler.Profiler;
import io.netty.buffer.Unpooled;
import java.lang.reflect.*;
import java.util.*;
import com.hbm.entity.train.EntityRailCarBase.BoundingBoxDummyEntity;
import com.hbm.entity.train.EntityRailCarRidable.SeatDummyEntity;

/** Reconstructed test artifact, not a copy of a recovered original log. */
public class Probe {
 static int checks;
 static void check(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
 @SuppressWarnings("unchecked") static <T> DataParameter<T> key(Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return (DataParameter<T>)f.get(null);}
 static class ProbeWorld extends World {
  ProbeWorld(boolean client){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),client);}
  protected IChunkProvider createChunkProvider(){throw new AssertionError("Chunk lifecycle outside metadata probe");}
  protected boolean isChunkLoaded(int x,int z,boolean allowEmpty){throw new AssertionError("Chunk lookup outside metadata probe");}
 }
 static class RegistrationSentinel extends TrainCargoTram {
  boolean observed;
  RegistrationSentinel(World world){super(world);}
  protected void entityInit(){check(slots==null&&passengerSeats==null,"virtual entityInit precedes actual Cargo/Ridable field allocation");super.entityInit();check(getSyncedOccupiedSlots()==0&&getPower()==0,"actual registrations available inside native constructor");observed=true;}
 }
 static List<EntityDataManager.DataEntry<?>> packet(Entity from,Entity to,boolean full)throws Exception {
  PacketBuffer buf=new PacketBuffer(Unpooled.buffer());new SPacketEntityMetadata(from.getEntityId(),from.getDataManager(),full).writePacketData(buf);
  SPacketEntityMetadata decoded=new SPacketEntityMetadata();decoded.readPacketData(buf);check(buf.readableBytes()==0,"native wire consumed");check(decoded.getEntityId()==from.getEntityId(),"native packet entity ID");
  List<EntityDataManager.DataEntry<?>> entries=decoded.getDataManagerEntries();if(entries!=null)to.getDataManager().setEntryValues(entries);buf.release();return entries;
 }
 static void allKeys(Entity e,int expected)throws Exception {
  Set<Integer> ids=new HashSet<>();for(EntityDataManager.DataEntry<?> entry:e.getDataManager().getAll())check(ids.add(entry.getKey().getId()),"unique native/inherited key");check(ids.size()==expected,"exact registration count");check(e.getAir()==300&&e.getCustomNameTag().equals("")&&!e.isSilent()&&!e.hasNoGravity(),"native base defaults");
 }
 static NBTTagCompound binary(NBTTagCompound n)throws Exception{java.io.ByteArrayOutputStream b=new java.io.ByteArrayOutputStream();CompressedStreamTools.write(n,new java.io.DataOutputStream(b));return CompressedStreamTools.read(new java.io.DataInputStream(new java.io.ByteArrayInputStream(b.toByteArray())));}
 static void bits(float a,float b,String why){check(Float.floatToIntBits(a)==Float.floatToIntBits(b),why);}
 public static void main(String[]args)throws Exception {
  net.minecraft.init.Bootstrap.register();
  if(args[0].equals("dummies-first")){Class.forName(BoundingBoxDummyEntity.class.getName());Class.forName(SeatDummyEntity.class.getName());}else Class.forName(TrainCargoTram.class.getName());
  ProbeWorld server=new ProbeWorld(false),client=new ProbeWorld(true);
  TrainCargoTram train=new TrainCargoTram(server),remoteTrain=new TrainCargoTram(client);TrainCargoTramTrailer trailer=new TrainCargoTramTrailer(server);
  RegistrationSentinel sentinel=new RegistrationSentinel(server);check(sentinel.observed&&sentinel.slots.length==29&&sentinel.passengerSeats.length==2,"hierarchy finishes after virtual registration");
  BoundingBoxDummyEntity bound=new BoundingBoxDummyEntity(server),remoteBound=new BoundingBoxDummyEntity(client);SeatDummyEntity seat=new SeatDummyEntity(server),remoteSeat=new SeatDummyEntity(client);
  DataParameter<Integer> occupied=key(EntityRailCarCargo.class,"OCCUPIED_SLOTS"),power=key(EntityRailCarElectric.class,"POWER"),boundTrain=key(BoundingBoxDummyEntity.class,"TRAIN_ID"),seatTrain=key(SeatDummyEntity.class,"TRAIN_ID"),seatIndex=key(SeatDummyEntity.class,"SEAT_INDEX");
  DataParameter<Float> width=key(BoundingBoxDummyEntity.class,"WIDTH"),height=key(BoundingBoxDummyEntity.class,"HEIGHT");
  check(occupied.getId()==6&&power.getId()==7,"Electric after actual Cargo, not legacy slot3");check(boundTrain.getId()==6&&width.getId()==7&&height.getId()==8,"independent bounds after native slots");check(seatTrain.getId()==6&&seatIndex.getId()==7,"seat does not inherit enclosing train metadata");
  for(DataParameter<Integer> k:Arrays.asList(occupied,power,boundTrain,seatTrain,seatIndex))check(k.getSerializer()==DataSerializers.VARINT,"native signed int serializer");check(width.getSerializer()==DataSerializers.FLOAT&&height.getSerializer()==DataSerializers.FLOAT,"native float serializers");
  allKeys(train,8);allKeys(remoteTrain,8);allKeys(trailer,7);allKeys(bound,9);allKeys(seat,8);
  check(train.slots.length==29&&trailer.slots.length==45,"actual inventory initializers");check(train.passengerSeats.length==2&&train.passengerSeats[0]==null,"actual Ridable constructor allocation");check(train.width==5F&&train.height==2F&&trailer.width==5F&&trailer.height==2F,"actual descendant constructor sizes");
  check(train.getPower()==0&&train.getSyncedOccupiedSlots()==0,"zero defaults before subclass allocation");check(bound.probeReadTRAIN_ID()==0&&bound.width==1F&&bound.height==1F,"bounds defaults");check(bound.getDataManager().get(width)==1F&&bound.getDataManager().get(height)==1F,"setSize after registration");check(seat.probeReadTRAIN_ID()==0&&seat.probeReadSEAT_INDEX()==0&&seat.width==0.5F&&seat.height==0.1F,"seat defaults");
  packet(train,remoteTrain,true);packet(bound,remoteBound,true);packet(seat,remoteSeat,true);check(packet(train,remoteTrain,false)==null,"full snapshot clears dirty entries");
  for(int v:new int[]{Integer.MIN_VALUE,-1,0,1,127,128,255,256,Integer.MAX_VALUE}){train.setPower(v);List<?> delta=packet(train,remoteTrain,false);check(delta!=null&&delta.size()==1,"power-only dirty delta");check(remoteTrain.getPower()==v,"signed VARINT range");train.getDataManager().setClean();train.setPower(v);check(!train.getDataManager().isDirty(),"equal power clean");}
  train.setEntityId(3001);BoundingBoxDummyEntity linked=new BoundingBoxDummyEntity(server,train,2.5F,3.75F);SeatDummyEntity linkedSeat=new SeatDummyEntity(server,train,1);
  check(linked.train==train&&linked.probeReadTRAIN_ID()==3001&&linked.width==2.5F&&linked.height==3.75F,"actual linked bounds constructor");check(linkedSeat.train==train&&linkedSeat.probeReadTRAIN_ID()==3001&&linkedSeat.probeReadSEAT_INDEX()==1,"actual linked seat constructor");
  packet(linked,remoteBound,true);packet(linkedSeat,remoteSeat,true);check(remoteBound.width==1F&&remoteBound.height==1F,"metadata alone leaves dimensions unchanged");remoteBound.onUpdate();check(remoteBound.width==2.5F&&remoteBound.height==3.75F,"client tick applies dimensions at original boundary");check(remoteBound.probeReadTRAIN_ID()==3001&&remoteSeat.probeReadTRAIN_ID()==3001&&remoteSeat.probeReadSEAT_INDEX()==1,"exact typed client reads");check(remoteBound.train==null&&remoteSeat.train==null,"metadata does not resolve ordinary train reference");
  linked.getDataManager().setClean();linked.setSize(2.5F,3.75F);check(!linked.getDataManager().isDirty(),"equal dimensions clean");linked.setSize(4.5F,3.75F);List<?> dims=packet(linked,remoteBound,false);check(dims!=null&&dims.size()==1,"width-only dirty delta");remoteBound.onUpdate();check(remoteBound.width==4.5F&&remoteBound.height==3.75F,"independent dimension delta");
  for(float value:new float[]{0F,-0F,Float.MIN_VALUE,-1F,Float.MAX_VALUE,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.NaN}){linked.getDataManager().set(width,value);linked.getDataManager().set(height,value);packet(linked,remoteBound,false);bits(remoteBound.getDataManager().get(width),value,"width float wire");bits(remoteBound.getDataManager().get(height),value,"height float wire");}
  linked.getDataManager().setClean();linked.getDataManager().set(width,Float.NaN);check(!linked.getDataManager().isDirty(),"equal boxed NaN clean");linked.getDataManager().set(width,0F);linked.getDataManager().setClean();linked.getDataManager().set(width,-0F);check(linked.getDataManager().isDirty(),"boxed signed zero distinction");
  for(int index:new int[]{-1,0,Integer.MAX_VALUE}){SeatDummyEntity d=new SeatDummyEntity(server,null,index);packet(d,remoteSeat,true);check(remoteSeat.probeReadSEAT_INDEX()==index,"raw seat index, no clamp");}
  linked.onUpdate();linkedSeat.onUpdate();check(!linked.isDead&&!linkedSeat.isDead,"linked server dummies alive");bound.onUpdate();seat.onUpdate();check(bound.isDead&&seat.isDead,"null-parent server dummies die");train.setDead();linked.onUpdate();linkedSeat.onUpdate();check(linked.isDead&&linkedSeat.isDead,"dead-parent server dummies die");check(!remoteBound.isDead&&!remoteSeat.isDead,"client unresolved reference stays alive");
  BoundingBoxDummyEntity nbtBounds=new BoundingBoxDummyEntity(server,remoteTrain,2F,4F);SeatDummyEntity nbtSeat=new SeatDummyEntity(server,remoteTrain,1);
  for(Entity e:Arrays.<Entity>asList(nbtBounds,nbtSeat)){NBTTagCompound n=new NBTTagCompound();check(!e.writeToNBTOptional(n)&&n.isEmpty(),"dummy refuses optional persistence");}
  NBTTagCompound bn=new NBTTagCompound(),sn=new NBTTagCompound();nbtBounds.writeEntityToNBT(bn);nbtSeat.writeEntityToNBT(sn);check(binary(bn).isEmpty()&&binary(sn).isEmpty(),"dummy write remains empty");nbtBounds.readEntityFromNBT(binary(bn));nbtSeat.readEntityFromNBT(binary(sn));check(nbtBounds.isDead&&nbtSeat.isDead,"dummy load self-destruction");
  TrainCargoTram saved=new TrainCargoTram(server),loaded=new TrainCargoTram(server);saved.setPower(765);net.minecraft.item.ItemStack cargoStack=new net.minecraft.item.ItemStack(net.minecraft.init.Items.DIAMOND,3);cargoStack.setTagCompound(new NBTTagCompound());cargoStack.getTagCompound().setString("train-test","retained");saved.setInventorySlotContents(0,cargoStack);NBTTagCompound n=new NBTTagCompound();saved.writeEntityToNBT(n);check(n.hasKey("Items",9)&&!n.hasKey("power")&&!n.hasKey("Power"),"Cargo NBT does not persist electric power");loaded.readEntityFromNBT(binary(n));check(loaded.getPower()==0,"fresh load power zero");check(loaded.getStackInSlot(0).getItem()==net.minecraft.init.Items.DIAMOND&&loaded.getStackInSlot(0).getCount()==3&&loaded.getStackInSlot(0).getTagCompound().getString("train-test").equals("retained"),"actual Cargo item binary NBT roundtrip");check(loaded.getSyncedOccupiedSlots()==1,"Cargo key independent of power");loaded.setPower(987);loaded.readEntityFromNBT(binary(n));check(loaded.getPower()==987,"existing live power unchanged by NBT load");
  check(saved.canAccelerate(),"original unconditional acceleration");saved.consumeFuel();check(saved.getPower()==765,"original no-op fuel consumption");
  System.out.println("PASS: "+checks+" native train metadata/constructor/packet/NBT assertions ("+args[0]+")");
 }
}
