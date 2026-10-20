# Incremental fresh local item and XP spawning

Apply **after** the immutable 43-site item-spawn production/evidence pair. Worker baseline is local c07b3228 (the CP57 tree plus that exact pair); no other root is included. This increment changes only **24 method-name tokens in 20 files**: 23 fresh local vanilla EntityItem producers and one inline vanilla EntityXPOrb in IWeaponAbility. All use actual native World.spawnEntity. No helper/shim, alias API, global event change, UUID bypass or dead-entity resurrection is introduced.

## Selection proof

LocalSpawnScanner uses javac symbol identity, not variable spelling or regex receiver suffixes:

1. The argument must be a local variable initialized directly by the exact five-argument native vanilla EntityItem constructor, without anonymous/custom subclasses.
2. The complete receiver must resolve as an exact World variable/field. The manifest records full receiver start/end, original declaration-through-call scope, argument symbol, and token position.
3. Every reference to that local symbol is inspected, across the compilation unit. Besides the sole spawn argument, only native Entity primitive motionX/Y/Z, posX/Y/Z, rotationYaw/Pitch and velocityChanged fields are allowed. Reads and assignments to these fields preserve existing preparation, including its evaluation order. No aliases are accepted in this bounded slice.
4. Direct reassignment, passing/storing/returning the entity, another spawn, arbitrary instance calls, dead/UUID fields or methods, and unsupported legacy getter/pickup-delay operations reject the candidate. Declaration and every use must have identical method/class/lambda/loop ancestry. This excludes reusing one constructed item across a newly introduced loop or closure.
5. Independent post-migration attribution verifies actual World.spawnEntity ownership and **complete receiver ranges** for every selected local site. Independently attributed preexisting native candidates must match as a complete position/receiver map, not just a count. XP has separate exact native constructor/call and receiver-range checks.

The four positive compiler controls include h.nested.world and a fresh constructor inside a loop. Eleven negative invocations exercise dead/UUID mutation, aliasing, reassignment, escape, callback, repeated spawning, outer-loop reuse, lambda capture and dead-field assignment. The post-migration controls compile successfully against real native APIs, proving their rejection is semantic/dataflow-based rather than caused by absent methods.

## Behavior and boundaries

All base World/WorldServer/WorldClient/event findings and the explicit **unknown external EntityConstructing/capability mutation or identity-collision gate** from native-entity-spawn/README.md continue to apply. NTM's owned constructor/event and lifespan audit applies equally to these exact vanilla EntityItems. The preparation whitelist cannot mutate identity, mark the entity dead, or escape it. It intentionally excludes legacy getEntityItem/NBT and delayBeforeCanPickup paths for separate API-root work.

The XP constructor is exact vanilla EntityXPOrb(World,double,double,double,int). Both binaries initialize health/size, position, random motion and XP value, without intentional death or UUID reuse; supplied Forge1614 XP patch changes pickup handling, not construction. NTM's construction/capability hooks are living/player-restricted, so do not mutate XP orbs. Unlike EntityItem, XP is not suppressed while restoring block snapshots. The old constructor also writes its legacy yOffset; target vanilla geometry/lifetime/pickup changes are **not** certified as original gameplay parity here. This migration preserves the original call arguments and uses the native vanilla entity rather than inventing a fake legacy XP class.

Custom gas/vent and grenade constructors remain excluded: their chains still depend on removed yOffset and additional constructor semantics. No reused/transferred/deserialized entities are included. Complete consumer classes and whole-mod compilation are not claimed.

## Tests

`run.py` checks current hashes, virtually reverses these 24 tokens, validates the exact immutable 43-site postimage, then reverses those 43 tokens to their CP57 hashes. This handles the intentionally overlapping EntityMovingPackage file without editing the earlier evidence. The earlier strict snapshot-only check_source.py remains a check of the earlier checkpoint; use this integrated inverse after applying the increment.

It compiles and executes four **actual source-extracted bounded producer scopes** against native APIs: EntityMovingPackage motion output, TileEntityMachineStrandCaster output, ExplosionLarge debris preparation and the IWeaponAbility XP call. Fixture wrappers supply surrounding fields/arguments only; native WorldServer/WorldClient, providers, chunks, constructors, event dispatch and callbacks run unchanged. It verifies cancellation, unloaded rejection, exact stack identity versus the original copied debris stack, position/motion and the native XP value. The original raw-position/AABB mismatch in ExplosionLarge is deliberately preserved and asserted, not silently corrected.

Passing run: 57 added native producer checks, plus the original 127 native boundary checks; 49 genuine original vanilla item and 49 genuine original vanilla XP bytecode checks. Original Forge1614 event/snapshot additions remain authenticated patch evidence, not claimed executed by the vanilla binary oracle. Supplied class hashes and XP constructor bytecode are retained. All actual local calls ignore the return already; native boundary tests continue to check actual booleans.

Reproduce from project root with existing dependencies, without downloading:

    python3 porting/tests/native-local-spawn/run.py --java-home JAVA8 --classpath-file CLASSPATH_FILE --legacy-client LEGACY_CLIENT_JAR --attribute

The source compiler's unresolved unrelated diagnostics are recorded and are not full-mod compilation. No client graphics, integrated gameplay, multiplayer, saved world or forced unloaded-server chunk-generation acceptance is asserted.
