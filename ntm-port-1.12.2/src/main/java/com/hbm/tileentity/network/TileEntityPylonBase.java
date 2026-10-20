package com.hbm.tileentity.network;

import java.util.ArrayList;
import java.util.List;

import com.hbm.util.ColorUtil;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.energymk2.Nodespace;
import api.hbm.energymk2.Nodespace.PowerNode;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import com.hbm.lib.ForgeDirection;

public abstract class TileEntityPylonBase extends TileEntityCableBaseNT {

	protected List<int[]> connected = new ArrayList<>();
	public int color;

	public static int canConnect(TileEntityPylonBase first, TileEntityPylonBase second) {

		if(first.getConnectionType() != second.getConnectionType())
			return 1;

		if(first == second)
			return 2;

		double len = Math.min(first.getMaxWireLength(), second.getMaxWireLength());

		Vec3d firstPos = first.getConnectionPoint();
		Vec3d secondPos = second.getConnectionPoint();

		Vec3d delta = new Vec3d(
				(secondPos.x) - (firstPos.x),
				(secondPos.y) - (firstPos.y),
				(secondPos.z) - (firstPos.z)
				);

		return len >= delta.length() ? 0 : 3;
	}

	public boolean setColor(ItemStack stack) {
		if(stack == null) return false;
		int color = ColorUtil.getColorFromDye(stack);
		if(color == 0 || color == this.color) return false;
		stack.stackSize--;
		this.color = color;

		this.markDirty();
		if(this.world instanceof WorldServer) {
			WorldServer world = (WorldServer) this.world;
			world.getPlayerManager().markBlockForUpdate(this.pos.getX(), this.pos.getY(), this.pos.getZ());
		}

		return true;
	}

	@Override
	public PowerNode createNode() {
		TileEntity tile = (TileEntity) this;
		PowerNode node = new PowerNode(new BlockPos(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ())).setConnections(new DirPos(this.pos.getX(), this.pos.getY(), this.pos.getZ(), ForgeDirection.UNKNOWN));
		for(int[] pos : this.connected) node.addConnection(new DirPos(pos[0], pos[1], pos[2], ForgeDirection.UNKNOWN));
		return node;
	}

	public void addConnection(int x, int y, int z) {

		connected.add(new int[] {x, y, z});

		PowerNode node = Nodespace.getNode(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());
		node.recentlyChanged = true;
		node.addConnection(new DirPos(x, y, z, ForgeDirection.UNKNOWN));

		this.markDirty();

		if(this.world instanceof WorldServer) {
			WorldServer world = (WorldServer) this.world;
			world.getPlayerManager().markBlockForUpdate(this.pos.getX(), this.pos.getY(), this.pos.getZ());
		}
	}

	public void disconnectAll() {

		for(int[] pos : connected) {

			TileEntity te = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));

			if(te == this)
				continue;

			if(te instanceof TileEntityPylonBase) {
				TileEntityPylonBase pylon = (TileEntityPylonBase) te;
				Nodespace.destroyNode(this.world, pos[0], pos[1], pos[2]);

				for(int i = 0; i < pylon.connected.size(); i++) {
					int[] conPos = pylon.connected.get(i);

					if(conPos[0] == this.pos.getX() && conPos[1] == this.pos.getY() && conPos[2] == this.pos.getZ()) {
						pylon.connected.remove(i);
						i--;
					}
				}

				pylon.markDirty();

				if(this.world instanceof WorldServer) {
					WorldServer world = (WorldServer) this.world;
					world.getPlayerManager().markBlockForUpdate(pylon.getPos().getX(), pylon.getPos().getY(), pylon.getPos().getZ());
				}
			}
		}

		Nodespace.destroyNode(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());
	}

	public abstract ConnectionType getConnectionType();
	public abstract Vec3d[] getMountPos();
	public abstract double getMaxWireLength();

	public Vec3d getConnectionPoint() {
		Vec3d[] mounts = this.getMountPos();

		if(mounts == null || mounts.length == 0)
			return new Vec3d(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5);

		return mounts[0].addVector(this.pos.getX(), this.pos.getY(), this.pos.getZ());
	}

	public List<int[]> getConnected() {
		return connected;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setInteger("conCount", connected.size());
		nbt.setInteger("color", color);

		for(int i = 0; i < connected.size(); i++) {
			nbt.setIntArray("con" + i, connected.get(i));
		}
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		int count = nbt.getInteger("conCount");
		this.color = nbt.getInteger("color");

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
		SINGLE,
		TRIPLE,
		QUAD
		//more to follow
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
}
