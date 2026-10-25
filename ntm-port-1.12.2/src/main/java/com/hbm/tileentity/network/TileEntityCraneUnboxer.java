package com.hbm.tileentity.network;

import api.hbm.conveyor.IConveyorBelt;
import com.hbm.entity.item.EntityMovingItem;
import com.hbm.inventory.container.ContainerCraneUnboxer;
import com.hbm.inventory.gui.GUICraneUnboxer;
import com.hbm.items.ModItems;
import com.hbm.tileentity.IGUIProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class TileEntityCraneUnboxer extends TileEntityCraneBase implements IGUIProvider {

	public TileEntityCraneUnboxer() {
		super(23);
	}

	@Override
	public String getDefaultName() {
		return "container.craneUnboxer";
	}

	@Override
	public void update() {
		super.update();
		if(!this.world.isRemote) {

			int delay = 20;

			if(slots[22] != null && slots[22].getItem() == ModItems.upgrade_ejector) {
				switch(slots[22].getItemDamage()) {
				case 0: delay = 10; break;
				case 1: delay = 5; break;
				case 2: delay = 2; break;
				}
			}

			/*boolean powered = false;

			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				if(this.worldObj.isBlockIndirectlyGettingPowered(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ)) {
					powered = true;
					break;
				}
			}*/

			if(this.world.getTotalWorldTime() % delay == 0 && !this.world.isBlockPowered(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ()))) {
				int amount = 1;

				if(slots[21] != null && slots[21].getItem() == ModItems.upgrade_stack) {
					switch(slots[21].getItemDamage()) {
					case 0: amount = 4; break;
					case 1: amount = 16; break;
					case 2: amount = 64; break;
					}
				}

				ForgeDirection outputSide = getInputSide(); // note the switcheroo!
				Block b = com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + outputSide.offsetX, this.pos.getY() + outputSide.offsetY, this.pos.getZ() + outputSide.offsetZ).getBlock();

				if(b instanceof IConveyorBelt) {

					IConveyorBelt belt = (IConveyorBelt) b;

					for(int i = 0; i < 21; i++) {
						ItemStack stack = slots[i];

						if(stack != null){
							stack = stack.copy();
							int toSend = Math.min(amount, stack.getCount());
							decrStackSize(i, toSend);
							stack.setCount(toSend);

							EntityMovingItem moving = new EntityMovingItem(this.world);
							Vec3d pos = new Vec3d(this.pos.getX() + 0.5 + outputSide.offsetX * 0.55, this.pos.getY() + 0.5 + outputSide.offsetY * 0.55, this.pos.getZ() + 0.5 + outputSide.offsetZ * 0.55);
							Vec3d snap = belt.getClosestSnappingPosition(this.world, this.pos.getX() + outputSide.offsetX, this.pos.getY() + outputSide.offsetY, this.pos.getZ() + outputSide.offsetZ, pos);
							moving.setPosition(snap.x, snap.y, snap.z);
							moving.setItemStack(stack);
							this.world.spawnEntity(moving);
							break;
						}
					}
				}
			}
		}
	}

	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return true;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return true;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerCraneUnboxer(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUICraneUnboxer(player.inventory, this);
	}
}
