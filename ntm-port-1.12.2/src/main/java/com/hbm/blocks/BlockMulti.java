package com.hbm.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public abstract class BlockMulti extends BlockBase implements IBlockMulti {

	public BlockMulti() {
		super();
	}

	public BlockMulti(Material mat) {
		super(mat);
	}

	@Override
	public int damageDropped(int meta) {
		return rectify(meta);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
		for(int i = 0; i < getSubCount(); ++i) {
			items.add(new ItemStack(this, 1, i));
		}
	}
}
