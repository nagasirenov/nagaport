package com.hbm.blocks.machine.rbmk;

import com.hbm.blocks.ModBlocks;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKControlAuto;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;

public class RBMKControlAuto extends RBMKPipedBase {

	public IIcon textureBottom;

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		super.registerBlockIcons(reg);
		if(this == ModBlocks.rbmk_control_reasim_auto)
			this.textureBottom = reg.registerIcon(this.getRBMKTexture() + "_bottom");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		if(this.renderLid == LID_NONE && this == ModBlocks.rbmk_control_reasim_auto && side == 0) return textureBottom;
		return super.getIcon(side, meta);
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= this.offset) return new TileEntityRBMKControlAuto();
		return null;
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		return openInv(world, x, y, z, player);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	}
}
