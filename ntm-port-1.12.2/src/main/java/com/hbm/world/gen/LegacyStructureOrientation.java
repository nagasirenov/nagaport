package com.hbm.world.gen;

import net.minecraft.util.EnumFacing;
import net.minecraft.world.gen.structure.StructureBoundingBox;

/**
 * NTM's structure rotations use the legacy horizontal order S, W, N, E.
 * The target StructureComponent owns the facing and its O NBT tag; no second
 * integer orientation is stored. The custom coordinate transforms rotate
 * instead of using vanilla's mirrored north/east transforms.
 */
public final class LegacyStructureOrientation {
    private LegacyStructureOrientation() { }

    public static EnumFacing fromLegacy(int mode) {
        switch(mode) {
        case -1: return null;
        case 0: return EnumFacing.SOUTH;
        case 1: return EnumFacing.WEST;
        case 2: return EnumFacing.NORTH;
        case 3: return EnumFacing.EAST;
        default: throw new IllegalArgumentException("Invalid structure orientation: " + mode);
        }
    }

    public static int toLegacy(EnumFacing facing) {
        if(facing == null) return -1;
        switch(facing) {
        case SOUTH: return 0;
        case WEST: return 1;
        case NORTH: return 2;
        case EAST: return 3;
        default: throw new IllegalArgumentException("Vertical structure orientation: " + facing);
        }
    }

    public static int offsetX(StructureBoundingBox box, int mode, int x, int z) {
        switch(mode) {
        case 0: return box.minX + x;
        case 1: return box.maxX - z;
        case 2: return box.maxX - x;
        case 3: return box.minX + z;
        default: return x;
        }
    }

    public static int offsetZ(StructureBoundingBox box, int mode, int x, int z) {
        switch(mode) {
        case 0: return box.minZ + z;
        case 1: return box.minZ + x;
        case 2: return box.maxZ - z;
        case 3: return box.maxZ - x;
        default: return z;
        }
    }
}
