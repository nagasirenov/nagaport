package com.hbm.blocks.bomb;

import com.hbm.blocks.generic.BlockFlammable;
import com.hbm.entity.item.EntityTNTPrimedBase;

import api.hbm.block.IFuckingExplode;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.material.Material;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public abstract class BlockDetonatable extends BlockFlammable implements IFuckingExplode {

	protected int popFuse; // A shorter fuse for when this explosive is dinked by another
	protected boolean detonateOnCollision;
	protected boolean detonateOnShot;

	public BlockDetonatable(Material mat, int en, int flam, int popFuse, boolean detonateOnCollision, boolean detonateOnShot) {
		super(mat, en, flam);
		this.popFuse = popFuse;
		this.detonateOnCollision = detonateOnCollision;
		this.detonateOnShot = detonateOnShot;
	}

	@Override
	public void onExplosionDestroy(World world, BlockPos pos, Explosion explosion) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		if(!world.isRemote) {
			EntityTNTPrimedBase tntPrimed = new EntityTNTPrimedBase(world, x + 0.5D, y + 0.5D, z + 0.5D, explosion != null ? explosion.getExplosivePlacedBy() : null, this);
			tntPrimed.fuse = popFuse <= 0 ? 0 : world.rand.nextInt(popFuse) + popFuse / 2;
			tntPrimed.detonateOnCollision = detonateOnCollision;
			world.spawnEntity(tntPrimed);
		}
	}

	@Override
	public boolean canDropFromExplosion(Explosion explosion) {
		return false;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		if(!world.isRemote && shouldIgnite(world, x, y, z)) {
			world.setBlockToAir(new BlockPos(x, y, z));
			onExplosionDestroy(world, pos, null);
		}
	}

	public void onShot(World world, int x, int y, int z) {
		if (!detonateOnShot) return;

		world.setBlockToAir(new BlockPos(x, y, z));
		explodeEntity(world, x, y, z, null); // insta-explod
	}

}
