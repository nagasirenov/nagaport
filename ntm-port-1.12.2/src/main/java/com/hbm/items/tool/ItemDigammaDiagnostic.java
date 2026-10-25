package com.hbm.items.tool;

import com.hbm.util.ContaminationUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemDigammaDiagnostic extends Item {

	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand nativeHand) {
		ItemStack stack = player.getHeldItem(nativeHand);
		return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.PASS, onItemRightClick(stack, world, player));
	}

	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(!world.isRemote) {
			world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:item.techBoop"), net.minecraft.util.SoundCategory.PLAYERS, 1.0F, 1.0F);
			ContaminationUtil.printDiagnosticData(player);
		}

		return stack;
	}
}
