package com.hbm.entity.logic;

import com.hbm.entity.projectile.EntityBulletBaseNT;
import com.hbm.handler.BulletConfigSyncingUtil;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.packet.toclient.AuxParticlePacketNT;

import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityDeathBlast extends Entity {

	public static final int maxAge = 60;

	public EntityDeathBlast(World p_i1582_1_) {
		super(p_i1582_1_);
		this.ignoreFrustumCheck = true;
	}

	@Override
	protected void entityInit() { }

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) { }

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) { }

	@Override
	public void onUpdate() {

		if(this.ticksExisted >= maxAge && !this.world.isRemote) {
			this.setDead();

			this.world.spawnEntity(EntityNukeExplosionMK5.statFacNoRad(this.world, 40, posX, posY, posZ));

			int count = 100;
			for(int i = 0; i < count; i++) {

				Vec3d vec = new Vec3d(0.2, 0, 0);
				vec = vec.rotateYaw((float)(2 * Math.PI * i / (float)count));

				EntityBulletBaseNT laser = new EntityBulletBaseNT(this.world, BulletConfigSyncingUtil.MASKMAN_BOLT);
				laser.setPosition(posX, posY + 2, posZ);
				laser.motionX = vec.x;
				laser.motionZ = vec.z;
				laser.motionY = -0.01;
				this.world.spawnEntity(laser);
			}

			NBTTagCompound data = new NBTTagCompound();
			data.setString("type", "muke");
			PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, posX, posY + 0.5, posZ), new TargetPoint(this.world.provider.getDimension(), posX, posY, posZ, 250));
			com.hbm.sound.WorldSoundEffects.play(this.world, posX, posY, posZ, "hbm:weapon.mukeExplosion", 25.0F, 0.9F);
		}
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

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return distance < 25000;
	}
}
