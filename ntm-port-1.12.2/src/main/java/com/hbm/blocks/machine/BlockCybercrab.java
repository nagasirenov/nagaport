package com.hbm.blocks.machine;

import java.util.Random;

import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityCyberCrab;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;

public class BlockCybercrab extends BlockContainer {
	
	@SideOnly(Side.CLIENT)
	private IIcon iconTop;
	@SideOnly(Side.CLIENT)
	private IIcon iconBottom;

	public BlockCybercrab(Material mat) {
		super(mat);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.iconTop = iconRegister.registerIcon(RefStrings.MODID + ":meteor_spawner_top");
		this.iconBottom = iconRegister.registerIcon(RefStrings.MODID + ":meteor_polished");
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":meteor_spawner_side");
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityCyberCrab();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return side == 1 ? this.iconTop : (side == 0 ? this.iconBottom : this.blockIcon);
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState p_149650_1_, Random p_149650_2_, int p_149650_3_) {
		return null;
    }
}
