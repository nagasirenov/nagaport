# Bounded native air removal

Exact production baseline: CP55 `2fa015374be0e330d2541e5bfe9d4b96e380aa05`.

## Scope and semantics

150 attributed old-arity World.setBlockToAir calls in 82 existing files, plus one narrow `com.hbm.world.WorldAirRemoval` domain helper. Actual Java 8 compiler attribution requires exactly native World variable/field receiver, three primitive-int arguments and no erroneous argument descendants. No Silo.java, DungeonToolbox.java or central crafting/ore registration changes. All 150 CP55 reported wrong-arity air-removal calls are covered; pre-existing BlockPos calls are untouched.

Old `ahb.f(III)Z` calls old `ahb.d(IIILaji;II)Z` with AIR, metadata zero, flags three. That path rejects X/Z outside [-30000000,30000000) and Y outside [0,256) before accessing a chunk. Actual native World.setBlockToAir(BlockPos) delegates to setBlockState with AIR default state and flags three, but native setBlockState only checks build height. A direct BlockPos rewrite would silently mutate out-of-domain horizontal chunks.

The helper checks null after receiver and all coordinates have been evaluated exactly once, rejects the original domain, and calls the real native virtual setBlockToAir, preserving its exact boolean return. No destroyBlock replacement, synthesized drop, custom block-state adapter, World shim or platform stub is used. Existing surrounding drops, returns, short-circuit conditions and actions are byte-preserved. No allocation/OOM or exception-message identity equivalence is claimed.

## Evidence

- `proof.log`: original and native full-source attribution at exact invocation positions: 150/150. Expected unfinished-port compiler errors are explicitly not successful whole-source compilation.
- `final-runtime.log`: 2,440 genuine original/native World and Chunk removal/unchanged-return/domain/evaluation assertions; 80 executions of the complete actual TileEntityMachineRocketAssembly.deletePlatform body, extracted unchanged into a TileEntity test owner; six direct compiled original-call evaluation checks; ten positive/negative attribution controls; seven rejected bounds/null/coordinate/return/drop-path mutants.
- `boundary-bytecode.txt`: real old and native bytecode, including native flags/default-state dispatch and old coordinate gate.
- `DispatchAudit.java`: actual classpath bytes, five World hierarchy classes among 48,240 native dependency classes and five among 1,803 original-client classes. Neither hierarchy contains a mutation override outside World. New overrides fail closed. Source audit finds only FauxWorld, which declares no old/native block mutation override; original source is archived too. Arbitrary externally added mods are not certified.
- `baseline.zip`, `manifest.json`, `audit.py`: archived complete changed-file inputs, exact selected invocations, byte-exact target regeneration and reversal. CRLF/trailing whitespace is deliberately retained.

The original runtime test loads the unchanged signed 1.7.10 client in an isolated loader, executes actual WorldClient/World/Chunk/ExtendedBlockStorage bytecode and supplies only a recording chunk-provider seam. Unsafe bypasses unrelated client startup; client flag avoids unavailable networking, and absent surrounding chunks bound lighting work. The native storage fixture inherits actual World mutation methods, supplies actual Chunks, and suppresses checkLight propagation only. Neither probe replaces World or Chunk mutation implementations. Native server-side neighbor notification executes; arbitrary custom-block drops, lighting, networking, whole affected classes, complete world generators and in-game parity are not claimed. Direct original null/evaluation fixture is moved to a named package only to avoid signed default-package collision; client bytes remain unchanged.

## Reproduce

From the module, use the supplied Java 8 JDK, existing native classpath and legacy client:

    python3 porting/tests/native-air-removal/prove.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt --legacy-client /tmp/nagaport-legacy-client.jar --attribute --mutants

Dependency hashes are recorded. No downloads, full Gradle builds, source exclusions or pushes are needed.

For a later tree, do not overwrite overlapping complete production files. Preflight then use exact invocation-only integration:

    python3 porting/tests/native-air-removal/merge.py --module TARGET_MODULE
    python3 porting/tests/native-air-removal/merge.py --module TARGET_MODULE --apply --receipt RECEIPT.json
    python3 porting/tests/native-air-removal/audit.py --module TARGET_MODULE --receipt RECEIPT.json

All files preflight before any write. Original invocation occurrence counts and ordinals must match; ambiguous/changed calls fail closed. Receipt verifies exact inverse to the later tree, retaining unrelated edits. Baseline prove.py intentionally remains strict rather than weakening whole-file input guards after integration; run native attribution separately on the integrated full source if its other files differ. `integration-preflight.log` records successful read-only preflight against then-current main.
