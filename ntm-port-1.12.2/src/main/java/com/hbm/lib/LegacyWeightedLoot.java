package com.hbm.lib;

import java.util.Random;

/**
 * The platform-independent part of the 1.7.10 weighted chest loot contract.
 * Slot collisions deliberately replace previous contents. Counts larger than a
 * stack's limit deliberately produce single-item stacks, rather than full stacks.
 */
public abstract class LegacyWeightedLoot<T> {

    public int itemWeight;
    public int theMinimumChanceToGenerateItem;
    public int theMaximumChanceToGenerateItem;

    protected LegacyWeightedLoot(int min, int max, int weight) {
        this.theMinimumChanceToGenerateItem = min;
        this.theMaximumChanceToGenerateItem = max;
        this.itemWeight = weight;
    }

    public interface Inventory<T> {
        int size();
        void set(int slot, T stack);
    }

    protected abstract T copyStack(int count);
    protected abstract int maximumStackSize();
    protected abstract boolean hasItem();

    public T generateStack(Random random) {
        return copyStack(randomCount(random));
    }

    private int randomCount(Random random) {
        long range = (long) theMaximumChanceToGenerateItem - theMinimumChanceToGenerateItem + 1;
        if (theMinimumChanceToGenerateItem < 0 || range <= 0 || range > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Invalid loot count range: " + theMinimumChanceToGenerateItem + ".." + theMaximumChanceToGenerateItem);
        }
        return theMinimumChanceToGenerateItem + random.nextInt((int) range);
    }

    public static <E extends LegacyWeightedLoot<?>> E getRandomItem(Random random, E[] pool) {
        long total = 0;
        for (E entry : pool) {
            if (entry.itemWeight < 0) throw new IllegalArgumentException("Negative loot weight");
            total += entry.itemWeight;
        }
        if (total <= 0 || total > Integer.MAX_VALUE) throw new IllegalArgumentException("Invalid total loot weight: " + total);
        int selected = random.nextInt((int) total);
        for (E entry : pool) {
            selected -= entry.itemWeight;
            if (selected < 0) return entry;
        }
        throw new IllegalStateException("Loot weights changed during selection");
    }

    public static <T, E extends LegacyWeightedLoot<T>> void generateChestContents(Random random, E[] pool, Inventory<T> inventory, int rolls) {
        for (int roll = 0; roll < rolls; roll++) {
            LegacyWeightedLoot<T> entry = getRandomItem(random, pool);
            int count = entry.randomCount(random);
            if (!entry.hasItem()) continue;
            int stacks = count > entry.maximumStackSize() ? count : 1;
            int stackSize = stacks == 1 ? count : 1;
            for (int stack = 0; stack < stacks; stack++) {
                T generated = entry.copyStack(stackSize);
                inventory.set(random.nextInt(inventory.size()), generated);
            }
        }
    }
}
