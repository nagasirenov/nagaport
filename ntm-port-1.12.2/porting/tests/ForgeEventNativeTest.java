import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Event plumbing proof only. Null entity payloads intentionally avoid world fixtures. */
public final class ForgeEventNativeTest {
    private static int checks;
    private static int entityAttach;
    private static int itemAttach;
    private static int clone;
    private static int player;

    public static final class Subscriber {
        @SubscribeEvent public void attach(AttachCapabilitiesEvent<Entity> event) { entityAttach++; }
        @SubscribeEvent public void item(AttachCapabilitiesEvent<ItemStack> event) { itemAttach++; }
        @SubscribeEvent public void cloned(PlayerEvent.Clone event) { clone++; }
        @SubscribeEvent public void player(PlayerEvent event) { player++; }
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        check(ForgeEventNativeTest.class.getClassLoader() == Launch.classLoader, "Test belongs to native launch loader");
        check(Event.class.getClassLoader() == Launch.classLoader, "Forge Event belongs to native launch loader");
        check(Launch.classLoader.getTransformers().get(0).getClass().getClassLoader() == Launch.classLoader,
                "Forge transformer resolves event parents in same loader");
        net.minecraft.init.Bootstrap.register();
        FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
        ModMetadata meta = new ModMetadata();
        meta.modId = "nativeeventprobe";
        meta.name = "Native event probe";
        meta.version = "1";
        Loader.instance().setupTestHarness(new DummyModContainer(meta));
        EventBus bus = new EventBus();
        Subscriber subscriber = new Subscriber();
        bus.register(subscriber);
        AttachCapabilitiesEvent<Entity> attach = new AttachCapabilitiesEvent<>(Entity.class, null);
        PlayerEvent.Clone cloned = new PlayerEvent.Clone(null, null, true);
        check(AttachCapabilitiesEvent.class.getConstructor().newInstance() instanceof AttachCapabilitiesEvent,
                "Forge supplied native event no-arg constructor");
        check(PlayerEvent.Clone.class.getConstructor().newInstance() instanceof PlayerEvent.Clone,
                "Forge supplied native Clone no-arg constructor");
        check(attach.getListenerList() != cloned.getListenerList(), "Distinct per-event native listener lists");
        bus.post(attach);
        check(entityAttach == 1 && itemAttach == 0 && clone == 0 && player == 0,
                "Native Entity attach dispatch excludes other generic and unrelated listeners");
        bus.post(new AttachCapabilitiesEvent<ItemStack>(ItemStack.class, ItemStack.EMPTY));
        check(entityAttach == 1 && itemAttach == 1 && clone == 0 && player == 0,
                "Native ItemStack attach dispatch excludes Entity listener");
        bus.post(cloned);
        check(entityAttach == 1 && itemAttach == 1 && clone == 1 && player == 1,
                "Native Clone reaches own and parent PlayerEvent listeners");
        bus.unregister(subscriber);
        bus.post(attach);
        bus.post(cloned);
        check(entityAttach == 1 && itemAttach == 1 && clone == 1 && player == 1, "Native unregister removes listeners");
        System.out.println("PASS: " + checks + " native Forge event assertions; actual EventBus attach/clone dispatch, "
                + "generic filtering, inherited listeners and unregister.");
    }
}
