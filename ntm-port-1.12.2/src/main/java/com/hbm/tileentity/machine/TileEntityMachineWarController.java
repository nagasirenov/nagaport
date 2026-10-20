package com.hbm.tileentity.machine;

import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystem;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerMachineWarController;
import com.hbm.inventory.gui.GUIWarController;
import com.hbm.items.ISatChip;
import com.hbm.items.ItemVOTVdrive;
import com.hbm.items.ModItems;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.saveddata.satellites.SatelliteWar;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;

import api.hbm.energymk2.IEnergyReceiverMK2;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class TileEntityMachineWarController extends TileEntityMachineBase implements IEnergyReceiverMK2, IGUIProvider, IControlReceiver {

	public int id;

	public TileEntityMachineWarController() {
		super(4);
	}

	@Override
	public String getDefaultName() {
		return "container.warController";
	}

	@Override
	public long getPower() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPower(long power) {
		// TODO Auto-generated method stub

	}

	@Override
	public long getMaxPower() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isLoaded() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineWarController(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIWarController(player.inventory, this);
	}

	@Override
	public void update() {
		if(!this.world.isRemote) {
			id = ISatChip.getFreqS(slots[2]);
			if(slots[1] == null || slots[1].getItem() != ModItems.full_drive) return;

			SatelliteSavedData data = SatelliteSavedData.getData(this.world, this.pos.getX(), this.pos.getZ());

			SolarSystem.Body target = ItemVOTVdrive.getDestination(slots[1]).body;
			CelestialBody body = target.getBody();

			Satellite sat = data.getSatFromFreq(id);

			if(sat instanceof SatelliteWar) {
				SatelliteWar satelliteWar = (SatelliteWar) sat;

				satelliteWar.setTarget(body);
			}
		}
	}

	@Override
	public void receiveControl(NBTTagCompound data) {
		if(data.hasKey("xcoord") && data.hasKey("zcoord")) {
			updateDriveCoords(data.getInteger("xcoord"), data.getInteger("zcoord"));
		}
	}

	private void updateDriveCoords(int x, int z) {
		if(slots[1] == null || slots[1].getItem() != ModItems.full_drive) return;

		ItemVOTVdrive.setCoordinates(slots[1], x, z);

		this.markDirty();
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return isUsableByPlayer(player);
	}

}
