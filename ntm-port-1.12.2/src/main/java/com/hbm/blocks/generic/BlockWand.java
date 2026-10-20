package com.hbm.blocks.generic;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/** Original non-colliding structure marker, with native state and model rendering. */
public class BlockWand extends Block {
    public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);
    public static final AxisAlignedBB BOUNDS = new AxisAlignedBB(1D/16D, 1D/16D, 1D/16D, 15D/16D, 15D/16D, 15D/16D);
    public final Block exportAs;

    public BlockWand(Block exportAs) {
        super(Material.GLASS);
        this.exportAs = exportAs;
        setDefaultState(blockState.getBaseState().withProperty(METADATA, 0));
    }
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, METADATA); }
    @Override public IBlockState getStateFromMeta(int metadata) { return getDefaultState().withProperty(METADATA, metadata & 15); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(METADATA); }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
    // Original pass 0 alpha-tested this binary-alpha texture. Native SOLID disables
    // alpha testing; CUTOUT_MIPPED retains alpha testing and the legacy terrain atlas
    // mipmap setting, with no blending and normal depth writes.
    @Override public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT_MIPPED; }
    @Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) { return BOUNDS; }
    @Nullable @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) { return NULL_AABB; }
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) { return BlockFaceShape.UNDEFINED; }
    @Override public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        // 1.7 supplied the neighbor position; native 1.12 supplies the owner's position.
        return world.getBlockState(pos.offset(side)).getBlock() != this;
    }
}
