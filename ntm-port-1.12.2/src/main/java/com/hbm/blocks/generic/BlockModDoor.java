package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.main.ModSoundEvents;
import com.hbm.world.gen.nbt.INBTBlockTransformable;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockModDoor extends BlockDoor implements INBTBlockTransformable {

	public BlockModDoor(Material materialIn) {
		super(materialIn);
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		return this.getSelectedBoundingBox(world.getBlockState(pos), world, pos);
	}

	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		return this.getCollisionBoundingBox(world.getBlockState(pos), world, pos);
	}

	public int func_150012_g(IBlockAccess worldIn, int x, int y, int z) {
		return BlockDoor.combineMetadata(worldIn, new BlockPos(x, y, z));
	}

	protected void func_150011_b(int meta) {
	}

	public void func_150014_a(World worldIn, int x, int y, int z, boolean open) {
		this.toggleDoor(worldIn, new BlockPos(x, y, z), open);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		BlockPos blockpos = state.getValue(HALF) == BlockDoor.EnumDoorHalf.LOWER ? pos : pos.down();
		IBlockState iblockstate = pos.equals(blockpos) ? state : worldIn.getBlockState(blockpos);

		if (iblockstate.getBlock() != this) {
			return false;
		} else {
			state = iblockstate.cycleProperty(OPEN);
			worldIn.setBlockState(blockpos, state, 10);
			worldIn.markBlockRangeForRenderUpdate(blockpos, pos);
			worldIn.playSound(null, pos, ModSoundEvents.resolve(getSFX()), SoundCategory.BLOCKS, 1.0F, worldIn.rand.nextFloat() * 0.1F + 0.9F);
			return true;
		}
	}

	@Override
	public void toggleDoor(World worldIn, BlockPos pos, boolean open) {
		IBlockState iblockstate = worldIn.getBlockState(pos);
		if (iblockstate.getBlock() == this) {
			BlockPos blockpos = iblockstate.getValue(HALF) == BlockDoor.EnumDoorHalf.LOWER ? pos : pos.down();
			IBlockState iblockstate1 = pos == blockpos ? iblockstate : worldIn.getBlockState(blockpos);
			if (iblockstate1.getBlock() == this && ((Boolean) iblockstate1.getValue(OPEN)).booleanValue() != open) {
				worldIn.setBlockState(blockpos, iblockstate1.withProperty(OPEN, Boolean.valueOf(open)), 10);
				worldIn.markBlockRangeForRenderUpdate(blockpos, pos);
				worldIn.playSound(null, pos, ModSoundEvents.resolve(getSFX()), SoundCategory.BLOCKS, 1.0F, worldIn.rand.nextFloat() * 0.1F + 0.9F);
			}
		}
	}

	protected String getSFX() {
		return "hbm:block.openDoor";
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		if (state.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER)
			return Items.AIR;

		return this.getItem();
	}

	public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
		return new ItemStack(this.getItem());
	}

	protected Item getItem() {
		if (this == ModBlocks.door_metal)
			return ModItems.door_metal;
		else if (this == ModBlocks.door_office)
			return ModItems.door_office;
		else if (this == ModBlocks.door_red)
			return ModItems.door_red;
		else if (this == ModBlocks.door_bunker)
			return ModItems.door_bunker;
		else
			return ModItems.fence_gate;
	}

	@Override
	public int transformMeta(int meta, int coordBaseMode) {
		return INBTBlockTransformable.transformMetaDoor(meta, coordBaseMode);
	}
}
