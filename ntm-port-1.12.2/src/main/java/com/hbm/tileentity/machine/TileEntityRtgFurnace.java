package com.hbm.tileentity.machine;

import com.hbm.blocks.machine.MachineRtgFurnace;
import com.hbm.inventory.container.ContainerRtgFurnace;
import com.hbm.inventory.gui.GUIRtgFurnace;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.RTGUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import com.hbm.util.NullableInventoryStackNbt;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TileEntityRtgFurnace extends TileEntity implements ISidedInventory, IGUIProvider, net.minecraft.util.ITickable {

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
    @Override
    public boolean shouldRefresh(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos,
            net.minecraft.block.state.IBlockState oldState, net.minecraft.block.state.IBlockState newState) {
        // Legacy metadata-only orientation changes retained inventory and all tile state.
        return oldState.getBlock() != newState.getBlock();
    }


	private ItemStack slots[];

	public int dualCookTime;
	public static final int processingSpeed = 1000;

	private static final int[] slots_top = new int[] {0};
	private static final int[] slots_bottom = new int[] {4};
	private static final int[] slots_side = new int[] {1, 2, 3};

	private String customName;

	public TileEntityRtgFurnace() {
		slots = new ItemStack[5];
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
		if(slots[i] != null)
		{
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
		if(itemStack != null && itemStack.getCount() > getInventoryStackLimit())
		{
			itemStack.setCount(getInventoryStackLimit());
		}
	}

	@Override
	public String getName() {
		return this.hasCustomName() ? this.customName : "container.rtgFurnace";
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
		if(this.world.getTileEntity(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ())) != this)
		{
			return false;
		}else{
			return player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <=64;
		}
	}



	//You scrubs aren't needed for anything (right now)
	@Override
	public void openInventory(net.minecraft.entity.player.EntityPlayer viewer) {}
	@Override
	public void closeInventory(net.minecraft.entity.player.EntityPlayer viewer) {}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return true;
	}

	public boolean isLoaded() {
		return RTGUtil.hasHeat(slots, slots_side);
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		if(slots[i] != null)
		{
			if(slots[i].getCount() <= j)
			{
				ItemStack itemStack = slots[i];
				slots[i] = null;
				return itemStack;
			}
			ItemStack itemStack1 = slots[i].splitStack(j);
			if (slots[i].getCount() == 0)
			{
				slots[i] = null;
			}

			return itemStack1;
		} else {
			return null;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		NBTTagList list = nbt.getTagList("items", 10);

		dualCookTime = nbt.getShort("CookTime");
		slots = new ItemStack[getSizeInventory()];

		for(int i = 0; i < list.tagCount(); i++)
		{
			NBTTagCompound nbt1 = list.getCompoundTagAt(i);
			byte b0 = nbt1.getByte("slot");
			if(b0 >= 0 && b0 < slots.length)
			{
				slots[b0] = NullableInventoryStackNbt.read(nbt1);
			}
		}

		customName = nbt.getString("name");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setShort("cookTime", (short) dualCookTime);
		NBTTagList list = new NBTTagList();

		for(int i = 0; i < slots.length; i++)
		{
			if(slots[i] != null)
			{
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
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace)
    {
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
		return j != 0 || i != 1 || itemStack.getItem() == Items.BUCKET;
	}

	public int getDiFurnaceProgressScaled(int i) {
		return (dualCookTime * i) / processingSpeed;
	}

	public boolean canProcess() {
		if(slots[0] == null)
		{
			return false;
		}
        ItemStack itemStack = FurnaceRecipes.instance().getSmeltingResult(this.slots[0]);
		if(itemStack == null)
		{
			return false;
		}

		if(slots[4] == null)
		{
			return true;
		}

		if(!slots[4].isItemEqual(itemStack)) {
			return false;
		}

		if(slots[4].getCount() < getInventoryStackLimit() && slots[4].getCount() < slots[4].getMaxStackSize()) {
			return true;
		}else{
			return slots[4].getCount() < itemStack.getMaxStackSize();
		}
	}

	private void processItem() {
		if(canProcess()) {
	        ItemStack itemStack = FurnaceRecipes.instance().getSmeltingResult(this.slots[0]);

			if(slots[4] == null)
			{
				slots[4] = itemStack.copy();
			}else if(slots[4].isItemEqual(itemStack)) {
				slots[4].grow(itemStack.getCount());
			}

			for(int i = 0; i < 1; i++)
			{
				if(slots[i].getCount() <= 0)
				{
					slots[i] = new ItemStack(slots[i].getItem());
				}else{
					slots[i].shrink(1);
				}
				if(slots[i].getCount() <= 0)
				{
					slots[i] = null;
				}
			}
		}
	}

	public boolean hasPower() {
		return isLoaded();
	}

	public boolean isProcessing() {
		return this.dualCookTime > 0;
	}

	@Override
	public void update() {
		this.hasPower();
		boolean flag1 = false;

		if(!this.world.isRemote)
		{
			if(hasPower() && canProcess())
			{
				dualCookTime += RTGUtil.updateRTGs(slots, slots_side);

				if(this.dualCookTime >= TileEntityRtgFurnace.processingSpeed)
				{
					this.dualCookTime = 0;
					this.processItem();
					flag1 = true;
				}
			}else{
				dualCookTime = 0;
				RTGUtil.updateRTGs(slots, slots_side);
			}

			boolean trigger = true;

			if(hasPower() && canProcess() && this.dualCookTime == 0)
			{
				trigger = false;
			}

			if(trigger)
            {
                flag1 = true;
                MachineRtgFurnace.updateBlockState(this.dualCookTime > 0, this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());
            }
		}

		if(flag1)
		{
			this.markDirty();
		}
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerRtgFurnace(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIRtgFurnace(player.inventory, this);
	}
}
