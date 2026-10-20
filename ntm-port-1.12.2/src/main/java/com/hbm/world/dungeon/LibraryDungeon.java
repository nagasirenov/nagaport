//Schematic to java Structure by jajo_11 | inspired by "MITHION'S .SCHEMATIC TO JAVA CONVERTINGTOOL"

package com.hbm.world.dungeon;

import java.util.Random;

import com.hbm.config.GeneralConfig;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.DungeonHooks;

public class LibraryDungeon extends WorldGenerator
{

    /** Native boundary for this dungeon's audited vanilla stone-brick variants. */
    private static void placeStoneBrick(World world, int x, int y, int z,
            net.minecraft.block.BlockStoneBrick brick, int metadata) {
        if(world == null) throw new NullPointerException();
        if(x >= -30000000 && x < 30000000 && z >= -30000000 && z < 30000000 && y >= 0 && y < 256) {
            world.setBlockState(new BlockPos(x, y, z), brick.getStateFromMeta(metadata), 3);
        }
    }

    private static void fillDungeonChest(World world, Random random, TileEntityChest chest) {
        WorldServer server = (WorldServer) world;
        LootContext context = new LootContext.Builder(server).build();
        server.getLootTableManager().getLootTableFromLocation(LootTableList.CHESTS_SIMPLE_DUNGEON)
            .fillInventory(chest, random, context);
    }

	public boolean LocationIsValidSpawn(World world, int x, int y, int z)
 {

		Block blockAbove = (world.isValid(new BlockPos(x, y  + 8, z)) ? world.getBlockState(new BlockPos(x, y  + 8, z)).getBlock() : Blocks.AIR);
		Block blockBelow = (world.isValid(new BlockPos(x, y - 1, z)) ? world.getBlockState(new BlockPos(x, y - 1, z)).getBlock() : Blocks.AIR);
		
		if(blockAbove.getDefaultState().getMaterial().isSolid() && blockBelow.getDefaultState().getMaterial().isSolid() && y - 1 > 4)
		{
			return true;
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
		if(!LocationIsValidSpawn(world, x, y, z) || !LocationIsValidSpawn(world, x + 8, y, z) || !LocationIsValidSpawn(world, x + 8, y, z + 10) || !LocationIsValidSpawn(world, x, y, z + 10))
		{
			return false;
		}

		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 0, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		placeStoneBrick(world, x + 1, y + 0, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 0, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 0, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 0, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 0, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 0, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 0, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + 0, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		placeStoneBrick(world, x + 0, y + 0, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 1, y + 0, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 0, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 0, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 0, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 0, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 0, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 0, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 8, y + 0, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 0, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 1, y + 0, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 0, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 0, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 0, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 0, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 0, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 0, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 8, y + 0, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 0, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 1, y + 0, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 0, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 0, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 0, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 0, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 0, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 0, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 8, y + 0, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 0, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 1, y + 0, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 0, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 0, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 0, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 0, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 0, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 0, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 8, y + 0, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 0, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 1, y + 0, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 0, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 0, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 0, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 0, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 0, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 0, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 8, y + 0, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 0, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 1, y + 0, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 0, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 0, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 0, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 0, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 0, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 0, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 8, y + 0, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 0, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 1, y + 0, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 0, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 0, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 0, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 0, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 0, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 0, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 8, y + 0, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 0, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 1, y + 0, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 0, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 0, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 0, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 0, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 0, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 0, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 8, y + 0, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 0, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 1, y + 0, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 0, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 0, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 0, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 0, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 0, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 0, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 8, y + 0, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 0, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		placeStoneBrick(world, x + 1, y + 0, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 0, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 0, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 0, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 0, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 0, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 0, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + 0, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 1, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		placeStoneBrick(world, x + 1, y + 1, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 1, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 1, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 1, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 1, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 1, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 1, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + 1, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		placeStoneBrick(world, x + 0, y + 1, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		world.setBlock(x + 1, y + 1, z + 1, getShelf(rand), 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 1, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 1, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 1, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 1, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 1, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 1, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 1, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 1, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		world.setBlock(x + 1, y + 1, z + 2, getShelf(rand), 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 1, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 1, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 1, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 1, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }

		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 1, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.MOB_SPAWNER.getStateFromMeta(0), 2); }
        TileEntityMobSpawner tileentitymobspawner = (TileEntityMobSpawner)world.getTileEntity(new BlockPos(x + 6, y + 1, z + 2));

        if (tileentitymobspawner != null)
        {
            tileentitymobspawner.func_145881_a().setEntityName(this.pickMobSpawner(rand));
        }
        else
        {
            System.err.println("Failed to fetch mob spawner entity at (" + (x + 6) + ", " + (y + 1) + ", " + (z + 2) + ")");
        }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 1, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 1, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 1, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		world.setBlock(x + 1, y + 1, z + 3, getShelf(rand), 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 1, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 1, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 1, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.OAK_FENCE.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 1, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 1, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 1, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 1, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 1, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		world.setBlock(x + 1, y + 1, z + 4, getShelf(rand), 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 1, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 1, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 1, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 1, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 1, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 1, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 1, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 1, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 1, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.CHEST.getStateFromMeta(5), 3); }
        TileEntityChest tileentitychest = (TileEntityChest)world.getTileEntity(new BlockPos(x + 1, y + 1, z + 5));

        if (tileentitychest != null)
        {
            fillDungeonChest(world, rand, tileentitychest);
        }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 1, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 1, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 1, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 1, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 1, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 1, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 1, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 1, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		world.setBlock(x + 1, y + 1, z + 6, getShelf(rand), 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 1, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 1, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 1, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.OAK_FENCE.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 1, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 1, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 1, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 1, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 1, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		world.setBlock(x + 1, y + 1, z + 7, getShelf(rand), 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 1, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 1, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 1, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 1, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 1, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 1, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 1, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 1, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 1, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.CHEST.getStateFromMeta(5), 3); }
        TileEntityChest tileentitychest1 = (TileEntityChest)world.getTileEntity(new BlockPos(x + 1, y + 1, z + 8));

        if (tileentitychest1 != null)
        {
            fillDungeonChest(world, rand, tileentitychest1);
        }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 1, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 1, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 1, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 1, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 1, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.BEDROCK.getStateFromMeta(0), 3); }

		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 1, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.MOB_SPAWNER.getStateFromMeta(0), 2); }
        TileEntityMobSpawner tileentitymobspawner1 = (TileEntityMobSpawner)world.getTileEntity(new BlockPos(x + 6, y + 1, z + 8));

        if (tileentitymobspawner1 != null)
        {
            tileentitymobspawner1.func_145881_a().setEntityName(this.pickMobSpawner(rand));
        }
        else
        {
            System.err.println("Failed to fetch mob spawner entity at (" + (x + 6) + ", " + (y + 1) + ", " + (z + 8) + ")");
        }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 1, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 1, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 1, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		world.setBlock(x + 1, y + 1, z + 9, getShelf(rand), 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 1, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 1, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 1, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 1, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 1, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 1, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 1, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 1, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		placeStoneBrick(world, x + 1, y + 1, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 1, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 1, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 1, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 1, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 1, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 1, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + 1, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 2, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		placeStoneBrick(world, x + 1, y + 2, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 2, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 2, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 2, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 2, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 2, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 2, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + 2, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		placeStoneBrick(world, x + 0, y + 2, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		world.setBlock(x + 1, y + 2, z + 1, getShelf(rand), 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 2, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 2, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 2, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 2, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 2, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 2, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 2, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 2, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		world.setBlock(x + 1, y + 2, z + 2, getShelf(rand), 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 2, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 2, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 2, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 2, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 2, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 2, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 2, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 2, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 2, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.CHEST.getStateFromMeta(5), 3); }
        TileEntityChest tileentitychest2 = (TileEntityChest)world.getTileEntity(new BlockPos(x + 1, y + 2, z + 3));

        if (tileentitychest2 != null)
        {
            fillDungeonChest(world, rand, tileentitychest2);
        }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 2, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 2, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 2, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.OAK_FENCE.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 2, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 2, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 2, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 2, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 2, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		world.setBlock(x + 1, y + 2, z + 4, getShelf(rand), 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 2, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 2, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 2, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 2, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 2, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 2, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 2, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 2, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 2, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 2, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 2, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 2, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 2, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 2, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 2, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 2, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 2, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		world.setBlock(x + 1, y + 2, z + 6, getShelf(rand), 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 2, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 2, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 2, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.OAK_FENCE.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 2, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 2, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 2, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 2, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 2, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		world.setBlock(x + 1, y + 2, z + 7, getShelf(rand), 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 2, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 2, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 2, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 2, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 2, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 2, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 2, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 2, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 2, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 2, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 2, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 2, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 2, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 2, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 2, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 2, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 2, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		world.setBlock(x + 1, y + 2, z + 9, getShelf(rand), 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 2, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 2, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 2, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 2, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 2, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 2, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 2, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 2, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		placeStoneBrick(world, x + 1, y + 2, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 2, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 2, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 2, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 2, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 2, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 2, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + 2, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 3, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		placeStoneBrick(world, x + 1, y + 3, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 3, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 3, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 3, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 3, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 3, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 3, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + 3, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		placeStoneBrick(world, x + 0, y + 3, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 3, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 3, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 3, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 3, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 3, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 3, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 3, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 3, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 3, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 3, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 3, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 3, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 3, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 3, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 3, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 3, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 3, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 3, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 3, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 3, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 3, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.DOUBLE_WOODEN_SLAB.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 3, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 3, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 3, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 3, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 3, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 3, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 3, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 3, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 3, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 3, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 3, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 3, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 3, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 3, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 3, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 3, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 3, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 3, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 3, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 3, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 3, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 3, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 3, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 3, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 3, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 3, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.DOUBLE_WOODEN_SLAB.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 3, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 3, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 3, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 3, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 3, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 3, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 3, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 3, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 3, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 3, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 3, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 3, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 3, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 3, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 3, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 3, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 3, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 3, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 3, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 3, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 3, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 3, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 3, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 3, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 3, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 3, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 3, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 3, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 3, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 3, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 3, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		placeStoneBrick(world, x + 1, y + 3, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 3, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 3, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 3, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 3, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 3, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 3, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + 3, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 4, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		placeStoneBrick(world, x + 1, y + 4, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 4, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 4, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 4, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 4, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 4, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 4, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + 4, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		placeStoneBrick(world, x + 0, y + 4, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 4, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 4, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 4, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 4, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 4, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 4, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 4, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 4, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 4, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 4, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 4, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 4, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 4, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 4, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 4, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 4, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 4, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 4, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 4, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 4, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 4, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 4, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.OAK_FENCE.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 4, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 4, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 4, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 4, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 4, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		world.setBlock(x + 1, y + 4, z + 4, getShelf(rand), 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 4, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 4, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 4, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 4, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 4, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 4, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 4, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 4, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 4, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.CHEST.getStateFromMeta(5), 3); }
        TileEntityChest tileentitychest3 = (TileEntityChest)world.getTileEntity(new BlockPos(x + 1, y + 4, z + 5));

        if (tileentitychest3 != null)
        {
            fillDungeonChest(world, rand, tileentitychest3);
        }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 4, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 4, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.BEDROCK.getStateFromMeta(0), 3); }

		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 4, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.MOB_SPAWNER.getStateFromMeta(0), 2); }
        TileEntityMobSpawner tileentitymobspawner2 = (TileEntityMobSpawner)world.getTileEntity(new BlockPos(x + 3, y + 4, z + 5));

        if (tileentitymobspawner2 != null)
        {
            tileentitymobspawner2.func_145881_a().setEntityName(this.pickMobSpawner(rand));
        }
        else
        {
            System.err.println("Failed to fetch mob spawner entity at (" + (x + 3) + ", " + (y + 4) + ", " + (z + 5) + ")");
        }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 4, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 4, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 4, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 4, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 4, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 4, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		world.setBlock(x + 1, y + 4, z + 6, getShelf(rand), 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 4, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 4, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 4, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.OAK_FENCE.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 4, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 4, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 4, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 4, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 4, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		world.setBlock(x + 1, y + 4, z + 7, getShelf(rand), 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 4, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 4, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 4, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 4, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 4, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 4, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 4, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 4, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		world.setBlock(x + 1, y + 4, z + 8, getShelf(rand), 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 4, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 4, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 4, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 4, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 4, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 4, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 4, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 4, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		world.setBlock(x + 1, y + 4, z + 9, getShelf(rand), 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 4, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.CHEST.getStateFromMeta(2), 3); }
        TileEntityChest tileentitychest4 = (TileEntityChest)world.getTileEntity(new BlockPos(x + 2, y + 4, z + 9));

        if (tileentitychest4 != null)
        {
            fillDungeonChest(world, rand, tileentitychest4);
        }
		world.setBlock(x + 3, y + 4, z + 9, getShelf(rand), 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 4, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 4, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 4, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 4, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 4, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 4, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		placeStoneBrick(world, x + 1, y + 4, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 4, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 4, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 4, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 4, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 4, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 4, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + 4, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 5, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		placeStoneBrick(world, x + 1, y + 5, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 5, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 5, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 5, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 5, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 5, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 5, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + 5, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		placeStoneBrick(world, x + 0, y + 5, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 5, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 5, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 5, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 5, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 5, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 5, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 5, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		placeStoneBrick(world, x + 8, y + 5, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 5, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 5, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 5, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 5, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 5, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 5, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 5, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 5, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		placeStoneBrick(world, x + 8, y + 5, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 5, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 5, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 5, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 5, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.DOUBLE_WOODEN_SLAB.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 5, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 5, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 5, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		placeStoneBrick(world, x + 8, y + 5, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 5, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		world.setBlock(x + 1, y + 5, z + 4, getShelf(rand), 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 5, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 5, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 5, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 5, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 5, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 5, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		placeStoneBrick(world, x + 8, y + 5, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 5, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 5, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 5, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 5, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 5, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 5, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 5, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 5, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		placeStoneBrick(world, x + 8, y + 5, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 5, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		world.setBlock(x + 1, y + 5, z + 6, getShelf(rand), 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 5, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 5, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.DOUBLE_WOODEN_SLAB.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 5, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 5, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 5, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		placeStoneBrick(world, x + 8, y + 5, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 5, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 5, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.CHEST.getStateFromMeta(5), 3); }
        TileEntityChest tileentitychest5 = (TileEntityChest)world.getTileEntity(new BlockPos(x + 1, y + 5, z + 7));

        if (tileentitychest5 != null)
        {
            fillDungeonChest(world, rand, tileentitychest5);
        }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 5, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 5, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 5, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 5, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 5, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 5, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 5, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		world.setBlock(x + 1, y + 5, z + 8, getShelf(rand), 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 5, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 5, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 5, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 5, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 5, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 5, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 5, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 5, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		world.setBlock(x + 1, y + 5, z + 9, getShelf(rand), 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 5, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		world.setBlock(x + 3, y + 5, z + 9, getShelf(rand), 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 5, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 5, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 5, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 5, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 5, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 5, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		placeStoneBrick(world, x + 1, y + 5, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 5, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 5, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 5, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 5, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 5, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 5, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + 5, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 6, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		placeStoneBrick(world, x + 1, y + 6, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 6, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 6, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 6, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 6, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 6, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 6, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + 6, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		placeStoneBrick(world, x + 0, y + 6, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 6, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 6, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 6, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 6, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		world.setBlock(x + 5, y + 6, z + 1, getShelf(rand), 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 6, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.CHEST.getStateFromMeta(3), 3); }
        TileEntityChest tileentitychest6 = (TileEntityChest)world.getTileEntity(new BlockPos(x + 6, y + 6, z + 1));

        if (tileentitychest6 != null)
        {
            fillDungeonChest(world, rand, tileentitychest6);
        }
		world.setBlock(x + 7, y + 6, z + 1, getShelf(rand), 0, 3);
		placeStoneBrick(world, x + 8, y + 6, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 6, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 6, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 6, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 6, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 6, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 6, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 6, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		world.setBlock(x + 7, y + 6, z + 2, getShelf(rand), 0, 3);
		placeStoneBrick(world, x + 8, y + 6, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 6, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 6, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 6, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 6, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 6, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.OAK_FENCE.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 6, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 6, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		world.setBlock(x + 7, y + 6, z + 3, getShelf(rand), 0, 3);
		placeStoneBrick(world, x + 8, y + 6, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 6, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 6, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 6, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 6, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 6, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 6, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.BEDROCK.getStateFromMeta(0), 3); }

		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 6, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.MOB_SPAWNER.getStateFromMeta(0), 2); }
        TileEntityMobSpawner tileentitymobspawner3 = (TileEntityMobSpawner)world.getTileEntity(new BlockPos(x + 5, y + 6, z + 4));

        if (tileentitymobspawner3 != null)
        {
            tileentitymobspawner3.func_145881_a().setEntityName(this.pickMobSpawner(rand));
        }
        else
        {
            System.err.println("Failed to fetch mob spawner entity at (" + (x + 5) + ", " + (y + 6) + ", " + (z + 4) + ")");
        }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 6, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 6, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.CHEST.getStateFromMeta(4), 3); }
        TileEntityChest tileentitychest7 = (TileEntityChest)world.getTileEntity(new BlockPos(x + 7, y + 6, z + 4));

        if (tileentitychest7 != null)
        {
            fillDungeonChest(world, rand, tileentitychest7);
        }
		placeStoneBrick(world, x + 8, y + 6, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 6, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 6, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 6, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 6, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 6, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 6, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 6, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		world.setBlock(x + 7, y + 6, z + 5, getShelf(rand), 0, 3);
		placeStoneBrick(world, x + 8, y + 6, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 6, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 6, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 6, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 6, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 6, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.OAK_FENCE.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 6, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 6, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 6, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 6, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 6, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 6, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 6, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 6, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 6, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 6, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 6, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 6, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 6, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 6, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 6, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 6, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 6, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 6, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 6, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 6, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 6, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 6, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 6, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 6, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 6, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 6, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 6, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 6, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 6, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 6, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 6, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 6, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		placeStoneBrick(world, x + 1, y + 6, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 6, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 6, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 6, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 6, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 6, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 6, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + 6, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 7, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		placeStoneBrick(world, x + 1, y + 7, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 7, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 7, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 7, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 7, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 7, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 7, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + 7, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		placeStoneBrick(world, x + 0, y + 7, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 7, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 7, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 7, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 7, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		world.setBlock(x + 5, y + 7, z + 1, getShelf(rand), 0, 3);
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 7, z + 1); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		world.setBlock(x + 7, y + 7, z + 1, getShelf(rand), 0, 3);
		placeStoneBrick(world, x + 8, y + 7, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 7, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 7, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 7, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 7, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 7, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 7, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 7, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		world.setBlock(x + 7, y + 7, z + 2, getShelf(rand), 0, 3);
		placeStoneBrick(world, x + 8, y + 7, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 7, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 7, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 7, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 7, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 7, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.OAK_FENCE.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 7, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 7, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		world.setBlock(x + 7, y + 7, z + 3, getShelf(rand), 0, 3);
		placeStoneBrick(world, x + 8, y + 7, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 7, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 7, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 7, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 7, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 7, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 7, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 7, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 7, z + 4); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.WOODEN_SLAB.getStateFromMeta(8), 3); }
		placeStoneBrick(world, x + 8, y + 7, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 7, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 7, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 7, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 7, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 7, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 7, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 7, z + 5); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		world.setBlock(x + 7, y + 7, z + 5, getShelf(rand), 0, 3);
		placeStoneBrick(world, x + 8, y + 7, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 7, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 7, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 7, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 7, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 7, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.OAK_FENCE.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 7, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 7, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 7, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 7, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 7, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 7, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 7, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 7, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 7, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 7, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 7, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 7, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 7, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 7, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 7, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 7, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 7, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 7, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 7, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 7, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 7, z + 8); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 7, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 7, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 1, y + 7, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 2, y + 7, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 7, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 7, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 7, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 6, y + 7, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 7, y + 7, z + 9); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		placeStoneBrick(world, x + 8, y + 7, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 7, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		placeStoneBrick(world, x + 1, y + 7, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 7, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 7, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 7, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 7, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 7, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 7, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + 7, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 8, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		placeStoneBrick(world, x + 1, y + 8, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 8, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 8, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 8, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 8, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 8, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 8, z + 0, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + 8, z + 0); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		placeStoneBrick(world, x + 0, y + 8, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 1, y + 8, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 8, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 8, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 8, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 8, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 8, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 8, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 8, y + 8, z + 1, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 8, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 1, y + 8, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 8, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 8, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 8, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 8, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 8, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 8, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 8, y + 8, z + 2, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 8, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 1, y + 8, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 8, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 8, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 8, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 8, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 8, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 8, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 8, y + 8, z + 3, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 8, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 1, y + 8, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 8, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 8, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 8, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 8, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 8, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 8, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 8, y + 8, z + 4, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 8, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 1, y + 8, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 8, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 8, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 8, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 8, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 8, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 8, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 8, y + 8, z + 5, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 8, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 1, y + 8, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 8, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 8, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 8, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 8, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 8, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 8, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 8, y + 8, z + 6, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 8, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 1, y + 8, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 8, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 8, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 8, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 8, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 8, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 8, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 8, y + 8, z + 7, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 8, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 1, y + 8, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 8, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 8, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 8, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 8, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 8, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 8, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 8, y + 8, z + 8, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 0, y + 8, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 1, y + 8, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 8, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 8, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 8, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 8, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 8, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 8, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 8, y + 8, z + 9, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 0, y + 8, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }
		placeStoneBrick(world, x + 1, y + 8, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 2, y + 8, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 3, y + 8, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 4, y + 8, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 5, y + 8, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 6, y + 8, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		placeStoneBrick(world, x + 7, y + 8, z + 10, (net.minecraft.block.BlockStoneBrick) Blocks.STONEBRICK, getBrick(rand));
		{ BlockPos dungeonPos = new BlockPos(x + 8, y + 8, z + 10); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.STONEBRICK.getStateFromMeta(3), 3); }

		generate_r02_last(world, rand, x, y, z);
		return true;

	}
	public boolean generate_r02_last(World world, Random rand, int x, int y, int z)
	{

		/*world.setBlock(x + 4, y + 3, z + 2, Blocks.torch, 4, 3);
		world.setBlock(x + 5, y + 3, z + 3, Blocks.torch, 1, 3);
		world.setBlock(x + 5, y + 3, z + 6, Blocks.torch, 1, 3);
		world.setBlock(x + 3, y + 5, z + 3, Blocks.torch, 2, 3);
		world.setBlock(x + 3, y + 5, z + 6, Blocks.torch, 2, 3);
		world.setBlock(x + 4, y + 5, z + 7, Blocks.torch, 3, 3);*/
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 3, z + 2); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 3, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 5, y + 3, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 5, z + 3); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 3, y + 5, z + 6); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		{ BlockPos dungeonPos = new BlockPos(x + 4, y + 5, z + 7); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.AIR.getStateFromMeta(0), 3); }
		if(GeneralConfig.enableDebugMode)
			System.out.print("[Debug] Successfully spawned library at " + x + " " + y +" " + z + "\n");
		return true;

	}
	public int getBrick(Random rand) {
		return rand.nextInt(3);
		
	}
	public Block getShelf(Random rand) {
		int i = rand.nextInt(2);
		if(i == 0)
		{
			return Blocks.PLANKS;
		}
		return Blocks.BOOKSHELF;
	}
    private String pickMobSpawner(Random p_76543_1_)
    {
        return DungeonHooks.getRandomDungeonMob(p_76543_1_);
    }

}