package com.hbm.items.food;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemBDCL extends Item {

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 40;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.DRINK;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		player.setActiveHand(hand);
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
		if(entityLiving instanceof EntityPlayer) {
			return onEaten(stack, worldIn, (EntityPlayer) entityLiving);
		}
		return stack;
	}

	public ItemStack onEaten(ItemStack stack, World world, EntityPlayer player) {
		if(!player.capabilities.isCreativeMode) {
			stack.shrink(1);
		}
		return stack;
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
		if(count % 5 == 0 && count >= 10) {
			player.playSound(com.hbm.main.ModSoundEvents.resolve("hbm:player.gulp"), 1F, 1F);
		}

		if(count == 1) {
			if(player instanceof EntityPlayer) {
				this.onEaten(stack, player.world, (EntityPlayer) player);
			}
			player.resetActiveHand();
			player.playSound(com.hbm.main.ModSoundEvents.resolve("hbm:player.groan"), 1F, 1F);
		}
	}
}
