package com.hbm.blocks.machine;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.ct.CTContext;
import com.hbm.render.block.ct.IBlockCT;
import com.hbm.tileentity.machine.TileEntityPWRController;
import com.hbm.util.PersistentRegistryRef;

import api.hbm.fluidmk2.IFluidReceiverMK2;
import api.hbm.redstoneoverradio.IRORInteractive;
import api.hbm.redstoneoverradio.IRORValueProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class BlockPWR extends BlockContainer implements IBlockCT {


	public BlockPWR(Material mat) {
		super(mat);
        setDefaultState(blockState.getBaseState().withProperty(CTContext.METADATA, 0));
	}

    @Override protected net.minecraft.block.state.BlockStateContainer createBlockState() { return CTContext.stateContainer(this); }
    @Override public net.minecraft.block.state.IBlockState getStateFromMeta(int metadata) { return getDefaultState().withProperty(CTContext.METADATA, metadata & 15); }
    @Override public int getMetaFromState(net.minecraft.block.state.IBlockState state) { return state.getValue(CTContext.METADATA); }
    @Override public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) { return net.minecraft.util.EnumBlockRenderType.MODEL; }
    @Override public net.minecraft.block.state.IBlockState getExtendedState(net.minecraft.block.state.IBlockState state, IBlockAccess world, net.minecraft.util.math.BlockPos pos) {
        return CTContext.extend(state, world, pos, this);
    }


	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState i, Random rand, int j)  {
		return null;
	}

    @Override public int getCTMaterial(net.minecraft.block.state.IBlockState state) { return getMetaFromState(state) == 1 ? 1 : 0; }


	@Override
	public boolean canConnect(IBlockAccess world, int x, int y, int z, Block block) {
		return block == ModBlocks.pwr_block || block == ModBlocks.pwr_controller;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityBlockPWR();
	}

	@Override
	public void breakBlock(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		Block block = state.getBlock();
		int meta = block.getMetaFromState(state);

		TileEntity tile = world.getTileEntity(pos);

		if(tile instanceof TileEntityBlockPWR) {
			TileEntityBlockPWR pwr = (TileEntityBlockPWR) tile;
			world.removeTileEntity(pos);
			if(pwr.block != null) {
				com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, pwr.block);
				TileEntity controller = world.getTileEntity(new net.minecraft.util.math.BlockPos(pwr.coreX, pwr.coreY, pwr.coreZ));

				if(controller instanceof TileEntityPWRController) {
					((TileEntityPWRController) controller).assembled = false;
				}
			}
		} else {
			world.removeTileEntity(pos);
		}
		super.breakBlock(world, pos, state);
	}

	public static class TileEntityBlockPWR extends TileEntity implements IFluidReceiverMK2, ISidedInventory, IRORValueProvider, IRORInteractive, net.minecraft.util.ITickable {

		@Override
		public boolean isEmpty() {
			for(int i = 0; i < this.getSizeInventory(); i++) {
				if(this.getStackInSlot(i) != null && !this.getStackInSlot(i).isEmpty()) return false;
			}
			return true;
		}

		@Override public int getField(int id) { return 0; }
		@Override public void setField(int id, int value) {}
		@Override public int getFieldCount() { return 0; }

		@Override
		public void clear() {
			for(int i = 0; i < this.getSizeInventory(); i++) {
				this.setInventorySlotContents(i, null);
			}
		}

		public Block block;
		public int coreX;
		public int coreY;
		public int coreZ;
		private NBTTagCompound legacyRef;

		@Override
		public void update() {

			if(!this.world.isRemote) {

				if(this.world.getTotalWorldTime() % 20 == 0 && block != null) {

					TileEntityPWRController controller = getCore();

					if(controller != null) {
						if(!controller.assembled) {
							this.getBlockType().breakBlock(this.world, this.pos, this.world.getBlockState(this.pos));
						}
					} else if(this.world.getChunkProvider().getLoadedChunk(coreX >> 4, coreZ >> 4) != null) {
						this.getBlockType().breakBlock(this.world, this.pos, this.world.getBlockState(this.pos));
					}
				}
			}
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);

			block = PersistentRegistryRef.readBlock(nbt, "block");
			if(block != null && block != Blocks.AIR) {
				coreX = nbt.getInteger("cX");
				coreY = nbt.getInteger("cY");
				coreZ = nbt.getInteger("cZ");
			} else {
				block = null;
				this.legacyRef = PersistentRegistryRef.legacyOnly(nbt, "block", "cX", "cY", "cZ");
			}
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);

			if(block != null) {
				PersistentRegistryRef.writeBlock(nbt, "block", block);
				nbt.setInteger("cX", coreX);
				nbt.setInteger("cY", coreY);
				nbt.setInteger("cZ", coreZ);
			} else {
				PersistentRegistryRef.preserveLegacy(this.legacyRef, nbt, "block", "cX", "cY", "cZ");
			}
			return nbt;
		}

		@Override
		public void markDirty() {
			if(this.world != null) {
				this.world.markChunkDirty(this.pos, this);
			}
		}

		public TileEntityPWRController cachedCore;

		protected TileEntityPWRController getCore() {

			if(cachedCore != null && !cachedCore.isInvalid()) return cachedCore;

			if(this.world.getChunkProvider().getLoadedChunk(coreX >> 4, coreZ >> 4) != null) {

				TileEntity tile = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(coreX, coreY, coreZ));
				if(tile instanceof TileEntityPWRController) {
					TileEntityPWRController controller = (TileEntityPWRController) tile;
					cachedCore = controller;
					return controller;
				}
			}

			return null;
		}

		@Override
		public long transferFluid(FluidType type, int pressure, long fluid) {

			if(this.getBlockMetadata() != 1) return fluid;
			if(block == null) return fluid;
			TileEntityPWRController controller = this.getCore();
			if(controller != null) return controller.transferFluid(type, pressure, fluid);

			return fluid;
		}

		@Override
		public long getDemand(FluidType type, int pressure) {
			if(this.getBlockMetadata() != 1) return 0;
			if(block == null) return 0;
			TileEntityPWRController controller = this.getCore();
			if(controller != null) return controller.getDemand(type, pressure);
			return 0;
		}

		@Override
		public FluidTank[] getAllTanks() {
			if(this.getBlockMetadata() != 1) return FluidTank.EMPTY_ARRAY;
			if(block == null) return FluidTank.EMPTY_ARRAY;
			TileEntityPWRController controller = this.getCore();
			if(controller != null) return controller.getAllTanks();
			return FluidTank.EMPTY_ARRAY;
		}

		@Override
		public boolean canConnect(FluidType type, ForgeDirection dir) {
			return this.getBlockMetadata() == 1;
		}

		@Override
		public int getSizeInventory() {

			if(this.getBlockMetadata() != 1) return 0;
			if(block == null) return 0;
			TileEntityPWRController controller = this.getCore();
			if(controller != null) return controller.getSizeInventory();

			return 0;
		}

		private static ItemStack nativeStack(ItemStack stack) {
			return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack;
		}

		@Override
		public ItemStack getStackInSlot(int slot) {

			if(this.getBlockMetadata() != 1) return ItemStack.EMPTY;
			if(block == null) return ItemStack.EMPTY;
			TileEntityPWRController controller = this.getCore();
			if(controller != null) return nativeStack(controller.getStackInSlot(slot));

			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack decrStackSize(int slot, int amount) {

			if(this.getBlockMetadata() != 1) return ItemStack.EMPTY;
			if(block == null) return ItemStack.EMPTY;
			TileEntityPWRController controller = this.getCore();
			if(controller != null) return nativeStack(controller.decrStackSize(slot, amount));

			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack removeStackFromSlot(int slot) {

			if(this.getBlockMetadata() != 1) return ItemStack.EMPTY;
			if(block == null) return ItemStack.EMPTY;
			TileEntityPWRController controller = this.getCore();
			if(controller != null) return nativeStack(controller.removeStackFromSlot(slot));

			return ItemStack.EMPTY;
		}

		@Override
		public void setInventorySlotContents(int slot, ItemStack stack) {

			if(this.getBlockMetadata() != 1) return;
			if(block == null) return;
			TileEntityPWRController controller = this.getCore();
			if(controller != null) controller.setInventorySlotContents(slot, nativeStack(stack));
		}

		@Override
		public int getInventoryStackLimit() {

			if(this.getBlockMetadata() != 1) return 0;
			if(block == null) return 0;
			TileEntityPWRController controller = this.getCore();
			if(controller != null) return controller.getInventoryStackLimit();

			return 0;
		}

		@Override public boolean isUsableByPlayer(EntityPlayer player) { return false; }
		@Override public void openInventory(EntityPlayer player) { }
		@Override public void closeInventory(EntityPlayer player) { }
		@Override public String getName() { return ""; }
		@Override public boolean hasCustomName() { return false; }
		public String getInventoryName() { return getName(); }
		public boolean hasCustomInventoryName() { return hasCustomName(); }

		@Override
		public boolean isItemValidForSlot(int slot, ItemStack stack) {

			if(this.getBlockMetadata() != 1) return false;
			if(block == null) return false;
			TileEntityPWRController controller = this.getCore();
			if(controller != null) return controller.isItemValidForSlot(slot, stack);

			return false;
		}

		@Override
		public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
			int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();

			if(this.getBlockMetadata() != 1) return new int[0];
			if(block == null) return new int[0];
			TileEntityPWRController controller = this.getCore();
			if(controller != null) return controller.getSlotsForFace(com.hbm.lib.ForgeDirection.getOrientation(side).toEnumFacing());

			return new int[0];
		}

		@Override
		public boolean canInsertItem(int slot, ItemStack stack, net.minecraft.util.EnumFacing nativeFace) {
			int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();

			if(this.getBlockMetadata() != 1) return false;
			if(block == null) return false;
			TileEntityPWRController controller = this.getCore();
			if(controller != null) return controller.canInsertItem(slot, stack, com.hbm.lib.ForgeDirection.getOrientation(side).toEnumFacing());

			return false;
		}

		@Override
		public boolean canExtractItem(int slot, ItemStack stack, net.minecraft.util.EnumFacing nativeFace) {
			int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();

			if(this.getBlockMetadata() != 1) return false;
			if(block == null) return false;
			TileEntityPWRController controller = this.getCore();
			if(controller != null) return controller.canExtractItem(slot, stack, com.hbm.lib.ForgeDirection.getOrientation(side).toEnumFacing());

			return false;
		}

		public boolean isLoaded = true;

		@Override
		public boolean isLoaded() {
			return isLoaded;
		}

		@Override
		public void onChunkUnload() {
			super.onChunkUnload();
			this.isLoaded = false;
		}

		@Override
		public String[] getFunctionInfo() {
			if(this.getBlockMetadata() != 1) return new String[0];
			TileEntityPWRController controller = this.getCore();
			if(controller != null) return controller.getFunctionInfo();
			return new String[0];
		}

		@Override
		public String provideRORValue(String name) {
			if(this.getBlockMetadata() != 1) return "";
			TileEntityPWRController controller = this.getCore();
			if(controller != null) return controller.provideRORValue(name);
			return "";
		}

		@Override
		public String runRORFunction(String name, String[] params) {
			if(this.getBlockMetadata() != 1) return "";
			TileEntityPWRController controller = this.getCore();
			if(controller != null) return controller.runRORFunction(name, params);
			return "";
		}
	}
}
