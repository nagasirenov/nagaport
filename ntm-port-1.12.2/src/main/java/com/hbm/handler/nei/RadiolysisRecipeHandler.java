package com.hbm.handler.nei;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.gui.GUIRadiolysis;
import com.hbm.inventory.recipes.RadiolysisRecipes;
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
/** Native JEI category for the original RadiolysisRecipeHandler recipe source. */
public class RadiolysisRecipeHandler extends MachineRecipeCategory {
    private final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/nei/gui_nei_radiolysis.png");
    private IDrawable background;
    @Override public String getUid() { return "ntmRadiolysis"; }
    @Override public String getTitle() {
		return "Radiolysis";
	 }
    @Override public ItemStack[] getMachines() {
		return new ItemStack[]{
				new ItemStack(ModBlocks.machine_radiolysis)};
	 }
    @Override public IDrawable getBackground() { return background; }
    @Override public void initialize(IGuiHelper helper) {
        super.initialize(helper);
        background = helper.createDrawable(texture, 5, 11, 166, 65);
        progress = helper.createAnimatedDrawable(helper.createDrawable(texture, 5, 87, 64, 28), 60, IDrawableAnimated.StartDirection.LEFT, false);
    }
    @Override public void registerGuiAreas(IModRegistry registry) { registry.addRecipeClickArea(GUIRadiolysis.class, (66) + 5, (25) + 11, 25, 14, getUid()); }
    @Override public boolean isNavigationArea(int x, int y) {
        return (new java.awt.Rectangle(52, 19, 64, 27).contains(x, y));
    }
    @Override public List<MachineRecipeView> query(IFocus<ItemStack> focus) {
        List<MachineRecipeView> views = new ArrayList<>();
        if(focus == null) {
			HashMap<Object, Object[]> recipes = (HashMap<Object, Object[]>) RadiolysisRecipes.getRecipesForNEI();

			for(Entry<Object, Object[]> recipe : recipes.entrySet()) {
				views.add(createView((ItemStack)recipe.getKey(), (ItemStack)recipe.getValue()[0], (ItemStack)recipe.getValue()[1]));
			}
		 }
        else if(focus.getMode() == IFocus.Mode.OUTPUT) {
            ItemStack result = focus.getValue();

		HashMap<Object, Object[]> recipes = (HashMap<Object, Object[]>) RadiolysisRecipes.getRecipesForNEI();

		for(Entry<Object, Object[]> recipe : recipes.entrySet()) {
			if(compareFluidStacks((ItemStack)recipe.getValue()[0], result) || compareFluidStacks((ItemStack)recipe.getValue()[1], result))
				views.add(createView((ItemStack)recipe.getKey(), (ItemStack)recipe.getValue()[0], (ItemStack)recipe.getValue()[1]));
		}

        } else {
            ItemStack ingredient = focus.getValue();

		HashMap<Object, Object[]> recipes = (HashMap<Object, Object[]>) RadiolysisRecipes.getRecipesForNEI();

		for(Entry<Object, Object[]> recipe : recipes.entrySet()) {
			if(compareFluidStacks((ItemStack)recipe.getKey(), ingredient))
				views.add(createView((ItemStack)recipe.getKey(), (ItemStack)recipe.getValue()[0], (ItemStack)recipe.getValue()[1]));
		}

        }
        return views;
    }
    private MachineRecipeView createView(ItemStack input, ItemStack first, ItemStack second) {
        ItemStack shown = input.copy(); shown.setCount(1);
        return new MachineRecipeView(this, input, new ItemStack[][]{{shown}}, new ItemStack[][]{{first}, {second}})
            .addInputSlot(0, 34, 25).addOutputSlot(0, 118, 16).addOutputSlot(1, 118, 34);
    }
    @Override public void drawRecipe(MachineRecipeView view, Minecraft minecraft, int mouseX, int mouseY) {
        progress.draw(minecraft, 52, 19);
    }
    private IDrawableAnimated progress;
    private boolean compareFluidStacks(ItemStack a, ItemStack b) { return a.getItem() == b.getItem() && a.getItemDamage() == b.getItemDamage(); }
}
