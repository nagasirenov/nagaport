package com.hbm.handler.nei;

import java.awt.Rectangle;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.gui.GUIFurnaceCombo;
import com.hbm.inventory.recipes.CombinationRecipes;

public class CombinationHandler extends NEIUniversalHandler {

	public CombinationHandler() {
		super(ModBlocks.furnace_combination.getLocalizedName(), ModBlocks.furnace_combination, CombinationRecipes.getRecipes());
	}

	@Override
	public String getUid() {
		return "ntmCombination";
	}
	
	@Override
	public void registerGuiAreas(mezz.jei.api.IModRegistry registry) {
		registry.addRecipeClickArea(GUIFurnaceCombo.class, (49) + 5, (44) + 11, 18, 18, "ntmCombination");
	}
}
