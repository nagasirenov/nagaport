package com.hbm.handler.nei;

import java.awt.Rectangle;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.gui.GUIElectrolyserFluid;
import com.hbm.inventory.recipes.ElectrolyserFluidRecipes;

public class ElectrolyserFluidHandler extends NEIUniversalHandler {

	public ElectrolyserFluidHandler() {
		super(ModBlocks.machine_electrolyser.getLocalizedName(), ModBlocks.machine_electrolyser, ElectrolyserFluidRecipes.getRecipes());
	}

	@Override
	public String getUid() {
		return "ntmElectrolysisFluid";
	}
	
	@Override
	public void registerGuiAreas(mezz.jei.api.IModRegistry registry) {
		registry.addRecipeClickArea(GUIElectrolyserFluid.class, (57) + 5, (15) + 11, 12, 40, "ntmElectrolysisFluid");
	}
}
