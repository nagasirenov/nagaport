package com.hbm.main;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.oredict.OreDictionary;

import com.google.common.collect.ImmutableList;
import com.hbm.blocks.BlockEnums.EnumStoneType;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockToolConversion;
import com.hbm.commands.*;
import com.hbm.config.*;
import com.hbm.crafting.RodRecipes;
import com.hbm.creativetabs.*;
import com.hbm.entity.EntityMappings;
import com.hbm.entity.logic.IChunkLoader;
import com.hbm.entity.mob.siege.SiegeTier;
import com.hbm.handler.*;
import com.hbm.handler.atmosphere.ChunkAtmosphereManager;
import com.hbm.handler.ae2.AE2CompatHandler;
import com.hbm.handler.imc.*;
import com.hbm.handler.microblocks.MicroBlocksCompatHandler;
import com.hbm.handler.neutron.NeutronHandler;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.hazard.HazardRegistry;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.OreDictManager.DictFrame;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.recipes.*;
import com.hbm.inventory.recipes.anvil.AnvilRecipes;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ItemEnums.EnumAchievementType;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.sedna.mods.XWeaponModManager;
import com.hbm.lib.HbmWorld;
import com.hbm.lib.RefStrings;
import com.hbm.packet.PacketDispatcher;
import com.hbm.potion.HbmPotion;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.tileentity.TileMappings;
import com.hbm.tileentity.bomb.TileEntityLaunchPadBase;
import com.hbm.tileentity.bomb.TileEntityNukeCustom;
import com.hbm.tileentity.machine.rbmk.RBMKDials;
import com.hbm.util.*;
import com.hbm.world.feature.BedrockOre;
import com.hbm.world.feature.OreCave;
import com.hbm.world.feature.OreLayer3D;
import com.hbm.world.feature.SchistStratum;
import com.hbm.world.generator.CellularDungeonFactory;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.Mod.Metadata;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import com.hbm.advancement.NtmAdvancements;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatBasic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.EnumHelper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import com.hbm.dim.SolarSystem;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.dim.WorldTypeTeleport;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.world.PlanetGen;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = RefStrings.MODID, name = RefStrings.NAME, version = RefStrings.VERSION)
public class MainRegistry {

	@Instance(RefStrings.MODID)
	public static MainRegistry instance;

	@SidedProxy(clientSide = RefStrings.CLIENTSIDE, serverSide = RefStrings.SERVERSIDE)
	public static ServerProxy proxy;

	@Metadata
	public static ModMetadata meta;

	public static Logger logger = LogManager.getLogger("HBM");

	// Tool Materials
	public static ToolMaterial tMatSchrab = EnumHelper.addToolMaterial("SCHRABIDIUM", 3, 10000, 50.0F, 100.0F, 200);
	public static ToolMaterial tMatHammmer = EnumHelper.addToolMaterial("SCHRABIDIUMHAMMER", 3, 0, 50.0F, 999999996F, 200);
	public static ToolMaterial tMatChainsaw = EnumHelper.addToolMaterial("CHAINSAW", 3, 1500, 50.0F, 22.0F, 0);
	public static ToolMaterial tMatSteel = EnumHelper.addToolMaterial("HBM_STEEL", 2, 500, 7.5F, 2.0F, 10);
	public static ToolMaterial tMatTitan = EnumHelper.addToolMaterial("HBM_TITANIUM", 3, 750, 9.0F, 2.5F, 15);
	public static ToolMaterial tMatAlloy = EnumHelper.addToolMaterial("HBM_ALLOY", 3, 2000, 15.0F, 5.0F, 5);
	public static ToolMaterial tMatCMB = EnumHelper.addToolMaterial("HBM_CMB", 3, 8500, 40.0F, 55F, 100);
	public static ToolMaterial tMatElec = EnumHelper.addToolMaterial("HBM_ELEC", 3, 0, 30.0F, 12.0F, 2);
	public static ToolMaterial tMatDesh = EnumHelper.addToolMaterial("HBM_DESH", 2, 0, 7.5F, 2.0F, 10);
	public static ToolMaterial tMatCobalt = EnumHelper.addToolMaterial("HBM_COBALT", 3, 750, 9.0F, 2.5F, 60);

	public static ToolMaterial enumToolMaterialSaw = EnumHelper.addToolMaterial("SAW", 2, 750, 2.0F, 3.5F, 25);
	public static ToolMaterial enumToolMaterialBat = EnumHelper.addToolMaterial("BAT", 0, 500, 1.5F, 3F, 25);
	public static ToolMaterial enumToolMaterialBatNail = EnumHelper.addToolMaterial("BATNAIL", 0, 450, 1.0F, 4F, 25);
	public static ToolMaterial enumToolMaterialGolfClub = EnumHelper.addToolMaterial("GOLFCLUB", 1, 1000, 2.0F, 5F, 25);
	public static ToolMaterial enumToolMaterialPipeRusty = EnumHelper.addToolMaterial("PIPERUSTY", 1, 350, 1.5F, 4.5F, 25);
	public static ToolMaterial enumToolMaterialPipeLead = EnumHelper.addToolMaterial("PIPELEAD", 1, 250, 1.5F, 3F, 25);

	public static ToolMaterial enumToolMaterialBottleOpener = EnumHelper.addToolMaterial("OPENER", 1, 250, 1.5F, 0.5F, 200);
	public static ToolMaterial enumToolMaterialSledge = EnumHelper.addToolMaterial("SHIMMERSLEDGE", 1, 0, 25.0F, 26F, 200);

	public static ToolMaterial enumToolMaterialMultitool = EnumHelper.addToolMaterial("MULTITOOL", 3, 5000, 25F, 5.5F, 25);

	// Armor Materials
	public static ArmorMaterial aMatSchrab = EnumHelper.addArmorMaterial("HBM_SCHRABIDIUM", "leather", 100, new int[] { 3, 6, 8, 3 }, 50, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
	public static ArmorMaterial aMatEuph = EnumHelper.addArmorMaterial("HBM_EUPHEMIUM", "leather", 15000000, new int[] { 3, 6, 8, 3 }, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
	public static ArmorMaterial aMatHaz = EnumHelper.addArmorMaterial("HBM_HAZMAT", "leather", 60, new int[] { 1, 4, 5, 2 }, 5, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
	public static ArmorMaterial aMatHaz2 = EnumHelper.addArmorMaterial("HBM_HAZMAT2", "leather", 60, new int[] { 1, 4, 5, 2 }, 5, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
	public static ArmorMaterial aMatHaz3 = EnumHelper.addArmorMaterial("HBM_HAZMAT3", "leather", 60, new int[] { 1, 4, 5, 2 }, 5, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
	public static ArmorMaterial aMatSteel = EnumHelper.addArmorMaterial("HBM_STEEL", "leather", 20, new int[] { 2, 5, 6, 2 }, 5, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
	public static ArmorMaterial aMatAsbestos = EnumHelper.addArmorMaterial("HBM_ASBESTOS", "leather", 20, new int[] { 1, 3, 4, 1 }, 5, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
	public static ArmorMaterial aMatTitan = EnumHelper.addArmorMaterial("HBM_TITANIUM", "leather", 25, new int[] { 3, 6, 8, 3 }, 9, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
	public static ArmorMaterial aMatAlloy = EnumHelper.addArmorMaterial("HBM_ALLOY", "leather", 40, new int[] { 3, 6, 8, 3 }, 12, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
	public static ArmorMaterial aMatPaa = EnumHelper.addArmorMaterial("HBM_PAA", "leather", 75, new int[] { 3, 6, 8, 3 }, 25, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
	public static ArmorMaterial aMatCMB = EnumHelper.addArmorMaterial("HBM_CMB", "leather", 60, new int[] { 3, 6, 8, 3 }, 50, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
	public static ArmorMaterial aMatAus3 = EnumHelper.addArmorMaterial("HBM_AUSIII", "leather", 375, new int[] { 2, 5, 6, 2 }, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
	public static ArmorMaterial aMatSecurity = EnumHelper.addArmorMaterial("HBM_SECURITY", "leather", 100, new int[] { 3, 6, 8, 3 }, 15, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
	public static ArmorMaterial aMatCobalt = EnumHelper.addArmorMaterial("HBM_COBALT", "leather", 70, new int[] { 3, 6, 8, 3 }, 60, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
	public static ArmorMaterial aMatStarmetal = EnumHelper.addArmorMaterial("HBM_STARMETAL", "leather", 150, new int[] { 3, 6, 8, 3 }, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
	public static ArmorMaterial aMatBismuth = EnumHelper.addArmorMaterial("HBM_BISMUTH", "leather", 100, new int[] { 3, 6, 8, 3 }, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);

	// Creative Tabs

	public static CreativeTabs partsTab = new PartsTab(CreativeTabs.getNextID(), "tabParts");					// ingots, nuggets, wires, machine parts
	public static CreativeTabs controlTab = new ControlTab(CreativeTabs.getNextID(), "tabControl");				// items that belong in machines, fuels, etc
	public static CreativeTabs templateTab = new TemplateTab(CreativeTabs.getNextID(), "tabTemplate");			// templates, siren tracks
	public static CreativeTabs blockTab = new BlockTab(CreativeTabs.getNextID(), "tabBlocks");					// ore and mineral blocks
	public static CreativeTabs machineTab = new MachineTab(CreativeTabs.getNextID(), "tabMachine");				// machines, structure parts
	public static CreativeTabs nukeTab = new NukeTab(CreativeTabs.getNextID(), "tabNuke");						// bombs
	public static CreativeTabs missileTab = new MissileTab(CreativeTabs.getNextID(), "tabMissile");				// missiles, satellites
	public static CreativeTabs weaponTab = new WeaponTab(CreativeTabs.getNextID(), "tabWeapon");				// turrets, weapons, ammo
	public static CreativeTabs consumableTab = new ConsumableTab(CreativeTabs.getNextID(), "tabConsumable");	// drinks, kits, tools

	// Statistics
	public static StatBase statLegendary;
	public static StatBase statMines;
	public static StatBase statBullets;

	// Achievements
	public static ResourceLocation achSacrifice;
	public static ResourceLocation achImpossible;
	public static ResourceLocation achTOB;
	public static ResourceLocation achPotato;
	public static ResourceLocation achC20_5;
	public static ResourceLocation achFiend;
	public static ResourceLocation achFiend2;
	public static ResourceLocation achRadPoison;
	public static ResourceLocation achRadDeath;
	public static ResourceLocation achStratum;
	public static ResourceLocation achOmega12;
	public static ResourceLocation achSomeWounds;
	public static ResourceLocation achSlimeball;
	public static ResourceLocation achSulfuric;
	public static ResourceLocation achGoFish;
	public static ResourceLocation achNo9;
	public static ResourceLocation achInferno;
	public static ResourceLocation achRedRoom;
	public static ResourceLocation bobHidden;
	public static ResourceLocation horizonsStart;
	public static ResourceLocation horizonsEnd;
	public static ResourceLocation horizonsBonus;
	public static ResourceLocation bossCreeper;
	public static ResourceLocation bossMeltdown;
	public static ResourceLocation bossMaskman;
	public static ResourceLocation bossWorm;
	public static ResourceLocation bossUFO;
	public static ResourceLocation digammaSee;
	public static ResourceLocation digammaFeel;
	public static ResourceLocation digammaKnow;
	public static ResourceLocation digammaKauaiMoho;
	public static ResourceLocation digammaUpOnTop;
	public static ResourceLocation rotConsum;

	public static ResourceLocation achBurnerPress;
	public static ResourceLocation achBlastFurnace;
	public static ResourceLocation achAssembly;
	public static ResourceLocation achSelenium;
	public static ResourceLocation achChemplant;
	public static ResourceLocation achConcrete;
	public static ResourceLocation achPolymer;
	public static ResourceLocation achDesh;
	public static ResourceLocation achTantalum;
	public static ResourceLocation achRedBalloons;
	public static ResourceLocation achManhattan;
	public static ResourceLocation achGasCent;
	public static ResourceLocation achCentrifuge;
	public static ResourceLocation achFOEQ;
	public static ResourceLocation achSoyuz;
	public static ResourceLocation achSpace;
	public static ResourceLocation achSchrab;
	public static ResourceLocation achAcidizer;
	public static ResourceLocation achRadium;
	public static ResourceLocation achTechnetium;
	public static ResourceLocation achZIRNOXBoom;
	public static ResourceLocation achChicagoPile;
	public static ResourceLocation achSILEX;
	public static ResourceLocation achWatz;
	public static ResourceLocation achWatzBoom;
	public static ResourceLocation achRBMK;
	public static ResourceLocation achRBMKBoom;
	public static ResourceLocation achBismuth;
	public static ResourceLocation achBreeding;
	public static ResourceLocation achFusion;
	public static ResourceLocation achDriveFail;

	public static int generalOverride = 0;
	public static int polaroidID = 1;

	public static long startupTime = 0;
	public static File configDir;
	public static File configHbmDir;

	public Random rand = new Random();

	@EventHandler
	public void PreLoad(FMLPreInitializationEvent PreEvent) {
		CrashHelper.init();

		startupTime = System.currentTimeMillis();
		configDir = PreEvent.getModConfigurationDirectory();
		configHbmDir = new File(configDir.getAbsolutePath() + File.separatorChar + "hbmConfig");

		if(!configHbmDir.exists()) configHbmDir.mkdir();
		Identity.init(configDir);

		logger.info("Let us celebrate the fact that the logger finally works again!");

		// Reroll Polaroid
		if(generalOverride > 0 && generalOverride < 19) {
			polaroidID = generalOverride;
		} else {
			polaroidID = rand.nextInt(18) + 1;
			while(polaroidID == 4 || polaroidID == 9)
				polaroidID = rand.nextInt(18) + 1;
		}

		ShadyUtil.test();
		loadConfig(PreEvent);
		HbmPotion.init();

		/* For whichever fucking reason, replacing the bolt items with a bolt autogen broke all autogen items, most likely due to the load order.
		 * This "fix" just makes sure that the material system is loaded first no matter what. */
		Mats.MAT_STONE.getUnlocalizedName();
		Fluids.init();
		proxy.registerPreRenderInfo();
		com.hbm.registry.GameRegistry.beginRegistrationTables();
		ModBlocks.mainRegistry();
		ModItems.mainRegistry();
		com.hbm.registry.GameRegistry.finishRegistrationTables();
		proxy.registerRenderInfo();
		HbmWorld.mainRegistry();
		GameRegistry.registerFuelHandler(new FuelHandler());
		BulletConfigSyncingUtil.loadConfigsForSync();
		CellularDungeonFactory.init();
		Satellite.register();
		HTTPHandler.loadStats();
		com.hbm.crafting.nativeapi.NtmCraftingTables.schedule(CraftingManager::mainRegistry);
		SiegeTier.registerTiers();
		HazardRegistry.registerItems();
		HazardRegistry.registerTrafos();
		XWeaponModManager.init();

		SolarSystem.init();

		OreDictManager oreMan = new OreDictManager();
		MinecraftForge.EVENT_BUS.register(oreMan); //OreRegisterEvent
		OreDictManager.registerGroups(); //important to run first
		com.hbm.crafting.nativeapi.NtmCraftingTables.schedule(OreDictManager::registerOres);

		//BiomeGenNoMansLand.initDictionary();

		aMatSchrab.repairMaterial = new ItemStack(ModItems.ingot_schrabidium, 1, OreDictionary.WILDCARD_VALUE);
		aMatHaz.repairMaterial = new ItemStack(ModItems.hazmat_cloth, 1, OreDictionary.WILDCARD_VALUE);
		aMatHaz2.repairMaterial = new ItemStack(ModItems.hazmat_cloth_red, 1, OreDictionary.WILDCARD_VALUE);
		aMatHaz3.repairMaterial = new ItemStack(ModItems.hazmat_cloth_grey, 1, OreDictionary.WILDCARD_VALUE);
		aMatTitan.repairMaterial = new ItemStack(ModItems.ingot_titanium, 1, OreDictionary.WILDCARD_VALUE);
		aMatSteel.repairMaterial = new ItemStack(ModItems.ingot_steel, 1, OreDictionary.WILDCARD_VALUE);
		aMatAsbestos.repairMaterial = new ItemStack(ModItems.asbestos_cloth, 1, OreDictionary.WILDCARD_VALUE);
		aMatAlloy.repairMaterial = new ItemStack(ModItems.ingot_advanced_alloy, 1, OreDictionary.WILDCARD_VALUE);
		aMatPaa.repairMaterial = new ItemStack(ModItems.plate_paa, 1, OreDictionary.WILDCARD_VALUE);
		aMatCMB.repairMaterial = new ItemStack(ModItems.ingot_combine_steel, 1, OreDictionary.WILDCARD_VALUE);
		aMatAus3.repairMaterial = new ItemStack(ModItems.ingot_australium, 1, OreDictionary.WILDCARD_VALUE);
		aMatSecurity.repairMaterial = new ItemStack(ModItems.plate_kevlar, 1, OreDictionary.WILDCARD_VALUE);
		aMatCobalt.repairMaterial = new ItemStack(ModItems.ingot_cobalt, 1, OreDictionary.WILDCARD_VALUE);
		aMatStarmetal.repairMaterial = new ItemStack(ModItems.ingot_starmetal, 1, OreDictionary.WILDCARD_VALUE);
		aMatBismuth.repairMaterial = new ItemStack(ModItems.plate_bismuth, 1, OreDictionary.WILDCARD_VALUE);
		tMatSchrab.setRepairItem(new ItemStack(ModItems.ingot_schrabidium));
		tMatHammmer.setRepairItem(new ItemStack(Item.getItemFromBlock(ModBlocks.block_schrabidium)));
		tMatChainsaw.setRepairItem(new ItemStack(ModItems.ingot_steel));
		tMatTitan.setRepairItem(new ItemStack(ModItems.ingot_titanium));
		tMatSteel.setRepairItem(new ItemStack(ModItems.ingot_steel));
		tMatAlloy.setRepairItem(new ItemStack(ModItems.ingot_advanced_alloy));
		tMatCMB.setRepairItem(new ItemStack(ModItems.ingot_combine_steel));
		enumToolMaterialBottleOpener.setRepairItem(new ItemStack(ModItems.plate_steel));
		tMatDesh.setRepairItem(new ItemStack(ModItems.ingot_desh));

		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GUIHandler());

		TileMappings.writeMappings();
		MachineDynConfig.initialize();
		TileEntityLaunchPadBase.registerLaunchables();

		for(Entry<Class<? extends TileEntity>, String[]> e : TileMappings.map.entrySet()) {

			if(e.getValue().length == 1)
				GameRegistry.registerTileEntity(e.getKey(), e.getValue()[0]);
			else
				GameRegistry.registerTileEntityWithAlternatives(e.getKey(), e.getValue()[0], e.getValue());
		}

		VanillaLootHandler chestLoot = new VanillaLootHandler();
		chestLoot.addItem(LootTableList.CHESTS_VILLAGE_BLACKSMITH, ModItems.armor_polish, 3);
		chestLoot.addItem(LootTableList.CHESTS_VILLAGE_BLACKSMITH, ModItems.bathwater, 1);
		chestLoot.addItem(LootTableList.CHESTS_ABANDONED_MINESHAFT, ModItems.bathwater, 1);
		chestLoot.addItem(LootTableList.CHESTS_ABANDONED_MINESHAFT, ModItems.serum, 5);
		chestLoot.addItem(LootTableList.CHESTS_ABANDONED_MINESHAFT, ModItems.no9, 5);
		chestLoot.addItem(LootTableList.CHESTS_ABANDONED_MINESHAFT, ModItems.key_red_cracked, 5);
		chestLoot.addItem(LootTableList.CHESTS_SIMPLE_DUNGEON, ModItems.heart_piece, 1);
		chestLoot.addItem(LootTableList.CHESTS_SIMPLE_DUNGEON, ModItems.key_red_cracked, 5);
		chestLoot.addItem(LootTableList.CHESTS_DESERT_PYRAMID, ModItems.heart_piece, 1);
		chestLoot.addItem(LootTableList.CHESTS_JUNGLE_TEMPLE, ModItems.heart_piece, 1);
		chestLoot.addItem(LootTableList.CHESTS_SIMPLE_DUNGEON, ModItems.scrumpy, 1);
		chestLoot.addItem(LootTableList.CHESTS_DESERT_PYRAMID, ModItems.scrumpy, 1);
		chestLoot.addItem(LootTableList.CHESTS_SPAWN_BONUS_CHEST, ModItems.no9, 7);
		MinecraftForge.EVENT_BUS.register(chestLoot);

		EntityMappings.writeMappings();
		//CompatNER.init();

		ForgeChunkManager.setForcedChunkLoadingCallback(this, new LoadingCallback() {

			@Override
			public void ticketsLoaded(List<Ticket> tickets, World world) {
				for(Ticket ticket : tickets) {
					if(ticket.getType() == ForgeChunkManager.Type.NORMAL) {
						ChunkLoaderManager.loadTicket(world, ticket);
						return;
					}

					if(ticket.getEntity() instanceof IChunkLoader) {
						((IChunkLoader) ticket.getEntity()).init(ticket);
					}
				}
			}
		});

		DispenserBehaviorHandler.init();
		MicroBlocksCompatHandler.preInit();
	}

	@EventHandler
	public static void load(FMLInitializationEvent event) {

		RodRecipes.registerInit();

		statLegendary = new StatBasic("stat.ntmLegendary", new TextComponentTranslation("stat.ntmLegendary")).registerStat();
		statMines = new StatBasic("stat.ntmMines", new TextComponentTranslation("stat.ntmMines")).registerStat();
		statBullets = new StatBasic("stat.ntmBullets", new TextComponentTranslation("stat.ntmBullets")).registerStat();

		achSacrifice = NtmAdvancements.id("sacrifice");
		achImpossible = NtmAdvancements.id("impossible");
		achTOB = NtmAdvancements.id("tasteofblood");
		achGoFish = NtmAdvancements.id("goFish");
		achPotato = NtmAdvancements.id("potato");
		achC20_5 = NtmAdvancements.id("c20_5");
		achFiend = NtmAdvancements.id("fiend");
		achFiend2 = NtmAdvancements.id("fiend2");
		achStratum = NtmAdvancements.id("stratum");
		achOmega12 = NtmAdvancements.id("omega12");

		achNo9 = NtmAdvancements.id("no9");
		achSlimeball = NtmAdvancements.id("slimeball");
		achSulfuric = NtmAdvancements.id("sulfuric");
		achInferno = NtmAdvancements.id("inferno");
		achRedRoom = NtmAdvancements.id("redRoom");

		bobHidden = NtmAdvancements.id("hidden");

		horizonsStart = NtmAdvancements.id("horizonsStart");
		horizonsEnd = NtmAdvancements.id("horizonsEnd");
		horizonsBonus = NtmAdvancements.id("horizonsBonus");

		bossCreeper = NtmAdvancements.id("bossCreeper");
		bossMeltdown = NtmAdvancements.id("bossMeltdown");
		bossMaskman = NtmAdvancements.id("bossMaskman");
		bossWorm = NtmAdvancements.id("bossWorm");
		bossUFO = NtmAdvancements.id("bossUFO");

		achRadPoison = NtmAdvancements.id("radPoison");
		achRadDeath = NtmAdvancements.id("radDeath");

		achSomeWounds = NtmAdvancements.id("someWounds");

		digammaSee = NtmAdvancements.id("digammaSee");
		digammaFeel = NtmAdvancements.id("digammaFeel");
		digammaKnow = NtmAdvancements.id("digammaKnow");
		digammaKauaiMoho = NtmAdvancements.id("digammaKauaiMoho");
		digammaUpOnTop = NtmAdvancements.id("digammaUpOnTop");
		//rotConsum = new Achievement("achievement.rotConsum", "rotConsum", 7, 8, ModItems.rot_consumes, null).initIndependentStat().registerStat().setSpecial();

		//progression achieves
		achBurnerPress = NtmAdvancements.id("burnerPress");
		achBlastFurnace = NtmAdvancements.id("blastFurnace");
		achAssembly = NtmAdvancements.id("assembly");
		achSelenium = NtmAdvancements.id("selenium");
		achChemplant = NtmAdvancements.id("chemplant");
		achConcrete = NtmAdvancements.id("concrete");
		achPolymer = NtmAdvancements.id("polymer");
		achDesh = NtmAdvancements.id("desh");
		achTantalum = NtmAdvancements.id("tantalum");
		achGasCent = NtmAdvancements.id("gasCent");
		achCentrifuge = NtmAdvancements.id("centrifuge");
		achDriveFail = NtmAdvancements.id("driveFail");
		achFOEQ = NtmAdvancements.id("FOEQ");
		achSoyuz = NtmAdvancements.id("soyuz");
		achSpace = NtmAdvancements.id("space");
		achSchrab = NtmAdvancements.id("schrab");
		achAcidizer = NtmAdvancements.id("acidizer");
		achRadium = NtmAdvancements.id("radium");
		achTechnetium = NtmAdvancements.id("technetium");
		achZIRNOXBoom = NtmAdvancements.id("ZIRNOXBoom");
		achChicagoPile = NtmAdvancements.id("chicagoPile");
		achSILEX = NtmAdvancements.id("SILEX");
		achWatz = NtmAdvancements.id("watz");
		achWatzBoom = NtmAdvancements.id("watzBoom");
		achRBMK = NtmAdvancements.id("RBMK");
		achRBMKBoom = NtmAdvancements.id("RBMKBoom");
		achBismuth = NtmAdvancements.id("bismuth");
		achBreeding = NtmAdvancements.id("breeding");
		achFusion = NtmAdvancements.id("fusion");
		achRedBalloons = NtmAdvancements.id("redBalloons");
		achManhattan = NtmAdvancements.id("manhattan");

		NtmAdvancements.registerCounters();
		MinecraftForge.EVENT_BUS.register(new com.hbm.advancement.LegacyAchievementEvents());

		// MUST be initialized AFTER achievements!!
		BobmazonOfferFactory.init();

		IMCHandler.registerHandler("blastfurnace", new IMCBlastFurnace());
		IMCHandler.registerHandler("crystallizer", new IMCCrystallizer());
		IMCHandler.registerHandler("centrifuge", new IMCCentrifuge());

		PlanetGen.init();

	}

	@EventHandler
	public static void initIMC(IMCEvent event) {

		ImmutableList<IMCMessage> inbox = event.getMessages(); //tee-hee

		for(IMCMessage message : inbox) {
			IMCHandler handler = IMCHandler.getHandler(message.key);

			if(handler != null) {
				MainRegistry.logger.info("Received IMC of type >" + message.key + "< from " + message.getSender() + "!");
				handler.process(message);
			} else {
				MainRegistry.logger.error("Could not process unknown IMC type \"" + message.key + "\"");
			}
		}
	}

	@EventHandler
	public static void LoadComplete(net.minecraftforge.fml.common.event.FMLLoadCompleteEvent event) {
		proxy.onLoadComplete();
	}

	@EventHandler
	public static void PostLoad(FMLPostInitializationEvent PostEvent) {
		// to make sure that foreign registered fluids are accounted for,
		// even when the reload listener is registered too late due to load order
		// IMPORTANT: fluids have to load before recipes. weird shit happens if not.
		Fluids.reloadFluids();
		FluidContainerRegistry.register();

		MagicRecipes.register();
		LemegetonRecipes.register();
		SILEXRecipes.register();
		GasCentrifugeRecipes.register();

		CustomMachineConfigJSON.initialize();

		//the good stuff
		SerializableRecipe.registerAllHandlers();
		SerializableRecipe.initialize();

		//Anvil has to come after serializables (i.e. anvil)
		AnvilRecipes.register();

		//has to register after cracking, and therefore after all serializable recipes
		RadiolysisRecipes.registerRadiolysis();

		FalloutConfigJSON.initialize();
		ItemPoolConfigJSON.initialize();

		ClientConfig.initConfig();
		ServerConfig.initConfig();

		TileEntityNukeCustom.registerBombItems();
		ArmorUtil.register();
		HazmatRegistry.registerHazmats();
		DamageResistanceHandler.init();
		BlockToolConversion.registerRecipes();
		AchievementHandler.register();

		MobUtil.intializeMobPools();

		proxy.registerMissileItems();

		// Load compatibility for OC.
		CompatHandler.init();

		// Load compatibility for AE2.
		AE2CompatHandler.init();

		//expand for the largest entity we have (currently Quackos who is 17.5m in diameter, that's one fat duck)
		World.MAX_ENTITY_RADIUS = Math.max(World.MAX_ENTITY_RADIUS, 8.75);

		MinecraftForge.EVENT_BUS.register(new SchistStratum()); //DecorateBiomeEvent.Pre
		//MinecraftForge.EVENT_BUS.register(new DeepLayer()); //DecorateBiomeEvent.Pre

		// Global caves + layers
		// Sulfur caves can't be defined globally due to vacuums evaporating fluids
		if(WorldConfig.enableHematite) new OreLayer3D(ModBlocks.stone_resource, EnumStoneType.HEMATITE.ordinal()).setGlobal(true).setScaleH(0.04D).setScaleV(0.25D).setThreshold(230);
		if(WorldConfig.enableBauxite) new OreLayer3D(ModBlocks.stone_resource, EnumStoneType.BAUXITE.ordinal()).setGlobal(true).setScaleH(0.03D).setScaleV(0.15D).setThreshold(300);
		if(WorldConfig.enableMalachite) new OreLayer3D(ModBlocks.stone_resource, EnumStoneType.MALACHITE.ordinal()).setGlobal(true).setScaleH(0.1D).setScaleV(0.15D).setThreshold(275);


		// Earth caves + layers
		if(WorldConfig.enableSulfurCave) new OreCave(ModBlocks.stone_resource, EnumStoneType.SULFUR.ordinal()).setThreshold(1.5D).setRangeMult(20).setYLevel(30).setMaxRange(20).withFluid(ModBlocks.sulfuric_acid_block);
		if(WorldConfig.enableAsbestosCave) new OreCave(ModBlocks.stone_resource, EnumStoneType.ASBESTOS.ordinal()).setThreshold(1.75D).setRangeMult(20).setYLevel(25).setMaxRange(20);

		// Moon caves + layers
		if(WorldConfig.enableSulfurCave) new OreCave(ModBlocks.stone_resource, EnumStoneType.SULFUR.ordinal()).setDimension(SpaceConfig.moonDimension).setThreshold(1.5D).setRangeMult(20).setYLevel(30).setMaxRange(20);
		new OreLayer3D(ModBlocks.stone_resource, EnumStoneType.CONGLOMERATE.ordinal()).setDimension(SpaceConfig.moonDimension).setScaleH(0.04D).setScaleV(0.25D).setThreshold(220);

		// Duna caves + layers
		if(WorldConfig.enableSulfurCave) new OreCave(ModBlocks.stone_resource, EnumStoneType.SULFUR.ordinal()).setDimension(SpaceConfig.dunaDimension).setThreshold(1.5D).setRangeMult(20).setYLevel(30).setMaxRange(20);

		// Ike caves + layers
		if(WorldConfig.enableSulfurCave) new OreCave(ModBlocks.stone_resource, EnumStoneType.SULFUR.ordinal()).setDimension(SpaceConfig.ikeDimension).setThreshold(1.5D).setRangeMult(20).setYLevel(30).setMaxRange(20);
		new OreLayer3D(ModBlocks.stone_resource, EnumStoneType.CONGLOMERATE.ordinal()).setDimension(SpaceConfig.ikeDimension).setScaleH(0.04D).setScaleV(0.25D).setThreshold(220);

		// Eve caves + layers
		if(WorldConfig.enableSulfurCave) new OreCave(ModBlocks.stone_resource, EnumStoneType.SULFUR.ordinal()).setDimension(SpaceConfig.eveDimension).setThreshold(1.5D).setRangeMult(20).setYLevel(30).setMaxRange(20).withFluid(ModBlocks.sulfuric_acid_block);
		new OreCave(ModBlocks.basalt, 0).setDimension(SpaceConfig.eveDimension).setThreshold(0.15D).setRangeMult(40).setYLevel(54).setMaxRange(24).setBlockOverride(ModBlocks.eve_silt).setIgnoreWater(true).setStalagmites(false);

		// Moho caves + layers
		if(WorldConfig.enableSulfurCave) new OreCave(ModBlocks.stone_resource, EnumStoneType.SULFUR.ordinal()).setDimension(SpaceConfig.mohoDimension).setThreshold(1.5D).setRangeMult(20).setYLevel(30).setMaxRange(20);

		// Minmus caves + layers
		if(WorldConfig.enableSulfurCave) new OreCave(ModBlocks.stone_resource, EnumStoneType.SULFUR.ordinal()).setDimension(SpaceConfig.minmusDimension).setThreshold(1.5D).setRangeMult(20).setYLevel(30).setMaxRange(20);
		new OreLayer3D(ModBlocks.minmus_regolith, 0).setDimension(SpaceConfig.minmusDimension).setScaleH(0.06D).setScaleV(0.25D).setThreshold(220);
		new OreLayer3D(ModBlocks.minmus_smooth, 0).setDimension(SpaceConfig.minmusDimension).setScaleH(0.05D).setScaleV(0.15D).setThreshold(280);
		new OreLayer3D(ModBlocks.stone_resource, EnumStoneType.CONGLOMERATE.ordinal()).setDimension(SpaceConfig.minmusDimension).setScaleH(0.04D).setScaleV(0.25D).setThreshold(220);

		// Laythe caves + layers
		if(WorldConfig.enableSulfurCave) new OreCave(ModBlocks.stone_resource, EnumStoneType.SULFUR.ordinal()).setDimension(SpaceConfig.laytheDimension).setThreshold(1.5D).setRangeMult(20).setYLevel(30).setMaxRange(20).withFluid(ModBlocks.sulfuric_acid_block);
		if(WorldConfig.enableAsbestosCave) new OreCave(ModBlocks.stone_resource, EnumStoneType.ASBESTOS.ordinal()).setDimension(SpaceConfig.laytheDimension).setThreshold(1.75D).setRangeMult(20).setYLevel(25).setMaxRange(20);
		new OreCave(ModBlocks.tumor).setDimension(SpaceConfig.laytheDimension).setThreshold(0.3D).setRangeMult(20).setYLevel(25).setMaxRange(70);
		new OreCave(ModBlocks.laythe_silt, 6).setDimension(SpaceConfig.laytheDimension).setThreshold(0.25D).setRangeMult(80).setYLevel(54).setMaxRange(24).setBlockOverride(ModBlocks.laythe_silt).setIgnoreWater(true).setStalagmites(false);
		new OreCave(ModBlocks.laythe_silt, 3).setDimension(SpaceConfig.laytheDimension).setThreshold(0.2D).setRangeMult(60).setYLevel(58).setMaxRange(14).setBlockOverride(ModBlocks.laythe_silt).setIgnoreWater(true).setStalagmites(false);

		BedrockOre.init();
		PlanetGen.overrideOverworldProvider(); // Doing it as late as possible

		Compat.handleRailcraftNonsense();
		SuicideThreadDump.register();
		CommandReloadClient.register();

		WorldTypeTeleport.init();

		//ExplosionTests.runTest();
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		if(logger == null)
			logger = event.getModLog();

		com.hbm.extprop.HbmEntityProps.register();

		ModEventHandler commonHandler = new ModEventHandler();
		FMLCommonHandler.instance().bus().register(commonHandler);
		MinecraftForge.EVENT_BUS.register(commonHandler);
		MinecraftForge.TERRAIN_GEN_BUS.register(commonHandler);
		MinecraftForge.ORE_GEN_BUS.register(commonHandler);

		ModEventHandlerImpact impactHandler = new ModEventHandlerImpact();
		FMLCommonHandler.instance().bus().register(impactHandler);
		MinecraftForge.EVENT_BUS.register(impactHandler);
		MinecraftForge.TERRAIN_GEN_BUS.register(impactHandler);

		PacketDispatcher.registerPackets();

		ChunkRadiationManager radiationSystem = new ChunkRadiationManager();
		MinecraftForge.EVENT_BUS.register(radiationSystem);
		FMLCommonHandler.instance().bus().register(radiationSystem);

		PollutionHandler pollution = new PollutionHandler();
		MinecraftForge.EVENT_BUS.register(pollution);
		FMLCommonHandler.instance().bus().register(pollution);

		DamageResistanceHandler dmgHandler = new DamageResistanceHandler();
		MinecraftForge.EVENT_BUS.register(dmgHandler);

		ChunkAtmosphereManager atmosphere = new ChunkAtmosphereManager();
		MinecraftForge.EVENT_BUS.register(atmosphere);
		FMLCommonHandler.instance().bus().register(atmosphere);

		NeutronHandler neutronHandler = new NeutronHandler();
		MinecraftForge.EVENT_BUS.register(neutronHandler);
		FMLCommonHandler.instance().bus().register(neutronHandler);

		if(event.getSide() == Side.CLIENT) {
			HbmKeybinds.register();
			HbmKeybinds keyHandler = new HbmKeybinds();
			FMLCommonHandler.instance().bus().register(keyHandler);
		}
	}

	//yes kids, this is where we would usually register commands
	@EventHandler
	public void serverStart(FMLServerStartingEvent event) {
		World world = event.getServer().getEntityWorld();
		RBMKDials.createDials(world);
		event.registerServerCommand(new CommandReloadRecipes());
		event.registerServerCommand(new CommandDebugChunkLoad());
		event.registerServerCommand(new CommandSatellites());
		event.registerServerCommand(new CommandRadiation());
		event.registerServerCommand(new CommandStations());
		event.registerServerCommand(new CommandPacketInfo());
		event.registerServerCommand(new CommandReloadServer());
		event.registerServerCommand(new CommandLocate());
		event.registerServerCommand(new CommandTotalTime());
		event.registerServerCommand(new CommandCustomize());
		event.registerServerCommand(new CommandReapNetworks());
		ArcFurnaceRecipes.registerFurnaceSmeltables(); // because we have to wait for other mods to take their merry ass time to register recipes
	}

	@EventHandler
	public void serverStart(FMLServerStartedEvent event) {

		if(GeneralConfig.enableStatReRegistering) {
			logger.info("Attempting to re-register item stats...");
			StatHelper.resetStatShitFuck(); //shit yourself
			logger.info("Item stats re-registered");
		}
	}

	private void loadConfig(FMLPreInitializationEvent event) {

		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();

		GeneralConfig.loadFromConfig(config);
		WorldConfig.loadFromConfig(config);
		MachineConfig.loadFromConfig(config);
		BombConfig.loadFromConfig(config);
		RadiationConfig.loadFromConfig(config);
		PotionConfig.loadFromConfig(config);
		ToolConfig.loadFromConfig(config);
		WeaponConfig.loadFromConfig(config);
		MobConfig.loadFromConfig(config);
		StructureConfig.loadFromConfig(config);
		SpaceConfig.loadFromConfig(config);

		config.save();

		try {
			if(GeneralConfig.enableThermosPreventer && Class.forName("thermos.ThermosClassTransformer") != null) {
				throw new IllegalStateException("The mod tried to start on a Thermos or its fork server and therefore stopped. To allow the server to start on Thermos, change the appropriate "
					+ "config entry (0.00 in hbm.cfg). This was done because, by default, Thermos "
					+ "uses a so-called \"optimization\" feature that reduces tile ticking a lot, which will inevitably break a lot of machines. Most people aren't even aware "
					+ "of this, and start blaming random mods for all their stuff breaking. In order to adjust or even disable this feature, edit \"tileentities.yml\" in your "
					+ "Thermos install folder. If you believe that crashing the server until a config option is changed is annoying, then I would agree, but it's still preferable "
					+ "over wasting hours trying to fix an issue that is really just an \"intended feature\" added by Thermos itself, and not a bug in the mod. You'll have to "
					+ "change Thermos' config anyway so that extra change in NTM's config can't be that big of a burden.");
			}
		} catch(ClassNotFoundException e) { }
	}

}
