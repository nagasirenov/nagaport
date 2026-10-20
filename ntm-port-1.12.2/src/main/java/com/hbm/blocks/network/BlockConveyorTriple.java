package com.hbm.blocks.network;

import java.util.Random;

import com.hbm.items.ModItems;

import net.minecraft.item.Item;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class BlockConveyorTriple extends BlockConveyorBendable {

	@Override
	public Vec3d getClosestSnappingPosition(World world, int x, int y, int z, Vec3d itemPos) {

		ForgeDirection dir = this.getTravelDirection(world, x, y, z, itemPos);

		double itemX = MathHelper.clamp(itemPos.x, x, x + 1);
		double itemZ = MathHelper.clamp(itemPos.z, z, z + 1);

		double posX = x + 0.5;
		double posZ = z + 0.5;

		if(dir.offsetX != 0) {
			posX = itemX;
			posZ += (itemZ > posZ + 0.15 ? 0.3125 : itemZ < posZ - 0.15 ? -0.3125 : 0);
		}
		if(dir.offsetZ != 0) {
			posZ = itemZ;
			posX += (itemX > posX + 0.15 ? 0.3125 : itemX < posX - 0.15 ? -0.3125 : 0);
		}

		return new Vec3d(posX, y + 0.25, posZ);
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return ModItems.conveyor_wand;
	}

	@Override
	public int damageDropped(IBlockState state) {
		return 3;
	}

}
