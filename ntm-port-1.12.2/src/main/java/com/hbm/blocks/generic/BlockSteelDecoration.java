package com.hbm.blocks.generic;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import api.hbm.block.IToolable;
import com.hbm.world.gen.nbt.INBTBlockTransformable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.properties.IProperty;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/** The two non-tile wall/corner branches of DecoBlock, not its unrelated TESR owners. */
public final class BlockSteelDecoration extends Block implements IToolable, INBTBlockTransformable {
    public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);
    public static final IUnlistedProperty<AxisAlignedBB> WALL_BOUNDS = new IUnlistedProperty<AxisAlignedBB>() {
        @Override public String getName() { return "steel_wall_bounds"; }
        @Override public boolean isValid(AxisAlignedBB value) { return value != null; }
        @Override public Class<AxisAlignedBB> getType() { return AxisAlignedBB.class; }
        @Override public String valueToString(AxisAlignedBB value) { return value.toString(); }
    };
    private final boolean corner;
    // DecoBlock retained its last setBlockBounds when an invalid orientation was queried.
    // Keep that behavior without mutating any shared render geometry or listed state.
    private volatile AxisAlignedBB lastWallBounds = FULL_BLOCK_AABB;
    public BlockSteelDecoration(Material material, boolean corner) {
        super(material);
        this.corner = corner;
        setDefaultState(blockState.getBaseState().withProperty(METADATA, 0));
    }
    public boolean isCorner() { return corner; }
    @Override protected BlockStateContainer createBlockState() { return new ExtendedBlockState(this, new IProperty<?>[] {METADATA}, new IUnlistedProperty<?>[] {WALL_BOUNDS}); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(METADATA, meta & 15); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(METADATA); }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) { return true; }
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) { return BlockFaceShape.UNDEFINED; }
    @Override public Item getItemDropped(IBlockState state, Random random, int fortune) { return Item.getItemFromBlock(this); }
    @Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return corner ? FULL_BLOCK_AABB : rememberWallBounds(getMetaFromState(state));
    }
    @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return getBoundingBox(state, world, pos);
    }
    @Override public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB query,
            List<AxisAlignedBB> boxes, @Nullable Entity entity, boolean actualState) {
        if(corner) for(AxisAlignedBB box : cornerBounds(getMetaFromState(state))) addCollisionBoxToList(pos, query, boxes, box);
        else addCollisionBoxToList(pos, query, boxes, rememberWallBounds(getMetaFromState(state)));
    }
    @Override public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return ((IExtendedBlockState)state).withProperty(WALL_BOUNDS, corner ? FULL_BLOCK_AABB : rememberWallBounds(getMetaFromState(state)));
    }
    private AxisAlignedBB rememberWallBounds(int metadata) {
        if(metadata >= 2 && metadata <= 5) lastWallBounds = wallBounds(metadata);
        return lastWallBounds;
    }
    public static AxisAlignedBB modelBounds(IBlockState state) {
        if(!(state instanceof IExtendedBlockState)) throw new IllegalArgumentException("Wall model requires its bounds snapshot");
        AxisAlignedBB value = ((IExtendedBlockState)state).getValue(WALL_BOUNDS);
        if(value == null) throw new IllegalArgumentException("Wall model requires its bounds snapshot");
        return value;
    }
    @Override public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side,
            float hitX, float hitY, float hitZ, ToolType tool) {
        if(tool != ToolType.SCREWDRIVER) return false;
        BlockPos pos = new BlockPos(x, y, z);
        int meta = getMetaFromState(world.getBlockState(pos));
        int next = rotateMetadata(meta, player.isSneaking());
        if(next != meta) world.setBlockState(pos, getStateFromMeta(next), 3);
        return true;
    }
    public static int rotateMetadata(int metadata, boolean sneaking) {
        if(!sneaking) {
            switch(metadata) { case 3: return 4; case 4: return 2; case 2: return 5; case 5: return 3; default: return metadata; }
        }
        switch(metadata) { case 3: return 5; case 4: return 3; case 2: return 4; case 5: return 2; default: return metadata; }
    }
    @Override public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
        int facing = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        int[] metadata = {3, 4, 2, 5};
        world.setBlockState(pos, getStateFromMeta(metadata[facing]), 2);
    }
    @Override public int transformMeta(int meta, int coordBaseMode) { return INBTBlockTransformable.transformMetaDeco(meta, coordBaseMode); }
    public static AxisAlignedBB wallBounds(int metadata) {
        switch(metadata) {
        case 2: return new AxisAlignedBB(0, 0, .875, 1, 1, 1);
        case 3: return new AxisAlignedBB(0, 0, 0, 1, 1, .125);
        case 4: return new AxisAlignedBB(.875, 0, 0, 1, 1, 1);
        case 5: return new AxisAlignedBB(0, 0, 0, .125, 1, 1);
        default: return FULL_BLOCK_AABB;
        }
    }
    public static List<AxisAlignedBB> cornerBounds(int metadata) {
        switch(metadata) {
        case 2: return Arrays.asList(new AxisAlignedBB(.25, 0, .875, 1, 1, 1),
                new AxisAlignedBB(0, 0, .75, .25, 1, 1), new AxisAlignedBB(0, 0, 0, .125, 1, .75));
        case 3: return Arrays.asList(new AxisAlignedBB(0, 0, 0, .75, 1, .125),
                new AxisAlignedBB(.75, 0, 0, 1, 1, .25), new AxisAlignedBB(.875, 0, .25, 1, 1, 1));
        case 4: return Arrays.asList(new AxisAlignedBB(.875, 0, 0, 1, 1, .75),
                new AxisAlignedBB(.75, 0, .75, 1, 1, 1), new AxisAlignedBB(0, 0, .875, .75, 1, 1));
        case 5: return Arrays.asList(new AxisAlignedBB(0, 0, .25, .125, 1, 1),
                new AxisAlignedBB(0, 0, 0, .25, 1, .25), new AxisAlignedBB(.25, 0, 0, 1, 1, .125));
        default: return Collections.emptyList();
        }
    }
}
