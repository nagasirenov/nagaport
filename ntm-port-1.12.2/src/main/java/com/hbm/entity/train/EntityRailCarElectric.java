package com.hbm.entity.train;

import com.hbm.items.ModItems;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;

public abstract class EntityRailCarElectric extends EntityRailCarRidable {

	private static final DataParameter<Integer> POWER = EntityDataManager.createKey(EntityRailCarElectric.class, DataSerializers.VARINT);

	public EntityRailCarElectric(World world) {
		super(world);
	}

	public abstract int getMaxPower();
	public abstract int getPowerConsumption();
	
	public boolean hasChargeSlot() { return false; }
	public int getChargeSlot() { return 0; }
	
	@Override protected void entityInit() {
		super.entityInit();
		this.dataManager.register(POWER, new Integer(0));
	}
	
	@Override public boolean canAccelerate() {
		return true;
		//return this.getPower() >= this.getPowerConsumption();
	}
	
	@Override public void consumeFuel() {
		//this.setPower(this.getPower() - this.getPowerConsumption());
	}
	
	public void setPower(int power) {
		this.dataManager.set(POWER, power);
	}
	
	public int getPower() {
		return this.dataManager.get(POWER);
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		if(!this.world.isRemote) {
			
			if(this.hasChargeSlot()) {
				ItemStack stack = this.getStackInSlot(this.getChargeSlot());
				
				if(stack != null && stack.getItem() instanceof IBatteryItem) {
					IBatteryItem battery = (IBatteryItem) stack.getItem();
					int powerNeeded = this.getMaxPower() - this.getPower();
					long powerProvided = Math.min(battery.getDischargeRate(stack), battery.getCharge(stack));
					int powerTransfered = (int) Math.min(powerNeeded, powerProvided);
					
					if(powerTransfered > 0) {
						battery.dischargeBattery(stack, powerTransfered);
						this.setPower(this.getPower() + powerTransfered);
					}
				} else if(stack != null) {
					if(stack.getItem() == ModItems.battery_creative) {
						this.setPower(this.getMaxPower());
					}
				}
			}
		}
	}
}
