package com.hbm.tileentity;

import java.util.ArrayList;

import com.hbm.util.CompatExternal;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public interface IPersistentNBT {
	
	public static final String NBT_PERSISTENT_KEY = "persistent";

	public void writeNBT(NBTTagCompound nbt);
	public void readNBT(NBTTagCompound nbt);
	
	public default ArrayList<ItemStack> getDrops(Block b) {
		ArrayList<ItemStack> list = new ArrayList();
		ItemStack stack = new ItemStack(b);
		NBTTagCompound data = new NBTTagCompound();
		writeNBT(data);
		if(!data.isEmpty())
			stack.setTagCompound(data);
		list.add(stack);
		return list;
	}
	
	public static ArrayList<ItemStack> getDrops(World world, int x, int y, int z, Block b) {
		
		TileEntity tile = CompatExternal.getCoreFromPos(world, x, y, z);
		
		if(tile instanceof IPersistentNBT) {
			return ((IPersistentNBT) tile).getDrops(b);
		}
		
		return new ArrayList();
	}
	
	public static void restoreData(World world, int x, int y, int z, ItemStack stack) {
		try {
			if(!stack.hasTagCompound()) return;
			TileEntity tile = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
			if(!(tile instanceof IPersistentNBT)) return;
			IPersistentNBT persistent = (IPersistentNBT) tile;
			persistent.readNBT(stack.getTagCompound());
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
