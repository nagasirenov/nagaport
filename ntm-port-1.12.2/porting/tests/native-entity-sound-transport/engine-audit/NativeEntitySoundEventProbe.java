import java.lang.reflect.*;
import java.util.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import sun.misc.Unsafe;

/** Unmodified native event and World dispatch semantics only; no production helper is under test. */
public final class NativeEntitySoundEventProbe {
    static final Unsafe U;
    static {try {Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);U=(Unsafe)f.get(null);}catch(Exception x){throw new ExceptionInInitializerError(x);}}
    static int checks;
    static void check(boolean good,String reason){checks++;if(!good)throw new AssertionError(reason);}
    static <T>T alloc(Class<T> type)throws Exception{return type.cast(U.allocateInstance(type));}
    static final SoundEvent ORIGINAL=new SoundEvent(new ResourceLocation("entityaudit:original"));
    static final SoundEvent REPLACEMENT=new SoundEvent(new ResourceLocation("entityaudit:replacement"));
    public static final class Observer {
        int calls,mode; PlaySoundAtEntityEvent last;
        @SubscribeEvent public void sound(PlaySoundAtEntityEvent event){calls++;last=event;if(mode==1){event.setSound(REPLACEMENT);event.setCategory(SoundCategory.WEATHER);event.setVolume(9F);event.setPitch(7F);}if(mode==2)event.setSound(null);if(mode==3)event.setCanceled(true);}
    }
    public static final class WorldBoundary extends World {
        WorldBoundary(){super(null,null,null,null,false);}
        protected IChunkProvider createChunkProvider(){throw new AssertionError("unexpected chunk construction");}
        protected boolean isChunkLoaded(int x,int z,boolean empty){throw new AssertionError("unexpected chunk access");}
        void listeners(List<IWorldEventListener> list){eventListeners=list;}
    }
    public static void main(String[] args)throws Exception {
        net.minecraft.init.Bootstrap.register();
        net.minecraftforge.fml.common.FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
        net.minecraftforge.fml.common.ModMetadata meta=new net.minecraftforge.fml.common.ModMetadata();meta.modId="entityaudit";meta.name="Entity sound engine audit";meta.version="1";
        net.minecraftforge.fml.common.DummyModContainer owner=new net.minecraftforge.fml.common.DummyModContainer(meta);
        net.minecraftforge.fml.common.Loader.instance().setupTestHarness(owner);net.minecraftforge.fml.common.Loader.instance().setActiveModContainer(owner);
        Observer observer=new Observer();MinecraftForge.EVENT_BUS.register(observer);EntityPlayerMP player=alloc(EntityPlayerMP.class);
        observer.mode=1;PlaySoundAtEntityEvent event=ForgeEventFactory.onPlaySoundAtEntity(player,ORIGINAL,SoundCategory.BLOCKS,.5F,.75F);
        check(observer.calls==1,"factory posts exactly once");check(event==observer.last,"factory returns posted object");check(event.getEntity()==player,"exact entity identity");check(event.getSound()==REPLACEMENT,"sound setter observed");check(event.getCategory()==SoundCategory.WEATHER,"category setter observed");check(event.getVolume()==9F&&event.getPitch()==7F,"new volume and pitch mutable");check(event.getDefaultVolume()==.5F&&event.getDefaultPitch()==.75F,"default volume and pitch remain original");
        observer.mode=2;event=ForgeEventFactory.onPlaySoundAtEntity(null,ORIGINAL,SoundCategory.BLOCKS,1,1);check(observer.calls==2,"null entity event posts");check(event.getEntity()==null,"null entity preserved");check(event.getSound()==null&&!event.isCanceled(),"null sound is not event cancellation");
        List<Object[]> seen=new ArrayList<>();IWorldEventListener listener=(IWorldEventListener)Proxy.newProxyInstance(NativeEntitySoundEventProbe.class.getClassLoader(),new Class[]{IWorldEventListener.class},(p,m,a)->{if(m.getName().equals("playSoundToAllNearExcept")){seen.add(a);return null;}if(m.getName().equals("hashCode"))return System.identityHashCode(p);if(m.getName().equals("equals"))return p==a[0];return null;});
        WorldBoundary world=alloc(WorldBoundary.class);world.listeners(new ArrayList<>(Arrays.asList(listener)));
        observer.mode=1;world.playSound(player,1,2,3,ORIGINAL,SoundCategory.BLOCKS,.5F,.75F);check(observer.calls==3,"native World posts another event");check(seen.size()==1,"native callback once");Object[] a=seen.get(0);check(a[0]==player,"native world excludes supplied player");check(a[1]==REPLACEMENT&&a[2]==SoundCategory.WEATHER,"native world honors replacement and category");check((Float)a[6]==9F&&(Float)a[7]==7F,"native world honors mutable volume and pitch");
        observer.mode=2;world.playSound(null,1,2,3,ORIGINAL,SoundCategory.BLOCKS,1,1);check(observer.calls==4&&seen.size()==1,"native World treats null sound as early return despite not canceled");
        observer.mode=3;world.playSound(player,1,2,3,ORIGINAL,SoundCategory.BLOCKS,1,1);check(observer.calls==5&&seen.size()==1,"native World respects cancellation");check(observer.last.isCanceled(),"real Forge transformed event is cancelable");
        MinecraftForge.EVENT_BUS.unregister(observer);System.out.println("PASS native entity event engine probe: "+checks+" checks. Native event source identity, mutable vs immutable defaults, null not cancellation in event, World null-as-return, native player exclusion, one hook per API invocation. No production helper tested; no audio/gameplay claim.");
    }
}
