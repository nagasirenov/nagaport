# Player inventory indexed reads (CP86 candidate)

Baseline: sealed CP85 `abe006a0845275e8be52417967ac8ebf6f283a52`.
This is a bounded read migration, not a complete player-inventory or radiation integration.

## Production boundary

- 83 direct `InventoryPlayer.mainInventory` / `armorInventory` indexed reads now call the NTM-owned `PlayerInventoryRead.get(NonNullList<ItemStack>, int)`.
- 19 array-length reads use the native list's `size()`.
- One read-only alias in the complete `ArmorUtil.checkForFaraday` method captures the same native armor list rather than an array. Its eight fixed-index reads use the same helper. No snapshot or copied/view-backed collection is introduced.
- Exactly 31 existing production files are modified, plus the one new helper. All 21 assignment LHS expressions remain unchanged in this isolated batch. No production imports, other branches or callback bodies are changed.

The helper reads exactly one list entry. Only `stack == ItemStack.EMPTY` becomes null. Every other reference, including a zero/negative-count, invalid-item, air-item, invalid-NBT or tagged empty stack, is returned unchanged. Null is not rejected or copied if an out-of-domain custom list returns it. It does not call `isEmpty()`, restore old `ItemStack.getItem()` semantics, normalize malformed stacks, mark inventories dirty or change native IInventory methods.

Callers pass the already-evaluated field, not an InventoryPlayer/EntityPlayer owner. Java therefore captures the receiver/list before the index expression, reads the entry after that index completes, and preserves short-circuiting and repeated-read behavior. The Faraday method also preserves its original capture-before-predicate behavior if a predicate changes the owner's inventory reference.

The domain is the actual native fixed-size 36-slot main and 4-slot armor lists, not native combined inventory indices (41 slots including offhand). All lengths and reads preserve that separation. No offhand behavior is added.

## Evidence

`source-manifest.json` records each exact old/new token, original source location, before/after whole-file SHA-256, the 21 excluded writes, and the helper SHA-256. `check-source.py` restores the exact CP85 bytes of all 31 files and rejects whole-file and changed-token drift (62 negative checks). Its `reverse_source(relative, text, strict=False)` can be used by a separately whole-file-hash-checked composite inverse for disjoint read/write/foreach batches; the normal isolated invocation remains strict.

`cp85-diagnostic-attribution.json` ties every direct read and length to the sealed CP85 log and its hash:

- 83 of the 114 `NonNullList<ItemStack>` array errors are these direct reads.
- 21 are assignment sites owned by the separate write batch.
- 10 are `TileEntityFurnaceSpace`, outside player inventory ownership.
- 19 missing-symbol diagnostics correspond to the length reads.
- The Faraday alias declaration accounts for one `NonNullList<ItemStack>` to `ItemStack[]` mismatch; its eight legacy array reads were not independently erroneous before the declaration migration.

The bounded negative compilation replays the exact 102 direct/length expressions against native EntityPlayer/InventoryPlayer types, independently reproducing 83 array errors and 19 missing-length errors. This is not a second full build.

`run.py` compiles the actual complete production helper plus all 110 modified read expressions and the complete production Faraday method body against the restored real Forge/Minecraft classpath. The Faraday method's `isFaradayArmor` call target alone is instrumented to control results and observe predicate order; no surrounding ArmorUtil class compilation is claimed. Original read expressions are evaluated with ordinary Java arrays in small test-owned holders, not copied Forge/Minecraft classes or an old API shim.

The native test uses real EntityPlayer and InventoryPlayer constructors, real native lists, ItemStack/NBT and a minimal World subclass with chunk loading disabled. No Unsafe allocation, Minecraft/Forge replacement class or downloaded library is used. Its 1,277 checks cover:

- Actual 36 main / 4 armor / 1 offhand layout and all vacancy slots.
- Nonempty, zero, negative, air, null-item and invalid-NBT identities and payload retention in every main/armor slot; native zero-count `getItem() == Items.AIR` remains explicit.
- All 110 exact extracted expressions across empty, populated, malformed and uniquely populated slots, compared with the old array-expression shape.
- Returned mutable aliases, offhand exclusion, receiver→field→index→entry order, owner replacement and entry mutation inside the index, null owner/inventory/list timing, and throwing-index precedence.
- Negative, end, minimum and maximum integer indices for both real fixed lists. Actual `NonNullList.withSize` backing raises the same `ArrayIndexOutOfBoundsException` class as arrays for these cases. There is no exception wrapper; out-of-domain dynamic NonNullList inputs keep their native bounds behavior.
- Complete Faraday null guards, short-circuit predicate count/order, noncanonical stack identities and captured-list identity when a predicate replaces the player's inventory.

`native-inventory-contract.javap.txt` retains the actual native constructor/list bytecode used by these proofs. `native-probe.log` and `baseline-expression-diagnostics.log` retain results.

## Reproduce

From the port root, with an already-restored native classpath and Java 8:

    python3 porting/tests/player-inventory-reads/check-source.py
    python3 porting/tests/player-inventory-reads/run.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /workspace/shared/nagaport-toolchain/native-classpath.txt

The paths are configurable; the runner uses the platform classpath separator and executable suffix. Windows has not been run. No full build was run in this batch.

## Explicit remaining boundary

The separate parent-owned foreach batch addresses the ten still-legacy nullable foreach bodies, including HazardSystem's armor loop; HEVBattery already uses native emptiness checks and is excluded there. Those changes are not claimed by this read batch.

Whole-array consumers in InventoryUtil (including mutation, copy and zero-count identity behavior) remain deferred. FurnaceSpace is unrelated. Other item-stack operations, getCurrentArmor/other equipment accessors, full production consumer compilation, startup, persistence, client/server loading and gameplay are not established here. Successful indexed-read evidence alone does not make hazard/radiation handling ready.

## Integrated CP86 reproduction

On the combined CP86 tree, add `--composed-source` to the native runner. It verifies the pinned combined source boundary first; the loop runner then inverses the two disjoint inventory families for its strict isolated source view. Standalone family hash guards remain strict for manifest-defined isolated sources. Combined terminal results are in checkpoint86.
