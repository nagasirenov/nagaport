package com.hbm.tileentity.machine.albion;

import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.tileentity.machine.albion.TileEntityPASource.Particle;
import com.hbm.util.fauxpointtwelve.BlockPos;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import com.hbm.lib.ForgeDirection;

public class TileEntityPABeamline extends TileEntityLoadedBase implements IParticleUser, net.minecraft.util.ITickable {
	
	public boolean window = false;
	public boolean didPass = false;
	public float light;
	public float prevLight;
	
	@Override
	public void update() {
		
		if(this.world.isRemote) {
			
			this.prevLight = this.light;
			if(this.light > 0) this.light -= 0.25F;
			
			if(this.light > this.prevLight) this.prevLight = this.light;
			
		} else {
			this.networkPackNT(150);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeBoolean(window);
		buf.writeBoolean(didPass);
		didPass = false;
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.window = buf.readBoolean();
		this.didPass = buf.readBoolean();
		if(didPass) light = 2F;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.window = nbt.getBoolean("window");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setBoolean("window", window);
		return nbt;
	}

	@Override
	public boolean canParticleEnter(Particle particle, ForgeDirection dir, int x, int y, int z) {
		ForgeDirection beamlineDir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10).getRotation(ForgeDirection.DOWN);
		BlockPos input = new BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ()).offset(beamlineDir, -1);
		return input.compare(x, y, z) && beamlineDir == dir;
	}

	@Override
	public void onEnter(Particle particle, ForgeDirection dir) {
		particle.addDistance(3);
		this.didPass = true;
	}

	@Override
	public BlockPos getExitPos(Particle particle) {
		ForgeDirection beamlineDir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10).getRotation(ForgeDirection.DOWN);
		return new BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ()).offset(beamlineDir, 2);
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
					this.pos.getY() + 1,
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
}
