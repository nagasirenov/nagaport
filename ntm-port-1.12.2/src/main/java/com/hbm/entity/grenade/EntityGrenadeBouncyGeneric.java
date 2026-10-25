package com.hbm.entity.grenade;

import com.hbm.items.ModItems;
import com.hbm.items.weapon.ItemGenericGrenade;

import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityGrenadeBouncyGeneric extends EntityGrenadeBouncyBase implements IGenericGrenade {

	private static final DataParameter<Integer> GRENADE_ID = EntityDataManager.createKey(EntityGrenadeBouncyGeneric.class, DataSerializers.VARINT);

	public EntityGrenadeBouncyGeneric(World world) {
		super(world);
	}

	public EntityGrenadeBouncyGeneric(World world, EntityLivingBase living) {
		super(world, living);
	}

	public EntityGrenadeBouncyGeneric(World world, double x, double y, double z) {
		super(world, x, y, z);
	}
	
	public EntityGrenadeBouncyGeneric setType(ItemGenericGrenade grenade) {
		this.dataManager.set(GRENADE_ID, Item.getIdFromItem(grenade));
		return this;
	}

	@Override
	public ItemGenericGrenade getGrenade() {
		int grenadeId = this.dataManager.get(GRENADE_ID);
		Item item = Item.getItemById(grenadeId);
		ItemGenericGrenade gren = item instanceof ItemGenericGrenade ? (ItemGenericGrenade) item : null;
		return gren != null ? gren : (ItemGenericGrenade) ModItems.grenade_kyiv;
	}

	@Override
	protected void entityInit() {
		this.dataManager.register(GRENADE_ID, 0);
	}

	@Override
	public void explode() {
		getGrenade().explode(this, this.getThrower(), this.world, posX, posY, posZ);
		this.setDead();
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setInteger("grenade", this.dataManager.get(GRENADE_ID));
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		this.dataManager.set(GRENADE_ID, nbt.getInteger("grenade"));
	}

	@Override
	protected int getMaxTimer() {
		return getGrenade().getMaxTimer();
	}

	@Override
	protected double getBounceMod() {
		return getGrenade().getBounceMod();
	}
}
