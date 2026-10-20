import com.hbm.advancement.NtmAdvancements;
import com.hbm.packet.toserver.LegacyInventoryActionPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.*;
import java.util.ArrayDeque;
import java.util.concurrent.FutureTask;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import sun.misc.Unsafe;

/** Runs inside the genuine achievement manager/player fixture, using native server queue. */
public final class InventoryActionPacketNativeTest {
    private static int checks;
    private static void check(boolean value,String label) { checks++; if(!value)throw new AssertionError(label); }
    private static void set(Object target,Class<?> owner,String name,Object value) throws Exception {
        Field field=owner.getDeclaredField(name);field.setAccessible(true);field.set(target,value);
    }
    public static void run(MinecraftServer server,WorldServer world,EntityPlayerMP sender,EntityPlayerMP other) throws Exception {
        ByteBuf empty=Unpooled.buffer();
        try {
            LegacyInventoryActionPacket message=new LegacyInventoryActionPacket();
            message.toBytes(empty);check(empty.readableBytes()==0,"fixed action has no ID/player/payload");
            message.fromBytes(empty);check(empty.readerIndex()==0,"empty decode retains no buffer slice");
            empty.writeByte(1);boolean rejected=false;
            try { message.fromBytes(empty); } catch(IllegalArgumentException expected) { rejected=true; }
            check(rejected,"unexpected payload rejected");check(empty.readerIndex()==0,"malformed decode does not consume data");
        } finally { empty.release(); }
        Field unsafeField=Unsafe.class.getDeclaredField("theUnsafe");unsafeField.setAccessible(true);Unsafe unsafe=(Unsafe)unsafeField.get(null);
        NetHandlerPlayServer handler=(NetHandlerPlayServer)unsafe.allocateInstance(NetHandlerPlayServer.class);
        handler.player=sender;
        Constructor<MessageContext> constructor=MessageContext.class.getDeclaredConstructor(INetHandler.class,Side.class);
        constructor.setAccessible(true);MessageContext context=constructor.newInstance(handler,Side.SERVER);
        ArrayDeque<FutureTask<?>> queue=new ArrayDeque<>();
        set(server,MinecraftServer.class,"futureTaskQueue",queue);
        set(server,MinecraftServer.class,"serverThread",new Thread("controlled-native-server-main"));
        set(world,WorldServer.class,"server",server);
        check(!NtmAdvancements.has(sender,NtmAdvancements.OPEN_INVENTORY),"sender initially unawarded");
        check(new LegacyInventoryActionPacket.Handler().onMessage(new LegacyInventoryActionPacket(),context)==null,"no reply packet");
        check(queue.size()==1,"actual native server scheduler queues exactly one task");
        check(!NtmAdvancements.has(sender,NtmAdvancements.OPEN_INVENTORY),"network-thread handling does not mutate progress");
        set(server,MinecraftServer.class,"serverThread",Thread.currentThread());
        FutureTask<?> scheduled=queue.remove();scheduled.run();scheduled.get();
        check(NtmAdvancements.has(sender,NtmAdvancements.OPEN_INVENTORY),"scheduled work awards authenticated sender");
        check(!NtmAdvancements.has(other,NtmAdvancements.OPEN_INVENTORY),"other player unchanged");
        check(sender.getStatFile().readStat(NtmAdvancements.counter(NtmAdvancements.OPEN_INVENTORY))==1,"one scheduled attempt one counter increment");
        new LegacyInventoryActionPacket.Handler().onMessage(new LegacyInventoryActionPacket(),context);
        check(queue.isEmpty(),"native main-thread handler executes without unnecessary queued work");
        check(sender.getStatFile().readStat(NtmAdvancements.counter(NtmAdvancements.OPEN_INVENTORY))==2,"repeat inventory action increments original repeat counter");
        System.out.println("PASS "+checks+" native empty-action packet/queue/authenticated-recipient checks; not live network transport");
    }
}
