# Native Spotlight model and resource proof

## Result and reproduction

Run from the project root:

`python3 porting/tests/spotlight-model-native-probe.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath "$(cat /workspace/shared/nagaport-tick-preparation/classpath)"`

The cached original 1.7.10 client at `/tmp/nagaport-legacy-client.jar` is pinned to SHA256 `a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`; use `--legacy-client` to supply another local copy of the identical file. The probe does not download or install anything.

Terminal result in `spotlight-model-native-proof.log`: **3,498,230 assertions**, **2,208 exhaustive complete layouts**, and **837,256 ordered native vertices**. The complete production Spotlight, SpotlightModular, SpotlightBeam, BlockBeamBase, RenderLight, SpotlightGeometry, SpotlightBakedModel, SpotlightModelState, SpotlightBlockModelHandler, mesh/parser, TileEntityData, block transforms, block-position-light quad, interfaces, enums and direction classes compile together against real Minecraft 1.12.2/Forge. No production source is copied, extracted, or modified for compilation. Every production source fingerprint is printed in the terminal log. The script reuses only the owner probe's four explicit external NTM fixtures: ModBlocks, CelestialBody, CelestialBodyTrait and CBT_Lights. There is no owner fixture or model-owner integration gap. The separate owner probe additionally verifies full gameplay callbacks.

## Independent original evidence

`spotlight-model-native-source-manifest.json` pins pristine RenderLight, ObjUtil, Spotlight, SpotlightModular, ResourceManager, ModBlocks and all nine original assets. The script checks the six original ModBlocks constructor/type/on-off/texture definitions and unchanged source assets. Pristine RenderLight inventory/world/getRotation bodies and Spotlight model/part/direction selectors are executed verbatim in a test-only descriptor recorder. Its world adapter supplies six adjacency bits; it does not call production selectors.

A separate ordered OBJ parser supplies the vertex/UV/face-normal oracle. Actual original cached Vec3 bytecode verifies all three rotations in X, then Z, then Y order against native MathHelper-table arithmetic, with 8,232 exact double comparisons. World source positions are cast to float before translation, matching ObjUtil. World normals do not roll in the original, but they have no emitted normal channel in the native BLOCK format and do not produce diffuse shading. Inventory normals are compared as exact signed bytes after the original -90 degree Y rotation. Inventory geometry uses original scale 1.5 and the native block-item +0.5 recenter, with every item metadata value selecting the original zero-connection part independently of world orientation.

Every original asset triangle and UV is checked in source order: cage_lamp has 270 triangles, fluorescent_lamp has 104 triangles across its three groups, and flood_lamp has 76 triangles. Parts are CageLamp, FloodLamp, FluoroSingle, FluoroCap and FluoroMid. Every material is the original on/off atlas texture, with no normal/shader tint substituted. A separate controlled triangle/quad fixture tests last duplicate group selection, exact case, missing group empty output, and explicit rejection of missing UVs.

## Exhaustive layouts and native lighting

The direct suite checks all six registered owner definitions and all metadata 0..15. This includes every valid attachment direction, the broken bit, and the original UNKNOWN direction for metadata 12..15. Both modular owners check all 64 adjacency masks for every metadata value. The untouched original renderer determines first-connection axis priority, opposite-neighbor count, roll and part selection. Inventory checks all 96 owner/metadata routes.

6,144 owner/metadata/mask snapshots exercise actual SpotlightModelState against a real native IBlockAccess. Matching block identity with differing neighbor orientation remains connected; the opposite on/off owner does not connect. Listed metadata remains unchanged, state snapshots are immutable, and null/nonextended/unpopulated snapshots reject explicitly.

Both actual vanilla BlockModelRenderer.renderModelFlat and Forge VertexLighterFlat emit representative disconnected and connected models across all six directions. Asymmetric neighbor brightness proves each emitted vertex uses the original block-position packed light, not an adjacent face's light. Every emitted world color remains opaque white, with no added directional diffuse or AO. World models are unculled general geometry; item geometry has ITEM normals and diffuse lighting enabled. This is functional output validation, not a flag-only test.

## Native registry and resources

The real client ModelRegistryEvent subscriber is invoked after native block/ItemBlock registration. Each directory and packaged-JAR pass exercises all 96 metadata world routes and all 96 item routes, exact-case JSON loading, ModelLoaderRegistry custom loading, native BlockModelShapes and ItemModelMesher lookup. World JSON has identity model rotation and no UV lock. Every actual material is advertised to the atlas, and each of the six original PNGs is decoded.

All three OBJ resources are then changed through a controlled native IResourcePack, reloaded, and compared to independently parsed changed geometry. Original resources restore successfully. Missing each mesh fails explicitly rather than silently substituting unrelated geometry. Resource reload invalidates cached descriptors, accepts replacement sprite objects, and leaves already-baked lists immutable. The same checks run on the resource directory and a newly packaged resource JAR.

Custom-loader acceptance is tested directly for the canonical models/block prefix. ModelLoaderRegistry integration uses supported native and block-prefixed routes: passing an already models-prefixed location through that registry would add an extra models prefix, so the test does not invent that unsupported integration path.

## Boundaries

This is a bounded CPU-headless native proof, not full-mod compile, asset stitching on a GPU, graphical startup, or old/new screenshot/pixel equivalence. Controlled atlas rectangles supply UV interpolation; an Unsafe-allocated Minecraft resource-manager holder avoids GL startup. No Minecraft or Forge class is replaced. The four explicit external registry/celestial fixtures are not exercised as real production systems. Actual owner construction, model resources/materials, extended state and adjacency identity execute in this model suite. Owner tick, beam, repair, support, collision, generation and gameplay behavior have additional coverage in the separate complete-owner probe.
