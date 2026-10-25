package com.hbm.items.special;

import java.util.List;

import com.hbm.items.ModItems;
import com.hbm.util.ItemStackUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import com.hbm.render.icon.IIcon;

public class ItemKitCustom extends ItemKitNBT {

		@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public void getSubItems(net.minecraft.creativetab.CreativeTabs tab, net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> list) {
		if(this.isInCreativeTab(tab)) {
			// list.add(new ItemStack(this, 1, 0));
		}
	}

	public static ItemStack create(String name, String lore, int color1, int color2, ItemStack... contents) {
		ItemStack stack = new ItemStack(ModItems.kit_custom);

		stack.setTagCompound(new NBTTagCompound());

		setColor(stack, color1, 1);
		setColor(stack, color2, 2);

		if(lore != null) ItemStackUtil.addTooltipToStack(stack, lore.split("\\$"));
		stack.setStackDisplayName(TextFormatting.RESET + name);
		ItemStackUtil.addStacksToNBT(stack, contents);

		return stack;
	}

	public static void setColor(ItemStack stack, int color, int index) {
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setInteger("color" + index, color);
	}

	public static int getColor(ItemStack stack, int index) {
		if(!stack.hasTagCompound()) return 0;
		return stack.getTagCompound().getInteger("color" + index);
	}
}
