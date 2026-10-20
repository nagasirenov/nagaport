# Machine native naming boundary

2026-10-19. Bounded continuation of the inventory consumer audit, after sided callbacks.

## Production scope and parity

162 whole production files changed: MachineBase plus exactly 161 owned default-name declarations in its 182-descendant closure. Default declarations become public `getDefaultName`; all original string literals and conditional bodies remain exact. CustomMachine retains config-null INVALID and nullable configured name; RBMKConsole and TurretBrandon retain their original null defaults.

MachineBase implements native getName, hasCustomName, and getDisplayName. Two narrow root wrappers, getInventoryName and hasCustomInventoryName, remain because existing GUI/container consumers call them. Native display uses real TextComponentString for custom values and TextComponentTranslation for defaults. Nonempty whitespace still counts as a custom name. Empty/null reset naming without changing storage or introducing a clear-inventory callback.

Battery owns its independent private customName and dispatches its fallback to getDefaultName, avoiding reciprocal-wrapper recursion. RTG owns private name and retains its literal fallback rather than changing it to a virtual default hook. Its original NBT methods do not persist that private name: this existing behavior is intentionally not repaired. Original setCustomName, readFromNBT, writeToNBT and field ownership remain byte-identical for all three owners. FENSU inherits Battery naming without its own override.

The audited external getName candidates concern fluids, config/casing objects, or files, not renamed machine hooks. The independent StrandCaster getInventoryName/getName pair belongs to FoundryCastingBase, outside this closure, and remains untouched. No storage/EMPTY, NBT, energy, recipes, lifecycle, open/close, usability or independent-inventory changes belong to this migration.

## Fail-closed evidence

`check-machine-naming-source.py` verifies the 182-descendant closure, exact 161 owned hooks, 162 whole-file reversals and 162 deliberate drift rejections. `machine-naming-source-manifest.json` freezes before/after hashes and exact reversible edits. Apply naming reverse_source BEFORE sided reverse_source in historical regression views; never regenerate the earlier sided/NBT/tick baselines. No Git objects or pristine sibling checkout are needed.

Byte-preserving reads retain original CRLF in DishControl and PneumoTube. Git may report their changed declaration lines as whitespace warnings; normalizing these files would violate the frozen whole-file proof.

## Native executable probe

`machine-naming-native-test.py --java-home <Java 8 home> --classpath <resolved native classpath>` compiles actual extracted production naming methods and every default hook against real native IInventory, InventoryBasic and text components. InventoryBasic supplies unrelated inventory fixture context; complete machine classes are NOT compiled. The probe preserves each exact extracted hook and independently compares it to its frozen predecessor body.

Final result: 5,888 assertions cover all 161 default hooks, default and custom display component types/contents, empty/null/custom/whitespace/Unicode names, dirty calls, custom config branches, independent root/child fields, inherited FENSU naming, and explicit subclass dispatch including RTG's intentionally fixed literal fallback.

Actual final command, full resolved classpath, classpath hash/native jar hashes, output and exit code are stored in `porting/continuation-2026-10-19/machine-naming-native-probe.log`. This was an actual direct Python/Javac/Java invocation, not Gradle. The optional `porting/machine-naming-probe.init.gradle` provides `testPortMachineNaming` with configurable `-PportPython` and the project's resolved compile classpath; it was not run for this batch. The Python runner uses UTF-8, platform Java executable suffixes and os.pathsep.

Full production compile, registered-machine execution, and client/server gameplay acceptance remain parent-owned gates. Passing extracted naming tests does not establish those outcomes.
