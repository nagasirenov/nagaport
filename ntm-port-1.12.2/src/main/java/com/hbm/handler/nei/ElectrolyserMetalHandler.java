package com.hbm.handler.nei;

import java.awt.Rectangle;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.gui.GUIElectrolyserMetal;
import com.hbm.inventory.recipes.ElectrolyserMetalRecipes;

public class ElectrolyserMetalHandler extends NEIUniversalHandler {

	public ElectrolyserMetalHandler() {
		super(ModBlocks.machine_electrolyser.getLocalizedName(), ModBlocks.machine_electrolyser, ElectrolyserMetalRecipes.getRecipes());
	}

	@Override
	public String getUid() {
		return "ntmElectrolysisMetal";
	}
	
	@Override
	public void registerGuiAreas(mezz.jei.api.IModRegistry registry) {
		registry.addRecipeClickArea(GUIElectrolyserMetal.class, (2) + 5, (35) + 11, 22, 25, "ntmElectrolysisMetal");
	}
}
