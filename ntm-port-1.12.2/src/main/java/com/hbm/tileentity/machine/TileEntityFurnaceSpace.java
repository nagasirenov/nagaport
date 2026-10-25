package com.hbm.tileentity.machine;

import com.hbm.blocks.machine.BlockFurnaceSpace;
import com.hbm.tileentity.TileEntityMachineBase;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Method;

public class TileEntityFurnaceSpace extends TileEntityFurnace {
	private static final Method CAN_SMELT = ReflectionHelper.findMethod(TileEntityFurnace.class, "canSmelt", "func_145948_k");
	private static final Method SMELT_ITEM = ReflectionHelper.findMethod(TileEntityFurnace.class, "smeltItem", "func_145949_j");

	// Overrides the vanilla furnace TE to check airbreathing

	@Override
	public void update() {
		int burnTime = this.getField(0);
		boolean flag = burnTime > 0;
		boolean flag1 = false;

		if(burnTime > 0) {
			this.setField(0, --burnTime);
		}

		if(!this.world.isRemote) {
			ItemStack fuelStack = this.getStackInSlot(1);
			ItemStack inputStack = this.getStackInSlot(0);
			if(burnTime != 0 || !fuelStack.isEmpty() && !inputStack.isEmpty()) {
				if(burnTime == 0 && canSmeltVanilla()) {
					int newBurnTime = getItemBurnTime(fuelStack);

					// If the machine can't combust, provide no burning (lava works without an atmosphere though!)
					if(newBurnTime > 0 && fuelStack.getItem() != Items.LAVA_BUCKET && !TileEntityMachineBase.breatheAir(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), 0)) {
						newBurnTime = 0;
					}

					this.setField(1, newBurnTime);
					this.setField(0, newBurnTime);
					burnTime = newBurnTime;

					if(burnTime > 0) {
						flag1 = true;

						if(!fuelStack.isEmpty()) {
							Item fuelItem = fuelStack.getItem();
							fuelStack.shrink(1);

							if(fuelStack.isEmpty()) {
								this.setInventorySlotContents(1, fuelItem.getContainerItem(fuelStack));
							}
						}
					}
				}

				if(this.isBurning() && canSmeltVanilla()) {
					int cookTime = this.getField(2) + 1;
					this.setField(2, cookTime);

					if(cookTime == 200) {
						this.setField(2, 0);
						smeltItemVanilla();
						flag1 = true;
					}
				} else {
					this.setField(2, 0);
				}
			}

			burnTime = this.getField(0);
			if(flag != burnTime > 0) {
				flag1 = true;
				BlockFurnaceSpace.updateFurnaceBlockState(burnTime > 0, this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());
			}
		}

		if(flag1) {
			this.markDirty();
		}
	}

	private boolean canSmeltVanilla() {
		try {
			return (Boolean) CAN_SMELT.invoke(this);
		} catch(Exception e) {
			throw new IllegalStateException("Failed to invoke vanilla furnace canSmelt", e);
		}
	}

	private void smeltItemVanilla() {
		try {
			SMELT_ITEM.invoke(this);
		} catch(Exception e) {
			throw new IllegalStateException("Failed to invoke vanilla furnace smeltItem", e);
		}
	}

}
