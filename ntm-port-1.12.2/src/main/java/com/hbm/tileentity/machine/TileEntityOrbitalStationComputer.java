package com.hbm.tileentity.machine;

import com.hbm.dim.CelestialBody;
import com.hbm.dim.orbit.OrbitalStation;
import com.hbm.dim.orbit.OrbitalStation.StationState;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerOrbitalStationComputer;
import com.hbm.inventory.gui.GUIOrbitalStationComputer;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class TileEntityOrbitalStationComputer extends TileEntityMachineBase implements IGUIProvider, IControlReceiver {

	public boolean hasDrive;

	public TileEntityOrbitalStationComputer() {
		super(1);
	}

	public boolean travelTo(CelestialBody body, ItemStack drive) {
		OrbitalStation station = OrbitalStation.getStationFromPosition(this.pos.getX(), this.pos.getZ());

		if(station.orbiting == body) return false;

		station.travelTo(this.world, body);
		slots[0] = drive;
		markChanged();

		return true;
	}

	public boolean isTravelling() {
		OrbitalStation station = OrbitalStation.getStationFromPosition(this.pos.getX(), this.pos.getZ());

		return station.state != StationState.ORBIT;
	}

	@Override
	public String getDefaultName() {
		return "container.orbitalStationComputer";
	}

	@Override
	public void update() {
		if(!this.world.isRemote) {
			hasDrive = slots[0] != null;
			networkPackNT(50);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeBoolean(hasDrive);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		hasDrive = buf.readBoolean();
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerOrbitalStationComputer();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIOrbitalStationComputer(this);
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return true;
	}

	@Override
	public void receiveControl(NBTTagCompound data) {
		if(data.hasKey("name")) {
			OrbitalStation station = OrbitalStation.getStationFromPosition(this.pos.getX(), this.pos.getZ());
			station.name = data.getString("name");
		}

		if(data.hasKey("gravity")) {
			OrbitalStation station = OrbitalStation.getStationFromPosition(this.pos.getX(), this.pos.getZ());
			station.gravityMultiplier = data.getBoolean("gravity") ? 1 : 0;
		}
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if(bb == null) {
			bb = new AxisAlignedBB(
				this.pos.getX(),
				this.pos.getY(),
				this.pos.getZ(),
				this.pos.getX() + 1,
				this.pos.getY() + 2,
				this.pos.getZ() + 1
			);
		}

		return bb;
	}

}
