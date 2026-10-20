package com.hbm.render.item.nativeitem.context;

import java.nio.FloatBuffer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

/** Lexical custom-hand projection ownership. No saved stack/holder or pending draw. */
public final class NativeProjectionScope implements AutoCloseable {
    private final int mode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
    private final int modelDepth = GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH);
    private final int projectionDepth = GL11.glGetInteger(GL11.GL_PROJECTION_STACK_DEPTH);
    private final FloatBuffer model = BufferUtils.createFloatBuffer(16), projection = BufferUtils.createFloatBuffer(16);
    private final int active = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
    private final int[] bindings = new int[2];
    private final float lightX = OpenGlHelper.lastBrightnessX, lightY = OpenGlHelper.lastBrightnessY;
    private boolean closed;
    public NativeProjectionScope() {
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, model);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projection);
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit); bindings[0] = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit); bindings[1] = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GlStateManager.setActiveTexture(active);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
    }
    @Override public void close() {
        if(closed) return;
        closed = true;
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        while(GL11.glGetInteger(GL11.GL_PROJECTION_STACK_DEPTH) > projectionDepth) GlStateManager.popMatrix();
        GL11.glLoadMatrix(projection);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        while(GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH) > modelDepth) GlStateManager.popMatrix();
        GL11.glLoadMatrix(model);
        GL11.glPopAttrib();
        NativeItemFailureScope.synchronizeNativeCaches();
        for(int i = 0; i < 8; i++) {
            boolean enabled = GL11.glIsEnabled(GL11.GL_LIGHT0 + i);
            if(enabled) { GlStateManager.disableLight(i); GlStateManager.enableLight(i); }
            else { GlStateManager.enableLight(i); GlStateManager.disableLight(i); }
        }
        boolean color = GL11.glIsEnabled(GL11.GL_COLOR_MATERIAL);
        if(color) { GlStateManager.disableColorMaterial(); GlStateManager.enableColorMaterial(); }
        else { GlStateManager.enableColorMaterial(); GlStateManager.disableColorMaterial(); }
        int face = GL11.glGetInteger(GL11.GL_COLOR_MATERIAL_FACE), parameter = GL11.glGetInteger(GL11.GL_COLOR_MATERIAL_PARAMETER);
        GlStateManager.colorMaterial(face == GL11.GL_FRONT ? GL11.GL_BACK : GL11.GL_FRONT, GL11.GL_AMBIENT);
        GlStateManager.colorMaterial(face, parameter);
        // PopAttrib restored real texture state; force each native cached binding
        // and enabled flag to agree with it before another native hand renders.
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        for(int i = 0; i < 2; i++) {
            GlStateManager.setActiveTexture(i == 0 ? OpenGlHelper.defaultTexUnit : OpenGlHelper.lightmapTexUnit);
            boolean texture = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
            if(texture) { GlStateManager.disableTexture2D(); GlStateManager.enableTexture2D(); }
            else { GlStateManager.enableTexture2D(); GlStateManager.disableTexture2D(); }
            GlStateManager.bindTexture(0); GlStateManager.bindTexture(bindings[i]);
        }
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightX, lightY);
        GlStateManager.setActiveTexture(active);
        GlStateManager.matrixMode(mode);
    }
}
