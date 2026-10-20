package com.hbm.explosion.vanillant.standard;

import com.hbm.blocks.ModBlocks;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.interfaces.IBlockMutator;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockMutatorBalefire implements IBlockMutator {

	@Override public void mutatePre(ExplosionVNT explosion, Block block, int meta, int x, int y, int z) { }

	@Override
	public void mutatePost(ExplosionVNT explosion, int x, int y, int z) {

		Block block = com.hbm.world.WorldBlockState.read(explosion.world, x, y, z).getBlock();
		Block block1 = com.hbm.world.WorldBlockState.read(explosion.world, x, y - 1, z).getBlock();
		if(block.getDefaultState().getMaterial() == Material.AIR && block1.func_149730_j() && explosion.world.rand.nextInt(3) == 0) {
			explosion.world.setBlock(x, y, z, ModBlocks.balefire);
		}
	}
}
