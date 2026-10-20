package com.hbm.util;

import com.hbm.interfaces.NotableComments;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatCrafting;
import net.minecraft.stats.StatList;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.Iterator;
import java.util.Map;

@NotableComments
public class StatHelper {

	/*
	 * God is dead and we are pissing on his grave
	 */
	public static Map publicReferenceToOneshotStatListPleaseAllPointAndLaugh;

	/**
	 * This is probably the worst fucking way of doing this, but it's the only one I could think of that works.
	 * In short: stats are hilariously broken.
	 * Minecraft registers them at the earliest point during startup right after blocks and items are registered.
	 * The problem in that? Item remapping. Modded items probably don't even exist at that point and even if they did,
	 * the system would break because modded items have dynamic IDs and the stats register fixed IDs instead of item instances.
	 * What did forge do to solve this issue? Well nothing, of course! The injected bits comment on that in vanilla's stat
	 * registering code, but instead of fixing anything it just slaps a fat "TODO" onto it. Wow! Really helpful!
	 *
	 * So what do we do? Every time the world starts and we know the IDs are now correct, we smack that fucker up nice and good.
	 * All ID-bound stats get deep-cleaned out of this mess and registered 1:1 again. Is this terrible and prone to breaking with
	 * mods that do their own stat handling? Hard to say, but the possibility is there.
	 */
	public static void resetStatShitFuck() {

		publicReferenceToOneshotStatListPleaseAllPointAndLaugh = ReflectionHelper.getPrivateValue(StatList.class, null, "field_75942_a", "oneShotStats");

		for(int i = 0; i < StatList.objectCraftStats.length; i++) StatList.objectCraftStats[i] = null;
		for(int i = 0; i < StatList.mineBlockStatArray.length; i++) StatList.mineBlockStatArray[i] = null;
		for(int i = 0; i < StatList.objectUseStats.length; i++) StatList.objectUseStats[i] = null;
		for(int i = 0; i < StatList.objectBreakStats.length; i++) StatList.objectBreakStats[i] = null;
		StatList.objectMineStats.clear();
		StatList.itemStats.clear();

		try {
			initCraftItemStats();
			initBlockMineStats();
			initItemUseStats();
			initItemBreakStats();
		} catch(Throwable ex) { } // just to be sure
	}

	/**
	 * For reasons beyond human comprehension, this bit originally only registered items that are the result
	 * of an IRecipe instead of just all items outright like the item usage stats. The logical consequence of this is:
	 * 1) The code has to iterate over thousands of recipes to get recipe results which is no more performant than just going over
	 *    32k potential items, most of which are going to be null anyway
	 * 2) The system just will never work with items that don't have crafting table recipes
	 */
	private static void initCraftItemStats() {
		Iterator iterator = Item.itemRegistry.iterator();
		while(iterator.hasNext()) {
			Item item = (Item) iterator.next();

			if(item != null) {
				int i = Item.getIdFromItem(item);
				try {
					StatList.objectCraftStats[i] = registerStat(new StatCrafting("stat.craftItem." + i, "", new TextComponentTranslation("stat.craftItem", new Object[] { (new ItemStack(item)).getTextComponent() }), item));
				} catch(Throwable ex) { }
			}
		}

		replaceAllSimilarBlocks(StatList.objectCraftStats);
	}

	private static void initBlockMineStats() {
		Iterator iterator = Block.blockRegistry.iterator();

		while(iterator.hasNext()) {
			Block block = (Block) iterator.next();

			if(Item.getItemFromBlock(block) != null) {
				int i = Block.getIdFromBlock(block);
				try {
					if(block.getEnableStats()) {
						StatList.mineBlockStatArray[i] = registerStat(new StatCrafting("stat.mineBlock." + i, "", new TextComponentTranslation("stat.mineBlock", new Object[] { (new ItemStack(block)).getTextComponent() }), Item.getItemFromBlock(block)));
						StatList.objectMineStats.add((StatCrafting) StatList.mineBlockStatArray[i]);
					}
				} catch(Throwable ex) { }
			}
		}

		replaceAllSimilarBlocks(StatList.mineBlockStatArray);
	}

	private static void initItemUseStats() {
		Iterator iterator = Item.itemRegistry.iterator();

		while(iterator.hasNext()) {
			Item item = (Item) iterator.next();

			if(item != null) {
				int i = Item.getIdFromItem(item);
				try {
					StatList.objectUseStats[i] = registerStat(new StatCrafting("stat.useItem." + i, "", new TextComponentTranslation("stat.useItem", new Object[] { (new ItemStack(item)).getTextComponent() }), item));
					if(!(item instanceof ItemBlock)) {
						StatList.itemStats.add((StatCrafting) StatList.objectUseStats[i]);
					}
				} catch(Throwable ex) { }
			}
		}

		replaceAllSimilarBlocks(StatList.objectUseStats);
	}

	private static void initItemBreakStats() {
		Iterator iterator = Item.itemRegistry.iterator();

		while(iterator.hasNext()) {
			Item item = (Item) iterator.next();

			if(item != null) {
				int i = Item.getIdFromItem(item);
				try {
					if(item.isDamageable()) {
						StatList.objectBreakStats[i] = registerStat(new StatCrafting("stat.breakItem." + i, "", new TextComponentTranslation("stat.breakItem", new Object[] { (new ItemStack(item)).getTextComponent() }), item));
					}
				} catch(Throwable ex) { }
			}
		}

		replaceAllSimilarBlocks(StatList.objectBreakStats);
	}

	private static void replaceAllSimilarBlocks(StatBase[] stats) {
		func_151180_a(stats, Blocks.WATER, Blocks.FLOWING_WATER);
		func_151180_a(stats, Blocks.LAVA, Blocks.FLOWING_LAVA);
		func_151180_a(stats, Blocks.LIT_PUMPKIN, Blocks.PUMPKIN);
		func_151180_a(stats, Blocks.LIT_FURNACE, Blocks.FURNACE);
		func_151180_a(stats, Blocks.LIT_REDSTONE_ORE, Blocks.REDSTONE_ORE);
		func_151180_a(stats, Blocks.POWERED_REPEATER, Blocks.UNPOWERED_REPEATER);
		func_151180_a(stats, Blocks.POWERED_COMPARATOR, Blocks.UNPOWERED_COMPARATOR);
		func_151180_a(stats, Blocks.REDSTONE_TORCH, Blocks.UNLIT_REDSTONE_TORCH);
		func_151180_a(stats, Blocks.LIT_REDSTONE_LAMP, Blocks.REDSTONE_LAMP);
		func_151180_a(stats, Blocks.RED_MUSHROOM, Blocks.BROWN_MUSHROOM);
		func_151180_a(stats, Blocks.DOUBLE_STONE_SLAB, Blocks.STONE_SLAB);
		func_151180_a(stats, Blocks.DOUBLE_WOODEN_SLAB, Blocks.WOODEN_SLAB);
		func_151180_a(stats, Blocks.GRASS, Blocks.DIRT);
		func_151180_a(stats, Blocks.FARMLAND, Blocks.DIRT);
	}

	private static void func_151180_a(StatBase[] stats, Block block, Block similarBlock) {
		int i = Block.getIdFromBlock(block);
		int j = Block.getIdFromBlock(similarBlock);

		if(stats[i] != null && stats[j] == null) {
			stats[j] = stats[i];
		} else {
			StatList.allStats.remove(stats[i]);
			StatList.objectMineStats.remove(stats[i]);
			StatList.generalStats.remove(stats[i]);
			stats[i] = stats[j];
		}
	}

	private static StatBase registerStat(StatBase stat) {
		if(publicReferenceToOneshotStatListPleaseAllPointAndLaugh.containsKey(stat.statId)) {
			publicReferenceToOneshotStatListPleaseAllPointAndLaugh.remove(stat.statId);
		}

		StatList.allStats.add(stat);
		publicReferenceToOneshotStatListPleaseAllPointAndLaugh.put(stat.statId, stat);
		return stat;
	}
}
