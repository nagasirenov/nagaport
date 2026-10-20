package com.hbm.blocks.fluid;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.trait.CBT_Temperature;
import com.hbm.items.ModItems;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class MeltedFlesh extends Block {

	public MeltedFlesh(Material mat) {
		super(mat);
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
		this.setTickRandomly(true);
	}

	public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	public boolean isFullCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	public Item getItemDropped(net.minecraft.block.state.IBlockState p_149650_1_, Random p_149650_2_, int p_149650_3_) {
		if(this == ModBlocks.flesh_block) {
			return ModItems.flesh;	
		} else if(this == ModBlocks.charred_flesh_block) {
			return ModItems.grilled_flesh;	
		}

		return ModItems.powder_coal_tiny;
	}

	public boolean canPlaceBlockAt(World world, int x, int y, int z) {
		Block block = com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock();
		return block != Blocks.ICE && block != Blocks.PACKED_ICE ? (block.isLeaves(world, x, y - 1, z) ? true : (block == this && (com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y - 1, z) ) & 7) == 7 ? true : block.isOpaqueCube() && block.getDefaultState().getMaterial().blocksMovement())) : false;
	}

	public void onNeighborBlockChange(World world, int x, int y, int z, Block b) {
		this.func_150155_m(world, x, y, z);
	}

	private boolean func_150155_m(World world, int x, int y, int z) {
		if(!this.canPlaceBlockAt(world, x, y, z)) {
			com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
			return false;
		} else {
			return true;
		}
	}

	public boolean isReplaceable(IBlockAccess world, int x, int y, int z) {
		return true;
	}
	
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		super.randomDisplayTick(world, x, y, z, rand);

		CBT_Temperature temperature = CelestialBody.getTrait(world, CBT_Temperature.class);
		if(temperature != null && temperature.degrees > 100) {
			float f;
			float f1;
			float f2;
			f = (float)x + rand.nextFloat();
			f1 = (float)y + rand.nextFloat() * 0.5F + 0.5F;
			f2 = (float)z + rand.nextFloat();

			if(this == ModBlocks.flesh_block) {
				world.spawnParticle("smoke", (double)f, (double)f1, (double)f2, 0.0D, 0.0D, 0.0D);
				world.spawnParticle("cloud", (double)f, (double)f1, (double)f2, 0.0D, 0.0D, 0.0D);
			} else if(this == ModBlocks.charred_flesh_block) {
				world.spawnParticle("largesmoke", (double)f, (double)f1, (double)f2, 0.0D, 0.0D, 0.0D);	        	
			}
		}		
	}
	
	public void updateTick(World world, net.minecraft.util.math.BlockPos nativeTickPos, net.minecraft.block.state.IBlockState nativeTickState, Random rand) {
		int x = nativeTickPos.getX(), y = nativeTickPos.getY(), z = nativeTickPos.getZ();
		CBT_Temperature temperature = CelestialBody.getTrait(world, CBT_Temperature.class);
		if(temperature != null && temperature.degrees > 100) {
			if(this == ModBlocks.flesh_block) {
				world.setBlock(x, y, z, ModBlocks.charred_flesh_block);    	
			} else if(this == ModBlocks.charred_flesh_block) {
				world.setBlock(x, y, z, ModBlocks.carbonized_flesh_block);    	
			}  
		}
	}

}
