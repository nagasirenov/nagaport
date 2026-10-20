package com.hbm.blocks.machine.rbmk;

import java.util.*;
import com.hbm.render.block.ISBRHUniversal;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import com.hbm.blocks.generic.UniversalBlockState;
import net.minecraft.block.BlockContainer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public abstract class RBMKMiniPanelBase extends BlockContainer implements ISBRHUniversal {
    public RBMKMiniPanelBase() { super(Material.IRON); }
    @Override protected BlockStateContainer createBlockState() { return UniversalBlockState.create(this); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(UniversalBlockState.META, meta & 15); }
    @Override public int getMetaFromState(IBlockState state) { return UniversalBlockState.metadata(state); }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) { return BlockFaceShape.UNDEFINED; }
    @Override public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) { return true; }
    @Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        int meta=getMetaFromState(state);
        return new AxisAlignedBB(meta==4?.25:0,0,meta==2?.25:0,meta==5?.75:1,1,meta==3?.75:1);
    }
    @Override public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        int yaw=MathHelper.floor(placer.rotationYaw*4F/360F+.5D)&3;
        world.setBlockState(pos,getStateFromMeta(new int[]{2,5,3,4}[yaw]),2);
    }
    @Override public List<AxisAlignedBB> getRenderBoxes(IBlockState state, boolean inventory) {
        return Collections.singletonList(inventory?new AxisAlignedBB(.25,0,0,1,1,1):getBoundingBox(state,null,BlockPos.ORIGIN));
    }
}
