package com.hbm.tileentity.machine;

import com.hbm.config.VersatileConfig;
import com.hbm.inventory.container.ContainerMachineRTG;
import com.hbm.inventory.gui.GUIMachineRTG;
import com.hbm.items.machine.ItemRTGPellet;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.CompatEnergyControl;
import com.hbm.util.RTGUtil;

import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.tile.IInfoProviderEC;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class TileEntityMachineRTG extends TileEntityLoadedBase implements ISidedInventory, IEnergyProviderMK2, IGUIProvider, IInfoProviderEC, net.minecraft.util.ITickable {

	private ItemStack slots[];

	public int heat;
	public final int heatMax = VersatileConfig.rtgDecay() ? 600 : 200;
	public long power;
	public final long powerMax = 100000;

	public static final int[] slot_io = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 };

	private String customName;

	public TileEntityMachineRTG() {
		slots = new ItemStack[15];
	}

	/** Nullable domain storage stays private; native inventory boundaries expose EMPTY. */
	private static ItemStack nativeStack(ItemStack stack) {
		return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack;
	}

	@Override
	public boolean shouldRefresh(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos,
			net.minecraft.block.state.IBlockState oldState, net.minecraft.block.state.IBlockState newState) {
		// Legacy metadata-only changes retained the generator and its stored state.
		return oldState.getBlock() != newState.getBlock();
	}

	@Override public boolean isEmpty() {
		for(ItemStack stack : slots) if(!nativeStack(stack).isEmpty()) return false;
		return true;
	}
	@Override public int getField(int id) { return 0; }
	@Override public void setField(int id, int value) {}
	@Override public int getFieldCount() { return 0; }
	@Override public void clear() { java.util.Arrays.fill(slots, null); }
	@Override public net.minecraft.util.text.ITextComponent getDisplayName() {
		return hasCustomName() ? new net.minecraft.util.text.TextComponentString(getName()) : new net.minecraft.util.text.TextComponentTranslation(getName());
	}
	/** Compatibility for unmigrated GUI/container consumers. */
	public String getInventoryName() { return getName(); }
	public boolean hasCustomInventoryName() { return hasCustomName(); }

	@Override
	public int getSizeInventory() {
		return slots.length;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return nativeStack(slots[i]);
	}

	@Override
	public ItemStack removeStackFromSlot(int i) {
		if(!nativeStack(slots[i]).isEmpty())
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
		return this.hasCustomName() ? this.customName : "container.rtg";
	}

	@Override
	public boolean hasCustomName() {
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
	public boolean isUsableByPlayer(EntityPlayer player) {
		if(this.world.getTileEntity(this.pos) != this)
		{
			return false;
		}else{
			return player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <=64;
		}
	}

	//You scrubs aren't needed for anything (right now)
	@Override
	public void openInventory(EntityPlayer player) {}
	@Override
	public void closeInventory(EntityPlayer player) {}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return itemStack.getItem() instanceof ItemRTGPellet;
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		if(!nativeStack(slots[i]).isEmpty())
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

		power = nbt.getLong("power");
		heat = nbt.getInteger("heat");
		slots = new ItemStack[getSizeInventory()];

		for(int i = 0; i < list.tagCount(); i++)
		{
			NBTTagCompound nbt1 = list.getCompoundTagAt(i);
			byte b0 = nbt1.getByte("slot");
			if(b0 >= 0 && b0 < slots.length)
			{
				ItemStack stack = new ItemStack(nbt1);
				slots[b0] = stack.isEmpty() ? null : stack;
			}
		}

		customName = nbt.getString("name");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("power", power);
		nbt.setInteger("heat", heat);
		NBTTagList list = new NBTTagList();

		for(int i = 0; i < slots.length; i++) {
			if(!nativeStack(slots[i]).isEmpty()) {
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

	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int p_94128_1_ = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return slot_io;
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

	public long getPowerScaled(long i) {
		return (power * i) / powerMax;
	}

	public int getHeatScaled(int i) {
		return (heat * i) / heatMax;
	}

	public boolean hasPower() {
		return power > 0;
	}

	public boolean hasHeat() {
		return RTGUtil.hasHeat(slots, slot_io);
	}

	@Override
	public void update() {

		if(!this.world.isRemote) {

			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
				this.tryProvide(this.world, this.pos.getX() + dir.offsetX, this.pos.getY() + dir.offsetY, this.pos.getZ() + dir.offsetZ, dir);

			heat = RTGUtil.updateRTGs(slots, slot_io);

			if(heat > heatMax)
				heat = heatMax;

			power += heat * 5;
			if(power > powerMax)
				power = powerMax;

			networkPackNT(50);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public long getMaxPower() {
		return powerMax;
	}

	@Override
	public void setPower(long i) {
		this.power = i;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineRTG(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineRTG(player.inventory, this);
	}

	@Override
	public void provideExtraInfo(NBTTagCompound data) {
		data.setBoolean(CompatEnergyControl.B_ACTIVE, this.heat > 0);
		data.setDouble(CompatEnergyControl.D_OUTPUT_HE, heat * 5D);
	}
}
