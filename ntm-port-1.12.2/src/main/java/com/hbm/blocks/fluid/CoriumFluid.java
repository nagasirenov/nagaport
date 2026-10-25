package com.hbm.blocks.fluid;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIcon;
import net.minecraftforge.fluids.Fluid;

public class CoriumFluid extends Fluid {

	public CoriumFluid() {
		super("corium_fluid", new net.minecraft.util.ResourceLocation("hbm", "blocks/corium_still"), new net.minecraft.util.ResourceLocation("hbm", "blocks/corium_flowing"));
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon() {
		return getStillIcon();
	}

	@SideOnly(Side.CLIENT)
	public IIcon getStillIcon() {
		return CoriumBlock.stillIcon;
	}

	@SideOnly(Side.CLIENT)
	public IIcon getFlowingIcon() {
		return CoriumBlock.flowingIcon;
	}
}
