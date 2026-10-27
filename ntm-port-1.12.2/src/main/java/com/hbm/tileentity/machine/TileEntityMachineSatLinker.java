package com.hbm.tileentity.machine;

import com.hbm.inventory.container.ContainerMachineSatLinker;
import com.hbm.inventory.gui.GUIMachineSatLinker;
import com.hbm.items.ISatChip;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.tileentity.IGUIProvider;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import com.hbm.util.NullableInventoryStackNbt;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TileEntityMachineSatLinker extends TileEntity implements ISidedInventory, IGUIProvider, net.minecraft.util.ITickable {

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

	//public static final int maxFill = 64 * 3;

	private static final int[] slots_top = new int[] {0};
	private static final int[] slots_bottom = new int[] {1};
	private static final int[] slots_side = new int[] {2};

	private String customName;

	public TileEntityMachineSatLinker() {
		slots = new ItemStack[3];
	}

	@Override
	public int getSizeInventory() {
		return slots.length;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		ItemStack stack = slots[i]; return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack;
	}

	@Override
	public ItemStack removeStackFromSlot(int i) {
		if(slots[i] != null) {
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
		if(itemStack != null && itemStack.getCount() > getInventoryStackLimit()) {
			itemStack.setCount(getInventoryStackLimit());
		}
	}

	@Override
	public String getName() {
		return this.hasCustomName() ? this.customName : "container.satLinker";
	}

	@Override
	public boolean hasCustomName() {
		return this.customName != null && this.customName.length() > 0;
	}

	public String getInventoryName() {
		return getName();
	}

	public boolean hasCustomInventoryName() {
		return hasCustomName();
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
		if(this.world.getTileEntity(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ())) != this) {
			return false;
		} else {
			return player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64;
		}
	}



	@Override
	public void openInventory(net.minecraft.entity.player.EntityPlayer viewer) {}
	@Override
	public void closeInventory(net.minecraft.entity.player.EntityPlayer viewer) {}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		return false;
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		if(slots[i] != null) {
			if(slots[i].getCount() <= j) {
				ItemStack itemStack = slots[i];
				slots[i] = null;
				return itemStack;
			}
			ItemStack itemStack1 = slots[i].splitStack(j);
			if (slots[i].getCount() == 0) {
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
				nbt1.setByte("slot", (byte)i);
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

	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int p_94128_1_ = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
        return p_94128_1_ == 0 ? slots_bottom : (p_94128_1_ == 1 ? slots_top : slots_side);
    }

	@Override
	public boolean canInsertItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return this.isItemValidForSlot(i, itemStack);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return true;
	}

	@Override
	public void update() {
		if(!this.world.isRemote) {
			if(slots[0] != null && slots[1] != null && slots[0].getItem() instanceof ISatChip && slots[1].getItem() instanceof ISatChip) {
				ISatChip.setFreqS(slots[1], ISatChip.getFreqS(slots[0]));
			}

			if(slots[2] != null && slots[2].getItem() instanceof ISatChip) {
				SatelliteSavedData satelliteData = SatelliteSavedData.getData(this.world, this.pos.getX(), this.pos.getZ());
				int newId = this.world.rand.nextInt(100000);
				if(!satelliteData.isFreqTaken(newId)) {
					ISatChip.setFreqS(slots[2], newId);
				}
			}
		}
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineSatLinker(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineSatLinker(player.inventory, this);
	}
}
