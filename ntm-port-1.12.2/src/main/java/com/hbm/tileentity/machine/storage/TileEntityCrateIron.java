package com.hbm.tileentity.machine.storage;

import com.hbm.inventory.container.ContainerCrateIron;
import com.hbm.inventory.gui.GUICrateIron;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;

public class TileEntityCrateIron extends TileEntityCrateBase {

	public TileEntityCrateIron() {
		super(36);
	}

	@Override
	public String getName() {
		return this.hasCustomName() ? this.customName : "container.crateIron";
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerCrateIron(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUICrateIron(player.inventory, this);
	}
}
