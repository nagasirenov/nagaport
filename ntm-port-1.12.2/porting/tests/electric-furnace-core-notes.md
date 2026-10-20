# Electric furnace native production core

Baseline: checkpoint17 `48bd00359478c16dc9c47891073e700cb8107cb6`. Four complete production files changed: `TileEntityMachineElectricFurnace`, `ContainerElectricFurnace`, `ItemMachineUpgrade`, and `IUpgradeInfoProvider`. Original/pristine, recipes, upgrade manager, battery implementation, SlotUpgrade and SlotSmelting are not changed by this core batch. Other furnace block/model/GUI work has its own guards and evidence.

## Native implementation and preserved behavior

- Actual FurnaceRecipes.instance lookup returns native EMPTY for missing recipes. Native ItemStack counts use getCount/grow/shrink, while the inherited nullable machine array still clears to null. Native container absence/failed shift-click/Slot ingress use EMPTY. No global array conversion or old-API shim.
- Server tick order remains cooldown decrement; battery transfer; modulo40 six-direction subscriptions; upgrade recalculation; insufficient-energy cooldown assignment; progress/energy/soot; optional conversion; block-state callback; packet; dirty notification. Client ticks do none of these. Native world/BlockPos access replaces removed fields.
- Ordinary processing is 100 ticks at50HE, capacity100000. Speed tiers1–3 are delay75/50/25 and cost100/150/200; power tiers1–3 are delay110/120/130 and cost35/20/5. Insufficient power repeatedly resets20-tick cooldown; replenishment does not skip it. Interruptions reset progress.
- Original output-capacity OR expression is unchanged. It checks the existing output count, not count plus recipe result: count63 plus a3-item result still becomes66. Falling through can allow count20 despite an inventory limit1. No balance/overflow repair. Output result count and tags are copied for empty output; equal output merges into its existing identity/tags.
- Original input completion loop, including its nonpositive-count branch, remains present. Native zero/negative stacks are EMPTY and hide item identity, so those invalid input values cannot reach that old resurrection branch. Native EMPTY output is treated as absence, including a pre-existing invalid zero-count output whose identity is no longer available. A depleted positive input becomes null in backing storage while its former alias becomes a native empty stack. These representation differences are explicit, not claims of invalid-count 1.7 identity parity.
- Original completion trigger is retained: after a completion with another processable input, the block callback is skipped; the completion still marks dirty. A final recipe deactivates. A tick that just spent its last energy can still leave progress1/active=true until the next tick resets progress.
- Same-block state/metadata changes require a narrow shouldRefresh override returning oldBlock != newBlock. Forge1.12's modded default otherwise recreates the tile for facing changes, unlike1.7 metadata edits. Actual off/on block identity changes and removal still request refresh. The separate block suite exercises its save-and-restore swap lifecycle.
- UpgradeManagerNT is compiled complete and unchanged. Same-stack reference cache mutations are intentionally missed until replacement. Tier sums are capped as before. Persistent mutexType is not reset: after removal/rebuild, lower or equal mutex upgrades can remain absent. Public cache/map mutation can expose zero-duration/signed-overflow formulas; those are not clamped. No energy simulation flag is added.
- Container keeps its original routing/ranges and onSlotChange call. It does NOT acquire an onTake call. SlotSmelting's old achievements/XP/event implementation is outside this batch; the probe records callback dispatch with an explicitly controlled NTM SlotSmelting collaborator and cannot prove XP or achievement behavior.
- Upgrade tooltip overrides the actual World/List<String>/ITooltipFlag native method and forwards flag.isAdvanced. The sole ItemMetaUpgrade descendant does not override this method and is unchanged. Block label uses native getTranslationKey. Tooltip execution in a real screen and localization resource loading are not tested.

## Complete native compile boundary

The runner passes these25 complete, unmodified-at-test-time production source files directly to javac: furnace tile/container; ItemMachineUpgrade/IUpgradeInfoProvider/UpgradeManagerNT/SlotUpgrade; ItemBattery/ItemBatteryCreative/IBatteryItem; MachineBase/LoadedBase/IGUIProvider/IBufPacketReceiver; IEnergyReceiverMK2/IEnergyHandlerMK2/IEnergyConnectorMK2/IEnergyConductorMK2/ILoadedTile; ForgeDirection/Tuple/NotableComments/Spaghetti/AudioWrapper/ThreadedPacket/PrecompiledPacket.

Real Minecraft1.12.2, Forge14.23.5.2768, Netty, vanilla Slot/Container/InventoryPlayer, FurnaceRecipes, ItemStack/NBT and World subclasses are used. Test items are registered through ForgeRegistries.ITEMS; no Minecraft/Forge class is replaced. Full concrete batteries, full upgrade cache and actual inherited transferPower run.

Explicit NTM collaborators are atmosphere routing/fluid names; coordinate data carriers; node/network outcome and Compat tile lookup; ModItems/ModBlocks identity sets; block-state effect recording; pollution effect recording; GUI constructor; SlotSmelting callback recording; localization/number formatting; PacketThreading and debug-particle sink. Pollution's original SOOT_PER_SECOND=1F/25F is retained. Library.chargeTEFromItems and direction constants are extracted verbatim, not independently rewritten. BufPacket fields/constructors/read/write body are extracted verbatim, excluding its still-unported client Handler. Actual LoadedBase packet deduplication and actual PrecompiledPacket buffer creation run. Neither topology, full pollution handler, full Library, full packet handler/thread dispatch, achievements nor registered production item/block bootstrapping is accepted by these collaborators.

## Final evidence

`electric-furnace-core-native-FINAL.log`:484 direct native assertions pass. Coverage includes:

- Complete100tick vanilla smelt and each real upgrade tier's exact completion/cost; real battery source debit = machine receipt +5000 consumed; input/output conservation.
- Battery charge/rate/capacity, zero and signed-long extreme matrix on actual furnace ticks, negative free-capacity behavior, zero-capacity durability NaN, inherited transferPower negative/overflow behavior, scaled integer/long overflow and zero divisor.
- Actual native recipe rejection, multi-result and overfull output, output tags/aliases, all six sided inventories, empty-battery extraction, source alias depletion.
- Server/client separation,20tick cooldown,40tick subscription order/coordinates,20tick soot and block/packet/dirty call order; actual receiver→loaded cache→controlled conductor network boundary.
- Native compressed NBT preserves items/slot/name/power/progress/muffled and battery charge; cooldown/maxProgress/consumption remain transient. Missing tags retain original inventory-overlay behavior. No real game-save/chunk-reload claim.
- Real serialization bool/long/int/int order, long power above65535, progress200003/maxProgress300007 without window-field truncation, packet coordinate envelope, unchanged-packet suppression and modulo20 resend.
- Real native container Slot merge, battery/input/upgrade routing, invalid recipe rejection, partial/full transfer conservation, native EMPTY return/clears, and absence of added onTake.

`electric-furnace-combined-native-FINAL.log`:18 additional focused assertions pass with actual production MachineElectricFurnace + its actual production tile/container/batteries/upgrades compiled together, not swapped target stand-ins. The adapted native World/Chunk fixture creates the actual tile through actual block placement. Native container insertion feeds a100tick battery smelt; real off/on callbacks preserve the same tile, slot array, battery alias, name and facing without drops. Energy debit equals stored receipt plus5000HE. A metadata-only edit retains the tile; the preexisting real container extracts the result; a speed3 recipe completes25ticks for5000HE. Real native block removal drops each remaining battery/output/upgrade once. Actual inherited markDirty/World/Chunk callbacks run, unlike the observation-only subclass in the larger core suite. NBT is read from the actual retained tile, not a substitute. The fixture controls lighting checks, entity-spawn capture, chunk provisioning, GUI, pollution and network collaborators; this remains headless combined evidence rather than live game/server/save acceptance. Production source SHA256s are printed in both final native logs. GUI creation/XP remain explicitly outside this combined result.

`electric-furnace-core-source-FINAL.log`: four exact whole-file reverse/forward reconstructions and12 mutation rejections pass. The portable manifest contains all edit payloads plus before/after hashes, requiring no local Git objects. `reverse_source(relative,text)` passes unrelated files through and fails closed for known drift; compose it before checkpoint17's historical layers. Existing historical hashes are not refreshed. Unchanged manager, slot and subtype hashes are separately checked.

These are isolated complete-class native tests, not a clean full production build, runnable mod, actual GUI, live client/dedicated-server, world-save, network-thread, or XP/achievement acceptance. Parent-owned common compile and separate source-layer integration are reported separately. No production exclusions, fake APIs, placeholders or push.

## Reproduce

From project root with actual local native platform classpath (saved continuation classpath is workspace-specific):

    python3 porting/tests/check-electric-furnace-core-source.py
    python3 porting/tests/probe-electric-furnace-core.py --java-home /path/to/jdk8 --classpath "ACTUAL_NATIVE_CLASSPATH"
    python3 porting/tests/probe-electric-furnace-core.py --combined --java-home /path/to/jdk8 --classpath "ACTUAL_NATIVE_CLASSPATH"

Optional Gradle task resolves that platform classpath without full compilation:

    ./gradlew --offline -I porting/electric-furnace-core-probe.init.gradle testPortElectricFurnaceCore testPortElectricFurnaceCombined

Use gradlew.bat on Windows and -PportPython=python as needed. The portable runner uses os.pathsep, native executable suffixes and UTF-8, and promotes Java8's reported jre path to its parent JDK. Windows execution is not tested. The optional Gradle task itself is supplied but not redundantly run after the final direct javac/java probe.

Forge registration warnings about the `furnace_probe`/`furnace_combined` test namespace are expected because the headless bootstrap has no active mod container. Tests still use the real Forge registry API. No warning is suppressed or presented as production bootstrap acceptance.
