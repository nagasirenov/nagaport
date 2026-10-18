package com.hbm.blocks.generic;

import com.hbm.render.block.ct.CT;
import com.hbm.render.block.ct.CTStitchReceiver;
import com.hbm.render.block.ct.IBlockCT;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.IBlockAccess;

public class BlockNTMGlassCT extends BlockNTMGlass implements IBlockCT {

	public BlockNTMGlassCT(int layer, String name, Material material) {
		super(layer, name, material);
	}

	public BlockNTMGlassCT(int layer, String name, Material material, boolean doesDrop) {
		super(layer, name, material, doesDrop);
	}

	@Override
	public int getRenderType() {
		return CT.renderID;
	}

	@SideOnly(Side.CLIENT)
	public CTStitchReceiver rec;

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		super.registerBlockIcons(reg);
		this.rec = IBlockCT.primeReceiver(reg, this.blockIcon.getIconName(), this.blockIcon);
	}

	@Override
	public IIcon[] getFragments(IBlockAccess world, int x, int y, int z) {
		return rec.fragCache;
	}
}
