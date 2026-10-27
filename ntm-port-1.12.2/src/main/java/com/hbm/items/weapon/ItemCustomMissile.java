package com.hbm.items.weapon;

import java.util.List;

import com.hbm.handler.MissileStruct;
import com.hbm.items.ModItems;
import com.hbm.util.PersistentRegistryRef;
import com.hbm.util.i18n.I18nUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
		PersistentRegistryRef.writeItem(stack.getTagCompound(), key, item);
	}

	/**
	 * Reads a part written by buildMissile. Registry names are the only accepted
	 * persistent identity. A numeric-only tag is a 1.7.10 legacy value: there is
	 * no provable runtime mapping, so it is left untouched and reported as
	 * unresolved instead of being looked up in the current item registry.
	 */
	public static ItemCustomMissilePart readPart(ItemStack stack, String key) {

		if(stack == null || !stack.hasTagCompound())
			return null;

		Item item = PersistentRegistryRef.readItem(stack.getTagCompound(), key);
		return item instanceof ItemCustomMissilePart ? (ItemCustomMissilePart) item : null;
	}

	public static boolean hasUnresolvedLegacyPart(ItemStack stack, String key) {
		return stack != null && stack.hasTagCompound()
				&& PersistentRegistryRef.hasLegacyNumeric(stack.getTagCompound(), key);
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

		for(String key : new String[] { "chip", "warhead", "fuselage", "stability", "thruster" }) {
			if(hasUnresolvedLegacyPart(stack, key)) {
				list.add(TextFormatting.RED + "Legacy " + key + " id unresolved; raw NBT preserved");
			}
		}

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
