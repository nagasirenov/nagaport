package com.hbm.handler.nei;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.jei.MachineRecipeCategory;
import com.hbm.handler.jei.MachineRecipeView;
import com.hbm.inventory.gui.GUIDiFurnace;
import com.hbm.inventory.recipes.BlastFurnaceRecipes;
import com.hbm.inventory.recipes.MachineRecipes;
import com.hbm.handler.jei.RecipeStackMatcher;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.recipe.IFocus;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

/** Blast furnace recipes registered directly with JEI, including the existing fuel display. */
public class AlloyFurnaceRecipeHandler extends MachineRecipeCategory {
    private IDrawable background;
    private IDrawableAnimated flame;
    private IDrawableAnimated progress;
    private IDrawableAnimated fuelLevel;

    @Override public String getUid() { return "alloysmelting"; }
    @Override public String getTitle() { return "Blast Furnace"; }
    @Override public ItemStack[] getMachines() {
        return new ItemStack[]{new ItemStack(ModBlocks.machine_difurnace_off), new ItemStack(ModBlocks.machine_difurnace_rtg_off)};
    }
    @Override public void initialize(IGuiHelper helper) {
        super.initialize(helper);
        background = helper.createDrawable(GUIDiFurnace.texture, 5, 11, 166, 65);
        flame = helper.createAnimatedDrawable(helper.createDrawable(GUIDiFurnace.texture, 176, 0, 14, 14), 48, IDrawableAnimated.StartDirection.TOP, true);
        progress = helper.createAnimatedDrawable(helper.createDrawable(GUIDiFurnace.texture, 176, 14, 24, 16), 48, IDrawableAnimated.StartDirection.LEFT, false);
        fuelLevel = helper.createAnimatedDrawable(helper.createDrawable(GUIDiFurnace.texture, 201, 0, 16, 52), 480, IDrawableAnimated.StartDirection.TOP, true);
    }
    @Override public IDrawable getBackground() { return background; }
    @Override public void registerGuiAreas(IModRegistry registry) {
        registry.addRecipeClickArea(GUIDiFurnace.class, (96) + 5, (25) + 11, 24, 18, getUid());
    }
    @Override public List<MachineRecipeView> query(IFocus<ItemStack> focus) {
        List<MachineRecipeView> views = new ArrayList<>();
        for(Entry<List<ItemStack>[], ItemStack> recipe : BlastFurnaceRecipes.getRecipesForNEI().entrySet()) {
            int matches = focus == null ? 1 : 0;
            if(focus != null && focus.getMode() == IFocus.Mode.OUTPUT) {
                if(RecipeStackMatcher.matchesType(recipe.getValue(), focus.getValue())) matches = 1;
            } else if(focus != null) {
                // Preserve the source's occurrence-based usage results when both inputs match.
                for(List<ItemStack> input : recipe.getKey()) for(ItemStack variant : input)
                    if(RecipeStackMatcher.matchesType(focus.getValue(), variant)) matches++;
            }
            for(int i = 0; i < matches; i++) {
                MachineRecipeView view = new MachineRecipeView(this, recipe.getKey(),
                    new ItemStack[][]{recipe.getKey()[0].toArray(new ItemStack[0]), recipe.getKey()[1].toArray(new ItemStack[0])},
                    new ItemStack[][]{new ItemStack[]{recipe.getValue()}});
                view.addInputSlot(0, 75, 7).addInputSlot(1, 75, 43).addOutputSlot(0, 129, 25);
                List<ItemStack> fuels = new ArrayList<>();
                for(ItemStack fuel : MachineRecipes.instance().getAlloyFuels()) fuels.add(fuel);
                if(!fuels.isEmpty()) view.addDecoration(fuels.toArray(new ItemStack[0]), 3, 25);
                views.add(view);
            }
        }
        return views;
    }
    @Override public void drawRecipe(MachineRecipeView view, Minecraft minecraft, int mouseX, int mouseY) {
        flame.draw(minecraft, 57, 26);
        progress.draw(minecraft, 96, 24);
        fuelLevel.draw(minecraft, 39, 7);
    }
    @Override public boolean isNavigationArea(int x, int y) {
        return x >= 96 && x < 120 && y >= 25 && y < 43;
    }
}
