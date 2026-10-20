import com.hbm.extprop.*;
import com.hbm.config.*;
import com.hbm.handler.HbmKeybinds.EnumKeybind;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.nbt.*;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.objectweb.asm.Type;

/** Native Entity constructors, EventBus/ForgeEventFactory, capabilities, real entity NBT and
 * compressed bytes. Full production provider + player class; living class excludes setDigamma.
 * No NTM game server, production radiation tick/network/potions, or advancement acceptance. */
public final class EntityPropertiesNativeTest {
 static int checks, constructions;
 static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 static void equal(Object a,Object b,String message){check(Objects.equals(a,b),message+" expected="+a+" actual="+b);}
 public static class BeforeAttachment {
  @SubscribeEvent public void constructing(EntityEvent.EntityConstructing event) {
   if(event.getEntity() instanceof EntityLivingBase){constructions++;check(event.getEntity().getCapability(HbmEntityProps.LIVING,null)==null,"EntityConstructing genuinely precedes attachment");}
  }
 }
 static class ProbeWorld extends World {
  SoundEvent lastSound;EntityPlayer excluded;int soundCount;float volume,pitch;
  ProbeWorld(boolean remote){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),remote);provider.setWorld(this);}
  protected IChunkProvider createChunkProvider(){return null;}
  protected boolean isChunkLoaded(int x,int z,boolean empty){return false;}
  public net.minecraft.block.state.IBlockState getBlockState(BlockPos p){return net.minecraft.init.Blocks.AIR.getDefaultState();}
  public void playSound(EntityPlayer player,double x,double y,double z,SoundEvent sound,SoundCategory category,float v,float p){lastSound=sound;excluded=player;soundCount++;volume=v;pitch=p;}
 }
 static class Player extends EntityPlayer {
  Player(World world){super(world,new GameProfile(new UUID(4,2),"PropertyTest"));}
  public boolean isSpectator(){return false;}public boolean isCreative(){return false;}
 }
 static class Living extends EntityLivingBase {
  int damageCalls;float damage;
  Living(World world){super(world);}
  public Iterable<net.minecraft.item.ItemStack> getArmorInventoryList(){return Collections.emptyList();}
  public net.minecraft.item.ItemStack getItemStackFromSlot(net.minecraft.inventory.EntityEquipmentSlot slot){return net.minecraft.item.ItemStack.EMPTY;}
  public void setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot slot,net.minecraft.item.ItemStack stack){}
  public EnumHandSide getPrimaryHand(){return EnumHandSide.RIGHT;}
  public boolean attackEntityFrom(DamageSource source,float amount){damageCalls++;damage=amount;return true;}
 }
 static class NonLiving extends Entity {
  NonLiving(World world){super(world);}protected void entityInit(){}protected void readEntityFromNBT(NBTTagCompound tag){}protected void writeEntityToNBT(NBTTagCompound tag){}
 }
 static Field field(Class<?> cls,String name)throws Exception{Field f=cls.getDeclaredField(name);f.setAccessible(true);return f;}
 static void set(Object target,String name,Object value)throws Exception{field(target.getClass(),name).set(target,value);}
 static Object get(Object target,String name)throws Exception{return field(target.getClass(),name).get(target);}
 static NBTTagCompound save(Entity entity){return entity.writeToNBT(new NBTTagCompound());}
 static void fill(Player entity)throws Exception {
  HbmLivingProps living=HbmLivingProps.getData(entity);
  String[] names={"radiation","digamma","asbestos","blacklung","radEnv","radBuf","bombTimer","contagion","oil","activation","oxygen","fire","phosphorus","balefire","blackFire","gravity"};
  Object[] values={123.25F,2.5F,321,432,17F,18F,9,8,7,6.5F,43,11,12,13,14,true};
  for(int i=0;i<names.length;i++)set(living,names[i],values[i]);
  HbmLivingProps.ContaminationEffect cont=new HbmLivingProps.ContaminationEffect(9.5F,20,true);cont.time=6;HbmLivingProps.addCont(entity,cont);
  HbmPlayerProps player=HbmPlayerProps.getData(entity);
  player.hasReceivedBook=true;player.shield=12.5F;player.maxShield=23.5F;player.nitanCount=7;
  player.enableBackpack=false;player.enableHUD=false;player.enableMagnet=false;player.reputation=81;player.isOnLadder=true;player.hasWarped=true;player.lastDimension=-28;
  player.dashCooldown=4;player.stamina=27;player.totalDashCount=3;player.plinkCooldown=9;player.lastDamage=12;
  player.setKeyPressed(EnumKeybind.JETPACK,true);
 }
 static void attachment()throws Exception {
  ProbeWorld world=new ProbeWorld(false);Player one=new Player(world),two=new Player(world);Living mob=new Living(world);NonLiving nonliving=new NonLiving(world);
  check(constructions>=3,"constructor posts genuinely dispatched event");
  for(EnumFacing face:EnumFacing.values()){
   check(one.hasCapability(HbmEntityProps.LIVING,face)&&one.hasCapability(HbmEntityProps.PLAYER,face),"player exposes both native capabilities on "+face);
   check(mob.hasCapability(HbmEntityProps.LIVING,face)&&!mob.hasCapability(HbmEntityProps.PLAYER,face),"nonplayer living only has living capability");
   check(!nonliving.hasCapability(HbmEntityProps.LIVING,face)&&!nonliving.hasCapability(HbmEntityProps.PLAYER,face),"nonliving attaches neither");
  }
  check(HbmLivingProps.getData(one)==HbmLivingProps.registerData(one),"reachable registerData reuses attached living identity");
  check(HbmPlayerProps.getData(one)==HbmPlayerProps.registerData(one),"reachable registerData reuses attached player identity");
  check(HbmLivingProps.getData(one)!=HbmLivingProps.getData(two)&&HbmPlayerProps.getData(one)!=HbmPlayerProps.getData(two),"distinct owners do not share payloads");
  check(HbmLivingProps.getData(one).entity==one&&HbmPlayerProps.getData(one).player==one,"payload owner references bound to native entity");
  check(HbmLivingProps.getOxy(one)==100&&HbmPlayerProps.getData(one).enableMagnet,"fresh original defaults");
  Player client=new Player(new ProbeWorld(true));check(HbmLivingProps.getData(client).entity==client&&HbmPlayerProps.getData(client).player==client,"native client-side construction attaches both capabilities too");
 }
 static void persistence()throws Exception {
  ProbeWorld world=new ProbeWorld(false);Player original=new Player(world);fill(original);
  NBTTagCompound saved=save(original),caps=saved.getCompoundTag("ForgeCaps");
  equal(new HashSet<>(Arrays.asList("hbm:living_props","hbm:player_props")),caps.getKeySet(),"native ForgeCaps provider envelope");
  NBTTagCompound expectedLiving=new NBTTagCompound(),expectedPlayer=new NBTTagCompound();
  HbmLivingProps.getData(original).saveNBTData(expectedLiving);HbmPlayerProps.getData(original).saveNBTData(expectedPlayer);
  equal(expectedLiving,caps.getCompoundTag("hbm:living_props"),"Forge invokes unchanged original living NBT payload");
  equal(expectedPlayer,caps.getCompoundTag("hbm:player_props"),"Forge invokes unchanged original player NBT payload");
  NBTTagCompound living=expectedLiving.getCompoundTag("HbmLivingProps"),player=expectedPlayer.getCompoundTag("HbmPlayerProps");
  equal(new HashSet<>(Arrays.asList("hfr_radiation","hfr_digamma","hfr_asbestos","hfr_bomb","hfr_contagion","hfr_blacklung","hfr_oil","hfr_oxygen","hfr_activation","hfr_gravity","hfr_fire","hfr_phosphorus","hfr_balefire","hfr_blackfire","hfr_cont_count","cont_0")),living.getKeySet(),"exact living save key set");
  equal(new HashSet<>(Arrays.asList("hasReceivedBook","shield","maxShield","nitan","enableBackpack","enableMagnet","enableHUD","reputation","isOnLadder","hasWarped","lastDimension")),player.getKeySet(),"exact player save key set");
  check(player.hasKey("nitan",5)&&player.getFloat("nitan")==7F,"original nitan float save remains float");
  check(living.getCompoundTag("cont_0").getInteger("maxTime")==20&&living.getCompoundTag("cont_0").getInteger("time")==6,"nested contamination save unchanged");
  ByteArrayOutputStream output=new ByteArrayOutputStream();CompressedStreamTools.writeCompressed(saved,output);
  NBTTagCompound disk=CompressedStreamTools.readCompressed(new ByteArrayInputStream(output.toByteArray()));Player restored=new Player(world);restored.readFromNBT(disk);
  HbmPlayerProps reloaded=HbmPlayerProps.getData(restored);HbmLivingProps reloadedLiving=HbmLivingProps.getData(restored);
  equal(123.25F,HbmLivingProps.getRadiation(restored),"radiation survives native entity disk reload");equal(2.5F,HbmLivingProps.getDigamma(restored),"persisted digamma loaded without setter side effects");
  equal(321,HbmLivingProps.getAsbestos(restored),"asbestos reload");equal(432,HbmLivingProps.getBlackLung(restored),"blacklung reload");equal(9,HbmLivingProps.getTimer(restored),"bomb reload");equal(8,HbmLivingProps.getContagion(restored),"contagion reload");equal(7,HbmLivingProps.getOil(restored),"oil reload");equal(6.5F,HbmLivingProps.getNeutronActivation(restored),"activation reload");equal(43,HbmLivingProps.getOxy(restored),"oxygen reload");
  check(HbmLivingProps.hasGravity(restored)&&reloadedLiving.fire==11&&reloadedLiving.phosphorus==12&&reloadedLiving.balefire==13&&reloadedLiving.blackFire==14,"living saved flags/fire restore");
  check(HbmLivingProps.getRadEnv(restored)==0&&HbmLivingProps.getRadBuf(restored)==0&&HbmLivingProps.getAtmosphere(restored)==null,"unsaved living transient fields reset naturally");
  check(reloaded.hasReceivedBook&&reloaded.shield==12.5F&&reloaded.maxShield==23.5F&&reloaded.nitanCount==7&&!reloaded.enableBackpack&&!reloaded.enableHUD&&!reloaded.enableMagnet&&reloaded.reputation==81&&reloaded.isOnLadder&&reloaded.hasWarped&&reloaded.lastDimension==-28,"all exact persisted player fields restore");
  check(reloaded.dashCooldown==0&&reloaded.stamina==0&&reloaded.totalDashCount==0&&reloaded.plinkCooldown==0&&reloaded.lastDamage==0&&!reloaded.getKeyPressed(EnumKeybind.JETPACK),"unsaved player fields reset naturally");
  HbmLivingProps.ContaminationEffect effect=HbmLivingProps.getCont(restored).get(0);
  check(effect.maxRad==9.5F&&effect.maxTime==0&&effect.time==0&&!effect.ignoreArmor,"original contamination load reads time/armor from parent, not silently fixed");
  restored.readFromNBT(disk);check(HbmLivingProps.getCont(restored).size()==2,"original repeated living load appends, not silently cleared");
  Player missing=new Player(world);missing.readFromNBT(save(new Player(world)));check(HbmLivingProps.getOxy(missing)==100&&HbmPlayerProps.getData(missing).enableHUD,"fresh native save default roundtrip");
  ServerConfig.ENABLE_MKU.value=false;NBTTagCompound noMku=save(original);check(!noMku.getCompoundTag("ForgeCaps").getCompoundTag("hbm:living_props").getCompoundTag("HbmLivingProps").hasKey("hfr_contagion"),"MKU disabled omits persisted key exactly");Player mkuDisabled=new Player(world);mkuDisabled.readFromNBT(disk);equal(0,get(HbmLivingProps.getData(mkuDisabled),"contagion"),"MKU disabled ignores saved contagion");ServerConfig.ENABLE_MKU.value=true;
 }
 static void wireAndClone()throws Exception {
  ProbeWorld world=new ProbeWorld(false);Player original=new Player(world);fill(original);
  ByteBuf expected=Unpooled.buffer(),actual=Unpooled.buffer();
  expected.writeFloat(123.25F).writeFloat(2.5F).writeInt(321).writeInt(9).writeInt(8).writeInt(432).writeInt(7).writeInt(43).writeBoolean(true).writeFloat(6.5F).writeInt(11).writeInt(12).writeInt(13).writeInt(1).writeFloat(9.5F).writeInt(20).writeInt(6).writeBoolean(true);
  HbmLivingProps.getData(original).serialize(actual);check(ByteBufUtil.equals(expected,actual),"independent original living wire byte order and values");
  Player networkTarget=new Player(world);HbmLivingProps.getData(networkTarget).deserialize(actual);
  check(actual.readableBytes()==0&&HbmLivingProps.getCont(networkTarget).get(0).time==6&&HbmLivingProps.getCont(networkTarget).get(0).ignoreArmor,"living wire decode consumes exact payload with original contamination values");check(HbmLivingProps.getData(networkTarget).blackFire==0,"blackfire deliberately omitted from original living wire");expected.release();actual.release();
  expected=Unpooled.buffer();actual=Unpooled.buffer();expected.writeBoolean(true).writeFloat(12.5F).writeFloat(23.5F).writeBoolean(false).writeBoolean(false).writeInt(81).writeBoolean(true).writeBoolean(false);
  HbmPlayerProps.getData(original).serialize(actual);check(ByteBufUtil.equals(expected,actual)&&actual.readableBytes()==17,"independent exact original eight-field player wire");expected.release();actual.release();
  original.setDead();check(HbmPlayerProps.getData(original).reputation==81,"1.12 capabilities remain readable on dead original, no modern revival shim");
  for(boolean wasDeath:new boolean[]{true,false}){
   Player copy=new Player(world);ForgeEventFactory.onPlayerClone(copy,original,wasDeath);HbmPlayerProps player=HbmPlayerProps.getData(copy);
   check(player.hasReceivedBook&&player.shield==12.5F&&player.maxShield==23.5F&&!player.enableBackpack&&!player.enableHUD&&player.reputation==81&&player.isOnLadder&&!player.enableMagnet,"native clone event copies exact wire subset, death="+wasDeath);
   check(player.nitanCount==0&&!player.hasWarped&&player.lastDimension==0&&player.dashCooldown==0&&player.stamina==0&&player.totalDashCount==0&&!player.getKeyPressed(EnumKeybind.JETPACK),"clone preserves original omissions, death="+wasDeath);
   check(HbmLivingProps.getRadiation(copy)==0&&HbmLivingProps.getDigamma(copy)==0&&HbmLivingProps.getCont(copy).isEmpty()&&HbmLivingProps.getOxy(copy)==100,"clone does NOT copy living properties, death="+wasDeath);
   check(player.player==copy&&HbmLivingProps.getData(copy).entity==copy,"clone provider owner remains new entity");
  }
 }
 static void formulasAndPlayerMethods()throws Exception {
  ProbeWorld world=new ProbeWorld(false);Living entity=new Living(world);
  HbmLivingProps.setRadiation(entity,2490);HbmLivingProps.incrementRadiation(entity,25);equal(2500F,HbmLivingProps.getRadiation(entity),"radiation increment upper clamp");HbmLivingProps.incrementRadiation(entity,-3000);equal(0F,HbmLivingProps.getRadiation(entity),"radiation increment lower clamp");HbmLivingProps.setRadiation(entity,3000);equal(3000F,HbmLivingProps.getRadiation(entity),"direct radiation setter unchanged, no invented clamp");
  RadiationConfig.enableContamination=false;HbmLivingProps.incrementRadiation(entity,5);HbmLivingProps.setRadiation(entity,4);equal(0F,HbmLivingProps.getRadiation(entity),"disabled radiation read");equal(3000F,get(HbmLivingProps.getData(entity),"radiation"),"disabled radiation writes don't alter payload");RadiationConfig.enableContamination=true;
  HbmLivingProps.setNeutronActivation(entity,4);HbmLivingProps.incrementNeutronActivation(entity,-8);equal(0F,HbmLivingProps.getNeutronActivation(entity),"neutron lower clamp");RadiationConfig.disableNeutron=true;HbmLivingProps.setNeutronActivation(entity,5);equal(0F,HbmLivingProps.getNeutronActivation(entity),"disabled neutron gate");RadiationConfig.disableNeutron=false;
  HbmLivingProps.setAsbestos(entity,HbmLivingProps.maxAsbestos);check(HbmLivingProps.getAsbestos(entity)==0&&entity.damage==1000,"original asbestos threshold reset and damage");HbmLivingProps.setBlackLung(entity,HbmLivingProps.maxBlacklung);check(HbmLivingProps.getBlackLung(entity)==0&&entity.damageCalls==2,"original blacklung threshold");
  entity.ticksExisted=40;HbmLivingProps.setOxy(entity,-600);check(entity.damage==5&&HbmLivingProps.getOxy(entity)==-600,"original oxygen clamp/formula");entity.ticksExisted=8;HbmLivingProps.setOxy(entity,-600);check(entity.damage==1&&HbmLivingProps.getOxy(entity)==0,"early relog oxygen limit");
  Player player=new Player(world);HbmPlayerProps props=HbmPlayerProps.getData(player);props.setKeyPressed(EnumKeybind.TOGGLE_MAGNET,true);check(!props.enableMagnet,"magnet edge toggle");props.setKeyPressed(EnumKeybind.TOGGLE_MAGNET,true);check(!props.enableMagnet,"held key does not toggle again");props.setKeyPressed(EnumKeybind.TOGGLE_MAGNET,false);props.setKeyPressed(EnumKeybind.TOGGLE_MAGNET,true);check(props.enableMagnet,"second edge toggles");
  props.maxShield=10;equal(10F,props.getEffectiveMaxShield(),"native EMPTY chest keeps original effective shield");
  HbmPlayerProps.plink(player,"random.break",.5F,1.25F);check(world.soundCount==1&&world.lastSound==net.minecraft.init.SoundEvents.ENTITY_ITEM_BREAK&&world.excluded==null&&world.volume==.5F&&world.pitch==1.25F&&props.plinkCooldown==10,"vanilla item break native sound mapping broadcasts and retains cooldown");HbmPlayerProps.plink(player,"random.break",1,1);check(world.soundCount==1,"plink cooldown suppresses second sound");
 }
 public static void main(String[] args)throws Exception {
  Bootstrap.register();FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());ModMetadata meta=new ModMetadata();meta.modId="hbm";meta.name="Native property lifecycle harness";meta.version="probe";DummyModContainer owner=new DummyModContainer(meta);Loader.instance().setupTestHarness(owner);Loader.instance().setActiveModContainer(owner);
  // Native player save requests the server's CompoundDataFixer. Allocate only a
  // real native host object, without starting a server/network or replacing save code.
  Field unsafeField=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");unsafeField.setAccessible(true);
  Object server=((sun.misc.Unsafe)unsafeField.get(null)).allocateInstance(net.minecraft.server.dedicated.DedicatedServer.class);
  field(net.minecraft.server.MinecraftServer.class,"dataFixer").set(server,net.minecraft.util.datafix.DataFixesManager.createFixer());
  field(net.minecraftforge.fml.server.FMLServerHandler.class,"server").set(net.minecraftforge.fml.server.FMLServerHandler.instance(),server);
  ASMDataTable table=new ASMDataTable();for(Class<?> type:new Class<?>[]{HbmLivingProps.class,HbmPlayerProps.class}){
   Map<String,Object> values=new HashMap<>();values.put("value",Type.getType(type));table.addASMData(null,CapabilityInject.class.getName(),HbmEntityProps.class.getName(),type==HbmLivingProps.class?"LIVING":"PLAYER",values);
  }
  CapabilityManager.INSTANCE.injectCapabilities(table);HbmEntityProps.register();check(HbmEntityProps.LIVING!=null&&HbmEntityProps.PLAYER!=null,"actual native CapabilityManager annotation injection");MinecraftForge.EVENT_BUS.register(new BeforeAttachment());
  attachment();persistence();wireAndClone();formulasAndPlayerMethods();
  System.out.println("PASS "+checks+" native property lifecycle checks: real transformed Forge event dispatch/Entity constructor attachment; exact native ForgeCaps save/compressed reload; original NBT quirks and wire; death/nondeath clone omissions; selected radiation formulas; full production player class. Living setDigamma, production death handler, radiation ticks/network/achievements and loaded-game acceptance remain OPEN.");
 }
}
