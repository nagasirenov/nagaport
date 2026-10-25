package com.hbm.blocks.machine;

import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
import com.hbm.items.ModItems;
import com.hbm.tileentity.machine.TileEntityRadiobox;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class Radiobox extends BlockContainer {

	public Radiobox(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int i) {
		return new TileEntityRadiobox();
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

		if(i == 0) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 2, 2);
		if(i == 1) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 5, 2);
		if(i == 2) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 3, 2);
		if(i == 3) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 4, 2);
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;

		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			TileEntityRadiobox box = (TileEntityRadiobox)world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

			if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null && com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() == ModItems.battery_spark && !box.infinite) {
				com.hbm.util.NativeHeldItem.getMainHandNullable(player).shrink(1);
				world.playSound(null, x + 0.5, y + 0.5, z + 0.5, com.hbm.main.ModSoundEvents.resolve("hbm:item.upgradePlug"), net.minecraft.util.SoundCategory.BLOCKS, 1.5F, 1.0F);
				box.infinite = true;
				box.markDirty();
				return true;
			}

			int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );
			if(meta <= 5) {
				com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, meta + 4, 2);
				com.hbm.sound.WorldSoundEffects.play(world, x, y, z, "hbm:block.reactorStart", 1.0F, 1.0F);
			} else {
				com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, meta - 4, 2);
				com.hbm.sound.WorldSoundEffects.play(world, x, y, z, "hbm:block.reactorStart", 1.0F, 0.85F);
			}

			return true;
		} else {
			return false;
		}
	}

	private static final AxisAlignedBB BOUNDS_DEFAULT = new AxisAlignedBB(0.0D, 0.0D, 2 * 0.0625D, 1.0D, 1.0D, 14 * 0.0625D);
	private static final AxisAlignedBB BOUNDS_WEST = new AxisAlignedBB(11 * 0.0625D, 1 * 0.0625D, 4 * 0.0625D, 1.0D, 15 * 0.0625D, 12 * 0.0625D);
	private static final AxisAlignedBB BOUNDS_NORTH = new AxisAlignedBB(4 * 0.0625D, 1 * 0.0625D, 11 * 0.0625D, 12 * 0.0625D, 15 * 0.0625D, 1.0D);
	private static final AxisAlignedBB BOUNDS_EAST = new AxisAlignedBB(0.0D, 1 * 0.0625D, 4 * 0.0625D, 5 * 0.0625D, 15 * 0.0625D, 12 * 0.0625D);
	private static final AxisAlignedBB BOUNDS_SOUTH = new AxisAlignedBB(4 * 0.0625D, 1 * 0.0625D, 0.0D, 12 * 0.0625D, 15 * 0.0625D, 5 * 0.0625D);

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		int te = com.hbm.world.WorldBlockState.readMetadata(source, pos.getX(), pos.getY(), pos.getZ());
		switch(te) {
		case 4:
		case 8:
			return BOUNDS_WEST;
		case 2:
		case 6:
			return BOUNDS_NORTH;
		case 5:
		case 9:
			return BOUNDS_EAST;
		case 3:
		case 7:
			return BOUNDS_SOUTH;
		default:
			return BOUNDS_DEFAULT;
		}
	}

	@Override
	public void breakBlock(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		TileEntityRadiobox box = (TileEntityRadiobox)world.getTileEntity(pos);
		if(box != null && box.infinite) world.spawnEntity(new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, new ItemStack(ModItems.battery_spark)));

		super.breakBlock(world, pos, state);
	}
}
