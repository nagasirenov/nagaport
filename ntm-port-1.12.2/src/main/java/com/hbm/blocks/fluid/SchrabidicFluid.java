package com.hbm.blocks.fluid;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIcon;
import net.minecraftforge.fluids.Fluid;

public class SchrabidicFluid extends Fluid {

	public SchrabidicFluid() {
		super("schrabidic_fluid", new net.minecraft.util.ResourceLocation("hbm", "blocks/schrabidic_still"), new net.minecraft.util.ResourceLocation("hbm", "blocks/schrabidic_flowing"));
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon() {
		return getStillIcon();
	}

	@SideOnly(Side.CLIENT)
	public IIcon getStillIcon() {
		return SchrabidicBlock.stillIcon;
	}

	@SideOnly(Side.CLIENT)
	public IIcon getFlowingIcon() {
		return SchrabidicBlock.flowingIcon;
	}
}
