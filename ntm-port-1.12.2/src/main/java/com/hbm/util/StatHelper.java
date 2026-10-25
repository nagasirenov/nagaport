package com.hbm.util;

import com.hbm.interfaces.NotableComments;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatCrafting;
import net.minecraft.stats.StatList;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@NotableComments
public class StatHelper {

	public static Map publicReferenceToOneshotStatListPleaseAllPointAndLaugh;
	private static final Set<String> CUSTOM_CRAFT_STAT_IDS = new HashSet<>();

	/** Rebuild vanilla stats after Forge has finalized the item and block registry IDs. */
	public static void resetStatShitFuck() {
		if(publicReferenceToOneshotStatListPleaseAllPointAndLaugh != null) {
			for(String statId : CUSTOM_CRAFT_STAT_IDS) {
				StatBase stat = (StatBase) publicReferenceToOneshotStatListPleaseAllPointAndLaugh.remove(statId);
				if(stat != null) StatList.ALL_STATS.remove(stat);
			}
		}
		CUSTOM_CRAFT_STAT_IDS.clear();

		StatList.reinit();
		publicReferenceToOneshotStatListPleaseAllPointAndLaugh = ReflectionHelper.getPrivateValue(StatList.class, null, "field_188093_a", "ID_TO_STAT_MAP");
		initCraftItemStats();
	}

	/** 1.12 only registers recipe-result crafting stats; preserve stats for every registered item. */
	private static void initCraftItemStats() {
		StatBase[] craftStats = ReflectionHelper.getPrivateValue(StatList.class, null, "field_188066_af", "CRAFTS_STATS");
		Iterator<Item> iterator = Item.REGISTRY.iterator();
		while(iterator.hasNext()) {
			Item item = iterator.next();
			if(item == null) continue;

			int id = Item.getIdFromItem(item);
			if(id < 0 || id >= craftStats.length || craftStats[id] != null) continue;

			String statId = "stat.craftItem." + id;
			StatBase existing = (StatBase) publicReferenceToOneshotStatListPleaseAllPointAndLaugh.get(statId);
			if(existing != null) {
				craftStats[id] = existing;
				continue;
			}

			StatBase stat = new StatCrafting(statId, "", new TextComponentTranslation("stat.craftItem", new ItemStack(item).getTextComponent()), item).registerStat();
			craftStats[id] = stat;
			CUSTOM_CRAFT_STAT_IDS.add(stat.statId);
		}
	}
}
