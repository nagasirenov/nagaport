package com.hbm.handler;

import com.hbm.blocks.BlockDummyable;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import com.hbm.lib.ForgeDirection;

public class MultiblockHandlerXR {

	//when looking north
	//											U  D  N  S  W  E
	public static int[] uni = 		new int[] { 3, 0, 4, 4, 4, 4 };

	public static boolean checkSpace(World world, int x, int y, int z, int[] dim, int ox, int oy, int oz, ForgeDirection dir) {

		if(dim == null || dim.length != 6)
			return false;

		int count = 0;

		int[] rot = rotate(dim, dir);

		for(int a = x - rot[4]; a <= x + rot[5]; a++) {
			for(int b = y - rot[1]; b <= y + rot[0]; b++) {
				for(int c = z - rot[2]; c <= z + rot[3]; c++) {
					if(!world.getBlockState(new BlockPos(a, b, c)).getBlock().isReplaceable(world, new BlockPos(a, b, c))) {
						return false;
					}

					count++;

					if(count > 2000) {
						System.out.println("checkspace: ded " + a + " " + b + " " + c + " " + x + " " + y + " " + z);
						return false;
					}
				}
			}
		}

		return true;
	}

	public static void fillSpace(World world, int x, int y, int z, int[] dim, Block block, ForgeDirection dir) {

		if(dim == null || dim.length != 6)
			return;

		int count = 0;

		int[] rot = rotate(dim, dir);

		BlockDummyable.safeRem = true;

		for(int a = x - rot[4]; a <= x + rot[5]; a++) {
			for(int b = y - rot[1]; b <= y + rot[0]; b++) {
				for(int c = z - rot[2]; c <= z + rot[3]; c++) {

					int meta = 0;

					if(b < y) {
						meta = ForgeDirection.DOWN.ordinal();
					} else if(b > y) {
						meta = ForgeDirection.UP.ordinal();
					} else if(a < x) {
						meta = ForgeDirection.WEST.ordinal();
					} else if(a > x) {
						meta = ForgeDirection.EAST.ordinal();
					} else if(c < z) {
						meta = ForgeDirection.NORTH.ordinal();
					} else if(c > z) {
						meta = ForgeDirection.SOUTH.ordinal();
					} else {
						continue;
					}

					world.setBlockState(new BlockPos(a, b, c), block.getStateFromMeta(meta), 3);

					count++;

					if(count > 2000) {
						System.out.println("fillspace: ded " + a + " " + b + " " + c + " " + x + " " + y + " " + z);

						BlockDummyable.safeRem = false;
						return;
					}
				}
			}
		}

		BlockDummyable.safeRem = false;
	}

	@Deprecated
	public static void emptySpace(World world, int x, int y, int z, int[] dim, Block block, ForgeDirection dir) {

		if(dim == null || dim.length != 6)
			return;

		int count = 0;

		System.out.println("emptyspace is deprecated and shouldn't even be executed");

		int[] rot = rotate(dim, dir);

		for(int a = x - rot[4]; a <= x + rot[5]; a++) {
			for(int b = y - rot[1]; b <= y + rot[0]; b++) {
				for(int c = z - rot[2]; c <= z + rot[3]; c++) {

					if(world.getBlockState(new BlockPos(a, b, c)).getBlock() == block)
						world.setBlockToAir(new BlockPos(a, b, c));

					count++;

					if(count > 2000) {
						System.out.println("emptyspace: ded " + a + " " + b + " " + c);
						return;
					}
				}
			}
		}
	}

	public static int[] rotate(int[] dim, ForgeDirection dir) {

		if(dim == null)
			return null;

		if(dir == ForgeDirection.SOUTH)
			return dim;

		if(dir == ForgeDirection.NORTH) {
			//                 U       D       N       S       W       E
			return new int[] { dim[0], dim[1], dim[3], dim[2], dim[5], dim[4] };
		}

		if(dir == ForgeDirection.EAST) {
			//                 U       D       N       S       W       E
			return new int[] { dim[0], dim[1], dim[5], dim[4], dim[2], dim[3] };
		}

		if(dir == ForgeDirection.WEST) {
			//                 U       D       N       S       W       E
			return new int[] { dim[0], dim[1], dim[4], dim[5], dim[3], dim[2] };
		}

		return dim;
	}

}
