package com.hbm.tileentity.machine.pile;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;

import api.hbm.block.IPileNeutronReceiver;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityPileBreedingFuel extends TileEntityPileBase implements IPileNeutronReceiver {
	
	public int neutrons;
	public int lastNeutrons;
	public int progress;
	public static final int maxProgress = GeneralConfig.enable528 ? 50000 : 30000;
	
	@Override
	public void update() {
		if(!this.world.isRemote) {
			react();
			
			if(this.progress >= this.maxProgress) {
				this.world.setBlock(this.pos.getX(), this.pos.getY(), this.pos.getZ(), ModBlocks.block_graphite_tritium, this.getBlockMetadata(), 3);
			}
		}
	}
	
	private void react() {
		
		this.lastNeutrons = this.neutrons;
		this.progress += this.neutrons;
		
		this.neutrons = 0;
		
		if(lastNeutrons <= 0)
			return;
		
		for(int i = 0; i < 2; i++)
			this.castRay(1);
	}
	
	@Override
	public void receiveNeutrons(int n) {
		this.neutrons += n;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.progress = nbt.getInteger("progress");
		this.neutrons = nbt.getInteger("neutrons");
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("progress", this.progress);
		nbt.setInteger("neutrons", this.neutrons);
		return nbt;
	}
}
