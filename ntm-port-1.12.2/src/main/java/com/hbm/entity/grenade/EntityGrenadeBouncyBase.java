package com.hbm.entity.grenade;

import org.apache.logging.log4j.Level;

import com.hbm.config.GeneralConfig;
import com.hbm.main.MainRegistry;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class EntityGrenadeBouncyBase extends Entity implements IProjectile {

	protected EntityLivingBase thrower;
    protected String throwerName;
    protected int timer = 0;

	public EntityGrenadeBouncyBase(World world) {
		super(world);
        this.setSize(0.25F, 0.25F);
	}

    public EntityGrenadeBouncyBase(World world, EntityLivingBase living)
    {
        super(world);
        this.thrower = living;
        this.setSize(0.25F, 0.25F);
        this.setLocationAndAngles(living.posX, living.posY + (double)living.getEyeHeight(), living.posZ, living.rotationYaw, living.rotationPitch);
        this.posX -= (double)(MathHelper.cos(this.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
        this.posY -= 0.10000000149011612D;
        this.posZ -= (double)(MathHelper.sin(this.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
        this.setPosition(this.posX, this.posY, this.posZ);
        float f = 0.4F;
        this.motionX = (double)(-MathHelper.sin(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI) * f);
        this.motionZ = (double)(MathHelper.cos(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI) * f);
        this.motionY = (double)(-MathHelper.sin((this.rotationPitch + this.func_70183_g()) / 180.0F * (float)Math.PI) * f);
        this.shoot(this.motionX, this.motionY, this.motionZ, this.func_70182_d(), 1.0F);
        this.rotationPitch = 0;
        this.prevRotationPitch = 0;
    }

    public EntityGrenadeBouncyBase(World world, double posX, double posY, double posZ)
    {
        super(world);
        this.setSize(0.25F, 0.25F);
        this.setPosition(posX, posY, posZ);
    }

	@Override
	protected void entityInit() { }

    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double p_70112_1_)
    {
        double d1 = this.getEntityBoundingBox().getAverageEdgeLength() * 4.0D;
        d1 *= 64.0D;
        return p_70112_1_ < d1 * d1;
    }

    protected float func_70182_d()
    {
        return 1.5F;
    }

    protected float func_70183_g()
    {
        return 0.0F;
    }

    protected float getGravityVelocity()
    {
        return 0.03F;
    }

    public void shoot(double motionX, double motionY, double motionZ, float f0, float f1)
    {
        float f2 = MathHelper.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
        motionX /= (double)f2;
        motionY /= (double)f2;
        motionZ /= (double)f2;
        motionX += this.rand.nextGaussian() * 0.007499999832361937D * (double)f1;
        motionY += this.rand.nextGaussian() * 0.007499999832361937D * (double)f1;
        motionZ += this.rand.nextGaussian() * 0.007499999832361937D * (double)f1;
        motionX *= (double)f0;
        motionY *= (double)f0;
        motionZ *= (double)f0;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(motionX, motionZ) * 180.0D / Math.PI);
    }

    @SideOnly(Side.CLIENT)
    public void setVelocity(double motionX, double motionY, double motionZ)
    {
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;

        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
        {
            this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(motionX, motionZ) * 180.0D / Math.PI);
        }
    }

    public void onUpdate()
    {
        super.onUpdate();
        this.lastTickPosX = this.posX;
        this.lastTickPosY = this.posY;
        this.lastTickPosZ = this.posZ;

        this.prevRotationPitch = this.rotationPitch;

        this.rotationPitch -= new Vec3d(motionX, motionY, motionZ).length() * 25;

        //Bounce here

        boolean bounce = false;
        Vec3d vec3 = new Vec3d(this.posX, this.posY, this.posZ);
        Vec3d vec31 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
        RayTraceResult movingobjectposition = this.world.rayTraceBlocks(vec3, vec31, false, true, false);

        if (movingobjectposition != null)
        {
            vec31 = new Vec3d(movingobjectposition.hitVec.x, movingobjectposition.hitVec.y, movingobjectposition.hitVec.z);

            this.posX += (movingobjectposition.hitVec.x - this.posX) * 0.6;
            this.posY += (movingobjectposition.hitVec.y - this.posY) * 0.6;
            this.posZ += (movingobjectposition.hitVec.z - this.posZ) * 0.6;

		switch(movingobjectposition.sideHit.getIndex()) {
        	case 0:
        	case 1:
        		motionY *= -1; break;
        	case 2:
        	case 3:
        		motionZ *= -1; break;
        	case 4:
        	case 5:
        		motionX *= -1; break;

        	}

        	bounce = true;

            Vec3d mot = new Vec3d(motionX, motionY, motionZ);

            if(mot.length() > 0.05)
        		this.world.playSound(null, this.posX, this.posY, this.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:weapon.gBounce"), net.minecraft.util.SoundCategory.PLAYERS, 2.0F, 1.0F);

        	motionX *= getBounceMod();
        	motionY *= getBounceMod();
        	motionZ *= getBounceMod();
        }

        //Bounce here [END]

        if(!bounce) {
        	this.posX += this.motionX;
        	this.posY += this.motionY;
        	this.posZ += this.motionZ;
        }

        this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);

        while (this.rotationYaw - this.prevRotationYaw < -180.0F)
        {
            this.prevRotationYaw -= 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
        {
            this.prevRotationYaw += 360.0F;
        }

        this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
        float f2 = 0.99F;
        float f3 = this.getGravityVelocity();

        if (this.isInWater())
        {
            for (int i = 0; i < 4; ++i)
            {
                float f4 = 0.25F;
                this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * (double)f4, this.posY - this.motionY * (double)f4, this.posZ - this.motionZ * (double)f4, this.motionX, this.motionY, this.motionZ);
            }

            f2 = 0.8F;
        }

        this.motionX *= (double)f2;
        this.motionY *= (double)f2;
        this.motionZ *= (double)f2;
        this.motionY -= (double)f3;
        this.setPosition(this.posX, this.posY, this.posZ);

        timer++;

        if(timer >= getMaxTimer() && !this.world.isRemote) {
        	explode();

        	String s = "null";

        	if(thrower != null && thrower instanceof EntityPlayer)
        		s = ((EntityPlayer)thrower).getDisplayName().getUnformattedText();

    		if(GeneralConfig.enableExtendedLogging)
    			MainRegistry.logger.log(Level.INFO, "[GREN] Set off grenade at " + ((int)posX) + " / " + ((int)posY) + " / " + ((int)posZ) + " by " + s + "!");
        }
    }

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		timer = nbt.getInteger("timer");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setInteger("timer", timer);
	}

    @SideOnly(Side.CLIENT)
    public float getShadowSize()
    {
        return 0.0F;
    }

    public EntityLivingBase getThrower()
    {
        if (this.thrower == null && this.throwerName != null && this.throwerName.length() > 0)
        {
            this.thrower = this.world.getPlayerEntityByName(this.throwerName);
        }

        return this.thrower;
    }

    public abstract void explode();

    protected abstract int getMaxTimer();

    protected abstract double getBounceMod();

}
