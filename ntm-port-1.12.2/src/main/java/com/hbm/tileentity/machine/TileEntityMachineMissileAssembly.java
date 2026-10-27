package com.hbm.tileentity.machine;

import com.hbm.handler.MissileStruct;
import com.hbm.inventory.container.ContainerMachineMissileAssembly;
import com.hbm.inventory.gui.GUIMachineMissileAssembly;
import com.hbm.items.weapon.ItemCustomMissile;
import com.hbm.items.weapon.ItemCustomMissilePart;
import com.hbm.items.weapon.ItemCustomMissilePart.FuelType;
import com.hbm.items.weapon.ItemCustomMissilePart.PartType;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.TEMissileMultipartPacket;
import com.hbm.tileentity.IGUIProvider;

import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class TileEntityMachineMissileAssembly extends TileEntity implements ISidedInventory, IGUIProvider, net.minecraft.util.ITickable {

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

	public MissileStruct load;

	private static final int[] access = new int[] { 0 };

	private String customName;

	public TileEntityMachineMissileAssembly() {
		slots = new ItemStack[6];
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
	}

	@Override
	public String getName() {
		return this.hasCustomName() ? this.customName : "container.missileAssembly";
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
		if (this.world.getTileEntity(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ())) != this) {
			return false;
		} else {
			return player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64;
		}
	}



	// You scrubs aren't needed for anything (right now)
	@Override
	public void openInventory(net.minecraft.entity.player.EntityPlayer viewer) {
	}

	@Override
	public void closeInventory(net.minecraft.entity.player.EntityPlayer viewer) {
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		return false;
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		if (slots[i] != null) {
			if (slots[i].getCount() <= j) {
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

		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound nbt1 = list.getCompoundTagAt(i);
			byte b0 = nbt1.getByte("slot");
			if (b0 >= 0 && b0 < slots.length) {
				slots[b0] = NullableInventoryStackNbt.read(nbt1);
			}
		}

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

		if (customName != null) {
			nbt.setString("name", customName);
		}
		return nbt;
	}

	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int p_94128_1_ = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return access;
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

			MissileStruct multipart = new MissileStruct(slots[1], slots[2], slots[3], slots[4]);

			PacketDispatcher.wrapper.sendToAllAround(new TEMissileMultipartPacket(this.pos.getX(), this.pos.getY(), this.pos.getZ(), multipart), new TargetPoint(this.world.provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 250));
		}
	}

	public int fuselageState() {

		if(slots[2] != null && slots[2].getItem() instanceof ItemCustomMissilePart) {

			ItemCustomMissilePart part = (ItemCustomMissilePart)slots[2].getItem();

			if(part.type == PartType.FUSELAGE)
				return 1;
		}

		return 0;
	}

	public int chipState() {

		if(slots[0] != null && slots[0].getItem() instanceof ItemCustomMissilePart) {

			ItemCustomMissilePart part = (ItemCustomMissilePart)slots[0].getItem();

			if(part.type == PartType.CHIP)
				return 1;
		}

		return 0;
	}

	public int warheadState() {

		if(slots[1] != null && slots[1].getItem() instanceof ItemCustomMissilePart &&
				slots[2] != null && slots[2].getItem() instanceof ItemCustomMissilePart &&
				slots[4] != null && slots[4].getItem() instanceof ItemCustomMissilePart) {

			ItemCustomMissilePart part = (ItemCustomMissilePart)slots[1].getItem();
			ItemCustomMissilePart fuselage = (ItemCustomMissilePart)slots[2].getItem();
			ItemCustomMissilePart thruster = (ItemCustomMissilePart)slots[4].getItem();

			if(part.type == PartType.WARHEAD && fuselage.type == PartType.FUSELAGE && thruster.type == PartType.THRUSTER) {
				float weight = (float)part.mass;
				float thrust = (float)thruster.attributes[2];

				if(part.bottom == fuselage.top && weight <= thrust)
					return 1;
			}
		}

		return 0;
	}

	public int stabilityState() {

		if(slots[3] == null)
			return -1;

		if(slots[3] != null && slots[3].getItem() instanceof ItemCustomMissilePart &&
				slots[2] != null && slots[2].getItem() instanceof ItemCustomMissilePart) {

			ItemCustomMissilePart part = (ItemCustomMissilePart)slots[3].getItem();
			ItemCustomMissilePart fuselage = (ItemCustomMissilePart)slots[2].getItem();

			if(part.top == fuselage.bottom && part.type == PartType.FINS)
				return 1;
		}

		return 0;
	}

	public int thrusterState() {

		if(slots[4] != null && slots[4].getItem() instanceof ItemCustomMissilePart &&
				slots[2] != null && slots[2].getItem() instanceof ItemCustomMissilePart) {

			ItemCustomMissilePart part = (ItemCustomMissilePart)slots[4].getItem();
			ItemCustomMissilePart fuselage = (ItemCustomMissilePart)slots[2].getItem();

			if(part.type == PartType.THRUSTER && fuselage.type == PartType.FUSELAGE &&
					part.top == fuselage.bottom && (FuelType)part.attributes[0] == (FuelType)fuselage.attributes[0]) {
				return 1;
			}
		}

		return 0;
	}

	public boolean canBuild() {

		if(slots[5] == null && chipState() == 1 && warheadState() == 1 && fuselageState() == 1 && thrusterState() == 1) {
			return stabilityState() != 0;
		}

		return false;
	}

	public void construct() {

		if(!canBuild())
			return;

		slots[5] = ItemCustomMissile.buildMissile(slots[0], slots[1], slots[2], slots[3], slots[4]).copy();

		if(stabilityState() == 1)
			slots[3] = null;

		slots[0] = null;
		slots[1] = null;
		slots[2] = null;
		slots[4] = null;

		com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), "hbm:block.missileAssembly2", 1F, 1F);
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
		return new ContainerMachineMissileAssembly(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineMissileAssembly(player.inventory, this);
	}
}
