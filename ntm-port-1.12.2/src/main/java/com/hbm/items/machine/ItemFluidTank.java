package com.hbm.items.machine;

import java.util.List;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.util.i18n.I18nUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import com.hbm.render.icon.IIcon;

public class ItemFluidTank extends Item {

	protected com.hbm.render.icon.IIcon itemIcon;

	protected IIcon overlayIcon;

	public ItemFluidTank() {
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

			if(type.hasNoContainer()) continue;
			int id = type.getID();

			if(type.needsLeadContainer()) {
				if(this == ModItems.fluid_tank_lead_full) {
					list.add(new ItemStack(item, 1, id));
				}

			} else {
				list.add(new ItemStack(item, 1, id));
			}
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		String s1 = ("" + I18nUtil.resolveKey(Fluids.fromID(stack.getItemDamage()).getConditionalName())).trim();
		String s = ("" + I18nUtil.format(this.getTranslationKey() + ".name", s1)).trim();
		return s;
	}

	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister p_94581_1_) {
		// The original used the setTextureName base, not the registry name.
		String base = com.hbm.render.item.NativeItemTextureRegistry.texture(this);
		if(base == null) base = this.getRegistryName().toString();
		this.itemIcon = p_94581_1_.registerIcon(base);

		if(this == ModItems.fluid_tank_full) this.overlayIcon = p_94581_1_.registerIcon("hbm:fluid_tank_overlay");
		if(this == ModItems.fluid_tank_lead_full) this.overlayIcon = p_94581_1_.registerIcon("hbm:fluid_tank_lead_overlay");
		if(this == ModItems.fluid_barrel_full) this.overlayIcon = p_94581_1_.registerIcon("hbm:fluid_barrel_overlay");
		if(this == ModItems.fluid_pack_full) this.overlayIcon = p_94581_1_.registerIcon("hbm:fluid_pack_overlay");
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
			int j = Fluids.fromID(stack.getItemDamage()).getColor();
			if(j < 0) j = 16777215;
			return j;
		}
	}
}
