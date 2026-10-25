package com.hbm.items.weapon;

import com.hbm.entity.grenade.EntityGrenadeBouncyGeneric;
import com.hbm.entity.grenade.EntityGrenadeImpactGeneric;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemGenericGrenade extends ItemGrenade {

	public ItemGenericGrenade(int fuse) {
		super(fuse);
	}

	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand nativeHand) {
		ItemStack stack = player.getHeldItem(nativeHand);
		return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.PASS, onItemRightClick(stack, world, player));
	}

	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(!player.capabilities.isCreativeMode) {
			stack.shrink(1);
		}

		world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.main.ModSoundEvents.resolve("random.bow"), net.minecraft.util.SoundCategory.PLAYERS, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

		if(!world.isRemote) {

			if(fuse == -1)
				world.spawnEntity(new EntityGrenadeImpactGeneric(world, player).setType(this));
			else
				world.spawnEntity(new EntityGrenadeBouncyGeneric(world, player).setType(this));
		}

		return stack;
	}

	public void explode(Entity grenade, EntityLivingBase thrower, World world, double x, double y, double z) { }

	public int getMaxTimer() {
		return this.fuse * 20;
	}

	public double getBounceMod() {
		return 0.5D;
	}
}
