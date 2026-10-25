package com.hbm.items.tool;

import java.util.List;

import com.hbm.util.AstronomyUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemWandTime extends Item {

	@Override
	public void addInformation(ItemStack stack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {
		list.add("Creative-only item");
		list.add(TextFormatting.ITALIC + "\"Wibbly wobbly, timey-wimey... stuff\"");
		list.add("Probably doesn't work on servers");
	}


		@Override
	public net.minecraft.util.EnumActionResult onItemUse(net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumHand hand, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ) {
		net.minecraft.item.ItemStack stack = player.getHeldItem(hand);
		int side = facing != null ? facing.getIndex() : 0;
		return onItemUse(stack, player, world, pos.getX(), pos.getY(), pos.getZ(), side, hitX, hitY, hitZ) ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.PASS;
	}

public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float fx, float fy, float fz) {
		if(world.isRemote) return true;

		if(player.isSneaking()) {
			AstronomyUtil.TIME_MULTIPLIER /= 2;
			if(AstronomyUtil.TIME_MULTIPLIER < 1) AstronomyUtil.TIME_MULTIPLIER = 1;
		} else {
			AstronomyUtil.TIME_MULTIPLIER *= 2;
		}

		player.sendMessage(new TextComponentString("Celestial Time Multiplier set to: " + AstronomyUtil.TIME_MULTIPLIER));

		return true;
	}

}
