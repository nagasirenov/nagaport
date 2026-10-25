package com.hbm.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import com.hbm.lib.ForgeDirection;

public class BlockDummyableBeam extends BlockDummyable implements ILookOverlay {

	// Passes on any interactions to the true dummyable

	public BlockDummyableBeam(Material mat) {
		super(mat);
		setLightLevel(1.0F);
		setLightOpacity(0);
	}

	@Override public int getRenderType() { return -1; }

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {0, 0, 0, 0, 0, 0};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	public int[] findCore(World world, int x, int y, int z) {
		int metadata = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );

		// if it's an extra, remove the extra-ness
		if(metadata >= extra) metadata -= extra;

		ForgeDirection dir = ForgeDirection.getOrientation(metadata).getOpposite();

		x += dir.offsetX;
		y += dir.offsetY;
		z += dir.offsetZ;

		Block b = com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock();

		if(b instanceof BlockDummyable && !(b instanceof BlockDummyableBeam)) {
			return ((BlockDummyable) b).findCore(world, x, y, z);
		}

		return null;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int i) {
		int metadata = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );

		// if it's an extra, remove the extra-ness
		if(metadata >= extra) metadata -= extra;

		ForgeDirection dir = ForgeDirection.getOrientation(metadata).getOpposite();

		x += dir.offsetX;
		y += dir.offsetY;
		z += dir.offsetZ;

		Block b = com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock();

		if(b instanceof BlockDummyable) {
			((BlockDummyable) b).breakBlock(world, x, y, z, b, i);
		}
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		if(world.isRemote || safeRem) return;

		int metadata = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );

		// if it's an extra, remove the extra-ness
		if(metadata >= extra) metadata -= extra;

		ForgeDirection dir = ForgeDirection.getOrientation(metadata).getOpposite();
		Block b = com.hbm.world.WorldBlockState.read(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ).getBlock();

		if(!(b instanceof BlockDummyable)) {
			com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
		}
	}


		@Override
	public void updateTick(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, java.util.Random rand) {
		updateTick(worldIn, pos.getX(), pos.getY(), pos.getZ(), rand);
	}

public void updateTick(World world, int x, int y, int z, Random rand) {
		if(world.isRemote) return;

		int metadata = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );

		// if it's an extra, remove the extra-ness
		if(metadata >= extra) metadata -= extra;

		ForgeDirection dir = ForgeDirection.getOrientation(metadata).getOpposite();
		Block b = com.hbm.world.WorldBlockState.read(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ).getBlock();

		if(!(b instanceof BlockDummyable)) {
			com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
		}
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;

		int metadata = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );

		// if it's an extra, remove the extra-ness
		if(metadata >= extra) metadata -= extra;

		ForgeDirection dir = ForgeDirection.getOrientation(metadata).getOpposite();

		x += dir.offsetX;
		y += dir.offsetY;
		z += dir.offsetZ;

		Block b = com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock();

		if(b instanceof BlockDummyable && !(b instanceof BlockDummyableBeam)) {
			net.minecraft.util.math.BlockPos targetPos = new net.minecraft.util.math.BlockPos(x, y, z);
			return b.onBlockActivated(world, targetPos, world.getBlockState(targetPos), player, nativeHand, nativeFacing, hitX, hitY, hitZ);
		}

		return false;
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		int metadata = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );

		// if it's an extra, remove the extra-ness
		if(metadata >= extra) metadata -= extra;

		ForgeDirection dir = ForgeDirection.getOrientation(metadata).getOpposite();

		x += dir.offsetX;
		y += dir.offsetY;
		z += dir.offsetZ;

		Block b = com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock();

		if(b instanceof BlockDummyable && !(b instanceof BlockDummyableBeam) && b instanceof ILookOverlay) {
			((ILookOverlay) b).printHook(event, world, x, y, z);
		}
	}

}
