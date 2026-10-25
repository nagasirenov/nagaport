package com.hbm.tileentity.machine;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.dim.trait.CBT_Dyson;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.BlockAllocatorStandard;
import com.hbm.explosion.vanillant.standard.BlockMutatorFire;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.explosion.vanillant.standard.CustomDamageHandlerDyson;
import com.hbm.explosion.vanillant.standard.EntityProcessorStandard;
import com.hbm.explosion.vanillant.standard.ExplosionEffectStandard;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.items.ISatChip;
import com.hbm.main.MainRegistry;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.saveddata.satellites.SatelliteDysonRelay;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IDysonConverter;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.ParticleUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.EnumSkyBlock;
import com.hbm.lib.ForgeDirection;

public class TileEntityDysonReceiver extends TileEntityMachineBase {

	// Connects to a dyson swarm via ID, receiving energy during the day
	// also receives energy at night if a satellite relay is in orbit around the planet

	// The energy received is fired as a violently powerful beam,
	// converters can collect this beam and turn it into HE/TU or used for analysis, crafting, etc.

	public boolean isReceiving;
	public int swarmId;
	public int swarmCount;
	public int swarmConsumers;
	public int beamLength;

	private AudioWrapper audio;

	public TileEntityDysonReceiver() {
		super(1);
	}

	// Sun luminosity is 4*10^26, which we can't represent in any Java integer primitive
	// therefore the upper bound for power generation is higher than a FEnSU, effectively
	// reality doesn't provide any interesting solutions that make the system engaging to use
	// so we're going to build our own power curve.
	// We need to encourage players to build large swarms, so single satellites must suck but together they produce enormous power
	// Gompertz is a funne name
	public static long getEnergyOutput(int swarmCount) {
		double adjustedDensity = (double)swarmCount / 1024.0D;
		long maxOutput = Long.MAX_VALUE / 10;
		double b = 32.0D;
		double c = 1.3D;
		double gompertz = Math.exp(-b * Math.exp(-c * adjustedDensity));
		return (long)(maxOutput * gompertz) / 20;
	}

	@Override
	public String getDefaultName() {
		return "container.machineDysonReceiver";
	}

	@Override
	public void update() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getOpposite();

		if(!this.world.isRemote) {
			swarmId = ISatChip.getFreqS(slots[0]);

			SatelliteSavedData data = SatelliteSavedData.getData(this.world, this.pos.getX(), this.pos.getZ());
			Satellite sat = data.getSatFromFreq(swarmId);
			int sun = this.world.getLightFor(EnumSkyBlock.SKY, new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ())) - this.world.getSkylightSubtracted() - 11;

			boolean occluded = false;
			for(int x = -3; x <= 3; x++) {
				for(int z = -3; z <= 3; z++) {
					if(this.world.getHeight(this.pos.getX() + x, this.pos.getZ() + z) > this.pos.getY() + 10) {
						occluded = true;
						break;
					}
				}
			}

			swarmCount = CBT_Dyson.count(this.world, swarmId);
			swarmConsumers = CBT_Dyson.consumers(this.world, swarmId);

			isReceiving = (sat instanceof SatelliteDysonRelay || sun > 0) && swarmId > 0 && !occluded && swarmCount > 0 && swarmConsumers > 0;

			if(isReceiving) {
				long energyOutput = getEnergyOutput(swarmCount) / swarmConsumers;
				int maxLength = 24;

				beamLength = maxLength;
				for(int i = 9; i < maxLength; i++) {
					int x = this.pos.getX() + dir.offsetX * i;
					int y = this.pos.getY() + 1;
					int z = this.pos.getZ() + dir.offsetZ * i;

					Block block = com.hbm.world.WorldBlockState.read(this.world, x, y, z).getBlock();

					// two block gap minimum
					boolean detonate = true;
					TileEntity te = null;
					if(i > 10) {
						if(block instanceof BlockDummyable) {
							int[] pos = ((BlockDummyable) block).findCore(this.world, x, y, z);
							if(pos != null) {
								te = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));
							}
						} else {
							te = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
						}

						if(te instanceof IDysonConverter) {
							detonate = !((IDysonConverter) te).provideEnergy(x, y, z, energyOutput);
						}
					}

					if(block.getDefaultState().isOpaqueCube() || te != null) {
						if(detonate) {
							com.hbm.world.WorldAirRemoval.remove(this.world, x, y, z);

							ExplosionVNT vnt = new ExplosionVNT(this.world, x, y, z, 3, null);
							vnt.setBlockAllocator(new BlockAllocatorStandard());
							vnt.setBlockProcessor(new BlockProcessorStandard().withBlockEffect(new BlockMutatorFire()));
							vnt.setEntityProcessor(new EntityProcessorStandard().allowSelfDamage());
							vnt.setPlayerProcessor(new PlayerProcessorStandard());
							vnt.setSFX(new ExplosionEffectStandard());
							vnt.explode();
						}

						beamLength = i;
						break;
					}
				}


				double blx = Math.min(this.pos.getX(), this.pos.getX() + dir.offsetX * beamLength) + 0.2;
				double bux = Math.max(this.pos.getX(), this.pos.getX() + dir.offsetX * beamLength) + 0.8;
				double bly = Math.min(this.pos.getY(), 1 + this.pos.getY() + dir.offsetY * beamLength) + 0.2;
				double buy = Math.max(this.pos.getY(), 1 + this.pos.getY() + dir.offsetY * beamLength) + 0.8;
				double blz = Math.min(this.pos.getZ(), this.pos.getZ() + dir.offsetZ * beamLength) + 0.2;
				double buz = Math.max(this.pos.getZ(), this.pos.getZ() + dir.offsetZ * beamLength) + 0.8;

				@SuppressWarnings("unchecked")
				List<EntityLivingBase> list = this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(blx, bly, blz, bux, buy, buz));

				for(EntityLivingBase entity : list) {
					ExplosionVNT vnt = new ExplosionVNT(this.world, entity.posX - dir.offsetX, entity.posY + 1.5, entity.posZ - dir.offsetZ, 3, null);
					vnt.setBlockAllocator(new BlockAllocatorStandard());
					vnt.setBlockProcessor(new BlockProcessorStandard().withBlockEffect(new BlockMutatorFire()));
					vnt.setEntityProcessor(new EntityProcessorStandard().withDamageMod(new CustomDamageHandlerDyson(energyOutput)));
					vnt.setPlayerProcessor(new PlayerProcessorStandard());
					vnt.setSFX(new ExplosionEffectStandard());
					vnt.explode();
				}
			}

			networkPackNT(250);
		} else {
			if(isReceiving) {
				if(audio == null) {
					audio = MainRegistry.proxy.getLoopedSound("hbm:block.dysonBeam", this.pos.getX() + dir.offsetX * 8, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 8, 0.75F, 20F, 1.0F, 20);
					audio.startSound();
				}

				audio.keepAlive();
				audio.updatePitch(0.85F);

				if(this.world.rand.nextInt(10) == 0) {
					ParticleUtil.spawnFlare(this.world, this.pos.getX() - 5 + this.world.rand.nextDouble() * 10, this.pos.getY() + 11, this.pos.getZ() - 5 + this.world.rand.nextDouble() * 10, 0, 0.1 + this.world.rand.nextFloat() * 0.1, 0, 4F + this.world.rand.nextFloat() * 2);
				}
			} else {
				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeBoolean(isReceiving);
		buf.writeInt(swarmId);
		buf.writeInt(swarmCount);
		buf.writeInt(swarmConsumers);
		buf.writeInt(beamLength);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		isReceiving = buf.readBoolean();
		swarmId = buf.readInt();
		swarmCount = buf.readInt();
		swarmConsumers = buf.readInt();
		beamLength = buf.readInt();
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();

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

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = new AxisAlignedBB(
				this.pos.getX() - 25,
				this.pos.getY(),
				this.pos.getZ() - 25,
				this.pos.getX() + 25,
				this.pos.getY() + 19,
				this.pos.getZ() + 25
			);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	public static void runTests() {
		for(int i = 1; i < 5000; i *= 2) {
			MainRegistry.logger.info(i + " dyson swarm members produces: " + BobMathUtil.getShortNumber(getEnergyOutput(i) * 20) + "HE/s");
		}
	}

}
