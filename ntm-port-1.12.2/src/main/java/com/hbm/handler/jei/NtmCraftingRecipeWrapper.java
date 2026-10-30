package com.hbm.handler.jei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.hbm.crafting.nativeapi.NtmCraftingRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IStackHelper;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IShapedRecipe;

/** Shows NTM's native crafting recipes (shaped and shapeless) in JEI's crafting category. */
public class NtmCraftingRecipeWrapper implements ICraftingRecipeWrapper {
    private final NtmCraftingRecipe recipe;
    private final IStackHelper stackHelper;

    private NtmCraftingRecipeWrapper(NtmCraftingRecipe recipe, IStackHelper stackHelper) {
        this.recipe = recipe; this.stackHelper = stackHelper;
    }

    public static IRecipeWrapper create(NtmCraftingRecipe recipe, IStackHelper stackHelper) {
        if(recipe instanceof IShapedRecipe) return new Shaped(recipe, stackHelper, (IShapedRecipe) recipe);
        return new NtmCraftingRecipeWrapper(recipe, stackHelper);
    }

    @Override public void getIngredients(IIngredients ingredients) {
        List<List<ItemStack>> inputs = new ArrayList<>();
        for(Ingredient ingredient : recipe.getIngredients()) {
            ItemStack[] matching = ingredient.getMatchingStacks();
            if(matching.length == 0) inputs.add(Collections.<ItemStack>emptyList());
            else inputs.add(stackHelper.getAllSubtypes(Arrays.asList(matching)));
        }
        ingredients.setInputLists(VanillaTypes.ITEM, inputs);
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
    }

    @Override public ResourceLocation getRegistryName() { return recipe.getRegistryName(); }

    private static final class Shaped extends NtmCraftingRecipeWrapper implements IShapedCraftingRecipeWrapper {
        private final IShapedRecipe shape;
        Shaped(NtmCraftingRecipe recipe, IStackHelper stackHelper, IShapedRecipe shape) { super(recipe, stackHelper); this.shape = shape; }
        @Override public int getWidth() { return shape.getRecipeWidth(); }
        @Override public int getHeight() { return shape.getRecipeHeight(); }
    }
}
