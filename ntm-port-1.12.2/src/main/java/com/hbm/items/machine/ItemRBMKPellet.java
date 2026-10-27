package com.hbm.items.machine;

import java.util.List;

import com.hbm.items.special.ItemNuclearWaste;
import com.hbm.main.MainRegistry;
import com.hbm.render.item.NativeItemTextureRegistry;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import com.hbm.render.icon.IIcon;

public class ItemRBMKPellet extends ItemNuclearWaste {

	public String fullName = "";
	protected boolean hasXenon = true;

	public ItemRBMKPellet(String fullName) {
		this.fullName = fullName;
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
		this.setCreativeTab(MainRegistry.controlTab);
	}

	public ItemRBMKPellet disableXenon() {
		this.hasXenon = false;
		return this;
	}

	public boolean isXenonEnabled() {
		return hasXenon;
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
		for(int i = 0; i < (this.hasXenon ? 10 : 5); ++i) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	@SideOnly(Side.CLIENT)
	private IIcon[] enrichmentOverlays;

	@SideOnly(Side.CLIENT)
	private IIcon xenonOverlay;

	@SideOnly(Side.CLIENT)
	protected IIcon itemIcon;

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister) {
		String baseTexture = NativeItemTextureRegistry.texture(this);
		this.itemIcon = iconRegister.registerIcon(baseTexture != null ? baseTexture : this.getRegistryName().toString());

		this.enrichmentOverlays = new IIcon[5];

		for(int i = 0; i < enrichmentOverlays.length; i++) {
			enrichmentOverlays[i] = iconRegister.registerIcon("hbm:rbmk_pellet_overlay_e" + i);
		}

		if(this.hasXenon)
			xenonOverlay = iconRegister.registerIcon("hbm:rbmk_pellet_overlay_xenon");
	}

	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	public int getRenderPasses(int meta) {
		return hasXenon(meta) ? 3 : 2;
	}


		@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public void addInformation(net.minecraft.item.ItemStack stack, net.minecraft.world.World worldIn, java.util.List<java.lang.String> tooltip, net.minecraft.client.util.ITooltipFlag flagIn) {
		addInformation(stack, (net.minecraft.entity.player.EntityPlayer) null, tooltip, flagIn.isAdvanced());
	}

public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {

		list.add(TextFormatting.ITALIC + this.fullName);
		list.add(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + "Pellet for recycling");

		int meta = rectify(stack.getItemDamage());

		switch(meta % 5) {
		case 0: list.add(TextFormatting.GOLD + "Brand New"); break;
		case 1: list.add(TextFormatting.YELLOW + "Barely Depleted"); break;
		case 2: list.add(TextFormatting.GREEN + "Moderately Depleted"); break;
		case 3: list.add(TextFormatting.DARK_GREEN + "Highly Depleted"); break;
		case 4: list.add(TextFormatting.DARK_GRAY + "Fully Depleted"); break;
		}

		if(hasXenon(meta))
			list.add(TextFormatting.DARK_PURPLE + "High Xenon Poison");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamageForRenderPass(int meta, int pass) {

		if(pass == 0)
			return this.itemIcon;

		if(pass == 2)
			return this.xenonOverlay;

		return this.enrichmentOverlays[rectify(meta) % 5];
	}

	public static boolean hasXenon(int meta) {
		return rectify(meta) >= 5;
	}

	public static int rectify(int meta) {
		return Math.abs(meta) % 10;
	}
}
