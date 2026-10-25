package com.hbm.world.dungeon;

import java.util.Arrays;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockBobble.BobbleType;
import com.hbm.blocks.generic.BlockBobble.TileEntityBobble;
import com.hbm.config.GeneralConfig;
import com.hbm.inventory.RecipesCommon.MetaBlock;
import com.hbm.main.MainRegistry;
import com.hbm.world.generator.DungeonToolbox;

import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.util.math.BlockPos;

public class ArcticVault {

	public void trySpawn(World world, int x, int y, int z) {

		y--;

		Biome biome = world.getBiomeProvider().getBiome(new BlockPos(x, 0, z));

		if(biome.getTemperature(new BlockPos(x, y, z)) < 0.2 && (world.isValid(new BlockPos(x, y, z)) ? world.getBlockState(new BlockPos(x, y, z)).getBlock() : Blocks.AIR).getDefaultState().getMaterial() == Material.ROCK) {
			build(world, x, y, z);
		}

	}

	private void build(World world, int x, int y, int z) {

		List<MetaBlock> brick = Arrays.asList(new MetaBlock[] {new MetaBlock(Blocks.STONEBRICK), new MetaBlock(Blocks.STONEBRICK, 2)});
		List<MetaBlock> web = Arrays.asList(new MetaBlock[] {new MetaBlock(Blocks.AIR), new MetaBlock(Blocks.AIR), new MetaBlock(Blocks.WEB)});
		List<MetaBlock> crates = Arrays.asList(new MetaBlock[] {new MetaBlock(ModBlocks.crate), new MetaBlock(ModBlocks.crate_metal), new MetaBlock(ModBlocks.crate_ammo), new MetaBlock(ModBlocks.crate_can), new MetaBlock(ModBlocks.crate_jungle)});

		DungeonToolbox.generateBox(world, x - 5, y, z - 5, 11, 1, 11, brick);
		DungeonToolbox.generateBox(world, x - 5, y + 6, z - 5, 11, 1, 11, brick);
		DungeonToolbox.generateBox(world, x - 5, y + 1, z - 5, 11, 5, 1, brick);
		DungeonToolbox.generateBox(world, x - 5, y + 1, z + 5, 11, 5, 1, brick);
		DungeonToolbox.generateBox(world, x - 5, y + 1, z - 5, 1, 5, 11, brick);
		DungeonToolbox.generateBox(world, x + 5, y + 1, z - 5, 1, 5, 11, brick);
		DungeonToolbox.generateBox(world, x - 4, y + 1, z - 4, 9, 3, 9, Blocks.AIR);
		DungeonToolbox.generateBox(world, x - 4, y + 1, z - 4, 9, 1, 9, new MetaBlock(Blocks.SNOW_LAYER));
		DungeonToolbox.generateBox(world, x - 2, y + 1, z - 2, 5, 2, 1, new MetaBlock(ModBlocks.tape_recorder, 3));
		DungeonToolbox.generateBox(world, x - 2, y + 3, z - 2, 5, 1, 1, new MetaBlock(Blocks.SNOW_LAYER));
		DungeonToolbox.generateBox(world, x - 2, y + 1, z + 2, 5, 2, 1, new MetaBlock(ModBlocks.tape_recorder, 2));
		DungeonToolbox.generateBox(world, x - 2, y + 3, z + 2, 5, 1, 1, new MetaBlock(Blocks.SNOW_LAYER));
		DungeonToolbox.generateBox(world, x - 4, y + 4, z - 4, 9, 2, 9, web);

		for(int i = 0; i < 15; i++) {
			int ix = x - 4 + world.rand.nextInt(10);
			int iz = z - 4 + world.rand.nextInt(10);

			if(com.hbm.world.WorldBlockState.read(world, ix, y + 1, iz).getBlock() == Blocks.SNOW_LAYER) {

				if(i == 0) {
					com.hbm.world.WorldBlockState.writeLegacyBlock(world, ix, y + 1, iz, ModBlocks.bobblehead, world.rand.nextInt(16), 3);
					TileEntityBobble bobble = (TileEntityBobble) world.getTileEntity(new net.minecraft.util.math.BlockPos(ix, y + 1, iz));

					if(bobble != null) {
						bobble.type = BobbleType.values()[world.rand.nextInt(BobbleType.values().length - 1) + 1];
						bobble.markDirty();
					}

				} else {
					MetaBlock b = DungeonToolbox.getRandom(crates, world.rand);
					com.hbm.world.WorldBlockState.writeLegacyBlock(world, ix, y + 1, iz, b.block, b.meta, 2);
					{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = ix; int nativePlacementY = y + 2; int nativePlacementZ = iz; net.minecraft.block.Block nativePlacementBlock = Blocks.SNOW_LAYER; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
				}
			}
		}

		int iy = world.getHeight(x, z);

		if(com.hbm.world.WorldBlockState.read(world, x, iy - 1, z).getBlock().canPlaceTorchOnTop(com.hbm.world.WorldBlockState.read(world, x, iy - 1, z), world, new net.minecraft.util.math.BlockPos(x, iy - 1, z))) {
			com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, iy, z, ModBlocks.tape_recorder);
		}

		if(GeneralConfig.enableDebugMode)
			MainRegistry.logger.info("[Debug] Successfully spawned arctic code vault at " + x + " " + y + " " + z);
	}
}
