package com.hbm.blocks;

import com.hbm.lib.RefStrings;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.init.Blocks;
import com.hbm.render.icon.IIcon;

public class BlockAstroturf extends Block {

	@SideOnly(Side.CLIENT)
	private IIcon iconTop;

	protected BlockAstroturf(Material mat) {
		super(mat);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.iconTop = iconRegister.registerIcon(RefStrings.MODID + ":astroturf_top");
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":astroturf_side");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return side == 1 ? this.iconTop : (side == 0 ? Blocks.dirt.getBlockTextureFromSide(side) : this.blockIcon);
	}
	
}
