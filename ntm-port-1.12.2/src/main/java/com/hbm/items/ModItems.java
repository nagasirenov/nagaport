package com.hbm.items;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.oredict.OreDictionary;

import java.util.HashSet;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.VersatileConfig;
import com.hbm.handler.BucketHandler;
import com.hbm.handler.ability.IToolAreaAbility;
import com.hbm.handler.ability.IToolHarvestAbility;
import com.hbm.handler.ability.IWeaponAbility;
import com.hbm.interfaces.HalfLifeType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ItemAmmoEnums.*;
import com.hbm.items.ItemEnums.*;
import com.hbm.items.armor.*;
import com.hbm.items.bomb.*;
import com.hbm.items.food.*;
import com.hbm.items.machine.*;
import com.hbm.items.machine.ItemFELCrystal.EnumWavelengths;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.items.machine.ItemPlateFuel.FunctionEnum;
import com.hbm.items.machine.ItemPWRFuel.EnumPWRFuel;
import com.hbm.items.machine.ItemRBMKRod.EnumBurnFunc;
import com.hbm.items.machine.ItemRBMKRod.EnumDepleteFunc;
import com.hbm.items.machine.ItemRTGPelletDepleted.DepletedRTGMaterial;
import com.hbm.items.machine.ItemStamp.StampType;
import com.hbm.items.special.*;
import com.hbm.items.special.ItemPlasticScrap.ScrapType;
import com.hbm.items.tool.*;
import com.hbm.items.tool.ItemToolAbility.EnumToolType;
import com.hbm.items.weapon.*;
import com.hbm.items.weapon.ItemCustomMissilePart.*;
import com.hbm.items.weapon.ItemMissile.MissileFormFactor;
import com.hbm.items.weapon.ItemMissile.MissileFuel;
import com.hbm.items.weapon.ItemMissile.MissileTier;
import com.hbm.items.weapon.sedna.factory.GunFactory;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.rbmk.IRBMKFluxReceiver.NType;
import com.hbm.util.RTGUtil;
import com.hbm.registry.GameRegistry;
import com.hbm.render.item.NativeItemTextureRegistry;

import api.hbm.block.IToolable.ToolType;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemSeedFood;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;

public class ModItems {

	public static HashSet<Item> excludeNEI = new HashSet();

	public static void mainRegistry() {
		initializeItem();
		registerItem();
	}

	public static Item redstone_sword;
	public static Item big_sword;
	public static Item butter_sword;

	public static Item ingot_th232;
	public static Item ingot_uranium;
	public static Item ingot_u233;
	public static Item ingot_u235;
	public static Item ingot_u238;
	public static Item ingot_u238m2;
	public static Item ingot_plutonium;
	public static Item ingot_pu238;
	public static Item ingot_pu239;
	public static Item ingot_pu240;
	public static Item ingot_pu241;
	public static Item ingot_pu_mix;
	public static Item ingot_am241;
	public static Item ingot_am242;
	public static Item ingot_am_mix;
	public static Item ingot_cm242;
	public static Item ingot_cm243;
	public static Item ingot_cm244;
	public static Item ingot_cm245;
	public static Item ingot_cm246;
	public static Item ingot_cm247;
	public static Item ingot_cm_fuel;

	public static Item ingot_cm_mix;

	public static Item ingot_bk247;

	public static Item ingot_cf251;

	public static Item ingot_es253;
	public static Item ingot_es255;

	public static Item ingot_neptunium;
	public static Item ingot_polonium;
	public static Item ingot_technetium;
	public static Item ingot_co60;
	public static Item ingot_sr90;
	public static Item ingot_au198;
	public static Item ingot_pb209;
	public static Item ingot_ra226;
	public static Item ingot_titanium;
	public static Item ingot_nickel;
	public static Item ingot_cobalt;
	public static Item ingot_gallium;
	public static Item ingot_zinc;
	public static Item ingot_gaas;
	public static Item ingot_iridium;
	public static Item sulfur;
	public static Item ingot_palladium;

	public static Item nitra;
	public static Item nitra_small;

	public static Item coke;
	public static Item powder_coke;
	public static Item lignite;
	public static Item powder_lignite;
	public static Item briquette;
	public static Item coal_infernal;
	public static Item cinnebar;

	public static Item woodemium_briquette;

	public static Item butter; //////////////////////////////////////////////
	public static Item s_cream;
	public static Item min_cream;
	public static Item powder_ash;
	public static Item powder_limestone;
	public static Item powder_cement;

	public static Item niter;
	public static Item ingot_copper;
	public static Item ingot_red_copper;
	public static Item ingot_tungsten;
	public static Item ingot_aluminium;
	public static Item fluorite;
	public static Item ingot_beryllium;
	public static Item ingot_schraranium;
	public static Item ingot_schrabidium;
	public static Item ingot_schrabidate;
	public static Item ingot_plutonium_fuel;
	public static Item ingot_neptunium_fuel;
	public static Item ingot_uranium_fuel;
	public static Item ingot_mox_fuel;
	public static Item ingot_americium_fuel;
	public static Item ingot_schrabidium_fuel;
	public static Item ingot_thorium_fuel;
	public static Item nugget_uranium_fuel;
	public static Item nugget_thorium_fuel;
	public static Item nugget_plutonium_fuel;
	public static Item nugget_neptunium_fuel;
	public static Item nugget_mox_fuel;
	public static Item nugget_americium_fuel;
	public static Item nugget_schrabidium_fuel;
	public static Item ingot_advanced_alloy;
	public static Item ingot_tcalloy;
	public static Item ingot_cdalloy;
	public static Item ingot_bismuth_bronze;
	public static Item ingot_arsenic_bronze;
	public static Item ingot_bscco;
	public static Item lithium;
	public static Item ingot_zirconium;
	public static Item ingot_hes;
	public static Item ingot_les;
	public static Item nugget_hes;
	public static Item nugget_les;
	public static Item ingot_magnetized_tungsten;
	public static Item ingot_combine_steel;
	public static Item ingot_solinium;
	public static Item nugget_solinium;
	public static Item ingot_phosphorus;
	public static Item ingot_semtex;
	public static Item ingot_c4;
	public static Item ingot_boron;
	public static Item ingot_graphite;
	public static Item ingot_firebrick;
	public static Item ingot_smore;

	public static Item ingot_gh336;
	public static Item nugget_gh336;

	public static Item ingot_cn989;

	public static Item ingot_australium;
	public static Item ingot_australium_lesser;
	public static Item ingot_australium_greater;
	public static Item nugget_australium;
	public static Item nugget_australium_lesser;
	public static Item nugget_australium_greater;

	public static Item ingot_desh;
	public static Item nugget_desh;
	public static Item ingot_dineutronium;
	public static Item nugget_dineutronium;
	public static Item powder_dineutronium;
	public static Item ingot_tetraneutronium;
	public static Item nugget_tetraneutronium;
	public static Item powder_tetraneutronium;
	public static Item ingot_starmetal;
	public static Item ingot_gunmetal;
	public static Item plate_gunmetal;
	public static Item ingot_weaponsteel;
	public static Item plate_weaponsteel;
	public static Item ingot_saturnite;
	public static Item plate_saturnite;
	public static Item ingot_ferrouranium;
	public static Item ingot_electronium;
	public static Item ingot_gwenium;
	public static Item nugget_zirconium;
	public static Item nugget_nickel;
	public static Item nugget_zinc;
	public static Item nugget_gallium;
	public static Item nugget_gaas;
	public static Item nugget_mercury;
	public static Item ingot_mercury; //It's to prevent any ambiguity, as it was treated as a full ingot in the past anyway
	public static Item ingot_menthol; //this is correct but i want to call it menthol_crystals so bad
	public static Item nugget_menthol;
	public static Item bottle_mercury;

	public static Item ingot_tt;
	public static Item ingot_ttas;

	public static Item ore_byproduct; //byproduct of variable purity and quantity, can be treated as a nugget, might require shredding or acidizing, depends on the type

	public static Item ore_bedrock;
	public static Item ore_centrifuged;
	public static Item ore_cleaned;
	public static Item ore_separated;
	public static Item ore_purified;
	public static Item ore_nitrated;
	public static Item ore_nitrocrystalline;
	public static Item ore_deepcleaned;
	public static Item ore_seared;
	//public static Item ore_radcleaned;
	public static Item ore_enriched; //final stage
	public static Item bedrock_ore_base;
	public static Item bedrock_ore;
	public static Item bedrock_ore_fragment;

	public static Item crystal_mineral;
	public static Item crystal_cleaned;
	public static Item mineral_dust;
	public static Item mineral_fragment;

	public static Item billet_uranium;
	public static Item billet_u233;
	public static Item billet_u235;
	public static Item billet_u238;
	public static Item billet_uzh;
	public static Item billet_th232;
	public static Item billet_plutonium;
	public static Item billet_pu238;
	public static Item billet_pu239;
	public static Item billet_pu240;
	public static Item billet_pu241;
	public static Item billet_pu_mix;
	public static Item billet_am241;
	public static Item billet_am242;
	public static Item billet_bk247;
	public static Item billet_cm242;
	public static Item billet_cm243;
	public static Item billet_cm244;
	public static Item billet_cm245;
	public static Item billet_cm246;
	public static Item billet_cm247;
	public static Item billet_cn989;
	public static Item billet_cm_fuel;
	public static Item billet_cf252;
    public static Item billet_cf251;
	public static Item ingot_cf252;
	public static Item billet_cm_mix;
	public static Item billet_es253;
	public static Item billet_am_mix;
	public static Item billet_neptunium;
	public static Item billet_polonium;
	public static Item billet_technetium;
	public static Item billet_cobalt;
	public static Item billet_co60;
	public static Item billet_sr90;
	public static Item billet_au198;
	public static Item billet_pb209;
	public static Item billet_ra226;
	public static Item billet_actinium;
	public static Item billet_schrabidium;
	public static Item billet_solinium;
	public static Item billet_gh336;
	public static Item billet_australium;
	public static Item billet_australium_lesser;
	public static Item billet_australium_greater;
	public static Item billet_uranium_fuel;
	public static Item billet_thorium_fuel;
	public static Item billet_plutonium_fuel;
	public static Item billet_neptunium_fuel;
	public static Item billet_mox_fuel;
	public static Item billet_americium_fuel;
	public static Item billet_les;
	public static Item billet_schrabidium_fuel;
	public static Item billet_hes;
	public static Item billet_po210be;
	public static Item billet_ra226be;
	public static Item billet_pu238be;
	public static Item billet_yharonite;
	public static Item billet_balefire_gold;
	public static Item billet_flashlead;
	public static Item billet_zfb_bismuth;
	public static Item billet_zfb_pu241;
	public static Item billet_zfb_am_mix;
	public static Item billet_beryllium;
	public static Item billet_bismuth;
	public static Item billet_zirconium;
	public static Item billet_gaas;
	public static Item billet_nuclear_waste;
	public static Item billet_red_copper;
	public static Item billet_menthol;
	public static Item chocolate_mint_billet;

	public static Item nugget_th232;
	public static Item nugget_uranium;
	public static Item nugget_u233;
	public static Item nugget_u235;
	public static Item nugget_u238;
	public static Item nugget_plutonium;
	public static Item nugget_pu238;
	public static Item nugget_pu239;
	public static Item nugget_pu240;
	public static Item nugget_pu241;
	public static Item nugget_pu_mix;
	public static Item nugget_am241;
	public static Item nugget_am242;
	public static Item nugget_bk247;
	public static Item nugget_cm242;
	public static Item nugget_cm243;
	public static Item nugget_cm244;
	public static Item nugget_cm245;
	public static Item nugget_cm246;
	public static Item nugget_cm247;
	public static Item nugget_cn989;
	public static Item nugget_cm_fuel;
    public static Item nugget_cf251;
	public static Item nugget_cf252;
	public static Item nugget_cm_mix;
	public static Item nugget_es253;
	public static Item nugget_am_mix;
	public static Item nugget_neptunium;
	public static Item nugget_polonium;
	public static Item nugget_technetium;
	public static Item nugget_cobalt;
	public static Item nugget_co60;
	public static Item nugget_sr90;
	public static Item nugget_au198;
	public static Item nugget_pb209;
	public static Item nugget_ra226;
	public static Item nugget_actinium;
	public static Item plate_titanium;
	public static Item plate_nickel;
	public static Item plate_aluminium;
	public static Item neutron_reflector;
	public static Item ingot_steel;
	public static Item ingot_stainless;
	public static Item plate_stainless;
	public static Item plate_steel;
	public static Item plate_iron;
	public static Item powder_cn989;
	public static Item plate_cn989;
	public static Item ingot_lead;
	public static Item nugget_lead;
	public static Item ingot_bismuth;
	public static Item nugget_bismuth;
	public static Item ingot_arsenic;
	public static Item nugget_arsenic;
	public static Item ingot_tantalium;
	public static Item nugget_tantalium;
	public static Item ingot_silicon;
	public static Item billet_silicon;
	public static Item nugget_silicon;
	public static Item ingot_niobium;
	public static Item nugget_niobium;
	public static Item ingot_osmiridium;
	public static Item nugget_osmiridium;
	public static Item ingot_hafnium;
	public static Item nugget_hafnium;
	public static Item plate_lead;
	public static Item plate_dura_steel;
	public static Item nugget_schrabidium;
	public static Item plate_schrabidium;
	public static Item plate_copper;
	public static Item nugget_beryllium;
	public static Item plate_gold;
	public static Item hazmat_cloth;
	public static Item hazmat_cloth_red;
	public static Item hazmat_cloth_grey;
	public static Item asbestos_cloth;
	public static Item rag;
	public static Item rag_damp;
	public static Item rag_piss;
	public static Item rag_blood;
	public static Item filter_coal;
	public static Item plate_advanced_alloy;
	public static Item plate_combine_steel;
	public static Item plate_mixed;
	public static Item plate_paa;
	@Deprecated public static Item pipes_steel;
	public static Item drill_titanium;
	public static Item plate_dalekanium;
	public static Item plate_euphemium;
	public static Item bolt;
	public static Item bolt_spike;
	public static Item plate_polymer;
	public static Item plate_kevlar;
	public static Item plate_dineutronium;
	public static Item plate_desh;
	public static Item plate_bismuth;
	public static Item photo_panel;
	public static Item beryllium_mirror;
	public static Item sat_base;
	public static Item thruster_nuclear;
	public static Item safety_fuse;
	public static Item part_generic;
	public static Item item_expensive;
	public static Item item_secret;
	public static Item ingot_metal;
	public static Item chemical_dye;
	public static Item crayon;
	public static Item divine_shard;

	public static Item scuttertail;
	public static Item saltleaf;
	public static Item leaf_rubber;
	public static Item leaf_pet;

	public static Item undefined;

	public static Item ball_resin;
	public static Item ingot_dura_steel;
	public static Item ingot_polymer;
	public static Item ingot_bakelite;
	public static Item ingot_biorubber;
	public static Item ingot_rubber;
	public static Item ingot_pet;
	public static Item ingot_pc;
	public static Item ingot_pvc;
	public static Item stick_pvc;
	public static Item stick_vinyl;

	public static Item ingot_fiberglass;
	public static Item ingot_asbestos;
	public static Item powder_asbestos;
	public static Item ingot_calcium;
	public static Item powder_calcium;
	public static Item ingot_cadmium;
	public static Item powder_cadmium;
	public static Item powder_bismuth;
	public static Item ingot_mud;
	public static Item ingot_magma;
	public static Item ingot_cft;

	public static Item ingot_lanthanium;
	public static Item nugget_lanthanium;
	public static Item ingot_actinium;

	public static Item ingot_meteorite;
	public static Item ingot_meteorite_forged;
	public static Item blade_meteorite;
	public static Item ingot_steel_dusted;
	public static Item ingot_chainsteel;

	public static Item plate_armor_titanium;
	public static Item plate_armor_ajr;
	public static Item plate_armor_hev;
	public static Item plate_armor_lunar;
	public static Item plate_armor_fau;
	public static Item plate_armor_dnt;

	public static Item oil_tar;
	public static Item solid_fuel;
	public static Item solid_fuel_presto;
	public static Item solid_fuel_presto_triplet;
	public static Item solid_fuel_bf;
	public static Item solid_fuel_presto_bf;
	public static Item solid_fuel_presto_triplet_bf;
	public static Item rocket_fuel;

	public static Item crystal_coal;
	public static Item crystal_iron;
	public static Item crystal_gold;
	public static Item crystal_redstone;
	public static Item crystal_lapis;
	public static Item crystal_diamond;
	public static Item crystal_uranium;
	public static Item crystal_thorium;
	public static Item crystal_plutonium;
	public static Item crystal_titanium;
	public static Item crystal_sulfur;
	public static Item crystal_niter;
	public static Item crystal_copper;
	public static Item crystal_tungsten;
	public static Item crystal_aluminium;
	public static Item crystal_fluorite;
	public static Item crystal_beryllium;
	public static Item crystal_lead;
	public static Item crystal_schraranium;
	public static Item crystal_schrabidium;
	public static Item crystal_rare;
	public static Item crystal_phosphorus;
	public static Item crystal_lithium;
	public static Item crystal_cobalt;
	public static Item crystal_starmetal;
	public static Item crystal_cinnebar;
	public static Item crystal_trixite;
	public static Item crystal_osmiridium;
	public static Item crystal_nickel;
	public static Item crystal_niobium;
	//my name is seven, i made the zinc dispatcher
	//it was difficult, to dispatch the zinc() together
	//but unforutnatley something went so chopped chin wrong
	//now i cant do anything but sing this stupid song!!!!!!!!!!
	public static Item crystal_zinc;

	public static Item nickel_salts;


	public static Item gem_sodalite;
	public static Item gem_tantalium;
	public static Item gem_volcanic;
	public static Item gem_rad;
	public static Item gem_alexandrite;

	public static Item powder_lead;
	public static Item powder_tantalium;
	public static Item powder_neptunium;
	public static Item powder_polonium;
	public static Item powder_co60;
	public static Item powder_sr90;
	public static Item powder_sr90_tiny;
	public static Item powder_au198;
	public static Item powder_ra226;
	public static Item powder_i131;
	public static Item powder_i131_tiny;
	public static Item powder_xe135;
	public static Item powder_xe135_tiny;
	public static Item powder_cs137;
	public static Item powder_cs137_tiny;
	public static Item powder_at209;
	public static Item powder_schrabidium;
	public static Item powder_schrabidate;

	public static Item powder_aluminium;
	public static Item powder_beryllium;
	public static Item powder_copper;
	public static Item powder_gold;
	public static Item powder_iron;
	public static Item powder_titanium;
	public static Item powder_nickel;
	public static Item powder_gallium_tiny;
	public static Item powder_gallium;
	public static Item powder_zinc;
	public static Item powder_tungsten;
	public static Item powder_uranium;
	public static Item powder_plutonium;
	public static Item dust;
	public static Item dust_tiny;
	public static Item fallout;
	public static Item powder_power;

	public static Item powder_thorium;
	public static Item powder_iodine;
	public static Item powder_neodymium;
	public static Item powder_astatine;
	public static Item powder_caesium;

	public static Item powder_strontium;
	public static Item powder_cobalt;
	public static Item powder_bromine;
	public static Item powder_niobium;
	public static Item powder_tennessine;
	public static Item powder_cerium;

	public static Item powder_advanced_alloy;
	public static Item powder_tcalloy;
	public static Item powder_coal;
	public static Item powder_coal_tiny;
	public static Item powder_combine_steel;
	public static Item powder_diamond;
	public static Item powder_emerald;
	public static Item powder_lapis;
	public static Item powder_quartz;
	public static Item powder_magnetized_tungsten;
	public static Item powder_chlorophyte;
	public static Item powder_red_copper;
	public static Item powder_steel;
	public static Item powder_lithium;
	public static Item powder_zirconium;
	public static Item powder_sodium;

	public static Item powder_australium;

	public static Item powder_dura_steel;
	public static Item powder_polymer;
	public static Item powder_bakelite;
	public static Item powder_rubber;
	public static Item powder_pvc;
	public static Item powder_euphemium;
	public static Item powder_meteorite;

	public static Item powder_wd2004_tiny;
	public static Item powder_wd2004;

	public static Item powder_steel_tiny;
	public static Item powder_lithium_tiny;
	public static Item powder_neodymium_tiny;
	public static Item powder_cobalt_tiny;
	public static Item powder_niobium_tiny;
	public static Item powder_cerium_tiny;
	public static Item powder_lanthanium_tiny;
	public static Item powder_actinium_tiny;
	public static Item powder_boron_tiny;
	public static Item powder_meteorite_tiny;

	public static Item powder_coltan_ore;
	public static Item powder_coltan;
	public static Item powder_tektite;
	public static Item powder_paleogenite;
	public static Item powder_paleogenite_tiny;
	public static Item powder_impure_osmiridium;
	public static Item powder_borax;
	public static Item powder_chlorocalcite;
	public static Item powder_molysite;

	public static Item powder_lanthanium;
	public static Item powder_actinium;
	public static Item powder_boron;
	public static Item powder_desh;
	public static Item powder_semtex_mix;
	public static Item powder_desh_mix;
	public static Item powder_desh_ready;
	public static Item powder_nitan_mix;
	public static Item powder_spark_mix;
	public static Item powder_yellowcake;
	public static Item powder_magic;
	public static Item powder_balefire;
	public static Item powder_sawdust;
	public static Item powder_flux;
	public static Item powder_fertilizer;

	public static Item fragment_neodymium;
	public static Item fragment_cobalt;
	public static Item fragment_niobium;
	public static Item fragment_cerium;
	public static Item fragment_lanthanium;
	public static Item fragment_actinium;
	public static Item fragment_boron;
	public static Item fragment_meteorite;
	public static Item fragment_coltan;
	public static Item chunk_ore;

	public static Item biomass;
	public static Item flesh;
	public static Item flesh_wafer;
	public static Item grilled_flesh;
	public static Item flesh_burger;
	public static Item biomass_compressed;
	public static Item bio_wafer;
	public static Item plant_item;

	public static Item coil_copper;
	public static Item coil_copper_torus;
	public static Item coil_tungsten;
	@Deprecated public static Item tank_steel;
	public static Item motor;
	public static Item motor_desh;
	public static Item motor_bismuth;
	public static Item centrifuge_element;
	public static Item reactor_core;
	public static Item rtg_unit;

	public static Item coil_advanced_alloy;
	public static Item coil_advanced_torus;
	public static Item coil_magnetized_tungsten;
	public static Item coil_gold;
	public static Item coil_gold_torus;
	public static Item chlorine_pinwheel;
	public static Item oxy_pinwheel;
	public static Item deuterium_filter;

	public static Item parts_legendary;

	public static Item circuit;

	public static Item crt_display;
	public static ItemEnumMulti circuit_star_piece;
	public static ItemEnumMulti circuit_star_component;
	public static Item circuit_star;

	public static Item assembly_nuke;

	public static Item casing;

	public static Item wiring_red_copper;
	public static Item wrench;

	public static Item shell;
	public static Item pipe;
	public static Item fins_flat;
	public static Item fins_small_steel;
	public static Item fins_big_steel;
	public static Item fins_tri_steel;
	public static Item fins_quad_titanium;
	public static Item sphere_steel;
	public static Item pedestal_steel;
	public static Item dysfunctional_reactor;
	public static Item blade_titanium;
	public static Item blade_syngas;
	public static Item turbine_titanium;
	public static Item blade_tungsten;
	public static Item turbine_tungsten;
	public static Item turbine_syngas;
	public static Item ring_starmetal;
	public static Item flywheel_beryllium;

	public static Item gear_large;
	public static Item sawblade;

	public static Item ducttape;
	public static Item catalyst_clay;

	public static Item warhead_generic_small;
	public static Item warhead_generic_medium;
	public static Item warhead_generic_large;
	public static Item warhead_incendiary_small;
	public static Item warhead_incendiary_medium;
	public static Item warhead_incendiary_large;
	public static Item warhead_cluster_small;
	public static Item warhead_cluster_medium;
	public static Item warhead_cluster_large;
	public static Item warhead_buster_small;
	public static Item warhead_buster_medium;
	public static Item warhead_buster_large;
	public static Item warhead_nuclear;
	public static Item warhead_mirv;
	public static Item warhead_volcano;

	public static Item fuel_tank_small;
	public static Item fuel_tank_medium;
	public static Item fuel_tank_large;

	public static Item thruster_small;
	public static Item thruster_medium;
	public static Item thruster_large;

	public static Item sat_head_mapper;
	public static Item sat_head_scanner;
	public static Item sat_head_radar;
	public static Item sat_head_laser;
	public static Item sat_head_resonator;

	public static Item seg_10;
	public static Item seg_15;
	public static Item seg_20;

	public static Item combine_scrap;

	public static Item shimmer_head;
	public static Item shimmer_axe_head;
	public static Item shimmer_handle;

	//public static Item telepad;
	public static Item entanglement_kit;

	public static Item stamp_stone_flat;
	public static Item stamp_stone_plate;
	public static Item stamp_stone_wire;
	public static Item stamp_stone_circuit;
	public static Item stamp_iron_flat;
	public static Item stamp_iron_plate;
	public static Item stamp_iron_wire;
	public static Item stamp_iron_circuit;
	public static Item stamp_steel_flat;
	public static Item stamp_steel_plate;
	public static Item stamp_steel_wire;
	public static Item stamp_steel_circuit;
	public static Item stamp_titanium_flat;
	public static Item stamp_titanium_plate;
	public static Item stamp_titanium_wire;
	public static Item stamp_titanium_circuit;
	public static Item stamp_obsidian_flat;
	public static Item stamp_obsidian_plate;
	public static Item stamp_obsidian_wire;
	public static Item stamp_obsidian_circuit;
	public static Item stamp_desh_flat;
	public static Item stamp_desh_plate;
	public static Item stamp_desh_wire;
	public static Item stamp_desh_circuit;
	public static Item stamp_book;

	public static Item stamp_357;
	public static Item stamp_44;
	public static Item stamp_9;
	public static Item stamp_50;

	public static Item stamp_desh_357;
	public static Item stamp_desh_44;
	public static Item stamp_desh_9;
	public static Item stamp_desh_50;

	public static Item blades_steel;
	public static Item blades_titanium;
	public static Item blades_advanced_alloy;
	public static Item blades_desh;

	public static Item mold_base;
	public static Item mold;
	public static Item scraps;
	public static Item ingot_raw;
	public static Item plate_cast;
	public static Item plate_welded;
	public static Item wire_fine;
	public static Item wire_dense;
	public static Item part_barrel_light;
	public static Item part_barrel_heavy;
	public static Item part_receiver_light;
	public static Item part_receiver_heavy;
	public static Item part_mechanism;
	public static Item part_stock;
	public static Item part_grip;

	public static Item part_lithium;
	public static Item part_beryllium;
	public static Item part_carbon;
	public static Item part_copper;
	public static Item part_plutonium;

	public static Item laser_crystal_co2;
	public static Item laser_crystal_bismuth;
	public static Item laser_crystal_cmb;
	public static Item laser_crystal_iron;
	public static Item laser_crystal_dnt;
	public static Item laser_crystal_digamma;

	public static Item thermo_element;

	public static Item catalytic_converter;
	public static Item crackpipe;

	public static Item pellet_rtg_depleted;

	public static Item pellet_rtg_radium;
	public static Item pellet_rtg_weak;
	public static Item pellet_rtg;
	public static Item pellet_rtg_strontium;
	public static Item pellet_rtg_cobalt;
	public static Item pellet_rtg_actinium;
	public static Item pellet_rtg_polonium;
	public static Item pellet_rtg_americium;
	public static Item pellet_rtg_berkelium;
	public static Item pellet_rtg_gold;
	public static Item pellet_rtg_lead;
	public static Item pellet_rtg_cf251;
	public static Item pellet_rtg_cf252;

	public static Item piston_selenium;
	public static Item piston_set;
	public static Item drillbit;

	public static Item rune_blank;
	public static Item rune_isa;
	public static Item rune_dagaz;
	public static Item rune_hagalaz;
	public static Item rune_jera;
	public static Item rune_thurisaz;

	public static Item ams_catalyst_blank;
	public static Item ams_catalyst_aluminium;
	public static Item ams_catalyst_beryllium;
	public static Item ams_catalyst_caesium;
	public static Item ams_catalyst_cerium;
	public static Item ams_catalyst_cobalt;
	public static Item ams_catalyst_copper;
	public static Item ams_catalyst_dineutronium;
	public static Item ams_catalyst_euphemium;
	public static Item ams_catalyst_iron;
	public static Item ams_catalyst_lithium;
	public static Item ams_catalyst_niobium;
	public static Item ams_catalyst_schrabidium;
	public static Item ams_catalyst_strontium;
	public static Item ams_catalyst_thorium;
	public static Item ams_catalyst_tungsten;

	public static Item ams_lens;

	public static Item ams_core_sing;
	public static Item ams_core_wormhole;
	public static Item ams_core_eyeofharmony;
	public static Item ams_core_thingy;

	@Deprecated public static Item fusion_shield_tungsten;
	@Deprecated public static Item fusion_shield_desh;
	@Deprecated public static Item fusion_shield_chlorophyte;
	@Deprecated public static Item fusion_shield_vaporwave;

	public static Item cell_empty;
	public static Item cell_uf6;
	public static Item cell_puf6;
	public static Item cell_deuterium;
	public static Item cell_tritium;
	public static Item cell_sas3;
	public static Item cell_antimatter;
	public static Item cell_anti_schrabidium;
	public static Item cell_balefire;

	public static Item demon_core_open;
	public static Item demon_core_closed;

	public static Item pa_coil;

	public static Item particle_empty;
	public static Item particle_hydrogen;
	public static Item particle_copper;
	public static Item particle_lead;
	public static Item particle_amat;
	public static Item particle_aschrab;
	public static Item particle_higgs;
	public static Item particle_muon;
	public static Item particle_tachyon;
	public static Item particle_strange;
	public static Item particle_dark;
	public static Item particle_sparkticle;
	public static Item particle_digamma;
	public static Item particle_lutece;

	public static Item pellet_antimatter;
	public static Item singularity;
	public static Item singularity_counter_resonant;
	public static Item singularity_super_heated;
	public static Item black_hole;
	public static Item singularity_spark;
	public static Item crystal_xen;
	public static Item inf_water;
	public static Item inf_water_mk2;

	public static Item fuel_additive;

	public static Item canister_empty;
	public static Item canister_full;
	public static Item canister_napalm;

	public static Item gas_empty;
	public static Item gas_full;

	public static Item fluid_tank_full;
	public static Item fluid_tank_empty;
	public static Item fluid_tank_lead_full;
	public static Item fluid_tank_lead_empty;
	public static Item fluid_barrel_full;
	public static Item fluid_barrel_empty;
	public static Item fluid_barrel_infinite;
	public static Item fluid_pack_full;
	public static Item fluid_pack_empty;
	public static Item pipette;
	public static Item pipette_boron;
	public static Item pipette_laboratory;
	public static Item siphon;

	public static Item disperser_canister_empty;
	public static Item disperser_canister;
	public static Item glyphid_gland;
	public static Item glyphid_gland_empty;

	public static Item syringe_empty;
	public static Item syringe_antidote;
	public static Item syringe_poison;
	public static Item syringe_awesome;
	public static Item syringe_metal_empty;
	public static Item syringe_metal_stimpak;
	public static Item syringe_metal_medx;
	public static Item syringe_metal_psycho;
	public static Item syringe_metal_super;
	public static Item syringe_taint;
	public static Item syringe_mkunicorn;
	public static Item iv_empty;
	public static Item iv_blood;
	public static Item iv_xp_empty;
	public static Item iv_xp;
	public static Item radaway;
	public static Item radaway_strong;
	public static Item radaway_flush;
	public static Item radx;
	public static Item siox;
	public static Item pill_herbal;
	public static Item xanax;
	public static Item fmn;
	public static Item five_htp;
	public static Item med_bag;
	public static Item pill_iodine;
	public static Item plan_c;
	public static Item pill_red;
	public static Item stealth_boy;
	public static Item gas_mask_filter;
	public static Item gas_mask_filter_mono;
	public static Item gas_mask_filter_combo;
	public static Item gas_mask_filter_rag;
	public static Item gas_mask_filter_piss;
	public static Item jetpack_tank;
	public static Item lox_tank;
	public static Item gun_kit_1;
	public static Item gun_kit_2;
	public static Item cbt_device;
	public static Item cigarette;

	public static Item animan;

	public static Item can_empty;
	public static Item can_smart;
	public static Item can_creature;
	public static Item can_redbomb;
	public static Item can_mrsugar;
	public static Item can_overcharge;
	public static Item can_luna;
	public static Item can_bepis;
	public static Item can_breen;
	public static Item can_mug;
	public static Item mucho_mango;
	public static Item bottle_empty;
	public static Item bottle_nuka;
	public static Item bottle_cherry;
	public static Item bottle_quantum;
	public static Item bottle_sparkle;
	public static Item bottle_rad;
	public static Item bottle2_empty;
	public static Item bottle2_korl;
	public static Item bottle2_fritz;
	public static Item flask_empty;
	public static Item flask_infusion;
	public static Item chocolate_milk;
	public static Item coffee;
	public static Item coffee_radium;
	public static Item chocolate;
	public static Item cap_nuka;
	public static Item cap_quantum;
	public static Item cap_sparkle;
	public static Item cap_rad;
	public static Item cap_korl;
	public static Item cap_fritz;
	public static Item ring_pull;
	public static Item bdcl;
	public static ItemEnumMulti canned_conserve;
	public static Item can_key;

	public static Item glass_empty;
	public static Item glass_smilk;
	public static Item strawberry;
	public static Item mint_leaves;
	public static Item teaseeds;
	public static Item tea_leaf;
	public static Item bean_raw;
	public static Item bean_roast;
	public static Item powder_coffee;
	public static Item cmug_empty;
	public static Item teacup;
	public static Item teacup_empty;
	public static Item bottle_honey;
	public static Item paraffin_seeds;


	public static Item boat_rubber;
	public static Item cart;
	public static Item train;
	public static Item drone;

	public static Item coin_creeper;
	public static Item coin_radiation;
	public static Item coin_maskman;
	public static Item coin_worm;
	public static Item coin_ufo;
	public static Item coin_airliner;
	//public static Item coin_siege;
	//public static Item source;

	public static Item rod_empty;
	public static Item rod;
	public static Item rod_dual_empty;
	public static Item rod_dual;
	public static Item rod_quad_empty;
	public static Item rod_quad;

	public static Item rod_zirnox_empty;
	public static Item rod_zirnox_tritium;
	public static ItemEnumMulti rod_zirnox;

	public static ItemEnumMulti full_drive;
	public static Item hard_drive;

	public static Item rod_zirnox_natural_uranium_fuel_depleted;
	public static Item rod_zirnox_uranium_fuel_depleted;
	public static Item rod_zirnox_thorium_fuel_depleted;
	public static Item rod_zirnox_mox_fuel_depleted;
	public static Item rod_zirnox_plutonium_fuel_depleted;
	public static Item rod_zirnox_u233_fuel_depleted;
	public static Item rod_zirnox_u235_fuel_depleted;
	public static Item rod_zirnox_les_fuel_depleted;
	public static Item rod_zirnox_zfb_mox_depleted;

	public static Item waste_natural_uranium;
	public static Item waste_uranium;
	public static Item waste_thorium;
	public static Item waste_mox;
	public static Item waste_plutonium;
	public static Item waste_u233;
	public static Item waste_u235;
	public static Item waste_schrabidium;
	public static Item waste_zfb_mox;

	public static Item waste_plate_u233;
	public static Item waste_plate_u235;
	public static Item waste_plate_mox;
	public static Item waste_plate_pu239;
	public static Item waste_plate_sa326;
	public static Item waste_plate_ra226be;
	public static Item waste_plate_pu238be;

	public static Item pile_rod_uranium;
	public static Item pile_rod_pu239;
	public static Item pile_rod_plutonium;
	public static Item pile_rod_source;
	public static Item pile_rod_boron;
	public static Item pile_rod_lithium;
	public static Item pile_rod_detector;

	public static Item plate_fuel_u233;
	public static Item plate_fuel_u235;
	public static Item plate_fuel_mox;
	public static Item plate_fuel_pu239;
	public static Item plate_fuel_sa326;
	public static Item plate_fuel_ra226be;
	public static Item plate_fuel_pu238be;

	public static Item pwr_fuel;
	public static Item pwr_fuel_hot;
	public static Item pwr_fuel_depleted;
	public static Item pwr_printer;

	public static Item rbmk_lid;
	public static Item rbmk_lid_glass;
	public static Item rbmk_fuel_empty;
	public static ItemRBMKRod rbmk_fuel_ueu;
	public static ItemRBMKRod rbmk_fuel_meu;
	public static ItemRBMKRod rbmk_fuel_heu233;
	public static ItemRBMKRod rbmk_fuel_heu235;
	public static ItemRBMKRod rbmk_fuel_uzh;
	public static ItemRBMKRod rbmk_fuel_thmeu;
	public static ItemRBMKRod rbmk_fuel_lep;
	public static ItemRBMKRod rbmk_fuel_mep;
	public static ItemRBMKRod rbmk_fuel_hep239;
	public static ItemRBMKRod rbmk_fuel_hep241;
	public static ItemRBMKRod rbmk_fuel_lea;
	public static ItemRBMKRod rbmk_fuel_mea;
	public static ItemRBMKRod rbmk_fuel_hea241;
	public static ItemRBMKRod rbmk_fuel_hea242;
	public static ItemRBMKRod rbmk_fuel_bk247;
	public static ItemRBMKRod rbmk_fuel_men;
	public static ItemRBMKRod rbmk_fuel_hen;
	public static ItemRBMKRod rbmk_fuel_mox;
	public static ItemRBMKRod rbmk_fuel_les;
	public static ItemRBMKRod rbmk_fuel_mes;
	public static ItemRBMKRod rbmk_fuel_hes;
	public static ItemRBMKRod rbmk_fuel_leaus;
	public static ItemRBMKRod rbmk_fuel_heaus;
	public static ItemRBMKRod rbmk_fuel_po210be;
	public static ItemRBMKRod rbmk_fuel_ra226be;
	public static ItemRBMKRod rbmk_fuel_pu238be;
	public static ItemRBMKRod rbmk_fuel_balefire_gold;
	public static ItemRBMKRod rbmk_fuel_flashlead;
	public static ItemRBMKRod rbmk_fuel_balefire;
	public static ItemRBMKRod rbmk_fuel_zfb_bismuth;
	public static ItemRBMKRod rbmk_fuel_zfb_pu241;
	public static ItemRBMKRod rbmk_fuel_zfb_am_mix;
	public static ItemRBMKRod rbmk_fuel_drx;
	public static ItemRBMKRod rbmk_fuel_test;
	public static ItemRBMKRod rbmk_fuel_lecm;
	public static ItemRBMKRod rbmk_fuel_mecm;
	public static ItemRBMKRod rbmk_fuel_hecm;
	public static ItemRBMKPellet rbmk_pellet_ueu;
	public static ItemRBMKPellet rbmk_pellet_meu;
	public static ItemRBMKPellet rbmk_pellet_heu233;
	public static ItemRBMKPellet rbmk_pellet_heu235;
	public static ItemRBMKPellet rbmk_pellet_uzh;
	public static ItemRBMKPellet rbmk_pellet_thmeu;
	public static ItemRBMKPellet rbmk_pellet_lep;
	public static ItemRBMKPellet rbmk_pellet_mep;
	public static ItemRBMKPellet rbmk_pellet_hep239;
	public static ItemRBMKPellet rbmk_pellet_hep241;
	public static ItemRBMKPellet rbmk_pellet_lea;
	public static ItemRBMKPellet rbmk_pellet_mea;
	public static ItemRBMKPellet rbmk_pellet_hea241;
	public static ItemRBMKPellet rbmk_pellet_hea242;
	public static ItemRBMKPellet rbmk_pellet_bk247;
	public static ItemRBMKPellet rbmk_pellet_men;
	public static ItemRBMKPellet rbmk_pellet_hen;
	public static ItemRBMKPellet rbmk_pellet_mox;
	public static ItemRBMKPellet rbmk_pellet_les;
	public static ItemRBMKPellet rbmk_pellet_mes;
	public static ItemRBMKPellet rbmk_pellet_hes;
	public static ItemRBMKPellet rbmk_pellet_leaus;
	public static ItemRBMKPellet rbmk_pellet_heaus;
	public static ItemRBMKPellet rbmk_pellet_po210be;
	public static ItemRBMKPellet rbmk_pellet_ra226be;
	public static ItemRBMKPellet rbmk_pellet_pu238be;
	public static ItemRBMKPellet rbmk_pellet_balefire_gold;
	public static ItemRBMKPellet rbmk_pellet_flashlead;
	public static ItemRBMKPellet rbmk_pellet_balefire;
	public static ItemRBMKPellet rbmk_pellet_zfb_bismuth;
	public static ItemRBMKPellet rbmk_pellet_zfb_pu241;
	public static ItemRBMKPellet rbmk_pellet_zfb_am_mix;
	public static ItemRBMKPellet rbmk_pellet_drx;
	public static ItemRBMKPellet rbmk_pellet_lecm;
	public static ItemRBMKPellet rbmk_pellet_mecm;
	public static ItemRBMKPellet rbmk_pellet_hecm;
	public static ItemRBMKPellet rbmk_pellet_lecf;
	public static ItemRBMKPellet rbmk_pellet_mecf;
	public static ItemRBMKPellet rbmk_pellet_hecf;

	public static Item watz_pellet;
	public static Item watz_pellet_depleted;

	public static Item icf_pellet_empty;
	public static Item icf_pellet;
	public static Item icf_pellet_depleted;

	public static Item scrap_plastic;
	public static Item scrap;
	public static Item scrap_oil;
	public static Item scrap_nuclear;
	public static Item trinitite;
	public static Item nuclear_waste_long;
	public static Item nuclear_waste_long_tiny;
	public static Item nuclear_waste_short;
	public static Item nuclear_waste_short_tiny;
	public static Item nuclear_waste_long_depleted;
	public static Item nuclear_waste_long_depleted_tiny;
	public static Item nuclear_waste_short_depleted;
	public static Item nuclear_waste_short_depleted_tiny;
	public static Item nuclear_waste;
	public static Item nuclear_waste_tiny;
	public static Item nuclear_waste_vitrified;
	public static Item nuclear_waste_vitrified_tiny;

	public static Item debris_graphite;
	public static Item debris_metal;
	public static Item debris_fuel;
	public static Item debris_concrete;
	public static Item debris_exchanger;
	public static Item debris_shrapnel;
	public static Item debris_element;

	public static Item containment_box;
	public static Item plastic_bag;

	public static Item ammo_bag;
	public static Item ammo_bag_infinite;
	public static Item casing_bag;

	public static Item cordite;
	public static Item ballistite;
	public static Item ball_dynamite;
	public static Item ball_tnt;
	public static Item ball_tatb;
	public static Item ball_fireclay;
	public static Item ammonium_nitrate;
	public static Item ball_ferric_clay;

	public static Item pellet_cluster;
	public static Item powder_fire;
	public static Item powder_ice;
	public static Item powder_poison;
	public static Item powder_thermite;
	public static Item pellet_gas;
	public static Item magnetron;
	public static Item pellet_buckshot;
	public static Item pellet_charged;

	public static Item rangefinder;
	public static Item designator;
	public static Item designator_range;
	public static Item designator_manual;
	public static Item designator_arty_range;
	public static Item linker;
	public static Item reactor_sensor;
	public static Item oil_detector;
	public static Item dosimeter;
	public static Item geiger_counter;
	public static Item digamma_diagnostic;
	public static Item pollution_detector;
	public static Item ore_density_scanner;
	public static Item survey_scanner;
	public static Item mirror_tool;
	public static Item rbmk_tool;
	public static Item coltan_tool;
	public static Item power_net_tool;
	public static Item analysis_tool;
	public static Item coupling_tool;
	public static Item drone_linker;
	public static Item radar_linker;
	public static Item settings_tool;
	public static Item atmosphere_scanner;

	public static Item blueprints;
	public static Item blueprint_folder;
	public static Item template_folder;
	public static Item crucible_template;
	public static Item fluid_identifier_multi;
	public static Item fluid_icon;
	public static Item siren_track;
	public static Item fluid_duct;

	public static Item bobmazon;
	public static Item bobmazon_hidden;

	public static Item launch_code_piece;
	public static Item launch_code;
	public static Item launch_key;

	public static Item transporter_linker;

	public static Item missile_assembly;
	public static Item missile_generic;
	public static Item missile_anti_ballistic;
	public static Item missile_incendiary;
	public static Item missile_cluster;
	public static Item missile_buster;
	public static Item missile_decoy;
	public static Item missile_strong;
	public static Item missile_incendiary_strong;
	public static Item missile_cluster_strong;
	public static Item missile_buster_strong;
	public static Item missile_emp_strong;
	public static Item missile_burst;
	public static Item missile_inferno;
	public static Item missile_rain;
	public static Item missile_drill;
	public static Item missile_nuclear;
	public static Item missile_nuclear_cluster;
	public static Item missile_volcano;
	public static Item missile_doomsday;
	public static Item missile_doomsday_rusted;
	public static Item missile_taint;
	public static Item missile_micro;
	public static Item missile_bhole;
	public static Item missile_schrabidium;
	public static Item missile_emp;
	public static Item missile_shuttle;
	public static Item missile_stealth;
	public static Item missile_test;

	public static Item mp_thruster_10_kerosene;
	public static Item mp_thruster_10_solid;
	public static Item mp_thruster_10_hydrazine;
	public static Item mp_thruster_10_xenon;
	public static Item mp_thruster_15_kerosene;
	public static Item mp_thruster_15_kerosene_dual;
	public static Item mp_thruster_15_kerosene_triple;
	public static Item mp_thruster_15_solid;
	public static Item mp_thruster_15_solid_hexdecuple;
	public static Item mp_thruster_15_hydrogen;
	public static Item mp_thruster_15_hydrogen_dual;
	public static Item mp_thruster_15_balefire_short;
	public static Item mp_thruster_15_balefire;
	public static Item mp_thruster_15_balefire_large;
	public static Item mp_thruster_15_balefire_large_rad;
	public static Item mp_thruster_20_kerosene;
	public static Item mp_thruster_20_kerosene_dual;
	public static Item mp_thruster_20_kerosene_triple;
	public static Item mp_thruster_20_methalox;
	public static Item mp_thruster_20_methalox_dual;
	public static Item mp_thruster_20_methalox_triple;
	public static Item mp_thruster_20_hydrogen;
	public static Item mp_thruster_20_hydrogen_dual;
	public static Item mp_thruster_20_hydrogen_triple;
	public static Item mp_thruster_20_solid;
	public static Item mp_thruster_20_solid_multi;
	public static Item mp_thruster_20_solid_multier;
	public static Item mp_thruster_20_hydrazine;

	public static Item mp_stability_10_flat;
	public static Item mp_stability_10_cruise;
	public static Item mp_stability_10_space;
	public static Item mp_stability_15_flat;
	public static Item mp_stability_15_thin;
	public static Item mp_stability_15_soyuz;
	public static Item mp_stability_20_flat;

	public static Item mp_fuselage_10_kerosene;
	public static Item mp_fuselage_10_kerosene_camo;
	public static Item mp_fuselage_10_kerosene_desert;
	public static Item mp_fuselage_10_kerosene_sky;
	public static Item mp_fuselage_10_kerosene_flames;
	public static Item mp_fuselage_10_kerosene_insulation;
	public static Item mp_fuselage_10_kerosene_sleek;
	public static Item mp_fuselage_10_kerosene_metal;
	public static Item mp_fuselage_10_kerosene_taint;

	public static Item mp_fuselage_10_solid;
	public static Item mp_fuselage_10_solid_flames;
	public static Item mp_fuselage_10_solid_insulation;
	public static Item mp_fuselage_10_solid_sleek;
	public static Item mp_fuselage_10_solid_soviet_glory;
	public static Item mp_fuselage_10_solid_cathedral;
	public static Item mp_fuselage_10_solid_moonlit;
	public static Item mp_fuselage_10_solid_battery;
	public static Item mp_fuselage_10_solid_duracell;

	public static Item mp_fuselage_10_hydrazine;

	public static Item mp_fuselage_10_xenon;
	public static Item mp_fuselage_10_xenon_bhole;

	public static Item mp_fuselage_10_long_kerosene;
	public static Item mp_fuselage_10_long_kerosene_camo;
	public static Item mp_fuselage_10_long_kerosene_desert;
	public static Item mp_fuselage_10_long_kerosene_sky;
	public static Item mp_fuselage_10_long_kerosene_flames;
	public static Item mp_fuselage_10_long_kerosene_insulation;
	public static Item mp_fuselage_10_long_kerosene_sleek;
	public static Item mp_fuselage_10_long_kerosene_metal;
	public static Item mp_fuselage_10_long_kerosene_taint;
	public static Item mp_fuselage_10_long_kerosene_dash;
	public static Item mp_fuselage_10_long_kerosene_vap;

	public static Item mp_fuselage_10_long_solid;
	public static Item mp_fuselage_10_long_solid_flames;
	public static Item mp_fuselage_10_long_solid_insulation;
	public static Item mp_fuselage_10_long_solid_sleek;
	public static Item mp_fuselage_10_long_solid_soviet_glory;
	public static Item mp_fuselage_10_long_solid_bullet;
	public static Item mp_fuselage_10_long_solid_silvermoonlight;

	public static Item mp_fuselage_10_15_kerosene;
	public static Item mp_fuselage_10_15_solid;
	public static Item mp_fuselage_10_15_hydrogen;
	public static Item mp_fuselage_10_15_balefire;

	public static Item mp_fuselage_15_kerosene;
	public static Item mp_fuselage_15_kerosene_camo;
	public static Item mp_fuselage_15_kerosene_desert;
	public static Item mp_fuselage_15_kerosene_sky;
	public static Item mp_fuselage_15_kerosene_insulation;
	public static Item mp_fuselage_15_kerosene_metal;
	public static Item mp_fuselage_15_kerosene_decorated;
	public static Item mp_fuselage_15_kerosene_steampunk;
	public static Item mp_fuselage_15_kerosene_polite;
	public static Item mp_fuselage_15_kerosene_blackjack;
	public static Item mp_fuselage_15_kerosene_lambda;
	public static Item mp_fuselage_15_kerosene_minuteman;
	public static Item mp_fuselage_15_kerosene_pip;
	public static Item mp_fuselage_15_kerosene_taint;
	public static Item mp_fuselage_15_kerosene_yuck;

	public static Item mp_fuselage_15_solid;
	public static Item mp_fuselage_15_solid_insulation;
	public static Item mp_fuselage_15_solid_desh;
	public static Item mp_fuselage_15_solid_soviet_glory;
	public static Item mp_fuselage_15_solid_soviet_stank;
	public static Item mp_fuselage_15_solid_faust;
	public static Item mp_fuselage_15_solid_silvermoonlight;
	public static Item mp_fuselage_15_solid_snowy;
	public static Item mp_fuselage_15_solid_panorama;
	public static Item mp_fuselage_15_solid_roses;
	public static Item mp_fuselage_15_solid_mimi;

	public static Item mp_fuselage_15_hydrogen;
	public static Item mp_fuselage_15_hydrogen_cathedral;

	public static Item mp_fuselage_15_balefire;

	public static Item mp_fuselage_15_20_kerosene;
	public static Item mp_fuselage_15_20_kerosene_magnusson;
	public static Item mp_fuselage_15_20_solid;

	public static Item mp_warhead_10_he;
	public static Item mp_warhead_10_incendiary;
	public static Item mp_warhead_10_buster;
	public static Item mp_warhead_10_nuclear;
	public static Item mp_warhead_10_nuclear_large;
	public static Item mp_warhead_10_taint;
	public static Item mp_warhead_10_cloud;
	public static Item mp_warhead_15_he;
	public static Item mp_warhead_15_incendiary;
	public static Item mp_warhead_15_nuclear;
	public static Item mp_warhead_15_nuclear_shark;
	public static Item mp_warhead_15_nuclear_mimi;
	public static Item mp_warhead_15_boxcar;
	public static Item mp_warhead_15_n2;
	public static Item mp_warhead_15_balefire;
	public static Item mp_warhead_15_turbine;

	public static Item mp_chip_1;
	public static Item mp_chip_2;
	public static Item mp_chip_3;
	public static Item mp_chip_4;
	public static Item mp_chip_5;

	public static Item rp_fuselage_20_12;
	public static Item rp_fuselage_20_6;
	public static Item rp_fuselage_20_3;
	public static Item rp_fuselage_20_1;
	public static Item rp_fuselage_20_12_hydrazine;

	public static Item rp_legs_20;

	public static Item rp_capsule_20;
	public static Item rp_station_core_20;
	public static Item rp_pod_20;

	public static Item missile_custom;
	public static Item rocket_custom;

	public static Item missile_soyuz;
	public static Item missile_soyuz_lander;
	public static Item sat_mapper;
	public static Item sat_scanner;
	public static Item sat_radar;
	public static Item sat_laser;
	public static Item sat_foeq;
	public static Item sat_resonator;
	public static Item sat_miner;
	public static Item sat_lunar_miner;
	public static Item sat_gerald;
	public static Item sat_dyson_relay;
	public static Item sat_chip;
	public static Item sat_war;

	public static Item sat_interface;
	public static Item sat_coord;
	public static Item sat_designator;
	public static Item sat_relay;

	public static ItemEnumMulti ammo_misc;
	public static ItemEnumMulti ammo_shell;
	public static ItemEnumMulti ammo_fireext;

	public static Item ammo_dgk;
	public static Item ammo_arty;
	public static Item ammo_himars;

	public static Item gun_b92;
	public static Item gun_b92_ammo;
	public static Item gun_fireext;

	public static Item gun_debug;
	public static Item ammo_debug;

	public static Item gun_pepperbox;
	public static Item gun_light_revolver;
	public static Item gun_light_revolver_atlas;
	public static Item gun_light_revolver_dani;
	public static Item gun_henry;
	public static Item gun_henry_lincoln;
	public static Item gun_greasegun;
	public static Item gun_maresleg;
	public static Item gun_maresleg_akimbo;
	public static Item gun_maresleg_broken;
	public static Item gun_flaregun;
	public static Item gun_heavy_revolver;
	public static Item gun_heavy_revolver_lilmac;
	public static Item gun_heavy_revolver_protege;
	public static Item gun_carbine;
	public static Item gun_am180;
	public static Item gun_liberator;
	public static Item gun_congolake;
	public static Item gun_flamer;
	public static Item gun_flamer_topaz;
	public static Item gun_flamer_daybreaker;
	public static Item gun_uzi;
	public static Item gun_uzi_akimbo;
	public static Item gun_spas12;
	public static Item gun_panzerschreck;
	public static Item gun_star_f;
	public static Item gun_star_f_akimbo;
	public static Item gun_g3;
	public static Item gun_g3_zebra;
	public static Item gun_stinger;
	public static Item gun_chemthrower;
	public static Item gun_amat;
	public static Item gun_amat_subtlety;
	public static Item gun_amat_penance;
	public static Item gun_m2;
	public static Item gun_autoshotgun;
	public static Item gun_autoshotgun_shredder;
	public static Item gun_autoshotgun_sexy;
	public static Item gun_autoshotgun_heretic;
	public static Item gun_quadro;
	public static Item gun_lag;
	public static Item gun_minigun;
	public static Item gun_minigun_dual;
	public static Item gun_minigun_lacunae;
	public static Item gun_missile_launcher;
	public static Item gun_tesla_cannon;
	public static Item gun_laser_pistol;
	public static Item gun_laser_pistol_pew_pew;
	public static Item gun_laser_pistol_morning_glory;
	public static Item gun_stg77;
	public static Item gun_tau;
	public static Item gun_fatman;
	public static Item gun_lasrifle;
	public static Item gun_coilgun;
	public static Item gun_hangman;
	public static Item gun_mas36;
	public static Item gun_bolter;
	public static Item gun_folly;
	public static Item gun_aberrator;
	public static Item gun_aberrator_eott;
	public static Item gun_double_barrel;
	public static Item gun_double_barrel_sacred_dragon;
	public static Item gun_n_i_4_n_i;
	public static Item gun_charge_thrower;
	public static Item gun_drill;
	public static Item gun_pa_melee;
	public static Item gun_pa_ranged;

	public static Item ammo_standard;
	public static Item ammo_secret;

	public static Item weapon_mod_test;
	public static Item weapon_mod_generic;
	public static Item weapon_mod_special;
	public static Item weapon_mod_caliber;

	public static Item crucible;

	public static Item stick_dynamite;
	public static Item stick_dynamite_fishing;
	public static Item stick_tnt;
	public static Item stick_semtex;
	public static Item stick_c4;

	public static Item grenade_generic;
	public static Item grenade_strong;
	public static Item grenade_frag;
	public static Item grenade_fire;
	public static Item grenade_shrapnel;
	public static Item grenade_cluster;
	public static Item grenade_flare;
	public static Item grenade_electric;
	public static Item grenade_poison;
	public static Item grenade_gas;
	public static Item grenade_pulse;
	public static Item grenade_plasma;
	public static Item grenade_tau;
	public static Item grenade_schrabidium;
	public static Item grenade_lemon;
	public static Item grenade_gascan;
	public static Item grenade_kyiv;
	public static Item grenade_mk2;
	public static Item grenade_aschrab;
	public static Item grenade_nuke;
	public static Item grenade_nuclear;
	public static Item grenade_zomg;
	public static Item grenade_black_hole;
	public static Item grenade_cloud;
	public static Item grenade_pink_cloud;
	public static Item ullapool_caber;

	public static Item grenade_if_generic;
	public static Item grenade_if_he;
	public static Item grenade_if_bouncy;
	public static Item grenade_if_sticky;
	public static Item grenade_if_impact;
	public static Item grenade_if_incendiary;
	public static Item grenade_if_toxic;
	public static Item grenade_if_concussion;
	public static Item grenade_if_brimstone;
	public static Item grenade_if_mystery;
	public static Item grenade_if_spark;
	public static Item grenade_if_hopwire;
	public static Item grenade_if_null;

	public static Item grenade_smart;
	public static Item grenade_mirv;
	public static Item grenade_breach;
	public static Item grenade_burst;

	public static Item nuclear_waste_pearl;

	public static Item weaponized_starblaster_cell;

	public static Item bomb_waffle;
	public static Item schnitzel_vegan;
	public static Item cotton_candy;
	public static Item apple_lead;
	public static Item apple_schrabidium;
	public static Item tem_flakes;
	public static Item glowing_stew;
	public static Item balefire_scrambled;
	public static Item balefire_and_ham;
	public static Item lemon;
	public static Item definitelyfood;
	public static Item loops;
	public static Item loop_stew;
	public static Item spongebob_macaroni;
	public static Item fooditem;
	public static Item twinkie;
	public static Item static_sandwich;
	public static Item pudding;
	public static Item pancake;
	public static Item nugget;
	public static Item peas;
	public static Item marshmallow;
	public static Item cheese;
	public static Item quesadilla;
	public static Item glyphid_meat;
	public static Item glyphid_meat_grilled;
	public static Item egg_glyphid;
	public static Item flour;

	public static Item med_ipecac;
	public static Item med_ptsd;
	public static Item med_schizophrenia;

	public static Item canteen_vodka;

	public static Item defuser;
	public static Item reacher;
	public static Item bismuth_tool;
	public static Item meltdown_tool;

	public static Item flame_pony;
	public static Item flame_conspiracy;
	public static Item flame_politics;
	public static Item flame_opinion;

	//public static Item gadget_explosive;
	public static Item early_explosive_lenses;
	public static Item explosive_lenses;
	public static Item gadget_wireing;
	public static Item gadget_core;
	public static Item boy_igniter;
	public static Item boy_propellant;
	public static Item boy_bullet;
	public static Item boy_target;
	public static Item boy_shielding;
	//public static Item man_explosive;
	public static Item man_igniter;
	public static Item man_core;
	public static Item mike_core;
	public static Item mike_deut;
	public static Item mike_cooling_unit;
	public static Item tsar_core;
	public static Item fleija_igniter;
	public static Item fleija_propellant;
	public static Item fleija_core;
	public static Item solinium_igniter;
	public static Item solinium_propellant;
	public static Item solinium_core;
	public static Item n2_charge;
	public static Item egg_balefire_shard;
	public static Item egg_balefire;

	public static Item custom_tnt;
	public static Item custom_nuke;
	public static Item custom_hydro;
	public static Item custom_amat;
	public static Item custom_dirty;
	public static Item custom_schrab;
	public static Item custom_fall;

	@Deprecated public static Item battery_generic;
	@Deprecated public static Item battery_advanced;
	@Deprecated public static Item battery_lithium;
	@Deprecated public static Item battery_schrabidium;
	@Deprecated public static Item battery_red_cell;
	@Deprecated public static Item battery_red_cell_6;
	@Deprecated public static Item battery_red_cell_24;
	@Deprecated public static Item battery_advanced_cell;
	@Deprecated public static Item battery_advanced_cell_4;
	@Deprecated public static Item battery_advanced_cell_12;
	@Deprecated public static Item battery_lithium_cell;
	@Deprecated public static Item battery_lithium_cell_3;
	@Deprecated public static Item battery_lithium_cell_6;
	@Deprecated public static Item battery_schrabidium_cell;
	@Deprecated public static Item battery_schrabidium_cell_2;
	@Deprecated public static Item battery_schrabidium_cell_4;
	@Deprecated public static Item battery_spark_cell_6;
	@Deprecated public static Item battery_spark_cell_25;
	@Deprecated public static Item battery_spark_cell_100;
	@Deprecated public static Item battery_spark_cell_1000;
	@Deprecated public static Item battery_spark_cell_2500;
	@Deprecated public static Item battery_spark_cell_10000;
	@Deprecated public static Item battery_spark_cell_power;

	public static Item battery_pack;
	public static Item battery_creative;
	public static Item cube_power;
	public static Item battery_spark;
	public static Item battery_trixite;
	public static Item core_angel;

	@Deprecated public static Item battery_sc_uranium;
	@Deprecated public static Item battery_sc_technetium;
	@Deprecated public static Item battery_sc_plutonium;
	@Deprecated public static Item battery_sc_polonium;
	@Deprecated public static Item battery_sc_gold;
	@Deprecated public static Item battery_sc_lead;
	@Deprecated public static Item battery_sc_americium;

	public static Item battery_sc;

	public static Item battery_potato;
	public static Item battery_potatos;
	public static Item hev_battery;
	public static Item fusion_core;
	public static Item energy_core;
	public static Item fuse;
	//by using these in crafting table recipes, i'm running the risk of making my recipes too greg-ian (which i don't like)
	//in the event that i forget about the meaning of the word "sparingly", please throw a brick at my head
	public static Item screwdriver;
	public static Item screwdriver_desh;
	public static Item hand_drill;
	public static Item hand_drill_desh;
	public static Item wrench_archineer;
	public static Item chemistry_set;
	public static Item chemistry_set_boron;
	public static Item blowtorch;
	public static Item acetylene_torch;
	public static Item boltgun;
	public static Item overfuse;
	public static Item arc_electrode;
	public static Item arc_electrode_burnt;

	public static Item upgrade_muffler;

	public static Item upgrade_template;
	public static Item upgrade_speed_1;
	public static Item upgrade_speed_2;
	public static Item upgrade_speed_3;
	public static Item upgrade_effect_1;
	public static Item upgrade_effect_2;
	public static Item upgrade_effect_3;
	public static Item upgrade_power_1;
	public static Item upgrade_power_2;
	public static Item upgrade_power_3;
	public static Item upgrade_fortune_1;
	public static Item upgrade_fortune_2;
	public static Item upgrade_fortune_3;
	public static Item upgrade_afterburn_1;
	public static Item upgrade_afterburn_2;
	public static Item upgrade_afterburn_3;
	public static Item upgrade_overdrive_1;
	public static Item upgrade_overdrive_2;
	public static Item upgrade_overdrive_3;
	public static Item upgrade_radius;
	public static Item upgrade_health;
	public static Item upgrade_smelter;
	public static Item upgrade_shredder;
	public static Item upgrade_centrifuge;
	public static Item upgrade_crystallizer;
	public static Item upgrade_nullifier;
	public static Item upgrade_screm;
	public static Item upgrade_gc_speed;
	public static Item upgrade_5g;
	public static Item upgrade_stack;
	public static Item upgrade_ejector;

	public static Item ingot_euphemium;
	public static Item nugget_euphemium;
	public static Item euphemium_helmet;
	public static Item euphemium_plate;
	public static Item euphemium_legs;
	public static Item euphemium_boots;
	public static Item apple_euphemium;
	public static Item watch;

	public static Item goggles;
	public static Item ashglasses;
	public static Item gas_mask;
	public static Item gas_mask_m65;
	public static Item gas_mask_mono;
	public static Item gas_mask_olde;
	public static Item mask_rag;
	public static Item mask_piss;
	public static Item hat;
	public static Item beta;
	public static Item no9;

	public static Item t51_helmet;
	public static Item t51_plate;
	public static Item t51_legs;
	public static Item t51_boots;
	public static Item steamsuit_helmet;
	public static Item steamsuit_plate;
	public static Item steamsuit_legs;
	public static Item steamsuit_boots;
	public static Item dieselsuit_helmet;
	public static Item dieselsuit_plate;
	public static Item dieselsuit_legs;
	public static Item dieselsuit_boots;

	public static Item chainsaw;

	public static Item schrabidium_helmet;
	public static Item schrabidium_plate;
	public static Item schrabidium_legs;
	public static Item schrabidium_boots;
	public static Item titanium_helmet;
	public static Item titanium_plate;
	public static Item titanium_legs;
	public static Item titanium_boots;
	public static Item steel_helmet;
	public static Item steel_plate;
	public static Item steel_legs;
	public static Item steel_boots;
	public static Item alloy_helmet;
	public static Item alloy_plate;
	public static Item alloy_legs;
	public static Item alloy_boots;
	public static Item cmb_helmet;
	public static Item cmb_plate;
	public static Item cmb_legs;
	public static Item cmb_boots;
	public static Item paa_plate;
	public static Item paa_legs;
	public static Item paa_boots;
	public static Item asbestos_helmet;
	public static Item asbestos_plate;
	public static Item asbestos_legs;
	public static Item asbestos_boots;
	public static Item security_helmet;
	public static Item security_plate;
	public static Item security_legs;
	public static Item security_boots;
	public static Item cobalt_helmet;
	public static Item cobalt_plate;
	public static Item cobalt_legs;
	public static Item cobalt_boots;
	public static Item starmetal_helmet;
	public static Item starmetal_plate;
	public static Item starmetal_legs;
	public static Item starmetal_boots;
	public static Item dnt_helmet;
	public static Item dnt_plate;
	public static Item dnt_legs;
	public static Item dnt_boots;
	public static Item ajr_helmet;
	public static Item ajr_plate;
	public static Item ajr_legs;
	public static Item ajr_boots;
	public static Item ajro_helmet;
	public static Item ajro_plate;
	public static Item ajro_legs;
	public static Item ajro_boots;
	public static Item rpa_helmet;
	public static Item rpa_plate;
	public static Item rpa_legs;
	public static Item rpa_boots;
	public static Item ncrpa_helmet;
	public static Item ncrpa_plate;
	public static Item ncrpa_legs;
	public static Item ncrpa_boots;
	public static Item bismuth_helmet;
	public static Item bismuth_plate;
	public static Item bismuth_legs;
	public static Item bismuth_boots;
	public static Item bj_helmet;
	public static Item bj_plate;
	public static Item bj_plate_jetpack;
	public static Item bj_legs;
	public static Item bj_boots;
	public static Item envsuit_helmet;
	public static Item envsuit_plate;
	public static Item envsuit_legs;
	public static Item envsuit_boots;
	public static Item hev_helmet;
	public static Item hev_plate;
	public static Item hev_legs;
	public static Item hev_boots;
	public static Item fau_helmet;
	public static Item fau_plate;
	public static Item fau_legs;
	public static Item fau_boots;
	public static Item dns_helmet;
	public static Item dns_plate;
	public static Item dns_legs;
	public static Item dns_boots;
	public static Item taurun_helmet;
	public static Item taurun_plate;
	public static Item taurun_legs;
	public static Item taurun_boots;
	public static Item trenchmaster_helmet;
	public static Item trenchmaster_plate;
	public static Item trenchmaster_legs;
	public static Item trenchmaster_boots;
	public static Item zirconium_legs;
	public static Item robes_helmet;
	public static Item robes_plate;
	public static Item robes_legs;
	public static Item robes_boots;

	public static Item jetpack_boost;
	public static Item jetpack_break;
	public static Item jetpack_fly;
	public static Item jetpack_vector;
	public static Item wings_limp;
	public static Item wings_murk;
	public static Item oxy_plss;

	public static Item jackt;
	public static Item jackt2;

	public static Item schrabidium_sword;
	public static Item schrabidium_pickaxe;
	public static Item schrabidium_axe;
	public static Item schrabidium_shovel;
	public static Item schrabidium_hoe;
	public static Item titanium_sword;
	public static Item titanium_pickaxe;
	public static Item titanium_axe;
	public static Item titanium_shovel;
	public static Item titanium_hoe;
	public static Item steel_sword;
	public static Item steel_pickaxe;
	public static Item steel_axe;
	public static Item steel_shovel;
	public static Item steel_hoe;
	public static Item alloy_sword;
	public static Item alloy_pickaxe;
	public static Item alloy_axe;
	public static Item alloy_shovel;
	public static Item alloy_hoe;
	public static Item cmb_sword;
	public static Item cmb_pickaxe;
	public static Item cmb_axe;
	public static Item cmb_shovel;
	public static Item cmb_hoe;
	public static Item elec_sword;
	public static Item elec_pickaxe;
	public static Item elec_axe;
	public static Item elec_shovel;
	public static Item desh_sword;
	public static Item desh_pickaxe;
	public static Item desh_axe;
	public static Item desh_shovel;
	public static Item desh_hoe;
	public static Item cobalt_sword;
	public static Item cobalt_pickaxe;
	public static Item cobalt_axe;
	public static Item cobalt_shovel;
	public static Item cobalt_hoe;
	public static Item cobalt_decorated_sword;
	public static Item cobalt_decorated_pickaxe;
	public static Item cobalt_decorated_axe;
	public static Item cobalt_decorated_shovel;
	public static Item cobalt_decorated_hoe;
	public static Item starmetal_sword;
	public static Item starmetal_pickaxe;
	public static Item starmetal_axe;
	public static Item starmetal_shovel;
	public static Item starmetal_hoe;
	public static Item smashing_hammer;
	public static Item centri_stick;
	public static Item drax;
	public static Item drax_mk2;
	public static Item drax_mk3;
	public static Item bismuth_pickaxe;
	public static Item bismuth_axe;
	public static Item volcanic_pickaxe;
	public static Item volcanic_axe;
	public static Item chlorophyte_pickaxe;
	public static Item chlorophyte_axe;
	public static Item mese_pickaxe;
	public static Item mese_axe;
	public static Item dnt_sword;
	public static Item dwarven_pickaxe;

	public static Item meteorite_sword;
	public static Item meteorite_sword_seared;
	public static Item meteorite_sword_reforged;
	public static Item meteorite_sword_hardened;
	public static Item meteorite_sword_alloyed;
	public static Item meteorite_sword_machined;
	public static Item meteorite_sword_treated;
	public static Item meteorite_sword_etched;
	public static Item meteorite_sword_bred;
	public static Item meteorite_sword_irradiated;
	public static Item meteorite_sword_fused;
	public static Item meteorite_sword_baleful;

	public static Item matchstick;
	public static Item balefire_and_steel;

	public static Item mask_of_infamy;

	public static Item schrabidium_hammer;
	public static Item shimmer_sledge;
	public static Item shimmer_axe;
	public static Item bottle_opener;
	public static Item pch; //for compat please do not hit me
	public static Item wood_gavel;
	public static Item lead_gavel;
	public static Item diamond_gavel;
	public static Item mese_gavel;

	public static Item crowbar;

	public static Item wrench_flipped;
	public static Item memespoon;

	public static Item saw;
	public static Item bat;
	public static Item bat_nail;
	public static Item golf_club;
	public static Item pipe_rusty;
	public static Item pipe_lead;
	public static Item reer_graar;
	public static Item stopsign;
	public static Item sopsign;
	public static Item chernobylsign;

	public static Item crystal_horn;
	public static Item crystal_charred;

	public static Item attachment_mask;
	public static Item attachment_mask_mono;
	public static Item back_tesla;
	public static Item servo_set;
	public static Item servo_set_desh;
	public static Item pads_rubber;
	public static Item pads_slime;
	public static Item pads_static;
	public static Item cladding_paint;
	public static Item cladding_rubber;
	public static Item cladding_lead;
	public static Item cladding_desh;
	public static Item cladding_ghiorsium;
	public static Item cladding_iron;
	public static Item cladding_obsidian;
	public static Item insert_kevlar;
	public static Item insert_sapi;
	public static Item insert_esapi;
	public static Item insert_xsapi;
	public static Item insert_steel;
	public static Item insert_du;
	public static Item insert_polonium;
	public static Item insert_ghiorsium;
	public static Item insert_cmb;
	public static Item insert_era;
	public static Item insert_yharonite;
	public static Item insert_doxium;
	public static Item armor_polish;
	public static Item bandaid;
	public static Item serum;
	public static Item quartz_plutonium;
	public static Item morning_glory;
	public static Item lodestone;
	public static Item horseshoe_magnet;
	public static Item industrial_magnet;
	public static Item bathwater;
	public static Item bathwater_mk2;
	public static Item spider_milk;
	public static Item ink;
	public static Item heart_piece;
	public static Item heart_container;
	public static Item heart_booster;
	public static Item heart_fab;
	public static Item black_diamond;
	public static Item wd40;
	public static Item scrumpy;
	public static Item wild_p;
	public static Item shackles;
	public static Item injector_5htp;
	public static Item injector_knife;
	public static Item medal_liquidator;
	public static Item v1;
	public static Item protection_charm;
	public static Item meteor_charm;
	public static Item neutrino_lens;
	public static Item gas_tester;
	public static Item defuser_gold;
	public static Item ballistic_gauntlet;
	public static Item night_vision;
	public static Item card_aos;
	public static Item card_qos;
	public static Item australium_iii;
	public static Item armor_battery;
	public static Item armor_battery_mk2;
	public static Item armor_battery_mk3;
	public static Item flippers;
	public static Item heavy_boots;

	public static Item hazmat_helmet;
	public static Item hazmat_plate;
	public static Item hazmat_legs;
	public static Item hazmat_boots;
	public static Item hazmat_helmet_red;
	public static Item hazmat_plate_red;
	public static Item hazmat_legs_red;
	public static Item hazmat_boots_red;
	public static Item hazmat_helmet_grey;
	public static Item hazmat_plate_grey;
	public static Item hazmat_legs_grey;
	public static Item hazmat_boots_grey;
	public static Item liquidator_helmet;
	public static Item liquidator_plate;
	public static Item liquidator_legs;
	public static Item liquidator_boots;

	public static Item hazmat_paa_helmet;
	public static Item hazmat_paa_plate;
	public static Item hazmat_paa_legs;
	public static Item hazmat_paa_boots;

	public static Item rebar_placer;

	public static Item wand;
	public static Item wand_s;
	public static Item wand_d;
	public static Item wand_time;

	public static Item structure_single;
	public static Item structure_solid;
	public static Item structure_pattern;
	public static Item structure_randomized;
	public static Item structure_randomly;
	public static Item structure_custommachine;

	public static Item rod_of_discord;

	public static Item cape_radiation;
	public static Item cape_gasmask;
	public static Item cape_schrabidium;
	public static Item cape_hidden;
	/*public static Item cape_hbm;
	public static Item cape_dafnik;
	public static Item cape_lpkukin;
	public static Item cape_vertice;
	public static Item cape_codered_;
	public static Item cape_ayy;
	public static Item cape_nostalgia;*/

	public static Item nuke_starter_kit;
	public static Item nuke_advanced_kit;
	public static Item nuke_commercially_kit;
	public static Item nuke_electric_kit;
	public static Item gadget_kit;
	public static Item boy_kit;
	public static Item man_kit;
	public static Item mike_kit;
	public static Item tsar_kit;
	public static Item multi_kit;
	public static Item custom_kit;
	public static Item grenade_kit;
	public static Item fleija_kit;
	public static Item prototype_kit;
	public static Item missile_kit;
	public static Item euphemium_kit;
	public static Item solinium_kit;
	public static Item hazmat_kit;
	public static Item hazmat_red_kit;
	public static Item hazmat_grey_kit;
	public static Item kit_custom;

	public static Item toolbox;

	public static Item loot_10;
	public static Item loot_15;
	public static Item loot_misc;

	public static Item ammo_container;

	public static Item igniter;
	public static Item detonator;
	public static Item detonator_multi;
	public static Item detonator_laser;
	public static Item detonator_deadman;
	public static Item detonator_de;
	public static Item bomb_caller;
	public static Item meteor_remote;
	public static Item anchor_remote;
	public static Item remote;
	//public static Item turret_control;
	public static Item turret_chip;
	//public static Item turret_biometry;

	public static Item spawn_chopper;
	public static Item spawn_worm;
	public static Item spawn_ufo;
	public static Item spawn_duck;
	public static Item spawn_angel;

	public static Item key;
	public static Item key_red;
	public static Item key_red_cracked;
	public static Item key_kit;
	public static Item key_fake;
	public static Item pin;
	public static Item padlock_rusty;
	public static Item padlock;
	public static Item padlock_reinforced;
	public static Item padlock_unbreakable;

	public static Item mech_key;

	public static Item bucket_mud;
	public static Item bucket_acid;
	public static Item bucket_toxic;
	public static Item bucket_schrabidic_acid;
	public static Item bucket_sulfuric_acid;
	public static Item bucket_mercury;
	public static Item bucket_bromine;
	public static Item bucket_ccl;

	public static Item door_metal;
	public static Item door_office;
	public static Item door_bunker;
	public static Item door_red;

	public static Item fence_gate;

	public static Item record_lc;
	public static Item record_ss;
	public static Item record_vc;
	public static Item record_gs;
	public static Item record_gp;
	public static Item record_el;
	public static Item record_glass;

	public static Item book_guide;
	public static Item book_lore;
	public static Item holotape_image;
	public static Item holotape_damaged;
	public static Item clay_tablet;

	public static Item polaroid;
	public static Item glitch;
	public static Item book_secret;
	public static Item book_of_;
	public static Item page_of_;
	public static Item book_lemegeton;
	public static Item burnt_bark;

	public static Item chlorine1;
	public static Item chlorine2;
	public static Item chlorine3;
	public static Item chlorine4;
	public static Item chlorine5;
	public static Item chlorine6;
	public static Item chlorine7;
	public static Item chlorine8;
	public static Item pc1;
	public static Item pc2;
	public static Item pc3;
	public static Item pc4;
	public static Item pc5;
	public static Item pc6;
	public static Item pc7;
	public static Item pc8;
	public static Item cloud1;
	public static Item cloud2;
	public static Item cloud3;
	public static Item cloud4;
	public static Item cloud5;
	public static Item cloud6;
	public static Item cloud7;
	public static Item cloud8;
	public static Item orange1;
	public static Item orange2;
	public static Item orange3;
	public static Item orange4;
	public static Item orange5;
	public static Item orange6;
	public static Item orange7;
	public static Item orange8;

	public static Item nothing;
	public static Item broken_item;

	public static Item achievement_icon;

	public static Item mysteryshovel;
	public static Item memory;

	public static Item conveyor_wand;

	public static Item swarm_member;

	public static void initializeItem() {

		redstone_sword = NativeItemTextureRegistry.texture(new RedstoneSword(ToolMaterial.STONE).setTranslationKey("redstone_sword").setCreativeTab(CreativeTabs.COMBAT), RefStrings.MODID + ":redstone_sword");
		big_sword = NativeItemTextureRegistry.texture(new BigSword(ToolMaterial.DIAMOND).setTranslationKey("big_sword").setCreativeTab(CreativeTabs.COMBAT), RefStrings.MODID + ":big_sword");
		butter_sword = NativeItemTextureRegistry.texture(new BSword(ToolMaterial.WOOD).setTranslationKey("butter_sword").setCreativeTab(CreativeTabs.COMBAT), RefStrings.MODID + ":budder_sword");

		ingot_th232 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_th232").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_th232");
		ingot_uranium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_uranium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_uranium");
		ingot_u233 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_u233").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_u233");
		ingot_u235 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_u235").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_u235");
		ingot_u238 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_u238").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_u238");
		ingot_u238m2 = NativeItemTextureRegistry.texture(new ItemUnstable(350, 200).setTranslationKey("ingot_u238m2").setCreativeTab(null), RefStrings.MODID + ":ingot_u238m2");
		ingot_plutonium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_plutonium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_plutonium");
		ingot_pu238 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_pu238").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_pu238");
		ingot_pu239 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_pu239").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_pu239");
		ingot_pu240 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_pu240").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_pu240");
		ingot_pu241 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_pu241").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_pu241");
		ingot_pu_mix = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_pu_mix").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_pu_mix");
		ingot_am241 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_am241").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_am241");
		ingot_am242 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_am242").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_am242");
		ingot_am_mix = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_am_mix").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_am_mix");
		ingot_cm242 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_cm242").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_cm242");
		ingot_cm243 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_cm243").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_cm243");
		ingot_cm244 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_cm244").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_cm244");
		ingot_cm245 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_cm245").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_cm245");
		ingot_cm246 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_cm246").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_cm246");
		ingot_cm247 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_cm247").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_cm247");
		ingot_cm_fuel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_cm_fuel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_cm_fuel");
		ingot_cm_mix = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_cm_mix").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_cm_mix");
		ingot_bk247 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_bk247").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_bk247");
		ingot_cf251 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_cf251").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID +":ingot_cf251");
		ingot_cf252 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_cf252").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_cf252");


		ingot_es253 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_es253").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_es253");
		ingot_es255 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_es255").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_es255");
		ingot_neptunium = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("ingot_neptunium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_neptunium");
		ingot_polonium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_polonium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_polonium");
		ingot_technetium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_technetium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_technetium");
		ingot_co60 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_co60").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_co60");
		ingot_sr90 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_sr90").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_sr90");
		ingot_au198 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_au198").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_au198");
		ingot_pb209 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_pb209").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_pb209");
		ingot_ra226 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_ra226").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_ra226");
		ingot_titanium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_titanium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_titanium");
		ingot_nickel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_nickel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_nickel");
		ingot_cobalt = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_cobalt").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_cobalt");
		ingot_boron = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_boron").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_boron");
		ingot_graphite = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_graphite").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_graphite");
		ingot_firebrick = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_firebrick").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_firebrick");
		ingot_smore = NativeItemTextureRegistry.texture(new ItemFood(10, 20F, false).setTranslationKey("ingot_smore").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_smore");
		sulfur = NativeItemTextureRegistry.texture(new Item().setTranslationKey("sulfur").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":sulfur");
		nitra = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nitra").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nitra");
		nitra_small = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nitra_small").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nitra_small");

		ingot_uranium_fuel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_uranium_fuel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_uranium_fuel");
		ingot_plutonium_fuel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_plutonium_fuel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_plutonium_fuel");
		ingot_neptunium_fuel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_neptunium_fuel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_neptunium_fuel");
		ingot_mox_fuel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_mox_fuel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_mox_fuel");
		ingot_americium_fuel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_americium_fuel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_americium_fuel");
		ingot_schrabidium_fuel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_schrabidium_fuel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_schrabidium_fuel");
		ingot_thorium_fuel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_thorium_fuel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_thorium_fuel");
		nugget_uranium_fuel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_uranium_fuel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_uranium_fuel");
		nugget_thorium_fuel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_thorium_fuel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_thorium_fuel");
		nugget_plutonium_fuel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_plutonium_fuel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_plutonium_fuel");
		nugget_neptunium_fuel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_neptunium_fuel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_neptunium_fuel");
		nugget_mox_fuel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_mox_fuel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_mox_fuel");
		nugget_americium_fuel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_americium_fuel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_americium_fuel");
		nugget_schrabidium_fuel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_schrabidium_fuel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_schrabidium_fuel");
		ingot_advanced_alloy = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_advanced_alloy").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_advanced_alloy");
		ingot_tcalloy = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_tcalloy").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_tcalloy");
		ingot_cdalloy = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_cdalloy").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_cdalloy");
		ingot_bismuth_bronze = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_bismuth_bronze").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_bismuth_bronze");
		ingot_arsenic_bronze = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_arsenic_bronze").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_arsenic_bronze");
		ingot_bscco = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_bscco").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_bscco");
		ingot_menthol = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_menthol").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_menthol");
		nugget_menthol = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_menthol").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_menthol");



		niter = NativeItemTextureRegistry.texture(new Item().setTranslationKey("niter").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":salpeter");
		ingot_copper = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_copper").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_copper");
		ingot_red_copper = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_red_copper").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_red_copper");
		billet_red_copper = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_red_copper").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_red_copper");
		ingot_tungsten = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_tungsten").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_tungsten");
		ingot_aluminium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_aluminium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_aluminium");
		fluorite = NativeItemTextureRegistry.texture(new Item().setTranslationKey("fluorite").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":fluorite");
		ingot_beryllium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_beryllium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_beryllium");
		ingot_steel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_steel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_steel");
		plate_steel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("plate_steel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_steel");
		ingot_stainless = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_stainless").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_stainless");
		plate_stainless = NativeItemTextureRegistry.texture(new Item().setTranslationKey("plate_stainless").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_stainless");
		plate_iron = NativeItemTextureRegistry.texture(new Item().setTranslationKey("plate_iron").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_iron");
		ingot_lead = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_lead").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_lead");
		plate_lead = NativeItemTextureRegistry.texture(new Item().setTranslationKey("plate_lead").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_lead");
		plate_dura_steel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("plate_dura_steel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_dura_steel");
		ingot_schraranium = NativeItemTextureRegistry.texture(new ItemSchraranium().setTranslationKey("ingot_schraranium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_schraranium");
		ingot_schrabidium = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.RARE).setTranslationKey("ingot_schrabidium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_schrabidium");
		ingot_schrabidate = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.RARE).setTranslationKey("ingot_schrabidate").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_schrabidate");
		plate_schrabidium = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.RARE).setTranslationKey("plate_schrabidium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_schrabidium");
		plate_copper = NativeItemTextureRegistry.texture(new Item().setTranslationKey("plate_copper").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_copper");
		plate_gold = NativeItemTextureRegistry.texture(new Item().setTranslationKey("plate_gold").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_gold");
		plate_advanced_alloy = NativeItemTextureRegistry.texture(new Item().setTranslationKey("plate_advanced_alloy").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_advanced_alloy");
		lithium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("lithium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":lithium");
		ingot_zirconium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_zirconium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_zirconium");
		ingot_semtex = NativeItemTextureRegistry.texture(new ItemLemon(4, 5, true).setTranslationKey("ingot_semtex").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_semtex");
		ingot_c4 = NativeItemTextureRegistry.texture(new ItemLemon(4, 5, true).setTranslationKey("ingot_c4").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_c4");
		ingot_phosphorus = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_phosphorus").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_phosphorus");
		coil_advanced_alloy = NativeItemTextureRegistry.texture(new Item().setTranslationKey("coil_advanced_alloy").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":coil_advanced_alloy");
		coil_advanced_torus = NativeItemTextureRegistry.texture(new Item().setTranslationKey("coil_advanced_torus").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":coil_advanced_torus");
		ingot_magnetized_tungsten = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_magnetized_tungsten").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_magnetized_tungsten");
		ingot_combine_steel = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("ingot_combine_steel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_combine_steel");
		plate_mixed = NativeItemTextureRegistry.texture(new Item().setTranslationKey("plate_mixed").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_mixed");
		plate_paa = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.UNCOMMON).setTranslationKey("plate_paa").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_paa");
		pipes_steel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("pipes_steel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":pipes_steel");
		drill_titanium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("drill_titanium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":drill_titanium");
		plate_dalekanium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("plate_dalekanium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_dalekanium");
		plate_euphemium = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.EPIC).setTranslationKey("plate_euphemium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_euphemium");
		bolt = NativeItemTextureRegistry.texture(new ItemAutogen(MaterialShapes.BOLT).oun("boltntm").setTranslationKey("bolt").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":bolt");
		bolt_spike = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("bolt_spike").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":bolt_spike");
		plate_polymer = NativeItemTextureRegistry.texture(new Item().setTranslationKey("plate_polymer").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_polymer");
		plate_kevlar = NativeItemTextureRegistry.texture(new Item().setTranslationKey("plate_kevlar").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_kevlar");
		plate_dineutronium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("plate_dineutronium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_dineutronium");
		plate_desh = NativeItemTextureRegistry.texture(new Item().setTranslationKey("plate_desh").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_desh");
		plate_bismuth = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("plate_bismuth").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_bismuth");
		ingot_solinium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_solinium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_solinium");
		nugget_solinium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_solinium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_solinium");
		photo_panel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("photo_panel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":photo_panel");
		beryllium_mirror = NativeItemTextureRegistry.texture(new Item().setTranslationKey("beryllium_mirror").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":beryllium_mirror");
		sat_base = NativeItemTextureRegistry.texture(new Item().setTranslationKey("sat_base").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":sat_base");
		thruster_nuclear = NativeItemTextureRegistry.texture(new Item().setTranslationKey("thruster_nuclear").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":thruster_nuclear");
		safety_fuse = NativeItemTextureRegistry.texture(new Item().setTranslationKey("safety_fuse").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":safety_fuse");
		part_generic = NativeItemTextureRegistry.texture(new ItemGenericPart().setTranslationKey("part_generic").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":part_generic");
		item_expensive = NativeItemTextureRegistry.texture(new ItemExpensive().setTranslationKey("item_expensive").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":item_expensive");
		item_secret = NativeItemTextureRegistry.texture(new ItemEnumMulti(EnumSecretType.class, true, true).setTranslationKey("item_secret").setCreativeTab(null), RefStrings.MODID + ":item_secret");
		ingot_metal = NativeItemTextureRegistry.texture(new ItemEnumMulti(EnumIngotMetal.class, true, true).setTranslationKey("ingot_metal").setCreativeTab(null), RefStrings.MODID + ":ingot_metal");
		chemical_dye = NativeItemTextureRegistry.texture(new ItemChemicalDye().setTranslationKey("chemical_dye").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":chemical_dye");
		crayon = NativeItemTextureRegistry.texture(new ItemCrayon().setTranslationKey("crayon").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crayon");
		divine_shard = NativeItemTextureRegistry.texture(new Item().setTranslationKey("divine_shard").setCreativeTab(null), RefStrings.MODID + ":divine_shard");

		undefined = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("undefined").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":undefined");

		billet_uranium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_uranium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_uranium");
		billet_u233 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_u233").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_u233");
		billet_u235 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_u235").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_u235");
		billet_u238 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_u238").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_u238");
		billet_uzh = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_uzh").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_uzh");
		billet_th232 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_th232").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_th232");
		billet_plutonium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_plutonium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_plutonium");
		billet_pu238 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_pu238").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_pu238");
		billet_pu239 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_pu239").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_pu239");
		billet_pu240 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_pu240").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_pu240");
		billet_pu241 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_pu241").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_pu241");
		billet_pu_mix = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_pu_mix").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_pu_mix");
		billet_am241 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_am241").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_am241");
		billet_am242 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_am242").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_am242");
		billet_am_mix = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_am_mix").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_am_mix");
		billet_bk247 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_bk247").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_bk247");
		billet_cm242 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_cm242").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_cm242");
		billet_cm243 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_cm243").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_cm243");
		billet_cm244 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_cm244").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_cm244");
		billet_cm245 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_cm245").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_cm245");
		billet_cm246 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_cm246").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_cm246");
		billet_cm247 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_cm247").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_cm247");
		billet_cf252 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_cf252").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_cf252");

		billet_es253 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_es253").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_es253");
		billet_cf251 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_cf251").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID +":billet_cf251");
		billet_cm_mix = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_cm_mix").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_cm_mix");
		billet_cm_fuel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_cm_fuel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_cm_fuel");
		billet_neptunium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_neptunium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_neptunium");
		billet_polonium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_polonium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_polonium");
		billet_technetium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_technetium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_technetium");
		billet_cobalt = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_cobalt").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_cobalt");
		billet_co60 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_co60").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_co60");
		billet_sr90 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_sr90").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_sr90");
		billet_au198 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_au198").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_au198");
		billet_pb209 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_pb209").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_pb209");
		billet_ra226 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_ra226").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_ra226");
		billet_actinium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_actinium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_actinium");
		billet_schrabidium = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.RARE).setTranslationKey("billet_schrabidium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_schrabidium");
		billet_solinium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_solinium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_solinium");
		billet_gh336 = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.EPIC).setTranslationKey("billet_gh336").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_gh336");
		billet_australium = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.UNCOMMON).setTranslationKey("billet_australium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_australium");
		billet_australium_lesser = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.UNCOMMON).setTranslationKey("billet_australium_lesser").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_australium_lesser");
		billet_australium_greater = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.UNCOMMON).setTranslationKey("billet_australium_greater").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_australium_greater");
		billet_uranium_fuel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_uranium_fuel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_uranium_fuel");
		billet_thorium_fuel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_thorium_fuel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_thorium_fuel");
		billet_plutonium_fuel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_plutonium_fuel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_plutonium_fuel");
		billet_neptunium_fuel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_neptunium_fuel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_neptunium_fuel");
		billet_mox_fuel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_mox_fuel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_mox_fuel");
		billet_americium_fuel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_americium_fuel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_americium_fuel");
		billet_les = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_les").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_les");
		billet_schrabidium_fuel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_schrabidium_fuel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_schrabidium_fuel");
		billet_hes = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_hes").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_hes");
		billet_po210be = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_po210be").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_po210be");
		billet_ra226be = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_ra226be").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_ra226be");
		billet_pu238be = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_pu238be").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_pu238be");
		billet_beryllium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_beryllium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_beryllium");
		billet_bismuth = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_bismuth").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_bismuth");
		billet_zirconium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_zirconium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_zirconium");
		billet_yharonite = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_yharonite").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_yharonite");
		billet_balefire_gold = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.UNCOMMON).setTranslationKey("billet_balefire_gold").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_balefire_gold");
		billet_flashlead = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.UNCOMMON).setTranslationKey("billet_flashlead").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_flashlead");
		billet_zfb_bismuth = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_zfb_bismuth").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_zfb_bismuth");
		billet_zfb_pu241 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_zfb_pu241").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_zfb_pu241");
		billet_zfb_am_mix = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_zfb_am_mix").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_zfb_am_mix");
		billet_nuclear_waste = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_nuclear_waste").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_nuclear_waste");
		billet_menthol = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_menthol").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_menthol");


		ball_resin = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("ball_resin").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ball_resin");
		ingot_dura_steel = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("ingot_dura_steel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_dura_steel");
		ingot_polymer = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("ingot_polymer").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_polymer");
		ingot_bakelite = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("ingot_bakelite").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_bakelite");
		ingot_biorubber = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("ingot_biorubber").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_biorubber");
		ingot_rubber = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("ingot_rubber").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_rubber");
		ingot_pc = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("ingot_pc").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_pc");
		ingot_pvc = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("ingot_pvc").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_pvc");
		ingot_desh = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("ingot_desh").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_desh");
		nugget_desh = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("nugget_desh").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_desh");
		ingot_dineutronium = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("ingot_dineutronium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_dineutronium");
		nugget_dineutronium = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("nugget_dineutronium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_dineutronium");
		powder_dineutronium = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("powder_dineutronium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_dineutronium");
		ingot_starmetal = NativeItemTextureRegistry.texture(new ItemStarmetal().setTranslationKey("ingot_starmetal").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_starmetal");
		ingot_gunmetal = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_gunmetal").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_gunmetal");
		plate_gunmetal = NativeItemTextureRegistry.texture(new Item().setTranslationKey("plate_gunmetal").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_gunmetal");
		ingot_weaponsteel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_weaponsteel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_gunsteel");
		plate_weaponsteel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("plate_weaponsteel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_gunsteel");
		ingot_saturnite = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.RARE).setTranslationKey("ingot_saturnite").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_saturnite");
		plate_saturnite = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.RARE).setTranslationKey("plate_saturnite").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_saturnite");
		ingot_ferrouranium = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("ingot_ferrouranium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_ferrouranium");
		ingot_fiberglass = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("ingot_fiberglass").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_fiberglass");
		ingot_asbestos = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("ingot_asbestos").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_asbestos");
		powder_asbestos = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("powder_asbestos").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_asbestos");
		ingot_electronium = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("ingot_electronium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_electronium");
		ingot_gwenium = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("ingot_gwenium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_gwenium");
		nugget_zirconium = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("nugget_zirconium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_zirconium");
		nugget_nickel = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("nugget_nickel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_nickel");
		nugget_mercury = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_mercury_tiny").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_mercury_tiny");
		ingot_mercury = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("nugget_mercury").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_mercury");
		bottle_mercury = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("bottle_mercury").setContainerItem(Items.GLASS_BOTTLE).setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":bottle_mercury");
		ingot_calcium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_calcium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_calcium");
		powder_calcium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_calcium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_calcium");
		ingot_cadmium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_cadmium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_cadmium");
		powder_cadmium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_cadmium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_cadmium");
		powder_bismuth = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_bismuth").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_bismuth");
		ingot_mud = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_mud").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_mud");
		ingot_magma = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_magma").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_magma");
		ingot_cft = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_cft").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_cft");
		stick_pvc = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("stick_pvc").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":stick_pvc");
		stick_vinyl = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("stick_vinyl").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":stick_vinyl");

		ore_byproduct = NativeItemTextureRegistry.texture(new ItemByproduct().setTranslationKey("ore_byproduct").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":byproduct");

		crystal_mineral = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("crystal_mineral").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_mineral");
		crystal_cleaned = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("crystal_cleaned").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_mineralcf");
		mineral_dust = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("mineral_dust").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_mineral");
		mineral_fragment = new ItemMineralOre().setTranslationKey("mineral_fragment").setCreativeTab(MainRegistry.partsTab);

		ore_bedrock = NativeItemTextureRegistry.texture(new ItemBedrockOre().setTranslationKey("ore_bedrock").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ore_bedrock");
		ore_centrifuged = NativeItemTextureRegistry.texture(new ItemBedrockOre().setTranslationKey("ore_centrifuged").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ore_centrifuged");
		ore_cleaned = NativeItemTextureRegistry.texture(new ItemBedrockOre().setTranslationKey("ore_cleaned").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ore_cleaned");
		ore_separated = NativeItemTextureRegistry.texture(new ItemBedrockOre().setTranslationKey("ore_separated").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ore_separated");
		ore_purified = NativeItemTextureRegistry.texture(new ItemBedrockOre().setTranslationKey("ore_purified").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ore_purified");
		ore_nitrated = NativeItemTextureRegistry.texture(new ItemBedrockOre().setTranslationKey("ore_nitrated").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ore_nitrated");
		ore_nitrocrystalline = NativeItemTextureRegistry.texture(new ItemBedrockOre().setTranslationKey("ore_nitrocrystalline").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ore_nitrocrystalline");
		ore_deepcleaned = NativeItemTextureRegistry.texture(new ItemBedrockOre().setTranslationKey("ore_deepcleaned").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ore_deepcleaned");
		ore_seared = NativeItemTextureRegistry.texture(new ItemBedrockOre().setTranslationKey("ore_seared").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ore_seared");
		//ore_radcleaned = new ItemBedrockOre().setUnlocalizedName("ore_radcleaned").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ore_radcleaned");
		ore_enriched = NativeItemTextureRegistry.texture(new ItemBedrockOre().setTranslationKey("ore_enriched").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ore_enriched");
		bedrock_ore_base = NativeItemTextureRegistry.texture(new ItemBedrockOreBase().setTranslationKey("bedrock_ore_base").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":bedrock_ore_new");
		bedrock_ore = NativeItemTextureRegistry.texture(new ItemBedrockOreNew().setTranslationKey("bedrock_ore").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":bedrock_ore_new");
		bedrock_ore_fragment = NativeItemTextureRegistry.texture(new ItemAutogen(MaterialShapes.FRAGMENT).aot(Mats.MAT_BISMUTH, "bedrock_ore_fragment_bismuth").setTranslationKey("bedrock_ore_fragment").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":bedrock_ore_fragment");

		ingot_lanthanium = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("ingot_lanthanium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_lanthanium");
		nugget_lanthanium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_lanthanium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_lanthanium");
		ingot_actinium = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("ingot_actinium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_actinium");

		ingot_meteorite = NativeItemTextureRegistry.texture(new ItemHot(200).setTranslationKey("ingot_meteorite").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_meteorite");
		ingot_meteorite_forged = NativeItemTextureRegistry.texture(new ItemHot(200).setTranslationKey("ingot_meteorite_forged").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_meteorite_forged");
		blade_meteorite = NativeItemTextureRegistry.texture(new ItemHot(200).setTranslationKey("blade_meteorite").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":blade_meteorite");
		ingot_steel_dusted = NativeItemTextureRegistry.texture(new ItemHotDusted(200).setTranslationKey("ingot_steel_dusted").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_steel_dusted");
		ingot_chainsteel = NativeItemTextureRegistry.texture(new ItemHot(100).setTranslationKey("ingot_chainsteel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_chainsteel");

		plate_armor_titanium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("plate_armor_titanium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_armor_titanium");
		plate_armor_ajr = NativeItemTextureRegistry.texture(new Item().setTranslationKey("plate_armor_ajr").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_armor_ajr");
		plate_armor_hev = NativeItemTextureRegistry.texture(new Item().setTranslationKey("plate_armor_hev").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_armor_hev");
		plate_armor_lunar = NativeItemTextureRegistry.texture(new Item().setTranslationKey("plate_armor_lunar").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_armor_lunar");
		plate_armor_fau = NativeItemTextureRegistry.texture(new Item().setTranslationKey("plate_armor_fau").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_armor_fau");
		plate_armor_dnt = NativeItemTextureRegistry.texture(new Item().setTranslationKey("plate_armor_dnt").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_armor_dnt");

		oil_tar = NativeItemTextureRegistry.texture(new ItemEnumMulti(EnumTarType.class, true, true).setTranslationKey("oil_tar").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":oil_tar");
		solid_fuel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("solid_fuel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":solid_fuel");
		solid_fuel_presto = NativeItemTextureRegistry.texture(new Item().setTranslationKey("solid_fuel_presto").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":solid_fuel_presto");
		solid_fuel_presto_triplet = NativeItemTextureRegistry.texture(new Item().setTranslationKey("solid_fuel_presto_triplet").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":solid_fuel_presto_triplet");
		solid_fuel_bf = NativeItemTextureRegistry.texture(new Item().setTranslationKey("solid_fuel_bf").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":solid_fuel_bf");
		solid_fuel_presto_bf = NativeItemTextureRegistry.texture(new Item().setTranslationKey("solid_fuel_presto_bf").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":solid_fuel_presto_bf");
		solid_fuel_presto_triplet_bf = NativeItemTextureRegistry.texture(new Item().setTranslationKey("solid_fuel_presto_triplet_bf").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":solid_fuel_presto_triplet_bf");
		rocket_fuel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("rocket_fuel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":rocket_fuel");
		coke = NativeItemTextureRegistry.texture(new ItemEnumMulti(EnumCokeType.class, true, true).setTranslationKey("coke").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":coke");
		powder_coke = NativeItemTextureRegistry.texture(new ItemEnumMulti(EnumCokeType.class, true, true).setTranslationKey("powder_coke").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_coke");
		lignite = NativeItemTextureRegistry.texture(new Item().setTranslationKey("lignite").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":lignite");
		briquette = new ItemEnumMulti(EnumBriquetteType.class, true, true).setTranslationKey("briquette").setCreativeTab(MainRegistry.partsTab);
		powder_lignite = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_lignite").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_lignite");
		coal_infernal = NativeItemTextureRegistry.texture(new Item().setTranslationKey("coal_infernal").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":coal_infernal");
		cinnebar = NativeItemTextureRegistry.texture(new Item().setTranslationKey("cinnebar").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":cinnebar");
		woodemium_briquette = NativeItemTextureRegistry.texture(new Item().setTranslationKey("woodemium_briquette").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":briquette_woodemium");
		butter = NativeItemTextureRegistry.texture(new Item().setTranslationKey("butter").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":ingot_butter");
		s_cream	= NativeItemTextureRegistry.texture(new ItemLemon(8, 1.0F, false).setTranslationKey("s_cream").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":ice_cream_s");
		min_cream	= NativeItemTextureRegistry.texture(new ItemLemon(10, 1.0F, false).setTranslationKey("min_cream").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":ice_cream_min");

		powder_ash = NativeItemTextureRegistry.texture(new ItemEnumMulti(EnumAshType.class, true, true).setTranslationKey("powder_ash").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_ash");
		powder_limestone = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_limestone").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_limestone");
		powder_cement = NativeItemTextureRegistry.texture(new ItemLemon(2, 0.5F, false).setTranslationKey("powder_cement").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_cement");

		ingot_gh336 = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.EPIC).setTranslationKey("ingot_gh336").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_gh336");
		nugget_gh336 = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.EPIC).setTranslationKey("nugget_gh336").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_gh336");
		ingot_cn989 = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.EPIC).setTranslationKey("ingot_cn989").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_cn989");
		nugget_cn989 = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.EPIC).setTranslationKey("nugget_cn989").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_cn989");
		billet_cn989 = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.EPIC).setTranslationKey("billet_cn989").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_cn989");
		plate_cn989 = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.EPIC).setTranslationKey("plate_cn989").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_cn989");
		powder_cn989 = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.EPIC).setTranslationKey("powder_cn989").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_cn989");

		ingot_australium = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.UNCOMMON).setTranslationKey("ingot_australium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_australium");
		ingot_australium_lesser = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.UNCOMMON).setTranslationKey("ingot_australium_lesser").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_australium_lesser");
		ingot_australium_greater = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.UNCOMMON).setTranslationKey("ingot_australium_greater").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_australium_greater");
		nugget_australium = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.UNCOMMON).setTranslationKey("nugget_australium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_australium");
		nugget_australium_lesser = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.UNCOMMON).setTranslationKey("nugget_australium_lesser").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_australium_lesser");
		nugget_australium_greater = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.UNCOMMON).setTranslationKey("nugget_australium_greater").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_australium_greater");

		nugget_th232 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_th232").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_th232");
		nugget_uranium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_uranium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_uranium");
		nugget_u233 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_u233").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_u233");
		nugget_u235 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_u235").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_u235");
		nugget_u238 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_u238").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_u238");
		nugget_plutonium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_plutonium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_plutonium");
		nugget_pu238 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_pu238").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_pu238");
		nugget_pu239 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_pu239").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_pu239");
		nugget_pu240 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_pu240").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_pu240");
		nugget_pu241 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_pu241").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_pu241");
		nugget_pu_mix = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_pu_mix").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_pu_mix");
		nugget_am241 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_am241").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_am241");
		nugget_am242 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_am242").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_am242");
		nugget_am_mix = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_am_mix").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_am_mix");
		nugget_bk247 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_bk247").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_bk247");
		nugget_cm242 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_cm242").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_cm242");
		nugget_cm243 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_cm243").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_cm243");
		nugget_cm244 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_cm244").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_cm244");
		nugget_cm245 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_cm245").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_cm245");
		nugget_cm246 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_cm246").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_cm246");
		nugget_cm247 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_cm247").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_cm247");
		nugget_cf251 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_cf251").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID +":nugget_cf251");
		nugget_cf252 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_cf252").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_cf252");
		nugget_cm_fuel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_cm_fuel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_cm_fuel");
		nugget_es253 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_es253").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_es253");
		nugget_cm_mix = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_cm_mix").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_cm_mix");
		nugget_neptunium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_neptunium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_neptunium");
		nugget_polonium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_polonium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_polonium");
		nugget_technetium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_technetium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_technetium");
		nugget_cobalt = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_cobalt").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_cobalt");
		nugget_co60 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_co60").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_co60");
		nugget_sr90 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_sr90").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_sr90");
		nugget_au198 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_au198").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_au198");
		nugget_pb209 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_pb209").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_pb209");
		nugget_ra226 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_ra226").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_ra226");
		nugget_actinium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_actinium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_actinium");
		plate_titanium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("plate_titanium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_titanium");
		plate_nickel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("plate_nickel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_nickel");
		plate_aluminium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("plate_aluminium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_aluminium");
		neutron_reflector = NativeItemTextureRegistry.texture(new Item().setTranslationKey("neutron_reflector").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":neutron_reflector");
		nugget_lead = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_lead").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_lead");
		ingot_bismuth = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("ingot_bismuth").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_bismuth");
		nugget_bismuth = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_bismuth").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_bismuth");
		ingot_arsenic = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("ingot_arsenic").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_arsenic");
		nugget_arsenic = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_arsenic").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_arsenic");
		ingot_tantalium = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("ingot_tantalium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_tantalium");
		nugget_tantalium = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("nugget_tantalium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_tantalium");
		ingot_silicon = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_silicon").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_silicon");
		billet_silicon = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_silicon").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_silicon");
		nugget_silicon = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_silicon").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_silicon");
		ingot_niobium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_niobium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_niobium");
		ingot_gallium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_gallium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_gallium");
		nugget_gallium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_gallium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_gallium");
		powder_gallium= NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_gallium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_gallium");
		powder_gallium_tiny= NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_gallium_tiny").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_gallium_tiny");
		ingot_tt = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.UNCOMMON).setTranslationKey("ingot_tt").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_techtactium");
		ingot_ttas = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.RARE).setTranslationKey("ingot_ttas").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_techtactium_as");
		ingot_gaas = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.RARE).setTranslationKey("ingot_gaas").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_gaas1");
		nugget_gaas = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_gaas").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_gaas");
		billet_gaas = NativeItemTextureRegistry.texture(new Item().setTranslationKey("billet_gaas").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":billet_gaas1");
		nugget_zinc = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_zinc").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_zinc");
		ingot_zinc = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_zinc").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_zinc");
		powder_zinc = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_zinc").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_zinc");
		nugget_niobium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_niobium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_niobium");
		ingot_osmiridium = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.RARE).setTranslationKey("ingot_osmiridium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_osmiridium");
		nugget_osmiridium = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.RARE).setTranslationKey("nugget_osmiridium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_osmiridium");
		ingot_hafnium = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("ingot_hafnium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_hafnium");
		nugget_hafnium = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("nugget_hafnium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_hafnium");
		ingot_iridium = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.RARE).setTranslationKey("ingot_iridium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_iridium");
		nugget_schrabidium = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.RARE).setTranslationKey("nugget_schrabidium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_schrabidium");
		nugget_beryllium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_beryllium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_beryllium");
		hazmat_cloth = NativeItemTextureRegistry.texture(new Item().setTranslationKey("hazmat_cloth").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":hazmat_cloth");
		hazmat_cloth_red = NativeItemTextureRegistry.texture(new Item().setTranslationKey("hazmat_cloth_red").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":hazmat_cloth_red");
		hazmat_cloth_grey = NativeItemTextureRegistry.texture(new Item().setTranslationKey("hazmat_cloth_grey").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":hazmat_cloth_grey");
		asbestos_cloth = NativeItemTextureRegistry.texture(new Item().setTranslationKey("asbestos_cloth").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":asbestos_cloth");
		rag = NativeItemTextureRegistry.texture(new ItemRag().setTranslationKey("rag").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":rag");
		rag_damp = NativeItemTextureRegistry.texture(new Item().setTranslationKey("rag_damp").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":rag_damp");
		rag_piss = NativeItemTextureRegistry.texture(new Item().setTranslationKey("rag_piss").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":rag_piss");
		rag_blood = NativeItemTextureRegistry.texture(new Item().setTranslationKey("rag_blood").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":rag_blood");
		filter_coal = NativeItemTextureRegistry.texture(new Item().setTranslationKey("filter_coal").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":filter_coal");
		ingot_hes = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_hes").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_hes");
		ingot_les = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ingot_les").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_les");
		nugget_hes = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_hes").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_hes");
		nugget_les = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nugget_les").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_les");
		plate_combine_steel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("plate_combine_steel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_combine_steel");
		ingot_palladium = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("ingot_palladium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_palladium");
		scuttertail = NativeItemTextureRegistry.texture(new Item().setTranslationKey("scuttertail").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":scuttertail");
		saltleaf = NativeItemTextureRegistry.texture(new Item().setTranslationKey("saltleaf").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":salt_leaf");
		leaf_rubber = NativeItemTextureRegistry.texture(new Item().setTranslationKey("rubber_leaves").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":rubber_leaves");
		leaf_pet = NativeItemTextureRegistry.texture(new Item().setTranslationKey("pet_leaves").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":pet_leaves");

		crystal_coal = NativeItemTextureRegistry.texture(new Item().setTranslationKey("crystal_coal").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_coal");
		crystal_iron = NativeItemTextureRegistry.texture(new Item().setTranslationKey("crystal_iron").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_iron");
		crystal_gold = NativeItemTextureRegistry.texture(new Item().setTranslationKey("crystal_gold").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_gold");
		crystal_redstone = NativeItemTextureRegistry.texture(new Item().setTranslationKey("crystal_redstone").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_redstone");
		crystal_lapis = NativeItemTextureRegistry.texture(new Item().setTranslationKey("crystal_lapis").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_lapis");
		crystal_diamond = NativeItemTextureRegistry.texture(new Item().setTranslationKey("crystal_diamond").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_diamond");
		crystal_uranium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("crystal_uranium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_uranium");
		crystal_thorium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("crystal_thorium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_thorium");
		crystal_plutonium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("crystal_plutonium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_plutonium");
		crystal_titanium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("crystal_titanium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_titanium");
		crystal_sulfur = NativeItemTextureRegistry.texture(new Item().setTranslationKey("crystal_sulfur").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_sulfur");
		crystal_niter = NativeItemTextureRegistry.texture(new Item().setTranslationKey("crystal_niter").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_niter");
		crystal_copper = NativeItemTextureRegistry.texture(new Item().setTranslationKey("crystal_copper").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_copper");
		crystal_tungsten = NativeItemTextureRegistry.texture(new Item().setTranslationKey("crystal_tungsten").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_tungsten");
		crystal_aluminium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("crystal_aluminium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_aluminium");
		crystal_fluorite = NativeItemTextureRegistry.texture(new Item().setTranslationKey("crystal_fluorite").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_fluorite");
		crystal_beryllium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("crystal_beryllium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_beryllium");
		crystal_lead = NativeItemTextureRegistry.texture(new Item().setTranslationKey("crystal_lead").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_lead");
		crystal_schraranium = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.RARE).setTranslationKey("crystal_schraranium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_schraranium");
		crystal_schrabidium = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.RARE).setTranslationKey("crystal_schrabidium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_schrabidium");
		crystal_rare = NativeItemTextureRegistry.texture(new Item().setTranslationKey("crystal_rare").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_rare");
		crystal_phosphorus = NativeItemTextureRegistry.texture(new Item().setTranslationKey("crystal_phosphorus").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_phosphorus");
		crystal_lithium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("crystal_lithium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_lithium");
		crystal_cobalt = NativeItemTextureRegistry.texture(new Item().setTranslationKey("crystal_cobalt").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_cobalt");
		crystal_starmetal = NativeItemTextureRegistry.texture(new Item().setTranslationKey("crystal_starmetal").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_starmetal");
		crystal_cinnebar = NativeItemTextureRegistry.texture(new Item().setTranslationKey("crystal_cinnebar").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_cinnebar");
		crystal_trixite = NativeItemTextureRegistry.texture(new Item().setTranslationKey("crystal_trixite").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_trixite");
		crystal_osmiridium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("crystal_osmiridium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_osmiridium");
		gem_sodalite = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("gem_sodalite").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":gem_sodalite");
		gem_tantalium = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("gem_tantalium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":gem_tantalium");
		gem_volcanic = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.UNCOMMON).setTranslationKey("gem_volcanic").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":gem_volcanic");
		gem_rad = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.UNCOMMON).setTranslationKey("gem_rad").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":gem_rad");
		gem_alexandrite = NativeItemTextureRegistry.texture(new ItemAlexandrite().setTranslationKey("gem_alexandrite").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":gem_alexandrite");
		crystal_nickel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("crystal_nickel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_nickel");
		crystal_niobium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("crystal_niobium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_niobium");
		crystal_zinc = NativeItemTextureRegistry.texture(new Item().setTranslationKey("crystal_zinc").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_zinc");

		powder_lead = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_lead").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_lead");
		powder_tantalium = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("powder_tantalium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_tantalium");
		powder_neptunium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_neptunium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_neptunium");
		powder_polonium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_polonium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_polonium");
		powder_co60 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_co60").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_co60");
		powder_sr90 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_sr90").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_sr90");
		powder_sr90_tiny = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_sr90_tiny").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_sr90_tiny");
		powder_i131 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_i131").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_i131");
		powder_i131_tiny = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_i131_tiny").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_i131_tiny");
		powder_xe135 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_xe135").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_xe135");
		powder_xe135_tiny = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_xe135_tiny").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_xe135_tiny");
		powder_cs137 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_cs137").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_cs137");
		powder_cs137_tiny = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_cs137_tiny").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_cs137_tiny");
		powder_au198 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_au198").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_au198");
		powder_ra226 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_ra226").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_ra226");
		powder_at209 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_at209").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_at209");
		powder_schrabidium = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.RARE).setTranslationKey("powder_schrabidium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_schrabidium");
		powder_schrabidate = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.RARE).setTranslationKey("powder_schrabidate").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_schrabidate");
		powder_aluminium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_aluminium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_aluminium");
		powder_beryllium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_beryllium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_beryllium");
		powder_copper = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_copper").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_copper");
		powder_zinc = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_zinc").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_zinc");
		powder_lead = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_lead").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_lead");
		powder_gold = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_gold").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_gold");
		powder_iron = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_iron").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_iron");
		powder_titanium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_titanium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_titanium");
		powder_nickel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_nickel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_nickel");
		powder_tungsten = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_tungsten").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_tungsten");
		powder_uranium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_uranium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_uranium");
		powder_plutonium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_plutonium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_plutonium");
		dust = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("dust").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":dust");
		dust_tiny = NativeItemTextureRegistry.texture(new Item().setTranslationKey("dust_tiny").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":dust_tiny");
		fallout = NativeItemTextureRegistry.texture(new Item().setTranslationKey("fallout").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":fallout");
		powder_advanced_alloy = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_advanced_alloy").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_advanced_alloy");
		powder_tcalloy = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_tcalloy").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_tcalloy");
		powder_coal = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_coal").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_coal");
		powder_coal_tiny = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_coal_tiny").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_coal_tiny");
		powder_combine_steel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_combine_steel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_combine_steel");
		powder_diamond = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_diamond").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_diamond");
		powder_emerald = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_emerald").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_emerald");
		powder_lapis = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_lapis").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_lapis");
		powder_quartz = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_quartz").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_quartz");
		powder_magnetized_tungsten = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_magnetized_tungsten").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_magnetized_tungsten");
		powder_chlorophyte = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_chlorophyte").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_chlorophyte");
		powder_red_copper = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_red_copper").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_red_copper");
		powder_steel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_steel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_steel");
		powder_lithium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_lithium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_lithium");
		powder_zirconium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_zirconium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_zirconium");
		powder_sodium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_sodium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_sodium");
		powder_power = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.UNCOMMON).setTranslationKey("powder_power").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_energy_alt");
		powder_iodine = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.EPIC).setTranslationKey("powder_iodine").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_iodine");
		powder_thorium = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.UNCOMMON).setTranslationKey("powder_thorium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_thorium");
		powder_neodymium = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.EPIC).setTranslationKey("powder_neodymium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_neodymium");
		powder_astatine = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.EPIC).setTranslationKey("powder_astatine").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_astatine");
		powder_caesium = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.EPIC).setTranslationKey("powder_caesium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_caesium");
		powder_australium = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.UNCOMMON).setTranslationKey("powder_australium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_australium");
		powder_strontium = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.EPIC).setTranslationKey("powder_strontium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_strontium");
		powder_cobalt = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.EPIC).setTranslationKey("powder_cobalt").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_cobalt");
		powder_bromine = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.EPIC).setTranslationKey("powder_bromine").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_bromine");
		powder_niobium = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.EPIC).setTranslationKey("powder_niobium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_niobium");
		powder_tennessine = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.EPIC).setTranslationKey("powder_tennessine").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_tennessine");
		powder_cerium = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.EPIC).setTranslationKey("powder_cerium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_cerium");
		powder_dura_steel = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("powder_dura_steel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_dura_steel");
		powder_polymer = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("powder_polymer").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_polymer");
		powder_bakelite = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("powder_bakelite").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_bakelite");
		powder_rubber = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("powder_rubber").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_rubber");
		powder_pvc = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("powder_pvc").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_pvc");
		powder_euphemium = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.EPIC).setTranslationKey("powder_euphemium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_euphemium");
		powder_wd2004_tiny = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.EPIC).setTranslationKey("powder_wd2004_tiny").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_wd2004_tiny");
		powder_wd2004 = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.EPIC).setTranslationKey("powder_wd2004").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_wd2004");
		powder_meteorite = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_meteorite").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_meteorite");
		powder_lanthanium = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.EPIC).setTranslationKey("powder_lanthanium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_lanthanium");
		powder_actinium = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.EPIC).setTranslationKey("powder_actinium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_actinium");
		powder_boron = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.EPIC).setTranslationKey("powder_boron").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_boron");
		powder_semtex_mix = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_semtex_mix").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_semtex_mix");
		powder_desh_mix = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_desh_mix").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_desh_mix");
		powder_desh_ready = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_desh_ready").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_desh_ready");
		powder_nitan_mix = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_nitan_mix").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_nitan_mix");
		powder_spark_mix = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_spark_mix").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_spark_mix");
		powder_desh = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_desh").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_desh");
		powder_steel_tiny = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_steel_tiny").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_steel_tiny");
		powder_lithium_tiny = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_lithium_tiny").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_lithium_tiny");
		powder_neodymium_tiny = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_neodymium_tiny").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_neodymium_tiny");
		powder_cobalt_tiny = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_cobalt_tiny").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_cobalt_tiny");
		powder_niobium_tiny = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_niobium_tiny").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_niobium_tiny");
		powder_cerium_tiny = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_cerium_tiny").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_cerium_tiny");
		powder_lanthanium_tiny = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_lanthanium_tiny").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_lanthanium_tiny");
		powder_actinium_tiny = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_actinium_tiny").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_actinium_tiny");
		powder_boron_tiny = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_boron_tiny").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_boron_tiny");
		powder_meteorite_tiny = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_meteorite_tiny").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_meteorite_tiny");
		powder_yellowcake = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_yellowcake").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_yellowcake");
		powder_magic = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_magic").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_magic");
		powder_balefire = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_balefire").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_balefire");
		powder_sawdust = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_sawdust").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_sawdust");
		powder_flux = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_flux").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_flux");
		powder_fertilizer = NativeItemTextureRegistry.texture(new ItemFertilizer().setTranslationKey("powder_fertilizer").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_fertilizer");
		powder_coltan_ore = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_coltan_ore").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_coltan_ore");
		powder_coltan = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_coltan").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_coltan");
		powder_tektite = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_tektite").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_tektite");
		powder_paleogenite = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_paleogenite").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_paleogenite");
		powder_paleogenite_tiny = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_paleogenite_tiny").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_paleogenite_tiny");
		powder_impure_osmiridium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_impure_osmiridium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_impure_osmiridium");
		powder_borax = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_borax").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_borax");
		powder_chlorocalcite = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_chlorocalcite").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_chlorocalcite");
		powder_molysite = NativeItemTextureRegistry.texture(new Item().setTranslationKey("powder_molysite").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_molysite");

		nickel_salts = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nickel_salts").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nickel_salts");

		fragment_neodymium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("fragment_neodymium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":fragment_neodymium");
		fragment_cobalt = NativeItemTextureRegistry.texture(new Item().setTranslationKey("fragment_cobalt").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":fragment_cobalt");
		fragment_niobium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("fragment_niobium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":fragment_niobium");
		fragment_cerium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("fragment_cerium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":fragment_cerium");
		fragment_lanthanium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("fragment_lanthanium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":fragment_lanthanium");
		fragment_actinium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("fragment_actinium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":fragment_actinium");
		fragment_boron = NativeItemTextureRegistry.texture(new Item().setTranslationKey("fragment_boron").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":fragment_boron");
		fragment_meteorite = NativeItemTextureRegistry.texture(new Item().setTranslationKey("fragment_meteorite").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":fragment_meteorite");
		fragment_coltan = NativeItemTextureRegistry.texture(new Item().setTranslationKey("fragment_coltan").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":fragment_coltan");
		chunk_ore = new ItemEnumMulti(EnumChunkType.class, true, true).setTranslationKey("chunk_ore").setCreativeTab(MainRegistry.partsTab);

		biomass = NativeItemTextureRegistry.texture(new Item().setTranslationKey("biomass").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":biomass");
		biomass_compressed = NativeItemTextureRegistry.texture(new Item().setTranslationKey("biomass_compressed").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":biomass_compressed");
		bio_wafer = NativeItemTextureRegistry.texture(new ItemLemon(4, 0.6F, false).setTranslationKey("bio_wafer").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":bio_wafer");
		flesh = NativeItemTextureRegistry.texture(new Item().setTranslationKey("flesh").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":flesh");
		flesh_wafer = NativeItemTextureRegistry.texture(new ItemLemon(5, 0.6F, false).setTranslationKey("flesh_wafer").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":blood_wafer");
		grilled_flesh = NativeItemTextureRegistry.texture(new ItemLemon(6, 0.8F, false).setTranslationKey("grilled_flesh").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":blood_patty");
	    flesh_burger = NativeItemTextureRegistry.texture(new ItemLemon(7, 1.0F, false).setTranslationKey("flesh_burger").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":blood_burger");
		plant_item = NativeItemTextureRegistry.texture(new ItemEnumMulti(EnumPlantType.class, true, true).setTranslationKey("plant_item").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plant_item");

		coil_copper = NativeItemTextureRegistry.texture(new Item().setTranslationKey("coil_copper").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":coil_copper");
		coil_copper_torus = NativeItemTextureRegistry.texture(new Item().setTranslationKey("coil_copper_torus").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":coil_copper_torus");
		coil_tungsten = NativeItemTextureRegistry.texture(new Item().setTranslationKey("coil_tungsten").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":coil_tungsten");
		tank_steel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("tank_steel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":tank_steel");
		motor = NativeItemTextureRegistry.texture(new Item().setTranslationKey("motor").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":motor");
		motor_desh = NativeItemTextureRegistry.texture(new Item().setTranslationKey("motor_desh").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":motor_desh");
		motor_bismuth = NativeItemTextureRegistry.texture(new Item().setTranslationKey("motor_bismuth").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":motor_bismuth");
		centrifuge_element = NativeItemTextureRegistry.texture(new Item().setTranslationKey("centrifuge_element").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":centrifuge_element");
		reactor_core = NativeItemTextureRegistry.texture(new Item().setTranslationKey("reactor_core").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":reactor_core");
		rtg_unit = NativeItemTextureRegistry.texture(new Item().setTranslationKey("rtg_unit").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":rtg_unit");
		coil_magnetized_tungsten = NativeItemTextureRegistry.texture(new Item().setTranslationKey("coil_magnetized_tungsten").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":coil_magnetized_tungsten");
		coil_gold = NativeItemTextureRegistry.texture(new Item().setTranslationKey("coil_gold").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":coil_gold");
		coil_gold_torus = NativeItemTextureRegistry.texture(new Item().setTranslationKey("coil_gold_torus").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":coil_gold_torus");
		chlorine_pinwheel = NativeItemTextureRegistry.texture(new ItemInfiniteFluid(Fluids.CHLORINE, 1, 2).setTranslationKey("chlorine_pinwheel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":chlorine_pinwheel");
		FluidTank.noDualUnload.add(chlorine_pinwheel);
		oxy_pinwheel = NativeItemTextureRegistry.texture(new ItemInfiniteFluid(Fluids.OXYGEN, 1).setTranslationKey("oxy_pinwheel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":oxy_pinwheel");
		FluidTank.noDualUnload.add(oxy_pinwheel);
		ring_starmetal = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ring_starmetal").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ring_starmetal");
		flywheel_beryllium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("flywheel_beryllium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":flywheel_beryllium");
		deuterium_filter = NativeItemTextureRegistry.texture(new Item().setTranslationKey("deuterium_filter").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":deuterium_filter");
		parts_legendary = NativeItemTextureRegistry.texture(new ItemEnumMulti(EnumLegendaryType.class, false, true).setTranslationKey("parts_legendary").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":parts_legendary");

		gear_large = NativeItemTextureRegistry.texture(new ItemGear().setTranslationKey("gear_large").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":gear_large");
		sawblade = NativeItemTextureRegistry.texture(new Item().setTranslationKey("sawblade").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":sawblade");

		shell = NativeItemTextureRegistry.texture(new ItemAutogen(MaterialShapes.SHELL).oun("shellntm").setTranslationKey("shell").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":shell");
		pipe = NativeItemTextureRegistry.texture(new ItemAutogen(MaterialShapes.PIPE).oun("pipentm").setTranslationKey("pipe").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":pipe");
		fins_flat = NativeItemTextureRegistry.texture(new Item().setTranslationKey("fins_flat").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":fins_flat");
		fins_small_steel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("fins_small_steel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":fins_small_steel");
		fins_big_steel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("fins_big_steel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":fins_big_steel");
		fins_tri_steel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("fins_tri_steel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":fins_tri_steel");
		fins_quad_titanium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("fins_quad_titanium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":fins_quad_titanium");
		sphere_steel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("sphere_steel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":sphere_steel");
		pedestal_steel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("pedestal_steel").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":pedestal_steel");
		dysfunctional_reactor = NativeItemTextureRegistry.texture(new Item().setTranslationKey("dysfunctional_reactor").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":dysfunctional_reactor");
		blade_titanium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("blade_titanium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":blade_titanium");
		blade_syngas = NativeItemTextureRegistry.texture(new Item().setTranslationKey("blade_syngas").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":blade_syngas");
		turbine_titanium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("turbine_titanium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":turbine_titanium");
		blade_tungsten = NativeItemTextureRegistry.texture(new Item().setTranslationKey("blade_tungsten").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":blade_tungsten");
		turbine_tungsten = NativeItemTextureRegistry.texture(new Item().setTranslationKey("turbine_tungsten").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":turbine_tungsten");
		turbine_syngas = NativeItemTextureRegistry.texture(new Item().setTranslationKey("turbine_syngas").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":turbine_syngas");

		ducttape = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ducttape").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ducttape");
		catalyst_clay = NativeItemTextureRegistry.texture(new Item().setTranslationKey("catalyst_clay").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":catalyst_clay");

		warhead_generic_small = NativeItemTextureRegistry.texture(new Item().setTranslationKey("warhead_generic_small").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":warhead_generic_small");
		warhead_generic_medium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("warhead_generic_medium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":warhead_generic_medium");
		warhead_generic_large = NativeItemTextureRegistry.texture(new Item().setTranslationKey("warhead_generic_large").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":warhead_generic_large");
		warhead_incendiary_small = NativeItemTextureRegistry.texture(new Item().setTranslationKey("warhead_incendiary_small").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":warhead_incendiary_small");
		warhead_incendiary_medium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("warhead_incendiary_medium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":warhead_incendiary_medium");
		warhead_incendiary_large = NativeItemTextureRegistry.texture(new Item().setTranslationKey("warhead_incendiary_large").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":warhead_incendiary_large");
		warhead_cluster_small = NativeItemTextureRegistry.texture(new Item().setTranslationKey("warhead_cluster_small").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":warhead_cluster_small");
		warhead_cluster_medium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("warhead_cluster_medium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":warhead_cluster_medium");
		warhead_cluster_large = NativeItemTextureRegistry.texture(new Item().setTranslationKey("warhead_cluster_large").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":warhead_cluster_large");
		warhead_buster_small = NativeItemTextureRegistry.texture(new Item().setTranslationKey("warhead_buster_small").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":warhead_buster_small");
		warhead_buster_medium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("warhead_buster_medium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":warhead_buster_medium");
		warhead_buster_large = NativeItemTextureRegistry.texture(new Item().setTranslationKey("warhead_buster_large").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":warhead_buster_large");
		warhead_nuclear = NativeItemTextureRegistry.texture(new Item().setTranslationKey("warhead_nuclear").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":warhead_nuclear");
		warhead_mirv = NativeItemTextureRegistry.texture(new Item().setTranslationKey("warhead_mirv").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":warhead_mirv");
		warhead_volcano = NativeItemTextureRegistry.texture(new Item().setTranslationKey("warhead_volcano").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":warhead_volcano");

		fuel_tank_small = NativeItemTextureRegistry.texture(new Item().setTranslationKey("fuel_tank_small").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":fuel_tank_small");
		fuel_tank_medium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("fuel_tank_medium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":fuel_tank_medium");
		fuel_tank_large = NativeItemTextureRegistry.texture(new Item().setTranslationKey("fuel_tank_large").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":fuel_tank_large");

		thruster_small = NativeItemTextureRegistry.texture(new Item().setTranslationKey("thruster_small").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":thruster_small");
		thruster_medium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("thruster_medium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":thruster_medium");
		thruster_large = NativeItemTextureRegistry.texture(new Item().setTranslationKey("thruster_large").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":thruster_large");

		sat_head_mapper = NativeItemTextureRegistry.texture(new Item().setTranslationKey("sat_head_mapper").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":sat_head_mapper");
		sat_head_scanner = NativeItemTextureRegistry.texture(new Item().setTranslationKey("sat_head_scanner").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":sat_head_scanner");
		sat_head_radar = NativeItemTextureRegistry.texture(new Item().setTranslationKey("sat_head_radar").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":sat_head_radar");
		sat_head_laser = NativeItemTextureRegistry.texture(new Item().setTranslationKey("sat_head_laser").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":sat_head_laser");
		sat_head_resonator = NativeItemTextureRegistry.texture(new Item().setTranslationKey("sat_head_resonator").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":sat_head_resonator");

		seg_10 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("seg_10").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":seg_10");
		seg_15 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("seg_15").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":seg_15");
		seg_20 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("seg_20").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":seg_20");

		combine_scrap = NativeItemTextureRegistry.texture(new Item().setTranslationKey("combine_scrap").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":combine_scrap");

		shimmer_head = NativeItemTextureRegistry.texture(new Item().setTranslationKey("shimmer_head").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":shimmer_head_original");
		shimmer_axe_head = NativeItemTextureRegistry.texture(new Item().setTranslationKey("shimmer_axe_head").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":shimmer_axe_head");
		shimmer_handle = NativeItemTextureRegistry.texture(new Item().setTranslationKey("shimmer_handle").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":shimmer_handle");

		entanglement_kit = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("entanglement_kit").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":entanglement_kit");

		circuit = NativeItemTextureRegistry.texture(new ItemCircuit().setTranslationKey("circuit").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":circuit");
		crt_display = NativeItemTextureRegistry.texture(new Item().setTranslationKey("crt_display").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crt_display");
		circuit_star_piece = (ItemEnumMulti) new ItemEnumMulti(ScrapType.class, true, true).setTranslationKey("circuit_star_piece").setCreativeTab(null);
		circuit_star_component = (ItemEnumMulti) new ItemCircuitStarComponent().setTranslationKey("circuit_star_component").setCreativeTab(null);
		circuit_star = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.UNCOMMON).setTranslationKey("circuit_star").setCreativeTab(null), RefStrings.MODID + ":circuit_star");
		assembly_nuke = NativeItemTextureRegistry.texture(new Item().setTranslationKey("assembly_nuke").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":assembly_nuke");
		casing = NativeItemTextureRegistry.texture(new ItemEnumMulti(ItemEnums.EnumCasingType.class, true, true).setTranslationKey("casing").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":casing");

		wiring_red_copper = NativeItemTextureRegistry.texture(new ItemWiring().setTranslationKey("wiring_red_copper").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":wiring_red_copper");
		wrench = NativeItemTextureRegistry.texture(new ItemWrench(MainRegistry.tMatSteel).setTranslationKey("wrench").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":wrench");

		pellet_rtg_depleted = new ItemRTGPelletDepleted().setContainerItem(plate_iron).setTranslationKey("pellet_rtg_depleted").setCreativeTab(MainRegistry.controlTab);

		pellet_rtg_radium = NativeItemTextureRegistry.texture(new ItemRTGPellet(3).setDecays(DepletedRTGMaterial.LEAD, (long) (RTGUtil.getLifespan(16.0F, HalfLifeType.LONG, false) * 1.5)).setTranslationKey("pellet_rtg_radium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1), RefStrings.MODID + ":pellet_rtg_radium");
		pellet_rtg_weak = NativeItemTextureRegistry.texture(new ItemRTGPellet(5).setDecays(DepletedRTGMaterial.LEAD, (long) (RTGUtil.getLifespan(1.0F, HalfLifeType.LONG, false) * 1.5)).setTranslationKey("pellet_rtg_weak").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1), RefStrings.MODID + ":pellet_rtg_weak");
		pellet_rtg = NativeItemTextureRegistry.texture(new ItemRTGPellet(10).setDecays(DepletedRTGMaterial.LEAD, (long) (RTGUtil.getLifespan(87.7F, HalfLifeType.MEDIUM, false) * 1.5)).setTranslationKey("pellet_rtg").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1), RefStrings.MODID + ":pellet_rtg");
		pellet_rtg_strontium = NativeItemTextureRegistry.texture(new ItemRTGPellet(15).setDecays(DepletedRTGMaterial.ZIRCONIUM, (long) (RTGUtil.getLifespan(29.0F, HalfLifeType.MEDIUM, false) * 1.5)).setTranslationKey("pellet_rtg_strontium").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":pellet_rtg_strontium");
		pellet_rtg_cobalt = NativeItemTextureRegistry.texture(new ItemRTGPellet(15).setDecays(DepletedRTGMaterial.NICKEL, (long) (RTGUtil.getLifespan(5.3F, HalfLifeType.MEDIUM, false) * 1.5)).setTranslationKey("pellet_rtg_cobalt").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":pellet_rtg_cobalt");
		pellet_rtg_actinium = NativeItemTextureRegistry.texture(new ItemRTGPellet(20).setDecays(DepletedRTGMaterial.LEAD, (long) (RTGUtil.getLifespan(21.8F, HalfLifeType.MEDIUM, false) * 1.5)).setTranslationKey("pellet_rtg_actinium").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":pellet_rtg_actinium");
		pellet_rtg_americium = NativeItemTextureRegistry.texture(new ItemRTGPellet(20).setDecays(DepletedRTGMaterial.NEPTUNIUM, (long) (RTGUtil.getLifespan(4.7F, HalfLifeType.LONG, false) * 1.5)).setTranslationKey("pellet_rtg_americium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1), RefStrings.MODID + ":pellet_rtg_americium");
		pellet_rtg_berkelium = NativeItemTextureRegistry.texture(new ItemRTGPellet(20).setDecays(DepletedRTGMaterial.AMERICIUM, (long) (RTGUtil.getLifespan(13.8F, HalfLifeType.LONG, false) * 1.5)).setTranslationKey("pellet_rtg_berkelium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1), RefStrings.MODID + ":pellet_rtg_berkelium");
		pellet_rtg_polonium = NativeItemTextureRegistry.texture(new ItemRTGPellet(50).setDecays(DepletedRTGMaterial.LEAD, (long) (RTGUtil.getLifespan(138.0F, HalfLifeType.SHORT, false) * 1.5)).setTranslationKey("pellet_rtg_polonium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1), RefStrings.MODID + ":pellet_rtg_polonium");
		pellet_rtg_gold = NativeItemTextureRegistry.texture(new ItemRTGPellet(VersatileConfig.rtgDecay() ? 200 : 100).setDecays(DepletedRTGMaterial.MERCURY, (long) (RTGUtil.getLifespan(2.7F, HalfLifeType.SHORT, false) * 1.5)).setTranslationKey("pellet_rtg_gold").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1), RefStrings.MODID + ":pellet_rtg_gold");
		pellet_rtg_lead = NativeItemTextureRegistry.texture(new ItemRTGPellet(VersatileConfig.rtgDecay() ? 600 : 200).setDecays(DepletedRTGMaterial.BISMUTH, (long) (RTGUtil.getLifespan(0.3F, HalfLifeType.SHORT, false) * 1.5)).setTranslationKey("pellet_rtg_lead").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":pellet_rtg_lead");
		pellet_rtg_cf251 = NativeItemTextureRegistry.texture(new ItemRTGPellet(VersatileConfig.rtgDecay() ? 600 : 200).setDecays(DepletedRTGMaterial.LEAD, (long) (RTGUtil.getLifespan(1F, HalfLifeType.SHORT, false) * 2.5)).setTranslationKey("pellet_rtg_cf251").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":pellet_rtg_cf251");
		pellet_rtg_cf252 = NativeItemTextureRegistry.texture(new ItemRTGPellet(VersatileConfig.rtgDecay() ? 600 : 200).setDecays(DepletedRTGMaterial.LEAD, (long) (RTGUtil.getLifespan(1F, HalfLifeType.SHORT, false) * 2.5)).setTranslationKey("pellet_rtg_cf252").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":pellet_rtg_cf252");

		piston_selenium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("piston_selenium").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":piston_selenium");
		piston_set = new ItemPistons().setTranslationKey("piston_set").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1);
		drillbit = new ItemDrillbit().setTranslationKey("drillbit").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1);

		rune_blank = NativeItemTextureRegistry.texture(new ItemCustomLore().setEffect().setTranslationKey("rune_blank").setCreativeTab(MainRegistry.partsTab).setMaxStackSize(1), RefStrings.MODID + ":rune_blank");
		rune_isa = NativeItemTextureRegistry.texture(new ItemCustomLore().setEffect().setTranslationKey("rune_isa").setCreativeTab(MainRegistry.partsTab).setMaxStackSize(1), RefStrings.MODID + ":rune_isa");
		rune_dagaz = NativeItemTextureRegistry.texture(new ItemCustomLore().setEffect().setTranslationKey("rune_dagaz").setCreativeTab(MainRegistry.partsTab).setMaxStackSize(1), RefStrings.MODID + ":rune_dagaz");
		rune_hagalaz = NativeItemTextureRegistry.texture(new ItemCustomLore().setEffect().setTranslationKey("rune_hagalaz").setCreativeTab(MainRegistry.partsTab).setMaxStackSize(1), RefStrings.MODID + ":rune_hagalaz");
		rune_jera = NativeItemTextureRegistry.texture(new ItemCustomLore().setEffect().setTranslationKey("rune_jera").setCreativeTab(MainRegistry.partsTab).setMaxStackSize(1), RefStrings.MODID + ":rune_jera");
		rune_thurisaz = NativeItemTextureRegistry.texture(new ItemCustomLore().setEffect().setTranslationKey("rune_thurisaz").setCreativeTab(MainRegistry.partsTab).setMaxStackSize(1), RefStrings.MODID + ":rune_thurisaz");

		ams_catalyst_blank = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ams_catalyst_blank").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1), RefStrings.MODID + ":ams_catalyst_blank");
		ams_catalyst_aluminium = 	NativeItemTextureRegistry.texture(new ItemCatalyst(0xCCCCCC, 1000000, 1.15F, 	0.85F, 	1.15F).setTranslationKey("ams_catalyst_aluminium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1), RefStrings.MODID + ":ams_catalyst_aluminium");
		ams_catalyst_beryllium = 	NativeItemTextureRegistry.texture(new ItemCatalyst(0x97978B, 0, 		1.25F, 	0.95F, 	1.05F).setTranslationKey("ams_catalyst_beryllium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1), RefStrings.MODID + ":ams_catalyst_beryllium");
		ams_catalyst_caesium = 		NativeItemTextureRegistry.texture(new ItemCatalyst(0x6400FF, 2500000, 1.00F, 	0.85F, 	1.15F).setTranslationKey("ams_catalyst_caesium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1), RefStrings.MODID + ":ams_catalyst_caesium");
		ams_catalyst_cerium = 		NativeItemTextureRegistry.texture(new ItemCatalyst(0x1D3FFF, 1000000, 1.15F, 	1.15F, 	0.85F).setTranslationKey("ams_catalyst_cerium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1), RefStrings.MODID + ":ams_catalyst_cerium");
		ams_catalyst_cobalt = 		NativeItemTextureRegistry.texture(new ItemCatalyst(0x789BBE, 0, 		1.25F, 	1.05F, 	0.95F).setTranslationKey("ams_catalyst_cobalt").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1), RefStrings.MODID + ":ams_catalyst_cobalt");
		ams_catalyst_copper = 		NativeItemTextureRegistry.texture(new ItemCatalyst(0xAADE29, 0, 		1.25F, 	1.00F, 	1.00F).setTranslationKey("ams_catalyst_copper").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1), RefStrings.MODID + ":ams_catalyst_copper");
		ams_catalyst_dineutronium = NativeItemTextureRegistry.texture(new ItemCatalyst(0x334077, 2500000, 1.00F, 	1.15F, 	0.85F).setTranslationKey("ams_catalyst_dineutronium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1), RefStrings.MODID + ":ams_catalyst_dineutronium");
		ams_catalyst_euphemium = 	NativeItemTextureRegistry.texture(new ItemCatalyst(0xFF9CD2, 2500000, 1.00F, 	1.00F, 	1.00F).setTranslationKey("ams_catalyst_euphemium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1), RefStrings.MODID + ":ams_catalyst_euphemium");
		ams_catalyst_iron = 		NativeItemTextureRegistry.texture(new ItemCatalyst(0xFF7E22, 1000000, 1.15F, 	0.95F, 	1.05F).setTranslationKey("ams_catalyst_iron").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1), RefStrings.MODID + ":ams_catalyst_iron");
		ams_catalyst_lithium = 		NativeItemTextureRegistry.texture(new ItemCatalyst(0xFF2727, 0, 		1.25F, 	0.85F, 	1.15F).setTranslationKey("ams_catalyst_lithium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1), RefStrings.MODID + ":ams_catalyst_lithium");
		ams_catalyst_niobium = 		NativeItemTextureRegistry.texture(new ItemCatalyst(0x3BF1B6, 1000000, 1.15F, 	1.05F, 	0.95F).setTranslationKey("ams_catalyst_niobium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1), RefStrings.MODID + ":ams_catalyst_niobium");
		ams_catalyst_schrabidium = 	NativeItemTextureRegistry.texture(new ItemCatalyst(0x32FFFF, 2500000, 1.00F, 	1.05F, 	0.95F).setTranslationKey("ams_catalyst_schrabidium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1), RefStrings.MODID + ":ams_catalyst_schrabidium");
		ams_catalyst_strontium = 	NativeItemTextureRegistry.texture(new ItemCatalyst(0xDD0D35, 1000000, 1.15F, 	1.00F, 	1.00F).setTranslationKey("ams_catalyst_strontium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1), RefStrings.MODID + ":ams_catalyst_strontium");
		ams_catalyst_thorium = 		NativeItemTextureRegistry.texture(new ItemCatalyst(0x653B22, 2500000, 1.00F, 	0.95F, 	1.05F).setTranslationKey("ams_catalyst_thorium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1), RefStrings.MODID + ":ams_catalyst_thorium");
		ams_catalyst_tungsten = 	NativeItemTextureRegistry.texture(new ItemCatalyst(0xF5FF48, 0, 		1.25F, 	1.15F, 	0.85F).setTranslationKey("ams_catalyst_tungsten").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1), RefStrings.MODID + ":ams_catalyst_tungsten");

		cell_empty = NativeItemTextureRegistry.texture(new Item().setTranslationKey("cell_empty").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":cell_empty");
		cell_uf6 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("cell_uf6").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.cell_empty), RefStrings.MODID + ":cell_uf6");
		cell_puf6 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("cell_puf6").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.cell_empty), RefStrings.MODID + ":cell_puf6");
		cell_antimatter = NativeItemTextureRegistry.texture(new ItemDrop().setTranslationKey("cell_antimatter").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.cell_empty), RefStrings.MODID + ":cell_antimatter");
		cell_deuterium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("cell_deuterium").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.cell_empty), RefStrings.MODID + ":cell_deuterium");
		cell_tritium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("cell_tritium").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.cell_empty), RefStrings.MODID + ":cell_tritium");
		cell_sas3 = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.RARE).setTranslationKey("cell_sas3").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.cell_empty), RefStrings.MODID + ":cell_sas3");
		cell_anti_schrabidium = NativeItemTextureRegistry.texture(new ItemDrop().setTranslationKey("cell_anti_schrabidium").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.cell_empty), RefStrings.MODID + ":cell_anti_schrabidium");
		cell_balefire = NativeItemTextureRegistry.texture(new Item().setTranslationKey("cell_balefire").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.cell_empty), RefStrings.MODID + ":cell_balefire");

		demon_core_open = NativeItemTextureRegistry.texture(new ItemDemonCore().setTranslationKey("demon_core_open").setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":demon_core_open");
		demon_core_closed = NativeItemTextureRegistry.texture(new Item().setTranslationKey("demon_core_closed").setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":demon_core_closed");

		pa_coil = NativeItemTextureRegistry.texture(new ItemPACoil().setTranslationKey("pa_coil").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":pa_coil");

		particle_empty = NativeItemTextureRegistry.texture(new Item().setTranslationKey("particle_empty").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":particle_empty");
		particle_hydrogen = NativeItemTextureRegistry.texture(new Item().setTranslationKey("particle_hydrogen").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty), RefStrings.MODID + ":particle_hydrogen");
		particle_copper = NativeItemTextureRegistry.texture(new Item().setTranslationKey("particle_copper").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty), RefStrings.MODID + ":particle_copper");
		particle_lead = NativeItemTextureRegistry.texture(new Item().setTranslationKey("particle_lead").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty), RefStrings.MODID + ":particle_lead");
		particle_amat = NativeItemTextureRegistry.texture(new Item().setTranslationKey("particle_amat").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty), RefStrings.MODID + ":particle_amat");
		particle_aschrab = NativeItemTextureRegistry.texture(new Item().setTranslationKey("particle_aschrab").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty), RefStrings.MODID + ":particle_aschrab");
		particle_higgs = NativeItemTextureRegistry.texture(new Item().setTranslationKey("particle_higgs").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty), RefStrings.MODID + ":particle_higgs");
		particle_muon = NativeItemTextureRegistry.texture(new Item().setTranslationKey("particle_muon").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty), RefStrings.MODID + ":particle_muon");
		particle_tachyon = NativeItemTextureRegistry.texture(new Item().setTranslationKey("particle_tachyon").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty), RefStrings.MODID + ":particle_tachyon");
		particle_strange = NativeItemTextureRegistry.texture(new Item().setTranslationKey("particle_strange").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty), RefStrings.MODID + ":particle_strange");
		particle_dark = NativeItemTextureRegistry.texture(new Item().setTranslationKey("particle_dark").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty), RefStrings.MODID + ":particle_dark");
		particle_sparkticle = NativeItemTextureRegistry.texture(new Item().setTranslationKey("particle_sparkticle").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty), RefStrings.MODID + ":particle_sparkticle");
		particle_digamma = NativeItemTextureRegistry.texture(new ItemDigamma(60).setTranslationKey("particle_digamma").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty), RefStrings.MODID + ":particle_digamma");
		particle_lutece = NativeItemTextureRegistry.texture(new Item().setTranslationKey("particle_lutece").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty), RefStrings.MODID + ":particle_lutece");

		singularity = NativeItemTextureRegistry.texture(new ItemDrop().setTranslationKey("singularity").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.nuclear_waste), RefStrings.MODID + ":singularity");
		singularity_counter_resonant = NativeItemTextureRegistry.texture(new ItemDrop().setTranslationKey("singularity_counter_resonant").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.nuclear_waste), RefStrings.MODID + ":singularity_alt");
		singularity_super_heated = NativeItemTextureRegistry.texture(new ItemDrop().setTranslationKey("singularity_super_heated").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.nuclear_waste), RefStrings.MODID + ":singularity_5");
		black_hole = NativeItemTextureRegistry.texture(new ItemDrop().setTranslationKey("black_hole").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.nuclear_waste), RefStrings.MODID + ":singularity_4");
		singularity_spark = NativeItemTextureRegistry.texture(new ItemDrop().setTranslationKey("singularity_spark").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.nuclear_waste), RefStrings.MODID + ":singularity_spark_alt");
		pellet_antimatter = NativeItemTextureRegistry.texture(new ItemDrop().setTranslationKey("pellet_antimatter").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.cell_empty), RefStrings.MODID + ":pellet_antimatter");
		crystal_xen = NativeItemTextureRegistry.texture(new ItemDrop().setTranslationKey("crystal_xen").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":crystal_xen");

		stamp_stone_flat = NativeItemTextureRegistry.texture(new ItemStamp(32, StampType.FLAT).setTranslationKey("stamp_stone_flat").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_stone_flat");
		stamp_stone_plate = NativeItemTextureRegistry.texture(new ItemStamp(32, StampType.PLATE).setTranslationKey("stamp_stone_plate").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_stone_plate");
		stamp_stone_wire = NativeItemTextureRegistry.texture(new ItemStamp(32, StampType.WIRE).setTranslationKey("stamp_stone_wire").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_stone_wire");
		stamp_stone_circuit = NativeItemTextureRegistry.texture(new ItemStamp(32, StampType.CIRCUIT).setTranslationKey("stamp_stone_circuit").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_stone_circuit");
		stamp_iron_flat = NativeItemTextureRegistry.texture(new ItemStamp(64, StampType.FLAT).setTranslationKey("stamp_iron_flat").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_iron_flat");
		stamp_iron_plate = NativeItemTextureRegistry.texture(new ItemStamp(64, StampType.PLATE).setTranslationKey("stamp_iron_plate").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_iron_plate");
		stamp_iron_wire = NativeItemTextureRegistry.texture(new ItemStamp(64, StampType.WIRE).setTranslationKey("stamp_iron_wire").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_iron_wire");
		stamp_iron_circuit = NativeItemTextureRegistry.texture(new ItemStamp(64, StampType.CIRCUIT).setTranslationKey("stamp_iron_circuit").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_iron_circuit");
		stamp_steel_flat = NativeItemTextureRegistry.texture(new ItemStamp(192, StampType.FLAT).setTranslationKey("stamp_steel_flat").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_steel_flat");
		stamp_steel_plate = NativeItemTextureRegistry.texture(new ItemStamp(192, StampType.PLATE).setTranslationKey("stamp_steel_plate").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_steel_plate");
		stamp_steel_wire = NativeItemTextureRegistry.texture(new ItemStamp(192, StampType.WIRE).setTranslationKey("stamp_steel_wire").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_steel_wire");
		stamp_steel_circuit = NativeItemTextureRegistry.texture(new ItemStamp(192, StampType.CIRCUIT).setTranslationKey("stamp_steel_circuit").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_steel_circuit");
		stamp_titanium_flat = NativeItemTextureRegistry.texture(new ItemStamp(256, StampType.FLAT).setTranslationKey("stamp_titanium_flat").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_titanium_flat");
		stamp_titanium_plate = NativeItemTextureRegistry.texture(new ItemStamp(256, StampType.PLATE).setTranslationKey("stamp_titanium_plate").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_titanium_plate");
		stamp_titanium_wire = NativeItemTextureRegistry.texture(new ItemStamp(256, StampType.WIRE).setTranslationKey("stamp_titanium_wire").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_titanium_wire");
		stamp_titanium_circuit = NativeItemTextureRegistry.texture(new ItemStamp(256, StampType.CIRCUIT).setTranslationKey("stamp_titanium_circuit").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_titanium_circuit");
		stamp_obsidian_flat = NativeItemTextureRegistry.texture(new ItemStamp(512, StampType.FLAT).setTranslationKey("stamp_obsidian_flat").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_obsidian_flat");
		stamp_obsidian_plate = NativeItemTextureRegistry.texture(new ItemStamp(512, StampType.PLATE).setTranslationKey("stamp_obsidian_plate").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_obsidian_plate");
		stamp_obsidian_wire = NativeItemTextureRegistry.texture(new ItemStamp(512, StampType.WIRE).setTranslationKey("stamp_obsidian_wire").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_obsidian_wire");
		stamp_obsidian_circuit = NativeItemTextureRegistry.texture(new ItemStamp(512, StampType.CIRCUIT).setTranslationKey("stamp_obsidian_circuit").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_obsidian_circuit");
		stamp_desh_flat = NativeItemTextureRegistry.texture(new ItemStamp(0, StampType.FLAT).setTranslationKey("stamp_desh_flat").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_desh_flat");
		stamp_desh_plate = NativeItemTextureRegistry.texture(new ItemStamp(0, StampType.PLATE).setTranslationKey("stamp_desh_plate").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_desh_plate");
		stamp_desh_wire = NativeItemTextureRegistry.texture(new ItemStamp(0, StampType.WIRE).setTranslationKey("stamp_desh_wire").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_desh_wire");
		stamp_desh_circuit = NativeItemTextureRegistry.texture(new ItemStamp(0, StampType.CIRCUIT).setTranslationKey("stamp_desh_circuit").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_desh_circuit");
		stamp_357 = NativeItemTextureRegistry.texture(new ItemStamp(1000, StampType.C357).setTranslationKey("stamp_357").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_357");
		stamp_44 = NativeItemTextureRegistry.texture(new ItemStamp(1000, StampType.C44).setTranslationKey("stamp_44").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_44");
		stamp_9 = NativeItemTextureRegistry.texture(new ItemStamp(1000, StampType.C9).setTranslationKey("stamp_9").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_9");
		stamp_50 = NativeItemTextureRegistry.texture(new ItemStamp(1000, StampType.C50).setTranslationKey("stamp_50").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_50");
		stamp_book = NativeItemTextureRegistry.texture(new ItemStampBook().setTranslationKey("stamp_book").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":stamp_book");

		stamp_desh_357 = NativeItemTextureRegistry.texture(new ItemStamp(0, StampType.C357).setTranslationKey("stamp_desh_357").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_357_desh");
		stamp_desh_44 = NativeItemTextureRegistry.texture(new ItemStamp(0, StampType.C44).setTranslationKey("stamp_desh_44").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_44_desh");
		stamp_desh_9 = NativeItemTextureRegistry.texture(new ItemStamp(0, StampType.C9).setTranslationKey("stamp_desh_9").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_9_desh");
		stamp_desh_50 = NativeItemTextureRegistry.texture(new ItemStamp(0, StampType.C50).setTranslationKey("stamp_desh_50").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":stamp_50_desh");

		blades_steel = NativeItemTextureRegistry.texture(new ItemBlades(200).setTranslationKey("blades_steel").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":blades_steel");
		blades_titanium = NativeItemTextureRegistry.texture(new ItemBlades(350).setTranslationKey("blades_titanium").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":blades_titanium");
		blades_advanced_alloy = NativeItemTextureRegistry.texture(new ItemBlades(700).setTranslationKey("blades_advanced_alloy").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":blades_advanced_alloy");
		blades_desh = NativeItemTextureRegistry.texture(new ItemBlades(0).setTranslationKey("blades_desh").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":blades_desh");

		mold_base = NativeItemTextureRegistry.texture(new Item().setTranslationKey("mold_base").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":mold_base");
		mold = new ItemMold().setTranslationKey("mold").setCreativeTab(MainRegistry.controlTab);
		scraps = NativeItemTextureRegistry.texture(new ItemScraps().aot(Mats.MAT_BISMUTH, "scraps_bismuth").setTranslationKey("scraps").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":scraps");
		ingot_raw = NativeItemTextureRegistry.texture(new ItemAutogen(MaterialShapes.INGOT).setTranslationKey("ingot_raw").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_raw");
		plate_cast = NativeItemTextureRegistry.texture(new ItemAutogen(MaterialShapes.CASTPLATE).aot(Mats.MAT_BISMUTH, "plate_cast_bismuth").setTranslationKey("plate_cast").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_cast");
		plate_welded = NativeItemTextureRegistry.texture(new ItemAutogen(MaterialShapes.WELDEDPLATE).setTranslationKey("plate_welded").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":plate_welded");
		wire_fine = NativeItemTextureRegistry.texture(new ItemAutogen(MaterialShapes.WIRE)
				.aot(Mats.MAT_ALUMINIUM, "wire_aluminium").aot(Mats.MAT_COPPER, "wire_copper")
				.aot(Mats.MAT_MINGRADE, "wire_red_copper").aot(Mats.MAT_GOLD, "wire_gold")
				.aot(Mats.MAT_TUNGSTEN, "wire_tungsten").aot(Mats.MAT_ALLOY, "wire_advanced_alloy")
				.aot(Mats.MAT_CARBON, "wire_carbon").aot(Mats.MAT_SCHRABIDIUM, "wire_schrabidium")
				.aot(Mats.MAT_ZINC, "wire_zinc")
				.aot(Mats.MAT_MAGTUNG, "wire_magnetized_tungsten").setTranslationKey("wire_fine").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":wire_fine");
		wire_dense = NativeItemTextureRegistry.texture(new ItemAutogen(MaterialShapes.DENSEWIRE).setTranslationKey("wire_dense").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":wire_dense");

		part_barrel_light = NativeItemTextureRegistry.texture(new ItemAutogen(MaterialShapes.LIGHTBARREL).setTranslationKey("part_barrel_light").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":part_barrel_light");
		part_barrel_heavy = NativeItemTextureRegistry.texture(new ItemAutogen(MaterialShapes.HEAVYBARREL).setTranslationKey("part_barrel_heavy").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":part_barrel_heavy");
		part_receiver_light = NativeItemTextureRegistry.texture(new ItemAutogen(MaterialShapes.LIGHTRECEIVER).setTranslationKey("part_receiver_light").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":part_receiver_light");
		part_receiver_heavy = NativeItemTextureRegistry.texture(new ItemAutogen(MaterialShapes.HEAVYRECEIVER).setTranslationKey("part_receiver_heavy").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":part_receiver_heavy");
		part_mechanism = NativeItemTextureRegistry.texture(new ItemAutogen(MaterialShapes.MECHANISM).setTranslationKey("part_mechanism").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":part_mechanism");
		part_stock = NativeItemTextureRegistry.texture(new ItemAutogen(MaterialShapes.STOCK).setTranslationKey("part_stock").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":part_stock");
		part_grip = NativeItemTextureRegistry.texture(new ItemAutogen(MaterialShapes.GRIP).setTranslationKey("part_grip").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":part_grip");

		part_lithium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("part_lithium").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":part_lithium");
		part_beryllium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("part_beryllium").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":part_beryllium");
		part_carbon = NativeItemTextureRegistry.texture(new Item().setTranslationKey("part_carbon").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":part_carbon");
		part_copper = NativeItemTextureRegistry.texture(new Item().setTranslationKey("part_copper").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":part_copper");
		part_plutonium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("part_plutonium").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":part_plutonium");

		laser_crystal_co2 = NativeItemTextureRegistry.texture(new ItemFELCrystal(EnumWavelengths.IR).setTranslationKey("laser_crystal_co2").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":laser_crystal_co2");
		laser_crystal_bismuth = NativeItemTextureRegistry.texture(new ItemFELCrystal(EnumWavelengths.VISIBLE).setTranslationKey("laser_crystal_bismuth").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":laser_crystal_bismuth");
		laser_crystal_cmb = NativeItemTextureRegistry.texture(new ItemFELCrystal(EnumWavelengths.UV).setTranslationKey("laser_crystal_cmb").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":laser_crystal_cmb");
		laser_crystal_iron = NativeItemTextureRegistry.texture(new ItemFELCrystal(EnumWavelengths.XRAY).setTranslationKey("laser_crystal_iron").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":laser_crystal_iron");
		laser_crystal_dnt = NativeItemTextureRegistry.texture(new ItemFELCrystal(EnumWavelengths.GAMMA).setTranslationKey("laser_crystal_dnt").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":laser_crystal_dnt");
		laser_crystal_digamma = NativeItemTextureRegistry.texture(new ItemFELCrystal(EnumWavelengths.DRX).setTranslationKey("laser_crystal_digamma").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":laser_crystal_digamma");

		thermo_element = NativeItemTextureRegistry.texture(new Item().setTranslationKey("thermo_element").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":thermo_element");
		catalytic_converter = NativeItemTextureRegistry.texture(new Item().setTranslationKey("catalytic_converter").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":catalytic_converter");

		fuel_additive = NativeItemTextureRegistry.texture(new ItemEnumMulti(ItemEnums.EnumFuelAdditive.class, true, true).setTranslationKey("fuel_additive").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":fuel_additive");

		canister_empty = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("canister_empty").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":canister_empty");
		canister_full = NativeItemTextureRegistry.texture(new ItemCanister().setTranslationKey("canister_full").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.canister_empty), RefStrings.MODID + ":canister_empty");
		canister_napalm = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("canister_napalm").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.canister_empty), RefStrings.MODID + ":canister_napalm");
		gas_empty = NativeItemTextureRegistry.texture(new Item().setTranslationKey("gas_empty").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":gas_empty");
		gas_full = NativeItemTextureRegistry.texture(new ItemGasTank().setTranslationKey("gas_full").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.gas_empty), RefStrings.MODID + ":gas_empty");

		ItemSimpleConsumable.init();

		//TODO: move all this crap to ItemSimpleConsumable
		syringe_empty = NativeItemTextureRegistry.texture(new Item().setTranslationKey("syringe_empty").setFull3D().setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":syringe_empty");
		syringe_metal_empty = NativeItemTextureRegistry.texture(new Item().setTranslationKey("syringe_metal_empty").setFull3D().setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":syringe_metal_empty");
		syringe_metal_stimpak = NativeItemTextureRegistry.texture(new ItemSyringe().setTranslationKey("syringe_metal_stimpak").setFull3D().setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":syringe_metal_stimpak");
		syringe_metal_medx = NativeItemTextureRegistry.texture(new ItemSyringe().setTranslationKey("syringe_metal_medx").setFull3D().setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":syringe_metal_medx");
		syringe_metal_psycho = NativeItemTextureRegistry.texture(new ItemSyringe().setTranslationKey("syringe_metal_psycho").setFull3D().setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":syringe_metal_psycho");
		syringe_metal_super = NativeItemTextureRegistry.texture(new ItemSyringe().setTranslationKey("syringe_metal_super").setFull3D().setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":syringe_metal_super");
		syringe_taint = NativeItemTextureRegistry.texture(new ItemSyringe().setTranslationKey("syringe_taint").setFull3D().setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":syringe_taint");
		syringe_mkunicorn = NativeItemTextureRegistry.texture(new ItemSyringe().setTranslationKey("syringe_mkunicorn").setFull3D().setCreativeTab(null), RefStrings.MODID + ":syringe_mkunicorn");

		med_bag = NativeItemTextureRegistry.texture(new ItemSyringe().setTranslationKey("med_bag").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":med_bag");
		radx = NativeItemTextureRegistry.texture(new ItemPill(0).setTranslationKey("radx").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":radx");
		siox = NativeItemTextureRegistry.texture(new ItemPill(0).setTranslationKey("siox").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":siox");
		pill_herbal = NativeItemTextureRegistry.texture(new ItemPill(0).setTranslationKey("pill_herbal").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":pill_herbal");
		xanax = NativeItemTextureRegistry.texture(new ItemPill(0).setTranslationKey("xanax").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":xanax");

		animan = NativeItemTextureRegistry.texture(new ItemPill(0).setTranslationKey("animan").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":animan");


		fmn = NativeItemTextureRegistry.texture(new ItemPill(0).setTranslationKey("fmn").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":tablet");
		five_htp = NativeItemTextureRegistry.texture(new ItemPill(0).setTranslationKey("five_htp").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":5htp");
		pill_iodine = NativeItemTextureRegistry.texture(new ItemPill(0).setTranslationKey("pill_iodine").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":pill_iodine");
		plan_c = NativeItemTextureRegistry.texture(new ItemPill(0).setTranslationKey("plan_c").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":plan_c");
		pill_red = NativeItemTextureRegistry.texture(new ItemPill(0).setTranslationKey("pill_red").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":pill_red");
		stealth_boy = NativeItemTextureRegistry.texture(new ItemStarterKit().setTranslationKey("stealth_boy").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":stealth_boy");
		gas_mask_filter = NativeItemTextureRegistry.texture(new ItemFilter().setTranslationKey("gas_mask_filter"), RefStrings.MODID + ":gas_mask_filter");
		gas_mask_filter_mono = NativeItemTextureRegistry.texture(new ItemFilter().setTranslationKey("gas_mask_filter_mono"), RefStrings.MODID + ":gas_mask_filter_mono");
		gas_mask_filter_combo = NativeItemTextureRegistry.texture(new ItemFilter().setTranslationKey("gas_mask_filter_combo"), RefStrings.MODID + ":gas_mask_filter_combo");
		gas_mask_filter_rag = NativeItemTextureRegistry.texture(new ItemFilter().setTranslationKey("gas_mask_filter_rag"), RefStrings.MODID + ":gas_mask_filter_rag");
		gas_mask_filter_piss = NativeItemTextureRegistry.texture(new ItemFilter().setTranslationKey("gas_mask_filter_piss"), RefStrings.MODID + ":gas_mask_filter_piss");
		jetpack_tank = NativeItemTextureRegistry.texture(new ItemSyringe().setTranslationKey("jetpack_tank").setMaxStackSize(16).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":jetpack_tank");
		gun_kit_1 = NativeItemTextureRegistry.texture(new ItemRepairKit(10).setTranslationKey("gun_kit_1").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":gun_kit_1");
		gun_kit_2 = NativeItemTextureRegistry.texture(new ItemRepairKit(100).setTranslationKey("gun_kit_2").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":gun_kit_2");
		cbt_device = NativeItemTextureRegistry.texture(new ItemSyringe().setTranslationKey("cbt_device").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":cbt_device");
		cigarette = NativeItemTextureRegistry.texture(new ItemCigarette().setTranslationKey("cigarette").setFull3D().setMaxStackSize(16).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":cigarette");
		crackpipe = NativeItemTextureRegistry.texture(new ItemCigarette().setTranslationKey("crackpipe").setFull3D().setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":crackpipe");
		bdcl = NativeItemTextureRegistry.texture(new ItemBDCL().setTranslationKey("bdcl").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":bdcl");
		lox_tank = NativeItemTextureRegistry.texture(new ItemSyringe().setTranslationKey("lox_tank").setMaxStackSize(16).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":lox_tank");

		attachment_mask = NativeItemTextureRegistry.texture(new ItemModGasmask().setTranslationKey("attachment_mask"), RefStrings.MODID + ":attachment_mask");
		attachment_mask_mono = NativeItemTextureRegistry.texture(new ItemModGasmask().setTranslationKey("attachment_mask_mono"), RefStrings.MODID + ":attachment_mask_mono");
		back_tesla = NativeItemTextureRegistry.texture(new ItemModTesla().setTranslationKey("back_tesla"), RefStrings.MODID + ":back_tesla");
		servo_set = NativeItemTextureRegistry.texture(new ItemModServos().setTranslationKey("servo_set"), RefStrings.MODID + ":servo_set");
		servo_set_desh = NativeItemTextureRegistry.texture(new ItemModServos().setTranslationKey("servo_set_desh"), RefStrings.MODID + ":servo_set_desh");
		pads_rubber = NativeItemTextureRegistry.texture(new ItemModPads(0.5F).setTranslationKey("pads_rubber"), RefStrings.MODID + ":pads_rubber");
		pads_slime = NativeItemTextureRegistry.texture(new ItemModPads(0.25F).setTranslationKey("pads_slime"), RefStrings.MODID + ":pads_slime");
		pads_static = NativeItemTextureRegistry.texture(new ItemModPads(0.75F).setTranslationKey("pads_static"), RefStrings.MODID + ":pads_static");
		cladding_paint = NativeItemTextureRegistry.texture(new ItemModCladding(0.025).setTranslationKey("cladding_paint"), RefStrings.MODID + ":cladding_paint");
		cladding_rubber = NativeItemTextureRegistry.texture(new ItemModCladding(0.005).setTranslationKey("cladding_rubber"), RefStrings.MODID + ":cladding_rubber");
		cladding_lead = NativeItemTextureRegistry.texture(new ItemModCladding(0.1).setTranslationKey("cladding_lead"), RefStrings.MODID + ":cladding_lead");
		cladding_desh = NativeItemTextureRegistry.texture(new ItemModCladding(0.2).setTranslationKey("cladding_desh"), RefStrings.MODID + ":cladding_desh");
		cladding_ghiorsium = NativeItemTextureRegistry.texture(new ItemModCladding(0.5).setTranslationKey("cladding_ghiorsium"), RefStrings.MODID + ":cladding_ghiorsium");
		cladding_iron = NativeItemTextureRegistry.texture(new ItemModIron().setTranslationKey("cladding_iron"), RefStrings.MODID + ":cladding_iron");
		cladding_obsidian = NativeItemTextureRegistry.texture(new ItemModObsidian().setTranslationKey("cladding_obsidian"), RefStrings.MODID + ":cladding_obsidian");
		insert_kevlar = NativeItemTextureRegistry.texture(new ItemModInsert(1500, 1F, 0.9F, 1F, 1F).setTranslationKey("insert_kevlar"), RefStrings.MODID + ":insert_kevlar");
		insert_sapi = NativeItemTextureRegistry.texture(new ItemModInsert(1750, 1F, 0.85F, 1F, 1F).setTranslationKey("insert_sapi"), RefStrings.MODID + ":insert_sapi");
		insert_esapi = NativeItemTextureRegistry.texture(new ItemModInsert(2000, 0.95F, 0.8F, 1F, 1F).setTranslationKey("insert_esapi"), RefStrings.MODID + ":insert_esapi");
		insert_xsapi = NativeItemTextureRegistry.texture(new ItemModInsert(2500, 0.9F, 0.75F, 1F, 1F).setTranslationKey("insert_xsapi"), RefStrings.MODID + ":insert_xsapi");
		insert_steel = NativeItemTextureRegistry.texture(new ItemModInsert(1000, 1F, 0.95F, 0.75F, 0.95F).setTranslationKey("insert_steel"), RefStrings.MODID + ":insert_steel");
		insert_du = NativeItemTextureRegistry.texture(new ItemModInsert(1500, 0.9F, 0.85F, 0.5F, 0.9F).setTranslationKey("insert_du"), RefStrings.MODID + ":insert_du");
		insert_polonium = NativeItemTextureRegistry.texture(new ItemModInsert(500, 0.9F, 1F, 0.95F, 0.9F).setTranslationKey("insert_polonium"), RefStrings.MODID + ":insert_polonium");
		insert_ghiorsium = NativeItemTextureRegistry.texture(new ItemModInsert(2000, 0.8F, 0.75F, 0.35F, 0.9F).setTranslationKey("insert_ghiorsium"), RefStrings.MODID + ":insert_ghiorsium");
		insert_cmb = NativeItemTextureRegistry.texture(new ItemModInsert(9999, 0.7F, 0.9F, 0.4F, 1F).withCorrosionProtection().setTranslationKey("insert_cmb"), RefStrings.MODID + ":insert_cmb");
		insert_era = NativeItemTextureRegistry.texture(new ItemModInsert(25, 0.5F, 1F, 0.25F, 1F).setTranslationKey("insert_era"), RefStrings.MODID + ":insert_era");
		insert_yharonite = NativeItemTextureRegistry.texture(new ItemModInsert(9999, 0.01F, 1F, 1F, 1F).withCorrosionProtection().setTranslationKey("insert_yharonite"), RefStrings.MODID + ":insert_yharonite");
		insert_doxium = NativeItemTextureRegistry.texture(new ItemModInsert(9999, 5.0F, 1F, 1F, 1F).setTranslationKey("insert_doxium"), RefStrings.MODID + ":insert_doxium");
		armor_polish = NativeItemTextureRegistry.texture(new ItemModPolish().setTranslationKey("armor_polish"), RefStrings.MODID + ":armor_polish");
		bandaid = NativeItemTextureRegistry.texture(new ItemModBandaid().setTranslationKey("bandaid"), RefStrings.MODID + ":bandaid");
		serum = NativeItemTextureRegistry.texture(new ItemModSerum().setTranslationKey("serum"), RefStrings.MODID + ":serum");
		quartz_plutonium = NativeItemTextureRegistry.texture(new ItemModQuartz().setTranslationKey("quartz_plutonium"), RefStrings.MODID + ":quartz_plutonium");
		morning_glory = NativeItemTextureRegistry.texture(new ItemModMorningGlory().setTranslationKey("morning_glory"), RefStrings.MODID + ":morning_glory");
		lodestone = NativeItemTextureRegistry.texture(new ItemModLodestone(5).setTranslationKey("lodestone"), RefStrings.MODID + ":lodestone");
		horseshoe_magnet = NativeItemTextureRegistry.texture(new ItemModLodestone(8).setTranslationKey("horseshoe_magnet"), RefStrings.MODID + ":horseshoe_magnet");
		industrial_magnet = NativeItemTextureRegistry.texture(new ItemModLodestone(12).setTranslationKey("industrial_magnet"), RefStrings.MODID + ":industrial_magnet");
		bathwater = NativeItemTextureRegistry.texture(new ItemModBathwater().setTranslationKey("bathwater"), RefStrings.MODID + ":bathwater");
		bathwater_mk2 = NativeItemTextureRegistry.texture(new ItemModBathwater().setTranslationKey("bathwater_mk2"), RefStrings.MODID + ":bathwater_mk2");
		spider_milk = NativeItemTextureRegistry.texture(new ItemModMilk().setTranslationKey("spider_milk"), RefStrings.MODID + ":spider_milk");
		ink = NativeItemTextureRegistry.texture(new ItemModInk().setTranslationKey("ink"), RefStrings.MODID + ":ink");
		heart_piece = NativeItemTextureRegistry.texture(new ItemModHealth(5F).setTranslationKey("heart_piece"), RefStrings.MODID + ":heart_piece");
		heart_container = NativeItemTextureRegistry.texture(new ItemModHealth(20F).setTranslationKey("heart_container"), RefStrings.MODID + ":heart_container");
		heart_booster = NativeItemTextureRegistry.texture(new ItemModHealth(40F).setTranslationKey("heart_booster"), RefStrings.MODID + ":heart_booster");
		heart_fab = NativeItemTextureRegistry.texture(new ItemModHealth(60F).setTranslationKey("heart_fab"), RefStrings.MODID + ":heart_fab");
		black_diamond = NativeItemTextureRegistry.texture(new ItemModHealth(40F).setTranslationKey("black_diamond"), RefStrings.MODID + ":black_diamond");
		wd40 = NativeItemTextureRegistry.texture(new ItemModWD40().setTranslationKey("wd40"), RefStrings.MODID + ":wd40");
		scrumpy = NativeItemTextureRegistry.texture(new ItemModRevive(1).setTranslationKey("scrumpy"), RefStrings.MODID + ":scrumpy");
		wild_p = NativeItemTextureRegistry.texture(new ItemModRevive(3).setTranslationKey("wild_p"), RefStrings.MODID + ":wild_p");
		shackles = NativeItemTextureRegistry.texture(new ItemModShackles().setTranslationKey("shackles"), RefStrings.MODID + ":shackles");
		injector_5htp = NativeItemTextureRegistry.texture(new ItemModAuto().setTranslationKey("injector_5htp"), RefStrings.MODID + ":injector_5htp");
		injector_knife = NativeItemTextureRegistry.texture(new ItemModKnife().setTranslationKey("injector_knife"), RefStrings.MODID + ":injector_knife");
		medal_liquidator = NativeItemTextureRegistry.texture(new ItemModMedal().setTranslationKey("medal_liquidator"), RefStrings.MODID + ":medal_liquidator");
		v1 = NativeItemTextureRegistry.texture(new ItemModV1().setTranslationKey("v1"), RefStrings.MODID + ":v1");
		protection_charm = NativeItemTextureRegistry.texture(new ItemModCharm().setTranslationKey("protection_charm"), RefStrings.MODID + ":protection_charm");
		meteor_charm = NativeItemTextureRegistry.texture(new ItemModCharm().setTranslationKey("meteor_charm"), RefStrings.MODID + ":meteor_charm");
		neutrino_lens = NativeItemTextureRegistry.texture(new ItemModLens().setTranslationKey("neutrino_lens"), RefStrings.MODID + ":neutrino_lens");
		gas_tester = NativeItemTextureRegistry.texture(new ItemModSensor().setTranslationKey("gas_tester"), RefStrings.MODID + ":gas_tester");
		defuser_gold = NativeItemTextureRegistry.texture(new ItemModDefuser().setTranslationKey("defuser_gold"), RefStrings.MODID + ":defuser_gold");
		ballistic_gauntlet = NativeItemTextureRegistry.texture(new ItemModTwoKick().setTranslationKey("ballistic_gauntlet"), RefStrings.MODID + ":ballistic_gauntlet");
		night_vision = NativeItemTextureRegistry.texture(new ItemModNightVision().setTranslationKey("night_vision"), RefStrings.MODID + ":night_vision");
		card_aos = NativeItemTextureRegistry.texture(new ItemModCard().setTranslationKey("card_aos"), RefStrings.MODID + ":card_aos");
		card_qos = NativeItemTextureRegistry.texture(new ItemModCard().setTranslationKey("card_qos"), RefStrings.MODID + ":card_qos");
		australium_iii = NativeItemTextureRegistry.texture(new ItemModShield(25F).setTranslationKey("australium_iii"), RefStrings.MODID + ":australium_iii");
		armor_battery = NativeItemTextureRegistry.texture(new ItemModBattery(1.25D).setTranslationKey("armor_battery"), RefStrings.MODID + ":armor_battery");
		armor_battery_mk2 = NativeItemTextureRegistry.texture(new ItemModBattery(1.5D).setTranslationKey("armor_battery_mk2"), RefStrings.MODID + ":armor_battery_mk2");
		armor_battery_mk3 = NativeItemTextureRegistry.texture(new ItemModBattery(2D).setTranslationKey("armor_battery_mk3"), RefStrings.MODID + ":armor_battery_mk3");
		flippers = NativeItemTextureRegistry.texture(new ItemModFlippers().setTranslationKey("flippers"), RefStrings.MODID + ":flippers");
		heavy_boots = NativeItemTextureRegistry.texture(new ItemModHeavyBoots().setTranslationKey("heavy_boots"), RefStrings.MODID + ":heavy_boots");

		cap_nuka = NativeItemTextureRegistry.texture(new Item().setTranslationKey("cap_nuka").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":cap_nuka");
		cap_quantum = NativeItemTextureRegistry.texture(new Item().setTranslationKey("cap_quantum").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":cap_quantum");
		cap_sparkle = NativeItemTextureRegistry.texture(new Item().setTranslationKey("cap_sparkle").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":cap_sparkle");
		cap_rad = NativeItemTextureRegistry.texture(new Item().setTranslationKey("cap_rad").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":cap_rad");
		cap_korl = NativeItemTextureRegistry.texture(new Item().setTranslationKey("cap_korl").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":cap_korl");
		cap_fritz = NativeItemTextureRegistry.texture(new Item().setTranslationKey("cap_fritz").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":cap_fritz");
		ring_pull = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ring_pull").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":ring_pull");
		hard_drive = NativeItemTextureRegistry.texture(new Item().setTranslationKey("hard_drive").setMaxStackSize(64).setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":votv_e");

		can_empty = NativeItemTextureRegistry.texture(new Item().setTranslationKey("can_empty").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":can_empty");
		can_smart = NativeItemTextureRegistry.texture(new ItemEnergy().makeCan().setTranslationKey("can_smart"), RefStrings.MODID + ":can_smart");
		can_creature = NativeItemTextureRegistry.texture(new ItemEnergy().makeCan().setTranslationKey("can_creature"), RefStrings.MODID + ":can_creature");
		can_redbomb = NativeItemTextureRegistry.texture(new ItemEnergy().makeCan().setTranslationKey("can_redbomb"), RefStrings.MODID + ":can_redbomb");
		can_mrsugar = NativeItemTextureRegistry.texture(new ItemEnergy().makeCan().setTranslationKey("can_mrsugar"), RefStrings.MODID + ":can_mrsugar");
		can_overcharge = NativeItemTextureRegistry.texture(new ItemEnergy().makeCan().setTranslationKey("can_overcharge"), RefStrings.MODID + ":can_overcharge");
		can_luna = NativeItemTextureRegistry.texture(new ItemEnergy().makeCan().setTranslationKey("can_luna"), RefStrings.MODID + ":can_luna");
		can_bepis = NativeItemTextureRegistry.texture(new ItemEnergy().makeCan().setTranslationKey("can_bepis"), RefStrings.MODID + ":can_bepis");
		can_breen = NativeItemTextureRegistry.texture(new ItemEnergy().makeCan().setTranslationKey("can_breen"), RefStrings.MODID + ":can_breen");
		can_mug = NativeItemTextureRegistry.texture(new ItemEnergy().makeCan().setTranslationKey("can_mug"), RefStrings.MODID + ":can_mug");
		bottle_empty = NativeItemTextureRegistry.texture(new Item().setTranslationKey("bottle_empty"), RefStrings.MODID + ":bottle_empty");
		bottle_nuka = NativeItemTextureRegistry.texture(new ItemEnergy().makeBottle(bottle_empty, cap_nuka).setTranslationKey("bottle_nuka"), RefStrings.MODID + ":bottle_nuka");
		bottle_cherry = NativeItemTextureRegistry.texture(new ItemEnergy().makeBottle(bottle_empty, cap_nuka).setTranslationKey("bottle_cherry").setContainerItem(ModItems.bottle_empty), RefStrings.MODID + ":bottle_cherry");
		bottle_quantum = NativeItemTextureRegistry.texture(new ItemEnergy().makeBottle(bottle_empty, cap_quantum).setTranslationKey("bottle_quantum").setContainerItem(ModItems.bottle_empty), RefStrings.MODID + ":bottle_quantum");
		bottle_sparkle = NativeItemTextureRegistry.texture(new ItemEnergy().makeBottle(bottle_empty, cap_sparkle).setTranslationKey("bottle_sparkle").setContainerItem(ModItems.bottle_empty), RefStrings.MODID + ":bottle_sparkle");
		bottle_rad = NativeItemTextureRegistry.texture(new ItemEnergy().makeBottle(bottle_empty, cap_rad).setTranslationKey("bottle_rad").setContainerItem(ModItems.bottle_empty), RefStrings.MODID + ":bottle_rad");
		bottle2_empty = NativeItemTextureRegistry.texture(new Item().setTranslationKey("bottle2_empty"), RefStrings.MODID + ":bottle2_empty");
		bottle2_korl = NativeItemTextureRegistry.texture(new ItemEnergy().makeBottle(bottle2_empty, cap_korl).setTranslationKey("bottle2_korl").setContainerItem(ModItems.bottle2_empty), RefStrings.MODID + ":bottle2_korl");
		bottle2_fritz = NativeItemTextureRegistry.texture(new ItemEnergy().makeBottle(bottle2_empty, cap_fritz).setTranslationKey("bottle2_fritz").setContainerItem(ModItems.bottle2_empty), RefStrings.MODID + ":bottle2_fritz");
		flask_infusion = NativeItemTextureRegistry.texture(new ItemFlask().setTranslationKey("flask_infusion").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":flask");
		chocolate_milk = NativeItemTextureRegistry.texture(new ItemEnergy().setTranslationKey("chocolate_milk"), RefStrings.MODID + ":chocolate_milk");
		coffee = NativeItemTextureRegistry.texture(new ItemEnergy().setTranslationKey("coffee"), RefStrings.MODID + ":coffee");
		coffee_radium = NativeItemTextureRegistry.texture(new ItemEnergy().setTranslationKey("coffee_radium"), RefStrings.MODID + ":coffee_radium");
		chocolate = NativeItemTextureRegistry.texture(new ItemPill(0).setTranslationKey("chocolate").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":chocolate");

		glass_smilk = NativeItemTextureRegistry.texture(new ItemEnergy().makeGlass().setTranslationKey("glass_smilk"), RefStrings.MODID + ":glass_smilk");
		glass_empty = NativeItemTextureRegistry.texture(new Item().setTranslationKey("glass_empty").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":glass_empty");
		strawberry = NativeItemTextureRegistry.texture(new ItemSeedFood(3, 0.4F, ModBlocks.crop_strawberry, Blocks.FARMLAND).setTranslationKey("strawberry").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":strawberry");
		mint_leaves = NativeItemTextureRegistry.texture(new ItemSeedFood(3, 0.4F, ModBlocks.crop_mint, Blocks.FARMLAND).setTranslationKey("mint_leaves").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":mint_leaves");
		bean_raw = NativeItemTextureRegistry.texture(new ItemSeedFood(1, 0.2F, ModBlocks.crop_coffee, Blocks.FARMLAND).setTranslationKey("bean_raw").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":coffeebeanraw");
		powder_coffee = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("powder_coffee").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":powder_coffee");
		bean_roast = NativeItemTextureRegistry.texture(new Item().setTranslationKey("bean_roast").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":coffeebeanroast");
		cmug_empty = NativeItemTextureRegistry.texture(new Item().setTranslationKey("cmug_empty").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":mug_empty");
		teaseeds = NativeItemTextureRegistry.texture(new ItemSeedFood(0, 0, ModBlocks.crop_tea, Blocks.FARMLAND).setTranslationKey("teaseeds").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":teaseeds");
		tea_leaf = NativeItemTextureRegistry.texture(new Item().setTranslationKey("tea_leaf").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":tea_leaf");
		teacup_empty = NativeItemTextureRegistry.texture(new Item().setTranslationKey("teacup_empty").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":teacup");
		teacup = NativeItemTextureRegistry.texture(new ItemEnergy().makeGlass().setTranslationKey("teacup").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":teacup_full");
		bottle_honey = NativeItemTextureRegistry.texture(new ItemEnergy().makeGlass().setTranslationKey("bottle_honey").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":bottle_honey");
		paraffin_seeds = NativeItemTextureRegistry.texture(new ItemSeedFood(1, 0.2F, ModBlocks.crop_paraffin, ModBlocks.rubber_farmland).setTranslationKey("paraffin_seeds").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":paraffin_seeds");
		canned_conserve = (ItemEnumMulti) NativeItemTextureRegistry.texture(new ItemConserve().setTranslationKey("canned_conserve").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":canned");
		can_key = NativeItemTextureRegistry.texture(new Item().setTranslationKey("can_key").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":can_key");

		boat_rubber = NativeItemTextureRegistry.texture(new ItemBoatRubber().setTranslationKey("boat_rubber"), RefStrings.MODID + ":boat_rubber");
		cart = new ItemModMinecart().setTranslationKey("cart");
		train = new ItemTrain().setTranslationKey("train");
		drone = new ItemDrone().setTranslationKey("drone");

		coin_creeper = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.UNCOMMON).setTranslationKey("coin_creeper").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":coin_creeper");
		coin_radiation = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.UNCOMMON).setTranslationKey("coin_radiation").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":coin_radiation");
		coin_maskman = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.UNCOMMON).setTranslationKey("coin_maskman").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":coin_maskman");
		coin_worm = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.UNCOMMON).setTranslationKey("coin_worm").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":coin_worm");
		coin_ufo = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.UNCOMMON).setTranslationKey("coin_ufo").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":coin_ufo");
		coin_airliner = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.UNCOMMON).setTranslationKey("coin_airliner").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":coin_airliner");

		rod_empty = NativeItemTextureRegistry.texture(new Item().setTranslationKey("rod_empty").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":rod_empty");
		rod = (ItemEnumMulti) new ItemBreedingRod().setTranslationKey("rod").setContainerItem(ModItems.rod_empty).setCreativeTab(MainRegistry.controlTab);
		rod_dual_empty = NativeItemTextureRegistry.texture(new Item().setTranslationKey("rod_dual_empty").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":rod_dual_empty");
		rod_dual = (ItemEnumMulti) new ItemBreedingRod().setTranslationKey("rod_dual").setContainerItem(ModItems.rod_dual_empty).setCreativeTab(MainRegistry.controlTab);
		rod_quad_empty = NativeItemTextureRegistry.texture(new Item().setTranslationKey("rod_quad_empty").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":rod_quad_empty");
		rod_quad = (ItemEnumMulti) new ItemBreedingRod().setTranslationKey("rod_quad").setContainerItem(ModItems.rod_quad_empty).setCreativeTab(MainRegistry.controlTab);

		rod_zirnox_empty = NativeItemTextureRegistry.texture(new Item().setTranslationKey("rod_zirnox_empty").setMaxStackSize(64).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":rod_zirnox_empty");
		rod_zirnox_tritium = NativeItemTextureRegistry.texture(new Item().setTranslationKey("rod_zirnox_tritium").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.rod_zirnox_empty), RefStrings.MODID + ":rod_zirnox_tritium");
		rod_zirnox = (ItemEnumMulti) NativeItemTextureRegistry.texture(new ItemZirnoxRod().setTranslationKey("rod_zirnox").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":rod_zirnox");

		full_drive = (ItemEnumMulti) NativeItemTextureRegistry.texture(new ItemVOTVdrive().setTranslationKey("hard_drive_full").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":votv_f");

		rod_zirnox_natural_uranium_fuel_depleted = NativeItemTextureRegistry.texture(new Item().setTranslationKey("rod_zirnox_natural_uranium_fuel_depleted").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.rod_zirnox_empty), RefStrings.MODID + ":rod_zirnox_uranium_fuel_depleted");
		rod_zirnox_uranium_fuel_depleted = NativeItemTextureRegistry.texture(new Item().setTranslationKey("rod_zirnox_uranium_fuel_depleted").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.rod_zirnox_empty), RefStrings.MODID + ":rod_zirnox_uranium_fuel_depleted");
		rod_zirnox_thorium_fuel_depleted = NativeItemTextureRegistry.texture(new Item().setTranslationKey("rod_zirnox_thorium_fuel_depleted").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.rod_zirnox_empty), RefStrings.MODID + ":rod_zirnox_thorium_fuel_depleted");
		rod_zirnox_mox_fuel_depleted = NativeItemTextureRegistry.texture(new Item().setTranslationKey("rod_zirnox_mox_fuel_depleted").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.rod_zirnox_empty), RefStrings.MODID + ":rod_zirnox_mox_fuel_depleted");
		rod_zirnox_plutonium_fuel_depleted =  NativeItemTextureRegistry.texture(new Item().setTranslationKey("rod_zirnox_plutonium_fuel_depleted").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.rod_zirnox_empty), RefStrings.MODID + ":rod_zirnox_plutonium_fuel_depleted");
		rod_zirnox_u233_fuel_depleted = NativeItemTextureRegistry.texture(new Item().setTranslationKey("rod_zirnox_u233_fuel_depleted").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.rod_zirnox_empty), RefStrings.MODID + ":rod_zirnox_u233_fuel_depleted");
		rod_zirnox_u235_fuel_depleted = NativeItemTextureRegistry.texture(new Item().setTranslationKey("rod_zirnox_u235_fuel_depleted").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.rod_zirnox_empty), RefStrings.MODID + ":rod_zirnox_u235_fuel_depleted");
		rod_zirnox_les_fuel_depleted = NativeItemTextureRegistry.texture(new Item().setTranslationKey("rod_zirnox_les_fuel_depleted").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.rod_zirnox_empty), RefStrings.MODID + ":rod_zirnox_les_fuel_depleted");
		rod_zirnox_zfb_mox_depleted = NativeItemTextureRegistry.texture(new Item().setTranslationKey("rod_zirnox_zfb_mox_depleted").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.rod_zirnox_empty), RefStrings.MODID + ":rod_zirnox_zfb_mox_depleted");

		waste_natural_uranium = NativeItemTextureRegistry.texture(new ItemDepletedFuel().setTranslationKey("waste_natural_uranium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":waste_uranium");
		waste_uranium = NativeItemTextureRegistry.texture(new ItemDepletedFuel().setTranslationKey("waste_uranium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":waste_uranium");
		waste_thorium = NativeItemTextureRegistry.texture(new ItemDepletedFuel().setTranslationKey("waste_thorium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":waste_thorium");
		waste_mox = NativeItemTextureRegistry.texture(new ItemDepletedFuel().setTranslationKey("waste_mox").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":waste_mox");
		waste_plutonium = NativeItemTextureRegistry.texture(new ItemDepletedFuel().setTranslationKey("waste_plutonium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":waste_plutonium");
		waste_u233 = NativeItemTextureRegistry.texture(new ItemDepletedFuel().setTranslationKey("waste_u233").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":waste_uranium");
		waste_u235 = NativeItemTextureRegistry.texture(new ItemDepletedFuel().setTranslationKey("waste_u235").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":waste_uranium");
		waste_schrabidium = NativeItemTextureRegistry.texture(new ItemDepletedFuel().setTranslationKey("waste_schrabidium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":waste_schrabidium");
		waste_zfb_mox = NativeItemTextureRegistry.texture(new ItemDepletedFuel().setTranslationKey("waste_zfb_mox").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":waste_zfb_mox");

		waste_plate_u233 = NativeItemTextureRegistry.texture(new ItemDepletedFuel().setTranslationKey("waste_plate_u233").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":waste_plate_uranium");
		waste_plate_u235 = NativeItemTextureRegistry.texture(new ItemDepletedFuel().setTranslationKey("waste_plate_u235").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":waste_plate_uranium");
		waste_plate_mox = NativeItemTextureRegistry.texture(new ItemDepletedFuel().setTranslationKey("waste_plate_mox").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":waste_plate_mox");
		waste_plate_pu239 = NativeItemTextureRegistry.texture(new ItemDepletedFuel().setTranslationKey("waste_plate_pu239").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":waste_plate_mox");
		waste_plate_sa326 = NativeItemTextureRegistry.texture(new ItemDepletedFuel().setTranslationKey("waste_plate_sa326").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":waste_plate_sa326");
		waste_plate_ra226be = NativeItemTextureRegistry.texture(new ItemDepletedFuel().setTranslationKey("waste_plate_ra226be").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":waste_plate_ra226be");
		waste_plate_pu238be = NativeItemTextureRegistry.texture(new ItemDepletedFuel().setTranslationKey("waste_plate_pu238be").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":waste_plate_pu238be");

		pile_rod_uranium = NativeItemTextureRegistry.texture(new ItemPileRod().setTranslationKey("pile_rod_uranium").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":pile_rod_uranium");
		pile_rod_pu239 = NativeItemTextureRegistry.texture(new ItemPileRod().setTranslationKey("pile_rod_pu239").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":pile_rod_pu239");
		pile_rod_plutonium = NativeItemTextureRegistry.texture(new ItemPileRod().setTranslationKey("pile_rod_plutonium").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":pile_rod_plutonium");
		pile_rod_source = NativeItemTextureRegistry.texture(new ItemPileRod().setTranslationKey("pile_rod_source").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":pile_rod_source");
		pile_rod_boron = NativeItemTextureRegistry.texture(new ItemPileRod().setTranslationKey("pile_rod_boron").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":pile_rod_boron");
		pile_rod_lithium = NativeItemTextureRegistry.texture(new ItemPileRod().setTranslationKey("pile_rod_lithium").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":pile_rod_lithium");
		pile_rod_detector = NativeItemTextureRegistry.texture(new ItemPileRod().setTranslationKey("pile_rod_detector").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":pile_rod_detector");

		plate_fuel_u233 = NativeItemTextureRegistry.texture(new ItemPlateFuel(2200000).setFunction(FunctionEnum.SQUARE_ROOT, 50).setTranslationKey("plate_fuel_u233").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":plate_fuel_u233");
		plate_fuel_u235 = NativeItemTextureRegistry.texture(new ItemPlateFuel(2200000).setFunction(FunctionEnum.SQUARE_ROOT, 40).setTranslationKey("plate_fuel_u235").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":plate_fuel_u235");
		plate_fuel_mox = NativeItemTextureRegistry.texture(new ItemPlateFuel(2400000).setFunction(FunctionEnum.LOGARITHM, 50).setTranslationKey("plate_fuel_mox").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":plate_fuel_mox");
		plate_fuel_pu239 = NativeItemTextureRegistry.texture(new ItemPlateFuel(2000000).setFunction(FunctionEnum.NEGATIVE_QUADRATIC, 50).setTranslationKey("plate_fuel_pu239").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":plate_fuel_pu239");
		plate_fuel_sa326 = NativeItemTextureRegistry.texture(new ItemPlateFuel(2000000).setFunction(FunctionEnum.LINEAR, 80).setTranslationKey("plate_fuel_sa326").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":plate_fuel_sa326");
		plate_fuel_ra226be = NativeItemTextureRegistry.texture(new ItemPlateFuel(1300000).setFunction(FunctionEnum.PASSIVE, 30).setTranslationKey("plate_fuel_ra226be").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":plate_fuel_ra226be");
		plate_fuel_pu238be = NativeItemTextureRegistry.texture(new ItemPlateFuel(1000000).setFunction(FunctionEnum.PASSIVE, 50).setTranslationKey("plate_fuel_pu238be").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":plate_fuel_pu238be");

		pwr_fuel = NativeItemTextureRegistry.texture(new ItemPWRFuel().setTranslationKey("pwr_fuel").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":pwr_fuel");
		pwr_fuel_hot = NativeItemTextureRegistry.texture(new ItemEnumMulti(EnumPWRFuel.class, true, false).setTranslationKey("pwr_fuel_hot").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":pwr_fuel_hot");
		pwr_fuel_depleted = NativeItemTextureRegistry.texture(new ItemEnumMulti(EnumPWRFuel.class, true, false).setTranslationKey("pwr_fuel_depleted").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":pwr_fuel_depleted");
		pwr_printer = NativeItemTextureRegistry.texture(new ItemPWRPrinter().setTranslationKey("pwr_printer").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":pwr_printer");

		rbmk_lid = NativeItemTextureRegistry.texture(new ItemRBMKLid().setTranslationKey("rbmk_lid").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":rbmk_lid");
		rbmk_lid_glass = NativeItemTextureRegistry.texture(new ItemRBMKLid().setTranslationKey("rbmk_lid_glass").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":rbmk_lid_glass");

		rbmk_pellet_ueu = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Unenriched Uranium").setTranslationKey("rbmk_pellet_ueu"), RefStrings.MODID + ":rbmk_pellet_ueu");
		rbmk_pellet_meu = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Medium Enriched Uranium-235").setTranslationKey("rbmk_pellet_meu"), RefStrings.MODID + ":rbmk_pellet_meu");
		rbmk_pellet_heu233 = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Highly Enriched Uranium-233").setTranslationKey("rbmk_pellet_heu233"), RefStrings.MODID + ":rbmk_pellet_heu233");
		rbmk_pellet_heu235 = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Highly Enriched Uranium-235").setTranslationKey("rbmk_pellet_heu235"), RefStrings.MODID + ":rbmk_pellet_heu235");
		rbmk_pellet_uzh = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Uranium Zirconium Hydride").setTranslationKey("rbmk_pellet_uzh"), RefStrings.MODID + ":rbmk_pellet_uzh");
		rbmk_pellet_thmeu = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Thorium with MEU Driver Fuel").setTranslationKey("rbmk_pellet_thmeu"), RefStrings.MODID + ":rbmk_pellet_thmeu");
		rbmk_pellet_lep = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Low Enriched Plutonium-239").setTranslationKey("rbmk_pellet_lep"), RefStrings.MODID + ":rbmk_pellet_lep");
		rbmk_pellet_mep = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Medium Enriched Plutonium-239").setTranslationKey("rbmk_pellet_mep"), RefStrings.MODID + ":rbmk_pellet_mep");
		rbmk_pellet_hep239 = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Highly Enriched Plutonium-239").setTranslationKey("rbmk_pellet_hep239"), RefStrings.MODID + ":rbmk_pellet_hep239");
		rbmk_pellet_hep241 = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Highly Enriched Plutonium-241").setTranslationKey("rbmk_pellet_hep241"), RefStrings.MODID + ":rbmk_pellet_hep241");
		rbmk_pellet_lea = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Low Enriched Americium-242").setTranslationKey("rbmk_pellet_lea"), RefStrings.MODID + ":rbmk_pellet_lea");
		rbmk_pellet_mea = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Medium Enriched Americium-242").setTranslationKey("rbmk_pellet_mea"), RefStrings.MODID + ":rbmk_pellet_mea");
		rbmk_pellet_hea241 = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Highly Enriched Americium-241").setTranslationKey("rbmk_pellet_hea241"), RefStrings.MODID + ":rbmk_pellet_hea241");
		rbmk_pellet_hea242 = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Highly Enriched Americium-242").setTranslationKey("rbmk_pellet_hea242"), RefStrings.MODID + ":rbmk_pellet_hea242");
		rbmk_pellet_bk247 = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Highly Enriched Berkelium-247").setTranslationKey("rbmk_pellet_bk247"), RefStrings.MODID + ":rbmk_pellet_bk247");
		rbmk_pellet_men = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Medium Enriched Neptunium-237").setTranslationKey("rbmk_pellet_men"), RefStrings.MODID + ":rbmk_pellet_men");
		rbmk_pellet_hen = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Highly Enriched Neptunium-237").setTranslationKey("rbmk_pellet_hen"), RefStrings.MODID + ":rbmk_pellet_hen");
		rbmk_pellet_mox = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Mixed MEU & LEP Oxide").setTranslationKey("rbmk_pellet_mox"), RefStrings.MODID + ":rbmk_pellet_mox");
		rbmk_pellet_les = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Low Enriched Schrabidium-326").setTranslationKey("rbmk_pellet_les"), RefStrings.MODID + ":rbmk_pellet_les");
		rbmk_pellet_mes = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Medium Enriched Schrabidium-326").setTranslationKey("rbmk_pellet_mes"), RefStrings.MODID + ":rbmk_pellet_mes");
		rbmk_pellet_hes = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Highly Enriched Schrabidium-326").setTranslationKey("rbmk_pellet_hes"), RefStrings.MODID + ":rbmk_pellet_hes");
		rbmk_pellet_leaus = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Low Enriched Australium (Tasmanite)").setTranslationKey("rbmk_pellet_leaus"), RefStrings.MODID + ":rbmk_pellet_leaus");
		rbmk_pellet_heaus = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Highly Enriched Australium (Ayerite)").setTranslationKey("rbmk_pellet_heaus"), RefStrings.MODID + ":rbmk_pellet_heaus");
		rbmk_pellet_po210be = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Polonium-210 & Beryllium Neutron Source").disableXenon().setTranslationKey("rbmk_pellet_po210be"), RefStrings.MODID + ":rbmk_pellet_po210be");
		rbmk_pellet_ra226be = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Radium-226 & Beryllium Neutron Source").disableXenon().setTranslationKey("rbmk_pellet_ra226be"), RefStrings.MODID + ":rbmk_pellet_ra226be");
		rbmk_pellet_pu238be = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Plutonium-238 & Beryllium Neutron Source").setTranslationKey("rbmk_pellet_pu238be"), RefStrings.MODID + ":rbmk_pellet_pu238be");
		rbmk_pellet_balefire_gold = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Antihydrogen in a Magnetized Gold-198 Lattice").disableXenon().setTranslationKey("rbmk_pellet_balefire_gold"), RefStrings.MODID + ":rbmk_pellet_balefire_gold");
		rbmk_pellet_flashlead = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Antihydrogen confined by a Magnetized Gold-198 and Lead-209 Lattice").disableXenon().setTranslationKey("rbmk_pellet_flashlead"), RefStrings.MODID + ":rbmk_pellet_flashlead");
		rbmk_pellet_balefire = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Draconic Flames").disableXenon().setTranslationKey("rbmk_pellet_balefire"), RefStrings.MODID + ":rbmk_pellet_balefire");
		rbmk_pellet_zfb_bismuth = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Zirconium Fast Breeder - LEU/HEP-241#Bi").setTranslationKey("rbmk_pellet_zfb_bismuth"), RefStrings.MODID + ":rbmk_pellet_zfb_bismuth");
		rbmk_pellet_zfb_pu241 = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Zirconium Fast Breeder - HEU-235/HEP-240#Pu-241").setTranslationKey("rbmk_pellet_zfb_pu241"), RefStrings.MODID + ":rbmk_pellet_zfb_pu241");
		rbmk_pellet_zfb_am_mix = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Zirconium Fast Breeder - HEP-241#MEA").setTranslationKey("rbmk_pellet_zfb_am_mix"), RefStrings.MODID + ":rbmk_pellet_zfb_am_mix");
		rbmk_pellet_drx = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet(TextFormatting.OBFUSCATED + "can't you hear, can't you hear the thunder?").setTranslationKey("rbmk_pellet_drx"), RefStrings.MODID + ":rbmk_pellet_drx");
		rbmk_pellet_lecm = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Low Enriched Curium-245").setTranslationKey("rbmk_pellet_lecm"), RefStrings.MODID + ":rbmk_pellet_lecm");
		rbmk_pellet_mecm = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Medium Enriched Curium-245").setTranslationKey("rbmk_pellet_mecm"), RefStrings.MODID + ":rbmk_pellet_mecm");
		rbmk_pellet_hecm = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Highly Enriched Curium-245").setTranslationKey("rbmk_pellet_hecm"), RefStrings.MODID + ":rbmk_pellet_hecm");
		rbmk_pellet_lecf = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Low Enriched Californium-252").setTranslationKey("rbmk_pellet_lecf"), RefStrings.MODID + ":rbmk_pellet_lecf");
		rbmk_pellet_mecf = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Medium Enriched Californium-252").setTranslationKey("rbmk_pellet_mecf"), RefStrings.MODID + ":rbmk_pellet_mecf");
		rbmk_pellet_hecf = (ItemRBMKPellet) NativeItemTextureRegistry.texture(new ItemRBMKPellet("Highly Enriched Californium-252").setTranslationKey("rbmk_pellet_hecf"), RefStrings.MODID + ":rbmk_pellet_hecf");

		int tintUranium = 0x868D82;
		int tintNeptunium = 0x757E73;
		int tintPlutonium = 0x656E6B;
		int tintAmericium = 0xA88A8F;
		int tintThorium = 0x665448;
		int tintZirconium = 0xAAA36A;
		int tintSchrabidium = 0x2D9A94;
		int tintPolonium = 0x563A26;
		int tintRadium = 0xB3B6AD;
		int tintAustralium = 0xFFEE00;
		int tintFlashgold = 0xDC9613;
		int tintFlashlead = 0x7B7B87;
		int tintBalefire = 0xB2FF1B;
		int tintDRX = 0xD77276;
		
		rbmk_fuel_empty = NativeItemTextureRegistry.texture(new Item().setTranslationKey("rbmk_fuel_empty").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":rbmk_fuel_empty");
		rbmk_fuel_ueu = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_ueu)
				.setYield(100_000_000D)
				.setStats(15)
				.setFunction(EnumBurnFunc.LOG_TEN)
				.setDepletionFunction(EnumDepleteFunc.RAISING_SLOPE)
				.setHeat(0.65) //0.5 is too much of a nerf in heat; pu239 buildup justifies it being on par with MEU ig
				.setMeltingPoint(2865)
				.setTint(tintUranium).setTranslationKey("rbmk_fuel_ueu"), RefStrings.MODID + ":rbmk_fuel_ueu");
		rbmk_fuel_meu = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_meu)
				.setYield(100_000_000D)
				.setStats(20)
				.setFunction(EnumBurnFunc.LOG_TEN)
				.setDepletionFunction(EnumDepleteFunc.RAISING_SLOPE)
				.setHeat(0.65) //0.75 was a bit too much...
				.setMeltingPoint(2865)
				.setTint(tintUranium).setTranslationKey("rbmk_fuel_meu"), RefStrings.MODID + ":rbmk_fuel_meu");
		rbmk_fuel_heu233 = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_heu233)
				.setYield(100_000_000D)
				.setStats(27.5D)
				.setFunction(EnumBurnFunc.LINEAR)
				.setHeat(1.25D)
				.setMeltingPoint(2865)
				.setTint(tintUranium).setTranslationKey("rbmk_fuel_heu233"), RefStrings.MODID + ":rbmk_fuel_heu233");
		rbmk_fuel_heu235 = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_heu235)
				.setYield(100_000_000D)
				.setStats(50) //Consistency with HEN; its critical mass is too high to justify a linear function
				.setFunction(EnumBurnFunc.SQUARE_ROOT)
				.setMeltingPoint(2865)
				.setTint(tintUranium).setTranslationKey("rbmk_fuel_heu235"), RefStrings.MODID + ":rbmk_fuel_heu235");
		rbmk_fuel_uzh = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_uzh)
				.setYield(50_000_000D)
				.setStats(30)
				.setFunction(EnumBurnFunc.LOG_TEN)
				.setDepletionFunction(EnumDepleteFunc.GENTLE_SLOPE)
				.setHeat(0.75)
				.setHeatCoeff(1_000D, 500D)
				.setDiffusion(0.1D)
				.setMeltingPoint(1845)
				.setTint(0x7077AF).setTranslationKey("rbmk_fuel_uzh"), RefStrings.MODID + ":rbmk_fuel_uzh");
		rbmk_fuel_thmeu = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_thmeu)
				.setYield(100000000D)
				.setStats(20)
				.setFunction(EnumBurnFunc.PLATEU)
				.setDepletionFunction(EnumDepleteFunc.BOOSTED_SLOPE)
				.setHeat(0.65D) //Consistency with MEU
				.setMeltingPoint(3350)
				.setTint(tintThorium).setTranslationKey("rbmk_fuel_thmeu"), RefStrings.MODID + ":rbmk_fuel_thmeu");
		rbmk_fuel_lep = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_lep)
				.setYield(100000000D)
				.setStats(35)
				.setFunction(EnumBurnFunc.LOG_TEN)
				.setDepletionFunction(EnumDepleteFunc.RAISING_SLOPE)
				.setHeat(0.75D)
				.setMeltingPoint(2744)
				.setTint(tintPlutonium).setTranslationKey("rbmk_fuel_lep"), RefStrings.MODID + ":rbmk_fuel_lep");
		rbmk_fuel_mep = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_mep)
				.setYield(100000000D)
				.setStats(35)
				.setFunction(EnumBurnFunc.SQUARE_ROOT)
				.setMeltingPoint(2744)
				.setTint(tintPlutonium).setTranslationKey("rbmk_fuel_mep"), RefStrings.MODID + ":rbmk_fuel_mep");
		rbmk_fuel_hep239 = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_hep239)
				.setYield(100000000D)
				.setStats(30)
				.setFunction(EnumBurnFunc.LINEAR)
				.setHeat(1.25D)
				.setMeltingPoint(2744)
				.setTint(tintPlutonium).setTranslationKey("rbmk_fuel_hep"), RefStrings.MODID + ":rbmk_fuel_hep");
		rbmk_fuel_hep241 = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_hep241)
				.setYield(100000000D)
				.setStats(40)
				.setFunction(EnumBurnFunc.LINEAR)
				.setHeat(1.75D)
				.setMeltingPoint(2744)
				.setTint(tintPlutonium).setTranslationKey("rbmk_fuel_hep241"), RefStrings.MODID + ":rbmk_fuel_hep241");
		rbmk_fuel_lea = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_lea)
				.setYield(100000000D)
				.setStats(60, 10)
				.setFunction(EnumBurnFunc.SQUARE_ROOT)
				.setDepletionFunction(EnumDepleteFunc.RAISING_SLOPE)
				.setHeat(1.5D)
				.setMeltingPoint(2386)
				.setTint(tintAmericium).setTranslationKey("rbmk_fuel_lea"), RefStrings.MODID + ":rbmk_fuel_lea");
		rbmk_fuel_mea = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_mea)
				.setYield(100000000D)
				.setStats(35D, 20)
				.setFunction(EnumBurnFunc.ARCH)
				.setHeat(1.75D)
				.setMeltingPoint(2386)
				.setTint(tintAmericium).setTranslationKey("rbmk_fuel_mea"), RefStrings.MODID + ":rbmk_fuel_mea");
		rbmk_fuel_hea241 = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_hea241)
				.setYield(100000000D)
				.setStats(65, 15)
				.setFunction(EnumBurnFunc.SQUARE_ROOT)
				.setHeat(1.85D)
				.setMeltingPoint(2386)
				.setNeutronTypes(NType.FAST, NType.FAST)
				.setTint(tintAmericium).setTranslationKey("rbmk_fuel_hea241"), RefStrings.MODID + ":rbmk_fuel_hea241");
		rbmk_fuel_hea242 = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_hea242)
				.setYield(100000000D)
				.setStats(45)
				.setFunction(EnumBurnFunc.LINEAR)
				.setHeat(2D)
				.setMeltingPoint(2386)
				.setTint(tintAmericium).setTranslationKey("rbmk_fuel_hea242"), RefStrings.MODID + ":rbmk_fuel_hea242");				
		rbmk_fuel_bk247 = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_bk247)
				.setYield(100000000D)
				.setStats(50)
				.setFunction(EnumBurnFunc.LINEAR)
				.setHeat(2D)
				.setMeltingPoint(2993)
				.setTranslationKey("rbmk_fuel_bk247"), RefStrings.MODID + ":rbmk_fuel_bk247");
		rbmk_fuel_men = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_men)
				.setYield(100000000D)
				.setStats(30)
				.setFunction(EnumBurnFunc.SQUARE_ROOT)
				.setDepletionFunction(EnumDepleteFunc.RAISING_SLOPE)
				.setHeat(0.75)
				.setMeltingPoint(2800)
				.setNeutronTypes(NType.ANY, NType.FAST) //Build-up of Pu-239 leads to both speeds of neutrons grooving
				.setTint(tintNeptunium).setTranslationKey("rbmk_fuel_men"), RefStrings.MODID + ":rbmk_fuel_men");
		rbmk_fuel_hen = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_hen)
				.setYield(100000000D)
				.setStats(40)
				.setFunction(EnumBurnFunc.SQUARE_ROOT)
				.setMeltingPoint(2800)
				.setNeutronTypes(NType.FAST, NType.FAST)
				.setTint(tintNeptunium).setTranslationKey("rbmk_fuel_hen"), RefStrings.MODID + ":rbmk_fuel_hen");
		rbmk_fuel_mox = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_mox)
				.setYield(100000000D)
				.setStats(40)
				.setFunction(EnumBurnFunc.LOG_TEN)
				.setDepletionFunction(EnumDepleteFunc.RAISING_SLOPE)
				.setMeltingPoint(2815)
				.setTint(tintUranium).setTranslationKey("rbmk_fuel_mox"), RefStrings.MODID + ":rbmk_fuel_mox");
		rbmk_fuel_les = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_les)
				.setYield(100000000D)
				.setStats(50)
				.setFunction(EnumBurnFunc.SQUARE_ROOT)
				.setHeat(1.25D)
				.setMeltingPoint(2500)
				.setNeutronTypes(NType.SLOW, NType.SLOW) //Beryllium Moderation
				.setTint(tintSchrabidium).setTranslationKey("rbmk_fuel_les"), RefStrings.MODID + ":rbmk_fuel_les");
		rbmk_fuel_mes = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_mes)
				.setYield(100000000D)
				.setStats(75D)
				.setFunction(EnumBurnFunc.ARCH)
				.setHeat(1.5D)
				.setMeltingPoint(2750)
				.setTint(tintSchrabidium).setTranslationKey("rbmk_fuel_mes"), RefStrings.MODID + ":rbmk_fuel_mes");
		rbmk_fuel_hes = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_hes)
				.setYield(100000000D)
				.setStats(90)
				.setFunction(EnumBurnFunc.LINEAR)
				.setDepletionFunction(EnumDepleteFunc.LINEAR)
				.setHeat(1.75D)
				.setMeltingPoint(3000)
				.setTint(tintSchrabidium).setTranslationKey("rbmk_fuel_hes"), RefStrings.MODID + ":rbmk_fuel_hes");
		rbmk_fuel_leaus = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_leaus)
				.setYield(100000000D)
				.setStats(30)
				.setFunction(EnumBurnFunc.SIGMOID)
				.setDepletionFunction(EnumDepleteFunc.LINEAR)
				.setXenon(0.05D, 50D)
				.setHeat(1.5D)
				.setTint(tintAustralium).setMeltingPoint(7029).setTranslationKey("rbmk_fuel_leaus"), RefStrings.MODID + ":rbmk_fuel_leaus");
		rbmk_fuel_heaus = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_heaus)
				.setYield(100000000D)
				.setStats(35)
				.setFunction(EnumBurnFunc.LINEAR)
				.setXenon(0.05D, 50D)
				.setHeat(1.5D)
				.setTint(tintAustralium).setMeltingPoint(5211).setTranslationKey("rbmk_fuel_heaus"), RefStrings.MODID + ":rbmk_fuel_heaus");
		rbmk_fuel_po210be = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_po210be)
				.setYield(25000000D)
				.setStats(0D, 50)
				.setFunction(EnumBurnFunc.PASSIVE)
				.setDepletionFunction(EnumDepleteFunc.LINEAR)
				.setXenon(0.0D, 50D)
				.setHeat(0.1D)
				.setDiffusion(0.05D)
				.setMeltingPoint(1287)
				.setNeutronTypes(NType.SLOW, NType.SLOW) //Beryllium Moderation
				.setTint(tintPolonium).setTranslationKey("rbmk_fuel_po210be"), RefStrings.MODID + ":rbmk_fuel_po210be");
		rbmk_fuel_ra226be = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_ra226be)
				.setYield(100000000D)
				.setStats(0D, 20)
				.setFunction(EnumBurnFunc.PASSIVE)
				.setDepletionFunction(EnumDepleteFunc.LINEAR)
				.setXenon(0.0D, 50D)
				.setHeat(0.035D)
				.setDiffusion(0.5D)
				.setMeltingPoint(700)
				.setNeutronTypes(NType.SLOW, NType.SLOW) //Beryllium Moderation
				.setTint(tintRadium).setTranslationKey("rbmk_fuel_ra226be"), RefStrings.MODID + ":rbmk_fuel_ra226be");
		rbmk_fuel_pu238be = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_pu238be)
				.setYield(50000000D)
				.setStats(40, 40)
				.setFunction(EnumBurnFunc.SQUARE_ROOT)
				.setHeat(0.1D)
				.setDiffusion(0.05D)
				.setMeltingPoint(1287)
				.setNeutronTypes(NType.SLOW, NType.SLOW) //Beryllium Moderation
				.setTint(tintPlutonium).setTranslationKey("rbmk_fuel_pu238be"), RefStrings.MODID + ":rbmk_fuel_pu238be");
		rbmk_fuel_balefire_gold = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_balefire_gold)
				.setYield(100000000D)
				.setStats(50, 10)
				.setFunction(EnumBurnFunc.ARCH)
				.setDepletionFunction(EnumDepleteFunc.LINEAR)
				.setXenon(0.0D, 50D)
				.setMeltingPoint(2000)
				.setTint(tintFlashgold).setTranslationKey("rbmk_fuel_balefire_gold"), RefStrings.MODID + ":rbmk_fuel_balefire_gold");
		rbmk_fuel_flashlead = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_flashlead)
				.setYield(250000000D)
				.setStats(40, 50)
				.setFunction(EnumBurnFunc.ARCH)
				.setDepletionFunction(EnumDepleteFunc.LINEAR)
				.setXenon(0.0D, 50D)
				.setMeltingPoint(2050)
				.setTint(tintFlashlead).setTranslationKey("rbmk_fuel_flashlead"), RefStrings.MODID + ":rbmk_fuel_flashlead");
		rbmk_fuel_balefire = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_balefire)
				.setYield(100000000D)
				.setStats(100, 35)
				.setFunction(EnumBurnFunc.LINEAR)
				.setXenon(0.0D, 50D)
				.setHeat(3D)
				.setMeltingPoint(3652)
				.setTint(tintBalefire).setTranslationKey("rbmk_fuel_balefire"), RefStrings.MODID + ":rbmk_fuel_balefire");
		rbmk_fuel_zfb_bismuth = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_zfb_bismuth)
				.setYield(50000000D)
				.setStats(20)
				.setFunction(EnumBurnFunc.SQUARE_ROOT)
				.setHeat(1.75D)
				.setMeltingPoint(2744)
				.setTint(tintZirconium).setTranslationKey("rbmk_fuel_zfb_bismuth"), RefStrings.MODID + ":rbmk_fuel_zfb_bismuth");
		rbmk_fuel_zfb_pu241 = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_zfb_pu241)
				.setYield(50000000D)
				.setStats(20)
				.setFunction(EnumBurnFunc.SQUARE_ROOT)
				.setMeltingPoint(2865)
				.setTint(tintZirconium).setTranslationKey("rbmk_fuel_zfb_pu241"), RefStrings.MODID + ":rbmk_fuel_zfb_pu241");
		rbmk_fuel_zfb_am_mix = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_zfb_am_mix)
				.setYield(50000000D)
				.setStats(20)
				.setFunction(EnumBurnFunc.LINEAR)
				.setHeat(1.75D)
				.setMeltingPoint(2744)
				.setTint(tintZirconium).setTranslationKey("rbmk_fuel_zfb_am_mix"), RefStrings.MODID + ":rbmk_fuel_zfb_am_mix");
		rbmk_fuel_drx = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_drx)
				.setYield(10000000D)
				.setStats(1000, 10)
				.setFunction(EnumBurnFunc.QUADRATIC)
				.setHeat(0.1D)
				.setMeltingPoint(100000)
				.setTint(tintDRX).setTranslationKey("rbmk_fuel_drx"), RefStrings.MODID + ":rbmk_fuel_drx");
		rbmk_fuel_test = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod("THE VOICES")
				.setYield(1000000D)
				.setStats(100)
				.setFunction(EnumBurnFunc.EXPERIMENTAL)
				.setHeat(1.0D)
				.setMeltingPoint(100000)
				.setTranslationKey("rbmk_fuel_test"), RefStrings.MODID + ":rbmk_fuel_test");
		rbmk_fuel_lecm = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_lecm)
				.setYield(30000000D)
				.setStats(20, 5)
				.setFunction(EnumBurnFunc.SLOW_LINEAR)
				.setDepletionFunction(EnumDepleteFunc.CF_SLOPE)
				.setHeat(1.25D)
				.setMeltingPoint(1340)
				.setDiffusion(0.4D)
				.setTranslationKey("rbmk_fuel_lecm"), RefStrings.MODID + ":rbmk_fuel_lecm");
		rbmk_fuel_mecm = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_mecm)
				.setYield(27000000D)
				.setStats(30, 10)
				.setFunction(EnumBurnFunc.SLOW_LINEAR)
				.setDepletionFunction(EnumDepleteFunc.CF_SLOPE)
				.setHeat(1.25D)
				.setMeltingPoint(1720)
				.setDiffusion(0.3D)
				.setTranslationKey("rbmk_fuel_mecm"), RefStrings.MODID + ":rbmk_fuel_mecm");
		rbmk_fuel_hecm = (ItemRBMKRod) NativeItemTextureRegistry.texture(new ItemRBMKRod(rbmk_pellet_hecm)
				.setYield(24000000D)
				.setStats(60, 25)
				.setFunction(EnumBurnFunc.SLOW_LINEAR)
				.setDepletionFunction(EnumDepleteFunc.CF_SLOPE)
				.setHeat(1.3D)
				.setMeltingPoint(1880)
				.setDiffusion(0.3D)
				.setTranslationKey("rbmk_fuel_hecm"), RefStrings.MODID + ":rbmk_fuel_hecm");

		watz_pellet = NativeItemTextureRegistry.texture(new ItemWatzPellet().setTranslationKey("watz_pellet"), RefStrings.MODID + ":watz_pellet");
		watz_pellet_depleted = NativeItemTextureRegistry.texture(new ItemWatzPellet().setTranslationKey("watz_pellet_depleted"), RefStrings.MODID + ":watz_pellet");

		icf_pellet_empty = NativeItemTextureRegistry.texture(new Item().setTranslationKey("icf_pellet_empty").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":icf_pellet_empty");
		icf_pellet = NativeItemTextureRegistry.texture(new ItemICFPellet().setTranslationKey("icf_pellet").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":icf_pellet");
		icf_pellet_depleted = NativeItemTextureRegistry.texture(new Item().setTranslationKey("icf_pellet_depleted").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1), RefStrings.MODID + ":icf_pellet_depleted");

		trinitite = NativeItemTextureRegistry.texture(new ItemNuclearWaste().setTranslationKey("trinitite").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":trinitite");
		nuclear_waste_long = NativeItemTextureRegistry.texture(new ItemWasteLong().setTranslationKey("nuclear_waste_long").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nuclear_waste_long");
		nuclear_waste_long_tiny = NativeItemTextureRegistry.texture(new ItemWasteLong().setTranslationKey("nuclear_waste_long_tiny").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nuclear_waste_long_tiny");
		nuclear_waste_short = NativeItemTextureRegistry.texture(new ItemWasteShort().setTranslationKey("nuclear_waste_short").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nuclear_waste_short");
		nuclear_waste_short_tiny = NativeItemTextureRegistry.texture(new ItemWasteShort().setTranslationKey("nuclear_waste_short_tiny").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nuclear_waste_short_tiny");
		nuclear_waste_long_depleted = NativeItemTextureRegistry.texture(new ItemWasteLong().setTranslationKey("nuclear_waste_long_depleted").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nuclear_waste_long_depleted");
		nuclear_waste_long_depleted_tiny = NativeItemTextureRegistry.texture(new ItemWasteLong().setTranslationKey("nuclear_waste_long_depleted_tiny").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nuclear_waste_long_depleted_tiny");
		nuclear_waste_short_depleted = NativeItemTextureRegistry.texture(new ItemWasteShort().setTranslationKey("nuclear_waste_short_depleted").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nuclear_waste_short_depleted");
		nuclear_waste_short_depleted_tiny = NativeItemTextureRegistry.texture(new ItemWasteShort().setTranslationKey("nuclear_waste_short_depleted_tiny").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nuclear_waste_short_depleted_tiny");
		nuclear_waste = NativeItemTextureRegistry.texture(new ItemNuclearWaste().setTranslationKey("nuclear_waste").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nuclear_waste");
		nuclear_waste_tiny = NativeItemTextureRegistry.texture(new ItemNuclearWaste().setTranslationKey("nuclear_waste_tiny").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nuclear_waste_tiny");
		nuclear_waste_vitrified = NativeItemTextureRegistry.texture(new ItemNuclearWaste().setTranslationKey("nuclear_waste_vitrified").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nuclear_waste_vitrified");
		nuclear_waste_vitrified_tiny = NativeItemTextureRegistry.texture(new ItemNuclearWaste().setTranslationKey("nuclear_waste_vitrified_tiny").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nuclear_waste_vitrified_tiny");
		scrap_plastic = NativeItemTextureRegistry.texture(new ItemPlasticScrap().setTranslationKey("scrap_plastic"), RefStrings.MODID + ":scrap_plastic");
		scrap = NativeItemTextureRegistry.texture(new Item().setTranslationKey("scrap").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":scrap");
		scrap_oil = NativeItemTextureRegistry.texture(new Item().setTranslationKey("scrap_oil").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":scrap_oil");
		scrap_nuclear = NativeItemTextureRegistry.texture(new Item().setTranslationKey("scrap_nuclear").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":scrap_nuclear");
		containment_box = NativeItemTextureRegistry.texture(new ItemLeadBox().setTranslationKey("containment_box").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":containment_box");
		plastic_bag = NativeItemTextureRegistry.texture(new ItemPlasticBag().setTranslationKey("plastic_bag").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":plastic_bag");

		ammo_bag = NativeItemTextureRegistry.texture(new ItemAmmoBag().setTranslationKey("ammo_bag").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":ammo_bag");
		ammo_bag_infinite = NativeItemTextureRegistry.texture(new ItemAmmoBag().setTranslationKey("ammo_bag_infinite").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":ammo_bag_infinite");
		casing_bag = NativeItemTextureRegistry.texture(new ItemCasingBag().setTranslationKey("casing_bag").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":casing_bag");

		debris_graphite = NativeItemTextureRegistry.texture(new Item().setTranslationKey("debris_graphite").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":debris_graphite");
		debris_metal = NativeItemTextureRegistry.texture(new Item().setTranslationKey("debris_metal").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":debris_metal");
		debris_fuel = NativeItemTextureRegistry.texture(new Item().setTranslationKey("debris_fuel").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":debris_fuel");
		debris_concrete = NativeItemTextureRegistry.texture(new Item().setTranslationKey("debris_concrete").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":debris_concrete");
		debris_exchanger = NativeItemTextureRegistry.texture(new Item().setTranslationKey("debris_exchanger").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":debris_exchanger");
		debris_shrapnel =NativeItemTextureRegistry.texture(new Item().setTranslationKey("debris_shrapnel").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":debris_shrapnel");
		debris_element =NativeItemTextureRegistry.texture(new Item().setTranslationKey("debris_element").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":debris_element");

		pellet_cluster = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("pellet_cluster").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":pellet_cluster");
		powder_fire = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("powder_fire").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_red_phosphorus");
		powder_ice = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("powder_ice").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_ice");
		powder_poison = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("powder_poison").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_poison");
		powder_thermite = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("powder_thermite").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":powder_thermite");
		cordite = NativeItemTextureRegistry.texture(new Item().setTranslationKey("cordite").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":cordite");
		ammonium_nitrate = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ammonium_nitrate").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ammonium_nitrate");
		ballistite = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ballistite").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ballistite");
		ball_dynamite = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ball_dynamite").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ball_dynamite");
		ball_tnt = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ball_tnt").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ball_tnt");
		ball_tatb = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ball_tatb").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ball_tatb");
		ball_fireclay = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ball_fireclay").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ball_fireclay");
		ball_ferric_clay = NativeItemTextureRegistry.texture(new Item().setTranslationKey("ball_ferric_clay").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ferric_clay");
		pellet_gas = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("pellet_gas").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":pellet_gas");
		magnetron = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("magnetron").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":magnetron_alt");
		pellet_buckshot = NativeItemTextureRegistry.texture(new Item().setTranslationKey("pellet_buckshot").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":pellets_lead");
		pellet_charged = NativeItemTextureRegistry.texture(new Item().setTranslationKey("pellet_charged").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":pellets_charged");

		rangefinder = NativeItemTextureRegistry.texture(new ItemRangefinder().setTranslationKey("rangefinder").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":rangefinder");
		designator = NativeItemTextureRegistry.texture(new ItemDesingator().setTranslationKey("designator").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":designator");
		designator_range = NativeItemTextureRegistry.texture(new ItemDesingatorRange().setTranslationKey("designator_range").setFull3D().setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":designator_range_alt");
		designator_manual = NativeItemTextureRegistry.texture(new ItemDesingatorManual().setTranslationKey("designator_manual").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":designator_manual");
		designator_arty_range = NativeItemTextureRegistry.texture(new ItemDesignatorArtyRange().setTranslationKey("designator_arty_range").setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":designator_arty_range");
		launch_code_piece = NativeItemTextureRegistry.texture(new Item().setTranslationKey("launch_code_piece").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":launch_code_piece");
		launch_code = NativeItemTextureRegistry.texture(new Item().setTranslationKey("launch_code").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":launch_code");
		launch_key = NativeItemTextureRegistry.texture(new Item().setTranslationKey("launch_key").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":launch_key");
		missile_assembly = NativeItemTextureRegistry.texture(new Item().setTranslationKey("missile_assembly").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":missile_assembly");
		missile_generic = NativeItemTextureRegistry.texture(new ItemMissile(MissileFormFactor.V2, MissileTier.TIER1).setTranslationKey("missile_generic").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":missile_generic");
		missile_anti_ballistic = NativeItemTextureRegistry.texture(new ItemMissile(MissileFormFactor.ABM, MissileTier.TIER1).setTranslationKey("missile_anti_ballistic").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":missile_anti_ballistic");
		missile_incendiary = NativeItemTextureRegistry.texture(new ItemMissile(MissileFormFactor.V2, MissileTier.TIER1).setTranslationKey("missile_incendiary").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":missile_incendiary");
		missile_cluster = NativeItemTextureRegistry.texture(new ItemMissile(MissileFormFactor.V2, MissileTier.TIER1).setTranslationKey("missile_cluster").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":missile_cluster");
		missile_buster = NativeItemTextureRegistry.texture(new ItemMissile(MissileFormFactor.V2, MissileTier.TIER1).setTranslationKey("missile_buster").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":missile_buster");
		missile_decoy = NativeItemTextureRegistry.texture(new ItemMissile(MissileFormFactor.V2, MissileTier.TIER1).setTranslationKey("missile_decoy").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":missile_decoy");
		missile_strong = NativeItemTextureRegistry.texture(new ItemMissile(MissileFormFactor.STRONG, MissileTier.TIER2).setTranslationKey("missile_strong").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":missile_strong");
		missile_incendiary_strong = NativeItemTextureRegistry.texture(new ItemMissile(MissileFormFactor.STRONG, MissileTier.TIER2).setTranslationKey("missile_incendiary_strong").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":missile_incendiary_strong");
		missile_cluster_strong = NativeItemTextureRegistry.texture(new ItemMissile(MissileFormFactor.STRONG, MissileTier.TIER2).setTranslationKey("missile_cluster_strong").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":missile_cluster_strong");
		missile_buster_strong = NativeItemTextureRegistry.texture(new ItemMissile(MissileFormFactor.STRONG, MissileTier.TIER2).setTranslationKey("missile_buster_strong").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":missile_buster_strong");
		missile_emp_strong = NativeItemTextureRegistry.texture(new ItemMissile(MissileFormFactor.STRONG, MissileTier.TIER2).setTranslationKey("missile_emp_strong").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":missile_emp_strong");
		missile_burst = NativeItemTextureRegistry.texture(new ItemMissile(MissileFormFactor.HUGE, MissileTier.TIER3).setTranslationKey("missile_burst").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":missile_burst");
		missile_inferno = NativeItemTextureRegistry.texture(new ItemMissile(MissileFormFactor.HUGE, MissileTier.TIER3).setTranslationKey("missile_inferno").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":missile_inferno");
		missile_rain = NativeItemTextureRegistry.texture(new ItemMissile(MissileFormFactor.HUGE, MissileTier.TIER3).setTranslationKey("missile_rain").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":missile_rain");
		missile_drill = NativeItemTextureRegistry.texture(new ItemMissile(MissileFormFactor.HUGE, MissileTier.TIER3).setTranslationKey("missile_drill").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":missile_drill");
		missile_nuclear = NativeItemTextureRegistry.texture(new ItemMissile(MissileFormFactor.ATLAS, MissileTier.TIER4).setTranslationKey("missile_nuclear").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":missile_nuclear");
		missile_nuclear_cluster = NativeItemTextureRegistry.texture(new ItemMissile(MissileFormFactor.ATLAS, MissileTier.TIER4).setTranslationKey("missile_nuclear_cluster").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":missile_nuclear_cluster");
		missile_volcano = NativeItemTextureRegistry.texture(new ItemMissile(MissileFormFactor.ATLAS, MissileTier.TIER4).setTranslationKey("missile_volcano").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":missile_volcano");
		missile_doomsday = NativeItemTextureRegistry.texture(new ItemMissile(MissileFormFactor.ATLAS, MissileTier.TIER4).setTranslationKey("missile_doomsday").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":missile_doomsday");
		missile_doomsday_rusted = NativeItemTextureRegistry.texture(new ItemMissile(MissileFormFactor.ATLAS, MissileTier.TIER4).notLaunchable().setTranslationKey("missile_doomsday_rusted").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":missile_doomsday");
		missile_taint = NativeItemTextureRegistry.texture(new ItemMissile(MissileFormFactor.MICRO, MissileTier.TIER0).setTranslationKey("missile_taint").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":missile_taint");
		missile_micro = NativeItemTextureRegistry.texture(new ItemMissile(MissileFormFactor.MICRO, MissileTier.TIER0).setTranslationKey("missile_micro").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":missile_micro");
		missile_bhole = NativeItemTextureRegistry.texture(new ItemMissile(MissileFormFactor.MICRO, MissileTier.TIER0).setTranslationKey("missile_bhole").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":missile_bhole");
		missile_schrabidium = NativeItemTextureRegistry.texture(new ItemMissile(MissileFormFactor.MICRO, MissileTier.TIER0).setTranslationKey("missile_schrabidium").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":missile_schrabidium");
		missile_emp = NativeItemTextureRegistry.texture(new ItemMissile(MissileFormFactor.MICRO, MissileTier.TIER0).setTranslationKey("missile_emp").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":missile_emp");
		missile_shuttle = NativeItemTextureRegistry.texture(new ItemMissile(MissileFormFactor.OTHER, MissileTier.TIER3, MissileFuel.KEROSENE_PEROXIDE).setTranslationKey("missile_shuttle").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":missile_shuttle");
		missile_stealth = NativeItemTextureRegistry.texture(new ItemMissile(MissileFormFactor.STRONG, MissileTier.TIER1).setTranslationKey("missile_stealth").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":missile_stealth");
		missile_test = NativeItemTextureRegistry.texture(new ItemMissile(MissileFormFactor.MICRO, MissileTier.TIER0).setTranslationKey("missile_test").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":missile_micro");
		missile_soyuz = NativeItemTextureRegistry.texture(new ItemSoyuz().setTranslationKey("missile_soyuz").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":soyuz");
		missile_soyuz_lander = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("missile_soyuz_lander").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":soyuz_lander");

		missile_custom = NativeItemTextureRegistry.texture(new ItemCustomMissile().setTranslationKey("missile_custom").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":missile_custom");
		rocket_custom = NativeItemTextureRegistry.texture(new ItemCustomRocket().setTranslationKey("rocket_custom").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":missile_custom");

		sat_mapper = NativeItemTextureRegistry.texture(new ItemSatellite().setTranslationKey("sat_mapper").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":sat_mapper");
		sat_scanner = NativeItemTextureRegistry.texture(new ItemSatellite().setTranslationKey("sat_scanner").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":sat_scanner");
		sat_radar = NativeItemTextureRegistry.texture(new ItemSatellite().setTranslationKey("sat_radar").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":sat_radar");
		sat_laser = NativeItemTextureRegistry.texture(new ItemSatellite(48_000).setTranslationKey("sat_laser").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":sat_laser");
		sat_foeq = NativeItemTextureRegistry.texture(new ItemSatellite(20_000).setTranslationKey("sat_foeq").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":sat_foeq");
		sat_resonator = NativeItemTextureRegistry.texture(new ItemSatellite(20_000).setTranslationKey("sat_resonator").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":sat_resonator");
		sat_miner = NativeItemTextureRegistry.texture(new ItemSatellite(64_000).setTranslationKey("sat_miner").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":sat_miner");
		sat_lunar_miner = NativeItemTextureRegistry.texture(new ItemSatellite(64_000).setTranslationKey("sat_lunar_miner").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":sat_lunar_miner");
		sat_gerald = NativeItemTextureRegistry.texture(new ItemSatellite(128_000).setTranslationKey("sat_gerald").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":sat_gerald");
		sat_dyson_relay = NativeItemTextureRegistry.texture(new ItemSatellite(32_000).setTranslationKey("sat_dyson_relay").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":sat_dyson_relay");
		sat_chip = NativeItemTextureRegistry.texture(new ItemSatChip().setTranslationKey("sat_chip").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":sat_chip");
		sat_war = NativeItemTextureRegistry.texture(new ItemSatellite(128_000).setTranslationKey("sat_war").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":sat_gerald");

		sat_interface = NativeItemTextureRegistry.texture(new ItemSatInterface().setTranslationKey("sat_interface").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":sat_interface");
		sat_coord = NativeItemTextureRegistry.texture(new ItemSatInterface().setTranslationKey("sat_coord").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":sat_coord");
		sat_designator = NativeItemTextureRegistry.texture(new ItemSatDesignator().setTranslationKey("sat_designator").setFull3D().setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":sat_designator");
		sat_relay = NativeItemTextureRegistry.texture(new ItemSatInterface().setTranslationKey("sat_relay").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":sat_relay");
																														//in kg
		mp_thruster_10_kerosene = new ItemCustomMissilePart().makeThruster(FuelType.KEROSENE, 1F, 1_500, PartSize.SIZE_10, 16_000, 180, 308).setHealth(10F)					.setTranslationKey("mp_thruster_10_kerosene");
		mp_thruster_10_solid = new ItemCustomMissilePart().makeThruster(FuelType.SOLID, 1F, 1_500, PartSize.SIZE_10, 60_000, 400, 195).setHealth(15F)						.setTranslationKey("mp_thruster_10_solid");
		mp_thruster_10_xenon = new ItemCustomMissilePart().makeThruster(FuelType.XENON, 1F, 1_500, PartSize.SIZE_10, 2_000, 200, 4200).setHealth(5F)							.setTranslationKey("mp_thruster_10_xenon");
		mp_thruster_15_kerosene = new ItemCustomMissilePart().makeThruster(FuelType.KEROSENE, 1F, 7_500, PartSize.SIZE_15, 120_000, 900, 308).setHealth(15F)					.setTranslationKey("mp_thruster_15_kerosene");
		mp_thruster_15_kerosene_dual = new ItemCustomMissilePart().makeThruster(FuelType.KEROSENE, 1F, 2_500, PartSize.SIZE_15, 200_000, 1600, 308).setHealth(15F)				.setTranslationKey("mp_thruster_15_kerosene_dual");
		mp_thruster_15_kerosene_triple = new ItemCustomMissilePart().makeThruster(FuelType.KEROSENE, 1F, 5_000, PartSize.SIZE_15, 280_000, 2200, 308).setHealth(15F)				.setTranslationKey("mp_thruster_15_kerosene_triple");
		mp_thruster_15_solid = new ItemCustomMissilePart().makeThruster(FuelType.SOLID, 1F, 5_000, PartSize.SIZE_15, 220_000, 1000, 195).setHealth(20F)							.setTranslationKey("mp_thruster_15_solid");
		mp_thruster_15_solid_hexdecuple = new ItemCustomMissilePart().makeThruster(FuelType.SOLID, 1F, 5_000, PartSize.SIZE_15, 260_000, 1200, 195).setHealth(25F).setRarity(Rarity.UNCOMMON).setTranslationKey("mp_thruster_15_solid_hexdecuple");
		mp_thruster_15_hydrogen = new ItemCustomMissilePart().makeThruster(FuelType.HYDROGEN, 1F, 7_500, PartSize.SIZE_15, 100_000, 800, 380).setHealth(20F)					.setTranslationKey("mp_thruster_15_hydrogen");
		mp_thruster_15_hydrogen_dual = new ItemCustomMissilePart().makeThruster(FuelType.HYDROGEN, 1F, 2_500, PartSize.SIZE_15, 200_000, 1400, 380).setHealth(15F)				.setTranslationKey("mp_thruster_15_hydrogen_dual");
		mp_thruster_15_balefire_short = new ItemCustomMissilePart().makeThruster(FuelType.BALEFIRE, 1F, 5_000, PartSize.SIZE_15, 800_000, 1600, 666).setHealth(25F)				.setTranslationKey("mp_thruster_15_balefire_short");
		mp_thruster_15_balefire = new ItemCustomMissilePart().makeThruster(FuelType.BALEFIRE, 1F, 5_000, PartSize.SIZE_15, 1_000_000, 2000, 666).setHealth(25F)					.setTranslationKey("mp_thruster_15_balefire");
		mp_thruster_15_balefire_large = new ItemCustomMissilePart().makeThruster(FuelType.BALEFIRE, 1F, 7_500, PartSize.SIZE_15, 1_200_000, 2400, 666).setHealth(35F)			.setTranslationKey("mp_thruster_15_balefire_large");
		mp_thruster_15_balefire_large_rad = new ItemCustomMissilePart().makeThruster(FuelType.BALEFIRE, 1F, 7_500, PartSize.SIZE_15, 1_200_000, 2400, 666).setAuthor("The Master").setHealth(35F).setRarity(Rarity.UNCOMMON).setTranslationKey("mp_thruster_15_balefire_large_rad");
		mp_thruster_20_kerosene = new ItemCustomMissilePart().makeThruster(FuelType.KEROSENE, 1F, 100_000, PartSize.SIZE_20, 1_536_000, 2400, 308).setHealth(30F)					.setTranslationKey("mp_thruster_20_kerosene");
		mp_thruster_20_kerosene_dual = new ItemCustomMissilePart().makeThruster(FuelType.KEROSENE, 1F, 100_000, PartSize.SIZE_20, 1_934_000, 3200, 308).setHealth(30F)				.setTranslationKey("mp_thruster_20_kerosene_dual");
		mp_thruster_20_kerosene_triple = new ItemCustomMissilePart().makeThruster(FuelType.KEROSENE, 1F, 100_000, PartSize.SIZE_20, 2_542_000, 4400, 308).setHealth(30F)			.setTranslationKey("mp_thruster_20_kerosene_triple");
		mp_thruster_20_methalox = new ItemCustomMissilePart().makeThruster(FuelType.METHALOX, 1F, 100_000, PartSize.SIZE_20, 890_000, 2400, 320).setHealth(30F)					.setTranslationKey("mp_thruster_20_methalox");
		mp_thruster_20_methalox_dual = new ItemCustomMissilePart().makeThruster(FuelType.METHALOX, 1F, 100_000, PartSize.SIZE_20, 1_184_000, 3200, 320).setHealth(30F)				.setTranslationKey("mp_thruster_20_methalox_dual");
		mp_thruster_20_methalox_triple = new ItemCustomMissilePart().makeThruster(FuelType.METHALOX, 1F, 100_000, PartSize.SIZE_20, 1_456_000, 4400, 320).setHealth(30F)			.setTranslationKey("mp_thruster_20_methalox_triple");
		mp_thruster_20_hydrogen = new ItemCustomMissilePart().makeThruster(FuelType.HYDROGEN, 1F, 100_000, PartSize.SIZE_20, 480_000, 2600, 380).setHealth(30F)					.setTranslationKey("mp_thruster_20_hydrogen");
		mp_thruster_20_hydrogen_dual = new ItemCustomMissilePart().makeThruster(FuelType.HYDROGEN, 1F, 100_000, PartSize.SIZE_20, 640_000, 3400, 380).setHealth(30F)				.setTranslationKey("mp_thruster_20_hydrogen_dual");
		mp_thruster_20_hydrogen_triple = new ItemCustomMissilePart().makeThruster(FuelType.HYDROGEN, 1F, 100_000, PartSize.SIZE_20, 938_000, 4500, 380).setHealth(30F)			.setTranslationKey("mp_thruster_20_hydrogen_triple");
		mp_thruster_20_solid = new ItemCustomMissilePart().makeThruster(FuelType.SOLID, 1F, 100_000, PartSize.SIZE_20, 1_400_000, 2400, 195).setHealth(35F).setWittyText("It's basically just a big hole at the end of the fuel tank.").setTranslationKey("mp_thruster_20_solid");
		mp_thruster_20_solid_multi = new ItemCustomMissilePart().makeThruster(FuelType.SOLID, 1F, 100_000, PartSize.SIZE_20, 1_830_000, 3200, 195).setHealth(35F)					.setTranslationKey("mp_thruster_20_solid_multi");
		mp_thruster_20_solid_multier = new ItemCustomMissilePart().makeThruster(FuelType.SOLID, 1F, 100_000, PartSize.SIZE_20, 2_320_000, 4000, 195).setHealth(35F).setWittyText("Did I miscount? Hope not.").setTranslationKey("mp_thruster_20_solid_multier");
		mp_thruster_20_hydrazine = new ItemCustomMissilePart().makeThruster(FuelType.HYDRAZINE, 1F, 100_000, PartSize.SIZE_20, 600_000, 1200, 220).setHealth(35F).setTranslationKey("mp_thruster_20_hydrazine");
		mp_thruster_10_hydrazine = new ItemCustomMissilePart().makeThruster(FuelType.HYDRAZINE, 1F, 1_500, PartSize.SIZE_10, 14_000, 120, 220).setHealth(15F)						.setTranslationKey("mp_thruster_10_hydrazine");

		mp_stability_10_flat = new ItemCustomMissilePart().makeStability(0.5F, PartSize.SIZE_10).setHealth(10F).setMaxStackSize(1).setTranslationKey("mp_stability_10_flat");
		mp_stability_10_cruise = new ItemCustomMissilePart().makeStability(0.25F, PartSize.SIZE_10).setHealth(5F).setMaxStackSize(1).setTranslationKey("mp_stability_10_cruise");
		mp_stability_10_space = new ItemCustomMissilePart().makeStability(0.35F, PartSize.SIZE_10).setHealth(5F).setRarity(Rarity.COMMON).setWittyText("Standing there alone, the ship is waiting / All systems are go, are you sure?").setMaxStackSize(1).setTranslationKey("mp_stability_10_space");
		mp_stability_15_flat = new ItemCustomMissilePart().makeStability(0.5F, PartSize.SIZE_15).setHealth(10F).setMaxStackSize(1).setTranslationKey("mp_stability_15_flat");
		mp_stability_15_thin = new ItemCustomMissilePart().makeStability(0.35F, PartSize.SIZE_15).setHealth(5F).setMaxStackSize(1).setTranslationKey("mp_stability_15_thin");
		mp_stability_15_soyuz = new ItemCustomMissilePart().makeStability(0.25F, PartSize.SIZE_15).setHealth(15F).setRarity(Rarity.COMMON).setWittyText("Союз!").setMaxStackSize(1).setTranslationKey("mp_stability_15_soyuz");
		mp_stability_20_flat = new ItemCustomMissilePart().makeStability(0.5F, PartSize.SIZE_20).setMaxStackSize(1).setTranslationKey("mp_s_20");

		mp_fuselage_10_kerosene = new ItemCustomMissilePart().makeFuselage(FuelType.KEROSENE, 2500, 1000, PartSize.SIZE_10, PartSize.SIZE_10).setAuthor("Hoboy").setHealth(20F).setTranslationKey("mp_fuselage_10_kerosene");
		mp_fuselage_10_kerosene_camo =			((ItemCustomMissilePart) mp_fuselage_10_kerosene).copy().setRarity(Rarity.COMMON).setTitle("Camo").setTranslationKey("mp_fuselage_10_kerosene_camo");
		mp_fuselage_10_kerosene_desert = 		((ItemCustomMissilePart) mp_fuselage_10_kerosene).copy().setRarity(Rarity.COMMON).setTitle("Desert Camo").setTranslationKey("mp_fuselage_10_kerosene_desert");
		mp_fuselage_10_kerosene_sky = 			((ItemCustomMissilePart) mp_fuselage_10_kerosene).copy().setRarity(Rarity.COMMON).setTitle("Sky Camo").setTranslationKey("mp_fuselage_10_kerosene_sky");
		mp_fuselage_10_kerosene_flames = 		((ItemCustomMissilePart) mp_fuselage_10_kerosene).copy().setRarity(Rarity.UNCOMMON).setTitle("Sick Flames").setTranslationKey("mp_fuselage_10_kerosene_flames");
		mp_fuselage_10_kerosene_insulation = 	((ItemCustomMissilePart) mp_fuselage_10_kerosene).copy().setRarity(Rarity.COMMON).setTitle("Orange Insulation").setHealth(25F).setTranslationKey("mp_fuselage_10_kerosene_insulation");
		mp_fuselage_10_kerosene_sleek = 		((ItemCustomMissilePart) mp_fuselage_10_kerosene).copy().setRarity(Rarity.RARE).setTitle("IF-R&D").setHealth(35F).setTranslationKey("mp_fuselage_10_kerosene_sleek");
		mp_fuselage_10_kerosene_metal = 		((ItemCustomMissilePart) mp_fuselage_10_kerosene).copy().setRarity(Rarity.UNCOMMON).setTitle("Bolted Metal").setHealth(30F).setAuthor("Hoboy").setTranslationKey("mp_fuselage_10_kerosene_metal");
		mp_fuselage_10_kerosene_taint =			((ItemCustomMissilePart) mp_fuselage_10_kerosene).copy().setRarity(Rarity.UNCOMMON).setAuthor("Sam").setTitle("Tainted").setTranslationKey("mp_fuselage_10_kerosene_taint");

		mp_fuselage_10_solid = new ItemCustomMissilePart().makeFuselage(FuelType.SOLID, 2500, 1000, PartSize.SIZE_10, PartSize.SIZE_10).setHealth(25F)			.setTranslationKey("mp_fuselage_10_solid");
		mp_fuselage_10_solid_flames = 		((ItemCustomMissilePart) mp_fuselage_10_solid).copy().setRarity(Rarity.UNCOMMON).setTitle("Sick Flames").setTranslationKey("mp_fuselage_10_solid_flames");
		mp_fuselage_10_solid_insulation = 	((ItemCustomMissilePart) mp_fuselage_10_solid).copy().setRarity(Rarity.COMMON).setTitle("Orange Insulation").setHealth(30F).setTranslationKey("mp_fuselage_10_solid_insulation");
		mp_fuselage_10_solid_sleek = 		((ItemCustomMissilePart) mp_fuselage_10_solid).copy().setRarity(Rarity.RARE).setTitle("IF-R&D").setHealth(35F).setTranslationKey("mp_fuselage_10_solid_sleek");
		mp_fuselage_10_solid_soviet_glory = ((ItemCustomMissilePart) mp_fuselage_10_solid).copy().setRarity(Rarity.EPIC).setAuthor("Hoboy").setHealth(35F).setTitle("Soviet Glory").setTranslationKey("mp_fuselage_10_solid_soviet_glory");
		mp_fuselage_10_solid_cathedral = 	((ItemCustomMissilePart) mp_fuselage_10_solid).copy().setRarity(Rarity.RARE).setAuthor("Satan").setTitle("Unholy Cathedral").setWittyText("Quakeesque!").setTranslationKey("mp_fuselage_10_solid_cathedral");
		mp_fuselage_10_solid_moonlit = 		((ItemCustomMissilePart) mp_fuselage_10_solid).copy().setRarity(Rarity.UNCOMMON).setAuthor("The Master & Hoboy").setTitle("Moonlit").setTranslationKey("mp_fuselage_10_solid_moonlit");
		mp_fuselage_10_solid_battery = 		((ItemCustomMissilePart) mp_fuselage_10_solid).copy().setRarity(Rarity.UNCOMMON).setAuthor("wolfmonster222").setHealth(30F).setTitle("Ecstatic").setWittyText("I got caught eating batteries again :(").setTranslationKey("mp_fuselage_10_solid_battery");
		mp_fuselage_10_solid_duracell = 	((ItemCustomMissilePart) mp_fuselage_10_solid).copy().setRarity(Rarity.RARE).setAuthor("Hoboy").setTitle("Duracell").setHealth(30F).setWittyText("The crunchiest battery on the market!").setTranslationKey("mp_fuselage_10_solid_duracell");

		mp_fuselage_10_xenon = new ItemCustomMissilePart().makeFuselage(FuelType.XENON, 5000, 1000, PartSize.SIZE_10, PartSize.SIZE_10).setHealth(20F)			.setTranslationKey("mp_fuselage_10_xenon");
		mp_fuselage_10_xenon_bhole = 	((ItemCustomMissilePart) mp_fuselage_10_xenon).copy().setRarity(Rarity.RARE).setAuthor("Sten89").setTitle("Morceus-1457").setTranslationKey("mp_fuselage_10_xenon_bhole");
		mp_fuselage_10_hydrazine = new ItemCustomMissilePart().makeFuselage(FuelType.HYDRAZINE, 2000, 1000, PartSize.SIZE_10, PartSize.SIZE_10).setHealth(25F)			.setTranslationKey("mp_fuselage_10_hydrazine");
		rp_fuselage_20_12_hydrazine = new ItemCustomMissilePart().makeFuselage(FuelType.HYDRAZINE, 12500, 1000, PartSize.SIZE_20, PartSize.SIZE_20).setHealth(25F)			.setTranslationKey("mp_fuselage_20_hydrazine");

		mp_fuselage_10_long_kerosene = new ItemCustomMissilePart().makeFuselage(FuelType.KEROSENE, 5000, 1000, PartSize.SIZE_10, PartSize.SIZE_10).setAuthor("Hoboy").setHealth(30F).setTranslationKey("mp_fuselage_10_long_kerosene");
		mp_fuselage_10_long_kerosene_camo = 		((ItemCustomMissilePart) mp_fuselage_10_long_kerosene).copy().setRarity(Rarity.COMMON).setTitle("Camo").setTranslationKey("mp_fuselage_10_long_kerosene_camo");
		mp_fuselage_10_long_kerosene_desert = 		((ItemCustomMissilePart) mp_fuselage_10_long_kerosene).copy().setRarity(Rarity.COMMON).setTitle("Desert Camo").setTranslationKey("mp_fuselage_10_long_kerosene_desert");
		mp_fuselage_10_long_kerosene_sky = 			((ItemCustomMissilePart) mp_fuselage_10_long_kerosene).copy().setRarity(Rarity.COMMON).setTitle("Sky Camo").setTranslationKey("mp_fuselage_10_long_kerosene_sky");
		mp_fuselage_10_long_kerosene_flames = 		((ItemCustomMissilePart) mp_fuselage_10_long_kerosene).copy().setRarity(Rarity.UNCOMMON).setTitle("Sick Flames").setTranslationKey("mp_fuselage_10_long_kerosene_flames");
		mp_fuselage_10_long_kerosene_insulation = 	((ItemCustomMissilePart) mp_fuselage_10_long_kerosene).copy().setRarity(Rarity.COMMON).setTitle("Orange Insulation").setHealth(35F).setTranslationKey("mp_fuselage_10_long_kerosene_insulation");
		mp_fuselage_10_long_kerosene_sleek =		((ItemCustomMissilePart) mp_fuselage_10_long_kerosene).copy().setRarity(Rarity.RARE).setTitle("IF-R&D").setHealth(40F).setTranslationKey("mp_fuselage_10_long_kerosene_sleek");
		mp_fuselage_10_long_kerosene_metal = 		((ItemCustomMissilePart) mp_fuselage_10_long_kerosene).copy().setRarity(Rarity.UNCOMMON).setAuthor("Hoboy").setHealth(35F).setTranslationKey("mp_fuselage_10_long_kerosene_metal");
		mp_fuselage_10_long_kerosene_dash = 		((ItemCustomMissilePart) mp_fuselage_10_long_kerosene).copy().setRarity(Rarity.EPIC).setAuthor("Sam").setTitle("Dash").setWittyText("I wash my hands of it.").setCreativeTab(null).setTranslationKey("mp_fuselage_10_long_kerosene_dash");
		mp_fuselage_10_long_kerosene_taint = 		((ItemCustomMissilePart) mp_fuselage_10_long_kerosene).copy().setRarity(Rarity.UNCOMMON).setAuthor("Sam").setTitle("Tainted").setTranslationKey("mp_fuselage_10_long_kerosene_taint");
		mp_fuselage_10_long_kerosene_vap = 			((ItemCustomMissilePart) mp_fuselage_10_long_kerosene).copy().setRarity(Rarity.EPIC).setAuthor("VT-6/24").setTitle("Minty Contrail").setWittyText("Upper rivet!").setTranslationKey("mp_fuselage_10_long_kerosene_vap");

		mp_fuselage_10_long_solid = new ItemCustomMissilePart().makeFuselage(FuelType.SOLID, 5000, 1000, PartSize.SIZE_10, PartSize.SIZE_10).setHealth(35F)	.setTranslationKey("mp_fuselage_10_long_solid");
		mp_fuselage_10_long_solid_flames = 			((ItemCustomMissilePart) mp_fuselage_10_long_solid).copy().setRarity(Rarity.UNCOMMON).setTitle("Sick Flames").setTranslationKey("mp_fuselage_10_long_solid_flames");
		mp_fuselage_10_long_solid_insulation = 		((ItemCustomMissilePart) mp_fuselage_10_long_solid).copy().setRarity(Rarity.COMMON).setTitle("Orange Insulation").setHealth(40F).setTranslationKey("mp_fuselage_10_long_solid_insulation");
		mp_fuselage_10_long_solid_sleek = 			((ItemCustomMissilePart) mp_fuselage_10_long_solid).copy().setRarity(Rarity.RARE).setTitle("IF-R&D").setHealth(45F).setTranslationKey("mp_fuselage_10_long_solid_sleek");
		mp_fuselage_10_long_solid_soviet_glory = 	((ItemCustomMissilePart) mp_fuselage_10_long_solid).copy().setRarity(Rarity.EPIC).setAuthor("Hoboy").setHealth(45F).setTitle("Soviet Glory").setWittyText("Fully Automated Luxury Gay Space Communism!").setTranslationKey("mp_fuselage_10_long_solid_soviet_glory");
		mp_fuselage_10_long_solid_bullet = 			((ItemCustomMissilePart) mp_fuselage_10_long_solid).copy().setRarity(Rarity.COMMON).setAuthor("Sam").setTitle("Bullet Bill").setTranslationKey("mp_fuselage_10_long_solid_bullet");
		mp_fuselage_10_long_solid_silvermoonlight = ((ItemCustomMissilePart) mp_fuselage_10_long_solid).copy().setRarity(Rarity.UNCOMMON).setAuthor("The Master").setTitle("Silver Moonlight").setTranslationKey("mp_fuselage_10_long_solid_silvermoonlight");

		mp_fuselage_10_15_kerosene = new ItemCustomMissilePart().makeFuselage(FuelType.KEROSENE, 10000, 1000, PartSize.SIZE_10, PartSize.SIZE_15).setHealth(40F).setTranslationKey("mp_fuselage_10_15_kerosene");
		mp_fuselage_10_15_solid = new ItemCustomMissilePart().makeFuselage(FuelType.SOLID, 10000, 1000, PartSize.SIZE_10, PartSize.SIZE_15).setHealth(40F)		.setTranslationKey("mp_fuselage_10_15_solid");
		mp_fuselage_10_15_hydrogen = new ItemCustomMissilePart().makeFuselage(FuelType.HYDROGEN, 10000, 1000, PartSize.SIZE_10, PartSize.SIZE_15).setHealth(40F).setTranslationKey("mp_fuselage_10_15_hydrogen");
		mp_fuselage_10_15_balefire = new ItemCustomMissilePart().makeFuselage(FuelType.BALEFIRE, 10000, 1000, PartSize.SIZE_10, PartSize.SIZE_15).setHealth(40F).setTranslationKey("mp_fuselage_10_15_balefire");

		mp_fuselage_15_kerosene = new ItemCustomMissilePart().makeFuselage(FuelType.KEROSENE, 15000, 1000, PartSize.SIZE_15, PartSize.SIZE_15).setAuthor("Hoboy").setHealth(50F).setTranslationKey("mp_fuselage_15_kerosene");
		mp_fuselage_15_kerosene_camo = 			((ItemCustomMissilePart) mp_fuselage_15_kerosene).copy().setRarity(Rarity.COMMON).setTitle("Camo").setTranslationKey("mp_fuselage_15_kerosene_camo");
		mp_fuselage_15_kerosene_desert = 		((ItemCustomMissilePart) mp_fuselage_15_kerosene).copy().setRarity(Rarity.COMMON).setTitle("Desert Camo").setTranslationKey("mp_fuselage_15_kerosene_desert");
		mp_fuselage_15_kerosene_sky = 			((ItemCustomMissilePart) mp_fuselage_15_kerosene).copy().setRarity(Rarity.COMMON).setTitle("Sky Camo").setTranslationKey("mp_fuselage_15_kerosene_sky");
		mp_fuselage_15_kerosene_insulation = 	((ItemCustomMissilePart) mp_fuselage_15_kerosene).copy().setRarity(Rarity.COMMON).setTitle("Orange Insulation").setHealth(55F).setWittyText("Rest in spaghetti Columbia :(").setTranslationKey("mp_fuselage_15_kerosene_insulation");
		mp_fuselage_15_kerosene_metal = 		((ItemCustomMissilePart) mp_fuselage_15_kerosene).copy().setRarity(Rarity.UNCOMMON).setAuthor("Hoboy").setTitle("Bolted Metal").setHealth(60F).setWittyText("Metal frame with metal plating reinforced with bolted metal sheets and metal.").setTranslationKey("mp_fuselage_15_kerosene_metal");
		mp_fuselage_15_kerosene_decorated = 	((ItemCustomMissilePart) mp_fuselage_15_kerosene).copy().setRarity(Rarity.UNCOMMON).setAuthor("Hoboy").setTitle("Decorated").setHealth(60F).setTranslationKey("mp_fuselage_15_kerosene_decorated");
		mp_fuselage_15_kerosene_steampunk = 	((ItemCustomMissilePart) mp_fuselage_15_kerosene).copy().setRarity(Rarity.RARE).setAuthor("Hoboy").setTitle("Steampunk").setHealth(60F).setTranslationKey("mp_fuselage_15_kerosene_steampunk");
		mp_fuselage_15_kerosene_polite = 		((ItemCustomMissilePart) mp_fuselage_15_kerosene).copy().setRarity(Rarity.LEGENDARY).setAuthor("Hoboy").setTitle("Polite").setHealth(60F).setTranslationKey("mp_fuselage_15_kerosene_polite");
		mp_fuselage_15_kerosene_blackjack = 	((ItemCustomMissilePart) mp_fuselage_15_kerosene).copy().setRarity(Rarity.LEGENDARY).setTitle("Queen Whiskey").setHealth(100F).setTranslationKey("mp_fuselage_15_kerosene_blackjack");
		mp_fuselage_15_kerosene_lambda = 		((ItemCustomMissilePart) mp_fuselage_15_kerosene).copy().setRarity(Rarity.RARE).setAuthor("VT-6/24").setTitle("Lambda Complex").setHealth(75F).setWittyText("MAGNIFICENT MICROWAVE CASSEROLE").setTranslationKey("mp_fuselage_15_kerosene_lambda");
		mp_fuselage_15_kerosene_minuteman = 	((ItemCustomMissilePart) mp_fuselage_15_kerosene).copy().setRarity(Rarity.UNCOMMON).setAuthor("Spexta").setTitle("MX 1702").setTranslationKey("mp_fuselage_15_kerosene_minuteman");
		mp_fuselage_15_kerosene_pip = 			((ItemCustomMissilePart) mp_fuselage_15_kerosene).copy().setRarity(Rarity.EPIC).setAuthor("The Doctor").setTitle("LittlePip").setWittyText("31!").setCreativeTab(null).setTranslationKey("mp_fuselage_15_kerosene_pip");
		mp_fuselage_15_kerosene_taint = 		((ItemCustomMissilePart) mp_fuselage_15_kerosene).copy().setRarity(Rarity.UNCOMMON).setAuthor("Sam").setTitle("Tainted").setWittyText("DUN-DUN!").setTranslationKey("mp_fuselage_15_kerosene_taint");
		mp_fuselage_15_kerosene_yuck = 			((ItemCustomMissilePart) mp_fuselage_15_kerosene).copy().setRarity(Rarity.EPIC).setAuthor("Hoboy").setTitle("Flesh").setWittyText("Note: Never clean DNA vials with your own spit.").setHealth(60F).setTranslationKey("mp_fuselage_15_kerosene_yuck");

		mp_fuselage_15_solid = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeFuselage(FuelType.SOLID, 15000, 1000, PartSize.SIZE_15, PartSize.SIZE_15).setHealth(60F)			.setTranslationKey("mp_fuselage_15_solid"), RefStrings.MODID + ":mp_fuselage");
		mp_fuselage_15_solid_insulation = 		((ItemCustomMissilePart) mp_fuselage_15_solid).copy().setRarity(Rarity.COMMON).setTitle("Orange Insulation").setHealth(65F).setTranslationKey("mp_fuselage_15_solid_insulation");
		mp_fuselage_15_solid_desh = 			((ItemCustomMissilePart) mp_fuselage_15_solid).copy().setRarity(Rarity.RARE).setAuthor("Hoboy").setTitle("Desh Plating").setHealth(80F).setTranslationKey("mp_fuselage_15_solid_desh");
		mp_fuselage_15_solid_soviet_glory = 	((ItemCustomMissilePart) mp_fuselage_15_solid).copy().setRarity(Rarity.RARE).setAuthor("Hoboy").setTitle("Soviet Glory").setHealth(70F).setTranslationKey("mp_fuselage_15_solid_soviet_glory");
		mp_fuselage_15_solid_soviet_stank = 	((ItemCustomMissilePart) mp_fuselage_15_solid).copy().setRarity(Rarity.EPIC).setAuthor("Hoboy").setTitle("Soviet Stank").setHealth(15F).setWittyText("Aged like a fine wine! Well, almost.").setTranslationKey("mp_fuselage_15_solid_soviet_stank");
		mp_fuselage_15_solid_faust = 			((ItemCustomMissilePart) mp_fuselage_15_solid).copy().setRarity(Rarity.LEGENDARY).setAuthor("Dr.Nostalgia").setTitle("Mighty Lauren").setHealth(250F).setWittyText("Welcome to Subway, may I take your order?").setTranslationKey("mp_fuselage_15_solid_faust");
		mp_fuselage_15_solid_silvermoonlight = 	((ItemCustomMissilePart) mp_fuselage_15_solid).copy().setRarity(Rarity.UNCOMMON).setAuthor("The Master").setTitle("Silver Moonlight").setTranslationKey("mp_fuselage_15_solid_silvermoonlight");
		mp_fuselage_15_solid_snowy = 			((ItemCustomMissilePart) mp_fuselage_15_solid).copy().setRarity(Rarity.UNCOMMON).setAuthor("Dr.Nostalgia").setTitle("Chilly Day").setTranslationKey("mp_fuselage_15_solid_snowy");
		mp_fuselage_15_solid_panorama = 		((ItemCustomMissilePart) mp_fuselage_15_solid).copy().setRarity(Rarity.RARE).setAuthor("Hoboy").setTitle("Panorama").setTranslationKey("mp_fuselage_15_solid_panorama");
		mp_fuselage_15_solid_roses = 			((ItemCustomMissilePart) mp_fuselage_15_solid).copy().setRarity(Rarity.UNCOMMON).setAuthor("Hoboy").setTitle("Bed of roses").setTranslationKey("mp_fuselage_15_solid_roses");
		mp_fuselage_15_solid_mimi = 			((ItemCustomMissilePart) mp_fuselage_15_solid).copy().setRarity(Rarity.RARE).setTitle("Mimi-chan").setTranslationKey("mp_fuselage_15_solid_mimi");

		mp_fuselage_15_hydrogen = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeFuselage(FuelType.HYDROGEN, 15000, 1000, PartSize.SIZE_15, PartSize.SIZE_15).setHealth(50F)	.setTranslationKey("mp_fuselage_15_hydrogen"), RefStrings.MODID + ":mp_fuselage");
		mp_fuselage_15_hydrogen_cathedral = ((ItemCustomMissilePart) mp_fuselage_15_hydrogen).copy().setRarity(Rarity.UNCOMMON).setAuthor("Satan").setTitle("Unholy Cathedral").setTranslationKey("mp_fuselage_15_hydrogen_cathedral");

		mp_fuselage_15_balefire = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeFuselage(FuelType.BALEFIRE, 15000, 1000, PartSize.SIZE_15, PartSize.SIZE_15).setHealth(75F)	.setTranslationKey("mp_fuselage_15_balefire"), RefStrings.MODID + ":mp_fuselage");

		mp_fuselage_15_20_kerosene = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeFuselage(FuelType.KEROSENE, 20000, 1000, PartSize.SIZE_15, PartSize.SIZE_20).setAuthor("Hoboy").setHealth(70F).setTranslationKey("mp_fuselage_15_20_kerosene").setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":mp_fuselage");
		mp_fuselage_15_20_kerosene_magnusson = ((ItemCustomMissilePart)mp_fuselage_15_20_kerosene).copy().setRarity(Rarity.RARE).setAuthor("VT-6/24").setTitle("White Forest Rocket").setWittyText("And get your cranio-conjugal parasite away from my nose cone!").setTranslationKey("mp_fuselage_15_20_kerosene_magnusson");
		mp_fuselage_15_20_solid = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeFuselage(FuelType.SOLID, 20000, 1000, PartSize.SIZE_15, PartSize.SIZE_20).setHealth(70F).setTranslationKey("mp_fuselage_15_20_solid"), RefStrings.MODID + ":mp_fuselage");

		mp_warhead_10_he = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeWarhead(WarheadType.HE, 15F, 1500, PartSize.SIZE_10).setHealth(5F)								.setTranslationKey("mp_warhead_10_he").setMaxStackSize(1), RefStrings.MODID + ":mp_warhead");
		mp_warhead_10_incendiary = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeWarhead(WarheadType.INC, 15F, 1500, PartSize.SIZE_10).setHealth(5F)					.setTranslationKey("mp_warhead_10_incendiary").setMaxStackSize(1), RefStrings.MODID + ":mp_warhead");
		mp_warhead_10_buster = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeWarhead(WarheadType.BUSTER, 5F, 1500, PartSize.SIZE_10).setHealth(5F)						.setTranslationKey("mp_warhead_10_buster").setMaxStackSize(1), RefStrings.MODID + ":mp_warhead");
		mp_warhead_10_nuclear = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeWarhead(WarheadType.NUCLEAR, 35F, 1500, PartSize.SIZE_10).setTitle("Tater Tot").setHealth(10F).setTranslationKey("mp_warhead_10_nuclear").setMaxStackSize(1), RefStrings.MODID + ":mp_warhead");
		mp_warhead_10_nuclear_large = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeWarhead(WarheadType.NUCLEAR, 75F, 2500, PartSize.SIZE_10).setTitle("Chernobyl Boris").setHealth(15F).setTranslationKey("mp_warhead_10_nuclear_large").setMaxStackSize(1), RefStrings.MODID + ":mp_warhead");
		mp_warhead_10_taint = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeWarhead(WarheadType.TAINT, 15F, 1500, PartSize.SIZE_10).setHealth(20F).setRarity(Rarity.UNCOMMON).setWittyText("Eat my taint! Bureaucracy is dead and we killed it!").setTranslationKey("mp_warhead_10_taint").setMaxStackSize(1), RefStrings.MODID + ":mp_warhead");
		mp_warhead_10_cloud = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeWarhead(WarheadType.CLOUD, 15F, 1500, PartSize.SIZE_10).setHealth(20F).setRarity(Rarity.RARE).setTranslationKey("mp_warhead_10_cloud").setMaxStackSize(1), RefStrings.MODID + ":mp_warhead");
		mp_warhead_15_he = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeWarhead(WarheadType.HE, 50F, 2500, PartSize.SIZE_15).setHealth(10F)							.setTranslationKey("mp_warhead_15_he").setMaxStackSize(1), RefStrings.MODID + ":mp_warhead");
		mp_warhead_15_incendiary = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeWarhead(WarheadType.INC, 35F, 2500, PartSize.SIZE_15).setHealth(10F)					.setTranslationKey("mp_warhead_15_incendiary").setMaxStackSize(1), RefStrings.MODID + ":mp_warhead");
		mp_warhead_15_nuclear = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeWarhead(WarheadType.NUCLEAR, 125F, 5000, PartSize.SIZE_15).setTitle("Auntie Bertha").setHealth(15F).setTranslationKey("mp_warhead_15_nuclear").setMaxStackSize(1), RefStrings.MODID + ":mp_warhead");
		mp_warhead_15_nuclear_shark = ((ItemCustomMissilePart) mp_warhead_15_nuclear).copy().setRarity(Rarity.UNCOMMON).setTitle("Discount Bullet Bill").setWittyText("Nose art on a cannon bullet? Who does that?").setTranslationKey("mp_warhead_15_nuclear_shark");
		mp_warhead_15_nuclear_mimi = ((ItemCustomMissilePart) mp_warhead_15_nuclear).copy().setRarity(Rarity.RARE).setTitle("FASHIONABLE MISSILE").setTranslationKey("mp_warhead_15_nuclear_mimi");
		mp_warhead_15_boxcar = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeWarhead(WarheadType.TX, 250F, 7500, PartSize.SIZE_15).setWittyText("?!?!").setHealth(35F).setRarity(Rarity.LEGENDARY).setTranslationKey("mp_warhead_15_boxcar").setMaxStackSize(1), RefStrings.MODID + ":mp_warhead");
		mp_warhead_15_n2 = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeWarhead(WarheadType.N2, 100F, 5000, PartSize.SIZE_15).setWittyText("[screams geometrically]").setHealth(20F).setRarity(Rarity.RARE).setTranslationKey("mp_warhead_15_n2").setMaxStackSize(1), RefStrings.MODID + ":mp_warhead");
		mp_warhead_15_balefire = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeWarhead(WarheadType.BALEFIRE, 100F, 7500, PartSize.SIZE_15).setRarity(Rarity.LEGENDARY).setAuthor("VT-6/24").setHealth(15F).setWittyText("Hightower, never forgetti.").setTranslationKey("mp_warhead_15_balefire").setMaxStackSize(1), RefStrings.MODID + ":mp_warhead");
		mp_warhead_15_turbine = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeWarhead(WarheadType.TURBINE, 200F, 5000, PartSize.SIZE_15).setRarity(Rarity.SEWS_CLOTHES_AND_SUCKS_HORSE_COCK).setHealth(250F).setTranslationKey("mp_warhead_15_turbine").setMaxStackSize(1), RefStrings.MODID + ":mp_warhead");

		mp_chip_1 = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeChip(0.1F)	.setTranslationKey("mp_c_1").setMaxStackSize(1), RefStrings.MODID + ":mp_c_1");
		mp_chip_2 = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeChip(0.05F)	.setTranslationKey("mp_c_2").setMaxStackSize(1), RefStrings.MODID + ":mp_c_2");
		mp_chip_3 = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeChip(0.01F)	.setTranslationKey("mp_c_3").setMaxStackSize(1), RefStrings.MODID + ":mp_c_3");
		mp_chip_4 = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeChip(0.005F)	.setTranslationKey("mp_c_4").setMaxStackSize(1), RefStrings.MODID + ":mp_c_4");
		mp_chip_5 = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeChip(0.0F)	.setTranslationKey("mp_c_5").setMaxStackSize(1), RefStrings.MODID + ":mp_c_5");

		rp_fuselage_20_12 = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeFuselage(FuelType.ANY, 64_000, 4000, PartSize.SIZE_20, PartSize.SIZE_20).setTranslationKey("rp_f_20_12"), RefStrings.MODID + ":mp_fuselage");
		rp_fuselage_20_6 = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeFuselage(FuelType.ANY, 32_000, 2100, PartSize.SIZE_20, PartSize.SIZE_20).setTranslationKey("rp_f_20_6"), RefStrings.MODID + ":mp_fuselage");
		rp_fuselage_20_3 = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeFuselage(FuelType.ANY, 16_000, 1200, PartSize.SIZE_20, PartSize.SIZE_20).setTranslationKey("rp_f_20_3"), RefStrings.MODID + ":mp_fuselage");
		rp_fuselage_20_1 = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeFuselage(FuelType.ANY, 6_000, 500, PartSize.SIZE_20, PartSize.SIZE_20).setTranslationKey("rp_f_20_1"), RefStrings.MODID + ":mp_fuselage");

		rp_legs_20 = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeStability(0, PartSize.SIZE_20).setMaxStackSize(1).setTranslationKey("rp_l_20"), RefStrings.MODID + ":mp_fuselage");

		rp_capsule_20 = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeWarhead(WarheadType.APOLLO, 15F, 8_000, PartSize.SIZE_20).setTranslationKey("rp_c_20").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":mp_warhead");
		rp_station_core_20 = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeWarhead(WarheadType.SATELLITE, 15F, 64_000, PartSize.SIZE_20).setTranslationKey("rp_sc_20").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":mp_warhead");
		rp_pod_20 = NativeItemTextureRegistry.texture(new ItemCustomMissilePart().makeWarhead(WarheadType.APOLLO, 15F, 4_000, PartSize.SIZE_20).setTranslationKey("rp_pod_20").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":mp_warhead");

		ammo_shell = (ItemEnumMulti) new ItemAmmo(Ammo240Shell.class).setCreativeTab(MainRegistry.weaponTab).setTranslationKey("ammo_shell");
		ammo_dgk = new ItemCustomLore().setTranslationKey("ammo_dgk").setCreativeTab(MainRegistry.weaponTab);
		ammo_fireext = (ItemEnumMulti) new ItemAmmo(AmmoFireExt.class).setCreativeTab(MainRegistry.weaponTab).setTranslationKey("ammo_fireext");
		ammo_misc = new ItemAmmo(AmmoMisc.class).setTranslationKey("ammo_misc");
		ammo_arty = new ItemAmmoArty().setTranslationKey("ammo_arty");
		ammo_himars = new ItemAmmoHIMARS().setTranslationKey("ammo_himars");

		gun_b92_ammo = NativeItemTextureRegistry.texture(new GunB92Cell().setTranslationKey("gun_b92_ammo").setMaxStackSize(1).setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":gun_b92_ammo_alt");
		gun_b92 = NativeItemTextureRegistry.texture(new GunB92().setTranslationKey("gun_b92").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":gun_b92");

		ToolMaterial matCrucible = EnumHelper.addToolMaterial("CRUCIBLE", 10, 3, 50.0F, 100.0F, 0);
		crucible = NativeItemTextureRegistry.texture(new ItemCrucible(5000, 1F, matCrucible).setTranslationKey("crucible").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":crucible");

		stick_dynamite = NativeItemTextureRegistry.texture(new ItemGrenade(3).setTranslationKey("stick_dynamite").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":stick_dynamite");
		stick_dynamite_fishing = NativeItemTextureRegistry.texture(new ItemGrenadeFishing(3).setTranslationKey("stick_dynamite_fishing").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":stick_dynamite_fishing");
		stick_tnt = NativeItemTextureRegistry.texture(new Item().setTranslationKey("stick_tnt").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":stick_tnt");
		stick_semtex = NativeItemTextureRegistry.texture(new Item().setTranslationKey("stick_semtex").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":stick_semtex");
		stick_c4 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("stick_c4").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":stick_c4");

		grenade_generic = NativeItemTextureRegistry.texture(new ItemGrenade(4).setTranslationKey("grenade_generic").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_generic");
		grenade_strong = NativeItemTextureRegistry.texture(new ItemGrenade(5).setTranslationKey("grenade_strong").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_strong");
		grenade_frag = NativeItemTextureRegistry.texture(new ItemGrenade(4).setTranslationKey("grenade_frag").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_frag_alt");
		grenade_fire = NativeItemTextureRegistry.texture(new ItemGrenade(4).setTranslationKey("grenade_fire").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_fire_alt");
		grenade_shrapnel = NativeItemTextureRegistry.texture(new ItemGrenade(4).setTranslationKey("grenade_shrapnel").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_shrapnel");
		grenade_cluster = NativeItemTextureRegistry.texture(new ItemGrenade(5).setTranslationKey("grenade_cluster").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_cluster_alt");
		grenade_flare = NativeItemTextureRegistry.texture(new ItemGrenade(0).setTranslationKey("grenade_flare").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_flare_alt");
		grenade_electric = NativeItemTextureRegistry.texture(new ItemGrenade(5).setTranslationKey("grenade_electric").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_electric_alt");
		grenade_poison = NativeItemTextureRegistry.texture(new ItemGrenade(4).setTranslationKey("grenade_poison").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_poison_alt");
		grenade_gas = NativeItemTextureRegistry.texture(new ItemGrenade(4).setTranslationKey("grenade_gas").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_gas_alt");
		grenade_pulse = NativeItemTextureRegistry.texture(new ItemGrenade(4).setTranslationKey("grenade_pulse").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_pulse");
		grenade_plasma = NativeItemTextureRegistry.texture(new ItemGrenade(5).setTranslationKey("grenade_plasma").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_plasma_alt");
		grenade_tau = NativeItemTextureRegistry.texture(new ItemGrenade(5).setTranslationKey("grenade_tau").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_tau_alt");
		grenade_schrabidium = NativeItemTextureRegistry.texture(new ItemGrenade(7).setTranslationKey("grenade_schrabidium").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_schrabidium_alt");
		grenade_lemon = NativeItemTextureRegistry.texture(new ItemGrenade(4).setTranslationKey("grenade_lemon").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_lemon");
		grenade_gascan = NativeItemTextureRegistry.texture(new ItemGrenade(-1).setTranslationKey("grenade_gascan").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_gascan");
		grenade_kyiv = NativeItemTextureRegistry.texture(new ItemGrenadeKyiv(-1).setTranslationKey("grenade_kyiv").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_kyiv");
		grenade_mk2 = NativeItemTextureRegistry.texture(new ItemGrenade(5).setTranslationKey("grenade_mk2").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_mk2_alt");
		grenade_aschrab = NativeItemTextureRegistry.texture(new ItemGrenade(-1).setTranslationKey("grenade_aschrab").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_aschrab");
		grenade_nuke = NativeItemTextureRegistry.texture(new ItemGrenade(-1).setTranslationKey("grenade_nuke").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_nuke_alt");
		grenade_nuclear = NativeItemTextureRegistry.texture(new ItemGrenade(7).setTranslationKey("grenade_nuclear").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_nuclear");
		grenade_zomg = NativeItemTextureRegistry.texture(new ItemGrenade(7).setTranslationKey("grenade_zomg").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_zomg");
		grenade_black_hole = NativeItemTextureRegistry.texture(new ItemGrenade(7).setTranslationKey("grenade_black_hole").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_black_hole");
		grenade_cloud = NativeItemTextureRegistry.texture(new ItemGrenade(-1).setTranslationKey("grenade_cloud").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_cloud");
		grenade_pink_cloud = NativeItemTextureRegistry.texture(new ItemGrenade(-1).setTranslationKey("grenade_pink_cloud").setCreativeTab(null), RefStrings.MODID + ":grenade_pink_cloud");
		ullapool_caber = NativeItemTextureRegistry.texture(new WeaponSpecial(MainRegistry.tMatSteel).setTranslationKey("ullapool_caber").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":ullapool_caber");

		grenade_if_generic = NativeItemTextureRegistry.texture(new ItemGrenade(4).setTranslationKey("grenade_if_generic").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_if_generic");
		grenade_if_he = NativeItemTextureRegistry.texture(new ItemGrenade(5).setTranslationKey("grenade_if_he").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_if_he");
		grenade_if_bouncy = NativeItemTextureRegistry.texture(new ItemGrenade(4).setTranslationKey("grenade_if_bouncy").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_if_bouncy");
		grenade_if_sticky = NativeItemTextureRegistry.texture(new ItemGrenade(4).setTranslationKey("grenade_if_sticky").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_if_sticky");
		grenade_if_impact = NativeItemTextureRegistry.texture(new ItemGrenade(-1).setTranslationKey("grenade_if_impact").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_if_impact");
		grenade_if_incendiary = NativeItemTextureRegistry.texture(new ItemGrenade(4).setTranslationKey("grenade_if_incendiary").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_if_incendiary");
		grenade_if_toxic = NativeItemTextureRegistry.texture(new ItemGrenade(4).setTranslationKey("grenade_if_toxic").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_if_toxic");
		grenade_if_concussion = NativeItemTextureRegistry.texture(new ItemGrenade(4).setTranslationKey("grenade_if_concussion").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_if_concussion");
		grenade_if_brimstone = NativeItemTextureRegistry.texture(new ItemGrenade(5).setTranslationKey("grenade_if_brimstone").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_if_brimstone");
		grenade_if_mystery = NativeItemTextureRegistry.texture(new ItemGrenade(5).setTranslationKey("grenade_if_mystery").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_if_mystery");
		grenade_if_spark = NativeItemTextureRegistry.texture(new ItemGrenade(7).setTranslationKey("grenade_if_spark").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_if_spark");
		grenade_if_hopwire = NativeItemTextureRegistry.texture(new ItemGrenade(7).setTranslationKey("grenade_if_hopwire").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_if_hopwire");
		grenade_if_null = NativeItemTextureRegistry.texture(new ItemGrenade(7).setTranslationKey("grenade_if_null").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_if_null");

		grenade_smart = NativeItemTextureRegistry.texture(new ItemGrenade(-1).setTranslationKey("grenade_smart").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_smart");
		grenade_mirv = NativeItemTextureRegistry.texture(new ItemGrenade(1).setTranslationKey("grenade_mirv").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_mirv");
		grenade_breach = NativeItemTextureRegistry.texture(new ItemGrenade(-1).setTranslationKey("grenade_breach").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_breach");
		grenade_burst = NativeItemTextureRegistry.texture(new ItemGrenade(1).setTranslationKey("grenade_burst").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_burst");
		nuclear_waste_pearl = NativeItemTextureRegistry.texture(new ItemGrenade(-1).setTranslationKey("nuclear_waste_pearl").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":nuclear_waste_pearl");

		weaponized_starblaster_cell = NativeItemTextureRegistry.texture(new WeaponizedCell().setTranslationKey("weaponized_starblaster_cell").setMaxStackSize(1).setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":gun_b92_ammo_weaponized");

		bomb_waffle = NativeItemTextureRegistry.texture(new ItemWaffle(20, false).setTranslationKey("bomb_waffle").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":bomb_waffle");
		schnitzel_vegan = NativeItemTextureRegistry.texture(new ItemSchnitzelVegan(0, true).setTranslationKey("schnitzel_vegan").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":schnitzel_vegan");
		cotton_candy = NativeItemTextureRegistry.texture(new ItemCottonCandy(5, false).setTranslationKey("cotton_candy").setCreativeTab(MainRegistry.consumableTab).setFull3D(), RefStrings.MODID + ":cotton_candy");
		apple_lead = NativeItemTextureRegistry.texture(new ItemAppleSchrabidium(5, 0, false).setTranslationKey("apple_lead").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":apple_lead");
		apple_schrabidium = NativeItemTextureRegistry.texture(new ItemAppleSchrabidium(20, 100, false).setTranslationKey("apple_schrabidium").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":apple_schrabidium");
		tem_flakes = NativeItemTextureRegistry.texture(new ItemTemFlakes(0, 0, false).setTranslationKey("tem_flakes").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":tem_flakes");
		glowing_stew = NativeItemTextureRegistry.texture(new ItemSoup(6).setTranslationKey("glowing_stew").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":glowing_stew");
		balefire_scrambled = NativeItemTextureRegistry.texture(new ItemSoup(6).setTranslationKey("balefire_scrambled").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":balefire_scrambled");
		balefire_and_ham = NativeItemTextureRegistry.texture(new ItemSoup(6).setTranslationKey("balefire_and_ham").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":balefire_and_ham");
		lemon = NativeItemTextureRegistry.texture(new ItemLemon(3, 0.5F, false).setTranslationKey("lemon").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":lemon");
		definitelyfood = NativeItemTextureRegistry.texture(new ItemLemon(3, 0.5F, false).setTranslationKey("definitelyfood").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":definitelyfood");
		med_ipecac = NativeItemTextureRegistry.texture(new ItemLemon(0, 0, false).setTranslationKey("med_ipecac").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":med_ipecac_new");
		med_ptsd = NativeItemTextureRegistry.texture(new ItemLemon(0, 0, false).setTranslationKey("med_ptsd").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":med_ptsd_new");
		med_schizophrenia = NativeItemTextureRegistry.texture(new ItemLemon(0, 0, false).setTranslationKey("med_schizophrenia").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":med_schizophrenia_new");
		loops = NativeItemTextureRegistry.texture(new ItemLemon(4, 0.25F, false).setTranslationKey("loops").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":loops");
		loop_stew = NativeItemTextureRegistry.texture(new ItemLemon(10, 0.5F, false).setTranslationKey("loop_stew").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":loop_stew");
		spongebob_macaroni = NativeItemTextureRegistry.texture(new ItemLemon(5, 1F, false).setTranslationKey("spongebob_macaroni").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":spongebob_macaroni");
		fooditem = NativeItemTextureRegistry.texture(new ItemLemon(2, 0.6F, false).setTranslationKey("fooditem").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":fooditem");
		twinkie = NativeItemTextureRegistry.texture(new ItemLemon(3, 0.25F, false).setTranslationKey("twinkie").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":twinkie");
		static_sandwich = NativeItemTextureRegistry.texture(new ItemLemon(6, 1F, false).setTranslationKey("static_sandwich").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":static_sandwich");
		pudding = NativeItemTextureRegistry.texture(new ItemLemon(6, 0.6F, false).setTranslationKey("pudding").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":pudding");
		canteen_vodka = NativeItemTextureRegistry.texture(new ItemCanteen(3 * 60).setTranslationKey("canteen_vodka").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":canteen_vodka");
		pancake = NativeItemTextureRegistry.texture(new ItemPancake(20, 1.0F, false).setTranslationKey("pancake").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":pancake");
		nugget = NativeItemTextureRegistry.texture(new ItemLemon(200, 1F, false).setTranslationKey("nugget").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":nugget");
		peas = NativeItemTextureRegistry.texture(new ItemPeas().setTranslationKey("peas").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":peas");
		marshmallow = NativeItemTextureRegistry.texture(new ItemMarshmallow().setTranslationKey("marshmallow").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":marshmallow");
		cheese = NativeItemTextureRegistry.texture(new ItemLemon(5, 0.75F, false).setTranslationKey("cheese").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":cheese");
		quesadilla = NativeItemTextureRegistry.texture(new ItemLemon(8, 1F, false).setTranslationKey("cheese_quesadilla").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":quesadilla");
		mucho_mango = NativeItemTextureRegistry.texture(new ItemMuchoMango(10).setTranslationKey("mucho_mango").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":mucho_mango");
		glyphid_meat = NativeItemTextureRegistry.texture(new ItemLemon(3, 0.5F, true).setTranslationKey("glyphid_meat").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":glyphid_meat");
		glyphid_meat_grilled = NativeItemTextureRegistry.texture(new ItemLemon(8, 0.75F, true).setPotionEffect(Potion.damageBoost.id, 180, 1, 1F).setTranslationKey("glyphid_meat_grilled").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":glyphid_meat_grilled");
		chocolate_mint_billet = NativeItemTextureRegistry.texture(new ItemLemon(5, 5F, true).setTranslationKey("chocolate_mint_billet").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":chocolate_mint_billet");
		egg_glyphid = NativeItemTextureRegistry.texture(new Item().setTranslationKey("egg_glyphid").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":egg_glyphid");
		flour = NativeItemTextureRegistry.texture(new Item().setTranslationKey("flour").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":flour");

		defuser = NativeItemTextureRegistry.texture(new ItemDefuser(ToolType.DEFUSER, 100).setTranslationKey("defuser").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":defuser");
		reacher = NativeItemTextureRegistry.texture(new Item().setTranslationKey("reacher").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":reacher");
		bismuth_tool = NativeItemTextureRegistry.texture(new ItemAmatExtractor().setTranslationKey("bismuth_tool").setMaxStackSize(1).setFull3D().setCreativeTab(null), RefStrings.MODID + ":bismuth_tool");
		meltdown_tool = NativeItemTextureRegistry.texture(new ItemDyatlov().setTranslationKey("meltdown_tool").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":meltdown_tool");

		flame_pony = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("flame_pony").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":flame_pony");
		flame_conspiracy = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("flame_conspiracy").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":flame_conspiracy");
		flame_politics = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("flame_politics").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":flame_politics");
		flame_opinion = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("flame_opinion").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":flame_opinion");

		early_explosive_lenses = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("early_explosive_lenses").setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":gadget_explosive8");
		explosive_lenses = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("explosive_lenses").setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":man_explosive8");
		gadget_wireing = NativeItemTextureRegistry.texture(new Item().setTranslationKey("gadget_wireing").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":gadget_wireing");
		gadget_core = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.UNCOMMON).setTranslationKey("gadget_core").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":gadget_core");

		boy_igniter = NativeItemTextureRegistry.texture(new Item().setTranslationKey("boy_igniter").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":boy_igniter");
		boy_propellant = NativeItemTextureRegistry.texture(new Item().setTranslationKey("boy_propellant").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":boy_propellant");
		boy_bullet = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.UNCOMMON).setTranslationKey("boy_bullet").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":boy_bullet");
		boy_target = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.UNCOMMON).setTranslationKey("boy_target").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":boy_target");
		boy_shielding = NativeItemTextureRegistry.texture(new Item().setTranslationKey("boy_shielding").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":boy_shielding");

		man_igniter = NativeItemTextureRegistry.texture(new Item().setTranslationKey("man_igniter").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":man_igniter");
		man_core = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.UNCOMMON).setTranslationKey("man_core").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":man_core");

		mike_core = NativeItemTextureRegistry.texture(new Item().setTranslationKey("mike_core").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":mike_core");
		mike_deut = NativeItemTextureRegistry.texture(new Item().setTranslationKey("mike_deut").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setContainerItem(ModItems.tank_steel), RefStrings.MODID + ":mike_deut");
		mike_cooling_unit = NativeItemTextureRegistry.texture(new Item().setTranslationKey("mike_cooling_unit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":mike_cooling_unit");

		tsar_core = NativeItemTextureRegistry.texture(new Item().setTranslationKey("tsar_core").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":tsar_core");

		fleija_igniter = NativeItemTextureRegistry.texture(new ItemFleija().setTranslationKey("fleija_igniter").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":fleija_igniter");
		fleija_propellant = NativeItemTextureRegistry.texture(new ItemFleija().setTranslationKey("fleija_propellant").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":fleija_propellant");
		fleija_core = NativeItemTextureRegistry.texture(new ItemFleija().setTranslationKey("fleija_core").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":fleija_core");

		solinium_igniter = NativeItemTextureRegistry.texture(new ItemSolinium().setTranslationKey("solinium_igniter").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":solinium_igniter");
		solinium_propellant = NativeItemTextureRegistry.texture(new ItemSolinium().setTranslationKey("solinium_propellant").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":solinium_propellant");
		solinium_core = NativeItemTextureRegistry.texture(new ItemSolinium().setTranslationKey("solinium_core").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":solinium_core");

		n2_charge = NativeItemTextureRegistry.texture(new ItemN2().setTranslationKey("n2_charge").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":n2_charge");

		egg_balefire_shard = NativeItemTextureRegistry.texture(new Item().setTranslationKey("egg_balefire_shard").setMaxStackSize(16).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":egg_balefire_shard");
		egg_balefire = NativeItemTextureRegistry.texture(new Item().setTranslationKey("egg_balefire").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":egg_balefire");

		custom_tnt = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("custom_tnt").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":custom_tnt");
		custom_nuke = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("custom_nuke").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":custom_nuke");
		custom_hydro = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("custom_hydro").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":custom_hydro");
		custom_amat = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("custom_amat").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":custom_amat");
		custom_dirty = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("custom_dirty").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":custom_dirty");
		custom_schrab = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("custom_schrab").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":custom_schrab");
		custom_fall = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("custom_fall").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":custom_fall");

		battery_generic = NativeItemTextureRegistry.texture(new ItemBattery(5000, 100, 100).setTranslationKey("battery_generic").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":battery_generic_new");
		battery_advanced = NativeItemTextureRegistry.texture(new ItemBattery(20000, 500, 500).setTranslationKey("battery_advanced").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":battery_advanced_new");
		battery_lithium = NativeItemTextureRegistry.texture(new ItemBattery(250000, 1000, 1000).setTranslationKey("battery_lithium").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":battery_lithium");
		battery_schrabidium = NativeItemTextureRegistry.texture(new ItemBattery(1000000, 5000, 5000).setTranslationKey("battery_schrabidium").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":battery_schrabidium_new");
		battery_spark = NativeItemTextureRegistry.texture(new Item().setTranslationKey("battery_spark").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":battery_spark");
		battery_trixite = NativeItemTextureRegistry.texture(new Item().setTranslationKey("battery_trixite").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":battery_trixite");
		battery_red_cell = NativeItemTextureRegistry.texture(new ItemBattery(15000, 100, 100).setTranslationKey("battery_red_cell").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":battery_red_cell");
		battery_red_cell_6 = NativeItemTextureRegistry.texture(new ItemBattery(15000 * 6, 100, 100).setTranslationKey("battery_red_cell_6").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":battery_red_cell_6");
		battery_red_cell_24 = NativeItemTextureRegistry.texture(new ItemBattery(15000 * 24, 100, 100).setTranslationKey("battery_red_cell_24").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":battery_red_cell_24");
		battery_advanced_cell = NativeItemTextureRegistry.texture(new ItemBattery(60000, 500, 500).setTranslationKey("battery_advanced_cell").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":battery_advanced_cell");
		battery_advanced_cell_4 = NativeItemTextureRegistry.texture(new ItemBattery(60000 * 4, 500, 500).setTranslationKey("battery_advanced_cell_4").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":battery_advanced_cell_4");
		battery_advanced_cell_12 = NativeItemTextureRegistry.texture(new ItemBattery(60000 * 12, 500, 500).setTranslationKey("battery_advanced_cell_12").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":battery_advanced_cell_12");
		battery_lithium_cell = NativeItemTextureRegistry.texture(new ItemBattery(750000, 1000, 1000).setTranslationKey("battery_lithium_cell").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":battery_lithium_cell");
		battery_lithium_cell_3 = NativeItemTextureRegistry.texture(new ItemBattery(750000 * 3, 1000, 1000).setTranslationKey("battery_lithium_cell_3").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":battery_lithium_cell_3");
		battery_lithium_cell_6 = NativeItemTextureRegistry.texture(new ItemBattery(750000 * 6, 1000, 1000).setTranslationKey("battery_lithium_cell_6").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":battery_lithium_cell_6");
		battery_schrabidium_cell = NativeItemTextureRegistry.texture(new ItemBattery(3000000, 5000, 5000).setTranslationKey("battery_schrabidium_cell").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":battery_schrabidium_cell");
		battery_schrabidium_cell_2 = NativeItemTextureRegistry.texture(new ItemBattery(3000000 * 2, 5000, 5000).setTranslationKey("battery_schrabidium_cell_2").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":battery_schrabidium_cell_2");
		battery_schrabidium_cell_4 = NativeItemTextureRegistry.texture(new ItemBattery(3000000 * 4, 5000, 5000).setTranslationKey("battery_schrabidium_cell_4").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":battery_schrabidium_cell_4");
		battery_spark_cell_6 = NativeItemTextureRegistry.texture(new ItemBattery(100000000L * 6L, 2000000, 2000000).setTranslationKey("battery_spark_cell_6").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":battery_spark_cell_6");
		battery_spark_cell_25 = NativeItemTextureRegistry.texture(new ItemBattery(100000000L * 25L, 2000000, 2000000).setTranslationKey("battery_spark_cell_25").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":battery_spark_cell_25");
		battery_spark_cell_100 = NativeItemTextureRegistry.texture(new ItemBattery(100000000L * 100L, 2000000, 2000000).setTranslationKey("battery_spark_cell_100").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":battery_spark_cell_100");
		battery_spark_cell_1000 = NativeItemTextureRegistry.texture(new ItemBattery(100000000L * 1000L, 20000000, 20000000).setTranslationKey("battery_spark_cell_1000").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":battery_spark_cell_1000");
		battery_spark_cell_2500 = NativeItemTextureRegistry.texture(new ItemBattery(100000000L * 2500L, 20000000, 20000000).setTranslationKey("battery_spark_cell_2500").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":battery_spark_cell_2500");
		battery_spark_cell_10000 = NativeItemTextureRegistry.texture(new ItemBattery(100000000L * 10000L, 200000000, 200000000).setTranslationKey("battery_spark_cell_10000").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":battery_spark_cell_10000");
		battery_spark_cell_power = NativeItemTextureRegistry.texture(new ItemBattery(100000000L * 1000000L, 200000000, 200000000).setTranslationKey("battery_spark_cell_power").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":battery_spark_cell_power");

		battery_pack = NativeItemTextureRegistry.texture(new ItemBatteryPack().setTranslationKey("battery_pack"), RefStrings.MODID + ":battery_generic_new");
		battery_creative = NativeItemTextureRegistry.texture(new ItemBatteryCreative().setTranslationKey("battery_creative").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":battery_creative_new");
		cube_power = NativeItemTextureRegistry.texture(new ItemBattery(1000000000000000000L, 1000000000000000L, 1000000000000000L).setTranslationKey("cube_power").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":cube_power");
		core_angel = NativeItemTextureRegistry.texture(new ItemBattery(16_000_000L, 16_000_000L, 800_000L).setTranslationKey("core_angel").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":core_angel");

		battery_sc_uranium = NativeItemTextureRegistry.texture(new ItemSelfcharger(5).setTranslationKey("battery_sc_uranium").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":battery_sc_uranium");
		battery_sc_technetium = NativeItemTextureRegistry.texture(new ItemSelfcharger(25).setTranslationKey("battery_sc_technetium").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":battery_sc_technetium");
		battery_sc_plutonium = NativeItemTextureRegistry.texture(new ItemSelfcharger(100).setTranslationKey("battery_sc_plutonium").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":battery_sc_plutonium");
		battery_sc_polonium = NativeItemTextureRegistry.texture(new ItemSelfcharger(500).setTranslationKey("battery_sc_polonium").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":battery_sc_polonium");
		battery_sc_gold = NativeItemTextureRegistry.texture(new ItemSelfcharger(2500).setTranslationKey("battery_sc_gold").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":battery_sc_gold");
		battery_sc_lead = NativeItemTextureRegistry.texture(new ItemSelfcharger(5000).setTranslationKey("battery_sc_lead").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":battery_sc_lead");
		battery_sc_americium = NativeItemTextureRegistry.texture(new ItemSelfcharger(10000).setTranslationKey("battery_sc_americium").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":battery_sc_americium");
		battery_sc = NativeItemTextureRegistry.texture(new ItemBatterySC().setTranslationKey("battery_sc").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":battery_sc");

		battery_potato = NativeItemTextureRegistry.texture(new ItemBattery(1000, 0, 100).setTranslationKey("battery_potato").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":battery_potato");
		battery_potatos = NativeItemTextureRegistry.texture(new ItemPotatos(500000, 0, 100).setTranslationKey("battery_potatos").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":battery_potatos");
		hev_battery = NativeItemTextureRegistry.texture(new ItemFusionCore(150000).setTranslationKey("hev_battery").setMaxStackSize(4).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":hev_battery");
		fusion_core = NativeItemTextureRegistry.texture(new ItemFusionCore(2500000).setTranslationKey("fusion_core").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":fusion_core");
		energy_core = NativeItemTextureRegistry.texture(new ItemBattery(10000000, 0, 1000).setTranslationKey("energy_core").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":energy_core");
		fuse = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("fuse").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":fuse");
		screwdriver = new ItemTooling(ToolType.SCREWDRIVER, 100).setTranslationKey("screwdriver");
		screwdriver_desh = new ItemTooling(ToolType.SCREWDRIVER, 0).setTranslationKey("screwdriver_desh");
		hand_drill = new ItemTooling(ToolType.HAND_DRILL, 100).setTranslationKey("hand_drill");
		hand_drill_desh = new ItemTooling(ToolType.HAND_DRILL, 0).setTranslationKey("hand_drill_desh");
		wrench_archineer = NativeItemTextureRegistry.texture(new ItemToolingWeapon(ToolType.WRENCH, 1000, 12F).setTranslationKey("wrench_archineer"), RefStrings.MODID + ":wrench_archineer_hd");
		chemistry_set = new ItemCraftingDegradation(100).setTranslationKey("chemistry_set");
		chemistry_set_boron = new ItemCraftingDegradation(0).setTranslationKey("chemistry_set_boron");
		blowtorch = new ItemBlowtorch().setTranslationKey("blowtorch");
		acetylene_torch = new ItemBlowtorch().setTranslationKey("acetylene_torch");
		boltgun = new ItemBoltgun().setTranslationKey("boltgun");
		overfuse = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("overfuse").setMaxStackSize(1).setFull3D(), RefStrings.MODID + ":overfuse");
		arc_electrode = NativeItemTextureRegistry.texture(new ItemArcElectrode().setTranslationKey("arc_electrode").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":arc_electrode");
		arc_electrode_burnt = NativeItemTextureRegistry.texture(new ItemArcElectrodeBurnt().setTranslationKey("arc_electrode_burnt").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":arc_electrode_burnt");

		ams_lens = NativeItemTextureRegistry.texture(new ItemLens(60 * 60 * 60 * 20 * 100).setTranslationKey("ams_lens").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":ams_lens");
		ams_core_sing = NativeItemTextureRegistry.texture(new ItemAMSCore(1000000000L, 200, 10).setTranslationKey("ams_core_sing").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":ams_core_sing");
		ams_core_wormhole = NativeItemTextureRegistry.texture(new ItemAMSCore(1500000000L, 200, 15).setTranslationKey("ams_core_wormhole").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":ams_core_wormhole");
		ams_core_eyeofharmony = NativeItemTextureRegistry.texture(new ItemAMSCore(2500000000L, 300, 10).setTranslationKey("ams_core_eyeofharmony").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":ams_core_eyeofharmony");
		ams_core_thingy = NativeItemTextureRegistry.texture(new ItemAMSCore(5000000000L, 250, 5).setTranslationKey("ams_core_thingy").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":ams_core_thingy");

		fusion_shield_tungsten = NativeItemTextureRegistry.texture(new ItemFusionShield(60 * 60 * 60 * 5, 3500).setTranslationKey("fusion_shield_tungsten").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":fusion_shield_tungsten");
		fusion_shield_desh = NativeItemTextureRegistry.texture(new ItemFusionShield(60 * 60 * 60 * 10, 4500).setTranslationKey("fusion_shield_desh").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":fusion_shield_desh");
		fusion_shield_chlorophyte = NativeItemTextureRegistry.texture(new ItemFusionShield(60 * 60 * 60 * 15, 9000).setTranslationKey("fusion_shield_chlorophyte").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":fusion_shield_chlorophyte");
		fusion_shield_vaporwave = NativeItemTextureRegistry.texture(new ItemFusionShield(60 * 60 * 60 * 10, 1916169).setTranslationKey("fusion_shield_vaporwave").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":fusion_shield_vaporwave");

		upgrade_muffler = NativeItemTextureRegistry.texture(new ItemMuffler().setTranslationKey("upgrade_muffler").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":upgrade_muffler");

		upgrade_template = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("upgrade_template").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":upgrade_template");
		upgrade_speed_1 = NativeItemTextureRegistry.texture(new ItemMachineUpgrade(UpgradeType.SPEED, 1).setTranslationKey("upgrade_speed_1").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":upgrade_speed_1");
		upgrade_speed_2 = NativeItemTextureRegistry.texture(new ItemMachineUpgrade(UpgradeType.SPEED, 2).setTranslationKey("upgrade_speed_2").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":upgrade_speed_2");
		upgrade_speed_3 = NativeItemTextureRegistry.texture(new ItemMachineUpgrade(UpgradeType.SPEED, 3).setTranslationKey("upgrade_speed_3").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":upgrade_speed_3");
		upgrade_effect_1 = NativeItemTextureRegistry.texture(new ItemMachineUpgrade(UpgradeType.EFFECT, 1).setTranslationKey("upgrade_effect_1").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":upgrade_effect_1");
		upgrade_effect_2 = NativeItemTextureRegistry.texture(new ItemMachineUpgrade(UpgradeType.EFFECT, 2).setTranslationKey("upgrade_effect_2").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":upgrade_effect_2");
		upgrade_effect_3 = NativeItemTextureRegistry.texture(new ItemMachineUpgrade(UpgradeType.EFFECT, 3).setTranslationKey("upgrade_effect_3").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":upgrade_effect_3");
		upgrade_power_1 = NativeItemTextureRegistry.texture(new ItemMachineUpgrade(UpgradeType.POWER, 1).setTranslationKey("upgrade_power_1").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":upgrade_power_1");
		upgrade_power_2 = NativeItemTextureRegistry.texture(new ItemMachineUpgrade(UpgradeType.POWER, 2).setTranslationKey("upgrade_power_2").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":upgrade_power_2");
		upgrade_power_3 = NativeItemTextureRegistry.texture(new ItemMachineUpgrade(UpgradeType.POWER, 3).setTranslationKey("upgrade_power_3").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":upgrade_power_3");
		upgrade_fortune_1 = NativeItemTextureRegistry.texture(new ItemMachineUpgrade(UpgradeType.FORTUNE, 1).setTranslationKey("upgrade_fortune_1").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":upgrade_fortune_1");
		upgrade_fortune_2 = NativeItemTextureRegistry.texture(new ItemMachineUpgrade(UpgradeType.FORTUNE, 2).setTranslationKey("upgrade_fortune_2").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":upgrade_fortune_2");
		upgrade_fortune_3 = NativeItemTextureRegistry.texture(new ItemMachineUpgrade(UpgradeType.FORTUNE, 3).setTranslationKey("upgrade_fortune_3").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":upgrade_fortune_3");
		upgrade_afterburn_1 = NativeItemTextureRegistry.texture(new ItemMachineUpgrade(UpgradeType.AFTERBURN, 1).setTranslationKey("upgrade_afterburn_1").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":upgrade_afterburn_1");
		upgrade_afterburn_2 = NativeItemTextureRegistry.texture(new ItemMachineUpgrade(UpgradeType.AFTERBURN, 2).setTranslationKey("upgrade_afterburn_2").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":upgrade_afterburn_2");
		upgrade_afterburn_3 = NativeItemTextureRegistry.texture(new ItemMachineUpgrade(UpgradeType.AFTERBURN, 3).setTranslationKey("upgrade_afterburn_3").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":upgrade_afterburn_3");
		upgrade_overdrive_1 = NativeItemTextureRegistry.texture(new ItemMachineUpgrade(UpgradeType.OVERDRIVE, 1).setTranslationKey("upgrade_overdrive_1").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":upgrade_overdrive_1");
		upgrade_overdrive_2 = NativeItemTextureRegistry.texture(new ItemMachineUpgrade(UpgradeType.OVERDRIVE, 2).setTranslationKey("upgrade_overdrive_2").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":upgrade_overdrive_2");
		upgrade_overdrive_3 = NativeItemTextureRegistry.texture(new ItemMachineUpgrade(UpgradeType.OVERDRIVE, 3).setTranslationKey("upgrade_overdrive_3").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":upgrade_overdrive_3");
		upgrade_radius = NativeItemTextureRegistry.texture(new ItemMachineUpgrade().setTranslationKey("upgrade_radius").setMaxStackSize(16).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":upgrade_radius");
		upgrade_health = NativeItemTextureRegistry.texture(new ItemMachineUpgrade().setTranslationKey("upgrade_health").setMaxStackSize(16).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":upgrade_health");
		upgrade_smelter = NativeItemTextureRegistry.texture(new ItemMachineUpgrade().setTranslationKey("upgrade_smelter").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":upgrade_smelter");
		upgrade_shredder = NativeItemTextureRegistry.texture(new ItemMachineUpgrade().setTranslationKey("upgrade_shredder").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":upgrade_shredder");
		upgrade_centrifuge = NativeItemTextureRegistry.texture(new ItemMachineUpgrade().setTranslationKey("upgrade_centrifuge").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":upgrade_centrifuge");
		upgrade_crystallizer = NativeItemTextureRegistry.texture(new ItemMachineUpgrade().setTranslationKey("upgrade_crystallizer").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":upgrade_crystallizer");
		upgrade_nullifier = NativeItemTextureRegistry.texture(new ItemMachineUpgrade().setTranslationKey("upgrade_nullifier").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":upgrade_nullifier");
		upgrade_screm = NativeItemTextureRegistry.texture(new ItemMachineUpgrade().setTranslationKey("upgrade_screm").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":upgrade_screm");
		upgrade_gc_speed = NativeItemTextureRegistry.texture(new ItemMachineUpgrade().setTranslationKey("upgrade_gc_speed").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":upgrade_gc_speed");
		upgrade_5g = NativeItemTextureRegistry.texture(new ItemMachineUpgrade().setTranslationKey("upgrade_5g").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":upgrade_5g");
		upgrade_stack = NativeItemTextureRegistry.texture(new ItemMetaUpgrade(3).setTranslationKey("upgrade_stack").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":upgrade_stack");
		upgrade_ejector = NativeItemTextureRegistry.texture(new ItemMetaUpgrade(3).setTranslationKey("upgrade_ejector").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":upgrade_ejector");

		rebar_placer = NativeItemTextureRegistry.texture(new ItemRebarPlacer().setTranslationKey("rebar_placer").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":rebar_placer");
		wand = NativeItemTextureRegistry.texture(new ItemWand().setTranslationKey("wand_k").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setFull3D(), RefStrings.MODID + ":wand");
		wand_s = NativeItemTextureRegistry.texture(new ItemWandS().setTranslationKey("wand_s").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setFull3D(), RefStrings.MODID + ":wand_s");
		wand_d = NativeItemTextureRegistry.texture(new ItemWandD().setTranslationKey("wand_d").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setFull3D(), RefStrings.MODID + ":wand_d");
		wand_time = NativeItemTextureRegistry.texture(new ItemWandTime().setTranslationKey("wand_time").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setFull3D(), RefStrings.MODID + ":wand_time");

		structure_single = NativeItemTextureRegistry.texture(new ItemStructureSingle().setTranslationKey("structure_single").setMaxStackSize(1).setCreativeTab(null).setFull3D(), RefStrings.MODID + ":structure_single");
		structure_solid = NativeItemTextureRegistry.texture(new ItemStructureSolid().setTranslationKey("structure_solid").setMaxStackSize(1).setCreativeTab(null).setFull3D(), RefStrings.MODID + ":structure_solid");
		structure_pattern = NativeItemTextureRegistry.texture(new ItemStructurePattern().setTranslationKey("structure_pattern").setMaxStackSize(1).setCreativeTab(null).setFull3D(), RefStrings.MODID + ":structure_pattern");
		structure_randomized = NativeItemTextureRegistry.texture(new ItemStructureRandomized().setTranslationKey("structure_randomized").setMaxStackSize(1).setCreativeTab(null).setFull3D(), RefStrings.MODID + ":structure_randomized");
		structure_randomly = NativeItemTextureRegistry.texture(new ItemStructureRandomly().setTranslationKey("structure_randomly").setMaxStackSize(1).setCreativeTab(null).setFull3D(), RefStrings.MODID + ":structure_randomly");
		structure_custommachine = NativeItemTextureRegistry.texture(new ItemCMStructure().setTranslationKey("structure_custommachine").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setFull3D(), RefStrings.MODID + ":structure_custommachine");

		rod_of_discord = NativeItemTextureRegistry.texture(new ItemDiscord().setTranslationKey("rod_of_discord").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setFull3D(), RefStrings.MODID + ":rod_of_discord");

		nuke_starter_kit = NativeItemTextureRegistry.texture(new ItemStarterKit().setTranslationKey("nuke_starter_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":nuke_starter_kit");
		nuke_advanced_kit = NativeItemTextureRegistry.texture(new ItemStarterKit().setTranslationKey("nuke_advanced_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":nuke_advanced_kit");
		nuke_commercially_kit = NativeItemTextureRegistry.texture(new ItemStarterKit().setTranslationKey("nuke_commercially_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":nuke_commercially_kit");
		nuke_electric_kit = NativeItemTextureRegistry.texture(new ItemStarterKit().setTranslationKey("nuke_electric_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":nuke_electric_kit");
		gadget_kit = NativeItemTextureRegistry.texture(new ItemStarterKit().setTranslationKey("gadget_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":gadget_kit");
		boy_kit = NativeItemTextureRegistry.texture(new ItemStarterKit().setTranslationKey("boy_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":boy_kit");
		man_kit = NativeItemTextureRegistry.texture(new ItemStarterKit().setTranslationKey("man_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":man_kit");
		mike_kit = NativeItemTextureRegistry.texture(new ItemStarterKit().setTranslationKey("mike_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":mike_kit");
		tsar_kit = NativeItemTextureRegistry.texture(new ItemStarterKit().setTranslationKey("tsar_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":tsar_kit");
		multi_kit = NativeItemTextureRegistry.texture(new ItemStarterKit().setTranslationKey("multi_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":multi_kit");
		custom_kit = NativeItemTextureRegistry.texture(new ItemStarterKit().setTranslationKey("custom_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":custom_kit");
		grenade_kit = NativeItemTextureRegistry.texture(new ItemStarterKit().setTranslationKey("grenade_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":grenade_kit");
		fleija_kit = NativeItemTextureRegistry.texture(new ItemStarterKit().setTranslationKey("fleija_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":fleija_kit");
		prototype_kit = NativeItemTextureRegistry.texture(new ItemStarterKit().setTranslationKey("prototype_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":prototype_kit");
		missile_kit = NativeItemTextureRegistry.texture(new ItemStarterKit().setTranslationKey("missile_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":missile_kit");
		euphemium_kit = NativeItemTextureRegistry.texture(new ItemStarterKit().setTranslationKey("euphemium_kit").setMaxStackSize(1), RefStrings.MODID + ":code");
		solinium_kit = NativeItemTextureRegistry.texture(new ItemStarterKit().setTranslationKey("solinium_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":solinium_kit");
		hazmat_kit = NativeItemTextureRegistry.texture(new ItemStarterKit().setTranslationKey("hazmat_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":hazmat_kit");
		hazmat_red_kit = NativeItemTextureRegistry.texture(new ItemStarterKit().setTranslationKey("hazmat_red_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":hazmat_red_kit");
		hazmat_grey_kit = NativeItemTextureRegistry.texture(new ItemStarterKit().setTranslationKey("hazmat_grey_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":hazmat_grey_kit");
		kit_custom = NativeItemTextureRegistry.texture(new ItemKitCustom().setTranslationKey("kit_custom").setCreativeTab(null), RefStrings.MODID + ":kit");
		toolbox = NativeItemTextureRegistry.texture(new ItemToolBox().setTranslationKey("toolbox").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":kit_toolbox");

		loot_10 = NativeItemTextureRegistry.texture(new ItemLootCrate().setTranslationKey("loot_10").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":loot_10");
		loot_15 = NativeItemTextureRegistry.texture(new ItemLootCrate().setTranslationKey("loot_15").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":loot_15");
		loot_misc = NativeItemTextureRegistry.texture(new ItemLootCrate().setTranslationKey("loot_misc").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab), RefStrings.MODID + ":loot_misc");

		ammo_container = NativeItemTextureRegistry.texture(new ItemAmmoContainer().setTranslationKey("ammo_container").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":ammo_container");

		ingot_euphemium = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.EPIC).setTranslationKey("ingot_euphemium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":ingot_euphemium");
		nugget_euphemium = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.EPIC).setTranslationKey("nugget_euphemium").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":nugget_euphemium");
		watch = NativeItemTextureRegistry.texture(new ItemCustomLore().setRarity(EnumRarity.EPIC).setTranslationKey("watch").setMaxStackSize(1), RefStrings.MODID + ":watch");
		apple_euphemium = NativeItemTextureRegistry.texture(new ItemAppleEuphemium(20, 100, false).setTranslationKey("apple_euphemium").setMaxStackSize(1), RefStrings.MODID + ":apple_euphemium");

		igniter = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("igniter").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":trigger");
		detonator = NativeItemTextureRegistry.texture(new ItemDetonator().setTranslationKey("detonator").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":detonator");
		detonator_multi = NativeItemTextureRegistry.texture(new ItemMultiDetonator().setTranslationKey("detonator_multi").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":detonator_multi");
		detonator_laser = NativeItemTextureRegistry.texture(new ItemLaserDetonator().setTranslationKey("detonator_laser").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":detonator_laser");
		detonator_deadman = NativeItemTextureRegistry.texture(new ItemDrop().setTranslationKey("detonator_deadman").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":detonator_deadman");
		detonator_de = NativeItemTextureRegistry.texture(new ItemDrop().setTranslationKey("detonator_de").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.nukeTab), RefStrings.MODID + ":detonator_de");
		bomb_caller = NativeItemTextureRegistry.texture(new ItemBombCaller().setTranslationKey("bomb_caller").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":bomb_caller");
		meteor_remote = NativeItemTextureRegistry.texture(new ItemMeteorRemote().setTranslationKey("meteor_remote").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":meteor_remote");
		anchor_remote = NativeItemTextureRegistry.texture(new ItemAnchorRemote().setTranslationKey("anchor_remote").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":anchor_remote");
		spawn_chopper = NativeItemTextureRegistry.texture(new ItemChopper().setTranslationKey("chopper").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":chopper");
		spawn_worm = NativeItemTextureRegistry.texture(new ItemChopper().setTranslationKey("spawn_worm").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":spawn_worm");
		spawn_ufo = NativeItemTextureRegistry.texture(new ItemChopper().setTranslationKey("spawn_ufo").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":spawn_ufo");
		spawn_angel = NativeItemTextureRegistry.texture(new ItemChopper().setTranslationKey("spawn_angel").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":spawn_angel");
		spawn_duck = NativeItemTextureRegistry.texture(new ItemChopper().setTranslationKey("spawn_duck").setMaxStackSize(16).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":spawn_duck");
		linker = NativeItemTextureRegistry.texture(new ItemTeleLink().setTranslationKey("linker").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":linker");
		reactor_sensor = NativeItemTextureRegistry.texture(new ItemReactorSensor().setTranslationKey("reactor_sensor").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":reactor_sensor");
		oil_detector = NativeItemTextureRegistry.texture(new ItemOilDetector().setTranslationKey("oil_detector").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":oil_detector");
		turret_chip = NativeItemTextureRegistry.texture(new ItemTurretChip().setTranslationKey("turret_chip").setMaxStackSize(1).setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":turret_chip");
		dosimeter = NativeItemTextureRegistry.texture(new ItemDosimeter().setTranslationKey("dosimeter").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":dosimeter");
		geiger_counter = NativeItemTextureRegistry.texture(new ItemGeigerCounter().setTranslationKey("geiger_counter").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":geiger_counter");
		digamma_diagnostic = NativeItemTextureRegistry.texture(new ItemDigammaDiagnostic().setTranslationKey("digamma_diagnostic").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":digamma_diagnostic");
		pollution_detector = NativeItemTextureRegistry.texture(new ItemPollutionDetector().setTranslationKey("pollution_detector").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":pollution_detector");
		ore_density_scanner = NativeItemTextureRegistry.texture(new ItemOreDensityScanner().setTranslationKey("ore_density_scanner").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":ore_density_scanner");
		survey_scanner = NativeItemTextureRegistry.texture(new ItemSurveyScanner().setTranslationKey("survey_scanner").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":survey_scanner");
		mirror_tool = NativeItemTextureRegistry.texture(new ItemMirrorTool().setTranslationKey("mirror_tool").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":mirror_tool");
		rbmk_tool = NativeItemTextureRegistry.texture(new ItemRBMKTool().setTranslationKey("rbmk_tool").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":rbmk_tool");
		coltan_tool = NativeItemTextureRegistry.texture(new ItemColtanCompass().setTranslationKey("coltan_tool").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":coltass");
		power_net_tool = NativeItemTextureRegistry.texture(new ItemPowerNetTool().setTranslationKey("power_net_tool").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":power_net_tool");
		analysis_tool = NativeItemTextureRegistry.texture(new ItemAnalysisTool().setTranslationKey("analysis_tool").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":analysis_tool");
		coupling_tool = NativeItemTextureRegistry.texture(new ItemCouplingTool().setTranslationKey("coupling_tool").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":coupling_tool");
		drone_linker = NativeItemTextureRegistry.texture(new ItemDroneLinker().setTranslationKey("drone_linker").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":drone_linker");
		radar_linker = NativeItemTextureRegistry.texture(new ItemRadarLinker().setTranslationKey("radar_linker").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":radar_linker");
		settings_tool = NativeItemTextureRegistry.texture(new ItemSettingsTool().setTranslationKey("settings_tool").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":settings_tool");
		atmosphere_scanner = NativeItemTextureRegistry.texture(new ItemAtmosphereScanner().setTranslationKey("atmosphere_scanner").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":atmosphere_scanner");

		key = NativeItemTextureRegistry.texture(new ItemKey().setTranslationKey("key").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":key");
		key_red = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("key_red").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":key_red");
		key_red_cracked = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("key_red_cracked").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":key_red_cracked");
		key_kit = NativeItemTextureRegistry.texture(new ItemCounterfitKeys().setTranslationKey("key_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":key_pair");
		key_fake = NativeItemTextureRegistry.texture(new ItemKey().setTranslationKey("key_fake").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":key_gold");
		pin = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("pin").setMaxStackSize(8).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":pin");
		padlock_rusty = NativeItemTextureRegistry.texture(new ItemLock(1).setTranslationKey("padlock_rusty").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":padlock_rusty");
		padlock = NativeItemTextureRegistry.texture(new ItemLock(0.1).setTranslationKey("padlock").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":padlock");
		padlock_reinforced = NativeItemTextureRegistry.texture(new ItemLock(0.02).setTranslationKey("padlock_reinforced").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":padlock_reinforced");
		padlock_unbreakable = NativeItemTextureRegistry.texture(new ItemLock(0).setTranslationKey("padlock_unbreakable").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":padlock_unbreakable");

		mech_key = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("mech_key").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":mech_key");

		blueprints = NativeItemTextureRegistry.texture(new ItemBlueprints().setTranslationKey("blueprints").setCreativeTab(MainRegistry.templateTab), RefStrings.MODID + ":blueprints");
		blueprint_folder = NativeItemTextureRegistry.texture(new ItemBlueprintFolder().setTranslationKey("blueprint_folder").setCreativeTab(MainRegistry.templateTab), RefStrings.MODID + ":blueprint_folder");
		template_folder = NativeItemTextureRegistry.texture(new ItemTemplateFolder().setTranslationKey("template_folder").setMaxStackSize(1).setCreativeTab(MainRegistry.templateTab), RefStrings.MODID + ":template_folder");
		crucible_template = NativeItemTextureRegistry.texture(new ItemCrucibleTemplate().setTranslationKey("crucible_template").setMaxStackSize(1).setCreativeTab(MainRegistry.templateTab), RefStrings.MODID + ":crucible_template");
		fluid_identifier_multi = NativeItemTextureRegistry.texture(new ItemFluidIDMulti().setTranslationKey("fluid_identifier_multi").setMaxStackSize(1).setCreativeTab(MainRegistry.templateTab), RefStrings.MODID + ":fluid_identifier_multi");
		fluid_icon = NativeItemTextureRegistry.texture(new ItemFluidIcon().setTranslationKey("fluid_icon").setCreativeTab(null), RefStrings.MODID + ":fluid_icon");
		fluid_tank_empty = NativeItemTextureRegistry.texture(new Item().setTranslationKey("fluid_tank_empty").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":fluid_tank");
		fluid_tank_full = NativeItemTextureRegistry.texture(new ItemFluidTank().setTranslationKey("fluid_tank_full").setContainerItem(ModItems.fluid_tank_empty).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":fluid_tank");
		fluid_tank_lead_empty = NativeItemTextureRegistry.texture(new Item().setTranslationKey("fluid_tank_lead_empty").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":fluid_tank_lead");
		fluid_tank_lead_full = NativeItemTextureRegistry.texture(new ItemFluidTank().setTranslationKey("fluid_tank_lead_full").setContainerItem(ModItems.fluid_tank_lead_empty).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":fluid_tank_lead");
		fluid_barrel_empty = NativeItemTextureRegistry.texture(new Item().setTranslationKey("fluid_barrel_empty").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":fluid_barrel");
		fluid_barrel_full = NativeItemTextureRegistry.texture(new ItemFluidTank().setTranslationKey("fluid_barrel_full").setContainerItem(ModItems.fluid_barrel_empty).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":fluid_barrel");
		fluid_barrel_infinite = NativeItemTextureRegistry.texture(new ItemInfiniteFluid(null, 1_000_000_000).setTranslationKey("fluid_barrel_infinite").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":fluid_barrel_infinite");
		fluid_pack_empty = NativeItemTextureRegistry.texture(new Item().setTranslationKey("fluid_pack_empty").setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":fluid_pack");
		fluid_pack_full = NativeItemTextureRegistry.texture(new ItemFluidTank().setTranslationKey("fluid_pack_full").setContainerItem(ModItems.fluid_pack_empty).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":fluid_pack");
		pipette = NativeItemTextureRegistry.texture(new ItemPipette().setTranslationKey("pipette").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":pipette");
		pipette_boron = NativeItemTextureRegistry.texture(new ItemPipette().setTranslationKey("pipette_boron").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":pipette_boron");
		pipette_laboratory = NativeItemTextureRegistry.texture(new ItemPipette().setTranslationKey("pipette_laboratory").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":pipette_laboratory");
		siphon = NativeItemTextureRegistry.texture(new ItemFluidSiphon().setTranslationKey("siphon").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":siphon");
		inf_water = NativeItemTextureRegistry.texture(new ItemInfiniteFluid(Fluids.WATER, 50, true).setTranslationKey("inf_water").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":inf_water");
		inf_water_mk2 = NativeItemTextureRegistry.texture(new ItemInfiniteFluid(Fluids.WATER, 500, true).setTranslationKey("inf_water_mk2").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":inf_water_mk2");

		transporter_linker = NativeItemTextureRegistry.texture(new ItemTransporterLinker().setTranslationKey("transporter_linker").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab), RefStrings.MODID + ":transporter_linker");

		FluidTank.noDualUnload.add(fluid_barrel_infinite);
		FluidTank.noDualUnload.add(inf_water);
		FluidTank.noDualUnload.add(inf_water_mk2);

		disperser_canister_empty = NativeItemTextureRegistry.texture(new Item().setTranslationKey("disperser_canister_empty").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":disperser_canister");
		disperser_canister = NativeItemTextureRegistry.texture(new ItemDisperser().setTranslationKey("disperser_canister").setContainerItem(ModItems.disperser_canister_empty).setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":disperser_canister");

		glyphid_gland_empty = NativeItemTextureRegistry.texture(new Item().setTranslationKey("glyphid_gland_empty").setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":glyphid_gland");
		glyphid_gland = NativeItemTextureRegistry.texture(new ItemDisperser().setTranslationKey("glyphid_gland").setContainerItem(ModItems.glyphid_gland_empty).setCreativeTab(MainRegistry.weaponTab), RefStrings.MODID + ":glyphid_gland");

		siren_track = NativeItemTextureRegistry.texture(new ItemCassette().setTranslationKey("siren_track").setMaxStackSize(1).setCreativeTab(MainRegistry.templateTab), RefStrings.MODID + ":cassette");
		fluid_duct = NativeItemTextureRegistry.texture(new ItemFluidDuct().setTranslationKey("fluid_duct").setCreativeTab(MainRegistry.templateTab), RefStrings.MODID + ":duct");

		bobmazon = NativeItemTextureRegistry.texture(new ItemCatalog().setTranslationKey("bobmazon").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":bobmazon");
		bobmazon_hidden = NativeItemTextureRegistry.texture(new ItemCatalog().setTranslationKey("bobmazon_hidden").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":bobmazon_special");

		initializeItem2();
	}

	public static void initializeItem2() {
		
		ModItemsArmor.init();

		chainsaw = NativeItemTextureRegistry.texture(new ItemChainsaw(25, -0.05, MainRegistry.tMatChainsaw, EnumToolType.AXE, 5000, 1, 250,
				Fluids.DIESEL, Fluids.DIESEL_CRACK, Fluids.KEROSENE, Fluids.BIOFUEL, Fluids.GASOLINE, Fluids.GASOLINE_LEADED, Fluids.PETROIL, Fluids.PETROIL_LEADED, Fluids.COALGAS, Fluids.COALGAS_LEADED)
				.addAbility(IToolHarvestAbility.SILK, 0)
				.addAbility(IToolAreaAbility.RECURSION, 2)
				.addAbility(IWeaponAbility.CHAINSAW, 1)
				.addAbility(IWeaponAbility.BEHEADER, 0).setShears().setTranslationKey("chainsaw"), RefStrings.MODID + ":chainsaw");

		schrabidium_sword = NativeItemTextureRegistry.texture(new ItemSwordAbility(75, 0, MainRegistry.tMatSchrab)
				.addAbility(IWeaponAbility.RADIATION, 1)
				.addAbility(IWeaponAbility.VAMPIRE, 0)
				.setRarity(EnumRarity.RARE).setTranslationKey("schrabidium_sword"), RefStrings.MODID + ":schrabidium_sword");

		schrabidium_pickaxe = NativeItemTextureRegistry.texture(new ItemToolAbility(20, 0, MainRegistry.tMatSchrab, EnumToolType.PICKAXE)
				.addAbility(IWeaponAbility.RADIATION, 0)
				.addAbility(IToolAreaAbility.HAMMER, 1)
				.addAbility(IToolAreaAbility.HAMMER_FLAT, 1)
				.addAbility(IToolAreaAbility.RECURSION, 6)
				.addAbility(IToolHarvestAbility.SILK, 0)
				.addAbility(IToolHarvestAbility.LUCK, 4)
				.addAbility(IToolHarvestAbility.SMELTER, 0)
				.addAbility(IToolHarvestAbility.SHREDDER, 0)
				.setRarity(EnumRarity.RARE).setTranslationKey("schrabidium_pickaxe"), RefStrings.MODID + ":schrabidium_pickaxe");

		schrabidium_axe = NativeItemTextureRegistry.texture(new ItemToolAbility(25, 0, MainRegistry.tMatSchrab, EnumToolType.AXE)
				.addAbility(IWeaponAbility.RADIATION, 0)
				.addAbility(IToolAreaAbility.HAMMER, 1)
				.addAbility(IToolAreaAbility.HAMMER_FLAT, 1)
				.addAbility(IToolAreaAbility.RECURSION, 6)
				.addAbility(IToolHarvestAbility.SILK, 0)
				.addAbility(IToolHarvestAbility.LUCK, 4)
				.addAbility(IToolHarvestAbility.SMELTER, 0)
				.addAbility(IToolHarvestAbility.SHREDDER, 0)
				.addAbility(IWeaponAbility.BEHEADER, 0)
				.setRarity(EnumRarity.RARE).setTranslationKey("schrabidium_axe"), RefStrings.MODID + ":schrabidium_axe");

		schrabidium_shovel = NativeItemTextureRegistry.texture(new ItemToolAbility(15, 0, MainRegistry.tMatSchrab, EnumToolType.SHOVEL)
				.addAbility(IWeaponAbility.RADIATION, 0)
				.addAbility(IToolAreaAbility.HAMMER, 1)
				.addAbility(IToolAreaAbility.HAMMER_FLAT, 1)
				.addAbility(IToolAreaAbility.RECURSION, 6)
				.addAbility(IToolHarvestAbility.SILK, 0)
				.addAbility(IToolHarvestAbility.LUCK, 4)
				.addAbility(IToolHarvestAbility.SMELTER, 0)
				.addAbility(IToolHarvestAbility.SHREDDER, 0)
				.setRarity(EnumRarity.RARE).setTranslationKey("schrabidium_shovel"), RefStrings.MODID + ":schrabidium_shovel");

		schrabidium_hoe = NativeItemTextureRegistry.texture(new HoeSchrabidium(MainRegistry.tMatSchrab).setTranslationKey("schrabidium_hoe"), RefStrings.MODID + ":schrabidium_hoe");

		titanium_sword = NativeItemTextureRegistry.texture(new ItemSwordAbility(6.5F, 0, MainRegistry.tMatTitan).setTranslationKey("titanium_sword"), RefStrings.MODID + ":titanium_sword");
		titanium_pickaxe = NativeItemTextureRegistry.texture(new ItemToolAbility(4.5F, 0, MainRegistry.tMatTitan, EnumToolType.PICKAXE).setTranslationKey("titanium_pickaxe"), RefStrings.MODID + ":titanium_pickaxe");
		titanium_axe = NativeItemTextureRegistry.texture(new ItemToolAbility(5.5F, 0, MainRegistry.tMatTitan, EnumToolType.AXE)
				.addAbility(IWeaponAbility.BEHEADER, 0).setTranslationKey("titanium_axe"), RefStrings.MODID + ":titanium_axe");
		titanium_shovel = NativeItemTextureRegistry.texture(new ItemToolAbility(3.5F, 0, MainRegistry.tMatTitan, EnumToolType.SHOVEL).setTranslationKey("titanium_shovel"), RefStrings.MODID + ":titanium_shovel");
		titanium_hoe = NativeItemTextureRegistry.texture(new ModHoe(MainRegistry.tMatTitan).setTranslationKey("titanium_hoe"), RefStrings.MODID + ":titanium_hoe");
		steel_sword = NativeItemTextureRegistry.texture(new ItemSwordAbility(6F, 0, MainRegistry.tMatSteel).setTranslationKey("steel_sword"), RefStrings.MODID + ":steel_sword");
		steel_pickaxe = NativeItemTextureRegistry.texture(new ItemToolAbility(4F, 0, MainRegistry.tMatSteel, EnumToolType.PICKAXE).setTranslationKey("steel_pickaxe"), RefStrings.MODID + ":steel_pickaxe");
		steel_axe = NativeItemTextureRegistry.texture(new ItemToolAbility(5F, 0, MainRegistry.tMatSteel, EnumToolType.AXE)
				.addAbility(IWeaponAbility.BEHEADER, 0).setTranslationKey("steel_axe"), RefStrings.MODID + ":steel_axe");
		steel_shovel = NativeItemTextureRegistry.texture(new ItemToolAbility(3F, 0, MainRegistry.tMatSteel, EnumToolType.SHOVEL).setTranslationKey("steel_shovel"), RefStrings.MODID + ":steel_shovel");
		steel_hoe = NativeItemTextureRegistry.texture(new ModHoe(MainRegistry.tMatSteel).setTranslationKey("steel_hoe"), RefStrings.MODID + ":steel_hoe");

		alloy_sword = NativeItemTextureRegistry.texture(new ItemSwordAbility(8F, 0, MainRegistry.tMatAlloy)
				.addAbility(IWeaponAbility.STUN, 0).setTranslationKey("alloy_sword"), RefStrings.MODID + ":alloy_sword");

		alloy_pickaxe = NativeItemTextureRegistry.texture(new ItemToolAbility(5F, 0, MainRegistry.tMatAlloy, EnumToolType.PICKAXE)
				.addAbility(IToolAreaAbility.RECURSION, 0).setTranslationKey("alloy_pickaxe"), RefStrings.MODID + ":alloy_pickaxe");

		alloy_axe = NativeItemTextureRegistry.texture(new ItemToolAbility(7F, 0, MainRegistry.tMatAlloy, EnumToolType.AXE)
				.addAbility(IToolAreaAbility.RECURSION, 0)
				.addAbility(IWeaponAbility.BEHEADER, 0).setTranslationKey("alloy_axe"), RefStrings.MODID + ":alloy_axe");

		alloy_shovel = NativeItemTextureRegistry.texture(new ItemToolAbility(4F, 0, MainRegistry.tMatAlloy, EnumToolType.SHOVEL)
				.addAbility(IToolAreaAbility.RECURSION, 0).setTranslationKey("alloy_shovel"), RefStrings.MODID + ":alloy_shovel");

		alloy_hoe = NativeItemTextureRegistry.texture(new ModHoe(MainRegistry.tMatAlloy).setTranslationKey("alloy_hoe"), RefStrings.MODID + ":alloy_hoe");

		cmb_sword = NativeItemTextureRegistry.texture(new ItemSwordAbility(35F, 0, MainRegistry.tMatCMB)
				.addAbility(IWeaponAbility.STUN, 0)
				.addAbility(IWeaponAbility.VAMPIRE, 0).setTranslationKey("cmb_sword"), RefStrings.MODID + ":cmb_sword");

		cmb_pickaxe = NativeItemTextureRegistry.texture(new ItemToolAbility(10F, 0, MainRegistry.tMatCMB, EnumToolType.PICKAXE)
				.addAbility(IToolAreaAbility.RECURSION, 2)
				.addAbility(IToolHarvestAbility.SMELTER, 0)
				.addAbility(IToolHarvestAbility.SILK, 0)
				.addAbility(IToolHarvestAbility.LUCK, 2).setTranslationKey("cmb_pickaxe"), RefStrings.MODID + ":cmb_pickaxe");

		cmb_axe = NativeItemTextureRegistry.texture(new ItemToolAbility(30F, 0, MainRegistry.tMatCMB, EnumToolType.AXE)
				.addAbility(IToolAreaAbility.RECURSION, 2)
				.addAbility(IToolHarvestAbility.SMELTER, 0)
				.addAbility(IToolHarvestAbility.SILK, 0)
				.addAbility(IToolHarvestAbility.LUCK, 2)
				.addAbility(IWeaponAbility.BEHEADER, 0).setTranslationKey("cmb_axe"), RefStrings.MODID + ":cmb_axe");

		cmb_shovel = NativeItemTextureRegistry.texture(new ItemToolAbility(8F, 0, MainRegistry.tMatCMB, EnumToolType.SHOVEL)
				.addAbility(IToolAreaAbility.RECURSION, 2)
				.addAbility(IToolHarvestAbility.SMELTER, 0)
				.addAbility(IToolHarvestAbility.SILK, 0)
				.addAbility(IToolHarvestAbility.LUCK, 2).setTranslationKey("cmb_shovel"), RefStrings.MODID + ":cmb_shovel");

		cmb_hoe = NativeItemTextureRegistry.texture(new ModHoe(MainRegistry.tMatCMB).setTranslationKey("cmb_hoe"), RefStrings.MODID + ":cmb_hoe");

		elec_sword = NativeItemTextureRegistry.texture(new ItemSwordAbilityPower(12.5F, 0, MainRegistry.tMatElec, 500000, 1000, 100)
				.addAbility(IWeaponAbility.STUN, 2).setTranslationKey("elec_sword"), RefStrings.MODID + ":elec_sword_anim");

		elec_pickaxe = NativeItemTextureRegistry.texture(new ItemToolAbilityPower(6F, 0, MainRegistry.tMatElec, EnumToolType.PICKAXE, 500000, 1000, 100)
				.addAbility(IToolAreaAbility.HAMMER, 0)
				.addAbility(IToolAreaAbility.HAMMER_FLAT, 0)
				.addAbility(IToolAreaAbility.RECURSION, 2)
				.addAbility(IToolHarvestAbility.SILK, 0)
				.addAbility(IToolHarvestAbility.LUCK, 1).setTranslationKey("elec_pickaxe"), RefStrings.MODID + ":elec_drill_anim");

		elec_axe = NativeItemTextureRegistry.texture(new ItemToolAbilityPower(10F, 0, MainRegistry.tMatElec, EnumToolType.AXE, 500000, 1000, 100)
				.addAbility(IToolAreaAbility.HAMMER, 0)
				.addAbility(IToolAreaAbility.HAMMER_FLAT, 0)
				.addAbility(IToolAreaAbility.RECURSION, 2)
				.addAbility(IToolHarvestAbility.SILK, 0)
				.addAbility(IToolHarvestAbility.LUCK, 1)
				.addAbility(IWeaponAbility.CHAINSAW, 0)
				.addAbility(IWeaponAbility.BEHEADER, 0).setShears().setTranslationKey("elec_axe"), RefStrings.MODID + ":elec_chainsaw_anim");

		elec_shovel = NativeItemTextureRegistry.texture(new ItemToolAbilityPower(5F, 0, MainRegistry.tMatElec, EnumToolType.SHOVEL, 500000, 1000, 100)
				.addAbility(IToolAreaAbility.HAMMER, 0)
				.addAbility(IToolAreaAbility.HAMMER_FLAT, 0)
				.addAbility(IToolAreaAbility.RECURSION, 2)
				.addAbility(IToolHarvestAbility.SILK, 0)
				.addAbility(IToolHarvestAbility.LUCK, 1).setTranslationKey("elec_shovel"), RefStrings.MODID + ":elec_shovel_anim");

		desh_sword = NativeItemTextureRegistry.texture(new ItemSwordAbility(12.5F, 0, MainRegistry.tMatDesh)
				.addAbility(IWeaponAbility.STUN, 0).setTranslationKey("desh_sword"), RefStrings.MODID + ":desh_sword");

		desh_pickaxe = NativeItemTextureRegistry.texture(new ItemToolAbility(5F, -0.05, MainRegistry.tMatDesh, EnumToolType.PICKAXE)
				.addAbility(IToolAreaAbility.HAMMER, 0)
				.addAbility(IToolAreaAbility.HAMMER_FLAT, 0)
				.addAbility(IToolAreaAbility.RECURSION, 0)
				.addAbility(IToolHarvestAbility.SILK, 0)
				.addAbility(IToolHarvestAbility.LUCK, 1).setTranslationKey("desh_pickaxe"), RefStrings.MODID + ":desh_pickaxe");

		desh_axe = NativeItemTextureRegistry.texture(new ItemToolAbility(7.5F, -0.05, MainRegistry.tMatDesh, EnumToolType.AXE)
				.addAbility(IToolAreaAbility.HAMMER, 0)
				.addAbility(IToolAreaAbility.HAMMER_FLAT, 0)
				.addAbility(IToolAreaAbility.RECURSION, 0)
				.addAbility(IToolHarvestAbility.SILK, 0)
				.addAbility(IToolHarvestAbility.LUCK, 1)
				.addAbility(IWeaponAbility.BEHEADER, 0).setTranslationKey("desh_axe"), RefStrings.MODID + ":desh_axe");

		desh_shovel = NativeItemTextureRegistry.texture(new ItemToolAbility(4F, -0.05, MainRegistry.tMatDesh, EnumToolType.SHOVEL)
				.addAbility(IToolAreaAbility.HAMMER, 0)
				.addAbility(IToolAreaAbility.HAMMER_FLAT, 0)
				.addAbility(IToolAreaAbility.RECURSION, 0)
				.addAbility(IToolHarvestAbility.SILK, 0)
				.addAbility(IToolHarvestAbility.LUCK, 1).setTranslationKey("desh_shovel"), RefStrings.MODID + ":desh_shovel");

		desh_hoe = NativeItemTextureRegistry.texture(new ModHoe(MainRegistry.tMatDesh).setTranslationKey("desh_hoe"), RefStrings.MODID + ":desh_hoe");

		cobalt_sword = NativeItemTextureRegistry.texture(new ItemSwordAbility(12F, 0, MainRegistry.tMatCobalt).setTranslationKey("cobalt_sword"), RefStrings.MODID + ":cobalt_sword");
		cobalt_pickaxe = NativeItemTextureRegistry.texture(new ItemToolAbility(4F, 0, MainRegistry.tMatCobalt, EnumToolType.PICKAXE)
				.addAbility(IToolAreaAbility.RECURSION, 1)
				.addAbility(IToolHarvestAbility.SILK, 0)
				.addAbility(IToolHarvestAbility.LUCK, 0).setTranslationKey("cobalt_pickaxe"), RefStrings.MODID + ":cobalt_pickaxe");
		cobalt_axe = NativeItemTextureRegistry.texture(new ItemToolAbility(6F, 0, MainRegistry.tMatCobalt, EnumToolType.AXE)
				.addAbility(IToolAreaAbility.RECURSION, 1)
				.addAbility(IToolHarvestAbility.SILK, 0)
				.addAbility(IToolHarvestAbility.LUCK, 0)
				.addAbility(IWeaponAbility.BEHEADER, 0).setTranslationKey("cobalt_axe"), RefStrings.MODID + ":cobalt_axe");
		cobalt_shovel = NativeItemTextureRegistry.texture(new ItemToolAbility(3.5F, 0, MainRegistry.tMatCobalt, EnumToolType.SHOVEL)
				.addAbility(IToolAreaAbility.RECURSION, 1)
				.addAbility(IToolHarvestAbility.SILK, 0)
				.addAbility(IToolHarvestAbility.LUCK, 0).setTranslationKey("cobalt_shovel"), RefStrings.MODID + ":cobalt_shovel");
		cobalt_hoe = NativeItemTextureRegistry.texture(new ModHoe(MainRegistry.tMatCobalt).setTranslationKey("cobalt_hoe"), RefStrings.MODID + ":cobalt_hoe");

		ToolMaterial matDecCobalt = EnumHelper.addToolMaterial("HBM_COBALT2", 3, 2500, 15.0F, 2.5F, 75).setRepairItem(new ItemStack(ModItems.ingot_cobalt));
		cobalt_decorated_sword = NativeItemTextureRegistry.texture(new ItemSwordAbility(15F, 0, matDecCobalt)
				.addAbility(IWeaponAbility.BOBBLE, 0).setTranslationKey("cobalt_decorated_sword"), RefStrings.MODID + ":cobalt_decorated_sword");
		cobalt_decorated_pickaxe = NativeItemTextureRegistry.texture(new ItemToolAbility(6F, 0, matDecCobalt, EnumToolType.PICKAXE)
				.addAbility(IToolAreaAbility.RECURSION, 1)
				.addAbility(IToolAreaAbility.HAMMER, 0)
				.addAbility(IToolAreaAbility.HAMMER_FLAT, 0)
				.addAbility(IToolHarvestAbility.SILK, 0)
				.addAbility(IToolHarvestAbility.LUCK, 2).setTranslationKey("cobalt_decorated_pickaxe"), RefStrings.MODID + ":cobalt_decorated_pickaxe");
		cobalt_decorated_axe = NativeItemTextureRegistry.texture(new ItemToolAbility(8F, 0, matDecCobalt, EnumToolType.AXE)
				.addAbility(IToolAreaAbility.RECURSION, 1)
				.addAbility(IToolAreaAbility.HAMMER, 0)
				.addAbility(IToolAreaAbility.HAMMER_FLAT, 0)
				.addAbility(IToolHarvestAbility.SILK, 0)
				.addAbility(IToolHarvestAbility.LUCK, 2)
				.addAbility(IWeaponAbility.BEHEADER, 0).setTranslationKey("cobalt_decorated_axe"), RefStrings.MODID + ":cobalt_decorated_axe");
		cobalt_decorated_shovel = NativeItemTextureRegistry.texture(new ItemToolAbility(5F, 0, matDecCobalt, EnumToolType.SHOVEL)
				.addAbility(IToolAreaAbility.RECURSION, 1)
				.addAbility(IToolAreaAbility.HAMMER, 0)
				.addAbility(IToolAreaAbility.HAMMER_FLAT, 0)
				.addAbility(IToolHarvestAbility.SILK, 0)
				.addAbility(IToolHarvestAbility.LUCK, 2).setTranslationKey("cobalt_decorated_shovel"), RefStrings.MODID + ":cobalt_decorated_shovel");
		cobalt_decorated_hoe = NativeItemTextureRegistry.texture(new ModHoe(matDecCobalt).setTranslationKey("cobalt_decorated_hoe"), RefStrings.MODID + ":cobalt_decorated_hoe");

		ToolMaterial matStarmetal = EnumHelper.addToolMaterial("HBM_STARMETAL", 3, 3000, 20.0F, 2.5F, 100).setRepairItem(new ItemStack(ModItems.ingot_starmetal));
		starmetal_sword = NativeItemTextureRegistry.texture(new ItemSwordAbility(25F, 0, matStarmetal)
				.addAbility(IWeaponAbility.BEHEADER, 0)
				.addAbility(IWeaponAbility.STUN, 1)
				.addAbility(IWeaponAbility.BOBBLE, 0).setTranslationKey("starmetal_sword"), RefStrings.MODID + ":starmetal_sword");
		starmetal_pickaxe = NativeItemTextureRegistry.texture(new ItemToolAbility(8F, 0, matStarmetal, EnumToolType.PICKAXE)
				.addAbility(IToolAreaAbility.RECURSION, 3)
				.addAbility(IToolAreaAbility.HAMMER, 1)
				.addAbility(IToolAreaAbility.HAMMER_FLAT, 1)
				.addAbility(IToolHarvestAbility.SILK, 0)
				.addAbility(IToolHarvestAbility.LUCK, 4)
				.addAbility(IWeaponAbility.STUN, 1).setTranslationKey("starmetal_pickaxe"), RefStrings.MODID + ":starmetal_pickaxe");
		starmetal_axe = NativeItemTextureRegistry.texture(new ItemToolAbility(12F, 0, matStarmetal, EnumToolType.AXE)
				.addAbility(IToolAreaAbility.RECURSION, 3)
				.addAbility(IToolAreaAbility.HAMMER, 1)
				.addAbility(IToolAreaAbility.HAMMER_FLAT, 1)
				.addAbility(IToolHarvestAbility.SILK, 0)
				.addAbility(IToolHarvestAbility.LUCK, 4)
				.addAbility(IWeaponAbility.BEHEADER, 0)
				.addAbility(IWeaponAbility.STUN, 1).setTranslationKey("starmetal_axe"), RefStrings.MODID + ":starmetal_axe");
		starmetal_shovel = NativeItemTextureRegistry.texture(new ItemToolAbility(7F, 0, matStarmetal, EnumToolType.SHOVEL)
				.addAbility(IToolAreaAbility.RECURSION, 3)
				.addAbility(IToolAreaAbility.HAMMER, 1)
				.addAbility(IToolAreaAbility.HAMMER_FLAT, 1)
				.addAbility(IToolHarvestAbility.SILK, 0)
				.addAbility(IToolHarvestAbility.LUCK, 4)
				.addAbility(IWeaponAbility.STUN, 1).setTranslationKey("starmetal_shovel"), RefStrings.MODID + ":starmetal_shovel");
		starmetal_hoe = NativeItemTextureRegistry.texture(new ModHoe(matStarmetal).setTranslationKey("starmetal_hoe"), RefStrings.MODID + ":starmetal_hoe");

		centri_stick = NativeItemTextureRegistry.texture(new ItemToolAbility(3F, 0, MainRegistry.tMatElec, EnumToolType.MINER)
				.addAbility(IToolHarvestAbility.CENTRIFUGE, 0).setMaxDamage(50).setTranslationKey("centri_stick"), RefStrings.MODID + ":centri_stick");
		smashing_hammer = NativeItemTextureRegistry.texture(new ItemToolAbility(12F, -0.1, MainRegistry.tMatSteel, EnumToolType.MINER)
				.addAbility(IToolHarvestAbility.SHREDDER, 0).setMaxDamage(2500).setTranslationKey("smashing_hammer"), RefStrings.MODID + ":smashing_hammer");
		drax = NativeItemTextureRegistry.texture(new ItemToolAbilityPower(10F, -0.05, MainRegistry.tMatElec, EnumToolType.MINER, 500000000, 100000, 5000)
				.addAbility(IToolHarvestAbility.SMELTER, 0)
				.addAbility(IToolHarvestAbility.SHREDDER, 0)
				.addAbility(IToolHarvestAbility.LUCK, 1)
				.addAbility(IToolAreaAbility.HAMMER, 1)
				.addAbility(IToolAreaAbility.HAMMER_FLAT, 1)
				.addAbility(IToolAreaAbility.RECURSION, 2).setTranslationKey("drax").setCreativeTab(null), RefStrings.MODID + ":drax");
		drax_mk2 = NativeItemTextureRegistry.texture(new ItemToolAbilityPower(15F, -0.05, MainRegistry.tMatElec, EnumToolType.MINER, 1000000000, 250000, 7500)
				.addAbility(IToolHarvestAbility.SMELTER, 0)
				.addAbility(IToolHarvestAbility.SHREDDER, 0)
				.addAbility(IToolHarvestAbility.CENTRIFUGE, 0)
				.addAbility(IToolHarvestAbility.LUCK, 2)
				.addAbility(IToolAreaAbility.HAMMER, 2)
				.addAbility(IToolAreaAbility.HAMMER_FLAT, 2)
				.addAbility(IToolAreaAbility.RECURSION, 4).setTranslationKey("drax_mk2").setCreativeTab(null), RefStrings.MODID + ":drax_mk2");
		drax_mk3 = NativeItemTextureRegistry.texture(new ItemToolAbilityPower(20F, -0.05, MainRegistry.tMatElec, EnumToolType.MINER, 2500000000L, 500000, 10000)
				.addAbility(IToolHarvestAbility.SMELTER, 0)
				.addAbility(IToolHarvestAbility.SHREDDER, 0)
				.addAbility(IToolHarvestAbility.CENTRIFUGE, 0)
				.addAbility(IToolHarvestAbility.CRYSTALLIZER, 0)
				.addAbility(IToolHarvestAbility.SILK, 0)
				.addAbility(IToolHarvestAbility.LUCK, 3)
				.addAbility(IToolAreaAbility.HAMMER, 3)
				.addAbility(IToolAreaAbility.HAMMER_FLAT, 3)
				.addAbility(IToolAreaAbility.RECURSION, 5).setTranslationKey("drax_mk3").setCreativeTab(null), RefStrings.MODID + ":drax_mk3");

		ToolMaterial matBismuth = EnumHelper.addToolMaterial("HBM_BISMUTH", 4, 0, 50F, 0.0F, 200).setRepairItem(new ItemStack(ModItems.ingot_bismuth));
		bismuth_pickaxe = NativeItemTextureRegistry.texture(new ItemToolAbility(15F, 0, matBismuth, EnumToolType.MINER)
				.addAbility(IToolAreaAbility.HAMMER, 1)
				.addAbility(IToolAreaAbility.HAMMER_FLAT, 1)
				.addAbility(IToolAreaAbility.RECURSION, 1)
				.addAbility(IToolHarvestAbility.SHREDDER, 0)
				.addAbility(IToolHarvestAbility.LUCK, 1)
				.addAbility(IToolHarvestAbility.SILK, 0)
				.addAbility(IWeaponAbility.STUN, 2)
				.addAbility(IWeaponAbility.VAMPIRE, 0)
				.addAbility(IWeaponAbility.BEHEADER, 0)
				.setDepthRockBreaker().setTranslationKey("bismuth_pickaxe"), RefStrings.MODID + ":bismuth_pickaxe");
		bismuth_axe = NativeItemTextureRegistry.texture(new ItemToolAbility(25F, 0, matBismuth, EnumToolType.AXE)
				.addAbility(IToolAreaAbility.HAMMER, 1)
				.addAbility(IToolAreaAbility.HAMMER_FLAT, 1)
				.addAbility(IToolAreaAbility.RECURSION, 1)
				.addAbility(IToolHarvestAbility.SHREDDER, 0)
				.addAbility(IToolHarvestAbility.LUCK, 1)
				.addAbility(IToolHarvestAbility.SILK, 0)
				.addAbility(IWeaponAbility.STUN, 3)
				.addAbility(IWeaponAbility.VAMPIRE, 1)
				.addAbility(IWeaponAbility.BEHEADER, 0).setTranslationKey("bismuth_axe"), RefStrings.MODID + ":bismuth_axe");


		ToolMaterial matVolcano = EnumHelper.addToolMaterial("HBM_VOLCANIC", 4, 0, 50F, 0.0F, 200).setRepairItem(new ItemStack(ModItems.ingot_bismuth));
		volcanic_pickaxe = NativeItemTextureRegistry.texture(new ItemToolAbility(15F, 0, matVolcano, EnumToolType.MINER)
				.addAbility(IToolAreaAbility.HAMMER, 1)
				.addAbility(IToolAreaAbility.HAMMER_FLAT, 1)
				.addAbility(IToolAreaAbility.RECURSION, 1)
				.addAbility(IToolHarvestAbility.SMELTER, 0)
				.addAbility(IToolHarvestAbility.LUCK, 2)
				.addAbility(IToolHarvestAbility.SILK, 0)
				.addAbility(IWeaponAbility.FIRE, 0)
				.addAbility(IWeaponAbility.VAMPIRE, 0)
				.addAbility(IWeaponAbility.BEHEADER, 0)
				.setDepthRockBreaker().setTranslationKey("volcanic_pickaxe"), RefStrings.MODID + ":volcanic_pickaxe");
		volcanic_axe = NativeItemTextureRegistry.texture(new ItemToolAbility(25F, 0, matVolcano, EnumToolType.AXE)
				.addAbility(IToolAreaAbility.HAMMER, 1)
				.addAbility(IToolAreaAbility.HAMMER_FLAT, 1)
				.addAbility(IToolAreaAbility.RECURSION, 1)
				.addAbility(IToolHarvestAbility.SMELTER, 0)
				.addAbility(IToolHarvestAbility.LUCK, 2)
				.addAbility(IToolHarvestAbility.SILK, 0)
				.addAbility(IWeaponAbility.FIRE, 1)
				.addAbility(IWeaponAbility.VAMPIRE, 1)
				.addAbility(IWeaponAbility.BEHEADER, 0).setTranslationKey("volcanic_axe"), RefStrings.MODID + ":volcanic_axe");

		ToolMaterial matChlorophyte = EnumHelper.addToolMaterial("HBM_CHLOROPHYTE", 4, 0, 75F, 0.0F, 200).setRepairItem(new ItemStack(ModItems.powder_chlorophyte));
		chlorophyte_pickaxe = NativeItemTextureRegistry.texture(new ItemToolAbility(20F, 0, matChlorophyte, EnumToolType.MINER)
				.addAbility(IToolAreaAbility.HAMMER, 1)
				.addAbility(IToolAreaAbility.HAMMER_FLAT, 1)
				.addAbility(IToolAreaAbility.RECURSION, 1)
				.addAbility(IToolHarvestAbility.LUCK, 3)
				.addAbility(IToolHarvestAbility.CENTRIFUGE, 0)
				.addAbility(IToolHarvestAbility.MERCURY, 0)
				.addAbility(IWeaponAbility.STUN, 3)
				.addAbility(IWeaponAbility.VAMPIRE, 2)
				.addAbility(IWeaponAbility.BEHEADER, 0)
				.setDepthRockBreaker().setTranslationKey("chlorophyte_pickaxe"), RefStrings.MODID + ":chlorophyte_pickaxe");
		chlorophyte_axe = NativeItemTextureRegistry.texture(new ItemToolAbility(50F, 0, matChlorophyte, EnumToolType.AXE)
				.addAbility(IToolAreaAbility.HAMMER, 1)
				.addAbility(IToolAreaAbility.HAMMER_FLAT, 1)
				.addAbility(IToolAreaAbility.RECURSION, 1)
				.addAbility(IToolHarvestAbility.LUCK, 3)
				.addAbility(IWeaponAbility.STUN, 4)
				.addAbility(IWeaponAbility.VAMPIRE, 3)
				.addAbility(IWeaponAbility.BEHEADER, 0).setTranslationKey("chlorophyte_axe"), RefStrings.MODID + ":chlorophyte_axe");

		ToolMaterial matMese = EnumHelper.addToolMaterial("HBM_MESE", 4, 0, 100F, 0.0F, 200).setRepairItem(new ItemStack(ModItems.plate_paa));
		mese_pickaxe = NativeItemTextureRegistry.texture(new ItemToolAbility(35F, 0, matMese, EnumToolType.MINER)
				.addAbility(IToolAreaAbility.HAMMER, 2)
				.addAbility(IToolAreaAbility.HAMMER_FLAT, 2)
				.addAbility(IToolAreaAbility.RECURSION, 2)
				.addAbility(IToolHarvestAbility.CRYSTALLIZER, 0)
				.addAbility(IToolHarvestAbility.SILK, 0)
				.addAbility(IToolHarvestAbility.LUCK, 5)
				.addAbility(IToolAreaAbility.EXPLOSION, 3)
				.addAbility(IWeaponAbility.STUN, 3)
				.addAbility(IWeaponAbility.PHOSPHORUS, 0)
				.addAbility(IWeaponAbility.BEHEADER, 0)
				.setDepthRockBreaker().setTranslationKey("mese_pickaxe"), RefStrings.MODID + ":mese_pickaxe");
		mese_axe = NativeItemTextureRegistry.texture(new ItemToolAbility(75F, 0, matMese, EnumToolType.AXE)
				.addAbility(IToolAreaAbility.HAMMER, 2)
				.addAbility(IToolAreaAbility.HAMMER_FLAT, 2)
				.addAbility(IToolAreaAbility.RECURSION, 2)
				.addAbility(IToolHarvestAbility.SILK, 0)
				.addAbility(IToolHarvestAbility.LUCK, 5)
				.addAbility(IToolAreaAbility.EXPLOSION, 3)
				.addAbility(IWeaponAbility.STUN, 4)
				.addAbility(IWeaponAbility.PHOSPHORUS, 1)
				.addAbility(IWeaponAbility.BEHEADER, 0).setTranslationKey("mese_axe"), RefStrings.MODID + ":mese_axe");

		dnt_sword = NativeItemTextureRegistry.texture(new ItemSwordAbility(12F, 0, matMese).setTranslationKey("dnt_sword"), RefStrings.MODID + ":dnt_sword");

		ToolMaterial matDwarf = EnumHelper.addToolMaterial("HBM_DWARVEN", 2, 0, 4F, 0.0F, 10).setRepairItem(new ItemStack(ModItems.ingot_copper));
		dwarven_pickaxe = NativeItemTextureRegistry.texture(new ItemToolAbility(5F, -0.1, matDwarf, EnumToolType.MINER)
				.addAbility(IToolAreaAbility.HAMMER, 0)
				.addAbility(IToolAreaAbility.HAMMER_FLAT, 0).setTranslationKey("dwarven_pickaxe").setMaxDamage(250), RefStrings.MODID + ":dwarven_pickaxe");

		ToolMaterial matMeteorite = EnumHelper.addToolMaterial("HBM_METEORITE", 4, 0, 50F, 0.0F, 200).setRepairItem(new ItemStack(ModItems.plate_paa));
		meteorite_sword = NativeItemTextureRegistry.texture(new ItemSwordMeteorite(9F, 0, matMeteorite).setTranslationKey("meteorite_sword"), RefStrings.MODID + ":meteorite_sword");
		meteorite_sword_seared = NativeItemTextureRegistry.texture(new ItemSwordMeteorite(10F, 0, matMeteorite).setTranslationKey("meteorite_sword_seared"), RefStrings.MODID + ":meteorite_sword");
		meteorite_sword_reforged = NativeItemTextureRegistry.texture(new ItemSwordMeteorite(12.5F, 0, matMeteorite).setTranslationKey("meteorite_sword_reforged"), RefStrings.MODID + ":meteorite_sword");
		meteorite_sword_hardened = NativeItemTextureRegistry.texture(new ItemSwordMeteorite(15F, 0, matMeteorite).setTranslationKey("meteorite_sword_hardened"), RefStrings.MODID + ":meteorite_sword");
		meteorite_sword_alloyed = NativeItemTextureRegistry.texture(new ItemSwordMeteorite(17.5F, 0, matMeteorite).setTranslationKey("meteorite_sword_alloyed"), RefStrings.MODID + ":meteorite_sword");
		meteorite_sword_machined = NativeItemTextureRegistry.texture(new ItemSwordMeteorite(20F, 0, matMeteorite).setTranslationKey("meteorite_sword_machined"), RefStrings.MODID + ":meteorite_sword");
		meteorite_sword_treated = NativeItemTextureRegistry.texture(new ItemSwordMeteorite(22.5F, 0, matMeteorite).setTranslationKey("meteorite_sword_treated"), RefStrings.MODID + ":meteorite_sword");
		meteorite_sword_etched = NativeItemTextureRegistry.texture(new ItemSwordMeteorite(25F, 0, matMeteorite).setTranslationKey("meteorite_sword_etched"), RefStrings.MODID + ":meteorite_sword");
		meteorite_sword_bred = NativeItemTextureRegistry.texture(new ItemSwordMeteorite(30F, 0, matMeteorite).setTranslationKey("meteorite_sword_bred"), RefStrings.MODID + ":meteorite_sword");
		meteorite_sword_irradiated = NativeItemTextureRegistry.texture(new ItemSwordMeteorite(35F, 0, matMeteorite).setTranslationKey("meteorite_sword_irradiated"), RefStrings.MODID + ":meteorite_sword");
		meteorite_sword_fused = NativeItemTextureRegistry.texture(new ItemSwordMeteorite(50F, 0, matMeteorite).setTranslationKey("meteorite_sword_fused"), RefStrings.MODID + ":meteorite_sword");
		meteorite_sword_baleful = NativeItemTextureRegistry.texture(new ItemSwordMeteorite(75F, 0, matMeteorite).setTranslationKey("meteorite_sword_baleful"), RefStrings.MODID + ":meteorite_sword");

		mask_of_infamy = NativeItemTextureRegistry.texture(new MaskOfInfamy(ArmorMaterial.IRON, 0).setTranslationKey("mask_of_infamy").setMaxStackSize(1), RefStrings.MODID + ":mask_of_infamy");

		hazmat_helmet = NativeItemTextureRegistry.texture(new ArmorHazmatMask(MainRegistry.aMatHaz, 0, RefStrings.MODID + ":textures/armor/hazmat_1.png").setTranslationKey("hazmat_helmet"), RefStrings.MODID + ":hazmat_helmet");
		hazmat_plate = NativeItemTextureRegistry.texture(new ArmorHazmat(MainRegistry.aMatHaz, 1, RefStrings.MODID + ":textures/armor/hazmat_1.png").setTranslationKey("hazmat_plate"), RefStrings.MODID + ":hazmat_plate");
		hazmat_legs = NativeItemTextureRegistry.texture(new ArmorHazmat(MainRegistry.aMatHaz, 2, RefStrings.MODID + ":textures/armor/hazmat_2.png").setTranslationKey("hazmat_legs"), RefStrings.MODID + ":hazmat_legs");
		hazmat_boots = NativeItemTextureRegistry.texture(new ArmorHazmat(MainRegistry.aMatHaz, 3, RefStrings.MODID + ":textures/armor/hazmat_1.png").setTranslationKey("hazmat_boots"), RefStrings.MODID + ":hazmat_boots");
		hazmat_helmet_red = NativeItemTextureRegistry.texture(new ArmorHazmatMask(MainRegistry.aMatHaz2, 0, "hbm:textures/models/ModelHazRed.png").setTranslationKey("hazmat_helmet_red"), RefStrings.MODID + ":hazmat_helmet_red");
		hazmat_plate_red = NativeItemTextureRegistry.texture(new ArmorHazmat(MainRegistry.aMatHaz2, 1, RefStrings.MODID + ":textures/armor/hazmat_1_red.png").setTranslationKey("hazmat_plate_red"), RefStrings.MODID + ":hazmat_plate_red");
		hazmat_legs_red = NativeItemTextureRegistry.texture(new ArmorHazmat(MainRegistry.aMatHaz2, 2, RefStrings.MODID + ":textures/armor/hazmat_2_red.png").setTranslationKey("hazmat_legs_red"), RefStrings.MODID + ":hazmat_legs_red");
		hazmat_boots_red = NativeItemTextureRegistry.texture(new ArmorHazmat(MainRegistry.aMatHaz2, 3, RefStrings.MODID + ":textures/armor/hazmat_1_red.png").setTranslationKey("hazmat_boots_red"), RefStrings.MODID + ":hazmat_boots_red");
		hazmat_helmet_grey = NativeItemTextureRegistry.texture(new ArmorHazmatMask(MainRegistry.aMatHaz3, 0, "hbm:textures/models/ModelHazGrey.png")
				.setTranslationKey("hazmat_helmet_grey"), RefStrings.MODID + ":hazmat_helmet_grey");
		hazmat_plate_grey = NativeItemTextureRegistry.texture(new ArmorHazmat(MainRegistry.aMatHaz3, 1, RefStrings.MODID + ":textures/armor/hazmat_1_grey.png").cloneStats((ArmorFSB) hazmat_helmet_grey).setTranslationKey("hazmat_plate_grey"), RefStrings.MODID + ":hazmat_plate_grey");
		hazmat_legs_grey = NativeItemTextureRegistry.texture(new ArmorHazmat(MainRegistry.aMatHaz3, 2, RefStrings.MODID + ":textures/armor/hazmat_2_grey.png").cloneStats((ArmorFSB) hazmat_helmet_grey).setTranslationKey("hazmat_legs_grey"), RefStrings.MODID + ":hazmat_legs_grey");
		hazmat_boots_grey = NativeItemTextureRegistry.texture(new ArmorHazmat(MainRegistry.aMatHaz3, 3, RefStrings.MODID + ":textures/armor/hazmat_1_grey.png").cloneStats((ArmorFSB) hazmat_helmet_grey).setTranslationKey("hazmat_boots_grey"), RefStrings.MODID + ":hazmat_boots_grey");
		hazmat_paa_helmet = NativeItemTextureRegistry.texture(new ArmorHazmatMask(MainRegistry.aMatPaa, 0, RefStrings.MODID + ":textures/armor/hazmat_paa_1.png")
				.setTranslationKey("hazmat_paa_helmet"), RefStrings.MODID + ":hazmat_paa_helmet");
		hazmat_paa_plate = NativeItemTextureRegistry.texture(new ArmorHazmat(MainRegistry.aMatPaa, 1, RefStrings.MODID + ":textures/armor/hazmat_paa_1.png").cloneStats((ArmorFSB) hazmat_paa_helmet).setTranslationKey("hazmat_paa_plate"), RefStrings.MODID + ":hazmat_paa_plate");
		hazmat_paa_legs = NativeItemTextureRegistry.texture(new ArmorHazmat(MainRegistry.aMatPaa, 2, RefStrings.MODID + ":textures/armor/hazmat_paa_2.png").cloneStats((ArmorFSB) hazmat_paa_helmet).setTranslationKey("hazmat_paa_legs"), RefStrings.MODID + ":hazmat_paa_legs");
		hazmat_paa_boots = NativeItemTextureRegistry.texture(new ArmorHazmat(MainRegistry.aMatPaa, 3, RefStrings.MODID + ":textures/armor/hazmat_paa_1.png").cloneStats((ArmorFSB) hazmat_paa_helmet).setTranslationKey("hazmat_paa_boots"), RefStrings.MODID + ":hazmat_paa_boots");

		ArmorMaterial aMatLiquidator = EnumHelper.addArmorMaterial("HBM_LIQUIDATOR", "leather", 750, new int[] { 3, 6, 8, 3 }, 10, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
		aMatLiquidator.repairMaterial = new ItemStack(ModItems.plate_lead, 1, OreDictionary.WILDCARD_VALUE);
		liquidator_helmet = NativeItemTextureRegistry.texture(new ArmorLiquidatorMask(aMatLiquidator, 0, RefStrings.MODID + ":textures/armor/liquidator_helmet.png")
				.setStep("hbm:step.metal")
				.setJump("hbm:step.iron_jump")
				.setFall("hbm:step.iron_land").setTranslationKey("liquidator_helmet").setMaxStackSize(1), RefStrings.MODID + ":liquidator_helmet");
		liquidator_plate = NativeItemTextureRegistry.texture(new ArmorLiquidator(aMatLiquidator, 1, RefStrings.MODID + ":textures/armor/liquidator_1.png").cloneStats((ArmorFSB) liquidator_helmet).setTranslationKey("liquidator_plate").setMaxStackSize(1), RefStrings.MODID + ":liquidator_plate");
		liquidator_legs = NativeItemTextureRegistry.texture(new ArmorLiquidator(aMatLiquidator, 2, RefStrings.MODID + ":textures/armor/liquidator_2.png").cloneStats((ArmorFSB) liquidator_helmet).setTranslationKey("liquidator_legs").setMaxStackSize(1), RefStrings.MODID + ":liquidator_legs");
		liquidator_boots = NativeItemTextureRegistry.texture(new ArmorLiquidator(aMatLiquidator, 3, RefStrings.MODID + ":textures/armor/liquidator_1.png").cloneStats((ArmorFSB) liquidator_helmet).setTranslationKey("liquidator_boots").setMaxStackSize(1), RefStrings.MODID + ":liquidator_boots");

		jetpack_boost = NativeItemTextureRegistry.texture(new JetpackBooster(Fluids.BALEFIRE, 32000).setTranslationKey("jetpack_boost").setCreativeTab(CreativeTabs.COMBAT).setMaxStackSize(1), RefStrings.MODID + ":jetpack_boost");
		jetpack_break = NativeItemTextureRegistry.texture(new JetpackBreak(Fluids.KEROSENE, 12000).setTranslationKey("jetpack_break").setCreativeTab(CreativeTabs.COMBAT).setMaxStackSize(1), RefStrings.MODID + ":jetpack_break");
		jetpack_fly = NativeItemTextureRegistry.texture(new JetpackRegular(Fluids.KEROSENE, 12000).setTranslationKey("jetpack_fly").setCreativeTab(CreativeTabs.COMBAT).setMaxStackSize(1), RefStrings.MODID + ":jetpack_fly");
		jetpack_vector = NativeItemTextureRegistry.texture(new JetpackVectorized(Fluids.KEROSENE, 16000).setTranslationKey("jetpack_vector").setCreativeTab(CreativeTabs.COMBAT).setMaxStackSize(1), RefStrings.MODID + ":jetpack_vector");
		wings_murk = NativeItemTextureRegistry.texture(new WingsMurk().setTranslationKey("wings_murk").setCreativeTab(CreativeTabs.COMBAT).setMaxStackSize(1), RefStrings.MODID + ":wings_murk");
		wings_limp = NativeItemTextureRegistry.texture(new WingsMurk().setTranslationKey("wings_limp").setCreativeTab(CreativeTabs.COMBAT).setMaxStackSize(1), RefStrings.MODID + ":wings_limp");
		oxy_plss = NativeItemTextureRegistry.texture(new ItemModOxy(16000, 10, 1).setTranslationKey("oxy_plss").setCreativeTab(MainRegistry.consumableTab).setMaxStackSize(1), RefStrings.MODID + ":oxy_plss");

		cape_radiation = NativeItemTextureRegistry.texture(new ArmorModel(ArmorMaterial.CHAIN, 1).setTranslationKey("cape_radiation").setCreativeTab(MainRegistry.consumableTab).setMaxStackSize(1), RefStrings.MODID + ":cape_radiation");
		cape_gasmask = NativeItemTextureRegistry.texture(new ArmorModel(ArmorMaterial.CHAIN, 1).setTranslationKey("cape_gasmask").setCreativeTab(MainRegistry.consumableTab).setMaxStackSize(1), RefStrings.MODID + ":cape_gasmask");
		cape_schrabidium = NativeItemTextureRegistry.texture(new ArmorModel(MainRegistry.aMatSchrab, 1).setTranslationKey("cape_schrabidium").setCreativeTab(MainRegistry.consumableTab).setMaxStackSize(1), RefStrings.MODID + ":cape_schrabidium");
		cape_hidden = NativeItemTextureRegistry.texture(new ArmorModel(ArmorMaterial.CHAIN, 1).setTranslationKey("cape_hidden").setCreativeTab(MainRegistry.consumableTab).setMaxStackSize(1), RefStrings.MODID + ":cape_unknown");

		schrabidium_hammer = NativeItemTextureRegistry.texture(new WeaponSpecial(MainRegistry.tMatHammmer).setTranslationKey("schrabidium_hammer").setMaxStackSize(1), RefStrings.MODID + ":schrabidium_hammer");
		shimmer_sledge = NativeItemTextureRegistry.texture(new WeaponSpecial(MainRegistry.enumToolMaterialSledge).setTranslationKey("shimmer_sledge").setMaxStackSize(1), RefStrings.MODID + ":shimmer_sledge_original");
		shimmer_axe = NativeItemTextureRegistry.texture(new WeaponSpecial(MainRegistry.enumToolMaterialSledge).setTranslationKey("shimmer_axe").setMaxStackSize(1), RefStrings.MODID + ":shimmer_axe");
		bottle_opener = NativeItemTextureRegistry.texture(new WeaponSpecial(MainRegistry.enumToolMaterialBottleOpener).setTranslationKey("bottle_opener").setCreativeTab(MainRegistry.consumableTab).setMaxStackSize(1), RefStrings.MODID + ":bottle_opener");
		pch = NativeItemTextureRegistry.texture(new WeaponSpecial(MainRegistry.tMatHammmer).setTranslationKey("pch").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":schrabidium_hammer");
		matchstick = NativeItemTextureRegistry.texture(new ItemMatch().setTranslationKey("matchstick").setCreativeTab(CreativeTabs.TOOLS).setFull3D(), RefStrings.MODID + ":matchstick");
		balefire_and_steel = NativeItemTextureRegistry.texture(new ItemBalefireMatch().setTranslationKey("balefire_and_steel").setCreativeTab(CreativeTabs.TOOLS).setFull3D(), RefStrings.MODID + ":balefire_and_steel");
		crowbar = NativeItemTextureRegistry.texture(new ModSword(MainRegistry.tMatSteel).setTranslationKey("crowbar").setFull3D(), RefStrings.MODID + ":crowbar");
		wrench_flipped = NativeItemTextureRegistry.texture(new WeaponSpecial(MainRegistry.tMatElec).setTranslationKey("wrench_flipped").setMaxStackSize(1), RefStrings.MODID + ":wrench_flipped");
		memespoon = NativeItemTextureRegistry.texture(new WeaponSpecial(MainRegistry.tMatSteel).setTranslationKey("memespoon").setMaxStackSize(1), RefStrings.MODID + ":memespoon");
		wood_gavel = NativeItemTextureRegistry.texture(new WeaponSpecial(ToolMaterial.WOOD).setTranslationKey("wood_gavel").setMaxStackSize(1), RefStrings.MODID + ":wood_gavel");
		lead_gavel = NativeItemTextureRegistry.texture(new WeaponSpecial(MainRegistry.tMatSteel).setTranslationKey("lead_gavel").setMaxStackSize(1), RefStrings.MODID + ":lead_gavel");
		diamond_gavel = NativeItemTextureRegistry.texture(new WeaponSpecial(ToolMaterial.DIAMOND).setTranslationKey("diamond_gavel").setMaxStackSize(1), RefStrings.MODID + ":diamond_gavel");
		ToolMaterial matMeseGavel = EnumHelper.addToolMaterial("HBM_MESEGAVEL", 4, 0, 50F, 0.0F, 200).setRepairItem(new ItemStack(ModItems.plate_paa));
		mese_gavel = NativeItemTextureRegistry.texture(new ItemSwordAbility(250, 1.5, matMeseGavel)
				.addAbility(IWeaponAbility.PHOSPHORUS, 0)
				.addAbility(IWeaponAbility.RADIATION, 2)
				.addAbility(IWeaponAbility.STUN, 3)
				.addAbility(IWeaponAbility.VAMPIRE, 4)
				.addAbility(IWeaponAbility.BEHEADER, 0).setTranslationKey("mese_gavel").setMaxStackSize(1), RefStrings.MODID + ":mese_gavel");

		saw = NativeItemTextureRegistry.texture(new ModSword(MainRegistry.enumToolMaterialSaw).setTranslationKey("weapon_saw").setCreativeTab(null).setFull3D(), RefStrings.MODID + ":saw");
		bat = NativeItemTextureRegistry.texture(new ModSword(MainRegistry.enumToolMaterialBat).setTranslationKey("weapon_bat").setCreativeTab(null).setFull3D(), RefStrings.MODID + ":bat");
		bat_nail = NativeItemTextureRegistry.texture(new ModSword(MainRegistry.enumToolMaterialBatNail).setTranslationKey("weapon_bat_nail").setCreativeTab(null).setFull3D(), RefStrings.MODID + ":bat_nail");
		golf_club = NativeItemTextureRegistry.texture(new ModSword(MainRegistry.enumToolMaterialGolfClub).setTranslationKey("weapon_golf_club").setCreativeTab(null).setFull3D(), RefStrings.MODID + ":golf_club");
		pipe_rusty = NativeItemTextureRegistry.texture(new ModSword(MainRegistry.enumToolMaterialPipeRusty).setTranslationKey("weapon_pipe_rusty").setCreativeTab(null).setFull3D(), RefStrings.MODID + ":pipe_rusty");
		pipe_lead = NativeItemTextureRegistry.texture(new ModSword(MainRegistry.enumToolMaterialPipeLead).setTranslationKey("weapon_pipe_lead").setFull3D(), RefStrings.MODID + ":pipe_lead");
		reer_graar = NativeItemTextureRegistry.texture(new ModSword(MainRegistry.tMatTitan).setTranslationKey("reer_graar").setFull3D(), RefStrings.MODID + ":reer_graar_hd");
		stopsign = NativeItemTextureRegistry.texture(new WeaponSpecial(MainRegistry.tMatAlloy).setTranslationKey("stopsign"), RefStrings.MODID + ":stopsign");
		sopsign = NativeItemTextureRegistry.texture(new WeaponSpecial(MainRegistry.tMatAlloy).setTranslationKey("sopsign"), RefStrings.MODID + ":sopsign");
		chernobylsign = NativeItemTextureRegistry.texture(new WeaponSpecial(MainRegistry.tMatAlloy).setTranslationKey("chernobylsign"), RefStrings.MODID + ":chernobylsign");

		crystal_horn = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("crystal_horn").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_horn");
		crystal_charred = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("crystal_charred").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":crystal_charred");

		bucket_mud = NativeItemTextureRegistry.texture(new ItemModBucket(ModBlocks.mud_block, ModBlocks.mud_fluid).setTranslationKey("bucket_mud").setContainerItem(Items.BUCKET).setCreativeTab(MainRegistry.blockTab), RefStrings.MODID + ":bucket_mud");
		bucket_acid = NativeItemTextureRegistry.texture(new ItemModBucket(ModBlocks.acid_block, ModBlocks.acid_fluid).setTranslationKey("bucket_acid").setContainerItem(Items.BUCKET).setCreativeTab(MainRegistry.blockTab), RefStrings.MODID + ":bucket_acid");
		bucket_toxic = NativeItemTextureRegistry.texture(new ItemModBucket(ModBlocks.toxic_block, ModBlocks.toxic_fluid).setTranslationKey("bucket_toxic").setContainerItem(Items.BUCKET).setCreativeTab(MainRegistry.blockTab), RefStrings.MODID + ":bucket_toxic");
		bucket_schrabidic_acid = NativeItemTextureRegistry.texture(new ItemModBucket(ModBlocks.schrabidic_block, ModBlocks.schrabidic_fluid).setTranslationKey("bucket_schrabidic_acid").setContainerItem(Items.BUCKET).setCreativeTab(MainRegistry.blockTab), RefStrings.MODID + ":bucket_schrabidic_acid");
		bucket_sulfuric_acid = NativeItemTextureRegistry.texture(new ItemModBucket(ModBlocks.sulfuric_acid_block, ModBlocks.sulfuric_acid_fluid).setTranslationKey("bucket_sulfuric_acid").setContainerItem(Items.BUCKET).setCreativeTab(MainRegistry.blockTab), RefStrings.MODID + ":bucket_sulfuric_acid");
		bucket_mercury = NativeItemTextureRegistry.texture(new ItemModBucket(ModBlocks.mercury_block, ModBlocks.mercury_fluid).setTranslationKey("bucket_mercury").setContainerItem(Items.BUCKET).setCreativeTab(MainRegistry.blockTab), RefStrings.MODID + ":bucket_mercury");
		bucket_bromine = NativeItemTextureRegistry.texture(new ItemModBucket(ModBlocks.bromine_block, ModBlocks.bromine_fluid).setTranslationKey("bucket_bromine").setContainerItem(Items.BUCKET).setCreativeTab(MainRegistry.blockTab), RefStrings.MODID + ":bucket_bromine");
		bucket_ccl = NativeItemTextureRegistry.texture(new ItemModBucket(ModBlocks.ccl_block, ModBlocks.ccl_fluid).setTranslationKey("bucket_ccl").setContainerItem(Items.BUCKET).setCreativeTab(MainRegistry.blockTab), RefStrings.MODID + ":bucket_ccl");

		door_metal = NativeItemTextureRegistry.texture(new ItemModDoor().setTranslationKey("door_metal").setCreativeTab(MainRegistry.blockTab), RefStrings.MODID + ":door_metal");
		door_office = NativeItemTextureRegistry.texture(new ItemModDoor().setTranslationKey("door_office").setCreativeTab(MainRegistry.blockTab), RefStrings.MODID + ":door_office");
		door_bunker = NativeItemTextureRegistry.texture(new ItemModDoor().setTranslationKey("door_bunker").setCreativeTab(MainRegistry.blockTab), RefStrings.MODID + ":door_bunker");
		door_red = NativeItemTextureRegistry.texture(new ItemModDoor().setTranslationKey("door_red").setCreativeTab(null), RefStrings.MODID + ":door_red");

		fence_gate = NativeItemTextureRegistry.texture(new ItemModDoor().setTranslationKey("fence_gate").setCreativeTab(MainRegistry.blockTab), RefStrings.MODID + ":fence_metal");

		record_lc = NativeItemTextureRegistry.texture(new ItemModRecord("lc").setTranslationKey("record_lc").setCreativeTab(CreativeTabs.MISC), RefStrings.MODID + ":record_lc");
		record_ss = NativeItemTextureRegistry.texture(new ItemModRecord("ss").setTranslationKey("record_ss").setCreativeTab(CreativeTabs.MISC), RefStrings.MODID + ":record_ss");
		record_vc = NativeItemTextureRegistry.texture(new ItemModRecord("vc").setTranslationKey("record_vc").setCreativeTab(CreativeTabs.MISC), RefStrings.MODID + ":record_vc");
		record_gs = NativeItemTextureRegistry.texture(new ItemModRecord("gs").setTranslationKey("record_gs").setCreativeTab(CreativeTabs.MISC), RefStrings.MODID + ":record_gs");
		record_gp = NativeItemTextureRegistry.texture(new ItemModRecord("gp").setTranslationKey("record_gp").setCreativeTab(CreativeTabs.MISC), RefStrings.MODID + ":record_gp");
		record_el = NativeItemTextureRegistry.texture(new ItemModRecord("el").setTranslationKey("record_el").setCreativeTab(CreativeTabs.MISC), RefStrings.MODID + ":record_el");
		record_glass = NativeItemTextureRegistry.texture(new ItemModRecord("glass").setTranslationKey("record_glass").setCreativeTab(null), RefStrings.MODID + ":record_glass");

		book_guide = NativeItemTextureRegistry.texture(new ItemGuideBook().setTranslationKey("book_guide").setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":book_guide");
		book_lore = NativeItemTextureRegistry.texture(new ItemBookLore().setTranslationKey("book_lore").setCreativeTab(null), RefStrings.MODID + ":book_pages");
		holotape_image = NativeItemTextureRegistry.texture(new ItemHolotapeImage().setTranslationKey("holotape_image").setCreativeTab(null), RefStrings.MODID + ":holotape");
		holotape_damaged = NativeItemTextureRegistry.texture(new Item().setTranslationKey("holotape_damaged").setCreativeTab(null), RefStrings.MODID + ":holotape_damaged");
		clay_tablet = NativeItemTextureRegistry.texture(new ItemClayTablet().setTranslationKey("clay_tablet").setCreativeTab(null), RefStrings.MODID + ":clay_tablet");

		polaroid = NativeItemTextureRegistry.texture(new ItemPolaroid().setTranslationKey("polaroid").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":polaroid_" + MainRegistry.polaroidID);
		glitch = NativeItemTextureRegistry.texture(new ItemGlitch().setTranslationKey("glitch").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab), RefStrings.MODID + ":glitch_" + MainRegistry.polaroidID);
		book_secret = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("book_secret").setCreativeTab(MainRegistry.polaroidID == 11 ? MainRegistry.consumableTab : null), RefStrings.MODID + ":book_secret");
		book_of_ = NativeItemTextureRegistry.texture(new ItemBook().setTranslationKey("book_of_").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":book_of_");
		page_of_ = NativeItemTextureRegistry.texture(new ItemEnumMulti(ItemEnums.EnumPages.class, true, false).setTranslationKey("page_of_").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":page_of_");
		book_lemegeton = NativeItemTextureRegistry.texture(new ItemBookLemegeton().setTranslationKey("book_lemegeton").setMaxStackSize(1).setCreativeTab(null), RefStrings.MODID + ":book_lemegeton");
		burnt_bark = NativeItemTextureRegistry.texture(new ItemCustomLore().setTranslationKey("burnt_bark").setCreativeTab(null), RefStrings.MODID + ":burnt_bark");

		chlorine1 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("chlorine1"), RefStrings.MODID + ":chlorine1");
		chlorine2 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("chlorine2"), RefStrings.MODID + ":chlorine2");
		chlorine3 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("chlorine3"), RefStrings.MODID + ":chlorine3");
		chlorine4 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("chlorine4"), RefStrings.MODID + ":chlorine4");
		chlorine5 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("chlorine5"), RefStrings.MODID + ":chlorine5");
		chlorine6 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("chlorine6"), RefStrings.MODID + ":chlorine6");
		chlorine7 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("chlorine7"), RefStrings.MODID + ":chlorine7");
		chlorine8 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("chlorine8"), RefStrings.MODID + ":chlorine8");
		pc1 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("pc1"), RefStrings.MODID + ":pc1");
		pc2 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("pc2"), RefStrings.MODID + ":pc2");
		pc3 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("pc3"), RefStrings.MODID + ":pc3");
		pc4 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("pc4"), RefStrings.MODID + ":pc4");
		pc5 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("pc5"), RefStrings.MODID + ":pc5");
		pc6 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("pc6"), RefStrings.MODID + ":pc6");
		pc7 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("pc7"), RefStrings.MODID + ":pc7");
		pc8 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("pc8"), RefStrings.MODID + ":pc8");
		cloud1 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("cloud1"), RefStrings.MODID + ":cloud1");
		cloud2 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("cloud2"), RefStrings.MODID + ":cloud2");
		cloud3 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("cloud3"), RefStrings.MODID + ":cloud3");
		cloud4 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("cloud4"), RefStrings.MODID + ":cloud4");
		cloud5 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("cloud5"), RefStrings.MODID + ":cloud5");
		cloud6 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("cloud6"), RefStrings.MODID + ":cloud6");
		cloud7 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("cloud7"), RefStrings.MODID + ":cloud7");
		cloud8 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("cloud8"), RefStrings.MODID + ":cloud8");
		orange1 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("orange1"), RefStrings.MODID + ":orange1");
		orange2 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("orange2"), RefStrings.MODID + ":orange2");
		orange3 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("orange3"), RefStrings.MODID + ":orange3");
		orange4 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("orange4"), RefStrings.MODID + ":orange4");
		orange5 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("orange5"), RefStrings.MODID + ":orange5");
		orange6 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("orange6"), RefStrings.MODID + ":orange6");
		orange7 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("orange7"), RefStrings.MODID + ":orange7");
		orange8 = NativeItemTextureRegistry.texture(new Item().setTranslationKey("orange8"), RefStrings.MODID + ":orange8");

		nothing = NativeItemTextureRegistry.texture(new Item().setTranslationKey("nothing"), RefStrings.MODID + ":nothing");
		broken_item = NativeItemTextureRegistry.texture(new BrokenItem().setTranslationKey("broken_item"), RefStrings.MODID + ":broken_item");

		achievement_icon = new ItemEnumMulti(ItemEnums.EnumAchievementType.class, true, true).setTranslationKey("achievement_icon");

		mysteryshovel = NativeItemTextureRegistry.texture(new ItemMS().setTranslationKey("mysteryshovel").setFull3D().setMaxStackSize(1), RefStrings.MODID + ":cursed_shovel");
		memory = NativeItemTextureRegistry.texture(new ItemBattery(Long.MAX_VALUE / 100L, 100000000000000L, 100000000000000L).setTranslationKey("memory").setMaxStackSize(1), RefStrings.MODID + ":mo8_anim");

		conveyor_wand = new ItemConveyorWand().setTranslationKey("conveyor_wand").setCreativeTab(MainRegistry.machineTab).setFull3D();

		swarm_member = NativeItemTextureRegistry.texture(new Item().setTranslationKey("swarm_member").setCreativeTab(MainRegistry.partsTab), RefStrings.MODID + ":sat_base");

		GunFactory.init();

		BucketHandler.INSTANCE.buckets.put(ModBlocks.mud_block, ModItems.bucket_mud);
		BucketHandler.INSTANCE.buckets.put(ModBlocks.acid_block, ModItems.bucket_acid);
		BucketHandler.INSTANCE.buckets.put(ModBlocks.toxic_block, ModItems.bucket_toxic);
		BucketHandler.INSTANCE.buckets.put(ModBlocks.schrabidic_block, ModItems.bucket_schrabidic_acid);
		BucketHandler.INSTANCE.buckets.put(ModBlocks.sulfuric_acid_block, ModItems.bucket_sulfuric_acid);
		BucketHandler.INSTANCE.buckets.put(ModBlocks.mercury_block, ModItems.bucket_mercury);
		BucketHandler.INSTANCE.buckets.put(ModBlocks.bromine_block, ModItems.bucket_bromine);
		BucketHandler.INSTANCE.buckets.put(ModBlocks.ccl_block, ModItems.bucket_ccl);

		MinecraftForge.EVENT_BUS.register(BucketHandler.INSTANCE);
	}

	private static void registerItem() {

		excludeNEI.add(item_secret);

		//Weapons
		GameRegistry.registerItem(redstone_sword, redstone_sword.getTranslationKey());
		GameRegistry.registerItem(big_sword, big_sword.getTranslationKey());
		GameRegistry.registerItem(butter_sword, butter_sword.getTranslationKey());

		//Ingots
		GameRegistry.registerItem(ingot_uranium, ingot_uranium.getTranslationKey());
		GameRegistry.registerItem(ingot_u233, ingot_u233.getTranslationKey());
		GameRegistry.registerItem(ingot_u235, ingot_u235.getTranslationKey());
		GameRegistry.registerItem(ingot_u238, ingot_u238.getTranslationKey());
		GameRegistry.registerItem(ingot_u238m2, ingot_u238m2.getTranslationKey());
		GameRegistry.registerItem(ingot_th232, ingot_th232.getTranslationKey());
		GameRegistry.registerItem(ingot_plutonium, ingot_plutonium.getTranslationKey());
		GameRegistry.registerItem(ingot_pu238, ingot_pu238.getTranslationKey());
		GameRegistry.registerItem(ingot_pu239, ingot_pu239.getTranslationKey());
		GameRegistry.registerItem(ingot_pu240, ingot_pu240.getTranslationKey());
		GameRegistry.registerItem(ingot_pu241, ingot_pu241.getTranslationKey());
		GameRegistry.registerItem(ingot_pu_mix, ingot_pu_mix.getTranslationKey());
		GameRegistry.registerItem(ingot_am241, ingot_am241.getTranslationKey());
		GameRegistry.registerItem(ingot_am242, ingot_am242.getTranslationKey());
		GameRegistry.registerItem(ingot_am_mix, ingot_am_mix.getTranslationKey());
		GameRegistry.registerItem(ingot_cm242, ingot_cm242.getTranslationKey());
		GameRegistry.registerItem(ingot_cm243, ingot_cm243.getTranslationKey());
		GameRegistry.registerItem(ingot_cm244, ingot_cm244.getTranslationKey());
		GameRegistry.registerItem(ingot_cm245, ingot_cm245.getTranslationKey());
		GameRegistry.registerItem(ingot_cm246, ingot_cm246.getTranslationKey());
		GameRegistry.registerItem(ingot_cm247, ingot_cm247.getTranslationKey());
		GameRegistry.registerItem(ingot_cm_fuel, ingot_cm_fuel.getTranslationKey());
		GameRegistry.registerItem(ingot_cm_mix, ingot_cm_mix.getTranslationKey());
		GameRegistry.registerItem(ingot_bk247, ingot_bk247.getTranslationKey());
		GameRegistry.registerItem(ingot_cf251, ingot_cf251.getTranslationKey());
		GameRegistry.registerItem(ingot_cf252, ingot_cf252.getTranslationKey());
		GameRegistry.registerItem(ingot_es253, ingot_es253.getTranslationKey());
		GameRegistry.registerItem(ingot_es255, ingot_es255.getTranslationKey());
		GameRegistry.registerItem(ingot_neptunium, ingot_neptunium.getTranslationKey());
		GameRegistry.registerItem(ingot_polonium, ingot_polonium.getTranslationKey());
		GameRegistry.registerItem(ingot_technetium, ingot_technetium.getTranslationKey());
		GameRegistry.registerItem(ingot_co60, ingot_co60.getTranslationKey());
		GameRegistry.registerItem(ingot_sr90, ingot_sr90.getTranslationKey());
		GameRegistry.registerItem(ingot_au198, ingot_au198.getTranslationKey());
		GameRegistry.registerItem(ingot_pb209, ingot_pb209.getTranslationKey());
		GameRegistry.registerItem(ingot_ra226, ingot_ra226.getTranslationKey());
		GameRegistry.registerItem(ingot_titanium, ingot_titanium.getTranslationKey());
		GameRegistry.registerItem(ingot_nickel, ingot_nickel.getTranslationKey());
		GameRegistry.registerItem(ingot_copper, ingot_copper.getTranslationKey());
		GameRegistry.registerItem(ingot_red_copper, ingot_red_copper.getTranslationKey());
		GameRegistry.registerItem(billet_red_copper, billet_red_copper.getTranslationKey());
		GameRegistry.registerItem(ingot_advanced_alloy, ingot_advanced_alloy.getTranslationKey());
		GameRegistry.registerItem(ingot_zinc, ingot_zinc.getTranslationKey());
		GameRegistry.registerItem(ingot_tungsten, ingot_tungsten.getTranslationKey());
		GameRegistry.registerItem(ingot_gallium, ingot_gallium.getTranslationKey());
		GameRegistry.registerItem(ingot_gaas, ingot_gaas.getTranslationKey());
		GameRegistry.registerItem(ingot_tt, ingot_tt.getTranslationKey());
		GameRegistry.registerItem(ingot_ttas, ingot_ttas.getTranslationKey());
		GameRegistry.registerItem(ingot_iridium, ingot_iridium.getTranslationKey());
		GameRegistry.registerItem(ingot_palladium, ingot_palladium.getTranslationKey());
		GameRegistry.registerItem(ingot_aluminium, ingot_aluminium.getTranslationKey());
		GameRegistry.registerItem(ingot_steel, ingot_steel.getTranslationKey());
		GameRegistry.registerItem(ingot_stainless, ingot_stainless.getTranslationKey());
		GameRegistry.registerItem(ingot_tcalloy, ingot_tcalloy.getTranslationKey());
		GameRegistry.registerItem(ingot_cdalloy, ingot_cdalloy.getTranslationKey());
		GameRegistry.registerItem(ingot_bismuth_bronze, ingot_bismuth_bronze.getTranslationKey());
		GameRegistry.registerItem(ingot_arsenic_bronze, ingot_arsenic_bronze.getTranslationKey());
		GameRegistry.registerItem(ingot_bscco, ingot_bscco.getTranslationKey());
		GameRegistry.registerItem(ingot_menthol, ingot_menthol.getTranslationKey());
		GameRegistry.registerItem(ingot_lead, ingot_lead.getTranslationKey());
		GameRegistry.registerItem(ingot_bismuth, ingot_bismuth.getTranslationKey());
		GameRegistry.registerItem(ingot_arsenic, ingot_arsenic.getTranslationKey());
		GameRegistry.registerItem(ingot_calcium, ingot_calcium.getTranslationKey());
		GameRegistry.registerItem(ingot_cadmium, ingot_cadmium.getTranslationKey());
		GameRegistry.registerItem(ingot_tantalium, ingot_tantalium.getTranslationKey());
		GameRegistry.registerItem(ingot_silicon, ingot_silicon.getTranslationKey());
		GameRegistry.registerItem(ingot_niobium, ingot_niobium.getTranslationKey());
		GameRegistry.registerItem(ingot_beryllium, ingot_beryllium.getTranslationKey());
		GameRegistry.registerItem(ingot_cobalt, ingot_cobalt.getTranslationKey());
		GameRegistry.registerItem(ingot_boron, ingot_boron.getTranslationKey());
		GameRegistry.registerItem(ingot_graphite, ingot_graphite.getTranslationKey());
		GameRegistry.registerItem(ingot_firebrick, ingot_firebrick.getTranslationKey());
		GameRegistry.registerItem(ingot_dura_steel, ingot_dura_steel.getTranslationKey());
		GameRegistry.registerItem(ingot_polymer, ingot_polymer.getTranslationKey());
		GameRegistry.registerItem(ingot_bakelite, ingot_bakelite.getTranslationKey());
		GameRegistry.registerItem(ingot_biorubber, ingot_biorubber.getTranslationKey());
		GameRegistry.registerItem(ingot_rubber, ingot_rubber.getTranslationKey());
		GameRegistry.registerItem(ingot_pc, ingot_pc.getTranslationKey());
		GameRegistry.registerItem(ingot_pvc, ingot_pvc.getTranslationKey());
		GameRegistry.registerItem(ingot_mud, ingot_mud.getTranslationKey());
		GameRegistry.registerItem(ingot_magma, ingot_magma.getTranslationKey());
		GameRegistry.registerItem(ingot_cft, ingot_cft.getTranslationKey());
		GameRegistry.registerItem(ingot_schraranium, ingot_schraranium.getTranslationKey());
		GameRegistry.registerItem(ingot_schrabidium, ingot_schrabidium.getTranslationKey());
		GameRegistry.registerItem(ingot_schrabidate, ingot_schrabidate.getTranslationKey());
		GameRegistry.registerItem(ingot_magnetized_tungsten, ingot_magnetized_tungsten.getTranslationKey());
		GameRegistry.registerItem(ingot_combine_steel, ingot_combine_steel.getTranslationKey());
		GameRegistry.registerItem(ingot_solinium, ingot_solinium.getTranslationKey());
		GameRegistry.registerItem(ingot_gh336, ingot_gh336.getTranslationKey());
		GameRegistry.registerItem(ingot_cn989, ingot_cn989.getTranslationKey());
		GameRegistry.registerItem(ingot_uranium_fuel, ingot_uranium_fuel.getTranslationKey());
		GameRegistry.registerItem(ingot_thorium_fuel, ingot_thorium_fuel.getTranslationKey());
		GameRegistry.registerItem(ingot_plutonium_fuel, ingot_plutonium_fuel.getTranslationKey());
		GameRegistry.registerItem(ingot_neptunium_fuel, ingot_neptunium_fuel.getTranslationKey());
		GameRegistry.registerItem(ingot_mox_fuel, ingot_mox_fuel.getTranslationKey());
		GameRegistry.registerItem(ingot_americium_fuel, ingot_americium_fuel.getTranslationKey());
		GameRegistry.registerItem(ingot_schrabidium_fuel, ingot_schrabidium_fuel.getTranslationKey());
		GameRegistry.registerItem(ingot_hes, ingot_hes.getTranslationKey());
		GameRegistry.registerItem(ingot_les, ingot_les.getTranslationKey());
		GameRegistry.registerItem(ingot_australium, ingot_australium.getTranslationKey());
		GameRegistry.registerItem(ingot_australium_lesser, ingot_australium_lesser.getTranslationKey());
		GameRegistry.registerItem(ingot_australium_greater, ingot_australium_greater.getTranslationKey());
		GameRegistry.registerItem(ingot_lanthanium, ingot_lanthanium.getTranslationKey());
		GameRegistry.registerItem(ingot_actinium, ingot_actinium.getTranslationKey());
		GameRegistry.registerItem(ingot_desh, ingot_desh.getTranslationKey());
		GameRegistry.registerItem(ingot_ferrouranium, ingot_ferrouranium.getTranslationKey());
		GameRegistry.registerItem(ingot_starmetal, ingot_starmetal.getTranslationKey());
		GameRegistry.registerItem(ingot_gunmetal, ingot_gunmetal.getTranslationKey());
		GameRegistry.registerItem(ingot_weaponsteel, ingot_weaponsteel.getTranslationKey());
		GameRegistry.registerItem(ingot_saturnite, ingot_saturnite.getTranslationKey());
		GameRegistry.registerItem(ingot_euphemium, ingot_euphemium.getTranslationKey());
		GameRegistry.registerItem(ingot_dineutronium, ingot_dineutronium.getTranslationKey());
		GameRegistry.registerItem(ingot_electronium, ingot_electronium.getTranslationKey());
		GameRegistry.registerItem(ingot_gwenium, ingot_gwenium.getTranslationKey());
		GameRegistry.registerItem(ingot_smore, ingot_smore.getTranslationKey());
		GameRegistry.registerItem(ingot_osmiridium, ingot_osmiridium.getTranslationKey());
		GameRegistry.registerItem(ingot_hafnium, ingot_hafnium.getTranslationKey());


		//Meteorite Ingots
		GameRegistry.registerItem(ingot_steel_dusted, ingot_steel_dusted.getTranslationKey());
		GameRegistry.registerItem(ingot_chainsteel, ingot_chainsteel.getTranslationKey());
		GameRegistry.registerItem(ingot_meteorite, ingot_meteorite.getTranslationKey());
		GameRegistry.registerItem(ingot_meteorite_forged, ingot_meteorite_forged.getTranslationKey());
		GameRegistry.registerItem(blade_meteorite, blade_meteorite.getTranslationKey());

		//Misc Ingots
		GameRegistry.registerItem(ingot_phosphorus, ingot_phosphorus.getTranslationKey());
		GameRegistry.registerItem(lithium, lithium.getTranslationKey());
		GameRegistry.registerItem(ingot_zirconium, ingot_zirconium.getTranslationKey());
		GameRegistry.registerItem(ingot_semtex, ingot_semtex.getTranslationKey());
		GameRegistry.registerItem(ingot_c4, ingot_c4.getTranslationKey());
		GameRegistry.registerItem(oil_tar, oil_tar.getTranslationKey());
		GameRegistry.registerItem(solid_fuel, solid_fuel.getTranslationKey());
		GameRegistry.registerItem(solid_fuel_presto, solid_fuel_presto.getTranslationKey());
		GameRegistry.registerItem(solid_fuel_presto_triplet, solid_fuel_presto_triplet.getTranslationKey());
		GameRegistry.registerItem(solid_fuel_bf, solid_fuel_bf.getTranslationKey());
		GameRegistry.registerItem(solid_fuel_presto_bf, solid_fuel_presto_bf.getTranslationKey());
		GameRegistry.registerItem(solid_fuel_presto_triplet_bf, solid_fuel_presto_triplet_bf.getTranslationKey());
		GameRegistry.registerItem(rocket_fuel, rocket_fuel.getTranslationKey());
		GameRegistry.registerItem(ingot_fiberglass, ingot_fiberglass.getTranslationKey());
		GameRegistry.registerItem(ingot_asbestos, ingot_asbestos.getTranslationKey());
		GameRegistry.registerItem(ingot_raw, ingot_raw.getTranslationKey());

		//Billets
		GameRegistry.registerItem(billet_uranium, billet_uranium.getTranslationKey());
		GameRegistry.registerItem(billet_u233, billet_u233.getTranslationKey());
		GameRegistry.registerItem(billet_u235, billet_u235.getTranslationKey());
		GameRegistry.registerItem(billet_u238, billet_u238.getTranslationKey());
		GameRegistry.registerItem(billet_uzh, billet_uzh.getTranslationKey());
		GameRegistry.registerItem(billet_th232, billet_th232.getTranslationKey());
		GameRegistry.registerItem(billet_plutonium, billet_plutonium.getTranslationKey());
		GameRegistry.registerItem(billet_pu238, billet_pu238.getTranslationKey());
		GameRegistry.registerItem(billet_pu239, billet_pu239.getTranslationKey());
		GameRegistry.registerItem(billet_pu240, billet_pu240.getTranslationKey());
		GameRegistry.registerItem(billet_pu241, billet_pu241.getTranslationKey());
		GameRegistry.registerItem(billet_pu_mix, billet_pu_mix.getTranslationKey());
		GameRegistry.registerItem(billet_am241, billet_am241.getTranslationKey());
		GameRegistry.registerItem(billet_am242, billet_am242.getTranslationKey());
		GameRegistry.registerItem(billet_am_mix, billet_am_mix.getTranslationKey());
		GameRegistry.registerItem(billet_bk247, billet_bk247.getTranslationKey());
		GameRegistry.registerItem(billet_cm242, billet_cm242.getTranslationKey());
		GameRegistry.registerItem(billet_cm243, billet_cm243.getTranslationKey());
		GameRegistry.registerItem(billet_cm244, billet_cm244.getTranslationKey());
		GameRegistry.registerItem(billet_cm245, billet_cm245.getTranslationKey());
		GameRegistry.registerItem(billet_cm246, billet_cm246.getTranslationKey());
		GameRegistry.registerItem(billet_cm247, billet_cm247.getTranslationKey());
		GameRegistry.registerItem(billet_cm_fuel, billet_cm_fuel.getTranslationKey());
		GameRegistry.registerItem(billet_cm_mix, billet_cm_mix.getTranslationKey());
		GameRegistry.registerItem(billet_cf251, billet_cf251.getTranslationKey());
		GameRegistry.registerItem(billet_cf252, billet_cf252.getTranslationKey());
		GameRegistry.registerItem(billet_es253, billet_es253.getTranslationKey());
		GameRegistry.registerItem(billet_cn989, billet_cn989.getTranslationKey());
		GameRegistry.registerItem(billet_neptunium, billet_neptunium.getTranslationKey());
		GameRegistry.registerItem(billet_polonium, billet_polonium.getTranslationKey());
		GameRegistry.registerItem(billet_technetium, billet_technetium.getTranslationKey());
		GameRegistry.registerItem(billet_cobalt, billet_cobalt.getTranslationKey());
		GameRegistry.registerItem(billet_co60, billet_co60.getTranslationKey());
		GameRegistry.registerItem(billet_sr90, billet_sr90.getTranslationKey());
		GameRegistry.registerItem(billet_au198, billet_au198.getTranslationKey());
		GameRegistry.registerItem(billet_pb209, billet_pb209.getTranslationKey());
		GameRegistry.registerItem(billet_ra226, billet_ra226.getTranslationKey());
		GameRegistry.registerItem(billet_actinium, billet_actinium.getTranslationKey());
		GameRegistry.registerItem(billet_schrabidium, billet_schrabidium.getTranslationKey());
		GameRegistry.registerItem(billet_solinium, billet_solinium.getTranslationKey());
		GameRegistry.registerItem(billet_gh336, billet_gh336.getTranslationKey());
		GameRegistry.registerItem(billet_gaas, billet_gaas.getTranslationKey());
		GameRegistry.registerItem(billet_australium, billet_australium.getTranslationKey());
		GameRegistry.registerItem(billet_australium_lesser, billet_australium_lesser.getTranslationKey());
		GameRegistry.registerItem(billet_australium_greater, billet_australium_greater.getTranslationKey());
		GameRegistry.registerItem(billet_uranium_fuel, billet_uranium_fuel.getTranslationKey());
		GameRegistry.registerItem(billet_thorium_fuel, billet_thorium_fuel.getTranslationKey());
		GameRegistry.registerItem(billet_plutonium_fuel, billet_plutonium_fuel.getTranslationKey());
		GameRegistry.registerItem(billet_neptunium_fuel, billet_neptunium_fuel.getTranslationKey());
		GameRegistry.registerItem(billet_mox_fuel, billet_mox_fuel.getTranslationKey());
		GameRegistry.registerItem(billet_americium_fuel, billet_americium_fuel.getTranslationKey());
		GameRegistry.registerItem(billet_les, billet_les.getTranslationKey());
		GameRegistry.registerItem(billet_schrabidium_fuel, billet_schrabidium_fuel.getTranslationKey());
		GameRegistry.registerItem(billet_hes, billet_hes.getTranslationKey());
		GameRegistry.registerItem(billet_po210be, billet_po210be.getTranslationKey());
		GameRegistry.registerItem(billet_ra226be, billet_ra226be.getTranslationKey());
		GameRegistry.registerItem(billet_pu238be, billet_pu238be.getTranslationKey());
		GameRegistry.registerItem(billet_beryllium, billet_beryllium.getTranslationKey());
		GameRegistry.registerItem(billet_bismuth, billet_bismuth.getTranslationKey());
		GameRegistry.registerItem(billet_silicon, billet_silicon.getTranslationKey());
		GameRegistry.registerItem(billet_zirconium, billet_zirconium.getTranslationKey());
		GameRegistry.registerItem(billet_zfb_bismuth, billet_zfb_bismuth.getTranslationKey());
		GameRegistry.registerItem(billet_zfb_pu241, billet_zfb_pu241.getTranslationKey());
		GameRegistry.registerItem(billet_zfb_am_mix, billet_zfb_am_mix.getTranslationKey());
		GameRegistry.registerItem(billet_yharonite, billet_yharonite.getTranslationKey());
		GameRegistry.registerItem(billet_balefire_gold, billet_balefire_gold.getTranslationKey());
		GameRegistry.registerItem(billet_flashlead, billet_flashlead.getTranslationKey());
		GameRegistry.registerItem(billet_nuclear_waste, billet_nuclear_waste.getTranslationKey());
		GameRegistry.registerItem(billet_menthol, billet_menthol.getTranslationKey());
		GameRegistry.registerItem(chocolate_mint_billet, chocolate_mint_billet.getTranslationKey());

		//Dusts & Other
		GameRegistry.registerItem(cinnebar, cinnebar.getTranslationKey());
		GameRegistry.registerItem(nugget_mercury, nugget_mercury.getTranslationKey());
		GameRegistry.registerItem(ingot_mercury, ingot_mercury.getTranslationKey());
		GameRegistry.registerItem(bottle_mercury, bottle_mercury.getTranslationKey());
		GameRegistry.registerItem(coke, coke.getTranslationKey());
		GameRegistry.registerItem(powder_coke, powder_coke.getTranslationKey());
		GameRegistry.registerItem(lignite, lignite.getTranslationKey());
		GameRegistry.registerItem(coal_infernal, coal_infernal.getTranslationKey());
		GameRegistry.registerItem(briquette, briquette.getTranslationKey());
		GameRegistry.registerItem(woodemium_briquette, woodemium_briquette.getTranslationKey());
		GameRegistry.registerItem(sulfur, sulfur.getTranslationKey());
		GameRegistry.registerItem(niter, niter.getTranslationKey());
		GameRegistry.registerItem(nitra, nitra.getTranslationKey());
		GameRegistry.registerItem(nitra_small, nitra_small.getTranslationKey());
		GameRegistry.registerItem(fluorite, fluorite.getTranslationKey());
		GameRegistry.registerItem(powder_coal, powder_coal.getTranslationKey());
		GameRegistry.registerItem(powder_coal_tiny, powder_coal_tiny.getTranslationKey());
		GameRegistry.registerItem(powder_iron, powder_iron.getTranslationKey());
		GameRegistry.registerItem(powder_gold, powder_gold.getTranslationKey());
		GameRegistry.registerItem(powder_lapis, powder_lapis.getTranslationKey());
		GameRegistry.registerItem(powder_quartz, powder_quartz.getTranslationKey());
		GameRegistry.registerItem(powder_diamond, powder_diamond.getTranslationKey());
		GameRegistry.registerItem(powder_emerald, powder_emerald.getTranslationKey());
		GameRegistry.registerItem(powder_uranium, powder_uranium.getTranslationKey());
		GameRegistry.registerItem(powder_plutonium, powder_plutonium.getTranslationKey());
		GameRegistry.registerItem(powder_neptunium, powder_neptunium.getTranslationKey());
		GameRegistry.registerItem(powder_polonium, powder_polonium.getTranslationKey());
		GameRegistry.registerItem(powder_co60, powder_co60.getTranslationKey());
		GameRegistry.registerItem(powder_sr90, powder_sr90.getTranslationKey());
		GameRegistry.registerItem(powder_sr90_tiny, powder_sr90_tiny.getTranslationKey());
		GameRegistry.registerItem(powder_i131, powder_i131.getTranslationKey());
		GameRegistry.registerItem(powder_i131_tiny, powder_i131_tiny.getTranslationKey());
		GameRegistry.registerItem(powder_xe135, powder_xe135.getTranslationKey());
		GameRegistry.registerItem(powder_xe135_tiny, powder_xe135_tiny.getTranslationKey());
		GameRegistry.registerItem(powder_cs137, powder_cs137.getTranslationKey());
		GameRegistry.registerItem(powder_cs137_tiny, powder_cs137_tiny.getTranslationKey());
		GameRegistry.registerItem(powder_cn989, powder_cn989.getTranslationKey());
		GameRegistry.registerItem(powder_au198, powder_au198.getTranslationKey());
		GameRegistry.registerItem(powder_ra226, powder_ra226.getTranslationKey());
		GameRegistry.registerItem(powder_at209, powder_at209.getTranslationKey());
		GameRegistry.registerItem(powder_titanium, powder_titanium.getTranslationKey());
		GameRegistry.registerItem(powder_nickel, powder_nickel.getTranslationKey());
		GameRegistry.registerItem(powder_copper, powder_copper.getTranslationKey());
		GameRegistry.registerItem(powder_zinc, powder_zinc.getTranslationKey());
		GameRegistry.registerItem(powder_gallium_tiny, powder_gallium_tiny.getTranslationKey());
		GameRegistry.registerItem(powder_gallium, powder_gallium.getTranslationKey());
		GameRegistry.registerItem(powder_red_copper, powder_red_copper.getTranslationKey());
		GameRegistry.registerItem(powder_advanced_alloy, powder_advanced_alloy.getTranslationKey());
		GameRegistry.registerItem(powder_tungsten, powder_tungsten.getTranslationKey());
		GameRegistry.registerItem(powder_aluminium, powder_aluminium.getTranslationKey());
		GameRegistry.registerItem(powder_steel, powder_steel.getTranslationKey());
		GameRegistry.registerItem(powder_steel_tiny, powder_steel_tiny.getTranslationKey());
		GameRegistry.registerItem(powder_tcalloy, powder_tcalloy.getTranslationKey());
		GameRegistry.registerItem(powder_lead, powder_lead.getTranslationKey());
		GameRegistry.registerItem(powder_bismuth, powder_bismuth.getTranslationKey());
		GameRegistry.registerItem(powder_calcium, powder_calcium.getTranslationKey());
		GameRegistry.registerItem(powder_cadmium, powder_cadmium.getTranslationKey());
		GameRegistry.registerItem(powder_coltan_ore, powder_coltan_ore.getTranslationKey());
		GameRegistry.registerItem(powder_coltan, powder_coltan.getTranslationKey());
		GameRegistry.registerItem(powder_tantalium, powder_tantalium.getTranslationKey());
		GameRegistry.registerItem(powder_tektite, powder_tektite.getTranslationKey());
		GameRegistry.registerItem(powder_paleogenite, powder_paleogenite.getTranslationKey());
		GameRegistry.registerItem(powder_paleogenite_tiny, powder_paleogenite_tiny.getTranslationKey());
		GameRegistry.registerItem(powder_impure_osmiridium, powder_impure_osmiridium.getTranslationKey());
		GameRegistry.registerItem(powder_borax, powder_borax.getTranslationKey());
		GameRegistry.registerItem(powder_chlorocalcite, powder_chlorocalcite.getTranslationKey());
		GameRegistry.registerItem(powder_molysite, powder_molysite.getTranslationKey());
		GameRegistry.registerItem(powder_yellowcake, powder_yellowcake.getTranslationKey());
		GameRegistry.registerItem(powder_beryllium, powder_beryllium.getTranslationKey());
		GameRegistry.registerItem(powder_dura_steel, powder_dura_steel.getTranslationKey());
		GameRegistry.registerItem(powder_polymer, powder_polymer.getTranslationKey());
		GameRegistry.registerItem(powder_bakelite, powder_bakelite.getTranslationKey());
		GameRegistry.registerItem(powder_rubber, powder_rubber.getTranslationKey());
		GameRegistry.registerItem(powder_pvc, powder_pvc.getTranslationKey());
		GameRegistry.registerItem(powder_schrabidium, powder_schrabidium.getTranslationKey());
		GameRegistry.registerItem(powder_schrabidate, powder_schrabidate.getTranslationKey());
		GameRegistry.registerItem(powder_magnetized_tungsten, powder_magnetized_tungsten.getTranslationKey());
		GameRegistry.registerItem(powder_chlorophyte, powder_chlorophyte.getTranslationKey());
		GameRegistry.registerItem(powder_combine_steel, powder_combine_steel.getTranslationKey());
		GameRegistry.registerItem(powder_lithium, powder_lithium.getTranslationKey());
		GameRegistry.registerItem(powder_lithium_tiny, powder_lithium_tiny.getTranslationKey());
		GameRegistry.registerItem(powder_zirconium, powder_zirconium.getTranslationKey());
		GameRegistry.registerItem(powder_sodium, powder_sodium.getTranslationKey());
		GameRegistry.registerItem(powder_lignite, powder_lignite.getTranslationKey());
		GameRegistry.registerItem(powder_iodine, powder_iodine.getTranslationKey());
		GameRegistry.registerItem(powder_thorium, powder_thorium.getTranslationKey());
		GameRegistry.registerItem(powder_neodymium, powder_neodymium.getTranslationKey());
		GameRegistry.registerItem(powder_neodymium_tiny, powder_neodymium_tiny.getTranslationKey());
		GameRegistry.registerItem(powder_astatine, powder_astatine.getTranslationKey());
		GameRegistry.registerItem(powder_caesium, powder_caesium.getTranslationKey());
		GameRegistry.registerItem(powder_australium, powder_australium.getTranslationKey());
		GameRegistry.registerItem(powder_strontium, powder_strontium.getTranslationKey());
		GameRegistry.registerItem(powder_cobalt, powder_cobalt.getTranslationKey());
		GameRegistry.registerItem(powder_cobalt_tiny, powder_cobalt_tiny.getTranslationKey());
		GameRegistry.registerItem(powder_bromine, powder_bromine.getTranslationKey());
		GameRegistry.registerItem(powder_niobium, powder_niobium.getTranslationKey());
		GameRegistry.registerItem(powder_niobium_tiny, powder_niobium_tiny.getTranslationKey());
		GameRegistry.registerItem(powder_tennessine, powder_tennessine.getTranslationKey());
		GameRegistry.registerItem(powder_cerium, powder_cerium.getTranslationKey());
		GameRegistry.registerItem(powder_cerium_tiny, powder_cerium_tiny.getTranslationKey());
		GameRegistry.registerItem(powder_lanthanium, powder_lanthanium.getTranslationKey());
		GameRegistry.registerItem(powder_lanthanium_tiny, powder_lanthanium_tiny.getTranslationKey());
		GameRegistry.registerItem(powder_actinium, powder_actinium.getTranslationKey());
		GameRegistry.registerItem(powder_actinium_tiny, powder_actinium_tiny.getTranslationKey());
		GameRegistry.registerItem(powder_boron, powder_boron.getTranslationKey());
		GameRegistry.registerItem(powder_boron_tiny, powder_boron_tiny.getTranslationKey());
		GameRegistry.registerItem(powder_asbestos, powder_asbestos.getTranslationKey());
		GameRegistry.registerItem(powder_magic, powder_magic.getTranslationKey());
		GameRegistry.registerItem(powder_sawdust, powder_sawdust.getTranslationKey());
		GameRegistry.registerItem(powder_flux, powder_flux.getTranslationKey());
		GameRegistry.registerItem(powder_fertilizer, powder_fertilizer.getTranslationKey());
		GameRegistry.registerItem(powder_balefire, powder_balefire.getTranslationKey());
		GameRegistry.registerItem(powder_semtex_mix, powder_semtex_mix.getTranslationKey());
		GameRegistry.registerItem(powder_desh_mix, powder_desh_mix.getTranslationKey());
		GameRegistry.registerItem(powder_desh_ready, powder_desh_ready.getTranslationKey());
		GameRegistry.registerItem(powder_desh, powder_desh.getTranslationKey());
		GameRegistry.registerItem(powder_nitan_mix, powder_nitan_mix.getTranslationKey());
		GameRegistry.registerItem(powder_spark_mix, powder_spark_mix.getTranslationKey());
		GameRegistry.registerItem(powder_meteorite, powder_meteorite.getTranslationKey());
		GameRegistry.registerItem(powder_meteorite_tiny, powder_meteorite_tiny.getTranslationKey());
		GameRegistry.registerItem(powder_euphemium, powder_euphemium.getTranslationKey());
		GameRegistry.registerItem(powder_wd2004_tiny, powder_wd2004_tiny.getTranslationKey());
		GameRegistry.registerItem(powder_wd2004, powder_wd2004.getTranslationKey());
		GameRegistry.registerItem(powder_dineutronium, powder_dineutronium.getTranslationKey());
		GameRegistry.registerItem(dust, dust.getTranslationKey());
		GameRegistry.registerItem(dust_tiny, dust_tiny.getTranslationKey());
		GameRegistry.registerItem(fallout, fallout.getTranslationKey());
		GameRegistry.registerItem(powder_ash, powder_ash.getTranslationKey());
		GameRegistry.registerItem(powder_limestone, powder_limestone.getTranslationKey());
		GameRegistry.registerItem(powder_cement, powder_cement.getTranslationKey());

		GameRegistry.registerItem(nickel_salts, nickel_salts.getTranslationKey());

		//Powders
		GameRegistry.registerItem(powder_fire, powder_fire.getTranslationKey());
		GameRegistry.registerItem(powder_ice, powder_ice.getTranslationKey());
		GameRegistry.registerItem(powder_poison, powder_poison.getTranslationKey());
		GameRegistry.registerItem(powder_thermite, powder_thermite.getTranslationKey());
		GameRegistry.registerItem(powder_power, powder_power.getTranslationKey());
		GameRegistry.registerItem(cordite, cordite.getTranslationKey());
		GameRegistry.registerItem(ammonium_nitrate, ammonium_nitrate.getTranslationKey());
		GameRegistry.registerItem(ballistite, ballistite.getTranslationKey());
		GameRegistry.registerItem(ball_dynamite, ball_dynamite.getTranslationKey());
		GameRegistry.registerItem(ball_tnt, ball_tnt.getTranslationKey());
		GameRegistry.registerItem(ball_tatb, ball_tatb.getTranslationKey());
		GameRegistry.registerItem(ball_resin, ball_resin.getTranslationKey());
		GameRegistry.registerItem(ball_fireclay, ball_fireclay.getTranslationKey());
		GameRegistry.registerItem(ball_ferric_clay, ball_ferric_clay.getTranslationKey());

		//Ores
		GameRegistry.registerItem(ore_bedrock, ore_bedrock.getTranslationKey());
		GameRegistry.registerItem(bedrock_ore_fragment, bedrock_ore_fragment.getTranslationKey());

		GameRegistry.registerItem(ore_centrifuged, ore_centrifuged.getTranslationKey());
		GameRegistry.registerItem(ore_cleaned, ore_cleaned.getTranslationKey());
		GameRegistry.registerItem(ore_separated, ore_separated.getTranslationKey());
		GameRegistry.registerItem(ore_purified, ore_purified.getTranslationKey());
		GameRegistry.registerItem(ore_nitrated, ore_nitrated.getTranslationKey());
		GameRegistry.registerItem(ore_nitrocrystalline, ore_nitrocrystalline.getTranslationKey());
		GameRegistry.registerItem(ore_deepcleaned, ore_deepcleaned.getTranslationKey());
		GameRegistry.registerItem(ore_seared, ore_seared.getTranslationKey());
		GameRegistry.registerItem(ore_enriched, ore_enriched.getTranslationKey());
		GameRegistry.registerItem(ore_byproduct, ore_byproduct.getTranslationKey());
		GameRegistry.registerItem(bedrock_ore_base, bedrock_ore_base.getTranslationKey());
		GameRegistry.registerItem(bedrock_ore, bedrock_ore.getTranslationKey());

		GameRegistry.registerItem(crystal_mineral, crystal_mineral.getTranslationKey());
		GameRegistry.registerItem(crystal_cleaned, crystal_cleaned.getTranslationKey());
		GameRegistry.registerItem(mineral_dust, mineral_dust.getTranslationKey());
		GameRegistry.registerItem(mineral_fragment, mineral_fragment.getTranslationKey());

		//Crystals
		GameRegistry.registerItem(crystal_coal, crystal_coal.getTranslationKey());
		GameRegistry.registerItem(crystal_iron, crystal_iron.getTranslationKey());
		GameRegistry.registerItem(crystal_gold, crystal_gold.getTranslationKey());
		GameRegistry.registerItem(crystal_redstone, crystal_redstone.getTranslationKey());
		GameRegistry.registerItem(crystal_lapis, crystal_lapis.getTranslationKey());
		GameRegistry.registerItem(crystal_diamond, crystal_diamond.getTranslationKey());
		GameRegistry.registerItem(crystal_uranium, crystal_uranium.getTranslationKey());
		GameRegistry.registerItem(crystal_thorium, crystal_thorium.getTranslationKey());
		GameRegistry.registerItem(crystal_plutonium, crystal_plutonium.getTranslationKey());
		GameRegistry.registerItem(crystal_titanium, crystal_titanium.getTranslationKey());
		GameRegistry.registerItem(crystal_sulfur, crystal_sulfur.getTranslationKey());
		GameRegistry.registerItem(crystal_niter, crystal_niter.getTranslationKey());
		GameRegistry.registerItem(crystal_copper, crystal_copper.getTranslationKey());
		GameRegistry.registerItem(crystal_tungsten, crystal_tungsten.getTranslationKey());
		GameRegistry.registerItem(crystal_aluminium, crystal_aluminium.getTranslationKey());
		GameRegistry.registerItem(crystal_fluorite, crystal_fluorite.getTranslationKey());
		GameRegistry.registerItem(crystal_beryllium, crystal_beryllium.getTranslationKey());
		GameRegistry.registerItem(crystal_lead, crystal_lead.getTranslationKey());
		GameRegistry.registerItem(crystal_schraranium, crystal_schraranium.getTranslationKey());
		GameRegistry.registerItem(crystal_schrabidium, crystal_schrabidium.getTranslationKey());
		GameRegistry.registerItem(crystal_rare, crystal_rare.getTranslationKey());
		GameRegistry.registerItem(crystal_phosphorus, crystal_phosphorus.getTranslationKey());
		GameRegistry.registerItem(crystal_lithium, crystal_lithium.getTranslationKey());
		GameRegistry.registerItem(crystal_cobalt, crystal_cobalt.getTranslationKey());
		GameRegistry.registerItem(crystal_starmetal, crystal_starmetal.getTranslationKey());
		GameRegistry.registerItem(crystal_cinnebar, crystal_cinnebar.getTranslationKey());
		GameRegistry.registerItem(crystal_trixite, crystal_trixite.getTranslationKey());
		GameRegistry.registerItem(crystal_osmiridium, crystal_osmiridium.getTranslationKey());
		GameRegistry.registerItem(gem_sodalite, gem_sodalite.getTranslationKey());
		GameRegistry.registerItem(gem_tantalium, gem_tantalium.getTranslationKey());
		GameRegistry.registerItem(gem_volcanic, gem_volcanic.getTranslationKey());
		GameRegistry.registerItem(gem_rad, gem_rad.getTranslationKey());
		GameRegistry.registerItem(gem_alexandrite, gem_alexandrite.getTranslationKey());
		GameRegistry.registerItem(crystal_nickel, crystal_nickel.getTranslationKey()); //l like nickel, i dont care, fuck off
		GameRegistry.registerItem(crystal_niobium, crystal_niobium.getTranslationKey()); // the true path to enlightenment is ignoring the little green circle
		GameRegistry.registerItem(crystal_zinc, crystal_zinc.getTranslationKey()); // the true path to enlightenment is ignoring the little green circle

		//Fragments
		GameRegistry.registerItem(fragment_neodymium, fragment_neodymium.getTranslationKey());
		GameRegistry.registerItem(fragment_cobalt, fragment_cobalt.getTranslationKey());
		GameRegistry.registerItem(fragment_niobium, fragment_niobium.getTranslationKey());
		GameRegistry.registerItem(fragment_cerium, fragment_cerium.getTranslationKey());
		GameRegistry.registerItem(fragment_lanthanium, fragment_lanthanium.getTranslationKey());
		GameRegistry.registerItem(fragment_actinium, fragment_actinium.getTranslationKey());
		GameRegistry.registerItem(fragment_boron, fragment_boron.getTranslationKey());
		GameRegistry.registerItem(fragment_meteorite, fragment_meteorite.getTranslationKey());
		GameRegistry.registerItem(fragment_coltan, fragment_coltan.getTranslationKey());
		GameRegistry.registerItem(chunk_ore, chunk_ore.getTranslationKey());

		//Things that look like rotten flesh but aren't
		GameRegistry.registerItem(biomass, biomass.getTranslationKey());
		GameRegistry.registerItem(biomass_compressed, biomass_compressed.getTranslationKey());
		//delicious!
		GameRegistry.registerItem(bio_wafer, bio_wafer.getTranslationKey());
		GameRegistry.registerItem(flesh, flesh.getTranslationKey());
		GameRegistry.registerItem(flesh_wafer, flesh_wafer.getTranslationKey());
		GameRegistry.registerItem(grilled_flesh, grilled_flesh.getTranslationKey());
		GameRegistry.registerItem(flesh_burger, flesh_burger.getTranslationKey());
		GameRegistry.registerItem(stick_pvc, stick_pvc.getTranslationKey());
		GameRegistry.registerItem(stick_vinyl, stick_vinyl.getTranslationKey());

		//Nuggets
		GameRegistry.registerItem(nugget_uranium, nugget_uranium.getTranslationKey());
		GameRegistry.registerItem(nugget_u233, nugget_u233.getTranslationKey());
		GameRegistry.registerItem(nugget_u235, nugget_u235.getTranslationKey());
		GameRegistry.registerItem(nugget_u238, nugget_u238.getTranslationKey());
		GameRegistry.registerItem(nugget_th232, nugget_th232.getTranslationKey());
		GameRegistry.registerItem(nugget_plutonium, nugget_plutonium.getTranslationKey());
		GameRegistry.registerItem(nugget_pu238, nugget_pu238.getTranslationKey());
		GameRegistry.registerItem(nugget_pu239, nugget_pu239.getTranslationKey());
		GameRegistry.registerItem(nugget_pu240, nugget_pu240.getTranslationKey());
		GameRegistry.registerItem(nugget_pu241, nugget_pu241.getTranslationKey());
		GameRegistry.registerItem(nugget_pu_mix, nugget_pu_mix.getTranslationKey());
		GameRegistry.registerItem(nugget_am241, nugget_am241.getTranslationKey());
		GameRegistry.registerItem(nugget_am242, nugget_am242.getTranslationKey());
		GameRegistry.registerItem(nugget_am_mix, nugget_am_mix.getTranslationKey());
		GameRegistry.registerItem(nugget_bk247, nugget_bk247.getTranslationKey());
		GameRegistry.registerItem(nugget_cm_mix, nugget_cm_mix.getTranslationKey());
		GameRegistry.registerItem(nugget_cm242, nugget_cm242.getTranslationKey());
		GameRegistry.registerItem(nugget_cm243, nugget_cm243.getTranslationKey());
		GameRegistry.registerItem(nugget_cm244, nugget_cm244.getTranslationKey());
		GameRegistry.registerItem(nugget_cm245, nugget_cm245.getTranslationKey());
		GameRegistry.registerItem(nugget_cm246, nugget_cm246.getTranslationKey());
		GameRegistry.registerItem(nugget_cm247, nugget_cm247.getTranslationKey());
		GameRegistry.registerItem(nugget_cm_fuel, nugget_cm_fuel.getTranslationKey());
		GameRegistry.registerItem(nugget_cf251, nugget_cf251.getTranslationKey());
		GameRegistry.registerItem(nugget_cf252, nugget_cf252.getTranslationKey());
		GameRegistry.registerItem(nugget_es253, nugget_es253.getTranslationKey());
		GameRegistry.registerItem(nugget_cn989, nugget_cn989.getTranslationKey());
		GameRegistry.registerItem(nugget_neptunium, nugget_neptunium.getTranslationKey());
		GameRegistry.registerItem(nugget_polonium, nugget_polonium.getTranslationKey());
		GameRegistry.registerItem(nugget_cobalt, nugget_cobalt.getTranslationKey());
		GameRegistry.registerItem(nugget_co60, nugget_co60.getTranslationKey());
		GameRegistry.registerItem(nugget_sr90, nugget_sr90.getTranslationKey());
		GameRegistry.registerItem(nugget_technetium, nugget_technetium.getTranslationKey());
		GameRegistry.registerItem(nugget_au198, nugget_au198.getTranslationKey());
		GameRegistry.registerItem(nugget_pb209, nugget_pb209.getTranslationKey());
		GameRegistry.registerItem(nugget_ra226, nugget_ra226.getTranslationKey());
		GameRegistry.registerItem(nugget_actinium, nugget_actinium.getTranslationKey());
		GameRegistry.registerItem(nugget_lead, nugget_lead.getTranslationKey());
		GameRegistry.registerItem(nugget_menthol, nugget_menthol.getTranslationKey());
		GameRegistry.registerItem(nugget_bismuth, nugget_bismuth.getTranslationKey());
		GameRegistry.registerItem(nugget_arsenic, nugget_arsenic.getTranslationKey());
		GameRegistry.registerItem(nugget_tantalium, nugget_tantalium.getTranslationKey());
		GameRegistry.registerItem(nugget_silicon, nugget_silicon.getTranslationKey());
		GameRegistry.registerItem(nugget_niobium, nugget_niobium.getTranslationKey());
		GameRegistry.registerItem(nugget_beryllium, nugget_beryllium.getTranslationKey());
		GameRegistry.registerItem(nugget_schrabidium, nugget_schrabidium.getTranslationKey());
		GameRegistry.registerItem(nugget_solinium, nugget_solinium.getTranslationKey());
		GameRegistry.registerItem(nugget_gh336, nugget_gh336.getTranslationKey());
		GameRegistry.registerItem(nugget_uranium_fuel, nugget_uranium_fuel.getTranslationKey());
		GameRegistry.registerItem(nugget_thorium_fuel, nugget_thorium_fuel.getTranslationKey());
		GameRegistry.registerItem(nugget_plutonium_fuel, nugget_plutonium_fuel.getTranslationKey());
		GameRegistry.registerItem(nugget_neptunium_fuel, nugget_neptunium_fuel.getTranslationKey());
		GameRegistry.registerItem(nugget_mox_fuel, nugget_mox_fuel.getTranslationKey());
		GameRegistry.registerItem(nugget_americium_fuel, nugget_americium_fuel.getTranslationKey());
		GameRegistry.registerItem(nugget_schrabidium_fuel, nugget_schrabidium_fuel.getTranslationKey());
		GameRegistry.registerItem(nugget_hes, nugget_hes.getTranslationKey());
		GameRegistry.registerItem(nugget_les, nugget_les.getTranslationKey());
		GameRegistry.registerItem(nugget_zirconium, nugget_zirconium.getTranslationKey());
		GameRegistry.registerItem(nugget_nickel, nugget_nickel.getTranslationKey());
		GameRegistry.registerItem(nugget_gaas, nugget_gaas.getTranslationKey());
		GameRegistry.registerItem(nugget_zinc, nugget_zinc.getTranslationKey());
		GameRegistry.registerItem(nugget_gallium, nugget_gallium.getTranslationKey());
		GameRegistry.registerItem(nugget_australium, nugget_australium.getTranslationKey());
		GameRegistry.registerItem(nugget_australium_lesser, nugget_australium_lesser.getTranslationKey());
		GameRegistry.registerItem(nugget_australium_greater, nugget_australium_greater.getTranslationKey());
		GameRegistry.registerItem(nugget_desh, nugget_desh.getTranslationKey());
		GameRegistry.registerItem(nugget_euphemium, nugget_euphemium.getTranslationKey());
		GameRegistry.registerItem(nugget_dineutronium, nugget_dineutronium.getTranslationKey());
		GameRegistry.registerItem(nugget_osmiridium, nugget_osmiridium.getTranslationKey());
		GameRegistry.registerItem(nugget_hafnium, nugget_hafnium.getTranslationKey());
		GameRegistry.registerItem(nugget_lanthanium, nugget_lanthanium.getTranslationKey());

		//Plates
		GameRegistry.registerItem(plate_iron, plate_iron.getTranslationKey());
		GameRegistry.registerItem(plate_gold, plate_gold.getTranslationKey());
		GameRegistry.registerItem(plate_titanium, plate_titanium.getTranslationKey());
		GameRegistry.registerItem(plate_nickel, plate_nickel.getTranslationKey());
		GameRegistry.registerItem(plate_aluminium, plate_aluminium.getTranslationKey());
		GameRegistry.registerItem(plate_steel, plate_steel.getTranslationKey());
		GameRegistry.registerItem(plate_stainless, plate_stainless.getTranslationKey());
		GameRegistry.registerItem(plate_lead, plate_lead.getTranslationKey());
		GameRegistry.registerItem(plate_copper, plate_copper.getTranslationKey());
		GameRegistry.registerItem(plate_advanced_alloy, plate_advanced_alloy.getTranslationKey());
		GameRegistry.registerItem(plate_dura_steel, plate_dura_steel.getTranslationKey());
		GameRegistry.registerItem(neutron_reflector, neutron_reflector.getTranslationKey());
		GameRegistry.registerItem(plate_schrabidium, plate_schrabidium.getTranslationKey());
		GameRegistry.registerItem(plate_combine_steel, plate_combine_steel.getTranslationKey());
		GameRegistry.registerItem(plate_mixed, plate_mixed.getTranslationKey());
		GameRegistry.registerItem(plate_gunmetal, plate_gunmetal.getTranslationKey());
		GameRegistry.registerItem(plate_weaponsteel, plate_weaponsteel.getTranslationKey());
		GameRegistry.registerItem(plate_saturnite, plate_saturnite.getTranslationKey());
		GameRegistry.registerItem(plate_paa, plate_paa.getTranslationKey());
		GameRegistry.registerItem(plate_polymer, plate_polymer.getTranslationKey());
		GameRegistry.registerItem(plate_kevlar, plate_kevlar.getTranslationKey());
		GameRegistry.registerItem(plate_dalekanium, plate_dalekanium.getTranslationKey());
		GameRegistry.registerItem(plate_desh, plate_desh.getTranslationKey());
		GameRegistry.registerItem(plate_bismuth, plate_bismuth.getTranslationKey());
		GameRegistry.registerItem(plate_euphemium, plate_euphemium.getTranslationKey());
		GameRegistry.registerItem(plate_dineutronium, plate_dineutronium.getTranslationKey());
		GameRegistry.registerItem(plate_cn989, plate_cn989.getTranslationKey());

		//Armor Plates
		GameRegistry.registerItem(plate_armor_titanium, plate_armor_titanium.getTranslationKey());
		GameRegistry.registerItem(plate_armor_ajr, plate_armor_ajr.getTranslationKey());
		GameRegistry.registerItem(plate_armor_hev, plate_armor_hev.getTranslationKey());
		GameRegistry.registerItem(plate_armor_lunar, plate_armor_lunar.getTranslationKey());
		GameRegistry.registerItem(plate_armor_fau, plate_armor_fau.getTranslationKey());
		GameRegistry.registerItem(plate_armor_dnt, plate_armor_dnt.getTranslationKey());

		//Heavy/Cast Plate
		GameRegistry.registerItem(plate_cast, plate_cast.getTranslationKey());
		GameRegistry.registerItem(plate_welded, plate_welded.getTranslationKey());
		GameRegistry.registerItem(shell, shell.getTranslationKey());
		GameRegistry.registerItem(pipe, pipe.getTranslationKey());

		//Bolts
		GameRegistry.registerItem(bolt, bolt.getTranslationKey());
		GameRegistry.registerItem(bolt_spike, bolt_spike.getTranslationKey());

		//Cloth
		GameRegistry.registerItem(hazmat_cloth, hazmat_cloth.getTranslationKey());
		GameRegistry.registerItem(hazmat_cloth_red, hazmat_cloth_red.getTranslationKey());
		GameRegistry.registerItem(hazmat_cloth_grey, hazmat_cloth_grey.getTranslationKey());
		GameRegistry.registerItem(asbestos_cloth, asbestos_cloth.getTranslationKey());
		GameRegistry.registerItem(rag, rag.getTranslationKey());
		GameRegistry.registerItem(rag_damp, rag_damp.getTranslationKey());
		GameRegistry.registerItem(rag_piss, rag_piss.getTranslationKey());
		GameRegistry.registerItem(rag_blood, rag_blood.getTranslationKey());
		GameRegistry.registerItem(filter_coal, filter_coal.getTranslationKey());

		//Wires
		GameRegistry.registerItem(wire_fine, wire_fine.getTranslationKey());
		GameRegistry.registerItem(wire_dense, wire_dense.getTranslationKey());

		//Parts
		GameRegistry.registerItem(coil_copper, coil_copper.getTranslationKey());
		GameRegistry.registerItem(coil_copper_torus, coil_copper_torus.getTranslationKey());
		GameRegistry.registerItem(coil_advanced_alloy, coil_advanced_alloy.getTranslationKey());
		GameRegistry.registerItem(coil_advanced_torus, coil_advanced_torus.getTranslationKey());
		GameRegistry.registerItem(coil_gold, coil_gold.getTranslationKey());
		GameRegistry.registerItem(coil_gold_torus, coil_gold_torus.getTranslationKey());
		GameRegistry.registerItem(coil_tungsten, coil_tungsten.getTranslationKey());
		GameRegistry.registerItem(coil_magnetized_tungsten, coil_magnetized_tungsten.getTranslationKey());
		GameRegistry.registerItem(safety_fuse, safety_fuse.getTranslationKey());
		GameRegistry.registerItem(tank_steel, tank_steel.getTranslationKey());
		GameRegistry.registerItem(motor, motor.getTranslationKey());
		GameRegistry.registerItem(motor_desh, motor_desh.getTranslationKey());
		GameRegistry.registerItem(motor_bismuth, motor_bismuth.getTranslationKey());
		GameRegistry.registerItem(centrifuge_element, centrifuge_element.getTranslationKey());
		GameRegistry.registerItem(reactor_core, reactor_core.getTranslationKey());
		GameRegistry.registerItem(rtg_unit, rtg_unit.getTranslationKey());
		GameRegistry.registerItem(pipes_steel, pipes_steel.getTranslationKey());
		GameRegistry.registerItem(drill_titanium, drill_titanium.getTranslationKey());
		GameRegistry.registerItem(photo_panel, photo_panel.getTranslationKey());
		GameRegistry.registerItem(beryllium_mirror, beryllium_mirror.getTranslationKey());
		GameRegistry.registerItem(chlorine_pinwheel, chlorine_pinwheel.getTranslationKey());
		GameRegistry.registerItem(oxy_pinwheel, oxy_pinwheel.getTranslationKey());
		GameRegistry.registerItem(ring_starmetal, ring_starmetal.getTranslationKey());
		GameRegistry.registerItem(deuterium_filter, deuterium_filter.getTranslationKey());
		GameRegistry.registerItem(chemical_dye, chemical_dye.getTranslationKey());
		GameRegistry.registerItem(crayon, crayon.getTranslationKey());
		GameRegistry.registerItem(part_generic, part_generic.getTranslationKey());
		GameRegistry.registerItem(item_expensive, item_expensive.getTranslationKey());
		GameRegistry.registerItem(item_secret, item_secret.getTranslationKey());
		GameRegistry.registerItem(ingot_metal, ingot_metal.getTranslationKey());
		GameRegistry.registerItem(parts_legendary, parts_legendary.getTranslationKey());
		GameRegistry.registerItem(gear_large, gear_large.getTranslationKey());
		GameRegistry.registerItem(sawblade, sawblade.getTranslationKey());
		GameRegistry.registerItem(part_barrel_light, part_barrel_light.getTranslationKey());
		GameRegistry.registerItem(part_barrel_heavy, part_barrel_heavy.getTranslationKey());
		GameRegistry.registerItem(part_receiver_light, part_receiver_light.getTranslationKey());
		GameRegistry.registerItem(part_receiver_heavy, part_receiver_heavy.getTranslationKey());
		GameRegistry.registerItem(part_mechanism, part_mechanism.getTranslationKey());
		GameRegistry.registerItem(part_stock, part_stock.getTranslationKey());
		GameRegistry.registerItem(part_grip, part_grip.getTranslationKey());
		GameRegistry.registerItem(hard_drive, hard_drive.getTranslationKey());
		GameRegistry.registerItem(full_drive, full_drive.getTranslationKey());
		GameRegistry.registerItem(divine_shard, divine_shard.getTranslationKey());

		//Plant Products
		GameRegistry.registerItem(plant_item, plant_item.getTranslationKey());
		GameRegistry.registerItem(scuttertail, scuttertail.getTranslationKey());
		GameRegistry.registerItem(saltleaf, saltleaf.getTranslationKey());
		GameRegistry.registerItem(leaf_rubber, leaf_rubber.getTranslationKey());
		GameRegistry.registerItem(leaf_pet, leaf_pet.getTranslationKey());
		//Teleporter Parts
		//GameRegistry.registerItem(telepad, telepad.getUnlocalizedName());
		GameRegistry.registerItem(entanglement_kit, entanglement_kit.getTranslationKey());

		//Bomb Parts
		GameRegistry.registerItem(fins_flat, fins_flat.getTranslationKey());
		GameRegistry.registerItem(fins_small_steel, fins_small_steel.getTranslationKey());
		GameRegistry.registerItem(fins_big_steel, fins_big_steel.getTranslationKey());
		GameRegistry.registerItem(fins_tri_steel, fins_tri_steel.getTranslationKey());
		GameRegistry.registerItem(fins_quad_titanium, fins_quad_titanium.getTranslationKey());
		GameRegistry.registerItem(sphere_steel, sphere_steel.getTranslationKey());
		GameRegistry.registerItem(pedestal_steel, pedestal_steel.getTranslationKey());
		GameRegistry.registerItem(dysfunctional_reactor, dysfunctional_reactor.getTranslationKey());
		GameRegistry.registerItem(blade_titanium, blade_titanium.getTranslationKey());
		GameRegistry.registerItem(blade_syngas, blade_syngas.getTranslationKey());
		GameRegistry.registerItem(blade_tungsten, blade_tungsten.getTranslationKey());
		GameRegistry.registerItem(turbine_titanium, turbine_titanium.getTranslationKey());
		GameRegistry.registerItem(turbine_tungsten, turbine_tungsten.getTranslationKey());
		GameRegistry.registerItem(turbine_syngas, turbine_syngas.getTranslationKey());
		GameRegistry.registerItem(flywheel_beryllium, flywheel_beryllium.getTranslationKey());
		GameRegistry.registerItem(ducttape, ducttape.getTranslationKey());
		GameRegistry.registerItem(catalyst_clay, catalyst_clay.getTranslationKey());
		GameRegistry.registerItem(missile_assembly, missile_assembly.getTranslationKey());
		GameRegistry.registerItem(warhead_generic_small, warhead_generic_small.getTranslationKey());
		GameRegistry.registerItem(warhead_generic_medium, warhead_generic_medium.getTranslationKey());
		GameRegistry.registerItem(warhead_generic_large, warhead_generic_large.getTranslationKey());
		GameRegistry.registerItem(warhead_incendiary_small, warhead_incendiary_small.getTranslationKey());
		GameRegistry.registerItem(warhead_incendiary_medium, warhead_incendiary_medium.getTranslationKey());
		GameRegistry.registerItem(warhead_incendiary_large, warhead_incendiary_large.getTranslationKey());
		GameRegistry.registerItem(warhead_cluster_small, warhead_cluster_small.getTranslationKey());
		GameRegistry.registerItem(warhead_cluster_medium, warhead_cluster_medium.getTranslationKey());
		GameRegistry.registerItem(warhead_cluster_large, warhead_cluster_large.getTranslationKey());
		GameRegistry.registerItem(warhead_buster_small, warhead_buster_small.getTranslationKey());
		GameRegistry.registerItem(warhead_buster_medium, warhead_buster_medium.getTranslationKey());
		GameRegistry.registerItem(warhead_buster_large, warhead_buster_large.getTranslationKey());
		GameRegistry.registerItem(warhead_nuclear, warhead_nuclear.getTranslationKey());
		GameRegistry.registerItem(warhead_mirv, warhead_mirv.getTranslationKey());
		GameRegistry.registerItem(warhead_volcano, warhead_volcano.getTranslationKey());
		GameRegistry.registerItem(fuel_tank_small, fuel_tank_small.getTranslationKey());
		GameRegistry.registerItem(fuel_tank_medium, fuel_tank_medium.getTranslationKey());
		GameRegistry.registerItem(fuel_tank_large, fuel_tank_large.getTranslationKey());
		GameRegistry.registerItem(thruster_small, thruster_small.getTranslationKey());
		GameRegistry.registerItem(thruster_medium, thruster_medium.getTranslationKey());
		GameRegistry.registerItem(thruster_large, thruster_large.getTranslationKey());
		GameRegistry.registerItem(thruster_nuclear, thruster_nuclear.getTranslationKey());
		GameRegistry.registerItem(sat_base, sat_base.getTranslationKey());
		GameRegistry.registerItem(sat_head_mapper, sat_head_mapper.getTranslationKey());
		GameRegistry.registerItem(sat_head_scanner, sat_head_scanner.getTranslationKey());
		GameRegistry.registerItem(sat_head_radar, sat_head_radar.getTranslationKey());
		GameRegistry.registerItem(sat_head_laser, sat_head_laser.getTranslationKey());
		GameRegistry.registerItem(sat_head_resonator, sat_head_resonator.getTranslationKey());
		GameRegistry.registerItem(seg_10, seg_10.getTranslationKey());
		GameRegistry.registerItem(seg_15, seg_15.getTranslationKey());
		GameRegistry.registerItem(seg_20, seg_20.getTranslationKey());

		//Chopper parts
		GameRegistry.registerItem(combine_scrap, combine_scrap.getTranslationKey());

		//Hammer Parts
		GameRegistry.registerItem(shimmer_head, shimmer_head.getTranslationKey());
		GameRegistry.registerItem(shimmer_axe_head, shimmer_axe_head.getTranslationKey());
		GameRegistry.registerItem(shimmer_handle, shimmer_handle.getTranslationKey());

		//Circuits
		GameRegistry.registerItem(circuit, circuit.getTranslationKey());
		GameRegistry.registerItem(crt_display, crt_display.getTranslationKey());
		GameRegistry.registerItem(circuit_star_piece, circuit_star_piece.getTranslationKey());
		GameRegistry.registerItem(circuit_star_component, circuit_star_component.getTranslationKey());
		GameRegistry.registerItem(circuit_star, circuit_star.getTranslationKey());

		//Casing
		GameRegistry.registerItem(casing, casing.getTranslationKey());

		//Bullet Assemblies
		GameRegistry.registerItem(assembly_nuke, assembly_nuke.getTranslationKey());

		//Wiring
		GameRegistry.registerItem(wiring_red_copper, wiring_red_copper.getTranslationKey());

		//Flame War in a Box
		GameRegistry.registerItem(flame_pony, flame_pony.getTranslationKey());
		GameRegistry.registerItem(flame_conspiracy, flame_conspiracy.getTranslationKey());
		GameRegistry.registerItem(flame_politics, flame_politics.getTranslationKey());
		GameRegistry.registerItem(flame_opinion, flame_opinion.getTranslationKey());

		//Pellets
		GameRegistry.registerItem(pellet_rtg_radium, pellet_rtg_radium.getTranslationKey());
		GameRegistry.registerItem(pellet_rtg_weak, pellet_rtg_weak.getTranslationKey());
		GameRegistry.registerItem(pellet_rtg, pellet_rtg.getTranslationKey());
		GameRegistry.registerItem(pellet_rtg_strontium, pellet_rtg_strontium.getTranslationKey());
		GameRegistry.registerItem(pellet_rtg_cobalt, pellet_rtg_cobalt.getTranslationKey());
		GameRegistry.registerItem(pellet_rtg_actinium, pellet_rtg_actinium.getTranslationKey());
		GameRegistry.registerItem(pellet_rtg_polonium, pellet_rtg_polonium.getTranslationKey());
		GameRegistry.registerItem(pellet_rtg_americium, pellet_rtg_americium.getTranslationKey());
		GameRegistry.registerItem(pellet_rtg_berkelium, pellet_rtg_berkelium.getTranslationKey());
		GameRegistry.registerItem(pellet_rtg_gold, pellet_rtg_gold.getTranslationKey());
		GameRegistry.registerItem(pellet_rtg_lead, pellet_rtg_lead.getTranslationKey());
		GameRegistry.registerItem(pellet_rtg_cf251, pellet_rtg_cf251.getTranslationKey());
		GameRegistry.registerItem(pellet_rtg_cf252, pellet_rtg_cf252.getTranslationKey());
		GameRegistry.registerItem(pellet_rtg_depleted, pellet_rtg_depleted.getTranslationKey());
		GameRegistry.registerItem(pellet_cluster, pellet_cluster.getTranslationKey());
		GameRegistry.registerItem(pellet_buckshot, pellet_buckshot.getTranslationKey());
		GameRegistry.registerItem(pellet_charged, pellet_charged.getTranslationKey());
		GameRegistry.registerItem(pellet_gas, pellet_gas.getTranslationKey());
		GameRegistry.registerItem(magnetron, magnetron.getTranslationKey());

		//Engine Pieces
		GameRegistry.registerItem(piston_selenium, piston_selenium.getTranslationKey());
		GameRegistry.registerItem(piston_set, piston_set.getTranslationKey());
		GameRegistry.registerItem(drillbit, drillbit.getTranslationKey());

		//Cells
		GameRegistry.registerItem(cell_empty, cell_empty.getTranslationKey());
		GameRegistry.registerItem(cell_uf6, cell_uf6.getTranslationKey());
		GameRegistry.registerItem(cell_puf6, cell_puf6.getTranslationKey());
		GameRegistry.registerItem(cell_deuterium, cell_deuterium.getTranslationKey());
		GameRegistry.registerItem(cell_tritium, cell_tritium.getTranslationKey());
		GameRegistry.registerItem(cell_sas3, cell_sas3.getTranslationKey());
		GameRegistry.registerItem(cell_antimatter, cell_antimatter.getTranslationKey());
		GameRegistry.registerItem(cell_anti_schrabidium, cell_anti_schrabidium.getTranslationKey());
		GameRegistry.registerItem(cell_balefire, cell_balefire.getTranslationKey());

		//DEMON CORE
		GameRegistry.registerItem(demon_core_open, demon_core_open.getTranslationKey());
		GameRegistry.registerItem(demon_core_closed, demon_core_closed.getTranslationKey());

		//PA
		GameRegistry.registerItem(pa_coil, pa_coil.getTranslationKey());

		//Particle Containers
		GameRegistry.registerItem(particle_empty, particle_empty.getTranslationKey());
		GameRegistry.registerItem(particle_hydrogen, particle_hydrogen.getTranslationKey());
		GameRegistry.registerItem(particle_copper, particle_copper.getTranslationKey());
		GameRegistry.registerItem(particle_lead, particle_lead.getTranslationKey());
		GameRegistry.registerItem(particle_amat, particle_amat.getTranslationKey());
		GameRegistry.registerItem(particle_aschrab, particle_aschrab.getTranslationKey());
		GameRegistry.registerItem(particle_higgs, particle_higgs.getTranslationKey());
		GameRegistry.registerItem(particle_muon, particle_muon.getTranslationKey());
		GameRegistry.registerItem(particle_tachyon, particle_tachyon.getTranslationKey());
		GameRegistry.registerItem(particle_strange, particle_strange.getTranslationKey());
		GameRegistry.registerItem(particle_dark, particle_dark.getTranslationKey());
		GameRegistry.registerItem(particle_sparkticle, particle_sparkticle.getTranslationKey());
		GameRegistry.registerItem(particle_digamma, particle_digamma.getTranslationKey());
		GameRegistry.registerItem(particle_lutece, particle_lutece.getTranslationKey());

		//Singularities, black holes and other cosmic horrors
		GameRegistry.registerItem(singularity, singularity.getTranslationKey());
		GameRegistry.registerItem(singularity_counter_resonant, singularity_counter_resonant.getTranslationKey());
		GameRegistry.registerItem(singularity_super_heated, singularity_super_heated.getTranslationKey());
		GameRegistry.registerItem(black_hole, black_hole.getTranslationKey());
		GameRegistry.registerItem(singularity_spark, singularity_spark.getTranslationKey());
		GameRegistry.registerItem(crystal_xen, crystal_xen.getTranslationKey());
		GameRegistry.registerItem(pellet_antimatter, pellet_antimatter.getTranslationKey());

		//Infinite Tanks
		GameRegistry.registerItem(inf_water, inf_water.getTranslationKey());
		GameRegistry.registerItem(inf_water_mk2, inf_water_mk2.getTranslationKey());

		GameRegistry.registerItem(transporter_linker, transporter_linker.getTranslationKey());

		//Canisters
		GameRegistry.registerItem(fuel_additive, fuel_additive.getTranslationKey());
		GameRegistry.registerItem(canister_empty, canister_empty.getTranslationKey());
		GameRegistry.registerItem(canister_full, canister_full.getTranslationKey());
		GameRegistry.registerItem(canister_napalm, canister_napalm.getTranslationKey());

		//Gas Tanks
		GameRegistry.registerItem(gas_empty, gas_empty.getTranslationKey());
		GameRegistry.registerItem(gas_full, gas_full.getTranslationKey());

		//Universal Tank
		GameRegistry.registerItem(fluid_tank_empty, fluid_tank_empty.getTranslationKey());
		GameRegistry.registerItem(fluid_tank_full, fluid_tank_full.getTranslationKey());
		GameRegistry.registerItem(fluid_tank_lead_empty, fluid_tank_lead_empty.getTranslationKey());
		GameRegistry.registerItem(fluid_tank_lead_full, fluid_tank_lead_full.getTranslationKey());
		GameRegistry.registerItem(fluid_barrel_empty, fluid_barrel_empty.getTranslationKey());
		GameRegistry.registerItem(fluid_barrel_full, fluid_barrel_full.getTranslationKey());
		GameRegistry.registerItem(fluid_barrel_infinite, fluid_barrel_infinite.getTranslationKey());

		//Packaged fluids
		GameRegistry.registerItem(fluid_pack_empty, fluid_pack_empty.getTranslationKey());
		GameRegistry.registerItem(fluid_pack_full, fluid_pack_full.getTranslationKey());

		//Pipette
		GameRegistry.registerItem(pipette, pipette.getTranslationKey());
		GameRegistry.registerItem(pipette_boron, pipette_boron.getTranslationKey());
		GameRegistry.registerItem(pipette_laboratory, pipette_laboratory.getTranslationKey());

		//Siphon
		GameRegistry.registerItem(siphon, siphon.getTranslationKey());

		//Batteries
		GameRegistry.registerItem(battery_generic, battery_generic.getTranslationKey());
		GameRegistry.registerItem(battery_red_cell, battery_red_cell.getTranslationKey());
		GameRegistry.registerItem(battery_red_cell_6, battery_red_cell_6.getTranslationKey());
		GameRegistry.registerItem(battery_red_cell_24, battery_red_cell_24.getTranslationKey());
		GameRegistry.registerItem(battery_advanced, battery_advanced.getTranslationKey());
		GameRegistry.registerItem(battery_advanced_cell, battery_advanced_cell.getTranslationKey());
		GameRegistry.registerItem(battery_advanced_cell_4, battery_advanced_cell_4.getTranslationKey());
		GameRegistry.registerItem(battery_advanced_cell_12, battery_advanced_cell_12.getTranslationKey());
		GameRegistry.registerItem(battery_lithium, battery_lithium.getTranslationKey());
		GameRegistry.registerItem(battery_lithium_cell, battery_lithium_cell.getTranslationKey());
		GameRegistry.registerItem(battery_lithium_cell_3, battery_lithium_cell_3.getTranslationKey());
		GameRegistry.registerItem(battery_lithium_cell_6, battery_lithium_cell_6.getTranslationKey());
		GameRegistry.registerItem(battery_schrabidium, battery_schrabidium.getTranslationKey());
		GameRegistry.registerItem(battery_schrabidium_cell, battery_schrabidium_cell.getTranslationKey());
		GameRegistry.registerItem(battery_schrabidium_cell_2, battery_schrabidium_cell_2.getTranslationKey());
		GameRegistry.registerItem(battery_schrabidium_cell_4, battery_schrabidium_cell_4.getTranslationKey());
		GameRegistry.registerItem(battery_spark, battery_spark.getTranslationKey());
		GameRegistry.registerItem(battery_trixite, battery_trixite.getTranslationKey());
		GameRegistry.registerItem(battery_spark_cell_6, battery_spark_cell_6.getTranslationKey());
		GameRegistry.registerItem(battery_spark_cell_25, battery_spark_cell_25.getTranslationKey());
		GameRegistry.registerItem(battery_spark_cell_100, battery_spark_cell_100.getTranslationKey());
		GameRegistry.registerItem(battery_spark_cell_1000, battery_spark_cell_1000.getTranslationKey());
		GameRegistry.registerItem(battery_spark_cell_2500, battery_spark_cell_2500.getTranslationKey());
		GameRegistry.registerItem(battery_spark_cell_10000, battery_spark_cell_10000.getTranslationKey());
		GameRegistry.registerItem(battery_spark_cell_power, battery_spark_cell_power.getTranslationKey());

		GameRegistry.registerItem(battery_pack, battery_pack.getTranslationKey());
		GameRegistry.registerItem(battery_sc, battery_sc.getTranslationKey());
		GameRegistry.registerItem(battery_creative, battery_creative.getTranslationKey());
		GameRegistry.registerItem(cube_power, cube_power.getTranslationKey());
		GameRegistry.registerItem(core_angel, core_angel.getTranslationKey());

		GameRegistry.registerItem(battery_potato, battery_potato.getTranslationKey());
		GameRegistry.registerItem(battery_potatos, battery_potatos.getTranslationKey());
		GameRegistry.registerItem(battery_sc_uranium, battery_sc_uranium.getTranslationKey());
		GameRegistry.registerItem(battery_sc_technetium, battery_sc_technetium.getTranslationKey());
		GameRegistry.registerItem(battery_sc_plutonium, battery_sc_plutonium.getTranslationKey());
		GameRegistry.registerItem(battery_sc_polonium, battery_sc_polonium.getTranslationKey());
		GameRegistry.registerItem(battery_sc_gold, battery_sc_gold.getTranslationKey());
		GameRegistry.registerItem(battery_sc_lead, battery_sc_lead.getTranslationKey());
		GameRegistry.registerItem(battery_sc_americium, battery_sc_americium.getTranslationKey());
		GameRegistry.registerItem(hev_battery, hev_battery.getTranslationKey());
		GameRegistry.registerItem(fusion_core, fusion_core.getTranslationKey());
		GameRegistry.registerItem(energy_core, energy_core.getTranslationKey());

		//Folders
		GameRegistry.registerItem(blueprints, blueprints.getTranslationKey());
		GameRegistry.registerItem(blueprint_folder, blueprint_folder.getTranslationKey());
		GameRegistry.registerItem(template_folder, template_folder.getTranslationKey());
		GameRegistry.registerItem(bobmazon, bobmazon.getTranslationKey());
		GameRegistry.registerItem(bobmazon_hidden, bobmazon_hidden.getTranslationKey());

		//Hydraulic Press Stamps
		GameRegistry.registerItem(stamp_stone_flat, stamp_stone_flat.getTranslationKey());
		GameRegistry.registerItem(stamp_stone_plate, stamp_stone_plate.getTranslationKey());
		GameRegistry.registerItem(stamp_stone_wire, stamp_stone_wire.getTranslationKey());
		GameRegistry.registerItem(stamp_stone_circuit, stamp_stone_circuit.getTranslationKey());
		GameRegistry.registerItem(stamp_iron_flat, stamp_iron_flat.getTranslationKey());
		GameRegistry.registerItem(stamp_iron_plate, stamp_iron_plate.getTranslationKey());
		GameRegistry.registerItem(stamp_iron_wire, stamp_iron_wire.getTranslationKey());
		GameRegistry.registerItem(stamp_iron_circuit, stamp_iron_circuit.getTranslationKey());
		GameRegistry.registerItem(stamp_steel_flat, stamp_steel_flat.getTranslationKey());
		GameRegistry.registerItem(stamp_steel_plate, stamp_steel_plate.getTranslationKey());
		GameRegistry.registerItem(stamp_steel_wire, stamp_steel_wire.getTranslationKey());
		GameRegistry.registerItem(stamp_steel_circuit, stamp_steel_circuit.getTranslationKey());
		GameRegistry.registerItem(stamp_titanium_flat, stamp_titanium_flat.getTranslationKey());
		GameRegistry.registerItem(stamp_titanium_plate, stamp_titanium_plate.getTranslationKey());
		GameRegistry.registerItem(stamp_titanium_wire, stamp_titanium_wire.getTranslationKey());
		GameRegistry.registerItem(stamp_titanium_circuit, stamp_titanium_circuit.getTranslationKey());
		GameRegistry.registerItem(stamp_obsidian_flat, stamp_obsidian_flat.getTranslationKey());
		GameRegistry.registerItem(stamp_obsidian_plate, stamp_obsidian_plate.getTranslationKey());
		GameRegistry.registerItem(stamp_obsidian_wire, stamp_obsidian_wire.getTranslationKey());
		GameRegistry.registerItem(stamp_obsidian_circuit, stamp_obsidian_circuit.getTranslationKey());
		GameRegistry.registerItem(stamp_desh_flat, stamp_desh_flat.getTranslationKey());
		GameRegistry.registerItem(stamp_desh_plate, stamp_desh_plate.getTranslationKey());
		GameRegistry.registerItem(stamp_desh_wire, stamp_desh_wire.getTranslationKey());
		GameRegistry.registerItem(stamp_desh_circuit, stamp_desh_circuit.getTranslationKey());
		GameRegistry.registerItem(stamp_357, stamp_357.getTranslationKey());
		GameRegistry.registerItem(stamp_44, stamp_44.getTranslationKey());
		GameRegistry.registerItem(stamp_9, stamp_9.getTranslationKey());
		GameRegistry.registerItem(stamp_50, stamp_50.getTranslationKey());

		GameRegistry.registerItem(stamp_desh_357, stamp_desh_357.getTranslationKey());
		GameRegistry.registerItem(stamp_desh_44, stamp_desh_44.getTranslationKey());
		GameRegistry.registerItem(stamp_desh_9, stamp_desh_9.getTranslationKey());
		GameRegistry.registerItem(stamp_desh_50, stamp_desh_50.getTranslationKey());
		GameRegistry.registerItem(stamp_book, stamp_book.getTranslationKey());

		//Molds
		GameRegistry.registerItem(mold_base, mold_base.getTranslationKey());
		GameRegistry.registerItem(mold, mold.getTranslationKey());
		GameRegistry.registerItem(scraps, scraps.getTranslationKey());

		//Machine Upgrades
		GameRegistry.registerItem(upgrade_muffler, upgrade_muffler.getTranslationKey());
		GameRegistry.registerItem(upgrade_template, upgrade_template.getTranslationKey());
		GameRegistry.registerItem(upgrade_speed_1, upgrade_speed_1.getTranslationKey());
		GameRegistry.registerItem(upgrade_speed_2, upgrade_speed_2.getTranslationKey());
		GameRegistry.registerItem(upgrade_speed_3, upgrade_speed_3.getTranslationKey());
		GameRegistry.registerItem(upgrade_effect_1, upgrade_effect_1.getTranslationKey());
		GameRegistry.registerItem(upgrade_effect_2, upgrade_effect_2.getTranslationKey());
		GameRegistry.registerItem(upgrade_effect_3, upgrade_effect_3.getTranslationKey());
		GameRegistry.registerItem(upgrade_power_1, upgrade_power_1.getTranslationKey());
		GameRegistry.registerItem(upgrade_power_2, upgrade_power_2.getTranslationKey());
		GameRegistry.registerItem(upgrade_power_3, upgrade_power_3.getTranslationKey());
		GameRegistry.registerItem(upgrade_fortune_1, upgrade_fortune_1.getTranslationKey());
		GameRegistry.registerItem(upgrade_fortune_2, upgrade_fortune_2.getTranslationKey());
		GameRegistry.registerItem(upgrade_fortune_3, upgrade_fortune_3.getTranslationKey());
		GameRegistry.registerItem(upgrade_afterburn_1, upgrade_afterburn_1.getTranslationKey());
		GameRegistry.registerItem(upgrade_afterburn_2, upgrade_afterburn_2.getTranslationKey());
		GameRegistry.registerItem(upgrade_afterburn_3, upgrade_afterburn_3.getTranslationKey());
		GameRegistry.registerItem(upgrade_overdrive_1, upgrade_overdrive_1.getTranslationKey());
		GameRegistry.registerItem(upgrade_overdrive_2, upgrade_overdrive_2.getTranslationKey());
		GameRegistry.registerItem(upgrade_overdrive_3, upgrade_overdrive_3.getTranslationKey());
		GameRegistry.registerItem(upgrade_radius, upgrade_radius.getTranslationKey());
		GameRegistry.registerItem(upgrade_health, upgrade_health.getTranslationKey());
		GameRegistry.registerItem(upgrade_smelter, upgrade_smelter.getTranslationKey());
		GameRegistry.registerItem(upgrade_shredder, upgrade_shredder.getTranslationKey());
		GameRegistry.registerItem(upgrade_centrifuge, upgrade_centrifuge.getTranslationKey());
		GameRegistry.registerItem(upgrade_crystallizer, upgrade_crystallizer.getTranslationKey());
		GameRegistry.registerItem(upgrade_nullifier, upgrade_nullifier.getTranslationKey());
		GameRegistry.registerItem(upgrade_screm, upgrade_screm.getTranslationKey());
		GameRegistry.registerItem(upgrade_gc_speed, upgrade_gc_speed.getTranslationKey());
		GameRegistry.registerItem(upgrade_5g, upgrade_5g.getTranslationKey());
		GameRegistry.registerItem(upgrade_stack, upgrade_stack.getTranslationKey());
		GameRegistry.registerItem(upgrade_ejector, upgrade_ejector.getTranslationKey());

		//Machine Templates
		GameRegistry.registerItem(siren_track, siren_track.getTranslationKey());
		GameRegistry.registerItem(fluid_identifier_multi, fluid_identifier_multi.getTranslationKey());
		GameRegistry.registerItem(fluid_icon, fluid_icon.getTranslationKey());
		GameRegistry.registerItem(fluid_duct, fluid_duct.getTranslationKey());
		GameRegistry.registerItem(crucible_template, crucible_template.getTranslationKey());

		//Machine Items
		GameRegistry.registerItem(fuse, fuse.getTranslationKey());
		GameRegistry.registerItem(screwdriver, screwdriver.getTranslationKey());
		GameRegistry.registerItem(screwdriver_desh, screwdriver_desh.getTranslationKey());
		GameRegistry.registerItem(hand_drill, hand_drill.getTranslationKey());
		GameRegistry.registerItem(hand_drill_desh, hand_drill_desh.getTranslationKey());
		GameRegistry.registerItem(chemistry_set, chemistry_set.getTranslationKey());
		GameRegistry.registerItem(chemistry_set_boron, chemistry_set_boron.getTranslationKey());
		GameRegistry.registerItem(blowtorch, blowtorch.getTranslationKey());
		GameRegistry.registerItem(acetylene_torch, acetylene_torch.getTranslationKey());
		GameRegistry.registerItem(boltgun, boltgun.getTranslationKey());
		GameRegistry.registerItem(overfuse, overfuse.getTranslationKey());
		GameRegistry.registerItem(arc_electrode, arc_electrode.getTranslationKey());
		GameRegistry.registerItem(arc_electrode_burnt, arc_electrode_burnt.getTranslationKey());

		//Particle Collider Fuel
		GameRegistry.registerItem(part_lithium, part_lithium.getTranslationKey());
		GameRegistry.registerItem(part_beryllium, part_beryllium.getTranslationKey());
		GameRegistry.registerItem(part_carbon, part_carbon.getTranslationKey());
		GameRegistry.registerItem(part_copper, part_copper.getTranslationKey());
		GameRegistry.registerItem(part_plutonium, part_plutonium.getTranslationKey());

		//FEL laser crystals
		GameRegistry.registerItem(laser_crystal_co2, laser_crystal_co2.getTranslationKey());
		GameRegistry.registerItem(laser_crystal_bismuth, laser_crystal_bismuth.getTranslationKey());
		GameRegistry.registerItem(laser_crystal_cmb, laser_crystal_cmb.getTranslationKey());
		GameRegistry.registerItem(laser_crystal_iron, laser_crystal_iron.getTranslationKey());
		GameRegistry.registerItem(laser_crystal_dnt, laser_crystal_dnt.getTranslationKey());
		GameRegistry.registerItem(laser_crystal_digamma, laser_crystal_digamma.getTranslationKey());

		//Catalyst Rune Sigils
		GameRegistry.registerItem(rune_blank, rune_blank.getTranslationKey());
		GameRegistry.registerItem(rune_isa, rune_isa.getTranslationKey());
		GameRegistry.registerItem(rune_dagaz, rune_dagaz.getTranslationKey());
		GameRegistry.registerItem(rune_hagalaz, rune_hagalaz.getTranslationKey());
		GameRegistry.registerItem(rune_jera, rune_jera.getTranslationKey());
		GameRegistry.registerItem(rune_thurisaz, rune_thurisaz.getTranslationKey());

		//AMS Catalysts
		GameRegistry.registerItem(ams_catalyst_blank, ams_catalyst_blank.getTranslationKey());
		GameRegistry.registerItem(ams_catalyst_aluminium, ams_catalyst_aluminium.getTranslationKey());
		GameRegistry.registerItem(ams_catalyst_beryllium, ams_catalyst_beryllium.getTranslationKey());
		GameRegistry.registerItem(ams_catalyst_caesium, ams_catalyst_caesium.getTranslationKey());
		GameRegistry.registerItem(ams_catalyst_cerium, ams_catalyst_cerium.getTranslationKey());
		GameRegistry.registerItem(ams_catalyst_cobalt, ams_catalyst_cobalt.getTranslationKey());
		GameRegistry.registerItem(ams_catalyst_copper, ams_catalyst_copper.getTranslationKey());
		GameRegistry.registerItem(ams_catalyst_euphemium, ams_catalyst_euphemium.getTranslationKey());
		GameRegistry.registerItem(ams_catalyst_dineutronium, ams_catalyst_dineutronium.getTranslationKey());
		GameRegistry.registerItem(ams_catalyst_iron, ams_catalyst_iron.getTranslationKey());
		GameRegistry.registerItem(ams_catalyst_lithium, ams_catalyst_lithium.getTranslationKey());
		GameRegistry.registerItem(ams_catalyst_niobium, ams_catalyst_niobium.getTranslationKey());
		GameRegistry.registerItem(ams_catalyst_schrabidium, ams_catalyst_schrabidium.getTranslationKey());
		GameRegistry.registerItem(ams_catalyst_strontium, ams_catalyst_strontium.getTranslationKey());
		GameRegistry.registerItem(ams_catalyst_thorium, ams_catalyst_thorium.getTranslationKey());
		GameRegistry.registerItem(ams_catalyst_tungsten, ams_catalyst_tungsten.getTranslationKey());

		//Shredder Blades
		GameRegistry.registerItem(blades_steel, blades_steel.getTranslationKey());
		GameRegistry.registerItem(blades_titanium, blades_titanium.getTranslationKey());
		GameRegistry.registerItem(blades_advanced_alloy, blades_advanced_alloy.getTranslationKey());
		GameRegistry.registerItem(blades_desh, blades_desh.getTranslationKey());

		//Generator Stuff
		GameRegistry.registerItem(thermo_element, thermo_element.getTranslationKey());
		GameRegistry.registerItem(catalytic_converter, catalytic_converter.getTranslationKey());

		//AMS Components
		GameRegistry.registerItem(ams_lens, ams_lens.getTranslationKey());
		GameRegistry.registerItem(ams_core_sing, ams_core_sing.getTranslationKey());
		GameRegistry.registerItem(ams_core_wormhole, ams_core_wormhole.getTranslationKey());
		GameRegistry.registerItem(ams_core_eyeofharmony, ams_core_eyeofharmony.getTranslationKey());
		GameRegistry.registerItem(ams_core_thingy, ams_core_thingy.getTranslationKey());

		//Fusion Shields
		GameRegistry.registerItem(fusion_shield_tungsten, fusion_shield_tungsten.getTranslationKey());
		GameRegistry.registerItem(fusion_shield_desh, fusion_shield_desh.getTranslationKey());
		GameRegistry.registerItem(fusion_shield_chlorophyte, fusion_shield_chlorophyte.getTranslationKey());
		GameRegistry.registerItem(fusion_shield_vaporwave, fusion_shield_vaporwave.getTranslationKey());

		//Breeding Rods
		GameRegistry.registerItem(rod_empty, rod_empty.getTranslationKey());
		GameRegistry.registerItem(rod, rod.getTranslationKey());
		GameRegistry.registerItem(rod_dual_empty, rod_dual_empty.getTranslationKey());
		GameRegistry.registerItem(rod_dual, rod_dual.getTranslationKey());
		GameRegistry.registerItem(rod_quad_empty, rod_quad_empty.getTranslationKey());
		GameRegistry.registerItem(rod_quad, rod_quad.getTranslationKey());

		//ZIRNOX parts
		GameRegistry.registerItem(rod_zirnox_empty, rod_zirnox_empty.getTranslationKey());
		GameRegistry.registerItem(rod_zirnox_tritium, rod_zirnox_tritium.getTranslationKey());
		GameRegistry.registerItem(rod_zirnox, rod_zirnox.getTranslationKey());

		GameRegistry.registerItem(rod_zirnox_natural_uranium_fuel_depleted, rod_zirnox_natural_uranium_fuel_depleted.getTranslationKey());
		GameRegistry.registerItem(rod_zirnox_uranium_fuel_depleted, rod_zirnox_uranium_fuel_depleted.getTranslationKey());
		GameRegistry.registerItem(rod_zirnox_thorium_fuel_depleted, rod_zirnox_thorium_fuel_depleted.getTranslationKey());
		GameRegistry.registerItem(rod_zirnox_mox_fuel_depleted, rod_zirnox_mox_fuel_depleted.getTranslationKey());
		GameRegistry.registerItem(rod_zirnox_plutonium_fuel_depleted, rod_zirnox_plutonium_fuel_depleted.getTranslationKey());
		GameRegistry.registerItem(rod_zirnox_u233_fuel_depleted, rod_zirnox_u233_fuel_depleted.getTranslationKey());
		GameRegistry.registerItem(rod_zirnox_u235_fuel_depleted, rod_zirnox_u235_fuel_depleted.getTranslationKey());
		GameRegistry.registerItem(rod_zirnox_les_fuel_depleted, rod_zirnox_les_fuel_depleted.getTranslationKey());
		GameRegistry.registerItem(rod_zirnox_zfb_mox_depleted, rod_zirnox_zfb_mox_depleted.getTranslationKey());

		//Depleted Fuel
		GameRegistry.registerItem(waste_natural_uranium, waste_natural_uranium.getTranslationKey());
		GameRegistry.registerItem(waste_uranium, waste_uranium.getTranslationKey());
		GameRegistry.registerItem(waste_thorium, waste_thorium.getTranslationKey());
		GameRegistry.registerItem(waste_mox, waste_mox.getTranslationKey());
		GameRegistry.registerItem(waste_plutonium, waste_plutonium.getTranslationKey());
		GameRegistry.registerItem(waste_u233, waste_u233.getTranslationKey());
		GameRegistry.registerItem(waste_u235, waste_u235.getTranslationKey());
		GameRegistry.registerItem(waste_schrabidium, waste_schrabidium.getTranslationKey());
		GameRegistry.registerItem(waste_zfb_mox, waste_zfb_mox.getTranslationKey());

		GameRegistry.registerItem(waste_plate_u233, waste_plate_u233.getTranslationKey());
		GameRegistry.registerItem(waste_plate_u235, waste_plate_u235.getTranslationKey());
		GameRegistry.registerItem(waste_plate_mox, waste_plate_mox.getTranslationKey());
		GameRegistry.registerItem(waste_plate_pu239, waste_plate_pu239.getTranslationKey());
		GameRegistry.registerItem(waste_plate_ra226be, waste_plate_ra226be.getTranslationKey());
		GameRegistry.registerItem(waste_plate_sa326, waste_plate_sa326.getTranslationKey());
		GameRegistry.registerItem(waste_plate_pu238be, waste_plate_pu238be.getTranslationKey());

		//Pile parts
		GameRegistry.registerItem(pile_rod_uranium, pile_rod_uranium.getTranslationKey());
		GameRegistry.registerItem(pile_rod_pu239, pile_rod_pu239.getTranslationKey());
		GameRegistry.registerItem(pile_rod_plutonium, pile_rod_plutonium.getTranslationKey());
		GameRegistry.registerItem(pile_rod_source, pile_rod_source.getTranslationKey());
		GameRegistry.registerItem(pile_rod_boron, pile_rod_boron.getTranslationKey());
		GameRegistry.registerItem(pile_rod_lithium, pile_rod_lithium.getTranslationKey());
		GameRegistry.registerItem(pile_rod_detector, pile_rod_detector.getTranslationKey());

		//Plate Fuels
		GameRegistry.registerItem(plate_fuel_u233, plate_fuel_u233.getTranslationKey());
		GameRegistry.registerItem(plate_fuel_u235, plate_fuel_u235.getTranslationKey());
		GameRegistry.registerItem(plate_fuel_mox, plate_fuel_mox.getTranslationKey());
		GameRegistry.registerItem(plate_fuel_pu239, plate_fuel_pu239.getTranslationKey());
		GameRegistry.registerItem(plate_fuel_sa326, plate_fuel_sa326.getTranslationKey());
		GameRegistry.registerItem(plate_fuel_ra226be, plate_fuel_ra226be.getTranslationKey());
		GameRegistry.registerItem(plate_fuel_pu238be, plate_fuel_pu238be.getTranslationKey());

		//PWR Parts
		GameRegistry.registerItem(pwr_fuel, pwr_fuel.getTranslationKey());
		GameRegistry.registerItem(pwr_fuel_hot, pwr_fuel_hot.getTranslationKey());
		GameRegistry.registerItem(pwr_fuel_depleted, pwr_fuel_depleted.getTranslationKey());
		GameRegistry.registerItem(pwr_printer, pwr_printer.getTranslationKey());

		//RBMK parts
		GameRegistry.registerItem(rbmk_lid, rbmk_lid.getTranslationKey());
		GameRegistry.registerItem(rbmk_lid_glass, rbmk_lid_glass.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_empty, rbmk_fuel_empty.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_ueu, rbmk_fuel_ueu.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_meu, rbmk_fuel_meu.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_heu233, rbmk_fuel_heu233.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_heu235, rbmk_fuel_heu235.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_uzh, rbmk_fuel_uzh.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_thmeu, rbmk_fuel_thmeu.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_lep, rbmk_fuel_lep.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_mep, rbmk_fuel_mep.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_hep239, rbmk_fuel_hep239.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_hep241, rbmk_fuel_hep241.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_lea, rbmk_fuel_lea.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_mea, rbmk_fuel_mea.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_hea241, rbmk_fuel_hea241.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_hea242, rbmk_fuel_hea242.getTranslationKey());
		GameRegistry.registerItem( rbmk_fuel_bk247,  rbmk_fuel_bk247.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_men, rbmk_fuel_men.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_hen, rbmk_fuel_hen.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_mox, rbmk_fuel_mox.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_les, rbmk_fuel_les.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_mes, rbmk_fuel_mes.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_hes, rbmk_fuel_hes.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_leaus, rbmk_fuel_leaus.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_heaus, rbmk_fuel_heaus.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_po210be, rbmk_fuel_po210be.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_ra226be, rbmk_fuel_ra226be.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_pu238be, rbmk_fuel_pu238be.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_balefire_gold, rbmk_fuel_balefire_gold.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_flashlead, rbmk_fuel_flashlead.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_balefire, rbmk_fuel_balefire.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_zfb_bismuth, rbmk_fuel_zfb_bismuth.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_zfb_pu241, rbmk_fuel_zfb_pu241.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_zfb_am_mix, rbmk_fuel_zfb_am_mix.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_drx, rbmk_fuel_drx.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_test, rbmk_fuel_test.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_lecm, rbmk_fuel_lecm.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_mecm, rbmk_fuel_mecm.getTranslationKey());
		GameRegistry.registerItem(rbmk_fuel_hecm, rbmk_fuel_hecm.getTranslationKey());

		GameRegistry.registerItem(rbmk_pellet_ueu, rbmk_pellet_ueu.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_meu, rbmk_pellet_meu.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_heu233, rbmk_pellet_heu233.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_heu235, rbmk_pellet_heu235.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_uzh, rbmk_pellet_uzh.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_thmeu, rbmk_pellet_thmeu.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_lep, rbmk_pellet_lep.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_mep, rbmk_pellet_mep.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_hep239, rbmk_pellet_hep239.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_hep241, rbmk_pellet_hep241.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_lea, rbmk_pellet_lea.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_mea, rbmk_pellet_mea.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_hea241, rbmk_pellet_hea241.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_hea242, rbmk_pellet_hea242.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_bk247, rbmk_pellet_bk247.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_men, rbmk_pellet_men.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_hen, rbmk_pellet_hen.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_mox, rbmk_pellet_mox.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_les, rbmk_pellet_les.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_mes, rbmk_pellet_mes.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_hes, rbmk_pellet_hes.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_leaus, rbmk_pellet_leaus.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_heaus, rbmk_pellet_heaus.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_po210be, rbmk_pellet_po210be.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_ra226be, rbmk_pellet_ra226be.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_pu238be, rbmk_pellet_pu238be.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_balefire_gold, rbmk_pellet_balefire_gold.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_flashlead, rbmk_pellet_flashlead.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_balefire, rbmk_pellet_balefire.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_zfb_bismuth, rbmk_pellet_zfb_bismuth.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_zfb_pu241, rbmk_pellet_zfb_pu241.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_zfb_am_mix, rbmk_pellet_zfb_am_mix.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_drx, rbmk_pellet_drx.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_lecm, rbmk_pellet_lecm.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_mecm, rbmk_pellet_mecm.getTranslationKey());
		GameRegistry.registerItem(rbmk_pellet_hecm, rbmk_pellet_hecm.getTranslationKey());

		GameRegistry.registerItem(watz_pellet, watz_pellet.getTranslationKey());
		GameRegistry.registerItem(watz_pellet_depleted, watz_pellet_depleted.getTranslationKey());

		GameRegistry.registerItem(icf_pellet_empty, icf_pellet_empty.getTranslationKey());
		GameRegistry.registerItem(icf_pellet, icf_pellet.getTranslationKey());
		GameRegistry.registerItem(icf_pellet_depleted, icf_pellet_depleted.getTranslationKey());

		GameRegistry.registerItem(debris_graphite, debris_graphite.getTranslationKey());
		GameRegistry.registerItem(debris_metal, debris_metal.getTranslationKey());
		GameRegistry.registerItem(debris_fuel, debris_fuel.getTranslationKey());
		GameRegistry.registerItem(debris_concrete, debris_concrete.getTranslationKey());
		GameRegistry.registerItem(debris_exchanger, debris_exchanger.getTranslationKey());
		GameRegistry.registerItem(debris_shrapnel, debris_shrapnel.getTranslationKey());
		GameRegistry.registerItem(debris_element, debris_element.getTranslationKey());
		GameRegistry.registerItem(undefined, undefined.getTranslationKey());

		GameRegistry.registerItem(scrap_plastic, scrap_plastic.getTranslationKey());
		GameRegistry.registerItem(scrap, scrap.getTranslationKey());
		GameRegistry.registerItem(scrap_oil, scrap_oil.getTranslationKey());
		GameRegistry.registerItem(scrap_nuclear, scrap_nuclear.getTranslationKey());
		GameRegistry.registerItem(trinitite, trinitite.getTranslationKey());
		GameRegistry.registerItem(nuclear_waste_long, nuclear_waste_long.getTranslationKey());
		GameRegistry.registerItem(nuclear_waste_long_tiny, nuclear_waste_long_tiny.getTranslationKey());
		GameRegistry.registerItem(nuclear_waste_short, nuclear_waste_short.getTranslationKey());
		GameRegistry.registerItem(nuclear_waste_short_tiny, nuclear_waste_short_tiny.getTranslationKey());
		GameRegistry.registerItem(nuclear_waste_long_depleted, nuclear_waste_long_depleted.getTranslationKey());
		GameRegistry.registerItem(nuclear_waste_long_depleted_tiny, nuclear_waste_long_depleted_tiny.getTranslationKey());
		GameRegistry.registerItem(nuclear_waste_short_depleted, nuclear_waste_short_depleted.getTranslationKey());
		GameRegistry.registerItem(nuclear_waste_short_depleted_tiny, nuclear_waste_short_depleted_tiny.getTranslationKey());
		GameRegistry.registerItem(nuclear_waste, nuclear_waste.getTranslationKey());
		GameRegistry.registerItem(nuclear_waste_tiny, nuclear_waste_tiny.getTranslationKey());
		GameRegistry.registerItem(nuclear_waste_vitrified, nuclear_waste_vitrified.getTranslationKey());
		GameRegistry.registerItem(nuclear_waste_vitrified_tiny, nuclear_waste_vitrified_tiny.getTranslationKey());

		//Spawners
		GameRegistry.registerItem(spawn_chopper, spawn_chopper.getTranslationKey());
		GameRegistry.registerItem(spawn_worm, spawn_worm.getTranslationKey());
		GameRegistry.registerItem(spawn_ufo, spawn_ufo.getTranslationKey());
		GameRegistry.registerItem(spawn_duck, spawn_duck.getTranslationKey());
		GameRegistry.registerItem(spawn_angel, spawn_angel.getTranslationKey());

		//Computer Tools
		GameRegistry.registerItem(rangefinder, rangefinder.getTranslationKey());
		GameRegistry.registerItem(designator, designator.getTranslationKey());
		GameRegistry.registerItem(designator_range, designator_range.getTranslationKey());
		GameRegistry.registerItem(designator_manual, designator_manual.getTranslationKey());
		GameRegistry.registerItem(designator_arty_range, designator_arty_range.getTranslationKey());
		GameRegistry.registerItem(turret_chip, turret_chip.getTranslationKey());
		GameRegistry.registerItem(linker, linker.getTranslationKey());
		GameRegistry.registerItem(reactor_sensor, reactor_sensor.getTranslationKey());
		GameRegistry.registerItem(oil_detector, oil_detector.getTranslationKey());
		GameRegistry.registerItem(ore_density_scanner, ore_density_scanner.getTranslationKey());
		GameRegistry.registerItem(survey_scanner, survey_scanner.getTranslationKey());
		GameRegistry.registerItem(mirror_tool, mirror_tool.getTranslationKey());
		GameRegistry.registerItem(rbmk_tool, rbmk_tool.getTranslationKey());
		GameRegistry.registerItem(drone_linker, drone_linker.getTranslationKey());
		GameRegistry.registerItem(radar_linker, radar_linker.getTranslationKey());
		GameRegistry.registerItem(coltan_tool, coltan_tool.getTranslationKey());
		GameRegistry.registerItem(power_net_tool, power_net_tool.getTranslationKey());
		GameRegistry.registerItem(analysis_tool, analysis_tool.getTranslationKey());
		GameRegistry.registerItem(coupling_tool, coupling_tool.getTranslationKey());
		GameRegistry.registerItem(settings_tool, settings_tool.getTranslationKey());
		GameRegistry.registerItem(dosimeter, dosimeter.getTranslationKey());
		GameRegistry.registerItem(geiger_counter, geiger_counter.getTranslationKey());
		GameRegistry.registerItem(digamma_diagnostic, digamma_diagnostic.getTranslationKey());
		GameRegistry.registerItem(pollution_detector, pollution_detector.getTranslationKey());
		GameRegistry.registerItem(containment_box, containment_box.getTranslationKey());
		GameRegistry.registerItem(plastic_bag, plastic_bag.getTranslationKey());
		GameRegistry.registerItem(atmosphere_scanner, atmosphere_scanner.getTranslationKey());

		GameRegistry.registerItem(ammo_bag, ammo_bag.getTranslationKey());
		GameRegistry.registerItem(ammo_bag_infinite, ammo_bag_infinite.getTranslationKey());
		GameRegistry.registerItem(casing_bag, casing_bag.getTranslationKey());

		//Keys and Locks
		GameRegistry.registerItem(key, key.getTranslationKey());
		GameRegistry.registerItem(key_red, key_red.getTranslationKey());
		GameRegistry.registerItem(key_red_cracked, key_red_cracked.getTranslationKey());
		GameRegistry.registerItem(key_kit, key_kit.getTranslationKey());
		GameRegistry.registerItem(key_fake, key_fake.getTranslationKey());
		GameRegistry.registerItem(mech_key, mech_key.getTranslationKey());
		GameRegistry.registerItem(pin, pin.getTranslationKey());
		GameRegistry.registerItem(padlock_rusty, padlock_rusty.getTranslationKey());
		GameRegistry.registerItem(padlock, padlock.getTranslationKey());
		GameRegistry.registerItem(padlock_reinforced, padlock_reinforced.getTranslationKey());
		GameRegistry.registerItem(padlock_unbreakable, padlock_unbreakable.getTranslationKey());
		GameRegistry.registerItem(launch_code_piece, launch_code_piece.getTranslationKey());
		GameRegistry.registerItem(launch_code, launch_code.getTranslationKey());
		GameRegistry.registerItem(launch_key, launch_key.getTranslationKey());

		//Missiles
		//Tier 0
		GameRegistry.registerItem(missile_test, missile_test.getTranslationKey());
		GameRegistry.registerItem(missile_taint, missile_taint.getTranslationKey());
		GameRegistry.registerItem(missile_micro, missile_micro.getTranslationKey());
		GameRegistry.registerItem(missile_bhole, missile_bhole.getTranslationKey());
		GameRegistry.registerItem(missile_schrabidium, missile_schrabidium.getTranslationKey());
		GameRegistry.registerItem(missile_emp, missile_emp.getTranslationKey());
		//Tier 1
		GameRegistry.registerItem(missile_generic, missile_generic.getTranslationKey());
		GameRegistry.registerItem(missile_decoy, missile_decoy.getTranslationKey());
		GameRegistry.registerItem(missile_incendiary, missile_incendiary.getTranslationKey());
		GameRegistry.registerItem(missile_cluster, missile_cluster.getTranslationKey());
		GameRegistry.registerItem(missile_buster, missile_buster.getTranslationKey());
		GameRegistry.registerItem(missile_stealth, missile_stealth.getTranslationKey());
		GameRegistry.registerItem(missile_anti_ballistic, missile_anti_ballistic.getTranslationKey());
		//Tier 2
		GameRegistry.registerItem(missile_strong, missile_strong.getTranslationKey());
		GameRegistry.registerItem(missile_incendiary_strong, missile_incendiary_strong.getTranslationKey());
		GameRegistry.registerItem(missile_cluster_strong, missile_cluster_strong.getTranslationKey());
		GameRegistry.registerItem(missile_buster_strong, missile_buster_strong.getTranslationKey());
		GameRegistry.registerItem(missile_emp_strong, missile_emp_strong.getTranslationKey());
		//Tier 3
		GameRegistry.registerItem(missile_burst, missile_burst.getTranslationKey());
		GameRegistry.registerItem(missile_inferno, missile_inferno.getTranslationKey());
		GameRegistry.registerItem(missile_rain, missile_rain.getTranslationKey());
		GameRegistry.registerItem(missile_drill, missile_drill.getTranslationKey());
		GameRegistry.registerItem(missile_shuttle, missile_shuttle.getTranslationKey());
		//Tier 4
		GameRegistry.registerItem(missile_nuclear, missile_nuclear.getTranslationKey());
		GameRegistry.registerItem(missile_nuclear_cluster, missile_nuclear_cluster.getTranslationKey());
		GameRegistry.registerItem(missile_volcano, missile_volcano.getTranslationKey());
		GameRegistry.registerItem(missile_doomsday, missile_doomsday.getTranslationKey());
		GameRegistry.registerItem(missile_doomsday_rusted, missile_doomsday_rusted.getTranslationKey());
		//Rockets
		GameRegistry.registerItem(missile_soyuz, missile_soyuz.getTranslationKey());
		GameRegistry.registerItem(missile_soyuz_lander, missile_soyuz_lander.getTranslationKey());
		GameRegistry.registerItem(missile_custom, missile_custom.getTranslationKey());
		GameRegistry.registerItem(rocket_custom, rocket_custom.getTranslationKey());

		//Missile Parts
		GameRegistry.registerItem(mp_thruster_10_kerosene, mp_thruster_10_kerosene.getTranslationKey());
		GameRegistry.registerItem(mp_thruster_10_solid, mp_thruster_10_solid.getTranslationKey());
		GameRegistry.registerItem(mp_thruster_10_hydrazine, mp_thruster_10_hydrazine.getTranslationKey());
		GameRegistry.registerItem(mp_thruster_10_xenon, mp_thruster_10_xenon.getTranslationKey());
		GameRegistry.registerItem(mp_thruster_15_kerosene, mp_thruster_15_kerosene.getTranslationKey());
		GameRegistry.registerItem(mp_thruster_15_kerosene_dual, mp_thruster_15_kerosene_dual.getTranslationKey());
		GameRegistry.registerItem(mp_thruster_15_kerosene_triple, mp_thruster_15_kerosene_triple.getTranslationKey());
		GameRegistry.registerItem(mp_thruster_15_solid, mp_thruster_15_solid.getTranslationKey());
		GameRegistry.registerItem(mp_thruster_15_solid_hexdecuple, mp_thruster_15_solid_hexdecuple.getTranslationKey());
		GameRegistry.registerItem(mp_thruster_15_hydrogen, mp_thruster_15_hydrogen.getTranslationKey());
		GameRegistry.registerItem(mp_thruster_15_hydrogen_dual, mp_thruster_15_hydrogen_dual.getTranslationKey());
		GameRegistry.registerItem(mp_thruster_15_balefire_short, mp_thruster_15_balefire_short.getTranslationKey());
		GameRegistry.registerItem(mp_thruster_15_balefire, mp_thruster_15_balefire.getTranslationKey());
		GameRegistry.registerItem(mp_thruster_15_balefire_large, mp_thruster_15_balefire_large.getTranslationKey());
		GameRegistry.registerItem(mp_thruster_15_balefire_large_rad, mp_thruster_15_balefire_large_rad.getTranslationKey());
		GameRegistry.registerItem(mp_thruster_20_kerosene, mp_thruster_20_kerosene.getTranslationKey());
		GameRegistry.registerItem(mp_thruster_20_kerosene_dual, mp_thruster_20_kerosene_dual.getTranslationKey());
		GameRegistry.registerItem(mp_thruster_20_kerosene_triple, mp_thruster_20_kerosene_triple.getTranslationKey());
		GameRegistry.registerItem(mp_thruster_20_methalox, mp_thruster_20_methalox.getTranslationKey());
		GameRegistry.registerItem(mp_thruster_20_methalox_dual, mp_thruster_20_methalox_dual.getTranslationKey());
		GameRegistry.registerItem(mp_thruster_20_methalox_triple, mp_thruster_20_methalox_triple.getTranslationKey());
		GameRegistry.registerItem(mp_thruster_20_hydrogen, mp_thruster_20_hydrogen.getTranslationKey());
		GameRegistry.registerItem(mp_thruster_20_hydrogen_dual, mp_thruster_20_hydrogen_dual.getTranslationKey());
		GameRegistry.registerItem(mp_thruster_20_hydrogen_triple, mp_thruster_20_hydrogen_triple.getTranslationKey());
		GameRegistry.registerItem(mp_thruster_20_solid, mp_thruster_20_solid.getTranslationKey());
		GameRegistry.registerItem(mp_thruster_20_solid_multi, mp_thruster_20_solid_multi.getTranslationKey());
		GameRegistry.registerItem(mp_thruster_20_solid_multier, mp_thruster_20_solid_multier.getTranslationKey());
		GameRegistry.registerItem(mp_thruster_20_hydrazine, mp_thruster_20_hydrazine.getTranslationKey());

		GameRegistry.registerItem(mp_stability_10_flat, mp_stability_10_flat.getTranslationKey());
		GameRegistry.registerItem(mp_stability_10_cruise, mp_stability_10_cruise.getTranslationKey());
		GameRegistry.registerItem(mp_stability_10_space, mp_stability_10_space.getTranslationKey());
		GameRegistry.registerItem(mp_stability_15_flat, mp_stability_15_flat.getTranslationKey());
		GameRegistry.registerItem(mp_stability_15_thin, mp_stability_15_thin.getTranslationKey());
		GameRegistry.registerItem(mp_stability_15_soyuz, mp_stability_15_soyuz.getTranslationKey());
		GameRegistry.registerItem(mp_stability_20_flat, mp_stability_20_flat.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_kerosene, mp_fuselage_10_kerosene.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_kerosene_camo, mp_fuselage_10_kerosene_camo.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_kerosene_desert, mp_fuselage_10_kerosene_desert.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_kerosene_sky, mp_fuselage_10_kerosene_sky.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_kerosene_flames, mp_fuselage_10_kerosene_flames.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_kerosene_insulation, mp_fuselage_10_kerosene_insulation.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_kerosene_sleek, mp_fuselage_10_kerosene_sleek.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_kerosene_metal, mp_fuselage_10_kerosene_metal.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_kerosene_taint, mp_fuselage_10_kerosene_taint.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_solid, mp_fuselage_10_solid.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_hydrazine, mp_fuselage_10_hydrazine.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_solid_flames, mp_fuselage_10_solid_flames.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_solid_insulation, mp_fuselage_10_solid_insulation.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_solid_sleek, mp_fuselage_10_solid_sleek.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_solid_soviet_glory, mp_fuselage_10_solid_soviet_glory.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_solid_cathedral, mp_fuselage_10_solid_cathedral.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_solid_moonlit, mp_fuselage_10_solid_moonlit.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_solid_battery, mp_fuselage_10_solid_battery.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_solid_duracell, mp_fuselage_10_solid_duracell.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_xenon, mp_fuselage_10_xenon.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_xenon_bhole, mp_fuselage_10_xenon_bhole.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_long_kerosene, mp_fuselage_10_long_kerosene.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_long_kerosene_camo, mp_fuselage_10_long_kerosene_camo.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_long_kerosene_desert, mp_fuselage_10_long_kerosene_desert.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_long_kerosene_sky, mp_fuselage_10_long_kerosene_sky.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_long_kerosene_flames, mp_fuselage_10_long_kerosene_flames.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_long_kerosene_insulation, mp_fuselage_10_long_kerosene_insulation.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_long_kerosene_sleek, mp_fuselage_10_long_kerosene_sleek.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_long_kerosene_metal, mp_fuselage_10_long_kerosene_metal.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_long_kerosene_dash, mp_fuselage_10_long_kerosene_dash.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_long_kerosene_taint, mp_fuselage_10_long_kerosene_taint.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_long_kerosene_vap, mp_fuselage_10_long_kerosene_vap.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_long_solid, mp_fuselage_10_long_solid.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_long_solid_flames, mp_fuselage_10_long_solid_flames.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_long_solid_insulation, mp_fuselage_10_long_solid_insulation.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_long_solid_sleek, mp_fuselage_10_long_solid_sleek.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_long_solid_soviet_glory, mp_fuselage_10_long_solid_soviet_glory.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_long_solid_bullet, mp_fuselage_10_long_solid_bullet.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_long_solid_silvermoonlight, mp_fuselage_10_long_solid_silvermoonlight.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_15_kerosene, mp_fuselage_10_15_kerosene.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_15_solid, mp_fuselage_10_15_solid.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_15_hydrogen, mp_fuselage_10_15_hydrogen.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_10_15_balefire, mp_fuselage_10_15_balefire.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_kerosene, mp_fuselage_15_kerosene.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_kerosene_camo, mp_fuselage_15_kerosene_camo.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_kerosene_desert, mp_fuselage_15_kerosene_desert.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_kerosene_sky, mp_fuselage_15_kerosene_sky.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_kerosene_insulation, mp_fuselage_15_kerosene_insulation.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_kerosene_metal, mp_fuselage_15_kerosene_metal.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_kerosene_decorated, mp_fuselage_15_kerosene_decorated.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_kerosene_steampunk, mp_fuselage_15_kerosene_steampunk.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_kerosene_polite, mp_fuselage_15_kerosene_polite.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_kerosene_blackjack, mp_fuselage_15_kerosene_blackjack.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_kerosene_lambda, mp_fuselage_15_kerosene_lambda.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_kerosene_minuteman, mp_fuselage_15_kerosene_minuteman.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_kerosene_pip, mp_fuselage_15_kerosene_pip.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_kerosene_taint, mp_fuselage_15_kerosene_taint.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_kerosene_yuck, mp_fuselage_15_kerosene_yuck.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_solid, mp_fuselage_15_solid.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_solid_insulation, mp_fuselage_15_solid_insulation.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_solid_desh, mp_fuselage_15_solid_desh.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_solid_soviet_glory, mp_fuselage_15_solid_soviet_glory.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_solid_soviet_stank, mp_fuselage_15_solid_soviet_stank.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_solid_faust, mp_fuselage_15_solid_faust.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_solid_silvermoonlight, mp_fuselage_15_solid_silvermoonlight.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_solid_snowy, mp_fuselage_15_solid_snowy.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_solid_panorama, mp_fuselage_15_solid_panorama.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_solid_roses, mp_fuselage_15_solid_roses.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_solid_mimi, mp_fuselage_15_solid_mimi.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_hydrogen, mp_fuselage_15_hydrogen.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_hydrogen_cathedral, mp_fuselage_15_hydrogen_cathedral.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_balefire, mp_fuselage_15_balefire.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_20_kerosene, mp_fuselage_15_20_kerosene.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_20_kerosene_magnusson, mp_fuselage_15_20_kerosene_magnusson.getTranslationKey());
		GameRegistry.registerItem(mp_fuselage_15_20_solid, mp_fuselage_15_20_solid.getTranslationKey());
		GameRegistry.registerItem(mp_warhead_10_he, mp_warhead_10_he.getTranslationKey());
		GameRegistry.registerItem(mp_warhead_10_incendiary, mp_warhead_10_incendiary.getTranslationKey());
		GameRegistry.registerItem(mp_warhead_10_buster, mp_warhead_10_buster.getTranslationKey());
		GameRegistry.registerItem(mp_warhead_10_nuclear, mp_warhead_10_nuclear.getTranslationKey());
		GameRegistry.registerItem(mp_warhead_10_nuclear_large, mp_warhead_10_nuclear_large.getTranslationKey());
		GameRegistry.registerItem(mp_warhead_10_taint, mp_warhead_10_taint.getTranslationKey());
		GameRegistry.registerItem(mp_warhead_10_cloud, mp_warhead_10_cloud.getTranslationKey());
		GameRegistry.registerItem(mp_warhead_15_he, mp_warhead_15_he.getTranslationKey());
		GameRegistry.registerItem(mp_warhead_15_incendiary, mp_warhead_15_incendiary.getTranslationKey());
		GameRegistry.registerItem(mp_warhead_15_nuclear, mp_warhead_15_nuclear.getTranslationKey());
		GameRegistry.registerItem(mp_warhead_15_nuclear_shark, mp_warhead_15_nuclear_shark.getTranslationKey());
		GameRegistry.registerItem(mp_warhead_15_nuclear_mimi, mp_warhead_15_nuclear_mimi.getTranslationKey());
		GameRegistry.registerItem(mp_warhead_15_boxcar, mp_warhead_15_boxcar.getTranslationKey());
		GameRegistry.registerItem(mp_warhead_15_n2, mp_warhead_15_n2.getTranslationKey());
		GameRegistry.registerItem(mp_warhead_15_balefire, mp_warhead_15_balefire.getTranslationKey());
		GameRegistry.registerItem(mp_warhead_15_turbine, mp_warhead_15_turbine.getTranslationKey());
		GameRegistry.registerItem(mp_chip_1, mp_chip_1.getTranslationKey());
		GameRegistry.registerItem(mp_chip_2, mp_chip_2.getTranslationKey());
		GameRegistry.registerItem(mp_chip_3, mp_chip_3.getTranslationKey());
		GameRegistry.registerItem(mp_chip_4, mp_chip_4.getTranslationKey());
		GameRegistry.registerItem(mp_chip_5, mp_chip_5.getTranslationKey());

		GameRegistry.registerItem(rp_fuselage_20_12, rp_fuselage_20_12.getTranslationKey());
		GameRegistry.registerItem(rp_fuselage_20_6, rp_fuselage_20_6.getTranslationKey());
		GameRegistry.registerItem(rp_fuselage_20_3, rp_fuselage_20_3.getTranslationKey());
		GameRegistry.registerItem(rp_fuselage_20_1, rp_fuselage_20_1.getTranslationKey());
		GameRegistry.registerItem(rp_fuselage_20_12_hydrazine, rp_fuselage_20_12_hydrazine.getTranslationKey());
		GameRegistry.registerItem(rp_legs_20, rp_legs_20.getTranslationKey());
		GameRegistry.registerItem(rp_capsule_20, rp_capsule_20.getTranslationKey());
		GameRegistry.registerItem(rp_station_core_20, rp_station_core_20.getTranslationKey());
		GameRegistry.registerItem(rp_pod_20, rp_pod_20.getTranslationKey());

		//Satellites
		GameRegistry.registerItem(sat_mapper, sat_mapper.getTranslationKey());
		GameRegistry.registerItem(sat_scanner, sat_scanner.getTranslationKey());
		GameRegistry.registerItem(sat_radar, sat_radar.getTranslationKey());
		GameRegistry.registerItem(sat_laser, sat_laser.getTranslationKey());
		GameRegistry.registerItem(sat_foeq, sat_foeq.getTranslationKey());
		GameRegistry.registerItem(sat_resonator, sat_resonator.getTranslationKey());
		GameRegistry.registerItem(sat_miner, sat_miner.getTranslationKey());
		GameRegistry.registerItem(sat_lunar_miner, sat_lunar_miner.getTranslationKey());
		GameRegistry.registerItem(sat_gerald, sat_gerald.getTranslationKey());
		GameRegistry.registerItem(sat_dyson_relay, sat_dyson_relay.getTranslationKey());
		GameRegistry.registerItem(sat_chip, sat_chip.getTranslationKey());
		GameRegistry.registerItem(sat_interface, sat_interface.getTranslationKey());
		GameRegistry.registerItem(sat_coord, sat_coord.getTranslationKey());
		GameRegistry.registerItem(sat_designator, sat_designator.getTranslationKey());
		GameRegistry.registerItem(sat_relay, sat_relay.getTranslationKey());
		GameRegistry.registerItem(sat_war, sat_war.getTranslationKey());

		//Guns
		GameRegistry.registerItem(gun_b92, gun_b92.getTranslationKey());
		GameRegistry.registerItem(crucible, crucible.getTranslationKey());

		GameRegistry.registerItem(gun_debug, gun_debug.getTranslationKey());
		GameRegistry.registerItem(ammo_debug, ammo_debug.getTranslationKey());

		GameRegistry.registerItem(gun_pepperbox, gun_pepperbox.getTranslationKey());
		GameRegistry.registerItem(gun_light_revolver, gun_light_revolver.getTranslationKey());
		GameRegistry.registerItem(gun_light_revolver_atlas, gun_light_revolver_atlas.getTranslationKey());
		GameRegistry.registerItem(gun_light_revolver_dani, gun_light_revolver_dani.getTranslationKey());
		GameRegistry.registerItem(gun_henry, gun_henry.getTranslationKey());
		GameRegistry.registerItem(gun_henry_lincoln, gun_henry_lincoln.getTranslationKey());
		GameRegistry.registerItem(gun_greasegun, gun_greasegun.getTranslationKey());
		GameRegistry.registerItem(gun_maresleg, gun_maresleg.getTranslationKey());
		GameRegistry.registerItem(gun_maresleg_akimbo, gun_maresleg_akimbo.getTranslationKey());
		GameRegistry.registerItem(gun_maresleg_broken, gun_maresleg_broken.getTranslationKey());
		GameRegistry.registerItem(gun_flaregun, gun_flaregun.getTranslationKey());
		GameRegistry.registerItem(gun_heavy_revolver, gun_heavy_revolver.getTranslationKey());
		GameRegistry.registerItem(gun_heavy_revolver_lilmac, gun_heavy_revolver_lilmac.getTranslationKey());
		GameRegistry.registerItem(gun_heavy_revolver_protege, gun_heavy_revolver_protege.getTranslationKey());
		GameRegistry.registerItem(gun_carbine, gun_carbine.getTranslationKey());
		GameRegistry.registerItem(gun_am180, gun_am180.getTranslationKey());
		GameRegistry.registerItem(gun_liberator, gun_liberator.getTranslationKey());
		GameRegistry.registerItem(gun_congolake, gun_congolake.getTranslationKey());
		GameRegistry.registerItem(gun_flamer, gun_flamer.getTranslationKey());
		GameRegistry.registerItem(gun_flamer_topaz, gun_flamer_topaz.getTranslationKey());
		GameRegistry.registerItem(gun_flamer_daybreaker, gun_flamer_daybreaker.getTranslationKey());
		GameRegistry.registerItem(gun_uzi, gun_uzi.getTranslationKey());
		GameRegistry.registerItem(gun_uzi_akimbo, gun_uzi_akimbo.getTranslationKey());
		GameRegistry.registerItem(gun_spas12, gun_spas12.getTranslationKey());
		GameRegistry.registerItem(gun_panzerschreck, gun_panzerschreck.getTranslationKey());
		GameRegistry.registerItem(gun_star_f, gun_star_f.getTranslationKey());
		GameRegistry.registerItem(gun_star_f_akimbo, gun_star_f_akimbo.getTranslationKey());
		GameRegistry.registerItem(gun_g3, gun_g3.getTranslationKey());
		GameRegistry.registerItem(gun_g3_zebra, gun_g3_zebra.getTranslationKey());
		GameRegistry.registerItem(gun_stinger, gun_stinger.getTranslationKey());
		GameRegistry.registerItem(gun_chemthrower, gun_chemthrower.getTranslationKey());
		GameRegistry.registerItem(gun_amat, gun_amat.getTranslationKey());
		GameRegistry.registerItem(gun_amat_subtlety, gun_amat_subtlety.getTranslationKey());
		GameRegistry.registerItem(gun_amat_penance, gun_amat_penance.getTranslationKey());
		GameRegistry.registerItem(gun_m2, gun_m2.getTranslationKey());
		GameRegistry.registerItem(gun_autoshotgun, gun_autoshotgun.getTranslationKey());
		GameRegistry.registerItem(gun_autoshotgun_shredder, gun_autoshotgun_shredder.getTranslationKey());
		GameRegistry.registerItem(gun_autoshotgun_sexy, gun_autoshotgun_sexy.getTranslationKey());
		GameRegistry.registerItem(gun_autoshotgun_heretic, gun_autoshotgun_heretic.getTranslationKey());
		GameRegistry.registerItem(gun_quadro, gun_quadro.getTranslationKey());
		GameRegistry.registerItem(gun_lag, gun_lag.getTranslationKey());
		GameRegistry.registerItem(gun_minigun, gun_minigun.getTranslationKey());
		GameRegistry.registerItem(gun_minigun_dual, gun_minigun_dual.getTranslationKey());
		GameRegistry.registerItem(gun_minigun_lacunae, gun_minigun_lacunae.getTranslationKey());
		GameRegistry.registerItem(gun_missile_launcher, gun_missile_launcher.getTranslationKey());
		GameRegistry.registerItem(gun_tesla_cannon, gun_tesla_cannon.getTranslationKey());
		GameRegistry.registerItem(gun_laser_pistol, gun_laser_pistol.getTranslationKey());
		GameRegistry.registerItem(gun_laser_pistol_pew_pew, gun_laser_pistol_pew_pew.getTranslationKey());
		GameRegistry.registerItem(gun_laser_pistol_morning_glory, gun_laser_pistol_morning_glory.getTranslationKey());
		GameRegistry.registerItem(gun_stg77, gun_stg77.getTranslationKey());
		GameRegistry.registerItem(gun_tau, gun_tau.getTranslationKey());
		GameRegistry.registerItem(gun_fatman, gun_fatman.getTranslationKey());
		GameRegistry.registerItem(gun_lasrifle, gun_lasrifle.getTranslationKey());
		GameRegistry.registerItem(gun_coilgun, gun_coilgun.getTranslationKey());
		GameRegistry.registerItem(gun_hangman, gun_hangman.getTranslationKey());
		GameRegistry.registerItem(gun_mas36, gun_mas36.getTranslationKey());
		GameRegistry.registerItem(gun_bolter, gun_bolter.getTranslationKey());
		GameRegistry.registerItem(gun_folly, gun_folly.getTranslationKey());
		GameRegistry.registerItem(gun_aberrator, gun_aberrator.getTranslationKey());
		GameRegistry.registerItem(gun_aberrator_eott, gun_aberrator_eott.getTranslationKey());
		GameRegistry.registerItem(gun_double_barrel, gun_double_barrel.getTranslationKey());
		GameRegistry.registerItem(gun_double_barrel_sacred_dragon, gun_double_barrel_sacred_dragon.getTranslationKey());
		GameRegistry.registerItem(gun_n_i_4_n_i, gun_n_i_4_n_i.getTranslationKey());

		GameRegistry.registerItem(gun_fireext, gun_fireext.getTranslationKey());
		GameRegistry.registerItem(gun_charge_thrower, gun_charge_thrower.getTranslationKey());
		GameRegistry.registerItem(gun_drill, gun_drill.getTranslationKey());
		GameRegistry.registerItem(gun_pa_melee, gun_pa_melee.getTranslationKey());
		GameRegistry.registerItem(gun_pa_ranged, gun_pa_ranged.getTranslationKey());

		GameRegistry.registerItem(ammo_standard, ammo_standard.getTranslationKey());
		GameRegistry.registerItem(ammo_secret, ammo_secret.getTranslationKey());

		GameRegistry.registerItem(weapon_mod_test, weapon_mod_test.getTranslationKey());
		GameRegistry.registerItem(weapon_mod_generic, weapon_mod_generic.getTranslationKey());
		GameRegistry.registerItem(weapon_mod_special, weapon_mod_special.getTranslationKey());
		GameRegistry.registerItem(weapon_mod_caliber, weapon_mod_caliber.getTranslationKey());

		//Ammo
		GameRegistry.registerItem(gun_b92_ammo, gun_b92_ammo.getTranslationKey());

		GameRegistry.registerItem(ammo_fireext, ammo_fireext.getTranslationKey());
		GameRegistry.registerItem(ammo_shell, ammo_shell.getTranslationKey());
		GameRegistry.registerItem(ammo_dgk, ammo_dgk.getTranslationKey());
		GameRegistry.registerItem(ammo_arty, ammo_arty.getTranslationKey());
		GameRegistry.registerItem(ammo_himars, ammo_himars.getTranslationKey());

		GameRegistry.registerItem(ammo_container, ammo_container.getTranslationKey());

		//Grenades
		GameRegistry.registerItem(stick_dynamite, stick_dynamite.getTranslationKey()); //heave-ho!
		GameRegistry.registerItem(stick_dynamite_fishing, stick_dynamite_fishing.getTranslationKey());
		GameRegistry.registerItem(stick_tnt, stick_tnt.getTranslationKey());
		GameRegistry.registerItem(stick_semtex, stick_semtex.getTranslationKey());
		GameRegistry.registerItem(stick_c4, stick_c4.getTranslationKey());
		GameRegistry.registerItem(grenade_generic, grenade_generic.getTranslationKey());
		GameRegistry.registerItem(grenade_strong, grenade_strong.getTranslationKey());
		GameRegistry.registerItem(grenade_frag, grenade_frag.getTranslationKey());
		GameRegistry.registerItem(grenade_fire, grenade_fire.getTranslationKey());
		GameRegistry.registerItem(grenade_shrapnel, grenade_shrapnel.getTranslationKey());
		GameRegistry.registerItem(grenade_cluster, grenade_cluster.getTranslationKey());
		GameRegistry.registerItem(grenade_flare, grenade_flare.getTranslationKey());
		GameRegistry.registerItem(grenade_electric, grenade_electric.getTranslationKey());
		GameRegistry.registerItem(grenade_poison, grenade_poison.getTranslationKey());
		GameRegistry.registerItem(grenade_gas, grenade_gas.getTranslationKey());
		GameRegistry.registerItem(grenade_cloud, grenade_cloud.getTranslationKey());
		GameRegistry.registerItem(grenade_pink_cloud, grenade_pink_cloud.getTranslationKey());
		GameRegistry.registerItem(grenade_smart, grenade_smart.getTranslationKey());
		GameRegistry.registerItem(grenade_mirv, grenade_mirv.getTranslationKey());
		GameRegistry.registerItem(grenade_breach, grenade_breach.getTranslationKey());
		GameRegistry.registerItem(grenade_burst, grenade_burst.getTranslationKey());
		GameRegistry.registerItem(grenade_pulse, grenade_pulse.getTranslationKey());
		GameRegistry.registerItem(grenade_plasma, grenade_plasma.getTranslationKey());
		GameRegistry.registerItem(grenade_tau, grenade_tau.getTranslationKey());
		GameRegistry.registerItem(grenade_schrabidium, grenade_schrabidium.getTranslationKey());
		GameRegistry.registerItem(grenade_nuke, grenade_nuke.getTranslationKey());
		GameRegistry.registerItem(grenade_lemon, grenade_lemon.getTranslationKey());
		GameRegistry.registerItem(grenade_gascan, grenade_gascan.getTranslationKey());
		GameRegistry.registerItem(grenade_kyiv, grenade_kyiv.getTranslationKey());
		GameRegistry.registerItem(grenade_mk2, grenade_mk2.getTranslationKey());
		GameRegistry.registerItem(grenade_aschrab, grenade_aschrab.getTranslationKey());
		GameRegistry.registerItem(grenade_nuclear, grenade_nuclear.getTranslationKey());
		GameRegistry.registerItem(grenade_zomg, grenade_zomg.getTranslationKey());
		GameRegistry.registerItem(grenade_black_hole, grenade_black_hole.getTranslationKey());

		GameRegistry.registerItem(grenade_if_generic, grenade_if_generic.getTranslationKey());
		GameRegistry.registerItem(grenade_if_he, grenade_if_he.getTranslationKey());
		GameRegistry.registerItem(grenade_if_bouncy, grenade_if_bouncy.getTranslationKey());
		GameRegistry.registerItem(grenade_if_sticky, grenade_if_sticky.getTranslationKey());
		GameRegistry.registerItem(grenade_if_impact, grenade_if_impact.getTranslationKey());
		GameRegistry.registerItem(grenade_if_incendiary, grenade_if_incendiary.getTranslationKey());
		GameRegistry.registerItem(grenade_if_toxic, grenade_if_toxic.getTranslationKey());
		GameRegistry.registerItem(grenade_if_concussion, grenade_if_concussion.getTranslationKey());
		GameRegistry.registerItem(grenade_if_brimstone, grenade_if_brimstone.getTranslationKey());
		GameRegistry.registerItem(grenade_if_mystery, grenade_if_mystery.getTranslationKey());
		GameRegistry.registerItem(grenade_if_spark, grenade_if_spark.getTranslationKey());
		GameRegistry.registerItem(grenade_if_hopwire, grenade_if_hopwire.getTranslationKey());
		GameRegistry.registerItem(grenade_if_null, grenade_if_null.getTranslationKey());
		GameRegistry.registerItem(nuclear_waste_pearl, nuclear_waste_pearl.getTranslationKey());

		//Disperser Canister
		GameRegistry.registerItem(disperser_canister_empty, disperser_canister_empty.getTranslationKey());
		GameRegistry.registerItem(disperser_canister, disperser_canister.getTranslationKey());
		GameRegistry.registerItem(glyphid_gland_empty, glyphid_gland_empty.getTranslationKey());
		GameRegistry.registerItem(glyphid_gland, glyphid_gland.getTranslationKey());

		GameRegistry.registerItem(ullapool_caber, ullapool_caber.getTranslationKey());
		GameRegistry.registerItem(weaponized_starblaster_cell, weaponized_starblaster_cell.getTranslationKey());

		//Capes
		GameRegistry.registerItem(cape_radiation, cape_radiation.getTranslationKey());
		GameRegistry.registerItem(cape_gasmask, cape_gasmask.getTranslationKey());
		GameRegistry.registerItem(cape_schrabidium, cape_schrabidium.getTranslationKey());
		GameRegistry.registerItem(cape_hidden, cape_hidden.getTranslationKey());

		//Tools
		GameRegistry.registerItem(dwarven_pickaxe, dwarven_pickaxe.getTranslationKey());
		GameRegistry.registerItem(schrabidium_sword, schrabidium_sword.getTranslationKey());
		GameRegistry.registerItem(schrabidium_hammer, schrabidium_hammer.getTranslationKey());
		GameRegistry.registerItem(shimmer_sledge, shimmer_sledge.getTranslationKey());
		GameRegistry.registerItem(shimmer_axe, shimmer_axe.getTranslationKey());
		GameRegistry.registerItem(wood_gavel, wood_gavel.getTranslationKey());
		GameRegistry.registerItem(lead_gavel, lead_gavel.getTranslationKey());
		GameRegistry.registerItem(diamond_gavel, diamond_gavel.getTranslationKey());
		GameRegistry.registerItem(mese_gavel, mese_gavel.getTranslationKey());
		GameRegistry.registerItem(schrabidium_pickaxe, schrabidium_pickaxe.getTranslationKey());
		GameRegistry.registerItem(schrabidium_axe, schrabidium_axe.getTranslationKey());
		GameRegistry.registerItem(schrabidium_shovel, schrabidium_shovel.getTranslationKey());
		GameRegistry.registerItem(schrabidium_hoe, schrabidium_hoe.getTranslationKey());
		GameRegistry.registerItem(steel_sword, steel_sword.getTranslationKey());
		GameRegistry.registerItem(steel_pickaxe, steel_pickaxe.getTranslationKey());
		GameRegistry.registerItem(steel_axe, steel_axe.getTranslationKey());
		GameRegistry.registerItem(steel_shovel, steel_shovel.getTranslationKey());
		GameRegistry.registerItem(steel_hoe, steel_hoe.getTranslationKey());
		GameRegistry.registerItem(titanium_sword, titanium_sword.getTranslationKey());
		GameRegistry.registerItem(titanium_pickaxe, titanium_pickaxe.getTranslationKey());
		GameRegistry.registerItem(titanium_axe, titanium_axe.getTranslationKey());
		GameRegistry.registerItem(titanium_shovel, titanium_shovel.getTranslationKey());
		GameRegistry.registerItem(titanium_hoe, titanium_hoe.getTranslationKey());
		GameRegistry.registerItem(cobalt_sword, cobalt_sword.getTranslationKey());
		GameRegistry.registerItem(cobalt_pickaxe, cobalt_pickaxe.getTranslationKey());
		GameRegistry.registerItem(cobalt_axe, cobalt_axe.getTranslationKey());
		GameRegistry.registerItem(cobalt_shovel, cobalt_shovel.getTranslationKey());
		GameRegistry.registerItem(cobalt_hoe, cobalt_hoe.getTranslationKey());
		GameRegistry.registerItem(cobalt_decorated_sword, cobalt_decorated_sword.getTranslationKey());
		GameRegistry.registerItem(cobalt_decorated_pickaxe, cobalt_decorated_pickaxe.getTranslationKey());
		GameRegistry.registerItem(cobalt_decorated_axe, cobalt_decorated_axe.getTranslationKey());
		GameRegistry.registerItem(cobalt_decorated_shovel, cobalt_decorated_shovel.getTranslationKey());
		GameRegistry.registerItem(cobalt_decorated_hoe, cobalt_decorated_hoe.getTranslationKey());
		GameRegistry.registerItem(starmetal_sword, starmetal_sword.getTranslationKey());
		GameRegistry.registerItem(starmetal_pickaxe, starmetal_pickaxe.getTranslationKey());
		GameRegistry.registerItem(starmetal_axe, starmetal_axe.getTranslationKey());
		GameRegistry.registerItem(starmetal_shovel, starmetal_shovel.getTranslationKey());
		GameRegistry.registerItem(starmetal_hoe, starmetal_hoe.getTranslationKey());
		GameRegistry.registerItem(alloy_sword, alloy_sword.getTranslationKey());
		GameRegistry.registerItem(alloy_pickaxe, alloy_pickaxe.getTranslationKey());
		GameRegistry.registerItem(alloy_axe, alloy_axe.getTranslationKey());
		GameRegistry.registerItem(alloy_shovel, alloy_shovel.getTranslationKey());
		GameRegistry.registerItem(alloy_hoe, alloy_hoe.getTranslationKey());
		GameRegistry.registerItem(cmb_sword, cmb_sword.getTranslationKey());
		GameRegistry.registerItem(cmb_pickaxe, cmb_pickaxe.getTranslationKey());
		GameRegistry.registerItem(cmb_axe, cmb_axe.getTranslationKey());
		GameRegistry.registerItem(cmb_shovel, cmb_shovel.getTranslationKey());
		GameRegistry.registerItem(cmb_hoe, cmb_hoe.getTranslationKey());
		GameRegistry.registerItem(desh_sword, desh_sword.getTranslationKey());
		GameRegistry.registerItem(desh_pickaxe, desh_pickaxe.getTranslationKey());
		GameRegistry.registerItem(desh_axe, desh_axe.getTranslationKey());
		GameRegistry.registerItem(desh_shovel, desh_shovel.getTranslationKey());
		GameRegistry.registerItem(desh_hoe, desh_hoe.getTranslationKey());
		GameRegistry.registerItem(elec_sword, elec_sword.getTranslationKey());
		GameRegistry.registerItem(elec_pickaxe, elec_pickaxe.getTranslationKey());
		GameRegistry.registerItem(elec_axe, elec_axe.getTranslationKey());
		GameRegistry.registerItem(elec_shovel, elec_shovel.getTranslationKey());
		GameRegistry.registerItem(dnt_sword, dnt_sword.getTranslationKey());
		GameRegistry.registerItem(smashing_hammer, smashing_hammer.getTranslationKey());
		GameRegistry.registerItem(centri_stick, centri_stick.getTranslationKey());
		GameRegistry.registerItem(drax, drax.getTranslationKey());
		GameRegistry.registerItem(drax_mk2, drax_mk2.getTranslationKey());
		GameRegistry.registerItem(drax_mk3, drax_mk3.getTranslationKey());
		GameRegistry.registerItem(bismuth_pickaxe, bismuth_pickaxe.getTranslationKey());
		GameRegistry.registerItem(bismuth_axe, bismuth_axe.getTranslationKey());
		GameRegistry.registerItem(volcanic_pickaxe, volcanic_pickaxe.getTranslationKey());
		GameRegistry.registerItem(volcanic_axe, volcanic_axe.getTranslationKey());
		GameRegistry.registerItem(chlorophyte_pickaxe, chlorophyte_pickaxe.getTranslationKey());
		GameRegistry.registerItem(chlorophyte_axe, chlorophyte_axe.getTranslationKey());
		GameRegistry.registerItem(mese_pickaxe, mese_pickaxe.getTranslationKey());
		GameRegistry.registerItem(mese_axe, mese_axe.getTranslationKey());		GameRegistry.registerItem(matchstick, matchstick.getTranslationKey());
		GameRegistry.registerItem(balefire_and_steel, balefire_and_steel.getTranslationKey());
		GameRegistry.registerItem(crowbar, crowbar.getTranslationKey());
		GameRegistry.registerItem(wrench, wrench.getTranslationKey());
		GameRegistry.registerItem(wrench_archineer, wrench_archineer.getTranslationKey());
		GameRegistry.registerItem(wrench_flipped, wrench_flipped.getTranslationKey());
		GameRegistry.registerItem(memespoon, memespoon.getTranslationKey());
		GameRegistry.registerItem(saw, saw.getTranslationKey());
		GameRegistry.registerItem(bat, bat.getTranslationKey());
		GameRegistry.registerItem(bat_nail, bat_nail.getTranslationKey());
		GameRegistry.registerItem(golf_club, golf_club.getTranslationKey());
		GameRegistry.registerItem(pipe_rusty, pipe_rusty.getTranslationKey());
		GameRegistry.registerItem(pipe_lead, pipe_lead.getTranslationKey());
		GameRegistry.registerItem(reer_graar, reer_graar.getTranslationKey());
		GameRegistry.registerItem(stopsign, stopsign.getTranslationKey());
		GameRegistry.registerItem(sopsign, sopsign.getTranslationKey());
		GameRegistry.registerItem(chernobylsign, chernobylsign.getTranslationKey());

		GameRegistry.registerItem(meteorite_sword, meteorite_sword.getTranslationKey());
		GameRegistry.registerItem(meteorite_sword_seared, meteorite_sword_seared.getTranslationKey());
		GameRegistry.registerItem(meteorite_sword_reforged, meteorite_sword_reforged.getTranslationKey());
		GameRegistry.registerItem(meteorite_sword_hardened, meteorite_sword_hardened.getTranslationKey());
		GameRegistry.registerItem(meteorite_sword_alloyed, meteorite_sword_alloyed.getTranslationKey());
		GameRegistry.registerItem(meteorite_sword_machined, meteorite_sword_machined.getTranslationKey());
		GameRegistry.registerItem(meteorite_sword_treated, meteorite_sword_treated.getTranslationKey());
		GameRegistry.registerItem(meteorite_sword_etched, meteorite_sword_etched.getTranslationKey());
		GameRegistry.registerItem(meteorite_sword_bred, meteorite_sword_bred.getTranslationKey());
		GameRegistry.registerItem(meteorite_sword_irradiated, meteorite_sword_irradiated.getTranslationKey());
		GameRegistry.registerItem(meteorite_sword_fused, meteorite_sword_fused.getTranslationKey());
		GameRegistry.registerItem(meteorite_sword_baleful, meteorite_sword_baleful.getTranslationKey());

		//Syringes & Pills
		GameRegistry.registerItem(syringe_empty, syringe_empty.getTranslationKey());
		GameRegistry.registerItem(syringe_antidote, syringe_antidote.getTranslationKey());
		GameRegistry.registerItem(syringe_poison, syringe_poison.getTranslationKey());
		GameRegistry.registerItem(syringe_awesome, syringe_awesome.getTranslationKey());
		GameRegistry.registerItem(syringe_metal_empty, syringe_metal_empty.getTranslationKey());
		GameRegistry.registerItem(syringe_metal_stimpak, syringe_metal_stimpak.getTranslationKey());
		GameRegistry.registerItem(syringe_metal_medx, syringe_metal_medx.getTranslationKey());
		GameRegistry.registerItem(syringe_metal_psycho, syringe_metal_psycho.getTranslationKey());
		GameRegistry.registerItem(syringe_metal_super, syringe_metal_super.getTranslationKey());
		GameRegistry.registerItem(syringe_taint, syringe_taint.getTranslationKey());
		GameRegistry.registerItem(syringe_mkunicorn, syringe_mkunicorn.getTranslationKey());
		GameRegistry.registerItem(med_bag, med_bag.getTranslationKey());
		GameRegistry.registerItem(iv_empty, iv_empty.getTranslationKey());
		GameRegistry.registerItem(iv_blood, iv_blood.getTranslationKey());
		GameRegistry.registerItem(iv_xp_empty, iv_xp_empty.getTranslationKey());
		GameRegistry.registerItem(iv_xp, iv_xp.getTranslationKey());
		GameRegistry.registerItem(radaway, radaway.getTranslationKey());
		GameRegistry.registerItem(radaway_strong, radaway_strong.getTranslationKey());
		GameRegistry.registerItem(radaway_flush, radaway_flush.getTranslationKey());
		GameRegistry.registerItem(radx, radx.getTranslationKey());
		GameRegistry.registerItem(siox, siox.getTranslationKey());
		GameRegistry.registerItem(pill_herbal, pill_herbal.getTranslationKey());
		GameRegistry.registerItem(pill_iodine, pill_iodine.getTranslationKey());
		GameRegistry.registerItem(xanax, xanax.getTranslationKey());
		GameRegistry.registerItem(fmn, fmn.getTranslationKey());
		GameRegistry.registerItem(five_htp, five_htp.getTranslationKey());
		GameRegistry.registerItem(plan_c, plan_c.getTranslationKey());
		GameRegistry.registerItem(pill_red, pill_red.getTranslationKey());
		GameRegistry.registerItem(stealth_boy, stealth_boy.getTranslationKey());
		GameRegistry.registerItem(gas_mask_filter, gas_mask_filter.getTranslationKey());
		GameRegistry.registerItem(gas_mask_filter_mono, gas_mask_filter_mono.getTranslationKey());
		GameRegistry.registerItem(gas_mask_filter_combo, gas_mask_filter_combo.getTranslationKey());
		GameRegistry.registerItem(gas_mask_filter_rag, gas_mask_filter_rag.getTranslationKey());
		GameRegistry.registerItem(gas_mask_filter_piss, gas_mask_filter_piss.getTranslationKey());
		GameRegistry.registerItem(jetpack_tank, jetpack_tank.getTranslationKey());
		GameRegistry.registerItem(lox_tank, lox_tank.getTranslationKey());
		GameRegistry.registerItem(gun_kit_1, gun_kit_1.getTranslationKey());
		GameRegistry.registerItem(gun_kit_2, gun_kit_2.getTranslationKey());

		GameRegistry.registerItem(animan, animan.getTranslationKey());

		//Food
		GameRegistry.registerItem(bomb_waffle, bomb_waffle.getTranslationKey());
		GameRegistry.registerItem(schnitzel_vegan, schnitzel_vegan.getTranslationKey());
		GameRegistry.registerItem(cotton_candy, cotton_candy.getTranslationKey());
		GameRegistry.registerItem(apple_lead, apple_lead.getTranslationKey());
		GameRegistry.registerItem(apple_schrabidium, apple_schrabidium.getTranslationKey());
		GameRegistry.registerItem(tem_flakes, tem_flakes.getTranslationKey());
		GameRegistry.registerItem(glowing_stew, glowing_stew.getTranslationKey());
		GameRegistry.registerItem(balefire_scrambled, balefire_scrambled.getTranslationKey());
		GameRegistry.registerItem(balefire_and_ham, balefire_and_ham.getTranslationKey());
		GameRegistry.registerItem(lemon, lemon.getTranslationKey());
		GameRegistry.registerItem(definitelyfood, definitelyfood.getTranslationKey());
		GameRegistry.registerItem(loops, loops.getTranslationKey());
		GameRegistry.registerItem(loop_stew, loop_stew.getTranslationKey());
		GameRegistry.registerItem(spongebob_macaroni, spongebob_macaroni.getTranslationKey());
		GameRegistry.registerItem(fooditem, fooditem.getTranslationKey());
		GameRegistry.registerItem(twinkie, twinkie.getTranslationKey());
		GameRegistry.registerItem(static_sandwich, static_sandwich.getTranslationKey());
		GameRegistry.registerItem(pudding, pudding.getTranslationKey());
		GameRegistry.registerItem(pancake, pancake.getTranslationKey());
		GameRegistry.registerItem(butter, butter.getTranslationKey());
		GameRegistry.registerItem(s_cream, s_cream.getTranslationKey());
		GameRegistry.registerItem(min_cream, min_cream.getTranslationKey());
		GameRegistry.registerItem(nugget, nugget.getTranslationKey());
		GameRegistry.registerItem(peas, peas.getTranslationKey());
		GameRegistry.registerItem(marshmallow, marshmallow.getTranslationKey());
		GameRegistry.registerItem(cheese, cheese.getTranslationKey());
		GameRegistry.registerItem(quesadilla, quesadilla.getTranslationKey());
		GameRegistry.registerItem(glyphid_meat, glyphid_meat.getTranslationKey());
		GameRegistry.registerItem(glyphid_meat_grilled, glyphid_meat_grilled.getTranslationKey());
		GameRegistry.registerItem(egg_glyphid, egg_glyphid.getTranslationKey());
		GameRegistry.registerItem(med_ipecac, med_ipecac.getTranslationKey());
		GameRegistry.registerItem(med_ptsd, med_ptsd.getTranslationKey());
		GameRegistry.registerItem(canteen_vodka, canteen_vodka.getTranslationKey());
		GameRegistry.registerItem(mucho_mango, mucho_mango.getTranslationKey());
		GameRegistry.registerItem(chocolate, chocolate.getTranslationKey());
		GameRegistry.registerItem(flour, flour.getTranslationKey());

		//Energy Drinks
		GameRegistry.registerItem(can_empty, can_empty.getTranslationKey());
		GameRegistry.registerItem(can_smart, can_smart.getTranslationKey());
		GameRegistry.registerItem(can_creature, can_creature.getTranslationKey());
		GameRegistry.registerItem(can_redbomb, can_redbomb.getTranslationKey());
		GameRegistry.registerItem(can_mrsugar, can_mrsugar.getTranslationKey());
		GameRegistry.registerItem(can_overcharge, can_overcharge.getTranslationKey());
		GameRegistry.registerItem(can_luna, can_luna.getTranslationKey());
		GameRegistry.registerItem(can_bepis, can_bepis.getTranslationKey());
		GameRegistry.registerItem(can_breen, can_breen.getTranslationKey());
		GameRegistry.registerItem(can_mug, can_mug.getTranslationKey());

		//Coffee
		GameRegistry.registerItem(coffee, coffee.getTranslationKey());
		GameRegistry.registerItem(coffee_radium, coffee_radium.getTranslationKey());

		//Cola
		GameRegistry.registerItem(bottle_empty, bottle_empty.getTranslationKey());
		GameRegistry.registerItem(bottle_nuka, bottle_nuka.getTranslationKey());
		GameRegistry.registerItem(bottle_cherry, bottle_cherry.getTranslationKey());
		GameRegistry.registerItem(bottle_quantum, bottle_quantum.getTranslationKey());
		GameRegistry.registerItem(bottle_sparkle, bottle_sparkle.getTranslationKey());
		GameRegistry.registerItem(bottle_rad, bottle_rad.getTranslationKey());
		GameRegistry.registerItem(bottle2_empty, bottle2_empty.getTranslationKey());
		GameRegistry.registerItem(bottle2_korl, bottle2_korl.getTranslationKey());
		GameRegistry.registerItem(bottle2_fritz, bottle2_fritz.getTranslationKey());
		GameRegistry.registerItem(bottle_opener, bottle_opener.getTranslationKey());

		//Flasks
		GameRegistry.registerItem(flask_infusion, flask_infusion.getTranslationKey());

		//Canned Food
		GameRegistry.registerItem(canned_conserve, canned_conserve.getTranslationKey());

		//Money
		GameRegistry.registerItem(cap_nuka, cap_nuka.getTranslationKey());
		GameRegistry.registerItem(cap_quantum, cap_quantum.getTranslationKey());
		GameRegistry.registerItem(cap_sparkle, cap_sparkle.getTranslationKey());
		GameRegistry.registerItem(cap_rad, cap_rad.getTranslationKey());
		GameRegistry.registerItem(cap_korl, cap_korl.getTranslationKey());
		GameRegistry.registerItem(cap_fritz, cap_fritz.getTranslationKey());
		GameRegistry.registerItem(ring_pull, ring_pull.getTranslationKey());
		GameRegistry.registerItem(can_key, can_key.getTranslationKey());
		GameRegistry.registerItem(coin_creeper, coin_creeper.getTranslationKey());
		GameRegistry.registerItem(coin_radiation,coin_radiation.getTranslationKey());
		GameRegistry.registerItem(coin_maskman, coin_maskman.getTranslationKey());
		GameRegistry.registerItem(coin_worm, coin_worm.getTranslationKey());
		GameRegistry.registerItem(coin_ufo, coin_ufo.getTranslationKey());
		GameRegistry.registerItem(coin_airliner, coin_airliner.getTranslationKey());
		GameRegistry.registerItem(medal_liquidator, medal_liquidator.getTranslationKey());
		GameRegistry.registerItem(v1, v1.getTranslationKey());
		GameRegistry.registerItem(protection_charm, protection_charm.getTranslationKey());
		GameRegistry.registerItem(meteor_charm, meteor_charm.getTranslationKey());
		GameRegistry.registerItem(neutrino_lens, neutrino_lens.getTranslationKey());
		GameRegistry.registerItem(gas_tester, gas_tester.getTranslationKey());
		GameRegistry.registerItem(defuser_gold, defuser_gold.getTranslationKey());
		GameRegistry.registerItem(ballistic_gauntlet, ballistic_gauntlet.getTranslationKey());
		GameRegistry.registerItem(night_vision, night_vision.getTranslationKey());
		GameRegistry.registerItem(card_aos, card_aos.getTranslationKey());
		GameRegistry.registerItem(card_qos, card_qos.getTranslationKey());
		GameRegistry.registerItem(australium_iii, australium_iii.getTranslationKey());
		GameRegistry.registerItem(armor_battery, armor_battery.getTranslationKey());
		GameRegistry.registerItem(armor_battery_mk2, armor_battery_mk2.getTranslationKey());
		GameRegistry.registerItem(armor_battery_mk3, armor_battery_mk3.getTranslationKey());
		GameRegistry.registerItem(flippers, flippers.getTranslationKey());
		GameRegistry.registerItem(heavy_boots, heavy_boots.getTranslationKey());

		//Chaos
		GameRegistry.registerItem(chocolate_milk, chocolate_milk.getTranslationKey());
		GameRegistry.registerItem(cbt_device, cbt_device.getTranslationKey());
		GameRegistry.registerItem(cigarette, cigarette.getTranslationKey());
		GameRegistry.registerItem(crackpipe, crackpipe.getTranslationKey());
		GameRegistry.registerItem(bdcl, bdcl.getTranslationKey());


		GameRegistry.registerItem(glass_smilk, glass_smilk.getTranslationKey());
		GameRegistry.registerItem(glass_empty, glass_empty.getTranslationKey());
		GameRegistry.registerItem(strawberry, strawberry.getTranslationKey());
		GameRegistry.registerItem(mint_leaves, mint_leaves.getTranslationKey());
		GameRegistry.registerItem(teaseeds, teaseeds.getTranslationKey());
		GameRegistry.registerItem(tea_leaf, tea_leaf.getTranslationKey());
		GameRegistry.registerItem(powder_coffee, powder_coffee.getTranslationKey());
		GameRegistry.registerItem(bean_raw, bean_raw.getTranslationKey());
		GameRegistry.registerItem(bean_roast, bean_roast.getTranslationKey());
		GameRegistry.registerItem(cmug_empty, cmug_empty.getTranslationKey());
		GameRegistry.registerItem(teacup_empty, teacup_empty.getTranslationKey());
		GameRegistry.registerItem(teacup, teacup.getTranslationKey());
		GameRegistry.registerItem(bottle_honey, bottle_honey.getTranslationKey());
		GameRegistry.registerItem(paraffin_seeds, paraffin_seeds.getTranslationKey());


		//Armor mods
		GameRegistry.registerItem(attachment_mask, attachment_mask.getTranslationKey());
		GameRegistry.registerItem(attachment_mask_mono, attachment_mask_mono.getTranslationKey());
		GameRegistry.registerItem(back_tesla, back_tesla.getTranslationKey());
		GameRegistry.registerItem(servo_set, servo_set.getTranslationKey());
		GameRegistry.registerItem(servo_set_desh, servo_set_desh.getTranslationKey());
		GameRegistry.registerItem(pads_rubber, pads_rubber.getTranslationKey());
		GameRegistry.registerItem(pads_slime, pads_slime.getTranslationKey());
		GameRegistry.registerItem(pads_static, pads_static.getTranslationKey());
		GameRegistry.registerItem(cladding_paint, cladding_paint.getTranslationKey());
		GameRegistry.registerItem(cladding_rubber, cladding_rubber.getTranslationKey());
		GameRegistry.registerItem(cladding_lead, cladding_lead.getTranslationKey());
		GameRegistry.registerItem(cladding_desh, cladding_desh.getTranslationKey());
		GameRegistry.registerItem(cladding_ghiorsium, cladding_ghiorsium.getTranslationKey());
		GameRegistry.registerItem(cladding_iron, cladding_iron.getTranslationKey());
		GameRegistry.registerItem(cladding_obsidian, cladding_obsidian.getTranslationKey());
		GameRegistry.registerItem(insert_kevlar, insert_kevlar.getTranslationKey());
		GameRegistry.registerItem(insert_sapi, insert_sapi.getTranslationKey());
		GameRegistry.registerItem(insert_esapi, insert_esapi.getTranslationKey());
		GameRegistry.registerItem(insert_xsapi, insert_xsapi.getTranslationKey());
		GameRegistry.registerItem(insert_steel, insert_steel.getTranslationKey());
		GameRegistry.registerItem(insert_du, insert_du.getTranslationKey());
		GameRegistry.registerItem(insert_polonium, insert_polonium.getTranslationKey());
		GameRegistry.registerItem(insert_ghiorsium, insert_ghiorsium.getTranslationKey());
		GameRegistry.registerItem(insert_cmb, insert_cmb.getTranslationKey());
		GameRegistry.registerItem(insert_era, insert_era.getTranslationKey());
		GameRegistry.registerItem(insert_yharonite, insert_yharonite.getTranslationKey());
		GameRegistry.registerItem(insert_doxium, insert_doxium.getTranslationKey());
		GameRegistry.registerItem(armor_polish, armor_polish.getTranslationKey());
		GameRegistry.registerItem(bandaid, bandaid.getTranslationKey());
		GameRegistry.registerItem(serum, serum.getTranslationKey());
		GameRegistry.registerItem(quartz_plutonium, quartz_plutonium.getTranslationKey());
		GameRegistry.registerItem(morning_glory, morning_glory.getTranslationKey());
		GameRegistry.registerItem(lodestone, lodestone.getTranslationKey());
		GameRegistry.registerItem(horseshoe_magnet, horseshoe_magnet.getTranslationKey());
		GameRegistry.registerItem(industrial_magnet, industrial_magnet.getTranslationKey());
		GameRegistry.registerItem(bathwater, bathwater.getTranslationKey());
		GameRegistry.registerItem(bathwater_mk2, bathwater_mk2.getTranslationKey());
		GameRegistry.registerItem(spider_milk, spider_milk.getTranslationKey());
		GameRegistry.registerItem(ink, ink.getTranslationKey());
		GameRegistry.registerItem(heart_piece, heart_piece.getTranslationKey());
		GameRegistry.registerItem(heart_container, heart_container.getTranslationKey());
		GameRegistry.registerItem(heart_booster, heart_booster.getTranslationKey());
		GameRegistry.registerItem(heart_fab, heart_fab.getTranslationKey());
		GameRegistry.registerItem(black_diamond, black_diamond.getTranslationKey());
		GameRegistry.registerItem(wd40, wd40.getTranslationKey());
		GameRegistry.registerItem(scrumpy, scrumpy.getTranslationKey());
		GameRegistry.registerItem(wild_p, wild_p.getTranslationKey());
		GameRegistry.registerItem(shackles, shackles.getTranslationKey());
		GameRegistry.registerItem(injector_5htp, injector_5htp.getTranslationKey());
		GameRegistry.registerItem(injector_knife, injector_knife.getTranslationKey());

		//Vehicles
		GameRegistry.registerItem(boat_rubber, boat_rubber.getTranslationKey());
		GameRegistry.registerItem(cart, cart.getTranslationKey());
		GameRegistry.registerItem(train, train.getTranslationKey());
		GameRegistry.registerItem(drone, drone.getTranslationKey());

		//High Explosive Lenses
		GameRegistry.registerItem(early_explosive_lenses, early_explosive_lenses.getTranslationKey());
		GameRegistry.registerItem(explosive_lenses, explosive_lenses.getTranslationKey());

		//The Gadget
		GameRegistry.registerItem(gadget_wireing, gadget_wireing.getTranslationKey());
		GameRegistry.registerItem(gadget_core, gadget_core.getTranslationKey());

		//Little Boy
		GameRegistry.registerItem(boy_shielding, boy_shielding.getTranslationKey());
		GameRegistry.registerItem(boy_target, boy_target.getTranslationKey());
		GameRegistry.registerItem(boy_bullet, boy_bullet.getTranslationKey());
		GameRegistry.registerItem(boy_propellant, boy_propellant.getTranslationKey());
		GameRegistry.registerItem(boy_igniter, boy_igniter.getTranslationKey());;

		//Fat Man
		GameRegistry.registerItem(man_igniter, man_igniter.getTranslationKey());
		GameRegistry.registerItem(man_core, man_core.getTranslationKey());

		//Ivy Mike
		GameRegistry.registerItem(mike_core, mike_core.getTranslationKey());
		GameRegistry.registerItem(mike_deut, mike_deut.getTranslationKey());
		GameRegistry.registerItem(mike_cooling_unit, mike_cooling_unit.getTranslationKey());

		//Tsar Bomba
		GameRegistry.registerItem(tsar_core, tsar_core.getTranslationKey());

		//FLEIJA
		GameRegistry.registerItem(fleija_igniter, fleija_igniter.getTranslationKey());
		GameRegistry.registerItem(fleija_propellant, fleija_propellant.getTranslationKey());
		GameRegistry.registerItem(fleija_core, fleija_core.getTranslationKey());

		//Solinium
		GameRegistry.registerItem(solinium_igniter, solinium_igniter.getTranslationKey());
		GameRegistry.registerItem(solinium_propellant, solinium_propellant.getTranslationKey());
		GameRegistry.registerItem(solinium_core, solinium_core.getTranslationKey());

		//N2
		GameRegistry.registerItem(n2_charge, n2_charge.getTranslationKey());

		//FSTBMB
		GameRegistry.registerItem(egg_balefire_shard, egg_balefire_shard.getTranslationKey());
		GameRegistry.registerItem(egg_balefire, egg_balefire.getTranslationKey());

		//Conventional Armor
		GameRegistry.registerItem(goggles, goggles.getTranslationKey());
		GameRegistry.registerItem(ashglasses, ashglasses.getTranslationKey());
		GameRegistry.registerItem(gas_mask, gas_mask.getTranslationKey());
		GameRegistry.registerItem(gas_mask_m65, gas_mask_m65.getTranslationKey());
		GameRegistry.registerItem(gas_mask_mono, gas_mask_mono.getTranslationKey());
		GameRegistry.registerItem(gas_mask_olde, gas_mask_olde.getTranslationKey());
		GameRegistry.registerItem(mask_rag, mask_rag.getTranslationKey());
		GameRegistry.registerItem(mask_piss, mask_piss.getTranslationKey());
		GameRegistry.registerItem(hat, hat.getTranslationKey());
		GameRegistry.registerItem(beta, beta.getTranslationKey());
		GameRegistry.registerItem(no9, no9.getTranslationKey());

		GameRegistry.registerItem(steel_helmet, steel_helmet.getTranslationKey());
		GameRegistry.registerItem(steel_plate, steel_plate.getTranslationKey());
		GameRegistry.registerItem(steel_legs, steel_legs.getTranslationKey());
		GameRegistry.registerItem(steel_boots, steel_boots.getTranslationKey());
		GameRegistry.registerItem(titanium_helmet, titanium_helmet.getTranslationKey());
		GameRegistry.registerItem(titanium_plate, titanium_plate.getTranslationKey());
		GameRegistry.registerItem(titanium_legs, titanium_legs.getTranslationKey());
		GameRegistry.registerItem(titanium_boots, titanium_boots.getTranslationKey());
		GameRegistry.registerItem(alloy_helmet, alloy_helmet.getTranslationKey());
		GameRegistry.registerItem(alloy_plate, alloy_plate.getTranslationKey());
		GameRegistry.registerItem(alloy_legs, alloy_legs.getTranslationKey());
		GameRegistry.registerItem(alloy_boots, alloy_boots.getTranslationKey());

		//Custom Rods
		GameRegistry.registerItem(custom_tnt, custom_tnt.getTranslationKey());
		GameRegistry.registerItem(custom_nuke, custom_nuke.getTranslationKey());
		GameRegistry.registerItem(custom_hydro, custom_hydro.getTranslationKey());
		GameRegistry.registerItem(custom_amat, custom_amat.getTranslationKey());
		GameRegistry.registerItem(custom_dirty, custom_dirty.getTranslationKey());
		GameRegistry.registerItem(custom_schrab, custom_schrab.getTranslationKey());
		GameRegistry.registerItem(custom_fall, custom_fall.getTranslationKey());

		//Power Armor
		GameRegistry.registerItem(steamsuit_helmet, steamsuit_helmet.getTranslationKey());
		GameRegistry.registerItem(steamsuit_plate, steamsuit_plate.getTranslationKey());
		GameRegistry.registerItem(steamsuit_legs, steamsuit_legs.getTranslationKey());
		GameRegistry.registerItem(steamsuit_boots, steamsuit_boots.getTranslationKey());
		GameRegistry.registerItem(dieselsuit_helmet, dieselsuit_helmet.getTranslationKey());
		GameRegistry.registerItem(dieselsuit_plate, dieselsuit_plate.getTranslationKey());
		GameRegistry.registerItem(dieselsuit_legs, dieselsuit_legs.getTranslationKey());
		GameRegistry.registerItem(dieselsuit_boots, dieselsuit_boots.getTranslationKey());
		GameRegistry.registerItem(t51_helmet, t51_helmet.getTranslationKey());
		GameRegistry.registerItem(t51_plate, t51_plate.getTranslationKey());
		GameRegistry.registerItem(t51_legs, t51_legs.getTranslationKey());
		GameRegistry.registerItem(t51_boots, t51_boots.getTranslationKey());
		GameRegistry.registerItem(ajr_helmet, ajr_helmet.getTranslationKey());
		GameRegistry.registerItem(ajr_plate, ajr_plate.getTranslationKey());
		GameRegistry.registerItem(ajr_legs, ajr_legs.getTranslationKey());
		GameRegistry.registerItem(ajr_boots, ajr_boots.getTranslationKey());
		GameRegistry.registerItem(ajro_helmet, ajro_helmet.getTranslationKey());
		GameRegistry.registerItem(ajro_plate, ajro_plate.getTranslationKey());
		GameRegistry.registerItem(ajro_legs, ajro_legs.getTranslationKey());
		GameRegistry.registerItem(ajro_boots, ajro_boots.getTranslationKey());
		GameRegistry.registerItem(rpa_helmet, rpa_helmet.getTranslationKey());
		GameRegistry.registerItem(rpa_plate, rpa_plate.getTranslationKey());
		GameRegistry.registerItem(rpa_legs, rpa_legs.getTranslationKey());
		GameRegistry.registerItem(rpa_boots, rpa_boots.getTranslationKey());
		GameRegistry.registerItem(ncrpa_helmet, ncrpa_helmet.getTranslationKey());
		GameRegistry.registerItem(ncrpa_plate, ncrpa_plate.getTranslationKey());
		GameRegistry.registerItem(ncrpa_legs, ncrpa_legs.getTranslationKey());
		GameRegistry.registerItem(ncrpa_boots, ncrpa_boots.getTranslationKey());
		GameRegistry.registerItem(bj_helmet, bj_helmet.getTranslationKey());
		GameRegistry.registerItem(bj_plate, bj_plate.getTranslationKey());
		GameRegistry.registerItem(bj_plate_jetpack, bj_plate_jetpack.getTranslationKey());
		GameRegistry.registerItem(bj_legs, bj_legs.getTranslationKey());
		GameRegistry.registerItem(bj_boots, bj_boots.getTranslationKey());
		GameRegistry.registerItem(envsuit_helmet, envsuit_helmet.getTranslationKey());
		GameRegistry.registerItem(envsuit_plate, envsuit_plate.getTranslationKey());
		GameRegistry.registerItem(envsuit_legs, envsuit_legs.getTranslationKey());
		GameRegistry.registerItem(envsuit_boots, envsuit_boots.getTranslationKey());
		GameRegistry.registerItem(hev_helmet, hev_helmet.getTranslationKey());
		GameRegistry.registerItem(hev_plate, hev_plate.getTranslationKey());
		GameRegistry.registerItem(hev_legs, hev_legs.getTranslationKey());
		GameRegistry.registerItem(hev_boots, hev_boots.getTranslationKey());
		GameRegistry.registerItem(fau_helmet, fau_helmet.getTranslationKey());
		GameRegistry.registerItem(fau_plate, fau_plate.getTranslationKey());
		GameRegistry.registerItem(fau_legs, fau_legs.getTranslationKey());
		GameRegistry.registerItem(fau_boots, fau_boots.getTranslationKey());
		GameRegistry.registerItem(dns_helmet, dns_helmet.getTranslationKey());
		GameRegistry.registerItem(dns_plate, dns_plate.getTranslationKey());
		GameRegistry.registerItem(dns_legs, dns_legs.getTranslationKey());
		GameRegistry.registerItem(dns_boots, dns_boots.getTranslationKey());
		GameRegistry.registerItem(taurun_helmet, taurun_helmet.getTranslationKey());
		GameRegistry.registerItem(taurun_plate, taurun_plate.getTranslationKey());
		GameRegistry.registerItem(taurun_legs, taurun_legs.getTranslationKey());
		GameRegistry.registerItem(taurun_boots, taurun_boots.getTranslationKey());
		GameRegistry.registerItem(trenchmaster_helmet, trenchmaster_helmet.getTranslationKey());
		GameRegistry.registerItem(trenchmaster_plate, trenchmaster_plate.getTranslationKey());
		GameRegistry.registerItem(trenchmaster_legs, trenchmaster_legs.getTranslationKey());
		GameRegistry.registerItem(trenchmaster_boots, trenchmaster_boots.getTranslationKey());

		//Nobody will ever read this anyway, so it shouldn't matter.
		GameRegistry.registerItem(chainsaw, chainsaw.getTranslationKey());
		GameRegistry.registerItem(igniter, igniter.getTranslationKey());
		GameRegistry.registerItem(detonator, detonator.getTranslationKey());
		GameRegistry.registerItem(detonator_multi, detonator_multi.getTranslationKey());
		GameRegistry.registerItem(detonator_laser, detonator_laser.getTranslationKey());
		GameRegistry.registerItem(detonator_deadman, detonator_deadman.getTranslationKey());
		GameRegistry.registerItem(detonator_de, detonator_de.getTranslationKey());
		GameRegistry.registerItem(bomb_caller, bomb_caller.getTranslationKey());
		GameRegistry.registerItem(meteor_remote, meteor_remote.getTranslationKey());
		GameRegistry.registerItem(anchor_remote, anchor_remote.getTranslationKey());
		GameRegistry.registerItem(defuser, defuser.getTranslationKey());
		GameRegistry.registerItem(reacher, reacher.getTranslationKey());
		GameRegistry.registerItem(bismuth_tool, bismuth_tool.getTranslationKey());
		GameRegistry.registerItem(meltdown_tool, meltdown_tool.getTranslationKey());

		GameRegistry.registerItem(hazmat_helmet, hazmat_helmet.getTranslationKey());
		GameRegistry.registerItem(hazmat_plate, hazmat_plate.getTranslationKey());
		GameRegistry.registerItem(hazmat_legs, hazmat_legs.getTranslationKey());
		GameRegistry.registerItem(hazmat_boots, hazmat_boots.getTranslationKey());
		GameRegistry.registerItem(hazmat_helmet_red, hazmat_helmet_red.getTranslationKey());
		GameRegistry.registerItem(hazmat_plate_red, hazmat_plate_red.getTranslationKey());
		GameRegistry.registerItem(hazmat_legs_red, hazmat_legs_red.getTranslationKey());
		GameRegistry.registerItem(hazmat_boots_red, hazmat_boots_red.getTranslationKey());
		GameRegistry.registerItem(hazmat_helmet_grey, hazmat_helmet_grey.getTranslationKey());
		GameRegistry.registerItem(hazmat_plate_grey, hazmat_plate_grey.getTranslationKey());
		GameRegistry.registerItem(hazmat_legs_grey, hazmat_legs_grey.getTranslationKey());
		GameRegistry.registerItem(hazmat_boots_grey, hazmat_boots_grey.getTranslationKey());
		GameRegistry.registerItem(hazmat_paa_helmet, hazmat_paa_helmet.getTranslationKey());
		GameRegistry.registerItem(hazmat_paa_plate, hazmat_paa_plate.getTranslationKey());
		GameRegistry.registerItem(hazmat_paa_legs, hazmat_paa_legs.getTranslationKey());
		GameRegistry.registerItem(hazmat_paa_boots, hazmat_paa_boots.getTranslationKey());
		GameRegistry.registerItem(liquidator_helmet, liquidator_helmet.getTranslationKey());
		GameRegistry.registerItem(liquidator_plate, liquidator_plate.getTranslationKey());
		GameRegistry.registerItem(liquidator_legs, liquidator_legs.getTranslationKey());
		GameRegistry.registerItem(liquidator_boots, liquidator_boots.getTranslationKey());
		GameRegistry.registerItem(cmb_helmet, cmb_helmet.getTranslationKey());
		GameRegistry.registerItem(cmb_plate, cmb_plate.getTranslationKey());
		GameRegistry.registerItem(cmb_legs, cmb_legs.getTranslationKey());
		GameRegistry.registerItem(cmb_boots, cmb_boots.getTranslationKey());
		GameRegistry.registerItem(paa_plate, paa_plate.getTranslationKey());
		GameRegistry.registerItem(paa_legs, paa_legs.getTranslationKey());
		GameRegistry.registerItem(paa_boots, paa_boots.getTranslationKey());
		GameRegistry.registerItem(asbestos_helmet, asbestos_helmet.getTranslationKey());
		GameRegistry.registerItem(asbestos_plate, asbestos_plate.getTranslationKey());
		GameRegistry.registerItem(asbestos_legs, asbestos_legs.getTranslationKey());
		GameRegistry.registerItem(asbestos_boots, asbestos_boots.getTranslationKey());
		GameRegistry.registerItem(security_helmet, security_helmet.getTranslationKey());
		GameRegistry.registerItem(security_plate, security_plate.getTranslationKey());
		GameRegistry.registerItem(security_legs, security_legs.getTranslationKey());
		GameRegistry.registerItem(security_boots, security_boots.getTranslationKey());
		GameRegistry.registerItem(cobalt_helmet, cobalt_helmet.getTranslationKey());
		GameRegistry.registerItem(cobalt_plate, cobalt_plate.getTranslationKey());
		GameRegistry.registerItem(cobalt_legs, cobalt_legs.getTranslationKey());
		GameRegistry.registerItem(cobalt_boots, cobalt_boots.getTranslationKey());
		GameRegistry.registerItem(starmetal_helmet, starmetal_helmet.getTranslationKey());
		GameRegistry.registerItem(starmetal_plate, starmetal_plate.getTranslationKey());
		GameRegistry.registerItem(starmetal_legs, starmetal_legs.getTranslationKey());
		GameRegistry.registerItem(starmetal_boots, starmetal_boots.getTranslationKey());
		GameRegistry.registerItem(zirconium_legs, zirconium_legs.getTranslationKey());
		GameRegistry.registerItem(dnt_helmet, dnt_helmet.getTranslationKey());
		GameRegistry.registerItem(dnt_plate, dnt_plate.getTranslationKey());
		GameRegistry.registerItem(dnt_legs, dnt_legs.getTranslationKey());
		GameRegistry.registerItem(dnt_boots, dnt_boots.getTranslationKey());
		GameRegistry.registerItem(schrabidium_helmet, schrabidium_helmet.getTranslationKey());
		GameRegistry.registerItem(schrabidium_plate, schrabidium_plate.getTranslationKey());
		GameRegistry.registerItem(schrabidium_legs, schrabidium_legs.getTranslationKey());
		GameRegistry.registerItem(schrabidium_boots, schrabidium_boots.getTranslationKey());
		GameRegistry.registerItem(bismuth_helmet, bismuth_helmet.getTranslationKey());
		GameRegistry.registerItem(bismuth_plate, bismuth_plate.getTranslationKey());
		GameRegistry.registerItem(bismuth_legs, bismuth_legs.getTranslationKey());
		GameRegistry.registerItem(bismuth_boots, bismuth_boots.getTranslationKey());
		GameRegistry.registerItem(euphemium_helmet, euphemium_helmet.getTranslationKey());
		GameRegistry.registerItem(euphemium_plate, euphemium_plate.getTranslationKey());
		GameRegistry.registerItem(euphemium_legs, euphemium_legs.getTranslationKey());
		GameRegistry.registerItem(euphemium_boots, euphemium_boots.getTranslationKey());
		GameRegistry.registerItem(robes_helmet, robes_helmet.getTranslationKey());
		GameRegistry.registerItem(robes_plate, robes_plate.getTranslationKey());
		GameRegistry.registerItem(robes_legs, robes_legs.getTranslationKey());
		GameRegistry.registerItem(robes_boots, robes_boots.getTranslationKey());
		GameRegistry.registerItem(apple_euphemium, apple_euphemium.getTranslationKey());
		GameRegistry.registerItem(watch, watch.getTranslationKey());
		GameRegistry.registerItem(mask_of_infamy, mask_of_infamy.getTranslationKey());
		GameRegistry.registerItem(jackt, jackt.getTranslationKey());
		GameRegistry.registerItem(jackt2, jackt2.getTranslationKey());
		GameRegistry.registerItem(jetpack_fly, jetpack_fly.getTranslationKey());
		GameRegistry.registerItem(jetpack_break, jetpack_break.getTranslationKey());
		GameRegistry.registerItem(jetpack_vector, jetpack_vector.getTranslationKey());
		GameRegistry.registerItem(jetpack_boost, jetpack_boost.getTranslationKey());
		GameRegistry.registerItem(wings_limp, wings_limp.getTranslationKey());
		GameRegistry.registerItem(wings_murk, wings_murk.getTranslationKey());
		GameRegistry.registerItem(oxy_plss, oxy_plss.getTranslationKey());

		//Expensive Ass Shit
		GameRegistry.registerItem(crystal_horn, crystal_horn.getTranslationKey());
		GameRegistry.registerItem(crystal_charred, crystal_charred.getTranslationKey());

		//Wands, Tools, Other Crap
		GameRegistry.registerItem(rebar_placer, rebar_placer.getTranslationKey());
		GameRegistry.registerItem(wand, wand.getTranslationKey());
		GameRegistry.registerItem(wand_s, wand_s.getTranslationKey());
		GameRegistry.registerItem(wand_d, wand_d.getTranslationKey());
		GameRegistry.registerItem(wand_time, wand_time.getTranslationKey());
		GameRegistry.registerItem(structure_single, structure_single.getTranslationKey());
		GameRegistry.registerItem(structure_solid, structure_solid.getTranslationKey());
		GameRegistry.registerItem(structure_pattern, structure_pattern.getTranslationKey());
		GameRegistry.registerItem(structure_randomized, structure_randomized.getTranslationKey());
		GameRegistry.registerItem(structure_randomly, structure_randomly.getTranslationKey());
		GameRegistry.registerItem(structure_custommachine, structure_custommachine.getTranslationKey());
		GameRegistry.registerItem(rod_of_discord, rod_of_discord.getTranslationKey());
		//GameRegistry.registerItem(analyzer, analyzer.getUnlocalizedName());
		//GameRegistry.registerItem(remote, remote.getUnlocalizedName());
		GameRegistry.registerItem(polaroid, polaroid.getTranslationKey());
		GameRegistry.registerItem(glitch, glitch.getTranslationKey());
		GameRegistry.registerItem(book_secret, book_secret.getTranslationKey());
		GameRegistry.registerItem(book_of_, book_of_.getTranslationKey());
		GameRegistry.registerItem(page_of_, page_of_.getTranslationKey());
		GameRegistry.registerItem(book_lemegeton, book_lemegeton.getTranslationKey());
		GameRegistry.registerItem(burnt_bark, burnt_bark.getTranslationKey());

		//Kits
		GameRegistry.registerItem(nuke_starter_kit, nuke_starter_kit.getTranslationKey());
		GameRegistry.registerItem(nuke_advanced_kit, nuke_advanced_kit.getTranslationKey());
		GameRegistry.registerItem(nuke_commercially_kit, nuke_commercially_kit.getTranslationKey());
		GameRegistry.registerItem(nuke_electric_kit, nuke_electric_kit.getTranslationKey());
		GameRegistry.registerItem(gadget_kit, gadget_kit.getTranslationKey());
		GameRegistry.registerItem(boy_kit, boy_kit.getTranslationKey());
		GameRegistry.registerItem(man_kit, man_kit.getTranslationKey());
		GameRegistry.registerItem(mike_kit, mike_kit.getTranslationKey());
		GameRegistry.registerItem(tsar_kit, tsar_kit.getTranslationKey());
		GameRegistry.registerItem(prototype_kit, prototype_kit.getTranslationKey());
		GameRegistry.registerItem(fleija_kit, fleija_kit.getTranslationKey());
		GameRegistry.registerItem(solinium_kit, solinium_kit.getTranslationKey());
		GameRegistry.registerItem(multi_kit, multi_kit.getTranslationKey());
		GameRegistry.registerItem(custom_kit, custom_kit.getTranslationKey());
		GameRegistry.registerItem(missile_kit, missile_kit.getTranslationKey());
		GameRegistry.registerItem(grenade_kit, grenade_kit.getTranslationKey());
		GameRegistry.registerItem(hazmat_kit, hazmat_kit.getTranslationKey());
		GameRegistry.registerItem(hazmat_red_kit, hazmat_red_kit.getTranslationKey());
		GameRegistry.registerItem(hazmat_grey_kit, hazmat_grey_kit.getTranslationKey());
		GameRegistry.registerItem(kit_custom, kit_custom.getTranslationKey());
		GameRegistry.registerItem(euphemium_kit, euphemium_kit.getTranslationKey());
		GameRegistry.registerItem(toolbox, toolbox.getTranslationKey());

		//Misile Loot Boxes
		GameRegistry.registerItem(loot_10, loot_10.getTranslationKey());
		GameRegistry.registerItem(loot_15, loot_15.getTranslationKey());
		GameRegistry.registerItem(loot_misc, loot_misc.getTranslationKey());

		//THIS is a bucket.
		GameRegistry.registerItem(bucket_mud, bucket_mud.getTranslationKey());
		GameRegistry.registerItem(bucket_acid, bucket_acid.getTranslationKey());
		GameRegistry.registerItem(bucket_toxic, bucket_toxic.getTranslationKey());
		GameRegistry.registerItem(bucket_schrabidic_acid, bucket_schrabidic_acid.getTranslationKey());
		GameRegistry.registerItem(bucket_sulfuric_acid, bucket_sulfuric_acid.getTranslationKey());
		GameRegistry.registerItem(bucket_mercury, bucket_mercury.getTranslationKey());
		GameRegistry.registerItem(bucket_bromine, bucket_bromine.getTranslationKey());
		GameRegistry.registerItem(bucket_ccl, bucket_ccl.getTranslationKey());

		//Door Items
		GameRegistry.registerItem(door_metal, door_metal.getTranslationKey());
		GameRegistry.registerItem(door_office, door_office.getTranslationKey());
		GameRegistry.registerItem(door_bunker, door_bunker.getTranslationKey());
		GameRegistry.registerItem(door_red, door_red.getTranslationKey());
		GameRegistry.registerItem(fence_gate, fence_gate.getTranslationKey());

		//Records
		GameRegistry.registerItem(record_lc, record_lc.getTranslationKey());
		GameRegistry.registerItem(record_ss, record_ss.getTranslationKey());
		GameRegistry.registerItem(record_vc, record_vc.getTranslationKey());
		GameRegistry.registerItem(record_gs, record_gs.getTranslationKey());
		GameRegistry.registerItem(record_gp, record_gp.getTranslationKey());
		GameRegistry.registerItem(record_el, record_el.getTranslationKey());
		GameRegistry.registerItem(record_glass, record_glass.getTranslationKey());

		//wow we're far down the item registry, is this the cellar?
		GameRegistry.registerItem(book_guide, book_guide.getTranslationKey());
		GameRegistry.registerItem(book_lore, book_lore.getTranslationKey());
		GameRegistry.registerItem(holotape_image, holotape_image.getTranslationKey());
		GameRegistry.registerItem(holotape_damaged, holotape_damaged.getTranslationKey());
		GameRegistry.registerItem(clay_tablet, clay_tablet.getTranslationKey());

		//Technical Items
		GameRegistry.registerItem(chlorine1, chlorine1.getTranslationKey());
		GameRegistry.registerItem(chlorine2, chlorine2.getTranslationKey());
		GameRegistry.registerItem(chlorine3, chlorine3.getTranslationKey());
		GameRegistry.registerItem(chlorine4, chlorine4.getTranslationKey());
		GameRegistry.registerItem(chlorine5, chlorine5.getTranslationKey());
		GameRegistry.registerItem(chlorine6, chlorine6.getTranslationKey());
		GameRegistry.registerItem(chlorine7, chlorine7.getTranslationKey());
		GameRegistry.registerItem(chlorine8, chlorine8.getTranslationKey());
		GameRegistry.registerItem(pc1, pc1.getTranslationKey());
		GameRegistry.registerItem(pc2, pc2.getTranslationKey());
		GameRegistry.registerItem(pc3, pc3.getTranslationKey());
		GameRegistry.registerItem(pc4, pc4.getTranslationKey());
		GameRegistry.registerItem(pc5, pc5.getTranslationKey());
		GameRegistry.registerItem(pc6, pc6.getTranslationKey());
		GameRegistry.registerItem(pc7, pc7.getTranslationKey());
		GameRegistry.registerItem(pc8, pc8.getTranslationKey());
		GameRegistry.registerItem(cloud1, cloud1.getTranslationKey());
		GameRegistry.registerItem(cloud2, cloud2.getTranslationKey());
		GameRegistry.registerItem(cloud3, cloud3.getTranslationKey());
		GameRegistry.registerItem(cloud4, cloud4.getTranslationKey());
		GameRegistry.registerItem(cloud5, cloud5.getTranslationKey());
		GameRegistry.registerItem(cloud6, cloud6.getTranslationKey());
		GameRegistry.registerItem(cloud7, cloud7.getTranslationKey());
		GameRegistry.registerItem(cloud8, cloud8.getTranslationKey());
		GameRegistry.registerItem(orange1, orange1.getTranslationKey());
		GameRegistry.registerItem(orange2, orange2.getTranslationKey());
		GameRegistry.registerItem(orange3, orange3.getTranslationKey());
		GameRegistry.registerItem(orange4, orange4.getTranslationKey());
		GameRegistry.registerItem(orange5, orange5.getTranslationKey());
		GameRegistry.registerItem(orange6, orange6.getTranslationKey());
		GameRegistry.registerItem(orange7, orange7.getTranslationKey());
		GameRegistry.registerItem(orange8, orange8.getTranslationKey());
		GameRegistry.registerItem(achievement_icon, achievement_icon.getTranslationKey());

		GameRegistry.registerItem(mysteryshovel, mysteryshovel.getTranslationKey());
		GameRegistry.registerItem(memory, memory.getTranslationKey());

		GameRegistry.registerItem(conveyor_wand, conveyor_wand.getTranslationKey());

		GameRegistry.registerItem(swarm_member, swarm_member.getTranslationKey());

		GameRegistry.registerItem(nothing, nothing.getTranslationKey());
		GameRegistry.registerItem(broken_item, broken_item.getTranslationKey());
	}

	public static void addRemap(String unloc, Item item, Enum sub) {
		addRemap(unloc, item, sub.ordinal());
	}

	public static void addRemap(String unloc, Item item, int meta) {
		Item remap = NativeItemTextureRegistry.texture(new ItemRemap(item, meta).setTranslationKey(unloc), RefStrings.MODID + ":plate_armor_titanium");
		GameRegistry.registerItem(remap, remap.getTranslationKey());
	}
}
