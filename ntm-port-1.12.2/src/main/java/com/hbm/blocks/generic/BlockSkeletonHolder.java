package com.hbm.blocks.generic;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import com.hbm.util.NullableInventoryStackNbt;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BlockSkeletonHolder extends BlockContainer {

	public BlockSkeletonHolder() {
		super(Material.ROCK);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntitySkeletonHolder();
	}

	@Override public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.INVISIBLE;
	}
	@Override public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) { return false; }
	@Override public boolean isFullCube(net.minecraft.block.state.IBlockState state) { return false; }

	@Override
	public void onBlockPlacedBy(World world, net.minecraft.util.math.BlockPos nativePlacementPos, net.minecraft.block.state.IBlockState nativePlacementState, EntityLivingBase player, ItemStack itemStack) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
		int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		if(i == 0) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 5, 2);
		if(i == 1) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 3, 2);
		if(i == 2) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 4, 2);
		if(i == 3) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 2, 2);
	} //I hope you change this shit to not be hardcoded because im not doing it

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;

		if(player.isSneaking()) return false;

		TileEntitySkeletonHolder pedestal = (TileEntitySkeletonHolder) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(pedestal.item == null && com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null) {
			if(world.isRemote) return true;
			pedestal.item = com.hbm.util.NativeHeldItem.getMainHandNullable(player).copy();
			player.inventory.mainInventory.set(player.inventory.currentItem, ItemStack.EMPTY);
			pedestal.markDirty();
			com.hbm.world.WorldBlockState.markBlockForUpdate(world, x, y, z);
			return true;
		} else if(pedestal.item != null && com.hbm.util.NativeHeldItem.getMainHandNullable(player) == null) {
			if(world.isRemote) return true;
			player.inventory.mainInventory.set(player.inventory.currentItem, pedestal.item.copy());
			pedestal.item = null;
			pedestal.markDirty();
			com.hbm.world.WorldBlockState.markBlockForUpdate(world, x, y, z);
			return true;
		}

		return false;
	}

	@Override
	public void breakBlock(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		Block block = state.getBlock();
		int meta = block.getMetaFromState(state);

		if(!world.isRemote) {
			TileEntitySkeletonHolder entity = (TileEntitySkeletonHolder) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
			if(entity != null && entity.item != null) {
				EntityItem item = new EntityItem(world, x + 0.5, y, z + 0.5, entity.item.copy());
				world.spawnEntity(item);
			}
		}

		super.breakBlock(world, pos, state);
	}

	public static class TileEntitySkeletonHolder extends TileEntity {

		public ItemStack item;


		@Override
		public SPacketUpdateTileEntity getUpdatePacket() {
			NBTTagCompound nbt = new NBTTagCompound();
			this.writeToNBT(nbt);
			return new SPacketUpdateTileEntity(this.getPos(), 0, nbt);
		}

		@Override
		public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
			this.readFromNBT(pkt.getNbtCompound());
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			this.item = NullableInventoryStackNbt.read(nbt.getCompoundTag("item"));
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			if(this.item != null) {
				NBTTagCompound stack = new NBTTagCompound();
				this.item.writeToNBT(stack);
				nbt.setTag("item", stack);
			}
			return nbt;
		}
	}
}
