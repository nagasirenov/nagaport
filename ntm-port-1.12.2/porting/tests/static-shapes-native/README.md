# Four constructor-constant native block shapes

Baseline: CP82 `df18fc077648dd18e6d0c1055d82c11dc2ebdb78`. No shared legacy Block shim, source exclusion, dependency update, main-worktree edit, network action, or push.

## Production scope

- `BlockCrop`: original `(0,0,0)-(1,.25,1)` selection/ray shape; native BlockBush's genuine null collision is retained. This owner serves the five crop registrations, not native vanilla crop growth/shape assumptions.
- `BlockMush`: exact original float expressions with `f = 0.2F`, including float rounding before double widening. Its explicit null collision moves to the native signature.
- `BlockFallout`: original 1/8-block selection/collision/ray shape for regular and salted fallout. The original constructor identity comparison and tick behavior remain unchanged.
- `MeltedFlesh`: original 1/8-block selection/collision/ray shape for fresh, charred and carbonized flesh.

Each constructor stores the same six evaluated arguments in a private final native AABB. The native `getBoundingBox` returns that local box; native Block supplies collision and world offsets for the two solid owners. No callback performs a second offset. The existing native BlockBush null collision and the mushroom's null callback prevent introducing plant collision. No field is global or mutable. No state codec, growth, radiation, temperature, support, drop, tile, texture, model, render callback, or registration is changed.

All four own one constructor bounds assignment and no state/item bounds setter, custom ray method, selection override or collision-list override. The parse-only 4,065-source census has no descendants of these four classes. Bounded javac attribution against complete selected owners confirms their actual native Block/BlockBush ancestors, four real six-double AABB constructor destinations and five actual native overrides. Their unrelated unresolved production dependencies remain errors, not hidden stubs. `sites.json` reconstructs all four exact CP82 files and independently matches the original pristine constructor geometry. Twelve source drift controls reject changes.

## Original callback and native engine audit

`engine.py` checks 21 methods from the genuine original signed 1.7.10 client and native 1.12.2 engine, original MCP mappings, and Forge 10.13.4.1614 Block/BlockBush patches. Original setBlockBounds widens six already-evaluated floats to stored doubles. Original Block state/item bounds hooks are no-ops. Selection and collision read those six fields. Collision-list construction calls the virtual collision method, and ray tracing invokes the no-op state setter before testing the stored shape. Original BlockBush returns null collision.

Native selection, collision and ray callbacks delegate through state to getBoundingBox, with offsets owned by the native consumers. Native BlockBush still returns NULL_AABB. The AABB constructor normalizes min/max unlike the old raw constructor; all selected inputs are finite and ordered, so no inversion, NaN or infinity semantics are changed. Those cases are explicitly excluded.

## Runtime evidence and exact boundaries

The runner compiles the complete production constructor and selected geometry/opacity/fullcube methods for each actual named owner against the genuine native ancestor, omitting unrelated unported methods/interfaces. Only the ModBlocks salted-fallout identity and CBT_Atmosphere type are controlled NTM dependencies. This is an explicit projection, NOT successful complete-owner compilation. Original fixtures execute the exact pristine constructor geometry statements against actual original Block/BlockBush; no fake Minecraft/Forge implementation supplies the oracle.

15,666 assertions pass: actual native World/Chunk storage, state dispatch, positive/negative/large positions, all metadata inputs to the existing decoder, direct and real World collision-list enumeration, touching/overlap/miss queries, six-face and 300 seeded rays per owner/position, direct callback edge/corner/zero-length rays, null-collision filtering, unchanged constructor ticks, immutable box reuse and coordinate isolation. Original/native direct callback geometry and hit points are bit-exact. World traversal uses native DDA clipping; hit/null and face/owner are exact, while its points are compared with 32 ULPs of the largest world coordinate plus 1e-12 for cross-axis floating-point propagation. This is not a claim of bit-identical original/native World traversal internals. Endpoint-only corner rays are compared at the actual old/native block callbacks, not conflated with world traversal's cell-ownership rule.

Five runtime mutants all fail: full-cube fallback, lost mushroom null collision, doubled world offset, double rather than original float evaluation, and restored old-style non-dispatching shape signature. Mutation runs still compile, then fail real native runtime assertions.

The complete four-owner bounded javac probe remains failed because unrelated NTM dependencies/callbacks are unported. Its diagnostic difference is recorded separately; it is not a full-mod error-count claim. No full-mod build, gameplay, actual client/server, save/load, plant growth, radiation, temperature or render acceptance is certified.

## Rendering and exclusions

Existing visual ownership is untouched. Crop/mushroom positive legacy render callbacks and icon paths remain independently unported; fallout/flesh old texture bindings are not certified by shape tests. No native model or fallback cube is added. Original Block item/state bounds no-ops and the absence of external NTM setBlockBounds calls establish this finite family, not arbitrary cross-mod mutation compatibility. Third-party subclasses or code that mutated old public global bounds are outside the proof.

Bobble, Snowglobe, Loot, DecoBlockAlt, BombMulti and MachineSatDock look constant but set boxes only during state/collision callbacks; direct original selection can retain pre-callback bounds. They are intentionally excluded rather than silently normalizing that legacy mutable-state behavior. Dynamic rails, gates, doors, waypoints, connector shapes, random Digamma bounds, and existing BlockDummyable/owned bridges are also excluded. No inherited fence/pane behavior is assumed.

## Reproduction

From this directory, using the already available Java 8 and native dependency cache:

    python3 verify.py
    python3 run.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt --legacy-jar /tmp/nagaport-legacy-client.jar --out /tmp/static-shapes-fresh
    python3 engine.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt --legacy-jar /tmp/nagaport-legacy-client.jar --legacy-srg /tmp/nagaport1710-srg.zip --legacy-forge /tmp/nagaport-forge1614-userdev.jar

Add `--mutant fullcube`, `null-collision`, `offset-twice`, `float-rounding` or `old-signature` to run.py with a separate fresh output directory; each must exit nonzero with the expected geometry assertion, not a compile failure.

For bounded attribution, compile Attribute.java with `$JAVA_HOME/lib/tools.jar` on javac's classpath; run it with the module root, native classpath file, and a nonexistent sourcepath directory. It parses the full source census but analyzes only the four selected complete owners. No complete-mod compilation is performed.

All dependency paths are inputs, not redistributed artifacts. The original client SHA-256 is `a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`; further original engine hashes and checked method bytecode are in proof/engine.json. Windows execution is untested.
