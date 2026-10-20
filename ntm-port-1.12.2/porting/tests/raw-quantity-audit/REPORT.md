# Raw quantity read audit at CP57

Repository HEAD 324f35ad53f98e42045ad81c7ec4b45410d663df. Main not modified; all scratch files here. No dependencies downloaded. This is a read audit/proposal, not an accepted production migration.

## Findings

- Stable39 fields.csv and mcp-srg.srg map ItemStack.stackSize -> field_77994_a. Native Forge2768 ItemStack is final; field is private int. Official existing Forge convention is ReflectionHelper.findField(ItemStack.class, "stackSize", "field_77994_a"). Cache Field once and use getInt; propagate IllegalAccessException as an explicit IllegalStateException. Null input must still throw NPE. No fallback normalization, mutation, global ItemStack change, Unsafe, AT, or reflective writes in production.
- Native getCount bytecode reads cached isEmpty, returning 0 when true, raw stackSize otherwise. isEmpty checks canonical singleton, null/AIR item, count <=0 and invalid damage. Old add.b is public int with exact constructor assignment (mapped by old joined.srg to field_77994_a). Negative values and integer overflow are real raw quantities; getCount is NOT a replacement.
- Crucially canonical native ItemStack.EMPTY has raw count ONE. Native static init constructs new ItemStack((Item)null), whose default count is 1. Returning raw1 is correct field access but not correct semantic recovery of an old absent slot represented by null. Never silently normalize this in the raw helper; exclude affected consumers until receiver lifecycle is resolved.
- fast version `int n=s.getCount(); return n!=0?n:raw(s);` is equivalent for this final genuine class in ordinary single-thread evaluation: nonzero result is necessarily the raw field; zero falls back. It adds branch/method access and a second exceptional path, with no measured need. Direct cached field access is the smallest auditable design. No performance claim made.

## Actual tests

RawTest.java compiles against cached native classpath, bootstraps native Items plus authentic original engine in separate loader, and passes 1,636 checks. Matrix includes MIN_INT, negative, zero, normal, overfull, MAX_INT; Apple/AIR/null items; constructor/copy/setCount/grow/shrink overflow; invalid-damage and stale-cache fixtures; canonical EMPTY; single receiver evaluation; null NPE. getCount differs in 25 initial matrix cases. No singleton or global stack mutated. Reflection writes ONLY to newly allocated test fixtures for invalid damage/cache. The first run intentionally uncovered the wrong EMPTY=0 assumption and failed; corrected expectation=1 is justified by actual static initializer. runtime.log records final pass.

## Source inventory

Scan.java attributes all 4,051 current production sources with native dependencies; analysis is NOT successful compilation (20,250 diagnostics, distinct from build pipeline counts). accesses.tsv contains all member access sites. 870 total .stackSize syntax nodes in 376 files, of which 866 resolve exact owner AND receiver net.minecraft.item.ItemStack:
- 649 read-only uses, 315 files;
- 34 simple writes;
- 81 compound mutations;
- 102 unary mutations.
Four unresolved excluded accesses: FurnaceSpace furnaceItemStacks[1] (one read, one unary); MachineExcavator i.getEntityItem() read; ArmorUtil entity.getEquipmentInSlot(slot+1) read. Lexical rg line totals are not access totals. Scanner is audit inventory, not yet mutation-tested production transform; a production selector must also reject malformed/static syntax and preserve separators/Unicode/parenthesized lvalues and prove exact byte inverses.

## Lifecycle exclusions and finite proposal

154 of 649 reads occur in inventory/container sources. Six direct inventory accessor reads occur in SlotCraftingOutput, SlotNonRetarded, ItemInventory, ItemAmmoBag, ItemCasingBag, ItemPlasticBag. These need path-specific work; merely having a getHasStack/null guard or a copied value is not a lifecycle proof. 49 read-containing files also explicitly reference ItemStack.EMPTY. This is a conservative prioritization signal, NOT complete alias analysis.

Verified current boundaries:
- TileEntityMachineBase.nativeStack, TileEntityInventoryBase.nativeStack and proxy inventory paths normalize null OR isEmpty to EMPTY. Their native outward getters thus lose original noncanonical negative/zero raw count; setters may canonicalize to nullable internal storage. Raw helper cannot reconstruct discarded values.
- ItemStackUtil.carefulCopyWithSize returns singleton EMPTY when copied input is empty, before requested size assignment. Its callers must not be treated as raw-preserving producers.
- NativeHeldItem.getMainHandNullable uniquely maps ONLY singleton EMPTY to null and otherwise preserves identity, including zero/negative/tagged stacks. Its docs correctly disclaim restoring getItem identity.

Bounded first candidate: BlockFireworks.java lines68 and75, the two direct NativeHeldItem.getMainHandNullable(player).stackSize expressions only. Their exact receiver path cannot return canonical EMPTY. Proposed substitution is RawItemQuantity.read(existing full receiver expression), preserving each original repeated virtual lookup and arithmetic, not caching lookup or changing control flow. Zero writes in same method stay untouched and full class remains unported. Counterfactual malicious/dynamic entity getter returning different stacks on repeated calls preserves original call count and existing nullable-bridge behavior; helper must not mask resulting NPE.

This is a finite helper-plus-two-reads PROPOSAL, not complete consumer parity proof. Before implementation, add complete extracted onBlockActivated-method tests against old/native boundary with existing excluded writes represented honestly, control flow/evaluation/error cases, exact source provenance and inverse/source mutation controls. If that whole-method boundary cannot be proved without implementing deferred writes, land no consumer edits; retain accessor audit for later coherent inventory work. Do not extrapolate from these two to the other647 reads. Original item-identity loss for zero stacks remains separate and is not repaired here.

Artifacts: native.txt, old.txt (actual javap); Scan.java, inputs.txt, accesses.tsv, scan.log; RawTest.java and runtime.log. Reproduce Java8 compilation/runtime with /tmp/nagaport-ae2-native-classpath.txt and /tmp/nagaport-legacy-client.jar. No full mod/client/server/gameplay acceptance.

## Final recommendation after parent review

Do not add a helper for only two reads. Preserve this evidence for a coherent receiver-lifecycle migration. Largest future candidate families: machine tiles241 reads; containers154; bomb tiles39; bomb blocks35; tool items17; InventoryUtil16; recipe family14. Counts are candidate sizing, not approved substitutions. Native getCount is appropriate only after actual receiver path proves nonempty AND cache coherent (or consumer explicitly defines absent as quantity0). A null check alone proves neither; positive raw counts with AIR/null items or invalid damage still normalize. Container transfer families often start with getHasStack, then mutate/copy/merge the stack before the quantity read, so admission-time nonemptiness does not prove read-time raw equivalence. Machine-owned nullable arrays and positive constructor-produced recipe outputs are more promising for preserving raw quantities, but writes and getter/setter normalization must be included in the same family. Recipe deserializer/template paths can construct zero/negative values and need raw-range tests. No exhaustive new reachability proof performed.

## Portable reproduction of retained runtime audit

From the module, set J to a Java8 home and CP to the genuine cached native dependency classpath. Compile RawTest.java into a fresh temporary output directory with `$J/bin/javac -cp "$CP" -d "$OUT" porting/tests/raw-quantity-audit/RawTest.java`; run `$J/bin/java -cp "$OUT:$CP" RawTest /path/to/authentic-1.7.10-client.jar`. Do not modify production or singleton EMPTY. The scanner input list is regenerated from the chosen frozen source tree rather than relying on its prior absolute-path inputs.txt: list all src/main/java/**/*.java into a UTF-8 file, compile Scan.java with Java8 tools.jar, then run Scan INPUT_LIST CLASSPATH_FILE with tools.jar on the runtime classpath. Scan diagnostics are expected for the incomplete port, not a successful build. Retained accesses.tsv describes the CP57 audit snapshot, not later added classes.
