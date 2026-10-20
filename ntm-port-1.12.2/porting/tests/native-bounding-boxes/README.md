# Bounded native AABB construction after CP63

Exact production baseline: `0bbba4f47800cea8dd60370f697450597f27c8a6`.
This separate family changes only 20 factory selectors in 9 existing Java files. All six argument expressions remain byte-identical. No helper, platform shim, reflective/final-field mutation, source exclusion, runtime guard, geometry normalization policy or balance change is introduced. Main was never edited by this worker.

## Scope and arithmetic proof

The whole-source javac audit visits all 4,062 production Java inputs and records 372 calls named `getBoundingBox`. Exactly 346 receivers resolve to the genuine `net.minecraft.util.math.AxisAlignedBB` TypeElement, not a local/field/instance/domain lookalike. The 20 accepted calls are:

- All 12 steel-corner collision pieces across the four DecoBlock metadata branches. Each coordinate parameter is int and addition promotes it to double before adding the existing ordered dyadic literal offsets. Every int is exactly representable as double; the selected offsets at all int extrema are exact and finite. Integer overflow and floating endpoint reversal are therefore impossible.
- Meteorite's one search box. The int coordinates promote before +/-7.5, so the same ordered, finite, exact proof covers every int including MIN_VALUE and MAX_VALUE.
- Seven point boxes using identical primitive local expressions. Six use int coordinates, either directly or after the same +0.5 promotion. ExplosionNukeGeneric uses the same three primitive double locals twice without arithmetic. Identical local reads cannot change between operands, and no selected argument has calls, assignments, fields, arrays, volatile access or other side effects. Native Math.min/max of the same raw value preserves both endpoints, including signed zero and tested signed signaling/quiet NaN payloads.

The compiler identifies parameter/local symbols and primitive types. A structural AST proof accepts identical pure local expressions or same integral-local affine expressions with double arithmetic and ordered finite literal offsets. It rejects integer additions before widening, reversed ranges, field reads, method calls, unknown types, mutable/instance receivers and homonyms. No blind textual search/replace selects production calls. The source-edit step uses only the attributed spans and validates the exact selector and unchanged parenthesized argument substring.

352 calls stay byte-for-byte untouched: 26 non-native/domain/unknown receivers, 25 exact-native receivers with unresolved argument types, and 301 other exact-native calls not proven by this bounded arithmetic/local-stability contract. This includes intentionally reversed dungeon query endpoints, arbitrary block min/max fields, dynamic entity positions and int arithmetic with potential overflow. This is a concise classification, not a complete semantic audit of each deferred call. CP53's separate 74 literal-only conversions remain unchanged and are not counted here.

## Why the old factory is not a universal constructor alias

Actual original 1.7.10 `azt.a(DDDDDD)Lazt;` creates a box whose constructor stores the six supplied doubles directly. Actual native 1.12.2 `AxisAlignedBB(DDDDDD)V` uses Math.min/Math.max for each pair and its fields are final. The preserved bytecode dumps and binary hashes establish this with the actual engine classes. Reversed bounds, one-sided NaNs, two different NaN payloads, reversed +/-0.0, reversed infinities and int-overflow endpoints are observable differences. Seven runtime counterexamples must remain different; the probe does not normalize these cases or treat them as accepted production input.

Original factory and native constructor evaluate the unchanged six arguments left-to-right. Real original/native probes cover all six evaluation events and abrupt completion at operand 3. Selected operands themselves are side-effect-free primitive locals/arithmetic, so there is no user-code initialization or receiver evaluation to drop. As with ordinary new-expression migrations, allocation failure timing/OOME is not a gameplay-equivalence claim.

## Evidence and reproducibility

Run from the module or invoke by absolute path:

```
bash porting/tests/native-bounding-boxes/run.sh
python3 porting/tests/native-bounding-boxes/source.py --strict-all
python3 porting/tests/native-bounding-boxes/verify_attribution.py
python3 porting/tests/native-bounding-boxes/attribute.py
```

Defaults use retained Java8, `/tmp/nagaport-ae2-native-classpath.txt` and the actual original `/tmp/nagaport-legacy-client.jar`. Overrides: JAVA8_HOME, NATIVE_CP_FILE, LEGACY_CLIENT_JAR. No downloads. The legacy jar is SHA256 `a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`. Only unchanged original azt/azw/azu class bytes are temporarily extracted, avoiding signed default-package classloader conflicts; no fake old/native platform classes are generated. Runtime verifies the native AABB class SHA256 as well.

- `manifest.json` freezes all 4,062 baseline input hashes, nine before/after source hashes, exact expressions and offsets, variable types and per-axis proof categories. Every edited file reverses exactly to CP63. `--strict-all` also proves all 4,053 untouched files and production input membership unchanged. This intentionally isolated whole-file guard requires a combined inverse receipt after unrelated overlapping authorized edits on integrated main; it does not silently ignore them.
- `before.jsonl` and `after.jsonl` record actual full-source compiler attribution, not independent parsing of selected fragments. All 20 new expressions bind to the native six-double constructor. Counts are 15,952 before and 15,932 after javac errors. These are attribution runs on an unfinished port, not a successful production Gradle build; no assertion that unrelated diagnostic multisets are identical is made.
- `BoundProbe` is generated from the exact verified production expressions and complete DecoBlock four-branch switch. 1,632,429 actual old/native checks cover all 20 extracted expressions, all int edge-coordinate triples, deterministic random ints, raw same-local double extrema/subnormals/zeros/infinities/NaN payloads, random double bits, intersections, axis collision offsets and ordered switch results/cardinalities for valid and abnormal metadata.
- Four positive and fourteen negative compiler controls test genuine simple/FQ native owners, point and promoted-local expressions, overflow, reverse bounds, fields, floats, wrong arity, nonnumeric args, increments, method calls, instance/parameter shadow, domain factories, unknown owners, subclasses and nested homonyms.
- Eighteen in-memory source mutation controls reject selector/argument changes and unrelated file edits. Seven actual old/native semantic counterexamples reject an unsafe global replacement.

The source and expression/switch probes do not compile or run the nine complete gameplay classes, world queries, machines, entity lifecycle, full collision callback, renderer, mod startup, client/server, saves or actual game. Surrounding obsolete `expand`, `intersectsWith` and other APIs are not changed or certified. No full compile success, runnable mod jar or downstream acceptance gate is claimed. CP37+ withholding and all unrelated lighting/HUD/attribute decisions remain outside this patch.
