package com.hbm.blocks;

import java.util.Random;

import com.hbm.lib.RefStrings;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fluids.BlockFluidBase;

public class RubberGrass extends Block {

	@SideOnly(Side.CLIENT)
	private IIcon iconTop;
	@SideOnly(Side.CLIENT)
	private IIcon iconBottom;

	public RubberGrass(Material mat, boolean tick) {
		super(mat);
		this.setTickRandomly(tick);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.iconTop = iconRegister.registerIcon(RefStrings.MODID + ":rubber_grass_top");
		this.iconBottom = iconRegister.registerIcon(RefStrings.MODID + ":rubber_silt");
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":rubber_grass_side");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return side == 1 ? this.iconTop : (side == 0 ? this.iconBottom : this.blockIcon);
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState p_149650_1_, Random p_149650_2_, int p_149650_3_) {
		return Item.getItemFromBlock(this);
	}

	@Override
	public int quantityDropped(Random p_149745_1_) {
		return 1;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		if(this == ModBlocks.rubber_grass) {
			net.minecraft.block.state.IBlockState nativeCubeState_b = com.hbm.world.WorldBlockState.read(world, x, y + 1, z);
			Block b = nativeCubeState_b.getBlock();
			if(b instanceof BlockLiquid || b instanceof BlockFluidBase || b.isNormalCube(nativeCubeState_b)) {
				{ net.minecraft.world.World nativeRegisteredWriteWorld = world; int nativeRegisteredWriteX = x; int nativeRegisteredWriteY = y; int nativeRegisteredWriteZ = z; net.minecraft.block.Block nativeRegisteredWriteBlock = ModBlocks.rubber_silt; int nativeRegisteredWriteMeta = 0; int nativeRegisteredWriteFlags = 3; if (nativeRegisteredWriteWorld == null) throw new NullPointerException(); if (nativeRegisteredWriteX >= -30000000 && nativeRegisteredWriteX < 30000000 && nativeRegisteredWriteZ >= -30000000 && nativeRegisteredWriteZ < 30000000 && nativeRegisteredWriteY >= 0 && nativeRegisteredWriteY < 256) com.hbm.world.WorldBlockState.write(nativeRegisteredWriteWorld, nativeRegisteredWriteX, nativeRegisteredWriteY, nativeRegisteredWriteZ, nativeRegisteredWriteBlock.getStateFromMeta(nativeRegisteredWriteMeta), nativeRegisteredWriteFlags); }
			}
		}
	}

	@Override
	public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction, IPlantable plantable) {
		return false;
	}

}
