package com.hbm.tileentity.machine.storage;

import com.hbm.inventory.container.ContainerBatterySocket;
import com.hbm.inventory.gui.GUIBatterySocket;
import com.hbm.util.CompatEnergyControl;
import com.hbm.util.EnumUtil;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.redstoneoverradio.IRORInteractive;
import api.hbm.redstoneoverradio.IRORValueProvider;
import api.hbm.tile.IInfoProviderEC;
import net.minecraftforge.fml.common.Optional;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class TileEntityBatterySocket extends TileEntityBatteryBase implements IRORValueProvider, IRORInteractive, IInfoProviderEC {

	public long[] log = new long[20];
	public long delta = 0;

	public long syncPower = 0;
	public long syncMaxPower = 0;
	public ItemStack syncStack;
	
	public TileEntityBatterySocket() {
		super(1);
	}

	@Override public String getDefaultName() { return "container.batterySocket"; }

	@Override
	public void update() {
		long prevPower = this.getPower();

		super.update();

		if(!this.world.isRemote) {

			long avg = (this.getPower() + prevPower) / 2;
			this.delta = avg - this.log[0];

			for(int i = 1; i < this.log.length; i++) {
				this.log[i - 1] = this.log[i];
			}

			this.log[19] = avg;
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(delta);
		buf.writeLong(this.getPower());
		buf.writeLong(this.getMaxPower());
		if(this.slots[0] != null) {
			buf.writeInt(Item.getIdFromItem(slots[0].getItem()));
			buf.writeShort(slots[0].getItemDamage());
		} else {
			buf.writeInt(-1);
		}
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		delta = buf.readLong();
		syncPower = buf.readLong();
		syncMaxPower = buf.readLong();
		int itemId = buf.readInt();
		if(itemId != -1) this.syncStack = new ItemStack(Item.getItemById(itemId), 1, buf.readShort());
		else this.syncStack = null;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack stack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		if(stack.getItem() instanceof IBatteryItem) {
			if(i == mode_input && ((IBatteryItem)stack.getItem()).getCharge(stack) == 0) return true;
			if(i == mode_output && ((IBatteryItem)stack.getItem()).getCharge(stack) == ((IBatteryItem)stack.getItem()).getMaxCharge(stack)) return true;
		}
		return false;
	}

	@Override public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal(); return new int[] {0}; }

	@Override public long getPower() { return powerFromStack(slots[0]); }
	@Override public long getMaxPower() { return maxPowerFromStack(slots[0]); }
	
	@Override
	public void setPower(long power) {
		if(slots[0] == null || !(slots[0].getItem() instanceof IBatteryItem)) return;
		((IBatteryItem) slots[0].getItem()).setCharge(slots[0], power);
	}
	
	public static long powerFromStack(ItemStack stack) {
		if(stack == null || !(stack.getItem() instanceof IBatteryItem)) return 0;
		return ((IBatteryItem) stack.getItem()).getCharge(stack);
	}
	
	public static long maxPowerFromStack(ItemStack stack) {
		if(stack == null || !(stack.getItem() instanceof IBatteryItem)) return 0;
		return ((IBatteryItem) stack.getItem()).getMaxCharge(stack);
	}

	@Override public long getProviderSpeed() {
		if(slots[0] == null || !(slots[0].getItem() instanceof IBatteryItem)) return 0;
		int mode = this.getRelevantMode(true);
		return mode == mode_output || mode == mode_buffer ? ((IBatteryItem) slots[0].getItem()).getDischargeRate(slots[0]) : 0;
	}

	@Override public long getReceiverSpeed() {
		if(slots[0] == null || !(slots[0].getItem() instanceof IBatteryItem)) return 0;
		int mode = this.getRelevantMode(true);
		return mode == mode_input || mode == mode_buffer ? ((IBatteryItem) slots[0].getItem()).getChargeRate(slots[0]) : 0;
	}

	@Override
	public BlockPos[] getPortPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
		return new BlockPos[] {
				new BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ()),
				new BlockPos(this.pos.getX() - dir.offsetX, this.pos.getY(), this.pos.getZ() - dir.offsetZ),
				new BlockPos(this.pos.getX() + rot.offsetX, this.pos.getY(), this.pos.getZ() + rot.offsetZ),
				new BlockPos(this.pos.getX() - dir.offsetX + rot.offsetX, this.pos.getY(), this.pos.getZ() - dir.offsetZ + rot.offsetZ)
		};
	}

	@Override
	public DirPos[] getConPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
		return new DirPos[] {
				new DirPos(this.pos.getX() + dir.offsetX, this.pos.getY(), this.pos.getZ() + dir.offsetZ, dir),
				new DirPos(this.pos.getX() + dir.offsetX + rot.offsetX, this.pos.getY(), this.pos.getZ() + dir.offsetZ + rot.offsetZ, dir),

				new DirPos(this.pos.getX() - dir.offsetX * 2, this.pos.getY(), this.pos.getZ() - dir.offsetZ * 2, dir.getOpposite()),
				new DirPos(this.pos.getX() - dir.offsetX * 2 + rot.offsetX, this.pos.getY(), this.pos.getZ() - dir.offsetZ * 2 + rot.offsetZ, dir.getOpposite()),

				new DirPos(this.pos.getX() + rot.offsetX * 2, this.pos.getY(), this.pos.getZ() + rot.offsetZ * 2, rot),
				new DirPos(this.pos.getX() + rot.offsetX * 2 - dir.offsetX, this.pos.getY(), this.pos.getZ() + rot.offsetZ * 2 - dir.offsetZ, rot),

				new DirPos(this.pos.getX() - rot.offsetX, this.pos.getY(), this.pos.getZ() - rot.offsetZ, rot.getOpposite()),
				new DirPos(this.pos.getX() - rot.offsetX - dir.offsetX, this.pos.getY(), this.pos.getZ() - rot.offsetZ - dir.offsetZ, rot.getOpposite())
		};
	}

	@Override public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) { return new ContainerBatterySocket(player.inventory, this); }
	@Override public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) { return new GUIBatterySocket(player.inventory, this); }

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
					this.pos.getX() - 1,
					this.pos.getY(),
					this.pos.getZ() - 1,
					this.pos.getX() + 2,
					this.pos.getY() + 2,
					this.pos.getZ() + 2
					);
		}

		return bb;
	}

	@Override
	public String[] getFunctionInfo() {
		return new String[] {
				PREFIX_VALUE + "fill",
				PREFIX_VALUE + "fillpercent",
				PREFIX_VALUE + "delta",
				PREFIX_FUNCTION + "setmode" + NAME_SEPARATOR + "mode",
				PREFIX_FUNCTION + "setmode" + NAME_SEPARATOR + "mode" + PARAM_SEPARATOR + "fallback",
				PREFIX_FUNCTION + "setredmode" + NAME_SEPARATOR + "mode",
				PREFIX_FUNCTION + "setredmode" + NAME_SEPARATOR + "mode" + PARAM_SEPARATOR + "fallback",
				PREFIX_FUNCTION + "setpriority" + NAME_SEPARATOR + "priority",
		};
	}

	@Override
	public String provideRORValue(String name) {
		if((PREFIX_VALUE + "fill").equals(name))		return "" + this.getPower();
		if((PREFIX_VALUE + "fillpercent").equals(name))	return "" + this.getPower() * 100 / (Math.max(this.getMaxPower(), 1));
		if((PREFIX_VALUE + "delta").equals(name))		return "" + delta;
		return null;
	}

	@Override
	public String runRORFunction(String name, String[] params) {

		if((PREFIX_FUNCTION + "setmode").equals(name) && params.length > 0) {
			int mode = IRORInteractive.parseInt(params[0], 0, 3);

			if(mode != this.redLow) {
				this.redLow = (short) mode;
				this.markChanged();
				return null;
			} else if(params.length > 1) {
				int altmode = IRORInteractive.parseInt(params[1], 0, 3);
				this.redLow = (short) altmode;
				this.markChanged();
				return null;
			}
			return null;
		}

		if((PREFIX_FUNCTION + "setredmode").equals(name) && params.length > 0) {
			int mode = IRORInteractive.parseInt(params[0], 0, 3);

			if(mode != this.redHigh) {
				this.redHigh = (short) mode;
				this.markChanged();
				return null;
			} else if(params.length > 1) {
				int altmode = IRORInteractive.parseInt(params[1], 0, 3);
				this.redHigh = (short) altmode;
				this.markChanged();
				return null;
			}
			return null;
		}

		if((PREFIX_FUNCTION + "setpriority").equals(name) && params.length > 0) {
			int priority = IRORInteractive.parseInt(params[0], 0, 2) + 1;
			ConnectionPriority p = EnumUtil.grabEnumSafely(ConnectionPriority.class, priority);
			this.priority = p;
			this.markChanged();
			return null;
		}
		return null;
	}

	@Override
	public void provideExtraInfo(NBTTagCompound data) {
		data.setLong(CompatEnergyControl.L_DIFF_HE, (log[0] - log[19]) / 20L);
	}

	// do some opencomputer stuff
	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getEnergyInfo(Context context, Arguments args) {
		return new Object[] {getPower(), getMaxPower(), this.delta};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getPackInfo(Context context, Arguments args) {
		if(slots[0] == null || !(slots[0].getItem() instanceof IBatteryItem)) return new Object[] {"", 0, 0};
		IBatteryItem bat = ((IBatteryItem) slots[0].getItem());
		return new Object[] {slots[0].getUnlocalizedName(), bat.getChargeRate(slots[0]), bat.getDischargeRate(slots[0])};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getInfo(Context context, Arguments args) {
		Object[] energyInfo = getEnergyInfo(context, args);
		Object[] packInfo = getPackInfo(context, args);
		Object[] modeInfo = getModeInfo(context, args);
		return new Object[] {energyInfo[0], energyInfo[1], energyInfo[2], modeInfo[0], modeInfo[1], modeInfo[2], packInfo[0], packInfo[1], packInfo[2]};
	}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public String[] methods() {
		return new String[] {
			"getEnergyInfo",
			"getPackInfo",
			"getModeInfo",
			"setModeLow",
			"setModeHigh",
			"setPriority",
			"getInfo"
		};
	}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public Object[] invoke(String method, Context context, Arguments args) throws Exception {
		switch (method) {
			case "getEnergyInfo": return getEnergyInfo(context, args);
			case "getPackInfo": return getPackInfo(context, args);
			case "getModeInfo": return getModeInfo(context, args);
			case "setModeLow": return setModeLow(context, args);
			case "setModeHigh": return setModeHigh(context, args);
			case "setPriority": return setPriority(context, args);
			case "getInfo": return getInfo(context, args);
		}
		throw new NoSuchMethodException();
	}
}
