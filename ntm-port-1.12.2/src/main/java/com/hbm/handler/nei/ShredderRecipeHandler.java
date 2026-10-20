package com.hbm.handler.nei;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.gui.GUIMachineShredder;
import com.hbm.inventory.recipes.ShredderRecipes;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
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
/** Native JEI category for the original ShredderRecipeHandler recipe source. */
public class ShredderRecipeHandler extends MachineRecipeCategory {
    private final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/nei/gui_nei_shredder.png");
    private IDrawable background;
    @Override public String getUid() { return "shredding"; }
    @Override public String getTitle() {
		return "Shredder";
	 }
    @Override public ItemStack[] getMachines() {
		return new ItemStack[]{
				new ItemStack(ModBlocks.machine_shredder)};
	 }
    @Override public IDrawable getBackground() { return background; }
    @Override public void initialize(IGuiHelper helper) {
        super.initialize(helper);
        background = helper.createDrawable(texture, 5, 11, 166, 65);
        energy = helper.createAnimatedDrawable(helper.createDrawable(texture, 36, 86, 16, 52), 480, IDrawableAnimated.StartDirection.TOP, true);
        progress = helper.createAnimatedDrawable(helper.createDrawable(texture, 100, 118, 24, 16), 48, IDrawableAnimated.StartDirection.LEFT, false);
    }
    @Override public void registerGuiAreas(IModRegistry registry) { registry.addRecipeClickArea(GUIMachineShredder.class, (63 - 7 + 4) + 5, (89 - 11) + 11, 34, 18, getUid()); }
    @Override public boolean isNavigationArea(int x, int y) {
        return (new java.awt.Rectangle(74 + 6, 23, 24, 18).contains(x, y));
    }
    @Override public List<MachineRecipeView> query(IFocus<ItemStack> focus) {
        List<MachineRecipeView> views = new ArrayList<>();
        if(focus == null) {
			Map<Object, Object> recipes = ShredderRecipes.getShredderRecipes();
			for(Map.Entry<Object, Object> recipe : recipes.entrySet()) {
				views.add(createView(((ComparableStack) recipe.getKey()).toStack(), (ItemStack) recipe.getValue()));
			}
		 }
        else if(focus.getMode() == IFocus.Mode.OUTPUT) {
            ItemStack result = focus.getValue();

		Map<Object, Object> recipes = ShredderRecipes.getShredderRecipes();
		for(Map.Entry<Object, Object> recipe : recipes.entrySet()) {
			if(RecipeStackMatcher.matchesType((ItemStack) recipe.getValue(), result))
				views.add(createView(((ComparableStack) recipe.getKey()).toStack(), (ItemStack) recipe.getValue()));
		}

        } else {
            ItemStack ingredient = focus.getValue();

		Map<Object, Object> recipes = ShredderRecipes.getShredderRecipes();
		for(Map.Entry<Object, Object> recipe : recipes.entrySet()) {
			if(RecipeStackMatcher.matchesCrafting(ingredient, ((ComparableStack) recipe.getKey()).toStack()))
				views.add(createView(((ComparableStack) recipe.getKey()).toStack(), (ItemStack) recipe.getValue()));
		}

        }
        return views;
    }
    private MachineRecipeView createView(ItemStack input, ItemStack result) {
        ItemStack shown = input.copy(); shown.setCount(1);
        ItemStack[] blades = {new ItemStack(ModItems.blades_advanced_alloy), new ItemStack(ModItems.blades_steel), new ItemStack(ModItems.blades_titanium), new ItemStack(ModItems.blades_desh)};
        return new MachineRecipeView(this, input, new ItemStack[][]{{shown}}, new ItemStack[][]{{result}})
            .addInputSlot(0, 39, 24).addOutputSlot(0, 129, 24)
            .addDecoration(blades, 84, 6).addDecoration(blades, 84, 42);
    }
    @Override public void drawRecipe(MachineRecipeView view, Minecraft minecraft, int mouseX, int mouseY) {
        energy.draw(minecraft, 3, 6); progress.draw(minecraft, 80, 23);
    }
    private IDrawableAnimated energy, progress;
}
