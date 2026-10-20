# Native living equipment reads (CP87 candidate)

Baseline: CP86 `fc2cd2dba34caba0dba74ed288f95107ab4dc979`.
Production scope is 44 calls in 14 existing files plus `LivingEquipmentRead`.
The 34 equipment calls and 10 player armor calls are distinct contracts:

- Equipment indices 0,1,2,3,4 map to MAINHAND, FEET, LEGS, CHEST, HEAD and call native virtual `getItemStackFromSlot` exactly once. No offhand is introduced.
- Player `getCurrentArmor` previously called virtual `InventoryPlayer.armorItemInSlot`, not equipment dispatch. That separate hook and its original integer argument remain intact, including custom inventory hooks that accept other integer indices.
- Only identity-equal canonical `ItemStack.EMPTY` becomes null. Every noncanonical object, including zero/negative/AIR/null-item/invalid-NBT or tagged stacks, remains the same reference. This does not repair changed native `ItemStack.getItem()` behavior for malformed stacks.
- Receiver and index expressions are unchanged, evaluated once in Java order before helper entry. The equipment receiver null check precedes the fixed mapping lookup. Repeated reads and short-circuit predicates are not cached or rewritten.

The parsed repository-wide slot closure in `slot-domain.json` pins every helper call and armor/protection wrapper call, with enclosing loop headers. All equipment reads are in 0..4: loop reads explicitly use 0..<5, 1..<5 or 1..4; armor wrappers receive 0..3 through literals, the 0..<4 loops, or audited forwarding. All armor reads are in 0..3. This is a bounded repository contract, not restoration of the removed external integer API, arbitrary external overrides, enlarged/replaced legacy backing arrays, or malformed/uninitialized entity internals. Out-of-domain failure behavior is not generalized from the fixed mapping. Tests separately verify receiver-null/index-expression ordering.

All charger/refueler equipment expressions are included. Exact whole-file inverses prove that their energy arithmetic, charging controls and surrounding logic are unchanged. This is not machine/energy correctness acceptance.

## Evidence and reproduction

From the module, using the already-cached Java 8/native classpath/original client:

    python3 porting/tests/equipment-reads-native/check_source.py
    python3 porting/tests/equipment-reads-native/audit_source.py
    python3 porting/tests/equipment-reads-native/verify_engine.py
    python3 porting/tests/equipment-reads-native/check_slot_domain.py
    python3 porting/tests/equipment-reads-native/ownership_fixtures.py
    python3 porting/tests/equipment-reads-native/run.py --attribute

`check_source.py` proves 14 exact CP86 inverses plus the pinned helper, with 28 source drift controls. `inverse(relative, text, strict=False)` is available only for a separately whole-source-hash-checked composition: it still requires every reviewed replacement token and exact token count. Do not overwrite whole files when combining siblings.

The complete declaration audit screened 3,840 pristine and 4,066 CP86 Java units, parsing all candidate/Unicode units, with no owned old or native equipment/armor hook declarations. Actual javac before/after ownership resolves all 44 selected sites. Twenty-two fixtures reject unknown/generic/shadow/static/super/Unicode-spelled or owned-overridden lookalikes. The full binary declaration census covers 1,803 original and 4,876 native classes; original equipment readers are base/mob/player, native adds ArmorStand. Exact SRG mappings, selected bytecode and all relevant Forge1614/FML patches are pinned.

The original runtime probes use actual vanilla 1.7.10 player/mob/inventory classes in an isolated loader. The complete relevant Forge1614/FML patch review establishes that these selected read methods are unchanged. This does not assume all vanilla methods are a valid mod oracle: Forge explicitly corrects the player setter's mapping, as retained in `forge1614-patches.json`. Writes are separate evidence and not changed here.

Runtime proof: 2,165 assertions, real native player/zombie/skeleton/cow classes and inventory/ItemStack/NBT, all 44 complete selected original/native expressions, native and original virtual inventory/equipment hooks, malformed stack identity, receiver/index/null ordering and bounds cases. Complete production armor predicates and the full worn-set construction expression execute natively, including repeated/short-circuit reads. The complete five-slot hazard traversal executes against original and native equipment with only its downstream `applyHazards` dependency instrumented to record alias/order/receiver. The original event getter is an explicitly supplied holder dependency; native expressions use the actual Forge LivingEvent. Unsafe is used only to skip unrelated world/network constructors. These are not complete NTM production-class constructions or gameplay tests.

Five runtime mutants are rejected: isEmpty normalization, swapped slots, altered null/index order, bypassed inventory armor virtual dispatch, and copied aliases.

All 44 old selected expressions independently fail against native signatures and all 44 replacements compile. Full-tree attribution reports 11,787 errors before and 11,744 after (not a full build). The net reduction is 43 because ArmorUtil.java:122 newly exposes `stackSize has private access in ItemStack`, previously hidden by missing getEquipmentInSlot; exact bounded diagnostics are retained. The raw count expression is deliberately not changed. The generic null equipment write there is also not changed.

No full build, client/server startup, radiation formulas, protection implementation, machine energy behavior, multiplayer or saves are accepted by this evidence. No network or push occurred.
