# Native positive ItemStack count assignment boundary

Base: CP52 e0d4369. This finite batch migrates **25 discarded simple positive-literal count assignments in 22 files** to genuine `ItemStack.setCount`. No field reads, compound assignments, increment/decrement, NBT fields, entity fields, platform classes or gameplay values change. Full production compilation/gameplay is not claimed.

## Attribution and selection

`CountAssignmentScanner.java` analyzes **all production Java sources** with the cached native Forge 1.12.2/stable39 classpath. Every selected receiver has declared native ItemStack type and its resolved field symbol is owned by that native class. Class-name/static access, unresolved receivers, unrelated same-name fields, compound/unary/value-used writes, comments inside the replaced separator and escaped identifiers are rejected. It emits no class files and does not call a failing analysis a successful compile. The original whole-source analysis reports 32,469 errors, matching CP52.

57 discarded assignments were attributed; `all-attributed-candidates.tsv` records every candidate. `attributed-accesses.tsv` records the25 selected ones. The exact UTF8 source manifest has forward/inverse fragments, byte offsets, before/after hashes, and32 rejected candidates with reasons. All CRLF/LF, comments and unrelated bytes survive. `merge_fragments.py TARGET_MODULE` permits disjoint workers' unrelated changes, fails if a matching statement is ambiguous, and prepares all outputs before writing any.

## Semantics and consciously deferred behavior

The native setter writes the exact supplied int, without size clamping, then recalculates cached empty state. This is NOT simply a public raw-field alias. Native getCount returns0 when empty, and getItem returns AIR. A positive setCount revives a real-item zero/negative stack, including a copied stack; original1.7.10 positive raw assignment likewise makes its count positive while retaining its item. Native copy in Forge2768 always constructs a new stack with raw item/count, rather than returning singleton EMPTY. Native EMPTY/null-item/AIR/invalid-damage stacks remain observably empty even with a positive raw count. Such malformed/template inputs need a broader representation migration, not a blind read rewrite.

Selected statements use positive literals (1,2,8,12,9,4) only; the dynamic MassStorage.quickExtract candidate is deferred. The deferred MassStorage candidate proof covers ordinary positive limits1/16/64; third-party items with nonpositive custom stack limits are outside this supported boundary and require continuation analysis. They normalize a copied valid recipe/input stack to the original amount, or a live nonempty slot to1. The dynamic quickExtract production statement is deliberately unchanged because external custom items may return nonpositive maxima. Only the API access changes: receiver and RHS are evaluated exactly once, receiver before RHS, RHS before a null-receiver failure. Positive counts over the normal stack limit remain unchanged. The native setter can restore an item's public identity after a zero/negative count; this matches the original engine's item-retaining raw count assignment and is tested, not suppressed.

All18 zero-count assignments remain unchanged, including six obvious identity sentinels in StorageStack, GUIHydroponic, GUILaunchPadLarge, GUITurretBase, ItemRebarPlacer and TileEntityMachineExposureChamber. The remaining12 consumption-looking writes also remain: indirect caller/alias observers may still depend on original item identity after consumption. Hazard dispatch is one such boundary needing continuation review. Original zero-count copies retain their public item identity; native zero-count copies expose AIR. Tests demonstrate this incompatibility. These consumers need intentional template/identity representation, not native empty stacks used as item templates. No workaround or behavior fix is introduced here.

14 dynamic-count assignments remain, including original raw count expressions and externally supplied count values whose consumer ranges/empty behavior are not proved. All stackSize reads and compound writes remain unchanged. This deliberately small batch does not purport to close the stackSize family.

## Tests

Recorded result:1,344 native/original runtime assertions,47 source mutation controls,15 attributed ownership fixtures; all25 selected writes re-attributed among57 candidates.

```
python3 porting/tests/native-stack-counts/run.py \
 --java-home /workspace/shared/nagaport-toolchain/java8 \
 --classpath-file /tmp/nagaport-ae2-native-classpath.txt \
 --legacy-client /tmp/nagaport-legacy-client.jar --verify-attribution
```

The original client is SHA256 a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce. No downloads occur. Native runtime uses actual engine ItemStack/Items/NBT and actual original-engine `add`/`adb` in a separate loader, not platform fakes. Reflection inspects fields only in tests, never production. Runtime matrix covers negative/zero/positive/overfull/int extremes, aliases, NBT identity, copied-stack revival, EMPTY, AIR, null item, invalid damage, null receiver, side effects and throwing RHS. Tests also verify the genuine native setter/getter bytecode.

The complete current production `TileEntityMassStorage.quickExtract` method is extracted into a fixture with the proposed setter substitution applied ONLY in that fixture, not production. Only its mod-owned collaborators (stockpile/type/dirty hooks) are bounded driver implementations; native ItemStack is genuine. Disabled, insufficient stock, exact amount, stock decrement, dirty marking, copy identity and template nonmutation are checked. This is a method-boundary proof, **not full tile class compilation, persistence, ticking or gameplay**.

Source guards test exact forward/inverse byte restoration and reject every changed fragment or unrelated-byte mutation. Fifteen attributed positive/negative fixtures cover owner/type resolution, static misuse, comments/strings, Unicode, unknown types, used assignment values, compound and unary mutations. Full optional attribution reconstructs original sources and requires exactly the recorded57 candidates and25 selected writes.

Remaining gates: complete consumer/slot null-to-EMPTY migration; original zero-count template identity; arbitrary count reads; exactly-once compound mutations; unported production classes/callbacks; whole-mod clean compilation; client/server world and save validation. No full Gradle build, remote push, excluded production package, stub platform or new dependency is used.
