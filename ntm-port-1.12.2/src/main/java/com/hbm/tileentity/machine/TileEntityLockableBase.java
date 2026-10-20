package com.hbm.tileentity.machine;

import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemKey;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.ArmorUtil;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public abstract class TileEntityLockableBase extends TileEntityLoadedBase {

	protected int lock;
	private boolean isLocked = false;
	protected double lockMod = 0.1D;

	public boolean isLocked() {
		return isLocked;
	}

	public void lock() {
		if(lock == 0) {
			MainRegistry.logger.error("A block has been set to locked state before setting pins, this should not happen and may cause errors! " + this.toString());
		}
		isLocked = true;
		markDirty();
	}

	public void setPins(int pins) { lock = pins; markDirty(); }
	public int getPins() { return lock; }
	public void setMod(double mod) { lockMod = mod; markDirty(); }
	public double getMod() { return lockMod; }

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		lock = nbt.getInteger("lock");
		isLocked = nbt.getBoolean("isLocked");
		lockMod = nbt.getDouble("lockMod");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setInteger("lock", lock);
		nbt.setBoolean("isLocked", isLocked);
		nbt.setDouble("lockMod", lockMod);
		return nbt;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);

		buf.writeInt(lock);
		buf.writeBoolean(isLocked);
		buf.writeDouble(lockMod);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);

		lock = buf.readInt();
		isLocked = buf.readBoolean();
		lockMod = buf.readDouble();
	}

	public boolean canAccess(EntityPlayer player) {

		if(!isLocked) {
			return true;
		} else {
			if(player == null)
				return false;
			ItemStack stack = com.hbm.util.NativeHeldItem.getMainHandNullable(player);

			if(stack != null && stack.getItem() instanceof ItemKey && ItemKey.getPins(stack) == this.lock) {
				this.world.playSoundAtEntity(player, "hbm:block.lockOpen", 1.0F, 1.0F);
				return true;
			}

			if(stack != null && stack.getItem() == ModItems.key_red) {
				this.world.playSoundAtEntity(player, "hbm:block.lockOpen", 1.0F, 1.0F);
				return true;
			}

			return tryPick(player);
		}
	}

	private boolean tryPick(EntityPlayer player) {

		boolean canPick = false;
		ItemStack stack = com.hbm.util.NativeHeldItem.getMainHandNullable(player);
		double chanceOfSuccess = this.lockMod * 100;

		if(stack != null && stack.getItem() == ModItems.pin && (player.inventory.hasItem(ModItems.screwdriver) || player.inventory.hasItem(ModItems.screwdriver_desh))) {

			stack.stackSize--;
			canPick = true;
		}

		if(stack != null && (stack.getItem() == ModItems.screwdriver || stack.getItem() == ModItems.screwdriver_desh) && player.inventory.hasItem(ModItems.pin)) {

			player.inventory.consumeInventoryItem(ModItems.pin);
			player.inventoryContainer.detectAndSendChanges();
			canPick = true;
		}

		if(canPick) {

			if(ArmorUtil.checkArmorPiece(player, ModItems.jackt, 2) || ArmorUtil.checkArmorPiece(player, ModItems.jackt2, 2))
				chanceOfSuccess *= 100D;

			double rand = player.world.rand.nextDouble() * 100;

			if(chanceOfSuccess > rand) {
				this.world.playSoundAtEntity(player, "hbm:item.pinUnlock", 1.0F, 1.0F);
				return true;
			}

			this.world.playSoundAtEntity(player, "hbm:item.pinBreak", 1.0F, 0.8F + player.world.rand.nextFloat() * 0.2F);
		}

		return false;
	}

}
