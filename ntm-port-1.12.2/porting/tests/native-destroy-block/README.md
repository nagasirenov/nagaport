# Bounded native World destruction

Baseline: sealed CP86 `fc2cd2dba34caba0dba74ed288f95107ab4dc979`.

## Production scope

60 original `World.func_147480_a(int,int,int,boolean)` invocations in 50 files are replaced, plus the small NTM-owned `WorldBlockDestruction` helper. Java 8 attribution requires a variable/field receiver of exactly declared native `World` type, three primitive `int` coordinates, a primitive `boolean` flag, and no erroneous expression descendants. Every receiver and argument expression is preserved verbatim, with receiver, X, Y, Z and flag evaluated once in that order. This deliberately retains the pre-existing X-for-Z argument in `TileEntityNukeCustom`; this task does not repair unrelated gameplay behavior.

The original mapped `ahb.a(IIIZ)Z` first reads through `World.getBlock`, which rejects X/Z outside [-30000000,30000000) and Y outside [0,256). Native `getBlockState` lacks that horizontal rejection. The helper checks null after all arguments, rejects the original domain before any chunk lookup, and invokes actual virtual native `destroyBlock(BlockPos,boolean)`, returning its result. It does not use `setBlockToAir`, duplicate events, invent item drops, recompute metadata, or rewrite any mod drop/formula. Actual native and old bytecode both order event 2001, optional chance-1/fortune-0 drop, then AIR mutation with flags 3; mutation can return false even after an event/drop. No new world shim or fake Forge type is introduced. Allocation/OOM behavior and exception-message identity are not claimed equivalent.

## Engine boundary, not universal parity

The unchanged 1.7.10 client is a vanilla runtime oracle, not a Forge runtime. Authentic Forge1614 and FML World patches are archived and matched to the provided userdev jar. They do not patch the destroy method; Forge changes `isAirBlock` separately, and changes block mutation internals for snapshots/notifications. The original destroy material-AIR test differs from Forge1.12 native virtual `Block.isAir`. Native Block's default implementation checks material AIR. The only current NTM isAir declaration is actual `BlockBeamBase`, constructed with AIR and returning true; its complete source compiles and is exercised here.

An actual external dependency is different: AE2 rv6-stable-7 `appeng.block.paint.BlockPaint` constructs `MaterialLiquid(MapColor.AIR)` and returns true from isAir. Native destruction leaves that block untouched. This is intentionally native1.12 engine behavior, not old-engine parity for foreign blocks. No speculative rejection, extra state/isAir probe, or alternate mutation path is inserted. The archive includes the exact dependency-byte override inventory and relevant AE2 bytecode, inspected with javap from `/workspace/shared/nagaport-toolchain/gradle-user-home/caches/minecraft/deobfedDeps/deobf/appeng/appliedenergistics2/rv6-stable-7/appliedenergistics2-rv6-stable-7.jar`; the jar SHA-256 is in `dependency-hashes.json`. Neither arbitrary external mods nor future World/Block overrides are certified. The source/helper preserves virtual native dispatch in-domain; current dependency World subclasses have no mutation override. The bounds contract follows current original World behavior, not hypothetical externally overridden out-of-range destroy methods.

## Executed evidence

- `manifest.json`, `baseline.zip`, `audit.py`: exact invocation-only edits, byte-exact target regeneration and reverse to complete original inputs, preserving CRLF and all surrounding code.
- `proof.log`: 3,357 genuine native and 3,296 original assertions, plus six direct original evaluation checks, covering genuine native World/Chunk domain, event/drop/removal, metadata, final-return and evaluation assertions; genuine unchanged original World/Chunk assertions; six direct compiled original invocation-order tests. Full-source attribution is explicitly not whole-source compile success.
- Native callbacks use an actually registered recording Block only to observe the real final drop hook. Events use actual World listener dispatch. All 16 metadata values and both flag values are checked; an event listener removes the block first to prove final false returns survive. Real native stone destruction spawns exactly one cobblestone only when requested.
- Original fixtures use actual WorldClient/World/Chunk/ExtendedBlockStorage, recording chunk-provider and event-listener interfaces, and a recording Block. Unsafe bypasses unrelated startup. Client mode avoids networking and ordinary item-spawn setup; native stone item spawning is tested separately. Only fixture classes are moved to a named package to avoid the signed default-package collision; the original client jar is unchanged.
- The native fixture inherits actual World mutations and actual Chunks, suppressing only lighting propagation. Full real `TileEntityDummy`, `BlockBeamBase`, `IMultiblock` and existing `WorldBlockState` sources compile. TileEntityDummy's server destruction/client guard and actual beam AIR behavior execute. No compilation or runtime claim is made for the other 49 affected production classes, full generators/machines, network synchronization, lighting, all mod item-drop callbacks, or in-game parity.
- Two positive and ten negative attribution controls; eight behavioral mutants for domain, null, coordinates, flag, boolean return and drop suppression must fail.
- `DispatchAudit.java`: actual-byte World hierarchy inspection across native dependencies and original client. `BlockAirAudit.java`: actual native dependency isAir override inventory. Source checks fail when the audited World or NTM isAir declaration set changes.

## Reproduce without network or full build

From the module:

    python3 porting/tests/native-destroy-block/prove.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt --legacy-client /tmp/nagaport-legacy-client.jar --legacy-forge /tmp/nagaport-forge1614-userdev.jar --attribute --mutants

For overlapping later source, do not overwrite complete files. Use exact occurrence-guarded, invocation-only integration:

    python3 porting/tests/native-destroy-block/merge.py --module TARGET_MODULE
    python3 porting/tests/native-destroy-block/merge.py --module TARGET_MODULE --apply --receipt RECEIPT.json
    python3 porting/tests/native-destroy-block/audit.py --module TARGET_MODULE --receipt RECEIPT.json

All files preflight before any write; count or anchor ambiguity fails closed. Receipt reversal hashes verify preservation of unrelated changes. `integration-test.py` proves later-tree sentinel preservation and changed-anchor rejection. Baseline whole-file verification intentionally remains strict; use receipt or manifest reverse composition when another checkpoint edits overlapping files. No main worktree changes, full Gradle build, downloads or pushes were performed.
