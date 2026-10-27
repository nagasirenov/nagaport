package com.hbm.blocks.machine;

import java.util.Random;

import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityDecon;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;

public class BlockDecon extends BlockContainer {

	protected IIcon blockIcon;

	@SideOnly(Side.CLIENT)
	private IIcon iconTop;
	
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		
		this.iconTop = iconRegister.registerIcon(RefStrings.MODID + ":decon_top");
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":decon_side");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		
		return side == 1 ? this.iconTop : this.blockIcon;
	}

	public BlockDecon(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityDecon();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(net.minecraft.block.state.IBlockState state, World world, net.minecraft.util.math.BlockPos pos, Random rand) { }


	@Override
	public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.MODEL;
	}
}
