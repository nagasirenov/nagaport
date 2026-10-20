import java.util.ArrayList;
import java.util.List;

import codechicken.nei.util.NEIServerUtils;
import com.hbm.handler.jei.RecipeStackMatcher;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

/** Differential oracle is the actual pinned native NEI binary, not a copied predicate. */
public final class RecipeStackMatcherNativeTest {
    private static int checks;

    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }

    private static ItemStack stack(Item item, int count, int damage, int tagKind) {
        ItemStack stack = new ItemStack(item, count, damage);
        if (tagKind != 0) {
            NBTTagCompound tag = new NBTTagCompound();
            if (tagKind > 1) tag.setString("focus", tagKind == 2 ? "alpha" : "beta");
            stack.setTagCompound(tag);
        }
        return stack;
    }

    private static String snapshot(ItemStack stack) {
        return stack == null ? "null" : System.identityHashCode(stack.getItem()) + ":"
                + stack.getCount() + ":" + stack.getItemDamage() + ":" + stack.getTagCompound();
    }

    public static void main(String[] args) {
        Bootstrap.register();
        // Include the uncommon subtype + damageable combination. These are real
        // Minecraft Items, not replacements for ItemStack or other platform APIs.
        Item hybrid = new Item() { @Override public boolean isDamageable() { return true; } }
                .setHasSubtypes(true).setMaxDamage(100);
        Item plain = new Item();
        Item[] items = {Items.APPLE, Items.DYE, Items.IRON_SWORD, hybrid, plain};
        int[] counts = {-1, 0, 1, 64};
        int[] damages = {0, 1, OreDictionary.WILDCARD_VALUE};
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(null);
        stacks.add(ItemStack.EMPTY);
        for (Item item : items)
            for (int count : counts)
                for (int damage : damages)
                    for (int tag = 0; tag < 4; tag++) stacks.add(stack(item, count, damage, tag));
        List<String> before = new ArrayList<>();
        for (ItemStack value : stacks) before.add(snapshot(value));
        for (int a = 0; a < stacks.size(); a++) {
            for (int b = 0; b < stacks.size(); b++) {
                ItemStack recipe = stacks.get(a), focus = stacks.get(b);
                check(RecipeStackMatcher.matchesType(recipe, focus)
                        == NEIServerUtils.areStacksSameType(recipe, focus), "type pair " + a + "," + b);
                check(RecipeStackMatcher.matchesCrafting(recipe, focus)
                        == NEIServerUtils.areStacksSameTypeCrafting(recipe, focus), "crafting pair " + a + "," + b);
            }
        }
        for (int i = 0; i < stacks.size(); i++)
            check(before.get(i).equals(snapshot(stacks.get(i))), "input is unchanged " + i);
        check(!RecipeStackMatcher.matchesType(null, null), "null/null is not a match");
        check(RecipeStackMatcher.matchesType(ItemStack.EMPTY, ItemStack.EMPTY), "EMPTY/EMPTY remains native true");
        check(RecipeStackMatcher.matchesCrafting(ItemStack.EMPTY, ItemStack.EMPTY), "crafting EMPTY/EMPTY remains true");
        check(RecipeStackMatcher.matchesType(stack(plain, 1, 0, 0), stack(plain, 64, 1, 0)),
                "type ignores non-subtype metadata and count");
        check(!RecipeStackMatcher.matchesCrafting(stack(plain, 1, 0, 0), stack(plain, 64, 1, 0)),
                "crafting still checks metadata for nondamageable nonsubtype items");
        check(!RecipeStackMatcher.matchesType(stack(hybrid, 1, 0, 0), stack(hybrid, 1, 1, 0)),
                "type checks subtype metadata even for a damageable item");
        check(RecipeStackMatcher.matchesCrafting(stack(hybrid, 1, 0, 0), stack(hybrid, 1, 1, 0)),
                "crafting ignores damage for a damageable subtype item");
        check(!RecipeStackMatcher.matchesType(stack(Items.DYE, 1, 0, 2), stack(Items.DYE, 64, 0, 3)),
                "type requires equal NBT");
        check(RecipeStackMatcher.matchesCrafting(stack(Items.DYE, 1, 0, 2), stack(Items.DYE, 64, 0, 3)),
                "crafting ignores unequal NBT");
        System.out.println("PASS: " + checks + " native matcher checks; " + stacks.size()
                + " stack variants, both ordered predicates against pinned NEI 2.4.3.245.");
    }
}
