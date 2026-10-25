package com.hbm.items.special;

import java.util.List;

import com.hbm.entity.mob.siege.SiegeTier;
import com.hbm.lib.RefStrings;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import com.hbm.render.icon.IIcon;

public class ItemSiegeCoin extends Item {

	protected IIcon itemIcon;

	@SideOnly(Side.CLIENT)
	private IIcon[] icons;

	public ItemSiegeCoin() {
		this.hasSubtypes = true;
		this.setMaxDamage(0);
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int meta) {
		return this.icons[meta % icons.length];
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister p_94581_1_) {
		this.itemIcon = com.hbm.render.icon.LegacyIconRegister.wrapSprite(net.minecraft.client.Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(net.minecraft.init.Items.STICK, 0));

		icons = new IIcon[SiegeTier.getLength()];

		for(int i = 0; i < SiegeTier.getLength(); i++) {
			icons[i] = p_94581_1_.registerIcon(RefStrings.MODID + ":coin_siege_" + SiegeTier.tiers[i].name);
		}
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {
		return EnumRarity.UNCOMMON;
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

		for(int i = 0; i < SiegeTier.getLength(); i++) {
			list.add(new ItemStack(item, 1, i));
		}
	}



		@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public void addInformation(net.minecraft.item.ItemStack stack, net.minecraft.world.World worldIn, java.util.List<java.lang.String> tooltip, net.minecraft.client.util.ITooltipFlag flagIn) {
		addInformation(stack, (net.minecraft.entity.player.EntityPlayer) null, tooltip, flagIn.isAdvanced());
	}

@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		list.add(TextFormatting.YELLOW + "Tier " + (stack.getItemDamage() + 1));
	}
}
