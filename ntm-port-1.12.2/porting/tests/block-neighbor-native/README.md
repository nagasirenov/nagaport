# Native neighbor callbacks: bounded CP68 family

Baseline: `175b014cbb5c5990107fced236f7131f234998b6`. Exactly 14 callback methods in 14 files change. Main is untouched. No downloads, installations, source exclusions, fake Minecraft/Forge classes, gameplay balancing, or general compatibility facade.

## Selected scope and production change

Selected: NukeAntimatter, NukeBalefire, NukeCustom, ExplosiveCharge, DetMiner, BlockPlasticExplosive, BombFlameWar, BombFloat, BlockTNTBase, BombThermo, BlockChargeBase, PistonInserter, CrystalPulsar and BlockOreBasalt.

Each old scalar `onNeighborBlockChange(World,x,y,z,Block changed)` becomes the actual native `neighborChanged(IBlockState,World,BlockPos target,Block changed,BlockPos fromPos)`. The changed-block parameter keeps its original identity/name. Extract only used x/y/z locals from the target position. Existing bodies do not have source-position context, so neither supplied state nor fromPos is used to invent new behavior. Original world-based state/metadata reads stay world-based. Preserve `@Override` and every original statement's position and order.

Ten callbacks additionally require one exact power-read adaptation: old boolean `world.isBlockIndirectlyGettingPowered(x,y,z)` becomes native boolean `world.isBlockPowered(new BlockPos(x,y,z))`. This is not replacement with an integer aggregate. The bounded original/native dispatcher proof establishes the same down/up/north/south/west/east order, side indices, >0 condition and short-circuit behavior. Existing native Forge power hooks remain in force; this is not a claim that every unported modded power block works.

The scanner records 71 live old scalar declarations before this batch, not 71 compiler errors. Fifty-seven are excluded. Already-native BlockDummyable bridges and their scalar overrides remain intact. Plants, fluids, old metadata mutation, scheduled-tick paths, and nuclear callbacks still calling an inherited old player-destruction method are not mechanically converted. Those need separate lifecycle/metadata attribution rather than silently constructing a substitute state or changing effects. The intentionally empty basalt override is preserved, not used to replace any meaningful behavior.

## Hierarchy, callers and bodies

`selection.json` contains the 14 exact old/new methods, locals, ten explicit power-expression pairs and all 57 excluded method bodies. `baseline.zip` stores the 14 complete baseline files plus six unchanged collaborator files. Its bytes were checked against the exact Git CP68 object. `source-proof.py` verifies complete whole-file forward/inverse equality, not just callback text. Original pristine methods are recorded separately: ten match the CP68 methods exactly; four contain only prior port adaptations, displayed with normalized indentation in `original-to-cp68.diff` (world reads, air removal, side solidity and the previously proven bounded air placement). This batch preserves those existing adaptations.

`attribution.json` contains full-source declaration, class-hierarchy and occurrence attribution. All relevant callback declarations are top-level: no anonymous/nested override is missed. Nine selected-root descendants inherit unchanged native dispatch: five TNT variants and four attachment-charge variants. None declares an excluded scalar override. BlockTNTBase, BlockPlasticExplosive and ExplosiveCharge have an already-native BlockDetonatable ancestor; their selected native overrides now correctly supersede that parent, matching their original child bodies rather than invoking both. None of the selected bodies calls its old neighbor method or a super neighbor method.

All remaining explicit scalar neighbor calls resolve to excluded families: BlockDummyable's established native-to-domain bridge; multiblock Beam/door/launchpad/reactor overrides; plant/fluid/vanilla-vine/farmland super calls; and BlockModDoor's recursive scalar call. No selected method is an explicit scalar-call target. No bridge is deleted or duplicated. Callback virtual dispatch and the TNT helper's subsequent virtual prime call are retained. The inherited old player-destruction lifecycle of other nuclear classes is specifically excluded; BlockTNTBase differs because it owns the scalar domain method that forwards exact metadata 1 to its own prime hook.

## Reproduction and evidence

```
python3 source-proof.py --report attribution.json
python3 run.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt --mutants
python3 dispatch/prove.py > dispatch/proof.log
```

All 14 complete changed methods compile with genuine native Block/World/IBlockState/BlockPos and `@Override`. The runner additionally compiles the exact unchanged native BlockDetonatable callback, two TNT domain helpers, three attachment metadata methods, and complete production WorldBlockState, WorldAirRemoval, FacingCompat, ForgeDirection and IBomb files. Owner fixtures are explicitly bounded; this is not compilation of 14 whole production owners.

Runtime tests use actual World/Chunk state, native redstone-block power, World.neighborChanged through StateImplementation, native all-neighbor and excluded-face notification, and genuine removal through the complete WorldAirRemoval collaborator. They cover server/client World gates versus direct callback calls, exact state/target/changed-block/fromPos identity, ignored supplied-state mismatch, negative coordinates, all six power faces and no power, exact power-read order/short circuit, metadata 0..15 attachment support including UNKNOWN, no unintended parent ignition, crystal remote/check ordering, thermal identity branches, BombFloat's unconditional world assignment, and TNT fire and power branches. Native removal precedes thermal effects. A throwing TNT prime hook prevents removal; actual World wraps the same cause in ReportedException. Target-state lookup failure precedes the catch region and callback and remains unwrapped. An inherited native TNT callback reaches a derived NTM prime override exactly once.

Domain explosion, crystal and piston collaborators are explicit recording boundaries, not fabricated implementations or claims of completed gameplay. The thermal native createExplosion call is intercepted at a declared World override to inspect exact arguments/order after real removal; its explosion algorithm is not executed. The primary runtime path uses real native redstone lookup, not a boolean power stub. Original TNT scalar helpers retain exact metadata/entity arguments and real native removal; spawning production explosive entities remains outside the fixture.

Four runtime mutants must fail: inverted power predicate, swapped target coordinate, doubled prime, and inverted attachment solidity. Restoring a scalar callback signature fails native `@Override` compilation. `native-test.log` gives the passing count, and `native-attribution.log` captures the compiled methods. No old fake-class equivalence claim is made: original evidence is exact source and actual original archive bytecode/CFG attribution.

## Native notification and power contract

`dispatch/prove.py` checks genuine native and original archive bytecode with pinned archive and per-class hashes. Twelve assertion groups cover:

- Native World server gate, target-state lookup, exact virtual state-to-block forwarding and callback-only Throwable wrapper.
- West/east/down/up/north/south notification order and original source position as fromPos; excluded-face behavior, Forge cancellation and observer ordering; exceptions abort later notifications.
- Corresponding vanilla 1.7 changed-block/coordinate/server-gate behavior, which has no fromPos input.
- Native boolean isBlockPowered versus original func_72864_z across 729 six-side sign assignments and another 729 strong-power assignments, including identical query traces and short-circuits.

The native Forge `shouldCheckWeakPower` hook differs from the supplied vanilla legacy client; only its default normal-cube and dispatcher correspondence are asserted. Forge cancellation/observer differences are preserved, not erased. Direct block/state callback calls have no implicit server gate; only the actual World dispatcher supplies it. The proof is static actual-bytecode/CFG verification, not a launched game or universal circuit equivalence. The eight consumed SRG identity lines are included with their original full mapping hash. No Minecraft jar is distributed.

## Integration and limits

`apply-fragments.py --module MODULE` validates only the exact approved complete-method fragments and inverses. Add `--write` only for authorized integration. Its dry-run receipt confirms same-file activation/opacity/naming changes can be preserved without replacing their files. The strict whole-file source proof targets this isolated CP68 baseline; a composed tree uses fragment receipts to demonstrate preservation of all unrelated bytes.

No full-mod compile, complete owner, live server/client, rendered effects, explosive/piston behavior, or world-save acceptance. Metadata/tick/plant exclusions remain unresolved. Existing CP37+ policy and unrelated lighting-consent holds remain unchanged.
