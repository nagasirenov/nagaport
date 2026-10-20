package com.hbm.tileentity.network;

import com.hbm.lib.Library;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.energymk2.Nodespace.PowerNode;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;
import com.hbm.lib.ForgeDirection;

public class TileEntityPylon extends TileEntityPylonBase {

	@Override
	public ConnectionType getConnectionType() {
		return ConnectionType.SINGLE;
	}

	@Override
	public Vec3d[] getMountPos() {
		return new Vec3d[] {new Vec3d(0.5, 5.4, 0.5)};
	}

	@Override
	public double getMaxWireLength() {
		return 25D;
	}

	@Override
	public PowerNode createNode() {
		TileEntity tile = (TileEntity) this;
		PowerNode node = new PowerNode(new BlockPos(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ())).setConnections(
				new DirPos(this.pos.getX(), this.pos.getY(), this.pos.getZ(), ForgeDirection.UNKNOWN),
				new DirPos(this.pos.getX() + 1, this.pos.getY(), this.pos.getZ(), Library.POS_X),
				new DirPos(this.pos.getX() - 1, this.pos.getY(), this.pos.getZ(), Library.NEG_X),
				new DirPos(this.pos.getX(), this.pos.getY() + 1, this.pos.getZ(), Library.POS_Y),
				new DirPos(this.pos.getX(), this.pos.getY() - 1, this.pos.getZ(), Library.NEG_Y),
				new DirPos(this.pos.getX(), this.pos.getY(), this.pos.getZ() + 1, Library.POS_Z),
				new DirPos(this.pos.getX(), this.pos.getY(), this.pos.getZ() - 1, Library.NEG_Z)
				);
		for(int[] pos : this.connected) node.addConnection(new DirPos(pos[0], pos[1], pos[2], ForgeDirection.UNKNOWN));
		return node;
	}
}
