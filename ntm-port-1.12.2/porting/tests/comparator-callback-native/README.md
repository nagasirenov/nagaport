# Nine paired native comparator callbacks

Frozen input: CP80 `a0dc7d14f453a1d6f01b0d5abdcd455358bf2714`.
Production scope is exactly nine presence and nine value callback signatures, with three
coordinate locals introduced at each value entry. All value bodies, arithmetic, casts,
null behavior, core discovery, metadata gates, notifications and resources stay unchanged.
No visual/geometry/lighting change. No no-argument comparator declarations remain.

Owners: BlockStorageCrate, BlockMassStorage, GeigerCounter, MachineBattery,
MachineBigAssTank9000, MachineFluidTank, MachineReactorControl, BlockGraphiteFuel,
MachineBatterySocket. None has a production descendant. Project callers already use
native state or block-state signatures (RadioTorchBase and TileEntityRadioTorchSender).
BlockDummyable has no comparator bridges. Its remaining no-argument opacity/fullness
methods DO already have native bridges: this is why that initially suggested root was
not modified.

Old Forge1614 MCP mappings document presence/value as a pair. Legacy `aji.M()` and
`aji.g(World,int,int,int,int)` are invoked by the old comparator engine. Native
`BlockStateContainer.StateImplementation` calls the matching state-aware Block methods,
and native BlockRedstoneComparator calls those state methods. The unused side argument
is removed only after checking every selected method body. Evidence: legacy-mapping.txt,
legacy-engine.log, original-owner-callbacks.txt, native-api.log, input-sha256.json.

Run from any working directory:

    python run.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt
    python audit-engine.py

`run.py` extracts the exact complete callback methods from production and the frozen
baseline; it does not rewrite their bodies or replace Minecraft classes. Owner shells,
findCore results and domain tiles are explicit bounded fixtures. Native World is a test
subclass providing a source state and tile, not a fake platform API. Native Block,
IBlockState, Container inventory calculation, chest, MathHelper, and comparator input
calculation are real Forge/Minecraft classes. The complete production WorldBlockState
helper is compiled unchanged. The original method side parameter is tested at side 5;
its non-use is checked structurally in each body, and the native comparator is exercised
in all four horizontal orientations.

PASS: 81,033 assertions; 20,160 cases covering nine owners, metadata 0–15, core absent
or present, null/wrong tiles, integer/domain values, fractional radiation, original
failure types, the graphite divide-by-zero edge, and empty/partial/full chest inventories.
Queries retain coordinate and order equivalence. Compiler negative control rejects all
18 original signatures as invalid native overrides. Runtime mutants reject capability
loss, x/z substitution, and battery value loss. Full class compilation/gameplay,
neighbor notification scheduling, and real multiblock findCore implementations are NOT
claimed by this bounded extraction test; those remain owned by broader port validation.
The full project compile is independently run by the coordinator.

baseline.zip plus selection.json freeze all nine original files and exact operations.
The source audit reverses changes to byte-identical CP80 files. `--allow-unrelated` permits
integration of independent source changes while still requiring byte-identical original
complete callback methods after inverse; its receipt marks the full-file inverse false
when unrelated edits exist. apply-fragments.py applies exact guarded fragments only.
Production patch: cp81-comparator-production.patch supplied outside repository.

Historical native-tile-lookup/ProductionMethodsTest.java has three invocation-only
updates for the newly native crate signature; its complete method extraction remains.
