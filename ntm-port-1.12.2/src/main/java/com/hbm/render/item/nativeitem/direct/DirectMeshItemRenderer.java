package com.hbm.render.item.nativeitem.direct;

import javax.vecmath.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

/** Native renderer for context-independent mesh draws with explicit, pure local
 * perspective matrices. No last context, holder, stack, or arbitrary GL capture.
 */
public abstract class DirectMeshItemRenderer extends TileEntityItemStackRenderer {
    public abstract Matrix4f localTransform(TransformType type, long timeMillis);
    protected abstract void draw(ItemStack stack);
    public boolean usesGuiStandardLighting() { return false; }

    @Override public final void renderByItem(ItemStack stack) {
        int texture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_LIGHTING_BIT | GL11.GL_CURRENT_BIT | GL11.GL_COLOR_BUFFER_BIT);
        try {
            int tint = Minecraft.getMinecraft().getItemColors().colorMultiplier(stack, 0);
            GL11.glColor4f((tint >> 16 & 255) / 255F, (tint >> 8 & 255) / 255F, (tint & 255) / 255F, 1F);
            draw(stack);
        } finally {
            GlStateManager.bindTexture(texture);
            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }
    }
    @Override public final void renderByItem(ItemStack stack, float partialTicks) { renderByItem(stack); }
}
