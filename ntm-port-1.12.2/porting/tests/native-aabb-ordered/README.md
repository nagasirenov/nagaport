# CP84 bounded ordered AABB constructors

Baseline: CP83 `bdb4c897` (the complete commit ID is in manifest.json).
This patch changes only 24 genuine `AxisAlignedBB.getBoundingBox` selectors in 15 production files to the native six-double constructor. Every argument expression, cast, numeric literal, read and its evaluation order remains byte-identical. No new production helper, global legacy shim, reflective/final-field mutation, normalization guard or API exclusion is introduced.

## Why this subset, not a global replacement

Actual original 1.7.10 `azt.a(DDDDDD)Lazt;` invokes a constructor that stores all six doubles verbatim. Actual native 1.12.2 `AxisAlignedBB(DDDDDD)V` applies `Math.min` and `Math.max` to each endpoint pair. Inversion, integer overflow, one-sided NaNs, differing NaN payloads, reversed infinities and reversed signed zeros can therefore change results. These differences are explicit negative controls, not normalized away.

The full-source compiler scan identifies 351 calls with this method spelling: 325 genuine native TypeElement receivers and 26 other/unresolved owners. It accepts exactly:

- Seven DoorDecl boxes whose only variable operand is a boolean-local/parameter ternary between finite ordered literals. Both flag values are executed. All endpoint zeros here are positive zero.
- Seventeen native TileEntity position boxes. Javac resolves `this.pos` to the native nonvolatile `TileEntity.pos : BlockPos` field and each getter to native `Vec3i.getX/getY/getZ : int`. Arithmetic promotes to double before any addition/subtraction. The selected constants are exact small dyadic values, so every int coordinate, including both extrema, plus these offsets is finite, exact and ordered. No integer addition before widening is selected.

The coordinate family is explicitly limited to ordinary same-thread native TileEntity position ownership. Actual native `TileEntity.setPos` calls `BlockPos.toImmutable`; native immutable `Vec3i` coordinates are private final ints and the actual getter bytecode simply reads them. `MutableBlockPos.toImmutable` snapshots into a plain BlockPos; the runtime probe verifies that mutating the original does not change this snapshot. The full-source typed write/override audit finds zero assignments to native TileEntity.pos and zero overrides of native setPos in this baseline (a same-spelling RequestNetwork inner-record field is unrelated). The six selected operands perform only these native getter reads and primitive arithmetic, not arbitrary user callbacks.

This is not a claim about foreign subclasses overriding getters/toImmutable, unsupported direct replacement with a foreign mutable position, or concurrent position mutation. The patch does not capture or widen scope to such values. Arbitrary entity double fields, radius variables, legacy block min/max fields, int-overflow-prone bounds, intentionally inverted dungeon/door queries, and unknown receivers remain unchanged. Exactly 327 scanned calls are deferred (301 native and 26 other/unresolved), and proof/before.jsonl records their exact expressions and per-axis rejection.

## Evidence

- `manifest.json` freezes all 4,065 baseline production-input hashes, all 15 before/after file hashes, all 24 original/replacement spans, argument types and structural proofs. `source.py` validates exact inverses and forward round trips; `--strict-all` also checks all untouched inputs and membership. Thirty in-memory mutations are rejected. The manifest is isolated-branch evidence: overlapping authorized integrations need a combined inverse, not relaxed hash guards.
- `OrderedAabbScanner.java` uses real javac symbols and ASTs, not name-based replacement or regex selection. Exact native class ownership rejects variable/instance/domain/nested homonyms. Conditional and coordinate argument proofs reject unknown types, side effects, signed-zero literals, reversed or dynamic bounds, int-overflow arithmetic, non-dyadic affine constants and foreign/shadowed position fields. Four positive and fourteen negative compiler controls exercise these boundaries.
- `proof/before.jsonl` / `after.jsonl` prove all 24 replacement sites bind to actual native `(DDDDDD)V`. Compressed diagnostic records and `verify_attribution.py` show 12,169 to 12,145 javac attribution errors: exactly the 24 selected missing-factory diagnostics disappear; zero appear; all unrelated path/line/code/message multiplicities are identical. This is read-only all-source attribution, not bytecode emission or a successful Gradle/full-mod build.
- `run.sh` generates tests directly from all 24 exact source expressions. It passes 1,525,082 actual original/native assertions over both door states, 729 edge-coordinate triples plus 256 seeded random full-int triples, raw coordinate bits, finiteness/order, touching/inside/outside intersections and X/Y/Z collision offsets. Seven unsafe-global-rewrite counterexamples must stay observably different. Additional probes establish left-to-right operand order and abrupt completion; allocation-failure/OOME timing is not a gameplay-parity claim.
- `engine.py` reads the real original/native classes, checks retained hashes, and records only relevant disassembly and hashes in proof/. It establishes raw versus normalizing construction and native position ownership behavior. No proprietary binaries or engine stubs are shipped. The runtime test temporarily extracts unchanged original azt/azw/azu bytes solely to avoid signed default-package loading conflicts and removes them afterward.

The runtime scope is extracted geometry expressions with real native BlockPos getters and real old/new bounding-box implementations, not full TileEntity methods, DoorDecl callbacks, world/entity searches, rendering, machine lifecycle, saves, startup, or in-game play. Surrounding legacy `expand` and other unrelated API work is untouched and not certified.

## Reproduce without downloads

From the module:

- `bash porting/tests/native-aabb-ordered/run.sh`
- `python3 porting/tests/native-aabb-ordered/source.py --strict-all`
- `python3 porting/tests/native-aabb-ordered/verify_attribution.py`
- `python3 porting/tests/native-aabb-ordered/attribute.py` to regenerate both original-inverse and current all-source compiler attribution views without emitting gameplay classes.

Defaults use `/workspace/shared/nagaport-toolchain/java8`, `/tmp/nagaport-ae2-native-classpath.txt`, and `/tmp/nagaport-legacy-client.jar`. Override with JAVA8_HOME, NATIVE_CP_FILE and LEGACY_CLIENT_JAR as needed. No network or Gradle invocation is needed. No full-build, runnable mod or downstream acceptance gate is claimed.
