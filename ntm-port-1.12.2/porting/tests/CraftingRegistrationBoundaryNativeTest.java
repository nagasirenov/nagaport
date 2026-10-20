import com.hbm.registry.GameRegistry;
import com.hbm.crafting.nativeapi.NtmCraftingRecipe;
import com.hbm.crafting.nativeapi.NtmCraftingRegistry;
import com.hbm.crafting.nativeapi.NtmCraftingTables;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.registries.GameData;

/** Boundary probe, NOT a completed port or full Loader launch. No platform doubles. */
public final class CraftingRegistrationBoundaryNativeTest {
    private static int checks;
    private static Block block;
    private static IRecipe registered;
    private static int recipeEvents;
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
        checks++;
        System.out.println("PASS " + message);
    }
    private static InventoryCrafting grid(ItemStack... stacks) {
        InventoryCrafting grid = new InventoryCrafting(new Container() {
            public boolean canInteractWith(EntityPlayer player) { return true; }
        }, 3, 3);
        for (int i = 0; i < stacks.length; i++) grid.setInventorySlotContents(i, stacks[i]);
        return grid;
    }
    public static final class RecipeEvents {
        @SubscribeEvent public void recipes(RegistryEvent.Register<IRecipe> event) {
            recipeEvents++;
            check(Item.getItemFromBlock(block) != Items.AIR, "recipe event sees registered block item");
            registered = new ShapedOreRecipe(null, new ItemStack(block, 4, 2), "##", '#', Items.IRON_INGOT);
            registered.setRegistryName(new ResourceLocation("hbm", "boundary/shaped"));
            event.getRegistry().register(registered);
        }
    }
    public static void main(String[] args) {
        Bootstrap.register();
        ModMetadata metadata = new ModMetadata(); metadata.modId = "hbm"; metadata.name = "Crafting boundary";
        DummyModContainer owner = new DummyModContainer(metadata);
        Loader.instance().setupTestHarness(owner); Loader.instance().setActiveModContainer(owner);
        block = new Block(Material.IRON);
        GameRegistry.beginRegistrationTables();
        GameRegistry.registerBlock(block, "boundary_block");
        GameRegistry.finishRegistrationTables();
        ItemStack prematureOutput = new ItemStack(block, 4, 2);
        check(prematureOutput.isEmpty(), "preInit construction loses queued block output");
        MinecraftForge.EVENT_BUS.register(new RecipeEvents());
        final java.util.List<String> producerOrder = new java.util.ArrayList<>();
        final IRecipe[] produced = new IRecipe[2];
        NtmCraftingTables.schedule(() -> {
            producerOrder.add("recipes");
            if (args.length > 0) NtmCraftingRegistry.shaped("boundary/production", false, new ItemStack(Items.GOLD_INGOT), "#", '#', Items.APPLE);
            produced[0] = NtmCraftingRegistry.shaped("boundary/production", false, new ItemStack(block, 6, 3), "#", '#', Items.GOLD_INGOT);
            produced[1] = NtmCraftingRegistry.shapeless("boundary/live", true, new ItemStack(Items.APPLE), "boundaryLateOre");
        });
        NtmCraftingTables.schedule(() -> { producerOrder.add("ores"); OreDictionary.registerOre("boundaryLateOre", Items.DIAMOND); });
        // These are the same native dispatchers called by Loader / CraftingHelper.
        // Their relative ordering is checked separately against pinned source.
        GameData.fireRegistryEvents(id -> !id.equals(GameData.RECIPES));
        check(recipeEvents == 0, "non-recipe registry phase does not flush recipes");
        check(!new ItemStack(block).isEmpty(), "item registry establishes block mapping");
        check(prematureOutput.isEmpty(), "flushing item registry does not repair old empty recipe output");
        GameData.fireRegistryEvents(id -> id.equals(GameData.RECIPES));
        check(producerOrder.equals(java.util.Arrays.asList("recipes", "ores")), "production deferred producers retain crafting-before-ores order");
        IRecipe actual = ForgeRegistries.RECIPES.getValue(produced[0].getRegistryName());
        System.out.println("STABLE_ID=" + actual.getRegistryName());
        check(actual != null && actual.getRecipeOutput().getCount() == 6 && actual.getRecipeOutput().getMetadata() == 3,
                "production lazy table builds real block output only after item registration");
        IRecipe live = ForgeRegistries.RECIPES.getValue(produced[1].getRegistryName());
        check(live.matches(grid(new ItemStack(Items.DIAMOND)), null), "production ingredient sees ore registered after recipe construction");
        NtmCraftingRegistry.shaped("boundary/production", false, new ItemStack(Items.APPLE), "#", '#', Items.GOLD_INGOT);
        IRecipe duplicate = NtmCraftingRegistry.shaped("boundary/production", false, new ItemStack(block, 6, 3), "#", '#', Items.GOLD_INGOT);
        String firstKey = actual.getRegistryName().toString();
        check(duplicate.getRegistryName().toString().equals(firstKey.substring(0, firstKey.length() - 1) + "1"), "exact duplicate retained with duplicate-only ordinal, unrelated recipe does not shift definition key");
        check(recipeEvents == 1, "native filtered registry dispatch delivers recipe event once");
        check(ForgeRegistries.RECIPES.getValue(registered.getRegistryName()) == registered, "real IRecipe registry stores event recipe");
        InventoryCrafting shaped = grid(new ItemStack(Items.IRON_INGOT), new ItemStack(Items.IRON_INGOT));
        check(registered.matches(shaped, null), "shaped two-input recipe matches");
        ItemStack result = registered.getCraftingResult(shaped);
        check(result.getCount() == 4 && result.getMetadata() == 2, "output count and metadata retained");
        result.setCount(1);
        check(registered.getCraftingResult(shaped).getCount() == 4, "craft result is independent output copy");
        check(!registered.matches(grid(new ItemStack(Items.IRON_INGOT)), null), "shaped missing ingredient rejected");
        ShapedOreRecipe meta = new ShapedOreRecipe(null, new ItemStack(Items.APPLE), "#", '#', new ItemStack(Items.DYE, 1, 2));
        check(meta.matches(grid(new ItemStack(Items.DYE, 64, 2)), null), "ingredient count ignored, exact metadata accepted");
        check(!meta.matches(grid(new ItemStack(Items.DYE, 1, 3)), null), "different metadata rejected");
        ShapedOreRecipe wild = new ShapedOreRecipe(null, new ItemStack(Items.APPLE), "#", '#', new ItemStack(Items.DYE, 1, OreDictionary.WILDCARD_VALUE));
        check(wild.matches(grid(new ItemStack(Items.DYE, 1, 3)), null), "explicit wildcard matches metadata");
        ItemStack tagged = new ItemStack(Items.DYE, 1, 2); NBTTagCompound tag = new NBTTagCompound(); tag.setString("key", "value"); tagged.setTagCompound(tag);
        ShapedOreRecipe nbt = new ShapedOreRecipe(null, tagged, "#", '#', tagged);
        check(nbt.matches(grid(new ItemStack(Items.DYE, 1, 2)), null), "ordinary ore recipe ingredient ignores NBT");
        check(tag.equals(nbt.getCraftingResult(grid()).getTagCompound()), "output NBT retained");
        ShapelessOreRecipe blockInput = new ShapelessOreRecipe(null, new ItemStack(Items.APPLE), Blocks.WOOL);
        check(blockInput.matches(grid(new ItemStack(Blocks.WOOL, 1, 7)), null), "native bare Block shapeless input is wildcard (legacy was meta zero)");
        OreDictionary.registerOre("boundaryEither", new ItemStack(Items.IRON_INGOT));
        OreDictionary.registerOre("boundaryEither", new ItemStack(Items.GOLD_INGOT));
        ShapelessOreRecipe overlap = new ShapelessOreRecipe(null, new ItemStack(Items.APPLE), "boundaryEither", Items.IRON_INGOT);
        check(overlap.matches(grid(new ItemStack(Items.IRON_INGOT), new ItemStack(Items.GOLD_INGOT)), null), "native overlapping ore shapeless matching backtracks (legacy greedy rejects this order)");
        check(overlap.matches(grid(new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.IRON_INGOT)), null), "ore shapeless reversed grid also matches");
        ShapelessOreRecipe milk = new ShapelessOreRecipe(null, new ItemStack(Items.APPLE), Items.MILK_BUCKET);
        check(milk.getRemainingItems(grid(new ItemStack(Items.MILK_BUCKET))).get(0).getItem() == Items.BUCKET, "native remaining item retains milk bucket container");
        NtmCraftingRecipe legacyBlock = NtmCraftingRecipe.shapeless(new ItemStack(Items.APPLE), true, Blocks.WOOL);
        check(!legacyBlock.matches(grid(new ItemStack(Blocks.WOOL, 1, 7)), null), "production preserves original shapeless bare Block metadata zero");
        check(legacyBlock.matches(grid(new ItemStack(Blocks.WOOL, 1, 0)), null), "production bare Block zero still matches");
        NtmCraftingRecipe legacyOverlap = NtmCraftingRecipe.shapeless(new ItemStack(Items.APPLE), true, "boundaryEither", Items.IRON_INGOT);
        check(!legacyOverlap.matches(grid(new ItemStack(Items.IRON_INGOT), new ItemStack(Items.GOLD_INGOT)), null), "production preserves original greedy rejection without backtracking");
        check(legacyOverlap.matches(grid(new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.IRON_INGOT)), null), "production greedy reversed grid matches original");
        NtmCraftingRecipe normal = NtmCraftingRecipe.shaped(tagged, false, "AB", 'A', new ItemStack(Items.DYE, 1, 2), 'B', Items.IRON_INGOT);
        check(normal.matches(grid(new ItemStack(Items.IRON_INGOT), new ItemStack(Items.DYE, 20, 2)), null), "production vanilla shaped mirrors and ignores counts and NBT");
        check(!normal.matches(grid(new ItemStack(Items.IRON_INGOT), new ItemStack(Items.DYE, 20, 3)), null), "production vanilla shaped rejects metadata mismatch");
        check(normal instanceof net.minecraftforge.common.crafting.IShapedRecipe, "native shaped dimensions exposed to recipe viewers");
        check(tag.equals(normal.getCraftingResult(grid()).getTagCompound()), "production output preserves NBT");
        NtmCraftingRecipe wildcard = NtmCraftingRecipe.shaped(new ItemStack(Items.APPLE), false, "#", '#', Blocks.WOOL);
        check(wildcard.matches(grid(new ItemStack(Blocks.WOOL, 1, 7)), null), "production shaped bare Block retains original wildcard matching");
        NtmCraftingRecipe container = NtmCraftingRecipe.shapeless(new ItemStack(Items.APPLE), false, Items.MILK_BUCKET);
        check(container.getRemainingItems(grid(new ItemStack(Items.MILK_BUCKET))).get(0).getItem() == Items.BUCKET, "production uses real native remaining-container handling");
        // Generated from the byte-conserved real producer; only its NTM block identity is substituted.
        NtmCraftingRecipe originalPaper = OriginalCraftingFixture.vinylPaper(block);
        net.minecraftforge.common.crafting.IShapedRecipe paperShape = (net.minecraftforge.common.crafting.IShapedRecipe) originalPaper;
        check(paperShape.getRecipeWidth() == 2 && paperShape.getRecipeHeight() == 1, "exact original vinyl-paper producer has two inputs in ONE row");
        InventoryCrafting twoPlanks = grid(new ItemStack(block), new ItemStack(block));
        InventoryCrafting fourPlanks = grid(new ItemStack(block), new ItemStack(block), ItemStack.EMPTY, new ItemStack(block), new ItemStack(block));
        check(originalPaper.matches(twoPlanks, null) && !originalPaper.matches(fourPlanks, null), "exact original paper shape rejects invented two-row fixture");
        OreDictionary.registerOre("plankWood", block);
        IRecipe genuineTable = ForgeRegistries.RECIPES.getValue(new ResourceLocation("minecraft", "crafting_table"));
        check(genuineTable != null && genuineTable.matches(fourPlanks, null) && !genuineTable.matches(twoPlanks, null), "genuine native vanilla workbench does NOT overlap exact original paper shape");
        for (boolean isShaped : new boolean[] {true, false}) for (boolean isOre : new boolean[] {false, true}) {
            ItemStack originalOutput = new ItemStack(Items.APPLE, 2, 3);
            NBTTagCompound originalTag = new NBTTagCompound(); originalTag.setString("value", "before"); originalOutput.setTagCompound(originalTag);
            NtmCraftingRecipe alias = isShaped ? NtmCraftingRecipe.shaped(originalOutput, isOre, "#", '#', Items.IRON_INGOT)
                    : NtmCraftingRecipe.shapeless(originalOutput, isOre, Items.IRON_INGOT);
            check((alias.getRecipeOutput() == originalOutput) == !isOre, "family output identity shaped=" + isShaped + " ore=" + isOre);
            originalOutput.setCount(7); originalTag.setString("value", "after");
            check(alias.getRecipeOutput().getCount() == (isOre ? 2 : 7) && alias.getRecipeOutput().getTagCompound().getString("value").equals(isOre ? "before" : "after"), "family constructor copy/reference retains original count/NBT mutation semantics");
            alias.getRecipeOutput().setCount(5); alias.getRecipeOutput().getTagCompound().setString("value", "stored");
            ItemStack craftCopy = alias.getCraftingResult(grid());
            check(craftCopy != alias.getRecipeOutput() && craftCopy.getTagCompound() != alias.getRecipeOutput().getTagCompound() && craftCopy.getCount() == 5 && craftCopy.getTagCompound().getString("value").equals("stored"), "all families copy current stored output at craft time");
            craftCopy.setCount(1); craftCopy.getTagCompound().setString("value", "isolated");
            check(alias.getRecipeOutput().getCount() == 5 && alias.getRecipeOutput().getTagCompound().getString("value").equals("stored"), "craft result count/NBT mutations cannot mutate stored output");
        }
        ItemStack canonicalA = new ItemStack(Items.APPLE), canonicalB = new ItemStack(Items.APPLE);
        NBTTagCompound tagA = new NBTTagCompound(), tagB = new NBTTagCompound();
        tagA.setString("Aa", "first"); tagA.setString("BB", "second");
        tagB.setString("BB", "second"); tagB.setString("Aa", "first");
        NBTTagCompound nestedA = new NBTTagCompound(), nestedB = new NBTTagCompound();
        nestedA.setInteger("Aa", 1); nestedA.setInteger("BB", 2); nestedB.setInteger("BB", 2); nestedB.setInteger("Aa", 1);
        tagA.setTag("nested", nestedA); tagB.setTag("nested", nestedB);
        for (NBTTagCompound compound : new NBTTagCompound[] {tagA, tagB}) {
            compound.setByteArray("bytes", new byte[] {1, -2, 3}); compound.setIntArray("ints", new int[] {1, 1000000});
            compound.setTag("longs", new net.minecraft.nbt.NBTTagLongArray(new long[] {1L, Long.MAX_VALUE}));
            net.minecraft.nbt.NBTTagList list = new net.minecraft.nbt.NBTTagList();
            list.appendTag(new net.minecraft.nbt.NBTTagString("a")); list.appendTag(new net.minecraft.nbt.NBTTagString("b")); compound.setTag("list", list);
        }
        canonicalA.setTagCompound(tagA); canonicalB.setTagCompound(tagB);
        IRecipe orderedA = NtmCraftingRegistry.shapeless("boundary/nbt", false, canonicalA, Items.IRON_INGOT);
        IRecipe orderedB = NtmCraftingRegistry.shapeless("boundary/nbt", false, canonicalB, Items.IRON_INGOT);
        String canonicalKey = orderedA.getRegistryName().toString();
        check(orderedB.getRegistryName().toString().equals(canonicalKey.substring(0, canonicalKey.length() - 1) + "1"), "canonical NBT identity sorts nested compound keys, including colliding HashMap keys");
        canonicalB.getTagCompound().setInteger("distinct", 1);
        IRecipe changedNbt = NtmCraftingRegistry.shapeless("boundary/nbt", false, canonicalB, Items.IRON_INGOT);
        check(!changedNbt.getRegistryName().equals(orderedA.getRegistryName()) && changedNbt.getRegistryName().toString().endsWith("/0"), "changed NBT receives a distinct definition identity");
        java.util.Set<ResourceLocation> distinctDefinitions = new java.util.HashSet<>();
        distinctDefinitions.add(NtmCraftingRegistry.shapeless("boundary/distinct", false, new ItemStack(Items.APPLE, 1, 0), Items.IRON_INGOT).getRegistryName());
        distinctDefinitions.add(NtmCraftingRegistry.shapeless("boundary/distinct", false, new ItemStack(Items.APPLE, 2, 0), Items.IRON_INGOT).getRegistryName());
        distinctDefinitions.add(NtmCraftingRegistry.shapeless("boundary/distinct", false, new ItemStack(Items.APPLE, 1, 1), Items.IRON_INGOT).getRegistryName());
        distinctDefinitions.add(NtmCraftingRegistry.shapeless("boundary/distinct", true, new ItemStack(Items.APPLE), "boundaryEither").getRegistryName());
        distinctDefinitions.add(NtmCraftingRegistry.shapeless("boundary/distinct", true, new ItemStack(Items.APPLE), "boundaryLateOre").getRegistryName());
        distinctDefinitions.add(NtmCraftingRegistry.shaped("boundary/distinct", false, new ItemStack(Items.APPLE), "#", '#', Items.IRON_INGOT).getRegistryName());
        check(distinctDefinitions.size() == 6 && distinctDefinitions.stream().allMatch(id -> id.toString().endsWith("/0")), "output count/metadata, ore name and recipe shape each affect definition identity without global ordinals");
        System.out.println("PASS " + checks + " native boundary assertions; full producer registration remains BLOCKED, no gameplay claim");
    }
}
