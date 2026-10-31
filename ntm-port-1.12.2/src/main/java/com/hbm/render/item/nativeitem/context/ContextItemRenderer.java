package com.hbm.render.item.nativeitem.context;

import javax.vecmath.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

/** Native typed mesh draw. Context is an argument for the entire lexical call,
 * not a pending model callback, global last holder, or legacy Object[] protocol. */
public abstract class ContextItemRenderer extends TileEntityItemStackRenderer {
    public abstract Matrix4f localTransform(ItemStack stack, NativeItemContext context);
    protected abstract void draw(ItemStack stack, NativeItemContext context);

    public final void render(ItemStack stack, NativeItemContext context) {
        int texture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_LIGHTING_BIT | GL11.GL_CURRENT_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_POLYGON_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        try {
            int tint = Minecraft.getMinecraft().getItemColors().colorMultiplier(stack, 0);
            GL11.glColor4f((tint >> 16 & 255) / 255F, (tint >> 8 & 255) / 255F, (tint & 255) / 255F, 1);
            draw(stack, context);
        } finally {
            GlStateManager.bindTexture(texture);
            GL11.glPopAttrib();
            NativeItemFailureScope.synchronizeNativeCaches();
            GL11.glPopMatrix();
        }
    }
    /** Direct third-party TEISR calls have no supplied perspective/holder. This is
     * explicitly NONE, never a stale context borrowed from an earlier draw. */
    @Override public final void renderByItem(ItemStack stack) {
        NativeItemContext context = new NativeItemContext(TransformType.NONE, null);
        GL11.glPushMatrix();
        try {
            javax.vecmath.Matrix4f matrix = localTransform(stack, context);
            multMatrix(matrix);
            render(stack, context);
        } finally { GL11.glPopMatrix(); }
    }
    @Override public final void renderByItem(ItemStack stack, float partialTicks) { renderByItem(stack); }
    // Render thread only; GL consumes it before any nested draw can reuse it.
    private static final java.nio.FloatBuffer MATRIX = org.lwjgl.BufferUtils.createFloatBuffer(16);
    protected static void multMatrix(javax.vecmath.Matrix4f m) {
        MATRIX.clear();
        MATRIX.put(new float[]{m.m00,m.m10,m.m20,m.m30,m.m01,m.m11,m.m21,m.m31,
                m.m02,m.m12,m.m22,m.m32,m.m03,m.m13,m.m23,m.m33}).flip();
        GL11.glMultMatrix(MATRIX);
    }
}
