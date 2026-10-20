package com.hbm.blocks.network;

import com.hbm.tileentity.network.TileEntityCableBaseNT;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BlockCable extends BlockContainer {
    public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);
    public BlockCable(Material material) {
        super(material);
        setDefaultState(blockState.getBaseState().withProperty(METADATA, 0));
    }
    @Override protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty<?>[] {METADATA}, new IUnlistedProperty<?>[] {ConnectionState.CONNECTIONS});
    }
    @Override public IBlockState getStateFromMeta(int metadata) { return getDefaultState().withProperty(METADATA, metadata & 15); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(METADATA); }
    @Override public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return ((IExtendedBlockState)state).withProperty(ConnectionState.CONNECTIONS, ConnectionState.energy(world, pos));
    }
    @Override public TileEntity createNewTileEntity(World world, int metadata) { return new TileEntityCableBaseNT(); }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
    @Override public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT_MIPPED; }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) { return BlockFaceShape.UNDEFINED; }
    @Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) { return bounds(ConnectionState.energy(world, pos)); }
    @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        // The original collision was one merged box, not a union of narrow arms.
        return bounds(ConnectionState.energy(world, pos));
    }
    public static AxisAlignedBB bounds(int mask) {
        double min = 5.5D / 16D, max = 10.5D / 16D;
        return new AxisAlignedBB((mask & ConnectionState.NEG_X) != 0 ? 0 : min,
                (mask & ConnectionState.NEG_Y) != 0 ? 0 : min, (mask & ConnectionState.NEG_Z) != 0 ? 0 : min,
                (mask & ConnectionState.POS_X) != 0 ? 1 : max, (mask & ConnectionState.POS_Y) != 0 ? 1 : max,
                (mask & ConnectionState.POS_Z) != 0 ? 1 : max);
    }
}
