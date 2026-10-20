# Native inventory lifecycle, CP70

Base: `ea933e28fe3a1a9a60e9d438411a02182b2ab22a`.

Production change: 45 files; 72 old no-argument declarations become the native
`openInventory(EntityPlayer)` / `closeInventory(EntityPlayer)` callbacks. Eight
containers pass `invPlayer.player` when opening and their existing close argument
when closing. Two item-inventory descendants forward that argument to `super`.
The argument is deliberately named `viewer`: it must not shadow the existing
`ItemInventory.player` owner used for sounds, NBT synchronization and random state.
All 62 original empty callback bodies are retained, not newly invented stubs.
The only body-level prerequisite is the crate-close `hasNoTags()` to native
`isEmpty()` predicate. Both versions invoke `Map.isEmpty()`, as captured in proof.

## Evidence and limits

`verify.py` compares every source file against the sealed base, permits only the
listed transformations, reverses them byte-for-byte (including mixed CRLF), scans
for remaining zero-argument lifecycle references, and checks all 72 callback
bodies against pristine. The pristine comparison allows only the explicit,
already-landed world/position/sound/stack-tag access mappings plus this migration.
The generated native probes contain exact complete production callback methods.
They inherit native `InventoryBasic` only to provide unrelated `IInventory`
obligations. They are NOT compiled whole machine owners.

`LifecycleNativeTest` passes 117 assertions using the actual 1.12.2 IInventory,
ItemStack, NBTTagCompound, World listener interface, production WorldSoundEffects
and production ModSoundEvents. World and player fixtures are allocated without a
running game. Checks cover sound identity, position, volume/pitch, native virtual
dispatch, server/client cabinet counts, original negative-count behavior,
container owner versus supplied viewer, null viewer, bag overrides, crate
stack-lock cleanup/re-lock, toolbox NBT and owner RNG, and callback ordering.
`checkNBT` is an explicit recording identity collaborator: NBT size compression,
ejection and its failure paths are NOT validated by this probe. Every unchanged
empty callback is compiled and invoked through native IInventory.

Native IInventory extends IWorldNameable, requiring `getName`, `hasCustomName`
and `getDisplayName` in addition to lifecycle, fields, clear and EMPTY-based
stack operations. MachineBase and InventoryBase already have native naming and
display components at CP70. Other direct owners and legacy virtual naming
families still need work. This patch does not rename their names, change custom
name/localization selection, fill missing obligations with stubs, fix nullable
stack boundaries, prove full machines, compile the full port, or claim gameplay.

## Reproduce

Use the existing Java 8 toolchain and cached native classpath, no downloads:

    export JAVA_HOME=/path/to/java8
    export NATIVE_CLASSPATH_FILE=/path/to/native-classpath.txt
    bash ntm-port-1.12.2/porting/tests/native-inventory-lifecycle/run.sh /tmp/lifecycle-proof

The strict source reversal is intended on CP70 plus this production patch, before
integrating unrelated edits. Other patches require independent contextual review.
For whitespace checks preserving original CRLF:

    git -c core.whitespace=cr-at-eol diff --check

Evidence logs include expected Unsafe/compiler and headless registry-prefix
warnings. No server/client launch or audio device was used.
