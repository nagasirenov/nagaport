package com.hbm.inventory.fluid.tank;

import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.fluid.FluidType;

import net.minecraft.item.ItemStack;

public class FluidLoaderStandard extends FluidLoadingHandler {

	@Override
	public boolean fillItem(ItemStack[] slots, int in, int out, FluidTank tank) {
		
		if(tank.pressure != 0) return false;
		if((slots[in] == null || slots[in].isEmpty())) return true;
		
		FluidType type = tank.getTankType();
		ItemStack full = FluidContainerRegistry.getFullContainer(slots[in], type);
		
		if(full != null && slots[in] != null && !slots[in].isEmpty() && tank.getFill() - FluidContainerRegistry.getFluidContent(full, type) >= 0) {
			
			String name = slots[in].hasDisplayName() ? slots[in].getDisplayName() : null;
			
			if((slots[out] == null || slots[out].isEmpty())) {
				
				tank.setFill(tank.getFill() - FluidContainerRegistry.getFluidContent(full, type));
				slots[out] = full.copy();
				slots[in].shrink(1);
				if(slots[in].getCount() <= 0) {
					slots[in] = ItemStack.EMPTY;
				}
				
				if(name != null) slots[out].setStackDisplayName(name);
				
			} else if(slots[out] != null && !slots[out].isEmpty() && slots[out].getItem() == full.getItem() && slots[out].getItemDamage() == full.getItemDamage() && slots[out].getCount() < slots[out].getMaxStackSize()) {
				
				tank.setFill(tank.getFill() - FluidContainerRegistry.getFluidContent(full, type));
				slots[in].shrink(1);
				
				if(slots[in].getCount() <= 0) {
					slots[in] = ItemStack.EMPTY;
				}
				slots[out].grow(1);
				
				if(name != null) slots[out].setStackDisplayName(name);
			}
		}
		
		return false;
	}

	@Override
	public boolean emptyItem(ItemStack[] slots, int in, int out, FluidTank tank) {
		
		if((slots[in] == null || slots[in].isEmpty()))
			return true;
		
		FluidType type = tank.getTankType();
		int amount = FluidContainerRegistry.getFluidContent(slots[in], type);
		
		if(amount > 0 && tank.getFill() + amount <= tank.maxFluid) {
			
			ItemStack emptyContainer = FluidContainerRegistry.getEmptyContainer(slots[in]);
			
			String name = slots[in].hasDisplayName() ? slots[in].getDisplayName() : null;
			
			if((slots[out] == null || slots[out].isEmpty())) {
				
				tank.setFill(tank.getFill() + amount);
				slots[out] = emptyContainer == null ? ItemStack.EMPTY : emptyContainer;
				
				if(emptyContainer != null) {
					if(name != null) slots[out].setStackDisplayName(name);
				}
				
				slots[in].shrink(1);
				if(slots[in].getCount() <= 0) {
					slots[in] = ItemStack.EMPTY;
				}
				
			} else if(slots[out] != null && !slots[out].isEmpty() && (emptyContainer == null || (slots[out].getItem() == emptyContainer.getItem() && slots[out].getItemDamage() == emptyContainer.getItemDamage() && slots[out].getCount() < slots[out].getMaxStackSize()))) {
				
				tank.setFill(tank.getFill() + amount);
				slots[in].shrink(1);
				
				if(slots[in].getCount() <= 0) {
					slots[in] = ItemStack.EMPTY;
				}
				
				if(emptyContainer != null) {
					slots[out].grow(1);
					if(name != null) slots[out].setStackDisplayName(name);
				}
			}
			
			return true;
		}
		
		return false;
	}

}
