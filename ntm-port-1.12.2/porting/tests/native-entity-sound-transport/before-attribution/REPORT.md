# CP59 player sound BEFORE attribution

Result: **36 eligible original calls, 21 production files, 11 bundled sound names**. This is source/typing eligibility only, not a claim that a replacement preserves runtime semantics. Production migration and AFTER attribution are paused pending the parent’s dispatch decision and supplied helper/overlay.

- Baseline: `e72ec3d2ae8db6653ebaad529a574ee32c99260b` (CP59).
- Diagnostic seed: `/tmp/compile-cp58.log`, exact missing `playSoundAtEntity(EntityPlayer,String,float,float)` on `World`, limited to `com/hbm/items/tool` and `com/hbm/tileentity/machine`.
- Full real production source was read from a 6,214,848-byte `git archive` ZIP, not a broad copied tree or source fixtures. All 4,056 units were parsed and analyzed by Java 8 `JavacTask`; zero parse errors and 17,475 baseline source errors. No class emission.
- All 41 diagnostic-coordinate invocations were extracted as complete AST expressions. Eligibility uses attributed receiver/argument types and a direct String literal node, not a receiver regex.
- Selected receivers: `world` (27), `this.world` (9), all declared `net.minecraft.world.World`. Every selected argument tuple is exactly `net.minecraft.entity.player.EntityPlayer`, `java.lang.String`, `float`, `float`.
- Receiver, every argument, whole method-select/invocation, and enclosing-method spans are retained. Original source extraction verifies every receiver/argument/invocation and all 25 enclosing method spans. Java offsets are UTF-16 code units.
- All 36 baseline diagnostic failures independently reproduce at their original call sites (`compiler.err.cant.resolve.location.args`).
- Names: `hbm:block.lockHang`, `hbm:block.lockOpen`, `hbm:item.boltgun`, `hbm:item.gasmaskScrew`, `hbm:item.pinBreak`, `hbm:item.pinUnlock`, `hbm:item.repair`, `hbm:item.spray`, `hbm:item.techBleep`, `hbm:item.techBoop`, `hbm:item.unpack`.
- Their 18 referenced OGG samples exist at CP59. Native `ResourceLocation` resolves mixed-case JSON sample paths to the actual lowercase asset paths; original JSON names and definitions remain intact in the manifest.
- Excluded: four `random.orb` calls and `ItemFusionCore`’s `hbm:item.battery` call. No dynamic or missing-bundled-name candidate passed.

## Evidence

- `before-selected-manifest.json`: all 36 full receivers and arguments, original invocations, source ranges/types/elements, source hashes, whole enclosing methods, all bundled sound definitions and OGG hashes, and exclusion rationale.
- `selected-calls.txt`: readable full receiver/argument/invocation listing of all 36 calls.
- `before.tsv`: raw analyzed AST fields for all 41 diagnostic seeds.
- `before-diagnostics.tsv`, `selected-before-diagnostics.json`, `before.log`, `verification.log`: independent compiler and verification evidence.
- `PlayerSoundAttribution.java`: complete source parser/analyzer, with optional sparse AFTER overlay support. No synthetic fixture sources.
- `run-before.sh`, `select_candidates.py`, `ResourceNames.java`: reproducible BEFORE runner and validation.
- `inputs.json`, `native-classpath.txt`, `classpath-inputs.json`: baseline/archive/diagnostic/dependency provenance.

No main-tree changes, patches, replacement helper, or AFTER claim were made. The compressed source ZIP and compiled scanner are under `/tmp/player-sound-attribution`; durable evidence is under `/workspace/shared/player-sound-attribution` and remains small.
