package com.hbm.entity.mob.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.Vec3d;

public class EntityAISwimmingVTwo extends EntityAIBase {

	private EntityCreature fish;
	private double xPosition;
	private double yPosition;
	private double zPosition;
	private double speed;

	public EntityAISwimmingVTwo(EntityCreature fish, double speed) {
		this.fish = fish;
		this.speed = speed;
		this.setMutexBits(1); // Set the mutex bits as needed
	}

	@Override
	public boolean shouldExecute() {
		if(this.fish.getRNG().nextInt(120) != 0) {
			return false;
		}

		Vec3d randomTarget = RandomPositionGenerator.findRandomTarget(this.fish, 10, 7);
		if(randomTarget == null) {
			return false;
		} else {
			this.xPosition = randomTarget.x;
			this.yPosition = randomTarget.y;
			this.zPosition = randomTarget.z;
			return true;
		}
	}


		@Override
	public boolean shouldContinueExecuting() {
		return continueExecuting();
	}

public boolean continueExecuting() {
		return !this.fish.getNavigator().noPath();
	}

	@Override
	public void startExecuting() {
		this.fish.getNavigator().tryMoveToXYZ(this.xPosition, this.yPosition, this.zPosition, this.speed);
	}
}


