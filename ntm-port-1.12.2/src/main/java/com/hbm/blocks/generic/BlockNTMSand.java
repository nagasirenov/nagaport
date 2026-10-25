package com.hbm.blocks.generic;

import java.util.Locale;
import java.util.Random;

import com.hbm.blocks.BlockEnumMulti;
import com.hbm.lib.RefStrings;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.world.World;

public class BlockNTMSand extends BlockEnumMulti {

	public BlockNTMSand(Material mat) {
		super(mat, EnumSandType.class, true, true);
	}

	@Override
	public void onBlockAdded(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		world.scheduleUpdate(new net.minecraft.util.math.BlockPos(x, y, z), this, this.tickRate(world));
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block b, BlockPos fromPos) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		world.scheduleUpdate(new net.minecraft.util.math.BlockPos(x, y, z), this, this.tickRate(world));
	}

	@Override
	public void updateTick(World world, net.minecraft.util.math.BlockPos nativeTickPos, net.minecraft.block.state.IBlockState nativeTickState, Random rand) {
		int x = nativeTickPos.getX(), y = nativeTickPos.getY(), z = nativeTickPos.getZ();
		if(!world.isRemote) {
			this.fall(world, x, y, z);
		}
	}

	private void fall(World world, int x, int y, int z) {
		if(BlockFalling.canFallThrough(com.hbm.world.WorldBlockState.read(world, x, y - 1, z)) && y >= 0) {
			byte b0 = 32;

			if(!BlockFalling.fallInstantly && world.isAreaLoaded(new net.minecraft.util.math.BlockPos(x - b0, y - b0, z - b0), new net.minecraft.util.math.BlockPos(x + b0, y + b0, z + b0))) {
				if(!world.isRemote) {
					EntityFallingBlock entityfallingblock = new EntityFallingBlock(world, x + 0.5, y + 0.5, z + 0.5, com.hbm.world.WorldBlockState.read(world, x, y, z));
					world.spawnEntity(entityfallingblock);
				}
			} else {
				com.hbm.world.WorldAirRemoval.remove(world, x, y, z);

				while(BlockFalling.canFallThrough(com.hbm.world.WorldBlockState.read(world, x, y - 1, z)) && y > 0) {
					--y;
				}

				if(y > 0) {
					{ net.minecraft.world.World nativeRegisteredWriteWorld = world; int nativeRegisteredWriteX = x; int nativeRegisteredWriteY = y; int nativeRegisteredWriteZ = z; net.minecraft.block.Block nativeRegisteredWriteBlock = this; int nativeRegisteredWriteMeta = 0; int nativeRegisteredWriteFlags = 3; if (nativeRegisteredWriteWorld == null) throw new NullPointerException(); if (nativeRegisteredWriteX >= -30000000 && nativeRegisteredWriteX < 30000000 && nativeRegisteredWriteZ >= -30000000 && nativeRegisteredWriteZ < 30000000 && nativeRegisteredWriteY >= 0 && nativeRegisteredWriteY < 256) com.hbm.world.WorldBlockState.write(nativeRegisteredWriteWorld, nativeRegisteredWriteX, nativeRegisteredWriteY, nativeRegisteredWriteZ, nativeRegisteredWriteBlock.getStateFromMeta(nativeRegisteredWriteMeta), nativeRegisteredWriteFlags); }
				}
			}
		}
	}

	@Override
	public int tickRate(World p_149738_1_) {
		return 2;
	}

	@Override
	public String getTextureMultiName(Enum num) {
		return RefStrings.MODID + ":sand_" + num.name().toLowerCase(Locale.US);
	}

	@Override
	public String getUnlocalizedMultiName(Enum num) {
		return "tile.sand_" + num.name().toLowerCase(Locale.US);
	}

	public static enum EnumSandType {
		BORON, LEAD, URANIUM, POLONIUM, QUARTZ
	}
}
