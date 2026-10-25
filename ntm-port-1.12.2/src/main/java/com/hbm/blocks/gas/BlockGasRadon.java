package com.hbm.blocks.gas;

import java.util.Random;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.util.ArmorRegistry;
import com.hbm.util.ArmorUtil;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ArmorRegistry.HazardClass;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class BlockGasRadon extends BlockGasBase {

	public BlockGasRadon() {
		super(0.1F, 0.8F, 0.1F);
	}


		@Override
	public void onEntityCollision(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, net.minecraft.entity.Entity entityIn) {
		onEntityCollidedWithBlock(worldIn, pos.getX(), pos.getY(), pos.getZ(), entityIn);
	}

public void onEntityCollidedWithBlock(World world, int p_149670_2_, int p_149670_3_, int p_149670_4_, Entity entity) {

		if(!(entity instanceof EntityLivingBase))
			return;

		EntityLivingBase entityLiving = (EntityLivingBase) entity;

		if(ArmorRegistry.hasAllProtection(entityLiving, 3, HazardClass.PARTICLE_FINE)) {
			ArmorUtil.damageGasMaskFilter(entityLiving, 1);
		} else {
			ContaminationUtil.contaminate((EntityLivingBase)entity, HazardType.RADIATION, ContaminationType.RAD_BYPASS, 0.05F);
			HbmLivingProps.incrementAsbestos((EntityLivingBase)entity, 1);
		}
	}

	@Override
	public ForgeDirection getFirstDirection(World world, int x, int y, int z) {

		if(world.rand.nextInt(5) == 0)
			return ForgeDirection.UP;

		return ForgeDirection.DOWN;
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
