package com.hbm.tileentity.machine;

import net.minecraft.util.math.BlockPos;

/**
 * Immutable mirror registration key. The legacy hash is intentional: boiler
 * beam limits consume HashSet iteration order. Do not replace with BlockPos's
 * hash, make this a BlockPos subclass, or introduce Comparable ordering.
 */
public final class SolarMirrorPosition {
    private final BlockPos position;

    public SolarMirrorPosition(int x, int y, int z) {
        this.position = new BlockPos(x, y, z);
    }

    public int getX() { return position.getX(); }
    public int getY() { return position.getY(); }
    public int getZ() { return position.getZ(); }

    @Override
    public int hashCode() {
        // Official 1.7.10 ChunkCoordinates hash, including overflow/shift masking.
        return ((getX() + getZ()) << (8 + getY())) << 16;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof SolarMirrorPosition)) return false;
        SolarMirrorPosition mirror = (SolarMirrorPosition) other;
        return position.equals(mirror.position);
    }
}
