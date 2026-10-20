package com.hbm.tileentity.machine.rbmk;

import com.hbm.handler.neutron.RBMKNeutronHandler;
import com.hbm.inventory.container.ContainerRBMKStorage;
import com.hbm.inventory.gui.GUIRBMKStorage;
import com.hbm.items.machine.ItemRBMKRod;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKConsole.ColumnType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class TileEntityRBMKStorage extends TileEntityRBMKSlottedBase implements IRBMKLoadable {

	public TileEntityRBMKStorage() {
		super(12);
	}

	@Override
	public String getName() {
		return "container.rbmkStorage";
	}
	
	@Override
	public void update() {
		
		if(!this.world.isRemote && this.world.getTotalWorldTime() % 10 == 0) {
			
			for(int i = 0; i < slots.length - 1; i++) {
				
				if(slots[i] == null && slots[i + 1] != null) {
					slots[i] = slots[i + 1];
					slots[i + 1] = null;
				}
			}
		}
		
		super.update();
	}

	@Override
	public RBMKNeutronHandler.RBMKType getRBMKType() {
		return RBMKNeutronHandler.RBMKType.OTHER;
	}

	@Override
	public ColumnType getConsoleType() {
		return ColumnType.STORAGE;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return itemStack.getItem() instanceof ItemRBMKRod;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return true;
	}

	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int p_94128_1_ = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
	}

	@Override
	public boolean canLoad(ItemStack toLoad) {
		return slots[11] == null;
	}

	@Override
	public void load(ItemStack toLoad) {
		slots[11] = toLoad.copy();
	}

	@Override
	public boolean canUnload() {
		return slots[0] != null;
	}

	@Override
	public ItemStack provideNext() {
		return slots[0];
	}

	@Override
	public void unload() {
		slots[0] = null;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerRBMKStorage(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIRBMKStorage(player.inventory, this);
	}
}
