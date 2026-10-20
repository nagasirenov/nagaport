package com.hbm.handler.nei;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;
import com.hbm.inventory.recipes.MachineRecipes;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.util.Tuple.Triplet;
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
/** Native JEI category for the original FluidRecipeHandler recipe source. */
public class FluidRecipeHandler extends MachineRecipeCategory {
    private final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/nei/gui_nei_fluid.png");
    private IDrawable background;
    @Override public String getUid() { return "fluidcons"; }
    @Override public String getTitle() {
		return "Fluid Containers";
	 }
    @Override public ItemStack[] getMachines() {
		return new ItemStack[]{
				new ItemStack(ModItems.fluid_barrel_empty),
				new ItemStack(ModItems.fluid_tank_empty),
				new ItemStack(ModItems.fluid_tank_lead_empty),
				new ItemStack(ModItems.canister_empty),
				new ItemStack(ModItems.gas_empty),
				new ItemStack(ModItems.cell_empty),
				new ItemStack(ModItems.disperser_canister_empty),
				new ItemStack(ModItems.glyphid_gland_empty)};
	 }
    @Override public IDrawable getBackground() { return background; }
    @Override public void initialize(IGuiHelper helper) {
        super.initialize(helper);
        background = helper.createDrawable(texture, 5, 11, 166, 65);

    }
    @Override public void registerGuiAreas(IModRegistry registry) {  }
    @Override public boolean isNavigationArea(int x, int y) {
        return (new java.awt.Rectangle(71, 23, 42, 18).contains(x, y));
    }
    @Override public List<MachineRecipeView> query(IFocus<ItemStack> focus) {
        List<MachineRecipeView> views = new ArrayList<>();
        if(focus == null) {
			List<Triplet<ItemStack, ItemStack, ItemStack>> recipes = MachineRecipes.instance().getFluidContainers();
			for(Triplet<ItemStack, ItemStack, ItemStack> recipe : recipes) {
				views.add(createView(recipe.getX(), recipe.getY(), recipe.getZ()));
			}
		 }
        else if(focus.getMode() == IFocus.Mode.OUTPUT) {
            ItemStack result = focus.getValue();

		List<Triplet<ItemStack, ItemStack, ItemStack>> recipes = MachineRecipes.instance().getFluidContainers();
		for(Triplet<ItemStack, ItemStack, ItemStack> recipe : recipes) {
			if(RecipeStackMatcher.matchesType(recipe.getY(), result) ||
					RecipeStackMatcher.matchesType(recipe.getZ(), result) ||
					compareFluidStacks(result, recipe.getX()))
				views.add(createView(recipe.getX(), recipe.getY(), recipe.getZ()));
		}

        } else {
            ItemStack ingredient = focus.getValue();

		List<Triplet<ItemStack, ItemStack, ItemStack>> recipes = MachineRecipes.instance().getFluidContainers();
		for(Triplet<ItemStack, ItemStack, ItemStack> recipe : recipes) {
			if(RecipeStackMatcher.matchesType(recipe.getY(), ingredient) ||
					RecipeStackMatcher.matchesType(recipe.getZ(), ingredient) ||
					compareFluidStacks(ingredient, recipe.getX()))
				views.add(createView(recipe.getX(), recipe.getY(), recipe.getZ()));
		}

        }
        return views;
    }
    private MachineRecipeView createView(ItemStack fluid, ItemStack empty, ItemStack full) {
        ItemStack shown = fluid.copy(); shown.setCount(1);
        ItemStack[][] inputs = empty == null ? new ItemStack[][]{{shown}} : new ItemStack[][]{{shown}, {empty}};
        MachineRecipeView view = new MachineRecipeView(this, fluid, inputs, new ItemStack[][]{{full}})
            .addInputSlot(0, 30, 24).addOutputSlot(0, 120, 24);
        if(empty != null) view.addInputSlot(1, 48, 24);
        return view;
    }
    @Override public void drawRecipe(MachineRecipeView view, Minecraft minecraft, int mouseX, int mouseY) {

    }
    private boolean compareFluidStacks(ItemStack a, ItemStack b) { return a.getItem() == b.getItem() && a.getItemDamage() == b.getItemDamage(); }
}
