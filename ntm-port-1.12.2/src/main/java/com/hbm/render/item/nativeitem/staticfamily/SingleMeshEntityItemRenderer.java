package com.hbm.render.item.nativeitem.staticfamily;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;
import java.util.Map;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** Native dropped-item rendering with one complete mesh for explicitly owned item identities. */
public class SingleMeshEntityItemRenderer extends RenderEntityItem {
    private final Set<Item> ownedItems;
    private final Map<Item, BlockItemDraw> blockItems;

    /** Exact legacy block-helper draw for explicitly selected identities only. */
    public interface BlockItemDraw {
        default boolean handlesDropped(ItemStack stack) { return true; }
        void renderDropped(EntityItem entity, double x, double y, double z, float yaw,
                float partialTicks, boolean outlines, int outlineColor);
    }

    public SingleMeshEntityItemRenderer(RenderManager manager, RenderItem renderer,
            Collection<? extends Item> ownedItems) {
        this(manager, renderer, ownedItems, Collections.emptyMap());
    }

    public SingleMeshEntityItemRenderer(RenderManager manager, RenderItem renderer,
            Collection<? extends Item> ownedItems, Map<? extends Item, ? extends BlockItemDraw> blockItems) {
        super(manager, renderer);
        Map<Item, BlockItemDraw> blocks = new IdentityHashMap<>();
        for(Map.Entry<? extends Item, ? extends BlockItemDraw> entry : Objects.requireNonNull(blockItems, "blockItems").entrySet())
            blocks.put(Objects.requireNonNull(entry.getKey(), "block item"), Objects.requireNonNull(entry.getValue(), "block draw"));
        this.blockItems = Collections.unmodifiableMap(blocks);
        Set<Item> identities = Collections.newSetFromMap(new IdentityHashMap<Item, Boolean>());
        for(Item item : Objects.requireNonNull(ownedItems, "ownedItems"))
            identities.add(Objects.requireNonNull(item, "owned item"));
        for(Item item : blocks.keySet()) if(identities.contains(item))
            throw new IllegalArgumentException("An item cannot own both one-mesh and block-helper dropped rendering");
        this.ownedItems = Collections.unmodifiableSet(identities);
    }

    @Override public void doRender(EntityItem entity, double x, double y, double z, float yaw, float partialTicks) {
        BlockItemDraw block = blockItems.get(entity.getItem().getItem());
        if(block == null || !block.handlesDropped(entity.getItem())) { super.doRender(entity, x, y, z, yaw, partialTicks); return; }
        block.renderDropped(entity, x, y, z, yaw, partialTicks, renderOutlines,
                renderOutlines ? getTeamColor(entity) : 0);
        // RenderEntityItem finishes with Render.doRender, whose only action is this name draw.
        renderName(entity, x, y, z);
    }

    @Override
    protected int getModelCount(ItemStack stack) {
        return ownedItems.contains(stack.getItem()) ? 1 : super.getModelCount(stack);
    }
}
