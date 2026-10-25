package com.hbm.tileentity.machine;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystem;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Rocket;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.i18n.I18nUtil;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.fluid.IFluidStandardReceiver;
import api.hbm.tile.IPropulsion;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import com.hbm.lib.ForgeDirection;

public class TileEntityMachineLPW2 extends TileEntityMachineBase implements IPropulsion, IFluidStandardReceiver {

	public FluidTank[] tanks;

	private boolean isOn;
	private float speed;
	public double lastTime;
	public double time;
	private float soundtime;
	private AudioWrapper audio;

	private boolean hasRegistered;

	private int fuelCost;

	public TileEntityMachineLPW2() {
		super(0);
		tanks = new FluidTank[2];
		tanks[0] = new FluidTank(Fluids.KEROSENE_REFORM, 256_000);
		tanks[1] = new FluidTank(Fluids.OXYGEN, 256_000);
	}

	@Override
	public void update() {
		if(!CelestialBody.inOrbit(this.world)) return;

		if(!this.world.isRemote) {
			if(!hasRegistered) {
				if(isFacingPrograde()) registerPropulsion();
				hasRegistered = true;
				isOn = false;
			}

			for(DirPos pos : getConPos()) {
				for(FluidTank tank : tanks) {
					trySubscribe(tank.getTankType(), this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
				}
			}

			if(isOn) {
				soundtime++;

				if(soundtime == 1) {
					com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), "hbm:misc.lpwstart", 1.5F, 1F);
				} else if(soundtime > 20) {
					soundtime = 20;
				}
			} else {
				soundtime--;

				if(soundtime == 19) {
					com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), "hbm:misc.lpwstop", 2.0F, 1F);
				} else if(soundtime <= 0) {
					soundtime = 0;
				}
			}

			networkPackNT(250);
		} else {
			if(isOn) {
				speed += 0.05D;
				if(speed > 1) speed = 1;

				if(soundtime > 18) {
					if(audio == null) {
						audio = createAudioLoop();
						audio.startSound();
					} else if(!audio.isPlaying()) {
						audio = rebootAudio(audio);
					}

					audio.updateVolume(getVolume(1F));
					audio.keepAlive();

					ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getRotation(ForgeDirection.UP);

					NBTTagCompound data = new NBTTagCompound();
					data.setDouble("posX", this.pos.getX() + dir.offsetX * 8.5);
					data.setDouble("posY", this.pos.getY() + 4.5);
					data.setDouble("posZ", this.pos.getZ() + dir.offsetZ * 8.5);
					data.setString("type", "missileContrail");
					data.setFloat("scale", 3);
					data.setDouble("moX", dir.offsetX * 10);
					data.setDouble("moY", 0);
					data.setDouble("moZ", dir.offsetZ * 10);
					data.setInteger("maxAge", 20 + this.world.rand.nextInt(20));
					MainRegistry.proxy.effectNT(data);
				}
			} else {
				speed -= 0.05D;
				if(speed < 0) speed = 0;

				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
			}

		}

		lastTime = time;
		time += speed;
	}

	private DirPos[] getConPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		return new DirPos[] {
			new DirPos(this.pos.getX() + dir.offsetX * 4 - rot.offsetX, this.pos.getY() + 3, this.pos.getZ() + dir.offsetZ * 4 - rot.offsetZ, rot),
			new DirPos(this.pos.getX() - dir.offsetX * 4 - rot.offsetX, this.pos.getY() + 3, this.pos.getZ() - dir.offsetZ * 4 - rot.offsetZ, rot.getOpposite())
		};
	}

	@Override
	public AudioWrapper createAudioLoop() {
		return MainRegistry.proxy.getLoopedSound("hbm:misc.lpwloop", this.pos.getX(), this.pos.getY(), this.pos.getZ(), 0.25F, 27.5F, 1.0F, 20);
	}

	@Override
	public void invalidate() {
		super.invalidate();

		if(hasRegistered) {
			unregisterPropulsion();
			hasRegistered = false;
		}

		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();

		if(hasRegistered) {
			unregisterPropulsion();
			hasRegistered = false;
		}

		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeBoolean(isOn);
		buf.writeFloat(soundtime);
		buf.writeInt(fuelCost);
		for(int i = 0; i < tanks.length; i++) tanks[i].serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		isOn = buf.readBoolean();
		soundtime = buf.readFloat();
		fuelCost = buf.readInt();
		for(int i = 0; i < tanks.length; i++) tanks[i].deserialize(buf);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setBoolean("on", isOn);
		for(int i = 0; i < tanks.length; i++) tanks[i].writeToNBT(nbt, "t" + i);
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		isOn = nbt.getBoolean("on");
		for(int i = 0; i < tanks.length; i++) tanks[i].readFromNBT(nbt, "t" + i);
	}

	public boolean isFacingPrograde() {
		return ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset) == ForgeDirection.SOUTH;
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if(bb == null) bb = new AxisAlignedBB(this.pos.getX() - 10, this.pos.getY(), this.pos.getZ() - 10, this.pos.getX() + 11, this.pos.getY() + 7, this.pos.getZ() + 11);
		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public TileEntity getTileEntity() {
		return this;
	}

	@Override
	public boolean canPerformBurn(int shipMass, double deltaV) {
		FT_Rocket trait = tanks[0].getTankType().getTrait(FT_Rocket.class);
		int isp = trait != null ? trait.getISP() : 300;

		fuelCost = SolarSystem.getFuelCost(deltaV, shipMass, isp);

		for(FluidTank tank : tanks) {
			if(tank.getFill() < fuelCost) return false;
		}

		return true;
	}

	@Override
	public void addErrors(List<String> errors) {
		for(FluidTank tank : tanks) {
			if(tank.getFill() < fuelCost) {
				errors.add(TextFormatting.RED + I18nUtil.resolveKey(getBlockType().getTranslationKey() + ".name") + " - Insufficient fuel: needs " + fuelCost + "mB");
			}
		}
	}

	@Override
	public float getThrust() {
		return 2_000_000_000.0F; // F1 thrust
	}

	@Override
	public int startBurn() {
		isOn = true;
		for(FluidTank tank : tanks) {
			tank.setFill(tank.getFill() - fuelCost);
		}
		return 120;
	}

	@Override
	public int endBurn() {
		isOn = false;
		return 120; // Cooldown
	}

	@Override
	public String getDefaultName() {
		return "container.lpw";
	}

	@Override
	public FluidTank[] getAllTanks() {
		return tanks;
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return tanks;
	}
}
