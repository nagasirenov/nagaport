# Native conveyor cuboid proof

## Production contract

`ConveyorCuboidGeometry.append(output, inventory, sprites, rotations, minX, minY, minZ, maxX, maxY, maxZ[, yOffset])` emits six native `BakedQuad` faces in `EnumFacing` order. Arrays use DOWN/UP/NORTH/SOUTH/WEST/EAST, corresponding to the original bottom/top/east/west/north/south rotation fields. The overload's Y offset changes positions only, not texture sampling. Original conveyor cuboids use coordinates within 0..1; this is deliberately a conveyor-specific helper, not a general replacement for the retired renderer API.

The model controls cull buckets. The native builder uses ITEM format for both contexts, just as native FaceBakery does: axis normals remain explicit, including zero-area grate faces, while the native lighter generates chunk lightmaps. Every face is white, untinted, diffuse-enabled, with uncontracted original atlas coordinates. There is no fixed world-light override or pre-baked AO. `inventory` remains in the caller contract although both contexts correctly use the same vertex format and native lighting flags.

## Original behavior verified

The test executes the six original face methods from cached Minecraft 1.7.10 client SHA256 `a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`, isolated by a URL class loader. Its original renderer and tessellator are real classes, not test shims. A proxy provides only atlas-icon interpolation. The original renderer instance is allocated without its client constructor to avoid graphical startup; all face-method bytecode runs unchanged.

261 deterministic partial, asymmetric, full, and degenerate boxes are checked across all four rotations, item/world offsets, uniform/mixed per-face rotations, and all six materials. Position/UV float bits match exactly per corresponding corner. Native canonical corner order differs from the original only by cyclic shifts `{0,2,1,0,1,3}`; winding and each XYZ-to-UV association remain unchanged. These shifts are necessary because vanilla smooth AO assigns corner values by native vertex index.

The oracle caught two important partial-box differences from naive texture rotation: negative-Z and positive-X default U reflect the box endpoints instead of the entire texture; vertical-side quarter-turn U similarly uses the box's reversed Y endpoints. Production preserves both.

## Native lighting and culling proof

The test uses the real Forge `VertexLighterSmoothAo` through `ForgeBlockModelRenderer.render`, and the actual vanilla `BlockModelRenderer.renderModelSmooth` fallback. A controlled native `IBlockAccess` supplies asymmetric sky/block light and stone occluders. The slab, vertical strip, zero-width X grate, and zero-width Z grate produce non-flat lighting: 14–24 distinct light values and 7–12 colors across 24 vertices. Uniform-air tests verify exactly one native directional diffuse factor in both pipelines. Every zero-width face retains explicit finite normals and opaque output colors.

For nondegenerate meshes, emitted positions, colors, and lightmap words match an independent native FaceBakery reference under the same asymmetric world, verifying native AO corner association rather than merely checking `isAmbientOcclusion()`.

A controlled model fixture routes the slab's top face to general quads and its bottom/horizontal faces to cull buckets. Both native renderers emit only the top against opaque neighbors and all six against air. General lift/chute-style cuboid faces remain unculled against opaque neighbors. The original lift/chute `shouldSideBeRendered` methods return true. This helper test proves the native culling mechanism, not integration of the production model's buckets; the separate conveyor model probe owns that integration.

## Reproduce

Run `python3 porting/tests/conveyor-cuboid-native-probe.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath "$(cat /workspace/shared/nagaport-tick-preparation/classpath)" --legacy-client /tmp/nagaport-legacy-client.jar` from the project root.

Result: **806,774 passing assertions**. Full run saved in `conveyor-cuboid-native-proof.log`. Only the new production helper and native test code are compiled; there are no Minecraft/Forge stubs, legacy compatibility classes, downloaded packages, or installs.

## Boundaries

This establishes exact original geometry/UV/material associations and correct supported native AO/diffuse/culling behavior. Minecraft 1.7 and 1.12 lighting algorithms are different; this does not claim old/new lighting pixel parity or live graphical acceptance. Registry/model reload integration and every renderer-selected assembly remain the parent conveyor-model probe's responsibility.
