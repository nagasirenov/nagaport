package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.IBlockMultiPass;
import com.hbm.blocks.MultipassContext;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;

public class BlockSellafieldOre extends BlockSellafieldSlaked implements IBlockMultiPass {
    public BlockSellafieldOre withSoundType(net.minecraft.block.SoundType sound) { setSoundType(sound); return this; }


    @Override protected net.minecraft.block.state.BlockStateContainer createBlockState() { return MultipassContext.stateContainer(this); }
    @Override public net.minecraft.block.state.IBlockState getStateFromMeta(int metadata) { return getDefaultState().withProperty(MultipassContext.METADATA, metadata & 15); }
    @Override public int getMetaFromState(net.minecraft.block.state.IBlockState state) { return state.getValue(MultipassContext.METADATA); }
    @Override public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) { return net.minecraft.util.EnumBlockRenderType.MODEL; }
    @Override public net.minecraft.util.BlockRenderLayer getRenderLayer() { return net.minecraft.util.BlockRenderLayer.CUTOUT_MIPPED; }
    @Override public net.minecraft.block.state.IBlockState getExtendedState(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos) {
        return ((net.minecraftforge.common.property.IExtendedBlockState) state).withProperty(MultipassContext.PROPERTY, getMultipassContext(state, world, pos));
    }
    @Override public MultipassContext getMultipassContext(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos) {
        int metadata = getMetaFromState(state);
        long l = (long) (pos.getX() * 3129871) ^ (long) pos.getY() * 116129781L ^ (long) pos.getZ();
        l = l * l * 42317861L + l * 11L;
        return MultipassContext.of(metadata, null, false, java.awt.Color.HSBtoRGB(0F, 0F, 1F - metadata / 15F), 0, 6, 6, (int)(l >> 16 & 3L));
    }


	public BlockSellafieldOre(Material mat) {
		super(mat);
	}











	@Override
	public int getPasses() {
		return 2;
	}

	@Override
	public boolean shouldRenderItemMulti() {
		return true;
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState meta, Random rand, int fortune) {
		if(this == ModBlocks.ore_sellafield_diamond) return Items.DIAMOND;
		if(this == ModBlocks.ore_sellafield_emerald) return Items.EMERALD;
		if(this == ModBlocks.ore_sellafield_radgem) return ModItems.gem_rad;
		return Item.getItemFromBlock(this);
	}

	@Override
	public int quantityDropped(Random rand) {
		return 1;
	}

	@Override
	public int quantityDroppedWithBonus(int fortune, Random rand) {
		if(fortune > 0 && Item.getItemFromBlock(this) != this.getItemDropped(getStateFromMeta(0), rand, fortune)) {
			int j = rand.nextInt(fortune + 2) - 1;
			if(j < 0) j = 0;
			return this.quantityDropped(rand) * (j + 1);
		} else {
			return this.quantityDropped(rand);
		}
	}

	private Random rand = new Random();

	@Override
	public int getExpDrop(IBlockAccess world, int meta, int fortune) {
		if(this.getItemDropped(getStateFromMeta(meta), rand, fortune) != Item.getItemFromBlock(this)) {
			int j1 = 0;

			if(this == ModBlocks.ore_sellafield_diamond) j1 = MathHelper.getRandomIntegerInRange(rand, 3, 7);
			if(this == ModBlocks.ore_sellafield_emerald) j1 = MathHelper.getRandomIntegerInRange(rand, 3, 7);
			if(this == ModBlocks.ore_sellafield_radgem) j1 = MathHelper.getRandomIntegerInRange(rand, 3, 7);
			
			return j1;
		}
		return 0;
	}
}
