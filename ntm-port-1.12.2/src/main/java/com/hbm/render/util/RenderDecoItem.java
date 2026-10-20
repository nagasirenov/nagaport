package com.hbm.render.util;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** World-space GUI display. Retains the old flat glint and cube/icon placement,
 * while complete native custom models use the installed RenderItem owner. */
public final class RenderDecoItem {
    private static final ResourceLocation GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");

    public void renderGui(ItemStack source, int x, int y, boolean custom, boolean effect) {
        RenderItem renderer = Minecraft.getMinecraft().getRenderItem();
        float previous = renderer.zLevel;
        renderer.zLevel = 0;
        try { renderGuiAtLocalDepth(source, x, y, custom, effect); }
        finally { renderer.zLevel = previous; }
    }
    private void renderGuiAtLocalDepth(ItemStack source, int x, int y, boolean custom, boolean effect) {
        ItemStack stack = NativeGuiItemDraw.displayStack(source);
        if(stack.isEmpty()) return;
        Minecraft mc = Minecraft.getMinecraft();
        RenderItem renderer = mc.getRenderItem();
        IBakedModel selected = renderer.getItemModelWithOverrides(stack, null, null);
        if(custom && selected.isBuiltInRenderer()) {
            renderer.renderItemAndEffectIntoGUI(stack, x, y);
            return;
        }
        IBakedModel model = NativeGuiItemDraw.baseModel(renderer, stack);
        List<BakedQuad> layers = NativeMachineItemRenderer.iconLayers(model);
        if(stack.getItem() instanceof ItemBlock && model.isGui3d() && !model.isBuiltInRenderer()) {
            drawCube(mc, stack, model, x, y);
        } else if(layers != null) {
            for(BakedQuad layer : layers) {
                mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                int color = layer.hasTintIndex() ? mc.getItemColors().colorMultiplier(stack, layer.getTintIndex()) : 0xFFFFFF;
                GlStateManager.color((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F, 1);
                GlStateManager.enablePolygonOffset();
                GlStateManager.doPolygonOffset(-1, -1);
                try {
                    GlStateManager.disableLighting();
                    GlStateManager.enableBlend();
                    GlStateManager.enableAlpha();
                    icon(layer.getSprite(), x, y);
                    GlStateManager.disableAlpha();
                    GlStateManager.disableBlend();
                    GlStateManager.enableLighting();
                    if(effect && stack.hasEffect()) glint(mc, x, y);
                } finally { GlStateManager.disablePolygonOffset(); }
            }
        } else {
            // Native-only non-generated base geometry remains complete. It cannot
            // safely be reconstructed from a particle sprite or legacy render ID.
            NativeGuiItemDraw.baseIntoGui(renderer, stack, x, y, effect);
        }
    }
    private static void icon(TextureAtlasSprite icon, int x, int y) {
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        b.pos(x, y + 16, 0).tex(icon.getMinU(), icon.getMaxV()).endVertex();
        b.pos(x + 16, y + 16, 0).tex(icon.getMaxU(), icon.getMaxV()).endVertex();
        b.pos(x + 16, y, 0).tex(icon.getMaxU(), icon.getMinV()).endVertex();
        b.pos(x, y, 0).tex(icon.getMinU(), icon.getMinV()).endVertex();
        Tessellator.getInstance().draw();
    }
    private static void drawCube(Minecraft mc, ItemStack stack, IBakedModel model, int x, int y) {
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        Block block = ((ItemBlock) stack.getItem()).getBlock();
        boolean translucent = block.getRenderLayer() == BlockRenderLayer.TRANSLUCENT;
        GlStateManager.alphaFunc(GL11.GL_GREATER, translucent ? .1F : .5F);
        if(translucent) { GlStateManager.enableBlend(); GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0); }
        else GlStateManager.disableBlend();
        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(x - 2, y + 3, -3);
            GlStateManager.scale(10, 10, 10);
            GlStateManager.translate(1, .5F, 1);
            GlStateManager.scale(1, 1, -1);
            GlStateManager.rotate(210, 1, 0, 0);
            GlStateManager.rotate(45, 0, 1, 0);
            GlStateManager.rotate(-90, 0, 1, 0);
            GlStateManager.enablePolygonOffset();
            GlStateManager.doPolygonOffset(-1, -1);
            GlStateManager.translate(-.5F, -.5F, -.5F); // native cubes are [0,1], legacy inventory cubes centered
            NativeMachineItemRenderer.drawModel(mc, model, stack);
        } finally {
            GlStateManager.disablePolygonOffset();
            if(!translucent) GlStateManager.alphaFunc(GL11.GL_GREATER, .1F);
            GlStateManager.popMatrix();
        }
    }
    private static void glint(Minecraft mc, int x, int y) {
        GlStateManager.depthFunc(GL11.GL_EQUAL);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        mc.getTextureManager().bindTexture(GLINT);
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.color(.5F, .25F, .8F, 1);
        try {
            for(int i = 0; i < 2; i++) {
                GlStateManager.tryBlendFuncSeparate(772, 1, 0, 0);
                float u = (Minecraft.getSystemTime() % (3000 + i * 1873)) / (3000F + i * 1873) * 256F;
                float hScale = i < 1 ? 4F : -1F;
                BufferBuilder b = Tessellator.getInstance().getBuffer();
                b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                b.pos(x, y + 16, 0).tex((u + 16 * hScale) * .00390625F, 16 * .00390625F).endVertex();
                b.pos(x + 16, y + 16, 0).tex((u + 16 + 16 * hScale) * .00390625F, 16 * .00390625F).endVertex();
                b.pos(x + 16, y, 0).tex((u + 16) * .00390625F, 0).endVertex();
                b.pos(x, y, 0).tex(u * .00390625F, 0).endVertex();
                Tessellator.getInstance().draw();
            }
        } finally {
            GlStateManager.depthMask(true);
            GlStateManager.disableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.enableLighting();
            GlStateManager.depthFunc(GL11.GL_LEQUAL);
        }
    }
}
