# DiFurnace lifecycle blocker at CP79

Scope inspected: TileEntityDiFurnace, TileEntityDiFurnaceRTG, their inherited
TileEntityMachineBase storage, BlastFurnaceRecipes, and RecipesCommon matching.
Base commit: 77b595c32b46e2f9309bcf9924d654ddc29a3c95.
No production edit is proposed. This is a finite counterexample audit, not a
successful machine port, full class compilation, or gameplay test.

## Exact blocking paths

* DiFurnace has six stackSize compiler diagnostics at lines 156, 169, 205, 206.
  The capacity sum and output addition must preserve signed overflow. Native
  getCount cannot substitute for the raw fields on zero/negative stacks.
* Fuel getItemPower uses stack.getItem before decrement. Authentic original
  zero/negative COAL still identifies as COAL and returns breathable power200;
  native returns AIR. Merely introducing raw count reads cannot fix this.
* Its fuel code decrements then calls stack.getItem().getContainerItem(stack)
  when quantity reaches zero. Native loses the lava-bucket receiver at that
  precise boundary. Capturing receiver before decrement solves positive fuel
  depletion, as the existing thermal migration does, but does not restore fuel
  identification for stacks already zero/negative on entry.
* MachineBase.setInventorySlotContents normalizes isEmpty to null; getter also
  normalizes to EMPTY, whose actual raw quantity is ONE. Its NBT read discards
  nonpositive registered stacks and write omits them. Its decrement bypasses
  them whereas original decrement returns the same alias and clears the slot.
* BlastFurnaceRecipes.getOutput calls AStack.matchesRecipe(in,true). The CP79
  ComparableStack/NBTStack implementations reject isEmpty before matching,
  unlike original. Fixing just the furnace cannot recover those discarded
  recipe candidates. Recipe outputs may be supplied by JSON/IMC, not just the
  positive hard-coded defaults. Native copy preserves raw count but does not
  make getItem expose identity on a copied nonpositive stack.
* RTG shares recipes/storage and also has its own setter normalization plus an
  explicit nonpositive-input reconstruction branch. It is not an independent
  shortcut around the same problem.

## Reproduced evidence

LifecycleBoundaryTest.java uses genuine native Minecraft/Forge and authentic
original client classes in a separate loader. It tests MIN_INT, -1, 0, 1, 64,
MAX_INT coal identity, raw count versus native getter, native copy behavior,
last-unit lava identity, and canonical EMPTY raw count. Runtime result: PASS39.
There are no reflective writes, Unsafe, AT, global/singleton mutation, fake
platform classes, downloaded dependencies, or production exclusions.

Run from the module:

    J=/workspace/shared/nagaport-toolchain/java8
    CP="$(cat /tmp/nagaport-ae2-native-classpath.txt)"
    OUT=$(mktemp -d /tmp/difurnace-boundary.XXXXXX)
    "$J/bin/javac" -cp "$CP" -d "$OUT" porting/tests/difurnace-lifecycle-audit/LifecycleBoundaryTest.java
    "$J/bin/java" -cp "$OUT:$CP" LifecycleBoundaryTest /tmp/nagaport-legacy-client.jar

## Smallest viable next boundary

Do not land a count-only six-edit patch or restrict the accepted input range.
First select a coherent nullable MachineBase storage boundary: preserve identity
and nonpositive counts through setter/read/write/decrement; translate only actual
absence at native-facing getters. Test native external insertion/extraction and
legacy alias behavior explicitly. The existing NullableInventoryStackNbt.read
already preserves registered nonpositive records and is a useful producer, but
it does not fix getter/matcher item identity. A separate read-only raw-item
identity policy (or an equivalent proven identity-preserving adapter) is needed
for fuel and count-ignoring recipe matching. A temporary positive copy is not a
universal substitute for arbitrary AStack implementations or callback arguments.
Once that boundary is settled, DiFurnace's six quantity changes plus fuel receiver
capture form a finite consumer batch. This audit deliberately does not silently
make that shared architecture decision or weaken original-behavior requirements.
