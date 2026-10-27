package com.hbm.tileentity.bomb;

import java.util.List;

import com.hbm.entity.missile.EntityMissileCustom;
import com.hbm.handler.CompatHandler;
import com.hbm.handler.MissileStruct;
import com.hbm.inventory.container.ContainerLaunchTable;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.gui.GUIMachineLaunchTable;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.ItemCustomMissile;
import com.hbm.items.weapon.ItemCustomMissilePart;
import com.hbm.items.weapon.ItemCustomMissilePart.FuelType;
import com.hbm.items.weapon.ItemCustomMissilePart.PartSize;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.TEMissileMultipartPacket;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IRadarCommandReceiver;
import com.hbm.tileentity.TileEntityLoadedBase;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluid.IFluidStandardReceiver;
import api.hbm.item.IDesignatorItem;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import com.hbm.util.NullableInventoryStackNbt;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")})
public class TileEntityLaunchTable extends TileEntityLoadedBase implements ISidedInventory, IEnergyReceiverMK2, IFluidStandardReceiver, IGUIProvider, IBufPacketReceiver, IRadarCommandReceiver, CompatHandler.OCComponent, net.minecraft.util.ITickable {

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

	public long power;
	public static final long maxPower = 100000;
	public int solid;
	public static final int maxSolid = 100000;
	public FluidTank[] tanks;
	public PartSize padSize;
	public int height;

	public MissileStruct load;

	private static final int[] access = new int[] { 0 };

	private String customName;

	public TileEntityLaunchTable() {
		slots = new ItemStack[8];
		tanks = new FluidTank[2];
		tanks[0] = new FluidTank(Fluids.NONE, 100000);
		tanks[1] = new FluidTank(Fluids.NONE, 100000);
		padSize = PartSize.SIZE_10;
		height = 10;
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
		return this.hasCustomName() ? this.customName : "container.launchTable";
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

	public long getPowerScaled(long i) {
		return (power * i) / maxPower;
	}

	public int getSolidScaled(int i) {
		return (solid * i) / maxSolid;
	}

	@Override
	public void update() {

		if (!this.world.isRemote) {

			updateTypes();

			if(this.world.getTotalWorldTime() % 20 == 0)
				this.updateConnections();

			tanks[0].loadTank(2, 6, slots);
			tanks[1].loadTank(3, 7, slots);

			power = Library.chargeTEFromItems(slots, 5, power, maxPower);

			if(slots[4] != null && slots[4].getItem() == ModItems.rocket_fuel && solid + 250 <= maxSolid) {

				this.decrStackSize(4, 1);
				solid += 250;
			}

			networkPackNT(50);

			MissileStruct multipart = getStruct(slots[0]);

			if(multipart != null)
				PacketDispatcher.wrapper.sendToAllAround(new TEMissileMultipartPacket(this.pos.getX(), this.pos.getY(), this.pos.getZ(), multipart), new TargetPoint(this.world.provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 250));
			else
				PacketDispatcher.wrapper.sendToAllAround(new TEMissileMultipartPacket(this.pos.getX(), this.pos.getY(), this.pos.getZ(), new MissileStruct()), new TargetPoint(this.world.provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 250));

			outer:
			for(int x = -4; x <= 4; x++) {
				for(int z = -4; z <= 4; z++) {

					if(this.world.isBlockPowered(new net.minecraft.util.math.BlockPos(this.pos.getX() + x, this.pos.getY(), this.pos.getZ() + z)) && canLaunch()) {
						launchFromDesignator();
						break outer;
					}
				}
			}
		} else {

			List<EntityMissileCustom> entities = this.world.getEntitiesWithinAABB(EntityMissileCustom.class, new AxisAlignedBB(this.pos.getX() - 0.5, this.pos.getY(), this.pos.getZ() - 0.5, this.pos.getX() + 1.5, this.pos.getY() + 10, this.pos.getZ() + 1.5));

			if(!entities.isEmpty()) {
				for(int i = 0; i < 15; i++) {

					boolean dir = this.world.rand.nextBoolean();
					float moX = (float) (dir ? 0 : this.world.rand.nextGaussian() * 0.65F);
					float moZ = (float) (!dir ? 0 : this.world.rand.nextGaussian() * 0.65F);

					NBTTagCompound data = new NBTTagCompound();
					data.setDouble("posX", this.pos.getX() + 0.5);
					data.setDouble("posY", this.pos.getY() + 0.25);
					data.setDouble("posZ", this.pos.getZ() + 0.5);
					data.setString("type", "launchSmoke");
					data.setDouble("moX", moX);
					data.setDouble("moY", 0);
					data.setDouble("moZ", moZ);
					MainRegistry.proxy.effectNT(data);
				}
			}
		}
	}

	@Override public void serialize(ByteBuf buf) {
		buf.writeLong(power);
		buf.writeInt(solid);
		buf.writeByte((byte) padSize.ordinal());
		tanks[0].serialize(buf);
		tanks[1].serialize(buf);
	}

	@Override public void deserialize(ByteBuf buf) {
		this.power = buf.readLong();
		this.solid = buf.readInt();
		this.padSize = PartSize.values()[buf.readByte()];
		tanks[0].deserialize(buf);
		tanks[1].deserialize(buf);
	}

	private void updateConnections() {

		for(int i = -4; i <= 4; i++) {
			this.trySubscribe(this.world, this.pos.getX() + i, this.pos.getY(), this.pos.getZ() + 5, Library.POS_Z);
			this.trySubscribe(this.world, this.pos.getX() + i, this.pos.getY(), this.pos.getZ() - 5, Library.NEG_Z);
			this.trySubscribe(this.world, this.pos.getX() + 5, this.pos.getY(), this.pos.getZ() + i, Library.POS_X);
			this.trySubscribe(this.world, this.pos.getX() - 5, this.pos.getY(), this.pos.getZ() + i, Library.NEG_X);

			for(int j = 0; j < 2; j++) {
				this.trySubscribe(tanks[j].getTankType(), this.world, this.pos.getX() + i, this.pos.getY(), this.pos.getZ() + 5, Library.POS_Z);
				this.trySubscribe(tanks[j].getTankType(), this.world, this.pos.getX() + i, this.pos.getY(), this.pos.getZ() - 5, Library.NEG_Z);
				this.trySubscribe(tanks[j].getTankType(), this.world, this.pos.getX() + 5, this.pos.getY(), this.pos.getZ() + i, Library.POS_X);
				this.trySubscribe(tanks[j].getTankType(), this.world, this.pos.getX() - 5, this.pos.getY(), this.pos.getZ() + i, Library.NEG_X);
			}
		}
	}

	public boolean canLaunch() {

		if(power >= maxPower * 0.75 && isMissileValid() && hasFuel())
			return true;

		return false;
	}

	@Override
	public boolean sendCommandEntity(Entity target) {
		return sendCommandPosition((int) Math.floor(target.posX), this.pos.getY(), (int) Math.floor(target.posX));
	}

	@Override
	public boolean sendCommandPosition(int x, int y, int z) {
		if(!canLaunch()) return false;
		this.launchTo(x, z);
		return true;
	}

	public void launchFromDesignator() {

		if(slots[1] != null && slots[1].getItem() instanceof IDesignatorItem) {
			IDesignatorItem designator = (IDesignatorItem) slots[1].getItem();

			if(designator.isReady(this.world, slots[1], this.pos.getX(), this.pos.getY(), this.pos.getZ())) {
				Vec3d coords = designator.getCoords(this.world, slots[1], this.pos.getX(), this.pos.getY(), this.pos.getZ());
				int tX = (int) Math.floor(coords.x);
				int tZ = (int) Math.floor(coords.z);

				this.launchTo(tX, tZ);
			}
		}
	}

	public void launchTo(int tX, int tZ) {

		MissileStruct struct = getStruct(slots[0]);

		if(struct == null)
			return;

		com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), "hbm:weapon.missileTakeOff", 10.0F, 1.0F);

		ItemCustomMissilePart chip = ItemCustomMissile.readPart(slots[0], "chip");
		float c = 1.0F;

		if(chip != null)
			c = (Float) chip.attributes[0];

		float f = 1.0F;

		if(struct.fins != null)
			f = (Float) struct.fins.attributes[0];

		Vec3d target = new Vec3d(this.pos.getX() - tX, 0, this.pos.getZ() - tZ);
		target = new Vec3d(target.x * c * f, target.y, target.z * c * f);

		target = target.rotateYaw(this.world.rand.nextFloat() * 360);

		EntityMissileCustom missile = new EntityMissileCustom(this.world, this.pos.getX() + 0.5F, this.pos.getY() + 2.5F, this.pos.getZ() + 0.5F, tX + (int)target.x, tZ + (int)target.z, struct);
		this.world.spawnEntity(missile);

		subtractFuel();

		slots[0] = null;
	}

	private boolean hasFuel() {

		return solidState() != 0 && liquidState() != 0 && oxidizerState() != 0;
	}

	private void subtractFuel() {

		MissileStruct multipart = getStruct(slots[0]);

		if(multipart == null || multipart.fuselage == null)
			return;

		ItemCustomMissilePart fuselage = (ItemCustomMissilePart)multipart.fuselage;

		int fuel = (int)fuselage.attributes[1];

		switch((FuelType)fuselage.attributes[0]) {
			case KEROSENE:
				tanks[0].setFill(tanks[0].getFill() - fuel);
				tanks[1].setFill(tanks[1].getFill() - fuel);
				break;
			case HYDROGEN:
				tanks[0].setFill(tanks[0].getFill() - fuel);
				tanks[1].setFill(tanks[1].getFill() - fuel);
				break;
			case XENON:
				tanks[0].setFill(tanks[0].getFill() - fuel);
				break;
			case BALEFIRE:
				tanks[0].setFill(tanks[0].getFill() - fuel);
				tanks[1].setFill(tanks[1].getFill() - fuel);
				break;
			case SOLID:
				this.solid -= fuel; break;
			default: break;
		}

		this.power -= maxPower * 0.75;
	}

	public static MissileStruct getStruct(ItemStack stack) {

		return ItemCustomMissile.getStruct(stack);
	}

	public boolean isMissileValid() {

		MissileStruct multipart = getStruct(slots[0]);

		if(multipart == null || multipart.fuselage == null)
			return false;

		ItemCustomMissilePart fuselage = (ItemCustomMissilePart)multipart.fuselage;

		return fuselage.top == padSize;
	}

	public boolean hasDesignator() {

		if(slots[1] != null && slots[1].getItem() instanceof IDesignatorItem && ((IDesignatorItem)slots[1].getItem()).isReady(this.world, slots[1], this.pos.getX(), this.pos.getY(), this.pos.getZ())) {
			return true;
		}

		return false;
	}

	public int solidState() {

		MissileStruct multipart = getStruct(slots[0]);

		if(multipart == null || multipart.fuselage == null)
			return -1;

		ItemCustomMissilePart fuselage = (ItemCustomMissilePart)multipart.fuselage;

		if((FuelType)fuselage.attributes[0] == FuelType.SOLID) {

			if(solid >= (int)fuselage.attributes[1])
				return 1;
			else
				return 0;
		}

		return -1;
	}

	public int liquidState() {

		MissileStruct multipart = getStruct(slots[0]);

		if(multipart == null || multipart.fuselage == null)
			return -1;

		ItemCustomMissilePart fuselage = (ItemCustomMissilePart)multipart.fuselage;

		switch((FuelType)fuselage.attributes[0]) {
			case KEROSENE:
			case HYDROGEN:
			case XENON:
			case BALEFIRE:

				if(tanks[0].getFill() >= (int)fuselage.attributes[1])
					return 1;
				else
					return 0;
			default: break;
		}

		return -1;
	}

	public int oxidizerState() {

		MissileStruct multipart = getStruct(slots[0]);

		if(multipart == null || multipart.fuselage == null)
			return -1;

		ItemCustomMissilePart fuselage = (ItemCustomMissilePart)multipart.fuselage;

		switch((FuelType)fuselage.attributes[0]) {
			case KEROSENE:
			case HYDROGEN:
			case BALEFIRE:

				if(tanks[1].getFill() >= (int)fuselage.attributes[1])
					return 1;
				else
					return 0;
			default: break;
		}

		return -1;
	}

	public void updateTypes() {

		MissileStruct multipart = getStruct(slots[0]);

		if(multipart == null || multipart.fuselage == null)
			return;

		ItemCustomMissilePart fuselage = (ItemCustomMissilePart)multipart.fuselage;

		switch((FuelType)fuselage.attributes[0]) {
			case KEROSENE:
				tanks[0].setTankType(Fluids.KEROSENE);
				tanks[1].setTankType(Fluids.PEROXIDE);
				break;
			case HYDROGEN:
				tanks[0].setTankType(Fluids.HYDROGEN);
				tanks[1].setTankType(Fluids.OXYGEN);
				break;
			case XENON:
				tanks[0].setTankType(Fluids.XENON);
				break;
			case BALEFIRE:
				tanks[0].setTankType(Fluids.BALEFIRE);
				tanks[1].setTankType(Fluids.PEROXIDE);
				break;
			default: break;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		NBTTagList list = nbt.getTagList("items", 10);

		tanks[0].readFromNBT(nbt, "fuel");
		tanks[1].readFromNBT(nbt, "oxidizer");
		solid = nbt.getInteger("solidfuel");
		power = nbt.getLong("power");
		padSize = PartSize.values()[nbt.getInteger("padSize")];

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

		tanks[0].writeToNBT(nbt, "fuel");
		tanks[1].writeToNBT(nbt, "oxidizer");
		nbt.setInteger("solidfuel", solid);
		nbt.setLong("power", power);
		nbt.setInteger("padSize", padSize.ordinal());

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
	public void setPower(long i) {
		this.power = i;
	}

	@Override
	public long getPower() {
		return this.power;
	}

	@Override
	public long getMaxPower() {
		return this.maxPower;
	}

	@Override
	public long transferPower(long power) {

		this.power += power;

		if(this.power > this.getMaxPower()) {

			long overshoot = this.power - this.getMaxPower();
			this.power = this.getMaxPower();
			return overshoot;
		}

		return 0;
	}

	@Override
	public boolean canConnect(ForgeDirection dir) {
		return dir != ForgeDirection.UP && dir != ForgeDirection.DOWN && dir != ForgeDirection.UNKNOWN;
	}

	@Override
	public boolean canConnect(FluidType type, ForgeDirection dir) {
		return dir != ForgeDirection.UP && dir != ForgeDirection.DOWN && dir != ForgeDirection.UNKNOWN;
	}

	@Override
	public FluidTank[] getAllTanks() {
		return tanks;
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return tanks;
	}

	// do some opencomputer stuff
	@Override
	@Optional.Method(modid = "OpenComputers")
	public String getComponentName() {
		return "ntm_custom_launch_pad";
	}

	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] getEnergyInfo(Context context, Arguments args) {
		return new Object[] {getPower(), getMaxPower()};
	}

	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] getContents(Context context, Arguments args) {
		return new Object[] {
				tanks[0].getFill(), tanks[0].getMaxFill(), tanks[0].getTankType().getUnlocalizedName(),
				tanks[1].getFill(), tanks[1].getMaxFill(), tanks[1].getTankType().getUnlocalizedName(),
				solid, maxSolid
		};
	}

	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] getLaunchInfo(Context context, Arguments args) {
		return new Object[] {canLaunch(), isMissileValid(), hasDesignator(), hasFuel()};
	}

	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] getCoords(Context context, Arguments args) {
		if (slots[1] != null && slots[1].getItem() instanceof IDesignatorItem) {
			int xCoord2;
			int zCoord2;
			if (slots[1].getTagCompound() != null) {
				xCoord2 = slots[1].getTagCompound().getInteger("xCoord");
				zCoord2 = slots[1].getTagCompound().getInteger("zCoord");
			} else
				return new Object[] {false};

			return new Object[] {xCoord2, zCoord2};
		}
		return new Object[] {false, "Designator not found"};
	}
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] setCoords(Context context, Arguments args) {
		if (slots[1] != null && slots[1].getItem() instanceof IDesignatorItem) {
			slots[1].setTagCompound(new NBTTagCompound());
			slots[1].getTagCompound().setInteger("xCoord", args.checkInteger(0));
			slots[1].getTagCompound().setInteger("zCoord", args.checkInteger(1));

			return new Object[] {true};
		}
		return new Object[] {false, "Designator not found"};
	}

	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] launch(Context context, Arguments args) {
		if(this.canLaunch()) {
			this.launchFromDesignator();
			return new Object[] {true};
		}
		return new Object[] {false};
	}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public String[] methods() {
		return new String[] {
				"getEnergyInfo",
				"getContents",
				"getLaunchInfo",
				"getCoords",
				"setCoords",
				"launch"
		};
	}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public Object[] invoke(String method, Context context, Arguments args) throws Exception {
		switch(method) {
			case ("getEnergyInfo"):
				return getEnergyInfo(context, args);
			case ("getContents"):
				return getContents(context, args);
			case ("getLaunchInfo"):
				return getLaunchInfo(context, args);
			case ("getCoords"):
				return getCoords(context, args);
			case ("setCoords"):
				return setCoords(context, args);
			case ("launch"):
				return launch(context, args);
	}
	throw new NoSuchMethodException();
}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerLaunchTable(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineLaunchTable(player.inventory, this);
	}
}
