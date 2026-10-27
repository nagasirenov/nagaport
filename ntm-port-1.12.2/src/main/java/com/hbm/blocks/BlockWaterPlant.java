package com.hbm.blocks;

import java.util.Random;

import com.hbm.items.ModItems;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class BlockWaterPlant extends Block {

	public BlockWaterPlant withSoundType(net.minecraft.block.SoundType sound) {
		super.setSoundType(sound);
		return this;
	}

	//once carbon tetrachloride exists, this will be rewritten. i just want my damn plants already ffs...
	protected BlockWaterPlant() {
		super(Material.PLANTS);
	}

	public int getRenderType() {
		return 6;
	}

	@Override
	public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	private static final AxisAlignedBB WATER_PLANT_AABB = new AxisAlignedBB(0.1D, 0.0D, 0.1D, 0.9D, 0.8D, 0.9D);

	@Override
	public AxisAlignedBB getBoundingBox(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess source, net.minecraft.util.math.BlockPos pos) {
		return WATER_PLANT_AABB;
	}

	@Override
	public net.minecraft.block.state.BlockFaceShape getBlockFaceShape(net.minecraft.world.IBlockAccess worldIn, net.minecraft.block.state.IBlockState state, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumFacing face) {
		return net.minecraft.block.state.BlockFaceShape.UNDEFINED;
	}

	@Override
	public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.MODEL;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos) {
		return NULL_AABB;
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState parMetadata, Random parRand, int parFortune)   {
	   return (ModItems.saltleaf);
	}

	public int quantityDropped(net.minecraft.block.state.IBlockState state, int fortune, java.util.Random random) {
		return random.nextInt(4);
	}

	@Override
	public boolean canPlaceBlockAt(World world, net.minecraft.util.math.BlockPos nativePlacementPos) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
		return super.canPlaceBlockAt(world, new net.minecraft.util.math.BlockPos(x, y, z)) && com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock().getDefaultState().getMaterial().isLiquid();
	}


	@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public net.minecraft.util.BlockRenderLayer getRenderLayer() {
		return net.minecraft.util.BlockRenderLayer.CUTOUT;
	}
}
