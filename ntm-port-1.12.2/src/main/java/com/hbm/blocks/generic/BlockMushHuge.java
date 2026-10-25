package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.item.Item;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;

public class BlockMushHuge extends Block {

	protected IIcon blockIcon;
	public BlockMushHuge withSoundType(net.minecraft.block.SoundType sound) {
		super.setSoundType(sound);
		return this;
	}


	@SideOnly(Side.CLIENT)
	private IIcon iconTop;

	public BlockMushHuge(Material p_i45394_1_) {
		super(p_i45394_1_);
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.iconTop = iconRegister.registerIcon(RefStrings.MODID + (this == ModBlocks.mush_block ? ":mush_block_skin" : ":mush_block_inside"));
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + (this == ModBlocks.mush_block ? ":mush_block_skin" : ":mush_block_stem"));
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return side == 1 ? this.iconTop : (side == 0 ? this.iconTop : this.blockIcon);
	}

    @Override
	public int quantityDropped(Random p_149745_1_)
    {
        int i = p_149745_1_.nextInt(10) - 7;

        if (i < 0)
        {
            i = 0;
        }

        return i;
    }

    @Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState p_149650_1_, Random p_149650_2_, int p_149650_3_)
    {
        return Item.getItemFromBlock(ModBlocks.mush);
    }

	@SideOnly(Side.CLIENT)
    public net.minecraft.item.ItemStack getItem(World p_149694_1_, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state)
    {
    	int p_149694_2_ = pos.getX(), p_149694_3_ = pos.getY(), p_149694_4_ = pos.getZ();
        return new net.minecraft.item.ItemStack(Item.getItemFromBlock(ModBlocks.mush));
    }

}
