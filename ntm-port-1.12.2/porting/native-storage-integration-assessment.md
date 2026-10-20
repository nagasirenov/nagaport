# Native AE2 / Inventory Tweaks integration assessment

Assessment date: 2026-10-19. This document started as a source-only proposal. The user subsequently supplied the requested AE2 release JAR, resolving the AE2 binary blocker. The bounded native implementation and actual-binary checks are now complete; Inventory Tweaks remains separately unresolved. See `tests/ae2-storage-native-notes.md` for reproducible commands, exact test boundaries, and remaining game/runtime validation.

## Implemented / verified update

- Supplied file: `appliedenergistics2-rv6-stable-7.jar`, 4,062,630 bytes. ZIP CRC passes; `mcmod.info` identifies `appliedenergistics2`, `rv6-stable-7`, Minecraft `1.12.2`.
- Computed SHA-256: `e7f7fbf6caaf6206dbd958fb8f9185f874a405289e62ae3e97209b455a89fb1a`. This pins the user-supplied artifact and verified structure/version; no independently authenticated publisher checksum was obtained.
- `install-ae2-dependency.py` validates the artifact and installs it without network access into the narrowly ignored `.local-dependencies/maven` repository. It refuses conflicting existing JAR/POM files and never changes the uploaded source. No third-party binary is included in the patch.
- ForgeGradle successfully resolved and remapped the real full JAR offline as `deobfProvided 'appeng:appliedenergistics2:rv6-stable-7'`. Actual `javap` confirms native API and implementation signatures; the API-only artifact would be insufficient for the original native monitor/adaptor behavior.
- Five existing integration classes migrated, plus a native `IStorageMonitorableAccessor` capability provider. The optional entry point remains `Loader.isModLoaded` guarded; the provider refers to AE2's public capability constant, with no NTM-side `@CapabilityInject` annotation that could force global optional linking.
- Exact eight relevant MSU/AFL helper bodies receive only native count/EMPTY adaptations. Original ordering, caps, type/NBT matching, simulation semantics and long-to-int anomalies remain.
- Actual binary execution: 80 bounded assertions pass, including native AE2 monitors, item channels and Forge capability access. A separate runtime with AE2 entirely absent passes the guarded entry point with zero attempted `appeng` loads and a failing negative adapter-load control.
- The headless harness uses Forge's genuine optional-API transformer on genuine AE2 bytes. It is not a full Forge game launch: unrelated event registration warnings are retained, capability registration is real AE2 but constant assignment is scoped test setup, and attachment is invoked with a genuine event directly. No claim of whole-world/event-bus/client smoke coverage is made.

## Original source-only assessment and transport history

The AE2 root requires a real 1.12 capability migration, not just dependency declarations. Official rv6.stable.7 removes IExternalStorageHandler and externalStorage registration. Its PartStorageBus first queries Forge IStorageMonitorableAccessor and only then generic IItemHandler. Generic sided inventory would lose the original NTM virtual stockpile and AFL recipe-routing semantics, so it is not an acceptable replacement.

At the initial assessment, no real 1.12 AE2 or Inventory Tweaks dependency was cached under /workspace/shared/nagaport-toolchain/gradle-user-home (209 JAR/POM files examined by names). Official tagged API and implementation sources were read successfully. At that stage, real JAR resolution was not established and implementation was paused. The supplied AE2 artifact subsequently resolved this part of the blocker; Inventory Tweaks still lacks a validated binary.

Exact access outcomes:
- Python urllib GET/HEAD of official Forge Maven appeng:appliedenergistics2:rv6-stable-7 POM, full JAR, and API JAR: HTTP 403. This was a successful shell tool returning an HTTP error, not a policy/reviewer denial. No credentials/authentication were requested or used. Response body was not captured for this first probe.
- Official Inventory Tweaks API coordinate variants at Forge Maven: HTTP 403, same distinction.
- Official AE2 website directly links CurseForge file 2747063 and GitHub release rv6.stable.7. GitHub release API reported assets:[]; source archives only.
- The verified official CurseForge AE2 download page returned HTTP 403 from Python, headers server:envoy, content-type:text/plain; body: error code:1010. Public web reader could read the release/download page, but could not materialize a binary or expose a verified CDN URL. The /download/2747063/file endpoint was unavailable in the web reader. No CAPTCHA, login, certificate warning or security bypass attempted.
- No AE2 artifact was fetched from a non-official mirror. Existing unrelated repository declarations were not used as an AE2 fallback.
- Official public raw GitHub source reads are working. Guessed archival branches rv6 and 8.4.7 returned 404; no further branch hunting needed.

## Real dependency identities and provenance

1. AE2 historical coordinates: appeng:appliedenergistics2:rv6-stable-7. Official tagged README documents group appeng, artifact appliedenergistics2 and unclassified full-mod / :api variants; tag gradle.properties pins Minecraft 1.12.2 and Forge 14.23.5.2768, exactly this port's Forge version. The historical README names Forge Maven. Use full-mod deobfProvided, not API-only, if retaining MEMonitorIInventory and IMEAdaptor, because those are appeng implementation classes. ForgeGradle 2.3 must deobfuscate/remap the real runtime JAR before using native named MC ItemStack methods. Coordinate was initially source-verified only; it is now resolved from the validated local full JAR and remapped successfully by ForgeGradle.
2. Official AE2 file: https://www.curseforge.com/minecraft/mc-mods/applied-energistics-2/files/2747063 . Filename appliedenergistics2-rv6-stable-7.jar; uploader AlgorithmX2; game 1.12.2; release 2019-07-23; 3.9 MB. Official website https://appliedenergistics.org/download links this exact file. Official GitHub tag https://github.com/AppliedEnergistics/Applied-Energistics-2/releases/tag/rv6.stable.7 , commit c483bd1.
3. Inventory Tweaks official tag 1.63 declares group inventorytweaks, artifact InventoryTweaks, version 1.63, API classifier api. AE2's rv6 tagged dependencies.gradle instead uses legacy CurseForge Maven group inventory-tweaks:InventoryTweaks:1.63:api. These are distinct publishing namespaces; do not guess they resolve interchangeably. Actual resolver availability is unverified.
4. Inventory Tweaks official file https://www.curseforge.com/minecraft/mc-mods/inventory-tweaks/files/2482481 (project 223094), Filename InventoryTweaks-1.63.jar, 1.12.2, 2017-09-21. That page explicitly lists Inventory Tweaks API 1.63 (17.4 KB) and Deobfuscated 1.63 (231.7 KB) as additional files. The exact additional-file IDs/CDN download URLs have not been retrieved. Its source annotation is unchanged: invtweaks.api.container.ChestContainer retains rowSize, isLargeChest, showButtons. Existing six NTM annotations should remain unchanged when the authentic API is available; do not create replacement annotations in the app.

## Bounded production scope

- build.gradle: exact authentic full AE2 dependency, preferably deobfProvided to avoid making the optional integration a hard distributable dependency. Do not shade APIs or silently add runtime-required dependencies. Inventory Tweaks compileOnly authentic API if independently resolved. Do not retain a knowingly unresolvable coordinate as a build fix.
- src/main/java/com/hbm/handler/ae2/AE2CompatHandler.java: keep public init() with Loader.isModLoaded("appliedenergistics2") gate. Its existing MainRegistry.PostLoad invocation is after mod initialization and before normal world creation, suitable for registering an AttachCapabilitiesEvent<TileEntity> listener. Register the listener only inside the guarded @Optional.Method; no unguarded static AE2-typed fields on this entry class.
- src/main/java/com/hbm/handler/ae2/MSUExternalStorageHandler.java and AFLExternalStorageHandler.java: replace removed external-handler interface with NTM-owned factory/accessor behavior using real Forge capability and AE2 API types, while retaining native monitors. Renaming to appropriate capability helper names is optional, not required. Existing adapters no longer implement absent IExternalStorageHandler.
- New handler/ae2 provider/listener, lazily loaded behind the guard: attach ICapabilityProvider to MSU and all TileEntityProxyCombo plus direct AFL tiles. Resolve a proxy's getTile() only when queried, not during attachment (world/core may not yet be assigned). Return IStorageMonitorableAccessor for direct MSU/AFL or an AFL proxy; false/null for unrelated or MSU proxies, matching original external-handler scope. Use AE2's existing public Capabilities.STORAGE_MONITORABLE_ACCESSOR constant only in this guarded helper. Do not add an NTM @CapabilityInject annotation referring to optional AE2, and do not register AE2's capability yourself.
- Preserve stable accessor identity per resolved tile/core, because PartStorageBus hashes target plus capability identity. If a proxy resolves to a different core, refresh accessor identity rather than retaining a stale core. Each accessor.getInventory(IActionSource) creates a per-bus IStorageMonitorable/monitor wrapping that source, as original handler calls did; do not share one mutable monitor/action source among all buses. getInventory(channel) returns the item monitor for the exact native item channel and null for unsupported channels.
- src/main/java/com/hbm/handler/ae2/MassStorageMEInventory.java and ArcFurnaceLargeMEInventory.java: IActionSource replaces BaseActionSource. IStorageChannel<IAEItemStack> / IItemStorageChannel replaces StorageChannel.ITEMS. AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(ItemStack) replaces createItemStack; IAEItemStack.createItemStack() replaces getItemStack(). AE2 still uses null to signify no extracted stack / no remainder even though MC ItemStack uses EMPTY.
- Retain MEMonitorIInventory(new IMEAdaptor(customInventory, source)) because both real classes exist in rv6 and storage bus recognizes ITickingMonitor, polls external changes and installs listeners. Retain the MSU monitor's isPrioritized override comparing selected filter type + item damage + NBT. This requires full AE2 JAR, not only API.
- TileEntityMassStorage.java: exact helper bodies getTotalStockpile, increase/decrease/changeTotalStockpile, getType and only necessary ItemStack EMPTY/count boundaries. TileEntityMachineArcFurnaceLarge.java: exact helper bodies distributeInput, collectRequested, getAvailableItemFromSlot and directly necessary count/boundary references. Do not convert the whole machine or fix balance/routing issues opportunistically. Some whole-tile old API errors will remain outside this bounded root.

## Original machine behavior that tests must pin

Mass storage (current lines 130-228, same original method logic):
- Filter/type lives in slot 1 and is not inventory stock. No filter or mismatched item+damage+NBT refuses insertion and extraction. No fuzzy/ore matching.
- Inventory count = internal stockpile + compatible slot 0 + compatible slot 2. Incompatible input/output buffers are excluded, never overwritten.
- Both insert and extract visit internal stockpile first, input buffer second, output buffer third. Insert permits reservoir capacity plus both compatible buffer capacities; each buffer cap is the item max stack size, not a generic hardcoded 64.
- Simulate computes the same remainder/result but must not change stockpile, buffers, dirty state, or the caller's AE stack.
- Execute mutates only requested resources and marks dirty in the original helper. Zero result/remainder converts to AE null, not ItemStack.EMPTY leaking into AE API.
- Custom ME access ignores ordinary output-enabled flag, ordinary hopper/slot lock gating, and side. Original MSU external handler accepted every EnumFacing; do not invent face or lock restrictions. Ordinary ISidedInventory remains slots 0/2 with lock restrictions, separately.
- Original cast (int)input.getStackSize() and (int)request.getStackSize() is deliberate existing behavior/comment. Preserve its overflow anomalies in this port unless separately authorized to fix; do not silently clamp long counts as a balance/behavior fix. In particular original fulfilled = long requested minus int missing.
- Native stack migration must not populate a buffer by making a zero-count type copy then invoking logic that reads that empty stack's item identity; the existing zero-count-copy followed by `grow` path was exercised against the real 1.12 ItemStack and preserves item/NBT after regrowth. Preserve the semantic null sentinel in these internal helpers; general public IInventory boundary migration remains outside this root. Do not mix EMPTY and nullable checks so an empty sentinel masquerades as the configured type.

Large arc furnace (current lines 431-532):
- Insertion electrodes visits slots 0,1,2, one each into empty positions; electrode overflow returned.
- Non-electrodes only accepted when ArcFurnaceRecipes.getOutput(item, liquidMode) exists; distribute directly across slots 5-24, not generic automation input slots 25-29.
- Capacity each slot = min(liquidMode?64:solidOutput.maxStackSize/solidOutput.count, input.maxStackSize, getMaxInputSize()). Preserve integer division and scan order; incompatible slots untouched.
- Extraction scans 0-2 only for non-electrode items, then 5-24 only when requested type has no recipe in current liquidMode. Slots 3/4 and 25-29 never exposed by original custom AE inventory; recipe inputs/electrodes are hidden.
- getAvailableItemFromSlot used only indexes 0-24, excludes current electrodes, recipe inputs, 3 and 4. Original helper returns underlying stack; channel factory copies it.
- Custom AE operations do not impose ordinary lid conditions (ordinary canExtractItem does). Do not add lid gating or force ordinary sided wrappers.
- Simulate may mutate the local MC stack produced by IAEItemStack.createItemStack (a new copy), but never tile slots or input AE stack; execution must preserve original partial-fill / partial-extract bookkeeping. Original extraction helper does not explicitly call markDirty; do not silently add unrelated persistence behavior while claiming exact port.
- Proxy handler accepts any TileEntityProxyCombo whose core is AFL, regardless of proxy inventory flag, and direct AFL. Preserve this rather than checking only ordinary inventory-capable proxies.

## Completed source-parity evidence

Before migration, a brace-aware extraction compared the port against ntm-original-pristine for all eight relevant helper bodies: MSU getTotalStockpile, increaseTotalStockpile, decreaseTotalStockpile, changeTotalStockpile, getType; AFL distributeInput, collectRequested, getAvailableItemFromSlot. All eight bodies were byte-for-byte identical after text newline normalization. Thus the semantics documented above are original pinned behavior, not inferred from a derivative port. That comparison captured the original baseline before migration. Native compilation/runtime tests and the supplied-artifact hash are now recorded in the implementation update above.

## Validation checklist and remaining scope

1. Completed through the user-supplied artifact: record source, size and computed SHA-256, local Gradle remap, and actual javap signatures for IActionSource, IStorageChannel, createItemStack, MEMonitorIInventory, IMEAdaptor and Forge capability attach API. Independent publisher checksum/signature authenticity was not obtained.
2. Completed: compile all five real integration classes plus new provider against genuine native Forge/MC + deobfuscated AE2. Use explicit NTM fixtures only for unrelated unported NTM tile surroundings; no appeng/net.minecraft/net.minecraftforge stand-ins. Extract exact production helper methods into the native test harness if the full tiles cannot compile, with source identity checks.
3. MSU cases: no filter; type/damage/NBT mismatch; reservoir+both buffers exact fill; overflow; incompatible buffers; item stack cap 1/16/64; zero available; partial extraction; reservoir/input/output order; simulate then execute equality; dirty and input mutation checks; selected-type prioritization; count long-to-int anomaly snapshots.
4. AFL cases: three electrode slots; liquid/solid max capacities; partial merge; wrong recipes; all slots full; partial/complete extraction; exclusion of electrodes/recipe inputs/slots3,4,25+; lid state parity; simulation immutable tile state and AE input; face matrix for direct/core/proxy/unrelated/missing core; capability identity stability and replacement when resolved core changes.
5. Real monitor tests: enumeration through AE ItemList, listener notifications for execute and for external stockpile changes followed by monitor tick, source forwarding, supported/unsupported channel, storage-bus polling compatibility and prioritized type.
6. Completed AE2 absence test: no appeng classes on the runtime classpath; actual Forge Loader guard executes without even requesting them. Presence test verifies direct attach handler invocation and item-channel path. Full event-bus routing, actual chunk unload/reload and game/server smoke remain pending. Inventory Tweaks binary resolution/classloading is outside this completed AE2 root.

## Primary source links

- https://raw.githubusercontent.com/AppliedEnergistics/Applied-Energistics-2/rv6.stable.7/README.md
- https://raw.githubusercontent.com/AppliedEnergistics/Applied-Energistics-2/rv6.stable.7/gradle.properties
- https://raw.githubusercontent.com/AppliedEnergistics/Applied-Energistics-2/rv6.stable.7/gradle/scripts/artifacts.gradle
- https://raw.githubusercontent.com/AppliedEnergistics/Applied-Energistics-2/rv6.stable.7/gradle/scripts/dependencies.gradle
- https://raw.githubusercontent.com/AppliedEnergistics/Applied-Energistics-2/rv6.stable.7/src/main/java/appeng/parts/misc/PartStorageBus.java
- https://raw.githubusercontent.com/AppliedEnergistics/Applied-Energistics-2/rv6.stable.7/src/main/java/appeng/me/storage/MEMonitorIInventory.java
- https://raw.githubusercontent.com/AppliedEnergistics/Applied-Energistics-2/rv6.stable.7/src/main/java/appeng/util/inv/IMEAdaptor.java
- https://raw.githubusercontent.com/AppliedEnergistics/Applied-Energistics-2/rv6.stable.7/src/api/java/appeng/api/storage/IMEInventory.java
- https://raw.githubusercontent.com/AppliedEnergistics/Applied-Energistics-2/rv6.stable.7/src/api/java/appeng/api/storage/IStorageMonitorableAccessor.java
- https://raw.githubusercontent.com/AppliedEnergistics/Applied-Energistics-2/rv6.stable.7/src/api/java/appeng/api/storage/IStorageMonitorable.java
- https://raw.githubusercontent.com/AppliedEnergistics/Applied-Energistics-2/rv6.stable.7/src/api/java/appeng/api/storage/IStorageHelper.java
- https://raw.githubusercontent.com/AppliedEnergistics/Applied-Energistics-2/rv6.stable.7/src/api/java/appeng/api/storage/data/IAEItemStack.java
- https://raw.githubusercontent.com/Inventory-Tweaks/inventory-tweaks/1.63/gradle.properties
- https://raw.githubusercontent.com/Inventory-Tweaks/inventory-tweaks/1.63/src/main/java/invtweaks/api/container/ChestContainer.java

The linked official sources are reference material only, not production stand-ins. No separately downloaded source tree is required to reproduce the native proof.
