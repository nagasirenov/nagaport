# Native placement caller and owner closure

Baseline: CP93 `d2f171750be2f0d156a019f25fa781db39db308a`. This production patch is isolated from CP94 and does not claim a successful project build or a playable mod.

## Finite production rule

29 files, 36 exact edit ranges. Fifteen remaining NTM `canPlaceBlockAt(World,int,int,int)` owners become the actual native `(World,BlockPos)` override, unpacking XYZ at method entry. All 31 old four-argument calls become `(sameWorld,new BlockPos(sameX,sameY,sameZ))`, including ten `super` calls. Receiver, world and XYZ remain evaluated once, left to right, with the original expression grouping, integer arithmetic, virtual calls and short-circuit structure. There is no compatibility shim, fake native Block method or source exclusion.

The fifteen owners are Kelp, VinylVine, WaterPlant, DigammaFlame, Landmine, MeltedFlesh, DeadPlant, Fallout, ModDoor, Mush, RubberPlant, Stalagmite, TallPlant, TallPlantWater and OrbitalStation. The original archive and current source agree on two additional inherited descendants: NTM BlockFenceGate and BlockOrbitalStationLauncher. Seven already-native placement owners are unchanged: Coral, Chain, HangingVine, Layering, NTMFlower, Reeds and RubberCacti. Together these close all 22 current/original declaration owners and all 41 current call sites.

Only necessary queries inside these fifteen method bodies are adapted:

* Four old `World.doesBlockHaveSolidTopSurface` calls become the owning block's native `isSideSolid(capturedState,world,pos,UP)`. Forge 1614 implements the original helper as that direct block hook, not `World.isSideSolid`. Guarded `WorldBlockState.read` preserves the original explicit support-read domain. Stalactite's distinct world-side DOWN hook remains unchanged.
* Landmine preserves its support-first short circuit and a second world read when support is false, including mutations made by the support callback. Original `BlockFence.func_149825_a` recognizes exactly the oak and nether-brick fence identities. The new test is those same two identities, not `instanceof BlockFence` or all newly available wood fences.
* Fallout reuses its previously captured state; MeltedFlesh captures the state at the original block read. The state reaches native leaves/opacity hooks without a world reread. Fallout's leaves-and-not-air rule remains different from Flesh's leaves rule. Their original later metadata reread, `& 7 == 7` formula, and default-state material lookup stay unchanged.
* Door's height guard still executes before support, lower and upper checks. OrbitalStation's main-port identity still returns false without consulting the world. Kelp's exact two support identities and all seven owned `canBlockStay(World,int,int,int)` delegations remain intact.

## Native platform boundary, not blanket engine equivalence

Both original and native BlockVine inherit the base placement method; the actual Forge 1614 Vine patch adds no placement override. Native superclass placement is intentionally native platform behavior. Native World state access has the Y bound but not the old ±30,000,000 horizontal guard; explicit NTM support reads still use the old-domain helper. Original Forge base placement asks the world occupant's replaceability hook, and native base placement also uses the actual target occupant, not the prospective placing block. However, native default replaceability rereads the world state material, whereas original default replaceability uses its own material. Native BlockAir overrides that default and avoids the second read. Tests and bytecode evidence distinguish these cases rather than claiming identical engine read traces for every block/world.

Original support/leaves override owners already have native signatures. Exact current callback bodies (side solidity, leaves, opacity, replaceability and stay), block inheritance and placement bodies are guarded. The actual Beam leaves/air implementation is exercised. No unrelated capability body is renamed or replaced. Existing deliberately mismatched OilWell replaceability calls, the remaining inline-air family, hardness delegation, metadata state migration and custom stay internals remain outside this patch.

## Evidence and limits

* `check_source.py`: 29 byte-exact reverse/forward source proofs, 87 rejected mutations. `reverse(raw, entry)` accepts only the pinned after-file bytes and reconstructs the exact CP93 file. No newline normalization.
* `engine/scan.py`: strictly reproduces original 22-owner/17-selected-closure bytecode census, native placement/leaves census, original placement method dumps, both Vine inheritance checks, Forge support/base/default-replaceability patches, fence predicate and old/native world-read bytecode. Ordinary mode never records new baselines. `--record` is an explicit evidence-authoring mode only.
* `PlacementNativeTest.java`: **627 assertions**. Invokes the genuine original signed-client fence predicate across its actual block identities; executes genuine native Block/World/BlockVine methods, virtual replaceability, default reread versus BlockAir, all owner condition paths, two inherited descendants, short circuits, distinct support hooks, state identity under leaf callback mutation, deliberate support callback mutation, integer nibble tests, null receiver/argument throw ordering and native BlockPos wrapping.
* Six production classes compile in full against only the genuine native classpath: WorldBlockState, ForgeDirection, FacingCompat, BlockBeamBase, BlockHangingVine and PlantBlockState. Beam's abstract tile factory is supplied by an unexercised test subclass throwing on use.
* The fifteen changed owners are **exact method projections**, not complete-class compilation. The test bank is generated from the strict current source slices. Constructors/material selection, four ModBlocks identity slots and seven stay collaborators are explicitly named fixtures. Fallout/Flesh's exact current opacity methods are extracted too. Metadata-state test subclasses exercise every nibble and preserve the formula; they do not establish complete layer metadata-state migration. In particular VinylVine's existing `canBlockStay` still contains unmigrated legacy calls. This evidence does not claim otherwise.
* `PlacementAttribution.java` parses/analyzes all **4,071** genuine frozen CP93 source units, with no original NTM JAR fallback and no class generation. Before: seven native overrides and 31 legacy calls. After: 22 native overrides and all 41 calls resolve to `(World,BlockPos)`. The exact complete before/after source hashes and attribution rows are retained. The frozen result is 10,164 → 10,117 diagnostics (47 removed). This is a failed attribution snapshot, not successful compilation and not the later common build. The exact checked-in Inventory Tweaks ChestContainer annotation is scoped-compiled separately into a temporary API directory, with source and class-byte pins. This reproduces the real API input even after main build output cleanup; without it six unrelated missing-annotation diagnostics appear, while placement attribution is identical. No substitute API is authored.

All genuine dependencies, original archives, mappings and Java tools are byte-pinned. Only the two proven inert ForgeGradle dummy JARs use exact ZIP entry/payload pins, with no class entries permitted, so normal ZIP timestamp regeneration does not invalidate evidence. The generated API output directory may be absent after packaging or contain exactly the pinned class compiled from the checked-in annotation; the API resource directory may be absent or empty. The isolated API compile is always pinned independently. Nothing changes or regenerates a production dependency.

## Reproduce offline

From the repository root, with the restored native cache and original inputs available:

```
python3 ntm-port-1.12.2/porting/tests/native-block-placement/check_source.py
python3 ntm-port-1.12.2/porting/tests/native-block-placement/run.py
python3 ntm-port-1.12.2/porting/tests/native-block-placement/run.py --verify-attribution
```

The last command uses `git archive` of the exact CP93 baseline, validates all 4,071 before/after source hashes, and overlays only these 29 pinned production files. It does not silently compare newer unrelated production edits to historical diagnostic totals. It requires that historical commit to be locally available. The default command checks current production source inverses, current callback closure, genuine dependencies and bounded runtime behavior without a project build.
