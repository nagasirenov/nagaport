package com.hbm.world.test;

import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MapGenTest extends MapGenStructure {

	@Override
	public String getStructureName() {
		return "HFR_TEST";
	}

	@Override
	public BlockPos getNearestStructurePos(World world, BlockPos position, boolean findUnexplored) {
		return findNearestStructurePosBySpacing(world, this, position, 1, 0, 0, false, 100, findUnexplored);
	}

	@Override
	protected boolean canSpawnStructureAtCoords(int x, int z) {
		return true;
	}

	@Override
	protected StructureStart getStructureStart(int x, int z) {
		return new StructureStartTest(world, rand, x, z);
	}

}
