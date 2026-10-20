package com.hbm.blocks.generic;

import java.util.*;
import com.hbm.render.block.ISBRHUniversal;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import com.hbm.blocks.BlockEnumMulti;
import com.hbm.extprop.HbmPlayerProps;
import com.hbm.util.EnumUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class BlockWoodStructure extends BlockEnumMulti implements ISBRHUniversal {
    public BlockWoodStructure(Material material) { super(material,EnumWoodStructure.class,true,false); }
    @Override public String getTextureName() { return "hbm:wood_barrier"; }
    public enum EnumWoodStructure { ROOF, SCAFFOLD, CEILING }
    @Override protected BlockStateContainer createBlockState() { return UniversalBlockState.create(this); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(UniversalBlockState.META, meta & 15); }
    @Override public int getMetaFromState(IBlockState state) { return UniversalBlockState.metadata(state); }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) { return BlockFaceShape.UNDEFINED; }
    private EnumWoodStructure type(IBlockState state) { return EnumUtil.grabEnumSafely(EnumWoodStructure.class,getMetaFromState(state)); }
    @Override public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) { return true; }
    @Override public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) { return type(world.getBlockState(pos)) == EnumWoodStructure.SCAFFOLD && side == EnumFacing.UP; }
    @Override public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        int mask=0,meta=getMetaFromState(state);
        for(EnumFacing side:EnumFacing.values()) {
            IBlockState neighbor=world.getBlockState(pos.offset(side));
            if(neighbor.getBlock()==this && getMetaFromState(neighbor)==meta) mask |= 1 << side.getIndex();
        }
        return UniversalBlockState.withConnections(state,mask);
    }
    @Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        switch(type(state)) {
        case ROOF: return new AxisAlignedBB(0,0,0,1,.1875,1);
        case SCAFFOLD: return new AxisAlignedBB(.0625,0,.0625,.9375,1,.9375);
        case CEILING: return new AxisAlignedBB(0,.875,0,1,1,1);
        default: throw new AssertionError(type(state));
        }
    }
    @Override public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {
        return (type(state)==EnumWoodStructure.SCAFFOLD?FULL_BLOCK_AABB:getBoundingBox(state,world,pos)).offset(pos);
    }
    @Override public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        if(entity instanceof EntityPlayer && type(world.getBlockState(pos))==EnumWoodStructure.SCAFFOLD) HbmPlayerProps.getData((EntityPlayer)entity).isOnLadder=true;
    }
    @Override public double getInventoryYOffset(IBlockState state) { return type(state)==EnumWoodStructure.ROOF?.125:type(state)==EnumWoodStructure.CEILING?.625:0; }
    @Override public List<AxisAlignedBB> getRenderBoxes(IBlockState state, boolean inventory) {
        List<AxisAlignedBB> boxes=new ArrayList<>();
        AxisAlignedBB bounds=FULL_BLOCK_AABB;
        EnumWoodStructure type=type(state);
        int mask=UniversalBlockState.connections(state);
        if(inventory) {
		if(type == type.ROOF) {
			bounds = new AxisAlignedBB(0D, 0D, 0D, 0.125D, 0.125D, 1D); boxes.add(bounds);
			bounds = new AxisAlignedBB(0.875D, 0D, 0D, 1D, 0.125D, 1D); boxes.add(bounds);
			bounds = new AxisAlignedBB(0D, 0.125D, 0.0625D, 1D, 0.1875D, 0.4375D); boxes.add(bounds);
			bounds = new AxisAlignedBB(0D, 0.125D, 0.5625D, 1D, 0.1875D, 0.9375D); boxes.add(bounds);
		}
		if(type == type.SCAFFOLD) {
			bounds = new AxisAlignedBB(0.0625D, 0D, 0.0625D, 0.1875D, 0.875D, 0.1875D); boxes.add(bounds);
			bounds = new AxisAlignedBB(0.8125D, 0D, 0.0625D, 0.9375D, 0.875D, 0.1875D); boxes.add(bounds);
			bounds = new AxisAlignedBB(0.0625D, 0D, 0.8125D, 0.1875D, 0.875D, 0.9375D); boxes.add(bounds);
			bounds = new AxisAlignedBB(0.8125D, 0D, 0.8125D, 0.9375D, 0.875D, 0.9375D); boxes.add(bounds);
			bounds = new AxisAlignedBB(0D, 0.125D, 0D, 0.0625D, 0.375D, 1D); boxes.add(bounds);
			bounds = new AxisAlignedBB(0.9375D, 0.125D, 0D, 1D, 0.375D, 1D); boxes.add(bounds);
			bounds = new AxisAlignedBB(0D, 0.5D, 0D, 1D, 0.75D, 0.0625D); boxes.add(bounds);
			bounds = new AxisAlignedBB(0D, 0.5D, 0.9375D, 1D, 0.75D, 1D); boxes.add(bounds);
			bounds = new AxisAlignedBB(0D, 0.875D, 0D, 1D, 1D, 1D); boxes.add(bounds);
		}
		if(type == type.CEILING) {
			bounds = new AxisAlignedBB(0D, 0.0625D, 0D, 0.125D, 0.125D, 1D); boxes.add(bounds);
			bounds = new AxisAlignedBB(0.875D, 0.0625D, 0D, 1D, 0.125D, 1D); boxes.add(bounds);
			bounds = new AxisAlignedBB(0D, 0.125D, 0.0625D, 1D, 0.1875D, 0.4375D); boxes.add(bounds);
			bounds = new AxisAlignedBB(0D, 0.125D, 0.5625D, 1D, 0.1875D, 0.9375D); boxes.add(bounds);
		}
		
        } else {
            // Deliberately preserve unconditional draws after the original five
            // unbraced ifs, including duplicates of the previous bounds.
		if(type == type.ROOF) {
			boolean nx = UniversalBlockState.connected(mask,EnumFacing.WEST);
			boolean px = UniversalBlockState.connected(mask,EnumFacing.EAST);
			bounds = new AxisAlignedBB(0D, 0D, 0D, 0.125D, 0.125D, 1D); boxes.add(bounds);
			bounds = new AxisAlignedBB(0.875D, 0D, 0D, 1D, 0.125D, 1D); boxes.add(bounds);
			bounds = new AxisAlignedBB(nx ? 0D : 0.0625D, 0.125D, 0.0625D, px ? 1D : 0.9375D, 0.1875D, 0.4375D); boxes.add(bounds);
			bounds = new AxisAlignedBB(nx ? 0D : 0.0625D, 0.125D, 0.5625D, px ? 1D : 0.9375D, 0.1875D, 0.9375D); boxes.add(bounds);
		}
		if(type == type.SCAFFOLD) {
			boolean py = UniversalBlockState.connected(mask,EnumFacing.UP);
			boolean nx = UniversalBlockState.connected(mask,EnumFacing.WEST);
			boolean nz = UniversalBlockState.connected(mask,EnumFacing.NORTH);
			boolean px = UniversalBlockState.connected(mask,EnumFacing.EAST);
			boolean pz = UniversalBlockState.connected(mask,EnumFacing.SOUTH);
			bounds = new AxisAlignedBB(0.0625D, 0D, 0.0625D, 0.1875D, py ? 1D : 0.875D, 0.1875D); boxes.add(bounds);
			bounds = new AxisAlignedBB(0.8125D, 0D, 0.0625D, 0.9375D, py ? 1D : 0.875D, 0.1875D); boxes.add(bounds);
			bounds = new AxisAlignedBB(0.0625D, 0D, 0.8125D, 0.1875D, py ? 1D : 0.875D, 0.9375D); boxes.add(bounds);
			bounds = new AxisAlignedBB(0.8125D, 0D, 0.8125D, 0.9375D, py ? 1D : 0.875D, 0.9375D); boxes.add(bounds);
			
			if(!nx) bounds = new AxisAlignedBB(0D, 0.125D, 0D, 0.0625D, 0.375D, 1D); boxes.add(bounds);
			if(!px) bounds = new AxisAlignedBB(0.9375D, 0.125D, 0D, 1D, 0.375D, 1D); boxes.add(bounds);
			if(!nz) bounds = new AxisAlignedBB(0D, 0.5D, 0D, 1D, 0.75D, 0.0625D); boxes.add(bounds);
			if(!pz) bounds = new AxisAlignedBB(0D, 0.5D, 0.9375D, 1D, 0.75D, 1D); boxes.add(bounds);
			
			if(!py) bounds = new AxisAlignedBB(0D, 0.875D, 0D, 1D, 1D, 1D); boxes.add(bounds);
		}
		if(type == type.CEILING) {
			bounds = new AxisAlignedBB(0D, 0.875D, 0D, 0.125D, 0.9375D, 1D); boxes.add(bounds);
			bounds = new AxisAlignedBB(0.875D, 0.875D, 0D, 1D, 0.9375D, 1D); boxes.add(bounds);
			bounds = new AxisAlignedBB(0D, 0.9375D, 0.0625D, 1D, 1D, 0.4375D); boxes.add(bounds);
			bounds = new AxisAlignedBB(0D, 0.9375D, 0.5625D, 1D, 1D, 0.9375D); boxes.add(bounds);
		}
		
        }
        return boxes;
    }
}
