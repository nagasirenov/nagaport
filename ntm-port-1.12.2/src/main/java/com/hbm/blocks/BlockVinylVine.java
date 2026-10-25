package com.hbm.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockVine;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos;

public class BlockVinylVine extends BlockVine {

	public BlockVinylVine withSoundType(net.minecraft.block.SoundType sound) {
		super.setSoundType(sound);
		return this;
	}

	public BlockVinylVine() {
		super();
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlockId, BlockPos fromPos) {
		super.neighborChanged(state, world, pos, neighborBlockId, fromPos);
	}

	@Override
	public boolean canPlaceBlockAt(World world, net.minecraft.util.math.BlockPos nativePlacementPos) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
		return super.canPlaceBlockAt(world, new net.minecraft.util.math.BlockPos(x, y, z)) && this.canBlockStay(world, x, y, z);
	}

	protected boolean canPlaceBlockOn(Block block) {
		return !block.getDefaultState().isOpaqueCube() || block instanceof BlockRubberLeaves;
	}


	public boolean canBlockStay(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {
		return canBlockStay(worldIn, pos.getX(), pos.getY(), pos.getZ());
	}

public boolean canBlockStay(World world, int x, int y, int z) {
		Block blockBelow = com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock();
		return !blockBelow.getDefaultState().isOpaqueCube() || blockBelow instanceof BlockRubberLeaves || false;
	}

	@SideOnly(Side.CLIENT)
	public int getBlockColor() {
		return 0xFFFFFF;
	}

	/**
	 * Returns the color this block should be rendered. Used by leaves.
	 */
	@SideOnly(Side.CLIENT)
	public int getRenderColor(int p_149741_1_) {
		return 0xFFFFFF;
	}

	@SideOnly(Side.CLIENT)
	public int colorMultiplier(IBlockAccess p_149720_1_, int p_149720_2_, int p_149720_3_, int p_149720_4_) {
		return 0xFFFFFF;
	}

}
