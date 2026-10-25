package com.hbm.blocks.generic;

import java.util.List;

import com.hbm.lib.RefStrings;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import com.hbm.render.icon.IIcon;

public class BlockGlyph extends Block {

	protected IIcon blockIcon;
	
	@SideOnly(Side.CLIENT)
	private IIcon[] glyphIcons;

	public BlockGlyph(Material material) {
		super(material);
	}
	
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":brick_jungle");
		
		this.glyphIcons = new IIcon[16];
		
		for(int i = 0; i < 16; i++)
			this.glyphIcons[i] = iconRegister.registerIcon(RefStrings.MODID + ":brick_jungle_glyph_" + i);
	}
	
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return side == 1 ? this.blockIcon : (side == 0 ? this.blockIcon : (this.glyphIcons[metadata]));
	}

	@Override
    public int damageDropped(net.minecraft.block.state.IBlockState state)
    {
    	int p_149692_1_ = state.getBlock().getMetaFromState(state);
        return p_149692_1_;
    }
	
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item p_149666_1_, CreativeTabs p_149666_2_, List p_149666_3_)
    {
        for (int i = 0; i < 16; ++i) {
            p_149666_3_.add(new ItemStack(p_149666_1_, 1, i));
        }
    }
}
