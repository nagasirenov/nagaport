import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.hbm.lib.LegacyWeightedLoot;

/** Run against the production algorithm without fake Minecraft classes. */
public final class LegacyWeightedLootTest {
    private static final class Entry extends LegacyWeightedLoot<String> {
        final String name;
        final int limit;
        final boolean present;
        Entry(String name, int min, int max, int weight, int limit) {
            this(name, min, max, weight, limit, true);
        }
        Entry(String name, int min, int max, int weight, int limit, boolean present) {
            super(min, max, weight);
            this.name = name;
            this.limit = limit;
            this.present = present;
        }
        @Override protected String copyStack(int count) { return name + ":" + count; }
        @Override protected int maximumStackSize() { return limit; }
        @Override protected boolean hasItem() { return present; }
    }

    private static final class Inventory implements LegacyWeightedLoot.Inventory<String> {
        final String[] slots;
        final List<String> writes = new ArrayList<>();
        Inventory(int size) { slots = new String[size]; }
        @Override public int size() { return slots.length; }
        @Override public void set(int slot, String stack) {
            writes.add(slot + "=" + stack);
            slots[slot] = stack;
        }
    }

    private static Random values(final int... values) {
        return new Random(0) {
            int index;
            @Override public int nextInt(int bound) {
                if (index >= values.length) throw new AssertionError("Unexpected random draw, bound=" + bound);
                int result = values[index++];
                if (result < 0 || result >= bound) throw new AssertionError("Invalid scripted draw " + result + "/" + bound);
                return result;
            }
        };
    }

    private static void equal(Object expected, Object actual) {
        if (!expected.equals(actual)) throw new AssertionError("Expected " + expected + ", got " + actual);
    }

    private static void rejects(Runnable task) {
        try { task.run(); } catch (IllegalArgumentException expected) { return; }
        throw new AssertionError("Expected invalid loot data to be rejected");
    }

    // Independent oracle for Forge 1.7.10's weighted selection and
    // ChestGenHooks.generateStacks contract. No Minecraft API stubs are used.
    private static void reference(Random random, Entry[] pool, Inventory inventory, int rolls) {
        int total = 0;
        for (Entry entry : pool) total += entry.itemWeight;
        for (int roll = 0; roll < rolls; roll++) {
            int ticket = random.nextInt(total);
            Entry entry = null;
            int accumulated = 0;
            for (Entry candidate : pool) {
                accumulated += candidate.itemWeight;
                if (ticket < accumulated) { entry = candidate; break; }
            }
            int count = entry.theMinimumChanceToGenerateItem + random.nextInt(entry.theMaximumChanceToGenerateItem - entry.theMinimumChanceToGenerateItem + 1);
            if (!entry.present) continue;
            if (count <= entry.limit) {
                inventory.set(random.nextInt(inventory.size()), entry.name + ":" + count);
            } else {
                for (int item = 0; item < count; item++) {
                    inventory.set(random.nextInt(inventory.size()), entry.name + ":1");
                }
            }
        }
    }

    public static void main(String[] args) {
        Entry a = new Entry("a", 2, 5, 1, 64);
        Entry zero = new Entry("zero", 1, 1, 0, 64);
        Entry b = new Entry("b", 1, 1, 3, 64);
        Entry[] weights = {zero, a, b};
        equal(a, LegacyWeightedLoot.getRandomItem(values(0), weights));
        for (int ticket = 1; ticket < 4; ticket++) equal(b, LegacyWeightedLoot.getRandomItem(values(ticket), weights));
        equal("a:2", a.generateStack(values(0)));
        equal("a:5", a.generateStack(values(3)));
        equal("fixed:1", new Entry("fixed", 1, 1, 1, 1).generateStack(values(0)));

        Inventory split = new Inventory(2);
        LegacyWeightedLoot.generateChestContents(values(0, 0, 0, 1, 0), new Entry[] {new Entry("tool", 3, 3, 1, 1)}, split, 1);
        equal(Arrays.asList("0=tool:1", "1=tool:1", "0=tool:1"), split.writes);
        equal("tool:1", split.slots[0]); // collisions replace; no merge/retry

        Inventory zeroCount = new Inventory(1);
        LegacyWeightedLoot.generateChestContents(values(0, 0, 0), new Entry[] {new Entry("empty", 0, 0, 1, 64)}, zeroCount, 1);
        equal(Arrays.asList("0=empty:0"), zeroCount.writes);
        Inventory absent = new Inventory(1);
        LegacyWeightedLoot.generateChestContents(values(0, 0), new Entry[] {new Entry("missing", 1, 1, 1, 64, false)}, absent, 1);
        equal(0, absent.writes.size());

        Entry[] mixed = {zero, a, b, new Entry("oversize", 60, 68, 2, 64), new Entry("unstackable", 1, 4, 1, 1)};
        for (int seed = 0; seed < 1000; seed++) {
            Inventory actual = new Inventory(27);
            Inventory expected = new Inventory(27);
            LegacyWeightedLoot.generateChestContents(new Random(seed), mixed, actual, 20);
            reference(new Random(seed), mixed, expected, 20);
            equal(expected.writes, actual.writes);
            equal(Arrays.asList(expected.slots), Arrays.asList(actual.slots));
        }
        rejects(() -> LegacyWeightedLoot.getRandomItem(new Random(0), new Entry[0]));
        rejects(() -> LegacyWeightedLoot.getRandomItem(new Random(0), new Entry[] {zero}));
        rejects(() -> LegacyWeightedLoot.getRandomItem(new Random(0), new Entry[] {new Entry("negative", 1, 1, -1, 64)}));
        rejects(() -> LegacyWeightedLoot.getRandomItem(new Random(0), new Entry[] {new Entry("huge", 1, 1, Integer.MAX_VALUE, 64), b}));
        rejects(() -> new Entry("inverted", 5, 2, 1, 64).generateStack(new Random(0)));
        System.out.println("PASS: weighted boundaries, inclusive/fixed/zero counts, stack splitting, overwrite collisions, absent items, invalid inputs, 1000 seeded inventories (20000 rolls)");
    }
}
