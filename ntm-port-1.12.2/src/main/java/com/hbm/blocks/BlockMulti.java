package com.hbm.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public abstract class BlockMulti extends BlockBase implements IBlockMulti {

	/**
	 * Stored block metadata is a four-bit value, not necessarily a subtype index:
	 * some descendants also encode orientation or the upper half of a plant.
	 * Keep all bits here and leave item/drop rectification to the owning callback.
	 * Descendants with their own native state container override all three methods.
	 */
	public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);

	public BlockMulti() {
		super();
	}

	public BlockMulti(Material mat) {
		super(mat);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, METADATA);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(METADATA, meta & 15);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(METADATA);
	}

	@Override
	public int damageDropped(IBlockState state) {
		return damageDropped(getMetaFromState(state));
	}

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
