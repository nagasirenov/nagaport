package com.hbm.blocks.machine;

import com.hbm.blocks.BlockBase;
import com.hbm.lib.RefStrings;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import com.hbm.render.icon.IIcon;

public class MachineTeleanchor extends BlockBase {

	@SideOnly(Side.CLIENT) private IIcon iconTop;
	
	public MachineTeleanchor() {
		super(Material.iron);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.iconTop = iconRegister.registerIcon(RefStrings.MODID + ":tele_anchor_top");
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":tele_anchor_side");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return side == 1 ? this.iconTop : this.blockIcon;
	}
}
