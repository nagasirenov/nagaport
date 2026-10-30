package com.hbm.blocks.machine;

import com.hbm.tileentity.machine.TileEntityICFStruct;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BlockICFStruct extends BlockContainer {
	// TESR skips the core cell; 1.7.10 type 0 cube body stays.
	@Override
	public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.MODEL;
	}


	public BlockICFStruct(Material mat) { super(mat); }
	@Override public TileEntity createNewTileEntity(World world, int meta) { return new TileEntityICFStruct(); }
	@Override public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) { return false; }

	@Override
	public void onBlockPlacedBy(World world, net.minecraft.util.math.BlockPos nativePlacementPos, net.minecraft.block.state.IBlockState nativePlacementState, EntityLivingBase player, ItemStack itemStack) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
		int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		if(i == 0) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 2, 2);
		if(i == 1) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 5, 2);
		if(i == 2) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 3, 2);
		if(i == 3) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 4, 2);
	}
}
