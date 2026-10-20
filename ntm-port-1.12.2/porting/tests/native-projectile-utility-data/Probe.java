import java.lang.reflect.*;
import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.nbt.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.*;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import io.netty.buffer.Unpooled;
import com.hbm.entity.projectile.*;
import com.hbm.entity.effect.*;
import com.hbm.lib.ForgeDirection;
import com.hbm.inventory.fluid.*;
import com.hbm.inventory.fluid.trait.*;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.trait.CBT_Atmosphere;

public class Probe {
 static int checks;
 static void check(boolean v,String why){checks++;if(!v)throw new AssertionError(why);}
 static int bits(float v){return Float.floatToRawIntBits(v);}
 static Object field(Class<?> c,Object o,String name)throws Exception{Field f=c.getDeclaredField(name);f.setAccessible(true);return f.get(o);}
 @SuppressWarnings("unchecked") static <T> DataParameter<T> key(Class<?> c,String name)throws Exception{return (DataParameter<T>)field(c,null,name);}
 static Object call(Class<?> c,Object o,String name,Class<?>[] types,Object...args)throws Exception{Method m=c.getDeclaredMethod(name,types);m.setAccessible(true);try{return m.invoke(o,args);}catch(InvocationTargetException e){if(e.getCause() instanceof RuntimeException)throw (RuntimeException)e.getCause();throw e;}}
 static void nbtWrite(Class<?> c,Object e,NBTTagCompound n)throws Exception{call(c,e,"writeEntityToNBT",new Class<?>[]{NBTTagCompound.class},n);}
 static void nbtRead(Class<?> c,Object e,NBTTagCompound n)throws Exception{call(c,e,"readEntityFromNBT",new Class<?>[]{NBTTagCompound.class},n);}
 static ForgeDirection direction(EntityDepress e)throws Exception{return (ForgeDirection)call(EntityDepress.class,e,"getDir",new Class<?>[0]);}
 static void direction(EntityDepress e,ForgeDirection d)throws Exception{call(EntityDepress.class,e,"setDir",new Class<?>[]{ForgeDirection.class},d);}
 static class W extends World {
  IBlockState state=Blocks.WOOL.getDefaultState();BlockPos lastRead;
  W(boolean remote){super(new net.minecraft.world.storage.SaveHandlerMP(),new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new net.minecraft.profiler.Profiler(),remote);}
  protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
  protected boolean isChunkLoaded(int x,int z,boolean allow){throw new AssertionError("Chunk operations outside scope");}
  public IBlockState getBlockState(BlockPos pos){lastRead=pos.toImmutable();return state;}
 }
 static class ObservedChemical extends EntityChemical {
  static int inits;static boolean uninitializedFlags;
  ObservedChemical(World w){super(w);}
  protected void entityInit(){inits++;uninitializedFlags=!ignoreFrustumCheck&&!isImmuneToFire;super.entityInit();}
 }
 static class ObservedDepress extends EntityDepress {
  static boolean registeredBeforeFields,writeAfterFields;static int inits;
  ObservedDepress(World w,ForgeDirection d,int ttl){super(w,d,ttl);}
  protected void entityInit(){inits++;registeredBeforeFields=timeToLive==0&&!noClip&&!ignoreFrustumCheck&&!isImmuneToFire;super.entityInit();}
  public void notifyDataManagerChange(DataParameter<?> k){super.notifyDataManagerChange(k);if(k.getId()==6)writeAfterFields=timeToLive==23&&noClip&&ignoreFrustumCheck&&isImmuneToFire;}
 }
 static List<EntityDataManager.DataEntry<?>> packet(Entity a,Entity b,boolean full,int count)throws Exception{
  PacketBuffer out=new PacketBuffer(Unpooled.buffer());new SPacketEntityMetadata(a.getEntityId(),a.getDataManager(),full).writePacketData(out);SPacketEntityMetadata read=new SPacketEntityMetadata();read.readPacketData(out);
  check(out.readableBytes()==0,"all actual metadata packet bytes consumed");check(read.getEntityId()==a.getEntityId(),"actual packet entity ID");List<EntityDataManager.DataEntry<?>> data=read.getDataManagerEntries();check((data==null?0:data.size())==count,"exact full/delta entry count");if(data!=null)b.getDataManager().setEntryValues(data);out.release();return data;
 }
 static void layout(Entity e,int size,int...own){Set<Integer> ids=new HashSet<>();for(EntityDataManager.DataEntry<?> d:e.getDataManager().getAll())check(ids.add(d.getKey().getId()),"no inherited key collision");check(ids.size()==size,"actual registered key count");for(int id:own)check(ids.contains(id),"own ID registered");}
 static NBTTagCompound binary(NBTTagCompound n)throws Exception{java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();CompressedStreamTools.write(n,new java.io.DataOutputStream(out));return CompressedStreamTools.read(new java.io.DataInputStream(new java.io.ByteArrayInputStream(out.toByteArray())));}
 static <T> T snapshotValue(List<EntityDataManager.DataEntry<?>> entries,DataParameter<T> key){for(EntityDataManager.DataEntry<?> e:entries)if(e.getKey().getId()==key.getId())return (T)e.getValue();throw new AssertionError("key absent");}
 static void bytes(EntityFallingNuke a,EntityFallingNuke b,EntityDepress d,EntityDepress e,DataParameter<Byte> block,DataParameter<Byte> dir)throws Exception{
  a.getDataManager().setClean();d.getDataManager().setClean();
  for(int i=-128;i<=127;i++){
   a.setBlockMetadata((byte)i);d.getDataManager().set(dir,(byte)i);check(a.getDataManager().isDirty()&&d.getDataManager().isDirty(),"changed signed byte dirty");
   check(ConsumerSlices.rendererLookup(a)==(byte)i,"exact erased renderer lookup preserves signed raw byte");packet(a,b,false,1);packet(d,e,false,1);
   check(b.getBlockMetadata()==(byte)i&&ConsumerSlices.rendererLookup(b)==(byte)i,"all signed model metadata values roundtrip");check(e.getDataManager().get(dir)==(byte)i,"all signed directions roundtrip without clamping");
   check(direction(e)==(i>=0&&i<=5?ForgeDirection.values()[i]:ForgeDirection.UNKNOWN),"existing direction interpretation and invalid fallback");
   a.setBlockMetadata((byte)i);d.getDataManager().set(dir,(byte)i);check(!a.getDataManager().isDirty()&&!d.getDataManager().isDirty(),"equal byte stays clean");
  }
  List<EntityDataManager.DataEntry<?>> snap=a.getDataManager().getAll();a.setBlockMetadata((byte)-20);check(snapshotValue(snap,block)==(byte)127,"immutable byte snapshot");packet(a,b,false,1);a.getDataManager().setDirty(block);packet(a,b,false,1);check(b.getBlockMetadata()==-20,"force dirty same byte");
  packet(a,b,false,0);packet(d,e,false,0);
 }
 static void fluids(EntityChemical a,EntityChemical b,DataParameter<Integer> id)throws Exception{
  a.getDataManager().setClean();
  for(int i:new int[]{Integer.MIN_VALUE,-32769,-128,-1,0,1,127,128,255,256,16383,16384,65535,Integer.MAX_VALUE}){
   FluidType fixture=new FluidType(i,0x123456);check(a.setFluid(fixture)==a,"fluent fluid setter unchanged");check(a.getDataManager().get(id)==i,"raw original numeric fluid ID retained");packet(a,b,false,1);check(b.getDataManager().get(id)==i,"native VARINT edge roundtrip");
   check(b.getType()==(i==0?Fluids.NONE:i==1?Fluids.IONGEL:Fluids.NONE),"exact fromID lookup fallback, no synchronization clamping");a.setFluid(new FluidType(i,0));check(!a.getDataManager().isDirty(),"equal independent fluid ID clean");
  }
  int old=a.getDataManager().get(id);boolean failed=false;try{a.setFluid(null);}catch(NullPointerException expected){failed=true;}check(failed&&a.getDataManager().get(id)==old&&!a.getDataManager().isDirty(),"null fluid still throws before metadata write");
  FluidType[] fs={Fluids.NONE,Fluids.IONGEL,new FluidType(100,0,new FT_Amat(),new FT_Liquid()),new FluidType(101,0,new FT_Gaseous()),new FluidType(102,0,new FT_Gaseous_ART(),new FT_Flammable()),new FluidType(103,0,new FT_Liquid(),new FT_Combustible()),new FluidType(104,0,new FT_Liquid())};
  EntityChemical.ChemicalStyle[] styles={EntityChemical.ChemicalStyle.NULL,EntityChemical.ChemicalStyle.LIGHTNING,EntityChemical.ChemicalStyle.AMAT,EntityChemical.ChemicalStyle.GAS,EntityChemical.ChemicalStyle.GASFLAME,EntityChemical.ChemicalStyle.BURNING,EntityChemical.ChemicalStyle.LIQUID};int[] ages={100,5,100,60,20,600,600};double[] gravity={0.03,0,0,0,-0.01,0.03,0.03};
  for(int i=0;i<fs.length;i++){Fluids.fixtureRegister(fs[i]);a.setFluid(fs[i]);packet(a,b,false,1);check(b.getType()==fs[i]&&b.getStyle()==styles[i],"exact registered identity/style application consumer");check(b.getMaxAge()==ages[i]&&b.getGravityVelocity()==gravity[i],"unaltered metadata-dependent age/gravity getters");}
  List<EntityDataManager.DataEntry<?>> snapshot=a.getDataManager().getAll();a.setFluid(Fluids.NONE);check(snapshotValue(snapshot,id)==104,"immutable VARINT snapshot");packet(a,b,false,1);a.getDataManager().setDirty(id);packet(a,b,false,1);packet(a,b,false,0);
 }
 static void floatData(EntityNukeTorex a,EntityNukeTorex b,DataParameter<Float> scale,DataParameter<Integer> color)throws Exception{
  a.getDataManager().setClean();float[] values={0F,-0F,Float.MIN_VALUE,-Float.MIN_VALUE,1.5F,-3F,Float.MAX_VALUE,-Float.MAX_VALUE,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.intBitsToFloat(0x7fc01234)};
  for(float v:values){a.setSynchronizedScale(v);check(a.getDataManager().isDirty(),"changed float dirty");packet(a,b,false,1);check(bits(b.getDataManager().get(scale))==bits(v),"native FLOAT preserves raw edge bits");a.setSynchronizedScale(v);check(!a.getDataManager().isDirty(),"equal Float stays clean");}
  a.setSynchronizedScale(Float.intBitsToFloat(0x7fc05678));check(!a.getDataManager().isDirty()&&bits(a.getDataManager().get(scale))==0x7fc01234,"Float.equals NaN payload coalescing retained");
  List<EntityDataManager.DataEntry<?>> snapshot=a.getDataManager().getAll();a.setSynchronizedScale(2.5F);check(bits(snapshotValue(snapshot,scale))==0x7fc01234,"immutable float snapshot");packet(a,b,false,1);a.getDataManager().setDirty(scale);packet(a,b,false,1);packet(a,b,false,0);
  for(int v:new int[]{Integer.MIN_VALUE,-1,0,1,2,3,Integer.MAX_VALUE}){check(a.setType(v)==a,"fluent color setter");check(a.getDataManager().get(color)==v,"arbitrary signed color ID");packet(a,b,false,1);check(b.getDataManager().get(color)==v,"actual native type VARINT");a.setType(v);check(!a.getDataManager().isDirty(),"equal color ID clean");}
  a.setSynchronizedScale(3.75F);a.setType(1);packet(a,b,false,2);check(b.getScale()==3.75&&b.getDataManager().get(color)==1,"two distinct Torex keys in same dirty packet");
 }
 static void effects(W server,W client,DataParameter<Byte> dir)throws Exception{
  EntityDepress defaults=new EntityDepress(server);check(defaults.timeToLive==100&&direction(defaults)==ForgeDirection.DOWN&&defaults.noClip&&defaults.ignoreFrustumCheck&&defaults.isImmuneToFire(),"complete delegated default constructor");
  ObservedDepress d=new ObservedDepress(server,ForgeDirection.EAST,23);check(ObservedDepress.inits==1&&ObservedDepress.registeredBeforeFields&&ObservedDepress.writeAfterFields,"native constructor virtual init then field assignment then direction write");
  for(ForgeDirection value:ForgeDirection.values()){
   direction(d,value);check(direction(d)==value,"all ordinal setter values including UNKNOWN");d.setPosition(10,20,30);server.rand.setSeed(19);Random r=new Random(19);NBTTagCompound n=d.particleParametersSlice();
   check(n.getDouble("posX")==10+r.nextGaussian()*0.25&&n.getDouble("posY")==20+r.nextGaussian()*0.25&&n.getDouble("posZ")==30+r.nextGaussian()*0.25,"exact particle-position random draws");
   check(n.getDouble("moX")==value.offsetX*0.5+r.nextGaussian()*0.25&&n.getDouble("moY")==value.offsetY*0.5+r.nextGaussian()*0.25&&n.getDouble("moZ")==value.offsetZ*0.5+r.nextGaussian()*0.25,"native synchronized direction feeds exact particle vector");check(n.getInteger("maxAge")==100+r.nextInt(20)&&n.getInteger("color")==0xFFFFFF&&n.getFloat("scale")==0.5F&&n.getString("type").equals("depress"),"unchanged complete particle parameter values");check(server.rand.nextLong()==r.nextLong(),"original random consumption");
  }
  byte old=d.getDataManager().get(dir);boolean failed=false;try{direction(d,null);}catch(NullPointerException expected){failed=true;}check(failed&&d.getDataManager().get(dir)==old,"null direction throws before metadata mutation");
  for(int ttl:new int[]{Integer.MIN_VALUE,-1,0,23,Integer.MAX_VALUE}){d.timeToLive=ttl;direction(d,ForgeDirection.EAST);NBTTagCompound n=new NBTTagCompound();nbtWrite(EntityDepress.class,d,n);check(n.getKeySet().equals(Collections.singleton("ttl"))&&n.hasKey("ttl",3),"only original integer TTL persisted");EntityDepress read=new EntityDepress(client);nbtRead(EntityDepress.class,read,binary(n));check(read.timeToLive==ttl&&direction(read)==ForgeDirection.DOWN,"direction deliberately not saved");direction(read,ForgeDirection.WEST);nbtRead(EntityDepress.class,read,new NBTTagCompound());check(read.timeToLive==0&&direction(read)==ForgeDirection.WEST,"missing TTL zero; live direction remains");}
 }
 static void nukeNBT(W server,W client)throws Exception{
  float[] values={-0F,Float.MIN_VALUE,-4F,Float.MAX_VALUE,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.intBitsToFloat(0x7fc01234)};String[] fields={"tnt","nuke","hydro","amat","dirty","schrab","euph"};EntityFallingNuke a=new EntityFallingNuke(server,values[0],values[1],values[2],values[3],values[4],values[5],values[6]);
  check(a.ignoreFrustumCheck&&a.width==0.98F&&a.height==0.98F&&a.rotationPitch==90&&a.prevRotationPitch==90&&a.rotationYaw==90&&a.prevRotationYaw==90,"complete payload constructor flags/size/rotations");for(int i=0;i<7;i++)check(bits((Float)field(EntityFallingNuke.class,a,fields[i]))==bits(values[i]),"complete constructor stores raw payload floats");
  a.setBlockMetadata((byte)5);NBTTagCompound n=new NBTTagCompound();a.writeEntityToNBT(n);for(String name:fields)check(n.hasKey(name,5),"original float NBT payload type");check(!n.hasKey("metadata")&&!n.hasKey("BLOCK_METADATA"),"model orientation still not persisted");
  EntityFallingNuke b=new EntityFallingNuke(client);b.readEntityFromNBT(binary(n));for(int i=0;i<7;i++)check(Float.floatToIntBits((Float)field(EntityFallingNuke.class,b,fields[i]))==Float.floatToIntBits(values[i]),"actual native parent and payload binary NBT roundtrip (binary NBT canonicalizes NaN)");check(b.getBlockMetadata()==0,"fresh NBT load loses orientation as before");b.setBlockMetadata((byte)-7);b.readEntityFromNBT(new NBTTagCompound());for(String name:fields)check(bits((Float)field(EntityFallingNuke.class,b,name))==bits(0F),"missing payload tags zero");check(b.getBlockMetadata()==-7,"NBT load does not alter existing model metadata");
 }
 static void torexBehavior(W server,W client)throws Exception{
  for(boolean orbit:new boolean[]{false,true})for(Double pressure:new Double[]{null,0D,0.0099,0.01,1D}){CelestialBody.orbit=orbit;CelestialBody.atmosphere=pressure==null?null:new CBT_Atmosphere(pressure);EntityNukeTorex t=new EntityNukeTorex(server);check((Boolean)field(EntityNukeTorex.class,t,"hasSufficientPressure")==(!orbit&&pressure!=null&&pressure>=0.01),"complete original atmosphere constructor branch against explicit fixture inputs");check(t.ignoreFrustumCheck&&t.width==1F&&t.height==50F&&t.cloudlets.isEmpty(),"complete constructor dimensions and initialized collection");}
  CelestialBody.orbit=false;CelestialBody.atmosphere=new CBT_Atmosphere(1);
  EntityNukeTorex s=new EntityNukeTorex(server),c=new EntityNukeTorex(client);check(s.getScale()==1&&s.coreHeight==3&&s.convectionHeight==3&&s.torusWidth==3&&s.rollerSize==1,"metadata initialization precedes exact local field initialization");
  check(s.setScale(3)==s&&s.getScale()==3&&s.coreHeight==6&&s.convectionHeight==6&&s.torusWidth==6&&s.rollerSize==2,"server setter writes metadata then unchanged local rescaling");check(c.setScale(3)==c&&c.getScale()==1&&c.coreHeight==6&&c.rollerSize==2,"client setter does not synchronize scale but still rescales locals");
  ConsumerSlices.setBoyScale(s);check(s.getScale()==1.5&&s.coreHeight==6&&s.rollerSize==2,"NukeBoy raw-write replacement must not call resizing setter");s.setSynchronizedScale(2);packet(s,c,true,8);check(c.getScale()==2&&c.coreHeight==6&&c.rollerSize==2,"metadata packet application does not resize receiver simulation fields");
  for(float scale:new float[]{-1,0,0.5F,1,1.5F,5,Float.POSITIVE_INFINITY,Float.NaN}){s.setSynchronizedScale(scale);check(s.getMaxAge()==(int)(900D*(double)scale),"unchanged max-age widening/cast for all float categories");}
  s.setSynchronizedScale(1);for(int tick:new int[]{0,225,226,450,451,675,676,900,901}){s.ticksExisted=tick;double sim=tick>450?0:tick>225?1D-(tick-225)/225D:1D;check(s.getSimulationSpeed()==sim,"exact age-dependent simulation-speed getter");check(s.getSaturation()==1D-Math.pow(tick/900D,4),"exact saturation getter");check(s.getGreying()==(tick>675?1D+(tick-675)/225D:1D),"exact greying getter");check(s.getAlpha()==(tick>675?1F-(float)(tick-675)/225F:1F),"exact alpha getter");}
  s.coreHeight=3;s.heat=1;s.setPosition(0,0,0);
  for(int type:new int[]{Integer.MIN_VALUE,0,1,2,3,Integer.MAX_VALUE})for(EntityNukeTorex.TorexType kind:new EntityNukeTorex.TorexType[]{EntityNukeTorex.TorexType.STANDARD,EntityNukeTorex.TorexType.RING,EntityNukeTorex.TorexType.STARFISH}){
   s.setType(type);EntityNukeTorex.Cloudlet cloud=s.new Cloudlet(0,3,0,0.75F,0,20,kind);double red,green,blue;
   if(kind==EntityNukeTorex.TorexType.STARFISH){red=16;green=3;blue=1;}else if(type==1){red=2;green=4;blue=1;}else if(type==2&&kind==EntityNukeTorex.TorexType.RING){red=2;green=2;blue=2;}else if(type==2){java.awt.Color rgb=java.awt.Color.getHSBColor(0.75F/2F/(float)Math.PI,1,1);red=rgb.getRed()/255D;green=rgb.getGreen()/255D;blue=rgb.getBlue()/255D;}else{red=4;green=3;blue=1;}
   check(cloud.color.x==red&&cloud.color.y==green&&cloud.color.z==blue,"complete inner Cloudlet constructor/color consumer uses its actual outer typed key");check(cloud.age==0&&cloud.cloudletLife==20&&cloud.type==kind&&cloud.prevColor==null,"complete cloudlet initialization order");
  }
  NBTTagCompound n=new NBTTagCompound();n.setString("sentinel","keep");nbtWrite(EntityNukeTorex.class,s,n);check(n.getKeySet().equals(Collections.singleton("sentinel"))&&!s.writeToNBTOptional(n),"unchanged Torex no-save policy");s.setSynchronizedScale(4);s.readEntityFromNBT(binary(n));check(s.isDead&&s.getScale()==4,"unchanged read-kills behavior without metadata reset");
 }
 public static void main(String[] args)throws Exception{
  net.minecraft.init.Bootstrap.register();String[] order=args[0].equals("projectiles-first")?new String[]{"com.hbm.entity.projectile.EntityChemical","com.hbm.entity.projectile.EntityFallingNuke","com.hbm.entity.effect.EntityDepress","com.hbm.entity.effect.EntityNukeTorex"}:new String[]{"com.hbm.entity.effect.EntityNukeTorex","com.hbm.entity.effect.EntityDepress","com.hbm.entity.projectile.EntityFallingNuke","com.hbm.entity.projectile.EntityChemical"};for(String n:order)Class.forName(n);
  W server=new W(false),client=new W(true);DataParameter<Integer> fluid=key(EntityChemical.class,"FLUID_ID"),color=key(EntityNukeTorex.class,"COLOR_TYPE");DataParameter<Byte> block=key(EntityFallingNuke.class,"BLOCK_METADATA"),dir=key(EntityDepress.class,"DIRECTION"),stuck=key(EntityThrowableNT.class,"STUCK_FACE");DataParameter<Float> scale=key(EntityNukeTorex.class,"SCALE");
  check(fluid.getSerializer()==DataSerializers.VARINT&&color.getSerializer()==DataSerializers.VARINT&&block.getSerializer()==DataSerializers.BYTE&&dir.getSerializer()==DataSerializers.BYTE&&scale.getSerializer()==DataSerializers.FLOAT,"true native serializer ownership");check(stuck.getId()==6&&fluid.getId()==7&&block.getId()==6&&dir.getId()==6&&scale.getId()==6&&color.getId()==7,"independent owner allocation across opposite class load orders");
  ObservedChemical a=new ObservedChemical(server);EntityChemical b=new EntityChemical(client);EntityFallingNuke n=new EntityFallingNuke(server),nr=new EntityFallingNuke(client);EntityDepress d=new EntityDepress(server),dr=new EntityDepress(client);EntityNukeTorex t=new EntityNukeTorex(server),tr=new EntityNukeTorex(client);
  check(ObservedChemical.inits==1&&ObservedChemical.uninitializedFlags&&a.ignoreFrustumCheck&&a.isImmuneToFire()&&a.width==0.25F&&a.height==0.25F,"complete Chemical/world-ancestor constructors and genuine native virtual init");
  layout(a,7,7);layout(n,7,6);layout(d,7,6);layout(t,8,6,7);for(EntityDataManager.DataEntry<?> e:a.getDataManager().getAll())check(e.getKey().getId()!=stuck.getId(),"Chemical intentionally omits inherited STUCK_FACE registration");
  check(a.getType()==Fluids.NONE&&a.getDataManager().get(fluid)==0&&n.getBlockMetadata()==0&&direction(d)==ForgeDirection.DOWN&&t.getScale()==1&&t.getDataManager().get(color)==0,"original exact hard-coded defaults");
  packet(a,b,true,7);packet(n,nr,true,7);packet(d,dr,true,7);packet(t,tr,true,8);bytes(n,nr,d,dr,block,dir);fluids(a,b,fluid);floatData(t,tr,scale,color);effects(server,client,dir);nukeNBT(server,client);torexBehavior(server,client);
  for(int meta=0;meta<16;meta++){server.state=Blocks.WOOL.getStateFromMeta(meta);ConsumerSlices.setFallingFromBlock(n,server,3,64,-4);check(server.lastRead.equals(new BlockPos(3,64,-4))&&n.getBlockMetadata()==(byte)meta&&ConsumerSlices.rendererLookup(n)==(byte)meta,"exact NukeCustom native-state-to-byte-to-renderer chain");}
  System.out.println("PASS "+checks+" native projectile/utility metadata assertions ("+args[0]+"); genuine data managers/full+dirty packet application, bounded source execution only");
 }
}
