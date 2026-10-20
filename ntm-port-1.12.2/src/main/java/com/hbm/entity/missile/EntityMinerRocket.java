package com.hbm.entity.missile;

import com.hbm.blocks.ModBlocks;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.util.ParticleUtil;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

public class EntityMinerRocket extends Entity {

	private static final DataParameter<Integer> FLIGHT_MODE = EntityDataManager.createKey(EntityMinerRocket.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> SATELLITE_FREQUENCY = EntityDataManager.createKey(EntityMinerRocket.class, DataSerializers.VARINT);
	//0 landing, 1 unloading, 2 lifting
	public int timer = 0;

	public EntityMinerRocket(World p_i1582_1_) {
		super(p_i1582_1_);
		this.ignoreFrustumCheck = true;
        this.setSize(1F, 3F);
	}

	public int getFlightMode() {
		return this.dataManager.get(FLIGHT_MODE);
	}

	public int getSatelliteFrequency() {
		return this.dataManager.get(SATELLITE_FREQUENCY);
	}

	public void setSatelliteFrequency(int frequency) {
		this.dataManager.set(SATELLITE_FREQUENCY, frequency);
	}

	@Override
	protected void entityInit() {
		this.dataManager.register(FLIGHT_MODE, 0);
		this.dataManager.register(SATELLITE_FREQUENCY, 0);
	}
	
	@Override
	public void onUpdate() {
		if(dataManager.get(FLIGHT_MODE) == 0)
			motionY = -0.75;
		if(dataManager.get(FLIGHT_MODE) == 1)
			motionY = 0;
		if(dataManager.get(FLIGHT_MODE) == 2)
			motionY = 1;
		
		motionX = 0;
		motionZ = 0;
		
		this.setPositionAndRotation(posX + motionX, posY + motionY, posZ + motionZ, 0.0F, 0.0F);

		if(dataManager.get(FLIGHT_MODE) == 0 && com.hbm.world.WorldBlockState.read(this.world, (int)(posX - 0.5), (int)(posY - 0.5), (int)(posZ - 0.5)).getBlock() == ModBlocks.sat_dock) {
			dataManager.set(FLIGHT_MODE, 1);
			motionY = 0;
			posY = (int)posY;
		} else if(com.hbm.world.WorldBlockState.read(this.world, (int)(posX - 0.5), (int)(posY + 1), (int)(posZ - 0.5)).getBlock().getMaterial() != Material.AIR && !this.world.isRemote && dataManager.get(FLIGHT_MODE) != 1) {
			this.setDead();
			ExplosionLarge.explodeFire(this.world, posX - 0.5, posY, posZ - 0.5, 10F, true, false, true);
			//worldObj.setBlock((int)(posX - 0.5), (int)(posY + 0.5), (int)(posZ - 0.5), Blocks.dirt);
		}
		
		if(dataManager.get(FLIGHT_MODE) == 1) {
			if(!this.world.isRemote && ticksExisted % 4 == 0)
				ExplosionLarge.spawnShock(this.world, posX, posY, posZ, 1 + rand.nextInt(3), 1 + rand.nextGaussian());
			
			timer++;
			
			if(timer > 100) {
				dataManager.set(FLIGHT_MODE, 2);
			}
		}
		
		if(dataManager.get(FLIGHT_MODE) != 1 && !this.world.isRemote && ticksExisted % 2 == 0) {
			ParticleUtil.spawnGasFlame(this.world, posX, posY - 0.5, posZ, 0.0, -1.0, 0.0);
		}
		
		if(dataManager.get(FLIGHT_MODE) == 2 && posY > 300)
			this.setDead();
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		dataManager.set(FLIGHT_MODE, nbt.getInteger("mode"));
		dataManager.set(SATELLITE_FREQUENCY, nbt.getInteger("sat"));
		timer = nbt.getInteger("timer");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setInteger("mode", dataManager.get(FLIGHT_MODE));
		nbt.setInteger("sat", dataManager.get(SATELLITE_FREQUENCY));
		nbt.setInteger("timer", timer);
	}
}
