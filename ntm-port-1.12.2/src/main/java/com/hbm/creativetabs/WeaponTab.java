package com.hbm.creativetabs;

import com.hbm.items.ModItems;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

public class WeaponTab extends CreativeTabs {

	public WeaponTab(int index, String name) {
		super(index, name);
	}


		@Override
	public net.minecraft.item.ItemStack createIcon() {
		return new net.minecraft.item.ItemStack(getTabIconItem());
	}

public Item getTabIconItem() {
		if(ModItems.gun_greasegun != null) return ModItems.gun_greasegun;
		return Items.IRON_PICKAXE;
	}
}
