package com.hbm.items.block;

import com.hbm.blocks.generic.BlockConcreteColored;

import net.minecraft.block.Block;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;

public class ItemBlockColoredConcrete extends ItemBlockBlastInfo {

	public ItemBlockColoredConcrete(Block block) {
		super(block);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
	}

	public int getMetadata(int meta) {
		return meta;
	}
	
	public String getTranslationKey(ItemStack stack) {
		return super.getTranslationKey() + "." + EnumDyeColor.byDyeDamage(BlockConcreteColored.func_150032_b(stack.getItemDamage())).getTranslationKey();
	}
}
