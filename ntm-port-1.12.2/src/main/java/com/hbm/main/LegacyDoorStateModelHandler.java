package com.hbm.main;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/** World routes for the four legacy doors and the two-block metal fence door. */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class LegacyDoorStateModelHandler {

	private LegacyDoorStateModelHandler() { }

	@SubscribeEvent
	public static void register(ModelRegistryEvent event) {
		for(Block block : new Block[] { ModBlocks.door_office, ModBlocks.door_metal,
				ModBlocks.door_red, ModBlocks.door_bunker, ModBlocks.fence_gate }) {
			// Power drives OPEN but has no independent texture or geometry.
			// Keep FACING, HALF, HINGE and OPEN for the actual-state model lookup.
			ModelLoader.setCustomStateMapper(block, new StateMap.Builder()
					.ignore(BlockDoor.POWERED).build());
		}
	}
}
