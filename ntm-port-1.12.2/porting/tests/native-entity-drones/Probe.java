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
 public static void main(String[] args) throws Exception {
  EntityRequestDrone request=new EntityRequestDrone(null),rc=new EntityRequestDrone(null);EntityDeliveryDrone delivery=new EntityDeliveryDrone(null),dc=new EntityDeliveryDrone(null);
  DataParameter<Byte> appearance=key(EntityDroneBase.class,"APPEARANCE"),load=key(EntityDeliveryDrone.class,"LOAD");
  check(appearance.getSerializer()==DataSerializers.BYTE && load.getSerializer()==DataSerializers.BYTE,"native byte serializers");check(appearance.getId()<load.getId(),"inherited key allocated first");
  for(EntityDroneBase d:new EntityDroneBase[]{request,rc,delivery,dc}) {check(d.width==0.75F && d.height==0.75F,"actual constructor size");check(d.targetX==-1 && d.targetY==-1 && d.targetZ==-1,"original target field initialization");check(d.getAppearance()==0,"original signed byte0 default");Set<Integer> ids=new HashSet<>();for(EntityDataManager.DataEntry<?> e:d.getDataManager().getAll())check(ids.add(e.getKey().getId()),"no duplicate vanilla/inherited keys");check(ids.size()==(d instanceof EntityRequestDrone?7:8),"exact key ownership");}
  check(request.program.isEmpty() && request.nextActionTimer==0 && request.heldItem==null,"original request field initialization");check(delivery.slots.length==18 && delivery.slots[0]==null && !delivery.chunkLoading,"actual delivery inventory allocation, not ItemStack.EMPTY conversion");
  packet(request,rc,true);packet(delivery,dc,true);check(dc.getLoad()==0,"full packet load default");
  for(int value:new int[]{-129,-128,-1,0,1,127,128,255,256}) {
   request.setAppearance(value);delivery.setAppearance(value);delivery.setLoad((byte)value);packet(request,rc,false);packet(delivery,dc,false);
   check(rc.getAppearance()==(byte)value && dc.getAppearance()==(byte)value && dc.getLoad()==(byte)value,"signed narrowing and widening exactly preserved");check(dc.getSpeed()==((byte)value==1?0.375*3:0.375),"only load byte1 is express, original formula");
   check(Consumers.expressTexture(dc)==((byte)value==1) && !Consumers.expressTexture(rc),"shared renderer request guard excludes delivery-only key");
  }
  delivery.getDataManager().setClean();delivery.setLoad((byte)0);check(!delivery.getDataManager().isDirty(),"equal byte set clean");Consumers.express(delivery);packet(delivery,dc,false);check(dc.getLoad()==1 && dc.getSpeed()==1.125,"actual ItemDrone typed setter");
  request.setTarget(10,20,30);delivery.setTarget(-10,-20,-30);request.setAppearance(255);delivery.setAppearance(128);delivery.setLoad((byte)-7);
  NBTTagCompound rn=new NBTTagCompound(),dn=new NBTTagCompound();request.writeScalarCalls(rn);delivery.writeScalarCalls(dn);rc.readScalarCalls(binary(rn));dc.readScalarCalls(binary(dn));
  check(rc.targetX==10 && rc.targetY==21 && rc.targetZ==30 && rc.getAppearance()==-1,"complete base NBT plus duplicate request app statement");check(dc.targetX==-10 && dc.targetY==-20 && dc.targetZ==-30 && dc.getAppearance()==-128 && dc.getLoad()==-7,"complete base NBT plus delivery scalar load statement");
  check(!rn.hasKey("held") && !dn.hasKey("Items") && !dn.hasKey("chunkLoading"),"bounded helpers do not claim payload/ticket persistence");
  rc.readScalarCalls(new NBTTagCompound());dc.readScalarCalls(new NBTTagCompound());check(rc.targetY==21 && dc.targetY==-20,"missing tY retains live targets");check(rc.getAppearance()==0 && dc.getAppearance()==0 && dc.getLoad()==0,"missing scalar tags reset byte defaults");
  EntityRequestDrone fresh=new EntityRequestDrone(null);fresh.readScalarCalls(new NBTTagCompound());check(fresh.targetX==-1 && fresh.targetY==-1 && fresh.targetZ==-1,"fresh missing targets retain -1");
  System.out.println("PASS: "+checks+" native drone constructor/inheritance/byte/consumer/bounded-NBT assertions");
 }
}
