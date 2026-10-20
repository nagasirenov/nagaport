package com.hbm.tileentity.network;

import com.hbm.tileentity.network.RequestNetwork.PathNode;
import com.hbm.util.fauxpointtwelve.BlockPos;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import com.hbm.lib.ForgeDirection;

public class TileEntityDroneWaypointRequest extends TileEntityRequestNetwork {

	public int height = 5;
	
	@Override
	public BlockPos getCoord() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata());
		return new BlockPos(this.pos.getX() + dir.offsetX * height, this.pos.getY() + dir.offsetY * height, this.pos.getZ() + dir.offsetZ * height);
	}
	
	public void addHeight(int h) {
		height += h;
		height = MathHelper.clamp(height, 1, 15);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.height = nbt.getInteger("height");
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setInteger("height", height);
		return nbt;
	}

	@Override
	public PathNode createNode(BlockPos pos) {
		return new PathNode(pos, this.reachableNodes);
	}
}
