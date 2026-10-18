package com.hbm.blocks.network;

import java.util.Random;

import com.hbm.items.ModItems;

import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BlockConveyorExpress extends BlockConveyorBendable {

	@Override
	public Vec3d getTravelLocation(World world, int x, int y, int z, Vec3d itemPos, double speed) {
		return super.getTravelLocation(world, x, y, z, itemPos, speed * 3);
	}

	@Override
	public Item getItemDropped(int meta, Random rand, int fortune) {
		return ModItems.conveyor_wand;
	}

	@Override
	public int damageDropped(int meta) {
		return 1;
	}

}