package com.hbm.handler.guncfg;

import java.util.List;

import com.hbm.entity.grenade.EntityGrenadeFlare;
import com.hbm.entity.projectile.EntityBulletBaseNT;
import com.hbm.entity.projectile.EntityBulletBaseNT.*;
import com.hbm.handler.BulletConfiguration;
import com.hbm.util.BobMathUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.util.math.Vec3d;

public class BulletConfigFactory {

	public static final float defaultSpread = 0.005F;

	/// STANDARD CONFIGS ///
	//do not include damage or ammo
	public static BulletConfiguration standardBulletConfig() {

		BulletConfiguration bullet = new BulletConfiguration();

		bullet.velocity = 5.0F;
		bullet.spread = defaultSpread;
		bullet.wear = 10;
		bullet.bulletsMin = 1;
		bullet.bulletsMax = 1;
		bullet.gravity = 0D;
		bullet.maxAge = 100;
		bullet.doesRicochet = true;
		bullet.ricochetAngle = 5;
		bullet.HBRC = 2;
		bullet.LBRC = 95;
		bullet.headshotMult = 1.25F;
		bullet.bounceMod = 0.8;
		bullet.doesPenetrate = true;
		bullet.doesBreakGlass = true;
		bullet.destroysBlocks = false;
		bullet.style = BulletConfiguration.STYLE_NORMAL;
		bullet.plink = BulletConfiguration.PLINK_BULLET;
		bullet.leadChance = 5;

		return bullet;
	}

	public static BulletConfiguration standardRocketConfig() {

		BulletConfiguration bullet = new BulletConfiguration();

		bullet.velocity = 2.0F;
		bullet.spread = defaultSpread;
		bullet.wear = 10;
		bullet.bulletsMin = 1;
		bullet.bulletsMax = 1;
		bullet.gravity = 0.005D;
		bullet.maxAge = 300;
		bullet.doesRicochet = true;
		bullet.ricochetAngle = 10;
		bullet.HBRC = 2;
		bullet.LBRC = 100;
		bullet.bounceMod = 0.8;
		bullet.doesPenetrate = false;
		bullet.doesBreakGlass = false;
		bullet.explosive = 5.0F;
		bullet.style = BulletConfiguration.STYLE_ROCKET;
		bullet.plink = BulletConfiguration.PLINK_GRENADE;
		bullet.vPFX = "smoke";

		return bullet;
	}

	public static BulletConfiguration standardGrenadeConfig() {

		BulletConfiguration bullet = new BulletConfiguration();

		bullet.velocity = 2.0F;
		bullet.spread = defaultSpread;
		bullet.wear = 10;
		bullet.bulletsMin = 1;
		bullet.bulletsMax = 1;
		bullet.gravity = 0.035D;
		bullet.maxAge = 300;
		bullet.doesRicochet = false;
		bullet.ricochetAngle = 0;
		bullet.HBRC = 0;
		bullet.LBRC = 0;
		bullet.bounceMod = 1.0;
		bullet.doesPenetrate = false;
		bullet.doesBreakGlass = false;
		bullet.explosive = 2.5F;
		bullet.style = BulletConfiguration.STYLE_GRENADE;
		bullet.plink = BulletConfiguration.PLINK_GRENADE;
		bullet.vPFX = "smoke";

		return bullet;
	}

	public static IBulletUpdateBehaviorNT getHomingBehavior(final double range, final double angle) {

		IBulletUpdateBehaviorNT onUpdate = new IBulletUpdateBehaviorNT() {

			@Override
			public void behaveUpdate(EntityBulletBaseNT bullet) {

				if(bullet.world.isRemote)
					return;

				if(bullet.world.getEntityByID(bullet.getEntityData().getInteger("homingTarget")) == null) {
					chooseTarget(bullet);
				}

				Entity target = bullet.world.getEntityByID(bullet.getEntityData().getInteger("homingTarget"));

				if(target != null) {

					Vec3d delta = new Vec3d(target.posX - bullet.posX, target.posY + target.height / 2 - bullet.posY, target.posZ - bullet.posZ);
					delta = delta.normalize();
					double vel = new Vec3d(bullet.motionX, bullet.motionY, bullet.motionZ).length();

					bullet.motionX = delta.x * vel;
					bullet.motionY = delta.y * vel;
					bullet.motionZ = delta.z * vel;
				}
			}

			private void chooseTarget(EntityBulletBaseNT bullet) {

				List<EntityLivingBase> entities = bullet.world.getEntitiesWithinAABB(EntityLivingBase.class, bullet.getEntityBoundingBox().expand(range, range, range));

				Vec3d mot = new Vec3d(bullet.motionX, bullet.motionY, bullet.motionZ);

				EntityLivingBase target = null;
				double targetAngle = angle;

				for(EntityLivingBase e : entities) {

					if(!e.isEntityAlive() || e == bullet.getThrower())
						continue;

					Vec3d delta = new Vec3d(e.posX - bullet.posX, e.posY + e.height / 2 - bullet.posY, e.posZ - bullet.posZ);

					if(bullet.world.rayTraceBlocks(new Vec3d(bullet.posX, bullet.posY, bullet.posZ), new Vec3d(e.posX, e.posY + e.height / 2, e.posZ), false, true, false) != null)
						continue;

					double dist = e.getDistanceSq(bullet);

					if(dist < range * range) {

						double deltaAngle = BobMathUtil.getCrossAngle(mot, delta);

						if(deltaAngle < targetAngle) {
							//Checks if the bullet is not already inside the entity's bounding box, so it doesn't pick the same target
							if(bullet.getConfig().doesPenetrate && bullet.world.getEntitiesWithinAABB(EntityLivingBase.class, bullet.getEntityBoundingBox().grow(2, 2, 2)) == null) {
								continue;
							}
							target = e;
							targetAngle = deltaAngle;
						}
					}
				}

				if(target != null) {
					bullet.getEntityData().setInteger("homingTarget", target.getEntityId());
				}
			}
		};

		return onUpdate;
	}
	/** Resets the bullet's target **/
	public static IBulletHurtBehaviorNT getPenHomingBehavior(){
		return (bullet, hit) -> bullet.getEntityData().setInteger("homingTarget", 0);
	}

}
