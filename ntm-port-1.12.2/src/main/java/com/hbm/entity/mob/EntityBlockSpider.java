package com.hbm.entity.mob;

import net.minecraft.block.Block;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class EntityBlockSpider extends EntityMob {

	private static final DataParameter<Integer> BLOCK_ID = EntityDataManager.createKey(EntityBlockSpider.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> BLOCK_META = EntityDataManager.createKey(EntityBlockSpider.class, DataSerializers.VARINT);


	public EntityBlockSpider(World world) {
		super(world);

		this.setSize(0.95F, 1.25F);
		this.getNavigator().getNodeProcessor().setCanSwim(false);
		this.tasks.addTask(1, new EntityAIWander(this, 0.5F));
		this.targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(1F);
	}

	public boolean isAIEnabled() {
		return true;
	}

	@Override
	protected void entityInit() {
		super.entityInit();

		this.dataManager.register(BLOCK_ID, 1);
		this.dataManager.register(BLOCK_META, 0);
	}

	public void makeBlock(Block block, int meta) {

		int b = Block.getIdFromBlock(block);

		this.dataManager.set(BLOCK_ID, b);
		this.dataManager.set(BLOCK_META, meta);

		double health = Math.max(1D, block.getExplosionResistance(null));

		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(health);
		this.setHealth(this.getMaxHealth());
	}

	public int getBlockId() { return this.dataManager.get(BLOCK_ID); }
	public int getBlockMetadata() { return this.dataManager.get(BLOCK_META); }
}
