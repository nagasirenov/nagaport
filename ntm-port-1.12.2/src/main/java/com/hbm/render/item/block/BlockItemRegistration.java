package com.hbm.render.item.block;

import com.hbm.render.item.nativeitem.AngelCoreFrameCorrection;
import com.hbm.render.item.nativeitem.staticfamily.SingleMeshEntityItemRenderer;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

/** One explicitly owned wand, registered only after native item/block registry events. */
public final class BlockItemRegistration {
    private final Item item;
    private final ItemRenderBlock renderer;
    private boolean active;
    public BlockItemRegistration(Item item, ItemRenderBlock renderer) {
        this.item = Objects.requireNonNull(item);
        this.renderer = Objects.requireNonNull(renderer);
    }
    public Map<Item, SingleMeshEntityItemRenderer.BlockItemDraw> droppedRenderers() {
        return Collections.singletonMap(item, renderer);
    }
    private ModelResourceLocation location() { return new ModelResourceLocation(Objects.requireNonNull(item.getRegistryName()), "inventory"); }
    @SubscribeEvent public void register(ModelRegistryEvent event) {
        ModelResourceLocation key = location();
        ModelBakery.registerItemVariants(item, key);
        ModelLoader.setCustomMeshDefinition(item, stack -> key);
    }
    @SubscribeEvent public void bake(ModelBakeEvent event) {
        ModelResourceLocation key = location();
        IBakedModel original = event.getModelRegistry().getObject(key);
        if(original == null) throw new IllegalStateException("Missing original conveyor-wand model: " + key);
        renderer.validateModels(event.getModelRegistry());
        BlockItemModel replacement = new BlockItemModel(original, renderer);
        TileEntityItemStackRenderer previous = item.getTileEntityItemStackRenderer();
        try {
            event.getModelRegistry().putObject(key, replacement);
            item.setTileEntityItemStackRenderer(renderer);
            active = true;
        } catch(RuntimeException | Error failure) {
            event.getModelRegistry().putObject(key, original);
            item.setTileEntityItemStackRenderer(previous);
            throw failure;
        }
    }
    @SubscribeEvent public void frame(RenderItemInFrameEvent event) {
        if(!active || event.getItem().isEmpty() || event.getItem().getItem() != item) return;
        double[] delta = AngelCoreFrameCorrection.translation(event.getEntityItemFrame().getRotation());
        GL11.glTranslated(delta[0], delta[1], delta[2]);
    }
}
