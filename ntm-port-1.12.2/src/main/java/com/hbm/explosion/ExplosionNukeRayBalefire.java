package com.hbm.explosion;

import com.hbm.blocks.ModBlocks;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class ExplosionNukeRayBalefire extends ExplosionNukeRayBatched {

	public ExplosionNukeRayBalefire(World world, int x, int y, int z, int strength, int speed, int length) {
		super(world, x, y, z, strength, speed, length);
	}

	protected void handleTip(int x, int y, int z) {

		if(world.rand.nextInt(5) == 0 && com.hbm.lib.FacingCompat.isBlockSideSolid(world, x, y - 1, z, ForgeDirection.UP)) {
			com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, ModBlocks.balefire, 0, 3);
		} else {
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		}
	}
}
