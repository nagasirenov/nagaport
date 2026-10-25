package com.hbm.tileentity.network;

import com.hbm.uninos.UniNodespace;

import net.minecraft.block.Block;
import net.minecraft.world.World;

public class TileEntityFluidValve extends TileEntityPipeBaseNT {

	@Override
	public boolean shouldCreateNode() {
		return this.getBlockMetadata() == 1;
	}

	public void updateState() {


		if(this.getBlockMetadata() == 0 && this.node != null) {
			UniNodespace.destroyNode(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.getType().getNetworkProvider());
			this.node = null;
		}
	}

	public boolean shouldRefresh(Block oldBlock, Block newBlock, int oldMeta, int newMeta, World world, int x, int y, int z) {
		return oldBlock != newBlock;
	}
}
