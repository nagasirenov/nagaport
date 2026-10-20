package com.hbm.blocks.machine;

import com.hbm.lib.RefStrings;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import com.hbm.render.icon.IIconRegister;
import com.hbm.render.icon.IIcon;

public class BlockPillar extends Block {

	// The old world retained all four metadata bits even though these icons ignore them.
	public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);

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

	// Owned here rather than on native Block, which no longer has legacy textures.
	protected String textureName;
	@SideOnly(Side.CLIENT)
	protected IIcon blockIcon;

	@SideOnly(Side.CLIENT)
	private IIcon iconTop;
	@SideOnly(Side.CLIENT)
	private IIcon iconAlt;

	private String textureTop = "";
	private String textureAlt = "";

	public BlockPillar(Material mat, String top) {
		super(mat);
		textureTop = top;
	}

	public BlockPillar(Material mat, String top, String bottom) {
		this(mat, top);
		textureAlt = bottom;
	}

	public Block setBlockTextureName(String name) {

		if(textureTop.isEmpty()) textureTop = name;
		if(textureAlt.isEmpty()) textureAlt = name;
		this.textureName = name;
		return this;
	}
	
	/** Original side selection, including the deliberately unused alternate icon. */
	public String getTextureName(int side) {
		return side == 0 || side == 1 ? textureTop : textureName;
	}

	public String getAlternateTextureName() {
		return textureAlt.isEmpty() ? RefStrings.MODID + ":code" : textureAlt;
	}

	/** 1.7 block-atlas paths are relative to textures/blocks, not textures. */
	private static String atlasName(String name) {
		net.minecraft.util.ResourceLocation texture = new net.minecraft.util.ResourceLocation(name);
		return new net.minecraft.util.ResourceLocation(texture.getNamespace(), "blocks/" + texture.getPath()).toString();
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		
		this.iconAlt = iconRegister.registerIcon(atlasName(getAlternateTextureName()));
		this.iconTop = iconRegister.registerIcon(atlasName(textureTop));
		this.blockIcon = iconRegister.registerIcon(atlasName(this.textureName));
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return side == 1 ? this.iconTop : (side == 0 ? this.iconTop : this.blockIcon);
	}
}
