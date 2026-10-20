import com.hbm.main.ModEventHandlerClient;
import com.hbm.sound.*;
import com.hbm.sound.MovingSoundPlayerLoop.EnumHbmSound;
import com.hbm.entity.mob.EntityHunterChopper;
import com.hbm.entity.projectile.EntityChopperMine;
import com.hbm.entity.train.EntityRailCarRidable;
import com.hbm.util.Library;
import com.hbm.items.ItemCustomLore;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import io.netty.buffer.Unpooled;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.*;
import net.minecraft.client.audio.*;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.entity.Entity;
import net.minecraft.init.*;
import net.minecraft.network.*;
import net.minecraft.network.play.client.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.gameevent.TickEvent.*;
import net.minecraftforge.fml.relauncher.Side;
import sun.misc.Unsafe;

/** Native types and event dispatch, isolated NTM collaborator and hardware boundaries. */
public final class ClientSoundTrainNativeTest {
 static final Unsafe U;static int checks;static Minecraft mc;static RecordingHandler handler;static TestWorld world;static EntityPlayerSP player;static RecordingConnection connection;
 static {try{Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);U=(Unsafe)f.get(null);}catch(Exception e){throw new ExceptionInInitializerError(e);}}
 static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 static void set(Object o,Class<?> c,String n,Object v)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);f.set(o,v);}
 static final class TestWorld extends WorldClient {
  IBlockState state;BlockPos queried;
  TestWorld(){super(null,null,0,null,null);}
  @Override public IBlockState getBlockState(BlockPos p){queried=p;return state;}
 }
 static final class RecordingHandler extends SoundHandler {
  Set<ISound> playing=new HashSet<>();int starts;
  RecordingHandler(IResourceManager r){super(r,null);}
  @Override public boolean isSoundPlaying(ISound s){return playing.contains(s);}
  @Override public void playSound(ISound s){starts++;playing.add(s);}
 }
 static final class RecordingConnection extends NetHandlerPlayClient {
  List<Packet<?>> packets;
  RecordingConnection(){super(null,null,null,null);}
  @Override public void sendPacket(Packet<?> p){packets.add(p);}
 }
 public static final class Observer {
  Event.Result result;ISound sound;
  @SubscribeEvent(priority=EventPriority.LOWEST) public void play(PlaySoundEvent e){result=e.getResult();sound=e.getResultSound();}
 }
 static ISound sound(String name){return new PositionedSoundRecord(new ResourceLocation(name),SoundCategory.MASTER,1,1,false,0,ISound.AttenuationType.NONE,3,4,5);}
 static PlaySoundEvent post(String name){PlaySoundEvent e=new PlaySoundEvent(null,sound(name));MinecraftForge.EVENT_BUS.post(e);return e;}
 public static void main(String[] args)throws Exception{
  Bootstrap.register();net.minecraftforge.fml.common.ModMetadata md=new net.minecraftforge.fml.common.ModMetadata();md.modId="hbm";md.name="Native client sound/train probe";md.version="probe";net.minecraftforge.fml.common.DummyModContainer owner=new net.minecraftforge.fml.common.DummyModContainer(md);net.minecraftforge.fml.common.Loader.instance().setupTestHarness(owner);net.minecraftforge.fml.common.Loader.instance().setActiveModContainer(owner);SimpleReloadableResourceManager rm=new SimpleReloadableResourceManager(new MetadataSerializer());rm.reloadResourcePack(new FolderResourcePack(new File(args[0])));rm.registerReloadListener(new SoundCategoryResolver());
  mc=(Minecraft)U.allocateInstance(Minecraft.class);set(null,Minecraft.class,"instance",mc);handler=new RecordingHandler(rm);set(mc,Minecraft.class,"soundHandler",handler);
  world=(TestWorld)U.allocateInstance(TestWorld.class);world.state=Blocks.AIR.getDefaultState();mc.world=world;player=(EntityPlayerSP)U.allocateInstance(EntityPlayerSP.class);mc.player=player;
  connection=(RecordingConnection)U.allocateInstance(RecordingConnection.class);connection.packets=new ArrayList<>();set(player,EntityPlayerSP.class,"connection",connection);player.movementInput=new MovementInput();
  ModEventHandlerClient listener=new ModEventHandlerClient();Observer observer=new Observer();MinecraftForge.EVENT_BUS.register(listener);MinecraftForge.EVENT_BUS.register(observer);
  check(MusicTicker.MusicType.CREATIVE.getMusicLocation().getSoundName().equals(new ResourceLocation("minecraft:music.creative")),"native creative music key");
  music(observer);categoryReload(rm,args[0]);loops();train();resources(rm);
  try(FileResourcePack zip=new FileResourcePack(new File(args[1]))){SimpleReloadableResourceManager jarResources=new SimpleReloadableResourceManager(new MetadataSerializer());jarResources.reloadResourcePack(zip);resources(jarResources);}
  legacyPackets(args[2]);
  MinecraftForge.EVENT_BUS.unregister(listener);MinecraftForge.EVENT_BUS.unregister(observer);
  System.out.println("PASS native client sound/train: "+checks+" checks; genuine Forge dispatch/hook, actual production moving sounds, native serialized packets; extracted methods only, no launched client/audio/server");
 }
 static void provider(WorldProvider p,int dim)throws Exception{p.setDimension(dim);set(world,World.class,"provider",p);}
 static void music(Observer observer)throws Exception{
  provider(new WorldProviderSurface(),0);PlaySoundEvent e=post("minecraft:music.game");check(e.getResultSound()==e.getSound(),"Earth unchanged");
  mc.world=null;e=post("minecraft:music.game");check(e.getResultSound()==e.getSound(),"no world unchanged");mc.world=world;
  provider(new com.hbm.dim.WorldProviderCelestial(),0);e=post("minecraft:music.game");check(e.getResultSound()==e.getSound(),"celestial dimension zero unchanged");
  provider(new WorldProviderSurface(),2);e=post("minecraft:music.game");check(e.getResultSound()==e.getSound(),"noncelestial nonzero unchanged");
  for(WorldProvider p:Arrays.asList(new com.hbm.dim.WorldProviderCelestial(),new com.hbm.dim.orbit.WorldProviderOrbit())){
   provider(p,42);
   for(String name:Arrays.asList("minecraft:music.game","minecraft:music.creative")){
    e=post(name);ISound music=e.getResultSound();check(music!=e.getSound()&&music.getSoundLocation().equals(new ResourceLocation("hbm:music.game.space")),"space replacement "+name);
    check(music.getCategory()==SoundCategory.MUSIC&&!music.canRepeat()&&music.getRepeatDelay()==0&&music.getAttenuationType()==ISound.AttenuationType.NONE,"original music defaults");check(music.getXPosF()==0&&music.getYPosF()==0&&music.getZPosF()==0,"music origin");
    handler.playing.add(music);e=post(name);check(e.getResultSound()==null&&e.getResult()==Event.Result.DENY,"overlap suppression plus DENY");check(observer.result==Event.Result.DENY&&observer.sound==null,"downstream result observer preserved");
    check(ForgeHooksClient.playSound(null,sound(name))==null,"real native hook returns null");e=post("minecraft:music.menu");check(e.getResultSound()==e.getSound(),"unrelated music unaffected by overlap");handler.playing.clear();
   }
  }
  player.posX=-0.1;player.posY=4.99;player.posZ=-2.1;world.state=Blocks.BARRIER.getDefaultState();e=post("hbm:misc.nullChopper");check(e.getResultSound()==null&&Library.calls==0,"vacuum returns before entity lookup");check(world.queried.equals(new BlockPos(-1,4,-3)),"vacuum floor coordinates");world.state=Blocks.AIR.getDefaultState();
 }
 static void categoryReload(SimpleReloadableResourceManager rm,String original)throws Exception{
  ISound held=post("minecraft:music.game").getResultSound();check(held.getCategory()==SoundCategory.MUSIC,"initial replacement category from original metadata");
  java.nio.file.Path root=java.nio.file.Files.createTempDirectory("music-category-");java.nio.file.Path json=root.resolve("assets/hbm/sounds.json");java.nio.file.Files.createDirectories(json.getParent());java.nio.file.Files.write(json,"{\"music.game.space\":{\"category\":\"record\",\"replace\":true,\"sounds\":[]}}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
  rm.reloadResources(Arrays.asList(new FolderResourcePack(root.toFile())));check(held.getCategory()==SoundCategory.RECORDS,"already-created replacement follows category reload");
  rm.reloadResources(Collections.emptyList());check(held.getCategory()==SoundCategory.MASTER,"removed metadata uses original default");
  rm.reloadResources(Arrays.asList(new FolderResourcePack(new File(original))));check(held.getCategory()==SoundCategory.MUSIC,"original category restored on existing replacement");
 }
 static void loops(){
  String[] triggers={"misc.nullChopper","misc.nullCrashing","misc.nullMine"};String[] loops={"entity.chopperFlyingLoop","entity.chopperCrashingLoop","entity.chopperMineLoop"};EnumHbmSound[] kinds={EnumHbmSound.soundChopperLoop,EnumHbmSound.soundCrashingLoop,EnumHbmSound.soundMineLoop};
  for(int i=0;i<3;i++){
   MovingSoundPlayerLoop.globalSoundList.clear();Library.chopper=null;Library.mine=null;Library.calls=0;int starts=handler.starts;
   PlaySoundEvent e=post("hbm:"+triggers[i]);check(MovingSoundPlayerLoop.globalSoundList.isEmpty()&&handler.starts==starts,"no target no loop");check(e.getResultSound()==e.getSound(),"trigger sound not consumed");
   Library.chopper=new EntityHunterChopper();Library.mine=new EntityChopperMine();Entity target=i==2?Library.mine:Library.chopper;Library.calls=0;
   e=post("hbm:"+triggers[i]);MovingSoundPlayerLoop s=MovingSoundPlayerLoop.getSoundByPlayer(target,kinds[i]);check(s!=null,"normalized trigger matches original camel-case key");check(s.getSoundLocation().equals(new ResourceLocation("hbm:"+loops[i]))&&s.getVolume()==10&&s.getCategory()==SoundCategory.HOSTILE,"actual loop key volume/category");check(Library.calls==2&&Library.x==3&&Library.y==4&&Library.z==5&&Library.range==2,"original duplicate lookup and xyz/range");
   check(MovingSoundPlayerLoop.globalSoundList.size()==2&&MovingSoundPlayerLoop.globalSoundList.get(0)==MovingSoundPlayerLoop.globalSoundList.get(1),"original constructor + caller duplicate registration retained");check(handler.starts==starts+1&&s.init&&!s.isDonePlaying(),"duplicate list starts once");
   post("hbm:"+triggers[i]);check(MovingSoundPlayerLoop.globalSoundList.size()==2&&handler.starts==starts+1,"existing loop not duplicated/restarted");s.setDone(true);post("hbm:misc.rumble");check(handler.starts==starts+2&&!s.isDonePlaying(),"any nonvacuum sound revives done loop");
   world.state=Blocks.BARRIER.getDefaultState();s.setDone(true);post("hbm:misc.rumble");check(handler.starts==starts+2&&s.isDonePlaying(),"vacuum skips restart pass");world.state=Blocks.AIR.getDefaultState();
  }
  MovingSoundPlayerLoop.globalSoundList.clear();Library.chopper=null;Library.mine=null;
 }
 static void tick(Phase phase){MinecraftForge.EVENT_BUS.post(new WorldTickEvent(Side.CLIENT,phase,world));}
 static void train()throws Exception{
  EntityRailCarRidable train=new EntityRailCarRidable();set(player,Entity.class,"ridingEntity",train);player.rotationYaw=-72.125f;player.rotationPitch=31.5f;player.onGround=true;player.moveStrafing=-0.75f;player.moveForward=0.25f;player.movementInput.jump=true;player.movementInput.sneak=false;
  for(Phase phase:Phase.values()){
   connection.packets.clear();int lore=ItemCustomLore.calls;tick(phase);check(connection.packets.size()==2,"train packets both original phases "+phase);check(ItemCustomLore.calls==lore+(phase==Phase.END?1:0),"lore END only");
   check(connection.packets.get(0) instanceof CPacketPlayer.Rotation&&connection.packets.get(1) instanceof CPacketInput,"look then input exact order");
   PacketBuffer b=new PacketBuffer(Unpooled.buffer());connection.packets.get(0).writePacketData(b);check(b.readableBytes()==9&&b.readFloat()==-72.125f&&b.readFloat()==31.5f&&b.readUnsignedByte()==1&&!b.isReadable(),"rotation exact wire yaw pitch grounded");b.release();
   for(int bits=0;bits<4;bits++){player.movementInput.jump=(bits&1)!=0;player.movementInput.sneak=(bits&2)!=0;connection.packets.clear();tick(phase);b=new PacketBuffer(Unpooled.buffer());connection.packets.get(1).writePacketData(b);check(b.readableBytes()==9&&b.readFloat()==-0.75f&&b.readFloat()==0.25f&&b.readUnsignedByte()==bits&&!b.isReadable(),"input wire strafe forward jump/sneak bits "+bits);b.release();}
  }
  train.sit=true;connection.packets.clear();tick(Phase.START);check(connection.packets.isEmpty(),"sitting train skips compensation");set(player,Entity.class,"ridingEntity",null);tick(Phase.END);check(connection.packets.isEmpty(),"unmounted skips compensation");mc.player=null;tick(Phase.END);check(connection.packets.isEmpty(),"null player skips compensation");mc.player=player;
 }
 static byte[] wire(Packet<?> packet)throws Exception{PacketBuffer b=new PacketBuffer(Unpooled.buffer());try{packet.writePacketData(b);byte[] bytes=new byte[b.readableBytes()];b.readBytes(bytes);return bytes;}finally{b.release();}}
 static void legacyPackets(String jar)throws Exception{
  try(java.net.URLClassLoader old=new java.net.URLClassLoader(new java.net.URL[]{new File(jar).toURI().toURL()},ClientSoundTrainNativeTest.class.getClassLoader())){
   Class<?> buffer=Class.forName("et",true,old),rotation=Class.forName("jg",true,old),input=Class.forName("jk",true,old);Random random=new Random(0xC03C0C);
   for(int i=0;i<200;i++){
    float x=random.nextFloat()*720-360,y=random.nextFloat()*180-90;boolean jump=(i&1)!=0,sneak=(i&2)!=0;
    Object[] original={rotation.getConstructor(float.class,float.class,boolean.class).newInstance(x,y,jump),input.getConstructor(float.class,float.class,boolean.class,boolean.class).newInstance(x,y,jump,sneak)};
    Packet<?>[] nativePackets={new CPacketPlayer.Rotation(x,y,jump),new CPacketInput(x,y,jump,sneak)};
    for(int j=0;j<2;j++){io.netty.buffer.ByteBuf b=Unpooled.buffer();try{Object wrapper=buffer.getConstructor(io.netty.buffer.ByteBuf.class).newInstance(b);original[j].getClass().getMethod("b",buffer).invoke(original[j],wrapper);byte[] bytes=new byte[b.readableBytes()];b.readBytes(bytes);if(j==0)check(Arrays.equals(bytes,wire(nativePackets[j])),"unchanged 1.7.10 rotation bytecode parity sample "+i);
     else {PacketBuffer legacy=new PacketBuffer(Unpooled.wrappedBuffer(bytes));PacketBuffer modern=new PacketBuffer(Unpooled.wrappedBuffer(wire(nativePackets[j])));try{check(legacy.readableBytes()==10&&modern.readableBytes()==9,"intentional vanilla input protocol size change");check(legacy.readFloat()==modern.readFloat()&&legacy.readFloat()==modern.readFloat(),"legacy/native strafe forward wire order");boolean oldJump=legacy.readBoolean(),oldSneak=legacy.readBoolean();int bits=modern.readUnsignedByte();check(oldJump==((bits&1)!=0)&&oldSneak==((bits&2)!=0)&&!legacy.isReadable()&&!modern.isReadable(),"legacy separate booleans map to native flag bits without changing input");}finally{legacy.release();modern.release();}}}finally{b.release();}}
   }
  }
 }
 @SuppressWarnings("unchecked") static void resources(IResourceManager rm)throws Exception{
  Method parse=SoundHandler.class.getDeclaredMethod("getSoundMap",InputStream.class);parse.setAccessible(true);Method load=SoundHandler.class.getDeclaredMethod("loadSoundResource",ResourceLocation.class,SoundList.class);load.setAccessible(true);
  Map<String,SoundList> defs;try(IResource resource=rm.getResource(new ResourceLocation("hbm:sounds.json"))){defs=(Map<String,SoundList>)parse.invoke(handler,resource.getInputStream());}
  for(String n:Arrays.asList("misc.nullChopper","misc.nullCrashing","misc.nullMine","entity.chopperFlyingLoop","entity.chopperCrashingLoop","entity.chopperMineLoop","music.game.space")){
   ResourceLocation key=new ResourceLocation("hbm:"+n);load.invoke(handler,key,defs.get(n));check(handler.getAccessor(key)!=null,"native sounds.json parser resolves "+n);
   for(Sound sample:defs.get(n).getSounds())try(IResource res=rm.getResource(sample.getSoundAsOggLocation())){InputStream in=res.getInputStream();check(in.read()=='O'&&in.read()=='g'&&in.read()=='g'&&in.read()=='S',"normalized sample actual OGG "+sample.getSoundAsOggLocation());}
  }
 }
}
