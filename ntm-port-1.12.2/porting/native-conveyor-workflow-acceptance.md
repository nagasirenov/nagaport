# Native conveyor gameplay workflow and moving-item engine

## Frozen finite scope

The six production files and their final hashes are listed in `tests/conveyor-workflow-production-scope.json`: the complete `ItemConveyorWand`, `EntityMovingConveyorObject`, and `EntityMovingItem` owners; native `WorldInAJar` access; two mechanical `ILookOverlay` mappings; and two native constructor mappings in the existing mutable faux `BlockPos` helper. The previous 24-file conveyor block/model family remains byte-identical. The single ModItems initializer integration is parent-owned and separate.

No generic legacy World/rendering facade, replacement Minecraft/Forge classes, source exclusions, placeholder production behavior, crane/package rewrite, or balancing fix was introduced.

## Wand behavior retained

- Native item-use/start-break/update/subtype/tooltip callbacks retain the four variants, original translation keys, inventory subtype selection, NBT fields, player-facing orientation, end snapping, route decisions, metadata, placement order, flags, messages, and consumption order.
- Independent verbatim pristine construction/helper bodies determine expected routes. Dry runs remain non-mutating; preview writes retain the original raw block/metadata values; actual native World/Chunk writes are checked separately.
- Original immediate sneak placement still runs on its existing client/server paths and consumes even in creative. No new permission check, alternate routing, or opportunistic inventory correction was added. The ordinary construction path retains its original server-only real construction and client-only preview cleanup.
- Recursive creative destruction keeps the original depth, input/output traversal, harvested/removed/destroyed order, packet correction, and cancellation behavior. Native `ForgeHooks.onBlockBreakEvent` returns an experience integer, with -1 cancellation; actual Forge events, spectator rejection, positive experience, cancellation, and correction packets are tested.
- The original mixed line endings in ItemConveyorWand remain unchanged outside necessary edited/inserted lines. CRLF-aware diff checks pass.

## Moving engine behavior retained

Both complete engine owners run against the actual frozen conveyor owners. Tests cover actual loose-item conversion followed by engine travel, snapping, metadata/neighbor behavior, entry/falling direction and priority, native no-clip movement, pickup, hit, leave, copy/alias semantics, and spawn success/rejection/exception ordering. Death-before-spawn and callback-before-death order remain as originally written. Partial inventory insertion does not opportunistically delete or shrink the original moving stack when the insertion call reports false.

The original explicit tick-counter increment and `<=5` condition remain. Actual native World dispatch increments as well: the tested server counter sequence is 2/4/6, with movement/departure reached on the third World update. Calling onUpdate directly instead reaches the gate on its sixth call; the proof does not confuse these two paths.

Native ITEM_STACK synchronization is paired with a HAS_ITEM boolean. Only missing/uninitialized or explicitly null state yields the original fresh stone fallback. A valid stack consumed down to zero remains present-empty across repeated getters, native dirty/full metadata packets, client cache, pickup, hit, and leaving the belt. It cannot manufacture stone. Native serialization can normalize an empty stack object's item identity, but its empty accounting is retained.

The `Item` NBT compound key and valid native item/count/damage/custom tags survive actual serialized save/load. Invalid stored stacks still kill the entity. Arbitrary 1.7 numeric-item-ID world import/remapping is not claimed by native 1.12 string-ID ItemStack construction.

Client position interpolation, remembered velocity, ignored yaw/pitch/teleport inputs, and cache identity/equality updates are tested through the native callback and data-manager APIs.

## Shared dependency closure

WorldInAJar retains its original coordinate block, signed-short metadata, and tile arrays and public storage access. Native IBlockAccess methods expose actual metadata states, native air/side-solid checks, the original unchanged raw lightlevel, zero power, plains biome, and the required DEFAULT virtual world type. Bounds, no-op out-of-bounds writes, tile identity, reset behavior, and actual-metadata solidity are tested. No packed-fullbright value was invented.

The consumer audit finds 37 files referencing WorldInAJar, including jar scenes/actions/actors, the preview renderer, and debris rendering. Their existing coordinate storage calls remain available; their graphical implementations are not silently declared migrated. ILookOverlay changes only the event resolution getter and native GUI atlas constant. Faux BlockPos retains its mutable semantics, public API, and sibling enterBlock signatures; only TileEntity coordinate access and MathHelper.floor names change.

## Reproducible native evidence

- Engine: `tests/probe-native-moving-item-engine.py`, `tests/MovingItemEngineNativeTest.java`, and `tests/native-moving-item-engine-{notes.md,proof.log,scope.json}`. Final terminal result: **20,859 assertions**, **3,854 direct engine callbacks**, and **six real World-dispatched updates**. All 22 complete production owners/dependencies compile.
- Wand: `tests/conveyor-wand-workflow-*` probe, independent oracle, native test, notes, manifest, and terminal log. Final coherent result: **244,865 assertions** and **49,176 pristine routing scenarios**, with all 25 complete production owners/dependencies compiled, including the corrected synced-presence engine. The finite artifact index is `tests/conveyor-workflow-artifact-scope.json`.

The actual native event launcher is the already tracked `tests/ForgeEventNativeLauncher.java`, unchanged. Explicit unrelated NTM collaborators are registry identities, localization, MainRegistry's player lookup, the RenderOverhead handoff sink, the Crane base type predicate, and unused recipe/tool registration storage. No engine, conveyor block, wand, jar, overlay, or Minecraft/Forge class is substituted. Headless native world/player/network subclasses observe calls; real World/Chunk storage, stacks, NBT, metadata packets, and Forge event behavior run.

## Remaining gates

RenderOverhead's on-screen preview, RenderMovingItem, EntityMovingPackage, broad Crane owners, jar/debris graphical consumers, and entity registration remain unresolved separate roots. Full-mod compilation and live graphical/server acceptance belong to the parent checkpoint, not these selected tests. The separate ItemRenderBlock wand-rendering family is not edited here.
