package com.hbm.blocks.generic;

import java.util.Random;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;
import com.hbm.lib.ForgeDirection;

public class BlockNTMGlassPane extends BlockPane
{
	int renderLayer;
	boolean doesDrop = false;

    //NOTE when you have eclipse make the constructor for you it *WILL BE 'protected'* so make sure to make this public like below.
	/*public BlockNTMGlassPane(String flatFaceTextureName, String rimTextureName,
		Material mat, boolean bool) {
	super(flatFaceTextureName, rimTextureName, mat, bool);
	// TODO Auto-generated constructor stub
		this.setLightOpacity(1);
		this.opaque = true;
	}*/

	public BlockNTMGlassPane(int layer, String name, String rimTextureName, Material material, boolean doesDrop) {
		super(material, false);
		this.renderLayer = layer;
		this.doesDrop = doesDrop;
		
		this.setLightOpacity(1);
	}

	@Override
	public boolean canPaneConnectTo(net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumFacing dir) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		Block b = com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock();
		return super.canPaneConnectTo(world, pos, dir) || b instanceof BlockNTMGlass;
	}

	@SideOnly(Side.CLIENT)
	public int getRenderBlockPass() {
		return renderLayer;
	}

	@Override
	public int quantityDropped(Random rand) {
		return doesDrop ? 1 : 0;
	}


	@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public net.minecraft.util.BlockRenderLayer getRenderLayer() {
		return renderLayer == 1 ? net.minecraft.util.BlockRenderLayer.TRANSLUCENT : net.minecraft.util.BlockRenderLayer.CUTOUT_MIPPED;
	}
}
