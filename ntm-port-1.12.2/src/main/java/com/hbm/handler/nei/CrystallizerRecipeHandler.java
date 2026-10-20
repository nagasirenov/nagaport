package com.hbm.handler.nei;

import java.awt.Rectangle;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.gui.GUICrystallizer;
import com.hbm.inventory.recipes.CrystallizerRecipes;
import com.hbm.inventory.recipes.CrystallizerRecipes.CrystallizerRecipe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class CrystallizerRecipeHandler extends NEIUniversalHandler {

	public CrystallizerRecipeHandler() {
		super("Acidizer", ModBlocks.machine_crystallizer, CrystallizerRecipes.getRecipes());
	}

	@Override
	public String getUid() {
		return "ntmCrystallizer";
	}
	
	@Override
	public void registerGuiAreas(mezz.jei.api.IModRegistry registry) {
		registry.addRecipeClickArea(GUICrystallizer.class, (80 - 5) + 5, (47 - 11) + 11, 27, 12, "ntmCrystallizer");
	}

	@Override
	public void drawRecipe(com.hbm.handler.jei.MachineRecipeView rec, Minecraft minecraft, int mouseX, int mouseY) {

		super.drawRecipe(rec, minecraft, mouseX, mouseY);
		
		CrystallizerRecipe cRecipe = CrystallizerRecipes.getOutput(rec.displayedInput(1), Fluids.fromID(rec.displayedInput(0).getItemDamage()));
		
		if(cRecipe != null && cRecipe.productivity > 0) {
			FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
			String momentum = "Effectiveness: +" + Math.min((int) (cRecipe.productivity * 100), 99) + "% per level";
			int side = 8;
			fontRenderer.drawString(momentum, side, 52, 0x404040);
		}
	}
}
