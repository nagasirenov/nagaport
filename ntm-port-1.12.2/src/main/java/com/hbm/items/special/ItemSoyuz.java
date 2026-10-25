package com.hbm.items.special;

import java.util.List;

import com.hbm.lib.RefStrings;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import com.hbm.render.icon.IIcon;
import net.minecraft.util.math.MathHelper;

public class ItemSoyuz extends Item {

	IIcon[] icons = new IIcon[3];

	public ItemSoyuz() {
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

    	for(int i = 0; i < icons.length; i++)
    		list.add(new ItemStack(item, 1, i));
    }

    @Override
	public EnumRarity getRarity(ItemStack stack) {

    	if(stack.getItemDamage() == 0)
		return EnumRarity.UNCOMMON;

    	if(stack.getItemDamage() == 1)
		return EnumRarity.RARE;

    	if(stack.getItemDamage() == 2)
		return EnumRarity.EPIC;

		return EnumRarity.COMMON;
    }

	@Override
	public void addInformation(ItemStack stack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {

		list.add("Skin:");

		switch(stack.getItemDamage()) {
		case 0: list.add(TextFormatting.GOLD + "Original"); break;
		case 1: list.add(TextFormatting.BLUE + "Luna Space Center"); break;
		case 2: list.add(TextFormatting.GREEN + "Post War"); break;
		}
	}

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg)
    {
    	for(int i = 0; i < icons.length; i++) {
    		icons[i] = reg.registerIcon(RefStrings.MODID + ":soyuz_" + i);
    	}
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int meta)
    {
        int j = MathHelper.clamp(meta, 0, icons.length - 1);
        return icons[j];
    }

}
