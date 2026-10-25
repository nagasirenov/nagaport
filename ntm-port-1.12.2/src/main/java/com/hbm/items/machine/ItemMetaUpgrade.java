package com.hbm.items.machine;

import java.util.List;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import com.hbm.render.icon.IIcon;

public class ItemMetaUpgrade extends ItemMachineUpgrade {

	protected int levels;

	public ItemMetaUpgrade(int levels) {
		super();
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
		this.levels = levels;
	}

	public ItemMetaUpgrade(UpgradeType type, int levels) {
		super(type);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
		this.levels = levels;
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

		for(int i = 0; i < this.levels; i++) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	protected IIcon[] icons;

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister reg) {

		this.icons = new IIcon[levels];

		for(int i = 0; i < levels; i++) {
			this.icons[i] = reg.registerIcon(this.getRegistryName().toString() + "_" + (i + 1));
		}
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int meta) {
		if(meta >= 0 && meta < levels) {
			return this.icons[meta];
		}

		return this.icons[0];
	}

	@Override
	public String getTranslationKey(ItemStack stack) {
		return super.getTranslationKey() + "_" + (stack.getItemDamage() + 1);
	}
}
