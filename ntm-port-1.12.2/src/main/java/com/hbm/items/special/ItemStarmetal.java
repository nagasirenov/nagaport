package com.hbm.items.special;

import java.util.List;

import com.hbm.lib.RefStrings;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import com.hbm.render.icon.IIcon;

public class ItemStarmetal extends Item {

	protected IIcon itemIcon;

	private IIcon[] icons = new IIcon[4];

	public ItemStarmetal() {
		this.setHasSubtypes(true);
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister reg) {
		this.itemIcon = reg.registerIcon(this.getRegistryName().toString());

		this.icons[0] = reg.registerIcon(RefStrings.MODID + ":ingot_starmetal");
		this.icons[1] = reg.registerIcon(RefStrings.MODID + ":ingot_starmetal_astra");
		this.icons[2] = reg.registerIcon(RefStrings.MODID + ":ingot_starmetal_ursa");
		this.icons[3] = reg.registerIcon(RefStrings.MODID + ":ingot_starmetal_orion");
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

		list.add(new net.minecraft.item.ItemStack(item, 1, 0));

		//for(int i = 0; i < 4; i++)
		//	list.add(new ItemStack(item, 1, i));
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int meta) {

		int s = Math.abs(meta) % 4;
		return icons[s];
	}

	@Override
	public void addInformation(ItemStack stack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {

		switch(stack.getItemDamage()) {
		case 1: list.add(TextFormatting.ITALIC + "Astra"); break;
		case 2: list.add(TextFormatting.ITALIC + "Ursa"); break;
		case 3: list.add(TextFormatting.ITALIC + "Orion"); break;
		}
	}
}
