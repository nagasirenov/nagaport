package com.hbm.handler.nei;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.inventory.recipes.CrucibleRecipes;
import com.hbm.inventory.recipes.CrucibleRecipes.CrucibleRecipe;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemScraps;
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
/** Native JEI category for the original CrucibleAlloyingHandler recipe source. */
public class CrucibleAlloyingHandler extends MachineRecipeCategory {
    private final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/nei/gui_nei_crucible.png");
    private IDrawable background;
    @Override public String getUid() { return "ntmCrucibleAlloying"; }
    @Override public String getTitle() {
		return "Crucible Alloying";
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

			for(CrucibleRecipe recipe : CrucibleRecipes.recipes) {
				views.add(createView(recipe));
			}
		 }
        else if(focus.getMode() == IFocus.Mode.OUTPUT) {
            ItemStack result = focus.getValue();


		if(result.getItem() != ModItems.scraps)
			return views;

		NTMMaterial material = Mats.matById.get(result.getItemDamage());

		for(CrucibleRecipe recipe : CrucibleRecipes.recipes) {

			for(MaterialStack stack : recipe.output) {
				if(stack.material == material) {
					views.add(createView(recipe));
					break;
				}
			}
		}

        } else {
            ItemStack ingredient = focus.getValue();


		if(ingredient.getItem() != ModItems.scraps)
			return views;

		NTMMaterial material = Mats.matById.get(ingredient.getItemDamage());

		for(CrucibleRecipe recipe : CrucibleRecipes.recipes) {

			for(MaterialStack stack : recipe.input) {
				if(stack.material == material) {
					views.add(createView(recipe));
					break;
				}
			}
		}

        }
        return views;
    }
    private MachineRecipeView createView(CrucibleRecipe recipe) {
        ItemStack[][] input = new ItemStack[recipe.input.length][];
        ItemStack[][] output = new ItemStack[recipe.output.length][];
        for(int i = 0; i < input.length; i++) input[i] = new ItemStack[]{ItemScraps.create(recipe.input[i], true)};
        for(int i = 0; i < output.length; i++) output[i] = new ItemStack[]{ItemScraps.create(recipe.output[i], true)};
        MachineRecipeView view = new MachineRecipeView(this, recipe, input, output)
            .addDecoration(new ItemStack[]{new ItemStack(ModItems.crucible_template, 1, recipe.getId())}, 75, 6)
            .addDecoration(new ItemStack[]{new ItemStack(ModBlocks.machine_crucible)}, 75, 42);
        for(int i = 0; i < input.length; i++) view.addInputSlot(i, 12 + (i % 3) * 18, 6 + (i / 3) * 18);
        for(int i = 0; i < output.length; i++) view.addOutputSlot(i, 102 + (i % 3) * 18, 6 + (i / 3) * 18);
        return view;
    }
    @Override public void drawRecipe(MachineRecipeView view, Minecraft minecraft, int mouseX, int mouseY) {

    }

}
