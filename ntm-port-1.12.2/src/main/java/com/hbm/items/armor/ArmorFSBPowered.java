package com.hbm.items.armor;

import java.util.List;

import com.hbm.handler.ArmorModHandler;
import com.hbm.util.BobMathUtil;

import api.hbm.energymk2.IBatteryItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ArmorFSBPowered extends ArmorFSB implements IBatteryItem {

	public long maxPower = 1;
	public long chargeRate;
	public long consumption;
	public long drain;

	public ArmorFSBPowered(ArmorMaterial material, int slot, String texture, long maxPower, long chargeRate, long consumption, long drain) {
		super(material, slot, texture);
		this.maxPower = maxPower;
		this.chargeRate = chargeRate;
		this.consumption = consumption;
		this.drain = drain;
		this.setMaxDamage(1);
	}

	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		list.add("Charge: " + BobMathUtil.getShortNumber(getCharge(stack)) + " / " + BobMathUtil.getShortNumber(getMaxCharge(stack)));
		super.addInformation(stack, player, list, ext);
	}

	@Override
	public boolean isArmorEnabled(ItemStack stack) {
		return getCharge(stack) > 0;
	}

	@Override
	public void chargeBattery(ItemStack stack, long i) {
		if(stack.getItem() instanceof ArmorFSBPowered) {
			if(stack.hasTagCompound()) {
				stack.getTagCompound().setLong("charge", stack.getTagCompound().getLong("charge") + i);
			} else {
				stack.setTagCompound(new NBTTagCompound());
				stack.getTagCompound().setLong("charge", i);
			}
		}
	}

	@Override
	public void setCharge(ItemStack stack, long i) {
		if(stack.getItem() instanceof ArmorFSBPowered) {
			if(stack.hasTagCompound()) {
				stack.getTagCompound().setLong("charge", i);
			} else {
				stack.setTagCompound(new NBTTagCompound());
				stack.getTagCompound().setLong("charge", i);
			}
		}
	}

	@Override
	public void dischargeBattery(ItemStack stack, long i) {
		if(stack.getItem() instanceof ArmorFSBPowered) {
			if(stack.hasTagCompound()) {
				stack.getTagCompound().setLong("charge", stack.getTagCompound().getLong("charge") - i);
			} else {
				stack.setTagCompound(new NBTTagCompound());
				stack.getTagCompound().setLong("charge", getMaxCharge(stack) - i);
			}

			if(stack.getTagCompound().getLong("charge") < 0)
				stack.getTagCompound().setLong("charge", 0);
		}
	}

	@Override
	public long getCharge(ItemStack stack) {
		if(stack.getItem() instanceof ArmorFSBPowered) {
			if(stack.hasTagCompound()) {
				return Math.min(stack.getTagCompound().getLong("charge"), getMaxCharge(stack));
			} else {
				stack.setTagCompound(new NBTTagCompound());
				stack.getTagCompound().setLong("charge", getMaxCharge(stack));
				return stack.getTagCompound().getLong("charge");
			}
		}

		return 0;
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return getCharge(stack) < getMaxCharge(stack);
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {

		return 1 - (double) getCharge(stack) / (double) getMaxCharge(stack);
	}

	@Override
	public long getMaxCharge(ItemStack stack) {
		if(ArmorModHandler.hasMods(stack)) {
			ItemStack mod = ArmorModHandler.pryMod(stack, ArmorModHandler.battery);
			if(mod != null && mod.getItem() instanceof ItemModBattery) {
				return (long) (maxPower * ((ItemModBattery) mod.getItem()).mod);
			}
		}
		return maxPower;
	}

	@Override
	public long getChargeRate(ItemStack stack) {
		return chargeRate;
	}

	@Override
	public long getDischargeRate(ItemStack stack) {
		return 0;
	}

	@Override
	public void setDamage(ItemStack stack, int damage) {
		this.dischargeBattery(stack, damage * consumption);
	}

	public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {

		super.onArmorTick(world, player, itemStack);

		if(this.drain > 0 && ArmorFSB.hasFSBArmor(player) && !player.capabilities.isCreativeMode) {
			this.dischargeBattery(itemStack, drain);
		}
	}
}
