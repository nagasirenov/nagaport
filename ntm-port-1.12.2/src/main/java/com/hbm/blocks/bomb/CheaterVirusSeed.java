package com.hbm.blocks.bomb;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class CheaterVirusSeed extends Block {

	public CheaterVirusSeed(Material p_i45394_1_) {
		
		super(p_i45394_1_);
		this.setTickRandomly(true);
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int i) {
		
		super.breakBlock(world, x, y, z, block, i);
		
		if(!GeneralConfig.enableVirus)
			return;

    	if((com.hbm.world.WorldBlockState.read(world, x + 1, y, z).getBlock() == Blocks.AIR || com.hbm.world.WorldBlockState.read(world, x + 1, y, z).getBlock() == ModBlocks.cheater_virus || com.hbm.world.WorldBlockState.read(world, x + 1, y, z).getBlock() == ModBlocks.cheater_virus_seed) && 
    			(com.hbm.world.WorldBlockState.read(world, x - 1, y, z).getBlock() == Blocks.AIR || com.hbm.world.WorldBlockState.read(world, x - 1, y, z).getBlock() == ModBlocks.cheater_virus || com.hbm.world.WorldBlockState.read(world, x - 1, y, z).getBlock() == ModBlocks.cheater_virus_seed) && 
    			(com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() == Blocks.AIR || com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() == ModBlocks.cheater_virus || com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() == ModBlocks.cheater_virus_seed) && 
    			(com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() == Blocks.AIR || com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() == ModBlocks.cheater_virus || com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() == ModBlocks.cheater_virus_seed) && 
    			(com.hbm.world.WorldBlockState.read(world, x, y, z + 1).getBlock() == Blocks.AIR || com.hbm.world.WorldBlockState.read(world, x, y, z + 1).getBlock() == ModBlocks.cheater_virus || com.hbm.world.WorldBlockState.read(world, x, y, z + 1).getBlock() == ModBlocks.cheater_virus_seed) && 
    			(com.hbm.world.WorldBlockState.read(world, x, y, z - 1).getBlock() == Blocks.AIR || com.hbm.world.WorldBlockState.read(world, x, y, z - 1).getBlock() == ModBlocks.cheater_virus || com.hbm.world.WorldBlockState.read(world, x, y, z - 1).getBlock() == ModBlocks.cheater_virus_seed) && 
    			!world.isRemote) {
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
    	} else {
    		world.setBlock(x, y, z, ModBlocks.cheater_virus);
    	}
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {
		
		if(!GeneralConfig.enableVirus)
			return;
    	
    	if((com.hbm.world.WorldBlockState.read(world, x + 1, y, z).getBlock() == Blocks.AIR || com.hbm.world.WorldBlockState.read(world, x + 1, y, z).getBlock() == ModBlocks.cheater_virus || com.hbm.world.WorldBlockState.read(world, x + 1, y, z).getBlock() == ModBlocks.cheater_virus_seed) && 
    			(com.hbm.world.WorldBlockState.read(world, x - 1, y, z).getBlock() == Blocks.AIR || com.hbm.world.WorldBlockState.read(world, x - 1, y, z).getBlock() == ModBlocks.cheater_virus || com.hbm.world.WorldBlockState.read(world, x - 1, y, z).getBlock() == ModBlocks.cheater_virus_seed) && 
    			(com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() == Blocks.AIR || com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() == ModBlocks.cheater_virus || com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() == ModBlocks.cheater_virus_seed) && 
    			(com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() == Blocks.AIR || com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() == ModBlocks.cheater_virus || com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() == ModBlocks.cheater_virus_seed) && 
    			(com.hbm.world.WorldBlockState.read(world, x, y, z + 1).getBlock() == Blocks.AIR || com.hbm.world.WorldBlockState.read(world, x, y, z + 1).getBlock() == ModBlocks.cheater_virus || com.hbm.world.WorldBlockState.read(world, x, y, z + 1).getBlock() == ModBlocks.cheater_virus_seed) && 
    			(com.hbm.world.WorldBlockState.read(world, x, y, z - 1).getBlock() == Blocks.AIR || com.hbm.world.WorldBlockState.read(world, x, y, z - 1).getBlock() == ModBlocks.cheater_virus || com.hbm.world.WorldBlockState.read(world, x, y, z - 1).getBlock() == ModBlocks.cheater_virus_seed) && 
    			!world.isRemote) {
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
    	} else {
    		world.setBlock(x, y, z, ModBlocks.cheater_virus);
    	}
	}
    
    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
    	
    	if((com.hbm.world.WorldBlockState.read(world, x + 1, y, z).getBlock() == Blocks.AIR || com.hbm.world.WorldBlockState.read(world, x + 1, y, z).getBlock() == ModBlocks.cheater_virus || com.hbm.world.WorldBlockState.read(world, x + 1, y, z).getBlock() == ModBlocks.cheater_virus_seed) && 
    			(com.hbm.world.WorldBlockState.read(world, x - 1, y, z).getBlock() == Blocks.AIR || com.hbm.world.WorldBlockState.read(world, x - 1, y, z).getBlock() == ModBlocks.cheater_virus || com.hbm.world.WorldBlockState.read(world, x - 1, y, z).getBlock() == ModBlocks.cheater_virus_seed) && 
    			(com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() == Blocks.AIR || com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() == ModBlocks.cheater_virus || com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() == ModBlocks.cheater_virus_seed) && 
    			(com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() == Blocks.AIR || com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() == ModBlocks.cheater_virus || com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() == ModBlocks.cheater_virus_seed) && 
    			(com.hbm.world.WorldBlockState.read(world, x, y, z + 1).getBlock() == Blocks.AIR || com.hbm.world.WorldBlockState.read(world, x, y, z + 1).getBlock() == ModBlocks.cheater_virus || com.hbm.world.WorldBlockState.read(world, x, y, z + 1).getBlock() == ModBlocks.cheater_virus_seed) && 
    			(com.hbm.world.WorldBlockState.read(world, x, y, z - 1).getBlock() == Blocks.AIR || com.hbm.world.WorldBlockState.read(world, x, y, z - 1).getBlock() == ModBlocks.cheater_virus || com.hbm.world.WorldBlockState.read(world, x, y, z - 1).getBlock() == ModBlocks.cheater_virus_seed) && 
    			!world.isRemote) {
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
    	}
    }
}
