package com.hbm.tileentity.machine;

import com.hbm.entity.missile.EntityMinerRocket;
import com.hbm.explosion.ExplosionNukeSmall;
import com.hbm.inventory.container.ContainerSatDock;
import com.hbm.inventory.gui.GUISatDock;
import com.hbm.itempool.ItemPool;
import com.hbm.items.ISatChip;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.saveddata.satellites.SatelliteMiner;
import com.hbm.tileentity.IGUIProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import com.hbm.util.NullableInventoryStackNbt;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import com.hbm.lib.WeightedRandomChestContent;
import net.minecraft.world.World;

import java.util.List;

public class TileEntityMachineSatDock extends TileEntity implements ISidedInventory, IGUIProvider, net.minecraft.util.ITickable {

	@Override
	public boolean isEmpty() {
		for(int i = 0; i < this.getSizeInventory(); i++) {
			if(this.getStackInSlot(i) != null && !this.getStackInSlot(i).isEmpty()) return false;
		}
		return true;
	}

	@Override public int getField(int id) { return 0; }
	@Override public void setField(int id, int value) {}
	@Override public int getFieldCount() { return 0; }

	@Override
	public void clear() {
		for(int i = 0; i < this.getSizeInventory(); i++) {
		this.setInventorySlotContents(i, null);
		}
	}

	private ItemStack[] slots;
	private static final int[] access = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 };

	private String customName;
	private AxisAlignedBB renderBoundingBox;

	public TileEntityMachineSatDock() { slots = new ItemStack[16]; }

	@Override public int getSizeInventory() { return slots.length; }
	@Override public ItemStack getStackInSlot(int i) { ItemStack stack = slots[i]; return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack; }

	@Override
	public ItemStack removeStackFromSlot(int i) {
		ItemStack itemStack = slots[i];
		slots[i] = null;
		return itemStack == null || itemStack.isEmpty() ? ItemStack.EMPTY : itemStack;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemStack) {
		itemStack = itemStack == null || itemStack.isEmpty() ? null : itemStack;
		slots[i] = itemStack;
		if(itemStack != null && itemStack.getCount() > getInventoryStackLimit()) {
			itemStack.setCount(getInventoryStackLimit());
		}
	}

	@Override public String getName() { return this.hasCustomName() ? this.customName : "container.satDock"; }
	@Override public boolean hasCustomName() { return this.customName != null && this.customName.length() > 0; }
	public String getInventoryName() { return getName(); }
	public boolean hasCustomInventoryName() { return hasCustomName(); }
	public void setCustomName(String name) { this.customName = name; markDirty(); }
	@Override public int getInventoryStackLimit() { return 64; }

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		if(this.world.getTileEntity(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ())) != this) {
			return false;
		} else {
			return player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64;
		}
	}



	@Override public void openInventory(net.minecraft.entity.player.EntityPlayer viewer) { }
	@Override public void closeInventory(net.minecraft.entity.player.EntityPlayer viewer) { }

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) { return i == 15; }

	@Override
	public ItemStack decrStackSize(int i, int j) {
		if(slots[i] != null) {
			if(slots[i].getCount() <= j) {
				ItemStack itemStack = slots[i];
				slots[i] = null;
				return itemStack;
			}
			ItemStack itemStack1 = slots[i].splitStack(j);
			if(slots[i].getCount() == 0) {
				slots[i] = null;
			}

			return itemStack1;
		} else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		NBTTagList list = nbt.getTagList("items", 10);

		slots = new ItemStack[getSizeInventory()];

		for(int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound nbt1 = list.getCompoundTagAt(i);
			byte b0 = nbt1.getByte("slot");
			if(b0 >= 0 && b0 < slots.length) {
				slots[b0] = NullableInventoryStackNbt.read(nbt1);
			}
		}

		customName = nbt.getString("name");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		NBTTagList list = new NBTTagList();

		for(int i = 0; i < slots.length; i++) {
			if(slots[i] != null) {
				NBTTagCompound nbt1 = new NBTTagCompound();
				nbt1.setByte("slot", (byte) i);
				slots[i].writeToNBT(nbt1);
				list.appendTag(nbt1);
			}
		}
		nbt.setTag("items", list);

		if (customName != null) {
			nbt.setString("name", customName);
		}
		return nbt;
	}

	@Override public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int p_94128_1_ = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal(); return access; }
	@Override public boolean canInsertItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal(); return this.isItemValidForSlot(i, itemStack); }
	@Override public boolean canExtractItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal(); return true; }

	@Override
	public void update() {
		if(!this.world.isRemote) {
			SatelliteSavedData data = SatelliteSavedData.getData(this.world, this.pos.getX(), this.pos.getZ());

			if(slots[15] != null) {
				int freq = ISatChip.getFreqS(slots[15]);

				Satellite sat = data.getSatFromFreq(freq);

				int delay = 10 * 60 * 1000;

				if(sat instanceof SatelliteMiner) {
					SatelliteMiner miner = (SatelliteMiner) sat;

					if(miner.lastOp + delay < System.currentTimeMillis()) {
						EntityMinerRocket rocket = new EntityMinerRocket(this.world);
						rocket.posX = this.pos.getX() + 0.5;
						rocket.posY = 300;
						rocket.posZ = this.pos.getZ() + 0.5;

						rocket.setSatelliteFrequency(freq);
						this.world.spawnEntity(rocket);
						miner.lastOp = System.currentTimeMillis();
						data.markDirty();
					}
				}
			}

			@SuppressWarnings("unchecked")
			List<EntityMinerRocket> list = this.world.getEntitiesWithinAABB(EntityMinerRocket.class,
					new AxisAlignedBB(this.pos.getX() - 0.25 + 0.5, this.pos.getY() + 0.75, this.pos.getZ() - 0.25 + 0.5, this.pos.getX() + 0.25 + 0.5, this.pos.getY() + 2, this.pos.getZ() + 0.25 + 0.5));

			for(EntityMinerRocket rocket : list) {
				if(slots[15] != null && ISatChip.getFreqS(slots[15]) != rocket.getSatelliteFrequency()) {
					rocket.setDead();
					ExplosionNukeSmall.explode(this.world, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, ExplosionNukeSmall.PARAMS_TOTS);
					break;
				}

				if(rocket.getFlightMode() == 1 && rocket.timer == 50) {
					Satellite sat = data.getSatFromFreq(ISatChip.getFreqS(slots[15]));
					if(sat != null) unloadCargo((SatelliteMiner) sat);
				}
			}

			ejectInto(this.pos.getX() + 2, this.pos.getY(), this.pos.getZ());
			ejectInto(this.pos.getX() - 2, this.pos.getY(), this.pos.getZ());
			ejectInto(this.pos.getX(), this.pos.getY(), this.pos.getZ() + 2);
			ejectInto(this.pos.getX(), this.pos.getY(), this.pos.getZ() - 2);
		}
	}

	private void unloadCargo(SatelliteMiner satellite) {
		int itemAmount = this.world.rand.nextInt(6) + 10;

		WeightedRandomChestContent[] cargo = ItemPool.getPool(satellite.getCargo());

		for(int i = 0; i < itemAmount; i++) {
			addToInv(ItemPool.getStack(cargo, this.world.rand));
		}
	}

	private void addToInv(ItemStack stack) {
		for(int i = 0; i < 15; i++) {
			if(slots[i] != null && slots[i].getItem() == stack.getItem() && slots[i].getItemDamage() == stack.getItemDamage() && slots[i].getCount() < slots[i].getMaxStackSize()) {
				int toAdd = Math.min(slots[i].getMaxStackSize() - slots[i].getCount(), stack.getCount());

				slots[i].grow(toAdd);
				stack.shrink(toAdd);

				if(stack.getCount() <= 0)
					return;
			}
		}

		for(int i = 0; i < 15; i++) {
			if(slots[i] == null) {
				slots[i] = new ItemStack(stack.getItem(), 1, stack.getItemDamage());
				return;
			}
		}
	}

	private void ejectInto(int x, int y, int z) {
		TileEntity te = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(te instanceof IInventory) {
			IInventory chest = (IInventory) te;

			for(int i = 0; i < 15; i++) {
				if(slots[i] != null) {
					for(int j = 0; j < chest.getSizeInventory(); j++) {
						ItemStack sta = slots[i].copy();
						sta.setCount(1);

						if(chest.getStackInSlot(j) != null && !chest.getStackInSlot(j).isEmpty() && chest.getStackInSlot(j).isItemEqual(slots[i]) && ItemStack.areItemStackTagsEqual(chest.getStackInSlot(j), slots[i])
								&& chest.getStackInSlot(j).getCount() < chest.getStackInSlot(j).getMaxStackSize()) {

							slots[i].shrink(1);

							if(slots[i].getCount() <= 0)
								slots[i] = null;

							chest.getStackInSlot(j).grow(1);
							return;
						}
					}
				}
			}

			for(int i = 0; i < 15; i++) {
				if(slots[i] != null) {
					for(int j = 0; j < chest.getSizeInventory(); j++) {
						ItemStack sta = slots[i].copy();
						sta.setCount(1);

						if((chest.getStackInSlot(j) == null || chest.getStackInSlot(j).isEmpty()) && chest.isItemValidForSlot(j, sta)) {
							slots[i].shrink(1);

							if(slots[i].getCount() <= 0)
								slots[i] = null;

							chest.setInventorySlotContents(j, sta);
							return;
						}
					}
				}
			}
		}
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if(renderBoundingBox == null) {
			renderBoundingBox = new AxisAlignedBB(this.pos.getX() - 1, this.pos.getY(), this.pos.getZ() - 1, this.pos.getX() + 2, this.pos.getY() + 1, this.pos.getZ() + 2);
		}

		return renderBoundingBox;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerSatDock(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUISatDock(player.inventory, this);
	}
}
