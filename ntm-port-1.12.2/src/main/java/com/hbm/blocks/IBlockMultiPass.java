package com.hbm.blocks;

import net.minecraftforge.fml.client.registry.RenderingRegistry;

public interface IBlockMultiPass {

	public int getPasses();

	public static int renderID = RenderingRegistry.getNextAvailableRenderId();
	public static int getRenderType() {
		return renderID;
	}
	
	public default boolean shouldRenderItemMulti() {
		return false;
	}
}
