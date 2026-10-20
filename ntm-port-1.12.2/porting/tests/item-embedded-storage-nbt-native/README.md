# Item-embedded nullable storage NBT

Base: sealed recovered CP77 `d5ef20a8f1cb65fbd06faa86a4901870c8bab3f2`.
Production: `cec937b0`; eight loader substitutions in six owners, six imports,
and one genuine `NBTTagCompound.hasNoTags()` to `isEmpty()` method mapping.
`NullableInventoryStackNbt` is unchanged. No ItemStack empty/count rewrite, new
null guard, schema change, numeric-world importer, or platform API shim.

## Actual ownership and finite scope

1. `ItemBlockStorageCrate.InventoryCrate` owns the nullable array inherited from
   `ItemInventory`. Its constructor creates the array, initializes missing owner
   NBT/stacklock, then loads each `slotN`. Its writer removes null slot keys, writes
   non-null stacks, preserves root/lock/spider data, and refreshes stacklock.
2. `BlockStorageCrate` has two readers of that same `slotN` schema: the locked
   container's heavy/empty scan and the unlocked contents tooltip. Both use null
   to distinguish vacancy. The block-drop writer emits those same nested records.
3. `HazardTransformerRadiationContainer` reads the same crate records, and only
   non-null cargo contributes to its original radiation calculation. Type selection,
   raw count arithmetic and radiation transforms remain unchanged.
4. `BlockMassStorage` reads the portable storage type from `slot1`. Its existing
   block-drop writer serializes `slotN` records and the stockpile separately;
   type tooltip behavior still gates on null. Capacity/stockpile behavior is unchanged.
5. `ArmorModHandler` owns `ntm_armor_mods/mod_slot_N` records. Both pry readers keep
   registered records and remove invalid/missing records through the original
   `removeMod`/`clearMods` flow. Missing array entries remain null. Its genuine NBT
   emptiness mapping allows actual cleanup methods to compile and execute rather
   than inventing a stand-in. Null ItemStack/owner behavior is not newly guarded.
6. `ArmorUtil` owns the nested `hfrFilter` record. Its install/remove/getter methods
   preserve the original null guard and non-cleaning getter. Unlike armor mods,
   an invalid stored filter is returned as null but not removed automatically.

The helper checks native named registry IDs, not native `isEmpty()`, so registered
zero/negative-count records remain distinct non-null objects. Native public getters
and serialization retain native empty-stack behavior. Numeric 1.7-world IDs remain
outside this project's fresh/native-save boundary. Existing crate block-drop
`null || isEmpty()` filters predate this batch and are not altered.

`scope-audit.json` accounts for all 30 remaining loader calls in the sealed source:
eight selected, 22 deferred. IMC recipe interchange, artillery cargo synchronization,
entity/navigation ownership, block loot/display state, machine/sync state and Compat
count overrides require their own audits. Deferral is not a claim of incompatibility.
The selected nine missing method references are not claimed as a full-build delta
until the composed build is measured independently.

## Exact source evidence and runtime boundaries

`manifest.json` pins each before/after/pristine source hash, the unchanged helper,
three related storage/base owners and their pristine counterparts, and the actual
sealed FAIL12887 compiler log hash. Every production file reverses byte-for-byte to
the sealed base by undoing only the finite loader/import edits and the NBT mapping.
The verifier also compares 22 selected source fragments/constants to pristine code,
allowing only explicitly listed inherited tag getter/setter and text enum mappings
plus this batch's two mapping kinds. It checks individual original null consumers,
slot cleanup, ownership declarations and original base getter behavior.

Tests compile unchanged actual production statements into explicitly limited hosts:

- Crate read: the exact missing-tag initialization and whole slot load loop. The
  host supplies an already allocated array and target. Original constructor type
  dispatch/allocation/player assignment is source-protected, not runtime-tested.
- Crate write: exact NBT initialization and the entire remove/write/stacklock tail.
  Inventory size is an explicit test parameter instead of mocked crate-type lookup.
  `getStackInSlot` is the exact current/pristine ItemInventory method, not a fake
  native inventory implementation. No host implements a Minecraft interface.
- Locked tooltip: complete real spider and locked-content branches under the real
  has-tag gate, preserving both loops/early returns. Unlocked tooltip, hazard and
  mass-type probes execute the exact selected load statement only; synthetic return
  statements expose the result. Their formatting/arithmetic/type/capacity gates are
  not claimed as runtime coverage.
- Block-drop writers: actual three-statement native serializer/key-write fragments
  from both selected block owners. World/drop dispatch and their existing filters
  are outside runtime coverage and unchanged by exact reversal.
- Armor: full actual hasMods/clearMods/removeMod/pryMods/pryMod methods and constants.
  The writer's real NBT setup/serialization fragments are included, with an explicit
  slot parameter replacing only omitted ItemArmorMod type dispatch. Applicability
  and armor equipment-slot code are not compiled or claimed as complete.
- Filter: full actual install/remove/getter methods and constant. Recursive entity
  equipment selection and damage handling are unchanged and outside these hosts.

Observed: **224 new native assertions PASS**, plus a fresh rerun of the unchanged
**191-check native helper probe**. Real mapped Forge 1.12.2 NBT, ItemStack, registry,
item hooks and capabilities are used. The classpath ItemStack hash is pinned and
checked against sealed genuine runtime evidence. `javap` independently attributes
the cleanup call to `net.minecraft.nbt.NBTTagCompound.isEmpty:()Z`, not ItemStack.
Tests cover null/invalid/AIR records, registered signed counts, native serialization,
slot/case/bounds semantics, root/child aliases, cleanup differences, sibling/unknown
keys, original failure paths, locked tooltip early returns and nested armor/filter
roundtrips. The 191 helper assertions are rerun shared evidence, not new authorship;
the prior 1.7 runtime result is retained provenance, not a new run.

No native-owner/full-mod compilation, GUI/physics/radiation gameplay, startup or
in-world acceptance is claimed. Full compilation remains the parent's separate
composed measurement. No downloads, new dependencies, main-tree edits or push.

## Reproduce

Set Java 8 `JAVA_HOME` and actual mapped Forge 1.12.2 `MINECRAFT_CLASSPATH`, then run
`./run.sh`. `verification.log` records this batch's fresh run. The expected Forge
prefix warning comes from the original helper probe's test-only registered item.
