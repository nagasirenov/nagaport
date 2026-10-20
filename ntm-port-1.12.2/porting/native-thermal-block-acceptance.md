# Native thermal blocks and furnace extension

## Production scope

The brick furnace, RTG furnace, RTG double furnace, ordinary double furnace and its extension use native 1.12.2 block state/render APIs. The four furnace types share `BlockThermalFurnace`, retaining every original metadata value 0–15. The ordinary double furnace adds a neighbor-derived `covered` actual-state property; coverage is not persisted in metadata. Four narrowly scoped tile `shouldRefresh` methods retain tile state on metadata changes while still invalidating different block identities. Existing on/off swap code continues to retain and reattach the original tile. The non-normal extension returns native `BlockFaceShape.UNDEFINED`, preserving its old non-solid support behavior without altering its original full collision bounds.

Texture selectors preserve the original ordering, including metadata 0's front on both DOWN and SOUTH where applicable, metadata 1's front on UP, the ordinary double furnace's unconditional world DOWN `brick_fire`, and its different context-free inventory DOWN. Covered fronts, covered top and the active/off textures remain independent. Cube faces remain in native cull buckets, never duplicated in the general item bucket. DOWN UV is explicitly inverted relative to native FaceBakery defaults, matching original RenderBlocks and the already validated electric furnace.

`ThermalFurnaceModelHandler` registers a real native `ICustomModelLoader` and 16 item metadata entries for each of the nine block identities. Nine real blockstate JSON files describe 161 world variants. The custom loader declares all atlas textures before stitching and produces the final baked geometry directly; there is no missing-model stand-in or late overriding placeholder. Native blockstate `Variant` prefixes (`block/`, followed by `models/`) are handled alongside direct inventory model locations. The loader reads the original OBJ through the resource manager and invalidates that mesh on reload. Original CP22 item-family ownership is unchanged; none of these nine items was part of that custom mesh family.

## Exact extension geometry and lighting

The original OBJ file remains byte-identical to pristine (`ce9b7ad83ddee6b0d499afcc0ff405d3110a9f1c84c65e8e2a7d9fd54de233f1`). The 20 source triangles are emitted in Top, Bottom, Side order, with the original material assignment, V flip, triangle-to-degenerate-quad conversion and X/Z +0.5 centering. No geometry is substituted or rescaled. Resource geometry is read again on reload; missing required parts or UVs fail explicitly rather than producing simplified geometry.

World shading uses the original `ObjUtil.renderPartWithIcon` formula, not its different `renderWithIcon` formula:

`max(0.45, normal.y * 0.3 + 0.7 - abs(normal.x) * 0.1 + abs(normal.z) * 0.1)`

That shade is quantized to the original Tessellator color byte. World AO and additional native diffuse multiplication are disabled. Item quads retain the original geometric normals and white vertex color with native item lighting. Both use the native block display transforms.

`BlockPositionLightQuad` is limited to this unculled, already-shaded extension path. Its supported native `pipe` override emits a zero *lighting* normal into Forge's `VertexLighterFlat`, selecting `packedLight[0]`, exactly the block-position brightness used by the old ISBRH. With Forge's optional light pipeline disabled, its lighting-face axis is selected from an actual span of that triangle: native `fillQuadBounds` therefore does not classify it as a neighbor-sampled boundary face. This changes neither positions nor UVs. Item rendering does not use this wrapper. Both actual native render paths are tested with deliberately different center and neighbor light; choosing general/null-face quads alone would not have been enough.

## Verification and limits

The focused test suite is `porting/tests/thermal-block-probe.init.gradle`, task `testPortThermalBlocks`. It compiles complete production block, model and loader classes against the real cached Minecraft/Forge 1.12.2 API. It uses explicit NTM-only registry/tile/GUI collaborators, never replacement public Minecraft/Forge classes. Tests separately cover the native World/Chunk lifecycle and the renderer/model registry.

The final completed run passed 9,006 lifecycle assertions and 411,490 model/render assertions, including all-six-face support behavior and the unchanged full collision bounds. The final test log is `porting/native-thermal-block-test.log`.

The model tests independently calculate texture/UV expectations from pristine selectors and OBJ data, execute the real native flat lighting pipelines, resolve real parsed blockstate JSON through ModelLoaderRegistry, register native item mesher mappings and verify resource reload. Source checks pin pristine inputs and test fixture assertions instead of claiming target snapshots as independent behavior proof.

These focused tests do not establish a full mod launch, gameplay acceptance, OpenGL screenshot parity, dedicated-server startup, full production tile processing/network integration or pack-wide acceptance. The parent checkpoint retains those broader remaining gates. No source-set exclusions, relocated Forge shim, geometry placeholder or global compatibility rendering layer was introduced.
