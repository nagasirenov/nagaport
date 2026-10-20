package com.hbm.handler.nei;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.Untested;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.gui.GUIMachineEPress;
import com.hbm.inventory.gui.GUIMachinePress;
import com.hbm.inventory.recipes.PressRecipes;
import com.hbm.items.machine.ItemStamp;
import com.hbm.items.machine.ItemStamp.StampType;
import com.hbm.lib.RefStrings;
import com.hbm.util.Tuple.Pair;
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
/** Native JEI category for the original PressRecipeHandler recipe source. */
public class PressRecipeHandler extends MachineRecipeCategory {
    private final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/nei/gui_nei_press.png");
    private IDrawable background;
    @Override public String getUid() { return "pressing"; }
    @Override public String getTitle() {
		return "Press";
	 }
    @Override public ItemStack[] getMachines() {
		return new ItemStack[]{
				new ItemStack(ModBlocks.machine_press),
				new ItemStack(ModBlocks.machine_epress),
				new ItemStack(ModBlocks.machine_conveyor_press)};
	 }
    @Override public IDrawable getBackground() { return background; }
    @Override public void initialize(IGuiHelper helper) {
        super.initialize(helper);
        background = helper.createDrawable(texture, 5, 11, 166, 65);
        progress = helper.createAnimatedDrawable(helper.createDrawable(texture, 0, 86, 18, 18), 20, IDrawableAnimated.StartDirection.TOP, false);
    }
    @Override public void registerGuiAreas(IModRegistry registry) { registry.addRecipeClickArea(GUIMachinePress.class, (74 + 6 + 18) + 5, (23) + 11, 24, 18, getUid()); registry.addRecipeClickArea(GUIMachineEPress.class, (74 + 6 + 18) + 5, (23) + 11, 24, 18, getUid()); }
    @Override public boolean isNavigationArea(int x, int y) {
        return (new java.awt.Rectangle(74 + 6, 23, 24, 18).contains(x, y));
    }
    @Override public List<MachineRecipeView> query(IFocus<ItemStack> focus) {
        List<MachineRecipeView> views = new ArrayList<>();
        if(focus == null) {

			HashMap<Pair<AStack, StampType>, ItemStack> recipes = PressRecipes.recipes;

			for(Map.Entry<Pair<AStack, StampType>, ItemStack> recipe : recipes.entrySet()) {
				views.add(createView(ItemStamp.stamps.get(recipe.getKey().getValue()), recipe.getKey().getKey(), recipe.getValue()));
			}
		 }
        else if(focus.getMode() == IFocus.Mode.OUTPUT) {
            ItemStack result = focus.getValue();


		HashMap<Pair<AStack, StampType>, ItemStack> recipes = PressRecipes.recipes;

		for(Map.Entry<Pair<AStack, StampType>, ItemStack> recipe : recipes.entrySet()) {
			if(RecipeStackMatcher.matchesType(recipe.getValue(), result))
				views.add(createView(ItemStamp.stamps.get(recipe.getKey().getValue()), recipe.getKey().getKey(), recipe.getValue()));
		}

        } else {
            ItemStack ingredient = focus.getValue();


		HashMap<Pair<AStack, StampType>, ItemStack> recipes = PressRecipes.recipes;

		for(Map.Entry<Pair<AStack, StampType>, ItemStack> recipe : recipes.entrySet()) {
			AStack in = recipe.getKey().getKey();
			StampType stamp = recipe.getKey().getValue();

			if(in.matchesRecipe(ingredient, true))
				views.add(createView(ItemStamp.stamps.get(recipe.getKey().getValue()), new ComparableStack(ingredient), recipe.getValue()));
			else if(ingredient.getItem() instanceof ItemStamp && ((ItemStamp)ingredient.getItem()).getStampType(ingredient.getItem(), ingredient.getItemDamage()) == stamp)
				views.add(createView(ingredient, recipe.getKey().getKey(), recipe.getValue()));
		}

        }
        return views;
    }
    private MachineRecipeView createView(Object stamp, AStack input, ItemStack result) {
        List<ItemStack> inputs = input.extractForNEI();
        ItemStack[] shown = new ItemStack[inputs.size()];
        for(int i = 0; i < shown.length; i++) { shown[i] = inputs.get(i).copy(); shown[i].setCount(1); }
        List<ItemStack> stamps = new ArrayList<>();
        stamps.addAll(Arrays.asList(com.hbm.handler.jei.RecipeItemAlternatives.from(stamp)));
        return new MachineRecipeView(this, input, new ItemStack[][]{shown, stamps.toArray(new ItemStack[0])}, new ItemStack[][]{{result}})
            .addInputSlot(0, 48, 42).addInputSlot(1, 48, 6).addOutputSlot(0, 111, 24);
    }
    @Override public void drawRecipe(MachineRecipeView view, Minecraft minecraft, int mouseX, int mouseY) {
        progress.draw(minecraft, 47, 24);
    }
    private IDrawableAnimated progress;
}
