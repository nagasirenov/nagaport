package com.hbm.items.machine;

import java.util.List;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemGear extends Item {
	
	public ItemGear() {
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tabs, List list) {
		for(int i = 0; i < 2; i++) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	public String getTranslationKey(ItemStack stack) {
		return this.getTranslationKey() + (stack.getItemDamage() == 1 ? "_steel" : "");
	}

}
