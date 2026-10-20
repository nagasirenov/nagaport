package com.hbm.tileentity.machine.storage;

import com.hbm.lib.Library;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.energymk2.Nodespace;
import api.hbm.energymk2.Nodespace.PowerNode;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import com.hbm.lib.ForgeDirection;

public class TileEntityMachineFENSU extends TileEntityMachineBattery {

	public float prevRotation = 0F;
	public float rotation = 0F;
	
	public static final long maxTransfer = 10_000_000_000_000_000L;

	@Override public long getProviderSpeed() {
		int mode = this.getRelevantMode(true);
		return mode == mode_output || mode == mode_buffer ? maxTransfer : 0;
	}
	
	@Override public long getReceiverSpeed() {
		int mode = this.getRelevantMode(true);
		return mode == mode_input || mode == mode_buffer ? maxTransfer : 0;
	}
	
	@Override
	public void update() {
		
		if(!this.world.isRemote) {

			int mode = this.getRelevantMode(false);
			
			if(this.node == null || this.node.expired) {
				
				this.node = Nodespace.getNode(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());
				
				if(this.node == null || this.node.expired) {
					this.node = this.createNode();
					Nodespace.createNode(this.world, this.node);
				}
			}
			
			long prevPower = this.power;
			
			power = Library.chargeItemsFromTE(slots, 1, power, getMaxPower());
			
			if(mode == mode_output || mode == mode_buffer) {
				this.tryProvide(this.world, this.pos.getX(), this.pos.getY() - 1, this.pos.getZ(), ForgeDirection.DOWN);
			} else {
				if(node != null && node.hasValidNet()) node.net.removeProvider(this);
			}
			
			byte comp = this.getComparatorPower();
			if(comp != this.lastRedstone)
				this.markDirty();
			this.lastRedstone = comp;
			
			if(mode == mode_input || mode == mode_buffer) {
				if(node != null && node.hasValidNet()) node.net.addReceiver(this);
			} else {
				if(node != null && node.hasValidNet()) node.net.removeReceiver(this);
			}
			
			power = Library.chargeTEFromItems(slots, 0, power, getMaxPower());

			long avg = (power / 2 + prevPower / 2);
			this.delta = avg - this.log[0];
			
			for(int i = 1; i < this.log.length; i++) {
				this.log[i - 1] = this.log[i];
			}
			
			this.log[19] = avg;
			
			this.networkPackNT(20);
		}
		
		if(this.world.isRemote) {
			this.prevRotation = this.rotation;
			this.rotation += this.getSpeed();
			
			if(rotation >= 360) {
				rotation -= 360;
				prevRotation -= 360;
			}
		}
	}

	@Override
	public PowerNode createNode() {
		return new PowerNode(new BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ())).setConnections(new DirPos(this.pos.getX(), this.pos.getY() - 1, this.pos.getZ(), Library.NEG_Y));
	}

	@Override
	public long getPowerRemainingScaled(long i) {
		double powerScaled = (double)power / (double)getMaxPower();
		return (long)(i * powerScaled);
	}

	@Override
	public long getMaxPower() {
		return Long.MAX_VALUE;
	}
	
	public float getSpeed() {
		return (float) Math.pow(Math.log(power * 0.75 + 1) * 0.05F, 5);
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
	
	@Override
	public long transferPower(long power) {
		
		long overshoot = 0;
		
		// if power exceeds our transfer limit, truncate
		if(power > maxTransfer) {
			overshoot += power - maxTransfer;
			power = maxTransfer;
		}
		
		// this check is in essence the same as the default implementation, but re-arranged to never overflow the int64 range
		// if the remaining power exceeds the power cap, truncate again
		long freespace = this.getMaxPower() - this.getPower();
		
		if(freespace < power) {
			overshoot += power - freespace;
			power = freespace;
		}
		
		// what remains is sure to not exceed the transfer limit and the power cap (and therefore the int64 range)
		this.setPower(this.getPower() + power);
		
		return overshoot;
	}
}
