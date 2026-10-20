package com.hbm.handler.nei;

import java.awt.Rectangle;
import java.util.Locale;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.gui.GUIVacuumCircuit;
import com.hbm.inventory.recipes.VacuumCircuitRecipes.VacuumCircuitRecipe;
import com.hbm.inventory.recipes.VacuumCircuitRecipes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;

public class VacuumCircuitHandler extends NEIUniversalHandler {

    public VacuumCircuitHandler() {
		super("Vacuum Solderer", ModBlocks.machine_vacuum_circuit, VacuumCircuitRecipes.getRecipes());
	}

	@Override
	public String getUid() {
		return "ntmVacuumCircuit";
	}
	
	@Override
	public void registerGuiAreas(mezz.jei.api.IModRegistry registry) {
		registry.addRecipeClickArea(GUIVacuumCircuit.class, (58) + 5, (17) + 11, 32, 14, "ntmVacuumCircuit");
	}

	@Override
	public void drawRecipe(com.hbm.handler.jei.MachineRecipeView rec, Minecraft minecraft, int mouseX, int mouseY) {

		super.drawRecipe(rec, minecraft, mouseX, mouseY);
		ItemStack output = rec.displayedOutput(0);
		
		for(VacuumCircuitRecipe sol : VacuumCircuitRecipes.recipes) {
			
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
