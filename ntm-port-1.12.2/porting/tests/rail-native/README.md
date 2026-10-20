# Native original rail / funnel family

Base: CP49 `caf22cf027f33f9d0d0be366454c2f54fdba7915`. Work is isolated from the coordinator's main worktree.

## Finite scope

The original `RenderRail` handler owns eleven custom multiblock rails and `MachineFunnel`, which shares `IRenderBlock` despite not being a rail. All twelve are migrated together. The four separate vanilla minecart rails (`rail_wood`, `rail_narrow`, `rail_highspeed`, `rail_booster`) are not consumers of this handler and are unchanged. Their speed/booster semantics are not replaced or certified here.

- Native metadata state and existing native `BlockDummyable` placement, dummy-chain, removal and collision lifecycle remain in use.
- Rail owner world access, AABBs, switch interaction and tile update tag use real 1.12 APIs. `IRenderBlock` is a finite mod-owned marker, not an old renderer facade.
- `RailModelHandler` binds all twelve real owners, 177 world states (11×16 plus the metadata-insensitive funnel), and 192 ItemBlock metadata routes. Twelve exact-case blockstate JSON files route to the native custom loader.
- The loader reads the original twelve OBJ files and eight textures. All twenty asset bytes are unchanged, source-order triangles are retained, no UV contraction or automatic triangulation is introduced.
- Switch models preserve no-sign-without-the-correct-tile, straight/turn signs, all sixteen metadata values, and original inventory-only `Rail` selection.
- Shared files have only thirteen unique, byte-preserving fragments: twelve `ModBlocks` construction lines and the one retired handler registration. No unrelated declarations, imports, registration lists or line endings are modified.

One existing mechanical-port bug is corrected to preserve the original: 1.7 `Vec3.subtract` is argument-minus-receiver; 1.12 `Vec3d.subtract` reverses that contract. The two waypoint calls now use `subtractReverse`. The independent original-method oracle detects reverting this correction. Original quirks such as the buffer's Z-path comparison against `vec.x`, ignored `normalize()` return in waypoint traversal, and flipped-switch requirement/fill differences are deliberately retained, not balanced or redesigned.

## Reproduction

From the module root, using the existing Java 8/native classpath:

    python porting/tests/rail-native/check-source.py
    python porting/tests/rail-native/run-native.py --original-root /workspace/shared/nagaport/ntm-original-pristine --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt --output /tmp/rail-native-reproduction
    python porting/tests/rail-native/oracle-run.py --original-root /workspace/shared/nagaport/ntm-original-pristine --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt

`run-native.py` compiles and invokes the exact twelve `ModBlocks` construction statements and compiles the complete actual rail owners, funnel owner, existing `BlockDummyable`/multiblock helper closure, native model handler, geometry and model classes. It prints each production source hash and each controlled NTM collaborator. No Minecraft/Forge class is stubbed or redefined; no full Gradle build is run.

Controlled NTM boundaries: unrelated global registry/GUI identity, train-item identity, directional constants, debug particle submission, translation lookup, external-core resolver, and the not-yet-native funnel processing tile. Production `EntityRailCarBase.generateYaw` and `BobMathUtil.angularDifference` are extracted as exact algorithms with only the required native math API spelling. This does not certify the full train entity or funnel crafting/processing tile.

`build-routing-oracle.py` separately extracts all relevant routing/placement methods and waypoint constructors from pinned pristine source. Its independent mutable vector retains original subtraction, normalization and rotation contracts. Native owner results are checked against those methods, not against port-derived expected values.

## Results

- 235,183 native owner/routing/placement/collision/lifecycle assertions. Includes all eleven owners, four orientations, absent/straight/turned switch tile contexts, forward/reverse/zero/overshooting motion, exact ordered multiblock occupancy, every dummy's core resolution, occupied placement requirements, five-step ramp collision, all metadata values, both interaction hands, train-tool pass-through, switch save/update/initial-chunk NBT and funnel item/NBT conservation on native break.
- 1,038,370 actual Forge model registration/state mapper/JSON/custom-loader/ItemModelMesher and native flat-light assertions. Folder and jar resources, all twelve changed/restored/missing mesh reloads, retained-list immutability, original own-block packed light and precomputed shade through both Forge `VertexLighterFlat` and vanilla flat renderer.
- Independent geometry oracle: 1,536 owner/metadata/world-item/tile states, 1,703,808 vertices and 16,328,193 assertions. World and item XYZ, UV and packed colors are bit-exact to extracted original render methods. See `oracle-readme.md` for exact evidence boundaries.
- Two intentional behavior mutations and eight independent geometry/material/shader/normal mutations are rejected.
- Source guard: thirty owned production files, thirteen exact shared fragments, ninety-nine rejected source mutations. Shared unrelated sibling edits are deliberately allowed; owned sources are fully frozen. `reverse_source(relative, text)` must be composed before earlier continuation guards when auditing the combined tree.

## Explicit remaining gates

These are CPU-headless native probes, not a launched Minecraft game or a whole-mod compile. Full client/server startup, real train movement, chunk boundaries, switch interaction over a real connection, shader/OptiFine paths, real item camera appearance and in-game visual acceptance remain integration gates.

The original inventory GL rotations acted after the original signed-byte normal was decoded. Native baked item normals must be re-encoded after rotation; the independent oracle observes at most one signed-byte step difference (maximum 0.0078740296) for those inventory normals. Positions, UVs and colors are exact. This is documented, not silently treated as visual parity or waived by the CPU pass.

CP37 visual/lighting approval remains pending. This work neither changes a global lighting policy nor grants that approval. No CE sources, balancing changes, exclusions, broad old-Forge shims, platform facades, downloads or pushes are introduced.

## Freeze artifacts

`production-files.txt`, `production.sha256`, `source-manifest.json`, `source-proof.log`, `routing-original-manifest.json`, `oracle-original-manifest.json`, `native-proof.log`, `oracle-proof.log`, and the independent oracle mutation evidence identify the finite deliverable and its tested inputs.
