# Native achievement backend: bounded acceptance evidence

Production freeze: 2026-10-19, CP28 continuation. This implements the approved single-tab presentation, not a recreation of the old GUI. The acceptance below is genuine native MC 1.12.2 / Forge 14.23.5.2768, not whole-mod compilation or an in-game player acceptance claim.

## Production scope and semantics

- `com.hbm.advancement.AchievementDefinitions`: exact 63 active NTM mappings and 42 direct gameplay-parent edges; seven vanished vanilla dependencies. `rotConsum` is still null, not a 64th definition. The historical `achConcrete` assignment contains a tab before `=` and must not be missed by source counting.
- `NtmAdvancements`: real `AdvancementManager` / `PlayerAdvancements` completion. It resolves objects afresh after reload; does not cache advancement instances. Only an actual server player, never standard Forge `FakePlayer`, may receive awards. Null is a no-op. A missing direct parent discards the event immediately, with no replay or recursive ancestor check. Native `StatBasic` counters supplement real advancements, one increment for every parent-qualified original attempt, including repetitions. They do not stand in for achievements.
- 71 actual `assets/hbm/advancements` JSON files: 63 NTM, seven dependencies, one presentation-only root. All use the native impossible criterion. No XP, loot, item, recipe or function rewards were introduced. The root is granted with the first qualifying award, has no gameplay gate, toast or chat. Original special frames become native challenge frames; layout changes are presentation only. The original `hidden` award was not actually hidden and is not hidden here.
- `AchievementHandler`: original 35-entry item/block dispatcher and metadata matching retained. The complete production `RecipesCommon` is compiled unchanged in the native test, not an approximate replacement equality implementation.
- `LegacyAchievementEvents`: successful captured post-pickup logs/log2, crafted workbench/any `ItemPickaxe`/furnace, and player furnace extraction iron/cooked fish. No possession predicate or generic machine crafting was added. The parent's inventory-action C2S packet owns the seventh, explicit inventory-key action. Native `SlotFurnaceOutput` and production `SlotSmelting` feed the smelting listener; the old extra explicit custom-slot award branches are removed, not executed alongside it.
- All original callers, MainRegistry activation and gneiss reward/read integration are owned by the parent. In particular, gneiss's original unconditional 500-XP assignment after the guarded award attempt must remain at that call site. There is no advancement reward to double it, and no `award` return-value gate is a substitute for the original condition.

`achievement-definition-baseline.json` records the actual original 63 declarations, exact legacy strings, names, icons/metadata and parents, plus the seven verified vanilla dependencies. `achievement-crafting-baseline.json` records all 35 original map entries. Tests need neither Git history nor the original jars at runtime. `achievement-native-source-hashes.json` records this bounded production freeze.

## Exact repeated-callback evidence and native adaptation

The original official 1.7.10 client and Forge 1614 patches were examined, not inferred from the removed API:

- Official client SHA-256 `a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`.
- Forge1614 userdev SHA-256 `6ed364c0defdb481a41ef2d99c02d382f16fc42a6ea0ea1c2b849848d8466e91`.
- Original mappings: `aaj` ContainerFurnace, `aak` SlotFurnace, `aax` SlotCrafting, `aay` Slot, `add` ItemStack, from the pinned userdev `conf/packaged.srg`.
- Original furnace transfer calls `Slot.onSlotChange` and then `onPickupFromSlot`. Both invoke the furnace callback and achievement checks, even when the second stack has count zero. The old ItemStack still exposed its raw item. Native full transfer keeps both callbacks but hides a zero-count item's identity as AIR. Native `copy()` retains the raw item and its delegate; normalizing only that copy recovers the original achievement comparison. The original inventory stack, count and NBT are not changed. A shared `ItemStack.EMPTY` remains AIR and receives no award. Zero and negative raw stack counts are also checked without mutating source serialization.
- Hence normal furnace player extraction attempts once and a full shift transfer attempts twice, for both native and NTM output slots. The test executes actual native `ContainerFurnace.transferStackInSlot`, not a hand-posted proxy for those paths. Empty-source transfers attempt zero times. Repeated callbacks are not deduplicated merely because the award is already complete.
- Original SlotCrafting's *vanilla* achievement checks ran in both callbacks, whereas Forge1614's FML crafting event was inserted only in `onPickupFromSlot`. Forge2768 emits the FML event inside positive `amountCrafted` processing instead, leaving only one event for a normal shift pair. `LegacyCraftingCallbacks` recognizes only the exact native standard workbench/player-container transfer chain and restores the second legacy-vanilla attempt. It never doubles the 35-entry NTM dispatcher. Normal crafting remains one attempt. Nearest FML callback provenance plus exactly one intervening event post prevents nested direct/FML posts from borrowing the outer shift context.
- Native MCP/SRG mappings verified from cached MCP stable39 `mcp-srg.srg`: SlotCrafting single callback `func_75208_c`, amount callback `func_75210_a`, Slot change `func_75220_a`, both standard transfer methods `func_82846_b`. No ASM patch, ItemStack shim, private-item reflection or remembered per-player event state is used.

This correction deliberately targets actual unmodified vanilla standard-container flow. It does not claim equivalence for arbitrary third-party containers, overridden callbacks/coremods, fabricated events, legacy cancellable `AchievementEvent` integrations, or old achievement scoreboard/command/revoke semantics. No NTM caller subscribes to that old cancellation event. Auxiliary counter files are native new-format statistics, not automatic conversion of original achievement saves.

## Localization and resource loading

The 14 removed vanilla English title/description keys are appended without overwriting existing HBM keys. Exact text and source provenance are captured in `achievement-language-baseline.json`; the cached official client contains only the English language asset. Other locales use English fallback for these seven dependencies, not invented translations. Existing HBM translations, including Russian, remain untouched. The inventory description retains its old `%1$s` placeholder and receives a native `keybind: key.inventory` text component so a rebound key need not be hardcoded.

Uppercase locale filenames are correct for the project's current resource setup: there is no pack.mcmeta, so genuine Forge folder/file packs synthesize pack_format 2. Actual `FMLClientHandler.addModAsResource` selects native `LegacyV2Adapter`, which maps lowercase native requests to the existing uppercase country filenames. Renaming alone would have broken this adapter. The test executes that exact selection, real resource reload and native `Locale` parsing for both a folder and a temporary test archive. No release jar was packaged.

## Reproduction and terminal result

Run:

```
python3 porting/tests/probe-native-achievements.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat porting/continuation-2026-10-19/native-probe-classpath.txt)"
```

Terminal log: `porting/continuation-2026-10-19/native-achievements-FINAL.log`.

- 960 native achievement definition/gate/counter/persistence/dispatch assertions.
- 13 authenticated empty-action packet/native server queue assertions from the parent's companion test.
- 44 native Forge language/adapter assertions.

All 71 production assets are discovered by the genuine Forge mod-asset path, parsed by native AdvancementManager, and built with native criteria/display/rewards. Native PlayerAdvancements and StatisticsManagerServer save real files, then restore them in a replacement player; manager reload uses new advancement objects; native `setPlayer` retains respawn progress. Every gameplay definition is tested for repeat attempts and every direct-parent gate for early discard/no replay; deeper chains are tested with only the direct parent externally granted. Native completion events occur once, counter repeats persist, FakePlayer and null leave both stores unchanged.

Actual EntityItem pickup is exercised with success, Forge cancellation and full-inventory rejection. Actual vanilla and production NTM output slots exercise ordinary/bulk takes and standard-container full shift transfer, iron and both cooked-fish variants. Both genuine standard crafting containers exercise all three predicates on normal/shift paths. Nested callback negatives, shared EMPTY, native icon metadata, all 35 metadata-sensitive craft mappings and the non-doubling NTM native shift path are covered.

## Explicit fixture boundaries / remaining acceptance

The complete backend/events/helper/dispatcher, unchanged full RecipesCommon, production SlotSmelting, and production inventory packet are compiled against the genuine MC/Forge jar. Test-only NTM registry tables provide real native registered item/block identities, rather than loading the still-incomplete entire mod. Unsafe allocation bypasses server/world/player startup only: native advancement/stat objects, their logic and files, registries, events, inventory classes and container transfer methods are real. World chunk ticking, player XP delivery and pickup network/entity tracking are isolated boundary methods. Slot tests supply outputs to actual inventories; they are not acceptance of every mod recipe or machine.

Live transport, physical keyboard/mouse rebinding, multiplayer client UI/toasts/tab rendering, full player death/respawn/login flow, original-world/player-save migration, and real player radiation/Bobmazon/gneiss gameplay remain separate acceptance work. The synthetic fixtures and native file reload checks do not close those game-level items.
