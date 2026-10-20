import java.lang.reflect.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.audio.*;
import net.minecraft.network.play.server.SPacketCustomSound;
import net.minecraft.util.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.*;

/** Actual native custom packet -> client -> SoundManager -> Forge event, before audio hardware. */
public final class NativeCustomSoundProbe {
    static int checks;
    static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
    static <T>T alloc(Class<T> c)throws Exception{return NativeWorldSoundCallbackProbe.alloc(c);}
    static void set(Object x,Class<?> c,String f,Object v)throws Exception{NativeWorldSoundCallbackProbe.set(x,c,f,v);}
    public static final class Sink {
        int count;ISound input,result;List<String> frames;
        @SubscribeEvent(priority=EventPriority.LOWEST) public void sound(net.minecraftforge.client.event.sound.PlaySoundEvent e){count++;input=e.getSound();result=e.getResultSound();frames=new ArrayList<>();for(StackTraceElement f:Thread.currentThread().getStackTrace())frames.add(f.getClassName()+"."+f.getMethodName());e.setResultSound(null);}
    }
    public static void main(String[] args)throws Exception {
        net.minecraft.init.Bootstrap.register();net.minecraftforge.fml.common.FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
        net.minecraftforge.fml.common.ModMetadata m=new net.minecraftforge.fml.common.ModMetadata();m.modId="entityaudit";m.name="Native custom sound audit";m.version="1";net.minecraftforge.fml.common.DummyModContainer owner=new net.minecraftforge.fml.common.DummyModContainer(m);net.minecraftforge.fml.common.Loader.instance().setupTestHarness(owner);net.minecraftforge.fml.common.Loader.instance().setActiveModContainer(owner);
        net.minecraft.client.Minecraft mc=alloc(net.minecraft.client.Minecraft.class);set(mc,net.minecraft.client.Minecraft.class,"thread",Thread.currentThread());
        SoundHandler handler=alloc(SoundHandler.class);SoundManager manager=alloc(SoundManager.class);set(manager,SoundManager.class,"loaded",true);set(manager,SoundManager.class,"sndHandler",handler);set(handler,SoundHandler.class,"sndManager",manager);set(mc,net.minecraft.client.Minecraft.class,"soundHandler",handler);
        net.minecraft.client.network.NetHandlerPlayClient connection=alloc(net.minecraft.client.network.NetHandlerPlayClient.class);set(connection,net.minecraft.client.network.NetHandlerPlayClient.class,"client",mc);
        Path pack=Files.createTempDirectory("entity-custom-sound-audit-");List<Path> files=new ArrayList<>();for(String domain:new String[]{"hbm","thirdparty"}){Path p=pack.resolve("assets/"+domain+"/sounds.json");Files.createDirectories(p.getParent());Files.write(p,(domain.equals("hbm")?"{\"block.crateOpen\":{\"category\":\"weather\",\"sounds\":[]}}":"{\"replacement_only\":{\"category\":\"weather\",\"sounds\":[]}}").getBytes(java.nio.charset.StandardCharsets.UTF_8));files.add(p);}
        net.minecraft.client.resources.SimpleReloadableResourceManager resources=new net.minecraft.client.resources.SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer());resources.registerReloadListener(new com.hbm.sound.SoundCategoryResolver());resources.reloadResources(Arrays.asList(new net.minecraft.client.resources.FolderResourcePack(pack.toFile())));
        Sink sink=new Sink();MinecraftForge.EVENT_BUS.register(com.hbm.sound.WorldSoundCategories.class);MinecraftForge.EVENT_BUS.register(sink);
        for(String name:new String[]{"hbm:block.crateOpen","thirdparty:replacement_only","ThirdParty:MiXeD","raw sound !"}){
            SPacketCustomSound sent=new SPacketCustomSound(name,SoundCategory.MASTER,-.249,23.03125,3,.8F,50F/63F);net.minecraft.network.PacketBuffer bytes=new net.minecraft.network.PacketBuffer(new io.netty.buffer.UnpooledByteBufAllocator(false).heapBuffer());sent.writePacketData(bytes);SPacketCustomSound received=new SPacketCustomSound();received.readPacketData(bytes);bytes.release();check(received.getSoundName().equals(name),"raw string transport preserved: "+name);connection.handleCustomSound(received);check(sink.input.getClass()==PositionedSoundRecord.class,"exact native playback record");check(sink.input.getSoundLocation().equals(new ResourceLocation(name)),"native client ResourceLocation parse");check(sink.input.getCategory()==SoundCategory.MASTER,"wire category initially authoritative");check(sink.frames.contains("net.minecraft.client.network.NetHandlerPlayClient.handleCustomSound")&&sink.frames.contains("net.minecraft.client.audio.SoundManager.playSound")&&sink.frames.contains("net.minecraftforge.client.ForgeHooksClient.playSound"),"genuine native custom packet playback stack");
            if(name.startsWith("hbm:")){check(sink.result!=sink.input&&sink.result.getCategory()==SoundCategory.WEATHER,"existing bounded category wrapper covers approved source name");}
            if(name.startsWith("thirdparty:")){ResourceLocation id=new ResourceLocation(name);check(net.minecraftforge.fml.common.registry.ForgeRegistries.SOUND_EVENTS.getValue(id)==null,"replacement has no registered SoundEvent");check(com.hbm.sound.SoundCategoryResolver.get(id)==SoundCategory.WEATHER,"receiving pack replacement metadata exists");check(sink.result==sink.input&&sink.result.getCategory()==SoundCategory.MASTER,"existing allowlist misses arbitrary replacement category");}
        }
        check(sink.count==4,"one native playback event per custom packet");boolean bad=false;try{new SPacketCustomSound(null,SoundCategory.MASTER,0,0,0,1,1);}catch(NullPointerException expected){bad=true;}check(bad,"native custom packet null rejection at actual packet construction");
        try(URLClassLoader legacy=new URLClassLoader(new URL[]{new java.io.File(args[0]).toURI().toURL()},NativeCustomSoundProbe.class.getClassLoader())){Object old=legacy.loadClass("bqx").getConstructor(String.class).newInstance("ThirdParty:MiXeD");check(old.toString().equals("thirdparty:MiXeD"),"legacy path case retained");check(new ResourceLocation("ThirdParty:MiXeD").toString().equals("thirdparty:mixed"),"native path lowercased");}
        MinecraftForge.EVENT_BUS.unregister(sink);MinecraftForge.EVENT_BUS.unregister(com.hbm.sound.WorldSoundCategories.class);for(Path p:files){Files.delete(p);Files.delete(p.getParent());}Files.delete(pack.resolve("assets"));Files.delete(pack);
        System.out.println("PASS native custom packet audit: "+checks+" checks. Arbitrary raw names transported without registry; real playback event; existing allowlist demonstrably misses arbitrary replacement resource category; native ResourceLocation changes path case. No production edits, no audible-audio claim.");
    }
}
