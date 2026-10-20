package com.hbm.handler.jei;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

/**
 * JEI's native extension point for machine-specific recipe discovery. Queries go
 * directly to the category, rather than freezing recipes in JEI's default index.
 */
public final class NativeRecipeLookup implements IRecipeRegistryPlugin {

    private final Map<String, MachineRecipeCategory> categories;
    private final IIngredientHelper<ItemStack> itemHelper;

    public NativeRecipeLookup(Collection<? extends MachineRecipeCategory> categories, IIngredientRegistry ingredients) {
        Objects.requireNonNull(categories, "categories");
        this.itemHelper = Objects.requireNonNull(ingredients, "ingredients").getIngredientHelper(VanillaTypes.ITEM);
        Map<String, MachineRecipeCategory> byUid = new LinkedHashMap<>();
        for (MachineRecipeCategory category : categories) {
            Objects.requireNonNull(category, "category");
            String uid = Objects.requireNonNull(category.getUid(), "category uid");
            if (byUid.put(uid, category) != null) {
                throw new IllegalArgumentException("Duplicate JEI recipe category: " + uid);
            }
        }
        this.categories = Collections.unmodifiableMap(byUid);
    }

    @Override
    public <V> List<String> getRecipeCategoryUids(IFocus<V> focus) {
        if (focus != null && !isItemFocus(focus)) {
            return Collections.emptyList();
        }
        IFocus<ItemStack> itemFocus = asItemFocus(focus);
        List<String> result = new ArrayList<>();
        for (MachineRecipeCategory category : categories.values()) {
            if (!query(category, itemFocus).isEmpty()) {
                result.add(category.getUid());
            }
        }
        return result;
    }

    @Override
    public <T extends IRecipeWrapper, V> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
        MachineRecipeCategory category = findCategory(recipeCategory);
        if (category == null || focus != null && !isItemFocus(focus)) {
            return Collections.emptyList();
        }
        return asWrappers(query(category, asItemFocus(focus)));
    }

    @Override
    public <T extends IRecipeWrapper> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory) {
        MachineRecipeCategory category = findCategory(recipeCategory);
        return category == null ? Collections.emptyList() : asWrappers(category.query(null));
    }

    @Nullable
    private MachineRecipeCategory findCategory(IRecipeCategory<?> recipeCategory) {
        MachineRecipeCategory category = categories.get(recipeCategory.getUid());
        // Identity also establishes the MachineRecipeView type for the generic result.
        return category == recipeCategory ? category : null;
    }

    private List<MachineRecipeView> query(MachineRecipeCategory category, @Nullable IFocus<ItemStack> focus) {
        return category.query(isCatalyst(category, focus) ? null : focus);
    }

    private boolean isCatalyst(MachineRecipeCategory category, @Nullable IFocus<ItemStack> focus) {
        if (focus == null || focus.getMode() != IFocus.Mode.INPUT) {
            return false;
        }
        List<ItemStack> machines = new ArrayList<>();
        for (ItemStack machine : category.getMachines()) {
            if (machine != null && !machine.isEmpty()) {
                machines.add(machine);
            }
        }
        return !machines.isEmpty() && itemHelper.getMatch(machines, focus.getValue()) != null;
    }

    private static boolean isItemFocus(IFocus<?> focus) {
        return focus.getValue() instanceof ItemStack && !((ItemStack) focus.getValue()).isEmpty();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static IFocus<ItemStack> asItemFocus(@Nullable IFocus<?> focus) {
        // All entry points reject non-item values before this cast. IFocus is immutable.
        return (IFocus<ItemStack>) focus;
    }

    @SuppressWarnings("unchecked")
    private static <T extends IRecipeWrapper> List<T> asWrappers(List<MachineRecipeView> recipes) {
        // Called only after matching the exact registered IRecipeCategory<MachineRecipeView>.
        return (List<T>) (List<?>) recipes;
    }
}
