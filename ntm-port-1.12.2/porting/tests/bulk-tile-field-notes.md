# Native inherited TileEntity field batch

## Fixed scope

Baseline: checkpoint19 `07fe9fadc127801648b1897663e2d453ebf3ae88`.
The manifest authorizes exactly **10,939 field reads in 384 production files**:

- 2,751 bare inherited `worldObj` reads in 362 files become `this.world`.
- 8,188 inherited coordinate reads in 359 files become
  `this.pos.getX()`, `this.pos.getY()` or `this.pos.getZ()`.
- Coordinate totals: 2,746 X, 2,698 Y and 2,744 Z. This includes 533
  explicitly qualified `this.xCoord/yCoord/zCoord` reads.
- All 434 named TileEntity owners remain unchanged. There are selected nested
  named TileEntities in 38 files. No new runtime aliases, legacy fields, dynamic
  getters, world-method overload shims or machine algorithm rewrites are added.

## Exclusions and native ownership

World-name locals/parameters exclude the whole world field family in Tesla,
FusionKlystron and CrateBase (23 tokens). Coordinate-name locals/parameters
exclude the coordinate family in Multiblock, ReactorControl and FusionKlystron.
Private domain `pos` fields exclude coordinate replacement in RBMKRod and
RBMKRodReaSim, including their inheritance branches. Coordinate exclusions total
210 tokens. Other field families in these files remain eligible when safe.

The completed Brick/Iron/Steel thermal files are explicitly excluded. All other
receivers are excluded, including 132 arbitrary receiver tokens inside proven
tile owners. The batch does not infer `tile.xCoord`, Entity coordinates, Vec3d
coordinates or custom position APIs from a variable name. Anonymous-class and
nested non-tile owners are excluded. No anonymous-class candidate was present.
No selected token is a write, increment/decrement, declaration or shadowed field.

Mapped native TileEntity declares protected World `world` and BlockPos `pos`.
Native TileEntityFurnace/TileEntityLockable add no shadowing fields. `getWorld()`
is overridden in AirPump; direct field replacement intentionally preserves
nonvirtual access rather than changing dispatch. There are no zero-argument
`getPos()` overrides in the audited source closure, but the migration does not
rely on that fact to substitute getters.

## Portable source guard and transaction utility

`bulk-tile-field-migration.py` reads/writes UTF-8 bytes without newline
normalization. The JSON records full before/after SHA-256, exact old/new token
spans, original owner and line, counts, excluded counts, closure membership and
untouched Java source hashes. Apply/reverse validate every source file and the
whole ownership plan before writing. They recognize only exact before/after
states, permit safe resume/no-op, atomically replace each file, and attempt to
roll back this invocation's completed writes if a later write fails. They do not
overwrite concurrent unknown edits. Do not run with Python `-O`.

From the project root, using one Python 3 executable:

    python porting/tests/bulk-tile-field-migration.py verify
    python porting/tests/bulk-tile-field-migration.py verify --strict-untouched
    python porting/tests/bulk-tile-field-migration.py self-test

Historical developer operations, not needed by recipients of migrated source:

    python porting/tests/bulk-tile-field-migration.py dry-run
    python porting/tests/bulk-tile-field-migration.py capture
    python porting/tests/bulk-tile-field-migration.py apply
    python porting/tests/bulk-tile-field-migration.py reverse

Dry-run/capture require the old source spelling. Capture refuses to overwrite an
existing manifest and must never be used to refresh historical expected hashes.
Verify checks selected files and ownership by default; `--strict-untouched` adds
all other Java source fingerprints. Both source states can run the in-memory
self-test, which checks 384 exact inverses and 384 added-byte drift rejections.
`--root` and `--manifest` accept explicit paths. No Git objects are needed.

History integration API:

- `reverse_source(relative, text, manifest=None)` restores only recorded tokens
  after checking full selected-file state; unrelated paths pass through.
- `verify(transform=None, root=ROOT, manifest=None, strict_untouched=False)` accepts
  an optional exact inverse for a newer migration, then checks this layer.

The parent owns integration with historical thermal, world, coordinate, inventory,
NBT and tick audits. This inverse must run before their older payload comparisons;
old fingerprints must not be regenerated. The immutable manifest is cached per
process, so whole-project historical scans do not repeatedly parse its 4.3 MB.
Callers must not mutate the cached object; an explicit manifest argument bypasses
loading, and a deliberate in-process manifest replacement requires
`read_manifest.cache_clear()`. Reconstructed legacy source is only an
audit view, never a compilation input or runtime compatibility layer.

## Actual-native execution

    python porting/tests/bulk-tile-field-native-probe.py --java-home JDK8_PATH --classpath TARGET_COMPILE_CLASSPATH

The runner handles Windows executable suffixes and classpath separators, compiles
with subprocess argument arrays, and uses a temporary working directory. The
classpath is the project's actual mapped target classpath, not a fake Minecraft
implementation. Windows execution remains untested.

The probe compiles seven verbatim migrated expressions from AirPump, the nested
Geyser tile and VolcanoV2, plus the complete existing production ForgeDirection.
Fixtures extend actual TileEntity, TileEntityFurnace and World; only unused World
chunk-storage services are controlled. It executes 7,121 assertions covering:

- Direct/inherited/nested/Furnace field storage, initial null/origin, World
  identity/rebinding/null, client/server direction and unchanged null failure.
- Full signed coordinates, integer-overflow-before-double promotion, actual
  native NBT coordinate persistence and immutable native `setPos` snapshots.
- Deliberately misleading overridden getters, proving direct field reads do not
  dispatch virtually; distinct subclass domain fields and outer non-tile state.
- Exact Volcano cast, long arithmetic, direction and time expressions, including
  one World time invocation even though the original `% 1` result is zero.

Source and native evidence is saved under
`porting/continuation-2026-10-19/bulk-tile-field-source.log` and
`porting/continuation-2026-10-19/bulk-tile-field-native.log`.

## Explicit limit

This batch is native field API/source parity plus executed sampled expressions.
It is not complete compilation or execution of 384 production machines, native
World-overload closure, live lifecycle/save migration, client networking or
original gameplay acceptance. The parent owns fresh common compiler evidence
and reports remaining independent blockers separately. No CE/balance changes.
