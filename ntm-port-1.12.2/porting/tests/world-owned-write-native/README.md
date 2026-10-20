# Owned native state writes, bounded CP82 family

Base: `df18fc077648dd18e6d0c1055d82c11dc2ebdb78`.

## Production boundary

57 missing legacy `World.setBlock` statements now use the existing `WorldBlockState.write(World,int,int,int,IBlockState,int)` boundary:

- 18 calls in `BlockKeyhole`, 18 in `BlockRedBrickKeyhole`, 21 in `Antenna`.
- 36 exact `ModBlocks.brick_red` identities, metadata 0–6; 20 `steel_poles`, metadata 2/3/4; one `pole_top`, metadata 4. All retain flags 3 and ignored boolean results. No return-valued write is selected.
- The three caller files are exactly reversible to the CP82 archive except for the listed statements. Their other unfinished code is not repaired, suppressed or compiled as a fake replacement.
- `BlockRedBrick` receives a genuine integer property covering every stored nibble 0–15, with native state conversion and constructor default state. Its original icon branch is unchanged: side equals metadata selects front/top, and metadata 6–15 has no ordinary front face. This does not reinterpret metadata 6 as zero.
- Its missing inherited icon field becomes NTM-owned, with the two obsolete vanilla `@Override` annotations removed. The complete owner now compiles against genuine native APIs, not a stub. The icon methods remain available to the existing NTM-owned icon layer; this is not a claim that the full RedBrick renderer/model route has been ported.
- RedBrick's existing placed-by callback now preserves the original piston integer-corner, float-proximity, strict vertical thresholds and yaw arithmetic. It deliberately does not use native half-block/eye-height facing. Native feet-relative Y replaces legacy offset-relative Y, consistently with the prior Wand boundary. The callback changes the actual stored block's state, not an assumed callback receiver, retaining flags 2 and the original coordinate domain. Its bounded cases are tile-free owners, not a general metadata-only migration for arbitrary blocks.

## Assignment, evaluation and state closure

The compiler resolves all selected receivers to exact native World variables/fields and all block references to the exact static ModBlocks fields. Every original invocation position has a CP82 missing-method diagnostic. There is exactly one source assignment for each selected field, no declaration initializer and no source subclass of the three actual owners. Each assignment constructs its stated exact class through native Block identity-returning builders; both attributed method ownership and runtime identity are checked. Unreviewed aliases, field initializers, constructor replacements, extra assignments and new source subclasses fail the proof. In-domain equivalence assumes completed ModBlocks initialization and the exact nonnull constructor identities. External mods/reflection replacing public fields are outside this closed source contract.

The separate `null-block-limits.log` records an intentionally excluded case: an original in-domain null target throws NPE without changing the stored block ID in seeded stone, air and empty-section fixtures, but it does invalidate a height cache and may allocate/count an empty storage section before failure. Native conversion throws before chunk access. Thus matching exception type does NOT establish pre-initialization null-target equivalence. Selected normal lifecycle writes use the three initialized nonnull registered owners; manual/pre-init calls or reflection replacements are outside that claim. No native production failure-side-effect emulation is added.

ModBlocks fields can be null before initialization. Therefore an eager `ModBlocks.field.getStateFromMeta` before the old bounds check would be incorrect. Each replacement captures the original receiver, coordinates, block lookup, metadata and flags exactly once in left-to-right order; checks the receiver; rejects the old X/Z/Y domain; only then converts the captured block to a genuine native state and calls the unchanged boundary. The duplicate domain check inside that boundary is intentional. No Block/metadata compatibility API, lazy fake World, universal masked conversion, source exclusion or platform mutation override is introduced.

The actual three conversion implementations resolve all 16 canonical values to stable immutable states. A five-method bytecode audit rejects field/array writes, synchronization, dynamic invocation and unreviewed calls in the conversion graph. Original raw out-of-range metadata is deliberately not normalized at call sites: an actual original repeated write of raw 16 stores 0 but returns changed again. This is why arbitrary raw input is not admitted.

Native engine behavior remains native. The previously documented observer notifications, DEBUG_ALL_BLOCK_STATES rejection, lighting checks and Forge snapshot differences are accepted platform adaptation; this patch does not override them or alter flags. No full gameplay equivalence for those platform systems is asserted.

## Evidence and original provenance

Run from the module:

    python3 porting/tests/world-owned-write-native/run.py --attribute --mutants

The test compiles complete production owners, native helper and actual icon contracts. No production Minecraft/Forge class is stubbed. It reuses the previous suite's genuine World/Chunk fixture, not the previous source snapshot guard.

Original block owners come directly from the supplied repository file `mods1710/HBM-NTM-.1.0.27_X5645_H261.jar`, SHA-256 `3f5629077628b5956ff7ff4e254c25ef0e1bc51f5b6668fde77badd5f784f67b`. `dependency-provenance.json` pins that jar, the original vanilla client, Forge1614 and SRG archive, plus the original and remapped hashes of all three exact classfile entries. `LegacyOwners.java` performs symbol-only SRG-to-obfuscated and owner-package remapping; no method instructions are deleted/replaced. These are not compiler-generated reconstructions. Original DecoPoleTop's static render-ID initialization executes the genuine Forge1614 RenderingRegistry from its archived binaries jar.

`proof.log` records:

- 36,416 assertions over actual original/native World/Chunk/storage, all owner nibbles, exact identity and metadata, changed/unchanged results, horizontal/height limits, flags, alias identity, inherited builder identity, original icon selection, actual placement callbacks, subclass virtual-dispatch controls and actual-state ownership.
- An orientation differential against the actual original BlockPistonBase method, including float-boundary/corner/large-coordinate/yaw values, plus explicit offset-relative-to-feet-relative coordinate cases. No native facing substitute is used.
- 10 generated-production-template null/bounds/evaluation/conversion/dispatch checks and six directly compiled original-call checks. Only the latter test class is package-relocated to avoid the signed original default-package collision; original Minecraft bytecode is unchanged.
- Five conversion bytecode audits, two positive and ten negative invocation-attribution controls, and two rejected assignment-closure mutations.
- Attribution of all 57 new exact native-state boundary positions. Remaining unrelated whole-source attribution diagnostics: 12,296 versus 12,359 at CP82. This is not full compile success.
- Five owner behavior mutants, five capture/order behavior mutants and all 57 individual call-deletion mutations rejected. Exact source regeneration/inverse preserves all bytes outside the selected statements in the three caller files; the complete bounded RedBrick delta is separately pinned.

## Limits

The fixture executes actual original vanilla 1.7 World/Chunk and native Forge 1.12 mutation methods. The original Forge mutation-patch analysis and platform distinctions remain in the earlier `world-block-write-native` bundle. Original startup unrelated to this boundary is bypassed with Unsafe; native broad lighting and server networking are not integrated. Client-side storage fixtures suppress unrelated engine lifecycle startup. Actual selected owners are tile-free and have no custom onBlockAdded/removal mechanic to discard; direct placed-by callbacks are separately executed. No assertion covers arbitrary old-block removal effects, unrelated tile refresh, original Forge snapshot capture in a launched world, addon reflection/reassignment or third-party World subclasses.

Full enclosing dungeon/keyhole methods, full client/server sessions, save/reload, multiblock machinery, RedBrick model registration and remaining metadata-only callers are NOT certified. No full mod build, downloads, pushes or main-tree edits were performed by this worker. The parent owns combined compilation and integration.
