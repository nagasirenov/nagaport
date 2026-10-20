package com.hbm.blocks.bomb;

import java.util.Random;

import org.apache.logging.log4j.Level;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class CrystalVirus extends Block {
	
	public CrystalVirus(Material p_i45394_1_) {
		super(p_i45394_1_);
		this.setTickRandomly(true);
	}

    @Override
	public void updateTick(World world, net.minecraft.util.math.BlockPos nativeTickPos, net.minecraft.block.state.IBlockState nativeTickState, Random rand) {
		int x = nativeTickPos.getX(), y = nativeTickPos.getY(), z = nativeTickPos.getZ();
    	
    	if(GeneralConfig.enableVirus) {
    		if(com.hbm.world.WorldBlockState.read(world, x + 1, y, z).getBlock() != ModBlocks.crystal_virus && com.hbm.world.WorldBlockState.read(world, x + 1, y, z).getBlock() != Blocks.AIR && com.hbm.world.WorldBlockState.read(world, x + 1, y, z).getBlock() != ModBlocks.crystal_hardened && com.hbm.world.WorldBlockState.read(world, x + 1, y, z).getBlock() != ModBlocks.crystal_pulsar) {
    			world.setBlock(x + 1, y, z, ModBlocks.crystal_virus);
    		}
    	
    		if(com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() != ModBlocks.crystal_virus && com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() != Blocks.AIR && com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() != ModBlocks.crystal_hardened && com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() != ModBlocks.crystal_pulsar) {
    			world.setBlock(x, y + 1, z, ModBlocks.crystal_virus);
    		}
    	
    		if(com.hbm.world.WorldBlockState.read(world, x, y, z + 1).getBlock() != ModBlocks.crystal_virus && com.hbm.world.WorldBlockState.read(world, x, y, z + 1).getBlock() != Blocks.AIR && com.hbm.world.WorldBlockState.read(world, x, y, z + 1).getBlock() != ModBlocks.crystal_hardened && com.hbm.world.WorldBlockState.read(world, x, y, z + 1).getBlock() != ModBlocks.crystal_pulsar) {
    			world.setBlock(x, y, z + 1, ModBlocks.crystal_virus);
    		}
    	
    		if(com.hbm.world.WorldBlockState.read(world, x - 1, y, z).getBlock() != ModBlocks.crystal_virus && com.hbm.world.WorldBlockState.read(world, x - 1, y, z).getBlock() != Blocks.AIR && com.hbm.world.WorldBlockState.read(world, x - 1, y, z).getBlock() != ModBlocks.crystal_hardened && com.hbm.world.WorldBlockState.read(world, x - 1, y, z).getBlock() != ModBlocks.crystal_pulsar) {
    			world.setBlock(x - 1, y, z, ModBlocks.crystal_virus);
    		}
    	
    		if(com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() != ModBlocks.crystal_virus && com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() != Blocks.AIR && com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() != ModBlocks.crystal_hardened && com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() != ModBlocks.crystal_pulsar) {
    			world.setBlock(x, y - 1, z, ModBlocks.crystal_virus);
    		}
    	
    		if(com.hbm.world.WorldBlockState.read(world, x, y, z - 1).getBlock() != ModBlocks.crystal_virus && com.hbm.world.WorldBlockState.read(world, x, y, z - 1).getBlock() != Blocks.AIR && com.hbm.world.WorldBlockState.read(world, x, y, z - 1).getBlock() != ModBlocks.crystal_hardened && com.hbm.world.WorldBlockState.read(world, x, y, z - 1).getBlock() != ModBlocks.crystal_pulsar) {
    			world.setBlock(x, y, z - 1, ModBlocks.crystal_virus);
    		}
			{ net.minecraft.world.World nativeRegisteredWriteWorld = world; int nativeRegisteredWriteX = x; int nativeRegisteredWriteY = y; int nativeRegisteredWriteZ = z; net.minecraft.block.Block nativeRegisteredWriteBlock = ModBlocks.crystal_hardened; int nativeRegisteredWriteMeta = 0; int nativeRegisteredWriteFlags = 3; if (nativeRegisteredWriteWorld == null) throw new NullPointerException(); if (nativeRegisteredWriteX >= -30000000 && nativeRegisteredWriteX < 30000000 && nativeRegisteredWriteZ >= -30000000 && nativeRegisteredWriteZ < 30000000 && nativeRegisteredWriteY >= 0 && nativeRegisteredWriteY < 256) com.hbm.world.WorldBlockState.write(nativeRegisteredWriteWorld, nativeRegisteredWriteX, nativeRegisteredWriteY, nativeRegisteredWriteZ, nativeRegisteredWriteBlock.getStateFromMeta(nativeRegisteredWriteMeta), nativeRegisteredWriteFlags); }
    	}
    	
    }
    
    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
    	
    	if((com.hbm.world.WorldBlockState.read(world, x + 1, y, z).getBlock() == Blocks.AIR || com.hbm.world.WorldBlockState.read(world, x + 1, y, z).getBlock() == ModBlocks.crystal_virus || com.hbm.world.WorldBlockState.read(world, x + 1, y, z).getBlock() == ModBlocks.crystal_hardened || com.hbm.world.WorldBlockState.read(world, x + 1, y, z).getBlock() == ModBlocks.crystal_pulsar) && 
    			(com.hbm.world.WorldBlockState.read(world, x - 1, y, z).getBlock() == Blocks.AIR || com.hbm.world.WorldBlockState.read(world, x - 1, y, z).getBlock() == ModBlocks.crystal_virus || com.hbm.world.WorldBlockState.read(world, x - 1, y, z).getBlock() == ModBlocks.crystal_hardened || com.hbm.world.WorldBlockState.read(world, x - 1, y, z).getBlock() == ModBlocks.crystal_pulsar) && 
    			(com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() == Blocks.AIR || com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() == ModBlocks.crystal_virus || com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() == ModBlocks.crystal_hardened || com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() == ModBlocks.crystal_pulsar) && 
    			(com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() == Blocks.AIR || com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() == ModBlocks.crystal_virus || com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() == ModBlocks.crystal_hardened || com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() == ModBlocks.crystal_pulsar) && 
    			(com.hbm.world.WorldBlockState.read(world, x, y, z + 1).getBlock() == Blocks.AIR || com.hbm.world.WorldBlockState.read(world, x, y, z + 1).getBlock() == ModBlocks.crystal_virus || com.hbm.world.WorldBlockState.read(world, x, y, z + 1).getBlock() == ModBlocks.crystal_hardened || com.hbm.world.WorldBlockState.read(world, x, y, z + 1).getBlock() == ModBlocks.crystal_pulsar) && 
    			(com.hbm.world.WorldBlockState.read(world, x, y, z - 1).getBlock() == Blocks.AIR || com.hbm.world.WorldBlockState.read(world, x, y, z - 1).getBlock() == ModBlocks.crystal_virus || com.hbm.world.WorldBlockState.read(world, x, y, z - 1).getBlock() == ModBlocks.crystal_hardened || com.hbm.world.WorldBlockState.read(world, x, y, z - 1).getBlock() == ModBlocks.crystal_pulsar) && 
    			!world.isRemote) {
			{ net.minecraft.world.World nativeRegisteredWriteWorld = world; int nativeRegisteredWriteX = x; int nativeRegisteredWriteY = y; int nativeRegisteredWriteZ = z; net.minecraft.block.Block nativeRegisteredWriteBlock = ModBlocks.crystal_hardened; int nativeRegisteredWriteMeta = 0; int nativeRegisteredWriteFlags = 3; if (nativeRegisteredWriteWorld == null) throw new NullPointerException(); if (nativeRegisteredWriteX >= -30000000 && nativeRegisteredWriteX < 30000000 && nativeRegisteredWriteZ >= -30000000 && nativeRegisteredWriteZ < 30000000 && nativeRegisteredWriteY >= 0 && nativeRegisteredWriteY < 256) com.hbm.world.WorldBlockState.write(nativeRegisteredWriteWorld, nativeRegisteredWriteX, nativeRegisteredWriteY, nativeRegisteredWriteZ, nativeRegisteredWriteBlock.getStateFromMeta(nativeRegisteredWriteMeta), nativeRegisteredWriteFlags); }
    	}
    }

}
