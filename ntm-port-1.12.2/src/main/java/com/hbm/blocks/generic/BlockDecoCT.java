package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.render.block.ct.CTContext;
import com.hbm.render.block.ct.IBlockCT;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.world.IBlockAccess;

public class BlockDecoCT extends Block implements IBlockCT {
	public boolean allowFortune = true;

	public BlockDecoCT(Material mat) {
		super(mat);
        setDefaultState(blockState.getBaseState().withProperty(CTContext.METADATA, 0));
	}

    @Override protected net.minecraft.block.state.BlockStateContainer createBlockState() { return CTContext.stateContainer(this); }
    @Override public net.minecraft.block.state.IBlockState getStateFromMeta(int metadata) { return getDefaultState().withProperty(CTContext.METADATA, metadata & 15); }
    @Override public int getMetaFromState(net.minecraft.block.state.IBlockState state) { return state.getValue(CTContext.METADATA); }
    @Override public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) { return net.minecraft.util.EnumBlockRenderType.MODEL; }
    @Override public net.minecraft.block.state.IBlockState getExtendedState(net.minecraft.block.state.IBlockState state, IBlockAccess world, net.minecraft.util.math.BlockPos pos) {
        return CTContext.extend(state, world, pos, this);
    }



	@Override
	public boolean canConnect(IBlockAccess world, int x, int y, int z, Block block) {
		// i don't care
		if(this == ModBlocks.deco_steel && block == ModBlocks.deco_rusty_steel) return true;
		if(this == ModBlocks.deco_rusty_steel && block == ModBlocks.deco_steel) return true;
		return this == block;
	}
	
	public BlockDecoCT noFortune() {
		this.allowFortune = false;
		return this;
	}
	
	@Override
	public int quantityDroppedWithBonus(int fortune, Random rand) {

		if(fortune > 0 && Item.getItemFromBlock(this) != this.getItemDropped(getStateFromMeta(0), rand, fortune) && allowFortune) {
			int mult = rand.nextInt(fortune + 2) - 1;

			if(mult < 0) {
				mult = 0;
			}

			return this.quantityDropped(rand) * (mult + 1);
		} else {
			return this.quantityDropped(rand);
		}
	}
}
