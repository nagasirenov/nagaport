package com.hbm.tileentity.machine;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.entity.projectile.EntityCog;
import com.hbm.lib.Library;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.IConfigurableMachine;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.tile.IHeatSource;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import com.hbm.lib.ForgeDirection;

public class TileEntityStirling extends TileEntityLoadedBase implements IBufPacketReceiver, IEnergyProviderMK2, IConfigurableMachine, net.minecraft.util.ITickable {

	public long powerBuffer;
	public int heat;
	private int warnCooldown = 0;
	private int overspeed = 0;
	public boolean hasCog = true;

	public float spin;
	public float lastSpin;

	/* CONFIGURABLE CONSTANTS */
	public static double diffusion = 0.1D;
	public static double efficiency = 0.5D;
	public static int maxHeatNormal = 300;
	public static int maxHeatSteel = 1500;
	public static int overspeedLimit = 300;

	@Override
	public void update() {

		if(!this.world.isRemote) {

			if(hasCog) {
				this.powerBuffer = 0;
				tryPullHeat();

				this.powerBuffer = (long) (this.heat * (this.isCreative() ? 1 : this.efficiency));

				if(warnCooldown > 0)
					warnCooldown--;

				if(heat > maxHeat() && !isCreative()) {

					this.overspeed++;

					if(overspeed > 60 && warnCooldown == 0) {
						warnCooldown = 100;
						com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX() + 0.5, this.pos.getY() + 1, this.pos.getZ() + 0.5, "hbm:block.warnOverspeed", 2.0F, 1.0F);
					}

					if(overspeed > overspeedLimit) {
						this.hasCog = false;
						this.world.newExplosion(null, this.pos.getX() + 0.5, this.pos.getY() + 1, this.pos.getZ() + 0.5, 5F, false, false);

						int orientation = this.getBlockMetadata() - BlockDummyable.offset;
						ForgeDirection dir = ForgeDirection.getOrientation(orientation);
						EntityCog cog = new EntityCog(this.world, this.pos.getX() + 0.5 + dir.offsetX, this.pos.getY() + 1, this.pos.getZ() + 0.5 + dir.offsetZ).setOrientation(orientation).setMeta(this.getGeatMeta());
						ForgeDirection rot = dir.getRotation(ForgeDirection.DOWN);

						cog.motionX = rot.offsetX;
						cog.motionY = 1 + (heat - maxHeat()) * 0.0001D;
						cog.motionZ = rot.offsetZ;
						this.world.spawnEntityInWorld(cog);

						this.markDirty();
					}

				} else {
					this.overspeed = 0;
				}
			} else {
				this.overspeed = 0;
				this.warnCooldown = 0;
			}

			networkPackNT(150);

			if(hasCog) {
				for(DirPos pos : getConPos()) {
					this.tryProvide(this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
				}
			} else {

				if(this.powerBuffer > 0)
					this.powerBuffer--;
			}

			this.heat = 0;
		} else {

			float momentum = powerBuffer * 50F / ((float) maxHeat());

			if(this.isCreative()) momentum = Math.min(momentum, 45F);

			this.lastSpin = this.spin;
			this.spin += momentum;

			if(this.spin >= 360F) {
				this.spin -= 360F;
				this.lastSpin -= 360F;
			}
		}
	}

	public int getGeatMeta() {
		return this.getBlockType() == ModBlocks.machine_stirling ? 0 : this.getBlockType() == ModBlocks.machine_stirling_creative ? 2 : 1;
	}

	public int maxHeat() {
		return this.getBlockType() == ModBlocks.machine_stirling ? 300 : 1500;
	}

	public boolean isCreative() {
		return this.getBlockType() == ModBlocks.machine_stirling_creative;
	}

	protected DirPos[] getConPos() {
		return new DirPos[] {
				new DirPos(this.pos.getX() + 2, this.pos.getY(), this.pos.getZ(), Library.POS_X),
				new DirPos(this.pos.getX() - 2, this.pos.getY(), this.pos.getZ(), Library.NEG_X),
				new DirPos(this.pos.getX(), this.pos.getY(), this.pos.getZ() + 2, Library.POS_Z),
				new DirPos(this.pos.getX(), this.pos.getY(), this.pos.getZ() - 2, Library.NEG_Z)
		};
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeLong(this.powerBuffer);
		buf.writeInt(this.heat);
		buf.writeBoolean(this.hasCog);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.powerBuffer = buf.readLong();
		this.heat = buf.readInt();
		this.hasCog = buf.readBoolean();
	}

	protected void tryPullHeat() {
		TileEntity con = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY() - 1, this.pos.getZ()));

		if(con instanceof IHeatSource) {
			IHeatSource source = (IHeatSource) con;
			int heatSrc = (int) (source.getHeatStored() * diffusion);

			if(heatSrc > 0) {
				source.useUpHeat(heatSrc);
				this.heat += heatSrc;
				return;
			}
		}

		this.heat = Math.max(this.heat - Math.max(this.heat / 1000, 1), 0);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.powerBuffer = nbt.getLong("powerBuffer");
		this.hasCog = nbt.getBoolean("hasCog");
		this.overspeed = nbt.getInteger("overspeed");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setLong("powerBuffer", powerBuffer);
		nbt.setBoolean("hasCog", hasCog);
		nbt.setInteger("overspeed", overspeed);
		return nbt;
	}

	@Override
	public void setPower(long power) {
		this.powerBuffer = power;
	}

	@Override
	public long getPower() {
		return powerBuffer;
	}

	@Override
	public long getMaxPower() {
		return powerBuffer;
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
					this.pos.getX() - 1,
					this.pos.getY(),
					this.pos.getZ() - 1,
					this.pos.getX() + 2,
					this.pos.getY() + 2,
					this.pos.getZ() + 2
					);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public String getConfigName() {
		return "stirling";
	}

	@Override
	public void readIfPresent(JsonObject obj) {
		diffusion = IConfigurableMachine.grab(obj, "D:diffusion", diffusion);
		efficiency = IConfigurableMachine.grab(obj, "D:efficiency", efficiency);
		maxHeatNormal = IConfigurableMachine.grab(obj, "I:maxHeatNormal", maxHeatNormal);
		maxHeatSteel = IConfigurableMachine.grab(obj, "I:maxHeatSteel", maxHeatSteel);
		overspeedLimit = IConfigurableMachine.grab(obj, "I:overspeedLimit", overspeedLimit);
	}

	@Override
	public void writeConfig(JsonWriter writer) throws IOException {
		writer.name("D:diffusion").value(diffusion);
		writer.name("D:efficiency").value(efficiency);
		writer.name("I:maxHeatNormal").value(maxHeatNormal);
		writer.name("I:maxHeatSteel").value(maxHeatSteel);
		writer.name("I:overspeedLimit").value(overspeedLimit);
	}
}
