# CP95 finite-local crane query boxes

Baseline: `58f7f0c93cb4f5814d552e003ec9c7e2179a115f` (sealed CP95).
Exactly two production selectors change, in `TileEntityCraneGrabber.update`, from `AxisAlignedBB.getBoundingBox` to `new AxisAlignedBB`. Every argument, initializer, conditional, statement and evaluation order is unchanged. No helper, shim, normalization, scope exclusion or gameplay adjustment is added. The prior CP53/63/84 AABB families remain unchanged.

## Bounded proof

The actual original 1.7.10 factory stores all six doubles verbatim; the native 1.12.2 six-double constructor normalizes endpoint pairs with `Math.min`/`Math.max`. Therefore remaining arbitrary factory calls cannot be blindly replaced.

The full-source javac scan accepts only these two calls, using exact receiver and local-variable symbols:

- Receiver is the native `net.minecraft.util.math.AxisAlignedBB` TypeElement, not an instance, field, parameter or domain homonym.
- Each axis has the same primitive double local on both sides, with the unchanged exact additions `+ 0.1875D` and `+ 0.8125D`.
- Both lexical groups of `x`, `y`, `z`, and `reach` are audited separately by symbol identity. Every definition of each local is examined across the containing method. Compound writes, increment/decrement, circular dependencies, unsupported initializers and non-finite/unbounded values are rejected.
- Both reach groups have exactly `1D`, `0.5D`, `0.33D` definitions. The original coordinate initializers are native int position getters plus int direction offsets multiplied by reach. Even permitting the full int range for every coordinate and every direction offset proves each resulting double is between -4,294,967,296 and 4,294,967,294. This does not depend on position mutability or on constraining the direction enum to unit vectors: the six factory operands read already-computed primitive locals only.
- Within those bounds, both endpoint additions are finite and monotone, with spacing far smaller than their exact 0.625 difference. Each lower endpoint is strictly less than its upper endpoint. NaNs, infinities and reversed signed-zero pairs are impossible for this subset. Reach `0.33D` need not be exactly decimal 0.33: its actual finite binary double and all existing rounding are retained.

The integral reads and arithmetic in the initializers execute before the selected call and remain unchanged. An integral initializer can overflow yet still produce a finite bounded int; that is distinct from unsafe integer arithmetic between lower/upper factory arguments. Those remaining overflow-prone factories are excluded. All selected argument reads are pure primitive local reads, so null/other exceptions in coordinate initialization and all subsequent user code stay in the same place. Allocation-failure/OOME timing is not a gameplay parity claim.

No BlockDoorGeneric normalization boxes, other crane logic, other world queries, inventory dispatch, or sound code is changed.

## Evidence

- `manifest.json` pins all 4,071 baseline Java inputs, the complete before/after edited file, exact edits, types, per-axis bounds and symbol-resolved dependency evidence. `source.py --strict-all` proves the whole-file inverse/forward round trip and every untouched source; two in-memory mutations are rejected. Integrated overlapping edits require the parent's combined inverse boundary, not relaxed hashes.
- `FiniteAabbScanner.java` analyzes genuine production source and native dependencies. `proof/before.jsonl` includes the complete call inventory, including exclusions; `after.jsonl` resolves both replacements to actual native `(DDDDDD)V`. The before/after diagnostic multiset is exactly 10,014 to 10,012: two selected missing factories removed, zero added, all unrelated line/code/message multiplicities identical. This is attribution without class emission, not a full Gradle build.
- `run.sh` passes 3,764,682 assertions with actual original and native AABB implementations. Both exact source-extracted coordinate initializers and box expressions run for 985 edge/random int coordinate triples, all seven values of the actual checked-in ForgeDirection class, and all three reach values. Raw endpoint bits, finiteness/order, intersections and X/Y/Z collision offsets agree. Unsafe global-rewrite controls include inverted values, NaN payloads, infinities, signed zeros, int overflow, and mixed int/float rounding. Left-to-right operand order and abrupt completion are also checked.
- Two positive and thirteen negative typed controls cover scoped local identity, all-write finite flow, native versus instance/domain receiver, unknown parameter/call/field values, unsupported writes, excessive magnitude, reverse offsets and integer-overflow bounds.
- `engine.py` reads and verifies real retained original/native AABB bytecode and hashes. Unchanged original azt/azw/azu bytes are temporarily extracted solely to avoid signed default-package loading conflicts and are removed afterward. No engine binaries or platform stubs are included. The actual checked-in ForgeDirection source is hash-pinned and compiled in the isolated runtime directory.

Runtime coverage is exact extracted coordinate/box geometry, not complete crane update, inventory transfer, entity queries, world state, save/network behavior, client/server startup or in-game acceptance. No successful full build, playable mod or downstream gate is claimed.

## Reproduce offline

From the module:

- `bash porting/tests/native-aabb-finite-locals/run.sh`
- `python3 porting/tests/native-aabb-finite-locals/source.py --strict-all`
- `python3 porting/tests/native-aabb-finite-locals/verify_attribution.py`
- `python3 porting/tests/native-aabb-finite-locals/attribute.py` regenerates both read-only attribution views.

Defaults use the retained Java8 installation, `/tmp/nagaport-ae2-native-classpath.txt` and `/tmp/nagaport-legacy-client.jar`; `JAVA8_HOME`, `NATIVE_CP_FILE` and `LEGACY_CLIENT_JAR` can override them. No download, dependency regeneration, Gradle invocation, main edit or push is performed.
