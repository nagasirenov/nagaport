package com.hbm.handler.nei;

import java.awt.Rectangle;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.gui.GUIMachineCoker;
import com.hbm.inventory.recipes.CokerRecipes;

public class CokingHandler extends NEIUniversalHandler {

	public CokingHandler() {
		super(ModBlocks.machine_coker.getLocalizedName(), ModBlocks.machine_coker, CokerRecipes.getRecipes());
	}

	@Override
	public String getUid() {
		return "ntmCoking";
	}
	
	@Override
	public void registerGuiAreas(mezz.jei.api.IModRegistry registry) {
		registry.addRecipeClickArea(GUIMachineCoker.class, (55) + 5, (15) + 11, 36, 18, "ntmCoking");
	}
}
