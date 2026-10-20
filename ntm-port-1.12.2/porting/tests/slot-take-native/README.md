# Native Slot take callback closure

Exact baseline: CP81, `242867906a53eb7da03c879f476ced61c1170e14`.

This is a bounded callback migration, not an inventory storage or quantity migration. The existing TileEntityMachineBase IInventory/ISidedInventory bridge was already native; it is unchanged. No fake vanilla API, compatibility callback shim, source exclusion, or production placeholder is added.

## Production boundary

32 Java files change:

- Seven legacy `onPickupFromSlot(EntityPlayer, ItemStack)` declarations become actual `ItemStack onTake(EntityPlayer, ItemStack)` overrides: shared SlotCraftingOutput, the anonymous output plus named input slot in each of ContainerAnvil, ContainerArmorTable and ContainerWeaponTable.
- Seven superclass edges become native `super.onTake` calls at exactly their original positions. SlotCraftingOutput returns the native parent's result. The other six return their original stack argument after all original side effects, including both early returns in the anvil output callback.
- 32 ordinary Slot-typed invocations become `onTake` with identical receivers and arguments. Their returned value remains ignored, matching the original void calls.

The original callback bodies are otherwise byte-identical under the exact signature/return inverse. In particular, SmithingSlot's superclass invokes its virtual onSlotChanged, then the explicit updateSmithing call still runs: the original double update is not silently deduplicated. Craft buffer resets still occur after the achievement callback; failures still stop later dirtying/consumption/reset. All original null tests, array storage, ItemStack quantity reads, transfer algorithms, recipe selection, energy logic, NBT, output accounting and GUI code remain unchanged.

The historical 1.7 callback mention in LegacyCraftingCallbacks Javadoc is explicitly excluded and unchanged. Existing native SlotSmelting, ContainerNT and ContainerBase callbacks/callers remain untouched. No direct raw quantity reader is added or extended.

## Inheritance and current compiler evidence

Read-only javac parse/analyze covers all 4,065 production Java units against the actual cached Forge 1.12.2/stable_39 classpath. It does not emit production classes or run Gradle. The complete 22-class Slot subtree, including anonymous classes, has identical inheritance before and after. All seven selected methods are genuine native overrides after the change. All 39 selected call edges resolve to native Slot.onTake or the migrated SlotCraftingOutput.onTake; every ordinary receiver is actually typed Slot, not a domain lookalike. No legacy method/call remains in the attributed source graph.

Diagnostics: 12,471 before, 12,429 after. Exactly 38 missing-method diagnostics and four invalid-override diagnostics disappear, with zero additions after ignoring line shifts only. The other three declarations did not produce an invalid-override error: two lacked @Override and the anonymous anvil output already overrode the old shared root. The 39th call was its valid old shared-root superclass call. Counts are diagnostics, not completed classes or percentages.

The exact CP81 full-compile input hash is recorded. This family does not claim a whole-mod build succeeds. Complete containers and SlotCraftingOutput still have unrelated unported APIs, including quantity access; this migration deliberately does not hide or repair those outside scope.

## Actual engine and runtime evidence

The supplied original 1.7 client and SRG map aay.a(yz,add) to Slot.func_82870_a. Actual original bytecode invokes virtual onSlotChanged once and returns void. Actual native Slot.onTake invokes virtual onSlotChanged once and returns the identical argument without normalization. Twenty-four tests execute those genuine old/native parent implementations, including inherited dispatch and native null/EMPTY/zero-count/live identity cases.

117 native assertions execute all seven complete selected callback bodies against genuine native Slot, ItemStack, InventoryBasic, EntityPlayer and Container.slotClick, including native PICKUP for all selected owners and THROW for the crafting root. Tests cover return identity, crafting/achievement/dirty order, captured owner versus callback player, buffer reset and exception order, anvil null/no-match/normal/swapped/first-match exits, exact consumption arguments, original SmithingSlot double update, armor applicability/removal, weapon mod alias/index forwarding, final index reset, install failure, and all 32 verbatim ordinary invocation edges. Real native Container dispatch, Slot parent methods and ItemStack crafting execute; no net.minecraft or net.minecraftforge class is faked.

Fixture limits are intentional:

- SlotCraftingOutput is compiled with only its separate still-unported decrStackSize method omitted. Its selected callback, constructor, isItemValid, checkAchievements and both onCrafting overloads are unchanged. The test seeds craftBuffer explicitly. It proves callback dispatch/reset, not original quantity accumulation or complete class readiness.
- Container owners are projections containing the exact selected callbacks. SmithingSlot.onSlotChanged and ModSlot.refreshInstalledMods are also complete production methods. Recipe matching/consumption answers, updateSmithing, achievement dispatch, armor applicability/removal and weapon installs are recording domain collaborators. Their algorithms are not certified here.
- Native inventory fixtures optionally return null solely to exercise retained original null guards. Production storage is not normalized or rewritten. Three additional boundary checks explicitly demonstrate the unresolved native-empty behavior: empty anvil inputs pass the old null guard to recipe matching, and armor-removal / weapon-refresh collaborators receive native EMPTY rather than null. Their real EMPTY handling is not certified or changed. These table inventories are not being declared gameplay-ready.
- The real lightweight player/world subclasses do not start a server or simulate world/chunk gameplay. Native transfer algorithms from the affected production containers are not compiled or certified. Call-edge wrappers preserve the exact invocation statements but not surrounding transfer methods.

Four behavior mutants are rejected: omitted native dirtying, lost anvil return identity, omitted weapon index reset, and crafting after dirtying. Restoring the old shared-root callback fails compilation against genuine native Slot. All 32 entire-file inverses, seven complete callback body inverses and 96 source drift rejections pass. These are bounded callback/dispatch claims, not complete inventory, recipes, upgrades, achievements, machine or gameplay parity.

## Reproduction

From this module's root, using already cached inputs:

    python3 porting/tests/slot-take-native/source-proof.py
    python3 porting/tests/slot-take-native/run-attribution.py JAVA_HOME CLASSPATH_FILE OUTPUT_DIR
    python3 porting/tests/slot-take-native/run-native.py JAVA_HOME CLASSPATH_FILE LEGACY_CLIENT_JAR NATIVE_OUTPUT_DIR
    python3 porting/tests/slot-take-native/run-mutants.py JAVA_HOME CLASSPATH_FILE NATIVE_OUTPUT_DIR

The source proof exposes reverse_source(relative, text) and fails closed on any drift. The attribution runner checks all 4,065 source blobs against the exact sealed Git tree, builds a temporary baseline of immutable originals/unchanged source links, and compares typed before/after source. It performs no network, installation or full Gradle build. Native runtime files and mutant outputs are confined to the supplied output directory. The classpath/legacy compiler-input hashes, mapped old methods, selected attribution and native/original bytecode are retained with this evidence.
