package com.hbm.main;

import com.hbm.lib.RefStrings;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;

/** Native JSON models for the two raw BlockContainer electric-furnace blocks. */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class ElectricFurnaceModelHandler {

	private ElectricFurnaceModelHandler() { }

	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent event) {
		registerItem("tile.machine_electric_furnace_off");
		registerItem("tile.machine_electric_furnace_on");
	}

	private static void registerItem(String path) {
		ResourceLocation name = new ResourceLocation(RefStrings.MODID, path);
		Item item = ForgeRegistries.ITEMS.getValue(name);
		if(!(item instanceof ItemBlock) || !name.equals(((ItemBlock) item).getBlock().getRegistryName())) {
			throw new IllegalStateException("Missing electric-furnace ItemBlock: " + name);
		}
		// Preserve all legacy metadata values, including the unusual 0/1 faces.
		// The item models share native cull-face quads with the block models;
		// they must not duplicate those quads in the general/null-face list.
		for(int metadata = 0; metadata < 16; metadata++) {
			ModelLoader.setCustomModelResourceLocation(item, metadata,
					new ModelResourceLocation(RefStrings.MODID + ":" + path + "_" + metadata, "inventory"));
		}
	}
}
