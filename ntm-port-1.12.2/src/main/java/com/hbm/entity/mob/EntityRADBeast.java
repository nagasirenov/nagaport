package com.hbm.entity.mob;

import java.util.List;

import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.items.ModItems;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.MainRegistry;

import api.hbm.entity.ISuffocationImmune;
import api.hbm.entity.IRadiationImmune;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class EntityRADBeast extends EntityMob implements IRadiationImmune, ISuffocationImmune {

	private static final DataParameter<Integer> TARGET_ID = EntityDataManager.createKey(EntityRADBeast.class, DataSerializers.VARINT);
	protected int attackTime;
	protected boolean hasAttacked;

	private float heightOffset = 0.5F;
	private int heightOffsetUpdateTime;

	public EntityRADBeast(World world) {
		super(world);
		this.isImmuneToFire = true;
		this.experienceValue = 30;
	}

	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(120.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(16.0D);
	}

	public EntityRADBeast makeLeader() {
		this.setDropChance(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, 1F);
		this.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.coin_radiation));
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(360.0D);
		this.heal(this.getMaxHealth());
		return this;
	}

	protected boolean canDespawn() {
		return false;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(TARGET_ID, Integer.valueOf(0));
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return com.hbm.main.ModSoundEvents.resolve("hbm:item.geiger" + (1 + rand.nextInt(6)));
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return com.hbm.main.ModSoundEvents.resolve("mob.blaze.hit");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return com.hbm.main.ModSoundEvents.resolve("hbm:step.iron");
	}

	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender(float f) {
		return 15728880;
	}

	public float getBrightness(float f) {
		return 1.0F;
	}

	public int getTotalArmorValue() {
		return 8;
	}

	public void onLivingUpdate() {

		if(!this.world.isRemote) {

			if(this.isWet()) {
				this.attackEntityFrom(DamageSource.DROWN, 1.0F);
			}

			--this.heightOffsetUpdateTime;

			if(this.heightOffsetUpdateTime <= 0) {
				this.heightOffsetUpdateTime = 100;
				this.heightOffset = 0.5F + (float) this.rand.nextGaussian() * 3.0F;
			}

			if(this.getAttackTarget() != null && this.getAttackTarget().posY + (double) this.getAttackTarget().getEyeHeight() > this.posY + (double) this.getEyeHeight() + (double) this.heightOffset) {
				this.motionY += (0.30000001192092896D - this.motionY) * 0.30000001192092896D;
			}

			if(this.getAttackTarget() != null && attackTime < 10) {

				if(this.dataManager.get(TARGET_ID).intValue() != this.getAttackTarget().getEntityId())
					this.dataManager.set(TARGET_ID, Integer.valueOf(this.getAttackTarget().getEntityId()));
			} else {
				this.dataManager.set(TARGET_ID, Integer.valueOf(0));
			}
		}

		if(!this.onGround && this.motionY < 0.0D) {
			this.motionY *= 0.6D;
		}

		if(this.getMaxHealth() <= 150) {

			for(int i = 0; i < 6; i++) {
				this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.TOWN_AURA, this.posX + (this.rand.nextDouble() - 0.5D) * (double) this.width * 1.5, this.posY + this.rand.nextDouble() * (double) this.height, this.posZ + (this.rand.nextDouble() - 0.5D) * (double) this.width * 1.5, 0.0D, 0.0D, 0.0D);
			}

			if(this.rand.nextInt(6) == 0) {

				this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.FLAME, this.posX + (this.rand.nextDouble() - 0.5D) * (double) this.width, this.posY + this.rand.nextDouble() * (double) this.height * 0.75, this.posZ + (this.rand.nextDouble() - 0.5D) * (double) this.width, 0.0D, 0.0D, 0.0D);
			}

		} else {
			this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.LAVA, this.posX + (this.rand.nextDouble() - 0.5D) * (double) this.width, this.posY + this.rand.nextDouble() * (double) this.height * 0.75, this.posZ + (this.rand.nextDouble() - 0.5D) * (double) this.width, 0.0D, 0.0D, 0.0D);
		}

		if(this.attackTime > 0) {
			--this.attackTime;
		}

		if(!this.world.isRemote && this.getAttackTarget() != null) {
			this.attackEntity(this.getAttackTarget(), (float) this.getDistance(this.getAttackTarget()));
		}

		super.onLivingUpdate();
	}

	protected void attackEntity(Entity target, float dist) {

		if(this.attackTime <= 0 && dist < 2.0F && target.getEntityBoundingBox().maxY > this.getEntityBoundingBox().minY && target.getEntityBoundingBox().minY < this.getEntityBoundingBox().maxY) {
			this.attackTime = 20;
			this.attackEntityAsMob(target);

		} else if(dist < 30.0F) {

			double deltaX = target.posX - this.posX;
			double deltaZ = target.posZ - this.posZ;

			if(this.attackTime == 0 && this.getAttackTarget() != null) {

				ChunkRadiationManager.proxy.incrementRad(this.world, (int) Math.floor(posX), (int) Math.floor(posY), (int) Math.floor(posZ), 100);
				target.attackEntityFrom(ModDamageSource.radiation, 16.0F);
				this.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
				this.playLivingSound();
				this.attackTime = 20;
			}

			this.rotationYaw = (float) (Math.atan2(deltaZ, deltaX) * 180.0D / Math.PI) - 90.0F;
			this.hasAttacked = true;
		}
	}

	public Entity getUnfortunateSoul() {

		int id = this.dataManager.get(TARGET_ID).intValue();
		return this.world.getEntityByID(id);
	}

	protected void fall(float p_70069_1_) {
	}

	protected Item getDropItem() {
		return ModItems.rod_zirnox_uranium_fuel_depleted;
	}

	@Override
	public void onDeath(DamageSource p_70645_1_) {
		super.onDeath(p_70645_1_);

		if(this.getMaxHealth() > 150) {
			List<EntityPlayer> players = this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow(50, 50, 50));

			for(EntityPlayer player : players) {
				com.hbm.advancement.NtmAdvancements.award(player, MainRegistry.bossMeltdown);
			}
		}
	}

	protected void dropFewItems(boolean beenHit, int looting) {

		if(beenHit) {

			if(looting > 0) {
				this.dropItem(ModItems.nugget_polonium, looting);
			}

			int count = this.rand.nextInt(3) + 1;

			for(int i = 0; i < count; i++) {

				int r = this.rand.nextInt(3);

				if(r == 0) {
					this.dropItem(this.isWet() ? ModItems.waste_uranium : ModItems.rod_zirnox_uranium_fuel_depleted, this.isWet() ? 2 : 1);

				} else if(r == 1) {
					this.dropItem(this.isWet() ? ModItems.waste_mox : ModItems.rod_zirnox_mox_fuel_depleted, this.isWet() ? 2 : 1);

				} else if(r == 2) {
					this.dropItem(this.isWet() ? ModItems.waste_plutonium : ModItems.rod_zirnox_plutonium_fuel_depleted, this.isWet() ? 2 : 1);

				}
			}
		}
	}
}
