package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.util.fauxpointtwelve.DirPos;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class TileEntityDeuteriumTower extends TileEntityDeuteriumExtractor {

	public TileEntityDeuteriumTower() {
		super();
		tanks[0] = new FluidTank(Fluids.WATER, 50000);
		tanks[1] = new FluidTank(Fluids.HEAVYWATER, 5000);
	}

	@Override
	protected void updateConnections() {

		for(DirPos pos : getConPos()) {
			this.trySubscribe(this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
		}
	}

	@Override
	public void subscribeToAllAround(FluidType type, World world, int x, int y, int z) {

		for(DirPos pos : getConPos()) {
			this.trySubscribe(type, world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
		}
	}

	@Override
	public void sendFluidToAll(FluidTank tank, TileEntity te) {

		for(DirPos pos : getConPos()) {
			this.sendFluid(tank, this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
		}
	}

	private DirPos[] getConPos() {

		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		ForgeDirection rot = dir.getRotation(ForgeDirection.DOWN);

		return new DirPos[] {
				new DirPos(this.pos.getX() - dir.offsetX * 2, this.pos.getY(), this.pos.getZ() - dir.offsetZ * 2, dir.getOpposite()),
				new DirPos(this.pos.getX() - dir.offsetX * 2 + rot.offsetX, this.pos.getY(), this.pos.getZ() - dir.offsetZ * 2 + rot.offsetZ, dir.getOpposite()),

				new DirPos(this.pos.getX() + dir.offsetX, this.pos.getY(), this.pos.getZ() + dir.offsetZ, dir),
				new DirPos(this.pos.getX() + dir.offsetX + rot.offsetX, this.pos.getY(), this.pos.getZ() + dir.offsetZ  + rot.offsetZ, dir),

				new DirPos(this.pos.getX() - rot.offsetX, this.pos.getY(), this.pos.getZ() - rot.offsetZ, rot.getOpposite()),
				new DirPos(this.pos.getX() - dir.offsetX - rot.offsetX, this.pos.getY(), this.pos.getZ() - dir.offsetZ - rot.offsetZ, rot.getOpposite()),

				new DirPos(this.pos.getX() + rot.offsetX * 2, this.pos.getY(), this.pos.getZ() + rot.offsetZ * 2, rot),
				new DirPos(this.pos.getX() - dir.offsetX + rot.offsetX * 2, this.pos.getY(), this.pos.getZ() - dir.offsetZ + rot.offsetZ * 2, rot),
		};
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
					this.pos.getY() + 10,
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

	@Override
	public long getMaxPower() {
		return 100_000;
	}
}
