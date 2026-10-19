package com.hbm.world;

import com.hbm.config.SpaceConfig;
import com.hbm.config.WorldConfig;
import com.hbm.dim.BiomeCollisionException;
import com.hbm.dim.BiomeGenBaseCelestial;
import com.hbm.dim.Ike.BiomeGenIke;
import com.hbm.dim.dres.biome.BiomeGenBaseDres;
import com.hbm.dim.dres.biome.BiomeGenDresCanyon;
import com.hbm.dim.dres.biome.BiomeGenDresPlains;
import com.hbm.dim.duna.biome.BiomeGenDunaHills;
import com.hbm.dim.duna.biome.BiomeGenDunaLowlands;
import com.hbm.dim.duna.biome.BiomeGenDunaPlains;
import com.hbm.dim.duna.biome.BiomeGenDunaPolar;
import com.hbm.dim.duna.biome.BiomeGenDunaPolarHills;
import com.hbm.dim.eve.biome.BiomeGenBaseEve;
import com.hbm.dim.eve.biome.BiomeGenEveMountains;
import com.hbm.dim.eve.biome.BiomeGenEveOcean;
import com.hbm.dim.eve.biome.BiomeGenEvePlains;
import com.hbm.dim.eve.biome.BiomeGenEveRiver;
import com.hbm.dim.eve.biome.BiomeGenEveSeismicPlains;
import com.hbm.dim.laythe.biome.BiomeGenBaseLaythe;
import com.hbm.dim.laythe.biome.BiomeGenLaytheCoast;
import com.hbm.dim.laythe.biome.BiomeGenLaytheIslands;
import com.hbm.dim.laythe.biome.BiomeGenLaytheOcean;
import com.hbm.dim.laythe.biome.BiomeGenLaythePolar;
import com.hbm.dim.minmus.biome.BiomeGenMinmusBasin;
import com.hbm.dim.minmus.biome.BiomeGenMinmusHills;
import com.hbm.dim.moho.biome.BiomeGenBaseMoho;
import com.hbm.dim.moho.biome.BiomeGenMohoBasalt;
import com.hbm.dim.moho.biome.BiomeGenMohoCrag;
import com.hbm.dim.moho.biome.BiomeGenMohoLavaSea;
import com.hbm.dim.moho.biome.BiomeGenMohoPlateau;
import com.hbm.dim.orbit.BiomeGenOrbit;
import com.hbm.dim.moon.BiomeGenMoon;
import com.hbm.dim.thatmo.BiomeGenThatmo;
import com.hbm.dim.tekto.biome.BiomeGenBaseTekto;
import com.hbm.dim.tekto.biome.BiomeGenForest;
import com.hbm.dim.tekto.biome.BiomeGenHalogenHills;
import com.hbm.dim.tekto.biome.BiomeGenPolyvinylPlains;
import com.hbm.dim.tekto.biome.BiomeGenTetrachloricRiver;
import com.hbm.dim.tekto.biome.BiomeGenVinylSands;
import com.hbm.dim.duna.biome.BiomeGenBaseDuna;
import com.hbm.dim.minmus.biome.BiomeGenBaseMinmus;
import com.hbm.lib.RefStrings;
import com.hbm.util.Compat;
import com.hbm.world.biome.BiomeGenCraterBase;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = RefStrings.MODID)
public class ModBiomes
{
    private static final ResourceLocation ORBIT_BIOME_NAME = new ResourceLocation(RefStrings.MODID, "orbit");
    private static final ResourceLocation CRATER_BIOME_NAME = new ResourceLocation(RefStrings.MODID, "crater");
    private static final ResourceLocation CRATER_INNER_BIOME_NAME = new ResourceLocation(RefStrings.MODID, "crater_inner");
    private static final ResourceLocation CRATER_OUTER_BIOME_NAME = new ResourceLocation(RefStrings.MODID, "crater_outer");

    @SubscribeEvent
    public static void registerCelestialBiomes(RegistryEvent.Register<Biome> event)
    {
        BiomeGenBaseDres.dresPlains = new BiomeGenDresPlains(SpaceConfig.dresBiome);
        BiomeGenBaseDres.dresCanyon = new BiomeGenDresCanyon(SpaceConfig.dresBasinBiome);

        BiomeGenBaseDuna.dunaPlains = new BiomeGenDunaPlains(SpaceConfig.dunaBiome);
        BiomeGenBaseDuna.dunaLowlands = new BiomeGenDunaLowlands(SpaceConfig.dunaLowlandsBiome);
        BiomeGenBaseDuna.dunaPolar = new BiomeGenDunaPolar(SpaceConfig.dunaPolarBiome);
        BiomeGenBaseDuna.dunaHills = new BiomeGenDunaHills(SpaceConfig.dunaHillsBiome);
        BiomeGenBaseDuna.dunaPolarHills = new BiomeGenDunaPolarHills(SpaceConfig.dunaPolarHillsBiome);

        BiomeGenBaseEve.evePlains = new BiomeGenEvePlains(SpaceConfig.eveBiome);
        BiomeGenBaseEve.eveOcean = new BiomeGenEveOcean(SpaceConfig.eveOceanBiome);
        BiomeGenBaseEve.eveMountains = new BiomeGenEveMountains(SpaceConfig.eveMountainsBiome);
        BiomeGenBaseEve.eveSeismicPlains = new BiomeGenEveSeismicPlains(SpaceConfig.eveSeismicBiome);
        BiomeGenBaseEve.eveRiver = new BiomeGenEveRiver(SpaceConfig.eveRiverBiome);

        BiomeGenBaseLaythe.laytheIsland = new BiomeGenLaytheIslands(SpaceConfig.laytheBiome);
        BiomeGenBaseLaythe.laytheOcean = new BiomeGenLaytheOcean(SpaceConfig.laytheOceanBiome);
        BiomeGenBaseLaythe.laythePolar = new BiomeGenLaythePolar(SpaceConfig.laythePolarBiome);
        BiomeGenBaseLaythe.laytheCoast = new BiomeGenLaytheCoast(SpaceConfig.laytheCoastBiome);

        BiomeGenBaseMinmus.minmusPlains = new BiomeGenMinmusHills(SpaceConfig.minmusBiome);
        BiomeGenBaseMinmus.minmusCanyon = new BiomeGenMinmusBasin(SpaceConfig.minmusBasinBiome);

        BiomeGenBaseMoho.mohoCrag = new BiomeGenMohoCrag(SpaceConfig.mohoBiome);
        BiomeGenBaseMoho.mohoBasalt = new BiomeGenMohoBasalt(SpaceConfig.mohoBasaltBiome);
        BiomeGenBaseMoho.mohoLavaSea = new BiomeGenMohoLavaSea(SpaceConfig.mohoLavaBiome);
        BiomeGenBaseMoho.mohoPlateau = new BiomeGenMohoPlateau(SpaceConfig.mohoPlateauBiome);

        BiomeGenBaseTekto.polyvinylPlains = new BiomeGenPolyvinylPlains(SpaceConfig.tektoPolyvinylBiome);
        BiomeGenBaseTekto.halogenHills = new BiomeGenHalogenHills(SpaceConfig.tektoHalogenHillBiome);
        BiomeGenBaseTekto.tetrachloricRiver = new BiomeGenTetrachloricRiver(SpaceConfig.tektoRiverBiome);
        BiomeGenBaseTekto.forest = new BiomeGenForest(SpaceConfig.tektoForestBiome);
        BiomeGenBaseTekto.vinylsands = new BiomeGenVinylSands(SpaceConfig.tektoVinylIslandBiome);

        BiomeGenMoon.biome = new BiomeGenMoon(SpaceConfig.moonBiome);
        BiomeGenIke.biome = new BiomeGenIke(SpaceConfig.ikeBiome);
        BiomeGenThatmo.biome = new BiomeGenThatmo(SpaceConfig.thatmoBiome);

        BiomeGenBaseCelestial[] biomes = {
                (BiomeGenBaseCelestial)BiomeGenBaseDres.dresPlains,
                (BiomeGenBaseCelestial)BiomeGenBaseDres.dresCanyon,
                (BiomeGenBaseCelestial)BiomeGenBaseDuna.dunaPlains,
                (BiomeGenBaseCelestial)BiomeGenBaseDuna.dunaLowlands,
                (BiomeGenBaseCelestial)BiomeGenBaseDuna.dunaPolar,
                (BiomeGenBaseCelestial)BiomeGenBaseDuna.dunaHills,
                (BiomeGenBaseCelestial)BiomeGenBaseDuna.dunaPolarHills,
                (BiomeGenBaseCelestial)BiomeGenBaseEve.evePlains,
                (BiomeGenBaseCelestial)BiomeGenBaseEve.eveOcean,
                (BiomeGenBaseCelestial)BiomeGenBaseEve.eveMountains,
                (BiomeGenBaseCelestial)BiomeGenBaseEve.eveSeismicPlains,
                (BiomeGenBaseCelestial)BiomeGenBaseEve.eveRiver,
                (BiomeGenBaseCelestial)BiomeGenBaseLaythe.laytheIsland,
                (BiomeGenBaseCelestial)BiomeGenBaseLaythe.laytheOcean,
                (BiomeGenBaseCelestial)BiomeGenBaseLaythe.laythePolar,
                (BiomeGenBaseCelestial)BiomeGenBaseLaythe.laytheCoast,
                (BiomeGenBaseCelestial)BiomeGenBaseMinmus.minmusPlains,
                (BiomeGenBaseCelestial)BiomeGenBaseMinmus.minmusCanyon,
                (BiomeGenBaseCelestial)BiomeGenBaseMoho.mohoCrag,
                (BiomeGenBaseCelestial)BiomeGenBaseMoho.mohoBasalt,
                (BiomeGenBaseCelestial)BiomeGenBaseMoho.mohoLavaSea,
                (BiomeGenBaseCelestial)BiomeGenBaseMoho.mohoPlateau,
                (BiomeGenBaseCelestial)BiomeGenBaseTekto.polyvinylPlains,
                (BiomeGenBaseCelestial)BiomeGenBaseTekto.halogenHills,
                (BiomeGenBaseCelestial)BiomeGenBaseTekto.tetrachloricRiver,
                (BiomeGenBaseCelestial)BiomeGenBaseTekto.forest,
                (BiomeGenBaseCelestial)BiomeGenBaseTekto.vinylsands,
                (BiomeGenBaseCelestial)BiomeGenMoon.biome,
                (BiomeGenBaseCelestial)BiomeGenIke.biome,
                (BiomeGenBaseCelestial)BiomeGenThatmo.biome
        };
        ResourceLocation[] names = {
                new ResourceLocation(RefStrings.MODID, "dres_plains"),
                new ResourceLocation(RefStrings.MODID, "dres_canyon"),
                new ResourceLocation(RefStrings.MODID, "duna_plains"),
                new ResourceLocation(RefStrings.MODID, "duna_lowlands"),
                new ResourceLocation(RefStrings.MODID, "duna_polar"),
                new ResourceLocation(RefStrings.MODID, "duna_hills"),
                new ResourceLocation(RefStrings.MODID, "duna_polar_hills"),
                new ResourceLocation(RefStrings.MODID, "eve_plains"),
                new ResourceLocation(RefStrings.MODID, "eve_ocean"),
                new ResourceLocation(RefStrings.MODID, "eve_mountains"),
                new ResourceLocation(RefStrings.MODID, "eve_seismic_plains"),
                new ResourceLocation(RefStrings.MODID, "eve_river"),
                new ResourceLocation(RefStrings.MODID, "laythe_islands"),
                new ResourceLocation(RefStrings.MODID, "laythe_ocean"),
                new ResourceLocation(RefStrings.MODID, "laythe_polar"),
                new ResourceLocation(RefStrings.MODID, "laythe_coast"),
                new ResourceLocation(RefStrings.MODID, "minmus_plains"),
                new ResourceLocation(RefStrings.MODID, "minmus_canyon"),
                new ResourceLocation(RefStrings.MODID, "moho_crag"),
                new ResourceLocation(RefStrings.MODID, "moho_basalt"),
                new ResourceLocation(RefStrings.MODID, "moho_lava_sea"),
                new ResourceLocation(RefStrings.MODID, "moho_plateau"),
                new ResourceLocation(RefStrings.MODID, "tekto_polyvinyl_plains"),
                new ResourceLocation(RefStrings.MODID, "tekto_halogen_hills"),
                new ResourceLocation(RefStrings.MODID, "tekto_tetrachloric_river"),
                new ResourceLocation(RefStrings.MODID, "tekto_forest"),
                new ResourceLocation(RefStrings.MODID, "tekto_vinyl_sands"),
                new ResourceLocation(RefStrings.MODID, "moon"),
                new ResourceLocation(RefStrings.MODID, "ike"),
                new ResourceLocation(RefStrings.MODID, "thatmo")
        };

        Set<Integer> configuredIds = new HashSet<>();
        Set<ResourceLocation> configuredNames = new HashSet<>();
        for(int i = 0; i < biomes.length; i++) {
            int id = biomes[i].getConfiguredId();
            validateCelestialBiomeRegistration(id, names[i], configuredIds, configuredNames);
        }
        for(int i = 0; i < biomes.length; i++) {
            registerCelestialBiome(biomes[i], names[i]);
        }

        registerCelestialBiomeTypes();

    }

    private static void validateCelestialBiomeRegistration(int id, ResourceLocation name,
            Set<Integer> configuredIds, Set<ResourceLocation> configuredNames)
    {
        if(id < 0) {
            throw new IllegalStateException("Invalid configured celestial biome ID: " + id);
        }
        if(id > 255 && !Loader.isModLoaded(Compat.MOD_EIDS)) {
            throw new IllegalStateException("Configured celestial biome ID " + id
                    + " exceeds 255; EIDS is required, or the ID must be <= 255.");
        }
        if(!configuredIds.add(id)) {
            throw biomeConflict("Celestial biome ID " + id + " is configured more than once.");
        }
        if(!configuredNames.add(name)) {
            throw biomeConflict("Celestial biome registry name " + name + " is configured more than once.");
        }

        Biome idConflict = Biome.getBiome(id);
        if(idConflict != null) {
            throw biomeConflict(String.format("Biome ID conflict!\nAttempted to register NTM celestial biome at ID %d, already used by %s (%s).\nPlease modify hbm.cfg to fix this error.",
                    id, idConflict.getRegistryName(), idConflict.getClass().getName()));
        }
        Biome nameConflict = ForgeRegistries.BIOMES.getValue(name);
        if(nameConflict != null) {
            throw biomeConflict("Celestial biome registry name " + name + " is already assigned to "
                    + nameConflict.getClass().getName());
        }
    }

    private static void registerCelestialBiome(BiomeGenBaseCelestial biome, ResourceLocation name)
    {
        int configuredId = biome.getConfiguredId();
        biome.synchronizeTerrainBlockStates();
        Biome.registerBiome(configuredId, name.toString(), biome);
        int registeredId = Biome.getIdForBiome(biome);
        if(registeredId != configuredId
                || ForgeRegistries.BIOMES.getID(biome) != configuredId
                || !name.equals(biome.getRegistryName())
                || ForgeRegistries.BIOMES.getValue(name) != biome) {
            throw biomeConflict("Celestial biome " + name + " registered with ID " + registeredId
                    + " instead of configured ID " + configuredId
                    + "; refusing to continue because celestial chunk data writes configured biome IDs.");
        }
    }

    private static void registerCelestialBiomeTypes()
    {
        BiomeDictionary.addTypes(BiomeGenBaseDuna.dunaPlains, Type.COLD, Type.DRY, Type.DEAD);
        BiomeDictionary.addTypes(BiomeGenBaseDuna.dunaLowlands, Type.COLD, Type.DRY, Type.DEAD);
        BiomeDictionary.addTypes(BiomeGenBaseDuna.dunaPolar, Type.COLD, Type.DRY, Type.DEAD, Type.SNOWY);
        BiomeDictionary.addTypes(BiomeGenBaseDuna.dunaHills, Type.COLD, Type.DRY, Type.DEAD,
                Type.HILLS, Type.SNOWY, Type.MOUNTAIN);
        BiomeDictionary.addTypes(BiomeGenBaseDuna.dunaPolarHills, Type.COLD, Type.DRY, Type.DEAD);
        BiomeDictionary.addTypes(BiomeGenBaseMinmus.minmusCanyon, Type.COLD, Type.DRY, Type.DEAD,
                Type.SNOWY, Type.MOUNTAIN);
        BiomeDictionary.addTypes(BiomeGenBaseMinmus.minmusPlains, Type.COLD, Type.DRY, Type.DEAD,
                Type.SNOWY, Type.MOUNTAIN);

        BiomeDictionary.addTypes(BiomeGenBaseEve.evePlains, Type.HOT, Type.DRY, Type.DEAD, Type.SPOOKY);
        BiomeDictionary.addTypes(BiomeGenBaseEve.eveOcean, Type.HOT, Type.DRY, Type.DEAD, Type.SPOOKY);
        BiomeDictionary.addTypes(BiomeGenBaseEve.eveMountains, Type.HOT, Type.DRY, Type.DEAD, Type.SPOOKY);
        BiomeDictionary.addTypes(BiomeGenBaseEve.eveSeismicPlains, Type.HOT, Type.DRY, Type.DEAD, Type.SPOOKY);
        BiomeDictionary.addTypes(BiomeGenBaseEve.eveRiver, Type.HOT, Type.DRY, Type.DEAD, Type.SPOOKY);

        BiomeDictionary.addTypes(BiomeGenBaseLaythe.laytheIsland, Type.COLD, Type.WET, Type.DENSE, Type.SPOOKY);
        BiomeDictionary.addTypes(BiomeGenBaseLaythe.laytheOcean, Type.COLD, Type.WET, Type.DENSE, Type.SPOOKY);
        BiomeDictionary.addTypes(BiomeGenBaseLaythe.laythePolar, Type.COLD, Type.WET, Type.DENSE, Type.SPOOKY);
        BiomeDictionary.addTypes(BiomeGenBaseLaythe.laytheCoast, Type.COLD, Type.WET, Type.DENSE, Type.SPOOKY);

        BiomeDictionary.addTypes(BiomeGenBaseTekto.polyvinylPlains, Type.HOT, Type.DRY, Type.DEAD, Type.SPOOKY);
        BiomeDictionary.addTypes(BiomeGenBaseTekto.halogenHills, Type.HOT, Type.DRY, Type.DEAD, Type.SPOOKY);
        BiomeDictionary.addTypes(BiomeGenBaseTekto.tetrachloricRiver, Type.HOT, Type.DRY, Type.DEAD, Type.SPOOKY);
        BiomeDictionary.addTypes(BiomeGenBaseTekto.forest, Type.HOT, Type.DRY, Type.DEAD, Type.SPOOKY);
        BiomeDictionary.addTypes(BiomeGenBaseTekto.vinylsands, Type.HOT, Type.DRY, Type.DEAD, Type.SPOOKY);
    }

    @SubscribeEvent
    public static void registerOrbitBiome(RegistryEvent.Register<Biome> event)
    {
        int id = SpaceConfig.orbitBiome;
        if (BiomeGenOrbit.biome != null) {
            throw new IllegalStateException("Orbit biome was already assigned before its registry event");
        }
        if (id < 0) {
            throw new IllegalStateException("Invalid configured Orbit biome ID: " + id);
        }
        if (id > 255 && !Loader.isModLoaded(Compat.MOD_EIDS)) {
            throw new IllegalStateException("Configured Orbit biome ID " + id
                    + " exceeds 255; EIDS is required, or the ID must be <= 255.");
        }

        Biome idConflict = Biome.getBiome(id);
        if (idConflict != null) {
            String message = String.format("Biome ID conflict!\nAttempted to register NTM Orbit biome at ID %d, already used by %s (%s).\nPlease modify hbm.cfg to fix this error.",
                    id, idConflict.getRegistryName(), idConflict.getClass().getName());
            if (SpaceConfig.crashOnBiomeConflict) {
                throw new BiomeCollisionException(message);
            }
            throw new IllegalStateException(message + "\ncrashOnBiomeConflict=false cannot safely overwrite a Forge biome registry entry.");
        }

        Biome nameConflict = ForgeRegistries.BIOMES.getValue(ORBIT_BIOME_NAME);
        if (nameConflict != null) {
            throw new IllegalStateException("Orbit biome registry name " + ORBIT_BIOME_NAME
                    + " is already assigned to " + nameConflict.getClass().getName());
        }

        BiomeGenOrbit orbitBiome = new BiomeGenOrbit();
        Biome.registerBiome(id, ORBIT_BIOME_NAME.toString(), orbitBiome);

        int registeredId = ForgeRegistries.BIOMES.getID(orbitBiome);
        if (registeredId != id) {
            throw new IllegalStateException("Orbit biome registered with ID " + registeredId
                    + " instead of configured ID " + id + "; refusing to continue because Orbit chunks write the configured ID.");
        }

        BiomeGenOrbit.biome = orbitBiome;
    }

    @SubscribeEvent
    public static void registerCraterBiomes(RegistryEvent.Register<Biome> event)
    {
        Biome[] biomes = {
                BiomeGenCraterBase.craterBiome,
                BiomeGenCraterBase.craterInnerBiome,
                BiomeGenCraterBase.craterOuterBiome
        };
        int[] ids = {
                WorldConfig.craterBiomeId,
                WorldConfig.craterBiomeInnerId,
                WorldConfig.craterBiomeOuterId
        };
        ResourceLocation[] names = {
                CRATER_BIOME_NAME,
                CRATER_INNER_BIOME_NAME,
                CRATER_OUTER_BIOME_NAME
        };

        Set<Integer> configuredIds = new HashSet<>();
        Set<ResourceLocation> configuredNames = new HashSet<>();
        for (int i = 0; i < biomes.length; i++) {
            validateCraterBiomeRegistration(ids[i], names[i], configuredIds, configuredNames);
        }

        for (int i = 0; i < biomes.length; i++) {
            Biome.registerBiome(ids[i], names[i].toString(), biomes[i]);
            int registeredId = Biome.getIdForBiome(biomes[i]);
            if (registeredId != ids[i]
                    || ForgeRegistries.BIOMES.getID(biomes[i]) != ids[i]
                    || !names[i].equals(biomes[i].getRegistryName())
                    || ForgeRegistries.BIOMES.getValue(names[i]) != biomes[i]) {
                throw biomeConflict("Crater biome " + names[i] + " registered with ID " + registeredId
                        + " instead of configured ID " + ids[i] + "; refusing to continue because crater chunks write the configured ID.");
            }
        }

        if (WorldConfig.enableCraterBiomes) {
            for (Biome biome : biomes) {
                BiomeDictionary.addTypes(biome, Type.DRY, Type.DEAD, Type.WASTELAND);
            }
        }
    }

    private static void validateCraterBiomeRegistration(int id, ResourceLocation name,
            Set<Integer> configuredIds, Set<ResourceLocation> configuredNames)
    {
        if (id < 0) {
            throw new IllegalStateException("Invalid configured crater biome ID: " + id);
        }
        if (id > 255 && !Loader.isModLoaded(Compat.MOD_EIDS)) {
            throw new IllegalStateException("Configured crater biome ID " + id
                    + " exceeds 255; EIDS is required, or the ID must be <= 255.");
        }
        if (!configuredIds.add(id)) {
            throw biomeConflict("Crater biome ID " + id + " is configured more than once.");
        }
        if (!configuredNames.add(name)) {
            throw biomeConflict("Crater biome registry name " + name + " is configured more than once.");
        }

        Biome idConflict = Biome.getBiome(id);
        if (idConflict != null) {
            throw biomeConflict(String.format("Biome ID conflict!\nAttempted to register NTM crater biome at ID %d, already used by %s (%s).\nPlease modify hbm.cfg to fix this error.",
                    id, idConflict.getRegistryName(), idConflict.getClass().getName()));
        }

        Biome nameConflict = ForgeRegistries.BIOMES.getValue(name);
        if (nameConflict != null) {
            throw biomeConflict("Crater biome registry name " + name + " is already assigned to "
                    + nameConflict.getClass().getName());
        }
    }

    private static RuntimeException biomeConflict(String message)
    {
        if (SpaceConfig.crashOnBiomeConflict) {
            return new BiomeCollisionException(message);
        }
        return new IllegalStateException(message
                + "\ncrashOnBiomeConflict=false cannot safely overwrite a Forge biome registry entry.");
    }
}
