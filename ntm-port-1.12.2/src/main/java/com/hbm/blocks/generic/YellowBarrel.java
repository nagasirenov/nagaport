package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.bomb.BlockDetonatable;

import com.hbm.entity.item.EntityTNTPrimedBase;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.handler.radiation.ChunkRadiationManager;

import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
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


public class YellowBarrel extends BlockDetonatable {

	Random rand = new Random();

	public YellowBarrel(Material material) {
		super(material, 0, 0, 100, true, false);
		setDefaultState(blockState.getBaseState().withProperty(METADATA, 0));
	}

	@Override
	public void onExplosionDestroy(World world, BlockPos pos, Explosion explosion) {
		if (this != ModBlocks.yellow_barrel) return;
		super.onExplosionDestroy(world, pos, explosion);
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
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		super.randomDisplayTick(state, world, pos, rand);

		world.spawnParticle(EnumParticleTypes.TOWN_AURA, x + rand.nextFloat() * 0.5F + 0.25F, y + 1.1F, z + rand.nextFloat() * 0.5F + 0.25F, 0.0D, 0.0D, 0.0D);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		super.updateTick(world, pos, state, rand);

		if(this == ModBlocks.yellow_barrel)
			ChunkRadiationManager.proxy.incrementRad(world, x, y, z, 5.0F);
		else
			ChunkRadiationManager.proxy.incrementRad(world, x, y, z, 0.5F);

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

	@Override
	public void explodeEntity(World world, double x, double y, double z, EntityTNTPrimedBase entity) {
		int ix = MathHelper.floor(x), iy = MathHelper.floor(y), iz = MathHelper.floor(z);

		if(rand.nextInt(3) == 0) {
			world.setBlockState(new BlockPos(ix, iy, iz), ModBlocks.toxic_block.getDefaultState());
		} else {
			world.createExplosion(entity, x, y, z, 12.0F, true);
		}
		ExplosionNukeGeneric.waste(world, ix, iy, iz, 35);

		for(int i = -5; i <= 5; i++) {
			for(int j = -5; j <= 5; j++) {
				for(int k = -5; k <= 5; k++) {
					
					if(world.rand.nextInt(5) == 0 && world.getBlockState(new BlockPos(ix + i, iy + j, iz + k)).getBlock() == Blocks.AIR)
						world.setBlockState(new BlockPos(ix + i, iy + j, iz + k), ModBlocks.gas_radon_dense.getDefaultState());
				}
			}
		}
		ChunkRadiationManager.proxy.incrementRad(world, ix, iy, iz, 35);
	}

}
