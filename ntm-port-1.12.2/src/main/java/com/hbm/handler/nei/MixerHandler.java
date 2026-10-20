package com.hbm.handler.nei;

import java.awt.Rectangle;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.gui.GUIMixer;
import com.hbm.inventory.recipes.MixerRecipes;

public class MixerHandler extends NEIUniversalHandler {

	public MixerHandler() {
		super(ModBlocks.machine_mixer.getLocalizedName(), ModBlocks.machine_mixer, MixerRecipes.getRecipes());
	}

	@Override
	public String getUid() {
		return "ntmMixer";
	}
	
	@Override
	public void registerGuiAreas(mezz.jei.api.IModRegistry registry) {
		registry.addRecipeClickArea(GUIMixer.class, (57) + 5, (25) + 11, 52, 44, "ntmMixer");
	}
}
