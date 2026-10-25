package com.hbm.items.tool;

import java.util.List;
import java.util.Random;

import com.hbm.handler.BossSpawnHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class ItemMeteorRemote extends Item {

	Random rand = new Random();

	public ItemMeteorRemote() {
		this.canRepair = false;
		this.setMaxDamage(2);
	}

	@Override
	public void addInformation(ItemStack itemstack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {
		list.add("Right click to summon a meteorite!");
	}

	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand nativeHand) {
		ItemStack stack = player.getHeldItem(nativeHand);
		return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.PASS, onItemRightClick(stack, world, player));
	}

	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		stack.damageItem(1, player);

		if(!world.isRemote) {
			BossSpawnHandler.spawnMeteorAtPlayer(player, false);
			player.sendMessage(new TextComponentString("Watch your head!"));
		}

		world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:item.techBleep"), net.minecraft.util.SoundCategory.PLAYERS, 1.0F, 1.0F);
		player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);

		return stack;
	}
}
