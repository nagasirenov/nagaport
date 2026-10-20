import net.minecraft.entity.Entity;
import net.minecraft.nbt.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.*;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.*;
import java.util.*;

public class Probe {
 static int checks;
 interface Action {void run() throws Exception;}
 static void check(boolean b,String why) {checks++;if(!b)throw new AssertionError(why);}
 static void expect(Class<? extends Throwable> type,Action a,String why) throws Exception {checks++;try{a.run();}catch(Throwable t){if(type.isInstance(t))return;throw new AssertionError(why+": wrong exception",t);}throw new AssertionError(why+": missing exception");}
 @SuppressWarnings("unchecked") static <T> DataParameter<T> key(Class<?> c,String n) throws Exception {Field f=c.getDeclaredField(n);f.setAccessible(true);return (DataParameter<T>)f.get(null);}
 static void packet(Entity from,Entity to,boolean full,int entries) throws Exception {
  PacketBuffer wire=new PacketBuffer(Unpooled.buffer());new SPacketEntityMetadata(from.getEntityId(),from.getDataManager(),full).writePacketData(wire);
  SPacketEntityMetadata read=new SPacketEntityMetadata();read.readPacketData(wire);
  check(wire.readableBytes()==0,"real PacketBuffer fully consumed");check(read.getEntityId()==from.getEntityId(),"packet entity ID preserved");
  List<EntityDataManager.DataEntry<?>> list=read.getDataManagerEntries();check((list==null?0:list.size())==entries,"exact full/dirty entry count");
  if(list!=null)to.getDataManager().setEntryValues(list);wire.release();
 }
 static NBTTagCompound binary(NBTTagCompound n) throws Exception {java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();CompressedStreamTools.write(n,new java.io.DataOutputStream(bytes));return CompressedStreamTools.read(new java.io.DataInputStream(new java.io.ByteArrayInputStream(bytes.toByteArray())));}
 static void set(Entity e,int value) throws Exception {
  if(e instanceof EntityArtilleryRocket)check(((EntityArtilleryRocket)e).setType(value)==e,"rocket fluent setter returns same object");
  else if(e instanceof EntityArtilleryShell)check(((EntityArtilleryShell)e).setType(value)==e,"shell fluent setter returns same object");
  else {BulletConfig carrier=new BulletConfig();BulletConfig.configs.remove(carrier);carrier.id=value;if(e instanceof EntityBulletBaseMK4)((EntityBulletBaseMK4)e).setBulletConfig(carrier);else ((EntityBulletBeamBase)e).setBulletConfig(carrier);}
 }
 static void metadata(Entity a,Entity b,Class<?> owner,String field,int count) throws Exception {
  DataParameter<Integer> k=key(owner,field);check(k.getSerializer()==DataSerializers.VARINT,"owner uses native VARINT");check(k.getId()>5,"owner key above native Entity slots");check(a.getDataManager().get(k)==0 && b.getDataManager().get(k)==0,"original integer zero default");
  Set<Integer> ids=new HashSet<>();for(EntityDataManager.DataEntry<?> e:a.getDataManager().getAll())check(ids.add(e.getKey().getId()),"unique registered key");check(ids.size()==count,"exact registration count including omitted rocket parent");
  packet(a,b,true,count);a.getDataManager().setClean();set(a,0);check(!a.getDataManager().isDirty(),"equal default is not dirty");packet(a,b,false,0);
  for(int v:new int[]{1,-1,127,128,255,256,65535,Integer.MAX_VALUE,Integer.MIN_VALUE,0}) {
   set(a,v);check(a.getDataManager().isDirty(),"changed scalar marks dirty");packet(a,b,false,1);check(b.getDataManager().get(k)==v,"signed integer unchanged through real native packet");check(!a.getDataManager().isDirty(),"packet dirty extraction clears manager");check(a.getDataManager().getDirty()==null,"second extraction empty");
   set(a,v);check(!a.getDataManager().isDirty(),"equal scalar remains clean");
  }
  List<EntityDataManager.DataEntry<?>> snapshot=a.getDataManager().getAll();set(a,99);for(EntityDataManager.DataEntry<?> e:snapshot)if(e.getKey().getId()==k.getId())check(e.getValue().equals(0),"old scalar snapshot does not alias replacement");packet(a,b,true,count);check(b.getDataManager().get(k)==99,"full snapshot after mutations");
  a.getDataManager().setClean();a.getDataManager().setDirty(k);packet(a,b,false,1);check(b.getDataManager().get(k)==99,"native explicit force-dirty preserved");
 }
 static void sedna() throws Exception {
  BulletConfig.configs.clear();BulletConfig zero=new BulletConfig(),one=new BulletConfig();check(zero.id==0&&one.id==1,"exact source registry constructor assigns sequential IDs");
  for(boolean beam:new boolean[]{false,true}) {
   final Entity e=beam?new EntityBulletBeamBase(null):new EntityBulletBaseMK4(null);final DataParameter<Integer> k=key(beam?EntityBulletBeamBase.class:EntityBulletBaseMK4.class,"BULLET_CONFIG");
   set(e,0);BulletConfig got=beam?((EntityBulletBeamBase)e).getBulletConfig():((EntityBulletBaseMK4)e).getBulletConfig();check(got==zero,"registry0 lookup");
   set(e,1);got=beam?((EntityBulletBeamBase)e).getBulletConfig():((EntityBulletBaseMK4)e).getBulletConfig();check(got==one,"registry1 lookup");
   for(int v:new int[]{-1,Integer.MIN_VALUE,3,Integer.MAX_VALUE}) {set(e,v);got=beam?((EntityBulletBeamBase)e).getBulletConfig():((EntityBulletBaseMK4)e).getBulletConfig();check(got==null,"original invalid ID returns null without new clamp");}
   set(e,2);expect(IndexOutOfBoundsException.class,()->{if(beam)((EntityBulletBeamBase)e).getBulletConfig();else ((EntityBulletBaseMK4)e).getBulletConfig();},"original id==size off-by-one remains");
   set(e,1);e.getDataManager().setClean();expect(NullPointerException.class,()->{if(beam)((EntityBulletBeamBase)e).setBulletConfig(null);else ((EntityBulletBaseMK4)e).setBulletConfig(null);},"null config assigns field before original dereference");check(e.getDataManager().get(k)==1&&!e.getDataManager().isDirty(),"null setter leaves metadata untouched");
   if(beam)check(((EntityBulletBeamBase)e).config==null,"beam config assignment precedes NPE");else check(((EntityBulletBaseMK4)e).config==null,"bullet config assignment precedes NPE");
   if(beam)((EntityBulletBeamBase)e).resolveConfigSlice();else ((EntityBulletBaseMK4)e).resolveConfigSlice();check((beam?((EntityBulletBeamBase)e).config:((EntityBulletBaseMK4)e).config)==one,"exact lazy read resolves only null cache");
   e.getDataManager().set(k,0);if(beam)((EntityBulletBeamBase)e).resolveConfigSlice();else ((EntityBulletBaseMK4)e).resolveConfigSlice();check((beam?((EntityBulletBeamBase)e).config:((EntityBulletBaseMK4)e).config)==one,"metadata change does not silently refresh populated cache");
  }
  EntityBulletBeamBase beam=new EntityBulletBeamBase(null),client=new EntityBulletBeamBase(null);beam.setBulletConfig(one);packet(beam,client,true,7);check(client.config==null,"metadata packet alone does not populate client config field");client.resolveConfigSlice();check(client.config==one,"client lazy read uses synchronized ID");
  NBTTagCompound out=new NBTTagCompound();out.setInteger("sentinel",5);beam.writeEntityToNBT(out);check(out.getKeySet().size()==1&&!out.hasKey("type")&&!out.hasKey("config"),"beam persists neither config nor new data");check(!beam.writeToNBTOptional(out),"beam refuses optional save");beam.readEntityFromNBT(binary(out));check(beam.isDead,"beam original transient load dies");
  beam.beamLength=-123.5;beam.rotationYaw=45.125F;beam.rotationPitch=-19.75F;ByteBuf buf=Unpooled.buffer();beam.writeSpawnData(buf);check(buf.readableBytes()==16,"unchanged beam extra spawn layout");client.readSpawnData(buf);check(client.beamLength==beam.beamLength&&client.rotationYaw==beam.rotationYaw&&client.rotationPitch==beam.rotationPitch&&buf.readableBytes()==0,"complete beam extra spawn byte roundtrip");buf.release();
  EntityBulletBaseMK4 bullet=new EntityBulletBaseMK4(null);buf=Unpooled.buffer();bullet.writeSpawnData(buf);check(buf.readableBytes()==4&&buf.readInt()==-1,"unchanged null-thrower spawn sentinel");buf.release();
 }
 static void artillery() throws Exception {
  EntityArtilleryRocket rocket=new EntityArtilleryRocket(null),other=new EntityArtilleryRocket(null);EntityArtilleryShell shell=new EntityArtilleryShell(null),sc=new EntityArtilleryShell(null);
  for(int v:new int[]{0,1,-1,2,Integer.MIN_VALUE,Integer.MAX_VALUE}) {
   rocket.setType(v);shell.setType(v);check(rocket.getType()==ItemAmmoHIMARS.itemTypes[v==1?1:0]&&shell.getType()==ItemAmmoArty.itemTypes[v==1?1:0],"original catch fallback selection");check(rocket.getTypeId()==v,"raw accessor never normalizes malformed ID");
   if(v==0||v==1)check(TextureConsumer.getEntityTexture(rocket)==ItemAmmoHIMARS.itemTypes[v].texture,"exact texture consumer raw read");else expect(ArrayIndexOutOfBoundsException.class,()->TextureConsumer.getEntityTexture(rocket),"renderer retains raw invalid-index exception, not domain fallback");
  }
  HIMARSRocket keep=ItemAmmoHIMARS.itemTypes[1];ItemAmmoHIMARS.itemTypes[1]=null;rocket.setType(1);check(rocket.getType()==null,"null array entry is not replaced");expect(NullPointerException.class,()->TextureConsumer.getEntityTexture(rocket),"null renderer entry still fails");ItemAmmoHIMARS.itemTypes[1]=keep;
  rocket.setType(Integer.MIN_VALUE);rocket.setPosition(1.25,-5.5,99.75);check(rocket.lastTargetPos==null,"new rocket target defaults null in metadata model");NBTTagCompound save=new NBTTagCompound();rocket.writeScalarTail(save);check(rocket.lastTargetPos.x==1.25&&rocket.lastTargetPos.y==-5.5&&rocket.lastTargetPos.z==99.75,"original save mutates null target to current position");other.readScalarTail(binary(save));check(other.getTypeId()==Integer.MIN_VALUE&&other.lastTargetPos.x==1.25&&other.lastTargetPos.y==-5.5&&other.lastTargetPos.z==99.75,"complete rocket tail uses native binary NBT");check(save.getKeySet().equals(new HashSet<String>(Arrays.asList("type","targetX","targetY","targetZ"))),"rocket tail exact tag set, no configuration extras");
  other.readScalarTail(new NBTTagCompound());check(other.getTypeId()==0&&other.lastTargetPos.x==0&&other.lastTargetPos.y==0&&other.lastTargetPos.z==0,"missing rocket tags original zero defaults");
  shell.setType(Integer.MAX_VALUE);shell.setTarget(-1.25,300.5,123456.75);shell.setWhistle(true);save=new NBTTagCompound();shell.writeScalarTail(save);save.setBoolean("didWhistle",true);sc.readScalarTail(binary(save));check(sc.getDataManager().get(key(EntityArtilleryShell.class,"AMMO_TYPE")).equals(Integer.MAX_VALUE)&&Arrays.equals(sc.getTarget(),new double[]{-1.25,300.5,123456.75})&&sc.getWhistle()&&sc.didWhistle(),"shell scalar prefix native binary roundtrip");check(save.getKeySet().equals(new HashSet<String>(Arrays.asList("type","targetX","targetY","targetZ","shouldWhistle","didWhistle"))),"shell selected scalar prefix exact tags, cargo excluded");
  sc.readScalarTail(new NBTTagCompound());check(sc.getDataManager().get(key(EntityArtilleryShell.class,"AMMO_TYPE")).equals(0)&&Arrays.equals(sc.getTarget(),new double[]{0,0,0})&&!sc.getWhistle()&&!sc.didWhistle(),"missing shell scalar tags reset original defaults");
  rocket.getDataManager().setClean();save=new NBTTagCompound();save.setInteger("type",257);rocket.readScalarTail(save);check(rocket.getDataManager().isDirty(),"NBT type load triggers native dirty state");packet(rocket,other,false,1);check(other.getTypeId()==257,"loaded type reaches receiving manager");
 }
 public static void main(String[] args) throws Exception {
  if(args[0].equals("artillery-first")){new EntityArtilleryShell(null);new EntityArtilleryRocket(null);}else{new EntityBulletBeamBase(null);new EntityBulletBaseMK4CL(null);}
  EntityBulletBaseMK4 mk=new EntityBulletBaseMK4(null),mc=new EntityBulletBaseMK4(null);EntityBulletBaseMK4CL cl=new EntityBulletBaseMK4CL(null),cc=new EntityBulletBaseMK4CL(null);EntityBulletBeamBase beam=new EntityBulletBeamBase(null),bc=new EntityBulletBeamBase(null);EntityArtilleryRocket rocket=new EntityArtilleryRocket(null),rc=new EntityArtilleryRocket(null);EntityArtilleryShell shell=new EntityArtilleryShell(null),sc=new EntityArtilleryShell(null);
  DataParameter<Byte> stuck=key(EntityThrowableNT.class,"STUCK_FACE");DataParameter<Integer> m=key(EntityBulletBaseMK4.class,"BULLET_CONFIG"),b=key(EntityBulletBeamBase.class,"BULLET_CONFIG"),r=key(EntityArtilleryRocket.class,"AMMO_TYPE"),s=key(EntityArtilleryShell.class,"AMMO_TYPE");
  check(m!=b&&m!=r&&m!=s&&b!=r&&b!=s&&r!=s,"four independent true-owner key objects");check(m.getId()==stuck.getId()+1&&r.getId()==stuck.getId()+1&&s.getId()==stuck.getId()+1,"siblings independently allocate after inherited static key");check(b.getId()==stuck.getId(),"direct Entity beam sibling does not borrow throwable key");
  check(mk.getStuckIn()==0&&shell.getStuckIn()==0,"MK4 and Shell retain super registration");check(rocket.getDataManager().getAll().size()==7,"Rocket omitted super remains omitted");
  for(Field f:EntityBulletBaseMK4CL.class.getDeclaredFields())check(f.getType()!=DataParameter.class,"CL does not shadow parent key");
  check(shell.width==0.5F&&shell.height==0.5F,"complete shell World constructor retains size after metadata init");
  metadata(mk,mc,EntityBulletBaseMK4.class,"BULLET_CONFIG",8);metadata(cl,cc,EntityBulletBaseMK4.class,"BULLET_CONFIG",8);metadata(beam,bc,EntityBulletBeamBase.class,"BULLET_CONFIG",7);metadata(rocket,rc,EntityArtilleryRocket.class,"AMMO_TYPE",7);metadata(shell,sc,EntityArtilleryShell.class,"AMMO_TYPE",8);
  sedna();artillery();check(AssumedTicketBoundary.returns>0,"explicitly assumed ticket boundary exercised");
  System.out.println("PASS: "+checks+" native projectile-config assertions, "+args[0]+"; ticket, gameplay, full constructors/NBT excluded as documented");
 }
}
class AssumedTicketBoundary {
 static int returns;
 static void recordReturn(Entity e) {
  returns++;int expected=e instanceof EntityArtilleryRocket?6:e instanceof EntityArtilleryShell?7:8;
  if(e.getDataManager().getAll().size()!=expected)throw new AssertionError("Original super/key/ticket ordering changed");
  if(e instanceof EntityBulletBaseMK4CL&&((EntityBulletBaseMK4CL)e).config!=null)throw new AssertionError("Expected constructor-time config null before fields initialize");
 }
}
