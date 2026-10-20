package com.hbm.handler.nei;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.gui.GUIBook;
import com.hbm.inventory.recipes.MagicRecipes;
import com.hbm.inventory.recipes.MagicRecipes.MagicRecipe;
import com.hbm.items.ModItems;
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
/** Native JEI category for the original BookRecipeHandler recipe source. */
public class BookRecipeHandler extends MachineRecipeCategory {
    private final ResourceLocation texture = new ResourceLocation(GUIBook.texture.toString());
    private IDrawable background;
    @Override public String getUid() { return "book_of_boxcars"; }
    @Override public String getTitle() {
		return "Black Book";
	 }
    @Override public ItemStack[] getMachines() {
		return new ItemStack[]{
				new ItemStack(ModItems.book_of_)};
	 }
    @Override public IDrawable getBackground() { return background; }
    @Override public void initialize(IGuiHelper helper) {
        super.initialize(helper);
        background = helper.createDrawable(texture, 5, 11, 166, 65);

    }
    @Override public void registerGuiAreas(IModRegistry registry) { registry.addRecipeClickArea(GUIBook.class, (84) + 5, (34 - 11) + 11, 24, 18, getUid()); }
    @Override public boolean isNavigationArea(int x, int y) {
        return (new java.awt.Rectangle(84, 34 - 11, 24, 18).contains(x, y));
    }
    @Override public List<MachineRecipeView> query(IFocus<ItemStack> focus) {
        List<MachineRecipeView> views = new ArrayList<>();
        if(focus == null) {

			List<MagicRecipe> recipes = MagicRecipes.getRecipes();

			for(MagicRecipe recipe : recipes) {

				List<Object> input = new ArrayList();

				for(AStack stack : recipe.in) {

					if(stack instanceof ComparableStack)
						input.add(((ComparableStack)stack).toStack());

					if(stack instanceof OreDictStack)
						input.add(((OreDictStack)stack).toStacks());
				}

				views.add(createView(input, recipe.out));
			}

		 }
        else if(focus.getMode() == IFocus.Mode.OUTPUT) {
            ItemStack result = focus.getValue();


		List<MagicRecipe> recipes = MagicRecipes.getRecipes();

		for (MagicRecipe recipe : recipes) {

			if (RecipeStackMatcher.matchesCrafting(recipe.out, result)) {

				List<Object> input = new ArrayList();

				for(AStack stack : recipe.in) {

					if(stack instanceof ComparableStack)
						input.add(((ComparableStack)stack).toStack());

					if(stack instanceof OreDictStack)
						input.add(((OreDictStack)stack).toStacks());
				}

				views.add(createView(input, recipe.out));
			}
		}

        } else {
            ItemStack ingredient = focus.getValue();


		List<MagicRecipe> recipes = MagicRecipes.getRecipes();

		for (MagicRecipe recipe : recipes) {

			for(AStack astack : recipe.in) {

				if(astack.matchesRecipe(ingredient, true)) {

					List<Object> input = new ArrayList();

					for(AStack stack : recipe.in) {

						if(stack instanceof ComparableStack)
							input.add(((ComparableStack)stack).toStack());

						if(stack instanceof OreDictStack)
							input.add(((OreDictStack)stack).toStacks());
					}

					views.add(createView(input, recipe.out));
					break;
				}
			}
		}

        }
        return views;
    }
    private MachineRecipeView createView(List<Object> in, ItemStack result) {
        ItemStack[][] inputs = new ItemStack[Math.min(in.size(), 4)][];
        for(int i = 0; i < inputs.length; i++) inputs[i] = com.hbm.handler.jei.RecipeItemAlternatives.from(in.get(i));
        MachineRecipeView view = new MachineRecipeView(this, in, inputs, new ItemStack[][]{{result}}).addOutputSlot(0, 119, 24);
        for(int i = 0; i < inputs.length; i++) view.addInputSlot(i, 25 + (i % 2) * 36, 6 + (i / 2) * 36);
        return view;
    }
    @Override public void drawRecipe(MachineRecipeView view, Minecraft minecraft, int mouseX, int mouseY) {

    }

}
