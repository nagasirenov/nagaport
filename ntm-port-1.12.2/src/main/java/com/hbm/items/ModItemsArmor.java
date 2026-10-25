package com.hbm.items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import com.hbm.render.item.NativeItemTextureRegistry;

import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.armor.*;
import com.hbm.items.armor.IArmorDisableModel.EnumPlayerPart;
import com.hbm.items.special.ItemDrop;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.potion.HbmPotion;
import com.hbm.util.ArmorUtil;

import static com.hbm.items.ModItems.*;

import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.common.util.EnumHelper;

public class ModItemsArmor {

	public static void init() {
		ArmorMaterial aMatRags = EnumHelper.addArmorMaterial("HBM_RAGS", "leather", 150, new int[] { 1, 1, 1, 1 }, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
		aMatRags.repairMaterial = new ItemStack(ModItems.rag, 1, OreDictionary.WILDCARD_VALUE);

		goggles = NativeItemTextureRegistry.texture(new ArmorModel(ArmorMaterial.IRON, 0).setTranslationKey("goggles").setMaxStackSize(1), RefStrings.MODID + ":goggles");
		ashglasses = NativeItemTextureRegistry.texture(new ArmorAshGlasses(ArmorMaterial.IRON, 0).setTranslationKey("ashglasses").setMaxStackSize(1), RefStrings.MODID + ":ashglasses");
		gas_mask = NativeItemTextureRegistry.texture(new ArmorGasMask().setTranslationKey("gas_mask").setMaxStackSize(1), RefStrings.MODID + ":gas_mask");
		gas_mask_m65 = NativeItemTextureRegistry.texture(new ArmorGasMask().setTranslationKey("gas_mask_m65").setMaxStackSize(1), RefStrings.MODID + ":gas_mask_m65");
		gas_mask_mono = NativeItemTextureRegistry.texture(new ArmorGasMask().setTranslationKey("gas_mask_mono").setMaxStackSize(1), RefStrings.MODID + ":gas_mask_mono");
		gas_mask_olde = NativeItemTextureRegistry.texture(new ArmorGasMask().setTranslationKey("gas_mask_olde").setMaxStackSize(1), RefStrings.MODID + ":gas_mask_olde");
		mask_rag = NativeItemTextureRegistry.texture(new ModArmor(aMatRags, 0).setTranslationKey("mask_rag").setMaxStackSize(1), RefStrings.MODID + ":mask_rag");
		mask_piss = NativeItemTextureRegistry.texture(new ModArmor(aMatRags, 0).setTranslationKey("mask_piss").setMaxStackSize(1), RefStrings.MODID + ":mask_piss");
		hat = NativeItemTextureRegistry.texture(new ArmorHat(MainRegistry.aMatAlloy, 0).setTranslationKey("nossy_hat").setMaxStackSize(1), RefStrings.MODID + ":hat");
		no9 = NativeItemTextureRegistry.texture(new ArmorNo9(MainRegistry.aMatSteel, 0).setTranslationKey("no9").setMaxStackSize(1), RefStrings.MODID + ":no9");
		beta = NativeItemTextureRegistry.texture(new ItemDrop().setTranslationKey("beta").setMaxStackSize(1), RefStrings.MODID + ":beta");

		euphemium_helmet = NativeItemTextureRegistry.texture(new ArmorEuphemium(MainRegistry.aMatEuph, 0).setTranslationKey("euphemium_helmet").setMaxStackSize(1), RefStrings.MODID + ":euphemium_helmet");
		euphemium_plate = NativeItemTextureRegistry.texture(new ArmorEuphemium(MainRegistry.aMatEuph, 1).setTranslationKey("euphemium_plate").setMaxStackSize(1), RefStrings.MODID + ":euphemium_plate");
		euphemium_legs = NativeItemTextureRegistry.texture(new ArmorEuphemium(MainRegistry.aMatEuph, 2).setTranslationKey("euphemium_legs").setMaxStackSize(1), RefStrings.MODID + ":euphemium_legs");
		euphemium_boots = NativeItemTextureRegistry.texture(new ArmorEuphemium(MainRegistry.aMatEuph, 3).setTranslationKey("euphemium_boots").setMaxStackSize(1), RefStrings.MODID + ":euphemium_boots");


		schrabidium_helmet = new ArmorFSB(MainRegistry.aMatSchrab, 0, RefStrings.MODID + ":textures/armor/schrabidium_1.png")
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.HASTE, 20, 2))
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.STRENGTH, 20, 2))
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.JUMP_BOOST, 20, 1))
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.SPEED, 20, 2))
				.setTranslationKey("schrabidium_helmet");
		schrabidium_plate = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatSchrab, 1, RefStrings.MODID + ":textures/armor/schrabidium_1.png").cloneStats((ArmorFSB) schrabidium_helmet).setTranslationKey("schrabidium_plate"), RefStrings.MODID + ":schrabidium_plate");
		schrabidium_legs = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatSchrab, 2, RefStrings.MODID + ":textures/armor/schrabidium_2.png").cloneStats((ArmorFSB) schrabidium_helmet).setTranslationKey("schrabidium_legs"), RefStrings.MODID + ":schrabidium_legs");
		schrabidium_boots = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatSchrab, 3, RefStrings.MODID + ":textures/armor/schrabidium_1.png").cloneStats((ArmorFSB) schrabidium_helmet).setTranslationKey("schrabidium_boots"), RefStrings.MODID + ":schrabidium_boots");
		bismuth_helmet = new ArmorBismuth(MainRegistry.aMatBismuth, 0, RefStrings.MODID + ":textures/armor/starmetal_1.png")
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.JUMP_BOOST, 20, 6))
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.SPEED, 20, 6))
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.REGENERATION, 20, 1))
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.NIGHT_VISION, 15 * 20, 0))
				.setDashCount(3)
				.setTranslationKey("bismuth_helmet");
		bismuth_plate = NativeItemTextureRegistry.texture(new ArmorBismuth(MainRegistry.aMatBismuth, 1, RefStrings.MODID + ":textures/armor/starmetal_2.png").cloneStats((ArmorFSB) bismuth_helmet).setTranslationKey("bismuth_plate"), RefStrings.MODID + ":bismuth_plate");
		bismuth_legs = NativeItemTextureRegistry.texture(new ArmorBismuth(MainRegistry.aMatBismuth, 2, RefStrings.MODID + ":textures/armor/starmetal_1.png").cloneStats((ArmorFSB) bismuth_helmet).setTranslationKey("bismuth_legs"), RefStrings.MODID + ":bismuth_legs");
		bismuth_boots = NativeItemTextureRegistry.texture(new ArmorBismuth(MainRegistry.aMatBismuth, 3, RefStrings.MODID + ":textures/armor/starmetal_1.png").cloneStats((ArmorFSB) bismuth_helmet).setTranslationKey("bismuth_boots"), RefStrings.MODID + ":bismuth_boots");
		titanium_helmet = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatTitan, 0, RefStrings.MODID + ":textures/armor/titanium_1.png").setTranslationKey("titanium_helmet").setMaxStackSize(1), RefStrings.MODID + ":titanium_helmet");
		titanium_plate = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatTitan, 1, RefStrings.MODID + ":textures/armor/titanium_1.png").cloneStats((ArmorFSB) titanium_helmet).setTranslationKey("titanium_plate").setMaxStackSize(1), RefStrings.MODID + ":titanium_plate");
		titanium_legs = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatTitan, 2, RefStrings.MODID + ":textures/armor/titanium_2.png").cloneStats((ArmorFSB) titanium_helmet).setTranslationKey("titanium_legs").setMaxStackSize(1), RefStrings.MODID + ":titanium_legs");
		titanium_boots = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatTitan, 3, RefStrings.MODID + ":textures/armor/titanium_1.png").cloneStats((ArmorFSB) titanium_helmet).setTranslationKey("titanium_boots"), RefStrings.MODID + ":titanium_boots");
		steel_helmet = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatSteel, 0, RefStrings.MODID + ":textures/armor/steel_1.png").setTranslationKey("steel_helmet"), RefStrings.MODID + ":steel_helmet");
		steel_plate = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatSteel, 1, RefStrings.MODID + ":textures/armor/steel_1.png").cloneStats((ArmorFSB) steel_helmet).setTranslationKey("steel_plate"), RefStrings.MODID + ":steel_plate");
		steel_legs = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatSteel, 2, RefStrings.MODID + ":textures/armor/steel_2.png").cloneStats((ArmorFSB) steel_helmet).setTranslationKey("steel_legs"), RefStrings.MODID + ":steel_legs");
		steel_boots = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatSteel, 3, RefStrings.MODID + ":textures/armor/steel_1.png").cloneStats((ArmorFSB) steel_helmet).setTranslationKey("steel_boots"), RefStrings.MODID + ":steel_boots");
		alloy_helmet = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatAlloy, 0, RefStrings.MODID + ":textures/armor/alloy_1.png").setTranslationKey("alloy_helmet"), RefStrings.MODID + ":alloy_helmet");
		alloy_plate = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatAlloy, 1, RefStrings.MODID + ":textures/armor/alloy_1.png").cloneStats((ArmorFSB) alloy_helmet).setTranslationKey("alloy_plate"), RefStrings.MODID + ":alloy_plate");
		alloy_legs = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatAlloy, 2, RefStrings.MODID + ":textures/armor/alloy_2.png").cloneStats((ArmorFSB) alloy_helmet).setTranslationKey("alloy_legs"), RefStrings.MODID + ":alloy_legs");
		alloy_boots = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatAlloy, 3, RefStrings.MODID + ":textures/armor/alloy_1.png").cloneStats((ArmorFSB) alloy_helmet).setTranslationKey("alloy_boots"), RefStrings.MODID + ":alloy_boots");
		cmb_helmet = new ArmorFSB(MainRegistry.aMatCMB, 0, RefStrings.MODID + ":textures/armor/cmb_1.png")
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.SPEED, 20, 2))
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.HASTE, 20, 2))
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.STRENGTH, 20, 4))
				.setTranslationKey("cmb_helmet");
		cmb_plate = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatCMB, 1, RefStrings.MODID + ":textures/armor/cmb_1.png").cloneStats((ArmorFSB) cmb_helmet).setTranslationKey("cmb_plate"), RefStrings.MODID + ":cmb_plate");
		cmb_legs = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatCMB, 2, RefStrings.MODID + ":textures/armor/cmb_2.png").cloneStats((ArmorFSB) cmb_helmet).setTranslationKey("cmb_legs"), RefStrings.MODID + ":cmb_legs");
		cmb_boots = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatCMB, 3, RefStrings.MODID + ":textures/armor/cmb_1.png").cloneStats((ArmorFSB) cmb_helmet).setTranslationKey("cmb_boots"), RefStrings.MODID + ":cmb_boots");
		paa_plate = new ArmorFSB(MainRegistry.aMatPaa, 1, RefStrings.MODID + ":textures/armor/paa_1.png").setNoHelmet(true)
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.HASTE, 20, 0)).setTranslationKey("paa_plate");
		paa_legs = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatPaa, 2, RefStrings.MODID + ":textures/armor/paa_2.png").cloneStats((ArmorFSB) paa_plate).setTranslationKey("paa_legs"), RefStrings.MODID + ":paa_legs");
		paa_boots = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatPaa, 3, RefStrings.MODID + ":textures/armor/paa_1.png").cloneStats((ArmorFSB) paa_plate).setTranslationKey("paa_boots"), RefStrings.MODID + ":paa_boots");
		asbestos_helmet = new ArmorFSB(MainRegistry.aMatAsbestos, 0, RefStrings.MODID + ":textures/armor/asbestos_1.png").setOverlay(RefStrings.MODID + ":textures/misc/overlay_asbestos.png").setTranslationKey("asbestos_helmet");
		asbestos_plate = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatAsbestos, 1, RefStrings.MODID + ":textures/armor/asbestos_1.png").setTranslationKey("asbestos_plate"), RefStrings.MODID + ":asbestos_plate");
		asbestos_legs = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatAsbestos, 2, RefStrings.MODID + ":textures/armor/asbestos_2.png").setTranslationKey("asbestos_legs"), RefStrings.MODID + ":asbestos_legs");
		asbestos_boots = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatAsbestos, 3, RefStrings.MODID + ":textures/armor/asbestos_1.png").setTranslationKey("asbestos_boots"), RefStrings.MODID + ":asbestos_boots");
		security_helmet = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatSecurity, 0, RefStrings.MODID + ":textures/armor/security_1.png").setTranslationKey("security_helmet"), RefStrings.MODID + ":security_helmet");
		security_plate = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatSecurity, 1, RefStrings.MODID + ":textures/armor/security_1.png").cloneStats((ArmorFSB) security_helmet).setTranslationKey("security_plate"), RefStrings.MODID + ":security_plate");
		security_legs = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatSecurity, 2, RefStrings.MODID + ":textures/armor/security_2.png").cloneStats((ArmorFSB) security_helmet).setTranslationKey("security_legs"), RefStrings.MODID + ":security_legs");
		security_boots = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatSecurity, 3, RefStrings.MODID + ":textures/armor/security_1.png").cloneStats((ArmorFSB) security_helmet).setTranslationKey("security_boots"), RefStrings.MODID + ":security_boots");
		cobalt_helmet = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatCobalt, 0, RefStrings.MODID + ":textures/armor/cobalt_1.png").setTranslationKey("cobalt_helmet"), RefStrings.MODID + ":cobalt_helmet");
		cobalt_plate = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatCobalt, 1, RefStrings.MODID + ":textures/armor/cobalt_1.png").cloneStats((ArmorFSB) cobalt_helmet).setTranslationKey("cobalt_plate"), RefStrings.MODID + ":cobalt_plate");
		cobalt_legs = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatCobalt, 2, RefStrings.MODID + ":textures/armor/cobalt_2.png").cloneStats((ArmorFSB) cobalt_helmet).setTranslationKey("cobalt_legs"), RefStrings.MODID + ":cobalt_legs");
		cobalt_boots = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatCobalt, 3, RefStrings.MODID + ":textures/armor/cobalt_1.png").cloneStats((ArmorFSB) cobalt_helmet).setTranslationKey("cobalt_boots"), RefStrings.MODID + ":cobalt_boots");
		starmetal_helmet = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatStarmetal, 0, RefStrings.MODID + ":textures/armor/starmetal_1.png")
				.setTranslationKey("starmetal_helmet"), RefStrings.MODID + ":starmetal_helmet");
		starmetal_plate = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatStarmetal, 1, RefStrings.MODID + ":textures/armor/starmetal_1.png").cloneStats((ArmorFSB) starmetal_helmet).setTranslationKey("starmetal_plate"), RefStrings.MODID + ":starmetal_plate");
		starmetal_legs = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatStarmetal, 2, RefStrings.MODID + ":textures/armor/starmetal_2.png").cloneStats((ArmorFSB) starmetal_helmet).setTranslationKey("starmetal_legs"), RefStrings.MODID + ":starmetal_legs");
		starmetal_boots = NativeItemTextureRegistry.texture(new ArmorFSB(MainRegistry.aMatStarmetal, 3, RefStrings.MODID + ":textures/armor/starmetal_1.png").cloneStats((ArmorFSB) starmetal_helmet).setTranslationKey("starmetal_boots"), RefStrings.MODID + ":starmetal_boots");

		robes_helmet = NativeItemTextureRegistry.texture(new ArmorFSB(ArmorMaterial.CHAIN, 0, RefStrings.MODID + ":textures/armor/robes_1.png").setTranslationKey("robes_helmet"), RefStrings.MODID + ":robes_helmet");
		robes_plate = NativeItemTextureRegistry.texture(new ArmorFSB(ArmorMaterial.CHAIN, 1, RefStrings.MODID + ":textures/armor/robes_1.png").cloneStats((ArmorFSB) robes_helmet).setTranslationKey("robes_plate"), RefStrings.MODID + ":robes_plate");
		robes_legs = NativeItemTextureRegistry.texture(new ArmorFSB(ArmorMaterial.CHAIN, 2, RefStrings.MODID + ":textures/armor/robes_2.png").cloneStats((ArmorFSB) robes_helmet).setTranslationKey("robes_legs"), RefStrings.MODID + ":robes_legs");
		robes_boots = NativeItemTextureRegistry.texture(new ArmorFSB(ArmorMaterial.CHAIN, 3, RefStrings.MODID + ":textures/armor/robes_1.png").cloneStats((ArmorFSB) robes_helmet).setTranslationKey("robes_boots"), RefStrings.MODID + ":robes_boots");


		ArmorMaterial aMatZirconium = EnumHelper.addArmorMaterial("HBM_ZIRCONIUM", "leather", 1000, new int[] { 1, 3, 5, 2 }, 1000, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
		aMatZirconium.repairMaterial = new ItemStack(ModItems.ingot_zirconium, 1, OreDictionary.WILDCARD_VALUE);
		zirconium_legs = NativeItemTextureRegistry.texture(new ArmorFSB(aMatZirconium, 2, RefStrings.MODID + ":textures/armor/zirconium_2.png").setTranslationKey("zirconium_legs"), RefStrings.MODID + ":zirconium_legs");

		ArmorMaterial aMatDNT = EnumHelper.addArmorMaterial("HBM_DNT_LOLOLOL", "leather", 3, new int[] { 1, 1, 1, 1 }, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
		aMatDNT.repairMaterial = new ItemStack(ModItems.ingot_dineutronium, 1, OreDictionary.WILDCARD_VALUE);
		dnt_helmet = NativeItemTextureRegistry.texture(new ArmorFSB(aMatDNT, 0, RefStrings.MODID + ":textures/armor/dnt_1.png")
				.setTranslationKey("dnt_helmet"), RefStrings.MODID + ":dnt_helmet");
		dnt_plate = NativeItemTextureRegistry.texture(new ArmorFSB(aMatDNT, 1, RefStrings.MODID + ":textures/armor/dnt_1.png").cloneStats((ArmorFSB) dnt_helmet).setTranslationKey("dnt_plate"), RefStrings.MODID + ":dnt_plate");
		dnt_legs = NativeItemTextureRegistry.texture(new ArmorFSB(aMatDNT, 2, RefStrings.MODID + ":textures/armor/dnt_2.png").cloneStats((ArmorFSB) dnt_helmet).setTranslationKey("dnt_legs"), RefStrings.MODID + ":dnt_legs");
		dnt_boots = NativeItemTextureRegistry.texture(new ArmorFSB(aMatDNT, 3, RefStrings.MODID + ":textures/armor/dnt_1.png").cloneStats((ArmorFSB) dnt_helmet).setTranslationKey("dnt_boots"), RefStrings.MODID + ":dnt_boots");

		ArmorMaterial aMatT51 = EnumHelper.addArmorMaterial("HBM_T51", "leather", 150, new int[] { 3, 6, 8, 3 }, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
		aMatT51.repairMaterial = new ItemStack(ModItems.plate_armor_titanium, 1, OreDictionary.WILDCARD_VALUE);
		t51_helmet = new ArmorT51(aMatT51, 0, RefStrings.MODID + ":textures/armor/starmetal_1.png", 1000000, 10000, 1000, 5)
				.enableVATS(true)
				.setHasGeigerSound(true)
				.setHasHardLanding(true)
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.STRENGTH, 20, 0))
				.setStep("hbm:step.metal").setJump("hbm:step.iron_jump").setFall("hbm:step.iron_land")
				.setSealed(true)
				.hides(EnumPlayerPart.HAT)
				.setHazardClass(ArmorUtil.FULL_NO_LIGHT).setRadResist(1D /*90%*/)
				.setTranslationKey("t51_helmet").setMaxStackSize(1);
		t51_plate = NativeItemTextureRegistry.texture(new ArmorT51(aMatT51, 1, RefStrings.MODID + ":textures/armor/starmetal_1.png", 1000000, 10000, 1000, 5).cloneStats((ArmorFSB) t51_helmet).setTranslationKey("t51_plate").setMaxStackSize(1), RefStrings.MODID + ":armor");
		t51_legs = NativeItemTextureRegistry.texture(new ArmorT51(aMatT51, 2, RefStrings.MODID + ":textures/armor/starmetal_1.png", 1000000, 10000, 1000, 5).cloneStats((ArmorFSB) t51_helmet).setTranslationKey("t51_legs").setMaxStackSize(1), RefStrings.MODID + ":armor");
		t51_boots = NativeItemTextureRegistry.texture(new ArmorT51(aMatT51, 3, RefStrings.MODID + ":textures/armor/starmetal_1.png", 1000000, 10000, 1000, 5).cloneStats((ArmorFSB) t51_helmet).setTranslationKey("t51_boots").setMaxStackSize(1), RefStrings.MODID + ":armor");

		ArmorMaterial aMatDesh = EnumHelper.addArmorMaterial("HBM_DESH", "leather", 150, new int[] { 3, 6, 8, 3 }, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
		aMatDesh.repairMaterial = new ItemStack(ModItems.ingot_desh, 1, OreDictionary.WILDCARD_VALUE);
		steamsuit_helmet = new ArmorDesh(aMatDesh, 0, RefStrings.MODID + ":textures/armor/starmetal_1.png", Fluids.STEAM, 64_000, 500, 50, 1)
				.setHasHardLanding(true)
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.HASTE, 20, 4))
				.setSealed(true)
				.hides(EnumPlayerPart.HAT)
				.setHazardClass(ArmorUtil.FULL_PACKAGE).setRadResist(1.3D /*95%*/)
				.setTranslationKey("steamsuit_helmet").setMaxStackSize(1);
		steamsuit_plate = NativeItemTextureRegistry.texture(new ArmorDesh(aMatDesh, 1, RefStrings.MODID + ":textures/armor/starmetal_1.png", Fluids.STEAM, 64_000, 500, 50, 1).cloneStats((ArmorFSB) steamsuit_helmet).setTranslationKey("steamsuit_plate").setMaxStackSize(1), RefStrings.MODID + ":steamsuit_plate");
		steamsuit_legs = NativeItemTextureRegistry.texture(new ArmorDesh(aMatDesh, 2, RefStrings.MODID + ":textures/armor/starmetal_2.png", Fluids.STEAM, 64_000, 500, 50, 1).cloneStats((ArmorFSB) steamsuit_helmet).setTranslationKey("steamsuit_legs").setMaxStackSize(1), RefStrings.MODID + ":steamsuit_legs");
		steamsuit_boots = NativeItemTextureRegistry.texture(new ArmorDesh(aMatDesh, 3, RefStrings.MODID + ":textures/armor/starmetal_1.png", Fluids.STEAM, 64_000, 500, 50, 1).cloneStats((ArmorFSB) steamsuit_helmet).setTranslationKey("steamsuit_boots").setMaxStackSize(1), RefStrings.MODID + ":steamsuit_boots");

		ArmorMaterial aMatDiesel = EnumHelper.addArmorMaterial("HBM_BNUUY", "leather", 150, new int[] { 3, 6, 8, 3 }, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
		aMatDiesel.repairMaterial = new ItemStack(ModItems.plate_copper, 1, OreDictionary.WILDCARD_VALUE);
		dieselsuit_helmet = new ArmorDiesel(aMatDiesel, 0, RefStrings.MODID + ":textures/armor/starmetal_1.png", Fluids.DIESEL, 64_000, 500, 50, 1)
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.SPEED, 20, 2))
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.JUMP_BOOST, 20, 2))
				.enableThermalSight(true)
				.enableVATS(true)
				.setTranslationKey("dieselsuit_helmet").setMaxStackSize(1);
		dieselsuit_plate = NativeItemTextureRegistry.texture(new ArmorDiesel(aMatDiesel, 1, RefStrings.MODID + ":textures/armor/starmetal_1.png", Fluids.DIESEL, 64_000, 500, 50, 1).cloneStats((ArmorFSB) dieselsuit_helmet).setTranslationKey("dieselsuit_plate").setMaxStackSize(1), RefStrings.MODID + ":dieselsuit_plate");
		dieselsuit_legs = NativeItemTextureRegistry.texture(new ArmorDiesel(aMatDiesel, 2, RefStrings.MODID + ":textures/armor/starmetal_2.png", Fluids.DIESEL, 64_000, 500, 50, 1).cloneStats((ArmorFSB) dieselsuit_helmet).setTranslationKey("dieselsuit_legs").setMaxStackSize(1), RefStrings.MODID + ":dieselsuit_legs");
		dieselsuit_boots = NativeItemTextureRegistry.texture(new ArmorDiesel(aMatDiesel, 3, RefStrings.MODID + ":textures/armor/starmetal_1.png", Fluids.DIESEL, 64_000, 500, 50, 1).cloneStats((ArmorFSB) dieselsuit_helmet).setTranslationKey("dieselsuit_boots").setMaxStackSize(1), RefStrings.MODID + ":dieselsuit_boots");

		ArmorMaterial aMatAJR = EnumHelper.addArmorMaterial("HBM_T45AJR", "leather", 150, new int[] { 3, 6, 8, 3 }, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
		aMatAJR.repairMaterial = new ItemStack(ModItems.plate_armor_ajr, 1, OreDictionary.WILDCARD_VALUE);
		ajr_helmet = new ArmorAJR(aMatAJR, 0, RefStrings.MODID + ":textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25)
				.enableVATS(true)
				.setHasGeigerSound(true)
				.setHasHardLanding(true)
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.JUMP_BOOST, 20, 0))
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.STRENGTH, 20, 0))
				.setStep("hbm:step.metal").setJump("hbm:step.iron_jump").setFall("hbm:step.iron_land")
				.setSealed(true)
				.hides(EnumPlayerPart.HAT)
				.setHazardClass(ArmorUtil.FULL_PACKAGE).setRadResist(1.3D /*95%*/)
				.setTranslationKey("ajr_helmet");
		ajr_plate = NativeItemTextureRegistry.texture(new ArmorAJR(aMatAJR, 1, RefStrings.MODID + ":textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25).cloneStats((ArmorFSB) ajr_helmet).setTranslationKey("ajr_plate"), RefStrings.MODID + ":ajr_plate");
		ajr_legs = NativeItemTextureRegistry.texture(new ArmorAJR(aMatAJR, 2, RefStrings.MODID + ":textures/armor/starmetal_2.png", 2500000, 10000, 2000, 25).cloneStats((ArmorFSB) ajr_helmet).setTranslationKey("ajr_legs"), RefStrings.MODID + ":ajr_legs");
		ajr_boots = NativeItemTextureRegistry.texture(new ArmorAJR(aMatAJR, 3, RefStrings.MODID + ":textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25).cloneStats((ArmorFSB) ajr_helmet).setTranslationKey("ajr_boots"), RefStrings.MODID + ":ajr_boots");

		ajro_helmet = new ArmorAJRO(aMatAJR, 0, RefStrings.MODID + ":textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25)
				.enableVATS(true)
				.setHasGeigerSound(true)
				.setHasHardLanding(true)
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.JUMP_BOOST, 20, 0))
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.STRENGTH, 20, 0))
				.setStep("hbm:step.metal").setJump("hbm:step.iron_jump").setFall("hbm:step.iron_land")
				.setSealed(true)
				.hides(EnumPlayerPart.HAT)
				.setHazardClass(ArmorUtil.FULL_PACKAGE).setRadResist(1.3D /*95%*/)
				.setTranslationKey("ajro_helmet");
		ajro_plate = NativeItemTextureRegistry.texture(new ArmorAJRO(aMatAJR, 1, RefStrings.MODID + ":textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25).cloneStats((ArmorFSB) ajro_helmet).setTranslationKey("ajro_plate"), RefStrings.MODID + ":ajro_plate");
		ajro_legs = NativeItemTextureRegistry.texture(new ArmorAJRO(aMatAJR, 2, RefStrings.MODID + ":textures/armor/starmetal_2.png", 2500000, 10000, 2000, 25).cloneStats((ArmorFSB) ajro_helmet).setTranslationKey("ajro_legs"), RefStrings.MODID + ":ajro_legs");
		ajro_boots = NativeItemTextureRegistry.texture(new ArmorAJRO(aMatAJR, 3, RefStrings.MODID + ":textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25).cloneStats((ArmorFSB) ajro_helmet).setTranslationKey("ajro_boots"), RefStrings.MODID + ":ajro_boots");

		rpa_helmet = new ArmorRPA(aMatAJR, 0, RefStrings.MODID + ":textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25)
				.enableVATS(true)
				.setHasGeigerSound(true)
				.setHasHardLanding(true)
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.STRENGTH, 20, 3))
				.setStep("hbm:step.powered")
				.setJump("hbm:step.powered")
				.setFall("hbm:step.powered")
				.setSealed(true)
				.hides(EnumPlayerPart.HAT)
				.setHazardClass(ArmorUtil.FULL_PACKAGE).setRadResist(2D /*99%*/)
				.setTranslationKey("rpa_helmet");
		rpa_plate = NativeItemTextureRegistry.texture(new ArmorRPA(aMatAJR, 1, RefStrings.MODID + ":textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25).cloneStats((ArmorFSB) rpa_helmet).setTranslationKey("rpa_plate"), RefStrings.MODID + ":rpa_plate");
		rpa_legs = NativeItemTextureRegistry.texture(new ArmorRPA(aMatAJR, 2, RefStrings.MODID + ":textures/armor/starmetal_2.png", 2500000, 10000, 2000, 25).cloneStats((ArmorFSB) rpa_helmet).setTranslationKey("rpa_legs"), RefStrings.MODID + ":rpa_legs");
		rpa_boots = NativeItemTextureRegistry.texture(new ArmorRPA(aMatAJR, 3, RefStrings.MODID + ":textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25).cloneStats((ArmorFSB) rpa_helmet).setTranslationKey("rpa_boots"), RefStrings.MODID + ":rpa_boots");

		ncrpa_helmet = new ArmorNCRPA(aMatAJR, 0, RefStrings.MODID + ":textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25)
				.enableVATS(true)
				.setHasGeigerSound(true)
				.setHasHardLanding(true)
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.STRENGTH, 20, 3))
				.setStep("hbm:step.powered")
				.setJump("hbm:step.powered")
				.setFall("hbm:step.powered")
				.setSealed(true)
				.hides(EnumPlayerPart.HAT)
				.setHazardClass(ArmorUtil.FULL_PACKAGE).setRadResist(1.7D /*97%*/)
				.setTranslationKey("ncrpa_helmet");
		ncrpa_plate = NativeItemTextureRegistry.texture(new ArmorNCRPA(aMatAJR, 1, RefStrings.MODID + ":textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25).cloneStats((ArmorFSB) ncrpa_helmet).setTranslationKey("ncrpa_plate"), RefStrings.MODID + ":rpa_plate");
		ncrpa_legs = NativeItemTextureRegistry.texture(new ArmorNCRPA(aMatAJR, 2, RefStrings.MODID + ":textures/armor/starmetal_2.png", 2500000, 10000, 2000, 25).cloneStats((ArmorFSB) ncrpa_helmet).setTranslationKey("ncrpa_legs"), RefStrings.MODID + ":rpa_legs");
		ncrpa_boots = NativeItemTextureRegistry.texture(new ArmorNCRPA(aMatAJR, 3, RefStrings.MODID + ":textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25).cloneStats((ArmorFSB) ncrpa_helmet).setTranslationKey("ncrpa_boots"), RefStrings.MODID + ":rpa_boots");

		ArmorMaterial aMatBJ = EnumHelper.addArmorMaterial("HBM_BLACKJACK", "leather", 150, new int[] { 3, 6, 8, 3 }, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
		aMatBJ.repairMaterial = new ItemStack(ModItems.plate_armor_lunar, 1, OreDictionary.WILDCARD_VALUE);
		bj_helmet = new ArmorBJ(aMatBJ, 0, RefStrings.MODID + ":textures/armor/starmetal_1.png", 10000000, 10000, 1000, 100)
				.enableVATS(true)
				.enableThermalSight(true)
				.setHasGeigerSound(true)
				.setHasHardLanding(true)
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.SPEED, 20, 1))
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.JUMP_BOOST, 20, 0))
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.SATURATION, 20, 0))
				.addEffect(new PotionEffect(HbmPotion.radx, 20, 0))
				.setStep("hbm:step.metal")
				.setJump("hbm:step.iron_jump")
				.setFall("hbm:step.iron_land")
				.setRadResist(1D /*90%*/)
				.setTranslationKey("bj_helmet");
		bj_plate = NativeItemTextureRegistry.texture(new ArmorBJ(aMatBJ, 1, RefStrings.MODID + ":textures/armor/starmetal_1.png", 10000000, 10000, 1000, 100).cloneStats((ArmorFSB) bj_helmet).setTranslationKey("bj_plate"), RefStrings.MODID + ":bj_plate");
		bj_plate_jetpack = NativeItemTextureRegistry.texture(new ArmorBJJetpack(aMatBJ, 1, RefStrings.MODID + ":textures/armor/starmetal_1.png", 10000000, 10000, 1000, 100).cloneStats((ArmorFSB) bj_helmet).setTranslationKey("bj_plate_jetpack"), RefStrings.MODID + ":bj_plate_jetpack");
		bj_legs = NativeItemTextureRegistry.texture(new ArmorBJ(aMatBJ, 2, RefStrings.MODID + ":textures/armor/starmetal_2.png", 10000000, 10000, 1000, 100).cloneStats((ArmorFSB) bj_helmet).setTranslationKey("bj_legs"), RefStrings.MODID + ":bj_legs");
		bj_boots = NativeItemTextureRegistry.texture(new ArmorBJ(aMatBJ, 3, RefStrings.MODID + ":textures/armor/starmetal_1.png", 10000000, 10000, 1000, 100).cloneStats((ArmorFSB) bj_helmet).setTranslationKey("bj_boots"), RefStrings.MODID + ":bj_boots");

		ArmorMaterial aMatEnv = EnumHelper.addArmorMaterial("HBM_ENV", "leather", 150, new int[] { 3, 6, 8, 3 }, 10, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
		aMatEnv.repairMaterial = new ItemStack(ModItems.plate_armor_hev, 1, OreDictionary.WILDCARD_VALUE);
		envsuit_helmet = new ArmorEnvsuit(aMatEnv, 0, RefStrings.MODID + ":textures/armor/starmetal_1.png", 100_000, 1_000, 250, 0)
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.SPEED, 20, 1))
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.JUMP_BOOST, 20, 0))
				.setSealed(true)
				.hides(EnumPlayerPart.HAT)
				.setHazardClass(ArmorUtil.FULL_PACKAGE).setRadResist(1.0D /*90%*/)
				.setTranslationKey("envsuit_helmet");
		envsuit_plate = NativeItemTextureRegistry.texture(new ArmorEnvsuit(aMatEnv, 1, RefStrings.MODID + ":textures/armor/starmetal_1.png", 100_000, 1_000, 250, 0).cloneStats((ArmorFSB) envsuit_helmet).setTranslationKey("envsuit_plate"), RefStrings.MODID + ":envsuit_plate");
		envsuit_legs = NativeItemTextureRegistry.texture(new ArmorEnvsuit(aMatEnv, 2, RefStrings.MODID + ":textures/armor/starmetal_2.png", 100_000, 1_000, 250, 0).cloneStats((ArmorFSB) envsuit_helmet).setTranslationKey("envsuit_legs"), RefStrings.MODID + ":envsuit_legs");
		envsuit_boots = NativeItemTextureRegistry.texture(new ArmorEnvsuit(aMatEnv, 3, RefStrings.MODID + ":textures/armor/starmetal_1.png", 100_000, 1_000, 250, 0).cloneStats((ArmorFSB) envsuit_helmet).setTranslationKey("envsuit_boots"), RefStrings.MODID + ":envsuit_boots");

		ArmorMaterial aMatHEV = EnumHelper.addArmorMaterial("HBM_HEV", "leather", 150, new int[] { 3, 6, 8, 3 }, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
		aMatHEV.repairMaterial = new ItemStack(ModItems.plate_armor_hev, 1, OreDictionary.WILDCARD_VALUE);
		hev_helmet = new ArmorHEV(aMatHEV, 0, RefStrings.MODID + ":textures/armor/starmetal_1.png", 1000000, 10000, 2500, 0)
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.SPEED, 20, 1))
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.JUMP_BOOST, 20, 0))
				.setHasGeigerSound(true)
				.setHasCustomGeiger(true)
				.setSealed(true)
				.hides(EnumPlayerPart.HAT)
				.setHazardClass(ArmorUtil.FULL_PACKAGE).setRadResist(2.3D /*99.5%*/)
				.setTranslationKey("hev_helmet");
		hev_plate = NativeItemTextureRegistry.texture(new ArmorHEV(aMatHEV, 1, RefStrings.MODID + ":textures/armor/starmetal_1.png", 1000000, 10000, 2500, 0).cloneStats((ArmorFSB) hev_helmet).setTranslationKey("hev_plate"), RefStrings.MODID + ":hev_plate");
		hev_legs = NativeItemTextureRegistry.texture(new ArmorHEV(aMatHEV, 2, RefStrings.MODID + ":textures/armor/starmetal_2.png", 1000000, 10000, 2500, 0).cloneStats((ArmorFSB) hev_helmet).setTranslationKey("hev_legs"), RefStrings.MODID + ":hev_legs");
		hev_boots = NativeItemTextureRegistry.texture(new ArmorHEV(aMatHEV, 3, RefStrings.MODID + ":textures/armor/starmetal_1.png", 1000000, 10000, 2500, 0).cloneStats((ArmorFSB) hev_helmet).setTranslationKey("hev_boots"), RefStrings.MODID + ":hev_boots");

		jackt = NativeItemTextureRegistry.texture(new ModArmor(MainRegistry.aMatSteel, 1).setTranslationKey("jackt"), RefStrings.MODID + ":jackt");
		jackt2 = NativeItemTextureRegistry.texture(new ModArmor(MainRegistry.aMatSteel, 1).setTranslationKey("jackt2"), RefStrings.MODID + ":jackt2");

		ArmorMaterial aMatFau = EnumHelper.addArmorMaterial("HBM_DIGAMMA", "leather", 150, new int[] { 3, 6, 8, 3 }, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
		aMatFau.repairMaterial = new ItemStack(ModItems.plate_armor_fau, 1, OreDictionary.WILDCARD_VALUE);
		fau_helmet = new ArmorDigamma(aMatFau, 0, RefStrings.MODID + ":textures/armor/starmetal_1.png", 10000000, 10000, 2500, 0)
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.JUMP_BOOST, 20, 1))
				.setHasGeigerSound(true)
				.enableThermalSight(true)
				.setHasHardLanding(true)
				.setStep("hbm:step.metal")
				.setJump("hbm:step.iron_jump")
				.setFall("hbm:step.iron_land")
				.setSealed(true)
				.hides(EnumPlayerPart.HAT)
				.setHazardClass(ArmorUtil.FULL_PACKAGE).setRadResist(4D /*99.99%*/)
				.setTranslationKey("fau_helmet");
		fau_plate = new ArmorDigamma(aMatFau, 1, RefStrings.MODID + ":textures/armor/starmetal_1.png", 10000000, 10000, 2500, 0).cloneStats((ArmorFSB) fau_helmet).setFullSetForHide().setTranslationKey("fau_plate");
		fau_legs = new ArmorDigamma(aMatFau, 2, RefStrings.MODID + ":textures/armor/starmetal_2.png", 10000000, 10000, 2500, 0).cloneStats((ArmorFSB) fau_helmet).hides(EnumPlayerPart.LEFT_LEG, EnumPlayerPart.RIGHT_LEG).setFullSetForHide().setTranslationKey("fau_legs");
		fau_boots = NativeItemTextureRegistry.texture(new ArmorDigamma(aMatFau, 3, RefStrings.MODID + ":textures/armor/starmetal_1.png", 10000000, 10000, 2500, 0).cloneStats((ArmorFSB) fau_helmet).setTranslationKey("fau_boots"), RefStrings.MODID + ":fau_boots");

		ArmorMaterial aMatDNS = EnumHelper.addArmorMaterial("HBM_DNT_NANO", "leather", 150, new int[] { 3, 6, 8, 3 }, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
		aMatDNS.repairMaterial = new ItemStack(ModItems.plate_armor_dnt, 1, OreDictionary.WILDCARD_VALUE);
		dns_helmet = new ArmorDNT(aMatDNS, 0, RefStrings.MODID + ":textures/armor/starmetal_1.png", 1000000000, 1000000, 100000, 115)
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.STRENGTH, 20, 9))
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.HASTE, 20, 7))
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.JUMP_BOOST, 20, 2))
				.setHasGeigerSound(true)
				.enableVATS(true)
				.enableThermalSight(true)
				.setHasHardLanding(true)
				.setStep("hbm:step.metal")
				.setJump("hbm:step.iron_jump")
				.setFall("hbm:step.iron_land")
				.setSealed(true)
				.hides(EnumPlayerPart.HAT)
				.setHazardClass(ArmorUtil.FULL_PACKAGE).setRadResist(5D /*99.999%*/)
				.setTranslationKey("dns_helmet");
		dns_plate = NativeItemTextureRegistry.texture(new ArmorDNT(aMatDNS, 1, RefStrings.MODID + ":textures/armor/starmetal_1.png", 1000000000, 1000000, 100000, 115).cloneStats((ArmorFSB) dns_helmet).setTranslationKey("dns_plate"), RefStrings.MODID + ":dns_plate");
		dns_legs = NativeItemTextureRegistry.texture(new ArmorDNT(aMatDNS, 2, RefStrings.MODID + ":textures/armor/starmetal_2.png", 1000000000, 1000000, 100000, 115).cloneStats((ArmorFSB) dns_helmet).setTranslationKey("dns_legs"), RefStrings.MODID + ":dns_legs");
		dns_boots = NativeItemTextureRegistry.texture(new ArmorDNT(aMatDNS, 3, RefStrings.MODID + ":textures/armor/starmetal_1.png", 1000000000, 1000000, 100000, 115).cloneStats((ArmorFSB) dns_helmet).setTranslationKey("dns_boots"), RefStrings.MODID + ":dns_boots");

		ArmorMaterial aMatTaurun = EnumHelper.addArmorMaterial("HBM_TRENCH", "leather", 150, new int[] { 3, 6, 8, 3 }, 10, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
		aMatTaurun.repairMaterial = new ItemStack(ModItems.plate_iron, 1, OreDictionary.WILDCARD_VALUE);
		taurun_helmet = new ArmorTaurun(aMatTaurun, 0, RefStrings.MODID + ":textures/armor/starmetal_1.png")
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.STRENGTH, 20, 0))
				.setStepSize(1)
				.setSealed(true)
				.hides(EnumPlayerPart.HAT)
				.setHazardClass(ArmorUtil.FULL_PACKAGE).setRadResist(0.125D /*25%*/)
				.setTranslationKey("taurun_helmet");
		taurun_plate = NativeItemTextureRegistry.texture(new ArmorTaurun(aMatTaurun, 1, RefStrings.MODID + ":textures/armor/starmetal_1.png").cloneStats((ArmorFSB) taurun_helmet).setTranslationKey("taurun_plate"), RefStrings.MODID + ":taurun_plate");
		taurun_legs = NativeItemTextureRegistry.texture(new ArmorTaurun(aMatTaurun, 2, RefStrings.MODID + ":textures/armor/starmetal_2.png").cloneStats((ArmorFSB) taurun_helmet).setTranslationKey("taurun_legs"), RefStrings.MODID + ":taurun_legs");
		taurun_boots = NativeItemTextureRegistry.texture(new ArmorTaurun(aMatTaurun, 3, RefStrings.MODID + ":textures/armor/starmetal_1.png").cloneStats((ArmorFSB) taurun_helmet).setTranslationKey("taurun_boots"), RefStrings.MODID + ":taurun_boots");
		ArmorMaterial aMatTrench = EnumHelper.addArmorMaterial("HBM_TRENCH", "leather", 150, new int[] { 3, 6, 8, 3 }, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
		aMatTrench.repairMaterial = new ItemStack(ModItems.plate_iron, 1, OreDictionary.WILDCARD_VALUE);
		trenchmaster_helmet = new ArmorTrenchmaster(aMatTrench, 0, RefStrings.MODID + ":textures/armor/starmetal_1.png")
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.STRENGTH, 20, 2))
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.HASTE, 20, 1))
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.JUMP_BOOST, 20, 1))
				.addEffect(new PotionEffect(net.minecraft.init.MobEffects.SPEED, 20, 0))
				.enableVATS(true)
				.setStepSize(1)
				.setSealed(true)
				.hides(EnumPlayerPart.HAT)
				.setRadResist(1D /*90%*/)
				.setHazardClass(ArmorUtil.FULL_PACKAGE).setTranslationKey("trenchmaster_helmet");
		trenchmaster_plate = NativeItemTextureRegistry.texture(new ArmorTrenchmaster(aMatTrench, 1, RefStrings.MODID + ":textures/armor/starmetal_1.png").cloneStats((ArmorFSB) trenchmaster_helmet).setTranslationKey("trenchmaster_plate"), RefStrings.MODID + ":trenchmaster_plate");
		trenchmaster_legs = NativeItemTextureRegistry.texture(new ArmorTrenchmaster(aMatTrench, 2, RefStrings.MODID + ":textures/armor/starmetal_2.png").cloneStats((ArmorFSB) trenchmaster_helmet).setTranslationKey("trenchmaster_legs"), RefStrings.MODID + ":trenchmaster_legs");
		trenchmaster_boots = NativeItemTextureRegistry.texture(new ArmorTrenchmaster(aMatTrench, 3, RefStrings.MODID + ":textures/armor/starmetal_1.png").cloneStats((ArmorFSB) trenchmaster_helmet).setTranslationKey("trenchmaster_boots"), RefStrings.MODID + ":trenchmaster_boots");

		jackt = NativeItemTextureRegistry.texture(new ModArmor(MainRegistry.aMatSteel, 1).setTranslationKey("jackt"), RefStrings.MODID + ":jackt");
		jackt2 = NativeItemTextureRegistry.texture(new ModArmor(MainRegistry.aMatSteel, 1).setTranslationKey("jackt2"), RefStrings.MODID + ":jackt2");
	}
}
