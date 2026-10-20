package com.hbm.handler.nei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.FuelPoolRecipes;
import com.hbm.items.machine.ItemRBMKRod;

import net.minecraft.item.ItemStack;

public class FuelPoolHandler extends NEIUniversalHandler {

	public FuelPoolHandler() {
		super("Spent Fuel Pool Drum", ModBlocks.machine_waste_drum, FuelPoolRecipes.recipes);
	}

	@Override
	public String getUid() {
		return "ntmSpentDrum";
	}

    @Override
    protected boolean acceptsFocus(mezz.jei.api.recipe.IFocus<ItemStack> focus) {
        if(focus == null) return true;
        ItemStack ingredient = focus.getValue();
        if(ingredient != null && ingredient.getItem() instanceof ItemRBMKRod) {
            if(focus.getMode() == mezz.jei.api.recipe.IFocus.Mode.INPUT)
                return !(ItemRBMKRod.getCoreHeat(ingredient) < 50 && ItemRBMKRod.getHullHeat(ingredient) < 50);
            return !(ItemRBMKRod.getCoreHeat(ingredient) >= 50 || ItemRBMKRod.getHullHeat(ingredient) >= 50);
        }
        return true;
    }
}
