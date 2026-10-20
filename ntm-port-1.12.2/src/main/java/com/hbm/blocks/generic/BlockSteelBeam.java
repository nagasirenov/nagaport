package com.hbm.blocks.generic;

import api.hbm.block.IToolable;
import com.hbm.world.gen.nbt.INBTBlockTransformable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import java.util.Random;

/** The steel-beam branch of DecoBlock, isolated from its still-distinct AO/TESR owners. */
public final class BlockSteelBeam extends Block implements IToolable, INBTBlockTransformable {
    public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);
    private static final AxisAlignedBB BOUNDS = new AxisAlignedBB(7 / 16D, 0, 7 / 16D, 9 / 16D, 1, 9 / 16D);
    public BlockSteelBeam(Material material) {
        super(material);
        setDefaultState(blockState.getBaseState().withProperty(METADATA, 0));
    }
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, METADATA); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(METADATA, meta & 15); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(METADATA); }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
    @Override public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT_MIPPED; }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) { return true; }
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) { return BlockFaceShape.UNDEFINED; }
    @Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) { return BOUNDS; }
    @Override public Item getItemDropped(IBlockState state, Random rand, int fortune) { return Item.getItemFromBlock(this); }
    @Override public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float hitX, float hitY, float hitZ, ToolType tool) {
        // Original DecoBlock accepts screwdriver rotation only for wall/corner, never beam.
        return false;
    }
    @Override public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
        int facing = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        int[] metadata = {3, 4, 2, 5};
        world.setBlockState(pos, getStateFromMeta(metadata[facing]), 2);
    }
    @Override public int transformMeta(int meta, int coordBaseMode) {
        return INBTBlockTransformable.transformMetaDeco(meta, coordBaseMode);
    }
}
