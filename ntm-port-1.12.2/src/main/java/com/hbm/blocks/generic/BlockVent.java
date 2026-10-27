package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.lib.RefStrings;
import com.hbm.tileentity.deco.TileEntityVent;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;

public class BlockVent extends BlockContainer {

	protected IIcon blockIcon;
	
	@SideOnly(Side.CLIENT)
	private IIcon iconTop;

	public BlockVent(Material p_i45386_1_) {
		super(p_i45386_1_);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityVent();
	}
	
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.blockIcon = iconRegister.registerIcon(this.getRegistryName().toString());
		this.iconTop = iconRegister.registerIcon(RefStrings.MODID + ":vent_blank");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return side == 1 ? this.iconTop : (side == 0 ? this.iconTop : this.blockIcon);
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState p_149650_1_, Random p_149650_2_, int p_149650_3_)
    {
        return null;
    }


	@Override
	public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.MODEL;
	}
}
