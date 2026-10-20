# Native vanilla block writes, bounded CP81 family

Baseline: CP81 `242867906a53eb7da03c879f476ced61c1170e14`.

## Production scope

36 missing legacy `World.setBlock` calls in 17 production files now supply genuine native states and flags to the original-coordinate-domain `WorldBlockState.write` boundary. The helper accepts `IBlockState`, never a legacy Block/metadata pair. It performs native virtual `World.setBlockState(BlockPos,IBlockState,int)` dispatch and returns its exact boolean. Existing read/metadata methods and all surrounding production bodies remain byte-identical.

Selected states: AIR 0 (19 calls), FIRE 0 (9), GRASS 0 (2), DIRT 0/1 (2), MOSSY_COBBLESTONE 0 (1), FLOWING_LAVA 0 (2), LAVA 0 (1). Original flags 2/3 are unchanged. Four-argument calls explicitly mean metadata zero and flags three, not an assumed default state.

Selection requires Java compiler attribution of an exact native World variable/field receiver, a real static vanilla Blocks field, primitive-int coordinates with no erroneous descendants, and literal metadata/flags in the bounded ranges. Every selected position is also a CP81 missing-method diagnostic. The 51 structurally eligible vanilla calls include 15 deliberately excluded nonrepresentable metadata cases, listed in `manifest.json`: eight TORCH 0, one CHEST 0, two NETHER_WART 5, DIAMOND_BLOCK 5 and 8, and two LIT_REDSTONE_LAMP 5. Torch 0 maps to native meta 5; chest 0 maps to 2; wart 5 throws; inert block/lamp nonzero metadata collapses to zero. Choosing what these old raw values mean is a migration decision, not authorized by this patch. Generic/custom Block writes and all 266 metadata-only diagnostics remain unchanged pending real owner-state contracts.

## Semantics and boundaries

- Original World writes reject X/Z outside [-30000000,30000000) and Y outside [0,256) before chunk access. Native setBlockState only rejects build height. The production boundary preserves the complete old domain, including signed-overflow results, before any native chunk access.
- Receiver, coordinates, original block-field lookup and supplied flags are each evaluated once in their original left-to-right order. The explicit conversion is the only inserted work before entry. The exact seven vanilla implementations in its call graph have no field/array writes, synchronization, dynamic invocation, World access or unreviewed method calls. Their successful paths only read default/immutable property-state tables. Actual allowed values resolve totally to preconstructed state identities; repeated conversion and metadata round trips are tested. Thus inserted conversion cannot move an observable effect or value error ahead of the original null/bounds checks for this selected family. No claim covers arbitrary custom conversions, reflection-replaced vanilla singletons, allocation/OOM behavior, or exception message identity.
- Original raw metadata storage keeps the low four bits, but arbitrary values are not generally native states and original no-change comparisons can still inspect the unmasked argument. Only exact canonical 0/1 inputs are selected, with real original/native storage tests. No masking normalization was introduced.
- Both original Forge1614 and native Forge mutation paths preserve bit 2 client update before bit 1 neighbor/comparator notification, and bit 4 suppresses client-side rerender. The archived original Forge patch establishes its snapshot-capture suppression. No custom notification/drops/lighting reimplementation is added.
- Native platform behavior is retained and is not asserted to be identical to the old engine: native server DEBUG_ALL_BLOCK_STATES worlds refuse writes; native neighbor notification includes observers, and no-bit-1 writes without bit 16 notify observers separately. Original 1.7 has neither debug-world type nor observers. Native lighting only checks when opacity/emission changes rather than unconditionally. Native Forge snapshot handling remains native. These are explicit platform integration differences, not hidden extra flags or engine overrides; no selected call site targets a debug world or implements a custom observer contract. Full in-game consequences are not certified here.

## Evidence and limits

`proof.log` passes:

- 22,797 assertions running actual original 1.7.10 and native 1.12 World/Chunk/ExtendedBlockStorage mutation bytecode, exact block identity and metadata, changed/unchanged returns, horizontal/height boundaries, low-four-bit original storage, ordered dispatch, null and argument failures, native flags and debug-world rejection.
- Seven actual conversion-method bytecode audits, plus exact allowed-state totality/identity checks.
- Six direct compiled original-call evaluation/null/exception checks. The test owner alone is relocated into a named package to avoid the signed original default-package collision; client bytecode is unchanged.
- Actual dependency hierarchy audit: five native and five original World hierarchy classes have no overriding mutation path outside World. The only source World subclass is FauxWorld, with no mutation override. External mods not in this classpath are not certified.
- Two positive and twelve negative attribution controls; all 36 exact transformed invocation positions resolve to the correct native-state helper overload. Attribution reports 12,435 unrelated unfinished-port diagnostics versus 12,471 at CP81. This is compiler attribution, NOT a successful full compile.
- Eight behavior mutants rejected: lower/upper bounds, height, null handling, coordinate swap, false-return loss, flag changes and metadata/default-state collapse.
- 36 production-call deletion mutations rejected by source guards. All 36 exact inverses across 17 complete archived files preserve every other byte; the added helper leaves existing read/metadata implementations unchanged. Fifteen gated candidates remain untouched.

The native and original storage fixtures are client-side to suppress unrelated placement/ticking/network startup. Actual World and Chunk mutation methods are never stubbed. A recording provider supplies real chunks; native broad lighting propagation is suppressed. Native markAndNotifyBlock itself runs with recording notification seams. The complete production helper runs; affected entity/machine/explosion classes do not run in full. Fire/lava placement callbacks, ticks, drops, lighting, snapshots, networking, arbitrary mod interactions, complete generators and client/server/save integration are not certified. Unsafe bypasses unrelated original client startup only.

`boundary-conversion-bytecode.txt`, `forge1614-World.java.patch.txt`, `dependency-hashes.json`, `attributed.tsv`, `manifest.json` and `baseline.zip` retain evidence. The Floodlight file's original CRLF bytes are deliberately preserved; diff --check flags the two changed CRLF lines as trailing whitespace. Do not normalize them.

## Reproduce

From the module:

    python3 porting/tests/world-block-write-native/run.py --attribute --mutants

Defaults use the restored Java 8 toolchain, `/tmp/nagaport-ae2-native-classpath.txt`, and `/tmp/nagaport-legacy-client.jar`; paths can be supplied as command options. No download, full Gradle build, push, platform shim, source exclusion, or placeholder is required.

Review/apply the isolated commit or patch using ordinary conflict-aware Git operations. Do not overwrite overlapping complete production files from the baseline archive. `audit.py` is intentionally strict for this exact snapshot and should not be weakened to accept unrelated later source drift.
