package com.hbm.tileentity.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.CompatEnergyControl;

import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.tile.IInfoProviderEC;
import net.minecraft.nbt.NBTTagCompound;
import com.hbm.lib.ForgeDirection;

public class TileEntityMachineMiniRTG extends TileEntityLoadedBase implements IEnergyProviderMK2, IInfoProviderEC, net.minecraft.util.ITickable {

	public long power;
	boolean tact = false;
	
	@Override
	public boolean shouldRefresh(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos,
			net.minecraft.block.state.IBlockState oldState, net.minecraft.block.state.IBlockState newState) {
		// Legacy metadata-only changes retained the generator and its stored state.
		return oldState.getBlock() != newState.getBlock();
	}

	@Override
	public void update() {
		
		if(!this.world.isRemote) {

			power += this.getOutput();
			
			if(power > getMaxPower())
				power = getMaxPower();

			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				this.tryProvide(this.world, this.pos.getX() + dir.offsetX, this.pos.getY() + dir.offsetY, this.pos.getZ() + dir.offsetZ, dir);
			}
		}
	}
	
	public long getOutput() {
		if(this.getBlockType() == ModBlocks.machine_powerrtg) return 2_500;
		return 700;
	}

	@Override
	public long getMaxPower() {
		if(this.getBlockType() == ModBlocks.machine_powerrtg) return 50_000;
		return 1_400;
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public void setPower(long i) {
		power = i;
	}


	@Override
	public void provideExtraInfo(NBTTagCompound data) {
		data.setBoolean(CompatEnergyControl.B_ACTIVE, true);
		data.setDouble(CompatEnergyControl.D_OUTPUT_HE, this.getOutput());
	}
}
