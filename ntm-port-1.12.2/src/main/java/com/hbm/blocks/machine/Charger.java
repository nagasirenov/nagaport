package com.hbm.blocks.machine;

import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
import com.hbm.tileentity.machine.TileEntityCharger;
import com.hbm.world.gen.nbt.INBTBlockTransformable;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class Charger extends BlockContainer implements INBTBlockTransformable {

	public Charger(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityCharger();
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

		if(i == 0) {
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 2, 2);
		}
		if(i == 1) {
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 5, 2);
		}
		if(i == 2) {
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 3, 2);
		}
		if(i == 3) {
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 4, 2);
		}
	}

	private static final AxisAlignedBB BOUNDS_DEFAULT = new AxisAlignedBB(5 * 0.0625D, 0.25D, 5 * 0.0625D, 11 * 0.0625D, 0.75D, 11 * 0.0625D);
	private static final AxisAlignedBB BOUNDS_NORTH = new AxisAlignedBB(5 * 0.0625D, 0.25D, 12 * 0.0625D, 11 * 0.0625D, 0.75D, 1.0D);
	private static final AxisAlignedBB BOUNDS_SOUTH = new AxisAlignedBB(5 * 0.0625D, 0.25D, 0.0D, 11 * 0.0625D, 0.75D, 4 * 0.0625D);
	private static final AxisAlignedBB BOUNDS_WEST = new AxisAlignedBB(12 * 0.0625D, 0.25D, 5 * 0.0625D, 1.0D, 0.75D, 11 * 0.0625D);
	private static final AxisAlignedBB BOUNDS_EAST = new AxisAlignedBB(0.0D, 0.25D, 5 * 0.0625D, 4 * 0.0625D, 0.75D, 11 * 0.0625D);

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		switch(com.hbm.world.WorldBlockState.readMetadata(source, pos.getX(), pos.getY(), pos.getZ())) {
		case 2: return BOUNDS_NORTH;
		case 3: return BOUNDS_SOUTH;
		case 4: return BOUNDS_WEST;
		case 5: return BOUNDS_EAST;
		default: return BOUNDS_DEFAULT;
		}
	}

	@Override
	public int transformMeta(int meta, int coordBaseMode) {
		return INBTBlockTransformable.transformMetaDeco(meta, coordBaseMode);
	}
}
