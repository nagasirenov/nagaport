package com.hbm.world.feature;

import java.util.Random;

import com.hbm.blocks.ModBlocks;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;

public class SchistStratum {

	NoiseGeneratorPerlin noise;

	@SubscribeEvent
	public void onDecorate(DecorateBiomeEvent.Pre event) {
		
		if(this.noise == null) {
			this.noise = new NoiseGeneratorPerlin(new Random(event.getWorld().getSeed()), 4);
		}

		World world = event.getWorld();
		
		if(world.provider == null || world.provider.getDimension() != 0)
			return;
		
		int cX = event.chunkX;
		int cZ = event.chunkZ;
		
		double scale = 0.01D;
		int threshold = 5;
		
		for(int x = cX + 8; x < cX + 24; x++) {
			for(int z = cZ + 8; z < cZ + 24; z++) {
				
				double n = noise.func_151601_a(x * scale, z * scale);
				
				if(n > threshold) {
					int range = (int)((n - threshold) * 3);
					
					if(range > 4)
						range = 8 - range;
					
					if(range < 0)
						continue;
					
					for(int y = 30 - range; y <= 30 + range; y++) {
						
						net.minecraft.block.state.IBlockState nativeCubeState_target = com.hbm.world.WorldBlockState.read(world, x, y, z);
						Block target = nativeCubeState_target.getBlock();
						
						if(target.isNormalCube(nativeCubeState_target) && target.getDefaultState().getMaterial() == Material.ROCK && target.isReplaceableOreGen(world, x, y, z, Blocks.STONE)) {
							{ net.minecraft.world.World nativeRegisteredWriteWorld = world; int nativeRegisteredWriteX = x; int nativeRegisteredWriteY = y; int nativeRegisteredWriteZ = z; net.minecraft.block.Block nativeRegisteredWriteBlock = ModBlocks.stone_gneiss; int nativeRegisteredWriteMeta = 0; int nativeRegisteredWriteFlags = 2; if (nativeRegisteredWriteWorld == null) throw new NullPointerException(); if (nativeRegisteredWriteX >= -30000000 && nativeRegisteredWriteX < 30000000 && nativeRegisteredWriteZ >= -30000000 && nativeRegisteredWriteZ < 30000000 && nativeRegisteredWriteY >= 0 && nativeRegisteredWriteY < 256) com.hbm.world.WorldBlockState.write(nativeRegisteredWriteWorld, nativeRegisteredWriteX, nativeRegisteredWriteY, nativeRegisteredWriteZ, nativeRegisteredWriteBlock.getStateFromMeta(nativeRegisteredWriteMeta), nativeRegisteredWriteFlags); }
						}
					}
				}
			}
		}
	}
}
