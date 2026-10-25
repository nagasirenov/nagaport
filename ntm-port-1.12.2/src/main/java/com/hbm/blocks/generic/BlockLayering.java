package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.ZirnoxDestroyed;
import com.hbm.blocks.machine.rbmk.RBMKDebris;
import com.hbm.world.WorldAirRemoval;
import com.hbm.world.WorldBlockState;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockLayering extends Block {

	public BlockLayering withSoundType(net.minecraft.block.SoundType sound) {
		super.setSoundType(sound);
		return this;
	}


    // Keep the complete stored nibble. Geometry uses the low three bits, but
    // original replacement uses the unmasked value (including values 8..15).
    public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);

    public BlockLayering(Material mat) {
        super(mat);
        setDefaultState(blockState.getBaseState().withProperty(METADATA, 0));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, METADATA);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(METADATA, meta & 15);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(METADATA);
    }

    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return new AxisAlignedBB(0, 0, 0, 1, ((getMetaFromState(state) & 7) + 1) / 8.0D, 1);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        // Intentionally one layer lower than the selection box, including a
        // zero-height box at metadata zero, exactly as in the original block.
        return new AxisAlignedBB(0, 0, 0, 1, (getMetaFromState(state) & 7) / 8.0D, 1);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) {
        // Even eight visible layers were not a normal cube/solid support face.
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        BlockPos below = pos.down();
        IBlockState support = WorldBlockState.read(world, below.getX(), below.getY(), below.getZ());
        Block block = support.getBlock();
        if (block instanceof RBMKDebris || block instanceof ZirnoxDestroyed) return true;
        if (block == Blocks.ICE || block == Blocks.PACKED_ICE) return false;
        return block.isLeaves(support, world, below)
                || (block == this && (getMetaFromState(support) & 7) == 7)
                || (support.isOpaqueCube() && support.getMaterial().blocksMovement());
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbor, BlockPos neighborPos) {
        if (!canPlaceBlockAt(world, pos)) {
            // Support loss removes the layer without drops, as before.
            WorldAirRemoval.remove(world, pos.getX(), pos.getY(), pos.getZ());
        }
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity tile, ItemStack tool) {
        super.harvestBlock(world, player, pos, state, tile, tool);
        WorldAirRemoval.remove(world, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        // Native Block.getDrops tests Items.AIR, not the old null sentinel.
        return Items.AIR;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        // The original melting/light check was disabled.
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return side == EnumFacing.UP || super.shouldSideBeRendered(state, world, pos, side);
    }

    @Override
    public int quantityDropped(IBlockState state, int fortune, Random random) {
        return (getMetaFromState(state) & 7) + 1;
    }

    @Override
    public boolean isReplaceable(IBlockAccess world, BlockPos pos) {
        if (this == ModBlocks.leaves_layer || this == ModBlocks.oil_spill || this == ModBlocks.foam_layer) return true;
        // Decode the actual owner, not this block. IBlockAccess supplies native
        // states here; this is not a codec for WorldInAJar's arbitrary raw shorts.
        int meta = WorldBlockState.metadata(world.getBlockState(pos));
        return meta < 7 && material.isReplaceable();
    }
}
