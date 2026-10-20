package com.hbm.render.item.nativeitem.staticfamily;

import java.util.Objects;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;

/** Complete ordered native mesh/material draw, never a tile or old renderer surrogate. */
public final class OrderedStaticMeshItemRenderer extends TileEntityItemStackRenderer {
    private final Runnable draw;
    public OrderedStaticMeshItemRenderer(Runnable draw) { this.draw = Objects.requireNonNull(draw, "draw"); }
    @Override public void renderByItem(ItemStack stack) {
        int texture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GL11.glPushMatrix();
        // Common draws use raw GL. Restore the same raw states; CURRENT includes radgen's color.
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_LIGHTING_BIT | GL11.GL_CURRENT_BIT);
        try {
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            // No universal shade override: original flat/smooth choices belong to each draw.
            draw.run();
        } finally {
            GlStateManager.bindTexture(texture);
            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }
    }
    @Override public void renderByItem(ItemStack stack, float partialTicks) { renderByItem(stack); }
}
