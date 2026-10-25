package com.hbm.blocks.gas;

import java.util.Random;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.util.ArmorRegistry;
import com.hbm.util.ArmorRegistry.HazardClass;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import com.hbm.lib.ForgeDirection;

public class BlockGasAsbestos extends BlockGasBase {

	public BlockGasAsbestos() {
		super(0.6F, 0.6F, 0.5F);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		super.randomDisplayTick(state, world, pos, rand);
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();

		if(world.rand.nextInt(5) == 0)
			world.spawnParticle(net.minecraft.util.EnumParticleTypes.TOWN_AURA, x + rand.nextFloat(), y + rand.nextFloat(), z + rand.nextFloat(), 0.0D, 0.0D, 0.0D);
	}


		@Override
	public void onEntityCollision(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, net.minecraft.entity.Entity entityIn) {
		onEntityCollidedWithBlock(worldIn, pos.getX(), pos.getY(), pos.getZ(), entityIn);
	}

public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {

		if(entity instanceof EntityLivingBase) {

			EntityLivingBase living = (EntityLivingBase) entity;

			if(!ArmorRegistry.hasProtection(living, 3, HazardClass.PARTICLE_FINE))
				HbmLivingProps.incrementAsbestos(living, 1);
		}
	}

	@Override
	public ForgeDirection getFirstDirection(World world, int x, int y, int z) {

		if(world.rand.nextInt(5) == 0)
			return ForgeDirection.DOWN;

		return ForgeDirection.getOrientation(world.rand.nextInt(6));
	}

	@Override
	public ForgeDirection getSecondDirection(World world, int x, int y, int z) {
		return this.randomHorizontal(world);
	}


		@Override
	public void updateTick(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, java.util.Random rand) {
		updateTick(worldIn, pos.getX(), pos.getY(), pos.getZ(), rand);
	}

public void updateTick(World world, int x, int y, int z, Random rand) {

		if(!world.isRemote && rand.nextInt(50) == 0) {
			com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
			return;
		}

		super.updateTick(world, x, y, z, rand);
	}
}
