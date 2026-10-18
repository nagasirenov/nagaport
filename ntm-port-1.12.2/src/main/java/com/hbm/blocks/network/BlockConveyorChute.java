package com.hbm.blocks.network;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;

import api.hbm.block.IToolable;
import api.hbm.conveyor.IConveyorBelt;
import api.hbm.conveyor.IEnterableBlock;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class BlockConveyorChute extends BlockConveyorBase implements IToolable {

	@Override
	public Vec3d getTravelLocation(World world, int x, int y, int z, Vec3d itemPos, double speed) {

		Block below = world.getBlock(x, y - 1, z);
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

		Block below = world.getBlock(x, y - 1, z);
		if(below instanceof IConveyorBelt || below instanceof IEnterableBlock || itemPos.y > y + 0.25) {
			return ForgeDirection.UP;
		}

		return ForgeDirection.getOrientation(world.getBlockMetadata(x, y, z));
	}

	@Override
	public Vec3d getClosestSnappingPosition(World world, int x, int y, int z, Vec3d itemPos) {

		Block below = world.getBlock(x, y - 1, z);
		if(below instanceof IConveyorBelt || below instanceof IEnterableBlock || itemPos.y > y + 0.25) {
			return new Vec3d(x + 0.5, itemPos.y, z + 0.5);
		} else {
			return super.getClosestSnappingPosition(world, x, y, z, itemPos);
		}
	}

	@Override
	protected Vec3d getTravelOriginAfterSnap(World world, int x, int y, int z, Vec3d itemPos) {
		Block below = world.getBlock(x, y - 1, z);
		if(below instanceof IConveyorBelt || below instanceof IEnterableBlock || itemPos.y > y + 0.25) return itemPos;
		return super.getTravelOriginAfterSnap(world, x, y, z, itemPos);
	}

	public static int renderID = RenderingRegistry.getNextAvailableRenderId();

	@Override
	public int getRenderType() {
		return renderID;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) { }

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess p_149646_1_, int p_149646_2_, int p_149646_3_, int p_149646_4_, int p_149646_5_) {
		return true;
	}

	@Override
	public Item getItemDropped(int meta, Random rand, int fortune) {
		return ModItems.conveyor_wand;
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {

		if(tool != ToolType.SCREWDRIVER)
			return false;
		int meta = world.getBlockMetadata(x, y, z);
		int newMeta = meta;

		if(!player.isSneaking()) {
			if(meta > 9) meta -= 8;
			if(meta > 5) meta -= 4;
			newMeta = ForgeDirection.getOrientation(meta).getRotation(ForgeDirection.UP).ordinal();

			world.setBlockMetadataWithNotify(x, y, z, newMeta, 3);
		} else {
			world.setBlock(x, y, z, ModBlocks.conveyor, newMeta, 3);
		}

		return true;
	}
}
