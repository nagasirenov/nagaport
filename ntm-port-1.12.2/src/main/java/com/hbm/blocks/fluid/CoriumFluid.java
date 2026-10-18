package com.hbm.blocks.fluid;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIcon;
import net.minecraftforge.fluids.Fluid;

public class CoriumFluid extends Fluid {

	public CoriumFluid() {
		super("corium_fluid");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon() {
		return getStillIcon();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getStillIcon() {
		return CoriumBlock.stillIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getFlowingIcon() {
		return CoriumBlock.flowingIcon;
	}
}
