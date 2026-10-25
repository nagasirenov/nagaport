package com.hbm.blocks.generic;

import java.util.List;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import com.hbm.render.icon.IIcon;

public class BlockConcreteColored extends Block {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] icons;

	public BlockConcreteColored(Material p_i45398_1_) {
		super(p_i45398_1_);
		this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
	}
	
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int p_149691_1_, int p_149691_2_) {
		return this.icons[p_149691_2_ % this.icons.length];
	}
	
	public int damageDropped(int p_149692_1_) {
		return p_149692_1_;
	}

	public static int func_150032_b(int p_150032_0_) {
		return func_150031_c(p_150032_0_);
	}

	public static int func_150031_c(int p_150031_0_) {
		return ~p_150031_0_ & 15;
	}
	
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item p_149666_1_, CreativeTabs p_149666_2_, List p_149666_3_) {
		for(int i = 0; i < 16; ++i) {
			p_149666_3_.add(new ItemStack(p_149666_1_, 1, i));
		}
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister p_149651_1_) {
		this.icons = new IIcon[16];
		// The original built color icons from the setBlockTextureName base "concrete".
		String base = com.hbm.blocks.LegacyBlockTextures.texture(this);
		if(base == null) base = this.getRegistryName().toString();

		for(int i = 0; i < this.icons.length; ++i) {
			this.icons[i] = p_149651_1_.registerIcon(base + "_" + net.minecraft.item.EnumDyeColor.byDyeDamage(func_150031_c(i)).getDyeColorName());
		}
	}

	public MapColor getMapColor(int p_149728_1_) {
		return new net.minecraft.block.material.MapColor[] {net.minecraft.block.material.MapColor.SNOW, net.minecraft.block.material.MapColor.ADOBE, net.minecraft.block.material.MapColor.MAGENTA, net.minecraft.block.material.MapColor.LIGHT_BLUE, net.minecraft.block.material.MapColor.YELLOW, net.minecraft.block.material.MapColor.LIME, net.minecraft.block.material.MapColor.PINK, net.minecraft.block.material.MapColor.GRAY, net.minecraft.block.material.MapColor.SILVER, net.minecraft.block.material.MapColor.CYAN, net.minecraft.block.material.MapColor.PURPLE, net.minecraft.block.material.MapColor.BLUE, net.minecraft.block.material.MapColor.BROWN, net.minecraft.block.material.MapColor.GREEN, net.minecraft.block.material.MapColor.RED, net.minecraft.block.material.MapColor.BLACK}[p_149728_1_ % 16];
	}
}
