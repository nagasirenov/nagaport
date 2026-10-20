package com.hbm.tileentity.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.inventory.fluid.trait.FT_Polluting;
import com.hbm.inventory.fluid.trait.FT_Gaseous;
import com.hbm.inventory.fluid.trait.FluidTrait.FluidReleaseType;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Amat;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Liquid;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Viscous;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.energymk2.IEnergyReceiverMK2.ConnectionPriority;
import api.hbm.fluid.IFluidStandardReceiver;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.EnumFacing;
import com.hbm.lib.ForgeDirection;

public class TileEntityMachineDrain extends TileEntityLoadedBase implements IFluidStandardReceiver, IFluidCopiable, net.minecraft.util.ITickable {

	public FluidTank tank;

	public TileEntityMachineDrain() {
		this.tank = new FluidTank(Fluids.NONE, 2_000);
	}

	@Override
	public void update() {

		if(!this.world.isRemote) {

			if(this.world.getTotalWorldTime() % 20 == 0) {
				for(DirPos pos : getConPos()) this.trySubscribe(tank.getTankType(), this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
			}

			networkPackNT(50);

			if(tank.getFill() > 0) {
				if(tank.getTankType().hasTrait(FT_Amat.class)) {
					this.world.newExplosion(null, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, 10F, true, true);
					return;
				}
				int toSpill = Math.max(tank.getFill() / 2, 1);
				tank.setFill(tank.getFill() - toSpill);

				FT_Polluting.pollute(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), tank.getTankType(), FluidReleaseType.SPILL, toSpill);
				FT_Gaseous.release(this.world, tank.getTankType(), toSpill);

				if(toSpill >= 100 && this.world.rand.nextInt(20) == 0 && tank.getTankType().hasTrait(FT_Liquid.class) && tank.getTankType().hasTrait(FT_Viscous.class) && tank.getTankType().hasTrait(FT_Flammable.class)) {
					ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
					Vec3d start = new Vec3d(this.pos.getX() + 0.5 - dir.offsetX * 3, this.pos.getY() + 0.5, this.pos.getZ() + 0.5 - dir.offsetZ * 3);
					Vec3d end = start.addVector(this.world.rand.nextGaussian() * 5, -25, this.world.rand.nextGaussian() * 5);
					RayTraceResult mop = this.world.rayTraceBlocks(start, end, false, true, false);

					if(mop != null && mop.typeOfHit == Type.BLOCK && mop.sideHit == EnumFacing.UP) {
						int x = mop.getBlockPos().getX();
						int y = mop.getBlockPos().getY() + 1;
						int z = mop.getBlockPos().getZ();
						Block block = com.hbm.world.WorldBlockState.read(this.world, x, y, z).getBlock();
						if(!block.getDefaultState().getMaterial().isLiquid() && block.isReplaceable(this.world, x, y, z) && ModBlocks.oil_spill.canPlaceBlockAt(this.world, x, y, z)) {
							this.world.setBlock(x, y, z, ModBlocks.oil_spill);
						}
					}
				}
			}

		} else {

			if(tank.getFill() > 0 && MainRegistry.proxy.me().getDistance(this.pos.getX(), this.pos.getY(), this.pos.getZ()) < 100) {
				ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);

				NBTTagCompound data = new NBTTagCompound();
				data.setFloat("lift", 0.5F);
				data.setFloat("base", 0.375F);
				data.setFloat("max", 3F);
				data.setInteger("life", 100 + this.world.rand.nextInt(50));
				
				if(tank.getTankType().hasTrait(FT_Gaseous.class)) {
					data.setString("type", "tower");
				} else {
					data.setString("type", "splash");
				}

				data.setInteger("color", tank.getTankType().getColor());
				data.setDouble("posX", this.pos.getX() + 0.5 - dir.offsetX * 2.5);
				data.setDouble("posZ", this.pos.getZ() + 0.5 - dir.offsetZ * 2.5);
				data.setDouble("posY", this.pos.getY() + 0.5);

				MainRegistry.proxy.effectNT(data);
			}
		}
	}

	public DirPos[] getConPos() {
		ForgeDirection dir0 = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
		ForgeDirection dir1 = dir0.getRotation(ForgeDirection.UP);
		ForgeDirection dir2 = dir0.getRotation(ForgeDirection.DOWN);
		return new DirPos[] {
				new DirPos(this.pos.getX() + dir0.offsetX, this.pos.getY(), this.pos.getZ() + dir0.offsetZ, dir0),
				new DirPos(this.pos.getX() + dir1.offsetX, this.pos.getY(), this.pos.getZ() + dir1.offsetZ, dir1),
				new DirPos(this.pos.getX() + dir2.offsetX, this.pos.getY(), this.pos.getZ() + dir2.offsetZ, dir2)
		};
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.tank.readFromNBT(nbt, "t");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		this.tank.writeToNBT(nbt, "t");
		return nbt;
	}

	@Override public void serialize(ByteBuf buf) { tank.serialize(buf); }
	@Override public void deserialize(ByteBuf buf) { tank.deserialize(buf); }

	@Override public FluidTank[] getAllTanks() { return new FluidTank[] {tank}; }
	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] {tank}; }
	@Override public ConnectionPriority getFluidPriority() { return ConnectionPriority.LOW; }

	@Override
	public boolean canConnect(FluidType type, ForgeDirection dir) {
		return dir != ForgeDirection.UP && dir != ForgeDirection.DOWN;
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
					this.pos.getX() - 2,
					this.pos.getY(),
					this.pos.getZ() - 2,
					this.pos.getX() + 3,
					this.pos.getY() + 1,
					this.pos.getZ() + 3
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
	public FluidTank getTankToPaste() {
		return tank;
	}
}
