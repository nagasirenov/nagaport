# Loaded paint-removal notification boundary (CP88)

Base: `c2f60149e9a9fb836bade505fe566d40a54ef3cc` (CP87). This is a deliberately finite **five-call subset**, not a translation of all 43 missing World notifications.

## Production changes

Exactly one `onScrew` paint-removal notification changes in each of:

- `BlockCablePaintable`
- `BlockOpenComputersCablePaintable`
- `PneumoTubePaintableBlock`
- `FluidDuctPaintable`
- `FluidDuctPaintableBlockExhaust`

Each now calls the NTM-owned `TileEntityBlockUpdate.notifyAt(World,int,int,int)`. The helper captures the position once, reads the actual stored native state once, and invokes native `World.notifyBlockUpdate(pos, state, state, 2)` once. It does not write a block, reconstruct/default its metadata, mutate a tile, call neighbors, relight, generate replacement packets, suppress listeners, use reflection, or introduce a fake World/Forge API. The same immutable stored state is supplied as both old and new because this operation changes paint in a tile, not the block state.

The original receiver and each coordinate are still evaluated once in their original order. `pipe.block = null` (or `tube.block = null`) stays before notification; `markDirty()` stays after it. No item, fluid, network, ownership, paint eligibility or return formula changes. In particular the pneumatic hand-drill branch still returns false after doing its work; it was not incidentally “fixed.” All other bytes of these five files invert exactly to their CP87 originals, including line endings.

## Why this subset is loaded and side-effect bounded

The repository has exactly three `.onScrew` dispatchers: ItemTooling, ItemBlowtorch and ItemBoltgun. All first read the same-position block using the real `WorldBlockState.read`. Only ItemTooling's screwdriver/hand-drill types can reach these selected branches; TORCH and BOLT do not pass their predicates. ItemTooling does no callback or world mutation between that block read and the onScrew dispatch. The original coordinate bounds select AIR outside the old domain, so no selected owner is dispatched there. In ordinary single-threaded native item interaction, the same chunk remains loaded.

The selected onScrew branches then obtain the same-position tile. After its successful type check, only direct field reads/casts and clearing the paint field precede the notification. No intervening Item.getDamage or external color hook is admitted. The native `getTileEntity` pending-tile path was inspected: a tile lookup alone would not universally prove a chunk was loaded, which is why the earlier actual block read is part of this closure.

All five stored owners' full source files are guarded. Cable, OC Cable and Pneumo directly extend native BlockContainer; the two fluid owners share FluidDuctBase, which also directly extends BlockContainer. None overrides native collision/bounding-box methods, FluidDuctBase does not either, and there are no source subclasses of the five concrete owners. Their native collision path returns native FULL_BLOCK_AABB. The common metadata state methods and the genuine production MultipassContext extended-state container are compiled in the bounded fixture. Thus the new native PathWorldListener's two collision-box reads are pure for these owners, return the same box, and do not update navigation.

This is a normal registered-owner/item-interaction contract, not support for arbitrary direct/manual callbacks, concurrent unloads, externally substituted states/providers, addon subclasses, or reflection replacing public registry fields. The helper is explicitly **not an unloaded-world API**. It must not be broadened to an unproven call. Calling it on an arbitrary unloaded position would cause native getBlockState to request a chunk; no fake AIR/null state, silent skipping, precondition crash, or listener filtering was added to hide that difference.

## Original and native engine behavior

The original authentic 1.7.10 `ahb.g(III)V` (World.func_147471_g) simply iterates IWorldAccess listeners with coordinates. It reads no chunk, checks no coordinate bounds, and does not itself mutate blocks or notify neighbors. Actual original RenderGlobal expands the dirty range by one block in every direction. Actual WorldManager forwards to PlayerManager, which looks up an existing watched entry, coalesces duplicate local positions and marks the section for later packet work.

The native actual World listener loop receives World, BlockPos, old/new states and flags. Native RenderGlobal retains the same one-block dirty margin; flag 2 leaves immediate-render bit 8 unset. Native ServerWorldEventHandler forwards to the actual PlayerChunkMap. The actual map/entry queues and coalesces the position, then entry.update creates a block-state packet plus the tile's update packet. This preserves the paint-removal packet purpose and deferred timing rather than sending a bespoke early packet. Notifications to an unwatched loaded position do not create a watched entry or send packets.

There are genuine native platform differences, not universal cross-version equivalence:

- Native notifications have state/flag arguments and the built-in PathWorldListener; original notifications do not. The additional same-state collision reads are tested and bounded as above, not suppressed.
- Native watched-entry sent-to-player gating and native render scheduling/packet implementations remain native.
- Original Forge1614 modifies PlayerManager's dynamic update array/clumping threshold, metadata-sensitive `hasTileEntity`, and the tile-packet loop. These patch files are retained in full. The small duplicate/single-position original oracle is within the unmodified queue behavior. The native test actually exercises its metadata-sensitive tile packet path. It does not claim identical cross-version whole-chunk batching or wire payloads.
- The authentic Forge/FML World patches modify callers of func_147471_g, but not that method's listener-only body. The Forge RenderGlobal patch does not alter the selected notification methods. No WorldManager/IWorldAccess patches exist in those archives.

`dependency-hashes.json`, complete extracted Forge/FML patches and `boundary-bytecode.txt` retain provenance. Tests verify authentic original mappings and byte-identical patch archive contents. Original Minecraft method instructions are never edited; only the test class is package-relocated to avoid collision with the signed original default package.

## Tests and exact claims

Run from the module:

    python3 porting/tests/world-update-notification-native/prove.py --attribute --mutants

The proof uses the existing native classpath, supplied vanilla/Forge originals and JDK8. It does not download, change dependency caches, invoke Gradle or compile the full mod into classes.

- 8,078 native assertions cover all 16 metadata states, both client/server world flags, multiple chunk edges and heights, actual Chunk storage, exact same-state listener identity, native path-listener collision reads, unchanged navigation, real RenderGlobal dispatch, real PlayerChunkMap/entry queue behavior, actual single-block and tile packet construction, virtual World dispatch and null/argument ordering.
- 907 authentic original assertions execute original World listener dispatch (including out-of-domain coordinates), original RenderGlobal margin forwarding, original WorldManager/PlayerManager duplicate queues, and evaluation failures. The old out-of-domain behavior is recorded as a limit, not falsely imposed on native state reads.
- The exact three selected production statements are mechanically extracted (only fixture variable/field names are renamed) and executed. The common three native metadata/state methods are also extracted verbatim and compiled with the complete real MultipassContext. These are honest focused projections, **not complete enclosing owner/item builds**.
- Full-source javac attribution, without code emission, proves the original five positions belong to exact World receivers with int/int/int arguments, finds all 43 old typed World calls, and resolves exactly five new helper calls. The original positions correspond to the CP87 missing-method diagnostics. Two positive and eight negative controls reject foreign/subtype, boxed/wrong-arity, unresolved and effectful receivers.
- Eight helper behavior mutants and deletion of each of the five calls are rejected. Exact full-file regeneration/inverse checks preserve all nonselected source bytes and pin the loaded-tool and shape closure.

Native fixture seams: unrelated world/renderer/server startup is bypassed where necessary using test-only Unsafe; rendering stops at a recording ViewFrustum; packet sending stops at a recording PlayerChunkMapEntry.sendPacket, after genuine packet construction; an existing-only chunk provider throws if generation is attempted. Native notification, path comparison, storage, map, entry and packet methods are real. No production Minecraft/Forge class is stubbed. Original server startup is likewise bypassed; the authentic old queue methods execute. Full OpenGL invalidation, network transport, original Forge launched runtime, owner construction beyond the focused state contract, full tooling callbacks, game sessions, save/reload, and full machine/energy/radiation behavior are not certified.

## Unchanged and deferred

The remaining **38 World.markBlockForUpdate calls** are byte-for-byte untouched. They include mutation/generation flows, ticks, packet callbacks and painting paths with extra intervening hooks; their loaded-state/collision closure is not asserted by this work. Seven direct `getPlayerManager().markBlockForUpdate` calls are a separate server-only API family and are also untouched. No source exclusion, placeholder, gameplay deletion, final build success or playable-JAR claim is made. The parent owns composition and the next full offline build.
