package com.hbm.handler.nei;

import java.awt.Rectangle;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.gui.GUIPyroOven;
import com.hbm.inventory.recipes.PyroOvenRecipes;

public class PyroHandler extends NEIUniversalHandler {

	public PyroHandler() {
		super(ModBlocks.machine_pyrooven.getLocalizedName(), ModBlocks.machine_pyrooven, PyroOvenRecipes.getRecipes());
	}

	@Override
	public String getUid() {
		return "ntmPyrolysis";
	}
	
	@Override
	public void registerGuiAreas(mezz.jei.api.IModRegistry registry) {
		registry.addRecipeClickArea(GUIPyroOven.class, (50) + 5, (35) + 11, 28, 14, "ntmPyrolysis");
	}
}
