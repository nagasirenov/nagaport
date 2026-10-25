package com.hbm.tileentity.network;

import api.hbm.conveyor.IConveyorBelt;
import com.hbm.entity.item.EntityMovingPackage;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerCraneBoxer;
import com.hbm.inventory.gui.GUICraneBoxer;
import com.hbm.tileentity.IGUIProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class TileEntityCraneBoxer extends TileEntityCraneBase implements IGUIProvider, IControlReceiver {

	public byte mode = 0;
	public static final byte MODE_4 = 0;
	public static final byte MODE_8 = 1;
	public static final byte MODE_16 = 2;
	public static final byte MODE_REDSTONE = 3;

	private boolean lastRedstone = false;


	public TileEntityCraneBoxer() {
		super(7 * 3);
	}

	@Override
	public String getDefaultName() {
		return "container.craneBoxer";
	}

	@Override
	public void update() {
		super.update();
		if(!this.world.isRemote) {

			boolean redstone = this.world.isBlockPowered(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ()));

			if(mode == MODE_REDSTONE && redstone && !lastRedstone) {

				ForgeDirection outputSide = getOutputSide();
				Block b = com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + outputSide.offsetX, this.pos.getY() + outputSide.offsetY, this.pos.getZ() + outputSide.offsetZ).getBlock();
				IConveyorBelt belt = null;

				if(b instanceof IConveyorBelt) {
					belt = (IConveyorBelt) b;
				}

				int pack = 0;

				for(int i = 0; i < slots.length; i++) {
					if(slots[i] != null) {
						pack++;
					}
				}

				if(belt != null && pack > 0) {

					ItemStack[] box = new ItemStack[pack];

					for(int i = 0; i < slots.length && pack > 0; i++) {

						if(slots[i] != null) {
							pack--;
							box[pack] = slots[i].copy();
							slots[i] = null;
						}
					}

					EntityMovingPackage moving = new EntityMovingPackage(this.world);
					Vec3d pos = new Vec3d(this.pos.getX() + 0.5 + outputSide.offsetX * 0.55, this.pos.getY() + 0.5 + outputSide.offsetY * 0.55, this.pos.getZ() + 0.5 + outputSide.offsetZ * 0.55);
					Vec3d snap = belt.getClosestSnappingPosition(this.world, this.pos.getX() + outputSide.offsetX, this.pos.getY() + outputSide.offsetY, this.pos.getZ() + outputSide.offsetZ, pos);
					moving.setPosition(snap.x, snap.y, snap.z);
					moving.setItemStacks(box);
					this.world.spawnEntity(moving);
				}
			}

			this.lastRedstone = redstone;

			if(mode != MODE_REDSTONE && this.world.getTotalWorldTime() % 2 == 0) {
				int pack = 1;

				switch(mode) {
				case MODE_4: pack = 4; break;
				case MODE_8: pack = 8; break;
				case MODE_16: pack = 16; break;
				}

				int fullStacks = 0;

				// NO!
				/*StorageManifest manifest = new StorageManifest(); //i wrote some of this for a feature that i scrapped so why not make proper use of it?

				for(int i = 0; i < slots.length; i++) {
					if(slots[i] != null) {
						manifest.writeStack(slots[i]);
					}
				}*/

				for(int i = 0; i < slots.length; i++) {

					if(slots[i] != null && slots[i].getCount() == slots[i].getMaxStackSize()) {
						fullStacks++;
					}
				}

				ForgeDirection outputSide = getOutputSide();
				Block b = com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + outputSide.offsetX, this.pos.getY() + outputSide.offsetY, this.pos.getZ() + outputSide.offsetZ).getBlock();
				IConveyorBelt belt = null;

				if(b instanceof IConveyorBelt) {
					belt = (IConveyorBelt) b;
				}

				if(belt != null && fullStacks >= pack) {

					ItemStack[] box = new ItemStack[pack];

					for(int i = 0; i < slots.length && pack > 0; i++) {

						if(slots[i] != null && slots[i].getCount() == slots[i].getMaxStackSize()) {
							pack--;
							box[pack] = slots[i].copy();
							slots[i] = null;
						}
					}

					EntityMovingPackage moving = new EntityMovingPackage(this.world);
					Vec3d pos = new Vec3d(this.pos.getX() + 0.5 + outputSide.offsetX * 0.55, this.pos.getY() + 0.5 + outputSide.offsetY * 0.55, this.pos.getZ() + 0.5 + outputSide.offsetZ * 0.55);
					Vec3d snap = belt.getClosestSnappingPosition(this.world, this.pos.getX() + outputSide.offsetX, this.pos.getY() + outputSide.offsetY, this.pos.getZ() + outputSide.offsetZ, pos);
					moving.setPosition(snap.x, snap.y, snap.z);
					moving.setItemStacks(box);
					this.world.spawnEntity(moving);
				}
			}

			this.networkPackNT(15);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeByte(this.mode);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.mode = buf.readByte();
	}

	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int p_94128_1_ = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return true;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.mode = nbt.getByte("mode");
		this.lastRedstone = nbt.getBoolean("lastRedstone");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setByte("mode", mode);
		nbt.setBoolean("lastRedstone", lastRedstone);
		return nbt;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerCraneBoxer(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUICraneBoxer(player.inventory, this);
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return new Vec3d(this.pos.getX() - player.posX, this.pos.getY() - player.posY, this.pos.getZ() - player.posZ).length() < 20;
	}

	@Override
	public void receiveControl(NBTTagCompound data) {
		if(data.hasKey("toggle")) {
			this.mode = (byte) ((this.mode + 1) % 4);
		}
	}
}
