package com.hbm.tileentity.machine.storage;

import com.hbm.inventory.container.ContainerFileCabinet;
import com.hbm.inventory.gui.GUIFileCabinet;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.IGUIProvider;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class TileEntityFileCabinet extends TileEntityCrateBase implements IGUIProvider, IBufPacketReceiver, net.minecraft.util.ITickable {

	@Override
	public boolean isEmpty() {
		for(int i = 0; i < this.getSizeInventory(); i++) {
			if(this.getStackInSlot(i) != null && !this.getStackInSlot(i).isEmpty()) return false;
		}
		return true;
	}

	@Override public int getField(int id) { return 0; }
	@Override public void setField(int id, int value) {}
	@Override public int getFieldCount() { return 0; }

	@Override
	public void clear() {
		for(int i = 0; i < this.getSizeInventory(); i++) {
		this.setInventorySlotContents(i, null);
		}
	}

	private int timer = 0;
	private int playersUsing = 0;
	//meh, it's literally just two extra variables
	public float lowerExtent = 0; //i don't know a term for how 'open' something is
	public float prevLowerExtent = 0;
	public float upperExtent = 0;
	public float prevUpperExtent = 0;

	public TileEntityFileCabinet() {
		super(8);
	}

	@Override
	public String getName() {
		return "container.fileCabinet";
	}

	@Override
	public void openInventory(net.minecraft.entity.player.EntityPlayer viewer) {
		if(!this.world.isRemote) this.playersUsing++;
	}

	@Override
	public void closeInventory(net.minecraft.entity.player.EntityPlayer viewer) {
		if(!this.world.isRemote) this.playersUsing--;
	}

	@Override public void serialize(ByteBuf buf) {
		buf.writeInt(timer);
		buf.writeInt(playersUsing);
	}

	@Override public void deserialize(ByteBuf buf) {
		timer = buf.readInt();
		playersUsing = buf.readInt();
	}

	@Override
	public void update() {

		if(!this.world.isRemote) {

			if(this.playersUsing > 0) {
				if(timer < 10) {
					timer++;
				}
			} else
				timer = 0;

			networkPackNT(25);
		} else {
			this.prevLowerExtent = lowerExtent;
			this.prevUpperExtent = upperExtent;
		}

		float openSpeed = playersUsing > 0 ? 1F / 16F : 1F / 25F;
		float maxExtent = 0.8F;

		if(this.playersUsing > 0) {
			if(lowerExtent == 0F && upperExtent == 0F)
				com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, "hbm:block.crateOpen", 0.8F, 1.0F);
			else {
				if(upperExtent + openSpeed >= maxExtent && lowerExtent < maxExtent)
					com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, "hbm:block.crateOpen", 0.5F, this.world.rand.nextFloat() * 0.1F + 0.7F);

				if(lowerExtent + openSpeed >= maxExtent && lowerExtent < maxExtent)
					com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, "hbm:block.crateOpen", 0.5F, this.world.rand.nextFloat() * 0.1F + 0.7F);
			}

			this.lowerExtent += openSpeed;

			if(timer >= 10)
				this.upperExtent += openSpeed;

		} else if(lowerExtent > 0) {
			if(upperExtent - openSpeed < maxExtent / 2 && upperExtent >= maxExtent / 2 && upperExtent != lowerExtent)
				com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, "hbm:block.crateClose", 0.8F, 1.0F);

			if(lowerExtent - openSpeed < maxExtent / 2 && lowerExtent >= maxExtent / 2)
				com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, "hbm:block.crateClose", 0.8F, 1.0F);

			this.upperExtent -= openSpeed;
			this.lowerExtent -= openSpeed;
		}

		this.lowerExtent = MathHelper.clamp(lowerExtent, 0F, maxExtent);
		this.upperExtent = MathHelper.clamp(upperExtent, 0F, maxExtent);
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerFileCabinet(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIFileCabinet(player.inventory, this);
	}

	//No automation, it's a filing cabinet.
	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		return false;
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return false;
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = new AxisAlignedBB(
					this.pos.getX() - 1,
					this.pos.getY(),
					this.pos.getZ() - 1,
					this.pos.getX() + 1,
					this.pos.getY() + 1,
					this.pos.getZ() + 1
					);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}
