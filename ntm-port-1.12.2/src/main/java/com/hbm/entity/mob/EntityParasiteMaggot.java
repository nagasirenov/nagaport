package com.hbm.entity.mob;

import api.hbm.entity.ISuffocationImmune;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.world.World;

public class EntityParasiteMaggot extends EntityMob implements ISuffocationImmune {

	public EntityParasiteMaggot(World world) {
		super(world);
		this.setSize(0.3F, 0.7F);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(1.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
	}

	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	protected Entity findPlayerToAttack() {
		return this.world.getNearestAttackablePlayer(this, 16, 16);
	}

	@Override
	public void onUpdate() {
		this.renderYawOffset = this.rotationYaw;
		super.onUpdate();
	}

	@Override
	protected boolean isValidLightLevel() {
		return true;
	}

	@Override
	public EnumCreatureAttribute getCreatureAttribute() {
		return EnumCreatureAttribute.ARTHROPOD;
	}
}
