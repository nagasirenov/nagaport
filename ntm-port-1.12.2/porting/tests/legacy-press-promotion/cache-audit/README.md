# Press / EPress cached-metadata consumer audit

## Result

The bounded mod-source audit found no gameplay, renderer, ordinary NBT, or ordinary sync-packet difference caused *solely* by the server tile's old raw metadata cache 16..25 versus the native canonical value 0..9 after the proposed one-shot legacy promotion. This is not proof that the cache is universally unobservable. There is an explicit engine diagnostic counterexample: `TileEntity.addInfoToCrashReport` reports the cached integer, so raw 16 produces `16 / 0x10 / 0b10000` while canonical 0 produces `0 / 0x0 / 0b0000`. A claim covering crash diagnostics or arbitrary external callers cannot be accepted as equivalent without a separately approved contract decision. Do not write the private native engine cache to conceal that difference.

The generic source-reader closure is substantive, not a grep-absence claim. `PileNeutronHandler` can actually read a Press/EPress tile. `BlockDoorGeneric` and the klystron helper also have generic TileEntity receivers. Their exact uses are detailed below.

## Snapshot and limits

Read-only audit of `/workspace/shared/nagaport-metadata-cp90/ntm-port-1.12.2/src`, completed 2026-10-20. The requested comparison base was CP89 `42dea8e6`; the worktree HEAD observed during audit is `ff63e8d072f8282be83b1d7d94f20712dbe85884` (metadata lifecycle-counterexample documentation), with the parent's four Press/EPress edits and untracked `LegacyPressMetadataUpgrade.java` present. This audit made no repository edits, installations, downloads, or builds.

The claim is narrowly about observing a cache-only difference with the same Press/EPress object retained, same world/block state, same ordinary domain fields, and no additional behavioral divergence from the promotion operation. It does not establish the entire promotion's callback/lifecycle correctness, packet order correctness, equivalence of preexisting native-port defects, external-mod behavior, arbitrary reflection, crash-report equivalence, or arbitrary future code. The updated source is not claimed to be a whole-project compiling state.

For old nibble `m`, let `R=m+10` and `N=R & 15`. At m=0..5, R=N. The only unequal cases are m=6..15, with R=16..25 and N=0..9. These are the cases closed below.

## Enumeration and receiver closure

A comment/string-aware scan of every `src/**/*.java` found 518 active zero-argument `getBlockMetadata()` occurrences: 513 invocations and 5 declarations. Of the invocations, 291 use `this`, 8 are implicit-this calls, 191 are in renderer/model sources, and 23 are explicitly qualified non-render calls. The original unstripped candidate set is 520 occurrences; two comments account for the difference. Scan manifests are `all-zero-arg.tsv` and `active-zero-arg.tsv`. `scan.py` reproduces the inventory; `manifest.json` pins the traced source inputs.

No source method reference to this getter, quoted reflective getter name, `func_145832_p`, or `field_145847_g` was found. Seven direct `blockMetadata` field references belong only to FluidCounterValve, FluidValve, ObjTester, PlasmaHeater, and Pumpjack, not the Press inheritance chain. These counts are a finite index, not the argument for equivalence.

1. Press and EPress extend `TileEntityMachineBase -> TileEntityLoadedBase -> native TileEntity`. No mod class in that chain calls the getter or accesses the field. None of their implemented source interfaces adds such a read. The two concrete classes have no source subclasses. Their GUI/container classes also do not read the getter. Thus all 299 explicit/implicit-this source invocation sites belong to different runtime classes, including nested tile classes declared in block source files. They cannot execute with either concrete press as `this`.

2. Three of the 23 non-render qualified calls have receiver `IBlockSource`, not TileEntity: `DispenserBehaviorHandler:300,324`, `ItemModMinecart:180`.

3. Sixteen non-render qualified calls are narrowed by concrete declarations/casts or `instanceof` guards to non-press receiver classes:
   - `RailStandardSwitch:87`, `RailStandardSwitchFlipped:85`: TileEntityRailSwitch.
   - `MachineTurbineGas:83`: TileEntityMachineTurbineGas.
   - `MachineRotaryFurnace:81`: TileEntityMachineRotaryFurnace.
   - `MachineChungus:60`: TileEntityChungus.
   - `MachineDiesel:49`: guarded TileEntityMachineDiesel (local remains declared TileEntity).
   - `MachineIndustrialTurbine:54`: TileEntityTurbineBase.
   - `PistonInserter:86`: TileEntityPistonInserter.
   - `MachineCryoDistill:104`: TileEntityMachineCryoDistill.
   - `CraneSplitter:76`: TileEntityCraneSplitter.
   - `FluidPipeAnchor:95`: TileEntityPipeAnchor.
   - `RBMKConsole:71`: TileEntityRBMKConsole.
   - `RBMKBase:234`: TileEntityRBMKBase.
   - `TileEntityFEL:142`: TileEntitySILEX.
   - `GUIScreenRadioTorchController:85`, `GUIScreenRadioTorchReader:94`: their non-press radio-torch fields.
   A wrong runtime press at an explicit cast fails before its getter is reached; this cache difference does not change the cast.

4. The remaining four non-render qualified calls have generic TileEntity receivers and were traced:
   - `handler/neutron/PileNeutronHandler.java:81`: obtains an arbitrary world tile, so Press/EPress really is reachable. The integer is used only as `(meta & 8) == 0` at lines 95 and 105; all other branches depend on block identity, receiver interfaces, or flux. Since R and N differ only by bit 16, `(R & 8) == (N & 8)` for every unequal case. Further, ordinary presses are neither graphite rod/detector blocks nor IPileNeutronReceiver. The local integer differs, but no action/branch/output here does.
   - `blocks/generic/BlockDoorGeneric.java:184,188`: the core search accepts only this door's block identity (`BlockDummyable.findCoreRec`, identity test around line 229), so an ordinary retained Press/EPress does not supply this receiver. Even allowing a malformed press-at-door association, the first use is `ForgeDirection.getOrientation(value-10)` and the second is a switch on `value-10` with cases 2,3,4,5. R-10 is 6..15 and N-10 is -10..-1; both orientation values are UNKNOWN and both switches take the default path. `ForgeDirection.java:40-44` explicitly maps anything outside 0..5 to UNKNOWN. This use therefore also cannot distinguish the cache-only values through its result.
   - `tileentity/machine/fusion/TileEntityFusionKlystron.java:159`: `handleKNode(GenNode, TileEntity)` is generic, not concretely typed. Its only source callers are the normal klystron's `update` at line 106 and creative klystron's `update` at line 34, each passing its own `this`. Neither is a press. Even an external direct press argument would take the same UNKNOWN direction for R-10 versus N-10; all node coordinates and net operations in this method consequently stay equal for the cache-only pair.

5. Renderer/model sources: 191 invocation sites comprise 3 entity receivers (`ModelBlockSpider`, `RenderFallingNuke`, `RenderRubble`) and 188 tile-renderer reads. A generic TESR parameter alone is not an arbitrary runtime receiver: ClientProxy binds Press specifically to RenderPress and EPress specifically to RenderEPress (`ClientProxy.java:323-324`), and no other source binding or direct RenderEPress construction/call route exists. `RenderPress` has no metadata read. RenderEPress's three sites are closed next.

## RenderEPress: all three reads, side and arithmetic

`render/tileentity/RenderEPress.java:29,51,78` each switches on `tileentity.getBlockMetadata() - BlockDummyable.offset`, with `offset=10` and only cases 2,3,4,5. At R=16..25 and N=0..9 both take no case, so all three perform the same GL sequence. That argument is independent of client/server separation, client packet timing, and whether one artificially hands the renderer a server object. Subsequent head/item drawing uses press animation fields and syncStack, not metadata.

In ordinary source execution the raw cache difference is server-only: `BlockDummyable.destroyIfOrphan` returns immediately for `world.isRemote` (lines 171-173), and the only source call to `fixLegacyMonoblock` is inside that method (line 190). Press/EPress also schedule the legacy repair only in their server update branch. The registered renderer gets a separate client-world tile. `MainRegistry` uses a sided proxy, and only ClientProxy binds these TESRs. The server cache is not one of the serialized values below.

## Exact ordinary serialization / packet closure

`TileEntityMachineBase` does not override serialize/deserialize, so the inherited prefix is exactly `TileEntityLoadedBase.serialize`: one boolean `muffled` (line 58). Its decoder reads that same boolean. No metadata is included.

Each BufPacket body is exactly:

- Header (`BufPacket.java:39-44`): x:int, y:int, z:int.
- Press (`TileEntityMachinePress.java:203-209`): muffled:boolean; speed:int; burnTime:int; press:int; `BufferUtil.writeItemStack(slots[2])` (item ID/count/damage and item tag when applicable). This is item metadata, not tile metadata. The client decoder stores speed, burnTime, syncPress, syncStack and sets turnProgress=2.
- EPress (`TileEntityMachineEPress.java:190-198`): muffled:boolean; power:long; press:int; then either short(-1) or `BufferUtil.writeNBT(slots[2].getTagCompound())`. The decoder stores power, syncPress, a syncStack decoded from that NBT and turnProgress=2. The existing stack-NBT encoding choice is not changed or validated by this audit.

`TileEntityLoadedBase.networkPackNT:68-86` returns for a client world; creates BufPacket with the tile as its serializer; obtains the compiled buffer before suppression/dispatch. `ThreadedPacket.compile` calls only the packet's toBytes. PacketThreading prepares/reuses that compiled content and dispatches to the target point. `PacketDispatcher.java:67` registers BufPacket on Side.CLIENT. The handler reads the client world tile at x/y/z and invokes its deserialize (`BufPacket.java:32-36,50-65`). Neither handler, serializer nor decoder transfers or reads the metadata cache. A retained Java receiver reference in the server packet is not transmitted.

Persistent NBT also excludes the cache:
- Native TileEntity.writeInternal: registry id, x/y/z, optional ForgeData/ForgeCaps; no blockMetadata. The old 1.7.10 vanilla aor.b(dh) writes id,x,y,z and likewise no cache.
- LoadedBase: muffled.
- MachineBase: inventory items and custom name.
- Press: press, burnTime, speed, ret.
- EPress: press, power, ret.
- Neither press nor its mod superclasses overrides getUpdatePacket, getUpdateTag, onDataPacket or handleUpdateTag. Native getUpdatePacket returns null; getUpdateTag calls writeInternal directly. Thus vanilla tile-update/chunk-tag payloads do not expose this cache. The only in-repo tile AttachCapabilitiesEvent handler targets MassStorage, large ArcFurnace and ProxyCombo, not either press. Arbitrary external attached capabilities are outside this closure.

Native block-state network paths were checked in the installed Forge 1.12.2 bytecode:
- SPacketBlockChange(World,pos) takes World.getBlockState(pos) and serializes its registered state ID, not tile.getBlockMetadata.
- SPacketMultiBlockChange.BlockUpdateData(short,Chunk) takes Chunk.getBlockState(pos).
- SPacketChunkData.extractChunkData serializes each ExtendedBlockStorage's BlockStateContainer. Its separate tile tags come from getUpdateTag as described above.
- NetHandlerPlayClient.handleBlockChange/handleMultiBlockChange install those states in WorldClient; handleChunkData loads Chunk.read then hands NBT tags to each client tile's handleUpdateTag.
Thus ordinary native synchronization cannot carry the server's preexisting raw metadata cache into the client's renderer. This audit verified the installed native packet paths, not an exhaustive decompilation of every old 1.7 networking class.

Containers transmit inventory state through ordinary slots and use no custom metadata/progress fields; MachineBase.getField/getFieldCount return 0. EPress's Energy Control provider reports its press progress only (`provideExtraInfo:343-345`).

## Concrete diagnostic counterexample and acceptance boundary

The installed native TileEntity.addInfoToCrashReport calls getBlockMetadata and passes the result to CrashReportCategory.addBlockInfo(Block,int). Its inner detail formatter prints the entire integer in decimal, hex and binary, with no nibble mask. This is a real generic inherited consumer that can receive either press. Native World.updateEntities calls it on a ticking tile error, and can print the resulting report or throw it. The old 1.7.10 client jar's TileEntity (`aor`) has the corresponding path: a(CrashReportCategory) invokes p() (its cached getter) and k.a(...,Block,int), and the old formatter `m` likewise prints the full integer.

Example while the mismatch persists, before markDirty/updateContainingBlockInfo normalizes or clears it: old m=6 promotion leaves R=16 in the old tile and N=0 in storage; the native tile returns0. A crash category built for that retained tile contains different `Block data value` text. `Actual block data value` may still agree, since it is separately read from the world. This is not a claimed new spontaneous press crash; it establishes that cache equivalence is false for the inherited diagnostic API and for a tile error occurring in that interval.

Therefore:
- Defensible bounded statement: no difference in the enumerated mod-source gameplay/render/ordinary persistence/sync outputs caused solely by R versus N.
- Indefensible statement: the cache is unobservable, all defined public behavior is preserved, or full engine/external-mod trace equality is proven.
- If the corrective proof's required observations include crash reports/public getter outputs, this audit is a counterexample and the proof remains blocked, not an approval to alter private engine state.

## Read-only evidence artifacts

- `all-zero-arg.tsv`, `active-zero-arg.tsv`: complete source scan indexes.
- `bytecode.zip:press-native-tileentity-javap.txt`: native getter, NBT, update tag, crash report and inherited methods.
- `bytecode.zip:press-legacy-tileentity-javap.txt`: old cached getter, NBT and crash-report route.
- `bytecode.zip:press-CrashReportCategory-javap.txt`, `bytecode.zip:press-CrashReportCategory-2-javap.txt`: native diagnostic payload and formatter.
- `bytecode.zip:press-legacy-crashcategory-javap.txt`, `bytecode.zip:press-legacy-crashmetadata-javap.txt`: old diagnostic payload and formatter.
- `bytecode.zip:press-SPacketBlockChange-javap.txt`, `bytecode.zip:press-SPacketMultiBlockChange-javap.txt`, `bytecode.zip:press-SPacketMultiBlockChange$BlockUpdateData-javap.txt`, `bytecode.zip:press-SPacketChunkData-javap.txt`, `bytecode.zip:press-NetHandlerPlayClient-javap.txt`: verified native network closure.
- Existing parent artifact `native-world-crash-route.txt` supplies the native ticking-error call into addInfoToCrashReport.

All native bytecode was read with the existing Java8 javap and `/tmp/nagaport-ae2-native-classpath.txt`; legacy evidence came from the existing official minecraft-1.7.10-client.jar. This independent cache audit ran no execution tests or full build; the parent lifecycle suite separately executes the original/native fixtures.

## CP90 composition check

The corrective source is rebased onto CP90 `0be133c9fb9f07053e26f081371487e641c95214`. The two metadata reads in RenderCrucible and RenderStrandCaster shifted by two lines after added imports; their receiver types and read expressions did not change. MainRegistry changed only unrelated armor material registration/repair data, not sided proxy selection. Every active getter occurrence (ignoring its line number) agrees with the original audited inventory. The reproducible manifest uses the CP90-composed source hashes, including the four exact corrective insertions. This is source-audit evidence, not a complete compilation of those renderer/packet consumers.
