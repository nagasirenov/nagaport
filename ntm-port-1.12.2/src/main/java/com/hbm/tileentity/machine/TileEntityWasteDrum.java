package com.hbm.tileentity.machine;

import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.container.ContainerWasteDrum;
import com.hbm.inventory.gui.GUIWasteDrum;
import com.hbm.inventory.recipes.FuelPoolRecipes;
import com.hbm.items.machine.ItemRBMKRod;
import com.hbm.tileentity.IGUIProvider;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import com.hbm.util.NullableInventoryStackNbt;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class TileEntityWasteDrum extends TileEntity implements ISidedInventory, IGUIProvider, net.minecraft.util.ITickable {

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

	private ItemStack slots[];

	private static final int[] slots_arr = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };

	private String customName;

	public TileEntityWasteDrum() {
		slots = new ItemStack[12];
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
		return this.hasCustomName() ? this.customName : "container.wasteDrum";
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
		return 1;
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
		return FuelPoolRecipes.recipes.keySet().contains(new ComparableStack(itemStack)) || itemStack.getItem() instanceof ItemRBMKRod;
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
			return ItemStack.EMPTY;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		NBTTagList list = nbt.getTagList("items", 10);

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
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int p_94128_1_ = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return slots_arr;
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return this.isItemValidForSlot(i, itemStack);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		if(itemStack.getItem() instanceof ItemRBMKRod) {
			return ItemRBMKRod.getCoreHeat(itemStack) < 50 && ItemRBMKRod.getHullHeat(itemStack) < 50;
		} else {
			return !FuelPoolRecipes.recipes.containsKey(new ComparableStack(getStackInSlot(i)));
		}
	}

	@Override
	public void update() {

		if(!this.world.isRemote) {

			int water = 0;

			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				if(com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + dir.offsetX, this.pos.getY() + dir.offsetY, this.pos.getZ() + dir.offsetZ).getBlock() == Blocks.WATER || com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + dir.offsetX, this.pos.getY() + dir.offsetY, this.pos.getZ() + dir.offsetZ).getBlock() == Blocks.FLOWING_WATER) {
					water++;
				}
			}

			if(water > 0) {

				int r = 60 * 60 * 20 / water;

				for(int i = 0; i < 12; i++) {

					if(slots[i] != null) {

						if(slots[i].getItem() instanceof ItemRBMKRod) {

							ItemRBMKRod rod = (ItemRBMKRod) slots[i].getItem();
							rod.updateHeat(this.world, slots[i], 0.025D);
							rod.provideHeat(this.world, slots[i], 20D, 0.025D);

						} else if(this.world.rand.nextInt(r) == 0) {

							ComparableStack comp = new ComparableStack(getStackInSlot(i));
							if(FuelPoolRecipes.recipes.containsKey(comp)) {
								slots[i] = FuelPoolRecipes.recipes.get(comp).copy();
							}
						}
					}
				}
			}
		}
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerWasteDrum(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIWasteDrum(player.inventory, this);
	}
}
