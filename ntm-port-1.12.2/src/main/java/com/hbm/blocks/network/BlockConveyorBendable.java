package com.hbm.blocks.network;

import api.hbm.block.IToolable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import com.hbm.lib.ForgeDirection;

public abstract class BlockConveyorBendable extends BlockConveyorBase implements IToolable {

	protected int getPathDirection(int meta) {

		if(meta >= 6 && meta <= 9) return 1;
		if(meta >= 10 && meta <= 13) return 2;
		return 0;
	}

	@Override
	public ForgeDirection getInputDirection(World world, int x, int y, int z) {
		int meta = getMetaFromState(world.getBlockState(new BlockPos(x, y, z)));
		int dir = getPathDirection(meta);
		return ForgeDirection.getOrientation(meta - dir * 4);
	}

	@Override
	public ForgeDirection getOutputDirection(World world, int x, int y, int z) {
		int meta = getMetaFromState(world.getBlockState(new BlockPos(x, y, z)));
		int dir = getPathDirection(meta);
		meta -= dir * 4;

		ForgeDirection primary = ForgeDirection.getOrientation(meta).getOpposite();

		if(dir == 2) return primary.getRotation(ForgeDirection.UP);
		if(dir == 1) return primary.getRotation(ForgeDirection.DOWN);
		return primary;
	}

	@Override
	public ForgeDirection getTravelDirection(World world, int x, int y, int z, Vec3d itemPos) {

		int meta = getMetaFromState(world.getBlockState(new BlockPos(x, y, z)));
		int dir = getPathDirection(meta);
		meta -= dir * 4;

		ForgeDirection primary = ForgeDirection.getOrientation(meta);

		if(dir > 0) {
			dir--;
			double ix = x + 0.5;
			double iz = z + 0.5;
			ForgeDirection secondary = primary.getRotation(ForgeDirection.UP);

			ix -= -primary.offsetX * 0.5 + secondary.offsetX * (0.5 - dir);
			iz -= -primary.offsetZ * 0.5 + secondary.offsetZ * (0.5 - dir);

			double dX = Math.abs(itemPos.x - ix);
			double dZ = Math.abs(itemPos.z - iz);

			if(dX + dZ >= 1) {

				if(dir == 0)
					return secondary.getOpposite();
				else
					return secondary;
			}
		}

		return primary;
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

			if(dir < 2)
				newMeta += 4;
			else
				newMeta -= 8;
		}

		world.setBlockState(new BlockPos(x, y, z), getStateFromMeta(newMeta), 3);

		return true;
	}
}
