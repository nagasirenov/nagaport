package com.hbm.items.machine;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.world.World;
import java.util.List;
import java.util.Locale;

import com.hbm.inventory.fluid.trait.FT_Combustible.FuelGrade;
import com.hbm.items.ItemEnumMulti;
import com.hbm.util.EnumUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import com.hbm.render.icon.IIcon;

public class ItemPistons extends ItemEnumMulti {

	public ItemPistons() {
		super(EnumPistonType.class, true, true);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister reg) {
		Enum[] enums = theEnum.getEnumConstants();
		this.icons = new IIcon[enums.length];
		
		for(int i = 0; i < icons.length; i++) {
			Enum num = enums[i];
			this.icons[i] = reg.registerIcon(this.getIconString() + "_" + num.name().toLowerCase(Locale.US));
		}
	}
	
	@Override
	public String getTranslationKey(ItemStack stack) {
		Enum num = EnumUtil.grabEnumSafely(theEnum, stack.getItemDamage());
		return super.getTranslationKey() + "_" + num.name().toLowerCase(Locale.US);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		EnumPistonType type = EnumUtil.grabEnumSafely(theEnum, stack.getItemDamage());
		

		list.add(TextFormatting.YELLOW + "Fuel efficiency:");
		for(int i = 0; i < type.eff.length; i++) {
			list.add(TextFormatting.YELLOW + "-" + FuelGrade.values()[i].getGrade() + ": " + TextFormatting.RED + "" + (int)(type.eff[i] * 100) + "%");
		}
	}
	
	public static enum EnumPistonType {
		STEEL		(1.00, 0.75, 0.25, 0.00, 0.00),
		DURA		(0.50, 1.00, 0.90, 0.50, 0.00),
		DESH		(0.00, 0.50, 1.00, 0.75, 0.00),
		STARMETAL	(0.50, 0.75, 1.00, 0.90, 0.50);
		
		public double[] eff;
		
		private EnumPistonType(double... eff) {
			this.eff = new double[Math.min(FuelGrade.values().length, eff.length)];
			for(int i = 0; i < eff.length; i++) {
				this.eff[i] = eff[i];
			}
		}
	}
}
