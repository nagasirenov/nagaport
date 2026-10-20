package com.hbm.tileentity.network;

import com.hbm.blocks.BlockDummyable;
import com.hbm.lib.Library;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.energymk2.Nodespace.PowerNode;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;
import com.hbm.lib.ForgeDirection;

public class TileEntitySubstation extends TileEntityPylonBase {

	@Override
	public ConnectionType getConnectionType() {
		return ConnectionType.QUAD;
	}

	@Override
	public Vec3d[] getMountPos() {
		
		double topOff = 5.25;
		Vec3d vec = new Vec3d(1, 0, 0);

		switch(getBlockMetadata() - BlockDummyable.offset) {
		case 2: vec = vec.rotateYaw((float) Math.PI * 0.0F); break;
		case 4: vec = vec.rotateYaw((float) Math.PI * 0.5F); break;
		case 3: vec = vec.rotateYaw((float) Math.PI * 0.0F); break;
		case 5: vec = vec.rotateYaw((float) Math.PI * 0.5F); break;
		}
		
		return new Vec3d[] {
				new Vec3d(0.5 + vec.x * 0.5, topOff, 0.5 + vec.z * 0.5),
				new Vec3d(0.5 + vec.x * 1.5, topOff, 0.5 + vec.z * 1.5),
				new Vec3d(0.5 - vec.x * 0.5, topOff, 0.5 - vec.z * 0.5),
				new Vec3d(0.5 - vec.x * 1.5, topOff, 0.5 - vec.z * 1.5),
		};
	}

	@Override
	public Vec3d getConnectionPoint() {
		return new Vec3d(this.pos.getX() + 0.5, this.pos.getY() + 5.25, this.pos.getZ() + 0.5);
	}

	@Override
	public double getMaxWireLength() {
		return 20;
	}

	@Override
	public PowerNode createNode() {
		TileEntity tile = (TileEntity) this;
		PowerNode node = new PowerNode(new BlockPos(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ()),
				new BlockPos(tile.getPos().getX() + 1, tile.getPos().getY(), tile.getPos().getZ() + 1),
				new BlockPos(tile.getPos().getX() + 1, tile.getPos().getY(), tile.getPos().getZ() - 1),
				new BlockPos(tile.getPos().getX() - 1, tile.getPos().getY(), tile.getPos().getZ() + 1),
				new BlockPos(tile.getPos().getX() - 1, tile.getPos().getY(), tile.getPos().getZ() - 1)).setConnections(
				new DirPos(this.pos.getX(), this.pos.getY(), this.pos.getZ(), ForgeDirection.UNKNOWN),
				new DirPos(this.pos.getX() + 2, this.pos.getY(), this.pos.getZ() - 1, Library.POS_X),
				new DirPos(this.pos.getX() + 2, this.pos.getY(), this.pos.getZ() + 1, Library.POS_X),
				new DirPos(this.pos.getX() - 2, this.pos.getY(), this.pos.getZ() - 1, Library.NEG_X),
				new DirPos(this.pos.getX() - 2, this.pos.getY(), this.pos.getZ() + 1, Library.NEG_X),
				new DirPos(this.pos.getX() - 1, this.pos.getY(), this.pos.getZ() + 2, Library.POS_Z),
				new DirPos(this.pos.getX() + 1, this.pos.getY(), this.pos.getZ() + 2, Library.POS_Z),
				new DirPos(this.pos.getX() - 1, this.pos.getY(), this.pos.getZ() - 2, Library.NEG_Z),
				new DirPos(this.pos.getX() + 1, this.pos.getY(), this.pos.getZ() - 2, Library.NEG_Z)
				);
		for(int[] pos : this.connected) node.addConnection(new DirPos(pos[0], pos[1], pos[2], ForgeDirection.UNKNOWN));
		return node;
	}
}
