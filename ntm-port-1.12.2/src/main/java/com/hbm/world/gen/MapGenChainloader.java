package com.hbm.world.gen;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.InitMapGenEvent.EventType;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MapGenChainloader extends MapGenBase {

	/**
	 * There is no way - with existing forge hooks - to add entirely new `MapGenBase` generators to existing dimensions (overworld, nether).
	 * So, in order to fix that (while not breaking mods like Greg's Caves), we chainload our own MapGenBase into the existing cave generator
	 */

	private MapGenBase parent;

	private List<MapGenBase> generators = new ArrayList<>();

	// These may be added to before OR after mapgen registration safely
	private static List<MapGenBase> overworldGenerators = new ArrayList<>();
	private static List<MapGenBase> netherGenerators = new ArrayList<>();

	// Executes our chainloaded parent, and all our child generators
	public void generate(World world, int chunkX, int chunkZ, ChunkPrimer primer) {
		parent.generate(world, chunkX, chunkZ, primer);

		// Some mods may use vanilla gen events for added dimensions, so we guard against that here
		int dimension = world.provider.getDimension();
		if(dimension != 0 && dimension != -1) return;

		for(MapGenBase generator : generators) {
			generator.generate(world, chunkX, chunkZ, primer);
		}
	}

	public static void register() {
		MapGenEventHandler handler = new MapGenEventHandler();
		MinecraftForge.TERRAIN_GEN_BUS.register(handler);
	}

	public static void addOverworldGenerator(MapGenBase generator) {
		if(overworldGenerators.contains(generator)) return;
		overworldGenerators.add(generator);
	}

	public static void addNetherGenerator(MapGenBase generator) {
		if(netherGenerators.contains(generator)) return;
		netherGenerators.add(generator);
	}

	public static class MapGenEventHandler {

		// Register as late as possible to pick up any modded cave generators
		@SubscribeEvent(priority = EventPriority.LOWEST)
		public void addMapGenChainloader(InitMapGenEvent event) {
			MapGenBase original = event.getNewGen();
			if(original == null || original instanceof MapGenChainloader) return;

			if(event.getType() == EventType.CAVE) {
				MapGenChainloader loader = new MapGenChainloader();
				loader.parent = original;
				loader.generators = overworldGenerators;
				event.setNewGen(loader);
			} else if(event.getType() == EventType.NETHER_CAVE) {
				MapGenChainloader loader = new MapGenChainloader();
				loader.parent = original;
				loader.generators = netherGenerators;
				event.setNewGen(loader);
			}
		}

	}

}
