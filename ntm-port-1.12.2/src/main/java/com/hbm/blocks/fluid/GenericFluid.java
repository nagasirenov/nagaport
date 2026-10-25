package com.hbm.blocks.fluid;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIcon;
import net.minecraftforge.fluids.Fluid;

public class GenericFluid extends Fluid {

	public GenericFluid(String name) {
		super(name, new net.minecraft.util.ResourceLocation("hbm", "blocks/" + name + "_still"), new net.minecraft.util.ResourceLocation("hbm", "blocks/" + name + "_flowing"));
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon() {
		return getStillIcon();
	}

	@SideOnly(Side.CLIENT)
	public IIcon getStillIcon() {
		return com.hbm.render.icon.LegacyIconRegister.wrapSprite(net.minecraft.client.Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(net.minecraft.item.Item.getItemFromBlock(this.block), 0));
	}

	@SideOnly(Side.CLIENT)
	public IIcon getFlowingIcon() {
		return com.hbm.render.icon.LegacyIconRegister.wrapSprite(net.minecraft.client.Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(net.minecraft.item.Item.getItemFromBlock(this.block), 1));
	}
}
