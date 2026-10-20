package com.hbm.blocks.machine;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

/** Immutable render snapshot; connection identity deliberately ignores neighbor orientation. */
public final class SpotlightModelState {
    private SpotlightModelState() { }
    public static final IUnlistedProperty<Integer> CONNECTIONS = new IUnlistedProperty<Integer>() {
        @Override public String getName() { return "spotlight_connections"; }
        @Override public boolean isValid(Integer value) { return value != null && value >= 0 && value < 64; }
        @Override public Class<Integer> getType() { return Integer.class; }
        @Override public String valueToString(Integer value) { return value.toString(); }
    };
    public static BlockStateContainer create(Block block, PropertyInteger metadata) {
        return new ExtendedBlockState(block, new IProperty<?>[] {metadata}, new IUnlistedProperty<?>[] {CONNECTIONS});
    }
    public static IBlockState extend(IBlockState state, IBlockAccess world, BlockPos pos) {
        int mask = 0;
        if(state.getBlock() instanceof SpotlightModular) {
            SpotlightModular block = (SpotlightModular)state.getBlock();
            for(EnumFacing side : EnumFacing.values()) {
                BlockPos neighbor = pos.offset(side);
                if(block.canConnectTo(world, neighbor.getX(), neighbor.getY(), neighbor.getZ())) mask |= 1 << side.getIndex();
            }
        }
        return ((IExtendedBlockState)state).withProperty(CONNECTIONS, mask);
    }
    public static int connections(IBlockState state) {
        if(!(state instanceof IExtendedBlockState)) throw new IllegalArgumentException("Spotlight world model requires its extended state");
        Integer value = ((IExtendedBlockState)state).getValue(CONNECTIONS);
        if(value == null) throw new IllegalArgumentException("Spotlight world model requires its connection snapshot");
        return value;
    }
}
