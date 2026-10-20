package com.hbm.blocks.bomb;

import com.hbm.blocks.ModBlocks;
import com.hbm.explosion.ExplosionChaos;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

public class CrystalPulsar extends Block {

	public CrystalPulsar(Material p_i45394_1_) {
		super(p_i45394_1_);
	}
    
    @Override
    public void neighborChanged(net.minecraft.block.state.IBlockState nativeState, World world, net.minecraft.util.math.BlockPos nativePos, Block block, net.minecraft.util.math.BlockPos fromPos) {
		int x = nativePos.getX(); int y = nativePos.getY(); int z = nativePos.getZ();
    	
    	if(com.hbm.world.WorldBlockState.read(world, x + 1, y, z).getBlock() == ModBlocks.crystal_virus || 
    			com.hbm.world.WorldBlockState.read(world, x - 1, y, z).getBlock() == ModBlocks.crystal_virus || 
    			com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() == ModBlocks.crystal_virus || 
    			com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() == ModBlocks.crystal_virus || 
    			com.hbm.world.WorldBlockState.read(world, x, y, z + 1).getBlock() == ModBlocks.crystal_virus || 
    			com.hbm.world.WorldBlockState.read(world, x, y, z - 1).getBlock() == ModBlocks.crystal_virus || 
    			!world.isRemote) {
			ExplosionChaos.hardenVirus(world, x, y, z, 10);
    	}
    }

}
