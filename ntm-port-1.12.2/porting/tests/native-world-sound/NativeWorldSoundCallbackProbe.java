import com.hbm.main.ModSoundEvents;
import com.hbm.sound.WorldSoundEffects;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.*;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import sun.misc.Unsafe;

/** Real production helper + native listeners, packet, WorldServer, server and PlayerList.
 * Only world startup/chunks and socket delivery are controlled boundaries; no native API stub.
 */
public final class NativeWorldSoundCallbackProbe {
    static final Unsafe U;
    static { try { Field f=Unsafe.class.getDeclaredField("theUnsafe"); f.setAccessible(true); U=(Unsafe)f.get(null); }catch(Exception e){throw new ExceptionInInitializerError(e);} }
    static int checks;
    static void check(boolean ok,String msg){checks++;if(!ok)throw new AssertionError(msg);}
    static void eq(float a,float b,String msg){check(Float.floatToIntBits(a)==Float.floatToIntBits(b),msg+": "+a+" != "+b);}
    static void eq(double a,double b,String msg){check(Double.doubleToLongBits(a)==Double.doubleToLongBits(b),msg+": "+a+" != "+b);}
    static void set(Object o,Class<?> type,String name,Object value)throws Exception {Field f=type.getDeclaredField(name);f.setAccessible(true);f.set(o,value);}
    static <T>T alloc(Class<T> c)throws Exception{return c.cast(U.allocateInstance(c));}
    static float wirePitch(float pitch){return Math.max(0,Math.min(255,(int)(pitch*63F)))/63F;}
    public static final class WorldBoundary extends World {
        WorldBoundary(){super(null,null,null,null,false);}
        void replaceListeners(List<IWorldEventListener> replacement){eventListeners=replacement;}
        protected IChunkProvider createChunkProvider(){throw new AssertionError("chunk access");}
        protected boolean isChunkLoaded(int x,int z,boolean allowEmpty){throw new AssertionError("chunk access");}
        @Override public void playSound(EntityPlayer player,double x,double y,double z,SoundEvent sound,SoundCategory category,float volume,float pitch){throw new AssertionError("Native World.playSound must not be invoked");}
    }
    public static class RecordingPlayerList extends net.minecraft.server.dedicated.DedicatedPlayerList {
        Object excluded; double x,y,z,radius; int dimension,calls; Packet<?> packet;
        RecordingPlayerList(){super(null);}
        @Override public void sendToAllNearExcept(EntityPlayer excluded,double x,double y,double z,double radius,int dimension,Packet<?> packet){this.excluded=excluded;this.x=x;this.y=y;this.z=z;this.radius=radius;this.dimension=dimension;this.packet=packet;calls++;}
    }
    public static final class DispatchPlayerList extends PlayerList {DispatchPlayerList(){super(null);}}
    public static final class CaptureConnection extends NetHandlerPlayServer {
        int sends; Packet<?> packet;
        CaptureConnection(){super(null,null,null);}
        @Override public void sendPacket(Packet<?> packet){sends++;this.packet=packet;}
    }
    public static final class SubclassObserver extends ServerWorldEventHandler {
        int calls; float pitch,volume; EntityPlayer excluded; SoundEvent sound;
        SubclassObserver(){super(null,null);}
        @Override public void playSoundToAllNearExcept(EntityPlayer p,SoundEvent s,SoundCategory category,double x,double y,double z,float volume,float pitch){calls++;this.pitch=pitch;this.volume=volume;this.excluded=p;this.sound=s;}
    }
    public static final class InheritedServerHandler extends ServerWorldEventHandler {
        InheritedServerHandler(MinecraftServer server,WorldServer world){super(server,world);}
    }
    static final List<String> evaluation=new ArrayList<>();
    static World evaluateWorld(World world){evaluation.add("receiver");return world;}
    static double evaluateDouble(String label,double value){evaluation.add(label);return value;}
    static String evaluateName(){evaluation.add("name");return "hbm:block.crateOpen";}
    static float evaluateFloat(String label,float value){evaluation.add(label);return value;}
    static double throwY(){evaluation.add("y");throw new IllegalStateException("argument Y");}
    static void evaluationTests()throws Exception {
        List<String> expected=Arrays.asList("receiver","x","y","z","name","volume","pitch");
        for(boolean nullReceiver:new boolean[]{false,true}){
            List<Object[]> calls=new ArrayList<>();WorldBoundary world=world(new ArrayList<>(Arrays.asList(listener(a->calls.add(a)))),false);evaluation.clear();boolean failed=false;
            try {WorldSoundEffects.play(evaluateWorld(nullReceiver?null:world),evaluateDouble("x",1),evaluateDouble("y",2),evaluateDouble("z",3),evaluateName(),evaluateFloat("volume",.5F),evaluateFloat("pitch",.8F));}catch(NullPointerException correct){failed=true;}
            check(evaluation.equals(expected),"all original expressions evaluated once before receiver check: "+evaluation);check(failed==nullReceiver,"null receiver exception timing");check(calls.size()==(nullReceiver?0:1),"one or zero callbacks after evaluation");
            evaluation.clear();failed=false;
            try {WorldSoundEffects.play(evaluateWorld(nullReceiver?null:world),evaluateDouble("x",1),throwY(),evaluateDouble("z",3),evaluateName(),evaluateFloat("volume",.5F),evaluateFloat("pitch",.8F));}catch(IllegalStateException correct){check(correct.getMessage().equals("argument Y"),"original argument exception wins");failed=true;}
            check(failed&&evaluation.equals(Arrays.asList("receiver","x","y")),"middle argument failure prevents subsequent expressions");check(calls.size()==(nullReceiver?0:1),"argument failure produces no extra callback");
        }
    }
    public static final class EntityHookObserver {int calls;@SubscribeEvent public void observe(PlaySoundAtEntityEvent event){calls++;}}
    interface Invocation {void call(Object[] args);}
    static IWorldEventListener listener(final Invocation run){return (IWorldEventListener)Proxy.newProxyInstance(NativeWorldSoundCallbackProbe.class.getClassLoader(),new Class[]{IWorldEventListener.class},(p,m,a)->{if(m.getName().equals("playSoundToAllNearExcept")){run.call(a);return null;}if(m.getName().equals("equals"))return p==a[0];if(m.getName().equals("hashCode"))return System.identityHashCode(p);if(m.getName().equals("toString"))return "Native sound observer";throw new AssertionError("Unexpected callback "+m);});}
    static WorldBoundary world(List<IWorldEventListener> listeners,boolean remote)throws Exception {WorldBoundary world=alloc(WorldBoundary.class);set(world,World.class,"eventListeners",listeners);set(world,World.class,"isRemote",remote);return world;}
    static void play(World world,float volume,float pitch){WorldSoundEffects.play(world,-0.249D,17.03125D,30000000.125D,"hbm:block.crateOpen",volume,pitch);}
    static void failures()throws Exception {
        boolean failed=false;try{play(null,1F,1F);}catch(NullPointerException correct){failed=true;}check(failed,"null World fail-fast, no silent loss");
        for(String invalid:new String[]{null,"hbm:not_an_approved_world_sound"}) {failed=false;try{WorldSoundEffects.play(world(new ArrayList<>(),false),0,0,0,invalid,1,1);}catch(RuntimeException correct){failed=true;}check(failed,"unsupported/null literal rejected");}
    }
    static void observerTests(SoundEvent registered)throws Exception {
        EntityHookObserver entityHook=new EntityHookObserver();MinecraftForge.EVENT_BUS.register(entityHook);
        for(boolean remote:new boolean[]{false,true}) {
            List<IWorldEventListener> live=new ArrayList<>();WorldBoundary world=world(live,remote);
            play(world,.8F,.799999F); // Empty real World list must be silent, with no WorldServer cast.
            List<Object[]> observed=new ArrayList<>();IWorldEventListener first=listener(a->observed.add(a));live.add(first);live.add(first);
            live.add(alloc(RenderGlobal.class)); // Exact native RenderGlobal no-op, even with no Minecraft/player.
            SubclassObserver subclass=alloc(SubclassObserver.class);live.add(subclass);
            float pitch=.799999F;play(world,.8F,pitch);
            check(observed.size()==2,"duplicate callbacks preserved on remote="+remote);check(subclass.calls==1,"custom native ServerWorldEventHandler subclass called");eq(subclass.pitch,pitch,"subclass raw pitch");
            for(Object[] a:observed){check(a[0]==null,"no excluded player");check(a[1]==registered,"registered SoundEvent identity");check(a[2]==SoundCategory.BLOCKS,"original bundled category");eq((double)a[3],-.249D,"raw callback x");eq((double)a[4],17.03125D,"raw callback y");eq((double)a[5],30000000.125D,"raw callback z");eq((float)a[6],.8F,"raw callback volume");eq((float)a[7],pitch,"raw callback pitch");}
            live.clear();List<String> order=new ArrayList<>();IWorldEventListener second=listener(a->order.add("second"));IWorldEventListener third=listener(a->order.add("third"));IWorldEventListener fourth=listener(a->order.add("fourth"));
            final IWorldEventListener[] mutation=new IWorldEventListener[1];mutation[0]=listener(a->{order.add("first");live.remove(mutation[0]);live.add(fourth);});live.add(mutation[0]);live.add(second);live.add(third);play(world,1,1);
            check(order.equals(Arrays.asList("first","third","fourth")),"live indexed mutation ordering "+order);
            live.clear();order.clear();List<IWorldEventListener> replacement=new ArrayList<>();replacement.add(listener(a->order.add("new-zero-must-skip")));replacement.add(listener(a->order.add("new-one")));replacement.add(listener(a->order.add("new-two")));
            live.add(listener(a->{order.add("old-zero");world.replaceListeners(replacement);}));live.add(listener(a->order.add("old-one-must-not-run")));play(world,1,1);
            check(order.equals(Arrays.asList("old-zero","new-one","new-two")),"World-declared listener field replacement observed during loop: "+order);
        }
        check(entityHook.calls==0,"no newly introduced Forge PlaySoundAtEntityEvent");MinecraftForge.EVENT_BUS.unregister(entityHook);failures();evaluationTests();
    }
    static void nativeServerTests(SoundEvent registered)throws Exception {
        DedicatedServer server=alloc(DedicatedServer.class);RecordingPlayerList players=alloc(RecordingPlayerList.class);server.setPlayerList(players);
        WorldServer nativeWorld=alloc(WorldServer.class);set(nativeWorld,WorldServer.class,"server",server);
        WorldProviderSurface provider=alloc(WorldProviderSurface.class);provider.setDimension(-7);set(nativeWorld,World.class,"provider",provider);
        check(nativeWorld.getMinecraftServer()==server,"real WorldServer.getMinecraftServer resolves field");
        ServerWorldEventHandler builtin=new ServerWorldEventHandler(server,nativeWorld);
        SubclassObserver subclass=alloc(SubclassObserver.class);List<Object[]> observers=new ArrayList<>();
        WorldBoundary caller=world(new ArrayList<>(Arrays.asList(listener(a->observers.add(a)),builtin,subclass)),false);
        float[] values={Float.NEGATIVE_INFINITY,-1F,-0F,0F,.5F,.7F,.799999F,1F,Math.nextUp(1F),2F,16777217F,Float.MAX_VALUE,Float.POSITIVE_INFINITY,Float.NaN};
        int invocations=0;
        for(float volume:values)for(float pitch:values){play(caller,volume,pitch);invocations++;check(players.calls==invocations,"one native broadcast");check(players.excluded==null,"native broadcast excludes nobody");eq(players.x,-.249D,"unquantized radius-origin x");eq(players.y,17.03125D,"unquantized radius-origin y");eq(players.z,30000000.125D,"unquantized radius-origin z");eq(players.radius,volume>1F?(double)(16F*volume):16D,"exact float-first radius");check(players.dimension==-7,"provider dimension");check(players.packet.getClass()==SPacketSoundEffect.class,"real native registered packet");SPacketSoundEffect packet=(SPacketSoundEffect)players.packet;check(packet.getSound()==registered,"packet exact registered event");check(packet.getCategory()==SoundCategory.BLOCKS,"packet category transport");eq(packet.getX(),(double)((float)(int)(-.249D*8D)/8F),"native negative packet x");eq(packet.getY(),(double)((float)(int)(17.03125D*8D)/8F),"native packet y");eq(packet.getZ(),(double)((float)(int)(30000000.125D*8D)/8F),"native large packet z");eq(packet.getVolume(),volume,"native unchanged volume");eq(packet.getPitch(),wirePitch(pitch),"legacy quantized built-in pitch");eq(subclass.pitch,pitch,"native subclass raw pitch not mutated");eq((float)observers.get(observers.size()-1)[7],pitch,"observer raw pitch not mutated");}
        check(subclass.calls==invocations,"native subclass count");
        WorldBoundary inheritedCaller=world(new ArrayList<>(Arrays.asList(new InheritedServerHandler(server,nativeWorld))),false);
        play(inheritedCaller,.5F,.799999F);eq(((SPacketSoundEffect)players.packet).getPitch(),.799999F,"explicit boundary: subclass inheriting native builtin owns native unquantized pitch");check(Float.floatToIntBits(((SPacketSoundEffect)players.packet).getPitch())!=Float.floatToIntBits(wirePitch(.799999F)),"inherited subclass is deliberately not claimed as legacy wire parity");
    }
    static void recipientTests()throws Exception {
        DispatchPlayerList players=alloc(DispatchPlayerList.class);List<EntityPlayerMP> list=new ArrayList<>();set(players,PlayerList.class,"playerEntityList",list);
        double[] positions={0D,Math.nextDown(16D),16D,Math.nextUp(16D),-15D,0D,0D,Double.NaN,Double.POSITIVE_INFINITY};
        CaptureConnection[] connections=new CaptureConnection[positions.length];
        for(int i=0;i<positions.length;i++){EntityPlayerMP player=alloc(EntityPlayerMP.class);player.posX=positions[i];player.posY=0;player.posZ=0;player.dimension=i==5?8:7;connections[i]=alloc(CaptureConnection.class);player.connection=connections[i];list.add(player);}
        SPacketSoundEffect packet=new SPacketSoundEffect(ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("hbm","block.crateOpen")),SoundCategory.BLOCKS,0,0,0,1,1);
        players.sendToAllNearExcept(list.get(6),0,0,0,16,7,packet);
        for(int i=0;i<positions.length;i++){boolean expected=i==0||i==1||i==4;check(connections[i].sends==(expected?1:0),"real native recipient boundary/exclusion "+i);if(expected)check(connections[i].packet==packet,"same packet delivered");}
        for(CaptureConnection c:connections)c.sends=0;
        players.sendToAllNearExcept(null,0,0,0,16,7,packet);check(connections[6].sends==1,"null excludes nobody");check(connections[2].sends==0,"radius boundary strictly excluded");
    }
    public static final class ClientEventSink {
        int calls; net.minecraft.client.audio.ISound original,result; List<String> stack;
        @SubscribeEvent(priority=net.minecraftforge.fml.common.eventhandler.EventPriority.LOWEST)
        public void observe(net.minecraftforge.client.event.sound.PlaySoundEvent event){calls++;original=event.getSound();result=event.getResultSound();stack=new ArrayList<>();for(StackTraceElement frame:Thread.currentThread().getStackTrace())stack.add(frame.getClassName()+"."+frame.getMethodName());event.setResultSound(null);}
    }
    static void nativeClientPacketTest(SoundEvent registered)throws Exception {
        net.minecraft.client.Minecraft client=alloc(net.minecraft.client.Minecraft.class);
        set(client,net.minecraft.client.Minecraft.class,"thread",Thread.currentThread());
        net.minecraft.client.entity.EntityPlayerSP player=alloc(net.minecraft.client.entity.EntityPlayerSP.class);client.player=player;
        set(client,net.minecraft.client.Minecraft.class,"renderViewEntity",player);
        net.minecraft.client.multiplayer.WorldClient world=alloc(net.minecraft.client.multiplayer.WorldClient.class);set(world,net.minecraft.client.multiplayer.WorldClient.class,"mc",client);client.world=world;
        net.minecraft.client.audio.SoundHandler handler=alloc(net.minecraft.client.audio.SoundHandler.class);
        net.minecraft.client.audio.SoundManager manager=alloc(net.minecraft.client.audio.SoundManager.class);set(manager,net.minecraft.client.audio.SoundManager.class,"loaded",true);set(manager,net.minecraft.client.audio.SoundManager.class,"sndHandler",handler);set(handler,net.minecraft.client.audio.SoundHandler.class,"sndManager",manager);set(client,net.minecraft.client.Minecraft.class,"soundHandler",handler);
        net.minecraft.client.network.NetHandlerPlayClient network=alloc(net.minecraft.client.network.NetHandlerPlayClient.class);set(network,net.minecraft.client.network.NetHandlerPlayClient.class,"client",client);
        java.nio.file.Path pack=java.nio.file.Files.createTempDirectory("world-sound-category-audit-");java.nio.file.Path json=pack.resolve("assets/hbm/sounds.json");java.nio.file.Files.createDirectories(json.getParent());java.nio.file.Files.write(json,"{\"block.crateOpen\":{\"category\":\"weather\",\"sounds\":[]}}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        net.minecraft.client.resources.SimpleReloadableResourceManager resources=new net.minecraft.client.resources.SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer());resources.registerReloadListener(new com.hbm.sound.SoundCategoryResolver());resources.reloadResources(Arrays.asList(new net.minecraft.client.resources.FolderResourcePack(pack.toFile())));
        check(com.hbm.sound.SoundCategoryResolver.get(registered.getSoundName())==SoundCategory.WEATHER,"real resource reload changed client category");
        MinecraftForge.EVENT_BUS.register(com.hbm.sound.WorldSoundCategories.class);ClientEventSink sink=new ClientEventSink();MinecraftForge.EVENT_BUS.register(sink);
        SPacketSoundEffect sent=new SPacketSoundEffect(registered,SoundCategory.BLOCKS,-.249,17.03125,30000000.125,.8F,wirePitch(.799999F));
        net.minecraft.network.PacketBuffer bytes=new net.minecraft.network.PacketBuffer(new io.netty.buffer.UnpooledByteBufAllocator(false).heapBuffer());sent.writePacketData(bytes);SPacketSoundEffect received=new SPacketSoundEffect();received.readPacketData(bytes);bytes.release();network.handleSoundEffect(received);
        check(sink.calls==1,"actual native packet playback event once");check(sink.original.getClass()==net.minecraft.client.audio.PositionedSoundRecord.class,"packet builds exact native PositionedSoundRecord");check(sink.original.getCategory()==SoundCategory.BLOCKS,"packet transport category remains bundled");check(sink.result!=sink.original&&sink.result!=null,"production event subscriber remaps exact native packet record");check(sink.result.getCategory()==SoundCategory.WEATHER,"receiving-client resource category wins over server bundled category");check(sink.result.getSoundLocation().equals(registered.getSoundName()),"native registered resource ID");check(!sink.result.canRepeat()&&sink.result.getRepeatDelay()==0,"native packet nonrepeating immediate playback");check(sink.result.getAttenuationType()==net.minecraft.client.audio.ISound.AttenuationType.LINEAR,"native packet LINEAR attenuation");eq(sink.result.getXPosF(),(float)received.getX(),"native packet-to-client x");eq(sink.result.getYPosF(),(float)received.getY(),"native packet-to-client y");eq(sink.result.getZPosF(),(float)received.getZ(),"native packet-to-client z");
        Field rawVolume=net.minecraft.client.audio.PositionedSound.class.getDeclaredField("volume"),rawPitch=net.minecraft.client.audio.PositionedSound.class.getDeclaredField("pitch");rawVolume.setAccessible(true);rawPitch.setAccessible(true);eq(rawVolume.getFloat(sink.original),received.getVolume(),"native packet-to-client raw volume");eq(rawPitch.getFloat(sink.original),received.getPitch(),"native packet-to-client raw pitch");
        for(String frame:Arrays.asList("net.minecraft.client.network.NetHandlerPlayClient.handleSoundEffect","net.minecraft.client.multiplayer.WorldClient.playSound","net.minecraft.client.audio.SoundHandler.playSound","net.minecraft.client.audio.SoundManager.playSound","net.minecraftforge.client.ForgeHooksClient.playSound"))check(sink.stack.contains(frame),"actual native playback frame "+frame);
        MinecraftForge.EVENT_BUS.unregister(sink);MinecraftForge.EVENT_BUS.unregister(com.hbm.sound.WorldSoundCategories.class);java.nio.file.Files.delete(json);java.nio.file.Files.delete(json.getParent());java.nio.file.Files.delete(pack.resolve("assets"));java.nio.file.Files.delete(pack);
    }
    public static void main(String[] args)throws Exception {
        net.minecraft.init.Bootstrap.register();
        net.minecraftforge.fml.common.FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
        net.minecraftforge.fml.common.ModMetadata metadata=new net.minecraftforge.fml.common.ModMetadata();metadata.modId="hbm";metadata.name="World sound audit";metadata.version="1";
        net.minecraftforge.fml.common.DummyModContainer owner=new net.minecraftforge.fml.common.DummyModContainer(metadata);
        net.minecraftforge.fml.common.Loader.instance().setupTestHarness(owner);net.minecraftforge.fml.common.Loader.instance().setActiveModContainer(owner);
        ModSoundEvents.registerSounds(new RegistryEvent.Register<SoundEvent>(new ResourceLocation("minecraft","soundevents"),ForgeRegistries.SOUND_EVENTS));
        SoundEvent registered=ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("hbm","block.crateOpen"));check(registered!=null,"approved event registered");
        observerTests(registered);nativeServerTests(registered);recipientTests();nativeClientPacketTest(registered);
        System.out.println("PASS production WorldSoundEffects native callback/broadcast probe: "+checks+" checks, ordinary+remote+empty World lists, raw custom observers/subclass, native silent RenderGlobal, mutation order, no entity hook, actual ServerWorldEventHandler metadata/packet encoding, actual PlayerList recipient boundaries/exclusion, actual registered packet roundtrip through native client network/world/sound manager and production category event hook (audio stopped at final event sink).");
    }
}
