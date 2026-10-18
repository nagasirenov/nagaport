package com.hbm.inventory.fluid.trait;

import java.util.List;

import net.minecraft.util.text.TextFormatting;

public class FluidTraitSimple {

	/** gaseous at room temperature, for cryogenic hydrogen for example */
	public static class FT_Gaseous_ART extends FluidTrait {
		@Override public void addInfoHidden(List<String> info) {
			info.add(TextFormatting.BLUE + "[Gaseous at Room Temperature]");
		}
	}

	public static class FT_Liquid extends FluidTrait {
		@Override public void addInfoHidden(List<String> info) {
			info.add(TextFormatting.BLUE + "[Liquid]");
		}
	}

	/** to viscous to be sprayed/turned into a mist */
	public static class FT_Viscous extends FluidTrait {
		@Override public void addInfoHidden(List<String> info) {
			info.add(TextFormatting.BLUE + "[Viscous]");
		}
	}

	public static class FT_Plasma extends FluidTrait {
		@Override public void addInfoHidden(List<String> info) {
			info.add(TextFormatting.LIGHT_PURPLE + "[Plasma]");
		}
	}

	public static class FT_Amat extends FluidTrait {
		@Override public void addInfo(List<String> info) {
			info.add(TextFormatting.DARK_RED + "[Antimatter]");
		}
	}

	public static class FT_LeadContainer extends FluidTrait {
		@Override public void addInfo(List<String> info) {
			info.add(TextFormatting.DARK_RED + "[Requires hazardous material tank to hold]");
		}
	}

	public static class FT_Delicious extends FluidTrait {
		@Override public void addInfoHidden(List<String> info) {
			info.add(TextFormatting.DARK_GREEN + "[Delicious]");
		}
	}
	public static class FT_ULTRAKILL extends FluidTrait {
		@Override public void addInfoHidden(List<String> info) {
		info.add(TextFormatting.DARK_RED + "[ULTRAKILL]");
	}}
	public static class FT_EXPLOSIVE extends FluidTrait {
		@Override public void addInfoHidden(List<String> info) {
		info.add(TextFormatting.RED + "[Explosive]");
	}}
	
	public static class FT_Leaded extends FluidTrait {
		@Override public void addInfoHidden(List<String> info) {
			info.add(TextFormatting.BLUE + "[Leaded Fuel]");
		}
	}

	public static class FT_Unsiphonable extends FluidTrait {
		@Override public void addInfoHidden(List<String> info) {
			info.add(TextFormatting.BLUE + "[Ignored by siphon]");
		}
	}

	public static class FT_NoID extends FluidTrait { }
	public static class FT_NoContainer extends FluidTrait { }
}
