package com.hbm.tileentity.machine.oil;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.CrackingRecipes;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.Tuple.Pair;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.fluid.IFluidStandardTransceiver;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import com.hbm.lib.ForgeDirection;

public class TileEntityMachineCatalyticCracker extends TileEntityLoadedBase implements IBufPacketReceiver, IFluidStandardTransceiver, IFluidCopiable, net.minecraft.util.ITickable {

	public FluidTank[] tanks;

	public TileEntityMachineCatalyticCracker() {
		tanks = new FluidTank[5];
		tanks[0] = new FluidTank(Fluids.BITUMEN, 4000);
		tanks[1] = new FluidTank(Fluids.STEAM, 8000);
		tanks[2] = new FluidTank(Fluids.OIL, 4000);
		tanks[3] = new FluidTank(Fluids.PETROLEUM, 4000);
		tanks[4] = new FluidTank(Fluids.SPENTSTEAM, 800);
	}

	@Override
	public void update() {

		if(!this.world.isRemote) {

			this.world.profiler.startSection("catalyticCracker_setup_tanks");
			setupTanks();
			this.world.profiler.endStartSection("catalyticCracker_update_connections");
			updateConnections();

			this.world.profiler.endStartSection("catalyticCracker_do_recipe");
			if(this.world.getTotalWorldTime() % 5 == 0)
				crack();

			this.world.profiler.endStartSection("catalyticCracker_send_fluid");
			if(this.world.getTotalWorldTime() % 10 == 0) {

				for(DirPos pos : getConPos()) {
					for(int i = 2; i <= 4; i++) {
						if(tanks[i].getFill() > 0) this.sendFluid(tanks[i], this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
					}
				}

			}
			this.world.profiler.endSection();
			networkPackNT(25);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		for(FluidTank tank : tanks)
			tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		for(FluidTank tank : tanks)
			tank.deserialize(buf);
	}

	private void updateConnections() {

		for(DirPos pos : getConPos()) {
			this.trySubscribe(tanks[0].getTankType(), this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
			this.trySubscribe(tanks[1].getTankType(), this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
		}
	}

	private void crack() {

		Pair<FluidStack, FluidStack> quart = CrackingRecipes.getCracking(tanks[0].getTankType());

		if(quart != null) {

			int left = quart.getKey().fill;
			int right = quart.getValue().fill;

			for(int i = 0; i < 2; i++) {
				if(tanks[0].getFill() >= 100 && tanks[1].getFill() >= 200 && hasSpace(left, right)) {
					tanks[0].setFill(tanks[0].getFill() - 100);
					tanks[1].setFill(tanks[1].getFill() - 200);
					tanks[2].setFill(tanks[2].getFill() + left);
					tanks[3].setFill(tanks[3].getFill() + right);
					tanks[4].setFill(tanks[4].getFill() + 2); //LPS has the density of WATER not STEAM (1%!)
				}
			}
		}
	}

	private boolean hasSpace(int left, int right) {
		return tanks[2].getFill() + left <= tanks[2].getMaxFill() && tanks[3].getFill() + right <= tanks[3].getMaxFill() && tanks[4].getFill() + 2 <= tanks[4].getMaxFill();
	}

	private void setupTanks() {

		Pair<FluidStack, FluidStack> quart = CrackingRecipes.getCracking(tanks[0].getTankType());

		if(quart != null) {
			tanks[1].setTankType(Fluids.STEAM);
			tanks[2].setTankType(quart.getKey().type);
			tanks[3].setTankType(quart.getValue().type);
			tanks[4].setTankType(Fluids.SPENTSTEAM);
		} else {
			tanks[2].setTankType(Fluids.NONE);
			tanks[3].setTankType(Fluids.NONE);
			tanks[4].setTankType(Fluids.NONE);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		for(int i = 0; i < 5; i++)
			tanks[i].readFromNBT(nbt, "tank" + i);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		for(int i = 0; i < 5; i++)
			tanks[i].writeToNBT(nbt, "tank" + i);
		return nbt;
	}

	protected DirPos[] getConPos() {

		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		return new DirPos[] {
				new DirPos(this.pos.getX() + dir.offsetX * 4 + rot.offsetX * 1, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 4 + rot.offsetZ * 1, dir),
				new DirPos(this.pos.getX() + dir.offsetX * 4 - rot.offsetX * 2, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 4 - rot.offsetZ * 2, dir),
				new DirPos(this.pos.getX() - dir.offsetX * 4 + rot.offsetX * 1, this.pos.getY(), this.pos.getZ() - dir.offsetZ * 4 + rot.offsetZ * 1, dir.getOpposite()),
				new DirPos(this.pos.getX() - dir.offsetX * 4 - rot.offsetX * 2, this.pos.getY(), this.pos.getZ() - dir.offsetZ * 4 - rot.offsetZ * 2, dir.getOpposite()),
				new DirPos(this.pos.getX() + dir.offsetX * 2 + rot.offsetX * 3, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 2 + rot.offsetZ * 3, rot),
				new DirPos(this.pos.getX() + dir.offsetX * 2 - rot.offsetX * 4, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 2 - rot.offsetZ * 4, rot),
				new DirPos(this.pos.getX() - dir.offsetX * 2 + rot.offsetX * 3, this.pos.getY(), this.pos.getZ() - dir.offsetZ * 2 + rot.offsetZ * 3, rot.getOpposite()),
				new DirPos(this.pos.getX() - dir.offsetX * 2 - rot.offsetX * 4, this.pos.getY(), this.pos.getZ() - dir.offsetZ * 2 - rot.offsetZ * 4, rot.getOpposite())
		};
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
					this.pos.getX() - 3,
					this.pos.getY(),
					this.pos.getZ() - 3,
					this.pos.getX() + 4,
					this.pos.getY() + 16,
					this.pos.getZ() + 4
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
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] {tanks[2], tanks[3], tanks[4]};
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] {tanks[0], tanks[1]};
	}

	@Override
	public FluidTank[] getAllTanks() {
		return tanks;
	}

	@Override
	public FluidTank getTankToPaste() {
		return tanks[0];
	}
}
