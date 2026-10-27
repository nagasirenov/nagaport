package com.hbm.blocks.generic;

import java.util.ArrayList;
import java.util.Random;

import com.hbm.blocks.BlockEnumMulti;
import com.hbm.blocks.BlockEnums;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class BlockStalagmite extends BlockEnumMulti {

	public BlockStalagmite() {
		super(Material.ROCK, BlockEnums.EnumStalagmiteType.class, true, true);
	}

	@Override
	public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	public int getRenderType() {
		return 1;
	}

	@Override
	public net.minecraft.item.Item getItemDropped(net.minecraft.block.state.IBlockState state, Random rang, int fortune) {
		int meta = state.getBlock().getMetaFromState(state);

		switch(meta) {
		case 0: return ModItems.sulfur;
		case 1: return ModItems.powder_asbestos;
		case 2: return ModItems.flesh;
		}

		return null;
	}

	public static int getMetaFromResource(Block block, int meta) {
		if(block == ModBlocks.tumor) return 2;
		return meta;
	}

	@Override
	public boolean canPlaceBlockAt(World world, net.minecraft.util.math.BlockPos nativePlacementPos) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();

		if(this == ModBlocks.stalagmite) {
			net.minecraft.block.state.IBlockState nativeSupportState = com.hbm.world.WorldBlockState.read(world, x, y - 1, z);
			return nativeSupportState.getBlock().isSideSolid(nativeSupportState, world, new net.minecraft.util.math.BlockPos(x, y - 1, z), net.minecraft.util.EnumFacing.UP);
		}
		if(this == ModBlocks.stalactite) return com.hbm.lib.FacingCompat.isWorldSideSolid(world, x, y + 1, z, ForgeDirection.DOWN);

		return true;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		if(!canPlaceBlockAt(world, new net.minecraft.util.math.BlockPos(x, y, z))) {
			com.hbm.world.WorldBlockDestruction.destroy(world, x, y, z, true);
		}
	}

	private static final AxisAlignedBB STALAG_AABB = new AxisAlignedBB(0.1D, 0.0D, 0.1D, 0.9D, 1.0D, 0.9D);

	@Override
	public net.minecraft.util.math.AxisAlignedBB getBoundingBox(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess source, net.minecraft.util.math.BlockPos pos) {
		return STALAG_AABB;
	}

	@Override
	public net.minecraft.block.state.BlockFaceShape getBlockFaceShape(net.minecraft.world.IBlockAccess worldIn, IBlockState state, BlockPos pos, net.minecraft.util.EnumFacing face) {
		return net.minecraft.block.state.BlockFaceShape.UNDEFINED;
	}

	@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public net.minecraft.util.BlockRenderLayer getRenderLayer() {
		return net.minecraft.util.BlockRenderLayer.CUTOUT;
	}

	@Override
	public net.minecraft.util.math.AxisAlignedBB getCollisionBoundingBox(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess worldIn, net.minecraft.util.math.BlockPos pos) {
		return NULL_AABB;
	}


		@Override
	public void getDrops(net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> drops, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, int fortune) {
		int meta = state.getBlock().getMetaFromState(state);
		net.minecraft.world.World w = world instanceof net.minecraft.world.World ? (net.minecraft.world.World) world : null;
		java.util.ArrayList<net.minecraft.item.ItemStack> ret = getDrops(w, pos.getX(), pos.getY(), pos.getZ(), meta, fortune);
		if(ret != null) drops.addAll(ret);
	}

public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		return ModBlocks.getDropsWithoutDamage(world, this, metadata, fortune);
	}

}
