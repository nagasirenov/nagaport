# Native structural fence and chain boundary

This batch migrates the complete original BlockMetalFence and BlockChain owners and their RenderFence/RenderChain consumers. StructuralBakedModel, StructuralGeometry and StructuralBlockModelHandler provide actual Forge 1.12.2 atlas quads, block state mapping, late registered ItemBlock identities, native ItemLayerModel inventory rendering and reload rebaking. No RenderBlocks emulation, fake Minecraft/Forge classes, excluded sources, empty renderer stubs or replacement meshes are used. Four original texture files are unchanged.

## Mechanics retained

The metal fence retains 16 raw metadata states, modulo-two drops, exact original same-owner/custom-gate/oak-gate/opaque-full-non-gourd connectivity, both ordered collision arms, bounds, lead attachment and the original client activation return. Collision height is 1.0, not vanilla fence's 1.5: the original HBM override sets maxY=1 and calls inherited Block.getCollisionBoundingBoxFromPool. Actual official 1.7.10 BlockFence bytecode (akz in the already cached original client) confirms that it has no collision-box-accessor override. Its *own* addCollisionBoxes sets 1.5, which HBM overrides. Native inheritance alone would therefore change mechanics.

Chain retains noncollision, always-climbable behavior, bottom material selection, capped selection minY=.25, exact ordered placement fallbacks, matching metadata above, support-loss destruction with drops, and NBT structure orientation. Unset metadata 1 and 6..15 produce no mesh as before. They also retain the original previous-selection-bounds carry rather than silently inventing a new shape; this explicitly odd compatibility behavior is documented and tested. Native side-solid queries preserve IBlockAccess/World's fallback path. No chain server-only guard is introduced, since the original neighbor callback has none.

## Geometry and native rendering

Fence preserves both zero-width planes, isolated stub width, straight-span post suppression, forced metadata-one post, whole-texture NORTH/EAST U mirroring and post override. Nonzero raw metadata selects the post material for panel geometry, but only metadata 1 forces a post, as in the original. Model routing also selects the correct post particle texture. All six faces, including degenerate zero-area caps, are retained. Chain preserves every ordered vertex/UV and two-sided face; no substitute crossed-plant geometry is used. Both retain mipmapped pass-zero texture sampling via CUTOUT_MIPPED.

Chain's own-block light and undimmed white color are checked against the actual Forge VertexLighterFlat and vanilla flat fallback with differently lit neighbors. Fence is checked through actual Forge flat/smooth-AO and vanilla flat/smooth renderers for emission count and positions. This does not assert that native AO is pixel-identical to 1.7 AO, or claim a launched-world screenshot comparison. Geometry/UV/material parity is independently checked against generated recording oracles using the supplied pristine renderer callback bodies, with canonical cyclic vertex order for native cuboid faces.

## Reproduce

From the module, with the existing native dependency classpath:

    python3 porting/tests/probe-native-structural-blocks.py --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" --java-home /workspace/shared/nagaport-toolchain/java8
    python3 porting/tests/check-native-structural-source.py

The runner compiles twelve complete production classes, plus a recording-only original geometry oracle generated from the supplied ntm-original-pristine tree. The sole NTM fixture is ModBlocks.fence_gate identity, represented by the actual vanilla IRON_DOOR; custom gate mechanics are outside this family. All Minecraft, Forge, item layer, registry, resource-manager, model-loader and light-pipeline types are genuine native classes. Cached LWJGL Linux libraries are extracted from the existing classpath, not downloaded. Actual ModelRegistryEvent registration checks 32 item-metadata routes and all 288 listed state combinations; PNG resources are read through native directory and ZIP packs. The headless client constructor is used for registry setup, not a game loop.

The model event filters the exact two owned registry names before class/item validation. Reusing BlockChain or BlockMetalFence under foreign namespaces or other hbm names does not cause registration capture or rejection; the native event regression registers all four such cases without ItemBlocks and verifies they remain untouched. A focused second native event run replaces both exact owned names with different block classes and verifies their pre-existing addon item routes and state mappers are preserved rather than rejected or overwritten.

Source/resource scope and exact hashes are in structural-production-scope.json. Evidence scope is structural-evidence-scope.json. The source guard also pins pristine inputs and audits parent-owned constructor/registry-removal integration without claiming whole ClientProxy/ModBlocks compilation. These tests are not a full mod compile, client/server launch, old-world/save test or complete custom gate acceptance.

## Deliberately open

BlockGrate and RenderGrate are byte-unchanged. Original metadata 8/9 rebases rendering into the adjacent block, including light/AO/culling origin. Merely moving baked vertices is not equivalent. Its complete native origin/light adaptation remains a separate gate; this batch does not flatten AO, omit those metadata states, replace their geometry, or claim that renderer migrated. Parent-owned registration retains RenderGrate. No remaining native declaration family is excluded from the production build.
