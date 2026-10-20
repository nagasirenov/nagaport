import com.hbm.main.ModSoundEvents;
import com.hbm.sound.*;
import com.google.gson.*;
import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.audio.*;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.*;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.RegistryManager;

/** Genuine native registry, event bus, sound accessors and resources; no audio hardware. */
public final class WorldSoundRegistryCategoryTest {
 static int checks; static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 static PositionedSoundRecord record(ResourceLocation name){return new PositionedSoundRecord(name,SoundCategory.BLOCKS,0.7F,0.8F,false,0,ISound.AttenuationType.LINEAR,1.25F,-2.5F,3.75F);}
 static PlaySoundEvent post(ISound sound){PlaySoundEvent event=new PlaySoundEvent(null,sound);MinecraftForge.EVENT_BUS.post(event);return event;}
 public static class Earlier {
  int mode; ISound replacement;
  @SubscribeEvent(priority=EventPriority.HIGH) public void sound(PlaySoundEvent e){if(mode==1)e.setResultSound(null);if(mode==2)e.setResultSound(replacement);}
 }
 static FolderResourcePack pack(String json)throws Exception{Path p=Files.createTempDirectory("world-sound-pack-");Path f=p.resolve("assets/hbm/sounds.json");Files.createDirectories(f.getParent());Files.write(f,json.getBytes(StandardCharsets.UTF_8));return new FolderResourcePack(p.toFile());}
 @SuppressWarnings("unchecked")
 public static void main(String[] args)throws Exception{
  Bootstrap.register();FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
  ModMetadata md=new ModMetadata();md.modId="hbm";md.name="Native world sound proof";md.version="1";DummyModContainer owner=new DummyModContainer(md);Loader.instance().setupTestHarness(owner);Loader.instance().setActiveModContainer(owner);
  JsonObject manifest=new JsonParser().parse(new FileReader(args[1])).getAsJsonObject();JsonObject names=manifest.getAsJsonObject("names");
  SoundEvent original=ModSoundEvents.BLOCK_CRATE_BREAK;int initial=ForgeRegistries.SOUND_EVENTS.getValuesCollection().size();
  Map<ResourceLocation,SoundEvent> identities=new LinkedHashMap<>();
  for(Map.Entry<String,JsonElement> entry:names.entrySet()){
   ResourceLocation name=new ResourceLocation("hbm",entry.getKey());SoundEvent sound=ModSoundEvents.worldEffect(name);identities.put(name,sound);
   check(sound.getSoundName().equals(name)&&sound.getRegistryName().equals(name),"canonical native identity");check(!ForgeRegistries.SOUND_EVENTS.containsKey(name),"no premature registration");check(ModSoundEvents.resolve(name.toString())==sound,"pre-reg identity");
   check(ModSoundEvents.worldEffectCategory(name)==SoundCategory.getByName(entry.getValue().getAsJsonObject().get("category").getAsString()),"bundled non-fallback category");
  }
  check(identities.size()==87,"exact 87 bounded world events");
  Method create=ModSoundEvents.class.getDeclaredMethod("create",String.class);create.setAccessible(true);check(create.invoke(null,"block.crateOpen")==ModSoundEvents.worldEffect(new ResourceLocation("hbm:block.crateOpen")),"duplicate declaration reuses exact event");check(create.invoke(null,"block.crateBreak")==original,"existing identity retained");
  MinecraftForge.EVENT_BUS.register(ModSoundEvents.class);RegistryEvent.Register<SoundEvent> registryEvent=new RegistryEvent.Register<>(RegistryManager.ACTIVE.getName(ForgeRegistries.SOUND_EVENTS),ForgeRegistries.SOUND_EVENTS);MinecraftForge.EVENT_BUS.post(registryEvent);
  check(ForgeRegistries.SOUND_EVENTS.getValuesCollection().size()==initial+17+87,"register existing17 + bounded87 exactly once");MinecraftForge.EVENT_BUS.post(new RegistryEvent.Register<SoundEvent>(RegistryManager.ACTIVE.getName(ForgeRegistries.SOUND_EVENTS),ForgeRegistries.SOUND_EVENTS));check(ForgeRegistries.SOUND_EVENTS.getValuesCollection().size()==initial+104,"repeat registry event creates no duplicate");
  for(Map.Entry<ResourceLocation,SoundEvent> entry:identities.entrySet())check(ForgeRegistries.SOUND_EVENTS.getValue(entry.getKey())==entry.getValue()&&SoundEvent.REGISTRY.getObject(entry.getKey())==entry.getValue(),"real registry object identity");check(ModSoundEvents.BLOCK_CRATE_BREAK==original,"existing field identity unchanged");
  for(String unsupported:new String[]{"hbm:item.upgradePlug","hbm:entity.slicer","hbm:misc.nullChopper","hbm:entity.vomit","other:block.crateopen","minecraft:random.click"}){ResourceLocation name=new ResourceLocation(unsupported);check(!ModSoundEvents.isWorldEffect(name),"explicit unsupported gate "+name);try{ModSoundEvents.worldEffect(name);throw new AssertionError("unknown accepted");}catch(IllegalArgumentException expected){checks++;}}
  FolderResourcePack bundled=new FolderResourcePack(new File(args[0]));SimpleReloadableResourceManager rm=new SimpleReloadableResourceManager(new MetadataSerializer());rm.reloadResourcePack(bundled);rm.registerReloadListener(new SoundCategoryResolver());
  SoundHandler handler=new SoundHandler(rm,null);Method parse=SoundHandler.class.getDeclaredMethod("getSoundMap",InputStream.class);parse.setAccessible(true);Method load=SoundHandler.class.getDeclaredMethod("loadSoundResource",ResourceLocation.class,SoundList.class);load.setAccessible(true);Map<String,SoundList> parsed;try(InputStream in=bundled.getInputStream(new ResourceLocation("hbm:sounds.json"))){parsed=(Map<String,SoundList>)parse.invoke(handler,in);}
  MinecraftForge.EVENT_BUS.register(WorldSoundCategories.class);Earlier earlier=new Earlier();MinecraftForge.EVENT_BUS.register(earlier);
  for(Map.Entry<String,JsonElement> entry:names.entrySet()){
   ResourceLocation name=new ResourceLocation("hbm",entry.getKey());load.invoke(handler,name,parsed.get(entry.getKey()));PositionedSoundRecord raw=record(name);ISound wrapped=post(raw).getResultSound();
   check(wrapped!=raw,"registered migrated record wrapped");check(wrapped.getCategory()==ModSoundEvents.worldEffectCategory(name),"original bundled metadata category");check(wrapped.getSoundLocation().equals(name),"sound key unchanged");
   check(wrapped.getXPosF()==1.25F&&wrapped.getYPosF()==-2.5F&&wrapped.getZPosF()==3.75F,"float coordinates unchanged");check(!wrapped.canRepeat()&&wrapped.getRepeatDelay()==0&&wrapped.getAttenuationType()==ISound.AttenuationType.LINEAR,"record flags unchanged");
   check(wrapped.createAccessor(handler)==handler.getAccessor(name),"native accessor delegated");check(wrapped.getSound()!=SoundHandler.MISSING_SOUND,"bundled native sample actually resolves");check(wrapped.getSound()==raw.getSound()&&wrapped.getVolume()==raw.getVolume()&&wrapped.getPitch()==raw.getPitch(),"same sample and scaling exactly once");
  }
  ResourceLocation name=new ResourceLocation("hbm:block.crateOpen");ISound held=post(record(name)).getResultSound();
  earlier.mode=1;check(post(record(name)).getResultSound()==null,"prior vacuum-style null cancellation preserved");earlier.mode=2;earlier.replacement=record(name);check(post(record(name)).getResultSound()==earlier.replacement,"earlier replacement preserved even same native class/name");earlier.mode=0;
  for(String unsupported:new String[]{"hbm:item.upgradePlug","hbm:entity.slicer","hbm:misc.nullChopper","other:block.crateopen","minecraft:music.game"}){ISound raw=record(new ResourceLocation(unsupported));check(post(raw).getResultSound()==raw,"existing/unknown path not overridden");}
  PositionedSoundRecord subclass=new PositionedSoundRecord(name,SoundCategory.MASTER,1F,1F,false,0,ISound.AttenuationType.LINEAR,0,0,0){};check(post(subclass).getResultSound()==subclass,"custom subclass retains ownership");
  List<IResourcePack> packs=new ArrayList<>();packs.add(bundled);packs.add(pack("{\"block.crateOpen\":{\"category\":\"music\",\"sounds\":[]}}"));rm.reloadResources(packs);check(held.getCategory()==SoundCategory.BLOCKS,"append preserves first category");
  packs.add(pack("{\"block.crateOpen\":{\"category\":\"record\",\"replace\":true,\"sounds\":[]}}"));rm.reloadResources(packs);check(held.getCategory()==SoundCategory.RECORDS,"replace updates existing wrapper");
  packs.add(pack("{\"block.crateOpen\":{\"replace\":true,\"sounds\":[]}}"));rm.reloadResources(packs);check(held.getCategory()==SoundCategory.MASTER,"replace without category default");
  packs.add(pack("{\"block.crateOpen\":{\"category\":\"music\",\"replace\":true},\"invalid\":{\"category\":\"bogus\"}}"));rm.reloadResources(packs);check(held.getCategory()==SoundCategory.MASTER,"invalid pack cannot partially alter metadata");
  rm.reloadResources(Collections.emptyList());check(held.getCategory()==SoundCategory.MASTER,"removed metadata is not stale");rm.reloadResources(Arrays.asList(bundled));check(held.getCategory()==SoundCategory.BLOCKS,"reload restores original category");
  System.out.println("PASS WorldSoundRegistryCategoryTest: "+checks+" assertions; registry identities, native event/category bridge,87 resource-backed sample selections, reload/replace/cancellation/ownership gates; no audio device or game launch");
 }
}
