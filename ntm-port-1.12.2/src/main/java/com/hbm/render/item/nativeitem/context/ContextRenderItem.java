package com.hbm.render.item.nativeitem.context;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/** Native virtual draw boundary. All unrelated items retain the actual vanilla
 * implementation, original mesher, colors, textures and glint behavior. */
public final class ContextRenderItem extends RenderItem {
    private final TextureManager textures;
    public ContextRenderItem(RenderItem original) {
        super((TextureManager)NativeItemFields.get(NativeItemFields.unique(RenderItem.class, TextureManager.class), original),
                original.getItemModelMesher().getModelManager(),
                (ItemColors)NativeItemFields.get(NativeItemFields.unique(RenderItem.class, ItemColors.class), original));
        if(original.getClass() != RenderItem.class) throw new IllegalArgumentException("Exact native RenderItem required");
        // Preserve original registration maps and reload-listener identity, rather
        // than copying a stale cache or abandoning the already registered listener.
        for(Field field : RenderItem.class.getDeclaredFields()) {
            if(Modifier.isStatic(field.getModifiers())) continue;
            field.setAccessible(true);
            NativeItemFields.set(field, this, NativeItemFields.get(field, original));
        }
        textures = (TextureManager)NativeItemFields.get(NativeItemFields.unique(RenderItem.class, TextureManager.class), original);
    }
    @Override public void renderItem(ItemStack stack, IBakedModel model) {
        if(!(model instanceof ContextItemModel)) { super.renderItem(stack, model); return; }
        if(stack.isEmpty()) return;
        ContextItemModel selected = (ContextItemModel)model;
        boolean rescale = GL11.glIsEnabled(GL12.GL_RESCALE_NORMAL);
        float[] color = NativeItemFailureScope.read(GL11.GL_CURRENT_COLOR, new float[4]);
        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(-.5F, -.5F, -.5F);
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.enableRescaleNormal();
            selected.renderer.render(stack, selected.context);
        } catch(RuntimeException | Error failure) {
            if(rescale) GlStateManager.enableRescaleNormal(); else GlStateManager.disableRescaleNormal();
            GlStateManager.color(color[0], color[1], color[2], color[3]);
            throw failure;
        } finally { GlStateManager.popMatrix(); }
    }
    @Override protected void renderItemModel(ItemStack stack, IBakedModel model, TransformType type, boolean left) {
        if(!(model instanceof ContextItemModel)) { super.renderItemModel(stack, model, type, left); return; }
        NativeItemFailureScope scope = new NativeItemFailureScope(textures);
        try { super.renderItemModel(stack, model, type, left); }
        catch(RuntimeException | Error failure) { scope.restore(); throw failure; }
        finally { scope.restoreTextureFilter(); }
    }
    @Override protected void renderItemModelIntoGUI(ItemStack stack, int x, int y, IBakedModel model) {
        if(!(model instanceof ContextItemModel)) { super.renderItemModelIntoGUI(stack, x, y, model); return; }
        NativeItemFailureScope scope = new NativeItemFailureScope(textures);
        try { super.renderItemModelIntoGUI(stack, x, y, model); }
        catch(RuntimeException | Error failure) { scope.restore(); throw failure; }
        finally { scope.restoreTextureFilter(); }
    }
    @Override public void renderItemAndEffectIntoGUI(EntityLivingBase holder, ItemStack stack, int x, int y) {
        if(stack.isEmpty() || !(getItemModelMesher().getItemModel(stack) instanceof ContextItemModel)) {
            super.renderItemAndEffectIntoGUI(holder, stack, x, y);
            return;
        }
        float previous = zLevel;
        try { super.renderItemAndEffectIntoGUI(holder, stack, x, y); }
        finally { zLevel = previous; }
    }

}
