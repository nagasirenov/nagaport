package com.hbm.blocks.bomb;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.explosion.ExplosionChaos;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class CheaterVirus extends Block {

	static boolean protect = true;
	
	public CheaterVirus(Material p_i45394_1_) {
		super(p_i45394_1_);
		this.setTickRandomly(true);
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int i)
	{
		super.breakBlock(world, x, y, z, block, i);
		
		if(CheaterVirus.protect)
			world.setBlock(x, y, z, this, i, 2);
	}

    @Override
	public void updateTick(World world, int x, int y, int z, Random rand) {
    	
    	if(GeneralConfig.enableVirus){
    		if(com.hbm.world.WorldBlockState.read(world, x + 1, y, z).getBlock() != ModBlocks.cheater_virus && com.hbm.world.WorldBlockState.read(world, x + 1, y, z).getBlock() != Blocks.AIR && com.hbm.world.WorldBlockState.read(world, x + 1, y, z).getBlock() != ModBlocks.cheater_virus) {
    			world.setBlock(x + 1, y, z, ModBlocks.cheater_virus);
    		}
    	
    		if(com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() != ModBlocks.cheater_virus && com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() != Blocks.AIR && com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() != ModBlocks.cheater_virus) {
    			world.setBlock(x, y + 1, z, ModBlocks.cheater_virus);
    		}
    	
    		if(com.hbm.world.WorldBlockState.read(world, x, y, z + 1).getBlock() != ModBlocks.cheater_virus && com.hbm.world.WorldBlockState.read(world, x, y, z + 1).getBlock() != Blocks.AIR && com.hbm.world.WorldBlockState.read(world, x, y, z + 1).getBlock() != ModBlocks.cheater_virus) {
    			world.setBlock(x, y, z + 1, ModBlocks.cheater_virus);
    		}
    	
    		if(com.hbm.world.WorldBlockState.read(world, x - 1, y, z).getBlock() != ModBlocks.cheater_virus && com.hbm.world.WorldBlockState.read(world, x - 1, y, z).getBlock() != Blocks.AIR && com.hbm.world.WorldBlockState.read(world, x - 1, y, z).getBlock() != ModBlocks.cheater_virus) {
    			world.setBlock(x - 1, y, z, ModBlocks.cheater_virus);
    		}
    	
    		if(com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() != ModBlocks.cheater_virus && com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() != Blocks.AIR && com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() != ModBlocks.cheater_virus) {
    			world.setBlock(x, y - 1, z, ModBlocks.cheater_virus);
    		}
    	
    		if(com.hbm.world.WorldBlockState.read(world, x, y, z - 1).getBlock() != ModBlocks.cheater_virus && com.hbm.world.WorldBlockState.read(world, x, y, z - 1).getBlock() != Blocks.AIR && com.hbm.world.WorldBlockState.read(world, x, y, z - 1).getBlock() != ModBlocks.cheater_virus) {
    			world.setBlock(x, y, z - 1, ModBlocks.cheater_virus);
    		}
    		
    		protect = false;
    		{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
    		protect = true;
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
			protect = false;
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			ExplosionChaos.spreadVirus(world, x, y, z, 5);
			protect = true;
    	}
    }

    @Override
	public void onEntityWalking(World p_149724_1_, int p_149724_2_, int p_149724_3_, int p_149724_4_, Entity entity)
    {
    	if(entity instanceof EntityLivingBase) {
    		((EntityLivingBase)entity).addPotionEffect(new PotionEffect(Potion.wither.id, 60 * 60 * 60, 9));
    	}
    }

}
