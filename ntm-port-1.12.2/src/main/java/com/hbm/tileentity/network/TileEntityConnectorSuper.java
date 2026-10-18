package com.hbm.tileentity.network;

import net.minecraft.util.math.Vec3d;
import com.hbm.lib.ForgeDirection;

public class TileEntityConnectorSuper extends TileEntityConnector {

	@Override
	public Vec3d[] getMountPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata());
		return new Vec3d[] {new Vec3d(0.5 + dir.offsetX * 0.375, 0.5 + dir.offsetY * 0.375, 0.5 + dir.offsetZ * 0.375)};
	}

	@Override
	public double getMaxWireLength() {
		return 100;
	}
}
