import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.hbm.handler.VanillaLootHandler;
import com.hbm.lib.WeightedRandomChestContent;

import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** An isolated real-Minecraft API check, not a complete mod or in-game test. */
public final class WeightedLootMinecraftTest {
    public static void main(String[] args) throws Exception {
        Bootstrap.register();
        ItemStack prototype = new ItemStack(Items.DYE, 1, 4);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("hbm_loot_test", "original");
        prototype.setTagCompound(tag);
        WeightedRandomChestContent entry = new WeightedRandomChestContent(prototype, 2, 2, 1);
        ItemStack generated = entry.generateStack(new Random(1));
        require(generated != prototype && generated.getCount() == 2, "count/copy");
        require(generated.getMetadata() == 4, "metadata");
        require("original".equals(generated.getTagCompound().getString("hbm_loot_test")), "NBT content");
        generated.getTagCompound().setString("hbm_loot_test", "changed");
        require("original".equals(prototype.getTagCompound().getString("hbm_loot_test")), "NBT isolation");
        require(prototype.getCount() == 1, "prototype unchanged");
        ItemStack empty = new WeightedRandomChestContent(prototype, 0, 0, 1).generateStack(new Random(1));
        require(empty == ItemStack.EMPTY, "canonical empty");
        InventoryBasic inventory = new InventoryBasic("loot_test", true, 1);
        inventory.setInventorySlotContents(0, new ItemStack(Items.STICK, 7));
        WeightedRandomChestContent.generateChestContents(new Random(1), new WeightedRandomChestContent[] {entry}, inventory, 2);
        require(inventory.getStackInSlot(0).getItem() == Items.DYE, "insertion overwrites");
        require(inventory.getStackInSlot(0).getCount() == 2, "insertion does not merge");
        WeightedRandomChestContent tool = new WeightedRandomChestContent(Items.IRON_SWORD, 0, 3, 3, 1);
        WeightedRandomChestContent.generateChestContents(new Random(1), new WeightedRandomChestContent[] {tool}, inventory, 1);
        require(inventory.getStackInSlot(0).getItem() == Items.IRON_SWORD, "unstackable item");
        require(inventory.getStackInSlot(0).getCount() == 1, "oversize splits to units");
        verifyZeroCountPrototype();
        VanillaLootHandler handler = new VanillaLootHandler();
        handler.addItem(LootTableList.CHESTS_SIMPLE_DUNGEON, Items.DYE, 7);
        Item pending = new Item();
        require(pending.getRegistryName() == null, "fixture starts before item registration");
        handler.addItem(LootTableList.CHESTS_SIMPLE_DUNGEON, pending, 2);
        pending.setRegistryName(new ResourceLocation("minecraft", "hbm_pending_loot_fixture"));
        LootEntry vanilla = new LootEntryItem(Items.STICK, 2, 0, new LootFunction[0], new LootCondition[0], "minecraft:stick");
        for (int reload = 0; reload < 2; reload++) {
            RandomValueRange rolls = new RandomValueRange(3, 5);
            RandomValueRange bonus = new RandomValueRange(0);
            LootPool pool = new LootPool(new LootEntry[] {vanilla}, new LootCondition[0], rolls, bonus, "main");
            LootTable table = new LootTable(new LootPool[] {pool});
            handler.onLootTableLoad(new LootTableLoadEvent(LootTableList.CHESTS_SIMPLE_DUNGEON, table, null));
            LootEntry added = pool.getEntry("minecraft:dye");
            require(added != null && added.getEffectiveWeight(0) == 7, "vanilla injection weight");
            require(pool.getEntry("minecraft:hbm_pending_loot_fixture") != null, "item names resolved after registration");
            require(pool.getEntry("minecraft:stick") == vanilla, "vanilla entries retained");
            require(pool.getRolls() == rolls && pool.getBonusRolls() == bonus, "vanilla rolls retained");
            List<ItemStack> stacks = new ArrayList<>();
            added.addLoot(stacks, new Random(1), null);
            require(stacks.size() == 1 && stacks.get(0).getCount() == 1, "vanilla injected min=max=1");
        }
        verifyBonusChestJson();
        System.out.println("PASS: real ItemStack copy/count/metadata/NBT isolation/EMPTY; InventoryBasic insertion and unstackable splitting");
        System.out.println("PASS: real LootTableLoadEvent injection/reload, weight/count, vanilla entries and rolls retained");
    }

    private static final class CountingRandom extends Random {
        int draws;
        CountingRandom() { super(1234); }
        @Override public int nextInt(int bound) { draws++; return super.nextInt(bound); }
    }

    private static void verifyZeroCountPrototype() {
        ItemStack zero = new ItemStack(Items.DYE, 0, 4);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("hbm_zero_template", "preserved");
        zero.setTagCompound(tag);
        WeightedRandomChestContent entry = new WeightedRandomChestContent(zero, 1, 1, 1);
        ItemStack generated = entry.generateStack(new Random(1));
        require(generated.getItem() == Items.DYE && generated.getCount() == 1, "zero template raw item/count");
        require(generated.getMetadata() == 4, "zero template metadata");
        require("preserved".equals(generated.getTagCompound().getString("hbm_zero_template")), "zero template NBT");
        generated.getTagCompound().setString("hbm_zero_template", "changed");
        require("preserved".equals(zero.getTagCompound().getString("hbm_zero_template")), "zero template NBT isolation");
        InventoryBasic inventory = new InventoryBasic("zero_count_template", true, 1);
        CountingRandom random = new CountingRandom();
        WeightedRandomChestContent.generateChestContents(random, new WeightedRandomChestContent[] {entry}, inventory, 1);
        require(inventory.getStackInSlot(0).getItem() == Items.DYE, "zero template inventory insertion");
        require(random.draws == 3, "zero template retains item/count/slot RNG draws");
        ItemStack zeroTool = new ItemStack(Items.IRON_SWORD, 0, 7);
        entry = new WeightedRandomChestContent(zeroTool, 3, 3, 1);
        random = new CountingRandom();
        WeightedRandomChestContent.generateChestContents(random, new WeightedRandomChestContent[] {entry}, inventory, 1);
        require(inventory.getStackInSlot(0).getItem() == Items.IRON_SWORD, "zero unstackable raw item");
        require(inventory.getStackInSlot(0).getCount() == 1 && inventory.getStackInSlot(0).getMetadata() == 7, "zero unstackable limit and metadata");
        require(random.draws == 5, "zero unstackable retains three slot RNG draws");
        require(zero.getCount() == 0 && zeroTool.getCount() == 0, "zero prototypes unchanged");
        random = new CountingRandom();
        entry = new WeightedRandomChestContent(ItemStack.EMPTY, 1, 1, 1);
        WeightedRandomChestContent.generateChestContents(random, new WeightedRandomChestContent[] {entry}, inventory, 1);
        require(random.draws == 2 && inventory.getStackInSlot(0).getItem() == Items.IRON_SWORD, "real EMPTY skips slot insertion");
        require(ItemStack.EMPTY.getCount() == 0 && ItemStack.EMPTY.isEmpty(), "EMPTY singleton unchanged");
        System.out.println("PASS: zero-count templates retain raw item/metadata/NBT and slot RNG; zero-count tools split correctly; actual EMPTY skipped");
    }

    private static void verifyBonusChestJson() throws Exception {
        Field gsonField = LootTableManager.class.getDeclaredField("GSON_INSTANCE");
        gsonField.setAccessible(true);
        Gson gson = (Gson) gsonField.get(null);
        String path = "/assets/minecraft/loot_tables/chests/spawn_bonus_chest.json";
        String json;
        JsonObject data;
        try (InputStream source = WeightedLootMinecraftTest.class.getResourceAsStream(path)) {
            require(source != null, "real target bonus chest JSON available");
            data = gson.fromJson(new InputStreamReader(source, StandardCharsets.UTF_8), JsonObject.class);
            require(data.getAsJsonArray("pools").size() == 4, "target bonus JSON has four pools");
            json = data.toString();
        }
        ResourceLocation name = LootTableList.CHESTS_SPAWN_BONUS_CHEST;
        LootTable baseline = ForgeHooks.loadLootTable(gson, name, json, false, null);
        Item no9 = new Item().setRegistryName(new ResourceLocation("minecraft", "hbm_no9_test"));
        ForgeRegistries.ITEMS.register(no9);
        VanillaLootHandler handler = new VanillaLootHandler();
        handler.addItem(name, no9, 7);
        // Forge's parser freezes the table after its event. This raw-jar probe
        // lacks FML's event class transformations, so use mutable pool wrappers
        // around the REAL deserialized entries/rolls and invoke the event callback
        // directly. Do not unfreeze or replace any of the parsed entry data.
        LootPool[] pools = new LootPool[4];
        for (int i = 0; i < pools.length; i++) {
            String poolName = i == 0 ? "main" : "pool" + i;
            LootPool original = baseline.getPool(poolName);
            require(original != null, "Forge parser assigns target pool name " + poolName);
            JsonObject originalJson = data.getAsJsonArray("pools").get(i).getAsJsonObject();
            require(!originalJson.has("conditions"), "target bonus pool has no conditions");
            JsonArray definitions = originalJson.getAsJsonArray("entries");
            LootEntry[] parsed = new LootEntry[definitions.size()];
            for (int entry = 0; entry < parsed.length; entry++) {
                String entryName = definitions.get(entry).getAsJsonObject().get("name").getAsString();
                parsed[entry] = original.getEntry(entryName);
                require(parsed[entry] != null, "real parsed target entry " + entryName);
            }
            pools[i] = new LootPool(parsed, new LootCondition[0], original.getRolls(), original.getBonusRolls(), poolName);
        }
        LootTable injected = new LootTable(pools);
        handler.onLootTableLoad(new LootTableLoadEvent(name, injected, null));
        for (String pool : new String[] {"main", "pool1", "pool2"}) {
            require(gson.toJson(baseline.getPool(pool)).equals(gson.toJson(injected.getPool(pool))), "bonus tool/food pool preserved: " + pool);
            require(injected.getPool(pool).getEntry("minecraft:hbm_no9_test") == null, "no9 excluded from tool/food pool " + pool);
        }
        require(injected.getPool("main").getEntry("minecraft:stone_axe").getEffectiveWeight(0) == 1, "stone axe weight retained");
        require(injected.getPool("main").getEntry("minecraft:wooden_axe").getEffectiveWeight(0) == 3, "wooden axe weight retained");
        require(injected.getPool("pool1").getEntry("minecraft:stone_pickaxe").getEffectiveWeight(0) == 1, "stone pickaxe retained");
        require(injected.getPool("pool1").getEntry("minecraft:wooden_pickaxe").getEffectiveWeight(0) == 3, "wooden pickaxe retained");
        LootPool resources = injected.getPool("pool3");
        require(resources.getEntry("minecraft:hbm_no9_test").getEffectiveWeight(0) == 7, "no9 joins resource pool with weight 7");
        require(resources.getRolls().getMin() == 4 && resources.getRolls().getMax() == 4, "resource pool keeps four rolls");
        JsonObject expected = gson.toJsonTree(baseline.getPool("pool3")).getAsJsonObject();
        JsonObject actual = gson.toJsonTree(resources).getAsJsonObject();
        JsonArray entries = actual.getAsJsonArray("entries");
        require(entries.size() == expected.getAsJsonArray("entries").size() + 1, "one resource entry added");
        entries.remove(entries.size() - 1);
        require(expected.equals(actual), "all original resource entries/counts/rolls preserved");
        System.out.println("PASS: real bonus JSON parsed by Forge with native event callback keeps axe/pickaxe/food pools unchanged; representative no9 weight7 joins only pool3");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
