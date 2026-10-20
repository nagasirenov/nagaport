package com.hbm.handler.jei;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;

/** Converts existing NTM recipe-source alternatives to native JEI item lists. */
public final class RecipeItemAlternatives {
    private RecipeItemAlternatives() { }
    public static ItemStack[] from(Object value) {
        List<ItemStack> result = new ArrayList<>();
        collect(value, result);
        return result.toArray(new ItemStack[0]);
    }
    private static void collect(Object value, List<ItemStack> result) {
        if(value == null) return;
        if(value instanceof ItemStack) { result.add(((ItemStack) value).copy()); return; }
        if(value instanceof Iterable<?>) {
            for(Object entry : (Iterable<?>) value) collect(entry, result);
            return;
        }
        if(value instanceof Object[]) {
            for(Object entry : (Object[]) value) collect(entry, result);
            return;
        }
        throw new IllegalArgumentException("Unsupported recipe alternative: " + value.getClass().getName());
    }
}
