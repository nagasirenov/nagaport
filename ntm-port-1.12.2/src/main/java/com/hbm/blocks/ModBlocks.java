package com.hbm.blocks;

import com.hbm.blocks.BlockEnums.*;
import com.hbm.blocks.bomb.*;
import com.hbm.blocks.fluid.*;
import com.hbm.blocks.gas.*;
import com.hbm.blocks.generic.*;
import com.hbm.blocks.generic.BlockHazard.ExtDisplayEffect;
import com.hbm.blocks.generic.BlockOreFluid.ReserveType;
import com.hbm.blocks.machine.*;
import com.hbm.blocks.machine.albion.*;
import com.hbm.blocks.machine.fusion.MachineFusionBoiler;
import com.hbm.blocks.machine.fusion.MachineFusionBreeder;
import com.hbm.blocks.machine.fusion.MachineFusionCollector;
import com.hbm.blocks.machine.fusion.MachineFusionCoupler;
import com.hbm.blocks.machine.fusion.MachineFusionKlystron;
import com.hbm.blocks.machine.fusion.MachineFusionKlystronCreative;
import com.hbm.blocks.machine.fusion.MachineFusionMHDT;
import com.hbm.blocks.machine.fusion.MachineFusionTorus;
import com.hbm.blocks.machine.pile.*;
import com.hbm.blocks.machine.rbmk.*;
import com.hbm.blocks.network.*;
import com.hbm.blocks.rail.*;
import com.hbm.blocks.test.*;
import com.hbm.blocks.turret.*;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.block.*;
import com.hbm.items.bomb.ItemPrototypeBlock;
import com.hbm.items.special.ItemOreBlock;
import com.hbm.lib.ModDamageSource;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.DoorDecl;
import com.hbm.tileentity.machine.storage.TileEntityFileCabinet;
import com.hbm.util.Compat;
import com.hbm.registry.GameRegistry;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Loader;

import java.util.ArrayList;

public class ModBlocks {

	public static void mainRegistry() {
		initializeBlock();
		registerBlock();
	}

	public static Block event_tester;
	public static Block obj_tester;
	public static Block structure_anchor;

	// Vanilla ores for other planets
	public static Block ore_iron;
	public static Block ore_gold;
	public static Block ore_redstone;
	public static Block ore_lapis;
	public static Block ore_emerald;
	public static Block ore_quartz;
	public static Block ore_diamond;

	public static Block ore_uranium;
	public static Block ore_uranium_scorched;
	public static Block ore_plutonium;
	public static Block ore_titanium;
	public static Block ore_sulfur;
	public static Block ore_glowstone;
	public static Block ore_thorium;
	public static Block ore_morkite;
	public static Block ore_niter;
	public static Block ore_copper;
	public static Block ore_nickel;
	public static Block ore_tungsten;
	public static Block ore_aluminium;
	public static Block ore_fluorite;
	public static Block ore_lead;
	public static Block ore_schrabidium;
	public static Block ore_beryllium;
	public static Block ore_australium;
	public static Block ore_rare;
	public static Block ore_cobalt;
	public static Block ore_cinnebar;
	public static Block ore_coltan;
	public static Block ore_lanthanium;
	public static Block ore_alexandrite;
	public static Block ore_mineral;
	public static Block ore_zinc;
	public static Block ore_lithium;
	public static Block ore_niobium;
	public static Block ore_cadmium;
	public static Block ore_palladium;
	public static Block ore_iodine;
	public static Block ore_arsenic;
	public static Block ore_silicon;
	public static Block ore_fire;
	public static Block ore_shale;

	public static Block ore_bedrock;
	public static Block ore_volcano;

	public static Block ore_nether_coal;
	public static Block ore_nether_smoldering;
	public static Block ore_nether_uranium;
	public static Block ore_nether_uranium_scorched;
	public static Block ore_nether_plutonium;
	public static Block ore_nether_tungsten;
	public static Block ore_nether_sulfur;
	public static Block ore_nether_fire;
	public static Block ore_nether_cobalt;
	public static Block ore_nether_schrabidium;

	public static Block ore_meteor;

	public static Block stone_gneiss;
	public static Block ore_gneiss_iron;
	public static Block ore_gneiss_gold;
	public static Block ore_gneiss_uranium;
	public static Block ore_gneiss_uranium_scorched;
	public static Block ore_gneiss_copper;
	public static Block ore_gneiss_asbestos;
	public static Block ore_gneiss_lithium;
	public static Block ore_gneiss_schrabidium;
	public static Block ore_gneiss_rare;
	public static Block ore_gneiss_gas; // nice ass

	public static Block gneiss_brick;
	public static Block gneiss_tile;
	public static Block gneiss_chiseled;

	public static Block stone_depth;
	public static Block ore_depth_cinnebar;
	public static Block ore_depth_zirconium;
	public static Block ore_depth_borax;
	public static Block cluster_depth_iron;
	public static Block cluster_depth_titanium;
	public static Block cluster_depth_tungsten;

	public static Block stone_keyhole;
	public static Block stone_keyhole_meta;

	public static Block stone_depth_nether;
	public static Block ore_depth_nether_neodymium;

	public static Block stone_porous;
	public static Block stone_resource;
	public static Block stalagmite;
	public static Block stalactite;
	public static Block stone_biome;

	public static Block depth_brick;
	public static Block depth_tiles;
	public static Block depth_nether_brick;
	public static Block depth_nether_tiles;
	public static Block depth_dnt;

	public static Block basalt;
	public static Block ore_basalt;
	public static Block basalt_smooth;
	public static Block basalt_brick;
	public static Block basalt_polished;
	public static Block basalt_tiles;

	public static Block cluster_iron;
	public static Block cluster_titanium;
	public static Block cluster_aluminium;
	public static Block cluster_copper;

	public static Block ore_oil;
	public static Block ore_oil_empty;
	public static Block ore_oil_sand;
	public static Block ore_bedrock_oil;
	public static Block ore_lignite;
	public static Block ore_asbestos;
	public static Block ore_gas;
	public static Block ore_gas_empty;
	public static Block ore_brine;
	public static Block ore_tikite;
	public static Block ore_brine_empty;

	public static Block ore_tekto;
	public static Block ore_tekto_empty;

	public static Block block_thorium;
	public static Block block_thorium_fuel;
	public static Block block_uranium;
	public static Block block_u233;
	public static Block block_u235;
	public static Block block_u238;
	public static Block block_uranium_fuel;
	public static Block block_neptunium;
	public static Block block_polonium;
	public static Block block_mox_fuel;
	public static Block block_plutonium;
	public static Block block_pu238;
	public static Block block_pu239;
	public static Block block_pu240;
	public static Block block_pu_mix;
	public static Block block_plutonium_fuel;
	public static Block block_titanium;
	public static Block block_sulfur;
	public static Block block_niter;
	public static Block block_copper;
	public static Block block_nickel;
	public static Block block_red_copper;
	public static Block block_tungsten;
	public static Block block_aluminium;
	public static Block block_fluorite;
	public static Block block_steel;
	public static Block block_tcalloy;
	public static Block block_cdalloy;
	public static Block block_lead;
	public static Block block_bismuth;
	public static Block block_cadmium;
	public static Block block_coltan;
	public static Block block_tantalium;
	public static Block block_niobium;
	public static Block block_trinitite;
	public static Block block_waste;
	public static Block block_waste_painted;
	public static Block block_waste_vitrified;
	public static Block ancient_scrap;
	public static Block block_corium;
	public static Block block_corium_cobble;
	public static Block block_scrap;
	public static Block block_electrical_scrap;
	public static Block block_beryllium;
	public static Block block_schraranium;
	public static Block block_schrabidium;
	public static Block block_schrabidate;
	public static Block block_solinium;
	public static Block block_schrabidium_fuel;
	public static Block block_euphemium;
	public static Block block_schrabidium_cluster;
	public static Block block_euphemium_cluster;
	public static Block block_dineutronium;
	public static Block block_advanced_alloy;
	public static Block block_magnetized_tungsten;
	public static Block block_combine_steel;
	public static Block block_desh;
	public static Block block_dura_steel;
	public static Block block_starmetal;
	public static Block block_polymer;
	public static Block block_bakelite;
	public static Block block_rubber;
	public static Block block_yellowcake;
	public static Block block_insulator;
	public static Block block_fiberglass;
	public static Block block_asbestos;
	public static Block block_cobalt;
	public static Block block_lithium;
	public static Block block_zirconium;
	public static Block block_white_phosphorus;
	public static Block block_red_phosphorus;
	public static Block block_fallout;
	public static Block block_foam;
	public static Block block_coke;
	public static Block block_graphite;
	public static Block block_graphite_drilled;
	public static Block block_graphite_fuel;
	public static Block block_graphite_plutonium;
	public static Block block_graphite_rod;
	public static Block block_graphite_source;
	public static Block block_graphite_lithium;
	public static Block block_graphite_tritium;
	public static Block block_graphite_detector;
	public static Block block_boron;
	public static Block block_lanthanium;
	public static Block block_ra226;
	public static Block block_actinium;
	public static Block block_tritium;
	public static Block block_semtex;
	public static Block block_c4;
	public static Block block_smore;
	public static Block block_slag;
	public static Block block_osmiridium;

	public static Block block_australium;

	public static Block block_cap;

	public static Block deco_titanium;
	public static Block deco_red_copper;
	public static Block deco_tungsten;
	public static Block deco_aluminium;
	public static Block deco_steel;
	public static Block deco_rusty_steel;
	public static Block deco_lead;
	public static Block deco_beryllium;
	public static Block deco_asbestos;
	public static Block deco_stainless;
	public static Block deco_rbmk;
	public static Block deco_rbmk_smooth;

	public static Block deco_emitter;
	public static Block part_emitter;
	public static Block deco_loot;
	public static Block pedestal;
	public static Block skeleton_holder;
	public static Block bobblehead;
	public static Block snowglobe;
	public static Block plushie;
	public static Block dungeon_spawner;

	public static Block gravel_obsidian;
	public static Block gravel_diamond;
	public static Block asphalt;
	public static Block asphalt_light;

	public static Block sandbags;
	public static Block wood_barrier;
	public static Block wood_structure;

	public static Block reinforced_brick;
	public static Block reinforced_ducrete;
	public static Block reinforced_glass;
	public static Block reinforced_glass_pane;
	public static Block reinforced_light;
	public static Block reinforced_sand;
	public static Block reinforced_lamp_off;
	public static Block reinforced_lamp_on;
	public static Block reinforced_laminate;
	public static Block reinforced_laminate_pane;

	public static Block lamp_tritium_green_off;
	public static Block lamp_tritium_green_on;
	public static Block lamp_tritium_blue_off;
	public static Block lamp_tritium_blue_on;

	public static Block lamp_demon;

	public static Block lantern;
	public static Block lantern_behemoth;

	public static Block spotlight_incandescent;
	public static Block spotlight_incandescent_off;
	public static Block spotlight_fluoro;
	public static Block spotlight_fluoro_off;
	public static Block spotlight_halogen;
	public static Block spotlight_halogen_off;
	public static Block spotlight_beam;
	public static Block floodlight;
	public static Block floodlight_beam;

	public static Block rebar;
	public static Block reinforced_stone;
	public static Block concrete_smooth;
	public static Block concrete_colored;
	public static Block concrete_colored_ext;
	public static Block concrete;
	public static Block concrete_asbestos;
	public static Block concrete_rebar;
	public static Block concrete_super;
	public static Block concrete_super_broken;
	public static Block ducrete_smooth;
	public static Block ducrete;
	public static Block concrete_pillar;
	public static Block brick_concrete;
	public static Block brick_concrete_mossy;
	public static Block brick_concrete_cracked;
	public static Block brick_concrete_broken;
	public static Block brick_concrete_marked;
	public static Block brick_ducrete;
	public static Block brick_obsidian;
	public static Block brick_light;
	public static Block brick_compound;
	public static Block brick_asbestos;
	public static Block brick_fire;

	public static Block lightstone;
	public static Block brick_forgotten;
	public static Block brick_forgotten_lock;

	public static Block concrete_slab;
	public static Block concrete_double_slab;
	public static Block concrete_brick_slab;
	public static Block concrete_brick_double_slab;
	public static Block brick_slab;
	public static Block brick_double_slab;
	public static Block stones_slab;
	public static Block stones_double_slab;

	public static Block concrete_smooth_stairs;
	public static Block concrete_stairs;
	public static Block concrete_asbestos_stairs;
	public static Block ducrete_smooth_stairs;
	public static Block ducrete_stairs;
	public static Block brick_concrete_stairs;
	public static Block brick_concrete_mossy_stairs;
	public static Block brick_concrete_cracked_stairs;
	public static Block brick_concrete_broken_stairs;
	public static Block brick_ducrete_stairs;
	public static Block reinforced_stone_stairs;
	public static Block reinforced_brick_stairs;
	public static Block brick_obsidian_stairs;
	public static Block brick_light_stairs;
	public static Block brick_compound_stairs;
	public static Block brick_asbestos_stairs;
	public static Block brick_fire_stairs;
	public static Block asphalt_stairs;
	public static Block lightstone_tile_stairs;
	public static Block lightstone_bricks_stairs;

	public static Block cmb_brick;
	public static Block cmb_brick_reinforced;

	public static Block vinyl_tile;

	public static Block tile_lab;
	public static Block tile_lab_cracked;
	public static Block tile_lab_broken;

	public static Block block_meteor;
	public static Block block_meteor_cobble;
	public static Block block_meteor_broken;
	public static Block block_meteor_molten;
	public static Block block_meteor_treasure;
	public static Block meteor_polished;
	public static Block meteor_brick;
	public static Block meteor_brick_mossy;
	public static Block meteor_brick_cracked;
	public static Block meteor_brick_chiseled;
	public static Block meteor_pillar;
	public static Block meteor_spawner;
	public static Block meteor_battery;

	public static Block moon_turf;
	public static Block moon_rock;

	public static Block tumor;


	public static Block duna_sands;
	public static Block duna_cobble;
	public static Block duna_rock;
	public static Block dry_ice;
	public static Block ferric_clay;

	public static Block ike_regolith;
	public static Block ike_stone; //Genuinely wish i stopped fucking doing this 5000 stone type shit

	public static Block dres_rock;

	public static Block laythe_silt;

	public static Block eve_silt;
	public static Block eve_rock;

	public static Block moho_regolith;
	public static Block moho_stone;

	public static Block minmus_regolith;
	public static Block minmus_stone;
	public static Block minmus_smooth;



	public static Block brick_jungle;
	public static Block brick_jungle_cracked;
	public static Block brick_jungle_fragile;
	public static Block brick_jungle_lava;
	public static Block brick_jungle_ooze;
	public static Block brick_jungle_mystic;
	public static Block brick_jungle_trap;
	public static Block brick_jungle_glyph;
	public static Block brick_jungle_circle;

	public static Block brick_red;

	public static Block deco_computer;
	public static Block deco_crt;
	public static Block deco_toaster;

	public static Block filing_cabinet;

	public static Block tape_recorder;
	public static Block steel_poles;
	public static Block pole_top;
	public static Block pole_satellite_receiver;
	public static Block steel_wall;
	public static Block steel_corner;
	public static Block steel_roof;
	public static Block steel_beam;
	public static Block steel_scaffold;
	public static Block steel_grate;
	public static Block steel_grate_wide;

	@Deprecated public static Block deco_pipe;
	@Deprecated public static Block deco_pipe_rusted;
	@Deprecated public static Block deco_pipe_green;
	@Deprecated public static Block deco_pipe_green_rusted;
	@Deprecated public static Block deco_pipe_red;
	@Deprecated public static Block deco_pipe_marked;
	@Deprecated public static Block deco_pipe_rim;
	@Deprecated public static Block deco_pipe_rim_rusted;
	@Deprecated public static Block deco_pipe_rim_green;
	@Deprecated public static Block deco_pipe_rim_green_rusted;
	@Deprecated public static Block deco_pipe_rim_red;
	@Deprecated public static Block deco_pipe_rim_marked;
	@Deprecated public static Block deco_pipe_framed;
	@Deprecated public static Block deco_pipe_framed_rusted;
	@Deprecated public static Block deco_pipe_framed_green;
	@Deprecated public static Block deco_pipe_framed_green_rusted;
	@Deprecated public static Block deco_pipe_framed_red;
	@Deprecated public static Block deco_pipe_framed_marked;
	@Deprecated public static Block deco_pipe_quad;
	@Deprecated public static Block deco_pipe_quad_rusted;
	@Deprecated public static Block deco_pipe_quad_green;
	@Deprecated public static Block deco_pipe_quad_green_rusted;
	@Deprecated public static Block deco_pipe_quad_red;
	@Deprecated public static Block deco_pipe_quad_marked;

	public static Block broadcaster_pc;
	public static Block geiger;
	public static Block hev_battery;

	public static Block fence_metal;

	public static Block sand_mix;
	public static Block ash_digamma;
	public static Block glass_boron;
	public static Block glass_lead;
	public static Block glass_uranium;
	public static Block glass_trinitite;
	public static Block glass_polonium;
	public static Block glass_ash;
	public static Block glass_quartz;
	public static Block glass_polarized;

	public static Block mush;
	public static Block mush_block;
	public static Block mush_block_stem;

	public static Block glyphid_base;
	public static Block glyphid_spawner;

	public static Block plant_flower;
	public static Block plant_tall;
	public static Block plant_dead;
	public static Block reeds;
	public static Block plant_tall_laythe;
	public static Block laythe_kelp;
	public static Block laythe_short;
	public static Block laythe_glow;
	public static Block laythe_coral;
	public static Block laythe_coral_block;

	public static Block crop_strawberry;
	public static Block crop_mint;
	public static Block crop_coffee;
	public static Block crop_tea;
	public static Block vine_phosphor;
	public static final Material thick_foliage = new MaterialLogic(MapColor.FOLIAGE) {
		@Override public boolean getCanBurn()  { return true; }
		@Override public boolean isToolNotRequired() { return false; }
		@Override public EnumPushReaction getPushReaction() { return EnumPushReaction.DESTROY; }
	};

	public static Block crop_paraffin;
	public static Block sapling_pvc;

	public static Block waste_earth;
	public static Block waste_mycelium;
	public static Block waste_trinitite;
	public static Block waste_trinitite_red;
	public static Block waste_log;
	public static Block waste_leaves;
	public static Block waste_planks;
	public static Block frozen_dirt;
	public static Block frozen_grass;
	public static Block frozen_log;
	public static Block frozen_planks;
	public static Block dirt_dead;
	public static Block dirt_oily;
	public static Block sand_dirty;
	public static Block sand_dirty_red;
	public static Block stone_cracked;
	public static Block burning_earth;
	public static Block tektite;
	public static Block ore_tektite_osmiridium;
	public static Block impact_dirt;

	public static Block rubber_leaves;
	public static Block rubber_grass;
	public static Block rubber_silt;
	public static Block spike_cacti;
	public static Block rubber_farmland;
	public static Block vinyl_sand;
	public static Block vinyl_vines;
	public static Block pet_leaves;
	public static Block vinyl_planks;
	public static Block pvc_planks;

	public static Block astroturf;

	public static Block fallout;
	public static Block salted_fallout;
	public static Block foam_layer;
	public static Block sand_boron_layer;
	public static Block leaves_layer;
	public static Block oil_spill;

	public static Block sellafield_slaked;
	public static Block sellafield;
	public static Block ore_sellafield_diamond;
	public static Block ore_sellafield_emerald;
	public static Block ore_sellafield_uranium_scorched;
	public static Block ore_sellafield_schrabidium;
	public static Block ore_sellafield_radgem;
	public static Block sellafield_bedrock;

	public static Block geysir_water;
	public static Block geysir_chlorine;
	public static Block geysir_vapor;
	public static Block geysir_nether;
	public static Block geysir_electric;
	public static Block geysir_volcanic;
	public static Block geysir_chloric;

	public static Block flame_war;
	public static Block float_bomb;
	public static Block therm_endo;
	public static Block therm_exo;
	public static Block emp_bomb;
	public static Block det_cord;
	public static Block det_charge;
	public static Block det_nuke;
	public static Block det_salt;
	public static Block det_miner;
	public static Block red_barrel;
	public static Block pink_barrel;
	public static Block yellow_barrel;
	public static Block vitrified_barrel;
	public static Block lox_barrel;
	public static Block taint_barrel;
	public static Block oil_barrel;
	public static Block crashed_balefire;
	public static Block fireworks;
	public static Block dynamite;
	public static Block tnt;
	public static Block semtex;
	public static Block c4;
	public static Block fissure_bomb;

	public static Block charge_dynamite;
	public static Block charge_miner;
	public static Block charge_c4;
	public static Block charge_semtex;

	public static Block mine_ap;
	public static Block mine_he;
	public static Block mine_shrap;
	public static Block mine_fat;
	public static Block mine_naval;

	public static Block crate;
	public static Block crate_weapon;
	public static Block crate_lead;
	public static Block crate_metal;
	public static Block crate_red;
	public static Block crate_can;
	public static Block crate_ammo;
	public static Block crate_jungle;

	public static Block boxcar;
	public static Block boat;

	public static Block seal_frame;
	public static Block seal_controller;
	public static Block seal_hatch;

	public static Block vault_door;
	public static Block blast_door;
	public static Block sliding_blast_door;
	public static Block fire_door;
	public static Block transition_seal;
	public static Block silo_hatch;
	public static Block silo_hatch_large;

	// 1.12.2 Doors
	public static Block secure_access_door;
	public static Block large_vehicle_door;
	public static Block qe_containment;
	public static Block qe_sliding_door;
	public static Block round_airlock_door;
	public static Block sliding_seal_door;
	public static Block water_door;

	public static Block door_metal;
	public static Block door_office;
	public static Block door_bunker;
	public static Block door_red;

	public static Block fence_gate;

	public static Block barbed_wire;
	public static Block barbed_wire_fire;
	public static Block barbed_wire_poison;
	public static Block barbed_wire_acid;
	public static Block barbed_wire_wither;
	public static Block barbed_wire_ultradeath;
	public static Block spikes;

	public static Block charger;
	public static Block refueler;

	public static Block tesla;

	public static Block sat_dock;
	public static Block gas_dock;
	public static Block soyuz_capsule;
	public static Block crate_supply;
	public static Block combat_drop;

	public static Block crate_iron;
	public static Block crate_steel;
	public static Block crate_desh;
	public static Block crate_tungsten;
	public static Block crate_template;
	public static Block safe;
	public static Block mass_storage;

	public static Block nuke_gadget;
	public static Block nuke_boy;
	public static Block nuke_man;
	public static Block nuke_mike;
	public static Block nuke_tsar;
	public static Block nuke_fleija;
	public static Block nuke_prototype;
	public static Block nuke_custom;
	public static Block nuke_solinium;
	public static Block nuke_n2;
	public static Block nuke_fstbmb;
	public static Block nuke_antimatter;
	public static Block bomb_multi;

	public static Block pump_steam;
	public static Block pump_electric;

	public static Block heater_firebox;
	public static Block heater_oven;
	public static Block heater_oilburner;
	public static Block heater_electric;
	public static Block heater_heatex;
	public static Block machine_ashpit;

	public static Block furnace;
	public static Block lit_furnace;

	public static Block furnace_iron;
	public static Block furnace_steel;
	public static Block furnace_combination;
	public static Block machine_stirling;
	public static Block machine_stirling_steel;
	public static Block machine_stirling_creative;
	public static Block machine_sawmill;
	public static Block machine_crucible;
	public static Block machine_boiler;
	public static Block machine_industrial_boiler;

	public static Block foundry_mold;
	public static Block foundry_basin;
	public static Block foundry_channel;
	public static Block foundry_tank;
	public static Block foundry_outlet;
	public static Block machine_strand_caster;
	public static Block foundry_slagtap;
	public static Block slag;

	public static Block machine_difurnace_off;
	public static Block machine_difurnace_on;
	public static Block machine_difurnace_extension;
	public static Block machine_difurnace_rtg_off;
	public static Block machine_difurnace_rtg_on;
	//public static final int guiID_test_difurnace = 1; historical

	public static Block machine_centrifuge;
	public static Block machine_gascent;

	public static Block machine_fel;
	public static Block machine_silex;

	public static Block machine_rotary_furnace;
	public static Block machine_crystallizer;

	public static Block machine_uf6_tank;
	public static Block machine_puf6_tank;

	public static Block machine_reactor_breeding;

	public static Block machine_furnace_brick_off;
	public static Block machine_furnace_brick_on;
	public static Block machine_rtg_furnace_off;
	public static Block machine_rtg_furnace_on;

	public static Block machine_industrial_generator;

	public static Block machine_cyclotron;
	public static Block machine_exposure_chamber;
	public static Block hadron_coil_alloy;
	public static Block hadron_coil_gold;
	public static Block hadron_coil_neodymium;
	public static Block hadron_coil_magtung;
	public static Block hadron_coil_schrabidium;
	public static Block hadron_coil_schrabidate;
	public static Block hadron_coil_starmetal;
	public static Block hadron_coil_chlorophyte;
	public static Block hadron_coil_mese;

	public static Block pa_source;
	public static Block pa_beamline;
	public static Block pa_rfc;
	public static Block pa_quadrupole;
	public static Block pa_dipole;
	public static Block pa_detector;

	public static Block machine_electric_furnace_off;
	public static Block machine_electric_furnace_on;
	public static Block machine_microwave;

	public static Block machine_battery_socket;
	public static Block machine_battery_redd;

	@Deprecated public static Block machine_battery_potato;
	@Deprecated public static Block machine_battery;
	@Deprecated public static Block machine_lithium_battery;
	@Deprecated public static Block machine_schrabidium_battery;
	@Deprecated public static Block machine_dineutronium_battery;
	@Deprecated public static Block machine_fensu;

	@Deprecated public static Block capacitor_bus;
	public static Block capacitor_copper; // neat for structures
	@Deprecated public static Block capacitor_gold;
	@Deprecated public static Block capacitor_niobium;
	@Deprecated public static Block capacitor_tantalium;
	@Deprecated public static Block capacitor_complex;

	@Deprecated public static Block capacitor_schrabidate;

	public static Block machine_wood_burner;

	public static Block red_wire_coated;
	public static Block red_cable;
	public static Block red_cable_classic;
	public static Block red_cable_paintable;
	public static Block red_cable_gauge;
	public static Block red_cable_box;
	public static Block red_connector;
	public static Block red_connector_super;
	public static Block red_pylon;
	public static Block red_pylon_medium_wood;
	public static Block red_pylon_medium_wood_transformer;
	public static Block red_pylon_medium_steel;
	public static Block red_pylon_medium_steel_transformer;
	public static Block red_pylon_large;
	public static Block substation;
	public static Block cable_switch;
	public static Block cable_detector;
	public static Block cable_diode;
	public static Block machine_detector;
	public static Block fluid_duct_neo;
	public static Block fluid_duct_box;
	public static Block fluid_duct_paintable;
	public static Block fluid_duct_gauge;
	public static Block fluid_duct_exhaust;
	public static Block fluid_duct_paintable_block_exhaust;
	public static Block pipe_anchor;
	public static Block fluid_valve;
	public static Block fluid_switch;
	public static Block fluid_counter_valve;
	public static Block fluid_pump;
	public static Block machine_drain;
	public static Block radio_torch_sender;
	public static Block radio_torch_receiver;
	public static Block radio_torch_counter;
	public static Block radio_torch_logic;
	public static Block radio_torch_reader;
	public static Block radio_torch_controller;
	public static Block radio_telex;
	public static Block oc_cable_paintable;

	public static Block conveyor;
	public static Block conveyor_express;
	public static Block conveyor_double;
	public static Block conveyor_triple;
	public static Block conveyor_chute;
	public static Block conveyor_lift;
	public static Block crane_extractor;
	public static Block crane_inserter;
	public static Block crane_grabber;
	public static Block crane_router;
	public static Block crane_boxer;
	public static Block crane_unboxer;
	public static Block crane_splitter;
	public static Block crane_partitioner;

	public static Block drone_waypoint;
	public static Block drone_crate;
	public static Block drone_waypoint_request;
	public static Block drone_dock;
	public static Block drone_crate_provider;
	public static Block drone_crate_requester;

	public static Block pneumatic_tube;
	public static Block pneumatic_tube_paintable;

	public static Block fan;
	public static Block piston_inserter;

	public static Block chain;

	public static Block ladder_sturdy;
	public static Block ladder_iron;
	public static Block ladder_gold;
	public static Block ladder_aluminium;
	public static Block ladder_copper;
	public static Block ladder_titanium;
	public static Block ladder_lead;
	public static Block ladder_cobalt;
	public static Block ladder_steel;
	public static Block ladder_tungsten;

	public static Block trapdoor_steel;

	public static Block barrel_plastic;
	@Deprecated public static Block barrel_corroded;
	@Deprecated public static Block barrel_iron;
	public static Block barrel_steel;
	public static Block barrel_tcalloy;
	public static Block barrel_antimatter;

	public static Block machine_transformer;

	public static Block machine_solar_boiler;
	public static Block solar_mirror;

	public static Block struct_launcher;
	public static Block struct_scaffold;
	public static Block struct_launcher_core;
	public static Block struct_launcher_core_large;
	public static Block struct_soyuz_core;
	public static Block struct_torus_core;
	public static Block struct_watz_core;
	public static Block struct_icf_core;

	public static Block factory_titanium_hull;
	public static Block factory_advanced_hull;

	public static Block cm_block;
	public static Block cm_sheet;
	public static Block cm_engine;
	public static Block cm_tank;
	public static Block cm_circuit;
	public static Block cm_port;
	public static Block cm_flux;
	public static Block cm_heat;
	public static Block custom_machine;
	public static Block cm_anchor;

	public static Block pwr_fuel;
	public static Block pwr_control;
	public static Block pwr_channel;
	public static Block pwr_heatex;
	public static Block pwr_heatsink;
	public static Block pwr_neutron_source;
	public static Block pwr_reflector;
	public static Block pwr_casing;
	public static Block pwr_port;
	public static Block pwr_controller;
	public static Block pwr_block;

	@Deprecated public static Block fusion_heater;
	@Deprecated public static Block fusion_hatch;
	@Deprecated public static Block plasma; // only actually used by the old plasma grenade, will die with the grenade rework

	@Deprecated public static Block iter;
	@Deprecated public static Block plasma_heater;

	public static Block fusion_component;
	public static Block fusion_torus;
	public static Block fusion_klystron;
	public static Block fusion_klystron_creative;
	public static Block fusion_breeder;
	public static Block fusion_collector;
	public static Block fusion_boiler;
	public static Block fusion_mhdt;
	public static Block fusion_coupler;

	public static Block machine_icf_press;
	public static Block icf_component;
	public static Block icf;
	public static Block icf_controller;
	public static Block icf_laser_component;
	public static Block icf_block;

	public static Block watz;
	public static Block watz_pump;

	public static Block watz_element;
	public static Block watz_cooler;
	public static Block watz_end;

	public static Block balefire;
	public static Block fire_digamma;
	public static Block digamma_matter;

	public static Block dfc_emitter;
	public static Block dfc_injector;
	public static Block dfc_receiver;
	public static Block dfc_stabilizer;
	public static Block dfc_core;

	public static Block machine_converter_he_rf;
	public static Block machine_converter_rf_he;

	public static Block machine_diesel;
	public static Block machine_combustion_engine;

	public static Block machine_shredder;

	public static Block machine_teleporter;
	public static Block teleanchor;
	public static Block field_disturber;
	public static Block troll_disturber;

	public static Block machine_rtg_grey;
	public static Block machine_minirtg;
	public static Block machine_powerrtg;
	public static Block machine_radiolysis;
	public static Block machine_hephaestus;
	public static Block machine_solar;
	public static Block machine_stardar;
	public static Block machine_drive_processor;
	public static Block machine_alkylation;

	public static Block machine_well;
	public static Block oil_pipe;
	public static Block machine_pumpjack;
	public static Block machine_fracking_tower;

	public static Block machine_flare;
	public static Block chimney_brick;
	public static Block chimney_industrial;

	public static Block machine_refinery;
	public static Block machine_vacuum_distill;
	public static Block machine_fraction_tower;
	public static Block fraction_spacer;
	public static Block machine_catalytic_cracker;
	public static Block machine_catalytic_reformer;
	public static Block machine_hydrotreater;
	public static Block machine_coker;
	public static Block machine_pyrooven;
	public static Block machine_milk_reformer;

	public static Block machine_cryo_distill;

	public static Block machine_boiler_off;

	public static Block machine_steam_engine;
	public static Block machine_turbine;
	@Deprecated public static Block machine_large_turbine;
	public static Block machine_industrial_turbine;
	public static Block machine_chungus;

	public static Block machine_condenser;
	public static Block machine_tower_small;
	public static Block machine_tower_large;
	public static Block machine_condenser_powered;

	public static Block machine_deuterium_extractor;
	public static Block machine_deuterium_tower;
	public static Block machine_atmo_tower;
	public static Block machine_atmo_vent;

	public static Block machine_liquefactor;
	public static Block machine_solidifier;
	public static Block machine_intake;
	public static Block machine_atmo_emitter;
	public static Block machine_compressor;
	public static Block machine_compressor_compact;
	public static Block machine_radiator;

	public static Block machine_electrolyser;

	public static Block machine_excavator;
	public static Block machine_ore_slopper;
	public static Block machine_annihilator;
	public static Block machine_autosaw;
	public static Block machine_magma;

	public static Block machine_mining_laser;
	public static Block barricade; // a sand bag that drops nothing, for automated walling purposes

	public static Block machine_assembly_machine;
	public static Block machine_assembly_factory;
	public static Block machine_precass;
	public static Block machine_arc_welder;
	public static Block machine_soldering_station;
	public static Block machine_arc_furnace;
	public static Block machine_vacuum_circuit;

	public static Block machine_chemical_plant;
	public static Block machine_chemical_factory;
	public static Block machine_purex;
	public static Block machine_mixer;

	public static Block machine_fluidtank;
	public static Block machine_bat9000;
	public static Block machine_orbus;

	public static Block launch_pad;
	public static Block launch_pad_rocket;

	public static Block launch_pad_rusted;
	public static Block launch_pad_large;

	public static Block machine_missile_assembly;
	public static Block machine_rocket_assembly;

	public static Block compact_launcher;
	public static Block launch_table;

	public static Block soyuz_launcher;

	public static Block machine_radar;
	public static Block machine_radar_large;
	public static Block radar_screen;

	public static Block machine_turbofan;
	public static Block machine_turbinegas;
	public static Block machine_lpw2;
	public static Block machine_htr3;
	public static Block machine_htrf4;
	public static Block machine_xenon_thruster;
	public static Block machine_htrf4neo;

	public static Block press_preheater;
	public static Block machine_press;
	public static Block machine_epress;
	public static Block machine_conveyor_press;
	public static Block machine_ammo_press;

	public static Block machine_siren;

	public static Block machine_radgen;

	public static Block machine_satlinker;
	public static Block machine_keyforge;

	public static Block machine_armor_table;
	public static Block machine_weapon_table;

	public static Block reactor_research;
	public static Block reactor_zirnox;
	public static Block zirnox_destroyed;

	public static Block machine_controller;
	public static Block machine_dish_controller;

	public static Block radiobox;
	public static Block radiorec;

	public static Block machine_forcefield;

	public static Block machine_waste_drum;
	public static Block machine_storage_drum;

	public static Block machine_autocrafter;
	public static Block machine_funnel;

	public static Block anvil_iron;
	public static Block anvil_lead;
	public static Block anvil_steel;
	public static Block anvil_desh;
	public static Block anvil_ferrouranium;
	public static Block anvil_saturnite;
	public static Block anvil_bismuth_bronze;
	public static Block anvil_arsenic_bronze;
	public static Block anvil_schrabidate;
	public static Block anvil_dnt;
	public static Block anvil_osmiridium;
	public static Block anvil_murky;

	public static Block turret_chekhov;
	public static Block turret_friendly;
	public static Block turret_jeremy;
	public static Block turret_tauon;
	public static Block turret_richard;
	public static Block turret_howard;
	public static Block turret_howard_damaged;
	public static Block turret_maxwell;
	public static Block turret_fritz;
	public static Block turret_arty;
	public static Block turret_himars;
	public static Block turret_sentry;
	public static Block turret_sentry_damaged;

	public static Block rbmk_rod;
	public static Block rbmk_rod_mod;
	public static Block rbmk_rod_reasim;
	public static Block rbmk_rod_reasim_mod;
	public static Block rbmk_control;
	public static Block rbmk_control_mod;
	public static Block rbmk_control_auto;
	public static Block rbmk_control_reasim;
	public static Block rbmk_control_reasim_auto;
	public static Block rbmk_blank;
	public static Block rbmk_boiler;
	public static Block rbmk_reflector;
	public static Block rbmk_absorber;
	public static Block rbmk_moderator;
	public static Block rbmk_outgasser;
	public static Block rbmk_storage;
	public static Block rbmk_cooler;
	public static Block rbmk_heater;
	public static Block rbmk_console;
	public static Block rbmk_crane_console;
	public static Block rbmk_display;
	public static Block rbmk_key_pad;
	public static Block rbmk_gauge;
	public static Block rbmk_autoloader;
	public static Block rbmk_loader;
	public static Block rbmk_steam_inlet;
	public static Block rbmk_steam_outlet;
	public static Block pribris;
	public static Block pribris_burning;
	public static Block pribris_radiating;
	public static Block pribris_digamma;

	public static Block book_guide;

	public static Block rail_wood;
	public static Block rail_narrow;
	public static Block rail_highspeed;
	public static Block rail_booster;

	public static Block rail_narrow_straight;
	public static Block rail_narrow_curve;
	public static Block rail_large_straight;
	public static Block rail_large_straight_short;
	public static Block rail_large_curve;
	public static Block rail_large_curve_7;
	public static Block rail_large_curve_9;
	public static Block rail_large_ramp;
	public static Block rail_large_buffer;
	public static Block rail_large_switch;
	public static Block rail_large_switch_flipped;

	public static Block war_controller;


	public static Block statue_elb;
	public static Block statue_elb_g;
	public static Block statue_elb_w;

	public static Block statue_elb_f;

	public static Block cheater_virus;
	public static Block cheater_virus_seed;
	public static Block crystal_virus;
	public static Block crystal_hardened;
	public static Block crystal_pulsar;
	public static Block taint;

	public static Block vent_chlorine;
	public static Block vent_cloud;
	public static Block vent_pink_cloud;
	public static Block vent_chlorine_seal;
	public static Block chlorine_gas;

	public static Block air_vent;
	public static Block air_scrubber;
	public static Block algae_film;
	public static Block hydrobay;

	public static Block gas_radon;
	public static Block gas_radon_dense;
	public static Block gas_radon_tomb;
	public static Block gas_meltdown;
	public static Block gas_monoxide;
	public static Block gas_asbestos;
	public static Block gas_coal;
	public static Block gas_flammable;
	public static Block gas_explosive;
	public static Block vacuum;

	@Deprecated public static Block absorber;
	@Deprecated public static Block absorber_red;
	@Deprecated public static Block absorber_green;
	@Deprecated public static Block absorber_pink;
	public static Block rad_absorber;
	public static Block decon;

	public static Block mud_block;
	public static Fluid mud_fluid;
	public static final Material fluidmud = (new MaterialLiquid(MapColor.ADOBE));

	public static Block acid_block;
	public static Fluid acid_fluid;
	public static final Material fluidacid = (new MaterialLiquid(MapColor.PURPLE));

	public static Block toxic_block;
	public static Fluid toxic_fluid;
	public static final Material fluidtoxic = (new MaterialLiquid(MapColor.GREEN));

	public static Block schrabidic_block;
	public static Fluid schrabidic_fluid;
	public static final Material fluidschrabidic = (new MaterialLiquid(MapColor.CYAN));

	public static Block corium_block;
	public static Fluid corium_fluid;
	public static final Material fluidcorium = (new MaterialLiquid(MapColor.BROWN) {
		@Override public boolean blocksMovement() { return true; }
		@Override public Material setImmovableMobility() { return super.setImmovableMobility(); } //override access modifier
	}.setImmovableMobility());

	public static Block volcanic_lava_block;
	public static Fluid volcanic_lava_fluid;
	public static Block rad_lava_block;
	public static Fluid rad_lava_fluid;

	public static Block sulfuric_acid_block;
	public static Fluid sulfuric_acid_fluid;

	public static Block mercury_block;
	public static Fluid mercury_fluid;

	public static Block bromine_block;
	public static Fluid bromine_fluid;

	public static Block concrete_liquid;

	public static Block ccl_block;
	public static Fluid ccl_fluid;


	public static Block flesh_block;
	public static Block charred_flesh_block;
	public static Block carbonized_flesh_block;
	//public static Fluid flesh_fluid;

	public static Block volcano_core;
	public static Block volcano_rad_core;

	public static Block dummy_block_blast;
	public static Block dummy_plate_compact_launcher;
	public static Block dummy_port_compact_launcher;
	public static Block dummy_plate_launch_table;
	public static Block dummy_port_launch_table;
	public static Block dummy_plate_cargo;

	public static Block ntm_dirt;

	public static Block pink_log;
	public static Block pink_planks;
	public static Block pink_slab;
	public static Block pink_double_slab;
	public static Block pink_stairs;

	public static Block bf_log;
	public static Block lattice_log;
	public static Block primed_log;
	public static Block eu_log;


	public static Block pvc_log;
	public static Block rubber_plant;
	public static Block vinyl_log;

	public static Block ff;

	public static Block atmosphere_editor;

	public static Block transporter_rocket;
	public static Block orbital_station;
	public static Block orbital_station_port;
	public static Block orbital_station_launcher;
	public static Block orbital_station_computer;
	public static Block propulsion_creative;

	public static Block dyson_launcher;
	public static Block dyson_receiver;
	public static Block dyson_converter_tu;
	public static Block dyson_converter_he;
	public static Block dyson_converter_anatmogenesis;

	public static Block orrery;

	public static Block dummy_beam;

	// NBT Structure wand blocks
	public static Block wand_air;
	public static Block wand_loot;
	public static Block wand_jigsaw;
	public static Block wand_logic;
	public static Block wand_tandem;
	public static Block wand_structure;

	public static Block logic_block;

	public static Material materialGas = new MaterialGas();

	private static Block createBarbedWire(String registryName) {
		BarbedWire block = new BarbedWire(Material.IRON);
		block.setModelTexture(RefStrings.MODID + ":blocks/" + registryName + "_model");
		block.setTranslationKey(registryName);
		block.setRegistryName(new ResourceLocation(RefStrings.MODID, block.getTranslationKey()));
		block.setHardness(5.0F);
		block.setResistance(10.0F);
		block.setCreativeTab(MainRegistry.blockTab);
		return block;
	}

	private static void initializeBlock() {

		event_tester = new TestEventTester(Material.IRON).setTranslationKey("event_tester").setCreativeTab(null).setHardness(2.5F).setResistance(0.0F).setBlockTextureName(RefStrings.MODID + ":event_tester");
		obj_tester = new TestObjTester(Material.IRON).setTranslationKey("obj_tester").setCreativeTab(null).setHardness(2.5F).setResistance(10.0F);
		structure_anchor = ((BlockBase) new BlockGeneric(Material.IRON).setBlockName("structure_anchor").setCreativeTab(null).setHardness(2.5F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":structure_anchor");

		ore_iron = new BlockOre(Material.ROCK, Blocks.IRON_ORE).setTranslationKey("ore_iron").setCreativeTab(MainRegistry.blockTab).setHardness(3.0F).setResistance(5.0F);
		ore_gold = new BlockOre(Material.ROCK, Blocks.GOLD_ORE).setTranslationKey("ore_gold").setCreativeTab(MainRegistry.blockTab).setHardness(3.0F).setResistance(5.0F);
		ore_redstone = new BlockOre(Material.ROCK, Blocks.REDSTONE_ORE).setTranslationKey("ore_redstone").setCreativeTab(MainRegistry.blockTab).setHardness(3.0F).setResistance(5.0F);
		ore_lapis = new BlockOre(Material.ROCK, Blocks.LAPIS_ORE).setTranslationKey("ore_lapis").setCreativeTab(MainRegistry.blockTab).setHardness(3.0F).setResistance(5.0F);
		ore_emerald = new BlockOre(Material.ROCK, Blocks.EMERALD_ORE).setTranslationKey("ore_emerald").setCreativeTab(MainRegistry.blockTab).setHardness(3.0F).setResistance(5.0F);
		ore_quartz = new BlockOre(Material.ROCK, Blocks.QUARTZ_ORE).setTranslationKey("ore_quartz").setCreativeTab(MainRegistry.blockTab).setHardness(3.0F).setResistance(5.0F);
		ore_diamond = new BlockOre(Material.ROCK, Blocks.DIAMOND_ORE).setTranslationKey("ore_diamond").setCreativeTab(MainRegistry.blockTab).setHardness(3.0F).setResistance(5.0F);

		ore_uranium = new BlockOreOutgas(Material.ROCK, true, 5, true).setTranslationKey("ore_uranium").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_uranium_scorched = com.hbm.blocks.PlainCubeTextures.bind(new BlockOutgas(Material.ROCK, true, 5, true).setTranslationKey("ore_uranium_scorched").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F), RefStrings.MODID + ":ore_uranium_scorched");
		ore_plutonium = new BlockOreOutgas(Material.ROCK, true, 5, true).setTranslationKey("ore_plutonium").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_titanium = new BlockOre(Material.ROCK).setTranslationKey("ore_titanium").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_sulfur = new BlockOre(Material.ROCK).setTranslationKey("ore_sulfur").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_glowstone = new BlockOre(Material.ROCK).setLightLevel(4F/15F).setTranslationKey("ore_glowstone").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_thorium = new BlockOre(Material.ROCK).setTranslationKey("ore_thorium").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_morkite = new BlockOre(Material.ROCK).withSoundType(ModSoundTypes.mork).setTranslationKey("ore_morkite").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_niter = new BlockOre(Material.ROCK).setTranslationKey("ore_niter").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_copper = new BlockOre(Material.ROCK).setTranslationKey("ore_copper").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_nickel = new BlockOre(Material.ROCK).setTranslationKey("ore_nickel").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_mineral = new BlockOre(Material.ROCK).setTranslationKey("ore_mineral").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_zinc = new BlockOre(Material.ROCK).setTranslationKey("ore_zinc").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_lithium = new BlockOre(Material.ROCK).setTranslationKey("ore_lithium").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_niobium = new BlockOre(Material.ROCK).setTranslationKey("ore_niobium").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_palladium = new BlockOre(Material.ROCK).setTranslationKey("ore_palladium").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_iodine = new BlockOre(Material.ROCK).setTranslationKey("ore_iodine").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_arsenic = new BlockOre(Material.ROCK).setTranslationKey("ore_arsenic").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_cadmium = new BlockOre(Material.ROCK).setTranslationKey("ore_cadmium").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_tungsten = new BlockOre(Material.ROCK).setTranslationKey("ore_tungsten").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_aluminium = new BlockOre(Material.ROCK).setTranslationKey("ore_aluminium").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_fluorite = new BlockOre(Material.ROCK).setTranslationKey("ore_fluorite").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_lead = new BlockOre(Material.ROCK).setTranslationKey("ore_lead").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_schrabidium = new BlockOre(Material.ROCK).setTranslationKey("ore_schrabidium").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(600.0F);
		ore_beryllium = new BlockOre(Material.ROCK).setTranslationKey("ore_beryllium").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(15.0F);
		ore_lignite = new BlockOre(Material.ROCK).setTranslationKey("ore_lignite").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(15.0F);
		ore_asbestos = new BlockOreOutgas(Material.ROCK, true, 5, true).setTranslationKey("ore_asbestos").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(15.0F);
		ore_silicon = new BlockOre(Material.ROCK).setTranslationKey("ore_silicon").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_fire = new BlockOre(Material.ROCK).setTranslationKey("ore_fire").setCreativeTab(MainRegistry.blockTab).setHardness(0.4F).setResistance(10.0F);
		ore_shale = new BlockOre(Material.ROCK).setTranslationKey("ore_shale").setCreativeTab(MainRegistry.blockTab).setHardness(1.5F).setResistance(10.0F);

		cluster_iron = new BlockCluster(Material.ROCK).setTranslationKey("cluster_iron").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(15.0F);
		cluster_titanium = new BlockCluster(Material.ROCK).setTranslationKey("cluster_titanium").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(15.0F);
		cluster_aluminium = new BlockCluster(Material.ROCK).setTranslationKey("cluster_aluminium").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(15.0F);
		cluster_copper = new BlockCluster(Material.ROCK).setTranslationKey("cluster_copper").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(15.0F);

		ore_nether_coal = com.hbm.blocks.PlainCubeTextures.bind(new BlockNetherCoal(Material.ROCK, false, 5, true).setTranslationKey("ore_nether_coal").setCreativeTab(MainRegistry.blockTab).setLightLevel(10F/15F).setHardness(0.4F).setResistance(10.0F), RefStrings.MODID + ":ore_nether_coal");
		ore_nether_smoldering = com.hbm.blocks.PlainCubeTextures.bind(new BlockSmolder(Material.ROCK).setTranslationKey("ore_nether_smoldering").setCreativeTab(MainRegistry.blockTab).setLightLevel(1F).setHardness(0.4F).setResistance(10.0F), RefStrings.MODID + ":ore_nether_smoldering");
		ore_nether_uranium = com.hbm.blocks.PlainCubeTextures.bind(new BlockOutgas(Material.ROCK, true, 5, true).setTranslationKey("ore_nether_uranium").setCreativeTab(MainRegistry.blockTab).setHardness(0.4F).setResistance(10.0F), RefStrings.MODID + ":ore_nether_uranium");
		ore_nether_uranium_scorched = com.hbm.blocks.PlainCubeTextures.bind(new BlockOutgas(Material.ROCK, true, 5, true).setTranslationKey("ore_nether_uranium_scorched").setCreativeTab(MainRegistry.blockTab).setHardness(0.4F).setResistance(10.0F), RefStrings.MODID + ":ore_nether_uranium_scorched");
		ore_nether_plutonium = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("ore_nether_plutonium").setCreativeTab(MainRegistry.blockTab).setHardness(0.4F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":ore_nether_plutonium");
		ore_nether_tungsten = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("ore_nether_tungsten").setCreativeTab(MainRegistry.blockTab).setHardness(0.4F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":ore_nether_tungsten");
		ore_nether_sulfur = com.hbm.blocks.PlainCubeTextures.bind(new BlockStepEffect(Material.ROCK).setTranslationKey("ore_nether_sulfur").setCreativeTab(MainRegistry.blockTab).setHardness(0.4F).setResistance(10.0F), RefStrings.MODID + ":ore_nether_sulfur");
		ore_nether_fire = com.hbm.blocks.PlainCubeTextures.bind(new BlockStepEffect(Material.ROCK).setTranslationKey("ore_nether_fire").setCreativeTab(MainRegistry.blockTab).setHardness(0.4F).setResistance(10.0F), RefStrings.MODID + ":ore_nether_fire");
		ore_nether_cobalt = com.hbm.blocks.PlainCubeTextures.bind(new BlockStepEffect(Material.ROCK).setTranslationKey("ore_nether_cobalt").setCreativeTab(MainRegistry.blockTab).setHardness(0.4F).setResistance(10.0F), RefStrings.MODID + ":ore_nether_cobalt");
		ore_nether_schrabidium = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("ore_nether_schrabidium").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(600.0F)).setBlockTextureName(RefStrings.MODID + ":ore_nether_schrabidium");

		ore_meteor = new BlockMeteorOre().setBlockName("ore_meteor").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);

		stone_gneiss = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("stone_gneiss").setCreativeTab(MainRegistry.blockTab).setHardness(1.5F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":stone_gneiss_var");
		ore_gneiss_iron = com.hbm.blocks.PlainCubeTextures.bind(new BlockStepEffect(Material.ROCK).setTranslationKey("ore_gneiss_iron").setCreativeTab(MainRegistry.blockTab).setHardness(1.5F).setResistance(10.0F), RefStrings.MODID + ":ore_gneiss_iron");
		ore_gneiss_gold = com.hbm.blocks.PlainCubeTextures.bind(new BlockStepEffect(Material.ROCK).setTranslationKey("ore_gneiss_gold").setCreativeTab(MainRegistry.blockTab).setHardness(1.5F).setResistance(10.0F), RefStrings.MODID + ":ore_gneiss_gold");
		ore_gneiss_uranium = com.hbm.blocks.PlainCubeTextures.bind(new BlockOutgas(Material.ROCK, true, 5, true).setTranslationKey("ore_gneiss_uranium").setCreativeTab(MainRegistry.blockTab).setHardness(1.5F).setResistance(10.0F), RefStrings.MODID + ":ore_gneiss_uranium");
		ore_gneiss_uranium_scorched = com.hbm.blocks.PlainCubeTextures.bind(new BlockOutgas(Material.ROCK, true, 5, true).setTranslationKey("ore_gneiss_uranium_scorched").setCreativeTab(MainRegistry.blockTab).setHardness(1.5F).setResistance(10.0F), RefStrings.MODID + ":ore_gneiss_uranium_scorched");
		ore_gneiss_copper = com.hbm.blocks.PlainCubeTextures.bind(new BlockStepEffect(Material.ROCK).setTranslationKey("ore_gneiss_copper").setCreativeTab(MainRegistry.blockTab).setHardness(1.5F).setResistance(10.0F), RefStrings.MODID + ":ore_gneiss_copper");
		ore_gneiss_asbestos = com.hbm.blocks.PlainCubeTextures.bind(new BlockOutgas(Material.ROCK, true, 5, true).setTranslationKey("ore_gneiss_asbestos").setCreativeTab(MainRegistry.blockTab).setHardness(1.5F).setResistance(10.0F), RefStrings.MODID + ":ore_gneiss_asbestos");
		ore_gneiss_lithium = com.hbm.blocks.PlainCubeTextures.bind(new BlockStepEffect(Material.ROCK).setTranslationKey("ore_gneiss_lithium").setCreativeTab(MainRegistry.blockTab).setHardness(1.5F).setResistance(10.0F), RefStrings.MODID + ":ore_gneiss_lithium");
		ore_gneiss_schrabidium = com.hbm.blocks.PlainCubeTextures.bind(new BlockStepEffect(Material.ROCK).setTranslationKey("ore_gneiss_schrabidium").setCreativeTab(MainRegistry.blockTab).setHardness(1.5F).setResistance(10.0F), RefStrings.MODID + ":ore_gneiss_schrabidium");
		ore_gneiss_rare = com.hbm.blocks.PlainCubeTextures.bind(new BlockStepEffect(Material.ROCK).setTranslationKey("ore_gneiss_rare").setCreativeTab(MainRegistry.blockTab).setHardness(1.5F).setResistance(10.0F), RefStrings.MODID + ":ore_gneiss_rare");
		ore_gneiss_gas = com.hbm.blocks.PlainCubeTextures.bind(new BlockStepEffect(Material.ROCK).setTranslationKey("ore_gneiss_gas").setCreativeTab(MainRegistry.blockTab).setHardness(1.5F).setResistance(10.0F), RefStrings.MODID + ":ore_gneiss_gas");

		gneiss_brick = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("gneiss_brick").setCreativeTab(MainRegistry.blockTab).setHardness(1.5F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":gneiss_brick");
		gneiss_tile = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("gneiss_tile").setCreativeTab(MainRegistry.blockTab).setHardness(1.5F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":gneiss_tile");
		gneiss_chiseled = ((BlockPillar) new BlockPillar(Material.ROCK, RefStrings.MODID + ":gneiss_tile").setTranslationKey("gneiss_chiseled").setCreativeTab(MainRegistry.blockTab).setHardness(1.5F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":gneiss_chiseled");

		stone_depth = com.hbm.blocks.PlainCubeTextures.bind(new BlockDepth().setTranslationKey("stone_depth").setCreativeTab(MainRegistry.blockTab), RefStrings.MODID + ":stone_depth");
		ore_depth_cinnebar = com.hbm.blocks.PlainCubeTextures.bind(new BlockDepthOre().setTranslationKey("ore_depth_cinnebar").setCreativeTab(MainRegistry.blockTab), RefStrings.MODID + ":ore_depth_cinnebar");
		ore_depth_zirconium = com.hbm.blocks.PlainCubeTextures.bind(new BlockDepthOre().setTranslationKey("ore_depth_zirconium").setCreativeTab(MainRegistry.blockTab), RefStrings.MODID + ":ore_depth_zirconium");
		ore_depth_borax = com.hbm.blocks.PlainCubeTextures.bind(new BlockDepthOre().setTranslationKey("ore_depth_borax").setCreativeTab(MainRegistry.blockTab), RefStrings.MODID + ":ore_depth_borax");
		cluster_depth_iron = com.hbm.blocks.PlainCubeTextures.bind(new BlockDepthOre().setTranslationKey("cluster_depth_iron").setCreativeTab(MainRegistry.blockTab), RefStrings.MODID + ":cluster_depth_iron");
		cluster_depth_titanium = com.hbm.blocks.PlainCubeTextures.bind(new BlockDepthOre().setTranslationKey("cluster_depth_titanium").setCreativeTab(MainRegistry.blockTab), RefStrings.MODID + ":cluster_depth_titanium");
		cluster_depth_tungsten = com.hbm.blocks.PlainCubeTextures.bind(new BlockDepthOre().setTranslationKey("cluster_depth_tungsten").setCreativeTab(MainRegistry.blockTab), RefStrings.MODID + ":cluster_depth_tungsten");
		ore_alexandrite = com.hbm.blocks.PlainCubeTextures.bind(new BlockDepthOre().setTranslationKey("ore_alexandrite").setCreativeTab(MainRegistry.blockTab), RefStrings.MODID + ":ore_alexandrite");

		stone_keyhole = new BlockKeyhole().setTranslationKey("stone_keyhole").setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":stone_keyhole");
		stone_keyhole_meta = new BlockRedBrickKeyhole(Material.ROCK).setCreativeTab(null).setTranslationKey("stone_keyhole_meta").setResistance(10_000);

		ore_bedrock = new BlockBedrockOreTE().setTranslationKey("ore_bedrock").setCreativeTab(null);
		ore_volcano = new BlockFissure().setTranslationKey("ore_volcano").setLightLevel(1F).setCreativeTab(MainRegistry.blockTab);

		depth_brick = com.hbm.blocks.PlainCubeTextures.bind(new BlockDepth().setTranslationKey("depth_brick").setCreativeTab(MainRegistry.blockTab), RefStrings.MODID + ":depth_brick");
		depth_tiles = com.hbm.blocks.PlainCubeTextures.bind(new BlockDepth().setTranslationKey("depth_tiles").setCreativeTab(MainRegistry.blockTab), RefStrings.MODID + ":depth_tiles");
		depth_nether_brick = com.hbm.blocks.PlainCubeTextures.bind(new BlockDepth().setTranslationKey("depth_nether_brick").setCreativeTab(MainRegistry.blockTab), RefStrings.MODID + ":depth_nether_brick");
		depth_nether_tiles = com.hbm.blocks.PlainCubeTextures.bind(new BlockDepth().setTranslationKey("depth_nether_tiles").setCreativeTab(MainRegistry.blockTab), RefStrings.MODID + ":depth_nether_tiles");
		depth_dnt = com.hbm.blocks.PlainCubeTextures.bind(new BlockDepth().setTranslationKey("depth_dnt").setCreativeTab(MainRegistry.blockTab).setResistance(60000.0F), RefStrings.MODID + ":depth_dnt");

		stone_depth_nether = com.hbm.blocks.PlainCubeTextures.bind(new BlockDepth().setTranslationKey("stone_depth_nether").setCreativeTab(MainRegistry.blockTab), RefStrings.MODID + ":stone_depth_nether");
		ore_depth_nether_neodymium = com.hbm.blocks.PlainCubeTextures.bind(new BlockDepthOre().setTranslationKey("ore_depth_nether_neodymium").setCreativeTab(MainRegistry.blockTab), RefStrings.MODID + ":ore_depth_nether_neodymium");

		stone_porous = new BlockPorous().setTranslationKey("stone_porous").setCreativeTab(MainRegistry.blockTab).setBlockTextureName(RefStrings.MODID + ":stone_porous");
		stone_resource = new BlockResourceStone().setBlockName("stone_resource").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		stalagmite = new BlockStalagmite().setBlockName("stalagmite").setCreativeTab(MainRegistry.blockTab).setHardness(0.5F).setResistance(2.0F);
		stalactite = new BlockStalagmite().setBlockName("stalactite").setCreativeTab(MainRegistry.blockTab).setHardness(0.5F).setResistance(2.0F);
		stone_biome = new BlockBiomeStone().setBlockName("stone_biome").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);

		basalt = ((BlockPillar) new BlockPillar(Material.ROCK, RefStrings.MODID + ":basalt_top").setTranslationKey("basalt").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":basalt");
		ore_basalt = ((BlockBase) new BlockOreBasalt().setBlockName("ore_basalt").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":ore_basalt");
		basalt_smooth = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("basalt_smooth").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":basalt_smooth");
		basalt_brick = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("basalt_brick").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":basalt_brick");
		basalt_polished = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("basalt_polished").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":basalt_polished");
		basalt_tiles = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("basalt_tiles").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":basalt_tiles");

		ore_australium = new BlockOre(Material.ROCK).setTranslationKey("ore_australium").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_rare = new BlockOre(Material.ROCK).setTranslationKey("ore_rare").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_cobalt = new BlockOre(Material.ROCK).setTranslationKey("ore_cobalt").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_cinnebar = new BlockOre(Material.ROCK).setTranslationKey("ore_cinnebar").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_coltan = new BlockOre(Material.ROCK).setTranslationKey("ore_coltan").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(10.0F);
		ore_lanthanium = new BlockOre(Material.ROCK).setTranslationKey("ore_lanthanium").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(10.0F);

		ore_oil_empty = new BlockOre(Material.ROCK).setTranslationKey("ore_oil_empty").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_oil = new BlockOreFluid(Material.ROCK, ore_oil_empty, ReserveType.OIL).setTranslationKey("ore_oil").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_oil_sand = new BlockFalling(Material.SAND).setTranslationKey("ore_oil_sand").setCreativeTab(MainRegistry.blockTab).setStepSound(SoundType.SAND).setHardness(0.5F).setResistance(1.0F).setBlockTextureName(RefStrings.MODID + ":ore_oil_sand_alt");
		ore_bedrock_oil = new BlockOreFluid(Material.ROCK, null, ReserveType.OIL).setTranslationKey("ore_bedrock_oil").setCreativeTab(MainRegistry.blockTab).setBlockUnbreakable().setResistance(1_000_000);
		ore_gas_empty = new BlockOre(Material.ROCK).setTranslationKey("ore_gas_empty").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_gas = new BlockOreFluid(Material.ROCK, ore_gas_empty, ReserveType.GAS).setTranslationKey("ore_gas").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);

		ore_brine_empty = new BlockOre(Material.ROCK).setTranslationKey("ore_brine_empty").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_brine = new BlockOreFluid(Material.ROCK, ore_brine_empty, ReserveType.BRINE).setTranslationKey("ore_brine").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);

		ore_tekto_empty = new BlockOre(Material.ROCK).setTranslationKey("ore_tekto_empty").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		ore_tekto = new BlockOreFluid(Material.ROCK, ore_tekto_empty, ReserveType.OIL).setTranslationKey("ore_tekto").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);

		ore_tikite = ((BlockBase) new BlockDragonProof(Material.ROCK).setBlockName("ore_tikite").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":ore_tikite_alt");

		// the block ID limit is weeping
		block_uranium = com.hbm.blocks.PlainCubeTextures.bind(((BlockHazard) new BlockHazard().makeBeaconable().setTranslationKey("block_uranium").setCreativeTab(MainRegistry.blockTab)).withSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(50.0F), RefStrings.MODID + ":block_uranium");
		block_u233 = com.hbm.blocks.PlainCubeTextures.bind(((BlockHazard) new BlockHazard().makeBeaconable().setDisplayEffect(ExtDisplayEffect.RADFOG).setTranslationKey("block_u233").setCreativeTab(MainRegistry.blockTab)).withSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(50.0F), RefStrings.MODID + ":block_u233");
		block_u235 = com.hbm.blocks.PlainCubeTextures.bind(((BlockHazard) new BlockHazard().makeBeaconable().setDisplayEffect(ExtDisplayEffect.RADFOG).setTranslationKey("block_u235").setCreativeTab(MainRegistry.blockTab)).withSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(50.0F), RefStrings.MODID + ":block_u235");
		block_u238 = com.hbm.blocks.PlainCubeTextures.bind(((BlockHazard) new BlockHazard().makeBeaconable().setTranslationKey("block_u238").setCreativeTab(MainRegistry.blockTab)).withSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(50.0F), RefStrings.MODID + ":block_u238");
		block_uranium_fuel = com.hbm.blocks.PlainCubeTextures.bind(((BlockHazard) new BlockHazard().makeBeaconable().setTranslationKey("block_uranium_fuel").setCreativeTab(MainRegistry.blockTab)).withSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(50.0F), RefStrings.MODID + ":block_uranium_fuel");
		block_thorium = com.hbm.blocks.PlainCubeTextures.bind(((BlockHazard) new BlockHazard().makeBeaconable().setTranslationKey("block_thorium").setCreativeTab(MainRegistry.blockTab)).withSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(50.0F), RefStrings.MODID + ":block_thorium");
		block_thorium_fuel = com.hbm.blocks.PlainCubeTextures.bind(((BlockHazard) new BlockHazard().makeBeaconable().setTranslationKey("block_thorium_fuel").setCreativeTab(MainRegistry.blockTab)).withSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(50.0F), RefStrings.MODID + ":block_thorium_fuel");
		block_neptunium = com.hbm.blocks.PlainCubeTextures.bind(((BlockHazard) new BlockHazard().makeBeaconable().setDisplayEffect(ExtDisplayEffect.RADFOG).setTranslationKey("block_neptunium").setCreativeTab(MainRegistry.blockTab)).withSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(60.0F), RefStrings.MODID + ":block_neptunium");
		block_polonium = com.hbm.blocks.PlainCubeTextures.bind(((BlockHazard) new BlockHotHazard().makeBeaconable().setDisplayEffect(ExtDisplayEffect.RADFOG).setTranslationKey("block_polonium").setCreativeTab(MainRegistry.blockTab)).withSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(50.0F), RefStrings.MODID + ":block_polonium");
		block_mox_fuel = com.hbm.blocks.PlainCubeTextures.bind(((BlockHazard) new BlockHazard().makeBeaconable().setDisplayEffect(ExtDisplayEffect.RADFOG).setTranslationKey("block_mox_fuel").setCreativeTab(MainRegistry.blockTab)).withSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(50.0F), RefStrings.MODID + ":block_mox_fuel");
		block_plutonium = com.hbm.blocks.PlainCubeTextures.bind(((BlockHazard) new BlockHazard().makeBeaconable().setDisplayEffect(ExtDisplayEffect.RADFOG).setTranslationKey("block_plutonium").setCreativeTab(MainRegistry.blockTab)).withSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(50.0F), RefStrings.MODID + ":block_plutonium");
		block_pu238 = com.hbm.blocks.PlainCubeTextures.bind(((BlockHazard) new BlockHotHazard().makeBeaconable().setDisplayEffect(ExtDisplayEffect.RADFOG).setTranslationKey("block_pu238").setCreativeTab(MainRegistry.blockTab).setLightLevel(5F/15F)).withSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(50.0F), RefStrings.MODID + ":block_pu238");
		block_pu239 = com.hbm.blocks.PlainCubeTextures.bind(((BlockHazard) new BlockHazard().makeBeaconable().setDisplayEffect(ExtDisplayEffect.RADFOG).setTranslationKey("block_pu239").setCreativeTab(MainRegistry.blockTab)).withSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(50.0F), RefStrings.MODID + ":block_pu239");
		block_pu240 = com.hbm.blocks.PlainCubeTextures.bind(((BlockHazard) new BlockHazard().makeBeaconable().setDisplayEffect(ExtDisplayEffect.RADFOG).setTranslationKey("block_pu240").setCreativeTab(MainRegistry.blockTab)).withSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(50.0F), RefStrings.MODID + ":block_pu240");
		block_pu_mix = com.hbm.blocks.PlainCubeTextures.bind(((BlockHazard) new BlockHazard().makeBeaconable().setDisplayEffect(ExtDisplayEffect.RADFOG).setTranslationKey("block_pu_mix").setCreativeTab(MainRegistry.blockTab)).withSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(50.0F), RefStrings.MODID + ":block_pu_mix");
		block_plutonium_fuel = com.hbm.blocks.PlainCubeTextures.bind(((BlockHazard) new BlockHazard().makeBeaconable().setDisplayEffect(ExtDisplayEffect.RADFOG).setTranslationKey("block_plutonium_fuel").setCreativeTab(MainRegistry.blockTab)).withSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(50.0F), RefStrings.MODID + ":block_plutonium_fuel");
		block_titanium = ((BlockBase) ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_titanium").setCreativeTab(MainRegistry.blockTab)).setSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(50.0F)).setBlockTextureName(RefStrings.MODID + ":block_titanium");
		block_sulfur = ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_sulfur").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":block_sulfur");
		block_niter = ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_niter").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":block_niter");
		block_copper = ((BlockBase) ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_copper").setCreativeTab(MainRegistry.blockTab)).setSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(20.0F)).setBlockTextureName(RefStrings.MODID + ":block_copper");
		block_nickel = ((BlockBase) ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_nickel").setCreativeTab(MainRegistry.blockTab)).setSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":block_nickel");
		block_red_copper = ((BlockBase) ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_red_copper").setCreativeTab(MainRegistry.blockTab)).setSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(25.0F)).setBlockTextureName(RefStrings.MODID + ":block_red_copper");
		block_osmiridium = ((BlockBase) ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_osmiridium").setCreativeTab(MainRegistry.blockTab)).setSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":block_osmiridium");
		block_tungsten = ((BlockBase) ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_tungsten").setCreativeTab(MainRegistry.blockTab)).setSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(20.0F)).setBlockTextureName(RefStrings.MODID + ":block_tungsten");
		block_aluminium = ((BlockBase) ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_aluminium").setCreativeTab(MainRegistry.blockTab)).setSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(20.0F)).setBlockTextureName(RefStrings.MODID + ":block_aluminium");
		block_fluorite = ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_fluorite").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":block_fluorite");
		block_steel = ((BlockBase) ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_steel").setCreativeTab(MainRegistry.blockTab)).setSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(50.0F)).setBlockTextureName(RefStrings.MODID + ":block_steel");
		block_tcalloy = ((BlockBase) ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_tcalloy").setCreativeTab(MainRegistry.blockTab)).setSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(70.0F)).setBlockTextureName(RefStrings.MODID + ":block_tcalloy");
		block_cdalloy = ((BlockBase) ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_cdalloy").setCreativeTab(MainRegistry.blockTab)).setSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(70.0F)).setBlockTextureName(RefStrings.MODID + ":block_cdalloy");
		block_lead = ((BlockBase) ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_lead").setCreativeTab(MainRegistry.blockTab)).setSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(50.0F)).setBlockTextureName(RefStrings.MODID + ":block_lead");
		block_bismuth = ((BlockBase) ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_bismuth").setCreativeTab(MainRegistry.blockTab)).setSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(90.0F)).setBlockTextureName(RefStrings.MODID + ":block_bismuth");
		block_cadmium = ((BlockBase) ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_cadmium").setCreativeTab(MainRegistry.blockTab)).setSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(90.0F)).setBlockTextureName(RefStrings.MODID + ":block_cadmium");
		block_coltan = ((BlockBase) ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_coltan").setCreativeTab(MainRegistry.blockTab)).setSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(50.0F)).setBlockTextureName(RefStrings.MODID + ":block_coltan");
		block_tantalium = ((BlockBase) ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_tantalium").setCreativeTab(MainRegistry.blockTab)).setSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(50.0F)).setBlockTextureName(RefStrings.MODID + ":block_tantalium");
		block_niobium = ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_niobium").setCreativeTab(MainRegistry.blockTab)).setSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(50.0F);
		block_trinitite = com.hbm.blocks.PlainCubeTextures.bind(new BlockHazard().makeBeaconable().setTranslationKey("block_trinitite").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F), RefStrings.MODID + ":block_trinitite");
		block_waste = com.hbm.blocks.PlainCubeTextures.bind(new BlockNuclearWaste().makeBeaconable().setDisplayEffect(ExtDisplayEffect.RADFOG).setTranslationKey("block_waste").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F), RefStrings.MODID + ":block_waste");
		block_waste_painted = com.hbm.blocks.PlainCubeTextures.bind(new BlockNuclearWaste().makeBeaconable().setDisplayEffect(ExtDisplayEffect.RADFOG).setTranslationKey("block_waste_painted").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F), RefStrings.MODID + ":block_waste_painted");
		block_waste_vitrified = com.hbm.blocks.PlainCubeTextures.bind(new BlockNuclearWaste().makeBeaconable().setDisplayEffect(ExtDisplayEffect.RADFOG).setTranslationKey("block_waste_vitrified").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F), RefStrings.MODID + ":block_waste_vitrified");
		ancient_scrap = com.hbm.blocks.PlainCubeTextures.bind(new BlockOutgas(Material.IRON, true, 1, true, true).setTranslationKey("ancient_scrap").setCreativeTab(MainRegistry.blockTab).setHardness(100.0F).setResistance(6000.0F), RefStrings.MODID + ":ancient_scrap");
		block_corium = com.hbm.blocks.PlainCubeTextures.bind(new BlockHazard(Material.IRON).setTranslationKey("block_corium").setCreativeTab(MainRegistry.blockTab).setHardness(100.0F).setResistance(6000.0F), RefStrings.MODID + ":block_corium");
		block_corium_cobble = com.hbm.blocks.PlainCubeTextures.bind(new BlockOutgas(Material.IRON, true, 1, true, true).setTranslationKey("block_corium_cobble").setCreativeTab(MainRegistry.blockTab).setHardness(100.0F).setResistance(6000.0F), RefStrings.MODID + ":block_corium_cobble");
		block_scrap = new BlockFalling(Material.SAND).setTranslationKey("block_scrap").setCreativeTab(MainRegistry.blockTab).setHardness(2.5F).setResistance(5.0F).setStepSound(SoundType.GROUND).setBlockTextureName(RefStrings.MODID + ":block_scrap");
		block_electrical_scrap = new BlockFalling(Material.IRON).setTranslationKey("block_electrical_scrap").setCreativeTab(MainRegistry.blockTab).setHardness(2.5F).setResistance(5.0F).setStepSound(ModSoundTypes.legacyMetal).setBlockTextureName(RefStrings.MODID + ":electrical_scrap");
		block_beryllium = ((BlockBase) ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_beryllium").setCreativeTab(MainRegistry.blockTab)).setSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(20.0F)).setBlockTextureName(RefStrings.MODID + ":block_beryllium");
		block_schraranium = com.hbm.blocks.PlainCubeTextures.bind(((BlockHazard) new BlockHazard().makeBeaconable().setDisplayEffect(ExtDisplayEffect.SCHRAB).setTranslationKey("block_schraranium").setCreativeTab(MainRegistry.blockTab)).withSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(250.0F), RefStrings.MODID + ":block_schraranium");
		block_schrabidium = com.hbm.blocks.PlainCubeTextures.bind(((BlockHazard) new BlockHazard().makeBeaconable().setDisplayEffect(ExtDisplayEffect.SCHRAB).setTranslationKey("block_schrabidium").setCreativeTab(MainRegistry.blockTab)).withSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(600.0F), RefStrings.MODID + ":block_schrabidium");
		block_schrabidate = com.hbm.blocks.PlainCubeTextures.bind(((BlockHazard) new BlockHazard().makeBeaconable().setDisplayEffect(ExtDisplayEffect.SCHRAB).setTranslationKey("block_schrabidate").setCreativeTab(MainRegistry.blockTab)).withSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(600.0F), RefStrings.MODID + ":block_schrabidate");
		block_solinium = com.hbm.blocks.PlainCubeTextures.bind(((BlockHazard) new BlockHazard().makeBeaconable().setDisplayEffect(ExtDisplayEffect.SCHRAB).setTranslationKey("block_solinium").setCreativeTab(MainRegistry.blockTab)).withSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(600.0F), RefStrings.MODID + ":block_solinium");
		block_schrabidium_fuel = com.hbm.blocks.PlainCubeTextures.bind(((BlockHazard) new BlockHazard().makeBeaconable().setDisplayEffect(ExtDisplayEffect.SCHRAB).setTranslationKey("block_schrabidium_fuel").setCreativeTab(MainRegistry.blockTab)).withSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(600.0F), RefStrings.MODID + ":block_schrabidium_fuel");
		block_euphemium = ((BlockBase) ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_euphemium").setCreativeTab(MainRegistry.blockTab)).setSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(60000.0F)).setBlockTextureName(RefStrings.MODID + ":block_euphemium");
		block_dineutronium = ((BlockBase) ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_dineutronium").setCreativeTab(MainRegistry.blockTab)).setSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(60000.0F)).setBlockTextureName(RefStrings.MODID + ":block_dineutronium");
		block_schrabidium_cluster = new BlockRotatablePillar(Material.ROCK, RefStrings.MODID + ":block_schrabidium_cluster_top").setTranslationKey("block_schrabidium_cluster").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(60000.0F).setBlockTextureName(RefStrings.MODID + ":block_schrabidium_cluster_side");
		block_euphemium_cluster = new BlockRotatablePillar(Material.ROCK, RefStrings.MODID + ":block_euphemium_cluster_top").setTranslationKey("block_euphemium_cluster").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(60000.0F).setBlockTextureName(RefStrings.MODID + ":block_euphemium_cluster_side");
		block_advanced_alloy = ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_advanced_alloy").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(60.0F)).setBlockTextureName(RefStrings.MODID + ":block_advanced_alloy");
		block_magnetized_tungsten = ((BlockBase) ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_magnetized_tungsten").setCreativeTab(MainRegistry.blockTab)).setSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(75.0F)).setBlockTextureName(RefStrings.MODID + ":block_magnetized_tungsten");
		block_combine_steel = ((BlockBase) ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_combine_steel").setCreativeTab(MainRegistry.blockTab)).setSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(600.0F)).setBlockTextureName(RefStrings.MODID + ":block_combine_steel");
		block_desh = ((BlockBase) ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_desh").setCreativeTab(MainRegistry.blockTab)).setSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(300.0F)).setBlockTextureName(RefStrings.MODID + ":block_desh");
		block_dura_steel = ((BlockBase) ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_dura_steel").setCreativeTab(MainRegistry.blockTab)).setSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(200.0F)).setBlockTextureName(RefStrings.MODID + ":block_dura_steel");
		block_starmetal = ((BlockBase) ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_starmetal").setCreativeTab(MainRegistry.blockTab)).setSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(400.0F)).setBlockTextureName(RefStrings.MODID + ":block_starmetal");
		block_polymer = ((BlockBase) ((BlockBase) new BlockBeaconable(Material.ROCK).setBlockName("block_polymer").setCreativeTab(MainRegistry.blockTab)).setSoundType(SoundType.STONE).setHardness(3.0F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":block_polymer");
		block_bakelite = ((BlockBase) ((BlockBase) new BlockBeaconable(Material.ROCK).setBlockName("block_bakelite").setCreativeTab(MainRegistry.blockTab)).setSoundType(SoundType.STONE).setHardness(3.0F).setResistance(5.0F)).setBlockTextureName(RefStrings.MODID + ":block_bakelite");
		block_rubber = ((BlockBase) ((BlockBase) new BlockBeaconable(Material.ROCK).setBlockName("block_rubber").setCreativeTab(MainRegistry.blockTab)).setSoundType(SoundType.STONE).setHardness(3.0F).setResistance(15.0F)).setBlockTextureName(RefStrings.MODID + ":block_rubber");
		block_yellowcake = com.hbm.blocks.PlainCubeTextures.bind(((BlockHazardFalling) new BlockHazardFalling().makeBeaconable().setTranslationKey("block_yellowcake").setCreativeTab(MainRegistry.blockTab)).withSoundType(SoundType.SAND).setHardness(5.0F).setResistance(10.0F), RefStrings.MODID + ":block_yellowcake");
		block_insulator = ((BlockRotatablePillar) new BlockRotatablePillar(Material.CLOTH, RefStrings.MODID + ":block_insulator_top").setTranslationKey("block_insulator").setCreativeTab(MainRegistry.blockTab)).withSoundType(SoundType.CLOTH).setHardness(5.0F).setResistance(10.0F).setBlockTextureName(RefStrings.MODID + ":block_insulator_side");
		block_fiberglass = ((BlockRotatablePillar) new BlockRotatablePillar(Material.CLOTH, RefStrings.MODID + ":block_fiberglass_top").setTranslationKey("block_fiberglass").setCreativeTab(MainRegistry.blockTab)).withSoundType(SoundType.CLOTH).setHardness(5.0F).setResistance(15.0F).setBlockTextureName(RefStrings.MODID + ":block_fiberglass_side");
		block_asbestos = com.hbm.blocks.PlainCubeTextures.bind(((BlockOutgas) new BlockOutgas(Material.CLOTH, true, 5, true).setTranslationKey("block_asbestos").setCreativeTab(MainRegistry.blockTab)).withSoundType(SoundType.CLOTH).setHardness(5.0F).setResistance(15.0F), RefStrings.MODID + ":block_asbestos");
		block_cobalt = ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_cobalt").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(50.0F)).setBlockTextureName(RefStrings.MODID + ":block_cobalt");
		block_lithium = ((BlockBase) ((BlockBase) new BlockLithium(Material.IRON).setBlockName("block_lithium")).setSoundType(ModSoundTypes.legacyMetal).setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":block_lithium");
		block_zirconium = ((BlockBase) ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_zirconium")).setSoundType(ModSoundTypes.legacyMetal).setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(30.0F)).setBlockTextureName(RefStrings.MODID + ":block_zirconium");
		block_white_phosphorus = com.hbm.blocks.PlainCubeTextures.bind(new BlockHazard(Material.ROCK).makeBeaconable().setTranslationKey("block_white_phosphorus").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F), RefStrings.MODID + ":block_white_phosphorus");
		block_red_phosphorus = com.hbm.blocks.PlainCubeTextures.bind(((BlockHazardFalling) new BlockHazardFalling().makeBeaconable()).withSoundType(SoundType.SAND).setTranslationKey("block_red_phosphorus").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F), RefStrings.MODID + ":block_red_phosphorus");
		block_fallout = com.hbm.blocks.PlainCubeTextures.bind(((BlockHazardFalling) new BlockHazardFalling()).withSoundType(SoundType.GROUND).setTranslationKey("block_fallout").setCreativeTab(MainRegistry.blockTab).setHardness(0.2F), RefStrings.MODID + ":ash");
		block_foam = ((BlockBase) ((BlockBase) new BlockGeneric(Material.CRAFTED_SNOW).setBlockName("block_foam").setCreativeTab(MainRegistry.blockTab)).setSoundType(SoundType.SNOW).setHardness(0.5F).setResistance(0.0F)).setBlockTextureName(RefStrings.MODID + ":foam");
		block_coke = ((BlockBase) new BlockCoke().setBlockName("block_coke").setCreativeTab(MainRegistry.blockTab)).setSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(10.0F);
		block_graphite = ((BlockBase) new BlockGraphite(Material.IRON, 30, 5).setBlockName("block_graphite").setCreativeTab(MainRegistry.blockTab)).setSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(10.0F);
		block_graphite_drilled = new BlockGraphiteDrilled().setBlockName("block_graphite_drilled");
		block_graphite_fuel = new BlockGraphiteFuel().setBlockName("block_graphite_fuel");
		block_graphite_plutonium = new BlockGraphiteSource().setBlockName("block_graphite_plutonium");
		block_graphite_rod = ((BlockBase) new BlockGraphiteRod().setBlockName("block_graphite_rod")).setBlockTextureName(RefStrings.MODID + ":block_graphite_rod_in");
		block_graphite_source = new BlockGraphiteSource().setBlockName("block_graphite_source");
		block_graphite_lithium = new BlockGraphiteBreedingFuel().setBlockName("block_graphite_lithium");
		block_graphite_tritium = new BlockGraphiteBreedingProduct().setBlockName("block_graphite_tritium");
		block_graphite_detector = new BlockGraphiteNeutronDetector().setBlockName("block_graphite_detector");
		block_boron = ((BlockBase) ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_boron").setCreativeTab(MainRegistry.blockTab)).setSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":block_boron");
		block_lanthanium = ((BlockBase) ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_lanthanium").setCreativeTab(MainRegistry.blockTab)).setSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":block_lanthanium");
		block_ra226 = com.hbm.blocks.PlainCubeTextures.bind(((BlockHazard) new BlockHazard().makeBeaconable().setTranslationKey("block_ra226").setCreativeTab(MainRegistry.blockTab)).withSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(10.0F), RefStrings.MODID + ":block_ra226");
		block_actinium = com.hbm.blocks.PlainCubeTextures.bind(((BlockHazard) new BlockHazard().makeBeaconable().setTranslationKey("block_actinium").setCreativeTab(MainRegistry.blockTab)).withSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(10.0F), RefStrings.MODID + ":block_actinium");
		block_tritium = ((BlockRotatablePillar) new BlockRotatablePillar(Material.GLASS, RefStrings.MODID + ":block_tritium_top").setTranslationKey("block_tritium").setCreativeTab(MainRegistry.blockTab)).withSoundType(SoundType.GLASS).setHardness(3.0F).setResistance(2.0F).setBlockTextureName(RefStrings.MODID + ":block_tritium_side");
		block_semtex = ((BlockBase) ((BlockBase) new BlockPlasticExplosive(Material.TNT).setBlockName("block_semtex").setCreativeTab(MainRegistry.blockTab)).setSoundType(ModSoundTypes.legacyMetal).setHardness(2.0F).setResistance(2.0F)).setBlockTextureName(RefStrings.MODID + ":block_semtex");
		block_c4 = ((BlockBase) ((BlockBase) new BlockPlasticExplosive(Material.TNT).setBlockName("block_c4").setCreativeTab(MainRegistry.blockTab)).setSoundType(ModSoundTypes.legacyMetal).setHardness(2.0F).setResistance(2.0F)).setBlockTextureName(RefStrings.MODID + ":block_c4");
		block_smore = ((BlockPillar) new BlockPillar(Material.ROCK, RefStrings.MODID + ":block_smore_top").setTranslationKey("block_smore").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(600.0F)).setBlockTextureName(RefStrings.MODID + ":block_smore_side");
		block_slag = ((BlockBase) ((BlockBase) new BlockSlag(Material.ROCK).setBlockName("block_slag").setCreativeTab(MainRegistry.blockTab)).setSoundType(SoundType.STONE).setHardness(2.0F)).setBlockTextureName(RefStrings.MODID + ":block_slag");

		block_australium = ((BlockBase) new BlockBeaconable(Material.IRON).setBlockName("block_australium").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":block_australium");

		block_cap = new BlockCap().setBlockName("block_cap").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);

		deco_titanium = new BlockOre(Material.IRON).noFortune().setTranslationKey("deco_titanium").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		deco_red_copper = new BlockDecoCT(Material.IRON).noFortune().setTranslationKey("deco_red_copper").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		deco_tungsten = new BlockDecoCT(Material.IRON).noFortune().setTranslationKey("deco_tungsten").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		deco_aluminium = new BlockDecoCT(Material.IRON).noFortune().setTranslationKey("deco_aluminium").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		deco_steel = new BlockDecoCT(Material.IRON).noFortune().setTranslationKey("deco_steel").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		deco_rusty_steel = new BlockDecoCT(Material.IRON).noFortune().setTranslationKey("deco_rusty_steel").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		deco_lead = new BlockDecoCT(Material.IRON).noFortune().setTranslationKey("deco_lead").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		deco_beryllium = new BlockDecoCT(Material.IRON).noFortune().setTranslationKey("deco_beryllium").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		deco_asbestos = com.hbm.blocks.PlainCubeTextures.bind(new BlockOutgas(Material.CLOTH, true, 5, true).setTranslationKey("deco_asbestos").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F), RefStrings.MODID + ":deco_asbestos");
		deco_rbmk = ((BlockBase) new BlockGeneric(Material.IRON).setBlockName("deco_rbmk").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(100.0F)).setBlockTextureName(RefStrings.MODID + ":rbmk/rbmk_top");
		deco_rbmk_smooth = ((BlockBase) new BlockGeneric(Material.IRON).setBlockName("deco_rbmk_smooth").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(100.0F)).setBlockTextureName(RefStrings.MODID + ":rbmk/rbmk_blank_top");
		deco_stainless = new BlockDecoCT(Material.IRON).setTranslationKey("deco_stainless").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);

		deco_emitter = new BlockEmitter().setTranslationKey("deco_emitter").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(20.0F).setBlockTextureName(RefStrings.MODID + ":emitter");
		part_emitter = new PartEmitter().setTranslationKey("part_emitter").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(20.0F).setBlockTextureName(RefStrings.MODID + ":part_top");
		deco_loot = new BlockLoot().setTranslationKey("deco_loot").setCreativeTab(null).setHardness(0.0F).setResistance(0.0F).setBlockTextureName(RefStrings.MODID + ":block_steel");
		pedestal = new BlockPedestal().setTranslationKey("pedestal").setCreativeTab(null).setHardness(2.0F).setResistance(10.0F);
		skeleton_holder = new BlockSkeletonHolder().setTranslationKey("skeleton_holder").setCreativeTab(null).setHardness(2.0F).setResistance(10.0F).setBlockTextureName("soul_sand");
		bobblehead = new BlockBobble().setTranslationKey("bobblehead").setCreativeTab(MainRegistry.blockTab).setHardness(0.0F).setResistance(0.0F).setBlockTextureName(RefStrings.MODID + ":block_steel");
		snowglobe = new BlockSnowglobe().setTranslationKey("snowglobe").setCreativeTab(MainRegistry.blockTab).setHardness(0.0F).setResistance(0.0F).setBlockTextureName(RefStrings.MODID + ":glass_boron");
		plushie = new BlockPlushie().setTranslationKey("plushie").setStepSound(SoundType.CLOTH).setResistance(50_0000.0F).setCreativeTab(MainRegistry.blockTab).setHardness(0.0F).setResistance(0.0F).setBlockTextureName(RefStrings.MODID + ":block_fiberglass_side");
		dungeon_spawner = new DungeonSpawner().setTranslationKey("dungeon_spawner").setResistance(50_0000.0F).setBlockUnbreakable().setBlockTextureName(RefStrings.MODID + ":dungeon_spawner");

		gravel_obsidian = new BlockFalling(Material.IRON).setTranslationKey("gravel_obsidian").setCreativeTab(MainRegistry.blockTab).setStepSound(SoundType.GROUND).setHardness(5.0F).setResistance(240.0F).setBlockTextureName(RefStrings.MODID + ":gravel_obsidian");
		gravel_diamond = new BlockFalling(Material.SAND).setTranslationKey("gravel_diamond").setCreativeTab(MainRegistry.blockTab).setStepSound(SoundType.GROUND).setHardness(0.6F).setBlockTextureName(RefStrings.MODID + ":gravel_diamond");
		asphalt = com.hbm.blocks.PlainCubeTextures.bind(new BlockSpeedy(Material.ROCK, 1.5).setTranslationKey("asphalt").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(120.0F), RefStrings.MODID + ":asphalt");
		asphalt_light = com.hbm.blocks.PlainCubeTextures.bind(new BlockSpeedy(Material.ROCK, 1.5).setTranslationKey("asphalt_light").setCreativeTab(MainRegistry.blockTab).setLightLevel(1F).setHardness(15.0F).setResistance(120.0F), RefStrings.MODID + ":asphalt_light");

		sandbags = new BlockSandbags(Material.GROUND).setTranslationKey("sandbags").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(30.0F);
		wood_barrier = new BlockBarrier(Material.WOOD).setSoundType(net.minecraft.block.SoundType.WOOD).setTranslationKey("wood_barrier").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(15.0F);
		wood_structure = ((BlockBase) new BlockWoodStructure(Material.WOOD)).setSoundType(net.minecraft.block.SoundType.WOOD).setTranslationKey("wood_structure").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(15.0F);

		reinforced_brick = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("reinforced_brick").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(300.0F)).setBlockTextureName(RefStrings.MODID + ":reinforced_brick");
		reinforced_glass = new BlockNTMGlassCT(0, RefStrings.MODID + ":reinforced_glass", Material.ROCK).setTranslationKey("reinforced_glass").setCreativeTab(MainRegistry.blockTab).setLightOpacity(0).setHardness(2.0F).setResistance(25.0F);
		reinforced_glass_pane = new BlockNTMGlassPane(0, RefStrings.MODID + ":reinforced_glass_pane", RefStrings.MODID + ":reinforced_glass_pane_edge", Material.ROCK, false).setTranslationKey("reinforced_glass_pane").setCreativeTab(MainRegistry.blockTab).setLightOpacity(1).setHardness(2.0F).setResistance(25.0F);
		reinforced_light = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("reinforced_light").setCreativeTab(MainRegistry.blockTab).setLightLevel(1.0F).setHardness(15.0F).setResistance(80.0F)).setBlockTextureName(RefStrings.MODID + ":reinforced_light");
		reinforced_sand = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("reinforced_sand").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(40.0F)).setBlockTextureName(RefStrings.MODID + ":reinforced_sand");
		reinforced_lamp_off = new ReinforcedLamp(Material.ROCK, false).setTranslationKey("reinforced_lamp_off").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(80.0F).setBlockTextureName(RefStrings.MODID + ":reinforced_lamp_off");
		reinforced_lamp_on = new ReinforcedLamp(Material.ROCK, true).setTranslationKey("reinforced_lamp_on").setHardness(15.0F).setResistance(80.0F).setBlockTextureName(RefStrings.MODID + ":reinforced_lamp_on");
		reinforced_laminate = new BlockNTMGlassCT(1, RefStrings.MODID + ":reinforced_laminate", Material.ROCK, true).setTranslationKey("reinforced_laminate").setCreativeTab(MainRegistry.blockTab).setLightOpacity(0).setHardness(15.0F).setResistance(300.0F);
		reinforced_laminate_pane = new BlockNTMGlassPane(1, RefStrings.MODID + ":reinforced_laminate_pane", RefStrings.MODID + ":reinforced_laminate_pane_edge", Material.ROCK, true).setTranslationKey("reinforced_laminate_pane").setCreativeTab(MainRegistry.blockTab).setLightOpacity(1).setHardness(15.0F).setResistance(300.0F);

		lamp_tritium_green_off = new TritiumLamp(Material.REDSTONE_LIGHT, false).setTranslationKey("lamp_tritium_green_off").setStepSound(SoundType.GLASS).setCreativeTab(MainRegistry.blockTab).setHardness(3.0F).setBlockTextureName(RefStrings.MODID + ":lamp_tritium_green_off");
		lamp_tritium_green_on = new TritiumLamp(Material.REDSTONE_LIGHT, true).setTranslationKey("lamp_tritium_green_on").setStepSound(SoundType.GLASS).setHardness(3.0F).setBlockTextureName(RefStrings.MODID + ":lamp_tritium_green_on");
		lamp_tritium_blue_off = new TritiumLamp(Material.REDSTONE_LIGHT, false).setTranslationKey("lamp_tritium_blue_off").setStepSound(SoundType.GLASS).setCreativeTab(MainRegistry.blockTab).setHardness(3.0F).setBlockTextureName(RefStrings.MODID + ":lamp_tritium_blue_off");
		lamp_tritium_blue_on = new TritiumLamp(Material.REDSTONE_LIGHT, true).setTranslationKey("lamp_tritium_blue_on").setStepSound(SoundType.GLASS).setHardness(3.0F).setBlockTextureName(RefStrings.MODID + ":lamp_tritium_blue_on");

		lamp_demon = new DemonLamp().setTranslationKey("lamp_demon").setStepSound(ModSoundTypes.legacyMetal).setCreativeTab(MainRegistry.blockTab).setLightLevel(1F).setHardness(3.0F).setBlockTextureName(RefStrings.MODID + ":lamp_demon");
		lantern = new BlockLantern().setTranslationKey("lantern").setStepSound(ModSoundTypes.legacyMetal).setCreativeTab(MainRegistry.blockTab).setLightLevel(1F).setHardness(3.0F).setBlockTextureName(RefStrings.MODID + ":block_steel");
		lantern_behemoth = new BlockLanternBehemoth().setTranslationKey("lantern_behemoth").setStepSound(ModSoundTypes.legacyMetal).setCreativeTab(null).setHardness(3.0F).setBlockTextureName(RefStrings.MODID + ":block_rust");

		spotlight_incandescent = new Spotlight(Material.IRON, 2, LightType.INCANDESCENT, true).setTranslationKey("spotlight_incandescent").setCreativeTab(MainRegistry.blockTab);
		spotlight_incandescent_off = new Spotlight(Material.IRON, 2, LightType.INCANDESCENT, false).setTranslationKey("spotlight_incandescent_off");
		spotlight_fluoro = new SpotlightModular(Material.IRON, 8, LightType.FLUORESCENT, true).setTranslationKey("spotlight_fluoro").setCreativeTab(MainRegistry.blockTab);
		spotlight_fluoro_off = new SpotlightModular(Material.IRON, 8, LightType.FLUORESCENT, false).setTranslationKey("spotlight_fluoro_off");
		spotlight_halogen = new Spotlight(Material.IRON, 32, LightType.HALOGEN, true).setTranslationKey("spotlight_halogen").setCreativeTab(MainRegistry.blockTab);
		spotlight_halogen_off = new Spotlight(Material.IRON, 32, LightType.HALOGEN, false).setTranslationKey("spotlight_halogen_off");
		spotlight_beam = new SpotlightBeam().setTranslationKey("spotlight_beam");
		floodlight = new Floodlight(Material.IRON).setTranslationKey("floodlight").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.blockTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		floodlight_beam = new FloodlightBeam().setTranslationKey("floodlight_beam");

		rebar = new BlockRebar().setTranslationKey("rebar").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(20.0F);
		reinforced_stone = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("reinforced_stone").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(100.0F)).setBlockTextureName(RefStrings.MODID + ":reinforced_stone");
		concrete_smooth = com.hbm.blocks.PlainCubeTextures.bind(new BlockRadResistant(Material.ROCK).setTranslationKey("concrete_smooth").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(140.0F), RefStrings.MODID + ":concrete");
		concrete_colored = new BlockConcreteColored(Material.ROCK).setTranslationKey("concrete_colored").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(140.0F).setBlockTextureName(RefStrings.MODID + ":concrete");
		concrete_colored_ext = ((BlockBase) new BlockConcreteColoredExt(Material.ROCK).setBlockName("concrete_colored_ext").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(140.0F)).setBlockTextureName(RefStrings.MODID + ":concrete_colored_ext");
		concrete = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("concrete").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(140.0F)).setBlockTextureName(RefStrings.MODID + ":concrete_tile");
		concrete_asbestos = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("concrete_asbestos").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(150.0F)).setBlockTextureName(RefStrings.MODID + ":concrete_asbestos");
		concrete_rebar = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("concrete_rebar").setCreativeTab(MainRegistry.blockTab).setHardness(50.0F).setResistance(240.0F)).setBlockTextureName(RefStrings.MODID + ":concrete_rebar");
		concrete_super = new BlockUberConcrete().setBlockName("concrete_super").setCreativeTab(MainRegistry.blockTab).setHardness(150.0F).setResistance(1000.0F);
		concrete_super_broken = com.hbm.blocks.PlainCubeTextures.bind(new BlockFalling(Material.ROCK).setTranslationKey("concrete_super_broken").setCreativeTab(MainRegistry.blockTab).setHardness(10.0F).setResistance(20.0F), RefStrings.MODID + ":concrete_super_broken");
		concrete_pillar = new BlockRotatablePillar(Material.ROCK, RefStrings.MODID + ":concrete_pillar_top").setTranslationKey("concrete_pillar").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(180.0F).setBlockTextureName(RefStrings.MODID + ":concrete_pillar_side");
		brick_concrete = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("brick_concrete").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(160.0F)).setBlockTextureName(RefStrings.MODID + ":brick_concrete");
		brick_concrete_mossy = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("brick_concrete_mossy").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(160.0F)).setBlockTextureName(RefStrings.MODID + ":brick_concrete_mossy");
		brick_concrete_cracked = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("brick_concrete_cracked").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(60.0F)).setBlockTextureName(RefStrings.MODID + ":brick_concrete_cracked");
		brick_concrete_broken = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("brick_concrete_broken").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(45.0F)).setBlockTextureName(RefStrings.MODID + ":brick_concrete_broken");
		brick_concrete_marked = new BlockWriting(Material.ROCK, RefStrings.MODID + ":brick_concrete").setTranslationKey("brick_concrete_marked").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(160.0F).setBlockTextureName(RefStrings.MODID + ":brick_concrete_marked");
		brick_obsidian = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("brick_obsidian").setCreativeTab(MainRegistry.blockTab).setLightOpacity(15).setHardness(15.0F).setResistance(120.0F)).setBlockTextureName(RefStrings.MODID + ":brick_obsidian");
		brick_light = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("brick_light").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(20.0F)).setBlockTextureName(RefStrings.MODID + ":brick_light");
		brick_compound = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("brick_compound").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(400.0F)).setBlockTextureName(RefStrings.MODID + ":brick_compound");
		cmb_brick = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("cmb_brick").setCreativeTab(MainRegistry.blockTab).setHardness(25.0F).setResistance(5000.0F)).setBlockTextureName(RefStrings.MODID + ":cmb_brick");
		cmb_brick_reinforced = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("cmb_brick_reinforced").setCreativeTab(MainRegistry.blockTab).setHardness(25.0F).setResistance(50000.0F)).setBlockTextureName(RefStrings.MODID + ":cmb_brick_reinforced");
		brick_asbestos = com.hbm.blocks.PlainCubeTextures.bind(new BlockOutgas(Material.ROCK, true, 5, true).setTranslationKey("brick_asbestos").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(1000.0F), RefStrings.MODID + ":brick_asbestos");
		brick_fire = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("brick_fire").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(35.0F)).setBlockTextureName(RefStrings.MODID + ":brick_fire");

		ducrete_smooth = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("ducrete_smooth").setCreativeTab(MainRegistry.blockTab).setHardness(20.0F).setResistance(500.0F)).setBlockTextureName(RefStrings.MODID + ":ducrete");
		ducrete = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("ducrete").setCreativeTab(MainRegistry.blockTab).setHardness(20.0F).setResistance(500.0F)).setBlockTextureName(RefStrings.MODID + ":ducrete_tile");
		brick_ducrete = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("brick_ducrete").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(750.0F)).setBlockTextureName(RefStrings.MODID + ":brick_ducrete");
		reinforced_ducrete = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("reinforced_ducrete").setCreativeTab(MainRegistry.blockTab).setHardness(20.0F).setResistance(1000.0F)).setBlockTextureName(RefStrings.MODID + ":reinforced_ducrete");

		lightstone = ((BlockBase) new BlockLightstone(Material.ROCK, LightstoneType.class, true, true).setBlockName("lightstone").setCreativeTab(MainRegistry.blockTab).setHardness(2F).setResistance(15.0F)).setBlockTextureName(RefStrings.MODID + ":lightstone");
		brick_forgotten = ((BlockPillar) new BlockPillar(Material.ROCK, RefStrings.MODID + ":brick_forgotten_top").setTranslationKey("brick_forgotten").setBlockUnbreakable().setResistance(666_666F)).setBlockTextureName(RefStrings.MODID + ":brick_forgotten");
		brick_forgotten_lock = new BlockForgottenLock(Material.ROCK, RefStrings.MODID + ":brick_forgotten_top").setTranslationKey("brick_forgotten_lock").setBlockUnbreakable().setResistance(666_666F).setBlockTextureName(RefStrings.MODID + ":brick_forgotten_lock");

		concrete_slab = new BlockMultiSlab(null, Material.ROCK, concrete_smooth, concrete, concrete_asbestos, ducrete_smooth, ducrete, asphalt).setTranslationKey("concrete_slab").setCreativeTab(MainRegistry.blockTab);
		concrete_double_slab = new BlockMultiSlab(concrete_slab, Material.ROCK, concrete_smooth, concrete, concrete_asbestos, ducrete_smooth, ducrete, asphalt).setTranslationKey("concrete_double_slab").setCreativeTab(MainRegistry.blockTab);
		concrete_brick_slab = new BlockMultiSlab(null, Material.ROCK, brick_concrete, brick_concrete_mossy, brick_concrete_cracked, brick_concrete_broken, brick_ducrete).setTranslationKey("concrete_brick_slab").setCreativeTab(MainRegistry.blockTab);
		concrete_brick_double_slab = new BlockMultiSlab(concrete_brick_slab, Material.ROCK, brick_concrete, brick_concrete_mossy, brick_concrete_cracked, brick_concrete_broken, brick_ducrete).setTranslationKey("concrete_brick_double_slab").setCreativeTab(MainRegistry.blockTab);
		brick_slab = new BlockMultiSlab(null, Material.ROCK, reinforced_stone, reinforced_brick, brick_obsidian, brick_light, brick_compound, brick_asbestos, brick_fire).setTranslationKey("brick_slab").setCreativeTab(MainRegistry.blockTab);
		brick_double_slab = new BlockMultiSlab(brick_slab, Material.ROCK, reinforced_stone, reinforced_brick, brick_obsidian, brick_light, brick_compound, brick_asbestos, brick_fire).setTranslationKey("brick_double_slab").setCreativeTab(MainRegistry.blockTab);
		stones_slab = new BlockMultiSlabMeta(null, Material.ROCK, new Block[] { lightstone, lightstone }, LightstoneType.TILE.ordinal(), LightstoneType.BRICKS.ordinal()).setTranslationKey("stones_slab").setCreativeTab(MainRegistry.blockTab);
		stones_double_slab = new BlockMultiSlabMeta(stones_slab, Material.ROCK, new Block[] { lightstone, lightstone }, LightstoneType.TILE.ordinal(), LightstoneType.BRICKS.ordinal()).setTranslationKey("stones_double_slab").setCreativeTab(MainRegistry.blockTab);

		concrete_smooth_stairs = new BlockGenericStairs(concrete_smooth, 0).setTranslationKey("concrete_smooth_stairs").setCreativeTab(MainRegistry.blockTab);
		concrete_stairs = new BlockGenericStairs(concrete, 0).setTranslationKey("concrete_stairs").setCreativeTab(MainRegistry.blockTab);
		concrete_asbestos_stairs = new BlockGenericStairs(concrete_asbestos, 0).setTranslationKey("concrete_asbestos_stairs").setCreativeTab(MainRegistry.blockTab);
		ducrete_smooth_stairs = new BlockGenericStairs(ducrete_smooth, 0).setTranslationKey("ducrete_smooth_stairs").setCreativeTab(MainRegistry.blockTab);
		ducrete_stairs = new BlockGenericStairs(ducrete, 0).setTranslationKey("ducrete_stairs").setCreativeTab(MainRegistry.blockTab);
		brick_concrete_stairs = new BlockGenericStairs(brick_concrete, 0).setTranslationKey("brick_concrete_stairs").setCreativeTab(MainRegistry.blockTab);
		brick_concrete_mossy_stairs = new BlockGenericStairs(brick_concrete_mossy, 0).setTranslationKey("brick_concrete_mossy_stairs").setCreativeTab(MainRegistry.blockTab);
		brick_concrete_cracked_stairs = new BlockGenericStairs(brick_concrete_cracked, 0).setTranslationKey("brick_concrete_cracked_stairs").setCreativeTab(MainRegistry.blockTab);
		brick_concrete_broken_stairs = new BlockGenericStairs(brick_concrete_broken, 0).setTranslationKey("brick_concrete_broken_stairs").setCreativeTab(MainRegistry.blockTab);
		brick_ducrete_stairs = new BlockGenericStairs(brick_ducrete, 0).setTranslationKey("brick_ducrete_stairs").setCreativeTab(MainRegistry.blockTab);
		reinforced_stone_stairs = new BlockGenericStairs(reinforced_stone, 0).setTranslationKey("reinforced_stone_stairs").setCreativeTab(MainRegistry.blockTab);
		reinforced_brick_stairs = new BlockGenericStairs(reinforced_brick, 0).setTranslationKey("reinforced_brick_stairs").setCreativeTab(MainRegistry.blockTab);
		brick_obsidian_stairs = new BlockGenericStairs(brick_obsidian, 0).setTranslationKey("brick_obsidian_stairs").setCreativeTab(MainRegistry.blockTab);
		brick_light_stairs = new BlockGenericStairs(brick_light, 0).setTranslationKey("brick_light_stairs").setCreativeTab(MainRegistry.blockTab);
		brick_compound_stairs = new BlockGenericStairs(brick_compound, 0).setTranslationKey("brick_compound_stairs").setCreativeTab(MainRegistry.blockTab);
		brick_asbestos_stairs = new BlockGenericStairs(brick_asbestos, 0).setTranslationKey("brick_asbestos_stairs").setCreativeTab(MainRegistry.blockTab);
		brick_fire_stairs = new BlockGenericStairs(brick_fire, 0).setTranslationKey("brick_fire_stairs").setCreativeTab(MainRegistry.blockTab);
		asphalt_stairs = new BlockSpeedyStairs(asphalt, 0, 1.5).setTranslationKey("asphalt_stairs").setCreativeTab(MainRegistry.blockTab);
		lightstone_tile_stairs = new BlockGenericStairs(lightstone, LightstoneType.TILE.ordinal()).setTranslationKey("lightstone_tile_stairs").setCreativeTab(MainRegistry.blockTab);
		lightstone_bricks_stairs = new BlockGenericStairs(lightstone, LightstoneType.BRICKS.ordinal()).setTranslationKey("lightstone_bricks_stairs").setCreativeTab(MainRegistry.blockTab);

		vinyl_tile = ((BlockBase) ((BlockBase) new BlockEnumMulti(Material.ROCK, TileType.class, true, true).setBlockName("vinyl_tile")).setSoundType(SoundType.GLASS).setCreativeTab(MainRegistry.blockTab).setHardness(10.0F).setResistance(60.0F)).setBlockTextureName(RefStrings.MODID + ":vinyl_tile");

		tile_lab = com.hbm.blocks.PlainCubeTextures.bind(((BlockOutgas) new BlockOutgas(Material.ROCK, false, 5, true).setTranslationKey("tile_lab")).withSoundType(SoundType.GLASS).setCreativeTab(MainRegistry.blockTab).setHardness(1.0F).setResistance(20.0F), RefStrings.MODID + ":tile_lab");
		tile_lab_cracked = com.hbm.blocks.PlainCubeTextures.bind(((BlockOutgas) new BlockOutgas(Material.ROCK, false, 5, true).setTranslationKey("tile_lab_cracked")).withSoundType(SoundType.GLASS).setCreativeTab(MainRegistry.blockTab).setHardness(1.0F).setResistance(20.0F), RefStrings.MODID + ":tile_lab_cracked");
		tile_lab_broken = com.hbm.blocks.PlainCubeTextures.bind(((BlockOutgas) new BlockOutgas(Material.ROCK, true, 5, true).setTranslationKey("tile_lab_broken")).withSoundType(SoundType.GLASS).setCreativeTab(MainRegistry.blockTab).setHardness(1.0F).setResistance(20.0F), RefStrings.MODID + ":tile_lab_broken");

		block_meteor = com.hbm.blocks.PlainCubeTextures.bind(new BlockStepEffect(Material.ROCK).noFortune().setTranslationKey("block_meteor").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(360.0F), RefStrings.MODID + ":meteor");
		block_meteor_cobble = com.hbm.blocks.PlainCubeTextures.bind(new BlockStepEffect(Material.ROCK).noFortune().setTranslationKey("block_meteor_cobble").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(360.0F), RefStrings.MODID + ":meteor_cobble");
		block_meteor_broken = com.hbm.blocks.PlainCubeTextures.bind(new BlockStepEffect(Material.ROCK).noFortune().setTranslationKey("block_meteor_broken").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(360.0F), RefStrings.MODID + ":meteor_crushed");
		block_meteor_molten = com.hbm.blocks.PlainCubeTextures.bind(new BlockStepEffect(Material.ROCK, true).noFortune().setTranslationKey("block_meteor_molten").setLightLevel(0.75F).setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(360.0F), RefStrings.MODID + ":meteor_cobble_molten");
		block_meteor_treasure = com.hbm.blocks.PlainCubeTextures.bind(new BlockMeteoriteTreasure(Material.ROCK).setTranslationKey("block_meteor_treasure").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(360.0F), RefStrings.MODID + ":meteor_treasure");
		meteor_polished = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("meteor_polished").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(360.0F)).setBlockTextureName(RefStrings.MODID + ":meteor_polished");
		meteor_brick = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("meteor_brick").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(360.0F)).setBlockTextureName(RefStrings.MODID + ":meteor_brick");
		meteor_brick_mossy = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("meteor_brick_mossy").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(360.0F)).setBlockTextureName(RefStrings.MODID + ":meteor_brick_mossy");
		meteor_brick_cracked = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("meteor_brick_cracked").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(360.0F)).setBlockTextureName(RefStrings.MODID + ":meteor_brick_cracked");
		meteor_brick_chiseled = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("meteor_brick_chiseled").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(360.0F)).setBlockTextureName(RefStrings.MODID + ":meteor_brick_chiseled");
		meteor_pillar = new BlockRotatablePillar(Material.ROCK, RefStrings.MODID + ":meteor_pillar_top").setTranslationKey("meteor_pillar").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(360.0F).setBlockTextureName(RefStrings.MODID + ":meteor_pillar");
		meteor_spawner = new BlockCybercrab(Material.ROCK).setTranslationKey("meteor_spawner").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(360.0F);
		meteor_battery = ((BlockPillar) new BlockPillar(Material.ROCK, RefStrings.MODID + ":meteor_power").setTranslationKey("meteor_battery").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(360.0F)).setBlockTextureName(RefStrings.MODID + ":meteor_spawner_side");

		moon_turf = new BlockFalling(Material.SAND).setTranslationKey("moon_turf").setStepSound(SoundType.SAND).setCreativeTab(MainRegistry.blockTab).setHardness(0.5F).setBlockTextureName(RefStrings.MODID + ":moon_turf");
		moon_rock = ((BlockBase) ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("moon_rock")).setSoundType(SoundType.STONE).setCreativeTab(MainRegistry.blockTab).setHardness(1.5F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":moon_rock");

		tumor = new BlockTint(Material.CLAY, "tumor_base2", "tumor_base3", "fleshv2").setTranslationKey("tumor").setStepSound(SoundType.SNOW).setCreativeTab(MainRegistry.blockTab).setHardness(1.0F).setBlockTextureName(RefStrings.MODID + ":tumor_base");

		duna_sands = new BlockFalling(Material.SAND).setTranslationKey("duna_sands").setStepSound(SoundType.SAND).setCreativeTab(MainRegistry.blockTab).setHardness(0.5F).setBlockTextureName(RefStrings.MODID + ":duna_sands");
		duna_cobble = ((BlockBase) ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("duna_cobble")).setSoundType(SoundType.STONE).setCreativeTab(MainRegistry.blockTab).setHardness(2.0F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":duna_cobble");
		duna_rock = ((BlockBase) ((BlockBase) new BlockGenericDrop(Material.ROCK, duna_cobble).setBlockName("duna_rock")).setSoundType(SoundType.STONE).setCreativeTab(MainRegistry.blockTab).setHardness(1.5F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":duna_rock");
		dry_ice = ((BlockBase) ((BlockBase) new BlockGeneric(Material.ICE).setBlockName("dry_ice")).setSoundType(SoundType.STONE).setCreativeTab(MainRegistry.blockTab).setHardness(0.5F)).setBlockTextureName(RefStrings.MODID + ":ice_dry");
		ferric_clay = ((BlockBase) ((BlockBase) new BlockGeneric(Material.CLAY).setBlockName("ferric_clay")).setSoundType(SoundType.GROUND).setCreativeTab(MainRegistry.blockTab).setHardness(0.6F)).setBlockTextureName(RefStrings.MODID + ":ferric_clay");

		eve_silt = new BlockFalling(Material.SAND).setTranslationKey("eve_silt").setStepSound(SoundType.SAND).setCreativeTab(MainRegistry.blockTab).setHardness(0.5F).setBlockTextureName(RefStrings.MODID + ":eve_silt");
		eve_rock = ((BlockBase) ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("eve_rock")).setSoundType(SoundType.STONE).setCreativeTab(MainRegistry.blockTab).setHardness(1.5F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":eve_stone_2");

		laythe_silt = new BlockFallingTint(Material.SAND).setTranslationKey("laythe_silt").setSoundType(net.minecraft.block.SoundType.SAND).setCreativeTab(MainRegistry.blockTab).setHardness(0.5F);


		ike_regolith = new BlockTint(Material.ROCK, "ike_regolith2", "ike_regolith3", "ike_regolith4", "ike_regolith5").setTranslationKey("ike_regolith").setStepSound(SoundType.STONE).setCreativeTab(MainRegistry.blockTab).setHardness(1.5F).setResistance(10.0F).setBlockTextureName(RefStrings.MODID + ":ike_regolith");
		ike_stone = ((BlockBase) ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("ike_stone")).setSoundType(SoundType.STONE).setCreativeTab(MainRegistry.blockTab).setHardness(1.5F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":ike_stone");

		dres_rock = ((BlockBase) ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("dres_rock")).setSoundType(SoundType.STONE).setCreativeTab(MainRegistry.blockTab).setHardness(1.5F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":dresbase");

		moho_regolith = ((BlockBase) ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("moho_regolith")).setSoundType(SoundType.STONE).setCreativeTab(MainRegistry.blockTab).setHardness(1.5F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":moho_regolith");
		moho_stone = ((BlockBase) ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("moho_stone")).setSoundType(SoundType.STONE).setCreativeTab(MainRegistry.blockTab).setHardness(1.5F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":moho_stone");

		minmus_regolith = ((BlockBase) ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("minmus_regolith")).setSoundType(SoundType.STONE).setCreativeTab(MainRegistry.blockTab).setHardness(1.5F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":minmus_regolith");
		minmus_stone = ((BlockBase) ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("minmus_stone")).setSoundType(SoundType.STONE).setCreativeTab(MainRegistry.blockTab).setHardness(1.5F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":minmus_stone");
		minmus_smooth = ((BlockBase) ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("minmus_smooth")).setSoundType(SoundType.STONE).setCreativeTab(MainRegistry.blockTab).setHardness(1.5F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":minflat_regolith");


		brick_jungle = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("brick_jungle").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(360.0F)).setBlockTextureName(RefStrings.MODID + ":brick_jungle");
		brick_jungle_cracked = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("brick_jungle_cracked").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(360.0F)).setBlockTextureName(RefStrings.MODID + ":brick_jungle_cracked");
		brick_jungle_fragile = com.hbm.blocks.PlainCubeTextures.bind(new FragileBrick(Material.ROCK).setTranslationKey("brick_jungle_fragile").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(360.0F), RefStrings.MODID + ":brick_jungle_fragile");
		brick_jungle_lava = ((BlockBase) new BlockGeneric(Material.ROCK).setBlockName("brick_jungle_lava").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(360.0F).setLightLevel(5F/15F)).setBlockTextureName(RefStrings.MODID + ":brick_jungle_lava");
		brick_jungle_ooze = com.hbm.blocks.PlainCubeTextures.bind(new BlockStepEffect(Material.ROCK).setTranslationKey("brick_jungle_ooze").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(360.0F).setLightLevel(5F/15F), RefStrings.MODID + ":brick_jungle_ooze");
		brick_jungle_mystic = com.hbm.blocks.PlainCubeTextures.bind(new BlockStepEffect(Material.ROCK).setTranslationKey("brick_jungle_mystic").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(360.0F).setLightLevel(5F/15F), RefStrings.MODID + ":brick_jungle_mystic");
		brick_jungle_trap = new TrappedBrick(Material.ROCK).setTranslationKey("brick_jungle_trap").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(360.0F).setBlockTextureName(RefStrings.MODID + ":brick_jungle_trap");
		brick_jungle_glyph = new BlockGlyph(Material.ROCK).setTranslationKey("brick_jungle_glyph").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(360.0F);
		brick_jungle_circle = new BlockBallsSpawner(Material.ROCK).setTranslationKey("brick_jungle_circle").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(360.0F).setBlockTextureName(RefStrings.MODID + ":brick_jungle_circle");

		brick_red = new BlockRedBrick(Material.ROCK).setTranslationKey("brick_red").setResistance(10_000);

		deco_computer = new BlockDecoModel(Material.IRON, DecoComputerEnum.class, true, false).setBlockBoundsTo(.125F, 0F, 0F, .875F, .875F, .625F).setBlockName("deco_computer").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		deco_crt = new BlockDecoCRT(Material.IRON).setBlockName("deco_crt").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		deco_toaster = new BlockDecoToaster(Material.IRON).setBlockName("deco_toaster").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(10.0F);
		filing_cabinet = new BlockDecoContainer(Material.IRON, DecoCabinetEnum.class, true, false, TileEntityFileCabinet.class).setBlockBoundsTo(.1875F, 0F, 0F, .8125F, 1F, .75F).setBlockName("filing_cabinet").setCreativeTab(MainRegistry.blockTab).setHardness(10.0F).setResistance(15.0F);

		tape_recorder = new DecoTapeRecorder(Material.IRON).setTranslationKey("tape_recorder").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(15.0F);
		steel_poles = new DecoSteelPoles(Material.IRON).setTranslationKey("steel_poles").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(15.0F);
		pole_top = new DecoPoleTop(Material.IRON).setTranslationKey("pole_top").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(15.0F);
		pole_satellite_receiver = new DecoPoleSatelliteReceiver(Material.IRON).setTranslationKey("pole_satellite_receiver").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(15.0F).setBlockTextureName(RefStrings.MODID + ":deco_satellite_receiver");
		steel_wall = new BlockSteelDecoration(Material.IRON, false).setTranslationKey("steel_wall").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(15.0F);
		steel_corner = new BlockSteelDecoration(Material.IRON, true).setTranslationKey("steel_corner").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(15.0F);
		steel_roof = new DecoBlock(Material.IRON).setTranslationKey("steel_roof").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(15.0F).setBlockTextureName(RefStrings.MODID + ":steel_roof");
		steel_beam = new BlockSteelBeam(Material.IRON).setTranslationKey("steel_beam").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(15.0F);
		steel_scaffold = new BlockScaffold().setTranslationKey("steel_scaffold").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(15.0F);
		steel_grate = new BlockGrate(Material.IRON, false).setTranslationKey("steel_grate").setSoundType(ModSoundTypes.grate).setCreativeTab(MainRegistry.blockTab).setHardness(2.0F).setResistance(5.0F);
		steel_grate_wide = new BlockGrate(Material.IRON, true).setTranslationKey("steel_grate_wide").setSoundType(ModSoundTypes.grate).setCreativeTab(MainRegistry.blockTab).setHardness(2.0F).setResistance(5.0F);

		//TODO: dear god
		deco_pipe = new BlockPipe(Material.IRON, RefStrings.MODID + ":pipe_side", 0).setTranslationKey("deco_pipe").setCreativeTab(MainRegistry.blockTab).setHardness(2.0F).setResistance(5.0F);
		deco_pipe_rusted = new BlockPipe(Material.IRON, RefStrings.MODID + ":pipe_side_rusty", 0).setTranslationKey("deco_pipe_rusted").setCreativeTab(MainRegistry.blockTab).setHardness(2.0F).setResistance(5.0F);
		deco_pipe_green = new BlockPipe(Material.IRON, RefStrings.MODID + ":pipe_side_green", 0).setTranslationKey("deco_pipe_green").setCreativeTab(MainRegistry.blockTab).setHardness(2.0F).setResistance(5.0F);
		deco_pipe_green_rusted = new BlockPipe(Material.IRON, RefStrings.MODID + ":pipe_side_green_rusty", 0).setTranslationKey("deco_pipe_green_rusted").setCreativeTab(MainRegistry.blockTab).setHardness(2.0F).setResistance(5.0F);
		deco_pipe_red = new BlockPipe(Material.IRON, RefStrings.MODID + ":pipe_side_red", 0).setTranslationKey("deco_pipe_red").setCreativeTab(MainRegistry.blockTab).setHardness(2.0F).setResistance(5.0F);
		deco_pipe_marked = new BlockPipe(Material.IRON, RefStrings.MODID + ":pipe_side_marked", 0).setTranslationKey("deco_pipe_marked").setCreativeTab(MainRegistry.blockTab).setHardness(2.0F).setResistance(5.0F);
		deco_pipe_rim = new BlockPipe(Material.IRON, RefStrings.MODID + ":pipe_side", 1).setTranslationKey("deco_pipe_rim").setCreativeTab(MainRegistry.blockTab).setHardness(2.0F).setResistance(5.0F);
		deco_pipe_rim_rusted = new BlockPipe(Material.IRON, RefStrings.MODID + ":pipe_side_rusty", 1).setTranslationKey("deco_pipe_rim_rusted").setCreativeTab(MainRegistry.blockTab).setHardness(2.0F).setResistance(5.0F);
		deco_pipe_rim_green = new BlockPipe(Material.IRON, RefStrings.MODID + ":pipe_side_green", 1).setTranslationKey("deco_pipe_rim_green").setCreativeTab(MainRegistry.blockTab).setHardness(2.0F).setResistance(5.0F);
		deco_pipe_rim_green_rusted = new BlockPipe(Material.IRON, RefStrings.MODID + ":pipe_side_green_rusty", 1).setTranslationKey("deco_pipe_rim_green_rusted").setCreativeTab(MainRegistry.blockTab).setHardness(2.0F).setResistance(5.0F);
		deco_pipe_rim_red = new BlockPipe(Material.IRON, RefStrings.MODID + ":pipe_side_red", 1).setTranslationKey("deco_pipe_rim_red").setCreativeTab(MainRegistry.blockTab).setHardness(2.0F).setResistance(5.0F);
		deco_pipe_rim_marked = new BlockPipe(Material.IRON, RefStrings.MODID + ":pipe_side_marked", 1).setTranslationKey("deco_pipe_rim_marked").setCreativeTab(MainRegistry.blockTab).setHardness(2.0F).setResistance(5.0F);
		deco_pipe_framed = new BlockPipe(Material.IRON, RefStrings.MODID + ":pipe_side", 3).setTranslationKey("deco_pipe_framed").setCreativeTab(MainRegistry.blockTab).setHardness(2.0F).setResistance(5.0F);
		deco_pipe_framed_rusted = new BlockPipe(Material.IRON, RefStrings.MODID + ":pipe_side_rusty", 3).setTranslationKey("deco_pipe_framed_rusted").setCreativeTab(MainRegistry.blockTab).setHardness(2.0F).setResistance(5.0F);
		deco_pipe_framed_green = new BlockPipe(Material.IRON, RefStrings.MODID + ":pipe_side_green", 3).setTranslationKey("deco_pipe_framed_green").setCreativeTab(MainRegistry.blockTab).setHardness(2.0F).setResistance(5.0F);
		deco_pipe_framed_green_rusted = new BlockPipe(Material.IRON, RefStrings.MODID + ":pipe_side_green_rusty", 3).setTranslationKey("deco_pipe_framed_green_rusted").setCreativeTab(MainRegistry.blockTab).setHardness(2.0F).setResistance(5.0F);
		deco_pipe_framed_red = new BlockPipe(Material.IRON, RefStrings.MODID + ":pipe_side_red", 3).setTranslationKey("deco_pipe_framed_red").setCreativeTab(MainRegistry.blockTab).setHardness(2.0F).setResistance(5.0F);
		deco_pipe_framed_marked = new BlockPipe(Material.IRON, RefStrings.MODID + ":pipe_side_marked", 3).setTranslationKey("deco_pipe_framed_marked").setCreativeTab(MainRegistry.blockTab).setHardness(2.0F).setResistance(5.0F);
		deco_pipe_quad = new BlockPipe(Material.IRON, RefStrings.MODID + ":pipe_side", 2).setTranslationKey("deco_pipe_quad").setCreativeTab(MainRegistry.blockTab).setHardness(2.0F).setResistance(5.0F);
		deco_pipe_quad_rusted = new BlockPipe(Material.IRON, RefStrings.MODID + ":pipe_side_rusty", 2).setTranslationKey("deco_pipe_quad_rusted").setCreativeTab(MainRegistry.blockTab).setHardness(2.0F).setResistance(5.0F);
		deco_pipe_quad_green = new BlockPipe(Material.IRON, RefStrings.MODID + ":pipe_side_green", 2).setTranslationKey("deco_pipe_quad_green").setCreativeTab(MainRegistry.blockTab).setHardness(2.0F).setResistance(5.0F);
		deco_pipe_quad_green_rusted = new BlockPipe(Material.IRON, RefStrings.MODID + ":pipe_side_green_rusty", 2).setTranslationKey("deco_pipe_quad_green_rusted").setCreativeTab(MainRegistry.blockTab).setHardness(2.0F).setResistance(5.0F);
		deco_pipe_quad_red = new BlockPipe(Material.IRON, RefStrings.MODID + ":pipe_side_red", 2).setTranslationKey("deco_pipe_quad_red").setCreativeTab(MainRegistry.blockTab).setHardness(2.0F).setResistance(5.0F);
		deco_pipe_quad_marked = new BlockPipe(Material.IRON, RefStrings.MODID + ":pipe_side_marked", 2).setTranslationKey("deco_pipe_quad_marked").setCreativeTab(MainRegistry.blockTab).setHardness(2.0F).setResistance(5.0F);

		broadcaster_pc = new PinkCloudBroadcaster(Material.IRON).setTranslationKey("broadcaster_pc").setCreativeTab(MainRegistry.machineTab).setHardness(5.0F).setResistance(15.0F).setBlockTextureName(RefStrings.MODID + ":broadcaster_pc");
		geiger = new GeigerCounter(Material.IRON).setTranslationKey("geiger").setCreativeTab(MainRegistry.machineTab).setHardness(15.0F).setResistance(0.25F).setBlockTextureName(RefStrings.MODID + ":geiger");
		hev_battery = new HEVBattery(Material.IRON).setTranslationKey("hev_battery").setCreativeTab(MainRegistry.machineTab).setLightLevel(10F/15F).setHardness(0.5F).setResistance(0.25F);

		fence_metal = new BlockMetalFence(Material.IRON).setTranslationKey("fence_metal").setCreativeTab(MainRegistry.machineTab).setHardness(15.0F).setResistance(0.25F);

		ash_digamma = new BlockAshes(Material.SAND).setTranslationKey("ash_digamma").setStepSound(SoundType.SAND).setCreativeTab(MainRegistry.machineTab).setHardness(0.5F).setResistance(150.0F).setBlockTextureName(RefStrings.MODID + ":ash_digamma");
		sand_mix = ((BlockBase) new BlockNTMSand(Material.SAND).setBlockName("sand_mix")).setSoundType(SoundType.SAND).setCreativeTab(MainRegistry.machineTab).setHardness(0.5F);
		glass_boron = new BlockNTMGlassCT(0, RefStrings.MODID + ":glass_boron", Material.GLASS).setTranslationKey("glass_boron").setSoundType(net.minecraft.block.SoundType.GLASS).setCreativeTab(MainRegistry.machineTab).setHardness(0.3F);
		glass_lead = new BlockNTMGlassCT(0, RefStrings.MODID + ":glass_lead", Material.GLASS).setTranslationKey("glass_lead").setSoundType(net.minecraft.block.SoundType.GLASS).setCreativeTab(MainRegistry.machineTab).setHardness(0.3F);
		glass_uranium = new BlockNTMGlassCT(1, RefStrings.MODID + ":glass_uranium", Material.GLASS).setTranslationKey("glass_uranium").setLightLevel(5F/15F).setSoundType(net.minecraft.block.SoundType.GLASS).setCreativeTab(MainRegistry.machineTab).setHardness(0.3F);
		glass_trinitite = new BlockNTMGlassCT(1, RefStrings.MODID + ":glass_trinitite", Material.GLASS).setTranslationKey("glass_trinitite").setLightLevel(5F/15F).setSoundType(net.minecraft.block.SoundType.GLASS).setCreativeTab(MainRegistry.machineTab).setHardness(0.3F);
		glass_polonium = new BlockNTMGlassCT(1, RefStrings.MODID + ":glass_polonium", Material.GLASS).setTranslationKey("glass_polonium").setLightLevel(5F/15F).setSoundType(net.minecraft.block.SoundType.GLASS).setCreativeTab(MainRegistry.machineTab).setHardness(0.3F);
		glass_ash = new BlockNTMGlassCT(1, RefStrings.MODID + ":glass_ash", Material.GLASS).setTranslationKey("glass_ash").setSoundType(net.minecraft.block.SoundType.GLASS).setCreativeTab(MainRegistry.machineTab).setHardness(3F);
		glass_quartz = new BlockNTMGlassCT(0, RefStrings.MODID + ":glass_quartz", Material.PACKED_ICE, true).setTranslationKey("glass_quartz").setCreativeTab(MainRegistry.blockTab).setSoundType(net.minecraft.block.SoundType.GLASS).setHardness(1.0F).setResistance(40.0F);
		glass_polarized = new BlockNTMGlassCT(0, RefStrings.MODID + ":glass_polarized", Material.GLASS).setTranslationKey("glass_polarized").setSoundType(net.minecraft.block.SoundType.GLASS).setCreativeTab(MainRegistry.machineTab).setHardness(0.3F);

		mush = new BlockMush(Material.PLANTS).setTranslationKey("mush").setCreativeTab(MainRegistry.blockTab).setLightLevel(0.5F).setStepSound(SoundType.PLANT).setBlockTextureName(RefStrings.MODID + ":mush");
		mush_block = new BlockMushHuge(Material.PLANTS).setTranslationKey("mush_block").setLightLevel(1.0F).setStepSound(SoundType.PLANT).setHardness(0.2F).setBlockTextureName(RefStrings.MODID + ":mush_block_skin");
		mush_block_stem = new BlockMushHuge(Material.PLANTS).setTranslationKey("mush_block_stem").setLightLevel(1.0F).setStepSound(SoundType.PLANT).setHardness(0.2F).setBlockTextureName(RefStrings.MODID + ":mush_block_stem");
		glyphid_base = new BlockGlyphid(Material.CORAL).setTranslationKey("glyphid_base").setStepSound(SoundType.CLOTH).setHardness(0.5F).setBlockTextureName(RefStrings.MODID + ":glyphid_base");
		glyphid_spawner = new BlockGlyphidSpawner(Material.CORAL).setTranslationKey("glyphid_spawner").setStepSound(SoundType.CLOTH).setHardness(0.5F).setBlockTextureName(RefStrings.MODID + ":glyphid_eggs_alt");

		rubber_farmland = new BlockRubberFarm(Material.SAND).setTranslationKey("rubber_farmland").setStepSound(SoundType.GROUND).setCreativeTab(MainRegistry.blockTab).setHardness(1.0F).setResistance(1.0F).setBlockTextureName(RefStrings.MODID + ":rubber_silt");

		plant_flower = ((BlockBase) new BlockNTMFlower().setBlockName("plant_flower").setCreativeTab(MainRegistry.blockTab)).setSoundType(SoundType.PLANT).setHardness(0.0F);
		plant_tall = ((BlockBase) new BlockTallPlant().setBlockName("plant_tall").setCreativeTab(MainRegistry.blockTab)).setSoundType(SoundType.PLANT).setHardness(0.0F);
		plant_dead = ((BlockBase) new BlockDeadPlant().setBlockName("plant_dead").setCreativeTab(MainRegistry.blockTab)).setSoundType(SoundType.PLANT).setHardness(0.0F);
		reeds = new BlockReeds().setTranslationKey("plant_reeds").setCreativeTab(MainRegistry.blockTab).setSoundType(SoundType.PLANT).setHardness(0.0F);
		plant_tall_laythe = ((BlockBase) new BlockTallPlantWater().setBlockName("plant_tall_laythe").setCreativeTab(MainRegistry.blockTab)).setSoundType(SoundType.PLANT).setHardness(0.0F);
		laythe_kelp = new BlockKelp().setTranslationKey("laythe_kelp").setCreativeTab(MainRegistry.blockTab).setStepSound(SoundType.PLANT).setHardness(0.0F);
		laythe_short = new BlockWaterPlant().setTranslationKey("laythe_seagrass").setCreativeTab(MainRegistry.blockTab).setStepSound(SoundType.PLANT).setHardness(0.0F).setBlockTextureName(RefStrings.MODID + ":laythe_seagrass");
		laythe_glow = new BlockWaterPlant().setTranslationKey("laythe_glowgrass").setCreativeTab(MainRegistry.blockTab).setStepSound(SoundType.PLANT).setHardness(0.0F).setLightLevel(1.0F).setBlockTextureName(RefStrings.MODID + ":laythe_glowgrass");
		crop_strawberry = new BlockCrop(Blocks.FARMLAND, (atmosphere) -> atmosphere.hasFluid(Fluids.EARTHAIR, 0.1) || atmosphere.hasFluid(Fluids.OXYGEN, 0.1), true).setTranslationKey("crop_strawberry").setStepSound(SoundType.PLANT).setHardness(0.0F).setBlockTextureName(RefStrings.MODID + ":strawberry");
		crop_mint = new BlockCrop(Blocks.FARMLAND, (atmosphere) -> atmosphere.hasFluid(Fluids.EARTHAIR, 0.1) || atmosphere.hasFluid(Fluids.OXYGEN, 0.1), true).setTranslationKey("crop_mint").setStepSound(SoundType.PLANT).setHardness(0.0F).setBlockTextureName(RefStrings.MODID + ":mint");
		crop_coffee = new BlockCrop(Blocks.FARMLAND, (atmosphere) -> atmosphere.hasFluid(Fluids.EARTHAIR, 0.1) || atmosphere.hasFluid(Fluids.OXYGEN, 0.1), true).setTranslationKey("crop_coffee").setStepSound(SoundType.PLANT).setHardness(0.0F).setBlockTextureName(RefStrings.MODID + ":coffee");
		crop_tea = new BlockCrop(Blocks.FARMLAND, (atmosphere) -> atmosphere.hasFluid(Fluids.EARTHAIR, 0.1) || atmosphere.hasFluid(Fluids.OXYGEN, 0.1), true).setTranslationKey("crop_tea").setStepSound(SoundType.PLANT).setHardness(0.0F).setBlockTextureName(RefStrings.MODID + ":tea");
		laythe_coral = new BlockCoral().setTranslationKey("laythe_coral").setCreativeTab(MainRegistry.blockTab).setSoundType(SoundType.PLANT).setHardness(0.0F);
		laythe_coral_block = ((BlockBase) ((BlockBase) new BlockEnumMulti(Material.CORAL, BlockCoral.EnumCoral.class, false, true).setHardness(0.5F).setBlockName("laythe_coral_block").setCreativeTab(MainRegistry.blockTab)).setSoundType(SoundType.PLANT)).setBlockTextureName(RefStrings.MODID + ":laythe_coral_block");
		vine_phosphor = new BlockHangingVine(thick_foliage).setTranslationKey("vine_phosphor").setCreativeTab(MainRegistry.blockTab).setSoundType(SoundType.PLANT).setHardness(0.5F);
		crop_paraffin = new BlockCrop(ModBlocks.rubber_farmland, (atmosphere) -> atmosphere.hasFluid(Fluids.TEKTOAIR, 0.1) || atmosphere.hasFluid(Fluids.CHLORINE, 0.1), false).setTranslationKey("crop_paraffin").setStepSound(SoundType.PLANT).setHardness(0.0F).setBlockTextureName(RefStrings.MODID + ":paraffin");
		sapling_pvc = new BlockNTSapling().setTranslationKey("sapling").setCreativeTab(MainRegistry.blockTab);

		waste_earth = ((WasteEarth) new WasteEarth(Material.GROUND, true).setTranslationKey("waste_earth")).withSoundType(SoundType.PLANT).setCreativeTab(MainRegistry.blockTab).setHardness(0.6F).setBlockTextureName(RefStrings.MODID + ":waste_earth");
		waste_mycelium = ((WasteEarth) new WasteEarth(Material.GROUND, true).setTranslationKey("waste_mycelium")).withSoundType(SoundType.PLANT).setLightLevel(1F).setCreativeTab(MainRegistry.blockTab).setHardness(0.6F).setBlockTextureName(RefStrings.MODID + ":waste_mycelium_side");
		waste_trinitite = com.hbm.blocks.PlainCubeTextures.bind(((BlockStepEffect) new BlockStepEffect(Material.SAND).noFortune().setTranslationKey("waste_trinitite")).withSoundType(SoundType.SAND).setCreativeTab(MainRegistry.blockTab).setHardness(0.5F).setResistance(2.5F), RefStrings.MODID + ":waste_trinitite");
		waste_trinitite_red = com.hbm.blocks.PlainCubeTextures.bind(((BlockStepEffect) new BlockStepEffect(Material.SAND).noFortune().setTranslationKey("waste_trinitite_red")).withSoundType(SoundType.SAND).setCreativeTab(MainRegistry.blockTab).setHardness(0.5F).setResistance(2.5F), RefStrings.MODID + ":waste_trinitite_red");
		waste_log = ((WasteLog) new WasteLog(Material.WOOD).setTranslationKey("waste_log")).withSoundType(SoundType.WOOD).setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(2.5F);
		waste_leaves = ((WasteLeaves) new WasteLeaves(Material.LEAVES).setTranslationKey("waste_leaves")).withSoundType(SoundType.PLANT).setCreativeTab(MainRegistry.blockTab).setHardness(0.1F).setBlockTextureName(RefStrings.MODID + ":waste_leaves");
		waste_planks = com.hbm.blocks.PlainCubeTextures.bind(((BlockStepEffect) new BlockStepEffect(Material.WOOD).setTranslationKey("waste_planks")).withSoundType(SoundType.WOOD).setCreativeTab(MainRegistry.blockTab).setHardness(0.5F).setResistance(2.5F), RefStrings.MODID + ":waste_planks");
		frozen_dirt = com.hbm.blocks.PlainCubeTextures.bind(((BlockStepEffect) new BlockStepEffect(Material.GROUND).setTranslationKey("frozen_dirt")).withSoundType(SoundType.GLASS).setCreativeTab(MainRegistry.blockTab).setHardness(0.5F).setResistance(2.5F), RefStrings.MODID + ":frozen_dirt");
		frozen_grass = ((WasteEarth) new WasteEarth(Material.GROUND, false).setTranslationKey("frozen_grass")).withSoundType(SoundType.GLASS).setCreativeTab(MainRegistry.blockTab).setHardness(0.5F).setResistance(2.5F);
		frozen_log = ((WasteLog) new WasteLog(Material.WOOD).setTranslationKey("frozen_log")).withSoundType(SoundType.GLASS).setCreativeTab(MainRegistry.blockTab).setHardness(0.5F).setResistance(2.5F);
		frozen_planks = com.hbm.blocks.PlainCubeTextures.bind(((BlockStepEffect) new BlockStepEffect(Material.WOOD).setTranslationKey("frozen_planks")).withSoundType(SoundType.GLASS).setCreativeTab(MainRegistry.blockTab).setHardness(0.5F).setResistance(2.5F), RefStrings.MODID + ":frozen_planks");
		fallout = new BlockFallout(Material.SAND).setTranslationKey("fallout").setStepSound(SoundType.GROUND).setCreativeTab(MainRegistry.blockTab).setHardness(0.1F).setLightOpacity(0).setBlockTextureName(RefStrings.MODID + ":ash");
		salted_fallout = new BlockFallout(Material.SNOW).setTranslationKey("salted_fallout").setStepSound(SoundType.GROUND).setCreativeTab(MainRegistry.blockTab).setHardness(0.1F).setLightOpacity(0).setBlockTextureName(RefStrings.MODID + ":ash");
		foam_layer = new BlockLayering(Material.SNOW).setTranslationKey("foam_layer").setStepSound(SoundType.SNOW).setCreativeTab(MainRegistry.blockTab).setHardness(0.1F).setLightOpacity(0).setBlockTextureName(RefStrings.MODID + ":foam");
		sand_boron_layer = new BlockLayering(Material.SAND).setTranslationKey("sand_boron_layer").setStepSound(SoundType.SAND).setCreativeTab(MainRegistry.blockTab).setHardness(0.1F).setLightOpacity(0).setBlockTextureName(RefStrings.MODID + ":sand_boron");
		leaves_layer = new BlockLayering(Material.LEAVES).setTranslationKey("leaves_layer").setStepSound(SoundType.PLANT).setCreativeTab(MainRegistry.blockTab).setHardness(0.1F).setLightOpacity(0).setBlockTextureName(RefStrings.MODID + ":waste_leaves");
		oil_spill = new BlockLayering(Material.GROUND).setTranslationKey("oil_spill").setStepSound(SoundType.SNOW).setCreativeTab(MainRegistry.blockTab).setHardness(0.1F).setLightOpacity(0).setBlockTextureName(RefStrings.MODID + ":oil_spill");

		burning_earth = ((WasteEarth) new WasteEarth(Material.GROUND, true).setTranslationKey("burning_earth")).withSoundType(SoundType.PLANT).setCreativeTab(MainRegistry.blockTab).setHardness(0.6F).setBlockTextureName(RefStrings.MODID + ":burning_earth");
		tektite = ((BlockBase) ((BlockBase) new BlockGeneric(Material.SAND).setBlockName("tektite")).setSoundType(SoundType.SAND).setCreativeTab(MainRegistry.blockTab).setHardness(0.5F)).setBlockTextureName(RefStrings.MODID + ":tektite");
		ore_tektite_osmiridium = ((BlockBase) ((BlockBase) new BlockGeneric(Material.SAND).setBlockName("ore_tektite_osmiridium")).setSoundType(SoundType.SAND).setCreativeTab(MainRegistry.blockTab).setHardness(0.5F)).setBlockTextureName(RefStrings.MODID + ":ore_tektite_osmiridium");
		impact_dirt = new BlockDirt(Material.GROUND, true).setTranslationKey("impact_dirt").setStepSound(SoundType.GROUND).setCreativeTab(MainRegistry.blockTab).setHardness(0.5F).setBlockTextureName(RefStrings.MODID + ":waste_earth_bottom");
		dirt_dead = new BlockFalling(Material.GROUND).setTranslationKey("dirt_dead").setStepSound(SoundType.GROUND).setCreativeTab(MainRegistry.blockTab).setHardness(0.5F).setBlockTextureName(RefStrings.MODID + ":dirt_dead");
		dirt_oily = new BlockFalling(Material.GROUND).setTranslationKey("dirt_oily").setStepSound(SoundType.GROUND).setCreativeTab(MainRegistry.blockTab).setHardness(0.5F).setBlockTextureName(RefStrings.MODID + ":dirt_oily");
		sand_dirty = new BlockFalling(Material.SAND).setTranslationKey("sand_dirty").setStepSound(SoundType.SAND).setCreativeTab(MainRegistry.blockTab).setHardness(0.5F).setBlockTextureName(RefStrings.MODID + ":sand_dirty");
		sand_dirty_red = new BlockFalling(Material.SAND).setTranslationKey("sand_dirty_red").setStepSound(SoundType.SAND).setCreativeTab(MainRegistry.blockTab).setHardness(0.5F).setBlockTextureName(RefStrings.MODID + ":sand_dirty_red");
		stone_cracked = new BlockFalling(Material.ROCK).setTranslationKey("stone_cracked").setStepSound(SoundType.STONE).setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setBlockTextureName(RefStrings.MODID + ":stone_cracked");

		sellafield_slaked = new BlockSellafieldSlaked(Material.ROCK).setTranslationKey("sellafield_slaked").setStepSound(SoundType.STONE).setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setBlockTextureName(RefStrings.MODID + ":sellafield_slaked");
		sellafield_bedrock = new BlockSellafieldSlaked(Material.ROCK).setTranslationKey("sellafield_bedrock").setBlockUnbreakable().setResistance(6000000.0F).setStepSound(SoundType.STONE).setCreativeTab(MainRegistry.blockTab).setBlockTextureName(RefStrings.MODID + ":sellafield_slaked");
		sellafield = new BlockSellafield(Material.ROCK).setTranslationKey("sellafield").setStepSound(SoundType.STONE).setHardness(5.0F).setBlockTextureName(RefStrings.MODID + ":sellafield_0");
		ore_sellafield_diamond = new BlockSellafieldOre(Material.ROCK).withSoundType(net.minecraft.block.SoundType.STONE).setTranslationKey("ore_sellafield_diamond").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F);
		ore_sellafield_emerald = new BlockSellafieldOre(Material.ROCK).withSoundType(net.minecraft.block.SoundType.STONE).setTranslationKey("ore_sellafield_emerald").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F);
		ore_sellafield_uranium_scorched = new BlockSellafieldOre(Material.ROCK).withSoundType(net.minecraft.block.SoundType.STONE).setTranslationKey("ore_sellafield_uranium_scorched").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F);
		ore_sellafield_schrabidium = new BlockSellafieldOre(Material.ROCK).withSoundType(net.minecraft.block.SoundType.STONE).setTranslationKey("ore_sellafield_schrabidium").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F);
		ore_sellafield_radgem = new BlockSellafieldOre(Material.ROCK).withSoundType(net.minecraft.block.SoundType.STONE).setTranslationKey("ore_sellafield_radgem").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F);

		geysir_water = new BlockGeysir(Material.ROCK).setTranslationKey("geysir_water").setStepSound(SoundType.STONE).setHardness(5.0F);
		geysir_chlorine = new BlockGeysir(Material.ROCK).setTranslationKey("geysir_chlorine").setStepSound(SoundType.STONE).setHardness(5.0F);
		geysir_vapor = new BlockGeysir(Material.ROCK).setTranslationKey("geysir_vapor").setStepSound(SoundType.STONE).setHardness(5.0F);
		geysir_nether = new BlockGeysir(Material.ROCK).setTranslationKey("geysir_nether").setLightLevel(1.0F).setStepSound(SoundType.STONE).setHardness(2.0F);
		geysir_volcanic = new BlockGeysir(Material.ROCK).setTranslationKey("geysir_volcanic").setLightLevel(1.0F).setStepSound(SoundType.STONE).setHardness(2.0F);
		geysir_electric = new BlockVolcanoV2(Material.ROCK).setTranslationKey("geysir_electric").setLightLevel(1.0F).setStepSound(SoundType.STONE).setHardness(2.0F).setBlockTextureName(RefStrings.MODID + ":basalt");
		geysir_chloric = new BlockGeysierDCM(Material.ROCK).setTranslationKey("geysir_chloric").setLightLevel(1.0F).setStepSound(SoundType.STONE).setHardness(2.0F).setBlockTextureName(RefStrings.MODID + ":basalt");;

		nuke_gadget = new NukeGadget(Material.IRON).setTranslationKey("nuke_gadget").setCreativeTab(MainRegistry.nukeTab).setHardness(5.0F).setResistance(200.0F).setBlockTextureName(RefStrings.MODID + ":theGadget");
		nuke_boy = new NukeBoy(Material.IRON).setTranslationKey("nuke_boy").setCreativeTab(MainRegistry.nukeTab).setHardness(5.0F).setResistance(200.0F).setBlockTextureName(RefStrings.MODID + ":lilBoy");
		nuke_man = new NukeMan(Material.IRON).setTranslationKey("nuke_man").setCreativeTab(MainRegistry.nukeTab).setHardness(5.0F).setResistance(200.0F).setBlockTextureName(RefStrings.MODID + ":fatMan");
		nuke_mike = new NukeMike(Material.IRON).setTranslationKey("nuke_mike").setCreativeTab(MainRegistry.nukeTab).setHardness(5.0F).setResistance(200.0F).setBlockTextureName(RefStrings.MODID + ":ivyMike");
		nuke_tsar = new NukeTsar(Material.IRON).setTranslationKey("nuke_tsar").setCreativeTab(MainRegistry.nukeTab).setHardness(5.0F).setResistance(200.0F).setBlockTextureName(RefStrings.MODID + ":tsarBomba");
		nuke_fleija = new NukeFleija(Material.IRON).setTranslationKey("nuke_fleija").setCreativeTab(MainRegistry.nukeTab).setHardness(5.0F).setResistance(200.0F).setBlockTextureName(RefStrings.MODID + ":fleija");
		nuke_prototype = new NukePrototype(Material.IRON).setTranslationKey("nuke_prototype").setCreativeTab(MainRegistry.nukeTab).setHardness(5.0F).setResistance(200).setBlockTextureName(RefStrings.MODID + ":prototype");
		nuke_custom = new NukeCustom(Material.IRON).setTranslationKey("nuke_custom").setCreativeTab(MainRegistry.nukeTab).setHardness(5.0F).setResistance(200.0F).setBlockTextureName(RefStrings.MODID + ":custom");
		nuke_solinium = new NukeSolinium(Material.IRON).setTranslationKey("nuke_solinium").setCreativeTab(MainRegistry.nukeTab).setHardness(5.0F).setResistance(200.0F).setBlockTextureName(RefStrings.MODID + ":nuke_solinium");
		nuke_n2 = new NukeN2(Material.IRON).setTranslationKey("nuke_n2").setCreativeTab(MainRegistry.nukeTab).setHardness(5.0F).setResistance(200.0F).setBlockTextureName(RefStrings.MODID + ":nuke_n2");
		nuke_fstbmb = new NukeBalefire(Material.IRON).setTranslationKey("nuke_fstbmb").setCreativeTab(MainRegistry.nukeTab).setHardness(5.0F).setResistance(200.0F).setBlockTextureName(RefStrings.MODID + ":nuke_fstbmb");
		nuke_antimatter = new NukeAntimatter(Material.IRON).setTranslationKey("nuke_antimatter").setCreativeTab(MainRegistry.nukeTab).setHardness(5.0F).setResistance(6000.0F).setBlockTextureName(RefStrings.MODID + ":theGadget");

		bomb_multi = new BombMulti(Material.IRON).setTranslationKey("bomb_multi").setCreativeTab(MainRegistry.nukeTab).setResistance(200.0F).setBlockTextureName(RefStrings.MODID + ":block_steel");

		flame_war = new BombFlameWar(Material.IRON).setTranslationKey("flame_war").setCreativeTab(MainRegistry.nukeTab).setHardness(5.0F).setResistance(200.0F).setBlockTextureName(RefStrings.MODID + ":flame_war");
		float_bomb = new BombFloat(Material.IRON).setTranslationKey("float_bomb").setCreativeTab(MainRegistry.nukeTab).setHardness(5.0F).setResistance(200.0F);
		therm_endo = new BombThermo(Material.IRON).setTranslationKey("therm_endo").setCreativeTab(MainRegistry.nukeTab).setHardness(5.0F).setResistance(200.0F);
		therm_exo = new BombThermo(Material.IRON).setTranslationKey("therm_exo").setCreativeTab(MainRegistry.nukeTab).setHardness(5.0F).setResistance(200.0F);
		emp_bomb = new BombFloat(Material.IRON).setTranslationKey("emp_bomb").setCreativeTab(MainRegistry.nukeTab).setHardness(5.0F).setResistance(200.0F);
		det_cord = new DetCord(Material.IRON).setBlockName("det_cord").setCreativeTab(MainRegistry.nukeTab).setHardness(0.1F).setResistance(0.0F);
		det_charge = ((BlockBase) new ExplosiveCharge(Material.IRON).setBlockName("det_charge").setCreativeTab(MainRegistry.nukeTab).setHardness(0.1F).setResistance(0.0F)).setBlockTextureName(RefStrings.MODID + ":det_charge");
		det_nuke = ((BlockBase) new ExplosiveCharge(Material.IRON).setBlockName("det_nuke").setCreativeTab(MainRegistry.nukeTab).setHardness(0.1F).setResistance(0.0F)).setBlockTextureName(RefStrings.MODID + ":det_nuke");
		det_salt = ((BlockBase) new ExplosiveCharge(Material.IRON).setBlockName("det_salt").setCreativeTab(MainRegistry.nukeTab).setHardness(0.1F).setResistance(0.0F)).setBlockTextureName(RefStrings.MODID + ":det_nuke");
		det_miner = new DetMiner(Material.IRON, RefStrings.MODID + ":det_miner_top").setTranslationKey("det_miner").setCreativeTab(MainRegistry.nukeTab).setHardness(0.1F).setResistance(0.0F).setBlockTextureName(RefStrings.MODID + ":det_miner_side");
		red_barrel = new RedBarrel(Material.IRON, true).setTranslationKey("red_barrel").setCreativeTab(MainRegistry.nukeTab).setHardness(0.5F).setResistance(2.5F);
		pink_barrel = new RedBarrel(Material.IRON, true).setTranslationKey("pink_barrel").setCreativeTab(MainRegistry.nukeTab).setHardness(0.5F).setResistance(2.5F);
		yellow_barrel = new YellowBarrel(Material.IRON).setTranslationKey("yellow_barrel").setCreativeTab(MainRegistry.nukeTab).setHardness(0.5F).setResistance(2.5F);
		oil_barrel = new BrownBarrel(Material.IRON).setTranslationKey("oil_barrel").setCreativeTab(MainRegistry.nukeTab).setHardness(0.5F).setResistance(2.5F);
		vitrified_barrel = new YellowBarrel(Material.IRON).setTranslationKey("vitrified_barrel").setCreativeTab(MainRegistry.nukeTab).setHardness(0.5F).setResistance(2.5F);
		lox_barrel = new RedBarrel(Material.IRON, false).setTranslationKey("lox_barrel").setCreativeTab(MainRegistry.nukeTab).setHardness(0.5F).setResistance(2.5F);
		taint_barrel = new RedBarrel(Material.IRON, false).setTranslationKey("taint_barrel").setCreativeTab(MainRegistry.nukeTab).setHardness(0.5F).setResistance(2.5F);
		crashed_balefire = ((BlockBase) new BlockCrashedBomb(Material.IRON).setBlockName("crashed_bomb").setCreativeTab(MainRegistry.nukeTab).setBlockUnbreakable().setResistance(6000.0F)).setBlockTextureName(RefStrings.MODID + ":block_rust");
		fireworks = new BlockFireworks(Material.IRON).setTranslationKey("fireworks").setCreativeTab(MainRegistry.nukeTab).setResistance(5.0F);
		charge_dynamite = new BlockChargeDynamite().setBlockName("charge_dynamite").setCreativeTab(MainRegistry.nukeTab).setResistance(1.0F);
		charge_miner = new BlockChargeMiner().setBlockName("charge_miner").setCreativeTab(MainRegistry.nukeTab).setResistance(1.0F);
		charge_c4 = new BlockChargeC4().setBlockName("charge_c4").setCreativeTab(MainRegistry.nukeTab).setResistance(1.0F);
		charge_semtex = new BlockChargeSemtex().setBlockName("charge_semtex").setCreativeTab(MainRegistry.nukeTab).setResistance(1.0F);
		mine_ap = new Landmine(Material.IRON, 1.5D, 1D).setTranslationKey("mine_ap").setCreativeTab(MainRegistry.nukeTab).setHardness(1.0F).setBlockTextureName(RefStrings.MODID + ":mine_ap");
		mine_he = new Landmine(Material.IRON, 2D, 5D).setTranslationKey("mine_he").setCreativeTab(MainRegistry.nukeTab).setHardness(1.0F).setBlockTextureName(RefStrings.MODID + ":mine_he");
		mine_shrap = new Landmine(Material.IRON, 1.5D, 1D).setTranslationKey("mine_shrap").setCreativeTab(MainRegistry.nukeTab).setHardness(1.0F).setBlockTextureName(RefStrings.MODID + ":mine_shrap");
		mine_fat = new Landmine(Material.IRON, 2.5D, 1D).setTranslationKey("mine_fat").setCreativeTab(MainRegistry.nukeTab).setHardness(1.0F).setBlockTextureName(RefStrings.MODID + ":mine_fat");
		mine_naval = new Landmine(Material.IRON, 2.5D, 1D).setTranslationKey("mine_naval").setCreativeTab(MainRegistry.nukeTab).setHardness(1.0F).setBlockTextureName(RefStrings.MODID + ":mine_naval");
		dynamite = ((BlockBase) ((BlockBase) new BlockDynamite().setBlockName("dynamite")).setSoundType(SoundType.PLANT).setCreativeTab(MainRegistry.nukeTab).setHardness(0.0F)).setBlockTextureName(RefStrings.MODID + ":dynamite");
		tnt = ((BlockBase) ((BlockBase) new BlockTNT().setBlockName("tnt_ntm")).setSoundType(SoundType.PLANT).setCreativeTab(MainRegistry.nukeTab).setHardness(0.0F)).setBlockTextureName(RefStrings.MODID + ":tnt");
		semtex = ((BlockBase) ((BlockBase) new BlockSemtex().setBlockName("semtex")).setSoundType(SoundType.PLANT).setCreativeTab(MainRegistry.nukeTab).setHardness(0.0F)).setBlockTextureName(RefStrings.MODID + ":semtex");
		c4 = ((BlockBase) ((BlockBase) new BlockC4().setBlockName("c4")).setSoundType(SoundType.PLANT).setCreativeTab(MainRegistry.nukeTab).setHardness(0.0F)).setBlockTextureName(RefStrings.MODID + ":c4");
		fissure_bomb = ((BlockBase) ((BlockBase) new BlockFissureBomb().setBlockName("fissure_bomb")).setSoundType(SoundType.PLANT).setCreativeTab(MainRegistry.nukeTab).setHardness(0.0F)).setBlockTextureName(RefStrings.MODID + ":fissure_bomb");

		pump_steam = new MachinePump().setTranslationKey("pump_steam").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_copper");
		pump_electric = new MachinePump().setTranslationKey("pump_electric").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");

		heater_firebox = new HeaterFirebox().setTranslationKey("heater_firebox").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		heater_oven = new HeaterOven().setTranslationKey("heater_oven").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":brick_fire");
		heater_oilburner = new HeaterOilburner().setTranslationKey("heater_oilburner").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		heater_electric = new HeaterElectric().setTranslationKey("heater_electric").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		heater_heatex = new HeaterHeatex().setTranslationKey("heater_heatex").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_ashpit = new MachineAshpit().setTranslationKey("machine_ashpit").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName("stonebrick");

		furnace = new BlockFurnaceSpace(false).setHardness(3.5F).setStepSound(SoundType.STONE).setTranslationKey("furnace");
		lit_furnace = new BlockFurnaceSpace(true).setHardness(3.5F).setStepSound(SoundType.STONE).setTranslationKey("lit_furnace").setLightLevel(0.875F);

		furnace_iron = new FurnaceIron().setTranslationKey("furnace_iron").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_aluminium");
		furnace_steel = new FurnaceSteel().setTranslationKey("furnace_steel").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		furnace_combination = new FurnaceCombination().setTranslationKey("furnace_combination").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":brick_light_alt");
		machine_stirling = new MachineStirling().setTranslationKey("machine_stirling").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_stirling_steel = new MachineStirling().setTranslationKey("machine_stirling_steel").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_stirling_creative = new MachineStirling().setTranslationKey("machine_stirling_creative").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_sawmill = new MachineSawmill().setTranslationKey("machine_sawmill").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_strand_caster = new MachineStrandCaster().setTranslationKey("machine_strand_caster").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":brick_fire");
		machine_crucible = new MachineCrucible().setTranslationKey("machine_crucible").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":brick_fire");
		machine_boiler = new MachineHeatBoiler().setTranslationKey("machine_boiler").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_copper");
		machine_industrial_boiler = new MachineHeatBoilerIndustrial().setTranslationKey("machine_industrial_boiler").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");

		foundry_mold = new FoundryMold().setTranslationKey("foundry_mold").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		foundry_basin = new FoundryBasin().setTranslationKey("foundry_basin").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		foundry_channel = new FoundryChannel().setTranslationKey("foundry_channel").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		foundry_tank = new FoundryTank().setTranslationKey("foundry_tank").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		foundry_outlet = new FoundryOutlet().setTranslationKey("foundry_outlet").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		foundry_slagtap = new FoundrySlagtap().setTranslationKey("foundry_slagtap").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		slag = new BlockDynamicSlag().setTranslationKey("slag").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":slag");

		machine_difurnace_off = new MachineDiFurnace(false).setTranslationKey("machine_difurnace_off").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		machine_difurnace_on = new MachineDiFurnace(true).setTranslationKey("machine_difurnace_on").setHardness(5.0F).setLightLevel(1.0F).setResistance(10.0F);
		machine_difurnace_extension = new MachineDiFurnaceExtension().setTranslationKey("machine_difurnace_extension").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		machine_difurnace_rtg_off = new MachineDiFurnaceRTG(false).setTranslationKey("machine_difurnace_rtg_off").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		machine_difurnace_rtg_on = new MachineDiFurnaceRTG(true).setTranslationKey("machine_difurnace_rtg_on").setHardness(5.0F).setResistance(10.0F).setLightLevel(1.0F).setCreativeTab(null);

		machine_centrifuge = new MachineCentrifuge(Material.IRON).setTranslationKey("machine_centrifuge").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_gascent = new MachineGasCent(Material.IRON).setTranslationKey("machine_gascent").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_fel = new MachineFEL(Material.IRON).setTranslationKey("machine_fel").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_silex = new MachineSILEX(Material.IRON).setTranslationKey("machine_silex").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_rotary_furnace = new MachineRotaryFurnace(Material.IRON).setTranslationKey("machine_rotary_furnace").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":brick_fire");
		machine_crystallizer = new MachineCrystallizer(Material.IRON).setTranslationKey("machine_crystallizer").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":machine_crystallizer");

		machine_uf6_tank = new MachineUF6Tank(Material.IRON).setTranslationKey("machine_uf6_tank").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null);
		machine_puf6_tank = new MachinePuF6Tank(Material.IRON).setTranslationKey("machine_puf6_tank").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null);

		machine_reactor_breeding = new MachineReactorBreeding(Material.IRON).setTranslationKey("machine_reactor").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":machine_reactor");

		machine_furnace_brick_off = new MachineBrickFurnace(false).setTranslationKey("machine_furnace_brick_off").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		machine_furnace_brick_on = new MachineBrickFurnace(true).setTranslationKey("machine_furnace_brick_on").setHardness(5.0F).setLightLevel(1.0F).setResistance(10.0F);
		machine_rtg_furnace_off = new MachineRtgFurnace(false).setTranslationKey("machine_rtg_furnace_off").setHardness(5.0F).setResistance(10.0F);
		machine_rtg_furnace_on = new MachineRtgFurnace(true).setTranslationKey("machine_rtg_furnace_on").setHardness(5.0F).setLightLevel(1.0F).setResistance(10.0F);

		machine_industrial_generator = new MachineIGenerator(Material.IRON).setTranslationKey("machine_industrial_generator").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName("gold_block");
		machine_cyclotron = new MachineCyclotron(Material.IRON).setTranslationKey("machine_cyclotron").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":cyclotron");
		machine_exposure_chamber = new MachineExposureChamber(Material.IRON).setTranslationKey("machine_exposure_chamber").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_aluminium");
		machine_radgen = new MachineRadGen(Material.IRON).setTranslationKey("machine_radgen").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":machine_radgen");

		hadron_coil_alloy = new BlockHadronCoil(Material.IRON, 10).setSoundType(ModSoundTypes.legacyMetal).setTranslationKey("hadron_coil_alloy").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		hadron_coil_gold = new BlockHadronCoil(Material.IRON, 25).setSoundType(ModSoundTypes.legacyMetal).setTranslationKey("hadron_coil_gold").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		hadron_coil_neodymium = new BlockHadronCoil(Material.IRON, 50).setSoundType(ModSoundTypes.legacyMetal).setTranslationKey("hadron_coil_neodymium").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		hadron_coil_magtung = new BlockHadronCoil(Material.IRON, 100).setSoundType(ModSoundTypes.legacyMetal).setTranslationKey("hadron_coil_magtung").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		hadron_coil_schrabidium = new BlockHadronCoil(Material.IRON, 250).setSoundType(ModSoundTypes.legacyMetal).setTranslationKey("hadron_coil_schrabidium").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		hadron_coil_schrabidate = new BlockHadronCoil(Material.IRON, 500).setSoundType(ModSoundTypes.legacyMetal).setTranslationKey("hadron_coil_schrabidate").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		hadron_coil_starmetal = new BlockHadronCoil(Material.IRON, 1000).setSoundType(ModSoundTypes.legacyMetal).setTranslationKey("hadron_coil_starmetal").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		hadron_coil_chlorophyte = new BlockHadronCoil(Material.IRON, 2500).setSoundType(ModSoundTypes.legacyMetal).setTranslationKey("hadron_coil_chlorophyte").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		hadron_coil_mese = new BlockHadronCoil(Material.IRON, 10000).setSoundType(ModSoundTypes.legacyMetal).setTranslationKey("hadron_coil_mese").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);

		pa_source = new BlockPASource().setStepSound(ModSoundTypes.legacyMetal).setTranslationKey("pa_source").setHardness(5.0F).setResistance(10.0F);
		pa_beamline = new BlockPABeamline().setStepSound(ModSoundTypes.legacyMetal).setTranslationKey("pa_beamline").setHardness(5.0F).setResistance(10.0F);
		pa_rfc = new BlockPARFC().setStepSound(ModSoundTypes.legacyMetal).setTranslationKey("pa_rfc").setHardness(5.0F).setResistance(10.0F);
		pa_quadrupole = new BlockPAQuadrupole().setStepSound(ModSoundTypes.legacyMetal).setTranslationKey("pa_quadrupole").setHardness(5.0F).setResistance(10.0F);
		pa_dipole = new BlockPADipole().setStepSound(ModSoundTypes.legacyMetal).setTranslationKey("pa_dipole").setHardness(5.0F).setResistance(10.0F);
		pa_detector = new BlockPADetector().setStepSound(ModSoundTypes.legacyMetal).setTranslationKey("pa_detector").setHardness(5.0F).setResistance(10.0F);

		machine_electric_furnace_off = new MachineElectricFurnace(false).setTranslationKey("machine_electric_furnace_off").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		machine_electric_furnace_on = new MachineElectricFurnace(true).setTranslationKey("machine_electric_furnace_on").setHardness(5.0F).setLightLevel(1.0F).setResistance(10.0F);
		machine_arc_furnace = new MachineArcFurnaceLarge().setTranslationKey("machine_arc_furnace").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_microwave = new MachineMicrowave(Material.IRON).setTranslationKey("machine_microwave").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":machine_microwave");

		machine_battery_socket = new MachineBatterySocket().setTranslationKey("machine_battery_socket").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_battery_redd = new MachineBatteryREDD().setTranslationKey("machine_battery_redd").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");

		machine_battery_potato = new MachineBattery(Material.IRON, 10_000).setTranslationKey("machine_battery_potato").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null);
		machine_battery = new MachineBattery(Material.IRON, 1_000_000).setTranslationKey("machine_battery").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null);
		machine_lithium_battery = new MachineBattery(Material.IRON, 50_000_000).setTranslationKey("machine_lithium_battery").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null);
		machine_schrabidium_battery = new MachineBattery(Material.IRON, 25_000_000_000L).setTranslationKey("machine_schrabidium_battery").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null);
		machine_dineutronium_battery = new MachineBattery(Material.IRON, 1_000_000_000_000L).setTranslationKey("machine_dineutronium_battery").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null);
		machine_fensu = new MachineFENSU(Material.IRON).setTranslationKey("machine_fensu").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":machine_fensu");

		capacitor_bus = new MachineCapacitorBus(Material.IRON).setTranslationKey("capacitor_bus").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null);
		capacitor_copper = new MachineCapacitor(Material.IRON, 1_000_000L, "copper").setTranslationKey("capacitor_copper").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		capacitor_gold = new MachineCapacitor(Material.IRON, 5_000_000L, "gold").setTranslationKey("capacitor_gold").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null);
		capacitor_niobium = new MachineCapacitor(Material.IRON, 25_000_000L, "niobium").setTranslationKey("capacitor_niobium").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null);
		capacitor_complex = new MachineCapacitor(Material.IRON, 100_000_000_000_000_000L, "complex").setTranslationKey("capacitor_complex").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);

		capacitor_tantalium = new MachineCapacitor(Material.IRON, 150_000_000L, "tantalium").setTranslationKey("capacitor_tantalium").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null);
		capacitor_schrabidate = new MachineCapacitor(Material.IRON, 50_000_000_000L, "schrabidate").setTranslationKey("capacitor_schrabidate").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null);

		machine_wood_burner = new MachineWoodBurner(Material.IRON).setTranslationKey("machine_wood_burner").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_diesel = new MachineDiesel().setTranslationKey("machine_diesel").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_combustion_engine = new MachineCombustionEngine().setTranslationKey("machine_combustion_engine").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");

		machine_shredder = new MachineShredder(Material.IRON).setTranslationKey("machine_shredder").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);

		machine_teleporter = new MachineTeleporter(Material.IRON).setTranslationKey("machine_teleporter").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		teleanchor = new MachineTeleanchor().setBlockName("teleanchor").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		field_disturber = new MachineFieldDisturber().setTranslationKey("field_disturber").setHardness(5.0F).setResistance(200.0F).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":field_disturber");
		troll_disturber = new MachineTrollDisturber(10000000F).setTranslationKey("troll_disturber").setHardness(5.0F).setResistance(200.0F).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":field_disturber");


		machine_rtg_grey = new MachineRTG(Material.IRON).setTranslationKey("machine_rtg_grey").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		machine_minirtg = new MachineMiniRTG(Material.IRON).setTranslationKey("machine_minirtg").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		machine_powerrtg = new MachineMiniRTG(Material.IRON).setTranslationKey("machine_powerrtg").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		machine_radiolysis = new MachineRadiolysis(Material.IRON).setTranslationKey("machine_radiolysis").setHardness(10.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel_machine");
		machine_hephaestus = new MachineHephaestus(Material.IRON).setTranslationKey("machine_hephaestus").setHardness(10.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel_machine");
		machine_solar = new MachineSolar(Material.IRON).setTranslationKey("machine_solar").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel_machine");
		machine_stardar = new MachineStardar(Material.IRON).setTranslationKey("machine_stardar").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel_machine");
		machine_drive_processor = new MachineDriveProcessor(Material.IRON).setTranslationKey("machine_drive_processor").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel_machine");
		machine_alkylation = new MachineAlkylation(Material.IRON).setTranslationKey("machine_alkylation").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel_machine");

		red_wire_coated = new WireCoated(Material.IRON).setTranslationKey("red_wire_coated").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		red_cable = new BlockCable(Material.IRON).setTranslationKey("red_cable").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		red_cable_classic = new BlockCable(Material.IRON).setTranslationKey("red_cable_classic").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		red_cable_paintable = new BlockCablePaintable().setTranslationKey("red_cable_paintable").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		red_cable_gauge = new BlockCableGauge().setTranslationKey("red_cable_gauge").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		red_cable_box = new PowerCableBox(Material.IRON).setTranslationKey("red_cable_box").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		red_connector = new ConnectorRedWire(Material.IRON).setTranslationKey("red_connector").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":red_connector");
		red_connector_super = new ConnectorRedWireSuper(Material.IRON).setTranslationKey("red_connector_super").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":red_connector");
		red_pylon = new PylonRedWire(Material.IRON).setTranslationKey("red_pylon").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":red_pylon");
		red_pylon_medium_wood = new PylonMedium(Material.WOOD).setTranslationKey("red_pylon_medium_wood").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":red_pylon");
		red_pylon_medium_wood_transformer = new PylonMedium(Material.WOOD).setTranslationKey("red_pylon_medium_wood_transformer").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":red_pylon");
		red_pylon_medium_steel = new PylonMedium(Material.IRON).setTranslationKey("red_pylon_medium_steel").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":red_pylon");
		red_pylon_medium_steel_transformer = new PylonMedium(Material.IRON).setTranslationKey("red_pylon_medium_steel_transformer").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":red_pylon");
		red_pylon_large = new PylonLarge(Material.IRON).setTranslationKey("red_pylon_large").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":red_pylon_large");
		substation = new Substation(Material.IRON).setTranslationKey("substation").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":substation");
		cable_switch = new CableSwitch(Material.IRON).setTranslationKey("cable_switch").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		cable_detector = new CableDetector(Material.IRON).setTranslationKey("cable_detector").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		cable_diode = new CableDiode(Material.IRON).setTranslationKey("cable_diode").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		machine_detector = new PowerDetector(Material.IRON).setTranslationKey("machine_detector").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":machine_detector_off");
		fluid_duct_neo = new FluidDuctStandard(Material.IRON).setTranslationKey("fluid_duct_neo").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		fluid_duct_box = new FluidDuctBox(Material.IRON).setTranslationKey("fluid_duct_box").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		fluid_duct_exhaust = new FluidDuctBoxExhaust(Material.IRON).setTranslationKey("fluid_duct_exhaust").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		fluid_duct_paintable_block_exhaust = new FluidDuctPaintableBlockExhaust().setTranslationKey("fluid_duct_paintable_block_exhaust").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		pipe_anchor = new FluidPipeAnchor().setTranslationKey("pipe_anchor").setStepSound(ModSoundTypes.pipe).setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		fluid_duct_paintable = new FluidDuctPaintable().setTranslationKey("fluid_duct_paintable").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		fluid_duct_gauge = new FluidDuctGauge().setTranslationKey("fluid_duct_gauge").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		fluid_valve = new FluidValve(Material.IRON).setTranslationKey("fluid_valve").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		fluid_switch = new FluidSwitch(Material.IRON).setTranslationKey("fluid_switch").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		fluid_counter_valve = new FluidCounterValve(Material.IRON).setTranslationKey("fluid_counter_valve").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		fluid_pump = new FluidPump(Material.IRON).setTranslationKey("fluid_pump").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_drain = new MachineDrain(Material.IRON).setTranslationKey("machine_drain").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":concrete");
		radio_torch_sender = new RadioTorchSender().setBlockName("radio_torch_sender").setHardness(0.1F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		radio_torch_receiver = new RadioTorchReceiver().setBlockName("radio_torch_receiver").setHardness(0.1F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		radio_torch_counter = new RadioTorchCounter().setBlockName("radio_torch_counter").setHardness(0.1F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		radio_torch_logic = new RadioTorchLogic().setBlockName("radio_torch_logic").setHardness(0.1F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		radio_torch_reader = new RadioTorchReader().setBlockName("radio_torch_reader").setHardness(0.1F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		radio_torch_controller = new RadioTorchController().setBlockName("radio_torch_controller").setHardness(0.1F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		radio_telex = new RadioTelex().setTranslationKey("radio_telex").setHardness(3F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":radio_telex");

		conveyor = new BlockConveyor().setTranslationKey("conveyor").setHardness(2.0F).setResistance(2.0F).setCreativeTab(null);
		conveyor_express = new BlockConveyorExpress().setTranslationKey("conveyor_express").setHardness(2.0F).setResistance(2.0F).setCreativeTab(null);
		conveyor_double = new BlockConveyorDouble().setTranslationKey("conveyor_double").setHardness(2.0F).setResistance(2.0F).setCreativeTab(null);
		conveyor_triple = new BlockConveyorTriple().setTranslationKey("conveyor_triple").setHardness(2.0F).setResistance(2.0F).setCreativeTab(null);
		conveyor_chute = new BlockConveyorChute().setTranslationKey("conveyor_chute").setHardness(2.0F).setResistance(2.0F).setCreativeTab(null);
		conveyor_lift = new BlockConveyorLift().setTranslationKey("conveyor_lift").setHardness(2.0F).setResistance(2.0F).setCreativeTab(null);
		crane_extractor = new CraneExtractor().setTranslationKey("crane_extractor").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		crane_inserter = new CraneInserter().setTranslationKey("crane_inserter").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		crane_grabber = new CraneGrabber().setTranslationKey("crane_grabber").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		crane_router = new CraneRouter().setTranslationKey("crane_router").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		crane_boxer = new CraneBoxer().setTranslationKey("crane_boxer").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		crane_unboxer = new CraneUnboxer().setTranslationKey("crane_unboxer").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		crane_splitter = new CraneSplitter().setTranslationKey("crane_splitter").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		crane_partitioner = new CranePartitioner().setTranslationKey("crane_partitioner").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		fan = new MachineFan().setTranslationKey("fan").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		piston_inserter = ((BlockBase) new PistonInserter().setBlockName("piston_inserter").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab)).setBlockTextureName(RefStrings.MODID + ":block_steel");

		drone_waypoint = new DroneWaypoint().setTranslationKey("drone_waypoint").setHardness(0.1F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":drone_waypoint");
		drone_crate = new DroneCrate().setTranslationKey("drone_crate").setHardness(0.1F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		drone_waypoint_request = new DroneWaypointRequest().setTranslationKey("drone_waypoint_request").setHardness(0.1F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":drone_waypoint_request");
		drone_dock = new DroneDock().setTranslationKey("drone_dock").setHardness(0.1F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":drone_dock");
		drone_crate_provider = new DroneDock().setTranslationKey("drone_crate_provider").setHardness(0.1F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":drone_crate_provider");
		drone_crate_requester = new DroneDock().setTranslationKey("drone_crate_requester").setHardness(0.1F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":drone_crate_requester");

		pneumatic_tube = new PneumoTube().setTranslationKey("pneumatic_tube").setSoundType(ModSoundTypes.pipe).setHardness(0.1F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		pneumatic_tube_paintable = new PneumoTubePaintableBlock().setTranslationKey("pneumatic_tube_paintable").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);

		chain = new BlockChain(Material.IRON).setTranslationKey("dungeon_chain").setHardness(0.25F).setResistance(2.0F).setCreativeTab(MainRegistry.blockTab);

		ladder_sturdy = new BlockNTMLadder().setTranslationKey("ladder_sturdy").setHardness(0.25F).setResistance(2.0F).setCreativeTab(MainRegistry.blockTab).setBlockTextureName(RefStrings.MODID + ":ladder_sturdy");
		ladder_iron = new BlockNTMLadder().setTranslationKey("ladder_iron").setHardness(0.25F).setResistance(2.0F).setCreativeTab(MainRegistry.blockTab).setBlockTextureName(RefStrings.MODID + ":ladder_iron");
		ladder_gold = new BlockNTMLadder().setTranslationKey("ladder_gold").setHardness(0.25F).setResistance(2.0F).setCreativeTab(MainRegistry.blockTab).setBlockTextureName(RefStrings.MODID + ":ladder_gold");
		ladder_aluminium = new BlockNTMLadder().setTranslationKey("ladder_aluminium").setHardness(0.25F).setResistance(2.0F).setCreativeTab(MainRegistry.blockTab).setBlockTextureName(RefStrings.MODID + ":ladder_aluminium");
		ladder_copper = new BlockNTMLadder().setTranslationKey("ladder_copper").setHardness(0.25F).setResistance(2.0F).setCreativeTab(MainRegistry.blockTab).setBlockTextureName(RefStrings.MODID + ":ladder_copper");
		ladder_titanium = new BlockNTMLadder().setTranslationKey("ladder_titanium").setHardness(0.25F).setResistance(2.0F).setCreativeTab(MainRegistry.blockTab).setBlockTextureName(RefStrings.MODID + ":ladder_titanium");
		ladder_lead = new BlockNTMLadder().setTranslationKey("ladder_lead").setHardness(0.25F).setResistance(2.0F).setCreativeTab(MainRegistry.blockTab).setBlockTextureName(RefStrings.MODID + ":ladder_lead");
		ladder_cobalt = new BlockNTMLadder().setTranslationKey("ladder_cobalt").setHardness(0.25F).setResistance(2.0F).setCreativeTab(MainRegistry.blockTab).setBlockTextureName(RefStrings.MODID + ":ladder_cobalt");
		ladder_steel = new BlockNTMLadder().setTranslationKey("ladder_steel").setHardness(0.25F).setResistance(2.0F).setCreativeTab(MainRegistry.blockTab).setBlockTextureName(RefStrings.MODID + ":ladder_steel");
		ladder_tungsten = new BlockNTMLadder().setTranslationKey("ladder_tungsten").setHardness(0.25F).setResistance(2.0F).setCreativeTab(MainRegistry.blockTab).setBlockTextureName(RefStrings.MODID + ":ladder_tungsten");

		trapdoor_steel = new BlockNTMTrapdoor(Material.IRON).setTranslationKey("trapdoor_steel").setHardness(3F).setResistance(8.0F).setStepSound(ModSoundTypes.legacyMetal).setCreativeTab(MainRegistry.blockTab).setBlockTextureName(RefStrings.MODID + ":trapdoor_steel");

		barrel_plastic = new BlockFluidBarrel(Material.IRON, 12000, net.minecraft.block.SoundType.STONE).setTranslationKey("barrel_plastic").setHardness(2.0F).setResistance(5.0F).setCreativeTab(MainRegistry.machineTab);
		barrel_corroded = new BlockFluidBarrel(Material.IRON, 6000, ModSoundTypes.legacyMetal).setTranslationKey("barrel_corroded").setHardness(2.0F).setResistance(5.0F).setCreativeTab(null);
		barrel_iron = new BlockFluidBarrel(Material.IRON, 8000, ModSoundTypes.legacyMetal).setTranslationKey("barrel_iron").setHardness(2.0F).setResistance(5.0F).setCreativeTab(null);
		barrel_steel = new BlockFluidBarrel(Material.IRON, 16000, ModSoundTypes.legacyMetal).setTranslationKey("barrel_steel").setHardness(2.0F).setResistance(5.0F).setCreativeTab(MainRegistry.machineTab);
		barrel_tcalloy = new BlockFluidBarrel(Material.IRON, 24000, ModSoundTypes.legacyMetal).setTranslationKey("barrel_tcalloy").setHardness(2.0F).setResistance(5.0F).setCreativeTab(MainRegistry.machineTab);
		barrel_antimatter = new BlockFluidBarrel(Material.IRON, 16000, ModSoundTypes.legacyMetal).setTranslationKey("barrel_antimatter").setHardness(2.0F).setResistance(5.0F).setCreativeTab(MainRegistry.machineTab);

		machine_transformer = new MachineTransformer(Material.IRON).setTranslationKey("machine_transformer").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":machine_transformer_iron");

		machine_satlinker = new MachineSatLinker(Material.IRON).setTranslationKey("machine_satlinker").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.missileTab).setBlockTextureName(RefStrings.MODID + ":machine_satlinker_side");
		machine_keyforge = new MachineKeyForge(Material.IRON).setTranslationKey("machine_keyforge").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.consumableTab).setBlockTextureName(RefStrings.MODID + ":machine_keyforge_side");
		machine_armor_table = new BlockArmorTable(Material.IRON).setTranslationKey("machine_armor_table").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.consumableTab);
		machine_weapon_table = new BlockWeaponTable().setTranslationKey("machine_weapon_table").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.consumableTab);

		machine_solar_boiler = new MachineSolarBoiler(Material.IRON).setTranslationKey("machine_solar_boiler").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":machine_solar_boiler");
		solar_mirror = new SolarMirror(Material.IRON).setTranslationKey("solar_mirror").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);

		struct_launcher = ((BlockBase) new BlockGeneric(Material.IRON).setBlockName("struct_launcher").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.missileTab)).setBlockTextureName(RefStrings.MODID + ":struct_launcher");
		struct_scaffold = ((BlockBase) new BlockGeneric(Material.IRON).setBlockName("struct_scaffold").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.missileTab)).setBlockTextureName(RefStrings.MODID + ":struct_scaffold");
		struct_launcher_core = new BlockStruct(Material.IRON).setTranslationKey("struct_launcher_core").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.missileTab).setBlockTextureName(RefStrings.MODID + ":struct_launcher_core");
		struct_launcher_core_large = new BlockStruct(Material.IRON).setTranslationKey("struct_launcher_core_large").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.missileTab).setBlockTextureName(RefStrings.MODID + ":struct_launcher_core_large");
		struct_soyuz_core = new BlockSoyuzStruct(Material.IRON).setTranslationKey("struct_soyuz_core").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.missileTab).setBlockTextureName(RefStrings.MODID + ":struct_soyuz_core");
		struct_torus_core = new BlockFusionTorusStruct(Material.IRON).setTranslationKey("struct_torus_core").setLightLevel(1F).setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":struct_torus_core");
		struct_watz_core = new BlockWatzStruct(Material.IRON).setTranslationKey("struct_watz_core").setLightLevel(1F).setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":struct_watz_core");
		struct_icf_core = new BlockICFStruct(Material.IRON).setTranslationKey("struct_icf_core").setLightLevel(1F).setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":struct_icf_core");

		factory_titanium_hull = ((BlockBase) new BlockGeneric(Material.IRON).setBlockName("factory_titanium_hull").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab)).setBlockTextureName(RefStrings.MODID + ":factory_titanium_hull");
		factory_advanced_hull = ((BlockBase) new BlockGeneric(Material.IRON).setBlockName("factory_advanced_hull").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab)).setBlockTextureName(RefStrings.MODID + ":factory_advanced_hull");

		cm_block = ((BlockBase) new BlockCM(Material.IRON, EnumCMMaterials.class, true, true).setBlockName("cm_block").setCreativeTab(MainRegistry.machineTab).setHardness(5.0F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":cm_block");
		cm_sheet = ((BlockBase) new BlockCM(Material.IRON, EnumCMMaterials.class, true, true).setBlockName("cm_sheet").setCreativeTab(MainRegistry.machineTab).setHardness(5.0F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":cm_sheet");
		cm_engine = ((BlockBase) new BlockCM(Material.IRON, EnumCMEngines.class, true, true).setBlockName("cm_engine").setCreativeTab(MainRegistry.machineTab).setHardness(5.0F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":cm_engine");
		cm_tank = ((BlockBase) new BlockCMGlass(Material.IRON, EnumCMMaterials.class, true, true).setBlockName("cm_tank").setCreativeTab(MainRegistry.machineTab).setHardness(5.0F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":cm_tank");
		cm_circuit = ((BlockBase) new BlockCM(Material.IRON, EnumCMCircuit.class, true, true).setBlockName("cm_circuit").setCreativeTab(MainRegistry.machineTab).setHardness(5.0F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":cm_circuit");
		cm_port = ((BlockBase) new BlockCMPort(Material.IRON, EnumCMMaterials.class, true, true).setBlockName("cm_port").setCreativeTab(MainRegistry.machineTab).setHardness(5.0F).setResistance(10.0F)).setBlockTextureName(RefStrings.MODID + ":cm_port");
		cm_flux = new BlockCMFlux(Material.IRON, RefStrings.MODID + ":cm_flux_top").setTranslationKey("cm_flux").setCreativeTab(MainRegistry.machineTab).setHardness(5.0F).setResistance(10.0F).setBlockTextureName(RefStrings.MODID + ":cm_flux_side");
		cm_heat = new BlockCMHeat(Material.IRON, RefStrings.MODID +":cm_heat_top").setTranslationKey("cm_heat").setCreativeTab(MainRegistry.machineTab).setHardness(5.0F).setResistance(10.0F).setBlockTextureName(RefStrings.MODID + ":cm_heat_side");
		custom_machine = new BlockCustomMachine().setTranslationKey("custom_machine").setCreativeTab(MainRegistry.machineTab).setLightLevel(1F).setHardness(5.0F).setResistance(10.0F);
		cm_anchor = new BlockCMAnchor().setTranslationKey("custom_machine_anchor").setCreativeTab(MainRegistry.machineTab).setHardness(5.0F).setResistance(10.0F);

		pwr_fuel = ((BlockPillar) new BlockPillarPWR(Material.IRON, RefStrings.MODID + ":pwr_fuel_top").setTranslationKey("pwr_fuel").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab)).setBlockTextureName(RefStrings.MODID + ":pwr_fuel_side");
		pwr_control = ((BlockPillar) new BlockPillarPWR(Material.IRON, RefStrings.MODID + ":pwr_control_top").setTranslationKey("pwr_control").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab)).setBlockTextureName(RefStrings.MODID + ":pwr_control_side");
		pwr_channel = ((BlockPillar) new BlockPillarPWR(Material.IRON, RefStrings.MODID + ":pwr_channel_top").setTranslationKey("pwr_channel").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab)).setBlockTextureName(RefStrings.MODID + ":pwr_channel_side");
		pwr_heatex = ((BlockBase) new BlockGenericPWR(Material.IRON).setBlockName("pwr_heatex").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab)).setBlockTextureName(RefStrings.MODID + ":pwr_heatex");
		pwr_heatsink = ((BlockBase) new BlockGenericPWR(Material.IRON).setBlockName("pwr_heatsink").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab)).setBlockTextureName(RefStrings.MODID + ":pwr_heatsink");
		pwr_neutron_source = ((BlockBase) new BlockGenericPWR(Material.IRON).setBlockName("pwr_neutron_source").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab)).setBlockTextureName(RefStrings.MODID + ":pwr_neutron_source");
		pwr_reflector = ((BlockBase) new BlockGenericPWR(Material.IRON).setBlockName("pwr_reflector").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab)).setBlockTextureName(RefStrings.MODID + ":pwr_reflector");
		pwr_casing = ((BlockBase) new BlockGenericPWR(Material.IRON).setBlockName("pwr_casing").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab)).setBlockTextureName(RefStrings.MODID + ":pwr_casing");
		pwr_port = ((BlockBase) new BlockGenericPWR(Material.IRON).setBlockName("pwr_port").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab)).setBlockTextureName(RefStrings.MODID + ":pwr_port");
		pwr_controller = new MachinePWRController(Material.IRON).setTranslationKey("pwr_controller").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":pwr_casing_blank");
		pwr_block = new BlockPWR(Material.IRON).setTranslationKey("pwr_block").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null);

		fusion_heater = ((BlockPillar) new BlockPillar(Material.IRON, RefStrings.MODID + ":fusion_heater_top").setTranslationKey("fusion_heater").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null)).setBlockTextureName(RefStrings.MODID + ":fusion_heater_side");
		fusion_hatch = new FusionHatch(Material.IRON).setTranslationKey("fusion_hatch").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":fusion_hatch");
		plasma = new BlockPlasma(Material.IRON).setTranslationKey("plasma").setHardness(5.0F).setResistance(6000.0F).setLightLevel(1.0F).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":plasma");
		iter = new MachineITER().setTranslationKey("iter").setHardness(5.0F).setResistance(60.0F).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":iter");
		plasma_heater = new MachinePlasmaHeater().setTranslationKey("plasma_heater").setHardness(5.0F).setResistance(60.0F).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":plasma_heater");

		fusion_component = ((BlockBase) new BlockFusionComponent().setBlockName("fusion_component").setHardness(5.0F).setResistance(30.0F).setCreativeTab(MainRegistry.machineTab)).setBlockTextureName(RefStrings.MODID + ":fusion_component");
		fusion_torus = new MachineFusionTorus().setTranslationKey("fusion_torus").setHardness(5.0F).setResistance(60.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		fusion_klystron = new MachineFusionKlystron().setTranslationKey("fusion_klystron").setHardness(5.0F).setResistance(60.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		fusion_klystron_creative = new MachineFusionKlystronCreative().setTranslationKey("fusion_klystron_creative").setHardness(5.0F).setResistance(60.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		fusion_breeder = new MachineFusionBreeder().setTranslationKey("fusion_breeder").setHardness(5.0F).setResistance(60.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		fusion_collector = new MachineFusionCollector().setTranslationKey("fusion_collector").setHardness(5.0F).setResistance(60.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		fusion_boiler = new MachineFusionBoiler().setTranslationKey("fusion_boiler").setHardness(5.0F).setResistance(60.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		fusion_mhdt = new MachineFusionMHDT().setTranslationKey("fusion_mhdt").setHardness(5.0F).setResistance(60.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		fusion_coupler = new MachineFusionCoupler().setTranslationKey("fusion_coupler").setHardness(5.0F).setResistance(60.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");

		machine_icf_press = new MachineICFPress().setTranslationKey("machine_icf_press").setHardness(5.0F).setResistance(60.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		icf = new MachineICF().setTranslationKey("icf").setHardness(5.0F).setResistance(60.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		icf_component = ((BlockBase) new BlockICFComponent().setBlockName("icf_component").setHardness(5.0F).setResistance(60.0F).setCreativeTab(MainRegistry.machineTab)).setBlockTextureName(RefStrings.MODID + ":icf_component");
		icf_controller = new MachineICFController().setTranslationKey("icf_controller").setHardness(5.0F).setResistance(60.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":icf_casing");
		icf_laser_component = new BlockICFLaserComponent().setBlockName("icf_laser_component").setHardness(5.0F).setResistance(60.0F).setCreativeTab(MainRegistry.machineTab);
		icf_block = new BlockICF(Material.IRON).setTranslationKey("icf_block").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null);

		watz_element = ((BlockPillar) new BlockPillar(Material.IRON, RefStrings.MODID + ":watz_element_top").setTranslationKey("watz_element").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab)).setBlockTextureName(RefStrings.MODID + ":watz_element_side");
		watz_cooler = ((BlockPillar) new BlockPillar(Material.IRON, RefStrings.MODID + ":watz_cooler_top").setTranslationKey("watz_cooler").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab)).setBlockTextureName(RefStrings.MODID + ":watz_cooler_side");
		watz_end = ((BlockBase) new BlockToolConversion(Material.IRON).addVariant("_bolted").setBlockName("watz_end").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab)).setBlockTextureName(RefStrings.MODID + ":watz_casing");
		watz = new Watz().setTranslationKey("watz").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		watz_pump = new WatzPump().setTranslationKey("watz_pump").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");

		balefire = new Balefire().setTranslationKey("balefire").setHardness(0.0F).setLightLevel(1.0F).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":balefire");
		fire_digamma = new DigammaFlame().setTranslationKey("fire_digamma").setHardness(0.0F).setResistance(150F).setLightLevel(1.0F).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":fire_digamma");
		digamma_matter = new DigammaMatter().setTranslationKey("digamma_matter").setBlockUnbreakable().setResistance(18000000).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":digamma_matter");

		machine_converter_he_rf = new BlockConverterHeRf(Material.IRON).setTranslationKey("machine_converter_he_rf").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":machine_converter_he_rf");
		machine_converter_rf_he = new BlockConverterRfHe(Material.IRON).setTranslationKey("machine_converter_rf_he").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":machine_converter_rf_he");

		dfc_emitter = new CoreComponent(Material.IRON).setTranslationKey("dfc_emitter").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":dfc_emitter");
		dfc_injector = new CoreComponent(Material.IRON).setTranslationKey("dfc_injector").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":dfc_injector");
		dfc_receiver = new CoreComponent(Material.IRON).setTranslationKey("dfc_receiver").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":dfc_receiver");
		dfc_stabilizer = new CoreComponent(Material.IRON).setTranslationKey("dfc_stabilizer").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":dfc_stabilizer");
		dfc_core = new CoreCore(Material.IRON).setTranslationKey("dfc_core").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":dfc_core");

		seal_frame = ((BlockBase) new BlockGeneric(Material.IRON).setBlockName("seal_frame").setHardness(10.0F).setResistance(100.0F).setCreativeTab(MainRegistry.machineTab)).setBlockTextureName(RefStrings.MODID + ":seal_frame");
		seal_controller = new BlockSeal(Material.IRON).setTranslationKey("seal_controller").setHardness(10.0F).setResistance(100.0F).setCreativeTab(MainRegistry.machineTab);
		seal_hatch = new BlockHatch(Material.IRON).setTranslationKey("seal_hatch").setHardness(Float.POSITIVE_INFINITY).setResistance(Float.POSITIVE_INFINITY).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":seal_hatch_3");

		vault_door = new BlockDoorGeneric(Material.IRON, DoorDecl.VAULT_DOOR).setTranslationKey("vault_door").setHardness(10.0F).setResistance(1_000.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":concrete");
		blast_door = new BlastDoor(Material.IRON).setTranslationKey("blast_door").setHardness(10.0F).setResistance(1_000.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":blast_door");

		sliding_blast_door = new BlockDoorGeneric(Material.IRON, DoorDecl.SLIDE_DOOR).setTranslationKey("sliding_blast_door").setHardness(10.0F).setResistance(750.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":sliding_blast_door");
		fire_door = new BlockDoorGeneric(Material.IRON, DoorDecl.FIRE_DOOR).setTranslationKey("fire_door").setHardness(10.0F).setResistance(1_000.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":fire_door");
		transition_seal = new BlockDoorGeneric(Material.IRON, DoorDecl.TRANSITION_SEAL).setTranslationKey("transition_seal").setHardness(10.0F).setResistance(1_000.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":transition_seal");
		silo_hatch = new BlockDoorGeneric(Material.IRON, DoorDecl.SILO_HATCH).setTranslationKey("silo_hatch").setHardness(10.0F).setResistance(100.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		silo_hatch_large = new BlockDoorGeneric(Material.IRON, DoorDecl.SILO_HATCH_LARGE).setTranslationKey("silo_hatch_large").setHardness(10.0F).setResistance(100.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		secure_access_door = new BlockDoorGeneric(Material.IRON, DoorDecl.SECURE_ACCESS_DOOR).setTranslationKey("secure_access_door").setHardness(20.0F).setResistance(2_000.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		large_vehicle_door = new BlockDoorGeneric(Material.IRON, DoorDecl.LARGE_VEHICLE_DOOR).setTranslationKey("large_vehicle_door").setHardness(10.0F).setResistance(1_000.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		qe_containment = new BlockDoorGeneric(Material.IRON, DoorDecl.QE_CONTAINMENT).setTranslationKey("qe_containment").setHardness(10.0F).setResistance(1_000.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		qe_sliding_door = new BlockDoorGeneric(Material.IRON, DoorDecl.QE_SLIDING).setTranslationKey("qe_sliding_door").setHardness(10.0F).setResistance(1_000.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		round_airlock_door = new BlockDoorGeneric(Material.IRON, DoorDecl.ROUND_AIRLOCK_DOOR).setTranslationKey("round_airlock_door").setHardness(10.0F).setResistance(1_000.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		sliding_seal_door = new BlockDoorGeneric(Material.IRON, DoorDecl.SLIDING_SEAL_DOOR).setTranslationKey("sliding_seal_door").setHardness(10.0F).setResistance(1_000.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		water_door = new BlockDoorGeneric(Material.IRON, DoorDecl.WATER_DOOR).setTranslationKey("water_door").setHardness(5.0F).setResistance(50.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");

		door_metal = new BlockModDoor(Material.IRON).setTranslationKey("door_metal").setHardness(5.0F).setResistance(5.0F).setBlockTextureName(RefStrings.MODID + ":door_metal");
		door_office = new BlockModDoor(Material.IRON).setTranslationKey("door_office").setHardness(10.0F).setResistance(10.0F).setBlockTextureName(RefStrings.MODID + ":door_office");
		door_bunker = new BlockModDoor(Material.IRON).setTranslationKey("door_bunker").setHardness(10.0F).setResistance(100.0F).setBlockTextureName(RefStrings.MODID + ":door_bunker");
		door_red = new BlockModDoor(Material.IRON).setTranslationKey("door_red").setHardness(10.0F).setResistance(100.0F).setBlockTextureName(RefStrings.MODID + ":door_red");

		fence_gate = new BlockFenceGate(Material.IRON).setTranslationKey("fence_gate").setHardness(5.0F).setResistance(1.0F).setBlockTextureName(RefStrings.MODID + ":fence_metal");

		barbed_wire = createBarbedWire("barbed_wire");
		barbed_wire_fire = createBarbedWire("barbed_wire_fire");
		barbed_wire_poison = createBarbedWire("barbed_wire_poison");
		barbed_wire_acid = createBarbedWire("barbed_wire_acid");
		barbed_wire_wither = createBarbedWire("barbed_wire_wither");
		barbed_wire_ultradeath = createBarbedWire("barbed_wire_ultradeath");
		spikes = new Spikes(Material.IRON).setTranslationKey("spikes").setHardness(2.5F).setResistance(5.0F).setCreativeTab(MainRegistry.blockTab).setBlockTextureName(RefStrings.MODID + ":spikes");

		charger = new Charger(Material.IRON).setTranslationKey("charger").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.blockTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		refueler = new BlockRefueler(Material.IRON).setTranslationKey("refueler").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.blockTab).setBlockTextureName(RefStrings.MODID + ":block_steel");

		tesla = new MachineTesla(Material.IRON).setTranslationKey("tesla").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.blockTab).setBlockTextureName(RefStrings.MODID + ":tesla");

		launch_pad = new LaunchPad(Material.IRON).setTranslationKey("launch_pad").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.missileTab).setBlockTextureName(RefStrings.MODID + ":launch_pad");
		launch_pad_rocket = new LaunchPadRocket(Material.IRON).setTranslationKey("launch_pad_rocket").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.missileTab).setBlockTextureName(RefStrings.MODID + ":launch_pad");
		launch_pad_rusted = new LaunchPadRusted(Material.IRON).setTranslationKey("launch_pad_rusted").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.missileTab).setBlockTextureName(RefStrings.MODID + ":block_rust");
		launch_pad_large = new LaunchPadLarge(Material.IRON).setTranslationKey("launch_pad_large").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.missileTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_radar = new MachineRadar(Material.IRON).setTranslationKey("machine_radar").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.missileTab).setBlockTextureName(RefStrings.MODID + ":machine_radar");
		machine_radar_large = new MachineRadarLarge(Material.IRON).setTranslationKey("machine_radar_large").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.missileTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		radar_screen = new MachineRadarScreen(Material.IRON).setTranslationKey("radar_screen").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.missileTab).setBlockTextureName(RefStrings.MODID + ":block_steel");

		machine_missile_assembly = new MachineMissileAssembly(Material.IRON).setTranslationKey("machine_missile_assembly").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.missileTab).setBlockTextureName(RefStrings.MODID + ":machine_missile_assembly");
		machine_rocket_assembly = new MachineRocketAssembly(Material.IRON).setTranslationKey("machine_rocket_assembly").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.missileTab).setBlockTextureName(RefStrings.MODID + ":machine_missile_assembly");
		compact_launcher = new CompactLauncher(Material.IRON).setTranslationKey("compact_launcher").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.missileTab).setBlockTextureName(RefStrings.MODID + ":compact_launcher");
		launch_table = new LaunchTable(Material.IRON).setTranslationKey("launch_table").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.missileTab).setBlockTextureName(RefStrings.MODID + ":launch_table");
		soyuz_launcher = new SoyuzLauncher(Material.IRON).setTranslationKey("soyuz_launcher").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.missileTab).setBlockTextureName(RefStrings.MODID + ":soyuz_launcher");

		sat_dock = new MachineSatDock(Material.IRON).setTranslationKey("sat_dock").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.missileTab).setBlockTextureName(RefStrings.MODID + ":sat_dock");
		soyuz_capsule = new SoyuzCapsule(Material.IRON).setTranslationKey("soyuz_capsule").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.missileTab).setBlockTextureName(RefStrings.MODID + ":soyuz_capsule");
		gas_dock = new MachineGasDock(Material.IRON).setTranslationKey("gas_dock").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.missileTab).setBlockTextureName(RefStrings.MODID + ":sat_dock");
		crate_supply = new BlockSupplyCrate(Material.WOOD).setTranslationKey("crate_supply").setHardness(1.0F).setResistance(2.5F).setCreativeTab(MainRegistry.missileTab);
		combat_drop = new CombatDropPod(Material.IRON).setTranslationKey("combat_drop").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.missileTab).setBlockTextureName(RefStrings.MODID + ":crate_can");

		turret_chekhov = new TurretChekhov(Material.IRON).setTranslationKey("turret_chekhov").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.weaponTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		turret_friendly = new TurretFriendly(Material.IRON).setTranslationKey("turret_friendly").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.weaponTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		turret_jeremy = new TurretJeremy(Material.IRON).setTranslationKey("turret_jeremy").setHardness(5.0F).setResistance(600.0F).setCreativeTab(MainRegistry.weaponTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		turret_tauon = new TurretTauon(Material.IRON).setTranslationKey("turret_tauon").setHardness(5.0F).setResistance(60.0F).setCreativeTab(MainRegistry.weaponTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		turret_richard = new TurretRichard(Material.IRON).setTranslationKey("turret_richard").setHardness(5.0F).setResistance(600.0F).setCreativeTab(MainRegistry.weaponTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		turret_howard = new TurretHoward(Material.IRON).setTranslationKey("turret_howard").setHardness(5.0F).setResistance(60.0F).setCreativeTab(MainRegistry.weaponTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		turret_howard_damaged = new TurretHowardDamaged(Material.IRON).setTranslationKey("turret_howard_damaged").setHardness(5.0F).setResistance(600.0F).setCreativeTab(MainRegistry.weaponTab).setBlockTextureName(RefStrings.MODID + ":block_rust");
		turret_maxwell = new TurretMaxwell(Material.IRON).setTranslationKey("turret_maxwell").setHardness(5.0F).setResistance(60.0F).setCreativeTab(MainRegistry.weaponTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		turret_fritz = new TurretFritz(Material.IRON).setTranslationKey("turret_fritz").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.weaponTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		turret_arty = new TurretArty(Material.IRON).setTranslationKey("turret_arty").setHardness(5.0F).setResistance(600.0F).setCreativeTab(MainRegistry.weaponTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		turret_himars = new TurretHIMARS(Material.IRON).setTranslationKey("turret_himars").setHardness(5.0F).setResistance(600.0F).setCreativeTab(MainRegistry.weaponTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		turret_sentry = new TurretSentry().setTranslationKey("turret_sentry").setHardness(5.0F).setResistance(5.0F).setCreativeTab(MainRegistry.weaponTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		turret_sentry_damaged = new TurretSentryDamaged().setTranslationKey("turret_sentry_damaged").setHardness(5.0F).setResistance(5.0F).setCreativeTab(MainRegistry.weaponTab).setBlockTextureName(RefStrings.MODID + ":block_rust");

		rbmk_rod = new RBMKRod(false).setTranslationKey("rbmk_rod").setCreativeTab(MainRegistry.machineTab);
		rbmk_rod_mod = new RBMKRod(true).setTranslationKey("rbmk_rod_mod").setCreativeTab(MainRegistry.machineTab);
		rbmk_rod_reasim = new RBMKRodReaSim(false).setTranslationKey("rbmk_rod_reasim").setCreativeTab(MainRegistry.machineTab);
		rbmk_rod_reasim_mod = new RBMKRodReaSim(true).setTranslationKey("rbmk_rod_reasim_mod").setCreativeTab(MainRegistry.machineTab);
		rbmk_control = new RBMKControl(false).setTranslationKey("rbmk_control").setCreativeTab(MainRegistry.machineTab);
		rbmk_control_mod = new RBMKControl(true).setTranslationKey("rbmk_control_mod").setCreativeTab(MainRegistry.machineTab);
		rbmk_control_auto = new RBMKControlAuto().setTranslationKey("rbmk_control_auto").setCreativeTab(MainRegistry.machineTab);
		rbmk_control_reasim = new RBMKControl(false).setTranslationKey("rbmk_control_reasim").setCreativeTab(MainRegistry.machineTab);
		rbmk_control_reasim_auto = new RBMKControlAuto().setTranslationKey("rbmk_control_reasim_auto").setCreativeTab(MainRegistry.machineTab);
		rbmk_blank = new RBMKBlank().setTranslationKey("rbmk_blank").setCreativeTab(MainRegistry.machineTab);
		rbmk_boiler = new RBMKBoiler().setTranslationKey("rbmk_boiler").setCreativeTab(MainRegistry.machineTab);
		rbmk_reflector = new RBMKReflector().setTranslationKey("rbmk_reflector").setCreativeTab(MainRegistry.machineTab);
		rbmk_absorber = new RBMKAbsorber().setTranslationKey("rbmk_absorber").setCreativeTab(MainRegistry.machineTab);
		rbmk_moderator = new RBMKModerator().setTranslationKey("rbmk_moderator").setCreativeTab(MainRegistry.machineTab);
		rbmk_outgasser = new RBMKOutgasser().setTranslationKey("rbmk_outgasser").setCreativeTab(MainRegistry.machineTab);
		rbmk_storage = new RBMKStorage().setTranslationKey("rbmk_storage").setCreativeTab(MainRegistry.machineTab);
		rbmk_cooler = new RBMKCooler().setTranslationKey("rbmk_cooler").setCreativeTab(MainRegistry.machineTab);
		rbmk_heater = new RBMKHeater().setTranslationKey("rbmk_heater").setCreativeTab(MainRegistry.machineTab);
		rbmk_console = new RBMKConsole().setTranslationKey("rbmk_console").setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":rbmk/rbmk_console");
		rbmk_crane_console = new RBMKCraneConsole().setTranslationKey("rbmk_crane_console").setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":rbmk/rbmk_crane_console");
		rbmk_display = new RBMKDisplay().setTranslationKey("rbmk_display").setCreativeTab(MainRegistry.machineTab);
		rbmk_key_pad = new RBMKKeyPad().setTranslationKey("rbmk_key_pad").setCreativeTab(MainRegistry.machineTab);
		rbmk_gauge = new RBMKGauge().setTranslationKey("rbmk_gauge").setCreativeTab(MainRegistry.machineTab);
		rbmk_autoloader = new RBMKAutoloader().setTranslationKey("rbmk_autoloader").setCreativeTab(MainRegistry.machineTab).setHardness(50.0F).setResistance(60.0F).setBlockTextureName(RefStrings.MODID + ":rbmk_autoloader");
		rbmk_loader = ((BlockBase) new RBMKLoader(Material.IRON).setBlockName("rbmk_loader").setCreativeTab(MainRegistry.machineTab).setHardness(50.0F).setResistance(60.0F)).setBlockTextureName(RefStrings.MODID + ":rbmk_loader");
		rbmk_steam_inlet = new RBMKInlet(Material.IRON).setTranslationKey("rbmk_steam_inlet").setCreativeTab(MainRegistry.machineTab).setHardness(50.0F).setResistance(60.0F).setBlockTextureName(RefStrings.MODID + ":rbmk_steam_inlet");
		rbmk_steam_outlet = new RBMKOutlet(Material.IRON).setTranslationKey("rbmk_steam_outlet").setCreativeTab(MainRegistry.machineTab).setHardness(50.0F).setResistance(60.0F).setBlockTextureName(RefStrings.MODID + ":rbmk_steam_outlet");
		pribris = new RBMKDebris().setTranslationKey("pribris").setCreativeTab(MainRegistry.machineTab).setHardness(50.0F).setResistance(600.0F);
		pribris_burning = new RBMKDebrisBurning().setTranslationKey("pribris_burning").setCreativeTab(MainRegistry.machineTab).setHardness(50.0F).setResistance(600.0F);
		pribris_radiating = new RBMKDebrisRadiating().setTranslationKey("pribris_radiating").setCreativeTab(MainRegistry.machineTab).setHardness(50.0F).setResistance(600.0F);
		pribris_digamma = new RBMKDebrisDigamma().setTranslationKey("pribris_digamma").setCreativeTab(MainRegistry.machineTab).setHardness(50.0F).setResistance(600.0F);

		book_guide = new Guide(Material.IRON).setTranslationKey("book_guide").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.nukeTab);

		rail_wood = new RailGeneric("axe", 0).setMaxSpeed(0.2F).setTranslationKey("rail_wood").setHardness(5.0F).setResistance(10.0F).setCreativeTab(CreativeTabs.tabTransport).setBlockTextureName(RefStrings.MODID + ":rail_wood");
		rail_narrow = new RailGeneric("pickaxe", 0).setTranslationKey("rail_narrow").setHardness(5.0F).setResistance(10.0F).setCreativeTab(CreativeTabs.tabTransport).setBlockTextureName(RefStrings.MODID + ":rail_narrow");
		rail_highspeed = new RailGeneric("pickaxe", 0).setMaxSpeed(1F).setFlexible(false).setTranslationKey("rail_highspeed").setHardness(5.0F).setResistance(10.0F).setCreativeTab(CreativeTabs.tabTransport).setBlockTextureName(RefStrings.MODID + ":rail_highspeed");
		rail_booster = new RailBooster("pickaxe", 0).setTranslationKey("rail_booster").setHardness(5.0F).setResistance(10.0F).setCreativeTab(CreativeTabs.tabTransport).setBlockTextureName(RefStrings.MODID + ":rail_booster");
		rail_narrow_straight = new RailNarrowStraight().setTranslationKey("rail_narrow_straight").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null);
		rail_narrow_curve = new RailNarrowCurve().setTranslationKey("rail_narrow_curve").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null);
		rail_large_straight = new RailStandardStraight().setTranslationKey("rail_large_straight").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null);
		rail_large_straight_short = new RailStandardStraightShort().setTranslationKey("rail_large_straight_short").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null);
		rail_large_curve = new RailStandardCurveBase().setTranslationKey("rail_large_curve").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null);
		rail_large_curve_7 = new RailStandardCurveWide7().setTranslationKey("rail_large_curve_7").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null);
		rail_large_curve_9 = new RailStandardCurveWide9().setTranslationKey("rail_large_curve_9").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null);
		rail_large_ramp = new RailStandardRamp().setTranslationKey("rail_large_ramp").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null);
		rail_large_buffer = new RailStandardBuffer().setTranslationKey("rail_large_buffer").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null);
		rail_large_switch = new RailStandardSwitch().setTranslationKey("rail_large_switch").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null);
		rail_large_switch_flipped = new RailStandardSwitchFlipped().setTranslationKey("rail_large_switch_flipped").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null);

		crate = ((BlockCrate) new BlockCrate(Material.WOOD).setTranslationKey("crate")).withSoundType(SoundType.WOOD).setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.consumableTab).setBlockTextureName(RefStrings.MODID + ":crate");
		crate_weapon = ((BlockCrate) new BlockCrate(Material.WOOD).setTranslationKey("crate_weapon")).withSoundType(SoundType.WOOD).setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.consumableTab).setBlockTextureName(RefStrings.MODID + ":crate_weapon");
		crate_lead = ((BlockCrate) new BlockCrate(Material.IRON).setTranslationKey("crate_lead")).withSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.consumableTab).setBlockTextureName(RefStrings.MODID + ":crate_lead");
		crate_metal = ((BlockCrate) new BlockCrate(Material.IRON).setTranslationKey("crate_metal")).withSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.consumableTab).setBlockTextureName(RefStrings.MODID + ":crate_metal");
		crate_red = ((BlockCrate) new BlockCrate(Material.IRON).setTranslationKey("crate_red")).withSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(10.0F).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":crate_red");
		crate_can = new BlockCanCrate(Material.WOOD).setTranslationKey("crate_can").setHardness(1.0F).setResistance(2.5F).setCreativeTab(MainRegistry.consumableTab);
		crate_ammo = new BlockAmmoCrate(Material.IRON).setTranslationKey("crate_ammo").setStepSound(ModSoundTypes.legacyMetal).setHardness(1.0F).setResistance(2.5F).setCreativeTab(MainRegistry.consumableTab);
		crate_jungle = new BlockJungleCrate(Material.ROCK).setTranslationKey("crate_jungle").setStepSound(SoundType.STONE).setHardness(1.0F).setResistance(2.5F).setCreativeTab(MainRegistry.consumableTab).setBlockTextureName(RefStrings.MODID + ":crate_jungle");
		crate_iron = ((BlockStorageCrate) new BlockStorageCrate(Material.IRON).setTranslationKey("crate_iron")).withSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		crate_steel = ((BlockStorageCrate) new BlockStorageCrate(Material.IRON).setTranslationKey("crate_steel")).withSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		crate_desh = ((BlockStorageCrate) new BlockStorageCrate(Material.IRON).setTranslationKey("crate_desh")).withSoundType(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		crate_tungsten = ((BlockStorageCrate) new BlockStorageCrate(Material.IRON).setTranslationKey("crate_tungsten")).withSoundType(ModSoundTypes.legacyMetal).setHardness(7.5F).setResistance(300.0F).setCreativeTab(MainRegistry.machineTab);
		crate_template = ((BlockStorageCrate) new BlockStorageCrate(Material.IRON).setTranslationKey("crate_template")).withSoundType(ModSoundTypes.legacyMetal).setHardness(7.5F).setResistance(300.0F).setCreativeTab(MainRegistry.machineTab);
		safe = ((BlockStorageCrate) new BlockStorageCrate(Material.IRON).setTranslationKey("safe")).withSoundType(ModSoundTypes.legacyMetal).setHardness(7.5F).setResistance(10000.0F).setCreativeTab(MainRegistry.machineTab);
		mass_storage = new BlockMassStorage().setTranslationKey("mass_storage").setStepSound(ModSoundTypes.legacyMetal).setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);

		boxcar = new DecoBlock(Material.IRON).setTranslationKey("boxcar").setStepSound(ModSoundTypes.legacyMetal).setHardness(10.0F).setResistance(10.0F).setCreativeTab(MainRegistry.blockTab).setBlockTextureName(RefStrings.MODID + ":boxcar");
		boat = new DecoBlock(Material.IRON).setTranslationKey("boat").setStepSound(ModSoundTypes.legacyMetal).setHardness(10.0F).setResistance(10.0F).setCreativeTab(MainRegistry.blockTab).setBlockTextureName(RefStrings.MODID + ":asphalt");

		war_controller = new WarController().setTranslationKey("war_controller").setStepSound(ModSoundTypes.legacyMetal).setHardness(10.0F).setResistance(10.0F).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":block_steel");


		machine_well = new MachineOilWell().setTranslationKey("machine_well").setHardness(5.0F).setResistance(20.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_pumpjack = new MachinePumpjack().setTranslationKey("machine_pumpjack").setHardness(5.0F).setResistance(20.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_fracking_tower = new MachineFrackingTower().setTranslationKey("machine_fracking_tower").setHardness(5.0F).setResistance(20.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		oil_pipe = com.hbm.blocks.PlainCubeTextures.bind(new BlockNoDrop(Material.IRON).setTranslationKey("oil_pipe").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null), RefStrings.MODID + ":oil_pipe");
		machine_flare = new MachineGasFlare(Material.IRON).setTranslationKey("machine_flare").setHardness(5.0F).setResistance(100.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		chimney_brick = new MachineChimneyBrick(Material.IRON).setTranslationKey("chimney_brick").setHardness(5.0F).setResistance(100.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":brick_fire");
		chimney_industrial = new MachineChimneyIndustrial(Material.IRON).setTranslationKey("chimney_industrial").setHardness(5.0F).setResistance(100.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":concrete_colored_ext.machine");
		machine_refinery = new MachineRefinery(Material.IRON).setTranslationKey("machine_refinery").setHardness(5.0F).setResistance(20.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":machine_refinery");
		machine_vacuum_distill = new MachineVacuumDistill(Material.IRON).setTranslationKey("machine_vacuum_distill").setHardness(5.0F).setResistance(20.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_fraction_tower = new MachineFractionTower(Material.IRON).setTranslationKey("machine_fraction_tower").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		fraction_spacer = new FractionSpacer(Material.IRON).setTranslationKey("fraction_spacer").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_catalytic_cracker = new MachineCatalyticCracker(Material.IRON).setTranslationKey("machine_catalytic_cracker").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_catalytic_reformer = new MachineCatalyticReformer(Material.IRON).setTranslationKey("machine_catalytic_reformer").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_milk_reformer = new MachineMilkReformer(Material.IRON).setTranslationKey("machine_milk_reformer").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_hydrotreater = new MachineHydrotreater(Material.IRON).setTranslationKey("machine_hydrotreater").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_coker = new MachineCoker(Material.IRON).setTranslationKey("machine_coker").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_pyrooven = new MachinePyroOven(Material.IRON).setTranslationKey("machine_pyrooven").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_autosaw = new MachineAutosaw().setTranslationKey("machine_autosaw").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_excavator = new MachineExcavator().setTranslationKey("machine_excavator").setHardness(5.0F).setResistance(100.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_magma = new MachineMagma().setTranslationKey("machine_magma").setHardness(5.0F).setResistance(100.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_ore_slopper = new MachineOreSlopper().setTranslationKey("machine_ore_slopper").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_annihilator = new MachineAnnihilator().setTranslationKey("machine_annihilator").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_mining_laser = new MachineMiningLaser(Material.IRON).setTranslationKey("machine_mining_laser").setHardness(5.0F).setResistance(100.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":machine_mining_laser");
		barricade = com.hbm.blocks.PlainCubeTextures.bind(new BlockNoDrop(Material.SAND).setTranslationKey("barricade").setHardness(1.0F).setResistance(2.5F).setCreativeTab(null), RefStrings.MODID + ":barricade");
		machine_assembly_machine = new MachineAssemblyMachine(Material.IRON).setTranslationKey("machine_assembly_machine").setHardness(5.0F).setResistance(30.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_assembly_factory = new MachineAssemblyFactory(Material.IRON).setTranslationKey("machine_assembly_factory").setHardness(5.0F).setResistance(30.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_precass = new MachinePrecAss(Material.IRON).setTranslationKey("machine_precass").setHardness(5.0F).setResistance(30.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_arc_welder = new MachineArcWelder(Material.IRON).setTranslationKey("machine_arc_welder").setHardness(5.0F).setResistance(30.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_soldering_station = new MachineSolderingStation(Material.IRON).setTranslationKey("machine_soldering_station").setHardness(5.0F).setResistance(30.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");

		machine_vacuum_circuit = new MachineVacuumCircuit(Material.IRON).setTranslationKey("machine_vacuum_circuit").setHardness(5.0F).setResistance(30.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_chemical_plant = new MachineChemicalPlant(Material.IRON).setTranslationKey("machine_chemical_plant").setHardness(5.0F).setResistance(30.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_chemical_factory = new MachineChemicalFactory(Material.IRON).setTranslationKey("machine_chemical_factory").setHardness(5.0F).setResistance(30.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_purex = new MachinePUREX(Material.IRON).setTranslationKey("machine_purex").setHardness(5.0F).setResistance(30.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_mixer = new MachineMixer(Material.IRON).setTranslationKey("machine_mixer").setHardness(5.0F).setResistance(30.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_fluidtank = new MachineFluidTank(Material.IRON).setTranslationKey("machine_fluidtank").setHardness(5.0F).setResistance(20.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_bat9000 = new MachineBigAssTank9000(Material.IRON).setTranslationKey("machine_bat9000").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_orbus = new MachineOrbus(Material.IRON).setTranslationKey("machine_orbus").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_turbofan = new MachineTurbofan(Material.IRON).setTranslationKey("machine_turbofan").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_turbinegas = new MachineTurbineGas(Material.IRON).setTranslationKey("machine_turbinegas").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_lpw2 = new MachineLPW2().setTranslationKey("machine_lpw2").setHardness(5.0F).setResistance(100.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_htr3 = new MachineHTR3().setTranslationKey("machine_htr3").setHardness(5.0F).setResistance(100.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_htrf4 = new MachineHTRF4().setTranslationKey("machine_htrf4").setHardness(5.0F).setResistance(100.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_htrf4neo = new MachineHTRFNeo().setTranslationKey("machine_htrf4neo").setHardness(5.0F).setResistance(100.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_xenon_thruster = new MachineXenonThruster(Material.IRON).setTranslationKey("machine_xenon_thruster").setHardness(5.0F).setResistance(100.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		press_preheater = ((BlockBase) new BlockBase(Material.IRON).setBlockName("press_preheater").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab)).setBlockTextureName(RefStrings.MODID + ":press_preheater");
		machine_press = new MachinePress(Material.IRON).setTranslationKey("machine_press").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":machine_press");
		machine_epress = new MachineEPress(Material.IRON).setTranslationKey("machine_epress").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":machine_epress");
		machine_conveyor_press = new MachineConveyorPress(Material.IRON).setTranslationKey("machine_conveyor_press").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_ammo_press = new MachineAmmoPress().setTranslationKey("machine_ammo_press").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		reactor_research = new ReactorResearch(Material.IRON).setTranslationKey("machine_reactor_small").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":machine_reactor_small");
		reactor_zirnox = new ReactorZirnox(Material.IRON).setTranslationKey("machine_zirnox").setHardness(5.0F).setResistance(100.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		zirnox_destroyed = new ZirnoxDestroyed(Material.IRON).setTranslationKey("zirnox_destroyed").setHardness(100.0F).setResistance(800.0F).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_controller = new MachineReactorControl(Material.IRON).setTranslationKey("machine_controller").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null);
		machine_dish_controller = new MachineDishControl(Material.IRON).setTranslationKey("dish_control").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel_machine");

		machine_cryo_distill = new MachineCryoDistill(Material.IRON).setTranslationKey("machine_cryo_distill").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");

		machine_boiler_off = new MachineBoiler(false).setTranslationKey("machine_boiler_off").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":machine_boiler_off");

		machine_steam_engine = new MachineSteamEngine().setTranslationKey("machine_steam_engine").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_turbine = new MachineTurbine(Material.IRON).setTranslationKey("machine_turbine").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":machine_turbine");
		machine_large_turbine = new MachineLargeTurbine(Material.IRON).setTranslationKey("machine_large_turbine").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":machine_large_turbine");
		machine_industrial_turbine = new MachineIndustrialTurbine().setTranslationKey("machine_industrial_turbine").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_chungus = new MachineChungus(Material.IRON).setTranslationKey("machine_chungus").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		machine_condenser = new MachineCondenser(Material.IRON).setTranslationKey("machine_condenser").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":condenser");
		machine_tower_small = new MachineTowerSmall(Material.IRON).setTranslationKey("machine_tower_small").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":brick_concrete");
		machine_tower_large = new MachineTowerLarge(Material.IRON).setTranslationKey("machine_tower_large").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":concrete");
		machine_condenser_powered = new MachineCondenserPowered(Material.IRON).setTranslationKey("machine_condenser_powered").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel_machine");
		machine_radiator = new MachineRadiator(Material.IRON).setTranslationKey("machine_radiator").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel_machine");

		machine_deuterium_extractor = new MachineDeuteriumExtractor(Material.IRON).setTranslationKey("machine_deuterium_extractor").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":machine_deuterium_extractor_side");
		machine_deuterium_tower = new DeuteriumTower(Material.IRON).setTranslationKey("machine_deuterium_tower").setHardness(10.0F).setResistance(20.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":concrete");
		machine_atmo_tower = new AtmoTower(Material.IRON).setTranslationKey("machine_atmo_tower").setHardness(10.0F).setResistance(20.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":concrete");
		machine_atmo_vent = new BlockAtmosphericCompressor(Material.IRON).setTranslationKey("machine_atmo_vent").setHardness(10.0F).setResistance(20.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":concrete");

		machine_liquefactor = new MachineLiquefactor().setTranslationKey("machine_liquefactor").setHardness(10.0F).setResistance(20.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel_machine");
		machine_solidifier = new MachineSolidifier().setTranslationKey("machine_solidifier").setHardness(10.0F).setResistance(20.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel_machine");
		machine_intake = new MachineIntake().setTranslationKey("machine_intake").setHardness(10.0F).setResistance(20.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel_machine");
		machine_atmo_emitter = new MachineAtmosphericEmitter().setTranslationKey("machine_atmo_emitter").setHardness(10.0F).setResistance(20.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel_machine");
		machine_compressor = new MachineCompressor().setTranslationKey("machine_compressor").setHardness(10.0F).setResistance(20.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel_machine");
		machine_compressor_compact = new MachineCompressorCompact().setTranslationKey("machine_compressor_compact").setHardness(10.0F).setResistance(20.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel_machine");

		machine_electrolyser = new MachineElectrolyser().setTranslationKey("machine_electrolyser").setHardness(10.0F).setResistance(20.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel_machine");

		machine_autocrafter = new MachineAutocrafter().setTranslationKey("machine_autocrafter").setHardness(10.0F).setResistance(20.0F).setCreativeTab(MainRegistry.machineTab);
		machine_funnel = new MachineFunnel().setTranslationKey("machine_funnel").setHardness(10.0F).setResistance(20.0F).setCreativeTab(MainRegistry.machineTab);

		anvil_iron = new NTMAnvil(Material.IRON, NTMAnvil.TIER_IRON).setTranslationKey("anvil_iron").setCreativeTab(MainRegistry.machineTab);
		anvil_lead = new NTMAnvil(Material.IRON, NTMAnvil.TIER_IRON).setTranslationKey("anvil_lead").setCreativeTab(MainRegistry.machineTab);
		anvil_steel = new NTMAnvil(Material.IRON, NTMAnvil.TIER_STEEL).setTranslationKey("anvil_steel").setCreativeTab(MainRegistry.machineTab);
		anvil_desh = new NTMAnvil(Material.IRON, NTMAnvil.TIER_OIL).setTranslationKey("anvil_desh").setCreativeTab(MainRegistry.machineTab);
		anvil_ferrouranium = new NTMAnvil(Material.IRON, NTMAnvil.TIER_NUCLEAR).setTranslationKey("anvil_ferrouranium").setCreativeTab(MainRegistry.machineTab);
		anvil_saturnite = new NTMAnvil(Material.IRON, NTMAnvil.TIER_RBMK).setTranslationKey("anvil_saturnite").setCreativeTab(MainRegistry.machineTab);
		anvil_bismuth_bronze = new NTMAnvil(Material.IRON, NTMAnvil.TIER_RBMK).setTranslationKey("anvil_bismuth_bronze").setCreativeTab(MainRegistry.machineTab);
		anvil_arsenic_bronze = new NTMAnvil(Material.IRON, NTMAnvil.TIER_RBMK).setTranslationKey("anvil_arsenic_bronze").setCreativeTab(MainRegistry.machineTab);
		anvil_schrabidate = new NTMAnvil(Material.IRON, NTMAnvil.TIER_FUSION).setTranslationKey("anvil_schrabidate").setCreativeTab(MainRegistry.machineTab);
		anvil_dnt = new NTMAnvil(Material.IRON, NTMAnvil.TIER_PARTICLE).setTranslationKey("anvil_dnt").setCreativeTab(MainRegistry.machineTab);
		anvil_osmiridium = new NTMAnvil(Material.IRON, NTMAnvil.TIER_GERALD).setTranslationKey("anvil_osmiridium").setCreativeTab(MainRegistry.machineTab);
		anvil_murky = new NTMAnvil(Material.IRON, 1916169).setTranslationKey("anvil_murky").setCreativeTab(MainRegistry.machineTab);

		machine_waste_drum = new WasteDrum(Material.IRON).setTranslationKey("machine_waste_drum").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":waste_drum");
		machine_storage_drum = new StorageDrum(Material.IRON).setTranslationKey("machine_storage_drum").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":machine_storage_drum");

		machine_siren = new MachineSiren(Material.IRON).setTranslationKey("machine_siren").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":machine_siren");

		radiobox = new Radiobox(Material.IRON).setTranslationKey("radiobox").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":radiobox");
		radiorec = new RadioRec(Material.IRON).setTranslationKey("radiorec").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":radiorec");

		machine_forcefield = new MachineForceField(Material.IRON).setTranslationKey("machine_forcefield").setHardness(5.0F).setResistance(100.0F).setCreativeTab(MainRegistry.missileTab).setBlockTextureName(RefStrings.MODID + ":machine_forcefield");

		cheater_virus = new CheaterVirus(Material.IRON).setTranslationKey("cheater_virus").setHardness(Float.POSITIVE_INFINITY).setResistance(Float.POSITIVE_INFINITY).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":cheater_virus");
		cheater_virus_seed = new CheaterVirusSeed(Material.IRON).setTranslationKey("cheater_virus_seed").setHardness(Float.POSITIVE_INFINITY).setResistance(Float.POSITIVE_INFINITY).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":cheater_virus_seed");
		crystal_virus = new CrystalVirus(Material.IRON).setTranslationKey("crystal_virus").setHardness(15.0F).setResistance(Float.POSITIVE_INFINITY).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":crystal_virus");
		crystal_hardened = ((BlockBase) new BlockGeneric(Material.IRON).setBlockName("crystal_hardened").setHardness(15.0F).setResistance(Float.POSITIVE_INFINITY).setCreativeTab(null)).setBlockTextureName(RefStrings.MODID + ":crystal_hardened");
		crystal_pulsar = new CrystalPulsar(Material.IRON).setTranslationKey("crystal_pulsar").setHardness(15.0F).setResistance(Float.POSITIVE_INFINITY).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":crystal_pulsar");
		taint = new BlockTaint(Material.IRON).setTranslationKey("taint").setHardness(15.0F).setResistance(10.0F).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":taint");

		vent_chlorine = new BlockVent(Material.IRON).setTranslationKey("vent_chlorine").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":vent_chlorine");
		vent_cloud = new BlockVent(Material.IRON).setTranslationKey("vent_cloud").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":vent_cloud");
		vent_pink_cloud = new BlockVent(Material.IRON).setTranslationKey("vent_pink_cloud").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":vent_pink_cloud");
		vent_chlorine_seal = new BlockClorineSeal(Material.IRON).setTranslationKey("vent_chlorine_seal").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		chlorine_gas = new BlockGasClorine().setTranslationKey("chlorine_gas").setHardness(0.0F).setResistance(0.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":chlorine_gas");

		air_vent = new BlockAirPump(Material.IRON).setTranslationKey("air_vent").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel_machine");
		air_scrubber = new BlockAirScrubber(Material.IRON).setTranslationKey("air_scrubber").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel_machine");
		algae_film = new BlockAlgaeFilm(Material.IRON).setTranslationKey("algae_film").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		hydrobay = new MachineHydroponic(Material.IRON).setTranslationKey("hydrobay").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel_machine");

		gas_radon = new BlockGasRadon().setTranslationKey("gas_radon").setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":gas_radon");
		gas_radon_dense = new BlockGasRadonDense().setTranslationKey("gas_radon_dense").setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":gas_radon_dense");
		gas_radon_tomb = new BlockGasRadonTomb().setTranslationKey("gas_radon_tomb").setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":gas_radon_tomb");
		gas_meltdown = new BlockGasMeltdown().setTranslationKey("gas_meltdown").setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":gas_meltdown");
		gas_monoxide = new BlockGasMonoxide().setTranslationKey("gas_monoxide").setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":gas_monoxide");
		gas_asbestos = new BlockGasAsbestos().setTranslationKey("gas_asbestos").setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":gas_asbestos");
		gas_coal = new BlockGasCoal().setTranslationKey("gas_coal").setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":gas_coal");
		gas_flammable = new BlockGasFlammable().setTranslationKey("gas_flammable").setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":gas_flammable");
		gas_explosive = new BlockGasExplosive().setTranslationKey("gas_explosive").setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":gas_explosive");
		vacuum = new BlockVacuum().setTranslationKey("vacuum").setResistance(1000000F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":vacuum");

		absorber = ((BlockBase) new BlockGeneric(Material.IRON).setBlockName("absorber").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null)).setBlockTextureName(RefStrings.MODID + ":block_deprecated");
		absorber_red = ((BlockBase) new BlockGeneric(Material.IRON).setBlockName("absorber_red").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null)).setBlockTextureName(RefStrings.MODID + ":block_deprecated");
		absorber_green = ((BlockBase) new BlockGeneric(Material.IRON).setBlockName("absorber_green").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null)).setBlockTextureName(RefStrings.MODID + ":block_deprecated");
		absorber_pink = ((BlockBase) new BlockGeneric(Material.IRON).setBlockName("absorber_pink").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null)).setBlockTextureName(RefStrings.MODID + ":block_deprecated");

		rad_absorber = new BlockAbsorber(Material.IRON).setBlockName("rad_absorber").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		decon = new BlockDecon(Material.IRON).setTranslationKey("decon").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":decon_side");

		if(Loader.isModLoaded("OpenComputers")) {
			oc_cable_paintable = new BlockOpenComputersCablePaintable().setTranslationKey("oc_cable_paintable").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
		}

		volcano_core = new BlockVolcano().setTranslationKey("volcano_core").setBlockUnbreakable().setResistance(10000.0F).setCreativeTab(MainRegistry.nukeTab).setBlockTextureName(RefStrings.MODID + ":volcano_core");
		volcano_rad_core = new BlockVolcano().setTranslationKey("volcano_rad_core").setBlockUnbreakable().setResistance(10000.0F).setCreativeTab(MainRegistry.nukeTab).setBlockTextureName(RefStrings.MODID + ":volcano_rad_core");

		statue_elb_f = new DecoBlockAlt(Material.IRON).setTranslationKey("#undef").setHardness(Float.POSITIVE_INFINITY).setLightLevel(1.0F).setResistance(Float.POSITIVE_INFINITY);

		mud_fluid = new MudFluid().setDensity(2500).setViscosity(3000).setLuminosity(5).setTemperature(2773).setUnlocalizedName("mud_fluid");
		FluidRegistry.registerFluid(mud_fluid);
		mud_block = new MudBlock(mud_fluid, fluidmud.setReplaceable(), ModDamageSource.mudPoisoning).setTranslationKey("mud_block").setResistance(500F);

		acid_fluid = new AcidFluid().setDensity(2500).setViscosity(1500).setLuminosity(5).setTemperature(2773).setUnlocalizedName("acid_fluid");
		FluidRegistry.registerFluid(acid_fluid);
		acid_block = new AcidBlock(acid_fluid, fluidacid.setReplaceable(), ModDamageSource.acid).setTranslationKey("acid_block").setResistance(500F);

		toxic_fluid = new ToxicFluid().setDensity(2500).setViscosity(2000).setLuminosity(15).setTemperature(2773).setUnlocalizedName("toxic_fluid");
		FluidRegistry.registerFluid(toxic_fluid);
		toxic_block = new ToxicBlock(toxic_fluid, fluidtoxic.setReplaceable(), ModDamageSource.radiation).setTranslationKey("toxic_block").setResistance(500F);

		schrabidic_fluid = new SchrabidicFluid().setDensity(31200).setViscosity(500).setTemperature(273).setUnlocalizedName("schrabidic_fluid");
		FluidRegistry.registerFluid(schrabidic_fluid);
		schrabidic_block = new SchrabidicBlock(schrabidic_fluid, Material.WATER, ModDamageSource.radiation).setTranslationKey("schrabidic_block").setResistance(500F);

		corium_fluid = new CoriumFluid().setDensity(600000).setViscosity(12000).setLuminosity(10).setTemperature(1500).setUnlocalizedName("corium_fluid");
		FluidRegistry.registerFluid(corium_fluid);
		corium_block = new CoriumFinite(corium_fluid, fluidcorium).setTranslationKey("corium_block").setResistance(500F);

		volcanic_lava_fluid = new VolcanicFluid().setLuminosity(15).setDensity(3000).setViscosity(3000).setTemperature(1300).setUnlocalizedName("volcanic_lava_fluid");
		FluidRegistry.registerFluid(volcanic_lava_fluid);
		volcanic_lava_block = new VolcanicBlock(volcanic_lava_fluid, Material.LAVA).setTranslationKey("volcanic_lava_block").setResistance(500F);

		rad_lava_fluid = new RadFluid().setLuminosity(15).setDensity(3000).setViscosity(3000).setTemperature(1300).setUnlocalizedName("rad_lava_fluid");
		FluidRegistry.registerFluid(rad_lava_fluid);
		rad_lava_block = new RadBlock(rad_lava_fluid, Material.LAVA).setTranslationKey("rad_lava_block").setResistance(500F);

		sulfuric_acid_fluid = new GenericFluid("sulfuric_acid_fluid").setDensity(1840).setViscosity(1000).setTemperature(273);
		FluidRegistry.registerFluid(sulfuric_acid_fluid);
		sulfuric_acid_block = new GenericFluidBlock(sulfuric_acid_fluid, Material.WATER, "sulfuric_acid_still", "sulfuric_acid_flowing").setDamage(ModDamageSource.acid, 5F).setTranslationKey("sulfuric_acid_block").setResistance(500F);

		mercury_fluid = new GenericFluid("mercury_fluid").setDensity(1840).setViscosity(1000).setTemperature(273);
		FluidRegistry.registerFluid(mercury_fluid);
		mercury_block = new GenericFluidBlock(mercury_fluid, Material.WATER, "mercury_still", "mercury_flowing").setDamage(ModDamageSource.lead, 1F).setTranslationKey("mercury_block").setResistance(500F);

		bromine_fluid = new GenericFluid("bromine_fluid").setDensity(3000).setViscosity(3000).setTemperature(273);
		FluidRegistry.registerFluid(bromine_fluid);
		bromine_block = new GenericFluidBlock(bromine_fluid, Material.WATER, "bromine_still", "bromine_flowing").setDamage(ModDamageSource.acid, 5F).setTranslationKey("bromine_block").setResistance(500F);

		Fluid liquidConcrete = new GenericFluid("concrete_liquid").setViscosity(2000);
		concrete_liquid = new GenericFiniteFluid(liquidConcrete, Material.ROCK, "concrete_liquid", "concrete_liquid_flowing").setQuantaPerBlock(4).setTranslationKey("concrete_liquid").setResistance(500F);

		flesh_block = new MeltedFlesh(Material.SNOW).setTranslationKey("flesh_block").setStepSound(SoundType.GROUND).setCreativeTab(MainRegistry.blockTab).setHardness(0.1F).setLightOpacity(0).setBlockTextureName(RefStrings.MODID + ":meltedFlesh");
		charred_flesh_block = new MeltedFlesh(Material.SNOW).setTranslationKey("charred_flesh_block").setStepSound(SoundType.GROUND).setCreativeTab(MainRegistry.blockTab).setHardness(0.1F).setLightOpacity(0).setBlockTextureName(RefStrings.MODID + ":charredFlesh");
		carbonized_flesh_block = new MeltedFlesh(Material.SNOW).setTranslationKey("carbonized_flesh_block").setStepSound(SoundType.GROUND).setCreativeTab(MainRegistry.blockTab).setHardness(0.1F).setLightOpacity(0).setBlockTextureName(RefStrings.MODID + ":carbonizedFlesh");

		ccl_fluid = new GenericFluid("ccl_fluid").setDensity(1840).setViscosity(1000).setTemperature(273);
		FluidRegistry.registerFluid(ccl_fluid);
		ccl_block = new GenericFluidBlock(ccl_fluid, Material.WATER, "ccl", "ccl_flowing").setDamage(ModDamageSource.lead, 1F).setTranslationKey("ccl_block").setResistance(500F);


		dummy_block_blast = new DummyBlockBlast(Material.IRON).setTranslationKey("dummy_block_blast").setHardness(10.0F).setResistance(10000.0F).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":block_steel");
		dummy_plate_compact_launcher = new DummyBlockMachine(Material.IRON, compact_launcher, false).setBounds(0, 16, 0, 16, 16, 16).setTranslationKey("dummy_plate_compact_launcher").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":block_steel");
		dummy_port_compact_launcher = new DummyBlockMachine(Material.IRON, compact_launcher, true).setTranslationKey("dummy_port_compact_launcher").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":block_steel");
		dummy_plate_launch_table = new DummyBlockMachine(Material.IRON, launch_table, false).setBounds(0, 16, 0, 16, 16, 16).setTranslationKey("dummy_plate_launch_table").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":block_steel");
		dummy_port_launch_table = new DummyBlockMachine(Material.IRON, launch_table, true).setTranslationKey("dummy_port_launch_table").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":block_steel");
		dummy_plate_cargo = new DummyBlockMachine(Material.IRON, sat_dock, false).setBounds(0, 0, 0, 16, 8, 16).setTranslationKey("dummy_plate_cargo").setHardness(5.0F).setResistance(10.0F).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":block_steel");

		ntm_dirt = new BlockNTMDirt().setTranslationKey("ntm_dirt").setHardness(0.5F).setStepSound(SoundType.GROUND).setCreativeTab(null).setBlockTextureName("dirt");

		pink_log = ((BlockLogNT) new BlockLogNT("log_oak", RefStrings.MODID + ":pink_log").setTranslationKey("pink_log").setHardness(0.5F)).withSoundType(SoundType.WOOD).setCreativeTab(null);
		pink_planks = ((BlockBase) ((BlockBase) new BlockGeneric(Material.WOOD).setBlockName("pink_planks")).setSoundType(SoundType.WOOD).setCreativeTab(null)).setBlockTextureName(RefStrings.MODID + ":pink_planks");
		pink_slab = new BlockPinkSlab(false, Material.WOOD).setTranslationKey("pink_slab").setStepSound(SoundType.WOOD).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":pink_planks");
		pink_double_slab = new BlockPinkSlab(true, Material.WOOD).setTranslationKey("pink_double_slab").setStepSound(SoundType.WOOD).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":pink_planks");
		pink_stairs = new BlockGenericStairs(pink_planks, 0).setTranslationKey("pink_stairs").setStepSound(SoundType.WOOD).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":pink_planks");

		vinyl_log = ((BlockLogNT) new BlockLogNT(RefStrings.MODID + ":vinyl_log_side", RefStrings.MODID + ":vinyl_log_top").setTranslationKey("vinyl_log")).withSoundType(SoundType.WOOD).setCreativeTab(MainRegistry.blockTab).setHardness(0.5F).setResistance(2.5F);
		pvc_log = ((BlockLogNT) new BlockLogNT(RefStrings.MODID + ":pvc_log_side", RefStrings.MODID + ":pvc_log_top").setTranslationKey("pvc_log")).withSoundType(SoundType.WOOD).setCreativeTab(MainRegistry.blockTab).setHardness(0.5F).setResistance(2.5F);
		rubber_leaves = new BlockRubberLeaves().setTranslationKey("rubber_leaves").setStepSound(SoundType.PLANT).setCreativeTab(MainRegistry.blockTab).setHardness(0.5F).setResistance(2.5F).setBlockTextureName(RefStrings.MODID + ":rubber_leaves");
		rubber_grass = new RubberGrass(Material.GRASS, false).setTranslationKey("rubber_grass").setStepSound(SoundType.PLANT).setCreativeTab(MainRegistry.blockTab).setHardness(0.5F).setResistance(2.5F);
		rubber_silt = ((BlockBase) ((BlockBase) new BlockGeneric(Material.SAND).setBlockName("rubber_silt")).setSoundType(SoundType.GROUND).setCreativeTab(MainRegistry.blockTab).setHardness(1.0F).setResistance(1.0F)).setBlockTextureName(RefStrings.MODID + ":rubber_silt");
		rubber_plant = ((BlockBase) new BlockRubberPlant().setBlockName("rubber_plant").setCreativeTab(MainRegistry.blockTab)).setSoundType(SoundType.PLANT).setHardness(0.0F);
		spike_cacti = ((BlockBase) new BlockRubberCacti(Material.GRASS).setBlockName("rubber_tall").setCreativeTab(MainRegistry.blockTab)).setSoundType(SoundType.PLANT).setHardness(0.0F);
		vinyl_sand = new BlockFalling(Material.SAND).setTranslationKey("vinyl_sand").setStepSound(SoundType.SAND).setCreativeTab(MainRegistry.blockTab).setHardness(1.0F).setResistance(1.0F).setBlockTextureName(RefStrings.MODID + ":sand_vinyl");
		vinyl_vines = new BlockVinylVine().setTranslationKey("vinyl_vine").setStepSound(SoundType.PLANT).setCreativeTab(MainRegistry.blockTab).setHardness(1.0F).setResistance(1.0F).setBlockTextureName(RefStrings.MODID + ":vine");
		pet_leaves = new BlockRubberLeaves().setTranslationKey("pet_leaves").setStepSound(SoundType.PLANT).setCreativeTab(MainRegistry.blockTab).setHardness(0.5F).setResistance(2.5F).setBlockTextureName(RefStrings.MODID + ":pet_leaves");
		vinyl_planks = ((BlockBase) ((BlockBase) new BlockGeneric(Material.WOOD).setBlockName("vinyl_planks")).setSoundType(SoundType.WOOD).setCreativeTab(MainRegistry.blockTab).setHardness(1.0F).setResistance(1.0F)).setBlockTextureName(RefStrings.MODID + ":vinyl_planks");
		pvc_planks = ((BlockBase) ((BlockBase) new BlockGeneric(Material.WOOD).setBlockName("pvc_planks")).setSoundType(SoundType.WOOD).setCreativeTab(MainRegistry.blockTab).setHardness(1.0F).setResistance(1.0F)).setBlockTextureName(RefStrings.MODID + ":pvc_planks");

		astroturf = new BlockAstroturf(Material.GRASS).setTranslationKey("astroturf").setStepSound(SoundType.PLANT).setCreativeTab(MainRegistry.blockTab).setHardness(0.6F);

		// stop self flagellating and do something about it
		lattice_log = ((BlockLogNT) new BlockLogNT(RefStrings.MODID + ":rad_log_side", RefStrings.MODID + ":rad_log_top").setTranslationKey("lattice_log")).withSoundType(SoundType.WOOD).setCreativeTab(MainRegistry.blockTab).setHardness(0.5F).setResistance(2.5F);
		bf_log = ((BlockLogNT) new BlockLogNT(RefStrings.MODID + ":bf_log_side", RefStrings.MODID + ":bf_log_top").setTranslationKey("bf_log")).withSoundType(SoundType.WOOD).setCreativeTab(MainRegistry.blockTab).setHardness(0.5F).setResistance(2.5F);
		primed_log = ((BlockLogNT) new BlockLogNT(RefStrings.MODID + ":primed_log_side", RefStrings.MODID + ":primed_log_top").setTranslationKey("primed_log")).withSoundType(SoundType.WOOD).setCreativeTab(MainRegistry.blockTab).setHardness(0.5F).setResistance(2.5F);
		eu_log = ((BlockLogNT) new BlockLogNT(RefStrings.MODID + ":eu_log_side", RefStrings.MODID + ":eu_log_top").setTranslationKey("eu_log")).withSoundType(SoundType.WOOD).setCreativeTab(MainRegistry.blockTab).setHardness(0.5F).setResistance(2.5F);

		ff = new BlockFF(Material.IRON).setTranslationKey("ff").setHardness(0.5F).setStepSound(SoundType.GROUND).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":code");

		atmosphere_editor = new BlockAtmosphereEditor(Material.IRON).setTranslationKey("atmosphere_editor").setHardness(1.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":atmosphere_editor");

		transporter_rocket = new BlockTransporterRocket(Material.IRON).setTranslationKey("transporter_rocket").setHardness(1.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		orbital_station = new BlockOrbitalStation(Material.IRON).setTranslationKey("orbital_station").setBlockUnbreakable().setResistance(Float.POSITIVE_INFINITY).setCreativeTab(null).setBlockTextureName(RefStrings.MODID + ":block_steel");
		orbital_station_port = new BlockOrbitalStation(Material.IRON).setTranslationKey("orbital_station_port").setHardness(1.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		orbital_station_launcher = new BlockOrbitalStationLauncher(Material.IRON).setTranslationKey("orbital_station_launcher").setHardness(1.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		orbital_station_computer = new BlockOrbitalStationComputer(Material.IRON).setTranslationKey("orbital_station_computer").setHardness(1.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");
		propulsion_creative = new BlockStationPropulsionCreative(Material.IRON).setTranslationKey("propulsion_creative").setHardness(1.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel");

		dyson_launcher = new MachineDysonLauncher(Material.IRON).setTranslationKey("dyson_launcher").setHardness(10.0F).setResistance(20.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel_machine");
		dyson_receiver = new MachineDysonReceiver(Material.IRON).setTranslationKey("dyson_receiver").setHardness(10.0F).setResistance(20.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel_machine");
		dyson_converter_tu = new MachineDysonConverterTU(Material.IRON).setTranslationKey("dyson_converter_tu").setHardness(10.0F).setResistance(20.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel_machine");
		dyson_converter_he = new MachineDysonConverterHE(Material.IRON).setTranslationKey("dyson_converter_he").setHardness(10.0F).setResistance(20.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel_machine");
		dyson_converter_anatmogenesis = new MachineDysonConverterAnatmogenesis(Material.IRON).setTranslationKey("dyson_converter_anatmogenesis").setHardness(10.0F).setResistance(20.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel_machine");

		orrery = new BlockOrrery(Material.IRON).setTranslationKey("orrery").setHardness(10.0F).setResistance(20.0F).setCreativeTab(MainRegistry.machineTab).setBlockTextureName(RefStrings.MODID + ":block_steel_machine");

		dummy_beam = new BlockDummyableBeam(Material.IRON).setTranslationKey("dummy_beam").setHardness(10.0F).setResistance(20.0F).setBlockTextureName(RefStrings.MODID + ":block_steel_machine");

		wand_air = new BlockWand(Blocks.AIR).setTranslationKey("wand_air");
		wand_loot = new BlockWandLoot().setTranslationKey("wand_loot");
		wand_jigsaw = new BlockWandJigsaw().setTranslationKey("wand_jigsaw");
		wand_logic = new BlockWandLogic().setTranslationKey("wand_logic");
		wand_tandem = new BlockWandTandem().setTranslationKey("wand_tandem");
		wand_structure = new BlockWandStructure().setTranslationKey("wand_structure");

		logic_block = new LogicBlock().setTranslationKey("logic_block").setBlockTextureName(RefStrings.MODID + ":logic_block");

	}

	private static void registerBlock() {
		//Test
		GameRegistry.registerBlock(event_tester, event_tester.getTranslationKey());
		GameRegistry.registerBlock(obj_tester, obj_tester.getTranslationKey());
		GameRegistry.registerBlock(structure_anchor, structure_anchor.getTranslationKey());

		// Vanilla ores (for other planets)
		register(ore_iron);
		register(ore_gold);
		register(ore_redstone);
		register(ore_lapis);
		register(ore_emerald);
		register(ore_quartz);
		register(ore_diamond);

		//Ores
		register(ore_uranium);
		GameRegistry.registerBlock(ore_uranium_scorched, ItemBlockBase.class, ore_uranium_scorched.getTranslationKey());
		register(ore_plutonium);
		register(ore_thorium);
		register(ore_morkite);
		register(ore_titanium);
		register(ore_sulfur);
		register(ore_glowstone);
		register(ore_niter);
		register(ore_copper);
		register(ore_nickel);
		register(ore_mineral);
		register(ore_zinc);
		register(ore_tungsten);
		register(ore_aluminium);
		register(ore_fluorite);
		register(ore_beryllium);
		register(ore_lead);
		register(ore_oil, ItemBlockLore.class);
		register(ore_oil_empty);
		register(ore_gas, ItemBlockLore.class);
		register(ore_gas_empty);
		GameRegistry.registerBlock(ore_oil_sand, ItemBlockBase.class, ore_oil_sand.getTranslationKey());
		register(ore_lignite);
		register(ore_asbestos);
		register(ore_schrabidium);
		register(ore_lithium);
		register(ore_niobium);
		register(ore_cadmium);
		register(ore_palladium);
		register(ore_iodine);
		register(ore_arsenic);
		register(ore_silicon);
		register(ore_fire);
		register(ore_shale);
		register(ore_brine, ItemBlockLore.class);
		register(ore_brine_empty, ItemBlockLore.class);
		register(ore_tekto, ItemBlockLore.class);
		register(ore_tekto_empty, ItemBlockLore.class);

		//Rare Minerals
		register(ore_australium);
		register(ore_rare);
		register(ore_cobalt);
		register(ore_cinnebar);
		register(ore_coltan);
		register(ore_lanthanium);

		//Stone clusters
		register(cluster_iron);
		register(cluster_titanium);
		register(cluster_aluminium);
		register(cluster_copper);

		//Bedrock ores
		register(ore_bedrock_oil);

		//Nether Ores
		GameRegistry.registerBlock(ore_nether_coal, ore_nether_coal.getTranslationKey());
		GameRegistry.registerBlock(ore_nether_smoldering, ore_nether_smoldering.getTranslationKey());
		GameRegistry.registerBlock(ore_nether_uranium, ore_nether_uranium.getTranslationKey());
		GameRegistry.registerBlock(ore_nether_uranium_scorched, ore_nether_uranium_scorched.getTranslationKey());
		GameRegistry.registerBlock(ore_nether_plutonium, ore_nether_plutonium.getTranslationKey());
		GameRegistry.registerBlock(ore_nether_tungsten, ore_nether_tungsten.getTranslationKey());
		GameRegistry.registerBlock(ore_nether_sulfur, ore_nether_sulfur.getTranslationKey());
		GameRegistry.registerBlock(ore_nether_fire, ore_nether_fire.getTranslationKey());
		GameRegistry.registerBlock(ore_nether_cobalt, ore_nether_cobalt.getTranslationKey());
		GameRegistry.registerBlock(ore_nether_schrabidium, ItemBlockLore.class, ore_nether_schrabidium.getTranslationKey());

		//Meteor Ores
		register(ore_meteor);

		//Gneiss Ores
		GameRegistry.registerBlock(ore_gneiss_iron, ore_gneiss_iron.getTranslationKey());
		GameRegistry.registerBlock(ore_gneiss_gold, ore_gneiss_gold.getTranslationKey());
		GameRegistry.registerBlock(ore_gneiss_uranium, ore_gneiss_uranium.getTranslationKey());
		GameRegistry.registerBlock(ore_gneiss_uranium_scorched, ore_gneiss_uranium_scorched.getTranslationKey());
		GameRegistry.registerBlock(ore_gneiss_copper, ore_gneiss_copper.getTranslationKey());
		GameRegistry.registerBlock(ore_gneiss_asbestos, ore_gneiss_asbestos.getTranslationKey());
		GameRegistry.registerBlock(ore_gneiss_lithium, ore_gneiss_lithium.getTranslationKey());
		GameRegistry.registerBlock(ore_gneiss_schrabidium, ItemBlockLore.class, ore_gneiss_schrabidium.getTranslationKey());
		GameRegistry.registerBlock(ore_gneiss_rare, ItemOreBlock.class, ore_gneiss_rare.getTranslationKey());
		GameRegistry.registerBlock(ore_gneiss_gas, ore_gneiss_gas.getTranslationKey());

		//Depth ores
		GameRegistry.registerBlock(ore_depth_cinnebar, ItemBlockBase.class, ore_depth_cinnebar.getTranslationKey());
		GameRegistry.registerBlock(ore_depth_zirconium, ItemBlockBase.class, ore_depth_zirconium.getTranslationKey());
		GameRegistry.registerBlock(ore_depth_borax, ItemBlockBase.class, ore_depth_borax.getTranslationKey());
		GameRegistry.registerBlock(cluster_depth_iron, ItemBlockBase.class, cluster_depth_iron.getTranslationKey());
		GameRegistry.registerBlock(cluster_depth_titanium, ItemBlockBase.class, cluster_depth_titanium.getTranslationKey());
		GameRegistry.registerBlock(cluster_depth_tungsten, ItemBlockBase.class, cluster_depth_tungsten.getTranslationKey());
		GameRegistry.registerBlock(ore_alexandrite, ItemBlockBase.class, ore_alexandrite.getTranslationKey());

		//Nether depth ores
		GameRegistry.registerBlock(ore_depth_nether_neodymium, ItemBlockBase.class, ore_depth_nether_neodymium.getTranslationKey());

		//Basalt ores
		register(ore_basalt);

		//End Ores
		GameRegistry.registerBlock(ore_tikite, ore_tikite.getTranslationKey());

		//Bedrock ore
		register(ore_bedrock);
		register(ore_volcano);

		//Secret
		register(stone_keyhole);
		register(stone_keyhole_meta);

		//Resource-bearing Stones
		register(stone_resource);
		register(stalagmite);
		register(stalactite);
		register(stone_biome);

		//Stone Variants
		GameRegistry.registerBlock(stone_porous, stone_porous.getTranslationKey());
		GameRegistry.registerBlock(stone_gneiss, stone_gneiss.getTranslationKey());
		GameRegistry.registerBlock(gneiss_brick, gneiss_brick.getTranslationKey());
		GameRegistry.registerBlock(gneiss_tile, gneiss_tile.getTranslationKey());
		GameRegistry.registerBlock(gneiss_chiseled, gneiss_chiseled.getTranslationKey());
		GameRegistry.registerBlock(stone_depth, ItemBlockBase.class, stone_depth.getTranslationKey());
		GameRegistry.registerBlock(depth_brick, ItemBlockBase.class, depth_brick.getTranslationKey());
		GameRegistry.registerBlock(depth_tiles, ItemBlockBase.class, depth_tiles.getTranslationKey());
		GameRegistry.registerBlock(stone_depth_nether, ItemBlockBase.class, stone_depth_nether.getTranslationKey());
		GameRegistry.registerBlock(depth_nether_brick, ItemBlockBase.class, depth_nether_brick.getTranslationKey());
		GameRegistry.registerBlock(depth_nether_tiles, ItemBlockBase.class, depth_nether_tiles.getTranslationKey());
		GameRegistry.registerBlock(depth_dnt, ItemBlockBase.class, depth_dnt.getTranslationKey());
		GameRegistry.registerBlock(basalt, basalt.getTranslationKey());
		GameRegistry.registerBlock(basalt_smooth, basalt_smooth.getTranslationKey());
		GameRegistry.registerBlock(basalt_brick, basalt_brick.getTranslationKey());
		GameRegistry.registerBlock(basalt_polished, basalt_polished.getTranslationKey());
		GameRegistry.registerBlock(basalt_tiles, basalt_tiles.getTranslationKey());
		//GameRegistry.registerBlock(stone_deep_cobble, ItemBlockBase.class, stone_deep_cobble.getTranslationKey());

		//Blocks
		GameRegistry.registerBlock(block_uranium, block_uranium.getTranslationKey());
		GameRegistry.registerBlock(block_u233, block_u233.getTranslationKey());
		GameRegistry.registerBlock(block_u235, block_u235.getTranslationKey());
		GameRegistry.registerBlock(block_u238, block_u238.getTranslationKey());
		GameRegistry.registerBlock(block_uranium_fuel, block_uranium_fuel.getTranslationKey());
		GameRegistry.registerBlock(block_neptunium, block_neptunium.getTranslationKey());
		GameRegistry.registerBlock(block_polonium, block_polonium.getTranslationKey());
		GameRegistry.registerBlock(block_mox_fuel, block_mox_fuel.getTranslationKey());
		GameRegistry.registerBlock(block_plutonium, block_plutonium.getTranslationKey());
		GameRegistry.registerBlock(block_pu238, block_pu238.getTranslationKey());
		GameRegistry.registerBlock(block_pu239, block_pu239.getTranslationKey());
		GameRegistry.registerBlock(block_pu240, block_pu240.getTranslationKey());
		GameRegistry.registerBlock(block_pu_mix, block_pu_mix.getTranslationKey());
		GameRegistry.registerBlock(block_plutonium_fuel, block_plutonium_fuel.getTranslationKey());
		GameRegistry.registerBlock(block_thorium, block_thorium.getTranslationKey());
		GameRegistry.registerBlock(block_thorium_fuel, block_thorium_fuel.getTranslationKey());
		GameRegistry.registerBlock(block_titanium, block_titanium.getTranslationKey());
		GameRegistry.registerBlock(block_sulfur, block_sulfur.getTranslationKey());
		GameRegistry.registerBlock(block_niter, block_niter.getTranslationKey());
		GameRegistry.registerBlock(block_copper, block_copper.getTranslationKey());
		GameRegistry.registerBlock(block_nickel, block_nickel.getTranslationKey());
		GameRegistry.registerBlock(block_red_copper, block_red_copper.getTranslationKey());
		GameRegistry.registerBlock(block_advanced_alloy, block_advanced_alloy.getTranslationKey());
		GameRegistry.registerBlock(block_tungsten, block_tungsten.getTranslationKey());
		GameRegistry.registerBlock(block_aluminium, block_aluminium.getTranslationKey());
		GameRegistry.registerBlock(block_fluorite, block_fluorite.getTranslationKey());
		GameRegistry.registerBlock(block_beryllium, block_beryllium.getTranslationKey());
		GameRegistry.registerBlock(block_cobalt, block_cobalt.getTranslationKey());
		GameRegistry.registerBlock(block_steel, block_steel.getTranslationKey());
		GameRegistry.registerBlock(block_osmiridium, block_osmiridium.getTranslationKey());
		GameRegistry.registerBlock(block_tcalloy, block_tcalloy.getTranslationKey());
		GameRegistry.registerBlock(block_cdalloy, block_cdalloy.getTranslationKey());
		GameRegistry.registerBlock(block_lead, block_lead.getTranslationKey());
		GameRegistry.registerBlock(block_bismuth, block_bismuth.getTranslationKey());
		GameRegistry.registerBlock(block_cadmium, block_cadmium.getTranslationKey());
		GameRegistry.registerBlock(block_coltan, block_coltan.getTranslationKey());
		GameRegistry.registerBlock(block_tantalium, block_tantalium.getTranslationKey());
		GameRegistry.registerBlock(block_niobium, block_niobium.getTranslationKey());
		GameRegistry.registerBlock(block_lithium, block_lithium.getTranslationKey());
		GameRegistry.registerBlock(block_zirconium, block_zirconium.getTranslationKey());
		GameRegistry.registerBlock(block_white_phosphorus, block_white_phosphorus.getTranslationKey());
		GameRegistry.registerBlock(block_red_phosphorus, block_red_phosphorus.getTranslationKey());
		GameRegistry.registerBlock(block_yellowcake, block_yellowcake.getTranslationKey());
		GameRegistry.registerBlock(block_scrap, block_scrap.getTranslationKey());
		GameRegistry.registerBlock(block_electrical_scrap, block_electrical_scrap.getTranslationKey());
		GameRegistry.registerBlock(block_fallout, block_fallout.getTranslationKey());
		GameRegistry.registerBlock(block_foam, block_foam.getTranslationKey());
		GameRegistry.registerBlock(block_graphite, block_graphite.getTranslationKey());
		register(block_coke);
		GameRegistry.registerBlock(block_graphite_drilled, block_graphite_drilled.getTranslationKey());
		GameRegistry.registerBlock(block_graphite_fuel, block_graphite_fuel.getTranslationKey());
		GameRegistry.registerBlock(block_graphite_rod, block_graphite_rod.getTranslationKey());
		GameRegistry.registerBlock(block_graphite_plutonium, block_graphite_plutonium.getTranslationKey());
		GameRegistry.registerBlock(block_graphite_source, block_graphite_source.getTranslationKey());
		GameRegistry.registerBlock(block_graphite_lithium, block_graphite_lithium.getTranslationKey());
		GameRegistry.registerBlock(block_graphite_tritium, block_graphite_tritium.getTranslationKey());
		GameRegistry.registerBlock(block_graphite_detector, block_graphite_detector.getTranslationKey());
		GameRegistry.registerBlock(block_boron, block_boron.getTranslationKey());
		GameRegistry.registerBlock(block_insulator, block_insulator.getTranslationKey());
		GameRegistry.registerBlock(block_fiberglass, block_fiberglass.getTranslationKey());
		GameRegistry.registerBlock(block_asbestos, block_asbestos.getTranslationKey());
		GameRegistry.registerBlock(block_trinitite, block_trinitite.getTranslationKey());
		GameRegistry.registerBlock(block_waste, block_waste.getTranslationKey());
		GameRegistry.registerBlock(block_waste_painted, block_waste_painted.getTranslationKey());
		GameRegistry.registerBlock(block_waste_vitrified, block_waste_vitrified.getTranslationKey());
		GameRegistry.registerBlock(ancient_scrap, ancient_scrap.getTranslationKey());
		GameRegistry.registerBlock(block_corium, block_corium.getTranslationKey());
		GameRegistry.registerBlock(block_corium_cobble, block_corium_cobble.getTranslationKey());
		GameRegistry.registerBlock(block_schraranium, ItemBlockBase.class, block_schraranium.getTranslationKey());
		GameRegistry.registerBlock(block_schrabidium, ItemBlockBase.class, block_schrabidium.getTranslationKey());
		GameRegistry.registerBlock(block_schrabidate, ItemBlockBase.class, block_schrabidate.getTranslationKey());
		GameRegistry.registerBlock(block_solinium, ItemBlockBase.class, block_solinium.getTranslationKey());
		GameRegistry.registerBlock(block_schrabidium_fuel, ItemBlockBase.class, block_schrabidium_fuel.getTranslationKey());
		GameRegistry.registerBlock(block_euphemium, ItemBlockLore.class, block_euphemium.getTranslationKey());
		GameRegistry.registerBlock(block_schrabidium_cluster, ItemBlockBase.class, block_schrabidium_cluster.getTranslationKey());
		GameRegistry.registerBlock(block_euphemium_cluster, ItemBlockLore.class, block_euphemium_cluster.getTranslationKey());
		GameRegistry.registerBlock(block_dineutronium, block_dineutronium.getTranslationKey());
		GameRegistry.registerBlock(block_magnetized_tungsten, block_magnetized_tungsten.getTranslationKey());
		GameRegistry.registerBlock(block_combine_steel, block_combine_steel.getTranslationKey());
		GameRegistry.registerBlock(block_desh, block_desh.getTranslationKey());
		GameRegistry.registerBlock(block_dura_steel, block_dura_steel.getTranslationKey());
		GameRegistry.registerBlock(block_starmetal, block_starmetal.getTranslationKey());
		GameRegistry.registerBlock(block_polymer, block_polymer.getTranslationKey());
		GameRegistry.registerBlock(block_bakelite, block_bakelite.getTranslationKey());
		GameRegistry.registerBlock(block_rubber, block_rubber.getTranslationKey());
		GameRegistry.registerBlock(block_australium, ItemOreBlock.class, block_australium.getTranslationKey());
		register(block_cap);
		GameRegistry.registerBlock(block_lanthanium, block_lanthanium.getTranslationKey());
		GameRegistry.registerBlock(block_ra226, block_ra226.getTranslationKey());
		GameRegistry.registerBlock(block_actinium, block_actinium.getTranslationKey());
		GameRegistry.registerBlock(block_tritium, block_tritium.getTranslationKey());
		GameRegistry.registerBlock(block_semtex, block_semtex.getTranslationKey());
		GameRegistry.registerBlock(block_c4, block_c4.getTranslationKey());
		GameRegistry.registerBlock(block_smore, block_smore.getTranslationKey());
		GameRegistry.registerBlock(block_slag, block_slag.getTranslationKey());

		//Deco Blocks
		register(deco_titanium, ItemBlock.class);
		register(deco_red_copper, ItemBlock.class);
		register(deco_tungsten, ItemBlock.class);
		register(deco_aluminium, ItemBlock.class);
		register(deco_steel, ItemBlock.class);
		register(deco_rusty_steel, ItemBlock.class);
		register(deco_lead, ItemBlock.class);
		register(deco_beryllium, ItemBlock.class);
		GameRegistry.registerBlock(deco_asbestos, deco_asbestos.getTranslationKey());
		register(deco_stainless, ItemBlock.class);
		GameRegistry.registerBlock(deco_emitter, ItemBlockBase.class, deco_emitter.getTranslationKey());
		GameRegistry.registerBlock(part_emitter, ItemBlockBase.class, part_emitter.getTranslationKey());
		GameRegistry.registerBlock(deco_loot, deco_loot.getTranslationKey());
		GameRegistry.registerBlock(pedestal, pedestal.getTranslationKey());
		register(skeleton_holder);
		register(dungeon_spawner);
		GameRegistry.registerBlock(bobblehead, ItemBlockMeta.class, bobblehead.getTranslationKey());
		GameRegistry.registerBlock(snowglobe, ItemBlockMeta.class, snowglobe.getTranslationKey());
		GameRegistry.registerBlock(plushie, ItemBlockBase.class, plushie.getTranslationKey());
		GameRegistry.registerBlock(deco_rbmk, deco_rbmk.getTranslationKey());
		GameRegistry.registerBlock(deco_rbmk_smooth, deco_rbmk_smooth.getTranslationKey());

		//Gravel
		GameRegistry.registerBlock(gravel_obsidian, ItemBlockBlastInfo.class, gravel_obsidian.getTranslationKey());
		GameRegistry.registerBlock(gravel_diamond, ItemBlockLore.class, gravel_diamond.getTranslationKey());

		//Lamps
		GameRegistry.registerBlock(lamp_tritium_green_off, lamp_tritium_green_off.getTranslationKey());
		GameRegistry.registerBlock(lamp_tritium_green_on, lamp_tritium_green_on.getTranslationKey());
		GameRegistry.registerBlock(lamp_tritium_blue_off, lamp_tritium_blue_off.getTranslationKey());
		GameRegistry.registerBlock(lamp_tritium_blue_on, lamp_tritium_blue_on.getTranslationKey());
		GameRegistry.registerBlock(lamp_demon, lamp_demon.getTranslationKey());
		GameRegistry.registerBlock(lantern, lantern.getTranslationKey());
		GameRegistry.registerBlock(lantern_behemoth, lantern_behemoth.getTranslationKey());
		GameRegistry.registerBlock(spotlight_incandescent, spotlight_incandescent.getTranslationKey());
		GameRegistry.registerBlock(spotlight_incandescent_off, spotlight_incandescent_off.getTranslationKey());
		GameRegistry.registerBlock(spotlight_fluoro, spotlight_fluoro.getTranslationKey());
		GameRegistry.registerBlock(spotlight_fluoro_off, spotlight_fluoro_off.getTranslationKey());
		GameRegistry.registerBlock(spotlight_halogen, spotlight_halogen.getTranslationKey());
		GameRegistry.registerBlock(spotlight_halogen_off, spotlight_halogen_off.getTranslationKey());
		GameRegistry.registerBlock(spotlight_beam, spotlight_beam.getTranslationKey());
		register(floodlight);
		GameRegistry.registerBlock(floodlight_beam, floodlight_beam.getTranslationKey());

		//Reinforced Blocks
		register(sandbags);
		register(wood_barrier);
		register(wood_structure);
		GameRegistry.registerBlock(asphalt, ItemBlockBlastInfo.class, asphalt.getTranslationKey());
		GameRegistry.registerBlock(asphalt_light, ItemBlockBlastInfo.class, asphalt_light.getTranslationKey());
		GameRegistry.registerBlock(reinforced_brick, ItemBlockBlastInfo.class, reinforced_brick.getTranslationKey());
		register(reinforced_glass, ItemBlock.class);
		GameRegistry.registerBlock(reinforced_glass_pane, ItemBlockBlastInfo.class, reinforced_glass_pane.getTranslationKey());
		GameRegistry.registerBlock(reinforced_light, ItemBlockBlastInfo.class, reinforced_light.getTranslationKey());
		GameRegistry.registerBlock(reinforced_sand, ItemBlockBlastInfo.class, reinforced_sand.getTranslationKey());
		GameRegistry.registerBlock(reinforced_lamp_off, ItemBlockBlastInfo.class, reinforced_lamp_off.getTranslationKey());
		GameRegistry.registerBlock(reinforced_lamp_on, ItemBlockBlastInfo.class, reinforced_lamp_on.getTranslationKey());
		register(reinforced_laminate, ItemBlock.class);
		GameRegistry.registerBlock(reinforced_laminate_pane,ItemBlockBlastInfo.class, reinforced_laminate_pane.getTranslationKey());

		//Bricks
		register(rebar);
		GameRegistry.registerBlock(reinforced_stone, ItemBlockBlastInfo.class, reinforced_stone.getTranslationKey());
		GameRegistry.registerBlock(reinforced_ducrete, ItemBlockBlastInfo.class, reinforced_ducrete.getTranslationKey());
		GameRegistry.registerBlock(concrete_smooth, ItemBlockBlastInfo.class, concrete_smooth.getTranslationKey());
		GameRegistry.registerBlock(concrete_colored, ItemBlockColoredConcrete.class, concrete_colored.getTranslationKey());
		GameRegistry.registerBlock(concrete_colored_ext, ItemBlockBlastInfo.class, concrete_colored_ext.getTranslationKey());
		GameRegistry.registerBlock(concrete, ItemBlockBlastInfo.class, concrete.getTranslationKey());
		GameRegistry.registerBlock(concrete_asbestos, ItemBlockBlastInfo.class, concrete_asbestos.getTranslationKey());
		GameRegistry.registerBlock(concrete_rebar, ItemBlockBlastInfo.class, concrete_rebar.getTranslationKey());
		GameRegistry.registerBlock(concrete_super, ItemBlockBlastInfo.class, concrete_super.getTranslationKey());
		GameRegistry.registerBlock(concrete_super_broken, ItemBlockBlastInfo.class, concrete_super_broken.getTranslationKey());
		GameRegistry.registerBlock(ducrete_smooth, ItemBlockBlastInfo.class, ducrete_smooth.getTranslationKey());
		GameRegistry.registerBlock(ducrete, ItemBlockBlastInfo.class, ducrete.getTranslationKey());
		GameRegistry.registerBlock(concrete_pillar, ItemBlockBlastInfo.class, concrete_pillar.getTranslationKey());
		GameRegistry.registerBlock(brick_concrete, ItemBlockBlastInfo.class, brick_concrete.getTranslationKey());
		GameRegistry.registerBlock(brick_concrete_mossy, ItemBlockBlastInfo.class, brick_concrete_mossy.getTranslationKey());
		GameRegistry.registerBlock(brick_concrete_cracked, ItemBlockBlastInfo.class, brick_concrete_cracked.getTranslationKey());
		GameRegistry.registerBlock(brick_concrete_broken, ItemBlockBlastInfo.class, brick_concrete_broken.getTranslationKey());
		GameRegistry.registerBlock(brick_concrete_marked, ItemBlockBlastInfo.class, brick_concrete_marked.getTranslationKey());
		GameRegistry.registerBlock(brick_ducrete, ItemBlockBlastInfo.class, brick_ducrete.getTranslationKey());
		GameRegistry.registerBlock(brick_obsidian, ItemBlockBlastInfo.class, brick_obsidian.getTranslationKey());
		GameRegistry.registerBlock(brick_compound, ItemBlockBlastInfo.class, brick_compound.getTranslationKey());
		GameRegistry.registerBlock(brick_light, ItemBlockBlastInfo.class, brick_light.getTranslationKey());
		GameRegistry.registerBlock(brick_asbestos, brick_asbestos.getTranslationKey());
		GameRegistry.registerBlock(brick_fire, ItemBlockBlastInfo.class, brick_fire.getTranslationKey());

		//Lightstone and its stair/slab variants
		register(lightstone);
		register(lightstone_tile_stairs);
		register(lightstone_bricks_stairs);
		register(stones_slab, ItemModSlab.class);
		register(stones_double_slab, ItemModSlab.class);

		register(brick_forgotten);
		register(brick_forgotten_lock);

		GameRegistry.registerBlock(concrete_slab, ItemModSlab.class, concrete_slab.getTranslationKey());
		GameRegistry.registerBlock(concrete_double_slab, ItemModSlab.class, concrete_double_slab.getTranslationKey());
		GameRegistry.registerBlock(concrete_brick_slab, ItemModSlab.class, concrete_brick_slab.getTranslationKey());
		GameRegistry.registerBlock(concrete_brick_double_slab, ItemModSlab.class, concrete_brick_double_slab.getTranslationKey());
		GameRegistry.registerBlock(brick_slab, ItemModSlab.class, brick_slab.getTranslationKey());
		GameRegistry.registerBlock(brick_double_slab, ItemModSlab.class, brick_double_slab.getTranslationKey());

		GameRegistry.registerBlock(concrete_smooth_stairs, concrete_smooth_stairs.getTranslationKey());
		GameRegistry.registerBlock(concrete_stairs, concrete_stairs.getTranslationKey());
		GameRegistry.registerBlock(concrete_asbestos_stairs, concrete_asbestos_stairs.getTranslationKey());
		GameRegistry.registerBlock(ducrete_smooth_stairs, ducrete_smooth_stairs.getTranslationKey());
		GameRegistry.registerBlock(brick_concrete_stairs, brick_concrete_stairs.getTranslationKey());
		GameRegistry.registerBlock(brick_concrete_mossy_stairs, brick_concrete_mossy_stairs.getTranslationKey());
		GameRegistry.registerBlock(brick_concrete_cracked_stairs, brick_concrete_cracked_stairs.getTranslationKey());
		GameRegistry.registerBlock(brick_concrete_broken_stairs, brick_concrete_broken_stairs.getTranslationKey());
		GameRegistry.registerBlock(brick_ducrete_stairs, brick_ducrete_stairs.getTranslationKey());
		GameRegistry.registerBlock(reinforced_stone_stairs, reinforced_stone_stairs.getTranslationKey());
		GameRegistry.registerBlock(reinforced_brick_stairs, reinforced_brick_stairs.getTranslationKey());
		GameRegistry.registerBlock(brick_obsidian_stairs, brick_obsidian_stairs.getTranslationKey());
		GameRegistry.registerBlock(brick_light_stairs, brick_light_stairs.getTranslationKey());
		GameRegistry.registerBlock(brick_compound_stairs, brick_compound_stairs.getTranslationKey());
		GameRegistry.registerBlock(brick_asbestos_stairs, brick_asbestos_stairs.getTranslationKey());
		GameRegistry.registerBlock(brick_fire_stairs, brick_fire_stairs.getTranslationKey());
		GameRegistry.registerBlock(ducrete_stairs, ducrete_stairs.getTranslationKey());
		GameRegistry.registerBlock(asphalt_stairs, asphalt_stairs.getTranslationKey());

		//CMB Building Elements
		GameRegistry.registerBlock(cmb_brick, ItemBlockBlastInfo.class, cmb_brick.getTranslationKey());
		GameRegistry.registerBlock(cmb_brick_reinforced, ItemBlockBlastInfo.class, cmb_brick_reinforced.getTranslationKey());

		//Tiles
		GameRegistry.registerBlock(vinyl_tile, ItemBlockBlastInfo.class, vinyl_tile.getTranslationKey()); //i would rather die than dip into fucking blocks with subtypes again
		GameRegistry.registerBlock(vinyl_planks, vinyl_planks.getTranslationKey());
		GameRegistry.registerBlock(pvc_planks, pvc_planks.getTranslationKey());

		GameRegistry.registerBlock(tile_lab, tile_lab.getTranslationKey());
		GameRegistry.registerBlock(tile_lab_cracked, tile_lab_cracked.getTranslationKey());
		GameRegistry.registerBlock(tile_lab_broken, tile_lab_broken.getTranslationKey());

		//Other defensive stuff
		GameRegistry.registerBlock(barbed_wire, barbed_wire.getTranslationKey());
		GameRegistry.registerBlock(barbed_wire_fire, barbed_wire_fire.getTranslationKey());
		GameRegistry.registerBlock(barbed_wire_poison, barbed_wire_poison.getTranslationKey());
		GameRegistry.registerBlock(barbed_wire_acid, barbed_wire_acid.getTranslationKey());
		GameRegistry.registerBlock(barbed_wire_wither, barbed_wire_wither.getTranslationKey());
		GameRegistry.registerBlock(barbed_wire_ultradeath, barbed_wire_ultradeath.getTranslationKey());
		GameRegistry.registerBlock(spikes, spikes.getTranslationKey());
		GameRegistry.registerBlock(tesla, tesla.getTranslationKey());

		//Charger
		GameRegistry.registerBlock(charger, charger.getTranslationKey());
		GameRegistry.registerBlock(refueler, refueler.getTranslationKey());
		//GameRegistry.registerBlock(floodlight, floodlight.getTranslationKey());

		//Decoration Blocks
		GameRegistry.registerBlock(block_meteor, block_meteor.getTranslationKey());
		GameRegistry.registerBlock(block_meteor_cobble, block_meteor_cobble.getTranslationKey());
		GameRegistry.registerBlock(block_meteor_broken, block_meteor_broken.getTranslationKey());
		GameRegistry.registerBlock(block_meteor_molten, block_meteor_molten.getTranslationKey());
		GameRegistry.registerBlock(block_meteor_treasure, block_meteor_treasure.getTranslationKey());
		GameRegistry.registerBlock(meteor_polished, meteor_polished.getTranslationKey());
		GameRegistry.registerBlock(meteor_brick, meteor_brick.getTranslationKey());
		GameRegistry.registerBlock(meteor_brick_mossy, meteor_brick_mossy.getTranslationKey());
		GameRegistry.registerBlock(meteor_brick_cracked, meteor_brick_cracked.getTranslationKey());
		GameRegistry.registerBlock(meteor_brick_chiseled, meteor_brick_chiseled.getTranslationKey());
		GameRegistry.registerBlock(meteor_pillar, meteor_pillar.getTranslationKey());
		GameRegistry.registerBlock(meteor_spawner, meteor_spawner.getTranslationKey());
		GameRegistry.registerBlock(meteor_battery, ItemBlockLore.class, meteor_battery.getTranslationKey());
		GameRegistry.registerBlock(brick_jungle, brick_jungle.getTranslationKey());
		GameRegistry.registerBlock(brick_jungle_cracked, brick_jungle_cracked.getTranslationKey());
		GameRegistry.registerBlock(brick_jungle_fragile, brick_jungle_fragile.getTranslationKey());
		GameRegistry.registerBlock(brick_jungle_lava, brick_jungle_lava.getTranslationKey());
		GameRegistry.registerBlock(brick_jungle_ooze, brick_jungle_ooze.getTranslationKey());
		GameRegistry.registerBlock(brick_jungle_mystic, brick_jungle_mystic.getTranslationKey());
		GameRegistry.registerBlock(brick_jungle_trap, ItemTrapBlock.class, brick_jungle_trap.getTranslationKey());
		GameRegistry.registerBlock(brick_jungle_glyph, ItemGlyphBlock.class, brick_jungle_glyph.getTranslationKey());
		GameRegistry.registerBlock(brick_jungle_circle, brick_jungle_circle.getTranslationKey());
		GameRegistry.registerBlock(brick_red, brick_red.getTranslationKey());
		register(deco_computer);
		register(deco_crt);
		register(deco_toaster);
		GameRegistry.registerBlock(filing_cabinet, ItemBlockBase.class, filing_cabinet.getTranslationKey());
		GameRegistry.registerBlock(tape_recorder, tape_recorder.getTranslationKey());
		GameRegistry.registerBlock(steel_poles, steel_poles.getTranslationKey());
		GameRegistry.registerBlock(pole_top, pole_top.getTranslationKey());
		GameRegistry.registerBlock(pole_satellite_receiver, pole_satellite_receiver.getTranslationKey());
		GameRegistry.registerBlock(steel_wall, steel_wall.getTranslationKey());
		GameRegistry.registerBlock(steel_corner, steel_corner.getTranslationKey());
		GameRegistry.registerBlock(steel_roof, steel_roof.getTranslationKey());
		GameRegistry.registerBlock(steel_beam, steel_beam.getTranslationKey());
		register(steel_scaffold);
		register(steel_grate, ItemBlock.class);
		register(steel_grate_wide);
		//register(scaffold_dynamic);
		/*
		 * operation: pipe annihilation
		 * each pipe has three rotations, and there's four shapes, so in theory, we only need 12 metas
		 * which leaves color, but that's the least bad thing to break due to tile syncing issues, so we
		 * can handle that using tile entities. it's unlikely that we add new shapes or invent new spacial
		 * axis to align them to, so it's also the easiest to extend
		 */
		GameRegistry.registerBlock(deco_pipe, ItemBlockBase.class, deco_pipe.getTranslationKey());
		GameRegistry.registerBlock(deco_pipe_rusted, ItemBlockBase.class, deco_pipe_rusted.getTranslationKey());
		GameRegistry.registerBlock(deco_pipe_green, ItemBlockBase.class, deco_pipe_green.getTranslationKey());
		GameRegistry.registerBlock(deco_pipe_green_rusted, ItemBlockBase.class, deco_pipe_green_rusted.getTranslationKey());
		GameRegistry.registerBlock(deco_pipe_red, ItemBlockBase.class, deco_pipe_red.getTranslationKey());
		GameRegistry.registerBlock(deco_pipe_marked, ItemBlockBase.class, deco_pipe_marked.getTranslationKey());
		GameRegistry.registerBlock(deco_pipe_rim, ItemBlockBase.class, deco_pipe_rim.getTranslationKey());
		GameRegistry.registerBlock(deco_pipe_rim_rusted, ItemBlockBase.class, deco_pipe_rim_rusted.getTranslationKey());
		GameRegistry.registerBlock(deco_pipe_rim_green, ItemBlockBase.class, deco_pipe_rim_green.getTranslationKey());
		GameRegistry.registerBlock(deco_pipe_rim_green_rusted, ItemBlockBase.class, deco_pipe_rim_green_rusted.getTranslationKey());
		GameRegistry.registerBlock(deco_pipe_rim_red, ItemBlockBase.class, deco_pipe_rim_red.getTranslationKey());
		GameRegistry.registerBlock(deco_pipe_rim_marked, ItemBlockBase.class, deco_pipe_rim_marked.getTranslationKey());
		GameRegistry.registerBlock(deco_pipe_framed, ItemBlockBase.class, deco_pipe_framed.getTranslationKey());
		GameRegistry.registerBlock(deco_pipe_framed_rusted, ItemBlockBase.class, deco_pipe_framed_rusted.getTranslationKey());
		GameRegistry.registerBlock(deco_pipe_framed_green, ItemBlockBase.class, deco_pipe_framed_green.getTranslationKey());
		GameRegistry.registerBlock(deco_pipe_framed_green_rusted, ItemBlockBase.class, deco_pipe_framed_green_rusted.getTranslationKey());
		GameRegistry.registerBlock(deco_pipe_framed_red, ItemBlockBase.class, deco_pipe_framed_red.getTranslationKey());
		GameRegistry.registerBlock(deco_pipe_framed_marked, ItemBlockBase.class, deco_pipe_framed_marked.getTranslationKey());
		GameRegistry.registerBlock(deco_pipe_quad, ItemBlockBase.class, deco_pipe_quad.getTranslationKey());
		GameRegistry.registerBlock(deco_pipe_quad_rusted, ItemBlockBase.class, deco_pipe_quad_rusted.getTranslationKey());
		GameRegistry.registerBlock(deco_pipe_quad_green, ItemBlockBase.class, deco_pipe_quad_green.getTranslationKey());
		GameRegistry.registerBlock(deco_pipe_quad_green_rusted, ItemBlockBase.class, deco_pipe_quad_green_rusted.getTranslationKey());
		GameRegistry.registerBlock(deco_pipe_quad_red, ItemBlockBase.class, deco_pipe_quad_red.getTranslationKey());
		GameRegistry.registerBlock(deco_pipe_quad_marked, ItemBlockBase.class, deco_pipe_quad_marked.getTranslationKey());
		register(plant_flower);
		register(plant_tall);
		register(plant_tall_laythe);
		register(laythe_kelp);
		register(laythe_short);
		register(laythe_glow);
		register(laythe_coral);
		register(laythe_coral_block);
		register(plant_dead);
		register(rubber_plant);
		register(reeds);
		register(crop_strawberry);
		register(crop_mint);
		register(crop_paraffin);
		register(crop_coffee);
		register(crop_tea);
		register(vine_phosphor);
		GameRegistry.registerBlock(mush, mush.getTranslationKey());
		GameRegistry.registerBlock(mush_block, mush_block.getTranslationKey());
		GameRegistry.registerBlock(mush_block_stem, mush_block_stem.getTranslationKey());
		register(glyphid_base);
		register(glyphid_spawner);
		GameRegistry.registerBlock(moon_turf, moon_turf.getTranslationKey());

		GameRegistry.registerBlock(moon_rock, moon_rock.getTranslationKey());
		register(sapling_pvc);

		GameRegistry.registerBlock(duna_sands, duna_sands.getTranslationKey());
		GameRegistry.registerBlock(duna_cobble, duna_cobble.getTranslationKey());
		GameRegistry.registerBlock(duna_rock, duna_rock.getTranslationKey());
		GameRegistry.registerBlock(dry_ice, dry_ice.getTranslationKey());
		GameRegistry.registerBlock(ferric_clay, ferric_clay.getTranslationKey());

		GameRegistry.registerBlock(eve_silt, eve_silt.getTranslationKey());
		GameRegistry.registerBlock(eve_rock, eve_rock.getTranslationKey());

		GameRegistry.registerBlock(laythe_silt, laythe_silt.getTranslationKey());

		GameRegistry.registerBlock(ike_regolith, ike_regolith.getTranslationKey());
		GameRegistry.registerBlock(ike_stone, ike_stone.getTranslationKey());

		GameRegistry.registerBlock(dres_rock, dres_rock.getTranslationKey());

		GameRegistry.registerBlock(moho_regolith, moho_regolith.getTranslationKey());
		GameRegistry.registerBlock(moho_stone, moho_stone.getTranslationKey());

		GameRegistry.registerBlock(minmus_regolith, minmus_regolith.getTranslationKey());
		GameRegistry.registerBlock(minmus_stone, minmus_stone.getTranslationKey());
		register(minmus_smooth, ItemBlockLore.class);


		//Waste
		GameRegistry.registerBlock(waste_earth, waste_earth.getTranslationKey());
		GameRegistry.registerBlock(waste_mycelium, waste_mycelium.getTranslationKey());
		GameRegistry.registerBlock(waste_trinitite, waste_trinitite.getTranslationKey());
		GameRegistry.registerBlock(waste_trinitite_red, waste_trinitite_red.getTranslationKey());
		GameRegistry.registerBlock(waste_log, waste_log.getTranslationKey());
		GameRegistry.registerBlock(waste_leaves, waste_leaves.getTranslationKey());
		GameRegistry.registerBlock(waste_planks, waste_planks.getTranslationKey());
		GameRegistry.registerBlock(frozen_grass, frozen_grass.getTranslationKey());
		GameRegistry.registerBlock(frozen_dirt, frozen_dirt.getTranslationKey());
		GameRegistry.registerBlock(frozen_log, frozen_log.getTranslationKey());
		GameRegistry.registerBlock(frozen_planks, frozen_planks.getTranslationKey());
		GameRegistry.registerBlock(dirt_dead, dirt_dead.getTranslationKey());
		GameRegistry.registerBlock(dirt_oily, dirt_oily.getTranslationKey());
		GameRegistry.registerBlock(sand_dirty, sand_dirty.getTranslationKey());
		GameRegistry.registerBlock(sand_dirty_red, sand_dirty_red.getTranslationKey());
		GameRegistry.registerBlock(stone_cracked, stone_cracked.getTranslationKey());
		GameRegistry.registerBlock(fallout, fallout.getTranslationKey());
		GameRegistry.registerBlock(salted_fallout, salted_fallout.getTranslationKey());
		GameRegistry.registerBlock(foam_layer, foam_layer.getTranslationKey());
		GameRegistry.registerBlock(sand_boron_layer, sand_boron_layer.getTranslationKey());
		GameRegistry.registerBlock(leaves_layer, leaves_layer.getTranslationKey());
		GameRegistry.registerBlock(oil_spill, oil_spill.getTranslationKey());
		GameRegistry.registerBlock(burning_earth, burning_earth.getTranslationKey());
		GameRegistry.registerBlock(tektite, tektite.getTranslationKey());
		GameRegistry.registerBlock(ore_tektite_osmiridium, ore_tektite_osmiridium.getTranslationKey());
		GameRegistry.registerBlock(impact_dirt, impact_dirt.getTranslationKey());
		GameRegistry.registerBlock(tumor, tumor.getTranslationKey());


		GameRegistry.registerBlock(primed_log, primed_log.getTranslationKey());
		GameRegistry.registerBlock(eu_log, eu_log.getTranslationKey());
		GameRegistry.registerBlock(bf_log, bf_log.getTranslationKey());
		GameRegistry.registerBlock(lattice_log, lattice_log.getTranslationKey());

		GameRegistry.registerBlock(pvc_log, pvc_log.getTranslationKey());
		GameRegistry.registerBlock(rubber_leaves, rubber_leaves.getTranslationKey());
		GameRegistry.registerBlock(rubber_grass, rubber_grass.getTranslationKey());
		GameRegistry.registerBlock(rubber_silt, rubber_silt.getTranslationKey());
		GameRegistry.registerBlock(spike_cacti, ItemBlockBase.class, spike_cacti.getTranslationKey());
		GameRegistry.registerBlock(rubber_farmland, rubber_farmland.getTranslationKey());
		GameRegistry.registerBlock(vinyl_sand, vinyl_sand.getTranslationKey());
		GameRegistry.registerBlock(vinyl_vines, vinyl_vines.getTranslationKey());
		GameRegistry.registerBlock(vinyl_log, vinyl_log.getTranslationKey());
		GameRegistry.registerBlock(pet_leaves, pet_leaves.getTranslationKey());

		GameRegistry.registerBlock(astroturf, astroturf.getTranslationKey());

		//RAD
		register(sellafield_slaked);
		register(ore_sellafield_diamond);
		register(sellafield_bedrock);
		register(ore_sellafield_emerald);
		register(ore_sellafield_uranium_scorched);
		register(ore_sellafield_schrabidium);
		register(ore_sellafield_radgem);
		GameRegistry.registerBlock(sellafield, ItemBlockNamedMeta.class, sellafield.getTranslationKey());

		//Geysirs
		GameRegistry.registerBlock(geysir_water, geysir_water.getTranslationKey());
		GameRegistry.registerBlock(geysir_chlorine, geysir_chlorine.getTranslationKey());
		GameRegistry.registerBlock(geysir_vapor, geysir_vapor.getTranslationKey());
		GameRegistry.registerBlock(geysir_nether, geysir_nether.getTranslationKey());
		GameRegistry.registerBlock(geysir_volcanic, geysir_volcanic.getTranslationKey());
		GameRegistry.registerBlock(geysir_electric, geysir_electric.getTranslationKey());
		GameRegistry.registerBlock(geysir_chloric, geysir_chloric.getTranslationKey());

		//Nukes
		GameRegistry.registerBlock(nuke_gadget, nuke_gadget.getTranslationKey());
		GameRegistry.registerBlock(nuke_boy, nuke_boy.getTranslationKey());
		GameRegistry.registerBlock(nuke_man, nuke_man.getTranslationKey());
		GameRegistry.registerBlock(nuke_mike, nuke_mike.getTranslationKey());
		GameRegistry.registerBlock(nuke_tsar, nuke_tsar.getTranslationKey());
		GameRegistry.registerBlock(nuke_prototype, ItemPrototypeBlock.class, nuke_prototype.getTranslationKey());
		GameRegistry.registerBlock(nuke_fleija, nuke_fleija.getTranslationKey());
		GameRegistry.registerBlock(nuke_solinium, nuke_solinium.getTranslationKey());
		GameRegistry.registerBlock(nuke_n2, nuke_n2.getTranslationKey());
		GameRegistry.registerBlock(nuke_antimatter, nuke_antimatter.getTranslationKey());
		GameRegistry.registerBlock(nuke_fstbmb, nuke_fstbmb.getTranslationKey());
		GameRegistry.registerBlock(nuke_custom, nuke_custom.getTranslationKey());

		//Generic Bombs
		GameRegistry.registerBlock(bomb_multi, bomb_multi.getTranslationKey());
		register(crashed_balefire);
		GameRegistry.registerBlock(fireworks, fireworks.getTranslationKey());
		GameRegistry.registerBlock(dynamite, dynamite.getTranslationKey());
		GameRegistry.registerBlock(tnt, tnt.getTranslationKey());
		GameRegistry.registerBlock(semtex, semtex.getTranslationKey());
		GameRegistry.registerBlock(c4, c4.getTranslationKey());
		register(fissure_bomb);

		//Turrets
		GameRegistry.registerBlock(turret_chekhov, turret_chekhov.getTranslationKey());
		GameRegistry.registerBlock(turret_friendly, turret_friendly.getTranslationKey());
		GameRegistry.registerBlock(turret_jeremy, turret_jeremy.getTranslationKey());
		GameRegistry.registerBlock(turret_tauon, turret_tauon.getTranslationKey());
		GameRegistry.registerBlock(turret_richard, turret_richard.getTranslationKey());
		GameRegistry.registerBlock(turret_howard, turret_howard.getTranslationKey());
		GameRegistry.registerBlock(turret_howard_damaged, turret_howard_damaged.getTranslationKey());
		GameRegistry.registerBlock(turret_maxwell, turret_maxwell.getTranslationKey());
		GameRegistry.registerBlock(turret_fritz, turret_fritz.getTranslationKey());
		//GameRegistry.registerBlock(turret_brandon, turret_brandon.getTranslationKey());
		GameRegistry.registerBlock(turret_arty, turret_arty.getTranslationKey());
		GameRegistry.registerBlock(turret_himars, turret_himars.getTranslationKey());
		GameRegistry.registerBlock(turret_sentry, turret_sentry.getTranslationKey());
		GameRegistry.registerBlock(turret_sentry_damaged, turret_sentry_damaged.getTranslationKey());

		//Wall-mounted Explosives
		GameRegistry.registerBlock(charge_dynamite, ItemBlockBase.class, charge_dynamite.getTranslationKey());
		GameRegistry.registerBlock(charge_miner, ItemBlockBase.class, charge_miner.getTranslationKey());
		GameRegistry.registerBlock(charge_c4, ItemBlockBase.class, charge_c4.getTranslationKey());
		GameRegistry.registerBlock(charge_semtex, ItemBlockBase.class, charge_semtex.getTranslationKey());

		//Mines
		GameRegistry.registerBlock(mine_ap, mine_ap.getTranslationKey());
		GameRegistry.registerBlock(mine_shrap, mine_shrap.getTranslationKey());
		GameRegistry.registerBlock(mine_he, mine_he.getTranslationKey());
		GameRegistry.registerBlock(mine_fat, mine_fat.getTranslationKey());
		GameRegistry.registerBlock(mine_naval, mine_naval.getTranslationKey());

		//Block Bombs
		GameRegistry.registerBlock(flame_war, flame_war.getTranslationKey());
		GameRegistry.registerBlock(float_bomb, float_bomb.getTranslationKey());
		GameRegistry.registerBlock(therm_endo, therm_endo.getTranslationKey());
		GameRegistry.registerBlock(therm_exo, therm_exo.getTranslationKey());
		GameRegistry.registerBlock(emp_bomb, emp_bomb.getTranslationKey());
		register(det_cord);
		GameRegistry.registerBlock(det_charge, det_charge.getTranslationKey());
		GameRegistry.registerBlock(det_nuke, det_nuke.getTranslationKey());
		GameRegistry.registerBlock(det_salt, det_salt.getTranslationKey());
		GameRegistry.registerBlock(det_miner, det_miner.getTranslationKey());
		GameRegistry.registerBlock(red_barrel, ItemBlockLore.class, red_barrel.getTranslationKey());
		GameRegistry.registerBlock(pink_barrel, ItemBlockLore.class, pink_barrel.getTranslationKey());
		GameRegistry.registerBlock(lox_barrel, ItemBlockLore.class, lox_barrel.getTranslationKey());
		GameRegistry.registerBlock(taint_barrel, taint_barrel.getTranslationKey());
		GameRegistry.registerBlock(yellow_barrel, yellow_barrel.getTranslationKey());
		GameRegistry.registerBlock(vitrified_barrel, vitrified_barrel.getTranslationKey());
		GameRegistry.registerBlock(oil_barrel, oil_barrel.getTranslationKey());

		//Siren
		GameRegistry.registerBlock(machine_siren, machine_siren.getTranslationKey());

		//This Thing
		GameRegistry.registerBlock(broadcaster_pc, broadcaster_pc.getTranslationKey());

		//Geiger Counter
		GameRegistry.registerBlock(geiger, geiger.getTranslationKey());

		//HEV Battery
		GameRegistry.registerBlock(hev_battery, hev_battery.getTranslationKey());

		//Chainlink Fence
		GameRegistry.registerBlock(fence_metal, ItemBlockBase.class, fence_metal.getTranslationKey());

		//Sands, Glass
		GameRegistry.registerBlock(ash_digamma, ash_digamma.getTranslationKey());
		register(sand_mix);
		register(glass_boron, ItemBlock.class);
		register(glass_lead, ItemBlock.class);
		register(glass_uranium, ItemBlock.class);
		register(glass_trinitite, ItemBlock.class);
		register(glass_polonium, ItemBlock.class);
		register(glass_ash, ItemBlock.class);
		register(glass_quartz, ItemBlock.class);
		register(glass_polarized, ItemBlock.class);

		//Silo Hatch
		GameRegistry.registerBlock(seal_frame, seal_frame.getTranslationKey());
		GameRegistry.registerBlock(seal_controller, seal_controller.getTranslationKey());
		GameRegistry.registerBlock(seal_hatch, seal_hatch.getTranslationKey());

		//Vault Door
		GameRegistry.registerBlock(vault_door, vault_door.getTranslationKey());
		GameRegistry.registerBlock(blast_door, blast_door.getTranslationKey());
		GameRegistry.registerBlock(fire_door, fire_door.getTranslationKey());
		GameRegistry.registerBlock(transition_seal, transition_seal.getTranslationKey());
		GameRegistry.registerBlock(silo_hatch, silo_hatch.getTranslationKey());
		GameRegistry.registerBlock(silo_hatch_large, silo_hatch_large.getTranslationKey());
		GameRegistry.registerBlock(sliding_blast_door, sliding_blast_door.getTranslationKey());

		//Doors
		GameRegistry.registerBlock(door_metal, door_metal.getTranslationKey());
		GameRegistry.registerBlock(door_office, door_office.getTranslationKey());
		GameRegistry.registerBlock(door_bunker, door_bunker.getTranslationKey());
		GameRegistry.registerBlock(door_red, door_red.getTranslationKey());
		GameRegistry.registerBlock(fence_gate, fence_gate.getTranslationKey());
		GameRegistry.registerBlock(secure_access_door, secure_access_door.getTranslationKey());
		GameRegistry.registerBlock(large_vehicle_door, large_vehicle_door.getTranslationKey());
		GameRegistry.registerBlock(qe_containment, qe_containment.getTranslationKey());
		GameRegistry.registerBlock(qe_sliding_door, qe_sliding_door.getTranslationKey());
		GameRegistry.registerBlock(round_airlock_door, round_airlock_door.getTranslationKey());
		GameRegistry.registerBlock(sliding_seal_door, sliding_seal_door.getTranslationKey());
		GameRegistry.registerBlock(water_door, water_door.getTranslationKey());

		//Crates
		register(crate_iron, ItemBlockStorageCrate.class);
		register(crate_steel, ItemBlockStorageCrate.class);
		register(crate_desh, ItemBlockStorageCrate.class);
		register(crate_tungsten, ItemBlockStorageCrate.class);
		register(crate_template, ItemBlockStorageCrate.class);
		register(safe, ItemBlockStorageCrate.class);
		register(mass_storage, ItemBlockStorageCrate.class);

		//Junk
		GameRegistry.registerBlock(boxcar, boxcar.getTranslationKey());
		GameRegistry.registerBlock(boat, boat.getTranslationKey());

		//Machines
		register(machine_autocrafter);
		register(machine_funnel);
		register(war_controller);

		register(anvil_iron);
		register(anvil_lead);
		register(anvil_steel);
		register(anvil_desh);
		register(anvil_ferrouranium);
		register(anvil_saturnite);
		register(anvil_bismuth_bronze);
		register(anvil_arsenic_bronze);
		register(anvil_schrabidate);
		register(anvil_dnt);
		register(anvil_osmiridium);
		register(anvil_murky);

		GameRegistry.registerBlock(press_preheater, press_preheater.getTranslationKey());
		GameRegistry.registerBlock(machine_press, machine_press.getTranslationKey());
		GameRegistry.registerBlock(machine_epress, machine_epress.getTranslationKey());
		register(machine_conveyor_press);
		register(machine_ammo_press);
		register(pump_steam);
		register(pump_electric);
		register(heater_firebox);
		register(heater_oven);
		register(machine_ashpit);
		register(heater_oilburner);
		register(heater_electric);
		register(heater_heatex);
		register(furnace);
		register(lit_furnace);
		register(furnace_iron);
		register(furnace_steel);
		register(furnace_combination);
		register(machine_stirling);
		register(machine_stirling_steel);
		register(machine_stirling_creative);
		register(machine_sawmill);
		register(machine_crucible);
		register(machine_strand_caster);
		register(machine_boiler);
		register(machine_industrial_boiler);
		register(foundry_mold);
		register(foundry_basin);
		register(foundry_channel);
		register(foundry_tank);
		register(foundry_outlet);
		register(foundry_slagtap);
		register(slag);
		register(machine_furnace_brick_off);
		register(machine_furnace_brick_on);
		register(machine_difurnace_off);
		register(machine_difurnace_on);
		register(machine_difurnace_extension);
		GameRegistry.registerBlock(machine_difurnace_rtg_off, machine_difurnace_rtg_off.getTranslationKey());
		GameRegistry.registerBlock(machine_difurnace_rtg_on, machine_difurnace_rtg_on.getTranslationKey());
		GameRegistry.registerBlock(machine_centrifuge, machine_centrifuge.getTranslationKey());
		GameRegistry.registerBlock(machine_gascent, machine_gascent.getTranslationKey());
		GameRegistry.registerBlock(machine_fel, machine_fel.getTranslationKey());
		GameRegistry.registerBlock(machine_silex, machine_silex.getTranslationKey());
		register(machine_rotary_furnace);
		GameRegistry.registerBlock(machine_crystallizer, machine_crystallizer.getTranslationKey());
		GameRegistry.registerBlock(machine_uf6_tank, machine_uf6_tank.getTranslationKey());
		GameRegistry.registerBlock(machine_puf6_tank, machine_puf6_tank.getTranslationKey());
		GameRegistry.registerBlock(machine_reactor_breeding, machine_reactor_breeding.getTranslationKey());
		GameRegistry.registerBlock(machine_rtg_furnace_off, machine_rtg_furnace_off.getTranslationKey());
		GameRegistry.registerBlock(machine_rtg_furnace_on, machine_rtg_furnace_on.getTranslationKey());
		register(machine_wood_burner);
		register(machine_diesel);
		register(machine_combustion_engine);
		GameRegistry.registerBlock(machine_controller, machine_controller.getTranslationKey());
		register(machine_dish_controller);
		GameRegistry.registerBlock(reactor_research, reactor_research.getTranslationKey());
		GameRegistry.registerBlock(reactor_zirnox, reactor_zirnox.getTranslationKey());
		GameRegistry.registerBlock(zirnox_destroyed, zirnox_destroyed.getTranslationKey());
		GameRegistry.registerBlock(machine_industrial_generator, machine_industrial_generator.getTranslationKey());
		GameRegistry.registerBlock(machine_radgen, machine_radgen.getTranslationKey());
		GameRegistry.registerBlock(machine_cyclotron, machine_cyclotron.getTranslationKey());
		GameRegistry.registerBlock(machine_exposure_chamber, machine_exposure_chamber.getTranslationKey());
		GameRegistry.registerBlock(machine_rtg_grey, machine_rtg_grey.getTranslationKey());
		GameRegistry.registerBlock(machine_minirtg, machine_minirtg.getTranslationKey());
		GameRegistry.registerBlock(machine_powerrtg, machine_powerrtg.getTranslationKey());
		GameRegistry.registerBlock(machine_radiolysis, machine_radiolysis.getTranslationKey());
		GameRegistry.registerBlock(machine_hephaestus, machine_hephaestus.getTranslationKey());
		GameRegistry.registerBlock(machine_solar, machine_solar.getTranslationKey());
		register(machine_stardar);
		register(machine_drive_processor);
		GameRegistry.registerBlock(machine_alkylation, machine_alkylation.getTranslationKey());

		register(hadron_coil_alloy);
		register(hadron_coil_gold);
		register(hadron_coil_neodymium);
		register(hadron_coil_magtung);
		register(hadron_coil_schrabidium);
		register(hadron_coil_schrabidate);
		register(hadron_coil_starmetal);
		register(hadron_coil_chlorophyte);
		register(hadron_coil_mese);

		register(pa_source);
		register(pa_beamline);
		register(pa_rfc);
		register(pa_quadrupole);
		register(pa_dipole);
		register(pa_detector);

		register(rbmk_rod);
		register(rbmk_rod_mod);
		register(rbmk_rod_reasim);
		register(rbmk_rod_reasim_mod);
		register(rbmk_control);
		register(rbmk_control_mod);
		register(rbmk_control_auto);
		register(rbmk_control_reasim);
		register(rbmk_control_reasim_auto);
		register(rbmk_blank);
		register(rbmk_boiler);
		register(rbmk_reflector);
		register(rbmk_absorber);
		register(rbmk_moderator);
		register(rbmk_outgasser);
		register(rbmk_storage);
		register(rbmk_cooler);
		register(rbmk_heater);
		GameRegistry.registerBlock(rbmk_console, rbmk_console.getTranslationKey());
		GameRegistry.registerBlock(rbmk_crane_console, rbmk_crane_console.getTranslationKey());
		register(rbmk_display);
		register(rbmk_key_pad);
		register(rbmk_gauge);
		register(rbmk_autoloader);
		register(rbmk_loader);
		register(rbmk_steam_inlet);
		register(rbmk_steam_outlet);
		GameRegistry.registerBlock(pribris, pribris.getTranslationKey());
		GameRegistry.registerBlock(pribris_burning, pribris_burning.getTranslationKey());
		GameRegistry.registerBlock(pribris_radiating, pribris_radiating.getTranslationKey());
		GameRegistry.registerBlock(pribris_digamma, pribris_digamma.getTranslationKey());

		GameRegistry.registerBlock(red_cable, red_cable.getTranslationKey());
		GameRegistry.registerBlock(red_cable_classic, red_cable_classic.getTranslationKey());
		register(red_cable_paintable, ItemBlock.class);
		register(red_cable_gauge);
		register(red_cable_box);
		register(red_wire_coated, ItemBlock.class);
		register(red_connector);
		register(red_connector_super);
		GameRegistry.registerBlock(red_pylon, ItemBlockBase.class, red_pylon.getTranslationKey());
		register(red_pylon_medium_wood);
		register(red_pylon_medium_wood_transformer);
		register(red_pylon_medium_steel);
		register(red_pylon_medium_steel_transformer);
		GameRegistry.registerBlock(red_pylon_large, ItemBlockBase.class, red_pylon_large.getTranslationKey());
		GameRegistry.registerBlock(substation, ItemBlockBase.class, substation.getTranslationKey());
		GameRegistry.registerBlock(cable_switch, cable_switch.getTranslationKey());
		GameRegistry.registerBlock(cable_detector, cable_detector.getTranslationKey());
		register(cable_diode);
		GameRegistry.registerBlock(machine_detector, machine_detector.getTranslationKey());

		register(fluid_duct_neo);
		register(fluid_duct_box);
		register(fluid_duct_exhaust);
		register(fluid_duct_paintable_block_exhaust);
		register(fluid_duct_paintable);
		register(pipe_anchor);
		register(fluid_duct_gauge);
		register(fluid_valve);
		register(fluid_counter_valve);
		register(fluid_switch);
		register(fluid_pump);
		register(machine_drain);
		register(radio_torch_sender);
		register(radio_torch_receiver);
		register(radio_torch_counter);
		register(radio_torch_logic);
		register(radio_torch_reader);
		register(radio_torch_controller);
		register(radio_telex);

		register(crane_extractor);
		register(crane_inserter);
		register(crane_grabber);
		register(crane_router);
		register(crane_boxer);
		register(crane_unboxer);
		register(conveyor);
		register(conveyor_express);
		register(conveyor_double);
		register(conveyor_triple);
		register(conveyor_chute);
		register(conveyor_lift);
		register(crane_splitter);
		register(crane_partitioner);
		register(drone_waypoint);
		register(drone_crate);
		register(drone_waypoint_request);
		register(drone_dock);
		register(drone_crate_provider);
		register(drone_crate_requester);
		register(pneumatic_tube);
		register(pneumatic_tube_paintable);
		register(fan);
		register(piston_inserter);

		GameRegistry.registerBlock(chain, chain.getTranslationKey());
		GameRegistry.registerBlock(ladder_sturdy, ladder_sturdy.getTranslationKey());
		GameRegistry.registerBlock(ladder_iron, ladder_iron.getTranslationKey());
		GameRegistry.registerBlock(ladder_gold, ladder_gold.getTranslationKey());
		GameRegistry.registerBlock(ladder_titanium, ladder_titanium.getTranslationKey());
		GameRegistry.registerBlock(ladder_copper, ladder_copper.getTranslationKey());
		GameRegistry.registerBlock(ladder_tungsten, ladder_tungsten.getTranslationKey());
		GameRegistry.registerBlock(ladder_aluminium, ladder_aluminium.getTranslationKey());
		GameRegistry.registerBlock(ladder_steel, ladder_steel.getTranslationKey());
		GameRegistry.registerBlock(ladder_lead, ladder_lead.getTranslationKey());
		GameRegistry.registerBlock(ladder_cobalt, ladder_cobalt.getTranslationKey());
		GameRegistry.registerBlock(trapdoor_steel, trapdoor_steel.getTranslationKey());

		register(barrel_plastic);
		register(barrel_corroded);
		register(barrel_iron);
		register(barrel_steel);
		register(barrel_tcalloy);
		register(barrel_antimatter);
		register(machine_battery_socket);
		register(machine_battery_redd);
		register(machine_battery_potato);
		register(machine_battery);
		register(machine_lithium_battery);
		register(machine_schrabidium_battery);
		register(machine_dineutronium_battery);
		register(machine_fensu);
		register(capacitor_bus);
		register(capacitor_copper);
		register(capacitor_gold);
		register(capacitor_niobium);
		register(capacitor_tantalium);
		register(capacitor_complex);

		register(capacitor_schrabidate);
		GameRegistry.registerBlock(machine_transformer, machine_transformer.getTranslationKey());
		GameRegistry.registerBlock(machine_converter_he_rf, machine_converter_he_rf.getTranslationKey());
		GameRegistry.registerBlock(machine_converter_rf_he, machine_converter_rf_he.getTranslationKey());
		GameRegistry.registerBlock(machine_electric_furnace_off, machine_electric_furnace_off.getTranslationKey());
		GameRegistry.registerBlock(machine_electric_furnace_on, machine_electric_furnace_on.getTranslationKey());
		GameRegistry.registerBlock(machine_microwave, machine_microwave.getTranslationKey());
		register(machine_assembly_machine);
		register(machine_assembly_factory);
		register(machine_precass);
		register(machine_chemical_plant);
		register(machine_chemical_factory);
		register(machine_purex);
		register(machine_arc_welder);
		register(machine_soldering_station);
		register(machine_vacuum_circuit);
		register(machine_arc_furnace);
		register(machine_mixer);
		register(machine_fluidtank);
		register(machine_bat9000);
		register(machine_orbus);
		GameRegistry.registerBlock(machine_boiler_off, machine_boiler_off.getTranslationKey());
		register(machine_steam_engine);
		register(machine_turbine);
		register(machine_large_turbine);
		register(machine_industrial_turbine);
		register(machine_chungus);
		GameRegistry.registerBlock(machine_condenser, machine_condenser.getTranslationKey());
		GameRegistry.registerBlock(machine_tower_small, machine_tower_small.getTranslationKey());
		GameRegistry.registerBlock(machine_tower_large, machine_tower_large.getTranslationKey());
		register(machine_condenser_powered);
		register(machine_radiator);
		GameRegistry.registerBlock(machine_deuterium_extractor, machine_deuterium_extractor.getTranslationKey());
		GameRegistry.registerBlock(machine_deuterium_tower, machine_deuterium_tower.getTranslationKey());
		GameRegistry.registerBlock(machine_atmo_tower, machine_atmo_tower.getTranslationKey());
		GameRegistry.registerBlock(machine_atmo_vent, machine_atmo_vent.getTranslationKey());
		register(machine_liquefactor);
		register(machine_solidifier);
		register(machine_intake);
		register(machine_atmo_emitter);
		register(machine_compressor);
		register(machine_compressor_compact);
		GameRegistry.registerBlock(machine_electrolyser, machine_electrolyser.getTranslationKey());
		GameRegistry.registerBlock(machine_waste_drum, machine_waste_drum.getTranslationKey());
		GameRegistry.registerBlock(machine_storage_drum, machine_storage_drum.getTranslationKey());
		GameRegistry.registerBlock(machine_shredder, machine_shredder.getTranslationKey());
		register(machine_well);
		register(machine_pumpjack);
		register(machine_fracking_tower);
		register(machine_flare);
		register(chimney_brick);
		register(chimney_industrial);
		register(machine_refinery);
		register(machine_vacuum_distill);
		register(machine_fraction_tower);
		register(fraction_spacer);
		register(machine_catalytic_cracker);
		register(machine_catalytic_reformer);
		register(machine_milk_reformer);
		register(machine_cryo_distill);
		register(machine_hydrotreater);
		register(machine_coker);
		register(machine_pyrooven);
		register(machine_autosaw);
		register(machine_excavator);
		register(machine_ore_slopper);
		register(machine_annihilator);
		register(machine_mining_laser);
		register(barricade);
		register(machine_turbofan);
		register(machine_turbinegas);
		register(machine_lpw2);
		register(machine_htr3);
		register(machine_htrf4);
		register(machine_htrf4neo);
		register(machine_xenon_thruster);
		register(machine_magma);

		GameRegistry.registerBlock(machine_teleporter, machine_teleporter.getTranslationKey());
		GameRegistry.registerBlock(teleanchor, teleanchor.getTranslationKey());
		GameRegistry.registerBlock(field_disturber, field_disturber.getTranslationKey());
		GameRegistry.registerBlock(troll_disturber, troll_disturber.getTranslationKey());
		GameRegistry.registerBlock(machine_satlinker, machine_satlinker.getTranslationKey());
		GameRegistry.registerBlock(machine_keyforge, machine_keyforge.getTranslationKey());
		GameRegistry.registerBlock(machine_armor_table, machine_armor_table.getTranslationKey());
		GameRegistry.registerBlock(machine_weapon_table, machine_weapon_table.getTranslationKey());
		GameRegistry.registerBlock(machine_forcefield, machine_forcefield.getTranslationKey());
		GameRegistry.registerBlock(radiorec, radiorec.getTranslationKey());
		GameRegistry.registerBlock(radiobox, radiobox.getTranslationKey());

		//Multiblock Parts
		GameRegistry.registerBlock(struct_launcher, struct_launcher.getTranslationKey());
		GameRegistry.registerBlock(struct_scaffold, struct_scaffold.getTranslationKey());
		GameRegistry.registerBlock(struct_launcher_core, struct_launcher_core.getTranslationKey());
		GameRegistry.registerBlock(struct_launcher_core_large, struct_launcher_core_large.getTranslationKey());
		GameRegistry.registerBlock(struct_soyuz_core, struct_soyuz_core.getTranslationKey());
		register(struct_torus_core);
		GameRegistry.registerBlock(struct_watz_core, struct_watz_core.getTranslationKey());
		GameRegistry.registerBlock(struct_icf_core, struct_icf_core.getTranslationKey());

		//Absorbers
		GameRegistry.registerBlock(absorber, absorber.getTranslationKey());
		GameRegistry.registerBlock(absorber_red, absorber_red.getTranslationKey());
		GameRegistry.registerBlock(absorber_green, absorber_green.getTranslationKey());
		GameRegistry.registerBlock(absorber_pink, absorber_pink.getTranslationKey());
		GameRegistry.registerBlock(rad_absorber, ItemBlockBase.class, rad_absorber.getTranslationKey());
		GameRegistry.registerBlock(decon, decon.getTranslationKey());

		//Solar Tower Blocks
		GameRegistry.registerBlock(machine_solar_boiler, machine_solar_boiler.getTranslationKey());
		GameRegistry.registerBlock(solar_mirror, solar_mirror.getTranslationKey());

		//Literal fucking garbage
		GameRegistry.registerBlock(factory_titanium_hull, factory_titanium_hull.getTranslationKey());
		GameRegistry.registerBlock(factory_advanced_hull, factory_advanced_hull.getTranslationKey());

		//CM stuff
		register(custom_machine, ItemCustomMachine.class);
		register(cm_block);
		register(cm_sheet);
		register(cm_engine);
		register(cm_tank);
		register(cm_circuit);
		register(cm_port);
		register(cm_flux);
		register(cm_heat);
		register(cm_anchor);

		//PWR
		register(pwr_fuel);
		register(pwr_control);
		register(pwr_channel);
		register(pwr_heatex);
		register(pwr_heatsink);
		register(pwr_neutron_source);
		register(pwr_reflector);
		register(pwr_casing);
		register(pwr_port);
		register(pwr_controller);
		register(pwr_block);

		//Multiblock Generators
		GameRegistry.registerBlock(fusion_heater, fusion_heater.getTranslationKey());
		GameRegistry.registerBlock(fusion_hatch, fusion_hatch.getTranslationKey());
		GameRegistry.registerBlock(plasma, ItemBlockLore.class, plasma.getTranslationKey());
		GameRegistry.registerBlock(iter, iter.getTranslationKey());
		GameRegistry.registerBlock(plasma_heater, plasma_heater.getTranslationKey());

		register(fusion_component);
		register(fusion_torus);
		register(fusion_klystron);
		register(fusion_klystron_creative);
		register(fusion_breeder);
		register(fusion_collector);
		register(fusion_boiler);
		register(fusion_mhdt);
		register(fusion_coupler);

		register(watz_element);
		register(watz_cooler);
		register(watz_end);
		register(watz);
		register(watz_pump);

		register(machine_icf_press);
		register(icf_laser_component);
		register(icf_controller);
		register(icf_block);
		register(icf_component);
		register(icf);

		//E
		GameRegistry.registerBlock(balefire, balefire.getTranslationKey());
		GameRegistry.registerBlock(fire_digamma, fire_digamma.getTranslationKey());
		GameRegistry.registerBlock(digamma_matter, digamma_matter.getTranslationKey());
		register(volcano_core);
		register(volcano_rad_core);

		//Dark Fusion Core
		GameRegistry.registerBlock(dfc_emitter, dfc_emitter.getTranslationKey());
		GameRegistry.registerBlock(dfc_injector, dfc_injector.getTranslationKey());
		GameRegistry.registerBlock(dfc_receiver, dfc_receiver.getTranslationKey());
		GameRegistry.registerBlock(dfc_stabilizer, dfc_stabilizer.getTranslationKey());
		GameRegistry.registerBlock(dfc_core, dfc_core.getTranslationKey());

		//Missile Blocks
		GameRegistry.registerBlock(machine_missile_assembly, machine_missile_assembly.getTranslationKey());
		register(machine_rocket_assembly);
		GameRegistry.registerBlock(launch_pad, launch_pad.getTranslationKey());
		register(launch_pad_rocket);
		GameRegistry.registerBlock(launch_pad_rusted, launch_pad_rusted.getTranslationKey());
		GameRegistry.registerBlock(launch_pad_large, launch_pad_large.getTranslationKey());
		GameRegistry.registerBlock(compact_launcher, compact_launcher.getTranslationKey());
		GameRegistry.registerBlock(launch_table, launch_table.getTranslationKey());
		GameRegistry.registerBlock(soyuz_launcher, soyuz_launcher.getTranslationKey());
		GameRegistry.registerBlock(sat_dock, sat_dock.getTranslationKey());
		GameRegistry.registerBlock(soyuz_capsule, soyuz_capsule.getTranslationKey());
		GameRegistry.registerBlock(combat_drop, combat_drop.getTranslationKey());
		GameRegistry.registerBlock(crate_supply, crate_supply.getTranslationKey());
		GameRegistry.registerBlock(machine_radar, machine_radar.getTranslationKey());
		GameRegistry.registerBlock(machine_radar_large, machine_radar_large.getTranslationKey());
		GameRegistry.registerBlock(radar_screen, radar_screen.getTranslationKey());
		GameRegistry.registerBlock(gas_dock, gas_dock.getTranslationKey());

		//Guide
		GameRegistry.registerBlock(book_guide, book_guide.getTranslationKey());

		//Rails
		register(rail_wood);
		register(rail_narrow);
		register(rail_highspeed);
		register(rail_booster);
		register(rail_narrow_straight);
		register(rail_narrow_curve);
		register(rail_large_straight);
		register(rail_large_straight_short);
		register(rail_large_curve);
		register(rail_large_curve_7);
		register(rail_large_curve_9);
		register(rail_large_ramp);
		register(rail_large_buffer);
		register(rail_large_switch);
		register(rail_large_switch_flipped);

		//Crate
		GameRegistry.registerBlock(crate, crate.getTranslationKey());
		GameRegistry.registerBlock(crate_weapon, crate_weapon.getTranslationKey());
		GameRegistry.registerBlock(crate_lead, crate_lead.getTranslationKey());
		GameRegistry.registerBlock(crate_metal, crate_metal.getTranslationKey());
		GameRegistry.registerBlock(crate_red, crate_red.getTranslationKey());
		GameRegistry.registerBlock(crate_can, crate_can.getTranslationKey());
		GameRegistry.registerBlock(crate_ammo, crate_ammo.getTranslationKey());
		GameRegistry.registerBlock(crate_jungle, crate_jungle.getTranslationKey());

		//ElB
		GameRegistry.registerBlock(statue_elb_f, statue_elb_f.getTranslationKey());

		//Fluids
		GameRegistry.registerBlock(mud_block, mud_block.getTranslationKey());
		GameRegistry.registerBlock(acid_block, acid_block.getTranslationKey());
		GameRegistry.registerBlock(toxic_block, toxic_block.getTranslationKey());
		GameRegistry.registerBlock(schrabidic_block, schrabidic_block.getTranslationKey());
		GameRegistry.registerBlock(corium_block, corium_block.getTranslationKey());
		GameRegistry.registerBlock(volcanic_lava_block, volcanic_lava_block.getTranslationKey());
		GameRegistry.registerBlock(rad_lava_block, rad_lava_block.getTranslationKey());
		GameRegistry.registerBlock(sulfuric_acid_block, sulfuric_acid_block.getTranslationKey());
		GameRegistry.registerBlock(mercury_block, mercury_block.getTranslationKey());
		GameRegistry.registerBlock(bromine_block, bromine_block.getTranslationKey());
		GameRegistry.registerBlock(flesh_block, flesh_block.getTranslationKey());
		GameRegistry.registerBlock(charred_flesh_block, charred_flesh_block.getTranslationKey());
		GameRegistry.registerBlock(carbonized_flesh_block, carbonized_flesh_block.getTranslationKey());
		GameRegistry.registerBlock(ccl_block, ccl_block.getTranslationKey());

		//Multiblock Dummy Blocks
		GameRegistry.registerBlock(dummy_block_blast, dummy_block_blast.getTranslationKey());
		GameRegistry.registerBlock(dummy_plate_compact_launcher, dummy_plate_compact_launcher.getTranslationKey());
		GameRegistry.registerBlock(dummy_port_compact_launcher, dummy_port_compact_launcher.getTranslationKey());
		GameRegistry.registerBlock(dummy_plate_launch_table, dummy_plate_launch_table.getTranslationKey());
		GameRegistry.registerBlock(dummy_port_launch_table, dummy_port_launch_table.getTranslationKey());
		GameRegistry.registerBlock(dummy_plate_cargo, dummy_plate_cargo.getTranslationKey());

		//Other Technical Blocks
		GameRegistry.registerBlock(oil_pipe, oil_pipe.getTranslationKey());
		GameRegistry.registerBlock(vent_chlorine, vent_chlorine.getTranslationKey());
		GameRegistry.registerBlock(vent_cloud, vent_cloud.getTranslationKey());
		GameRegistry.registerBlock(vent_pink_cloud, vent_pink_cloud.getTranslationKey());
		GameRegistry.registerBlock(vent_chlorine_seal, vent_chlorine_seal.getTranslationKey());
		GameRegistry.registerBlock(chlorine_gas, chlorine_gas.getTranslationKey());
		GameRegistry.registerBlock(gas_radon, gas_radon.getTranslationKey());
		GameRegistry.registerBlock(gas_radon_dense, gas_radon_dense.getTranslationKey());
		GameRegistry.registerBlock(gas_radon_tomb, gas_radon_tomb.getTranslationKey());
		GameRegistry.registerBlock(gas_meltdown, gas_meltdown.getTranslationKey());
		GameRegistry.registerBlock(gas_monoxide, gas_monoxide.getTranslationKey());
		GameRegistry.registerBlock(gas_asbestos, gas_asbestos.getTranslationKey());
		GameRegistry.registerBlock(gas_coal, gas_coal.getTranslationKey());
		GameRegistry.registerBlock(gas_flammable, gas_flammable.getTranslationKey());
		GameRegistry.registerBlock(gas_explosive, gas_explosive.getTranslationKey());
		GameRegistry.registerBlock(vacuum, vacuum.getTranslationKey());
		GameRegistry.registerBlock(air_vent, air_vent.getTranslationKey());
		register(air_scrubber);
		register(algae_film);
		register(hydrobay);

		// OC Compat Items
		if(Loader.isModLoaded(Compat.MOD_OC)) {
			register(oc_cable_paintable);
		}

		//???
		GameRegistry.registerBlock(crystal_virus, crystal_virus.getTranslationKey());
		GameRegistry.registerBlock(crystal_hardened, crystal_hardened.getTranslationKey());
		GameRegistry.registerBlock(crystal_pulsar, crystal_pulsar.getTranslationKey());
		register(taint);
		GameRegistry.registerBlock(cheater_virus, cheater_virus.getTranslationKey());
		GameRegistry.registerBlock(cheater_virus_seed, cheater_virus_seed.getTranslationKey());
		GameRegistry.registerBlock(ntm_dirt, ntm_dirt.getTranslationKey());
		GameRegistry.registerBlock(pink_log, pink_log.getTranslationKey());
		GameRegistry.registerBlock(pink_planks, pink_planks.getTranslationKey());
		GameRegistry.registerBlock(pink_slab, pink_slab.getTranslationKey());
		GameRegistry.registerBlock(pink_double_slab, pink_double_slab.getTranslationKey());
		GameRegistry.registerBlock(pink_stairs, pink_stairs.getTranslationKey());

		// Terraforming
		register(atmosphere_editor);

		register(transporter_rocket);
		register(orbital_station);
		register(orbital_station_port);
		register(orbital_station_launcher);
		register(orbital_station_computer);
		register(propulsion_creative);

		register(dyson_launcher);
		register(dyson_receiver);
		register(dyson_converter_tu);
		register(dyson_converter_he);
		register(dyson_converter_anatmogenesis);

		register(orrery);

		register(dummy_beam);

		register(wand_air);
		register(wand_loot);
		register(wand_jigsaw);
		register(wand_logic);
		register(wand_tandem);
		register(wand_structure);

		register(logic_block);
	}

	private static void register(Block b) {
		GameRegistry.registerBlock(b, ItemBlockBase.class, b.getTranslationKey());
	}

	private static void register(Block b, Class<? extends ItemBlock> clazz) {
		GameRegistry.registerBlock(b, clazz, b.getTranslationKey());
	}

	public static void addRemap(String unloc, Block block, int meta) {
		Block remap = new BlockRemap(block, meta).setTranslationKey(unloc);
		register(remap, ItemBlockRemap.class);
	}

	// Pretty much the default getDrops function but with no damage set on the item (fucks with recipes)
	// but setting the meta via damageDropped breaks creative middle-click and any WAILA-like overlays
	public static ArrayList<ItemStack> getDropsWithoutDamage(World world, Block block, int metadata, int fortune) {
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();

		int count = block.quantityDropped(metadata, fortune, world.rand);
		for(int i = 0; i < count; i++) {
			Item item = block.getItemDropped(metadata, world.rand, fortune);
			if(item != null) {
				ret.add(new ItemStack(item, 1, 0));
			}
		}

		return ret;
	}

	public static Block getBlockFromStack(ItemStack stack) {
		if(stack == null) return null;
		if(!(stack.getItem() instanceof ItemBlock)) return null;

		return ((ItemBlock) stack.getItem()).field_150939_a;
	}

	// Is this block a special structure handling block, so we can ignore it for blacklist selection, etc.
	public static boolean isStructureBlock(Block block, boolean includeAir) {
		if(block == null) return false;
		if(block == wand_air) return includeAir;
		if(block == wand_structure) return true;
		if(block == wand_jigsaw) return true;
		if(block == wand_logic) return true;
		if(block == wand_tandem) return true;
		if(block == wand_loot) return true;
		return false;
	}

}
