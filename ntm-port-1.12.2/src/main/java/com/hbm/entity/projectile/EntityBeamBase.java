package com.hbm.entity.projectile;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class EntityBeamBase extends Entity {

	public EntityBeamBase(World world) {
		super(world);
		this.ignoreFrustumCheck = true;
	}

	public EntityBeamBase(World world, EntityPlayer player) {
		super(world);
		
		this.ignoreFrustumCheck = true;
		this.dataWatcher.updateObject(20, player.getDisplayName());
		
		Vec3d vec = player.getLookVec();
		vec = vec.rotateYaw(-90F);
		float l = 0.075F;
		vec = vec.scale(l);
		
		Vec3d vec0 = player.getLookVec();
		float d = 0.1F;
		vec0 = vec0.scale(d);
		
		this.setPosition(player.posX + vec.x + vec0.x, player.posY + player.getEyeHeight() + vec0.y, player.posZ + vec.z + vec0.z);
	}

	@Override
	protected void entityInit() {
        this.dataWatcher.addObject(20, "");
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound p_70037_1_) {
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound p_70014_1_) {
	}

    @Override
	@SideOnly(Side.CLIENT)
    public int getBrightnessForRender(float p_70070_1_)
    {
        return 15728880;
    }

    @Override
	public float getBrightness(float p_70013_1_)
    {
        return 1.0F;
    }
}
