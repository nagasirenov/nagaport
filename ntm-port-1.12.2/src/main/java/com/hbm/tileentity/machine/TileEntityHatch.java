package com.hbm.tileentity.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.BlockSeal;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityHatch extends TileEntity implements net.minecraft.util.ITickable {

	public int x;
	public int y;
	public int z;

	@Override
	public void update() {
		
		Block b = com.hbm.world.WorldBlockState.read(this.world, x, y, z).getBlock();
		
		if(b != ModBlocks.seal_controller && !this.world.isRemote) {
			{ net.minecraft.world.World nativePlacementWorld = this.world; int nativePlacementX = this.pos.getX(); int nativePlacementY = this.pos.getY(); int nativePlacementZ = this.pos.getZ(); net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		} else {
			if(BlockSeal.getFrameSize(this.world, x, y, z) == 0 && !this.world.isRemote)
				{ net.minecraft.world.World nativePlacementWorld = this.world; int nativePlacementX = this.pos.getX(); int nativePlacementY = this.pos.getY(); int nativePlacementZ = this.pos.getZ(); net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound p_145839_1_) {
		this.x = p_145839_1_.getInteger("x1");
		this.y = p_145839_1_.getInteger("y1");
		this.z = p_145839_1_.getInteger("z1");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound p_145841_1_) {
		p_145841_1_.setInteger("x1", this.x);
		p_145841_1_.setInteger("y1", this.y);
		p_145841_1_.setInteger("z1", this.z);
		return p_145841_1_;
	}
	
	public void setControllerPos(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

}
