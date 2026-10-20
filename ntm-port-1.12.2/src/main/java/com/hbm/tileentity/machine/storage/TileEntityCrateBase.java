package com.hbm.tileentity.machine.storage;

import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.machine.TileEntityLockableBase;

import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import com.hbm.util.NullableInventoryStackNbt;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.world.World;

import java.util.Random;

public abstract class TileEntityCrateBase extends TileEntityLockableBase implements ISidedInventory, IGUIProvider {

	protected ItemStack[] slots;
	public String customName;

	public boolean hasSpiders = false;

	public TileEntityCrateBase(int count) {
		slots = new ItemStack[count];
	}

	@Override
	public int getSizeInventory() {
		return slots.length;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return slots[i];
	}

	@Override
	public ItemStack removeStackFromSlot(int i) {
		if (slots[i] != null) {
			ItemStack itemStack = slots[i];
			slots[i] = null;
			return itemStack == null || itemStack.isEmpty() ? ItemStack.EMPTY : itemStack;
		} else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemStack) {
		itemStack = itemStack == null || itemStack.isEmpty() ? null : itemStack;
		slots[i] = itemStack;
		if (itemStack != null && itemStack.getCount() > getInventoryStackLimit()) {
			itemStack.setCount(getInventoryStackLimit());
		}
		this.markDirty();
	}

	@Override
	public boolean hasCustomName() {
		return this.customName != null && !this.customName.isEmpty();
	}

	/** Native inventory title retains the original GUI literal/translation choice. */
	@Override
	public net.minecraft.util.text.ITextComponent getDisplayName() {
		return hasCustomName() ? new net.minecraft.util.text.TextComponentString(getName()) : new net.minecraft.util.text.TextComponentTranslation(getName());
	}

	public void setCustomName(String name) {
		this.customName = name;
		markDirty();
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		if (world.getTileEntity(pos) != this) {
			return false;
		} else {
			return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64;
		}
	}

	@Override
	public void openInventory(net.minecraft.entity.player.EntityPlayer viewer) {
		com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, "hbm:block.crateOpen", 1.0F, 1.0F);
	}

	@Override
	public void closeInventory(net.minecraft.entity.player.EntityPlayer viewer) {
		com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, "hbm:block.crateClose", 1.0F, 1.0F);
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		return true;
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		if (slots[i] != null) {
			if (slots[i].stackSize <= j) {
				ItemStack itemStack = slots[i];
				slots[i] = null;
				return itemStack;
			}
			ItemStack itemStack1 = slots[i].splitStack(j);
			if (slots[i].stackSize == 0) {
				slots[i] = null;
			}

			this.markDirty();
			return itemStack1;
		} else {
			return null;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		NBTTagList list = nbt.getTagList("items", 10);

		slots = new ItemStack[getSizeInventory()];

		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound nbt1 = list.getCompoundTagAt(i);
			byte b0 = nbt1.getByte("slot");
			if (b0 >= 0 && b0 < slots.length) {
				slots[b0] = NullableInventoryStackNbt.read(nbt1);
			}
		}
		this.hasSpiders = nbt.getBoolean("spiders");

		customName = nbt.getString("name");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		NBTTagList list = new NBTTagList();

		for (int i = 0; i < slots.length; i++) {
			if (slots[i] != null) {
				NBTTagCompound nbt1 = new NBTTagCompound();
				nbt1.setByte("slot", (byte) i);
				slots[i].writeToNBT(nbt1);
				list.appendTag(nbt1);
			}
		}
		nbt.setTag("items", list);
		nbt.setBoolean("spiders", hasSpiders);

		if (customName != null) {
			nbt.setString("name", customName);
		}
		return nbt;
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		return new SPacketUpdateTileEntity(this.getPos(), 0, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
		readFromNBT(packet.getNbtCompound());
	}

	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		int[] slots = new int[this.slots.length];
		for(int i = 0; i < slots.length; i++) slots[i] = i;
		return slots;
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return this.isItemValidForSlot(i, itemStack) && !this.isLocked();
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return !this.isLocked();
	}

	// Spiders!!!
	public void fillWithSpiders() {
		this.hasSpiders = true;
	}

	private static final int numSpiders = 3; // leave that at 3 for now TODO: maybe a config option or smth

	/// For when opening from a TileEntity.
	public static void spawnSpiders(EntityPlayer player, World worldObj, TileEntityCrateBase crate) {
		if(crate.hasSpiders) {
			Random random = new Random();

			for (int i = 0; i < numSpiders; i++) {

				EntityCaveSpider spider = new EntityCaveSpider(worldObj); // lord
				spider.setLocationAndAngles(crate.getPos().getX() + random.nextGaussian() * 2, crate.getPos().getY() + 1, crate.getPos().getZ() + random.nextGaussian() * 2, random.nextFloat(), 0);
				spider.setAttackTarget(player);

				worldObj.spawnEntityInWorld(spider);
			}
			crate.hasSpiders = false;
			crate.markDirty();
		}
	}

	/// For when opening from a player's inventory.
	public static void spawnSpiders(EntityPlayer player, World worldObj, ItemStack crate) {
		if(crate.hasTagCompound() && crate.getTagCompound().getBoolean("spiders")) {
			Random random = new Random();

			for (int i = 0; i < numSpiders; i++) {

				EntityCaveSpider spider = new EntityCaveSpider(worldObj);
				spider.setLocationAndAngles(player.posX + random.nextGaussian() * 2, player.posY + 1, player.posZ + random.nextGaussian() * 2, random.nextFloat(), 0);
				spider.setAttackTarget(player);

				worldObj.spawnEntityInWorld(spider);
			}
			crate.getTagCompound().removeTag("spiders");
		}
	}
}
