package com.hbm.items.machine;

import java.util.List;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids.CD_Canister;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.util.i18n.I18nUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import com.hbm.render.icon.IIcon;

public class ItemCanister extends Item {

	protected com.hbm.render.icon.IIcon itemIcon;

	IIcon overlayIcon;

	public ItemCanister() {
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

			if(type.getContainer(CD_Canister.class) != null) {
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

	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister reg) {
		// The original used the setTextureName base, not the registry name.
		String base = com.hbm.render.item.NativeItemTextureRegistry.texture(this);
		if(base == null) base = this.getRegistryName().toString();
		this.itemIcon = reg.registerIcon(base);
		this.overlayIcon = reg.registerIcon("hbm:canister_overlay");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamageForRenderPass(int p_77618_1_, int p_77618_2_) {
		return p_77618_2_ == 1 ? this.overlayIcon : this.itemIcon;
	}

	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int pass) {
		if(pass == 0) {
			return 16777215;
		} else {

			CD_Canister canister = Fluids.fromID(stack.getItemDamage()).getContainer(CD_Canister.class);
			int j = canister == null ? -1 : canister.color;

			if(j < 0) {
				j = 16777215;
			}

			return j;
		}
	}
}
