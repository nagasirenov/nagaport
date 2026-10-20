package com.hbm.entity.mob.ai;

import java.util.List;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

public class EntityAISwimAway extends EntityAIBase {

	private EntityCreature fish;
	private double speed;
	private EntityPlayer closestPlayer;
	private double randPosY;
	private double randPosX;
	private double randPosZ;

	public EntityAISwimAway(EntityCreature fish, double speed) {
		this.fish = fish;
		this.speed = speed;
	}

	@Override
	public boolean shouldExecute() {
		List players = fish.world.getEntitiesWithinAABB(EntityPlayer.class, fish.getEntityBoundingBox().grow(5.0D, 3.0D, 5.0D));
		if(!players.isEmpty()) {
			closestPlayer = (EntityPlayer) players.get(0);
			Vec3d vec3 = RandomPositionGenerator.findRandomTarget(this.fish, 5, 4);
			this.randPosX = vec3.x;
			this.randPosY = vec3.y;
			this.randPosZ = vec3.z;
			return true;
		}

		return false;
	}

	@Override
	public void startExecuting() {
		this.fish.getNavigator().tryMoveToXYZ(this.randPosX, this.randPosY, this.randPosZ, this.speed);
	}

	@Override
	public boolean continueExecuting() {
		// Ensure that the fish continues to flee as long as the player is close
		return fish.getDistanceToEntity(closestPlayer) < 10.0F;
	}
}