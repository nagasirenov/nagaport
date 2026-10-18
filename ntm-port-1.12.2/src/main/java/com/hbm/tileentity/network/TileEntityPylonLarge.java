package com.hbm.tileentity.network;

import com.hbm.blocks.BlockDummyable;

import net.minecraft.util.math.Vec3d;

public class TileEntityPylonLarge extends TileEntityPylonBase {

	@Override
	public ConnectionType getConnectionType() {
		return ConnectionType.QUAD;
	}

	@Override
	public Vec3d[] getMountPos() {
		double topOff = 0.75 + 0.0625;
		double sideOff = 3.375;
		
		Vec3d vec = new Vec3d(sideOff, 0, 0);

		switch(getBlockMetadata() - BlockDummyable.offset) {
		case 2: vec = vec.rotateYaw((float) Math.PI * 0.0F); break;
		case 4: vec = vec.rotateYaw((float) Math.PI * 0.25F); break;
		case 3: vec = vec.rotateYaw((float) Math.PI * 0.5F); break;
		case 5: vec = vec.rotateYaw((float) Math.PI * 0.75F); break;
		}
		
		return new Vec3d[] {
				new Vec3d(0.5 + vec.x, 11.5 + topOff, 0.5 + vec.z),
				new Vec3d(0.5 + vec.x, 11.5 - topOff, 0.5 + vec.z),
				new Vec3d(0.5 - vec.x, 11.5 + topOff, 0.5 - vec.z),
				new Vec3d(0.5 - vec.x, 11.5 - topOff, 0.5 - vec.z),
		};
	}

	@Override
	public double getMaxWireLength() {
		return 100;
	}
}
