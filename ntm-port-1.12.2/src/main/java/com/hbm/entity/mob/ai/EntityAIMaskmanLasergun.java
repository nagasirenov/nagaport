package com.hbm.entity.mob.ai;

import com.hbm.entity.projectile.EntityBulletBaseNT;
import com.hbm.handler.BulletConfigSyncingUtil;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.Vec3d;

public class EntityAIMaskmanLasergun extends EntityAIBase {

	private EntityCreature owner;
    private EntityLivingBase target;
    private EnumLaserAttack attack;
    private int timer;
    private int attackCount;

	public EntityAIMaskmanLasergun(EntityCreature owner, boolean checkSight, boolean nearbyOnly) {
		this.owner = owner;

		attack = EnumLaserAttack.values()[owner.getRNG().nextInt(3)];
	}

	@Override
	public boolean shouldExecute() {

        EntityLivingBase entity = this.owner.getAttackTarget();

        if(entity == null) {
            return false;

        } else {
            this.target = entity;
            double dist = new Vec3d(target.posX - owner.posX, target.posY - owner.posY, target.posZ - owner.posZ).length();
            return dist > 10;
        }
	}


        @Override
    public boolean shouldContinueExecuting() {
        return continueExecuting();
    }

public boolean continueExecuting() {
        return this.shouldExecute() || !this.owner.getNavigator().noPath();
    }

	@Override
    public void updateTask() {

		timer--;

		if(timer <= 0) {
			timer = attack.delay;

			switch(attack) {
			case ORB:
				EntityBulletBaseNT orb = new EntityBulletBaseNT(owner.world, BulletConfigSyncingUtil.MASKMAN_ORB, owner, target, 2.0F, 0);
				orb.motionY += 0.5D;

				owner.world.spawnEntity(orb);
				owner.world.playSound(null, owner.posX, owner.posY, owner.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:weapon.teslaShoot"), net.minecraft.util.SoundCategory.NEUTRAL, 1.0F, 1.0F);
				break;

			case MISSILE:
				EntityBulletBaseNT missile = new EntityBulletBaseNT(owner.world, BulletConfigSyncingUtil.MASKMAN_ROCKET, owner, target, 1.0F, 0);
				Vec3d vec = new Vec3d(target.posX - owner.posX, 0, target.posZ - owner.posZ);
				missile.motionX = vec.x * 0.05D;
				missile.motionY = 0.5D + owner.getRNG().nextDouble() * 0.5D;
				missile.motionZ = vec.z * 0.05D;

				owner.world.spawnEntity(missile);
				owner.world.playSound(null, owner.posX, owner.posY, owner.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:weapon.hkShoot"), net.minecraft.util.SoundCategory.NEUTRAL, 1.0F, 1.0F);
				break;

			case SPLASH:

				for(int i = 0; i < 5; i++) {
					EntityBulletBaseNT tracer = new EntityBulletBaseNT(owner.world, BulletConfigSyncingUtil.MASKMAN_TRACER, owner, target, 1.0F, 0.05F);
					owner.world.spawnEntity(tracer);
				}
				break;

			default:
				break;
			}

			attackCount++;

			if(attackCount >= attack.amount) {

				attackCount = 0;

				int newAtk = attack.ordinal() + owner.getRNG().nextInt(EnumLaserAttack.values().length - 1);
				attack = EnumLaserAttack.values()[newAtk % EnumLaserAttack.values().length];
			}
		}

		this.owner.rotationYaw = this.owner.rotationYawHead;
    }

	private static enum EnumLaserAttack {

		ORB(60, 5),
		MISSILE(10, 10),
		SPLASH(40, 3);

		public int delay;
		public int amount;

		private EnumLaserAttack(int delay, int amount) {
			this.delay = delay;
			this.amount = amount;
		}
	}
}
