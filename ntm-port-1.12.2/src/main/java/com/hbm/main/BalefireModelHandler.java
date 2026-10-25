package com.hbm.main;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/** Restores Balefire's original render-type-1 crossed squares and shared animated icon. */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class BalefireModelHandler {

	private static final ModelResourceLocation WORLD = new ModelResourceLocation(
			new ResourceLocation(RefStrings.MODID, "tile.balefire"), "normal");

	private BalefireModelHandler() { }

	@SubscribeEvent
	public static void register(ModelRegistryEvent event) {
		ModelLoader.setCustomStateMapper(ModBlocks.balefire, new StateMapperBase() {
			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
				// Legacy crossed squares use one icon, independent of age or attachments.
				// Ignore properties only for model lookup; world state remains unchanged.
				return WORLD;
			}
		});
	}
}
