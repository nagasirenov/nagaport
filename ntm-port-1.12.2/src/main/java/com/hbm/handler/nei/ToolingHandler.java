package com.hbm.handler.nei;

import com.hbm.blocks.generic.BlockToolConversion;
import com.hbm.items.ModItems;
import net.minecraft.item.ItemStack;

public class ToolingHandler extends NEIUniversalHandler {
	@Override
	public ItemStack[] getMachines() {
		return new ItemStack[]{
				new ItemStack(ModItems.boltgun),
				new ItemStack(ModItems.blowtorch),
				new ItemStack(ModItems.acetylene_torch)};
	}
	public ToolingHandler() {
		super("Tooling", BlockToolConversion.getRecipes(true), BlockToolConversion.getRecipes(false));
	}

	@Override
	public String getUid() {
		return "ntmTooling";
	}
}
