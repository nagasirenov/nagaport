package com.hbm.blocks.machine;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/** Native common foundry block declaration. Material transfer remains in its original owners. */
public abstract class FoundryBlock extends BlockContainer {
    public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);
    protected FoundryBlock() {
        super(Material.ROCK);
        setDefaultState(blockState.getBaseState().withProperty(METADATA, 0));
    }
    @Override protected BlockStateContainer createBlockState() { return FoundryModelState.create(this); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(METADATA, meta & 15); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(METADATA); }
    @Override public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) { return FoundryModelState.extend(state, world, pos); }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
    @Override public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT_MIPPED; }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) { return true; }
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) {
        return isSideSolid(state, world, pos, side) ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }
    @Override public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) { return false; }
    /** Native equivalent of the original tile description-packet/render notification. */
    public static void notifyChange(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 3);
    }
}
