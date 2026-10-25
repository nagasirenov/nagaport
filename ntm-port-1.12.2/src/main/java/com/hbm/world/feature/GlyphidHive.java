package com.hbm.world.feature;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.util.LootGenerator;

import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.world.World;

public class GlyphidHive {

	public static final int[][][] schematicSmall = new int[][][] {
		{
			{0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,1,1,1,0,0,0,0},
			{0,0,0,0,1,1,1,0,0,0,0},
			{0,0,0,0,1,1,1,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0},
		},
		{
			{0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,1,1,1,0,0,0,0},
			{0,0,0,1,1,1,1,1,0,0,0},
			{0,0,1,1,1,1,1,1,1,0,0},
			{0,0,1,1,1,1,1,1,1,0,0},
			{0,0,1,1,1,1,1,1,1,0,0},
			{0,0,0,1,1,1,1,1,0,0,0},
			{0,0,0,0,1,1,1,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0},
		},
		{
			{0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,1,1,1,0,0,0,0},
			{0,0,1,1,1,1,1,1,1,0,0},
			{0,0,1,1,1,1,1,1,1,0,0},
			{0,1,1,1,3,3,3,1,1,1,0},
			{0,1,1,1,3,3,3,1,1,1,0},
			{0,1,1,1,3,3,3,1,1,1,0},
			{0,0,1,1,1,1,1,1,1,0,0},
			{0,0,1,1,1,1,1,1,1,0,0},
			{0,0,0,0,1,1,1,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0},
		},
		{
			{0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,1,1,1,0,0,0,0},
			{0,0,1,1,1,1,1,1,1,0,0},
			{0,0,1,1,2,2,2,1,1,0,0},
			{0,1,1,2,2,2,2,2,1,1,0},
			{0,1,1,2,2,2,2,2,1,1,0},
			{0,1,1,2,2,2,2,2,1,1,0},
			{0,0,1,1,2,2,2,1,1,0,0},
			{0,0,1,1,1,1,1,1,1,0,0},
			{0,0,0,0,1,1,1,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0},
		},
		{
			{0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,1,1,1,0,0,0,0},
			{0,0,1,1,1,1,1,1,1,0,0},
			{0,0,1,1,1,1,1,1,1,0,0},
			{0,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,0},
			{0,1,1,1,1,1,1,1,1,1,0},
			{0,0,1,1,1,1,1,1,1,0,0},
			{0,0,1,1,1,1,1,1,1,0,0},
			{0,0,0,0,1,1,1,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0},
		}
	};
	public static void generateSmall(World world, int x, int y, int z, Random rand, boolean infected, boolean loot) {
		int overrideMeta = infected ? 1 : 0;

		for(int i = 0; i < 11; i++) {
			for(int j = 0; j < 5; j++) {
				for(int k = 0; k < 11; k++) {

					int block = schematicSmall[4 - j][i][k];
					int iX = x + i - 5;
					int iY = y + j - 2;
					int iZ = z + k - 5;

					switch(block) {
					case 1: com.hbm.world.WorldBlockState.writeLegacyBlock(world, iX, iY, iZ, ModBlocks.glyphid_base, overrideMeta, 2); break;
					case 2: com.hbm.world.WorldBlockState.writeLegacyBlock(world, iX, iY, iZ, rand.nextInt(3) == 0 ? ModBlocks.glyphid_spawner : ModBlocks.glyphid_base, overrideMeta, 2); break;
					case 3:
						int r = rand.nextInt(3);
						if(r == 0) {
							{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = iX; int nativePlacementY = iY; int nativePlacementZ = iZ; net.minecraft.block.Block nativePlacementBlock = Blocks.SKULL; int nativePlacementMeta = 1; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
							TileEntitySkull skull = (TileEntitySkull) world.getTileEntity(new net.minecraft.util.math.BlockPos(iX, iY, iZ));
							if(skull != null) skull.setSkullRotation(rand.nextInt(16));
						} else if(r == 1) {
							com.hbm.world.WorldBlockState.writeLegacyBlock(world, iX, iY, z + k - 5, ModBlocks.deco_loot, 0, 2);
							LootGenerator.lootBones(world, iX, iY, iZ);
						} else if(r == 2) {
							if(loot) {
								com.hbm.world.WorldBlockState.writeLegacyBlock(world, iX, iY, iZ, ModBlocks.deco_loot, 0, 2);
								LootGenerator.lootGlyphidHive(world, iX, iY, iZ);
							} else {
								com.hbm.world.WorldBlockState.writeLegacyBlock(world, iX, iY, iZ, ModBlocks.glyphid_base, overrideMeta, 2);
							}
						}
						break;
					}
				}
			}
		}
	}
}
