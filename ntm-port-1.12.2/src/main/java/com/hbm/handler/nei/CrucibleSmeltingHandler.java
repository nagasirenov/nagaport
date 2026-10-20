package com.hbm.handler.nei;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.recipes.CrucibleRecipes;
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
/** Native JEI category for the original CrucibleSmeltingHandler recipe source. */
public class CrucibleSmeltingHandler extends MachineRecipeCategory {
    private final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/nei/gui_nei_crucible_smelting.png");
    private IDrawable background;
    @Override public String getUid() { return "ntmCrucibleSmelting"; }
    @Override public String getTitle() {
		return "Crucible Smelting";
	 }
    @Override public ItemStack[] getMachines() {
		return new ItemStack[]{
				new ItemStack(ModBlocks.machine_crucible)};
	 }
    @Override public IDrawable getBackground() { return background; }
    @Override public void initialize(IGuiHelper helper) {
        super.initialize(helper);
        background = helper.createDrawable(texture, 5, 11, 166, 65);

    }
    @Override public void registerGuiAreas(IModRegistry registry) {  }
    @Override public boolean isNavigationArea(int x, int y) {
        return (new java.awt.Rectangle(65, 23, 36, 18).contains(x, y));
    }
    @Override public List<MachineRecipeView> query(IFocus<ItemStack> focus) {
        List<MachineRecipeView> views = new ArrayList<>();
        if(focus == null) {

			HashMap<AStack, List<ItemStack>> smelting = CrucibleRecipes.getSmeltingRecipes();

			for(Entry<AStack, List<ItemStack>> recipe : smelting.entrySet()) {
				views.add(createView(recipe.getKey(), recipe.getValue()));
			}
		 }
        else if(focus.getMode() == IFocus.Mode.OUTPUT) {
            ItemStack result = focus.getValue();

		HashMap<AStack, List<ItemStack>> smelting = CrucibleRecipes.getSmeltingRecipes();

		for(Entry<AStack, List<ItemStack>> recipe : smelting.entrySet()) {

			for(ItemStack stack : recipe.getValue()) {

				if(RecipeStackMatcher.matchesCrafting(stack, result)) {
					views.add(createView(recipe.getKey(), recipe.getValue()));
					break;
				}
			}
		}

        } else {
            ItemStack ingredient = focus.getValue();

		HashMap<AStack, List<ItemStack>> smelting = CrucibleRecipes.getSmeltingRecipes();

		for(Entry<AStack, List<ItemStack>> recipe : smelting.entrySet()) {
			if(recipe.getKey().matchesRecipe(ingredient, true)) {
				views.add(createView(recipe.getKey(), recipe.getValue()));
			}
		}

        }
        return views;
    }
    private MachineRecipeView createView(AStack input, List<ItemStack> outputs) {
        ItemStack[][] out = new ItemStack[outputs.size()][];
        for(int i = 0; i < out.length; i++) out[i] = new ItemStack[]{outputs.get(i)};
        MachineRecipeView view = new MachineRecipeView(this, input, new ItemStack[][]{input.extractForNEI().toArray(new ItemStack[0])}, out)
            .addInputSlot(0, 48, 24).addDecoration(new ItemStack[]{new ItemStack(ModBlocks.machine_crucible)}, 75, 42);
        for(int i = 0; i < out.length; i++) view.addOutputSlot(i, 102 + (i % 3) * 18, 6 + (i / 3) * 18);
        return view;
    }
    @Override public void drawRecipe(MachineRecipeView view, Minecraft minecraft, int mouseX, int mouseY) {

    }

}
