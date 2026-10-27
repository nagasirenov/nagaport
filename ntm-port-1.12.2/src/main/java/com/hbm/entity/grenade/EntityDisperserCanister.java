package com.hbm.entity.grenade;

import com.hbm.entity.effect.EntityMist;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.util.PersistentRegistryRef;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;

public class EntityDisperserCanister extends EntityGrenadeBase {

	private static final DataParameter<Integer> FLUID_TYPE = EntityDataManager.createKey(EntityDisperserCanister.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> ITEM_TYPE = EntityDataManager.createKey(EntityDisperserCanister.class, DataSerializers.VARINT);
	private int legacyItemId = -1;

	public EntityDisperserCanister(World world) {
		super(world);
	}

	public EntityDisperserCanister(World world, EntityLivingBase living) {
		super(world, living);
	}

	public EntityDisperserCanister(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	public EntityDisperserCanister setFluid(int id) {
		this.dataManager.set(FLUID_TYPE, id);
		return this;
	}

	@Override
	protected void entityInit() {
		this.dataManager.register(FLUID_TYPE, 0);
		this.dataManager.register(ITEM_TYPE, 0);
	}

	public EntityDisperserCanister setType(int id) {
		this.dataManager.set(ITEM_TYPE, id);
		return this;
	}

	public FluidType getFluid() {
		return Fluids.fromID(this.dataManager.get(FLUID_TYPE));
	}

	public Item getType() {
		return Item.getItemById(this.dataManager.get(ITEM_TYPE));
	}

	@Override
	public void explode() {
		if(!this.world.isRemote) {
			EntityMist mist = new EntityMist(this.world);
			mist.setType(getFluid());
			mist.setPosition(posX, posY, posZ);
			mist.setArea(10, 5);
			mist.setDuration(80);
			this.world.spawnEntity(mist);
			this.setDead();
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setInteger("fluid", this.dataManager.get(FLUID_TYPE));
		Item item = Item.getItemById(this.dataManager.get(ITEM_TYPE));
		if(this.legacyItemId >= 0) nbt.setInteger("item", this.legacyItemId);
		else PersistentRegistryRef.writeItem(nbt, "item", item);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		this.dataManager.set(FLUID_TYPE, nbt.getInteger("fluid"));
		Item item = PersistentRegistryRef.readItem(nbt, "item");
		if(item != null) {
			this.dataManager.set(ITEM_TYPE, Item.getIdFromItem(item));
		} else {
			this.legacyItemId = nbt.getInteger("item");
			this.dataManager.set(ITEM_TYPE, 0);
		}

	}
}
