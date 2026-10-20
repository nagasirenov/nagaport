# Original boxed-duct geometry versus native models

## Reproduce

From the repository root:

```
python3 porting/tests/box-duct-native-geometry/probe.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" \
  --legacy-client /tmp/nagaport-legacy-client.jar \
  --pristine /workspace/shared/nagaport/ntm-original-pristine
```

`porting/native-box-duct-geometry-test.log` records 3,387,630 passing assertions across 3,120 complete callback cases. There are 790 cases in which both original and native intentionally throw the same invalid-metadata array exception; the test preserves those domains rather than masking them.

## Independent original oracle

`source-manifest.json` pins the pristine `RenderBoxDuct`, all three owner classes, `RenderBlocksNT`, and `ColorUtil` source bytes, all original `boxduct_*` texture assets, and the Minecraft 1.7.10 client JAR. The probe executes the original inventory/world renderer bodies, owner icon registration and face-selection bodies, and the original lightening body in a test-only recording fixture. No production native geometry formula supplies the oracle.

The original world callback's `RenderBlocksNT.INSTANCE` replacement and final rotation reset execute as written. The recorder captures its ordered cuboid bounds, face textures, face-specific UV rotations, and final color. Inventory retains its one six-face draw batch and order (UP, DOWN, EAST, WEST, SOUTH, NORTH). The original item's −0.5 translation centers a unit-coordinate model; the native model stores those same coordinates before native display transforms.

For each recorded face, the test executes actual original 1.7.10 `RenderBlocks` face bytecode from the pinned client JAR. The six original rotation-field mappings are used. World negative-Z and positive-X instead execute the original `RenderBlocksNT` overridden methods verbatim, retaining its mirrored partial-face UV fix. These overrides execute with AO disabled because this proof measures geometry/UV, not the light solver. Original and native vertices compare exact IEEE float bits after the native canonical cyclic corner shift, which retains winding and face-corner association.

## Genuine production compilation

The probe compiles complete production `RenderBoxDuct`, `BoxDuctMaterials`, `BoxDuctBakedModel`, `PneumaticCuboidGeometry`, `ThermalModelTransforms`, `ItemTransformMatrix`, `ConnectionState`, `FluidConnectionState`, and `ForgeDirection` against genuine cached Minecraft/Forge 1.12.2 classes. It uses real `BakedQuad`, atlas sprites, vertex formats, native unlisted states, and item/model interfaces. No production methods are extracted or replaced.

The only NTM production collaborator fixture is `Library`, whose unused network method throws if called. Live network predicates are already tested independently with the complete native owners. The old API names appear only as nested test-recording classes, not production API facades or replacements for Minecraft/Forge dependencies.

## Coverage

- Every fluid/exhaust/cable family × all sixteen persisted metadata values × all 64 world connection masks, plus all sixteen inventory variants per family.
- Exact original ordered faces and textures, XYZ/UV float bits, all directional pipe shapes and elbows/junctions, and original custom N-Z/P-X UV behavior.
- Original white-skin-only final lightened tint, item white color, explicit finite axis normals, diffuse flag, and no second tint index.
- Complete baked-model output versus the same independent oracle, seed-independent cached identity, immutable lists, general-only face buckets, native AO/gui/model flags, and generic particle texture.
- Direct texture selection for all 64 masks and six faces at metadata −3, −1, 0–16, 31 and 255, checking both exact texture identity and invalid-index exception parity.
- All material dependencies have the original texture bytes. Fluid has 34 dependencies including its generic particle; exhaust and cable each have 12.

## Limits

This proves a bounded native rendering contract, not a full game build. It does not execute the original AO/brightness solver, compare framebuffer pixels, or claim old/new lighting equivalence. It does not replace the separate owner, texture-alpha/layer, model-loader/reload, item-transform, or destroy-stage integration proofs. Those remain separate explicit boundaries rather than hidden assumptions.
