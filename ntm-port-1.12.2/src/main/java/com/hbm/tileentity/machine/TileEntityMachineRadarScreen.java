package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.tileentity.IBufPacketReceiver;

import api.hbm.entity.RadarEntry;
import com.hbm.tileentity.TileEntityLoadedBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityMachineRadarScreen extends TileEntityLoadedBase implements IBufPacketReceiver, net.minecraft.util.ITickable {

	public List<RadarEntry> entries = new ArrayList();
	public int refX;
	public int refY;
	public int refZ;
	public int range;
	public boolean linked;

	@Override
	public void update() {

		if(!this.world.isRemote) {
			this.networkPackNT(100);
			entries.clear();
			this.linked = false;
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeBoolean(linked);
		buf.writeInt(refX);
		buf.writeInt(refY);
		buf.writeInt(refZ);
		buf.writeInt(range);
		buf.writeInt(entries.size());
		for(RadarEntry entry : entries) entry.toBytes(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		linked = buf.readBoolean();
		refX = buf.readInt();
		refY = buf.readInt();
		refZ = buf.readInt();
		range = buf.readInt();
		int count = buf.readInt();
		this.entries.clear();
		for(int i = 0; i < count; i++) {
			RadarEntry entry = new RadarEntry();
			entry.fromBytes(buf);
			this.entries.add(entry);
		}
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
}
