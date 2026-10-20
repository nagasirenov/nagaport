package com.hbm.tileentity.bomb;

import java.util.List;

import com.hbm.entity.missile.EntityMissileCustom;
import com.hbm.handler.MissileStruct;
import com.hbm.inventory.container.ContainerCompactLauncher;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.gui.GUIMachineCompactLauncher;
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
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluid.IFluidStandardReceiver;
import api.hbm.item.IDesignatorItem;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
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

public class TileEntityCompactLauncher extends TileEntityLoadedBase implements ISidedInventory, IEnergyReceiverMK2, IFluidStandardReceiver, IGUIProvider, IBufPacketReceiver, IRadarCommandReceiver, net.minecraft.util.ITickable {

	public ItemStack slots[];

	public long power;
	public static final long maxPower = 100000;
	public int solid;
	public static final int maxSolid = 25000;
	public FluidTank[] tanks;

	public MissileStruct load;

	private static final int[] access = new int[] { 0 };

	private String customName;

	public TileEntityCompactLauncher() {
		slots = new ItemStack[8];
		tanks = new FluidTank[2];
		tanks[0] = new FluidTank(Fluids.NONE, 25000);
		tanks[1] = new FluidTank(Fluids.NONE, 25000);
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
	}

	@Override
	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.customName : "container.compactLauncher";
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
			if (slots[i].stackSize <= j) {
				ItemStack itemStack = slots[i];
				slots[i] = null;
				return itemStack;
			}
			ItemStack itemStack1 = slots[i].splitStack(j);
			if (slots[i].stackSize == 0) {
				slots[i] = null;
			}

			return itemStack1;
		} else {
			return null;
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

			tanks[0].loadTank(2, 6, slots);
			tanks[1].loadTank(3, 7, slots);

			power = Library.chargeTEFromItems(slots, 5, power, maxPower);

			if(slots[4] != null && slots[4].getItem() == ModItems.rocket_fuel && solid + 250 <= maxSolid) {

				this.decrStackSize(4, 1);
				solid += 250;
			}

			if(this.world.getTotalWorldTime() % 20 == 0)
				this.updateConnections();

			networkPackNT(50);

			MissileStruct multipart = getStruct(slots[0]);

			if(multipart != null)
				PacketDispatcher.wrapper.sendToAllAround(new TEMissileMultipartPacket(this.pos.getX(), this.pos.getY(), this.pos.getZ(), multipart), new TargetPoint(this.world.provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 250));
			else
				PacketDispatcher.wrapper.sendToAllAround(new TEMissileMultipartPacket(this.pos.getX(), this.pos.getY(), this.pos.getZ(), new MissileStruct()), new TargetPoint(this.world.provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 250));

			outer:
			for(int x = -1; x <= 1; x++) {
				for(int z = -1; z <= 1; z++) {

					if(this.world.isBlockPowered(new net.minecraft.util.math.BlockPos(this.pos.getX() + x, this.pos.getY(), this.pos.getZ() + z)) && canLaunch()) {
						launchFromDesignator();
						break outer;
					}
				}
			}
		} else {

			List<Entity> entities = this.world.getEntitiesWithinAABBExcludingEntity(null, AxisAlignedBB.getBoundingBox(this.pos.getX() - 0.5, this.pos.getY(), this.pos.getZ() - 0.5, this.pos.getX() + 1.5, this.pos.getY() + 10, this.pos.getZ() + 1.5));

			for(Entity e : entities) {

				if(e instanceof EntityMissileCustom) {

					for(int i = 0; i < 15; i++) {

						boolean dir = this.world.rand.nextBoolean();
						float moX = (float) (dir ? 0 : this.world.rand.nextGaussian() * 0.5F);
						float moZ = (float) (!dir ? 0 : this.world.rand.nextGaussian() * 0.5F);

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

					break;
				}
			}
		}
	}

	@Override public void serialize(ByteBuf buf) {
		buf.writeLong(power);
		buf.writeInt(solid);
		tanks[0].serialize(buf);
		tanks[1].serialize(buf);
	}

	@Override public void deserialize(ByteBuf buf) {
		this.power = buf.readLong();
		this.solid = buf.readInt();
		tanks[0].deserialize(buf);
		tanks[1].deserialize(buf);
	}

	private void updateConnections() {

		for(DirPos pos : getConPos()) {
			this.trySubscribe(this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
			this.trySubscribe(tanks[0].getTankType(), this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
			this.trySubscribe(tanks[1].getTankType(), this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
		}
	}

	public DirPos[] getConPos() {
		return new DirPos[] {
				new DirPos(this.pos.getX() + 2, this.pos.getY(), this.pos.getZ() + 1, Library.POS_X),
				new DirPos(this.pos.getX() + 2, this.pos.getY(), this.pos.getZ() - 1, Library.POS_X),
				new DirPos(this.pos.getX() - 2, this.pos.getY(), this.pos.getZ() + 1, Library.NEG_X),
				new DirPos(this.pos.getX() - 2, this.pos.getY(), this.pos.getZ() - 1, Library.NEG_X),
				new DirPos(this.pos.getX() + 1, this.pos.getY(), this.pos.getZ() + 2, Library.POS_Z),
				new DirPos(this.pos.getX() - 1, this.pos.getY(), this.pos.getZ() + 2, Library.POS_Z),
				new DirPos(this.pos.getX() + 1, this.pos.getY(), this.pos.getZ() - 2, Library.NEG_Z),
				new DirPos(this.pos.getX() - 1, this.pos.getY(), this.pos.getZ() - 2, Library.NEG_Z),
				new DirPos(this.pos.getX() + 1, this.pos.getY() - 1, this.pos.getZ() + 1, Library.NEG_Y),
				new DirPos(this.pos.getX() + 1, this.pos.getY() - 1, this.pos.getZ() - 1, Library.NEG_Y),
				new DirPos(this.pos.getX() - 1, this.pos.getY() - 1, this.pos.getZ() + 1, Library.NEG_Y),
				new DirPos(this.pos.getX() - 1, this.pos.getY() - 1, this.pos.getZ() - 1, Library.NEG_Y)
		};
	}

	public boolean canLaunch() {

		if(power >= maxPower * 0.75 && isMissileValid() && hasDesignator() && hasFuel())
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

		com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), "hbm:weapon.missileTakeOff", 10.0F, 1.0F);

		ItemCustomMissilePart chip = (ItemCustomMissilePart) Item.getItemById(ItemCustomMissile.readFromNBT(slots[0], "chip"));
		float c = (Float)chip.attributes[0];
		float f = 1.0F;

		if(getStruct(slots[0]).fins != null) {
			ItemCustomMissilePart fins = (ItemCustomMissilePart) Item.getItemById(ItemCustomMissile.readFromNBT(slots[0], "stability"));
			f = (Float) fins.attributes[0];
		}

		Vec3d target = new Vec3d(this.pos.getX() - tX, 0, this.pos.getZ() - tZ);
		target = new Vec3d(target.x * c * f, target.y, target.z * c * f);

		target = target.rotateYaw(this.world.rand.nextFloat() * 360);

		EntityMissileCustom missile = new EntityMissileCustom(this.world, this.pos.getX() + 0.5F, this.pos.getY() + 2.5F, this.pos.getZ() + 0.5F, tX + (int)target.x, tZ + (int)target.z, getStruct(slots[0]));
		this.world.spawnEntityInWorld(missile);

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

		int fuel = fuselage.getTankSize();

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
			case HYDRAZINE:
				tanks[0].setFill(tanks[0].getFill() - fuel);
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

		return fuselage.top == PartSize.SIZE_10;
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

			if(solid >= fuselage.getTankSize())
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
			case HYDRAZINE:

				if(tanks[0].getFill() >= fuselage.getTankSize())
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

				if(tanks[1].getFill() >= fuselage.getTankSize())
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
			case HYDRAZINE:
				tanks[0].setTankType(Fluids.HYDRAZINE);
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
		return dir != ForgeDirection.UP && dir != ForgeDirection.UNKNOWN;
	}

	@Override
	public boolean canConnect(FluidType type, ForgeDirection dir) {
		return dir != ForgeDirection.UP && dir != ForgeDirection.UNKNOWN;
	}

	@Override
	public FluidTank[] getAllTanks() {
		return tanks;
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return tanks;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerCompactLauncher(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineCompactLauncher(player.inventory, this);
	}
}
