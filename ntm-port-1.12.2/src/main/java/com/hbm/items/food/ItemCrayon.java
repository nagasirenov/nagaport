package com.hbm.items.food;

import java.util.List;
import java.util.Locale;

import com.hbm.items.machine.ItemChemicalDye.EnumChemDye;
import com.hbm.lib.RefStrings;
import com.hbm.util.EnumUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import com.hbm.render.icon.IIcon;

public class ItemCrayon extends ItemFood {

	protected com.hbm.render.icon.IIcon itemIcon;

	@SideOnly(Side.CLIENT) protected IIcon overlayIcon;

	public ItemCrayon() {
		super(3, false);
		this.setHasSubtypes(true);
		this.setAlwaysEdible();
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
		for(int i = 0; i < EnumChemDye.values().length; i++) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	protected IIcon[] icons;

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister reg) {
		this.itemIcon = reg.registerIcon(this.getRegistryName().toString());
		this.overlayIcon = reg.registerIcon(RefStrings.MODID + ":crayon_overlay");
	}

	@Override
	public String getTranslationKey(ItemStack stack) {
		Enum num = EnumUtil.grabEnumSafely(EnumChemDye.class, stack.getItemDamage());
		return super.getTranslationKey() + "." + num.name().toLowerCase(Locale.US);
	}

	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamageForRenderPass(int meta, int pass) {
		return pass == 1 ? this.overlayIcon : this.itemIcon;
	}

	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int pass) {

		if(pass == 1) {
			EnumChemDye dye = EnumUtil.grabEnumSafely(EnumChemDye.class, stack.getItemDamage());
			return dye.color;
		}

		return 0xffffff;
	}
}
