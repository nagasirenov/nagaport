package com.hbm.blocks.generic;

import java.util.*;
import com.hbm.render.block.ISBRHUniversal;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public class BlockSandbags extends Block implements ISBRHUniversal {
    public BlockSandbags(Material material) { super(material); }
    @Override protected BlockStateContainer createBlockState() { return UniversalBlockState.create(this); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(UniversalBlockState.META, meta & 15); }
    @Override public int getMetaFromState(IBlockState state) { return UniversalBlockState.metadata(state); }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) { return BlockFaceShape.UNDEFINED; }
    @Override public boolean renderAllFaces() { return false; }
    @Override public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        int mask = UniversalBlockState.solidNeighbors(world, pos);
        for(EnumFacing side : EnumFacing.HORIZONTALS) if(world.getBlockState(pos.offset(side)).getBlock() == this) mask |= 1 << side.getIndex();
        return UniversalBlockState.withConnections(state, mask);
    }
    private AxisAlignedBB bounds(int mask) {
        return new AxisAlignedBB(UniversalBlockState.connected(mask,EnumFacing.WEST)?0:.25, 0,
            UniversalBlockState.connected(mask,EnumFacing.NORTH)?0:.25,
            UniversalBlockState.connected(mask,EnumFacing.EAST)?1:.75, 1,
            UniversalBlockState.connected(mask,EnumFacing.SOUTH)?1:.75);
    }
    @Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return bounds(UniversalBlockState.connections(getExtendedState(state,world,pos)));
    }
    @Override public List<AxisAlignedBB> getRenderBoxes(IBlockState state, boolean inventory) {
        return Collections.singletonList(inventory ? new AxisAlignedBB(.125,0,.125,.875,1,.875) : bounds(UniversalBlockState.connections(state)));
    }
}
