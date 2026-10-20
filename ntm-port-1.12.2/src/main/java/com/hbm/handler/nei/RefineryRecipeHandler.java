package com.hbm.handler.nei;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.gui.GUIMachineRefinery;
import com.hbm.inventory.recipes.RefineryRecipes;
import com.hbm.lib.RefStrings;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import com.hbm.handler.jei.MachineRecipeCategory;
import com.hbm.handler.jei.MachineRecipeView;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.recipe.IFocus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
/** Native JEI category for the original RefineryRecipeHandler recipe source. */
public class RefineryRecipeHandler extends MachineRecipeCategory {
    private final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/nei/gui_nei_refinery.png");
    private IDrawable background;
    @Override public String getUid() { return "refinery"; }
    @Override public String getTitle() {
		return "Refinery";
	 }
    @Override public ItemStack[] getMachines() {
		return new ItemStack[]{
				new ItemStack(ModBlocks.machine_refinery)};
	 }
    @Override public IDrawable getBackground() { return background; }
    @Override public void initialize(IGuiHelper helper) {
        super.initialize(helper);
        background = helper.createDrawable(texture, 5, 11, 166, 65);
        energy = helper.createAnimatedDrawable(helper.createDrawable(texture, 0, 86, 16, 52), 480, IDrawableAnimated.StartDirection.TOP, true);
        progress = helper.createAnimatedDrawable(helper.createDrawable(texture, 16, 86, 24, 17), 48, IDrawableAnimated.StartDirection.LEFT, false);
    }
    @Override public void registerGuiAreas(IModRegistry registry) { registry.addRecipeClickArea(GUIMachineRefinery.class, (48) + 5, (5) + 11, 31, 101, getUid()); }
    @Override public boolean isNavigationArea(int x, int y) {
        return (new java.awt.Rectangle(138 - 1 - 36 - 27, 23, 36, 18).contains(x, y));
    }
    @Override public List<MachineRecipeView> query(IFocus<ItemStack> focus) {
        List<MachineRecipeView> views = new ArrayList<>();
        if(focus == null) {
			Map<Object, Object[]> recipes = RefineryRecipes.getRefineryRecipe();
			for (Map.Entry<Object, Object[]> recipe : recipes.entrySet()) {
				views.add(createView((ItemStack)recipe.getKey(),
						(ItemStack)recipe.getValue()[0], (ItemStack)recipe.getValue()[1],
						(ItemStack)recipe.getValue()[2], (ItemStack)recipe.getValue()[3],
						(ItemStack)recipe.getValue()[4]));
			}
		 }
        else if(focus.getMode() == IFocus.Mode.OUTPUT) {
            ItemStack result = focus.getValue();

		Map<Object, Object[]> recipes = RefineryRecipes.getRefineryRecipe();
		for (Map.Entry<Object, Object[]> recipe : recipes.entrySet()) {
			if (compareFluidStacks((ItemStack)recipe.getValue()[0], result) ||
					compareFluidStacks((ItemStack)recipe.getValue()[1], result) ||
					compareFluidStacks((ItemStack)recipe.getValue()[2], result) ||
					compareFluidStacks((ItemStack)recipe.getValue()[3], result) ||
					compareFluidStacks((ItemStack)recipe.getValue()[4], result))
				views.add(createView((ItemStack)recipe.getKey(),
						(ItemStack)recipe.getValue()[0], (ItemStack)recipe.getValue()[1],
						(ItemStack)recipe.getValue()[2], (ItemStack)recipe.getValue()[3],
						(ItemStack)recipe.getValue()[4]));
		}

        } else {
            ItemStack ingredient = focus.getValue();

		Map<Object, Object[]> recipes = RefineryRecipes.getRefineryRecipe();
		for (Map.Entry<Object, Object[]> recipe : recipes.entrySet()) {
			if (compareFluidStacks(ingredient, (ItemStack)recipe.getKey()))
				views.add(createView((ItemStack)recipe.getKey(),
						(ItemStack)recipe.getValue()[0], (ItemStack)recipe.getValue()[1],
						(ItemStack)recipe.getValue()[2], (ItemStack)recipe.getValue()[3],
						(ItemStack)recipe.getValue()[4]));
		}

        }
        return views;
    }
    private MachineRecipeView createView(ItemStack input, ItemStack a, ItemStack b, ItemStack c, ItemStack d, ItemStack e) {
        ItemStack shown = input.copy(); shown.setCount(1);
        return new MachineRecipeView(this, input, new ItemStack[][]{{shown}}, new ItemStack[][]{{a}, {b}, {c}, {d}, {e}})
            .addInputSlot(0, 48, 24).addOutputSlot(0, 111, 6).addOutputSlot(1, 129, 15)
            .addOutputSlot(2, 111, 24).addOutputSlot(3, 129, 33).addOutputSlot(4, 111, 42);
    }
    @Override public void drawRecipe(MachineRecipeView view, Minecraft minecraft, int mouseX, int mouseY) {
        energy.draw(minecraft, 3, 6); progress.draw(minecraft, 78, 24);
    }
    private IDrawableAnimated energy, progress;
    private boolean compareFluidStacks(ItemStack a, ItemStack b) { return a.getItem() == b.getItem() && a.getItemDamage() == b.getItemDamage(); }
}
