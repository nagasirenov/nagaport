package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.tileentity.TileEntityData;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class SpotlightBeam extends BlockBeamBase {

	// If a block is placed onto the beam, handle the new cutoff
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		if (!world.isRemote) {
			for (ForgeDirection dir : getDirections(world, x, y, z)) {
				Spotlight.unpropagateBeam(world, x, y, z, dir);
			}
		}
		super.breakBlock(world, pos, state);
	}

	// If a block in the beam path is removed, repropagate beam
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		if (world.isRemote) return;
		if (neighborBlock instanceof SpotlightBeam) return;

		for(ForgeDirection dir : getDirections(world, x, y, z)) {
			Spotlight.backPropagate(world, x, y, z, dir, getMetaFromState(world.getBlockState(pos)));
		}
	}

	// Directions are stored as a set of 6 bits:
	// 000000 -> no incoming light directions are set, will be removed
	// 010000 -> UP bit set, at least one direction is providing light
	// 111111 -> ALL directions illuminated, all incoming lights need to be disabled to turn off the beam
	public static List<ForgeDirection> getDirections(World world, int x, int y, int z) {
		TileEntityData te = (TileEntityData) world.getTileEntity(new BlockPos(x, y, z));
		if(te == null) return new ArrayList<ForgeDirection>();
		return getDirections(te.metadata);
	}

	public static List<ForgeDirection> getDirections(int metadata) {
		List<ForgeDirection> directions = new ArrayList<ForgeDirection>(6);
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			if ((metadata & dir.flag) == dir.flag) directions.add(dir);
		}
		return directions;
	}

	// Returns the final metadata, so the caller can optionally remove the block
	public static int setDirection(World world, int x, int y, int z, ForgeDirection dir, boolean state) {
		TileEntityData te = (TileEntityData) world.getTileEntity(new BlockPos(x, y, z));
		if (te == null) return 0; // This shouldn't happen, and if it does, cancel propagation
		int transformedMetadata = applyDirection(te.metadata, dir, state);
		te.metadata = transformedMetadata;
		return transformedMetadata;
	}

	// Sets the metadata bit for a given direction
	public static int applyDirection(int metadata, ForgeDirection direction, boolean state) {
		if(state) {
			return metadata | direction.flag;
		} else {
			return metadata & ~direction.flag;
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityData();
	}
}
