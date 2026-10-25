package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.BlockEnumMulti;
import com.hbm.blocks.ModBlocks;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class BlockDeadPlant extends BlockEnumMulti {

	public BlockDeadPlant() {
		super(Material.PLANTS, EnumDeadPlantType.class, false, true);
	}

	public static enum EnumDeadPlantType {
		GENERIC,
		GRASS,
		FLOWER,
		BIGFLOWER,
		FERN
	}

	@Override
	public boolean canPlaceBlockAt(World world, net.minecraft.util.math.BlockPos nativePlacementPos) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
		return super.canPlaceBlockAt(world, new net.minecraft.util.math.BlockPos(x, y, z)) && this.canBlockStay(world, x, y, z);
	}

	protected boolean canPlaceBlockOn(Block block) {
		return block == Blocks.GRASS || block == Blocks.DIRT || block == ModBlocks.waste_earth || block == ModBlocks.dirt_oily || block == ModBlocks.dirt_dead;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		super.neighborChanged(state, world, pos, block, fromPos);
		this.checkAndDropBlock(world, pos.getX(), pos.getY(), pos.getZ());
	}

	protected void checkAndDropBlock(World world, int x, int y, int z) {
		if(!this.canBlockStay(world, x, y, z)) {
			this.dropBlockAsItem(world, new net.minecraft.util.math.BlockPos(x, y, z), com.hbm.world.WorldBlockState.read(world, x, y, z), 0);
			com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, getBlockById(0), 0, 2);
		}
	}


	public boolean canBlockStay(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {
		return canBlockStay(worldIn, pos.getX(), pos.getY(), pos.getZ());
	}

public boolean canBlockStay(World world, int x, int y, int z) {
		return canPlaceBlockOn(com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock());
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
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
	public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.MODEL;
	}

	@Override
	public int damageDropped(int meta) {
		return meta;
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState meta, Random rand, int fortune) {
		return null;
	}
}
