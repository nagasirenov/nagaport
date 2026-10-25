package com.hbm.items.special;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.world.World;
import java.util.List;

import com.hbm.config.GeneralConfig;
import com.hbm.items.ItemCustomLore;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.item.ItemStack;
import com.hbm.render.icon.IIcon;

public class ItemSchraranium extends ItemCustomLore {

	protected IIcon itemIcon;

	IIcon nikonium;

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister reg) {
		this.itemIcon = reg.registerIcon(this.getRegistryName().toString());
		this.nikonium = reg.registerIcon("hbm:ingot_nikonium");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int meta) {

		if(GeneralConfig.enableLBSM && GeneralConfig.enableLBSMFullSchrab)
			return this.nikonium;
		return this.itemIcon;
	}

	public String getItemStackDisplayName(ItemStack stack) {

		if(GeneralConfig.enableLBSM && GeneralConfig.enableLBSMFullSchrab)
			return "Nikonium Ingot";
		else
			return super.getItemStackDisplayName(stack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
		if(GeneralConfig.enableLBSM && GeneralConfig.enableLBSMFullSchrab) list.add("pankæk");
	}
}
