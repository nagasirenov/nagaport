package com.hbm.render.item.nativeitem.staticfamily;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import sun.misc.Unsafe;

/** Actual target renderer/count/registry APIs; no game client or GL execution. */
public final class StaticItemEntityOwnershipTest {
    private static int checks;
    private static Unsafe unsafe;
    private static Method nativeCount;

    private static void check(boolean condition, String label) {
        checks++;
        if(!condition) throw new AssertionError(label);
    }

    private static final class ProbeRenderer extends SingleMeshEntityItemRenderer {
        ProbeRenderer(List<Item> items) { super(null, null, items); }
        int count(ItemStack stack) { return getModelCount(stack); }
    }

    private static final class ForeignSubclass extends RenderEntityItem {
        ForeignSubclass(RenderManager manager, RenderItem itemRenderer) { super(manager, itemRenderer); }
    }

    private static final class ForeignRenderer extends Render<EntityItem> {
        ForeignRenderer(RenderManager manager) { super(manager); }
        @Override protected ResourceLocation getEntityTexture(EntityItem entity) { return null; }
    }

    private static final class EqualItem extends Item {
        @Override public boolean equals(Object other) { return other instanceof EqualItem; }
        @Override public int hashCode() { return 1; }
    }

    private static RenderManager manager() throws Exception {
        // The real constructor initializes RenderLivingBase/TextureUtil and requires GL.
        // Bypass only unrelated construction, retaining the actual native class and public map.
        RenderManager manager = (RenderManager) unsafe.allocateInstance(RenderManager.class);
        Field map = RenderManager.class.getField("entityRenderMap");
        map.setAccessible(true);
        map.set(manager, new HashMap<Class<? extends Entity>, Render<? extends Entity>>());
        return manager;
    }

    private static RenderItem itemRenderer() throws Exception {
        return (RenderItem) unsafe.allocateInstance(RenderItem.class);
    }

    private static TickEvent.RenderTickEvent tick(TickEvent.Phase phase) {
        return new TickEvent.RenderTickEvent(phase, .5F);
    }

    private static int count(RenderEntityItem renderer, ItemStack stack) throws Exception {
        return ((Integer) nativeCount.invoke(renderer, stack)).intValue();
    }

    private static void counts() throws Exception {
        Item owned = new EqualItem(), unowned = new EqualItem();
        List<Item> identities = new ArrayList<>(Collections.singletonList(owned));
        ProbeRenderer renderer = new ProbeRenderer(identities);
        identities.clear();
        identities.add(unowned);
        RenderEntityItem vanilla = new RenderEntityItem(null, null);
        for(int size = 1; size <= 128; size++) {
            for(int damage : new int[] {0, 3, 32767}) {
                ItemStack own = new ItemStack(owned, size, damage);
                ItemStack other = new ItemStack(unowned, size, damage);
                NBTTagCompound ownTag = new NBTTagCompound(), otherTag = new NBTTagCompound();
                ownTag.setString("sentinel", "owned unchanged");
                otherTag.setString("sentinel", "unowned unchanged");
                own.setTagCompound(ownTag);
                other.setTagCompound(otherTag);
                check(renderer.count(own) == 1, "owned complete mesh drawn once");
                check(renderer.count(other) == count(vanilla, other), "unowned uses actual native count");
                int expected = size > 48 ? 5 : size > 32 ? 4 : size > 16 ? 3 : size > 1 ? 2 : 1;
                check(renderer.count(other) == expected, "native count boundary oracle");
                check(own.getItem() == owned && own.getCount() == size && own.getItemDamage() == damage,
                        "owned stack identity/count/damage unchanged");
                check(other.getItem() == unowned && other.getCount() == size && other.getItemDamage() == damage,
                        "equal-valued unowned identity/count/damage unchanged");
                check(own.getTagCompound() == ownTag && ownTag.getString("sentinel").equals("owned unchanged"),
                        "owned NBT object and value unchanged");
                check(other.getTagCompound() == otherTag && otherTag.getString("sentinel").equals("unowned unchanged"),
                        "unowned NBT object and value unchanged");
            }
        }
        check(renderer.shouldBob() == vanilla.shouldBob(), "native bobbing unchanged");
        check(renderer.shouldSpreadItems() == vanilla.shouldSpreadItems(), "native spreading unchanged");
        Method[] overrides = SingleMeshEntityItemRenderer.class.getDeclaredMethods();
        int countMethods = 0, drawMethods = 0;
        for(Method method : overrides) if(!method.isBridge()) {
            if(method.getName().equals("getModelCount")) {
                countMethods++;
                check(Modifier.isProtected(method.getModifiers()), "no public production count probe");
            } else if(method.getName().equals("doRender")) drawMethods++;
            else throw new AssertionError("Unspecified native entity seam: " + method);
        }
        check(countMethods == 1 && drawMethods == 1,
                "only original count seam and explicit optional block-helper draw dispatch are overridden");
    }

    private static void acquisitionAndStability() throws Exception {
        RenderManager manager = manager();
        RenderItem itemRenderer = itemRenderer();
        RenderEntityItem vanilla = new RenderEntityItem(manager, itemRenderer);
        Render<?> unrelated = new ForeignRenderer(manager);
        manager.entityRenderMap.put(EntityItem.class, vanilla);
        manager.entityRenderMap.put(Entity.class, unrelated);
        List<Item> identities = new ArrayList<>();
        for(int i = 0; i < 161; i++) identities.add(new Item());
        List<Item> original = new ArrayList<>(identities);
        List<String> warnings = new ArrayList<>();
        int[] managerReads = {0}, itemReads = {0};
        StaticItemEntityOwnership listener = new StaticItemEntityOwnership(identities, warnings::add,
                () -> { managerReads[0]++; return manager; },
                () -> { itemReads[0]++; return itemRenderer; });
        identities.clear();
        Item addedAfterConstruction = new Item();
        identities.add(addedAfterConstruction);
        check(managerReads[0] == 0 && itemReads[0] == 0, "construction does not initialize Minecraft");
        listener.onRenderTick(tick(TickEvent.Phase.END));
        check(managerReads[0] == 0 && itemReads[0] == 0, "END does not read client services");
        check(manager.entityRenderMap.get(EntityItem.class) == vanilla, "END preserves vanilla owner");
        listener.onRenderTick(tick(TickEvent.Phase.START));
        Render<?> installed = manager.entityRenderMap.get(EntityItem.class);
        check(installed.getClass() == SingleMeshEntityItemRenderer.class, "START replaces exact vanilla owner");
        check(installed.getRenderManager() == manager, "native supplied manager identity retained");
        Field itemField = RenderEntityItem.class.getDeclaredField("itemRenderer");
        itemField.setAccessible(true);
        check(itemField.get(installed) == itemRenderer, "native RenderItem identity retained");
        check(manager.entityRenderMap.get(Entity.class) == unrelated && manager.entityRenderMap.size() == 2,
                "no unrelated entity-renderer keys changed");
        for(Item item : original) check(count((RenderEntityItem) installed, new ItemStack(item, 64)) == 1,
                "complete stable family identity snapshot");
        check(count((RenderEntityItem) installed, new ItemStack(addedAfterConstruction, 64)) == 5,
                "later collection mutation cannot expand ownership");
        for(int i = 0; i < 100; i++) listener.onRenderTick(tick(TickEvent.Phase.START));
        check(manager.entityRenderMap.get(EntityItem.class) == installed, "repeated/reload-time ticks retain exact owner");
        check(itemReads[0] == 1, "no renderer reconstruction or changed snapshot on repeated ticks");
        check(warnings.isEmpty(), "standalone success emits no compatibility warning");
        Render<?> laterOwner = new ForeignSubclass(manager, itemRenderer);
        manager.entityRenderMap.put(EntityItem.class, laterOwner);
        for(int i = 0; i < 5; i++) listener.onRenderTick(tick(TickEvent.Phase.START));
        check(manager.entityRenderMap.get(EntityItem.class) == laterOwner, "later foreign owner never overwritten");
        check(warnings.size() == 1 && warnings.get(0).contains(ForeignSubclass.class.getName()),
                "later loss warns once and identifies actual owner");
        RenderEntityItem resetVanilla = new RenderEntityItem(manager, itemRenderer);
        manager.entityRenderMap.put(EntityItem.class, resetVanilla);
        listener.onRenderTick(tick(TickEvent.Phase.START));
        check(manager.entityRenderMap.get(EntityItem.class) == resetVanilla && warnings.size() == 1,
                "never reclaim ownership after later loss even if vanilla returns");
        check(warnings.get(0).contains("remain enabled") && warnings.get(0).contains("unsupported/unverified"),
                "warning states chosen enabled-but-unsupported compatibility policy");
    }

    private static void conflicts() throws Exception {
        RenderManager manager = manager();
        RenderItem itemRenderer = itemRenderer();
        for(Render<?> foreign : Arrays.<Render<?>>asList(new ForeignSubclass(manager, itemRenderer),
                new ForeignRenderer(manager), null)) {
            manager.entityRenderMap.put(EntityItem.class, foreign);
            List<String> warnings = new ArrayList<>();
            int[] itemReads = {0};
            StaticItemEntityOwnership listener = new StaticItemEntityOwnership(Collections.singletonList(new Item()),
                    warnings::add, () -> manager, () -> { itemReads[0]++; return itemRenderer; });
            for(int i = 0; i < 8; i++) listener.onRenderTick(tick(TickEvent.Phase.START));
            check(manager.entityRenderMap.get(EntityItem.class) == foreign, "unknown/null exact owner preserved");
            check(warnings.size() == 1, "unknown/null conflict warns once");
            check(itemReads[0] == 0, "no renderer creation on incompatible ownership");
            RenderEntityItem vanilla = new RenderEntityItem(manager, itemRenderer);
            manager.entityRenderMap.put(EntityItem.class, vanilla);
            listener.onRenderTick(tick(TickEvent.Phase.START));
            check(manager.entityRenderMap.get(EntityItem.class) == vanilla, "no deferred takeover after initial conflict");
        }
        manager.entityRenderMap.clear();
        List<String> absentWarnings = new ArrayList<>();
        new StaticItemEntityOwnership(Collections.singletonList(new Item()), absentWarnings::add,
                () -> manager, () -> itemRenderer).onRenderTick(tick(TickEvent.Phase.START));
        check(!manager.entityRenderMap.containsKey(EntityItem.class) && absentWarnings.size() == 1,
                "missing map key preserved, not populated with fallback");
        List<String> noManager = new ArrayList<>();
        new StaticItemEntityOwnership(Collections.singletonList(new Item()), noManager::add,
                () -> null, () -> itemRenderer).onRenderTick(tick(TickEvent.Phase.START));
        check(noManager.size() == 1 && noManager.get(0).contains("RenderManager is unavailable"),
                "unavailable manager is diagnostic, not a crash");
        RenderEntityItem vanilla = new RenderEntityItem(manager, itemRenderer);
        manager.entityRenderMap.put(EntityItem.class, vanilla);
        List<String> noItem = new ArrayList<>();
        new StaticItemEntityOwnership(Collections.singletonList(new Item()), noItem::add,
                () -> manager, () -> null).onRenderTick(tick(TickEvent.Phase.START));
        check(manager.entityRenderMap.get(EntityItem.class) == vanilla && noItem.size() == 1,
                "unavailable RenderItem preserves vanilla without crash");
        ForeignRenderer changed = new ForeignRenderer(manager);
        List<String> changedWarnings = new ArrayList<>();
        new StaticItemEntityOwnership(Collections.singletonList(new Item()), changedWarnings::add,
                () -> manager, () -> { manager.entityRenderMap.put(EntityItem.class, changed); return itemRenderer; })
                .onRenderTick(tick(TickEvent.Phase.START));
        check(manager.entityRenderMap.get(EntityItem.class) == changed && changedWarnings.size() == 1,
                "identity recheck preserves owner changed during preparation");
    }

    private static void actualRegistryOrder() throws Exception {
        RenderManager manager = manager();
        RenderItem itemRenderer = itemRenderer();
        RenderEntityItem vanilla = new RenderEntityItem(manager, itemRenderer);
        ForeignSubclass fromFactory = new ForeignSubclass(manager, itemRenderer);
        ForeignRenderer oldStyle = new ForeignRenderer(manager);
        manager.entityRenderMap.put(EntityItem.class, vanilla);
        int[] factoryCalls = {0};
        RenderingRegistry.registerEntityRenderingHandler(EntityItem.class, (IRenderFactory<EntityItem>) supplied -> {
            check(supplied == manager, "real registry forwards native manager");
            factoryCalls[0]++;
            return fromFactory;
        });
        List<String> warnings = new ArrayList<>();
        StaticItemEntityOwnership listener = new StaticItemEntityOwnership(Collections.singletonList(new Item()),
                warnings::add, () -> manager, () -> itemRenderer);
        RenderingRegistry.loadEntityRenderers(manager, manager.entityRenderMap);
        check(factoryCalls[0] == 1 && manager.entityRenderMap.get(EntityItem.class) == fromFactory,
                "listener construction never displaces pending factory");
        RenderingRegistry.registerEntityRenderingHandler(EntityItem.class, oldStyle);
        RenderingRegistry.loadEntityRenderers(manager.entityRenderMap);
        listener.onRenderTick(tick(TickEvent.Phase.END));
        check(warnings.isEmpty(), "no ownership check on earlier END event");
        listener.onRenderTick(tick(TickEvent.Phase.START));
        check(manager.entityRenderMap.get(EntityItem.class) == oldStyle && warnings.size() == 1,
                "first START observes and preserves final old-style renderer");
        check(warnings.get(0).contains(ForeignRenderer.class.getName()), "diagnostic identifies final live owner");
        Map<Class<? extends Entity>, Render<? extends Entity>> loadedAgain = new HashMap<>();
        RenderingRegistry.loadEntityRenderers(manager, loadedAgain);
        check(factoryCalls[0] == 2 && loadedAgain.get(EntityItem.class) == fromFactory,
                "listener never mutates Forge factory registry");
        RenderingRegistry.loadEntityRenderers(loadedAgain);
        check(loadedAgain.get(EntityItem.class) == oldStyle, "listener never mutates old-style registry");
        SubscribeEvent annotation = StaticItemEntityOwnership.class
                .getMethod("onRenderTick", TickEvent.RenderTickEvent.class).getAnnotation(SubscribeEvent.class);
        check(annotation != null && annotation.priority() == EventPriority.LOWEST,
                "late render-start listener uses native LOWEST event priority");
    }

    public static void main(String[] args) throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        unsafe = (Unsafe) field.get(null);
        nativeCount = RenderEntityItem.class.getDeclaredMethod("getModelCount", ItemStack.class);
        nativeCount.setAccessible(true);
        Bootstrap.register();
        counts();
        acquisitionAndStability();
        conflicts();
        actualRegistryOrder();
        System.out.println("PASS " + checks + " actual-native dropped count/ownership/lifecycle assertions.");
        System.out.println("LIMIT: actual native types with GL-independent construction fixtures; no client startup, rendered pixels or external-mod parity claim.");
    }
}
