# CP88 cart and siege primitive entity metadata

Source baseline: CP87 `c2f60149e9a9fb836bade505fe566d40a54ef3cc`. Six production owners, 12 private static final native keys, 38 pristine watcher invocations and 38 precisely attributed CP87 metadata diagnostics. No new owner scope beyond this frozen family.

## Ownership and closure

- EntityMinecartNTM: CART_BASE / VARINT / Integer 0 replaces legacy constant `cart_base_id = 23`. The original NBT reader's literal 23 and all four constant-based operations now use that same owned key. The former public constant has no other source reference. Enum ordinals, invalid-index failures, default zero, `base` integer NBT, and calls to the genuine minecart parent remain unchanged.
- EntityMagnusCartus: BOGIE_ID / VARINT / Integer 0 replaces slot25. Its setter still assigns the ordinary bogie reference first and then publishes the entity ID. Packet receipt does not update the ordinary reference. Loading still asks the receiving world for the saved `bogie` ID and calls the setter only when the object is an EntityMinecartBogie. Missing tags select zero; unresolved and wrong-type lookups leave existing values untouched. No new reconnection, persistence or null handling was added.
- EntityUFOBase: WAYPOINT_X/Y/Z / VARINT / zero replace slots17/18/19. All three ordinary integers remain independent. Both siege flying descendants and the unchanged FBI drone inherit the exact same parent-owned keys.
- EntitySiegeUFO and EntitySiegeTunneler each own a separate SIEGE_TIER / VARINT / zero, originally slot12.
- EntitySiegeCraft owns SIEGE_TIER / VARINT / zero, LOCKON_X/Y/Z / FLOAT / +0F, and BEAM / BYTE / zero, originally slots12–16. Double-to-float narrowing, Vec3d construction, equality-to-1 beam reads and whole-byte boolean replacement are unchanged.

Native keys allocate once against the declaring class. They are not numeric-slot wrappers, aliases, reflection accessors or generic helpers. Registration is unconditional and parent-first on both sides. In the cached native platform, Entity supplies six entries; EntityMinecart supplies six more. Both cart siblings therefore allocate native index12 independently. Native EntityLiving supplies twelve inherited entries in total: UFOBase allocates12–14, SiegeUFO tier15, SiegeCraft tier15/float16–18/byte19, and Tunneler tier12. Opposite sibling loading orders are exercised in separate JVMs.

The source-wide closure contains 13 named classes and 28 reference files. It includes NTM's Ore/Powder/Semtex/ContainerBase/Crate/Destroyer descendants, both cart renderers, FBI drone, all siege renderers, spawn factories and the exact ClientProxy bindings. Existing typed getters/setters already close all external reads/writes, so no caller code was changed. The legacy MinecartModBase and MinecartDeobf packed-display metadata, unrelated mobs, ItemStack payloads and policy-bearing serializers are excluded.

## Preserved behavior, not repaired behavior

Tier getters retain the null-entry CLAY fallback but still fail for out-of-range indices. Tier NBT selection still indexes the table directly: absent tags select index0, not CLAY, and an unpopulated slot passes null to the setter and fails. All tier attribute/health/damage formulas and modifier behavior are byte-identical to CP87 after the declared metadata substitutions are reversed. These changes do not modernize those legacy attribute names or polish any boss mechanics.

SiegeCraft still saves only its tier among these custom fields. It does not gain waypoint, beam, lockon, lifetime or retreat persistence. SiegeUFO's existing lifetime/retreat tags remain. Neither new state nor corrective fallback logic is introduced. Constructor bodies, AI, combat, movement, random draws, inventory, spawning, explosions, rendering and other gameplay text are protected by complete-file inverse comparison, not assumed from selected test snippets.

## Executed proof and explicit limits

`probe.py` generates 79 pinned exact source slices inside bounded classes with their real packages and named inheritance. These are method projections, NOT complete production owners. It uses the genuine cached Minecraft/Forge EntityDataManager, DataParameter, serializers, PacketBuffer, SPacketEntityMetadata, NBT and entity parent classes. No Minecraft/Forge class or metadata implementation is mocked or replaced. Small World subclasses provide deterministic entity-ID lookups and reject chunk access.

Two fresh JVMs with opposite sibling loading orders each pass 3,695 assertions, 7,390 total:

- Native constructor virtual registration, hard-coded defaults on server/client, parent/child/sibling key allocation, all named descendant metadata layouts, distinct key identities and full/delta/empty/forced packet serialization/application.
- Signed integer extremes, all 256 raw signed BYTE values, byte boolean semantics, float narrowing/overflow/underflow/infinity/NaN/signed-zero behavior, Float.equals NaN equality, dirty resets and immutable integer snapshots. Vec3d's native zero normalization is distinguished from stored FLOAT bits.
- Complete exact cart metadata methods and complete custom cart NBT methods, invoking actual native minecart NBT. Binary NBT tests preserve base ordinal behavior, raw invalid values, zero defaults, bogie lookup behavior, ordinary-reference versus metadata asymmetry and assignment-before-null-failure ordering. Complete NTM/Magnus constructors are included; unchanged named cart descendants use deliberately synthetic minimal constructors and do not exercise inventory/NBT overrides.
- Complete exact waypoint, beam, lockon and tier getters. Complete exact siege custom NBT writers call genuine native superclass writers. The unchanged original NBT tier-selection expression and UFO lifetime-read suffix are separately identified projections.
- Only the exact first metadata statement of each siege `setTier` is executed as the explicitly named fixture method `setTierMetadataPrefix`. The original full setters, attribute side effects, health resets and complete siege NBT readers are NOT executed or represented as complete. They retain unresolved preexisting legacy attribute APIs and are protected by source inverse. No fixture silently substitutes a no-op full setter or reader. SiegeTier uses its exact primitive constructor and selected fields with test-created tier inputs; game item/drop registration and balance values are not simulated.
- Exact Magnus renderer cast/ID lookup and siege renderer tier lookup execute. The siege lockon read is a separately labeled expression projection; its surrounding unported addVector call and renderer GL are NOT executed. No render acceptance claim is made.
- UFOBase, SiegeUFO and SiegeCraft constructors are exact. Tunneler's legacy yOffset/AI constructor and burrowing ancestors use explicitly synthetic constructor-only projections preserving named ancestry and absence of extra metadata. Their AI, attributes, navigation and movement are not tested.

There is no whole-owner compilation, whole-mod build, dedicated-server/client startup, live-world simulation, original-client network compatibility, tier-effect acceptance, inventory acceptance or renderer acceptance claim. Native packet encoding intentionally follows the target platform rather than reproducing old-client wire encoding. CP87 still has 48 unrelated same-file diagnostics; `compiler-attribution.json` records them rather than hiding them.

## Original and source evidence

`source-manifest.json` pins all pristine/baseline/result hashes, each exact transformation, offsets/method/owner/type/default and the descendant hierarchy. `audit.py` requires pristine watcher invocation equality, correct single per-owner allocations, exact unconditional defaults, source-wide typed caller closure, exact NTM literal23 identity and every complete changed file's byte-for-byte inverse. Both production and inverse patches are independently applied and checked.

The original cached 1.7.10 client bytecode and joined SRG mappings establish Entity constructor registration, vanilla parent slots, DataWatcher type/default operations and ObjectUtils.notEqual update behavior. Original EntityMinecart init registers17–22; it does not own23 or25. Original EntityLivingBase uses6–9 and EntityLiving uses10/11, leaving the original siege12–19 arrangement distinct by hierarchy. The genuine original Forge1614 patches are included and hashed: they do not alter these parent metadata registrations. Forge's EntityConstructing/extended-property hooks occur after Entity's virtual entityInit call; no claim is made about arbitrary external mods adding data during those hooks. Native bytecode records the actual target allocation and serialization implementation. `--platform` regenerates both bytecode files from cached binaries and verifies the original Forge patches, without downloads or cache regeneration.

Earlier harness-only failures corrected expectations for unchanged-value empty deltas, synchronized the exhaustive BYTE test through explicit native forced-dirty packets rather than assuming the untransmitted setter resets reached the receiver, and narrowed the unsupported renderer vector-add expression projection. No production source was changed to make those fixtures pass.

## Run without downloads

From repository root:

    python3 ntm-port-1.12.2/porting/tests/native-entity-cart-siege/audit.py /workspace/shared/nagaport/ntm-original-pristine --platform
    python3 ntm-port-1.12.2/porting/tests/native-entity-cart-siege/probe.py

Java8 defaults to `/workspace/shared/nagaport-toolchain/java8`; native classpath defaults to `/workspace/shared/nagaport-toolchain/native-classpath.txt`. Override `JAVA_HOME` / `NAGAPORT_NATIVE_CP_FILE` if needed. See `validation.log`, `platform-manifest.json` and `production.sha256`.
