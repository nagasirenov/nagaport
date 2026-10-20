package com.hbm.lib;

import java.util.Random;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Mod-local replacement for the removed Minecraft 1.7.10 loot entry.
 * Existing NTM pool definitions and their serialized min/max/weight keep their
 * meaning; this is not a replacement for the vanilla 1.12 loot table registry.
 */
public class WeightedRandomChestContent extends LegacyWeightedLoot<ItemStack> {

    public ItemStack theItemId;

    public WeightedRandomChestContent(Item item, int metadata, int min, int max, int weight) {
        this(new ItemStack(item, 1, metadata), min, max, weight);
    }

    public WeightedRandomChestContent(ItemStack stack, int min, int max, int weight) {
        super(min, max, weight);
        this.theItemId = stack;
    }

    @Override
    protected ItemStack copyStack(int count) {
        // Forge's copy retains the raw item even if the template count is zero.
        // The pool's min/max, not that template count, determines the output.
        ItemStack stack = theItemId.copy();
        stack.setCount(count);
        return stack.isEmpty() ? ItemStack.EMPTY : stack;
    }

    @Override
    protected int maximumStackSize() {
        return normalizedPrototype().getMaxStackSize();
    }

    @Override
    protected boolean hasItem() {
        return !normalizedPrototype().isEmpty();
    }

    private ItemStack normalizedPrototype() {
        ItemStack stack = theItemId.copy();
        stack.setCount(1);
        return stack;
    }

    public static void generateChestContents(Random random, WeightedRandomChestContent[] pool, final IInventory inventory, int rolls) {
        LegacyWeightedLoot.generateChestContents(random, pool, new Inventory<ItemStack>() {
            @Override public int size() { return inventory.getSizeInventory(); }
            @Override public void set(int slot, ItemStack stack) { inventory.setInventorySlotContents(slot, stack); }
        }, rolls);
    }
}
