package com.hbm.handler;

import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.saveddata.TomSaveData;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;

import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockVine;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import com.hbm.lib.ForgeDirection;

public class ImpactWorldHandler {

	public static void impactEffects(World world) {

		if(!(world instanceof WorldServer))
			return;

		if(world.provider.getDimension() != 0) {
			return;
		}

		TomSaveData data = TomSaveData.forWorld(world);

		if (data.dust <= 0 && data.fire <= 0)
			return;

		WorldServer serv = (WorldServer) world;

		List<Chunk> list = serv.theChunkProviderServer.loadedChunks;
		int listSize = list.size();

		if(listSize > 0) {
			for(int i = 0; i < 3; i++) {

				Chunk chunk = list.get(serv.rand.nextInt(listSize));
				ChunkPos coord = chunk.getPos();

				for(int x = 0; x < 16; x++) {
					for(int z = 0; z < 16; z++) {

						if(world.rand.nextBoolean()) continue;

						int X = coord.getXStart() + x;
						int Z = coord.getZStart() + z;
						int Y = world.getHeightValue(X, Z) - world.rand.nextInt(Math.max(1, world.getHeightValue(X, Z)));


						if(data.dust > 0) {
							die(world, X, Y, Z);
						}
						if(data.fire > 0) {
							burn(world, X, Y, Z);
						}
					}
				}
			}
		}
	}

	/// Plants die without sufficient light.
	public static void die(World world, int x, int y, int z) {

		TomSaveData data = TomSaveData.forWorld(world);
		int light = Math.max(world.getSavedLightValue(EnumSkyBlock.Block, x, y + 1, z), (int) (world.getBlockLightValue(x, y + 1, z) * (1 - data.dust)));

		if(light < 4) {
			if(com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == Blocks.GRASS) {
				{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.DIRT; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			} else if(com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() instanceof BlockBush) {
				{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			} else if(com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() instanceof BlockLeaves) {
				{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			} else if(com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() instanceof BlockVine) {
				{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			}
		}
	}

	/// Burn the world.
	public static void burn(World world, int x, int y, int z) {

		Block b = com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock();
		if(com.hbm.lib.FacingCompat.isFlammable(b, world, x, y, z, ForgeDirection.UP) && com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() == Blocks.AIR && world.getSavedLightValue(EnumSkyBlock.Sky, x, y + 1, z) >= 7) {
			if(b instanceof BlockLeaves || b instanceof BlockBush) {
				com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
			}
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y + 1; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.FIRE; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }

		} else if((b == Blocks.GRASS || b == Blocks.MYCELIUM || b == ModBlocks.waste_earth || b == ModBlocks.frozen_grass || b == ModBlocks.waste_mycelium) &&
				!world.canLightningStrikeAt(x, y, z) && world.getSavedLightValue(EnumSkyBlock.Sky, x, y + 1, z) >= 7) {
			world.setBlock(x, y, z, ModBlocks.burning_earth);

		} else if(b == ModBlocks.frozen_dirt && world.getSavedLightValue(EnumSkyBlock.Sky, x, y + 1, z) >= 7) {
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.DIRT; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		}
	}

	public static World lastSyncWorld = null;
	public static float fire = 0F;
	public static float dust = 0F;
	public static long time = 0;
	public static boolean impact = false;

	@SideOnly(Side.CLIENT)
	public static float getFireForClient(World world) {
		if(world != lastSyncWorld) return 0F;
		return fire;
	}

	@SideOnly(Side.CLIENT)
	public static float getDustForClient(World world) {
		if(world != lastSyncWorld) return 0F;
		return dust;
	}

	@SideOnly(Side.CLIENT)
	public static boolean getImpactForClient(World world) {
		if(world != lastSyncWorld) return false;
		return impact;
	}

	@SideOnly(Side.CLIENT)
	public static long getTimeForClient(World world) {
		if(world != lastSyncWorld) return 0;
		return time;
	}

}
