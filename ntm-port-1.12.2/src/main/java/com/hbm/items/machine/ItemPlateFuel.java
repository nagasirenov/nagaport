package com.hbm.items.machine;

import java.util.List;

import com.hbm.util.BobMathUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemPlateFuel extends ItemFuelRod {

	public int reactivity;
	public FunctionEnum function;

	public ItemPlateFuel(int life) {
		super(life);
		this.canRepair = false;
	}

	public ItemPlateFuel setFunction(FunctionEnum function, int reactivity) {
		this.function = function;
		this.reactivity = reactivity;
		return this;
	}


		@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public void addInformation(net.minecraft.item.ItemStack stack, net.minecraft.world.World worldIn, java.util.List<java.lang.String> tooltip, net.minecraft.client.util.ITooltipFlag flagIn) {
		addInformation(stack, (net.minecraft.entity.player.EntityPlayer) null, tooltip, flagIn.isAdvanced());
	}

public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {

		list.add(TextFormatting.YELLOW + "[Research Reactor Plate Fuel]");
		list.add(TextFormatting.DARK_AQUA + "   " + getFunctionDesc());
		list.add(TextFormatting.DARK_AQUA + "   Yield of " + BobMathUtil.getShortNumber(lifeTime) + " events");

	}

	public static enum FunctionEnum {
		LOGARITHM(),
		SQUARE_ROOT(),
		NEGATIVE_QUADRATIC(),
		LINEAR(),
		PASSIVE();

		private FunctionEnum() { }
	}

	public String getFunctionDesc() {
		switch(this.function) {
		case LOGARITHM: return "f(x) = log10(x + 1) * 0.5 * " + reactivity;
		case SQUARE_ROOT: return "f(x) = sqrt(x) * " + reactivity + " / 10";
		case NEGATIVE_QUADRATIC: return "f(x) = [x - (x² / 10000)] / 100 * " + reactivity;
		case LINEAR: return "f(x) = x / 100 * " + reactivity;
		case PASSIVE: return "f(x) = " + reactivity;
		default: return "x";
		}
	}

	public int react(World world, ItemStack stack, int flux) {
		if(this.function != FunctionEnum.PASSIVE)
			setLifeTime(stack, getLifeTime(stack) + flux);

		switch(this.function) {
		case LOGARITHM: return (int) (Math.log10(flux + 1) * 0.5D * reactivity);
		case SQUARE_ROOT: return (int) (Math.sqrt(flux) * this.reactivity / 10D);
		case NEGATIVE_QUADRATIC: return (int) (Math.max((flux - (flux * flux / 10000D)) / 100D * reactivity, 0));
		case LINEAR: return (int) (flux / 100D * reactivity);
		case PASSIVE:
			setLifeTime(stack, getLifeTime(stack) + reactivity);
			return reactivity;
		default: return 0;
		}
	}
}
