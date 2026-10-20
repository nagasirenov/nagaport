# Registered native-state writes: bounded CP84 family

Base: `502f0b9dc3cd3e14622edd897fe3c753e309818d`.

## Production delta

72 remaining `World.setBlock` statements in 32 callers now capture their original receiver and arguments and invoke the existing `WorldBlockState.write(World,int,int,int,IBlockState,int)` boundary. The patch changes no state codec, target callback, helper implementation, method signature, flag, or surrounding control flow.

- 42 four-argument statements retain implicit metadata 0 / flags 3. Thirty six-argument statements retain their exact metadata expressions. Flags remain 3 in 65 statements and 2 in seven.
- 24 direct newly supported BlockMulti targets, one source-owned crashed-bomb alias, and the original metadata-zero sand self-placement are converted. Literal crashed-bomb metadata 5 and tall-plant upper-half metadata 8/9 remain intact.
- The other 46 statements use exact registered Generic, NoDrop, StepEffect, CT, or native BlockFalling owners with a proven metadata-zero state. The two native falling writes specifically target `concrete_super_broken`.
- All selected calls ignored the old boolean result. Return-valued expressions are not rewritten.
- The source inverse preserves every byte outside the 72 selected statements, including original CRLF in ItemWandD. Its resulting diff whitespace warning is the retained CR character, not extra spaces or line-ending normalization.

Every receiver, coordinate, block expression, metadata expression and flag is captured once in original left-to-right order. A null receiver is checked after argument evaluation, including at invalid coordinates. State conversion occurs only after the old horizontal and height bounds check. The helper's duplicate bounds check is deliberate. No general Block/metadata bridge, metadata-clamping shim, mutation override, or fake World is introduced in production.

## Identity, range and attribution closure

`RegisteredWriteScanner.java` resolves exact native World receivers, exact selected ModBlocks field identities, every argument type and the original source positions. It separately attributes all 32 constructor assignments through reviewed identity-returning builders, casts and PlainCubeTextures.bind. The single DesertAtom001.Block4 initializer resolves to crashed_balefire and source reassignment is rejected. The sand self-placement has no source subclass. External mods/reflection replacing fields or constructing unreviewed subclasses are outside this closed source contract.

The scanner is a typed candidate census, **not** permission to migrate arbitrary metadata. `manifest.json` is the exact reviewed selection. Frozen complete owner/range-dependency files and exact caller inverses preserve the source facts establishing its metadata domains: fixed enum ordinals, exhaustive bounded random switches, the two-value biome enum, the four-value dud enum and the existing rubber-plant state codec. `source-closure-audit.md` details each proof and exclusion. Source changes to these facts fail the guard rather than silently expanding the domain.

`audit.py` deterministically regenerates every replacement from its captured original receiver/arguments, verifies the exact whole-file inverse, checks temporary-name collision absence, pins dependencies, and independently matches all 72 original positions to CP84 missing-setBlock diagnostics. `attributed-before.tsv`, `assignments.tsv` and `attributed-after.tsv` retain the typed positions and owner results. In this isolated family, whole-source attribution reports 12,066 original errors and 11,994 after the migration. Attribution is not successful whole-mod compilation.

## Executed evidence

From the module:

    python3 porting/tests/registered-state-writes/run.py --attribute --mutants

`proof.log` records:

- **38,495 assertions** through genuine original 1.7 and native Forge 1.12 World/Chunk/storage methods. Complete original classfiles and complete native Generic, NoDrop, CT and BlockEnumMulti representatives, plus genuine original/native BlockFalling, cover canonical storage, exact block/state identity, changed/unchanged results, all nibbles for the two nibble codecs, limits and all eight low flag combinations.
- Actual original/native World writes exercise both remote modes and populated/unpopulated chunks. Observation seams record update/neighbor order. The native observer-only notification difference is explicitly retained rather than emulated away.
- Actual original/native BlockFalling on-added callbacks execute and request the same block/position/tickRate-2 schedule. The fixture records that request; it does not claim to run falling physics or the future scheduled tick.
- Sixteen **verbatim ModBlocks construction statement projections** instantiate complete native owners, preserving builder identity and real state storage. A small MainRegistry creative-tab collaborator and two CT identity fields are explicit test collaborators, not the complete registry. Constructors belonging to uncompiled lifecycle owners are attributed and source-pinned, not compiled as fake replacement classes.
- **Five read-only conversion-bytecode audits** reject field/array writes, monitors, invokedynamic and unreviewed calls in the actual native Block, BlockMulti and CT conversion graph. Stable immutable states are exercised separately.
- **15 generated-production-template checks** cover four- and six-argument evaluation, delayed conversion, bounds, nulls, flag forwarding and virtual dispatch. Eight directly compiled original-call cases independently establish original evaluation/null behavior.
- Two positive and eleven negative invocation-attribution controls; three rejected assignment-closure mutations; three rejected production-codec mutations; five rejected capture-template mutations; and rejection of every one of the 72 individual call-deletion mutations by exact-source regeneration.

The actual original supplied mod JAR is `mods1710/HBM-NTM-.1.0.27_X5645_H261.jar`, SHA-256 `3f5629077628b5956ff7ff4e254c25ef0e1bc51f5b6668fde77badd5f784f67b`. `LegacyRegisteredOwners.java` performs symbol-only SRG/package remapping of eight archived original class/interface entries; no method instructions are removed or replaced. `dependency-provenance.json` pins those entries before/after remapping plus the original client, Forge1614 and SRG inputs. The directly compiled OriginalFlagWorld fixture is separately identified and only supplies observation/lighting/scheduling seams. No proprietary binaries are included in this evidence folder.

## Explicit limits

These tests prove this write boundary, not complete target-owner gameplay. Production callbacks remain unchanged, and some still have unrelated legacy API errors. In particular:

- BlockNTMSand scheduling/falling, plant support/paired-half/growth/harvest behavior, and ore-asbestos interactions are **not** certified by their inherited-codec representative.
- CrashedBomb's real TileEntityCrashedBomb creation/refresh/ticking/radiation, activation/explosion and persistence are **not** represented by tile-free codec tests. The production ITileEntityProvider path is retained, never replaced with a plain block. The patch does not claim to finish that lifecycle.
- Native observer notifications, debug-world rejection, lighting and Forge snapshot behavior remain native platform boundaries. Broad light propagation, server networking, arbitrary old-block removal effects, third-party World overrides, launched client/server sessions and save/reload are outside this suite.
- In-domain equivalence assumes initialized nonnull selected registry objects. Original null targets may allocate storage/invalidate caches before throwing, while native state conversion throws earlier. Prior `world-owned-write-native/null-block-limits.log` documents that exclusion; this patch does not fake original failure side effects.
- Original raw metadata 16 can store 0 yet still return changed on a repeated write. The suite executes that counterexample, so the new state codec is not a license for blanket raw masking.

Two OreCave stalagmite writes remain because public mutable ore.meta lacks a closed canonical-nibble domain. Four Chunk absorber writes remain for their separate local/world-coordinate and scheduling/lifecycle boundary. Arbitrary item damage +8 tall-plant self-placement, metadata-only growth mutations and mixed/dynamic write sinks remain separate work. No full build, download, push or main-worktree edit was performed by this worker; combined compilation belongs to the parent checkpoint.
