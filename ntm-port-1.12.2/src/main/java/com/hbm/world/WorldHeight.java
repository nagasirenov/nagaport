package com.hbm.world;

import java.util.Objects;
import net.minecraft.world.World;

/**
 * NTM client column-height queries retain the original horizontal-domain sentinel.
 * Accepted callers are the Torex client branch and satellite GUI only. Server
 * lookup unload-queue semantics differ from the original and are not approved
 * by this boundary; do not extend callers to server paths without a new proof.
 */
public final class WorldHeight {
    private WorldHeight() {}

    public static int read(World world, int x, int z) {
        // The old instance call checked null after evaluating both coordinates.
        Objects.requireNonNull(world);
        if (x < -30000000 || z < -30000000 || x >= 30000000 || z >= 30000000) {
            return 64;
        }
        return world.getHeight(x, z);
    }
}
