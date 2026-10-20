package com.hbm.items.special;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.world.World;
import java.util.List;

import com.hbm.items.ItemEnumMulti;
import com.hbm.items.ItemEnums.EnumExpensiveType;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

public class ItemExpensive extends ItemEnumMulti {

	public ItemExpensive() {
		super(EnumExpensiveType.class, true, true);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
		list.add(TextFormatting.RED + "Expensive mode item");
	}
}
