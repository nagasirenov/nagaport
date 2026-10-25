package com.hbm.items.food;

import java.util.List;
import java.util.Random;

import com.hbm.items.ModItems;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class ItemMuchoMango extends ItemFood {

	public ItemMuchoMango(int hunger) {
		super(hunger, true);
		this.setAlwaysEdible();
	}

	Random rand = new Random();

	@Override
	protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
		if(!world.isRemote) {
			player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.SPEED, 200));
		}
	}

	@Override
	public void addInformation(ItemStack itemstack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {
		if(this == ModItems.mucho_mango) {
			list.add("The Comically Large Can");
		}
	}

	public int getMaxItemUseDuration(ItemStack p_77626_1_) {
		return 200;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack p_77661_1_) {
		return EnumAction.DRINK;
	}

	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand nativeHand) {
		ItemStack stack = player.getHeldItem(nativeHand);
		return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.PASS, onItemRightClick(stack, world, player));
	}


	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		player.setActiveHand(net.minecraft.util.EnumHand.MAIN_HAND);

		return stack;
	}
}
