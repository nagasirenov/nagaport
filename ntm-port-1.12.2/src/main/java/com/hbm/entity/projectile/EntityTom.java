package com.hbm.entity.projectile;

import com.hbm.entity.effect.EntityCloudTom;
import com.hbm.entity.logic.EntityTomBlast;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityTom extends EntityThrowable {

	public EntityTom(World p_i1582_1_) {
		super(p_i1582_1_);
		this.ignoreFrustumCheck = true;
	}

	@Override
	public void onUpdate() {

		this.lastTickPosX = this.prevPosX = posX;
		this.lastTickPosY = this.prevPosY = posY;
		this.lastTickPosZ = this.prevPosZ = posZ;
		this.setPosition(posX + this.motionX, posY + this.motionY, posZ + this.motionZ);

		if(this.ticksExisted % 100 == 0) {
			com.hbm.sound.WorldSoundEffects.play(this.world, posX, posY, posZ, "hbm:alarm.chime", 10000, 1.0F);
		}

		motionY = -0.5;

		if(com.hbm.world.WorldBlockState.read(this.world, (int) this.posX, (int) this.posY, (int) this.posZ).getBlock() != Blocks.AIR || this.posY < 10) {
			if(!this.world.isRemote) {
				EntityTomBlast tom = new EntityTomBlast(this.world);
				tom.posX = posX;
				tom.posY = posY;
				tom.posZ = posZ;
				tom.destructionRange = 600;
				this.world.spawnEntity(tom);

				EntityCloudTom cloud = new EntityCloudTom(this.world, 500);
				cloud.setLocationAndAngles(posX, posY, posZ, 0, 0);
				this.world.spawnEntity(cloud);
			}
			this.setDead();
		}
	}

	@Override
	protected void onImpact(RayTraceResult p_70184_1_) {
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return distance < 500000;
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
}
