package com.hbm.handler.nei;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.recipes.CrucibleRecipes;
import com.hbm.items.machine.ItemMold;
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
/** Native JEI category for the original CrucibleCastingHandler recipe source. */
public class CrucibleCastingHandler extends MachineRecipeCategory {
    private final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/nei/gui_nei_foundry.png");
    private IDrawable background;
    @Override public String getUid() { return "ntmCrucibleFoundry"; }
    @Override public String getTitle() {
		return "Crucible Casting";
	 }
    @Override public ItemStack[] getMachines() {
		return new ItemStack[]{
				new ItemStack(ModBlocks.foundry_basin),
				new ItemStack(ModBlocks.foundry_mold),
				new ItemStack(ModBlocks.machine_strand_caster)};
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

			for(ItemStack[] recipe : CrucibleRecipes.getMoldRecipes()) {
				views.add(createView(recipe));
			}
		 }
        else if(focus.getMode() == IFocus.Mode.OUTPUT) {
            ItemStack result = focus.getValue();


		for(ItemStack[] recipe : CrucibleRecipes.getMoldRecipes()) {
			if(RecipeStackMatcher.matchesCrafting(recipe[3], result)) {
				views.add(createView(recipe));
			}
		}

        } else {
            ItemStack ingredient = focus.getValue();


		for(ItemStack[] recipe : CrucibleRecipes.getMoldRecipes()) {

			for(int i = 0; i < 3; i++) {
				if(RecipeStackMatcher.matchesCrafting(recipe[i], ingredient)) {
					views.add(createView(recipe));
					break;
				}
			}
		}

        }
        return views;
    }
    private MachineRecipeView createView(ItemStack[] stacks) {
        ItemStack output = ItemMold.moldById.get(stacks[1].getItemDamage()).getOutput(Mats.matById.get(stacks[0].getItemDamage()));
        return new MachineRecipeView(this, stacks, new ItemStack[][]{{stacks[0]}, {stacks[1]}, {stacks[2]}}, new ItemStack[][]{{output}})
            .addInputSlot(0, 48, 24).addInputSlot(1, 75, 6).addInputSlot(2, 75, 42).addOutputSlot(0, 102, 24);
    }
    @Override public void drawRecipe(MachineRecipeView view, Minecraft minecraft, int mouseX, int mouseY) {

    }

}
