package com.hbm.blocks.generic;

import com.hbm.world.gen.nbt.INBTBlockTransformable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/** Native metadata and bottom-cap state for the original climbable, noncolliding chain. */
public class BlockChain extends Block implements INBTBlockTransformable {
    public static final PropertyInteger META = PropertyInteger.create("meta", 0, 15);
    public static final PropertyBool END = PropertyBool.create("end");
    public BlockChain(Material material) {
        super(material);
        setDefaultState(blockState.getBaseState().withProperty(META, 0).withProperty(END, true));
    }
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, META, END); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(META, meta & 15); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(META); }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
    public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT_MIPPED; }
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) { return BlockFaceShape.UNDEFINED; }
    @Override public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) { return true; }
    private boolean solid(IBlockAccess world, BlockPos pos, EnumFacing side) { return world.isSideSolid(pos, side, false); }
    private boolean matching(IBlockAccess world, BlockPos pos, int meta) {
        IBlockState state = world.getBlockState(pos);
        return state.getBlock() == this && getMetaFromState(state) == meta;
    }
    @Override public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.withProperty(END, !(solid(world, pos.down(), EnumFacing.UP) || matching(world, pos.down(), getMetaFromState(state))));
    }
    @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) { return NULL_AABB; }
    // The original unset metadata (1, 6..15) leaves the last selection bounds
    // untouched. Keep that observable fallback rather than silently invent bounds.
    private AxisAlignedBB selectionBounds = FULL_BLOCK_AABB;
    @Override public synchronized AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        int meta = getMetaFromState(state);
        switch(meta) {
            case 0: selectionBounds = new AxisAlignedBB(.375, 0, .375, .625, 1, .625); break;
            case 2: selectionBounds = new AxisAlignedBB(.375, 0, .875, .625, 1, 1); break;
            case 3: selectionBounds = new AxisAlignedBB(.375, 0, 0, .625, 1, .125); break;
            case 4: selectionBounds = new AxisAlignedBB(.875, 0, .375, 1, 1, .625); break;
            case 5: selectionBounds = new AxisAlignedBB(0, 0, .375, .125, 1, .625); break;
            default: break;
        }
        if(getActualState(state, world, pos).getValue(END))
            selectionBounds = new AxisAlignedBB(selectionBounds.minX, .25, selectionBounds.minZ,
                    selectionBounds.maxX, selectionBounds.maxY, selectionBounds.maxZ);
        return selectionBounds;
    }

    @Override public boolean canPlaceBlockAt(World world, BlockPos pos) {
        if(solid(world, pos.up(), EnumFacing.DOWN) || world.getBlockState(pos.up()).getBlock() == this) return true;
        return solid(world, pos.west(), EnumFacing.EAST) || solid(world, pos.east(), EnumFacing.WEST)
                || solid(world, pos.north(), EnumFacing.SOUTH) || solid(world, pos.south(), EnumFacing.NORTH);
    }
    @Override public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side,
            float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        int result = meta;
        if(side == EnumFacing.NORTH && solid(world, pos.south(), EnumFacing.NORTH)) result = 2;
        if(side == EnumFacing.SOUTH && solid(world, pos.north(), EnumFacing.SOUTH)) result = 3;
        if(side == EnumFacing.WEST && solid(world, pos.east(), EnumFacing.WEST)) result = 4;
        if(side == EnumFacing.EAST && solid(world, pos.west(), EnumFacing.EAST)) result = 5;
        if(result == 0) {
            IBlockState above = world.getBlockState(pos.up());
            if(above.getBlock() == this) return getStateFromMeta(getMetaFromState(above));
            if(solid(world, pos.up(), EnumFacing.DOWN)) return getStateFromMeta(0);
        }
        if(result == 0) {
            if(solid(world, pos.south(), EnumFacing.NORTH)) result = 2;
            if(solid(world, pos.north(), EnumFacing.SOUTH)) result = 3;
            if(solid(world, pos.east(), EnumFacing.WEST)) result = 4;
            if(solid(world, pos.west(), EnumFacing.EAST)) result = 5;
        }
        return getStateFromMeta(result);
    }
    @Override public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        int meta = getMetaFromState(state);
        if(matching(world, pos.up(), meta) || meta == 0 && solid(world, pos.up(), EnumFacing.DOWN)) {
            super.neighborChanged(state, world, pos, block, fromPos); return;
        }
        boolean supported = meta == 2 && solid(world, pos.south(), EnumFacing.NORTH)
                || meta == 3 && solid(world, pos.north(), EnumFacing.SOUTH)
                || meta == 4 && solid(world, pos.east(), EnumFacing.WEST)
                || meta == 5 && solid(world, pos.west(), EnumFacing.EAST);
        if(!supported) world.destroyBlock(pos, true);
        super.neighborChanged(state, world, pos, block, fromPos);
    }
    @Override public int transformMeta(int meta, int rotation) {
        if(rotation == 0 || meta == 0 || meta == 1) return meta;
        return INBTBlockTransformable.transformMetaDeco(meta, rotation);
    }
}
