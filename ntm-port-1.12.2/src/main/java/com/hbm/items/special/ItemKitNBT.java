package com.hbm.items.special;

import java.util.List;

import com.hbm.items.ModItems;
import com.hbm.util.ItemStackUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemKitNBT extends Item {

	public ItemKitNBT() {
		this.setMaxStackSize(1);
	}

	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand nativeHand) {
		ItemStack stack = player.getHeldItem(nativeHand);
		return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.PASS, onItemRightClick(stack, world, player));
	}

	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		ItemStack[] stacks = ItemStackUtil.readStacksFromNBT(stack);

		if(stacks != null) {

			for(ItemStack item : stacks) {
				if(item != null) {
					player.inventory.addItemStackToInventory(item.copy());
				}
			}
		}

		ItemStack container = stack.getItem().getContainerItem(stack);

		stack.shrink(1);

		if(container != null) {

			if(stack.getCount() > 0) {
				player.inventory.addItemStackToInventory(container.copy());
			} else {
				stack = container.copy();
			}
		}

		world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:item.unpack"), net.minecraft.util.SoundCategory.PLAYERS, 1.0F, 1.0F);

		return stack;
	}


		@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public void addInformation(net.minecraft.item.ItemStack stack, net.minecraft.world.World worldIn, java.util.List<java.lang.String> tooltip, net.minecraft.client.util.ITooltipFlag flagIn) {
		addInformation(stack, (net.minecraft.entity.player.EntityPlayer) null, tooltip, flagIn.isAdvanced());
	}

@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {

		ItemStack[] stacks = ItemStackUtil.readStacksFromNBT(stack);

		if(stacks != null) {

			list.add("Contains:");

			for(ItemStack item : stacks) {
				list.add("-" + item.getDisplayName() + (item.getCount() > 1 ? (" x" + item.getCount()) : ""));
			}
		}
	}

	public static ItemStack create(ItemStack... contents) {
		ItemStack stack = new ItemStack(ModItems.kit_custom);
		stack.setTagCompound(new NBTTagCompound());
		ItemStackUtil.addStacksToNBT(stack, contents);

		return stack;
	}
}
