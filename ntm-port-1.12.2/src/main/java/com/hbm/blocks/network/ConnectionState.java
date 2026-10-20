package com.hbm.blocks.network;

import com.hbm.lib.Library;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IUnlistedProperty;

/** Render-only six-side connectivity. Bits retain the network blocks' original XYZ order. */
public final class ConnectionState {
    private ConnectionState() { }
    public static final int POS_X = 32, NEG_X = 16, POS_Y = 8, NEG_Y = 4, POS_Z = 2, NEG_Z = 1;
    public static final IUnlistedProperty<Integer> CONNECTIONS = new IUnlistedProperty<Integer>() {
        @Override public String getName() { return "connections"; }
        @Override public boolean isValid(Integer value) { return value != null && value >= 0 && value < 64; }
        @Override public Class<Integer> getType() { return Integer.class; }
        @Override public String valueToString(Integer value) { return value.toString(); }
    };
    public static int energy(IBlockAccess world, BlockPos pos) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        return (Library.canConnect(world, x + 1, y, z, Library.POS_X) ? POS_X : 0)
             | (Library.canConnect(world, x - 1, y, z, Library.NEG_X) ? NEG_X : 0)
             | (Library.canConnect(world, x, y + 1, z, Library.POS_Y) ? POS_Y : 0)
             | (Library.canConnect(world, x, y - 1, z, Library.NEG_Y) ? NEG_Y : 0)
             | (Library.canConnect(world, x, y, z + 1, Library.POS_Z) ? POS_Z : 0)
             | (Library.canConnect(world, x, y, z - 1, Library.NEG_Z) ? NEG_Z : 0);
    }
}
