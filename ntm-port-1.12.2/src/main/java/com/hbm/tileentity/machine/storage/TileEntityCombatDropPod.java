package com.hbm.tileentity.machine.storage;

import com.hbm.config.SpaceConfig;
import com.hbm.inventory.container.ContainerSoyuzCapsule;
import com.hbm.inventory.gui.GUISoyuzCapsule;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityInventoryBase;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.tileentity.TileEntityProxyBase;
import com.hbm.tileentity.TileEntityProxyCombo;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class TileEntityCombatDropPod extends TileEntity implements IBufPacketReceiver, net.minecraft.util.ITickable{

	public NBTTagCompound entityType;
	public int amount;
	public int color;
	public int delay = 40;

	public double hatchopen;
	public double hatchopen2;
	public double prevHatchopen;
	public double prevHatchopen2;

	public TileEntityCombatDropPod() {

	}

	public void setPayload(NBTTagCompound entityType, int amount, int color) {
		this.entityType = entityType;
		this.amount = amount;
		this.color = color;
	}

	public int getColor() {
		return this.color;
	}

	public void setColor(int color) {
		this.color = color;

	}

	@Override
	public void update() {
		prevHatchopen = hatchopen;
		prevHatchopen2 = hatchopen2;

		if (delay > 0) {
			delay--;
			return;
		}
		if(delay == 0) {
			hatchopen += (90 - hatchopen) * 0.2;
			hatchopen2 += (-90 - hatchopen2) * 0.2;
		}

		if (entityType != null && amount > 0 && !this.world.isRemote) {

			for (int i = 0; i < amount; i++) {

				Entity entity = EntityList.createEntityFromNBT(entityType, this.world);

				if (entity != null) {
					entity.setPosition(this.pos.getX() + 0.5, this.pos.getY() + 1, this.pos.getZ() + 0.5);

					this.world.spawnEntity(entity);
				}
			}

			amount = 0;
			com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), "hbm:block.hatchOpen", 10.0F, 0.9F);
			return;

		}

		if(SpaceConfig.combatPodDespawn == true) {
			if(this.world.getTotalWorldTime() % 1000 == 0) {
				com.hbm.world.WorldAirRemoval.remove(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());
			}
		}
	}

	//we serious?? like actually??
	//i hate working on this game so much sometimes
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
	    NBTTagCompound nbt = new NBTTagCompound();
	    this.writeToNBT(nbt);
	    return new SPacketUpdateTileEntity(this.getPos(), 1, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
	    this.readFromNBT(pkt.getNbtCompound());
	}

	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(this.pos.getX() - 1, this.pos.getY() - 1, this.pos.getZ() - 1, this.pos.getX() + 2, this.pos.getY() + 3,
				this.pos.getZ() + 2);
	}


	@Override
	public void deserialize(ByteBuf buf) {
		this.color = buf.readInt();
		this.hatchopen = buf.readDouble();
		this.hatchopen2 = buf.readDouble();
	}
	@Override
	public void serialize(ByteBuf buf) {
		buf.writeInt(color);
		buf.writeDouble(hatchopen);
		buf.writeDouble(hatchopen2);

	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
	    super.writeToNBT(nbt);

	    if(entityType != null)
	        nbt.setTag("EntityType", entityType);

	    nbt.setInteger("amount", amount);
	    nbt.setInteger("color", color);
	    nbt.setInteger("delay", delay);
	    nbt.setDouble("hatchopen", hatchopen);
	    nbt.setDouble("hatchopen2", hatchopen2);
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
	    super.readFromNBT(nbt);

	    entityType = nbt.getCompoundTag("EntityType");
	    amount = nbt.getInteger("amount");
	    color = nbt.getInteger("color");
	    delay = nbt.getInteger("delay");
	    hatchopen = nbt.getDouble("hatchopen");
	    hatchopen2 = nbt.getDouble("hatchopen2");

	}


}
