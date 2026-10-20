import com.google.gson.*;
import com.hbm.main.NetworkHandler;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.LegacyEntitySoundPacket;
import com.hbm.sound.*;
import io.netty.buffer.ByteBuf;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.*;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.entity.player.*;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.*;
import net.minecraft.server.dedicated.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.network.*;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;

/** Genuine old/native parsers, registry, World listeners, project codec, scheduler and sound event.
 * World construction, socket/audio output and unrelated project bootstrap are explicit boundaries.
 * Empty sample streams test metadata/accessor wiring only, never OGG decoding. */
public final class NativeVanillaSoundProbe {
 static int checks; static JsonArray aliases; static JsonObject original,nativeJson; static URLClassLoader old;
 static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 static void eq(float a,float b,String m){check(Float.floatToRawIntBits(a)==Float.floatToRawIntBits(b),m);}
 static void eq(double a,double b,String m){check(Double.doubleToRawLongBits(a)==Double.doubleToRawLongBits(b),m);}
 static <T>T alloc(Class<T> c)throws Exception{return NativeEntityTransportProbe.alloc(c);}
 static void set(Object o,Class<?> c,String f,Object v)throws Exception{NativeEntityTransportProbe.set(o,c,f,v);}
 static Object get(Object o,Class<?> c,String f)throws Exception{return NativeEntityTransportProbe.get(o,c,f);}
 static JsonObject json(String path)throws Exception{try(Reader r=Files.newBufferedReader(Paths.get(path),StandardCharsets.UTF_8)){return new JsonParser().parse(r).getAsJsonObject();}}
 static String name(JsonElement row){return row.getAsJsonObject().get("legacy").getAsString();}
 static SoundEvent event(JsonElement row)throws Exception{return (SoundEvent)SoundEvents.class.getField(row.getAsJsonObject().get("field").getAsString()).get(null);}
 static SoundCategory category(JsonElement row){return SoundCategory.valueOf(row.getAsJsonObject().get("category").getAsString());}
 static Gson gson=new GsonBuilder().registerTypeAdapter(SoundList.class,new SoundListSerializer()).create();
 static void definitions()throws Exception {
  Class<?> list=old.loadClass("bsp"),sample=old.loadClass("bsq"),category=old.loadClass("w");
  Gson legacy=new GsonBuilder().registerTypeAdapter(list,old.loadClass("bss").newInstance()).create();
  int registryBefore=SoundEvent.REGISTRY.getKeys().size();
  for(JsonElement row:aliases){String n=name(row);SoundEvent actual=event(row);String target=row.getAsJsonObject().get("native").getAsString();
   check(actual==SoundEvent.REGISTRY.getObject(new ResourceLocation(target)),"genuine vanilla registry identity "+n);check(SoundEvent.REGISTRY.getObject(new ResourceLocation(n))==null,"resource alias never registers synthetic event "+n);
   Object before=legacy.fromJson(original.get(n),list);SoundList after=gson.fromJson(original.get(n),SoundList.class),canonical=gson.fromJson(nativeJson.get(target),SoundList.class);
   Object cat=list.getMethod("c").invoke(before);check(category.getMethod("a").invoke(cat).equals(category(row).getName()),"actual legacy category parser "+n);
   List<?> samples=(List<?>)list.getMethod("a").invoke(before);check(samples.size()==after.getSounds().size()&&samples.size()==canonical.getSounds().size(),"sample counts");
   for(int i=0;i<samples.size();i++){Object o=samples.get(i);Sound a=after.getSounds().get(i),b=canonical.getSounds().get(i);check(new ResourceLocation((String)sample.getMethod("a").invoke(o)).equals(a.getSoundLocation())&&a.getSoundLocation().equals(b.getSoundLocation()),"sample path");eq((Float)sample.getMethod("b").invoke(o),a.getVolume(),"actual legacy sample gain");eq((Float)sample.getMethod("c").invoke(o),a.getPitch(),"actual legacy sample pitch");check((Integer)sample.getMethod("d").invoke(o)==a.getWeight(),"actual legacy weight");check((Boolean)sample.getMethod("f").invoke(o)==a.isStreaming(),"actual legacy stream");eq(a.getVolume(),b.getVolume(),"native sample gain");eq(a.getPitch(),b.getPitch(),"native sample pitch");check(a.getWeight()==b.getWeight()&&a.isStreaming()==b.isStreaming()&&a.getType()==Sound.Type.FILE,"native metadata properties");}
  }
  check(SoundEvent.REGISTRY.getKeys().size()==registryBefore,"no registry mutation");
  System.out.println("PASS actual legacy/native definition parsers and all 15 genuine vanilla registry identities.");
 }
 public static final class WorldBoundary extends World {
  WorldBoundary(){super(null,null,null,null,false);}
  protected IChunkProvider createChunkProvider(){throw new AssertionError("chunks");}protected boolean isChunkLoaded(int x,int z,boolean a){throw new AssertionError("chunks");}
  void replace(List<IWorldEventListener> l){eventListeners=l;}
  @Override public void playSound(EntityPlayer p,double x,double y,double z,SoundEvent s,SoundCategory c,float v,float pi){throw new AssertionError("new native entity hook");}
 }
 static WorldBoundary world(List<IWorldEventListener> l,boolean remote)throws Exception{WorldBoundary w=alloc(WorldBoundary.class);set(w,World.class,"eventListeners",l);set(w,World.class,"isRemote",remote);return w;}
 interface Run{void call(Object[] a);}
 static IWorldEventListener observer(Run r){return (IWorldEventListener)java.lang.reflect.Proxy.newProxyInstance(NativeVanillaSoundProbe.class.getClassLoader(),new Class[]{IWorldEventListener.class},(p,m,a)->{if(m.getName().equals("equals"))return p==a[0];if(m.getName().equals("hashCode"))return System.identityHashCode(p);if(m.getName().equals("playSoundToAllNearExcept"))r.call(a);return null;});}
 static void play(World w,String n,float v,float p){VanillaWorldSoundEffects.play(w,-.249D,17.03125D,30000000.125D,n,v,p);}
 public static final class EntityHook {int count;@SubscribeEvent public void hear(PlaySoundAtEntityEvent e){count++;}}
 static void listeners()throws Exception {
  EntityHook hook=new EntityHook();MinecraftForge.EVENT_BUS.register(hook);
  for(boolean remote:new boolean[]{false,true})for(JsonElement row:aliases){String n=name(row);List<IWorldEventListener> live=new ArrayList<>();WorldBoundary w=world(live,remote);play(w,n,1,1);
   List<Object[]> received=new ArrayList<>();IWorldEventListener first=observer(a->received.add(a));live.add(first);live.add(first);live.add(alloc(RenderGlobal.class));NativeEntityTransportProbe.Override subclass=alloc(NativeEntityTransportProbe.Override.class);live.add(subclass);play(w,n,.8F,.799999F);
   check(received.size()==2&&subclass.last!=null,"duplicate/native client no-op/custom subclass");received.add(subclass.last);for(Object[] a:received){check(a[0]==null&&a[1]==event(row)&&a[2]==category(row),"listener owns real native identity and original category");eq((double)a[3],-.249D,"raw x");eq((double)a[4],17.03125D,"raw y");eq((double)a[5],30000000.125D,"raw z");eq((float)a[6],.8F,"raw volume");eq((float)a[7],.799999F,"raw pitch");}
  }
  List<IWorldEventListener> live=new ArrayList<>();WorldBoundary w=world(live,false);List<String> order=new ArrayList<>();IWorldEventListener second=observer(a->order.add("second")),third=observer(a->order.add("third"));final IWorldEventListener[] first=new IWorldEventListener[1];first[0]=observer(a->{order.add("first");live.remove(first[0]);live.add(third);});live.add(first[0]);live.add(second);play(w,"random.fizz",1,1);check(order.equals(Arrays.asList("first","third")),"live indexed mutation");
  order.clear();live.clear();List<IWorldEventListener> replacement=Arrays.asList(observer(a->order.add("skip")),observer(a->order.add("new-one")));live.add(observer(a->{order.add("old-zero");w.replace(replacement);}));live.add(second);play(w,"random.fizz",1,1);check(order.equals(Arrays.asList("old-zero","new-one")),"World-declared list replacement");
  List<Object[]> methodCalls=new ArrayList<>();WorldBoundary exact=world(new ArrayList<>(Arrays.asList(observer(a->{check(RepresentativeMethods.superCalls==0,"callback before original super method");methodCalls.add(a);}))),false);RepresentativeMethods.invoke(exact);check(methodCalls.size()==1&&RepresentativeMethods.superCalls==1,"complete production method executes sound then super");check(methodCalls.get(0)[1]==SoundEvents.ENTITY_GENERIC_SPLASH,"complete method native sound identity");eq((double)methodCalls.get(0)[3],-2D,"native TileEntity coordinate");
  check(hook.count==0,"no new Forge entity events");MinecraftForge.EVENT_BUS.unregister(hook);
  List<String> eval=new ArrayList<>();try{VanillaWorldSoundEffects.play(evalWorld(eval),evalDouble(eval,"x"),evalDouble(eval,"y"),evalDouble(eval,"z"),evalName(eval),evalFloat(eval,"volume"),evalFloat(eval,"pitch"));throw new AssertionError("null accepted");}catch(NullPointerException expected){}check(eval.equals(Arrays.asList("receiver","x","y","z","name","volume","pitch")),"receiver/arguments once before null failure");
  System.out.println("PASS both logical sides, exact client no-op, custom listeners/subclasses, live mutation, registry ownership and no entity hook.");
 }
 static World evalWorld(List<String> l){l.add("receiver");return null;}static double evalDouble(List<String> l,String n){l.add(n);return 0;}static String evalName(List<String> l){l.add("name");return "random.fizz";}static float evalFloat(List<String> l,String n){l.add(n);return 1;}
 static SimpleReloadableResourceManager resources;static FolderResourcePack bundled;static SoundHandler audio;static Minecraft mc;static Queue<FutureTask<?>> tasks;static NativeEntityTransportProbe.Sink sink;static FMLEmbeddedChannel client;
 static FolderResourcePack pack(String text)throws Exception{Path p=Files.createTempDirectory("vanilla-alias-pack-");Path f=p.resolve("assets/minecraft/sounds.json");Files.createDirectories(f.getParent());Files.write(f,text.getBytes(StandardCharsets.UTF_8));return new FolderResourcePack(p.toFile());}
 static void audioSetup(String folder)throws Exception {
  bundled=new FolderResourcePack(new File(folder));resources=new SimpleReloadableResourceManager(new MetadataSerializer());resources.registerReloadListener(new SoundCategoryResolver());resources.reloadResources(Arrays.asList(bundled));
  audio=new SoundHandler(resources,null);SoundManager manager=(SoundManager)get(audio,SoundHandler.class,"sndManager");set(manager,SoundManager.class,"loaded",true);
  mc=alloc(Minecraft.class);set(null,Minecraft.class,"instance",mc);set(mc,Minecraft.class,"thread",Thread.currentThread());tasks=new ArrayDeque<>();set(mc,Minecraft.class,"scheduledTasks",tasks);set(mc,Minecraft.class,"soundHandler",audio);
  sink=new NativeEntityTransportProbe.Sink();MinecraftForge.EVENT_BUS.register(sink);client=(FMLEmbeddedChannel)get(null,NetworkHandler.class,"clientChannel");client.attr(NetworkRegistry.NET_HANDLER).set(alloc(net.minecraft.client.network.NetHandlerPlayClient.class));
 }
 static void inbound(FMLProxyPacket packet,String n,SoundCategory cat,float v,float p)throws Exception {
  int before=sink.calls;AtomicReference<Throwable> failure=new AtomicReference<>();Thread t=new Thread(()->{try{client.writeInbound(packet);client.checkException();}catch(Throwable e){failure.set(e);}},"alias-native-network");t.start();t.join();if(failure.get()!=null)throw new AssertionError("native custom pipeline",failure.get());check(sink.calls==before&&tasks.size()==1,"network only queues");check(packet.payload().refCnt()==0,"existing custom codec releases inbound exactly");FutureTask<?> task=tasks.remove();task.run();task.get();check(sink.calls==before+1&&sink.thread==Thread.currentThread(),"main thread actual audio event");ISound sound=sink.sound;check(sound.getClass()==PositionedSoundRecord.class&&sound.getSoundLocation().equals(new ResourceLocation(n)),"ordinary record original raw alias");check(sound.getCategory()==cat,"receiving pack category "+n);check(!sound.canRepeat()&&sound.getRepeatDelay()==0&&sound.getAttenuationType()==ISound.AttenuationType.LINEAR,"repeat/attenuation");eq((Float)get(sound,PositionedSound.class,"volume"),v,"packet volume");eq((Float)get(sound,PositionedSound.class,"pitch"),Math.max(0,Math.min(255,(int)(p*63F)))/63F,"old pitch bytes");eq(sound.getXPosF(),-.125F,"negative packet truncation");eq(sound.getYPosF(),17F,"packet y");eq(sound.getZPosF(),(float)(int)(30000000.125D*8D)/8F,"packet float position");check(sink.frames.contains("net.minecraftforge.client.ForgeHooksClient.playSound"),"genuine Forge audio event");
 }
 static void builtin()throws Exception {
  DedicatedServer server=alloc(DedicatedServer.class);NativeEntityTransportProbe.CapturePlayers players=alloc(NativeEntityTransportProbe.CapturePlayers.class);server.setPlayerList(players);WorldServer world=alloc(WorldServer.class);WorldProviderSurface provider=alloc(WorldProviderSurface.class);provider.setDimension(-7);set(world,World.class,"provider",provider);ServerWorldEventHandler builtin=new ServerWorldEventHandler(server,world);WorldBoundary caller=world(new ArrayList<>(Arrays.asList(builtin)),false);
  float[] ps={Float.NEGATIVE_INFINITY,-0F,.799999F,Float.NaN,Float.POSITIVE_INFINITY};float[] vs={0F,-0F,.8F,10000F,Float.MAX_VALUE};
  for(JsonElement row:aliases)for(int i=0;i<ps.length;i++){String n=name(row);float p=ps[i],v=vs[i];play(caller,n,v,p);check(players.excluded==null&&players.dim==-7,"listener-owned dimension/exclusion");eq(players.radius,v>1F?(double)(16F*v):16D,"float-first radius");eq(players.x,-.249D,"unquantized audience x");FMLProxyPacket f=(FMLProxyPacket)players.packet;ByteBuf bytes=f.payload().duplicate();check(bytes.readUnsignedByte()==33,"existing discriminator33");ByteBuf before=NativeEntityTransportProbe.oldBytes(NativeEntityTransportProbe.old(n,-.249D,17.03125D,30000000.125D,v,p));check(Arrays.equals(NativeEntityTransportProbe.bytes(before),NativeEntityTransportProbe.bytes(bytes)),"actual original packet body and original raw name");before.release();inbound(f,n,category(row),v,p);}
  // A subclass that inherits the native callback owns its native packet path, not this alias route.
  EntitySoundTransport.dispatchResource(new NativeEntityTransportProbe.Inherited(server,world),SoundEvents.BLOCK_FIRE_EXTINGUISH,SoundCategory.BLOCKS,"random.fizz",1,2,3,.8F,.799999F,m->{throw new AssertionError("subclass hijacked");});check(players.packet instanceof net.minecraft.network.play.server.SPacketSoundEffect,"inherited subclass remains native");
  System.out.println("PASS 75 exact old packet body comparisons through the new helper, actual built-in server listener, project codec and real client main-thread playback event.");
 }
 static Object oldHandler;static Class<?> oldList,oldLoc;static Method oldLoad,oldGet;static Gson oldGson;
 static void oldCategoryInit()throws Exception{Class<?> h=old.loadClass("btp");oldHandler=alloc(h);set(oldHandler,h,"e",old.loadClass("btt").newInstance());oldList=old.loadClass("bsp");oldLoc=old.loadClass("bqx");oldLoad=h.getDeclaredMethod("a",oldLoc,oldList);oldLoad.setAccessible(true);oldGet=h.getMethod("a",oldLoc);oldGson=new GsonBuilder().registerTypeAdapter(oldList,old.loadClass("bss").newInstance()).create();}
 static void oldCategory(String def)throws Exception{oldLoad.invoke(oldHandler,oldLoc.getConstructor(String.class).newInstance("random.fizz"),oldGson.fromJson(def,oldList));}
 static SoundCategory oldCategory()throws Exception{Object accessor=oldGet.invoke(oldHandler,oldLoc.getConstructor(String.class).newInstance("random.fizz"));Object cat=accessor.getClass().getMethod("d").invoke(accessor);return SoundCategory.getByName((String)cat.getClass().getMethod("a").invoke(cat));}
 static void overrides()throws Exception {
  oldCategoryInit();oldCategory("{\"category\":\"block\",\"sounds\":[]}");List<IResourcePack> packs=new ArrayList<>(Arrays.asList(bundled));
  for(String def:new String[]{"{\"category\":\"music\",\"sounds\":[]}","{\"category\":\"record\",\"replace\":true,\"sounds\":[]}","{\"replace\":true,\"sounds\":[]}"}){oldCategory(def);packs.add(pack("{\"random.fizz\":"+def+"}"));resources.reloadResources(packs);check(SoundCategoryResolver.get(new ResourceLocation("random.fizz"))==oldCategory(),"actual old append/replace category semantics");FMLProxyPacket f=(FMLProxyPacket)PacketDispatcher.wrapper.getPacketFrom(new LegacyEntitySoundPacket("random.fizz",-.249,17.03125,30000000.125,.8F,.799999F));inbound(f,"random.fizz",oldCategory(),.8F,.799999F);}
  resources.reloadResources(Collections.emptyList());check(SoundCategoryResolver.get(new ResourceLocation("random.fizz"))==SoundCategory.MASTER,"removed alias no stale metadata");resources.reloadResources(Arrays.asList(bundled));check(SoundCategoryResolver.get(new ResourceLocation("random.fizz"))==SoundCategory.BLOCKS,"restore base");
  FolderResourcePack collision=pack("{\"random.fizz\":{\"category\":\"music\",\"sounds\":[]}}");resources.reloadResources(Arrays.asList(collision,bundled));check(SoundCategoryResolver.get(new ResourceLocation("random.fizz"))==SoundCategory.MUSIC,"earlier alias owner is not taken over");resources.reloadResources(Arrays.asList(bundled,collision));check(SoundCategoryResolver.get(new ResourceLocation("random.fizz"))==SoundCategory.BLOCKS,"later append does not replace original category");resources.reloadResources(Arrays.asList(bundled));System.out.println("COMPATIBILITY WARNING: another mod defining the same legacy aliases follows ordinary pack order; no forced ownership. Pack/mod integration remains a gate.");
  System.out.println("PASS actual old SoundHandler category merge versus native resource reload, plus actual client packets after append/replace/default overrides.");
 }
 public static final class WarningCapture extends org.apache.logging.log4j.core.appender.AbstractAppender {
  final List<String> messages=new ArrayList<>();WarningCapture(){super("alias-warning-audit",null,null);}
  @Override public void append(org.apache.logging.log4j.core.LogEvent event){String s=event.getMessage().getFormattedMessage();if(s.startsWith("Duplicate vanilla coordinate sound alias "))messages.add(s);}
 }
 static void diagnostic()throws Exception {
  org.apache.logging.log4j.core.Logger logger=(org.apache.logging.log4j.core.Logger)get(null,SoundCategoryResolver.class,"LOGGER");WarningCapture capture=new WarningCapture();capture.start();logger.addAppender(capture);
  try {
   FolderResourcePack append=pack("{\"random.fizz\":{\"category\":\"music\",\"sounds\":[]}}");FolderResourcePack replace=pack("{\"random.fizz\":{\"category\":\"record\",\"replace\":true,\"sounds\":[]}}");
   resources.reloadResources(Arrays.asList(bundled,append,replace));check(capture.messages.size()==1,"one warning per alias per reload despite three definitions");String msg=capture.messages.get(0);check(msg.contains("minecraft:random.fizz")&&msg.contains(bundled.getPackName())&&msg.contains(append.getPackName())&&msg.contains("(append)"),"runtime warning gives both packs, alias and operation");check(SoundCategoryResolver.get(new ResourceLocation("random.fizz"))==SoundCategory.RECORDS,"diagnostic does not interfere with later intentional replacement");
   resources.reloadResources(Arrays.asList(bundled,append,replace));check(capture.messages.size()==2,"warning limit resets on reload");resources.reloadResources(Arrays.asList(bundled));check(capture.messages.size()==2,"no duplicate no warning");
   resources.reloadResources(Arrays.asList(bundled,replace));check(capture.messages.size()==3&&capture.messages.get(2).contains("(replace)")&&capture.messages.get(2).contains(replace.getPackName()),"replace diagnostic and pack identity");
   FolderResourcePack other=pack("{\"block.fire.extinguish\":{\"sounds\":[]},\"foreign.unrelated\":{\"sounds\":[]}}");resources.reloadResources(Arrays.asList(other,other));check(capture.messages.size()==3,"no warnings for ordinary native or unknown custom names");
   FolderResourcePack invalid=pack("{\"random.fizz\":{\"sounds\":[]},\"invalid\":{\"category\":\"bad-category\"}}");resources.reloadResources(Arrays.asList(bundled,invalid));check(capture.messages.size()==3,"rejected metadata transaction cannot emit false duplicate warning");
   resources.reloadResources(Arrays.asList(bundled));new LegacyEntitySoundPacket.Handler().onMessage(new LegacyEntitySoundPacket("random.fizz",0,0,0,1,1),null);check(capture.messages.size()==3,"playback does not warn");
  } finally {logger.removeAppender(capture);capture.stop();}
  System.out.println("PASS bounded runtime duplicate diagnostics: both packs and append/replace, once per alias/reload, reload reset, no unknown-name logging, invalid-pack rollback, no playback logging, normal overrides preserved. Intent is deliberately not classified.");
 }
 static void accessors()throws Exception {
  Method load=SoundHandler.class.getDeclaredMethod("loadSoundResource",ResourceLocation.class,SoundList.class);load.setAccessible(true);
  // Only existence streams are fabricated. Official indexes separately prove identical real asset hashes.
  Path samples=Files.createTempDirectory("metadata-only-samples-");for(JsonElement row:aliases)for(Sound s:gson.fromJson(original.get(name(row)),SoundList.class).getSounds()){Path f=samples.resolve("assets/minecraft/"+s.getSoundAsOggLocation().getPath());Files.createDirectories(f.getParent());Files.write(f,new byte[0]);}
  resources.reloadResources(Arrays.asList(new FolderResourcePack(samples.toFile()),bundled));
  for(JsonElement row:aliases){String n=name(row),nativeName=row.getAsJsonObject().get("native").getAsString();load.invoke(audio,new ResourceLocation(n),gson.fromJson(original.get(n),SoundList.class));load.invoke(audio,new ResourceLocation(nativeName),gson.fromJson(nativeJson.get(nativeName),SoundList.class));SoundEventAccessor a=audio.getAccessor(new ResourceLocation(n)),b=audio.getAccessor(new ResourceLocation(nativeName));check(a!=null&&b!=null&&a!=b&&a.getWeight()==b.getWeight(),"separate alias/native accessor pools");PositionedSoundRecord sound=new PositionedSoundRecord(new ResourceLocation(n),category(row),.8F,.9F,false,0,ISound.AttenuationType.LINEAR,1,2,3);check(sound.createAccessor(audio)==a&&sound.getSound()!=SoundHandler.MISSING_SOUND,"genuine native sample selection metadata");eq(sound.getVolume(),.8F*sound.getSound().getVolume(),"one gain application");eq(sound.getPitch(),.9F*sound.getSound().getPitch(),"one pitch application");}
  ResourceLocation nativeName=SoundEvents.BLOCK_FIRE_EXTINGUISH.getSoundName();SoundEventAccessor kept=audio.getAccessor(nativeName);load.invoke(audio,new ResourceLocation("random.fizz"),gson.fromJson("{\"replace\":true,\"sounds\":[\"absent/audit/sample\"]}",SoundList.class));check(audio.getAccessor(new ResourceLocation("random.fizz")).cloneEntry()==SoundHandler.MISSING_SOUND,"native warns and missing sample is not replaced with unrelated sound");check(audio.getAccessor(nativeName)==kept,"alias replace never replaces native accessor");
  ResourceLocation alias=new ResourceLocation("random.fizz");SoundList replacement=gson.fromJson("{\"replace\":true,\"sounds\":[{\"name\":\"random/fizz\",\"volume\":0.25,\"pitch\":0.75,\"weight\":7}]}",SoundList.class);load.invoke(audio,alias,replacement);SoundEventAccessor replaced=audio.getAccessor(alias);check(replaced.getWeight()==7,"resource replace resets sample pool");PositionedSoundRecord changed=new PositionedSoundRecord(alias,SoundCategory.BLOCKS,.8F,.9F,false,0,ISound.AttenuationType.LINEAR,0,0,0);changed.createAccessor(audio);eq(changed.getVolume(),.8F*.25F,"override gain exactly once");eq(changed.getPitch(),.9F*.75F,"override pitch exactly once");load.invoke(audio,alias,gson.fromJson("{\"sounds\":[{\"name\":\"random/fizz\",\"weight\":2}]}",SoundList.class));check(audio.getAccessor(alias)==replaced&&replaced.getWeight()==9,"append retains accessor and adds weight");check(audio.getAccessor(nativeName)==kept,"override sample pool never changes native event pool");resources.reloadResources(Arrays.asList(bundled));
  System.out.println("PASS actual native accessor selection and missing-sample warning with explicit metadata-only empty-stream boundary. No sample decoding/audio output.");
 }
 public static void main(String[] a)throws Exception{
  NativeCustomCodecProbe.init();aliases=json(a[0]).getAsJsonArray("aliases");original=json(a[1]);nativeJson=json(a[2]);old=new URLClassLoader(new URL[]{new File(a[3]).toURI().toURL()},NativeVanillaSoundProbe.class.getClassLoader());NativeEntityTransportProbe.oldPacket=old.loadClass("hc");NativeEntityTransportProbe.oldBuffer=old.loadClass("et");definitions();listeners();PacketDispatcher.wrapper.registerMessage(LegacyEntitySoundPacket.Handler.class,LegacyEntitySoundPacket.class,33,Side.CLIENT);audioSetup(a[4]);builtin();overrides();accessors();diagnostic();MinecraftForge.EVENT_BUS.unregister(sink);old.close();System.out.println("PASS NativeVanillaSoundProbe "+checks+" checks. No complete mod/game launch, hardware playback, or unknown-mod collision integration.");
 }
}
