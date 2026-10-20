package com.hbm.entity.effect;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

public class EntityEMPBlast extends Entity {

	private static final DataParameter<Integer> MAX_AGE = EntityDataManager.createKey(EntityEMPBlast.class, DataSerializers.VARINT);
	
	public int maxAge = 100;
	public int age;
    public float scale = 0;

	public EntityEMPBlast(World p_i1582_1_) {
		super(p_i1582_1_);
        this.setSize(1.5F, 1.5F);
		this.ignoreFrustumCheck = true;
		this.isImmuneToFire = true;
		this.age = 0;
    	scale = 0;
	}

	@Override
	protected void entityInit() {
		// The original default constructor left watched maxAge at zero, despite the public field being 100.
		this.dataManager.register(MAX_AGE, Integer.valueOf(0));
	}

    @Override
	@SideOnly(Side.CLIENT)
    public int getBrightnessForRender()
    {
        return 15728880;
    }

    @Override
	public float getBrightness()
    {
        return 1.0F;
    }

	public EntityEMPBlast(World p_i1582_1_, int maxAge) {
		super(p_i1582_1_);
        this.setSize(1.5F, 1.5F);
		this.isImmuneToFire = true;
		this.setMaxAge(maxAge);
	}

    @Override
	public void onUpdate() {
        this.age++;
        
        if(this.age >= this.getMaxAge())
        {
    		this.age = 0;
        	this.setDead();
        }
        
        this.scale++;
    }

	@Override
	protected void readEntityFromNBT(NBTTagCompound p_70037_1_) {
		age = p_70037_1_.getShort("age");
		scale = p_70037_1_.getShort("scale");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound p_70014_1_) {
		p_70014_1_.setShort("age", (short)age);
		p_70014_1_.setShort("scale", (short)scale);
		
	}
	
	public void setMaxAge(int i) {
		this.dataManager.set(MAX_AGE, Integer.valueOf(i));
	}
	
	public int getMaxAge() {
		return this.dataManager.get(MAX_AGE);
	}
}
