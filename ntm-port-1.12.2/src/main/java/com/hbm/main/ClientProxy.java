package com.hbm.main;

import com.hbm.blocks.BlockVolcanoV2.TileEntityLightningVolcano;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockBobble.TileEntityBobble;
import com.hbm.blocks.generic.BlockEmitter.TileEntityEmitter;
import com.hbm.blocks.generic.BlockLoot.TileEntityLoot;
import com.hbm.blocks.generic.BlockOrrery.TileEntityOrrery;
import com.hbm.blocks.generic.BlockPedestal.TileEntityPedestal;
import com.hbm.blocks.generic.BlockPlushie.TileEntityPlushie;
import com.hbm.blocks.generic.BlockSkeletonHolder.TileEntitySkeletonHolder;
import com.hbm.blocks.generic.BlockSnowglobe.TileEntitySnowglobe;
import com.hbm.blocks.generic.BlockWandStructure.TileEntityWandStructure;
import com.hbm.blocks.machine.Floodlight.TileEntityFloodlight;
import com.hbm.blocks.machine.MachineFan.TileEntityFan;
import com.hbm.blocks.machine.PistonInserter.TileEntityPistonInserter;
import com.hbm.blocks.machine.WatzPump.TileEntityWatzPump;
import com.hbm.blocks.network.FluidPump.TileEntityFluidPump;
import com.hbm.config.GeneralConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.entity.cart.EntityMinecartCrate;
import com.hbm.entity.cart.EntityMinecartNTM;
import com.hbm.entity.effect.*;
import com.hbm.entity.grenade.*;
import com.hbm.entity.item.*;
import com.hbm.entity.logic.*;
import com.hbm.entity.missile.*;
import com.hbm.entity.missile.EntityMissileTier0.*;
import com.hbm.entity.missile.EntityMissileTier1.*;
import com.hbm.entity.missile.EntityMissileTier2.*;
import com.hbm.entity.missile.EntityMissileTier3.*;
import com.hbm.entity.missile.EntityMissileTier4.*;
import com.hbm.entity.missile.EntityRideableRocket.EntityRideableRocketDummy;
import com.hbm.entity.mob.*;
import com.hbm.entity.mob.botprime.EntityBOTPrimeBody;
import com.hbm.entity.mob.botprime.EntityBOTPrimeHead;
import com.hbm.entity.mob.glyphid.*;
import com.hbm.entity.mob.siege.*;
import com.hbm.entity.particle.*;
import com.hbm.entity.projectile.*;
import com.hbm.entity.train.EntityRailCarBase.BoundingBoxDummyEntity;
import com.hbm.entity.train.EntityRailCarRidable.SeatDummyEntity;
import com.hbm.entity.train.*;
import com.hbm.handler.CasingEjector;
import com.hbm.handler.HbmKeybinds;
import com.hbm.handler.HbmKeybinds.EnumKeybind;
import com.hbm.handler.ImpactWorldHandler;
import com.hbm.handler.imc.IMCHandlerNHNEI;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.sedna.factory.GunFactoryClient;
import com.hbm.lib.RefStrings;
import com.hbm.particle.*;
import com.hbm.particle.helper.ParticleCreators;
import com.hbm.particle.psys.engine.EventHandlerParticleEngine;
import com.hbm.qmaw.QMAWLoader;
import com.hbm.render.block.*;
import com.hbm.render.entity.RenderEmpty;
import com.hbm.render.entity.effect.*;
import com.hbm.render.entity.item.*;
import com.hbm.render.entity.mob.*;
import com.hbm.render.entity.projectile.*;
import com.hbm.render.entity.rocket.*;
import com.hbm.render.entity.rocket.part.RenderDropPod;
import com.hbm.render.item.*;
import com.hbm.render.item.ItemRenderMissileGeneric.RenderMissileType;
import com.hbm.render.item.block.ItemRenderBlock;
import com.hbm.render.item.block.ItemRenderDecoBlock;
import com.hbm.render.item.weapon.*;
import com.hbm.render.loader.HFRModelReloader;
import com.hbm.render.model.ModelDepthSquid;
import com.hbm.render.model.ModelMoonCow;
import com.hbm.render.model.ModelPigeon;
import com.hbm.render.model.ModelScrapFish;
import com.hbm.render.model.ModelScutter;
import com.hbm.render.model.ModelScuttlecrab;
import com.hbm.render.model.ModelSifterEel;
import com.hbm.render.tileentity.*;
import com.hbm.render.util.MissilePart;
import com.hbm.render.util.RenderInfoSystem;
import com.hbm.render.util.RenderInfoSystem.InfoEntry;
import com.hbm.render.util.RenderOverhead;
import com.hbm.render.util.RenderOverhead.Marker;
import com.hbm.sound.AudioWrapper;
import com.hbm.sound.AudioWrapperClient;
import com.hbm.tileentity.TileEntityDoorGeneric;
import com.hbm.tileentity.bomb.*;
import com.hbm.tileentity.deco.*;
import com.hbm.tileentity.machine.*;
import com.hbm.tileentity.machine.albion.*;
import com.hbm.tileentity.machine.fusion.*;
import com.hbm.tileentity.machine.oil.*;
import com.hbm.tileentity.machine.rbmk.*;
import com.hbm.tileentity.machine.storage.*;
import com.hbm.tileentity.network.*;
import com.hbm.tileentity.turret.*;
import com.hbm.util.BobMathUtil;
import com.hbm.util.ColorUtil;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.i18n.I18nClient;
import com.hbm.util.i18n.ITranslate;
import com.hbm.wiaj.cannery.Jars;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.model.ModelChicken;
import com.hbm.particle.NativeVanillaParticles;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.entity.RenderMinecart;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.Language;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import paulscode.sound.SoundSystemConfig;

import java.awt.*;
import java.net.URI;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;

public class ClientProxy extends ServerProxy {
    private LegacyBaseModelHandler legacyBaseModelHandler;
    private java.util.Set<Item> nativeStaticItems = java.util.Collections.emptySet();
    private java.util.Set<Item> nativeOneMeshItems;
    private java.util.Map<Item, com.hbm.render.item.nativeitem.staticfamily.SingleMeshEntityItemRenderer.BlockItemDraw> nativeDecoItemDraws;
    private com.hbm.render.item.nativeitem.context.ContextItemRegistration nativeContextItems;
    private com.hbm.render.item.nativeitem.context.ContextItemRegistration nativeSednaItems;
    private java.util.Map<Item, com.hbm.render.item.nativeitem.staticfamily.SingleMeshEntityItemRenderer.BlockItemDraw> nativeBlockItemDraws = java.util.Collections.emptyMap();
    private boolean nativeItemOwnershipFinished;


	private static final I18nClient I18N = new I18nClient();

	public RenderInfoSystem theInfoSystem = new RenderInfoSystem();

	public ITranslate getI18n() { return I18N; }

	/** Runs just before item an block init */
	@Override
	public void registerPreRenderInfo() {

		QMAWLoader.registerModFileURL(FMLCommonHandler.instance().findContainerFor(RefStrings.MODID).getSource());
	}

	/** Runs right after item and block init */
	@Override
	public void registerRenderInfo() {

		MinecraftForge.EVENT_BUS.register(new LegacyIconRegistrationHandler());
		MinecraftForge.EVENT_BUS.register(new com.hbm.render.item.NativeItemTextureModelHandler());
		legacyBaseModelHandler = new LegacyBaseModelHandler();
		MinecraftForge.EVENT_BUS.register(legacyBaseModelHandler);
		MinecraftForge.EVENT_BUS.register(new ChargeObjModelHandler());
		MinecraftForge.EVENT_BUS.register(new BarbedWireObjModelHandler());
		MinecraftForge.EVENT_BUS.register(new SpikeObjModelHandler());
		MinecraftForge.EVENT_BUS.register(new RubberCactusObjModelHandler());
		MinecraftForge.EVENT_BUS.register(new com.hbm.render.item.nativeitem.AngelCoreModelHandler(
				ModItems.core_angel, ResourceManager.bfangel, ResourceManager.bfangel_tex));
		// Item identities are resolved later, after native block/item registry events.
		registerClientEventHandler(new ModEventHandlerClient());
		registerClientEventHandler(new ModEventHandlerRenderer());
		registerClientEventHandler(new EventHandlerParticleEngine());
		registerClientEventHandler(theInfoSystem);

		registerTileEntitySpecialRenderer();
		registerItemRenderer();
		registerEntityRenderer();
		registerBlockRenderer();

		IReloadableResourceManager resourceMan = (IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager();
		resourceMan.registerReloadListener(new HFRModelReloader());
		resourceMan.registerReloadListener(new com.hbm.sound.SoundCategoryResolver());

		if(GeneralConfig.enableSoundExtension) {
			SoundSystemConfig.setNumberNormalChannels(GeneralConfig.normalSoundChannels);
			SoundSystemConfig.setNumberStreamingChannels(50);
		}
	}

	@Override
	public void registerPostRegistryInfo() {
		Jars.initJars();
		FallingTintModelHandler.registerColorHandlers();
		SlagModelHandler.registerColorHandlers();
		if(legacyBaseModelHandler != null) legacyBaseModelHandler.registerColorHandlers();
		com.hbm.render.item.ModItemColors.registerItemColors();

		// Must run after registry events: registering triggers an immediate reload that resolves items/blocks by name.
		((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new QMAWLoader());
	}

	private void registerClientEventHandler(Object handler) {
		MinecraftForge.EVENT_BUS.register(handler);
		FMLCommonHandler.instance().bus().register(handler);
	}



	@Override
	public void registerGunCfg() {
		GunFactoryClient.init();
	}

	@Override
	public void registerTileEntitySpecialRenderer() {
		//test crap
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityObjTester.class, new RendererObjTester());
		//deco
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDecoPoleSatelliteReceiver.class, new RenderPoleSatelliteReceiver());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDecoBlock.class, new RenderDecoBlock());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBroadcaster.class, new RenderDecoBlock());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGeiger.class, new RenderDecoBlock());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRadioRec.class, new RenderDecoBlock());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRadiobox.class, new RenderDecoBlock());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineSatDock.class, new RenderDecoBlock());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineGasDock.class, new RenderDecoBlock());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTransporterRocket.class, new RenderTransporterRocket());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDecoBlockAlt.class, new RenderDecoBlockAlt());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDecoBlockAltG.class, new RenderDecoBlockAlt());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDecoBlockAltW.class, new RenderDecoBlockAlt());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDecoBlockAltF.class, new RenderDecoBlockAlt());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDemonLamp.class, new RenderDemonLamp());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFloodlight.class, new RenderFloodlight());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLoot.class, new RenderLoot());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPedestal.class, new RenderPedestalTile());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySkeletonHolder.class, new RenderSkeletonHolder());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBobble.class, new RenderBobble());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySnowglobe.class, new RenderSnowglobe());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPlushie.class, new RenderPlushie());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEmitter.class, new RenderEmitter());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLightningVolcano.class, new RenderVol2());

		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLantern.class, new RenderLantern());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLanternBehemoth.class, new RenderLanternBehemoth());
		//bombs
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityNukeGadget.class, new RenderNukeGadget());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityNukeBoy.class, new RenderNukeBoy());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityNukeCustom.class, new RenderNukeCustom());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityNukeSolinium.class, new RenderNukeSolinium());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityNukeN2.class, new RenderNukeN2());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityNukeMan.class, new RenderNukeMan());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityNukeBalefire.class, new RenderNukeFstbmb());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAntimatter.class, new RenderAntimatter());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBombMulti.class, new RenderBombMulti());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityNukeMike.class, new RenderNukeMike());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityNukeTsar.class, new RenderNukeTsar());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityNukeFleija.class, new RenderNukeFleija());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCrashedBomb.class, new RenderCrashedBomb());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityNukePrototype.class, new RenderNukePrototype());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCharge.class, new RenderExplosiveCharge());
		//turrets
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurretChekhov.class, new RenderTurretChekhov());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurretFriendly.class, new RenderTurretFriendly());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurretJeremy.class, new RenderTurretJeremy());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurretTauon.class, new RenderTurretTauon());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurretRichard.class, new RenderTurretRichard());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurretHoward.class, new RenderTurretHoward());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurretHowardDamaged.class, new RenderTurretHowardDamaged());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurretMaxwell.class, new RenderTurretMaxwell());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurretFritz.class, new RenderTurretFritz());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurretBrandon.class, new RenderTurretBrandon());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurretArty.class, new RenderTurretArty());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurretHIMARS.class, new RenderTurretHIMARS());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurretSentry.class, new RenderTurretSentry());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurretSentryDamaged.class, new RenderTurretSentry());
		//mines
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLandmine.class, new RenderLandmine());
		//machines
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineCentrifuge.class, new RenderCentrifuge());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineGasCent.class, new RenderCentrifuge());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFEL.class, new RenderFEL());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySILEX.class, new RenderSILEX());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineUF6Tank.class, new RenderUF6Tank());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachinePuF6Tank.class, new RenderPuF6Tank());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineIGenerator.class, new RenderIGenerator());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineCyclotron.class, new RenderCyclotron());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPASource.class, new RenderPASource());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPABeamline.class, new RenderPABeamline());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPARFC.class, new RenderPARFC());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPAQuadrupole.class, new RenderPAQuadrupole());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPADipole.class, new RenderPADipole());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPADetector.class, new RenderPADetector());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineExposureChamber.class, new RenderExposureChamber());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineOilWell.class, new RenderDerrick());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineGasFlare.class, new RenderGasFlare());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityChimneyBrick.class, new RenderChimneyBrick());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityChimneyIndustrial.class, new RenderChimneyIndustrial());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineMiningLaser.class, new RenderLaserMiner());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineAnnihilator.class, new RenderAnnihilator());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineAssemblyMachine.class, new RenderAssemblyMachine());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineAssemblyFactory.class, new RenderAssemblyFactory());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachinePrecAss.class, new RenderPrecAss());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineChemicalPlant.class, new RenderChemicalPlant());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineChemicalFactory.class, new RenderChemicalFactory());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachinePUREX.class, new RenderPUREX());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineFluidTank.class, new RenderFluidTank());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineBAT9000.class, new RenderBAT9000());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineOrbus.class, new RenderOrbus());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineRefinery.class, new RenderRefinery());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineFractionTower.class, new RenderFractionTower());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySpacer.class, new RenderSpacer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachinePumpjack.class, new RenderPumpjack());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineFrackingTower.class, new RenderFrackingTower());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineTurbofan.class, new RenderTurbofan());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineTurbineGas.class, new RenderTurbineGas());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineLPW2.class, new RenderLPW2());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineHTR3.class, new RenderHTR3());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineHTRF4.class, new RenderHTRF4());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineHTRNeo.class, new RenderHTRNeo());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityXenonThruster.class, new RenderXenonThruster());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachinePress.class, new RenderPress());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineEPress.class, new RenderEPress());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineAmmoPress.class, new RenderAmmoPress());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineRadGen.class, new RenderRadGen());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGeiger.class, new RenderGeiger());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineRadarNT.class, new RenderRadar());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineRadarLarge.class, new RenderRadarLarge());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineRadarScreen.class, new RenderRadarScreen());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityReactorResearch.class, new RenderSmallReactor());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTesla.class, new RenderTesla());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBarrel.class, new RenderFluidBarrel());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineRotaryFurnace.class, new RenderRotaryFurnace());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineCrystallizer.class, new RenderCrystallizer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMicrowave.class, new RenderMicrowave());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineRTG.class, new RenderRTG());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineMiniRTG.class, new RenderRTG());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFF.class, new RenderForceField());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityForceField.class, new RenderMachineForceField());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineFENSU.class, new RenderFENSU());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBatterySocket.class, new RenderBatterySocket());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBatteryREDD.class, new RenderBatteryREDD());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineLargeTurbine.class, new RenderBigTurbine());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineTurbineGas.class, new RenderTurbineGas());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineIndustrialTurbine.class, new RenderIndustrialTurbine());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineReactorBreeding.class, new RenderBreeder());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySolarBoiler.class, new RenderSolarBoiler());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineSolarPanel.class, new RenderSolarPanel());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineStardar.class, new RenderStardar());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityOrbitalStation.class, new RenderOrbitalStation());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityOrbitalStationLauncher.class, new RenderOrbitalStation());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAirScrubber.class, new RenderAirScrubber());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityOrbitalStationComputer.class, new RenderOrbitalComputer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineDriveProcessor.class, new RenderDriveProcessor());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDishControl.class, new RenderDishControl());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityStorageDrum.class, new RenderStorageDrum());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityChungus.class, new RenderChungus());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTowerLarge.class, new RenderLargeTower());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTowerSmall.class, new RenderSmallTower());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCondenserPowered.class, new RenderCondenser());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRadiator.class, new RenderRadiator());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDeuteriumTower.class, new RenderDeuteriumTower());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAtmoTower.class, new RenderAtmoTower());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAtmosphericCompressor.class, new RenderAtmosphericCompressor());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineCatalyticCracker.class, new RenderCatalyticCracker());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineLiquefactor.class, new RenderLiquefactor());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineSolidifier.class, new RenderSolidifier());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineIntake.class, new RenderIntake());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAtmosphericEmitter.class, new RenderAtmosphericEmitter());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineCompressor.class, new RenderCompressor());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineCompressorCompact.class, new RenderCompressorCompact());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineDrain.class, new RenderDrain());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineRadiolysis.class, new RenderRadiolysis());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityElectrolyser.class, new RenderElectrolyser());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFurnaceIron.class, new RenderFurnaceIron());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFurnaceSteel.class, new RenderFurnaceSteel());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFurnaceCombination.class, new RenderFurnaceCombination());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHeaterFirebox.class, new RenderFirebox());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHeaterOven.class, new RenderHeatingOven());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAshpit.class, new RenderAshpit());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHeaterOilburner.class, new RenderOilburner());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHeaterElectric.class, new RenderElectricHeater());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHeaterHeatex.class, new RenderHeaterHeatex());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityStirling.class, new RenderStirling());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySawmill.class, new RenderSawmill());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCrucible.class, new RenderCrucible());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHeatBoiler.class, new RenderBoiler());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHeatBoilerIndustrial.class, new RenderIndustrialBoiler());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySteamEngine.class, new RenderSteamEngine());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineDiesel.class, new RenderDieselGen());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineCombustionEngine.class, new RenderCombustionEngine());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineExcavator.class, new RenderExcavator());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineOreSlopper.class, new RenderOreSlopper());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineMixer.class, new RenderMixer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineHephaestus.class, new RenderHephaestus());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineAutosaw.class, new RenderAutosaw());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineVacuumDistill.class, new RenderVacuumDistill());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineCatalyticReformer.class, new RenderCatalyticReformer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineMilkReformer.class, new RenderMilkReformer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineHydrotreater.class, new RenderHydrotreater());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineCoker.class, new RenderCoker());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachinePyroOven.class, new RenderPyroOven());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFan.class, new RenderFan());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineCryoDistill.class, new RenderCryoDistill());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineAlkylation.class, new RenderAlkylation());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineMagma.class, new RenderMagma());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHydroponic.class, new RenderHydroponic());

		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPistonInserter.class, new RenderPistonInserter());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityConveyorPress.class, new RenderConveyorPress());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRadioTelex.class, new RenderTelex());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachinePumpSteam.class, new RenderPump());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachinePumpElectric.class, new RenderPump());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineArcWelder.class, new RenderArcWelder());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineSolderingStation.class, new RenderSolderingStation());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineVacuumCircuit.class, new RenderVacuumCircuit());

		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineArcFurnaceLarge.class, new RenderArcFurnace());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineWoodBurner.class, new RenderWoodBurner());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFluidPump.class, new RenderFluidPump());
		//Foundry
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFoundryBasin.class, new RenderFoundry());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFoundryMold.class, new RenderFoundry());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFoundryChannel.class, new RenderFoundryFluid());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFoundryTank.class, new RenderFoundryFluid());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineStrandCaster.class, new RenderStrandCaster());
		//ZIRNOX
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityReactorZirnox.class, new RenderZirnox());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityZirnoxDestroyed.class, new RenderZirnoxDestroyed());
		//DFC
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCoreEmitter.class, new RenderCoreComponent());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCoreReceiver.class, new RenderCoreComponent());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCoreInjector.class, new RenderCoreComponent());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCoreStabilizer.class, new RenderCoreComponent());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCore.class, new RenderCore());
		//missile blocks
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLaunchPad.class, new RenderLaunchPad());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLaunchPadRocket.class, new RenderLaunchPadRocket());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLaunchPadRusted.class, new RenderLaunchPadRusted());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLaunchPadLarge.class, new RenderLaunchPadLarge());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineMissileAssembly.class, new RenderMissileAssembly());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineRocketAssembly.class, new RenderRocketAssembly());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCompactLauncher.class, new RenderCompactLauncher());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLaunchTable.class, new RenderLaunchTable());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySoyuzLauncher.class, new RenderSoyuzLauncher());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySoyuzCapsule.class, new RenderCapsule());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCombatDropPod.class, new RenderCombatPod());
		//network
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPylon.class, new RenderPylon());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityConnector.class, new RenderConnector());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityConnectorSuper.class, new RenderConnectorSuper());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPylonMedium.class, new RenderPylonMedium());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPylonLarge.class, new RenderPylonLarge());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySubstation.class, new RenderSubstation());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPipeAnchor.class, new RenderPipeAnchor());
		//chargers
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCharger.class, new RenderCharger());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRefueler.class, new RenderRefueler());
		//DecoContainer
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFileCabinet.class, new RenderFileCabinet());
		//multiblocks
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMultiblock.class, new RenderMultiblock());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySoyuzStruct.class, new RenderSoyuzMultiblock());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWatzStruct.class, new RenderWatzMultiblock());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityICFStruct.class, new RenderICFMultiblock());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCustomMachine.class, new RenderCustomMachine());
		//RBMK
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRBMKControlManual.class, new RenderRBMKControlRod());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRBMKControlAuto.class, new RenderRBMKControlRod());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCraneConsole.class, new RenderCraneConsole());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRBMKConsole.class, new RenderRBMKConsole());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRBMKDisplay.class, new RenderRBMKDisplay());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRBMKKeyPad.class, new RenderRBMKKeyPad());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRBMKGauge.class, new RenderRBMKGauge());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRBMKRod.class, new RenderRBMKFuelChannel());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRBMKRodReaSim.class, new RenderRBMKFuelChannel());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRBMKAutoloader.class, new RenderRBMKAutoloader());
		//ITER
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityITER.class, new RenderITER());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachinePlasmaHeater.class, new RenderPlasmaHeater());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityICF.class, new RenderICF());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityICFController.class, new RenderICFController());
		//Fusion
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFusionTorusStruct.class, new RenderFusionTorusMultiblock());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFusionTorus.class, new RenderFusionTorus());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFusionKlystron.class, new RenderFusionKlystron());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFusionKlystronCreative.class, new RenderFusionKlystronCreative());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFusionBreeder.class, new RenderFusionBreeder());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFusionCollector.class, new RenderFusionCollector());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFusionBoiler.class, new RenderFusionBoiler());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFusionMHDT.class, new RenderFusionMHDT());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFusionCoupler.class, new RenderFusionCoupler());
		//Watz
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWatz.class, new RenderWatz());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWatzPump.class, new RenderWatzPump());
		//doors
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBlastDoor.class, new RenderBlastDoor());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDoorGeneric.class, new RenderDoorGeneric());
		//storage
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMassStorage.class, new RenderMassStorage());
		//dyson
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDysonReceiver.class, new RenderDysonReceiver());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDysonConverterAnatmogenesis.class, new RenderDysonConverterAnatmogenesis());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDysonConverterHE.class, new RenderDysonConverterHE());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDysonConverterTU.class, new RenderDysonConverterTU());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDysonLauncher.class, new RenderDysonLauncher());
		//pretty
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityOrrery.class, new RenderOrrery());
		//NBTStructure
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWandStructure.class, new RenderWandStructure());
	}

	@Override
	public void registerItemRenderer() {
        MinecraftForge.EVENT_BUS.register(new com.hbm.render.item.nativeitem.callback.DeferredItemModelRegistration(this::initializeItemRendererModels));
        MinecraftForge.EVENT_BUS.register(new com.hbm.advancement.client.LegacyInventoryActionHandler(
                () -> com.hbm.packet.PacketDispatcher.wrapper.sendToServer(new com.hbm.packet.toserver.LegacyInventoryActionPacket())));
    }

    private void initializeItemRendererModels(net.minecraftforge.client.event.ModelRegistryEvent event) {
        nativeStaticItems = com.hbm.render.item.nativeitem.staticfamily.StaticItemFamilyBootstrap.register(event);

        java.util.Map<Item, com.hbm.render.item.nativeitem.direct.DirectMeshItemRenderer> directRenderers = new java.util.IdentityHashMap<>();
        ItemRenderLibrary.init();
        java.util.Map<Item, ItemRenderBase> nativeCallbacks = new java.util.IdentityHashMap<>();
        nativeCallbacks.putAll(ItemRenderLibrary.renderers);
        for(Object renderer : TileEntityRendererDispatcher.instance.renderers.values()) {
            if(renderer instanceof IItemRendererProvider) {
                IItemRendererProvider provider = (IItemRendererProvider) renderer;
                for(Item item : provider.getItemsForRenderer()) nativeCallbacks.put(item, provider.getRenderer());
            }
        }
        for(Item item : net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.getValuesCollection()) {
            if(item instanceof IItemRendererProvider) {
                IItemRendererProvider provider = (IItemRendererProvider) item;
                for(Item owned : provider.getItemsForRenderer()) nativeCallbacks.put(owned, provider.getRenderer());
            }
        }

        java.util.Map<Item, com.hbm.render.item.nativeitem.context.ContextItemRenderer> contextRenderers = new java.util.IdentityHashMap<>();
        java.util.Map<Item, com.hbm.render.item.nativeitem.staticfamily.SingleMeshEntityItemRenderer.BlockItemDraw> decoItemDraws = new java.util.IdentityHashMap<>();
		//universal JSON translated items
		double[] rtp = new double[] {0, 180, -90};
		double[] ttp_high = new double[] {0.125, 0.625, 0};
		double[] ttp_low = new double[] {0, 0.75, 0};
		double[] stp = new double[] {1.7, 1.7, 0.85};
		double[] rfp = new double[] {0, 180, -90};
		double[] tfp = new double[] {1.13, 5.2, -0.26};
		double[] sfp = new double[] {1.36, 1.36, 0.68};
		double[] rir = new double[] {0, 0, 0};
		double[] tir = new double[] {0, 0, 0};
		double[] sir = new double[] {1, 1, 1};

		contextRenderers.put(ModItems.titanium_sword, new ItemRenderTransformer(rtp, ttp_high, stp, rfp, tfp, sfp, rir, tir, sir));
		contextRenderers.put(ModItems.alloy_sword, new ItemRenderTransformer(rtp, ttp_high, stp, rfp, tfp, sfp, rir, tir, sir));
		contextRenderers.put(ModItems.desh_sword, new ItemRenderTransformer(rtp, ttp_low, stp, rfp, tfp, sfp, rir, tir, sir));
		contextRenderers.put(ModItems.cobalt_sword, new ItemRenderTransformer(rtp, ttp_high, stp, rfp, tfp, sfp, rir, tir, sir));
		contextRenderers.put(ModItems.cobalt_decorated_sword, new ItemRenderTransformer(rtp, ttp_high, stp, rfp, tfp, sfp, rir, tir, sir));
		contextRenderers.put(ModItems.starmetal_sword, new ItemRenderTransformer(rtp, ttp_high, stp, rfp, tfp, sfp, rir, tir, sir));
		contextRenderers.put(ModItems.schrabidium_sword, new ItemRenderTransformer(rtp, ttp_high, stp, rfp, tfp, sfp, rir, tir, sir));
		contextRenderers.put(ModItems.cmb_sword, new ItemRenderTransformer(rtp, ttp_high, stp, rfp, tfp, sfp, rir, tir, sir));
		contextRenderers.put(ModItems.dnt_sword, new ItemRenderTransformer(rtp, ttp_high, stp, rfp, tfp, sfp, rir, tir, sir));

		double[] sfp_default = new double[] {1, 1, 1};
		double[] tfp_default = new double[] {0, 0, 0};
		double[] sir_default = new double[] {1, 1, 1};

		contextRenderers.put(ModItems.bismuth_pickaxe, new ItemRenderTransformer(rtp, ttp_high, stp, rfp, tfp_default, sfp_default, rir, tir, sir_default));
		contextRenderers.put(ModItems.bismuth_axe, new ItemRenderTransformer(rtp, ttp_high, stp, rfp, tfp_default, sfp_default, rir, tir, sir_default));
		contextRenderers.put(ModItems.volcanic_pickaxe, new ItemRenderTransformer(rtp, ttp_high, stp, rfp, tfp_default, sfp_default, rir, tir, sir_default));
		contextRenderers.put(ModItems.volcanic_axe, new ItemRenderTransformer(rtp, ttp_high, stp, rfp, tfp_default, sfp_default, rir, tir, sir_default));
		contextRenderers.put(ModItems.chlorophyte_pickaxe, new ItemRenderTransformer(rtp, ttp_high, stp, rfp, tfp_default, sfp_default, rir, tir, sir_default));
		contextRenderers.put(ModItems.chlorophyte_axe, new ItemRenderTransformer(rtp, ttp_high, stp, rfp, tfp_default, sfp_default, rir, tir, sir_default));
		contextRenderers.put(ModItems.mese_pickaxe, new ItemRenderTransformer(rtp, ttp_high, stp, rfp, tfp_default, sfp_default, rir, tir, sir_default));
		contextRenderers.put(ModItems.mese_axe, new ItemRenderTransformer(rtp, ttp_high, stp, rfp, tfp_default, sfp_default, rir, tir, sir_default));

		ItemRenderMissileGeneric.init();
		directRenderers.put(ModItems.missile_test, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER0));
		directRenderers.put(ModItems.missile_taint, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER0));
		directRenderers.put(ModItems.missile_micro, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER0));
		directRenderers.put(ModItems.missile_bhole, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER0));
		directRenderers.put(ModItems.missile_schrabidium, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER0));
		directRenderers.put(ModItems.missile_emp, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER0));
		directRenderers.put(ModItems.missile_stealth, new ItemRenderMissileGeneric(RenderMissileType.TYPE_STEALTH));
		directRenderers.put(ModItems.missile_generic, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER1));
		directRenderers.put(ModItems.missile_incendiary, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER1));
		directRenderers.put(ModItems.missile_cluster, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER1));
		directRenderers.put(ModItems.missile_buster, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER1));
		directRenderers.put(ModItems.missile_decoy, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER1));
		directRenderers.put(ModItems.missile_anti_ballistic, new ItemRenderMissileGeneric(RenderMissileType.TYPE_ABM));
		directRenderers.put(ModItems.missile_strong, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER2));
		directRenderers.put(ModItems.missile_incendiary_strong, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER2));
		directRenderers.put(ModItems.missile_cluster_strong, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER2));
		directRenderers.put(ModItems.missile_buster_strong, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER2));
		directRenderers.put(ModItems.missile_emp_strong, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER2));
		directRenderers.put(ModItems.missile_burst, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER3));
		directRenderers.put(ModItems.missile_inferno, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER3));
		directRenderers.put(ModItems.missile_rain, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER3));
		directRenderers.put(ModItems.missile_drill, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER3));
		directRenderers.put(ModItems.missile_nuclear, new ItemRenderMissileGeneric(RenderMissileType.TYPE_NUCLEAR));
		directRenderers.put(ModItems.missile_nuclear_cluster, new ItemRenderMissileGeneric(RenderMissileType.TYPE_NUCLEAR));
		directRenderers.put(ModItems.missile_volcano, new ItemRenderMissileGeneric(RenderMissileType.TYPE_NUCLEAR));
		directRenderers.put(ModItems.missile_doomsday, new ItemRenderMissileGeneric(RenderMissileType.TYPE_NUCLEAR));
		directRenderers.put(ModItems.missile_doomsday_rusted, new ItemRenderMissileGeneric(RenderMissileType.TYPE_NUCLEAR));
		directRenderers.put(ModItems.missile_shuttle, new ItemRenderMissileGeneric(RenderMissileType.TYPE_ROBIN));

		nativeCallbacks.put(ModItems.battery_pack, new ItemRenderBatteryPack());
		if(com.hbm.items.weapon.ItemAmmoHIMARS.itemTypes[0] != null)
			com.hbm.render.item.nativeitem.NativeItemParticles.bind(ModItems.ammo_himars, com.hbm.items.weapon.ItemAmmoHIMARS.itemTypes[0].texture);
		com.hbm.render.item.nativeitem.NativeItemParticles.bind(Item.getItemFromBlock(ModBlocks.pa_source), ResourceManager.pa_source_tex);
		com.hbm.render.item.nativeitem.NativeItemParticles.bind(Item.getItemFromBlock(ModBlocks.pa_beamline), ResourceManager.pa_beamline_tex);
		com.hbm.render.item.nativeitem.NativeItemParticles.bind(Item.getItemFromBlock(ModBlocks.pa_rfc), ResourceManager.pa_rfc_tex);
		com.hbm.render.item.nativeitem.NativeItemParticles.bind(Item.getItemFromBlock(ModBlocks.pa_quadrupole), ResourceManager.pa_quadrupole_tex);
		com.hbm.render.item.nativeitem.NativeItemParticles.bind(Item.getItemFromBlock(ModBlocks.pa_dipole), ResourceManager.pa_dipole_tex);
		com.hbm.render.item.nativeitem.NativeItemParticles.bind(Item.getItemFromBlock(ModBlocks.pa_detector), ResourceManager.pa_detector_tex);
		// core_angel is owned by the native AngelCoreModelHandler registered above.

		//templates
		contextRenderers.put(ModItems.crucible_template, new ItemRenderTemplate());
		//hot stuff
		contextRenderers.put(ModItems.ingot_steel_dusted, new ItemRendererHot("ingot_steel_dusted"));
		contextRenderers.put(ModItems.ingot_chainsteel, new ItemRendererHot("ingot_chainsteel"));
		contextRenderers.put(ModItems.ingot_meteorite, new ItemRendererHot("ingot_meteorite"));
		contextRenderers.put(ModItems.ingot_meteorite_forged, new ItemRendererHot("ingot_meteorite_forged"));
		contextRenderers.put(ModItems.blade_meteorite, new ItemRendererHot("blade_meteorite"));
		//meteorite swords
		contextRenderers.put(ModItems.meteorite_sword_seared, new ItemRendererMeteorSword(1.0F, 0.5F, 0.0F));
		contextRenderers.put(ModItems.meteorite_sword_reforged, new ItemRendererMeteorSword(0.5F, 1.0F, 1.0F));
		contextRenderers.put(ModItems.meteorite_sword_hardened, new ItemRendererMeteorSword(0.25F, 0.25F, 0.25F));
		contextRenderers.put(ModItems.meteorite_sword_alloyed, new ItemRendererMeteorSword(0.0F, 0.5F, 1.0F));
		contextRenderers.put(ModItems.meteorite_sword_machined, new ItemRendererMeteorSword(1.0F, 1.0F, 0.0F));
		contextRenderers.put(ModItems.meteorite_sword_treated, new ItemRendererMeteorSword(0.5F, 1.0F, 0.5F));
		contextRenderers.put(ModItems.meteorite_sword_etched, new ItemRendererMeteorSword(1.0F, 1.0F, 0.5F));
		contextRenderers.put(ModItems.meteorite_sword_bred, new ItemRendererMeteorSword(0.5F, 0.5F, 0.0F));
		contextRenderers.put(ModItems.meteorite_sword_irradiated, new ItemRendererMeteorSword(0.75F, 1.0F, 0.0F));
		contextRenderers.put(ModItems.meteorite_sword_fused, new ItemRendererMeteorSword(1.0F, 0.0F, 0.5F));
		contextRenderers.put(ModItems.meteorite_sword_baleful, new ItemRendererMeteorSword(0.0F, 1.0F, 0.0F));
		//swords and hammers
		contextRenderers.put(ModItems.redstone_sword, new ItemRenderRedstoneSword());
		contextRenderers.put(ModItems.big_sword, new ItemRenderBigSword());
		contextRenderers.put(ModItems.shimmer_sledge, new ItemRenderShim());
		contextRenderers.put(ModItems.shimmer_axe, new ItemRenderShim());
		contextRenderers.put(ModItems.stopsign, new ItemRenderShim());
		contextRenderers.put(ModItems.sopsign, new ItemRenderShim());
		contextRenderers.put(ModItems.chernobylsign, new ItemRenderShim());
		contextRenderers.put(ModItems.wood_gavel, new ItemRenderGavel());
		contextRenderers.put(ModItems.lead_gavel, new ItemRenderGavel());
		contextRenderers.put(ModItems.diamond_gavel, new ItemRenderGavel());
		contextRenderers.put(ModItems.mese_gavel, new ItemRenderGavel());
		contextRenderers.put(ModItems.crucible, new ItemRenderCrucible());
		contextRenderers.put(ModItems.chainsaw, new ItemRenderChainsaw());
        contextRenderers.put(ModItems.boltgun, new ItemRenderBoltgun());
		//guns
		contextRenderers.put(ModItems.gun_b92, new ItemRenderGunAnim());
		directRenderers.put(ModItems.gun_fireext, new ItemRenderFireExt());
		com.hbm.render.item.nativeitem.NativeItemParticles.bind(ModItems.gun_fireext, ResourceManager.fireext_tex);
		contextRenderers.put(ModItems.detonator_laser, new ItemRenderDetonatorLaser());
		//blocks
		ItemRenderDecoBlock roofItemRenderer = new ItemRenderDecoBlock();
        contextRenderers.put(Item.getItemFromBlock(ModBlocks.steel_roof), roofItemRenderer);
        decoItemDraws.put(Item.getItemFromBlock(ModBlocks.steel_roof), roofItemRenderer);
        com.hbm.render.item.block.BlockItemRegistration blockItems = new com.hbm.render.item.block.BlockItemRegistration(
                ModItems.conveyor_wand, new ItemRenderBlock(ModBlocks.conveyor, ModBlocks.conveyor_express, ModBlocks.conveyor_double, ModBlocks.conveyor_triple));
        blockItems.register(event);
        MinecraftForge.EVENT_BUS.register(blockItems);
        nativeBlockItemDraws = new java.util.IdentityHashMap<>(blockItems.droppedRenderers());
        nativeDecoItemDraws = decoItemDraws;
        com.hbm.render.item.nativeitem.callback.CallbackItemRegistration callbacks =
                new com.hbm.render.item.nativeitem.callback.CallbackItemRegistration(nativeCallbacks);
        callbacks.register(event);
        MinecraftForge.EVENT_BUS.register(callbacks);
        java.util.Set<Item> oneMeshItems = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<Item, Boolean>());
        oneMeshItems.addAll(nativeStaticItems);
        oneMeshItems.addAll(callbacks.items());
        com.hbm.render.item.nativeitem.direct.DirectMeshItemRegistration direct = new com.hbm.render.item.nativeitem.direct.DirectMeshItemRegistration(directRenderers);
        direct.register(event);
        MinecraftForge.EVENT_BUS.register(direct);
        oneMeshItems.addAll(direct.items());
        nativeOneMeshItems = oneMeshItems;
        initializeNativeMissileParts();
        for(MissilePart part : MissilePart.parts.values()) {
            ItemRenderMissilePart renderer = new ItemRenderMissilePart(part);
            contextRenderers.put(part.part, renderer);
            if(part.texture != null) com.hbm.render.item.nativeitem.NativeItemParticles.bind(part.part, part.texture);
        }
        contextRenderers.put(ModItems.missile_custom, new ItemRenderMissile());
        for(java.util.Map.Entry<Item, com.hbm.render.item.nativeitem.context.ContextItemRenderer> entry : contextRenderers.entrySet()) {
            if(entry.getValue() instanceof com.hbm.render.item.nativeitem.context.NativeOriginalItemRenderer)
                decoItemDraws.put(entry.getKey(), (com.hbm.render.item.nativeitem.context.NativeOriginalItemRenderer)entry.getValue());
        }
        nativeContextItems = new com.hbm.render.item.nativeitem.context.ContextItemRegistration(contextRenderers, MainRegistry.logger::warn);
        nativeContextItems.register(event);
        MinecraftForge.EVENT_BUS.register(nativeContextItems);
        java.util.Map<Item, com.hbm.render.item.nativeitem.context.ContextItemRenderer> sednaRenderers = new java.util.IdentityHashMap<>();
        sednaRenderers.putAll(GunFactoryClient.createItemRenderers());
        nativeSednaItems = new com.hbm.render.item.nativeitem.context.ContextItemRegistration(sednaRenderers, MainRegistry.logger::warn);
        nativeSednaItems.register(event);
        MinecraftForge.EVENT_BUS.register(nativeSednaItems);
    }


    @Override
    public void onLoadComplete() {
        if(nativeItemOwnershipFinished) return;
        if(nativeContextItems == null || nativeSednaItems == null || nativeOneMeshItems == null)
            throw new IllegalStateException("Native item model registration did not precede load completion");
        nativeItemOwnershipFinished = true;
        com.hbm.render.item.nativeitem.context.NativeItemRendererOwnership ownership =
                new com.hbm.render.item.nativeitem.context.NativeItemRendererOwnership(nativeContextItems);
        if(ownership.install()) {
            for(Item item : nativeContextItems.items())
                if(!(item.getTileEntityItemStackRenderer() instanceof com.hbm.render.item.nativeitem.context.NativeSelectiveItemRenderer)) nativeOneMeshItems.add(item);
            nativeBlockItemDraws.putAll(nativeDecoItemDraws);
            // Sedna additionally requires owned native player/armor pose models.
            // Its separate transaction can abort without disabling Boltgun.
            com.hbm.render.item.nativeitem.context.SednaPlayerOwnership sedna =
                    new com.hbm.render.item.nativeitem.context.SednaPlayerOwnership(nativeSednaItems, ownership.getInstalled(), MainRegistry.logger::warn);
            if(sedna.install()) nativeOneMeshItems.addAll(nativeSednaItems.items());
        }
        // Preserve exact native dropped ownership during context preflight. An
        // aborted context transaction must not change fallback item counting.
        nativeOneMeshItems.removeAll(nativeBlockItemDraws.keySet()); // Block-helper dropped rendering takes precedence.
        MinecraftForge.EVENT_BUS.register(new com.hbm.render.item.nativeitem.staticfamily.StaticItemEntityOwnership(nativeOneMeshItems, nativeBlockItemDraws, MainRegistry.logger::warn));
    }

	@Override
	public void registerEntityRenderer() {
		// Snowball renderers must capture the manager supplied by Forge and the
		// live item renderer when its factory runs inside RenderManager construction.
		// The global manager is not assigned yet. Keep the exact native renderer
		// class so load-complete item ownership sees and updates its item cache.
		//projectiles
		RenderingRegistry.registerEntityRenderingHandler(EntityRocket.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.man_core, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntitySchrab.class, renderManager -> new RenderFlare(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityBullet.class, renderManager -> new RenderRocket(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityBulletBaseNT.class, RenderBullet::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityBulletBaseMK4.class, renderManager -> new RenderBulletMK4(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityBulletBaseMK4CL.class, renderManager -> new RenderBulletMK4(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityBulletBeamBase.class, renderManager -> new RenderBeam(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityExplosiveBeam.class, renderManager -> new RenderBeam5(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntitySiegeLaser.class, renderManager -> new RenderSiegeLaser(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityBombletZeta.class, renderManager -> new RenderBombletTheta(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMeteor.class, renderManager -> new RenderMeteor(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityBoxcar.class, renderManager -> new RenderBoxcar(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityDuchessGambit.class, renderManager -> new RenderBoxcar(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityBuilding.class, renderManager -> new RenderBoxcar(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityTorpedo.class, renderManager -> new RenderBoxcar(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityBomber.class, renderManager -> new RenderBomber(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityC130.class, renderManager -> new RenderC130(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityBurningFOEQ.class, renderManager -> new RenderFOEQ(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityFallingNuke.class, renderManager -> new RenderFallingNuke(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMinerRocket.class, renderManager -> new RenderMinerRocket(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityBobmazon.class, renderManager -> new RenderMinerRocket(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityTom.class, renderManager -> new RenderTom(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityChopperMine.class, renderManager -> new RenderChopperMine(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityRubble.class, RenderRubble::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityShrapnel.class, renderManager -> new RenderShrapnel(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityFireworks.class, renderManager -> new RenderShrapnel(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityWaterSplash.class, renderManager -> new RenderEmpty(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityEMP.class, renderManager -> new RenderEmpty(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityBeamVortex.class, renderManager -> new RenderVortexBeam(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityRBMKDebris.class, renderManager -> new RenderRBMKDebris(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityZirnoxDebris.class, renderManager -> new RenderZirnoxDebris(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityArtilleryShell.class, renderManager -> new RenderArtilleryShell(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityArtilleryRocket.class, renderManager -> new RenderArtilleryRocket(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityCog.class, renderManager -> new RenderCog(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntitySawblade.class, renderManager -> new RenderSawblade(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityCoin.class, renderManager -> new RenderCoin(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityChemical.class, renderManager -> new RenderChemical(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMist.class, renderManager -> new RenderMist(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityFireLingering.class, renderManager -> new RenderMist(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityWaypoint.class, renderManager -> new RenderMist(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityAcidBomb.class, renderManager -> new RenderSnowball<>(renderManager, Items.SLIME_BALL, Minecraft.getMinecraft().getRenderItem()));
		//grenades
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeGeneric.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_generic, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeStrong.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_strong, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeFrag.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_frag, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeFire.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_fire, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeCluster.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_cluster, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeFlare.class, renderManager -> new RenderFlare(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeElectric.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_electric, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadePoison.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_poison, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeGas.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_gas, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeSchrabidium.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_schrabidium, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeNuke.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_nuke, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeNuclear.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_nuclear, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadePlasma.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_plasma, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeTau.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_tau, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeLemon.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_lemon, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeMk2.class, renderManager -> new RenderGrenade(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeZOMG.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_zomg, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeASchrab.class, renderManager -> new RenderGrenade(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadePulse.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_pulse, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeShrapnel.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_shrapnel, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeBlackHole.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_black_hole, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeGascan.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_gascan, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeCloud.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_cloud, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadePC.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_pink_cloud, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeSmart.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_smart, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeMIRV.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_mirv, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeBreach.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_breach, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeBurst.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_burst, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeIFGeneric.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_if_generic, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeIFHE.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_if_he, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeIFBouncy.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_if_bouncy, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeIFSticky.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_if_sticky, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeIFImpact.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_if_impact, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeIFIncendiary.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_if_incendiary, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeIFToxic.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_if_toxic, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeIFConcussion.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_if_concussion, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeIFBrimstone.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_if_brimstone, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeIFMystery.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_if_mystery, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeIFSpark.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_if_spark, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeIFHopwire.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_if_hopwire, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeIFNull.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.grenade_if_null, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityWastePearl.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.nuclear_waste_pearl, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeDynamite.class, renderManager -> new RenderSnowball<>(renderManager, ModItems.stick_dynamite, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeBouncyGeneric.class, renderManager -> new RenderGenericGrenade(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeImpactGeneric.class, renderManager -> new RenderGenericGrenade(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityDisperserCanister.class, renderManager -> new RenderGenericGrenade(renderManager));
		//missiles
		RenderingRegistry.registerEntityRenderingHandler(EntityMissileCustom.class, renderManager -> new RenderMissileCustom(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityRideableRocket.class, renderManager -> new RenderRocketCustom(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityRideableRocketDummy.class, renderManager -> new RenderEmpty(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMissileGeneric.class, renderManager -> new RenderMissileGeneric(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMissileDecoy.class, renderManager -> new RenderMissileGeneric(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMissileStealth.class, renderManager -> new RenderMissileGeneric(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMissileAntiBallistic.class, renderManager -> new RenderMissileGeneric(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMissileIncendiary.class, renderManager -> new RenderMissileGeneric(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMissileCluster.class, renderManager -> new RenderMissileGeneric(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMissileBunkerBuster.class, renderManager -> new RenderMissileGeneric(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMissileStrong.class, renderManager -> new RenderMissileStrong(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMissileIncendiaryStrong.class, renderManager -> new RenderMissileStrong(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMissileClusterStrong.class, renderManager -> new RenderMissileStrong(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMissileBusterStrong.class, renderManager -> new RenderMissileStrong(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMissileEMPStrong.class, renderManager -> new RenderMissileStrong(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMissileBurst.class, renderManager -> new RenderMissileHuge(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMissileInferno.class, renderManager -> new RenderMissileHuge(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMissileRain.class, renderManager -> new RenderMissileHuge(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMissileDrill.class, renderManager -> new RenderMissileHuge(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMissileNuclear.class, renderManager -> new RenderMissileNuclear(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMissileMirv.class, renderManager -> new RenderMissileNuclear(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMissileVolcano.class, renderManager -> new RenderMissileNuclear(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMissileDoomsday.class, renderManager -> new RenderMissileNuclear(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMissileDoomsdayRusted.class, renderManager -> new RenderMissileNuclear(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntitySoyuz.class, renderManager -> new RenderSoyuz(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntitySoyuzCapsule.class, renderManager -> new RenderSoyuzCapsule(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityCombatDropPod.class, renderManager -> new RenderCombatDropPod(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntitySiegeUFO.class, renderManager -> new RenderSiegeUFO(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityParachuteCrate.class, renderManager -> new RenderParachuteCrate(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMissileTaint.class, renderManager -> new RenderMissileTaint(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMissileMicro.class, renderManager -> new RenderMissileTaint(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMissileBHole.class, renderManager -> new RenderMissileTaint(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMissileSchrabidium.class, renderManager -> new RenderMissileTaint(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMissileEMP.class, renderManager -> new RenderMissileTaint(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMissileShuttle.class, renderManager -> new RenderMissileShuttle(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMissileTest.class, renderManager -> new RenderMissileTaint(renderManager));
		//effects
		RenderingRegistry.registerEntityRenderingHandler(EntityCloudFleija.class, renderManager -> new RenderCloudFleija(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityCloudFleijaRainbow.class, renderManager -> new RenderCloudRainbow(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityCloudSolinium.class, renderManager -> new RenderCloudSolinium(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityCloudTom.class, renderManager -> new RenderCloudTom(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityFalloutRain.class, renderManager -> new RenderFallout(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityBlackHole.class, renderManager -> new RenderBlackHole(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityVortex.class, renderManager -> new RenderBlackHole(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityRagingVortex.class, renderManager -> new RenderBlackHole(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityQuasar.class, renderManager -> new RenderQuasar(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityDeathBlast.class, renderManager -> new RenderDeathBlast(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntitySpear.class, renderManager -> new RenderSpear(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityNukeTorex.class, renderManager -> new RenderTorex(renderManager));
		//minecarts
		RenderingRegistry.registerEntityRenderingHandler(EntityMinecartTest.class, renderManager -> new RenderMinecartTest(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMinecartCrate.class, renderManager -> new RenderMinecart(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMinecartNTM.class, renderManager -> new RenderNeoCart(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMagnusCartus.class, renderManager -> new RenderMagnusCartus(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityBoatRubber.class, renderManager -> new RenderBoatRubber(renderManager));
		//trains
		RenderingRegistry.registerEntityRenderingHandler(SeatDummyEntity.class, renderManager -> new RenderEmpty(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(BoundingBoxDummyEntity.class, renderManager -> new RenderEmpty(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(TrainCargoTram.class, renderManager -> new RenderTrainCargoTram(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(TrainCargoTramTrailer.class, renderManager -> new RenderTrainCargoTramTrailer(renderManager));
		//items
		RenderingRegistry.registerEntityRenderingHandler(EntityMovingItem.class, renderManager -> new RenderMovingItem(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityMovingPackage.class, renderManager -> new RenderMovingPackage(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityTNTPrimedBase.class, RenderTNTPrimedBase::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityDeliveryDrone.class, renderManager -> new RenderDeliveryDrone(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityRequestDrone.class, renderManager -> new RenderDeliveryDrone(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityFallingBlockNT.class, RenderFallingBlockNT::new);
		//mobs
		RenderingRegistry.registerEntityRenderingHandler(EntityCreeperNuclear.class, renderManager -> new RenderCreeperUniversal(renderManager, RefStrings.MODID + ":" + "textures/entity/creeper.png", RefStrings.MODID + ":" + "textures/entity/creeper_armor.png").setSwellMod(5F));
		RenderingRegistry.registerEntityRenderingHandler(EntityCreeperTainted.class, renderManager -> new RenderCreeperUniversal(renderManager, RefStrings.MODID + ":" + "textures/entity/creeper_tainted.png", RefStrings.MODID + ":" + "textures/entity/creeper_armor_taint.png"));
		RenderingRegistry.registerEntityRenderingHandler(EntityCreeperPhosgene.class, renderManager -> new RenderCreeperUniversal(renderManager, RefStrings.MODID + ":" + "textures/entity/creeper_phosgene.png", "textures/entity/creeper/creeper_armor.png"));
		RenderingRegistry.registerEntityRenderingHandler(EntityCreeperVolatile.class, renderManager -> new RenderCreeperUniversal(renderManager, RefStrings.MODID + ":" + "textures/entity/creeper_volatile.png", "textures/entity/creeper/creeper_armor.png"));
		RenderingRegistry.registerEntityRenderingHandler(EntityCreeperGold.class, renderManager -> new RenderCreeperUniversal(renderManager, RefStrings.MODID + ":" + "textures/entity/creeper_gold.png", "textures/entity/creeper/creeper_armor.png"));
		RenderingRegistry.registerEntityRenderingHandler(EntityCreeperFlesh.class, renderManager -> new RenderCreeperUniversal(renderManager, RefStrings.MODID + ":" + "textures/entity/creeper_flesh.png", "textures/entity/creeper/creeper_armor.png"));
		RenderingRegistry.registerEntityRenderingHandler(EntityHunterChopper.class, renderManager -> new RenderHunterChopper(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityCyberCrab.class, RenderCyberCrab::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityTeslaCrab.class, RenderTeslaCrab::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityTaintCrab.class, RenderTaintCrab::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityMaskMan.class, RenderMaskMan::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityBOTPrimeHead.class, renderManager -> new RenderWormHead(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityBOTPrimeBody.class, renderManager -> new RenderWormBody(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityDuck.class, renderManager -> new RenderDuck(renderManager, new ModelChicken(), 0.3F));
		RenderingRegistry.registerEntityRenderingHandler(EntityQuackos.class, renderManager -> new RenderQuacc(renderManager, new ModelChicken(), 7.5F));
		RenderingRegistry.registerEntityRenderingHandler(EntityFBI.class, RenderFBI::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityRADBeast.class, RenderRADBeast::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityBlockSpider.class, RenderBlockSpider::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityUFO.class, renderManager -> new RenderUFO(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityBFAngel.class, renderManager -> new RenderBFA(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntitySiegeCraft.class, renderManager -> new RenderSiegeCraft(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntitySiegeTunneler.class, renderManager -> new RenderSiegeTunneler(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityGhost.class, renderManager -> new RenderGhost(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityGlyphid.class, RenderGlyphid::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityGlyphidBrawler.class, RenderGlyphid::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityGlyphidBehemoth.class, RenderGlyphid::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityGlyphidBrenda.class, RenderGlyphid::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityGlyphidBombardier.class, RenderGlyphid::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityGlyphidBlaster.class, RenderGlyphid::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityGlyphidScout.class, RenderGlyphid::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityGlyphidNuclear.class, RenderGlyphidNuclear::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityParasiteMaggot.class, RenderMaggot::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityFBIDrone.class, renderManager -> new RenderDrone(renderManager));
		RenderPlasticBag.getModel(); // Preserve eager static resource sampling before factory construction.
		RenderingRegistry.registerEntityRenderingHandler(EntityPlasticBag.class, RenderPlasticBag::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityPigeon.class, renderManager -> new RenderPigeon(renderManager, new ModelPigeon(), 0.3F));

		RenderingRegistry.registerEntityRenderingHandler(EntityMoonCow.class, renderManager -> new RenderMoonCow(renderManager, new ModelMoonCow(), 0.7F));
		RenderingRegistry.registerEntityRenderingHandler(EntityScutterfish.class, renderManager -> new RenderScutter(renderManager, new ModelScutter(), 0.3F));
		RenderingRegistry.registerEntityRenderingHandler(EntityTankbot.class, RenderTankbot::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityWarBehemoth.class, RenderBehemoth::new);

		RenderingRegistry.registerEntityRenderingHandler(EntityScuttlecrab.class, renderManager -> new RenderEntityMulti(renderManager, new ModelScuttlecrab(), EntityScuttlecrab.Scuttlecrab.class, 0.5F));
		RenderingRegistry.registerEntityRenderingHandler(EntityDepthSquid.class, renderManager -> new RenderEntityMulti(renderManager, new ModelDepthSquid(), EntityDepthSquid.DepthSquid.class, 0.5F));
		RenderingRegistry.registerEntityRenderingHandler(EntityScrapFish.class, renderManager -> new RenderEntityMulti(renderManager, new ModelScrapFish(), EntityScrapFish.ScrapFish.class, 0.5F));
		RenderingRegistry.registerEntityRenderingHandler(EntitySifterEel.class, renderManager -> new RenderEntityMulti(renderManager, new ModelSifterEel(), EntitySifterEel.SifterEel.class, 0.5F));

		RenderingRegistry.registerEntityRenderingHandler(EntityDummy.class, renderManager -> new RenderDummy(renderManager));
		RenderingRegistry.registerEntityRenderingHandler(EntityUndeadSoldier.class, RenderUndeadSoldier::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityDepress.class, renderManager -> new RenderEmpty(renderManager));

		//"particles" (abysmal dogshit)
		final Item[] rendererItemsEntityChlorineFX = new Item[] { ModItems.chlorine1, ModItems.chlorine2, ModItems.chlorine3, ModItems.chlorine4, ModItems.chlorine5, ModItems.chlorine6, ModItems.chlorine7, ModItems.chlorine8 };
		RenderingRegistry.registerEntityRenderingHandler(EntityChlorineFX.class, renderManager -> new MultiCloudRenderer(renderManager, rendererItemsEntityChlorineFX));
		final Item[] rendererItemsEntityPinkCloudFX = new Item[] { ModItems.pc1, ModItems.pc2, ModItems.pc3, ModItems.pc4, ModItems.pc5, ModItems.pc6, ModItems.pc7, ModItems.pc8 };
		RenderingRegistry.registerEntityRenderingHandler(EntityPinkCloudFX.class, renderManager -> new MultiCloudRenderer(renderManager, rendererItemsEntityPinkCloudFX));
		final Item[] rendererItemsEntityCloudFX = new Item[] { ModItems.cloud1, ModItems.cloud2, ModItems.cloud3, ModItems.cloud4, ModItems.cloud5, ModItems.cloud6, ModItems.cloud7, ModItems.cloud8 };
		RenderingRegistry.registerEntityRenderingHandler(com.hbm.entity.particle.EntityCloudFX.class, renderManager -> new MultiCloudRenderer(renderManager, rendererItemsEntityCloudFX));
		final Item[] rendererItemsEntityOrangeFX = new Item[] { ModItems.orange1, ModItems.orange2, ModItems.orange3, ModItems.orange4, ModItems.orange5, ModItems.orange6, ModItems.orange7, ModItems.orange8 };
		RenderingRegistry.registerEntityRenderingHandler(EntityOrangeFX.class, renderManager -> new MultiCloudRenderer(renderManager, rendererItemsEntityOrangeFX));
		RenderingRegistry.registerEntityRenderingHandler(EntityFogFX.class, renderManager -> new FogRenderer(renderManager));
		// Keep eager resource sampling here; RenderManager exists only when the native factory runs.
		final com.hbm.render.mesh.ReloadableObjMesh empRing = RenderEMPBlast.loadRingModel();
		RenderingRegistry.registerEntityRenderingHandler(EntityEMPBlast.class, manager -> new RenderEMPBlast(manager, empRing));
	}

	@Override
	public void registerBlockRenderer() {


		/// STOP DOING THIS ///
		// DetCordModelHandler owns the native detonator cord model.
		// BoxDuctModelHandler owns native box-duct world, inventory and original damage models.
		// RadioTorchModelHandler owns the native RTTY world and flat item models.
		// SplitterModelHandler owns the native splitter OBJ, item and damage routes.


		// RailModelHandler owns the original rail/funnel native world and inventory models.

		// RBMKModelHandler owns the eighteen native column block/item models.

		// WandModelHandler owns the native wand_air block and flat item models.
	}

    private boolean nativeMissilePartsInitialized;

    private void initializeNativeMissileParts() {
        if(nativeMissilePartsInitialized) return;
        MissilePart.registerAllParts();
        nativeMissilePartsInitialized = true;
    }

    @Override
    public void registerMissileItems() {
        // Native model registration happens before postInit. Keep that same part identity map.
        initializeNativeMissileParts();
    }

	//mk3, only use this one
	@Override
	public void effectNT(NBTTagCompound data) {

		World world = Minecraft.getMinecraft().world;

		if(world == null) //might i ask why?
			return;

		TextureManager man = Minecraft.getMinecraft().renderEngine;
		EntityPlayer player = Minecraft.getMinecraft().player;
		int particleSetting = Minecraft.getMinecraft().gameSettings.particleSetting;
		Random rand = world.rand;
		String type = data.getString("type");
		double x = data.getDouble("posX");
		double y = data.getDouble("posY");
		double z = data.getDouble("posZ");

		boolean inOrbit = CelestialBody.inOrbit(world);
		CBT_Atmosphere atmosphere = !inOrbit ? CelestialBody.getTrait(world, CBT_Atmosphere.class) : null;
		double pressure = atmosphere != null ? atmosphere.getPressure() : 0;

		if(ParticleCreators.particleCreators.containsKey(type)) {
			ParticleCreators.particleCreators.get(type).makeParticle(world, player, man, rand, x, y, z, data);
			return;
		}

		// Old MK1 system ported to MK3:

		if("waterSplash".equals(type)) {
			for (int i = 0; i < 10; i++) {
				NativeVanillaParticles.Cloud smoke = new NativeVanillaParticles.Cloud(world, x + world.rand.nextGaussian(), y + world.rand.nextGaussian(), z + world.rand.nextGaussian(), 0.0, 0.0, 0.0);
				Minecraft.getMinecraft().effectRenderer.addEffect(smoke);
			}
		}

		if("cloudFX2".equals(type)) { // i have genuinely no idea what used this
			NativeVanillaParticles.Cloud smoke = new NativeVanillaParticles.Cloud(world, x, y, z, 0.0, 0.1, 0.0);
			Minecraft.getMinecraft().effectRenderer.addEffect(smoke);
		}

		if("ABMContrail".equals(type)) {
			ParticleContrail contrail = new ParticleContrail(man, world, x, y, z);
			Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
		}

		// End MK1 porting.

		// Old MK2 system ported to MK3:

		if("launchSmoke".equals(type)) {
			ParticleSmokePlume contrail = new ParticleSmokePlume(man, world, x, y, z);
			contrail.setMotion(data.getDouble("moX"), data.getDouble("moY"), data.getDouble("moZ"));
			Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
		}

		if("exKerosene".equals(type)) {
			ParticleContrail contrail = new ParticleContrail(man, world, x, y, z, 0F, 0F, 0F, 1F);
			Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
		}

		if("exSolid".equals(type)) {
			ParticleContrail contrail = new ParticleContrail(man, world, x, y, z, 0.3F, 0.2F, 0.05F, 1F);
			Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
		}

		if("exHydrogen".equals(type)) {
			ParticleContrail contrail = new ParticleContrail(man, world, x, y, z, 0.7F, 0.7F, 0.7F, 1F);
			Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
		}

		if("exBalefire".equals(type)) {
			ParticleContrail contrail = new ParticleContrail(man, world, x, y, z, 0.2F, 0.7F, 0.2F, 1F);
			Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
		}
		if("exHydrazine".equals(type)) {
			ParticleContrail contrail = new ParticleContrail(man, world, x, y, z);
			Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
		}

		if("radFog".equals(type)) {
			ParticleRadiationFog contrail = new ParticleRadiationFog(man, world, x, y, z);
			Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
		}

		// End MK2 porting.

		if("spinlaunch".equals(type)) {
			int count = data.getInteger("count");

			float scale = data.hasKey("scale") ? data.getFloat("scale") : 1F;
			double mX = data.getDouble("moX");
			double mY = data.getDouble("moY");
			double mZ = data.getDouble("moZ");

			int maxAge = data.getInteger("maxAge");

			for(int i = 0; i < count; i++) {
				double ox = world.rand.nextDouble() * 2 - 1;
				double oy = world.rand.nextDouble() * 2 - 1;
				double oz = world.rand.nextDouble() * 2 - 1;
				double mult = 1.0 - world.rand.nextDouble() * 0.1;

				ParticleRocketFlame fx = new ParticleRocketFlame(man, world, x + ox, y + oy, z + oz).setScale(scale);
				fx.setMotion(mX * mult + ox, mY * mult + oy, mZ * mult + oz);
				if(maxAge > 0) fx.setMaxAge(maxAge + world.rand.nextInt(5));
				Minecraft.getMinecraft().effectRenderer.addEffect(fx);
			}
		}

		if("missileContrail".equals(type) || "depress".equals(type)) {

			if(player == null || new Vec3d(player.posX - x, player.posY - y, player.posZ - z).length() > 350) return;

			float scale = data.hasKey("scale") ? data.getFloat("scale") : 1F;
			double mX = data.getDouble("moX");
			double mY = data.getDouble("moY");
			double mZ = data.getDouble("moZ");

			ParticleRocketFlame fx = new ParticleRocketFlame(man, world, x, y, z).setScale(scale);
			fx.setMotion(mX, mY, mZ);
			if(data.hasKey("maxAge")) fx.setMaxAge(data.getInteger("maxAge"));
			if(data.hasKey("color")) {
				Color color = new Color(data.getInteger("color"));
				fx.setCustomColor(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
			}
			if(!"depress".equals(type)) fx.setAtmosphericPressure(pressure);
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		}

		if("missileContrailf".equals(type)) {

			if(player == null || new Vec3d(player.posX - x, player.posY - y, player.posZ - z).length() > 350) return;

			float scale = data.hasKey("scale") ? data.getFloat("scale") : 1F;
			double mX = data.getDouble("moX");
			double mY = data.getDouble("moY");
			double mZ = data.getDouble("moZ");

			ParticleRocketFusion fx = new ParticleRocketFusion(man, world, x, y, z);
			fx.setScale(scale);
			fx.setMotion(mX, mY, mZ);
			if(data.hasKey("maxAge")) fx.setMaxAge(data.getInteger("maxAge"));
			fx.setAtmosphericPressure(pressure);
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		}

		if("missileContrailbf".equals(type)) {

			if(player == null || new Vec3d(player.posX - x, player.posY - y, player.posZ - z).length() > 350) return;

			float scale = data.hasKey("scale") ? data.getFloat("scale") : 1F;
			double mX = data.getDouble("moX");
			double mY = data.getDouble("moY");
			double mZ = data.getDouble("moZ");

			ParticleRocketBF fx = new ParticleRocketBF(man, world, x, y, z);
			fx.setScale(scale);
			fx.setMotion(mX, mY, mZ);
			if(data.hasKey("maxAge")) fx.setMaxAge(data.getInteger("maxAge"));
			fx.setAtmosphericPressure(pressure);
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		}

		if("missileContrailMUD".equals(type)) {

			if(player == null || new Vec3d(player.posX - x, player.posY - y, player.posZ - z).length() > 350) return;

			float scale = data.hasKey("scale") ? data.getFloat("scale") : 1F;
			double mX = data.getDouble("moX");
			double mY = data.getDouble("moY");
			double mZ = data.getDouble("moZ");

			ParticleRocketMUD fx = new ParticleRocketMUD(man, world, x, y, z);
			fx.setScale(scale);
			fx.setMotion(mX, mY, mZ);
			if(data.hasKey("maxAge")) fx.setMaxAge(data.getInteger("maxAge"));
			fx.setAtmosphericPressure(pressure);
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		}

		if("missileContrailSCH".equals(type)) {

			if(player == null || new Vec3d(player.posX - x, player.posY - y, player.posZ - z).length() > 350) return;

			float scale = data.hasKey("scale") ? data.getFloat("scale") : 1F;
			double mX = data.getDouble("moX");
			double mY = data.getDouble("moY");
			double mZ = data.getDouble("moZ");

			ParticleRocketSCH fx = new ParticleRocketSCH(man, world, x, y, z);
			fx.setScale(scale);
			fx.setMotion(mX, mY, mZ);
			if(data.hasKey("maxAge")) fx.setMaxAge(data.getInteger("maxAge"));
			fx.setAtmosphericPressure(pressure);
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		}

		if("missileContrailUP".equals(type)) {

			if(player == null || new Vec3d(player.posX - x, player.posY - y, player.posZ - z).length() > 350) return;

			float scale = data.hasKey("scale") ? data.getFloat("scale") : 1F;
			double mX = data.getDouble("moX");
			double mY = data.getDouble("moY");
			double mZ = data.getDouble("moZ");

			ParticleRocketUP fx = new ParticleRocketUP(man, world, x, y, z);
			fx.setScale(scale);
			fx.setMotion(mX, mY, mZ);
			if(data.hasKey("maxAge")) fx.setMaxAge(data.getInteger("maxAge"));
			fx.setAtmosphericPressure(pressure);
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		}

		if("smoke".equals(type)) {

			String mode = data.getString("mode");
			int count = Math.max(1, data.getInteger("count"));

			if("cloud".equals(mode)) {

				for(int i = 0; i < count; i++) {
					ParticleExSmoke fx = new ParticleExSmoke(man, world, x, y, z);
					double motionY = rand.nextGaussian() * (1 + (count / 100));
					double motionX = rand.nextGaussian() * (1 + (count / 150));
					double motionZ = rand.nextGaussian() * (1 + (count / 150));
					if(rand.nextBoolean()) motionY = Math.abs(motionY);
					fx.setMotion(motionX, motionY, motionZ);
					Minecraft.getMinecraft().effectRenderer.addEffect(fx);
				}
			}

			if("radial".equals(mode)) {

				for(int i = 0; i < count; i++) {
					ParticleExSmoke fx = new ParticleExSmoke(man, world, x, y, z);
					double motionY = rand.nextGaussian() * (1 + (count / 50));
					double motionX = rand.nextGaussian() * (1 + (count / 50));
					double motionZ = rand.nextGaussian() * (1 + (count / 50));
					fx.setMotion(motionX, motionY, motionZ);
					Minecraft.getMinecraft().effectRenderer.addEffect(fx);
				}
			}

			if("radialDigamma".equals(mode)) {

				Vec3d vec = new Vec3d(2, 0, 0);
				vec = vec.rotateYaw(rand.nextFloat() * (float)Math.PI * 2F);

				for(int i = 0; i < count; i++) {
					ParticleDigammaSmoke fx = new ParticleDigammaSmoke(man, world, x, y, z);
					fx.setMotion(vec.x, 0, vec.z);
					Minecraft.getMinecraft().effectRenderer.addEffect(fx);

					vec = vec.rotateYaw((float)Math.PI * 2F / (float)count);
				}
			}

			if("shock".equals(mode)) {

				double strength = data.getDouble("strength");

				Vec3d vec = new Vec3d(strength, 0, 0);
				vec = vec.rotateYaw(rand.nextInt(360));

				for(int i = 0; i < count; i++) {
					ParticleExSmoke fx = new ParticleExSmoke(man, world, x, y, z);
					fx.setMotion(vec.x, 0, vec.z);
					Minecraft.getMinecraft().effectRenderer.addEffect(fx);

					vec = vec.rotateYaw((float)Math.PI * 2F / (float)count);
				}
			}

			if("shockRand".equals(mode)) {

				double strength = data.getDouble("strength");

				Vec3d vec = new Vec3d(strength, 0, 0);
				vec = vec.rotateYaw(rand.nextInt(360));
				double r;

				for(int i = 0; i < count; i++) {
					r = rand.nextDouble();
					ParticleExSmoke fx = new ParticleExSmoke(man, world, x, y, z);
					fx.setMotion(vec.x * r, 0, vec.z * r);
					Minecraft.getMinecraft().effectRenderer.addEffect(fx);

					vec = vec.rotateYaw(360 / count);
				}
			}

			if("wave".equals(mode)) {

				double strength = data.getDouble("range");

				Vec3d vec = new Vec3d(strength, 0, 0);

				for(int i = 0; i < count; i++) {

					vec = vec.rotateYaw((float) Math.toRadians(rand.nextFloat() * 360F));

					ParticleExSmoke fx = new ParticleExSmoke(man, world, x + vec.x, y, z + vec.z);
					fx.maxAge = 50;
					fx.setMotion(0, 0, 0);
					Minecraft.getMinecraft().effectRenderer.addEffect(fx);

					vec = vec.rotateYaw(360 / count);
				}
			}

			if("foamSplash".equals(mode)) {

				double strength = data.getDouble("range");

				Vec3d vec = new Vec3d(strength, 0, 0);

				for(int i = 0; i < count; i++) {

					vec = vec.rotateYaw((float) Math.toRadians(rand.nextFloat() * 360F));

					ParticleFoam fx = new ParticleFoam(man, world, x + vec.x, y, z + vec.z);
					fx.maxAge = 50;
					fx.setMotion(0, 0, 0);
					Minecraft.getMinecraft().effectRenderer.addEffect(fx);

					vec = vec.rotateYaw(360 / count);
				}
			}
		}



		if("exhaust".equals(type)) {

			String mode = data.getString("mode");

			if("soyuz".equals(mode)) {

				if(player == null || new Vec3d(player.posX - x, player.posY - y, player.posZ - z).length() > 350)
					return;

				int count = Math.max(1, data.getInteger("count"));
				double width = data.getDouble("width");

				for(int i = 0; i < count; i++) {

					ParticleRocketFlame fx = new ParticleRocketFlame(man, world, x + rand.nextGaussian() * width, y, z + rand.nextGaussian() * width);
					fx.setVerticalMotion(-0.75 + rand.nextDouble() * 0.5);
					Minecraft.getMinecraft().effectRenderer.addEffect(fx);
				}
			}

			if("meteor".equals(mode)) {

				if(player == null || new Vec3d(player.posX - x, player.posY - y, player.posZ - z).length() > 350)
					return;

				int count = Math.max(1, data.getInteger("count"));
				double width = data.getDouble("width");

				for(int i = 0; i < count; i++) {

					ParticleRocketFlame fx = new ParticleRocketFlame(man, world, x + rand.nextGaussian() * width, y + rand.nextGaussian() * width, z + rand.nextGaussian() * width);
					Minecraft.getMinecraft().effectRenderer.addEffect(fx);
				}
			}
		}

		if("fireworks".equals(type)) {
			int color = data.getInteger("color");
			char c = (char)data.getInteger("char");

			ParticleLetter fx = new ParticleLetter(world, x, y, z, color, c);
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);

			for(int i = 0; i < 50; i++) {
				NativeVanillaParticles.FireworkSpark blast = new NativeVanillaParticles.FireworkSpark(world, x, y, z,
						0.4 * world.rand.nextGaussian(),
						0.4 * world.rand.nextGaussian(),
						0.4 * world.rand.nextGaussian(), Minecraft.getMinecraft().effectRenderer);
				blast.setColor(color);
				Minecraft.getMinecraft().effectRenderer.addEffect(blast);
			}
		}

		if("vanillaburst".equals(type)) {

			double motion = data.getDouble("motion");

			for(int i = 0; i < data.getInteger("count"); i++) {

				double mX = rand.nextGaussian() * motion;
				double mY = rand.nextGaussian() * motion;
				double mZ = rand.nextGaussian() * motion;

				Particle fx = null;

				if("flame".equals(data.getString("mode"))) {
					fx = new NativeVanillaParticles.Flame(world, x, y, z, mX, mY, mZ);
				}

				if("cloud".equals(data.getString("mode"))) {
					fx = new NativeVanillaParticles.Cloud(world, x, y, z, mX, mY, mZ);
				}

				if("reddust".equals(data.getString("mode"))) {
					fx = new NativeVanillaParticles.Reddust(world, x, y, z, 0.0F, 0.0F, 0.0F);
					NativeVanillaParticles.setMotion(fx, mX, mY, mZ);
				}

				if("bluedust".equals(data.getString("mode"))) {
					fx = new NativeVanillaParticles.Reddust(world, x, y, z, 0.01F, 0.01F, 1F);
				}

				if("greendust".equals(data.getString("mode"))) {
					fx = new NativeVanillaParticles.Reddust(world, x, y, z, 0.01F, 0.5F, 0.1F);
				}

				if("blockdust".equals(data.getString("mode"))) {

					Block b = Block.getBlockById(data.getInteger("block"));
					int m = data.getByte("meta");
					fx = new NativeVanillaParticles.BlockDust(world, x, y, z, mX, mY + 0.2, mZ, b, m);
					fx.setMaxAge(50 + rand.nextInt(50));
				}

				if(fx != null)
					Minecraft.getMinecraft().effectRenderer.addEffect(fx);
			}
		}

		if("vanillaExt".equals(type)) {

			double mX = data.getDouble("mX");
			double mY = data.getDouble("mY");
			double mZ = data.getDouble("mZ");

			Particle fx = null;

			if("flame".equals(data.getString("mode"))) {
				fx = new NativeVanillaParticles.Flame(world, x, y, z, mX, mY, mZ);
			}

			if("smoke".equals(data.getString("mode"))) {
				fx = new NativeVanillaParticles.Smoke(world, x, y, z, mX, mY, mZ);
			}

			if("volcano".equals(data.getString("mode"))) {
				fx = new NativeVanillaParticles.Smoke(world, x, y, z, mX, mY, mZ, 100F);
				fx.setMaxAge(200 + rand.nextInt(50));
				NativeVanillaParticles.setCollision(fx, false);
				double upward = 2.5 + rand.nextDouble();
				NativeVanillaParticles.setMotion(fx, rand.nextGaussian() * 0.2, upward, rand.nextGaussian() * 0.2);
			}

			if("cloud".equals(data.getString("mode"))) {
				fx = new NativeVanillaParticles.Cloud(world, x, y, z, mX, mY, mZ);

				if(data.hasKey("r")) {
					float rng = rand.nextFloat() * 0.1F;
					fx.setRBGColorF(data.getFloat("r") + rng, data.getFloat("g") + rng, data.getFloat("b") + rng);
					// Use native cloud size rather than reflecting into its private visual scale.
					NativeVanillaParticles.setMotion(fx, 0, 0, 0);
				}
			}

			if("reddust".equals(data.getString("mode"))) {
				fx = new NativeVanillaParticles.Reddust(world, x, y, z, (float)mX, (float)mY, (float)mZ);
			}

			if("bluedust".equals(data.getString("mode"))) {
				fx = new NativeVanillaParticles.Reddust(world, x, y, z, 0.01F, 0.01F, 1F);
			}

			if("greendust".equals(data.getString("mode"))) {
				fx = new NativeVanillaParticles.Reddust(world, x, y, z, 0.01F, 0.5F, 0.1F);
			}

			if("fireworks".equals(data.getString("mode"))) {
				fx = new NativeVanillaParticles.FireworkSpark(world, x, y, z, 0, 0, 0, Minecraft.getMinecraft().effectRenderer);
			}

			if("largeexplode".equals(data.getString("mode"))) {


				fx = new NativeVanillaParticles.LargeExplode(man, world, x, y, z, data.getFloat("size"), 0.0F, 0.0F);
				float r = 1.0F - rand.nextFloat() * 0.2F;
				fx.setRBGColorF(1F * r, 0.9F * r, 0.5F * r);

				for(int i = 0; i < data.getByte("count"); i++) {
					NativeVanillaParticles.Explode sec = new NativeVanillaParticles.Explode(world, x, y, z, 0.0F, 0.0F, 0.0F);
					float r2 = 1.0F - rand.nextFloat() * 0.5F;
					sec.setRBGColorF(0.5F * r2, 0.5F * r2, 0.5F * r2);
					sec.multipleParticleScaleBy(i + 1);
					Minecraft.getMinecraft().effectRenderer.addEffect(sec);
				}
			}

			if("townaura".equals(data.getString("mode"))) {
				fx = new NativeVanillaParticles.Aura(world, x, y, z, 0, 0, 0);
				float color = 0.5F + rand.nextFloat() * 0.5F;
				fx.setRBGColorF(0.8F * color, 0.9F * color, 1.0F * color);
				NativeVanillaParticles.setMotion(fx, mX, mY, mZ);
			}

			if("blockdust".equals(data.getString("mode"))) {

				Block b = Block.getBlockById(data.getInteger("block"));
				fx = new NativeVanillaParticles.BlockDust(world, x, y, z, mX, mY + 0.2, mZ, b, 0);
				fx.setMaxAge(10 + rand.nextInt(20));
			}

			if("colordust".equals(data.getString("mode"))) {

				Block b = Blocks.WOOL;
				fx = new NativeVanillaParticles.BlockDust(world, x, y, z, mX, mY + 0.2, mZ, b, 0);
				fx.setRBGColorF(data.getFloat("r"), data.getFloat("g"), data.getFloat("b"));
				fx.setMaxAge(10 + rand.nextInt(20));
			}

			if(fx != null) {

				if(data.getBoolean("noclip")) {
					NativeVanillaParticles.setCollision(fx, false);
				}

				if(data.getInteger("overrideAge") > 0) {
					fx.setMaxAge(data.getInteger("overrideAge"));
				}

				Minecraft.getMinecraft().effectRenderer.addEffect(fx);
			}
		}

		if("vanilla".equals(type)) {

			double mX = data.getDouble("mX");
			double mY = data.getDouble("mY");
			double mZ = data.getDouble("mZ");
			NativeVanillaParticles.spawnNamed(world, data.getString("mode"), x, y, z, mX, mY, mZ);
		}

		if("jetpack".equals(type)) {

			if(particleSetting == 2)
				return;

			Entity ent = world.getEntityByID(data.getInteger("player"));

			if(ent instanceof EntityPlayer) {

				EntityPlayer p = (EntityPlayer)ent;

				Vec3d vec = new Vec3d(0, 0, -0.25);
				Vec3d offset = new Vec3d(0.125, 0, 0);
				float angle = (float) -Math.toRadians(p.rotationYawHead - (p.rotationYawHead - p.renderYawOffset));

				vec = vec.rotateYaw(angle);
				offset = offset.rotateYaw(angle);

				double ix = p.posX + vec.x;
				double iy = p.posY + p.getEyeHeight() - 1;
				double iz = p.posZ + vec.z;
				double ox = offset.x;
				double oz = offset.z;

				double moX = 0;
				double moY = 0;
				double moZ = 0;

				int mode = data.getInteger("mode");

				if(mode == 0) {
					moY -= 0.2;
				}

				if(mode == 1) {
					Vec3d look = p.getLookVec();

					moX -= look.x * 0.1D;
					moY -= look.y * 0.1D;
					moZ -= look.z * 0.1D;
				}

				if(particleSetting == 0) {
					Vec3d pos = new Vec3d(ix, iy, iz);
					Vec3d thrust = new Vec3d(moX, moY, moZ);
					thrust = thrust.normalize();
					Vec3d target = pos.add(thrust.x * 10, thrust.y * 10, thrust.z * 10);
					RayTraceResult mop = player.world.rayTraceBlocks(pos, target, false, false, true);

					if(mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK && mop.sideHit == EnumFacing.UP) {

						IBlockState hitState = world.getBlockState(mop.getBlockPos());
						Block b = hitState.getBlock();
						int meta = b.getMetaFromState(hitState);

						Vec3d delta = new Vec3d(ix - mop.hitVec.x, iy - mop.hitVec.y, iz - mop.hitVec.z);
						Vec3d vel = new Vec3d(0.75 - delta.length() * 0.075, 0, 0);

						for(int i = 0; i < (10 - delta.length()); i++) {
							vel = vel.rotateYaw(world.rand.nextFloat() * (float)Math.PI * 2F);
							Minecraft.getMinecraft().effectRenderer.addEffect(new NativeVanillaParticles.BlockDust(world, mop.hitVec.x, mop.hitVec.y + 0.1, mop.hitVec.z, vel.x, 0.1, vel.z, b, meta));
						}
					}
				}

				double mX2 = BobMathUtil.safeClamp(p.motionX + moX * 2, -5, 5);
				double mY2 = BobMathUtil.safeClamp(p.motionY + moY * 2, -5, 5);
				double mZ2 = BobMathUtil.safeClamp(p.motionZ + moZ * 2, -5, 5);
				double mX3 = BobMathUtil.safeClamp(p.motionX + moX * 2, -10, 10);
				double mY3 = BobMathUtil.safeClamp(p.motionY + moY * 2, -10, 10);
				double mZ3 = BobMathUtil.safeClamp(p.motionZ + moZ * 2, -10, 10);

				Minecraft.getMinecraft().effectRenderer.addEffect(new NativeVanillaParticles.Flame(world, ix + ox, iy, iz + oz, mX2, mY2, mZ2));
				Minecraft.getMinecraft().effectRenderer.addEffect(new NativeVanillaParticles.Flame(world, ix - ox, iy, iz - oz, mX2, mY2, mZ2));

				if(particleSetting == 0) {
					Minecraft.getMinecraft().effectRenderer.addEffect(new NativeVanillaParticles.Smoke(world, ix + ox, iy, iz + oz, mX3, mY3, mZ3));
					Minecraft.getMinecraft().effectRenderer.addEffect(new NativeVanillaParticles.Smoke(world, ix - ox, iy, iz - oz, mX3, mY3, mZ3));
				}
			}
		}

		if("bnuuy".equals(type)) {

			if(particleSetting == 2)
				return;

			Entity ent = world.getEntityByID(data.getInteger("player"));

			if(ent instanceof EntityPlayer) {

				EntityPlayer p = (EntityPlayer)ent;

				Vec3d vec = new Vec3d(0, 0, -0.6);
				Vec3d offset = new Vec3d(0.275, 0, 0);
				float angle = (float) -Math.toRadians(p.rotationYawHead - (p.rotationYawHead - p.renderYawOffset));

				vec = vec.rotateYaw(angle);
				offset = offset.rotateYaw(angle);

				double ix = p.posX + vec.x;
				double iy = p.posY + p.getEyeHeight() - 1 + 0.4;
				double iz = p.posZ + vec.z;
				double ox = offset.x;
				double oz = offset.z;

				if(player.isSneaking()) {
					iy += 0.25;
				}

				vec = vec.normalize();
				double mult = 0.025D;
				double mX = vec.x * mult;
				double mZ = vec.z * mult;

				//Minecraft.getMinecraft().effectRenderer.addEffect(new NativeVanillaParticles.Flame(world, ix + ox, iy, iz + oz, 0, 0, 0));
				//Minecraft.getMinecraft().effectRenderer.addEffect(new NativeVanillaParticles.Flame(world, ix - ox, iy, iz - oz, 0, 0, 0));

				for(int i = 0; i < 2; i++) {
					NativeVanillaParticles.Smoke fx = new NativeVanillaParticles.Smoke(world, ix + ox * (i == 0 ? -1 : 1), iy, iz + oz * (i == 0 ? -1 : 1), mX, 0, mZ, 0.5F);
					Minecraft.getMinecraft().effectRenderer.addEffect(fx);
				}
			}
		}

		if("jetpack_bj".equals(type)) {

			if(particleSetting == 2)
				return;

			Entity ent = world.getEntityByID(data.getInteger("player"));

			if(ent instanceof EntityPlayer) {

				EntityPlayer p = (EntityPlayer)ent;

				Vec3d vec = new Vec3d(0, 0, -0.3125);
				Vec3d offset = new Vec3d(0.125, 0, 0);
				float angle = (float) -Math.toRadians(p.rotationYawHead - (p.rotationYawHead - p.renderYawOffset));

				vec = vec.rotateYaw(angle);
				offset = offset.rotateYaw(angle);

				double ix = p.posX + vec.x;
				double iy = p.posY + p.getEyeHeight() - 0.9375;
				double iz = p.posZ + vec.z;
				double ox = offset.x;
				double oz = offset.z;

				if(particleSetting == 0) {
					Vec3d pos = new Vec3d(ix, iy, iz);
					Vec3d thrust = new Vec3d(0, -1, 0);
					Vec3d target = pos.add(thrust.x * 10, thrust.y * 10, thrust.z * 10);
					RayTraceResult mop = player.world.rayTraceBlocks(pos, target, false, false, true);

					if(mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK && mop.sideHit == EnumFacing.UP) {

						IBlockState hitState = world.getBlockState(mop.getBlockPos());
						Block b = hitState.getBlock();
						int meta = b.getMetaFromState(hitState);

						Vec3d delta = new Vec3d(ix - mop.hitVec.x, iy - mop.hitVec.y, iz - mop.hitVec.z);
						Vec3d vel = new Vec3d(0.75 - delta.length() * 0.075, 0, 0);

						for(int i = 0; i < (10 - delta.length()); i++) {
							vel = vel.rotateYaw(world.rand.nextFloat() * (float)Math.PI * 2F);
							Minecraft.getMinecraft().effectRenderer.addEffect(new NativeVanillaParticles.BlockDust(world, mop.hitVec.x, mop.hitVec.y + 0.1, mop.hitVec.z, vel.x, 0.1, vel.z, b, meta));
						}
					}
				}

				NativeVanillaParticles.Reddust dust1 = new NativeVanillaParticles.Reddust(world, ix + ox, iy, iz + oz, 0.8F, 0.5F, 1.0F);
				NativeVanillaParticles.Reddust dust2 = new NativeVanillaParticles.Reddust(world, ix - ox, iy, iz - oz, 0.8F, 0.5F, 1.0F);
				NativeVanillaParticles.setMotion(dust1, p.motionX, p.motionY, p.motionZ);
				NativeVanillaParticles.setMotion(dust2, p.motionX, p.motionY, p.motionZ);
				Minecraft.getMinecraft().effectRenderer.addEffect(dust1);
				Minecraft.getMinecraft().effectRenderer.addEffect(dust2);
			}
		}

		if("jetpack_dns".equals(type)) {

			if(particleSetting == 2)
				return;

			Entity ent = world.getEntityByID(data.getInteger("player"));

			if(ent instanceof EntityPlayer) {

				EntityPlayer p = (EntityPlayer)ent;

				Vec3d offset = new Vec3d(0.125, 0, 0);
				float angle = (float) -Math.toRadians(p.rotationYawHead - (p.rotationYawHead - p.renderYawOffset));

				offset = offset.rotateYaw(angle);

				double ix = p.posX;
				double iy = p.posY - p.getYOffset() - 0.5D;
				double iz = p.posZ;
				double ox = offset.x;
				double oz = offset.z;

				if(particleSetting == 0) {
					Vec3d pos = new Vec3d(ix, iy, iz);
					Vec3d thrust = new Vec3d(0, -1, 0);
					Vec3d target = pos.add(thrust.x * 10, thrust.y * 10, thrust.z * 10);
					RayTraceResult mop = player.world.rayTraceBlocks(pos, target, false, false, true);

					if(mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK && mop.sideHit == EnumFacing.UP) {

						IBlockState hitState = world.getBlockState(mop.getBlockPos());
						Block b = hitState.getBlock();
						int meta = b.getMetaFromState(hitState);

						Vec3d delta = new Vec3d(ix - mop.hitVec.x, iy - mop.hitVec.y, iz - mop.hitVec.z);
						Vec3d vel = new Vec3d(0.75 - delta.length() * 0.075, 0, 0);

						for(int i = 0; i < (10 - delta.length()); i++) {
							vel = vel.rotateYaw(world.rand.nextFloat() * (float)Math.PI * 2F);
							Minecraft.getMinecraft().effectRenderer.addEffect(new NativeVanillaParticles.BlockDust(world, mop.hitVec.x, mop.hitVec.y + 0.1, mop.hitVec.z, vel.x, 0.1, vel.z, b, meta));
						}
					}
				}

				NativeVanillaParticles.Reddust dust1 = new NativeVanillaParticles.Reddust(world, ix + ox, iy, iz + oz, 0.01F, 1.0F, 1.0F);
				NativeVanillaParticles.Reddust dust2 = new NativeVanillaParticles.Reddust(world, ix - ox, iy, iz - oz, 0.01F, 1.0F, 1.0F);
				NativeVanillaParticles.setMotion(dust1, p.motionX, p.motionY, p.motionZ);
				NativeVanillaParticles.setMotion(dust2, p.motionX, p.motionY, p.motionZ);
				Minecraft.getMinecraft().effectRenderer.addEffect(dust1);
				Minecraft.getMinecraft().effectRenderer.addEffect(dust2);
			}
		}

		if("muke".equals(type)) {

			ParticleMukeWave wave = new ParticleMukeWave(man, world, x, y, z);
			ParticleMukeFlash flash = new ParticleMukeFlash(man, world, x, y, z, data.getBoolean("balefire"));

			Minecraft.getMinecraft().effectRenderer.addEffect(wave);
			Minecraft.getMinecraft().effectRenderer.addEffect(flash);

			//single swing: 			HT 15,  MHT 15
			//double swing: 			HT 60,  MHT 50

			player.hurtTime = 15;
			player.maxHurtTime = 15;
			player.attackedAtYaw = 0F;
		}

		if("tinytot".equals(type)) {

			ParticleMukeWave wave = new ParticleMukeWave(man, world, x, y, z);
			Minecraft.getMinecraft().effectRenderer.addEffect(wave);

			for(double d = 0.0D; d <= 1.6D; d += 0.1) {
				ParticleMukeCloud cloud = new ParticleMukeCloud(man, world, x, y, z, rand.nextGaussian() * 0.05, d + rand.nextGaussian() * 0.02, rand.nextGaussian() * 0.05);
				Minecraft.getMinecraft().effectRenderer.addEffect(cloud);
			}
			for(int i = 0; i < 50; i++) {
				ParticleMukeCloud cloud = new ParticleMukeCloud(man, world, x, y + 0.5, z, rand.nextGaussian() * 0.5, rand.nextInt(5) == 0 ? 0.02 : 0, rand.nextGaussian() * 0.5);
				Minecraft.getMinecraft().effectRenderer.addEffect(cloud);
			}
			for(int i = 0; i < 15; i++) {
				double ix = rand.nextGaussian() * 0.2;
				double iz = rand.nextGaussian() * 0.2;

				if(ix * ix + iz * iz > 0.75) {
					ix *= 0.5;
					iz *= 0.5;
				}

				double iy = 1.6 + (rand.nextDouble() * 2 - 1) * (0.75 - (ix * ix + iz * iz)) * 0.5;

				ParticleMukeCloud cloud = new ParticleMukeCloud(man, world, x, y, z, ix, iy + rand.nextGaussian() * 0.02, iz);
				Minecraft.getMinecraft().effectRenderer.addEffect(cloud);
			}
			player.hurtTime = 15;
			player.maxHurtTime = 15;
			player.attackedAtYaw = 0F;
		}

		if("ufo".equals(type)) {
			double motion = data.getDouble("motion");
			ParticleMukeCloud cloud = new ParticleMukeCloud(man, world, x, y, z, rand.nextGaussian() * motion, 0, rand.nextGaussian() * motion);
			Minecraft.getMinecraft().effectRenderer.addEffect(cloud);
		}

		if("bf".equals(type)) {
			ParticleMukeCloud cloud = new ParticleMukeCloudBF(man, world, x, y, z, 0, 0, 0);
			Minecraft.getMinecraft().effectRenderer.addEffect(cloud);
		}

		if("haze".equals(type)) {

			ParticleHaze fog = new ParticleHaze(man, world, x, y, z);
			Minecraft.getMinecraft().effectRenderer.addEffect(fog);
		}

		if("plasmablast".equals(type)) {

			ParticlePlasmaBlast cloud = new ParticlePlasmaBlast(man, world, x, y, z, data.getFloat("r"), data.getFloat("g"), data.getFloat("b"), data.getFloat("pitch"), data.getFloat("yaw"));
			cloud.setScale(data.getFloat("scale"));
			Minecraft.getMinecraft().effectRenderer.addEffect(cloud);
		}

		if("justTilt".equals(type)) {

			player.hurtTime = player.maxHurtTime = data.getInteger("time");
			player.attackedAtYaw = 0F;
		}

		if("properJolt".equals(type)) {

			player.hurtTime = data.getInteger("time");
			player.maxHurtTime = data.getInteger("maxTime");
			player.attackedAtYaw = 0F;
		}

		if("sweat".equals(type)) {

			Entity e = world.getEntityByID(data.getInteger("entity"));
			Block b = Block.getBlockById(data.getInteger("block"));
			int meta = data.getInteger("meta");

			if(e instanceof EntityLivingBase) {

				for(int i = 0; i < data.getInteger("count"); i++) {

					double ix = e.getEntityBoundingBox().minX - 0.2 + (e.getEntityBoundingBox().maxX - e.getEntityBoundingBox().minX + 0.4) * rand.nextDouble();
					double iy = e.getEntityBoundingBox().minY + (e.getEntityBoundingBox().maxY - e.getEntityBoundingBox().minY + 0.2) * rand.nextDouble();
					double iz = e.getEntityBoundingBox().minZ - 0.2 + (e.getEntityBoundingBox().maxZ - e.getEntityBoundingBox().minZ + 0.4) * rand.nextDouble();


					Particle fx = new NativeVanillaParticles.BlockDust(world, ix, iy, iz, 0, 0, 0, b, meta);
					fx.setMaxAge(150 + rand.nextInt(50));

					Minecraft.getMinecraft().effectRenderer.addEffect(fx);
				}
			}
		}

		if("vomit".equals(type)) {

			Entity e = world.getEntityByID(data.getInteger("entity"));
			int count = data.getInteger("count") / (particleSetting + 1);

			if(e instanceof EntityLivingBase) {

				double ix = e.posX;
				double iy = e.posY - e.getYOffset() + e.getEyeHeight() + (e instanceof EntityPlayer ? 1 : 0);
				double iz = e.posZ;

				Vec3d vec = e.getLookVec();

				for(int i = 0; i < count; i++) {

					if("normal".equals(data.getString("mode"))) {
						Particle fx = new NativeVanillaParticles.BlockDust(world, ix, iy, iz, (vec.x + rand.nextGaussian() * 0.2) * 0.2, (vec.y + rand.nextGaussian() * 0.2) * 0.2, (vec.z + rand.nextGaussian() * 0.2) * 0.2, Blocks.STAINED_HARDENED_CLAY, (rand.nextBoolean() ? 5 : 13));
						fx.setMaxAge(150 + rand.nextInt(50));
						Minecraft.getMinecraft().effectRenderer.addEffect(fx);
					}

					if("blood".equals(data.getString("mode"))) {
						Particle fx = new NativeVanillaParticles.BlockDust(world, ix, iy, iz, (vec.x + rand.nextGaussian() * 0.2) * 0.2, (vec.y + rand.nextGaussian() * 0.2) * 0.2, (vec.z + rand.nextGaussian() * 0.2) * 0.2, Blocks.REDSTONE_BLOCK, 0);
						fx.setMaxAge(150 + rand.nextInt(50));
						Minecraft.getMinecraft().effectRenderer.addEffect(fx);
					}

					if("smoke".equals(data.getString("mode"))) {
						Particle fx = new NativeVanillaParticles.Smoke(world, ix, iy, iz, (vec.x + rand.nextGaussian() * 0.1) * 0.05, (vec.y + rand.nextGaussian() * 0.1) * 0.05, (vec.z + rand.nextGaussian() * 0.1) * 0.05, 0.2F);
						fx.setMaxAge(10 + rand.nextInt(10));
						Minecraft.getMinecraft().effectRenderer.addEffect(fx);
					}
				}
			}
		}

		if("radiation".equals(type)) {

			for(int i = 0; i < data.getInteger("count"); i++) {

				NativeVanillaParticles.Aura flash = new NativeVanillaParticles.Aura(world,
						player.posX + rand.nextGaussian() * 4,
						player.posY + rand.nextGaussian() * 2,
						player.posZ + rand.nextGaussian() * 4,
						0, 0, 0);

				flash.setRBGColorF(0F, 0.75F, 1F);
				NativeVanillaParticles.setMotion(flash, rand.nextGaussian(), rand.nextGaussian(), rand.nextGaussian());
				Minecraft.getMinecraft().effectRenderer.addEffect(flash);
			}
		}

		if("schrabfog".equals(type)) {

			NativeVanillaParticles.Aura flash = new NativeVanillaParticles.Aura(world, x, y, z, 0, 0, 0);
			flash.setRBGColorF(0F, 1F, 1F);
			Minecraft.getMinecraft().effectRenderer.addEffect(flash);
		}

		if("hadron".equals(type)) {

			Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleHadron(man, world, x, y, z));
		}

		if("rift".equals(type)) {

			Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleRift(man, world, x, y, z));
		}

		if("rbmkflame".equals(type)) {
			int maxAge = data.getInteger("maxAge");
			Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleRBMKFlame(man, world, x, y, z, maxAge));
		}

		if("rbmksteam".equals(type)) {
			Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleRBMKSteam(man, world, x, y, z));
		}

		if("rbmkmush".equals(type)) {
			float scale = data.getFloat("scale");
			Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleRBMKMush(man, world, x, y, z, scale));
		}

		if("tower".equals(type) || (("splash").equals(type) && pressure < 0.08)) {
			if(particleSetting == 0 || (particleSetting == 1 && rand.nextBoolean())) {
				ParticleCoolingTower fx = new ParticleCoolingTower(man, world, x, y, z);
				fx.setLift(data.getFloat("lift"));
				fx.setBaseScale(data.getFloat("base"));
				fx.setMaxScale(data.getFloat("max"));
				if(data.hasKey("noWind") || pressure < 0.08) fx.noWind();
				if(data.hasKey("strafe")) fx.setStrafe(data.getFloat("strafe"));
				if(data.hasKey("alpha")) fx.alphaMod(data.getFloat("alpha"));

				int life = data.getInteger("life");

				double motionX = data.getDouble("mX");
				double motionY = data.getDouble("mY");
				double motionZ = data.getDouble("mZ");

				if(inOrbit) fx.setLift(0);
				if(pressure < 0.08) {
					double factor = (0.08 - pressure) * 4;
					motionX += (rand.nextDouble() - 0.5) * factor;
					motionY += (rand.nextDouble() - 0.5) * factor;
					motionZ += (rand.nextDouble() - 0.5) * factor;
					life /= 8;
				}
				fx.setMotion(motionX, motionY, motionZ);

				fx.setLife(life / (particleSetting + 1));

				if(data.hasKey("color")) {
					Color color = new Color(data.getInteger("color"));
					fx.setRBGColorF(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
				}

				Minecraft.getMinecraft().effectRenderer.addEffect(fx);
			}
		}

		if("splash".equals(type) && pressure >= 0.08) {
			if(particleSetting == 0 || (particleSetting == 1 && rand.nextBoolean())) {
				com.hbm.particle.ParticleSplash fx = new com.hbm.particle.ParticleSplash(man, world, x, y, z);

				if(data.hasKey("color")) {
					Color color = new Color(data.getInteger("color"));
					float f = 1F - rand.nextFloat() * 0.2F;
					fx.setRBGColorF(color.getRed() / 255F * f, color.getGreen() / 255F * f, color.getBlue() / 255F * f);
				}

				Minecraft.getMinecraft().effectRenderer.addEffect(fx);
			}
		}

		if("fluidfill".equals(type)) {
			double mX = data.getDouble("mX");
			double mY = data.getDouble("mY");
			double mZ = data.getDouble("mZ");

			Particle fx = new NativeVanillaParticles.Crit(world, x, y, z, mX, mY, mZ);
			fx.nextTextureIndexX();

			if(data.hasKey("color")) {
				Color color = new Color(data.getInteger("color"));
				fx.setRBGColorF(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
			}

			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		}

		if("deadleaf".equals(type)) {
			if(particleSetting == 0 || (particleSetting == 1 && rand.nextBoolean()))
				Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleDeadLeaf(man, world, x, y, z));
		}
		if("duoewe".equals(type)) {
			if(particleSetting == 0 || (particleSetting == 1 && rand.nextBoolean()))
				Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleDuoDec(man, world, x, y, z));
		}
		if("duodec".equals(type)) {
			if(particleSetting == 0 || (particleSetting == 1 && rand.nextBoolean()))
				Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleDuoDec2(man, world, x, y, z));
		}
		if("duststorm".equals(type)) {
			double mX = data.getDouble("mX");
			double mY = data.getDouble("mY");
			double mZ = data.getDouble("mZ");
			float scale = data.getFloat("scale");
			ParticleDust particle = new ParticleDust(world, x, y, z, mX, mY, mZ, scale);
			Minecraft.getMinecraft().effectRenderer.addEffect(particle);
		}
		if("flare".equals(type)) {
			double mX = data.getDouble("mX");
			double mY = data.getDouble("mY");
			double mZ = data.getDouble("mZ");
			float scale = data.getFloat("scale");
			ParticleGlow particle = new ParticleGlow(world, x, y, z, mX, mY, mZ, scale);
			Minecraft.getMinecraft().effectRenderer.addEffect(particle);
		}

		if("tau".equals(type)) {

			for(int i = 0; i < data.getByte("count"); i++)
				Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleSpark(world, x, y, z, rand.nextGaussian() * 0.05, 0.05, rand.nextGaussian() * 0.05));
			Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleHadron(man, world, x, y, z));
		}

		if("vanish".equals(type)) {
			int ent = data.getInteger("ent");
			this.vanish(ent);
		}

		if("giblets".equals(type)) {
			int ent = data.getInteger("ent");
			int gibType = data.getInteger("gibType");
			this.vanish(ent);
			Entity e = world.getEntityByID(ent);

			if(e == null)
				return;

			float width = e.width;
			float height = e.height;
			int gW = (int)(width / 0.25F);
			int gH = (int)(height / 0.25F);

			int count = (int) (gW * 1.5 * gH);

			if(data.hasKey("cDiv"))
				count = (int) Math.ceil(count / (double)data.getInteger("cDiv"));

			boolean blowMeIntoTheGodDamnStratosphere = rand.nextInt(15) == 0;
			double mult = 1D;

			if(blowMeIntoTheGodDamnStratosphere)
				mult *= 10;

			for(int i = 0; i < count; i++) {
				Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleGiblet(man, world, x, y, z, rand.nextGaussian() * 0.25 * mult, rand.nextDouble() * mult, rand.nextGaussian() * 0.25 * mult, gibType));
			}
		}

		if("amat".equals(type)) {
			Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleAmatFlash(world, x, y, z, data.getFloat("scale")));
		}

		if("debug".equals(type)) {
			String t = data.getString("text");
			int color = data.getInteger("color");
			float scale = data.getFloat("scale");
			ParticleText text = new ParticleText(world, x, y, z, color, t);
			text.multipleParticleScaleBy(scale);
			Minecraft.getMinecraft().effectRenderer.addEffect(text);
		}

		if("debugline".equals(type)) {
			double mX = data.getDouble("mX");
			double mY = data.getDouble("mY");
			double mZ = data.getDouble("mZ");
			int color = data.getInteger("color");
			ParticleDebugLine text = new ParticleDebugLine(world, x, y, z, mX, mY, mZ, color);
			Minecraft.getMinecraft().effectRenderer.addEffect(text);
		}

		if("debugdrone".equals(type)) {
			Item held = com.hbm.util.NativeHeldItem.getMainHandNullable(player) == null ? null : com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem();

			if(held == ModItems.drone ||
					held == Item.getItemFromBlock(ModBlocks.drone_crate_provider) ||
					held == Item.getItemFromBlock(ModBlocks.drone_crate_requester) ||
					held == Item.getItemFromBlock(ModBlocks.drone_dock) ||
					held == Item.getItemFromBlock(ModBlocks.drone_waypoint_request) ||
					held == Item.getItemFromBlock(ModBlocks.drone_waypoint) ||
					held == ModItems.drone_linker) {
				double mX = data.getDouble("mX");
				double mY = data.getDouble("mY");
				double mZ = data.getDouble("mZ");
				int color = data.getInteger("color");
				ParticleDebugLine text = new ParticleDebugLine(world, x, y, z, mX, mY, mZ, color);
				Minecraft.getMinecraft().effectRenderer.addEffect(text);
			}
		}

		if("network".equals(type)) {
			ParticleDebug debug = null;
			double mX = data.getDouble("mX");
			double mY = data.getDouble("mY");
			double mZ = data.getDouble("mZ");

			if("power".equals(data.getString("mode"))) {
				debug = new ParticleDebug(man, world, x, y, z, mX, mY, mZ);
			}
			if("fluid".equals(data.getString("mode"))) {
				int color = data.getInteger("color");
				debug = new ParticleDebug(man, world, x, y, z, mX, mY, mZ, color);
			}
			Minecraft.getMinecraft().effectRenderer.addEffect(debug);
		}

		if("gasfire".equals(type)) {
			double mX = data.getDouble("mX");
			double mY = data.getDouble("mY");
			double mZ = data.getDouble("mZ");
			float scale = data.getFloat("scale");
			ParticleGasFlame text = new ParticleGasFlame(world, x, y, z, mX, mY, mZ, scale > 0 ? scale : 6.5F);
			Minecraft.getMinecraft().effectRenderer.addEffect(text);
		}

		if("marker".equals(type)) {
			int color = data.getInteger("color");
			String label = data.getString("label");
			int expires = data.getInteger("expires");
			double dist = data.getDouble("dist");

			RenderOverhead.queuedMarkers.put(new BlockPos(x, y, z),  new Marker(color).setDist(dist).setExpire(expires > 0 ? System.currentTimeMillis() + expires : 0).withLabel(label.isEmpty() ? null : label));
		}

		if("casing".equals(type)) {
			CasingEjector ejector = CasingEjector.fromId(data.getInteger("ej"));
			if(ejector == null) return;
			SpentCasing casingConfig = SpentCasing.fromName((data.getString("name")));
			if(casingConfig == null) return;

			for(int i = 0; i < ejector.getAmount(); i++) {
				ejector.spawnCasing(man, casingConfig, world, x, y, z, data.getFloat("pitch"), data.getFloat("yaw"), data.getBoolean("crouched"));
			}

		}
		if("radSmoke".equals(type)) {
			ParticleRadiationFog contrail = new ParticleRadiationFog(man, world, x, y, z);
			Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
			//hopefully......
		}

		if("foundry".equals(type)) {
			int color = data.getInteger("color");
			byte dir = data.getByte("dir");
			float length = data.getFloat("len");
			float base = data.getFloat("base");
			float offset = data.getFloat("off");

			ParticleFoundry sploosh = new ParticleFoundry(man, world, x, y, z, color, dir, length, base, offset);
			Minecraft.getMinecraft().effectRenderer.addEffect(sploosh);
		}

		if("frozen".equals(type)) {
			player.motionX = 0;
			player.motionZ = 0;
			player.motionY = Math.min(player.motionY, 0);
			player.moveForward = 0;
			player.moveStrafing = 0;
		}
	}

	private static HashMap<Integer, Long> vanished = new HashMap();
	public static void vanish(int ent) { vanished.put(ent, System.currentTimeMillis() + 2000); }
	public static void vanish(int ent, int duration) { vanished.put(ent, System.currentTimeMillis() + duration); }

	@Override
	public boolean isVanished(Entity e) {
		if(e == null) return false;
		if(!this.vanished.containsKey(e.getEntityId())) return false;
		return this.vanished.get(e.getEntityId()) > System.currentTimeMillis();
	}

	@Override
	public AudioWrapper getLoopedSound(String sound, float x, float y, float z, float volume, float range, float pitch) {

		AudioWrapperClient audio = new AudioWrapperClient(new ResourceLocation(sound));
		audio.updatePosition(x, y, z);
		audio.updateVolume(volume);
		audio.updateRange(range);
		return audio;
	}

	@Override
	public AudioWrapper getLoopedSound(String sound, float x, float y, float z, float volume, float range, float pitch, int keepAlive) {
		AudioWrapper audio = getLoopedSound(sound, x, y, z, volume, range, pitch);
		audio.setKeepAlive(keepAlive);
		return audio;
	}

	@Override
	public AudioWrapper getLoopedSound(String sound, Entity entity, float volume, float range, float pitch, int keepAlive) {
		AudioWrapperClient audio = new AudioWrapperClient(new ResourceLocation(sound), entity);
		audio.updateVolume(volume);
		audio.updateRange(range);
		audio.setKeepAlive(keepAlive);
		return audio;
	}

	@Override
	public void playSound(String sound, Object data) { }

	@Override
	public void displayTooltip(String msg, int time, int id) {

		if(id != 0)
			this.theInfoSystem.push(new InfoEntry(msg, time), id);
		else
			this.theInfoSystem.push(new InfoEntry(msg, time));
	}

	@Override
	public boolean getIsKeyPressed(EnumKeybind key) {

		switch(key){
		case JETPACK:			return Minecraft.getMinecraft().gameSettings.keyBindJump.isKeyDown();
		case TOGGLE_JETPACK:	return HbmKeybinds.jetpackKey.isKeyDown();
		case TOGGLE_MAGNET:		return HbmKeybinds.magnetKey.isKeyDown();
		case TOGGLE_HEAD:		return HbmKeybinds.hudKey.isKeyDown();
		case RELOAD:			return HbmKeybinds.reloadKey.isKeyDown();
		case DASH:				return HbmKeybinds.dashKey.isKeyDown();
		case TRAIN:				return HbmKeybinds.trainKey.isKeyDown();
		case CRANE_UP:			return HbmKeybinds.craneUpKey.isKeyDown();
		case CRANE_DOWN:		return HbmKeybinds.craneDownKey.isKeyDown();
		case CRANE_LEFT:		return HbmKeybinds.craneLeftKey.isKeyDown();
		case CRANE_RIGHT:		return HbmKeybinds.craneRightKey.isKeyDown();
		case CRANE_LOAD:		return HbmKeybinds.craneLoadKey.isKeyDown();
		case ABILITY_CYCLE:		return HbmKeybinds.abilityCycle.isKeyDown();
		case ABILITY_ALT:		return HbmKeybinds.abilityAlt.isKeyDown();
		case TOOL_ALT:			return HbmKeybinds.copyToolAlt.isKeyDown();
		case TOOL_CTRL:			return HbmKeybinds.copyToolCtrl.isKeyDown();
		case GUN_PRIMARY:		return HbmKeybinds.gunPrimaryKey.isKeyDown();
		case GUN_SECONDARY:		return HbmKeybinds.gunSecondaryKey.isKeyDown();
		case GUN_TERTIARY:		return HbmKeybinds.gunTertiaryKey.isKeyDown();
		}

		return false;
	}

	@Override
	public EntityPlayer me() {
		return Minecraft.getMinecraft().player;
	}

	@Override
	public void openLink(String url) {
		try {
			Desktop.getDesktop().browse(new URI(url));
		} catch (Exception e) { }
	}

	@Override
	public List<ItemStack> getSubItems(ItemStack stack) {

		net.minecraft.util.NonNullList<ItemStack> list = net.minecraft.util.NonNullList.create();
		stack.getItem().getSubItems(stack.getItem().getCreativeTab(), list);
		for(ItemStack sta : list) {
			sta.setCount(stack.getCount());
		}
		return list;
	}

	@Override
	public float getImpactDust(World world) {
		return ImpactWorldHandler.getDustForClient(world);
	}

	@Override
	public float getImpactFire(World world) {
		return ImpactWorldHandler.getFireForClient(world);
	}

	@Override
	public boolean getImpact(World world) {
		return ImpactWorldHandler.getImpactForClient(world);
	}

	@Override
	public void playSoundClient(double x, double y, double z, String sound, float volume, float pitch) {
		Minecraft.getMinecraft().getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation(sound), net.minecraft.util.SoundCategory.MASTER, pitch, volume, false, 0, net.minecraft.client.audio.ISound.AttenuationType.LINEAR, (float) x, (float) y, (float) z));
	}

	@Override
	public String getLanguageCode() {
		Language lang = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage();
		return lang.getLanguageCode();
	}

	@Override
	public int getStackColor(ItemStack stack, boolean amplify) {
		if(stack == null) return 0x000000;
		if(stack.getItem() instanceof ItemBlock) {
			Block b = Block.getBlockFromItem(stack.getItem());
			return b.getDefaultState().getMaterial().getMaterialMapColor().colorValue;
		}
		int color = ColorUtil.getAverageColorFromStack(stack);
		if(amplify) color = ColorUtil.amplifyColor(color);
		return color;
	}
}
