package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.ModBlocks;

import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class BlockPinkSlab extends BlockSlab {

	public static final PropertyInteger VARIANT = PropertyInteger.create("variant", 0, 7);
	private boolean doubleSlab;

	public BlockPinkSlab withSoundType(net.minecraft.block.SoundType sound) {
		super.setSoundType(sound);
		return this;
	}


	public BlockPinkSlab(boolean bool, Material mat) {
		super(mat);
		this.doubleSlab = bool;
		this.fullBlock = bool;
		this.setDefaultState(this.blockState.getBaseState()
				.withProperty(HALF, EnumBlockHalf.BOTTOM)
				.withProperty(VARIANT, 0));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, HALF, VARIANT);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState()
				.withProperty(HALF, (meta & 8) == 0 ? EnumBlockHalf.BOTTOM : EnumBlockHalf.TOP)
				.withProperty(VARIANT, meta & 7);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(VARIANT)
				| (state.getValue(HALF) == EnumBlockHalf.TOP ? 8 : 0);
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState i, Random rand, int j) {
		return Item.getItemFromBlock(ModBlocks.pink_slab);
	}

	@Override
	protected ItemStack getSilkTouchDrop(net.minecraft.block.state.IBlockState state) {
		int i = com.hbm.world.WorldBlockState.metadata(state);
		return new ItemStack(Item.getItemFromBlock(ModBlocks.pink_slab), 2, i & 7);
	}

	@Override
	public String getTranslationKey(int p_150002_1_) {
		return null;
	}

	@Override
	public boolean isDouble() { return doubleSlab; }

	@Override
	public Comparable<?> getTypeForItem(net.minecraft.item.ItemStack stack) { return stack.getMetadata() & 7; }

	@Override
	public IProperty<?> getVariantProperty() { return VARIANT; }

	public BlockPinkSlab setDouble(boolean dbl) {
		this.doubleSlab = dbl;
		this.fullBlock = dbl;
		return this;
	}
}
