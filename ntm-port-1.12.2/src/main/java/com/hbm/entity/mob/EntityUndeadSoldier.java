package com.hbm.entity.mob;

import com.hbm.items.ModItems;

import net.minecraft.block.Block;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class EntityUndeadSoldier extends EntityMob {

	private static final DataParameter<Byte> TYPE = EntityDataManager.createKey(EntityUndeadSoldier.class, DataSerializers.BYTE);
	public static final byte TYPE_ZOMBIE = 0;
	public static final byte TYPE_SKELETON = 1;

	public EntityUndeadSoldier(World world) {
		super(world);
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(4, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(6, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true, false));
		this.targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityVillager.class, true, false));
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(TYPE, Byte.valueOf(TYPE_ZOMBIE));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5.0D);
	}

	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData data) {
		IEntityLivingData result = super.onInitialSpawn(difficulty, data);
		this.dataManager.set(TYPE, Byte.valueOf(rand.nextBoolean() ? TYPE_ZOMBIE : TYPE_SKELETON));
		return result;
	}

	public byte getUndeadType() {
		return this.dataManager.get(TYPE);
	}

	@Override
	protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
		this.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.HEAD, new ItemStack(ModItems.taurun_helmet));
		this.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.CHEST, new ItemStack(ModItems.taurun_plate));
		this.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.LEGS, new ItemStack(ModItems.taurun_legs));
		this.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.FEET, new ItemStack(ModItems.taurun_boots));

		int gun = rand.nextInt(5);
		if(gun == 0) this.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.gun_heavy_revolver));
		if(gun == 1) this.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.gun_light_revolver));
		if(gun == 2) this.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.gun_carbine));
		if(gun == 3) this.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.gun_maresleg));
		if(gun == 4) this.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.gun_greasegun));
	}

	@Override
	protected SoundEvent getAmbientSound() {
		byte type = this.dataManager.get(TYPE);
		if(type == TYPE_ZOMBIE) return SoundEvents.ENTITY_ZOMBIE_AMBIENT;
		if(type == TYPE_SKELETON) return SoundEvents.ENTITY_SKELETON_AMBIENT;
		return super.getAmbientSound();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		byte type = this.dataManager.get(TYPE);
		if(type == TYPE_ZOMBIE) return SoundEvents.ENTITY_ZOMBIE_HURT;
		if(type == TYPE_SKELETON) return SoundEvents.ENTITY_SKELETON_HURT;
		return super.getHurtSound(source);
	}

	@Override
	protected SoundEvent getDeathSound() {
		byte type = this.dataManager.get(TYPE);
		if(type == TYPE_ZOMBIE) return SoundEvents.ENTITY_ZOMBIE_DEATH;
		if(type == TYPE_SKELETON) return SoundEvents.ENTITY_SKELETON_DEATH;
		return super.getDeathSound();
	}

	@Override
	protected void playStepSound(BlockPos pos, Block block) {
		byte type = this.dataManager.get(TYPE);
		if(type == TYPE_ZOMBIE) this.playSound(SoundEvents.ENTITY_ZOMBIE_STEP, 0.15F, 1.0F);
		if(type == TYPE_SKELETON) this.playSound(SoundEvents.ENTITY_SKELETON_STEP, 0.15F, 1.0F);
	}

	@Override
	public EnumCreatureAttribute getCreatureAttribute() {
		return EnumCreatureAttribute.UNDEAD;
	}

	@Override
	public boolean getCanSpawnHere() {
		return this.world.getDifficulty() != EnumDifficulty.PEACEFUL
				&& this.world.checkNoEntityCollision(this.getEntityBoundingBox())
				&& this.world.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty()
				&& !this.world.containsAnyLiquid(this.getEntityBoundingBox());
	}

	@Override protected void dropFewItems(boolean player, int loot) { }
	@Override protected void dropEquipment(boolean player, int loot) { }
}
