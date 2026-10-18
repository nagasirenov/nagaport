package com.hbm.blocks.machine.rbmk;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import com.hbm.render.icon.IIcon;

public abstract class RBMKPipedBase extends RBMKBase {
	
	public static boolean renderPipes = false;

	public IIcon pipeTextureTop;
	public IIcon pipeTextureSide;

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		super.registerBlockIcons(reg);
		this.pipeTextureTop = reg.registerIcon(this.getTextureName() + "_pipe_top");
		this.pipeTextureSide = reg.registerIcon(this.getTextureName() + "_pipe_side");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		if(renderPipes) return side == 0 || side == 1 ? pipeTextureTop : pipeTextureSide;
		return super.getIcon(side, meta);
	}
	
	@Override
	public int getRenderType(){
		return this.renderIDControl;
	}
}
