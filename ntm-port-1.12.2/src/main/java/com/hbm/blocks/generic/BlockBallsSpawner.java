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

	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int i, float fx, float fy, float fz) {
		
		if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null && com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() == ModItems.mech_key) {
			com.hbm.util.NativeHeldItem.getMainHandNullable(player).stackSize--;
			
			if(!world.isRemote) {
				
				EntityBOTPrimeHead bot = new EntityBOTPrimeHead(world);
				bot.setPositionAndRotation(x + 0.5, 300, z + 0.5, 0, 0);
				bot.motionY = -1.0;
				bot.onSpawnWithEgg(null);
				world.spawnEntityInWorld(bot);
				
				{ net.minecraft.world.World nativeRegisteredWriteWorld = world; int nativeRegisteredWriteX = x; int nativeRegisteredWriteY = y; int nativeRegisteredWriteZ = z; net.minecraft.block.Block nativeRegisteredWriteBlock = ModBlocks.brick_jungle_cracked; int nativeRegisteredWriteMeta = 0; int nativeRegisteredWriteFlags = 3; if (nativeRegisteredWriteWorld == null) throw new NullPointerException(); if (nativeRegisteredWriteX >= -30000000 && nativeRegisteredWriteX < 30000000 && nativeRegisteredWriteZ >= -30000000 && nativeRegisteredWriteZ < 30000000 && nativeRegisteredWriteY >= 0 && nativeRegisteredWriteY < 256) com.hbm.world.WorldBlockState.write(nativeRegisteredWriteWorld, nativeRegisteredWriteX, nativeRegisteredWriteY, nativeRegisteredWriteZ, nativeRegisteredWriteBlock.getStateFromMeta(nativeRegisteredWriteMeta), nativeRegisteredWriteFlags); }
			}
		}
		
		return false;
	}

}
