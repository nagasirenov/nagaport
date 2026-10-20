package com.hbm.render.util;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;

public class RenderItemStack {

    /** Resolve the installed native owner, never the pre-load-complete instance. */
    public static RenderItem renderer() { return Minecraft.getMinecraft().getRenderItem(); }

    public static void renderItemStack(int x, int y, float partialTicks, ItemStack stack) {
        render(x, y, partialTicks, stack, true);
    }
    public static void renderItemStackNoEffect(int x, int y, float partialTicks, ItemStack stack) {
        render(x, y, partialTicks, stack, false);
    }
    private static void render(int x, int y, float partialTicks, ItemStack stack, boolean effect) {
        if(stack == null || stack == ItemStack.EMPTY) return;
        float animation = stack.getAnimationsToGo() - partialTicks;
        if(animation > 0) {
            GL11.glPushMatrix();
            float scale = 1F + animation / 5F;
            GL11.glTranslatef(x + 8, y + 12, 0);
            GL11.glScalef(1F / scale, (scale + 1F) / 2F, 1F);
            GL11.glTranslatef(-(x + 8), -(y + 12), 0);
        }
        try { draw(stack, x, y, effect); }
        finally { if(animation > 0) GL11.glPopMatrix(); }
        // Deliberate original second, unanimated draw. Do not "optimize" away.
        draw(stack, x, y, effect);
    }
    private static void draw(ItemStack stack, int x, int y, boolean effect) {
        RenderItem renderer = renderer();
        float previous = renderer.zLevel;
        renderer.zLevel = 0; // the original dedicated renderer's local GUI depth
        try {
            if(effect) renderer.renderItemAndEffectIntoGUI(NativeGuiItemDraw.displayStack(stack), x, y);
            else NativeGuiItemDraw.withoutEffect(renderer, stack, x, y);
        } finally { renderer.zLevel = previous; }
    }
}
