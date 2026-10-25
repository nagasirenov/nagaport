package com.hbm.blocks.generic;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import com.hbm.render.icon.IIcon;

public class BlockToolConversionPillar extends BlockToolConversion {

	public IIcon[] topIcons;
	public IIcon topIcon;

	public BlockToolConversionPillar(Material mat) {
		super(mat);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {

		this.blockIcon = iconRegister.registerIcon(this.getRegistryName().toString() + "_side");
		this.topIcon = iconRegister.registerIcon(this.getRegistryName().toString() + "_top");
		
		if(names != null) {
			icons = new IIcon[names.length];
			topIcons = new IIcon[names.length];
			
			for(int i = 0; i < names.length; i++) {
				icons[i] = iconRegister.registerIcon(getTextureName() + "_side" + names[i]);
				topIcons[i] = iconRegister.registerIcon(getTextureName() + "_top" + names[i]);
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		
		metadata -= 1;
		
		if(metadata == -1 || icons == null || metadata >= icons.length) {
			return side == 0 || side == 1 ? topIcon : blockIcon;
		}
		
		return side == 0 || side == 1 ? topIcons[metadata] : icons[metadata];
	}
}
