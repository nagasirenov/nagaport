package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.BlockPillar;
import com.hbm.items.ModItems;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class BlockForgottenLock extends BlockPillar {

	public BlockForgottenLock(Material mat, String top) {
		super(mat, top);
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;


		// placeholder
		if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null) {
			boolean cracked = com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() == ModItems.key_red_cracked;
			if((com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() == ModItems.key_red || cracked) && side != 0 && side != 1) {
				if(cracked) com.hbm.util.NativeHeldItem.getMainHandNullable(player).shrink(1);
				if(world.isRemote) return true;
				int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );
				ForgeDirection dir = ForgeDirection.getOrientation(side);

				generate(world, x, y, z, meta, dir);

				world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:block.lockOpen"), net.minecraft.util.SoundCategory.PLAYERS, 1.0F, 1.0F);
				return true;
			}
		}

		return false;
	}

	public static void generate(World world, int x, int y, int z, int meta, ForgeDirection dir) {

		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		int len = 15;
		for(int w = -2; w <= 2; w++) for(int h = -2; h <= 2; h++) for(int d = 0; d < len; d++) {
			Block b = (w == -2 || w == 2 || h == -2 || h == 2 || d == len - 1) ? ModBlocks.brick_forgotten : Blocks.AIR;
			com.hbm.world.WorldBlockState.writeLegacyBlock(world, x - dir.offsetX * d + rot.offsetX * w, y + h, z - dir.offsetZ * d + rot.offsetZ * w, b);
		}
	}
}
