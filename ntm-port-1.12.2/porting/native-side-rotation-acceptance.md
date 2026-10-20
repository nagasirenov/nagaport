# Native rotated-cube crane and editor family

## Finite scope

This family retires the legacy `RenderBlockSideRotation` rendering declaration and its static render ID through real native 1.12.2 blockstates, extended-state snapshots, baked models and a client ModelRegistryEvent loader. The existing renderer source is repurposed, not excluded or replaced by a legacy API facade.

The owned manifest is `tests/side-rotation-production-scope.json`: 16 Java sources and nine blockstate JSONs. It covers six crane block classes, their complete orientation/notification base tile, four complete editor block classes including nested tiles and GUIs, the shared interface/state/model/loader, and all nine registered block identities. The parent integrates only the corresponding nine initializer names, four covered editor texture setters and one retired renderer registration. No previous spotlight or conveyor source is changed.

## Rendering contract

- World metadata and item metadata remain distinct selectors. Crane output overrides are captured with all six original textures and UV rotations in an immutable extended-state snapshot; baked models never retain a World or TileEntity.
- The original renderer's misleading North/South/East/West UV assignments compose to actual geometric face source sides `[0,1,3,2,5,4]`. Values other than exactly 1, 2 or 3 retain the original default UV branch. All sixteen raw metadata values remain representable.
- Full-cube positions, atlas materials and UVs use the already verified native ConveyorCuboidGeometry face encoder, unchanged. World faces occupy the six native culling buckets with white vertices, explicit normals, diffuse lighting enabled and native smooth AO. They are not flattened to the flat OBJ path.
- The original inventory callback has no UV rotation assignments and applies +90 degrees around Y before centering. The native inventory mesh preserves that geometry/normal transform in block-item coordinates and uses the established native block display transforms.
- The loader resolves actual ItemBlock identities at ModelRegistryEvent, not during static initialization. Existing LegacyBaseModelHandler explicitly excludes this interface, so it does not claim these item models. Each metadata state has a real JSON world route and matching native item route. No invented default owner, placeholder texture or mesh is supplied.

## Owner and lifecycle evidence

`tests/native-crane-owners-proof.log`: **15,477 assertions**. All six complete crane block owners, complete production TileEntityCraneBase, real state/interface classes and native Minecraft/Forge compile together. Tests execute actual World/Chunk metadata mutation and block replacement, original world/item face selectors, 840 fixed-height piston placements, inventory transfer limits/order/side predicates, redstone/comparators, screw/settings ordering, packet bytes, filter copy behavior and exact seeded drops. A real native SPacketChunkData regression first reproduced missing initial client state; the crane-only getUpdateTag override now supplies full writeToNBT, preserving the original initial description-packet behavior. Full and partial chunk packet byte roundtrips plus native handleUpdateTag restore inventory/output override and first-render texture selection for all five crane owners.

`editor-cube-native-test.log`: **2,439 assertions**. The four complete owners, every nested tile and GUI compile against native Minecraft/Forge. The separate `editor-cube-source-test.log` verifies 54 complete behavior/GUI method bodies against pristine source after explicit native API normalization. Tests cover actual World/Chunk metadata retention on client/server sides, replacement invalidation, stored selections/control data, original NBT and wire bytes, placement thresholds, activation/tool/copy behavior, structure dispatch/replacement order and native GUI save behavior. Original quirks, including identity-string checks and the existing replacement fallback, are deliberately retained.

The five concrete crane tiles are the only descendants of TileEntityCraneBase; neither they nor its shared base chain override shouldRefresh. The four editor owners have no subclasses. The only lifecycle additions are block-identity refresh overrides in the owned crane base and four nested editor tiles. Real native chunk mutation proves inventory/output override or editor selection state survives metadata changes, while a different block still invalidates/removes the old tile.

## Explicit boundaries

These are focused native proofs, not a full mod launch or graphical acceptance. Production shared TileEntityMachineBase/TileEntityLoadedBase and the five concrete crane engines remain outside this scope. Tests compile and run the complete owned classes against explicitly declared NTM collaborators for those engines, external registries, inventory helpers, structure/loot catalogues, packet transport and related tools. Their production ticking, shared-base persistence, live GUI opening, real structure generation, FakePlayerFactory server integration and GL output are not claimed.

The headless native renderer checks use real Minecraft/Forge classes, controlled atlas rectangles and controlled neighbor states. Native smooth AO/culling behavior is tested rather than inferred solely from the AO flag; this does not imply pixel-identical lighting between different game versions.

## Model gate

`tests/side-rotation-model-native-proof.log` is terminal PASS: **584,751 assertions**, **2,584 complete layouts** and **62,016 ordered native vertices**. Its single native compilation includes all **39 complete production inputs**, including every owned Java source and all complete owner/nested classes. All 39 logged hashes match the final source, including the corrected crane initial-sync method.

The proof executes the cached original 1.7.10 RenderBlocks face bytecode with independently extracted pristine owner selectors. It checks all nine owners, all sixteen metadata values, all seven crane output overrides plus missing tiles, exact native XYZ/UV float bits and inventory normals. Real Forge SmoothAo and vanilla smooth output match native FaceBakery color/light corner references; air, stone and glass exercise culling. The real Forge damage-model path preserves output override geometry with crack textures.

Actual ModelRegistryEvent bus dispatch, deferred ItemBlock identities, ModelLoaderRegistry, ItemModelMesher and BlockModelShapes resolve all 144 item and 144 world routes. Directory and JAR resource packs decode all 87 exact-case original PNGs and all nine real JSONs. Cache reload/fresh atlas rebake preserves previously retained model geometry and sprite identities.

`tests/side-rotation-production-scope.json` freezes the exact 25 owned files. `tests/side-rotation-artifact-scope.json` lists this acceptance note and every finite test/log/fixture artifact for packaging. Parent-owned integration is audited separately.

## Portable replay inputs

No oracle invokes Git or depends on unpublished commits. The 138,471-byte `tests/fixtures/side-rotation-pristine.zip` contains 101 byte-identical original source/PNG inputs, checked against its pinned manifest. Both crane and combined-model generated oracle code are identical when read from this archive or the supplied original source tree; the 54-method editor source proof also produces identical output. `tests/side-rotation-fixture-proof.log` records these checks. The original client JAR remains an explicitly supplied cached input with pinned SHA256; the tests never download it.

Run the three focused native scripts using the cached JDK8 and native classpath:

- `tests/probe-native-crane-owners.py`
- `tests/editor-cube-native-probe.py`
- `tests/side-rotation-model-native-probe.py`

The companion notes give exact commands. `tests/editor-cube-source-test.py` defaults to the packaged pristine archive; an explicit original tree remains available for cross-checking.
