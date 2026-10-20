package com.hbm.items.weapon;

import java.util.List;

import com.hbm.config.WeaponConfig;
import com.hbm.entity.effect.EntityCloudFleijaRainbow;
import com.hbm.entity.logic.EntityNukeExplosionMK3;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class WeaponizedCell extends Item {

	public boolean onEntityItemUpdate(EntityItem item) {
		World world = item.world;

		if(item.ticksExisted > 50 * 20 || item.isBurning()) {

			if(!world.isRemote) {

				if(WeaponConfig.dropStar) {

					EntityNukeExplosionMK3 ex = EntityNukeExplosionMK3.statFacFleija(world, item.posX, item.posY, item.posZ, 100);
					if(!ex.isDead) {
						com.hbm.sound.VanillaWorldSoundEffects.play(world, item.posX, item.posY, item.posZ, "random.explode", 100.0F, world.rand.nextFloat() * 0.1F + 0.9F);
						world.spawnEntityInWorld(ex);

						EntityCloudFleijaRainbow cloud = new EntityCloudFleijaRainbow(world, 100);
						cloud.posX = item.posX;
						cloud.posY = item.posY;
						cloud.posZ = item.posZ;
						world.spawnEntityInWorld(cloud);
					}
				}

				item.setDead();
			}
		}

		int randy = (50 * 20) - item.ticksExisted;

		if(randy < 1)
			randy = 1;

		if(item.world.rand.nextInt(50 * 20) >= randy)
			world.spawnParticle(net.minecraft.util.EnumParticleTypes.REDSTONE, item.posX + item.world.rand.nextGaussian() * item.width / 2, item.posY + item.world.rand.nextGaussian() * item.height, item.posZ + item.world.rand.nextGaussian() * item.width / 2, 0.0, 0.0, 0.0);
		else
			world.spawnParticle(net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL, item.posX + item.world.rand.nextGaussian() * item.width / 2, item.posY + item.world.rand.nextGaussian() * item.height, item.posZ + item.world.rand.nextGaussian() * item.width / 2, 0.0, 0.0, 0.0);

		if(randy < 100)
			world.spawnParticle(net.minecraft.util.EnumParticleTypes.LAVA, item.posX + item.world.rand.nextGaussian() * item.width / 2, item.posY + item.world.rand.nextGaussian() * item.height, item.posZ + item.world.rand.nextGaussian() * item.width / 2, 0.0, 0.0, 0.0);

		return false;
	}

	@Override
	public void addInformation(ItemStack itemstack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {

		list.add("A charged energy cell, rigged to explode");
		list.add("when left on the floor for too long.");
	}
}
