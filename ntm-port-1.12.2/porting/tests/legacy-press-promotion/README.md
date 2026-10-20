# Scoped legacy Press/EPress promotion correction

Production base: CP90 `0be133c9fb9f07053e26f081371487e641c95214`.
The four changed existing files are byte-identical at CP89 and CP90 before this
correction. `baseline.zip`, `manifest.json` and `audit.restore_base(path, bytes)`
provide a checked, complete-file inverse; the sole added helper inverses to
absence. Unrelated bytes are not normalized, replaced or ignored.

## Defect and bounded correction

The preceding `../rbmk-metadata-assessment` preserves the pre-fix counterexample
and exact original/native engine distinction. Original metadata-only writes do
not invoke `shouldRefresh`; native same-block state writes do. The previously
migrated `BlockDummyable.fixLegacyMonoblock` can therefore discard a loaded
legacy press tile, including its inventory, without invoking the block's drop
hook. This correction does **not** migrate the remaining RBMK calls.

Only the complete `MachinePress` and `MachineEPress` block owners opt in:

1. Their `fixLegacyMonoblock` overrides obtain the actual tile and wrap the
   unchanged superclass promotion only when its concrete press type matches.
2. Each corresponding tile owns a private `LegacyPressMetadataUpgrade`. It
   records one synchronous expected transition: the same tile, world, position,
   actual stored owner, before-state identity, after-state identity and thread.
   Arming checks the tile's validity, world/position and actual world membership.
   A foreign callback receiver cannot arm permission for another stored owner.
3. The new `shouldRefresh` consumes and clears that expectation **before**
   testing it. Only that exact transition returns false. All other calls invoke
   the existing native superclass behavior. There is no blanket same-block
   retention, global queue, NBT snapshot/restore, custom World implementation in
   production, private engine-cache mutation or replacement-state architecture.
4. `finally` clears unconsumed permission on no-op, rejected write or exception.
   An attempted nested scope before consumption revokes the outer permission
   rather than borrowing it. Ordinary notifications after consumption can
   perform new full writes, which still replace the tile.

`BlockDummyable` itself, its codec, metadata read/write body, flag `3`, callback
dispatch and full-write extra-connector paths are unchanged. The existing
private setter still converts the **actual stored block**, not the assumed
callback receiver. The scoped helper does not perform any write.

### Metadata domain

The opt-in predicate is a tile `instanceof`, not a rotation-range predicate.
In a lone loaded fixture, both ordinary base callback entries promote stored
nibbles 0–11; the base regards 12–15 as existing cores and does not enter the fix.
The protected promotion operation itself admits every stored nibble 0–15, and
the scope follows all of them: its exact native target is the existing owner's
`getStateFromMeta(m + 10)`, whose stored nibble is `(m + 10) & 15`.
No production restriction to ordinary rotations 2–5 has been invented.

The caller ignores the write's boolean result and this +10 transition always
changes the stored nibble. This does not certify arbitrary raw integer writes,
their changed/unchanged return quirks, arbitrary owners, pre-initialization
registry replacement or foreign/air metadata conversion.

## Engine ordering and actual lifecycle tests

Run from the module:

    python3 porting/tests/legacy-press-promotion/run.py --mutants

No installation, download, common/full build or game launch is performed.
The focused classpath excludes the two mutable ForgeGradle placeholder jars
(they contain no classes) and the two project build-output directories.
All 65 remaining dependency jars are hash-pinned; no dependency/cache is
modified. This avoids treating regenerated placeholder ZIP timestamps as an
engine change and prevents accidentally using unfinished project class output.
`proof.log` records:

- **104 unchanged counterexample checks**, executing the previous marker-tile
  fixture and original furnace fixture again. The unguarded generic base still
  exhibits the defect; it was not globally changed to retain tiles.
- **1,539 native scoped-promotion checks**: both actual block owners, neighbor
  and scheduled-tick entries, all 16 stored nibbles, direct protected promotion,
  retained object/inventory-stack/NBT identities, real inventory counts,
  custom name, muffled state, persisted machine counters and transient animation
  fields; zero removal/break calls during the admitted operation.
- Ordinary same-block **full writes with both +1 and the exact +10 metadata
  change** still invalidate/replace the tile. A full write after promotion does
  too. A foreign callback owner does not obtain retention and cannot replace
  the actual stored block identity with itself.
- A reentrant write from the native neighbor/observer notification seam sees
  the retained tile, then replaces it normally. The expectation is already
  consumed. Native client-update notification precedes that seam.
- Wrong tile, world, position, old/new state and thread controls; moved and
  invalidated tiles; pre-refresh nesting; exceptions; actual native same-state
  no-op, height rejection and DEBUG world rejection; unconsumed-scope cleanup.
- **224 actual original all-nibble checks**, in both remote modes, executing
  original World/Chunk/storage and an actual original furnace tile: original
  section nibble, raw tile-cache argument, validity, fuel and inventory identity.
- **11 runtime guard mutants rejected**: disabled retention, always-retain,
  persistent permission, omitted cleanup, wrong delta and omitted world,
  position, tile, before-state, after-state or thread matching.
- Five unrelated source-mutation controls rejected by complete-file inverses;
  finite opt-in/no-subclass/no-new-caller source checks; supplied engine hashes;
  reproducible 518-occurrence cache-reader inventory and traced-source hashes.

`native-world-promotion-order.txt` preserves actual native World bytecode.
The preceding assessment's `native-chunk-state-bytecode.txt` shows the section
write at bytecode 203 and server `shouldRefresh` at 260, before tile-cache update
at 563 and before returning to World notifications. Same-block identity skips
`breakBlock`. On this exact owner path no block `onBlockAdded` runs either.
The new consume method clears permission without calling World or an observer.
The runtime test uses the genuine installed native World/Chunk mutation,
removal, invalidation and provider paths, not rewritten engine mutations.

## Honest fixture and compilation boundary

Twenty-one complete production files compile, including complete
`MachinePress`, `MachineEPress`, `BlockDummyable`, `MultiblockHandlerXR`,
`TileEntityMachineBase`, `TileEntityLoadedBase`, the new guard, native world
boundaries and their selected interfaces/dependencies.

The two concrete **tile classes are exact lifecycle/field projections**, not
complete machine builds. The runner extracts all actual field declarations,
the exact constructor, exact `getDefaultName`, and the complete new guard and
`shouldRefresh` methods. It adds an explicitly throwing unused `update` method
and omits their unrelated processing/interface implementation. Their real
inventory bases and native TileEntity execute. Existing `stackSize` and other
unported processing dependencies remain unresolved. Retained fields and
inventory aliases are real fields of these exact projections; identity proves
no projection field is reconstructed during the selected transition.

The fixture subclasses complete production blocks only to expose the protected
callback and count break calls. Legacy tiles are deliberately seeded at old
metadata, representing the existing tile that the actual opt-in tests; the
native provider would not newly construct a press at every such old nibble.
Loaded native chunks are necessary and explicitly marked loaded.

The surrounding NTM-only collaborators are visible in `run.py` and the reused
`probe-machine-core.py` literal dictionary: atmosphere backend, dimension
provider, packet dispatch, DirPos, registry, core lookup, a constructor-only
proxy marker, ComparableStack and empty UpgradeManagerNT constructor.
BufPacket fields/constructor/encode/decode are exact source slices; its client
handler is not compiled here. Processing, proxy behavior, atmosphere, packet
threading and actual network delivery are not acceptance claims.

The native test World inherits the established actual-World fixture. Broad
lighting and notification delivery are observation seams; native mutation and
tile lifecycle are not replaced. Reentrancy executes at a notification seam,
not through a launched observer block. Original execution uses actual vanilla
1.7.10 World/Chunk/furnace bytecode and a bounded chunk-provider fixture; the
Forge1614 metadata-only patch is separately audited, not launched.

## Raw tile-cache and consumer boundary

For m=6–15, original metadata-only code caches raw m+10 (16–25), whereas native
TileEntity returns the canonical stored nibble (0–9). The all-nibble test
explicitly observes this difference. Native cache handling remains untouched.

`cache-audit/README.md` closes the finite mod-source consumers rather than
claiming the cache is unused. In particular:

- `RenderEPress` has three reads. All use the same switch on metadata-minus-10,
  with cases 2–5; both unequal domains take its default branch. Ordinary
  promotion is server-only and rendering uses the separate client tile.
- Ordinary BufPacket, persistent NBT and native block/chunk synchronization do
  not transfer this tile cache. Exact source/bytecode paths are preserved.
- The generic neutron-stream reader can read a press tile, but uses only bit 8,
  which agrees. Generic door/klystron paths are separately receiver-closed or
  have the same UNKNOWN/default arithmetic result.
- Inherited crash reporting **does distinguish** the raw integer: 16 is printed
  differently from 0. Public getter equality and arbitrary external-mod or
  reflective consumers are not claimed. This is an explicitly accepted native
  engine diagnostic boundary, not a reason to modify private cache fields.

The finite claim is preservation of the admitted promotion's tile identity,
inventory and transient state, plus the enumerated mod gameplay/render/
ordinary persistence/sync meanings. It is not full engine trace equality,
complete press processing/save-import acceptance or complete mod compilation.

## Composition and source guards

`audit.restore_base` accepts module-relative paths and bytes. Four exact
after-files restore to their complete archived pre-fix contents; the exact new
helper returns `None` to signal deletion. The base and tile processing methods
outside those insertions must match byte-for-byte.

Historical source guards must opt into this inverse explicitly rather than
deleting their old hashes or ignoring changed files. The old assessment's
standalone guard intentionally continues to describe its no-production CP89
snapshot; this suite reruns its unchanged executable counterexample without
falsely asserting that the current press classes have no refresh override.

The cache-reader manifest is a CP90-composed snapshot. A later unrelated
renderer or GUI edit to one of its 377 source inputs must be explicitly
inverse-composed or re-audited; this suite does not silently bless hash drift.

The finite CP91 composition is supported explicitly:

    python3 porting/tests/legacy-press-promotion/run.py --compose-colored --compose-replaceability

`cache-audit/composition-colored.json` pins the sibling `check_source.py`,
`transform.py` and manifest. Its exact intersecting file is RenderPumpjack;
the real sibling `reverse_source` restores that complete file.
`composition-replaceability.json` pins that sibling's checker and manifest;
its exact intersections are MachineDrain, MachineExcavator and
machine/oil/MachinePumpjack tile files, using the real `reverse` byte inverse.
Each resulting full file must still equal the original CP90 cache-audit hash.
The checker rejects an added unrelated edit on each composed file. The getter
inventory also runs on those restored bytes, not a regex that ignores edits.
All other inputs remain strict and uncomposed. Armor attributes and GUI Slot
coordinates have no intersecting source input in this audit.

`composed-main-proof.log` records the composed guard and runtime suite against
the parent's actual integrated main source. At that run the frozen
replaceability evidence was read from its sibling worktree with the explicit
`--replaceability-evidence` option, because its evidence patch was not yet
installed in main; its bytes are pinned identically. No parent source was
rewritten. `composition-negative.log` preserves the expected rejection when
only the renderer inverse, but not the three tile inverses, was requested.
