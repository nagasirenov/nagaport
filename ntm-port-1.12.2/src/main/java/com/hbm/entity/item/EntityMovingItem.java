package com.hbm.entity.item;

import com.hbm.util.fauxpointtwelve.BlockPos;

import api.hbm.conveyor.IConveyorItem;
import api.hbm.conveyor.IEnterableBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class EntityMovingItem extends EntityMovingConveyorObject implements IConveyorItem {

	private static final DataParameter<ItemStack> ITEM = EntityDataManager.createKey(EntityMovingItem.class, DataSerializers.ITEM_STACK);
	private static final DataParameter<Boolean> HAS_ITEM = EntityDataManager.createKey(EntityMovingItem.class, DataSerializers.BOOLEAN);
	
	public EntityItem cacheForRender = null;

	public EntityMovingItem(World p_i1582_1_) {
		super(p_i1582_1_);
		this.setSize(0.375F, 0.375F);
	}

	public void setItemStack(ItemStack stack) {
		// The native ITEM_STACK serializer requires a non-null stack.
		this.dataManager.set(ITEM, stack == null ? ItemStack.EMPTY : stack);
		this.dataManager.set(HAS_ITEM, stack != null);
		// Retain the original forced update, including mutations to the same stack instance.
		this.dataManager.setDirty(ITEM);
	}

	@Override
	public ItemStack getItemStack() {
		// A live stack may be consumed down to zero by transfer callers. Native metadata
		// serializes that as EMPTY, so track presence separately from stack emptiness.
		return this.dataManager.get(HAS_ITEM) ? this.dataManager.get(ITEM) : new ItemStack(Blocks.STONE);
	}

	/** Adds the item to the player's inventory */
	@Override
	public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {

		if(!world.isRemote && !this.isDead && player.inventory.addItemStackToInventory(this.getItemStack().copy())) {
			player.inventoryContainer.detectAndSendChanges();
			this.setDead();
		}

		return false;
	}

	/** Knocks the item off the belt */
	@Override
	public boolean hitByEntity(Entity attacker) {

		if(!world.isRemote && !this.isDead) {
			this.setDead();
			world.spawnEntity(new EntityItem(world, posX, posY, posZ, this.getItemStack()));
		}
		return false;
	}

	/** Ensures the item is knocked off the belt due to non-player attacks (explosions, etc) */
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		this.hitByEntity(source.getTrueSource());
		return true;
	}

	@Override
	protected void entityInit() {
		this.dataManager.register(ITEM, ItemStack.EMPTY);
		this.dataManager.register(HAS_ITEM, false);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		
		if(world.isRemote) {
			ItemStack item = this.getItemStack();
			//initial cache creation
			if(this.cacheForRender == null) {
				cacheForRender = new EntityItem(world, 0, 0, 0, item);
			}
			//if the cache is no longer relevant, update
			if(!ItemStack.areItemStacksEqual(cacheForRender.getItem(), item)) {
				cacheForRender.setItem(item);
			}
		}
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {

		NBTTagCompound compound = nbt.getCompoundTag("Item");
		this.setItemStack(new ItemStack(compound));

		// Validate the stored value, not the getter's stone fallback.
		ItemStack stack = this.dataManager.get(ITEM);

		if(stack.isEmpty())
			this.setDead();
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {

		if(this.getItemStack() != null)
			nbt.setTag("Item", this.getItemStack().writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void enterBlock(IEnterableBlock enterable, BlockPos pos, ForgeDirection dir) {
		if(this.isDead) return;
		
		if(enterable.canItemEnter(world, pos.getX(), pos.getY(), pos.getZ(), dir, this)) {
			enterable.onItemEnter(world, pos.getX(), pos.getY(), pos.getZ(), dir, this);
			this.setDead();
		}
	}

	@Override
	public boolean onLeaveConveyor() {
		
		if(this.isDead) return true;
		
		this.setDead();
		EntityItem item = new EntityItem(world, posX + motionX * 2, posY + motionY * 2, posZ + motionZ * 2, this.getItemStack());
		item.motionX = this.motionX * 2;
		item.motionY = 0.1;
		item.motionZ = this.motionZ * 2;
		item.velocityChanged = true;
		world.spawnEntity(item);
		
		return true;
	}
}
