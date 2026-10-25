package com.hbm.blocks.gas;

import java.util.Random;

import com.hbm.util.ArmorRegistry;
import com.hbm.util.ArmorRegistry.HazardClass;
import com.hbm.util.ArmorUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class BlockGasClorine extends BlockGasBase {

	public BlockGasClorine() {
		super(0.7F, 0.8F, 0.6F);
	}

	@Override
	public int getRenderType() {
		return 0;
	}

	@SideOnly(Side.CLIENT)
	public int getRenderBlockPass() {
		return 1;
	}


		@Override
	public void onEntityCollision(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, net.minecraft.entity.Entity entityIn) {
		onEntityCollidedWithBlock(worldIn, pos.getX(), pos.getY(), pos.getZ(), entityIn);
	}

public void onEntityCollidedWithBlock(World world, int p_149670_2_, int p_149670_3_, int p_149670_4_, Entity entity) {

		if(!(entity instanceof EntityLivingBase))
			return;

		EntityLivingBase entityLiving = (EntityLivingBase) entity;

		if(ArmorRegistry.hasAllProtection(entityLiving, 3, HazardClass.GAS_LUNG)) {
			ArmorUtil.damageGasMaskFilter(entityLiving, 1);

		} else {
			entityLiving.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.BLINDNESS, 5 * 20, 0));
			entityLiving.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.POISON, 20 * 20, 2));
			entityLiving.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.WITHER, 1 * 20, 1));
			entityLiving.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.SLOWNESS, 30 * 20, 1));
			entityLiving.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.MINING_FATIGUE, 30 * 20, 2));
		}
	}


		@Override
	public void updateTick(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, java.util.Random rand) {
		updateTick(worldIn, pos.getX(), pos.getY(), pos.getZ(), rand);
	}

public void updateTick(World world, int x, int y, int z, Random rand) {

		if(!world.isRemote && rand.nextInt(10) == 0) {
			com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
			return;
		}

		super.updateTick(world, x, y, z, rand);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess p_149646_1_, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumFacing p_149646_5_) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		return !com.hbm.world.WorldBlockState.read(p_149646_1_, x, y, z).isOpaqueCube();
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
}
