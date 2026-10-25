package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.entity.mob.botprime.EntityBOTPrimeHead;
import com.hbm.items.ModItems;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class BlockBallsSpawner extends Block {

	public BlockBallsSpawner(Material mat) {
		super(mat);
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float fx, float fy, float fz) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int i = nativeFacing != null ? nativeFacing.getIndex() : 0;


		if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null && com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() == ModItems.mech_key) {
			com.hbm.util.NativeHeldItem.getMainHandNullable(player).shrink(1);

			if(!world.isRemote) {

				EntityBOTPrimeHead bot = new EntityBOTPrimeHead(world);
				bot.setPositionAndRotation(x + 0.5, 300, z + 0.5, 0, 0);
				bot.motionY = -1.0;
				bot.onInitialSpawn(bot.world.getDifficultyForLocation(new net.minecraft.util.math.BlockPos(bot)), (net.minecraft.entity.IEntityLivingData) null);
				world.spawnEntity(bot);

				{ net.minecraft.world.World nativeRegisteredWriteWorld = world; int nativeRegisteredWriteX = x; int nativeRegisteredWriteY = y; int nativeRegisteredWriteZ = z; net.minecraft.block.Block nativeRegisteredWriteBlock = ModBlocks.brick_jungle_cracked; int nativeRegisteredWriteMeta = 0; int nativeRegisteredWriteFlags = 3; if (nativeRegisteredWriteWorld == null) throw new NullPointerException(); if (nativeRegisteredWriteX >= -30000000 && nativeRegisteredWriteX < 30000000 && nativeRegisteredWriteZ >= -30000000 && nativeRegisteredWriteZ < 30000000 && nativeRegisteredWriteY >= 0 && nativeRegisteredWriteY < 256) com.hbm.world.WorldBlockState.write(nativeRegisteredWriteWorld, nativeRegisteredWriteX, nativeRegisteredWriteY, nativeRegisteredWriteZ, nativeRegisteredWriteBlock.getStateFromMeta(nativeRegisteredWriteMeta), nativeRegisteredWriteFlags); }
			}
		}

		return false;
	}

}
