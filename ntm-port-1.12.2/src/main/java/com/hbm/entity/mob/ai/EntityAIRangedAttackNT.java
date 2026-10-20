package com.hbm.entity.mob.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.MathHelper;

/** Ranged task for NTM mobs that retains the original attack-radius and timer rules.
 * Native EntityAIAttackRanged omits the radius check at the firing boundary.
 */
public class EntityAIRangedAttackNT extends EntityAIBase {
    private final EntityLiving host;
    private final IRangedAttackMob rangedHost;
    private EntityLivingBase target;
    private int attackTime = -1;
    private final double speed;
    private int seeTime;
    private final int minimumInterval;
    private final int maximumInterval;
    private final float radius;
    private final float radiusSquared;

    public EntityAIRangedAttackNT(IRangedAttackMob owner, double speed, int minimumInterval, int maximumInterval, float radius) {
        if (!(owner instanceof EntityLivingBase)) {
            throw new IllegalArgumentException("ArrowAttackGoal requires Mob implements RangedAttackMob");
        }
        this.rangedHost = owner;
        this.host = (EntityLiving) owner;
        this.speed = speed;
        this.minimumInterval = minimumInterval;
        this.maximumInterval = maximumInterval;
        this.radius = radius;
        this.radiusSquared = radius * radius;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        EntityLivingBase current = this.host.getAttackTarget();
        if (current == null) return false;
        this.target = current;
        return true;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.shouldExecute() || !this.host.getNavigator().noPath();
    }

    @Override
    public void resetTask() {
        this.target = null;
        this.seeTime = 0;
        this.attackTime = -1;
    }

    @Override
    public void updateTask() {
        double distance = this.host.getDistanceSq(this.target.posX, this.target.getEntityBoundingBox().minY, this.target.posZ);
        boolean visible = this.host.getEntitySenses().canSee(this.target);
        if (visible) ++this.seeTime;
        else this.seeTime = 0;

        if (distance > (double) this.radiusSquared || this.seeTime < 20) {
            this.host.getNavigator().tryMoveToEntityLiving(this.target, this.speed);
        } else {
            this.host.getNavigator().clearPath();
        }
        this.host.getLookHelper().setLookPositionWithEntity(this.target, 30.0F, 30.0F);

        if (--this.attackTime == 0) {
            // Keep the original zero-tick early return, including the subsequent negative-tick restart.
            if (distance > (double) this.radiusSquared || !visible) return;
            float ratio = MathHelper.sqrt(distance) / this.radius;
            float power = ratio;
            if (power < 0.1F) power = 0.1F;
            if (power > 1.0F) power = 1.0F;
            this.rangedHost.attackEntityWithRangedAttack(this.target, power);
            this.attackTime = MathHelper.floor(ratio * (float) (this.maximumInterval - this.minimumInterval) + (float) this.minimumInterval);
        } else if (this.attackTime < 0) {
            float ratio = MathHelper.sqrt(distance) / this.radius;
            this.attackTime = MathHelper.floor(ratio * (float) (this.maximumInterval - this.minimumInterval) + (float) this.minimumInterval);
        }
    }
}
