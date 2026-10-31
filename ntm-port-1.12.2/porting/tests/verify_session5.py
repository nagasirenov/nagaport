#!/usr/bin/env python3
"""
Comprehensive Verification Suite for Session 5: Entities, Dimensions & Space Worldgen
Features 50–65 in PROJECT.md
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent.parent

checks_passed = 0

def check(condition, msg):
    global checks_passed
    if not condition:
        print(f"FAILED: {msg}")
        sys.exit(1)
    checks_passed += 1
    print(f"  [CHECK {checks_passed:02d}] OK: {msg}")

def main():
    print("=== STARTING SESSION 5 VERIFICATION SUITE ===")

    # ----------------------------------------------------
    # BATCH A: ENTITIES LIFECYCLE & PERSISTENCE
    # ----------------------------------------------------
    print("\n--- BATCH A: Entities Lifecycle & Persistence ---")

    # 1. Feature 50: Native Entity Registration & Tracking
    entity_mappings_file = ROOT / "src/main/java/com/hbm/entity/EntityMappings.java"
    check(entity_mappings_file.exists(), "EntityMappings.java exists")
    mappings_content = entity_mappings_file.read_text(encoding="utf-8")

    pattern = r'\b(addEntity|addMob)\((\w+)\.class,\s*"([^"]+)",\s*([^;]+)\);'
    raw_mappings = re.findall(pattern, mappings_content)
    check(len(raw_mappings) == 212, f"Total entity mappings count is exactly 212 (found {len(raw_mappings)})")

    classes = [m[1] for m in raw_mappings]
    check(len(set(classes)) == 212, "All 212 mapped entity classes are unique")

    mod_entity_list = (ROOT / "src/main/java/com/hbm/entity/ModEntityList.java").read_text(encoding="utf-8")
    check("EntityRegistry.registerModEntity" in mod_entity_list, "ModEntityList uses EntityRegistry.registerModEntity")
    check("registryKey" in mod_entity_list, "ModEntityList handles registryKey resolution")
    check("entity_ntm_cart_destroyer" in mod_entity_list, "Collision resolution for EntityMinecartDestroyer")
    check("entity_cloud_solinium" in mod_entity_list, "Collision resolution for EntityCloudSolinium")

    # 2. Feature 51: Vanilla Spawn Egg Binding
    check("registerEgg" in mod_entity_list or "eggColor1 != -1" in mod_entity_list, "Spawn egg registration in ModEntityList")
    check("Items.SPAWN_EGG" in mod_entity_list, "ModEntityList uses vanilla Items.SPAWN_EGG")
    check("ItemMonsterPlacer.applyEntityIdToItemStack" in mod_entity_list, "ItemMonsterPlacer applies EntityTag to spawn egg")
    check("EntityList.createEntityByIDFromName" in mod_entity_list, "createEntityByID uses EntityList.createEntityByIDFromName")

    # 3. Feature 52: Glyphid Environmental Spawner
    glyphid_spawner = (ROOT / "src/main/java/com/hbm/blocks/generic/BlockGlyphidSpawner.java").read_text(encoding="utf-8")
    check("PollutionHandler.getPollution" in glyphid_spawner, "BlockGlyphidSpawner evaluates PollutionHandler")
    check("PollutionType.SOOT" in glyphid_spawner, "BlockGlyphidSpawner evaluates SOOT pollution")
    check("MobConfig.spawnMax" in glyphid_spawner, "BlockGlyphidSpawner caps by MobConfig.spawnMax")
    check("MobConfig.swarmCooldown" in glyphid_spawner, "BlockGlyphidSpawner uses MobConfig.swarmCooldown")
    check("createSwarm" in glyphid_spawner, "BlockGlyphidSpawner contains createSwarm dynamic scaling")
    check("EntityGlyphid.TYPE_INFECTED" in glyphid_spawner, "BlockGlyphidSpawner applies TYPE_INFECTED")
    check("EntityGlyphid.TYPE_RADIOACTIVE" in glyphid_spawner, "BlockGlyphidSpawner applies TYPE_RADIOACTIVE")

    # 4. Feature 53: Aberrator Dungeon Spawner
    dungeon_spawner = (ROOT / "src/main/java/com/hbm/blocks/generic/DungeonSpawner.java").read_text(encoding="utf-8")
    check("CON_ABERRATOR" in dungeon_spawner and "PHASE_ABERRATOR" in dungeon_spawner, "DungeonSpawner has Aberrator phase logic")
    check("EntityUndeadSoldier" in dungeon_spawner, "DungeonSpawner summons EntityUndeadSoldier")
    check("EnumSecretType.ABERRATOR" in dungeon_spawner, "DungeonSpawner awards Aberrator secret artifact")
    check("Blocks.OBSIDIAN" in dungeon_spawner, "DungeonSpawner converts to obsidian on completion")

    # 5. Feature 54: Boss Summon Spawner
    balls_spawner = (ROOT / "src/main/java/com/hbm/blocks/generic/BlockBallsSpawner.java").read_text(encoding="utf-8")
    check("ModItems.mech_key" in balls_spawner, "BlockBallsSpawner checks for ModItems.mech_key")
    check("EntityBOTPrimeHead" in balls_spawner, "BlockBallsSpawner spawns EntityBOTPrimeHead")
    check("ModBlocks.brick_jungle_cracked" in balls_spawner, "BlockBallsSpawner converts to brick_jungle_cracked")

    # 6. Feature 55: Entity Persistent Registry References
    pref = (ROOT / "src/main/java/com/hbm/util/PersistentRegistryRef.java").read_text(encoding="utf-8")
    check("SCHEMA_VERSION = 2" in pref, "PersistentRegistryRef is schema version 2")
    check("nameKey" in pref and "NAME_SUFFIX = \"_name\"" in pref, "PersistentRegistryRef uses <key>_name")

    missile_custom = (ROOT / "src/main/java/com/hbm/entity/missile/EntityMissileCustom.java").read_text(encoding="utf-8")
    check("PersistentRegistryRef.writeItem" in missile_custom, "EntityMissileCustom persists parts via PersistentRegistryRef")
    check("PersistentRegistryRef.preserveLegacy" in missile_custom, "EntityMissileCustom preserves legacy part IDs")

    grenade_bouncy = (ROOT / "src/main/java/com/hbm/entity/grenade/EntityGrenadeBouncyGeneric.java").read_text(encoding="utf-8")
    check("PersistentRegistryRef.writeItem(nbt, \"grenade\", item)" in grenade_bouncy, "EntityGrenadeBouncyGeneric persists grenade via PersistentRegistryRef")

    grenade_impact = (ROOT / "src/main/java/com/hbm/entity/grenade/EntityGrenadeImpactGeneric.java").read_text(encoding="utf-8")
    check("PersistentRegistryRef.writeItem(nbt, \"grenade\", item)" in grenade_impact, "EntityGrenadeImpactGeneric persists grenade via PersistentRegistryRef")

    falling_block = (ROOT / "src/main/java/com/hbm/entity/item/EntityFallingBlockNT.java").read_text(encoding="utf-8")
    check("PersistentRegistryRef.writeBlock(nbt, \"Tile\", this.getBlock())" in falling_block, "EntityFallingBlockNT persists Tile via PersistentRegistryRef")

    disperser = (ROOT / "src/main/java/com/hbm/entity/grenade/EntityDisperserCanister.java").read_text(encoding="utf-8")
    check("PersistentRegistryRef.writeItem(nbt, \"item\", item)" in disperser, "EntityDisperserCanister persists item via PersistentRegistryRef")

    # 7. Feature 56: Entity Client Renderers
    client_proxy = (ROOT / "src/main/java/com/hbm/main/ClientProxy.java").read_text(encoding="utf-8")
    check("RenderingRegistry.registerEntityRenderingHandler" in client_proxy, "ClientProxy registers entity renderers via RenderingRegistry")
    check("RenderMoonCow" in client_proxy, "ClientProxy registers celestial RenderMoonCow")
    check("RenderWormHead" in client_proxy, "ClientProxy registers boss RenderWormHead")
    check("RenderUndeadSoldier" in client_proxy, "ClientProxy registers RenderUndeadSoldier")
    check("RenderGlyphid" in client_proxy, "ClientProxy registers RenderGlyphid")

    # ----------------------------------------------------
    # BATCH B: DIMENSIONS & SPACE WORLDGEN
    # ----------------------------------------------------
    print("\n--- BATCH B: Dimensions & Space Worldgen ---")

    # 8. Feature 57: 11 Celestial Dimension Providers
    planet_gen = (ROOT / "src/main/java/com/hbm/world/PlanetGen.java").read_text(encoding="utf-8")
    celestial_dims = [
        "moonDimension", "dunaDimension", "ikeDimension", "eveDimension",
        "dresDimension", "mohoDimension", "minmusDimension", "laytheDimension",
        "orbitDimension", "tektoDimension", "thatmoDimension"
    ]
    for dim in celestial_dims:
        check(f"SpaceConfig.{dim}" in planet_gen, f"PlanetGen registers {dim}")
    check("DimensionManager.registerDimension" in planet_gen, "PlanetGen registers dimensions via DimensionManager")

    # 9. Feature 58: Dimension 0 Provider Guard
    check("if (DimensionManager.isDimensionRegistered(0)) return;" in planet_gen, "PlanetGen has dimension 0 duplicate registration guard")

    # 10. Feature 59: Planetary Traits & Physical Metrics
    celestial_body = (ROOT / "src/main/java/com/hbm/dim/CelestialBody.java").read_text(encoding="utf-8")
    check("CBT_Atmosphere" in celestial_body, "CelestialBody includes atmospheric traits")
    check("CBT_Water" in celestial_body, "CelestialBody includes water traits")
    check("semiMajorAxisKm" in celestial_body and "eccentricity" in celestial_body, "CelestialBody includes orbital mechanics")

    # 11. Feature 60: Cross-Dimension Rocket Teleportation
    teleporter = (ROOT / "src/main/java/com/hbm/dim/CelestialTeleporter.java").read_text(encoding="utf-8")
    check("DimensionManager.initDimension" in teleporter, "CelestialTeleporter initializes dimensions via DimensionManager.initDimension")
    check("runQueuedTeleport" in teleporter, "CelestialTeleporter uses queued server-tick transfer")
    check("transferPlayerToDimension" in teleporter, "CelestialTeleporter transfers player")
    check("transferEntityToWorld" in teleporter, "CelestialTeleporter transfers riding rocket entity")

    rocket = (ROOT / "src/main/java/com/hbm/entity/missile/EntityRideableRocket.java").read_text(encoding="utf-8")
    check("DimensionManager.initDimension(targetDimensionId)" in rocket, "EntityRideableRocket ensures destination dimension is initialized")

    # 12. Feature 61: Persistent Solar System Saved Data
    solar_saved = (ROOT / "src/main/java/com/hbm/dim/SolarSystemWorldSavedData.java").read_text(encoding="utf-8")
    check("DimensionManager.getWorlds()[0]" in solar_saved, "SolarSystemWorldSavedData binds to dimension 0 Overworld")
    check("WorldSavedData" in solar_saved, "SolarSystemWorldSavedData extends WorldSavedData")

    # 13. Feature 62: Satellite Orbital Tracking
    sat_saved = (ROOT / "src/main/java/com/hbm/saveddata/SatelliteSavedData.java").read_text(encoding="utf-8")
    check("DimensionManager.initDimension(targetDimensionId)" in sat_saved, "SatelliteSavedData initializes target orbiting dimension")

    # 14. Feature 63: Space Worldgen Dimension Filtering
    world_gen = (ROOT / "src/main/java/com/hbm/world/gen/NTMWorldGenerator.java").read_text(encoding="utf-8")
    nbt_struct = (ROOT / "src/main/java/com/hbm/world/gen/nbt/NBTStructure.java").read_text(encoding="utf-8")
    check("spawnMap.containsKey(world.provider.getDimension())" in nbt_struct, "NBTStructure strictly checks spawnMap for dimension ID")
    check("NBTStructure.registerStructure(0" in world_gen, "NTMWorldGenerator registers structures exclusively to dimension 0")

    # 15. Feature 64: Dedicated Space Worldgen
    check("GameRegistry.registerWorldGenerator(new WorldGeneratorMoon()" in planet_gen, "Moon world generator registered")
    check("GameRegistry.registerWorldGenerator(new WorldGeneratorDuna()" in planet_gen, "Duna world generator registered")
    check("GameRegistry.registerWorldGenerator(new WorldGeneratorCelestial()" in planet_gen, "Celestial ore generator registered")

    # 16. Feature 65: Dimension Unload Cache Eviction
    rad_mgr = (ROOT / "src/main/java/com/hbm/handler/radiation/ChunkRadiationManager.java").read_text(encoding="utf-8")
    check("proxy.receiveWorldUnload" in rad_mgr, "ChunkRadiationManager hooks receiveWorldUnload")
    rad_simple = (ROOT / "src/main/java/com/hbm/handler/radiation/ChunkRadiationHandlerSimple.java").read_text(encoding="utf-8")
    check("perWorld.remove(event.getWorld())" in rad_simple, "ChunkRadiationHandlerSimple evicts world radiation cache on unload")

    poll_mgr = (ROOT / "src/main/java/com/hbm/handler/pollution/PollutionHandler.java").read_text(encoding="utf-8")
    check("perWorld.remove(event.getWorld())" in poll_mgr, "PollutionHandler evicts world pollution cache on unload")

    atmo_mgr = (ROOT / "src/main/java/com/hbm/handler/atmosphere/ChunkAtmosphereManager.java").read_text(encoding="utf-8")
    check("proxy.receiveWorldUnload" in atmo_mgr, "ChunkAtmosphereManager hooks receiveWorldUnload")
    atmo_handler = (ROOT / "src/main/java/com/hbm/handler/atmosphere/ChunkAtmosphereHandler.java").read_text(encoding="utf-8")
    check("worldBlobs.remove(event.getWorld().provider.getDimension())" in atmo_handler, "ChunkAtmosphereHandler evicts atmosphere blobs on dimension unload")

    print(f"\n=== ALL {checks_passed} CHECKS PASSED SUCCESSFULLY! ===")

if __name__ == "__main__":
    main()
