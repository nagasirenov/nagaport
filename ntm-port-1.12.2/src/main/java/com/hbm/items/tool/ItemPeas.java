package com.hbm.items.tool;

import java.util.List;

import com.hbm.entity.mob.EntityQuackos;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemPeas extends Item{


	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand nativeHand) {
		ItemStack stack = player.getHeldItem(nativeHand);
		return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.PASS, onItemRightClick(stack, world, player));
	}

	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if (!player.capabilities.isCreativeMode) {
			stack.shrink(1);
		}

		List<EntityQuackos> quacc = world.getEntitiesWithinAABB(EntityQuackos.class, player.getEntityBoundingBox().grow(50, 50, 50));

		for(EntityQuackos ducc : quacc) {
			ducc.despawn();
		}

		return stack;
	}
}
