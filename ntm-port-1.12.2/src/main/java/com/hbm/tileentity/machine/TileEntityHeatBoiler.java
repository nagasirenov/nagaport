package com.hbm.tileentity.machine;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.BlockDummyable;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.EntityProcessorStandard;
import com.hbm.explosion.vanillant.standard.ExplosionEffectStandard;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Heatable;
import com.hbm.inventory.fluid.trait.FT_Heatable.HeatingStep;
import com.hbm.inventory.fluid.trait.FT_Heatable.HeatingType;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.saveddata.TomSaveData;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.IConfigurableMachine;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import api.hbm.tile.IHeatSource;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.EnumSkyBlock;
import com.hbm.lib.ForgeDirection;

public class TileEntityHeatBoiler extends TileEntityLoadedBase implements IBufPacketReceiver, IFluidStandardTransceiverMK2, IConfigurableMachine, IFluidCopiable, net.minecraft.util.ITickable {

	public int heat;
	public FluidTank[] tanks;
	public boolean isOn;
	public boolean hasExploded = false;

	private AudioWrapper audio;
	private int audioTime;

	/* CONFIGURABLE */
	public static int maxHeat = 3_200_000;
	public static double diffusion = 0.1D;
	public static boolean canExplode = true;

	public TileEntityHeatBoiler() {
		this.tanks = new FluidTank[2];

		this.tanks[0] = new FluidTank(Fluids.WATER, 16_000);
		this.tanks[1] = new FluidTank(Fluids.STEAM, 16_000 * 100);
	}

	ByteBuf buf;

	@Override
	public void update() {

		if(!this.world.isRemote) {

			if(this.buf != null)
				this.buf.release();
			this.buf = Unpooled.buffer();

			buf.writeBoolean(this.hasExploded);

			if(!this.hasExploded) {
				this.setupTanks();
				this.updateConnections();
				this.tryPullHeat();
				int lastHeat = this.heat;

				int light = this.world.getLightFor(EnumSkyBlock.SKY, new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ()));
				if(light > 7 && TomSaveData.forWorld(this.world).fire > 1e-5) {
					this.heat += ((maxHeat - heat) * 0.000005D); //constantly heat up 0.0005% of the remaining heat buffer for rampant but diminishing heating
				}

				buf.writeInt(lastHeat);

				tanks[0].serialize(buf);
				this.isOn = false;
				this.tryConvert();
				tanks[1].serialize(buf);

				if(this.tanks[1].getFill() > 0) {
					this.sendFluid();
				}
			}

			buf.writeBoolean(this.muffled);
			buf.writeBoolean(this.isOn);
			networkPackNT(25);
		} else {

			if(this.isOn) audioTime = 20;

			if(audioTime > 0) {

				audioTime--;

				if(audio == null) {
					audio = createAudioLoop();
					audio.startSound();
				} else if(!audio.isPlaying()) {
					audio = rebootAudio(audio);
				}

				audio.updateVolume(getVolume(1F));
				audio.keepAlive();

			} else {

				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
			}
		}
	}

	@Override
	public AudioWrapper createAudioLoop() {
		return MainRegistry.proxy.getLoopedSound("hbm:block.boiler", this.pos.getX(), this.pos.getY(), this.pos.getZ(), 0.125F, 10F, 1.0F, 20);
	}

	@Override
	public void onChunkUnload() {

		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public void invalidate() {

		super.invalidate();

		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeBytes(this.buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.hasExploded = buf.readBoolean();
		if (!this.hasExploded) {
			this.heat = buf.readInt();
			this.tanks[0].deserialize(buf);
			this.tanks[1].deserialize(buf);
			this.muffled = buf.readBoolean();
			this.isOn = buf.readBoolean();
		}
	}

	protected void tryPullHeat() {
		TileEntity con = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY() - 1, this.pos.getZ()));

		if(con instanceof IHeatSource) {
			IHeatSource source = (IHeatSource) con;
			int diff = source.getHeatStored() - this.heat;

			if(diff == 0) {
				return;
			}

			if(diff > 0) {
				diff = (int) Math.ceil(diff * diffusion);
				diff = Math.min(diff, this.maxHeat - this.heat);
				source.useUpHeat(diff);
				this.heat += diff;
				if(this.heat > this.maxHeat)
					this.heat = this.maxHeat;
				return;
			}
		}

		this.heat = Math.max(this.heat - Math.max(this.heat / 1000, 1), 0);
	}

	protected void setupTanks() {

		if(tanks[0].getTankType().hasTrait(FT_Heatable.class)) {
			FT_Heatable trait = tanks[0].getTankType().getTrait(FT_Heatable.class);
			if(trait.getEfficiency(HeatingType.BOILER) > 0) {
				HeatingStep entry = trait.getFirstStep();
				tanks[1].setTankType(entry.typeProduced);
				tanks[1].changeTankSize(tanks[0].getMaxFill() * entry.amountProduced / entry.amountReq);
				return;
			}
		}

		tanks[0].setTankType(Fluids.NONE);
		tanks[1].setTankType(Fluids.NONE);
	}

	protected void tryConvert() {

		if(tanks[0].getTankType().hasTrait(FT_Heatable.class)) {
			FT_Heatable trait = tanks[0].getTankType().getTrait(FT_Heatable.class);
			if(trait.getEfficiency(HeatingType.BOILER) > 0) {

				HeatingStep entry = trait.getFirstStep();
				int heatReq = (int) Math.max(entry.heatReq / trait.getEfficiency(HeatingType.BOILER), 1);
				int inputOps = this.tanks[0].getFill() / entry.amountReq;
				int outputOps = (this.tanks[1].getMaxFill() - this.tanks[1].getFill()) / entry.amountProduced;
				int heatOps = this.heat / heatReq;

				int ops = Math.min(inputOps, Math.min(outputOps, heatOps));

				this.tanks[0].setFill(this.tanks[0].getFill() - entry.amountReq * ops);
				this.tanks[1].setFill(this.tanks[1].getFill() + entry.amountProduced * ops);
				this.heat -= heatReq * ops;

				if(ops > 0 && this.world.rand.nextInt(400) == 0) {
					com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX() + 0.5, this.pos.getY() + 2, this.pos.getZ() + 0.5, "hbm:block.boilerGroan", 0.5F, 1.0F);
				}

				if(ops > 0) {
					this.isOn = true;
				}

				if(outputOps == 0 && canExplode) {
					this.hasExploded = true;
					BlockDummyable.safeRem = true;
					for(int x = this.pos.getX() - 1; x <= this.pos.getX() + 1; x++) {
						for(int y = this.pos.getY() + 2; y <= this.pos.getY() + 3; y++) {
							for(int z = this.pos.getZ() - 1; z <= this.pos.getZ() + 1; z++) {
								com.hbm.world.WorldAirRemoval.remove(this.world, x, y, z);
							}
						}
					}
					com.hbm.world.WorldAirRemoval.remove(this.world, this.pos.getX(), this.pos.getY() + 1, this.pos.getZ());

					ExplosionVNT xnt = new ExplosionVNT(this.world, this.pos.getX() + 0.5, this.pos.getY() + 2, this.pos.getZ() + 0.5, 5F);
					xnt.setEntityProcessor(new EntityProcessorStandard().withRangeMod(3F));
					xnt.setPlayerProcessor(new PlayerProcessorStandard());
					xnt.setSFX(new ExplosionEffectStandard());
					xnt.explode();

					BlockDummyable.safeRem = false;
				}
			}
		}
	}

	private void updateConnections() {

		for(DirPos pos : getConPos()) {
			this.trySubscribe(tanks[0].getTankType(), this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
		}
	}

	private void sendFluid() {

		for(DirPos pos : getConPos()) {
			this.tryProvide(tanks[1], this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
		}
	}

	private DirPos[] getConPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getRotation(ForgeDirection.UP);
		return new DirPos[] {
				new DirPos(this.pos.getX() + dir.offsetX * 2, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 2, dir),
				new DirPos(this.pos.getX() - dir.offsetX * 2, this.pos.getY(), this.pos.getZ() - dir.offsetZ * 2, dir.getOpposite()),
				new DirPos(this.pos.getX(), this.pos.getY() + 4, this.pos.getZ(), Library.POS_Y),
		};
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		tanks[0].readFromNBT(nbt, "water");
		tanks[1].readFromNBT(nbt, "steam");
		heat = nbt.getInteger("heat");
		hasExploded = nbt.getBoolean("exploded");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		tanks[0].writeToNBT(nbt, "water");
		tanks[1].writeToNBT(nbt, "steam");
		nbt.setInteger("heat", heat);
		nbt.setBoolean("exploded", hasExploded);
		return nbt;
	}

	@Override
	public FluidTank[] getAllTanks() {
		return tanks;
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] {tanks[1]};
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] {tanks[0]};
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = new AxisAlignedBB(
					this.pos.getX() - 1,
					this.pos.getY(),
					this.pos.getZ() - 1,
					this.pos.getX() + 2,
					this.pos.getY() + 4,
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
		return "boiler";
	}

	@Override
	public void readIfPresent(JsonObject obj) {
		maxHeat = IConfigurableMachine.grab(obj, "I:maxHeat", maxHeat);
		diffusion = IConfigurableMachine.grab(obj, "D:diffusion", diffusion);
		canExplode = IConfigurableMachine.grab(obj, "B:canExplode", canExplode);
	}

	@Override
	public void writeConfig(JsonWriter writer) throws IOException {
		writer.name("I:maxHeat").value(maxHeat);
		writer.name("D:diffusion").value(diffusion);
		writer.name("B:canExplode").value(canExplode);
	}
}
