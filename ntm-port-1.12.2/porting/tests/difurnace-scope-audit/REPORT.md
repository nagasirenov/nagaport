# Final scope counterexample: native empty is broader than nonpositive quantity

Production remains frozen. This audit is evidence for a scope decision, NOT
approval to broaden behavior and NOT an implementation workaround.

Positive raw quantity plus out-of-range damage is possible through public native
constructors/setters. Native reproduction gives:

* new ItemStack(IRON_INGOT,1,65536).copy(): raw1, isEmpty true.
* For valid matching inputs, positive fuel and vacant output slot, CP80's exact
  canProcess branch accepted that non-null result; new output.isEmpty guard
  rejects it. CP80 would go on to consume inputs/create the empty native output.
* SerializableRecipe.readItemStack source passes unrestricted integer metadata
  into that constructor. BlastFurnaceRecipes.readRecipe/addRecipe has no output
  validation; getOutput copies the selected output. The loader still has other
  unported registry syntax, so this audit does NOT claim successful full JSON
  loading. Public BlastFurnaceRecipes.addRecipe is also an unguarded producer.
* setItemDamage(65536) on an existing positive coal stack makes dynamic isEmpty
  true while cached getItem remains COAL. Thus even the new getItemPower guard
  changes an in-place malformed-fuel case: previously its existing getItem path
  could recognize COAL, now it rejects it. No reflective mutation is needed.

MachineBase's supported ordinary setter already maps native empties to null;
its NBT reader also filters isEmpty. NBT Damage is a short, then clamped >=0 by
the native constructor, so ordinary loaded damage cannot exceed65535. These
paths do exclude the case at admission, but public array/stack aliases and
unvalidated recipe outputs prevent claiming universal prior exclusion.

Conclusion: the new checks preserve ordinary valid positive stacks, but there
IS an additional malformed-positive semantic change beyond the user's exact
zero/negative approval. Parent must resolve that narrow scope before accepting
that broader claim. Do not add reflective production helpers to recover bad
metadata identity. No production code has changed during this follow-up.

ScopeBoundaryTest compiles/runs against genuine native classes. Raw count uses
read-only reflection. Its CP80/migrated decision comparison is an explicitly
shown branch-level reproduction, not complete CP80 machine execution. Source
provenance.json pins inspected source hashes and relevant verbatim methods.

Reproduce with Java8, /tmp/nagaport-ae2-native-classpath.txt and a fresh javac
output directory; run ScopeBoundaryTest. Runtime output is retained.
