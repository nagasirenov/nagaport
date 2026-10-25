package com.hbm.blocks.generic;

import java.util.List;
import java.util.Random;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.BlockDirt;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import com.hbm.util.i18n.I18nUtil;

public class BlockNTMDirt extends BlockDirt {
	public BlockNTMDirt withSoundType(net.minecraft.block.SoundType sound) {
		super.setSoundType(sound);
		return this;
	}

	@Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(net.minecraft.creativetab.CreativeTabs p_149666_2_, net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> p_149666_3_)
    {
        p_149666_3_.add(new ItemStack(this, 1, 0));
    }

    public String getLocalizedName()
    {
		return ("" + I18nUtil.resolveKey(Blocks.DIRT.getTranslationKey()) + ".name").trim();
    }

    @Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState p_149650_1_, Random p_149650_2_, int p_149650_3_)
    {
        return Item.getItemFromBlock(Blocks.DIRT);
    }

}
