package com.hbm.entity.effect;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;

import net.minecraft.world.World;

public class EntityCloudTom extends Entity {

	private static final DataParameter<Integer> MAX_AGE = EntityDataManager.createKey(EntityCloudTom.class, DataSerializers.VARINT);

	public int maxAge = 100;
	public int age;

	public EntityCloudTom(World p_i1582_1_) {
		super(p_i1582_1_);
		this.setSize(1, 4);
		this.ignoreFrustumCheck = true;
		this.isImmuneToFire = true;
		this.age = 0;
	}

	@Override
	protected void entityInit() {
		this.dataManager.register(MAX_AGE, Integer.valueOf(0));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender() {
		return 15728880;
	}

	@Override
	public float getBrightness() {
		return 1.0F;
	}

	public EntityCloudTom(World p_i1582_1_, int maxAge) {
		super(p_i1582_1_);
		this.setSize(20, 40);
		this.isImmuneToFire = true;
		this.setMaxAge(maxAge);
	}

	@Override
	public void onUpdate() {
		
		this.age++;
		this.world.setLastLightningBolt(2);

		if (this.age >= this.getMaxAge()) {
			this.setDead();
		}
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound p_70037_1_) {
		age = p_70037_1_.getShort("age");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound p_70014_1_) {
		p_70014_1_.setShort("age", (short) age);
	}

	public void setMaxAge(int i) {
		this.dataManager.set(MAX_AGE, Integer.valueOf(i));
	}

	public int getMaxAge() {
		return this.dataManager.get(MAX_AGE);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return true;
	}
}
