# Native entity-property capability foundation (CP23)

## Production scope

- New `com.hbm.extprop.HbmEntityProps`: Forge capability registration, per-entity serializable providers, `AttachCapabilitiesEvent<Entity>`, and the original player clone wire-copy operation.
- `HbmLivingProps` / `HbmPlayerProps`: remove `IExtendedEntityProperties`, route access to attached native capabilities, retain existing field defaults and NBT/wire payloads. Property-local `world`, max-health attribute, soul-sand constant, clamp, riding-entity, chest equipment and item-break sound API references are mapped to 1.12.2.
- `MainRegistry.preInit`: register this capability lifecycle before the common handler.
- `ModEventHandler`: remove old property lookups from `EntityConstructing` and remove the old duplicate clone subscriber. Keep the ashes-reset side effect, now using `getEntity()`. The complete original `onEntityDeath` body is unchanged.
- ArmorFSB's remaining `getExtendedProperties` text is inside an already inactive commented-out dash feature. It is not a runtime consumer and was not edited.

No weak map, off-entity fallback, replacement platform class, generic compatibility layer, generic ground-radiation source, saved-world conversion, or achievement replacement was added.

## Original and native lifecycle evidence

The pre-change CP22 source (`f711b253`) is retained in the portable `entity-properties-source-baseline.json` for the two property classes and original death method. Native API ordering was checked against the genuine cached Forge `1.12.2-14.23.5.2768` userdev `sources.zip` and `patches.zip`:

1. `net/minecraft/entity/Entity.java.patch` posts `EntityConstructing` and only then calls `ForgeEventFactory.gatherCapabilities(this)`. A capability lookup from that constructing event is too early. The original common handler was the only such subscriber found; it used the old lazy property registration mechanism. The new provider constructs and binds each payload during the native attachment event without querying the not-yet-installed dispatcher.
2. `Entity` automatically writes/reads `ForgeCaps` using the actual `CapabilityDispatcher`. Each provider's resource name is its serialization key.
3. `EntityPlayerMP.java.patch` posts `ForgeEventFactory.onPlayerClone` from `copyFrom`. The genuine Forge Clone event supplies `getOriginal()`, `getEntityPlayer()` and `isWasDeath()`. The original handler did not branch on death: it copied only HbmPlayerProps' eight-field wire representation. That behavior remains for both death and End-return clone events. It does not copy living properties or the larger persisted player field set.
4. Forge 1.12.2 has no modern invalidate/revive-capabilities lifecycle at this point. Tests read the original capability after native `setDead()` and then dispatch the real clone event factory. No modern-version revival shim was introduced.
5. Original actual external `registerData` call count is zero. Direct property/known radiation helper calls were not found in source entity constructors, `entityInit` or `applyEntityAttributes`. Searches of registry initialization roots found no explicit living/player construction. This bounded source audit is not proof of every future third-party call graph.

`getData`/`registerData` now require an attached capability. Repeated calls on normal constructed entities preserve the same attached payload identity. A missing registration/provider fails explicitly instead of silently creating an unpersisted substitute. This is an invariant assertion for the reviewed native lifecycle, NOT compatibility with arbitrary old third-party calls made before registration, from `EntityConstructing`, or with an unattached manually allocated entity. An actual required early-access path must be reviewed before extending this design.

## Persistence and exact original quirks

The native save shape is:

- `ForgeCaps` → `hbm:living_props` → `HbmLivingProps` → unchanged original fields.
- `ForgeCaps` → `hbm:player_props` → `HbmPlayerProps` → unchanged original fields.

This is native 1.12 persistence, **not 1.7.10 world/player save migration**. There is no automatic read of old top-level property tags and no file conversion.

Retained intentionally:

- All original inner keys, field types, defaults, write/read ordering and MKU configuration gates.
- Player `nitan` is saved as float and read as integer.
- Contamination's original NBT loader reads maxRad from its child but maxTime/time/ignoreArmor from the parent. The existing incorrect parent reads remain, as does list append on repeated load/deserialize.
- Living wire omits blackFire and several transients. Player wire has eight fields, fewer than its save format. Clone retains these omissions, including nitan/warp/dimension/key/cooldown/stamina values.
- Living radiation's direct setter is not newly clamped; only the original increment operation clamps 0–2500.
- The three `triggerAchievement(MainRegistry.digamma...)` calls remain exactly in production. They are still blocked by the unported achievement root, not replaced with statistics.

## Reproducible proof and boundaries

Run from any checkout/extracted archive with a genuine compatible local native classpath and Java 8:

    python3 porting/tests/probe-entity-properties-native.py --java-home /path/to/java8 --classpath "native Minecraft/Forge/dependency classpath"

No Git invocation, pristine-source checkout, network request, dependency download, or hardcoded host path is required by the runner. The frozen source baseline verifies original NBT/wire/defaults/formulas and unchanged death/achievement bodies.

The test compiles **complete production HbmEntityProps, HbmPlayerProps and IGUIProvider**. Its explicit living fixture consists of production HbmLivingProps with only `setDigamma` replaced by a test-only throwing boundary, because native advancement migration is unresolved. It therefore does **not** establish whole HbmLivingProps compilation. All other living fields/methods, including NBT/wire and selected radiation formulas, are the actual production bodies.

Other explicitly controlled NTM collaborators are config flags, atmosphere type, duck/train type identity, armor mods/shield item, damage-source identities, proxy, packet destinations, and chat builder. Keybind enum values are extracted unchanged from production. No Minecraft, Forge, Netty, Entity, capability or NBT implementation is substituted.

The shared `ForgeEventNativeLauncher` uses genuine LaunchWrapper and Forge's actual `EventSubscriptionTransformer`. Tests use real annotated subscriber registration and real entity constructors, not direct calls to attach/clone handlers or manually set entity dispatcher fields. Native CapabilityManager injection is exercised using a bounded ASM annotation table rather than full mod discovery.

The world is a bounded native World subclass with no chunk generation or network. A real native DedicatedServer object is allocated without startup solely to host the genuine DataFixesManager/CompoundDataFixer required by native player saving. Its save logic is not replaced. Native Entity/EntityPlayer NBT and compressed-byte write/read run normally.

Passing checks cover server/client attachment, player/mob/nonliving filtering, owner/instance identity, native ForgeCaps serialization and compressed reload, exact key sets/types/known quirks, independent original wire-byte oracles, real death/nondeath clone-event dispatch and omissions, selected radiation/oxygen formulas, player key edges and item-break cooldown/native sound arguments.

## Still open

- Complete HbmLivingProps compilation, pending the actual achievement/advancement design.
- Full MainRegistry/ModEventHandler and wider dependency-graph compilation.
- Actual production living-death dispatch, tick handlers, radiation sources/transport, armor protection, packet recipients/client synchronization, damage/potion/achievement behavior, real login/logout/respawn/dimension transitions, and loaded-game player radiation acceptance.
- Real audio playback, GUI behavior and networked game operation.

The native lifecycle proof is a real foundation, not a substitute for those open integration tests.
