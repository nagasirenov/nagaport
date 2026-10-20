# Native captured-state air queries

Baseline: sealed CP91 `f5ce86cd607379cc9810896cf91d1fee86ce6331`. Exactly 17 existing Block-local declarations capture their original native IBlockState; exactly 17 corresponding isAir calls use it. Fourteen production files change. No helper/shim, extra world read, default-state substitution, new owner override, dependency change, download, source exclusion, or full-project build is introduced.

## Finite source rule

Original:

    Block block = WorldBlockState.read(original receiver world/XYZ).getBlock();
    ... existing intervening statements and short-circuit expression ...
    block.isAir(original context world, original context X, Y, Z)

Native:

    IBlockState nativeAirState = WorldBlockState.read(original receiver world/XYZ);
    Block block = nativeAirState.getBlock();
    ... byte-identical intervening statements and short-circuit expression ...
    block.isAir(nativeAirState, original context world, new BlockPos(original context X, Y, Z))

The original read and getBlock invocation still execute once, at the same declaration, in the same order. The receiver local and four original query arguments retain their order and evaluation count. The added state argument is a local reference, not another world lookup. State and receiver stay paired by construction: the receiver comes directly from that captured state's getBlock(). Subsequent predicates, world reads, callbacks or mutations never cause a state reread or replacement of the original receiver. Raw integer context coordinates are not clipped/packed. Original CRLF and all unrelated source bytes are preserved.

The 17 declarations occur in the same existing scopes; there was no nativeAirState identifier before the migration. Existing early returns, loop conditions and short-circuit checks remain intact. For example, ICFController's tile-laser mutation branch breaks before the air query; the rest of its control flow is unchanged. BlockFallout's leaves check and the DroneWaypoint solidity checks still run before the query, using their original expressions.

## Intentional receiver/context mismatches

Four calls in DroneWaypoint / DroneWaypointRequest read the neighbor receiver at `(x-dir.offsetX, y-dir.offsetY, z-dir.offsetZ)` but query it with `(x,y,z)`. They now receive the captured neighbor state and the original `(x,y,z)` query context. This is not silently corrected or substituted with the state at the query position. The actual native stone-state/air-target counterexample and a stateful virtual-dispatch fixture reject such substitution. They also reject replacing the captured state with the receiver's default state.

The other thirteen selected calls retain their original receiver/context positions. Full source ranges and intervening code are recorded in manifest.json.

Twenty-four calls remain outside this bounded family: 22 inline receiver-read expressions need their own control-flow-safe capture treatment; the Block-parameter entrypoints TileEntityMachineExcavator.shouldIgnoreBlock and TileEntityMachineMiningLaser.canBreak need explicit caller/API ownership closure. They are not renamed or hidden from compilation. Placement, hardness and other block APIs remain separate work.

## Owner and engine boundary

The original NTM JAR has exactly one isAir override: BlockBeamBase `(IBlockAccess;III)Z`, whose complete instructions are ICONST_1, IRETURN. The only current source override is already-native BlockBeamBase `(IBlockState,IBlockAccess,BlockPos)`. Original and current owner closure is the same three genuine classes: BlockBeamBase, FloodlightBeam and SpotlightBeam. No owner code changes here.

Forge1614's original Block hook is `getMaterial() == Material.air`. Native Block checks the supplied state's material. Every selected receiver is paired with its actual captured native state, not another block's state. Full current-source parsing finds no getMaterial declarations. Actual dependency bytecode inventory finds the native Block getter and native state forwarding getter, not a stateful custom Block material override. Runtime checks execute all 5,485 actual native registry states, including state ownership and the actual native base air method.

The one native external Block air override is AE2 rv6-stable-7 BlockPaint. An actually constructed BlockPaint owns its actual native state, has non-AIR material, and returns true through native isAir virtual dispatch. That native behavior is preserved rather than bypassed with a material-only test. The inventory also lists unrelated AE2 IIngredient.isAir() separately by its zero-argument descriptor. This is NOT universal old/new foreign-mod parity, nor a promise about future dependencies or arbitrary custom state implementations.

## Executed evidence

- 14 byte-exact source inverses / forward round trips; 34 exact edit ranges; 42 rejected drift mutations.
- Actual Java 8 attribution identifies all 17 old/new receivers as net.minecraft.block.Block. Every new state argument is net.minecraft.block.state.IBlockState and each new invocation resolves exactly native Block.isAir(IBlockState,IBlockAccess,BlockPos). The 217 original / 200 modified partial-source diagnostics are not a successful project compile or a whole-build error prediction. The original NTM artifact is a type/ancestry dependency for this partial analysis only, not native runtime code.
- Scoped compilation of the entire genuine current BlockBeamBase, WorldBlockState and ForgeDirection classes against the genuine native dependency classpath succeeds. No NTM dependency stubs are needed. A minimal concrete test subclass supplies BlockBeamBase's abstract tile factory, but the tested isAir method and state container are the actual production class.
- 11,049 runtime assertions: 5,485 actual registry states; actual production Beam states 0..15; actually constructed AE2 BlockPaint; original material classes / original Block getter with an exact extracted Forge base method; exact ASM-copied original Beam isAir body; stateful native override and evaluation fixtures.
- Capture fixtures verify one original read, no added world read, non-default metadata retained, inherited native dispatch, original context object and raw coordinates, neighbor mismatch and later-world-mutation behavior, null-state timing, argument exceptions and and/or short-circuiting.

OriginalForgeAir is an explicitly extracted Forge method in a subclass of the ACTUAL old Minecraft Block using actual old Material objects, not a full Forge-patched engine. The original Beam method is an exact bytecode projection, not execution of the entire old Beam class. The selected fourteen containing caller classes are not certified as compiling by themselves. Full mod build, startup, machine lifecycle, networking, radiation, gameplay and live AE2 behavior remain unverified here. The parent supplies combined compilation separately.

## Reproduce offline

    python3 porting/tests/native-block-air-captures/run.py \
      --java-home /workspace/shared/nagaport-toolchain/java8 \
      --classpath-file /workspace/shared/nagaport-toolchain/native-classpath.txt \
      --original-jar /workspace/shared/nagaport/mods1710/HBM-NTM-.1.0.27_X5645_H261.jar \
      --legacy-client /tmp/nagaport-legacy-client.jar \
      --forge1614 /tmp/nagaport-forge1614-userdev.jar \
      --verify-attribution

All inputs are existing artifacts pinned in artifact-hashes.json. The runner checks the original hook directly inside forgepatches.zip in Forge1614 userdev. It uses temporary output and never mutates project sources. No proprietary binary is included in the evidence.
