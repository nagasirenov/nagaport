package com.hbm.blocks.gas;

import java.util.HashSet;
import java.util.Random;

import com.hbm.interfaces.Untested;
import com.hbm.util.Compat;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class BlockGasFlammable extends BlockGasBase {

	public static HashSet<Block> fireSources = new HashSet();

	public BlockGasFlammable() {
		super(0.8F, 0.8F, 0.2F);

		if(fireSources.isEmpty()) {
			fireSources.add(Blocks.FIRE);
			fireSources.add(Blocks.LAVA);
			fireSources.add(Blocks.TORCH);
			fireSources.add(Blocks.LIT_PUMPKIN);

			if(Compat.isModLoaded(Compat.MOD_TIC)) {
				Block stoneTorch = Compat.tryLoadBlock(Compat.MOD_TIC, "decoration.stonetorch");
				if(stoneTorch != null) {
					fireSources.add(stoneTorch);
				}
			}
		}
	}

	@Override
	public ForgeDirection getFirstDirection(World world, int x, int y, int z) {

		if(world.rand.nextInt(3) == 0)
			return ForgeDirection.getOrientation(world.rand.nextInt(2));

		return this.randomHorizontal(world);
	}

	@Override
	public ForgeDirection getSecondDirection(World world, int x, int y, int z) {
		return this.randomHorizontal(world);
	}


		@Override
	public void updateTick(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, java.util.Random rand) {
		updateTick(worldIn, pos.getX(), pos.getY(), pos.getZ(), rand);
	}

public void updateTick(World world, int x, int y, int z, Random rand) {

		if(!world.isRemote) {

			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				Block b = com.hbm.world.WorldBlockState.read(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ).getBlock();

				if(isFireSource(b)) {
					combust(world, x, y, z);
					return;
				}
			}

			if(rand.nextInt(20) == 0 && com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() == Blocks.AIR) {
				com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
				return;
			}
		}

		super.updateTick(world, x, y, z, rand);
	}


		@Override
	public void onEntityCollision(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, net.minecraft.entity.Entity entityIn) {
		onEntityCollidedWithBlock(worldIn, pos.getX(), pos.getY(), pos.getZ(), entityIn);
	}

public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {

		/* causes burning entities to set off the gas */
		if(!world.isRemote && entity.isBurning()) {
			this.combust(world, x, y, z);
		}
	}

	@Untested
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			Block b = com.hbm.world.WorldBlockState.read(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ).getBlock();

			if(isFireSource(b)) {
				world.scheduleUpdate(pos, this, 2);
			}
		}
	}

	protected void combust(World world, int x, int y, int z) {
		{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.FIRE; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
	}

	public boolean isFireSource(Block b) {
		return this.fireSources.contains(b);
	}

	@Override
	public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return true;
	}

	@Override
	public int getDelay(World world) {
		return world.rand.nextInt(5) + 16;
	}
}
