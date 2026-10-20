package com.hbm.handler.nei;

import java.util.HashMap;
import com.hbm.dim.CelestialBody;
import com.hbm.handler.jei.MachineRecipeView;
import com.hbm.util.Clock;
import com.hbm.util.i18n.I18nUtil;
import mezz.jei.api.recipe.IFocus;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

/** Celestial recipe context belongs to the wrapper, never to a mutable result-list index. */
public abstract class NEICelestialHandler extends NEIUniversalHandler {
    public NEICelestialHandler(String display, ItemStack[] machine, HashMap recipes) { super(display, machine, recipes); }
    public NEICelestialHandler(String display, HashMap recipes, HashMap machines) { super(display, recipes, machines); }
    public NEICelestialHandler(String display, ItemStack machine, HashMap recipes) { super(display, machine, recipes); }
    public NEICelestialHandler(String display, Item machine, HashMap recipes) { super(display, machine, recipes); }
    public NEICelestialHandler(String display, Block machine, HashMap recipes) { super(display, machine, recipes); }

    @Override protected ItemStack[][] recipeInputs(Object key) { return new ItemStack[0][]; }
    @Override protected boolean acceptsFocus(IFocus<ItemStack> focus) { return focus == null || focus.getMode() == IFocus.Mode.OUTPUT; }

    @Override public void drawRecipe(MachineRecipeView view, Minecraft minecraft, int mouseX, int mouseY) {
        super.drawRecipe(view, minecraft, mouseX, mouseY);
        CelestialBody body = (CelestialBody) view.source;
        minecraft.getTextureManager().bindTexture(body.texture);
        double uv = (double)(Clock.get_ms() % 4000) / 4000;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(32, 44, 0).tex(uv, 1).endVertex();
        buffer.pos(64, 44, 0).tex(1 + uv, 1).endVertex();
        buffer.pos(64, 12, 0).tex(1 + uv, 0).endVertex();
        buffer.pos(32, 12, 0).tex(uv, 0).endVertex();
        tessellator.draw();
        String name = I18nUtil.resolveKey("body." + body.name);
        minecraft.fontRenderer.drawString(name, 16 + (64 - minecraft.fontRenderer.getStringWidth(name)) / 2, 50, 0x000000);
    }
}
