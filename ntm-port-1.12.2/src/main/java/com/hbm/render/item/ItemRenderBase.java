package com.hbm.render.item;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;

/** Native TEISR for the mod's ordered mesh callbacks. Perspective callbacks are
 * owned by CallbackItemModel, not a removed Forge renderer context or global stack.
 */
public abstract class ItemRenderBase extends TileEntityItemStackRenderer {
    @Override public final void renderByItem(ItemStack item) {
        int texture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_LIGHTING_BIT | GL11.GL_CURRENT_BIT);
        try {
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            int tint = Minecraft.getMinecraft().getItemColors().colorMultiplier(item, 0);
            GL11.glColor4f((tint >> 16 & 255) / 255F, (tint >> 8 & 255) / 255F, (tint & 255) / 255F, 1F);
            renderCommon();
            renderCommonWithStack(item);
        } finally {
            GlStateManager.bindTexture(texture);
            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }
    }
    @Override public final void renderByItem(ItemStack item, float partialTicks) { renderByItem(item); }
    public void renderNonInv() { }
    public void renderInventory() { }
    public void renderCommon() { }
    public void renderCommonWithStack(ItemStack item) { }
}
