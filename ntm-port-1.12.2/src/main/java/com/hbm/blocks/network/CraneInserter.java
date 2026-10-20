package com.hbm.blocks.network;

import api.hbm.conveyor.IConveyorItem;
import api.hbm.conveyor.IConveyorPackage;
import api.hbm.conveyor.IEnterableBlock;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.network.TileEntityCraneBase;
import com.hbm.tileentity.network.TileEntityCraneInserter;
import com.hbm.util.InventoryUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class CraneInserter extends BlockCraneBase implements IEnterableBlock {

	public CraneInserter() {
		super(Material.IRON);
		initModelTextures();
	}

	@Override
	public TileEntityCraneBase createNewTileEntity(World world, int meta) {
		return new TileEntityCraneInserter();
	}

	@Override
	protected void initModelTextures() {
		super.initModelTextures();
		this.iconDirectional = new ResourceLocation(RefStrings.MODID, "blocks/crane_in_top");
		this.iconDirectionalUp = new ResourceLocation(RefStrings.MODID, "blocks/crane_in_side_up");
		this.iconDirectionalDown = new ResourceLocation(RefStrings.MODID, "blocks/crane_in_side_down");
		this.iconDirectionalTurnLeft = new ResourceLocation(RefStrings.MODID, "blocks/crane_in_top_left");
		this.iconDirectionalTurnRight = new ResourceLocation(RefStrings.MODID, "blocks/crane_in_top_right");
		this.iconDirectionalSideLeftTurnUp = new ResourceLocation(RefStrings.MODID, "blocks/crane_in_side_left_turn_up");
		this.iconDirectionalSideRightTurnUp = new ResourceLocation(RefStrings.MODID, "blocks/crane_in_side_right_turn_up");
		this.iconDirectionalSideLeftTurnDown = new ResourceLocation(RefStrings.MODID, "blocks/crane_in_side_left_turn_down");
		this.iconDirectionalSideRightTurnDown = new ResourceLocation(RefStrings.MODID, "blocks/crane_in_side_right_turn_down");
		this.iconDirectionalSideUpTurnLeft = new ResourceLocation(RefStrings.MODID, "blocks/crane_in_side_up_turn_left");
		this.iconDirectionalSideUpTurnRight = new ResourceLocation(RefStrings.MODID, "blocks/crane_in_side_up_turn_right");
		this.iconDirectionalSideDownTurnLeft = new ResourceLocation(RefStrings.MODID, "blocks/crane_in_side_down_turn_left");
		this.iconDirectionalSideDownTurnRight = new ResourceLocation(RefStrings.MODID, "blocks/crane_in_side_down_turn_right");
	}

	@Override
	public boolean canItemEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorItem entity) {
		ForgeDirection orientation = ForgeDirection.getOrientation(getMetaFromState(world.getBlockState(new BlockPos(x, y, z))));
		return orientation == dir;
	}

	@Override
	public void onItemEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorItem entity) {
		ForgeDirection outputDirection = getOutputSide(world, x, y, z);
		TileEntity te = world.getTileEntity(new BlockPos(x + outputDirection.offsetX, y + outputDirection.offsetY, z + outputDirection.offsetZ));

		if(entity == null || entity.getItemStack() == null || entity.getItemStack().getCount() <= 0) {
			return;
		}

		ItemStack toAdd = entity.getItemStack().copy();

		if (!world.isBlockPowered(new BlockPos(x, y, z))) {
			int[] access = null;

			if(te instanceof ISidedInventory) {
				ISidedInventory sided = (ISidedInventory) te;
				access = InventoryUtil.masquerade(sided, outputDirection.getOpposite().ordinal());
			}

			if(te instanceof IInventory) {
				IInventory inv = (IInventory) te;

				addToInventory(inv, access, toAdd, outputDirection.getOpposite().ordinal());
			}
		}

		TileEntityCraneInserter inserter = null;

		if(toAdd.getCount() > 0) {
			inserter = (TileEntityCraneInserter) world.getTileEntity(new BlockPos(x, y, z));
			addToInventory(inserter, null, toAdd, outputDirection.getOpposite().ordinal());
		}
		if(toAdd.getCount() > 0 && inserter != null && !inserter.destroyer) {
			EntityItem drop = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, toAdd.copy());
			world.spawnEntity(drop);
		}
	}

	public static ItemStack addToInventory(IInventory inv, int[] access, ItemStack toAdd, int side) {

		ISidedInventory sided = inv instanceof ISidedInventory ? (ISidedInventory) inv : null;
		int limit = inv.getInventoryStackLimit();

		int size = access == null ? inv.getSizeInventory() : access.length;

		for(int i = 0; i < size; i++) {
			int index = access == null ? i : access[i];
			ItemStack stack = inv.getStackInSlot(index);

			if(stack != null && !stack.isEmpty() && toAdd.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(toAdd, stack) && stack.getCount() < Math.min(stack.getMaxStackSize(), limit)
					 && ((sided == null || sided.canInsertItem(index, toAdd, com.hbm.lib.ForgeDirection.getOrientation(side).toEnumFacing())) && inv.isItemValidForSlot(index, toAdd))) {

				int stackLimit = Math.min(stack.getMaxStackSize(), limit);
				int amount = Math.min(toAdd.getCount(), stackLimit - stack.getCount());

				stack.grow(amount);
				toAdd.shrink(amount);
				inv.markDirty();

				if(toAdd.getCount() == 0) {
					return null;
				}
			}
		}

		for(int i = 0; i < size; i++) {
			int index = access == null ? i : access[i];
			ItemStack stack = inv.getStackInSlot(index);

			if((stack == null || stack.isEmpty()) && ((sided == null || sided.canInsertItem(index, toAdd, com.hbm.lib.ForgeDirection.getOrientation(side).toEnumFacing())) && inv.isItemValidForSlot(index, toAdd))) {

				int amount = Math.min(toAdd.getCount(), limit);

				ItemStack newStack = toAdd.copy();
				newStack.setCount(amount);
				inv.setInventorySlotContents(index, newStack);
				toAdd.shrink(amount);
				inv.markDirty();

				if(toAdd.getCount() == 0) {
					return null;
				}
			}
		}

		return toAdd;
	}

	@Override public boolean canPackageEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorPackage entity) { return true; }

	@Override
	public void onPackageEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorPackage entity) {
		ForgeDirection outputDirection = getOutputSide(world, x, y, z);
		TileEntity te = world.getTileEntity(new BlockPos(x + outputDirection.offsetX, y + outputDirection.offsetY, z + outputDirection.offsetZ));

		if(entity == null || entity.getItemStacks() == null || entity.getItemStacks().length == 0) {
			return;
		}
		
		ItemStack[] toAdd = entity.getItemStacks();

		if(!world.isBlockPowered(new BlockPos(x, y, z))) {
			int[] access = null;

			if(te instanceof ISidedInventory) {
				ISidedInventory sided = (ISidedInventory) te;
				access = InventoryUtil.masquerade(sided, outputDirection.getOpposite().ordinal());
			}

			if(te instanceof IInventory) {
				IInventory inv = (IInventory) te;

				for(ItemStack stack : toAdd) addToInventory(inv, access, stack, outputDirection.getOpposite().ordinal());
			}
		}

		TileEntityCraneInserter inserter = null;
		
		for(ItemStack stack : toAdd) {
			
			if(stack.getCount() > 0) {
				inserter = (TileEntityCraneInserter) world.getTileEntity(new BlockPos(x, y, z));
				addToInventory(inserter, null, stack, outputDirection.getOpposite().ordinal());
			}
			if(stack.getCount() > 0 && inserter != null && !inserter.destroyer) {
				EntityItem drop = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, stack.copy());
				world.spawnEntity(drop);
			}
		}
	}

	@Override public boolean hasComparatorInputOverride(IBlockState state) { return true; }

	@Override
	public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		return Container.calcRedstoneFromInventory((TileEntityCraneInserter)world.getTileEntity(new BlockPos(x, y, z)));
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		Block block = state.getBlock();
		int meta = getMetaFromState(state);
		this.dropContents(world, x, y, z, block, meta, 0, 21);
		super.breakBlock(world, pos, state);
	}
}
