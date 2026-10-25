package com.hbm.explosion.vanillant.standard;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.interfaces.IBlockMutator;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;

public class BlockMutatorFire implements IBlockMutator {

	@Override public void mutatePre(ExplosionVNT explosion, Block block, int meta, int x, int y, int z) { }

	@Override
	public void mutatePost(ExplosionVNT explosion, int x, int y, int z) {

		Block block = com.hbm.world.WorldBlockState.read(explosion.world, x, y, z).getBlock();
		Block block1 = com.hbm.world.WorldBlockState.read(explosion.world, x, y - 1, z).getBlock();
		if(block.getDefaultState().getMaterial() == Material.AIR && block1.getDefaultState().isFullBlock() && explosion.world.rand.nextInt(3) == 0) {
			com.hbm.world.WorldBlockState.write(explosion.world, x, y, z, Blocks.FIRE.getStateFromMeta(0), 3);
		}
	}
}
