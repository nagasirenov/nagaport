package com.hbm.blocks.fluid;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIcon;
import net.minecraftforge.fluids.Fluid;

public class RadFluid extends Fluid {

	public RadFluid() {
		super("rad_lava_fluid", new net.minecraft.util.ResourceLocation("hbm", "blocks/rad_lava_still"), new net.minecraft.util.ResourceLocation("hbm", "blocks/rad_lava_flowing"));
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon() {
		return getStillIcon();
	}

	@SideOnly(Side.CLIENT)
	public IIcon getStillIcon() {
		return RadBlock.stillIconRad;
	}

	@SideOnly(Side.CLIENT)
	public IIcon getFlowingIcon() {
		return RadBlock.flowingIconRad;
	}
}
