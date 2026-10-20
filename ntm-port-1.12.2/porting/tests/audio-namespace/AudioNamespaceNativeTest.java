import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.google.gson.*;
import com.hbm.sound.SoundCategoryResolver;
import net.minecraft.client.audio.*;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.*;
import net.minecraft.init.Bootstrap;
/** Genuine SoundHandler private parser/loader, as in the existing client/loop probes. No audio engine initialization. */
public class AudioNamespaceNativeTest {
 static int checks;static void check(boolean x,String why){checks++;if(!x)throw new AssertionError(why);}
 public static void main(String[] a)throws Exception{
  Bootstrap.register();
  check(new ResourceLocation("block/minerOperate").toString().equals("minecraft:block/mineroperate"),"native default domain and path case");
  check(new ResourceLocation("hbm:block/minerOperate").toString().equals("hbm:block/mineroperate"),"native HBM case");
  resources(new FolderResourcePack(new File(a[0])),"folder");
  try(FileResourcePack p=new FileResourcePack(new File(a[1]))){resources(p,"jar");}
  System.out.println("PASS "+checks+" native checks; all 408 events, all samples, actual folder/JAR resources; no OpenAL, audible output, or launched gameplay claim");
 }
 @SuppressWarnings("unchecked") static void resources(IResourcePack pack,String label)throws Exception{
  SimpleReloadableResourceManager rm=new SimpleReloadableResourceManager(new MetadataSerializer());rm.reloadResourcePack(pack);rm.registerReloadListener(new SoundCategoryResolver());
  SoundHandler h=new SoundHandler(rm,null);
  Method parse=SoundHandler.class.getDeclaredMethod("getSoundMap",InputStream.class);parse.setAccessible(true);
  Method load=SoundHandler.class.getDeclaredMethod("loadSoundResource",ResourceLocation.class,SoundList.class);load.setAccessible(true);
  Map<String,SoundList> defs;JsonObject json;
  try(InputStream in=pack.getInputStream(new ResourceLocation("hbm:sounds.json"))){defs=(Map<String,SoundList>)parse.invoke(h,in);}
  try(InputStream in=pack.getInputStream(new ResourceLocation("hbm:sounds.json"))){json=new JsonParser().parse(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();}
  check(defs.size()==408,label+" all events");Set<ResourceLocation> ids=new HashSet<>();int missing=0,samples=0;
  for(Map.Entry<String,SoundList> e:defs.entrySet()){
   ResourceLocation id=new ResourceLocation("hbm",e.getKey());check(ids.add(id),label+" event key collision "+id);load.invoke(h,id,e.getValue());
   JsonObject j=json.getAsJsonObject(e.getKey());check(SoundCategoryResolver.get(id)==SoundCategory.getByName(j.get("category").getAsString()),label+" category "+id);
   JsonArray arr=j.getAsJsonArray("sounds");check(arr.size()==e.getValue().getSounds().size(),label+" sample order/count");int index=0,weight=0;
   for(Sound s:e.getValue().getSounds()){
    samples++;JsonElement raw=arr.get(index++);JsonObject o=raw.isJsonObject()?raw.getAsJsonObject():new JsonObject();String name=raw.isJsonObject()?o.get("name").getAsString():raw.getAsString();
    check(s.getSoundLocation().equals(new ResourceLocation(name)),label+" name");check(s.getType()==Sound.Type.FILE,label+" file not event");
    check(s.isStreaming()==(o.has("stream")&&o.get("stream").getAsBoolean()),label+" stream");
    check(s.getWeight()==(o.has("weight")?o.get("weight").getAsInt():1),label+" weight");
    check(s.getVolume()==(o.has("volume")?o.get("volume").getAsFloat():1F),label+" volume");
    check(s.getPitch()==(o.has("pitch")?o.get("pitch").getAsFloat():1F),label+" pitch");
    ResourceLocation ogg=s.getSoundAsOggLocation();
    boolean absent=Arrays.asList("fm.clap","fm.mug","fm.sample").contains(e.getKey());
    if(absent){missing++;check(!pack.resourceExists(ogg)&&ogg.getNamespace().equals("minecraft"),label+" unresolved original preserved");continue;}
    check(ogg.getNamespace().equals("hbm"),label+" HBM namespace "+ogg);check(pack.resourceExists(ogg),label+" resource exists "+ogg);
    try(IResource resource=rm.getResource(ogg)){InputStream in=resource.getInputStream();check(in.read()=='O'&&in.read()=='g'&&in.read()=='g'&&in.read()=='S',label+" OGG header "+ogg);}
    weight+=s.getWeight();
   }
   check(h.getAccessor(id)!=null,label+" native accessor "+id);check(h.getAccessor(id).getWeight()==weight,label+" native weight "+id);
   check((h.getAccessor(id).cloneEntry()==SoundHandler.MISSING_SOUND)==(weight==0),label+" missing/real native sample "+id);
  }
  check(missing==3,label+" exact original unresolved count");System.out.println("PASS "+label+" "+defs.size()+" events, "+samples+" sample refs, "+missing+" unchanged unresolved");
 }
}
