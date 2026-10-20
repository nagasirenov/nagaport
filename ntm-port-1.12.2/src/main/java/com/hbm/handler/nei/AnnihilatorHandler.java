package com.hbm.handler.nei;

import java.util.Map.Entry;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.AnnihilatorRecipes;
import com.hbm.items.ModItems;
import com.hbm.util.InventoryUtil;


import net.minecraft.item.ItemStack;

public class AnnihilatorHandler extends NEIUniversalHandler {

	public AnnihilatorHandler() {
		super("Annihilator", ModBlocks.machine_annihilator, AnnihilatorRecipes.getRecipes());
	}

	@Override
	public String getUid() {
		return "ntmAnnihilating";
	}

    @Override
    protected boolean matchesOutput(ItemStack stack, ItemStack focus) {
        return super.matchesOutput(stack, focus) && ItemStack.areItemStackTagsEqual(stack, focus);
    }
}
