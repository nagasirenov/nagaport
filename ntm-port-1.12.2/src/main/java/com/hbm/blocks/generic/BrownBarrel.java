package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.ModBlocks;

import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.world.feature.OilSpot;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;



public class BrownBarrel extends Block {

	Random rand = new Random();
	
	public BrownBarrel(Material p_i45386_1_) {
		super(p_i45386_1_);
		setDefaultState(blockState.getBaseState().withProperty(METADATA, 0));
	}

	@Override
	public void onExplosionDestroy(World p_149723_1_, BlockPos pos, Explosion p_149723_5_) {
		int p_149723_2_ = pos.getX(), p_149723_3_ = pos.getY(), p_149723_4_ = pos.getZ();
		if(!p_149723_1_.isRemote && this == ModBlocks.yellow_barrel) {
			explode(p_149723_1_, p_149723_2_, p_149723_3_, p_149723_4_);
		}
	}

	public void explode(World world, int x, int y, int z) {

		if(this == ModBlocks.oil_barrel)
			world.newExplosion((Entity) null, x + 0.5F, y + 0.5F, z + 0.5F, 2.5F, true, true);

	}
    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state);

		//world.playSoundEffect(x, y, z, "mob.zombie.woodbreak", 2.0F, 0.95F + world.rand.nextFloat() * 0.2F);

    }

    public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, METADATA); }
    @Override public IBlockState getStateFromMeta(int metadata) { return getDefaultState().withProperty(METADATA, metadata & 15); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(METADATA); }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
    @Override public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT_MIPPED; }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) { return BlockFaceShape.UNDEFINED; }
    private static final AxisAlignedBB BOUNDS = new AxisAlignedBB(.125, 0, .125, .875, 1, .875);
    @Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) { return BOUNDS; }
    @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) { return BOUNDS; }

	@Override
	public boolean canDropFromExplosion(Explosion p_149659_1_) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World p_149734_1_, BlockPos pos, Random p_149734_5_) {
		super.randomDisplayTick(state, p_149734_1_, pos, p_149734_5_);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		super.updateTick(world, pos, state, rand);

		if(this == ModBlocks.oil_barrel)
			OilSpot.generateOilSpot(world, x, z, 4, 10, false);

		world.scheduleUpdate(pos, this, this.tickRate(world));
	}

	@Override
	public int tickRate(World world) {
		return 20;
	}

	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		super.onBlockAdded(world, pos, state);
		world.scheduleUpdate(pos, this, this.tickRate(world));
	}

}
