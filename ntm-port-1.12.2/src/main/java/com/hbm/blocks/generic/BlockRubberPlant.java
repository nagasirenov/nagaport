package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.BlockEnumMulti;
import com.hbm.blocks.ModBlocks;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class BlockRubberPlant extends BlockEnumMulti {

	public BlockRubberPlant() {
		super(Material.VINE, EnumRubberPlantType.class, false, true);
	}

	public static enum EnumRubberPlantType {
		GRASS,
		FERN,
		SHRUB
	}

	@Override
	public boolean canPlaceBlockAt(World world, net.minecraft.util.math.BlockPos nativePlacementPos) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
		return super.canPlaceBlockAt(world, new net.minecraft.util.math.BlockPos(x, y, z)) && this.canBlockStay(world, x, y, z);
	}

	protected boolean canPlaceBlockOn(Block block) {
		return block == ModBlocks.rubber_grass || block == ModBlocks.rubber_silt || block == ModBlocks.rubber_farmland;
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

	private static final AxisAlignedBB RUBBER_PLANT_AABB = new AxisAlignedBB(0.1D, 0.0D, 0.1D, 0.9D, 0.8D, 0.9D);

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, net.minecraft.world.IBlockAccess source, BlockPos pos) {
		return RUBBER_PLANT_AABB;
	}

	@Override
	public net.minecraft.block.state.BlockFaceShape getBlockFaceShape(net.minecraft.world.IBlockAccess worldIn, IBlockState state, BlockPos pos, net.minecraft.util.EnumFacing face) {
		return net.minecraft.block.state.BlockFaceShape.UNDEFINED;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos) {
		return NULL_AABB;
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

	@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public net.minecraft.util.BlockRenderLayer getRenderLayer() {
		return net.minecraft.util.BlockRenderLayer.CUTOUT;
	}
}
