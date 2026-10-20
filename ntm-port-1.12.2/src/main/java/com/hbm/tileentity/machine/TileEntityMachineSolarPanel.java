package com.hbm.tileentity.machine;

import com.hbm.dim.CelestialBody;
import com.hbm.dim.orbit.WorldProviderOrbit;
import com.hbm.tileentity.TileEntityLoadedBase;

import api.hbm.energymk2.IEnergyProviderMK2;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import com.hbm.lib.ForgeDirection;

public class TileEntityMachineSolarPanel extends TileEntityLoadedBase implements IEnergyProviderMK2, net.minecraft.util.ITickable {

	private long power;
	private long maxpwr = 1_000;

	@Override
	public void update() {
		
		if(!this.world.isRemote) {
			
			// Sun power ranges from 1-4
			int sun = this.world.getSavedLightValue(EnumSkyBlock.Sky, this.pos.getX(), this.pos.getY(), this.pos.getZ()) - this.world.skylightSubtracted - 11;

			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				tryProvide(this.world, this.pos.getX() + dir.offsetX, this.pos.getY() + dir.offsetY, this.pos.getZ() + dir.offsetZ, dir);
			}
			
			if(sun <= 0 || !this.world.canBlockSeeTheSky(this.pos.getX(), this.pos.getY() + 1, this.pos.getZ())) {
				return;
			}
			
			power += getOutput(sun);
			
			if(power > getMaxPower())
				power = getMaxPower();
		}
	}

	// Balanced around 100he/t on Earth
	public long getOutput(int sun) {
		float sunPower = this.world.provider instanceof WorldProviderOrbit
			? ((WorldProviderOrbit)this.world.provider).getSunPower()
			: CelestialBody.getBody(this.world).getSunPower();
		return MathHelper.ceil(sun * 25 * sunPower);
	}

	@Override
	public long getPower() {
		return power;
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}

	@Override
	public void setPower(long power) {
		this.power = power;		
	}

	@Override
	public long getMaxPower() {
		return maxpwr; //temp
	}
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.power = nbt.getLong("power");
		this.maxpwr = nbt.getLong("maxpwr");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		
		nbt.setLong("power", power);
		nbt.setLong("maxpwr", maxpwr);
		return nbt;
	}
}
