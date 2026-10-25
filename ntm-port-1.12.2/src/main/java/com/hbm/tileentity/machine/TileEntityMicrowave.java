package com.hbm.tileentity.machine;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.EntityProcessorCrossSmooth;
import com.hbm.explosion.vanillant.standard.ExplosionEffectWeapon;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.ICopiable;
import com.hbm.inventory.container.ContainerMicrowave;
import com.hbm.inventory.gui.GUIMicrowave;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;

import api.hbm.energymk2.IEnergyReceiverMK2;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")})
public class TileEntityMicrowave extends TileEntityMachineBase implements IEnergyReceiverMK2, IGUIProvider, SimpleComponent, CompatHandler.OCComponent, ICopiable {

	public long power;
	public static final long maxPower = 50000;
	public static final int consumption = 50;
	public static final int maxTime = 300;
	public int time;
	public int speed;
	public static final int maxSpeed = 5;

	public TileEntityMicrowave() {
		super(3);
	}

	@Override
	public String getDefaultName() {
		return "container.microwave";
	}

	@Override
	public void update() {

		if(!this.world.isRemote) {

			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) this.trySubscribe(this.world, this.pos.getX() + dir.offsetX, this.pos.getY() + dir.offsetY, this.pos.getZ() + dir.offsetZ, dir);

			this.power = Library.chargeTEFromItems(slots, 2, power, maxPower);

			if(canProcess()) {

				if(speed >= maxSpeed) {
					com.hbm.world.WorldBlockDestruction.destroy(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), false);
					ExplosionVNT vnt = new ExplosionVNT(this.world, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, 5);
					vnt.setEntityProcessor(new EntityProcessorCrossSmooth(1, 50));
					vnt.setPlayerProcessor(new PlayerProcessorStandard());
					vnt.setSFX(new ExplosionEffectWeapon(10, 2.5F, 1F));
					vnt.explode();
					return;
				}

				if(time >= maxTime) {
					process();
					time = 0;
				}

				if(canProcess()) {
					power -= consumption;
					time += speed * 2;
				}
			}

			networkPackNT(50);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeInt(time);
		buf.writeInt(speed);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		time = buf.readInt();
		speed = buf.readInt();
	}

	public void handleButtonPacket(int value, int meta) {

		if(value == 0)
			speed++;

		if(value == 1)
			speed--;

		if(speed < 0)
			speed = 0;

		if(speed > maxSpeed)
			speed = maxSpeed;
	}

	private void process() {

		ItemStack stack = FurnaceRecipes.instance().getSmeltingResult(slots[0]).copy();

		if(slots[1] == null) {
			slots[1] = stack;
		} else {
			slots[1].grow(stack.getCount());
		}

		this.decrStackSize(0, 1);

		this.markDirty();
	}

	private boolean canProcess() {

		if(speed  == 0)
			return false;

		if(power < consumption)
			return false;

		if(slots[0] != null && FurnaceRecipes.instance().getSmeltingResult(slots[0]) != null) {

			ItemStack stack = FurnaceRecipes.instance().getSmeltingResult(slots[0]);

			if(!(slots[0].getItem() instanceof ItemFood) && !(stack.getItem() instanceof ItemFood)) return false;
			if(slots[1] == null) return true;
			if(!stack.isItemEqual(slots[1])) return false;

			return stack.getCount() + slots[1].getCount() <= stack.getMaxStackSize();
		}

		return false;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return i == 0 && FurnaceRecipes.instance().getSmeltingResult(itemStack) != null;
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return this.isItemValidForSlot(i, itemStack);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return i == 1;
	}

	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return side == 0 ? new int[] { 1 } : new int[] { 0 };
	}

	public long getPowerScaled(int i) {
		return (power * i) / maxPower;
	}

	public int getProgressScaled(int i) {
		return (time * i) / maxTime;
	}

	public int getSpeedScaled(int i) {
		return (speed * i) / maxSpeed;
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
		power = i;
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		power = nbt.getLong("power");
		speed = nbt.getInteger("speed");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setLong("power", power);
		nbt.setInteger("speed", speed);
		return nbt;
	}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public String getComponentName() {
		return "microwave";
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] test(Context context, Arguments args) {
		return new Object[] {"This is a testing device for everything OC."};
	}

	@Callback(direct = true, getter = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] variableget(Context context, Arguments args) {
		return new Object[] {speed, "test of the `getter` callback function"};
	}

	@Callback(direct = true, setter = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] variableset(Context context, Arguments args) {
		speed = MathHelper.clamp(args.checkInteger(0), 0, 5);
		return new Object[] {"test of the `setter` callback function"};
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMicrowave(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMicrowave(player.inventory, this);
	}

	@Override
	public NBTTagCompound getSettings(World world, int x, int y, int z) {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("microSpeed", speed);
		return null;
	}

	@Override
	public void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, int x, int y, int z) {
		if(nbt.hasKey("microSpeed")) speed = nbt.getInteger("microSpeed");
	}

	@Override
	public String[] infoForDisplay(World world, int x, int y, int z) {
		return new String[]{ "copyTool.speed"};
	}
}
