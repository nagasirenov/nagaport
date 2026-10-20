# Native TileEntity ticking contract

Baseline: checkpoint4 `1160f7eec0257d77d6adaefed0aba88e7d86aacd`. The portable JSON manifest stores payload/scope fingerprints; validators require neither this git object nor a sibling checkout.

## Exact migration

- 336 genuine no-argument `updateEntity()` declarations renamed to native `update()` in 336 files. The commented YellowBarrel declaration remains unchanged.
- 133 first-declaring source roots implement native `net.minecraft.util.ITickable`: 77 direct LoadedBase children, 51 direct TileEntity children, three CrateBase children and two LockableBase children. FurnaceSpace already inherits native ITickable from vanilla Furnace and only changes its override name.
- All 434 named tile descendants are classified: 379 inherit genuine tick declarations, while 55 remain non-ITickable. LoadedBase, LockableBase, CrateBase, InventoryBase and ProxyBase remain non-ticking shared bases.
- 58 meaningful super calls renamed in place. Only the OpenComputers paintable cable's call to inherited vanilla no-op was removed. RBMKRod's two calls stay within their separate branches. No early-return, side guard or call ordering changes.
- Three abstract declarations retained: TickingBase, MachineBase, PileBase. Existing empty overrides in DysonConverterTU and MachineIGenerator retained without adding bridges or new no-op bodies.
- 14 obsolete `canUpdate()` methods removed, all originally constant false with no callers. Their 18-class suppression closure lies entirely outside ticking ownership; all chains terminate at native TileEntity, not another vanilla ticking class.
- 12 unrelated hazard `updateEntity(EntityItem,float)` declarations and the HazardSystem call remain unchanged. These are 13 occurrences, not 13 declarations. GaugeUnit/KeyUnit update helpers and AssemblyFactory's update(boolean) overload are untouched.
- Exactly 350 production files changed. NBT writers, inventory representation, serialization, coordinates and other migration roots are outside scope.

## Portable checks

`python3 porting/tests/check-tile-tick-migration.py` checks complete source ownership and static/tick inheritance, native signatures, abstract declarations, exact method bodies and hazard dispatch. Its canonicalizer strips whitespace only outside string/character literals and comments. It detects changes to embedded literal spaces and escape sequences. The manifest records both pre-change and native body fingerprints.

`--strict-snapshot` additionally certifies exact post-migration bytes for all 350 production files. Use that for auditing this migration, not as a permanent prohibition on unrelated future edits. The default method-level regression permits unrelated file edits. Existing `check-tile-nbt-migration.py` default regression also passes after these tick edits; its old strict snapshot is intentionally not reusable across unrelated changes.

`./gradlew -I porting/tile-ticks-probe.init.gradle testPortTileTicks` first runs source checks and then compiles/runs TileTickSchedulerTest using the actual project mapped Forge classpath. It does not change production source sets, exclude failed files, compile fake Minecraft classes or claim to compile all production tiles. The standalone runner supports `JAVA_HOME` and an explicit actual `MINECRAFT_CLASSPATH` without Gradle.

## Native scheduling evidence and limitations

The Java 8 test invokes the actual Forge 1.12.2 World.addTileEntity and World.updateEntities, native TileEntity and native Chunk. A controlled in-memory World fixture initializes storage/bootstrap fields via reflection/Unsafe rather than starting save/network services; the admission and scheduling methods are not overridden or copied.

Assertions cover static exclusion with onLoad, world binding, inherited dispatch, super-call order, early return, deferred admission and next-pass ticking, invalid removal, unloaded-chunk suppression/resumption, missing-world suppression and border suppression. This is genuine scheduler API evidence, not full production tile compilation, live machine gameplay, chunk save/reload or network lifecycle acceptance. In particular, FurnaceSpace and ElectricFurnace still have unrelated legacy APIs requiring further migration. ElectricFurnace inherits this native contract through MachineBase.

Target jar inspection confirms native TileEntity has neither updateEntity nor canUpdate; vanilla Furnace implements ITickable; World chooses ticking admission by instanceof ITickable and preserves native invalid/world/loaded/border guards. No broad legacy adapter or custom scheduler is introduced.
