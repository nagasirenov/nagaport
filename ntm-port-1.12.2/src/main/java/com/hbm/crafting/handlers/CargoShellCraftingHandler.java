package com.hbm.crafting.handlers;

import com.hbm.items.ModItems;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class CargoShellCraftingHandler extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

	@Override
	public boolean matches(InventoryCrafting inventory, World world) {
		
		int itemCount = 0;
		int shellCount = 0;
		
		for(int i = 0; i < 9; i++) {
			ItemStack stack = inventory.getStackInRowAndColumn(i % 3, i / 3);
			
			if(!stack.isEmpty()) {
				
				// Both original NTM leave-grid opt-outs also report a container; vanilla always left the grid.
				// The removed Forge hook adds no exclusion in that verified domain. See dynamic-crafting notes.
				if(stack.getItem().hasContainerItem(stack))
					return false;
				
				itemCount++;
				
				if(stack.getItem() == ModItems.ammo_arty && stack.getItemDamage() == 8 && !stack.hasTagCompound()) {
					shellCount++;
				}
			}
		}
		
		return itemCount == 2 && shellCount == 1;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inventory) {
		
		ItemStack shell = null;
		ItemStack cargo = null;
		
		for(int i = 0; i < 9; i++) {
			ItemStack stack = inventory.getStackInRowAndColumn(i % 3, i / 3);
			
			if(stack.isEmpty())
				continue;

			if(stack.getItem() == ModItems.ammo_arty && stack.getItemDamage() == 8 && !stack.hasTagCompound()) {
				ItemStack copy = stack.copy();
				copy.setCount(1);
				shell = copy;
			} else {
				ItemStack copy = stack.copy();
				copy.setCount(1);
				cargo = copy;
			}
		}
		
		if(shell == null || cargo == null)
			return ItemStack.EMPTY;
		
		if(!shell.hasTagCompound())
			shell.setTagCompound(new NBTTagCompound());

		shell.getTagCompound().setTag("cargo", cargo.writeToNBT(new NBTTagCompound()));
		
		return shell;
	}

	public int getRecipeSize() {
		return 9;
	}

	@Override
	public boolean canFit(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return new ItemStack(ModItems.ammo_shell, 1, 8);
	}
}
