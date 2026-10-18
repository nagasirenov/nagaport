package com.hbm.blocks.fluid;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIcon;
import net.minecraftforge.fluids.Fluid;

public class MudFluid extends Fluid {

	public MudFluid() {
		super("mud_fluid");
	}

	@Override
	@SideOnly(Side.CLIENT)
    public IIcon getIcon() {
    	return getStillIcon();
    }

	@Override
	@SideOnly(Side.CLIENT)
    public IIcon getStillIcon() {
        return MudBlock.stillIcon;
    }

	@Override
	@SideOnly(Side.CLIENT)
    public IIcon getFlowingIcon() {
        return MudBlock.flowingIcon;
    }
}
