package com.hbm.handler.nei;

import java.util.Locale;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.ParticleAcceleratorRecipes;
import com.hbm.inventory.recipes.ParticleAcceleratorRecipes.ParticleAcceleratorRecipe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class ParticleAcceleratorHandler extends NEIUniversalHandler {

	public ParticleAcceleratorHandler() {
		super(ModBlocks.pa_detector.getLocalizedName(), ModBlocks.pa_detector, ParticleAcceleratorRecipes.getRecipes());
	}

	@Override
	public String getUid() {
		return "ntmParticleAccelerator";
	}

	@Override
	public void drawRecipe(com.hbm.handler.jei.MachineRecipeView rec, Minecraft minecraft, int mouseX, int mouseY) {

		super.drawRecipe(rec, minecraft, mouseX, mouseY);
		
		ParticleAcceleratorRecipe paRecipe = ParticleAcceleratorRecipes.getOutput(rec.displayedInput(0), rec.displayedInput(1));
		
		if(paRecipe != null) {
			FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
			String momentum = "Momentum: " + String.format(Locale.US, "%,d", paRecipe.momentum);
			int side = 8;
			fontRenderer.drawString(momentum, side, 52, 0x404040);
		}
	}
}
