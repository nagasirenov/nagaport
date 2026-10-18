package com.hbm.explosion.vanillant.standard;

import java.util.HashSet;
import java.util.Iterator;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.interfaces.IBlockMutator;
import com.hbm.explosion.vanillant.interfaces.IBlockProcessor;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockProcessorNoDamage implements IBlockProcessor {
	
	protected IBlockMutator convert;
	
	public BlockProcessorNoDamage() { }
	
	public BlockProcessorNoDamage withBlockEffect(IBlockMutator convert) {
		this.convert = convert;
		return this;
	}

	@Override
	public void process(ExplosionVNT explosion, World world, double x, double y, double z, HashSet<BlockPos> affectedBlocks) {

		Iterator iterator = affectedBlocks.iterator();
		
		while(iterator.hasNext()) {
			BlockPos chunkposition = (BlockPos) iterator.next();
			int blockX = chunkposition.getX();
			int blockY = chunkposition.getY();
			int blockZ = chunkposition.getZ();
			Block block = world.getBlock(blockX, blockY, blockZ);
			
			if(block.getMaterial() != Material.air) {
				if(this.convert != null) this.convert.mutatePre(explosion, block, world.getBlockMetadata(blockX, blockY, blockZ), blockX, blockY, blockZ);
			}
		}
		
		
		if(this.convert != null) {
			iterator = affectedBlocks.iterator();
			
			while(iterator.hasNext()) {
				BlockPos chunkposition = (BlockPos) iterator.next();
				int blockX = chunkposition.getX();
				int blockY = chunkposition.getY();
				int blockZ = chunkposition.getZ();
				Block block = world.getBlock(blockX, blockY, blockZ);
				
				if(block.getMaterial() == Material.air) {
					this.convert.mutatePost(explosion, blockX, blockY, blockZ);
				}
			}
		}
		
		affectedBlocks.clear(); //tricks the standard SFX to not do the block damage particles
	}
}
