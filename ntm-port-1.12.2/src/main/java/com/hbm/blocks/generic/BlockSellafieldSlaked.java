package com.hbm.blocks.generic;

import java.awt.Color;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.EnumCreatureType;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.IBlockAccess;

public class BlockSellafieldSlaked extends Block {
	public BlockSellafieldSlaked withSoundType(net.minecraft.block.SoundType sound) {
		super.setSoundType(sound);
		return this;
	}

	public static final PropertyInteger META = PropertyInteger.create("metadata", 0, 15);

	public IIcon[] icons;

	public BlockSellafieldSlaked(Material mat) {
		super(mat);
		if(this.blockState.getBaseState().getPropertyKeys().contains(META)) { this.setDefaultState(this.blockState.getBaseState().withProperty(META, 0)); }
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, META);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(META, meta & 15);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(META);
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
		long l = (long) (x * 3129871) ^ (long)y * 116129781L ^ (long)z;
		l = l * l * 42317861L + l * 11L;
		int i = (int)(l >> 16 & 3L);
		return icons[(int)(Math.abs(i) % icons.length)];
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return icons[0];
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		icons = new IIcon[4];
		icons[0] = reg.registerIcon(RefStrings.MODID + ":sellafield_slaked");
		icons[1] = reg.registerIcon(RefStrings.MODID + ":sellafield_slaked_1");
		icons[2] = reg.registerIcon(RefStrings.MODID + ":sellafield_slaked_2");
		icons[3] = reg.registerIcon(RefStrings.MODID + ":sellafield_slaked_3");
	}

	@SideOnly(Side.CLIENT)
	public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
		int meta = com.hbm.world.WorldBlockState.readMetadata(world, x, y, z);
		return Color.HSBtoRGB(0F, 0F, 1F - meta / 15F);
	}

	@Override
	public boolean canCreatureSpawn(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos, net.minecraft.entity.EntityLiving.SpawnPlacementType type) {
		return this == ModBlocks.sellafield_bedrock ? false : super.canCreatureSpawn(state, world, pos, type);
	}
}
