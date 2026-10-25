package com.hbm.blocks.fluid;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIcon;
import net.minecraftforge.fluids.Fluid;

public class ToxicFluid extends Fluid {

	public ToxicFluid() {
		super("toxic_fluid", new net.minecraft.util.ResourceLocation("hbm", "blocks/toxic_still"), new net.minecraft.util.ResourceLocation("hbm", "blocks/toxic_flowing"));
	}

	@SideOnly(Side.CLIENT)
    public IIcon getIcon() {
    	return getStillIcon();
    }

	@SideOnly(Side.CLIENT)
    public IIcon getStillIcon() {
        return ToxicBlock.stillIcon;
    }

	@SideOnly(Side.CLIENT)
    public IIcon getFlowingIcon() {
        return ToxicBlock.flowingIcon;
    }
}
