package com.hbm.render.util;

import java.util.List;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.LightUtil;

/** Native baked-item geometry without the separate enchantment pass.
 * The caller owns the ITEM-format buffer and model/camera selection. This never
 * changes a stack, strips enchantments, invokes a TEISR, or changes GL state.
 */
public final class NativeItemQuadRenderer {
    private NativeItemQuadRenderer() { }

    public static void appendModel(BufferBuilder buffer, IBakedModel model,
            ItemStack stack, ItemColors colors) {
        for(EnumFacing face : EnumFacing.values())
            appendQuads(buffer, model.getQuads(null, face, 0L), stack, colors);
        appendQuads(buffer, model.getQuads(null, null, 0L), stack, colors);
    }

    public static void appendQuads(BufferBuilder buffer, List<BakedQuad> quads,
            ItemStack stack, ItemColors colors) {
        for(BakedQuad quad : quads) {
            int color = -1;
            if(!stack.isEmpty() && quad.hasTintIndex()) {
                color = colors.colorMultiplier(stack, quad.getTintIndex());
                if(EntityRenderer.anaglyphEnable) color = TextureUtil.anaglyphColor(color);
                color |= 0xFF000000;
            }
            LightUtil.renderQuadColor(buffer, quad, color);
        }
    }
}
