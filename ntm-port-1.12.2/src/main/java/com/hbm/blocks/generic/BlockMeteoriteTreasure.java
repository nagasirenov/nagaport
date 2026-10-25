package com.hbm.blocks.generic;

import java.util.ArrayList;

import com.hbm.itempool.ItemPool;
import com.hbm.itempool.ItemPoolsSingle;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import com.hbm.lib.WeightedRandomChestContent;
import net.minecraft.world.World;

public class BlockMeteoriteTreasure extends Block {

	public BlockMeteoriteTreasure(Material mat) {
		super(mat);
	}


		@Override
	public void getDrops(net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> drops, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, int fortune) {
		int meta = state.getBlock().getMetaFromState(state);
		net.minecraft.world.World w = world instanceof net.minecraft.world.World ? (net.minecraft.world.World) world : null;
		java.util.ArrayList<net.minecraft.item.ItemStack> ret = getDrops(w, pos.getX(), pos.getY(), pos.getZ(), meta, fortune);
		if(ret != null) drops.addAll(ret);
	}

public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();

		int count = 1 + world.rand.nextInt(3);
		WeightedRandomChestContent[] pool = ItemPool.getPool(ItemPoolsSingle.POOL_METEORITE_TREASURE);

		for(int i = 0; i < count; i++) {
			ret.add(ItemPool.getStack(pool, world.rand));
		}

		return ret;
	}
}
