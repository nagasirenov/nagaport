package com.hbm.handler.jei;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

/** Common presentation and navigation for NTM's native JEI recipe categories. */
public abstract class MachineRecipeCategory implements IRecipeCategory<MachineRecipeView> {

    @Nullable
    private static IJeiRuntime runtime;
    @Nullable
    private IDrawable background;

    /** Called during category registration, before JEI requests the background. */
    public void initialize(IGuiHelper guiHelper) {
        background = Objects.requireNonNull(guiHelper, "guiHelper").createBlankDrawable(166, 65);
    }

    /** Called by the JEI plugin when its runtime becomes available. */
    public static void setRuntime(@Nullable IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    @Override
    public abstract String getUid();

    @Override
    public abstract String getTitle();

    @Override
    public String getModName() {
        return "hbm";
    }

    @Override
    public IDrawable getBackground() {
        if (background == null) {
            throw new IllegalStateException("JEI category has not been initialized: " + getUid());
        }
        return background;
    }

    /**
     * Resolve recipes from their authoritative source. A null focus requests all
     * recipes; focused queries retain each machine's own metadata and NBT rules.
     */
    public abstract List<MachineRecipeView> query(@Nullable IFocus<ItemStack> focus);

    public void registerGuiAreas(IModRegistry registry) {
    }

    public ItemStack[] getMachines() {
        return new ItemStack[0];
    }

    public void drawRecipe(MachineRecipeView view, Minecraft minecraft, int mouseX, int mouseY) {
    }

    public boolean isNavigationArea(int mouseX, int mouseY) {
        return mouseX >= 147 && mouseX < 165 && mouseY >= 1 && mouseY < 19;
    }

    /** Wrapper tooltips are separate from JEI's independently rendered category tooltips. */
    public List<String> getRecipeTooltips(MachineRecipeView view, int mouseX, int mouseY) {
        return isNavigationArea(mouseX, mouseY)
                ? Collections.singletonList(I18n.format("jei.tooltip.show.all.recipes"))
                : Collections.emptyList();
    }

    public boolean handleClick(MachineRecipeView view, Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
        if ((mouseButton == 0 || mouseButton == 1) && isNavigationArea(mouseX, mouseY) && runtime != null) {
            runtime.getRecipesGui().showCategories(Collections.singletonList(getUid()));
            return true;
        }
        return false;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, MachineRecipeView recipeWrapper, IIngredients ingredients) {
        if (recipeWrapper.getCategory() != this) {
            throw new IllegalArgumentException("Recipe belongs to a different JEI category");
        }
        recipeWrapper.initializeSlots(recipeLayout.getItemStacks());
    }
}
