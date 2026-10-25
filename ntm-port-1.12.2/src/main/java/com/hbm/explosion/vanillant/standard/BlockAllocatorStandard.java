package com.hbm.explosion.vanillant.standard;

import java.util.HashSet;

import com.hbm.explosion.LegacyHashBlockPos;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.interfaces.IBlockAllocator;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockAllocatorStandard implements IBlockAllocator {

	protected int resolution;

	public BlockAllocatorStandard() {
		this(16);
	}

	public BlockAllocatorStandard(int resolution) {
		this.resolution = resolution;
	}

	@Override
	public HashSet<BlockPos> allocate(ExplosionVNT explosion, World world, double x, double y, double z, float size) {

		HashSet<BlockPos> affectedBlocks = new HashSet();

		for(int i = 0; i < this.resolution; ++i) {
			for(int j = 0; j < this.resolution; ++j) {
				for(int k = 0; k < this.resolution; ++k) {
					
					if(i == 0 || i == this.resolution - 1 || j == 0 || j == this.resolution - 1 || k == 0 || k == this.resolution - 1) {
						
						double d0 = (double) ((float) i / ((float) this.resolution - 1.0F) * 2.0F - 1.0F);
						double d1 = (double) ((float) j / ((float) this.resolution - 1.0F) * 2.0F - 1.0F);
						double d2 = (double) ((float) k / ((float) this.resolution - 1.0F) * 2.0F - 1.0F);
						double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
						
						d0 /= d3;
						d1 /= d3;
						d2 /= d3;
						
						float powerRemaining = size * (0.7F + world.rand.nextFloat() * 0.6F);
						double currentX = x;
						double currentY = y;
						double currentZ = z;

						for(float stepSize = 0.3F; powerRemaining > 0.0F; powerRemaining -= stepSize * 0.75F) {
							
							int blockX = MathHelper.floor(currentX);
							int blockY = MathHelper.floor(currentY);
							int blockZ = MathHelper.floor(currentZ);
							
							Block block = com.hbm.world.WorldBlockState.read(world, blockX, blockY, blockZ).getBlock();

							if(block.getDefaultState().getMaterial() != Material.AIR) {
								float blockResistance = block.getExplosionResistance(world, new net.minecraft.util.math.BlockPos(blockX, blockY, blockZ), explosion.exploder, explosion.compat);
								powerRemaining -= (blockResistance + 0.3F) * stepSize;
							}

							if(powerRemaining > 0.0F && explosion.exploder == null) {
								affectedBlocks.add(new LegacyHashBlockPos(blockX, blockY, blockZ));
							}

							currentX += d0 * (double) stepSize;
							currentY += d1 * (double) stepSize;
							currentZ += d2 * (double) stepSize;
						}
					}
				}
			}
		}
		
		return affectedBlocks;
	}
}
