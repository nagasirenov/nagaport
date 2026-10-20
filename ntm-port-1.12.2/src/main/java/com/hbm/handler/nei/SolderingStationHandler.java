package com.hbm.handler.nei;

import java.awt.Rectangle;
import java.util.Locale;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.gui.GUIMachineSolderingStation;
import com.hbm.inventory.recipes.SolderingRecipes;
import com.hbm.inventory.recipes.SolderingRecipes.SolderingRecipe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;

public class SolderingStationHandler extends NEIUniversalHandler {

	public SolderingStationHandler() {
		super(ModBlocks.machine_soldering_station.getLocalizedName(), ModBlocks.machine_soldering_station, SolderingRecipes.getRecipes());
	}

	@Override
	public String getUid() {
		return "ntmSoldering";
	}
	
	@Override
	public void registerGuiAreas(mezz.jei.api.IModRegistry registry) {
		registry.addRecipeClickArea(GUIMachineSolderingStation.class, (67) + 5, (17) + 11, 32, 14, "ntmSoldering");
	}

	@Override
	public void drawRecipe(com.hbm.handler.jei.MachineRecipeView rec, Minecraft minecraft, int mouseX, int mouseY) {

		super.drawRecipe(rec, minecraft, mouseX, mouseY);
		ItemStack output = rec.displayedOutput(0);
		
		for(SolderingRecipe sol : SolderingRecipes.recipes) {
			
			//TODO: rethink this concept, checks only use the output and if two things output the same thing it'll break
			
			if(ItemStack.areItemStacksEqual(sol.output, output)) {

				FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
				String duration = String.format(Locale.US, "%,d", sol.duration) + " ticks";
				String consumption = String.format(Locale.US, "%,d", sol.consumption) + " HE/t";
				int side = 160;
				fontRenderer.drawString(duration, side - fontRenderer.getStringWidth(duration), 43, 0x404040);
				fontRenderer.drawString(consumption, side - fontRenderer.getStringWidth(consumption), 55, 0x404040);
				return;
			}
		}
	}
}
