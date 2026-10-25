package com.hbm.tileentity.machine.storage;

import com.hbm.inventory.container.ContainerSoyuzCapsule;
import com.hbm.inventory.gui.GUISoyuzCapsule;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityInventoryBase;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class TileEntitySoyuzCapsule extends TileEntityInventoryBase implements IGUIProvider {

	public TileEntitySoyuzCapsule() {
		super(19);
	}

	@Override
	protected String getDefaultName() {
		return "container.soyuzCapsule";
	}

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {

        return new AxisAlignedBB(this.pos.getX() - 1, this.pos.getY() - 1, this.pos.getZ() - 1, this.pos.getX() + 2, this.pos.getY() + 3, this.pos.getZ() + 2);
    }

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerSoyuzCapsule(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUISoyuzCapsule(player.inventory, this);
	}

}
