# First machine vertical slice: ElectricFurnace

Status: PLANNED, not runnable; full production compilation is still a prerequisite. This contract does not replace the remaining port or authorize exclusions, substitute implementations, changed balance, or a CE base.

## Reference

Documented upstream revision: `cf6a396d6c3858880c5da39982cce5ebba9582c9`, named in PORTING-1.12.2.md and the root plan. The object is absent from this clone; the plan explicitly treats it as documentation, not verified archive provenance. Do not claim verified upstream byte equality.

Reproducible supplied reference: `dff345a2411f7d3d6bef5c0b6c3aa8ab47ac357e:ntm-original-pristine`, tree `b6f2e5a876b04ed3d72a16a91f465c6b909756f3`. The tree remains unchanged in checkpoint 2.

## Why this machine

ElectricFurnace has four slots and vanilla FurnaceRecipes, a conventional block and GUI. Shredder additionally requires thirty slots, custom recipe/ore dictionary infrastructure, blade wear, distributed inputs, sound and a destructive bobblehead branch. ElectricFurnace still requires real shared base, inventory, energy, pollution, upgrade, XP and network behavior. Test-fixture substitutes do not make it an end-to-end slice.

## Required implementation order

1. Assign one owner to TileEntityLoadedBase/TileEntityMachineBase. Port actual ticking, World/BlockPos, NBT return and inventory contracts. Audit the 125 direct MachineBase subclasses and 79 direct LoadedBase subclasses before changing shared null/EMPTY assumptions. Counts are current-source observations, not a promise that these are the only consumers.
2. Port the full machine and Library.chargeTEFromItems, IBatteryItem, UpgradeManagerNT, SlotUpgrade/ItemMachineUpgrade. Keep the HBM energy system and TileAccessCache/Nodespace/conductor subscription semantics.
3. Port both registered off/on blocks, tile identity `tileentity_electric_furnace`, states/facing/models/textures and inventory-preserving active-state transition. No separate temporary test block presented as the machine.
4. Port ContainerElectricFurnace, SlotSmelting, GUI and GUIHandler together. Preserve output XP and smelting event. The old AchievementList dependency needs an explicit advancement/stat migration decision, not silent deletion.
5. Close networkPackNT → BufPacket → PacketDispatcher/PacketThreading → client deserialize, including scheduling and ByteBuf ownership, disappeared tile/chunk behavior and server-safe class loading.
6. Only after the full production build passes, run real client and dedicated server acceptance. Native isolated algorithms/codecs may be tested earlier but are not a gameplay PASS.

## Acceptance checklist

- [ ] Ordinary recipe: 100 processing ticks at 50 energy/tick, capacity100000; invalid input, incompatible/full output, partial insertion and shift-click without loss/duplication.
- [ ] Battery charge precedes processing; network connection refresh every40ticks; insufficient power starts20tick cooldown; interrupted processing resets progress.
- [ ] Original speed/power upgrade tier1–3 formulas; original soot/pollution20tick condition.
- [ ] Sided insertion/extraction and hopper behavior; empty battery removal; upgrade slot; output pickup XP/event.
- [ ] Four facings; off/on changes preserve tile inventory/name/power/progress; breaking drops contents once and the off block item.
- [ ] NBT keys items/slot/name/power/progress/muffled; optional missing tags; real world save/reload and chunk unload/reload. Original cooldown is not persisted; do not add persistence as an accidental improvement.
- [ ] Real payload is muffled boolean, power long, maxProgress int, progress int, in that order. It is not the window-property callback path, and must not truncate to16bits.
- [ ] Two clients; GUI reopen/reconnect; unchanged-state chunk reload; destroy/unload before queued packet application; client cannot mutate authoritative inventory.
- [ ] Resource reload; facing textures/light/particles; dedicated server starts without loading client-only classes.

## Source locations

Compare matching paths in src/main/java and ../ntm-original-pristine/src/main/java:
- com/hbm/blocks/machine/MachineElectricFurnace.java
- com/hbm/tileentity/machine/TileEntityMachineElectricFurnace.java
- com/hbm/tileentity/TileEntityMachineBase.java and TileEntityLoadedBase.java
- com/hbm/inventory/container/ContainerElectricFurnace.java
- com/hbm/inventory/SlotSmelting.java
- com/hbm/inventory/gui/GUIMachineElectricFurnace.java
- com/hbm/packet/toclient/BufPacket.java
- api/hbm/energymk2/IEnergyReceiverMK2.java

No acceptance boxes have been checked by the planning audit.

## Infrastructure progress at checkpoint13 (not live acceptance)

Complete separate InventoryBase native boundary tested (62 checks), but furnace still inherits the larger MachineBase, whose125direct subclasses need their own caller/override audit. Battery API and extracted unchanged transfer operations verified (8046 checks), including source debit/receiver credit and native ItemStack NBT byte persistence; no complete concrete-battery/furnace or world-save claim. Tile cache native nonloading lookup tested (31 checks) against actual client/server providers, but network topology/subscription and queued synchronization remain unverified. No acceptance checkbox above is changed.

Additional required energy gates:
- [ ] Two-direction debit/receipt, capacity/rate/zero boundaries with real registered batteries and machine, including original negative/signed-overflow behavior characterized without unrequested balance fixes.
- [ ] Existing API simulation semantics only where actually present; IBatteryItem/Library transfer has no simulation flag.
- [ ] Real saved power/charge and reconnect/unload/reload topology on dedicated server; isolated ItemStack compressed-NBT tests do not establish this.
- [ ] Radioactive inventory item, player protection and container shielding integration separately passes the original-source radiation contract; machine Slot/NBT tests are not irradiation tests.
