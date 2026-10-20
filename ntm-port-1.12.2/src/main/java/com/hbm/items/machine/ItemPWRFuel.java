package com.hbm.items.machine;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.world.World;
import java.util.List;

import com.hbm.items.ItemEnumMulti;
import com.hbm.util.EnumUtil;
import com.hbm.util.function.Function;
import com.hbm.util.function.Function.FunctionLogarithmic;
import com.hbm.util.function.Function.FunctionSqrt;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

public class ItemPWRFuel extends ItemEnumMulti {

	public ItemPWRFuel() {
		super(EnumPWRFuel.class, true, true);
	}
	
	public static enum EnumPWRFuel {
		MEU(		05.0D,	new FunctionLogarithmic(20 * 30).withDiv(2_500)),
		HEU233(		07.5D,	new FunctionSqrt(25)),
		HEU235(		07.5D,	new FunctionSqrt(22.5)),
		MEN(		07.5D,	new FunctionLogarithmic(22.5 * 30).withDiv(2_500)),
		HEN237(		07.5D,	new FunctionSqrt(27.5)),
		MOX(		07.5D,	new FunctionLogarithmic(20 * 30).withDiv(2_500)),
		MEP(		07.5D,	new FunctionLogarithmic(22.5 * 30).withDiv(2_500)),
		HEP239(		10.0D,	new FunctionSqrt(22.5)),
		HEP241(		10.0D,	new FunctionSqrt(25)),
		MEA(		07.5D,	new FunctionLogarithmic(25 * 30).withDiv(2_500)),
		HEA242(		10.0D,	new FunctionSqrt(25)),
		HES326(		12.5D,	new FunctionSqrt(27.5)),
		HES327(		12.5D,	new FunctionSqrt(30)),
		BFB_AM_MIX(	2.5D,	new FunctionSqrt(15), 250_000_000),
		BFB_PU241(	2.5D,	new FunctionSqrt(15), 250_000_000);

		public double yield = 1_000_000_000;
		public double heatEmission;
		public Function function;
		
		private EnumPWRFuel(double heatEmission, Function function, double yield) {
			this.heatEmission = heatEmission;
			this.function = function;
		}
		
		private EnumPWRFuel(double heatEmission, Function function) {
			this(heatEmission, function, 1_000_000_000);
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		
		EnumPWRFuel num = EnumUtil.grabEnumSafely(EnumPWRFuel.class, stack.getItemDamage());
		
		String color = TextFormatting.GOLD + "";
		String reset = TextFormatting.RESET + "";
		
		list.add(color + "Heat per flux: " + reset + num.heatEmission + " TU");
		list.add(color + "Reaction function: " + reset + num.function.getLabelForFuel());
		list.add(color + "Fuel type: " + reset + num.function.getDangerFromFuel());
	}
}
