package com.hbm.handler.nei;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.gui.GUIMachineReactorBreeding;
import com.hbm.inventory.recipes.BreederRecipes;
import com.hbm.inventory.recipes.BreederRecipes.BreederRecipe;
import codechicken.lib.gui.GuiDraw;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import java.util.ArrayList;
import com.hbm.handler.jei.MachineRecipeCategory;
import com.hbm.handler.jei.MachineRecipeView;
import com.hbm.handler.jei.RecipeStackMatcher;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.recipe.IFocus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
/** Native JEI category for the original BreederRecipeHandler recipe source. */
public class BreederRecipeHandler extends MachineRecipeCategory {
    private final ResourceLocation texture = new ResourceLocation(GUIMachineReactorBreeding.texture.toString());
    private IDrawable background;
    @Override public String getUid() { return "breeding"; }
    @Override public String getTitle() {
		return "Breeding Reactor";
	 }
    @Override public ItemStack[] getMachines() {
		return new ItemStack[]{
				new ItemStack(ModBlocks.machine_reactor_breeding)};
	 }
    @Override public IDrawable getBackground() { return background; }
    @Override public void initialize(IGuiHelper helper) {
        super.initialize(helper);
        background = helper.createDrawable(texture, 5, 11, 166, 65);
        progress = helper.createAnimatedDrawable(helper.createDrawable(texture, 176, 0, 70, 20), 50, IDrawableAnimated.StartDirection.LEFT, false);
    }
    @Override public void registerGuiAreas(IModRegistry registry) { registry.addRecipeClickArea(GUIMachineReactorBreeding.class, (68) + 5, (9) + 11, 30, 37, getUid()); }
    @Override public boolean isNavigationArea(int x, int y) {
        return (new java.awt.Rectangle(68, 9, 30, 37).contains(x, y));
    }
    @Override public List<MachineRecipeView> query(IFocus<ItemStack> focus) {
        List<MachineRecipeView> views = new ArrayList<>();
        if(focus == null) {

			Map<ItemStack, BreederRecipe> recipes = BreederRecipes.getAllRecipes();

			for(Map.Entry<ItemStack, BreederRecipe> recipe : recipes.entrySet()) {
				views.add(createView(recipe.getKey(), recipe.getValue().output, recipe.getValue().flux));
			}
		 }
        else if(focus.getMode() == IFocus.Mode.OUTPUT) {
            ItemStack result = focus.getValue();


		Map<ItemStack, BreederRecipe> recipes = BreederRecipes.getAllRecipes();

		for(Map.Entry<ItemStack, BreederRecipe> recipe : recipes.entrySet()) {
			if(RecipeStackMatcher.matchesType(recipe.getValue().output, result))
				views.add(createView(recipe.getKey(), recipe.getValue().output, recipe.getValue().flux));
		}

        } else {
            ItemStack ingredient = focus.getValue();


		Map<ItemStack, BreederRecipe> recipes = BreederRecipes.getAllRecipes();

		for(Map.Entry<ItemStack, BreederRecipe> recipe : recipes.entrySet()) {
			if(RecipeStackMatcher.matchesType(ingredient, (ItemStack) recipe.getKey()))
				views.add(createView(recipe.getKey(), recipe.getValue().output, recipe.getValue().flux));
		}

        }
        return views;
    }
    private MachineRecipeView createView(ItemStack input, ItemStack result, int flux) {
        ItemStack shown = input.copy(); shown.setCount(1);
        return new MachineRecipeView(this, flux, new ItemStack[][]{{shown}}, new ItemStack[][]{{result}})
            .addInputSlot(0, 30, 24).addOutputSlot(0, 120, 24);
    }
    @Override public void drawRecipe(MachineRecipeView view, Minecraft minecraft, int mouseX, int mouseY) {
        progress.draw(minecraft, 48, 21);
        String flux = view.source.toString();
        minecraft.fontRenderer.drawString(flux, 83 - minecraft.fontRenderer.getStringWidth(flux) / 2, 10, 0x08FF00);
    }
    private IDrawableAnimated progress;
}
