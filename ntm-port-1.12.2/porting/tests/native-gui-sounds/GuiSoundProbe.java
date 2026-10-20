import com.hbm.sound.GuiSound;
import com.hbm.sound.SoundCategoryResolver;
import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.*;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import sun.misc.Unsafe;

public final class GuiSoundProbe {
 static int checks; static final Unsafe U;
 static {try{Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);U=(Unsafe)f.get(null);}catch(Exception e){throw new ExceptionInInitializerError(e);}}
 static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static void eq(float a,float b,String s){check(Float.floatToRawIntBits(a)==Float.floatToRawIntBits(b),s+" "+a+" != "+b);}
 static void set(Object o,Class<?> c,String n,Object v)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);f.set(o,v);}
 static Object get(Object o,Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f.get(o);}
 static final String[] NAMES={"block.bobble","weapon.cSwing","alarm.singer","block.rbmk_az5_cover","item.techBleep","block.leverLarge","block.shutdown","item.techBoop"};
 static void props(String jar)throws Exception{
  try(URLClassLoader l=new URLClassLoader(new URL[]{new File(jar).toURI().toURL()},GuiSoundProbe.class.getClassLoader())){
   Class<?> loc=l.loadClass("bqx"),rec=l.loadClass("bso");Method factory=rec.getMethod("a",loc,float.class);Object id=loc.getConstructor(String.class).newInstance("hbm:probe");
   Random r=new Random(93);for(int i=0;i<256;i++){
    float p=i<8?new float[]{0F,-0F,Float.NaN,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.MAX_VALUE,Float.MIN_VALUE,-1F}[i]:Float.intBitsToFloat(r.nextInt());
    Object old=factory.invoke(null,id,p);PositionedSoundRecord fresh=GuiSound.create(new ResourceLocation("hbm:probe"),p);
    eq(fresh.getVolume(),(Float)rec.getMethod("e").invoke(old),"raw quarter volume");eq(fresh.getPitch(),(Float)rec.getMethod("f").invoke(old),"raw pitch bits");
    eq(fresh.getXPosF(),(Float)rec.getMethod("g").invoke(old),"x");eq(fresh.getYPosF(),(Float)rec.getMethod("h").invoke(old),"y");eq(fresh.getZPosF(),(Float)rec.getMethod("i").invoke(old),"z");
    check(fresh.canRepeat()==(Boolean)rec.getMethod("c").invoke(old),"repeat");check(fresh.getRepeatDelay()==(Integer)rec.getMethod("d").invoke(old),"delay");check(fresh.getAttenuationType().ordinal()==((Enum<?>)rec.getMethod("j").invoke(old)).ordinal(),"attenuation");
   }
   Object old=factory.invoke(null,null,1F);check(rec.getMethod("b").invoke(old)==null&&GuiSound.create(null,1).getSoundLocation()==null,"factory null not rejected early");
  }
  check(GuiSound.create(new ResourceLocation("gui.button.press"),1).getSoundLocation().equals(net.minecraft.init.SoundEvents.UI_BUTTON_CLICK.getSoundName()),"old button maps exact native button event");
  ResourceLocation foreign=new ResourceLocation("other:gui.button.press");check(GuiSound.create(foreign,1).getSoundLocation()==foreign,"other namespace no remap");
  ResourceLocation custom=new ResourceLocation("other:arbitrary");check(GuiSound.create(custom,1).getSoundLocation()==custom,"arbitrary resource identity retained");
 }
 static SoundHandler handler;
 @SuppressWarnings("unchecked") static void resources(String folder)throws Exception{
  FolderResourcePack pack=new FolderResourcePack(new File(folder));SimpleReloadableResourceManager rm=new SimpleReloadableResourceManager(new MetadataSerializer());rm.reloadResourcePack(pack);rm.registerReloadListener(new SoundCategoryResolver());handler=new SoundHandler(rm,null);
  Method map=SoundHandler.class.getDeclaredMethod("getSoundMap",InputStream.class);map.setAccessible(true);Method load=SoundHandler.class.getDeclaredMethod("loadSoundResource",ResourceLocation.class,SoundList.class);load.setAccessible(true);
  Map<String,SoundList> definitions;JsonObject json;try(InputStream in=pack.getInputStream(new ResourceLocation("hbm:sounds.json"))){definitions=(Map<String,SoundList>)map.invoke(handler,in);}try(InputStream in=pack.getInputStream(new ResourceLocation("hbm:sounds.json"))){json=new JsonParser().parse(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();}
  for(String n:NAMES){ResourceLocation id=new ResourceLocation("hbm",n);SoundList def=definitions.get(n);load.invoke(handler,id,def);PositionedSoundRecord sound=GuiSound.create(id,.8F);check(sound.getCategory()==SoundCategory.getByName(json.getAsJsonObject(n).get("category").getAsString()),"original bundled category "+n);eq(sound.getVolume(),.25F,"pre-accessor quarter");check(sound.createAccessor(handler)!=null&&sound.getSound()!=SoundHandler.MISSING_SOUND,"actual SoundHandler resource accessor "+n);check(handler.getAccessor(id).getWeight()==def.getSounds().size(),"sample count "+n);eq(sound.getVolume(),.25F*sound.getSound().getVolume(),"sample volume applied once");eq(sound.getPitch(),.8F*sound.getSound().getPitch(),"sample pitch applied once");for(Sound sample:def.getSounds()){check(pack.resourceExists(sample.getSoundAsOggLocation()),"bundled sample exists");try(InputStream in=pack.getInputStream(sample.getSoundAsOggLocation())){check(in.read()=='O'&&in.read()=='g'&&in.read()=='g'&&in.read()=='S',"OGG header");}}}
  ResourceLocation foreign=new ResourceLocation("thirdparty:arbitrary");check(SoundEvent.REGISTRY.getObject(foreign)==null,"foreign event unregistered");
  SoundEventAccessor accessor=new SoundEventAccessor(foreign,null);accessor.addSound(new Sound("hbm:block/bobble",.5F,1.25F,1,Sound.Type.FILE,false));((SoundRegistry)get(handler,SoundHandler.class,"soundRegistry")).add(accessor);
  PositionedSoundRecord custom=GuiSound.create(foreign,.8F);check(custom.createAccessor(handler)==accessor,"arbitrary unregistered resource resolves");eq(custom.getVolume(),.125F,"foreign sample gain");eq(custom.getPitch(),1F,"foreign sample pitch");
  PositionedSoundRecord absent=GuiSound.create(new ResourceLocation("foreign:absent"),1);check(absent.createAccessor(handler)==null&&absent.getSound()==SoundHandler.MISSING_SOUND,"missing sound native missing, not failfast or fallback");
  Path tmp=Files.createTempDirectory("gui-sound-pack-");Path file=tmp.resolve("assets/thirdparty/sounds.json");Files.createDirectories(file.getParent());Files.write(file,"{\"arbitrary\":{\"category\":\"weather\",\"sounds\":[]}}".getBytes(StandardCharsets.UTF_8));rm.reloadResources(Arrays.asList(pack,new FolderResourcePack(tmp.toFile())));check(custom.getCategory()==SoundCategory.WEATHER,"existing record reload category");Files.write(file,"{\"arbitrary\":{\"category\":\"record\",\"replace\":true,\"sounds\":[]}}".getBytes(StandardCharsets.UTF_8));rm.reloadResources(Arrays.asList(pack,new FolderResourcePack(tmp.toFile())));check(custom.getCategory()==SoundCategory.RECORDS,"existing record replacement category");rm.reloadResources(Arrays.asList(pack));check(custom.getCategory()==SoundCategory.MASTER,"removed metadata resets category");
 }
 public static final class Replace {ISound input,replacement; boolean change; @SubscribeEvent(priority=EventPriority.HIGHEST) public void play(PlaySoundEvent e){input=e.getResultSound();eq(input.getVolume(),.25F,"event pre-accessor raw volume");if(change)e.setResultSound(replacement);}}
 public static final class Sink {ISound input;int calls;@SubscribeEvent(priority=EventPriority.LOWEST)public void play(PlaySoundEvent e){calls++;input=e.getResultSound();List<String> stack=new ArrayList<>();for(StackTraceElement s:Thread.currentThread().getStackTrace())stack.add(s.getClassName()+"."+s.getMethodName());for(String name:new String[]{"net.minecraft.client.audio.SoundHandler.playSound","net.minecraft.client.audio.SoundManager.playSound","net.minecraftforge.client.ForgeHooksClient.playSound"})check(stack.contains(name),"actual handler path "+name);e.setResultSound(null);}}
 static void events()throws Exception{
  SoundManager manager=(SoundManager)get(handler,SoundHandler.class,"sndManager");set(manager,SoundManager.class,"loaded",true);Replace first=new Replace();Sink last=new Sink();MinecraftForge.EVENT_BUS.register(first);MinecraftForge.EVENT_BUS.register(last);
  PositionedSoundRecord one=GuiSound.create(new ResourceLocation("hbm:alarm.singer"),1.3F);handler.playSound(one);check(first.input==one&&last.input==one&&last.calls==1,"exact original sound reaches observers and cancellation");eq(one.getPitch(),1.3F,"observer pitch raw");first.change=true;first.replacement=GuiSound.create(new ResourceLocation("foreign:unregistered"),.7F);handler.playSound(one);check(last.input==first.replacement&&last.calls==2,"event arbitrary replacement survives later observer");
  Minecraft mc=(Minecraft)U.allocateInstance(Minecraft.class);set(null,Minecraft.class,"instance",mc);set(mc,Minecraft.class,"soundHandler",handler);first.change=false;RepresentativeMethods.invoke(mc);check(last.calls==5,"three exact extracted complete production methods dispatch");MinecraftForge.EVENT_BUS.unregister(first);MinecraftForge.EVENT_BUS.unregister(last);
 }
 public static void main(String[] args)throws Exception{net.minecraft.init.Bootstrap.register();net.minecraftforge.fml.common.FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());net.minecraftforge.fml.common.ModMetadata m=new net.minecraftforge.fml.common.ModMetadata();m.modId="guisoundaudit";m.name="GUI sound audit";m.version="1";net.minecraftforge.fml.common.DummyModContainer owner=new net.minecraftforge.fml.common.DummyModContainer(m);net.minecraftforge.fml.common.Loader.instance().setupTestHarness(owner);net.minecraftforge.fml.common.Loader.instance().setActiveModContainer(owner);props(args[0]);resources(args[1]);events();System.out.println("PASS "+checks+" actual original/native property, resource, category, sample, missing, Forge cancellation/replacement and exact method checks; hardware output not run.");}
}
