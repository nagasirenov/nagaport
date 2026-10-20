# Machine player-callback assessment

Read-only source/native-binary audit, 2026-10-19. This document is the only repository edit made by this audit. No production edits, Gradle runs, compilation or gameplay tests. Naming migration was concurrently active, so line numbers below are snapshot locators, not immutable inputs. Storage, EMPTY/null, recipes, energy, NBT, fields, and unrelated coordinate APIs remain frozen.

## Required bounded closure

MachineBase plus 182 named descendants were inspected. Change root isUseableByPlayer to isUsableByPlayer; openInventory/closeInventory gain EntityPlayer. Do not introduce a compatibility bridge or leave descendant overrides shadowed by native root defaults.

Six usability overrides: TileEntityFusionTorus, TileEntityICF, TileEntityLaunchPadRocket, TileEntityMachineRadarNT, TileEntityMachineRocketAssembly, TileEntityMachineStardar.

Four open/close pairs: TileEntityAshpit, TileEntityFireboxBase, TileEntityMachineCombustionEngine, TileEntityTurretBaseNT. There are FIVE paired container callers: ContainerAshpit, ContainerFirebox, ContainerCombustionEngine, ContainerTurretBase, and ContainerMachineWoodBurner (inherits the root no-op pair). Pass invPlayer.player from construction, actual close argument from onContainerClosed, and preserve existing superclass-close ordering.

There are 101 concrete MachineBase-typed container usability calls and 35 descendant self-calls; exact snapshot manifest below. Additionally migrate ContainerBase's IInventory-typed call. Many generic-container descendants inherit this call, including LaunchPadRocket, RocketAssembly, and Stardar; migrating only concrete containers misses these dispatch paths.

### Independent edges and scope decisions

- TileEntityProxyCombo: native declarations and all three delegate calls. Preserve inventory enable flag, instanceof ISidedInventory checks, missing/wrong-type core false/no-op, and player identity.
- TileEntityProxyInventory: native declarations, preserving deliberate false/no-op. Do not invent forwarding for these callbacks.
- ContainerBase also serves independent TileEntityCrateBase and TileEntityMassStorage. Include CrateBase usability declaration plus native coordinate adaptation and ContainerMassStorage's typed usability call, or explicitly leave that edge blocked. MassStorage inherits CrateBase usability; preserve its squared distance 64, not MachineBase's 128. This edge does not itself require migrating crate/mass-storage open-close sounds.
- BlockPWR's independent inventory wrapper has false/no-op callback declarations: a small coherent addition if claiming wrapper callback closure.
- ProxyCombo can delegate to independent inventories, so this scope does not establish native completion of every possible core. RBMKActiveBase/SlottedBase, legacy bomb/inventory/item/entity owners and their callers remain separate. No global text replacement.
- TileEntityInventoryBase already has native callbacks and a documented legacy spelling bridge for ContainerSoyuzCapsule. Do not copy or incidentally remove that independent bridge.

## Native API and behavior evidence

Examined installed .gradle/minecraft/forgeBin-1.12.2-14.23.5.2768-PROJECT(ntm-port-1.12.2).jar with the installed Java 8 javap (no Gradle).

IInventory declares boolean isUsableByPlayer(EntityPlayer), void openInventory(EntityPlayer), void closeInventory(EntityPlayer). InventoryPlayer.player is public EntityPlayer. World.getTileEntity takes BlockPos. TileEntity supplies world/pos and getWorld/getPos. Entity.getDistanceSq(BlockPos) delegates to BlockPos.distanceSq and is NOT the centered distance. Use world.getTileEntity(pos) and explicit pos.getX/Y/Z()+0.5D doubles to preserve legacy centers.

Preserve these exact usability policies:
- MachineBase: same tile identity and distance squared <=128.
- ICF: same identity and <=256.
- FusionTorus and LaunchPadRocket: same identity and <=1024.
- RadarNT and RocketAssembly: identity only, no distance limit.
- Stardar: unconditional true, without world lookup.
- Independent CrateBase: same identity and <=64.

Ashpit, FireboxBase and CombustionEngine increment/decrement playersUsing only on !world.isRemote. Preserve unmatched decrement behavior, no newly introduced clamping or player filtering. Root open/close remain no-ops.

## Turret sound parity: resolved historical binary evidence

IMPORTANT: this section supersedes the audit's initial hypothetical client-local branch suggestion. Do NOT add a client-local playback branch.

Original official Minecraft 1.7.10 binary is /tmp/nagaport-legacy-client.jar, SHA-1 e80d9b3bf5085002218d4be59e668bac718abbc6. Forge 1614 userdev conf/packaged.srg and conf/methods.csv establish:
- ahb = World; ahb.a(DDDStringFF) = func_72908_a = playSoundEffect.
- bma = RenderGlobal; bma.a(StringDDDFF) = func_72704_a = playSound.
- mp = WorldManager; mp.a(StringDDDFF) = same playSound callback.
- bjf = WorldClient; hc = S29PacketSoundEffect.

Original javap bytecode proves World.playSoundEffect iterates its IWorldAccess listeners and calls playSound(String,DDD,FF). RenderGlobal's corresponding implementation is an empty return. WorldClient does not override this no-boolean sound overload; its separate boolean overload is the local playback path, not the method used by the turret. Server WorldManager constructs S29PacketSoundEffect and broadcasts to all nearby players in the dimension, radius 16 at volume 1 (16*volume when volume>1), with no player exclusion. Relevant Forge 1614 World/WorldClient/RenderGlobal patches do not change this overload's behavior.

Native WorldClient.playSound(EntityPlayer,DDD,SoundEvent,SoundCategory,FF) only invokes local playback when supplied player == Minecraft.player. Thus null intentionally yields client no-op. Native server listener broadcasting uses the supplied excluded player, so null preserves inclusion of the opener. Correct turret adaptation on BOTH sides is world.playSound(null, centered x,y,z, registered event, SoundCategory.BLOCKS,1.0F,1.0F). Do not pass the callback player, and do not add local client playback.

Legacy sounds.json lines 61-62 identify both block.openC and block.closeC with category "block"; native category is BLOCKS. Preserve centered coordinates, volume/pitch, and all chosen sample variants.

### Required registry/resource closure

ModSoundEvents currently registers only SLICER. Add two real registered SoundEvents for canonical hbm:block.openc and hbm:block.closec, preserving the event identities after native normalization. Do not instantiate unregistered throwaway SoundEvents or use nullable registry lookup as a substitute for registration.

Native ResourceLocation bytecode lowercases namespace AND path. Native SoundHandler constructs sounds.json event keys using new ResourceLocation(domain,key); the existing mixed-case keys therefore match canonical registered IDs. No unrelated JSON rewrite is necessary. Underlying sample filenames still require these six byte-preserving case-only renames:

- sounds/block/openC1.ogg -> sounds/block/openc1.ogg
- sounds/block/openC2.ogg -> sounds/block/openc2.ogg
- sounds/block/openCSqueaky.ogg -> sounds/block/opencsqueaky.ogg
- sounds/block/closeC1.ogg -> sounds/block/closec1.ogg
- sounds/block/closeC2.ogg -> sounds/block/closec2.ogg
- sounds/block/closeC3.ogg -> sounds/block/closec3.ogg

All paths are beneath src/main/resources/assets/hbm. Preserve file bytes and selected sound variants. Use actual stored-entry names for Windows-portable checks, not Path.exists() to reject historical case; verify both native FolderResourcePack and FileResourcePack canonical lookup. No blanket sound-resource normalization is justified.

## Proposed extracted real-native tests

No tests below were executed by this audit.

Extract actual production callback bodies verbatim into fixtures with native IInventory/TileEntityInventoryBase, native World subclass with controlled tile resolution, and native EntityPlayer subclass. No fabricated net.minecraft API classes. The complete production MachineBase still has unrelated unported dependencies, so report extracted callback acceptance, not full-machine compilation.

- Invoke all seven machine usability policies through IInventory. Test exact threshold, just outside, absent/replaced tile, and nonzero/negative positions to expose corner-vs-center mistakes. Radar/RocketAssembly accept distant player only while identity matches; Stardar returns true without world.
- Test hasPermission reaches custom usability overrides rather than root defaults.
- Test native open/close dispatch for all four overrides; server two opens/two closes and unmatched close; client count unchanged.
- Capture turret native World sound calls: registered IDs, category BLOCKS, exact center, 1F volume/pitch, null excluded player. Do not mistake call capture for client audio playback; binary evidence establishes actual client null no-op.
- Test ProxyCombo enabled/disabled, missing/wrong-type core, exact player forwarding, return value and call counts. ProxyInventory and BlockPWR remain false/no-op.
- Source-level checks for constructor/close player argument, superclass ordering, 101 typed container calls, 35 self-calls, all six overrides, all four pairs, and absence of old callback shapes within explicitly migrated closure.
- Verify registered events and six byte-identical OGG resources with native resource-pack readers and canonical paths. This is not audible playback, resource reload, or gameplay acceptance.
- Freeze unrelated source by independent original/migrated manifests and whole-file reversal checks, with explicitly enumerated position/sound adaptations. Do not bless unrelated changes merely because extraction compiles.

## Exact snapshot usability-call manifests

### 35 descendant self-calls

- `src/main/java/com/hbm/tileentity/bomb/TileEntityLaunchPadRocket.java`: `546: return this.isUseableByPlayer(player);`
- `src/main/java/com/hbm/tileentity/bomb/TileEntityLaunchPadRusted.java`: `222: return this.isUseableByPlayer(player);`
- `src/main/java/com/hbm/tileentity/machine/TileEntityElectrolyser.java`: `541: return this.isUseableByPlayer(player);`
- `src/main/java/com/hbm/tileentity/machine/TileEntityMachineAmmoPress.java`: `199: return this.isUseableByPlayer(player);`
- `src/main/java/com/hbm/tileentity/machine/TileEntityMachineAnnihilator.java`: `241: return this.isUseableByPlayer(player);`
- `src/main/java/com/hbm/tileentity/machine/TileEntityMachineArcFurnaceLarge.java`: `693: return this.isUseableByPlayer(player);`
- `src/main/java/com/hbm/tileentity/machine/TileEntityMachineAssemblyFactory.java`: `371: @Override public boolean hasPermission(EntityPlayer player) { return this.isUseableByPlayer(player); }`
- `src/main/java/com/hbm/tileentity/machine/TileEntityMachineAssemblyMachine.java`: `300: @Override public boolean hasPermission(EntityPlayer player) { return this.isUseableByPlayer(player); }`
- `src/main/java/com/hbm/tileentity/machine/TileEntityMachineChemicalFactory.java`: `397: @Override public boolean hasPermission(EntityPlayer player) { return this.isUseableByPlayer(player); }`
- `src/main/java/com/hbm/tileentity/machine/TileEntityMachineChemicalPlant.java`: `275: @Override public boolean hasPermission(EntityPlayer player) { return this.isUseableByPlayer(player); }`
- `src/main/java/com/hbm/tileentity/machine/TileEntityMachineCompressorBase.java`: `223: return this.isUseableByPlayer(player);`
- `src/main/java/com/hbm/tileentity/machine/TileEntityMachineDriveProcessor.java`: `141: return isUseableByPlayer(player);`
- `src/main/java/com/hbm/tileentity/machine/TileEntityMachineExcavator.java`: `806: return this.isUseableByPlayer(player);`
- `src/main/java/com/hbm/tileentity/machine/TileEntityMachineFunnel.java`: `173: return this.isUseableByPlayer(player);`
- `src/main/java/com/hbm/tileentity/machine/TileEntityMachinePUREX.java`: `233: @Override public boolean hasPermission(EntityPlayer player) { return this.isUseableByPlayer(player); }`
- `src/main/java/com/hbm/tileentity/machine/TileEntityMachinePrecAss.java`: `350: @Override public boolean hasPermission(EntityPlayer player) { return this.isUseableByPlayer(player); }`
- `src/main/java/com/hbm/tileentity/machine/TileEntityMachineRadarNT.java`: `426: return this.isUseableByPlayer(player);`
- `src/main/java/com/hbm/tileentity/machine/TileEntityMachineRocketAssembly.java`: `309: return isUseableByPlayer(player);`
- `src/main/java/com/hbm/tileentity/machine/TileEntityMachineSolderingStation.java`: `404: return this.isUseableByPlayer(player);`
- `src/main/java/com/hbm/tileentity/machine/TileEntityMachineStardar.java`: `339: return isUseableByPlayer(player);`
- `src/main/java/com/hbm/tileentity/machine/TileEntityMachineWarController.java`: `111: return isUseableByPlayer(player);`
- `src/main/java/com/hbm/tileentity/machine/TileEntityMachineWoodBurner.java`: `229: return this.isUseableByPlayer(player);`
- `src/main/java/com/hbm/tileentity/machine/TileEntityOrbitalStationLauncher.java`: `343: return isUseableByPlayer(player);`
- `src/main/java/com/hbm/tileentity/machine/TileEntityPWRController.java`: `565: return this.isUseableByPlayer(player);`
- `src/main/java/com/hbm/tileentity/machine/TileEntityTransporterBase.java`: `508: return this.isUseableByPlayer(player);`
- `src/main/java/com/hbm/tileentity/machine/TileEntityWatz.java`: `403: return this.isUseableByPlayer(player);`
- `src/main/java/com/hbm/tileentity/machine/albion/TileEntityPADipole.java`: `226: return this.isUseableByPlayer(player);`
- `src/main/java/com/hbm/tileentity/machine/albion/TileEntityPASource.java`: `239: return this.isUseableByPlayer(player);`
- `src/main/java/com/hbm/tileentity/machine/fusion/TileEntityFusionTorus.java`: `458: return this.isUseableByPlayer(player);`
- `src/main/java/com/hbm/tileentity/machine/rbmk/TileEntityRBMKAutoloader.java`: `260: return this.isUseableByPlayer(player);`
- `src/main/java/com/hbm/tileentity/machine/storage/TileEntityBatteryBase.java`: `176: @Override public boolean hasPermission(EntityPlayer player) { return this.isUseableByPlayer(player); }`
- `src/main/java/com/hbm/tileentity/network/TileEntityDroneCrate.java`: `265: return this.isUseableByPlayer(player);`
- `src/main/java/com/hbm/tileentity/network/TileEntityPneumoTube.java`: `296: @Override public boolean hasPermission(EntityPlayer player) { return this.isUseableByPlayer(player); }`
- `src/main/java/com/hbm/tileentity/network/TileEntityRadioTorchCounter.java`: `121: return this.isUseableByPlayer(player);`
- `src/main/java/com/hbm/tileentity/turret/TileEntityTurretBaseNT.java`: `73: return this.isUseableByPlayer(player);`

### 101 concrete container calls

- `src/main/java/com/hbm/inventory/container/ContainerAshpit.java`: `65: return ashpit.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerBarrel.java`: `81: return diFurnace.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerBatteryREDD.java`: `35: return socket.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerBatterySocket.java`: `34: return socket.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerCentrifuge.java`: `86: return diFurnace.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerCombustionEngine.java`: `85: return engine.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerCompressor.java`: `41: return compressor.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerCore.java`: `74: return nukeBoy.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerCoreEmitter.java`: `35: return nukeBoy.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerCoreInjector.java`: `74: return nukeBoy.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerCoreReceiver.java`: `35: return te.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerCoreStabilizer.java`: `69: return nukeBoy.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerCraneBoxer.java`: `70: return boxer.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerCraneInserter.java`: `66: return inserter.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerCraneUnboxer.java`: `71: return unboxer.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerCrucible.java`: `79: return crucible.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerCrystallizer.java`: `95: return diFurnace.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerDiFurnace.java`: `85: return diFurnace.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerDroneCrate.java`: `77: return crate.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerElectricFurnace.java`: `88: return diFurnace.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerElectrolyser.java`: `79: return electrolyser.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerElectrolyserFluid.java`: `99: return electrolyser.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerElectrolyserMetal.java`: `90: return electrolyser.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerFEL.java`: `70: return fel.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerFirebox.java`: `65: return firebox.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerFunnel.java`: `35: return funnel.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerFurnaceBrick.java`: `77: return furnace.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerFurnaceCombo.java`: `70: return furnace.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerFurnaceIron.java`: `73: return furnace.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerFurnaceSteel.java`: `71: return furnace.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerFusionBreeder.java`: `68: return breeder.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerFusionKlystron.java`: `66: return klystron.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerFusionTorus.java`: `38: return torus.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerHeaterHeatex.java`: `62: return heater.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerICF.java`: `76: return icf.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerICFPress.java`: `94: return press.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerIGenerator.java`: `83: return igen.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerITER.java`: `77: return iter.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerLaunchPadLarge.java`: `106: return launchpad.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerLaunchPadRusted.java`: `81: return launchpad.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerLiquefactor.java`: `40: return liquefactor.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineAmmoPress.java`: `82: return press.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineArcFurnaceLarge.java`: `86: return furnace.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineArcWelder.java`: `51: return welder.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineBattery.java`: `79: return diFurnace.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineCatalyticReformer.java`: `105: return reformer.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineCoker.java`: `71: return coker.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineCryoDistill.java`: `96: return cryo.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineCustom.java`: `61: return custom.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineCyclotron.java`: `112: return cyclotron.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineDiFurnaceRTG.java`: `45: return bFurnace.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineDiesel.java`: `78: return diFurnace.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineEPress.java`: `93: return nukeBoy.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineExcavator.java`: `90: return excavator.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineExposureChamber.java`: `84: return chamber.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineFluidTank.java`: `82: return diFurnace.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineGasCent.java`: `93: return gasCent.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineGasFlare.java`: `100: return testNuke.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineHydrotreater.java`: `106: return hydrotreater.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineLargeTurbine.java`: `92: return turbine.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineMilkReformer.java`: `92: return reformer.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineOilWell.java`: `87: return well.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachinePress.java`: `83: return press.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineRadGen.java`: `75: return diFurnace.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineRadarNT.java`: `74: return radar.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineReactorBreeding.java`: `72: return reactor.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineRefinery.java`: `98: return testNuke.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineRotaryFurnace.java`: `73: return furnace.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineSolderingStation.java`: `91: return solderer.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineTurbineGas.java`: `78: return turbinegas.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineTurbofan.java`: `86: return diFurnace.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineVacuumDistill.java`: `97: return distill.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineWarController.java`: `62: return shill.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMachineWoodBurner.java`: `95: return burner.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMicrowave.java`: `69: return microwave.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMiningLaser.java`: `75: return diFurnace.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerMixer.java`: `88: return mixer.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerNukeAntimatter.java`: `74: return SillyBomb.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerNukeFstbmb.java`: `71: return balefireBomb.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerOilburner.java`: `79: return heater.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerOreSlopper.java`: `99: return slopper.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerPADetector.java`: `43: return detector.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerPADipole.java`: `38: return quadrupole.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerPAQuadrupole.java`: `38: return quadrupole.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerPARFC.java`: `34: return rfc.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerPASource.java`: `44: return source.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerPWR.java`: `73: return controller.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerPlasmaHeater.java`: `66: return microwave.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerPyroOven.java`: `84: return pyro.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerRBMKAutoloader.java`: `52: return loader.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerRadiolysis.java`: `50: return radiolysis.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerReactorControl.java`: `70: return control.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerReactorResearch.java`: `80: return reactor.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerReactorZirnox.java`: `113: return zirnox.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerSILEX.java`: `97: return silex.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerSolidifier.java`: `42: return solidifier.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerSoyuzLauncher.java`: `95: return nukeBoy.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerStorageDrum.java`: `72: return drum.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerTurretBase.java`: `80: return turret.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerVacuumCircuit.java`: `86: return sucker.isUseableByPlayer(player);`
- `src/main/java/com/hbm/inventory/container/ContainerWatz.java`: `73: return watz.isUseableByPlayer(player);`
