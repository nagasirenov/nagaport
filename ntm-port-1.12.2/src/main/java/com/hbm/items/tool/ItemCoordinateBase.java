package com.hbm.items.tool;

import java.util.List;

import com.hbm.util.fauxpointtwelve.BlockPos;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public abstract class ItemCoordinateBase extends Item {

	public static BlockPos getPosition(ItemStack stack) {

		if(stack.hasTagCompound()) {
			return new BlockPos(stack.getTagCompound().getInteger("posX"), stack.getTagCompound().getInteger("posY"), stack.getTagCompound().getInteger("posZ"));
		}

		return null;
	}


		@Override
	public net.minecraft.util.EnumActionResult onItemUse(net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumHand hand, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ) {
		net.minecraft.item.ItemStack stack = player.getHeldItem(hand);
		int side = facing != null ? facing.getIndex() : 0;
		return onItemUse(stack, player, world, pos.getX(), pos.getY(), pos.getZ(), side, hitX, hitY, hitZ) ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.PASS;
	}

public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float fX, float fY, float fZ) {

		if(this.canGrabCoordinateHere(world, x, y, z)) {

			if(!world.isRemote) {
				BlockPos pos = this.getCoordinates(world, x, y, z);

				if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());

				stack.getTagCompound().setInteger("posX", pos.getX());
				if(includeY()) stack.getTagCompound().setInteger("posY", pos.getY());
				stack.getTagCompound().setInteger("posZ", pos.getZ());

				this.onTargetSet(world, pos.getX(), pos.getY(), pos.getZ(), player);
			}

			return true;
		}

		return false;
	}

	/** Whether this position can be saved or if the position target is valid */
	public abstract boolean canGrabCoordinateHere(World world, int x, int y, int z);

	/** Whether this linking item saves the Y coordinate */
	public boolean includeY() {
		return true;
	}

	/** Modified the saved coordinates, for example detecting the core for multiblocks */
	public BlockPos getCoordinates(World world, int x, int y, int z) {
		return new BlockPos(x, y, z);
	}

	/** Extra on successful target set, eg. sounds */
	public void onTargetSet(World world, int x, int y, int z, EntityPlayer player) { }


		@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public void addInformation(net.minecraft.item.ItemStack stack, net.minecraft.world.World worldIn, java.util.List<java.lang.String> tooltip, net.minecraft.client.util.ITooltipFlag flagIn) {
		addInformation(stack, (net.minecraft.entity.player.EntityPlayer) null, tooltip, flagIn.isAdvanced());
	}

public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {

		if(stack.hasTagCompound()) {
			list.add("X: " + stack.getTagCompound().getInteger("posX"));
			if(includeY()) list.add("Y: " + stack.getTagCompound().getInteger("posY"));
			list.add("Z: " + stack.getTagCompound().getInteger("posZ"));
		} else {
			list.add(TextFormatting.RED + "No position set!");
		}
	}
}
