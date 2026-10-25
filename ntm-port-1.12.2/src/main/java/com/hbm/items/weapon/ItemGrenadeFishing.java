package com.hbm.items.weapon;

import java.util.Random;

import com.hbm.dim.DimensionFishingLoot;
import com.hbm.entity.item.EntityItemBuoyant;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemGrenadeFishing extends ItemGenericGrenade {

	public ItemGrenadeFishing(int fuse) {
		super(fuse);
	}

	@Override
	public void explode(Entity grenade, EntityLivingBase thrower, World world, double x, double y, double z) {
		world.newExplosion(null, x, y + 0.25D, z, 3F, false, false);

		int iX = (int) Math.floor(x);
		int iY = (int) Math.floor(y);
		int iZ = (int) Math.floor(z);

		for(int i = 0; i < 15; i++) {

			int rX = iX + world.rand.nextInt(15) - 7;
			int rY = iY + world.rand.nextInt(15) - 7;
			int rZ = iZ + world.rand.nextInt(15) - 7;

			if(com.hbm.world.WorldBlockState.read(world, rX, rY, rZ).getBlock().getDefaultState().getMaterial() == Material.WATER) {
				ItemStack loot = getRandomLoot(world.rand, world);
				if(!loot.isEmpty()) {
					EntityItemBuoyant item = new EntityItemBuoyant(world, rX + 0.5, rY + 0.5, rZ + 0.5, loot.copy());
					item.motionY = 1;
					world.spawnEntity(item);
				}
			}
		}
	}

	public static ItemStack getRandomLoot(Random rand, World world) {
		return DimensionFishingLoot.getRandomFishLoot(world, rand);
	}

	@Override
	public int getMaxTimer() {
		return 60;
	}

	@Override
	public double getBounceMod() {
		return 0.5D;
	}
}
