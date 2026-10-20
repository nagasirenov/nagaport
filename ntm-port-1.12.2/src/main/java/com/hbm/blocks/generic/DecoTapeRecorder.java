package com.hbm.blocks.generic;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import com.hbm.world.gen.nbt.INBTBlockTransformable;
public class DecoTapeRecorder extends BlockContainer implements INBTBlockTransformable {
    public DecoTapeRecorder(Material material) { super(material); setDefaultState(blockState.getBaseState().withProperty(METADATA, 0)); }

    public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, METADATA); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(METADATA, meta & 15); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(METADATA); }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) { return BlockFaceShape.UNDEFINED; }
    @Override public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
        int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        int[] orientation = {2, 5, 3, 4};
        world.setBlockState(pos, getStateFromMeta(orientation[i]), 2);
    }
    @Override public TileEntity createNewTileEntity(World world, int metadata) { return null; }
    @Override public int transformMeta(int meta, int coordBaseMode) { return INBTBlockTransformable.transformMetaDeco(meta, coordBaseMode); }
}
