package com.hbm.handler.nei;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.gui.GUIMachineCyclotron;
import com.hbm.inventory.recipes.CyclotronRecipes;
import com.hbm.lib.RefStrings;
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
/** Native JEI category for the original CyclotronRecipeHandler recipe source. */
public class CyclotronRecipeHandler extends MachineRecipeCategory {
    private final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/nei/gui_nei_cyclotron.png");
    private IDrawable background;
    @Override public String getUid() { return "cyclotronProcessing"; }
    @Override public String getTitle() {
		return "Cyclotron";
	 }
    @Override public ItemStack[] getMachines() {
		return new ItemStack[]{
				new ItemStack(ModBlocks.machine_cyclotron)};
	 }
    @Override public IDrawable getBackground() { return background; }
    @Override public void initialize(IGuiHelper helper) {
        super.initialize(helper);
        background = helper.createDrawable(texture, 5, 11, 166, 65);
        progress = helper.createAnimatedDrawable(helper.createDrawable(texture, 100, 119, 24, 16), 48, IDrawableAnimated.StartDirection.LEFT, false);
    }
    @Override public void registerGuiAreas(IModRegistry registry) { registry.addRecipeClickArea(GUIMachineCyclotron.class, (48 - 5) + 5, (27 - 11) + 11, 34, 34, getUid()); }
    @Override public boolean isNavigationArea(int x, int y) {
        return (new java.awt.Rectangle(83 - 3 + 16 - 52, 5 + 18 + 1, 24, 18).contains(x, y));
    }
    @Override public List<MachineRecipeView> query(IFocus<ItemStack> focus) {
        List<MachineRecipeView> views = new ArrayList<>();
        if(focus == null) {
			Map<Object[], Object> recipes = CyclotronRecipes.getRecipes();
			for (Map.Entry<Object[], Object> recipe : recipes.entrySet()) {
				views.add(createView((ItemStack)recipe.getKey()[0], (ItemStack)recipe.getKey()[1], (ItemStack)recipe.getValue()));
			}
		 }
        else if(focus.getMode() == IFocus.Mode.OUTPUT) {
            ItemStack result = focus.getValue();

		Map<Object[], Object> recipes = CyclotronRecipes.getRecipes();
		for (Map.Entry<Object[], Object> recipe : recipes.entrySet()) {
			if (RecipeStackMatcher.matchesType((ItemStack)recipe.getValue(), result))
				views.add(createView((ItemStack)recipe.getKey()[0], (ItemStack)recipe.getKey()[1], (ItemStack)recipe.getValue()));
		}

        } else {
            ItemStack ingredient = focus.getValue();

		Map<Object[], Object> recipes = CyclotronRecipes.getRecipes();
		for (Map.Entry<Object[], Object> recipe : recipes.entrySet()) {
			if (RecipeStackMatcher.matchesType(ingredient, (ItemStack)recipe.getKey()[0]) || RecipeStackMatcher.matchesType(ingredient, (ItemStack)recipe.getKey()[1]))
				views.add(createView((ItemStack)recipe.getKey()[0], (ItemStack)recipe.getKey()[1], (ItemStack)recipe.getValue()));
		}

        }
        return views;
    }
    private MachineRecipeView createView(ItemStack input1, ItemStack input2, ItemStack result) {
        ItemStack first = input1.copy(); first.setCount(1);
        ItemStack second = input2.copy(); second.setCount(1);
        return new MachineRecipeView(this, new ItemStack[]{input1, input2}, new ItemStack[][]{{first}, {second}}, new ItemStack[][]{{result}})
            .addInputSlot(0, 21, 24).addInputSlot(1, 75, 24).addOutputSlot(0, 129, 24);
    }
    @Override public void drawRecipe(MachineRecipeView view, Minecraft minecraft, int mouseX, int mouseY) {
        progress.draw(minecraft, 44, 24);
    }
    private IDrawableAnimated progress;
}
