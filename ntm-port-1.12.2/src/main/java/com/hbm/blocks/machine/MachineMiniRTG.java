package com.hbm.blocks.machine;

import com.hbm.tileentity.machine.TileEntityMachineMiniRTG;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import static com.hbm.blocks.machine.MachineRTG.METADATA;

public class MachineMiniRTG extends BlockContainer {

	public MachineMiniRTG(Material mat) {
		super(mat);
        setDefaultState(blockState.getBaseState().withProperty(METADATA, 0));
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityMachineMiniRTG();
	}

    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, METADATA); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(METADATA, meta & 15); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(METADATA); }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.ENTITYBLOCK_ANIMATED; }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) { return false; }
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) { return BlockFaceShape.UNDEFINED; }

}
