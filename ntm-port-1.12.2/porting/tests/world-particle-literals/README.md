# Native World particle literal calls

Finite root: CP79 `77b595c32b46e2f9309bcf9924d654ddc29a3c95`. Exactly 115 compiler-attributed `World.spawnParticle(String, ...)` calls in 67 production files now supply fully qualified native `EnumParticleTypes` constants. `scope.json` records every original line, replacement line and source position; `cp79-attribution.log` preserves the original compiler's actual World overload attribution. Fully qualified constants avoid import changes and simplify composition with other source migrations.

All 16 selected original names resolve to native 1.12 enum entries with zero extra arguments. No helper or legacy string API is introduced. Receivers, side gates, settings, loops, conditionals, callback counts, coordinates, velocities, argument evaluation order and formulas are byte-identical outside the replaced name tokens. Native particle construction, motion details, manager admission and range/quality policy remain vanilla's responsibility under the user's simpler-visuals permission. No gameplay behavior or model rendering is changed.

Dynamic names, block/item suffix arguments, calls without genuine compiler World attribution, and unrelated remaining compilation failures are intentionally deferred. This is not a claim that every sourcewide literal hit or all particle migrations are complete.

## Evidence

Run from any directory:

    python3 porting/tests/world-particle-literals/verify.py --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" --java-home /workspace/shared/nagaport-toolchain/java8

`verify.py` checks an exact full-byte inverse against the frozen CP79 git objects, then compiles and executes a Java 8 native probe. The 16 name/enum mappings, zero-argument signatures, IDs, range flags, all six numeric arguments, empty varargs and single-event dispatch run through real inherited `World.spawnParticle`, not an overridden fake method. The test allocates real `WorldServer` without its unrelated save/server constructor and initializes only the event listener and random fields needed by these real methods. An `IWorldEventListener` proxy captures the legitimate dispatch endpoint.

The complete production `ExplosionEffectStandard.performClient` method is extracted unchanged and compiled against native classes. Runtime checks cover central large/huge threshold, empty/filled affected-block lists, one plus two-per-block event count, event ordering, positions/velocities and representative seeded calculation/evaluation preservation. This is bounded semantic evidence, not a visual/RNG micro-parity project. The test does not compile all 67 complete owners or claim live rendering. Existing native particle probes cover manager/render behavior separately and are unchanged.

Result: exact inverse passed; 282 native checks passed (`result.log`). Two expected javac warnings disclose the test-only Unsafe allocation.

## Composition

This patch is based on CP79, intended for the checkpoint after CP80. `ModEventHandler` has a single selected name token at original line 1489; it is independent of the coordinator's attribute-name changes. Apply production changes as the exact token replacements in `scope.json`, preserving unrelated new main changes. The full inverse deliberately stays frozen to CP79 and must be run in this isolated worktree; a merged tree with other authorized edits will not satisfy that historical full-file check. This does not authorize overwriting those edits. No Gradle build was run here; coordinator owns whole-tree compilation.
