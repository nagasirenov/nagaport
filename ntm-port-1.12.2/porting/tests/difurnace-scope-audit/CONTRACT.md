> Approval status superseded: native-empty malformed furnace boundaries were subsequently approved; repaired dynamic-nonempty aliases were not and are now preserved by the raw reader. The pending wording below records the historical proposal only.

> Historical pre-fallback proposal. The final private raw reader restores repaired-cache quantity parity, and the second lookup now rejects empty results. See ../difurnace-raw-reader/REPORT.md. The native predicate/entry-path evidence below remains applicable; the old capacity67 outcome is no longer the proposed production behavior.

# Complete bounded contract for the proposed DiFurnace migration

This describes the FROZEN proposal, not an extra policy change or claim of user
approval. Full native-empty/malformed-state approval is pending with the parent.

## Native state domain (genuine Forge2768 bytecode)

For a non-null ItemStack s, dynamic D = s.isEmpty() is exactly:
- s is canonical ItemStack.EMPTY; OR
- private raw item is null or Items.AIR; OR
- raw signed int count n <=0; OR
- raw damage d <-32768 or d >65535.

All other states have D=false. Stack tags, being overfull, item max durability,
registry membership and capability content are NOT additional tests in D.
A missing delegate affects getItem separately, not this dynamic predicate.
Java null is handled independently by furnace null checks, not passed to D.

Cached empty C is a separate field. getCount returns C ? 0 : n. getItem returns
AIR when C or delegate-null, otherwise the delegate's item. Construction and
setCount update C from D. grow/shrink perform raw int arithmetic then setCount.
Public setItemDamage calls Item.setDamage; the vanilla implementation writes
and clamps negative damage to0 but does NOT refresh C. Therefore D and C can
disagree through public APIs, without reflection:
- valid stack -> damage65536: D=true/C=false; new guards reject it.
- constructor damage65536 -> setItemDamage0: D=false/C=true; guards admit it,
  but count/item access retains native cached projections until count refresh.
Neither cached state is repaired or reconstructed by the proposed migration.

The lower invalid damage bound is part of the predicate even though ordinary
constructors and vanilla setters clamp negatives; no universal exclusion is
claimed for external/custom mutation. Native capability/delegate behavior is
retained, not emulated.

## Every changed furnace boundary

1. getItemPower: null OR D yields power0. Otherwise original native item lookup
   and fuel constants run. No slot is cleared. Existing refuel timing still
   precedes canProcess, so a blocked recipe can still refill fuel as before.
2. canProcess inputs0/1: null OR D rejects processing before recipe lookup.
3. canProcess returned recipe result: null OR D rejects processing.
4. canProcess output slot3: null OR D is considered vacant, so capacity/type
   checks are skipped. Otherwise original item equality/max-size checks run,
   with the exact signed-int sum of native cached getCount values.
5. processItem output slot3: null OR D gets a copy of the newly looked-up recipe
   result. Otherwise matching output grows by the result's native getCount.
   Existing repeated recipe lookup and mutation order remain; there is no new
   transactional revalidation or guarantee for adversarial recipe callbacks.
6. Fuel decrement uses shrink(1), and the zero branch uses native getCount==0.
   Positive coherent fuels reach it exactly on last unit. Captured Item receives
   the SAME depleted stack once; only canonical EMPTY residue becomes null.

This is per-read behavior, NOT global eager normalization. Rejected malformed
inputs/fuel stay in the backing array unless preexisting storage code removes
them. An empty/malformed output object is replaced only when processing writes
a result; aliases to the prior object are not rewritten. Callback results are
not broadly normalized: noncanonical zero or malformed residues remain the
callback's actual returned object and face guards on later reads.

D=false/C=true is included in the native behavior contract, not claimed to be
legacy-equivalent. Confirmed example: raw64 iron output, invalid constructor
metadata then public repair to0, has getCount0. New capacity accepts +3 and grow
retains raw arithmetic yielding67; old raw capacity would reject67. No arithmetic
clamp/long conversion is introduced. For D=false/C=false ordinary valid stacks,
getCount is exactly raw count and all positive mechanics remain unchanged.

## Entry and alias boundaries

Existing MachineBase setter/getter/NBT reader already project dynamic empties
as absence; no shared production change is proposed. NBT short damage and native
clamping prevent ordinary loaded metadata outside the dynamic valid interval.
However setters preserve admitted object aliases, slots[] is public, and native
stack setters can mutate an existing aliased object later. Repaired cached-empty
states have D=false and are admitted even by the current setter.

Recipe registration/IMC and JSON metadata construction can provide malformed
positive outputs; registration does not validate D. getOutput copies, retaining
raw count/damage and normally reconstructing cache, rather than filtering them.
The JSON loader is not claimed to compile fully in this unfinished port.
Fuel callbacks may return aliases, EMPTY or malformed objects; only the actual
canonical absence bridge described above applies.

## Valid-operation transient zero and overflow

- Last fuel: raw1 ->0, same callback argument, captured real fuel Item, vanilla
  lava -> one bucket, ordinary no-container fuel -> nullable absence.
- Last recipe input: actual unchanged MachineBase decrStackSize clears slot and
  returns its original count1 alias; that alias is not decremented to0. Partial
  inputs are split, preserving original remaining alias and ordering.
- Output merge: same output alias grows; recipe result insertion uses copy.
  Shared input/input and output/input aliases keep sequential original effects.
- Signed capacity/addition overflow is preserved as raw int arithmetic. A result
  overflowing to MIN_INT stays in the same object immediately; subsequent reads
  classify it by D as empty. No immediate clamp/removal/balance fix is added.
- Canonical EMPTY never reaches the quantity mutations through ordinary guarded
  fuel/input/result/output paths. No singleton/global mutation is introduced.

Existing native tests cover normal positive cycles/aliases/containers/overflow;
ScopeBoundaryTest demonstrates both invalid and repaired-cache counterexamples.
The contract above is sufficient to state all native empty/count projections
used by this proposal. No additional domain predicate or production policy is
proposed. If approved, the accurate claim is native furnace stack semantics,
including malformed and stale-cache states, while preserving coherent valid
positive gameplay; NOT universal raw-field equivalence for every positive int.
