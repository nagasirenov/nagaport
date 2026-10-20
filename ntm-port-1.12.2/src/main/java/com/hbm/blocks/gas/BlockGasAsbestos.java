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
import com.hbm.lib.ForgeDirection;

public class BlockGasAsbestos extends BlockGasBase {

	public BlockGasAsbestos() {
		super(0.6F, 0.6F, 0.5F);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		super.randomDisplayTick(world, x, y, z, rand);

		if(world.rand.nextInt(5) == 0)
			world.spawnParticle(net.minecraft.util.EnumParticleTypes.TOWN_AURA, x + rand.nextFloat(), y + rand.nextFloat(), z + rand.nextFloat(), 0.0D, 0.0D, 0.0D);
	}

	@Override
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
	public void updateTick(World world, int x, int y, int z, Random rand) {

		if(!world.isRemote && rand.nextInt(50) == 0) {
			com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
			return;
		}
		
		super.updateTick(world, x, y, z, rand);
	}
}
