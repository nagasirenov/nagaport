package com.hbm.tileentity.network;

import java.util.List;

import com.hbm.entity.item.EntityDeliveryDrone;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.ParticleUtil;
import com.hbm.util.fauxpointtwelve.BlockPos;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import com.hbm.lib.ForgeDirection;

public class TileEntityDroneWaypoint extends TileEntityLoadedBase implements IDroneLinkable, net.minecraft.util.ITickable {

	public int height = 5;
	public int nextX = -1;
	public int nextY = -1;
	public int nextZ = -1;

	@Override
	public void update() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata());

		if(!this.world.isRemote) {
			if(nextY != -1) {
				List<EntityDeliveryDrone> drones = this.world.getEntitiesWithinAABB(EntityDeliveryDrone.class, AxisAlignedBB.getBoundingBox(this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.pos.getX() + 1, this.pos.getY() + 1, this.pos.getZ() + 1).offset(dir.offsetX * height, dir.offsetY * height, dir.offsetZ * height));
				for(EntityDeliveryDrone drone : drones) {
					if(new Vec3d(drone.motionX, drone.motionY, drone.motionZ).length() < 0.05) {
						drone.setTarget(nextX + 0.5, nextY, nextZ + 0.5);
					}
				}
			}

			networkPackNT(15);
		} else {
			BlockPos pos = getCoord(dir);
			if(nextY != -1 && this.world.getTotalWorldTime() % 2 == 0) {
				double x = this.pos.getX() + height * dir.offsetX + 0.5;
				double y = this.pos.getY() + height * dir.offsetY + 0.5;
				double z = this.pos.getZ() + height * dir.offsetZ + 0.5;

				this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.REDSTONE, x, y, z, 0, 0, 0);

				ParticleUtil.spawnDroneLine(this.world,
				pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
				(nextX  - pos.getX()), (nextY - pos.getY()), (nextZ - pos.getZ()), 0x0000ff);
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeInt(height);
		buf.writeInt(nextX);
		buf.writeInt(nextY);
		buf.writeInt(nextZ);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		height = buf.readInt();
		nextX = buf.readInt();
		nextY = buf.readInt();
		nextZ = buf.readInt();
	}

	@Override
	public BlockPos getPoint() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata());
		return new BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ()).offset(dir, height);
	}

	@Override
	public void setNextTarget(int x, int y, int z) {
		this.nextX = x;
		this.nextY = y;
		this.nextZ = z;
		this.markDirty();
	}

	public void addHeight(int h) {
		height += h;
		height = MathHelper.clamp(height, 1, 15);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.height = nbt.getInteger("height");
		int[] pos = nbt.getIntArray("pos");
		this.nextX = pos[0];
		this.nextY = pos[1];
		this.nextZ = pos[2];
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setInteger("height", height);
		nbt.setIntArray("pos", new int[] {nextX, nextY, nextZ});
		return nbt;
	}
	public BlockPos getCoord(ForgeDirection dir) {
		return new BlockPos(this.pos.getX()  + height * dir.offsetX + 0.5, this.pos.getY() + height * dir.offsetY + 0.5, this.pos.getZ() + height * dir.offsetZ + 0.5);
	}
}
