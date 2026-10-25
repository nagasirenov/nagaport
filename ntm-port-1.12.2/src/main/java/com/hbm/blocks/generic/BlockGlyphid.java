package com.hbm.blocks.generic;

import java.util.List;
import java.util.Random;

import com.hbm.blocks.IBlockMulti;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.IBlockAccess;

public class BlockGlyphid extends Block implements IBlockMulti {
	public BlockGlyphid withSoundType(net.minecraft.block.SoundType sound) {
		super.setSoundType(sound);
		return this;
	}


	public IIcon[] iconsStandard = new IIcon[2];
	public IIcon[] iconsInfested = new IIcon[2];
	public IIcon[] iconsRad = new IIcon[2];

	public BlockGlyphid(Material mat) {
		super(mat);
		this.setCreativeTab(MainRegistry.blockTab);
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState meta, Random rand, int fortune) {
		return null;
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
		long l = (long) (x * 3129871) ^ (long)y * 116129781L ^ (long)z;
		l = l * l * 42317861L + l * 11L;
		int i = (int)(l >> 16 & 3L);
		IIcon[] icons = this.getIconArray(com.hbm.world.WorldBlockState.readMetadata(world, x, y, z));
		return icons[(int)(Math.abs(i) % icons.length)];
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		IIcon[] icons = this.getIconArray(meta);
		return icons[meta % icons.length];
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		iconsStandard[0] = reg.registerIcon(RefStrings.MODID + ":glyphid_base");
		iconsStandard[1] = reg.registerIcon(RefStrings.MODID + ":glyphid_base_alt");
		iconsInfested[0] = reg.registerIcon(RefStrings.MODID + ":glyphid_base_infested");
		iconsInfested[1] = reg.registerIcon(RefStrings.MODID + ":glyphid_base_infested_alt");
		iconsRad[0] = reg.registerIcon(RefStrings.MODID + ":glyphid_base_rad");
		iconsRad[1] = reg.registerIcon(RefStrings.MODID + ":glyphid_base_rad_alt");
	}

	protected IIcon[] getIconArray(int meta) {
		if(meta == 1) return this.iconsInfested;
		if(meta == 2) return this.iconsRad;
		return this.iconsStandard;
	}

	@Override
	public int getSubCount() {
		return 3;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(net.minecraft.creativetab.CreativeTabs tab, net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> list) {
	net.minecraft.item.Item item = net.minecraft.item.Item.getItemFromBlock(this);
		for(int i = 0; i < getSubCount(); ++i) list.add(new ItemStack(item, 1, i));
	}
}
