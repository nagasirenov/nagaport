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
            java.nio.FloatBuffer values = org.lwjgl.BufferUtils.createFloatBuffer(16);
            values.put(new float[]{matrix.m00,matrix.m10,matrix.m20,matrix.m30,matrix.m01,matrix.m11,matrix.m21,matrix.m31,
                    matrix.m02,matrix.m12,matrix.m22,matrix.m32,matrix.m03,matrix.m13,matrix.m23,matrix.m33}).flip();
            GL11.glMultMatrix(values);
            render(stack, context);
        } finally { GL11.glPopMatrix(); }
    }
    @Override public final void renderByItem(ItemStack stack, float partialTicks) { renderByItem(stack); }
}
