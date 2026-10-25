package com.hbm.blocks.machine;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntityDummy;

import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class DummyBlockMachine extends DummyOldBase {

	private Block drop;
	float oX = 0;
	float oY = 0;
	float oZ = 0;
	float dX = 1;
	float dY = 1;
	float dZ = 1;

	public DummyBlockMachine(Material p_i45386_1_, boolean port) {
		super(p_i45386_1_, port);
	}

	public DummyBlockMachine(Material mat, Block drop, boolean port) {
		super(mat, port);
		this.drop = drop;
	}

	public DummyBlockMachine setBounds(float oX, float oY, float oZ, float dX, float dY, float dZ) {

		this.oX = oX * 0.0625F;
		this.oY = oY * 0.0625F;
		this.oZ = oZ * 0.0625F;
		this.dX = dX * 0.0625F;
		this.dY = dY * 0.0625F;
		this.dZ = dZ * 0.0625F;

		return this;
	}

	@SideOnly(Side.CLIENT)
	public net.minecraft.item.ItemStack getItem(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		return new net.minecraft.item.ItemStack(Item.getItemFromBlock(drop));
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;

		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
			if(te != null && te instanceof TileEntityDummy) {
				int a = ((TileEntityDummy) te).targetX;
				int b = ((TileEntityDummy) te).targetY;
				int c = ((TileEntityDummy) te).targetZ;

				if(te != null) {
					FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, a, b, c);
				}
			}
			return true;
		} else {
			return false;
		}
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return new AxisAlignedBB(oX, oY, oZ, dX, dY, dZ);
	}
}
