package com.hbm.blocks.generic;

import java.util.ArrayList;
import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.extprop.HbmLivingProps.ContaminationEffect;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.items.ModItems;
import com.hbm.potion.HbmPotion;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockFallout extends Block {

	public BlockFallout withSoundType(net.minecraft.block.SoundType sound) {
		super.setSoundType(sound);
		return this;
	}


	private final net.minecraft.util.math.AxisAlignedBB shape;


	public BlockFallout(Material mat) {
		super(mat);
		this.shape = new net.minecraft.util.math.AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
		if(this==ModBlocks.salted_fallout)
		{
			this.setTickRandomly(true);
		}
	}

	public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	public boolean isFullCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	public Item getItemDropped(net.minecraft.block.state.IBlockState p_149650_1_, Random p_149650_2_, int p_149650_3_) {
		return ModItems.fallout;
	}

	public boolean canPlaceBlockAt(World world, net.minecraft.util.math.BlockPos nativePlacementPos) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
		net.minecraft.block.state.IBlockState nativeAirState = com.hbm.world.WorldBlockState.read(world, x, y - 1, z);
		Block block = nativeAirState.getBlock();

		if (block == Blocks.ICE || block == Blocks.PACKED_ICE) return false;
		if (block.isLeaves(nativeAirState, world, new net.minecraft.util.math.BlockPos(x, y - 1, z)) && !block.isAir(nativeAirState, world, new net.minecraft.util.math.BlockPos(x, y - 1, z))) return true;
		if (block == this && (com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y - 1, z) ) & 7) == 7) return true;

		return block.isOpaqueCube(nativeAirState) && block.getDefaultState().getMaterial().blocksMovement();
	}


		@Override
	public void onEntityWalk(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.entity.Entity entityIn) {
		onEntityWalking(worldIn, pos.getX(), pos.getY(), pos.getZ(), entityIn);
	}

public void onEntityWalking(World world, int x, int y, int z, Entity entity) {

		if(!world.isRemote && entity instanceof EntityLivingBase) {
			if(entity instanceof EntityPlayer && ((EntityPlayer)entity).capabilities.isCreativeMode) return;
			PotionEffect effect = new PotionEffect(HbmPotion.radiation, 10 * 60 * 20, 0);
			effect.setCurativeItems(new ArrayList());
			((EntityLivingBase) entity).addPotionEffect(effect);
		}
	}

	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {

		if(!world.isRemote) {
			HbmLivingProps.addCont(player, new ContaminationEffect(1F, 200, false));
		}
	}

	public void onNeighborBlockChange(World world, int x, int y, int z, Block b) {
		this.func_150155_m(world, x, y, z);
	}

	private boolean func_150155_m(World world, int x, int y, int z) {
		if(!this.canPlaceBlockAt(world, new net.minecraft.util.math.BlockPos(x, y, z))) {
			com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		super.onBlockAdded(world, pos, state);
	}

	public boolean isReplaceable(IBlockAccess world, net.minecraft.util.math.BlockPos pos) {
		return true;
	}

	@Override
	public void updateTick(World world, net.minecraft.util.math.BlockPos nativeTickPos, net.minecraft.block.state.IBlockState nativeTickState, Random rand) {
		int x = nativeTickPos.getX(), y = nativeTickPos.getY(), z = nativeTickPos.getZ();
		if(this==ModBlocks.salted_fallout)
		{
			ChunkRadiationManager.proxy.incrementRad(world, x, y, z, 50);
		}
	}


	public net.minecraft.util.math.AxisAlignedBB getBoundingBox(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos) {
		return shape;
	}
}
