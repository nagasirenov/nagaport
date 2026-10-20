//Schematic to java Structure by jajo_11 | inspired by "MITHION'S .SCHEMATIC TO JAVA CONVERTINGTOOL"

package com.hbm.world.dungeon;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.itempool.ItemPool;
import com.hbm.itempool.ItemPoolsLegacy;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityChest;
import com.hbm.lib.WeightedRandomChestContent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class Spaceship extends WorldGenerator {

	Block Block1 = ModBlocks.deco_tungsten;
	Block Block2 = ModBlocks.deco_red_copper;
	Block Block3 = ModBlocks.deco_steel;
	Block Block4 = ModBlocks.fusion_heater;
	Block Block5 = ModBlocks.block_meteor;
	Block Block6 = ModBlocks.pwr_fuel;
	Block Block7 = ModBlocks.cable_switch;
	Block Block8 = ModBlocks.fusion_hatch;
	Block Block9 = ModBlocks.reinforced_light;
	Block Block10 = ModBlocks.reinforced_glass;

	protected Block[] GetValidSpawnBlocks() {
		return new Block[] {
			Blocks.GRASS,
			Blocks.DIRT,
			Blocks.STONE,
			Blocks.SAND,
			Blocks.SANDSTONE,
			ModBlocks.moon_turf,
			ModBlocks.moon_rock,
			ModBlocks.duna_sands,
			ModBlocks.duna_rock,
			ModBlocks.duna_cobble,
			ModBlocks.ike_regolith,
			ModBlocks.ike_stone,
			ModBlocks.dres_rock,
			ModBlocks.laythe_silt,
			ModBlocks.eve_silt,
			ModBlocks.eve_rock,
			ModBlocks.moho_regolith,
			ModBlocks.moho_stone,
		};
	}

	public boolean LocationIsValidSpawn(World world, int x, int y, int z) {

		Block checkBlock = (world.isValid(new BlockPos(x, y - 1, z)) ? world.getBlockState(new BlockPos(x, y - 1, z)).getBlock() : Blocks.AIR);
		{ BlockPos dungeonPos = new BlockPos(x, y, z); if (world.isValid(dungeonPos)) world.getBlockState(dungeonPos).getBlock(); }
		Block blockBelow = (world.isValid(new BlockPos(x, y - 2, z)) ? world.getBlockState(new BlockPos(x, y - 2, z)).getBlock() : Blocks.AIR);

		for (Block i : GetValidSpawnBlocks()) {
			if (checkBlock == i)
			{
				return true;
			}
			else if (checkBlock == Blocks.SNOW_LAYER && blockBelow == i)
			{
				return true;
			}
			else if (checkBlock.getDefaultState().getMaterial() == Material.PLANTS && blockBelow == i)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		int i = rand.nextInt(1);

		if(i == 0) {
		    generate_r0(world, rand, x, y, z);
		}

       return true;

	}

	public boolean generate_r0(World world, Random rand, int x, int y, int z) {
		if(!LocationIsValidSpawn(world, x, y, z) || !LocationIsValidSpawn(world, x + 12, y, z) || !LocationIsValidSpawn(world, x, y, z + 23) || !LocationIsValidSpawn(world, x + 12, y, z + 23))
			return false;

		y += 1;

		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -3, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -3, z + 11); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -3, z + 11); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -3, z + 12); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -3, z + 12); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -3, z + 13); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -3, z + 13); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -3, z + 14); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -3, z + 14); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -3, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -3, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -3, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -3, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -3, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -3, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -3, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -3, z + 18, Block1);
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -3, z + 18); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -3, z + 18); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -3, z + 18, Block1);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -3, z + 19, Block2);
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -3, z + 19); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -3, z + 19); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -3, z + 19); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -3, z + 19); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -3, z + 19); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -3, z + 19); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -3, z + 19, Block2);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -3, z + 20, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -3, z + 20); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -3, z + 20); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -3, z + 20); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -3, z + 20); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -3, z + 20); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -3, z + 20); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -3, z + 20); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -3, z + 20); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -3, z + 20, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -3, z + 21, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -3, z + 21); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -3, z + 21); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -3, z + 21); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -3, z + 21); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -3, z + 21); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -3, z + 21); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -3, z + 21); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -3, z + 21); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -3, z + 21, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -3, z + 22, Block2);
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -3, z + 22); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -3, z + 22); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -3, z + 22); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -3, z + 22); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -3, z + 22); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -3, z + 22); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -3, z + 22); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -3, z + 22); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -3, z + 22, Block2);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -3, z + 23, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -3, z + 23); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -3, z + 23); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -3, z + 23); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 6, y + -3, z + 23, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 7, y + -3, z + 23, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -3, z + 23); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -3, z + 23); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -3, z + 23); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -3, z + 23, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -3, z + 24, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -3, z + 24); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -3, z + 24); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 5, y + -3, z + 24, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 6, y + -3, z + 24, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 7, y + -3, z + 24, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 8, y + -3, z + 24, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -3, z + 24); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -3, z + 24); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -3, z + 24, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -3, z + 25, Block2);
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -3, z + 25); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -3, z + 25); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 5, y + -3, z + 25, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 6, y + -3, z + 25, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 7, y + -3, z + 25, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 8, y + -3, z + 25, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -3, z + 25); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -3, z + 25); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -3, z + 25, Block2);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -3, z + 26, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -3, z + 26); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -3, z + 26); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 5, y + -3, z + 26, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 6, y + -3, z + 26, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 7, y + -3, z + 26, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 8, y + -3, z + 26, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -3, z + 26); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -3, z + 26); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -3, z + 26, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -3, z + 27, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -3, z + 27); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -3, z + 27); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 5, y + -3, z + 27, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 6, y + -3, z + 27, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 7, y + -3, z + 27, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 8, y + -3, z + 27, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -3, z + 27); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -3, z + 27); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -3, z + 27, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -3, z + 28, Block2);
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -3, z + 28); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -3, z + 28); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 5, y + -3, z + 28, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 6, y + -3, z + 28, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 7, y + -3, z + 28, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 8, y + -3, z + 28, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -3, z + 28); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -3, z + 28); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -3, z + 28, Block2);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -3, z + 29, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -3, z + 29); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -3, z + 29); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 5, y + -3, z + 29, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 6, y + -3, z + 29, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 7, y + -3, z + 29, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 8, y + -3, z + 29, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -3, z + 29); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -3, z + 29); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -3, z + 29, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -3, z + 30, ModBlocks.red_wire_coated);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 3, y + -3, z + 30, ModBlocks.red_cable);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 4, y + -3, z + 30, ModBlocks.red_wire_coated);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 5, y + -3, z + 30, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 6, y + -3, z + 30, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 7, y + -3, z + 30, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 8, y + -3, z + 30, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 9, y + -3, z + 30, ModBlocks.red_wire_coated);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 10, y + -3, z + 30, ModBlocks.red_cable);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -3, z + 30, ModBlocks.red_wire_coated);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -3, z + 31, Block2);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 5, y + -3, z + 31, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 6, y + -3, z + 31, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 7, y + -3, z + 31, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 8, y + -3, z + 31, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -3, z + 31, Block2);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -3, z + 32, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 5, y + -3, z + 32, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 6, y + -3, z + 32, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 7, y + -3, z + 32, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 8, y + -3, z + 32, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -3, z + 32, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -3, z + 33, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 5, y + -3, z + 33, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 6, y + -3, z + 33, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 7, y + -3, z + 33, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 8, y + -3, z + 33, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -3, z + 33, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -3, z + 34, Block2);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 5, y + -3, z + 34, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 6, y + -3, z + 34, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 7, y + -3, z + 34, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 8, y + -3, z + 34, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -3, z + 34, Block2);
		world.setBlock(x + 2, y + -3, z + 35, Block4, 0, 3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 5, y + -3, z + 35, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 6, y + -3, z + 35, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 7, y + -3, z + 35, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 8, y + -3, z + 35, Block3);
		world.setBlock(x + 11, y + -3, z + 35, Block4, 0, 3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 5, y + -3, z + 36, ModBlocks.red_wire_coated);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 6, y + -3, z + 36, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 7, y + -3, z + 36, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 8, y + -3, z + 36, ModBlocks.red_wire_coated);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 5, y + -3, z + 37, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 6, y + -3, z + 37, Block1);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 7, y + -3, z + 37, Block1);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 8, y + -3, z + 37, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -3, z + 38); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 5, y + -3, z + 38, Block1);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 6, y + -3, z + 38, Block1);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 7, y + -3, z + 38, Block1);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 8, y + -3, z + 38, Block1);
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -3, z + 39); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 5, y + -3, z + 39, Block1);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 6, y + -3, z + 39, Block1);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 7, y + -3, z + 39, Block1);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 8, y + -3, z + 39, Block1);
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -3, z + 39); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -3, z + 40); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -3, z + 40); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 6, y + -3, z + 40, Block1);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 7, y + -3, z + 40, Block1);
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -3, z + 40); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -3, z + 41); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 6, y + -3, z + 41, Block1);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 7, y + -3, z + 41, Block1);
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -3, z + 42); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -2, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -2, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -2, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -2, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -2, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + -2, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -2, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -2, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -2, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + -2, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -2, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -2, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -2, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -2, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + -2, z + 11); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -2, z + 11); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -2, z + 11); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -2, z + 11); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -2, z + 11); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 12, y + -2, z + 11); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + -2, z + 12); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -2, z + 12); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -2, z + 12); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -2, z + 12); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -2, z + 12); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -2, z + 12); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 12, y + -2, z + 12); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + -2, z + 13); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -2, z + 13); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -2, z + 13); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -2, z + 13); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -2, z + 13); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -2, z + 13); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -2, z + 13); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 12, y + -2, z + 13); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + -2, z + 14); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -2, z + 14); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -2, z + 14); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -2, z + 14); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -2, z + 14); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -2, z + 14); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -2, z + 14); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -2, z + 14); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 12, y + -2, z + 14); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + -2, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -2, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -2, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -2, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -2, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -2, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -2, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -2, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -2, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -2, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 12, y + -2, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + -2, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -2, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -2, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -2, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -2, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -2, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -2, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -2, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -2, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -2, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -2, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 12, y + -2, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + -2, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -2, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -2, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -2, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -2, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -2, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -2, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -2, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -2, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -2, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -2, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 12, y + -2, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 1, y + -2, z + 18, Block1);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -2, z + 18); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 3, y + -2, z + 18, Block1);
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -2, z + 18); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -2, z + 18); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -2, z + 18); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -2, z + 18); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -2, z + 18); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -2, z + 18); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 10, y + -2, z + 18, Block1);
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -2, z + 18); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 12, y + -2, z + 18, Block1);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 1, y + -2, z + 19, Block2);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -2, z + 19); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 3, y + -2, z + 19, Block2);
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -2, z + 19); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -2, z + 19); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -2, z + 19); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -2, z + 19); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -2, z + 19); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -2, z + 19); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 10, y + -2, z + 19, Block2);
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -2, z + 19); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 12, y + -2, z + 19, Block2);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 1, y + -2, z + 20, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -2, z + 20); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 3, y + -2, z + 20, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -2, z + 20); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -2, z + 20); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -2, z + 20); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -2, z + 20); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -2, z + 20); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -2, z + 20); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 10, y + -2, z + 20, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -2, z + 20); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 12, y + -2, z + 20, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 1, y + -2, z + 21, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -2, z + 21); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 3, y + -2, z + 21, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -2, z + 21); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -2, z + 21); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -2, z + 21); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -2, z + 21); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -2, z + 21); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -2, z + 21); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 10, y + -2, z + 21, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -2, z + 21); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 12, y + -2, z + 21, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 1, y + -2, z + 22, Block2);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -2, z + 22); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 3, y + -2, z + 22, Block2);
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -2, z + 22); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -2, z + 22); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -2, z + 22); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -2, z + 22); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -2, z + 22); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -2, z + 22); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 10, y + -2, z + 22, Block2);
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -2, z + 22); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 12, y + -2, z + 22, Block2);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 1, y + -2, z + 23, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -2, z + 23); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 3, y + -2, z + 23, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -2, z + 23); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -2, z + 23); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -2, z + 23); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -2, z + 23); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -2, z + 23); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -2, z + 23); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 10, y + -2, z + 23, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -2, z + 23); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 12, y + -2, z + 23, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 1, y + -2, z + 24, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -2, z + 24); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 3, y + -2, z + 24, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 4, y + -2, z + 24, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 5, y + -2, z + 24, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -2, z + 24); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -2, z + 24); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 8, y + -2, z + 24, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 9, y + -2, z + 24, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 10, y + -2, z + 24, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -2, z + 24); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 12, y + -2, z + 24, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 1, y + -2, z + 25, Block2);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -2, z + 25); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 3, y + -2, z + 25, Block2);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 4, y + -2, z + 25, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -2, z + 25); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.CHEST.getStateFromMeta(5), 3); }
		world.setBlockMetadataWithNotify(x + 5, y + -2, z + 25, 5, 3);
		if((world.isValid(new BlockPos(x + 5, y + -2, z + 25)) ? world.getBlockState(new BlockPos(x + 5, y + -2, z + 25)).getBlock() : Blocks.AIR) == Blocks.CHEST)
		{
			WeightedRandomChestContent.generateChestContents(rand, ItemPool.getPool(ItemPoolsLegacy.POOL_SPACESHIP), (TileEntityChest)world.getTileEntity(new BlockPos(x + 5, y + -2, z + 25)), 12);
		}
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -2, z + 25); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -2, z + 25); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -2, z + 25); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.CHEST.getStateFromMeta(4), 3); }
		world.setBlockMetadataWithNotify(x + 8, y + -2, z + 25, 4, 3);
		if((world.isValid(new BlockPos(x + 8, y + -2, z + 25)) ? world.getBlockState(new BlockPos(x + 8, y + -2, z + 25)).getBlock() : Blocks.AIR) == Blocks.CHEST)
		{
			WeightedRandomChestContent.generateChestContents(rand, ItemPool.getPool(ItemPoolsLegacy.POOL_SPACESHIP), (TileEntityChest)world.getTileEntity(new BlockPos(x + 8, y + -2, z + 25)), 12);
		}
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 9, y + -2, z + 25, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 10, y + -2, z + 25, Block2);
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -2, z + 25); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 12, y + -2, z + 25, Block2);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 1, y + -2, z + 26, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -2, z + 26); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 3, y + -2, z + 26, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 4, y + -2, z + 26, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -2, z + 26); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.CHEST.getStateFromMeta(5), 3); }
		world.setBlockMetadataWithNotify(x + 5, y + -2, z + 26, 5, 3);
		if((world.isValid(new BlockPos(x + 5, y + -2, z + 26)) ? world.getBlockState(new BlockPos(x + 5, y + -2, z + 26)).getBlock() : Blocks.AIR) == Blocks.CHEST)
		{
			WeightedRandomChestContent.generateChestContents(rand, ItemPool.getPool(ItemPoolsLegacy.POOL_SPACESHIP), (TileEntityChest)world.getTileEntity(new BlockPos(x + 5, y + -2, z + 26)), 12);
		}
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -2, z + 26); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -2, z + 26); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -2, z + 26); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.CHEST.getStateFromMeta(4), 3); }
		world.setBlockMetadataWithNotify(x + 8, y + -2, z + 26, 4, 3);
		if((world.isValid(new BlockPos(x + 8, y + -2, z + 26)) ? world.getBlockState(new BlockPos(x + 8, y + -2, z + 26)).getBlock() : Blocks.AIR) == Blocks.CHEST)
		{
			WeightedRandomChestContent.generateChestContents(rand, ItemPool.getPool(ItemPoolsLegacy.POOL_SPACESHIP), (TileEntityChest)world.getTileEntity(new BlockPos(x + 8, y + -2, z + 26)), 12);
		}
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 9, y + -2, z + 26, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 10, y + -2, z + 26, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -2, z + 26); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 12, y + -2, z + 26, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 1, y + -2, z + 27, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -2, z + 27); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 3, y + -2, z + 27, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 4, y + -2, z + 27, Block3);
		world.setBlock(x + 5, y + -2, z + 27, ModBlocks.hadron_coil_alloy);
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -2, z + 27); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -2, z + 27); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		world.setBlock(x + 8, y + -2, z + 27, ModBlocks.hadron_coil_alloy);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 9, y + -2, z + 27, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 10, y + -2, z + 27, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -2, z + 27); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 12, y + -2, z + 27, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 1, y + -2, z + 28, Block2);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -2, z + 28); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 3, y + -2, z + 28, Block2);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 4, y + -2, z + 28, Block3);
		world.setBlock(x + 5, y + -2, z + 28, Block5, 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -2, z + 28); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -2, z + 28); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		world.setBlock(x + 8, y + -2, z + 28, Block5, 0, 3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 9, y + -2, z + 28, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 10, y + -2, z + 28, Block2);
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -2, z + 28); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 12, y + -2, z + 28, Block2);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 1, y + -2, z + 29, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -2, z + 29); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 3, y + -2, z + 29, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 4, y + -2, z + 29, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -2, z + 29); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -2, z + 29); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -2, z + 29); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -2, z + 29); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 9, y + -2, z + 29, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 10, y + -2, z + 29, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -2, z + 29); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 12, y + -2, z + 29, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 1, y + -2, z + 30, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -2, z + 30); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		world.setBlock(x + 3, y + -2, z + 30, Block6, 0, 3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 4, y + -2, z + 30, ModBlocks.red_cable);
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -2, z + 30); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -2, z + 30); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -2, z + 30); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -2, z + 30); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 9, y + -2, z + 30, ModBlocks.red_cable);
		world.setBlock(x + 10, y + -2, z + 30, Block6, 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -2, z + 30); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 12, y + -2, z + 30, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 1, y + -2, z + 31, Block2);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -2, z + 31); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 3, y + -2, z + 31, Block2);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 4, y + -2, z + 31, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -2, z + 31); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -2, z + 31); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -2, z + 31); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -2, z + 31); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 9, y + -2, z + 31, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 10, y + -2, z + 31, Block2);
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -2, z + 31); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 12, y + -2, z + 31, Block2);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 1, y + -2, z + 32, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -2, z + 32); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 3, y + -2, z + 32, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 4, y + -2, z + 32, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 5, y + -2, z + 32, Block2);
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -2, z + 32); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -2, z + 32); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 8, y + -2, z + 32, Block2);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 9, y + -2, z + 32, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 10, y + -2, z + 32, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -2, z + 32); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 12, y + -2, z + 32, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 1, y + -2, z + 33, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -2, z + 33); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 3, y + -2, z + 33, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 4, y + -2, z + 33, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 5, y + -2, z + 33, Block2);
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -2, z + 33); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -2, z + 33); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 8, y + -2, z + 33, Block2);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 9, y + -2, z + 33, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 10, y + -2, z + 33, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -2, z + 33); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 12, y + -2, z + 33, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 1, y + -2, z + 34, Block2);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -2, z + 34); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 3, y + -2, z + 34, Block2);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 4, y + -2, z + 34, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 5, y + -2, z + 34, Block2);
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -2, z + 34); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -2, z + 34); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 8, y + -2, z + 34, Block2);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 9, y + -2, z + 34, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 10, y + -2, z + 34, Block2);
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -2, z + 34); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 12, y + -2, z + 34, Block2);
		world.setBlock(x + 1, y + -2, z + 35, Block4, 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -2, z + 35); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		world.setBlock(x + 3, y + -2, z + 35, Block4, 0, 3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 4, y + -2, z + 35, Block3);
		world.setBlock(x + 5, y + -2, z + 35, ModBlocks.capacitor_copper, 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -2, z + 35); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -2, z + 35); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		world.setBlock(x + 8, y + -2, z + 35, ModBlocks.capacitor_copper, 0, 3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 9, y + -2, z + 35, Block3);
		world.setBlock(x + 10, y + -2, z + 35, Block4, 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -2, z + 35); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		world.setBlock(x + 12, y + -2, z + 35, Block4, 0, 3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -2, z + 36, ModBlocks.red_wire_coated);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 3, y + -2, z + 36, ModBlocks.red_cable);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 4, y + -2, z + 36, ModBlocks.red_wire_coated);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 5, y + -2, z + 36, ModBlocks.red_cable);
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -2, z + 36); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -2, z + 36); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 8, y + -2, z + 36, ModBlocks.red_cable);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 9, y + -2, z + 36, ModBlocks.red_wire_coated);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 10, y + -2, z + 36, ModBlocks.red_cable);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -2, z + 36, ModBlocks.red_wire_coated);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 4, y + -2, z + 37, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -2, z + 37); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -2, z + 37); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONE_STAIRS.getStateFromMeta(3), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -2, z + 37); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -2, z + 37); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 9, y + -2, z + 37, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 4, y + -2, z + 38, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -2, z + 38); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -2, z + 38); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -2, z + 38); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -2, z + 38); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.CHEST.getStateFromMeta(2), 3); }
		world.setBlockMetadataWithNotify(x + 8, y + -2, z + 38, 2, 3);
		if((world.isValid(new BlockPos(x + 8, y + -2, z + 38)) ? world.getBlockState(new BlockPos(x + 8, y + -2, z + 38)).getBlock() : Blocks.AIR) == Blocks.CHEST)
		{
			WeightedRandomChestContent.generateChestContents(rand, ItemPool.getPool(ItemPoolsLegacy.POOL_EXPENSIVE), (TileEntityChest)world.getTileEntity(new BlockPos(x + 8, y + -2, z + 38)), 12);
		}
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 9, y + -2, z + 38, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 4, y + -2, z + 39, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 5, y + -2, z + 39, Block3);
		world.setBlock(x + 6, y + -2, z + 39, Block7, 0, 3);
		world.setBlock(x + 7, y + -2, z + 39, Block8, 2, 3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 8, y + -2, z + 39, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 9, y + -2, z + 39, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -2, z + 40); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 5, y + -2, z + 40, Block1);
		world.setBlock(x + 6, y + -2, z + 40, Block4, 0, 3);
		world.setBlock(x + 7, y + -2, z + 40, Block4, 0, 3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 8, y + -2, z + 40, Block1);
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -2, z + 40); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -2, z + 41); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 5, y + -2, z + 41, Block1);
		world.setBlock(x + 6, y + -2, z + 41, Block4, 0, 3);
		world.setBlock(x + 7, y + -2, z + 41, Block4, 0, 3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 8, y + -2, z + 41, Block1);
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -2, z + 41); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -2, z + 42); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 6, y + -2, z + 42, Block1);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 7, y + -2, z + 42, Block1);
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -2, z + 42); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -2, z + 43); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -2, z + 43); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -2, z + 43); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -2, z + 43); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -2, z + 44); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -2, z + 44); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -1, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -1, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -1, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -1, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -1, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + -1, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -1, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -1, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -1, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 12, y + -1, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + -1, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -1, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -1, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -1, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -1, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 12, y + -1, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + -1, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -1, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -1, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -1, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -1, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 12, y + -1, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + -1, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -1, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -1, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -1, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -1, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 12, y + -1, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + -1, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -1, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -1, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -1, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -1, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -1, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 12, y + -1, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -1, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -1, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -1, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -1, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -1, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -1, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 12, y + -1, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -1, z + 11); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -1, z + 11); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -1, z + 11); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 11); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 11); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 11); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -1, z + 11); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -1, z + 11); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -1, z + 11); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -1, z + 12); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -1, z + 12); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -1, z + 12); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 12); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 12); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 12); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 12); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -1, z + 12); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -1, z + 12); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -1, z + 12); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -1, z + 13); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -1, z + 13); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -1, z + 13); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 13); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 13); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 13); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 13); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -1, z + 13); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -1, z + 13); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -1, z + 13); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -1, z + 14); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -1, z + 14); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -1, z + 14); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 14); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 14); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 14); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 14); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -1, z + 14); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -1, z + 14); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -1, z + 14); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -1, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -1, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -1, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -1, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -1, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -1, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -1, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -1, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -1, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -1, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -1, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -1, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + -1, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -1, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -1, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -1, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -1, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + -1, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -1, z + 18, Block1);
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -1, z + 18); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -1, z + 18); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 18); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 18); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 18); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 18); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -1, z + 18); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -1, z + 18); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -1, z + 18, Block1);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -1, z + 19, Block2);
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -1, z + 19); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -1, z + 19); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 19); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 19); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 19); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 19); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -1, z + 19); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -1, z + 19); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -1, z + 19, Block2);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -1, z + 20, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -1, z + 20); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -1, z + 20); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 20); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 20); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 20); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 20); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -1, z + 20); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -1, z + 20); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -1, z + 20, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -1, z + 21, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -1, z + 21); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -1, z + 21); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 21); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 21); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 21); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 21); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -1, z + 21); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -1, z + 21); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -1, z + 21, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -1, z + 22, Block2);
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -1, z + 22); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -1, z + 22); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 22); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 22); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 22); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 22); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -1, z + 22); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -1, z + 22); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -1, z + 22, Block2);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -1, z + 23, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -1, z + 23); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -1, z + 23); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 23); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 23); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 23); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 23); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -1, z + 23); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -1, z + 23); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -1, z + 23, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -1, z + 24, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -1, z + 24); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 4, y + -1, z + 24, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 5, y + -1, z + 24, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 24); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 24); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 8, y + -1, z + 24, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 9, y + -1, z + 24, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -1, z + 24); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -1, z + 24, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -1, z + 25, Block2);
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -1, z + 25); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 4, y + -1, z + 25, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 25); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 25); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 25); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 25); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 9, y + -1, z + 25, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -1, z + 25); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -1, z + 25, Block2);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -1, z + 26, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -1, z + 26); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 4, y + -1, z + 26, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 26); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 26); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 26); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 26); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 9, y + -1, z + 26, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -1, z + 26); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -1, z + 26, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -1, z + 27, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -1, z + 27); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		world.setBlock(x + 4, y + -1, z + 27, Block9, 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 27); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 27); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 27); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 27); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		world.setBlock(x + 9, y + -1, z + 27, Block9, 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -1, z + 27); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -1, z + 27, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -1, z + 28, Block2);
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + -1, z + 28); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 4, y + -1, z + 28, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 28); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 28); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 28); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 28); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 9, y + -1, z + 28, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + -1, z + 28); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -1, z + 28, Block2);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -1, z + 29, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 3, y + -1, z + 29, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 4, y + -1, z + 29, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 29); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 29); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 29); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 29); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 9, y + -1, z + 29, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 10, y + -1, z + 29, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -1, z + 29, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -1, z + 30, Block3);
		world.setBlock(x + 3, y + -1, z + 30, ModBlocks.pwr_control, 0, 3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 4, y + -1, z + 30, ModBlocks.red_cable);
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 30); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 30); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 30); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 30); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 9, y + -1, z + 30, ModBlocks.red_cable);
		world.setBlock(x + 10, y + -1, z + 30, ModBlocks.pwr_control, 0, 3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -1, z + 30, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -1, z + 31, Block2);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 3, y + -1, z + 31, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 4, y + -1, z + 31, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 31); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 31); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 31); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 31); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 9, y + -1, z + 31, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 10, y + -1, z + 31, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -1, z + 31, Block2);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -1, z + 32, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 3, y + -1, z + 32, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 4, y + -1, z + 32, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 32); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 32); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 32); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 32); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 9, y + -1, z + 32, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 10, y + -1, z + 32, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -1, z + 32, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -1, z + 33, Block3);
		world.setBlock(x + 4, y + -1, z + 33, Block9, 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 33); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 33); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 33); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 33); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		world.setBlock(x + 9, y + -1, z + 33, Block9, 0, 3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -1, z + 33, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + -1, z + 34, Block2);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 4, y + -1, z + 34, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 34); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 34); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 34); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 34); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 9, y + -1, z + 34, Block3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 11, y + -1, z + 34, Block2);
		world.setBlock(x + 2, y + -1, z + 35, Block4, 0, 3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 4, y + -1, z + 35, Block3);
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 35); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 35); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 35); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 35); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 9, y + -1, z + 35, Block3);
		world.setBlock(x + 11, y + -1, z + 35, Block4, 0, 3);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 4, y + -1, z + 36, Block10);
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 36); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 36); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 36); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 36); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 9, y + -1, z + 36, Block10);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 4, y + -1, z + 37, Block10);
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 37); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 37); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 37); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 37); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 9, y + -1, z + 37, Block10);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 4, y + -1, z + 38, Block10);
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 38); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 38); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 38); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 38); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 9, y + -1, z + 38, Block10);
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -1, z + 39); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 5, y + -1, z + 39, Block10);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 6, y + -1, z + 39, Block10);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 7, y + -1, z + 39, Block10);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 8, y + -1, z + 39, Block10);
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -1, z + 39); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -1, z + 40); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 40); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 40); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 40); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 40); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -1, z + 40); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -1, z + 41); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 41); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 41); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 41); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 41); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + -1, z + 42); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 42); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 42); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 42); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 42); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -1, z + 42); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + -1, z + 43); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 43); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 43); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 43); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + -1, z + 43); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 44); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 44); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + -1, z + 44); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + -1, z + 45); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + -1, z + 45); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 0, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + 0, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 0, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + 0, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 0, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 0, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + 0, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + 0, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 0, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 0, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 0, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + 0, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + 0, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 12, y + 0, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 0, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 0, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 0, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + 0, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + 0, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 12, y + 0, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 0, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 0, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 0, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + 0, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + 0, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 0, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 0, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + 0, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + 0, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 0, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 0, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + 0, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + 0, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + 0, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 0, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 0, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 0, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + 0, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + 0, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + 0, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + 0, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 0, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 0, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 0, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 0, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 0, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + 0, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + 0, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + 0, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + 0, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 0, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 0, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 0, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 0, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 0, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 0, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + 0, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + 0, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + 0, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 11, y + 0, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 0, z + 11); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 0, z + 11); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 0, z + 11); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 0, z + 11); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 0, z + 11); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 0, z + 11); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + 0, z + 11); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + 0, z + 11); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + 0, z + 11); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 0, z + 12); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 0, z + 12); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 0, z + 12); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 0, z + 12); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 0, z + 12); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 0, z + 12); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + 0, z + 12); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + 0, z + 12); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + 0, z + 12); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 0, z + 13); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 0, z + 13); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 0, z + 13); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 0, z + 13); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 0, z + 13); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 0, z + 13); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + 0, z + 13); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + 0, z + 13); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + 0, z + 13); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 0, z + 14); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 0, z + 14); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 0, z + 14); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 0, z + 14); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 0, z + 14); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 0, z + 14); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + 0, z + 14); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + 0, z + 14); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + 0, z + 14); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 0, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 0, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 0, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 0, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 0, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 0, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + 0, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + 0, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + 0, z + 15); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 0, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 0, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 0, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 0, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 0, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 0, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + 0, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 9, y + 0, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 10, y + 0, z + 16); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 0, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 0, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 0, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 0, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 0, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 0, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + 0, z + 17); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }

		new Spaceship2().generate_r00(world, rand, x, y, z);
		return true;

	}
}