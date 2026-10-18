package com.hbm.tileentity.turret;

import com.hbm.handler.threading.PacketThreading;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.factory.XFactory9mm;
import com.hbm.packet.toclient.AuxParticlePacketNT;

import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;

public class TileEntityTurretSentryDamaged extends TileEntityTurretSentry {

	@Override
	public boolean hasPower() { //does not need power
		return true;
	}

	@Override
	public boolean isOn() { //is always on
		return true;
	}

	@Override
	public double getTurretYawSpeed() {
		return 3D;
	}

	@Override
	public double getTurretPitchSpeed() {
		return 2D;
	}

	@Override
	public boolean hasThermalVision() {
		return false;
	}

	@Override
	public boolean entityAcceptableTarget(Entity e) { //will fire at any living entity

		if(e instanceof EntityPlayer && ((EntityPlayer)e).capabilities.isCreativeMode)
			return false;

		return e instanceof EntityLivingBase;
	}

	@Override
	public void updateFiringTick() {

		timer++;

		if(timer % 10 == 0) {

			BulletConfig conf = XFactory9mm.p9_fmj;

			if(conf != null) {

				Vec3d pos = this.getTurretPos();
				Vec3d vec = new Vec3d(0, 0, 0);
				Vec3d side = new Vec3d(0, 0, 0);

				this.cachedCasingConfig = conf.casing;

				if(shotSide) {
					this.worldObj.playSoundEffect(xCoord, yCoord, zCoord, "hbm:turret.sentry_fire", 2.0F, 1.0F);
					this.spawnBullet(conf, 5F);

					com.hbm.util.Vec3NT barrel = new com.hbm.util.Vec3NT(this.getBarrelLength(), 0, 0);
					barrel.rotateAroundZRad((float) -this.rotationPitch);
					barrel.rotateAroundYRad((float) -(this.rotationYaw + Math.PI * 0.5));
					vec = barrel.toVec3d();

					side = new Vec3d(0.125 * (shotSide ? 1 : -1), 0, 0);
					side = side.rotateYaw((float) -(this.rotationYaw));

				} else {
					this.worldObj.playSoundEffect(xCoord, yCoord, zCoord, "hbm:turret.sentry_fire", 2.0F, 0.75F);
					if(usesCasings()) {
						if(this.casingDelay() == 0) {
							spawnCasing();
						} else {
							casingDelay = this.casingDelay();
						}
					}
				}

				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "vanillaExt");
				data.setString("mode", "largeexplode");
				data.setFloat("size", 1F);
				data.setByte("count", (byte) 1);
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, pos.x + vec.x + side.x, pos.y + vec.y, pos.z + vec.z + side.z), new TargetPoint(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 50));

				if(shotSide) {
					this.didJustShootLeft = true;
				} else {
					this.didJustShootRight = true;
				}
				shotSide = !shotSide;
			}
		}
	}
}
