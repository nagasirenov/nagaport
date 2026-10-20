# TileEntity NBT return-contract root

Checkpoint3 (`fb4263a`) source audit found 434 named tile descendants, including one through vanilla TileEntityFurnace, and exactly 297 void single-compound overrides. All 297 now return NBTTagCompound and the supplied argument. The one early return in CustomMachine returns that argument after the existing superclass call. Hatch's pre-existing no-super read/write behavior is deliberately retained; this patch does not repair its missing native metadata. The 37 unrelated void serializers, including nested tank/gauge/key helpers, are excluded.

No payload, call order, keys, field types, read methods, ticking, inventory or null/EMPTY semantics were migrated. This signature root is a prerequisite, not a runnable ElectricFurnace implementation.

## Portable source checks

Run `python3 porting/tests/check-tile-nbt-migration.py` for reusable method-ownership/signature/payload regression. It permits unrelated edits elsewhere in each file. The manifest carries SHA-256 fingerprints of pre-change bodies, independent of local commit objects or a pristine checkout. Body comparison ignores whitespace only outside quoted literals/comments, preserves literal/comment content exactly, and removes only the added return plumbing; it does not prove behavior or original upstream provenance. Changes to serialization payload intentionally require reviewing/updating the baseline.

Run the same command with `--strict-snapshot` to additionally audit exact post-migration file bytes and all 37 excluded void method bodies/signatures. This is a one-checkpoint migration audit and intentionally fails after legitimate future changes to those files. It is not the default ongoing regression.

## Native probe

For the resolved project classpath, run Gradle with `-I porting/tile-nbt-probe.init.gradle testPortTileNbt` under Java 8. This diagnostic-only task leaves production source sets unchanged.

Alternatively run `JAVA_HOME=<Java 8> MINECRAFT_CLASSPATH=<real mapped Forge compile/runtime classpath> bash porting/tests/run-tile-nbt-probe.sh`. It uses isolated javac/java, not Gradle or production source-set changes. No fake Minecraft classes or production replacement adapters are used.

The generator extracts verbatim complete writer methods for LoadedBase, MachineBase, ElectricFurnace and Hatch into explicit test-only data fixtures using real TileEntity, NBT and ItemStack classes. It also extracts the complete CustomMachine null-type/config early branch, with an assertion guarding the unrepresented normal branch. The fixture superclass for that branch is Inventory, not the actual polluting CustomMachine parent. This deliberately bounded test does not validate CustomMachine's tanks, matcher, recipes, normal branch or complete superclass chain.

All concrete test tile classes are registered under `nbt_probe` IDs before writing. Real TileEntity refuses unregistered concrete subclasses with a missing-mapping exception; the fixtures do not claim the production `tileentity_electric_furnace` registration works. Capability data is attached through the real CapabilityDispatcher using reflection into TileEntity's capability field; event-based capability attachment is not covered. The provider supports real serialize/deserialize payload data but exposes no capability query interface.

Assertions cover same supplied-object identity via virtual dispatch across the extracted writer layers, registered concrete identity/BlockPos, retained caller tags, ForgeData, ForgeCaps serialization and native-base read, real ItemStack payload roundtrip, null slots, names, muffled/power/progress, both early-branch conditions, omitted cooldown, and Hatch's unchanged no-super behavior.

These are **method fixtures, not compiled production tile classes**. Native base reads are tested, not mod readFromNBT methods. Full MachineBase/ElectricFurnace runtime, inventory EMPTY migration, ticking, recipes, energy, block registration, world save/reload, client/server behavior and gameplay remain unverified and require later production completion.
