package com.hbm.render.item.nativeitem.context;

import java.lang.reflect.Field;
import java.nio.FloatBuffer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;

/** Vanilla outer item methods have no finally cleanup. Restore their changed
 * state only on failure; successful native state transitions stay untouched. */
final class NativeItemFailureScope {
    private final int depth = GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH);
    private final int mode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
    private final int binding = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
    private final int alphaFunc = GL11.glGetInteger(GL11.GL_ALPHA_TEST_FUNC);
    private final float alphaRef = GL11.glGetFloat(GL11.GL_ALPHA_TEST_REF);
    private final int cullFace = GL11.glGetInteger(GL11.GL_CULL_FACE_MODE);
    private final boolean alpha = GL11.glIsEnabled(GL11.GL_ALPHA_TEST), blend = GL11.glIsEnabled(GL11.GL_BLEND);
    private final boolean lighting = GL11.glIsEnabled(GL11.GL_LIGHTING), rescale = GL11.glIsEnabled(GL12.GL_RESCALE_NORMAL);
    private final int srcRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB), dstRgb = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
    private final int srcAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA), dstAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
    private final FloatBuffer matrix = BufferUtils.createFloatBuffer(16), color = BufferUtils.createFloatBuffer(16);
    private final AbstractTexture atlas;
    private static final Field[] FILTER_FIELDS = {ReflectionHelper.findField(AbstractTexture.class, "blur", "field_174940_b"),
            ReflectionHelper.findField(AbstractTexture.class, "mipmap", "field_174941_c"),
            ReflectionHelper.findField(AbstractTexture.class, "blurLast", "field_174938_d"),
            ReflectionHelper.findField(AbstractTexture.class, "mipmapLast", "field_174939_e")};
    private final boolean[] filters;
    NativeItemFailureScope(TextureManager textures) {
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, matrix);
        GL11.glGetFloat(GL11.GL_CURRENT_COLOR, color);
        ITextureObject texture = textures.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        atlas = texture instanceof AbstractTexture ? (AbstractTexture)texture : null;
        filters = new boolean[4];
        if(atlas != null) for(int i = 0; i < 4; i++) filters[i] = (Boolean)NativeItemFields.get(FILTER_FIELDS[i], atlas);
    }
    void restore() {
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        while(GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH) > depth) GlStateManager.popMatrix();
        GL11.glLoadMatrix(matrix);
        GlStateManager.matrixMode(mode);
        if(alpha) GlStateManager.enableAlpha(); else GlStateManager.disableAlpha();
        if(blend) GlStateManager.enableBlend(); else GlStateManager.disableBlend();
        if(lighting) GlStateManager.enableLighting(); else GlStateManager.disableLighting();
        if(rescale) GlStateManager.enableRescaleNormal(); else GlStateManager.disableRescaleNormal();
        GlStateManager.alphaFunc(alphaFunc, alphaRef);
        GlStateManager.tryBlendFuncSeparate(srcRgb, dstRgb, srcAlpha, dstAlpha);
        GlStateManager.cullFace(cullFace == GL11.GL_FRONT ? GlStateManager.CullFace.FRONT
                : cullFace == GL11.GL_FRONT_AND_BACK ? GlStateManager.CullFace.FRONT_AND_BACK : GlStateManager.CullFace.BACK);
        GlStateManager.color(color.get(0), color.get(1), color.get(2), color.get(3));
        restoreTextureFilter();
        GlStateManager.bindTexture(binding);
    }
    void restoreTextureFilter() {
        int previous = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        if(atlas != null) {
            GlStateManager.bindTexture(atlas.getGlTextureId());
            atlas.setBlurMipmapDirect(filters[0], filters[1]);
            for(int i = 2; i < 4; i++) NativeItemFields.set(FILTER_FIELDS[i], atlas, filters[i]);
        }
        GlStateManager.bindTexture(previous);
    }
    /** Raw legacy mesh GL and a nested native draw may disagree with Minecraft's
     * state cache after glPopAttrib. Force the saved real values through both
     * sides; otherwise e.g. native disableRescaleNormal becomes a cached no-op. */
    static void synchronizeNativeCaches() {
        // Enabling/changing color material or reissuing the current color also
        // writes material vectors. Preserve those GPU values while repairing
        // the native cache; GL_LIGHTING_BIT leaves the current-color repair intact.
        GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
        try {
            boolean a = GL11.glIsEnabled(GL11.GL_ALPHA_TEST), b = GL11.glIsEnabled(GL11.GL_BLEND);
            boolean l = GL11.glIsEnabled(GL11.GL_LIGHTING), c = GL11.glIsEnabled(GL11.GL_CULL_FACE);
            boolean r = GL11.glIsEnabled(GL12.GL_RESCALE_NORMAL), d = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
            if(a) { GlStateManager.disableAlpha(); GlStateManager.enableAlpha(); } else { GlStateManager.enableAlpha(); GlStateManager.disableAlpha(); }
            if(b) { GlStateManager.disableBlend(); GlStateManager.enableBlend(); } else { GlStateManager.enableBlend(); GlStateManager.disableBlend(); }
            if(l) { GlStateManager.disableLighting(); GlStateManager.enableLighting(); } else { GlStateManager.enableLighting(); GlStateManager.disableLighting(); }
            // RenderHelper changes these cached enables and color-material selection.
            // glPopAttrib restores their GPU values, not GlStateManager's Java cache.
            for(int light = 0; light < 8; light++) {
                boolean enabled = GL11.glIsEnabled(GL11.GL_LIGHT0 + light);
                if(enabled) { GlStateManager.disableLight(light); GlStateManager.enableLight(light); }
                else { GlStateManager.enableLight(light); GlStateManager.disableLight(light); }
            }
            boolean material = GL11.glIsEnabled(GL11.GL_COLOR_MATERIAL);
            if(material) { GlStateManager.disableColorMaterial(); GlStateManager.enableColorMaterial(); }
            else { GlStateManager.enableColorMaterial(); GlStateManager.disableColorMaterial(); }
            int materialFace = GL11.glGetInteger(GL11.GL_COLOR_MATERIAL_FACE);
            int materialMode = GL11.glGetInteger(GL11.GL_COLOR_MATERIAL_PARAMETER);
            GlStateManager.colorMaterial(materialFace == GL11.GL_FRONT ? GL11.GL_BACK : GL11.GL_FRONT,
                    materialMode == GL11.GL_AMBIENT ? GL11.GL_DIFFUSE : GL11.GL_AMBIENT);
            GlStateManager.colorMaterial(materialFace, materialMode);
            if(c) { GlStateManager.disableCull(); GlStateManager.enableCull(); } else { GlStateManager.enableCull(); GlStateManager.disableCull(); }
            if(r) { GlStateManager.disableRescaleNormal(); GlStateManager.enableRescaleNormal(); } else { GlStateManager.enableRescaleNormal(); GlStateManager.disableRescaleNormal(); }
            if(d) { GlStateManager.disableDepth(); GlStateManager.enableDepth(); } else { GlStateManager.enableDepth(); GlStateManager.disableDepth(); }
            int func = GL11.glGetInteger(GL11.GL_ALPHA_TEST_FUNC); float ref = GL11.glGetFloat(GL11.GL_ALPHA_TEST_REF);
            GlStateManager.alphaFunc(func == GL11.GL_ALWAYS ? GL11.GL_NEVER : GL11.GL_ALWAYS, 0); GlStateManager.alphaFunc(func, ref);
            int sr = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB), dr = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
            int sa = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA), da = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
            GlStateManager.tryBlendFuncSeparate(sr == GL11.GL_ONE ? GL11.GL_ZERO : GL11.GL_ONE, 0, 1, 0);
            GlStateManager.tryBlendFuncSeparate(sr, dr, sa, da);
            int face = GL11.glGetInteger(GL11.GL_CULL_FACE_MODE);
            GlStateManager.cullFace(face == GL11.GL_FRONT ? GlStateManager.CullFace.BACK : GlStateManager.CullFace.FRONT);
            GlStateManager.cullFace(face == GL11.GL_FRONT ? GlStateManager.CullFace.FRONT : face == GL11.GL_FRONT_AND_BACK ? GlStateManager.CullFace.FRONT_AND_BACK : GlStateManager.CullFace.BACK);
            boolean mask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK); int depth = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
            GlStateManager.depthMask(!mask); GlStateManager.depthMask(mask);
            GlStateManager.depthFunc(depth == GL11.GL_ALWAYS ? GL11.GL_NEVER : GL11.GL_ALWAYS); GlStateManager.depthFunc(depth);
            int shade = GL11.glGetInteger(GL11.GL_SHADE_MODEL);
            GlStateManager.shadeModel(shade == GL11.GL_FLAT ? GL11.GL_SMOOTH : GL11.GL_FLAT); GlStateManager.shadeModel(shade);
            FloatBuffer color = BufferUtils.createFloatBuffer(16); GL11.glGetFloat(GL11.GL_CURRENT_COLOR, color);
            GlStateManager.resetColor(); GlStateManager.color(color.get(0), color.get(1), color.get(2), color.get(3));
        } finally {
            GL11.glPopAttrib();
        }
    }

}
