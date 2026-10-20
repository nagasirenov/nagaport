package com.hbm.tileentity.machine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.ModBlocks;
import com.hbm.tileentity.IConfigurableMachine;
import com.hbm.tileentity.TileEntityTickingBase;
import com.hbm.util.fauxpointtwelve.BlockPos;

import api.hbm.energymk2.IEnergyReceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import com.hbm.lib.ForgeDirection;

public class TileEntityICFController extends TileEntityTickingBase implements IEnergyReceiverMK2, IConfigurableMachine {
	
	public long power;
	public int laserLength;
	
	public int cellCount;
	public int emitterCount;
	public int capacitorCount;
	public int turbochargerCount;
	
	public static int capacitorPower = 2_500_000;
	public static int turboPower = 5_000_000;

	protected List<BlockPos> ports = new ArrayList();
	
	public boolean assembled;

	@Override
	public String getConfigName() {
		return "icfLaser";
	}

	@Override
	public void readIfPresent(JsonObject obj) {
		capacitorPower = IConfigurableMachine.grab(obj, "I:capacitorPower", capacitorPower);
		turboPower = IConfigurableMachine.grab(obj, "I:turboPower", turboPower);
	}

	@Override
	public void writeConfig(JsonWriter writer) throws IOException {
		writer.name("I:capacitorPower").value(capacitorPower);
		writer.name("I:turboPower").value(turboPower);
	}
	
	public void setup(HashSet<BlockPos> ports, HashSet<BlockPos> cells, HashSet<BlockPos> emitters, HashSet<BlockPos> capacitors, HashSet<BlockPos> turbochargers) {

		this.cellCount = 0;
		this.emitterCount = 0;
		this.capacitorCount = 0;
		this.turbochargerCount = 0;
		
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata()).getOpposite();
		BlockPos pos = new BlockPos(0, 0, 0);

		HashSet<BlockPos> validCells = new HashSet();
		HashSet<BlockPos> validEmitters = new HashSet();
		HashSet<BlockPos> validCapacitors = new HashSet();
		
		for(int i = 0; i < cells.size(); i++) {
			int j = i + 1;
			
			if(cells.contains(pos.mutate(this.pos.getX() + dir.offsetX * j, this.pos.getY(), this.pos.getZ() + dir.offsetZ * j))) {
				this.cellCount++;
				validCells.add(pos.clone());
			} else {
				break;
			}
		}
		
		for(BlockPos emitter : emitters) { for(ForgeDirection offset : ForgeDirection.VALID_DIRECTIONS) {
				pos.mutate(emitter.getX() + offset.offsetX, emitter.getY() + offset.offsetY, emitter.getZ() + offset.offsetZ);
				if(validCells.contains(pos)) { this.emitterCount++; validEmitters.add(emitter.clone()); break; }
			}
		}
		
		for(BlockPos capacitor : capacitors) { for(ForgeDirection offset : ForgeDirection.VALID_DIRECTIONS) {
				pos.mutate(capacitor.getX() + offset.offsetX, capacitor.getY() + offset.offsetY, capacitor.getZ() + offset.offsetZ);
				if(validEmitters.contains(pos)) { this.capacitorCount++; validCapacitors.add(capacitor.clone()); break; }
			}
		}
		
		for(BlockPos turbo : turbochargers) { for(ForgeDirection offset : ForgeDirection.VALID_DIRECTIONS) {
				pos.mutate(turbo.getX() + offset.offsetX, turbo.getY() + offset.offsetY, turbo.getZ() + offset.offsetZ);
				if(validCapacitors.contains(pos)) { this.turbochargerCount++; break; }
			}
		}
		
		this.ports.addAll(ports);
	}

	@Override
	public String getInventoryName() {
		return "container.icfController";
	}

	@Override
	public void update() {
		
		if(!this.world.isRemote) {
			
			this.networkPackNT(50);
			
			if(this.assembled) {
				for(BlockPos pos : ports) {
					for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
						BlockPos portPos = pos.offset(dir);
						if(this.getMaxPower() > 0) this.trySubscribe(this.world, portPos.getX(), portPos.getY(), portPos.getZ(), dir);
					}
				}
				
				if(this.power > 0) {
		
					ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata());
					
					for(int i = 1; i < 50; i++) {
						this.laserLength = i;
						
						Block b = com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + dir.offsetX * i, this.pos.getY(), this.pos.getZ() + dir.offsetZ * i).getBlock();
						if(b == ModBlocks.icf) {
							TileEntity tile = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(this.pos.getX() + dir.offsetX * (i + 8), this.pos.getY() - 3, this.pos.getZ() + dir.offsetZ * (i + 8)));
							if(tile instanceof TileEntityICF) {
								TileEntityICF icf = (TileEntityICF) tile;
								icf.laser += this.getPower();
								icf.maxLaser += this.getMaxPower();
								break;
							}
						}
						
						if(!b.isAir(this.world, this.pos.getX() + dir.offsetX * i, this.pos.getY(), this.pos.getZ() + dir.offsetZ * i)) {
							float hardness = b.getExplosionResistance(null);
							if(hardness < 6000) this.world.func_147480_a(this.pos.getX() + dir.offsetX * i, this.pos.getY(), this.pos.getZ() + dir.offsetZ * i, false);
							break;
						}
					}
		
					double blx = Math.min(this.pos.getX(), this.pos.getX() + dir.offsetX * laserLength) + 0.2;
					double bux = Math.max(this.pos.getX(), this.pos.getX() + dir.offsetX * laserLength) + 0.8;
					double bly = Math.min(this.pos.getY(), this.pos.getY() + dir.offsetY * laserLength) + 0.2;
					double buy = Math.max(this.pos.getY(), this.pos.getY() + dir.offsetY * laserLength) + 0.8;
					double blz = Math.min(this.pos.getZ(), this.pos.getZ() + dir.offsetZ * laserLength) + 0.2;
					double buz = Math.max(this.pos.getZ(), this.pos.getZ() + dir.offsetZ * laserLength) + 0.8;
					
					List<Entity> list = this.world.getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(blx, bly, blz, bux, buy, buz));
					
					for(Entity e : list) {
						e.attackEntityFrom(DamageSource.inFire, 50);
						e.setFire(5);
					}
					
					this.setPower(0);
				} else {
					this.laserLength = 0;
				}
				
			} else {
				this.laserLength = 0;
			}
		} else {
			
			if(this.laserLength > 0 && this.world.rand.nextInt(5) == 0) {
				ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata());
				ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
				double offXZ = this.world.rand.nextDouble() * 0.25 - 0.125;
				double offY = this.world.rand.nextDouble() * 0.25 - 0.125;
				double dist = 0.55;
				this.world.spawnParticle("reddust", this.pos.getX() + 0.5 + dir.offsetX * dist + rot.offsetX * offXZ, this.pos.getY() + 0.5 + offY, this.pos.getZ() + 0.5 + dir.offsetZ * dist + rot.offsetZ * offXZ, 0, 0, 0);
			}
		}
	}

	@Override public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeInt(capacitorCount);
		buf.writeInt(turbochargerCount);
		buf.writeInt(laserLength);
	}
	
	@Override public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
		this.capacitorCount = buf.readInt();
		this.turbochargerCount = buf.readInt();
		this.laserLength = buf.readInt();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		
		this.power = nbt.getLong("power");
		
		this.assembled = nbt.getBoolean("assembled");
		this.cellCount = nbt.getInteger("cellCount");
		this.emitterCount = nbt.getInteger("emitterCount");
		this.capacitorCount = nbt.getInteger("capacitorCount");
		this.turbochargerCount = nbt.getInteger("turbochargerCount");
		
		ports.clear();
		int portCount = nbt.getInteger("portCount");
		for(int i = 0; i < portCount; i++) {
			int[] port = nbt.getIntArray("p" + i);
			ports.add(new BlockPos(port[0], port[1], port[2]));
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		
		nbt.setLong("power", power);
		
		nbt.setBoolean("assembled", assembled);
		nbt.setInteger("cellCount", cellCount);
		nbt.setInteger("emitterCount", emitterCount);
		nbt.setInteger("capacitorCount", capacitorCount);
		nbt.setInteger("turbochargerCount", turbochargerCount);
		
		nbt.setInteger("portCount", ports.size());
		for(int i = 0; i < ports.size(); i++) {
			BlockPos pos = ports.get(i);
			nbt.setIntArray("p" + i, new int[] { pos.getX(), pos.getY(), pos.getZ() });
		}
		return nbt;
	}

	@Override
	public long getPower() {
		return Math.min(power, this.getMaxPower());
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public long getMaxPower() {
		return (long) (Math.sqrt(capacitorCount) * capacitorPower + Math.sqrt(Math.min(turbochargerCount, capacitorCount)) * turboPower);
	}
	
	AxisAlignedBB bb = null;
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		
		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
					this.pos.getX() + 0.5 - 50,
					this.pos.getY(),
					this.pos.getZ() + 0.5 - 50,
					this.pos.getX() + 0.5 + 50,
					this.pos.getY() + 1,
					this.pos.getZ() + 0.5 + 50
					);
		}
		
		return bb;
	}
}
