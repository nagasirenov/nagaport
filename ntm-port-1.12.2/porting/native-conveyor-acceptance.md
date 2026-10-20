# Native conveyor owner and model closure

## Finite content

This slice replaces three retired renderer roots (`RenderConveyor`, `RenderConveyorLift`, `RenderConveyorChute`) with native baked geometry and migrates the complete nine conveyor owner classes. The six existing registered blocks remain standard, express, double, triple, lift, and chute conveyors. Registry names, hardness, resistance, creative visibility, and default STONE sound remain unchanged. The parent-owned initializer/registration edits are separate from the 24 production paths listed in `tests/conveyor-production-scope.json`.

Classic is a real seventh concrete owner sharing the original slab render ID, but has no in-repository instantiation or registration. Its complete native owner is compiled and tested. A separately registered Classic must supply its atlas material explicitly through `setModelTexture(ResourceLocation)`; the native handler provides a state-mapped slab model. No Classic content or default texture is invented, and legacy external setter compatibility is not claimed.

## Native rendering contract

- Sixteen listed metadata states retain the original saved nibble. Lift/chute use an immutable unlisted snapshot of the six belt-neighbor identities, vertical enterable identities, and original y>0/y<255 render gates. Gameplay predicates remain separate and unchanged.
- Original cuboid bounds, box order, zero-width grate faces, material selection, and per-face UV rotations are preserved. The original partial-box UV formulas are not replaced with generic quarter-turn texture math. No texture PNG or animation metadata is changed.
- Native quads retain explicit axis normals, white untinted color, one diffuse factor, and native smooth AO. Belt bottom/horizontal faces are culled; its recessed top is not. Lift/chute retain their original unconditional face rendering.
- Native vertex order uses only cyclic shifts of the original face corners, preserving winding and every position-to-UV association while matching native smooth-AO corner indexing. Both the real Forge and vanilla smooth renderers are exercised under asymmetric light and occlusion. This is not an old/new pixel-equivalence claim.
- Standard/express/double/triple and Classic inventory slabs force original metadata 2 and preserve the original inventory height offset without moving texture coordinates. Chute retains its original three-belt/corner/grate inventory assembly. Lift retains `shouldRender3DInInventory=false` through a real native generated-flat-icon model rather than an empty or invented 3D model.
- `ConveyorBlockModelHandler` registers its loader at native `ModelRegistryEvent`; ItemBlock identities are resolved only then, after the parent registry flush. Six real blockstate JSON files expose every world route. Inventory routes are separately registered for all sixteen metadata values. Unknown owner/material routes fail explicitly rather than substituting unrelated geometry.

## Mechanics and bounded collaborators

Complete native owner tests use actual World, Chunk, block states, EntityItem, ItemStack, NBT, and Forge registry types. Original movement bodies form an independent mutable-vector oracle. The tests cover placement, all metadata/curve states, screwdriver cycles and block replacements, lane and speed thresholds, negative/world-height boundaries, collision/selection dispatch, drops, stack-copy identity/count/NBT, server/age/dead guards, and spawn success/rejection/exception order. A rejected spawn still kills the original item, exactly as the source did; this port does not opportunistically change that behavior.

The production EntityMovingItem and its parent entity engine are not closed by this slice. A clearly bounded NTM EntityMovingItem collaborator observes the conversion handoff. Registry identity, unused tool-registration storage, and localization collaborators are also declared by the owner probe. Passing tests do not establish full moving-item ticking, server/save behavior, external tool event dispatch, live OpenGL presentation, or complete-mod acceptance.

`ItemConveyorWand.java` itself also retains unresolved legacy gameplay callbacks, world metadata/block-placement calls, and an old-signature `onBlockPlacedBy` call. This selected owner proof does not claim the item construction workflow is native.

The registered conveyor-wand `ItemRenderBlock` callback is a separate sibling-owned closure. Its native callback must use `ConveyorBlockModelHandler.inventoryModelLocation(block, 0)` and preserve all-true original helper behavior; this block-family acceptance alone does not claim that wand rendering is complete.

## Reproducible evidence

- Owner complete-class compilation and movement/mechanics: `tests/probe-native-conveyor-owners.py`, `tests/ConveyorOwnersNativeTest.java`, `tests/native-conveyor-owners-notes.md`, and `tests/native-conveyor-owners-proof.log`. Final owner result: **469,591 assertions**, including **101,880 pristine-oracle movement vectors** and actual native fence/torch rejection with positive stone controls.
- Exact cached-original cuboid UV/geometry and native lighting: `tests/conveyor-cuboid-native-probe.py`, `tests/conveyor-cuboid-native-test.java.in`, `tests/conveyor-cuboid-native-notes.md`, and `tests/conveyor-cuboid-native-proof.log`. Final helper result: **806,774 assertions**.
- Whole-family geometry, registry, resource loader, animation, and reload proof: `tests/conveyor-model-native-probe.py`, `tests/conveyor-model-native-test.java.in`, `tests/conveyor-model-oracle.java.in`, `tests/conveyor-model-native-source-manifest.json`, `tests/conveyor-model-native-notes.md`, and `tests/conveyor-model-native-proof.log`. Final result: **5,942,823 assertions**, **30,320 pristine descriptor layouts**, and **813,600 ordered original faces**, including **768 actual native breaking-overlay cases**. Directory and packaged-JAR resources both pass actual ItemModelMesher/BlockModelShapes/custom-loader routing and model reload. All 26 compiled production dependency fingerprints match the final sources, including all 17 owned Java classes.

The family descriptor oracle executes verbatim pristine renderer decisions in a test-only cuboid recorder; its comparison reuses the separately verified native encoder. The independent cuboid probe separately checks that encoder against actual cached original face-method bytecode. Native PNG dimensions and animation metadata are parsed, while a documented CPU-only pixel/frame setup feeds the real generated-item bake because native TextureUtil initializes an OpenGL singleton in this headless environment. Controlled atlas rectangles and a headless Minecraft holder are fixtures, not substitutes for production classes or claims of texture upload.

All probes use cached actual Minecraft/Forge 1.12.2 dependencies and JDK 8. There are no replacement net.minecraft/net.minecraftforge classes, production old-Forge shims, source exclusions, placeholder meshes, downloads, or installs. The original face-method bytecode probe uses the existing cached 1.7.10 client only as a test oracle.

The 24 production sources are frozen with hashes in the scope manifest, and all three selected native probes pass. The independent finite artifact list is `tests/conveyor-artifact-scope.json`. Parent-owned full frozen-mod compilation and the separately owned conveyor-wand callback remain outside these selected family gates.
