package com.hbm.explosion;

import net.minecraft.util.math.BlockPos;

/**
 * A real BlockPos that retains the legacy ChunkPosition hash used by explosion
 * affected-block HashSets. Matching the old hash preserves bucket placement and
 * normal-bin traversal order while the coordinates are migrated to BlockPos.
 */
public final class LegacyHashBlockPos extends BlockPos {

	public LegacyHashBlockPos(int x, int y, int z) {
		super(x, y, z);
	}

	@Override
	public int hashCode() {
		return getX() * 8976890 + getY() * 981131 + getZ();
	}
}
