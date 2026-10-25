package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.saveddata.TomSaveData;

import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

/*
 *   ___________
 *  /           \
 * |\___________/|
 * | ,           |
 * | |         ` |
 * | |           |
 * | '         . |
 *  \___________/
 *
 *      PU-238
 *
 */
public class BlockDirt extends Block {
	public <T extends BlockDirt> T withSoundType(net.minecraft.block.SoundType sound) {
		super.setSoundType(sound);
		return (T) this;
	}


	public BlockDirt(Material mat) {
		super(mat);
	}

	public BlockDirt(Material mat, boolean tick) {
		super(mat);
		this.setTickRandomly(tick);
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState i, Random rand, int j) {
		return Item.getItemFromBlock(Blocks.DIRT);
	}

	/*@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {

		for(int i = -1; i < 2; i++) {
			for(int j = -1; j < 2; j++) {
				for(int k = -1; k < 2; k++) {
					Block b = world.getBlock(x + i, y + j, z + k);
					if(b instanceof BlockGrass) {
						com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, Blocks.DIRT);
					}
				}
			}
		}
	}*/

	@Override
	public void updateTick(World world, net.minecraft.util.math.BlockPos nativeTickPos, net.minecraft.block.state.IBlockState nativeTickState, Random rand) {
		int x = nativeTickPos.getX(), y = nativeTickPos.getY(), z = nativeTickPos.getZ();

		if(!world.isRemote) {
			TomSaveData data = TomSaveData.forWorld(world);

			int light = Math.max(world.getLightFor(EnumSkyBlock.BLOCK, new net.minecraft.util.math.BlockPos(x, y + 1, z)), (int) (world.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, new net.minecraft.util.math.BlockPos(x, y + 1, z)) * (1 - data.dust)));
			if(light >= 9 && data.fire == 0) {
				{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.GRASS; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
				if(com.hbm.world.WorldBlockState.read(world, x, y-1, z).getBlock()==Blocks.DIRT)
				{
					com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y-1, z, ModBlocks.impact_dirt);
				}
			}
		}
	}
}
