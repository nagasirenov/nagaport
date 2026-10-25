package com.hbm.blocks.generic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.hbm.util.Tuple.Quartet;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import com.hbm.util.NullableInventoryStackNbt;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockLoot extends BlockContainer {

	public BlockLoot() {
		super(Material.IRON);
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
	public Item getItemDropped(net.minecraft.block.state.IBlockState p_149650_1_, Random p_149650_2_, int p_149650_3_) {
		return null;
	}

	@Override
	public void breakBlock(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		Block block = state.getBlock();
		int meta = block.getMetaFromState(state);

		if(!world.isRemote) {
			TileEntityLoot entity = (TileEntityLoot) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
			if(entity != null) {

				for(Quartet<ItemStack, Double, Double, Double> quartet : entity.items) {
					EntityItem item = new EntityItem(world, x + 0.5, y, z + 0.5, quartet.getW());
					world.spawnEntity(item);
				}
			}
		}

		super.breakBlock(world, pos, state);
	}

	private static final AxisAlignedBB BOUNDS = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 0.0625F, 1.0F);

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return BOUNDS;
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;


		if(world.isRemote) {
			return true;

		} else if(!player.isSneaking()) {
			com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
			return true;

		} else {
			return false;
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityLoot();
	}

	public static class TileEntityLoot extends TileEntity {

		public List<Quartet<ItemStack, Double, Double, Double>> items = new ArrayList();


		public TileEntityLoot addItem(ItemStack stack, double x, double y, double z) {
			items.add(new Quartet(stack, x, y, z));
			return this;
		}

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

			int count = nbt.getInteger("count");

			for(int i = 0; i < count; i++) {
				ItemStack stack = NullableInventoryStackNbt.read(nbt.getCompoundTag("item" + i));
				if(stack == null || stack.getItem() == null) continue;
				double x = nbt.getDouble("x" + i);
				double y = nbt.getDouble("y" + i);
				double z = nbt.getDouble("z" + i);
				items.add(new Quartet(stack, x, y, z));
			}
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);

			nbt.setInteger("count", items.size());

			for(int i = 0; i < items.size(); i++) {
				Quartet<ItemStack, Double, Double, Double> item = items.get(i);
				if(item == null || item.getW() == null) continue;
				NBTTagCompound stack = new NBTTagCompound();
				item.getW().writeToNBT(stack);
				nbt.setTag("item" + i, stack);
				nbt.setDouble("x" + i, item.getX());
				nbt.setDouble("y" + i, item.getY());
				nbt.setDouble("z" + i, item.getZ());
			}
			return nbt;
		}

		AxisAlignedBB bb = null;

		@Override
		public AxisAlignedBB getRenderBoundingBox() {

			if(bb == null) {
				bb = new AxisAlignedBB(
						this.pos.getX() - 1,
						this.pos.getY(),
						this.pos.getZ() - 1,
						this.pos.getX() + 2,
						this.pos.getY() + 3,
						this.pos.getZ() + 2
						);
			}

			return bb;
		}
	}
}
