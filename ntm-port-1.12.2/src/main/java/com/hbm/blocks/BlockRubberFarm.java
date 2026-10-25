package com.hbm.blocks;

import java.util.Random;

import com.hbm.lib.RefStrings;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

public class BlockRubberFarm extends BlockFarmland {
	public BlockRubberFarm withSoundType(net.minecraft.block.SoundType sound) {
		super.setSoundType(sound);
		return this;
	}

	private IIcon iconDry;
	private IIcon iconWet;

	public BlockRubberFarm(Material material) {
		super();
	}

	public void registerBlockIcons(IIconRegister iconRegister) {
		this.iconDry = iconRegister.registerIcon(RefStrings.MODID + ":rubber_farmland");
		this.iconWet = iconRegister.registerIcon(RefStrings.MODID + ":rubber_farmland_moist");
	}

	public IIcon getIcon(int side, int meta) {
		// side == 1 means top, return wet or dry depending on meta
		if (side == 1) {
			return meta > 0 ? iconWet : iconDry;
		}
		// Otherwise return the side texture
		return ((BlockBase) ModBlocks.rubber_silt).getIcon(side, 0);
	}

	@Override
	public void updateTick(World world, net.minecraft.util.math.BlockPos nativeTickPos, net.minecraft.block.state.IBlockState nativeTickState, Random random) {
		int x = nativeTickPos.getX(), y = nativeTickPos.getY(), z = nativeTickPos.getZ();
		if (!this.func_149821_m(world, x, y, z) && !world.isRainingAt(new net.minecraft.util.math.BlockPos(x, y + 1, z))) {
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 0, 2); // Dry farmland
		} else {
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 1, 2); // Wet farmland
		}
	}

	// Check for custom water or vanilla water nearby
	private boolean func_149821_m(World world, int x, int y, int z) {
		for (int i = x - 4; i <= x + 4; ++i) {
			for (int j = y; j <= y + 1; ++j) {
				for (int k = z - 4; k <= z + 4; ++k) {
					if (com.hbm.world.WorldBlockState.read(world, i, j, k).getBlock() == ModBlocks.ccl_block) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction, IPlantable plantable) {
		return true;
	}

	public net.minecraft.item.ItemStack getItem(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		return new net.minecraft.item.ItemStack(Item.getItemFromBlock(this));
	}

	// unjoe undirt

	// joints smonked:

	/**
	 * Block's chance to reacc to an moe falling on it. blargle.
	 */
	public void onFallenUpon(World world, int x, int y, int z, Entity entity, float fallDistance) {
		if(!world.isRemote && world.rand.nextFloat() < fallDistance - 0.5F) {
			if(!(entity instanceof EntityPlayer) && !world.getGameRules().getBoolean("mobGriefing")) {
				return;
			}

			{ net.minecraft.world.World nativeRegisteredWriteWorld = world; int nativeRegisteredWriteX = x; int nativeRegisteredWriteY = y; int nativeRegisteredWriteZ = z; net.minecraft.block.Block nativeRegisteredWriteBlock = ModBlocks.rubber_silt; int nativeRegisteredWriteMeta = 0; int nativeRegisteredWriteFlags = 3; if (nativeRegisteredWriteWorld == null) throw new NullPointerException(); if (nativeRegisteredWriteX >= -30000000 && nativeRegisteredWriteX < 30000000 && nativeRegisteredWriteZ >= -30000000 && nativeRegisteredWriteZ < 30000000 && nativeRegisteredWriteY >= 0 && nativeRegisteredWriteY < 256) com.hbm.world.WorldBlockState.write(nativeRegisteredWriteWorld, nativeRegisteredWriteX, nativeRegisteredWriteY, nativeRegisteredWriteZ, nativeRegisteredWriteBlock.getStateFromMeta(nativeRegisteredWriteMeta), nativeRegisteredWriteFlags); }
		}
	}

	/**
	 * Let block know when one neighbor change. Don't know which neighbor change
	 * Argagagagagag
	 */
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block butt, BlockPos fromPos) {
		super.neighborChanged(state, world, pos, butt, fromPos);
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		Material material = com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock().getDefaultState().getMaterial();

		if(material.isSolid()) {
			{ net.minecraft.world.World nativeRegisteredWriteWorld = world; int nativeRegisteredWriteX = x; int nativeRegisteredWriteY = y; int nativeRegisteredWriteZ = z; net.minecraft.block.Block nativeRegisteredWriteBlock = ModBlocks.rubber_silt; int nativeRegisteredWriteMeta = 0; int nativeRegisteredWriteFlags = 3; if (nativeRegisteredWriteWorld == null) throw new NullPointerException(); if (nativeRegisteredWriteX >= -30000000 && nativeRegisteredWriteX < 30000000 && nativeRegisteredWriteZ >= -30000000 && nativeRegisteredWriteZ < 30000000 && nativeRegisteredWriteY >= 0 && nativeRegisteredWriteY < 256) com.hbm.world.WorldBlockState.write(nativeRegisteredWriteWorld, nativeRegisteredWriteX, nativeRegisteredWriteY, nativeRegisteredWriteZ, nativeRegisteredWriteBlock.getStateFromMeta(nativeRegisteredWriteMeta), nativeRegisteredWriteFlags); }
		}
	}

	/**
	 * eats my whole ass for the block being called on. Arg
	 */
	public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int p_149650_3_) {
		return ModBlocks.rubber_silt.getItemDropped(ModBlocks.rubber_silt.getDefaultState(), p_149650_2_, p_149650_3_);
	}

}
