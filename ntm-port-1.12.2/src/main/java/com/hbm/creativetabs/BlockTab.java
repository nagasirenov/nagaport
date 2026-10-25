package com.hbm.creativetabs;

import com.hbm.blocks.ModBlocks;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

public class BlockTab extends CreativeTabs {

	public BlockTab(int p_i1853_1_, String p_i1853_2_) {
		super(p_i1853_1_, p_i1853_2_);
	}


		@Override
	public net.minecraft.item.ItemStack createIcon() {
		return new net.minecraft.item.ItemStack(getTabIconItem());
	}

public Item getTabIconItem() {

		if(ModBlocks.ore_uranium != null)
		{
			return Item.getItemFromBlock(ModBlocks.ore_uranium);
		}

		return Items.IRON_PICKAXE;
	}

}
