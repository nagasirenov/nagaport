package com.hbm.tileentity.machine;

import com.hbm.blocks.ModBlocks;

import net.minecraft.tileentity.TileEntity;

public class TileEntityChlorineSeal extends TileEntity implements net.minecraft.util.ITickable {
	
	@Override
	public void update() {
		
		if(!this.world.isRemote && this.world.isBlockPowered(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ())))
			spread(this.pos.getX(), this.pos.getY(), this.pos.getZ(), 0);
	}
	
	private void spread(int x, int y, int z, int index) {
		
		if(index > 50)
			return;
		
		if(com.hbm.world.WorldBlockState.read(this.world, x, y, z).getBlock().isReplaceable(this.world, x, y, z))
			this.world.setBlock(x, y, z, ModBlocks.chlorine_gas);
		
		if(com.hbm.world.WorldBlockState.read(this.world, x, y, z).getBlock() != ModBlocks.chlorine_gas && com.hbm.world.WorldBlockState.read(this.world, x, y, z).getBlock() != ModBlocks.vent_chlorine_seal)
			return;
		
		switch(this.world.rand.nextInt(6)) {
		case 0:
			spread(x + 1, y, z, index + 1);
			break;
		case 1:
			spread(x - 1, y, z, index + 1);
			break;
		case 2:
			spread(x, y + 1, z, index + 1);
			break;
		case 3:
			spread(x, y - 1, z, index + 1);
			break;
		case 4:
			spread(x, y, z + 1, index + 1);
			break;
		case 5:
			spread(x, y, z - 1, index + 1);
			break;
		}
	}
}
