# Native fluid-duct block proof

The bounded production changes are the complete `FluidDuctBase` and `FluidDuctStandard` classes. They use native 1.12.2 activation, hand/stack access, world positions, sound, blockstate, collision, selection, pick-block, and render-layer APIs. Standard stores all sixteen legacy metadata values independently of its three visual/drop variants. Rendering uses native `MODEL` / `CUTOUT_MIPPED`; connectivity and fluid color are unlisted state only. The native model loader and rendering successor are validated separately.

## Reproduce

Run `python3 porting/tests/duct-native-probe.py --java-home <JDK8> --classpath <native Forge classpath> --original-root <pristine original repository>` from the repository. The script defaults its target repository to its own location and the pristine sibling to the current workspace's original checkout.

The successful output is `porting/native-fluid-duct-block-test.log`: 293,282 assertions against the real cached Minecraft/Forge 1.12.2 classes.

## What was proved

- Complete production Base and Standard classes compile, together with actual `IBlockMulti`, `IAnalyzable`, `IBlockFluidDuct`, `IItemFluidIdentifier`, connector interfaces, direction type, and both unlisted-state helpers. No Minecraft/Forge facade or production-method extraction replaces either block class. The final integration also compiles actual ModSoundType, ModSoundTypes and ModSoundEvents, with only MainRegistry RNG ownership controlled, and verifies the constructor retains the custom pipe sound.
- Pristine Base activation, recursive type-change and debug-query bodies are compared to production after only the explicitly enumerated native API substitutions. This deliberately preserves source-block recursive dispatch, including its non-duct neighboring tile endpoint behavior, and the original depth limit.
- Both pristine block source files are SHA-256 pinned. Original compound collision and selection branches are extracted into an executable oracle with only the native AABB constructor changed.
- All 64 masks × 16 persisted metadata states × 3 fluid types reproduce ordered collision boxes, merged selection bounds, one-time world offsets, clipped entity queries and fluid tint. Zero connections retain six disconnected arm boxes without a center; one connection and opposite pairs retain a full-axis box; other combinations retain center plus arms.
- Original three creative skins, drop metadata rectification, placement metadata, native non-full/opaque flags, cutout rendering, typed directional connectivity, block-connector short circuit and y-boundary behavior pass.
- Activation tests span both native hands and every Ctrl/Alt/sneak/single-versus-multi-identifier/same-versus-different-type combination, with empty/non-identifier/missing-tile rejection, copy-back and native experience-orb sound checks. Recursive depth, unchanged-type, wrong-type and source-block dispatch cases pass.
- Debug values, fluid-ID item picking, native missing-tile pick fallback and overlay text/colors are retained.
- Real `World`/`Chunk` instances create the pipe tile through the native block hook, retain all sixteen metadata states, resolve typed neighboring tiles, and update geometry after neighbor removal.

## Explicit missing-tile exception

The original compound collision routine emitted no boxes when its pipe tile was missing. That behavior remains unchanged. Its separate bound routines, however, reused whichever mutable global block bounds a previous query happened to leave behind. Native `getCollisionBoundingBox` now returns `NULL_AABB` for the missing tile, matching the absence of physical boxes, while selection/bounding returns deterministic `FULL_BLOCK_AABB` to keep the block selectable. Missing-tile rendering keeps the original magenta tint (`0xff00ff`) and `Fluids.NONE` connectivity fallback.

## Boundaries

Unrelated NTM registry, fluid, tile, identifier, player-property, network and overlay collaborators are controlled test fixtures. Production `Library.canConnect` and `Library.canConnectFluid` bodies are extracted unchanged for isolation; no temporary accessor adapters remain. Tile implementation/NBT behavior was not changed or claimed native by this work. This is CPU-headless native API/behavior evidence, not a live graphical/client acceptance run, and not a claim that the legacy repository as a whole builds.
