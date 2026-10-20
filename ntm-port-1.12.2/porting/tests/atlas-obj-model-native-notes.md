# Native Atlas OBJ family proof

`porting/native-atlas-obj-model-test.log` records a passing Java 8 native Forge 1.12.2 run: **3,266,948 assertions and 722,136 complete native vertices**, plus **7,776 exact double comparisons against the cached original 1.7.10 Vec3 runtime**. No production sources or source assets were edited by this test worker.

## Reproduce

```sh
python porting/tests/atlas-obj-model-native-probe.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat /workspace/shared/nagaport-tick-preparation/classpath)" \
  --legacy-client /tmp/nagaport-legacy-client.jar \
  > porting/native-atlas-obj-model-test.log 2>&1
```

The optional legacy-client argument accepts only the pinned cached original client SHA-256 `a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`; it never downloads anything. Its obfuscated Vec3 executes in a separate JVM from the native model run. The regular probe needs only Java 8, the already-cached native classpath, and the pristine sibling checkout (or `--original-root`). It builds in a temporary directory and finishes in seconds.

## Independence and scope

- The manifest pins 87 pristine source/asset files and requires all 72 used HBM OBJ/texture assets to remain byte-identical in the native tree. It records the 45 original owner registrations and textures from pristine ModBlocks: 24 decorative pipe variants, six capacitors, six fluid barrels, seven Brown/Yellow/RedBarrel material variants, HEV battery, and antenna top.
- Original BrownBarrel, YellowBarrel, and RedBarrel source explicitly proves their shared BlockFluidBarrel render-ID route; all seven additional registrations and textures participate in the same complete route/vertex checks.
- The five original renderer source files provide verbatim world orientation/selection statements; inventory part/material order is independently checked against the original inventory methods. Original GL inventory translations/scales are checked, then expressed in the native centered block-item coordinate system.
- Both distinct ObjUtil shading blocks are extracted from pinned pristine source, not copied from production helpers. The independent OBJ parser retains ordered groups, duplicate names, float-coordinate subtraction, original normal normalization, and flipped V coordinates. It does not call the production parser to create expected geometry.
- All eight original meshes are fully covered: pipe (32 triangles), pipe_rim (80), pipe_quad (128), pipe_frame (64), capacitor (24), barrel (12), battery (88), and antenna_top (70). Their original 498 faces are all triangles; the separate controlled fixture exercises valid quad replacements.
- The probe compiles complete production RenderPipe, RenderCapacitor, RenderBarrel, RenderBattery, RenderAntennaTop, AtlasObjGeometry, AtlasObjBakedModel, AtlasObjBlockModelHandler, ConnectionQuadFactory, BlockPositionLightQuad, ThermalModelTransforms, ObjMesh, ObjMeshParser, and RefStrings. Only the eight NTM owner-block collaborators are controlled to provide metadata/material/shape identity. Every Minecraft/Forge class is real. Actual complete owner blocks and gameplay behavior belong to the separate owner probe.

## Verified outcomes

- For every 45 owners × 16 persisted metadata values, both directory and packaged-jar runs resolve the actual native block-state mapper, parse the shipped blockstate JSON, invoke ModelLoaderRegistry, bake all exact original material dependencies, and compare every emitted XYZ/UV/color/normal value with the independent pristine oracle. The 720 registered ItemBlock variants per run are routed through the actual native ItemModelMesher.
- The oracle checks bit-exact final vertex positions and uncontracted atlas UVs, original source group/material order, float casts before inventory scaling, white inventory color, separate whole/part world brightness, saturation/truncation, and signed-byte item normals. Geometry is immutable, seed-independent, non-AO, 3D, untinted, and emitted only into the unculled general bucket. Original particle materials and native GUI scaling are checked.
- Representative meshes/orientations, including all capacitor direction cases and decorative-pipe metadata 4/8, pass both actual vanilla BlockModelRenderer flat rendering and Forge VertexLighterFlat. Emitted vertices retain original pre-shading without a second diffuse factor and use the owner block's packed light rather than neighboring block light.
- All eight mesh resources are shifted, reloaded, compared with a separately reparsed modified oracle, then restored. Previously baked lists remain unchanged. Removing each of the eight resources produces an explicit error naming that exact OBJ rather than silently succeeding with a missing-model placeholder. Both resource-pack forms exercise this sequence.
- A small valid quad fixture spans five source groups, including duplicate `Top` and distinct lowercase `top`. It proves last exact-case part lookup, no geometry for missing groups, part four-vertex emission, the original whole-model five-vertex-per-face stream across group boundaries, complete GL_QUADS batches with final remainder discarded, and per-vertex shading/normals on cross-face quads.
- The isolated original-client probe confirms original Z-then-Y Vec3 rotation signs and precision for canonical and noncanonical angles. It caught an initial candidate's incorrect double-trigonometry substitution; production now uses the original float lookup-table results promoted to doubles. The passing log contains hashes of the exact corrected production sources compiled.

## Boundaries

This is an economical CPU/headless native rendering and resource-lifecycle proof, not live graphical acceptance or a claim that the whole mod builds. It intentionally does not test the unported world/chunk/item-render lifecycle outside this family. Native client bootstrap uses Unsafe solely to supply the real Minecraft resource-manager singleton without opening a display; warnings and registry-prefix warnings in the log are expected test harness output. Forge supplies the internal `models/` prefix; public caller normalization is tested with the supported bare and `block/` routes, not a doubled `models/models/` path.
