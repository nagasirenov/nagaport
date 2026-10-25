package com.hbm.blocks.generic;

import java.util.ArrayList;

import com.hbm.blocks.BlockEnumMulti;
import com.hbm.blocks.BlockEnums;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.OreDictManager.DictFrame;
import com.hbm.items.ItemEnums.EnumChunkType;
import com.hbm.items.ModItems;

import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class BlockResourceStone extends BlockEnumMulti {

	public BlockResourceStone() {
		super(Material.ROCK, BlockEnums.EnumStoneType.class, true, true);
	}

	@Override
	public void dropBlockAsItemWithChance(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, float chance, int fortune) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ(); int meta = state.getBlock().getMetaFromState(state);

		if(meta == BlockEnums.EnumStoneType.ASBESTOS.ordinal()) {
			com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, ModBlocks.gas_asbestos);
		}

		super.dropBlockAsItemWithChance(world, new net.minecraft.util.math.BlockPos(x, y, z), world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)), chance, fortune);
	}


		@Override
	public void getDrops(net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> drops, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, int fortune) {
		int meta = state.getBlock().getMetaFromState(state);
		net.minecraft.world.World w = world instanceof net.minecraft.world.World ? (net.minecraft.world.World) world : null;
		java.util.ArrayList<net.minecraft.item.ItemStack> ret = getDrops(w, pos.getX(), pos.getY(), pos.getZ(), meta, fortune);
		if(ret != null) drops.addAll(ret);
	}

public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune) {

		if(meta == BlockEnums.EnumStoneType.MALACHITE.ordinal()) {
			ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
			ret.add(DictFrame.fromOne(ModItems.chunk_ore, EnumChunkType.MALACHITE, 3 + fortune + world.rand.nextInt(fortune + 2)));
			return ret;
		}

		net.minecraft.util.NonNullList<ItemStack> nativeDrops = net.minecraft.util.NonNullList.create();
		super.getDrops(nativeDrops, world, new net.minecraft.util.math.BlockPos(x, y, z), this.getStateFromMeta(meta), fortune);
		return new ArrayList<ItemStack>(nativeDrops);
	}
}
