package com.hbm.blocks.generic;

import com.hbm.blocks.IBlockMulti;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

/** Original four scaffold materials and four mounting orientations, stored in metadata. */
public class BlockScaffold extends Block implements IBlockMulti {
    public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);
    public BlockScaffold() {
        super(Material.IRON);
        setDefaultState(blockState.getBaseState().withProperty(METADATA, 0));
    }
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, METADATA); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(METADATA, meta & 15); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(METADATA); }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) { return BlockFaceShape.UNDEFINED; }
    @Override public int damageDropped(IBlockState state) { return rectify(getMetaFromState(state)); }
    @Override public int getSubCount() { return 4; }
    @Override public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        for(int i = 0; i < getSubCount(); i++) items.add(new ItemStack(this, 1, i));
    }
    @Override public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side,
            float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        // Retain the original two-stage placement: face first, then item color + orientation.
        return getStateFromMeta(side.getIndex());
    }
    @Override public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        EnumFacing placed = EnumFacing.byIndex(getMetaFromState(world.getBlockState(pos)));
        int meta = stack.getItemDamage();
        if(placed.getAxis() == EnumFacing.Axis.Y) {
            int rot = MathHelper.floor(placer.rotationYaw * 4F / 360F + .5D) & 3;
            meta += rot % 2 == 0 ? 0 : 8;
        } else if(placed.getAxis() == EnumFacing.Axis.Z) meta += 4;
        else meta += 12;
        world.setBlockState(pos, getStateFromMeta(meta), 2);
    }
    @Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        int meta = getMetaFromState(state);
        if(meta >= 12 || meta >= 4 && meta < 8) return new AxisAlignedBB(0, .125, 0, 1, .875, 1);
        if(meta >= 8) return new AxisAlignedBB(.125, 0, 0, .875, 1, 1);
        return new AxisAlignedBB(0, 0, .125, 1, 1, .875);
    }
    @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return getBoundingBox(state, world, pos);
    }
}
