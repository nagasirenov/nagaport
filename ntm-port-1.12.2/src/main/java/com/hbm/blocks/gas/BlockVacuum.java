package com.hbm.blocks.gas;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.ModDamageSource;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class BlockVacuum extends Block {

	public BlockVacuum() {
		super(Material.AIR);
		this.setHardness(0.0F);
		this.setResistance(0.0F);
	}

	@Override
	public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}
	
	@Override
	public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.INVISIBLE;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World p_149668_1_, int p_149668_2_, int p_149668_3_, int p_149668_4_) {
		return null;
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState p_149650_1_, Random p_149650_2_, int p_149650_3_) {
		return null;
	}

	@Override
	public boolean isFullCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
		return false;
	}

	@Override
	public boolean canCollideCheck(int p_149678_1_, boolean p_149678_2_) {
		return false;
	}

	@Override
	public boolean isReplaceable(IBlockAccess world, net.minecraft.util.math.BlockPos pos) {
		return false;
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		if(!world.isRemote) world.scheduleUpdate(new net.minecraft.util.math.BlockPos(x, y, z), this, 1);
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		if(!world.isRemote) world.scheduleUpdate(new net.minecraft.util.math.BlockPos(x, y, z), this, 1);
	}

	@Override
	public void updateTick(World world, net.minecraft.util.math.BlockPos nativeTickPos, net.minecraft.block.state.IBlockState nativeTickState, Random rand) {
		int x = nativeTickPos.getX(), y = nativeTickPos.getY(), z = nativeTickPos.getZ();
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			
			Block b = com.hbm.world.WorldBlockState.read(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ).getBlock();
			
			if(b == this)
				continue;
			
			if(isAirBlock(b)) {
				
				if(b.isReplaceable(world, new net.minecraft.util.math.BlockPos(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ))) {
					if(getAirCount(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ) == 0) {
						world.setBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, ModBlocks.vacuum);
						return;
					}
				}
				
				com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
				return;
			}
		}
	}
	
	private boolean isAirBlock(Block b) {
		
		if(b == this)
			return false;
		
		return b.getDefaultState().getMaterial() == Material.AIR || 
				b.getBlockBoundsMinX() > 0 || b.getBlockBoundsMinY() > 0 || b.getBlockBoundsMinZ() > 0 ||
				b.getBlockBoundsMaxX() < 1 || b.getBlockBoundsMaxY() < 1 || b.getBlockBoundsMaxZ() < 1;
	}
	
	private int getAirCount(World world, int x, int y, int z) {
		
		int air = 0;
		
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			
			Block b = com.hbm.world.WorldBlockState.read(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ).getBlock();
			
			if(isAirBlock(b))
				air++;
		}
		
		return air;
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
		
		if(!(entity instanceof EntityLivingBase))
			return;
		
		entity.attackEntityFrom(ModDamageSource.vacuum, 1F);
	}
}
