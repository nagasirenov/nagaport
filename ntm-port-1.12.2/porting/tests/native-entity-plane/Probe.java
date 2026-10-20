package com.hbm.entity.logic;

import java.lang.reflect.*;
import java.util.*;
import com.google.common.collect.*;
import com.hbm.config.GeneralConfig;
import com.hbm.main.MainRegistry;
import com.hbm.render.entity.item.RendererLookup;
import com.hbm.sound.AudioWrapper;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.*;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.common.*;
import io.netty.buffer.Unpooled;

public class Probe {
 static int checks;
 static void check(boolean value,String reason){checks++;if(!value)throw new AssertionError(reason);}
 static int bits(float value){return Float.floatToRawIntBits(value);}
 static void put(Class<?> type,Object target,String name,Object value)throws Exception {Field f=type.getDeclaredField(name);f.setAccessible(true);f.set(target,value);}
 static Object get(Class<?> type,Object target,String name)throws Exception {Field f=type.getDeclaredField(name);f.setAccessible(true);return f.get(target);}
 @SuppressWarnings("unchecked") static <T> DataParameter<T> key(Class<?> owner,String name)throws Exception{return (DataParameter<T>)get(owner,null,name);}
 static class W extends World {
  W(boolean remote){super(new net.minecraft.world.storage.SaveHandlerMP(),new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new net.minecraft.profiler.Profiler(),remote);}
  protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
  protected boolean isChunkLoaded(int x,int z,boolean allow){throw new AssertionError("Loaded chunks outside probe scope");}
 }
 /** Only test inputs for genuine Forge. No overridden or shadowed Forge classes. */
 @SuppressWarnings("unchecked") static void configureQuotaZero(W... worlds)throws Exception {
  Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);sun.misc.Unsafe unsafe=(sun.misc.Unsafe)uf.get(null);
  Loader loader=(Loader)unsafe.allocateInstance(Loader.class);
  LoadController controller=(LoadController)unsafe.allocateInstance(LoadController.class);
  ModMetadata metadata=new ModMetadata();metadata.modId="native_plane_probe";metadata.name="Native Plane Probe";
  DummyModContainer container=new DummyModContainer(metadata);
  BiMap<ModContainer,Object> map=HashBiMap.create();map.put(container,MainRegistry.instance);
  put(LoadController.class,controller,"modObjectList",map);put(Loader.class,loader,"modController",controller);put(Loader.class,null,"instance",loader);
  ForgeChunkManager.setForcedChunkLoadingCallback(MainRegistry.instance,new ForgeChunkManager.LoadingCallback(){public void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets,World world){throw new AssertionError("No ticket loads authorized in bounded probe");}});
  put(ForgeChunkManager.class,null,"defaultMaxCount",0);
  Map<World,Multimap<String,ForgeChunkManager.Ticket>> tickets=(Map<World,Multimap<String,ForgeChunkManager.Ticket>>)get(ForgeChunkManager.class,null,"tickets");
  for(W world:worlds)tickets.put(world,ArrayListMultimap.<String,ForgeChunkManager.Ticket>create());
 }
 static class Observed extends EntityBomber {
  static int initializations;static boolean beforeOwnFields,beforePlaneRegistration,nullTicket;
  Observed(World world){super(world);}
  public float getMaxHealth(){return 75F;}public int getLifetime(){return 300;}
  public void init(ForgeChunkManager.Ticket ticket){
   initializations++;beforeOwnFields=health==0F&&timer==0&&bombStart==0;
   beforePlaneRegistration=true;for(EntityDataManager.DataEntry<?> e:getDataManager().getAll())if(e.getKey().getId()==PLANE_HEALTH.getId())beforePlaneRegistration=false;
   nullTicket=ticket==null;super.init(ticket);
  }
 }
 static List<EntityDataManager.DataEntry<?>> packet(Entity from,Entity to,boolean full,int count)throws Exception {
  PacketBuffer buffer=new PacketBuffer(Unpooled.buffer());new SPacketEntityMetadata(from.getEntityId(),from.getDataManager(),full).writePacketData(buffer);
  SPacketEntityMetadata read=new SPacketEntityMetadata();read.readPacketData(buffer);
  check(read.getEntityId()==from.getEntityId(),"packet entity ID");check(buffer.readableBytes()==0,"packet fully consumed");
  List<EntityDataManager.DataEntry<?>> entries=read.getDataManagerEntries();check((entries==null?0:entries.size())==count,"exact native entry count");
  if(entries!=null)to.getDataManager().setEntryValues(entries);buffer.release();return entries;
 }
 static NBTTagCompound binary(NBTTagCompound n)throws Exception {
  java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();CompressedStreamTools.write(n,new java.io.DataOutputStream(out));
  return CompressedStreamTools.read(new java.io.DataInputStream(new java.io.ByteArrayInputStream(out.toByteArray())));
 }
 static void layout(Entity e,int expected){Set<Integer> ids=new HashSet<>();for(EntityDataManager.DataEntry<?> entry:e.getDataManager().getAll())check(ids.add(entry.getKey().getId()),"unique native inherited key");check(ids.size()==expected,"complete native key count");}
 static void floatPackets(EntityPlaneBase server,EntityPlaneBase client)throws Exception {
  DataParameter<Float> health=EntityPlaneBase.PLANE_HEALTH;
  server.getDataManager().setClean();server.health=50F;server.synchronizeHealthPrefix();check(!server.getDataManager().isDirty(),"equal default float clean");
  float[] values={0F,-0F,Float.MIN_VALUE,-Float.MIN_VALUE,Float.MAX_VALUE,-Float.MAX_VALUE,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.intBitsToFloat(0x7fc01234)};
  for(float value:values){
   server.health=value;server.synchronizeHealthPrefix();check(server.getDataManager().isDirty(),"changed health marks native dirty");
   packet(server,client,false,1);check(bits(client.getDataManager().get(health))==bits(value),"FLOAT packet raw bits preserved");
   client.health=333F;client.synchronizeHealthPrefix();check(bits(client.health)==bits(value),"actual client health prefix");
   check(!server.getDataManager().isDirty()&&server.getDataManager().getDirty()==null,"native dirty extraction resets");
   server.synchronizeHealthPrefix();check(!server.getDataManager().isDirty(),"equal value remains clean");
  }
  server.health=Float.intBitsToFloat(0x7fc05678);server.synchronizeHealthPrefix();check(!server.getDataManager().isDirty(),"Float.equals treats different NaN payloads as equal");check(bits(server.getDataManager().get(health))==0x7fc01234,"unchanged NaN retains first raw bits");
  List<EntityDataManager.DataEntry<?>> snapshot=server.getDataManager().getAll();server.health=-17.25F;server.synchronizeHealthPrefix();
  for(EntityDataManager.DataEntry<?> e:snapshot)if(e.getKey().getId()==health.getId())check(bits((Float)e.getValue())==0x7fc01234,"immutable float snapshot unchanged");
  packet(server,client,false,1);server.getDataManager().setClean();server.getDataManager().setDirty(health);packet(server,client,false,1);check(client.getDataManager().get(health)==-17.25F,"native forced-dirty characterization only");
 }
 static void bytePackets(EntityBomber a,EntityBomber b,DataParameter<Byte> style)throws Exception {
  a.getDataManager().setClean();
  for(int i=-128;i<=127;i++){
   a.getDataManager().set(style,(byte)i);check(a.getBomberStyle()==(byte)i&&RendererLookup.style(a)==i,"actual getter and renderer sign extension");
   packet(a,b,false,1);check(b.getBomberStyle()==(byte)i&&RendererLookup.style(b)==i,"all 256 native BYTE values roundtrip");
   a.getDataManager().set(style,(byte)i);check(!a.getDataManager().isDirty(),"equal byte remains clean");
  }
  List<EntityDataManager.DataEntry<?>> snapshot=a.getDataManager().getAll();a.getDataManager().set(style,(byte)-1);
  for(EntityDataManager.DataEntry<?> e:snapshot)if(e.getKey().getId()==style.getId())check(((Byte)e.getValue())==127,"byte snapshot unchanged");
 }
 static void nbt(W server,W client,DataParameter<Byte> style)throws Exception {
  EntityBomber source=new EntityBomber(server);source.ticksExisted=-123;source.timer=999;source.health=91F;source.getDataManager().set(EntityPlaneBase.PLANE_HEALTH,-7.25F);
  source.bombStart=Integer.MIN_VALUE;source.bombStop=Integer.MAX_VALUE;source.bombRate=-13;source.type=514;source.getDataManager().set(style,(byte)-128);
  NBTTagCompound n=new NBTTagCompound();n.setString("sentinel","keep");source.writeEntityToNBT(n);n=binary(n);
  check(n.getFloat("health")==-7.25F,"writer persists synchronized health, not local field");
  check(n.getTagId("health")==5&&n.getTagId("style")==1&&n.getTagId("ticksExisted")==3,"exact original NBT tag types");
  check(n.getKeySet().equals(new HashSet<String>(Arrays.asList("sentinel","ticksExisted","health","bombStart","bombStop","bombRate","type","style"))),"no new tags or newly saved timer");
  for(W world:new W[]{server,client}){
   EntityBomber loaded=new EntityBomber(world);loaded.readEntityFromNBT(n);
   check(loaded.health==50F&&loaded.timer==200,"read leaves local health and unsaved timer unchanged");check(loaded.getDataManager().get(EntityPlaneBase.PLANE_HEALTH)==-7.25F,"read only restores synchronized health");
   check(loaded.ticksExisted==-123&&loaded.bombStart==Integer.MIN_VALUE&&loaded.bombStop==Integer.MAX_VALUE&&loaded.bombRate==-13&&loaded.type==514&&loaded.getBomberStyle()==-128,"binary full bomber NBT values");
   loaded.synchronizeHealthPrefix();check(loaded.health==(world.isRemote?-7.25F:50F)&&loaded.getDataManager().get(EntityPlaneBase.PLANE_HEALTH)==(world.isRemote?-7.25F:50F),"original first-tick server overwrite/client-read behavior retained");
   loaded.readEntityFromNBT(new NBTTagCompound());check(loaded.ticksExisted==0&&loaded.bombStart==0&&loaded.bombStop==0&&loaded.bombRate==0&&loaded.type==0&&loaded.getBomberStyle()==0&&loaded.getDataManager().get(EntityPlaneBase.PLANE_HEALTH)==0F,"missing NBT defaults retained without fallback repair");
  }
  for(int payload:new int[]{Integer.MIN_VALUE,-3,-2,-1,0,1,2,3,Integer.MAX_VALUE}){
   EntityC130 c130=new EntityC130(client);NBTTagCompound data=binary(n.copy());data.setInteger("payload",payload);c130.readEntityFromNBT(binary(data));
   check(c130.payload.ordinal()==Math.abs(payload%3),"actual unchanged EnumUtil payload normalization");
   NBTTagCompound saved=new NBTTagCompound();c130.writeEntityToNBT(saved);saved=binary(saved);check(saved.getInteger("payload")==Math.abs(payload%3)&&saved.getFloat("health")==-7.25F&&saved.getInteger("ticksExisted")==-123,"C130 native superclass/own binary NBT");
  }
 }
 static void audio(W server,W client,DataParameter<Byte> style){
  EntityBomber bomber=new EntityBomber(client);EntityC130 c130=new EntityC130(client);
  for(float health:new float[]{50F,0F,-1F,Float.NaN,Float.POSITIVE_INFINITY})for(int value:new int[]{-128,0,4,5,8,9,10,127}){
   bomber.audio=null;bomber.getDataManager().set(EntityPlaneBase.PLANE_HEALTH,health);bomber.getDataManager().set(style,(byte)value);int calls=MainRegistry.proxy.requests;bomber.audioConsumerBlock();
   check(MainRegistry.proxy.requests==calls+(health>0?1:0),"audio gate uses synchronized float comparison");
   if(health>0){AudioWrapper playing=bomber.audio;check(playing.sound.equals(value>=9?"hbm:entity.jetloopbomb":value<=4?"hbm:entity.bomberSmallLoop":"hbm:entity.bomberLoop"),"unchanged style audio selection");check(playing.starts==1&&playing.keeps==1&&playing.positions==1,"exact audio block order/count");bomber.audioConsumerBlock();check(playing.starts==1&&playing.keeps==2&&playing.positions==2,"playing loop reused");bomber.getDataManager().set(EntityPlaneBase.PLANE_HEALTH,0F);bomber.audioConsumerBlock();check(playing.stops==1&&bomber.audio==null,"dead health stops audio");}
  }
  c130.getDataManager().set(EntityPlaneBase.PLANE_HEALTH,1F);c130.audioConsumerBlock();check(c130.audio.sound.equals("hbm:entity.bomberLoop"),"C130 inherits same health key");AudioWrapper sound=c130.audio;c130.getDataManager().set(EntityPlaneBase.PLANE_HEALTH,-1F);c130.audioConsumerBlock();check(sound.stops==1&&c130.audio==null,"C130 health stop gate");
  EntityBomber onServer=new EntityBomber(server);EntityC130 cServer=new EntityC130(server);int calls=MainRegistry.proxy.requests;onServer.audioConsumerBlock();cServer.audioConsumerBlock();check(MainRegistry.proxy.requests==calls,"no audio on server");
 }
 static EntityBomber factory(int index,W world){switch(index){case 0:return EntityBomber.statFacCarpet(world,8,30,-16);case 1:return EntityBomber.statFacNapalm(world,8,30,-16);case 2:return EntityBomber.statFacChlorine(world,8,30,-16);case 3:return EntityBomber.statFacOrange(world,8,30,-16);case 4:return EntityBomber.statFacABomb(world,8,30,-16);case 5:return EntityBomber.statFacStinger(world,8,30,-16);case 6:return EntityBomber.statFacBoxcar(world,8,30,-16);case 7:return EntityBomber.statFacPC(world,8,30,-16);case 8:return EntityBomber.statFacCV(world,8,30,-16);default:return EntityBomber.statFacCarpetJet(world,8,30,-16);}}
 static void factories(W world)throws Exception {
  int[] starts={50,50,50,75,60,50,50,75,75,10},stops={100,100,100,125,70,150,150,125,125,70},rates={2,5,4,1,65,10,10,1,90000,3};
  for(boolean shortMode:new boolean[]{false,true})for(int i=0;i<10;i++)for(long seed:new long[]{0,1,17,180,4096}){
   GeneralConfig.enableBomberShortMode=shortMode;world.rand.setSeed(seed);Random expected=new Random(seed);
   Vec3d vector=new Vec3d(expected.nextDouble()-0.5,0,expected.nextDouble()-0.5).normalize();double scale=shortMode?1:2;vector=new Vec3d(vector.x*scale,vector.y,vector.z*scale);
   int roll=expected.nextInt(7);int style=roll<2?1:roll<4?2:roll==4?5:roll==5?6:7;
   if(expected.nextInt(100)==0){int rare=expected.nextInt(4);style=rare==0?0:rare==1?3:rare==2?4:8;}
   if(i==4){style=5+expected.nextInt(3);if(expected.nextInt(100)==0)style=8;}
   if(i==5)style=4;if(i==6||i==7)style=6;if(i==8)style=9;if(i==9)style=10;
   EntityBomber b=factory(i,world);check(b.type==i&&b.bombStart==starts[i]&&b.bombStop==stops[i]&&b.bombRate==rates[i]&&b.timer==(i==9?150:200),"all factory local gameplay settings preserved");
   check(b.getBomberStyle()==style&&RendererLookup.style(b)==style,"factory native style caller/result");
   check(b.posX==8-vector.x*100&&b.posZ==-16-vector.z*100&&b.posY==(i==8?96:80),"exact factory position formula");check(b.motionX==vector.x*(i==9?1.5:1)&&b.motionZ==vector.z*(i==9?1.5:1)&&b.motionY==0,"exact speed and jet type-before-fac timing");check(world.rand.nextLong()==expected.nextLong(),"random draw order unchanged");
  }
  for(EntityC130.C130PayloadType type:EntityC130.C130PayloadType.values()){
   world.rand.setSeed(17);EntityC130 c=new EntityC130(world);c.fac(world,8,30,-16,type);Random r=new Random(17);Vec3d v=new Vec3d(r.nextDouble()-0.5,0,r.nextDouble()-0.5).normalize();v=new Vec3d(v.x*2,v.y,v.z*2);
   check(c.payload==type&&c.posY==130&&c.posX==8-v.x*100&&c.posZ==-16-v.z*100&&c.motionX==v.x&&c.motionZ==v.z,"complete C130 factory unchanged");
  }
 }
 public static void main(String[] args)throws Exception {
  net.minecraft.init.Bootstrap.register();W server=new W(false),client=new W(true);configureQuotaZero(server,client);
  if(args[0].equals("c130-first")){Class.forName("com.hbm.entity.logic.EntityC130");Class.forName("com.hbm.entity.logic.EntityBomber");}else{Class.forName("com.hbm.entity.logic.EntityBomber");Class.forName("com.hbm.entity.logic.EntityC130");}
  DataParameter<Float> health=EntityPlaneBase.PLANE_HEALTH;DataParameter<Byte> style=key(EntityBomber.class,"BOMBER_STYLE");
  check(health.getSerializer()==DataSerializers.FLOAT&&style.getSerializer()==DataSerializers.BYTE,"genuine native serializers");check(health.getId()==6&&style.getId()==7,"native owner/inheritance allocation independent of legacy slots17/16");
  Observed observed=new Observed(server);check(Observed.initializations==1&&Observed.beforeOwnFields&&Observed.beforePlaneRegistration&&Observed.nullTicket,"complete genuine Entity constructor virtual dispatch/init order with native Forge quota-zero request");
  check(observed.health==75F&&observed.timer==300&&observed.getDataManager().get(health)==50F&&observed.getBomberStyle()==0&&observed.bombStart==75,"field initialization follows metadata hard-coded default, no changed virtual defaults");
  EntityBomber bomber=new EntityBomber(server),receiving=new EntityBomber(client);EntityC130 c130=new EntityC130(server),cReceiving=new EntityC130(client);
  check(bomber.health==50F&&bomber.timer==200&&bomber.width==8F&&bomber.height==4F&&bomber.ignoreFrustumCheck,"complete original bomber constructor");check(c130.health==50F&&c130.timer==200&&c130.payload==EntityC130.C130PayloadType.SUPPLIES&&c130.ignoreFrustumCheck&&c130.width==8F&&c130.height==4F,"complete C130 constructor");
  layout(bomber,8);layout(c130,7);packet(bomber,receiving,true,8);packet(c130,cReceiving,true,7);check(receiving.getDataManager().get(health)==50F&&receiving.getBomberStyle()==0&&cReceiving.getDataManager().get(health)==50F,"full constructor defaults");
  floatPackets(bomber,receiving);floatPackets(c130,cReceiving);bytePackets(bomber,receiving,style);
  bomber.getDataManager().setClean();packet(bomber,receiving,false,0);bomber.health=-6.5F;bomber.synchronizeHealthPrefix();bomber.getDataManager().set(style,(byte)42);packet(bomber,receiving,false,2);check(receiving.getDataManager().get(health)==-6.5F&&receiving.getBomberStyle()==42,"independent parent/child delta entries");
  nbt(server,client,style);audio(server,client,style);factories(server);
  check(((Map<?,?>)get(ForgeChunkManager.class,null,"tickets")).size()==2,"only fixture worlds present, no world startup");
  System.out.println("PASS "+checks+" native plane metadata assertions ("+args[0]+"); native Forge quota-zero path, no loaded chunks, flight, payload, audio engine or GL");
 }
}
