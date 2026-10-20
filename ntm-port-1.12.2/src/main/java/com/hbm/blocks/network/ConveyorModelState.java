package com.hbm.blocks.network;

import api.hbm.conveyor.IConveyorBelt;
import api.hbm.conveyor.IEnterableBlock;
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

/** Render-only neighbor snapshot. Movement and collision retain their separate original predicates. */
public final class ConveyorModelState {
    private ConveyorModelState() { }
    public static final int BELOW_ENTERABLE = 1 << 6;
    public static final int ABOVE_ENTERABLE = 1 << 7;
    public static final int ABOVE_MIN_Y = 1 << 8;
    public static final int BELOW_MAX_Y = 1 << 9;
    public static final IUnlistedProperty<Integer> NEIGHBORS = new IUnlistedProperty<Integer>() {
        @Override public String getName() { return "conveyor_neighbors"; }
        @Override public boolean isValid(Integer value) { return value != null && value >= 0 && value < 1024; }
        @Override public Class<Integer> getType() { return Integer.class; }
        @Override public String valueToString(Integer value) { return value.toString(); }
    };
    public static BlockStateContainer create(Block block, PropertyInteger metadata) {
        return new ExtendedBlockState(block, new IProperty<?>[] {metadata}, new IUnlistedProperty<?>[] {NEIGHBORS});
    }
    public static IBlockState extend(IBlockState state, IBlockAccess world, BlockPos pos) {
        int mask = 0;
        for(EnumFacing side : EnumFacing.values()) {
            Block neighbor = world.getBlockState(pos.offset(side)).getBlock();
            if(neighbor instanceof IConveyorBelt) mask |= 1 << side.getIndex();
            if(neighbor instanceof IEnterableBlock) {
                if(side == EnumFacing.DOWN) mask |= BELOW_ENTERABLE;
                if(side == EnumFacing.UP) mask |= ABOVE_ENTERABLE;
            }
        }
        if(pos.getY() > 0) mask |= ABOVE_MIN_Y;
        if(pos.getY() < 255) mask |= BELOW_MAX_Y;
        return ((IExtendedBlockState)state).withProperty(NEIGHBORS, mask);
    }
    public static boolean belt(int mask, EnumFacing side) { return (mask & (1 << side.getIndex())) != 0; }
    public static int neighbors(IBlockState state) {
        if(!(state instanceof IExtendedBlockState)) throw new IllegalArgumentException("Conveyor world geometry requires its extended state");
        Integer mask = ((IExtendedBlockState)state).getValue(NEIGHBORS);
        if(mask == null) throw new IllegalArgumentException("Conveyor world geometry requires a neighbor snapshot");
        return mask;
    }
}
