package com.hbm.blocks.fluid;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIcon;
import net.minecraftforge.fluids.Fluid;

public class VolcanicFluid extends Fluid {

	public VolcanicFluid() {
		super("volcanic_lava_fluid", new net.minecraft.util.ResourceLocation("hbm", "blocks/volcanic_lava_still"), new net.minecraft.util.ResourceLocation("hbm", "blocks/volcanic_lava_flowing"));
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon() {
		return getStillIcon();
	}

	@SideOnly(Side.CLIENT)
	public IIcon getStillIcon() {
		return VolcanicBlock.stillIcon;
	}

	@SideOnly(Side.CLIENT)
	public IIcon getFlowingIcon() {
		return VolcanicBlock.flowingIcon;
	}
}
