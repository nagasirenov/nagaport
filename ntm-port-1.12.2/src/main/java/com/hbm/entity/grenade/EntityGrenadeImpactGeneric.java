package com.hbm.entity.grenade;

import com.hbm.items.ModItems;
import com.hbm.items.weapon.ItemGenericGrenade;
import com.hbm.util.PersistentRegistryRef;

import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityGrenadeImpactGeneric extends EntityGrenadeBase implements IGenericGrenade {

	private static final DataParameter<Integer> GRENADE_ID = EntityDataManager.createKey(EntityGrenadeImpactGeneric.class, DataSerializers.VARINT);
	private int legacyGrenadeId = -1;

	public EntityGrenadeImpactGeneric(World p_i1773_1_) {
		super(p_i1773_1_);
	}

	public EntityGrenadeImpactGeneric(World p_i1774_1_, EntityLivingBase p_i1774_2_) {
		super(p_i1774_1_, p_i1774_2_);
	}

	public EntityGrenadeImpactGeneric(World p_i1775_1_, double p_i1775_2_, double p_i1775_4_, double p_i1775_6_) {
		super(p_i1775_1_, p_i1775_2_, p_i1775_4_, p_i1775_6_);
	}
	
	public EntityGrenadeImpactGeneric setType(ItemGenericGrenade grenade) {
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

		if(!this.world.isRemote && getGrenade() != null) {
			getGrenade().explode(this, this.getThrower(), this.world, posX, posY, posZ);
			this.setDead();
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		Item item = Item.getItemById(this.dataManager.get(GRENADE_ID));
		if(this.legacyGrenadeId >= 0) nbt.setInteger("grenade", this.legacyGrenadeId);
		else PersistentRegistryRef.writeItem(nbt, "grenade", item);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		Item item = PersistentRegistryRef.readItem(nbt, "grenade");
		if(item != null) {
			this.dataManager.set(GRENADE_ID, Item.getIdFromItem(item));
		} else {
			this.legacyGrenadeId = nbt.getInteger("grenade");
			this.dataManager.set(GRENADE_ID, 0);
		}
	}
}
