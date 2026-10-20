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
import net.minecraft.item.ItemStack;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class EntityUndeadSoldier extends EntityMob {

	public static final int DW_TYPE = 12;
	public static final byte TYPE_ZOMBIE = 0;
	public static final byte TYPE_SKELETON = 1;

	public EntityUndeadSoldier(World world) {
		super(world);
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(4, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(6, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
		this.targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityVillager.class, 0, true));
	}

	protected void entityInit() {
		super.entityInit();
		this.getDataWatcher().addObject(DW_TYPE, Byte.valueOf((byte) 0));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0D);
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.25D);
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(5.0D);
	}

	@Override
	protected boolean isAIEnabled() {
		return true;
	}

	@Override
	public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
		this.addRandomArmor();
		this.dataWatcher.updateObject(DW_TYPE, rand.nextBoolean() ? TYPE_ZOMBIE : TYPE_SKELETON);
		return super.onSpawnWithEgg(data);
	}

	@Override
	protected void addRandomArmor() {
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
	protected String getLivingSound() {
		byte type = this.dataWatcher.getWatchableObjectByte(DW_TYPE);
		if(type == TYPE_ZOMBIE) return "mob.zombie.say";
		if(type == TYPE_SKELETON) return "mob.skeleton.say";
		return super.getLivingSound();
	}

	@Override
	protected String getHurtSound() {
		byte type = this.dataWatcher.getWatchableObjectByte(DW_TYPE);
		if(type == TYPE_ZOMBIE) return "mob.zombie.hurt";
		if(type == TYPE_SKELETON) return "mob.skeleton.hurt";
		return super.getHurtSound();
	}

	@Override
	protected String getDeathSound() {
		byte type = this.dataWatcher.getWatchableObjectByte(DW_TYPE);
		if(type == TYPE_ZOMBIE) return "mob.zombie.death";
		if(type == TYPE_SKELETON) return "mob.skeleton.death";
		return super.getDeathSound();
	}

	@Override
	protected void func_145780_a(int x, int y, int z, Block blck) {
		byte type = this.dataWatcher.getWatchableObjectByte(DW_TYPE);
		if(type == TYPE_ZOMBIE) this.playSound("mob.zombie.step", 0.15F, 1.0F);
		if(type == TYPE_SKELETON) this.playSound("mob.skeleton.step", 0.15F, 1.0F);
	}

	@Override
	public EnumCreatureAttribute getCreatureAttribute() {
		return EnumCreatureAttribute.UNDEAD;
	}

	@Override
	public boolean getCanSpawnHere() {
		return this.world.difficultySetting != EnumDifficulty.PEACEFUL && this.world.checkNoEntityCollision(this.getEntityBoundingBox()) && this.world.getCollidingBoundingBoxes(this, this.getEntityBoundingBox()).isEmpty() && !this.world.isAnyLiquid(this.getEntityBoundingBox());
	}

	@Override protected void dropFewItems(boolean player, int loot) { }
	@Override protected void dropEquipment(boolean player, int loot) { }
}
