package com.hbm.blocks.generic;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import com.hbm.render.icon.IIcon;

public class BlockSlag extends BlockBeaconable {
	
	@SideOnly(Side.CLIENT)
	private IIcon iconAlt;

	public BlockSlag(Material mat) {
		super(mat);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		super.registerBlockIcons(iconRegister);
		this.iconAlt = iconRegister.registerIcon(this.getTextureName() + "_broken");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		if(metadata == 1) return this.iconAlt;
		
		return this.blockIcon;
	}
}
