package com.hbm.tileentity.machine;

import java.util.Arrays;

import com.hbm.inventory.container.ContainerMachineSiren;
import com.hbm.inventory.gui.GUIMachineSiren;
import com.hbm.items.machine.ItemCassette;
import com.hbm.items.machine.ItemCassette.SoundType;
import com.hbm.items.machine.ItemCassette.TrackType;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.TESirenPacket;
import com.hbm.tileentity.IGUIProvider;

import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TileEntityMachineSiren extends TileEntity implements ISidedInventory, IGUIProvider, net.minecraft.util.ITickable {

	private ItemStack slots[];
	
	private static final int[] slots_top = new int[] { 0 };
	private static final int[] slots_bottom = new int[] { 0 };
	private static final int[] slots_side = new int[] { 0 };
	
	public boolean lock = false;
	
	private String customName;
	
	public TileEntityMachineSiren() {
		slots = new ItemStack[1];
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
	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.customName : "container.siren";
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
		return 64;
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
	
	//You scrubs aren't needed for anything (right now)
	@Override
	public void openInventory() {}
	@Override
	public void closeInventory() {}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		
		return false;
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
		return false;
	}

	@Override
	public void update() {
		
		if(!this.world.isRemote) {
			int id = Arrays.asList(TrackType.values()).indexOf(getCurrentType());
			
			if(getCurrentType().name().equals(TrackType.NULL.name())) {
				PacketDispatcher.wrapper.sendToAllAround(new TESirenPacket(this.pos.getX(), this.pos.getY(), this.pos.getZ(), id, false), new TargetPoint(this.world.provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 1500));
				return;
			}
			
			boolean active = this.world.isBlockIndirectlyGettingPowered(this.pos.getX(), this.pos.getY(), this.pos.getZ());
			
			if(getCurrentType().getType().name().equals(SoundType.LOOP.name())) {
				
				PacketDispatcher.wrapper.sendToAllAround(new TESirenPacket(this.pos.getX(), this.pos.getY(), this.pos.getZ(), id, active), new TargetPoint(this.world.provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 1500));
			} else {
				
				if(!lock && active) {
					lock = true;
					PacketDispatcher.wrapper.sendToAllAround(new TESirenPacket(this.pos.getX(), this.pos.getY(), this.pos.getZ(), id, false), new TargetPoint(this.world.provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 1500));
					PacketDispatcher.wrapper.sendToAllAround(new TESirenPacket(this.pos.getX(), this.pos.getY(), this.pos.getZ(), id, true), new TargetPoint(this.world.provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 1500));
				}
				
				if(lock && !active) {
					lock = false;
				}
			}
		}
	}
	
	public TrackType getCurrentType() {
		if(slots[0] != null && slots[0].getItem() instanceof ItemCassette) {
			return TrackType.getEnum(slots[0].getItemDamage());
		}
		
		return TrackType.NULL;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineSiren(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineSiren(player.inventory, this);
	}
}
