package com.hbm.blocks.bomb;

import java.util.Random;

import com.hbm.blocks.machine.BlockPillar;
import com.hbm.entity.item.EntityTNTPrimedBase;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.explosion.ExplosionNT;
import com.hbm.explosion.ExplosionNT.ExAttrib;
import com.hbm.interfaces.IBomb;

import api.hbm.block.IFuckingExplode;
import net.minecraft.util.math.MathHelper;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class DetMiner extends BlockPillar implements IBomb, IFuckingExplode {

	public DetMiner(Material mat, String top) {
		super(mat, top);
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState i, Random rand, int j) {
		return null;
	}

	@Override
	public BombReturnCode explode(World world, int x, int y, int z) {

		if(!world.isRemote) {

			com.hbm.world.WorldBlockDestruction.destroy(world, x, y, z, false);
			ExplosionNT explosion = new ExplosionNT(world, null, x + 0.5, y + 0.5, z + 0.5, 4);
			explosion.atttributes.add(ExAttrib.ALLDROP);
			explosion.atttributes.add(ExAttrib.NOHURT);
			explosion.doExplosionA();
			explosion.doExplosionB(false);

			ExplosionLarge.spawnParticles(world, x + 0.5, y + 0.5, z + 0.5, 30);
		}

		return BombReturnCode.DETONATED;
	}

	public void onBlockDestroyedByExplosion(World world, int x, int y, int z, Explosion explosion) {
		if(!world.isRemote) {
			EntityTNTPrimedBase tntPrimed = new EntityTNTPrimedBase(world, x + 0.5D, y + 0.5D, z + 0.5D, explosion != null ? explosion.getExplosivePlacedBy() : null, this);
			tntPrimed.fuse = 0;
			tntPrimed.detonateOnCollision = false;
			world.spawnEntity(tntPrimed);
		}
	}

	@Override
	public void neighborChanged(net.minecraft.block.state.IBlockState nativeState, World world, net.minecraft.util.math.BlockPos nativePos, Block p_149695_5_, net.minecraft.util.math.BlockPos fromPos) {
		int x = nativePos.getX(); int y = nativePos.getY(); int z = nativePos.getZ();
		if(world.isBlockPowered(new net.minecraft.util.math.BlockPos(x, y, z))) {
			this.explode(world, x, y, z);
		}
	}

	@Override
	public void explodeEntity(World world, double x, double y, double z, EntityTNTPrimedBase entity) {
		explode(world, MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
	}
}
