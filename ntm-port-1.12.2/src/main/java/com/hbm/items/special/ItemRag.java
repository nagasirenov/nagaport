package com.hbm.items.special;

import java.util.List;

import com.hbm.util.i18n.I18nUtil;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.items.ModItems;

import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemRag extends Item {

	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem) {

		if(entityItem != null && !entityItem.world.isRemote) {

			if (com.hbm.world.WorldBlockState.read(entityItem.world, (int) Math.floor(entityItem.posX), (int) Math.floor(entityItem.posY),
				(int) Math.floor(entityItem.posZ)).getMaterial() == Material.WATER) {
				ItemStack stack = entityItem.getItem();
				entityItem.setItem(new ItemStack(ModItems.rag_damp, stack.getCount()));
				return true;
			}
		}
		return false;
	}

	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand nativeHand) {
		ItemStack stack = player.getHeldItem(nativeHand);
		return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.PASS, onItemRightClick(stack, world, player));
	}

	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(HbmLivingProps.getRadiation(player) > 500) {
			stack.shrink(1);
			player.inventory.addItemStackToInventory(new ItemStack(ModItems.rag_blood));
			return stack;
		} else {
			stack.shrink(1);
			player.inventory.addItemStackToInventory(new ItemStack(ModItems.rag_piss));
			return stack;
		}
	}

	@Override
	public void addInformation(ItemStack itemstack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {
		String[] lines = I18nUtil.resolveKeyArray("item.rag.desc");
		for (String line : lines) {
			list.add(line);
		}
	}
}
