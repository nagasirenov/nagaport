# Normal-cube dispatch audit

Read-only audit of the original NTM archive, Minecraft 1.7.10 + Forge 1614 inputs, actual 1.12.2 Forge 2768/dependency classpath, and sealed CP92 source commit `0675e599e5ee31a60776b560af792372df191790`. No production edit, dependency download/build, full project build, network request, or push was performed.

## Decision

Safe migration within the audited implementation closure:

1. A block receiver obtained from an existing captured state becomes `block.isNormalCube(capturedState)`, using exactly the state that supplied that receiver. Retain capture placement, branching, short circuiting, and snapshots across mutations.
2. An inline `WorldBlockState.read(...).getBlock().isNormalCube()` becomes `WorldBlockState.read(...).isNormalCube()`. This preserves one guarded read and dispatches to the same actual block with that exact state.
3. Do not substitute default state, combine opacity/full-cube/power tests by hand, reread after mutation, or select the world/position overload.
4. Secondary closure requires PowerDetector's original constant-true `canProvidePower()` to become `canProvidePower(IBlockState)`. CP92 still had the obsolete signature. The consuming migration is responsible for verifying its corrected declaration/runtime behavior.

## Original semantics and closure

- `aji.r()Z` is original `Block.isNormalCube`/`func_149721_r`. Exact bytecode implements `J.k() && d() && !f()` in that short-circuit order. `J` is the block Material, `awt.k()` its opacity, `aji.d()` render-as-normal-block, and `aji.f()` can-provide-power. Base `d()` returns true; base `f()` returns false. `legacy-block.javap` supplies the full executable evidence for a real legacy fixture.
- None of 5,255 original NTM classes declares `isNormalCube` or `func_149721_r`. In the 1,803-class original engine, only Block/aji declares `r()Z` within the Block subclass closure. Forge's 838 binary classes contain no such declaration; all relevant Forge patch hits are retained. Forge adds a distinct world-aware overload but does not change the zero-argument predicate.
- Original power overrides are exactly MachineRadar, MachineRadarLarge, PowerDetector, RadioTorchLogic, RadioTorchReceiver. The original PowerDetector method `func_149744_f()Z` is constant true and has no NTM descendant. CP92 had native signatures for the other four and an obsolete zero-argument signature only for PowerDetector. `machine_detector` uses Material.IRON and inherited full cube, so leaving that obsolete signature would change its normal-cube result from false to true.
- Original NTM has 151 render-normal declarations. Their CP92 source counterparts comprise 139 explicit isFullCube declarations, seven legacy declarations reached by BlockDummyable's native virtual bridge, and five inherited native cases: BlockMetalFence inherits BlockFence's false full-cube method; FoundryCastingBase, FoundryChannel, FoundryOutlet, FoundryTank inherit FoundryBlock's false method. The seven bridge descendants are MachineCyclotron, MachineGasDock, MachineIGenerator, MachineRocketAssembly, RBMKBase, RBMKConsole, RBMKRod. These are declaration/dispatch closure findings, not a new whole-project compilation proof.
- Opacity is Material.isOpaque, not Block.isOpaqueCube. NTM declares no material getter or material-opacity override. Its sole custom Material descendant is MaterialGas: its blocksMovement false override supplies the secondary opacity dependency and is already native in CP92. Legacy Material opacity is `!translucent && blocksMovement()`; native Material uses the same chain. No weak-power value query is needed by normal-cube; only canProvidePower's boolean is involved.

## Actual native dispatch

- Native Block.isNormalCube(IBlockState) evaluates `state.getMaterial().isOpaque() && state.isFullCube() && !state.canProvidePower()` in that order.
- StateImplementation.isNormalCube is NOT cached in the actual Forge 2768 jar. Each call reads its final block field and invokes virtual `block.isNormalCube(this)`. Its getBlock method returns that same field. Material/full-cube/power state methods likewise delegate to the block on each call.
- Complete native classpath hierarchy scan finds only two concrete IBlockState implementations: BlockStateContainer.StateImplementation and Forge ExtendedBlockState.ExtendedStateImplementation. Extended state inherits getBlock/isNormalCube and all relevant delegates unchanged. BlockStateBase is abstract; the other entries are interfaces. CP92 NTM source declares no custom state subtype or isNormalCube method. Hence eliminating the explicit getBlock call in the inline form does not bypass a custom getter in this concrete audited closure.
- AEBaseBlock is the only dependency block declaration overriding isNormalCube; no vanilla block subclass does. Its one-argument final method returns `isFullSize() && isOpaque()`, whereas its world/position final overload returns only `isFullSize()`. Both approved forms retain the one-argument override. Choosing the context overload or reimplementing Block's base formula would be wrong for actual AE2 objects.

## Reproduce and inspect

Run `python3 scan.py` from this directory. It regenerates candidate evidence and strictly compares it with the retained files; `--record` explicitly records a new snapshot. The repository root follows the relocated evidence directory. Other inputs are read-only absolute paths embedded in the script. The script reads CP92 with `git archive`, never the changing worktree. `manifest.json` pins all genuine archive/jar inputs and API class files by SHA-256, plus the native classpath file and repository commit. Only compileDummy.jar and providedDummy.jar use exact ZIP entry-name/payload pins, rejecting any class or additional entry while ignoring build-regenerated container timestamps. `inventory.json` records class counts, declarations, dispatch closure, source declaration inventory, and shadowed duplicate classes. `scan.log` is the concise successful run output. Full javap dumps and relevant Forge patches accompany it.

The classpath contains an absent `build/resources/api` entry, explicitly recorded in the manifest. It contributes no executable classes. Five duplicate class entries are reported; hierarchy analysis follows actual first-entry classpath precedence. Final native inventory is 48,248 entries and 48,243 unique classes (including three API classes).

## Limits

This is a bounded dispatch/dependency audit, not full project compilation, arbitrary third-party IBlockState equivalence, full compatibility with unlisted runtime transformers, or a proof that every version-specific vanilla block has identical behavior between 1.7.10 and 1.12.2. Pinned source findings describe CP92 before the consuming migration, intentionally unaffected by concurrent source edits. The consuming task's capture inventory/runtime fixtures establish changed call-site behavior and the final PowerDetector signature. No claim is made about unrelated zero-argument/context queries outside that task.
