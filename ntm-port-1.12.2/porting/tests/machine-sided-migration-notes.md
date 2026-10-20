# Native sided inventory callback migration

2026-10-19. Bounded production change after `machine-inventory-consumer-audit.md`.

## Implemented and frozen scope

- 118 legacy ordinary `getAccessibleSlotsFromSide(int)` declarations become `getSlotsForFace(EnumFacing)`.
- 46 ordinary insert and 109 ordinary extract declarations receive native EnumFacing parameters.
- Their 273 original bodies are retained, with a local integer reconstructed through the existing `ForgeDirection.fromEnumFacing(nativeFace).ordinal()` at the start. This preserves six faces and owned UNKNOWN/null = 6 defaults without `EnumFacing.byIndex` wrapping.
- 36 audited ordinary call expressions are converted to native arguments. There are 14 caller files, 120 declaration files, and 125 total edited production files. The five caller-only files are CraneBoxer, CraneInserter, CraneUnboxer, PneumaticNetwork and InventoryUtil.
- All 19 coordinate-domain declarations remain (7 access, 5 insert, 7 extract, including interface declarations). Only the eight explicit ordinary delegates in ChemicalFactory, AssemblyFactory and PASource are converted. Their current ordinary targets ignore side, including inherited base insertion; their existing behavior for raw int domain values is consequently unchanged.
- Generic wrappers are included: ProxyInventory, ProxyCombo, PWR block proxy and PWR controller. Their conditional-domain forwarding retains coordinates and side integers.
- No changes to nullable getters, storage, EMPTY handling, recipes, stack mutations, energy, names, NBT, player callbacks, network payloads, or balance. InventoryBase already-native callbacks are not changed. ItemStackUtil belongs to a separate concurrent migration and is not included here.

## Important null/sentinel boundary

Native real face values preserve legacy ordinal ordering. Owned native callbacks explicitly handle null as legacy UNKNOWN6. Native Hopper bytecode confirms null insertion can reach canInsertItem, while putStackInInventoryAllSlots skips getSlotsForFace for null.

Generic ISidedInventory implementations do NOT promise getSlotsForFace(null). Existing malformed metadata/missing crane tiles can produce UNKNOWN; the audited conversions preserve UNKNOWN as null rather than choosing a physical face, rejecting, or inventing fallback behavior. Generic calls that encounter that malformed state remain an explicit compatibility edge; no claim is made that arbitrary external mod inventories tolerate it. Normal PneumoTube send/registration guards reject UNKNOWN before ordinary traffic. Valid registered machine metadata yields real faces for transporters/Dyson/cranes.

No broad legacy inventory interface, all-purpose shim, replacement machine, or no-op callback has been introduced.

## Fail-closed source proof

`machine-sided-source-manifest.json` records each concrete declaration and call, exact sequential edits, and whole-file before/after SHA-256 hashes. `check-machine-sided-source.py` exports `reverse_source(relative, text)` for historical source-check composition. Call this reversal BEFORE the older coordinate/world/tick reversals. It refuses any additional content drift in an owned file and requires exact restoration of its pre-migration snapshot. No Git objects or local pristine tree are needed.

Command:

    python porting/tests/check-machine-sided-source.py

Verified: 273 declaration mappings, 36 call mappings, 125 exact full-file reversals, 125 deliberate drift rejections, and all 19 retained coordinate-domain declarations. The all-source typed scan finds no remaining legacy ordinary native signatures. Comments, strings and unrelated overloads are not treated as callbacks.

## Native dispatch probe

`machine-sided-native-test.py` takes `--java-home` and `--classpath`; it uses platform executable suffixes and os.pathsep. It extracts current production callback signatures/bodies and slot arrays without rewriting them, then compiles with the actual resolved Minecraft/Forge artifact and complete production InventoryBase, ForgeDirection and IBatteryItem. Fixtures deliberately control registry identities, charge values, ItemStamp, and proxy target resolution. These are not fake Minecraft classes.

Final verified probe: 1,505 assertions covering all six faces/null, ElectricFurnace, Press, StorageDrum, directional battery/ICFPress/Funnel, battery charge extraction, proxy missing target and exact stack/face/array forwarding. Actual ProxyCombo callbacks and production IConditionalInvAccess additionally verify disabled/missing/noninventory cores and exact conditional coordinates (17, -3, 209), slot/stack/array forwarding, and null-to-legacy-side6 propagation. The production proxy target resolution is a controlled fixture dependency; callback bodies are extracted unchanged.

Example with a previously resolved native classpath:

    python porting/tests/machine-sided-native-test.py --java-home /path/to/java8 --classpath "<native Minecraft/Forge and dependency classpath>"

This proves EXTRACTED production callback dispatch, not compilation of complete machines. InventoryBase native success is not MachineBase or furnace runtime proof. Full production compile and genuine registered machine client/server acceptance remain separate parent-owned gates. No Gradle or commits performed for this batch.

Final evidence logs: `porting/machine-sided-source-2026-10-19.log` and `porting/machine-sided-native-2026-10-19.log`. Whole-file reversal retains existing CRLF bytes in CrateBase and existing trailing whitespace; git diff whitespace notices for these preserved bytes are not a source-parity mismatch. No post-freeze production edits.
