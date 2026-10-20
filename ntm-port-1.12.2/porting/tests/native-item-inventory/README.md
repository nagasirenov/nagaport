# Native item-inventory interface and nullable domain

Baseline CP91: `f5ce86cd607379cc9810896cf91d1fee86ce6331`. This isolated batch edits nine existing source files. It does not change the copied ammo/casing/plastic-bag inventory implementations or shared `ItemStackUtil`, nor claim that the complete box GUIs are playable.

## Finite production closure

The complete `ItemInventory` base implements genuine native `IInventory`. Its four actual descendants are `InventoryLeadBox`, `InventoryToolBox`, `InventoryRebar`, and `InventoryCrate`. The base's nullable storage access is explicit; only absent native return values become `ItemStack.EMPTY`. Other non-null stacks retain reference identity, including negative counts. Incoming singleton EMPTY denotes vacancy, not a new general `isEmpty`-normalization rule. The original setter's upper-only clamp, exactly-zero cleanup, signed/overflow decrement branches and complete removal identity are preserved. The read-only mapped quantity accessor follows the existing verified MCP/SRG convention, without private writes.

Native fieldless methods represent the actual absence of synchronized properties. `clear` empties the same nullable array and calls virtual `markDirty`, preserving crate-specific persistence. Native emptiness queries interpret native `ItemStack.isEmpty` without mutating domain storage. Native display components retain literal custom target names versus translation keys. The three list-inventory constructors test raw tag presence with `getTagCompound()==null`, preserving an existing compound even on a nonpositive target owner rather than using native masked `hasTagCompound`. Three subclass naming contracts, one crate usability override, three usability callers and six GUI title calls are native; original title keys and held-reference comparisons remain intact.

Domain-local list NBT methods preserve the original null-only serialization and signed-byte count/index schema, requested-count iteration, duplicate overwrite order, unrelated tags, and registered zero/negative records. They use genuine native `writeToNBT` and the already-existing `NullableInventoryStackNbt`. This is deliberately not a global change to shared helper behavior. Crate's distinct slotN schema, lack of base zero cleanup, stacklock lifecycle and close-time size check remain distinct.

## Ejection and native representation

Native compressed output is obtained using genuine `CompressedStreamTools.writeCompressed`. The threshold remains strictly greater than 6000 bytes. Position/chunk/motion formulas, random call order, spawn callback, ignored spawn result, warning, target reset and later zero cleanup are unchanged. Item/damage/tag are captured before shrinking the final positive chunk, so native empty masking cannot replace an ordinary final drop with AIR.

For a positive raw count whose public item is already masked to AIR (out-of-range damage, or repaired damage with stale native empty cache), an edge-only detached public NBT snapshot supplies the registered item ID; that resolved item's virtual `getDamage` supplies the full int, not the serialized short. No raw item writes, source-stack temporary mutations, new normalization, extra storage or invented data policy are introduced. Native handling of the resulting engine-empty `EntityItem` remains an explicit platform boundary. This preserves the mod payload; it does not promise old-engine pickup/update behavior for damage above 65535.

The fallback snapshot invokes native capability serialization, and resolution follows the current native registry/delegate. These are native effects not present in old vanilla stacks. Arbitrary capability callbacks, exceptions, registry replacement during callbacks, or unregistered items do not have a full behavioral-equivalence claim. Ordinary registered valid stacks do not take this extra snapshot. Existing persistence already invokes native capability serialization. See `production/REPORT.md` and actual damage matrix for exact evidence.

The original huge Euphemium armor material does not establish an ordinary high-damage producer: `ArmorEuphemium.setDamage` is a no-op. Audited ordinary tool material durability tops out at 10000, direct item durability registrations are below 65536, and the other large item values found are energy/yield rather than item damage. This is a bounded source audit, not a claim that every external caller supplies valid damage.

## Evidence

- `check_source.py`: exact inverse to immutable CP91 for every complete edited file, 27 drift controls, 15 protected collaborator/sibling pins, exact four-descendant closure, 37 native override declarations, and 56 owned typed calls. Complete attributed class slices must match current source bytes.
- `InventoryScanner.java` / `attribute.py`: parse and attribute all 4071 original production inputs without emitting mod bytecode. Exactly 21 errors removed, zero added: 10583 to 10562 on CP91. This is not a successful whole-mod build.
- `engine/`: actual original full ItemInventory/ItemStackUtil classfiles with name-only old platform remapping; real old and native ItemStack/NBT; 1,112 original assertions including three complete original nested constructors, 666 native assertions per MCP/SRG layout, and 401 byte-identical compressed payloads including exact 5999/6000/6001 sizes.
- `production/`: complete current base and real production persistence/sound collaborators compile and run against genuine native classes. Native Slot, original signed arithmetic/aliases, persistence, component dispatch, real constructed World/EntityPlayer/EntityItem callbacks and complete ejection are exercised. The World subclass observes spawn calls; it is not a running server/chunk spawn or gameplay claim.
- `subclasses/`: four exact complete nested declarations compile/run against the real base and native APIs. Explicit outer hosts supply only type/registry identities. Six crate-type collaborators are exact size/name constructor/method projections, not complete tile gameplay classes. Full outer item classes/containers/GUIs are not claimed compiled. 1,418 assertions cover list/slotN persistence, raw aliases, native Slot, virtual clear writer, six crate choices, naming, validity and actual held-reference/close dispatch.
- `disk-narrowing.json`: tracked historical copies were verified against recoverable Git blobs before sparse omission; unique work and canonical caches were retained.

Run each subdirectory's `run.py` with the existing offline toolchain, and `python3 check_source.py`. The engine remapper uses the already-existing `difurnace-raw-reader/RemapQuantityField.java`.

## Next coherent integration dependency

The base is not enough to make boxes playable. `ContainerLeadBox`, `ContainerToolBox`, and nested `ContainerRebar` still use the obsolete integer click-mode callback and legacy transfer assumptions. Their real native ClickType/transfer/held-box exclusion closure, plus the outer items' native right-click dispatch, is the next integration family. Copied bag inventories, unrelated toolbox hotbar/template operations, shared stack helper policy, complete crate tile lineage and full game startup remain separate.
