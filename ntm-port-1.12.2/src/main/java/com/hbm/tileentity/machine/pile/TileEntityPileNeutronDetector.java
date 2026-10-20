package com.hbm.tileentity.machine.pile;

import api.hbm.block.IPileNeutronReceiver;
import com.hbm.blocks.machine.pile.BlockGraphiteNeutronDetector;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityPileNeutronDetector extends TileEntity implements IPileNeutronReceiver, net.minecraft.util.ITickable {
	
	public int lastNeutrons;
	public int neutrons;
	public int maxNeutrons = 10;
	
	@Override
	public void update() {
		
		if(!this.world.isRemote) {
			
			//lastNeutrons is used to reduce the responsiveness of control rods; should cut down on sound/updates whilst keeping them still useful for automatic control.
			//Even with it, the auto rods are *very* subject to triggering on and off rapidly - this is necessary, as rays in smaller piles aren't guarenteed to consistently flood all surrounding areas
			if(this.neutrons >= this.maxNeutrons && (this.getBlockMetadata() & 8) > 0)
				((BlockGraphiteNeutronDetector)com.hbm.world.WorldBlockState.read(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ()).getBlock()).triggerRods(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());
			if(this.neutrons < this.maxNeutrons && this.lastNeutrons < this.maxNeutrons && (this.getBlockMetadata() & 8) == 0)
				((BlockGraphiteNeutronDetector)com.hbm.world.WorldBlockState.read(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ()).getBlock()).triggerRods(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());
			
			this.lastNeutrons = this.neutrons;
			this.neutrons = 0;
		}
	}

	@Override
	public void receiveNeutrons(int n) {
		this.neutrons += n;
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("maxNeutrons", this.maxNeutrons);
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.maxNeutrons = nbt.getInteger("maxNeutrons");
	}
}
