# Machine EMPTY consumer closure

Read-only source review 2026-10-19; checkpoint15 baseline with concurrent callback edits visible. Only this report was written; no source edits or Gradle. Line numbers are current source, not stale baseline-manifest numbers. This is a receiver/dataflow audit, not a compile or gameplay acceptance claim.

## Actual implementation set and invariants

Keep public nullable ItemStack[] slots and all direct recipe/energy/filter array semantics. Native MachineBase getter/decr/removal must return EMPTY for absence; setters must translate null or EMPTY to null before storage and clamp only nonempty incoming stacks without cloning. Preserve live nonempty identity, full-removal identity and partial split behavior. NBT read must translate native empty loads to null; write must omit absent/empty entries. Implement native isEmpty/clear/field callbacks without changing recipes or inherited LoadedBase behavior. DiFurnaceRTG:146 is the only descendant setter which writes storage without super; normalize it too. The other 11 descendant setters delegate to super, with upgrade-item sound checks or no-op commented recipe logic; retain callbacks and guard empty before upgrade work if converting their stack APIs.

Do NOT claim a MachineBase-only setter fix closes generic transfers. Independent nullable inventories can be adjacent to PneumaticNetwork, Transporter, Dyson and cranes. Native calls must send EMPTY for clears, then each owned nullable storage ingress must normalize it. Never select null versus EMPTY by a runtime class whitelist. TileEntityInventoryBase already does this correctly.

### Mandatory getter/read consumers

- InventoryUtil:135-170 native IInventory insertion overload: guard nonempty source/current, recognize null OR EMPTY free slots. Preserve nullable remainder result and existing ownership/aliasing. Slot merge:520-563 has occupied/free branches requiring the same treatment. Leave ItemStack[] insertion/count/recipe overloads nullable. Avoid changing doesStackDataMatch globally just to solve these boundaries; it serves domain callers too.
- PneumaticNetwork:75-76,135-136,151-152,172 distinguish empty source, occupied target and vacant target using null+EMPTY checks; 163/184 clear generic sources with EMPTY. Preserve proportional totals/filter/access order and nullable work results.
- TileEntityTransporterBase:194-195,234-236,249: source and target IInventory reads need null+EMPTY checks. Its direct slots[j]/out are nullable domain values. decr results are discarded; do not invent success/retry behavior.
- TileEntityDysonLauncher:203-213: guard source nonempty and removal nonempty before assigning removed to nullable slots[0] or using removed count. Never store EMPTY in slots[0]. Retain capacity/accepted-item logic.
- CraneInserter:107/109 and127/129: generic merge/vacancy branches need null+EMPTY checks. TileEntityCraneExtractor:115/117 likewise; 128/134 discard decr results, nullable transfer remainder remains null-based.
- TileEntityMachineSatDock:243/264: independent legacy machine, but adjacent chest is ANY IInventory, including MachineBase. Change native target checks, not SatDock's own slots[] recipe/ejection state.
- IControlReceiverFilter:59; TileEntityCraneBase:126; TileEntityCraneRouter:158: skip null/EMPTY when writing filter entries, preserving original slot byte/order/count. Their load/set paths must normalize native NBT empties and never size-mutate EMPTY. IControlReceiverFilter:39-41 currently unconditionally sets loaded item count to one; that is a separate required guard when converting its NBT constructor.
- TileEntityRadioTorchCounter:51: normalize native getter EMPTY to null while building its temporary invSlots array, or use a null+EMPTY predicate at64. Do not change its real slots[] or matcher semantics.
- TileEntityMachineAutocrafter:102-114: ingredient read must be nonempty before decrement/container-item handling; a direct nullable slots[i] read is a narrow valid alternative. Do not translate its domain arrays to EMPTY. Zirnox:323 unqualified getter is under the existing loaded-rod precondition; no behavior edit required, keep identity.
- ProxyInventory:23-61, ProxyCombo:231-273, BlockPWR:229-267: native getter/decr/remove must normalize legacy delegated null to EMPTY and return EMPTY when unavailable. Rename delegated closing to native removal where callable; independent legacy delegates need their native method implemented, not a fake ISidedInventory member. Setter forwards native EMPTY after ingress closure. Preserve coordinates/conditional sided routing and missing-core behavior. Current PWRController extends MachineBase (the prior report's assertion that it is independent is incorrect).
- MagazineBelt:39/88/133 and MagazineSingleTypeBase:99/183 accept generic IInventory, not InventoryPlayer. Their outer slot guards must accept null+EMPTY before recipe matching; inner InventoryAmmoBag reads remain independent nullable state. Existing callers are predominantly weapon/player paths, but signatures do not constrain MachineBase.
- ItemStackUtil spillItems:215/217 already checks null AND !isEmpty; no additional emptiness edit there.

### Slot paths beyond the 99-file getter manifest

- ContainerBase merge:143-145,181-183 reads generic Slot stacks; add null+EMPTY presence/vacancy checks. This is a separate merge implementation from InventoryUtil and is not optional.
- ContainerNT: custom slotClick is a full old click implementation. At163 and176-243, distinguish slot absence from native EMPTY; normalize/guard decr results at202/240. Its player cursor/hotbar branches (64,112,135-145,177-200,226,243,258-284,288-323) are native inventories too: migrate their empty predicates/clears together rather than treating only machine slots. Result/transfer/slotClick native return contract must be EMPTY, but nullable local algorithm state may remain nullable if explicitly adapted at entry/exit. Native ClickType signature and renamed Slot onTake are separate API work, not supplied by null checks.
- ModulePatternMatcher initPatternSmart:34 and initPatternStandard:72 need null+EMPTY guards, OR normalize the seven exact Slot→domain calls: ContainerAutocrafter:78, CounterTorch:52, CraneExtractor:109, CraneGrabber:104, DroneRequester:89, MachineCustom:98, CraneRouter:56. Changing those two entry guards is the smaller coherent set, preserves modes[i]=null on cleared filters, and leaves domain arrays alone. Router.initPattern:99 forwards to it.
- SlotPattern.putStack:36-44 must not copy EMPTY then assign count one. Preserve nonempty copying and allowStackSize. Forward native EMPTY, with domain setter ingress taking responsibility for null storage.
- GUIMachineStardar:247/286 must treat EMPTY drive slot as empty for its prompt. GUIArmorTable stacks are unrelated InventoryBasic state, not MachineBase read closure.
- Every other scanned container Slot.getStack occurrence is guarded by getHasStack (or immediately follows a nonempty write), apart from the cases above. TrainCargoTram:135, TrainCargoTramTrailer:125, ModEventHandlerClient:1242, ItemRebarPlacer:255 and SlotNonRetarded:34 also guard reads with getHasStack. SlotCraftingOutput:31-35 and SlotSmelting:29-34 guard reads with getHasStack and pass through decr; no EMPTY consumer logic change there. Ordinary transfer functions still have native return/clear API debt: see clear manifest below.

### Drop paths

Apply nonempty guard before random offsets, serialization, split/mutation or item construction. Shared BlockDummyable and BlockMachineBase, configurable BlockDecoContainer, all MachineBase typed drop paths and unconstrained generic world inventory drops are listed as EDIT below. Do not infer inheritance from machine-like names: RTG/RtgFurnace, Siren, SatDock/SatLinker, Shredder, ForceField, Turbine, WasteDrum, KeyForge, MissileAssembly, crates, RBMK and foundry inventories are independent. Generic casts in their block drop methods can still safely accept both representations, but do not flip those inventories' getters as part of MachineBase.

## Exact 99-file getter candidate disposition

EDIT means native read consumes MachineBase or unrestricted IInventory, or is the shared mixed Slot path. INDEPENDENT means not a MachineBase getter consumer; it may still need setter ingress/native APIs separately. SAFE means already tolerant or guarded. All member getter occurrences in each file are represented by its current line list.

- `blocks/BlockDummyable.java:390` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/bomb/BombMulti.java:63` — INDEPENDENT typed bomb inventory drop.
- `blocks/bomb/CompactLauncher.java:141` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/bomb/LaunchTable.java:186` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/bomb/NukeBoy.java:59` — INDEPENDENT typed bomb inventory drop.
- `blocks/bomb/NukeCustom.java:64` — INDEPENDENT typed bomb inventory drop.
- `blocks/bomb/NukeFleija.java:58` — INDEPENDENT typed bomb inventory drop.
- `blocks/bomb/NukeGadget.java:61` — INDEPENDENT typed bomb inventory drop.
- `blocks/bomb/NukeMan.java:59` — INDEPENDENT typed bomb inventory drop.
- `blocks/bomb/NukeMike.java:58` — INDEPENDENT typed bomb inventory drop.
- `blocks/bomb/NukeN2.java:56` — INDEPENDENT typed bomb inventory drop.
- `blocks/bomb/NukePrototype.java:59` — INDEPENDENT typed bomb inventory drop.
- `blocks/bomb/NukeSolinium.java:56` — INDEPENDENT typed bomb inventory drop.
- `blocks/bomb/NukeTsar.java:58` — INDEPENDENT typed bomb inventory drop.
- `blocks/generic/BlockDecoContainer.java:83` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/generic/BlockStorageCrate.java:138,271` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/machine/BlockCustomMachine.java:175` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/machine/BlockFluidBarrel.java:119` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/machine/BlockMachineBase.java:64` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/machine/BlockMassStorage.java:142,227` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/machine/BlockPWR.java:234` — EDIT delegates MachineBase PWRController.
- `blocks/machine/CoreCore.java:69` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/machine/MachineAutocrafter.java:76` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/machine/MachineBattery.java:185` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/machine/MachineDiFurnace.java:140` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/machine/MachineDiFurnaceRTG.java:166` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/machine/MachineElectricFurnace.java:185` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/machine/MachineForceField.java:101` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/machine/MachineFunnel.java:89` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/machine/MachineICFPress.java:75` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/machine/MachineKeyForge.java:87` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/machine/MachineMissileAssembly.java:76` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/machine/MachineRTG.java:87` — INDEPENDENT typed legacy machine inventory drop.
- `blocks/machine/MachineReactorControl.java:123` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/machine/MachineRtgFurnace.java:185` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/machine/MachineSatDock.java:92` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/machine/MachineSatLinker.java:85` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/machine/MachineShredder.java:104` — INDEPENDENT typed legacy machine inventory drop.
- `blocks/machine/MachineSiren.java:85` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/machine/MachineTurbine.java:89` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/machine/PistonInserter.java:131` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/machine/SoyuzCapsule.java:70` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/machine/SoyuzLauncher.java:182` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/machine/WasteDrum.java:114` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/network/BlockCraneBase.java:277` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/network/CraneInserter.java:107,127` — EDIT generic IInventory insertion.
- `blocks/network/CranePartitioner.java:231` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/network/DroneCrate.java:82` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/network/DroneDock.java:93` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `blocks/turret/TurretSentry.java:67` — EDIT MachineBase or unconstrained generic inventory drop/serialization (keep null tolerance).
- `entity/train/EntityRailCarCargo.java:29` — INDEPENDENT railcar self inventory.
- `entity/train/EntityRailCarElectric.java:50` — INDEPENDENT railcar self inventory.
- `handler/EntityEffectHandler.java:470` — INDEPENDENT player inventory.
- `hazard/type/HazardTypeNeutron.java:50` — INDEPENDENT player inventory.
- `inventory/container/ContainerAnvil.java:34,35,153,154` — INDEPENDENT player or container-local InventoryBasic/crafting/result inventory.
- `inventory/container/ContainerArmorTable.java:71,178,198,206,207,214` — INDEPENDENT player or container-local InventoryBasic/crafting/result inventory.
- `inventory/container/ContainerCrateBase.java:38,39` — INDEPENDENT player or container-local InventoryBasic/crafting/result inventory.
- `inventory/container/ContainerLemegeton.java:40` — INDEPENDENT player or container-local InventoryBasic/crafting/result inventory.
- `inventory/container/ContainerNT.java:258` — EDIT Slot/cursor path; explicit getter itself is InventoryPlayer.
- `inventory/container/ContainerWeaponTable.java:30,55,56,57,58,59,60,61,64,90,97,98,99,100,101,102,103,106,193,209,211,212,213,214,215,216,217,218` — INDEPENDENT player or container-local InventoryBasic/crafting/result inventory.
- `inventory/gui/GUIMachineCompactLauncher.java:99` — INDEPENDENT bomb/MissileAssembly or weapon-table local inventory.
- `inventory/gui/GUIMachineLaunchTable.java:146` — INDEPENDENT bomb/MissileAssembly or weapon-table local inventory.
- `inventory/gui/GUIMachineMissileAssembly.java:83,84,86,87,89,90,92,93` — INDEPENDENT bomb/MissileAssembly or weapon-table local inventory.
- `inventory/gui/GUINukeBoy.java:56,58,60,62,64` — INDEPENDENT bomb/MissileAssembly or weapon-table local inventory.
- `inventory/gui/GUINukeMike.java:61,64,67,71` — INDEPENDENT bomb/MissileAssembly or weapon-table local inventory.
- `inventory/gui/GUINukeTsar.java:58,66` — INDEPENDENT bomb/MissileAssembly or weapon-table local inventory.
- `inventory/gui/GUIWeaponTable.java:59,86` — INDEPENDENT bomb/MissileAssembly or weapon-table local inventory.
- `inventory/recipes/MagicRecipes.java:29,30` — INDEPENDENT InventoryCrafting recipe matrix.
- `items/ItemInventory.java:53` — INDEPENDENT item-backed inventory.
- `items/block/ItemBlockStorageCrate.java:158` — INDEPENDENT item-backed inventory.
- `items/tool/ItemAmmoBag.java:62` — INDEPENDENT item-backed inventory.
- `items/tool/ItemBoltgun.java:54` — INDEPENDENT player inventory.
- `items/tool/ItemCasingBag.java:73,84` — INDEPENDENT item-backed inventory.
- `items/tool/ItemToolBox.java:103` — INDEPENDENT player inventory.
- `items/weapon/sedna/mags/MagazineBelt.java:39,54,88,97,133,143` — EDIT outer receiver is generic IInventory (not InventoryPlayer); inner bag receiver is independent InventoryAmmoBag.
- `items/weapon/sedna/mags/MagazineSingleTypeBase.java:99,139,183,200` — EDIT outer receiver is generic IInventory (not InventoryPlayer); inner bag receiver is independent InventoryAmmoBag.
- `main/ModEventHandler.java:512,1477` — INDEPENDENT player inventory.
- `packet/toserver/ItemBobmazonPacket.java:105,132` — INDEPENDENT player inventory.
- `render/tileentity/RenderFoundry.java:79,84` — INDEPENDENT FoundryCastingBase inventory.
- `render/tileentity/RenderMissileAssembly.java:50,51,53,54,56,57,59,60` — INDEPENDENT MissileAssembly inventory.
- `tileentity/IControlReceiverFilter.java:59,61` — EDIT generic filter serialization.
- `tileentity/TileEntityProxyCombo.java:237` — EDIT mixed native delegate.
- `tileentity/TileEntityProxyInventory.java:28` — EDIT mixed native delegate.
- `tileentity/machine/TileEntityDecon.java:65` — INDEPENDENT player inventory.
- `tileentity/machine/TileEntityDemonLamp.java:70` — INDEPENDENT player inventory.
- `tileentity/machine/TileEntityDysonLauncher.java:203` — EDIT adjacent IInventory extraction.
- `tileentity/machine/TileEntityMachineAutocrafter.java:102` — EDIT self MachineBase ingredient.
- `tileentity/machine/TileEntityMachineSatDock.java:243,244,251,264` — EDIT adjacent IInventory, own storage independent.
- `tileentity/machine/TileEntityReactorResearch.java:194` — INDEPENDENT player inventory.
- `tileentity/machine/TileEntityTransporterBase.java:194,234,249` — EDIT adjacent IInventory.
- `tileentity/machine/rbmk/TileEntityRBMKConsole.java:633,651` — INDEPENDENT RBMK fuel/irradiation inventories.
- `tileentity/network/TileEntityCraneBase.java:126,128` — EDIT filter serialization.
- `tileentity/network/TileEntityCraneExtractor.java:115` — EDIT adjacent IInventory.
- `tileentity/network/TileEntityCraneRouter.java:158,160` — EDIT filter serialization.
- `tileentity/network/TileEntityDroneCrate.java:141` — INDEPENDENT EntityDeliveryDrone getter; own slots direct nullable.
- `tileentity/network/TileEntityRadioTorchCounter.java:51` — EDIT adjacent IInventory snapshot.
- `uninos/networkproviders/PneumaticNetwork.java:75,135,151,172` — EDIT generic inventory transfer.
- `util/InventoryUtil.java:142,144,147,166` — EDIT generic inventory insertion and Slot merge.
- `util/ItemStackUtil.java:215` — SAFE spill already null+EMPTY tolerant.

## Removal result closure

All 63-file decr candidates reviewed by result use: domain recipe calls discard their results and retain direct-slot preconditions. The consumed MachineBase-capable results are Dyson and ContainerNT, plus three proxy forwarders. CraneExtractor/Transporter discard returned removals; no null result handling is needed there. PistonInserter's assignment at102 is InventoryPlayer→its own nullable slot, not MachineBase, but must normalize EMPTY when that native path is migrated. All other consumed closing results in Anvil/ArmorTable/Book/Lemegeton/WeaponTable are container-local crafting/InventoryBasic inventories, not MachineBase. They need native removeStackFromSlot API conversion independently; do not introduce a broad legacy closing method on IInventory.

## Setter ingress-first exact declaration manifest

This stage can land without changing independent getters. For each DIRECT entry normalize incoming null/EMPTY to null BEFORE count clamp and storage assignment; retain markDirty/block update callbacks. DELEGATE entries need forwarding compatible with native EMPTY and the target normalized ingress, not conversion back to null. SUPER entries inherit normalization; existing nonempty upgrade-item checks should stay behaviorally unchanged. This covers all named and nested setInventorySlotContents declarations found in the owned source, including portable inventories and entities reachable through generic Slot containers; it does not authorize getter migrations for them.

- `blocks/machine/BlockPWR.java:262` — DELEGATE.
- `blocks/machine/PistonInserter.java:374` — DIRECT.
- `entity/cart/EntityMinecartContainerBase.java:70` — DIRECT.
- `entity/item/EntityDeliveryDrone.java:175` — DIRECT.
- `entity/train/EntityRailCarCargo.java:75` — DIRECT.
- `items/ItemInventory.java:105` — DIRECT.
- `items/tool/ItemAmmoBag.java:119` — DIRECT.
- `items/tool/ItemCasingBag.java:150` — DIRECT.
- `items/tool/ItemPlasticBag.java:93` — DIRECT.
- `tileentity/TileEntityInventoryBase.java:56` — SAFE already normalized.
- `tileentity/TileEntityMachineBase.java:61` — DIRECT.
- `tileentity/TileEntityProxyCombo.java:267` — DELEGATE.
- `tileentity/TileEntityProxyInventory.java:56` — DELEGATE.
- `tileentity/bomb/TileEntityBombMulti.java:77` — DIRECT.
- `tileentity/bomb/TileEntityCompactLauncher.java:93` — DIRECT.
- `tileentity/bomb/TileEntityLaunchTable.java:102` — DIRECT.
- `tileentity/bomb/TileEntityNukeBoy.java:75` — DIRECT.
- `tileentity/bomb/TileEntityNukeCustom.java:81` — DIRECT.
- `tileentity/bomb/TileEntityNukeFleija.java:75` — DIRECT.
- `tileentity/bomb/TileEntityNukeGadget.java:75` — DIRECT.
- `tileentity/bomb/TileEntityNukeMan.java:75` — DIRECT.
- `tileentity/bomb/TileEntityNukeMike.java:75` — DIRECT.
- `tileentity/bomb/TileEntityNukeN2.java:75` — DIRECT.
- `tileentity/bomb/TileEntityNukePrototype.java:76` — DIRECT.
- `tileentity/bomb/TileEntityNukeSolinium.java:75` — DIRECT.
- `tileentity/bomb/TileEntityNukeTsar.java:75` — DIRECT.
- `tileentity/machine/TileEntityDiFurnaceRTG.java:146` — DIRECT.
- `tileentity/machine/TileEntityForceField.java:126` — DIRECT.
- `tileentity/machine/TileEntityFoundryCastingBase.java:123` — DIRECT.
- `tileentity/machine/TileEntityMachineAmmoPress.java:90` — SUPER.
- `tileentity/machine/TileEntityMachineArcFurnaceLarge.java:96` — SUPER.
- `tileentity/machine/TileEntityMachineArcWelder.java:67` — SUPER.
- `tileentity/machine/TileEntityMachineCrystallizer.java:346` — SUPER.
- `tileentity/machine/TileEntityMachineCyclotron.java:313` — SUPER.
- `tileentity/machine/TileEntityMachineKeyForge.java:56` — DIRECT.
- `tileentity/machine/TileEntityMachineMiningLaser.java:601` — SUPER.
- `tileentity/machine/TileEntityMachineMissileAssembly.java:63` — DIRECT.
- `tileentity/machine/TileEntityMachineRTG.java:66` — DIRECT.
- `tileentity/machine/TileEntityMachineSatDock.java:54` — DIRECT.
- `tileentity/machine/TileEntityMachineSatLinker.java:57` — DIRECT.
- `tileentity/machine/TileEntityMachineShredder.java:71` — DIRECT.
- `tileentity/machine/TileEntityMachineSiren.java:65` — DIRECT.
- `tileentity/machine/TileEntityMachineSolderingStation.java:71` — SUPER.
- `tileentity/machine/TileEntityMachineTurbine.java:119` — DIRECT.
- `tileentity/machine/TileEntityMachineVacuumCircuit.java:68` — SUPER.
- `tileentity/machine/TileEntityRtgFurnace.java:62` — DIRECT.
- `tileentity/machine/TileEntityWasteDrum.java:58` — DIRECT.
- `tileentity/machine/oil/TileEntityMachinePyroOven.java:69` — SUPER.
- `tileentity/machine/oil/TileEntityOilDrillBase.java:183` — SUPER.
- `tileentity/machine/rbmk/TileEntityRBMKSlottedBase.java:51` — DIRECT.
- `tileentity/machine/storage/TileEntityCrateBase.java:51` — DIRECT.
- `tileentity/network/TileEntityCraneExtractor.java:48` — SUPER.
- `tileentity/network/TileEntityRequestNetworkContainer.java:65` — DIRECT.

Slot ingress additionally: SlotPattern:36 must reject empty before copy/count; ContainerArmorTable custom putStack:46/202 needs empty presence checks if that unrelated container is native-migrated; ContainerWeaponTable:34/197 similarly has upgrade unpack/install logic; ContainerAnvil:141 is a pure super forwarder. No global Slot shim.

## Native clear call manifest

Each line below contains an explicit null passed to Slot.putStack or IInventory.setInventorySlotContents. Slot is a native boundary even when backed by nullable owned storage: use EMPTY and make owned ingresses accept it. Plain direct slots[i]=null is intentionally NOT included. A whole-project native-clear cleanup is larger than the minimal MachineBase getter closure: container-local armor/weapon/anvil and item/player callers are separate native debt, not evidence for flipping their storage/getters. The lists let the implementation choose a coherent ingress-first batch without silently corrupting any nullable target.

- `entity/train/TrainCargoTram.java:154`
- `entity/train/TrainCargoTramTrailer.java:136`
- `inventory/container/ContainerAmmoBag.java:56`
- `inventory/container/ContainerAnvil.java:103,157,169`
- `inventory/container/ContainerArmorTable.java:75,159`
- `inventory/container/ContainerAshpit.java:54`
- `inventory/container/ContainerAutocrafter.java:108`
- `inventory/container/ContainerBarrel.java:68`
- `inventory/container/ContainerBase.java:53`
- `inventory/container/ContainerBatteryREDD.java:54`
- `inventory/container/ContainerBatterySocket.java:53`
- `inventory/container/ContainerBombMulti.java:62`
- `inventory/container/ContainerBook.java:90`
- `inventory/container/ContainerCasingBag.java:56`
- `inventory/container/ContainerCentrifuge.java:75`
- `inventory/container/ContainerCombustionEngine.java:74`
- `inventory/container/ContainerCompactLauncher.java:64`
- `inventory/container/ContainerCompressor.java:75`
- `inventory/container/ContainerCore.java:61`
- `inventory/container/ContainerCoreInjector.java:61`
- `inventory/container/ContainerCoreStabilizer.java:56`
- `inventory/container/ContainerCraneBoxer.java:57`
- `inventory/container/ContainerCraneExtractor.java:72`
- `inventory/container/ContainerCraneGrabber.java:67`
- `inventory/container/ContainerCraneInserter.java:53`
- `inventory/container/ContainerCraneUnboxer.java:58`
- `inventory/container/ContainerCrucible.java:68`
- `inventory/container/ContainerCrystallizer.java:84`
- `inventory/container/ContainerDiFurnace.java:74`
- `inventory/container/ContainerDroneCrate.java:64`
- `inventory/container/ContainerDroneRequester.java:51`
- `inventory/container/ContainerElectricFurnace.java:77`
- `inventory/container/ContainerElectrolyserFluid.java:88`
- `inventory/container/ContainerElectrolyserMetal.java:79`
- `inventory/container/ContainerFEL.java:59`
- `inventory/container/ContainerFileCabinet.java:57`
- `inventory/container/ContainerFirebox.java:54`
- `inventory/container/ContainerForceField.java:70`
- `inventory/container/ContainerFunnel.java:56`
- `inventory/container/ContainerFurnaceBrick.java:66`
- `inventory/container/ContainerFurnaceCombo.java:59`
- `inventory/container/ContainerFurnaceIron.java:62`
- `inventory/container/ContainerFurnaceSteel.java:60`
- `inventory/container/ContainerFusionBreeder.java:57`
- `inventory/container/ContainerFusionKlystron.java:55`
- `inventory/container/ContainerFusionTorus.java:64`
- `inventory/container/ContainerHeaterHeatex.java:51`
- `inventory/container/ContainerICF.java:65`
- `inventory/container/ContainerICFPress.java:83`
- `inventory/container/ContainerIGenerator.java:72`
- `inventory/container/ContainerITER.java:65`
- `inventory/container/ContainerLaunchPadLarge.java:95`
- `inventory/container/ContainerLaunchPadRusted.java:70`
- `inventory/container/ContainerLaunchTable.java:64`
- `inventory/container/ContainerLeadBox.java:56`
- `inventory/container/ContainerLemegeton.java:72`
- `inventory/container/ContainerLiquefactor.java:61`
- `inventory/container/ContainerMachineAmmoPress.java:71`
- `inventory/container/ContainerMachineAnnihilator.java:59`
- `inventory/container/ContainerMachineArcFurnaceLarge.java:75`
- `inventory/container/ContainerMachineArcWelder.java:81`
- `inventory/container/ContainerMachineAssemblyFactory.java:76`
- `inventory/container/ContainerMachineAssemblyMachine.java:63`
- `inventory/container/ContainerMachineBattery.java:66`
- `inventory/container/ContainerMachineCatalyticReformer.java:94`
- `inventory/container/ContainerMachineChemicalFactory.java:76`
- `inventory/container/ContainerMachineChemicalPlant.java:70`
- `inventory/container/ContainerMachineCoker.java:60`
- `inventory/container/ContainerMachineCryoDistill.java:85`
- `inventory/container/ContainerMachineCyclotron.java:101`
- `inventory/container/ContainerMachineDiFurnaceRTG.java:89`
- `inventory/container/ContainerMachineDiesel.java:65`
- `inventory/container/ContainerMachineEPress.java:82`
- `inventory/container/ContainerMachineExcavator.java:79`
- `inventory/container/ContainerMachineExposureChamber.java:73`
- `inventory/container/ContainerMachineFluidTank.java:69`
- `inventory/container/ContainerMachineGasCent.java:82`
- `inventory/container/ContainerMachineGasFlare.java:89`
- `inventory/container/ContainerMachineHydrotreater.java:95`
- `inventory/container/ContainerMachineKeyForge.java:67`
- `inventory/container/ContainerMachineLargeTurbine.java:79`
- `inventory/container/ContainerMachineMilkReformer.java:81`
- `inventory/container/ContainerMachineMissileAssembly.java:63`
- `inventory/container/ContainerMachineOilWell.java:76`
- `inventory/container/ContainerMachinePUREX.java:64`
- `inventory/container/ContainerMachinePrecAss.java:63`
- `inventory/container/ContainerMachinePress.java:72`
- `inventory/container/ContainerMachineRTG.java:82`
- `inventory/container/ContainerMachineRadGen.java:64`
- `inventory/container/ContainerMachineRadarNT.java:63`
- `inventory/container/ContainerMachineReactorBreeding.java:60`
- `inventory/container/ContainerMachineRefinery.java:87`
- `inventory/container/ContainerMachineRocketAssembly.java:72`
- `inventory/container/ContainerMachineRotaryFurnace.java:62`
- `inventory/container/ContainerMachineSatLinker.java:67`
- `inventory/container/ContainerMachineShredder.java:98`
- `inventory/container/ContainerMachineSiren.java:64`
- `inventory/container/ContainerMachineSolderingStation.java:80`
- `inventory/container/ContainerMachineStrandCaster.java:63`
- `inventory/container/ContainerMachineTurbine.java:79`
- `inventory/container/ContainerMachineTurbineGas.java:67`
- `inventory/container/ContainerMachineTurbofan.java:75`
- `inventory/container/ContainerMachineVacuumDistill.java:86`
- `inventory/container/ContainerMachineWarController.java:51`
- `inventory/container/ContainerMachineWoodBurner.java:84`
- `inventory/container/ContainerMassStorage.java:55`
- `inventory/container/ContainerMicrowave.java:58`
- `inventory/container/ContainerMiningLaser.java:62`
- `inventory/container/ContainerMixer.java:77`
- `inventory/container/ContainerNT.java:206,243,275,284,323`
- `inventory/container/ContainerNukeAntimatter.java:61`
- `inventory/container/ContainerNukeBoy.java:61`
- `inventory/container/ContainerNukeCustom.java:84`
- `inventory/container/ContainerNukeFleija.java:67`
- `inventory/container/ContainerNukeFstbmb.java:58`
- `inventory/container/ContainerNukeGadget.java:62`
- `inventory/container/ContainerNukeMan.java:62`
- `inventory/container/ContainerNukeMike.java:57`
- `inventory/container/ContainerNukeN2.java:67`
- `inventory/container/ContainerNukePrototype.java:70`
- `inventory/container/ContainerNukeSolinium.java:65`
- `inventory/container/ContainerNukeTsar.java:55`
- `inventory/container/ContainerOilburner.java:68`
- `inventory/container/ContainerOrbitalStationLauncher.java:78`
- `inventory/container/ContainerOreSlopper.java:88`
- `inventory/container/ContainerPADetector.java:69`
- `inventory/container/ContainerPADipole.java:64`
- `inventory/container/ContainerPAQuadrupole.java:64`
- `inventory/container/ContainerPARFC.java:55`
- `inventory/container/ContainerPASource.java:70`
- `inventory/container/ContainerPWR.java:62`
- `inventory/container/ContainerPlasmaHeater.java:55`
- `inventory/container/ContainerPlasticBag.java:51`
- `inventory/container/ContainerPyroOven.java:73`
- `inventory/container/ContainerRBMKAutoloader.java:41`
- `inventory/container/ContainerRBMKHeater.java:49`
- `inventory/container/ContainerRBMKOutgasser.java:51`
- `inventory/container/ContainerRBMKRod.java:70`
- `inventory/container/ContainerRBMKStorage.java:53`
- `inventory/container/ContainerRadiolysis.java:72`
- `inventory/container/ContainerReactorControl.java:57`
- `inventory/container/ContainerReactorResearch.java:69`
- `inventory/container/ContainerReactorZirnox.java:102`
- `inventory/container/ContainerRtgFurnace.java:71`
- `inventory/container/ContainerSILEX.java:84`
- `inventory/container/ContainerSatDock.java:78`
- `inventory/container/ContainerSolidifier.java:63`
- `inventory/container/ContainerSoyuzCapsule.java:66`
- `inventory/container/ContainerSoyuzLauncher.java:82`
- `inventory/container/ContainerStorageDrum.java:59`
- `inventory/container/ContainerToolBox.java:56`
- `inventory/container/ContainerTransporterRocket.java:55`
- `inventory/container/ContainerTurretBase.java:67`
- `inventory/container/ContainerVacuumCircuit.java:75`
- `inventory/container/ContainerWasteDrum.java:64`
- `inventory/container/ContainerWatz.java:60`
- `inventory/container/ContainerWeaponTable.java:65,107,176`
- `items/tool/ItemToolBox.java:109`
- `main/ModEventHandler.java:530`
- `tileentity/machine/TileEntityMachineAutocrafter.java:283`
- `tileentity/network/TileEntityDroneCrate.java:145`
- `uninos/networkproviders/PneumaticNetwork.java:163,184`

## Acceptance traps

1. Empty machine insertion and shift-click must work in both merge implementations; compare MachineBase, vanilla chest, and owned legacy crate targets in the same test.
2. Source depletion cannot leave EMPTY in any nullable array or pass null to vanilla setter. Test native source + legacy destination and legacy source + native destination, plus missing proxy core.
3. Clearing a ghost filter must set its matcher mode to null and serialize no empty entry; don't let SlotPattern set count on EMPTY.
4. Full decrement returns the same live stack; partial decrement returns split stack; empty decrement/removal returns EMPTY; direct storage remains null. Do not mutate the shared EMPTY singleton.
5. Drop empty slots without consuming RNG; retain filter exclusion ranges and per-block drops. Existing zero-count non-null domain stacks need deliberate boundary treatment, not array-wide rewrites.
6. Getter-only tests miss client Slot checks, ContainerNT cursor paths, setter overrides and filter mode forwarding. Native API symbols/signatures, player array→NonNullList work and block/world APIs remain separate compile debt, not fixed by this audit.
