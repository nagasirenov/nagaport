# Native TileEntity World field migration

## Bounded production change

148 explicit inherited `this.worldObj` reads in 92 named TileEntity owners / 92 files become `this.world`. The scope includes eight block files containing nested named TileEntities. All other bytes, including CRLF, comments, literals and preexisting indentation, are retained. No assignments, unqualified identifiers, other receivers, Entity/Particle/WorldProvider accesses, custom World fields, compatibility aliases or runtime shims are migrated.

The portable manifest records each owner and original offset, pre/post SHA-256, exact native receiver lines and 560 excluded receivers. All 3,767 untouched Java files have snapshot fingerprints. The existing hierarchy scanner supplies the 434 named TileEntity closure. Additional guards reject field-shadow candidates and anonymous-class receivers. The original/pristine checkout is not modified or needed to run the checks.

## Pinned native API

Mapped artifact: `.gradle/minecraft/forgeBin-1.12.2-14.23.5.2768-PROJECT(ntm-port-1.12.2).jar`.

SHA-256: `8c75037188f87bc62939eefa03c2480551509034f069728de012c09276f8f49e`.

Java 8 `javap -private -c` confirms `TileEntity` declares protected `World world`; `getWorld()` directly reads it, `setWorld(World)` directly writes it, and `hasWorld()` tests it for null. `TileEntityFurnace → TileEntityLockable → TileEntity` adds no shadowing field.

## Source audits and historical baseline bridge

- `python3 -B porting/tests/native-world-field-audit.py` is the strict migration snapshot: restoring only the 148 recorded tokens must recreate exact pre-change bytes, with all excluded Java sources unchanged. Future authorized source edits may intentionally invalidate this full snapshot.
- The reusable tick checker calls `verify_bridge()` instead: it validates the same receiver ownership/count, all exact native receiver lines and excluded legacy receiver lines, but permits changes to unrelated lines/files. It reconstructs historical spelling only for existing tick payload comparisons. No baseline hashes are regenerated, no broad regex normalization is used, and no production/probe compilation sees the historical spelling.
- The tick checker's `--strict-snapshot` still checks actual source bytes against its original historical migration snapshot. It is expected to reject subsequent authorized world-field changes; it is not silently normalized into a new snapshot.
- Existing reusable NBT source audit passes without modifications or writer-payload changes.
- In-memory adversarial checks accept an unrelated source comment and reject changed owned time arithmetic, an excluded custom-field rename and a newly shadowing native field. These checks do not modify production files.

## Native behavior probe

Run through the current project's actual compile classpath:

`./gradlew -I porting/native-world-field-probe.init.gradle testPortNativeWorldFields`

Or invoke `native-world-field-probe.py --java-home <JDK8> --classpath <actual mapped compile classpath>` directly. The init task does not exclude or modify production source sets. The direct isolated run passed 28 assertions against actual Forge/Minecraft classes:

- Native field/accessor identity across direct, indirect, nested named and Furnace inheritance, initial null, rebinding between distinct Worlds, and null rebinding.
- Actual native World admission binds the field before `onLoad`; deferred admission and next-pass ticks observe the exact same World. Clearing World suppresses native ticking.
- Four verbatim production expressions are extracted from FurnaceSpace (server branch), LoadedBase (buffer equality/time short circuit), FileCabinet (RNG pitch arithmetic), and FurnaceBrick (forwarded World argument). Checks preserve World identity, client/server direction, null behavior, time boundary behavior, short-circuiting and exactly one RNG/time read where required.

The World storage/bootstrap services are controlled fixtures using real World/Chunk classes. The test-only objects are not production compatibility implementations. Existing native scheduler tests remain separate and can be run with `testPortTileTicks`; the new probe does not rerun that suite.

## Limits

This batch does not port legacy coordinate APIs, World method overloads, dimension access, sounds, unqualified `worldObj`, or `getWorldObj()`. Native fixtures and extracted-expression compilation do not establish full production tile compilation, machine gameplay, save migration, networking or full original parity. The parent owns the subsequent clean full compile and reports those results separately.
