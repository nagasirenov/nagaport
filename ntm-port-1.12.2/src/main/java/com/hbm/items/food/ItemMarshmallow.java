package com.hbm.items.food;

import java.util.List;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import com.hbm.render.icon.IIcon;

public class ItemMarshmallow extends Item {

	protected com.hbm.render.icon.IIcon itemIcon;

	@SideOnly(Side.CLIENT)
	private IIcon iconRoasted;

	public ItemMarshmallow() {
		this.setHasSubtypes(true);
	}


		@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public void getSubItems(net.minecraft.creativetab.CreativeTabs tab, net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> list) {
		if(this.isInCreativeTab(tab)) {
			getSubItems(this, tab, list);
		}
	}

@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		list.add(new ItemStack(item, 1, 0));
		list.add(new ItemStack(item, 1, 1));
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister p_94581_1_) {
		this.itemIcon = p_94581_1_.registerIcon(this.getRegistryName().toString());
		this.iconRoasted = p_94581_1_.registerIcon(this.getRegistryName().toString() + "_roasted");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int damage) {

		if(damage == 1)
			return this.iconRoasted;

		return this.itemIcon;
	}
}
