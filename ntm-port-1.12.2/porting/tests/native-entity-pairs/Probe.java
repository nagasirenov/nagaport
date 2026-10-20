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
 static void defaults(Entity a,Entity b,int count) throws Exception {
  Set<Integer> ids=new HashSet<>();for(EntityDataManager.DataEntry<?> e:a.getDataManager().getAll())check(ids.add(e.getKey().getId()),"unique native inheritance allocation");check(ids.size()==count,"exact native entries");packet(a,b,true);
  for(Field f:a.getClass().getDeclaredFields()) if(f.getType()==DataParameter.class) {DataParameter<?> k=key(a.getClass(),f.getName());Object v=a.getDataManager().get(k);check(v.equals(b.getDataManager().get(k)),"default/full packet");check(v.equals(0)||v.equals(0F),"original zero default");}
 }
 public static void main(String[] args) throws Exception {
  EntityCog cog=new EntityCog(null),cc=new EntityCog(null);EntitySawblade saw=new EntitySawblade(null),sc=new EntitySawblade(null);EntityMist mist=new EntityMist(null),mc=new EntityMist(null);EntityDisperserCanister can=new EntityDisperserCanister(null),dc=new EntityDisperserCanister(null);
  defaults(cog,cc,9);defaults(saw,sc,9);defaults(mist,mc,9);defaults(can,dc,8);
  check(cog.width==1F && cog.height==1F && saw.width==1F && saw.height==1F,"real World constructors retain size side effects");check(mist.noClip && mist.maxAge==150,"real Mist constructor and field initializer");
  check(Probe.<Byte>key(EntityThrowableNT.class,"STUCK_FACE").getId()<Probe.<Integer>key(EntityCog.class,"ORIENTATION").getId(),"base key allocated before child");
  cog.setStuckIn(5);saw.setStuckIn(3);
  for(int v:new int[]{0,-1,6,Integer.MIN_VALUE,Integer.MAX_VALUE}) {
   cog.setOrientation(v).setMeta(~v);saw.setOrientation(v);packet(cog,cc,false);packet(saw,sc,false);
   check(Consumers.CoggetOrientation(cc)==v && Consumers.CoggetMeta(cc)==~v && Consumers.SawbladegetOrientation(sc)==v,"exact typed renderer reads");check(cc.getStuckIn()==5 && sc.getStuckIn()==3,"inherited byte preserved with child integer update");
   NBTTagCompound n=new NBTTagCompound();cog.writeScalarTail(n);cc.readScalarTail(binary(n));check(cc.getOrientation()==v && cc.getMeta()==~v,"cog exact child scalar NBT tail");
   n=new NBTTagCompound();saw.writeScalarTail(n);sc.readScalarTail(binary(n));check(sc.getOrientation()==v && !n.hasKey("meta"),"saw saves only rot, not unused meta slot");
   cog.fold();saw.fold();check(cog.getOrientation()==v+6 && saw.getOrientation()==v+6,"original fold addition including overflow");cog.unfold();saw.unfold();check(cog.getOrientation()==v && saw.getOrientation()==v,"original unfold subtraction");
   cog.getDataManager().setClean();cog.setOrientation(v);check(!cog.getDataManager().isDirty(),"equal int setter clean");
   can.setFluid(v).setType(~v);packet(can,dc,false);check(dc.getDataManager().get(Probe.<Integer>key(EntityDisperserCanister.class,"FLUID_TYPE"))==v && dc.getDataManager().get(Probe.<Integer>key(EntityDisperserCanister.class,"ITEM_TYPE"))==~v,"canister two scalar channels");
   n=new NBTTagCompound();can.writeEntityToNBT(n);dc.readEntityFromNBT(binary(n));check(n.getInteger("fluid")==v && n.getInteger("item")==~v && n.hasKey("xTile"),"complete canister NBT uses actual vanilla Throwable parent");
  }
  cc.readScalarTail(new NBTTagCompound());sc.readScalarTail(new NBTTagCompound());check(cc.getOrientation()==0 && cc.getMeta()==0 && sc.getOrientation()==0,"missing child NBT zero defaults");
  dc.readEntityFromNBT(new NBTTagCompound());check(dc.getDataManager().get(Probe.<Integer>key(EntityDisperserCanister.class,"FLUID_TYPE"))==0,"missing canister NBT default");
  DataParameter<Float> width=key(EntityMist.class,"AREA_WIDTH"),height=key(EntityMist.class,"AREA_HEIGHT");
  check(width.getSerializer()==DataSerializers.FLOAT && Probe.<Integer>key(EntityMist.class,"FLUID_TYPE").getSerializer()==DataSerializers.VARINT,"Mist native serializers");
  for(float v:new float[]{0F,-0F,-2F,Float.POSITIVE_INFINITY,Float.intBitsToFloat(0x7fc00042)}) {
   mist.setArea(v,-v).setDuration(80);packet(mist,mc,false);check(Float.floatToRawIntBits(mc.getDataManager().get(width))==Float.floatToRawIntBits(v),"Mist native metadata float raw bits");
   NBTTagCompound n=new NBTTagCompound();mist.writeArea(n);mc.readArea(binary(n));check(Float.floatToIntBits(mc.getDataManager().get(width))==Float.floatToIntBits(v) && Float.floatToIntBits(mc.getDataManager().get(height))==Float.floatToIntBits(-v),"exact area statements/native binary NBT");
   check(mc.maxAge==150 && mist.maxAge==80 && !n.hasKey("maxAge"),"duration remains unsynchronized and unsaved");
  }
  mist.getDataManager().setClean();mist.setArea(Float.NaN,Float.NaN);check(!mist.getDataManager().isDirty(),"boxed NaN equality stays clean");
  mc.readArea(new NBTTagCompound());check(mc.getDataManager().get(width)==0F && mc.getDataManager().get(height)==0F,"missing area tags zero");
  System.out.println("PASS: "+checks+" native constructor/inheritance/metadata/dirty/bounded-NBT assertions");
 }
}
