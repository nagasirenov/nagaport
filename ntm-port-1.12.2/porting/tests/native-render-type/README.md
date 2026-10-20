# Native invisible render callbacks (CP71 source boundary)

Base: `112cf44057e5cd1d1173561e66dbcca97edc9bdb`. Production changes are only 62 callback signatures/bodies. Main/CP72 was not modified.

## Scope and exact findings

Whole-source census finds 98 legacy zero-argument declarations: 70 literal `return -1` and 28 nonnegative/custom-ID domains. Migrate 62 exact negative declarations. Exclude seven BlockDummyable-domain declarations, whose existing state callback delegates to the old virtual hook; changing them would disturb that domain. Exclude BlockGasBase because BlockGasClorine overrides with 0, requiring separate geometry/model treatment. Every remaining declaration is listed in census.json; no positive ID is mapped to MODEL by this change.

Four real descendants inherit migrated methods: ConnectorRedWire, ConnectorRedWireSuper and PylonRedWire from PylonBase, and DummyBlockMachine from DummyOldBase. No descendant overrides render type. Native javac independently attributes all 62 methods as overrides of Block.getRenderType(IBlockState), not merely lookalike names.

Most selected owners already inherit INVISIBLE from native BlockContainer: this retires their erroneous old override, not a claim of newly fixing their appearance. BlockVacuum, PistonInserter and BlockCrashedBomb inherit through native Block instead and now expressly restore their original negative world-geometry exclusion.

Original RenderBlocks.b(Block,x,y,z) calls original Block.b(), compares exactly -1 and returns false before accessing world/geometry. Native BlockRendererDispatcher.renderBlock calls IBlockState.getRenderType(), compares INVISIBLE and returns false before accessing world/geometry. Native state dispatch calls Block.getRenderType(state). Original WorldRenderer and native RenderChunk collect tile entities independently before normal block geometry dispatch/rejection. Pinned bytecode excerpts and class hashes are in engine-audit.json.

## Consumer and preservation audit

Five direct source call sites are retained in attribution/calls.tsv. BlockDummyable's zero-argument call is intentionally unchanged. RenderMinecartTest's obsolete call on native Block remains an unresolved historical error; this work does not conceal or repair it. RenderMovingItem and NativeBlockMaterial already consume native state render types. LegacyBaseModelHandler consumes the native block/state callback and also reflectively looks up the old zero-argument method. Migrated BlockBase descendants remain ineligible for its cube fallback: the native state guard requires MODEL, so removing reflective -1 discovery cannot enable fallback cubes. This reflective consumer is checked separately from the direct-call census.

Only callback slices differ from the sealed base. Tile factories, TESR registration, item registration and model handlers, renderer bodies, collision, opacity, state factories and metadata methods are byte-for-byte untouched. Existing ClientProxy bindings for TileEntityPistonInserter/RenderPistonInserter and TileEntityCrashedBomb/RenderCrashedBomb remain. No new placeholder models or adapters are supplied. Retaining existing registrations is not proof they already function natively.

## Executed proof and limits

- Exact 62 source reversals against CP71 and original pristine constant-negative callbacks.
- Full-source native attribution: 4,065 units, zero parse errors; 13,966 -> 13,904 diagnostics. The only removed diagnostics, normalized for source line shifts, are the 62 selected invalid overrides. All remaining diagnostics persist; this is NOT a successful mod build.
- Executed 1,984 native state/world-dispatch tests: 62 extracted production callbacks, inherited controls and all 16 synthetic metadata states per fixture. Original 1.7.10 RenderBlocks negative dispatch executed against a real original Block subclass. Renderer instances are allocated without constructors solely to exercise the early-return branch; null world/buffer prove no geometry path is entered.
- Rejected mutants: old-signature compile failure, MODEL and state-dependent dispatch assertions.

Fixtures compile exact production method slices, not complete 62 block classes. Synthetic state containers do not prove actual custom state serialization. Full mod initialization, renderer constructors/bodies, TESR drawing, item drawing, client/dedicated-server launch, world persistence and in-game pixels are unexecuted and unclaimed. Unsafe exists only in the test harness and is not production code.

## Portable reproduction

Use local existing Java 8, native Forge 1.12.2 classpath text file, original client JAR and 1.7.10 SRG ZIP. No downloads or installs. Set ROOT to the patched ntm-port-1.12.2 directory and PRISTINE to the original pristine project; JDK, CP, OLD and SRG to the respective existing inputs. Run from this evidence directory:

```
python run-attribution.py --root "$ROOT" --java-home "$JDK" --classpath-file "$CP"
python check-attribution.py
python check-source.py --root "$ROOT" --pristine "$PRISTINE"
python check-engine.py --java-home "$JDK" --classpath-file "$CP" --legacy-jar "$OLD" --legacy-srg "$SRG"
python run-native.py --root "$ROOT" --java-home "$JDK" --classpath-file "$CP" --legacy-jar "$OLD"
python check-mutants.py --root "$ROOT" --java-home "$JDK" --classpath-file "$CP" --legacy-jar "$OLD"
```

check-source additionally requires the base git object and an otherwise production-clean CP71 baseline so it can prove no unrelated sources changed. Later integrations should replay in a source-only worktree at this base. Stored errors.tsv.gz are lossless compressed attribution reports; rerunning attribution generates plain TSV reports. Other paths inside evidence are project-relative. Source method offsets are character offsets in UTF-8 decoded text, with byte-exact SHA-256 boundaries preserving CRLF.
