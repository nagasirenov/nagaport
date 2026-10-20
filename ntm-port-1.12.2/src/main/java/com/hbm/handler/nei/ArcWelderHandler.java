package com.hbm.handler.nei;

import java.awt.Rectangle;
import java.util.Locale;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.gui.GUIMachineArcWelder;
import com.hbm.inventory.recipes.ArcWelderRecipes;
import com.hbm.inventory.recipes.ArcWelderRecipes.ArcWelderRecipe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;

public class ArcWelderHandler extends NEIUniversalHandler {

	public ArcWelderHandler() {
		super(ModBlocks.machine_arc_welder.getLocalizedName(), ModBlocks.machine_arc_welder, ArcWelderRecipes.getRecipes());
	}

	@Override
	public String getUid() {
		return "ntmArcWelder";
	}
	
	@Override
	public void registerGuiAreas(mezz.jei.api.IModRegistry registry) {
		registry.addRecipeClickArea(GUIMachineArcWelder.class, (67) + 5, (26) + 11, 32, 14, "ntmArcWelder");
	}

	@Override
	public void drawRecipe(com.hbm.handler.jei.MachineRecipeView rec, Minecraft minecraft, int mouseX, int mouseY) {

		super.drawRecipe(rec, minecraft, mouseX, mouseY);
		Object[] original = (Object[]) rec.source;
		ItemStack output = rec.displayedOutput(0);
		
		outer: for(ArcWelderRecipe arc : ArcWelderRecipes.recipes) {
			
			//checks do not include the fluid, will break of there's two recipes with identical input and output but with fluids
			if(ItemStack.areItemStacksEqual(arc.output, output) && arc.ingredients.length == original.length - (arc.fluid == null ? 0 : 1)) {
				
				for(int i = 0; i < rec.inputs.length - (arc.fluid == null ? 0 : 1); i++) {
					if(arc.ingredients[i] != original[i]) continue outer;
				}

				FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
				String duration = String.format(Locale.US, "%,d", arc.duration) + " ticks";
				String consumption = String.format(Locale.US, "%,d", arc.consumption) + " HE/t";
				int side = 160;
				fontRenderer.drawString(duration, side - fontRenderer.getStringWidth(duration), 43, 0x404040);
				fontRenderer.drawString(consumption, side - fontRenderer.getStringWidth(consumption), 55, 0x404040);
				return;
			}
		}
	}
}
