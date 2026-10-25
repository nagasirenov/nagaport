package com.hbm.items.weapon;

import java.util.List;

import com.hbm.handler.MissileStruct;
import com.hbm.items.ModItems;
import com.hbm.util.i18n.I18nUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

public class ItemCustomMissile extends Item {

	public static ItemStack buildMissile(Item chip, Item warhead, Item fuselage, Item stability, Item thruster) {

		if(stability == null) {
			return buildMissile(new ItemStack(chip), new ItemStack(warhead), new ItemStack(fuselage), null, new ItemStack(thruster));
		} else {
			return buildMissile(new ItemStack(chip), new ItemStack(warhead), new ItemStack(fuselage), new ItemStack(stability), new ItemStack(thruster));
		}
	}

	public static ItemStack buildMissile(ItemStack chip, ItemStack warhead, ItemStack fuselage, ItemStack stability, ItemStack thruster) {

		ItemStack missile = new ItemStack(ModItems.missile_custom);

		writePartToNBT(missile, "chip", chip.getItem());
		writePartToNBT(missile, "warhead", warhead.getItem());
		writePartToNBT(missile, "fuselage", fuselage.getItem());
		writePartToNBT(missile, "thruster", thruster.getItem());

		if(stability != null)
			writePartToNBT(missile, "stability", stability.getItem());

		return missile;
	}

	// numeric item ids shift between sessions; only registry names may be persisted
	private static void writePartToNBT(ItemStack stack, String key, Item item) {
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		ResourceLocation name = item.getRegistryName();
		if(name != null)
			stack.getTagCompound().setString(key + "_name", name.toString());
		else
			stack.getTagCompound().setInteger(key, Item.getIdFromItem(item));
	}

	public static int readFromNBT(ItemStack stack, String key) {
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		return stack.getTagCompound().getInteger(key);
	}

	// reads a part written by buildMissile; legacy numeric ids are validated by instanceof instead of cast
	public static ItemCustomMissilePart readPart(ItemStack stack, String key) {

		if(stack == null || !stack.hasTagCompound())
			return null;

		NBTTagCompound nbt = stack.getTagCompound();
		Item item = null;

		if(nbt.hasKey(key + "_name", 8))
			item = Item.getByNameOrId(nbt.getString(key + "_name"));
		else if(nbt.hasKey(key, 99))
			item = Item.getItemById(nbt.getInteger(key));

		return item instanceof ItemCustomMissilePart ? (ItemCustomMissilePart) item : null;
	}

	// validates a runtime numeric id (DataWatcher, packets); returns null for foreign ids instead of throwing
	public static ItemCustomMissilePart readPart(int id) {
		Item item = Item.getItemById(id);
		return item instanceof ItemCustomMissilePart ? (ItemCustomMissilePart) item : null;
	}

	@Override
	public void addInformation(ItemStack stack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {

		if(!stack.hasTagCompound())
			return;

		try {
			ItemCustomMissilePart chip = readPart(stack, "chip");
			ItemCustomMissilePart warhead = readPart(stack, "warhead");
			ItemCustomMissilePart fuselage = readPart(stack, "fuselage");
			ItemCustomMissilePart stability = readPart(stack, "stability");
			ItemCustomMissilePart thruster = readPart(stack, "thruster");

			list.add(TextFormatting.BOLD + I18nUtil.resolveKey("item.missile.desc.warhead") + ": " + TextFormatting.GRAY + warhead.getWarhead());
			list.add(TextFormatting.BOLD + I18nUtil.resolveKey("item.missile.desc.strength") + ": " + TextFormatting.GRAY + warhead.attributes[1]);
			list.add(TextFormatting.BOLD + I18nUtil.resolveKey("item.missile.desc.fuelType") + ": " + TextFormatting.GRAY + fuselage.getFuelName());
			list.add(TextFormatting.BOLD + I18nUtil.resolveKey("item.missile.desc.fuelAmount") + ": " + TextFormatting.GRAY + fuselage.getTankSize() + "l");
			list.add(TextFormatting.BOLD + I18nUtil.resolveKey("item.missile.desc.chipInaccuracy") + ": " + TextFormatting.GRAY + (Float)chip.attributes[0] * 100 + "%");

			if(stability != null)
				list.add(TextFormatting.BOLD + I18nUtil.resolveKey("item.missile.desc.finInaccuracy") + ": " + TextFormatting.GRAY + (Float)stability.attributes[0] * 100 + "%");
			else
				list.add(TextFormatting.BOLD + I18nUtil.resolveKey("item.missile.desc.finInaccuracy") + ": " + TextFormatting.GRAY + "100%");

			list.add(TextFormatting.BOLD + I18nUtil.resolveKey("item.missile.desc.size") + ": " + TextFormatting.GRAY + fuselage.getSize(fuselage.top) + "/" + fuselage.getSize(fuselage.bottom));

			float health = warhead.health + fuselage.health + thruster.health;
			if(stability != null)
				health += stability.health;

			list.add(TextFormatting.BOLD + I18nUtil.resolveKey("item.missile.desc.health") + ": " + TextFormatting.GRAY + health + "HP");

		} catch(Exception ex) {
			list.add(TextFormatting.RED + I18nUtil.resolveKey("error.generic"));
		}
	}

	public static MissileStruct getStruct(ItemStack stack) {

		if(stack == null || !(stack.getItem() instanceof ItemCustomMissile))
			return null;

		ItemCustomMissilePart warhead = readPart(stack, "warhead");
		ItemCustomMissilePart fuselage = readPart(stack, "fuselage");
		ItemCustomMissilePart thruster = readPart(stack, "thruster");

		if(warhead == null || fuselage == null || thruster == null)
			return null;

		MissileStruct missile = new MissileStruct(warhead, fuselage, readPart(stack, "stability"), thruster);

		return missile;
	}
}
