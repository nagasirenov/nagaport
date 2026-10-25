package com.hbm.blocks.machine;

import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
import com.hbm.tileentity.machine.TileEntityBroadcaster;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class PinkCloudBroadcaster extends BlockContainer {

	public PinkCloudBroadcaster(Material p_i45386_1_) {
		super(p_i45386_1_);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityBroadcaster();
	}

	@Override
	public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.INVISIBLE;
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
	public void onBlockPlacedBy(World world, net.minecraft.util.math.BlockPos nativePlacementPos, net.minecraft.block.state.IBlockState nativePlacementState, EntityLivingBase player, ItemStack itemStack) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
		int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

		if(i == 0)
		{
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 3, 2);
		}
		if(i == 1)
		{
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 4, 2);
		}
		if(i == 2)
		{
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 2, 2);
		}
		if(i == 3)
		{
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 5, 2);
		}
	}

	private static final AxisAlignedBB BOUNDS_DEFAULT = new AxisAlignedBB(0.0D, 0.0D, 2 * 0.0625D, 1.0D, 1.0D, 14 * 0.0625D);
	private static final AxisAlignedBB BOUNDS_EW = new AxisAlignedBB(4 * 0.0625D, 0.0D, 1 * 0.0625D, 12 * 0.0625D, 10 * 0.0625D, 15 * 0.0625D);
	private static final AxisAlignedBB BOUNDS_NS = new AxisAlignedBB(1 * 0.0625D, 0.0D, 4 * 0.0625D, 15 * 0.0625D, 10 * 0.0625D, 12 * 0.0625D);

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		int te = com.hbm.world.WorldBlockState.readMetadata(source, pos.getX(), pos.getY(), pos.getZ());
		switch(te) {
		case 4:
		case 5:
			return BOUNDS_EW;
		case 2:
		case 3:
			return BOUNDS_NS;
		default:
			return BOUNDS_DEFAULT;
		}
	}
}
