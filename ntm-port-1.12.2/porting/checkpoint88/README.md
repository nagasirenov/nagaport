# Checkpoint 88: native projectile callbacks, entity metadata and machine vertex batches

Baseline CP87 c2f60149e9a9fb836bade505fe566d40a54ef3cc. Fresh full offline compilation **FAILS with 11,430 diagnostics**, down **215** from 11,645. All **4,069 generated Java inputs** byte-match frozen production. Scope: **34 existing Java files plus one NTM-owned notification helper**. No playable JAR or launched client/server is claimed.

## Shared projectile interface root

Seven owned launch callbacks and 26 resolved callers become native shoot(DDDFF): 33 identifier-only changes in 16 files. Full typed local inheritance closure includes 90 classes, including 25 nested missile classes. All original motion, rotation, reset, RNG, helper dispatch and argument bodies are unchanged. The existing ChopperMine no-op is preserved from pristine source, not added as a stub. The full compile's 78 missing-abstract-shoot diagnostics are now zero; this does not mean those 78 complete classes otherwise compile or function.

41,494 native assertions cover complete source-exact callback/helper bodies in explicit real-Entity test projections, genuine IProjectile interface dispatch, seeded/scripted Gaussian and Boolean order, cached Gaussian state, signed/zero/NaN/infinite inputs and exact output bits. Six runtime mutants, seven original missing-interface compile controls, four source mutations and full caller/owner/type checks pass. Synthetic test constructors and omitted lifecycle bodies are documented, not represented as complete NTM owners. GlyphidBrawler's unrelated method and Bullet heading2 stay unchanged. SiegeLaser's inherited-engine caller remains deferred because native EntityThrowable changes atan2/degree conversion; no unsupported physics-parity claim is made.

## Primitive cart and siege synchronization

Six owners gain 12 correctly owned static native DataParameter keys replacing 38 watcher calls. The 13-class/28-consumer closure, superclass registration, sibling loading order, exact defaults, literal old slot23 cart NBT restore, numeric bounds, dirty decisions and original save omissions are preserved. No mutable payload serializer, new persistence, tier balance or unrelated boss work is introduced.

7,390 assertions across opposite sibling-loading JVMs exercise 79 source-exact slices on native Entity/EntityMinecart/EntityFlying/EntityCreature parents, full/delta packets, independent managers, INT/FLOAT/BYTE values and cart custom NBT quirks. Siege attribute-side-effect methods remain byte-preserved; only their precise metadata prefixes/readers execute in the bounded test. No full constructors, AI, networking transport, travel or game saves are claimed accepted. Original Forge patches and current native bytecode are pinned.

## Loaded paint-removal notifications

Five tool-interaction calls use TileEntityBlockUpdate: one actual stored-state read followed by notifyBlockUpdate(pos,state,state,2). They preserve paint-clear → notification → markDirty ordering, results and all machinery/network formulas. The actual tool/block/tile closure proves these selected same-position chunks are already loaded; the helper is not an unloaded-world compatibility API. Native collision reads are pure for these exact full-cube owners and do not alter navigation. No block write, neighbor call, default state, listener suppression or custom packet is added.

8,078 native and 907 original assertions exercise real listener dispatch, native RenderGlobal margins, PlayerChunkMap coalescing, and genuine block/tile packet construction. Eight behavioral and five deletion controls fail as expected. Native sent-player gating and listener contracts remain ordinary platform behavior, explicitly not universal old-engine equality. Other 38 World calls and seven direct PlayerManager calls remain unchanged.

## Crate sound initialization

Eleven fresh registry chains use legal owner-local setters in BlockCrate/BlockStorageCrate. Original material/name/sound evaluation order and exact WOOD/legacyMetal references remain; every loot/storage/lock/drop method is unchanged. 457 native and 12 original assertions, six runtime mutants, ten source mutants and two access negatives pass. The independent attribution removes eleven sound failures while exposing five existing texture failures. It is not complete crate rendering/gameplay acceptance. Original Forge's public old setter AT is not confused with the current protected native setter. No reflection, cache regeneration or new sound policy occurs.

## Four machine glow batches

SmallReactor, SolarBoiler, DemonLamp and BatteryREDD now use direct native BufferBuilder POSITION_COLOR. Four complete sites retain 56 vertex expressions, 18 original color evaluations, primitive mode, draw locations, geometry, GL/matrix sequence and random/time call counts. Float colors retain multiply-by-255, Java cast and clamp, not native unclamped float assumptions.

Actual original bmh and native BufferBuilder/Tessellator streams match across 136 paired streams, 424 batch comparisons, 12,496 vertices and 848 genuine Mesa driver draws, with 32 drift controls. This is extracted-batch/driver evidence on the tested little-endian platform, not complete renderer linkage, full framebuffer or in-game visual proof. FurnaceSteel is byte-identical to CP87: its mixed UV1-only lightmap format throws in actual native VertexFormat because UV0 must precede UV1. No dummy UV0, global-state workaround or omitted normal/brightness was substituted. The exact rejection is preserved for future work.

## Integration, reproduction and remaining work

The aggregate guard reverses all 34 existing files, pins the helper and rejects 105 mutations before the historical chain. Family source guards pass on the integrated files. Crate and projectile composed runners require the pinned aggregate guard, not skipped validation; the latter reverses only verified sibling edits in untouched callback exclusions (SiegeCraft metadata), then executes actual current method slices. Integrated native crate/projectile reruns pass. No production source changed after the full-build snapshot.

From the module:

    python3 porting/tests/check-thermal-family-source.py
    python3 porting/tests/crate-sound-builders-cp88/check-source.py --composed-source
    python3 porting/tests/crate-sound-builders-cp88/run-native.py --composed-source --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /workspace/shared/nagaport-toolchain/native-classpath.txt --legacy-jar /tmp/nagaport-legacy-client.jar
    python3 porting/tests/native-projectile-shoot/run.py --composed-source
    python3 porting/tests/native-entity-cart-siege/audit.py
    python3 porting/tests/world-update-notification-native/audit.py
    python3 porting/tests/machine-glow-batches/check_source.py
    /workspace/shared/nagaport-toolchain/run-gradle.sh --offline -I /workspace/shared/nagaport-toolchain/restore-official-repositories.init.gradle -I porting/compile-diagnostics-expanded.init.gradle clean compileJava --console=plain
    python3 porting/checkpoint88/verify-build.py

Family READMEs contain the isolated full-attribution and runtime commands and exact limitations. The first offline build poll was interrupted by the stale pending-Mojang-approval premise; its partial log remains. Process absence was checked, actual user grants reconciled and exact denied poll retried once (unknown process), then the same offline command ran to terminal FAIL11,430. No alternative network route, dependency change, source exclusion or fake success.

CP87 was durably saved in Library version15 before this stage. Published GitHub main stays CP75 eefb4c1f6f344e4fcb15efb7e6cb6a370419a570; no new push. Cumulative recovery replaces earlier recovery archives and targets only that exact backed-up clean baseline. Stop on conflicts/CRLF refusal; Windows execution is untested. Potion ID/config/save policy and prior explicit gaps remain isolated. Full mod compilation, startup, actual machines/energy/radiation, synchronization and saves remain unfinished.
