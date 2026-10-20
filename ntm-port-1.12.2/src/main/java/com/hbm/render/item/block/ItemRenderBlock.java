package com.hbm.render.item.block;

import com.hbm.main.ConveyorBlockModelHandler;
import com.hbm.render.item.nativeitem.staticfamily.SingleMeshEntityItemRenderer;
import com.hbm.render.util.NativeItemQuadRenderer;
import com.hbm.render.util.NativeMachineItemRenderer;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

/** Native conveyor-wand renderer. The only original owner selects four blocks by
 * damage, always metadata zero. Camera/helper transforms belong to BlockItemModel;
 * this draws full baked inventory geometry, never world-neighbor geometry. */
public class ItemRenderBlock extends TileEntityItemStackRenderer implements SingleMeshEntityItemRenderer.BlockItemDraw {
    private final Block[] blocks;
    public ItemRenderBlock(Block... blocks) { this.blocks = blocks; }

    public IBakedModel inventoryModel(int damage) {
        ModelManager models = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager();
        IBakedModel model = models.getModel(ConveyorBlockModelHandler.inventoryModelLocation(blocks[damage], 0));
        if(model == models.getMissingModel() || model.isBuiltInRenderer())
            throw new IllegalStateException("Missing drawable conveyor inventory model: " + blocks[damage].getRegistryName());
        return model;
    }
    void validateModels(net.minecraft.util.registry.IRegistry<net.minecraft.client.renderer.block.model.ModelResourceLocation, IBakedModel> models) {
        for(Block block : blocks) {
            IBakedModel model = models.getObject(ConveyorBlockModelHandler.inventoryModelLocation(block, 0));
            if(model == null || model.isBuiltInRenderer())
                throw new IllegalStateException("Missing drawable conveyor inventory model at bake: " + block.getRegistryName());
        }
    }

    @Override public void renderByItem(ItemStack stack) {
        if(stack.isEmpty()) return;
        IBakedModel model = inventoryModel(stack.getItemDamage());
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        // The original dedicated RenderBlocks resets to this block's white render
        // color, rather than applying the wand item's tint or enchantment pass.
        GlStateManager.color(1, 1, 1, 1);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
        NativeItemQuadRenderer.appendModel(buffer, model, stack, Minecraft.getMinecraft().getItemColors());
        Tessellator.getInstance().draw();
    }
    @Override public void renderByItem(ItemStack stack, float partialTicks) { renderByItem(stack); }

    @Override public void renderDropped(EntityItem entity, double x, double y, double z, float yaw,
            float partialTicks, boolean outlines, int outlineColor) {
        renderEntity(entity, x, y, z, partialTicks, false, false, outlines, outlineColor);
    }
    public void renderFrame(EntityItem entity, double x, double y, double z, float yaw,
            float partialTicks, NativeMachineItemRenderer.Display display) {
        renderEntity(entity, x, y, z, partialTicks, true,
                display == NativeMachineItemRenderer.Display.DECORATION_FRAME, false, 0);
    }
    private void renderEntity(EntityItem entity, double x, double y, double z, float partialTicks,
            boolean frame, boolean decoration, boolean outlines, int outlineColor) {
        ItemStack stack = entity.getItem();
        if(stack.isEmpty()) return;
        Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        ITextureObject atlas = mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        atlas.setBlurMipmap(false, false);
        GlStateManager.pushMatrix();
        try {
            float bob = decoration ? 0 : MathHelper.sin((entity.getAge() + partialTicks) / 10F + entity.hoverStart) * .1F + .1F;
            GlStateManager.translate((float)x, (float)y + bob, (float)z);
            GlStateManager.enableRescaleNormal();
            GlStateManager.rotate(((entity.getAge() + partialTicks) / 20F + entity.hoverStart) * 57.295776F, 0, 1, 0);
            if(outlines) { GlStateManager.enableColorMaterial(); GlStateManager.enableOutlineMode(outlineColor); }
            if(frame) {
                GlStateManager.scale(1.25F, 1.25F, 1.25F);
                GlStateManager.translate(0, .05F, 0);
                GlStateManager.rotate(-90, 0, 1, 0);
            }
            // Wand is not an ItemBlock: old Forge's BLOCK_3D helper used renderType=1.
            GlStateManager.scale(.5F, .5F, .5F);
            int count = decoration ? 1 : NativeMachineItemRenderer.blockCount(stack.getCount());
            Random random = new Random(187L);
            for(int i = 0; i < count; i++) {
                GlStateManager.pushMatrix();
                try {
                    if(i > 0) GlStateManager.translate(spread(random), spread(random), spread(random));
                    GlStateManager.translate(-.5F, -.5F, -.5F);
                    renderByItem(stack);
                } finally { GlStateManager.popMatrix(); }
            }
        } finally {
            if(outlines) { GlStateManager.disableOutlineMode(); GlStateManager.disableColorMaterial(); }
            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
            mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            atlas.restoreLastBlurMipmap();
        }
    }
    private static float spread(Random random) { return ((random.nextFloat() * 2F - 1F) * .2F) / .5F; }
}
