# Native HFR VBO geometry and payload acceptance

Bounded original-based migration from checkpoint 7 (`a60fe7806f90257b5dbfbe9a62f46b887946bf93`), 2026-10-19. The production VBO remains `HFRWavefrontObjectVBO`, now implementing the mod-owned `MeshModel` contract and consuming actual `ObjMesh.Part`/`Face`/`Corner` geometry. No CE/EE source, fake Minecraft/Forge types, legacy-interface shim, source exclusion, no-op conversion, packed-normal substitution or GL test backend was introduced.

## Production contract

- `ObjMeshVboPayload.pack` is the actual production packing path called once per source part, immediately before that part's uploads. It creates three distinct, direct, native-order float buffers: XYZ positions, UVW coordinates and source normal XYZ. Buffer positions are zero and limits are the populated float counts after flipping.
- Source ordering and all float bits survive, including non-unit normals and signed zero. UV V already has the original loader's `1 - v` flip; packing adds no inset, normalization, atlas transform or clamp. W is retained. Missing UVs are `(0, 0, 0)`, not current-attribute carry. Captured CPU smoothing does not influence VBO normals.
- The historical triangle-only capacity remains `faces.size() * 3 * 3` floats per stream. Quads still overflow; absent source normals still fail with `NullPointerException`, rather than being silently synthesized. Empty named groups still yield zero-size payloads and retain their original real GL allocation/upload path.
- The actual GL15 backend is retained. Each group generates/uploads/unbinds position, then UV, then normal buffers. Drawing binds each and sets the original float pointers, enables the same three client arrays, draws `GL_TRIANGLES`, disables the arrays in the same order and unbinds. Destruction deletes position, UV and normal handles in source-group order, then clears the groups. No new early return, deferred upload, error suppression or automatic destruction was added.
- `renderAll`, `renderPart`, `renderOnly`, `renderAllExcept` and `getPartNames` retain their original implementations. Source groups are outermost and requested names innermost; comparisons are case-insensitive. Repeated requested names multiply draws, and duplicate source names remain separate. Exclusion does not multiply anything.
- `getMesh()` publishes the captured source mesh only after a successful `load` and does not track later CPU reparses. `destroy()` empties it after clearing the buffers. The existing reloader must still destroy before loading. Public `load` itself retains its original append-to-existing-buffer-list behavior; its geometry snapshot describes its latest completed load, not an inferred concatenation. GL exceptions retain the old partial-upload/partial-destruction behavior, not a new atomic or retain-last-good GPU contract.

The pure helper is not an alternate renderer. Its buffers are the ones handed directly to the preserved `glBufferData` calls; the complete VBO class compiles against actual LWJGL and native Minecraft/Forge dependencies.

## Independent evidence

`vbo-mesh-baseline.json` records the checkpoint's VBO source hash, hashes of the original upload block and complete lifecycle/selection method bodies, 293 source-file/resource pairs and SHA-256 hashes of all 293 assets. `probe-vbo-mesh.py` strips comments without deleting string literals and traces balanced constructor/method-call expressions rather than assuming one constructor per line. It requires every active `.asVBO()` call to resolve to one explicit HFR constructor/resource, then compares the current inventory with the checkpoint manifest. Portable verification needs no local historical Git object. An optional provenance check independently re-derives the checkpoint inventory from Git. Source-path metadata and hashes stay unchanged when the coordinated resource-case migration moves mixed-case filenames to their native lowercase names. The Java oracle resolves each path through the actual target `ResourceLocation`; the Python audit applies the same lowercase normalization to the verified ASCII manifest paths. Neither performs a case-insensitive filesystem search or fallback. See `resource-case-migration.json` for the unchanged-byte renames.

The source audit verifies that the entire original allocation/upload block and the complete draw, delete and selection control flow are unchanged, allowing only the new destroyed-snapshot reset in `destroy`. It also verifies that the buffers passed to the actual upload calls are the production helper's buffers, with count from the same helper. A source audit is not a GPU execution test.

`VboMeshNativeTest` compiles and runs the actual mesh/parser/payload, CPU owner and VBO classes. For each asset, a separate raw-text oracle parses original float records and face indices without using production geometry or packing results to compute expected values. It compares every emitted position, UVW and normal component bit-for-bit, plus exact source-group order/names, faces, vertex counts, direct buffers, native byte order, capacities, limits and flipped positions.

Verified actual assets:

- 293 unchanged resources, 1,161 source groups, 367,607 triangles and 1,102,821 source corners.
- All faces are triangles and every corner has a source normal.
- The exact six resources with missing UVs are `machines/liquefactor.obj`, `machines/solidifier.obj`, `machines/chemical_factory.obj`, `weapons/detonator_laser.obj`, `armor/AJR.obj` and `armor/no9.obj` (under `models/`). Their missing UV payloads are zero-filled, while their normal values remain intact.
- Synthetic cases add nonzero positive/negative UV W, wrapping UVs, non-unit/high-precision/signed-zero normals, duplicate case-insensitive source names and requested selectors, empty groups, no matches, zero selectors, quad overflow and both kinds of missing-normal faces. Duplicate-selection behavior is checked in the native mesh selector; the actual VBO selector bodies are additionally hash-verified against the original, not executed through a fake GL backend.
- An explicit empty resource fixture implements the real native `IResourceManager`/`IResource` interfaces. Actual HFR construction, actual `asVBO()` registration, successful empty snapshot capture, independence from a later nonempty CPU reparse, empty selection methods and destroy/load transitions are exercised. No nonempty VBO upload is attempted. This fixture is not a replacement Minecraft/Forge class and does not establish GPU lifetime acceptance.

Initial isolated result: **PASS, 11,417,209 assertions**, including all 293 source-derived assets. The complete production HFR/VBO and native core compiled without extracting class bodies or substituting platform APIs. The common coordinated build result is recorded by the parent checkpoint, not assumed here.

## Reproduce

```sh
python3 porting/tests/probe-vbo-mesh.py --audit-only
# Optional local-history provenance check; not required by a patch recipient:
python3 porting/tests/probe-vbo-mesh.py --audit-only --check-baseline-revision a60fe78
/workspace/shared/nagaport-toolchain/run-gradle.sh --offline \
  --init-script porting/vbo-mesh-probe.init.gradle testPortVboMesh --console=plain
```

The init script supplies the actual production compile classpath and Java home. A direct isolated run can supply the same values using `--java-home` and `--classpath`; it builds only into a temporary directory and does not touch common Gradle outputs.

## Explicit limits

No live GL context, GPU allocation/upload/draw/delete, resource-reload GPU ordering, render-thread scheduling, client visuals, full consumer compilation, dedicated-server run or runnable mod is established by these tests. Headless LWJGL-native unavailability is not bypassed using fake GL classes. Existing production errors elsewhere remain the whole-mod migration's responsibility. Real client acceptance must still cover initial eager uploads, duplicate selection, rendering and resource reload/destruction with the preserved GL backend.
