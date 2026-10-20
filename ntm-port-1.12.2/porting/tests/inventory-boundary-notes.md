# Native inventory ownership boundary

## Scope and provenance

Baseline: `15b0a1044e501892ff3313ca91186a51aa065e49`. Exactly two production files:

- `com/hbm/tileentity/TileEntityInventoryBase.java`: complete existing production base, not an extracted algorithm or surrogate.
- `com/hbm/tileentity/machine/storage/TileEntitySoyuzCapsule.java`: sole change is `public String getName()` to `protected String getDefaultName()`.

The checked-in manifest retains exact pre-change source and before/after SHA-256 for those two files. No upstream provenance beyond the supplied baseline is asserted. MachineBase, LoadedBase, furnace, battery, recipes, containers, GUI and block production files are unchanged by this batch.

## Ownership and contract mapping

| Old contract | Native contract and retained behavior |
| --- | --- |
| Public nullable ItemStack[] slots | Same array/type/ownership. No global null-to-EMPTY replacement. |
| getStackInSlot | Native absence is EMPTY; nonempty result is the original object. |
| getStackInSlotOnClosing | removeStackFromSlot returns original nonempty object and clears backing to null. No old closing wrapper: no actual caller specific to this hierarchy requires one. |
| setInventorySlotContents | Null/native-empty input becomes backing null. Nonempty input is retained by identity and clamped in place to inventory limit 64, not item max. |
| decrStackSize | Full/excess removal returns original object. Partial split returns native split and preserves remainder object. No added amount clamp. |
| getName subclass translation-key hook | Protected getDefaultName hook. Native getName now returns custom name or default key. |
| getInventoryName/hasCustomInventoryName | Narrow wrappers retained for actual GUISoyuzCapsule calls. |
| display name | Literal TextComponentString for custom names; TextComponentTranslation for default key. |
| isUseableByPlayer | Native isUsableByPlayer uses world/BlockPos, same tile identity and squared distance <=128. Old spelling retained only for ContainerSoyuzCapsule. |
| openInventory()/closeInventory() | Native player-parameter no-ops. |
| sided int methods | Native EnumFacing methods. Same default exposed slot {0}, reject extraction, insertion delegated to isItemValidForSlot (default false). No compatibility direction shim. |
| no fields/clear/isEmpty contract | Native zero window fields and no-op setField; clear fills existing array with null; isEmpty inspects native emptiness. |
| old NBT ItemStack loader | Native new ItemStack(tag), with native invalid/empty result normalized to backing null. |

Only setCustomName marks dirty. Setter, decrement, removal, NBT read and the new clear do not add dirty notifications. Real native Slot.putStack supplies its own notification. Public backing-array aliases remain valid after clear.

## Caller audit

A production-source search finds exactly one direct InventoryBase subclass: SoyuzCapsule. It has no inventory operation overrides other than the old getName hook. The native naming collision is resolved by renaming only that hook. GUISoyuzCapsule uses hasCustomInventoryName/getInventoryName; ContainerSoyuzCapsule calls isUseableByPlayer. Those exact wrappers are retained and asserted by the source audit. Other consumers access the public slots array or tile type; that representation is unchanged. These findings do not assert that the surrounding old GUI/container/block classes now compile.

## NBT preservation and native empty differences

Preserved: items list, byte slot, name key; list ordering; signed-byte read bound `b0 >= 0 && b0 < slots.length`; last duplicate wins; absent entries preserve existing backing stacks; missing name becomes empty string; write returns its input compound. No clearing before read, no unsigned slot widening, no setter-based read clamping, and no added dirty marks.

The signed-byte anomaly remains intentional parity: slot 128 writes -128 and is ignored on read, even with a 130-slot test inventory. Soyuz currently has 19 slots.

Native validity differences are explicit, not universal invalid-input parity claims:
- Zero/negative-count or native EMPTY backing reads as EMPTY. Decrement leaves an already-empty backing reference alone; remove clears it. A zero-count old nonnull stack would previously enter the legacy branch.
- Empty input is normalized to null, and native-empty backing entries are omitted from NBT output. Empty/invalid decoded native stacks normalize to null.
- Negative decrement requests are deliberately not clamped: native splitStack(-2) on a five-item stack grows the original remainder to seven and produces an empty result, normalized to EMPTY. This is tested, not endorsed as an inventory operation.
- No copies are introduced for nonempty boundary traffic. Native split/NBT decoding naturally creates new stacks as before.

## Tests and reproducibility

`InventoryBoundaryMinecraftTest.java` compiles alongside the complete actual production base using the resolved real Forge/Minecraft classpath. A small concrete test inventory supplies only the abstract default name and a dirty counter. Tests use real native Slot, ItemStack, NBT and text components. World/EntityPlayer test subclasses run real constructors and distance computation; only tile lookup is controlled, while chunk loading is disabled. No classes are defined under Minecraft/Forge packages and no runtime libraries are replaced.

62 checks pass: empty/null ingress, aliasing, clamping, partial/exact/excess/zero/negative decrement, direct EMPTY/zero backing, removal, in-place clear, dirty ownership, Slot traffic, native names, side defaults, fields, tagged stack round-trip, duplicate/absent/invalid NBT entries, signed-byte edge, and native World/EntityPlayer inclusion at squared distance exactly 128, exclusion just beyond, missing/replaced tile rejection.

Standalone (Java 8, actual platform-specific classpath):

    python3 porting/tests/check-inventory-boundary.py
    python3 porting/tests/probe-inventory-boundary.py --java-home JAVA8 --classpath CLASSPATH

Gradle alternatives, not run by this worker:

    gradlew -I porting/inventory-boundary-probe.init.gradle checkPortInventoryBoundarySource testPortInventoryBoundary

Use gradlew.bat on Windows, with Python chosen through -PportPython=python when needed. The standalone runner uses os.pathsep and Java executable suffix detection. Windows execution is untested.

## Historical audits

`check-inventory-boundary.py` exports `legacy_text(relative, text)` and `restore_texts(texts)`. Call these before an older source audit's own reversals/scanning. The function changes only these two files, validates the entire reviewed native source hash and retained historical-source hash, and returns exact baseline bytes. It refuses subsequent native-source drift rather than masking new changes. This is an audit-only semantic migration record, never a production compatibility layer. Older NBT payload and world-field audits need this bounded reversal; this batch does not edit their shared scripts.

## Gates and limits

Passing the standalone probe does not compile the Soyuz production subclass or surrounding GUI/container, and does not compile MachineBase or the furnace. Full production compile remains parent-owned. Actual world persistence, hopper behavior, client/server loading and furnace gameplay remain pending. Every electric-furnace acceptance checkbox stays unchecked. The next shared-machine migration must separately audit the 125 MachineBase subclasses, not apply this contract blindly.
