package com.hbm.items.weapon;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemClip extends Item {

	private ItemStack toGive;

	public ItemClip(ItemStack stack) {
		toGive = stack;
		this.setMaxDamage(1);
	}

	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand nativeHand) {
		ItemStack stack = player.getHeldItem(nativeHand);
		return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.PASS, onItemRightClick(stack, world, player));
	}

	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(toGive == null) return stack;

		ItemStack ammo = toGive.copy();

		player.inventory.addItemStackToInventory(ammo);

		stack.shrink(1);
		if(stack.getCount() <= 0)
			stack.damageItem(5, player);

		return stack;
	}
}
