package com.hbm.entity.cart;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;

public class EntityMinecartBogie extends EntityMinecart {

	@Override
	public net.minecraft.entity.item.EntityMinecart.Type getType() {
		return net.minecraft.entity.item.EntityMinecart.Type.RIDEABLE;
	}

	public EntityMinecartBogie(World world) {
		super(world);
	}

	public EntityMinecartBogie(World world, double x, double y, double z) {
		super(world, x, y ,z);
	}

	public int getMinecartType() {
		return -1;
	}
}
