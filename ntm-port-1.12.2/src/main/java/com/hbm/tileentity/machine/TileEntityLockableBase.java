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
				this.world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:block.lockOpen"), net.minecraft.util.SoundCategory.PLAYERS, 1.0F, 1.0F);
				return true;
			}

			if(stack != null && stack.getItem() == ModItems.key_red) {
				this.world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:block.lockOpen"), net.minecraft.util.SoundCategory.PLAYERS, 1.0F, 1.0F);
				return true;
			}

			return tryPick(player);
		}
	}

	private boolean tryPick(EntityPlayer player) {

		boolean canPick = false;
		ItemStack stack = com.hbm.util.NativeHeldItem.getMainHandNullable(player);
		double chanceOfSuccess = this.lockMod * 100;

		if(stack != null && stack.getItem() == ModItems.pin && (player.inventory.hasItemStack(new ItemStack(ModItems.screwdriver)) || player.inventory.hasItemStack(new ItemStack(ModItems.screwdriver_desh)))) {

			stack.shrink(1);
			canPick = true;
		}

		if(stack != null && (stack.getItem() == ModItems.screwdriver || stack.getItem() == ModItems.screwdriver_desh) && player.inventory.hasItemStack(new ItemStack(ModItems.pin))) {

			player.inventory.clearMatchingItems(ModItems.pin, -1, 1, null);
			player.inventoryContainer.detectAndSendChanges();
			canPick = true;
		}

		if(canPick) {

			if(ArmorUtil.checkArmorPiece(player, ModItems.jackt, 2) || ArmorUtil.checkArmorPiece(player, ModItems.jackt2, 2))
				chanceOfSuccess *= 100D;

			double rand = player.world.rand.nextDouble() * 100;

			if(chanceOfSuccess > rand) {
				this.world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:item.pinUnlock"), net.minecraft.util.SoundCategory.PLAYERS, 1.0F, 1.0F);
				return true;
			}

			this.world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:item.pinBreak"), net.minecraft.util.SoundCategory.PLAYERS, 1.0F, 0.8F + player.world.rand.nextFloat() * 0.2F);
		}

		return false;
	}

}
