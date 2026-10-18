package com.hbm.handler.radiation;

import com.hbm.config.RadiationConfig;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;

public class ChunkRadiationManager {
	
	public static ChunkRadiationHandler proxy = new ChunkRadiationHandlerSimple();

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if(RadiationConfig.enableChunkRads) proxy.receiveWorldLoad(event);
	}
	
	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		if(RadiationConfig.enableChunkRads) proxy.receiveWorldUnload(event);
	}

	@SubscribeEvent
	public void onChunkLoad(ChunkDataEvent.Load event) {
		if(RadiationConfig.enableChunkRads) proxy.receiveChunkLoad(event);
	}
	
	@SubscribeEvent
	public void onChunkSave(ChunkDataEvent.Save event) {
		if(RadiationConfig.enableChunkRads) proxy.receiveChunkSave(event);
	}
	
	@SubscribeEvent
	public void onChunkUnload(ChunkEvent.Unload event) {
		if(RadiationConfig.enableChunkRads) proxy.receiveChunkUnload(event);
	}
	
	int eggTimer = 0;
	
	@SubscribeEvent
	public void updateSystem(TickEvent.ServerTickEvent event) {
		
		if(RadiationConfig.enableChunkRads && event.side == Side.SERVER && event.phase == Phase.END) {
			
			eggTimer++;
			
			if(eggTimer >= 20) {
				proxy.updateSystem();
				eggTimer = 0;
			}
			
			if(RadiationConfig.worldRadEffects) {
				proxy.handleWorldDestruction();
			}
			
			proxy.receiveWorldTick(event);
		}
	}
}
