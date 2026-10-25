package com.hbm.blocks.gas;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.potion.HbmPotion;
import com.hbm.util.ArmorRegistry;
import com.hbm.util.ArmorUtil;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ArmorRegistry.HazardClass;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.potion.PotionEffect;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class BlockGasRadonDense extends BlockGasBase {

	public BlockGasRadonDense() {
		super(0.1F, 0.5F, 0.1F);
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
			ContaminationUtil.contaminate((EntityLivingBase)entity, HazardType.RADIATION, ContaminationType.CREATIVE, 0.5F);
			entityLiving.addPotionEffect(new PotionEffect(HbmPotion.radiation, 15 * 20, 0));
			HbmLivingProps.incrementAsbestos(entityLiving, 5);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		super.randomDisplayTick(state, world, pos, rand);
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		world.spawnParticle(net.minecraft.util.EnumParticleTypes.TOWN_AURA, x + rand.nextFloat(), y + rand.nextFloat(), z + rand.nextFloat(), 0.0D, 0.0D, 0.0D);
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

		if(!world.isRemote) {

			if(rand.nextInt(20) == 0) {
				if(com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() == Blocks.GRASS)
					com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y - 1, z, ModBlocks.waste_earth);
			}

			if(rand.nextInt(30) == 0) {
				com.hbm.world.WorldAirRemoval.remove(world, x, y, z);

				if(ModBlocks.fallout.canPlaceBlockAt(world, new net.minecraft.util.math.BlockPos(x, y, z))) {
					com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, ModBlocks.fallout);
				}

				return;
			}
		}

		super.updateTick(world, x, y, z, rand);
	}
}
