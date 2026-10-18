package com.hbm.items.tool;

import java.util.List;

import com.hbm.blocks.bomb.LaunchPad;
import com.hbm.lib.Library;

import api.hbm.item.IDesignatorItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemDesingatorRange extends Item implements IDesignatorItem {

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		if(itemstack.stackTagCompound != null) {
			list.add("Target Coordinates:");
			list.add("X: " + itemstack.stackTagCompound.getInteger("xCoord"));
			list.add("Z: " + itemstack.stackTagCompound.getInteger("zCoord"));
		} else {
			list.add("Please select a target.");
		}
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		RayTraceResult pos = Library.rayTrace(player, 300, 1);
		int x = pos.getBlockPos().getX();
		int y = pos.getBlockPos().getY();
		int z = pos.getBlockPos().getZ();

		if(!(world.getBlock(x, y, z) instanceof LaunchPad)) {
			
			if(stack.stackTagCompound == null)
				stack.stackTagCompound = new NBTTagCompound();

			stack.stackTagCompound.setInteger("xCoord", x);
			stack.stackTagCompound.setInteger("zCoord", z);

			if(world.isRemote) {
				player.sendMessage(new TextComponentString("Position set to X:" + x + ", Z:" + z));
			}

			world.playSoundAtEntity(player, "hbm:item.techBleep", 1.0F, 1.0F);

			return stack;
		}

		return stack;
	}

	@Override
	public boolean isReady(World world, ItemStack stack, int x, int y, int z) {
		return stack.hasTagCompound();
	}

	@Override
	public Vec3d getCoords(World world, ItemStack stack, int x, int y, int z) {
		return new Vec3d(stack.stackTagCompound.getInteger("xCoord"), 0, stack.stackTagCompound.getInteger("zCoord"));
	}
}
