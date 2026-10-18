package com.hbm.world;

import com.hbm.config.SpaceConfig;
import com.hbm.dim.BiomeCollisionException;
import com.hbm.dim.orbit.BiomeGenOrbit;
import com.hbm.dim.duna.biome.BiomeGenBaseDuna;
import com.hbm.dim.minmus.biome.BiomeGenBaseMinmus;
import com.hbm.lib.RefStrings;
import com.hbm.util.Compat;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

@Mod.EventBusSubscriber(modid = RefStrings.MODID)
public class ModBiomes
{
    private static final ResourceLocation ORBIT_BIOME_NAME = new ResourceLocation(RefStrings.MODID, "orbit");

    public static void init()
    {
        BiomeDictionary.registerBiomeType(BiomeGenBaseDuna.dunaPlains, Type.COLD, Type.DRY, Type.DEAD);
        BiomeDictionary.registerBiomeType(BiomeGenBaseDuna.dunaLowlands, Type.COLD, Type.DRY, Type.DEAD);
        BiomeDictionary.registerBiomeType(BiomeGenBaseDuna.dunaPolar, Type.COLD, Type.DRY, Type.DEAD, Type.SNOWY);
        BiomeDictionary.registerBiomeType(BiomeGenBaseDuna.dunaHills, Type.COLD, Type.DRY, Type.DEAD, Type.HILLS);
        BiomeDictionary.registerBiomeType(BiomeGenBaseDuna.dunaHills, Type.COLD, Type.DRY, Type.DEAD, Type.SNOWY, Type.MOUNTAIN);
        BiomeDictionary.registerBiomeType(BiomeGenBaseMinmus.minmusCanyon, Type.COLD, Type.DRY, Type.DEAD, Type.SNOWY, Type.MOUNTAIN);
        BiomeDictionary.registerBiomeType(BiomeGenBaseMinmus.minmusPlains, Type.COLD, Type.DRY, Type.DEAD, Type.SNOWY, Type.MOUNTAIN);

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
}
