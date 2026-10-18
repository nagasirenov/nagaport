package com.hbm.items.weapon.sedna.impl;

import com.hbm.entity.projectile.EntityBulletBaseMK4;
import com.hbm.extprop.HbmPlayerProps;
import com.hbm.handler.HbmKeybinds.EnumKeybind;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.factory.XFactoryTool;
import com.hbm.util.ArmorUtil;
import com.hbm.util.Vec3NT;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemGunChargeThrower extends ItemGunBaseNT {

	public static final String KEY_LASTHOOK = "lasthook";
	
	public ItemGunChargeThrower(WeaponQuality quality, GunConfig... cfg) {
		super(quality, cfg);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isHeld) {
		super.onUpdate(stack, world, entity, slot, isHeld);
		
		if(this.getState(stack, 0) == GunState.RELOADING) {
			if(this.getLastHook(stack) != -1) this.setLastHook(stack, -1);
		}
		
		if(isHeld && entity instanceof EntityPlayer) {
			Entity e = world.getEntityByID(this.getLastHook(stack));
			if(e != null && !e.isDead && e instanceof EntityBulletBaseMK4 && ((EntityBulletBaseMK4) e).config == XFactoryTool.ct_hook && ((EntityBulletBaseMK4) e).velocity < 0.01) {
				EntityPlayer player = (EntityPlayer) entity;
				Vec3NT vec = new Vec3NT(e.posX - player.posX, e.posY - player.posY - player.getEyeHeight(), e.posZ - player.posZ);
				double line = vec.length();
				if(HbmPlayerProps.getData((EntityPlayer) entity).getKeyPressed(EnumKeybind.GUN_PRIMARY)) {
					vec.normalizeSelf().multiply(0.1);
					player.motionX += vec.x;
					player.motionY += vec.y + 0.04;
					player.motionZ += vec.z;
					if(!world.isRemote && line < 2) e.setDead();
				} else if(!HbmPlayerProps.getData((EntityPlayer) entity).getKeyPressed(EnumKeybind.GUN_SECONDARY)) {
					Vec3NT nextPos = new Vec3NT(player.posX + player.motionX, player.posY + player.getEyeHeight() + player.motionY, player.posZ + player.motionZ);
					Vec3NT delta = new Vec3NT(e.posX - nextPos.x, e.posY - nextPos.y, e.posZ - nextPos.z);
					if(delta.length() > line) {
						delta.normalizeSelf().multiply(line);
						Vec3NT newNext = new Vec3NT(e.posX - delta.x, e.posY - delta.y, e.posZ - delta.z);
						Vec3NT vel = new Vec3NT(newNext.x - player.posX, newNext.y - player.posY - player.getEyeHeight(), newNext.z - player.posZ);
						if(vel.length() < 3) {
							player.motionX = vel.x;
							player.motionY = vel.y;
							player.motionZ = vel.z;
						}
					}
				} else {
					player.motionX *= 0.5;
					player.motionY *= 0.5;
					player.motionZ *= 0.5;
				}
				
				if(player.motionY > -0.1) player.fallDistance = 0;
				ArmorUtil.resetFlightTime((EntityPlayer)entity);
			}
		} else {
			if(this.getLastHook(stack) != -1) this.setLastHook(stack, -1);
		}
	}
	
	public static int getLastHook(ItemStack stack) { return getValueInt(stack, KEY_LASTHOOK); }
	public static void setLastHook(ItemStack stack, int value) { setValueInt(stack, KEY_LASTHOOK, value); }
}
