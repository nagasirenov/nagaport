package com.hbm.handler.radiation;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.RadiationConfig;
import com.hbm.main.MainRegistry;

import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;

/**
 * Most basic implementation of a chunk radiation system: Each chunk has a radiation value which spreads out to its neighbors.
 * @author hbm
 */
public class ChunkRadiationHandlerSimple extends ChunkRadiationHandler {

	private Map<World, SimpleRadiationPerWorld> perWorld = new HashMap<>();
	private static final float maxRad = 100_000F;

	@Override
	public float getRadiation(World world, int x, int y, int z) {
		SimpleRadiationPerWorld radWorld = perWorld.get(world);

		if(radWorld != null) {
			ChunkPos coords = new ChunkPos(x >> 4, z >> 4);
			Float rad = radWorld.radiation.get(coords);
			return rad == null ? 0F : MathHelper.clamp(rad, 0, maxRad);
		}

		return 0;
	}

	@Override
	public void setRadiation(World world, int x, int y, int z, float rad) {
		SimpleRadiationPerWorld radWorld = perWorld.get(world);

		if(radWorld != null) {

			if(world.isBlockLoaded(new net.minecraft.util.math.BlockPos(x, 0, z))) {

				ChunkPos coords = new ChunkPos(x >> 4, z >> 4);
				radWorld.radiation.put(coords, MathHelper.clamp(rad, (float) (0), maxRad));
				world.getChunk(new net.minecraft.util.math.BlockPos(x, 0, z)).markDirty();
			}
		}
	}

	@Override
	public void incrementRad(World world, int x, int y, int z, float rad) {
		setRadiation(world, x, y, z, getRadiation(world, x, y, z) + rad * (float) RadiationConfig.chunkRadMultiplier);
	}

	@Override
	public void decrementRad(World world, int x, int y, int z, float rad) {
		setRadiation(world, x, y, z, Math.max(getRadiation(world, x, y, z) - rad, 0));
	}

	@Override
	public void updateSystem() {

		for(Entry<World, SimpleRadiationPerWorld> entry : perWorld.entrySet()) {

			Map<ChunkPos, Float> radiation = entry.getValue().radiation;
			Map<ChunkPos, Float> buff = new HashMap<>(radiation);
			radiation.clear();
			World world = entry.getKey();

			for(Entry<ChunkPos, Float> chunk : buff.entrySet()) {

				if(chunk.getValue() == 0)
					continue;

				ChunkPos coord = chunk.getKey();

				for(int i = -1; i <= 1; i++) {
					for(int j = -1; j<= 1; j++) {

						int type = Math.abs(i) + Math.abs(j);
						float percent = type == 0 ? 0.6F : type == 1 ? 0.075F : 0.025F;
						ChunkPos newCoord = new ChunkPos(coord.x + i, coord.z + j);

						if(buff.containsKey(newCoord)) {
							Float val = radiation.get(newCoord);
							float rad = val == null ? 0 : val;
							float newRad = rad + chunk.getValue() * percent;
							float decay = (float) RadiationConfig.chunkRadDecay; // 1 = original 0.99 / 0.05
							newRad = MathHelper.clamp(0F, newRad * (1F - 0.01F * decay) - 0.05F * decay, maxRad);
							radiation.put(newCoord, newRad);
						} else {
							radiation.put(newCoord, chunk.getValue() * percent);
						}

						float rad = radiation.get(newCoord);
						if(rad > RadiationConfig.fogRad && world != null && world.rand.nextInt(RadiationConfig.fogCh) == 0 && world.getChunkProvider().getLoadedChunk(coord.x, coord.z) != null) {

							int x = coord.x * 16 + world.rand.nextInt(16);
							int z = coord.z * 16 + world.rand.nextInt(16);
							int y = world.getHeight(x, z) + world.rand.nextInt(5);

							NBTTagCompound data = new NBTTagCompound();
							data.setString("type", "radFog");
							data.setDouble("posX", x);
							data.setDouble("posY", y);
							data.setDouble("posZ", z);
							MainRegistry.proxy.effectNT(data);
						}
					}
				}
			}
		}
	}

	@Override
	public void clearSystem(World world) {
		SimpleRadiationPerWorld radWorld = perWorld.get(world);

		if(radWorld != null) {
			radWorld.radiation.clear();
		}
	}

	@Override
	public void receiveWorldLoad(WorldEvent.Load event) {
		if(!event.getWorld().isRemote)
			perWorld.put(event.getWorld(), new SimpleRadiationPerWorld());
	}

	@Override
	public void receiveWorldUnload(WorldEvent.Unload event) {
		if(!event.getWorld().isRemote)
			perWorld.remove(event.getWorld());
	}

	private static final String NBT_KEY_CHUNK_RADIATION = "hfr_simple_radiation";

	@Override
	public void receiveChunkLoad(ChunkDataEvent.Load event) {

		if(!event.getWorld().isRemote) {
			SimpleRadiationPerWorld radWorld = perWorld.get(event.getWorld());

			if(radWorld != null) {
				radWorld.radiation.put(event.getChunk().getPos(), event.getData().getFloat(NBT_KEY_CHUNK_RADIATION));
			}
		}
	}

	@Override
	public void receiveChunkSave(ChunkDataEvent.Save event) {

		if(!event.getWorld().isRemote) {
			SimpleRadiationPerWorld radWorld = perWorld.get(event.getWorld());

			if(radWorld != null) {
				Float val = radWorld.radiation.get(event.getChunk().getPos());
				float rad = val == null ? 0F : val;
				event.getData().setFloat(NBT_KEY_CHUNK_RADIATION, rad);
			}
		}
	}

	@Override
	public void receiveChunkUnload(ChunkEvent.Unload event) {

		if(!event.getWorld().isRemote) {
			SimpleRadiationPerWorld radWorld = perWorld.get(event.getWorld());

			if(radWorld != null) {
				radWorld.radiation.remove(event.getChunk().getPos());
			}
		}
	}

	public static class SimpleRadiationPerWorld {

		public Map<ChunkPos, Float> radiation = new HashMap<>();
	}

	@Override
	public void handleWorldDestruction() {

		int count = 10;
		int threshold = 10;
		int chunks = 5;

		//for all worlds
		for(Entry<World, SimpleRadiationPerWorld> per : perWorld.entrySet()) {

			World world = per.getKey();
			SimpleRadiationPerWorld list = per.getValue();

			Object[] entries = list.radiation.entrySet().toArray();

			if(entries.length == 0)
				continue;

			//chose this many random chunks
			for(int c = 0; c < chunks; c++) {

				Entry<ChunkPos, Float> randEnt = (Entry<ChunkPos, Float>) entries[world.rand.nextInt(entries.length)];

				ChunkPos coords = randEnt.getKey();
				WorldServer serv = (WorldServer) world;
				ChunkProviderServer provider = (ChunkProviderServer) serv.getChunkProvider();

				//choose this many random locations within the chunk
				for(int i = 0; i < count; i++) {

					if(randEnt == null || randEnt.getValue() < threshold)
						continue;

					if(provider.getLoadedChunk(coords.x, coords.z) != null) {

						for(int a = 0; a < 16; a++) {
							for(int b = 0; b < 16; b++) {

								if(world.rand.nextInt(3) != 0)
									continue;

								int x = (coords.x << 4) + a;
								int z = (coords.z << 4) + b;
								int y = world.getHeight(x, z) - world.rand.nextInt(2);

								if(com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == Blocks.GRASS) {
									com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, ModBlocks.waste_earth);

								} else if(com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == Blocks.TALLGRASS) {
									{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }

								} else if(com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock().getDefaultState().getMaterial() == Material.LEAVES && !(com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == ModBlocks.waste_leaves)) {

									if(world.rand.nextInt(7) <= 5) {
										com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, ModBlocks.waste_leaves);
									} else {
										{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
