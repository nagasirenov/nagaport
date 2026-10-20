package com.hbm.blocks.machine;

import com.hbm.tileentity.machine.TileEntitySolarMirror;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/** Original single-block mirror, with a tile-backed native model snapshot. */
public class SolarMirror extends BlockContainer {
    public SolarMirror(Material material) { super(material); }
    @Override public TileEntity createNewTileEntity(World world, int metadata) { return new TileEntitySolarMirror(); }
    @Override protected BlockStateContainer createBlockState() { return SolarMirrorModelState.create(this); }
    @Override public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return SolarMirrorModelState.extend(state, world, pos);
    }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) {
        return BlockFaceShape.UNDEFINED;
    }
}
