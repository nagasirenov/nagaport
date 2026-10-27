package com.hbm.blocks.generic;

import net.minecraft.block.BlockLadder;

public class BlockNTMLadder extends BlockLadder {

	public BlockNTMLadder() {
		super();
	}

	@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public net.minecraft.util.BlockRenderLayer getRenderLayer() {
		return net.minecraft.util.BlockRenderLayer.CUTOUT;
	}
}
