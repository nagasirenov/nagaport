package com.hbm.handler.jei;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Focus predicates used by NTM's recipe categories. These preserve the pinned
 * native NEI 2.4.3.245 comparison behavior without requiring NEI at runtime.
 * Counts do not participate, and empty stacks deliberately receive no extra
 * rejection: native ItemStack item, metadata and tag behavior remains decisive.
 */
public final class RecipeStackMatcher {
    private RecipeStackMatcher() {}

    /** Compare item, subtype metadata (when applicable), and native stack tags. */
    public static boolean matchesType(ItemStack recipe, ItemStack focus) {
        return recipe != null && focus != null
                && recipe.getItem() == focus.getItem()
                && (!focus.getHasSubtypes() || focus.getItemDamage() == recipe.getItemDamage())
                && ItemStack.areItemStackTagsEqual(focus, recipe);
    }

    /**
     * Crafting focus ignores tags and accepts either wildcard or any damage
     * value on a damageable item, even when that item also has subtypes.
     */
    public static boolean matchesCrafting(ItemStack recipe, ItemStack focus) {
        return recipe != null && focus != null
                && recipe.getItem() == focus.getItem()
                && (recipe.getItemDamage() == focus.getItemDamage()
                    || recipe.getItemDamage() == OreDictionary.WILDCARD_VALUE
                    || focus.getItemDamage() == OreDictionary.WILDCARD_VALUE
                    || recipe.getItem().isDamageable());
    }
}
