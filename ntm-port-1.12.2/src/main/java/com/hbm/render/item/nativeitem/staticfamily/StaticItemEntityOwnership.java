package com.hbm.render.item.nativeitem.staticfamily;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Acquires only the vanilla EntityItem renderer after both Forge renderer registries load.
 * The native item models stay active when another mod owns this renderer; that combination
 * is explicitly unsupported for dropped-copy parity, never silently overwritten or crashed.
 */
public final class StaticItemEntityOwnership {
    private final List<Item> ownedItems;
    private final Map<Item, SingleMeshEntityItemRenderer.BlockItemDraw> blockItems;
    private final Consumer<String> warning;
    private final Supplier<RenderManager> managers;
    private final Supplier<RenderItem> renderers;
    private boolean attempted;
    private boolean warned;
    private SingleMeshEntityItemRenderer installed;

    public StaticItemEntityOwnership(Collection<? extends Item> ownedItems, Consumer<String> warning) {
        this(ownedItems, Collections.emptyMap(), warning);
    }

    public StaticItemEntityOwnership(Collection<? extends Item> ownedItems,
            Map<? extends Item, ? extends SingleMeshEntityItemRenderer.BlockItemDraw> blockItems, Consumer<String> warning) {
        this(ownedItems, blockItems, warning, () -> Minecraft.getMinecraft().getRenderManager(),
                () -> Minecraft.getMinecraft().getRenderItem());
    }

    // Suppliers keep the lifecycle testable without starting a client or initializing GL.
    StaticItemEntityOwnership(Collection<? extends Item> ownedItems, Consumer<String> warning,
            Supplier<RenderManager> managers, Supplier<RenderItem> renderers) {
        this(ownedItems, Collections.emptyMap(), warning, managers, renderers);
    }

    StaticItemEntityOwnership(Collection<? extends Item> ownedItems,
            Map<? extends Item, ? extends SingleMeshEntityItemRenderer.BlockItemDraw> blockItems, Consumer<String> warning,
            Supplier<RenderManager> managers, Supplier<RenderItem> renderers) {
        Map<Item, SingleMeshEntityItemRenderer.BlockItemDraw> blocks = new IdentityHashMap<>();
        for(Map.Entry<? extends Item, ? extends SingleMeshEntityItemRenderer.BlockItemDraw> entry : Objects.requireNonNull(blockItems, "blockItems").entrySet())
            blocks.put(Objects.requireNonNull(entry.getKey(), "block item"), Objects.requireNonNull(entry.getValue(), "block draw"));
        this.blockItems = Collections.unmodifiableMap(blocks);
        List<Item> identities = new ArrayList<>();
        for(Item item : Objects.requireNonNull(ownedItems, "ownedItems"))
            identities.add(Objects.requireNonNull(item, "owned item"));
        this.ownedItems = Collections.unmodifiableList(identities);
        this.warning = Objects.requireNonNull(warning, "warning");
        this.managers = Objects.requireNonNull(managers, "managers");
        this.renderers = Objects.requireNonNull(renderers, "renderers");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if(event.phase != TickEvent.Phase.START || (attempted && installed == null)) return;

        RenderManager manager = managers.get();
        if(attempted) {
            Render<?> current = manager == null ? null : manager.entityRenderMap.get(EntityItem.class);
            if(current != installed) warnOnce(current, "renderer ownership changed after installation");
            return;
        }

        attempted = true;
        if(manager == null) {
            warnOnce(null, "RenderManager is unavailable");
            return;
        }
        Map<Class<? extends Entity>, Render<? extends Entity>> map = manager.entityRenderMap;
        Render<?> current = map.get(EntityItem.class);
        // A subclass is another owner's renderer too, even if it inherits vanilla counting.
        if(current == null || current.getClass() != RenderEntityItem.class) {
            warnOnce(current, "the live renderer is not the exact vanilla RenderEntityItem");
            return;
        }
        RenderItem renderer = renderers.get();
        if(renderer == null) {
            warnOnce(current, "RenderItem is unavailable");
            return;
        }
        SingleMeshEntityItemRenderer replacement =
                new SingleMeshEntityItemRenderer(manager, renderer, ownedItems, blockItems);
        if(map.get(EntityItem.class) != current) {
            warnOnce(map.get(EntityItem.class), "renderer ownership changed while preparing installation");
            return;
        }
        // This runs on the client render thread; never register a competing Forge factory.
        map.put(EntityItem.class, replacement);
        installed = replacement;
    }

    private void warnOnce(Render<?> owner, String reason) {
        if(warned) return;
        warned = true;
        String ownerName = owner == null ? "null" : owner.getClass().getName();
        warning.accept("Native static-item models remain enabled, but dropped-item single-mesh parity "
                + "is unsupported/unverified: " + reason + ". EntityItem renderer: " + ownerName
                + ". The current renderer was preserved; no replacement will be attempted. "
                + "Affected item identities: " + (ownedItems.size() + blockItems.size()) + ".");
    }
}
