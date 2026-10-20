package com.hbm.crafting.nativeapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * NTM's ordinary crafting-table recipes, expressed as genuine 1.12 IRecipes.
 * The matching contract intentionally remains that of the supplied original:
 * item/damage matching ignores NBT and counts, ore alternatives stay live, and
 * shapeless matching consumes the first matching requirement, without backtracking.
 * This is a domain recipe, not a replacement for Forge registration APIs.
 */
public class NtmCraftingRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
    private final ItemStack output;
    private final NonNullList<Ingredient> ingredients;
    private final boolean shaped;
    private final int width;
    private final int height;

    private NtmCraftingRecipe(ItemStack output, NonNullList<Ingredient> ingredients, boolean shaped, int width, int height, boolean copyOutput) {
        if (output.isEmpty()) throw new IllegalArgumentException("NTM recipe output is empty; create recipes after item registration");
        this.output = copyOutput ? output.copy() : output; this.ingredients = ingredients;
        this.shaped = shaped; this.width = width; this.height = height;
    }

    public static NtmCraftingRecipe shaped(ItemStack output, boolean ore, Object... definition) {
        int cursor = 0, width = 0;
        List<String> rows = new ArrayList<>();
        if (definition[0] instanceof String[]) {
            for (String row : (String[]) definition[cursor++]) rows.add(row);
        } else {
            while (cursor < definition.length && definition[cursor] instanceof String) rows.add((String) definition[cursor++]);
        }
        if (rows.isEmpty()) throw new IllegalArgumentException("Missing NTM recipe pattern");
        width = rows.get(0).length();
        for (String row : rows) if (row.length() != width) throw new IllegalArgumentException("Nonrectangular NTM recipe pattern");
        Map<Character, Ingredient> keys = new HashMap<>();
        while (cursor < definition.length) {
            char key = (Character) definition[cursor++];
            keys.put(key, ingredient(definition[cursor++], ore, true));
        }
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (String row : rows) for (int x = 0; x < width; x++) {
            Ingredient value = keys.get(row.charAt(x));
            ingredients.add(value == null ? Ingredient.EMPTY : value);
        }
        return new Shaped(output, ingredients, width, rows.size(), ore);
    }

    private static final class Shaped extends NtmCraftingRecipe implements net.minecraftforge.common.crafting.IShapedRecipe {
        private final int recipeWidth, recipeHeight;
        Shaped(ItemStack output, NonNullList<Ingredient> ingredients, int width, int height, boolean ore) {
            super(output, ingredients, true, width, height, ore); recipeWidth = width; recipeHeight = height;
        }
        @Override public int getRecipeWidth() { return recipeWidth; }
        @Override public int getRecipeHeight() { return recipeHeight; }
    }

    public static NtmCraftingRecipe shapeless(ItemStack output, boolean ore, Object... definition) {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (Object value : definition) ingredients.add(ingredient(value, ore, false));
        return new NtmCraftingRecipe(output, ingredients, false, 0, 0, ore);
    }

    private static Ingredient ingredient(Object value, boolean ore, boolean shaped) {
        if (ore && value instanceof String) return new LiveOreIngredient((String) value);
        ItemStack stack;
        if (value instanceof ItemStack) stack = ((ItemStack) value).copy();
        else if (value instanceof Item) stack = new ItemStack((Item) value);
        else if (value instanceof Block) stack = new ItemStack((Block) value, 1, shaped ? OreDictionary.WILDCARD_VALUE : 0);
        else throw new IllegalArgumentException("Unsupported NTM crafting ingredient: " + value);
        if (stack.isEmpty()) throw new IllegalArgumentException("NTM crafting ingredient is empty; create recipes after item registration");
        return new StackIngredient(stack);
    }

    private static final class StackIngredient extends Ingredient {
        private final ItemStack stack;
        StackIngredient(ItemStack stack) { super(stack); this.stack = stack; }
        @Override public boolean apply(ItemStack input) { return input != null && !input.isEmpty() && OreDictionary.itemMatches(stack, input, false); }
        @Override public boolean isSimple() { return false; }
    }

    private static final class LiveOreIngredient extends OreIngredient {
        private final List<ItemStack> alternatives;
        LiveOreIngredient(String name) { super(name); alternatives = OreDictionary.getOres(name); }
        @Override public boolean apply(ItemStack input) {
            if (input == null || input.isEmpty()) return false;
            for (ItemStack candidate : alternatives) if (OreDictionary.itemMatches(candidate, input, false)) return true;
            return false;
        }
        @Override public boolean isSimple() { return false; }
    }

    @Override public boolean matches(InventoryCrafting inventory, World world) {
        if (shaped) {
            for (int x = 0; x <= inventory.getWidth() - width; x++)
                for (int y = 0; y <= inventory.getHeight() - height; y++)
                    if (matchesAt(inventory, x, y, false) || matchesAt(inventory, x, y, true)) return true;
            return false;
        }
        List<Ingredient> required = new ArrayList<>(ingredients);
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            int match = -1;
            for (int i = 0; i < required.size(); i++) if (required.get(i).apply(stack)) { match = i; break; }
            if (match < 0) return false;
            required.remove(match);
        }
        return required.isEmpty();
    }

    private boolean matchesAt(InventoryCrafting inventory, int startX, int startY, boolean mirror) {
        for (int x = 0; x < inventory.getWidth(); x++) for (int y = 0; y < inventory.getHeight(); y++) {
            int localX = x - startX, localY = y - startY;
            Ingredient expected = Ingredient.EMPTY;
            if (localX >= 0 && localY >= 0 && localX < width && localY < height)
                expected = ingredients.get((mirror ? width - localX - 1 : localX) + localY * width);
            if (!expected.apply(inventory.getStackInRowAndColumn(x, y))) return false;
        }
        return true;
    }

    public boolean isShaped() { return shaped; }
    public int inputSize() { return ingredients.size(); }
    @Override public boolean canFit(int width, int height) { return shaped ? width >= this.width && height >= this.height : width * height >= ingredients.size(); }
    @Override public ItemStack getCraftingResult(InventoryCrafting inventory) { return output.copy(); }
    @Override public ItemStack getRecipeOutput() { return output; }
    @Override public NonNullList<Ingredient> getIngredients() { return ingredients; }
    @Override public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inventory) { return ForgeHooks.defaultRecipeGetRemainingItems(inventory); }
}
