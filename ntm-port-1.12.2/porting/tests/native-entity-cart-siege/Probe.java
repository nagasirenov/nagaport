import java.lang.reflect.*;
import java.util.*;
import com.hbm.entity.cart.*;
import com.hbm.entity.item.EntityMagnusCartus;
import com.hbm.entity.mob.*;
import com.hbm.entity.mob.siege.*;
import com.hbm.items.tool.ItemModMinecart.EnumCartBase;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.*;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.*;
import io.netty.buffer.Unpooled;

public class Probe {
 static int checks;
 static void check(boolean ok,String why){checks++;if(!ok)throw new AssertionError(why);}
 @SuppressWarnings("unchecked") static <T> DataParameter<T> key(Class<?> owner,String name)throws Exception{Field f=owner.getDeclaredField(name);f.setAccessible(true);return (DataParameter<T>)f.get(null);}
 static class W extends World {
  final Map<Integer,Entity> entities=new HashMap<>();
  W(boolean remote){super(new net.minecraft.world.storage.SaveHandlerMP(),new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new net.minecraft.profiler.Profiler(),remote);}
  protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
  protected boolean isChunkLoaded(int x,int z,boolean allow){throw new AssertionError("No chunk operations in bounded metadata proof");}
  public Entity getEntityByID(int id){return entities.get(id);}
 }
 static List<EntityDataManager.DataEntry<?>> packet(Entity a,Entity b,boolean full,int count)throws Exception{
  PacketBuffer buf=new PacketBuffer(Unpooled.buffer());new SPacketEntityMetadata(a.getEntityId(),a.getDataManager(),full).writePacketData(buf);
  SPacketEntityMetadata p=new SPacketEntityMetadata();p.readPacketData(buf);check(p.getEntityId()==a.getEntityId(),"native entity id");check(buf.readableBytes()==0,"native framing fully consumed");
  List<EntityDataManager.DataEntry<?>> es=p.getDataManagerEntries();check((es==null?0:es.size())==count,"exact entry count expected="+count+" actual="+(es==null?0:es.size()));if(es!=null)b.getDataManager().setEntryValues(es);buf.release();return es;
 }
 static NBTTagCompound binary(NBTTagCompound n)throws Exception{java.io.ByteArrayOutputStream b=new java.io.ByteArrayOutputStream();CompressedStreamTools.write(n,new java.io.DataOutputStream(b));return CompressedStreamTools.read(new java.io.DataInputStream(new java.io.ByteArrayInputStream(b.toByteArray())));}
 static void layout(Entity e,int count){Set<Integer> ids=new HashSet<>();for(EntityDataManager.DataEntry<?> v:e.getDataManager().getAll())check(ids.add(v.getKey().getId()),"unique inherited metadata id");check(ids.size()==count,"exact inherited registration count");}
 static void ints(Entity a,Entity b,Class<?> owner,String name)throws Exception{
  DataParameter<Integer> k=key(owner,name);check(k.getSerializer()==DataSerializers.VARINT,"real VARINT");
  a.getDataManager().setClean();packet(a,b,false,0);
  for(int n:new int[]{Integer.MIN_VALUE,-1,0,1,127,128,255,65535,Integer.MAX_VALUE}){
   int before=a.getDataManager().get(k);a.getDataManager().set(k,n);check(a.getDataManager().isDirty()==(before!=n),"integer equality/dirty semantics");packet(a,b,false,before==n?0:1);check(b.getDataManager().get(k)==n,"signed native int packet");check(!a.getDataManager().isDirty()&&a.getDataManager().getDirty()==null,"native delta reset");a.getDataManager().set(k,n);check(!a.getDataManager().isDirty(),"equal int no delta");
  }
  List<EntityDataManager.DataEntry<?>> snapshot=a.getDataManager().getAll();a.getDataManager().set(k,-17);for(EntityDataManager.DataEntry<?> e:snapshot)if(e.getKey().getId()==k.getId())check(((Integer)e.getValue())==Integer.MAX_VALUE,"immutable integer snapshot");
  packet(a,b,false,1);a.getDataManager().setDirty(k);packet(a,b,false,1);check(b.getDataManager().get(k)==-17,"forced dirty native packet");
 }
 static void carts(W server,W client)throws Exception{
  EntityMinecartOre a=new EntityMinecartOre(server),b=new EntityMinecartOre(client);DataParameter<Integer> base=key(EntityMinecartNTM.class,"CART_BASE");
  check(base.getId()==12,"native minecart parent owns six keys after Entity six");layout(a,13);layout(b,13);check(a.getBase()==EnumCartBase.VANILLA&&b.getBase()==EnumCartBase.VANILLA,"hard-coded default zero enum both sides");packet(a,b,true,13);
  Class<?>[] children={EntityMinecartOre.class,EntityMinecartPowder.class,EntityMinecartSemtex.class,EntityMinecartContainerBase.class,EntityMinecartCrate.class,EntityMinecartDestroyer.class};
  for(Class<?> c:children){EntityMinecartNTM q=(EntityMinecartNTM)c.getConstructor(World.class).newInstance(server);layout(q,13);check(q.getBase()==EnumCartBase.VANILLA,"every named unchanged descendant inherits base default");q.setBase(EnumCartBase.PAINTED);check(q.getBase()==EnumCartBase.PAINTED,"every named descendant inherited native key");}
  for(EnumCartBase t:EnumCartBase.values()){int old=a.getDataManager().get(base);a.setBase(t);packet(a,b,false,old==t.ordinal()?0:1);check(b.getBase()==t,"exact enum ordinal getter/setter");NBTTagCompound n=new NBTTagCompound();a.saveFixture(n);n=binary(n);check(n.getTagId("base")==3&&n.getInteger("base")==t.ordinal(),"original base integer NBT");b.loadFixture(n);check(b.getBase()==t,"complete custom read invokes genuine native cart parent");}
  for(int raw:new int[]{Integer.MIN_VALUE,-1,4,Integer.MAX_VALUE}){NBTTagCompound n=new NBTTagCompound();n.setInteger("base",raw);a.loadFixture(binary(n));check(a.getDataManager().get(base)==raw,"literal legacy slot23 now same owned key, raw load not clamped");boolean threw=false;try{a.getBase();}catch(ArrayIndexOutOfBoundsException expected){threw=true;}check(threw,"invalid enum behavior remains failure rather than repaired fallback");}
  a.loadFixture(new NBTTagCompound());check(a.getBase()==EnumCartBase.VANILLA,"absent base default zero");a.getDataManager().setClean();a.setBase(EnumCartBase.VANILLA);check(!a.getDataManager().isDirty(),"idempotent base clean");ints(a,b,EntityMinecartNTM.class,"CART_BASE");
  EntityMagnusCartus m=new EntityMagnusCartus(server),n=new EntityMagnusCartus(client);DataParameter<Integer> bogie=key(EntityMagnusCartus.class,"BOGIE_ID");check(bogie!=base&&bogie.getId()==base.getId(),"sibling cart keys distinct despite same native index");check(m.getBogieID()==0&&n.getBogieID()==0,"bogie hard-coded default");layout(m,13);packet(m,n,true,13);
  EntityMinecartBogie linked=new EntityMinecartBogie(server);linked.setEntityId(-12345);server.entities.put(linked.getEntityId(),linked);m.setBogie(linked);check(m.bogie==linked&&m.getBogieID()==-12345,"setter keeps ordinary reference plus synchronized id");packet(m,n,false,1);check(n.bogie==null&&n.getBogieID()==-12345,"metadata receive does not repair unsynchronized local reference");check(Consumers.bogie(n)==null,"renderer lookup honors client's world, not server local reference");client.entities.put(-12345,linked);check(Consumers.bogie(n)==linked,"exact renderer typed id consumer");
  NBTTagCompound saved=new NBTTagCompound();m.saveFixture(saved);saved=binary(saved);check(saved.getTagId("bogie")==3&&saved.getInteger("bogie")==-12345,"bogie signed integer binary NBT");EntityMagnusCartus loaded=new EntityMagnusCartus(server);loaded.loadFixture(saved);check(loaded.bogie==linked&&loaded.getBogieID()==-12345,"complete custom NBT world lookup invokes original setter");
  saved.setInteger("bogie",888);loaded.loadFixture(saved);check(loaded.bogie==linked&&loaded.getBogieID()==-12345,"absent lookup retains old local and synchronized value");server.entities.put(888,a);loaded.loadFixture(saved);check(loaded.bogie==linked&&loaded.getBogieID()==-12345,"wrong-type lookup retains old values");loaded.loadFixture(new NBTTagCompound());check(loaded.getBogieID()==-12345,"missing NBT tag is lookup zero, not forced reset");
  boolean threw=false;try{m.setBogie(null);}catch(NullPointerException expected){threw=true;}check(threw&&m.bogie==null&&m.getBogieID()==-12345,"null setter preserves original assignment-before-NPE quirk");ints(m,n,EntityMagnusCartus.class,"BOGIE_ID");
 }
 static void waypoints(EntityUFOBase a,EntityUFOBase b)throws Exception{
  check(a.getX()==0&&a.getY()==0&&a.getZ()==0,"inherited hard-coded XYZ zero");for(String axis:new String[]{"X","Y","Z"})ints(a,b,EntityUFOBase.class,"WAYPOINT_"+axis);
  a.getDataManager().setClean();a.setWaypoint(Integer.MIN_VALUE,42,Integer.MAX_VALUE);packet(a,b,false,3);check(b.getX()==Integer.MIN_VALUE&&b.getY()==42&&b.getZ()==Integer.MAX_VALUE,"complete waypoint setter and axis getters");a.setWaypoint(Integer.MIN_VALUE,42,Integer.MAX_VALUE);check(!a.getDataManager().isDirty(),"idempotent waypoint all axes clean");
 }
 static void tier(Entity e,Entity receiving)throws Exception{
  Class<?> c=e.getClass();DataParameter<Integer> k=key(c,"SIEGE_TIER");Method set=c.getMethod("setTierMetadataPrefix",SiegeTier.class),get=c.getMethod("getTier"),select=c.getMethod("readTierSelection",NBTTagCompound.class);
  for(int n=0;n<100;n++){e.getDataManager().set(k,n);SiegeTier expected=SiegeTier.tiers[n]==null?SiegeTier.CLAY:SiegeTier.tiers[n];check(get.invoke(e)==expected,"null tier fallback remains CLAY, populated tier unchanged");}
  for(int n:new int[]{-1,100,Integer.MIN_VALUE,Integer.MAX_VALUE}){e.getDataManager().set(k,n);boolean threw=false;try{get.invoke(e);}catch(InvocationTargetException x){threw=x.getCause() instanceof ArrayIndexOutOfBoundsException;}check(threw,"tier out-of-bounds remains failure");}
  for(int n=0;n<9;n++){set.invoke(e,SiegeTier.tiers[n]);check(e.getDataManager().get(k)==n&&get.invoke(e)==SiegeTier.tiers[n],"exact original setter metadata prefix");NBTTagCompound tag=new NBTTagCompound();c.getMethod("writeEntityToNBT",NBTTagCompound.class).invoke(e,tag);tag=binary(tag);check(tag.getTagId("siegeTier")==3&&tag.getInteger("siegeTier")==n,"complete custom writer and native superclass binary NBT");check(select.invoke(null,tag)==SiegeTier.tiers[n],"exact original NBT tier selection expression");}
  check(select.invoke(null,new NBTTagCompound())==SiegeTier.tiers[0],"absent siegeTier still selects index0, not CLAY fallback");NBTTagCompound empty=new NBTTagCompound();empty.setInteger("siegeTier",99);check(select.invoke(null,empty)==null,"unpopulated NBT index selects null without repair");
  int before=e.getDataManager().get(k);boolean npe=false;try{set.invoke(e,new Object[]{null});}catch(InvocationTargetException x){npe=x.getCause() instanceof NullPointerException;}check(npe&&e.getDataManager().get(k)==before,"null tier setter fails before write");ints(e,receiving,c,"SIEGE_TIER");
 }
 static void locks(EntitySiegeCraft a,EntitySiegeCraft b)throws Exception{
  DataParameter<Byte> beam=key(EntitySiegeCraft.class,"BEAM");check(beam.getSerializer()==DataSerializers.BYTE,"beam native BYTE");a.getDataManager().setClean();
  for(int raw=-128;raw<=127;raw++){a.getDataManager().set(beam,(byte)raw);a.getDataManager().setDirty(beam);packet(a,b,false,1);check(b.getDataManager().get(beam)==(byte)raw,"all256 raw signed BYTE values survive native packet");check(b.getBeam()==(raw==1),"beam boolean is equality to1, not bitmask");a.getDataManager().setClean();a.setBeam(true);check(a.getDataManager().get(beam)==1&&a.getDataManager().isDirty()==(raw!=1),"true replaces entire byte and preserves native equality semantics");a.getDataManager().setClean();a.setBeam(false);check(a.getDataManager().get(beam)==0&&a.getDataManager().isDirty(),"false replaces entire byte with0");a.getDataManager().setClean();}
  for(String axis:new String[]{"X","Y","Z"})check(Probe.<Float>key(EntitySiegeCraft.class,"LOCKON_"+axis).getSerializer()==DataSerializers.FLOAT,"lockon native FLOAT");
  DataParameter<Float> x=key(EntitySiegeCraft.class,"LOCKON_X"),y=key(EntitySiegeCraft.class,"LOCKON_Y"),z=key(EntitySiegeCraft.class,"LOCKON_Z");double[] values={0.0,-0.0,Double.MIN_VALUE,-Double.MIN_VALUE,1.0/3.0,16777217.0,Double.MAX_VALUE,-Double.MAX_VALUE,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NaN};
  for(double value:values){a.getDataManager().setClean();float before=a.getDataManager().get(x),expected=(float)value;a.setLockon(value,value,value);boolean changed=!Float.valueOf(before).equals(expected);packet(a,b,false,changed?3:0);check(Float.floatToRawIntBits(b.getDataManager().get(x))==Float.floatToRawIntBits(expected),"double-to-float narrowing and native FLOAT packet");check(Float.floatToRawIntBits(b.getDataManager().get(y))==Float.floatToRawIntBits(expected)&&Float.floatToRawIntBits(b.getDataManager().get(z))==Float.floatToRawIntBits(expected),"all independent float axes");Vec3d v=b.getLockon();check(Double.isNaN(value)?Double.isNaN(v.x):v.x==(double)expected,"exact Vec3d getter values including native zero normalization");a.setLockon(value,value,value);check(!a.getDataManager().isDirty(),"Float.equals idempotence");}
  a.getDataManager().setClean();a.getDataManager().set(x,Float.intBitsToFloat(0x7fc01234));check(!a.getDataManager().isDirty(),"distinct NaN payload remains equal under Float.equals");
  a.setLockon(1.25,-2.5,3.75);a.posX=.25;a.posY=.5;a.posZ=.75;Vec3d delta=Consumers.lockonRead(a);check(delta.x==1.25&&delta.y==-2.5&&delta.z==3.75,"exact renderer lockon read expression; legacy vector addition excluded");a.setTierMetadataPrefix(SiegeTier.CLAY);check(Consumers.tier(a)==SiegeTier.CLAY,"actual renderer tier lookup");
  a.setBeam(true);NBTTagCompound tag=new NBTTagCompound();a.writeEntityToNBT(tag);check(!tag.hasKey("beam")&&!tag.hasKey("lockon")&&!tag.hasKey("lifetime")&&!tag.hasKey("isRetreating")&&tag.hasKey("siegeTier"),"craft old save omissions remain, no beam target or lifetime persistence introduced");
 }
 public static void main(String[] args)throws Exception{
  net.minecraft.init.Bootstrap.register();W server=new W(false),client=new W(true);
  String[] order=args[0].equals("craft-first")?new String[]{"EntitySiegeCraft","EntitySiegeUFO","EntitySiegeTunneler"}:new String[]{"EntitySiegeTunneler","EntitySiegeUFO","EntitySiegeCraft"};for(String n:order)Class.forName("com.hbm.entity.mob.siege."+n);
  for(int i=0;i<9;i++){SiegeTier t=new SiegeTier(20+i,"tier"+i);if(i==1)SiegeTier.CLAY=t;}
  carts(server,client);
  EntitySiegeCraft craft=new EntitySiegeCraft(server),craftClient=new EntitySiegeCraft(client);EntitySiegeUFO ufo=new EntitySiegeUFO(server),ufoClient=new EntitySiegeUFO(client);EntityFBIDrone fbi=new EntityFBIDrone(server),fbiClient=new EntityFBIDrone(client);EntitySiegeTunneler tunnel=new EntitySiegeTunneler(server),tunnelClient=new EntitySiegeTunneler(client);
  Entity[] es={craft,ufo,fbi,tunnel},cs={craftClient,ufoClient,fbiClient,tunnelClient};int[] counts={20,16,15,13};for(int i=0;i<es.length;i++){layout(es[i],counts[i]);layout(cs[i],counts[i]);packet(es[i],cs[i],true,counts[i]);}
  check(Probe.<Integer>key(EntityUFOBase.class,"WAYPOINT_X").getId()==12&&Probe.<Integer>key(EntitySiegeCraft.class,"SIEGE_TIER").getId()==15&&Probe.<Integer>key(EntitySiegeUFO.class,"SIEGE_TIER").getId()==15&&Probe.<Integer>key(EntitySiegeTunneler.class,"SIEGE_TIER").getId()==12,"actual native inheritance allocations independent of original slot numbers/loading order");
  check(key(EntitySiegeCraft.class,"SIEGE_TIER")!=key(EntitySiegeUFO.class,"SIEGE_TIER"),"sibling own key identity");check(craft.getTier()==SiegeTier.tiers[0]&&ufo.getTier()==SiegeTier.tiers[0]&&tunnel.getTier()==SiegeTier.tiers[0]&&!craft.getBeam()&&craft.getLockon().lengthSquared()==0,"exact original default custom values");
  waypoints(craft,craftClient);waypoints(ufo,ufoClient);waypoints(fbi,fbiClient);tier(craft,craftClient);tier(ufo,ufoClient);tier(tunnel,tunnelClient);locks(craft,craftClient);
  NBTTagCompound n=new NBTTagCompound();n.setInteger("lifetime",Integer.MIN_VALUE);n.setBoolean("isRetreating",true);ufo.readLifetimeSuffix(binary(n));ufo.setTierMetadataPrefix(SiegeTier.tiers[0]);NBTTagCompound saved=new NBTTagCompound();ufo.writeEntityToNBT(saved);saved=binary(saved);check(saved.getInteger("lifetime")==Integer.MIN_VALUE&&saved.getBoolean("isRetreating"),"exact UFO NBT suffix and full writer keep extra lifetime tags");ufo.readLifetimeSuffix(new NBTTagCompound());saved=new NBTTagCompound();ufo.writeEntityToNBT(saved);check(saved.getInteger("lifetime")==0&&!saved.getBoolean("isRetreating"),"missing UFO suffix defaults unchanged");
  System.out.println("PASS "+checks+" native cart/siege metadata assertions ("+args[0]+"); bounded projections only, not full owners, tier attribute effects, movement, AI, inventory, game startup or renderer GL");
 }
}
