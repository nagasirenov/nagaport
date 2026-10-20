package com.hbm.tileentity.bomb;

import com.hbm.inventory.container.ContainerBombMulti;
import com.hbm.inventory.gui.GUIBombMulti;
import com.hbm.items.ModItems;
import com.hbm.tileentity.IGUIProvider;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class TileEntityBombMulti extends TileEntity implements ISidedInventory, IGUIProvider {

	public ItemStack slots[];
	private String customName;

	public TileEntityBombMulti() {
		slots = new ItemStack[6];
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
	public ItemStack decrStackSize(int i, int j) {
		if(slots[i] != null)
		{
			if(slots[i].stackSize <= j)
			{
				ItemStack itemStack = slots[i];
				slots[i] = null;
				return itemStack;
			}
			ItemStack itemStack1 = slots[i].splitStack(j);
			if (slots[i].stackSize == 0)
			{
				slots[i] = null;
			}
			
			return itemStack1;
		} else {
			return null;
		}
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
	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.customName : "container.bombMulti";
	}

	@Override
	public boolean hasCustomInventoryName() {
		return this.customName != null && this.customName.length() > 0;
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
	public boolean isUseableByPlayer(EntityPlayer player) {
		if(this.world.getTileEntity(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ())) != this)
		{
			return false;
		}else{
			return player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <=64;
		}
	}

	@Override
	public void openInventory() {
		
	}

	@Override
	public void closeInventory() {
		
	}

	@Override
	public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
		return false;
	}

	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int p_94128_1_ = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return new int[0];
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
				slots[b0] = ItemStack.loadItemStackFromNBT(nbt1);
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
	
	public boolean isLoaded(){
		
		if(slots[0] != null && slots[0].getItem() == Item.getItemFromBlock(Blocks.TNT) && 
				slots[1] != null && slots[1].getItem() == Item.getItemFromBlock(Blocks.TNT) && 
				slots[3] != null && slots[3].getItem() == Item.getItemFromBlock(Blocks.TNT) && 
				slots[4] != null && slots[4].getItem() == Item.getItemFromBlock(Blocks.TNT))
		{
			return true;
		}
			
		return false;
	}
	
	public int return2type() {

		if(slots[2] != null)
		{
		if(slots[2].getItem() == Items.GUNPOWDER)
		{
			return 1;
		}
		
		if(slots[2].getItem() == Item.getItemFromBlock(Blocks.TNT))
		{
			return 2;
		}
		
		if(slots[2].getItem() == ModItems.pellet_cluster)
		{
			return 3;
		}
		
		if(slots[2].getItem() == ModItems.powder_fire)
		{
			return 4;
		}
		
		if(slots[2].getItem() == ModItems.powder_poison)
		{
			return 5;
		}
		
		if(slots[2].getItem() == ModItems.pellet_gas)
		{
			return 6;
		}
		}
		return 0;
	}
	
	public int return5type() {
		
		if(slots[5] != null)
		{
		if(slots[5].getItem() == Items.GUNPOWDER)
		{
			return 1;
		}
		
		if(slots[5].getItem() == Item.getItemFromBlock(Blocks.TNT))
		{
			return 2;
		}
		
		if(slots[5].getItem() == ModItems.pellet_cluster)
		{
			return 3;
		}
		
		if(slots[5].getItem() == ModItems.powder_fire)
		{
			return 4;
		}
		
		if(slots[5].getItem() == ModItems.powder_poison)
		{
			return 5;
		}
		
		if(slots[5].getItem() == ModItems.pellet_gas)
		{
			return 6;
		}
		}
		return 0;
	}
	
	public void clearSlots() {
		for(int i = 0; i < slots.length; i++)
		{
			slots[i] = null;
		}
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerBombMulti(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIBombMulti(player.inventory, this);
	}
}