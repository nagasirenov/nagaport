# Checkpoint78: native damage payloads, item storage and naming

Baseline: recovered and freshly validated CP77 d5ef20a8f1cb65fbd06faa86a4901870c8bab3f2. Published remote main remains CP75 eefb4c1f6f344e4fcb15efb7e6cb6a370419a570. No push. Cumulative delivery is relative to that published CP75, replacing earlier recovery archives.

## Production

43 Java files change.52 LivingHurtEvent/LivingAttackEvent payload accesses become genuine native getters/setters. Compound stores preserve float narrowing, receiver capture, RHS evaluation, exception/null ordering and original formulas. All20 selected production store expressions are tested; no cancellation, branch or armor balance change.

37 DamageSource identifier tokens migrate both direct/indirect source methods and both Sedna overrides. getEntity maps to getTrueSource; getSourceOfDamage maps to getImmediateSource, as established from original mapped bytecode. Null shooters do not acquire a projectile fallback; virtual forwarding and custom Sedna dispatch remain. Overlapping event fields were merged by current compiler-attributed identifier spans, not overwritten whole files.

Eight nested item-NBT loaders across six files preserve original nullable storage via the existing unchanged helper. One old NBT emptiness call becomes genuine isEmpty. Crate contents, armor modifications, filter records and the radiation-shield container loader keep original cleanup/alias semantics.224 new native checks plus191 helper checks pass. This does NOT test player inventory radiation, ground radiation, shielding arithmetic, whole inventory lifecycle or world integration.

Four item naming overrides retain their texture side effects through the existing native declaration registry.12 callers retain custom/inherited virtual dispatch; two minecart texture readers use that same declaration. Three descendants are unchanged.491 native method-slice checks and20 decoded pristine PNG checks pass. Minecart layered icons are not flattened; full cart callbacks, complete owner compilation and client visual acceptance remain unfinished.

## Current verification

Full clean OFFLINE compile: FAIL12775 diagnostics, versus12887 at the previous checkpoint,112 fewer. All4065 generated Java compiler inputs exactly match source bytes. Counts are diagnostics, not distinct bugs or progress percentages. No runnable JAR, client/server startup, full machine or gameplay claim.

Full historical source-boundary guard passes, including43 exact current whole-file inverses and drift rejection. Per-family proof logs retain the scope of native fixture collaborators and excluded full lifecycle behavior. Their isolated whole-file manifests target the CP77 baseline; after overlap composition, use the combined source guard and integrated DamageSource manifest rather than treating a different complete-file hash as corruption.

Bounded native payload tests:542 genuine native API/bytecode checks,9072 exact store-slice checks,1120 side-effect/null-order checks and31 attribution fixtures. Dependency/source override census is finite, not a guarantee about unknown future mods. Unknown accessor overrides are an explicit negative witness rather than silently claimed legacy equivalence.

Damage-source tests execute14 original-engine and50 native assertions including both full production Sedna classes;8 compiler fixtures reject unrelated and unresolved receivers. The integrated manifest preserves already-merged event edits. Current combined run is recorded separately from isolated proof.

## Reproduce / continue

From ntm-port-1.12.2:

    /workspace/shared/nagaport-toolchain/run-gradle.sh --offline -I /workspace/shared/nagaport-toolchain/restore-official-repositories.init.gradle -I porting/compile-diagnostics-expanded.init.gradle clean compileJava --console=plain
    python3 porting/tests/check-thermal-family-source.py

Native classpath /tmp/nagaport-ae2-native-classpath.txt; Java8 /workspace/shared/nagaport-toolchain/java8. Per-family READMEs contain exact native commands. The delivery contains source-only restoration scripts and local commit history, not proprietary Minecraft binaries. Official dependency downloads already required explicit approvals; do not retry a denied route or ModMaven.

Remaining roots include native placement metadata/owner migration, stack-count and null/EMPTY boundaries, graphics API bodies, entity data and engine callbacks. Sound setter/coordinate gates, inherited engine differences and unfinished machine/network/radiation integration remain explicit. No source exclusions, stubs or balance changes are permitted. Keep bounded verified batches and durable Library preservation before relying on local state; no new GitHub push without authorization.

Integrated DamageSource proof is terminal PASS with12775 attribution diagnostics, matching the independent full compile. A clean Gradle build regenerated two class-free dummy ZIP containers. Their checks now pin the official ForgeGradle generator JAR/class and require the sole dummyThing entry to contain exactly FF; added classes/entries are rejected. All real dependency hashes remain strict. This test-only repair avoids mistaking ZIP timestamp changes for engine changes and does not change production dependencies.
