package com.hbm.blocks.rail;

import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;

@Deprecated
public interface IRenderBlock {

	public static int renderID = RenderingRegistry.getNextAvailableRenderId();

	@SideOnly(Side.CLIENT) public void renderInventory(Tessellator tessellator, Block block, int metadata);
	@SideOnly(Side.CLIENT) public void renderWorld(Tessellator tessellator, Block block, int meta, IBlockAccess world, int x, int y, int z);
}
