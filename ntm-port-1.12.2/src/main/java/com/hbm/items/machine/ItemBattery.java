package com.hbm.items.machine;

import java.util.List;

import com.hbm.items.ModItems;
import com.hbm.util.BobMathUtil;

import api.hbm.energymk2.IBatteryItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemBattery extends Item implements IBatteryItem {

	protected long maxCharge;
	protected long chargeRate;
	protected long dischargeRate;

	public ItemBattery(long dura, long chargeRate, long dischargeRate) {
		this.maxCharge = dura;
		this.chargeRate = chargeRate;
		this.dischargeRate = dischargeRate;
	}

	@Override
	public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
		long charge = maxCharge;
		if(itemstack.hasTagCompound())
			charge = getCharge(itemstack);

		if(itemstack.getItem() != ModItems.fusion_core && itemstack.getItem() != ModItems.energy_core) {
			list.add("Energy stored: " + BobMathUtil.getShortNumber(charge) + "/" + BobMathUtil.getShortNumber(maxCharge) + "HE");
		} else {
			String charge1 = BobMathUtil.getShortNumber((charge * 100) / this.maxCharge);
			list.add("Charge: " + charge1 + "%");
			list.add("(" + BobMathUtil.getShortNumber(charge) + "/" + BobMathUtil.getShortNumber(maxCharge) + "HE)");
		}
		list.add("Charge rate: " + BobMathUtil.getShortNumber(chargeRate) + "HE/t");
		list.add("Discharge rate: " + BobMathUtil.getShortNumber(dischargeRate) + "HE/t");
	}

	@Override
	public EnumRarity getRarity(ItemStack p_77613_1_) {

		if(this == ModItems.battery_schrabidium) {
			return EnumRarity.RARE;
		}

		if(this == ModItems.fusion_core || this == ModItems.energy_core) {
			return EnumRarity.UNCOMMON;
		}

		return EnumRarity.COMMON;
	}

	public void chargeBattery(ItemStack stack, long i) {
		if(stack.getItem() instanceof ItemBattery) {
			if(stack.hasTagCompound()) {
				stack.getTagCompound().setLong("charge", stack.getTagCompound().getLong("charge") + i);
			} else {
				stack.setTagCompound(new NBTTagCompound());
				stack.getTagCompound().setLong("charge", i);
			}
		}
	}

	public void setCharge(ItemStack stack, long i) {
		if(stack.getItem() instanceof ItemBattery) {
			if(stack.hasTagCompound()) {
				stack.getTagCompound().setLong("charge", i);
			} else {
				stack.setTagCompound(new NBTTagCompound());
				stack.getTagCompound().setLong("charge", i);
			}
		}
	}

	public void dischargeBattery(ItemStack stack, long i) {
		if(stack.getItem() instanceof ItemBattery) {
			if(stack.hasTagCompound()) {
				stack.getTagCompound().setLong("charge", stack.getTagCompound().getLong("charge") - i);
			} else {
				stack.setTagCompound(new NBTTagCompound());
				stack.getTagCompound().setLong("charge", this.maxCharge - i);
			}
		}
	}

	public long getCharge(ItemStack stack) {
		if(stack.getItem() instanceof ItemBattery) {
			if(stack.hasTagCompound()) {
				return stack.getTagCompound().getLong("charge");
			} else {
				stack.setTagCompound(new NBTTagCompound());
				stack.getTagCompound().setLong("charge", ((ItemBattery) stack.getItem()).maxCharge);
				return stack.getTagCompound().getLong("charge");
			}
		}

		return 0;
	}

	@Override
	public long getMaxCharge(ItemStack stack) {
		return maxCharge;
	}

	@Override
	public long getChargeRate(ItemStack stack) {
		return chargeRate;
	}

	@Override
	public long getDischargeRate(ItemStack stack) {
		return dischargeRate;
	}

	public static ItemStack getEmptyBattery(Item item) {

		if(item instanceof ItemBattery) {
			ItemStack stack = new ItemStack(item);
			stack.setTagCompound(new NBTTagCompound());
			stack.getTagCompound().setLong("charge", 0);
			return stack.copy();
		}

		return null;
	}

	public static ItemStack getFullBattery(Item item) {

		if(item instanceof ItemBattery) {
			ItemStack stack = new ItemStack(item);
			stack.setTagCompound(new NBTTagCompound());
			stack.getTagCompound().setLong("charge", ((ItemBattery) item).getMaxCharge(stack));
			return stack.copy();
		}

		return new ItemStack(item);
	}

	public boolean showDurabilityBar(ItemStack stack) {
		return true;
	}

	public double getDurabilityForDisplay(ItemStack stack) {
		return 1D - (double) getCharge(stack) / (double) getMaxCharge(stack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {

		if(!this.isInCreativeTab(tab)) return;

		if(this.chargeRate > 0) {
			list.add(getEmptyBattery(this));
		}
		
		if(this.dischargeRate > 0) {
			list.add(getFullBattery(this));
		}
	}
}
