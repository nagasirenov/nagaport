package com.hbm.dim;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraftforge.event.LootTableLoadEvent;

public final class DimensionFishingLoot {

	private DimensionFishingLoot() { }

	public enum Category {
		FISH,
		JUNK,
		TREASURE
	}

	public interface Provider {
		List<Entry> getFishingLoot(Category category);
	}

	public static final class Entry {
		private final ItemStack stack;
		private final int weight;
		private final float enchantmentChance;

		public Entry(ItemStack stack, int weight) {
			this(stack, weight, 0.0F);
		}

		public Entry(ItemStack stack, int weight, float enchantmentChance) {
			if(stack == null || stack.isEmpty()) throw new IllegalArgumentException("Fishing loot stack must not be empty");
			if(weight <= 0) throw new IllegalArgumentException("Fishing loot weight must be positive");
			if(Float.isNaN(enchantmentChance) || enchantmentChance < 0.0F || enchantmentChance > 1.0F) {
				throw new IllegalArgumentException("Enchantment chance must be between 0 and 1");
			}

			this.stack = stack.copy();
			this.weight = weight;
			this.enchantmentChance = enchantmentChance;
		}

		private ItemStack createStack(Random random) {
			ItemStack result = stack.copy();
			if(enchantmentChance > 0.0F && random.nextFloat() < enchantmentChance) {
				result = EnchantmentHelper.addRandomEnchantment(random, result, 30, true);
			}
			return result;
		}
	}

	public static void onLootTableLoad(LootTableLoadEvent event) {
		Category category = getCategory(event.getName());
		if(category == null) return;

		LootTable delegate = event.getTable();
		while(delegate instanceof DimensionFishingLootTable) {
			delegate = ((DimensionFishingLootTable) delegate).delegate;
		}
		event.setTable(new DimensionFishingLootTable(delegate, category));
	}

	public static ItemStack getRandomFishLoot(World world, Random random) {
		if(!(world instanceof WorldServer)) return ItemStack.EMPTY;

		WorldServer serverWorld = (WorldServer) world;
		LootTableManager manager = serverWorld.getLootTableManager();
		LootTable fishTable = manager.getLootTableFromLocation(LootTableList.GAMEPLAY_FISHING_FISH);
		LootContext context = new LootContext(0.0F, serverWorld, manager, null, null, null);
		List<ItemStack> loot = fishTable.generateLootForPools(random, context);
		return loot.isEmpty() ? ItemStack.EMPTY : loot.get(random.nextInt(loot.size()));
	}

	private static Category getCategory(ResourceLocation name) {
		if(LootTableList.GAMEPLAY_FISHING_FISH.equals(name)) return Category.FISH;
		if(LootTableList.GAMEPLAY_FISHING_JUNK.equals(name)) return Category.JUNK;
		if(LootTableList.GAMEPLAY_FISHING_TREASURE.equals(name)) return Category.TREASURE;
		return null;
	}

	private static ItemStack roll(List<Entry> entries, Random random) {
		if(entries == null || entries.isEmpty()) return ItemStack.EMPTY;

		int totalWeight = 0;
		for(Entry entry : entries) {
			if(entry.weight > Integer.MAX_VALUE - totalWeight) {
				throw new IllegalStateException("Fishing loot weights exceed the supported total");
			}
			totalWeight += entry.weight;
		}

		int choice = random.nextInt(totalWeight);
		for(Entry entry : entries) {
			if(choice < entry.weight) return entry.createStack(random);
			choice -= entry.weight;
		}
		throw new IllegalStateException("Fishing loot weight selection failed");
	}

	private static final class DimensionFishingLootTable extends LootTable {
		private final LootTable delegate;
		private final Category category;

		private DimensionFishingLootTable(LootTable delegate, Category category) {
			super(new LootPool[0]);
			this.delegate = delegate;
			this.category = category;
		}

		@Override
		public List<ItemStack> generateLootForPools(Random random, LootContext context) {
			List<ItemStack> defaultLoot = delegate.generateLootForPools(random, context);
			if(defaultLoot.isEmpty() || context == null) return defaultLoot;

			WorldServer world = context.getWorld();
			if(world == null || world.provider.getDimension() == 0 || !(world.provider instanceof Provider)) {
				return defaultLoot;
			}

			List<Entry> customLoot = ((Provider) world.provider).getFishingLoot(category);
			if(customLoot == null) return defaultLoot;

			List<ItemStack> result = new ArrayList<>(defaultLoot.size());
			for(int i = 0; i < defaultLoot.size(); i++) {
				ItemStack stack = roll(customLoot, random);
				if(!stack.isEmpty()) result.add(stack);
			}
			return result;
		}
	}
}
