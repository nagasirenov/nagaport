package com.hbm.tileentity.machine.storage;

import com.hbm.inventory.container.ContainerMassStorage;
import com.hbm.inventory.gui.GUIMassStorage;
import com.hbm.items.ModItems;
import com.hbm.tileentity.IControlReceiverFilter;

import com.hbm.util.BufferUtil;
import com.hbm.util.ItemStackUtil;

import api.hbm.redstoneoverradio.IRORInteractive;
import api.hbm.redstoneoverradio.IRORValueProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TileEntityMassStorage extends TileEntityCrateBase implements IControlReceiverFilter, IRORValueProvider, IRORInteractive, net.minecraft.util.ITickable {

	private int stack = 0;
	public boolean output = false;
	private int capacity;
	public int redstone = 0;

	@SideOnly(Side.CLIENT) public ItemStack type;

	public TileEntityMassStorage() {
		super(3);
	}

	public TileEntityMassStorage(int capacity) {
		this();
		this.capacity = capacity;
	}

	@Override
	public String getInventoryName() {
		return "container.massStorage";
	}

	@Override
	public void update() {

		if(!this.world.isRemote) {

			int newRed = this.getStockpile() * 15 / this.capacity;

			if(newRed != this.redstone) {
				this.redstone = newRed;
				this.markDirty();
			}

			if(slots[0] != null && slots[0].getItem() == ModItems.fluid_barrel_infinite) {
				this.stack = this.getCapacity();
			}

			if(this.getType() == null)
				this.stack = 0;

			if(canInsert(slots[0])) {

				int remaining = getCapacity() - getStockpile();
				int toRemove = Math.min(remaining, slots[0].stackSize);
				this.decrStackSize(0, toRemove);
				this.stack += toRemove;
				this.world.markTileEntityChunkModified(this.pos.getX(), this.pos.getY(), this.pos.getZ(), this);
			}

			if(output && getType() != null) {

				if(slots[2] != null && !(slots[2].isItemEqual(getType()) && ItemStack.areItemStackTagsEqual(slots[2], getType()))) {
					return;
				}

				int amount = Math.min(getStockpile(), getType().getMaxStackSize());

				if(amount > 0) {
					if(slots[2] == null) {
						slots[2] = slots[1].copy();
						slots[2].stackSize = amount;
					} else {
						amount = Math.min(amount, slots[2].getMaxStackSize() - slots[2].stackSize);
						slots[2].stackSize += amount;
					}
					this.stack -= amount;
				}
			}

			networkPackNT(32); // TE render distance
		}
	}

	public boolean canInsert(ItemStack stack) {
		return getType() != null && getStockpile() < getCapacity() && stack != null && stack.isItemEqual(getType()) && ItemStack.areItemStackTagsEqual(stack, getType());
	}

	public boolean quickInsert(ItemStack stack) {
		if(!canInsert(stack)) return false;

		int remaining = getCapacity() - getStockpile();
		if(remaining < stack.stackSize) return false;

		this.stack += stack.stackSize;
		stack.stackSize = 0;
		this.markDirty();

		return true;
	}

	public ItemStack quickExtract() {
		if(!output) return null;

		int amount = getType().getMaxStackSize();

		if(getStockpile() < amount) return null;

		ItemStack result = slots[1].copy();
		result.stackSize = amount;
		this.stack -= amount;
		this.markDirty();

		return result;
	}

	// Note: the following three methods are used for AE2 integration, and aren't meant to be called in any other context by default

	public int getTotalStockpile() {
		ItemStack type = getType();
		if(type == null) return 0;

		int result = getStockpile();

		ItemStack inStack = slots[0];
		if(inStack != null && !inStack.isEmpty() && ItemStackUtil.areStacksCompatible(type, inStack)) {
			result += inStack.getCount();
		}

		ItemStack outStack = slots[2];
		if(outStack != null && !outStack.isEmpty() && ItemStackUtil.areStacksCompatible(type, outStack)) {
			result += outStack.getCount();
		}

		return result;
	}

	// Returns the remainder that didn't fit.
	// If `actually` is false, only predicts the outcome, but doesn't change the state
	public int increaseTotalStockpile(int amount, boolean actually) {
		return changeTotalStockpile(amount, actually, +1);
	}

	// Returns the remainder that couldn't be extracted.
	// If `actually` is false, only predicts the outcome, but doesn't change the state
	public int decreaseTotalStockpile(int amount, boolean actually) {
		return changeTotalStockpile(amount, actually, -1);
	}

	private int changeTotalStockpile(int amount, boolean actually, int sign) {
		ItemStack type = getType();
		if(type == null) return amount;

		int stockpileAvail = sign > 0 ? getCapacity() - getStockpile() : getStockpile();

		if (amount > 0 && stockpileAvail > 0) {
			int depositStockpile = Math.min(amount, stockpileAvail);
			if (actually) {
				this.stack += sign * depositStockpile;
			}
			amount -= depositStockpile;
		}

		int inputAvail = 0;
		ItemStack inStack = slots[0];
		if(inStack != null && !inStack.isEmpty() && ItemStackUtil.areStacksCompatible(type, inStack)) {
			inputAvail = sign > 0 ? inStack.getMaxStackSize() - inStack.getCount() : inStack.getCount();
		} else if(inStack == null || inStack.isEmpty()) {
			inputAvail = sign > 0 ? type.getMaxStackSize() : 0;
		}

		if(amount > 0 && inputAvail > 0) {
			int depositInput = Math.min(amount, inputAvail);
			if(actually) {
				if(slots[0] == null || slots[0].isEmpty()) { // Only possible with sign == +1
					slots[0] = slots[1].copy();
					slots[0].setCount(0);
				}
				slots[0].grow(sign * depositInput);
				if(slots[0].getCount() == 0) {
					slots[0] = null;
				}
			}
			amount -= depositInput;
		}

		int outputAvail = 0;
		ItemStack outStack = slots[2];
		if(outStack != null && !outStack.isEmpty() && ItemStackUtil.areStacksCompatible(type, outStack)) {
			outputAvail = sign > 0 ? outStack.getMaxStackSize() - outStack.getCount() : outStack.getCount();
		} else if(outStack == null || outStack.isEmpty()) {
			outputAvail = sign > 0 ? type.getMaxStackSize() : 0;
		}

		if(amount > 0 && outputAvail > 0) {
			int depositOutput = Math.min(amount, outputAvail);
			if(actually) {
				if(slots[2] == null || slots[2].isEmpty()) { // Only possible with sign == +1
					slots[2] = slots[1].copy();
					slots[2].setCount(0);
				}
				slots[2].grow(sign * depositOutput);
				if(slots[2].getCount() == 0) {
					slots[2] = null;
				}
			}
			amount -= depositOutput;
		}

		if(actually) {
			this.markDirty();
		}

		return amount;
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeInt(this.stack);
		buf.writeBoolean(this.output);
		BufferUtil.writeItemStack(buf, this.slots[1]);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.stack = buf.readInt();
		this.output = buf.readBoolean();
		this.type = BufferUtil.readItemStack(buf);
	}

	public int getCapacity() {
		return capacity;
	}

	public ItemStack getType() {
		return slots[1] == null || slots[1].isEmpty() ? null : slots[1].copy();
	}

	public int getStockpile() {
		return stack;
	}

	public void setStockpile(int stack) {
		this.stack = stack;
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return new Vec3d(this.pos.getX() - player.posX, this.pos.getY() - player.posY, this.pos.getZ() - player.posZ).length() < 20;
	}

	@Override
	public void openInventory() {
		com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, "hbm:block.storageOpen", 0.5F, 1.0F);
	}

	@Override
	public void closeInventory() {
		com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, "hbm:block.storageClose", 0.5F, 1.0F);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.stack = nbt.getInteger("stack");
		this.output = nbt.getBoolean("output");
		this.capacity = nbt.getInteger("capacity");
		this.redstone = nbt.getByte("redstone");

		if(this.capacity <= 0) {
			this.capacity = 10_000;
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("stack", stack);
		nbt.setBoolean("output", output);
		nbt.setInteger("capacity", capacity);
		nbt.setByte("redstone", (byte) redstone);
		return nbt;
	}

	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 1024.0D; // only render mass storage info 32 blocks away, for performance
	}

	@Override
	public void nextMode(int i) {

	}

	@Override
	public void receiveControl(NBTTagCompound data) {

		if(data.hasKey("provide") && slots[1] != null) {

			if(this.getStockpile() == 0) {
				return;
			}

			int amount = data.getBoolean("provide") ? slots[1].getMaxStackSize() : 1;
			amount = Math.min(amount, getStockpile());

			if(slots[2] != null && !(slots[2].isItemEqual(getType()) && ItemStack.areItemStackTagsEqual(slots[2], getType()))) {
				return;
			}

			if(slots[2] == null) {
				slots[2] = slots[1].copy();
				slots[2].stackSize = amount;
			} else {
				amount = Math.min(amount, slots[2].getMaxStackSize() - slots[2].stackSize);
				slots[2].stackSize += amount;
			}
			this.stack -= amount;
		}

		if(data.hasKey("toggle")) {
			this.output = !output;
		}

		if(data.hasKey("slot") && this.getStockpile() <= 0){
			setFilterContents(data);
		}
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return !this.isLocked() && i == 0 && (this.getType() == null || (getType().isItemEqual(itemStack) && ItemStack.areItemStackTagsEqual(itemStack, getType())));
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return !this.isLocked() && i == 2;
	}

	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return new int[] { 0, 2 };
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMassStorage(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMassStorage(player.inventory, this);
	}

	@Override
	public int[] getFilterSlots() {
		return new int[]{1,2};
	}

	@Override
	public String[] getFunctionInfo() {
		return new String[] {
				PREFIX_VALUE + "type",
				PREFIX_VALUE + "fill",
				PREFIX_VALUE + "fillpercent",
				PREFIX_FUNCTION + "toggleoutput",
		};
	}

	@Override
	public String provideRORValue(String name) {
		if((PREFIX_VALUE + "fill").equals(name))		return "" + this.stack;
		if((PREFIX_VALUE + "fillpercent").equals(name))	return "" + this.stack * 100 / this.capacity;
		if((PREFIX_VALUE + "type").equals(name)) {
			if(slots[1] == null) return "None";
			return slots[1].getDisplayName();
		}
		return null;
	}

	@Override
	public String runRORFunction(String name, String[] params) {

		if((PREFIX_FUNCTION + "toggleoutput").equals(name)) {
			this.output = !this.output;
			this.markDirty();
		}

		return null;
	}
}
