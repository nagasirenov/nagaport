package com.hbm.items;

import java.util.List;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;

public class ItemRemap extends Item {

	Item remapItem;
	int remapMeta;

	public ItemRemap(Item item, int meta) {
		this.remapItem = item;
		this.remapMeta = meta;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int meta) {
		return this.remapItem.getIconFromDamage(this.remapMeta);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean held) {

		if(!(entity instanceof EntityPlayer)) return;

		EntityPlayer player = (EntityPlayer) entity;
		player.inventory.setInventorySlotContents(slot, new ItemStack(this.remapItem, stack.stackSize, this.remapMeta));
	}

	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int pass) {
		return 0xFF8080;
	}
	
	@Override
	public void addInformation(ItemStack stack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {
		list.add(TextFormatting.RED + "Compatibility item, hold in inventory to convert!");
	}
}
