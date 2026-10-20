package com.hbm.handler;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.ClientConfig;
import com.hbm.items.ItemEnums.EnumIngotMetal;
import com.hbm.items.ItemEnums.EnumSecretType;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBattery;
import com.hbm.items.special.ItemBedrockOreNew;
import com.hbm.items.special.ItemBedrockOreNew.BedrockOreGrade;
import com.hbm.items.special.ItemBedrockOreNew.CelestialBedrockOre;
import com.hbm.items.special.ItemBedrockOreNew.CelestialBedrockOreType;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.factory.GunFactory.EnumAmmoSecret;
import com.hbm.main.MainRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** Shared item visibility policy, independent of either optional browser entry point. */
public final class RecipeBrowserVisibility {
    private RecipeBrowserVisibility() {}

    /** Both supported inventory browsers receive the same current visibility policy. */
    public static void configureHiddenItems(java.util.function.Consumer<ItemStack> hide) {
		if(ClientConfig.NEI_HIDE_SECRETS.get()) {
			for(Item item : ItemGunBaseNT.secrets) hide.accept(new ItemStack(item));
			for(int i = 0; i < EnumAmmoSecret.values().length; i++) hide.accept(new ItemStack(ModItems.ammo_secret, 1, i));
			for(int i = 0; i < EnumSecretType.values().length; i++) hide.accept(new ItemStack(ModItems.item_secret, 1, i));
		}

		for(int i = 0; i < EnumIngotMetal.values().length; i++) hide.accept(new ItemStack(ModItems.ingot_metal, 1, i));

		//Some things are even beyond my control...or are they?
		hide.accept(ItemBattery.getEmptyBattery(ModItems.memory));
		hide.accept(ItemBattery.getFullBattery(ModItems.memory));

		hide.accept(new ItemStack(ModBlocks.machine_electric_furnace_on));
		hide.accept(new ItemStack(ModBlocks.machine_difurnace_on));
		hide.accept(new ItemStack(ModBlocks.machine_rtg_furnace_on));
		hide.accept(new ItemStack(ModBlocks.reinforced_lamp_on));
		hide.accept(new ItemStack(ModBlocks.statue_elb_f));
		hide.accept(new ItemStack(ModBlocks.cheater_virus));
		hide.accept(new ItemStack(ModBlocks.cheater_virus_seed));
		hide.accept(new ItemStack(ModItems.euphemium_kit));
		hide.accept(new ItemStack(ModItems.bobmazon_hidden));
		hide.accept(new ItemStack(ModItems.book_lore)); //the broken nbt-less one shouldn't show up in normal play anyway
		if(MainRegistry.polaroidID != 11) {
			hide.accept(new ItemStack(ModItems.book_secret));
			hide.accept(new ItemStack(ModItems.book_of_));
			hide.accept(new ItemStack(ModItems.burnt_bark));
			hide.accept(new ItemStack(ModItems.ams_core_thingy));
		}
		hide.accept(new ItemStack(ModBlocks.dummy_block_blast));
		hide.accept(new ItemStack(ModBlocks.dummy_port_compact_launcher));
		hide.accept(new ItemStack(ModBlocks.dummy_port_launch_table));
		hide.accept(new ItemStack(ModBlocks.dummy_plate_compact_launcher));
		hide.accept(new ItemStack(ModBlocks.dummy_plate_launch_table));
		hide.accept(new ItemStack(ModBlocks.dummy_plate_cargo));

		hide.accept(new ItemStack(ModBlocks.pink_log));
		hide.accept(new ItemStack(ModBlocks.pink_planks));
		hide.accept(new ItemStack(ModBlocks.pink_slab));
		hide.accept(new ItemStack(ModBlocks.pink_double_slab));
		hide.accept(new ItemStack(ModBlocks.pink_stairs));

		hide.accept(new ItemStack(ModBlocks.spotlight_incandescent_off));
		hide.accept(new ItemStack(ModBlocks.spotlight_fluoro_off));
		hide.accept(new ItemStack(ModBlocks.spotlight_halogen_off));
		hide.accept(new ItemStack(ModBlocks.spotlight_beam));

		hide.accept(new ItemStack(ModItems.rocket_custom));
		hide.accept(new ItemStack(ModBlocks.orbital_station));

		hide.accept(new ItemStack(ModBlocks.conveyor));
		hide.accept(new ItemStack(ModBlocks.conveyor_chute));
		hide.accept(new ItemStack(ModBlocks.conveyor_lift));
		hide.accept(new ItemStack(ModBlocks.conveyor_express));
		hide.accept(new ItemStack(ModBlocks.conveyor_double));
		hide.accept(new ItemStack(ModBlocks.conveyor_triple));

		hide.accept(new ItemStack(ModBlocks.war_controller));
		hide.accept(new ItemStack(ModItems.sat_war));

		hide.accept(new ItemStack(ModBlocks.furnace));
		hide.accept(new ItemStack(ModBlocks.lit_furnace));

		for(BedrockOreGrade grade : BedrockOreGrade.values()) {
			if(grade == BedrockOreGrade.BASE) continue;
			for(CelestialBedrockOreType type : CelestialBedrockOre.getAllTypes()) {
				hide.accept(ItemBedrockOreNew.make(grade, type));
			}
		}

    }
}
