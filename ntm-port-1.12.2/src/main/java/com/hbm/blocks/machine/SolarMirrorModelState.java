package com.hbm.blocks.machine;

import com.hbm.tileentity.machine.TileEntitySolarMirror;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

/** No world or mutable tile escapes into a chunk worker's model data. */
public final class SolarMirrorModelState {
    private SolarMirrorModelState() { }
    public static final class Snapshot {
        public static final Snapshot ABSENT = new Snapshot(false, false, 0, 0, 0);
        public final boolean available, pitched;
        public final int dx, dy, dz;
        public Snapshot(boolean available, boolean pitched, int dx, int dy, int dz) {
            this.available = available; this.pitched = pitched;
            this.dx = dx; this.dy = dy; this.dz = dz;
        }
        public static Snapshot capture(TileEntity tile) {
            if(!(tile instanceof TileEntitySolarMirror)) return ABSENT;
            TileEntitySolarMirror mirror = (TileEntitySolarMirror)tile;
            BlockPos pos = mirror.getPos();
            int tx = mirror.tX, ty = mirror.tY, tz = mirror.tZ;
            // Keep the original absolute-Y test separate from overflowing int subtraction.
            return new Snapshot(true, ty > pos.getY(), tx - pos.getX(), ty - pos.getY(), tz - pos.getZ());
        }
        @Override public String toString() { return available + ":" + pitched + ":" + dx + ":" + dy + ":" + dz; }
    }
    public static final IUnlistedProperty<Snapshot> MIRROR = new IUnlistedProperty<Snapshot>() {
        @Override public String getName() { return "solar_mirror_target"; }
        @Override public boolean isValid(Snapshot value) { return value != null; }
        @Override public Class<Snapshot> getType() { return Snapshot.class; }
        @Override public String valueToString(Snapshot value) { return value.toString(); }
    };
    public static BlockStateContainer create(Block block) {
        return new ExtendedBlockState(block, new IProperty<?>[0], new IUnlistedProperty<?>[] {MIRROR});
    }
    public static IBlockState extend(IBlockState state, IBlockAccess world, BlockPos pos) {
        return ((IExtendedBlockState)state).withProperty(MIRROR, Snapshot.capture(world.getTileEntity(pos)));
    }
    public static Snapshot snapshot(IBlockState state) {
        if(!(state instanceof IExtendedBlockState)) return Snapshot.ABSENT;
        Snapshot snapshot = ((IExtendedBlockState)state).getValue(MIRROR);
        return snapshot == null ? Snapshot.ABSENT : snapshot;
    }
}
