package com.hbm.blocks.network;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;

import api.hbm.block.IToolable;
import api.hbm.conveyor.IConveyorBelt;
import api.hbm.conveyor.IEnterableBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import com.hbm.lib.ForgeDirection;

public class BlockConveyorLift extends BlockConveyorBase implements IToolable {

	private static final AxisAlignedBB TOP_BOUNDS = new AxisAlignedBB(0, 0, 0, 1, 0.5, 1);

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		boolean bottom = !(world.getBlockState(pos.down()).getBlock() instanceof IConveyorBelt);
		boolean top = !(world.getBlockState(pos.up()).getBlock() instanceof IConveyorBelt) && !bottom && !(world.getBlockState(pos.up()).getBlock() instanceof IEnterableBlock);
		return top ? TOP_BOUNDS : FULL_BLOCK_AABB;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return true;
	}

	@Override
	public ForgeDirection getInputDirection(World world, int x, int y, int z) {
		return ForgeDirection.DOWN;
	}

	@Override
	public ForgeDirection getOutputDirection(World world, int x, int y, int z) {
		return ForgeDirection.UP;
	}

	@Override
	public ForgeDirection getTravelDirection(World world, int x, int y, int z, Vec3d itemPos) {

		boolean bottom = !(world.getBlockState(new BlockPos(x, y - 1, z)).getBlock() instanceof IConveyorBelt);
		boolean top = !(world.getBlockState(new BlockPos(x, y + 1, z)).getBlock() instanceof IConveyorBelt) && !bottom && !(world.getBlockState(new BlockPos(x, y + 1, z)).getBlock() instanceof IEnterableBlock);

		if(!top) {
			return ForgeDirection.DOWN;
		}

		return ForgeDirection.getOrientation(getMetaFromState(world.getBlockState(new BlockPos(x, y, z))));
	}

	@Override
	public Vec3d getClosestSnappingPosition(World world, int x, int y, int z, Vec3d itemPos) {

		boolean bottom = !(world.getBlockState(new BlockPos(x, y - 1, z)).getBlock() instanceof IConveyorBelt);
		boolean top = !(world.getBlockState(new BlockPos(x, y + 1, z)).getBlock() instanceof IConveyorBelt) && !bottom && !(world.getBlockState(new BlockPos(x, y + 1, z)).getBlock() instanceof IEnterableBlock);

		if(!top) {
			return new Vec3d(x + 0.5, itemPos.y, z + 0.5);
		} else {
			return super.getClosestSnappingPosition(world, x, y, z, itemPos);
		}
	}

	@Override
	protected Vec3d getTravelOriginAfterSnap(World world, int x, int y, int z, Vec3d itemPos) {
		boolean bottom = !(world.getBlockState(new BlockPos(x, y - 1, z)).getBlock() instanceof IConveyorBelt);
		boolean top = !(world.getBlockState(new BlockPos(x, y + 1, z)).getBlock() instanceof IConveyorBelt) && !bottom && !(world.getBlockState(new BlockPos(x, y + 1, z)).getBlock() instanceof IEnterableBlock);
		return top ? super.getTravelOriginAfterSnap(world, x, y, z, itemPos) : itemPos;
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return ModItems.conveyor_wand;
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {

		if(tool != ToolType.SCREWDRIVER)
			return false;
		int meta = getMetaFromState(world.getBlockState(new BlockPos(x, y, z)));
		int newMeta = meta;

		if(!player.isSneaking()) {
			if(meta > 9) meta -= 8;
			if(meta > 5) meta -= 4;
			newMeta = ForgeDirection.getOrientation(meta).getRotation(ForgeDirection.UP).ordinal();

			world.setBlockState(new BlockPos(x, y, z), getStateFromMeta(newMeta), 3);
		} else {
			world.setBlockState(new BlockPos(x, y, z), ModBlocks.conveyor_chute.getStateFromMeta(newMeta), 3);
		}

		return true;
	}
}
