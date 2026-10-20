package com.hbm.blocks.bomb;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class DigammaMatter extends Block {
	
	private static Random rand = new Random(); 

	public DigammaMatter() {
		super(Material.PORTAL);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World p_149668_1_, int p_149668_2_, int p_149668_3_, int p_149668_4_) {
		return null;
	}

	@Override
	public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}
	
	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		float pixel = 0.0625F;
		this.setBlockBounds(rand.nextInt(9) * pixel, rand.nextInt(9) * pixel, rand.nextInt(9) * pixel, 1.0F - rand.nextInt(9) * pixel, 1.0F - rand.nextInt(9) * pixel, 1.0F - rand.nextInt(9) * pixel);
	}
	
	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		
		if(!world.isRemote)
			world.scheduleUpdate(new net.minecraft.util.math.BlockPos(x, y, z), this, 10 + rand.nextInt(40));
	}

	@Override
	public void updateTick(World world, net.minecraft.util.math.BlockPos nativeTickPos, net.minecraft.block.state.IBlockState nativeTickState, Random rand) {
		int x = nativeTickPos.getX(), y = nativeTickPos.getY(), z = nativeTickPos.getZ();

		if(!world.isRemote) {
			
			int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );
			
			if(meta >= 7) {
				{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			} else {

				{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
				//world.setBlock(x, y, z, this, meta + 1, 3);
				
				for(int i = -1; i <= 1; i++) {
					for(int j = -1; j <= 1; j++) {
						for(int k = -1; k <= 1; k++) {
							
							int dist = Math.abs(i) + Math.abs(j) + Math.abs(k);
							
							if(dist > 0 && dist < 3) {
								if(com.hbm.world.WorldBlockState.read(world, x + i, y + j, z + k).getBlock() != this)
									world.setBlock(x + i, y + j, z + k, this, meta + 1, 3);
							}
						}
					}
				}
			}
		}
	}
}
