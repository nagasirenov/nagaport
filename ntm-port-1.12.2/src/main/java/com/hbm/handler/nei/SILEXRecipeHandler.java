package com.hbm.handler.nei;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.gui.GUISILEX;
import com.hbm.inventory.recipes.SILEXRecipes;
import com.hbm.inventory.recipes.SILEXRecipes.SILEXRecipe;
import com.hbm.items.machine.ItemFELCrystal.EnumWavelengths;
import com.hbm.lib.RefStrings;
import com.hbm.util.WeightedRandomObject;
import com.hbm.util.i18n.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
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
/** Native JEI category for the original SILEXRecipeHandler recipe source. */
public class SILEXRecipeHandler extends MachineRecipeCategory {
    private final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/nei/gui_nei_silex.png");
    private IDrawable background;
    @Override public String getUid() { return "silex"; }
    @Override public String getTitle() {
		return "SILEX";
	 }
    @Override public ItemStack[] getMachines() {
		return new ItemStack[]{
				new ItemStack(ModBlocks.machine_silex)};
	 }
    @Override public IDrawable getBackground() { return background; }
    @Override public void initialize(IGuiHelper helper) {
        super.initialize(helper);
        background = helper.createDrawable(texture, 5, 11, 166, 65);

    }
    @Override public void registerGuiAreas(IModRegistry registry) { registry.addRecipeClickArea(GUISILEX.class, (39) + 5, (60) + 11, 60, 50, getUid()); }
    @Override public boolean isNavigationArea(int x, int y) {
        return (new java.awt.Rectangle(42, 34 - 11, 24, 18).contains(x, y));
    }
    @Override public List<MachineRecipeView> query(IFocus<ItemStack> focus) {
        List<MachineRecipeView> views = new ArrayList<>();
        if(focus == null) {

			Map<Object, SILEXRecipe> recipes = SILEXRecipes.getRecipes();

			for (Map.Entry<Object, SILEXRecipe> recipe : recipes.entrySet()) {
				views.add(createView(recipe.getKey(), recipe.getValue()));
			}

		 }
        else if(focus.getMode() == IFocus.Mode.OUTPUT) {
            ItemStack result = focus.getValue();


		Map<Object, SILEXRecipe> recipes = SILEXRecipes.getRecipes();

		for(Map.Entry<Object, SILEXRecipe> recipe : recipes.entrySet()) {

			for(WeightedRandomObject out : recipe.getValue().outputs) {

				if(RecipeStackMatcher.matchesCrafting(out.asStack(), result)) {
					views.add(createView(recipe.getKey(), recipe.getValue()));
				}
			}
		}

        } else {
            ItemStack ingredient = focus.getValue();


		Map<Object, SILEXRecipe> recipes = SILEXRecipes.getRecipes();

		for(Map.Entry<Object, SILEXRecipe> recipe : recipes.entrySet()) {

			if(recipe.getKey() instanceof ItemStack) {

				if (RecipeStackMatcher.matchesCrafting(ingredient, (ItemStack)recipe.getKey()))
					views.add(createView(recipe.getKey(), recipe.getValue()));

			} else if (recipe.getKey() instanceof ArrayList) {

				for(Object o : (ArrayList)recipe.getKey()) {
					ItemStack stack = (ItemStack)o;

					if (RecipeStackMatcher.matchesCrafting(ingredient, stack))
						views.add(createView(stack, recipe.getValue()));
				}
			}
		}

        }
        return views;
    }
    private MachineRecipeView createView(Object input, SILEXRecipe recipe) {
        ItemStack[][] outputs = new ItemStack[recipe.outputs.size()][];
        for(int i = 0; i < outputs.length; i++) outputs[i] = new ItemStack[]{recipe.outputs.get(i).asStack()};
        MachineRecipeView view = new MachineRecipeView(this, recipe,
            new ItemStack[][]{com.hbm.handler.jei.RecipeItemAlternatives.from(input)}, outputs).addInputSlot(0, 12, 24);
        for(int i = 0; i < outputs.length; i++) { int[] xy = outputPosition(i, outputs.length); view.addOutputSlot(i, xy[0], xy[1]); }
        return view;
    }
    private int[] outputPosition(int index, int count) {
        int sep = count > 4 ? 3 : 2;
        return index < sep ? new int[]{68, 24 + index * 18 - 9 * ((Math.min(count, sep) + 1) / 2)} :
            new int[]{116, 24 + (index - sep) * 18 - 9 * ((Math.min(count - sep, sep) + 1) / 2)};
    }
    @Override public void drawRecipe(MachineRecipeView view, Minecraft minecraft, int mouseX, int mouseY) {
        SILEXRecipe recipe = (SILEXRecipe) view.source;
        double weight = 0;
        for(WeightedRandomObject output : recipe.outputs) weight += output.itemWeight;
        for(int i = 0; i < recipe.outputs.size(); i++) {
            double chance = 100 * recipe.outputs.get(i).itemWeight / weight;
            int[] xy = outputPosition(i, recipe.outputs.size());
            minecraft.fontRenderer.drawString(((int)(chance * 10D) / 10D) + "%", xy[0] + 18, xy[1] + 4, 0x404040);
        }
        double produced = recipe.fluidProduced / recipe.fluidConsumed;
        String amount = ((int)(produced * 10D) / 10D) + "x";
        minecraft.fontRenderer.drawString(amount, 52 - minecraft.fontRenderer.getStringWidth(amount) / 2, 43, 0x404040);
        EnumWavelengths strength = recipe.laserStrength;
        String wavelength = strength == EnumWavelengths.NULL ? TextFormatting.WHITE + "N/A" : strength.textColor + I18nUtil.resolveKey(strength.name);
        minecraft.fontRenderer.drawString(wavelength, 33 - minecraft.fontRenderer.getStringWidth(wavelength) / 2, 8, 0x404040);
    }

}
