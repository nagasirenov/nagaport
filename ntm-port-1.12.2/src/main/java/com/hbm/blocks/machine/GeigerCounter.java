package com.hbm.blocks.machine;

import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
import com.hbm.tileentity.machine.TileEntityGeiger;
import com.hbm.util.ContaminationUtil;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class GeigerCounter extends BlockContainer {

	public GeigerCounter(Material p_i45386_1_) {
		super(p_i45386_1_);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityGeiger();
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
	private static final AxisAlignedBB BOUNDS_WEST = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 14 * 0.0625D, 9 * 0.0625D, 14.5D * 0.0625D);
	private static final AxisAlignedBB BOUNDS_NORTH = new AxisAlignedBB(1.5D * 0.0625D, 0.0D, 0.0D, 1.0D, 9 * 0.0625D, 14 * 0.0625D);
	private static final AxisAlignedBB BOUNDS_EAST = new AxisAlignedBB(2 * 0.0625D, 0.0D, 1.5D * 0.0625D, 1.0D, 9 * 0.0625D, 1.0D);
	private static final AxisAlignedBB BOUNDS_SOUTH = new AxisAlignedBB(0.0D, 0.0D, 2 * 0.0625D, 14.5D * 0.0625D, 9 * 0.0625D, 1.0D);

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		int te = com.hbm.world.WorldBlockState.readMetadata(source, pos.getX(), pos.getY(), pos.getZ());
		switch(te) {
		case 4: return BOUNDS_WEST;
		case 2: return BOUNDS_NORTH;
		case 5: return BOUNDS_EAST;
		case 3: return BOUNDS_SOUTH;
		default: return BOUNDS_DEFAULT;
		}
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;


		if(world.isRemote)
		{
			return true;
		} else if(!player.isSneaking())
		{
	    	world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:item.techBoop"), net.minecraft.util.SoundCategory.PLAYERS, 1.0F, 1.0F);
	    	ContaminationUtil.printGeigerData(player);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean hasComparatorInputOverride(net.minecraft.block.state.IBlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(net.minecraft.block.state.IBlockState state, World world, net.minecraft.util.math.BlockPos blockPos) {
		int x = blockPos.getX();
		int y = blockPos.getY();
		int z = blockPos.getZ();
		TileEntityGeiger te = (TileEntityGeiger)world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
		if (te == null) return 0;

		float rad = te.check();

		// 0 at exactly 0 rads/sec
		// +1 per 5 rads/sec
		// 15 at 75+ rads/sec
		return Math.min((int)Math.ceil(rad / 5f), 15);
	}
}
