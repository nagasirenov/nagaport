package com.hbm.blocks.generic;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

public class BlockGenericDrop extends BlockGeneric {

	private Block drops;

	public BlockGenericDrop(Material material, Block drops) {
		super(material);
		this.drops = drops;
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState meta, Random rand, int fortune) {
		return Item.getItemFromBlock(drops);
	}

}
