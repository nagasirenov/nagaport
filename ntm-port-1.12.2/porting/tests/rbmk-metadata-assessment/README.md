# Metadata-only lifecycle counterexample, CP89 assessment

Base: `42dea8e6e3241d0c4ce4ba9c5459fab2bdfdc99b`.

**No production patch is proposed.** The remaining RBMK lid writes must not be converted to ordinary `World.setBlockState` without a separately justified lifecycle migration. The assessment also identifies a previously migrated, reachable legacy-press conversion using the same unsafe mapping. It does not claim that a user's save has lost anything, or that the complete machine/reactor port runs.

## Exact engine distinction

The original `World.setBlockMetadataWithNotify` calls `Chunk.func_76589_b`, not the full block replacement method. Actual original `apx.a(IIII)Z` bytecode compares the raw metadata, updates the section, and updates the existing tile's containing-block cache and metadata. It neither removes/invalidates the tile nor invokes a refresh predicate. The Forge1614 patch changes its `instanceof ITileEntityProvider` check into `block.hasTileEntity(newMeta)`; it does not add a refresh/removal operation to this method. Its separate changed `getTileEntity` implementation removes *already invalid* tiles, not valid tiles merely because metadata changed.

Native `Chunk.setBlockState` calls `TileEntity.shouldRefresh` on a same-block state change and calls `World.removeTileEntity` when it returns true. Native default non-vanilla tiles return `oldState != newState`. With a loaded chunk, real World/Chunk removal invalidates/removes the old tile and creates the new tile via the block's provider. It does not serialize/restore the old tile. Same-block changes do not run block `breakBlock`, so a machine's block-drop logic is not an inventory-conservation mechanism here.

This is a mod mechanic boundary, not the accepted observer, debug-world, or lighting platform differences. Comparing original and native *default shouldRefresh bodies alone* cannot establish a metadata-only migration, because the original metadata-only path never calls that predicate.

Exact method disassemblies, original symbol mapping, Forge patch contexts and input hashes accompany this report. They are generated from the already supplied jars; no platform code is altered.

## Executed result and its limits

Run from the module:

    python3 porting/tests/rbmk-metadata-assessment/run.py

`proof.log` records **104 checks**:

- Complete, unchanged production `BlockDummyable`, `MultiblockHandlerXR`, `WorldBlockState` and seven native dependencies compile. The native neighbor and native scheduled-tick callback each reach the real base `destroyIfOrphan -> fixLegacyMonoblock -> setMetadata` path for all rotations 2–5, promoting to 12–15.
- Genuine native `World.setBlockState`, `Chunk.setBlockState`, `World.removeTileEntity`, `Chunk.removeTileEntity`, and tile invalidation execute. A loaded native chunk is seeded with an ordinary non-vanilla marker tile. The tile identity changes, the original becomes invalid, and its marker integer 12345 and marker ItemStack of 37 diamonds are absent from the replacement. The original object still exists as a test reference; this is world-state loss, not deletion of its Java fields. Block break callbacks remain zero.
- Genuine original 1.7 World/Chunk/storage bytecode executes metadata-only 2–5 to 12–15, with flags 3, in both remote modes. A genuine original furnace tile retains its identity, validity, fuel counter 2205 and inventory-stack identity. Original raw canonical nibbles are preserved.

The native block subclass supplies only a matching legacy-upgrade opt-in/provider contract and a marker tile; it is **not** either complete production press or RBMK owner/tile. Its marker stack is not a tested machine inventory implementation. The complete current base callback and engine mutations, rather than a rewritten callback, do execute. The original runtime uses original vanilla bytecode; Forge1614 changes are audited from its archived patch, **not** executed in a launched original Forge world. MainRegistry and CompatExternal are explicit surrounding collaborators, as in the existing dummyable suite. Network notifications and broad light propagation use the pre-existing observation seams. No full build, disk save migration, launched server, gameplay session, or full reactor/press tile execution was run.

The loaded-chunk flag matters: an initial unloaded-chunk fixture retained its map entry even after the world removal request. The reported counterexample deliberately uses `Chunk.markLoaded(true)` to represent the loaded chunk in which the real server callback is reachable. It does not extrapolate native removal behavior to unloaded chunks.

## Already migrated source reachability

The only live caller of `BlockDummyable.setMetadata` is `fixLegacyMonoblock`; the other two textual references are comments. The full source has exactly two overrides opting into this fix:

1. `MachinePress.isLegacyMonoblock` returns true for an existing `TileEntityMachinePress`.
2. `MachineEPress.isLegacyMonoblock` returns true for an existing `TileEntityMachineEPress`.

Both tiles' actual `update()` methods explicitly schedule their current block for a tick with delay 1 while its metadata is below 12. Independently, a native neighbor callback reaches the same check. The server-side base checks for a missing expected adjacent same-owner block and a loaded neighborhood, then invokes the opted-in upgrade. A legacy lone press with its real tile and metadata 2–5 meets those source conditions. No production owner in this inheritance chain overrides `shouldRefresh`.

**Source-derived affected state, not full-machine runtime observations:**

- Mechanical press: four inventory slots (fuel, stamp, input and output), inherited custom name/muffled state, and persisted `press`, `burnTime`, `speed`, `isRetracting` fields would be replaced by constructor defaults.
- Electric press: five inventory slots (including charge and upgrade), inherited custom name/muffled state, and persisted `press`, `power`, `isRetracting` fields would be replaced by constructor defaults.
- The normal state-change path does not call the base inventory-spill `breakBlock` hook. Thus no normal drop path compensates for that replacement.

This is a concrete source-reachable risk for already migrated `fixLegacyMonoblock`, supported by the whole-base runtime counterexample. Whether an actual imported press save is accepted by the unfinished port and reaches this callback is **not executed or established** here. Zero-valued/default legacy tiles have no valuable contents to lose; the hazard is specifically existing nondefault state.

## CP43 extra connectors and MultiblockHandlerXR are not the same path

Pristine source and the archived original mod classfile both show `makeExtra` and `removeExtra` used **full `World.setBlock(..., this, changedMeta, 3)`**, not metadata-only writes. Their metadata-only lines were comments. `MultiblockHandlerXR.fillSpace` likewise used full setBlock; `emptySpace` used setBlockToAir. Thus their already migrated methods are **not implicated by this particular no-shouldRefresh distinction**. The existing CP43 extra-upgrade/downgrade fixture correctly covers a full-write-family transition. This assessment does not newly certify every removal side effect or every connector descendant, but it does not retract that narrowly described result.

The CP43 README is qualified accordingly: the generic phrase "changed-metadata transitions agree" should not be read as certification of `fixLegacyMonoblock` or any remaining metadata-only writer.

## Remaining RBMK pair

`ItemRBMKLid.onItemUse` installs a lid; `RBMKBase.onScrew` removes one. `BlockDummyable.findCoreRec` enforces current source-owner identity and RBMK descendants do not override core lookup. Constants yield canonical values 12 (none), 15 (standard), 13 (glass), with flags 3. These are useful, closed metadata values; range/state representation is not the blocker.

However, both original writes are metadata-only. `TileEntityRBMKBase -> TileEntityLoadedBase -> TileEntity` has no refresh override. Its actual `invalidate` removes the neutron node, and its persisted base data include `heat`, `reasimWater`, `reasimSteam`; descendant inventories and other component state may also exist. A naive native conversion would therefore expose the same refresh/recreation behavior. That reactor consequence is **source-derived**, not executed against a complete RBMK tile in this suite. The current two old calls remain untouched, so this assessment does not assert a new RBMK regression was introduced by this work.

A blanket `shouldRefresh = oldBlock != newBlock` override is not offered: it would alter other full-write paths as well. A proper fix needs a scoped semantic design and proof of both the old metadata-only operation and the owner's other replacement/lifecycle contracts. No universal metadata helper, raw mask, forced tile retention, or new state architecture was added.
