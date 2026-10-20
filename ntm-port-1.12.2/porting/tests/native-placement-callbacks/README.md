# Bounded native placement callbacks

Base: ea933e28fe3a1a9a60e9d438411a02182b2ab22a (sealed CP70).

72 production methods change only their native callback declaration and introduce coordinate locals. The rest of every file and every original method body is byte-for-byte unchanged (including CRLF files). The new state parameter is deliberately unused. This does not repair remaining world metadata/orientation/logging APIs in those bodies and is not a complete-owner compilation or game-parity claim.

## Scope and source evidence

`manifest.json` enumerates exact file hashes, scoped edits and 14 excluded old-shaped declarations. The full Java compiler tree/type attribution parses 4,064 production units, resolves genuine native `Block.onBlockPlacedBy`, verifies the 72 selected owners are Block descendants and checks all 72 methods become actual native overrides. It explicitly records ERROR attribution rather than treating incomplete types as proof. The before/after diagnostics drop from 14,612 to 14,543, solely 69 removed override errors, without added errors (line shifts ignored). The difference from 72 is expected: CoreComponent lacked an annotation, and NukeAntimatter/NukeBalefire already overrode the old BlockMachineBase signature. All three now override the native method too.

Excluded: BlockDummyable's existing native/domain bridge and eight old-shaped descendant overrides; BlockOre's existing native/domain bridge; CompactLauncher, BlockBedrockOreTE, BlockStorageCrate and BlockMassStorage, whose explicit super calls need a separately justified body migration. No old-shaped compatibility entry point is added by this patch. Every existing native callback and excluded custom/domain overload stays byte-exact.

Descendant audit includes inherited BlockMachineBase behavior (WarController, MachineDiesel, MachineMicrowave and StorageDrum), selected NukeAntimatter/NukeBalefire overrides, and the four BlockChargeBase descendants. Explicit callback invocations are unchanged, and none is attributed to a selected owner in the baseline. The existing ModEventHandler furnace call remains an unrelated unresolved old call, not silently claimed fixed. Existing ItemConveyorWand and BlockWandLoot native calls retain their state reads and argument evaluation. The report retains full call receiver/type/owner evidence, including unresolved attribution.

`verify.py` offers `reverse_source(relative, text)` for integration into source-history chains, verifies exact inverses/forward replay, and rejects 216 independent source mutations. Paths outside this manifest are returned unchanged. No blanket textual normalization or method-name-only replacement is used as proof of API equivalence: selection is checked against compiler-resolved inheritance and exact old/new parameter types.

## Actual engine lifecycle

Original client `abh` is ItemBlock, and SRG maps `aji.a(World,int,int,int,EntityLivingBase,ItemStack)` to Block.func_149689_a. Its actual bytecode invokes the virtual callback after a successful placement and only when the block at the target remains the intended block. It passes the final target integer coordinates, original placer and stack, then calls the post-placement metadata callback. The callback is not the pre-placement orientation/state-selection hook.

Actual Forge 1.12.2 `ItemBlock.placeBlockAt` first calls World.setBlockState(pos,newState,11), returns false on failure, reads the resulting live state, and tests that its block remains the ItemBlock's owner. It then calls setTileEntityNBT, invokes Block.onBlockPlacedBy(world,pos,capturedState,player,stack), and optionally triggers the player advancement. The original placer/stack references survive unchanged. The supplied state is captured before setTileEntityNBT and can differ from a later world read; substituting it for original live reads is therefore not generally safe. The original body retains every world read and side effect in its prior order. Coordinate locals decode the supplied final BlockPos exactly once at entry; no extra world read or state lookup is introduced. The engine supplies non-null positions; no legacy null-position contract existed.

The genuine native dispatcher is invoked in 112 assertions: successful placement, rejected world write, replacement owner, negative/boundary coordinates, identity, order, and inherited/domain virtual dispatch. The exact transformed Floodlight callback is extracted unchanged into a Block-derived projection. Its setAngle collaborator records arguments, explicitly without claiming to test the separate angle/lighting body. Additional recording boundary fixtures verify player/stack identity and captured-state versus live-state divergence. A real World subclass overrides only tested world boundaries; unexpected chunk access throws. An Unsafe-allocated genuine EntityPlayerSP supplies identity without launching a client, and no fake net.minecraft/net.minecraftforge classes are created. These tests do not establish full owner compilation, rendering, chunk simulation, persistence, survival placement or whole-game behavior.

## Offline reproduction

Run with Java 8 and an existing real native classpath file, original client JAR, and 1.7.10 SRG ZIP:

    python3 porting/tests/native-placement-callbacks/run.py JAVA_HOME CLASSPATH_FILE LEGACY_CLIENT_JAR SRG_ZIP OUTPUT_DIRECTORY

The script reconstructs selected baseline sources from Git and links untouched files to avoid copying assets. It runs source inverses, baseline/native attribution, descendant/explicit-call audits, genuine native dispatch tests, and javap engine evidence. It does not download, install, change main, or launch Minecraft. `summary.json`, engine bytecode and logs are generated in OUTPUT_DIRECTORY. The current report accompanies these scripts as `verified-summary.json` and `verified-run.log`.
