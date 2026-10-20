package com.hbm.handler.nei;

import java.awt.Rectangle;
import java.util.Locale;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.gui.GUIMachineRotaryFurnace;
import com.hbm.inventory.recipes.RotaryFurnaceRecipes;
import com.hbm.inventory.recipes.RotaryFurnaceRecipes.RotaryFurnaceRecipe;
import com.hbm.items.machine.ItemScraps;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;

public class RotaryFurnaceHandler extends NEIUniversalHandler {

	public RotaryFurnaceHandler() {
		super(ModBlocks.machine_rotary_furnace.getLocalizedName(), ModBlocks.machine_rotary_furnace, RotaryFurnaceRecipes.getRecipes());
	}

	@Override
	public String getUid() {
		return "ntmRotaryFurnace";
	}
	
	@Override
	public void registerGuiAreas(mezz.jei.api.IModRegistry registry) {
		registry.addRecipeClickArea(GUIMachineRotaryFurnace.class, (58) + 5, (19) + 11, 32, 10, "ntmRotaryFurnace");
	}

	@Override
	public void drawRecipe(com.hbm.handler.jei.MachineRecipeView rec, Minecraft minecraft, int mouseX, int mouseY) {

		super.drawRecipe(rec, minecraft, mouseX, mouseY);
		Object[] original = (Object[]) rec.source;
		ItemStack output = rec.displayedOutput(0);
		
		outer: for(RotaryFurnaceRecipe arc : RotaryFurnaceRecipes.recipes) {
			
			if(ItemStack.areItemStacksEqual(ItemScraps.create(arc.output, true), output) && arc.ingredients.length == original.length - (arc.fluid == null ? 0 : 1)) {
				
				for(int i = 0; i < rec.inputs.length - (arc.fluid == null ? 0 : 1); i++) {
					if(arc.ingredients[i] != original[i]) continue outer;
				}

				FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
				String duration = String.format(Locale.US, "%,d", arc.duration) + " ticks";
				String consumption = I18nUtil.resolveKey(Fluids.STEAM.getUnlocalizedName()) + ": " + String.format(Locale.US, "%,d", arc.steam) + " mB/t";
				int side = 160;
				fontRenderer.drawString(duration, side - fontRenderer.getStringWidth(duration), 43, 0x404040);
				fontRenderer.drawString(consumption, side - fontRenderer.getStringWidth(consumption), 55, 0x404040);
				return;
			}
		}
	}
}
