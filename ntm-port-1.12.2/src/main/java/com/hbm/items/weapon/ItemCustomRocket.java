package com.hbm.items.weapon;

import java.util.List;

import com.hbm.handler.RocketStruct;
import com.hbm.items.ISatChip;
import com.hbm.items.ModItems;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;

public class ItemCustomRocket extends Item implements ISatChip {

	public static ItemStack build(RocketStruct rocket) {
		ItemStack stack = new ItemStack(ModItems.rocket_custom);

		stack.setTagCompound(new NBTTagCompound());
		rocket.writeToNBT(stack.getTagCompound());

		return stack;
	}

	public static ItemStack build(RocketStruct rocket, boolean hasFuel) {
		ItemStack stack = build(rocket);

		setFuel(stack, hasFuel);

		return stack;
	}

	public static RocketStruct get(ItemStack stack) {
		if(stack == null || !(stack.getItem() instanceof ItemCustomRocket) || stack.getTagCompound() == null)
			return null;

		return RocketStruct.readFromNBT(stack.getTagCompound());
	}

	public static boolean hasFuel(ItemStack stack) {
		if(stack == null || stack.getTagCompound() == null) return false;
		return stack.getTagCompound().getBoolean("hasFuel");
	}

	public static void setFuel(ItemStack stack, boolean hasFuel) {
		if(stack == null) return;
		if(stack.getTagCompound() == null) stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setBoolean("hasFuel", hasFuel);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void addInformation(ItemStack stack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {
		RocketStruct rocket = get(stack);

		if(rocket == null) return;

		list.add(TextFormatting.BOLD + "Payload: " + TextFormatting.GRAY + I18nUtil.resolveKey(rocket.capsule.part.getTranslationKey() + ".name"));
		list.add(TextFormatting.BOLD + "Stages: " + TextFormatting.GRAY + rocket.stages.size());

		if(hasFuel(stack)) {
			list.add(TextFormatting.GRAY + "Is fully fueled");
		}

		if(getFreq(stack) != 0) {
			list.add(TextFormatting.BOLD + "Satellite Frequency: " + TextFormatting.GRAY + getFreq(stack));
		}
	}

}
