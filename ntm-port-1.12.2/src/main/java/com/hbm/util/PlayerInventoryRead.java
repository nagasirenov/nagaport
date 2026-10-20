package com.hbm.util;

import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

/** Indexed player-inventory boundary for NTM consumers that deliberately use null. */
public final class PlayerInventoryRead {
    private PlayerInventoryRead() { }

    /**
     * Pass the already-evaluated mainInventory or armorInventory list, not its owner:
     * Java must read that field before evaluating a possibly side-effecting index.
     * Only the native canonical vacancy sentinel becomes null. Every other stack,
     * including zero-count, invalid-item or tagged empty objects, keeps its identity.
     * This does not restore legacy ItemStack.getItem behavior for malformed stacks.
     * No copy, mutation, bounds fallback, or whole-inventory view is introduced.
     */
    @Nullable
    public static ItemStack get(NonNullList<ItemStack> inventory, int slot) {
        ItemStack stack = inventory.get(slot);
        return stack == ItemStack.EMPTY ? null : stack;
    }
}
