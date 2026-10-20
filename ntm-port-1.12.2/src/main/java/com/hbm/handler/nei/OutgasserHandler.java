package com.hbm.handler.nei;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map.Entry;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.gui.GUIRBMKOutgasser;
import com.hbm.inventory.recipes.OutgasserRecipes;
import com.hbm.inventory.recipes.OutgasserRecipes.OutgasserRecipe;

import net.minecraft.item.ItemStack;

public class OutgasserHandler extends NEIUniversalHandler {

	public OutgasserHandler() {
		super(ModBlocks.rbmk_outgasser.getLocalizedName(), new ItemStack[] {new ItemStack(ModBlocks.rbmk_outgasser), new ItemStack(ModBlocks.fusion_breeder)}, OutgasserRecipes.getRecipes());
		
		// god i hate this class, just hacks upon hacks to add more stupid crap
		this.machineOverrides = new HashMap<Object, Object>();
		for(Entry<AStack, OutgasserRecipe> entry : OutgasserRecipes.recipes.entrySet()) {
			if(entry.getValue().fusionOnly) {
				this.machineOverrides.put(entry.getKey(), new ItemStack(ModBlocks.fusion_breeder));
			}
		}
	}

	@Override
	public String getUid() {
		return "ntmOutgasser";
	}
	
	@Override
	public void registerGuiAreas(mezz.jei.api.IModRegistry registry) {
		registry.addRecipeClickArea(GUIRBMKOutgasser.class, (75) + 5, (26) + 11, 16, 32, "ntmOutgasser");
	}
}
