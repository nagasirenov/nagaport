package com.hbm.items.weapon.sedna.impl;

import java.util.List;

import com.hbm.items.ICustomizable;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.mods.XWeaponModManager;
import com.hbm.util.ChatBuilder;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemGunNI4NI extends ItemGunBaseNT implements ICustomizable {

	public ItemGunNI4NI(WeaponQuality quality, GunConfig... cfg) {
		super(quality, cfg);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isHeld) {
		super.onUpdate(stack, world, entity, slot, isHeld);
		
		if(!world.isRemote) {
			
			int maxCoin = 4;
			if(XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_NI4NI_NICKEL)) maxCoin += 2;
			if(XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_NI4NI_DOUBLOONS)) maxCoin += 2;
			
			if(this.getCoinCount(stack) < maxCoin) {
				this.setCoinCharge(stack, this.getCoinCharge(stack) + 1);
				
				if(this.getCoinCharge(stack) >= 80) {
					this.setCoinCharge(stack, 0);
					int newCount = this.getCoinCount(stack) + 1;
					this.setCoinCount(stack, newCount);
					
					if(isHeld) {
						world.playSoundAtEntity(entity, "hbm:item.techBoop", 1.0F, 1F + newCount / (float) maxCoin);
					}
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		list.add("Now, don't get the wrong idea.");
		list.add("I " + TextFormatting.RED + "fucking hate " + TextFormatting.GRAY + "this game.");
		list.add("I didn't do this for you, I did it for sea.");
		super.addInformation(stack, player, list, ext);
	}

	@Override
	public void customize(EntityPlayer player, ItemStack stack, String... args) {
		
		if(args.length == 0) {
			resetColors(stack);
			player.sendMessage(ChatBuilder.start("Colors reset!").color(TextFormatting.GREEN).flush());
			return;
		}
		
		if(args.length != 3) {
			resetColors(stack);
			player.sendMessage(ChatBuilder.start("Requires three hexadecimal colors!").color(TextFormatting.RED).flush());
			return;
		}
		
		try {
			int dark = Integer.parseInt(args[0], 16);
			int light = Integer.parseInt(args[1], 16);
			int grip = Integer.parseInt(args[2], 16);
			
			if(dark < 0 || dark > 0xffffff || light < 0 || light > 0xffffff || grip < 0 || grip > 0xffffff) {
				player.sendMessage(ChatBuilder.start("Colors must range from 0 to FFFFFF!").color(TextFormatting.RED).flush());
				return;
			}
			
			setColors(stack, dark, light, grip);
			player.sendMessage(ChatBuilder.start("Colors set!").color(TextFormatting.GREEN).flush());
			
		} catch(Throwable ex) {
			player.sendMessage(ChatBuilder.start(ex.getLocalizedMessage()).color(TextFormatting.RED).flush());
		}
	}
	
	public static void resetColors(ItemStack stack) {
		if(!stack.hasTagCompound()) return;
		stack.getTagCompound().removeTag("colors");
	}
	
	public static void setColors(ItemStack stack, int dark, int light, int grip) {
		if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setIntArray("colors", new int[] {dark, light, grip});
	}
	
	public static int[] getColors(ItemStack stack) {
		if(!stack.hasTagCompound() || !stack.getTagCompound().hasKey("colors")) return null;
		int[] colors = stack.getTagCompound().getIntArray("colors");
		if(colors.length != 3) return null;
		return colors;
	}
	
	public static final String KEY_COIN_COUNT = "coincount";
	public static final String KEY_COIN_CHARGE = "coincharge";
	public static int getCoinCount(ItemStack stack) { return getValueInt(stack, KEY_COIN_COUNT); }
	public static void setCoinCount(ItemStack stack, int value) { setValueInt(stack, KEY_COIN_COUNT, value); }
	public static int getCoinCharge(ItemStack stack) { return getValueInt(stack, KEY_COIN_CHARGE); }
	public static void setCoinCharge(ItemStack stack, int value) { setValueInt(stack, KEY_COIN_CHARGE, value); }
}
