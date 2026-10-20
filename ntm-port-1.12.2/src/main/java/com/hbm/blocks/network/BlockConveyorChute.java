package com.hbm.blocks.network;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;

import api.hbm.block.IToolable;
import api.hbm.conveyor.IConveyorBelt;
import api.hbm.conveyor.IEnterableBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
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

public class BlockConveyorChute extends BlockConveyorBase implements IToolable {

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return FULL_BLOCK_AABB;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return true;
	}

	@Override
	public Vec3d getTravelLocation(World world, int x, int y, int z, Vec3d itemPos, double speed) {

		Block below = world.getBlockState(new BlockPos(x, y - 1, z)).getBlock();
		if(below instanceof IConveyorBelt || below instanceof IEnterableBlock) {
			speed *= 5;
		} else if(itemPos.y > y + 0.25) {
			speed *= 3;
		}

		return super.getTravelLocation(world, x, y, z, itemPos, speed);
	}

	@Override
	public ForgeDirection getInputDirection(World world, int x, int y, int z) {
		return ForgeDirection.UP;
	}

	@Override
	public ForgeDirection getOutputDirection(World world, int x, int y, int z) {
		return ForgeDirection.DOWN;
	}

	@Override
	public ForgeDirection getTravelDirection(World world, int x, int y, int z, Vec3d itemPos) {

		Block below = world.getBlockState(new BlockPos(x, y - 1, z)).getBlock();
		if(below instanceof IConveyorBelt || below instanceof IEnterableBlock || itemPos.y > y + 0.25) {
			return ForgeDirection.UP;
		}

		return ForgeDirection.getOrientation(getMetaFromState(world.getBlockState(new BlockPos(x, y, z))));
	}

	@Override
	public Vec3d getClosestSnappingPosition(World world, int x, int y, int z, Vec3d itemPos) {

		Block below = world.getBlockState(new BlockPos(x, y - 1, z)).getBlock();
		if(below instanceof IConveyorBelt || below instanceof IEnterableBlock || itemPos.y > y + 0.25) {
			return new Vec3d(x + 0.5, itemPos.y, z + 0.5);
		} else {
			return super.getClosestSnappingPosition(world, x, y, z, itemPos);
		}
	}

	@Override
	protected Vec3d getTravelOriginAfterSnap(World world, int x, int y, int z, Vec3d itemPos) {
		Block below = world.getBlockState(new BlockPos(x, y - 1, z)).getBlock();
		if(below instanceof IConveyorBelt || below instanceof IEnterableBlock || itemPos.y > y + 0.25) return itemPos;
		return super.getTravelOriginAfterSnap(world, x, y, z, itemPos);
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
			world.setBlockState(new BlockPos(x, y, z), ModBlocks.conveyor.getStateFromMeta(newMeta), 3);
		}

		return true;
	}
}
