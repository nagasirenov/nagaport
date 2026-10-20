package com.hbm.blocks.network;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import com.hbm.lib.ForgeDirection;

public class BlockConveyor extends BlockConveyorBendable {

	public BlockConveyor() {
		super();
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

		int dir = getPathDirection(meta);

		if(!player.isSneaking()) {
			if(meta > 9) meta -= 8;
			if(meta > 5) meta -= 4;
			newMeta = ForgeDirection.getOrientation(meta).getRotation(ForgeDirection.UP).ordinal() + dir * 4;
		} else {

			if(dir < 2) {
				newMeta += 4;
			} else {
				newMeta -= 8;

				// switcheroo
				world.setBlockState(new BlockPos(x, y, z), ModBlocks.conveyor_lift.getStateFromMeta(newMeta), 3);
				return true;
			}
		}

		world.setBlockState(new BlockPos(x, y, z), getStateFromMeta(newMeta), 3);

		return true;
	}

}