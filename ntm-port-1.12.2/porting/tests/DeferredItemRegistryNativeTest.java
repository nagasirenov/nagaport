import com.hbm.registry.GameRegistry;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.tileentity.IItemRendererProvider;
import com.hbm.render.item.nativeitem.callback.DeferredItemModelRegistration;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** Complete production queue + one-shot deferral, actual registry/event/model-event lifecycle.
 * Provider item and model listener are explicit NTM boundaries; no GL or complete ClientProxy.
 */
public final class DeferredItemRegistryNativeTest {
    private static int checks;
    private static int initializationCalls;
    private static int modelIdentityReads;
    private static int providerSnapshots;
    private static ModelRegistryEvent initializationEvent;
    private static RecordingModelRegistration modelRegistration;
    private static final ResourceLocation BLOCK_ID = new ResourceLocation("hbm", "tile.deferred_native_probe");
    private static final ResourceLocation PROVIDER_ID = new ResourceLocation("hbm", "item.deferred_native_provider");

    /** Constructed only by the full production GameRegistry reflective queue path. */
    public static final class RecordingItemBlock extends ItemBlock {
        static int constructions;
        static RecordingItemBlock constructed;
        public RecordingItemBlock(Block block) {
            super(block);
            constructions++;
            constructed = this;
        }
    }

    private static final class ProviderItem extends Item implements IItemRendererProvider {
        private final ItemRenderBase identity = new ItemRenderBase() { };
        @Override public Item getItemForRenderer() { return this; }
        @Override public ItemRenderBase getRenderer() { return identity; }
    }

    /** The parent's new initializer explicitly initializes newly registered model listeners
     * for the in-flight event. Forge doesn't retroactively add them to its dispatch snapshot. */
    public static final class RecordingModelRegistration {
        final ItemBlock itemBlock;
        final ProviderItem provider;
        final Object providerIdentity;
        int calls;
        ModelRegistryEvent lastEvent;
        RecordingModelRegistration(ItemBlock itemBlock, ProviderItem provider) {
            this.itemBlock = itemBlock;
            this.provider = provider;
            this.providerIdentity = provider.getRenderer();
        }
        @SubscribeEvent public void register(ModelRegistryEvent event) {
            calls++;
            lastEvent = event;
        }
    }

    private static void check(boolean condition, String label) {
        checks++;
        if (!condition) throw new AssertionError(label);
    }

    public static void main(String[] args) throws Exception {
        check(DeferredItemRegistryNativeTest.class.getClassLoader() == Launch.classLoader,
                "test uses genuine LaunchClassLoader");
        check(ModelRegistryEvent.class.getClassLoader() == Launch.classLoader,
                "native ModelRegistryEvent is transformed in the same loader");
        Bootstrap.register();
        FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
        ModMetadata metadata = new ModMetadata();
        metadata.modId = "hbm";
        metadata.name = "Deferred native item-registry fixture";
        metadata.version = "1";
        DummyModContainer owner = new DummyModContainer(metadata);
        Loader.instance().setupTestHarness(owner);
        Loader.instance().setActiveModContainer(owner);

        Block queuedBlock = new Block(Material.IRON);
        ProviderItem queuedProvider = new ProviderItem();
        check(!ForgeRegistries.BLOCKS.containsKey(BLOCK_ID) && !ForgeRegistries.ITEMS.containsKey(BLOCK_ID)
                && !ForgeRegistries.ITEMS.containsKey(PROVIDER_ID), "native registries begin without probe identities");
        GameRegistry.beginRegistrationTables();
        check(GameRegistry.registerBlock(queuedBlock, RecordingItemBlock.class, BLOCK_ID.toString()) == queuedBlock,
                "complete production block queue preserves block identity");
        GameRegistry.registerItem(queuedProvider, PROVIDER_ID.toString());
        check(RecordingItemBlock.constructions == 1 && RecordingItemBlock.constructed.getBlock() == queuedBlock,
                "production queue constructs its own actual ItemBlock immediately");
        check(Item.getItemFromBlock(queuedBlock) == Items.AIR,
                "pre-flush native block-to-item lookup is AIR, not queued ItemBlock");
        check(!ForgeRegistries.ITEMS.containsKey(PROVIDER_ID),
                "pre-flush native item snapshot cannot contain queued provider");

        DeferredItemModelRegistration deferred = new DeferredItemModelRegistration(event -> {
            initializationCalls++;
            initializationEvent = event;
            check(ForgeRegistries.BLOCKS.getValue(BLOCK_ID) == queuedBlock,
                    "model initialization occurs after production block queue flush");
            check(ForgeRegistries.ITEMS.getValue(PROVIDER_ID) == queuedProvider,
                    "model initialization occurs after production item queue flush");
            modelIdentityReads++;
            Item resolvedBlockItem = Item.getItemFromBlock(queuedBlock);
            check(resolvedBlockItem == RecordingItemBlock.constructed && resolvedBlockItem instanceof ItemBlock,
                    "native model-time block-to-item lookup resolves production queue's ItemBlock");
            check(ForgeRegistries.ITEMS.getValue(BLOCK_ID) == resolvedBlockItem,
                    "block mapping and genuine Forge item registry resolve identical ItemBlock");
            providerSnapshots++;
            List<ProviderItem> providers = new ArrayList<>();
            for (Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
                if (item instanceof IItemRendererProvider) providers.add((ProviderItem) item);
            }
            check(providers.size() == 1 && providers.get(0) == queuedProvider,
                    "model-time genuine registry snapshot discovers exact queued provider identity");
            check(providers.get(0).getItemsForRenderer().length == 1
                    && providers.get(0).getItemsForRenderer()[0] == queuedProvider,
                    "production provider default item ownership resolves actual queued item");
            modelRegistration = new RecordingModelRegistration((ItemBlock) resolvedBlockItem, providers.get(0));
            MinecraftForge.EVENT_BUS.register(modelRegistration);
            modelRegistration.register(event);
        });
        check(initializationCalls == 0 && modelIdentityReads == 0 && providerSnapshots == 0,
                "deferral constructor performs no initializer, identity resolution or registry snapshot");
        MinecraftForge.EVENT_BUS.register(deferred);
        check(initializationCalls == 0 && modelRegistration == null,
                "registering the deferred listener does not initialize models");
        GameRegistry.finishRegistrationTables();
        check(initializationCalls == 0 && !ForgeRegistries.BLOCKS.containsKey(BLOCK_ID)
                && !ForgeRegistries.ITEMS.containsKey(PROVIDER_ID),
                "finishing legacy tables still leaves real registries and models uninitialized");

        MinecraftForge.EVENT_BUS.post(new RegistryEvent.Register<Block>(net.minecraftforge.registries.RegistryManager.ACTIVE.getName(ForgeRegistries.BLOCKS), ForgeRegistries.BLOCKS));
        check(ForgeRegistries.BLOCKS.getValue(BLOCK_ID) == queuedBlock,
                "native Block event flushes actual production block queue");
        check(!ForgeRegistries.ITEMS.containsKey(BLOCK_ID) && !ForgeRegistries.ITEMS.containsKey(PROVIDER_ID)
                && Item.getItemFromBlock(queuedBlock) == Items.AIR,
                "Block event does not prematurely flush Item queue or construct fake mapping");
        check(initializationCalls == 0 && modelIdentityReads == 0 && providerSnapshots == 0,
                "native Block registry event does not trigger model initialization");

        MinecraftForge.EVENT_BUS.post(new RegistryEvent.Register<Item>(net.minecraftforge.registries.RegistryManager.ACTIVE.getName(ForgeRegistries.ITEMS), ForgeRegistries.ITEMS));
        check(ForgeRegistries.ITEMS.getValue(BLOCK_ID) == RecordingItemBlock.constructed,
                "native Item event flushes production queued ItemBlock");
        check(ForgeRegistries.ITEMS.getValue(PROVIDER_ID) == queuedProvider,
                "native Item event flushes production queued provider item");
        check(Item.getItemFromBlock(queuedBlock) == RecordingItemBlock.constructed,
                "native Forge registry callback populates real block-to-item mapping");
        check(initializationCalls == 0 && modelIdentityReads == 0 && providerSnapshots == 0,
                "registry flush itself still does not execute deferred initializer");

        ModelRegistryEvent first = new ModelRegistryEvent();
        MinecraftForge.EVENT_BUS.post(first);
        check(initializationCalls == 1 && initializationEvent == first && modelIdentityReads == 1 && providerSnapshots == 1,
                "genuine ModelRegistryEvent initializes once after both actual queue flushes");
        check(modelRegistration != null && modelRegistration.itemBlock == RecordingItemBlock.constructed
                && modelRegistration.provider == queuedProvider && modelRegistration.providerIdentity == queuedProvider.identity,
                "deferred registration retains actual ItemBlock/provider/model identities");
        check(modelRegistration.calls == 1 && modelRegistration.lastEvent == first,
                "explicit in-flight registration runs exactly once; newly subscribed listener is not retroactively dispatched");

        ModelRegistryEvent second = new ModelRegistryEvent();
        MinecraftForge.EVENT_BUS.post(second);
        check(initializationCalls == 1 && modelIdentityReads == 1 && providerSnapshots == 1 && initializationEvent == first,
                "second native model event does not rerun initializer or recapture identities");
        check(modelRegistration.calls == 2 && modelRegistration.lastEvent == second,
                "listener added during first model event participates normally in later dispatch");
        net.minecraftforge.fml.common.eventhandler.EventBus reentrantBus = new net.minecraftforge.fml.common.eventhandler.EventBus();
        int[] reentrantInitializations = {0};
        DeferredItemModelRegistration reentrant = new DeferredItemModelRegistration(event -> {
            reentrantInitializations[0]++;
            reentrantBus.post(new ModelRegistryEvent());
        });
        reentrantBus.register(reentrant);
        reentrantBus.post(new ModelRegistryEvent());
        check(reentrantInitializations[0] == 1,
                "initializer marked before invocation prevents recursive native ModelRegistryEvent initialization");
        reentrantBus.post(new ModelRegistryEvent());
        check(reentrantInitializations[0] == 1, "recursive event path remains initialized on later event");

        net.minecraftforge.fml.common.eventhandler.EventBus failureBus = new net.minecraftforge.fml.common.eventhandler.EventBus(
                (bus, event, listeners, index, failure) -> { });
        int[] failedInitializations = {0};
        RuntimeException expectedFailure = new RuntimeException("deliberate fixture initializer startup failure");
        failureBus.register(new DeferredItemModelRegistration(event -> {
            failedInitializations[0]++;
            throw expectedFailure;
        }));
        RuntimeException observedFailure = null;
        try { failureBus.post(new ModelRegistryEvent()); }
        catch (RuntimeException failure) { observedFailure = failure; }
        check(observedFailure == expectedFailure && failedInitializations[0] == 1,
                "native event dispatch propagates initialization failure rather than hiding startup error");
        failureBus.post(new ModelRegistryEvent());
        check(failedInitializations[0] == 1, "failed initializer is not partially retried by another model event");

        System.out.println("PASS " + checks + " native deferred-registry assertions: full production GameRegistry queue, "
                + "native Block then Item registry events, real ItemBlock mapping/provider identity, "
                + "constructor/pre-flush deferral, genuine ModelRegistryEvent dispatch, one-shot/reentrant initialization and failure propagation. "
                + "Controlled model provider/listener only; not complete ClientProxy/model bake/GL or loaded-game acceptance.");
    }
}
