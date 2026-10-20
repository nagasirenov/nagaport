package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.bomb.BlockDetonatable;

import com.hbm.entity.item.EntityTNTPrimedBase;
import com.hbm.explosion.ExplosionThermo;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
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


public class RedBarrel extends BlockDetonatable {

	// Flammable barrels also explode when shot
	public RedBarrel(Material material, boolean flammable) {
		super(material, flammable ? 2 : 0,  flammable ? 15 : 0, 100, true, flammable);
		setDefaultState(blockState.getBaseState().withProperty(METADATA, 0));
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
	public void explodeEntity(World world, double x, double y, double z, EntityTNTPrimedBase entity) {
		int ix = MathHelper.floor(x), iy = MathHelper.floor(y), iz = MathHelper.floor(z);

		if(this == ModBlocks.red_barrel || this == ModBlocks.pink_barrel) {
			world.newExplosion(entity, x, y, z, 2.5F, true, true);
		} else if(this == ModBlocks.lox_barrel) {
			world.newExplosion(entity, x, y, z, 1F, false, false);

			ExplosionThermo.freeze(world, ix, iy, iz, 7);
		} else if(this == ModBlocks.taint_barrel) {
			world.newExplosion(entity, x, y, z, 1F, false, false);

			Random rand = world.rand;
			for(int i = 0; i < 100; i++) {
				int a = rand.nextInt(9) - 4 + ix;
				int b = rand.nextInt(9) - 4 + iy;
				int c = rand.nextInt(9) - 4 + iz;
				BlockPos pos = new BlockPos(a, b, c);
				IBlockState state = world.getBlockState(pos);
				Block block = state.getBlock();
				if(block.isNormalCube(state) && !block.isAir(state, world, pos)) {
					world.setBlockState(pos, ModBlocks.taint.getStateFromMeta(rand.nextInt(3) + 4), 2);
				}
			}
		}
	}

}
