import com.hbm.main.NetworkHandler;
import com.hbm.packet.toclient.LegacyEntitySoundPacket;
import com.hbm.packet.toclient.ExplosionKnockbackPacket;
import io.netty.buffer.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.*;
import net.minecraft.network.*;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.*;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;

/** Actual scoped custom codec ownership on success, rejection, handler failure and deferred playback failure. */
public final class ScopedCodecOwnershipProbe {
    static int checks;static Minecraft mc;static FMLEmbeddedChannel channel;static NetworkHandler network;static Queue<FutureTask<?>> tasks;static NativeEntityTransportProbe.Sink sink;
    static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
    static <T>T alloc(Class<T> c)throws Exception{return NativeEntityTransportProbe.alloc(c);}
    static void set(Object o,Class<?> c,String f,Object v)throws Exception{NativeEntityTransportProbe.set(o,c,f,v);}
    static Object get(Object o,Class<?> c,String f)throws Exception{return NativeEntityTransportProbe.get(o,c,f);}
    static FMLProxyPacket incoming(byte[] bytes){return new FMLProxyPacket(new SPacketCustomPayload("ScopedOwnership",new PacketBuffer(Unpooled.wrappedBuffer(bytes.clone()))));}
    static byte[] encoded(LegacyEntitySoundPacket m){FMLProxyPacket p=(FMLProxyPacket)network.getPacketFrom(m);byte[] b=NativeEntityTransportProbe.bytes(p.payload());p.payload().release();return b;}
    static Throwable write(FMLProxyPacket p){try{channel.writeInbound(p);channel.checkException();return null;}catch(Throwable t){return t;}}
    static void malformed(byte[] b,String why){FMLProxyPacket p=incoming(b);Throwable t=write(p);check(t!=null,"malformed rejected: "+why);check(p.payload().refCnt()==0,"identified new packet released after malformed: "+why);check(tasks.isEmpty(),"malformed not scheduled: "+why);}
    static void drain()throws Exception{while(!tasks.isEmpty()){FutureTask<?> t=tasks.remove();t.run();t.get();}}
    public static void main(String[] args)throws Exception{
        NativeCustomCodecProbe.init();network=new NetworkHandler("ScopedOwnership");network.registerMessage(LegacyEntitySoundPacket.Handler.class,LegacyEntitySoundPacket.class,33,Side.CLIENT);network.registerMessage(ExplosionKnockbackPacket.Handler.class,ExplosionKnockbackPacket.class,34,Side.CLIENT);channel=(FMLEmbeddedChannel)get(null,NetworkHandler.class,"clientChannel");
        mc=alloc(Minecraft.class);set(null,Minecraft.class,"instance",mc);set(mc,Minecraft.class,"thread",Thread.currentThread());tasks=new ArrayDeque<>();set(mc,Minecraft.class,"scheduledTasks",tasks);mc.player=alloc(net.minecraft.client.entity.EntityPlayerSP.class);SoundHandler audio=alloc(SoundHandler.class);SoundManager manager=alloc(SoundManager.class);set(manager,SoundManager.class,"loaded",true);set(manager,SoundManager.class,"sndHandler",audio);set(audio,SoundHandler.class,"sndManager",manager);set(mc,Minecraft.class,"soundHandler",audio);sink=new NativeEntityTransportProbe.Sink();MinecraftForge.EVENT_BUS.register(sink);net.minecraft.client.network.NetHandlerPlayClient handler=alloc(net.minecraft.client.network.NetHandlerPlayClient.class);channel.attr(NetworkRegistry.NET_HANDLER).set(handler);
        for(Field f:LegacyEntitySoundPacket.class.getDeclaredFields())check(f.getType().isPrimitive()||f.getType()==String.class,"packet owns copied string/primitives only: "+f);
        byte[] good=encoded(new LegacyEntitySoundPacket("foreign:unregistered",1,2,3,.5F,1));FMLProxyPacket packet=incoming(good);check(packet.payload().refCnt()==1,"fresh unique inbound payload");check(!io.netty.util.ReferenceCountUtil.release(packet)&&packet.payload().refCnt()==1,"Netty automatic message release is no-op for FMLProxyPacket");
        AtomicReference<Throwable> fail=new AtomicReference<>();Thread thread=new Thread(()->{try{packet.processPacket(handler);}catch(Throwable t){fail.set(t);}},"audit-actual-FML-processPacket");thread.start();thread.join();check(fail.get()==null,"actual FMLProxyPacket.processPacket completed");check(packet.payload().refCnt()==0,"actual FML process scoped payload released exactly once");check(tasks.size()==1&&sink.calls==0,"real outer process schedules but does not play on network thread");drain();check(sink.calls==1,"copied message plays after buffer release");
        for(int size=1;size<good.length;size++)malformed(Arrays.copyOf(good,size),"truncation "+size);
        for(String name:new String[]{NativeEntityTransportProbe.repeat("a",257),NativeEntityTransportProbe.repeat("é",257)})malformed(encoded(new LegacyEntitySoundPacket(name,0,0,0,1,1)),"name length "+name.length());
        for(byte[] b:new byte[][]{{33,(byte)255,(byte)255,(byte)255,(byte)255,15},{33,(byte)128,(byte)128,(byte)128,(byte)128,(byte)128,0},{33,(byte)128,8}})malformed(b,"invalid string prefix");
        // Decoder completed and released the new message before the handler fails.
        set(null,Minecraft.class,"instance",null);FMLProxyPacket badHandler=incoming(good);check(write(badHandler)!=null,"handler failure exposed");check(badHandler.payload().refCnt()==0,"handler failure does not leak/double-release payload");set(null,Minecraft.class,"instance",mc);
        FMLProxyPacket badPlayback=incoming(good);Thread queued=new Thread(()->{Throwable t=write(badPlayback);if(t!=null)fail.set(t);},"audit-deferred-audio-failure");queued.start();queued.join();check(fail.get()==null&&tasks.size()==1&&badPlayback.payload().refCnt()==0,"scheduled failure fixture released before queued work");set(mc,Minecraft.class,"soundHandler",null);FutureTask<?> task=tasks.remove();task.run();boolean deferredFailure=false;try{task.get();}catch(ExecutionException expected){deferredFailure=expected.getCause() instanceof NullPointerException;}check(deferredFailure&&badPlayback.payload().refCnt()==0,"deferred playback failure has no buffer ownership");set(mc,Minecraft.class,"soundHandler",audio);
        FMLProxyPacket old=(FMLProxyPacket)network.getPacketFrom(new ExplosionKnockbackPacket(new Vec3d(.25,.5,-.75)));byte[] oldData=NativeEntityTransportProbe.bytes(old.payload());old.payload().release();FMLProxyPacket control=incoming(oldData);check(write(control)==null,"exact existing ExplosionKnockbackPacket handler works");check(mc.player.motionX==.25&&mc.player.motionY==.5&&mc.player.motionZ==-.75,"existing control data unchanged");check(control.payload().refCnt()==1,"existing control packet ownership unchanged");control.payload().release();
        FMLProxyPacket unknown=incoming(new byte[]{127});check(write(unknown)!=null&&unknown.payload().refCnt()==1,"unknown discriminator ownership unchanged, cannot identify new class");unknown.payload().release();FMLProxyPacket empty=incoming(new byte[0]);check(write(empty)!=null&&empty.payload().refCnt()==1,"empty envelope ownership unchanged, no new discriminator");empty.payload().release();
        MinecraftForge.EVENT_BUS.unregister(sink);System.out.println("PASS scoped actual custom codec ownership: "+checks+" checks. Actual FMLProxyPacket.processPacket success, every identified-new-packet truncation, name bounds/invalid lengths, immediate handler failure, scheduled playback failure, existing real ExplosionKnockbackPacket control unchanged. Empty/unknown envelopes remain original ownership because no new-packet class can be identified. PacketThreading lock/prefix-only fixture; no gameplay/full project initialization.");
    }
}
