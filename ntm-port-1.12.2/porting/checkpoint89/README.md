# Checkpoint 89: native event accessors, server update queues and configuration boundaries

Baseline CP88 a65d3acbf05634c3cedbce5eff43ad0bd15cefdb. Fresh full offline compilation **FAILS with 11,151 diagnostics**, down **279** from 11,430. All **4,069 generated Java inputs** byte-match frozen production. Scope: **43 existing Java files, no new production files**. No playable JAR or client/server startup is claimed. Diagnostic reduction is not a count of distinct gameplay bugs.

## Native Forge event fields

165 typed reads and ten genuine setter writes in 21 files replace removed/private fields. Existing conditions, event order, formulas and results remain. Fog components/density, fall-distance assignment/multiply and filled-bucket output use actual native setters; incompatible coordinate/state contracts remain untouched. The AnvilRepairEvent handler is deliberately unchanged: Forge1614's hook passes constructor values in a different order and NTM compensates. Renaming that handler's fields to similarly named getters would break its original meanings. 722 genuine native accessor/constructor/evaluation/dependency checks and 18 actual Forge EventBus assertions compiling the complete production BucketHandler pass. All 165 getters re-attribute to the exact selected native owners, 22 compiler controls and 175 individual source-edit mutations pass, and 46 authentic Forge1614 payload declarations are checked. The BucketHandler fixture uses an explicit in-memory World; this finite batch does not certify the other complete handlers or full gameplay integration.

## Direct server update scheduling

Seven calls in four paintable-pipe/pylon/pipeline owners use actual WorldServer.getPlayerChunkMap().markBlockForUpdate(BlockPos). Arguments retain their original order, values and one-time evaluation. This path does not add World state reads, listener dispatch, chunk generation or watched entries. Unloaded/unwatched positions remain no-create operations. Native sent-to-player gating is retained and documented as an engine boundary. Other World notifications and WorldUtil lifecycle code remain separate. 12,560 native and 1,491 authentic-original assertions, resolved caller ownership, twelve runtime mutants and seven source-deletion mutants pass. Real deferred block/tile packets are constructed, but full transport/watch sessions are not certified. The complete CP88 notification suite also reran successfully (8,078 native and 907 original assertions), using only a pinned exact inverse for the later Pneumo edit.

## Configuration comments

27 writes in complete CommonConfig, BombConfig and WeaponConfig owners use actual Property.setComment. Every comment, default, return, bounds, radius, value, save call and configuration policy remains unchanged. Native, original and native direct-field-oracle runs each pass 742 assertions; all 21 saved-file cases match byte-for-byte. Four runtime mutants, 32 source mutants and exactly 27 original private-field compile failures are retained. The native setter is only direct assignment, including null. Native save clears individual Property changed flags whereas Forge1614 does not; the same-native-engine direct-field oracle matches completely. This engine save distinction is documented, not changed. No potion ID policy is activated.

## Primitive flying-entity metadata

EntityUFO, EntityBFAngel and EntityHunterChopper receive 12 correctly owned native keys replacing 33 watcher calls. All six sibling-loading orders pass 14,969 assertions each (89,814 total). Exact inverses and pristine comparison retain all defaults, byte predicates, unused FLOAT channels, formulas, custom NBT and original save omissions. Native full/delta packets and dirty decisions execute through genuine EntityDataManager. These are 56 bounded source projections with real EntityFlying parents, not full owners or boss gameplay acceptance. Boss HUD and the chopper's independent ordinary isDying field are unchanged; no boss polishing or new mechanics.

## Collision flags and profiling

34 typed field tokens in twelve files become actual native Entity collision flags (13) and World.profiler (21), including machine section instrumentation. All assignments/conditions/section names and formulas are byte-preserved; unqualified reads use explicit this to avoid local capture. Full-source ownership attribution, sixteen typed positive/negative cases and 34 omitted-token controls pass.

117,673 actual original/native field and bounded production assertions cover storage/evaluation/nulls plus the four exact EntityDebrisBase collision assignments across zero, finite, infinite and NaN inputs. Those lines also match pristine original source. Three executable formula mutants fail. Genuine profiler instances execute the original section order and paths. This is not a collision solver, full movement lifecycle, engine timing-statistics or gameplay equivalence claim. Unsafe allocation and final-field setup exist only in tests; production adds no reflection/helper/emulation.

## Integration and continuation

The aggregate boundary reverses all 43 complete files, rejects 129 drift mutants and composes with all earlier source guards. Family source guards are checked on actual integrated sources. Production remained frozen throughout the full compile and generated-input verification. Native family receipts and precise fixture/projection limitations are in their individual READMEs.

From ntm-port-1.12.2:

    python3 porting/tests/check-thermal-family-source.py
    python3 porting/tests/native-collision-profiler-fields/run.py --attribute --mutants
    python3 porting/tests/property-comment-native/source-proof.py
    python3 porting/tests/native-entity-flying-boss/audit.py
    /workspace/shared/nagaport-toolchain/run-gradle.sh --offline -I /workspace/shared/nagaport-toolchain/restore-official-repositories.init.gradle -I porting/compile-diagnostics-expanded.init.gradle clean compileJava --console=plain
    python3 porting/checkpoint89/verify-build.py

The first build poll was denied on the historical pending-Mojang-approval premise. Its partial log is retained. Process absence and missing terminal receipt were checked, actual later user grants reconciled, exact denied poll retried once (unknown process), then the same offline command reached terminal FAIL11,151. No alternate network route, dependency regeneration, cache change or source exclusion was used.

CP88 is durably preserved in Library version16. Published GitHub main remains CP75 eefb4c1f6f344e4fcb15efb7e6cb6a370419a570; no new push. Cumulative recovery targets that exact clean baseline and replaces earlier recovery archives, not a patch chain. Stop on dirty conflicts or CRLF refusal; retained CRLF is intentional and Windows execution is untested. Potion config/save policy and previous explicit gaps remain isolated. Full native compilation/startup, actual machines/energy/radiation, networking and saves are unfinished. Continue from the newest compiler families without repeating historical fixes.
