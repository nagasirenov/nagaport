package com.hbm.handler.nei;

import java.awt.Rectangle;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.gui.GUIMachineCentrifuge;
import com.hbm.inventory.recipes.CentrifugeRecipes;

public class CentrifugeRecipeHandler extends NEIUniversalHandler {

	public CentrifugeRecipeHandler() {
		super("Centrifuge", ModBlocks.machine_centrifuge, CentrifugeRecipes.getRecipes());
	}

	@Override
	public String getUid() {
		return "ntmCentrifuge";
	}
	
	@Override
	public void registerGuiAreas(mezz.jei.api.IModRegistry registry) {
		registry.addRecipeClickArea(GUIMachineCentrifuge.class, (56) + 5, (0) + 11, 80, 38, "ntmCentrifuge");
	}
}
