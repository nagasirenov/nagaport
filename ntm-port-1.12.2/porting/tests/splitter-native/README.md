# Native crane splitter: bounded production freeze

Baseline: `641ba083c276c6ac75afa6a8f996cdd86f8b16d4`.
Work tree: `/workspace/shared/nagaport-splitter-next`, created from that commit. The main checkout was not edited.

## Five production files

- `src/main/java/com/hbm/blocks/network/CraneSplitter.java`
- `src/main/java/com/hbm/tileentity/network/TileEntityCraneSplitter.java`
- `src/main/java/com/hbm/render/block/RenderSplitter.java`
- `src/main/java/com/hbm/main/SplitterModelHandler.java`
- `src/main/java/com/hbm/render/tileentity/SplitterBreakingRenderer.java`

`integration-fragments.json` contains exactly two shared-file substitutions: the `ModBlocks` constructor chain and removal of the old `ClientProxy` ISBRH registration. Apply these fragments without replacing either shared file. Existing native `register(crane_splitter)` and existing `TileMappings` identity `tileentity_splitter` remain unchanged. Native model registration uses the exact original `hbm:tile.crane_splitter` owner and its actual ItemBlock after registration.

No compatibility API, Forge shim, fake platform class, global render context, helper override, new recipe, balance adjustment, or unrelated owner change was added. No full-mod compile, download, installation or push was performed.

## Preserved behavior

- Complete native block remains a `BlockDummyable`, with dimensions `{0,0,0,0,0,1}` and zero offset. Native superclass state mapping/placement/break logic is reused, not copied or replaced.
- Left/core metadata 12–15 and right/dummy direction metadata preserve the original two-block mapping. Entry direction/package rejection, snapping height, immutable-vector reproduction of the old mutable clamp side effect, tool selection/clamps and original no-GUI behavior remain unchanged. No filter was invented: the original splitter accepts item types unconditionally after its direction test.
- Ratio state machine, alternating preference, residual count, all four NBT keys and two-byte ratio payload are unchanged. Native copies preserve item, metadata, count and deep NBT. Original absent/nonpositive NBT ratio fallback remains, without introducing the tool's upper clamp into saved data.
- Original chunk dirty notification maps to `World.markChunkDirty(pos,tile)`, not the more consequential `TileEntity.markDirty()` comparator callback. Tick dispatch is real `ITickable.update`, and the original server-only `networkPackNT(15)` remains.
- Real `EntityMovingItem` callbacks consume the original once and spawn the two native stacks at original quarter-height snapping positions. Empty split halves do not spawn.
- All original OBJ group selectors and eleven authored face textures remain in order, including the reversed top texture on the bottom. The original base `crane_side` particle icon is retained separately; it is not a twelfth OBJ material. No authored asset was modified.
- World orientation and left/right selection match pristine renderer code for all 16 metadata values. Inventory retains both halves and the exact `S(.625) RY(-90) T(0,-.5,.5)` transform, followed by second-half `T(0,0,-1)`. Native centered item coordinates are explicit. Existing complete native item models/helpers are reused, with no per-frame mutable global.
- Existing native atlas OBJ geometry keeps original normal shading and owner-position brightness. Exact-owner breaking-only TESR reuses `NetworkBreakingRenderer(false)` because pristine splitter OBJ rendering did not honor a replacement crack icon. Its native damage buffer preserves atlas icons/lightmap and omits vertex colors as the original dispatcher did.

## Executed proof

Run from any directory:

    python porting/tests/splitter-native/run.py --module /path/to/ntm-port-1.12.2 --java-home /path/to/java8 --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" --pristine /path/to/ntm-original-pristine

`proof.log` records a passing compile of the five COMPLETE changed production classes, COMPLETE native `BlockDummyable`, multiblock helper, actual moving item/conveyor entity classes, actual native tile base and complete shared renderer/model classes against genuine cached Minecraft 1.12.2/Forge APIs. No Minecraft/Forge class is stubbed. `source-manifest.json` enumerates and hashes every compiled production/support source, original source and preserved splitter asset, and lists every explicit NTM-domain fixture.

70,234 assertions passed, including:

- Every ratio pair 1..16 through eleven serial input sizes, stateful rounding compared with the pristine split method, NBT save/reload between every input, native copies/counts and two-byte serialization.
- All horizontal multiblock mappings, native base core resolution, real moving entity entry/death/spawn/DataManager/NBT paths, count conservation and zero/one-item empty-half suppression, direction/package/no-GUI/tool branches and 384 original mutable-vector movement cases.
- 32 complete pristine renderer cases: all metadata world/item transforms, all vertices and UVs, exact part/material order, unculled count and exact float-arithmetic normal-shade bytes. Oracle selectors and GL order come from pristine methods, not rewritten production selectors.
- Real native Forge flat lighter and vanilla fallback for all metadata: owner brightness, unchanged normal-shade RGB and full vertex counts. Original colorless damage buffers retain original atlas UVs and owner lightmap.
- Actual `ModelRegistryEvent`, state mapper, item route registration, loader registry resolution, resource texture existence, exact-owner exclusion and repeat registration. Real FolderResourcePack and FileResourcePack/JAR loading and reload rebake both execute.

## Unresolved acceptance gates, not waived

- **Live GL/pixel acceptance:** no launched 1.7/1.12 paired scene, screenshot or GPU context was available in this bounded probe. Inventory/held/ground/frame appearance, animated belt sampling, alpha edges, mipmaps and item-light visual matching remain live acceptance gates. Headless geometry/UV/shader/brightness equality does not replace them.
- **Crack rendering:** exact native dispatch predicates, registered TESR, original material retention and colorless native output are exercised. `NetworkBreakingRenderer` GPU blend/alpha/polygon-offset upload and GL state restoration use existing audited support, not a newly executed live GL test. This remains a visual gate, not a claim that a crash-free chunk compile proves damage appearance.
- **Lighting:** both headless native flat pipelines verify the original owning-block brightness and no double shading for every metadata state. Chunk-boundary/day/night/occlusion/mixed-mod live scene appearance remains unverified.
- **Unknown-mod compatibility:** foreign registry IDs, unknown names and subclasses are explicitly excluded from this native registration/damage ownership and verified untouched. Their models and interactions are not validated; no generic parity guarantee is made.
- **Full integration:** whole-game launch, actual network transport/client synchronization across clients, persisted world migration, complete mod compilation and installation are coordinator/integration gates. NTM packet threading, external lookup, inventory/recipe/energy collaborators required by shared support are explicit fixtures, not functional transport proof.
