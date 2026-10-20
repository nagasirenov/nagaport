package com.hbm.sound;

import com.google.gson.*;
import com.hbm.entity.mob.EntityHunterChopper;
import com.hbm.items.machine.ItemCassette.SoundType;
import com.hbm.sound.MovingSoundPlayerLoop.EnumHbmSound;
import com.hbm.tileentity.machine.TileEntityMachineSiren;
import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.*;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.entity.Entity;
import net.minecraft.init.Bootstrap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import sun.misc.Unsafe;

/** Complete production family; actual native classes with controlled NTM and hardware boundaries. */
public final class LoopSoundNativeTest {
    static int checks;
    static final Unsafe U;
    static { try { Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);U=(Unsafe)f.get(null); } catch(Exception e){throw new ExceptionInInitializerError(e);} }
    static void check(boolean ok,String label){checks++;if(!ok)throw new AssertionError(label);}
    static void equal(float a,float b,String label){check(Float.floatToIntBits(a)==Float.floatToIntBits(b),label+": "+a+" != "+b);}
    static void field(Object o,Class<?> c,String n,Object value)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);f.set(o,value);}
    static Object field(Object o,Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f.get(o);}
    static ResourceLocation key(String s){return new ResourceLocation("hbm",s);}
    static final class TestEntity extends Entity {
        TestEntity(){super(null);}protected void entityInit(){}protected void readEntityFromNBT(NBTTagCompound n){}protected void writeEntityToNBT(NBTTagCompound n){}
    }
    static final class RecordingHandler extends SoundHandler {
        int starts,stops;ISound started,stopped;Set<ISound> playing=new HashSet<>();
        RecordingHandler(IResourceManager resources){super(resources,null);}
        @Override public void playSound(ISound sound){starts++;started=sound;playing.add(sound);}
        @Override public void stopSound(ISound sound){stops++;stopped=sound;playing.remove(sound);}
        @Override public boolean isSoundPlaying(ISound sound){return playing.contains(sound);}
    }
    static final class ObservedScheduler extends SoundManager {
        int starts,stops;ISound started,stopped;
        ObservedScheduler(SoundHandler handler){super(handler,null);}
        @Override public void playSound(ISound sound){starts++;started=sound;}
        @Override public void stopSound(ISound sound){stops++;stopped=sound;}
    }
    static Minecraft mc;static EntityPlayerSP local;static RecordingHandler handler;
    public static void main(String[] args)throws Exception{
        Bootstrap.register();
        SimpleReloadableResourceManager rm=new SimpleReloadableResourceManager(new MetadataSerializer());
        rm.reloadResourcePack(new FolderResourcePack(new File(args[0])));
        rm.registerReloadListener(new SoundCategoryResolver());
        mc=(Minecraft)U.allocateInstance(Minecraft.class);local=(EntityPlayerSP)U.allocateInstance(EntityPlayerSP.class);handler=new RecordingHandler(rm);
        field(null,Minecraft.class,"instance",mc);field(mc,Minecraft.class,"soundHandler",handler);mc.player=local;
        dynamic();wrappers();moving();tiles();scheduler();
        resources(new FolderResourcePack(new File(args[0])),Paths.get(args[2]),"folder");
        try(FileResourcePack zip=new FileResourcePack(new File(args[1]))){resources(zip,Paths.get(args[2]),"jar");}
        reloadMetadata();
        System.out.println("PASS native loop sounds: "+checks+" checks; complete 12-class family, original attenuation/keepalive/lifecycle, native resource parser and scheduler boundaries, reload categories and folder/JAR resolution");
    }
    static void dynamic(){
        local.posX=2;local.posY=4;local.posZ=6;
        AudioDynamic a=new AudioDynamic(key("block.motor"));
        check(a instanceof ITickableSound,"native tick interface");check(!a.isDonePlaying()&&a.canRepeat()&&a.getRepeatDelay()==0,"dynamic original lifecycle defaults");
        equal(a.range,10,"dynamic range default");equal(a.maxVolume,1,"dynamic max volume default");equal(a.getPitch(),1,"safe original pre-accessor pitch");equal(a.getVolume(),1,"safe original pre-accessor volume");
        check(a.getAttenuationType()==ISound.AttenuationType.NONE,"manual attenuation only");check(a.getCategory()==SoundCategory.BLOCKS,"machine source category");
        Random r=new Random(0x1234);for(int i=0;i<2000;i++){
            float volume=r.nextFloat()*7-2,range=r.nextFloat()*40-10,dist=r.nextFloat()*150-20;
            a.setVolume(volume);a.setRange(range);equal(a.func(dist),(dist/range)*-volume+volume,"exact unclamped dynamic formula");
        }
        a.setRange(0);a.setVolume(2);check(Float.isNaN(a.func(0)),"zero over zero remains NaN");check(a.func(1)==Float.NEGATIVE_INFINITY,"zero range infinity not clamped");
        a.setRange(10);a.setVolume(2);a.setPosition(5,8,6);a.update();equal(a.getUnscaledVolume(),1,"3-4-5 distance attenuation");
        a.setPosition(22,4,6);a.update();equal(a.getUnscaledVolume(),-2,"beyond range stays negative before native clamp");
        TestEntity parent=new TestEntity();parent.posX=7;parent.posY=4;parent.posZ=6;a.attachTo(parent);a.update();equal(a.getXPosF(),7,"remote parent position");equal(a.getUnscaledVolume(),1,"remote parent attenuates");
        a.attachTo(local);a.update();equal(a.getXPosF(),2,"local parent x");equal(a.getYPosF(),14,"original local +10 stereo workaround");equal(a.getUnscaledVolume(),2,"local parent full volume");
        mc.player=null;a.attachTo(parent);a.update();equal(a.getUnscaledVolume(),2,"no client player with entity full volume");
        a.attachTo(null);boolean failed=false;try{a.update();}catch(NullPointerException expected){failed=true;}check(failed,"original null player/null parent anomaly not silently repaired");mc.player=local;
        a.setKeepAlive(2);int stops=handler.stops;for(int i=0;i<3;i++)a.update();check(handler.stops==stops&&a.timeSinceKA==3,"strict keepalive greater-than allows 0,1,2");
        a.update();check(handler.stops==stops+1&&handler.stopped==a&&a.timeSinceKA==4,"expiry at previous counter3");check(!a.isDonePlaying()&&a.canRepeat(),"dynamic stop delegates without changing repeat/done");
        a.keepAlive();a.update();check(a.timeSinceKA==1&&handler.stops==stops+1,"keepalive resets counter");
        a.setKeepAlive(-1);a.keepAlive();a.update();check(handler.stops==stops+2,"negative keepalive immediate original expiry");
        a.setPitch(-2.5F);equal(a.getUnscaledPitch(),-2.5F,"negative pitch retained");a.setRepeat(false);check(!a.canRepeat(),"repeat forwarding");a.start();check(a.isPlaying()&&handler.started==a,"start exact identity");a.stop();check(!a.isPlaying(),"stop exact identity");
        TestEntity entity=new TestEntity();entity.posX=10;entity.posY=20;entity.posZ=30;AudioDynamicEntity e=new AudioDynamicEntity(key("entity.bomberLoop"),entity);
        entity.posX=13;entity.posY=24;entity.posZ=35;equal(e.getXPosF(),13,"entity live x getter");equal(e.getYPosF(),24,"entity live y getter");equal(e.getZPosF(),35,"entity live z getter");
        e.setVolume(3);e.setRange(100);e.update();float dist=(float)Math.sqrt(Math.pow(13-local.posX,2)+Math.pow(24-local.posY,2)+Math.pow(35-local.posZ,2));equal(e.getUnscaledVolume(),(dist/100)*-3+3,"entity getters feed parent attenuation");
        entity.isDead=true;stops=handler.stops;e.update();check(e.entity==null&&handler.stops==stops+1,"dead entity stop then clears entity");equal(e.getXPosF(),10,"dead entity getter falls back constructor x");equal(e.getYPosF(),20,"dead entity constructor y");e.update();check(handler.stops==stops+2,"repeated null entity stop preserved");
    }
    static void wrappers(){
        AudioWrapper server=new AudioWrapper();server.setKeepAlive(0);server.keepAlive();server.updatePosition(1,2,3);server.attachTo(local);server.updateVolume(8);server.updateRange(9);server.updatePitch(7);server.setDoesRepeat(true);server.startSound();server.stopSound();
        equal(server.getVolume(),0,"original server wrapper no-op volume");equal(server.getRange(),0,"original server range");equal(server.getPitch(),0,"original server pitch");check(!server.isPlaying(),"server no sound");
        AudioWrapperClient empty=new AudioWrapperClient(null);empty.setKeepAlive(1);empty.keepAlive();empty.updatePosition(1,2,3);empty.attachTo(local);empty.updateVolume(8);empty.updateRange(9);empty.updatePitch(7);empty.setDoesRepeat(false);empty.startSound();empty.stopSound();
        equal(empty.getVolume(),1,"null client default volume");equal(empty.getPitch(),1,"null client default pitch");equal(empty.getRange(),0,"client inherited range0 preserved");check(!empty.isPlaying(),"null client not playing");
        TestEntity dead=new TestEntity();dead.isDead=true;check(new AudioWrapperClient(key("block.motor"),dead).sound==null,"dead entity constructor rejects");check(new AudioWrapperClient(key("block.motor"),null).sound==null,"null entity constructor rejects");
        AudioWrapperClient w=new AudioWrapperClient(key("weapon.fire.tauLoop"));w.updateVolume(8);w.updateRange(42);w.updatePitch(.75F);w.updatePosition(1,2,3);w.attachTo(local);w.setKeepAlive(12);w.keepAlive();w.setDoesRepeat(false);
        check(w.sound.parentEntity==local&&w.sound.keepAlive==12&&!w.sound.canRepeat(),"all client wrapper setters");equal(w.sound.maxVolume,8,"maxvolume forwarding");equal(w.sound.range,42,"range forwarding");equal(w.getVolume(),1,"getter reports current not target volume before update");equal(w.getPitch(),.75F,"pre-playback wrapper raw pitch");
        w.sound.update();equal(w.getVolume(),8,"wrapper current volume after tick");w.startSound();check(w.isPlaying(),"client wrapper starts");int stops=handler.stops;w.stopSound();check(!w.isPlaying()&&handler.stops==stops+1&&w.sound.keepAlive==0&&w.sound.shouldExpire,"stop also sets future zero keepalive");
        AudioWrapperClient entity=new AudioWrapperClient(key("entity.bomberLoop"),new TestEntity());check(entity.sound instanceof AudioDynamicEntity,"entity wrapper real subclass");
    }
    static void moving(){
        MovingSoundPlayerLoop.globalSoundList.clear();EntityHunterChopper entity=new EntityHunterChopper();entity.posX=12;entity.posY=-3;entity.posZ=49;
        MovingSoundChopper fly=new MovingSoundChopper(key("entity.chopperFlyingLoop"),entity,EnumHbmSound.soundChopperLoop);
        check(MovingSoundPlayerLoop.globalSoundList.size()==1&&!fly.init&&fly.canRepeat()&&!fly.isDonePlaying(),"moving constructor original registration");
        check(fly.getAttenuationType()==ISound.AttenuationType.LINEAR&&fly.getCategory()==SoundCategory.HOSTILE,"chopper native category/attenuation");
        fly.update();equal(fly.getXPosF(),12,"moving x");equal(fly.getYPosF(),-3,"moving y");equal(fly.getZPosF(),49,"moving z");
        MovingSoundChopper duplicate=new MovingSoundChopper(key("entity.chopperFlyingLoop"),entity,EnumHbmSound.soundChopperLoop);check(MovingSoundPlayerLoop.globalSoundList.size()==1,"duplicate constructor not inserted");MovingSoundPlayerLoop.globalSoundList.add(fly);MovingSoundPlayerLoop.globalSoundList.add(duplicate);
        MovingSoundCrashing crash=new MovingSoundCrashing(key("entity.chopperCrashingLoop"),entity,EnumHbmSound.soundCrashingLoop);crash.update();check(crash.isDonePlaying()&&!crash.canRepeat()&&crash.player==null,"crash stops while chopper alive");
        entity.dying=true;fly.update();check(fly.isDonePlaying()&&!fly.canRepeat()&&fly.player==null,"flying stops when dying");check(MovingSoundPlayerLoop.getSoundByPlayer(entity,EnumHbmSound.soundChopperLoop)==null&&MovingSoundPlayerLoop.globalSoundList.isEmpty(),"stop removes every duplicate matching key");
        crash=new MovingSoundCrashing(key("entity.chopperCrashingLoop"),entity,EnumHbmSound.soundCrashingLoop);crash.update();check(!crash.isDonePlaying()&&crash.player==entity,"crashing while dying stays");entity.isDead=true;crash.update();check(crash.isDonePlaying()&&crash.player==null,"dead crashing stop");
        MovingSoundChopperMine mine=new MovingSoundChopperMine(key("entity.chopperMineLoop"),null,EnumHbmSound.soundMineLoop);mine.update();check(mine.isDonePlaying()&&!mine.canRepeat(),"null mine entity stop");
        mine.setDone(false);mine.setPitch(-5);mine.setVolume(-7);check(!mine.isDonePlaying(),"done reset preserved");equal(mine.getPitch(),-5,"moving negative pitch retained");equal(mine.getVolume(),-7,"moving negative volume retained");mine.stop();mine.stop();check(MovingSoundPlayerLoop.globalSoundList.isEmpty(),"repeated stop stable");
        TestEntity generic=new TestEntity();MovingSoundChopper other=new MovingSoundChopper(key("entity.chopperFlyingLoop"),generic,EnumHbmSound.soundTauLoop);other.update();check(!other.isDonePlaying(),"nonchopper entity supported");other.stop();
    }
    static void tiles(){
        TileEntity tile=new TileEntity(){};tile.setPos(new BlockPos(-2,17,55));SoundLoopMachine m=new SoundLoopMachine(key("block.motor"),tile);
        equal(m.getXPosF(),-2,"tile native blockpos x");equal(m.getYPosF(),17,"tile blockpos y");equal(m.getZPosF(),55,"tile blockpos z");equal(m.getVolume(),1,"machine volume1");equal(m.getPitch(),1,"machine pitch1");check(m.canRepeat()&&m.getRepeatDelay()==0&&m.getAttenuationType()==ISound.AttenuationType.LINEAR,"machine original defaults");
        tile.setPos(new BlockPos(99,99,99));m.update();equal(m.getXPosF(),-2,"machine retains constructor coordinates");check(!m.isDonePlaying(),"valid tile keeps running");tile.invalidate();m.update();check(m.isDonePlaying(),"native tile invalidation stops");tile.validate();m.update();check(m.isDonePlaying(),"original done latch");
        m=new SoundLoopMachine(key("block.motor"),tile);m.te=null;m.update();check(m.isDonePlaying(),"cleared tile stops");m.setVolume(-4);m.setPitch(-3);equal(m.getVolume(),-4,"machine negative volume");equal(m.getPitch(),-3,"machine negative pitch");m.stop();check(m.canRepeat(),"machine stop doesn't alter repeat");
        SoundLoopSiren.list.clear();TileEntityMachineSiren sirenTile=new TileEntityMachineSiren();sirenTile.setPos(new BlockPos(2,4,6));local.posX=2;local.posY=4;local.posZ=6;
        SoundLoopSiren s=new SoundLoopSiren(key("alarm.airRaid"),sirenTile,SoundType.LOOP);check(SoundLoopSiren.list.size()==1&&s.getTE()==sirenTile,"siren list and owner");equal(s.intendedVolume,10,"siren intended volume default");check(s.getAttenuationType()==ISound.AttenuationType.NONE,"siren manual attenuation");
        s.update();equal(s.getVolume(),2,"siren origin volume2 independent of v");check(s.canRepeat()&&!s.isDonePlaying(),"loop repeats");local.posX=17;s.update();equal(s.getVolume(),-1,"siren negative range output retained");
        Random rand=new Random(9);for(int i=0;i<2000;i++){float f=rand.nextFloat()*100-20,v=rand.nextFloat()*80-40;equal(s.func(f,v),(f/v)*-2+2,"exact unclamped siren formula");}
        check(Float.isNaN(s.func(0,0)),"siren zero range nan");mc.player=null;s.update();equal(s.getVolume(),10,"siren no player intended volume");mc.player=local;
        s.type=SoundType.PASS;s.update();check(!s.canRepeat(),"PASS doesn't repeat");s.type=SoundType.SOUND;s.update();check(!s.canRepeat(),"SOUND doesn't repeat");s.setRepeat(true);s.setRepeatDelay(-7);check(s.canRepeat()&&s.getRepeatDelay()==-7,"siren repeat/delay unmodified values");
        check(s.getPath().equals("hbm:alarm.airraid"),"native canonical sound path");s.endSound();check(s.isDonePlaying()&&SoundLoopSiren.list.contains(s),"end latches done without unregistering list");
        SoundLoopSiren wrong=new SoundLoopSiren(key("alarm.airRaid"),new TileEntity(){},SoundType.LOOP);wrong.update();check(wrong.isDonePlaying(),"wrong tile type stops");sirenTile.invalidate();SoundLoopSiren invalid=new SoundLoopSiren(key("alarm.airRaid"),sirenTile,SoundType.LOOP);invalid.update();check(invalid.isDonePlaying(),"invalid siren tile stops via superclass");
    }
    @SuppressWarnings("unchecked") static void scheduler()throws Exception{
        mc.player=local;AudioDynamic sound=new AudioDynamic(key("block.motor"));sound.attachTo(local);sound.setKeepAlive(5);
        ObservedScheduler scheduler=new ObservedScheduler(handler);scheduler.playDelayedSound(sound,2);scheduler.updateAllSounds();check(scheduler.starts==0&&sound.timeSinceKA==0,"actual native delay first tick");scheduler.updateAllSounds();check(scheduler.starts==1&&scheduler.started==sound&&sound.timeSinceKA==1,"actual native delay ticks production sound then admits once");scheduler.updateAllSounds();check(scheduler.starts==1&&sound.timeSinceKA==1,"native delay removed after admission");
        TileEntity tile=new TileEntity(){};tile.invalidate();SoundLoopMachine stopped=new SoundLoopMachine(key("block.motor"),tile);
        ((List<ITickableSound>)field(scheduler,SoundManager.class,"tickableSounds")).add(stopped);scheduler.updateAllSounds();check(stopped.isDonePlaying()&&scheduler.stops==1&&scheduler.stopped==stopped,"native scheduler calls production update, isDone and exact stop identity");
    }
    @SuppressWarnings("unchecked") static void resources(IResourcePack pack,Path manifest,String label)throws Exception{
        SimpleReloadableResourceManager rm=new SimpleReloadableResourceManager(new MetadataSerializer());rm.reloadResourcePack(pack);rm.registerReloadListener(new SoundCategoryResolver());
        SoundHandler nativeHandler=new SoundHandler(rm,null);Method map=SoundHandler.class.getDeclaredMethod("getSoundMap",InputStream.class);map.setAccessible(true);Method load=SoundHandler.class.getDeclaredMethod("loadSoundResource",ResourceLocation.class,SoundList.class);load.setAccessible(true);
        Map<String,SoundList> definitions;JsonObject json;try(InputStream in=pack.getInputStream(key("sounds.json"))){definitions=(Map<String,SoundList>)map.invoke(nativeHandler,in);}try(InputStream in=pack.getInputStream(key("sounds.json"))){json=new JsonParser().parse(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();}
        int count=0;for(String line:Files.readAllLines(manifest,StandardCharsets.UTF_8)){
            String[] parts=line.split("\t");ResourceLocation id=key(parts[0]);SoundList definition=definitions.get(parts[0]);int n=Integer.parseInt(parts[2]);
            check(SoundEvent.REGISTRY.getObject(id)==null,label+" loop intentionally requires no SoundEvent registry entry");load.invoke(nativeHandler,id,definition);
            AudioDynamic a=new AudioDynamic(id);check(a.createAccessor(nativeHandler)!=null,label+" genuine PositionedSound accessor");check(a.getSound()!=SoundHandler.MISSING_SOUND,label+" real selected sample");check(nativeHandler.getAccessor(id).getWeight()==n,label+" original sample count");check(a.getCategory()==SoundCategory.getByName(parts[1]),label+" exact original category");
            JsonArray originalSamples=json.getAsJsonObject(parts[0]).getAsJsonArray("sounds");int i=0;for(Sound sample:definition.getSounds()){
                ResourceLocation ogg=sample.getSoundAsOggLocation();check(ogg.getNamespace().equals("hbm"),label+" qualified mod sample");check(pack.resourceExists(ogg),label+" normalized sample exists "+ogg);check(sample.getType()==Sound.Type.FILE,label+" original file type");
                JsonElement j=originalSamples.get(i++);boolean stream=j.isJsonObject()&&j.getAsJsonObject().has("stream")&&j.getAsJsonObject().get("stream").getAsBoolean();check(sample.isStreaming()==stream,label+" original streaming flag");
                try(InputStream in=pack.getInputStream(ogg)){check(in.read()=='O'&&in.read()=='g'&&in.read()=='g'&&in.read()=='S',label+" actual original OGG header");}
            }
            count++;
        }check(count==69,label+" all 69 active loop selections");
        Gson gson=new GsonBuilder().registerTypeAdapter(SoundList.class,new SoundListSerializer()).create();ResourceLocation custom=key("probe.weighted");load.invoke(nativeHandler,custom,gson.fromJson("{\"sounds\":[{\"name\":\"hbm:block/motor\",\"volume\":0.5,\"pitch\":1.25}]}",SoundList.class));
        AudioWrapperClient wrapper=new AudioWrapperClient(custom);wrapper.updatePitch(.8F);wrapper.updateVolume(4);wrapper.attachTo(local);wrapper.sound.update();wrapper.sound.createAccessor(nativeHandler);
        check(wrapper.sound.getSound()!=SoundHandler.MISSING_SOUND,label+" custom multiplier source resolves");equal(wrapper.sound.getVolume(),2,label+" native selected sample volume multiply once");equal(wrapper.sound.getPitch(),1,label+" native sample pitch multiply once");equal(wrapper.getVolume(),4,label+" wrapper retains original raw current volume");equal(wrapper.getPitch(),.8F,label+" wrapper retains original raw pitch");
        AudioDynamic unknown=new AudioDynamic(key("absent"));check(unknown.createAccessor(nativeHandler)==null&&unknown.getSound()==SoundHandler.MISSING_SOUND,label+" missing sound remains missing");check(unknown.getCategory()==SoundCategory.MASTER,label+" missing metadata default master");
    }
    static IResourcePack pack(String json)throws Exception{
        Path root=Files.createTempDirectory("loop-category-pack-");Path f=root.resolve("assets/hbm/sounds.json");Files.createDirectories(f.getParent());Files.write(f,json.getBytes(StandardCharsets.UTF_8));return new FolderResourcePack(root.toFile());
    }
    static void reloadMetadata()throws Exception{
        SimpleReloadableResourceManager rm=new SimpleReloadableResourceManager(new MetadataSerializer());SoundCategoryResolver resolver=new SoundCategoryResolver();rm.registerReloadListener(resolver);List<IResourcePack> packs=new ArrayList<>();
        AudioDynamic existing=new AudioDynamic(key("probe"));check(existing.getCategory()==SoundCategory.MASTER,"empty initial manager default");
        packs.add(pack("{\"probe\":{\"category\":\"block\",\"sounds\":[]},\"default\":{\"sounds\":[]}}"));rm.reloadResources(packs);check(existing.getCategory()==SoundCategory.BLOCKS,"existing sound observes new metadata");check(SoundCategoryResolver.get(key("default"))==SoundCategory.MASTER,"omitted category original master default");
        packs.add(pack("{\"probe\":{\"category\":\"hostile\",\"sounds\":[]}}"));rm.reloadResources(packs);check(existing.getCategory()==SoundCategory.BLOCKS,"append pack retains first category");
        packs.add(pack("{\"probe\":{\"category\":\"record\",\"replace\":true,\"sounds\":[]}}"));rm.reloadResources(packs);check(existing.getCategory()==SoundCategory.RECORDS,"replace pack changes existing instance category");
        packs.add(pack("{\"probe\":{\"replace\":true,\"sounds\":[]}}"));rm.reloadResources(packs);check(existing.getCategory()==SoundCategory.MASTER,"replace omitted category resets master");
        packs.add(pack("{\"probe\":{\"category\":\"music\",\"replace\":true},\"bad\":{\"category\":\"bogus\"}}"));rm.reloadResources(packs);check(existing.getCategory()==SoundCategory.MASTER,"invalid metadata pack not partially applied");
        packs.add(pack("{\"probe\":{\"category\":\"ambient\",\"replace\":true}}"));rm.reloadResources(packs);check(existing.getCategory()==SoundCategory.AMBIENT,"later valid pack survives rejected predecessor");
        packs.add(pack("{\"probe\":{\"category\":\"music\",\"replace\":true},\"invalid_sample\":{\"sounds\":[{\"name\":\"hbm:block/motor\",\"weight\":0}]}}"));rm.reloadResources(packs);check(existing.getCategory()==SoundCategory.AMBIENT,"native invalid sample pack cannot replace valid category metadata");
        packs.add(pack("{not-json"));rm.reloadResources(packs);check(existing.getCategory()==SoundCategory.AMBIENT,"malformed JSON preserves previously valid packs");
        rm.reloadResources(Collections.emptyList());check(existing.getCategory()==SoundCategory.MASTER,"removed pack category cleared not stale");
    }
}
