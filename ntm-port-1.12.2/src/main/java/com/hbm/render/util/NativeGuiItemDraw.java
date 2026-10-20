package com.hbm.render.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import java.util.Set;
import java.util.Collections;
import java.util.IdentityHashMap;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;

/** GUI item/overlay pair using the registered native renderer, with local z ownership.
 * Legacy callers had separate RenderItem instances. Never leak their zLevel into
 * Minecraft's shared renderer, including when an item renderer throws.
 */
public final class NativeGuiItemDraw {
    private NativeGuiItemDraw() { }

    /** The old no-effect entry also bypassed custom IItemRenderer dispatch. */
    public static void withoutEffect(RenderItem renderer, ItemStack stack, int x, int y) {
        baseIntoGui(renderer, stack, x, y, false);
    }

    public static void withoutEffect(RenderItem renderer, ItemStack stack, int x, int y, float zLevel) {
        float previous = renderer.zLevel;
        renderer.zLevel = zLevel;
        try { withoutEffect(renderer, stack, x, y); }
        finally { renderer.zLevel = previous; }
    }

    public static void baseIntoGui(RenderItem renderer, ItemStack stack, int x, int y, boolean effect) {
        ItemStack display = displayStack(stack);
        if(display.isEmpty()) return;
        Minecraft mc = Minecraft.getMinecraft();
        IBakedModel model = baseModel(renderer, display);
        TextureManager textures = mc.getTextureManager();
        ResourceLocation atlas = TextureMap.LOCATION_BLOCKS_TEXTURE;
        textures.bindTexture(atlas);
        ITextureObject texture = textures.getTexture(atlas);
        texture.setBlurMipmap(false, false);
        GlStateManager.pushMatrix();
        try {
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableAlpha();
            GlStateManager.alphaFunc(GL11.GL_GREATER, .1F);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.translate(x, y, 100F + renderer.zLevel);
            GlStateManager.translate(8, 8, 0);
            GlStateManager.scale(16, -16, 16);
            if(model.isGui3d()) GlStateManager.enableLighting();
            else GlStateManager.disableLighting();
            model = ForgeHooksClient.handleCameraTransforms(model,
                    TransformType.GUI, false);
            if(effect || model.isBuiltInRenderer()) {
                // Native builtin base rendering does not add generic enchant glint.
                // Use the installed owner, including its typed context dispatcher.
                renderer.renderItem(display, model);
            } else {
                GlStateManager.translate(-.5F, -.5F, -.5F);
                BufferBuilder buffer = Tessellator.getInstance().getBuffer();
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
                NativeItemQuadRenderer.appendModel(buffer, model, display, mc.getItemColors());
                Tessellator.getInstance().draw();
            }
        } finally {
            GlStateManager.disableAlpha();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableLighting();
            GlStateManager.popMatrix();
            textures.bindTexture(atlas);
            texture.restoreLastBlurMipmap();
        }
    }

    public static IBakedModel baseModel(RenderItem renderer, ItemStack stack) {
        IBakedModel model = renderer.getItemModelMesher().getItemModel(stack);
        Set<IBakedModel> seen =
                Collections.newSetFromMap(new IdentityHashMap<>());
        while(model instanceof NativeItemBaseModel && seen.add(model)) {
            IBakedModel base = ((NativeItemBaseModel) model).getBaseModel();
            // A missing registry entry had native missing-model geometry before the
            // replacement too. Do not reinterpret its particle sprite as an icon.
            model = base == null ? renderer.getItemModelMesher().getModelManager().getMissingModel() : base;
        }
        return model.getOverrides().handleItemState(model, stack, null, null);
    }

    /** Recover intentional zero-count preview identity on a copy, never a slot. */
    public static ItemStack displayStack(ItemStack stack) {
        if(stack == null || stack == ItemStack.EMPTY) return ItemStack.EMPTY;
        if(!stack.isEmpty()) return stack;
        ItemStack display = stack.copy();
        display.setCount(1);
        return display;
    }

    public static void withOverlay(RenderItem renderer, FontRenderer font,
            ItemStack stack, int x, int y, float zLevel, String overlay) {
        if(stack == null || stack == ItemStack.EMPTY) return;
        ItemStack display = stack;
        if(stack.isEmpty()) {
            // Tutorial/manual entries can deliberately contain a zero-count item.
            // Native copy retains its raw item/NBT; never resurrect the source slot.
            display = stack.copy();
            display.setCount(1);
            if(display.isEmpty()) return;
            if(overlay == null) overlay = Integer.toString(stack.getCount());
        }
        float previous = renderer.zLevel;
        renderer.zLevel = zLevel;
        try {
            renderer.renderItemAndEffectIntoGUI(display, x, y);
            renderer.renderItemOverlayIntoGUI(font, display, x, y, overlay);
        } finally {
            renderer.zLevel = previous;
        }
    }
}
