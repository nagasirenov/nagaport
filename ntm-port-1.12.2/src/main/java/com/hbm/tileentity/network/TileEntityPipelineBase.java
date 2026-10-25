package com.hbm.tileentity.network;

import java.util.ArrayList;
import java.util.List;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.uninos.UniNodespace;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.fluidmk2.FluidNode;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import com.hbm.lib.ForgeDirection;

// copy pasted crap class
public abstract class TileEntityPipelineBase extends TileEntityPipeBaseNT {

	protected List<int[]> connected = new ArrayList<>();

	@Override
	public FluidNode createNode(FluidType type) {
		TileEntity tile = (TileEntity) this;
		FluidNode node = new FluidNode(type.getNetworkProvider(), new BlockPos(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ())).setConnections(new DirPos(this.pos.getX(), this.pos.getY(), this.pos.getZ(), ForgeDirection.UNKNOWN));
		for(int[] pos : this.connected) node.addConnection(new DirPos(pos[0], pos[1], pos[2], ForgeDirection.UNKNOWN));
		return node;
	}

	public void addConnection(int x, int y, int z) {

		connected.add(new int[] {x, y, z});

		FluidNode node = (FluidNode) UniNodespace.getNode(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.type.getNetworkProvider());
		node.recentlyChanged = true;
		node.addConnection(new DirPos(x, y, z, ForgeDirection.UNKNOWN));

		this.markDirty();

		if(this.world instanceof WorldServer) {
			WorldServer world = (WorldServer) this.world;
			world.getPlayerChunkMap().markBlockForUpdate(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ()));
		}
	}

	public void disconnectAll() {

		for(int[] pos : connected) {
			TileEntity te = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));
			if(te == this) continue;

			if(te instanceof TileEntityPipelineBase) {
				TileEntityPipelineBase pipeline = (TileEntityPipelineBase) te;
				UniNodespace.destroyNode(this.world, pos[0], pos[1], pos[2], this.type.getNetworkProvider());

				for(int i = 0; i < pipeline.connected.size(); i++) {
					int[] conPos = pipeline.connected.get(i);

					if(conPos[0] == this.pos.getX() && conPos[1] == this.pos.getY() && conPos[2] == this.pos.getZ()) {
						pipeline.connected.remove(i);
						i--;
					}
				}

				pipeline.markDirty();

				if(this.world instanceof WorldServer) {
					WorldServer world = (WorldServer) this.world;
					world.getPlayerChunkMap().markBlockForUpdate(new net.minecraft.util.math.BlockPos(pipeline.getPos().getX(), pipeline.getPos().getY(), pipeline.getPos().getZ()));
				}
			}
		}

		UniNodespace.destroyNode(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.type.getNetworkProvider());
	}

	@Override
	public void invalidate() {
		super.invalidate();
		disconnectAll();
	}

	/**
	 * Returns a status code based on the operation.<br>
	 * 0: Connected<br>
	 * 1: Connections are incompatible<br>
	 * 2: Both parties are the same block<br>
	 * 3: Connection length exceeds maximum
	 * 4: Pipeline fluid types do not match
	 */
	public static int canConnect(TileEntityPipelineBase first, TileEntityPipelineBase second) {

		if(first.getConnectionType() != second.getConnectionType()) return 1;
		if(first == second) return 2;
		if(first.type != second.type) return 4;

		double len = Math.min(first.getMaxPipeLength(), second.getMaxPipeLength());

		Vec3d firstPos = first.getConnectionPoint();
		Vec3d secondPos = second.getConnectionPoint();

		Vec3d delta = new Vec3d(
				(secondPos.x) - (firstPos.x),
				(secondPos.y) - (firstPos.y),
				(secondPos.z) - (firstPos.z)
				);

		return len >= delta.length() ? 0 : 3;
	}

	public abstract ConnectionType getConnectionType();
	public abstract Vec3d getMountPos();
	public abstract double getMaxPipeLength();

	public Vec3d getConnectionPoint() {
		Vec3d mount = this.getMountPos();
		return mount.add(this.pos.getX(), this.pos.getY(), this.pos.getZ());
	}

	public List<int[]> getConnected() {
		return connected;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setInteger("conCount", connected.size());

		for(int i = 0; i < connected.size(); i++) {
			nbt.setIntArray("con" + i, connected.get(i));
		}
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		int count = nbt.getInteger("conCount");

		this.connected.clear();

		for(int i = 0; i < count; i++) {
			connected.add(nbt.getIntArray("con" + i));
		}
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {

		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		return new SPacketUpdateTileEntity(this.getPos(), 0, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		this.readFromNBT(pkt.getNbtCompound());
	}

	public enum ConnectionType {
		SMALL
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB; // not great!
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}
