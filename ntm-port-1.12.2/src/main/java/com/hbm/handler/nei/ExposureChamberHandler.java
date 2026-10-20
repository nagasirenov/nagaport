package com.hbm.handler.nei;

import java.awt.Rectangle;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.gui.GUIMachineExposureChamber;
import com.hbm.inventory.recipes.ExposureChamberRecipes;

public class ExposureChamberHandler extends NEIUniversalHandler {

	public ExposureChamberHandler() {
		super(ModBlocks.machine_exposure_chamber.getLocalizedName(), ModBlocks.machine_exposure_chamber, ExposureChamberRecipes.getRecipes());
	}

	@Override
	public String getUid() {
		return "ntmExposure";
	}
	
	@Override
	public void registerGuiAreas(mezz.jei.api.IModRegistry registry) {
		registry.addRecipeClickArea(GUIMachineExposureChamber.class, (31) + 5, (28) + 11, 41, 10, "ntmExposure");
	}
}
