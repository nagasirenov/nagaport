package com.hbm.blocks.fluid;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIcon;
import net.minecraftforge.fluids.Fluid;

public class AcidFluid extends Fluid {

	public AcidFluid() {
		super("acid_fluid", new net.minecraft.util.ResourceLocation("hbm", "blocks/acid_still"), new net.minecraft.util.ResourceLocation("hbm", "blocks/acid_flowing"));
	}

	public AcidFluid(String name) {
		super(name, new net.minecraft.util.ResourceLocation("hbm", "blocks/" + name + "_still"), new net.minecraft.util.ResourceLocation("hbm", "blocks/" + name + "_flowing"));
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon() {
		return getStillIcon();
	}

	@SideOnly(Side.CLIENT)
	public IIcon getStillIcon() {
		return AcidBlock.stillIcon;
	}

	@SideOnly(Side.CLIENT)
	public IIcon getFlowingIcon() {
		return AcidBlock.flowingIcon;
	}
}
