# Native neighbor-dispatch and boolean-power proof

Baseline task provenance: CP68 `175b014cbb5c5990107fced236f7131f234998b6` (no repository was inspected or changed).

## Reproduce

Run `python3 prove.py > proof.log` in this directory.

The script uses Java 8 javap, `/tmp/nagaport-ae2-native-classpath.txt`, the supplied `/tmp/nagaport-legacy-client.jar`, and the included eight exact lines in `legacy-identities.srg` (original mapping hash in `mapping-provenance.json`). It reads inputs only and writes artifacts only in this directory. It performs no game launch, installation, download or project edit. The latest run passed all 12 assertion groups.

`selected-methods.javap` contains the focused bytecode evidence. `input-sha256.json` gives SHA-256 for each input archive, javap, classpath file, legacy SRG mapping and every examined class. Full raw javap is inspected in memory and can be regenerated from the pinned archives.

## Native callback contract

`World.neighborChanged(BlockPos targetPos, Block changedBlock, BlockPos fromPos)`:

1. Returns immediately if `isRemote` is true. No target state lookup occurs on that path.
2. Gets the state at targetPos once before the protected callback region.
3. Invokes `targetState.neighborChanged(thisWorld, targetPos, changedBlock, fromPos)`.
4. `BlockStateContainer.StateImplementation.neighborChanged` calls its associated block virtually as `block.neighborChanged(thisState, world, targetPos, changedBlock, fromPos)`.
5. Base `Block.neighborChanged` is an empty default implementation. There is no old `(World, int, int, int, Block)` dispatch in this chain.

Native WorldServer and WorldClient do not declare overrides of these three World methods in this artifact. The proof concerns this ordinary StateImplementation dispatch, not arbitrary third-party IBlockState implementations or World subclasses.

The callback is protected by `catch (Throwable)` over bytecode [14,25), with handler at 28. The state lookup is outside it. The handler makes a crash report labeled “Exception while updating neighbours”, adds a “Block being updated” category, describes the changedBlock as “Source block type”, adds targetPos and the retrieved target state, and throws ReportedException. There is no swallow/retry.

The supplied legacy vanilla client maps `ahb` to World and `aji` to Block. `ahb.e(III,aji)` has the same remote early return, gets the target block, then invokes `aji.a(ahb,III,aji)`, the mapped `func_149695_a` callback. Its callback-only Throwable region is [17,28), handler 31. Its diagnostic additionally attempts target metadata lookup, falling back to -1 if that lookup throws. Thus the change to an explicit IBlockState and source position is real, not an integer-coordinate overload alias.

## Native neighborhood notification

Both native wrappers first call ForgeEventFactory.onNeighborNotify using the source position's state. Cancellation returns before dispatch. The all-neighbors wrapper passes all six faces and its observer flag; the except wrapper removes the excluded face and passes false.

If uncanceled, callbacks run synchronously in west, east, down, up, north, south order. Each call is exactly:

`world.neighborChanged(sourcePos.direction(), changedBlock, sourcePos)`.

The except wrapper skips only the matching excluded face. Neither wrapper has its own isRemote guard or exception handler. The all-neighbors wrapper calls updateObservingBlocksAt only after all six calls, and only when its boolean flag is true. Thus a thrown callback aborts subsequent dispatch and observer update. The except wrapper has no observer update. Event-side face-set mutation is not consulted after the cancellation check in these bytecodes.

Legacy vanilla all-neighbors and except-side dispatch match that W/E/D/U/N/S order, coordinate offsets, and changedBlock argument. They have no native fromPos argument and no Forge event / observer step in the supplied client archive. This comparison is not an audit of a patched legacy Forge World.

Migration implication: replace the old callback signature with native `neighborChanged(IBlockState, World, BlockPos, Block, BlockPos)`, preserve old x/y/z meaning as targetPos coordinates and preserve changedBlock. Existing old bodies had no fromPos input. Do not invent direction-dependent behavior or reconstruct fromPos from targetPos. Native World forwards a direct caller's fromPos unchanged; only the audited six-neighbor wrappers establish it as their original source position.

## Boolean power query

The supplied legacy `ahb.v(III)Z` maps to World.func_72864_z, the old boolean query identified in the task as isBlockIndirectlyGettingPowered. Native `World.isBlockPowered(BlockPos)Z` is its boolean dispatcher counterpart in this classpath:

- Both query down/up/north/south/west/east in that order.
- The side values 0/1/2/3/4/5 correspond to DOWN/UP/NORTH/SOUTH/WEST/EAST; the EnumFacing definitions and unit BlockPos helpers are checked.
- Both return true immediately when the corresponding queried power is >0; otherwise false after all six.
- The proof follows both actual instruction CFGs for all 729 assignments of six power signs (-1,0,+1), asserting return and exact query trace.
- Corresponding strong-power aggregation CFGs are checked with all 729 assignments from {0,1,15}, asserting max result, >=15 short-circuit and query trace.

Native leaf `getRedstonePower` obtains target state, consults Forge Block.shouldCheckWeakPower, then chooses aggregate strong power or state.getWeakPower. The default hook returns state.isNormalCube. Legacy vanilla `ahb.g(IIII)I` branches on Block.func_149721_r, then chooses aggregate strong power or block weak power. This supports the dispatcher-level adaptation to `world.isBlockPowered(new BlockPos(x,y,z))`, not a claim that every modded block or redstone circuit is identical across versions.

This native classpath has no World declaration named isBlockIndirectlyGettingPowered; its integer aggregate is named `getRedstonePowerFromNeighbors(BlockPos)`. It must not be substituted for a boolean condition merely because another mapping version gives it an older-looking name.

## Limits

This is static javap plus an explicit small symbolic CFG interpreter, with external world/state/event/power calls represented by declared symbolic leaves. It is not a Minecraft runtime test, integration test, all-modded-block parity proof, migration body audit, or evidence that any bomb detonates correctly in-game. Existing classes, signatures, gates, stacks, offsets, branch destinations, finite condition partitions, and byte hashes are the evidence.
