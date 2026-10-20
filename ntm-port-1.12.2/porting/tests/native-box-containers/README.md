# Bounded item-inventory box transfers, not native GUI activation

Baseline: `d7544326` (CP94). Production changes are exactly three existing files plus one new helper:

- `ContainerLeadBox`: native transfer return/vacancy values, raw exactly-zero check, local domain merge.
- `ContainerToolBox`: same finite transfer changes.
- `SlotNonRetarded`: one legacy count read becomes native `getCount`, guarded by inherited native `getHasStack`.
- `ItemInventoryTransfer`: complete original two-pass merge algorithm localized to these two containers.

The original integer `slotClick` callbacks and all three outer item right-click methods remain byte-for-byte unchanged. The files therefore still have known obsolete callback compilation errors. No complete box GUI, complete outer item class, playable jar or whole-mod build is claimed. No global `InventoryUtil`, `ItemStackUtil`, `SlotPattern`, base inventory policy, copied bag inventory or crate tile lineage is changed.

## Transfer contract

Every destination retains the original virtual `Slot.getStack` read. Only the native canonical EMPTY return is translated to nullable vacancy. A noncanonical zero, negative, invalid-damage or stale-native-empty object remains occupied. The existing item-inventory read-only mapped quantity accessor supplies signed int counts; no private field writes, normalization, nonnegative clamp or long-arithmetic replacement is introduced. Destination/source aliases, callback order, iteration direction, original int overflow, original slot restrictions and exactly-zero cleanup remain.

The registered item identity of an engine-empty destination uses the same edge-only public NBT snapshot convention as CP94 ejection. It does not read narrowed NBT Count or Damage. Full damage is dispatched through the resolved item's native virtual `getDamage`. Direct nullable compound comparison reproduces the original merge's NBT comparison rather than the native helper's empty/capability gate. Ordinary source stacks already passed native `Slot.getHasStack`. Last-item removal uses the raw exactly-zero check, preserving aliases that become negative through overflow. Copies/splits and slot writes remain genuine native APIs.

### Explicit limits

- Native `Slot.getHasStack` still hides nonpositive and invalid-damage source stacks, so this is not original raw-invalid source transfer equivalence. Those sources are not consumed or normalized by these transfer methods.
- The single shared slot accessor uses genuine native `getHasStack`/`getCount`; this does not restore the old slot's raw invalid-stack display/limit interpretation.
- Edge-only identity snapshots invoke native capability serialization and current registry resolution. Arbitrary callback mutation, capability exceptions, registry replacement during callbacks, unregistered items, or Forge capability merging semantics do not have a full legacy-equivalence claim. No new capability compatibility rule is invented.
- Native InventoryPlayer, Slot, ItemStack copy/split, sounds and container close callbacks remain native platform operations. Native ordinary-click payload handling is a separate, unactivated dependency, not covered by this transfer proof.

## Evidence and reproducibility

`python3 run.py` uses only the existing offline native classpath and Java 8 toolchain. It compiles complete production ItemInventoryTransfer, ItemInventory, SlotNonRetarded, SlotPattern, NBT/held/sound collaborators and exact complete nested inventories. The two container constructor/transfer/usability/close methods are exact projections. Their obsolete `slotClick` methods are deliberately not compiled or activated. Outer hosts and registry/GUI identities are explicit fixtures, never fake Minecraft/Forge APIs.

The test runs 11,370 assertions per MCP/SRG quantity-field layout using real ItemStack, Slot, InventoryPlayer, World and EntityPlayer subclasses. A 1,173-case differential matrix compares the new helper with the actual pinned original mod-jar merge method bytecode, including forward/reverse traversal, signed counts, differing item/tag payloads, high damage, occupied raw destinations, overflow, source/destination aliases and full callback logs. Additional real container transfer projections cover all nine selected hotbar slots, last-item removal, return copies, reference identity, ordinary forward/reverse transfers and rejection filters. Persistence is the complete production ItemInventory implementation.

The original-bytecode oracle is **not** an execution in the old engine. `AdaptOriginalMerge` extracts only the original complete merge method, retains its control flow and arithmetic, remaps method names, adapts direct count reads/writes to raw-read/native-write boundaries, adapts legacy-null slot reads, and supplies explicit original item/damage/tag/copy semantics on real native objects. That isolates algorithm parity; separate assertions establish native storage/reference behavior. Other original InventoryUtil methods are not loaded or compiled. The input mod hash and adapted-method class hash are recorded.

Four executable mutants must fail: treating any engine-empty destination as vacant, masking signed quantity, masking registered raw item identity, and using native empty-aware tag equality. `check_source.py` checks three exact complete-file inverses, nine source-drift controls, one complete helper, eleven protected source pins and all runtime source hashes. Parent owns combined actual full compilation; no separate whole-source attribution or dependency regeneration is performed here.

## Unapproved next dependency

An out-of-tree `nagaport-box-unapproved-dispatch-candidate.patch` is a **proposal only**, deliberately excluded from the production and evidence patches and not approved for integration. It shows native lead/rebar right-click signatures and rebar ghost-slot preservation. It does not solve the native ordinary-click hazard, and is not a tested/approved complete callback family.

Native Container ordinary PICKUP can regard a retained negative/invalid stack as empty and overwrite it with the cursor; drag/SWAP/collect interactions also need a coherent preflight or faithful adapter. Merely documenting that loss does not authorize activation. Preserve the original lead +47 and toolbox +51 held-index guards, and the original rebar +47 anomaly: its actual hotbar begins at +28, but correcting the original bug is not a signature migration. Rebar slot zero intercepts every original click mode, including shift, not only PICKUP. Native offhand handling must return the exact held reference and must not open the main-hand provider from OFF_HAND. Toolbox outer dispatch additionally reaches unported `moveRows`, which remains separate.
