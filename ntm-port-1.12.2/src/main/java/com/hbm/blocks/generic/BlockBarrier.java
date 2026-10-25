package com.hbm.blocks.generic;

import java.util.*;
import com.hbm.render.block.ISBRHUniversal;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public class BlockBarrier extends Block implements ISBRHUniversal {
	@Override
	public net.minecraft.block.Block setSoundType(net.minecraft.block.SoundType sound) {
		return super.setSoundType(sound);
	}

    public BlockBarrier(Material material) { super(material); }
    @Override protected BlockStateContainer createBlockState() { return UniversalBlockState.create(this); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(UniversalBlockState.META, meta & 15); }
    @Override public int getMetaFromState(IBlockState state) { return UniversalBlockState.metadata(state); }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) { return BlockFaceShape.UNDEFINED; }
    @Override public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) { return true; }
    @Override public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float x, float y, float z, int meta, EntityLivingBase placer) {
        return getStateFromMeta(side.getAxis().isHorizontal() ? side.getIndex() : meta);
    }
    @Override public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        if(getMetaFromState(world.getBlockState(pos)) != 0) return;
        int yaw = MathHelper.floor(placer.rotationYaw * 4F / 360F + .5D) & 3;
        world.setBlockState(pos, getStateFromMeta(new int[]{2,5,3,4}[yaw]), 2);
    }
    @Override public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        int meta = getMetaFromState(state), mask = UniversalBlockState.solidNeighbors(world,pos);
        if(meta >= 2 && meta <= 5) mask |= 1 << EnumFacing.byIndex(meta).getOpposite().getIndex();
        return UniversalBlockState.withConnections(state,mask);
    }
    @Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        switch(getMetaFromState(state)) {
        case 5: return new AxisAlignedBB(0,0,0,.125,1,1);
        case 3: return new AxisAlignedBB(0,0,0,1,1,.125);
        case 4: return new AxisAlignedBB(.875,0,0,1,1,1);
        case 2: return new AxisAlignedBB(0,0,.875,1,1,1);
        default: return FULL_BLOCK_AABB;
        }
    }
    @Override public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> result, Entity entity, boolean actualState) {
        int mask=UniversalBlockState.connections(getExtendedState(state,world,pos));
        if(UniversalBlockState.connected(mask,EnumFacing.WEST)) addCollisionBoxToList(pos,entityBox,result,new AxisAlignedBB(0,0,0,.125,1,1));
        if(UniversalBlockState.connected(mask,EnumFacing.NORTH)) addCollisionBoxToList(pos,entityBox,result,new AxisAlignedBB(0,0,0,1,1,.125));
        if(UniversalBlockState.connected(mask,EnumFacing.EAST)) addCollisionBoxToList(pos,entityBox,result,new AxisAlignedBB(.875,0,0,1,1,1));
        if(UniversalBlockState.connected(mask,EnumFacing.SOUTH)) addCollisionBoxToList(pos,entityBox,result,new AxisAlignedBB(0,0,.875,1,1,1));
    }
    @Override public List<AxisAlignedBB> getRenderBoxes(IBlockState state, boolean inventory) {
        List<AxisAlignedBB> boxes=new ArrayList<>();
        if(inventory) {
            boxes.add(new AxisAlignedBB(.4375,0,.4375,.5625,1,.5625));
            boxes.add(new AxisAlignedBB(.5,.0625,0,.5625,.4725,1));
            boxes.add(new AxisAlignedBB(.5,.5625,0,.5625,.9375,1));
            return boxes;
        }
        int mask=UniversalBlockState.connections(state);
        boolean negX=UniversalBlockState.connected(mask,EnumFacing.WEST), negZ=UniversalBlockState.connected(mask,EnumFacing.NORTH);
        boolean posX=UniversalBlockState.connected(mask,EnumFacing.EAST), posZ=UniversalBlockState.connected(mask,EnumFacing.SOUTH);
        boolean posY=UniversalBlockState.connected(mask,EnumFacing.UP);
		if(negX) {
			boxes.add(new AxisAlignedBB(0D, 0D, 0.4375D, 0.125D, 1D, 0.5625D));
			boxes.add(new AxisAlignedBB(0D, 0.0625D, negZ ? 0.125D : 0D, 0.0625D, 0.4375D, posZ ? 0.875D : 1D));
			boxes.add(new AxisAlignedBB(0D, 0.5625D, negZ ? 0.125D : 0D, 0.0625D, 0.9375D, posZ ? 0.875D : 1D));
		}
		if(negZ) {
			boxes.add(new AxisAlignedBB(0.4375D, 0D, 0D, 0.5625D, 1D, 0.125D));
			boxes.add(new AxisAlignedBB(negX ? 0.125D : 0D, 0.0625D, 0D, posX ? 0.875D : 1D, 0.4375D, 0.0625D));
			boxes.add(new AxisAlignedBB(negX ? 0.125D : 0D, 0.5625D, 0D, posX ? 0.875D : 1D, 0.9375D, 0.0625D));
		}
		if(posX) {
			boxes.add(new AxisAlignedBB(0.875D, 0D, 0.4375D, 1D, 1D, 0.5625D));
			boxes.add(new AxisAlignedBB(0.9375D, 0.0625D, negZ ? 0.125D : 0D, 1D, 0.4375D, posZ ? 0.875D : 1D));
			boxes.add(new AxisAlignedBB(0.9375D, 0.5625D, negZ ? 0.125D : 0D, 1D, 0.9375D, posZ ? 0.875D : 1D));
		}
		if(posZ) {
			boxes.add(new AxisAlignedBB(0.4375D, 0D, 0.875D, 0.5625D, 1D, 1D));
			boxes.add(new AxisAlignedBB(negX ? 0.125D : 0D, 0.0625D, 0.9375D, posX ? 0.875D : 1D, 0.4375D, 1D));
			boxes.add(new AxisAlignedBB(negX ? 0.125D : 0D, 0.5625D, 0.9375D, posX ? 0.875D : 1D, 0.9375D, 1D));
		}
		if(posY) {
			boxes.add(new AxisAlignedBB(0D, 0.875D, 0D, 0.125D, 0.9375D, 1D));
			boxes.add(new AxisAlignedBB(0.875D, 0.875D, 0D, 1D, 0.9375D, 1D));
			boxes.add(new AxisAlignedBB(0D, 0.9375D, 0.0625D, 1D, 1D, 0.4375D));
			boxes.add(new AxisAlignedBB(0D, 0.9375D, 0.5625D, 1D, 1D, 0.9375D));
		}

        return boxes;
    }
}
