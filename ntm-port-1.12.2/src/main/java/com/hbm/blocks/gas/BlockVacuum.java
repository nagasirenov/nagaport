package com.hbm.blocks.gas;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.ModDamageSource;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
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
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return NULL_AABB;
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
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		return false;
	}

	@Override
	public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid) {
		return false;
	}

	@Override
	public boolean isReplaceable(IBlockAccess world, net.minecraft.util.math.BlockPos pos) {
		return false;
	}

	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		if(!worldIn.isRemote) worldIn.scheduleUpdate(pos, this, 1);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
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
						com.hbm.world.WorldBlockState.writeLegacyBlock(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, ModBlocks.vacuum);
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

		return b.getDefaultState().getMaterial() == Material.AIR || !b.getDefaultState().isFullCube();
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
	public void onEntityCollision(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, net.minecraft.entity.Entity entityIn) {
		onEntityCollidedWithBlock(worldIn, pos.getX(), pos.getY(), pos.getZ(), entityIn);
	}

public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {

		if(!(entity instanceof EntityLivingBase))
			return;

		entity.attackEntityFrom(ModDamageSource.vacuum, 1F);
	}
}
