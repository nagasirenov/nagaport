package com.hbm.blocks.generic;

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

/** Render-only adjacency, never stored in the block metadata or a mutable Block singleton. */
public final class UniversalBlockState {
    private UniversalBlockState() { }
    public static final PropertyInteger META = PropertyInteger.create("meta", 0, 15);
    public static final IUnlistedProperty<Integer> CONNECTIONS = new IUnlistedProperty<Integer>() {
        @Override public String getName() { return "universal_connections"; }
        @Override public boolean isValid(Integer value) { return value != null && value >= 0 && value < 64; }
        @Override public Class<Integer> getType() { return Integer.class; }
        @Override public String valueToString(Integer value) { return value.toString(); }
    };
    public static BlockStateContainer create(Block block) {
        return new ExtendedBlockState(block, new IProperty<?>[]{META}, new IUnlistedProperty<?>[]{CONNECTIONS});
    }
    public static int metadata(IBlockState state) { return state.getValue(META); }
    public static int connections(IBlockState state) {
        Integer value = state instanceof IExtendedBlockState ? ((IExtendedBlockState)state).getValue(CONNECTIONS) : null;
        return value == null ? 0 : value;
    }
    public static IBlockState withConnections(IBlockState state, int connections) {
        return ((IExtendedBlockState)state).withProperty(CONNECTIONS, connections);
    }
    public static boolean connected(int mask, EnumFacing side) { return (mask & (1 << side.getIndex())) != 0; }
    public static boolean solid(IBlockState state) { return state.isOpaqueCube() || state.isNormalCube(); }
    public static int solidNeighbors(IBlockAccess world, BlockPos pos) {
        int result = 0;
        for(EnumFacing side : EnumFacing.values()) if(solid(world.getBlockState(pos.offset(side)))) result |= 1 << side.getIndex();
        return result;
    }
}
