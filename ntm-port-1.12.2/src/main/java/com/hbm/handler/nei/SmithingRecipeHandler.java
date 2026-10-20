package com.hbm.handler.nei;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.gui.GUIAnvil;
import com.hbm.inventory.recipes.anvil.AnvilRecipes;
import com.hbm.inventory.recipes.anvil.AnvilSmithingRecipe;
import com.hbm.lib.RefStrings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
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
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
/** Native JEI category for the original SmithingRecipeHandler recipe source. */
public class SmithingRecipeHandler extends MachineRecipeCategory {
    private final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/nei/gui_nei_smithing.png");
    private IDrawable background;
    @Override public String getUid() { return "ntmSmithing"; }
    @Override public String getTitle() {
		return "Anvil";
	 }
    @Override public ItemStack[] getMachines() {
		return new ItemStack[]{
				new ItemStack(ModBlocks.anvil_iron),
				new ItemStack(ModBlocks.anvil_lead),
				new ItemStack(ModBlocks.anvil_steel),
				new ItemStack(ModBlocks.anvil_desh),
				new ItemStack(ModBlocks.anvil_saturnite),
				new ItemStack(ModBlocks.anvil_ferrouranium),
				new ItemStack(ModBlocks.anvil_bismuth_bronze),
				new ItemStack(ModBlocks.anvil_arsenic_bronze),
				new ItemStack(ModBlocks.anvil_schrabidate),
				new ItemStack(ModBlocks.anvil_dnt),
				new ItemStack(ModBlocks.anvil_osmiridium),
				new ItemStack(ModBlocks.anvil_murky)};
	 }
    @Override public IDrawable getBackground() { return background; }
    @Override public void initialize(IGuiHelper helper) {
        super.initialize(helper);
        background = helper.createDrawable(texture, 5, 11, 166, 65);

    }
    @Override public void registerGuiAreas(IModRegistry registry) { registry.addRecipeClickArea(GUIAnvil.class, (56 + 9) + 5, (24 - 9) + 11, 18, 18, getUid()); registry.addRecipeClickArea(GUIAnvil.class, (56 + 9 - 18 * 2) + 5, (24 - 9) + 11, 18, 18, getUid()); }
    @Override public boolean isNavigationArea(int x, int y) {
        return (new java.awt.Rectangle(56, 24, 18, 18).contains(x, y) || new java.awt.Rectangle(92, 24, 18, 18).contains(x, y));
    }
    @Override public List<MachineRecipeView> query(IFocus<ItemStack> focus) {
        List<MachineRecipeView> views = new ArrayList<>();
        if(focus == null) {
			List<AnvilSmithingRecipe> recipes = AnvilRecipes.getSmithing();

			for(AnvilSmithingRecipe recipe : recipes) {
				views.add(createView(recipe));
			}
		 }
        else if(focus.getMode() == IFocus.Mode.OUTPUT) {
            ItemStack result = focus.getValue();


		List<AnvilSmithingRecipe> recipes = AnvilRecipes.getSmithing();

		for(AnvilSmithingRecipe recipe : recipes) {
			if(RecipeStackMatcher.matchesCrafting(recipe.getSimpleOutput(), result)) {
				views.add(createView(recipe));
			}
		}

        } else {
            ItemStack ingredient = focus.getValue();


		List<AnvilSmithingRecipe> recipes = AnvilRecipes.getSmithing();

		outer:
		for(AnvilSmithingRecipe recipe : recipes) {

			for(ItemStack left : recipe.getLeft()) {
				if(RecipeStackMatcher.matchesCrafting(left, ingredient)) {
					views.add(createView(recipe));
					continue outer;
				}
			}

			for(ItemStack right : recipe.getRight()) {
				if(RecipeStackMatcher.matchesCrafting(right, ingredient)) {
					views.add(createView(recipe));
					continue outer;
				}
			}
		}

        }
        return views;
    }
    private MachineRecipeView createView(AnvilSmithingRecipe recipe) {
        ItemStack[] left = com.hbm.handler.jei.RecipeItemAlternatives.from(recipe.getLeft());
        ItemStack[] right = com.hbm.handler.jei.RecipeItemAlternatives.from(recipe.getRight());
        ItemStack output = recipe.getOutput(left.length == 0 ? ItemStack.EMPTY : left[0], right.length == 0 ? ItemStack.EMPTY : right[0]);
        return new MachineRecipeView(this, recipe, new ItemStack[][]{left, right}, new ItemStack[][]{{output}})
            .addInputSlot(0, 39, 24).addInputSlot(1, 75, 24).addOutputSlot(0, 111, 24);
    }
    @Override public void drawRecipe(MachineRecipeView view, Minecraft minecraft, int mouseX, int mouseY) {
        minecraft.fontRenderer.drawString("Tier " + ((AnvilSmithingRecipe) view.source).tier, 52, 43, 0x404040);
    }

}
