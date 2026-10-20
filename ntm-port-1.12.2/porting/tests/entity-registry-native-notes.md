# Native entity registration / spawn eggs

Production change: only src/main/java/com/hbm/entity/ModEntityList.java.
No main-tree writes, commits, downloads or pushes were performed by this worker.

## Apply and reproduce

1. Run `python3 apply.py --target /path/to/ntm-port-1.12.2` from this bundle. The script requires the original current-owner SHA256 before replacing it, permits an already identical result, and copies the native test plus pinned mapping manifest/notes. It will not overwrite divergent files.
2. Build the falling-root prerequisite using its `probe.py` (or use that root's successful existing classes). Set `FALLING_ROOT` to its location.
3. Run `JAVA_HOME=/path/to/jdk8 NATIVE_CLASSPATH="...native Forge/Minecraft classpath..." NTM_ROOT=/path/to/main NTM_ORIGINAL=/path/to/pristine FALLING_ROOT=/path/to/falling python3 probe.py` from this portable bundle. Defaults point to the current workspace. Native classpath must not contain replacement Minecraft/Forge/entity owner fixture classes. The actual falling entity is recompiled into this proof's clean output, not accepted from pre-existing bytecode.

## Evidence and contract

`native-proof.log`: full 212-entry source manifest parity audit plus 38 native assertions. Complete wrapper and complete selected EntityFallingBlockNT compile against native Forge/Minecraft. Genuine Loader.setupTestHarness supplies a DummyModContainer; genuine EntityRegistry.registerModEntity populates ForgeRegistries and EntityList. Tests assert absent identity before registration, actual identity afterward, actual owner instantiation, exact ID 158/range 1000/update 1/velocity true, and wrapper's original no-egg creation gate. An instrumented native EntityEntry factory demonstrates wrapper routing through native registry factory rather than a private reflective class map.

Additional explicit fixture entity subclasses exercise all four overloads, color-sentinel versus hasEgg semantics, default/custom tracking, native Items.SPAWN_EGG/ItemMonsterPlacer EntityTag.id, deep stack-copy isolation, NBT roundtrip, duplicate ID/class/key rejection, missing/negative IDs, null/non-egg behavior and late public egg helper. Fixture entities are test-only, not substitutes for production registration or production entities. Actual ItemMonsterPlacer spawning into a running world is not exercised.

`mapping-manifest.json` enumerates all 212 original registration calls in actual execution order (entity list first, mob list second), preserving local IDs, source names, argument options, and kind. `original-sha256.json` pins pristine owner/mappings/localization evidence. `evidence-input-sha256.json` records actual compiled sources and four explicit falling prerequisite collaborator fixtures. Falling-root classes include BlockFallingNT and its native model support; unrelated registry/gui collaborators are explicitly scoped by that root. No replacement net.minecraft/net.minecraftforge classes are compiled here. No fake global ID map or early identity seeding is used.

Original egg API audit: eggIdMap, eggIdList, getUniqueEntityEggId, eggIDCounter, registerEntityEgg and getEggFromEntity have no outside production callers. Only EntityMappings invokes registerEntity. Integer global egg IDs and APIs are removed; public egg helper uses native ResourceLocation and empty-stack behavior for unavailable eggs. getData/getName/createEntityByID safely handle negative IDs. Original hasEgg and color1 != -1 predicates remain distinct.

Two pre-existing duplicate-name pairs require unique native persistence/network keys:
- ID 142 EntityMinecartCrate retains hbm:entity_ntm_cart_crate; ID 143 EntityMinecartDestroyer gets hbm:entity_ntm_cart_destroyer. Both keep legacy raw/display name entity_ntm_cart_crate, range 250/update 1/velocity false and no egg.
- EntityCloudFleijaRainbow retains hbm:entity_cloud_rainbow; EntityCloudSolinium gets hbm:entity_cloud_solinium. Both keep legacy raw/display name entity_cloud_rainbow, range 1000/update 1/velocity true and no egg. Exact local IDs are in the manifest.
No other native keys are renamed. Native Forge stores EntityEntry display names verbatim, unlike the legacy mod namespace behavior, so registration supplies hbm.<legacyName>, matching existing entity.hbm.<legacyName>.name language keys and existing getName behavior. EntityData.name retains the exact original raw name.

## Boundaries

This is native registration/factory/egg-root evidence, not a full game launch, complete 212-entity compilation, full EntityMappings lifecycle, mod discovery or world-save migration test. The two colliding pairs remain unported classes outside this scope: collision-free mappings are proven statically, not claimed as runtime factory tests for those four owners. Native real falling owner is the representative runtime integration. No old 1.7-world import compatibility is promised.
