package com.hbm.items.special;

import java.util.List;

import com.hbm.items.ModItems;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

public class ItemDemonCore extends Item {

	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem) {

		if(entityItem != null && !entityItem.world.isRemote && entityItem.onGround) {
			entityItem.setItem(new ItemStack(ModItems.demon_core_closed));
			entityItem.world.spawnEntity(new EntityItem(entityItem.world, entityItem.posX, entityItem.posY, entityItem.posZ, new ItemStack(ModItems.screwdriver)));
			return true;
		}

		return false;
	}


		@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public void addInformation(net.minecraft.item.ItemStack stack, net.minecraft.world.World worldIn, java.util.List<java.lang.String> tooltip, net.minecraft.client.util.ITooltipFlag flagIn) {
		addInformation(stack, (net.minecraft.entity.player.EntityPlayer) null, tooltip, flagIn.isAdvanced());
	}

public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		list.add(TextFormatting.RED + "[" + I18nUtil.resolveKey("trait.drop") + "]");
	}
}
