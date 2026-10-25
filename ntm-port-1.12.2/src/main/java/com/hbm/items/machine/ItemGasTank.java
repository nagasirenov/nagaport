package com.hbm.items.machine;

import java.util.List;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.Fluids.CD_Gastank;
import com.hbm.lib.RefStrings;
import com.hbm.util.i18n.I18nUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import com.hbm.render.icon.IIcon;

public class ItemGasTank extends Item {


	public ItemGasTank() {
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}


		@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public void getSubItems(net.minecraft.creativetab.CreativeTabs tab, net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> list) {
		if(this.isInCreativeTab(tab)) {
			getSubItems(this, tab, list);
		}
	}

@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tabs, List list) {

		FluidType[] order = Fluids.getInNiceOrder();
		for(int i = 1; i < order.length; ++i) {
			FluidType type = order[i];

			if(type.getContainer(CD_Gastank.class) != null) {
				list.add(new ItemStack(item, 1, type.getID()));
			}
		}
	}

	public String getItemStackDisplayName(ItemStack stack) {
		String s = ("" + I18nUtil.resolveKey(this.getTranslationKey() + ".name")).trim();
		String s1 = ("" + I18nUtil.resolveKey(Fluids.fromID(stack.getItemDamage()).getConditionalName())).trim();

		if(s1 != null) {
			s = s + " " + s1;
		}

		return s;
	}

}
