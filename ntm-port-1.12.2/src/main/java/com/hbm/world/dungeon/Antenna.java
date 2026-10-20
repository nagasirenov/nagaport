//Schematic to java Structure by jajo_11 | inspired by "MITHION'S .SCHEMATIC TO JAVA CONVERTINGTOOL"

package com.hbm.world.dungeon;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
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

public class Antenna extends WorldGenerator
{
	protected Block[] GetValidSpawnBlocks()
	{
		return new Block[]
		{
			Blocks.GRASS,
			Blocks.DIRT,
			Blocks.STONE,
			Blocks.SAND,
		};
	}

	public boolean LocationIsValidSpawn(World world, int x, int y, int z)
 {

		Block checkBlock = (world.isValid(new BlockPos(x, y - 1, z)) ? world.getBlockState(new BlockPos(x, y - 1, z)).getBlock() : Blocks.AIR);
		Block blockAbove = (world.isValid(new BlockPos(x, y, z)) ? world.getBlockState(new BlockPos(x, y, z)).getBlock() : Blocks.AIR);
		Block blockBelow = (world.isValid(new BlockPos(x, y - 2, z)) ? world.getBlockState(new BlockPos(x, y - 2, z)).getBlock() : Blocks.AIR);

		for (Block i : GetValidSpawnBlocks())
		{
			if (blockAbove != Blocks.AIR)
			{
				return false;
			}
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
	public boolean generate(World world, Random rand, int x, int y, int z)
	{
		int i = rand.nextInt(1);

		if(i == 0)
		{
		    generate_r0(world, rand, x, y, z);
		}

       return true;

	}

	public boolean generate_r0(World world, Random rand, int x, int y, int z)
	{
		if(!LocationIsValidSpawn(world, x + 1, y, z + 1))
		{
			return false;
		}

		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 0, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + 1; int nativeOwnedWriteY = y + 0; int nativeOwnedWriteZ = z + 0; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.steel_poles; int nativeOwnedWriteMeta = 2; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 0, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + 0; int nativeOwnedWriteY = y + 0; int nativeOwnedWriteZ = z + 1; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.steel_poles; int nativeOwnedWriteMeta = 4; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 1, y + 0, z + 1, ModBlocks.deco_steel);
		world.setBlock(x + 2, y + 0, z + 1, ModBlocks.tape_recorder, 5, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 0, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + 1; int nativeOwnedWriteY = y + 0; int nativeOwnedWriteZ = z + 2; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.steel_poles; int nativeOwnedWriteMeta = 3; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
		world.setBlock(x + 2, y + 0, z + 2, Blocks.CHEST, 0, 3);
		world.setBlockMetadataWithNotify(x + 2, y + 0, z + 2, 5, 3);
        WeightedRandomChestContent.generateChestContents(rand, ItemPool.getPool(ItemPoolsLegacy.POOL_ANTENNA), (TileEntityChest)world.getTileEntity(new BlockPos(x + 2, y, z + 2)), 8);
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 1, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + 1; int nativeOwnedWriteY = y + 1; int nativeOwnedWriteZ = z + 0; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.steel_poles; int nativeOwnedWriteMeta = 2; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 1, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + 0; int nativeOwnedWriteY = y + 1; int nativeOwnedWriteZ = z + 1; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.steel_poles; int nativeOwnedWriteMeta = 4; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 1, y + 1, z + 1, ModBlocks.deco_steel);
		world.setBlock(x + 2, y + 1, z + 1, ModBlocks.tape_recorder, 5, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 1, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + 1; int nativeOwnedWriteY = y + 1; int nativeOwnedWriteZ = z + 2; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.steel_poles; int nativeOwnedWriteMeta = 3; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 1, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 2, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 1, y + 2, z + 0, ModBlocks.deco_steel);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 2, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 0, y + 2, z + 1, ModBlocks.deco_steel);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 1, y + 2, z + 1, ModBlocks.deco_steel);
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 2, y + 2, z + 1, ModBlocks.deco_steel);
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 2, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, x + 1, y + 2, z + 2, ModBlocks.deco_steel);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 2, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 3, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 3, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 3, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 3, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + 1; int nativeOwnedWriteY = y + 3; int nativeOwnedWriteZ = z + 1; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.steel_poles; int nativeOwnedWriteMeta = 4; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 3, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 3, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 3, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 3, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 4, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 4, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 4, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 4, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + 1; int nativeOwnedWriteY = y + 4; int nativeOwnedWriteZ = z + 1; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.steel_poles; int nativeOwnedWriteMeta = 4; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 4, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 4, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 4, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 4, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 5, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 5, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 5, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 5, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + 1; int nativeOwnedWriteY = y + 5; int nativeOwnedWriteZ = z + 1; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.steel_poles; int nativeOwnedWriteMeta = 4; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 5, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 5, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 5, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 5, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 6, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 6, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 6, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 6, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + 1; int nativeOwnedWriteY = y + 6; int nativeOwnedWriteZ = z + 1; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.steel_poles; int nativeOwnedWriteMeta = 4; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 6, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 6, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 6, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 6, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 7, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 7, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 7, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 7, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + 1; int nativeOwnedWriteY = y + 7; int nativeOwnedWriteZ = z + 1; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.steel_poles; int nativeOwnedWriteMeta = 4; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 7, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 7, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 7, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 7, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 8, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 8, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 8, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 8, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + 1; int nativeOwnedWriteY = y + 8; int nativeOwnedWriteZ = z + 1; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.steel_poles; int nativeOwnedWriteMeta = 4; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 8, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 8, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 8, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 8, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 9, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 9, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 9, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 9, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + 1; int nativeOwnedWriteY = y + 9; int nativeOwnedWriteZ = z + 1; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.steel_poles; int nativeOwnedWriteMeta = 4; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 9, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 9, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 9, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 9, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 10, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 10, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 10, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 10, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + 1; int nativeOwnedWriteY = y + 10; int nativeOwnedWriteZ = z + 1; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.steel_poles; int nativeOwnedWriteMeta = 4; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 10, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 10, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 10, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 10, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 11, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 11, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 11, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 11, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + 1; int nativeOwnedWriteY = y + 11; int nativeOwnedWriteZ = z + 1; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.steel_poles; int nativeOwnedWriteMeta = 4; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 11, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 11, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 11, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 11, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 12, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 12, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 12, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 12, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + 1; int nativeOwnedWriteY = y + 12; int nativeOwnedWriteZ = z + 1; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.steel_poles; int nativeOwnedWriteMeta = 4; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 12, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 12, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 12, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 12, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 13, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 13, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 13, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 13, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		world.setBlock(x + 1, y + 13, z + 1, ModBlocks.pole_satellite_receiver, 3, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 13, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 13, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 13, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 13, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 14, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 14, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 14, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 14, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + 1; int nativeOwnedWriteY = y + 14; int nativeOwnedWriteZ = z + 1; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.steel_poles; int nativeOwnedWriteMeta = 4; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 14, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 14, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 14, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 14, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 15, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 15, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 15, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 15, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + 1; int nativeOwnedWriteY = y + 15; int nativeOwnedWriteZ = z + 1; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.steel_poles; int nativeOwnedWriteMeta = 4; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 15, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 15, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 15, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 15, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 16, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 16, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 16, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 16, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + 1; int nativeOwnedWriteY = y + 16; int nativeOwnedWriteZ = z + 1; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.steel_poles; int nativeOwnedWriteMeta = 4; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 16, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 16, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 16, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 16, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 17, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 17, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 17, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 17, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		world.setBlock(x + 1, y + 17, z + 1, ModBlocks.pole_satellite_receiver, 2, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 17, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 17, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 17, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 17, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 18, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 18, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 18, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 18, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		world.setBlock(x + 1, y + 18, z + 1, ModBlocks.pole_satellite_receiver, 4, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 18, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 18, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 18, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 18, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 19, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 19, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 19, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 19, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + 1; int nativeOwnedWriteY = y + 19; int nativeOwnedWriteZ = z + 1; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.steel_poles; int nativeOwnedWriteMeta = 4; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 19, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 19, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 19, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 19, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 20, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 20, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 20, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 20, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ net.minecraft.world.World nativeOwnedWriteWorld = world; int nativeOwnedWriteX = x + 1; int nativeOwnedWriteY = y + 20; int nativeOwnedWriteZ = z + 1; net.minecraft.block.Block nativeOwnedWriteBlock = ModBlocks.pole_top; int nativeOwnedWriteMeta = 4; int nativeOwnedWriteFlags = 3; if (nativeOwnedWriteWorld == null) throw new NullPointerException(); if (nativeOwnedWriteX >= -30000000 && nativeOwnedWriteX < 30000000 && nativeOwnedWriteZ >= -30000000 && nativeOwnedWriteZ < 30000000 && nativeOwnedWriteY >= 0 && nativeOwnedWriteY < 256) com.hbm.world.WorldBlockState.write(nativeOwnedWriteWorld, nativeOwnedWriteX, nativeOwnedWriteY, nativeOwnedWriteZ, nativeOwnedWriteBlock.getStateFromMeta(nativeOwnedWriteMeta), nativeOwnedWriteFlags); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 20, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 20, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 20, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 20, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		if(GeneralConfig.enableDebugMode)
			System.out.print("[Debug] Successfully spawned antenna at " + x + " " + y +" " + z + "\n");
		return true;

	}

}