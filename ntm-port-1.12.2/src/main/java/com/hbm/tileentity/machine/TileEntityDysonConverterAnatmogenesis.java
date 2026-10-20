package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.trait.FT_Gaseous;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IDysonConverter;
import com.hbm.tileentity.TileEntityMachineBase;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import com.hbm.lib.ForgeDirection;

public class TileEntityDysonConverterAnatmogenesis extends TileEntityMachineBase implements IDysonConverter {

	// what the FUCK is anatmogenesis you ask?
	// I made it the fuck up

	// from:
	// * an-		= without
	// * atmo-		= vapour, air (from atmosphere)
	// * genesis	= creation

	// similar to abiogenesis (life from non-life/nothing)
	// anatmogenesis is the creation of an atmosphere from nothing

	// this is effectively the survival version of the creative atmosphere editor,
	// turning absolutely ridiculous amounts of energy into any gas you please,
	// or remove a gas entirely, if you so desire.

	public FluidType fluid = Fluids.OXYGEN;
	public boolean isEmitting = true;

	public long gasProduced;

	public boolean isConverting;

	// 100THE/s will produce 0.1atm in 8 hours
	private static final long HE_TO_MB = 28_800_000;

	public TileEntityDysonConverterAnatmogenesis() {
		super(0);
	}

	@Override
	public String getDefaultName() {
		return "container.dysonConverterAnatmogenesis";
	}

	@Override
	public void update() {
		if(!this.world.isRemote) {
			isConverting = gasProduced > 0;

			networkPackNT(250);
			gasProduced = 0;
		} else {
			if(isConverting) {
				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "tower");
				data.setFloat("lift", 0.5F);
				data.setFloat("base", 0.8F);
				data.setFloat("max", 4F);
				data.setInteger("life", 100 + this.world.rand.nextInt(50));

				data.setInteger("color", fluid.getColor());
				data.setDouble("posX", this.pos.getX() + 0.5);
				data.setDouble("posZ", this.pos.getZ() + 0.5);
				data.setDouble("posY", this.pos.getY() + 3.25);

				data.setDouble("mX", (this.world.rand.nextDouble() - 0.5));
				data.setDouble("mY", (this.world.rand.nextDouble()) * 2);
				data.setDouble("mZ", (this.world.rand.nextDouble() - 0.5));

				MainRegistry.proxy.effectNT(data);
			}
		}
	}

	@Override
	public boolean provideEnergy(int x, int y, int z, long energy) {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		int rx = this.pos.getX() + dir.offsetX * 5;
		int ry = this.pos.getY() + 1;
		int rz = this.pos.getZ() + dir.offsetZ * 5;

		if(x != rx || y != ry || z != rz) return false;

		long volume = energy / HE_TO_MB;
		gasProduced += volume;

		if(isEmitting) {
			FT_Gaseous.release(this.world, fluid, volume);
		} else {
			FT_Gaseous.capture(this.world, fluid, volume);
		}

		return true;
	}

	@Override
	public long maximumEnergy() {
		return Long.MAX_VALUE;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeBoolean(isConverting);
		buf.writeLong(gasProduced);
		buf.writeInt(fluid.getID());
		buf.writeBoolean(isEmitting);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		isConverting = buf.readBoolean();
		gasProduced = buf.readLong();
		fluid = Fluids.fromID(buf.readInt());
		isEmitting = buf.readBoolean();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		fluid = Fluids.fromID(nbt.getInteger("fluid"));
		isEmitting = nbt.getBoolean("emit");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("fluid", fluid.getID());
		nbt.setBoolean("emit", isEmitting);
		return nbt;
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
				this.pos.getX() - 6,
				this.pos.getY(),
				this.pos.getZ() - 6,
				this.pos.getX() + 7,
				this.pos.getY() + 6,
				this.pos.getZ() + 7
			);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

}
