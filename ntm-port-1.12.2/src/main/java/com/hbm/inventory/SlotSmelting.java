package com.hbm.inventory;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.math.MathHelper;

public class SlotSmelting extends Slot {
	
	private EntityPlayer thePlayer;
	private int itemCountBuffer;

	public SlotSmelting(EntityPlayer player, IInventory inv, int id, int x, int y) {
		super(inv, id, x, y);
		this.thePlayer = player;
	}
	
	public boolean isItemValid(ItemStack stack) {
		return false;
	}
	
	public ItemStack decrStackSize(int amount) {
		
		if(this.getHasStack()) {
			this.itemCountBuffer += Math.min(amount, this.getStack().getCount());
		}
		
		return super.decrStackSize(amount);
	}

	public ItemStack onTake(EntityPlayer player, ItemStack stack) {
		this.onCrafting(stack);
		return super.onTake(player, stack);
	}
	
	protected void onCrafting(ItemStack stack, int amount) {
		this.itemCountBuffer += amount;
		this.onCrafting(stack);
	}
	
	protected void onCrafting(ItemStack stack) {
		
		stack.onCrafting(this.thePlayer.world, this.thePlayer, this.itemCountBuffer);

		if(!this.thePlayer.world.isRemote) {
			
			int buffer = this.itemCountBuffer;
			float exp = FurnaceRecipes.instance().getSmeltingExperience(stack);
			int remainingExp;

			if(exp == 0.0F) {
				buffer = 0;
				
			} else if(exp < 1.0F) {
				remainingExp = MathHelper.floor((float) buffer * exp);

				if(remainingExp < MathHelper.ceil((float) buffer * exp) && (float) Math.random() < (float) buffer * exp - (float) remainingExp) {
					++remainingExp;
				}

				buffer = remainingExp;
			}

			while(buffer > 0) {
				remainingExp = EntityXPOrb.getXPSplit(buffer);
				buffer -= remainingExp;
				this.thePlayer.world.spawnEntity(new EntityXPOrb(this.thePlayer.world, this.thePlayer.posX, this.thePlayer.posY + 0.5D, this.thePlayer.posZ + 0.5D, remainingExp));
			}
		}

		this.itemCountBuffer = 0;

		FMLCommonHandler.instance().firePlayerSmeltedEvent(thePlayer, stack);

		// The native smelt event above owns both legacy iron/fish gates exactly once.
	}
}
